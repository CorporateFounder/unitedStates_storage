# CITU V35 Phase 3B-3 Target Adjustment Report

## Scope

Phase 3B-3 replaces the temporary V35 initial-target-only mining policy with a fork-gated automatic rolling-window target selector.

V35 remains disabled:

- Wallet: `Seting.V35_RESCUE_FORK_INDEX = Long.MAX_VALUE`
- Server: `Seting.V35_RESCUE_FORK_INDEX = Long.MAX_VALUE`

No block format, transaction format, signature format, hash format, legacy difficulty behavior, validation, server sync, tournament, controllers, UI/templates, website, or `pom.xml` files were changed.

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

- `C:\citu\CITU_V35_PHASE3B3_TARGET_ADJUSTMENT_REPORT.md`

## Methods Changed Or Added

Wallet and server:

- `International_Trade_Union.model.Mining.selectV35MiningCompactTarget(List<Block>)`
- `International_Trade_Union.protocol.v35.V35TargetDifficulty.calculateNextCompactTarget(long, long, long, long)`

Tests updated:

- `V35MiningTargetTest`
- `V35ParityTest`
- `V35TargetDifficultyTest`

## Target Adjustment Formula

The V35 mining target selector now:

1. Filters chain history to previous V35 blocks using `ProtocolVersion.isV35(block.getIndex())`.
2. Requires at least `Seting.V35_TARGET_WINDOW` V35 blocks.
3. Uses the latest V35 block's compact target as the previous target.
4. Uses the first block in the rolling window as `windowStart`.
5. Calculates:
   - `actualTimespanSeconds = lastBlock.timestamp - windowStart.timestamp`
   - `expectedTimespanSeconds = V35_TARGET_WINDOW * V35_TARGET_BLOCK_TIME_SECONDS`
   - `proposedTarget = previousTarget * actual / expected`
6. Applies `V35TargetDifficulty.clampTargetChange(...)`.
7. Encodes the adjusted target back to compact form for `Block.hashCompexity`.

Fast blocks harden the target. Slow blocks ease the target. Compact target encoding may quantize large targets, so tests assert direction, positivity, and clamp behavior rather than arbitrary full-precision round trips.

## Emergency Easing Behavior

`V35TargetDifficulty.calculateNextCompactTarget(...)` also applies emergency easing:

- `secondsSinceLastBlock = now - lastBlock.timestamp`
- If `secondsSinceLastBlock < V35_EMERGENCY_DELAY_SECONDS`, no emergency easing is applied.
- If `secondsSinceLastBlock >= V35_EMERGENCY_DELAY_SECONDS`, target easing is applied through `V35TargetDifficulty.applyEmergencyDrop(...)`.
- The emergency move is still capped by `V35_MAX_TARGET_UP`.
- Invalid decoded targets fall back to the initial compact target.

Timestamp assumption:

- This phase trusts existing block timestamp fields for deterministic target selection.
- Future activation must review timestamp validation so miners cannot abuse future or stale timestamps to force easier targets.

## Initial Target Fallback

Mining still falls back to `V35TargetDifficulty.getInitialCompactTarget()` when:

- chain history is null or empty;
- there are fewer than `V35_TARGET_WINDOW` V35 blocks;
- required timestamps are null;
- previous compact target decodes to an invalid target;
- adjusted/emergency target calculation returns invalid output.

Initial compact target:

- `0x207fffffL`

## Legacy Mining Confirmation

Before V35:

- `UtilsBlock.difficulty(...)` is still called.
- `Mining.customDiff` still overrides difficulty for `index >= Seting.V34_NEW_ALGO`.
- The legacy `new Block(..., difficulty, index)` path remains unchanged.
- Manual/custom difficulty remains active for legacy blocks.

After V35 only:

- `Mining.selectV35MiningCompactTarget(...)` chooses compact target.
- `Mining.mineV35Block(...)` stores compact target in `hashCompexity`.
- V35 mining uses `V35PowValidator.isHashUnderTarget(hash, target)`.
- Manual `customDiff` is not used to choose V35 network target.

## Tests Added Or Updated

Wallet and server:

- `V35TargetDifficultyTest`
  - compact target remains positive and decodable;
  - expected timespan keeps target stable;
  - slow blocks ease target;
  - fast blocks harden target;
  - target changes are clamped directionally;
  - emergency delay eases target only after threshold.

- `V35MiningTargetTest`
  - not enough history returns initial compact target;
  - synthetic enough V35 history with expected timespan keeps target stable;
  - compact target remains decodable and positive;
  - `V35_RESCUE_FORK_INDEX` remains `Long.MAX_VALUE`;
  - legacy `customDiff` setter/getter behavior still works while V35 is disabled.

- `V35ParityTest`
  - confirms `calculateNextCompactTarget(...)` method signature exists.

Full `Mining.miningDay(...)` runtime fixtures remain deferred because they require signed transactions, basis key material, genesis founder address file access, balance maps, previous block context, and potentially slow nonce search.

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
- Tests run: 35
- Failures: 0
- Errors: 0
- Skipped: 0
- `.\mvnw.cmd clean package`: PASS

Server:

- `.\mvnw.cmd test`: PASS
- Tests run: 35
- Failures: 0
- Errors: 0
- Skipped: 0
- `.\mvnw.cmd clean package`: PASS

Pre-existing Maven model warnings remain about duplicate dependency declarations in `pom.xml`. No `pom.xml` files were changed.

## Risks Before Phase 3B-4

1. Full mining runtime fixtures are still deferred.
2. Timestamp validation must be reviewed before activation to prevent target manipulation.
3. Chain tournament and server sync still do not use cumulative work.
4. `hashCompexity` remains a legacy misspelled field with fork-gated meaning.
5. Compact target encoding quantizes large targets, so future tests should compare compact-decoded behavior rather than arbitrary exact `BigInteger` precision.
6. V35 nonce search still lacks the legacy obsolete/stop checks because V35 is disabled; mining ergonomics should be revisited before activation.

## Recommended Next Phase

Phase 3B-4: cumulativeWork helper and chain tournament mapping/wiring plan.
