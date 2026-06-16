param(
    [string]$BaseUrl = "http://194.87.236.238:82",
    [string]$OutputDir = "C:\server\logs\diagnostic-captures",
    [int]$IntervalSeconds = 15,
    [int]$MaxFiles = 300
)

$ErrorActionPreference = "Continue"
$endpoints = @(
    "/size",
    "/debug/node/runtime",
    "/debug/memory/status",
    "/debug/memory/forensics",
    "/debug/block-submit/status",
    "/debug/block-submit/traces",
    "/debug/tournament/traces",
    "/debug/add-block/traces",
    "/v35/protocol"
)

New-Item -ItemType Directory -Force -Path $OutputDir | Out-Null
$heartbeat = Join-Path $OutputDir "WATCHER_HEARTBEAT.txt"
"started=$(Get-Date -Format o) baseUrl=$BaseUrl" | Set-Content -LiteralPath $heartbeat -Encoding UTF8

while ($true) {
    try {
        Invoke-WebRequest -UseBasicParsing -TimeoutSec 5 -Uri "$BaseUrl/size" | Out-Null
        break
    } catch {
        "waiting=$(Get-Date -Format o) endpoint=/size error=$($_.Exception.GetType().Name): $($_.Exception.Message)" | Add-Content -LiteralPath $heartbeat -Encoding UTF8
        Start-Sleep -Seconds 5
    }
}

while ($true) {
    $timestamp = Get-Date -Format "yyyyMMdd_HHmmss_fff"
    "heartbeat=$(Get-Date -Format o)" | Set-Content -LiteralPath $heartbeat -Encoding UTF8
    foreach ($endpoint in $endpoints) {
        $safeName = $endpoint.TrimStart("/").Replace("/", "_")
        if ([string]::IsNullOrWhiteSpace($safeName)) {
            $safeName = "root"
        }
        $file = Join-Path $OutputDir "$timestamp-$safeName.json"
        try {
            $response = Invoke-WebRequest -UseBasicParsing -TimeoutSec 8 -Uri "$BaseUrl$endpoint"
            $response.Content | Set-Content -LiteralPath $file -Encoding UTF8
        } catch {
            $errorFile = Join-Path $OutputDir "$timestamp-$safeName.ERROR.txt"
            "endpoint=$endpoint`r`ntime=$(Get-Date -Format o)`r`nerror=$($_.Exception.GetType().Name): $($_.Exception.Message)" |
                    Set-Content -LiteralPath $errorFile -Encoding UTF8
        }
    }

    Get-ChildItem -LiteralPath $OutputDir -File |
            Where-Object { $_.Name -ne "WATCHER_HEARTBEAT.txt" } |
            Sort-Object LastWriteTime -Descending |
            Select-Object -Skip $MaxFiles |
            Remove-Item -Force

    Start-Sleep -Seconds $IntervalSeconds
}
