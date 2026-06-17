# ADR 0004：AI 服务独立为 Python FastAPI

## 状态

Accepted

## 背景

AI 训练和推理生态主要集中在 Python，包括 PyTorch、ONNX Runtime、TensorRT、OpenCV、标注转换工具等。

## 决策

AI 能力独立为 Python FastAPI 服务，不直接写在 Java 后端内。

## 理由

- AI 生态兼容性更好。
- 推理和训练可以独立扩缩容。
- Java 后端保持业务编排职责，避免混入模型框架细节。
- 后续可以把训练任务迁移到 GPU 队列或 Kubernetes Job。

## 影响

- 中心后端和 AI 服务之间需要稳定 API。
- 长训练任务必须异步化。
- 模型版本、指标、产物地址必须结构化管理。
