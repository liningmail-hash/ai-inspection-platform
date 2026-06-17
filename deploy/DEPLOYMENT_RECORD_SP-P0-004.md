# SP-P0-004 Mock 联调环境部署记录

## 环境定位

- 用途：Mock 联调 / 演示环境
- 禁止：接入真实设备、写入真实设备密钥、作为生产环境使用
- 部署目录：`/opt/ai-inspection-platform/current`
- 部署方式：Docker Compose

## 服务器信息

- 公网 IP：`114.67.114.201`
- 系统：Ubuntu 24.04.2 LTS
- 内核：Linux 6.8.0-53-generic x86_64
- CPU/内存：4 核 / 7.8 GiB
- Docker：29.1.3
- Docker Compose：2.40.3

## 部署文件

- 基础编排：`deploy/docker-compose.yml`
- 云端 Mock 覆盖：`deploy/docker-compose.cloud.yml`
- 后端云端构建：`deploy/Dockerfile.backend-cloud`
- 前端云端构建：`deploy/Dockerfile.frontend-cloud`
- Python 服务云端构建：`deploy/Dockerfile.python-service-cloud`
- 环境变量文件：服务器 `deploy/.env`，仅包含 Mock 临时值，不纳入代码仓库

## 启动命令

```bash
cd /opt/ai-inspection-platform/current
docker compose --env-file deploy/.env \
  -f deploy/docker-compose.yml \
  -f deploy/docker-compose.cloud.yml \
  up -d --build
```

## 健康检查结果

- frontend：`http://114.67.114.201:5173`，HTTP 200
- backend：`http://114.67.114.201:8080/api/health`，HTTP 200
- ai-service：`http://114.67.114.201:8100/health`，HTTP 200
- edge-simulator：`http://114.67.114.201:8200/health`，HTTP 200
- postgres：`pg_isready` 正常
- redis：`redis-cli ping` 返回 `PONG`
- minio：`http://127.0.0.1:9000/minio/health/live` 正常
- mqtt：EMQX 节点状态正常
- video-service：SRS `http://127.0.0.1:1985/api/v1/versions` 正常

## 端口策略

公网开放：

- `5173`：前端
- `8080`：后端 API
- `8100`：AI 服务
- `8200`：边缘模拟器
- `22`：SSH 运维

仅服务器本机访问：

- `5432`：PostgreSQL
- `6379`：Redis
- `1883`：MQTT
- `18083`：EMQX 控制台
- `9000`：MinIO API
- `9001`：MinIO 控制台
- `1935`、`1985`、`8088`、`8000/udp`：SRS / video-service

## 注意事项

- 当前使用 root 完成一次性部署，不建议长期多人共用 root。
- 云端构建文件只调整 npm、pip、Maven 镜像源，不改变业务代码。
- Mock 环境 `.env` 使用临时值，生产环境必须更换。
- 如果需要公网访问 MinIO/EMQX 控制台，应通过临时白名单或 VPN，不建议直接开放公网。
