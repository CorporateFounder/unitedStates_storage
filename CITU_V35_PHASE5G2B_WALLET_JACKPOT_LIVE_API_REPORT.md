# CITU V35 Phase 5G-2B Wallet Jackpot Live API Report

## Scope

Phase 5G-2B implemented wallet-only live read-only jackpot API integration for the existing `/v35-dashboard` page.

No protocol, mining, validation, balance, server API, website, transaction format, or block format behavior was changed.

## Exact Files Changed

- `unitedStates_final-master/src/main/java/International_Trade_Union/controllers/V35DashboardApiClient.java`
- `unitedStates_final-master/src/main/java/International_Trade_Union/controllers/V35DashboardController.java`
- `unitedStates_final-master/src/main/resources/templates/v35-dashboard.html`
- `unitedStates_final-master/src/test/java/International_Trade_Union/controllers/V35DashboardApiClientTest.java`
- `unitedStates_final-master/src/test/java/International_Trade_Union/controllers/V35DashboardControllerTest.java`
- `CITU_V35_PHASE5G2B_WALLET_JACKPOT_LIVE_API_REPORT.md`

## Jackpot Endpoints Consumed

The wallet dashboard API client now attempts these read-only jackpot endpoints in addition to the existing V35 supply/protocol/founder endpoints:

- `/v35/jackpot/status`
- `/v35/jackpot/periods`
- `/v35/jackpot/history`

The dashboard does not call:

- `/v35/jackpot/tickets/{address}`
- `/v35/jackpot/proof/{periodType}/{periodEnd}`

Proof and ticket lookup remain on-demand future UI work.

## Live vs Static Data

Static jackpot policy remains available even when live jackpot endpoints are unavailable:

- Daily payout: `302.40 GOLD`
- Weekly payout: `1209.60 GOLD`
- Monthly payout: `3888.00 GOLD`
- Quarterly payout: `9720.00 GOLD`
- Half-year payout: `15724.80 GOLD`
- Yearly payout: `23652.00 GOLD`
- Future hash delay: `60` blocks
- Winner source: `minerAddress`
- Sender addresses are never jackpot participants

Live jackpot data is shown only when the corresponding endpoint response is available:

- Live status availability
- Live period status rows
- Current height
- Period start/end and maturity block
- Period mature/status fields
- Estimated ticket counts when reported
- Paid jackpot history rows from `/v35/jackpot/history`

No winners or paid history are fabricated. If history is live but empty, the dashboard shows:

- `No paid V35 jackpot history yet`

## Fallback / Unavailable Behavior

Endpoint failures, malformed JSON, timeouts, or old nodes without `/v35/jackpot/*` do not crash the wallet dashboard.

Fallback behavior:

- Static jackpot policy remains visible.
- Live jackpot endpoint availability is shown as `false`.
- Live period status shows `Live jackpot data unavailable`.
- Jackpot history shows `Unavailable` until a live history response is available.
- No fake winners or fake history are displayed.

## Jackpot Status / History Display

The wallet dashboard now has:

- A live jackpot status section.
- A live period status table.
- The canonical payout table.
- A jackpot history section.
- A ticket eligibility note.
- A proof-on-demand note.

The page clearly states:

- Only `minerAddress` can win.
- Sender addresses are never jackpot participants.
- Jackpot proof is available on demand via `/v35/jackpot/proof/{periodType}/{periodEnd}`.
- The dashboard does not fetch proof by default.

## V35 Disabled Confirmation

Confirmed before implementation:

- Wallet `Seting.V35_RESCUE_FORK_INDEX = Long.MAX_VALUE`
- Server `Seting.V35_RESCUE_FORK_INDEX = Long.MAX_VALUE`
- Wallet `Seting.V35_BURN_ADDRESS = "BURN_ADDRESS"`
- Server `Seting.V35_BURN_ADDRESS = "BURN_ADDRESS"`

V35 remains disabled.

## No Behavior Changes

This phase did not change:

- Mining behavior
- Validation behavior
- Balance behavior
- Protocol behavior
- Server API semantics
- Website behavior
- Transaction format
- Block format

No burn transaction was created.

No jackpot payout transaction was created.

## Tests Added / Updated

Updated wallet tests verify:

- The wallet API client calls only read-only V35 endpoints, now including the three jackpot endpoints.
- Malformed jackpot JSON is handled safely.
- Dashboard route still renders while V35 is disabled.
- Dashboard fallback shows live jackpot data unavailable.
- Static jackpot payout table remains visible.
- Live jackpot status, periods, and history are overlaid when provided.
- Empty live jackpot history shows `No paid V35 jackpot history yet`.
- Dashboard wording says only `minerAddress` can win.
- Dashboard wording says sender addresses are never jackpot participants.
- Dashboard references proof on demand without fetching proof by default.
- No mutation endpoints are called.

## Verification Results

Wallet:

- `.\mvnw.cmd test` passed
  - Tests run: `169`
  - Failures: `0`
  - Errors: `0`
- `.\mvnw.cmd clean package` passed
  - Tests run: `169`
  - Failures: `0`
  - Errors: `0`

Server:

- `.\mvnw.cmd test` passed
  - Tests run: `184`
  - Failures: `0`
  - Errors: `0`
- `.\mvnw.cmd clean package` passed
  - Tests run: `184`
  - Failures: `0`
  - Errors: `0`

Note: server verification still prints the pre-existing `\server\resources\error\error.txt` `FileNotFoundException` stack trace during burn-policy tests, but Maven reports build success with zero test failures/errors.

Website:

- `.\mvnw.cmd test` passed
  - Tests run: `4`
  - Failures: `0`
  - Errors: `0`
- `.\mvnw.cmd clean package` passed
  - Tests run: `4`
  - Failures: `0`
  - Errors: `0`

## Remaining Risks

- The wallet dashboard now consumes jackpot status/period/history endpoints, but ticket lookup and proof drill-down remain future UI work.
- Live jackpot display depends on the configured wallet server URL and the server exposing Phase 5G-1 endpoints.
- Full browser-level visual testing was not performed in this phase; coverage is controller/client/template-level.
- Server verification still has noisy pre-existing error-file logging during tests.

## Recommended Next Phase

Phase 6A - pre-activation hardening plan: live controller/database, old-chain validation, V35 dry-run.
