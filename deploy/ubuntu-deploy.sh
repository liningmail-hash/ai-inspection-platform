#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
ENV_FILE="${ROOT_DIR}/.env"

echo "== AI Inspection Platform Ubuntu Deploy =="
echo "Project: ${ROOT_DIR}"

if ! command -v docker >/dev/null 2>&1; then
  echo "Docker is not installed. Install Docker Desktop/Engine first, then rerun this script."
  exit 1
fi

if ! docker compose version >/dev/null 2>&1; then
  echo "Docker Compose plugin is not available. Install docker compose plugin first."
  exit 1
fi

if [ ! -f "${ENV_FILE}" ]; then
  cp "${ROOT_DIR}/deploy/.env.example" "${ENV_FILE}"
  echo "Created .env from deploy/.env.example"
  echo "Please review .env and change default passwords before production use."
fi

cd "${ROOT_DIR}"
docker compose -f deploy/docker-compose.yml up -d --build

echo
echo "Deployment started."
echo "Frontend:        http://localhost:5173"
echo "Backend API:     http://localhost:8080/api/health"
echo "Backend Docs:    http://localhost:8080/docs"
echo "AI Service:      http://localhost:8100/health"
echo "Edge Simulator:  http://localhost:8200/health"
echo "MinIO Console:   http://localhost:9001"
