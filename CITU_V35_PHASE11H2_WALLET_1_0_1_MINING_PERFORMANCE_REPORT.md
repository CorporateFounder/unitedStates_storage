# CITU V35 Phase 11H-2 Wallet 1.0.1 Mining Performance Report

## Executive summary

Phase 11H-2 completed a wallet-only 1.0.1 patch for mining performance and processing UI stability. The wallet release was built, tested, packaged, uploaded to GitHub, and publicly verified.

No server/node project files were changed. No website project files were changed. No `Seting.java` file was changed in this phase. V35 remains configured for activation at block `514291`, and `V35_BURN_ADDRESS` remains the internal placeholder `BURN_ADDRESS`.

## Mining behavior review

Pre-V35 mining was already multi-threaded through `Block.findHash_MT2`.

Before this patch, V35 mining in `Mining.mineV35Block` used a single `while (true)` nonce-search loop. That made V35 wallet mining much slower than legacy/pre-V35 mining.

After this patch, V35 mining uses a bounded fixed thread pool. Each worker searches a separate nonce sequence, and the first valid block stops the shared search. Consensus validation is unchanged: the patch still uses `V35TargetDifficulty.decodeCompactTarget(...)` and `V35PowValidator.isHashUnderTarget(...)`.

## Hash-rate display

Before this patch, the legacy mining hash-rate display used a misleading calculation based on one thread range and then multiplied by thread count.

After this patch, hash-rate is calculated from:

`totalAttempts / elapsedWallClockTime`

The display now supports `H/s`, `KH/s`, and `MH/s`, and includes the total attempt count.

## Remote transaction fetch behavior

Wallet mining startup now fetches remote `/getTransactions` with a short timeout:

- timeout: `2500 ms`
- unavailable or slow remote responses fall back to an empty/local transaction list
- mining continues instead of hanging on a slow node API

## Processing/mining UI changes

The wallet processing page was updated to show:

- activation block `514291`
- `v35Active=false` as expected before activation
- mining/updating status
- mining thread count
- hash-rate display
- current node size fallback
- last mining status
- clear legacy/pre-V35 warning before activation

Compatibility redirects were added for `/processing` and `/procecing`, and `/process-mining` now redirects to `/processUpdating` instead of self-redirecting.

## Files changed

- `unitedStates_final-master/pom.xml`
- `unitedStates_final-master/src/main/java/International_Trade_Union/controllers/BasisController.java`
- `unitedStates_final-master/src/main/java/International_Trade_Union/entity/blockchain/block/Block.java`
- `unitedStates_final-master/src/main/java/International_Trade_Union/model/Mining.java`
- `unitedStates_final-master/src/main/java/International_Trade_Union/network/AllTransactions.java`
- `unitedStates_final-master/src/main/resources/templates/processUpdating.html`
- `unitedStates_final-master/src/test/java/International_Trade_Union/controllers/WalletMiningProcessingTemplateTest.java`
- `unitedStates_final-master/src/test/java/International_Trade_Union/model/MiningPerformanceV35Test.java`
- `unitedStates_final-master/src/test/java/International_Trade_Union/network/AllTransactionsMiningFetchTest.java`

## Tests and package

Wallet tests:

- result: PASS
- tests run: `210`
- failures: `0`
- errors: `0`
- skipped: `0`

Wallet clean package:

- result: PASS
- built jar: `C:\citu\unitedStates_final-master\target\unitedStates-1.0.1.jar`

## Release artifact

Wallet release folder:

`C:\citu\release-dryrun\v35-514291-wallet-1.0.1`

Final wallet artifact:

`citu-wallet-v35-activation-514291-1.0.1.jar`

SHA256:

`f512b90fd427ec7d07f894df4328cc951c0a0a58f9b4f0b949dcb4fa6b503f93`

Release files:

- `citu-wallet-v35-activation-514291-1.0.1.jar`
- `CITU_V35_514291_WALLET_1.0.1_SHA256SUMS.txt`
- `CITU_V35_514291_WALLET_1.0.1_RELEASE_NOTES_EN.md`
- `CITU_V35_514291_WALLET_1.0.1_RELEASE_NOTES_RU.md`
- `start-wallet-windows.bat`

## GitHub Release

Repository:

`CorporateFounder/unitedStates_final`

Tag:

`v35-wallet-activation-514291-1.0.1`

Release URL:

https://github.com/CorporateFounder/unitedStates_final/releases/tag/v35-wallet-activation-514291-1.0.1

Wallet JAR URL:

https://github.com/CorporateFounder/unitedStates_final/releases/download/v35-wallet-activation-514291-1.0.1/citu-wallet-v35-activation-514291-1.0.1.jar

## Public download verification

The uploaded wallet JAR and SHA file were downloaded from GitHub Release into a temporary local folder.

Verification result:

- expected SHA256: `f512b90fd427ec7d07f894df4328cc951c0a0a58f9b4f0b949dcb4fa6b503f93`
- actual SHA256: `f512b90fd427ec7d07f894df4328cc951c0a0a58f9b4f0b949dcb4fa6b503f93`
- match: YES

The downloaded JAR was inspected and contains:

- `BOOT-INF/classes/International_Trade_Union/model/Mining.class`
- `BOOT-INF/classes/International_Trade_Union/entity/blockchain/block/Block.class`
- `BOOT-INF/classes/International_Trade_Union/network/AllTransactions.class`
- `BOOT-INF/classes/International_Trade_Union/controllers/BasisController.class`
- `BOOT-INF/classes/templates/processUpdating.html`

`javap` confirmed:

- `Mining.defaultMiningThreadCount`
- `Mining.formatHashRate`
- `Mining.recordMiningStatus`
- `AllTransactions.GET_TRANSACTIONS_TIMEOUT_MILLIS`
- `AllTransactions.fetchRemoteTransactions`

## VPS download command

```powershell
powershell -NoProfile -ExecutionPolicy Bypass -Command "[Net.ServicePointManager]::SecurityProtocol=[Net.SecurityProtocolType]::Tls12; Invoke-WebRequest -UseBasicParsing -Uri 'https://github.com/CorporateFounder/unitedStates_final/releases/download/v35-wallet-activation-514291-1.0.1/citu-wallet-v35-activation-514291-1.0.1.jar' -OutFile 'C:\citu-wallet-v35-activation-514291-1.0.1.jar'"
```

## VPS start command

```powershell
java -Xms256m -Xmx1024m -XX:+UseG1GC -XX:MaxGCPauseMillis=500 -XX:+HeapDumpOnOutOfMemoryError -XX:HeapDumpPath=C:\server\logs\citu-wallet-v35-514291.hprof -Dserver.port=8082 -jar C:\citu-wallet-v35-activation-514291-1.0.1.jar
```

## Safety confirmation

- No server/node project files changed.
- No website project files changed.
- No `Seting.java` file changed in this phase.
- `V35_RESCUE_FORK_INDEX` remains `514291`.
- `V35_BURN_ADDRESS` remains `BURN_ADDRESS`.
- No consensus rule changed.
- No reward rule changed.
- No burn transaction was created.
- No jackpot payout transaction was created.
- No VPS deploy was performed.
- No VPS restart or process kill was performed.
- No node release asset was uploaded.
- No website release asset was uploaded.

## Recommended next phase

Recommended next phase: controlled wallet VPS update using the verified wallet 1.0.1 artifact, after explicit deployment approval and a fresh process/port check.
