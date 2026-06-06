# CITU V35 Phase 11H-1 Node 1.0.1 Memory Guard Release Report

## Executive summary

Phase 11H-1 created a server/node-only `1.0.1` release for the Phase 11H-0 memory stability guards.

Only the node/server project was changed. Wallet and website were not touched. No VPS deployment, restart, process kill, resource access, burn transaction, jackpot payout, wallet upload, website upload, or source push was performed.

## Root cause from Phase 11H-0

The VPS `java.lang.OutOfMemoryError` was not tied to V35 consensus or rewards. The likely runtime pressure sources were:

- long outbound peer HTTP waits;
- unbounded simple peer GET response reads;
- peer update/block propagation fan-out;
- unbounded `/getTransactions` JSON response size;
- repeated wallet/website/public API calls amplifying node instability.

## Files changed

Committed server-only source/test/version/report changes:

- `CITU_V35_PHASE11H0_SERVER_OOM_AUDIT_REPORT.md`
- `unitedStates_storage-master/pom.xml`
- `unitedStates_storage-master/src/main/java/International_Trade_Union/controllers/TransactionController.java`
- `unitedStates_storage-master/src/main/java/International_Trade_Union/utils/UtilUrl.java`
- `unitedStates_storage-master/src/main/java/International_Trade_Union/utils/UtilsResolving.java`
- `unitedStates_storage-master/src/test/java/International_Trade_Union/controllers/TransactionControllerMemoryGuardTest.java`
- `unitedStates_storage-master/src/test/java/International_Trade_Union/utils/UtilUrlMemoryGuardTest.java`
- `unitedStates_storage-master/src/test/java/International_Trade_Union/utils/UtilsResolvingMemoryGuardTest.java`

Commit created:

- `366e91c phase 11H1 release node 1.0.1 memory guards`

This report was created after the GitHub Release upload so it can record final release URLs and public verification results.

## Version and artifact naming

Server Maven version:

- before: `1.0.0`
- after: `1.0.1`

Clean public artifact name:

- `citu-node-v35-activation-514291-1.0.1.jar`

No wallet `1.0.1` artifact was created. No website `1.0.1` artifact was created.

## Build and test results

Commands run in `C:\citu\unitedStates_storage-master`:

```bat
.\mvnw.cmd test
.\mvnw.cmd clean package
```

Results:

- Server tests: PASS, `239` tests run, `0` failures, `0` errors, `0` skipped.
- Server clean package: PASS.
- Built server jar: `C:\citu\unitedStates_storage-master\target\unitedStates-1.0.1.jar`.

Maven warnings observed:

- duplicate dependency declarations in `pom.xml`;
- log4j appender warnings during tests.

These warnings were pre-existing project build warnings and did not block tests/package.

## Node-only release folder

Local release folder:

- `C:\citu\release-dryrun\v35-514291-node-1.0.1`

Local release files:

- `citu-node-v35-activation-514291-1.0.1.jar`
- `CITU_V35_514291_NODE_1.0.1_SHA256SUMS.txt`
- `CITU_V35_514291_NODE_1.0.1_RELEASE_NOTES_EN.md`
- `CITU_V35_514291_NODE_1.0.1_RELEASE_NOTES_RU.md`
- `start-node-windows.bat`

Final node jar SHA256:

```text
0f3f95feaf80d270671957bd9498c162951952526e8823b52b2ec4626e67459f  citu-node-v35-activation-514291-1.0.1.jar
```

## GitHub Release

GitHub account:

- `CorporateFounder`

Repository:

- `CorporateFounder/unitedStates_storage`

Release tag:

- `v35-node-activation-514291-1.0.1`

Release URL:

- `https://github.com/CorporateFounder/unitedStates_storage/releases/tag/v35-node-activation-514291-1.0.1`

Assets uploaded:

- `citu-node-v35-activation-514291-1.0.1.jar`
- `CITU_V35_514291_NODE_1.0.1_SHA256SUMS.txt`
- `CITU_V35_514291_NODE_1.0.1_RELEASE_NOTES_EN.md`
- `CITU_V35_514291_NODE_1.0.1_RELEASE_NOTES_RU.md`
- `start-node-windows.bat`

No wallet assets were uploaded. No website assets were uploaded.

## Public download verification

Downloaded from the GitHub Release into:

- `C:\citu\tmp\phase11h1-public-download`

Verification result:

- Expected SHA256 from uploaded checksum file: `0f3f95feaf80d270671957bd9498c162951952526e8823b52b2ec4626e67459f`
- Actual downloaded jar SHA256: `0f3f95feaf80d270671957bd9498c162951952526e8823b52b2ec4626e67459f`
- Match: `true`

Downloaded jar class inspection confirmed the patched OOM guard classes/constants are present:

- `International_Trade_Union.utils.UtilUrl`
- `International_Trade_Union.utils.UtilsResolving`
- `International_Trade_Union.controllers.TransactionController`
- `DEFAULT_CONNECT_TIMEOUT_MILLIS`
- `MAX_GET_RESPONSE_CHARS`
- `MAX_PROPAGATION_HOSTS`
- `MAX_GET_TRANSACTIONS_RESPONSE`

## VPS commands

Node 1.0.1 release asset URL:

- `https://github.com/CorporateFounder/unitedStates_storage/releases/download/v35-node-activation-514291-1.0.1/citu-node-v35-activation-514291-1.0.1.jar`

Exact Windows VPS download command:

```bat
powershell -NoProfile -ExecutionPolicy Bypass -Command "[Net.ServicePointManager]::SecurityProtocol=[Net.SecurityProtocolType]::Tls12; Invoke-WebRequest -UseBasicParsing -Uri 'https://github.com/CorporateFounder/unitedStates_storage/releases/download/v35-node-activation-514291-1.0.1/citu-node-v35-activation-514291-1.0.1.jar' -OutFile 'C:\citu-node-v35-activation-514291-1.0.1.jar'"
```

Exact VPS start command:

```bat
java -Xms512m -Xmx1536m -XX:+UseG1GC -XX:MaxGCPauseMillis=500 -XX:+HeapDumpOnOutOfMemoryError -XX:HeapDumpPath=C:\server\logs\citu-node-v35-514291.hprof -XX:+ExitOnOutOfMemoryError -Dserver.tomcat.threads.max=80 -Dserver.tomcat.accept-count=100 -Dserver.tomcat.max-connections=1024 -Dserver.port=82 -Dserver.address=194.87.236.238 -jar C:\citu-node-v35-activation-514291-1.0.1.jar
```

## Safety confirmation

Confirmed:

- No wallet project files changed.
- No website project files changed.
- No `Seting.java` file changed.
- `V35_RESCUE_FORK_INDEX` remains `514291`.
- `V35_BURN_ADDRESS` remains `BURN_ADDRESS`.
- No consensus logic changed.
- No activation height changed.
- No reward logic changed.
- No `C:\resources` touched.
- No `C:\server\resources` touched.
- No VPS deploy/restart/kill performed.
- No burn transaction created.
- No jackpot payout created.
- No wallet release upload.
- No website release upload.
- No source push performed.

## Recommended next phase

Recommended next phase: controlled VPS node restart/deployment approval for node `1.0.1`, after live resource backup and explicit operator approval.
