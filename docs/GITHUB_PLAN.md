# GitHub 私有仓库发布计划

## 安全说明

不要把 Google/GitHub 密码告诉任何工具或写入项目文件。GitHub 登录建议使用浏览器 OAuth、GitHub CLI 登录，或 Personal Access Token。仓库先设为 Private，正式开源前再切换 Public。

## 本地仓库状态

当前目录已经初始化为 Git 仓库，主分支为 `main`。首次提交前需要确认 Git 用户名和邮箱：

```powershell
git config --global user.name "你的名字"
git config --global user.email "你的邮箱"
```

## 私有仓库创建流程

1. 安装 GitHub CLI。
2. 执行浏览器登录：

```powershell
gh auth login
```

3. 检查待提交文件：

```powershell
git status --short
git status --ignored --short
```

4. 首次提交：

```powershell
git add .
git commit -m "chore: scaffold ai inspection platform"
```

5. 创建 GitHub Private 仓库并推送：

```powershell
gh repo create ai-inspection-platform --private --source . --remote origin --push
```

6. 在 GitHub 页面确认仓库可见性是 Private。
7. 开启 GitHub Secret Scanning 和 Dependabot。
8. 后续稳定并完成脱敏后，再切换 Public。

## 开源前必须补齐

- `LICENSE`：已补齐 Apache-2.0。
- `CONTRIBUTING.md`：已补齐。
- `SECURITY.md`：已补齐。
- `README.md`：需随功能持续更新。
- 脱敏的演示数据。
- 一键启动文档。
- 截图和功能清单。
- CI 检查：前端构建、后端测试、Python 语法检查。

## 公开前检查清单

- `.env` 没有提交。
- `node_modules`、`dist`、日志、缓存没有提交。
- 原始设计文件没有提交，除非明确拥有可公开授权。
- 没有真实 RTSP、GB28181、JT/T 1078 地址。
- 没有客户名称、现场坐标、视频截图、告警证据。
- 没有 Google/GitHub/云服务账号密码。
