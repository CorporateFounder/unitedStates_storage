# CITU V35 Phase 11H-4 Node + Wallet 1.0.2 Diagnostic Release Report

## Executive summary
Phase 11H-4 created and published node and wallet 1.0.2 diagnostic releases for V35 block-submission and node OOM observability. The release is diagnostic-only: it does not change consensus, rewards, V35 activation height, burn address, jackpot logic, website assets, or VPS runtime state.

## Root cause from Phase 11H-3
Wallet mining submits found blocks to the node endpoint `/nodes/resolve_from_to_block`. The server receive endpoint validates and queues the block in `winnerList`; `/size` increases only later when `TournamentService.tournament(...)` persists the selected winner through `utilsResolving.addBlock3(...)`. If node memory instability or OOM occurs before/during tournament persistence, the submitted block can be observed in receive/queue diagnostics while `/size` remains unchanged.

## Files changed
Committed in source commit `a60c3a5 phase 11H4 release node wallet 1.0.2 diagnostics`:
- `CITU_V35_PHASE11H3_BLOCK_SUBMISSION_ROOT_CAUSE_INVESTIGATION_REPORT.md`
- `unitedStates_storage-master/pom.xml`
- `unitedStates_storage-master/src/main/java/International_Trade_Union/controllers/BasisController.java`
- `unitedStates_storage-master/src/main/java/International_Trade_Union/model/TournamentService.java`
- `unitedStates_storage-master/src/test/java/International_Trade_Union/controllers/BlockSubmitDebugStatusTest.java`
- `unitedStates_storage-master/src/test/java/International_Trade_Union/model/TournamentServiceMemoryGuardTest.java`
- `unitedStates_final-master/pom.xml`
- `unitedStates_final-master/src/main/java/International_Trade_Union/controllers/BasisController.java`
- `unitedStates_final-master/src/main/resources/templates/processUpdating.html`
- `unitedStates_final-master/src/test/java/International_Trade_Union/controllers/WalletMiningProcessingTemplateTest.java`

No website project files were changed in this phase.

## Versions bumped
- Node/server Maven version: `1.0.1` -> `1.0.2`
- Wallet Maven version: `1.0.1` -> `1.0.2`

## Diagnostic changes
Node/server:
- Added/kept read-only `/debug/block-submit/status` for last received block, validation result, persistence result, queue size, and JVM memory state.
- Added explicit `SERVER_BLOCK_RECEIVED`, `SERVER_BLOCK_VALIDATE_RESULT`, `SERVER_BLOCK_ADD_SUCCESS`, `SERVER_BLOCK_ADD_REJECTED`, and `SERVER_BLOCK_ADD_ERROR` logs.
- Added tournament memory/winner-list breadcrumbs around peer winner fetches and `addBlock3` persistence.
- Kept peer tournament memory guard limits and HTTP timeouts.

Wallet:
- Added/kept wallet mining submit telemetry for found block, submit URL, method, HTTP status, response/error, and elapsed time.
- Added `WALLET_MINING_STARTED` logs and UI visibility on `/processUpdating`.
- Confirmed wallet submit target remains node endpoint `http://194.87.236.238:82/nodes/resolve_from_to_block`.

## Tests and packages
Server:
- `cd C:\citu\unitedStates_storage-master`
- `.\mvnw.cmd test`: PASS, `246/246`, failures `0`, errors `0`, skipped `0`.
- `.\mvnw.cmd clean package`: PASS, produced `target\unitedStates-1.0.2.jar`.

Wallet:
- `cd C:\citu\unitedStates_final-master`
- `.\mvnw.cmd test`: PASS, `212/212`, failures `0`, errors `0`, skipped `0`.
- `.\mvnw.cmd clean package`: PASS, produced `target\unitedStates-1.0.2.jar`.

## Release folders and artifact names
Node release folder:
- `C:\citu\release-dryrun\v35-514291-node-1.0.2`

Node artifacts:
- `citu-node-v35-activation-514291-1.0.2.jar`
- `start-node-windows.bat`
- `CITU_V35_514291_NODE_1.0.2_SHA256SUMS.txt`
- `CITU_V35_514291_NODE_1.0.2_RELEASE_NOTES_EN.md`
- `CITU_V35_514291_NODE_1.0.2_RELEASE_NOTES_RU.md`

Wallet release folder:
- `C:\citu\release-dryrun\v35-514291-wallet-1.0.2`

Wallet artifacts:
- `citu-wallet-v35-activation-514291-1.0.2.jar`
- `start-wallet-windows.bat`
- `CITU_V35_514291_WALLET_1.0.2_SHA256SUMS.txt`
- `CITU_V35_514291_WALLET_1.0.2_RELEASE_NOTES_EN.md`
- `CITU_V35_514291_WALLET_1.0.2_RELEASE_NOTES_RU.md`

## SHA256
Node jar:
- `a0b09a8d014bf525c2f5aa550b3ec5c600e825e33ed0797fe870dea0a49a969d  citu-node-v35-activation-514291-1.0.2.jar`

Wallet jar:
- `b2ea4d41925326a8b44115177db17a06ee25e6bfab0e832d7da2153313196179  citu-wallet-v35-activation-514291-1.0.2.jar`

## GitHub releases
Node release:
- Repository: `CorporateFounder/unitedStates_storage`
- Tag: `v35-node-activation-514291-1.0.2`
- URL: `https://github.com/CorporateFounder/unitedStates_storage/releases/tag/v35-node-activation-514291-1.0.2`
- Uploaded assets: node jar, node Windows start script, node SHA256SUMS, node EN/RU release notes.

Wallet release:
- Repository: `CorporateFounder/unitedStates_final`
- Tag: `v35-wallet-activation-514291-1.0.2`
- URL: `https://github.com/CorporateFounder/unitedStates_final/releases/tag/v35-wallet-activation-514291-1.0.2`
- Uploaded assets: wallet jar, wallet Windows start script, wallet SHA256SUMS, wallet EN/RU release notes.

## Public download verification
Downloaded published assets into `C:\tmp\citu-phase11h4-public-verify` and verified:
- Node downloaded SHA matched uploaded SHA file: PASS.
- Wallet downloaded SHA matched uploaded SHA file: PASS.
- Node jar contains `BasisController.class` and `TournamentService.class`: PASS.
- Wallet jar contains `BasisController.class` and `templates/processUpdating.html`: PASS.

## VPS download commands
Node:
```powershell
powershell -NoProfile -ExecutionPolicy Bypass -Command "[Net.ServicePointManager]::SecurityProtocol=[Net.SecurityProtocolType]::Tls12; Invoke-WebRequest -UseBasicParsing -Uri 'https://github.com/CorporateFounder/unitedStates_storage/releases/download/v35-node-activation-514291-1.0.2/citu-node-v35-activation-514291-1.0.2.jar' -OutFile 'C:\citu-node-v35-activation-514291-1.0.2.jar'"
```

Wallet:
```powershell
powershell -NoProfile -ExecutionPolicy Bypass -Command "[Net.ServicePointManager]::SecurityProtocol=[Net.SecurityProtocolType]::Tls12; Invoke-WebRequest -UseBasicParsing -Uri 'https://github.com/CorporateFounder/unitedStates_final/releases/download/v35-wallet-activation-514291-1.0.2/citu-wallet-v35-activation-514291-1.0.2.jar' -OutFile 'C:\citu-wallet-v35-activation-514291-1.0.2.jar'"
```

## VPS start commands
Node diagnostic start command:
```bat
java -Xms512m -Xmx1536m -XX:+UseG1GC -XX:MaxGCPauseMillis=500 -XX:+HeapDumpOnOutOfMemoryError -XX:HeapDumpPath=C:\server\logs\citu-node-v35-514291.hprof -XX:+ExitOnOutOfMemoryError -Dserver.tomcat.threads.max=80 -Dserver.tomcat.accept-count=100 -Dserver.tomcat.max-connections=1024 -Dserver.port=82 -Dserver.address=194.87.236.238 -jar C:\citu-node-v35-activation-514291-1.0.2.jar
```

Wallet diagnostic start command:
```bat
java -Xms256m -Xmx1024m -XX:+UseG1GC -XX:MaxGCPauseMillis=500 -XX:+HeapDumpOnOutOfMemoryError -XX:HeapDumpPath=C:\server\logs\citu-wallet-v35-514291.hprof -Dserver.port=8082 -jar C:\citu-wallet-v35-activation-514291-1.0.2.jar
```

## Diagnostic URLs
- Node block submission status: `http://194.87.236.238:82/debug/block-submit/status`
- Wallet mining submit panel: `http://localhost:8082/processUpdating`

## Safety confirmation
- No website project changes in Phase 11H-4.
- Website remains on its separate 1.0.0 release line and was not uploaded.
- No `Seting.java` file was changed in Phase 11H-4.
- `V35_RESCUE_FORK_INDEX` remains `514291`.
- `V35_BURN_ADDRESS` remains `BURN_ADDRESS`.
- No consensus, reward, mining target, block format, transaction format, or jackpot logic change was made.
- No founder burn transaction was created, signed, broadcast, or simulated.
- No jackpot payout transaction was created.
- No `C:\resources` or `C:\server\resources` folder was touched.
- No VPS deploy, restart, or process kill was performed.
- No node/wallet assets outside the 1.0.2 releases were uploaded.

## Recommended next phase
Proceed to a controlled diagnostic VPS update only after explicit approval: download the 1.0.2 node/wallet jars, back up live resources, restart node and wallet in controlled order, then observe `/debug/block-submit/status`, wallet `/processUpdating`, and JVM heap behavior during mining.
