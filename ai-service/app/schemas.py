from typing import Any

from pydantic import BaseModel, Field


class InferenceRequest(BaseModel):
    stream_url: str
    algorithm_code: str
    roi: list[dict[str, Any]] = Field(default_factory=list)
    threshold: float = 0.72


class TrainingRequest(BaseModel):
    dataset_id: str
    algorithm_code: str
    base_model_version: str | None = None
    epochs: int = 50


class AiTaskRequest(BaseModel):
    source_type: str = "camera"
    channel_id: str
    algorithm_code: str
    model_version: str = "production"
    stream_url: str
    threshold: float = 0.72
