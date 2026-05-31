# CITU V35 Phase 10F-1G: Offline Server Smoke Profile

## Executive summary

Phase 10F-1G added an explicit `smoke-offline` Spring profile for local server V35 endpoint testing. The profile prevents the startup tournament network thread from contacting public peers while preserving local read-only endpoints.

Normal production behavior remains unchanged when the profile is absent. No consensus logic, `Seting.java`, wallet code, website code, activation constants, deployment state, release artifacts, checksums, or tags were changed.

Server tests passed `231/231`, `clean package` passed, and all requested offline local endpoints returned `HTTP 200`.

## Startup peer-contact risk found

The startup network path is owned by the `Tournament` singleton:

1. Spring constructs `Tournament`.
2. `Tournament.init()` runs through `@PostConstruct`.
3. It calls `TimeSyncManager.syncTime()`.
4. It starts a daemon thread running `Tournament.run()`.
5. `Tournament.run()` calls `BasisController.getNodes()`.
6. It sorts public peers through `UtilsResolving.sortPriorityHost()`.
7. It checks peer readiness through `NodeChecker.checkNodes()`.
8. It synchronizes state through `TournamentService.updatingNodeEndBlocks()` and `UtilsResolving.resolve3()`.
9. The continuing loop can fetch transactions, broadcast host information, and repeat synchronization.

Other inspected startup hooks in `NodeController`, `TransactionController`, `ConductorController`, `Mining`, and `UtilsResolving` wire local services but do not initiate outbound network requests by themselves.

## Files changed

- `unitedStates_storage-master/src/main/java/International_Trade_Union/model/Tournament.java`
- `unitedStates_storage-master/src/main/resources/application-smoke-offline.properties`
- `unitedStates_storage-master/src/test/java/International_Trade_Union/model/TournamentOfflineSmokeModeTest.java`
- `CITU_V35_PHASE10F1G_OFFLINE_SMOKE_PROFILE_REPORT.md`

## Offline smoke implementation

`Tournament` now reads:

```java
@Value("${citu.smoke.offline:false}")
private boolean offlineSmoke;
```

At the beginning of `Tournament.init()`, after local service wiring, it checks:

```java
if (!shouldStartNetworkServices(offlineSmoke)) {
    System.out.println("offline smoke mode: tournament network services disabled");
    return;
}
```

The `smoke-offline` Spring profile enables the property:

```properties
citu.smoke.offline=true
```

This skips startup time synchronization and prevents the tournament daemon thread from starting. Consequently, peer discovery, readiness checks, live-chain synchronization, transaction fetching, and host broadcast paths are not entered during offline smoke testing.

## Why production behavior is unchanged

The property defaults to `false`:

```java
@Value("${citu.smoke.offline:false}")
```

When the `smoke-offline` profile is absent, `Tournament.init()` follows the original startup path. No peer logic, consensus logic, chain validation, rewards, PoW, block format, transaction format, replay compatibility, or constants were changed.

## Tests added

Added `TournamentOfflineSmokeModeTest`:

| Test | Result |
|---|---|
| Offline smoke mode disables tournament network services | PASS |
| Normal mode still starts tournament network services | PASS |
| V35 constants remain `Long.MAX_VALUE` and `BURN_ADDRESS` | PASS |

The prior empty-chain bootstrap regression test also remained green.

## Server test and package result

Commands run:

```powershell
cd C:\citu\unitedStates_storage-master
.\mvnw.cmd "-Dtest=International_Trade_Union.model.TournamentOfflineSmokeModeTest,International_Trade_Union.utils.UtilsBlockEmptyChainBootstrapTest" test
.\mvnw.cmd test
.\mvnw.cmd clean package
```

Results:

| Verification | Result |
|---|---|
| Focused offline/bootstrap tests | PASS, `6/6` |
| Full server tests | PASS, `231/231` |
| Server clean package | PASS, `BUILD SUCCESS` |

Generated tracked `target` changes and disposable smoke files were cleaned after verification. No release artifact was prepared.

## Offline local launch command

```powershell
cd C:\citu\unitedStates_storage-master
java -jar target\unitedStates-0.0.423-SNAPSHOT.jar `
  --server.address=127.0.0.1 `
  --server.port=8083 `
  --spring.profiles.active=smoke-offline `
  "--spring.datasource.url=jdbc:h2:file:./target/smoke-offline/server-db;DB_CLOSE_ON_EXIT=FALSE;AUTO_RECONNECT=TRUE"
```

Startup log confirmation:

```text
The following 1 profile is active: "smoke-offline"
empty local chain: bootstrap state
offline smoke mode: tournament network services disabled
Started UnitedStatesApplication
```

The captured stdout/stderr logs were scanned for configured public IPs, peer discovery, resolution, readiness checks, timeout errors, and `UtilUrl` activity. No outbound-network pattern was found. Stderr remained empty.

## Offline V35 endpoint smoke results

| Endpoint | HTTP status | Short result |
|---|---:|---|
| `/size` | `200` | `0` |
| `/v35/protocol` | `200` | Disabled; fork index `Long.MAX_VALUE`; economics `200 / 432 / 8 / 6 / 2 / 0` |
| `/v35/supply` | `200` | Disabled empty-state supply |
| `/v35/burn` | `200` | `burnTransactionCreated=false` |
| `/v35/founder` | `200` | Model-only; actual burned supply `0.00` |
| `/v35/founder/burn-snapshot` | `200` | Height `-1`, hash `UNKNOWN`, no burn transaction |
| `/v35/jackpot/status` | `200` | Disabled policy status |
| `/v35/jackpot/periods` | `200` | Disabled policy periods |
| `/v35/jackpot/history` | `200` | Disabled empty history |

The local smoke process was stopped after the checks. Runtime blockchain file count remained `0`, and short metadata remained `size: 0`.

## Constants audit

| Project | `V35_RESCUE_FORK_INDEX` | `V35_BURN_ADDRESS` |
|---|---|---|
| Server | `Long.MAX_VALUE` | `"BURN_ADDRESS"` |
| Wallet | `Long.MAX_VALUE` | `"BURN_ADDRESS"` |

No `Seting.java` file changed.

## Safety confirmations

- No V35 activation occurred.
- No burn transaction was created, signed, broadcast, or simulated.
- No jackpot payout transaction was created.
- No deployment, tag, release artifact, or checksum action occurred.
- No historical replay was run.
- No wallet or website code was modified.

## Remaining risks

The offline profile guards the startup tournament orchestration path. It is intended for local read-only smoke verification, not as a production runtime mode.

If future local smoke coverage expands to transaction submission, mining, or administrative endpoints, those paths should receive separate offline-mode review before use. A configurable resource root would still improve isolation for richer local tests.

## Recommended next phase

Run a local manual server screenshot/check pass using the documented `smoke-offline` profile, then proceed to a focused review of whether the new bootstrap and offline-smoke changes should be committed as a separate operational-hardening patch before activation work continues.
