# CITU V35 Phase 5F-1 Wallet V35 Dashboard Report

## Scope

Implemented a wallet-side, read-only V35 dashboard page. This phase did not change protocol, mining, validation, balances, transaction format, block format, server protocol logic, or website files.

## Exact Files Changed

Wallet source:
- `unitedStates_final-master/src/main/java/International_Trade_Union/controllers/V35DashboardController.java`
- `unitedStates_final-master/src/main/resources/templates/v35-dashboard.html`
- `unitedStates_final-master/src/main/resources/templates/blocks/header.html`
- `unitedStates_final-master/src/test/java/International_Trade_Union/controllers/V35DashboardControllerTest.java`

Report:
- `CITU_V35_PHASE5F1_WALLET_V35_DASHBOARD_REPORT.md`

## Route / Page Added

Added wallet route:

- `GET /v35-dashboard`

The route renders:

- `v35-dashboard.html`

The page is linked from the existing wallet navigation menu as:

- `V35 dashboard`

## Data Source Model

The dashboard intentionally uses local/static V35 constants and safe unavailable placeholders in this phase. It does not call mutation endpoints and does not fake live supply values.

Static/local data displayed:
- V35 active state from `Seting.V35_RESCUE_FORK_INDEX`
- V35 fork index
- block time: `200` seconds
- blocks/day: `432`
- reward/block: `8.00 GOLD`
- miner reward: `6.00 GOLD`
- jackpot reserve/block: `2.00 GOLD`
- founder reward: `0.00 GOLD`
- annual emission: `1,261,440.00 GOLD/year`
- burn address: `BURN_ADDRESS`
- founder address from `Seting.ADDRESS_FOUNDER`
- jackpot periods, windows, per-block reserves, and canonical payout amounts
- policy text for deterministic delayed jackpot selection
- warning text for disabled V35, modeled burn values, and `BURN_ADDRESS`

Unavailable on the wallet page for this phase:
- total minted
- burned
- circulating
- founder reserve
- jackpot paid
- founder snapshot height
- founder snapshot block hash
- founder snapshot balance
- modeled burn amount tables

No live server endpoint integration was added in Phase 5F-1.

## Actual vs Modeled Supply Display

The page separates actual chain values from modeled burn projections:

- Actual supply fields are shown under an `Actual supply and burn` section.
- Since the wallet dashboard does not yet consume the server V35 supply endpoints, these actual fields render as `Unavailable`.
- Founder burn options `75%`, `80%`, `85%`, and `90%` are shown as modeled projections only.
- The `80%` option is marked recommended.
- Modeled values remain `Unavailable` until a live founder snapshot data source is wired.
- The page explicitly states that modeled burn values are projections and are not actual supply.

## V35 Disabled State

Confirmed:

- `V35_RESCUE_FORK_INDEX = Long.MAX_VALUE`
- V35 renders as inactive on the dashboard.
- The page displays a visible warning that V35 is not active while the fork index is `Long.MAX_VALUE`.

## BURN_ADDRESS Display

The dashboard shows:

- burn address: `BURN_ADDRESS`
- configured: `true`
- placeholder: `false`

The page states that `BURN_ADDRESS` is a protocol sink, sending to it is allowed, and spending from it is blocked after V35 by consensus policy.

## Jackpot Display

The page documents:

- every V35 mined block is a jackpot ticket
- eligible winner is the `minerAddress` of the selected block
- sender addresses are never jackpot participants
- randomness uses deterministic delayed future block hashes
- no last-winner display exists yet because no jackpot status/history endpoint is available

Displayed period table:

| Period | Window blocks | Reserve/block | Canonical payout |
| --- | ---: | ---: | ---: |
| Daily | 432 | 0.70 | 302.40 |
| Weekly | 3024 | 0.40 | 1209.60 |
| Monthly | 12960 | 0.30 | 3888.00 |
| Quarterly | 38880 | 0.25 | 9720.00 |
| Half-year | 78624 | 0.20 | 15724.80 |
| Yearly | 157680 | 0.15 | 23652.00 |

## Tests Added

Added wallet tests in:

- `unitedStates_final-master/src/test/java/International_Trade_Union/controllers/V35DashboardControllerTest.java`

Coverage:
- dashboard route returns `v35-dashboard`
- V35 disabled state renders from `Long.MAX_VALUE`
- `BURN_ADDRESS` appears and is configured
- reward model `8/6/2/0` appears
- block time `200` and blocks/day `432` appear
- live supply fields render as `Unavailable`
- burn options `75/80/85/90` exist and `80` is recommended
- jackpot period policy values render
- template/header contain required safety wording and navigation link

## Confirmations

- V35 remains disabled.
- `V35_RESCUE_FORK_INDEX` was not changed.
- `V35_BURN_ADDRESS` remains `BURN_ADDRESS`.
- No burn transaction was created.
- No protocol behavior changed.
- No mining behavior changed.
- No validation behavior changed.
- No balance behavior changed.
- No transaction or block format changed.
- No server protocol logic was changed.
- No website project files were changed.

## Verification

Wallet:
- `.\mvnw.cmd test` passed: `164` tests, `0` failures, `0` errors, `0` skipped.
- `.\mvnw.cmd clean package` passed: `164` tests, `0` failures, `0` errors, `0` skipped.

Server:
- `.\mvnw.cmd test` passed: `180` tests, `0` failures, `0` errors, `0` skipped.
- `.\mvnw.cmd clean package` passed: `180` tests, `0` failures, `0` errors, `0` skipped.

Notes:
- Maven still reports pre-existing duplicate dependency warnings in `pom.xml`.
- Server tests still print an existing `\server\resources\error\error.txt` file-save stack trace during burn-address validation tests, but Maven reports the test suite and package build as successful.

## Remaining Risks Before Website V35 Pages

- Wallet dashboard does not yet consume live server V35 endpoints, so supply/founder snapshot values remain unavailable.
- No browser/UI screenshot verification was performed in this phase.
- Jackpot last-winner/history display still needs future read-only endpoints.
- Website public V35 overview/tokenomics/burn/jackpot pages are still not implemented.
- Public copy must continue separating actual chain values from modeled burn projections.

## Recommended Next Phase

Phase 5F-2 — website public V35 overview/tokenomics/burn/jackpot pages.
