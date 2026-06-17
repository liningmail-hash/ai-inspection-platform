package com.aiplatform.inspection.repository;

import com.aiplatform.inspection.domain.*;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface InspectionRepository {
    List<Kpi> kpis();
    List<Device> devices();
    List<InspectionPlan> plans();
    List<AlarmEvent> alarms();
    List<Dataset> datasets();
    List<TrainingJob> trainingJobs();
    List<ModelVersion> models();
    List<DroneDock> droneDocks();
    List<FlightTask> flightTasks();
    List<InspectionTask> inspectionTasks();
    List<MediaAsset> mediaAssets();
    List<InspectionReport> inspectionReports();
    List<AlgorithmParameter> algorithmParameters();
    List<MapEvent> mapEvents();
    List<UserAccount> users();
    List<RoleSummary> roles();
    List<AuditLogEntry> auditLogs();
    List<IntegrationConfig> integrations();
    List<VideoChannel> videoChannels();
    Optional<VideoChannel> videoChannelById(String id);
    List<DroneAsset> drones();
    List<FlightRouteSummary> flightRoutes();
    List<VehicleAsset> vehicles();
    List<VehicleTrackPoint> vehicleTracks(String vehicleId);
    List<AiTask> aiTasks();
    List<EdgeEvent> edgeEvents();
    Optional<UserAccount> userByUsername(String username);
    AuditLogEntry appendAuditLog(String actor, String action, String targetType, String targetId, String result);
    InspectionTask createInspectionTask(String name, String type, String priority, Instant plannedAt);
    VideoSession createVideoSession(String channelId, String playUrl, String protocol, Instant startedAt, Instant expiresAt);
    AiTask createAiTask(String sourceType, String channelId, String algorithmCode, String modelVersion, String status, double confidence, String evidenceUrl, Instant createdAt);
    EdgeEvent appendEdgeEvent(String eventType, String sourceType, String sourceId, String severity, String title, double latitude, double longitude, String evidenceUrl, Instant detectedAt);
    Optional<AlarmEvent> updateAlarmStatus(String id, String status, Instant handledAt);
    Optional<ModelVersion> updateModelStatus(String id, String status, Map<String, Object> strategy);
    Optional<FlightTask> updateFlightTaskStatus(String id, String status, Instant actionAt);
}
