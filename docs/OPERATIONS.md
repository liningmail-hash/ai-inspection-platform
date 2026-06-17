# 运维与部署

## 部署形态

一期采用私有化单园区部署：

- 中心服务部署在园区服务器或私有云。
- 边缘节点部署在靠近摄像头和无人机视频流的位置。
- 视频默认不出域，中心平台主要保存结构化事件和证据文件。

## 服务端口

| 服务 | 默认端口 |
|---|---|
| 前端 | 5173 / 80 |
| 后端 | 8080 |
| AI 服务 | 8100 |
| 边缘模拟器 | 8200 |
| PostgreSQL | 5432 |
| Redis | 6379 |
| MinIO API | 9000 |
| MinIO Console | 9001 |

## QA 环境启动

QA 冒烟环境优先参考 [QA 环境安装与启动说明](QA_ENV_SETUP.md)。

本地多进程启动：

```powershell
.\deploy\qa-start\start-local.ps1
.\deploy\qa-start\health-check.ps1
```

Docker Compose 启动：

```powershell
docker compose -f deploy\docker-compose.yml up -d --build
.\deploy\qa-start\health-check.ps1
```

如果 Docker 不可用，使用本地多进程启动，不影响前端、后端、AI 服务、边缘模拟器的接口冒烟。

## 配置管理

- 本地配置使用 `.env`，不要提交到 Git。
- 示例配置在 `deploy/.env.example`。
- 生产环境必须修改默认密码。
- 设备密码、SIP 密码、厂商 SDK 密钥必须加密存储。
- Docker Compose 默认给后端启用 `postgres` Profile，读取 PostgreSQL 数据。
- 本地直接运行后端默认使用内存仓储，便于无数据库开发。

## 日志建议

后续正式实现时建议统一：

- 后端：JSON 日志，包含 trace_id、user_id、site_id、event_id。
- AI 服务：记录 request_id、model_version、latency_ms、gpu_usage。
- 边缘节点：记录 device_id、channel_id、task_id、network_status。
- 告警链路：记录从边缘生成到前端处置的完整时间线。

## 监控指标

一期至少需要：

- 服务健康：后端、AI服务、边缘节点、数据库、Redis、MinIO。
- 视频状态：在线通道数、离线通道数、拉流失败数。
- AI 状态：推理路数、平均延迟、GPU 使用率、显存使用率。
- 告警状态：新增、处理中、误报、关闭、超时未处置。
- 训练状态：队列长度、运行中任务、失败任务、模型指标。

## 备份策略

- PostgreSQL：每日备份，保留 7-30 天。
- MinIO：证据文件和训练样本按项目要求保留。
- 模型产物：生产版本不得被自动清理。
- 配置文件：部署参数、设备清单、算法配置需要版本化备份。

## 安全基线

- 生产环境禁用默认密码。
- 前端只访问后端 API，不直接访问数据库。
- 所有外部 Webhook 必须有签名或 Token。
- 告警证据、训练样本、模型文件不得公开暴露。
- GitHub 公开前必须做脱敏检查。

## 升级策略

- 先在测试环境升级。
- 后端接口保持向后兼容。
- 模型发布支持灰度和回滚。
- 数据库变更必须通过迁移脚本。
- 边缘节点升级要支持断点恢复和任务重试。
