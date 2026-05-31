# CITU V35 Phase 10F-1E: Local Server Startup and V35 Endpoint Audit

## Executive summary

Phase 10F-1E was a read-only audit. No source code, tests, templates, constants, deployment state, release artifacts, checksums, or tags were changed.

The failed isolated H2 launch exposed a real clean-start/bootstrap defect in the server startup path. The command was also incomplete as an isolation command: overriding the H2 database does not redirect the filesystem blockchain state. Runtime blockchain files remain fixed at `C:\server\resources\...`.

The V35 read-only endpoints are implemented in the server controllers. They were not started or called in this phase because the available local runtime tree is empty and the current application has no safe configurable resource-root switch. A smoke launch against the validated replay tree would require staging or binding state into `C:\server\resources`, which is outside this report-only phase and could mutate local chain state.

## Safety audit

| Item | Result |
|---|---|
| Current branch | `master` |
| Server `V35_RESCUE_FORK_INDEX` | `Long.MAX_VALUE` |
| Wallet `V35_RESCUE_FORK_INDEX` | `Long.MAX_VALUE` |
| Server/wallet `V35_BURN_ADDRESS` | `"BURN_ADDRESS"` |
| `Seting.java` diff | None |
| V35 activation | Disabled |

The working tree already contained unrelated wallet UI/documentation changes before this report. They were not modified by Phase 10F-1E.

## Root cause

The failure is caused by an empty filesystem blockchain, not by H2 itself.

Startup enters `BasisController.initializeBlockchain()`:

1. `UtilsCreatedDirectory.createPackages()` creates missing directories and empty files.
2. `Seting.TEMPORARY_BLOCKCHAIN_FILE` is read.
3. The local runtime metadata currently reports `size: 0`.
4. `blockcheinSize` becomes `0`.
5. Startup requests the previous block at `blockcheinSize - 1`, which is `-1`.
6. Startup calls `UtilsBlock.difficulty()` with an empty block list.
7. `UtilsBlock.difficulty()` dereferences `blocks.get(blocks.size() - 1)`, which becomes `blocks.get(-1)`.

Observed runtime metadata:

```json
{"size":0,"hashCount":0,"staking":0,"transactions":0,"bigRandomNumber":0,"cumulativeWork":"0","tipHash":"","validation":true}
```

Validated replay metadata inspected for comparison:

```json
{"size":506096,"hashCount":8162243,"staking":4190677.19777960000,"transactions":1235993,"bigRandomNumber":193869803,"validation":true}
```

## Classification

This is both:

- an incomplete smoke-launch command, because it isolates only H2; and
- a real clean-start/bootstrap server defect, because a newly created empty filesystem state is not handled safely.

The defect does not block an upgrade of a reviewed node that already has a coherent non-empty chain state. It does block clean-start server operation and a reliable isolated local endpoint smoke test. It should be patched and covered by a startup test before claiming clean-install/bootstrap support or final operational readiness.

## Files inspected

- `unitedStates_storage-master/src/main/java/International_Trade_Union/controllers/BasisController.java`
- `unitedStates_storage-master/src/main/java/International_Trade_Union/utils/UtilsBlock.java`
- `unitedStates_storage-master/src/main/java/International_Trade_Union/utils/UtilsCreatedDirectory.java`
- `unitedStates_storage-master/src/main/java/International_Trade_Union/controllers/V35SupplyController.java`
- `unitedStates_storage-master/src/main/java/International_Trade_Union/controllers/V35JackpotController.java`
- `unitedStates_storage-master/src/main/java/International_Trade_Union/setings/Seting.java`
- `unitedStates_final-master/src/main/java/International_Trade_Union/setings/Seting.java`
- `unitedStates_storage-master/src/main/resources/application.properties`
- `unitedStates_storage-master/src/test/java/International_Trade_Union/controllers/V35SupplyControllerTest.java`
- `unitedStates_storage-master/src/test/java/International_Trade_Union/controllers/V35JackpotControllerTest.java`
- `unitedStates_storage-master/src/test/java/UtilsBalanceTest.java`
- `unitedStates_storage-master/src/test/java/Testing.java`

## Runtime state location

`Seting.java` uses an empty `testPath`, so runtime state is rooted at:

```text
C:\server\resources\
```

Important paths:

```text
C:\server\resources\blockchain\
C:\server\resources\tempblockchain\shortBlockchain.txt
C:\server\resources\h2DB\
C:\server\resources\balance\
```

The application property default H2 database is aligned with that tree:

```text
jdbc:h2:file:/server/resources/h2DB/db;DB_CLOSE_ON_EXIT=FALSE;AUTO_RECONNECT=TRUE
```

The failed smoke command redirected only H2 to:

```text
./target/smoke/server-db
```

It did not redirect the blockchain, balance, or short-chain metadata paths. The current `C:\server\resources\blockchain\` contains `0` files. The validated replay tree contains `270` blockchain files at:

```text
C:\citu_chain_replay_full\resources\blockchain\
```

## Correct safe local launch method

There is no safe one-command isolated launch against an arbitrary resource directory in the current code. The resource root is not configurable.

For a future smoke run, use a disposable Windows VM, container, or separately approved configurable-resource-root patch. In that disposable environment:

1. Stage a reviewed coherent non-empty resource tree at `C:\server\resources\`.
2. Keep `blockchain\`, `tempblockchain\shortBlockchain.txt`, H2 state, balances, and related state mutually consistent.
3. Launch without redirecting H2 away from that resource tree:

```powershell
cd C:\citu\unitedStates_storage-master
java -jar target\unitedStates-0.0.423-SNAPSHOT.jar --server.address=127.0.0.1 --server.port=8083
```

Then call:

```powershell
Invoke-RestMethod http://127.0.0.1:8083/size
Invoke-RestMethod http://127.0.0.1:8083/v35/protocol
Invoke-RestMethod http://127.0.0.1:8083/v35/supply
Invoke-RestMethod http://127.0.0.1:8083/v35/burn
Invoke-RestMethod http://127.0.0.1:8083/v35/founder
Invoke-RestMethod http://127.0.0.1:8083/v35/founder/burn-snapshot
Invoke-RestMethod http://127.0.0.1:8083/v35/jackpot/status
```

Do not run this against production state. Do not mix an isolated H2 database with a different filesystem chain tree.

## V35 endpoint implementation status

| Endpoint | Controller | Implemented | Smoke result |
|---|---|---:|---|
| `/size` | `BasisController` | Yes | Not called: startup blocked |
| `/v35/protocol` | `V35SupplyController` | Yes | Not called: startup blocked |
| `/v35/supply` | `V35SupplyController` | Yes | Not called: startup blocked |
| `/v35/burn` | `V35SupplyController` | Yes | Not called: startup blocked |
| `/v35/founder` | `V35SupplyController` | Yes | Not called: startup blocked |
| `/v35/founder/burn-snapshot` | `V35SupplyController` | Yes | Not called: startup blocked |
| `/v35/jackpot/status` | `V35JackpotController` | Yes | Not called: startup blocked |

The V35 controllers are read-only. Their direct unit tests use an empty fake `BlockService` safely and assert that no block or account mutation occurs.

## Test coverage review

Existing controller tests cover safe empty-database responses when V35 controllers are instantiated directly:

- `V35SupplyControllerTest`
- `V35JackpotControllerTest`

No dedicated test was found that boots the full server with an empty filesystem blockchain and exercises `BasisController.initializeBlockchain()`. The direct controller tests therefore do not cover this startup defect.

## Empty-chain startup patch need

A separate code-hardening phase is recommended. It should:

- define the intended clean-start/bootstrap policy explicitly;
- prevent `indexFromFile(-1)` and `difficulty(emptyList)` calls;
- return a clear startup state or bootstrap error when no genesis/non-empty chain exists;
- add a focused application-startup regression test using an isolated resource root;
- preferably make the runtime resource root configurable for safe local smoke tests.

No patch was made in Phase 10F-1E.

## Release impact

| Area | Status |
|---|---|
| Existing-node V35 upgrade path | Not disproved by this audit |
| Clean-start/bootstrap server path | Needs patch/review |
| Local endpoint smoke audit | Blocked until isolated coherent state is available |
| V35 endpoint source implementation | Present |
| V35 activation | Still disabled |

## Safety confirmations

- No `Seting.java` file changed.
- `V35_RESCUE_FORK_INDEX` remains `Long.MAX_VALUE` in server and wallet.
- `V35_BURN_ADDRESS` remains `"BURN_ADDRESS"`.
- No activation occurred.
- No burn transaction was created, signed, broadcast, or simulated.
- No jackpot payout transaction was created.
- No deployment, tag, release artifact, or checksum action occurred.
- No historical replay or test suite was run.

## Recommended next phase

Run a separate server clean-start/bootstrap hardening phase. Add an isolated-resource-root mechanism and a focused empty-chain startup regression test, then repeat the local V35 endpoint smoke audit against a disposable coherent non-empty state tree.
