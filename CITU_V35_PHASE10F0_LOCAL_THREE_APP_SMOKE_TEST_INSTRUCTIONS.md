# CITU V35 Phase 10F-0 - Local Three-App Smoke Test Instructions

## Scope and safety warning

These instructions are for a local manual smoke test only.

- Do not deploy these builds.
- Do not publish or release generated JAR files.
- Do not point local smoke launches at production blockchain data.
- Do not change `V35_RESCUE_FORK_INDEX`.
- Do not change `V35_BURN_ADDRESS`.
- Do not create a burn transaction or jackpot payout.

V35 remains disabled:

```text
V35_RESCUE_FORK_INDEX = Long.MAX_VALUE
V35_BURN_ADDRESS = BURN_ADDRESS
```

## 1. Build and test commands

### Server

```powershell
cd C:\citu\unitedStates_storage-master
.\mvnw.cmd test
.\mvnw.cmd clean package
```

### Wallet

```powershell
cd C:\citu\unitedStates_final-master
.\mvnw.cmd test
.\mvnw.cmd clean package
```

### Website

```powershell
cd C:\citu\website.citu-web-master
.\mvnw.cmd test
.\mvnw.cmd clean package
```

## 2. Local run commands

Run each command in its own PowerShell terminal. Use the isolated local H2 paths below for server and wallet. Do not reuse live node state.

### Server

```powershell
cd C:\citu\unitedStates_storage-master
java -jar target\unitedStates-0.0.423-SNAPSHOT.jar `
  --server.address=localhost `
  --server.port=8083 `
  "--spring.datasource.url=jdbc:h2:file:./target/smoke/server-db;DB_CLOSE_ON_EXIT=FALSE;AUTO_RECONNECT=TRUE"
```

Open:

- `http://localhost:8083/size`
- `http://localhost:8083/v35/protocol`
- `http://localhost:8083/v35/supply`
- `http://localhost:8083/v35/founder`
- `http://localhost:8083/v35/founder/burn-snapshot`
- `http://localhost:8083/v35/burn`
- `http://localhost:8083/v35/jackpot/status`

### Wallet

```powershell
cd C:\citu\unitedStates_final-master
java -jar target\unitedStates-0.32.216-SNAPSHOT.jar `
  --server.address=localhost `
  --server.port=8082 `
  "--spring.datasource.url=jdbc:h2:file:./target/smoke/wallet-db;DB_CLOSE_ON_EXIT=FALSE;AUTO_RECONNECT=TRUE"
```

Open:

- `http://localhost:8082/`
- `http://localhost:8082/v35-dashboard`
- `http://localhost:8082/status`
- `http://localhost:8082/size`

### Website

```powershell
cd C:\citu\website.citu-web-master
java -jar target\website.citu-0.0.24-SNAPSHOT.jar `
  --server.address=localhost `
  --server.port=8080 `
  --server.ssl.enabled=false
```

Open:

- `http://localhost:8080/`
- `http://localhost:8080/v35`
- `http://localhost:8080/v35/preactivation-status`
- `http://localhost:8080/v35/operator-guide`
- `http://localhost:8080/v35/checksums`
- `http://localhost:8080/v35/burn-warning`

## 3. Stop commands

For a foreground launch, stop the application safely with:

```text
Ctrl+C
```

If an application was started in a separate process, record its process ID and stop only that process:

```powershell
Stop-Process -Id <PID>
```

## 4. Website screenshot checklist

Capture:

- V35 overview at `/v35`
- `/v35/preactivation-status`
- `/v35/operator-guide`
- `/v35/checksums`
- `/v35/burn-warning`
- `RU` sections
- `EN` sections
- `ZH` sections
- header and V35 navigation links
- warning that V35 is not active yet
- warning that no founder burn has occurred
- warning against fake or unofficial burn addresses
- placeholder final height, tag, and checksum text
- warning that dry-run artifacts are not final production artifacts

## 5. Server and wallet check checklist

Capture or record:

- server starts on `localhost:8083`
- wallet starts on `localhost:8082`
- server `/v35/protocol` response
- server `/v35/supply` response
- server `/v35/founder/burn-snapshot` response
- server `/v35/burn` response
- server `/v35/jackpot/status` response
- wallet `/v35-dashboard` page
- V35 disabled state where displayed
- burn address placeholder remains `BURN_ADDRESS`
- no burn transaction was executed
- no jackpot payout was created

Empty isolated local databases may produce empty-chain values. That is expected for this manual smoke test. Do not substitute production blockchain data.

