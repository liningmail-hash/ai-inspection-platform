#!/usr/bin/env bash
set -euo pipefail

FAILED=0

check() {
  local name="$1"
  local url="$2"
  if curl -fsS "$url" >/dev/null; then
    echo "[OK] $name $url"
  else
    echo "[FAIL] $name $url"
    FAILED=$((FAILED + 1))
  fi
}

check "frontend" "http://localhost:5173"
check "backend" "http://localhost:8080/api/health"
check "ai-service" "http://localhost:8100/health"
check "edge-simulator" "http://localhost:8200/health"

if [ "$FAILED" -gt 0 ]; then
  exit 1
fi
