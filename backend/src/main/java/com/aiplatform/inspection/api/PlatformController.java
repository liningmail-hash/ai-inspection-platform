package com.aiplatform.inspection.api;

import com.aiplatform.inspection.domain.*;
import com.aiplatform.inspection.service.AuthService;
import com.aiplatform.inspection.service.DeviceManagementService;
import com.aiplatform.inspection.service.InspectionCommandService;
import com.aiplatform.inspection.service.InspectionQueryService;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
@CrossOrigin
public class PlatformController {
    private final AuthService authService;
    private final InspectionQueryService queryService;
    private final InspectionCommandService commandService;
    private final DeviceManagementService deviceManagementService;

    public PlatformController(AuthService authService, InspectionQueryService queryService, InspectionCommandService commandService, DeviceManagementService deviceManagementService) {
        this.authService = authService;
        this.queryService = queryService;
        this.commandService = commandService;
        this.deviceManagementService = deviceManagementService;
    }

    @GetMapping("/health")
    public Map<String, Object> health() {
        return Map.of("status", "UP", "service", "inspection-backend", "time", Instant.now().toString());
    }

    @PostMapping("/auth/login")
    public AuthSession login(@RequestBody(required = false) Map<String, String> body) {
        Map<String, String> payload = body == null ? Map.of() : body;
        return authService.login(payload.get("username"), payload.get("password"));
    }

    @GetMapping("/auth/me")
    public UserAccount me(@RequestHeader(value = "Authorization", required = false) String authorization) {
        return authService.me(authorization);
    }

    @GetMapping("/overview")
    public PlatformOverview overview() {
        return queryService.overview();
    }

    @GetMapping("/devices")
    public List<Device> devices() {
        return queryService.devices();
    }

    @PostMapping("/devices")
    public Device createDevice(@RequestBody(required = false) Map<String, Object> payload) {
        return deviceManagementService.createDevice(payload == null ? Map.of() : payload);
    }

    @PutMapping("/devices/{id}")
    public Device updateDevice(@PathVariable String id, @RequestBody(required = false) Map<String, Object> payload) {
        return deviceManagementService.updateDevice(id, payload == null ? Map.of() : payload);
    }

    @DeleteMapping("/devices/{id}")
    public CommandResult deleteDevice(@PathVariable String id) {
        return deviceManagementService.deleteDevice(id);
    }

    @PostMapping("/devices/{id}/test")
    public CommandResult testDevice(@PathVariable String id) {
        return deviceManagementService.testDevice(id);
    }

    @PostMapping("/devices/{id}/sync-channels")
    public CommandResult syncDeviceChannels(@PathVariable String id) {
        return deviceManagementService.syncChannels(id);
    }

    // ========== v2 Device Management (Hierarchical) ==========

    @GetMapping("/devices/tree")
    public List<DeviceNode> deviceTree() {
        return deviceManagementService.devicesTree();
    }

    @GetMapping("/devices/nvrs")
    public List<NvrDevice> nvrs() {
        return deviceManagementService.nvrDevices();
    }

    @PostMapping("/devices/nvrs")
    public NvrDevice createNvr(@RequestBody(required = false) Map<String, Object> payload) {
        return deviceManagementService.createNvr(payload == null ? Map.of() : payload);
    }

    @GetMapping("/devices/nvrs/{id}")
    public NvrDevice nvrById(@PathVariable String id) {
        return deviceManagementService.nvrDeviceById(id);
    }

    @PutMapping("/devices/nvrs/{id}")
    public NvrDevice updateNvr(@PathVariable String id, @RequestBody(required = false) Map<String, Object> payload) {
        return deviceManagementService.updateNvr(id, payload == null ? Map.of() : payload);
    }

    @DeleteMapping("/devices/nvrs/{id}")
    public CommandResult deleteNvr(@PathVariable String id) {
        return deviceManagementService.deleteNvr(id);
    }

    @PostMapping("/devices/nvrs/{id}/sync")
    public CommandResult syncNvrChannelsV2(@PathVariable String id) {
        return deviceManagementService.syncNvrChannels(id);
    }

    @GetMapping("/devices/drones")
    public List<DroneDockNode> droneDocksV2() {
        return deviceManagementService.droneDocks();
    }

    @PostMapping("/devices/drones")
    public DroneDockNode createDroneDock(@RequestBody(required = false) Map<String, Object> payload) {
        return deviceManagementService.createDroneDock(payload == null ? Map.of() : payload);
    }

    @GetMapping("/devices/vehicles")
    public List<VehicleNode> vehiclesV2() {
        return deviceManagementService.vehicles();
    }

    @PostMapping("/devices/vehicles")
    public VehicleNode createVehicle(@RequestBody(required = false) Map<String, Object> payload) {
        return deviceManagementService.createVehicle(payload == null ? Map.of() : payload);
    }

    // Fix missing GET /api/devices/{id} for legacy compatibility
    @GetMapping("/devices/{id}")
    public Device getDeviceById(@PathVariable String id) {
        return deviceManagementService.devices().stream()
            .filter(d -> d.id().equals(id))
            .findFirst()
            .orElseThrow(() -> new org.springframework.web.server.ResponseStatusException(
                org.springframework.http.HttpStatus.NOT_FOUND, "Device not found: " + id));
    }

    @GetMapping("/inspection-plans")
    public List<InspectionPlan> plans() {
        return queryService.plans();
    }

    @GetMapping("/alarms")
    public List<AlarmEvent> alarms() {
        return queryService.alarms();
    }

    @PatchMapping("/alarms/{id}/status")
    public CommandResult updateAlarmStatus(@PathVariable String id, @RequestBody(required = false) Map<String, String> body) {
        return commandService.updateAlarmStatus(id, body == null ? null : body.get("status"));
    }

    @GetMapping("/datasets")
    public List<Dataset> datasets() {
        return queryService.datasets();
    }

    @GetMapping("/training-jobs")
    public List<TrainingJob> trainingJobs() {
        return queryService.trainingJobs();
    }

    @GetMapping("/models")
    public List<ModelVersion> models() {
        return queryService.models();
    }

    @PostMapping("/models/{id}/publish")
    public CommandResult publishModel(@PathVariable String id, @RequestBody(required = false) Map<String, Object> strategy) {
        return commandService.publishModel(id, strategy == null ? Map.of() : strategy);
    }

    @GetMapping("/flight-tasks")
    public List<FlightTask> flightTasks() {
        return queryService.flightTasks();
    }

    @GetMapping("/inspection-tasks")
    public List<InspectionTask> inspectionTasks() {
        return queryService.inspectionTasks();
    }

    @PostMapping("/inspection-tasks")
    public CommandResult createInspectionTask(@RequestBody(required = false) Map<String, Object> payload) {
        return commandService.createInspectionTask(payload == null ? Map.of() : payload);
    }

    @GetMapping("/media-assets")
    public List<MediaAsset> mediaAssets() {
        return queryService.mediaAssets();
    }

    @GetMapping("/inspection-reports")
    public List<InspectionReport> inspectionReports() {
        return queryService.inspectionReports();
    }

    @GetMapping("/algorithm-parameters")
    public List<AlgorithmParameter> algorithmParameters() {
        return queryService.algorithmParameters();
    }

    @GetMapping("/map-events")
    public List<MapEvent> mapEvents() {
        return queryService.mapEvents();
    }

    @GetMapping("/system/users")
    public List<UserAccount> users() {
        return queryService.users();
    }

    @GetMapping("/system/roles")
    public List<RoleSummary> roles() {
        return queryService.roles();
    }

    @GetMapping("/system/audit-logs")
    public List<AuditLogEntry> auditLogs() {
        return queryService.auditLogs();
    }

    @GetMapping("/integrations")
    public List<IntegrationConfig> integrations() {
        return queryService.integrations();
    }

    @PostMapping("/integrations/{id}/test")
    public CommandResult testIntegration(@PathVariable String id) {
        return commandService.testIntegration(id);
    }

    @PostMapping("/integrations/{id}/sync")
    public CommandResult syncIntegration(@PathVariable String id) {
        return commandService.syncIntegration(id);
    }

    @GetMapping("/video/channels")
    public List<VideoChannel> videoChannels() {
        return queryService.videoChannels();
    }

    @PostMapping("/video/sessions")
    public VideoSession createVideoSession(@RequestBody(required = false) Map<String, Object> payload) {
        return commandService.createVideoSession(payload == null ? Map.of() : payload);
    }

    @PostMapping("/video/channels/{id}/snapshot")
    public CommandResult snapshot(@PathVariable String id) {
        return commandService.snapshot(id);
    }

    @PostMapping("/video/channels/{id}/record")
    public CommandResult record(@PathVariable String id, @RequestBody(required = false) Map<String, Object> payload) {
        return commandService.record(id, payload == null ? Map.of() : payload);
    }

    @GetMapping("/ai/tasks")
    public List<AiTask> aiTasks() {
        return queryService.aiTasks();
    }

    @PostMapping("/ai/tasks")
    public CommandResult createAiTask(@RequestBody(required = false) Map<String, Object> payload) {
        return commandService.createAiTask(payload == null ? Map.of() : payload);
    }

    @PostMapping("/edge/heartbeat")
    public CommandResult edgeHeartbeat(@RequestBody(required = false) Map<String, Object> payload) {
        return commandService.heartbeat(payload == null ? Map.of() : payload);
    }

    @PostMapping("/edge/events")
    public CommandResult edgeEvent(@RequestBody(required = false) Map<String, Object> payload) {
        return commandService.appendEdgeEvent(payload == null ? Map.of() : payload);
    }

    @GetMapping("/edge/events")
    public List<EdgeEvent> edgeEvents() {
        return queryService.edgeEvents();
    }

    @GetMapping("/drones")
    public List<DroneAsset> drones() {
        return queryService.drones();
    }

    @GetMapping("/drone-docks")
    public List<DroneDock> droneDocks() {
        return queryService.droneDocks();
    }

    @GetMapping("/flight-routes")
    public List<FlightRouteSummary> flightRoutes() {
        return queryService.flightRoutes();
    }

    @PostMapping("/flight-tasks/{id}/start")
    public CommandResult startFlightTask(@PathVariable String id) {
        return commandService.updateFlightTaskStatus(id, "running");
    }

    @PostMapping("/flight-tasks/{id}/stop")
    public CommandResult stopFlightTask(@PathVariable String id) {
        return commandService.updateFlightTaskStatus(id, "stopped");
    }

    @GetMapping("/vehicles")
    public List<VehicleAsset> vehicles() {
        return queryService.vehicles();
    }

    @GetMapping("/vehicles/{id}/tracks")
    public List<VehicleTrackPoint> vehicleTracks(@PathVariable String id) {
        return queryService.vehicleTracks(id);
    }

    @GetMapping("/vehicles/{id}/channels")
    public List<VideoChannel> vehicleChannels(@PathVariable String id) {
        return queryService.vehicleChannels(id);
    }
}
