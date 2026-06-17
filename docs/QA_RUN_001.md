# QA RUN 001：第一轮 Mock 冒烟验收

## 1. 执行信息

| 项目 | 内容 |
|---|---|
| 任务编号 | QA-P0-002 |
| 负责人 | 测试工程师 |
| 执行日期 | 2026-06-16 |
| 执行范围 | 前端页面、中心后端 API、AI 服务、边缘模拟器 |
| 前端地址 | `http://localhost:5173` |
| 后端地址 | `http://localhost:8080/api` |
| AI 服务地址 | `http://localhost:8100` |
| 边缘模拟器地址 | `http://localhost:8200` |

## 2. Mock 输入

| 类型 | Mock ID | 来源 |
|---|---|---|
| 厂区摄像头通道 | `ch-camera-a1` | `frontend/src/data/mock.ts`、`backend/InMemoryInspectionRepository`、`edge-simulator` |
| 无人机通道 | `ch-drone-001` | `frontend/src/data/mock.ts`、`backend/InMemoryInspectionRepository`、`edge-simulator` |
| 车载通道 | `ch-vehicle-front` | `frontend/src/data/mock.ts`、`backend/InMemoryInspectionRepository`、`edge-simulator` |
| 车载备用通道 | `ch-vehicle-cargo` | `frontend/src/data/mock.ts`、`backend/InMemoryInspectionRepository` |

## 3. 环境检查

| 编号 | 检查项 | 结果 | 说明 |
|---|---|---|---|
| ENV-QA-001 | 前端 `http://localhost:5173` | 通过 | 页面返回 HTTP 200，前端可访问 |
| ENV-QA-002 | 中心后端启动 | 阻塞 | 当前环境未安装 `mvn`，项目无 Maven Wrapper，Docker 也不可用 |
| ENV-QA-003 | AI 服务启动 | 阻塞 | Python 环境缺 `fastapi`、`uvicorn`；执行 `pip install -r requirements.txt` 超时 |
| ENV-QA-004 | 边缘模拟器启动 | 阻塞 | Python 环境缺 `fastapi`、`uvicorn`；执行 `pip install -r requirements.txt` 超时 |

## 4. 前端页面冒烟结果

| 编号 | 页面 | 结果 | 观察 |
|---|---|---|---|
| UI-QA-001 | 视频墙 | 通过 | 展示厂区摄像头、无人机、车载通道；可见 `ch-camera-a1`、`ch-drone-001`、`ch-vehicle-front` 对应播放 URL 和 AI 状态 |
| UI-QA-002 | 设备管理 | 通过 | 展示海康、大疆、JT/T1078 集成卡；可见同步/测试按钮、设备表、统一视频通道、车载终端 |
| UI-QA-003 | 告警中心 | 通过 | 展示告警列表、证据 URL、来源点位、确认/误报/派单按钮和处置状态 |
| UI-QA-004 | 无人机巡检 | 通过 | 展示机场状态、无人机直播 URL、航线、启动/终止按钮、遥测和飞行任务 |
| UI-QA-005 | 报表统计 | 通过 | 展示 KPI、事件地图分布、巡检报告、车载轨迹、素材管理 |

补充观察：

- 前端顶部状态在部分页面显示 `Mock兜底`，符合后端不可用时的兜底预期。
- 浏览器控制台未捕获 `error` 级日志。
- 未发现文字重叠、按钮明显挤压或表格明显溢出。

## 5. 中心后端 API 冒烟结果

| 编号 | 接口 | 结果 | 缺陷/阻塞编号 | 说明 |
|---|---|---|---|---|
| API-QA-001 | `GET /api/integrations` | 阻塞 | BLK-QA-001 | 后端无法在当前环境启动 |
| API-QA-002 | `GET /api/video/channels` | 阻塞 | BLK-QA-001 | 后端无法在当前环境启动 |
| API-QA-003 | `POST /api/video/sessions` | 阻塞 | BLK-QA-001 | 后端无法在当前环境启动 |
| API-QA-004 | `GET /api/ai/tasks` | 阻塞 | BLK-QA-001 | 后端无法在当前环境启动 |
| API-QA-005 | `GET /api/edge/events` | 阻塞 | BLK-QA-001 | 后端无法在当前环境启动 |
| API-QA-006 | `GET /api/vehicles` | 阻塞 | BLK-QA-001 | 后端无法在当前环境启动 |
| API-QA-007 | `GET /api/drones` | 阻塞 | BLK-QA-001 | 后端无法在当前环境启动 |
| API-QA-008 | `GET /api/system/audit-logs` | 阻塞 | BLK-QA-001 | 后端无法在当前环境启动 |

## 6. AI 服务冒烟结果

| 编号 | 接口 | 结果 | 缺陷/阻塞编号 | 说明 |
|---|---|---|---|---|
| AI-QA-001 | `GET /health` | 阻塞 | BLK-QA-002 | AI 服务依赖缺失，无法启动 |
| AI-QA-002 | `POST /infer` | 阻塞 | BLK-QA-002 | AI 服务依赖缺失，无法启动 |
| AI-QA-003 | `GET /ai-tasks` | 阻塞 | BLK-QA-002 | AI 服务依赖缺失，无法启动 |

## 7. 边缘模拟器冒烟结果

| 编号 | 接口 | 结果 | 缺陷/阻塞编号 | 说明 |
|---|---|---|---|---|
| EDGE-QA-001 | `GET /health` | 阻塞 | BLK-QA-003 | 边缘模拟器依赖缺失，无法启动 |
| EDGE-QA-002 | `GET /node/status` | 阻塞 | BLK-QA-003 | 边缘模拟器依赖缺失，无法启动 |
| EDGE-QA-003 | `GET /video/channels` | 阻塞 | BLK-QA-003 | 边缘模拟器依赖缺失，无法启动 |
| EDGE-QA-004 | `GET /events/latest` | 阻塞 | BLK-QA-003 | 边缘模拟器依赖缺失，无法启动 |

## 8. 缺陷与阻塞列表

| 编号 | 类型 | 等级 | 标题 | 责任建议 | 状态 |
|---|---|---|---|---|---|
| BLK-QA-001 | 环境阻塞 | 阻塞 | 当前环境缺 `mvn`，无 Maven Wrapper，Docker 不可用，中心后端无法启动 | 机动人员/程序员 A | 新建 |
| BLK-QA-002 | 环境阻塞 | 阻塞 | 当前 Python 环境缺 `fastapi`、`uvicorn`，AI 服务依赖安装超时 | 机动人员 | 新建 |
| BLK-QA-003 | 环境阻塞 | 阻塞 | 当前 Python 环境缺 `fastapi`、`uvicorn`，边缘模拟器依赖安装超时 | 机动人员 | 新建 |

P0 缺陷：无。

P1 缺陷：无。

P2 缺陷：无。

## 9. 统计

| 指标 | 数量 |
|---|---:|
| 已执行项 | 20 |
| 通过项 | 5 |
| 失败项 | 0 |
| 阻塞项 | 15 |
| 不适用项 | 0 |

## 10. 需要总指挥分派的问题

- 分派机动人员补齐本机或统一测试环境：Maven 或 Maven Wrapper、Docker、Python 依赖安装能力。
- 分派程序员 A 确认后端 Mock API 的启动方式是否必须依赖本机 Maven，是否需要提交 Maven Wrapper。
- 分派机动人员提供可复用的 QA 启动脚本，避免后续冒烟验收被环境问题反复阻塞。

## 11. QA-P0-002-R1 恢复执行记录

执行日期：2026-06-16

### 前置环境确认

| 检查项 | 结果 | 说明 |
|---|---|---|
| JDK 21 / JAVA_HOME | 阻塞 | `java` 命令不可用，`JAVA_HOME` 为空 |
| Python 3.12 | 阻塞 | `python --version` 为 3.14.3，`py -3.12 --version` 未找到 3.12 |
| Node/npm | 通过 | `node v24.14.0`，`npm 11.9.0` |
| Docker | 不适用 | Docker 可选；当前 `docker` 命令不可用 |

### 启动与健康检查

| 步骤 | 结果 | 说明 |
|---|---|---|
| `.\deploy\qa-start\start-local.ps1` | 阻塞 | 脚本在 JDK 检查处停止：`java is not available` |
| `.\deploy\qa-start\health-check.ps1` | 阻塞 | frontend 通过，backend / ai-service / edge-simulator 均不可连接 |

健康检查结果：

| 服务 | 结果 |
|---|---|
| frontend | 通过，HTTP 200 |
| backend | 阻塞，无法连接 `http://localhost:8080/api/health` |
| ai-service | 阻塞，无法连接 `http://localhost:8100/health` |
| edge-simulator | 阻塞，无法连接 `http://localhost:8200/health` |

本次恢复未继续执行业务接口冒烟，原因是任务单要求健康检查失败时不要继续测业务接口。

统计：

| 指标 | 数量 |
|---|---:|
| 已执行接口数量 | 0 |
| 通过数量 | 0 |
| 失败数量 | 0 |
| 阻塞数量 | 3 |

新增 P0 缺陷：无。

新增 P1 缺陷：无。

新增 P2 缺陷：无。

阻塞说明：

- BLK-QA-001 持续存在：当前环境缺 JDK 21 / JAVA_HOME，后端无法启动。
- BLK-QA-002 持续存在：当前环境未安装 Python 3.12，AI 服务无法按脚本创建和运行目标环境。
- BLK-QA-003 持续存在：当前环境未安装 Python 3.12，边缘模拟器无法按脚本创建和运行目标环境。
