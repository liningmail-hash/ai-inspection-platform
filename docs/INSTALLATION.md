# Installation and Startup

## Environment Requirements

Recommended versions:

- JDK 21
- Node.js 22 LTS or later
- npm 10 or later
- Python 3.12
- Docker Desktop 4.x, only for full compose deployment
- Git 2.40 or later

The backend includes Maven Wrapper, so QA does not need a system Maven install
for normal backend Mock startup. If the wrapper cannot download Maven because
of network policy, install Maven 3.9 or later and use `mvn` instead of
`.\mvnw.cmd`.

For unified QA environment preparation, local multi-process startup, Docker
startup, and smoke health checks, see [QA_ENV_SETUP.md](QA_ENV_SETUP.md).

## Backend Mock Startup

Default backend startup uses the InMemory Mock repository and does not require
PostgreSQL.

PowerShell:

```powershell
cd backend
.\mvnw.cmd spring-boot:run
```

Equivalent command when system Maven is installed:

```powershell
cd backend
mvn spring-boot:run
```

Health check:

```powershell
Invoke-RestMethod http://localhost:8080/api/health
```

Expected result shape:

```json
{
  "status": "UP",
  "service": "inspection-backend",
  "time": "2026-06-16T10:00:00Z"
}
```

Useful Mock smoke URLs:

```text
http://localhost:8080/api/health
http://localhost:8080/api/video/channels
http://localhost:8080/api/integrations
http://localhost:8080/docs
```

## Backend Build Checks

With Maven Wrapper:

```powershell
cd backend
.\mvnw.cmd test
.\mvnw.cmd -DskipTests package
```

With system Maven:

```powershell
cd backend
mvn test
mvn -DskipTests package
```

## Backend PostgreSQL Profile

The PostgreSQL profile is unchanged and remains opt-in for local runs.

PowerShell:

```powershell
cd backend
$env:SPRING_PROFILES_ACTIVE="postgres"
$env:SPRING_DATASOURCE_URL="jdbc:postgresql://localhost:5432/ai_inspection"
$env:SPRING_DATASOURCE_USERNAME="ai_inspection"
$env:SPRING_DATASOURCE_PASSWORD="change-me"
.\mvnw.cmd spring-boot:run
```

When `SPRING_PROFILES_ACTIVE=postgres` is set, Spring loads
`application-postgres.yml` and uses `PostgresInspectionRepository`. Without that
profile, it uses `InMemoryInspectionRepository`.

## Frontend Startup

```powershell
cd frontend
npm install
npm run dev
```

Default URL:

```text
http://localhost:5173
```

Build check:

```powershell
npm run build
```

## AI Service Startup

```powershell
cd ai-service
python -m venv .venv
.\.venv\Scripts\Activate.ps1
pip install -r requirements.txt
uvicorn app.main:app --reload --port 8100
```

Health check:

```text
http://localhost:8100/health
```

## Edge Simulator Startup

```powershell
cd edge-simulator
python -m venv .venv
.\.venv\Scripts\Activate.ps1
pip install -r requirements.txt
uvicorn app.main:app --reload --port 8200
```

Health check:

```text
http://localhost:8200/health
```

## Docker Compose Startup

Docker Compose starts the backend with the `postgres` profile and reads seed
data from PostgreSQL.

```powershell
docker compose -f deploy\docker-compose.yml up -d --build
```

Common URLs:

```text
Frontend: http://localhost:5173
Backend: http://localhost:8080
Backend Swagger UI: http://localhost:8080/docs
AI service: http://localhost:8100
Edge simulator: http://localhost:8200
MinIO console: http://localhost:9001
PostgreSQL: localhost:5432
Redis: localhost:6379
```

Stop:

```powershell
docker compose -f deploy\docker-compose.yml down
```

Stop and remove data volumes:

```powershell
docker compose -f deploy\docker-compose.yml down -v
```

## QA Local Multi-Process Startup

Windows PowerShell:

```powershell
.\deploy\qa-start\start-local.ps1
.\deploy\qa-start\health-check.ps1
```

Ubuntu:

```bash
bash deploy/qa-start/start-local.sh
bash deploy/qa-start/health-check.sh
```

## Troubleshooting

Check JDK:

```powershell
java -version
```

Check Maven Wrapper:

```powershell
cd backend
.\mvnw.cmd -version
```

If Maven Wrapper download is blocked, install Maven 3.9 or later:

```powershell
mvn -version
```

Check Docker:

```powershell
docker --version
docker compose version
```
