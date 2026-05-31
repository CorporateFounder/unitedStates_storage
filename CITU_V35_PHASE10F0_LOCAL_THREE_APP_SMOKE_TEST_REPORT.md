# CITU V35 Phase 10F-0 - Local Three-App Smoke Test Report

## 1. Executive summary

Phase 10F-0 completed a local three-application build and smoke verification without activation or deployment.

Results:

- server tests and clean package: **PASS**
- wallet tests and clean package: **PASS**
- website tests and clean package: **PASS**
- website localhost runtime probe: **PASS**
- server and wallet manual isolated launch instructions: created

No source code or tests were changed. V35 remains disabled.

## 2. Branch and git status

| Check | Result |
| --- | --- |
| Branch | `master` |
| HEAD at start | `f9910c97a88b62d4f8f0b6ab0abf52589a7db96a` |
| Pre-existing untracked local IDE directory | `unitedStates_storage-master/.idea/` |
| Tracked Maven `target/` churn after builds | Restored |

The untracked `.idea/` directory was present before verification and was not modified intentionally.

## 3. Constants audit

| Project | `V35_RESCUE_FORK_INDEX` | `V35_BURN_ADDRESS` |
| --- | --- | --- |
| Server | `Long.MAX_VALUE` | `BURN_ADDRESS` |
| Wallet | `Long.MAX_VALUE` | `BURN_ADDRESS` |

Server and wallet constants match. No activation diff is present.

## 4. Server build and test result

| Command | Result |
| --- | --- |
| `.\mvnw.cmd test` | **PASS**: `225` tests, `0` failures, `0` errors |
| `.\mvnw.cmd clean package` | **PASS**: `BUILD SUCCESS` |

Local package output:

```text
C:\citu\unitedStates_storage-master\target\unitedStates-0.0.423-SNAPSHOT.jar
```

Known non-failing logger noise remains during expected rejected-validation test paths:

```text
\server\resources\error\error.txt
```

## 5. Wallet build and test result

| Command | Result |
| --- | --- |
| `.\mvnw.cmd test` | **PASS**: `200` tests, `0` failures, `0` errors |
| `.\mvnw.cmd clean package` | **PASS**: `BUILD SUCCESS` |

Local package output:

```text
C:\citu\unitedStates_final-master\target\unitedStates-0.32.216-SNAPSHOT.jar
```

## 6. Website build and test result

| Command | Result |
| --- | --- |
| `.\mvnw.cmd test` | **PASS**: `9` tests, `0` failures, `0` errors |
| `.\mvnw.cmd clean package` | **PASS**: `BUILD SUCCESS` |

Local package output:

```text
C:\citu\website.citu-web-master\target\website.citu-0.0.24-SNAPSHOT.jar
```

Website localhost runtime probe:

| Route | Result |
| --- | --- |
| `http://localhost:8080/` | `HTTP 200` |
| `http://localhost:8080/v35` | `HTTP 200` |
| `http://localhost:8080/v35/preactivation-status` | `HTTP 200` |
| `http://localhost:8080/v35/operator-guide` | `HTTP 200` |
| `http://localhost:8080/v35/checksums` | `HTTP 200` |
| `http://localhost:8080/v35/burn-warning` | `HTTP 200` |

The probe wrapper exited, but a child website Java process remained listening on
`127.0.0.1:8080`. That exact process was identified from its command line,
stopped explicitly, and checked again. Final result:

```text
PORT_8080_LISTENER=NONE
```

No deployment occurred.

Known pre-existing Maven warning:

```text
duplicate org.bouncycastle:bcprov-jdk15on:1.70 dependency declaration
```

## 7. Local run commands and URLs

Detailed commands and URLs are recorded in:

```text
CITU_V35_PHASE10F0_LOCAL_THREE_APP_SMOKE_TEST_INSTRUCTIONS.md
```

Recommended local URLs:

| App | Local URL | Purpose |
| --- | --- | --- |
| Server | `http://localhost:8083/v35/protocol` | V35 protocol status |
| Server | `http://localhost:8083/v35/founder/burn-snapshot` | read-only founder snapshot |
| Wallet | `http://localhost:8082/v35-dashboard` | wallet V35 dashboard |
| Website | `http://localhost:8080/v35` | public V35 overview |
| Website | `http://localhost:8080/v35/preactivation-status` | multilingual pre-activation page |

Server and wallet were not launched during this phase because their default configurations use local H2 node-state paths. The instructions provide isolated `target/smoke/` database overrides for a deliberate manual launch without production blockchain data.

## 8. Screenshot checklist

The instructions file includes:

- website overview and four pre-activation routes
- RU / EN / ZH sections
- header and navigation links
- disabled-state and no-burn warnings
- fake burn-address warning
- placeholder height, tag, and checksum text
- server read-only V35 endpoint checks
- wallet V35 dashboard checks

## 9. Confirmation no source activation diff

No source activation diff was introduced. Server and wallet `Seting.java` were not changed.

## 10. Confirmation V35 remains disabled

Confirmed:

```text
V35_RESCUE_FORK_INDEX = Long.MAX_VALUE
V35_BURN_ADDRESS = BURN_ADDRESS
```

Candidate height `514291` remains planning-only.

## 11. Confirmation no activation or release action

Phase 10F-0 performed no burn transaction, fork activation, jackpot payout, deployment, final tag, release artifact packaging, or checksum generation.

The Maven commands created local build outputs only. They were not copied, tagged, checksummed, or published as release artifacts.

## 12. Recommended next phase

Recommended next phase: **Phase 10F-1 - manual local screenshot capture and review using isolated local run commands, no activation and no deployment**.
