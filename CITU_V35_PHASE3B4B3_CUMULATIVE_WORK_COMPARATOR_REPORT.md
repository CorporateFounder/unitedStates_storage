# CITU V35 Phase 3B-4B3 Cumulative Work Comparator Report

Phase: 3B-4B3  
Project root: `C:\citu`  
Created: 2026-05-16

## Scope

Wired V35 cumulative-work comparison into chain priority helpers in wallet and server, but only when at least one compared short-chain metadata object has valid positive `cumulativeWork`.

This phase does not activate V35 and does not change mining, validation, block format, transaction format, controllers, UI, website, `NodeChecker`, `TournamentService`, or `pom.xml`.

V35 remains disabled:

- Wallet: `Seting.V35_RESCUE_FORK_INDEX = Long.MAX_VALUE`
- Server: `Seting.V35_RESCUE_FORK_INDEX = Long.MAX_VALUE`

## Files Changed

Wallet:

- `C:\citu\unitedStates_final-master\src\main\java\International_Trade_Union\model\comparator\HostEndDataShortBComparator.java`
- `C:\citu\unitedStates_final-master\src\main\java\International_Trade_Union\utils\UtilsResolving.java`
- `C:\citu\unitedStates_final-master\src\test\java\International_Trade_Union\model\comparator\HostEndDataShortBComparatorTest.java`

Server:

- `C:\citu\unitedStates_storage-master\src\main\java\International_Trade_Union\model\comparator\HostEndDataShortBComparator.java`
- `C:\citu\unitedStates_storage-master\src\main\java\International_Trade_Union\utils\UtilsResolving.java`
- `C:\citu\unitedStates_storage-master\src\test\java\International_Trade_Union\model\comparator\HostEndDataShortBComparatorTest.java`

Report:

- `C:\citu\CITU_V35_PHASE3B4B3_CUMULATIVE_WORK_COMPARATOR_REPORT.md`

Note: Phase 3B-4B0 helper files are still present as uncommitted workspace changes from the prior helper phase.

## Exact Methods Changed

Wallet and server `HostEndDataShortBComparator.java`:

- `compare(HostEndDataShortB h1, HostEndDataShortB h2)`
  - Uses V35 comparison only when `shouldUseV35Comparison(...)` is true.
  - Otherwise preserves the legacy comparison path.

Added helper methods:

- `shouldUseV35Comparison(DataShortBlockchainInformation d1, DataShortBlockchainInformation d2)`
- `compareV35(DataShortBlockchainInformation d1, DataShortBlockchainInformation d2)`
- `toChainScore(...)`
- `parseCumulativeWork(...)`
- `tieBreakerScore(...)`
- `normalized(BigDecimal value)`
- `normalized(long value)`

Wallet and server `UtilsResolving.java`:

- `isBig(DataShortBlockchainInformation actual, DataShortBlockchainInformation global)`
  - Uses `HostEndDataShortBComparator.compareV35(global, actual)` only when V35 comparison is active.
  - Otherwise preserves each project file's existing legacy branch.

- `isSmall(DataShortBlockchainInformation expected, DataShortBlockchainInformation actual)`
  - Uses `HostEndDataShortBComparator.compareV35(expected, actual)` only when V35 comparison is active.
  - Otherwise preserves legacy behavior.

## V35 Comparison Activation

V35 comparison activates only when:

```text
parseCumulativeWork(left).signum() > 0 || parseCumulativeWork(right).signum() > 0
```

Missing, empty, zero, negative, or invalid `cumulativeWork` parses as `BigInteger.ZERO`.

If both sides parse to zero, the old legacy comparison path is used.

## Legacy Fallback Preservation

When both compared chains have no valid positive cumulative work:

- `HostEndDataShortBComparator.compare(...)` keeps the existing legacy sort behavior:
  - legacy size comparison line remains unchanged;
  - compares `bigRandomNumber` descending;
  - then compares `transactions` descending.
- `UtilsResolving.isBig(...)` keeps the existing project-specific legacy branch.
  - Wallet legacy branch remains `global.getBigRandomNumber() != actual.getBigRandomNumber()`.
  - Server legacy branch remains `global.getBigRandomNumber() > actual.getBigRandomNumber()`.
- `UtilsResolving.isSmall(...)` keeps the existing legacy size/random checks.

## cumulativeWork Parsing

`cumulativeWork` is parsed as a decimal `BigInteger`.

Safe parsing rules:

- null metadata: zero;
- null string: zero;
- empty/blank string: zero;
- invalid number: zero;
- zero or negative number: zero;
- positive decimal string: positive `BigInteger`.

Invalid metadata does not crash comparison.

## Tie-Breaker Capping

The V35 tie-breaker is derived from existing metadata only after valid status, cumulative work, and height are equal.

Components:

- staking: `staking / (staking + 1)`
- transactions: `transactions / (transactions + 1)`
- random score: `bigRandomNumber / (bigRandomNumber + 1)`

Final score:

```text
capTieBreakerScore((stakeComponent + transactionComponent + randomComponent) / 3)
```

`ChainTournamentScorer.capTieBreakerScore(...)` clamps the final tie-breaker to `0.0..1.0`.

This tie-breaker cannot override cumulative work because `ChainTournamentScorer` compares cumulative work before height and tie-breaker.

## ChainTournamentScorer Priority Used

When V35 comparison is active, the comparator delegates to `ChainTournamentScorer`:

1. Valid chain beats invalid chain.
2. Higher cumulative work wins.
3. Higher height wins.
4. Higher capped tie-breaker wins.
5. Deterministic `tipHash` tie-breaker.

The Java `Comparator` result is inverted so sorted host lists still place the better chain first.

## Tests Added

Wallet and server:

- `HostEndDataShortBComparatorTest`
  - zero cumulative work uses legacy fallback;
  - old DTO without cumulative work does not crash;
  - higher cumulative work wins over huge stake/tx/random metadata;
  - invalid chain loses to valid chain;
  - equal cumulative work uses higher height;
  - equal work and height uses capped tie-breaker;
  - equal score uses deterministic `tipHash`;
  - invalid cumulative work falls back safely when no positive work exists;
  - `UtilsResolving.isBig(...)` uses V35 comparison when positive work exists.

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
- Tests run: 64
- Failures: 0
- Errors: 0
- Skipped: 0

Server `.\mvnw.cmd test`:

- Result: PASS
- Tests run: 64
- Failures: 0
- Errors: 0
- Skipped: 0

## Build Results

Wallet `.\mvnw.cmd clean package`:

- Result: PASS
- Tests run during package: 64
- Failures: 0
- Errors: 0
- Skipped: 0
- Jar built successfully: `target\unitedStates-0.32.216-SNAPSHOT.jar`

Server `.\mvnw.cmd clean package`:

- Result: PASS
- Tests run during package: 64
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

## Behavior Change Confirmation

V35 remains disabled.

No mining behavior changed.

No validation behavior changed.

No block or transaction format changed.

No controller, UI, website, `TournamentService`, or `NodeChecker` code was changed.

Legacy chain comparison is preserved whenever both compared chains have absent, invalid, empty, or zero cumulative work.

## Backward Compatibility Risks

1. Old nodes that omit `cumulativeWork` continue down the legacy path unless compared to a positive-work chain.
2. Invalid `cumulativeWork` strings are treated as zero and cannot crash sorting.
3. Mixed networks after V35 must ensure upgraded nodes provide positive cumulative work, otherwise legacy fallback can still occur between zero-work summaries.
4. The existing legacy comparator size comparison bug remains preserved in the legacy path by design.
5. `TournamentService` still uses legacy candidate block priority and must be reviewed separately.

## Risks Before Phase 3B-4B4

1. Server candidate block tournament still uses legacy `bigRandomWinner`, `hashCompexity`, staking, and transaction count.
2. Full signed chain sync fixtures are still needed before fork activation.
3. Tie-breaker normalization is intentionally simple and capped; future phases may want explicit protocol constants for stake/tx/random tie-breaker weights.
4. Positive cumulative work metadata must be correctly populated after V35 for this comparator to activate.

## Recommended Next Phase

Phase 3B-4B4: review and wire `TournamentService` candidate block priority if needed.

Recommended constraints:

- keep V35 disabled;
- do not change legacy tournament behavior before V35;
- use cumulative work or target-derived work only in V35 branch;
- prove candidate random/staking/transaction metadata cannot beat higher V35 work.
