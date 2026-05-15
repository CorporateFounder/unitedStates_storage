# CITU V35 Phase 3B-0 PoW Helpers Report

Date: 2026-05-15

Scope: Phase 3B-0 only. Implemented pure V35 PoW target helper math and tests. No mining, validation, block format, transaction format, hash format, server sync, tournament, controller, UI, website, legacy difficulty behavior, or `pom.xml` behavior was changed.

## Files Changed

Wallet:

- `C:\citu\unitedStates_final-master\src\main\java\International_Trade_Union\protocol\v35\V35PowValidator.java`
- `C:\citu\unitedStates_final-master\src\main\java\International_Trade_Union\protocol\v35\V35TargetDifficulty.java`
- `C:\citu\unitedStates_final-master\src\main\java\International_Trade_Union\protocol\v35\V35WorkCalculator.java`
- `C:\citu\unitedStates_final-master\src\test\java\International_Trade_Union\protocol\v35\V35PowValidatorTest.java`
- `C:\citu\unitedStates_final-master\src\test\java\International_Trade_Union\protocol\v35\V35TargetDifficultyTest.java`
- `C:\citu\unitedStates_final-master\src\test\java\International_Trade_Union\protocol\v35\V35WorkCalculatorTest.java`
- `C:\citu\unitedStates_final-master\src\test\java\International_Trade_Union\protocol\v35\V35ParityTest.java`

Server:

- `C:\citu\unitedStates_storage-master\src\main\java\International_Trade_Union\protocol\v35\V35PowValidator.java`
- `C:\citu\unitedStates_storage-master\src\main\java\International_Trade_Union\protocol\v35\V35TargetDifficulty.java`
- `C:\citu\unitedStates_storage-master\src\main\java\International_Trade_Union\protocol\v35\V35WorkCalculator.java`
- `C:\citu\unitedStates_storage-master\src\test\java\International_Trade_Union\protocol\v35\V35PowValidatorTest.java`
- `C:\citu\unitedStates_storage-master\src\test\java\International_Trade_Union\protocol\v35\V35TargetDifficultyTest.java`
- `C:\citu\unitedStates_storage-master\src\test\java\International_Trade_Union\protocol\v35\V35WorkCalculatorTest.java`
- `C:\citu\unitedStates_storage-master\src\test\java\International_Trade_Union\protocol\v35\V35ParityTest.java`

Report:

- `C:\citu\CITU_V35_PHASE3B0_POW_HELPERS_REPORT.md`

## Methods Added Or Completed

`V35PowValidator`:

- `isHashUnderTarget(String hashHex, BigInteger target)`
  - Treats `hashHex` as unsigned hexadecimal.
  - Returns false for null, empty, blank, invalid hex, null target, zero target, or negative target.
  - Returns true only when `hashNumber <= target`.

`V35TargetDifficulty`:

- `decodeCompactTarget(long compact)`
- `encodeCompactTarget(BigInteger target)`
- `clampTargetChange(BigInteger previousTarget, BigInteger proposedTarget)`
- `calculateNextTarget(BigInteger previousTarget, long actualTimespanSeconds, long expectedTimespanSeconds)`
- `applyEmergencyDrop(BigInteger currentTarget, long secondsSinceLastBlock)`

Existing accessors preserved:

- `getTargetWindow()`
- `getTargetBlockTimeSeconds()`
- `getEmergencyDelaySeconds()`

`V35WorkCalculator`:

- `workFromTarget(BigInteger target)`

Existing placeholders preserved:

- `zeroWork()`
- `addWork(BigInteger currentWork, BigInteger additionalWork)`

## Compact Target Encoding Decision

Phase 3B-0 uses a Bitcoin-style compact target encoding stored in the lower 32 bits of the existing Java `long` field that will later be carried by `Block.hashCompexity` after V35.

Encoding shape:

- highest byte: unsigned target byte length;
- lower 23 bits: positive mantissa;
- sign bit `0x00800000` is rejected on decode and avoided on encode;
- invalid, zero, negative, or signed compact values decode to `BigInteger.ZERO`.

Reasoning:

- A full 256-bit target cannot fit directly in the existing `long hashCompexity` field.
- A compact value fits safely in the existing field without changing block format.
- The full target is decoded to `BigInteger` for pure comparison and work math.
- The same method names and behavior are implemented in wallet and server.

## Why Block.java Was Not Changed

`Block.java` was intentionally not edited because Phase 3B-0 is pure helper work only.

No changes were made to:

- `hashCompexity` field type or serialization;
- `randomNumberProof`;
- `BlockForHash`;
- `hashForTransaction()`;
- `hashForBlockchain()`;
- nonce-search methods;
- block constructors.

V35 compact target values are not yet stored in blocks or used by mining/validation.

## V35 Disabled Confirmation

V35 remains disabled:

- Wallet: `Seting.V35_RESCUE_FORK_INDEX = Long.MAX_VALUE`
- Server: `Seting.V35_RESCUE_FORK_INDEX = Long.MAX_VALUE`

No fork activation value was introduced or changed.

## Behavior Change Confirmation

No production behavior is wired to these helpers yet.

Not edited:

- `Mining.java`
- `UtilsBlock.java`
- `Block.java`
- `Blockchain.java`
- `BlockchainDifficulty.java`
- `UtilsUse.java`
- `DataShortBlockchainInformation.java`
- server tournament/sync classes
- controllers
- templates/UI
- website
- `pom.xml`

Legacy mining, legacy difficulty, legacy validation, transaction format, block format, and hash format remain unchanged.

## Tests Added Or Updated

Wallet and server:

- `V35PowValidatorTest`
  - low hash under high target is true;
  - high hash over low target is false;
  - equal hash and target is true;
  - null hash is false;
  - empty hash is false;
  - invalid hex is false;
  - null target is false;
  - zero/negative target is false.

- `V35TargetDifficultyTest`
  - exposes V35 target constants;
  - compact target encode/decode roundtrip for safe aligned targets;
  - `calculateNextTarget(...)` remains unchanged when actual equals expected;
  - slow blocks increase/ease target;
  - fast blocks decrease/harden target;
  - clamp prevents too-large upward move;
  - clamp prevents too-large downward move;
  - emergency drop does not apply before delay;
  - emergency drop applies at delay and eases target.

- `V35WorkCalculatorTest`
  - lower target produces higher work;
  - higher target produces lower work;
  - null/zero/negative targets return zero.

- `V35ParityTest`
  - updated to include new V35 target and work helper method signatures.

## Verification Commands Run

Wallet:

```powershell
cd C:\citu\unitedStates_final-master
.\mvnw.cmd test
.\mvnw.cmd clean package
```

Server:

```powershell
cd C:\citu\unitedStates_storage-master
.\mvnw.cmd test
.\mvnw.cmd clean package
```

Repository cleanup:

```powershell
cd C:\citu
git restore unitedStates_final-master/target unitedStates_storage-master/target
```

## Test Results

Wallet `.\mvnw.cmd test`:

- Result: PASS
- Tests run: 25
- Failures: 0
- Errors: 0
- Skipped: 0

Server `.\mvnw.cmd test`:

- Result: PASS
- Tests run: 25
- Failures: 0
- Errors: 0
- Skipped: 0

## Build Results

Wallet `.\mvnw.cmd clean package`:

- Result: PASS
- Tests run during package: 25
- Failures: 0
- Errors: 0
- Skipped: 0
- Jar built successfully: `target\unitedStates-0.32.216-SNAPSHOT.jar`

Server `.\mvnw.cmd clean package`:

- Result: PASS
- Tests run during package: 25
- Failures: 0
- Errors: 0
- Skipped: 0
- Jar built successfully: `target\unitedStates-0.0.423-SNAPSHOT.jar`

Pre-existing Maven warnings remain:

- duplicate `spring-boot-starter-test`;
- duplicate/conflicting `h2`;
- duplicate `spring-boot-starter-data-jpa`;
- server duplicate `log4j:log4j`.

No `pom.xml` files were changed.

## Wallet / Server Parity Status

Wallet and server helper source files match by SHA-256:

- `V35TargetDifficulty.java`: `502CD461C72FF5B9EE39EE8309B0C0F363E9DA68CED8C931FD1D30163183238C`
- `V35PowValidator.java`: `96CCB4D5AEB180267A5903119D64907F0A610A0CF68D638A185F6E08285AC98A`
- `V35WorkCalculator.java`: `168AF30A715FE58078002AADF64091EFFCCD2F7FE21033A6419E87C0FC3F9642`

The tests are mirrored in both projects and both test suites passed with the same test count.

## Risks Before Phase 3B-1

- Helpers are pure and not wired; V35 PoW validation is not active yet.
- Compact encoding deliberately loses precision for unaligned large targets, as Bitcoin nBits does. Phase 3B-1 must compare expected compact encodings, not assume arbitrary target roundtrips are exact.
- `hashCompexity` still has legacy meanings until V35; Phase 3B-1 must decode it as compact target only behind `ProtocolVersion.isV35(index)`.
- No max target or initial compact target constant was added in this phase. Phase 3B-1 may need a V35-specific max/initial target constant before wiring.
- Emergency drop is deterministic and capped, but not yet connected to timestamp validation. Timestamp manipulation risk must be reviewed before activation.
- Cumulative work math is pure only. Server chain scoring and tournament still use legacy `hashCount`, random, staking, and transaction metrics.

## Recommended Next Phase

Phase 3B-1: wire V35 target PoW validation behind the fork gate, with V35 still disabled.

Recommended constraints for Phase 3B-1:

- keep `V35_RESCUE_FORK_INDEX = Long.MAX_VALUE`;
- do not activate V35;
- first add validation tests for V35 hash-under-target and wrong compact target;
- then branch `UtilsBlock.validationOneBlock(...)` behind `ProtocolVersion.isV35(index)`;
- keep all pre-V35 hash/difficulty validation unchanged.
