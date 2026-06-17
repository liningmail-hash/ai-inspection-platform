-- P0 upgrade: camera + drone + vehicle unified access model.
-- Run after the original baseline schema in database/init.sql.

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
