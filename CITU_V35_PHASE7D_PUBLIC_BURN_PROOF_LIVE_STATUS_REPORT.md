# CITU V35 Phase 7D Public Burn Proof Live Status Report

## 1. Exact reports/files read

Reports read:
- `CITU_V35_PHASE7A_FOUNDER_BURN_PROOF_PACKAGE_PLAN.md`
- `CITU_V35_PHASE7B_PUBLIC_BURN_PROOF_PAGE_REPORT.md`
- `CITU_V35_PHASE7C_FOUNDER_BURN_PROOF_LIVE_API_INTEGRATION_PLAN.md`
- `CITU_V35_PHASE5C2C_SUPPLY_API_ENDPOINTS_REPORT.md`
- `CITU_V35_PHASE5E1_FOUNDER_BURN_SNAPSHOT_REPORT.md`
- `CITU_V35_PHASE5D1_SENTINEL_BURN_ADDRESS_REPLACEMENT_REPORT.md`

Files inspected:
- `website.citu-web-master/src/main/java/com/example/website/citu/controller/V35PublicController.java`
- `website.citu-web-master/src/main/resources/templates/v35-founder-burn-proof.html`
- `website.citu-web-master/src/main/resources/templates/v35.html`
- `website.citu-web-master/src/main/resources/templates/v35-burn.html`
- `website.citu-web-master/src/main/resources/templates/blocks/header.html`
- `website.citu-web-master/src/test/java/com/example/website/citu/controller/V35PublicControllerTest.java`
- `website.citu-web-master/src/main/java/com/example/website/citu/controller/ExplorerController.java`
- `website.citu-web-master/src/main/java/com/example/website/citu/utils/UtilUrl.java`
- `website.citu-web-master/src/main/resources/application.properties`
- `unitedStates_final-master/src/main/java/International_Trade_Union/setings/Seting.java`
- `unitedStates_storage-master/src/main/java/International_Trade_Union/setings/Seting.java`
- `unitedStates_final-master/src/main/java/International_Trade_Union/controllers/V35DashboardApiClient.java`
- `unitedStates_storage-master/src/main/java/International_Trade_Union/controllers/V35SupplyController.java`
- `unitedStates_storage-master/src/main/java/International_Trade_Union/controllers/V35SupplyApiResponseBuilder.java`

Confirmed constants:
- `V35_RESCUE_FORK_INDEX = Long.MAX_VALUE` in wallet and server.
- `V35_BURN_ADDRESS = "BURN_ADDRESS"` in wallet and server.
- Founder address remains `nNifuwmFZr7fnV1zvmpiyQDV5z7ETWvqR6GSeqeHTY43`.

## 2. Exact files changed

Website-only changes:
- `website.citu-web-master/src/main/java/com/example/website/citu/controller/V35FounderBurnProofApiClient.java`
- `website.citu-web-master/src/main/java/com/example/website/citu/controller/V35PublicController.java`
- `website.citu-web-master/src/main/resources/templates/v35-founder-burn-proof.html`
- `website.citu-web-master/src/test/java/com/example/website/citu/controller/V35PublicControllerTest.java`
- `CITU_V35_PHASE7D_PUBLIC_BURN_PROOF_LIVE_STATUS_REPORT.md`

No wallet files were changed.
No server files were changed.
No website `pom.xml` changes were made.

## 3. Integration architecture used

Added a website-local read-only helper:
- `V35FounderBurnProofApiClient`

The client:
- Fetches optional JSON from the public node V35 read-only endpoints.
- Uses short 1500 ms connect/read timeouts.
- Parses responses as `Map<String, Object>` with Jackson.
- Converts endpoint failures, empty responses, malformed JSON, old-node 404s, and timeouts into safe `Unavailable` status.
- Never throws through the controller path.
- Never calls mutation endpoints.

Base URL selection:
1. System property `citu.v35.api.base-url`
2. Environment variable `CITU_V35_API_BASE_URL`
3. Existing website explorer node base URL from `ExplorerController.getAddress()`

This keeps integration optional while preserving the existing website node selection pattern.

## 4. API endpoints consumed or prepared

The website proof page now optionally prepares/consumes:
- `/v35/supply`
- `/v35/burn`
- `/v35/founder`
- `/v35/founder/burn-snapshot`

The page displays endpoint-level availability for each endpoint.

## 5. Fallback behavior

The `/v35/founder-burn-proof` page renders safely when:
- API server is unavailable.
- Endpoints are missing.
- Timeout occurs.
- Malformed JSON is returned.
- Partial data is returned.
- V35 is disabled.
- Burn is not executed.

Fallback display:
- API status: `Unavailable`
- Burn status: `Planned / Pending / Not executed yet`
- Unknown actual fields: `Unavailable`
- Unknown proof fields: `Pending`
- No fake transaction hash, block hash, snapshot hash, snapshot height, or actual burn amount.

## 6. Actual vs planned/modeled burn separation

The page now separates:
- Static policy values: founder address, `BURN_ADDRESS`, 80% recommendation, V35 disabled warning.
- Actual live values: total minted, burned, circulating, founder reserve, jackpot paid, annual emission, V35 state.
- Modeled/recommended values: modeled 80% burn amount when supplied by `/v35/founder`.
- Final transaction proof fields: still pending until real burn proof exists.

Rules preserved:
- Actual burned supply is displayed only from `/v35/supply` or `/v35/burn`.
- Modeled 80% burn is labeled as modeled and not actual burned supply.
- Burn transaction proof remains pending unless real proof is published later.
- V35 is not claimed active while fork index remains `Long.MAX_VALUE`.

## 7. Confirmation no fake burn proof values are displayed

No fake proof values were added.

The template still shows pending placeholders for:
- Burn transaction hash.
- Burn block index.
- Burn block hash.
- Burn timestamp.
- Burn amount.
- Fee.
- Confirmations.
- Validation status.
- Snapshot fields when no API data is available.

If an API later reports optional snapshot values, they are displayed in the live-status section and not treated as final burn transaction proof.

## 8. Confirmation no burn transaction was created

No founder burn transaction was created.
No transaction was broadcast.
No balance was changed.

## 9. Confirmation no jackpot payout transaction was created

No jackpot payout transaction was created.
No mining code was touched.

## 10. Confirmation V35 remains disabled

V35 remains disabled.

The production fork constants were not changed:
- Wallet: `V35_RESCUE_FORK_INDEX = Long.MAX_VALUE`
- Server: `V35_RESCUE_FORK_INDEX = Long.MAX_VALUE`

## 11. Confirmation no wallet/server consensus behavior changed

No wallet files were changed.
No server files were changed.
No consensus, mining, validation, balance, transaction, block, jackpot, burn policy, or API semantics were changed.

## 12. Test/build results

Commands run from `website.citu-web-master`:
- `.\mvnw.cmd test` — passed.
- `.\mvnw.cmd clean package` — passed.

Test coverage added/updated:
- Route still renders.
- Static proof template still uses pending proof values.
- Optional live API status section exists.
- Fallback when API calls fail.
- Malformed JSON fallback.
- Live response mapping without treating modeled burn as actual burned supply.
- V35 remains displayed as disabled in fallback/live test data.
- `BURN_ADDRESS` and founder address remain visible.

Build note:
- Maven still reports the pre-existing duplicate `org.bouncycastle:bcprov-jdk15on` dependency warning in the website `pom.xml`.
- `pom.xml` was not changed in this phase.

## 13. Remaining risks

- The website now uses the existing explorer node as fallback API source when no explicit V35 API base URL is configured. If that public node is old or unavailable, the proof page correctly falls back to unavailable live values.
- Live transaction proof still needs a future post-burn source of truth; this phase does not invent or derive burn transaction hash/block proof.
- Endpoint responses are parsed map-style for compatibility; schema drift will result in `Unavailable` fields rather than page failure.
- Public proof still needs real snapshot and burn data after the manual burn.

## 14. Recommended next phase

Recommended next phase:
- `Phase 8A — public release announcement / exchange notice draft`

Optional technical safety phase before public release wording:
- `Phase 6D-2 — optional long-window difficulty simulation`

If the manual burn is approaching:
- `Phase 7E — final burn execution checklist, no actual burn`
