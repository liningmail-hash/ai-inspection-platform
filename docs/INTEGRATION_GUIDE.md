# 联调指南

## 当前策略

前端采用“API 优先、Mock 兜底”策略：

- 后端启动时，前端优先请求 `VITE_API_BASE_URL`。
- 后端不可用时，前端自动回退到本地 Mock 数据。
- 顶部状态栏会显示 `API联调` 或 `Mock兜底`。

## 前端环境变量

在 `frontend/.env.local` 中可配置：

```text
VITE_API_BASE_URL=http://localhost:8080
VITE_AI_SERVICE_URL=http://localhost:8100
VITE_EDGE_SERVICE_URL=http://localhost:8200
```

如果不配置，默认：

```text
VITE_API_BASE_URL=http://localhost:8080
```

## 联调顺序

1. 启动后端：

```powershell
cd backend
mvn spring-boot:run
```

2. 启动前端：

```powershell
cd frontend
npm run dev
```

3. 打开前端：

```text
http://localhost:5173
```

4. 查看顶部状态：

- `API联调`：前端已读取后端 API。
- `Mock兜底`：后端不可用，前端使用本地数据。

## 当前已接入 API

- `/api/overview`
- `/api/devices`
- `/api/models`
- `PATCH /api/alarms/{id}/status`
- `POST /api/models/{id}/publish`

## 下一批联调目标

- 训练任务创建：后端转发或编排 AI 服务 `/training-jobs`
- 边缘节点状态：后端聚合边缘模拟器 `/node/status`

## 联调原则

- 前端页面不直接访问 AI 服务和边缘节点，默认通过中心后端聚合。
- 只有调试工具页可以直连 AI 服务或边缘模拟器。
- 后端接口字段调整必须同步更新 `docs/API_CONTRACTS.md`。
- Mock 数据结构要贴近 API 返回结构，避免后续切换成本过高。
