# CITU V35 Phase 3B-4B4D BasisController Resolve Fix Report

Phase: 3B-4B4D  
Project root: `C:\citu`  
Created: 2026-05-16

## Scope

Wired server `BasisController.resolve_conflict(...)` resolve/admission logic so V35 cumulative work can decide remote block admission after the V35 fork gate.

V35 remains disabled. Current live behavior remains legacy because `Seting.V35_RESCUE_FORK_INDEX = Long.MAX_VALUE`.

No wallet source code, mining behavior, block validation behavior outside resolve/admission routing, block format, transaction format, DTO fields, `HostEndDataShortBComparator`, `UtilsResolving`, `Blockchain.java`, `TournamentService`, `NodeChecker`, UI/templates, website, or `pom.xml` files were changed.

## V35 Disabled Confirmation

V35 remains disabled in both projects:

- Wallet: `C:\citu\unitedStates_final-master\src\main\java\International_Trade_Union\setings\Seting.java`
  - `V35_RESCUE_FORK_INDEX = Long.MAX_VALUE`
- Server: `C:\citu\unitedStates_storage-master\src\main\java\International_Trade_Union\setings\Seting.java`
  - `V35_RESCUE_FORK_INDEX = Long.MAX_VALUE`

## Exact Files Changed

Server:

- `C:\citu\unitedStates_storage-master\src\main\java\International_Trade_Union\controllers\BasisController.java`
- `C:\citu\unitedStates_storage-master\src\test\java\International_Trade_Union\controllers\BasisControllerResolveAdmissionTest.java`

Report:

- `C:\citu\CITU_V35_PHASE3B4B4D_BASISCONTROLLER_RESOLVE_FIX_REPORT.md`

Wallet:

- No wallet files were changed in this phase.

## Exact Methods Changed

Server `BasisController.java`:

- `resolve_conflict(SendBlocksEndInfo sendBlocksEndInfo)`
  - Replaced the raw `checkBlock.getHashCompexity() < Seting.V34_MIN_DIFF` condition with `isResolveBlockBelowMinimumDifficulty(checkBlock)`.
  - Replaced the raw admission condition:
    - `temp.getSize() > shortDataBlockchain.getSize()`
    - `temp.getHashCount() >= shortDataBlockchain.getHashCount()`
  - with `shouldAcceptResolveCandidate(shortDataBlockchain, temp)`.

Added package-visible/testable helpers:

- `shouldAcceptResolveCandidate(DataShortBlockchainInformation local, DataShortBlockchainInformation candidate)`
- `shouldAcceptResolveCandidate(DataShortBlockchainInformation local, DataShortBlockchainInformation candidate, long forkIndex)`
- `isLegacyResolveAdmissionPreferred(DataShortBlockchainInformation local, DataShortBlockchainInformation candidate)`
- `shouldUseV35ResolveAdmission(DataShortBlockchainInformation local, DataShortBlockchainInformation candidate, long forkIndex)`
- `compareV35ResolveAdmission(DataShortBlockchainInformation left, DataShortBlockchainInformation right)`
- `isResolveBlockBelowMinimumDifficulty(Block block)`
- `isResolveBlockBelowMinimumDifficulty(Block block, long forkIndex)`

Added private helpers:

- `toResolveChainScore(DataShortBlockchainInformation data)`
- `isShortInfoAtOrAfterFork(DataShortBlockchainInformation data, long forkIndex)`
- `parseResolveCumulativeWork(DataShortBlockchainInformation data)`

## Legacy Resolve Behavior Preservation

While V35 is disabled or the compared metadata is pre-V35, `shouldAcceptResolveCandidate(...)` falls back to the original legacy admission rule:

```java
candidate.getSize() > local.getSize()
        && candidate.getHashCount() >= local.getHashCount()
```

Since production passes `Seting.V35_RESCUE_FORK_INDEX`, and that constant remains `Long.MAX_VALUE`, the V35 resolve branch is unreachable in live behavior.

## V35 Fork Gate

The V35 resolve/admission branch is used only when all of these are true:

1. `forkIndex != Long.MAX_VALUE`
2. at least one compared short-chain metadata object has `size >= forkIndex`
3. at least one compared metadata object has valid positive decimal `cumulativeWork`

If any condition fails, resolve admission uses legacy `size` plus `hashCount` behavior.

Invalid, missing, empty, zero, or negative cumulative work parses as `BigInteger.ZERO` and does not crash.

## Cumulative Work Admission Behavior

When the V35 gate is active, admission uses `ChainTournamentScorer` with:

1. valid chain status
2. higher `cumulativeWork`
3. higher height/size
4. deterministic `tipHash`

For this resolve path the tie-breaker score is deliberately `0.0`; staking, transaction count, random score, and `hashCount` are not used to override cumulative work in the V35 branch.

This means after V35:

- higher cumulative work wins;
- lower cumulative work loses even with higher legacy `hashCount`;
- invalid cumulative work cannot win against valid positive local cumulative work;
- height is only used after equal cumulative work;
- `hashCount`, random, staking, and transaction count cannot override cumulative work.

## V34_MIN_DIFF Handling

The legacy `V34_MIN_DIFF` pre-check was preserved before V35 through:

```java
isResolveBlockBelowMinimumDifficulty(checkBlock)
```

Production behavior:

- before V35 / disabled: still rejects blocks with `hashCompexity < Seting.V34_MIN_DIFF`
- after V35: does not apply `V34_MIN_DIFF` to `hashCompexity` as if it were legacy difficulty

This avoids treating V35 compact target values as legacy 100-level difficulty.

## Tests Added

Server:

- `BasisControllerResolveAdmissionTest`
  - `v35DisabledKeepsLegacyResolveAdmission`
  - `hugeFakeCumulativeWorkBeforeV35DoesNotBypassLegacyAdmission`
  - `invalidCumulativeWorkDoesNotCrashOrWinAgainstValidV35Work`
  - `simulatedV35ResolveAdmissionPrefersHigherCumulativeWorkBeforeHashCount`
  - `simulatedV35ResolveAdmissionRejectsLowerWorkEvenWithHigherHashCountAndSize`
  - `simulatedV35ResolveAdmissionUsesSizeWhenCumulativeWorkIsEqual`
  - `legacyV34MinimumDifficultyStillAppliesBeforeV35`
  - `v35CompactTargetDoesNotUseLegacyV34MinimumDifficultyFloor`

Full controller endpoint fixtures were not added in this phase because `resolve_conflict(...)` depends on service state, file-backed chain state, signatures, balances, `winnerList`, and propagation calls. The new tests cover the newly added decision helpers directly.

## Verification Commands Run

Server:

```powershell
cd C:\citu\unitedStates_storage-master
.\mvnw.cmd test
.\mvnw.cmd clean package
```

Wallet:

```powershell
cd C:\citu\unitedStates_final-master
.\mvnw.cmd test
.\mvnw.cmd clean package
```

Cleanup:

```powershell
cd C:\citu
git restore unitedStates_storage-master/target unitedStates_final-master/target
```

## Test Results

Server `.\mvnw.cmd test`:

- Result: PASS
- Tests run: 74
- Failures: 0
- Errors: 0
- Skipped: 0

Wallet `.\mvnw.cmd test`:

- Result: PASS
- Tests run: 62
- Failures: 0
- Errors: 0
- Skipped: 0

## Build Results

Server `.\mvnw.cmd clean package`:

- Result: PASS
- Tests run during package: 74
- Failures: 0
- Errors: 0
- Skipped: 0
- Jar built successfully: `target\unitedStates-0.0.423-SNAPSHOT.jar`

Wallet `.\mvnw.cmd clean package`:

- Result: PASS
- Tests run during package: 62
- Failures: 0
- Errors: 0
- Skipped: 0
- Jar built successfully: `target\unitedStates-0.32.216-SNAPSHOT.jar`

Pre-existing Maven warnings remain:

- duplicate `spring-boot-starter-test`
- duplicate/conflicting `h2`
- duplicate `spring-boot-starter-data-jpa`
- server duplicate `log4j:log4j`

No `pom.xml` files were changed.

## Behavior Confirmation

- V35 remains disabled.
- Server resolve admission remains legacy while V35 is disabled.
- Wallet code was not changed.
- Mining behavior did not change.
- Block format did not change.
- Transaction format did not change.
- DTO fields did not change.
- Existing comparator, `UtilsResolving`, `Blockchain`, `TournamentService`, and `NodeChecker` behavior was not changed.
- After V35, resolve admission can no longer use legacy `hashCount` to override valid cumulativeWork comparison.

## Remaining Risks Before Jackpot Phase

1. Full endpoint/integration fixtures for `resolve_conflict(...)` are still needed before activation.
2. Active V35 behavior is tested through explicit helper fork-index parameters because the real fork index remains disabled.
3. `DataShortBlockchainInformation.getSize()` remains the available short-chain height indicator; if a future explicit tip index is added, the resolve gate should use the explicit tip index.
4. The existing controller log expression around wrong-block diagnostics remains awkward and was not refactored in this phase.
5. Broader network simulations should still prove old/malicious nodes cannot exploit stale metadata or partial chain data during V35 activation.

## Recommended Next Phase

Proceed to jackpot planning/implementation phases only after one more activation-readiness review confirms all chain-priority paths are cumulativeWork-gated:

- `HostEndDataShortBComparator`
- `UtilsResolving.isBig/isSmall`
- `TournamentService.sortWinner`
- `BasisController.resolve_conflict`
