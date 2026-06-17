# SP-REAL-P0-003 Cloud Backend Deployment Record

任务编号：SP-REAL-P0-003

执行时间：2026-06-16 15:48-15:54

执行人：机动人员

部署账号：`deploy`

部署目录：`/opt/ai-inspection-platform/current`

## 执行内容

- 已使用 `deploy` 账号登录云端。
- 已备份部署前容器状态到 `deploy/run-records/sp-real-p0-003-before-ps.txt`。
- 已备份部署前 backend 日志到 `deploy/run-records/sp-real-p0-003-before-backend.log`。
- 已同步本地最新项目代码到云端部署目录，保留云端 `deploy/.env`。
- 已确认云端代码包含：
  - `POST /api/devices`
  - `ch-camera-a1`
  - `ch-drone-001`
  - `ch-vehicle-front`
- 已重新构建 backend 镜像。
- 已重新创建并启动 backend 容器。
- 已执行数据库迁移脚本补齐旧 PostgreSQL volume 的 schema：
  - `database/migrations/001_three_line_integration.sql`
  - `database/migrations/002_device_management_crud.sql`

## 执行命令摘要

```bash
docker compose --env-file deploy/.env \
  -f deploy/docker-compose.yml \
  -f deploy/docker-compose.cloud.yml \
  build backend

docker compose --env-file deploy/.env \
  -f deploy/docker-compose.yml \
  -f deploy/docker-compose.cloud.yml \
  up -d backend

docker exec -i deploy-postgres-1 psql -U ai_inspection -d ai_inspection < database/migrations/001_three_line_integration.sql
docker exec -i deploy-postgres-1 psql -U ai_inspection -d ai_inspection < database/migrations/002_device_management_crud.sql
```

## 验证结果

- backend 是否重新 build：是。
- backend 是否重新 up：是。
- 健康检查结果：`GET /api/health` 返回 HTTP 200。
- `GET /api/devices`：返回 HTTP 200，字段包含 P0-REAL 设备管理字段。
- `POST /api/devices`：返回 HTTP 200，不再返回 405。
- `POST /api/video/sessions` with `ch-camera-a1`：返回 HTTP 200。
- `POST /api/ai/tasks` with `ch-camera-a1`：返回 HTTP 200。
- unknown channelId：返回 HTTP 404，message 为 `Video channel not found: unknown-channel-id`，不再返回 500。
- 审计日志：已看到 `DEVICE_CREATE`、`VIDEO_SESSION_CREATE`、`AI_TASK_CREATE`。

## 服务影响

- backend 容器重新创建，短暂重启。
- frontend、ai-service、edge-simulator、postgres、redis、minio、mqtt、video-service 保持运行。
- 未删除 PostgreSQL、MinIO、MQTT、SRS 数据卷。
- 未开放 MinIO/EMQX 控制台公网访问。

## 问题

- 初次部署后发现云端 PostgreSQL volume 保留旧 schema，导致 `GET /api/devices` 500。
- 已通过执行现有迁移脚本补齐 schema，未删除数据卷。
- Windows 到远端 SSH here-doc 验证 JSON 时出现引号转义问题，已改用 `scp` 上传临时 JSON 文件完成验证。
