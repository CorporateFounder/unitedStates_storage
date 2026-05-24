# CITU V35 Phase 6A Pre-Activation Hardening Plan

## 1. Exact Files And Classes Inspected

### Phase reports

All `CITU_V35_PHASE*.md` reports in `C:\citu` were listed and reviewed for phase continuity, test coverage, open risks, and recommended next phases.

The following reports were inspected in detail because they define the current pre-activation boundary:

- `CITU_V35_PHASE4E1G_JACKPOT_CONTROLLER_STORAGE_INTEGRATION_REPORT.md`
- `CITU_V35_PHASE5G2B_WALLET_JACKPOT_LIVE_API_REPORT.md`
- `CITU_V35_PHASE5G1_JACKPOT_READONLY_API_ENDPOINTS_REPORT.md`
- `CITU_V35_PHASE5D1_SENTINEL_BURN_ADDRESS_REPLACEMENT_REPORT.md`
- `CITU_V35_PHASE5E1_FOUNDER_BURN_SNAPSHOT_REPORT.md`

Additional phase report families reviewed:

- Phase 1 reports: V35 rescue fork gate and parity setup.
- Phase 2 reports: V35 reward wiring and reward fixture coverage.
- Phase 3 reports: PoW target, difficulty, cumulative work, admission, and tournament selection.
- Phase 4 reports: jackpot design, helper math, proof service, chain-context adapter, payout builder, mining wiring, validation, accounting, controller/storage tests.
- Phase 5 reports: founder burn policy, burn no-spend, supply helpers/API, sentinel burn address, founder burn snapshot, wallet/dashboard/site/API integration.

### Wallet project

Inspected wallet files/classes:

- `unitedStates_final-master/src/main/java/International_Trade_Union/setings/Seting.java`
- `unitedStates_final-master/src/main/java/International_Trade_Union/entity/blockchain/Blockchain.java`
- `unitedStates_final-master/src/main/java/International_Trade_Union/utils/UtilsBlock.java`
- `unitedStates_final-master/src/main/java/International_Trade_Union/utils/UtilsBalance.java`
- `unitedStates_final-master/src/main/java/International_Trade_Union/utils/UtilsResolving.java`
- `unitedStates_final-master/src/main/java/International_Trade_Union/controllers/BasisController.java`
- `unitedStates_final-master/src/main/java/International_Trade_Union/model/Mining.java`
- `unitedStates_final-master/src/main/java/International_Trade_Union/controllers/V35DashboardController.java`
- `unitedStates_final-master/src/main/java/International_Trade_Union/controllers/V35DashboardApiClient.java`
- `unitedStates_final-master/src/main/resources/templates/v35-dashboard.html`
- `unitedStates_final-master/src/main/java/International_Trade_Union/protocol/v35/BurnAddressPolicy.java`
- `unitedStates_final-master/src/main/java/International_Trade_Union/protocol/v35/JackpotCalculator.java`
- `unitedStates_final-master/src/main/java/International_Trade_Union/protocol/v35/JackpotPayoutCandidate.java`
- `unitedStates_final-master/src/main/java/International_Trade_Union/protocol/v35/JackpotPeriodInstance.java`
- `unitedStates_final-master/src/main/java/International_Trade_Union/protocol/v35/JackpotProofResult.java`
- `unitedStates_final-master/src/main/java/International_Trade_Union/protocol/v35/JackpotTicket.java`
- `unitedStates_final-master/src/main/java/International_Trade_Union/protocol/v35/JackpotWinnerProof.java`
- `unitedStates_final-master/src/main/java/International_Trade_Union/protocol/v35/ProtocolVersion.java`
- `unitedStates_final-master/src/main/java/International_Trade_Union/protocol/v35/V35JackpotChainContextAdapter.java`
- `unitedStates_final-master/src/main/java/International_Trade_Union/protocol/v35/V35JackpotPayoutBuilder.java`
- `unitedStates_final-master/src/main/java/International_Trade_Union/protocol/v35/V35JackpotProofService.java`
- `unitedStates_final-master/src/main/java/International_Trade_Union/protocol/v35/V35PowValidator.java`
- `unitedStates_final-master/src/main/java/International_Trade_Union/protocol/v35/V35RewardCalculator.java`
- `unitedStates_final-master/src/main/java/International_Trade_Union/protocol/v35/V35SupplyCalculator.java`
- `unitedStates_final-master/src/main/java/International_Trade_Union/protocol/v35/V35SupplyChainAdapter.java`
- `unitedStates_final-master/src/main/java/International_Trade_Union/protocol/v35/V35SupplySnapshot.java`
- `unitedStates_final-master/src/main/java/International_Trade_Union/protocol/v35/V35TargetDifficulty.java`
- `unitedStates_final-master/src/main/java/International_Trade_Union/protocol/v35/V35WorkCalculator.java`

Representative wallet tests inspected by name:

- `V35RewardCalculatorTest`
- `V35RewardValidationFixtureTest`
- `V35PowValidatorTest`
- `V35PowValidationGateTest`
- `V35TargetDifficultyTest`
- `V35WorkCalculatorTest`
- `JackpotCalculatorTest`
- `JackpotWinnerSelectionTest`
- `JackpotPayoutValidationTest`
- `V35JackpotProofServiceTest`
- `V35JackpotChainContextAdapterTest`
- `V35JackpotPayoutBuilderTest`
- `UtilsBlockV35JackpotChainContextTest`
- `UtilsBalanceV35JackpotAccountingTest`
- `UtilsResolvingV35JackpotWiringTest`
- `MiningV35JackpotPayoutWiringTest`
- `MiningV35JackpotPayoutE2ETest`
- `MiningV35JackpotControllerStorageIntegrationTest`
- `BurnAddressPolicyTest`
- `V35SupplyCalculatorTest`
- `V35SupplyChainAdapterTest`
- `V35DashboardApiClientTest`
- `V35DashboardControllerTest`
- `V35ParityTest`

### Server project

Inspected server files/classes:

- `unitedStates_storage-master/src/main/java/International_Trade_Union/setings/Seting.java`
- `unitedStates_storage-master/src/main/java/International_Trade_Union/entity/blockchain/Blockchain.java`
- `unitedStates_storage-master/src/main/java/International_Trade_Union/utils/UtilsBlock.java`
- `unitedStates_storage-master/src/main/java/International_Trade_Union/utils/UtilsBalance.java`
- `unitedStates_storage-master/src/main/java/International_Trade_Union/utils/UtilsResolving.java`
- `unitedStates_storage-master/src/main/java/International_Trade_Union/controllers/BasisController.java`
- `unitedStates_storage-master/src/main/java/International_Trade_Union/controllers/V35SupplyController.java`
- `unitedStates_storage-master/src/main/java/International_Trade_Union/controllers/V35JackpotController.java`
- `unitedStates_storage-master/src/main/java/International_Trade_Union/controllers/V35SupplyApiResponseBuilder.java`
- `unitedStates_storage-master/src/main/java/International_Trade_Union/controllers/V35JackpotApiResponseBuilder.java`
- `unitedStates_storage-master/src/main/java/International_Trade_Union/controllers/AllTransactions.java`
- `unitedStates_storage-master/src/main/java/International_Trade_Union/model/Mining.java`
- `unitedStates_storage-master/src/main/java/International_Trade_Union/service/TournamentService.java`
- `unitedStates_storage-master/src/main/java/International_Trade_Union/protocol/v35/BurnAddressPolicy.java`
- `unitedStates_storage-master/src/main/java/International_Trade_Union/protocol/v35/JackpotCalculator.java`
- `unitedStates_storage-master/src/main/java/International_Trade_Union/protocol/v35/JackpotPayoutCandidate.java`
- `unitedStates_storage-master/src/main/java/International_Trade_Union/protocol/v35/JackpotPeriodInstance.java`
- `unitedStates_storage-master/src/main/java/International_Trade_Union/protocol/v35/JackpotProofResult.java`
- `unitedStates_storage-master/src/main/java/International_Trade_Union/protocol/v35/JackpotTicket.java`
- `unitedStates_storage-master/src/main/java/International_Trade_Union/protocol/v35/JackpotWinnerProof.java`
- `unitedStates_storage-master/src/main/java/International_Trade_Union/protocol/v35/ProtocolVersion.java`
- `unitedStates_storage-master/src/main/java/International_Trade_Union/protocol/v35/V35JackpotChainContextAdapter.java`
- `unitedStates_storage-master/src/main/java/International_Trade_Union/protocol/v35/V35JackpotPayoutBuilder.java`
- `unitedStates_storage-master/src/main/java/International_Trade_Union/protocol/v35/V35JackpotProofService.java`
- `unitedStates_storage-master/src/main/java/International_Trade_Union/protocol/v35/V35PowValidator.java`
- `unitedStates_storage-master/src/main/java/International_Trade_Union/protocol/v35/V35RewardCalculator.java`
- `unitedStates_storage-master/src/main/java/International_Trade_Union/protocol/v35/V35SupplyCalculator.java`
- `unitedStates_storage-master/src/main/java/International_Trade_Union/protocol/v35/V35SupplyChainAdapter.java`
- `unitedStates_storage-master/src/main/java/International_Trade_Union/protocol/v35/V35SupplySnapshot.java`
- `unitedStates_storage-master/src/main/java/International_Trade_Union/protocol/v35/V35TargetDifficulty.java`
- `unitedStates_storage-master/src/main/java/International_Trade_Union/protocol/v35/V35WorkCalculator.java`

Representative server tests inspected by name:

- `V35SupplyControllerTest`
- `V35JackpotControllerTest`
- `BasisControllerResolveAdmissionTest`
- `TournamentServiceTest`
- Matching V35 reward, PoW, work, jackpot, burn, supply, mining, blockchain, and parity tests mirrored from wallet where applicable.

### Website project

Inspected website files/classes:

- `website.citu-web-master/src/main/java/com/example/website/citu/controller/V35PublicController.java`
- `website.citu-web-master/src/main/resources/templates/v35.html`
- `website.citu-web-master/src/main/resources/templates/v35-tokenomics.html`
- `website.citu-web-master/src/main/resources/templates/v35-burn.html`
- `website.citu-web-master/src/main/resources/templates/v35-jackpot.html`
- `website.citu-web-master/src/main/resources/templates/v35-mining.html`
- `website.citu-web-master/src/main/resources/templates/v35-faq.html`
- `website.citu-web-master/src/test/java/com/example/website/citu/controller/V35PublicControllerTest.java`

### Constants confirmed

Wallet:

- `V35_RESCUE_FORK_INDEX = Long.MAX_VALUE`
- `V35_BURN_ADDRESS = "BURN_ADDRESS"`
- `V35_GOLD_REWARD = 8.00`
- `V35_INSTANT_MINER_REWARD = 6.00`
- `V35_JACKPOT_REWARD = 2.00`
- `V35_EXPECTED_FOUNDER_REWARD = 0.00`

Server:

- `V35_RESCUE_FORK_INDEX = Long.MAX_VALUE`
- `V35_BURN_ADDRESS = "BURN_ADDRESS"`
- `V35_GOLD_REWARD = 8.00`
- `V35_INSTANT_MINER_REWARD = 6.00`
- `V35_JACKPOT_REWARD = 2.00`
- `V35_EXPECTED_FOUNDER_REWARD = 0.00`

## 2. Current V35 Readiness Summary

V35 core is ready for pre-activation hardening, not for activation.

Implemented and still disabled:

- V35 fork gate remains inactive because `V35_RESCUE_FORK_INDEX = Long.MAX_VALUE`.
- V35 burn address is configured as the sentinel `BURN_ADDRESS`.
- V35 reward model is implemented:
  - 200 seconds per block.
  - 432 blocks per day.
  - 8.00 GOLD total per block.
  - 6.00 GOLD instant miner reward.
  - 2.00 GOLD jackpot reserve.
  - 0.00 GOLD founder reward.
- V35 Bitcoin-like PoW target validation is implemented behind the fork gate.
- V35 automatic target difficulty and cumulative work selection are implemented behind the fork gate.
- Jackpot math, deterministic winner selection, proof service, chain-context adapter, payout builder, validation, mining wiring, accounting, and read-only APIs are implemented.
- `Mining.java` can create V35 jackpot payout transactions only after the V35 fork gate is active.
- Context-aware jackpot validation and accounting paths exist.
- `UtilsResolving` has been wired to avoid the known no-context jackpot validation/accounting bypass.
- Burn-address no-spend policy is implemented behind the V35 fork gate.
- Read-only supply, burn, founder, founder burn snapshot, jackpot, wallet dashboard, and website V35 pages exist.
- No burn transaction has been created.
- Founder burn has not happened.
- V35 remains inactive in live behavior.

Activation should wait until the hardening phases below prove the live node/controller/database paths, old-chain compatibility, and a simulated fork run.

## 3. What Is Already Covered By Tests

Existing phase reports and test files indicate coverage in these areas:

### V35 disabled and parity

- Wallet/server parity tests for V35 constants and helper behavior.
- `V35_RESCUE_FORK_INDEX = Long.MAX_VALUE` coverage.
- `BURN_ADDRESS` configured in wallet/server after sentinel replacement.
- Legacy behavior remains active while V35 is disabled.

### Reward model

- V35 8.00 GOLD total reward.
- 6.00 GOLD miner reward.
- 2.00 GOLD jackpot reserve.
- 0.00 GOLD founder reward.
- Founder reward rejection after V35.
- Reward fixture tests.

### PoW and chain selection

- V35 target encoding/decoding.
- V35 hash-under-target validation.
- V35 validation gate.
- V35 target difficulty rules.
- V35 cumulative work helper behavior.
- Metadata and data-short blockchain information behavior.

### Jackpot helper/proof/accounting path

- Jackpot payout amount classification.
- Jackpot amount uniqueness.
- Jackpot winner selection.
- 60-block delayed future hash behavior.
- Jackpot proof service validation.
- Jackpot chain-context adapter.
- Jackpot payout builder.
- Context-aware `UtilsBlock` validation helper.
- `UtilsBalance` jackpot accounting path.
- `UtilsResolving` helper-level wiring.
- Mining jackpot payout builder wiring.
- End-to-end helper/unit payout block coverage.
- Controller/storage-style JSON save/reload round trip.
- Later spend by jackpot winner after balance fix.

### Burn and supply

- Burn-address policy.
- Sending TO `BURN_ADDRESS` remains allowed by policy.
- Spending FROM `BURN_ADDRESS` is rejected after active V35 fork gate.
- Supply calculator and supply chain/account adapter.
- Server read-only V35 supply/protocol/burn/founder endpoints.
- Founder burn snapshot endpoint with 75/80/85/90 options and 80% recommendation.

### APIs, wallet dashboard, and website

- Server V35 supply endpoints.
- Server V35 jackpot endpoints.
- Wallet V35 dashboard static and live read-only supply/founder/burn integration.
- Wallet jackpot live API integration for status, periods, and history.
- Website static V35 overview/tokenomics/burn/jackpot/mining/FAQ pages.

## 4. What Is Not Yet Covered

The following areas remain insufficiently proven for activation:

1. Real controller and database-backed block admission

   Existing tests cover helper/unit and storage-style flows, but not a full running node scenario with real controller calls, `BlockService`, persistence, reload, conflict resolution, tournament candidate validation, and balance recalculation under one integration run.

2. Full old-chain validation against the actual historical chain

   V35 must prove that all old blocks, transactions, balances, hashes, and legacy difficulty behavior remain valid before the fork. This needs a full chain validation pass against real chain data, not only fixtures.

3. Multi-node simulated V35 dry-run

   A local/dev run must cross a simulated fork index, mine V35 blocks, create jackpot payouts at maturity, resolve competing chains by cumulative work, and verify APIs and dashboards against live node behavior.

4. Transaction-pool active-fork burn sentinel path

   Reports indicate `AllTransactions` was inspected and policy paths exist, but active-fork simulation was limited by the global disabled fork. A dedicated hardening test should prove the transaction pool rejects `BURN_ADDRESS` as sender before risky sender decode after a simulated active fork.

5. Real controller HTTP and database persistence coverage for jackpot payout blocks

   Phase 4E-1G proved a targeted storage-style path, but real HTTP receive/save/reload/resolve paths remain the most important unknown before mining-created jackpot payouts can be trusted on live nodes.

6. Endpoint performance and pagination

   Read-only supply and jackpot endpoints can scan canonical chain data. Current behavior is acceptable for pre-activation, but history, tickets, proof, and supply endpoints need performance limits or caching before public scale.

7. Browser-level UI verification

   Wallet and website route/template tests exist, but a browser-level visual verification pass should confirm pages render cleanly, disabled warnings are visible, and fallback states are usable.

8. Manual founder burn execution

   The burn snapshot helper exists, but the actual founder burn is still pending and must wait until the hardening plan passes.

## 5. Live Controller/Database Hardening Plan

Recommended next implementation phase: **Phase 6B - live controller/database hardening tests**.

### Scope

Phase 6B should prove that a real server/wallet node can accept, save, reload, resolve, validate, account, and expose V35-era blocks without bypassing the context-aware V35 paths.

### Paths to test

1. Block receive and admission

   Exercise controller endpoints that receive or import blocks and verify they route to context-aware validation where canonical context exists.

2. Block save and reload

   Use the same storage service paths used by running nodes. Save a chain containing V35 jackpot payout candidates, reload it, and revalidate balances.

3. Conflict resolution

   Exercise `BasisController.resolve_conflict(...)`, `UtilsResolving.resolve3(...)`, `UtilsResolving.helpResolve(...)`, `UtilsResolving.addBlock3(...)`, and rollback/replacement paths.

4. Tournament winner flow

   Exercise `TournamentService` candidate validation and winner selection paths that call resolving or short-check logic.

5. Balance recalculation

   Verify valid jackpot payouts credit only the winner and invalid jackpot payouts are rejected or not credited through live recalculation paths.

6. Transaction pool

   Verify `AllTransactions` and `UtilsUse` reject spending FROM `BURN_ADDRESS` after an active V35 test gate before `DtoTransaction.verify()` or public-key decode.

7. API endpoints

   Verify read-only endpoints remain non-mutating and stable during live controller/database operations:

   - `/v35/supply`
   - `/v35/protocol`
   - `/v35/burn`
   - `/v35/founder`
   - `/v35/founder/burn-snapshot`
   - `/v35/jackpot/status`
   - `/v35/jackpot/periods`
   - `/v35/jackpot/history`
   - `/v35/jackpot/tickets/{address}`
   - `/v35/jackpot/proof/{periodType}/{periodEnd}`

8. Wallet dashboard integration

   Run wallet dashboard against a local server exposing the V35 endpoints and verify fallback behavior when endpoints are unavailable.

### Test strategy

- Use an isolated test database or temporary storage directory.
- Load or create a canonical pre-V35 chain.
- Use a simulated fork index in test-only calls, not `Seting.V35_RESCUE_FORK_INDEX`.
- Build a small canonical V35 chain with:
  - pre-fork legacy blocks,
  - first V35 block,
  - eligible mined V35 blocks,
  - 60 delayed future hash blocks,
  - jackpot payout block,
  - later spend by the jackpot winner.
- Exercise controller and service paths that a real node uses.
- Assert that no source behavior changes when `V35_RESCUE_FORK_INDEX = Long.MAX_VALUE`.

### Required assertions

- Valid jackpot payout block is accepted only through context-aware validation.
- Ordinary no-context validation remains conservative.
- Wrong winner, immature payout, duplicate payout, founder recipient, non-canonical payout, and sender-based eligibility are rejected.
- Jackpot payout balance survives save/reload.
- Jackpot winner can spend payout later.
- `BURN_ADDRESS` cannot spend after active V35 test gate.
- Existing old non-jackpot paths remain unchanged.

## 6. Old-Chain Validation Plan

Recommended phase: **Phase 6C - full old-chain validation tests**.

### Goal

Prove that the V35 codebase validates the existing chain exactly as legacy before any real fork index is set.

### Required proof

1. All old blocks validate under legacy rules.

   Run full historical chain validation using the same file/database paths used by running nodes.

2. Old balances remain unchanged.

   Capture account balances before the validation pass, run the updated code, and compare resulting balances exactly.

3. Old transactions remain valid.

   No historical transaction should fail because of:

   - V35 burn-address policy,
   - V35 reward rules,
   - V35 jackpot classification,
   - V35 PoW target rules,
   - cumulative work metadata,
   - founder reward removal.

4. Old difficulty/hash behavior remains legacy.

   Blocks before fork must continue to use old validation rules, not V35 target PoW.

5. Old BASIS reward validation remains legacy.

   Historical founder/miner reward behavior must remain valid before fork.

6. No accidental `BURN_ADDRESS` effect before V35.

   The sentinel no-spend rule must be inactive before fork and must not invalidate old data.

### Suggested execution

- Snapshot current chain storage.
- Run `Blockchain.checkFromFile(...)` on actual chain files.
- Run controller/database reload path on actual or copied chain storage.
- Run `Blockchain.shortCheck(...)` on representative old block ranges.
- Compare:
  - chain height,
  - latest hash,
  - account balances,
  - data-short summaries,
  - total supply helpers,
  - known founder balance,
  - transaction counts.

### Required outputs

- Exact chain height tested.
- Latest block hash tested.
- Balance checksum before and after.
- Count of validated blocks.
- Count of rejected blocks, expected zero.
- Any legacy anomaly documented before activation.

## 7. V35 Dry-Run Plan

Recommended phase: **Phase 6D - simulated V35 fork dry-run**.

### Goal

Run a local/dev chain through a simulated V35 fork index and verify the complete post-fork behavior without activating production.

### Dry-run setup

- Use local isolated wallet/server nodes.
- Use a test-only fork index such as `N`, passed through test helpers or local non-release config.
- Mine or construct:
  - pre-V35 legacy blocks,
  - crossing block at the V35 fork,
  - multiple V35 blocks,
  - enough period and future hash blocks for jackpot maturity,
  - jackpot payout block,
  - later spend by jackpot winner,
  - competing branch with higher cumulative work.

### Required dry-run checks

1. Fork gate

   - Pre-fork blocks remain legacy.
   - V35 rules begin only at simulated fork index.

2. Reward behavior

   - Miner instant reward is exactly 6.00 GOLD.
   - Founder reward is not created.
   - Jackpot reserve is represented only through payout transactions when mature.

3. PoW and difficulty

   - V35 block hash must be under target.
   - Target metadata is valid.
   - Difficulty adjusts with the 200-second block target.
   - Emergency difficulty easing behaves as designed.

4. Cumulative work

   - Chain selection chooses the higher-work chain.
   - Height-only selection does not override cumulative work.

5. Jackpot

   - Every V35 mined block becomes a minerAddress ticket.
   - Sender addresses never become participants.
   - Future hash delay of 60 blocks is enforced.
   - Exact-slot payout behavior is enforced.
   - Daily/weekly/monthly/etc. payouts can mature together.
   - Wrong winner, immature, duplicate, founder, and non-canonical payouts are rejected.

6. Mining

   - `Mining.java` calls `V35JackpotPayoutBuilder` only after V35.
   - Mining skips payout if proof cannot be built.
   - Mining does not create founder reward.

7. Validation/accounting/resolving

   - Context-aware `UtilsBlock` validation accepts valid payout blocks.
   - `Blockchain.shortCheckWithCanonicalContext(...)` accepts valid payout blocks.
   - `UtilsResolving` paths do not bypass jackpot validation.
   - `UtilsBalance` credits valid jackpot payout and ignores invalid candidates.

8. Burn no-spend

   - Sending TO `BURN_ADDRESS` remains possible.
   - Spending FROM `BURN_ADDRESS` is rejected after V35.

9. API/UI

   - V35 supply endpoints reflect dry-run data.
   - V35 jackpot endpoints report status/history/proof safely.
   - Wallet dashboard consumes live endpoints.
   - Website pages render and do not claim production activation.

## 8. Burn Readiness Plan

Manual founder burn must wait until Phase 6B, 6C, 6D, and release/rollback readiness pass.

### Burn prerequisites

- V35 hardening tests pass.
- Full old-chain validation passes.
- Simulated fork dry-run passes.
- Release and rollback checklist is approved.
- Founder address is frozen operationally between snapshot and burn transaction.
- `BURN_ADDRESS` remains configured in wallet/server.
- No-spend policy remains behind V35 and tested.

### Snapshot procedure

Use `/v35/founder/burn-snapshot` on the authoritative node.

Record:

- founder address,
- `BURN_ADDRESS`,
- snapshot height,
- snapshot block hash,
- founder balance before burn,
- 75/80/85/90 modeled options,
- recommended 80% burn amount,
- actual burned supply before burn,
- circulating supply before burn,
- annual emission,
- modeled post-burn circulating supply,
- modeled post-burn inflation.

### Recommended burn amount

Use 80% of the founder GOLD balance at the recorded snapshot height.

Avoid using a moving live balance unless the burn transaction is created immediately after snapshot and the proof package documents the exact height and balance source.

### Burn procedure

1. Freeze founder spending.
2. Generate final snapshot.
3. Calculate exact 80% burn using canonical 2-decimal GOLD precision.
4. Create normal founder-signed transaction:

   `founderAddress -> BURN_ADDRESS`

5. Wait for confirmation.
6. Publish proof package.
7. Verify `/v35/supply`, `/v35/burn`, and `/v35/founder/burn-snapshot` after the burn.

### Public proof package

Publish:

- founder address,
- burn address,
- snapshot height,
- snapshot block hash,
- founder balance before burn,
- burn amount,
- burn transaction hash,
- burn block index,
- burn block hash,
- founder balance after burn,
- burned supply,
- circulating supply,
- annual emission,
- post-burn inflation,
- statement that burned coins remain historically minted but are excluded from circulating supply.

## 9. API/UI Readiness Plan

### Existing read-only endpoints

Supply/founder/burn:

- `/v35/supply`
- `/v35/protocol`
- `/v35/burn`
- `/v35/founder`
- `/v35/founder/burn-snapshot`

Jackpot:

- `/v35/jackpot/status`
- `/v35/jackpot/periods`
- `/v35/jackpot/history`
- `/v35/jackpot/tickets/{address}`
- `/v35/jackpot/proof/{periodType}/{periodEnd}`

### Required readiness checks

- Endpoints are read-only and non-mutating.
- Disabled state is explicit while `V35_RESCUE_FORK_INDEX = Long.MAX_VALUE`.
- Actual supply and modeled burn values are clearly separated.
- Burn transaction is not claimed to exist until chain data proves it.
- Jackpot history is not faked.
- Sender addresses are never shown as jackpot participants.
- Decimal values are string-safe or precision-safe.
- Endpoint failure does not crash wallet dashboard.
- Static policy fallback remains visible.

### Wallet dashboard

Verify:

- `/v35-dashboard` renders with live endpoints available.
- `/v35-dashboard` renders with live endpoints unavailable.
- V35 disabled warning remains visible.
- `BURN_ADDRESS` is visible.
- Actual vs modeled supply labels are visible.
- Jackpot static policy and live data are separated.
- Jackpot history empty state is honest.

### Website

Current website pages are static informational pages. They should be verified visually and tested for content accuracy.

One known follow-up risk: `v35-jackpot.html` was originally written before read-only jackpot endpoints existed and should be reviewed during docs cleanup so it does not imply that no jackpot read-only endpoint exists if the page text is now stale.

## 10. Performance And Caching Risks

### Full-chain scans

Risk:

- Supply snapshots, jackpot history, ticket counts, and proof endpoints may scan canonical chain data.

Mitigation:

- Cache read-only API snapshots by latest block height/hash.
- Add pagination or limits for jackpot history.
- Avoid exposing unbounded eligible block indexes by default.
- Prefer summary counts unless detailed proof is requested.

### Dashboard endpoint fan-out

Risk:

- Wallet dashboard can call several endpoints per page render.

Mitigation:

- Keep short timeouts.
- Cache successful response groups briefly.
- Show static policy plus unavailable live fields on failure.
- Consider a future aggregate `/v35/dashboard` endpoint if endpoint fan-out becomes expensive.

### Jackpot proof and ticket endpoints

Risk:

- `/v35/jackpot/tickets/{address}` and `/v35/jackpot/proof/{periodType}/{periodEnd}` can become expensive on long chains.

Mitigation:

- Require explicit period parameters for detailed proof.
- Limit ticket details to current periods by default.
- Cache paid history and period summaries.

### Old-chain validation runtime

Risk:

- Full old-chain validation may be slow on production data.

Mitigation:

- Run on copied storage first.
- Produce resumable logs/checkpoints.
- Record validation height/hash/checksum.

### Error logging

Risk:

- Prior wallet live API work noted noisy error-file logging when remote endpoints are unavailable.

Mitigation:

- Verify dashboard fallback does not flood logs.
- Add log throttling only in a later implementation phase if needed.

## 11. Release Checklist

Do not set a real V35 fork index until every checklist item passes.

### Pre-release code checklist

- Wallet tests pass.
- Server tests pass.
- Website tests pass.
- Wallet clean package passes.
- Server clean package passes.
- Website clean package passes.
- No accidental changes to transaction format.
- No accidental changes to block format.
- No accidental changes to hash format.
- `V35_RESCUE_FORK_INDEX` remains `Long.MAX_VALUE` until the final fork-index phase.
- `V35_BURN_ADDRESS` remains `BURN_ADDRESS`.

### Chain safety checklist

- Full old-chain validation passes.
- Old balances are unchanged.
- Old transactions remain valid.
- Old block hashes remain valid.
- Legacy difficulty behavior remains unchanged before V35.
- Legacy reward behavior remains unchanged before V35.

### V35 dry-run checklist

- Simulated fork crosses cleanly.
- V35 block mining succeeds.
- V35 target PoW validation succeeds.
- V35 cumulative work selection succeeds.
- Miner reward is 6.00 GOLD.
- Founder reward is not created.
- Jackpot payout generation/validation/accounting succeeds.
- Burn-address no-spend succeeds.
- Supply and jackpot APIs report expected data.
- Wallet dashboard handles live and fallback states.
- Website pages render.

### Burn checklist

- Founder snapshot endpoint verified.
- Founder address frozen for burn.
- 80% burn amount confirmed.
- Burn transaction not created until hardening passes.
- Public proof package prepared.

### Deployment checklist

- Release binaries built from a clean revision.
- Node operators receive upgrade instructions.
- Public docs explain disabled vs active state.
- Monitoring plan ready.
- Rollback plan ready.

## 12. Rollback Plan

### Before fork index is set

Rollback is straightforward:

- Keep `V35_RESCUE_FORK_INDEX = Long.MAX_VALUE`.
- Revert or redeploy previous binaries if a hardening issue is found.
- Do not create founder burn transaction.
- Do not publish activation date.

### After release but before fork height

If a problem is discovered before the chain reaches the fork height:

- Publish urgent operator notice.
- Ship a patch that restores `Long.MAX_VALUE` or moves activation to a later fork plan.
- Re-run old-chain validation.
- Re-run dry-run.

### After fork height

This is high risk and should be avoided through hardening.

If a severe post-fork issue occurs:

- Stop additional activation changes.
- Preserve node logs and block data.
- Identify whether the issue is validation, mining, accounting, resolving, API-only, or UI-only.
- If consensus is affected, coordinate a new emergency fork/recovery plan rather than ad hoc local rollback.
- If API/UI only, patch read-only layer without changing consensus.

### Burn rollback

The founder burn is intentionally irreversible. Do not execute it until all pre-activation hardening and dry-run steps pass.

## 13. Post-Activation Monitoring Plan

Monitor these items from the first V35 block onward:

### Consensus and mining

- First V35 block accepted by wallet and server.
- Block time trend around 200 seconds.
- Compact target metadata.
- Hash-under-target validation.
- Difficulty adjustment behavior.
- Emergency difficulty easing behavior.
- Cumulative work values and selected chain.

### Rewards

- Miner instant reward exactly 6.00 GOLD.
- No founder reward transaction.
- No non-canonical BASIS payouts.
- Jackpot payout transactions only when mature and proof-valid.

### Jackpot

- Eligible tickets are minerAddress-only.
- Future hash delay is 60 blocks.
- Daily/weekly/monthly/quarterly/half-year/yearly periods mature correctly.
- Duplicate payout attempts are rejected.
- Paid history appears in read-only API after valid payout.

### Burn and supply

- `BURN_ADDRESS` cannot spend after V35.
- Burned supply matches burn address balance.
- Circulating supply equals total minted minus burned.
- Founder reserve reflects post-burn balance.
- Supply APIs do not mix modeled and actual values.

### Node health

- Controller block receive/save/reload paths.
- Conflict resolution.
- Tournament winner selection.
- Balance recalculation.
- Transaction pool rejects invalid burn/jackpot transactions.
- API latency and error rates.
- Wallet dashboard fallback behavior.

## 14. Exact Safe Implementation Order For Phase 6B Onward

1. Phase 6B - live controller/database hardening tests

   Add integration tests for controller receive/save/reload/resolve/tournament/transaction-pool/API/dashboard paths using isolated storage and simulated V35 fork context. Apply only minimal fixes if tests prove bugs.

2. Phase 6C - full old-chain validation tests

   Validate actual existing chain data and prove old blocks, transactions, balances, and legacy rules remain unchanged.

3. Phase 6D - simulated V35 fork dry-run

   Run local/dev nodes across a simulated fork. Verify mining, PoW, cumulative work, jackpot, burn no-spend, validation, accounting, APIs, wallet dashboard, and website behavior.

4. Phase 6E - release/rollback checklist and documentation cleanup

   Finalize operator checklist, rollback checklist, public docs, stale website text, endpoint performance notes, and monitoring checklist.

5. Founder burn readiness checkpoint

   Use the founder burn snapshot endpoint, freeze founder spending, calculate 80%, prepare the burn transaction and proof package. Do not execute until hardening and release checklist pass.

6. Phase 6F - real fork index plan

   Choose the real fork index only after hardening, old-chain validation, dry-run, and burn readiness are approved. This phase should still be planning-only unless explicitly authorized to set the fork index.

7. Manual founder burn execution

   Execute the founder burn as a normal signed transaction only after the project owner approves the final operational window.

8. Final activation/release

   Set and release the final fork-index build only after all previous steps pass and public proof/checklists are prepared.

## 15. Highest Risks

1. Real controller/database path gap

   Helper and targeted integration tests are strong, but full live node receive/save/reload/resolve flows still need proof.

2. Old-chain compatibility

   V35 touches validation, rewards, PoW, chain selection, balances, burn policy, and APIs. Full historical validation is mandatory before activation.

3. Simulated fork complexity

   The first complete dry-run must prove many systems at once: mining, jackpot, PoW, cumulative work, validation, accounting, resolving, APIs, and dashboards.

4. Performance of read-only APIs

   Jackpot history/tickets/proof and supply snapshots may require chain scans. Without caching or pagination, public use could become expensive.

5. Burn timing and irreversibility

   The founder burn is irreversible. It must wait until hardening is complete and the snapshot/burn/proof process is operationally frozen.

6. Stale public wording

   Website/static documentation must be cleaned up before release so it does not contradict newer read-only jackpot APIs or imply activation/burn has already happened.

7. Global fork constant in tests

   Some active-fork behavior is difficult to exercise where code reads `Seting.V35_RESCUE_FORK_INDEX` directly. Hardening tests may need narrow test seams or explicit context helpers, but production behavior must remain unchanged while V35 is disabled.

## Recommendation

Proceed to **Phase 6B - live controller/database hardening tests**.

Do not set a real fork index and do not execute the founder burn yet.
