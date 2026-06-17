# 预览指南

当项目具备可视化成果时，优先按本文档预览。

## 当前可预览内容

### 0. Ubuntu 服务器预览

如果你提供 Ubuntu 环境，可以按 [Ubuntu 部署指南](UBUNTU_DEPLOYMENT.md) 部署：

```bash
bash deploy/ubuntu-check.sh
bash deploy/ubuntu-deploy.sh
```

部署后访问：

```text
http://<服务器IP>:5173
```

### 1. 前端工作台

启动：

```powershell
cd frontend
npm install
npm run dev
```

访问：

```text
http://localhost:5173
```

可看内容：

- 驾驶舱
- 视频监控
- AI巡检
- 告警中心
- 标注训练
- 模型管理
- 无人机巡检
- 设备管理
- 报表统计
- 系统配置

系统配置页当前已经可查看用户管理、角色管理、审计日志和演示登录账号。前端单独启动时使用 Mock 兜底数据；后端启动后会从 `/api/system/users`、`/api/system/roles`、`/api/system/audit-logs` 读取。

当前新增可预览内容：

- `AI巡检`：任务队列、即时任务创建入口、任务下发链路。
- `模型管理`：算法参数、阈值、灵敏度、启停状态。
- `报表统计`：巡检报告、素材管理、事件地图分布。

顶部状态含义：

- `Mock兜底`：只启动了前端，页面使用本地演示数据。
- `API联调`：后端已启动，页面从中心后端读取数据。

### 2. 后端 API 文档

安装 JDK 21 和 Maven 后启动：

```powershell
cd backend
mvn spring-boot:run
```

访问：

```text
http://localhost:8080/docs
http://localhost:8080/api/health
```

### 3. Docker 全栈预览

安装 Docker Desktop 后：

```powershell
copy deploy\.env.example .env
docker compose -f deploy\docker-compose.yml up -d --build
```

访问：

```text
前端: http://localhost:5173
后端: http://localhost:8080/docs
AI服务: http://localhost:8100/health
边缘模拟器: http://localhost:8200/health
MinIO控制台: http://localhost:9001
```

Docker 模式下后端启用 `postgres` Profile，会从 PostgreSQL 读取 `database/init.sql` 种子数据。

## 我会主动提醒你的节点

后续只要出现以下成果，我会明确提示你可以预览：

- 新前端页面或关键交互完成。
- 后端 API 可通过 Swagger/OpenAPI 查看。
- Docker Compose 可完整启动。
- AI 服务或边缘模拟器有可调用演示接口。
- 产生截图、报表、巡检报告等可视化产物。
