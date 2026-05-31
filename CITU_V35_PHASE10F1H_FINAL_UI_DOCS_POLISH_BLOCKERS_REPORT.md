# CITU V35 Phase 10F-1H Final UI / Docs Polish Blockers Report

## Executive summary

Phase 10F-1H completed the final local UI and public-documentation polish pass before sign-off. The website and wallet remain pre-activation only. No consensus code, `Seting.java`, V35 constants, deployment files, release artifacts, checksums, tags, burn transactions, or jackpot payout transactions were changed or created.

## Safety audit

- Branch reviewed: `master`.
- Server `V35_RESCUE_FORK_INDEX = Long.MAX_VALUE`.
- Wallet `V35_RESCUE_FORK_INDEX = Long.MAX_VALUE`.
- Server and wallet `V35_BURN_ADDRESS = BURN_ADDRESS`.
- No activation diff exists in either `Seting.java`.
- Planning candidate displayed publicly: `514291`.
- `BURN_ADDRESS` remains an internal sink placeholder and is not presented as a public address for manual transfers.

## Files changed

Wallet UI:

- `unitedStates_final-master/src/main/resources/templates/home.html`
- `unitedStates_final-master/src/main/resources/static/css/citu-wallet.css`

Website UI and documentation:

- `website.citu-web-master/src/main/resources/templates/main.html`
- `website.citu-web-master/src/main/resources/templates/charter.html`
- `website.citu-web-master/src/main/resources/templates/wallet_and_node_url.html`
- `website.citu-web-master/src/main/resources/templates/conductor.html`
- `website.citu-web-master/src/main/resources/static/css/styles.css`

Report:

- `CITU_V35_PHASE10F1H_FINAL_UI_DOCS_POLISH_BLOCKERS_REPORT.md`

## Wallet root polish

The wallet root `/` now uses the official CITU dark branding and controlled emblem size. Existing wallet actions remain wired to their original routes:

- local transaction form remains `POST /`;
- local blockchain refresh remains `GET /resolving`;
- account balance, connected server, and legacy live-chain indicator panels remain visible;
- legacy send/staking controls are grouped together;
- the private-key field is clearly labeled as a local legacy surface that must not be exposed in production URLs, logs, screenshots, or support messages;
- `/v35-dashboard` is linked from the root status area.

## Website public-page polish

- `/charter` now has a dark CITU-branded selector layout, a clear `CITU Charter` title, Russian and English legacy charter links, and a pre-activation status notice.
- `/` now labels difficulty values as `Legacy / pre-V35 live chain indicators`. It explains that manual difficulty is legacy data and cumulative work is the V35 public mining model.
- `/wallet_and_node_url` now includes a recommended exchange integration sequence, deposit and withdrawal guidance, signed-JSON submission guidance, confirmation checks, safe read-only `curl` examples, and a JSON payload shape.
- `/wallet_and_node_url` continues to warn that `/sendCoin?...&password=...` is local legacy-only and must never be used for exchange integration.
- `/conductor` now groups lookup and address-history forms into readable dark panels and states that live legacy explorer values do not activate V35.
- Long legacy resource URLs on `/` now wrap safely on narrow screens.

## Test and package results

Website:

- `.\mvnw.cmd test`: PASS, `11/11` tests.
- `.\mvnw.cmd clean package`: PASS, `BUILD SUCCESS`.

Wallet:

- `.\mvnw.cmd test`: PASS, `200/200` tests.
- `.\mvnw.cmd clean package`: PASS, `BUILD SUCCESS`.

Generated tracked wallet `target/` build output was restored after verification and is not part of this patch.

## Local visual smoke result

Fresh hidden local smoke processes were started from rebuilt jars. These URLs returned HTTP `200`:

- `http://127.0.0.1:8085/`
- `http://127.0.0.1:8085/charter`
- `http://127.0.0.1:8085/wallet_and_node_url`
- `http://127.0.0.1:8085/conductor`
- `http://127.0.0.1:8082/`
- `http://127.0.0.1:8082/v35-dashboard`

Manual Browser inspection confirmed:

- official CITU emblem and dark header render on website and wallet pages;
- wallet root status cards, balance card, server card, and legacy-operation warning render cleanly;
- wallet dashboard remains pre-activation with `Long.MAX_VALUE`, candidate `514291`, and expected pre-deployment endpoint unavailability;
- charter and explorer dark layouts render cleanly;
- website home narrow-screen width matches viewport width after the resource-link wrap fix.

## Safety confirmation

- No server code changed.
- No server tests were run in this phase.
- No wallet or server `Seting.java` changed.
- V35 remains disabled.
- No burn transaction was created, signed, broadcast, or simulated.
- No fork activation occurred.
- No jackpot payout transaction was created.
- No deployment, final tag, release artifact, or checksum action occurred.

## Remaining risks

- The wallet transaction form intentionally remains a legacy local-only surface and still accepts a private key locally; it must not be exposed as a production exchange integration path.
- Final fork height, final tag, checksums, deployment, and public activation notice still require their separate approved release sequence.
- Local smoke processes are for manual review only and must not be treated as deployment.

## Manual screenshot retest checklist

- Website: `/`, `/charter`, `/wallet_and_node_url`, `/conductor`, `/white_papper`, and `/v35`.
- Wallet: `/` and `/v35-dashboard`.
- Verify official emblem, dark header/footer, candidate `514291`, `V35 NOT ACTIVE YET`, no founder burn claim, fake-burn warning, and absence of horizontal overflow at narrow width.

## Recommended next phase

Proceed to a final manual screenshot sign-off using the local URLs above. Keep activation, deployment, founder burn, jackpot payout, release artifacts, checksums, and tagging in their separately approved later phases.
