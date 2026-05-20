# CITU V35 Phase 4E-1D Mining Jackpot Payout Wiring Report

## Scope

Phase 4E-1D wired `V35JackpotPayoutBuilder` into V35 mining only.

V35 remains disabled:

- `unitedStates_final-master/src/main/java/International_Trade_Union/setings/Seting.java`
  - `V35_RESCUE_FORK_INDEX = Long.MAX_VALUE`
- `unitedStates_storage-master/src/main/java/International_Trade_Union/setings/Seting.java`
  - `V35_RESCUE_FORK_INDEX = Long.MAX_VALUE`

Transaction format, block format, hash format, controllers, UI/templates, website, and `pom.xml` were not changed.

## Files Changed

Wallet:

- `unitedStates_final-master/src/main/java/International_Trade_Union/model/Mining.java`
- `unitedStates_final-master/src/test/java/International_Trade_Union/model/MiningV35JackpotPayoutWiringTest.java`

Server:

- `unitedStates_storage-master/src/main/java/International_Trade_Union/model/Mining.java`
- `unitedStates_storage-master/src/test/java/International_Trade_Union/model/MiningV35JackpotPayoutWiringTest.java`

Report:

- `CITU_V35_PHASE4E1D_MINING_JACKPOT_PAYOUT_WIRING_REPORT.md`

## Methods Changed

Wallet and server `Mining.java`:

- Added `buildV35JackpotPayoutTransactionsForMining(...)`.
- Added an overload that accepts an explicit `forkIndex` for tests.
- Updated the V35 branch in `miningDay(...)` to call `buildV35JackpotPayoutTransactionsForMining(...)` after creating the 6.00 GOLD instant miner reward and before the existing signature de-duplication / sorting pass.

## Mining Integration

The V35 branch now does this:

1. Creates the existing instant miner reward:
   - sender: `Seting.BASIS_ADDRESS`
   - recipient: current miner
   - GOLD: `V35RewardCalculator.getInstantMinerGoldReward()` = `6.00`
   - Silver/stock: `0.00`
   - bonus: transaction fee/bonus sum
2. Does not create a founder reward.
3. Calls `V35JackpotPayoutBuilder.buildPayoutTransactions(...)` with:
   - canonical `blockchain`
   - candidate payout block index
   - current miner address
   - founder address
   - fork index
4. Signs only builder-produced payout transactions with the BASIS private key.
5. Skips null, unprovable, duplicate, immature, non-exact-slot, or founder-recipient payouts because the builder/proof adapter returns no valid transaction for those cases.
6. Adds signed jackpot payout transactions to `forAdd`.
7. Keeps the existing final transaction de-duplication and signature sort.

## Legacy Behavior

Legacy mining remains unchanged before V35.

Because `ProtocolVersion.isV35(index)` still depends on `Seting.V35_RESCUE_FORK_INDEX`, and that constant is `Long.MAX_VALUE`, current live mining never enters the V35 jackpot payout creation path.

## Jackpot Payout Creation Rules

After V35 only:

- exact-slot payouts only;
- missed payouts are not paid later;
- up to one payout per jackpot period type;
- multiple different period types may be included in the same block;
- payout recipient is the deterministic winner miner address;
- sender addresses are never eligible;
- founder recipient is skipped;
- duplicate prior payout is skipped;
- insufficient future hashes or immature period returns no payout;
- the payout transaction amount is produced by `JackpotCalculator.expectedPayoutAmount(...)`.

Canonical payout amounts:

- daily: `302.40`
- weekly: `1209.60`
- monthly: `3888.00`
- quarterly: `9720.00`
- half-year: `15724.80`
- yearly: `23652.00`

## Transaction Ordering

Logical insertion order in the V35 branch is:

1. user transactions already accepted into `forAdd`;
2. instant miner reward;
3. builder-produced jackpot payouts in builder period order.

Actual block order remains the existing mining behavior:

- transactions are de-duplicated by encoded signature;
- null transactions are removed;
- final transaction list is sorted by encoded signature.

This preserves the existing deterministic signing/order convention.

## Validation Compatibility

Generated jackpot payout transactions are built by `V35JackpotPayoutBuilder` and verified by `V35JackpotChainContextAdapter` before the builder returns them.

Tests also verify that a mining-created signed payout passes `V35JackpotChainContextAdapter.verifyPayoutWithChainContext(...)`.

Production compatibility is intended with:

- `UtilsBlock.validationOneBlockWithCanonicalContext(...)`;
- `Blockchain.shortCheckWithCanonicalContext(...)`;
- `UtilsResolving` context-aware validation path;
- `UtilsBalance.applyV35JackpotPayoutsWithChainContext(...)`.

Full end-to-end mined block persistence/balance tests remain for Phase 4E-1E.

## Tests Added

Wallet:

- `unitedStates_final-master/src/test/java/International_Trade_Union/model/MiningV35JackpotPayoutWiringTest.java`

Server:

- `unitedStates_storage-master/src/test/java/International_Trade_Union/model/MiningV35JackpotPayoutWiringTest.java`

Coverage:

- disabled fork returns no mining jackpot payouts;
- mature daily payout is signed and proof-valid;
- weekly slot can produce both daily and weekly signed payouts;
- immature, duplicate, and founder-recipient contexts are skipped;
- 6.00 GOLD instant miner reward is not a jackpot canonical payout amount.

## Verification Commands

Wallet:

- `.\mvnw.cmd test`
  - Result: PASS, 133 tests, 0 failures, 0 errors
- `.\mvnw.cmd clean package`
  - Result: PASS, 133 tests, 0 failures, 0 errors

Server:

- `.\mvnw.cmd test`
  - Result: PASS, 145 tests, 0 failures, 0 errors
- `.\mvnw.cmd clean package`
  - Result: PASS, 145 tests, 0 failures, 0 errors

Existing Maven duplicate dependency warnings remain unchanged. `pom.xml` was not edited.

## Confirmations

- V35 remains disabled.
- Legacy mining remains unchanged before V35.
- `Mining.java` now creates jackpot payouts only inside the V35 branch.
- Instant miner reward remains `6.00` GOLD.
- Founder reward remains `0.00` after V35 and no founder reward transaction is created in the V35 branch.
- Generated jackpot payouts use `V35JackpotPayoutBuilder`.
- Transaction format unchanged.
- Block format unchanged.

## Remaining Risks Before End-To-End Jackpot Tests

1. Full end-to-end fixture still needs to validate: mining-created jackpot payout block -> context-aware block validation -> resolving save -> proof-aware balance accounting.
2. V35 remains inactive, so final activation still needs a simulated non-`Long.MAX_VALUE` fork index test suite.
3. The builder uses exact-slot-only payout policy; late payout support remains intentionally unimplemented.

## Recommended Next Phase

Phase 4E-1E — end-to-end jackpot payout block tests and old-chain safety tests.
