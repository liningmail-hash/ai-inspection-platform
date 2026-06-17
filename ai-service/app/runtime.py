from datetime import datetime, timezone
from typing import Any
from uuid import uuid4

from .schemas import AiTaskRequest, InferenceRequest, TrainingRequest

SUPPORTED_ALGORITHMS = {
    "smoke_fire": {"label": "烟火识别", "severity": "high", "confidence": 0.91},
    "person_intrusion": {"label": "人员闯入", "severity": "medium", "confidence": 0.88},
    "helmet_detection": {"label": "安全帽识别", "severity": "medium", "confidence": 0.86},
    "vehicle_parking": {"label": "车辆异常", "severity": "medium", "confidence": 0.82},
}

AI_TASKS: list[dict[str, Any]] = [
    {
        "id": "ai-task-001",
        "source_type": "camera",
        "channel_id": "ch-camera-a1",
        "algorithm_code": "smoke_fire",
        "model_version": "v1.4.2",
        "status": "running",
        "confidence": 0.91,
        "evidence_url": "/evidence/alarm-001.jpg",
        "created_at": "2026-06-16T09:18:00+08:00",
    }
]


def health_payload() -> dict[str, Any]:
    return {"status": "UP", "service": "ai-service", "time": datetime.now(timezone.utc).isoformat()}


def infer(payload: InferenceRequest) -> dict[str, Any]:
    profile = SUPPORTED_ALGORITHMS.get(payload.algorithm_code, {"label": payload.algorithm_code, "severity": "low", "confidence": payload.threshold})
    confidence = max(float(payload.threshold), float(profile["confidence"]))
    return {
        "request_id": str(uuid4()),
        "algorithm_code": payload.algorithm_code,
        "detections": [
            {
                "label": profile["label"],
                "confidence": round(confidence, 3),
                "box": [0.32, 0.18, 0.58, 0.64],
                "severity": profile["severity"],
            }
        ],
        "latency_ms": 86,
        "runtime": "mock-onnx-runtime",
    }


def create_ai_task(payload: AiTaskRequest) -> dict[str, Any]:
    detection = infer(InferenceRequest(stream_url=payload.stream_url, algorithm_code=payload.algorithm_code, threshold=payload.threshold))
    confidence = detection["detections"][0]["confidence"]
    task = {
        "id": f"ai-task-{uuid4().hex[:8]}",
        "source_type": payload.source_type,
        "channel_id": payload.channel_id,
        "algorithm_code": payload.algorithm_code,
        "model_version": payload.model_version,
        "status": "running",
        "confidence": confidence,
        "evidence_url": f"/evidence/{payload.channel_id}-{payload.algorithm_code}.jpg",
        "created_at": datetime.now(timezone.utc).isoformat(),
        "last_inference": detection,
    }
    AI_TASKS.insert(0, task)
    return task


def create_training_job(payload: TrainingRequest) -> dict[str, Any]:
    return {
        "id": f"job-{uuid4().hex[:8]}",
        "name": f"{payload.algorithm_code}_training",
        "status": "queued",
        "progress": 0,
        "dataset_id": payload.dataset_id,
        "epochs": payload.epochs,
        "runtime": "queued-celery-compatible",
    }
