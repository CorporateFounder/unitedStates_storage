# CITU V35 Phase 4E-1G Jackpot Controller/Storage Integration Report

## Scope

Phase 4E-1G added targeted controller/storage-style integration coverage for V35 jackpot payout blocks and applied the smallest balance-path fix proven by the reviewed risk.

V35 remains disabled in both projects:

- `unitedStates_final-master/src/main/java/International_Trade_Union/setings/Seting.java`
- `unitedStates_storage-master/src/main/java/International_Trade_Union/setings/Seting.java`

`V35_RESCUE_FORK_INDEX = Long.MAX_VALUE`

No transaction format, block format, signature format, hash format, controllers, UI/templates, website, `Mining.java`, `Block.java`, `DtoTransaction.java`, or `Account.java` were changed.

## Exact Files Changed

Wallet:

- `unitedStates_final-master/src/main/java/International_Trade_Union/utils/UtilsBalance.java`
- `unitedStates_final-master/src/main/java/International_Trade_Union/entity/blockchain/Blockchain.java`
- `unitedStates_final-master/src/test/java/International_Trade_Union/model/MiningV35JackpotControllerStorageIntegrationTest.java`

Server:

- `unitedStates_storage-master/src/main/java/International_Trade_Union/utils/UtilsBalance.java`
- `unitedStates_storage-master/src/main/java/International_Trade_Union/entity/blockchain/Blockchain.java`
- `unitedStates_storage-master/src/test/java/International_Trade_Union/model/MiningV35JackpotControllerStorageIntegrationTest.java`

Root report:

- `CITU_V35_PHASE4E1G_JACKPOT_CONTROLLER_STORAGE_INTEGRATION_REPORT.md`

## Production Code Changed

Yes, minimally.

### Bug Proven

`Blockchain.shortCheckWithCanonicalContext(...)` and `Blockchain.checkFromFile(...)` were already using context-aware jackpot validation, but their running balance updates still used legacy `UtilsBalance.calculateBalance(...)`.

That legacy calculator does not credit V35 jackpot payout candidates because V35 jackpot BASIS payouts are not founder/miner rewards. This means a valid jackpot payout block could validate, but a later spend by the jackpot winner could be rejected by stale running-balance state.

### Minimal Fix

Added:

- `UtilsBalance.calculateBalanceWithV35JackpotContext(...)`

Behavior:

- Before V35 or when a block has no jackpot payout candidate, delegates to legacy `calculateBalance(...)`.
- After V35, for jackpot payout candidate blocks:
  - strips jackpot payout candidates from legacy accounting,
  - runs legacy accounting for normal block transactions and the 6.00 GOLD miner reward,
  - applies jackpot payouts only through `applyV35JackpotPayoutsWithChainContext(...)`.
  - invalid jackpot payout candidates are not credited.

Updated:

- `Blockchain.shortCheckWithCanonicalContext(...)`
  - reuses the same canonical context snapshot for validation and balance accounting.
- `Blockchain.checkFromFile(...)`
  - delegates the existing no-arg behavior to a new overload.
  - added `checkFromFile(String filename, long forkIndex)` so tests/future V35 paths can use the same proof-aware balance path without activating global V35.

## Exact Tests Added

Added mirrored test class in wallet and server:

- `International_Trade_Union.model.MiningV35JackpotControllerStorageIntegrationTest`

Tests:

1. `disabledForkKeepsControllerStorageJackpotPathInactive`
   - Confirms `V35_RESCUE_FORK_INDEX = Long.MAX_VALUE`.
   - Confirms default mining builder returns no jackpot payouts while V35 is disabled.

2. `jackpotPayoutSurvivesStorageRoundTripAndLaterSpendInShortCheck`
   - Builds simulated V35 canonical chain context.
   - Builds a mining-created daily jackpot payout transaction.
   - Adds the required 6.00 GOLD instant miner reward.
   - Creates a V35 payout block.
   - Round-trips the payout block through JSON serialization/deserialization.
   - Confirms the reloaded payout block is still context-valid.
   - Adds a later block where the jackpot winner spends part of the payout.
   - Runs both blocks through `Blockchain.shortCheckWithCanonicalContext(...)`.
   - Confirms the winner balance is credited and the later spend succeeds.

3. `storageStyleValidationRejectsInvalidJackpotPayouts`
   - Builds a wrong-winner jackpot payout block.
   - Confirms context-aware jackpot validation rejects it.
   - Confirms the storage-style `shortCheckWithCanonicalContext(...)` path returns invalid.

## Controller/Storage-Style Path Coverage

Covered:

- Mining payout builder output.
- JSON save/reload-style block serialization.
- Context-aware jackpot validation after reload.
- `Blockchain.shortCheckWithCanonicalContext(...)` validation path.
- Running balance recalculation inside `Blockchain.shortCheckWithCanonicalContext(...)`.
- Later spend by jackpot winner after valid jackpot payout credit.
- Wrong-winner invalid payout rejection.

Not fully covered:

- Real controller HTTP request/response path.
- Real database-backed `BlockService` persistence.
- Full `Blockchain.checkFromFile(...)` from genesis through V35. The overload is now V35-testable, but building a complete old-chain fixture from genesis remains outside this phase.

## Jackpot Payout Survival

Jackpot payout blocks survive save/reload format-wise because they use unchanged existing fields:

- `Block`
- `DtoTransaction`
- `BASIS_ADDRESS -> winnerMinerAddress`
- canonical jackpot GOLD amount
- `digitalStockBalance = 0.00`
- `bonusForMiner = 0.00`

The new integration test round-trips the payout block through `UtilsJson.objToStringJson(...)` and `UtilsJson.jsonToBLock(...)`, then validates the reloaded block.

## Balance Recalculation

The reviewed running-balance bug was fixed for:

- `Blockchain.shortCheckWithCanonicalContext(...)`
- `Blockchain.checkFromFile(...)`

Valid jackpot payouts are now credited only through the shared proof-aware path. Invalid jackpot payout candidates are not credited.

## Later Spend

The new test proves:

- jackpot winner receives the valid payout,
- a later spend by the jackpot winner passes the short-check validation path,
- recipient balance increases by the spend amount,
- winner balance decreases by the spend amount after jackpot credit.

## Invalid Payouts

Covered directly in this phase:

- wrong winner payout rejected.

Covered by existing prior jackpot tests and still passing:

- immature payout rejected.
- duplicate same-period payout rejected.
- founder recipient rejected.
- non-canonical amount rejected.
- jackpot-looking payout before V35 rejected.
- missed exact-slot payout not paid later.

## V35 Disabled Confirmation

V35 remains disabled in both wallet and server:

- `V35_RESCUE_FORK_INDEX = Long.MAX_VALUE`

The new behavior is inactive for live/default paths unless an explicit test-only fork index is supplied.

## Format Confirmation

Unchanged:

- transaction format
- block format
- hash format
- signature format
- DTO fields

## Verification Commands

Wallet:

- `.\mvnw.cmd test`
- `.\mvnw.cmd clean package`

Server:

- `.\mvnw.cmd test`
- `.\mvnw.cmd clean package`

## Test Results

Initial test run:

- Wallet `.\mvnw.cmd test`: passed, 140 tests.
- Server `.\mvnw.cmd test`: passed, 152 tests.

Final verification:

- Wallet `.\mvnw.cmd test`: passed, 140 tests.
- Wallet `.\mvnw.cmd clean package`: passed, 140 tests during package.
- Server `.\mvnw.cmd test`: passed, 152 tests.
- Server `.\mvnw.cmd clean package`: passed, 152 tests during package.

Notes:

- Maven still prints existing duplicate-dependency model warnings in both projects.
- The controller/database-backed HTTP path remains untested; this phase covered storage-style JSON round-trip and `Blockchain.shortCheckWithCanonicalContext(...)`.

## Remaining Risks

1. Full controller/database-backed integration remains untested.
2. `UtilsBlock.validation(...)` and plain `Blockchain.shortCheck(...)` remain conservative/no-context paths and should not be used to accept jackpot payout blocks after V35 without canonical context.
3. `checkFromFile(...)` now has the proof-aware accounting path, but a full genesis-to-V35 file fixture still needs a larger historical-chain fixture.
4. The invalid-block path still prints legacy stack traces during expected negative tests.

## Recommendation

Proceed to Phase 5A burn/supply planning.

No further jackpot consensus fix phase is required before Phase 5A, but a later pre-activation hardening phase should add a database/controller-level integration test using real `BlockService` storage.
