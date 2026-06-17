from random import randint
from typing import Any
from uuid import uuid4

from fastapi import FastAPI

from .protocol import EdgeEventRequest, EdgeHeartbeat, TaskRequest, event_from_payload, now

app = FastAPI(title="Edge Node Simulator", version="0.1.0")

EDGE_EVENTS: list[dict[str, Any]] = [
    {
        "id": "evt-001",
        "event_type": "ai_alarm",
        "source_type": "camera",
        "source_id": "ch-camera-a1",
        "severity": "high",
        "title": "A区危化仓烟火识别",
        "latitude": 31.2312,
        "longitude": 121.4741,
        "evidence_url": "minio://evidence/alarm-001.jpg",
        "detected_at": now(),
        "status": "uploaded",
    }
]


@app.get("/health")
def health() -> dict[str, Any]:
    return {"status": "UP", "service": "edge-simulator", "time": now()}


@app.get("/node/status")
def node_status() -> dict[str, Any]:
    return {
        "node_id": "edge-001",
        "status": "online",
        "gpu": {"usage": randint(48, 74), "memory": "8.6GB/16GB", "temperature": randint(58, 72)},
        "channels": {"online": 126, "offline": 12, "ai_running": 16},
        "cached_events": 0,
    }


@app.post("/edge/heartbeat")
def heartbeat(payload: EdgeHeartbeat) -> dict[str, Any]:
    return {
        "node_id": payload.node_id,
        "status": payload.status,
        "gpu": {"usage": randint(48, 74), "memory": "8.6GB/16GB", "temperature": randint(58, 72)},
        "channels": {"online": payload.online_channels, "offline": 1, "ai_running": payload.ai_running},
        "cached_events": payload.cached_events,
        "reported_at": now(),
    }


@app.get("/devices")
def devices() -> list[dict[str, Any]]:
    return [
        {"id": "dev-001", "name": "A区危化仓-枪机01", "protocol": "GB28181", "status": "online", "bitrate": "3.8Mbps", "latency_ms": 86},
        {"id": "dev-002", "name": "B区围界-球机03", "protocol": "ONVIF", "status": "online", "bitrate": "2.4Mbps", "latency_ms": 92},
        {"id": "dev-004", "name": "D区停车场-枪机06", "protocol": "JT/T1078", "status": "offline", "bitrate": None, "latency_ms": None},
    ]


@app.get("/video/channels")
def video_channels() -> list[dict[str, Any]]:
    return [
        {"id": "ch-camera-a1", "source_type": "camera", "source_name": "A区危化仓-枪机01", "protocol": "HIK_SDK", "play_url": "http://localhost:8088/live/camera-a1.flv", "status": "online", "ai_enabled": True},
        {"id": "ch-drone-001", "source_type": "drone", "source_name": "一号机场无人机", "protocol": "DJI_SDK", "play_url": "http://localhost:8088/live/drone-001.flv", "status": "online", "ai_enabled": True},
        {"id": "ch-vehicle-front", "source_type": "vehicle", "source_name": "危化品转运车01", "protocol": "JT1078", "play_url": "http://localhost:8088/live/vehicle-front.flv", "status": "online", "ai_enabled": True},
    ]


@app.post("/inference-tasks")
def create_inference_task(payload: TaskRequest) -> dict[str, Any]:
    return {
        "task_id": f"edge-task-{uuid4().hex[:8]}",
        "status": "running",
        "plan_id": payload.plan_id,
        "channels": payload.channel_ids,
        "algorithm_code": payload.algorithm_code,
        "model_version": payload.model_version,
    }


@app.post("/ai/tasks")
def create_ai_task(payload: TaskRequest) -> dict[str, Any]:
    return {
        "task_id": f"edge-ai-{uuid4().hex[:8]}",
        "status": "running",
        "plan_id": payload.plan_id,
        "channels": payload.channel_ids,
        "algorithm_code": payload.algorithm_code,
        "model_version": payload.model_version,
        "started_at": now(),
    }


@app.get("/events/latest")
def latest_events() -> list[dict[str, Any]]:
    return EDGE_EVENTS


@app.post("/events")
def append_event(payload: EdgeEventRequest) -> dict[str, Any]:
    event = event_from_payload(payload)
    EDGE_EVENTS.insert(0, event)
    return event
