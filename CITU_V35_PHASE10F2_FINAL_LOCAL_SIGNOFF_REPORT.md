# CITU V35 Phase 10F-2 Final Local Sign-Off Report

## Executive summary

Phase 10F-2 completed the final report-only local UI, documentation, and endpoint sign-off review. Website and wallet routes are locally reachable and the required rendered UI checks pass. The local server `smoke-offline` process was not running during this phase, so its endpoint table is recorded as `WARN / NOT RUNNING` rather than re-launched in this report-only review.

V35 remains disabled. No code, templates, CSS, tests, `Seting.java`, constants, deployment files, release artifacts, checksums, tags, burn transactions, or jackpot payout transactions were changed or created in Phase 10F-2.

Overall sign-off status: **PASS WITH SERVER-SMOKE AVAILABILITY WARNING**.

## Git and constants safety audit

| Check | Result |
| --- | --- |
| Current branch | `master` |
| Workspace status | Existing Phase 10F-1H UI/docs changes remain uncommitted; no activation diff |
| Server `V35_RESCUE_FORK_INDEX` | `Long.MAX_VALUE` |
| Wallet `V35_RESCUE_FORK_INDEX` | `Long.MAX_VALUE` |
| Server `V35_BURN_ADDRESS` | `BURN_ADDRESS` |
| Wallet `V35_BURN_ADDRESS` | `BURN_ADDRESS` |
| `Seting.java` diff | None |
| V35 activation diff | None |

## Local start commands

The website and wallet were already running locally. The server was not running. The safe future local server smoke command is:

```powershell
cd C:\citu\unitedStates_storage-master
java -jar target\unitedStates-0.0.423-SNAPSHOT.jar --server.address=127.0.0.1 --server.port=8083 --spring.profiles.active=smoke-offline "--spring.datasource.url=jdbc:h2:file:./target/smoke/server-db;DB_CLOSE_ON_EXIT=FALSE;AUTO_RECONNECT=TRUE"
```

The reviewed server project contains:

- `unitedStates_storage-master/src/main/resources/application-smoke-offline.properties`
- `unitedStates_storage-master/target/unitedStates-0.0.423-SNAPSHOT.jar`

## Server endpoint status table

The offline server was not started in this report-only phase. Each request failed to connect to `127.0.0.1:8083`.

| Endpoint | Phase 10F-2 status |
| --- | --- |
| `/size` | WARN: local server not running |
| `/v35/protocol` | WARN: local server not running |
| `/v35/supply` | WARN: local server not running |
| `/v35/burn` | WARN: local server not running |
| `/v35/founder` | WARN: local server not running |
| `/v35/founder/burn-snapshot` | WARN: local server not running |
| `/v35/jackpot/status` | WARN: local server not running |
| `/v35/jackpot/periods` | WARN: local server not running |
| `/v35/jackpot/history` | WARN: local server not running |

This is an availability warning for the current local session, not an endpoint implementation failure. The Phase 10F context records prior offline smoke HTTP `200` results.

## Website route status table

| URL | HTTP status |
| --- | --- |
| `http://127.0.0.1:8085/` | `200` |
| `http://127.0.0.1:8085/wallet_and_node_url` | `200` |
| `http://127.0.0.1:8085/conductor` | `200` |
| `http://127.0.0.1:8085/white_papper` | `200` |
| `http://127.0.0.1:8085/charter` | `200` |
| `http://127.0.0.1:8085/v35` | `200` |
| `http://127.0.0.1:8085/v35/preactivation-status` | `200` |
| `http://127.0.0.1:8085/v35/checksums` | `200` |
| `http://127.0.0.1:8085/v35/burn-warning` | `200` |

## Wallet route status table

| URL | HTTP status |
| --- | --- |
| `http://127.0.0.1:8082/` | `200` |
| `http://127.0.0.1:8082/v35-dashboard` | `200` |
| `http://127.0.0.1:8082/mining` | `200` |

## Manual visual sign-off table

| Surface | Result | Notes |
| --- | --- | --- |
| Website `/` branding | PASS | Dark CITU branding visible; official logo and V35 links render |
| Website `/` legacy indicators | PASS | Labels say `Legacy / pre-V35 live chain indicators`; manual difficulty is not described as the V35 public mining model |
| Website `/wallet_and_node_url` | PASS | Exchange warning, key/password warning, local-only `/sendCoin?...password=...`, production-safe `POST /addTransaction`, and read-only V35 endpoint list are present |
| Website `/conductor` | PASS | Dark explorer renders with usable lookup forms and block list |
| Website `/white_papper` | PASS | Shows `200 sec`, `432 blocks/day`, `8 GOLD = 6 miner + 2 jackpot`, founder reward `0`, `cumulativeWork`, candidate `514291`, and inactive status |
| Website `/charter` | PASS | Dark CITU Charter selector renders; page is no longer broken or empty; pre-activation notice is visible |
| Website V35 pages | PASS | Candidate `514291`, inactive status, no founder burn, placeholder checksums, and fake-burn warnings are visible |
| Wallet `/` | PASS | Dark branding, controlled logo size, status cards, grouped legacy controls, private-key warning, and V35 dashboard link render |
| Wallet `/v35-dashboard` | PASS | `V35 active: false`, `Fork index: Long.MAX_VALUE`, candidate `514291`, expected pre-deployment endpoint explanation, founder-burn warning, and jackpot policy render |
| Wallet `/mining` | PASS | Clearly labeled legacy/pre-V35; difficulty `17` is a legacy default; page says manual controls do not define the V35 mining or reward model |

## Items passed

- Git/constants safety audit passed.
- No `Seting.java` change or activation diff exists.
- Website local HTTP checks passed: `9/9`.
- Wallet local HTTP checks passed: `3/3`.
- Required website visual surfaces passed.
- Required wallet visual surfaces passed.
- Planning candidate `514291` is displayed consistently.
- `BURN_ADDRESS` is not promoted as a public address for manual transfers.

## Remaining warnings

1. The server `smoke-offline` process was not running during Phase 10F-2, so current-session server HTTP checks could not be repeated.
2. Existing Phase 10F-1H UI/docs changes remain uncommitted in the working tree. They are not activation changes.
3. Wallet legacy transaction and mining controls intentionally remain local-only surfaces. Private keys must never be exposed through production URLs, logs, screenshots, or support channels.

## Safety confirmation

- No code changed in Phase 10F-2.
- No templates, CSS, or tests changed in Phase 10F-2.
- No `Seting.java` changed.
- V35 remains disabled with `V35_RESCUE_FORK_INDEX = Long.MAX_VALUE`.
- `V35_BURN_ADDRESS` remains `BURN_ADDRESS`.
- No activation, burn transaction, jackpot payout, deployment, final tag, release artifact, or checksum action occurred.

## Recommended next phase

Proceed to **Phase 10G - final activation preparation gate with fresh live `/size`, no burn, no deploy, no tag**.

