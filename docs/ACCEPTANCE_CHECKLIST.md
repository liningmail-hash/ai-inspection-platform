# 验收清单

本文档对应 `QA-P0-001` 的可执行验收清单和接口冒烟测试表。执行时在“结果”列填写 `通过`、`失败`、`阻塞` 或 `不适用`，失败项必须按 `docs/TEST_PLAN.md` 的缺陷模板记录。

## 1. 执行信息

| 项目 | 内容 |
|---|---|
| 任务编号 | QA-P0-001 |
| 负责人 | 测试工程师 |
| 验收版本 | 第一版三线验收清单 |
| 执行日期 | 待填写 |
| 前端地址 | `http://localhost:5173` |
| 后端地址 | `http://localhost:8080/api` |
| AI 服务地址 | `http://localhost:8100` |
| 边缘模拟器地址 | `http://localhost:8200` |

## 2. 构建与启动检查

| 编号 | 检查项 | 命令或入口 | 预期结果 | 结果 |
|---|---|---|---|---|
| ENV-001 | 前端依赖安装 | `cd frontend && npm install` | 依赖安装完成，无阻断错误 | 待执行 |
| ENV-002 | 前端构建 | `cd frontend && npm run build` | 构建通过 | 待执行 |
| ENV-003 | 后端测试 | `cd backend && mvn test` | 测试通过 | 待执行 |
| ENV-004 | 后端启动 | `cd backend && mvn spring-boot:run` | `/api/health` 可访问 | 待执行 |
| ENV-005 | AI 服务健康检查 | `GET http://localhost:8100/health` | 返回健康状态 | 待执行 |
| ENV-006 | 边缘模拟器健康检查 | `GET http://localhost:8200/health` | 返回健康状态 | 待执行 |
| ENV-007 | Docker 全栈启动 | `docker compose -f deploy/docker-compose.yml up -d --build` | 前端、后端、AI、边缘服务可访问 | 待执行 |

## 3. 三线端到端验收

### 3.1 厂区摄像头

| 编号 | 验收项 | 步骤摘要 | 预期结果 | 结果 |
|---|---|---|---|---|
| CAM-E2E-001 | 摄像头集成可见 | 登录后进入设备管理 | 显示摄像头集成、通道数、厂商和同步入口 | 待执行 |
| CAM-E2E-002 | 摄像头视频播放 | 在视频墙创建摄像头通道播放会话 | 返回播放地址，页面无白屏 | 待执行 |
| CAM-E2E-003 | 摄像头抓图 | 对摄像头通道执行抓图 | 返回证据对象 URL | 待执行 |
| CAM-E2E-004 | 摄像头 AI 任务 | 对摄像头通道创建 AI 分析任务 | 返回任务 ID 和状态 | 待执行 |
| CAM-E2E-005 | 摄像头告警处置 | 生成摄像头告警并处理 | 告警含证据，状态可闭环 | 待执行 |
| CAM-E2E-006 | 摄像头审计 | 查看审计日志 | 同步、播放、抓图、处置动作可追踪 | 待执行 |

### 3.2 无人机

| 编号 | 验收项 | 步骤摘要 | 预期结果 | 结果 |
|---|---|---|---|---|
| DRONE-E2E-001 | 机场状态可见 | 进入无人机巡检页 | 机场、无人机、遥测状态可见 | 待执行 |
| DRONE-E2E-002 | 航线任务可见 | 查看固定航线列表 | 航线、航点、风险等级和任务状态可见 | 待执行 |
| DRONE-E2E-003 | 飞行任务启动 | 启动飞行任务 | 返回成功或明确失败原因 | 待执行 |
| DRONE-E2E-004 | 无人机视频 AI | 对无人机通道创建播放会话和 AI 任务 | 任务绑定无人机通道 | 待执行 |
| DRONE-E2E-005 | 无人机告警处置 | 生成无人机巡检告警并处理 | 告警含航线、任务和证据信息 | 待执行 |
| DRONE-E2E-006 | 飞行任务停止审计 | 停止任务并查看审计 | 停止动作和结果可追踪 | 待执行 |

### 3.3 车载监控

| 编号 | 验收项 | 步骤摘要 | 预期结果 | 结果 |
|---|---|---|---|---|
| VEH-E2E-001 | 车载终端可见 | 进入设备管理或视频墙车载分组 | 车辆、终端、在线状态、通道数量可见 | 待执行 |
| VEH-E2E-002 | 车载视频播放 | 创建车辆通道播放会话 | 返回播放地址，按车载来源展示 | 待执行 |
| VEH-E2E-003 | 车辆轨迹查询 | 查询车辆轨迹点 | 返回时间、经纬度、速度等字段 | 待执行 |
| VEH-E2E-004 | 车载 AI 任务 | 对车载通道创建 AI 任务 | 任务绑定车辆通道 | 待执行 |
| VEH-E2E-005 | 车载告警处置 | 生成车载事件并处理 | 告警含车辆、通道和证据 | 待执行 |
| VEH-E2E-006 | 同步失败反馈 | 模拟 JT/T1078 同步失败 | 返回明确失败原因，不影响其他来源 | 待执行 |

## 4. 页面验收清单

| 编号 | 页面 | 验收点 | 结果 |
|---|---|---|---|
| UI-001 | 登录页 | 正确账号可登录，错误账号有提示 | 待执行 |
| UI-002 | 驾驶舱 | KPI、告警、设备状态和趋势可见 | 待执行 |
| UI-003 | 视频墙 | 厂区摄像头、无人机、车载监控三类来源可见 | 待执行 |
| UI-004 | AI 巡检 | 任务队列、即时任务创建和下发状态可见 | 待执行 |
| UI-005 | 告警中心 | 证据 URL、来源类型、处理按钮和状态反馈可见 | 待执行 |
| UI-006 | 无人机巡检 | 机场、无人机、航线、飞行任务和遥测可见 | 待执行 |
| UI-007 | 设备管理 | SDK 集成状态、同步按钮、通道数量、车载终端可见 | 待执行 |
| UI-008 | 报表统计 | 巡检报告、素材管理、事件分布数据可见 | 待执行 |
| UI-009 | 系统配置 | 用户、角色、审计日志和演示账号说明可见 | 待执行 |
| UI-010 | 响应式和布局 | 无文字重叠、按钮挤压、表格明显溢出 | 待执行 |

## 5. 中心后端 API 冒烟测试表

基础地址：`http://localhost:8080/api`

| 编号 | 方法 | 路径 | 请求体 | 预期结果 | 结果 |
|---|---|---|---|---|---|
| API-001 | GET | `/health` | 无 | 返回健康状态 | 待执行 |
| API-002 | POST | `/auth/login` | `{"username":"admin","password":"demo123"}` | 返回演示 token | 待执行 |
| API-003 | GET | `/auth/me` | Bearer Token | 返回当前用户 | 待执行 |
| API-004 | GET | `/overview` | 无 | 返回驾驶舱汇总 | 待执行 |
| API-005 | GET | `/devices` | 无 | 返回设备列表 | 待执行 |
| API-006 | GET | `/integrations` | 无 | 返回集成配置列表 | 待执行 |
| API-007 | POST | `/integrations/{id}/test` | 无或配置参数 | 返回连通性结果，不吞异常 | 待执行 |
| API-008 | POST | `/integrations/{id}/sync` | 无或同步参数 | 返回同步结果和通道数量 | 待执行 |
| API-009 | GET | `/video/channels` | 无 | 返回三类来源视频通道 | 待执行 |
| API-010 | POST | `/video/sessions` | `{"channelId":"ch-camera-a1","protocol":"webrtc"}` | 返回播放会话和播放地址 | 待执行 |
| API-011 | POST | `/video/channels/{id}/snapshot` | 无 | 返回证据对象 URL | 待执行 |
| API-012 | POST | `/video/channels/{id}/record` | 无或录像参数 | 返回录像对象 URL | 待执行 |
| API-013 | GET | `/ai/tasks` | 无 | 返回 AI 任务列表 | 待执行 |
| API-014 | POST | `/ai/tasks` | `{"channelId":"ch-camera-a1","algorithmCode":"smoke_fire","modelVersion":"production"}` | 返回任务 ID 和状态 | 待执行 |
| API-015 | GET | `/alarms` | 无 | 返回告警列表 | 待执行 |
| API-016 | PATCH | `/alarms/{id}/status` | `{"status":"processing"}` | 返回更新后的告警状态 | 待执行 |
| API-017 | GET | `/edge/events` | 无 | 返回边缘事件列表 | 待执行 |
| API-018 | POST | `/edge/events` | 边缘事件 JSON | 创建或接收边缘事件 | 待执行 |
| API-019 | POST | `/edge/heartbeat` | 心跳 JSON | 返回心跳接收结果 | 待执行 |
| API-020 | GET | `/drones` | 无 | 返回无人机摘要 | 待执行 |
| API-021 | GET | `/drone-docks` | 无 | 返回机场列表 | 待执行 |
| API-022 | GET | `/flight-routes` | 无 | 返回航线列表 | 待执行 |
| API-023 | GET | `/flight-tasks` | 无 | 返回飞行任务列表 | 待执行 |
| API-024 | POST | `/flight-tasks/{id}/start` | 无 | 返回启动结果 | 待执行 |
| API-025 | POST | `/flight-tasks/{id}/stop` | 无 | 返回停止结果 | 待执行 |
| API-026 | GET | `/vehicles` | 无 | 返回车辆列表 | 待执行 |
| API-027 | GET | `/vehicles/{id}/tracks` | 无 | 返回轨迹点列表 | 待执行 |
| API-028 | GET | `/vehicles/{id}/channels` | 无 | 返回车辆通道列表 | 待执行 |
| API-029 | POST | `/inspection-tasks` | `{"name":"即时巡检","type":"immediate","priority":"high"}` | 返回巡检任务 ID 和 `queued` 状态 | 待执行 |
| API-030 | GET | `/inspection-reports` | 无 | 返回巡检报告列表 | 待执行 |
| API-031 | GET | `/media-assets` | 无 | 返回素材列表 | 待执行 |
| API-032 | GET | `/system/audit-logs` | 无 | 返回审计日志 | 待执行 |

## 6. AI 服务冒烟测试表

基础地址：`http://localhost:8100`

| 编号 | 方法 | 路径 | 请求体 | 预期结果 | 结果 |
|---|---|---|---|---|---|
| AI-001 | GET | `/health` | 无 | 返回健康状态 | 待执行 |
| AI-002 | POST | `/infer` | 推理请求 JSON | 返回 `request_id`、`detections`、`latency_ms` | 待执行 |
| AI-003 | GET | `/datasets` | 无 | 返回数据集列表 | 待执行 |
| AI-004 | POST | `/training-jobs` | 训练任务 JSON | 返回训练任务 ID 和状态 | 待执行 |
| AI-005 | GET | `/training-jobs` | 无 | 返回训练任务列表 | 待执行 |
| AI-006 | GET | `/models` | 无 | 返回模型列表 | 待执行 |

## 7. 边缘模拟器冒烟测试表

基础地址：`http://localhost:8200`

| 编号 | 方法 | 路径 | 请求体 | 预期结果 | 结果 |
|---|---|---|---|---|---|
| EDGE-001 | GET | `/health` | 无 | 返回健康状态 | 待执行 |
| EDGE-002 | GET | `/node/status` | 无 | 返回节点、GPU、通道统计 | 待执行 |
| EDGE-003 | GET | `/devices` | 无 | 返回边缘设备状态 | 待执行 |
| EDGE-004 | POST | `/inference-tasks` | 推理任务 JSON | 返回任务 ID 和状态 | 待执行 |
| EDGE-005 | GET | `/events/latest` | 无 | 返回最新边缘事件 | 待执行 |

## 8. 通过标准

- 三条链路每条至少 5 个场景完成验收，且无 P0 缺陷。
- 登录、视频、AI、告警、审计、报告至少各有一个通过项。
- 中心后端核心 API 冒烟失败项不超过 P1，且失败原因已记录。
- 后端不可用时，前端 Mock 兜底仍能展示三线来源。
- 所有失败或阻塞项均有缺陷编号或阻塞说明。

## 9. 缺陷汇总表

| 缺陷编号 | 等级 | 链路 | 标题 | 状态 | 负责人 | 备注 |
|---|---|---|---|---|---|---|
| 待填写 | 待填写 | 待填写 | 待填写 | 新建 | 待分配 | 待填写 |

## 10. 需要程序员配合的数据或接口

- 提供可稳定返回的 Mock 通道 ID：摄像头、无人机、车载各至少 1 个。
- 提供可用于 `POST /video/sessions`、抓图、录像和 AI 任务的样例通道。
- 提供边缘事件上报样例，覆盖三类 `sourceType`。
- 提供告警状态流转样例和审计日志样例。
- 明确真实厂商适配器不可用时的错误码、错误文案和返回结构。

## QA-P0-002 第一轮 Mock 冒烟执行结果

详细记录见 `docs/QA_RUN_001.md`。

| 编号 | 检查项 | 结果 | 缺陷/阻塞编号 |
|---|---|---|---|
| QA2-UI-001 | 视频墙 | 通过 | - |
| QA2-UI-002 | 设备管理 | 通过 | - |
| QA2-UI-003 | 告警中心 | 通过 | - |
| QA2-UI-004 | 无人机巡检 | 通过 | - |
| QA2-UI-005 | 报表统计 | 通过 | - |
| QA2-API-001 | `GET /api/integrations` | 阻塞 | BLK-QA-001 |
| QA2-API-002 | `GET /api/video/channels` | 阻塞 | BLK-QA-001 |
| QA2-API-003 | `POST /api/video/sessions` | 阻塞 | BLK-QA-001 |
| QA2-API-004 | `GET /api/ai/tasks` | 阻塞 | BLK-QA-001 |
| QA2-API-005 | `GET /api/edge/events` | 阻塞 | BLK-QA-001 |
| QA2-API-006 | `GET /api/vehicles` | 阻塞 | BLK-QA-001 |
| QA2-API-007 | `GET /api/drones` | 阻塞 | BLK-QA-001 |
| QA2-API-008 | `GET /api/system/audit-logs` | 阻塞 | BLK-QA-001 |
| QA2-AI-001 | `GET /health` | 阻塞 | BLK-QA-002 |
| QA2-AI-002 | `POST /infer` | 阻塞 | BLK-QA-002 |
| QA2-AI-003 | `GET /ai-tasks` | 阻塞 | BLK-QA-002 |
| QA2-EDGE-001 | `GET /health` | 阻塞 | BLK-QA-003 |
| QA2-EDGE-002 | `GET /node/status` | 阻塞 | BLK-QA-003 |
| QA2-EDGE-003 | `GET /video/channels` | 阻塞 | BLK-QA-003 |
| QA2-EDGE-004 | `GET /events/latest` | 阻塞 | BLK-QA-003 |

统计：已执行 20 项，通过 5 项，失败 0 项，阻塞 15 项，不适用 0 项。

P0 缺陷：无。

P1 缺陷：无。

P2 缺陷：无。

## QA-P0-002-R1 恢复执行结果

详细记录见 `docs/QA_RUN_001.md`。

| 编号 | 检查项 | 结果 | 缺陷/阻塞编号 |
|---|---|---|---|
| QA2R1-ENV-001 | JDK 21 / JAVA_HOME | 阻塞 | BLK-QA-001 |
| QA2R1-ENV-002 | Python 3.12 | 阻塞 | BLK-QA-002 / BLK-QA-003 |
| QA2R1-ENV-003 | Node/npm | 通过 | - |
| QA2R1-ENV-004 | Docker | 不适用 | - |
| QA2R1-HC-001 | frontend `http://localhost:5173` | 通过 | - |
| QA2R1-HC-002 | backend `http://localhost:8080/api/health` | 阻塞 | BLK-QA-001 |
| QA2R1-HC-003 | ai-service `http://localhost:8100/health` | 阻塞 | BLK-QA-002 |
| QA2R1-HC-004 | edge-simulator `http://localhost:8200/health` | 阻塞 | BLK-QA-003 |

恢复结论：健康检查未全部通过，按任务单要求未继续执行中心后端 API、AI 服务和边缘模拟器业务接口冒烟。

## QA-P0-003 云端 Mock 联调环境冒烟结果

详细记录见 `docs/QA_RUN_002.md`。

| 编号 | 检查项 | 结果 | 缺陷编号 |
|---|---|---|---|
| QA3-UI-001 | 驾驶舱 | 通过 | - |
| QA3-UI-002 | 视频墙 | 通过 | - |
| QA3-UI-003 | 设备管理 | 通过 | - |
| QA3-UI-004 | 告警中心 | 通过 | - |
| QA3-UI-005 | 无人机巡检 | 通过 | - |
| QA3-UI-006 | 报表统计 | 通过 | - |
| QA3-UI-007 | 系统配置 | 通过 | - |
| QA3-API-001 | `GET /api/health` | 通过 | - |
| QA3-API-002 | `GET /api/overview` | 通过 | - |
| QA3-API-003 | `GET /api/integrations` | 通过 | - |
| QA3-API-004 | `GET /api/video/channels` | 通过 | - |
| QA3-API-005 | `POST /api/video/sessions` with `ch-camera-a1` | 失败 | P1-QA-003-001 |
| QA3-API-006 | `POST /api/ai/tasks` with `ch-camera-a1` | 失败 | P1-QA-003-001 |
| QA3-API-007 | `POST /api/edge/events` | 通过 | - |
| QA3-API-008 | `GET /api/edge/events` | 通过 | - |
| QA3-API-009 | `GET /api/vehicles` | 通过 | - |
| QA3-API-010 | `GET /api/drones` | 通过 | - |
| QA3-API-011 | `GET /api/system/audit-logs` | 通过 | - |
| QA3-AI-001 | `GET /health` | 通过 | - |
| QA3-AI-002 | `POST /infer` | 通过 | - |
| QA3-AI-003 | `GET /ai-tasks` | 通过 | - |
| QA3-AI-004 | `GET /datasets` | 通过 | - |
| QA3-AI-005 | `GET /models` | 通过 | - |
| QA3-EDGE-001 | `GET /health` | 通过 | - |
| QA3-EDGE-002 | `GET /node/status` | 通过 | - |
| QA3-EDGE-003 | `GET /video/channels` | 通过 | - |
| QA3-EDGE-004 | `GET /events/latest` | 通过 | - |

统计：已执行 27 项，通过 25 项，失败 2 项，阻塞 0 项，不适用 0 项。

P0 缺陷：无。

P1 缺陷：`P1-QA-003-001`。

P2 缺陷：无。

## QA-REAL-P0-001-R5 - 2026-06-16 21:05

- 云端设备管理页最终回归：通过。
- `P0-QA-REAL-001`：关闭。
- 页面不再显示可见 `Mock兜底`：通过。
- 主按钮 `接入设备`：通过。
- 新增真实设备且无 `Failed to fetch`：通过。
- 编辑后刷新保留：通过。
- 测试连接真实反馈：通过。
- 同步通道真实反馈：通过。
- 审计日志含新增、编辑、测试、同步：通过。
- 非阻断观察：删除目标行的页面确认/反馈建议后续单独复核。
