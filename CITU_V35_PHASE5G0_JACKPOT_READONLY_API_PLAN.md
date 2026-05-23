# CITU V35 Phase 5G-0 Jackpot Read-Only API Plan

Phase: 5G-0  
Project root: `C:\citu`  
Scope: planning only. No source code, tests, controllers, wallet UI, website, protocol, mining, validation, balance behavior, transaction format, block format, V35 activation, or burn transaction changes were made.

## 1. Exact Files / Classes Inspected

Reports read:

- `CITU_V35_PHASE4A_JACKPOT_DESIGN_PLAN.md`
- `CITU_V35_PHASE4B_JACKPOT_HELPERS_REPORT.md`
- `CITU_V35_PHASE4C_JACKPOT_WINNER_SELECTION_REPORT.md`
- `CITU_V35_PHASE4D_JACKPOT_PAYOUT_VALIDATION_REPORT.md`
- `CITU_V35_PHASE4D1_JACKPOT_PAYOUT_CLASSIFICATION_PLAN.md`
- `CITU_V35_PHASE4D1C_ECONOMICS_432_8GOLD_UPDATE_REPORT.md`
- `CITU_V35_PHASE4D2A_JACKPOT_PAYOUT_CLASSIFICATION_HELPERS_REPORT.md`
- `CITU_V35_PHASE4D2B_UTILSBLOCK_JACKPOT_VALIDATION_REPORT.md`
- `CITU_V35_PHASE4D2C_JACKPOT_CHAIN_CONTEXT_PROOF_PLAN.md`
- `CITU_V35_PHASE4D2D_JACKPOT_PROOF_SERVICE_REPORT.md`
- `CITU_V35_PHASE4D2E_JACKPOT_CHAIN_CONTEXT_ADAPTER_REPORT.md`
- `CITU_V35_PHASE4D2F_UTILSBLOCK_CHAIN_CONTEXT_JACKPOT_VALIDATION_REPORT.md`
- `CITU_V35_PHASE4D2G_UTILSBALANCE_JACKPOT_ACCOUNTING_REPORT.md`
- `CITU_V35_PHASE4E0_MINING_JACKPOT_PAYOUT_PLAN.md`
- `CITU_V35_PHASE4E1A_JACKPOT_PAYOUT_BUILDER_REPORT.md`
- `CITU_V35_PHASE4E1B_JACKPOT_PRODUCTION_VALIDATION_PATH_PLAN.md`
- `CITU_V35_PHASE4E1C0_CONTEXT_AWARE_VALIDATION_OVERLOAD_REPORT.md`
- `CITU_V35_PHASE4E1C1_PRODUCTION_JACKPOT_VALIDATION_WIRING_REPORT.md`
- `CITU_V35_PHASE4E1C2_UTILSRESOLVING_PREFLIGHT_REVIEW.md`
- `CITU_V35_PHASE4E1C3_UTILSRESOLVING_JACKPOT_WIRING_REPORT.md`
- `CITU_V35_PHASE4E1D_MINING_JACKPOT_PAYOUT_WIRING_REPORT.md`
- `CITU_V35_PHASE4E1E_JACKPOT_E2E_TEST_REPORT.md`
- `CITU_V35_PHASE4E1F_JACKPOT_CONTROLLER_STORAGE_REVIEW.md`
- `CITU_V35_PHASE4E1G_JACKPOT_CONTROLLER_STORAGE_INTEGRATION_REPORT.md`
- `CITU_V35_PHASE5F1_WALLET_V35_DASHBOARD_REPORT.md`
- `CITU_V35_PHASE5F2_WEBSITE_V35_PUBLIC_PAGES_REPORT.md`
- `CITU_V35_PHASE5F3B_WALLET_DASHBOARD_LIVE_API_REPORT.md`

Server files/classes inspected:

- `unitedStates_storage-master/src/main/java/International_Trade_Union/protocol/v35/JackpotCalculator.java`
  - `JackpotPeriod`
  - `JACKPOT_RANDOM_DELAY_BLOCKS`
  - `windowBlocksForPeriod(...)`
  - `reservePerBlockForPeriod(...)`
  - `expectedPayoutAmount(...)`
  - `classifyPayoutAmount(...)`
  - `selectWinner(...)`
- `unitedStates_storage-master/src/main/java/International_Trade_Union/protocol/v35/JackpotTicket.java`
- `unitedStates_storage-master/src/main/java/International_Trade_Union/protocol/v35/JackpotWinnerProof.java`
- `unitedStates_storage-master/src/main/java/International_Trade_Union/protocol/v35/JackpotPayoutCandidate.java`
- `unitedStates_storage-master/src/main/java/International_Trade_Union/protocol/v35/JackpotPeriodInstance.java`
- `unitedStates_storage-master/src/main/java/International_Trade_Union/protocol/v35/JackpotProofResult.java`
- `unitedStates_storage-master/src/main/java/International_Trade_Union/protocol/v35/V35JackpotProofService.java`
  - `verifyPayout(...)`
  - `periodInstanceForPayoutBlock(...)`
  - `markersForPayoutBlock(...)`
  - `ticketsForPeriod(...)`
  - `futureHashesForSeed(...)`
- `unitedStates_storage-master/src/main/java/International_Trade_Union/protocol/v35/V35JackpotChainContextAdapter.java`
  - `buildPayoutCandidate(...)`
  - `buildPeriodInstance(...)`
  - `collectEligibleTickets(...)`
  - `collectFutureHashes(...)`
  - `collectPriorPayoutMarkers(...)`
  - `hasPriorPayoutForPeriod(...)`
  - `verifyPayoutWithChainContext(...)`
- `unitedStates_storage-master/src/main/java/International_Trade_Union/protocol/v35/V35JackpotPayoutBuilder.java`
- `unitedStates_storage-master/src/main/java/International_Trade_Union/model/Mining.java`
  - `buildV35JackpotPayoutTransactionsForMining(...)`
  - V35 branch in `miningDay(...)`
- `unitedStates_storage-master/src/main/java/International_Trade_Union/entity/blockchain/Blockchain.java`
  - `shortCheckWithCanonicalContext(...)`
  - `checkFromFile(...)`
- `unitedStates_storage-master/src/main/java/International_Trade_Union/utils/UtilsBalance.java`
  - `calculateBalances(...)`
  - V35 jackpot proof-aware balance path
- `unitedStates_storage-master/src/main/java/International_Trade_Union/controllers/V35SupplyController.java`
- `unitedStates_storage-master/src/main/java/International_Trade_Union/protocol/v35/V35SupplyApiResponseBuilder.java`
- `unitedStates_storage-master/src/main/java/International_Trade_Union/controllers/BasisController.java`
- `unitedStates_storage-master/src/main/java/International_Trade_Union/controllers/ConductorController.java`
- `unitedStates_storage-master/src/main/java/International_Trade_Union/entity/services/BlockService.java`

Existing server endpoint/data access patterns inspected:

- `V35SupplyController`
  - `/v35/supply`
  - `/v35/protocol`
  - `/v35/burn`
  - `/v35/founder`
  - `/v35/founder/burn-snapshot`
  - uses `BlockService.countBlock()`
  - uses `BlockService.findBySpecialIndexBetween(0, currentHeight)`
  - uses `BlockService.findAllAccounts()`
- `BasisController`
  - `/size`
  - `/sub-blocks`
  - `/block`
  - `/prevBlock`
  - `/nodes/resolve_from_to_block`
  - `/allAccounts`
  - `/totalDollars`
- `ConductorController`
  - `/account`
  - `/senderTransactions`
  - `/customerTransactions`
  - `/senderCountDto`
  - `/customerCountDto`
  - `/addresses`
  - range block lookup endpoints
- `BlockService`
  - `findBySpecialIndexBetween(...)`
  - `findAllDto()`
  - `findAllAccounts()`
  - `countBlock()`

Constants confirmed:

- wallet `Seting.V35_RESCUE_FORK_INDEX = Long.MAX_VALUE`
- server `Seting.V35_RESCUE_FORK_INDEX = Long.MAX_VALUE`
- wallet `Seting.V35_BURN_ADDRESS = "BURN_ADDRESS"`
- server `Seting.V35_BURN_ADDRESS = "BURN_ADDRESS"`

V35 remains disabled.

## 2. Recommended Jackpot Endpoints

Implement server-first read-only endpoints under a new controller, for example:

- `GET /v35/jackpot/status`
- `GET /v35/jackpot/periods`
- `GET /v35/jackpot/history`
- `GET /v35/jackpot/tickets/{address}`
- `GET /v35/jackpot/proof/{periodType}/{periodEnd}`

Recommended controller:

- `International_Trade_Union.controllers.V35JackpotController`

Recommended response/helper classes:

- `International_Trade_Union.protocol.v35.V35JackpotApiResponseBuilder`
- Optional read-only service/helper:
  - `International_Trade_Union.protocol.v35.V35JackpotReadOnlyService`

The endpoints must be read-only:

- no mining
- no payout creation
- no burn creation
- no validation behavior changes
- no balance mutation
- no settings mutation
- no controller paths that write chain data

## 3. Response Schema

### 3.1 `GET /v35/jackpot/status`

Purpose:

- Show the current jackpot policy and current period status for all six jackpot periods.
- While V35 is disabled, return policy/static values and disabled-state fields without pretending live jackpot periods exist.

Recommended fields:

- `v35Active`
- `v35ForkIndex`
- `v35ForkIndexDisplay`
- `currentHeight`
- `currentBlockHash`
- `blockTimeSeconds`
- `blocksPerDay`
- `randomDelayBlocks`
- `firstPayoutBlockOffset`
  - value: `61`
  - explanation: payout block is `periodEnd + 60 + 1`
- `senderEligibilityAllowed`
  - value: `false`
- `winnerSource`
  - value: `minerAddress`
- `periods`
  - array of period status rows

Recommended period row fields:

- `periodType`
  - `DAILY`
  - `WEEKLY`
  - `MONTHLY`
  - `QUARTERLY`
  - `HALF_YEAR`
  - `YEARLY`
- `periodName`
- `windowBlocks`
- `reservePerBlockGold`
- `canonicalFullPeriodPayoutGold`
- `currentPeriodStart`
- `currentPeriodEnd`
- `maturityBlock`
- `firstPayoutBlock`
- `blocksUntilPeriodEnd`
- `blocksUntilMaturity`
- `blocksUntilFirstPayout`
- `eligibleTicketCount`
- `accruedEstimatedPoolGold`
- `status`
  - `DISABLED`
  - `OPEN`
  - `AWAITING_RANDOMNESS`
  - `MATURE_UNPAID`
  - `PAID`
  - `INSUFFICIENT_CONTEXT`
- `paid`
- `payoutBlockIndex`
- `payoutBlockHash`
- `payoutTransactionHash`
- `winnerMinerAddress`
- `proofAvailable`
- `proofEndpoint`

Disabled behavior:

- `v35Active = false`
- `v35ForkIndexDisplay = "Long.MAX_VALUE"`
- period policy rows are still returned
- live period fields may be `null` or `Unavailable`
- status should be `DISABLED`
- no live winners should be reported

### 3.2 `GET /v35/jackpot/periods`

Purpose:

- Return static jackpot policy/configuration values for dashboards and website copy.
- This endpoint can work even if no chain data is available.

Recommended fields:

- `v35Active`
- `v35ForkIndex`
- `blockTimeSeconds`
- `blocksPerDay`
- `randomDelayBlocks`
- `winnerSource`
- `senderEligibilityAllowed`
- `exactSlotOnly`
- `missedPayoutsPaidLater`
  - value: `false`
- `periods`

Recommended period config fields:

- `periodType`
- `periodName`
- `windowBlocks`
- `reservePerBlockGold`
- `canonicalFullPeriodPayoutGold`
- `annualizedOccurrencesEstimate`

Canonical period table:

| Period | Window blocks | Reserve/block | Canonical full-period payout |
|---|---:|---:|---:|
| Daily | 432 | 0.70 | 302.40 GOLD |
| Weekly | 3,024 | 0.40 | 1,209.60 GOLD |
| Monthly | 12,960 | 0.30 | 3,888.00 GOLD |
| Quarterly | 38,880 | 0.25 | 9,720.00 GOLD |
| Half-year | 78,624 | 0.20 | 15,724.80 GOLD |
| Yearly | 157,680 | 0.15 | 23,652.00 GOLD |

### 3.3 `GET /v35/jackpot/history`

Purpose:

- Return paid jackpot payout history from the canonical chain.
- Do not trust amount alone. A history item should be included as paid only if the transaction validates through the same proof path.

Recommended query parameters:

- `periodType`
  - optional
- `fromBlock`
  - optional
- `toBlock`
  - optional
- `limit`
  - optional, default bounded value
- `offset`
  - optional

Recommended response fields:

- `v35Active`
- `v35ForkIndex`
- `currentHeight`
- `limit`
- `offset`
- `items`
- `hasMore`

Recommended history item fields:

- `periodType`
- `periodStart`
- `periodEnd`
- `maturityBlock`
- `firstPayoutBlock`
- `payoutBlockIndex`
- `payoutBlockHash`
- `payoutTransactionHash`
- `payoutAmountGold`
- `winnerMinerAddress`
- `selectedOffset`
- `selectedBlockIndex`
- `eligibleTicketCount`
- `seedHex`
- `proofStatus`
- `proofEndpoint`

History should be empty while V35 is disabled unless a later activation/replay context can validate historical V35 payouts. With current `Long.MAX_VALUE`, no live jackpot payouts should be considered active.

### 3.4 `GET /v35/jackpot/tickets/{address}`

Purpose:

- Show ticket counts for a miner address.
- Tickets are based only on `Block.getMinerAddress()`.
- Sender/customer transaction addresses are never participants.

Recommended query parameters:

- `periodType`
  - optional
- `periodEnd`
  - optional
- `includeBlockIndexes`
  - optional, default `false`
- `limit`
  - optional if indexes are included
- `offset`
  - optional if indexes are included

Recommended response fields:

- `address`
- `v35Active`
- `v35ForkIndex`
- `senderEligibilityAllowed`
- `winnerSource`
- `periods`

Recommended period ticket fields:

- `periodType`
- `periodStart`
- `periodEnd`
- `ticketCount`
- `eligibleTicketCount`
- `ticketSharePercent`
- `firstTicketBlockIndex`
- `lastTicketBlockIndex`
- `blockIndexes`
  - optional, paginated

Disabled behavior:

- return policy metadata and `v35Active=false`
- ticket counts should be `0` or unavailable, not fabricated from pre-V35 blocks

### 3.5 `GET /v35/jackpot/proof/{periodType}/{periodEnd}`

Purpose:

- Return enough data to independently verify a period winner and payout status.
- If period is not mature, return a pending proof with no winner.

Recommended fields:

- `v35Active`
- `v35ForkIndex`
- `periodType`
- `periodStart`
- `periodEnd`
- `windowBlocks`
- `reservePerBlockGold`
- `canonicalFullPeriodPayoutGold`
- `maturityBlock`
- `firstPayoutBlock`
- `randomDelayBlocks`
- `seedHashStartBlock`
- `seedHashEndBlock`
- `seedHashes`
  - optional or bounded; default can include the 60 hashes because they are public block hashes
- `seedHex`
- `eligibleTicketCount`
- `selectedOffset`
- `selectedBlockIndex`
- `winnerMinerAddress`
- `status`
  - `DISABLED`
  - `NOT_MATURE`
  - `MATURE_NO_TICKETS`
  - `MATURE_PROVEN`
  - `PAID`
  - `INVALID_CONTEXT`
- `paid`
- `payoutBlockIndex`
- `payoutBlockHash`
- `payoutTransactionHash`
- `duplicateKey`
- `validationReason`

Proof endpoint rules:

- Do not create payout transactions.
- Do not infer participants from senders.
- If future hashes are missing, return `NOT_MATURE` or `INVALID_CONTEXT`.
- If no eligible mined blocks exist, return no winner.
- If `periodEnd` is not fork-relative and aligned to the requested period window, reject or return `INVALID_CONTEXT`.

## 4. How Current Periods Are Calculated

Use fork-relative period boundaries after V35:

```text
relativeIndex = currentHeight - v35ForkIndex
periodNumber = relativeIndex / windowBlocks
periodStart = v35ForkIndex + (periodNumber * windowBlocks)
periodEnd = periodStart + windowBlocks - 1
maturityBlock = periodEnd + 60
firstPayoutBlock = periodEnd + 61
```

Important:

- Pre-V35 blocks must never become jackpot tickets.
- If `V35_RESCUE_FORK_INDEX == Long.MAX_VALUE`, current live periods should not be calculated from chain height.
- `/v35/jackpot/periods` may still return static policy windows.
- `/v35/jackpot/status` should report disabled state rather than pretending a live period exists.

For proof lookup by `periodType` and `periodEnd`:

```text
periodStart = periodEnd - windowBlocks + 1
periodStart >= v35ForkIndex
(periodStart - v35ForkIndex) % windowBlocks == 0
```

If these conditions fail, the period identifier is invalid.

## 5. How Estimated Pools Are Calculated

Expose two different pool concepts so dashboards do not mix actual paid history with policy estimates:

1. `canonicalFullPeriodPayoutGold`
   - the full payout expected for a complete fixed period
   - static policy value
   - calculated by `JackpotCalculator.expectedPayoutAmount(periodType)`

2. `accruedEstimatedPoolGold`
   - current open-period estimate
   - calculated from elapsed V35 blocks in the current period
   - `elapsedBlocksInPeriod * reservePerBlockGold`
   - capped at `canonicalFullPeriodPayoutGold`
   - unavailable while V35 is disabled

Canonical full-period payout amounts:

- daily: `302.40`
- weekly: `1209.60`
- monthly: `3888.00`
- quarterly: `9720.00`
- half-year: `15724.80`
- yearly: `23652.00`

The current public pages can keep showing canonical full-period payout amounts. The live wallet/dashboard API should label any partially accrued estimate clearly as estimated, not paid, not minted, and not final.

## 6. How Winner / History / Proof Is Calculated

### Winner proof

Use the existing pure V35 proof path:

- collect eligible tickets from period blocks with `V35JackpotChainContextAdapter.collectEligibleTickets(...)`
- collect seed hashes with `V35JackpotChainContextAdapter.collectFutureHashes(...)`
- compute deterministic seed and winner using `JackpotCalculator.selectWinner(...)`
- return `JackpotWinnerProof`

Rules:

- only V35 mined blocks are eligible
- each mined block is one ticket in the first implementation
- `minerAddress` is the only participant field
- transaction sender/customer addresses are never eligible
- seed hashes are block hashes from `periodEnd + 1` through `periodEnd + 60`
- winner exists only after the delayed hash range is available

### Paid history

Recommended scan:

1. Load canonical chain from `BlockService`.
2. Iterate blocks from V35 fork index onward.
3. Find `BASIS_ADDRESS` transactions with canonical jackpot payout amounts.
4. Infer period type from amount with `JackpotCalculator.classifyPayoutAmount(...)`.
5. Infer exact period instance from payout block index using `V35JackpotProofService.periodInstanceForPayoutBlock(...)`.
6. Validate transaction using `V35JackpotChainContextAdapter.verifyPayoutWithChainContext(...)`.
7. Include only proof-valid jackpot payouts in history.

Do not include amount-only matches as valid jackpot history.

### Payout status for a period

For a period status/proof response:

- not mature if current height is below `maturityBlock`
- mature if seed hashes exist through `periodEnd + 60`
- paid if a proof-valid payout transaction exists at the exact first payout block
- unpaid if mature and no proof-valid payout is found at exact slot
- missed if first payout block is already passed and no payout was made

The current mining design uses exact-slot payouts only. Missed payouts are not paid later in the first implementation.

## 7. How V35 Disabled State Is Reported

With current constants:

- `V35_RESCUE_FORK_INDEX = Long.MAX_VALUE`
- `V35_BURN_ADDRESS = BURN_ADDRESS`

All jackpot endpoints should report:

- `v35Active = false`
- `v35ForkIndexDisplay = "Long.MAX_VALUE"`
- `liveJackpotActive = false`
- policy/config values available
- current live pools unavailable or disabled
- no current live winner
- no current live ticket count
- no payout creation

Recommended disabled-state text field:

```text
V35 jackpot policy is configured but inactive. Live jackpot periods, tickets, winners, and payouts begin only after V35_RESCUE_FORK_INDEX is set and reached.
```

If any endpoint exposes history while disabled, it must state that entries are proof-valid historical data only. Under the current disabled fork index, no V35 payout history should validate as active.

## 8. Performance / Caching Notes

Main performance risks:

- full-chain scans for `/v35/jackpot/history`
- yearly ticket windows up to `157,680` blocks
- repeated proof validation for every canonical jackpot-looking payout
- duplicate marker scans
- returning full seed hashes or full ticket block index lists

Recommended first implementation:

- Keep `/v35/jackpot/periods` static and cheap.
- Keep `/v35/jackpot/status` focused on the six current period rows.
- Add pagination to `/v35/jackpot/history`.
- Add optional `includeBlockIndexes=false` default for `/v35/jackpot/tickets/{address}`.
- Add optional `includeSeedHashes=true/false` later if response size becomes a problem.
- Build response from canonical `BlockService` data without mutating state.
- Prefer bounded range scans:
  - current period scans only need that period window
  - proof scans need period window plus 60 future hashes
  - history scans should respect `limit`, `fromBlock`, and `toBlock`

Recommended later cache:

- cache jackpot derived status by `(currentHeight, currentBlockHash)`
- cache paid history markers by payout block hash
- invalidate cache when canonical chain height/hash changes

Do not introduce mutable cache in Phase 5G-1 unless it is needed to pass basic performance tests. A simple read-only builder is safer first.

## 9. Safe Implementation Order For Phase 5G-1

1. Create server-only `V35JackpotController`.
2. Create `V35JackpotApiResponseBuilder` or read-only service under `International_Trade_Union.protocol.v35`.
3. Add `/v35/jackpot/periods` first because it is static and low-risk.
4. Add `/v35/jackpot/status` using current canonical height/hash and disabled-state handling.
5. Add `/v35/jackpot/proof/{periodType}/{periodEnd}` using existing proof helpers.
6. Add `/v35/jackpot/history` with pagination and proof validation.
7. Add `/v35/jackpot/tickets/{address}` with miner-address-only counts and no sender logic.
8. Add server tests for disabled behavior, period policy, proof behavior, history filtering, and ticket counting.
9. Run server, wallet, and website test/build verification.
10. Update the Phase 5G-1 report.

## 10. Files Phase 5G-1 May Edit

Recommended server files:

- `unitedStates_storage-master/src/main/java/International_Trade_Union/controllers/V35JackpotController.java`
- `unitedStates_storage-master/src/main/java/International_Trade_Union/protocol/v35/V35JackpotApiResponseBuilder.java`
- Optional:
  - `unitedStates_storage-master/src/main/java/International_Trade_Union/protocol/v35/V35JackpotReadOnlyService.java`
  - small response DTO/value classes under `International_Trade_Union.protocol.v35`
- Server tests:
  - `unitedStates_storage-master/src/test/java/International_Trade_Union/protocol/v35/V35JackpotApiResponseBuilderTest.java`
  - `unitedStates_storage-master/src/test/java/International_Trade_Union/controllers/V35JackpotControllerTest.java`
- Report:
  - `CITU_V35_PHASE5G1_JACKPOT_READONLY_API_REPORT.md`

Avoid wallet/website edits in Phase 5G-1 unless a later request explicitly includes dashboard consumption.

## 11. Files Phase 5G-1 Must Not Edit

Unless a later phase explicitly changes scope:

- `Mining.java`
- `UtilsBlock.java`
- `UtilsBalance.java`
- `Blockchain.java`
- `UtilsResolving.java`
- `Block.java`
- `DtoTransaction.java`
- `Account.java`
- wallet UI/templates/controllers
- website templates/controllers
- transaction format
- block format
- validation behavior
- mining behavior
- balance behavior
- `pom.xml`
- `Seting.V35_RESCUE_FORK_INDEX`

## 12. Required Tests

Server tests:

1. `/v35/jackpot/periods` returns all six periods.
2. Period endpoint returns:
   - 432 daily blocks
   - 3024 weekly blocks
   - 12960 monthly blocks
   - 38880 quarterly blocks
   - 78624 half-year blocks
   - 157680 yearly blocks
3. Period endpoint returns canonical payout amounts:
   - `302.40`
   - `1209.60`
   - `3888.00`
   - `9720.00`
   - `15724.80`
   - `23652.00`
4. Status endpoint reports `v35Active=false` while fork index is `Long.MAX_VALUE`.
5. Status endpoint reports policy values while disabled but no live jackpot winner.
6. Status endpoint does not mutate chain, balances, settings, or transactions.
7. Proof endpoint returns disabled/invalid state while `Long.MAX_VALUE`.
8. Simulated helper proof returns deterministic winner for a mature daily period.
9. Immature proof returns no winner.
10. Invalid period alignment is rejected.
11. History endpoint does not include amount-only fake jackpot payouts.
12. History endpoint includes only proof-valid jackpot payout transactions in simulated V35 helper tests.
13. Tickets endpoint counts only `minerAddress`.
14. Tickets endpoint ignores sender/customer addresses.
15. Tickets endpoint handles blank/unknown address safely.
16. Pagination/limit on history is enforced.
17. JSON decimal fields are string-safe or precise BigDecimal values.
18. `V35_BURN_ADDRESS = BURN_ADDRESS` remains configured.
19. No burn transaction is created.
20. No mining payout is created by API requests.

If full controller tests are heavy:

- add response-builder tests with fixture chains
- add lightweight controller route tests using mocked `BlockService`
- document any DB/controller test blocker in the Phase 5G-1 report

Verification for Phase 5G-1 should run:

- server `.\mvnw.cmd test`
- server `.\mvnw.cmd clean package`
- wallet `.\mvnw.cmd test`
- wallet `.\mvnw.cmd clean package`
- website `.\mvnw.cmd test`
- website `.\mvnw.cmd clean package`

## 13. Risks

1. Full-chain scan cost:
   - history and ticket endpoints can become expensive on a large chain.
   - Mitigation: pagination, bounded ranges, and later cache by height/hash.
2. Amount-only false positives:
   - transaction format has no jackpot marker.
   - Mitigation: history must verify through `V35JackpotChainContextAdapter`, not classify by amount alone.
3. Disabled-state confusion:
   - dashboards may imply jackpots are live while fork index is `Long.MAX_VALUE`.
   - Mitigation: every response includes `v35Active=false` and disabled messaging.
4. Exact-slot policy:
   - missed payouts are not paid later in the current design.
   - Mitigation: expose exact `firstPayoutBlock` and missed/unpaid status clearly.
5. Sender eligibility confusion:
   - users may expect senders or transaction volume to affect jackpot chances.
   - Mitigation: every endpoint should report `winnerSource=minerAddress` and `senderEligibilityAllowed=false`.
6. Proof response size:
   - returning 60 hashes and many ticket block indexes can be noisy.
   - Mitigation: include seed hashes for proof, but keep ticket block indexes optional/paginated.
7. Chain reorgs:
   - current period status and winner proof can change before payout finality if canonical chain changes.
   - Mitigation: include `currentHeight`, `currentBlockHash`, payout block hash, and proof block ranges.
8. Cache invalidation:
   - future caching must key off canonical height/hash.
   - Mitigation: start uncached or minimally cached in Phase 5G-1.

## 14. Rollback Plan

If Phase 5G-1 causes issues:

1. Remove the new `V35JackpotController` and read-only response builder/service.
2. Remove only the new jackpot API tests.
3. Leave all existing jackpot protocol helpers, mining, validation, balance accounting, wallet dashboard, website pages, and supply endpoints unchanged.
4. Confirm `V35_RESCUE_FORK_INDEX` remains `Long.MAX_VALUE`.
5. Re-run server, wallet, and website tests/builds.

Because the planned endpoints are read-only and V35 remains disabled, rollback should not affect legacy chain behavior.

## 15. Recommendation

Proceed with Phase 5G-1 as a server-only read-only jackpot API implementation:

- implement `/v35/jackpot/periods` and `/v35/jackpot/status` first
- then add proof/history/tickets using the existing proof adapter
- keep wallet/website consumption for a later phase
- keep all responses explicit about disabled V35 state, actual paid history, policy estimates, and modeled/open-period estimates

The strongest safety rule for Phase 5G-1 is simple: expose derived jackpot data, but never create payouts, never mutate chain/account state, and never treat canonical jackpot amount alone as proof of a valid jackpot payout.
