CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

CREATE TABLE IF NOT EXISTS organizations (
  id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
  name VARCHAR(120) NOT NULL,
  code VARCHAR(64) UNIQUE NOT NULL,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE IF NOT EXISTS sites (
  id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
  organization_id UUID REFERENCES organizations(id),
  name VARCHAR(120) NOT NULL,
  location VARCHAR(255),
  created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE IF NOT EXISTS roles (
  id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
  code VARCHAR(64) UNIQUE NOT NULL,
  name VARCHAR(120) NOT NULL,
  description TEXT,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE IF NOT EXISTS role_permissions (
  role_id UUID REFERENCES roles(id) ON DELETE CASCADE,
  permission_code VARCHAR(120) NOT NULL,
  PRIMARY KEY (role_id, permission_code)
);

CREATE TABLE IF NOT EXISTS user_accounts (
  id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
  organization_id UUID REFERENCES organizations(id),
  site_id UUID REFERENCES sites(id),
  username VARCHAR(64) UNIQUE NOT NULL,
  display_name VARCHAR(120) NOT NULL,
  password_hint VARCHAR(120),
  status VARCHAR(32) NOT NULL DEFAULT 'active',
  created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE IF NOT EXISTS user_roles (
  user_id UUID REFERENCES user_accounts(id) ON DELETE CASCADE,
  role_id UUID REFERENCES roles(id) ON DELETE CASCADE,
  PRIMARY KEY (user_id, role_id)
);

CREATE TABLE IF NOT EXISTS audit_logs (
  id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
  actor VARCHAR(120) NOT NULL,
  action VARCHAR(120) NOT NULL,
  target_type VARCHAR(64) NOT NULL,
  target_id VARCHAR(120) NOT NULL,
  result VARCHAR(32) NOT NULL,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE IF NOT EXISTS edge_nodes (
  id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
  site_id UUID REFERENCES sites(id),
  name VARCHAR(120) NOT NULL,
  status VARCHAR(32) NOT NULL DEFAULT 'offline',
  ip_address VARCHAR(64),
  gpu_status JSONB NOT NULL DEFAULT '{}'::jsonb,
  last_seen_at TIMESTAMPTZ
);

CREATE TABLE IF NOT EXISTS devices (
  id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
  site_id UUID REFERENCES sites(id),
  edge_node_id UUID REFERENCES edge_nodes(id),
  name VARCHAR(120) NOT NULL,
  vendor VARCHAR(64) NOT NULL,
  protocol VARCHAR(64) NOT NULL,
  status VARCHAR(32) NOT NULL DEFAULT 'offline',
  stream_url TEXT,
  ptz_enabled BOOLEAN NOT NULL DEFAULT false,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

ALTER TABLE devices ADD COLUMN IF NOT EXISTS source_type VARCHAR(32) NOT NULL DEFAULT 'camera';
ALTER TABLE devices ADD COLUMN IF NOT EXISTS endpoint TEXT NOT NULL DEFAULT '';
ALTER TABLE devices ADD COLUMN IF NOT EXISTS credential_ref TEXT NOT NULL DEFAULT '';
ALTER TABLE devices ADD COLUMN IF NOT EXISTS location VARCHAR(255) NOT NULL DEFAULT '';
ALTER TABLE devices ADD COLUMN IF NOT EXISTS edge_node_ref VARCHAR(120) NOT NULL DEFAULT 'EDGE-01';
ALTER TABLE devices ADD COLUMN IF NOT EXISTS updated_at TIMESTAMPTZ NOT NULL DEFAULT now();
ALTER TABLE devices ADD COLUMN IF NOT EXISTS deleted_at TIMESTAMPTZ;

CREATE TABLE IF NOT EXISTS video_channels (
  id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
  device_id UUID REFERENCES devices(id),
  name VARCHAR(120) NOT NULL,
  channel_no VARCHAR(64) NOT NULL,
  stream_profile VARCHAR(32) NOT NULL DEFAULT 'main',
  online BOOLEAN NOT NULL DEFAULT false
);

CREATE TABLE IF NOT EXISTS ai_algorithms (
  id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
  name VARCHAR(120) NOT NULL,
  code VARCHAR(64) UNIQUE NOT NULL,
  category VARCHAR(64) NOT NULL,
  status VARCHAR(32) NOT NULL DEFAULT 'enabled'
);

CREATE TABLE IF NOT EXISTS inspection_plans (
  id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
  site_id UUID REFERENCES sites(id),
  name VARCHAR(120) NOT NULL,
  schedule_cron VARCHAR(64) NOT NULL,
  status VARCHAR(32) NOT NULL DEFAULT 'enabled',
  rules JSONB NOT NULL DEFAULT '{}'::jsonb,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE IF NOT EXISTS alarm_events (
  id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
  site_id UUID REFERENCES sites(id),
  device_id UUID REFERENCES devices(id),
  algorithm_code VARCHAR(64) NOT NULL,
  severity VARCHAR(32) NOT NULL,
  status VARCHAR(32) NOT NULL DEFAULT 'new',
  title VARCHAR(160) NOT NULL,
  evidence_url TEXT,
  detected_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  handled_at TIMESTAMPTZ
);

CREATE TABLE IF NOT EXISTS datasets (
  id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
  name VARCHAR(120) NOT NULL,
  algorithm_code VARCHAR(64) NOT NULL,
  sample_count INTEGER NOT NULL DEFAULT 0,
  status VARCHAR(32) NOT NULL DEFAULT 'collecting'
);

CREATE TABLE IF NOT EXISTS training_jobs (
  id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
  dataset_id UUID REFERENCES datasets(id),
  name VARCHAR(120) NOT NULL,
  status VARCHAR(32) NOT NULL DEFAULT 'queued',
  progress INTEGER NOT NULL DEFAULT 0,
  metrics JSONB NOT NULL DEFAULT '{}'::jsonb,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE IF NOT EXISTS model_versions (
  id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
  algorithm_code VARCHAR(64) NOT NULL,
  version VARCHAR(64) NOT NULL,
  status VARCHAR(32) NOT NULL DEFAULT 'candidate',
  metrics JSONB NOT NULL DEFAULT '{}'::jsonb,
  artifact_url TEXT,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE IF NOT EXISTS drone_docks (
  id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
  site_id UUID REFERENCES sites(id),
  name VARCHAR(120) NOT NULL,
  vendor VARCHAR(64) NOT NULL,
  status VARCHAR(32) NOT NULL DEFAULT 'offline',
  battery_percent INTEGER NOT NULL DEFAULT 0,
  weather JSONB NOT NULL DEFAULT '{}'::jsonb,
  last_seen_at TIMESTAMPTZ
);

CREATE TABLE IF NOT EXISTS flight_routes (
  id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
  dock_id UUID REFERENCES drone_docks(id),
  name VARCHAR(120) NOT NULL,
  waypoint_count INTEGER NOT NULL DEFAULT 0,
  altitude_meter INTEGER NOT NULL DEFAULT 80,
  status VARCHAR(32) NOT NULL DEFAULT 'ready'
);

CREATE TABLE IF NOT EXISTS flight_tasks (
  id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
  route_id UUID REFERENCES flight_routes(id),
  name VARCHAR(120) NOT NULL,
  status VARCHAR(32) NOT NULL DEFAULT 'scheduled',
  planned_at TIMESTAMPTZ,
  started_at TIMESTAMPTZ,
  finished_at TIMESTAMPTZ
);

CREATE TABLE IF NOT EXISTS inspection_tasks (
  id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
  name VARCHAR(120) NOT NULL,
  type VARCHAR(32) NOT NULL DEFAULT 'immediate',
  priority VARCHAR(32) NOT NULL DEFAULT 'medium',
  status VARCHAR(32) NOT NULL DEFAULT 'queued',
  route_name VARCHAR(120),
  assignee VARCHAR(120),
  planned_at TIMESTAMPTZ,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE IF NOT EXISTS media_assets (
  id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
  name VARCHAR(160) NOT NULL,
  asset_type VARCHAR(32) NOT NULL,
  source VARCHAR(32) NOT NULL,
  related_task VARCHAR(160),
  status VARCHAR(32) NOT NULL DEFAULT 'stored',
  url TEXT,
  captured_at TIMESTAMPTZ,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE IF NOT EXISTS inspection_reports (
  id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
  title VARCHAR(160) NOT NULL,
  period VARCHAR(64) NOT NULL,
  status VARCHAR(32) NOT NULL DEFAULT 'draft',
  format VARCHAR(32) NOT NULL DEFAULT 'PDF',
  generated_at TIMESTAMPTZ,
  download_url TEXT,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE IF NOT EXISTS algorithm_parameters (
  id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
  algorithm_code VARCHAR(64) NOT NULL,
  threshold NUMERIC(4, 3) NOT NULL DEFAULT 0.700,
  sensitivity INTEGER NOT NULL DEFAULT 80,
  enabled BOOLEAN NOT NULL DEFAULT true,
  updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  UNIQUE(algorithm_code)
);

CREATE TABLE IF NOT EXISTS map_events (
  id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
  title VARCHAR(160) NOT NULL,
  event_type VARCHAR(64) NOT NULL,
  severity VARCHAR(32) NOT NULL,
  status VARCHAR(32) NOT NULL DEFAULT 'new',
  latitude DOUBLE PRECISION NOT NULL,
  longitude DOUBLE PRECISION NOT NULL,
  source VARCHAR(32) NOT NULL,
  detected_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

INSERT INTO organizations(name, code) VALUES ('示范园区运营中心', 'demo-org') ON CONFLICT (code) DO NOTHING;
INSERT INTO sites(organization_id, name, location)
SELECT id, '一号工业园', '东区 A/B/C 厂房' FROM organizations WHERE code = 'demo-org' LIMIT 1;

INSERT INTO roles(id, code, name, description) VALUES
('30000000-0000-0000-0000-000000000001', 'platform_admin', '平台管理员', '平台配置、设备、模型发布和审计管理'),
('30000000-0000-0000-0000-000000000002', 'inspection_operator', '巡检值班员', '查看视频、处置告警、下发巡检任务'),
('30000000-0000-0000-0000-000000000003', 'ai_engineer', '算法工程师', '管理数据集、训练任务和模型版本')
ON CONFLICT (code) DO NOTHING;

INSERT INTO role_permissions(role_id, permission_code) VALUES
('30000000-0000-0000-0000-000000000001', '*'),
('30000000-0000-0000-0000-000000000002', 'video:view'),
('30000000-0000-0000-0000-000000000002', 'alarm:handle'),
('30000000-0000-0000-0000-000000000002', 'task:dispatch'),
('30000000-0000-0000-0000-000000000003', 'dataset:manage'),
('30000000-0000-0000-0000-000000000003', 'training:manage'),
('30000000-0000-0000-0000-000000000003', 'model:publish')
ON CONFLICT DO NOTHING;

INSERT INTO user_accounts(id, organization_id, site_id, username, display_name, password_hint, status)
SELECT '31000000-0000-0000-0000-000000000001', o.id, s.id, 'admin', '系统管理员', 'demo123', 'active'
FROM organizations o JOIN sites s ON s.organization_id = o.id
WHERE o.code = 'demo-org' AND s.name = '一号工业园'
ON CONFLICT (username) DO NOTHING;

INSERT INTO user_accounts(id, organization_id, site_id, username, display_name, password_hint, status)
SELECT '31000000-0000-0000-0000-000000000002', o.id, s.id, 'operator', '巡检值班员', 'demo123', 'active'
FROM organizations o JOIN sites s ON s.organization_id = o.id
WHERE o.code = 'demo-org' AND s.name = '一号工业园'
ON CONFLICT (username) DO NOTHING;

INSERT INTO user_accounts(id, organization_id, site_id, username, display_name, password_hint, status)
SELECT '31000000-0000-0000-0000-000000000003', o.id, s.id, 'ai.engineer', '算法工程师', 'demo123', 'active'
FROM organizations o JOIN sites s ON s.organization_id = o.id
WHERE o.code = 'demo-org' AND s.name = '一号工业园'
ON CONFLICT (username) DO NOTHING;

INSERT INTO user_roles(user_id, role_id) VALUES
('31000000-0000-0000-0000-000000000001', '30000000-0000-0000-0000-000000000001'),
('31000000-0000-0000-0000-000000000002', '30000000-0000-0000-0000-000000000002'),
('31000000-0000-0000-0000-000000000003', '30000000-0000-0000-0000-000000000003')
ON CONFLICT DO NOTHING;

INSERT INTO audit_logs(actor, action, target_type, target_id, result) VALUES
('system', 'BOOTSTRAP', 'platform', 'demo', 'success')
ON CONFLICT DO NOTHING;

INSERT INTO ai_algorithms(name, code, category) VALUES
('人员闯入识别', 'person_intrusion', 'safety'),
('烟火识别', 'smoke_fire', 'safety'),
('安全帽识别', 'helmet_detection', 'ppe'),
('车辆违停识别', 'vehicle_parking', 'traffic')
ON CONFLICT (code) DO NOTHING;

INSERT INTO edge_nodes(id, site_id, name, status, ip_address, gpu_status, last_seen_at)
SELECT '10000000-0000-0000-0000-000000000001', s.id, 'EDGE-01', 'online', '192.168.10.21',
       '{"usage": 62, "memory": "8.6GB/16GB", "temperature": 66}'::jsonb, now()
FROM sites s
WHERE s.name = '一号工业园'
ON CONFLICT (id) DO NOTHING;

UPDATE devices
SET source_type = coalesce(nullif(source_type, ''), 'camera'),
    endpoint = coalesce(nullif(endpoint, ''), stream_url, ''),
    credential_ref = coalesce(nullif(credential_ref, ''), 'secret://integrations/demo'),
    location = coalesce(nullif(location, ''), 'Demo site'),
    edge_node_ref = coalesce(nullif(edge_node_ref, ''), 'EDGE-01'),
    updated_at = coalesce(updated_at, created_at)
WHERE deleted_at IS NULL;

INSERT INTO devices(id, site_id, edge_node_id, name, vendor, protocol, status, stream_url, ptz_enabled)
SELECT '20000000-0000-0000-0000-000000000001', s.id, '10000000-0000-0000-0000-000000000001', 'A区危化仓-枪机01', 'HIKVISION', 'GB28181', 'online', 'rtsp://example/live/a1', false
FROM sites s WHERE s.name = '一号工业园'
ON CONFLICT (id) DO NOTHING;

INSERT INTO devices(id, site_id, edge_node_id, name, vendor, protocol, status, stream_url, ptz_enabled)
SELECT '20000000-0000-0000-0000-000000000002', s.id, '10000000-0000-0000-0000-000000000001', 'B区围界-球机03', 'DAHUA', 'ONVIF', 'online', 'rtsp://example/live/b3', true
FROM sites s WHERE s.name = '一号工业园'
ON CONFLICT (id) DO NOTHING;

INSERT INTO devices(id, site_id, edge_node_id, name, vendor, protocol, status, stream_url, ptz_enabled)
SELECT '20000000-0000-0000-0000-000000000003', s.id, '10000000-0000-0000-0000-000000000001', 'C区装卸口-枪机02', 'GENERIC', 'RTSP', 'online', 'rtsp://example/live/c2', false
FROM sites s WHERE s.name = '一号工业园'
ON CONFLICT (id) DO NOTHING;

INSERT INTO devices(id, site_id, edge_node_id, name, vendor, protocol, status, stream_url, ptz_enabled)
SELECT '20000000-0000-0000-0000-000000000004', s.id, '10000000-0000-0000-0000-000000000001', 'D区停车场-枪机06', 'JT1078', 'JT/T1078', 'offline', null, false
FROM sites s WHERE s.name = '一号工业园'
ON CONFLICT (id) DO NOTHING;

INSERT INTO video_channels(id, device_id, name, channel_no, stream_profile, online) VALUES
('21000000-0000-0000-0000-000000000001', '20000000-0000-0000-0000-000000000001', '主码流', '1', 'main', true),
('21000000-0000-0000-0000-000000000002', '20000000-0000-0000-0000-000000000002', '主码流', '1', 'main', true),
('21000000-0000-0000-0000-000000000003', '20000000-0000-0000-0000-000000000003', '主码流', '1', 'main', true),
('21000000-0000-0000-0000-000000000004', '20000000-0000-0000-0000-000000000004', '主码流', '1', 'main', false)
ON CONFLICT (id) DO NOTHING;

INSERT INTO inspection_plans(id, site_id, name, schedule_cron, status, rules)
SELECT '30000000-0000-0000-0000-000000000001', s.id, '危化仓烟火巡检', '0 */5 * * * *', 'running',
       '{"algorithm": "烟火识别", "pointCount": 12, "severity": "high", "dedupeWindowSeconds": 120}'::jsonb
FROM sites s WHERE s.name = '一号工业园'
ON CONFLICT (id) DO NOTHING;

INSERT INTO inspection_plans(id, site_id, name, schedule_cron, status, rules)
SELECT '30000000-0000-0000-0000-000000000002', s.id, '围界人员闯入巡检', '0 * * * * *', 'running',
       '{"algorithm": "人员闯入", "pointCount": 18, "severity": "medium", "dedupeWindowSeconds": 120}'::jsonb
FROM sites s WHERE s.name = '一号工业园'
ON CONFLICT (id) DO NOTHING;

INSERT INTO inspection_plans(id, site_id, name, schedule_cron, status, rules)
SELECT '30000000-0000-0000-0000-000000000003', s.id, '装卸区安全帽巡检', '0 */10 8-20 * * MON-FRI', 'paused',
       '{"algorithm": "安全帽识别", "pointCount": 9, "severity": "medium", "dedupeWindowSeconds": 180}'::jsonb
FROM sites s WHERE s.name = '一号工业园'
ON CONFLICT (id) DO NOTHING;

INSERT INTO alarm_events(id, site_id, device_id, algorithm_code, severity, status, title, evidence_url, detected_at)
SELECT '40000000-0000-0000-0000-000000000001', s.id, '20000000-0000-0000-0000-000000000001', 'smoke_fire', 'high', 'new', '烟火识别', '/evidence/alarm-001.jpg', now() - interval '5 minutes'
FROM sites s WHERE s.name = '一号工业园'
ON CONFLICT (id) DO NOTHING;

INSERT INTO alarm_events(id, site_id, device_id, algorithm_code, severity, status, title, evidence_url, detected_at)
SELECT '40000000-0000-0000-0000-000000000002', s.id, '20000000-0000-0000-0000-000000000002', 'person_intrusion', 'medium', 'processing', '人员闯入', '/evidence/alarm-002.jpg', now() - interval '18 minutes'
FROM sites s WHERE s.name = '一号工业园'
ON CONFLICT (id) DO NOTHING;

INSERT INTO alarm_events(id, site_id, device_id, algorithm_code, severity, status, title, evidence_url, detected_at)
SELECT '40000000-0000-0000-0000-000000000003', s.id, '20000000-0000-0000-0000-000000000003', 'helmet_detection', 'low', 'closed', '安全帽识别', '/evidence/alarm-003.jpg', now() - interval '48 minutes'
FROM sites s WHERE s.name = '一号工业园'
ON CONFLICT (id) DO NOTHING;

INSERT INTO datasets(id, name, algorithm_code, sample_count, status) VALUES
('50000000-0000-0000-0000-000000000001', '烟火负样本增强', 'smoke_fire', 1284, 'labeling'),
('50000000-0000-0000-0000-000000000002', '围界闯入样本', 'person_intrusion', 876, 'qc'),
('50000000-0000-0000-0000-000000000003', '安全帽样本', 'helmet_detection', 2143, 'ready')
ON CONFLICT (id) DO NOTHING;

INSERT INTO training_jobs(id, dataset_id, name, status, progress, metrics) VALUES
('60000000-0000-0000-0000-000000000001', '50000000-0000-0000-0000-000000000001', 'smoke_fire_v1.5', 'running', 62,
 '{"mAP50": 0.948, "recall": 0.916, "gpu": "71%"}'::jsonb)
ON CONFLICT (id) DO NOTHING;

INSERT INTO model_versions(id, algorithm_code, version, status, metrics, artifact_url) VALUES
('70000000-0000-0000-0000-000000000001', '烟火识别', 'v1.4.2', 'production', '{"precision": "94.8%", "recall": "91.6%"}'::jsonb, 'minio://models/smoke_fire/v1.4.2.onnx'),
('70000000-0000-0000-0000-000000000002', '人员闯入', 'v1.2.0', 'canary', '{"precision": "96.1%", "recall": "93.2%"}'::jsonb, 'minio://models/person_intrusion/v1.2.0.onnx'),
('70000000-0000-0000-0000-000000000003', '安全帽识别', 'v0.9.8', 'candidate', '{"precision": "91.7%", "recall": "88.5%"}'::jsonb, 'minio://models/helmet_detection/v0.9.8.onnx')
ON CONFLICT (id) DO NOTHING;

INSERT INTO drone_docks(id, site_id, name, vendor, status, battery_percent, weather, last_seen_at)
SELECT '80000000-0000-0000-0000-000000000001', s.id, '一号机场', 'GENERIC_DOCK', 'ready', 87,
       '{"summary": "风速 3.1m/s / 小雨", "windSpeed": 3.1, "rain": "light"}'::jsonb, now()
FROM sites s WHERE s.name = '一号工业园'
ON CONFLICT (id) DO NOTHING;

INSERT INTO flight_routes(id, dock_id, name, waypoint_count, altitude_meter, status) VALUES
('90000000-0000-0000-0000-000000000001', '80000000-0000-0000-0000-000000000001', '东区围界固定航线', 18, 86, 'ready'),
('90000000-0000-0000-0000-000000000002', '80000000-0000-0000-0000-000000000001', '危化仓屋顶巡检', 12, 80, 'ready')
ON CONFLICT (id) DO NOTHING;

INSERT INTO flight_tasks(id, route_id, name, status, planned_at, started_at) VALUES
('91000000-0000-0000-0000-000000000001', '90000000-0000-0000-0000-000000000001', '东区围界固定航线', 'running', '2026-06-15T21:00:00+08:00', now() - interval '8 minutes'),
('91000000-0000-0000-0000-000000000002', '90000000-0000-0000-0000-000000000002', '危化仓屋顶巡检', 'scheduled', '2026-06-16T09:30:00+08:00', null)
ON CONFLICT (id) DO NOTHING;

INSERT INTO inspection_tasks(id, name, type, priority, status, route_name, assignee, planned_at) VALUES
('92000000-0000-0000-0000-000000000001', '危化仓烟火即时复核', 'immediate', 'high', 'running', 'A区危化仓点位', '巡检值班员', '2026-06-16T09:20:00+08:00'),
('92000000-0000-0000-0000-000000000002', '东区围界定时巡检', 'scheduled', 'medium', 'scheduled', '东区围界固定航线', '无人机机场', '2026-06-16T10:00:00+08:00'),
('92000000-0000-0000-0000-000000000003', '安全帽识别循环抽检', 'loop', 'medium', 'queued', 'C区装卸口', '边缘节点 EDGE-01', '2026-06-16T10:30:00+08:00')
ON CONFLICT (id) DO NOTHING;

INSERT INTO media_assets(id, name, asset_type, source, related_task, status, url, captured_at) VALUES
('93000000-0000-0000-0000-000000000001', 'A区危化仓烟火证据图', 'image', 'camera', '危化仓烟火巡检', 'stored', '/evidence/alarm-001.jpg', '2026-06-16T09:18:00+08:00'),
('93000000-0000-0000-0000-000000000002', '东区围界无人机巡检视频', 'video', 'drone', '东区围界固定航线', 'stored', '/media/flight-001.mp4', '2026-06-16T09:28:00+08:00'),
('93000000-0000-0000-0000-000000000003', '安全帽样本采集包', 'dataset', 'ai', '装卸区安全帽巡检', 'labeling', '/datasets/helmet-batch-001', '2026-06-16T09:35:00+08:00')
ON CONFLICT (id) DO NOTHING;

INSERT INTO inspection_reports(id, title, period, status, format, generated_at, download_url) VALUES
('94000000-0000-0000-0000-000000000001', '一号工业园日巡检报告', '2026-06-16', 'generated', 'PDF', '2026-06-16T09:40:00+08:00', '/reports/daily-20260616.pdf'),
('94000000-0000-0000-0000-000000000002', '无人机围界巡检报告', '2026-W25', 'draft', 'HTML', null, '/reports/flight-week-25.html'),
('94000000-0000-0000-0000-000000000003', 'AI误报复核周报', '2026-W25', 'queued', 'Excel', null, '/reports/ai-review-week-25.xlsx')
ON CONFLICT (id) DO NOTHING;

INSERT INTO algorithm_parameters(id, algorithm_code, threshold, sensitivity, enabled, updated_at) VALUES
('95000000-0000-0000-0000-000000000001', 'smoke_fire', 0.720, 86, true, '2026-06-16T09:00:00+08:00'),
('95000000-0000-0000-0000-000000000002', 'person_intrusion', 0.680, 80, true, '2026-06-16T09:00:00+08:00'),
('95000000-0000-0000-0000-000000000003', 'helmet_detection', 0.740, 78, true, '2026-06-16T09:00:00+08:00')
ON CONFLICT (algorithm_code) DO NOTHING;

INSERT INTO map_events(id, title, event_type, severity, status, latitude, longitude, source, detected_at) VALUES
('96000000-0000-0000-0000-000000000001', 'A区危化仓烟火识别', '烟火识别', 'high', 'new', 31.2312, 121.4741, 'camera', now() - interval '5 minutes'),
('96000000-0000-0000-0000-000000000002', 'B区围界人员闯入', '人员闯入', 'medium', 'processing', 31.2306, 121.4729, 'camera', now() - interval '18 minutes'),
('96000000-0000-0000-0000-000000000003', '东区无人机巡检异常点', '无人机巡检', 'medium', 'new', 31.2321, 121.4752, 'drone', now() - interval '26 minutes')
ON CONFLICT (id) DO NOTHING;

ALTER TABLE video_channels ADD COLUMN IF NOT EXISTS source_type VARCHAR(32) NOT NULL DEFAULT 'camera';
ALTER TABLE video_channels ADD COLUMN IF NOT EXISTS source_id VARCHAR(120);
ALTER TABLE video_channels ADD COLUMN IF NOT EXISTS source_name VARCHAR(160);
ALTER TABLE video_channels ADD COLUMN IF NOT EXISTS protocol VARCHAR(64);
ALTER TABLE video_channels ADD COLUMN IF NOT EXISTS stream_url TEXT;
ALTER TABLE video_channels ADD COLUMN IF NOT EXISTS play_url TEXT;
ALTER TABLE video_channels ADD COLUMN IF NOT EXISTS ai_enabled BOOLEAN NOT NULL DEFAULT true;
ALTER TABLE video_channels ADD COLUMN IF NOT EXISTS status VARCHAR(32) NOT NULL DEFAULT 'offline';
ALTER TABLE video_channels ADD COLUMN IF NOT EXISTS edge_node VARCHAR(120) NOT NULL DEFAULT 'EDGE-01';
ALTER TABLE video_channels ADD COLUMN IF NOT EXISTS latitude DOUBLE PRECISION NOT NULL DEFAULT 31.2312;
ALTER TABLE video_channels ADD COLUMN IF NOT EXISTS longitude DOUBLE PRECISION NOT NULL DEFAULT 121.4741;

UPDATE video_channels vc
SET source_id = coalesce(source_id, vc.device_id::text),
    source_name = coalesce(source_name, d.name),
    protocol = coalesce(vc.protocol, d.protocol),
    stream_url = coalesce(vc.stream_url, d.stream_url),
    play_url = coalesce(vc.play_url, 'http://localhost:8088/live/' || vc.id::text || '.flv'),
    status = case when vc.online then 'online' else 'offline' end
FROM devices d
WHERE d.id = vc.device_id;

CREATE TABLE IF NOT EXISTS integration_configs (
  id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
  name VARCHAR(160) NOT NULL,
  source_type VARCHAR(32) NOT NULL,
  vendor VARCHAR(64) NOT NULL,
  sdk_type VARCHAR(64) NOT NULL,
  status VARCHAR(32) NOT NULL DEFAULT 'offline',
  endpoint TEXT NOT NULL,
  credential_ref TEXT NOT NULL,
  last_sync_at TIMESTAMPTZ,
  channel_count INTEGER NOT NULL DEFAULT 0,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE IF NOT EXISTS video_sessions (
  id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
  channel_id UUID REFERENCES video_channels(id),
  play_url TEXT NOT NULL,
  protocol VARCHAR(32) NOT NULL,
  status VARCHAR(32) NOT NULL DEFAULT 'playing',
  started_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  expires_at TIMESTAMPTZ NOT NULL
);

CREATE TABLE IF NOT EXISTS drones (
  id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
  dock_id UUID REFERENCES drone_docks(id),
  name VARCHAR(160) NOT NULL,
  vendor VARCHAR(64) NOT NULL,
  status VARCHAR(32) NOT NULL DEFAULT 'offline',
  battery_percent INTEGER NOT NULL DEFAULT 0,
  latitude DOUBLE PRECISION NOT NULL,
  longitude DOUBLE PRECISION NOT NULL,
  telemetry JSONB NOT NULL DEFAULT '[]'::jsonb,
  active_task VARCHAR(160),
  last_seen_at TIMESTAMPTZ
);

CREATE TABLE IF NOT EXISTS vehicle_assets (
  id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
  plate_no VARCHAR(32) UNIQUE NOT NULL,
  name VARCHAR(160) NOT NULL,
  vendor VARCHAR(64) NOT NULL,
  status VARCHAR(32) NOT NULL DEFAULT 'offline',
  speed_kph NUMERIC(6, 2) NOT NULL DEFAULT 0,
  latitude DOUBLE PRECISION NOT NULL,
  longitude DOUBLE PRECISION NOT NULL,
  last_seen_at TIMESTAMPTZ
);

CREATE TABLE IF NOT EXISTS vehicle_track_points (
  id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
  vehicle_id UUID REFERENCES vehicle_assets(id),
  latitude DOUBLE PRECISION NOT NULL,
  longitude DOUBLE PRECISION NOT NULL,
  speed_kph NUMERIC(6, 2) NOT NULL DEFAULT 0,
  heading INTEGER NOT NULL DEFAULT 0,
  sampled_at TIMESTAMPTZ NOT NULL
);

CREATE TABLE IF NOT EXISTS ai_tasks (
  id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
  source_type VARCHAR(32) NOT NULL,
  channel_id UUID REFERENCES video_channels(id),
  algorithm_code VARCHAR(64) NOT NULL,
  model_version VARCHAR(64) NOT NULL,
  status VARCHAR(32) NOT NULL DEFAULT 'queued',
  confidence NUMERIC(5, 4) NOT NULL DEFAULT 0,
  evidence_url TEXT,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE IF NOT EXISTS edge_events (
  id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
  event_type VARCHAR(64) NOT NULL,
  source_type VARCHAR(32) NOT NULL,
  source_id VARCHAR(120) NOT NULL,
  severity VARCHAR(32) NOT NULL,
  title VARCHAR(160) NOT NULL,
  status VARCHAR(32) NOT NULL DEFAULT 'new',
  latitude DOUBLE PRECISION NOT NULL,
  longitude DOUBLE PRECISION NOT NULL,
  evidence_url TEXT,
  detected_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

INSERT INTO integration_configs(id, name, source_type, vendor, sdk_type, status, endpoint, credential_ref, last_sync_at, channel_count) VALUES
('a1000000-0000-0000-0000-000000000001', '海康厂区视频 SDK', 'camera', 'HIKVISION', 'HIK_SDK', 'online', 'https://hik-gateway.local', 'secret://integrations/hikvision', now() - interval '8 minutes', 3),
('a1000000-0000-0000-0000-000000000002', '大疆机场开放接口', 'drone', 'DJI_DOCK', 'DJI_CLOUD_API', 'online', 'https://dji-dock.local', 'secret://integrations/dji', now() - interval '12 minutes', 1),
('a1000000-0000-0000-0000-000000000003', '车载 JT/T1078 网关', 'vehicle', 'JT1078_GATEWAY', 'JT1078_SDK', 'online', 'https://jt1078-gateway.local', 'secret://integrations/jt1078', now() - interval '14 minutes', 2)
ON CONFLICT (id) DO NOTHING;

INSERT INTO drones(id, dock_id, name, vendor, status, battery_percent, latitude, longitude, telemetry, active_task, last_seen_at) VALUES
('a2000000-0000-0000-0000-000000000001', '80000000-0000-0000-0000-000000000001', '一号机场无人机', 'DJI_DOCK', 'ready', 87, 31.2321, 121.4752,
 '["ALT 86m", "SPD 8.4m/s", "HDG 126deg", "LINK 98%"]'::jsonb, '东区围界固定航线', now())
ON CONFLICT (id) DO NOTHING;

INSERT INTO vehicle_assets(id, plate_no, name, vendor, status, speed_kph, latitude, longitude, last_seen_at) VALUES
('a3000000-0000-0000-0000-000000000001', '沪A-D8123', '危化品转运车01', 'JT1078_GATEWAY', 'online', 36.5, 31.2298, 121.4718, now()),
('a3000000-0000-0000-0000-000000000002', '沪A-F2639', '巡逻车02', 'JT1078_GATEWAY', 'online', 18.2, 31.2330, 121.4760, now() - interval '1 minute')
ON CONFLICT (plate_no) DO NOTHING;

INSERT INTO vehicle_track_points(vehicle_id, latitude, longitude, speed_kph, heading, sampled_at) VALUES
('a3000000-0000-0000-0000-000000000001', 31.2298, 121.4718, 36.5, 86, '2026-06-16T09:20:00+08:00'),
('a3000000-0000-0000-0000-000000000001', 31.2304, 121.4726, 32.0, 92, '2026-06-16T09:24:00+08:00'),
('a3000000-0000-0000-0000-000000000001', 31.2311, 121.4734, 28.2, 104, '2026-06-16T09:28:00+08:00');

INSERT INTO video_channels(id, device_id, name, channel_no, stream_profile, online, source_type, source_id, source_name, protocol, stream_url, play_url, ai_enabled, status, edge_node, latitude, longitude) VALUES
('a4000000-0000-0000-0000-000000000001', null, '无人机直播', '1', 'main', true, 'drone', 'a2000000-0000-0000-0000-000000000001', '一号机场无人机', 'DJI_SDK', 'rtsp://example/live/drone-001', 'http://localhost:8088/live/drone-001.flv', true, 'online', 'EDGE-01', 31.2321, 121.4752),
('a4000000-0000-0000-0000-000000000002', null, '前向摄像头', '1', 'main', true, 'vehicle', 'a3000000-0000-0000-0000-000000000001', '危化品转运车01', 'JT1078', 'jt1078://vehicle-001/front', 'http://localhost:8088/live/vehicle-front.flv', true, 'online', 'EDGE-01', 31.2298, 121.4718),
('a4000000-0000-0000-0000-000000000003', null, '车厢摄像头', '2', 'main', true, 'vehicle', 'a3000000-0000-0000-0000-000000000001', '危化品转运车01', 'JT1078', 'jt1078://vehicle-001/cargo', 'http://localhost:8088/live/vehicle-cargo.flv', true, 'online', 'EDGE-01', 31.2298, 121.4718)
ON CONFLICT (id) DO NOTHING;

INSERT INTO ai_tasks(id, source_type, channel_id, algorithm_code, model_version, status, confidence, evidence_url, created_at) VALUES
('a5000000-0000-0000-0000-000000000001', 'camera', '21000000-0000-0000-0000-000000000001', 'smoke_fire', 'v1.4.2', 'running', 0.9100, '/evidence/alarm-001.jpg', now() - interval '9 minutes'),
('a5000000-0000-0000-0000-000000000002', 'drone', 'a4000000-0000-0000-0000-000000000001', 'person_intrusion', 'v1.2.0', 'running', 0.8700, '/media/flight-001.mp4', now() - interval '6 minutes'),
('a5000000-0000-0000-0000-000000000003', 'vehicle', 'a4000000-0000-0000-0000-000000000002', 'vehicle_parking', 'v0.8.0', 'running', 0.8200, '/evidence/vehicle-001.jpg', now() - interval '4 minutes')
ON CONFLICT (id) DO NOTHING;

INSERT INTO edge_events(id, event_type, source_type, source_id, severity, title, status, latitude, longitude, evidence_url, detected_at) VALUES
('a6000000-0000-0000-0000-000000000001', 'ai_alarm', 'camera', '21000000-0000-0000-0000-000000000001', 'high', 'A区危化仓烟火识别', 'new', 31.2312, 121.4741, '/evidence/alarm-001.jpg', now() - interval '5 minutes'),
('a6000000-0000-0000-0000-000000000002', 'ai_alarm', 'vehicle', 'a4000000-0000-0000-0000-000000000002', 'medium', '车载通道异常停车', 'new', 31.2298, 121.4718, '/evidence/vehicle-001.jpg', now() - interval '4 minutes')
ON CONFLICT (id) DO NOTHING;
