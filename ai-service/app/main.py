from typing import Any

from fastapi import FastAPI

from .runtime import AI_TASKS, create_ai_task, create_training_job as enqueue_training_job, health_payload, infer
from .schemas import AiTaskRequest, InferenceRequest, TrainingRequest

app = FastAPI(title="AI Inspection Service", version="0.1.0")


@app.get("/health")
def health() -> dict[str, Any]:
    return health_payload()


@app.post("/infer")
def run_inference(payload: InferenceRequest) -> dict[str, Any]:
    return infer(payload)


@app.post("/ai-tasks")
def start_ai_task(payload: AiTaskRequest) -> dict[str, Any]:
    return create_ai_task(payload)


@app.get("/ai-tasks")
def ai_tasks() -> list[dict[str, Any]]:
    return AI_TASKS


@app.get("/datasets")
def datasets() -> list[dict[str, Any]]:
    return [
        {"id": "dataset-001", "name": "烟火负样本增强", "algorithm_code": "smoke_fire", "sample_count": 1284, "status": "labeling"},
        {"id": "dataset-002", "name": "围界闯入样本", "algorithm_code": "person_intrusion", "sample_count": 876, "status": "qc"},
    ]


@app.post("/training-jobs")
def create_training_job(payload: TrainingRequest) -> dict[str, Any]:
    return enqueue_training_job(payload)


@app.get("/training-jobs")
def training_jobs() -> list[dict[str, Any]]:
    return [
        {
            "id": "job-001",
            "name": "smoke_fire_v1.5",
            "status": "running",
            "progress": 62,
            "gpu": {"name": "single-gpu", "usage": 71, "memory": "9.8GB/16GB"},
            "metrics": {"map50": 0.948, "recall": 0.916},
        }
    ]


@app.get("/models")
def models() -> list[dict[str, Any]]:
    return [
        {"id": "model-001", "algorithm": "smoke_fire", "version": "v1.4.2", "status": "production", "metrics": {"precision": 0.948, "recall": 0.916}},
        {"id": "model-002", "algorithm": "person_intrusion", "version": "v1.2.0", "status": "canary", "metrics": {"precision": 0.961, "recall": 0.932}},
    ]
