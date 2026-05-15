# CITU V35 Phase 3B-1 PoW Validation Report

## Scope

Phase 3B-1 wires V35 Bitcoin-like target PoW validation into block validation behind the disabled V35 fork gate only.

V35 remains disabled because both projects keep:

- `International_Trade_Union.setings.Seting.V35_RESCUE_FORK_INDEX = Long.MAX_VALUE`

No mining, block format, transaction format, hash format, controller, UI, website, tournament, or sync behavior was changed.

## Files Changed

Wallet:

- `C:\citu\unitedStates_final-master\src\main\java\International_Trade_Union\utils\UtilsBlock.java`
- `C:\citu\unitedStates_final-master\src\test\java\International_Trade_Union\protocol\v35\V35PowValidationGateTest.java`

Server:

- `C:\citu\unitedStates_storage-master\src\main\java\International_Trade_Union\utils\UtilsBlock.java`
- `C:\citu\unitedStates_storage-master\src\test\java\International_Trade_Union\protocol\v35\V35PowValidationGateTest.java`

Report:

- `C:\citu\CITU_V35_PHASE3B1_POW_VALIDATION_REPORT.md`

## Methods Changed

Wallet:

- `International_Trade_Union.utils.UtilsBlock.validationOneBlock(...)`

Server:

- `International_Trade_Union.utils.UtilsBlock.validationOneBlock(...)`

The existing legacy hash and difficulty checks remain inside the pre-V35 branch.

## V35 PoW Validation Behavior

After `ProtocolVersion.isV35(thisBlock.getIndex())` becomes true:

1. `thisBlock.getHashCompexity()` is decoded as a V35 compact target through `V35TargetDifficulty.decodeCompactTarget(...)`.
2. Targets with `signum() <= 0` are rejected.
3. `thisBlock.getHashBlock()` is validated with `V35PowValidator.isHashUnderTarget(hashBlock, decodedTarget)`.
4. Blocks are rejected when `hashNumber > target`.
5. Legacy `UtilsUse.chooseComplexity(...)` is not used for V35 blocks.

Before V35:

1. `BlockchainDifficulty.calculateTarget(...)` is still used.
2. `BlockchainDifficulty.calculateTargetV30(...)` is still used.
3. `UtilsUse.chooseComplexity(...)` is still used.
4. Existing legacy difficulty checks against `UtilsBlock.difficulty(...)`, `Seting.v4MeetsDifficulty`, `Seting.V34_NEW_ALGO`, and `Seting.V34_MIN_DIFF` remain unchanged.

The existing post-difficulty hash consistency check remains after both branches:

- `thisBlock.getHashBlock().equals(thisBlock.hashForTransaction())`

## Compact Target Handling

The Phase 3B-0 compact target helper is reused without changing `Block.java`.

- Existing field used after V35: `Block.hashCompexity`
- Decoding helper: `V35TargetDifficulty.decodeCompactTarget(long compact)`
- PoW helper: `V35PowValidator.isHashUnderTarget(String hashHex, BigInteger target)`

Invalid compact target values decode to `BigInteger.ZERO` and are rejected in `UtilsBlock.validationOneBlock(...)`.

## Tests Added

Wallet:

- `International_Trade_Union.protocol.v35.V35PowValidationGateTest`

Server:

- `International_Trade_Union.protocol.v35.V35PowValidationGateTest`

Test coverage added:

- Compact target decode plus low hash acceptance.
- Compact target decode plus high hash rejection.
- Invalid compact target/hash rejection.
- Confirmation that `V35_RESCUE_FORK_INDEX` remains `Long.MAX_VALUE`.
- Confirmation that normal indexes remain legacy while V35 is disabled.

## Full Block Fixture Status

Direct `UtilsBlock.validationOneBlock(...)` V35 block fixtures were not added in this phase.

Block-level fixtures are still blocked by runtime dependencies that are not yet isolated for V35:

- Valid signed transaction construction.
- Reward transaction shape after V35 mining is not wired yet.
- Previous block/current block pairing.
- Balance maps and duplicate signature checks.
- Hash recomputation and chain context.

The smallest safe test coverage for this phase is therefore helper-level validation of the exact compact target and `hash <= target` logic that `UtilsBlock.validationOneBlock(...)` now calls.

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
- Tests run: 29
- Failures: 0
- Errors: 0
- Skipped: 0
- `.\mvnw.cmd clean package`: PASS

Server:

- `.\mvnw.cmd test`: PASS
- Tests run: 29
- Failures: 0
- Errors: 0
- Skipped: 0
- `.\mvnw.cmd clean package`: PASS

Build warnings remain pre-existing Maven model warnings about duplicate dependency declarations in `pom.xml`.

## Legacy Behavior Confirmation

- V35 is still disabled by `Long.MAX_VALUE`.
- Normal current indexes continue through the legacy difficulty path.
- Mining is not changed.
- Legacy hash format is not changed.
- Legacy block format is not changed.
- Legacy transaction/signature formats are not changed.

## Wallet/Server Parity

Wallet and server changes match by:

- Import set for V35 PoW validation helpers.
- `UtilsBlock.validationOneBlock(...)` branch structure.
- V35 compact target rejection behavior.
- V35 `hash <= target` validation behavior.
- New test class name, package, methods, and assertions.

## Risks Before Phase 3B-2

1. Mining still produces legacy difficulty values, not V35 compact targets.
2. Full block-level V35 PoW fixtures are deferred until V35 mining can create deterministic V35-shaped blocks.
3. Chain tournament still does not use cumulative work.
4. Automatic target selection is not wired yet.
5. `hashCompexity` spelling and signed `long` storage remain legacy constraints and must stay consistently handled by the compact target helper.

## Recommended Next Phase

Phase 3B-2: implement V35 automatic target selection for mining behind the fork gate, still disabled.
