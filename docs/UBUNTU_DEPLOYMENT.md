# Ubuntu 部署指南

本文档用于在 Ubuntu 服务器上部署 AI 巡检平台，方便预览整体效果和发现功能缺口。

## 推荐服务器配置

试点预览最低配置：

- Ubuntu 22.04 LTS 或 24.04 LTS
- 4 核 CPU
- 8 GB 内存
- 80 GB 磁盘
- Docker Engine + Docker Compose Plugin

如果要测试 AI 训练或 GPU 推理，建议：

- NVIDIA GPU
- NVIDIA Driver
- NVIDIA Container Toolkit
- Python 3.12 环境

当前工程预览阶段不强制要求 GPU。

## 安全建议

- 不要把服务器 root 密码、GitHub 密码、Google 密码写入聊天或项目文件。
- 推荐使用 SSH Key 或临时部署账号。
- `.env` 中的默认密码只适合本地预览，生产前必须修改。
- 第一次公开到外网前，请确认没有真实摄像头地址、Token、客户数据。

## 1. 环境检查

进入项目目录后执行：

```bash
bash deploy/ubuntu-check.sh
```

检查内容包括：

- Git
- Docker
- Docker Compose
- Node.js
- Java/Maven
- Python
- 常用端口占用
- 磁盘和内存

## 2. 部署

```bash
bash deploy/ubuntu-deploy.sh
```

脚本会：

- 检查 Docker 和 Docker Compose
- 如果没有 `.env`，从 `deploy/.env.example` 创建
- 使用 Docker Compose 构建并启动服务

## 3. 预览地址

如果在服务器本机浏览：

```text
前端: http://localhost:5173
后端健康: http://localhost:8080/api/health
后端文档: http://localhost:8080/docs
AI服务: http://localhost:8100/health
边缘模拟器: http://localhost:8200/health
MinIO控制台: http://localhost:9001
```

如果从你的电脑访问服务器，把 `localhost` 换成服务器 IP。

```text
http://<服务器IP>:5173
```

## 4. 查看服务状态

```bash
docker compose -f deploy/docker-compose.yml ps
docker compose -f deploy/docker-compose.yml logs -f backend
docker compose -f deploy/docker-compose.yml logs -f frontend
```

## 5. 停止服务

```bash
bash deploy/ubuntu-stop.sh
```

## 6. 清理数据

预览环境需要清空数据库和对象存储时执行：

```bash
docker compose -f deploy/docker-compose.yml down -v
```

注意：这会删除 PostgreSQL、Redis、MinIO 的数据卷。

## 当前可观察的功能缺口

部署后可以重点检查这些点：

- 前端页面是否完整，导航是否符合你的业务理解。
- 顶部是否显示 `API联调`。
- 告警按钮是否能触发后端动作。
- 模型灰度/回滚按钮是否能触发后端动作。
- 后端 Swagger 中接口是否清晰。
- Docker 启动后 PostgreSQL 种子数据是否正确进入前端。

已知还没完成的真实能力：

- 真实海康/大华 SDK 接入。
- 真实 GB28181/SIP 服务。
- 真实 JT/T 1078 部标视频接入。
- 真实无人机机场厂家协议。
- 真实视频播放链路。
- 真实 AI 模型训练与 GPU 推理。
