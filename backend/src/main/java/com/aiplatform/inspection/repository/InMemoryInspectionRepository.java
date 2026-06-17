package com.aiplatform.inspection.repository;

import com.aiplatform.inspection.domain.*;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Repository
@Profile("!postgres")
public class InMemoryInspectionRepository implements InspectionRepository {
    private final Map<String, AlarmEvent> alarms = new ConcurrentHashMap<>();
    private final Map<String, ModelVersion> models = new ConcurrentHashMap<>();
    private final Map<String, InspectionTask> inspectionTasks = new ConcurrentHashMap<>();
    private final Map<String, FlightTask> flightTasks = new ConcurrentHashMap<>();
    private final Map<String, AiTask> aiTasks = new ConcurrentHashMap<>();
    private final Map<String, EdgeEvent> edgeEvents = new ConcurrentHashMap<>();
    private final Map<String, UserAccount> users = new ConcurrentHashMap<>();
    private final Map<String, RoleSummary> roles = new ConcurrentHashMap<>();
    private final List<AuditLogEntry> auditLogs = new ArrayList<>();

    public InMemoryInspectionRepository() {
        seedAlarms().forEach(alarm -> alarms.put(alarm.id(), alarm));
        seedModels().forEach(model -> models.put(model.id(), model));
        seedInspectionTasks().forEach(task -> inspectionTasks.put(task.id(), task));
        seedFlightTasks().forEach(task -> flightTasks.put(task.id(), task));
        seedAiTasks().forEach(task -> aiTasks.put(task.id(), task));
        seedEdgeEvents().forEach(event -> edgeEvents.put(event.id(), event));
        seedRoles().forEach(role -> roles.put(role.id(), role));
        seedUsers().forEach(user -> users.put(user.username(), user));
        appendAuditLog("system", "BOOTSTRAP", "platform", "demo", "success");
    }

    @Override
    public List<Kpi> kpis() {
        return List.of(
            new Kpi("在线设备", "126/138", "+4", "success"),
            new Kpi("今日告警", "37", "-12%", "warning"),
            new Kpi("AI任务运行", "16", "GPU 62%", "primary"),
            new Kpi("无人机任务", "3", "1 执行中", "success")
        );
    }

    @Override
    public List<Device> devices() {
        return List.of(
            new Device("dev-001", "A区危化仓-枪机01", "HIKVISION", "GB28181", "online", "rtsp://example/live/a1"),
            new Device("dev-002", "B区围界-球机03", "DAHUA", "ONVIF", "online", "rtsp://example/live/b3"),
            new Device("dev-003", "C区装卸口-枪机02", "GENERIC", "RTSP", "online", "rtsp://example/live/c2"),
            new Device("dev-004", "D区停车场-枪机06", "JT1078", "JT/T1078", "offline", null)
        );
    }

    @Override
    public List<InspectionPlan> plans() {
        return List.of(
            new InspectionPlan("plan-001", "危化仓烟火巡检", 12, "烟火识别", "每5分钟", "running"),
            new InspectionPlan("plan-002", "围界人员闯入巡检", 18, "人员闯入", "全天", "running"),
            new InspectionPlan("plan-003", "装卸区安全帽巡检", 9, "安全帽识别", "工作日", "paused")
        );
    }

    @Override
    public List<AlarmEvent> alarms() {
        return alarms.values().stream()
            .sorted((left, right) -> right.detectedAt().compareTo(left.detectedAt()))
            .toList();
    }

    private List<AlarmEvent> seedAlarms() {
        return List.of(
            new AlarmEvent("ALM-20260615-001", "high", "烟火识别", "A区危化仓-枪机01", "new", Instant.now(), "/evidence/alarm-001.jpg"),
            new AlarmEvent("ALM-20260615-002", "medium", "人员闯入", "B区围界-球机03", "processing", Instant.now(), "/evidence/alarm-002.jpg"),
            new AlarmEvent("ALM-20260615-003", "low", "安全帽识别", "C区装卸口-枪机02", "closed", Instant.now(), "/evidence/alarm-003.jpg")
        );
    }

    @Override
    public List<Dataset> datasets() {
        return List.of(
            new Dataset("dataset-001", "烟火负样本增强", "smoke_fire", 1284, "labeling"),
            new Dataset("dataset-002", "围界闯入样本", "person_intrusion", 876, "qc"),
            new Dataset("dataset-003", "安全帽样本", "helmet_detection", 2143, "ready")
        );
    }

    @Override
    public List<TrainingJob> trainingJobs() {
        return List.of(new TrainingJob("job-001", "smoke_fire_v1.5", "running", 62, Map.of("mAP50", 0.948, "recall", 0.916, "gpu", "71%")));
    }

    @Override
    public List<ModelVersion> models() {
        return models.values().stream()
            .sorted((left, right) -> left.algorithm().compareTo(right.algorithm()))
            .toList();
    }

    private List<ModelVersion> seedModels() {
        return List.of(
            new ModelVersion("model-001", "烟火识别", "v1.4.2", "production", Map.of("precision", "94.8%", "recall", "91.6%")),
            new ModelVersion("model-002", "人员闯入", "v1.2.0", "canary", Map.of("precision", "96.1%", "recall", "93.2%")),
            new ModelVersion("model-003", "安全帽识别", "v0.9.8", "candidate", Map.of("precision", "91.7%", "recall", "88.5%"))
        );
    }

    @Override
    public List<DroneDock> droneDocks() {
        return List.of(new DroneDock("dock-001", "一号机场", "ready", 87, "风速 3.1m/s / 小雨"));
    }

    @Override
    public List<FlightTask> flightTasks() {
        return flightTasks.values().stream()
            .sorted((left, right) -> left.plannedAt().compareTo(right.plannedAt()))
            .toList();
    }

    @Override
    public List<IntegrationConfig> integrations() {
        return List.of(
            new IntegrationConfig("int-camera-hik", "海康厂区视频 SDK", "camera", "HIKVISION", "HIK_SDK", "online", "https://hik-gateway.local", "secret://integrations/hikvision", "2026-06-16T09:30:00+08:00", 3),
            new IntegrationConfig("int-drone-dji", "大疆机场开放接口", "drone", "DJI_DOCK", "DJI_CLOUD_API", "online", "https://dji-dock.local", "secret://integrations/dji", "2026-06-16T09:26:00+08:00", 1),
            new IntegrationConfig("int-vehicle-jt1078", "车载 JT/T1078 网关", "vehicle", "JT1078_GATEWAY", "JT1078_SDK", "online", "https://jt1078-gateway.local", "secret://integrations/jt1078", "2026-06-16T09:24:00+08:00", 2)
        );
    }

    @Override
    public List<VideoChannel> videoChannels() {
        return List.of(
            new VideoChannel("ch-camera-a1", "dev-001", "camera", "A区危化仓-枪机01", "主码流", "HIK_SDK", "rtsp://example/live/a1", "http://localhost:8088/live/camera-a1.flv", true, "online", "EDGE-01", 31.2312, 121.4741),
            new VideoChannel("ch-camera-b3", "dev-002", "camera", "B区围界-球机03", "主码流", "DAHUA_SDK", "rtsp://example/live/b3", "http://localhost:8088/live/camera-b3.flv", true, "online", "EDGE-01", 31.2306, 121.4729),
            new VideoChannel("ch-drone-001", "drone-001", "drone", "一号机场无人机", "无人机直播", "DJI_SDK", "rtsp://example/live/drone-001", "http://localhost:8088/live/drone-001.flv", true, "online", "EDGE-01", 31.2321, 121.4752),
            new VideoChannel("ch-vehicle-front", "vehicle-001", "vehicle", "危化品转运车01", "前向摄像头", "JT1078", "jt1078://vehicle-001/front", "http://localhost:8088/live/vehicle-front.flv", true, "online", "EDGE-01", 31.2298, 121.4718),
            new VideoChannel("ch-vehicle-cargo", "vehicle-001", "vehicle", "危化品转运车01", "车厢摄像头", "JT1078", "jt1078://vehicle-001/cargo", "http://localhost:8088/live/vehicle-cargo.flv", true, "online", "EDGE-01", 31.2298, 121.4718)
        );
    }

    @Override
    public Optional<VideoChannel> videoChannelById(String id) {
        return videoChannels().stream()
            .filter(channel -> channel.id().equals(id))
            .findFirst();
    }

    @Override
    public List<DroneAsset> drones() {
        return List.of(new DroneAsset(
            "drone-001",
            "dock-001",
            "一号机场无人机",
            "DJI_DOCK",
            "ready",
            87,
            31.2321,
            121.4752,
            List.of("ALT 86m", "SPD 8.4m/s", "HDG 126deg", "LINK 98%"),
            "东区围界固定航线"
        ));
    }

    @Override
    public List<FlightRouteSummary> flightRoutes() {
        return List.of(
            new FlightRouteSummary("route-001", "dock-001", "东区围界固定航线", 18, 86, "ready"),
            new FlightRouteSummary("route-002", "dock-001", "危化仓屋顶巡检", 12, 80, "ready")
        );
    }

    @Override
    public List<VehicleAsset> vehicles() {
        return List.of(
            new VehicleAsset("vehicle-001", "沪A-D8123", "危化品转运车01", "JT1078_GATEWAY", "online", 36.5, 31.2298, 121.4718, "2026-06-16T09:28:00+08:00"),
            new VehicleAsset("vehicle-002", "沪A-F2639", "巡逻车02", "JT1078_GATEWAY", "online", 18.2, 31.2330, 121.4760, "2026-06-16T09:27:00+08:00")
        );
    }

    @Override
    public List<VehicleTrackPoint> vehicleTracks(String vehicleId) {
        return List.of(
            new VehicleTrackPoint(vehicleId, 31.2298, 121.4718, 36.5, 86, "2026-06-16T09:20:00+08:00"),
            new VehicleTrackPoint(vehicleId, 31.2304, 121.4726, 32.0, 92, "2026-06-16T09:24:00+08:00"),
            new VehicleTrackPoint(vehicleId, 31.2311, 121.4734, 28.2, 104, "2026-06-16T09:28:00+08:00")
        );
    }

    @Override
    public List<AiTask> aiTasks() {
        return aiTasks.values().stream()
            .sorted((left, right) -> right.createdAt().compareTo(left.createdAt()))
            .toList();
    }

    @Override
    public List<EdgeEvent> edgeEvents() {
        return edgeEvents.values().stream()
            .sorted((left, right) -> right.detectedAt().compareTo(left.detectedAt()))
            .toList();
    }

    @Override
    public List<InspectionTask> inspectionTasks() {
        return inspectionTasks.values().stream()
            .sorted((left, right) -> left.plannedAt().compareTo(right.plannedAt()))
            .toList();
    }

    @Override
    public List<MediaAsset> mediaAssets() {
        return List.of(
            new MediaAsset("media-001", "A区危化仓烟火证据图", "image", "camera", "危化仓烟火巡检", "stored", "/evidence/alarm-001.jpg", "2026-06-16T09:18:00+08:00"),
            new MediaAsset("media-002", "东区围界无人机巡检视频", "video", "drone", "东区围界固定航线", "stored", "/media/flight-001.mp4", "2026-06-16T09:28:00+08:00"),
            new MediaAsset("media-003", "安全帽样本采集包", "dataset", "ai", "装卸区安全帽巡检", "labeling", "/datasets/helmet-batch-001", "2026-06-16T09:35:00+08:00")
        );
    }

    @Override
    public List<InspectionReport> inspectionReports() {
        return List.of(
            new InspectionReport("report-001", "一号工业园日巡检报告", "2026-06-16", "generated", "PDF", "2026-06-16T09:40:00+08:00", "/reports/daily-20260616.pdf"),
            new InspectionReport("report-002", "无人机围界巡检报告", "2026-W25", "draft", "HTML", "-", "/reports/flight-week-25.html"),
            new InspectionReport("report-003", "AI误报复核周报", "2026-W25", "queued", "Excel", "-", "/reports/ai-review-week-25.xlsx")
        );
    }

    @Override
    public List<AlgorithmParameter> algorithmParameters() {
        return List.of(
            new AlgorithmParameter("param-001", "smoke_fire", "烟火识别", 0.72, 86, true, "2026-06-16T09:00:00+08:00"),
            new AlgorithmParameter("param-002", "person_intrusion", "人员闯入", 0.68, 80, true, "2026-06-16T09:00:00+08:00"),
            new AlgorithmParameter("param-003", "helmet_detection", "安全帽识别", 0.74, 78, true, "2026-06-16T09:00:00+08:00")
        );
    }

    @Override
    public List<MapEvent> mapEvents() {
        return List.of(
            new MapEvent("map-001", "A区危化仓烟火识别", "烟火识别", "high", "new", 31.2312, 121.4741, "camera", Instant.now()),
            new MapEvent("map-002", "B区围界人员闯入", "人员闯入", "medium", "processing", 31.2306, 121.4729, "camera", Instant.now()),
            new MapEvent("map-003", "东区无人机巡检异常点", "无人机巡检", "medium", "new", 31.2321, 121.4752, "drone", Instant.now())
        );
    }

    @Override
    public List<UserAccount> users() {
        return users.values().stream()
            .sorted((left, right) -> left.username().compareTo(right.username()))
            .toList();
    }

    @Override
    public List<RoleSummary> roles() {
        return roles.values().stream()
            .sorted((left, right) -> left.code().compareTo(right.code()))
            .toList();
    }

    @Override
    public List<AuditLogEntry> auditLogs() {
        synchronized (auditLogs) {
            return auditLogs.stream()
                .sorted((left, right) -> right.createdAt().compareTo(left.createdAt()))
                .limit(50)
                .toList();
        }
    }

    @Override
    public Optional<UserAccount> userByUsername(String username) {
        return Optional.ofNullable(users.get(username));
    }

    @Override
    public AuditLogEntry appendAuditLog(String actor, String action, String targetType, String targetId, String result) {
        AuditLogEntry entry = new AuditLogEntry(
            UUID.randomUUID().toString(),
            actor,
            action,
            targetType,
            targetId,
            result,
            Instant.now()
        );
        synchronized (auditLogs) {
            auditLogs.add(entry);
        }
        return entry;
    }

    @Override
    public InspectionTask createInspectionTask(String name, String type, String priority, Instant plannedAt) {
        InspectionTask task = new InspectionTask(
            "task-" + UUID.randomUUID(),
            name,
            type,
            priority,
            "queued",
            "东区围界固定航线",
            "巡检值班员",
            plannedAt.toString()
        );
        inspectionTasks.put(task.id(), task);
        return task;
    }

    @Override
    public VideoSession createVideoSession(String channelId, String playUrl, String protocol, Instant startedAt, Instant expiresAt) {
        return new VideoSession(
            "session-" + UUID.randomUUID(),
            channelId,
            playUrl,
            protocol,
            "playing",
            startedAt.toString(),
            expiresAt.toString()
        );
    }

    @Override
    public AiTask createAiTask(String sourceType, String channelId, String algorithmCode, String modelVersion, String status, double confidence, String evidenceUrl, Instant createdAt) {
        AiTask task = new AiTask(
            "ai-task-" + UUID.randomUUID(),
            sourceType,
            channelId,
            algorithmCode,
            modelVersion,
            status,
            confidence,
            evidenceUrl,
            createdAt.toString()
        );
        aiTasks.put(task.id(), task);
        return task;
    }

    @Override
    public EdgeEvent appendEdgeEvent(String eventType, String sourceType, String sourceId, String severity, String title, double latitude, double longitude, String evidenceUrl, Instant detectedAt) {
        EdgeEvent event = new EdgeEvent(
            "edge-event-" + UUID.randomUUID(),
            eventType,
            sourceType,
            sourceId,
            severity,
            title,
            "new",
            latitude,
            longitude,
            evidenceUrl,
            detectedAt
        );
        edgeEvents.put(event.id(), event);
        return event;
    }

    @Override
    public Optional<AlarmEvent> updateAlarmStatus(String id, String status, Instant handledAt) {
        AlarmEvent current = alarms.get(id);
        if (current == null) {
            return Optional.empty();
        }
        AlarmEvent updated = new AlarmEvent(
            current.id(),
            current.level(),
            current.type(),
            current.device(),
            status,
            current.detectedAt(),
            current.evidenceUrl()
        );
        alarms.put(id, updated);
        return Optional.of(updated);
    }

    @Override
    public Optional<ModelVersion> updateModelStatus(String id, String status, Map<String, Object> strategy) {
        ModelVersion current = models.get(id);
        if (current == null) {
            return Optional.empty();
        }
        Map<String, Object> metrics = new LinkedHashMap<>(current.metrics());
        metrics.put("lastStrategy", strategy);
        ModelVersion updated = new ModelVersion(current.id(), current.algorithm(), current.version(), status, metrics);
        models.put(id, updated);
        return Optional.of(updated);
    }

    @Override
    public Optional<FlightTask> updateFlightTaskStatus(String id, String status, Instant actionAt) {
        FlightTask current = flightTasks.get(id);
        if (current == null) {
            return Optional.empty();
        }
        FlightTask updated = new FlightTask(current.id(), current.routeName(), status, current.plannedAt());
        flightTasks.put(id, updated);
        return Optional.of(updated);
    }

    private List<RoleSummary> seedRoles() {
        return List.of(
            new RoleSummary("role-admin", "platform_admin", "平台管理员", "平台配置、设备、模型发布和审计管理", List.of("*"), 1),
            new RoleSummary("role-operator", "inspection_operator", "巡检值班员", "查看视频、处置告警、下发巡检任务", List.of("alarm:handle", "task:dispatch", "video:view"), 1),
            new RoleSummary("role-ai", "ai_engineer", "算法工程师", "管理数据集、训练任务和模型版本", List.of("dataset:manage", "training:manage", "model:publish"), 1)
        );
    }

    private List<UserAccount> seedUsers() {
        return List.of(
            new UserAccount("user-admin", "admin", "系统管理员", "示范园区运营中心", "一号工业园", "active", List.of("平台管理员"), List.of("*")),
            new UserAccount("user-operator", "operator", "巡检值班员", "示范园区运营中心", "一号工业园", "active", List.of("巡检值班员"), List.of("alarm:handle", "task:dispatch", "video:view")),
            new UserAccount("user-ai", "ai.engineer", "算法工程师", "示范园区运营中心", "一号工业园", "active", List.of("算法工程师"), List.of("dataset:manage", "training:manage", "model:publish"))
        );
    }

    private List<InspectionTask> seedInspectionTasks() {
        return List.of(
            new InspectionTask("task-001", "危化仓烟火即时复核", "immediate", "high", "running", "A区危化仓点位", "巡检值班员", "2026-06-16T09:20:00+08:00"),
            new InspectionTask("task-002", "东区围界定时巡检", "scheduled", "medium", "scheduled", "东区围界固定航线", "无人机机场", "2026-06-16T10:00:00+08:00"),
            new InspectionTask("task-003", "安全帽识别循环抽检", "loop", "medium", "queued", "C区装卸口", "边缘节点 EDGE-01", "2026-06-16T10:30:00+08:00")
        );
    }

    private List<FlightTask> seedFlightTasks() {
        return List.of(
            new FlightTask("flight-001", "东区围界固定航线", "running", "2026-06-15T21:00:00+08:00"),
            new FlightTask("flight-002", "危化仓屋顶巡检", "scheduled", "2026-06-16T09:30:00+08:00")
        );
    }

    private List<AiTask> seedAiTasks() {
        return List.of(
            new AiTask("ai-task-001", "camera", "ch-camera-a1", "smoke_fire", "v1.4.2", "running", 0.91, "/evidence/alarm-001.jpg", "2026-06-16T09:18:00+08:00"),
            new AiTask("ai-task-002", "drone", "ch-drone-001", "person_intrusion", "v1.2.0", "running", 0.87, "/media/flight-001.mp4", "2026-06-16T09:22:00+08:00"),
            new AiTask("ai-task-003", "vehicle", "ch-vehicle-front", "vehicle_parking", "v0.8.0", "running", 0.82, "/evidence/vehicle-001.jpg", "2026-06-16T09:25:00+08:00")
        );
    }

    private List<EdgeEvent> seedEdgeEvents() {
        return List.of(
            new EdgeEvent("edge-event-001", "ai_alarm", "camera", "ch-camera-a1", "high", "A区危化仓烟火识别", "new", 31.2312, 121.4741, "/evidence/alarm-001.jpg", Instant.now()),
            new EdgeEvent("edge-event-002", "ai_alarm", "vehicle", "ch-vehicle-front", "medium", "车载通道异常停车", "new", 31.2298, 121.4718, "/evidence/vehicle-001.jpg", Instant.now())
        );
    }
}
