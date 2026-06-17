param(
  [string]$Root = (Resolve-Path (Join-Path $PSScriptRoot "..\..")).Path
)

$ErrorActionPreference = "Stop"

function Assert-Command {
  param([string]$Name, [string]$Hint)
  if (-not (Get-Command $Name -ErrorAction SilentlyContinue)) {
    throw "$Name is not available. $Hint"
  }
}

Assert-Command "node" "Install Node.js 22 LTS or later."
Assert-Command "npm" "Install npm with Node.js."
Assert-Command "java" "Install JDK 21 and reopen PowerShell."
Assert-Command "python" "Install Python 3.12 and add it to PATH."

$frontend = Join-Path $Root "frontend"
$backend = Join-Path $Root "backend"
$ai = Join-Path $Root "ai-service"
$edge = Join-Path $Root "edge-simulator"
$mavenCommand = if (Test-Path (Join-Path $backend "mvnw.cmd")) { ".\mvnw.cmd" } else { "mvn" }

if ($mavenCommand -eq "mvn") {
  Assert-Command "mvn" "Install Maven 3.9+ or add it to PATH."
}

if (-not (Test-Path (Join-Path $frontend "node_modules"))) {
  Write-Host "Installing frontend dependencies..."
  Push-Location $frontend
  npm install
  Pop-Location
}

if (-not (Test-Path (Join-Path $ai ".venv"))) {
  Write-Host "Creating ai-service venv..."
  Push-Location $ai
  python -m venv .venv
  .\.venv\Scripts\python.exe -m pip install --upgrade pip
  .\.venv\Scripts\pip.exe install -r requirements.txt
  Pop-Location
}

if (-not (Test-Path (Join-Path $edge ".venv"))) {
  Write-Host "Creating edge-simulator venv..."
  Push-Location $edge
  python -m venv .venv
  .\.venv\Scripts\python.exe -m pip install --upgrade pip
  .\.venv\Scripts\pip.exe install -r requirements.txt
  Pop-Location
}

Write-Host "Starting frontend on 5173..."
Start-Process powershell -WindowStyle Normal -ArgumentList @(
  "-NoExit",
  "-Command",
  "cd `"$frontend`"; npm run dev"
)

Write-Host "Starting backend on 8080..."
Start-Process powershell -WindowStyle Normal -ArgumentList @(
  "-NoExit",
  "-Command",
  "cd `"$backend`"; $mavenCommand spring-boot:run"
)

Write-Host "Starting ai-service on 8100..."
Start-Process powershell -WindowStyle Normal -ArgumentList @(
  "-NoExit",
  "-Command",
  "cd `"$ai`"; .\.venv\Scripts\Activate.ps1; uvicorn app.main:app --host 0.0.0.0 --port 8100"
)

Write-Host "Starting edge-simulator on 8200..."
Start-Process powershell -WindowStyle Normal -ArgumentList @(
  "-NoExit",
  "-Command",
  "cd `"$edge`"; .\.venv\Scripts\Activate.ps1; uvicorn app.main:app --host 0.0.0.0 --port 8200"
)

Write-Host ""
Write-Host "Services are starting in separate PowerShell windows."
Write-Host "Run health checks with:"
Write-Host ".\deploy\qa-start\health-check.ps1"
