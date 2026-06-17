# FE-REAL-P0-001-R1 Cloud Frontend Deployment Record

任务编号：FE-REAL-P0-001-R1

执行时间：2026-06-16 17:22-17:28

执行人：机动人员

部署账号：`deploy`

部署目录：`/opt/ai-inspection-platform/current`

## 执行内容

- 已在本地确认前端设备管理页保留真实接口调用能力：
  - `createDevice`
  - `updateDevice`
  - `deleteDevice`
  - `testDevice`
  - `syncDeviceChannels`
- 已修正云端前端页面重点项：
  - 顶部状态不再显示“Mock兜底”
  - 设备管理主按钮文案改为“接入设备”
  - 页面保留编辑入口、删除入口、测试连接、同步通道入口
  - 设备页失败提示改为真实接口反馈
  - 系统配置页数据来源兜底文案改为“接口暂不可用”
- 已同步前端代码到云端部署目录。
- 已执行前端镜像重新构建与容器重建。

## 执行命令摘要

```bash
scp -i ~/.ssh/ai_platform_mock_deploy_ed25519 -r frontend deploy@114.67.114.201:/opt/ai-inspection-platform/current/

docker compose --env-file deploy/.env \
  -f deploy/docker-compose.yml \
  -f deploy/docker-compose.cloud.yml \
  build frontend

docker compose --env-file deploy/.env \
  -f deploy/docker-compose.yml \
  -f deploy/docker-compose.cloud.yml \
  up -d frontend

docker compose --env-file deploy/.env \
  -f deploy/docker-compose.yml \
  -f deploy/docker-compose.cloud.yml \
  ps frontend

docker compose --env-file deploy/.env \
  -f deploy/docker-compose.yml \
  -f deploy/docker-compose.cloud.yml \
  logs --tail=120 frontend
```

## 验证结果

- 本地 `npm run build`：通过。
- 云端 `docker compose build frontend`：通过。
- 云端 `docker compose up -d frontend`：通过。
- 云端 `docker compose ps frontend`：容器运行中，端口 `5173->80/tcp`。
- 公网 `http://114.67.114.201:5173`：HTTP 200。
- 云端首页引用新产物：`assets/index-DhMwMR-S.js`。
- 本地构建产物静态串检查通过，确认包含：
  - `接入设备`
  - `中心后端联调`
  - `接口暂不可用`
- 本地构建产物静态串检查未再发现页面展示层 `Mock 兜底` 文案。

## 服务影响

- 仅重建前端容器。
- backend、ai-service、edge-simulator、postgres、redis、minio、mqtt、video-service 保持运行。

## 备注

- PowerShell 下远端 `grep` 中文关键字转义不稳定，最终以本地构建产物静态串检查、云端新资源文件名、容器重建日志和公网首页返回共同确认部署生效。
- 最终页面回归仍需 QA 在浏览器侧继续验证弹窗、编辑、删除、测试连接、同步通道交互。
