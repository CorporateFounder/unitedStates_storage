# CITU V35 Phase 5F-3B Wallet Dashboard Live API Report

Phase: 5F-3B only  
Scope: wallet read-only dashboard live API integration  
V35 activation: still disabled  
Burn transaction: not created  
Protocol/mining/validation/balance behavior changes: none

## 1. Exact Files Changed

Wallet source:

- `unitedStates_final-master/src/main/java/International_Trade_Union/controllers/V35DashboardApiClient.java`
- `unitedStates_final-master/src/main/java/International_Trade_Union/controllers/V35DashboardController.java`
- `unitedStates_final-master/src/main/resources/templates/v35-dashboard.html`

Wallet tests:

- `unitedStates_final-master/src/test/java/International_Trade_Union/controllers/V35DashboardApiClientTest.java`
- `unitedStates_final-master/src/test/java/International_Trade_Union/controllers/V35DashboardControllerTest.java`

Report:

- `CITU_V35_PHASE5F3B_WALLET_DASHBOARD_LIVE_API_REPORT.md`

No server protocol files, website files, mining files, validation files, balance files, transaction format files, block format files, or `pom.xml` files were changed.

## 2. Live API Client / Helper Added

Added wallet-local read-only helper:

- `V35DashboardApiClient`

Purpose:

- resolve the configured server URL;
- call only read-only V35 endpoints;
- parse endpoint responses into tolerant `Map<String, Object>` data;
- return empty/unavailable results instead of throwing when endpoints fail, return malformed JSON, time out, or are missing.

Endpoints fetched:

- `/v35/protocol`
- `/v35/supply`
- `/v35/burn`
- `/v35/founder`
- `/v35/founder/burn-snapshot`

Timeout:

- 1500 ms per endpoint.

The helper exposes injectable fetch/server-url interfaces for deterministic wallet tests. No mutation endpoints are called.

## 3. Server URL Selection

`V35DashboardApiClient` selects the server URL in this order:

1. Read `Seting.YOUR_SERVER` via `UtilsFileSaveRead.read(Seting.YOUR_SERVER)`.
2. If that file contains a nonblank value, use it.
3. Otherwise, use the first nonblank value from `Seting.ORIGINAL_ADDRESSES`.
4. Normalize trailing slashes before appending `/v35/...`.

If no URL is available, the dashboard remains available and reports live V35 data as unavailable.

## 4. Endpoint Failure Behavior

Endpoint failures do not crash `/v35-dashboard`.

Handled safely:

- missing server URL;
- old server without `/v35/*`;
- connection failure;
- timeout;
- empty response;
- malformed JSON;
- missing fields;
- wrong field types.

Fallback behavior:

- static V35 protocol policy remains visible;
- actual supply/founder/snapshot fields remain `Unavailable`;
- the dashboard shows a live data unavailable warning;
- `BURN_ADDRESS` and V35 disabled warnings remain visible.

## 5. Fields Live Vs Static Vs Unavailable

Static baseline fields always come from wallet constants/helpers:

- V35 disabled status from `Seting.V35_RESCUE_FORK_INDEX`;
- fork index;
- block time `200`;
- blocks/day `432`;
- reward model `8/6/2/0`;
- annual emission `1,261,440.00`;
- burn address from `BurnAddressPolicy`;
- jackpot period/payout table from `JackpotCalculator`;
- warnings and safety text.

Live fields when endpoints are available:

- `/v35/protocol`: protocol status, fork index, reward constants, burn/founder address flags.
- `/v35/supply`: `totalMintedGold`, `burnedGold`, `circulatingGold`, `founderReserveGold`, `jackpotPaidGold`, `annualEmissionGold`, validity/source metadata.
- `/v35/burn`: burn address flags, actual burned amount if supply is unavailable, burn transaction created flag.
- `/v35/founder`: founder address/reserve if supply is unavailable.
- `/v35/founder/burn-snapshot`: snapshot height/hash, founder balance, modeled 75/80/85/90 burn options, proof checklist, snapshot note.

Unavailable fields remain explicitly labeled `Unavailable` when the live endpoint data cannot be read safely.

## 6. Actual Vs Modeled Burn Separation

Actual values are displayed only from actual live supply/burn/founder endpoint fields:

- `totalMintedGold`
- `burnedGold`
- `circulatingGold`
- `founderReserveGold`
- `jackpotPaidGold`

Modeled burn values are displayed only in the founder burn snapshot options:

- `burnGold`
- `founderReserveAfterBurnGold`
- `modeledBurnedGoldAfterBurn`
- `modeledCirculatingGoldAfterBurn`
- `modeledInflationAfterBurnPercent`

Modeled values never overwrite actual burned or circulating supply. The template keeps the modeled snapshot section labeled as projections and keeps actual supply fields separate.

## 7. V35 / Burn Safety Confirmations

- `V35_RESCUE_FORK_INDEX` remains `Long.MAX_VALUE`.
- `V35_BURN_ADDRESS` remains `BURN_ADDRESS`.
- V35 remains disabled.
- No burn transaction was created.
- No protocol behavior changed.
- No mining behavior changed.
- No validation behavior changed.
- No balance behavior changed.
- No transaction or block format changed.
- No server API semantics changed.
- No website project files were edited.

## 8. Tests Added / Updated

Added:

- `V35DashboardApiClientTest`
  - confirms only read-only `/v35/*` endpoints are called;
  - confirms no mutation-style endpoints are called;
  - confirms malformed JSON does not crash the client;
  - confirms server URL normalization.

Updated:

- `V35DashboardControllerTest`
  - uses injected dashboard client data;
  - verifies fallback/unavailable rendering;
  - verifies static 200 sec / 432 blocks/day / 8-6-2-0 model;
  - verifies BURN_ADDRESS and V35 disabled status;
  - verifies live endpoint overlay behavior;
  - verifies modeled burn options do not overwrite actual burned/circulating supply;
  - verifies template live-data and safety wording.

## 9. Test / Build Results

Wallet:

- `.\mvnw.cmd test` passed.
  - Tests run: 169
  - Failures: 0
  - Errors: 0
- `.\mvnw.cmd clean package` passed.
  - Tests run: 169
  - Failures: 0
  - Errors: 0

Server:

- `.\mvnw.cmd test` passed.
  - Tests run: 180
  - Failures: 0
  - Errors: 0
- `.\mvnw.cmd clean package` passed.
  - Tests run: 180
  - Failures: 0
  - Errors: 0

Website:

- Initial command used the wrong directory name (`C:\citu\website`) and failed before running Maven.
- Re-run in `C:\citu\website.citu-web-master`: `.\mvnw.cmd test` passed.
  - Tests run: 4
  - Failures: 0
  - Errors: 0
- Re-run in `C:\citu\website.citu-web-master`: `.\mvnw.cmd clean package` passed.
  - Tests run: 4
  - Failures: 0
  - Errors: 0

Notes:

- Maven still reports pre-existing duplicate dependency warnings in wallet/server/website POMs.
- Server tests/build print pre-existing `FileNotFoundException` stack traces for `\server\resources\error\error.txt`, but Maven reports success with zero test failures/errors.

## 10. Remaining Risks Before Jackpot Read-Only API / Hardening

1. The wallet dashboard trusts server-reported read-only values; a malicious or stale configured server can display misleading supply data.
2. Dashboard requests currently fetch five endpoints per page render. The 1500 ms timeout limits hangs, but a slow server can still delay rendering.
3. No cache is used yet; repeated refreshes may repeatedly call the server endpoints.
4. Live jackpot winner/status/history is still not available because no jackpot read-only API exists yet.
5. The dashboard is server-data driven; it does not independently prove supply values from local wallet chain/account data.

## 11. Recommended Next Phase

Phase 5G-0 - jackpot read-only API/status/history plan.
