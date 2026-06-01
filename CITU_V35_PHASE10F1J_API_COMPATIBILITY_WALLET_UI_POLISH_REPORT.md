# CITU V35 Phase 10F-1J API Compatibility, Wallet Balance Review, and UI Polish Report

## 1. Executive summary

Phase 10F-1J completed a targeted local UI and compatibility pass without activating V35. The public website API reference now documents the legacy server and wallet routes alongside the prepared V35 read-only endpoints. The wallet home, settings, and legacy mining pages received narrow UI-only polish.

Wallet balance rendering was reviewed end to end. The wallet home page reads the configured local miner address and queries the local wallet account database for that exact address. The local smoke instance resolved the configured address from `C:\resources\minerAccount\minerAccount.txt` and rendered nonzero balances. No balance binding bug was found.

## 2. Safety audit

Branch reviewed: `master`.

Constants remain unchanged in both server and wallet:

| Project | Constant | Value |
| --- | --- | --- |
| Server | `V35_RESCUE_FORK_INDEX` | `Long.MAX_VALUE` |
| Wallet | `V35_RESCUE_FORK_INDEX` | `Long.MAX_VALUE` |
| Server | `V35_BURN_ADDRESS` | `BURN_ADDRESS` |
| Wallet | `V35_BURN_ADDRESS` | `BURN_ADDRESS` |

`git diff --name-only -- '*Seting.java'` returned no files. No activation diff exists.

## 3. Files changed in this phase

Website:

- `website.citu-web-master/src/main/resources/templates/wallet_and_node_url.html`
- `website.citu-web-master/src/main/resources/templates/conductor.html`

Wallet:

- `unitedStates_final-master/src/main/resources/templates/home.html`
- `unitedStates_final-master/src/main/resources/templates/seting.html`
- `unitedStates_final-master/src/main/resources/templates/mining.html`

Report:

- `CITU_V35_PHASE10F1J_API_COMPATIBILITY_WALLET_UI_POLISH_REPORT.md`

Pre-existing server V35 read-only endpoint edits from the previous phase were preserved and were not modified as part of this UI pass.

## 4. API compatibility review

Existing server and wallet API mappings were inspected without removing or renaming routes. The public API reference now separates:

- legacy server node, block, transaction, balance, readiness, and pool routes;
- legacy wallet local UI and helper routes;
- prepared V35 read-only server routes;
- compatibility appendix routes that remain available but are not the minimum exchange integration surface.

The documented V35 read-only routes include:

- `GET /v35/protocol`
- `GET /v35/supply`
- `GET /v35/burn`
- `GET /v35/founder`
- `GET /v35/founder/burn-snapshot`
- `GET /v35/jackpot/status`
- `GET /v35/jackpot/periods`
- `GET /v35/jackpot/history`
- `GET /v35/jackpot/tickets/{address}`
- `GET /v35/jackpot/proof/{periodType}/{periodEnd}`

The compatibility quick reference keeps commonly used legacy routes visible, including `GET /size`, `GET /prevBlock`, `POST /statusTransaction58`, `POST /statusTransaction64`, and `POST /addTransaction`.

## 5. Wallet balance review

Wallet home binding:

- `MainController.home()` loads `User.getUserAddress()`.
- `BlockService.findByAccount(address)` queries the local wallet account table.
- When an address is absent from local state, the service returns a zero-valued account rather than failing.
- The home template renders the returned account and now explains that the displayed balance belongs to the configured miner address.

Local smoke evidence:

| Item | Result |
| --- | --- |
| Runtime miner-account file | `C:\resources\minerAccount\minerAccount.txt` |
| Runtime configured address | `rDqx8hhZRzNm6xxvL1GL5aWyYoQRKVdjEHqDo5PY2nbM` |
| Home rendered same address | Yes |
| Home rendered nonzero CITU balance | Yes |
| Home rendered staking balance | Yes |
| Template binding defect found | No |

The source-resource default miner file remains separate from the runtime external local file. A zero balance can legitimately mean that the configured address is absent from the local H2 account state. The settings page now makes the balance source clearer.

## 6. UI polish

Website:

- Expanded `/wallet_and_node_url` into a compatibility-oriented API reference.
- Kept the V35 pre-activation warning and candidate `514291`.
- Clarified that `BURN_ADDRESS` is an internal placeholder and not a public address.
- Removed the explorer table-header white override.
- Labeled displayed explorer difficulty as legacy / pre-V35 difficulty.

Wallet:

- Added a configured-address balance-source hint on `/`.
- Reworked `/seting` into a clearer local configuration page.
- Reworked `/mining` into clearer legacy / pre-V35 panels.
- Preserved all existing form actions and backend behavior.

## 7. Test and package results

| Project | Command | Result |
| --- | --- | --- |
| Website | `.\mvnw.cmd test` | PASS, 11/11 tests |
| Website | `.\mvnw.cmd clean package` | PASS, BUILD SUCCESS |
| Wallet | `.\mvnw.cmd test` | PASS, 200/200 tests |
| Wallet | `.\mvnw.cmd clean package` | PASS, BUILD SUCCESS |

No server build was required for this UI-only phase.

## 8. Local smoke results

Updated local website and wallet jars were started temporarily for route and visual smoke checks, then stopped. The existing local server on `127.0.0.1:8083` was queried read-only.

Website routes returning HTTP `200`:

- `http://127.0.0.1:8085/`
- `http://127.0.0.1:8085/wallet_and_node_url`
- `http://127.0.0.1:8085/conductor`
- `http://127.0.0.1:8085/white_papper`
- `http://127.0.0.1:8085/charter`
- `http://127.0.0.1:8085/v35`
- `http://127.0.0.1:8085/v35/preactivation-status`
- `http://127.0.0.1:8085/v35/checksums`
- `http://127.0.0.1:8085/v35/burn-warning`

Wallet routes returning HTTP `200`:

- `http://127.0.0.1:8082/`
- `http://127.0.0.1:8082/seting`
- `http://127.0.0.1:8082/mining`
- `http://127.0.0.1:8082/v35-dashboard`

Server read-only endpoint results:

| Endpoint | Result |
| --- | --- |
| `GET /size` | HTTP `200`, local smoke size `0` |
| `GET /v35/protocol` | HTTP `200`, `v35Active=false`, `v35ForkIndex=Long.MAX_VALUE` |
| `GET /v35/burn` | HTTP `200`, placeholder `true`, configured `false`, burn transaction created `false` |

Visual checks were performed in the in-app browser on the API reference, wallet settings, and wallet legacy mining pages, including a narrow viewport pass.

## 9. Public wording safety

Confirmed:

- public V35 pages use planning candidate `514291`, not superseded `513332`;
- V35 pages state that V35 is not active yet;
- founder burn is not claimed as executed;
- official public burn address is not claimed as published;
- `BURN_ADDRESS` is not presented as a user-facing address;
- dry-run artifacts are not presented as final release artifacts.

## 10. Actions not performed

No `Seting.java` file was changed. No consensus rule, reward, PoW, transaction format, or block format was changed. No V35 activation, founder burn transaction, jackpot payout, deployment, final tag, release artifact, checksum, or commit was created.

## 11. Remaining risks

- The public API reference is documentation, not a substitute for an external exchange integration test.
- Wallet balances depend on the runtime local account state and configured miner-account file.
- The local server smoke database is intentionally isolated and empty; it is suitable for endpoint shape checks, not live chain validation.
- Final activation still requires a fresh `/size` review, explicit approval, final build/checksum/tag flow, and operator communication.

## 12. Recommended next phase

Proceed to **Phase 10F-2B: final local sign-off warning closure report**, report only. Keep V35 disabled and preserve the separation between activation and founder burn execution.
