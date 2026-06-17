from datetime import datetime, timezone
from typing import Any
from uuid import uuid4

from pydantic import BaseModel


class TaskRequest(BaseModel):
    plan_id: str
    channel_ids: list[str]
    algorithm_code: str
    model_version: str


class EdgeHeartbeat(BaseModel):
    node_id: str = "edge-001"
    status: str = "online"
    online_channels: int = 5
    ai_running: int = 3
    cached_events: int = 0


class EdgeEventRequest(BaseModel):
    event_type: str = "ai_alarm"
    source_type: str = "camera"
    source_id: str
    severity: str = "medium"
    title: str
    latitude: float = 31.2312
    longitude: float = 121.4741
    evidence_url: str = "/evidence/edge-event.jpg"


def now() -> str:
    return datetime.now(timezone.utc).isoformat()


def event_from_payload(payload: EdgeEventRequest) -> dict[str, Any]:
    return {
        "id": f"evt-{uuid4().hex[:8]}",
        "event_type": payload.event_type,
        "source_type": payload.source_type,
        "source_id": payload.source_id,
        "severity": payload.severity,
        "title": payload.title,
        "latitude": payload.latitude,
        "longitude": payload.longitude,
        "evidence_url": payload.evidence_url,
        "detected_at": now(),
        "status": "cached",
    }
