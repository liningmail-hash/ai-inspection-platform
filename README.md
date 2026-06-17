# AI巡检平台

AI巡检平台是一个面向园区、工厂、能源站点等场景的开源 AI 视频巡检与无人机巡检系统。平台目标是把摄像头接入、边缘 AI 推理、告警处置、样本标注、模型训练、模型发布、无人机固定航线巡检串成一条可持续迭代的闭环。

当前仓库处于一期工程骨架阶段，重点是建立清晰架构、统一接口、可运行前端工作台和后续可扩展的工程规范。

## 核心能力

- 摄像头接入：海康、大华、ONVIF、RTSP、GB/T 28181、JT/T 1078 适配层预留。
- 视频 AI 巡检：巡检计划、点位、算法、ROI、告警规则、时段策略。
- 告警闭环：告警分级、证据图/视频片段、确认、误报、派单、关闭。
- 标注训练：样本采集、数据集、标注任务、训练任务、模型评估、版本管理。
- 模型发布：候选、灰度、生产、回滚，发布到边缘节点。
- 无人机巡检：机场状态、固定航线、无人机视频 AI、遥测数据、巡检报告。
- 私有云边协同：中心平台管业务，边缘节点管接入、推理和断网缓存。

## 技术栈

| 模块 | 技术 |
|---|---|
| 前端 | Vue 3、TypeScript、Vite、Lucide Icons |
| 中心后端 | Java 21、Spring Boot 3、Spring Web、Actuator、OpenAPI |
| AI 服务 | Python 3.12、FastAPI、PyTorch/ONNX Runtime/TensorRT 预留 |
| 边缘节点 | Python FastAPI 模拟器，后续可替换为真实边缘 Agent |
| 数据库 | PostgreSQL |
| 缓存/消息 | Redis、MQTT 预留 |
| 对象存储 | MinIO |
| 部署 | Docker Compose，二期支持 Kubernetes |

## 仓库结构

```text
.
├── frontend/          # Vue 前端工作台
├── backend/           # Spring Boot 中心后端
├── ai-service/        # AI 推理/训练服务
├── edge-simulator/    # 边缘节点模拟器
├── database/          # PostgreSQL 初始化脚本
├── deploy/            # Docker Compose 和环境模板
├── docs/              # 架构、流程、安装、接口、治理文档
└── 设计文件/           # 本地设计稿导出，默认不提交
```

## 快速开始

详细步骤见 [安装与启动文档](docs/INSTALLATION.md)。

前端本地启动：

```powershell
cd frontend
npm install
npm run dev
```

前端构建检查：

```powershell
cd frontend
npm run build
```

AI 服务：

```powershell
cd ai-service
python -m venv .venv
.\.venv\Scripts\Activate.ps1
pip install -r requirements.txt
uvicorn app.main:app --reload --port 8100
```

边缘模拟器：

```powershell
cd edge-simulator
python -m venv .venv
.\.venv\Scripts\Activate.ps1
pip install -r requirements.txt
uvicorn app.main:app --reload --port 8200
```

后端：

```powershell
cd backend
mvn spring-boot:run
```

Docker Compose：

```powershell
copy deploy\.env.example .env
docker compose -f deploy\docker-compose.yml up -d --build
```

## 文档导航

- [总体方案与项目流程](docs/PROJECT_PLAN.md)
- [项目状态与部署决策报告](docs/PROJECT_STATUS.md)
- [功能比对与缺口分析](docs/FUNCTION_COMPARISON.md)
- [系统组件规划](docs/COMPONENTS.md)
- [架构设计](docs/ARCHITECTURE.md)
- [接口契约](docs/API_CONTRACTS.md)
- [联调指南](docs/INTEGRATION_GUIDE.md)
- [开发规范](docs/DEVELOPMENT_GUIDE.md)
- [团队协同与总指挥执行手册](docs/TEAM_COMMAND_CENTER.md)
- [安装与启动](docs/INSTALLATION.md)
- [Ubuntu 部署指南](docs/UBUNTU_DEPLOYMENT.md)
- [预览指南](docs/PREVIEW_GUIDE.md)
- [运维与部署](docs/OPERATIONS.md)
- [UI 实现约束](docs/UI_GUIDELINES.md)
- [路线图](docs/ROADMAP.md)
- [GitHub 私有仓库发布计划](docs/GITHUB_PLAN.md)
- [安全策略](SECURITY.md)
- [贡献指南](CONTRIBUTING.md)

## 项目状态

- 已完成一期工程骨架。
- 前端工作台已覆盖 10 个核心入口。
- 后端、AI服务、边缘模拟器已具备第一批接口形状。
- 数据库、Compose、开源基础材料已建立。
- 下一阶段重点：前端从 Mock 切换到后端 API，后端接入 PostgreSQL 持久化，跑通告警与训练发布闭环。

## 开源说明

项目采用 Apache-2.0 License。正式公开前请确认仓库中没有真实设备地址、账号密码、密钥、客户数据、现场截图或未授权设计素材。建议先使用 GitHub Private 仓库维护，稳定后再切换 Public。
