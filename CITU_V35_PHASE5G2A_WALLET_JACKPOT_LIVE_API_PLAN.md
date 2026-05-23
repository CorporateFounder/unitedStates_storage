# CITU V35 Phase 5G-2A Wallet Jackpot Live API Plan

Phase: 5G-2A  
Project root: `C:\citu`  
Scope: planning only. No source code, wallet dashboard, server jackpot API, website, protocol, mining, validation, balance, transaction format, block format, V35 activation, burn transaction, or jackpot payout transaction changes were made.

## 1. Exact Files / Methods Inspected

Reports read:

- `CITU_V35_PHASE5G1_JACKPOT_READONLY_API_ENDPOINTS_REPORT.md`
- `CITU_V35_PHASE5F3B_WALLET_DASHBOARD_LIVE_API_REPORT.md`
- `CITU_V35_PHASE5F1_WALLET_V35_DASHBOARD_REPORT.md`

Wallet files inspected:

- `unitedStates_final-master/src/main/java/International_Trade_Union/controllers/V35DashboardController.java`
  - `dashboard(...)`
  - `overlayLiveData(...)`
  - `overlayProtocol(...)`
  - `overlaySupply(...)`
  - `overlayBurn(...)`
  - `overlayFounder(...)`
  - `overlayFounderBurnSnapshot(...)`
  - `jackpotPeriods()`
  - `warnings()`
- `unitedStates_final-master/src/main/java/International_Trade_Union/controllers/V35DashboardApiClient.java`
  - `fetch()`
  - `fetchMap(...)`
  - `configuredServerUrl()`
  - `normalizeBaseUrl(...)`
  - `stringField(...)`
  - `booleanField(...)`
  - `listOfMapsField(...)`
  - `LiveData`
- `unitedStates_final-master/src/main/resources/templates/v35-dashboard.html`
  - Live read-only data section
  - Protocol/reward model section
  - Actual supply/burn section
  - Jackpot policy section
  - Warning section
- `unitedStates_final-master/src/test/java/International_Trade_Union/controllers/V35DashboardControllerTest.java`
- `unitedStates_final-master/src/test/java/International_Trade_Union/controllers/V35DashboardApiClientTest.java`

Server jackpot API files inspected:

- `unitedStates_storage-master/src/main/java/International_Trade_Union/controllers/V35JackpotController.java`
  - `/v35/jackpot/status`
  - `/v35/jackpot/periods`
  - `/v35/jackpot/history`
  - `/v35/jackpot/tickets/{address}`
  - `/v35/jackpot/proof/{periodType}/{periodEnd}`
- `unitedStates_storage-master/src/main/java/International_Trade_Union/protocol/v35/V35JackpotApiResponseBuilder.java`
  - `status(...)`
  - `periods(...)`
  - `history(...)`
  - `tickets(...)`
  - `proof(...)`

Constants confirmed:

- wallet `Seting.V35_RESCUE_FORK_INDEX = Long.MAX_VALUE`
- server `Seting.V35_RESCUE_FORK_INDEX = Long.MAX_VALUE`
- wallet `Seting.V35_BURN_ADDRESS = "BURN_ADDRESS"`
- server `Seting.V35_BURN_ADDRESS = "BURN_ADDRESS"`

V35 remains disabled.

## 2. Recommended Wallet Jackpot Dashboard Sections

Phase 5G-2B should update the existing `/v35-dashboard` page, not create a separate wallet page.

Recommended jackpot dashboard sections:

1. Jackpot live status
   - V35 jackpot active/inactive state
   - server jackpot API availability
   - disabled warning:
     - `V35 jackpot policy is configured but not active yet.`
   - current height if available
   - random delay blocks
   - winner source: `minerAddress`
   - sender eligibility: `false`

2. Jackpot policy / canonical payout table
   - preserve the current static table as baseline
   - overlay live `/v35/jackpot/periods` values when available
   - display six period rows:
     - daily
     - weekly
     - monthly
     - quarterly
     - half-year
     - yearly

3. Period status table
   - from `/v35/jackpot/status`
   - fields:
     - period type
     - canonical payout
     - current period start/end
     - maturity block
     - mature flag
     - estimated tickets
     - status
   - when disabled, show `DISABLED` rows and do not imply live tickets

4. Paid jackpot history summary
   - from `/v35/jackpot/history`
   - show a compact table if paid history exists
   - if empty:
     - `No paid V35 jackpot history yet.`
   - while disabled:
     - explain that live jackpot history starts only after V35 activation

5. Ticket eligibility note
   - always visible
   - wording:
     - `Only mined V35 blocks create jackpot tickets. The participant is block minerAddress. Sender addresses are never jackpot participants.`

6. Proof availability note
   - do not fetch proof by default
   - show proof links only when status/history rows include enough period data
   - wording:
     - `Deterministic proof is available after a period matures and the delayed 60 future block hashes exist.`

## 3. Recommended Jackpot Endpoints To Consume

Consume first in Phase 5G-2B:

1. `/v35/jackpot/status`
   - highest value for wallet dashboard
   - provides disabled warning and per-period status rows

2. `/v35/jackpot/periods`
   - use to overlay canonical payout table
   - static fallback already exists in wallet, so endpoint failure is low risk

3. `/v35/jackpot/history`
   - show paid history summary
   - empty list while disabled is expected

Do not fetch by default in Phase 5G-2B:

4. `/v35/jackpot/tickets/{address}`
   - requires a selected wallet/miner address
   - the current dashboard does not expose a clear selected miner address input
   - should wait until Phase 5G-2C or a wallet account selector phase

5. `/v35/jackpot/proof/{periodType}/{periodEnd}`
   - proof can be expensive/noisy
   - should be linked from period/history rows but not fetched automatically
   - proof detail can wait until a later page/modal/endpoint drilldown phase

## 4. Fields To Display

### From `/v35/jackpot/status`

Top-level fields:

- `v35Active`
- `v35ForkIndex`
- `blockTimeSeconds`
- `blocksPerDay`
- `randomDelayBlocks`
- `firstPayoutBlockOffset`
- `burnAddress`
- `winnerSource`
- `senderEligibilityAllowed`
- `currentHeight`
- `jackpotReservePerBlockGold`
- `warning`

Period fields:

- `periodType`
- `windowBlocks`
- `canonicalPayoutGold`
- `currentPeriodStart`
- `currentPeriodEnd`
- `maturityBlock`
- `mature`
- `estimatedTickets`
- `status`

### From `/v35/jackpot/periods`

Top-level fields:

- `exactSlotOnly`
- `missedPayoutsPaidLater`
- `explanation`
- `winnerSource`
- `senderEligibilityAllowed`

Period fields:

- `periodType`
- `periodName`
- `windowBlocks`
- `canonicalPayoutGold`
- `reservePerBlockGold`
- `randomDelayBlocks`
- `explanation`

### From `/v35/jackpot/history`

Top-level fields:

- `status`
- `warning`
- `items`

History item fields:

- `periodType`
- `periodStart`
- `periodEnd`
- `maturityBlock`
- `payoutBlockIndex`
- `payoutBlockHash`
- `payoutTxHash`
- `winnerMinerAddress`
- `payoutGold`
- `proofStatus`
- `selectedOffset`
- `selectedBlockIndex`

If `items` is empty, display:

- `No paid V35 jackpot history yet.`

## 5. Fallback / Unavailable Behavior

Endpoint failures must not break `/v35-dashboard`.

Recommended behavior:

- If all jackpot endpoints fail:
  - keep current static jackpot policy table visible
  - show `Live jackpot data unavailable`
  - show status/history/tickets/proof as `Unavailable`
  - keep V35 disabled warning visible

- If only `/v35/jackpot/status` fails:
  - show policy table from `/v35/jackpot/periods` or static fallback
  - show period statuses as `Unavailable`

- If only `/v35/jackpot/periods` fails:
  - keep static local `JackpotCalculator` period table
  - continue showing status/history if available

- If `/v35/jackpot/history` fails:
  - show `Live jackpot history unavailable`
  - do not fake winners

- If `/v35/jackpot/history` returns empty:
  - show `No paid V35 jackpot history yet`
  - while disabled, also show that jackpots begin after fork activation

The dashboard must never mix static policy data with actual paid history. Actual jackpot history comes only from `/v35/jackpot/history`.

## 6. Ticket / Proof Display Plan

### Tickets

Do not implement live ticket lookup in Phase 5G-2B unless the wallet already has a selected miner address in model state.

Recommended Phase 5G-2B behavior:

- show eligibility rules only
- show `/v35/jackpot/tickets/{address}` as a future read-only source
- do not call it by default

Reason:

- sender addresses are not participants
- the wallet may have multiple accounts/addresses
- calling tickets for the wrong address could mislead users

Future Phase 5G-2C:

- add a selected miner/account address control
- call `/v35/jackpot/tickets/{address}` only after user chooses the address
- default to no ticket lookup rather than guessing

### Proof

Do not fetch proof by default in Phase 5G-2B.

Recommended behavior:

- show proof availability note
- if status/history rows include `periodType` and `periodEnd`, render a link or text target:
  - `/v35/jackpot/proof/{periodType}/{periodEnd}`
- defer a proof detail view to a later phase

Reason:

- proof is period-specific
- proof can become verbose
- the current endpoint does not expose all 60 seed hashes yet
- automatic proof fetching for six periods on every page render is unnecessary while V35 is disabled

## 7. Existing Client Extension Recommendation

Extend the existing `V35DashboardApiClient`.

Recommended additions:

- Add endpoint path constants:
  - `JACKPOT_STATUS_PATH = "/v35/jackpot/status"`
  - `JACKPOT_PERIODS_PATH = "/v35/jackpot/periods"`
  - `JACKPOT_HISTORY_PATH = "/v35/jackpot/history"`
- Fetch these after the existing five supply/founder endpoints.
- Add fields to `LiveData`:
  - `jackpotStatus`
  - `jackpotPeriods`
  - `jackpotHistory`
- Add availability methods:
  - `jackpotStatusAvailable()`
  - `jackpotPeriodsAvailable()`
  - `jackpotHistoryAvailable()`
- Keep current `Map<String,Object>` parsing style.

Do not introduce strict DTO classes yet. The current tolerant map model is safer for old nodes, partial API upgrades, and malformed endpoint responses.

## 8. Exact Implementation Order For Phase 5G-2B

1. Confirm `V35_RESCUE_FORK_INDEX = Long.MAX_VALUE` and `V35_BURN_ADDRESS = BURN_ADDRESS`.
2. Extend `V35DashboardApiClient` to call:
   - `/v35/jackpot/status`
   - `/v35/jackpot/periods`
   - `/v35/jackpot/history`
3. Keep the client read-only and keep timeout/error behavior unchanged.
4. Extend `LiveData` with jackpot maps and availability methods.
5. In `V35DashboardController.dashboard(...)`, initialize jackpot live model defaults:
   - `liveJackpotStatusAvailable = false`
   - `liveJackpotPeriodsAvailable = false`
   - `liveJackpotHistoryAvailable = false`
   - `jackpotStatusWarning = ""`
   - `jackpotPeriodStatuses = []`
   - `jackpotHistoryItems = []`
   - `jackpotHistoryMessage = "Unavailable"`
6. Add overlay methods:
   - `overlayJackpotStatus(...)`
   - `overlayJackpotPeriods(...)`
   - `overlayJackpotHistory(...)`
7. Keep static `jackpotPeriods()` as fallback.
8. Update `v35-dashboard.html` jackpot section:
   - live jackpot API availability
   - disabled warning/status
   - status table
   - policy table
   - history summary
   - ticket eligibility note
   - proof availability note
9. Update wallet tests.
10. Run wallet/server/website tests and packages.
11. Create Phase 5G-2B report.

## 9. Files Phase 5G-2B May Edit

Wallet:

- `unitedStates_final-master/src/main/java/International_Trade_Union/controllers/V35DashboardApiClient.java`
- `unitedStates_final-master/src/main/java/International_Trade_Union/controllers/V35DashboardController.java`
- `unitedStates_final-master/src/main/resources/templates/v35-dashboard.html`
- `unitedStates_final-master/src/test/java/International_Trade_Union/controllers/V35DashboardApiClientTest.java`
- `unitedStates_final-master/src/test/java/International_Trade_Union/controllers/V35DashboardControllerTest.java`

Report:

- `CITU_V35_PHASE5G2B_WALLET_JACKPOT_LIVE_API_REPORT.md`

## 10. Files Phase 5G-2B Must Not Edit

- server `V35JackpotController.java`
- server `V35JackpotApiResponseBuilder.java`
- server protocol/mining/validation/balance files
- wallet `Mining.java`
- wallet `UtilsBlock.java`
- wallet `UtilsBalance.java`
- wallet `Blockchain.java`
- website project
- transaction format
- block format
- `Seting.V35_RESCUE_FORK_INDEX`
- burn transaction or jackpot payout creation code
- `pom.xml`

## 11. Required Tests

Wallet API client tests:

- client calls only read-only jackpot endpoints:
  - `/v35/jackpot/status`
  - `/v35/jackpot/periods`
  - `/v35/jackpot/history`
- client still calls existing supply/founder endpoints.
- client does not call:
  - `/mine`
  - `/send`
  - `/nodes/resolve_from_to_block`
  - any mutation endpoint
- malformed jackpot JSON does not crash the client.
- old server without jackpot endpoints keeps jackpot live fields unavailable.

Wallet controller/template tests:

- dashboard renders if jackpot endpoints unavailable.
- static jackpot policy table remains visible when jackpot API fails.
- live jackpot disabled warning appears when returned by `/v35/jackpot/status`.
- status table shows `DISABLED` rows while V35 is disabled.
- history empty message says `No paid V35 jackpot history yet`.
- sender addresses are never described as eligible participants.
- winner source is shown as `minerAddress`.
- proof is described as available only after maturity.
- tickets are not fetched by default.
- modeled/supply data remains separate from jackpot history.
- route still renders.

Cross-project verification:

- wallet `.\mvnw.cmd test`
- wallet `.\mvnw.cmd clean package`
- server `.\mvnw.cmd test`
- server `.\mvnw.cmd clean package`
- website `.\mvnw.cmd test`
- website `.\mvnw.cmd clean package`

## 12. Risks

1. Page render latency:
   - The dashboard currently fetches five endpoints; adding three more makes eight per render.
   - Mitigation: keep the existing short timeout and tolerant fallback.
2. Endpoint version skew:
   - Older servers will not have `/v35/jackpot/*`.
   - Mitigation: unavailable state must be normal, not an error page.
3. Misleading live state:
   - V35 is disabled, so jackpot status must not look active.
   - Mitigation: show disabled warning and `DISABLED` rows.
4. Ticket address confusion:
   - The wallet may not know which miner address to query.
   - Mitigation: do not call tickets endpoint until an address is selected.
5. Proof noise:
   - Proof is period-specific and potentially verbose.
   - Mitigation: link or mention proof endpoint, do not fetch by default.
6. Actual vs policy confusion:
   - Static jackpot policy can be shown without API, but paid history must not be faked.
   - Mitigation: only show paid history from `/v35/jackpot/history`.

## 13. Rollback Plan

If Phase 5G-2B causes dashboard issues:

1. Revert only the wallet dashboard live jackpot API changes.
2. Keep server jackpot endpoints from Phase 5G-1 unchanged.
3. Restore the current static jackpot policy section and existing supply/founder live API behavior.
4. Confirm no protocol/mining/validation/balance files changed.
5. Re-run wallet/server/website tests and packages.

Because Phase 5G-2B should remain wallet read-only UI/client work, rollback should not affect consensus or node behavior.

## 14. Recommendation

Phase 5G-2B should integrate only these jackpot endpoints into the wallet dashboard by default:

- `/v35/jackpot/status`
- `/v35/jackpot/periods`
- `/v35/jackpot/history`

Tickets and proof should remain later/on-demand work:

- `/v35/jackpot/tickets/{address}` after a selected miner address exists
- `/v35/jackpot/proof/{periodType}/{periodEnd}` as a link or drilldown, not an automatic page-render fetch

This keeps the dashboard useful immediately while preserving the most important safety rule: the wallet can show policy and read-only history, but it must not create payout expectations, fake winners, or imply V35 jackpot activation while the fork index is still `Long.MAX_VALUE`.
