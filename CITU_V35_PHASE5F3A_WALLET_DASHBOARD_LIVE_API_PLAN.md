# CITU V35 Phase 5F-3A Wallet Dashboard Live API Integration Plan

Phase: 5F-3A only  
Scope: planning/report only  
Source changes: none  
V35 activation: still disabled  
Burn transaction: not created  
Protocol/mining/validation/balance behavior changes: none

## 1. Exact Files / Methods Inspected

Reports read:

- `CITU_V35_PHASE5F1_WALLET_V35_DASHBOARD_REPORT.md`
- `CITU_V35_PHASE5F2_WEBSITE_V35_PUBLIC_PAGES_REPORT.md`
- `CITU_V35_PHASE5C2C_SUPPLY_API_ENDPOINTS_REPORT.md`
- `CITU_V35_PHASE5E1_FOUNDER_BURN_SNAPSHOT_REPORT.md`

Wallet inspected:

- `unitedStates_final-master/src/main/java/International_Trade_Union/controllers/V35DashboardController.java`
  - `dashboard(Model model)`
  - `isV35Active()`
  - `forkIndexValue()`
  - `burnOptions()`
  - `jackpotPeriods()`
  - `proofChecklist()`
  - `warnings()`
- `unitedStates_final-master/src/main/resources/templates/v35-dashboard.html`
- `unitedStates_final-master/src/main/resources/templates/blocks/header.html`
- `unitedStates_final-master/src/main/java/International_Trade_Union/controllers/MainController.java`
  - static initialization server-setting load
  - `home(Model model)`
  - `seting(Model model)`
- `unitedStates_final-master/src/main/resources/templates/seting.html`
- `unitedStates_final-master/src/main/resources/templates/home.html`
- `unitedStates_final-master/src/main/java/International_Trade_Union/utils/UtilUrl.java`
  - `readJsonFromUrl(String url)`
  - `readJsonFromUrl(String url, int timeoutMillis)`
  - `readJsonFromUrl_silent(String url)`
  - `getObject(String jsonObject, String requestStr)`
  - `sendPost(...)`
- `unitedStates_final-master/src/main/java/International_Trade_Union/utils/UtilsJson.java`
  - current typed JSON helpers
- `unitedStates_final-master/src/main/java/International_Trade_Union/setings/Seting.java`
  - `YOUR_SERVER`
  - `ORIGINAL_ADDRESSES`
  - `V35_RESCUE_FORK_INDEX`
  - `V35_BURN_ADDRESS`
  - V35 reward / timing constants

Server inspected for response shape only:

- `unitedStates_storage-master/src/main/java/International_Trade_Union/controllers/V35SupplyController.java`
  - `supply()`
  - `protocol()`
  - `burn()`
  - `founder()`
  - `founderBurnSnapshot()`
  - `supplyContext()`
- `unitedStates_storage-master/src/main/java/International_Trade_Union/protocol/v35/V35SupplyApiResponseBuilder.java`
  - `supply(...)`
  - `protocol(...)`
  - `burn(...)`
  - `founder(...)`
  - `founderBurnSnapshot(...)`

## 2. V35 Status Confirmation

Confirmed in wallet and server:

```text
V35_RESCUE_FORK_INDEX = Long.MAX_VALUE
V35_BURN_ADDRESS = BURN_ADDRESS
```

V35 remains disabled. No burn transaction exists.

## 3. Current Wallet Dashboard Data Flow

Current `GET /v35-dashboard` flow:

1. `V35DashboardController.dashboard(...)` builds all model data locally.
2. Protocol/reward fields come from wallet constants:
   - `Seting.V35_TARGET_BLOCK_TIME_SECONDS`
   - `Seting.V35_GOLD_REWARD`
   - `Seting.V35_INSTANT_MINER_REWARD`
   - `Seting.V35_JACKPOT_REWARD`
   - `Seting.V35_EXPECTED_FOUNDER_REWARD`
   - `Seting.V35_RESCUE_FORK_INDEX`
3. Burn fields come from local policy:
   - `BurnAddressPolicy.getBurnAddress()`
   - `BurnAddressPolicy.isPlaceholderBurnAddress()`
4. Jackpot period rows come from `JackpotCalculator`.
5. Supply/founder snapshot fields are hardcoded to `Unavailable`.
6. `liveSupplyAvailable = false`.
7. Burn options are present but all modeled amounts are `Unavailable`.

Current behavior is safe and static, but cannot show actual live supply/burn/founder snapshot data.

## 4. Existing Wallet Server / API Call Patterns

The wallet already performs remote read calls from controllers and resolving helpers.

Server URL source:

- `Seting.YOUR_SERVER = /resources/server/server.txt`
- `MainController` reads it with `UtilsFileSaveRead.read(Seting.YOUR_SERVER)`.
- If nonblank, it clears and replaces `Seting.ORIGINAL_ADDRESSES`.
- Otherwise, `Seting.ORIGINAL_ADDRESSES` defaults to configured public server(s).
- Settings page `/seting` displays and allows editing the saved server.

Existing GET pattern:

```java
UtilUrl.readJsonFromUrl(address + "/size")
UtilUrl.readJsonFromUrl(address + "/version")
UtilUrl.readJsonFromUrl(address + "/difficultyBlockchain")
```

Existing failure behavior:

- catch exception,
- log a simple message,
- set safe fallback such as `-1`,
- continue rendering.

Existing timeout behavior:

- `UtilUrl.readJsonFromUrl(String url)` uses 5 second connect/read timeouts.
- `UtilUrl.readJsonFromUrl(String url, int timeoutMillis)` can be used for a shorter dashboard-specific read timeout.

Existing JSON helpers:

- `UtilsJson` has typed readers for domain classes.
- It does not currently expose a generic `Map<String, Object>` reader.
- Jackson `ObjectMapper` is already available in the project.

## 5. Recommended Live API Integration Model

Recommended model for Phase 5F-3B:

1. Keep `V35DashboardController` read-only.
2. Keep local/static constants as the first-render baseline.
3. Add a small wallet-only read-only helper/client, for example:

```text
International_Trade_Union.controllers.v35.V35DashboardApiClient
```

or, if package style should stay flat:

```text
International_Trade_Union.controllers.V35DashboardApiClient
```

4. The helper resolves the preferred server URL from:
   - `UtilsFileSaveRead.read(Seting.YOUR_SERVER)`
   - fallback to first usable value from `Seting.ORIGINAL_ADDRESSES`
5. The helper fetches only read-only endpoints:
   - `/v35/protocol`
   - `/v35/supply`
   - `/v35/burn`
   - `/v35/founder`
   - `/v35/founder/burn-snapshot`
6. Use a short timeout, preferably `1000-2000 ms` per endpoint, to avoid making the wallet dashboard feel stuck.
7. Merge live data into the dashboard model only after successful parsing and minimal sanity checks.
8. If any endpoint fails, leave the affected section as `Unavailable`, keep static policy values, and show a visible unavailable note.

Recommended fetch priority:

1. `/v35/protocol`
   - protocol status and canonical constants.
   - If it fails, keep local constants.
2. `/v35/supply`
   - actual supply fields.
   - If it fails, keep actual supply fields as `Unavailable`.
3. `/v35/burn`
   - burn address details and actual burned supply note.
   - If it fails, keep local burn policy fields.
4. `/v35/founder`
   - founder reserve/model summary.
   - If it fails, do not overwrite founder fields.
5. `/v35/founder/burn-snapshot`
   - snapshot height/hash, founder balance, burn options, proof checklist.
   - If it fails, keep burn options present with modeled values `Unavailable`.

The dashboard should support both:

- local static policy display,
- live read-only server data where available.

It should not use local wallet chain/account state for supply in Phase 5F-3B because the server endpoints already centralize the canonical supply and founder snapshot logic.

## 6. Endpoint Failure / Unavailable Behavior

Failure must not crash `/v35-dashboard`.

On endpoint failure:

- render page normally,
- keep `liveSupplyAvailable = false` if `/v35/supply` fails,
- show `Unavailable` for actual supply fields,
- show `Unavailable` for snapshot height/hash/founder balance if snapshot fails,
- keep static policy values from wallet constants,
- display the server URL attempted if useful,
- display a short read-only warning such as:

```text
Live V35 server endpoint data is unavailable. Static protocol policy is shown.
```

Failure cases to handle:

- empty saved server URL,
- malformed saved server URL,
- connection refused,
- timeout,
- HTTP error,
- non-JSON response,
- missing fields,
- old server without `/v35/*` endpoints,
- incompatible field type such as number vs string,
- endpoint returning `valid = false` on `/v35/supply`.

No retry loop should run inside the page render. One attempt per endpoint is enough.

## 7. Response Fields To Map

`/v35/protocol`:

- `v35Active`
- `v35ForkIndex`
- `blockTimeSeconds`
- `blocksPerDay`
- `rewardPerBlockGold`
- `minerRewardGold`
- `jackpotReservePerBlockGold`
- `founderRewardGold`
- `burnAddress`
- `founderAddress`
- `isBurnAddressPlaceholder`
- `burnAddressConfigured`
- `annualEmissionGold`

`/v35/supply`:

- all protocol fields above when present
- `totalMintedGold`
- `burnedGold`
- `circulatingGold`
- `founderReserveGold`
- `jackpotPaidGold`
- `annualEmissionGold`
- `valid`
- `invalidReason`
- `snapshotSource`

`/v35/burn`:

- `burnAddress`
- `isBurnAddressPlaceholder`
- `burnAddressConfigured`
- `burnedGold`
- `noSpendAfterV35`
- `burnTransactionCreated`
- `v35Active`
- `v35ForkIndex`
- `note`

`/v35/founder`:

- `founderAddress`
- `founderReserveGold`
- `founderRewardGold`
- `modeledBurnPercent`
- `modeledBurnGold`
- `actualBurnedGold`
- `modelOnly`
- `v35Active`
- `v35ForkIndex`
- `note`

`/v35/founder/burn-snapshot`:

- `founderAddress`
- `burnAddress`
- `burnAddressConfigured`
- `isBurnAddressPlaceholder`
- `snapshotHeight`
- `snapshotBlockHash`
- `founderBalanceGold`
- `totalMintedGold`
- `actualBurnedGold`
- `actualCirculatingGold`
- `annualEmissionGold`
- `v35Active`
- `v35ForkIndex`
- `recommendedBurnPercent`
- `burnTransactionCreated`
- `modelOnly`
- `burnOptions`
- `proofChecklist`
- `note`

## 8. Actual Vs Modeled Display Rules

The wallet dashboard must keep these sections separate:

Actual chain values:

- `totalMintedGold`
- `burnedGold`
- `circulatingGold`
- `founderReserveGold`
- `jackpotPaidGold`
- `annualEmissionGold`

Modeled founder burn values:

- `burnOptions[*].burnGold`
- `burnOptions[*].founderReserveAfterBurnGold`
- `burnOptions[*].modeledBurnedGoldAfterBurn`
- `burnOptions[*].modeledCirculatingGoldAfterBurn`
- `burnOptions[*].modeledInflationAfterBurnPercent`
- `proofChecklist` pending/manual burn fields

Rules:

- never add modeled burn to actual burned supply in the UI;
- never subtract modeled burn from actual circulating supply in the actual section;
- never claim founder burn happened unless live endpoint indicates actual burn proof exists;
- keep `burnTransactionCreated = false` visible unless the server later detects a real burn transaction;
- if `modelOnly = true`, label burn options as projections;
- if `snapshotBlockHash = UNKNOWN`, show snapshot as incomplete proof;
- if `valid = false`, show actual supply values conservatively and display `invalidReason`.

## 9. DTO / Response Parsing Recommendation

Recommendation: use a small wallet-local Map-based response reader with normalization helpers, not rigid DTO classes in Phase 5F-3B.

Reasons:

- server endpoints return `Map<String, Object>`;
- some fields are strings, some booleans, some numeric;
- `v35ForkIndex` can be either `"Long.MAX_VALUE"` or a number;
- `burnOptions` and `proofChecklist` are nested structures;
- Map parsing is more tolerant of endpoint evolution during dashboard hardening;
- tests can cover missing/invalid fields without needing large DTO constructors.

Recommended helper methods:

```text
Map<String, Object> getMap(String baseUrl, String path)
String stringField(Map<String, Object> map, String field, String fallback)
boolean booleanField(Map<String, Object> map, String field, boolean fallback)
List<Map<String, Object>> listOfMapsField(Map<String, Object> map, String field)
Map<String, Object> mapField(Map<String, Object> map, String field)
```

If DTOs are added later, do it after endpoint schemas stabilize.

Minimal JSON support options:

- add a generic `UtilsJson.jsonToMap(String json)` helper, or
- keep an `ObjectMapper` private to the wallet dashboard API client.

Prefer a private client-local `ObjectMapper` in Phase 5F-3B to avoid widening global utility surface unless multiple components need it.

## 10. Caching Recommendation

Phase 5F-3B should fetch per request with short timeouts and no persistent cache.

Reasons:

- dashboard is read-only and user-driven;
- endpoint data may change after a burn or chain update;
- a cache adds invalidation complexity;
- server endpoint may scan chain/account data, but Phase 5F-3B should not change server behavior.

Optional later hardening:

- short in-memory cache, for example 15-60 seconds, if dashboard requests become expensive;
- expose last successful snapshot timestamp and server URL in the UI.

## 11. Exact Implementation Order For Phase 5F-3B

1. Add wallet read-only API helper/client.
   - Resolve server URL from `Seting.YOUR_SERVER`, falling back to `Seting.ORIGINAL_ADDRESSES`.
   - Normalize trailing slashes.
   - Fetch only `GET /v35/*` endpoints.
   - Use short timeouts.
   - Return safe empty/unavailable results on failure.

2. Add safe response normalization.
   - Convert values to strings/booleans/lists safely.
   - Do not throw on missing fields.
   - Preserve static dashboard defaults when live fields are absent.

3. Update `V35DashboardController.dashboard(...)`.
   - Build current static model first.
   - Overlay protocol values from `/v35/protocol` when valid.
   - Overlay actual supply values from `/v35/supply` when valid.
   - Overlay burn fields from `/v35/burn`.
   - Overlay founder snapshot fields and burn options from `/v35/founder/burn-snapshot`.
   - Add `liveSupplyAvailable`, `liveProtocolAvailable`, `liveBurnAvailable`, `liveFounderSnapshotAvailable`, `liveApiBaseUrl`, and `liveApiError` fields.

4. Update `v35-dashboard.html`.
   - Show source status for live API sections.
   - Keep existing unavailable fallback.
   - Keep V35 disabled warning prominent.
   - Keep actual and modeled sections separate.

5. Add wallet tests.
   - Use helper-level tests with stubbed/fake responses if no HTTP mocking standard exists.
   - Keep current static fallback tests.
   - Add malformed/missing endpoint response tests.

6. Run required verification.

## 12. Files Phase 5F-3B May Edit

Wallet:

- `unitedStates_final-master/src/main/java/International_Trade_Union/controllers/V35DashboardController.java`
- `unitedStates_final-master/src/main/resources/templates/v35-dashboard.html`
- new wallet read-only helper/client class, for example:
  - `unitedStates_final-master/src/main/java/International_Trade_Union/controllers/V35DashboardApiClient.java`
  - or `unitedStates_final-master/src/main/java/International_Trade_Union/protocol/v35/V35DashboardApiClient.java`
- wallet tests:
  - `unitedStates_final-master/src/test/java/International_Trade_Union/controllers/V35DashboardControllerTest.java`
  - new helper/client tests if added
- report:
  - `CITU_V35_PHASE5F3B_WALLET_DASHBOARD_LIVE_API_REPORT.md`

Only if truly needed:

- `UtilsJson.java` to add a generic JSON-to-map helper.

## 13. Files Phase 5F-3B Must Not Edit

- `Mining.java`
- `UtilsBlock.java`
- `UtilsBalance.java`
- `Blockchain.java`
- `DtoTransaction.java`
- `Block.java`
- server `V35SupplyController.java`
- server `V35SupplyApiResponseBuilder.java`
- server protocol/consensus helpers
- website project files
- transaction/block format classes
- `pom.xml`

## 14. Required Tests

Wallet tests:

1. Static fallback still renders when no server endpoint is reachable.
2. Dashboard route does not throw on endpoint failure.
3. Dashboard route shows `liveSupplyAvailable = false` when `/v35/supply` fails.
4. Dashboard route keeps protocol constants from local defaults when `/v35/protocol` fails.
5. Successful `/v35/protocol` maps:
   - `v35Active`
   - `v35ForkIndex`
   - `200`
   - `432`
   - `8/6/2/0`
   - `BURN_ADDRESS`
6. Successful `/v35/supply` maps actual:
   - `totalMintedGold`
   - `burnedGold`
   - `circulatingGold`
   - `founderReserveGold`
   - `jackpotPaidGold`
7. Successful `/v35/founder/burn-snapshot` maps:
   - `snapshotHeight`
   - `snapshotBlockHash`
   - `founderBalanceGold`
   - `burnOptions`
   - `proofChecklist`
8. Missing fields do not crash and fall back to `Unavailable`.
9. Wrong field types do not crash and fall back safely.
10. Modeled burn options are not copied into actual burned/circulating fields.
11. V35 disabled warning remains visible when fork index is `Long.MAX_VALUE`.
12. No mutation endpoints are called.

If HTTP-level tests are hard:

- inject a small fetch abstraction into the helper/client,
- unit test with fake endpoint maps,
- keep controller tests focused on model output.

## 15. Risks

1. The wallet dashboard could become slow if each page render waits for five endpoints with long timeouts.
2. Old servers without `/v35/*` endpoints could make the dashboard look broken unless fallback is strong.
3. Field type differences, especially `v35ForkIndex`, could cause parsing bugs if handled rigidly.
4. Actual and modeled values could be accidentally mixed if the controller overwrites fields carelessly.
5. The server `/v35/supply` endpoint may perform chain/account scans, so repeated dashboard refreshes could be expensive on production nodes.
6. Saved server URL can be malformed or stale.
7. A malicious/untrusted server could return misleading read-only values; the wallet should label source URL and treat values as server-reported, not local proof.

## 16. Rollback Plan

Because Phase 5F-3B should be read-only:

- remove live API helper/client usage from `V35DashboardController`;
- keep static Phase 5F-1 dashboard model;
- keep `/v35-dashboard` route and template;
- keep server endpoints unchanged;
- keep V35 disabled;
- no chain/balance/protocol rollback should be required.

## 17. Final Recommendation

Phase 5F-3B should integrate the wallet dashboard with server read-only V35 endpoints directly, using the wallet's configured server URL and strong fallback behavior.

Use static local V35 policy data as baseline, overlay live server values only when endpoint responses are available and parse safely, and keep actual chain values visually separated from modeled founder burn projections.
