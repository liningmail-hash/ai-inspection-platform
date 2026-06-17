# 项目状态与部署决策报告

更新时间：2026-06-16

本文档由总指挥维护，用于向项目负责人汇报当前阶段、重大进展、部署条件、风险和下一步安排。每完成一个 P0/P1 任务、出现阻塞、进入部署窗口或发现重大风险时必须更新。

## 1. 当前阶段

当前阶段：P0 三线接入真实视频与告警闭环阶段。

阶段判断：

- 已完成厂区摄像头、无人机、车载监控三类 `sourceType` 的统一模型和前端展示。
- 已完成后端三线接口骨架、SDK 适配器注册框架、Mock 厂家适配器。
- 已完成视频服务、MQTT、MinIO 的 Docker Compose 拓扑预留。
- 已完成测试计划、验收清单和设备 SDK 资料清单。
- 尚未完成真实厂家 SDK 接入、真实视频流、真实 AI 模型、真实设备联调和稳定性压测。
- QA-P0-002 已部分执行：前端页面 Mock 冒烟 5 项通过，中心后端/AI/边缘接口冒烟因环境缺 Maven/Docker/Python 依赖被阻塞。
- SP-P0-002 已完成：统一 QA 环境文档和 `deploy/qa-start/` 启动/健康检查脚本已就绪，支持 Windows PowerShell、Ubuntu Bash、Docker Compose 和本地多进程路径。
- SP-P0-004 已完成：云服务器 Mock 联调环境已通过 Docker Compose 部署，frontend/backend/ai-service/edge-simulator 健康检查均通过。
- FE-REAL-P0-001-R2 已完成：设备管理页真实提交链路已打通，P0-QA-REAL-001 已关闭。

一句话结论：

项目已经具备“设备真实可用、可回归、可分工推进”的 P0 骨架，当前开始推进真实视频与告警闭环。

## 2. 已完成的关键进展

### 产品与协同

- 明确平台目标：生产安全园区 AI 巡检，覆盖厂区、无人机、车载监控。
- 建立总指挥协同机制：[TEAM_COMMAND_CENTER.md](TEAM_COMMAND_CENTER.md)。
- 建立角色分工：后端 A、前端 B、测试工程师、机动人员。
- 建立任务单、回报格式、冲突处理和验收机制。

### 后端与数据库

- 已有中心后端、AI 服务、边缘模拟器和 PostgreSQL 基线。
- 新增集成、视频通道、视频会话、AI 任务、边缘事件、无人机、车辆、轨迹等接口骨架。
- 新增厂家 SDK 适配器选择框架，默认 `MOCK_VENDOR`，预留 `HIKVISION`、`DAHUA`、`DJI_DOCK`、`JT1078`。
- BE-REAL-P0-002 已完成：设备 CRUD、稳定 Mock ID、404 错误模型和审计日志已云端复核通过。

### 前端

- 视频墙、设备管理、告警中心、无人机巡检已覆盖三线来源。
- 关键操作已有页面反馈：开流、AI、抓图、录像、SDK 测试/同步、告警处置、飞行任务启停。
- 设备管理页真实提交链路已打通并关闭 P0。
- 下一步前端重点转向视频墙、告警中心、无人机/车载页的真实联动。

### 测试与资料

- 已新增 [TEST_PLAN.md](TEST_PLAN.md)。
- 已新增 [ACCEPTANCE_CHECKLIST.md](ACCEPTANCE_CHECKLIST.md)。
- 已新增 [DEVICE_SDK_REQUIREMENTS.md](DEVICE_SDK_REQUIREMENTS.md)。
- 已新增 [QA_RUN_001.md](QA_RUN_001.md)，记录第一轮 Mock 冒烟执行结果。
- 已新增 [DEPLOYMENT_RECORD_SP-P0-004.md](../deploy/DEPLOYMENT_RECORD_SP-P0-004.md)，记录云端 Mock 联调环境部署结果。

## 3. 当前阻塞与风险

### P0 阻塞

- 本地环境缺少 Java、Maven、Docker，后端 `mvn test` 和全栈 Compose 未能在当前机器验证。
- QA-P0-002 接口冒烟被环境阻塞：当前环境缺 `mvn`、无 Maven Wrapper、Docker 不可用。
- AI 服务和边缘模拟器依赖安装超时，缺 `fastapi`、`uvicorn` 时无法启动服务冒烟。
- BE-P0-003 已解除系统 Maven 依赖，后端可用 Maven Wrapper 启动；当前机器仍缺 JDK 21/JAVA_HOME。
- SP-P0-002 已提供 Python 3.12 venv 安装路径；当前机器 Python 为 3.14.3，仍需人工安装 Python 3.12 后恢复 AI/边缘服务冒烟。
- SP-P0-003 回报 JDK 21 与 Python 3.12 已安装，AI 服务和边缘模拟器健康检查通过；但当前执行后端启动的 PowerShell 会话仍无法识别 `java`/`JAVA_HOME`，后端 8080 尚未恢复。
- 本地 QA 环境仍有后端 Java/JAVA_HOME 会话问题，但云端 Mock 环境已可替代本地继续执行接口冒烟。
- HIKVISION 真实接入模式未定：平台网关优先，还是摄像头/NVR 直连。
- HIKVISION SDK 包、动态库、平台 OpenAPI 权限、设备地址、端口、账号、通道编码规则尚未提供。

### P1 风险

- `DEVICE_SDK_REQUIREMENTS.md` 中部分字段名需要与后端实际 `VideoChannel` 对齐，避免后续 A/B/QA 理解不一致。
- `syncChannels` 当前在真实 SDK 缺失时只能返回空列表，容易被误判为同步成功但无通道；需要在 BE-P0-002-S1 或下一轮补明确错误响应。
- HIKVISION `openPreview` 当前仍可能以异常形式表达未接 SDK，后续要转为统一 `CommandResult` 或明确错误结构，避免前端收到不可读 500。
- 当前视频服务拓扑已预留 SRS，但真实转流链路还未跑通。
- AI 服务当前仍是 Mock Runtime，未接 ONNX/TensorRT。
- 边缘模拟器尚未升级为真实边缘 Agent。

## 4. 部署判断

### 当前可以部署什么

可以部署：

- 设备管理真实可用版。
- 后端真实业务骨架版。
- AI Mock 推理服务。
- 边缘模拟器。
- Docker Compose 拓扑草案。
- 云端真实视频与告警闭环联调环境。

用途：

- 内部演示。
- 产品评审。
- 前后端联调。
- 云端真实视频与告警联调准备。
- QA 下一轮真实闭环验收。

不建议用于：

- 客户现场试点验收。
- 真实安全生产告警。
- 真实无人机远程控制。
- 真实 50-150 路视频接入。

### 部署时间判断

| 部署类型 | 最早时间 | 条件 | 结论 |
|---|---:|---|---|
| 前端 Mock 预览 | 已可用 | `npm run dev` 或静态构建 | 可立即给内部看 |
| 本机 Mock 全栈 | 1-2 天 | 安装 Java/Maven/Docker，并跑通 Compose | 可做开发联调 |
| 内网 Mock 演示部署 | 2-3 天 | Ubuntu/Docker 可用，端口和环境变量确认 | 可做管理层演示 |
| 云端真实闭环联调 | 进行中 | 设备管理已真实可用，下一步接视频与告警链路 | 可开始真实闭环开发 |
| HIKVISION 单路真实视频试点 | 3-7 天 | 提供海康平台或设备接入参数、SDK、网络权限 | 可做真实链路验证 |
| 三线真实设备小试点 | 2-4 周 | 摄像头、无人机、车载三类设备参数齐全，AI 和视频链路联调完成 | 可做园区试点 |
| 生产验收版本 | 6-10 周 | 72 小时压测、权限、审计、告警闭环、部署手册、真实 SDK 全部通过 | 可进入正式验收 |

## 5. 当前总指挥决策

1. 真实接入优先级：HIKVISION 第一，DJI_DOCK 第二，JT1078 第三，DAHUA 第四。
2. HIKVISION 优先推荐平台网关模式；若现场权限短期无法提供，则用 1 台摄像头或 1 台 NVR 直连做最小闭环。
3. 后端 A 进入 BE-REAL-P0-003，优先打通视频、AI、告警、证据、审计闭环。
4. 前端 B 进入 FE-REAL-P0-002，优先接视频墙、告警中心、无人机页真实联动。
5. 机动人员进入 SP-REAL-P0-004，继续补齐真实视频试点资料。
6. QA 进入 QA-REAL-P0-002，待真实闭环后做验收。

## 6. 下一轮里程碑

### M1：真实视频与告警闭环可验收

目标：

- 真实或准真实视频会话可创建。
- AI 任务可创建并回写结果。
- 告警可产生、可确认、可误报、可派单、可关闭。
- 审计和证据可追踪。

责任：

- A：提供后端闭环和事件契约。
- B：接前端页面。
- QA：执行闭环验收。

### M2：三线真实设备小试点

目标：

- 在 Ubuntu 或 Docker 环境跑起前端、后端、AI、边缘、PostgreSQL、MinIO、MQTT、SRS。
- 打通至少一条真实摄像头或平台网关链路。
- 形成可复用部署记录。

进入条件：

- 有 Java/Maven/Docker 环境。
- `frontend npm run build` 通过。
- `backend mvn test` 通过或有明确豁免。
- Python 服务语法检查和健康检查通过。
- 至少一路真实通道接入完成。

### M3：HIKVISION 单路真实视频闭环

目标：

- 同步一路真实摄像头通道。
- 创建播放会话。
- 抓图生成证据。
- 创建 AI 任务。
- 生成告警并处置。
- 审计日志可追踪。

进入条件：

- 明确海康接入模式。
- 提供 SDK 或平台 OpenAPI 权限。
- 提供测试设备或通道。
- 网络端口打通。

## 7. 汇报机制

总指挥必须在以下场景向项目负责人汇报：

- 每个 P0 任务完成或阻塞。
- 出现 P0 阻断缺陷。
- 可以进入部署或不能部署的判断发生变化。
- 真实设备接入条件发生变化。
- 需要项目负责人协调现场资源、账号、网络、设备或人员。
- 每完成一个里程碑 M1/M2/M3。

标准汇报格式：

```text
项目阶段：
本轮完成：
当前可部署状态：
不能部署的原因：
主要风险：
需要你协调：
下一步安排：
预计进入下一阶段时间：
```

## 8. 当前需要项目负责人协调

请优先协调 HIKVISION 试点参数：

- 接入模式：平台网关 / NVR / 单摄像头直连。
- SDK 或 OpenAPI 资料。
- 测试设备地址、端口、账号。
- 通道列表或资源编码规则。
- 视频输出方式：RTSP / HLS / FLV / WebRTC。
- 部署目标系统：Windows / Linux，x86_64 / ARM64。
- 是否允许平台回调、事件订阅、抓图、录像、PTZ。

## 9. 最新总指挥裁决

### BE-P0-002 阶段裁决

结论：阶段通过，任务不关闭。

已认可：

- HIKVISION 作为第一真实接入目标。
- HIKVISION 配置占位和 unavailable message 方向正确。
- 车载 snapshot/record 不再误走 camera adapter 的边界修正正确。
- Controller REST 接口保持不变。

待补：

- BE-P0-002-S1 必须给 QA 稳定 Mock ID、边缘事件样例、成功响应样例和错误响应结构。
- 同步接口缺 SDK 时不能只表现为 `channelCount=0`，必须让 QA 能区分“同步成功但 0 通道”和“SDK 未配置无法同步”。
- HIKVISION preview 未接 SDK 时，后续应返回统一错误响应，避免不可读异常。

### QA-P0-002 阶段裁决

结论：部分通过，任务暂停在环境阻塞点。

已认可：

- 前端页面 Mock 冒烟通过 5 项。
- 稳定 Mock ID 已写入 API 契约：`ch-camera-a1`、`ch-drone-001`、`ch-vehicle-front`。
- 暂无 P0/P1/P2 功能缺陷。

阻塞：

- `BLK-QA-001`：中心后端无法启动，缺 Maven/Maven Wrapper/Docker。
- `BLK-QA-002`：AI 服务依赖安装超时，缺 FastAPI/Uvicorn。
- `BLK-QA-003`：边缘模拟器依赖安装超时，缺 FastAPI/Uvicorn。

裁决：

- QA-P0-002 继续保留，不关闭。
- 先执行环境解阻任务，再恢复接口冒烟。
- 前端 B 暂无返工任务。

### SP-P0-002 阶段裁决

结论：通过验收，任务关闭。

已认可：

- [QA_ENV_SETUP.md](QA_ENV_SETUP.md)、[INSTALLATION.md](INSTALLATION.md)、[OPERATIONS.md](OPERATIONS.md) 已补充 QA 环境路径。
- `deploy/qa-start/start-local.ps1`、`health-check.ps1`、`start-local.sh`、`health-check.sh` 已提供。
- 支持 Docker Compose 路径和 Docker 不可用时的本地多进程路径。
- 当前前端健康检查通过，后端/AI/边缘健康检查能正确报失败。

仍需人工安装：

- JDK 21 和 `JAVA_HOME`。
- Python 3.12。
- Docker Desktop/Engine，仅在走 Compose 时需要。

### SP-P0-003 阶段裁决

结论：部分通过，不关闭。

已通过：

- JDK 21 已安装。
- Python 3.12 已安装。
- AI 服务健康检查已通过。
- 边缘模拟器健康检查已通过。
- 前端健康检查已通过。

仍阻塞：

- 当前用于启动后端的 PowerShell 会话未识别 `java` 和 `JAVA_HOME`。
- 后端 8080 健康检查未通过。

裁决：

- 先要求机动人员重新打开 PowerShell 并在同一会话内验证 `java -version`、`$env:JAVA_HOME`、`backend\mvnw.cmd -version`。
- 如果新会话仍无法识别 Java，则继续由机动人员修环境变量，不进入后端代码修复。
- 只有在 Java/Maven Wrapper 可用但 `spring-boot:run` 仍失败时，才将问题升级给程序员 A。

裁决：

- QA-P0-002 可以在完成 JDK 21 和 Python 3.12 安装后恢复。
- 恢复时优先走本地多进程路径，不强制 Docker。

### SP-P0-004 阶段裁决

结论：通过验收，任务关闭。

已通过：

- 云服务器 Ubuntu 24.04.2 LTS，4 核 / 7.8 GiB，Docker 29.1.3，Compose 2.40.3。
- 部署目录：`/opt/ai-inspection-platform/current`。
- 前端、后端、AI 服务、边缘模拟器健康检查通过。
- PostgreSQL、MinIO、MQTT、SRS 均已启动，内部访问策略清晰。
- 公网仅开放 `22`、`5173`、`8080`、`8100`、`8200`。

裁决：

- 当前云环境定位为 Mock 联调 / 演示环境，不是生产环境。
- 立即恢复 QA-P0-002 云端接口冒烟。
- 不建议长期共用 root；下一步创建非 root 运维账号并回收/轮换 root 登录凭据。
- 暂不开放 MinIO/EMQX 控制台公网访问；如必须访问，使用指定 IP 白名单或 SSH 隧道。
- 域名和 HTTPS 建议进入下一步任务，用于稳定演示和后续试点。

裁决：

- 允许下一步扩展 adapter 内部同步错误模型，但不允许新增或破坏公开 REST 路径。
- 优先采用内部 `AdapterResult` 或新增内部 `ChannelSyncResult` 方式表达同步结果；Controller 对外仍返回 `CommandResult`。
- 真实 HIKVISION SDK 参数由项目负责人协调，机动人员继续支援收集，不由程序员 A 猜测。

### SP-P0-005 阶段裁决

结论：通过验收，任务关闭。

已确认：

- 云端 Mock 环境已改用 `deploy` 非 root 账号运维。
- `deploy` 已支持 SSH key 登录，并具备 docker/sudo 运维能力。
- root 密码已锁定，root 密码登录已禁用。
- 当前业务服务未被中断，后端健康检查仍正常。
- MinIO、EMQX、PostgreSQL、Redis、MQTT、SRS 等内部端口未开放公网访问。

裁决：

- 后续云端运维默认使用 `deploy` 账号，不再使用 root 密码。
- 若后续多人长期协作，应为每个协作人员单独配置 SSH key，不共用私钥。
- 若办公公网 IP 稳定，建议在云厂商安全组限制 `22` 端口只允许指定办公 IP 访问。
- `deploy` 免密 sudo 可在 Mock 联调期临时保留；进入真实设备试点前，应收敛为最小权限 sudo。

### QA-P0-003 阶段裁决

结论：云端 Mock 冒烟主体通过，任务关闭；发现 1 个 P1 缺陷，转入 BE-P0-004 修复。

已确认：

- 云端 frontend、backend、ai-service、edge-simulator 健康检查全部通过。
- 本轮执行 27 项，通过 25 项，失败 2 项，阻塞 0 项。
- 当前无 P0 缺陷，不影响继续云端 Mock 联调和产品演示评审。
- P1 缺陷 `P1-QA-003-001`：后端云端通道 ID 与稳定 Mock ID 不一致；`POST /api/video/sessions`、`POST /api/ai/tasks` 使用 `ch-camera-a1` 返回 500，使用后端返回 UUID 可成功。

裁决：

- 稳定 Mock ID 是对外验收契约，后端必须兼容：`ch-camera-a1`、`ch-drone-001`、`ch-vehicle-front`。
- 后端内部数据库可以继续使用 UUID，但 REST 层和 QA 验收必须能使用稳定 Mock ID。
- 未知通道不得返回 500，应返回 4xx 和明确错误信息。
- 该问题由程序员 A 执行 BE-P0-004；QA 等待修复完成后执行 QA-P0-003-R1 回归。

### 总指挥目标校准：从演示环境转入真实业务功能

结论：当前项目唯一目标不是持续打磨演示环境，而是把系统推进为可真实使用的生产安全园区 AI 巡检系统。

产品原则：

- Mock 只允许作为无真实设备时的联调兜底，不允许替代真实业务功能。
- 所有新增任务必须服务于真实闭环：设备接入、通道管理、视频播放、AI 任务、告警处置、用户权限、训练模型、审计报表。
- 前端按钮必须逐步具备真实操作能力：新增、编辑、删除、保存、测试、同步、启停、处置、导出。
- 后端接口必须具备真实持久化、明确错误响应和审计记录，不允许只返回静态演示数据。
- QA 验收不再只看页面是否能打开，还要验证数据是否真的写入、状态是否真的变化、错误是否可解释、审计是否可追溯。

阶段判断：

- 云端 Mock 部署和安全加固已完成，作为联调环境保留。
- 下一阶段进入“可操作业务功能开发”，优先补齐设备管理、用户权限、视频通道、AI 任务、告警闭环这些实际功能。
- 真实 SDK 接入继续按 HIKVISION / DJI_DOCK / JT1078 优先级推进，但在真实设备参数未齐前，业务系统本身也必须先能完成配置、保存、同步、测试和审计。

### BE-REAL-P0-002 阶段裁决

结论：本地代码阶段通过，云端部署阻塞，任务不关闭。

已确认：

- 程序员 A 已完成设备 CRUD、稳定 Mock ID 兼容、未知 channelId 404、本地审计日志写入等代码工作。
- 本地设置 JDK 21 `JAVA_HOME` 后，`.\mvnw.cmd test` 通过，BUILD SUCCESS。
- 本地已验证 `POST /api/video/sessions` with `ch-camera-a1` 成功。
- 本地已验证 `POST /api/ai/tasks` with `ch-camera-a1` 成功。
- 本地未知 channelId 返回 404，不再是 500。

云端阻塞：

- 云端仍为旧版本表现。
- 云端 `POST /api/devices` 返回 405 Method Not Allowed。
- 云端 `POST /api/video/sessions` with `ch-camera-a1` 仍返回 500。
- 云端 `POST /api/ai/tasks` with `ch-camera-a1` 仍返回 500。
- 程序员 A 当前机器无 `deploy` SSH 私钥，无法完成云端部署。

裁决：

- 新增 `SP-REAL-P0-003`，由机动人员或拥有云端 `deploy` SSH key 的运维人员优先执行云端后端重新构建和部署。
- 程序员 A 暂不继续改代码，等待云端部署生效后复核接口。
- QA-REAL-P0-001 暂停，等待云端部署验证通过后再启动。
- 程序员 B 暂不启动联调修正，等待 QA 给出真实前端问题。

### SP-REAL-P0-003 阶段裁决

结论：通过验收，任务关闭。

已确认：

- 机动人员已使用 `deploy` 账号完成云端 backend 重新构建和部署。
- 已执行现有数据库迁移脚本，补齐旧 PostgreSQL volume 的设备管理字段。
- 已新增部署记录 `deploy/DEPLOYMENT_RECORD_SP-REAL-P0-003.md`。
- backend、frontend、ai-service、edge-simulator 公网健康检查保持可用。

总指挥公网复核：

```text
GET  /api/health                         通过
GET  /api/devices                        通过，字段包含 sourceType、endpoint、credentialRef、location、edgeNodeId、createdAt、updatedAt
POST /api/devices                        通过，不再返回 405
POST /api/video/sessions ch-camera-a1    通过，返回 playing 会话
POST /api/ai/tasks ch-camera-a1          通过，返回 running 任务
POST /api/video/sessions unknown         返回 404，不再返回 500
```

裁决：

- 云端后端部署阻塞已解除。
- 程序员 A 立即补交 `BE-REAL-P0-002` 云端复核回报。
- QA 允许启动 `QA-REAL-P0-001`，正式验收设备管理真实 CRUD、审计日志和稳定 Mock ID 回归。
- 程序员 B 继续等待 QA 结果，如 QA 发现前端字段或交互问题，再启动 `FE-REAL-P0-001-R1`。

### FE-REAL-P0-001-R1 阶段裁决

结论：本地修复完成，云端前端未确认生效，任务不关闭。

已确认：

- 程序员 B 已修复前端 API base 默认地址逻辑，避免云端访问时错误请求浏览器本机 `localhost:8080`。
- 本地 `npm run build` 已通过。
- 程序员 B 已确认本地代码具备真实新增表单、编辑入口、删除确认、测试连接反馈、同步通道反馈。

当前阻塞：

- QA-REAL-P0-001 发现 `P0-QA-REAL-001`：云端设备管理页仍显示 Mock 兜底/旧版展示。
- 云端页面点击“接入设备”无真实新增表单。
- 云端页面未出现真实编辑/删除入口。
- 当前更像是云端 frontend 尚未重新构建部署，或部署后静态资源未刷新生效。

裁决：

- 需要新增前端云端部署解阻任务，由具备云端权限的人员重新构建并部署 frontend。
- QA 暂不关闭 `QA-REAL-P0-001`，等待前端云端生效后执行页面回归。
- BE-REAL-P0-002 维持完成状态，不再回退到后端。
