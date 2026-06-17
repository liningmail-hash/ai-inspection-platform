# QA RUN 003：设备管理真实 CRUD 验收

## 1. 执行信息

| 项目 | 内容 |
|---|---|
| 任务编号 | QA-REAL-P0-001 |
| 负责人 | 测试工程师 |
| 执行时间 | 2026-06-16 16:02 |
| 前端 | `http://114.67.114.201:5173` |
| 后端 | `http://114.67.114.201:8080/api` |
| AI 服务 | `http://114.67.114.201:8100` |
| 边缘模拟器 | `http://114.67.114.201:8200` |

本轮目标：验收设备管理是否从“能看”进入“能用”，覆盖真实保存、刷新后保留、审计日志和稳定 Mock ID 回归。

## 2. 云端健康检查

| 编号 | 项目 | 结果 | 说明 |
|---|---|---|---|
| HC-001 | backend `/api/health` | 通过 | HTTP 200 |
| HC-002 | ai-service `/health` | 通过 | HTTP 200 |
| HC-003 | edge-simulator `/health` | 通过 | HTTP 200 |

## 3. 后端真实 CRUD 与持久化

本轮测试批次：`qa-real-20260616160228`

| 编号 | 验收项 | 结果 | 说明 |
|---|---|---|---|
| API-001 | `GET /api/devices` 基线查询 | 通过 | 返回 6 条设备 |
| API-002 | `GET /api/video/channels` 基线查询 | 通过 | 返回 11 条通道 |
| API-003 | `GET /api/system/audit-logs` 基线查询 | 通过 | 返回 24 条审计日志 |
| API-004 | 新增 camera 设备 | 通过 | id `996a1fc2-bb95-42e2-8781-19f680b0a1e4` |
| API-005 | 新增 drone 设备 | 通过 | id `15c3e000-238f-443c-a77b-3c5eb5f40a25` |
| API-006 | 新增 vehicle 设备 | 通过 | id `cb9d88b9-be10-47d1-87fc-7a566ff7aab6` |
| API-007 | 新增 camera 后重新查询仍存在 | 通过 | 通过 `GET /api/devices` 复核 |
| API-008 | 新增 drone 后重新查询仍存在 | 通过 | 通过 `GET /api/devices` 复核 |
| API-009 | 新增 vehicle 后重新查询仍存在 | 通过 | 通过 `GET /api/devices` 复核 |
| API-010 | 编辑 camera 名称、endpoint、location | 通过 | 更新后接口立即返回新值 |
| API-011 | 编辑 camera 后重新查询仍保留 | 通过 | 通过 `GET /api/devices` 复核 |
| API-012 | 测试连接返回明确结果 | 通过 | 返回 `status=online`，message `MOCK_VENDOR SDK gateway reachable` |
| API-013 | 同步通道返回明确结果 | 通过 | 返回 `status=synced`，message `device channels synced`，`channelCount=2` |
| API-014 | 同步后视频通道查询可用 | 通过 | 通道数量从 11 变为 13 |
| API-015 | 删除 vehicle 设备 | 通过 | 删除后列表不再返回该 id |
| API-016 | `GET /api/devices` 字段完整性 | 通过 | 包含 `name/sourceType/vendor/protocol/endpoint/credentialRef/location/edgeNodeId/status/streamUrl/createdAt/updatedAt` |

## 4. 审计日志验证

| 编号 | 审计动作 | 结果 |
|---|---|---|
| AUDIT-001 | `DEVICE_CREATE` | 通过 |
| AUDIT-002 | `DEVICE_UPDATE` | 通过 |
| AUDIT-003 | `DEVICE_DELETE` | 通过 |
| AUDIT-004 | `DEVICE_TEST` | 通过 |
| AUDIT-005 | `DEVICE_SYNC_CHANNELS` | 通过 |

## 5. 稳定 Mock ID 回归

| 编号 | 验收项 | 结果 | 说明 |
|---|---|---|---|
| MOCK-001 | `POST /api/video/sessions` with `ch-camera-a1` | 通过 | 返回 `status=playing`，`channelId=ch-camera-a1` |
| MOCK-002 | `POST /api/ai/tasks` with `ch-camera-a1` | 通过 | 返回 `status=running` |
| MOCK-003 | unknown channelId 创建视频会话 | 通过 | 返回 HTTP 404，不返回 500 |

## 6. 前端设备管理页验收

| 编号 | 验收项 | 结果 | 缺陷编号 | 说明 |
|---|---|---|---|---|
| UI-001 | 设备管理页打开 | 通过 | - | 页面非白屏，设备管理页面可见 |
| UI-002 | 页面显示真实 API 数据源 | 失败 | P0-QA-REAL-001 | 云端页面显示 `Mock兜底`，未显示 API 联调状态 |
| UI-003 | 点击“接入设备”出现新增表单 | 失败 | P0-QA-REAL-001 | 点击后无弹窗、无表单、无新增输入控件 |
| UI-004 | 页面存在编辑/删除/测试/同步真实操作入口 | 失败 | P0-QA-REAL-001 | 云端页面仍是旧版展示：只有集成卡测试/同步，未出现设备行编辑、删除、真实表单操作 |

补充定位：

- 本地代码 `frontend/src/views/DeviceView.vue` 已能看到新增/编辑/删除/测试/同步表单逻辑。
- 云端前端页面仍展示旧版“设备接入中心 / SDK 集成卡”形态，点击“接入设备”无反馈。
- 推测云端前端未重新构建部署，或前端构建时 API base 仍指向 `localhost` 导致兜底 Mock。

## 7. 缺陷列表

| 缺陷编号 | 等级 | 分类 | 标题 | 影响 | 建议责任 |
|---|---|---|---|---|---|
| P0-QA-REAL-001 | P0 | 前端/部署 | 云端设备管理页未启用真实 CRUD 表单，仍处于 Mock 兜底/旧版展示状态 | 阻断用户通过页面完成新增、编辑、删除、测试、同步；设备管理真实 CRUD 无法从页面验收通过 | 程序员 B / 机动人员 |

## 8. 统计

| 指标 | 数量 |
|---|---:|
| 执行项数量 | 28 |
| 通过数量 | 25 |
| 失败数量 | 3 |
| 阻塞数量 | 0 |

P0 缺陷：`P0-QA-REAL-001`

P1 缺陷：无

P2 缺陷：无

## 9. 结论

- 后端真实保存验证结果：通过。设备新增、编辑、删除、测试、同步均已在云端接口层验证，刷新式重新查询后数据保留。
- 审计日志验证结果：通过。新增、编辑、删除、测试、同步动作均可查。
- 稳定 Mock ID 回归结果：通过。`ch-camera-a1` 视频会话和 AI 任务成功，未知通道返回 404。
- 页面真实 CRUD 验收结果：失败。云端前端设备管理页仍未提供真实新增、编辑、删除表单与反馈，不能按全链路完成处理。

## 10. 需要总指挥分派

- 分派程序员 B 启动 `FE-REAL-P0-001-R1`，修复云端设备管理页真实 CRUD 表单、编辑/删除入口、错误反馈和 API 数据源显示。
- 分派机动人员或前端负责人确认云端 frontend 是否已重新构建部署，并确保构建时 API base 指向云端后端而不是浏览器本机 `localhost`。

## QA-REAL-P0-001-R5 Final Regression - 2026-06-16 21:05

Scope: FE-REAL-P0-001-R2 cloud deployment verification on http://114.67.114.201:5173.

Result summary:
- Status: completed with P0 closed.
- Executed items: 12
- Passed: 11
- Failed: 0
- Blocked: 0
- Observation: 1 non-blocking follow-up for delete feedback/target-row confirmation.

Checks:
1. Cloud frontend reachable: pass.
2. Backend health via direct API: pass.
3. AI service health: pass.
4. Edge simulator health: pass.
5. Device page no longer shows visible `Mock兜底`: pass.
6. Main button shows `接入设备`: pass.
7. Add form opens with device fields: pass.
8. UI create device succeeds without `Failed to fetch`: pass.
9. Created device appears in `/api/devices`: pass.
10. UI edit device succeeds and persists after refresh: pass.
11. UI test connection returns real feedback `MOCK_VENDOR SDK gateway reachable`: pass.
12. UI sync channels returns real feedback `device channels synced`: pass.

Test data:
- Created device: `qa-r5-1781614505650-camera`
- Edited device: `qa-r5-1781614505650-camera-edited`
- Device id observed in audit logs: `5ed7281c-9e80-4b3e-a6bf-06d89d8d456e`

Audit verification:
- `DEVICE_CREATE`: pass.
- `DEVICE_UPDATE`: pass.
- `DEVICE_TEST`: pass.
- `DEVICE_SYNC_CHANNELS`: pass.

P0 decision:
- `P0-QA-REAL-001` can be closed. Original P0 symptoms are fixed: cloud page no longer stays in Mock fallback, `接入设备` is present, add form works, and create no longer reports `Failed to fetch`.

Follow-up observation:
- During cleanup, QA did not reliably confirm target-row deletion through the browser. This does not block closing `P0-QA-REAL-001`, but should be kept as a follow-up UX/API verification item for delete confirmation and row-level feedback.
