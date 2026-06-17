package com.aiplatform.inspection.service;

import com.aiplatform.inspection.domain.*;
import com.aiplatform.inspection.repository.InspectionRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class InspectionQueryService {
    private final InspectionRepository repository;
    private final DeviceManagementService deviceManagementService;

    public InspectionQueryService(InspectionRepository repository, DeviceManagementService deviceManagementService) {
        this.repository = repository;
        this.deviceManagementService = deviceManagementService;
    }

    public PlatformOverview overview() {
        DroneDock dock = repository.droneDocks().stream()
            .findFirst()
            .orElse(new DroneDock("dock-unknown", "未配置机场", "offline", 0, "未配置"));
        return new PlatformOverview(repository.kpis(), alarms(), plans(), dock);
    }

    public List<Device> devices() {
        return deviceManagementService.devices();
    }

    public List<InspectionPlan> plans() {
        return repository.plans();
    }

    public List<AlarmEvent> alarms() {
        return repository.alarms();
    }

    public List<Dataset> datasets() {
        return repository.datasets();
    }

    public List<TrainingJob> trainingJobs() {
        return repository.trainingJobs();
    }

    public List<ModelVersion> models() {
        return repository.models();
    }

    public List<FlightTask> flightTasks() {
        return repository.flightTasks();
    }

    public List<InspectionTask> inspectionTasks() {
        return repository.inspectionTasks();
    }

    public List<MediaAsset> mediaAssets() {
        return repository.mediaAssets();
    }

    public List<InspectionReport> inspectionReports() {
        return repository.inspectionReports();
    }

    public List<AlgorithmParameter> algorithmParameters() {
        return repository.algorithmParameters();
    }

    public List<MapEvent> mapEvents() {
        return repository.mapEvents();
    }

    public List<UserAccount> users() {
        return repository.users();
    }

    public List<RoleSummary> roles() {
        return repository.roles();
    }

    public List<AuditLogEntry> auditLogs() {
        return repository.auditLogs();
    }

    public List<IntegrationConfig> integrations() {
        return repository.integrations();
    }

    public List<VideoChannel> videoChannels() {
        return repository.videoChannels();
    }

    public List<DroneDock> droneDocks() {
        return repository.droneDocks();
    }

    public List<DroneAsset> drones() {
        return repository.drones();
    }

    public List<FlightRouteSummary> flightRoutes() {
        return repository.flightRoutes();
    }

    public List<VehicleAsset> vehicles() {
        return repository.vehicles();
    }

    public List<VehicleTrackPoint> vehicleTracks(String vehicleId) {
        return repository.vehicleTracks(vehicleId);
    }

    public List<VideoChannel> vehicleChannels(String vehicleId) {
        return repository.videoChannels().stream()
            .filter(channel -> "vehicle".equals(channel.sourceType()))
            .filter(channel -> channel.sourceId().equals(vehicleId))
            .toList();
    }

    public List<AiTask> aiTasks() {
        return repository.aiTasks();
    }

    public List<EdgeEvent> edgeEvents() {
        return repository.edgeEvents();
    }
}
