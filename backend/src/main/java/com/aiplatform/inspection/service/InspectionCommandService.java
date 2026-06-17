package com.aiplatform.inspection.service;

import com.aiplatform.inspection.adapter.AdapterResult;
import com.aiplatform.inspection.adapter.CameraSdkAdapter;
import com.aiplatform.inspection.adapter.StreamDescriptor;
import com.aiplatform.inspection.adapter.VehicleVideoAdapter;
import com.aiplatform.inspection.adapter.VendorSdkAdapterRegistry;
import com.aiplatform.inspection.domain.AiTask;
import com.aiplatform.inspection.domain.CommandResult;
import com.aiplatform.inspection.domain.EdgeEvent;
import com.aiplatform.inspection.domain.EdgeHeartbeat;
import com.aiplatform.inspection.domain.IntegrationConfig;
import com.aiplatform.inspection.domain.InspectionTask;
import com.aiplatform.inspection.domain.VideoChannel;
import com.aiplatform.inspection.domain.VideoSession;
import com.aiplatform.inspection.repository.InspectionRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.Map;
import java.util.Set;

@Service
public class InspectionCommandService {
    private static final Set<String> ALARM_STATUSES = Set.of("new", "processing", "false_positive", "dispatched", "closed");
    private static final Set<String> MODEL_STATUSES = Set.of("candidate", "canary", "production", "archived");

    private final InspectionRepository repository;
    private final VendorSdkAdapterRegistry adapterRegistry;

    public InspectionCommandService(InspectionRepository repository, VendorSdkAdapterRegistry adapterRegistry) {
        this.repository = repository;
        this.adapterRegistry = adapterRegistry;
    }

    public CommandResult updateAlarmStatus(String id, String status) {
        String targetStatus = normalizeStatus(status, "processing");
        requireAllowed(targetStatus, ALARM_STATUSES, "Unsupported alarm status");
        Instant updatedAt = Instant.now();
        return repository.updateAlarmStatus(id, targetStatus, updatedAt)
            .map(alarm -> {
                repository.appendAuditLog("system", "ALARM_STATUS_UPDATE", "alarm", alarm.id(), "success");
                return new CommandResult(alarm.id(), alarm.status(), "alarm status updated", updatedAt, Map.of("type", alarm.type(), "device", alarm.device()));
            })
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Alarm not found: " + id));
    }

    public CommandResult publishModel(String id, Map<String, Object> strategy) {
        String targetStatus = normalizeStatus(asString(strategy.get("targetStatus")), "canary");
        requireAllowed(targetStatus, MODEL_STATUSES, "Unsupported model status");
        Instant updatedAt = Instant.now();
        return repository.updateModelStatus(id, targetStatus, strategy)
            .map(model -> {
                repository.appendAuditLog("system", "MODEL_PUBLISH", "model", model.id(), "success");
                return new CommandResult(model.id(), model.status(), "model publish requested", updatedAt, Map.of(
                    "algorithm", model.algorithm(),
                    "version", model.version(),
                    "strategy", strategy
                ));
            })
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Model not found: " + id));
    }

    public CommandResult createInspectionTask(Map<String, Object> payload) {
        String name = normalizeStatus(asString(payload.get("name")), "即时巡检任务");
        String type = normalizeStatus(asString(payload.get("type")), "immediate");
        String priority = normalizeStatus(asString(payload.get("priority")), "high");
        Instant plannedAt = Instant.now();
        InspectionTask task = repository.createInspectionTask(name, type, priority, plannedAt);
        repository.appendAuditLog("system", "TASK_CREATE", "inspection_task", task.id(), "success");
        return new CommandResult(task.id(), task.status(), "inspection task created", plannedAt, Map.of(
            "name", task.name(),
            "type", task.type(),
            "priority", task.priority()
        ));
    }

    public CommandResult testIntegration(String id) {
        IntegrationConfig config = integrationById(id);
        AdapterResult result = "vehicle".equals(config.sourceType())
            ? vehicleAdapter().testConnection(config)
            : cameraAdapter(config.sourceType()).testConnection(config);
        repository.appendAuditLog("system", "INTEGRATION_TEST", "integration", id, result.success() ? "success" : "failed");
        return new CommandResult(id, config.status(), result.message(), Instant.now(), result.data());
    }

    public CommandResult syncIntegration(String id) {
        IntegrationConfig config = integrationById(id);
        String selectedVendor;
        int channelCount;
        if ("vehicle".equals(config.sourceType())) {
            VehicleVideoAdapter adapter = vehicleAdapter();
            selectedVendor = adapter.vendor();
            channelCount = adapter.syncChannels("vehicle-001").size();
        } else {
            CameraSdkAdapter adapter = cameraAdapter(config.sourceType());
            selectedVendor = adapter.vendor();
            channelCount = adapter.syncChannels(config).size();
        }
        repository.appendAuditLog("system", "INTEGRATION_SYNC", "integration", id, "success");
        return new CommandResult(id, "synced", "integration channels synced", Instant.now(), Map.of(
            "sourceType", config.sourceType(),
            "vendor", selectedVendor,
            "channelCount", channelCount
        ));
    }

    public VideoSession createVideoSession(Map<String, Object> payload) {
        String channelId = normalizeStatus(asString(payload.get("channelId")), "");
        String protocol = normalizeStatus(asString(payload.get("protocol")), "webrtc");
        VideoChannel channel = repository.videoChannelById(channelId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Video channel not found: " + channelId));
        StreamDescriptor descriptor;
        try {
            descriptor = "vehicle".equals(channel.sourceType())
                ? vehicleAdapter().openPreview(channel.id(), protocol)
                : cameraAdapter(channel.sourceType()).openPreview(channel.id(), protocol);
        } catch (IllegalStateException exception) {
            repository.appendAuditLog("system", "VIDEO_SESSION_CREATE", "video_channel", channel.id(), "failed");
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, exception.getMessage(), exception);
        }
        if (descriptor.url() == null || descriptor.url().isBlank()) {
            repository.appendAuditLog("system", "VIDEO_SESSION_CREATE", "video_channel", channel.id(), "failed");
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "video preview unavailable for " + channel.sourceType() + " channel " + channel.id());
        }
        Instant startedAt = Instant.now();
        VideoSession session = repository.createVideoSession(channel.id(), descriptor.url(), descriptor.transport(), startedAt, startedAt.plusSeconds(30 * 60));
        repository.appendAuditLog("system", "VIDEO_SESSION_CREATE", "video_channel", channel.id(), "success");
        return session;
    }

    public CommandResult snapshot(String channelId) {
        VideoChannel channel = repository.videoChannelById(channelId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Video channel not found: " + channelId));
        AdapterResult result = "vehicle".equals(channel.sourceType())
            ? vehicleAdapter().snapshot(channelId)
            : cameraAdapter(channel.sourceType()).snapshot(channelId);
        repository.appendAuditLog("system", "VIDEO_SNAPSHOT", "video_channel", channelId, result.success() ? "success" : "failed");
        return new CommandResult(channelId, result.success() ? "success" : "failed", result.message(), Instant.now(), result.data());
    }

    public CommandResult record(String channelId, Map<String, Object> payload) {
        int seconds = intValue(payload.get("seconds"), 12);
        VideoChannel channel = repository.videoChannelById(channelId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Video channel not found: " + channelId));
        AdapterResult result = "vehicle".equals(channel.sourceType())
            ? vehicleAdapter().record(channelId, seconds)
            : cameraAdapter(channel.sourceType()).record(channelId, seconds);
        repository.appendAuditLog("system", "VIDEO_RECORD", "video_channel", channelId, result.success() ? "success" : "failed");
        return new CommandResult(channelId, result.success() ? "success" : "failed", result.message(), Instant.now(), result.data());
    }

    public CommandResult createAiTask(Map<String, Object> payload) {
        String channelId = normalizeStatus(asString(payload.get("channelId")), "");
        VideoChannel channel = repository.videoChannelById(channelId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Video channel not found: " + channelId));
        String algorithmCode = normalizeStatus(asString(payload.get("algorithmCode")), "smoke_fire");
        String modelVersion = normalizeStatus(asString(payload.get("modelVersion")), "production");
        Instant createdAt = Instant.now();
        AiTask task = repository.createAiTask(channel.sourceType(), channel.id(), algorithmCode, modelVersion, "running", 0.88, "/evidence/" + channel.id() + "-" + algorithmCode + ".jpg", createdAt);
        repository.appendAuditLog("system", "AI_TASK_CREATE", "ai_task", task.id(), "success");
        return new CommandResult(task.id(), task.status(), "ai task created", createdAt, Map.of(
            "sourceType", task.sourceType(),
            "channelId", task.channelId(),
            "algorithmCode", task.algorithmCode(),
            "modelVersion", task.modelVersion()
        ));
    }

    public CommandResult heartbeat(Map<String, Object> payload) {
        String nodeId = normalizeStatus(asString(payload.get("nodeId")), "edge-001");
        EdgeHeartbeat heartbeat = new EdgeHeartbeat(
            nodeId,
            normalizeStatus(asString(payload.get("status")), "online"),
            normalizeStatus(asString(payload.get("ipAddress")), "127.0.0.1"),
            Map.of("usage", intValue(payload.get("gpuUsage"), 62), "temperature", intValue(payload.get("temperature"), 66)),
            intValue(payload.get("onlineChannels"), 0),
            intValue(payload.get("aiRunning"), 0),
            intValue(payload.get("cachedEvents"), 0)
        );
        repository.appendAuditLog("system", "EDGE_HEARTBEAT", "edge_node", nodeId, "success");
        return new CommandResult(nodeId, heartbeat.status(), "edge heartbeat accepted", Instant.now(), Map.of(
            "nodeId", heartbeat.nodeId(),
            "gpu", heartbeat.gpu(),
            "onlineChannels", heartbeat.onlineChannels(),
            "aiRunning", heartbeat.aiRunning(),
            "cachedEvents", heartbeat.cachedEvents()
        ));
    }

    public CommandResult appendEdgeEvent(Map<String, Object> payload) {
        Instant detectedAt = Instant.now();
        EdgeEvent event = repository.appendEdgeEvent(
            normalizeStatus(asString(payload.get("eventType")), "ai_alarm"),
            normalizeStatus(asString(payload.get("sourceType")), "camera"),
            normalizeStatus(asString(payload.get("sourceId")), "unknown"),
            normalizeStatus(asString(payload.get("severity")), "medium"),
            normalizeStatus(asString(payload.get("title")), "AI识别事件"),
            doubleValue(payload.get("latitude"), 31.2312),
            doubleValue(payload.get("longitude"), 121.4741),
            normalizeStatus(asString(payload.get("evidenceUrl")), "/evidence/edge-event.jpg"),
            detectedAt
        );
        repository.appendAuditLog("system", "EDGE_EVENT_APPEND", "edge_event", event.id(), "success");
        return new CommandResult(event.id(), event.status(), "edge event accepted", detectedAt, Map.of(
            "title", event.title(),
            "sourceType", event.sourceType(),
            "severity", event.severity(),
            "evidenceUrl", event.evidenceUrl()
        ));
    }

    public CommandResult updateFlightTaskStatus(String id, String status) {
        String targetStatus = normalizeStatus(status, "running");
        Instant updatedAt = Instant.now();
        return repository.updateFlightTaskStatus(id, targetStatus, updatedAt)
            .map(task -> {
                repository.appendAuditLog("system", "FLIGHT_TASK_STATUS", "flight_task", task.id(), "success");
                return new CommandResult(task.id(), task.status(), "flight task status updated", updatedAt, Map.of(
                    "routeName", task.routeName(),
                    "plannedAt", task.plannedAt()
                ));
            })
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Flight task not found: " + id));
    }

    private String normalizeStatus(String value, String fallback) {
        if (value == null || value.isBlank()) {
            return fallback;
        }
        return value.trim();
    }

    private String asString(Object value) {
        return value == null ? null : String.valueOf(value);
    }

    private void requireAllowed(String status, Set<String> allowed, String message) {
        if (!allowed.contains(status)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, message + ": " + status);
        }
    }

    private IntegrationConfig integrationById(String id) {
        return repository.integrations().stream()
            .filter(integration -> integration.id().equals(id))
            .findFirst()
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Integration not found: " + id));
    }

    private CameraSdkAdapter cameraAdapter(String sourceType) {
        return adapterRegistry.cameraAdapter(sourceType);
    }

    private VehicleVideoAdapter vehicleAdapter() {
        return adapterRegistry.vehicleAdapter();
    }

    private int intValue(Object value, int fallback) {
        if (value == null) {
            return fallback;
        }
        try {
            return Integer.parseInt(String.valueOf(value));
        } catch (NumberFormatException exception) {
            return fallback;
        }
    }

    private double doubleValue(Object value, double fallback) {
        if (value == null) {
            return fallback;
        }
        try {
            return Double.parseDouble(String.valueOf(value));
        } catch (NumberFormatException exception) {
            return fallback;
        }
    }
}
