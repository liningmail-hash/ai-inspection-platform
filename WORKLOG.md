<!--
  WORKLOG.md — 开发工作日志（AI/开发者必读）

  目的：记录每次开发会话的目标、决策、变更和下一步计划。
  后续 AI 或开发者接手时，先读此文件即可了解完整上下文。

  使用规范：
  - 每次开发会话开始，在顶部添加新条目
  - 每次版本发布，同步更新 CHANGELOG.md 和 git tag
  - 重大决策必须在 "决策记录" 中留痕
-->

# 开发工作日志

---

## 会话 #1 — 2026-06-17

### 参与者
- 用户 + AI 助手（Codex）

### 完成事项
1. **Git 仓库初始化与发布**
   - 初始化 Git 仓库，配置 .gitignore（排除 node_modules、.venv、target/、*.tar.gz 等）
   - 创建 GitHub 仓库：[liningmail-hash/ai-inspection-platform](https://github.com/liningmail-hash/ai-inspection-platform)
   - 初始提交 163 个文件，18518 行代码
   - 打标签 `v0.1.0`，Git 提交者：`AI Platform Dev <liningmail@gmail.com>`

2. **服务器健康检查（114.67.114.201）**
   - SSH 端口 22 仅接受公钥认证，本地三个密钥均未授权（待解决）
   - 4 个核心服务全部正常运行：frontend(5173)、backend(8080)、ai-service(8100)、edge-simulator(8200)
   - 后端 API 数据完整：13 台设备、1 架无人机在线、1 辆车在线、3 条告警、3 个巡检任务
   - **安全问题**：内部端口（5432/6379/1883/9000/9001/18083）全部对外暴露，返回 502，但 TCP 连接已建立

3. **建立文档追踪体系**
   - 创建 WORKLOG.md（本文件）
   - 创建 CHANGELOG.md
   - 创建 VERSION 文件

### 当前版本
- **v0.1.0** — 初始架构，Mock 联调环境已部署

### 决策记录
- D001：Git 提交者使用 `AI Platform Dev`（非个人实名）
- D002：版本号遵循语义化版本 SemVer 2.0（主版本.次版本.修订号）
- D003：所有开发目标、版本变更、决策必须写入文件并提交 Git

### 待解决问题
- [ ] SSH 无权限登录 114.67.114.201（需云控制台添加公钥）
- [ ] 内部端口对外暴露（需配置防火墙/安全组）
- [ ] HTTPS 未启用（全部 HTTP）

### 下一步
- 确定本次迭代的开发目标
- 修复服务器安全问题（端口封锁 + SSH 密钥）


---

## 会话 #2 — 2026-06-17

### 目标
- 全面测试当前系统，建立基线
- 根据测试结果确定修改方向
- 开始系统迭代开发

### 测试基线
（见下方测试结果）

### 当前版本
- v0.1.0
---

## 版本策略

| 版本号 | Git Tag | 发布方式 |
|--------|---------|---------|
| v0.1.0 ~ v0.9.x | 开发阶段 | `git tag -a vX.Y.Z` + `git push --tags` |
| v1.0.0 | 正式发布 | 同上 + GitHub Release |

### 版本命名
```
v<主版本>.<次版本>.<修订号>
```
- 主版本：重大架构变更、不兼容 API 修改
- 次版本：新功能、向后兼容
- 修订号：Bug 修复、文档更新

### 每次迭代流程
```
1. 在 WORKLOG.md 顶部添加新会话条目，写明目标
2. 开发完成后更新 CHANGELOG.md
3. 更新 VERSION 文件中的版本号
4. git add -A && git commit -m "feat/chore/fix: 描述"
5. git tag -a vX.Y.Z -m "版本说明"
6. git push origin main --tags
```

