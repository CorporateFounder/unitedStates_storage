# CITU V35 Phase 10F-1F: Server Clean-Start Bootstrap Hardening

## Executive summary

Phase 10F-1F fixed the empty-chain startup crash with a narrow server-only bootstrap guard. The server now starts with an empty local filesystem chain and an isolated H2 smoke database, exposes the requested read-only V35 endpoints, and reports V35 as disabled.

The patch does not create genesis data, activate V35, change consensus constants, or alter non-empty chain validation. Server tests and `clean package` passed with `228/228` tests.

## Root cause

`BasisController.initializeBlockchain()` loaded short metadata with `size = 0`, then requested block index `-1` and called `UtilsBlock.difficulty()` with an empty list. `UtilsBlock.difficulty()` dereferenced:

```java
blocks.get(blocks.size() - 1)
```

For an empty list, this became `blocks.get(-1)` and threw `IndexOutOfBoundsException`.

## Files changed

- `unitedStates_storage-master/src/main/java/International_Trade_Union/controllers/BasisController.java`
- `unitedStates_storage-master/src/main/java/International_Trade_Union/utils/UtilsBlock.java`
- `unitedStates_storage-master/src/test/java/International_Trade_Union/utils/UtilsBlockEmptyChainBootstrapTest.java`
- `CITU_V35_PHASE10F1F_SERVER_CLEAN_START_BOOTSTRAP_HARDENING_REPORT.md`

Pre-existing wallet UI/documentation changes remained in the working tree and were not modified by this phase.

## Empty-chain hardening approach

`UtilsBlock.difficulty()` now returns the existing legacy bootstrap/default difficulty value `1` when its block list is null or empty.

`BasisController.initializeBlockchain()` now handles `blockcheinSize == 0` explicitly:

- `prevBlock = null`
- `dificultyOneBlock = 1`
- log `empty local chain: bootstrap state`
- return before any `indexFromFile(-1)` call

`GET /size` now refreshes `prevBlock` only when the refreshed blockchain size is non-zero. An empty chain returns size `0` safely.

No synthetic genesis block is created.

## Why non-empty behavior is unchanged

All new branches are limited to null or empty-chain conditions:

- non-empty difficulty calculation continues through the prior implementation;
- non-empty startup still loads the previous block and existing difficulty behavior;
- validation, rewards, PoW, block format, transaction format, replay compatibility, and V35 logic were not changed.

## Regression tests

Added `UtilsBlockEmptyChainBootstrapTest`:

| Test | Result |
|---|---|
| Empty list returns legacy bootstrap difficulty `1` | PASS |
| Null list returns legacy bootstrap difficulty `1` | PASS |
| V35 constants remain `Long.MAX_VALUE` and `BURN_ADDRESS` | PASS |

The local jar smoke launch also verified the full Spring startup path with empty chain metadata.

## Server tests and package

Commands run:

```powershell
cd C:\citu\unitedStates_storage-master
.\mvnw.cmd "-Dtest=International_Trade_Union.utils.UtilsBlockEmptyChainBootstrapTest" test
.\mvnw.cmd test
.\mvnw.cmd clean package
```

Results:

| Verification | Result |
|---|---|
| Focused regression tests | PASS, `3/3` |
| Full server tests | PASS, `228/228` |
| Server clean package | PASS, `BUILD SUCCESS` |

Generated tracked `target` changes and disposable smoke files were cleaned after verification. No release artifact was prepared.

## Local server launch

Smoke command:

```powershell
cd C:\citu\unitedStates_storage-master
java -jar target\unitedStates-0.0.423-SNAPSHOT.jar --server.address=127.0.0.1 --server.port=8083 "--spring.datasource.url=jdbc:h2:file:./target/smoke/server-db;DB_CLOSE_ON_EXIT=FALSE;AUTO_RECONNECT=TRUE"
```

Result: PASS. Tomcat started on `127.0.0.1:8083`, and startup logged:

```text
static: shortDataBlockchain: DataShortBlockchainInformation(size=0, ...)
empty local chain: bootstrap state
Started UnitedStatesApplication
```

The hidden smoke process was stopped after endpoint checks. Runtime blockchain files remained at count `0`, and short metadata remained `size: 0`.

## V35 endpoint smoke results

| Endpoint | HTTP status | Short result |
|---|---:|---|
| `/size` | `200` | `0` |
| `/v35/protocol` | `200` | `v35Active=false`, fork index `Long.MAX_VALUE`, economics `200 / 432 / 8 / 6 / 2 / 0` |
| `/v35/supply` | `200` | Disabled, zero empty-state supply |
| `/v35/burn` | `200` | `burnTransactionCreated=false` |
| `/v35/founder` | `200` | Model-only, actual burned supply `0.00` |
| `/v35/founder/burn-snapshot` | `200` | Height `-1`, hash `UNKNOWN`, no created burn transaction |
| `/v35/jackpot/status` | `200` | Disabled policy status |
| `/v35/jackpot/periods` | `200` | Disabled policy periods |
| `/v35/jackpot/history` | `200` | Disabled, empty history |

## Constants audit

| Project | `V35_RESCUE_FORK_INDEX` | `V35_BURN_ADDRESS` |
|---|---|---|
| Server | `Long.MAX_VALUE` | `"BURN_ADDRESS"` |
| Wallet | `Long.MAX_VALUE` | `"BURN_ADDRESS"` |

No `Seting.java` file changed.

## Remaining risks

The empty-state server launch is safe from the original index error, but it is not fully offline. During smoke startup, the application automatically discovered configured public peers and attempted synchronization reads. No blockchain files were persisted during this short run.

A future hardening phase should add an explicit offline/local-smoke profile or configurable peer list so endpoint smoke tests cannot contact public nodes. A configurable resource root would also make isolated bootstrap testing cleaner.

Other empty-chain operational methods outside the requested read-only endpoint set may still assume a non-null previous block. This patch intentionally hardens startup and the `/size` display path without inventing a genesis workflow.

## Safety confirmations

- No server or wallet `Seting.java` file changed.
- V35 remains disabled.
- No V35 activation occurred.
- No burn transaction was created, signed, broadcast, or simulated.
- No jackpot payout transaction was created.
- No deployment, tag, release artifact, or checksum action occurred.
- No historical replay was run.
- No wallet or website code was modified by this phase.

## Recommended next phase

Run a focused offline-smoke-profile hardening phase. Make peer discovery suppressible for local smoke tests, then repeat the same endpoint smoke audit without any public-node contact.
