# QA RUN 002：云端 Mock 联调环境冒烟验收

## 1. 执行信息

| 项目 | 内容 |
|---|---|
| 任务编号 | QA-P0-003 |
| 负责人 | 测试工程师 |
| 执行日期 | 2026-06-16 |
| 前端 | `http://114.67.114.201:5173` |
| 后端 | `http://114.67.114.201:8080/api` |
| AI 服务 | `http://114.67.114.201:8100` |
| 边缘模拟器 | `http://114.67.114.201:8200` |

稳定 Mock ID：

| 类型 | ID |
|---|---|
| camera | `ch-camera-a1` |
| drone | `ch-drone-001` |
| vehicle | `ch-vehicle-front` |

## 2. 前端页面冒烟

| 编号 | 页面 | 结果 | 观察 |
|---|---|---|---|
| UI-CLOUD-001 | 驾驶舱 | 通过 | 页面非白屏，KPI、设备健康、三线接入、告警和巡检计划可见 |
| UI-CLOUD-002 | 视频墙 | 通过 | 厂区摄像头、无人机、车载通道均可见，开流/AI 控制按钮可见 |
| UI-CLOUD-003 | 设备管理 | 通过 | 集成卡、测试/同步按钮、设备表、统一视频通道和车载终端可见 |
| UI-CLOUD-004 | 告警中心 | 通过 | 告警列表、证据 URL、来源、确认/误报/派单按钮可见 |
| UI-CLOUD-005 | 无人机巡检 | 通过 | 机场状态、无人机直播、航线、启动/终止按钮、遥测和飞行任务可见 |
| UI-CLOUD-006 | 报表统计 | 通过 | KPI、事件地图分布、巡检报告、车载轨迹、素材管理可见 |
| UI-CLOUD-007 | 系统配置 | 通过 | 登录权限、用户管理、角色管理、审计日志和系统参数可见 |

页面补充观察：

- 未发现白屏。
- 未捕获浏览器 `error` 级控制台日志。
- 未发现明显布局错乱、按钮挤压或表格明显溢出。
- 部分页面顶部显示 `Mock兜底` 或 `数据加载中`，但页面主体数据可见；本轮不判失败。

## 3. 中心后端 API 冒烟

基础地址：`http://114.67.114.201:8080/api`

| 编号 | 接口 | 结果 | 缺陷编号 | 观察 |
|---|---|---|---|---|
| API-CLOUD-001 | `GET /api/health` | 通过 | - | HTTP 200，返回 `inspection-backend` 健康状态 |
| API-CLOUD-002 | `GET /api/overview` | 通过 | - | HTTP 200，返回 KPI、告警、任务等汇总 |
| API-CLOUD-003 | `GET /api/integrations` | 通过 | - | HTTP 200，返回海康、大疆、JT1078 集成配置 |
| API-CLOUD-004 | `GET /api/video/channels` | 通过 | - | HTTP 200，返回 camera/drone/vehicle 通道；但后端通道 ID 为 UUID，不是任务指定稳定 Mock ID |
| API-CLOUD-005 | `POST /api/video/sessions` | 失败 | P1-QA-003-001 | 使用 `ch-camera-a1` 返回 HTTP 500；使用后端返回的 UUID 通道可成功 |
| API-CLOUD-006 | `POST /api/ai/tasks` | 失败 | P1-QA-003-001 | 使用 `ch-camera-a1` 返回 HTTP 500；使用后端返回的 UUID 通道可成功 |
| API-CLOUD-007 | `POST /api/edge/events` | 通过 | - | HTTP 200，事件被接收并返回 ID |
| API-CLOUD-008 | `GET /api/edge/events` | 通过 | - | HTTP 200，可查到刚上报的 QA 事件 |
| API-CLOUD-009 | `GET /api/vehicles` | 通过 | - | HTTP 200，返回车载终端列表 |
| API-CLOUD-010 | `GET /api/drones` | 通过 | - | HTTP 200，返回无人机摘要 |
| API-CLOUD-011 | `GET /api/system/audit-logs` | 通过 | - | HTTP 200，返回审计日志，包含边缘事件追加记录 |

缺陷定位补充：

- `POST /api/video/sessions` 使用稳定 ID `ch-camera-a1` 返回：
  `{"status":500,"error":"Internal Server Error","path":"/api/video/sessions"}`
- `POST /api/ai/tasks` 使用稳定 ID `ch-camera-a1` 返回：
  `{"status":500,"error":"Internal Server Error","path":"/api/ai/tasks"}`
- 同一接口改用 `GET /api/video/channels` 返回的在线 camera UUID `21000000-0000-0000-0000-000000000002` 后，两项均返回 HTTP 200。

## 4. AI 服务冒烟

基础地址：`http://114.67.114.201:8100`

| 编号 | 接口 | 结果 | 缺陷编号 | 观察 |
|---|---|---|---|---|
| AI-CLOUD-001 | `GET /health` | 通过 | - | HTTP 200，返回 `ai-service` 健康状态 |
| AI-CLOUD-002 | `POST /infer` | 通过 | - | HTTP 200，返回 `request_id`、`detections`、`latency_ms` |
| AI-CLOUD-003 | `GET /ai-tasks` | 通过 | - | HTTP 200，返回 `ch-camera-a1` 任务 |
| AI-CLOUD-004 | `GET /datasets` | 通过 | - | HTTP 200，返回数据集列表 |
| AI-CLOUD-005 | `GET /models` | 通过 | - | HTTP 200，返回模型列表 |

## 5. 边缘模拟器冒烟

基础地址：`http://114.67.114.201:8200`

| 编号 | 接口 | 结果 | 缺陷编号 | 观察 |
|---|---|---|---|---|
| EDGE-CLOUD-001 | `GET /health` | 通过 | - | HTTP 200，返回 `edge-simulator` 健康状态 |
| EDGE-CLOUD-002 | `GET /node/status` | 通过 | - | HTTP 200，返回节点、GPU、通道和缓存事件状态 |
| EDGE-CLOUD-003 | `GET /video/channels` | 通过 | - | HTTP 200，返回 `ch-camera-a1`、`ch-drone-001`、`ch-vehicle-front` |
| EDGE-CLOUD-004 | `GET /events/latest` | 通过 | - | HTTP 200，返回最新边缘事件 |

## 6. 缺陷列表

| 缺陷编号 | 等级 | 分类 | 标题 | 复现步骤 | 影响 | 建议责任 |
|---|---|---|---|---|---|---|
| P1-QA-003-001 | P1 | 接口/数据 | 后端云端通道 ID 与总指挥指定稳定 Mock ID 不一致，稳定 ID 创建视频会话和 AI 任务返回 500 | 1. `GET /api/video/channels` 观察返回 UUID 通道 ID；2. `POST /api/video/sessions` 使用 `ch-camera-a1`；3. `POST /api/ai/tasks` 使用 `ch-camera-a1` | 影响按稳定 Mock ID 执行自动化冒烟和三线联调；错误响应不利于前端展示 | 程序员 A |

P0 缺陷：无。

P1 缺陷：`P1-QA-003-001`。

P2 缺陷：无。

## 7. 统计

| 指标 | 数量 |
|---|---:|
| 已执行项 | 27 |
| 通过项 | 25 |
| 失败项 | 2 |
| 阻塞项 | 0 |
| 不适用项 | 0 |

## 8. 需要总指挥分派的问题

- 分派程序员 A 统一云端后端种子数据和总指挥指定稳定 Mock ID，或明确后端 API 是否应兼容 `ch-camera-a1`、`ch-drone-001`、`ch-vehicle-front`。
- 分派程序员 A 优化 `POST /api/video/sessions`、`POST /api/ai/tasks` 的错误响应：未知通道应返回 4xx 和明确错误信息，不应返回 500。
