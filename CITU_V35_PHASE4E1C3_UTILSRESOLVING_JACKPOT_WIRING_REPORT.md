# CITU V35 Phase 4E-1C3 UtilsResolving Jackpot Wiring Report

## Scope

Phase 4E-1C3 wired `UtilsResolving` to the existing V35 context-aware jackpot validation/accounting path where canonical chain context is available.

V35 remains disabled:

- `unitedStates_final-master/src/main/java/International_Trade_Union/setings/Seting.java`
  - `V35_RESCUE_FORK_INDEX = Long.MAX_VALUE`
- `unitedStates_storage-master/src/main/java/International_Trade_Union/setings/Seting.java`
  - `V35_RESCUE_FORK_INDEX = Long.MAX_VALUE`

No mining payout creation was added.

## Files Changed

Wallet:

- `unitedStates_final-master/src/main/java/International_Trade_Union/utils/UtilsResolving.java`
- `unitedStates_final-master/src/test/java/International_Trade_Union/utils/UtilsResolvingV35JackpotWiringTest.java`

Server:

- `unitedStates_storage-master/src/main/java/International_Trade_Union/utils/UtilsResolving.java`
- `unitedStates_storage-master/src/test/java/International_Trade_Union/utils/UtilsResolvingV35JackpotWiringTest.java`

Report:

- `CITU_V35_PHASE4E1C3_UTILSRESOLVING_JACKPOT_WIRING_REPORT.md`

## Methods Changed

Wallet and server `UtilsResolving.java`:

- `resolve3(...)`
  - Replaced remaining `Blockchain.shortCheck(...)` resolving calls with `Blockchain.shortCheckWithCanonicalContext(...)`.
  - Context is loaded from the canonical chain before the current previous block.
  - With V35 disabled, the context helper returns an empty list and the existing behavior remains legacy.

- `helpResolve(...)`
  - Replaced the replacement-branch `Blockchain.shortCheck(...)` loop with `Blockchain.shortCheckWithCanonicalContext(...)`.
  - Maintains an in-memory `v35ReplacementContext` as candidate blocks validate, so future V35 jackpot proof checks can see the replacement prefix.
  - Balance rollback for validation remains legacy unless V35 is active and deleted blocks require jackpot-aware accounting.

- `addBlock3(...)`
  - Replaced direct per-block `calculateBalance(...)` with `calculateResolvingBalance(...)`.
  - Legacy blocks still use the original `calculateBalance(...)` path.
  - V35 jackpot payout candidates, if V35 is active, are accounted by stripping jackpot payout candidates from legacy BASIS accounting and then applying `UtilsBalance.applyV35JackpotPayoutsWithChainContext(...)`.

- `rollBackAddBlock3(...)`
  - Legacy rollback remains unchanged while V35 is disabled.
  - If V35 is active and deleted blocks contain jackpot payout candidates, balances are recalculated from the canonical remaining prefix with `UtilsBalance.calculateBalances(..., founderAddress, forkIndex)` instead of using legacy per-block rollback that cannot safely reverse jackpot proof accounting.

## Helper Methods Added

Wallet and server `UtilsResolving.java`:

- `v35CanonicalContextBefore(Block prevBlock)`
- `v35CanonicalContextBeforeIndex(long index)`
- `isV35JackpotAccountingRequired(Block block)`
- `containsV35JackpotPayoutCandidate(List<Block> blocks)`
- `containsV35JackpotPayoutCandidate(Block block)`
- `requiresV35JackpotAccounting(List<Block> blocks)`
- `withoutV35JackpotPayoutCandidates(Block block)`
- `calculateResolvingBalance(...)`

## Validation Fix Details

The risky no-context validation calls in `UtilsResolving` now use `Blockchain.shortCheckWithCanonicalContext(...)`.

For V35 jackpot payout blocks, this prevents `UtilsResolving` from accepting or rejecting blocks through the old one-block path when proof validation needs canonical chain context.

While `V35_RESCUE_FORK_INDEX = Long.MAX_VALUE`, the helper context remains empty and V35 jackpot validation cannot activate.

## Accounting Fix Details

`addBlock3(...)` now keeps legacy per-block accounting for all pre-V35/current blocks.

If V35 is later activated and a block includes canonical jackpot payout candidates:

1. The block is first accounted without jackpot payout candidates through legacy `calculateBalance(...)`.
2. Jackpot payouts are then applied only through `UtilsBalance.applyV35JackpotPayoutsWithChainContext(...)`.
3. Invalid proof, wrong winner, immature period, duplicate payout, founder recipient, or missing context fails the V35 accounting path.

This avoids trusting jackpot amount alone and keeps jackpot sender eligibility out of accounting.

## Paths Wired

Wallet and server:

- `resolve3(...)` downloaded-block validation branches
- `helpResolve(...)` replacement-chain validation loop
- `addBlock3(...)` block-save balance accounting
- `rollBackAddBlock3(...)` rollback balance recalculation when V35 jackpot payouts are involved

## Paths Remaining Conservative

- `Mining.java` was not changed and still does not create jackpot payout transactions.
- Ordinary no-context validation remains conservative for ambiguous jackpot payout blocks.
- Full end-to-end `UtilsResolving` integration tests are still limited by runtime/network/database setup complexity; this phase added helper-level tests around the new gate and stripping behavior.

## Transaction / Block Format

Unchanged.

No fields were added to:

- `Block.java`
- `DtoTransaction.java`
- `Account.java`

## Tests Added

Wallet:

- `unitedStates_final-master/src/test/java/International_Trade_Union/utils/UtilsResolvingV35JackpotWiringTest.java`

Server:

- `unitedStates_storage-master/src/test/java/International_Trade_Union/utils/UtilsResolvingV35JackpotWiringTest.java`

Test coverage confirms:

- jackpot payout candidates are detected by amount classification;
- `V35_RESCUE_FORK_INDEX = Long.MAX_VALUE` prevents jackpot-aware accounting activation;
- jackpot payout candidates can be stripped while preserving the 6.00 GOLD miner reward;
- list/null/empty candidate detection is safe.

## Verification Commands

Wallet:

- `.\mvnw.cmd test`
  - Result: PASS, 129 tests, 0 failures, 0 errors
- `.\mvnw.cmd clean package`
  - Result: PASS, 129 tests, 0 failures, 0 errors

Server:

- `.\mvnw.cmd test`
  - Result: PASS, 141 tests, 0 failures, 0 errors
- `.\mvnw.cmd clean package`
  - Result: PASS, 141 tests, 0 failures, 0 errors

Existing Maven warnings remain about duplicate dependencies in `pom.xml`; this phase did not edit `pom.xml`.

## Confirmation

- V35 remains disabled.
- `Mining.java` was not changed.
- Transaction format unchanged.
- Block format unchanged.
- Legacy behavior remains the active behavior while `V35_RESCUE_FORK_INDEX = Long.MAX_VALUE`.
- `UtilsResolving` no longer has direct `Blockchain.shortCheck(...)` call sites.

## Remaining Risks

1. Full runtime `UtilsResolving` integration tests still require a configured H2/file/network resolving fixture.
2. The V35 jackpot branch is inactive until a real fork index is set, so final activation still needs an end-to-end resolving simulation.
3. Mining payout wiring should remain gated until an activation-height test validates: builder -> mining transaction -> context-aware validation -> resolving save -> proof-aware balance accounting.

## Mining Payout Wiring Status

Mining payout wiring is safer to begin after this phase because the known `UtilsResolving` no-context validation and per-block accounting paths have been guarded.

Recommended next phase:

- Phase 4E-1D — wire `V35JackpotPayoutBuilder` into `Mining.java` behind the V35 fork gate, still disabled.
