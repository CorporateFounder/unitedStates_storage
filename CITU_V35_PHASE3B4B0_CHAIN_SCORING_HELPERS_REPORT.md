# CITU V35 Phase 3B-4B0 Chain Scoring Helpers Report

Phase: 3B-4B0  
Project root: `C:\citu`  
Created: 2026-05-15

## Scope

Implemented pure V35 cumulative work and chain scoring helpers in both wallet and server projects. This phase does not wire the helpers to blockchain metadata, server sync, tournament selection, DTOs, comparators, network code, mining, validation, UI, website, or controllers.

V35 remains disabled:

- Wallet: `Seting.V35_RESCUE_FORK_INDEX = Long.MAX_VALUE`
- Server: `Seting.V35_RESCUE_FORK_INDEX = Long.MAX_VALUE`

## Files Changed

Wallet:

- `C:\citu\unitedStates_final-master\src\main\java\International_Trade_Union\protocol\v35\V35WorkCalculator.java`
- `C:\citu\unitedStates_final-master\src\main\java\International_Trade_Union\protocol\v35\ChainTournamentScorer.java`
- `C:\citu\unitedStates_final-master\src\test\java\International_Trade_Union\protocol\v35\V35WorkCalculatorTest.java`
- `C:\citu\unitedStates_final-master\src\test\java\International_Trade_Union\protocol\v35\ChainTournamentScorerTest.java`
- `C:\citu\unitedStates_final-master\src\test\java\International_Trade_Union\protocol\v35\V35ParityTest.java`

Server:

- `C:\citu\unitedStates_storage-master\src\main\java\International_Trade_Union\protocol\v35\V35WorkCalculator.java`
- `C:\citu\unitedStates_storage-master\src\main\java\International_Trade_Union\protocol\v35\ChainTournamentScorer.java`
- `C:\citu\unitedStates_storage-master\src\test\java\International_Trade_Union\protocol\v35\V35WorkCalculatorTest.java`
- `C:\citu\unitedStates_storage-master\src\test\java\International_Trade_Union\protocol\v35\ChainTournamentScorerTest.java`
- `C:\citu\unitedStates_storage-master\src\test\java\International_Trade_Union\protocol\v35\V35ParityTest.java`

Report:

- `C:\citu\CITU_V35_PHASE3B4B0_CHAIN_SCORING_HELPERS_REPORT.md`

Generated `target` outputs were restored after Maven verification and are not part of this phase diff.

## Methods And Classes Added

### V35WorkCalculator

Added:

- `workFromCompactTarget(long compactTarget)`
  - Decodes the compact target with `V35TargetDifficulty.decodeCompactTarget(...)`.
  - Calculates work with `workFromTarget(...)`.
  - Invalid compact target values return `BigInteger.ZERO`.

- `cumulativeWorkFromCompactTargets(List<Long> compactTargets)`
  - Sums work from a list of compact targets.
  - Null list, empty list, null entries, and invalid compact targets are handled safely.
  - Invalid compact targets contribute zero.

Existing method preserved:

- `workFromTarget(BigInteger target)`
  - Formula: `2^256 / (target + 1)`.
  - Null, zero, or negative targets return `BigInteger.ZERO`.

### ChainTournamentScorer

Added pure scoring API:

- `capTieBreakerScore(double score)`
  - Clamps score to `0.0..1.0`.
  - NaN, negative, and zero values become `0.0`.
  - Infinity and values above `1.0` become `1.0`.

- `score(boolean valid, BigInteger cumulativeWork, long height, double cappedTieBreakerScore, String tipHash)`
  - Creates an immutable `ChainScore`.

- `compare(ChainScore left, ChainScore right)`
  - Positive result means the left chain is better.
  - Negative result means the right chain is better.
  - Zero result means equal score.

- `betterOf(ChainScore left, ChainScore right)`
  - Returns the better score using the same priority.

Added immutable nested value object:

- `ChainTournamentScorer.ChainScore`
  - `boolean valid`
  - `BigInteger cumulativeWork`
  - `long height`
  - `double cappedTieBreakerScore`
  - `String tipHash`

Scorer priority implemented:

1. Valid chain beats invalid chain.
2. Higher cumulative work wins.
3. Higher height wins.
4. Higher capped tie-breaker wins.
5. Lower lexicographic `tipHash` wins as deterministic final tie-breaker.

## No Integration Confirmation

No sync, tournament, DTO, comparator, blockchain, mining, validation, controller, UI, website, block format, transaction format, or JSON DTO integration was performed.

Not edited:

- `Blockchain.java`
- `DataShortBlockchainInformation.java`
- `UtilsResolving.java`
- `HostEndDataShortBComparator.java`
- `TournamentService.java`
- `NodeChecker.java`
- `Mining.java`
- `UtilsBlock.java`
- `Block.java`
- controllers
- UI/templates
- website
- `pom.xml`

Production sync and tournament behavior remains unchanged.

## Tests Added Or Updated

Wallet and server:

- `V35WorkCalculatorTest`
  - lower target produces higher work;
  - higher target produces lower work;
  - same target produces same work;
  - invalid target returns zero;
  - compact target work is positive for valid compact target;
  - cumulative work sums multiple compact targets;
  - invalid compact targets contribute zero.

- `ChainTournamentScorerTest`
  - valid chain beats invalid chain;
  - higher cumulative work beats lower cumulative work even when the lower-work chain has a huge tie-breaker;
  - equal cumulative work uses higher height;
  - equal work and height uses capped tie-breaker;
  - final tie-breaker is deterministic by `tipHash`;
  - huge tie-breaker cannot beat higher cumulative work.

- `V35ParityTest`
  - updated reflection checks for new `V35WorkCalculator` and `ChainTournamentScorer` methods.

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

Cleanup of generated Maven outputs:

```powershell
cd C:\citu
git restore unitedStates_final-master/target unitedStates_storage-master/target
```

## Test Results

Wallet `.\mvnw.cmd test`:

- Result: PASS
- Tests run: 46
- Failures: 0
- Errors: 0
- Skipped: 0

Server `.\mvnw.cmd test`:

- Result: PASS
- Tests run: 46
- Failures: 0
- Errors: 0
- Skipped: 0

## Build Results

Wallet `.\mvnw.cmd clean package`:

- Result: PASS
- Tests run during package: 46
- Failures: 0
- Errors: 0
- Skipped: 0
- Jar built successfully: `target\unitedStates-0.32.216-SNAPSHOT.jar`

Server `.\mvnw.cmd clean package`:

- Result: PASS
- Tests run during package: 46
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

Wallet and server helper implementations are mirrored:

- `V35WorkCalculator.java` behavior matches in wallet and server.
- `ChainTournamentScorer.java` behavior matches in wallet and server.
- `V35WorkCalculatorTest`, `ChainTournamentScorerTest`, and `V35ParityTest` are mirrored.
- Both projects passed the same 46-test V35 suite.

## Behavior Change Confirmation

No production behavior changed.

The new helper logic is pure and currently unused by:

- chain sync;
- host sorting;
- server tournament;
- short chain DTOs;
- mining;
- validation.

V35 remains disabled because `V35_RESCUE_FORK_INDEX` is still `Long.MAX_VALUE` in both projects.

## Risks Before Phase 3B-4B1

1. The helper exists but is not wired; sync and tournament still use legacy random/staking/transaction scoring.
2. `DataShortBlockchainInformation` still has no `cumulativeWork` or `tipHash` field.
3. The tie-breaker cap is local to the pure helper (`0.0..1.0`). Phase 3B-4B1 or later must decide how real stake/transaction metrics are normalized before being passed to this helper.
4. Existing legacy comparator behavior, including the size comparison bug in `HostEndDataShortBComparator`, remains untouched.
5. JSON compatibility for future `cumulativeWork` and `tipHash` fields still needs separate DTO tests.
6. Wallet/server parity must continue to be checked when DTO and comparator wiring begins.

## Recommended Next Phase

Phase 3B-4B1: add `cumulativeWork` and `tipHash` fields to short chain info DTOs, while still not changing comparator behavior.

Recommended constraints:

- keep V35 disabled;
- preserve the existing DTO constructor;
- add overloads/defaults for new fields;
- update clone behavior;
- add JSON compatibility tests for old and new short-info payloads;
- do not wire host comparators or tournament selection until the DTO field behavior is verified.
