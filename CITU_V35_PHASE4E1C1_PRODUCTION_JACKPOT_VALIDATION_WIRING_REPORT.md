# CITU V35 Phase 4E-1C1 Production Jackpot Validation Wiring Report

## Scope

Phase 4E-1C1 wires context-aware V35 jackpot validation into production validation paths that can provide canonical chain context. V35 remains disabled because `Seting.V35_RESCUE_FORK_INDEX = Long.MAX_VALUE` in both wallet and server.

No mining payout creation was added. `Mining.java` was not changed. Transaction and block formats were not changed.

## Files Changed

Wallet:
- `unitedStates_final-master/src/main/java/International_Trade_Union/utils/UtilsBlock.java`
- `unitedStates_final-master/src/main/java/International_Trade_Union/entity/blockchain/Blockchain.java`
- `unitedStates_final-master/src/main/java/International_Trade_Union/controllers/BasisController.java`
- `unitedStates_final-master/src/test/java/International_Trade_Union/protocol/v35/ProductionJackpotValidationWiringTest.java`

Server:
- `unitedStates_storage-master/src/main/java/International_Trade_Union/utils/UtilsBlock.java`
- `unitedStates_storage-master/src/main/java/International_Trade_Union/entity/blockchain/Blockchain.java`
- `unitedStates_storage-master/src/main/java/International_Trade_Union/controllers/BasisController.java`
- `unitedStates_storage-master/src/main/java/International_Trade_Union/model/TournamentService.java`
- `unitedStates_storage-master/src/test/java/International_Trade_Union/protocol/v35/ProductionJackpotValidationWiringTest.java`

## Methods Changed Or Added

Wallet and server `UtilsBlock.java`:
- Added `validationOneBlockWithCanonicalContext(...)`.
- Existing `validationOneBlock(...)` now delegates to the context-aware overload with an empty chain context and `Seting.V35_RESCUE_FORK_INDEX`.
- Added private `isV35ActiveForValidation(Block block, long forkIndex)`.
- V35 jackpot BASIS payout candidates can now be accepted only when explicit canonical chain context is supplied and `validateV35JackpotPayoutWithChainContext(...)` returns a valid proof.
- Ordinary no-context validation remains conservative and rejects jackpot payout candidates.

Wallet and server `Blockchain.java`:
- Added `shortCheckWithCanonicalContext(...)`.
- Existing `shortCheck(...)` delegates to the context-aware overload with empty context and the disabled fork index.
- Added private `canonicalContextBeforeBlock(...)` to combine canonical prefix blocks and already accepted candidate blocks.
- `checkFromFile(...)` now passes its canonical prefix into `UtilsBlock.validationOneBlockWithCanonicalContext(...)`.

Wallet `BasisController.java`:
- Wallet sync validation now calls `Blockchain.shortCheckWithCanonicalContext(...)`.
- Wallet local mined-block validation now calls `UtilsBlock.validationOneBlockWithCanonicalContext(...)`.
- Added `v35CanonicalContextBefore(...)` and `v35CanonicalContextForLocalValidation(...)`.

Server `BasisController.java`:
- `resolve_conflict(...)` now calls `Blockchain.shortCheckWithCanonicalContext(...)`.
- Added `v35CanonicalContextBefore(...)`.

Server `TournamentService.java`:
- Candidate validation and final winner validation now call `Blockchain.shortCheckWithCanonicalContext(...)`.
- Added `v35CanonicalContextBefore(...)`.

## Production Paths Wired

- Wallet `Blockchain.checkFromFile(...)`.
- Server `Blockchain.checkFromFile(...)`.
- Wallet `Blockchain.shortCheckWithCanonicalContext(...)`.
- Server `Blockchain.shortCheckWithCanonicalContext(...)`.
- Wallet sync path in `BasisController`.
- Wallet local mined-block validation path in `BasisController`.
- Server remote resolve path in `BasisController.resolve_conflict(...)`.
- Server tournament candidate validation in `TournamentService.validateAndAddBlock(...)`.
- Server tournament final winner validation in `TournamentService.tournament(...)`.

## Paths That Remain Conservative

- Existing `UtilsBlock.validationOneBlock(...)` remains available and receives no canonical chain context.
- Existing `Blockchain.shortCheck(...)` remains available and receives no canonical chain context.
- `UtilsResolving` call sites were not edited in this phase because they were not in the allowed file list. They continue using existing conservative validation paths unless routed through the newly wired production paths.
- Mining remains unwired; it still does not create jackpot payouts.

## V35 Gate

V35 remains disabled:
- Wallet `Seting.V35_RESCUE_FORK_INDEX = Long.MAX_VALUE`.
- Server `Seting.V35_RESCUE_FORK_INDEX = Long.MAX_VALUE`.

The new context-aware validation path only enters V35 jackpot validation when the supplied fork index is not `Long.MAX_VALUE` and the block index is at or beyond the supplied fork index. With the current constants, production behavior remains legacy.

## Jackpot Validation Behavior

When V35 is active in a context-aware path:
- Instant miner reward remains distinct from jackpot payout.
- Canonical jackpot amount classification is required.
- Full chain context is required.
- `V35JackpotChainContextAdapter` and `V35JackpotProofService` validate deterministic winner, maturity, duplicate state, and founder-recipient rejection.
- If context is missing or incomplete, jackpot payout validation rejects.

When no context is supplied:
- Jackpot payout candidates remain rejected.
- This prevents accepting ambiguous BASIS payouts.

## Tests Added

Wallet and server:
- `ProductionJackpotValidationWiringTest`

The new test confirms:
- V35 remains disabled with `Long.MAX_VALUE`.
- `UtilsBlock.validationOneBlockWithCanonicalContext(...)` exists.
- `Blockchain.shortCheckWithCanonicalContext(...)` exists.

Existing jackpot tests continue to cover:
- `UtilsBlock.validateV35JackpotPayoutWithChainContext(...)`.
- `UtilsBlock.validateV35JackpotPayoutsWithCanonicalContext(...)`.
- `V35JackpotChainContextAdapter`.
- `V35JackpotProofService`.
- `V35JackpotPayoutBuilder`.
- `UtilsBalance` jackpot accounting helper behavior.

Full signed production block fixtures are still not added because constructing complete valid blocks with signatures, balances, hashes, V35 target PoW, jackpot proof, and canonical history remains a larger fixture task.

## Verification Commands

Wallet:
- `.\mvnw.cmd test` passed: 126 tests, 0 failures, 0 errors.
- `.\mvnw.cmd clean package` passed: 126 tests, 0 failures, 0 errors.

Server:
- `.\mvnw.cmd test` passed: 138 tests, 0 failures, 0 errors.
- `.\mvnw.cmd clean package` passed: 138 tests, 0 failures, 0 errors.

Build warnings remain pre-existing Maven duplicate dependency warnings.

## Confirmations

- V35 remains disabled.
- `Mining.java` was not changed.
- `UtilsBalance.java` was not changed.
- `Block.java` was not changed.
- `DtoTransaction.java` was not changed.
- Transaction format unchanged.
- Block format unchanged.
- Mining behavior unchanged.
- Existing no-context validation path remains available and conservative.

## Remaining Risks Before Mining Payout Wiring

1. `UtilsResolving` still has legacy call sites that were not edited because this phase's allowed files did not include it.
2. Full end-to-end signed block fixtures are still missing.
3. When V35 is activated, canonical context loading may be expensive for very long chains because controller paths gather prefix blocks from storage.
4. Jackpot payout creation in mining must not start until the remaining resolve paths are reviewed or routed through the context-aware validation path.

## Recommended Next Phase

Phase 4E-1D: wire `V35JackpotPayoutBuilder` into `Mining.java` behind the V35 fork gate only after reviewing whether remaining `UtilsResolving` validation paths need explicit context-aware routing.
