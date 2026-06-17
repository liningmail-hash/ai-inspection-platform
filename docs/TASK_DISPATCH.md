# 总指挥任务分派板

更新时间：2026-06-16 21:20

本文档是团队领取任务的唯一入口。项目负责人只需要通知团队：“总指挥已更新任务分派板，请到 `docs/TASK_DISPATCH.md` 查看自己的任务，到 `docs/EXECUTION_COMMANDS.md` 查看执行命令，完成或阻塞后把回报追加到 `docs/TASK_FEEDBACK.md`。”

## 1. 当前总目标

当前阶段：P0-REAL，真实业务可操作闭环开发。

总目标不是继续打磨演示环境，而是把系统推进为可真实使用的生产安全园区 AI 巡检系统。

第一阶段验收链路：

```text
新增设备 -> 保存成功 -> 同步通道 -> 视频墙选择通道 -> 创建播放会话 -> 创建 AI 任务 -> 上报告警 -> 告警处置 -> 审计可查
```

Mock 只允许作为无真实设备时的联调兜底，不允许替代真实业务功能。

## 2. 本轮任务状态

| 任务编号 | 负责人 | 状态 | 总指挥结论 |
|---|---|---|---|
| BE-REAL-P0-001 | 程序员 A | 完成 | 已由 BE-REAL-P0-002 形成云端闭环，关闭 |
| FE-REAL-P0-001 | 程序员 B | 完成 | 原始前端设备管理能力已交付，后续修正转 FE-REAL-P0-001-R1 |
| SP-REAL-P0-001 | 机动人员 | 完成 | 阶段通过，真实海康资料仍需项目负责人协调现场补齐 |
| QA-P0-003 | 测试工程师 | 完成 | 云端 Mock 冒烟主体通过，遗留 P1-QA-003-001 转后端修复 |
| BE-REAL-P0-002 | 程序员 A | 完成 | 云端设备 CRUD、稳定 Mock ID、404 错误模型、审计日志已复核通过 |
| SP-REAL-P0-002 | 机动人员 | 部分完成 | 海康资料已整理，但现场真实参数仍缺，不具备单路真实视频试点条件 |
| SP-REAL-P0-003 | 机动人员 | 完成 | 云端 backend 已重新构建部署，公网接口复核通过 |
| FE-REAL-P0-001-R1 | 程序员 B / 机动人员 | 完成 | 前端真实 CRUD 页已修复并完成云端重部署，等待 QA 最终回归 |
| QA-REAL-P0-001 | 测试工程师 | 部分完成 | 接口层通过，页面层需对新前端版本做最终回归 |
| FE-REAL-P0-001-R2 | 程序员 B | 完成 | 云端部署完成，设备管理页真实提交链路已打通 |
| QA-REAL-P0-001-R5 | 测试工程师 | 完成 | 设备管理页 P0 已关闭，允许进入下一阶段 |

## 3. 新任务分派

### BE-REAL-P0-002：后端真实业务闭环收口

负责人：程序员 A

优先级：P0

状态：等待程序员 A 做云端复核回填

任务目标：

- 关闭 BE-REAL-P0-001 的未完成项。
- 修复 QA-P0-003 遗留的 `P1-QA-003-001`。
- 让设备管理、稳定 Mock 通道、视频会话、AI 任务在云端 PostgreSQL 环境下形成可验收闭环。

允许修改：

- `backend/`
- `database/`
- `docs/API_CONTRACTS.md`

禁止修改：

- `frontend/`
- `ai-service/`
- `edge-simulator/`
- 除必要数据库部署说明外，不修改部署拓扑。

已完成：

1. 本地已设置 JDK 21 `JAVA_HOME` 并执行 Maven Wrapper。
2. `.\mvnw.cmd test` 通过，BUILD SUCCESS。
3. 本地已验证 `POST /api/video/sessions` with `ch-camera-a1` 成功。
4. 本地已验证 `POST /api/ai/tasks` with `ch-camera-a1` 成功。
5. 本地未知 channelId 已返回 404，message 为 `Video channel not found: unknown-channel-id`。
6. 本地已验证审计日志写入：`DEVICE_CREATE`、`DEVICE_TEST`、`DEVICE_SYNC_CHANNELS`、`DEVICE_DELETE`。

历史阻塞：

1. 云端仍未部署新后端。
2. 云端 `POST /api/devices` 返回 405 Method Not Allowed，说明线上仍是旧版本。
3. 云端 `POST /api/video/sessions` with `ch-camera-a1` 仍返回 500。
4. 云端 `POST /api/ai/tasks` with `ch-camera-a1` 仍返回 500。
5. 程序员 A 当前机器无法 SSH：`deploy@114.67.114.201: Permission denied (publickey)`。

剩余必须完成：

1. 程序员 A 复核 `GET /api/devices` 返回字段包含前端需要的：
   - `name`
   - `sourceType`
   - `vendor`
   - `protocol`
   - `endpoint`
   - `credentialRef`
   - `location`
   - `edgeNodeId`
   - `status`
   - `streamUrl`
   - `createdAt`
   - `updatedAt`
2. 云端复核 `POST /api/devices`、`PUT /api/devices/{id}` 返回完整设备对象。
3. 云端复核 `POST /api/devices/{id}/test`、`POST /api/devices/{id}/sync-channels` 返回统一 `success/message`。
4. 云端 PostgreSQL profile 下验证设备新增、编辑、删除、测试、同步通道真实入库。
5. 云端复核稳定 Mock ID，后端必须兼容：
   - `ch-camera-a1`
   - `ch-drone-001`
   - `ch-vehicle-front`
6. 云端 `POST /api/video/sessions` 使用 `ch-camera-a1` 必须成功。
7. 云端 `POST /api/ai/tasks` 使用 `ch-camera-a1` 必须成功。
8. 云端未知 channelId 必须返回 400 或 404，并给出明确错误信息，不允许返回 500。
9. 云端新增、编辑、删除、测试、同步通道必须写审计日志。

建议实现口径：

- 数据库内部可以继续使用 UUID。
- REST/API 层和 QA 验收必须兼容稳定 Mock ID。
- 可以给 `video_channels` 增加稳定 ID/别名字段，或在 repository/service 层做稳定 ID 到内部 UUID 的解析。
- 不建议把 `video_channels.id` 主键整体从 UUID 改成字符串，避免扩大影响面。

必须执行命令：

- 见 `docs/EXECUTION_COMMANDS.md` 的 `BE-REAL-P0-002` 章节。

完成后回报格式：

```text
任务编号：BE-REAL-P0-002
当前状态：完成 / 部分完成 / 阻塞
修改文件：
数据库变化：
设备 CRUD 云端 PostgreSQL 验证结果：
稳定 Mock ID 兼容结果：
未知 channelId 错误响应示例：
审计日志验证结果：
Maven/编译测试结果：
是否已部署到云端：
需要前端 B 调整：
需要 QA 回归：
发现的问题：
```

### FE-REAL-P0-001-R1：前端设备管理联调修正

负责人：程序员 B

优先级：P0

状态：允许启动

任务目标：

- 根据后端最终字段和 QA 结果，修正设备管理真实表单的联调问题。

当前不立即开工。等待条件：

- 程序员 A 完成 BE-REAL-P0-002。
- QA 或总指挥确认前端存在字段、交互或错误展示问题。

允许修改：

- `frontend/`

禁止修改：

- `backend/`
- `database/`
- `deploy/`
- `ai-service/`
- `edge-simulator/`

必须执行命令：

- 见 `docs/EXECUTION_COMMANDS.md` 的 `FE-REAL-P0-001-R1` 章节。

完成后回报格式：

```text
任务编号：FE-REAL-P0-001-R1
当前状态：
修改文件：
修正的问题：
接口字段对齐情况：
npm run build 结果：
需要 QA 回归：
发现的问题：
```

### BE-REAL-P0-003：真实视频与告警闭环后端接线

负责人：程序员 A

优先级：P0

状态：立即执行

任务目标：

- 把真实视频会话、AI 任务、告警事件、证据留存、审计串成后端闭环。
- 为厂区摄像头、无人机、车载三线接入预留统一事件出口。

允许修改：

- `backend/`
- `database/`
- `docs/API_CONTRACTS.md`

禁止修改：

- `frontend/`
- `ai-service/`
- `edge-simulator/`
- 不改真实设备 SDK 资料文档

必须完成：

1. 梳理视频会话、AI 任务、告警事件的真实后端数据流。
2. 确认 `/api/video/sessions`、`/api/ai/tasks` 能承接后续真实视频流。
3. 增加或补齐告警事件关联字段，使告警可回溯到设备、通道、证据和处置人。
4. 统一告警状态流转：待确认、已确认、误报、已派单、已关闭。
5. 为证据图片/短视频/抓图结果预留入库字段和接口契约。
6. 保证审计日志覆盖告警创建、确认、误报、派单、关闭。
7. 本地执行 `.\mvnw.cmd test`。
8. 如需调整接口契约，更新 `docs/API_CONTRACTS.md`。

必须执行命令：

- 见 `docs/EXECUTION_COMMANDS.md` 的 `BE-REAL-P0-003` 章节。

完成后回报格式：

```text
任务编号：BE-REAL-P0-003
当前状态：完成 / 部分完成 / 阻塞
修改文件：
接口/字段变化：
告警闭环设计：
证据字段设计：
mvn test 结果：
需要前端 B 调整：
需要 QA 回归：
发现的问题：
```

### FE-REAL-P0-002：前端真实视频与告警联动

负责人：程序员 B

优先级：P0

状态：待执行

任务目标：

- 把视频墙、告警中心、无人机页接到真实后端闭环。
- 让前端不再只停留在设备管理页，而是能看见真实视频、真实告警和真实处置状态。

允许修改：

- `frontend/`

禁止修改：

- `backend/`
- `database/`
- `deploy/`
- `ai-service/`
- `edge-simulator/`

必须完成：

1. 视频墙接入真实播放/预览入口。
2. 告警中心接入真实告警列表、证据与处置状态。
3. 无人机页和车载页能展示后续真实接入位置和任务状态。
4. 保留页面反馈，但去掉演示感文案。
5. 本地执行 `npm run build`。
6. 如后端契约变化，按 `docs/API_CONTRACTS.md` 同步前端字段。

必须执行命令：

- 见 `docs/EXECUTION_COMMANDS.md` 的 `FE-REAL-P0-002` 章节。

完成后回报格式：

```text
任务编号：FE-REAL-P0-002
当前状态：完成 / 部分完成 / 阻塞
修改文件：
接入页面：
真实数据源：
npm run build 结果：
需要 QA 回归：
发现的问题：
```

### SP-REAL-P0-004：真实视频试点资料补齐与部署准备

负责人：机动人员

优先级：P0

状态：待执行

任务目标：

- 把真实摄像头/平台网关/无人机机场/车载网关的现场资料继续补齐。
- 为后续单路真实视频试点准备最小开工包。

允许修改：

- `docs/REAL_DEVICE_ACCESS_HIKVISION.md`
- `docs/DEVICE_SDK_REQUIREMENTS.md`
- `docs/REAL_DEVICE_ACCESS_DJI_DOCK.md`
- `docs/REAL_DEVICE_ACCESS_JT1078.md`

禁止修改：

- `backend/`
- `frontend/`
- `database/`
- `deploy/`
- `ai-service/`
- `edge-simulator/`

必须完成：

1. 确认 HIKVISION / DAHUA / DJI_DOCK / JT1078 的真实接入优先级。
2. 补齐至少 1 条可试点的真实通道资料。
3. 补齐 endpoint、端口、账号、credentialRef、权限范围。
4. 明确视频输出方式和事件订阅方式。
5. 记录是否具备单路真实视频试点条件。

必须执行命令：

- 仅文档任务，无强制构建命令。

完成后回报格式：

```text
任务编号：SP-REAL-P0-004
当前状态：完成 / 部分完成 / 阻塞
已确认接入模式：
已确认平台：
已确认真实通道数量：
仍缺参数：
是否具备单路试点条件：
更新文档：
发现的问题：
```

### QA-REAL-P0-002：真实视频与告警闭环验收

负责人：测试工程师

优先级：P0

状态：待执行

任务目标：

- 验证真实视频、AI 任务、告警事件、处置闭环是否跑通。

启动条件：

- BE-REAL-P0-003 完成。
- FE-REAL-P0-002 完成或明确无阻断。
- 至少具备一条可验收的真实或准真实通道。

必须测试：

1. 真实或准真实视频会话创建成功。
2. AI 任务创建成功且有真实/准真实结果。
3. 告警进入告警中心。
4. 告警可确认、误报、派单、关闭。
5. 证据可查。
6. 审计日志可查。

必须执行命令：

- 见 `docs/EXECUTION_COMMANDS.md` 的 `QA-REAL-P0-002` 章节。

完成后回报格式：

```text
任务编号：QA-REAL-P0-002
当前状态：完成 / 部分完成 / 阻塞
执行项数量：
通过数量：
失败数量：
阻塞数量：
P0 缺陷：
P1 缺陷：
P2 缺陷：
真实视频验证结果：
AI 任务验证结果：
告警闭环验证结果：
更新的测试记录文档：
需要总指挥分派：
```

### FE-REAL-P0-001-R2：前端真实提交链路修复

负责人：程序员 B

优先级：P0

状态：立即执行

任务目标：

- 修复云端设备管理页新增设备时的 `Failed to fetch`。
- 清理页面残留的 `Mock兜底` 和“添加设备”旧文案。
- 让设备管理页前端真正连到云端后端，而不是继续走浏览器本机或错误兜底路径。

允许修改：

- `frontend/`
- 必要时可补充 `deploy/DEPLOYMENT_RECORD_FE-REAL-P0-001-R2.md`

禁止修改：

- `backend/`
- `database/`
- `ai-service/`
- `edge-simulator/`
- 不改后端 REST 契约

必须完成：

1. 排查云端前端新增设备为何报 `Failed to fetch`。
2. 重点检查前端 API base 推导逻辑，确认在云端访问 `http://114.67.114.201:5173` 时，请求目标不是浏览器本机 `localhost`。
3. 修复新增设备提交链路，确保页面新增成功后后端 `/api/devices` 可查到新记录。
4. 清理设备管理页残留 `Mock兜底` 文案。
5. 清理主按钮旧文案“添加设备”，统一为“接入设备”。
6. 本地执行 `npm run build`。
7. 将修复后的前端重新部署到云端测试环境。
8. 回填部署结果与验证结果。

必须执行命令：

- 见 `docs/EXECUTION_COMMANDS.md` 的 `FE-REAL-P0-001-R2` 章节。

完成后回报格式：

```text
任务编号：FE-REAL-P0-001-R2
当前状态：完成 / 部分完成 / 阻塞
修改文件：
修复的问题：
Failed to fetch 根因：
API base 修复结果：
页面文案修复结果：
npm run build 结果：
云端部署结果：
是否需要 QA 回归：
发现的问题：
```

### QA-REAL-P0-001：设备管理真实 CRUD 验收

负责人：测试工程师

优先级：P0

状态：等待 BE-REAL-P0-002 完成后启动

任务目标：

- 验证设备管理是否从“能看”变成“能用”。

启动条件：

- SP-REAL-P0-003 已完成。
- 总指挥公网复核已通过。
- FE-REAL-P0-001 当前完成状态保持有效。

必须测试：

1. 新增 camera 设备，刷新后仍存在。
2. 新增 drone 设备，刷新后仍存在。
3. 新增 vehicle 设备，刷新后仍存在。
4. 编辑设备名称、endpoint、location，刷新后仍保留。
5. 删除设备，列表状态正确。
6. 测试连接返回明确 success/failed 和 message。
7. 同步通道后 `GET /api/video/channels` 结果发生合理变化。
8. 审计日志包含新增、编辑、删除、测试、同步。
9. `POST /api/video/sessions` 使用 `ch-camera-a1` 成功。
10. `POST /api/ai/tasks` 使用 `ch-camera-a1` 成功。
11. 未知 channelId 返回 400/404，不返回 500。

必须执行命令：

- 见 `docs/EXECUTION_COMMANDS.md` 的 `QA-REAL-P0-001` 章节。

完成后回报格式：

```text
任务编号：QA-REAL-P0-001
当前状态：完成 / 部分完成 / 阻塞
执行项数量：
通过数量：
失败数量：
阻塞数量：
P0 缺陷：
P1 缺陷：
P2 缺陷：
真实保存验证结果：
审计日志验证结果：
稳定 Mock ID 回归结果：
更新的测试记录文档：
需要总指挥分派：
```

### QA-REAL-P0-001-R4：设备管理页最终回归关单

负责人：测试工程师

优先级：P0

状态：立即执行

任务目标：

- 确认云端 `http://114.67.114.201:5173` 已切换到新前端版本。
- 正式关闭此前的 P0 缺陷 `P0-QA-REAL-001`，确认设备管理页已经从“演示页”变成“可操作页”。

允许修改：

- `docs/QA_RUN_003.md`
- `docs/ACCEPTANCE_CHECKLIST.md`
- `docs/TASK_FEEDBACK.md`

禁止修改：

- `backend/`
- `frontend/`
- `database/`
- `deploy/`
- `ai-service/`
- `edge-simulator/`

必须完成：

1. 打开云端前端 `http://114.67.114.201:5173`，进入设备管理页。
2. 确认页面顶部不再显示 `Mock兜底` 文案。
3. 点击“接入设备”，确认出现真实新增表单。
4. 确认页面存在编辑入口、删除入口、测试连接入口、同步通道入口。
5. 用页面完成至少 1 次真实新增、1 次编辑、1 次删除。
6. 用页面触发至少 1 次测试连接、1 次同步通道，并确认页面有真实成功/失败反馈。
7. 刷新页面后，确认新增/编辑结果仍通过 API 数据保留。
8. 如全部通过，关闭 `P0-QA-REAL-001`；如仍失败，明确失败页面、按钮、提示文案、浏览器行为。

必须执行命令：

- 见 `docs/EXECUTION_COMMANDS.md` 的 `QA-REAL-P0-001-R4` 章节。

完成后回报格式：

```text
任务编号：QA-REAL-P0-001-R4
当前状态：完成 / 部分完成 / 阻塞
执行项数量：
通过数量：
失败数量：
阻塞数量：
P0 缺陷：
P1 缺陷：
P2 缺陷：
页面是否已切到新前端版本：
P0-QA-REAL-001 是否关闭：
更新的测试记录文档：
发现的问题：
需要总指挥分派：
```

### SP-REAL-P0-002：真实海康单路试点资料补齐

负责人：机动人员

优先级：P0

状态：待执行

任务目标：

- 继续推进真实设备接入，不和 A/B 的代码开发冲突。
- 把海康单路真实视频试点所需现场资料补齐到可开工状态。

允许修改：

- `docs/REAL_DEVICE_ACCESS_HIKVISION.md`
- `docs/DEVICE_SDK_REQUIREMENTS.md`

禁止修改：

- `backend/`
- `frontend/`
- `database/`
- `deploy/`
- `ai-service/`
- `edge-simulator/`

必须完成：

1. 确认现场最终接入模式：IPC/NVR 直连、平台网关、还是 ISUP。
2. 确认是否有 HikCentral / iSecure Center / OpenAPI 平台。
3. 收集至少 1 路测试通道资料：
   - endpoint
   - 端口
   - vendorDeviceCode
   - vendorChannelCode
   - channelNo
   - streamType
4. 确认测试账号权限：
   - 通道读取
   - 实时预览
   - 抓图
   - 录像查询/回放
   - 事件订阅
   - PTZ，如现场允许
5. 明确 credentialRef 的现场密钥管理方式。
6. 不记录真实密码、真实 token、真实密钥。

必须执行命令：

- 文档任务无强制构建命令。

完成后回报格式：

```text
任务编号：SP-REAL-P0-002
当前状态：完成 / 部分完成 / 阻塞
已确认接入模式：
是否有平台网关：
是否已有 1 路真实通道资料：
仍缺参数：
是否具备单路真实视频试点条件：
更新文档：
需要项目负责人协调：
风险：
```

### SP-REAL-P0-003：云端后端部署解阻

负责人：机动人员或拥有云端 `deploy` SSH key 的运维人员

优先级：P0

状态：完成

任务目标：

- 把程序员 A 已完成的后端真实业务代码部署到云端。
- 让云端 backend 从旧版本切换到支持设备 CRUD、稳定 Mock ID、未知通道 404 的新版本。

允许修改：

- 云端部署目录 `/opt/ai-inspection-platform/current`
- `deploy/` 下必要部署记录文档

禁止修改：

- 不修改业务代码。
- 不删除 PostgreSQL、MinIO、MQTT、SRS 数据卷。
- 不执行 `docker compose down -v`。
- 不开放 MinIO/EMQX 控制台公网访问。

必须完成：

1. 使用 `deploy` 账号登录云端。
2. 备份当前云端部署状态和 backend 日志。
3. 确认云端代码已包含本地最新 backend 修改。
4. 重新构建 backend 容器，而不是只重启旧镜像。
5. 启动后验证 backend 健康检查。
6. 验证云端 `POST /api/devices` 不再返回 405。
7. 验证云端 `POST /api/video/sessions` with `ch-camera-a1` 成功。
8. 验证云端 `POST /api/ai/tasks` with `ch-camera-a1` 成功。
9. 验证云端 unknown channelId 返回 400/404，不返回 500。

必须执行命令：

- 见 `docs/EXECUTION_COMMANDS.md` 的 `SP-REAL-P0-003` 章节。

完成后回报格式：

```text
任务编号：SP-REAL-P0-003
当前状态：完成 / 部分完成 / 阻塞
云端登录账号：
部署目录：
执行的 docker compose 命令：
backend 镜像是否重新构建：
健康检查结果：
POST /api/devices 验证结果：
ch-camera-a1 video session 验证结果：
ch-camera-a1 ai task 验证结果：
unknown channelId 验证结果：
是否影响现有服务：
更新文档：
发现的问题：
```

## 4. 项目负责人通知模板

项目负责人以后只需要把下面这句话发给团队：

```text
总指挥已更新任务分派。
请所有人到 docs/TASK_DISPATCH.md 查看自己的任务，到 docs/EXECUTION_COMMANDS.md 查看需要执行的命令。
完成或阻塞后，按任务文档里的回报格式追加写入 docs/TASK_FEEDBACK.md。
写完后只需要通知：我已回填 docs/TASK_FEEDBACK.md，请总指挥核查。
```

## 5. 固定回报落点

团队所有任务回报统一写入：

```text
docs/TASK_FEEDBACK.md
```

禁止只通过聊天、微信、口头或项目负责人转述长文本作为正式回报。项目负责人只负责通知总指挥“他们已回填 `docs/TASK_FEEDBACK.md`”，总指挥再读取文档核查。

## 6. 总指挥当前裁决

- 程序员 A 当前无新编码任务，转入待命，准备下一轮“真实设备接入”后端开发。
- 程序员 B 当前无新编码任务，等待 `QA-REAL-P0-001-R4` 回归结果；如页面仍有缺陷再开新单。
- QA 立即执行 `QA-REAL-P0-001-R4`，这是当前唯一必须收口的 P0 页面验收任务。
- 机动人员继续推进 `SP-REAL-P0-002`，但不得阻塞当前页面验收闭环。
