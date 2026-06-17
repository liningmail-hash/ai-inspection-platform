# SP-P0-005 云端 Mock 联调环境安全加固记录

## 加固范围

- 环境：云端 Mock 联调 / 演示环境
- 服务器：`114.67.114.201`
- 部署目录：`/opt/ai-inspection-platform/current`
- 业务影响：未停止业务容器，未修改业务代码

## 账号策略

- 运维账号：`deploy`
- 登录方式：SSH key
- 公钥指纹：`SHA256:aUXnSJv2CpvERGQFhYPz1/dlWgA3Td94y6u48FVGlFA`
- 用户组：`sudo`、`docker`
- sudo 策略：`deploy ALL=(ALL) NOPASSWD:ALL`
- 本地私钥路径：`~/.ssh/ai_platform_mock_deploy_ed25519`

## SSH 策略

配置文件：`/etc/ssh/sshd_config.d/99-ai-platform-hardening.conf`

```text
PubkeyAuthentication yes
PasswordAuthentication no
KbdInteractiveAuthentication no
PermitRootLogin prohibit-password
```

- root 密码：已锁定
- root 密码登录：已禁用
- deploy key 登录：已验证通过

## 权限验证

`deploy` 已验证可执行：

```bash
cd /opt/ai-inspection-platform/current
docker compose --env-file deploy/.env \
  -f deploy/docker-compose.yml \
  -f deploy/docker-compose.cloud.yml \
  ps

docker compose --env-file deploy/.env \
  -f deploy/docker-compose.yml \
  -f deploy/docker-compose.cloud.yml \
  logs --tail=30 backend
```

## 端口策略

公网开放：

- `22`：SSH，仅 key 登录
- `5173`：前端
- `8080`：后端 API
- `8100`：AI 服务
- `8200`：边缘模拟器

仅服务器本机访问：

- `5432`：PostgreSQL
- `6379`：Redis
- `1883`：MQTT
- `18083`：EMQX 控制台
- `9000`：MinIO API
- `9001`：MinIO 控制台
- `1935`、`1985`、`8088`、`8000/udp`：SRS / video-service

## 防火墙检查

- UFW：inactive
- 当前端口隔离主要由 Docker 端口绑定到 `127.0.0.1` 实现
- 公网验证：`9001`、`18083` 不可访问

## 注意事项

- 不记录任何明文密码、SSH 私钥、真实设备密钥。
- 当前 `deploy` 具备免密 sudo，适合联调期快速运维；生产环境建议改为最小权限 sudo。
- 如需多人协作，应为每个人单独配置 SSH key，不共用 `deploy` 私钥。
