# CITU V35 Phase 11D-1 Full Wallet and Website Legacy UI Cleanup Report

## Executive summary

Phase 11D-1 continued from the existing dirty working tree after the stream disconnect and completed the wallet/website legacy UI cleanup review. The wallet UI and public website guides now use the current CITU V35 release context, official 1.0.0 artifact names, dark CITU styling, safer account/key wording, clearer node/pool instructions, and V35/burn warnings.

No deployment, push, commit, tag, burn transaction, jackpot payout, release upload, or official server restart was performed.

## Branch and working tree status

Latest known HEAD before this phase work:

- `80d441d phase 11C upload final V35 GitHub releases`

Final `git status --short` showed source/template/test/report changes plus generated wallet `target/` output from local package verification. The generated `target/` files were not cleaned or staged in this phase.

## Files changed

Wallet source/UI files:

- `unitedStates_final-master/src/main/java/International_Trade_Union/controllers/LawsController.java`
- `unitedStates_final-master/src/main/java/International_Trade_Union/controllers/MainController.java`
- `unitedStates_final-master/src/main/java/International_Trade_Union/controllers/StatisticsController.java`
- `unitedStates_final-master/src/main/resources/static/css/citu-wallet.css`
- `unitedStates_final-master/src/main/resources/templates/about.html`
- `unitedStates_final-master/src/main/resources/templates/blocks/header.html`
- `unitedStates_final-master/src/main/resources/templates/board-of-shareholders.html`
- `unitedStates_final-master/src/main/resources/templates/charter.html`
- `unitedStates_final-master/src/main/resources/templates/create-account.html`
- `unitedStates_final-master/src/main/resources/templates/current-laws.html`
- `unitedStates_final-master/src/main/resources/templates/mining.html`
- `unitedStates_final-master/src/main/resources/templates/seting.html`
- `unitedStates_final-master/src/main/resources/templates/statistics.html`
- `unitedStates_final-master/src/main/resources/templates/transactions.html`
- `unitedStates_final-master/src/main/resources/templates/voting.html`
- `unitedStates_final-master/src/test/java/International_Trade_Union/controllers/WalletPublicUiTemplateTest.java`

Website source/UI files:

- `website.citu-web-master/src/main/java/com/example/website/citu/controller/WebController.java`
- `website.citu-web-master/src/main/resources/templates/blocks/footer.html`
- `website.citu-web-master/src/main/resources/templates/blocks/header.html`
- `website.citu-web-master/src/main/resources/templates/blocks/v35-release-links.html`
- `website.citu-web-master/src/main/resources/templates/charter.html`
- `website.citu-web-master/src/main/resources/templates/how_mining_pool.html`
- `website.citu-web-master/src/main/resources/templates/how_to_install.html`
- `website.citu-web-master/src/main/resources/templates/how_to_install_server.html`
- `website.citu-web-master/src/main/resources/templates/how_to_mining.html`
- `website.citu-web-master/src/main/resources/templates/how_to_open_an_account.html`
- `website.citu-web-master/src/main/resources/templates/how_to_transaction.html`
- `website.citu-web-master/src/main/resources/templates/main.html`
- `website.citu-web-master/src/main/resources/templates/v35-burn-warning.html`
- `website.citu-web-master/src/main/resources/templates/v35-checksums.html`
- `website.citu-web-master/src/main/resources/templates/v35-jackpot.html`
- `website.citu-web-master/src/main/resources/templates/v35-operator-guide.html`
- `website.citu-web-master/src/main/resources/templates/v35-preactivation-status.html`
- `website.citu-web-master/src/main/resources/templates/v35.html`
- `website.citu-web-master/src/main/resources/templates/wallet_and_node_url.html`
- `website.citu-web-master/src/main/resources/templates/white_papper.html`
- `website.citu-web-master/src/test/java/com/example/website/citu/controller/V35PublicControllerTest.java`

Reports:

- `CITU_V35_PHASE11D_WEBSITE_REAL_RELEASE_LINKS_REPORT.md`
- `CITU_V35_PHASE11D1_FULL_WALLET_WEBSITE_LEGACY_UI_CLEANUP_REPORT.md`

Generated build output:

- `unitedStates_final-master/target/...`

These `target/` files were created/updated by local wallet verification and should not be staged for a source commit.

## Wallet cleanup summary

Wallet templates were updated toward a modern dark CITU UI with safer wording and clearer current-release context:

- Account creation page now separates public address/login/public key from private key/password and warns users not to share secrets.
- Statistics, governance, transactions, voting, about, charter, mining, and settings pages were restyled with dark CITU panels.
- Header links now expose cleaner `/settings` and `/vote` aliases while preserving legacy routes.
- Settings/mining pages now explain:
  - official public node: `http://194.87.236.238:82`
  - local node: `http://127.0.0.1:8083`
  - local wallet: `http://127.0.0.1:8082`
  - pool/mining fields are legacy/pre-V35 support surfaces, not new V35 reward features.
- Statistics and current-laws routes now fail softly on empty local smoke state instead of returning HTTP 500.

## Website cleanup summary

Website guides and release pages were updated with current V35 1.0.0 release links and user-safe instructions:

- The shared V35 release links block provides official GitHub release/download buttons.
- Installation, server, account, transaction, mining, pool, V35, checksum, burn, jackpot, and white paper pages now reference the V35 release context.
- Old or confusing instructions were replaced with current local run commands and verification warnings.
- A new `/how_mining_pool` page documents mining/pool status as legacy/pre-V35 support and avoids presenting unsupported pool behavior as a V35 feature.

## Tests and package results

Observed before disconnect and after continuation:

- Wallet: `.\mvnw.cmd test` passed, `203/203` tests.
- Wallet: `.\mvnw.cmd clean package` passed.
- Website: `.\mvnw.cmd test` passed, `13/13` tests.
- Website: `.\mvnw.cmd clean package` passed.

Notes:

- Wallet Maven emitted existing duplicate dependency warnings.
- Website Maven emitted an existing duplicate dependency warning.
- These warnings did not block tests or packaging.

## Local smoke results

Temporary local wallet instance:

- URL base: `http://127.0.0.1:8087`
- Result: all checked routes returned HTTP 200.

Wallet routes checked:

- `/`
- `/create-account`
- `/board-of-shareholders`
- `/transactions`
- `/statistics`
- `/current-laws`
- `/voting`
- `/vote`
- `/settings`
- `/mining`
- `/v35-dashboard`

Temporary local website instance:

- URL base: `http://127.0.0.1:8085`
- Result: all checked routes returned HTTP 200.

Website routes checked:

- `/`
- `/v35`
- `/wallet_and_node_url`
- `/how_to_install`
- `/how_to_install_server`
- `/how_to_open_an_account`
- `/how_to_transaction`
- `/how_to_mining`
- `/how_mining_pool`
- `/v35/checksums`
- `/white_papper`

Temporary smoke processes on ports `8085` and `8087` were stopped after verification.

## Stale wording search

Searched current wallet and website templates for:

- `unitedStates-0.0.1-SNAPSHOT.jar`
- `0.0.423`
- `0.32.216`
- `Long.MAX_VALUE`
- `planning-only`

Result:

- No matches were found in the checked current wallet/website template files.

## Constants and Seting.java safety

Confirmed current constants:

- Server `V35_RESCUE_FORK_INDEX = 514291`
- Server `V35_BURN_ADDRESS = "BURN_ADDRESS"`
- Wallet `V35_RESCUE_FORK_INDEX = 514291`
- Wallet `V35_BURN_ADDRESS = "BURN_ADDRESS"`

Safety confirmation:

- No `Seting.java` source file is changed in the current diff.
- `V35_RESCUE_FORK_INDEX` was not changed in this phase.
- `V35_BURN_ADDRESS` was not changed in this phase.

## No prohibited actions

Confirmed:

- No deployment.
- No official server restart.
- No source push.
- No commit.
- No tag.
- No release upload.
- No checksum generation/publication.
- No burn transaction.
- No jackpot payout.
- No consensus or mining consensus change.

## Remaining review notes

- Generated `unitedStates_final-master/target/` files are dirty from local package verification and should be excluded from any future source commit.
- Manual browser review should focus on visual polish and text clarity for:
  - website `/`, `/v35`, `/wallet_and_node_url`, `/how_to_install`, `/how_to_install_server`, `/how_to_open_an_account`, `/how_to_transaction`, `/how_to_mining`, `/how_mining_pool`, `/v35/checksums`, `/white_papper`
  - wallet `/create-account`, `/statistics`, `/current-laws`, `/settings`, `/mining`, `/v35-dashboard`

## Recommended next phase

Proceed to manual review and then create a careful source-only commit after excluding generated `target/` output. Do not deploy until the website/wallet UI cleanup is reviewed and approved.
