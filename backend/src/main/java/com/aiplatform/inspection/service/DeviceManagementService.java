package com.aiplatform.inspection.service;

import com.aiplatform.inspection.adapter.AdapterResult;
import com.aiplatform.inspection.adapter.CameraSdkAdapter;
import com.aiplatform.inspection.adapter.VehicleVideoAdapter;
import com.aiplatform.inspection.adapter.VendorSdkAdapterRegistry;
import com.aiplatform.inspection.domain.CommandResult;
import com.aiplatform.inspection.domain.Device;
import com.aiplatform.inspection.domain.IntegrationConfig;
import com.aiplatform.inspection.domain.VideoChannel;
import com.aiplatform.inspection.repository.DeviceManagementRepository;
import com.aiplatform.inspection.repository.InspectionRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Service
public class DeviceManagementService {
    private static final Set<String> SOURCE_TYPES = Set.of("camera", "drone", "vehicle");

    private final DeviceManagementRepository deviceRepository;
    private final InspectionRepository inspectionRepository;
    private final VendorSdkAdapterRegistry adapterRegistry;

    public DeviceManagementService(DeviceManagementRepository deviceRepository, InspectionRepository inspectionRepository, VendorSdkAdapterRegistry adapterRegistry) {
        this.deviceRepository = deviceRepository;
        this.inspectionRepository = inspectionRepository;
        this.adapterRegistry = adapterRegistry;
    }

    public List<Device> devices() {
        return deviceRepository.devices();
    }

    public Device createDevice(Map<String, Object> payload) {
        Instant now = Instant.now();
        Device device = toDevice(UUID.randomUUID().toString(), payload, "offline", now.toString(), now.toString());
        Device created = deviceRepository.createDevice(device);
        inspectionRepository.appendAuditLog("system", "DEVICE_CREATE", "device", created.id(), "success");
        return created;
    }

    public Device updateDevice(String id, Map<String, Object> payload) {
        Device current = deviceById(id);
        Device updated = new Device(
            id,
            value(payload, "name", current.name()),
            value(payload, "sourceType", current.sourceType()).toLowerCase(Locale.ROOT),
            value(payload, "vendor", current.vendor()),
            value(payload, "protocol", current.protocol()),
            value(payload, "endpoint", current.endpoint()),
            value(payload, "credentialRef", current.credentialRef()),
            value(payload, "location", current.location()),
            value(payload, "edgeNodeId", current.edgeNodeId()),
            value(payload, "status", current.status()),
            value(payload, "streamUrl", current.streamUrl()),
            current.createdAt(),
            Instant.now().toString()
        );
        if (!SOURCE_TYPES.contains(updated.sourceType())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Unsupported sourceType: " + updated.sourceType());
        }
        Device saved = deviceRepository.updateDevice(id, updated)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Device not found: " + id));
        inspectionRepository.appendAuditLog("system", "DEVICE_UPDATE", "device", id, "success");
        return saved;
    }

    public CommandResult deleteDevice(String id) {
        boolean deleted = deviceRepository.deleteDevice(id, Instant.now());
        if (!deleted) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Device not found: " + id);
        }
        inspectionRepository.appendAuditLog("system", "DEVICE_DELETE", "device", id, "success");
        return new CommandResult(id, "deleted", "device deleted", Instant.now(), Map.of("id", id));
    }

    public CommandResult testDevice(String id) {
        Device device = deviceById(id);
        AdapterResult result = "vehicle".equals(device.sourceType())
            ? vehicleAdapter(device.vendor()).testConnection(toIntegration(device))
            : cameraAdapter(device.vendor()).testConnection(toIntegration(device));
        String status = result.success() ? "online" : "error";
        deviceRepository.updateDeviceStatus(id, status, Instant.now());
        inspectionRepository.appendAuditLog("system", "DEVICE_TEST", "device", id, result.success() ? "success" : "failed");
        return new CommandResult(id, status, result.message(), Instant.now(), result.data());
    }

    public CommandResult syncChannels(String id) {
        Device device = deviceById(id);
        List<VideoChannel> synced = rawChannels(device);
        if (synced.isEmpty()) {
            if (!"MOCK_VENDOR".equalsIgnoreCase(device.vendor())) {
                inspectionRepository.appendAuditLog("system", "DEVICE_SYNC_CHANNELS", "device", id, "failed");
                return new CommandResult(id, "failed", device.vendor() + " channel sync unavailable: SDK not configured or no channels returned", Instant.now(), Map.of(
                    "sourceType", device.sourceType(),
                    "vendor", device.vendor(),
                    "channelCount", 0,
                    "sdkRequired", true
                ));
            }
            synced = fallbackChannels(device);
        }
        List<VideoChannel> normalized = normalizeChannels(device, synced);
        List<VideoChannel> saved = deviceRepository.replaceDeviceChannels(device.id(), normalized, Instant.now());
        deviceRepository.updateDeviceStatus(id, "online", Instant.now());
        inspectionRepository.appendAuditLog("system", "DEVICE_SYNC_CHANNELS", "device", id, "success");
        return new CommandResult(id, "synced", "device channels synced", Instant.now(), Map.of(
            "sourceType", device.sourceType(),
            "vendor", device.vendor(),
            "channelCount", saved.size()
        ));
    }

    private Device deviceById(String id) {
        return deviceRepository.deviceById(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Device not found: " + id));
    }

    private Device toDevice(String id, Map<String, Object> payload, String fallbackStatus, String createdAt, String updatedAt) {
        String name = required(payload, "name");
        String sourceType = value(payload, "sourceType", "camera").toLowerCase(Locale.ROOT);
        if (!SOURCE_TYPES.contains(sourceType)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Unsupported sourceType: " + sourceType);
        }
        return new Device(
            id,
            name,
            sourceType,
            required(payload, "vendor"),
            value(payload, "protocol", protocolFor(sourceType)),
            value(payload, "endpoint", ""),
            value(payload, "credentialRef", ""),
            value(payload, "location", ""),
            value(payload, "edgeNodeId", "EDGE-01"),
            value(payload, "status", fallbackStatus),
            value(payload, "streamUrl", value(payload, "endpoint", "")),
            createdAt,
            updatedAt
        );
    }

    private IntegrationConfig toIntegration(Device device) {
        return new IntegrationConfig(device.id(), device.name(), device.sourceType(), device.vendor(), device.protocol(), device.status(), device.endpoint(), device.credentialRef(), "", 0);
    }

    private List<VideoChannel> rawChannels(Device device) {
        if ("vehicle".equals(device.sourceType())) {
            return vehicleAdapter(device.vendor()).syncChannels(device.id());
        }
        return cameraAdapter(device.vendor()).syncChannels(toIntegration(device));
    }

    private List<VideoChannel> fallbackChannels(Device device) {
        String rawUrl = safe(device.streamUrl()).isBlank() ? safe(device.endpoint()) : device.streamUrl();
        String playUrl = "http://localhost:8088/live/" + device.id() + "-main.flv";
        return List.of(new VideoChannel(UUID.randomUUID().toString(), device.id(), device.sourceType(), device.name(), "Main stream", device.protocol(), rawUrl, playUrl, true, "online", device.edgeNodeId(), 0, 0));
    }

    private List<VideoChannel> normalizeChannels(Device device, List<VideoChannel> channels) {
        return channels.stream()
            .map(channel -> new VideoChannel(
                UUID.randomUUID().toString(),
                device.id(),
                device.sourceType(),
                device.name(),
                channel.name(),
                channel.protocol(),
                channel.streamUrl(),
                channel.playUrl(),
                channel.aiEnabled(),
                channel.status(),
                device.edgeNodeId(),
                channel.latitude(),
                channel.longitude()
            ))
            .toList();
    }

    private CameraSdkAdapter cameraAdapter(String vendor) {
        return adapterRegistry.cameraAdapterByVendor(vendor);
    }

    private VehicleVideoAdapter vehicleAdapter(String vendor) {
        return adapterRegistry.vehicleAdapterByVendor(vendor);
    }

    private String required(Map<String, Object> payload, String key) {
        String value = value(payload, key, "");
        if (value.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Missing required field: " + key);
        }
        return value;
    }

    private String value(Map<String, Object> payload, String key, String fallback) {
        Object value = payload.get(key);
        return value == null || String.valueOf(value).isBlank() ? fallback : String.valueOf(value).trim();
    }

    private String protocolFor(String sourceType) {
        return switch (sourceType) {
            case "drone" -> "DJI_SDK";
            case "vehicle" -> "JT1078";
            default -> "RTSP";
        };
    }

    private String safe(String value) {
        return value == null ? "" : value;
    }
}
