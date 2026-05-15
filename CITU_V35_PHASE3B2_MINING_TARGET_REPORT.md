# CITU V35 Phase 3B-2 Mining Target Report

## Scope

Phase 3B-2 wires V35 mining target selection behind the disabled V35 fork gate.

V35 remains disabled because both projects still keep:

- `International_Trade_Union.setings.Seting.V35_RESCUE_FORK_INDEX = Long.MAX_VALUE`

No block format, transaction format, signature format, hash format, validation, server sync, tournament, controller, UI, website, or `pom.xml` behavior was changed.

## Files Changed

Wallet:

- `C:\citu\unitedStates_final-master\src\main\java\International_Trade_Union\model\Mining.java`
- `C:\citu\unitedStates_final-master\src\main\java\International_Trade_Union\protocol\v35\V35TargetDifficulty.java`
- `C:\citu\unitedStates_final-master\src\test\java\International_Trade_Union\protocol\v35\V35MiningTargetTest.java`
- `C:\citu\unitedStates_final-master\src\test\java\International_Trade_Union\protocol\v35\V35ParityTest.java`
- `C:\citu\unitedStates_final-master\src\test\java\International_Trade_Union\protocol\v35\V35TargetDifficultyTest.java`

Server:

- `C:\citu\unitedStates_storage-master\src\main\java\International_Trade_Union\model\Mining.java`
- `C:\citu\unitedStates_storage-master\src\main\java\International_Trade_Union\protocol\v35\V35TargetDifficulty.java`
- `C:\citu\unitedStates_storage-master\src\test\java\International_Trade_Union\protocol\v35\V35MiningTargetTest.java`
- `C:\citu\unitedStates_storage-master\src\test\java\International_Trade_Union\protocol\v35\V35ParityTest.java`
- `C:\citu\unitedStates_storage-master\src\test\java\International_Trade_Union\protocol\v35\V35TargetDifficultyTest.java`

Report:

- `C:\citu\CITU_V35_PHASE3B2_MINING_TARGET_REPORT.md`

## Methods Changed Or Added

Wallet:

- `International_Trade_Union.model.Mining.miningDay(...)`
- `International_Trade_Union.model.Mining.selectV35MiningCompactTarget(List<Block>)`
- `International_Trade_Union.model.Mining.mineV35Block(...)`
- `International_Trade_Union.protocol.v35.V35TargetDifficulty.getInitialCompactTarget()`
- `International_Trade_Union.protocol.v35.V35TargetDifficulty.getInitialTarget()`

Server:

- `International_Trade_Union.model.Mining.miningDay(...)`
- `International_Trade_Union.model.Mining.selectV35MiningCompactTarget(List<Block>)`
- `International_Trade_Union.model.Mining.mineV35Block(...)`
- `International_Trade_Union.protocol.v35.V35TargetDifficulty.getInitialCompactTarget()`
- `International_Trade_Union.protocol.v35.V35TargetDifficulty.getInitialTarget()`

## Legacy Mining Confirmation

Before V35:

- `UtilsBlock.difficulty(...)` is still called.
- `Mining.customDiff` still overrides difficulty for `index >= Seting.V34_NEW_ALGO`.
- Existing legacy reward and block-constructor path remains unchanged.
- Existing manual/custom difficulty behavior remains active for legacy blocks.

## V35 Mining Target Behavior

After `ProtocolVersion.isV35(index)` becomes true:

1. Mining selects `v35CompactTarget = Mining.selectV35MiningCompactTarget(blockchain)`.
2. The current implementation returns `V35TargetDifficulty.getInitialCompactTarget()`.
3. The compact target is stored in the existing `Block.hashCompexity` field by the full `Block` constructor.
4. `Mining.mineV35Block(...)` decodes the compact target with `V35TargetDifficulty.decodeCompactTarget(...)`.
5. Mining increments nonce candidates and hashes them with the existing `Block.hashForTransaction()` serialization.
6. Mining accepts only hashes where `V35PowValidator.isHashUnderTarget(hash, target)` returns true.
7. Legacy 100-level difficulty matching is not used in the V35 mining branch.

`Block.java` was not changed. The V35 branch uses the existing full block constructor to avoid the legacy constructor's internal legacy PoW search.

## Initial Target Policy

The initial V35 compact target is:

- `0x207fffffL`

This is a Bitcoin-style compact target value that decodes to a valid positive `BigInteger` target and fits in the existing `long hashCompexity` field. It is intentionally easy for first wiring and early testability. It is a placeholder initial target policy until Phase 3B-3 wires deterministic rolling-window target adjustment from previous blocks.

## Tests Added Or Updated

Wallet and server:

- `V35MiningTargetTest`
  - verifies `Mining.selectV35MiningCompactTarget(...)` returns the configured initial compact target;
  - verifies the compact target decodes to a positive `BigInteger`;
  - verifies a hash equal to the target passes `V35PowValidator`;
  - verifies `V35_RESCUE_FORK_INDEX` is still `Long.MAX_VALUE`;
  - verifies a normal index remains legacy;
  - verifies `Mining.customDiff` still works while V35 is disabled.

- `V35TargetDifficultyTest`
  - verifies `getInitialCompactTarget()`;
  - verifies `getInitialTarget()`;
  - verifies initial compact target decode parity.

- `V35ParityTest`
  - verifies the new initial target helper method signatures exist.

Full `Mining.miningDay(...)` runtime fixture tests were not added because they require signed transaction fixtures, basis key material, genesis founder address file access, balance maps, previous block context, and potentially slow nonce search. The safe coverage for this phase is helper-level and branch-target behavior.

## Verification Commands

Wallet:

- `cd C:\citu\unitedStates_final-master`
- `.\mvnw.cmd test`
- `.\mvnw.cmd clean package`

Server:

- `cd C:\citu\unitedStates_storage-master`
- `.\mvnw.cmd test`
- `.\mvnw.cmd clean package`

## Verification Results

Wallet:

- `.\mvnw.cmd test`: PASS
- Tests run: 32
- Failures: 0
- Errors: 0
- Skipped: 0
- `.\mvnw.cmd clean package`: PASS

Server:

- `.\mvnw.cmd test`: PASS
- Tests run: 32
- Failures: 0
- Errors: 0
- Skipped: 0
- `.\mvnw.cmd clean package`: PASS

Pre-existing Maven model warnings remain about duplicate dependency declarations in `pom.xml`. No `pom.xml` files were changed.

## V35 Disabled Confirmation

- Wallet: `Seting.V35_RESCUE_FORK_INDEX = Long.MAX_VALUE`
- Server: `Seting.V35_RESCUE_FORK_INDEX = Long.MAX_VALUE`

Normal current indexes still take the legacy mining path.

## Wallet/Server Parity

Wallet and server changes match by:

- target helper methods;
- initial compact target value;
- mining selector method name and behavior;
- V35 mining branch structure;
- helper-level tests and assertions.

## Risks Before Phase 3B-3

1. V35 target selection currently uses only the initial target. It does not yet adjust from previous blocks.
2. Full `Mining.miningDay(...)` fixtures are still deferred because constructing valid signed block contexts is not lightweight.
3. V35 nonce search currently has no explicit stop/obsolete check because it is dormant while V35 is disabled; a later mining ergonomics phase should add bounded stop handling consistent with current mining.
4. Chain tournament and sync still do not use cumulative work.
5. The initial target is intentionally easy and must be reviewed before real activation.

## Recommended Next Phase

Phase 3B-3: automatic difficulty adjustment calculation using previous blocks, still disabled.
