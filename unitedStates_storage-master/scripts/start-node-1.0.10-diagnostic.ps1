param(
    [string]$JarUrl,
    [string]$ExpectedSha256,
    [string]$JarPath = "C:\citu-node-v35-activation-514291-1.0.10-diagnostic.jar",
    [string]$Address = "194.87.236.238",
    [int]$Port = 82,
    [string]$LogPath = "C:\server\logs"
)

$ErrorActionPreference = "Stop"

if ([string]::IsNullOrWhiteSpace($JarUrl)) {
    throw "JarUrl is required for the approved diagnostic release artifact."
}
if ([string]::IsNullOrWhiteSpace($ExpectedSha256)) {
    throw "ExpectedSha256 is required before starting the diagnostic node."
}

New-Item -ItemType Directory -Force -Path $LogPath | Out-Null

if (-not (Test-Path -LiteralPath $JarPath)) {
    [Net.ServicePointManager]::SecurityProtocol = [Net.SecurityProtocolType]::Tls12
    Invoke-WebRequest -UseBasicParsing -Uri $JarUrl -OutFile $JarPath
}
$actualSha256 = (Get-FileHash -Algorithm SHA256 -LiteralPath $JarPath).Hash.ToLowerInvariant()
if ($actualSha256 -ne $ExpectedSha256.ToLowerInvariant()) {
    throw "SHA256 mismatch for $JarPath. expected=$ExpectedSha256 actual=$actualSha256"
}

$matchingNodeProcesses = Get-CimInstance Win32_Process -Filter "Name = 'java.exe'" |
        Where-Object {
            $_.CommandLine -like "*citu-node-v35-activation-514291*" -and
            ($_.CommandLine -like "*-Dserver.port=$Port*" -or $_.CommandLine -like "*--server.port=$Port*")
        }

foreach ($process in $matchingNodeProcesses) {
    Stop-Process -Id $process.ProcessId -Force
}

Start-Process -FilePath "java" -WorkingDirectory "C:\" -ArgumentList @(
    "-Xms512m",
    "-Xmx1536m",
    "-XX:+UseG1GC",
    "-XX:MaxGCPauseMillis=500",
    "-XX:+ExitOnOutOfMemoryError",
    "-Dlogging.file.path=$LogPath",
    "-Dlogging.level.root=INFO",
    "-Dcitu.prePersistPeerPropagation.enabled=false",
    "-Dcitu.memoryForensics.enabled=true",
    "-Dcitu.memoryForensics.file.enabled=true",
    "-Dcitu.memoryForensics.lastTraces=3",
    "-Dcitu.memoryForensics.verbose=false",
    "-Dcitu.memoryForensics.file.path=$LogPath\citu-memory-forensics.jsonl",
    "-Dserver.tomcat.threads.max=80",
    "-Dserver.tomcat.accept-count=100",
    "-Dserver.tomcat.max-connections=1024",
    "-Dserver.port=$Port",
    "-Dserver.address=$Address",
    "-jar",
    $JarPath
) -WindowStyle Hidden

$baseUrl = "http://$Address`:$Port"
for ($attempt = 1; $attempt -le 60; $attempt++) {
    try {
        Invoke-WebRequest -UseBasicParsing -TimeoutSec 5 -Uri "$baseUrl/size" | Out-Null
        break
    } catch {
        if ($attempt -eq 60) {
            throw "Node did not answer /size after diagnostic start."
        }
        Start-Sleep -Seconds 5
    }
}

$watcher = Join-Path $PSScriptRoot "v35-diagnostic-watch-node.ps1"
Start-Process -FilePath "powershell" -ArgumentList @(
    "-NoProfile",
    "-ExecutionPolicy",
    "Bypass",
    "-File",
    $watcher,
    "-BaseUrl",
    $baseUrl,
    "-OutputDir",
    "$LogPath\diagnostic-captures",
    "-IntervalSeconds",
    "15",
    "-MaxFiles",
    "300"
) -WindowStyle Hidden
