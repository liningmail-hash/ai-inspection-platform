# QA 环境安装与启动说明

任务编号：SP-P0-002  
目标：解阻 QA-P0-002 的中心后端、AI 服务、边缘模拟器接口冒烟。本文只处理运行环境，不新增业务功能。

## 1. QA 启动方式选择

推荐顺序：

1. Docker Compose 全栈启动：适合 Ubuntu 服务器或已安装 Docker Desktop 的 Windows。
2. 本地多进程启动：Docker 不可用时使用，前端、后端、AI 服务、边缘模拟器分别启动。

当前 QA 冒烟至少需要以下服务可访问：

| 服务 | 地址 | 健康检查 |
|---|---|---|
| 前端 | `http://localhost:5173` | HTTP 200 |
| 中心后端 | `http://localhost:8080/api/health` | `status=UP` |
| AI 服务 | `http://localhost:8100/health` | HTTP 200 |
| 边缘模拟器 | `http://localhost:8200/health` | HTTP 200 |

## 2. Windows PowerShell 环境准备

必装软件：

- Git 2.40+
- Node.js 22 LTS+，包含 npm
- JDK 21，并配置 `JAVA_HOME`
- Python 3.12
- Docker Desktop 4.x，可选但推荐
- Maven 3.9+ 可选；后端优先使用 `backend/mvnw.cmd`

检查命令：

```powershell
git --version
node --version
npm --version
java -version
python --version
docker --version
docker compose version
.\backend\mvnw.cmd -version
```

如果 Maven Wrapper 因网络策略无法下载 Maven，再安装系统 Maven 并检查：

```powershell
mvn -version
```

### 2.1 前端依赖

```powershell
cd F:\我的编程\AI编程\AI平台开发\frontend
npm install
```

### 2.2 AI 服务依赖

```powershell
cd F:\我的编程\AI编程\AI平台开发\ai-service
python -m venv .venv
.\.venv\Scripts\Activate.ps1
python -m pip install --upgrade pip
pip install -r requirements.txt
deactivate
```

### 2.3 边缘模拟器依赖

```powershell
cd F:\我的编程\AI编程\AI平台开发\edge-simulator
python -m venv .venv
.\.venv\Scripts\Activate.ps1
python -m pip install --upgrade pip
pip install -r requirements.txt
deactivate
```

### 2.4 后端依赖

后端默认 Mock 仓储启动，不需要 PostgreSQL。

```powershell
cd F:\我的编程\AI编程\AI平台开发\backend
.\mvnw.cmd -version
.\mvnw.cmd -DskipTests package
```

如果 Wrapper 不可用：

```powershell
cd F:\我的编程\AI编程\AI平台开发\backend
mvn -DskipTests package
```

## 3. Windows PowerShell 本地多进程启动

执行前确认已完成依赖安装。

```powershell
cd F:\我的编程\AI编程\AI平台开发
.\deploy\qa-start\start-local.ps1
```

脚本会分别启动：

- 前端：`npm run dev`，端口 `5173`
- 后端：`mvnw.cmd spring-boot:run` 或 `mvn spring-boot:run`，端口 `8080`
- AI 服务：`uvicorn app.main:app --host 0.0.0.0 --port 8100`
- 边缘模拟器：`uvicorn app.main:app --host 0.0.0.0 --port 8200`

脚本使用独立 PowerShell 窗口启动各服务，便于 QA 查看日志。关闭对应窗口即可停止服务。

健康检查：

```powershell
.\deploy\qa-start\health-check.ps1
```

如 PowerShell 执行策略拦截脚本：

```powershell
Set-ExecutionPolicy -Scope Process -ExecutionPolicy Bypass
.\deploy\qa-start\start-local.ps1
```

## 4. Ubuntu 环境准备

基础工具：

```bash
sudo apt-get update
sudo apt-get install -y git curl ca-certificates unzip
```

Node.js 22 LTS：

```bash
curl -fsSL https://deb.nodesource.com/setup_22.x | sudo -E bash -
sudo apt-get install -y nodejs
node --version
npm --version
```

JDK 21：

```bash
sudo apt-get install -y openjdk-21-jdk
java -version
```

Maven 可选。优先使用 `backend/mvnw`，如果 Wrapper 不可用再安装：

```bash
sudo apt-get install -y maven
mvn -version
```

Python 3.12 和 venv：

```bash
sudo apt-get install -y python3.12 python3.12-venv python3-pip
python3.12 --version
```

Docker Engine，可选但推荐：

```bash
sudo apt-get install -y docker.io docker-compose-plugin
sudo systemctl enable --now docker
docker --version
docker compose version
```

### 4.1 Ubuntu 本地多进程依赖

```bash
cd /path/to/AI平台开发

cd frontend
npm install

cd ../ai-service
python3.12 -m venv .venv
. .venv/bin/activate
python -m pip install --upgrade pip
pip install -r requirements.txt
deactivate

cd ../edge-simulator
python3.12 -m venv .venv
. .venv/bin/activate
python -m pip install --upgrade pip
pip install -r requirements.txt
deactivate
```

### 4.2 Ubuntu 本地多进程启动

```bash
cd /path/to/AI平台开发
bash deploy/qa-start/start-local.sh
```

健康检查：

```bash
bash deploy/qa-start/health-check.sh
```

## 5. Docker Compose 全栈启动

Docker 可用时，优先使用 Compose 统一启动。

Windows PowerShell：

```powershell
cd F:\我的编程\AI编程\AI平台开发
copy deploy\.env.example .env
docker compose -f deploy\docker-compose.yml up -d --build
.\deploy\qa-start\health-check.ps1
```

Ubuntu：

```bash
cd /path/to/AI平台开发
cp deploy/.env.example .env
docker compose -f deploy/docker-compose.yml up -d --build
bash deploy/qa-start/health-check.sh
```

停止：

```powershell
docker compose -f deploy\docker-compose.yml down
```

Ubuntu 停止：

```bash
docker compose -f deploy/docker-compose.yml down
```

## 6. 手工健康检查命令

Windows PowerShell：

```powershell
Invoke-WebRequest -UseBasicParsing http://localhost:5173
Invoke-WebRequest -UseBasicParsing http://localhost:8080/api/health
Invoke-WebRequest -UseBasicParsing http://localhost:8100/health
Invoke-WebRequest -UseBasicParsing http://localhost:8200/health
```

Ubuntu：

```bash
curl -f http://localhost:5173
curl -f http://localhost:8080/api/health
curl -f http://localhost:8100/health
curl -f http://localhost:8200/health
```

## 7. 常见失败原因与处理

### 7.1 `java` 命令不存在

原因：JDK 21 未安装，或 `JAVA_HOME` / PATH 未配置。  
处理：安装 JDK 21，重新打开终端后执行 `java -version`。

### 7.2 Maven Wrapper 下载失败

原因：QA 机器无法访问 Maven 分发地址或公司网络策略拦截。  
处理：安装 Maven 3.9+，使用 `mvn spring-boot:run`。

### 7.3 后端 8080 无响应

可能原因：

- JDK 不可用。
- Maven Wrapper 下载失败。
- 端口 `8080` 被占用。
- 后端窗口启动失败。

处理：

```powershell
netstat -ano | findstr :8080
cd backend
.\mvnw.cmd spring-boot:run
```

查看终端日志中的第一条异常。

### 7.4 前端 5173 无响应

可能原因：

- `npm install` 未执行。
- 端口 `5173` 被占用。
- Vite 启动失败。

处理：

```powershell
cd frontend
npm install
npm run dev
```

### 7.5 AI 服务或边缘模拟器无法启动

可能原因：

- Python 版本不是 3.12。
- venv 未创建或未激活。
- requirements 未安装。
- 端口 `8100` / `8200` 被占用。

处理：

```powershell
cd ai-service
python -m venv .venv
.\.venv\Scripts\Activate.ps1
pip install -r requirements.txt
uvicorn app.main:app --reload --port 8100
```

边缘模拟器同理，目录换成 `edge-simulator`，端口换成 `8200`。

### 7.6 Docker Compose 启动失败

可能原因：

- Docker Desktop / Docker Engine 未启动。
- 端口被本地多进程占用。
- 镜像构建时无法联网下载依赖。

处理：

```powershell
docker --version
docker compose version
docker compose -f deploy\docker-compose.yml ps
docker compose -f deploy\docker-compose.yml logs backend
```

如果端口冲突，先停止本地多进程窗口，再重新执行 Compose。

## 8. QA 可恢复执行的接口冒烟范围

环境恢复后，QA-P0-002 可继续执行：

中心后端：

- `GET /api/health`
- `GET /api/overview`
- `GET /api/devices`
- `GET /api/inspection-plans`
- `GET /api/alarms`
- `GET /api/models`
- `GET /api/system/users`
- `GET /api/inspection-tasks`
- `GET /api/media-assets`
- `GET /api/map-events`

AI 服务：

- `GET /health`
- `GET /datasets`
- `GET /training-jobs`
- `GET /models`

边缘模拟器：

- `GET /health`
- `GET /node/status`
- `GET /devices`
- `GET /events/latest`

## 9. 不在本任务范围

- 不接真实摄像头、无人机、车载网关。
- 不修改后端、前端、AI 服务、边缘模拟器业务代码。
- 不新增业务功能。
- 不写入真实密码、真实 Token、真实客户 IP。
