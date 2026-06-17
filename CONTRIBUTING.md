# 贡献指南

感谢关注 AI 巡检平台。当前项目处于一期工程骨架阶段，最重要的是把架构、接口、代码质量和文档基础打稳。

## 贡献方向

优先欢迎以下方向：

- 前端工作台组件化和 API 联调。
- 后端领域模型、数据库持久化、接口测试。
- AI 服务训练/推理任务抽象。
- 边缘节点模拟器和真实边缘 Agent 设计。
- 摄像头协议、无人机厂家适配器。
- 文档、部署脚本、验收测试。

## 开发原则

- 保持功能边界清楚，不把平台做成臃肿大中台。
- 摄像头、无人机、AI算法都通过适配层扩展，不把具体厂商逻辑写死在核心流程里。
- 前端遵循 `docs/UI_GUIDELINES.md` 的深色工业工作台风格。
- 代码遵循 `docs/DEVELOPMENT_GUIDE.md`。
- 接口遵循 `docs/API_CONTRACTS.md`，破坏性变更必须说明原因和迁移方式。
- 不提交真实设备地址、账号、密码、Token、视频流地址或客户数据。

## 分支规范

- `main`：稳定主分支。
- `feature/<name>`：新功能。
- `fix/<name>`：缺陷修复。
- `docs/<name>`：文档。
- `chore/<name>`：工程配置。

## 提交信息

建议格式：

```text
type: short summary
```

常用类型：

- `feat:` 新功能
- `fix:` 修复
- `docs:` 文档
- `chore:` 工程配置
- `test:` 测试
- `refactor:` 重构

## 本地检查

前端：

```powershell
cd frontend
npm install
npm run build
```

后端：

```powershell
cd backend
mvn test
```

AI 服务：

```powershell
cd ai-service
python -m py_compile app\main.py
```

边缘模拟器：

```powershell
cd edge-simulator
python -m py_compile app\main.py
```

## Pull Request 要求

PR 描述至少包含：

- 改了什么。
- 为什么要改。
- 影响哪些模块。
- 怎么验证。
- 是否涉及接口、数据库、部署或安全变更。

如果涉及 UI，请附截图或说明对应页面。

## 不接受的提交

- 大段复制粘贴但没有抽象的代码。
- 把真实账号、密码、Token、设备地址提交进仓库。
- 绕过适配器直接在业务服务里写厂商 SDK 调用。
- 没有说明原因的破坏性接口变更。
- 与一期目标无关的大而泛功能。
