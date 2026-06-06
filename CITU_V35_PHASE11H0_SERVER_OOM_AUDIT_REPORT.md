# CITU V35 Phase 11H-0 Server OutOfMemory Audit Report

## Executive summary

Phase 11H-0 inspected the public node/server memory-risk paths after a VPS log showed `java.lang.OutOfMemoryError` in the Tomcat NIO poller thread after the V35 release startup.

The audit did not find a V35 consensus/reward issue. The practical OOM candidates are runtime pressure from public-node request handling and peer networking:

- outbound peer HTTP reads used long/default timeouts and unbounded response reads;
- peer update/propagation paths could schedule work against large host lists;
- `/getTransactions` returned the complete in-memory transaction pool without a response cap;
- website/wallet/public callers can amplify pressure if the node is already slow or unstable.

Targeted server-only memory guards were added. No wallet project, website project, `Seting.java`, consensus logic, V35 height, rewards, burn logic, jackpot payout logic, resources, deployment, restart, upload, tag, or commit action was performed.

## Source status and safety audit

- Project root: `C:\citu`
- Server project: `C:\citu\unitedStates_storage-master`
- Wallet project: not modified.
- Website project: not modified.
- Server `Seting.java`: not modified.
- Wallet `Seting.java`: not modified.
- Server `V35_RESCUE_FORK_INDEX`: remains `514291`.
- Wallet `V35_RESCUE_FORK_INDEX`: remains `514291`.
- Server `V35_BURN_ADDRESS`: remains `BURN_ADDRESS`.
- Wallet `V35_BURN_ADDRESS`: remains `BURN_ADDRESS`.

Pre-existing unrelated untracked files remain outside this phase scope:

- `CITU_V35_PHASE11G0_RESOURCE_PATH_AUDIT_BEFORE_VPS_RESTART_REPORT.md`
- `CITU_V35_PHASE11G6_WEBSITE_EMERGENCY_HTTP_ONLY_FAST_PATCH_REPORT.md`
- `Cstart_citu_v35_all.bat`
- `unitedStates_final-master.zip`

## Code paths inspected

Server memory-heavy paths reviewed:

- Peer host metadata and `/datashort` reads in `UtilsResolving.fetchDataFromHosts`.
- Peer priority sorting in `UtilsResolving.sortPriorityHost`.
- Block propagation in `UtilsResolving.sendAllBlocksToStorage`.
- Peer HTTP utility calls in `UtilUrl`.
- Public transaction pool response in `TransactionController.getTransaction` (`/getTransactions`).
- `/size` behavior in `BasisController.sizeBlockchain`.
- `/datashort` behavior in `BasisController`.
- V35 endpoint status remained outside the OOM hot path and was not changed.

## Root cause candidates

The exact VPS heap dump was not available in this phase, so the finding is a bounded root-cause candidate list rather than a single proven heap object.

Most likely contributors:

1. Long-lived outbound HTTP operations against peers.
   `UtilUrl.readJsonFromUrl` used `18000` ms connect/read timeouts and read responses into an unbounded `StringBuilder`. Slow, bad, or malicious peers could keep request threads and buffers alive longer than necessary.

2. Peer fan-out and temporary future lists.
   Peer discovery/update and block propagation could schedule work over large host lists. Some paths capped random peer selection, while others still accepted full lists or used fixed pools without bounding the scheduled input.

3. Unbounded transaction pool exposure.
   `/getTransactions` returned every pending transaction. A large pending pool could become a large JSON serialization response and amplify memory pressure from public callers.

4. Public API amplification.
   Wallet mining, website pages, and external users can repeatedly call `/size`, `/datashort`, `/getTransactions`, V35 status endpoints, and block-add endpoints. If the node is already slow, these calls can accumulate in Tomcat worker/NIO structures.

Less likely but still worth monitoring:

- `/size` is normally lightweight because it returns cached `blockcheinSize`, but if the size is zero it can inspect chain files. This was not changed in this phase because changing bootstrap behavior would be broader than an OOM guard.
- Block validation and replay code can naturally hold larger block/account structures, but the reported OOM happened in server runtime after startup, not during historical replay.

## Files changed

Server source:

- `unitedStates_storage-master/src/main/java/International_Trade_Union/utils/UtilUrl.java`
- `unitedStates_storage-master/src/main/java/International_Trade_Union/utils/UtilsResolving.java`
- `unitedStates_storage-master/src/main/java/International_Trade_Union/controllers/TransactionController.java`

Server tests:

- `unitedStates_storage-master/src/test/java/International_Trade_Union/utils/UtilUrlMemoryGuardTest.java`
- `unitedStates_storage-master/src/test/java/International_Trade_Union/utils/UtilsResolvingMemoryGuardTest.java`
- `unitedStates_storage-master/src/test/java/International_Trade_Union/controllers/TransactionControllerMemoryGuardTest.java`

Report:

- `CITU_V35_PHASE11H0_SERVER_OOM_AUDIT_REPORT.md`

## Changes made

### HTTP utility limits

`UtilUrl` now has explicit public constants:

- `DEFAULT_CONNECT_TIMEOUT_MILLIS = 3000`
- `DEFAULT_READ_TIMEOUT_MILLIS = 6000`
- `SILENT_TIMEOUT_MILLIS = 3000`
- `MAX_GET_RESPONSE_CHARS = 1_000_000`

Generic GET reads now use bounded reads and fail with `IOException` if a peer response exceeds the cap. HTTP connections are still disconnected in `finally`.

### Peer update and propagation limits

`UtilsResolving` now:

- caps peer metadata work to `MAX_PROPAGATION_HOSTS`;
- derives executor size from host count, processor count, and `RANDOM_HOSTS`;
- handles empty selected host lists without creating a zero-size thread pool;
- caps block propagation targets to `MAX_PROPAGATION_HOSTS`;
- bounds propagation wait with `orTimeout(12, TimeUnit.SECONDS)`;
- uses `shutdownNow()` after bounded propagation wait.

These changes limit runtime fan-out and stuck future retention without changing block validation, rewards, PoW, transaction format, block format, or V35 consensus rules.

### Public transaction response cap

`TransactionController.getTransaction` now caps `/getTransactions` responses to `500` transactions. The in-memory transaction pool itself is not changed; only the public response is bounded.

## Tests and regression checks

Added tests verify:

- configured URLConnection timeouts are applied;
- oversized simple GET responses are rejected;
- default peer HTTP timeout/response constants remain bounded;
- `/getTransactions` returns empty for null and caps oversized lists to `500`;
- peer propagation host cap tracks `Seting.RANDOM_HOSTS`;
- peer pool size never becomes zero and never exceeds the propagation cap.

## Verification result

Commands run in `C:\citu\unitedStates_storage-master`:

```bat
.\mvnw.cmd test
.\mvnw.cmd clean package
```

Result:

- Server tests: PASS, `239` tests run, `0` failures, `0` errors, `0` skipped.
- Server clean package: PASS.
- Built jar: `target\unitedStates-1.0.0.jar`.

Maven warnings observed but not introduced by this phase:

- duplicate dependency declarations in `pom.xml`;
- log4j appender warnings during tests.

## Consensus and activation safety

Confirmed:

- No `Seting.java` file was changed.
- `V35_RESCUE_FORK_INDEX` remains `514291`.
- `V35_BURN_ADDRESS` remains `BURN_ADDRESS`.
- No consensus logic was changed.
- No mining reward logic was changed.
- No PoW logic was changed.
- No block format was changed.
- No transaction format was changed.
- No burn transaction was created.
- No jackpot payout was created.
- No VPS deploy/restart/kill was performed.
- No resources were touched.
- No GitHub release/upload/tag/push/commit was performed.

## Recommended JVM launch flags

For the VPS public node, use explicit heap and diagnostics so the process has a stable ceiling and leaves evidence if memory is exhausted again.

Recommended baseline for a modest VPS:

```bat
java -Xms512m -Xmx1536m -XX:+UseG1GC -XX:MaxGCPauseMillis=500 -XX:+HeapDumpOnOutOfMemoryError -XX:HeapDumpPath=C:\server\logs\citu-node-v35-514291.hprof -XX:+ExitOnOutOfMemoryError -Dserver.tomcat.threads.max=80 -Dserver.tomcat.accept-count=100 -Dserver.tomcat.max-connections=1024 -Dserver.port=82 -Dserver.address=194.87.236.238 -jar C:\citu-node-v35-activation-514291-1.0.0.jar
```

If the VPS has less available memory, lower `-Xmx` conservatively, for example `-Xmx1200m`. If it has at least 4 GB free after OS and other Java processes, `-Xmx2g` is reasonable.

## Remaining risks

- Without a heap dump from the VPS OOM, the exact retained object graph is not proven.
- `/getTransactions` is now response-bounded, but the underlying transaction pool can still grow according to existing pool logic.
- Some validation/sync paths necessarily process block/account data and may still need heap sizing appropriate to live-chain data.
- Website and wallet should continue using short node API timeouts so they do not amplify node instability.

## Recommended next phase

Recommended next phase: Phase 11H-1, rebuild and publish a refreshed node release artifact with the server OOM guards, then perform a controlled VPS node restart only after backup and operator approval.
