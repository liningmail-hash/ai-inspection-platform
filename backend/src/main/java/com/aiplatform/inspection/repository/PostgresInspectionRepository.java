package com.aiplatform.inspection.repository;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.aiplatform.inspection.domain.*;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Repository
@Profile("postgres")
public class PostgresInspectionRepository implements InspectionRepository {
    private final JdbcTemplate jdbcTemplate;
    private final ObjectMapper objectMapper;

    public PostgresInspectionRepository(JdbcTemplate jdbcTemplate, ObjectMapper objectMapper) {
        this.jdbcTemplate = jdbcTemplate;
        this.objectMapper = objectMapper;
    }

    @Override
    public List<Kpi> kpis() {
        int totalDevices = count("select count(*) from devices");
        int onlineDevices = count("select count(*) from devices where status = 'online'");
        int todayAlarms = count("select count(*) from alarm_events where detected_at >= now() - interval '24 hours'");
        int runningPlans = count("select count(*) from inspection_plans where status = 'running'");
        int runningFlights = count("select count(*) from flight_tasks where status = 'running'");
        return List.of(
            new Kpi("在线设备", onlineDevices + "/" + totalDevices, "+0", "success"),
            new Kpi("今日告警", String.valueOf(todayAlarms), "24h", todayAlarms > 0 ? "warning" : "success"),
            new Kpi("AI任务运行", String.valueOf(runningPlans), "数据库", "primary"),
            new Kpi("无人机任务", String.valueOf(runningFlights), runningFlights > 0 ? "执行中" : "待命", "success")
        );
    }

    @Override
    public List<Device> devices() {
        return jdbcTemplate.query("""
            select id::text, name, vendor, protocol, status, stream_url
            from devices
            order by created_at desc, name asc
            """, (rs, rowNum) -> new Device(
            rs.getString("id"),
            rs.getString("name"),
            rs.getString("vendor"),
            rs.getString("protocol"),
            rs.getString("status"),
            rs.getString("stream_url")
        ));
    }

    @Override
    public List<InspectionPlan> plans() {
        return jdbcTemplate.query("""
            select id::text,
                   name,
                   coalesce((rules ->> 'pointCount')::int, 0) as point_count,
                   coalesce(rules ->> 'algorithm', '未配置算法') as algorithm,
                   schedule_cron,
                   status
            from inspection_plans
            order by created_at desc, name asc
            """, (rs, rowNum) -> new InspectionPlan(
            rs.getString("id"),
            rs.getString("name"),
            rs.getInt("point_count"),
            rs.getString("algorithm"),
            rs.getString("schedule_cron"),
            rs.getString("status")
        ));
    }

    @Override
    public List<AlarmEvent> alarms() {
        return jdbcTemplate.query("""
            select a.id::text,
                   a.severity,
                   a.title,
                   coalesce(d.name, '未知设备') as device_name,
                   a.status,
                   a.detected_at,
                   a.evidence_url
            from alarm_events a
            left join devices d on d.id = a.device_id
            order by a.detected_at desc
            limit 50
            """, (rs, rowNum) -> new AlarmEvent(
            rs.getString("id"),
            rs.getString("severity"),
            rs.getString("title"),
            rs.getString("device_name"),
            rs.getString("status"),
            toInstant(rs.getTimestamp("detected_at")),
            rs.getString("evidence_url")
        ));
    }

    @Override
    public List<Dataset> datasets() {
        return jdbcTemplate.query("""
            select id::text, name, algorithm_code, sample_count, status
            from datasets
            order by name asc
            """, (rs, rowNum) -> new Dataset(
            rs.getString("id"),
            rs.getString("name"),
            rs.getString("algorithm_code"),
            rs.getInt("sample_count"),
            rs.getString("status")
        ));
    }

    @Override
    public List<TrainingJob> trainingJobs() {
        return jdbcTemplate.query("""
            select id::text, name, status, progress, metrics::text
            from training_jobs
            order by created_at desc
            """, (rs, rowNum) -> new TrainingJob(
            rs.getString("id"),
            rs.getString("name"),
            rs.getString("status"),
            rs.getInt("progress"),
            parseJsonMap(rs.getString("metrics"))
        ));
    }

    @Override
    public List<ModelVersion> models() {
        return jdbcTemplate.query("""
            select id::text, algorithm_code, version, status, metrics::text
            from model_versions
            order by created_at desc
            """, (rs, rowNum) -> new ModelVersion(
            rs.getString("id"),
            rs.getString("algorithm_code"),
            rs.getString("version"),
            rs.getString("status"),
            parseJsonMap(rs.getString("metrics"))
        ));
    }

    @Override
    public List<DroneDock> droneDocks() {
        return jdbcTemplate.query("""
            select id::text, name, status, battery_percent, coalesce(weather ->> 'summary', weather::text) as weather_summary
            from drone_docks
            order by last_seen_at desc nulls last, name asc
            """, (rs, rowNum) -> new DroneDock(
            rs.getString("id"),
            rs.getString("name"),
            rs.getString("status"),
            rs.getInt("battery_percent"),
            rs.getString("weather_summary")
        ));
    }

    @Override
    public List<FlightTask> flightTasks() {
        return jdbcTemplate.query("""
            select t.id::text,
                   coalesce(r.name, t.name) as route_name,
                   t.status,
                   t.planned_at::text as planned_at
            from flight_tasks t
            left join flight_routes r on r.id = t.route_id
            order by t.planned_at desc nulls last
            """, (rs, rowNum) -> new FlightTask(
            rs.getString("id"),
            rs.getString("route_name"),
            rs.getString("status"),
            rs.getString("planned_at")
        ));
    }

    @Override
    public List<InspectionTask> inspectionTasks() {
        return jdbcTemplate.query("""
            select id::text, name, type, priority, status, route_name, assignee, planned_at::text as planned_at
            from inspection_tasks
            order by planned_at asc nulls last, created_at desc
            """, (rs, rowNum) -> new InspectionTask(
            rs.getString("id"),
            rs.getString("name"),
            rs.getString("type"),
            rs.getString("priority"),
            rs.getString("status"),
            rs.getString("route_name"),
            rs.getString("assignee"),
            rs.getString("planned_at")
        ));
    }

    @Override
    public List<MediaAsset> mediaAssets() {
        return jdbcTemplate.query("""
            select id::text, name, asset_type, source, related_task, status, url, captured_at::text as captured_at
            from media_assets
            order by captured_at desc nulls last
            limit 50
            """, (rs, rowNum) -> new MediaAsset(
            rs.getString("id"),
            rs.getString("name"),
            rs.getString("asset_type"),
            rs.getString("source"),
            rs.getString("related_task"),
            rs.getString("status"),
            rs.getString("url"),
            rs.getString("captured_at")
        ));
    }

    @Override
    public List<InspectionReport> inspectionReports() {
        return jdbcTemplate.query("""
            select id::text, title, period, status, format, generated_at::text as generated_at, download_url
            from inspection_reports
            order by generated_at desc nulls last, created_at desc
            """, (rs, rowNum) -> new InspectionReport(
            rs.getString("id"),
            rs.getString("title"),
            rs.getString("period"),
            rs.getString("status"),
            rs.getString("format"),
            rs.getString("generated_at"),
            rs.getString("download_url")
        ));
    }

    @Override
    public List<AlgorithmParameter> algorithmParameters() {
        return jdbcTemplate.query("""
            select p.id::text,
                   p.algorithm_code,
                   coalesce(a.name, p.algorithm_code) as algorithm_name,
                   p.threshold,
                   p.sensitivity,
                   p.enabled,
                   p.updated_at::text as updated_at
            from algorithm_parameters p
            left join ai_algorithms a on a.code = p.algorithm_code
            order by p.algorithm_code asc
            """, (rs, rowNum) -> new AlgorithmParameter(
            rs.getString("id"),
            rs.getString("algorithm_code"),
            rs.getString("algorithm_name"),
            rs.getDouble("threshold"),
            rs.getInt("sensitivity"),
            rs.getBoolean("enabled"),
            rs.getString("updated_at")
        ));
    }

    @Override
    public List<MapEvent> mapEvents() {
        return jdbcTemplate.query("""
            select id::text, title, event_type, severity, status, latitude, longitude, source, detected_at
            from map_events
            order by detected_at desc
            limit 100
            """, (rs, rowNum) -> new MapEvent(
            rs.getString("id"),
            rs.getString("title"),
            rs.getString("event_type"),
            rs.getString("severity"),
            rs.getString("status"),
            rs.getDouble("latitude"),
            rs.getDouble("longitude"),
            rs.getString("source"),
            toInstant(rs.getTimestamp("detected_at"))
        ));
    }

    @Override
    public List<UserAccount> users() {
        return jdbcTemplate.query("""
            select u.id::text,
                   u.username,
                   u.display_name,
                   coalesce(o.name, '') as organization_name,
                   coalesce(s.name, '') as site_name,
                   u.status,
                   coalesce(string_agg(distinct r.name, ','), '') as role_names,
                   coalesce(string_agg(distinct rp.permission_code, ','), '') as permissions
            from user_accounts u
            left join organizations o on o.id = u.organization_id
            left join sites s on s.id = u.site_id
            left join user_roles ur on ur.user_id = u.id
            left join roles r on r.id = ur.role_id
            left join role_permissions rp on rp.role_id = r.id
            group by u.id, o.name, s.name
            order by u.username asc
            """, (rs, rowNum) -> new UserAccount(
            rs.getString("id"),
            rs.getString("username"),
            rs.getString("display_name"),
            rs.getString("organization_name"),
            rs.getString("site_name"),
            rs.getString("status"),
            splitCsv(rs.getString("role_names")),
            splitCsv(rs.getString("permissions"))
        ));
    }

    @Override
    public List<RoleSummary> roles() {
        return jdbcTemplate.query("""
            select r.id::text,
                   r.code,
                   r.name,
                   r.description,
                   coalesce(string_agg(distinct rp.permission_code, ','), '') as permissions,
                   count(distinct ur.user_id) as user_count
            from roles r
            left join role_permissions rp on rp.role_id = r.id
            left join user_roles ur on ur.role_id = r.id
            group by r.id
            order by r.code asc
            """, (rs, rowNum) -> new RoleSummary(
            rs.getString("id"),
            rs.getString("code"),
            rs.getString("name"),
            rs.getString("description"),
            splitCsv(rs.getString("permissions")),
            rs.getInt("user_count")
        ));
    }

    @Override
    public List<AuditLogEntry> auditLogs() {
        return jdbcTemplate.query("""
            select id::text, actor, action, target_type, target_id, result, created_at
            from audit_logs
            order by created_at desc
            limit 50
            """, (rs, rowNum) -> new AuditLogEntry(
            rs.getString("id"),
            rs.getString("actor"),
            rs.getString("action"),
            rs.getString("target_type"),
            rs.getString("target_id"),
            rs.getString("result"),
            toInstant(rs.getTimestamp("created_at"))
        ));
    }

    @Override
    public List<IntegrationConfig> integrations() {
        return jdbcTemplate.query("""
            select id::text, name, source_type, vendor, sdk_type, status, endpoint, credential_ref,
                   last_sync_at::text as last_sync_at, channel_count
            from integration_configs
            order by source_type asc, vendor asc
            """, (rs, rowNum) -> new IntegrationConfig(
            rs.getString("id"),
            rs.getString("name"),
            rs.getString("source_type"),
            rs.getString("vendor"),
            rs.getString("sdk_type"),
            rs.getString("status"),
            rs.getString("endpoint"),
            rs.getString("credential_ref"),
            rs.getString("last_sync_at"),
            rs.getInt("channel_count")
        ));
    }

    @Override
    public List<VideoChannel> videoChannels() {
        return jdbcTemplate.query("""
            select case vc.id::text
                     when '21000000-0000-0000-0000-000000000001' then 'ch-camera-a1'
                     when 'a4000000-0000-0000-0000-000000000001' then 'ch-drone-001'
                     when 'a4000000-0000-0000-0000-000000000002' then 'ch-vehicle-front'
                     else vc.id::text
                   end as id,
                   coalesce(vc.source_id, vc.device_id::text) as source_id,
                   vc.source_type,
                   coalesce(d.name, vc.source_name, vc.name) as source_name,
                   vc.name,
                   coalesce(vc.protocol, d.protocol, 'RTSP') as protocol,
                   coalesce(vc.stream_url, d.stream_url) as stream_url,
                   vc.play_url,
                   vc.ai_enabled,
                   vc.status,
                   vc.edge_node,
                   vc.latitude,
                   vc.longitude
            from video_channels vc
            left join devices d on d.id = vc.device_id
            order by vc.source_type asc, vc.name asc
            """, (rs, rowNum) -> toVideoChannel(rs));
    }

    @Override
    public Optional<VideoChannel> videoChannelById(String id) {
        List<VideoChannel> results = jdbcTemplate.query("""
            select case vc.id::text
                     when '21000000-0000-0000-0000-000000000001' then 'ch-camera-a1'
                     when 'a4000000-0000-0000-0000-000000000001' then 'ch-drone-001'
                     when 'a4000000-0000-0000-0000-000000000002' then 'ch-vehicle-front'
                     else vc.id::text
                   end as id,
                   coalesce(vc.source_id, vc.device_id::text) as source_id,
                   vc.source_type,
                   coalesce(d.name, vc.source_name, vc.name) as source_name,
                   vc.name,
                   coalesce(vc.protocol, d.protocol, 'RTSP') as protocol,
                   coalesce(vc.stream_url, d.stream_url) as stream_url,
                   vc.play_url,
                   vc.ai_enabled,
                   vc.status,
                   vc.edge_node,
                   vc.latitude,
                   vc.longitude
            from video_channels vc
            left join devices d on d.id = vc.device_id
            where vc.id::text = ?
            """, (rs, rowNum) -> toVideoChannel(rs), toDbChannelId(id));
        return results.stream().findFirst();
    }

    @Override
    public List<DroneAsset> drones() {
        return jdbcTemplate.query("""
            select id::text, dock_id::text, name, vendor, status, battery_percent, latitude, longitude,
                   telemetry::text, active_task
            from drones
            order by name asc
            """, (rs, rowNum) -> new DroneAsset(
            rs.getString("id"),
            rs.getString("dock_id"),
            rs.getString("name"),
            rs.getString("vendor"),
            rs.getString("status"),
            rs.getInt("battery_percent"),
            rs.getDouble("latitude"),
            rs.getDouble("longitude"),
            splitCsv(rs.getString("telemetry")),
            rs.getString("active_task")
        ));
    }

    @Override
    public List<FlightRouteSummary> flightRoutes() {
        return jdbcTemplate.query("""
            select id::text, dock_id::text, name, waypoint_count, altitude_meter, status
            from flight_routes
            order by name asc
            """, (rs, rowNum) -> new FlightRouteSummary(
            rs.getString("id"),
            rs.getString("dock_id"),
            rs.getString("name"),
            rs.getInt("waypoint_count"),
            rs.getInt("altitude_meter"),
            rs.getString("status")
        ));
    }

    @Override
    public List<VehicleAsset> vehicles() {
        return jdbcTemplate.query("""
            select id::text, plate_no, name, vendor, status, speed_kph, latitude, longitude, last_seen_at::text as last_seen_at
            from vehicle_assets
            order by name asc
            """, (rs, rowNum) -> new VehicleAsset(
            rs.getString("id"),
            rs.getString("plate_no"),
            rs.getString("name"),
            rs.getString("vendor"),
            rs.getString("status"),
            rs.getDouble("speed_kph"),
            rs.getDouble("latitude"),
            rs.getDouble("longitude"),
            rs.getString("last_seen_at")
        ));
    }

    @Override
    public List<VehicleTrackPoint> vehicleTracks(String vehicleId) {
        return jdbcTemplate.query("""
            select vehicle_id::text, latitude, longitude, speed_kph, heading, sampled_at::text as sampled_at
            from vehicle_track_points
            where vehicle_id = ?::uuid
            order by sampled_at asc
            """, (rs, rowNum) -> new VehicleTrackPoint(
            rs.getString("vehicle_id"),
            rs.getDouble("latitude"),
            rs.getDouble("longitude"),
            rs.getDouble("speed_kph"),
            rs.getInt("heading"),
            rs.getString("sampled_at")
        ), vehicleId);
    }

    @Override
    public List<AiTask> aiTasks() {
        return jdbcTemplate.query("""
            select id::text, source_type, channel_id::text, algorithm_code, model_version, status,
                   confidence, evidence_url, created_at::text as created_at
            from ai_tasks
            order by created_at desc
            """, (rs, rowNum) -> new AiTask(
            rs.getString("id"),
            rs.getString("source_type"),
            rs.getString("channel_id"),
            rs.getString("algorithm_code"),
            rs.getString("model_version"),
            rs.getString("status"),
            rs.getDouble("confidence"),
            rs.getString("evidence_url"),
            rs.getString("created_at")
        ));
    }

    @Override
    public List<EdgeEvent> edgeEvents() {
        return jdbcTemplate.query("""
            select id::text, event_type, source_type, source_id, severity, title, status,
                   latitude, longitude, evidence_url, detected_at
            from edge_events
            order by detected_at desc
            limit 100
            """, (rs, rowNum) -> new EdgeEvent(
            rs.getString("id"),
            rs.getString("event_type"),
            rs.getString("source_type"),
            rs.getString("source_id"),
            rs.getString("severity"),
            rs.getString("title"),
            rs.getString("status"),
            rs.getDouble("latitude"),
            rs.getDouble("longitude"),
            rs.getString("evidence_url"),
            toInstant(rs.getTimestamp("detected_at"))
        ));
    }

    @Override
    public Optional<UserAccount> userByUsername(String username) {
        return users().stream()
            .filter(user -> user.username().equals(username))
            .findFirst();
    }

    @Override
    public AuditLogEntry appendAuditLog(String actor, String action, String targetType, String targetId, String result) {
        String id = UUID.randomUUID().toString();
        Instant createdAt = Instant.now();
        jdbcTemplate.update("""
            insert into audit_logs(id, actor, action, target_type, target_id, result, created_at)
            values (?::uuid, ?, ?, ?, ?, ?, ?)
            """, id, actor, action, targetType, targetId, result, Timestamp.from(createdAt));
        return new AuditLogEntry(id, actor, action, targetType, targetId, result, createdAt);
    }

    @Override
    public InspectionTask createInspectionTask(String name, String type, String priority, Instant plannedAt) {
        String id = UUID.randomUUID().toString();
        jdbcTemplate.update("""
            insert into inspection_tasks(id, name, type, priority, status, route_name, assignee, planned_at)
            values (?::uuid, ?, ?, ?, 'queued', '东区围界固定航线', '巡检值班员', ?)
            """, id, name, type, priority, Timestamp.from(plannedAt));
        return new InspectionTask(id, name, type, priority, "queued", "东区围界固定航线", "巡检值班员", plannedAt.toString());
    }

    @Override
    public VideoSession createVideoSession(String channelId, String playUrl, String protocol, Instant startedAt, Instant expiresAt) {
        String id = UUID.randomUUID().toString();
        jdbcTemplate.update("""
            insert into video_sessions(id, channel_id, play_url, protocol, status, started_at, expires_at)
            values (?::uuid, ?::uuid, ?, ?, 'playing', ?, ?)
            """, id, toDbChannelId(channelId), playUrl, protocol, Timestamp.from(startedAt), Timestamp.from(expiresAt));
        return new VideoSession(id, channelId, playUrl, protocol, "playing", startedAt.toString(), expiresAt.toString());
    }

    @Override
    public AiTask createAiTask(String sourceType, String channelId, String algorithmCode, String modelVersion, String status, double confidence, String evidenceUrl, Instant createdAt) {
        String id = UUID.randomUUID().toString();
        jdbcTemplate.update("""
            insert into ai_tasks(id, source_type, channel_id, algorithm_code, model_version, status, confidence, evidence_url, created_at)
            values (?::uuid, ?, ?::uuid, ?, ?, ?, ?, ?, ?)
            """, id, sourceType, toDbChannelId(channelId), algorithmCode, modelVersion, status, confidence, evidenceUrl, Timestamp.from(createdAt));
        return new AiTask(id, sourceType, channelId, algorithmCode, modelVersion, status, confidence, evidenceUrl, createdAt.toString());
    }

    @Override
    public EdgeEvent appendEdgeEvent(String eventType, String sourceType, String sourceId, String severity, String title, double latitude, double longitude, String evidenceUrl, Instant detectedAt) {
        String id = UUID.randomUUID().toString();
        jdbcTemplate.update("""
            insert into edge_events(id, event_type, source_type, source_id, severity, title, status, latitude, longitude, evidence_url, detected_at)
            values (?::uuid, ?, ?, ?, ?, ?, 'new', ?, ?, ?, ?)
            """, id, eventType, sourceType, sourceId, severity, title, latitude, longitude, evidenceUrl, Timestamp.from(detectedAt));
        return new EdgeEvent(id, eventType, sourceType, sourceId, severity, title, "new", latitude, longitude, evidenceUrl, detectedAt);
    }

    @Override
    public Optional<AlarmEvent> updateAlarmStatus(String id, String status, Instant handledAt) {
        int updated = jdbcTemplate.update("""
            update alarm_events
            set status = ?,
                handled_at = case when ? in ('false_positive', 'dispatched', 'closed') then ? else handled_at end
            where id = ?::uuid
            """, status, status, Timestamp.from(handledAt), id);
        return updated == 0 ? Optional.empty() : alarmById(id);
    }

    @Override
    public Optional<ModelVersion> updateModelStatus(String id, String status, Map<String, Object> strategy) {
        String strategyJson = toJson(Map.of("lastStrategy", strategy, "lastPublishedAt", Instant.now().toString()));
        int updated = jdbcTemplate.update("""
            update model_versions
            set status = ?,
                metrics = metrics || cast(? as jsonb)
            where id = ?::uuid
            """, status, strategyJson, id);
        return updated == 0 ? Optional.empty() : modelById(id);
    }

    @Override
    public Optional<FlightTask> updateFlightTaskStatus(String id, String status, Instant actionAt) {
        int updated = jdbcTemplate.update("""
            update flight_tasks
            set status = ?,
                started_at = case when ? = 'running' then ? else started_at end,
                finished_at = case when ? in ('cancelled', 'success', 'stopped') then ? else finished_at end
            where id = ?::uuid
            """, status, status, Timestamp.from(actionAt), status, Timestamp.from(actionAt), id);
        if (updated == 0) {
            return Optional.empty();
        }
        List<FlightTask> results = jdbcTemplate.query("""
            select t.id::text,
                   coalesce(r.name, t.name) as route_name,
                   t.status,
                   t.planned_at::text as planned_at
            from flight_tasks t
            left join flight_routes r on r.id = t.route_id
            where t.id = ?::uuid
            """, (rs, rowNum) -> new FlightTask(
            rs.getString("id"),
            rs.getString("route_name"),
            rs.getString("status"),
            rs.getString("planned_at")
        ), id);
        return results.stream().findFirst();
    }

    private int count(String sql) {
        Integer value = jdbcTemplate.queryForObject(sql, Integer.class);
        return value == null ? 0 : value;
    }

    private Instant toInstant(Timestamp timestamp) {
        return timestamp == null ? Instant.EPOCH : timestamp.toInstant();
    }

    private Optional<AlarmEvent> alarmById(String id) {
        List<AlarmEvent> results = jdbcTemplate.query("""
            select a.id::text,
                   a.severity,
                   a.title,
                   coalesce(d.name, '未知设备') as device_name,
                   a.status,
                   a.detected_at,
                   a.evidence_url
            from alarm_events a
            left join devices d on d.id = a.device_id
            where a.id = ?::uuid
            """, (rs, rowNum) -> new AlarmEvent(
            rs.getString("id"),
            rs.getString("severity"),
            rs.getString("title"),
            rs.getString("device_name"),
            rs.getString("status"),
            toInstant(rs.getTimestamp("detected_at")),
            rs.getString("evidence_url")
        ), id);
        return results.stream().findFirst();
    }

    private Optional<ModelVersion> modelById(String id) {
        List<ModelVersion> results = jdbcTemplate.query("""
            select id::text, algorithm_code, version, status, metrics::text
            from model_versions
            where id = ?::uuid
            """, (rs, rowNum) -> new ModelVersion(
            rs.getString("id"),
            rs.getString("algorithm_code"),
            rs.getString("version"),
            rs.getString("status"),
            parseJsonMap(rs.getString("metrics"))
        ), id);
        return results.stream().findFirst();
    }

    private List<String> splitCsv(String value) {
        if (value == null || value.isBlank()) {
            return List.of();
        }
        String cleaned = value.replace("[", "").replace("]", "").replace("\"", "");
        return Arrays.stream(cleaned.split(","))
            .map(String::trim)
            .filter(item -> !item.isBlank())
            .toList();
    }

    private String toDbChannelId(String channelId) {
        return switch (channelId) {
            case "ch-camera-a1" -> "21000000-0000-0000-0000-000000000001";
            case "ch-drone-001" -> "a4000000-0000-0000-0000-000000000001";
            case "ch-vehicle-front" -> "a4000000-0000-0000-0000-000000000002";
            default -> channelId;
        };
    }

    private VideoChannel toVideoChannel(java.sql.ResultSet rs) throws java.sql.SQLException {
        return new VideoChannel(
            rs.getString("id"),
            rs.getString("source_id"),
            rs.getString("source_type"),
            rs.getString("source_name"),
            rs.getString("name"),
            rs.getString("protocol"),
            rs.getString("stream_url"),
            rs.getString("play_url"),
            rs.getBoolean("ai_enabled"),
            rs.getString("status"),
            rs.getString("edge_node"),
            rs.getDouble("latitude"),
            rs.getDouble("longitude")
        );
    }

    private String toJson(Map<String, Object> value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException exception) {
            throw new IllegalArgumentException("Invalid JSON payload", exception);
        }
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> parseJsonMap(String value) {
        if (value == null || value.isBlank()) {
            return Map.of();
        }
        try {
            return objectMapper.readValue(value, Map.class);
        } catch (JsonProcessingException exception) {
            return Map.of("raw", value);
        }
    }
}
