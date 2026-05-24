# CITU V35 Phase 6D-1 Simulated Fork Dry-Run Report

## Scope

Phase 6D-1 added deterministic simulated V35 fork dry-run tests in wallet and server. The tests use an explicit test fork index (`6000`) passed into existing V35-aware helpers and validation entrypoints. Production constants were not changed, no burn transaction was created, and no manual jackpot payout transaction was created outside test fixtures.

## 1. Exact Files / Classes Inspected

Reports:

- `CITU_V35_PHASE6A_PRE_ACTIVATION_HARDENING_PLAN.md`
- `CITU_V35_PHASE6B1_LIVE_CONTROLLER_DATABASE_HARDENING_REPORT.md`
- `CITU_V35_PHASE6C1_OLD_CHAIN_VALIDATION_BASELINE_REPORT.md`
- `CITU_V35_PHASE6C2_HISTORICAL_CHAIN_VALIDATION_REPORT.md`
- V35 reward, PoW, difficulty, cumulativeWork, jackpot, burn, supply, API, wallet dashboard, and website phase reports through Phase 5G-2B.

Wallet source and tests:

- `unitedStates_final-master/src/main/java/International_Trade_Union/entity/blockchain/Blockchain.java`
- `unitedStates_final-master/src/main/java/International_Trade_Union/utils/UtilsBlock.java`
- `unitedStates_final-master/src/main/java/International_Trade_Union/utils/UtilsBalance.java`
- `unitedStates_final-master/src/main/java/International_Trade_Union/utils/UtilsResolving.java`
- `unitedStates_final-master/src/main/java/International_Trade_Union/model/Mining.java`
- `unitedStates_final-master/src/main/java/International_Trade_Union/setings/Seting.java`
- `unitedStates_final-master/src/main/java/International_Trade_Union/controllers/BasisController.java`
- `unitedStates_final-master/src/main/java/International_Trade_Union/protocol/v35/*`
- `unitedStates_final-master/src/test/java/International_Trade_Union/model/MiningV35JackpotControllerStorageIntegrationTest.java`
- wallet V35 helper/API/dashboard tests.

Server source and tests:

- `unitedStates_storage-master/src/main/java/International_Trade_Union/entity/blockchain/Blockchain.java`
- `unitedStates_storage-master/src/main/java/International_Trade_Union/utils/UtilsBlock.java`
- `unitedStates_storage-master/src/main/java/International_Trade_Union/utils/UtilsBalance.java`
- `unitedStates_storage-master/src/main/java/International_Trade_Union/utils/UtilsResolving.java`
- `unitedStates_storage-master/src/main/java/International_Trade_Union/model/Mining.java`
- `unitedStates_storage-master/src/main/java/International_Trade_Union/setings/Seting.java`
- `unitedStates_storage-master/src/main/java/International_Trade_Union/controllers/BasisController.java`
- `unitedStates_storage-master/src/main/java/International_Trade_Union/entity/services/BlockService.java`
- `unitedStates_storage-master/src/main/java/International_Trade_Union/service/TournamentService.java`
- `unitedStates_storage-master/src/main/java/International_Trade_Union/network/AllTransactions.java`
- `unitedStates_storage-master/src/main/java/International_Trade_Union/protocol/v35/*`
- `unitedStates_storage-master/src/test/java/International_Trade_Union/model/Phase6B1LiveControllerDatabaseHardeningTest.java`
- server V35 API, transaction pool, tournament, old-chain, historical replay, and protocol tests.

## 2. Exact Files Changed

Added test files:

- `unitedStates_final-master/src/test/java/International_Trade_Union/model/SimulatedV35ForkDryRunTest.java`
- `unitedStates_storage-master/src/test/java/International_Trade_Union/model/SimulatedV35ForkDryRunTest.java`

Added report:

- `CITU_V35_PHASE6D1_SIMULATED_FORK_DRY_RUN_REPORT.md`

No production source files were changed.

## 3. Tests Added

Both wallet and server now include mirrored `SimulatedV35ForkDryRunTest` coverage:

- `simulatedForkGateDoesNotMutateProductionActivationConstants`
- `preForkLegacyBlockStillUsesLegacyRewardAndDifficultyRules`
- `forkBlockUsesV35RewardAndTargetValidation`
- `powDifficultyAndCumulativeWorkHelpersMatchV35DryRunPolicy`
- `jackpotPayoutBuilderValidationAndAccountingWorkAfterSimulatedFork`
- `burnAddressNoSpendIsForkGatedAndRecipientRemainsAllowed`

## 4. Simulated Fork Index Model

The dry-run uses `private static final long FORK_INDEX = 6000L` inside the new tests only.

The tests do not modify `Seting.V35_RESCUE_FORK_INDEX`. Instead, they pass the simulated fork index into explicit V35-aware helpers:

- `V35JackpotProofService.isV35ActiveAt(index, forkIndex)`
- `BurnAddressPolicy.isNoSpendActive(index, forkIndex)`
- `BurnAddressPolicy.isSpendForbidden(index, sender, forkIndex)`
- `UtilsBlock.validationOneBlockWithCanonicalContext(..., forkIndex)`
- `UtilsBlock.validateV35JackpotPayoutsWithCanonicalContext(..., forkIndex)`
- `Mining.buildV35JackpotPayoutTransactionsForMining(..., forkIndex)`
- `Blockchain.shortCheckWithCanonicalContext(..., forkIndex)`

The tests also assert that the real production helper `ProtocolVersion.isV35(FORK_INDEX)` remains false because `Seting.V35_RESCUE_FORK_INDEX` is still `Long.MAX_VALUE`.

## 5. Pre-Fork Behavior Proven

Before simulated fork index:

- `V35JackpotProofService.isV35ActiveAt(FORK_INDEX - 1, FORK_INDEX)` is false.
- `BurnAddressPolicy.isNoSpendActive(FORK_INDEX - 1, FORK_INDEX)` is false.
- A deterministic legacy-style block at `FORK_INDEX - 1` validates through `UtilsBlock.validationOneBlockWithCanonicalContext(...)` using legacy reward and legacy difficulty behavior.
- The V35 6/2/0 reward model is not required for the pre-fork block.
- Burn no-spend policy is not applied before the simulated fork.

## 6. Post-Fork V35 Behavior Proven

At and after simulated fork index:

- `V35JackpotProofService.isV35ActiveAt(FORK_INDEX, FORK_INDEX)` is true.
- `BurnAddressPolicy.isNoSpendActive(FORK_INDEX, FORK_INDEX)` is true.
- A deterministic post-fork V35 block with exactly one 6.00 GOLD miner reward and no founder reward validates.
- A post-fork founder reward transaction is rejected.
- A post-fork wrong miner reward amount is rejected.
- A post-fork block with invalid V35 target PoW is rejected.
- The V35 validation path is selected through the context-aware block validation overload.

## 7. PoW Dry-Run Results

The tests prove:

- V35 target block time remains `200` seconds.
- Initial compact target decodes to a valid target.
- A hash at or below target is accepted by `V35PowValidator`.
- A high hash against a tiny target is rejected by `V35PowValidator`.
- `UtilsBlock.validationOneBlockWithCanonicalContext(...)` accepts mined V35 test blocks and rejects an explicitly invalid high-hash block after simulated fork.

## 8. cumulativeWork Dry-Run Results

The tests prove:

- `V35WorkCalculator.workFromCompactTarget(...)` assigns more work to the harder target.
- `ChainTournamentScorer.betterOf(...)` prefers the higher cumulativeWork chain score over a weaker-work chain score even when the weaker score has a higher capped tie-breaker.

Server-side tournament production wiring remains covered by the existing `Phase6B1LiveControllerDatabaseHardeningTest` and `TournamentServiceTest`; this phase did not change tournament code.

## 9. Difficulty Dry-Run Results

The tests prove:

- `V35TargetDifficulty.getTargetBlockTimeSeconds()` remains `200`.
- `V35TargetDifficulty.calculateNextCompactTarget(...)` leaves the target unchanged for an on-time block interval before emergency delay.
- Existing V35 difficulty tests continue to cover compact target round trips, clamp behavior, target windows, and emergency easing.

Remaining gap: a long deterministic post-fork adjustment-window chain was not added in this phase. The focused helper-level dry-run covers the fork-gated target policy without creating a large fixture.

## 10. Jackpot Dry-Run Results

The tests prove after simulated fork:

- `Mining.buildV35JackpotPayoutTransactionsForMining(..., forkIndex)` creates a deterministic daily jackpot payout when the exact-slot proof is mature.
- The payout recipient is the deterministic winner `minerAddress`.
- The payout amount matches the canonical daily amount.
- `UtilsBlock.validateV35JackpotPayoutsWithCanonicalContext(...)` accepts the valid payout.
- A wrong winner payout is rejected.
- A duplicate same-period context is rejected.
- `Blockchain.shortCheckWithCanonicalContext(...)` validates the payout block and credits the winner balance through the proof-aware accounting path.

The test fixture uses synthetic chain context and test-created payout transactions only; it does not create any real/manual jackpot payout transaction.

## 11. Burn Address Dry-Run Results

The tests prove:

- `BURN_ADDRESS` is the configured burn address.
- Spending from `BURN_ADDRESS` is not forbidden before simulated fork.
- Spending from `BURN_ADDRESS` is forbidden at simulated fork.
- Ordinary senders are not blocked by burn no-spend policy.
- Sending to `BURN_ADDRESS` remains represented as normal recipient/account usage; no recipient-side public-key decode is required by the burn policy.

Server transaction-pool pre-verify no-spend behavior remains covered by `AllTransactionsV35BurnHardeningTest`.

## 12. API/UI Dry-Run Findings

No API, wallet dashboard, or website files were changed.

The V35 API and UI layers still read the real production constants. Since the real fork index remains `Long.MAX_VALUE`, they correctly continue to report V35 as disabled. This phase did not introduce a simulated fork injection into API/UI endpoints, by design.

Existing tests still cover:

- Server V35 supply endpoints disabled/configured state.
- Server V35 jackpot endpoints disabled/status behavior.
- Wallet dashboard fallback/live API rendering.
- Website static V35 pages rendering.

## 13. Production Code Changed

No production code was changed.

No minimal production fix was required. The dry-run tests passed using existing fork-gated helpers and context-aware validation/accounting paths.

## 14. Constant / Transaction Confirmations

Confirmed:

- Wallet `Seting.V35_RESCUE_FORK_INDEX = Long.MAX_VALUE`
- Server `Seting.V35_RESCUE_FORK_INDEX = Long.MAX_VALUE`
- Wallet `Seting.V35_BURN_ADDRESS = "BURN_ADDRESS"`
- Server `Seting.V35_BURN_ADDRESS = "BURN_ADDRESS"`
- No real founder burn transaction was created.
- No real/manual jackpot payout transaction was created.
- Transaction format unchanged.
- Block format unchanged.
- Reward amounts unchanged.
- Jackpot policy unchanged.
- PoW policy unchanged.
- Burn policy unchanged.

## 15. Test / Build Results

Commands run:

- Server: `.\mvnw.cmd test`
  - Result: PASS
  - Tests: 207 run, 0 failures, 0 errors, 0 skipped
- Wallet: `.\mvnw.cmd test`
  - Result: PASS
  - Tests: 183 run, 0 failures, 0 errors, 0 skipped
- Server: `.\mvnw.cmd clean package`
  - Result: PASS
  - Tests: 207 run, 0 failures, 0 errors, 0 skipped
- Wallet: `.\mvnw.cmd clean package`
  - Result: PASS
  - Tests: 183 run, 0 failures, 0 errors, 0 skipped

Observed existing test-output warnings:

- Maven reports duplicate dependency declarations in existing `pom.xml` files.
- Some tests print legacy `log4j` / missing `\server\resources\error\error.txt` warnings from existing logging paths, but Maven reports build success.

Website tests/package were not run because no website files were touched.

## 16. Remaining Dry-Run Gaps

- No full long-running V35 difficulty adjustment window simulation was added.
- API/UI simulated active-fork rendering was not added because those layers intentionally use real constants and must remain disabled until the real fork index is set.
- No full live database controller run with a real persistent node database was added; Phase 6B-1 and 4E-1G cover deterministic controller/storage-style paths.
- No real historical production chain data was replayed; Phase 6C-2 documented that full historical replay requires a live-node chain copy.

## 17. Recommended Next Phase

Recommended next phase:

Phase 6E - release/rollback checklist.

Optional Phase 6D-2 should be used only if the team wants a larger simulated long-run fixture for difficulty windows, API active-state injection tests, or persistent database dry-run before release checklist work.
