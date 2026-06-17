# 团队执行命令手册

更新时间：2026-06-16

本文档只放可执行命令和检查命令。任务目标、文件范围、回报格式见 `docs/TASK_DISPATCH.md`。

执行原则：

- 先看 `docs/TASK_DISPATCH.md`，确认自己是否有任务。
- 再看本文档对应任务编号执行命令。
- 不要执行没有分配给自己的任务命令。
- 不要在文档或代码里记录真实密码、真实 token、真实私钥。
- 完成、失败或阻塞后，必须把结果追加写入 `docs/TASK_FEEDBACK.md`。

## 1. 通用路径

Windows 本地项目目录：

```powershell
cd F:\我的编程\AI编程\AI平台开发
```

云端部署目录：

```bash
cd /opt/ai-inspection-platform/current
```

云端访问地址：

```text
frontend: http://114.67.114.201:5173
backend:  http://114.67.114.201:8080/api
ai:       http://114.67.114.201:8100
edge:     http://114.67.114.201:8200
```

## 2. BE-REAL-P0-002

负责人：程序员 A

### 2.1 本地 Java 环境检查

PowerShell：

```powershell
java -version
javac -version
$env:JAVA_HOME
```

如果 `JAVA_HOME` 为空，但机器已安装 Microsoft OpenJDK 21，可在当前 PowerShell 临时设置：

```powershell
$env:JAVA_HOME = "C:\Program Files\Microsoft\jdk-21.0.11.10-hotspot"
$env:Path = "$env:JAVA_HOME\bin;$env:Path"
java -version
javac -version
```

### 2.2 后端编译和测试

PowerShell：

```powershell
cd F:\我的编程\AI编程\AI平台开发\backend
.\mvnw.cmd -version
.\mvnw.cmd test
```

如果 Maven Wrapper 下载失败，需要在回报里说明网络或 Maven 源问题。

### 2.3 后端本地启动检查

PowerShell：

```powershell
cd F:\我的编程\AI编程\AI平台开发\backend
.\mvnw.cmd spring-boot:run
```

另开一个 PowerShell：

```powershell
Invoke-RestMethod http://localhost:8080/api/health
Invoke-RestMethod http://localhost:8080/api/devices
```

### 2.4 云端容器状态检查

在有云端 SSH key 的机器上执行：

```bash
ssh deploy@114.67.114.201
cd /opt/ai-inspection-platform/current
docker compose --env-file deploy/.env -f deploy/docker-compose.yml -f deploy/docker-compose.cloud.yml ps
docker compose --env-file deploy/.env -f deploy/docker-compose.yml -f deploy/docker-compose.cloud.yml logs --tail=80 backend
```

### 2.5 云端后端健康检查

PowerShell：

```powershell
Invoke-RestMethod http://114.67.114.201:8080/api/health
Invoke-RestMethod http://114.67.114.201:8080/api/devices
Invoke-RestMethod http://114.67.114.201:8080/api/video/channels
Invoke-RestMethod http://114.67.114.201:8080/api/system/audit-logs
```

### 2.6 设备 CRUD 冒烟命令

PowerShell：

```powershell
$base = "http://114.67.114.201:8080/api"

$device = @{
  name = "QA真实业务摄像头"
  sourceType = "camera"
  vendor = "MOCK_VENDOR"
  protocol = "RTSP"
  endpoint = "rtsp://example.local/qa-camera"
  credentialRef = "qa-credential-ref"
  location = "QA测试区域"
  edgeNodeId = "edge-mock-01"
  status = "offline"
  streamUrl = "rtsp://example.local/qa-camera/stream"
} | ConvertTo-Json

$created = Invoke-RestMethod "$base/devices" -Method Post -ContentType "application/json" -Body $device
$created

$id = $created.id

Invoke-RestMethod "$base/devices"
Invoke-RestMethod "$base/devices/$id/test" -Method Post
Invoke-RestMethod "$base/devices/$id/sync-channels" -Method Post

$updated = @{
  name = "QA真实业务摄像头-已编辑"
  sourceType = "camera"
  vendor = "MOCK_VENDOR"
  protocol = "RTSP"
  endpoint = "rtsp://example.local/qa-camera-edited"
  credentialRef = "qa-credential-ref"
  location = "QA测试区域-已编辑"
  edgeNodeId = "edge-mock-01"
  status = "offline"
  streamUrl = "rtsp://example.local/qa-camera-edited/stream"
} | ConvertTo-Json

Invoke-RestMethod "$base/devices/$id" -Method Put -ContentType "application/json" -Body $updated
Invoke-RestMethod "$base/devices/$id" -Method Delete
Invoke-RestMethod "$base/system/audit-logs"
```

### 2.7 稳定 Mock ID 回归命令

PowerShell：

```powershell
$base = "http://114.67.114.201:8080/api"

$sessionBody = @{
  channelId = "ch-camera-a1"
  protocol = "WebRTC"
} | ConvertTo-Json

Invoke-RestMethod "$base/video/sessions" -Method Post -ContentType "application/json" -Body $sessionBody

$aiTaskBody = @{
  channelId = "ch-camera-a1"
  modelId = "helmet-detection-v1"
  taskType = "helmet"
} | ConvertTo-Json

Invoke-RestMethod "$base/ai/tasks" -Method Post -ContentType "application/json" -Body $aiTaskBody
```

未知通道负向测试：

```powershell
$base = "http://114.67.114.201:8080/api"

$badSessionBody = @{
  channelId = "unknown-channel-id"
  protocol = "WebRTC"
} | ConvertTo-Json

try {
  Invoke-RestMethod "$base/video/sessions" -Method Post -ContentType "application/json" -Body $badSessionBody
} catch {
  $_.Exception.Response.StatusCode.value__
  $_.ErrorDetails.Message
}

$badAiTaskBody = @{
  channelId = "unknown-channel-id"
  modelId = "helmet-detection-v1"
  taskType = "helmet"
} | ConvertTo-Json

try {
  Invoke-RestMethod "$base/ai/tasks" -Method Post -ContentType "application/json" -Body $badAiTaskBody
} catch {
  $_.Exception.Response.StatusCode.value__
  $_.ErrorDetails.Message
}
```

## 3. FE-REAL-P0-001-R1

负责人：程序员 B

该任务当前等待后端完成后启动。

### 3.1 前端构建检查

PowerShell：

```powershell
cd F:\我的编程\AI编程\AI平台开发\frontend
npm run build
```

### 3.2 前端本地运行

PowerShell：

```powershell
cd F:\我的编程\AI编程\AI平台开发\frontend
npm run dev
```

打开：

```text
http://localhost:5173
```

### 3.3 前端接口入口检查

PowerShell：

```powershell
cd F:\我的编程\AI编程\AI平台开发
rg "createDevice|updateDevice|deleteDevice|testDevice|syncDeviceChannels" frontend/src
```

## 3.4 FE-REAL-P0-001-R2

负责人：程序员 B

目标：修复云端设备管理页新增设备 `Failed to fetch`，并清理残留 Mock 文案。

### 3.4.1 先检查任务文档

PowerShell：

```powershell
Get-Content -Raw -Encoding utf8 docs\TASK_DISPATCH.md
Get-Content -Raw -Encoding utf8 docs\EXECUTION_COMMANDS.md
```

### 3.4.2 排查前端 API base

PowerShell：

```powershell
cd F:\我的编程\AI编程\AI平台开发
rg -n "localhost:8080|window.location|import.meta.env|VITE|apiBase|baseURL" frontend/src
Get-Content -Raw -Encoding utf8 frontend\src\services\api.ts
```

重点确认：
- 云端浏览器访问 `http://114.67.114.201:5173` 时，前端请求目标应为 `http://114.67.114.201:8080/api`
- 不允许继续落到浏览器本机 `http://localhost:8080/api`

### 3.4.3 本地构建验证

PowerShell：

```powershell
cd F:\我的编程\AI编程\AI平台开发\frontend
npm run build
```

### 3.4.4 云端前端部署

在有 `deploy` SSH key 的机器上执行：

```powershell
scp -i C:\Users\Ning\.ssh\ai_platform_mock_deploy_ed25519 -r F:\我的编程\AI编程\AI平台开发\frontend deploy@114.67.114.201:/opt/ai-inspection-platform/current/
```

```bash
ssh deploy@114.67.114.201
cd /opt/ai-inspection-platform/current
docker compose --env-file deploy/.env -f deploy/docker-compose.yml -f deploy/docker-compose.cloud.yml build frontend
docker compose --env-file deploy/.env -f deploy/docker-compose.yml -f deploy/docker-compose.cloud.yml up -d frontend
docker compose --env-file deploy/.env -f deploy/docker-compose.yml -f deploy/docker-compose.cloud.yml ps frontend
docker compose --env-file deploy/.env -f deploy/docker-compose.yml -f deploy/docker-compose.cloud.yml logs --tail=120 frontend
```

### 3.4.5 云端结果自查

PowerShell：

```powershell
Invoke-WebRequest http://114.67.114.201:5173 | Select-Object StatusCode
Invoke-RestMethod http://114.67.114.201:8080/api/health
```

页面需自查：
- 不显示 `Mock兜底`
- 主按钮为“接入设备”
- 新增设备提交不再报 `Failed to fetch`

### 3.4.6 回填要求

把结果追加写入：

```text
docs/TASK_FEEDBACK.md
```

如执行了云端前端重部署，同时补写：

```text
deploy/DEPLOYMENT_RECORD_FE-REAL-P0-001-R2.md
```

## 3.5 BE-REAL-P0-003

负责人：程序员 A

目标：把真实视频会话、AI 任务、告警事件、证据和审计串成后端闭环。

### 3.5.1 本地编译测试

PowerShell：

```powershell
cd F:\我的编程\AI编程\AI平台开发\backend
.\mvnw.cmd test
```

### 3.5.2 接口契约检查

PowerShell：

```powershell
cd F:\我的编程\AI编程\AI平台开发
rg -n "video/sessions|ai/tasks|alarms|evidence|audit" backend/src docs/API_CONTRACTS.md
```

### 3.5.3 回填要求

把结果追加写入：

```text
docs/TASK_FEEDBACK.md
```

如修改了接口契约，同时更新：

```text
docs/API_CONTRACTS.md
```

## 3.6 FE-REAL-P0-002

负责人：程序员 B

目标：把视频墙、告警中心、无人机页接到真实后端闭环。

### 3.6.1 前端构建

PowerShell：

```powershell
cd F:\我的编程\AI编程\AI平台开发\frontend
npm run build
```

### 3.6.2 页面入口检查

PowerShell：

```powershell
cd F:\我的编程\AI编程\AI平台开发
rg -n "VideoWallView|AlarmView|DroneView|VehicleView|alarm|video" frontend/src
```

### 3.6.3 回填要求

把结果追加写入：

```text
docs/TASK_FEEDBACK.md
```

## 3.7 SP-REAL-P0-004

负责人：机动人员

目标：补齐真实视频试点资料。

### 3.7.1 文档检查

PowerShell：

```powershell
Get-Content -Raw -Encoding utf8 docs\REAL_DEVICE_ACCESS_HIKVISION.md
Get-Content -Raw -Encoding utf8 docs\DEVICE_SDK_REQUIREMENTS.md
```

### 3.7.2 回填要求

把结果追加写入：

```text
docs/TASK_FEEDBACK.md
```

## 4. QA-REAL-P0-002

负责人：测试工程师

目标：验收真实视频、AI 任务、告警闭环。

### 4.1 健康检查

PowerShell：

```powershell
Invoke-RestMethod http://114.67.114.201:8080/api/health
Invoke-RestMethod http://114.67.114.201:8100/health
Invoke-RestMethod http://114.67.114.201:8200/health
```

### 4.2 视频与告警接口复核

PowerShell：

```powershell
$base = "http://114.67.114.201:8080/api"
Invoke-RestMethod "$base/video/channels"
Invoke-RestMethod "$base/system/audit-logs"
```

### 4.3 回填要求

把结果追加写入：

```text
docs/TASK_FEEDBACK.md
```

## 4. QA-REAL-P0-001

负责人：测试工程师

该任务当前等待 BE-REAL-P0-002 完成后启动。

### 4.1 云端健康检查

PowerShell：

```powershell
Invoke-RestMethod http://114.67.114.201:8080/api/health
Invoke-RestMethod http://114.67.114.201:8100/health
Invoke-RestMethod http://114.67.114.201:8200/health
```

前端页面：

```text
http://114.67.114.201:5173
```

### 4.2 设备 CRUD 验收

PowerShell：

```powershell
$base = "http://114.67.114.201:8080/api"
Invoke-RestMethod "$base/devices"
Invoke-RestMethod "$base/video/channels"
Invoke-RestMethod "$base/system/audit-logs"
```

QA 需要通过页面完成新增、编辑、删除、测试、同步，再用接口复核数据和审计日志。

### 4.3 稳定 Mock ID 回归

PowerShell：

```powershell
$base = "http://114.67.114.201:8080/api"

$sessionBody = @{
  channelId = "ch-camera-a1"
  protocol = "WebRTC"
} | ConvertTo-Json
Invoke-RestMethod "$base/video/sessions" -Method Post -ContentType "application/json" -Body $sessionBody

$aiTaskBody = @{
  channelId = "ch-camera-a1"
  modelId = "helmet-detection-v1"
  taskType = "helmet"
} | ConvertTo-Json

## 4.4 QA-REAL-P0-001-R4

负责人：测试工程师

目标：对云端新前端版本做最终页面回归，确认关闭 `P0-QA-REAL-001`。

### 4.4.1 先检查云端服务

PowerShell：
```powershell
Invoke-RestMethod http://114.67.114.201:8080/api/health
Invoke-RestMethod http://114.67.114.201:8100/health
Invoke-RestMethod http://114.67.114.201:8200/health
```

页面地址：
```text
http://114.67.114.201:5173
```

### 4.4.2 页面必验项

打开设备管理页，逐项核查：

1. 页面顶部不再显示 `Mock兜底`。
2. 页面存在“接入设备”按钮。
3. 点击“接入设备”后弹出真实新增表单。
4. 表单至少包含：
   - `name`
   - `sourceType`
   - `vendor`
   - `endpoint`
   - `credentialRef`
   - `location`
   - `edgeNodeId`
5. 设备行存在：
   - 编辑入口
   - 删除入口
   - 测试连接入口
   - 同步通道入口
6. 页面操作后有真实成功/失败反馈，不是静态演示提示。

### 4.4.3 页面回归动作

先用页面完成一轮：

1. 新增 1 台 camera 设备。
2. 编辑该设备名称或 endpoint。
3. 执行测试连接。
4. 执行同步通道。
5. 刷新页面，确认数据仍存在。
6. 删除该设备，确认页面列表移除。

### 4.4.4 接口复核

PowerShell：
```powershell
$base = "http://114.67.114.201:8080/api"
Invoke-RestMethod "$base/devices"
Invoke-RestMethod "$base/video/channels"
Invoke-RestMethod "$base/system/audit-logs"
```

### 4.4.5 回填要求

把结果追加写入：

```text
docs/QA_RUN_003.md
docs/ACCEPTANCE_CHECKLIST.md
docs/TASK_FEEDBACK.md
```
Invoke-RestMethod "$base/ai/tasks" -Method Post -ContentType "application/json" -Body $aiTaskBody
```

### 4.4 负向错误验收

PowerShell：

```powershell
$base = "http://114.67.114.201:8080/api"

$body = @{
  channelId = "unknown-channel-id"
  protocol = "WebRTC"
} | ConvertTo-Json

try {
  Invoke-RestMethod "$base/video/sessions" -Method Post -ContentType "application/json" -Body $body
} catch {
  "status=" + $_.Exception.Response.StatusCode.value__
  $_.ErrorDetails.Message
}
```

期望：

- 状态码为 400 或 404。
- 响应中能看出 channel 不存在。
- 不允许返回 500。

## 5. SP-REAL-P0-002

负责人：机动人员

文档任务，无强制构建命令。

### 5.1 只允许修改

```text
docs/REAL_DEVICE_ACCESS_HIKVISION.md
docs/DEVICE_SDK_REQUIREMENTS.md
```

### 5.2 禁止记录

```text
真实密码
真实 token
真实 AppSecret
SSH 私钥
客户内网敏感 IP 明细
```

### 5.3 文档检查

PowerShell：

```powershell
cd F:\我的编程\AI编程\AI平台开发
rg "HIKVISION|HikCentral|iSecure|ISUP|RTSP|credentialRef" docs/REAL_DEVICE_ACCESS_HIKVISION.md docs/DEVICE_SDK_REQUIREMENTS.md
```

## 6. SP-REAL-P0-003

负责人：机动人员或拥有云端 `deploy` SSH key 的运维人员

目标：重新构建并部署云端 backend，让 BE-REAL-P0-002 的后端改动生效。

### 6.1 登录云端

```bash
ssh deploy@114.67.114.201
```

如出现：

```text
Permission denied (publickey)
```

说明当前机器没有可用部署私钥，需要项目负责人安排拥有 `deploy` SSH key 的人员执行，或为该人员单独配置 SSH key。

### 6.2 进入部署目录

```bash
cd /opt/ai-inspection-platform/current
pwd
```

### 6.3 部署前状态备份

```bash
mkdir -p deploy/run-records
date -Is | tee deploy/run-records/sp-real-p0-003-start.txt

docker compose --env-file deploy/.env \
  -f deploy/docker-compose.yml \
  -f deploy/docker-compose.cloud.yml \
  ps | tee deploy/run-records/sp-real-p0-003-before-ps.txt

docker compose --env-file deploy/.env \
  -f deploy/docker-compose.yml \
  -f deploy/docker-compose.cloud.yml \
  logs --tail=120 backend > deploy/run-records/sp-real-p0-003-before-backend.log
```

### 6.4 确认云端代码版本

```bash
git status --short || true
git log -1 --oneline || true
grep -R "ch-camera-a1" -n backend/src/main/java/com/aiplatform/inspection/repository/PostgresInspectionRepository.java || true
grep -R "PostMapping.*devices\\|/api/devices\\|createDevice" -n backend/src/main/java/com/aiplatform/inspection || true
```

如果云端代码没有最新修改，需要先按项目当前代码同步方式把最新代码同步到 `/opt/ai-inspection-platform/current`，再继续构建。不要手工在云端临时改业务代码。

### 6.5 重新构建 backend 容器

```bash
docker compose --env-file deploy/.env \
  -f deploy/docker-compose.yml \
  -f deploy/docker-compose.cloud.yml \
  build backend
```

### 6.6 重启 backend

```bash
docker compose --env-file deploy/.env \
  -f deploy/docker-compose.yml \
  -f deploy/docker-compose.cloud.yml \
  up -d backend
```

### 6.7 查看服务状态

```bash
docker compose --env-file deploy/.env \
  -f deploy/docker-compose.yml \
  -f deploy/docker-compose.cloud.yml \
  ps

docker compose --env-file deploy/.env \
  -f deploy/docker-compose.yml \
  -f deploy/docker-compose.cloud.yml \
  logs --tail=120 backend
```

### 6.8 云端接口验证

```bash
curl -sS http://127.0.0.1:8080/api/health
curl -sS http://127.0.0.1:8080/api/devices
curl -sS http://127.0.0.1:8080/api/video/channels
```

设备新增验证：

```bash
curl -sS -X POST http://127.0.0.1:8080/api/devices \
  -H "Content-Type: application/json" \
  -d '{
    "name": "SP云端验证摄像头",
    "sourceType": "camera",
    "vendor": "MOCK_VENDOR",
    "protocol": "RTSP",
    "endpoint": "rtsp://example.local/sp-camera",
    "credentialRef": "sp-credential-ref",
    "location": "SP云端验证区域",
    "edgeNodeId": "EDGE-01",
    "status": "offline",
    "streamUrl": "rtsp://example.local/sp-camera/stream"
  }'
```

稳定 Mock ID 验证：

```bash
curl -sS -X POST http://127.0.0.1:8080/api/video/sessions \
  -H "Content-Type: application/json" \
  -d '{"channelId":"ch-camera-a1","protocol":"WebRTC"}'

curl -sS -X POST http://127.0.0.1:8080/api/ai/tasks \
  -H "Content-Type: application/json" \
  -d '{"channelId":"ch-camera-a1","modelId":"helmet-detection-v1","taskType":"helmet"}'
```

未知通道负向验证：

```bash
curl -i -sS -X POST http://127.0.0.1:8080/api/video/sessions \
  -H "Content-Type: application/json" \
  -d '{"channelId":"unknown-channel-id","protocol":"WebRTC"}'
```

期望：

- `POST /api/devices` 不返回 405。
- `ch-camera-a1` 创建 video session 成功。
- `ch-camera-a1` 创建 ai task 成功。
- `unknown-channel-id` 返回 400 或 404，不返回 500。

### 6.9 写部署记录

把执行结果追加到新文档：

```text
deploy/DEPLOYMENT_RECORD_SP-REAL-P0-003.md
```

记录内容必须包含：

```text
任务编号：SP-REAL-P0-003
执行时间：
执行人：
backend 是否重新 build：
backend 是否重新 up：
健康检查结果：
POST /api/devices 结果：
ch-camera-a1 video session 结果：
ch-camera-a1 ai task 结果：
unknown channelId 结果：
是否影响现有服务：
问题：
```

写完部署记录后，还必须把摘要追加到：

```text
docs/TASK_FEEDBACK.md
```

## 7. 云端服务常用只读检查

仅限有 SSH 权限的运维人员使用：

```bash
ssh deploy@114.67.114.201
cd /opt/ai-inspection-platform/current
docker compose --env-file deploy/.env -f deploy/docker-compose.yml -f deploy/docker-compose.cloud.yml ps
docker compose --env-file deploy/.env -f deploy/docker-compose.yml -f deploy/docker-compose.cloud.yml logs --tail=50 backend
docker compose --env-file deploy/.env -f deploy/docker-compose.yml -f deploy/docker-compose.cloud.yml logs --tail=50 frontend
docker compose --env-file deploy/.env -f deploy/docker-compose.yml -f deploy/docker-compose.cloud.yml logs --tail=50 ai-service
docker compose --env-file deploy/.env -f deploy/docker-compose.yml -f deploy/docker-compose.cloud.yml logs --tail=50 edge-simulator
```

禁止在未得到总指挥明确安排时执行：

```text
docker compose down
docker system prune
删除数据库 volume
删除 MinIO 数据
修改生产或试点凭据
```

## 8. FE-REAL-P0-001-R1 云端前端部署

负责人：机动人员或拥有云端 `deploy` SSH key 的人员

目标：把程序员 B 已完成的前端修复部署到云端，让设备管理页真正使用真实 CRUD 页面。

### 8.1 登录云端

```bash
ssh deploy@114.67.114.201
cd /opt/ai-inspection-platform/current
```

### 8.2 重新构建 frontend

```bash
docker compose --env-file deploy/.env \
  -f deploy/docker-compose.yml \
  -f deploy/docker-compose.cloud.yml \
  build frontend
```

### 8.3 重启 frontend

```bash
docker compose --env-file deploy/.env \
  -f deploy/docker-compose.yml \
  -f deploy/docker-compose.cloud.yml \
  up -d frontend
```

### 8.4 查看日志

```bash
docker compose --env-file deploy/.env \
  -f deploy/docker-compose.yml \
  -f deploy/docker-compose.cloud.yml \
  ps

docker compose --env-file deploy/.env \
  -f deploy/docker-compose.yml \
  -f deploy/docker-compose.cloud.yml \
  logs --tail=120 frontend
```

### 8.5 页面访问验证

打开：

```text
http://114.67.114.201:5173
```

重点看设备管理页：

- 页面不再显示 `Mock兜底`
- 点击“接入设备”能弹出真实新增表单
- 存在编辑入口
- 存在删除入口
- 测试连接、同步通道有真实反馈

### 8.6 写部署记录

部署结果写入：

```text
deploy/DEPLOYMENT_RECORD_FE-REAL-P0-001-R1.md
```

并把摘要追加到：

```text
docs/TASK_FEEDBACK.md
```
