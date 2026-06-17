#!/usr/bin/env bash
set -euo pipefail

echo "== AI Inspection Platform Ubuntu Check =="
echo "Host: $(hostname)"
echo "Kernel: $(uname -a)"
echo

check_cmd() {
  local name="$1"
  if command -v "$name" >/dev/null 2>&1; then
    echo "[OK] $name: $($name --version 2>&1 | head -n 1)"
  else
    echo "[MISS] $name"
  fi
}

check_cmd git
check_cmd docker
if docker compose version >/dev/null 2>&1; then
  echo "[OK] docker compose: $(docker compose version)"
else
  echo "[MISS] docker compose"
fi
check_cmd node
check_cmd npm
check_cmd java
check_cmd mvn
check_cmd python3

echo
echo "== Ports =="
for port in 5173 8080 8100 8200 5432 6379 9000 9001; do
  if ss -ltn 2>/dev/null | awk '{print $4}' | grep -q ":${port}$"; then
    echo "[BUSY] ${port}"
  else
    echo "[FREE] ${port}"
  fi
done

echo
echo "== Disk =="
df -h .

echo
echo "== Memory =="
free -h || true

echo
echo "Check complete."
