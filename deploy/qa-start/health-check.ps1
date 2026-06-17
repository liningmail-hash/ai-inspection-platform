$ErrorActionPreference = "Continue"

$checks = @(
  @{ Name = "frontend"; Url = "http://localhost:5173" },
  @{ Name = "backend"; Url = "http://localhost:8080/api/health" },
  @{ Name = "ai-service"; Url = "http://localhost:8100/health" },
  @{ Name = "edge-simulator"; Url = "http://localhost:8200/health" }
)

$failed = 0

foreach ($check in $checks) {
  try {
    $response = Invoke-WebRequest -UseBasicParsing -Uri $check.Url -TimeoutSec 5
    if ($response.StatusCode -ge 200 -and $response.StatusCode -lt 300) {
      Write-Host "[OK] $($check.Name) $($check.Url) HTTP $($response.StatusCode)"
    } else {
      Write-Host "[FAIL] $($check.Name) $($check.Url) HTTP $($response.StatusCode)"
      $failed++
    }
  } catch {
    Write-Host "[FAIL] $($check.Name) $($check.Url) $($_.Exception.Message)"
    $failed++
  }
}

if ($failed -gt 0) {
  exit 1
}

exit 0
