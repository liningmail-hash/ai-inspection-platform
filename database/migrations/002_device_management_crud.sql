ALTER TABLE devices ADD COLUMN IF NOT EXISTS source_type VARCHAR(32) NOT NULL DEFAULT 'camera';
ALTER TABLE devices ADD COLUMN IF NOT EXISTS endpoint TEXT NOT NULL DEFAULT '';
ALTER TABLE devices ADD COLUMN IF NOT EXISTS credential_ref TEXT NOT NULL DEFAULT '';
ALTER TABLE devices ADD COLUMN IF NOT EXISTS location VARCHAR(255) NOT NULL DEFAULT '';
ALTER TABLE devices ADD COLUMN IF NOT EXISTS edge_node_ref VARCHAR(120) NOT NULL DEFAULT 'EDGE-01';
ALTER TABLE devices ADD COLUMN IF NOT EXISTS updated_at TIMESTAMPTZ NOT NULL DEFAULT now();
ALTER TABLE devices ADD COLUMN IF NOT EXISTS deleted_at TIMESTAMPTZ;

UPDATE devices
SET endpoint = coalesce(nullif(endpoint, ''), stream_url, ''),
    credential_ref = coalesce(nullif(credential_ref, ''), 'secret://integrations/demo'),
    location = coalesce(nullif(location, ''), 'Demo site'),
    edge_node_ref = coalesce(nullif(edge_node_ref, ''), 'EDGE-01'),
    updated_at = coalesce(updated_at, created_at)
WHERE deleted_at IS NULL;
