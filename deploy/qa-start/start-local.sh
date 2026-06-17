#!/usr/bin/env bash
set -euo pipefail

ROOT="${1:-$(cd "$(dirname "${BASH_SOURCE[0]}")/../.." && pwd)}"

command -v node >/dev/null || { echo "node is not available. Install Node.js 22 LTS."; exit 1; }
command -v npm >/dev/null || { echo "npm is not available."; exit 1; }
command -v java >/dev/null || { echo "java is not available. Install JDK 21."; exit 1; }
command -v python3.12 >/dev/null || { echo "python3.12 is not available."; exit 1; }

FRONTEND="$ROOT/frontend"
BACKEND="$ROOT/backend"
AI="$ROOT/ai-service"
EDGE="$ROOT/edge-simulator"
LOG_DIR="$ROOT/.qa-logs"
mkdir -p "$LOG_DIR"

if [ -x "$BACKEND/mvnw" ]; then
  MAVEN_CMD="./mvnw"
else
  command -v mvn >/dev/null || { echo "mvn is not available and backend/mvnw is missing. Install Maven 3.9+."; exit 1; }
  MAVEN_CMD="mvn"
fi

if [ ! -d "$FRONTEND/node_modules" ]; then
  (cd "$FRONTEND" && npm install)
fi

if [ ! -d "$AI/.venv" ]; then
  (cd "$AI" && python3.12 -m venv .venv && . .venv/bin/activate && python -m pip install --upgrade pip && pip install -r requirements.txt)
fi

if [ ! -d "$EDGE/.venv" ]; then
  (cd "$EDGE" && python3.12 -m venv .venv && . .venv/bin/activate && python -m pip install --upgrade pip && pip install -r requirements.txt)
fi

(cd "$FRONTEND" && nohup npm run dev > "$LOG_DIR/frontend.log" 2>&1 & echo $! > "$LOG_DIR/frontend.pid")
(cd "$BACKEND" && nohup $MAVEN_CMD spring-boot:run > "$LOG_DIR/backend.log" 2>&1 & echo $! > "$LOG_DIR/backend.pid")
(cd "$AI" && nohup .venv/bin/uvicorn app.main:app --host 0.0.0.0 --port 8100 > "$LOG_DIR/ai-service.log" 2>&1 & echo $! > "$LOG_DIR/ai-service.pid")
(cd "$EDGE" && nohup .venv/bin/uvicorn app.main:app --host 0.0.0.0 --port 8200 > "$LOG_DIR/edge-simulator.log" 2>&1 & echo $! > "$LOG_DIR/edge-simulator.pid")

echo "QA services are starting. Logs: $LOG_DIR"
echo "Run: bash deploy/qa-start/health-check.sh"
