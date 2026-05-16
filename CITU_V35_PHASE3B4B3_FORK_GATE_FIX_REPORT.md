# CITU V35 Phase 3B-4B3 Fork Gate Fix Report

Phase: 3B-4B3-FIX  
Project root: `C:\citu`  
Created: 2026-05-16

## Scope

Added a strict V35 activation gate to cumulative-work chain comparison.

This fix prevents remote, old, or malicious nodes from influencing chain priority with positive `cumulativeWork` metadata before V35 is actually active.

No mining, validation, block format, transaction format, DTO field, controller, UI, website, `Blockchain.java`, `DataShortBlockchainInformation.java`, `TournamentService`, `NodeChecker`, or `pom.xml` files were changed.

## V35 Disabled Confirmation

V35 remains disabled in both projects:

- Wallet: `C:\citu\unitedStates_final-master\src\main\java\International_Trade_Union\setings\Seting.java`
  - `V35_RESCUE_FORK_INDEX = Long.MAX_VALUE`
- Server: `C:\citu\unitedStates_storage-master\src\main\java\International_Trade_Union\setings\Seting.java`
  - `V35_RESCUE_FORK_INDEX = Long.MAX_VALUE`

## Exact Files Changed

Wallet:

- `C:\citu\unitedStates_final-master\src\main\java\International_Trade_Union\model\comparator\HostEndDataShortBComparator.java`
- `C:\citu\unitedStates_final-master\src\test\java\International_Trade_Union\model\comparator\HostEndDataShortBComparatorTest.java`

Server:

- `C:\citu\unitedStates_storage-master\src\main\java\International_Trade_Union\model\comparator\HostEndDataShortBComparator.java`
- `C:\citu\unitedStates_storage-master\src\test\java\International_Trade_Union\model\comparator\HostEndDataShortBComparatorTest.java`

Report:

- `C:\citu\CITU_V35_PHASE3B4B3_FORK_GATE_FIX_REPORT.md`

## Exact Methods Changed

Wallet and server `HostEndDataShortBComparator.java`:

- `shouldUseV35Comparison(DataShortBlockchainInformation d1, DataShortBlockchainInformation d2)`
  - Now returns `false` when `Seting.V35_RESCUE_FORK_INDEX == Long.MAX_VALUE`.
  - Now returns `false` unless at least one compared metadata object has `size >= Seting.V35_RESCUE_FORK_INDEX`.
  - Only after those gates can valid positive `cumulativeWork` activate V35 comparison.

Added helper:

- `isAtOrAfterV35(DataShortBlockchainInformation data)`
  - Uses `DataShortBlockchainInformation.getSize()` as the available short-chain height metadata.
  - Null metadata is treated as not at V35.

No `UtilsResolving.java` changes were needed in this fix because it already delegates to `HostEndDataShortBComparator.shouldUseV35Comparison(...)`.

## V35 Activation Gate

Cumulative-work comparison is now possible only when all required conditions are true:

1. `Seting.V35_RESCUE_FORK_INDEX != Long.MAX_VALUE`
2. At least one compared short-chain metadata object has `size >= Seting.V35_RESCUE_FORK_INDEX`
3. At least one compared short-chain metadata object has valid positive decimal `cumulativeWork`

If height/index cannot be determined safely from metadata, the gate fails and legacy comparison is used.

## Legacy Fallback

When V35 is disabled, positive, huge, fake, malformed, invalid, or missing `cumulativeWork` cannot activate V35 comparison.

The comparator falls back to the existing legacy comparison path:

- existing legacy size comparison line remains unchanged;
- `bigRandomNumber` descending;
- `transactions` descending.

The pre-existing legacy size comparison bug remains preserved by design in this phase.

## Remote Metadata Safety

Remote metadata is now safe before V35 activation:

- positive `cumulativeWork`: ignored while fork index is `Long.MAX_VALUE`;
- huge `cumulativeWork`: ignored while fork index is `Long.MAX_VALUE`;
- invalid `cumulativeWork`: parsed as zero and does not crash;
- old DTOs without `cumulativeWork`: continue using legacy fallback;
- metadata below V35 height: cannot activate V35 comparison.

## Tests Added Or Updated

Wallet and server:

- `HostEndDataShortBComparatorTest`
  - `positiveCumulativeWorkDoesNotActivateWhileV35Disabled`
  - `hugeCumulativeWorkBelowV35DoesNotWin`
  - `utilsResolvingUsesLegacyWhenV35DisabledEvenWithPositiveWork`
  - retained fallback coverage for zero, invalid, and old DTO metadata
  - retained direct `compareV35(...)` helper coverage separately from activation

Active fork-path comparator activation could not be tested by changing `Seting.V35_RESCUE_FORK_INDEX` because that would violate this phase's requirement to keep V35 disabled and not change constants. The direct `compareV35(...)` helper and `ChainTournamentScorerTest` still cover the V35 scoring priority itself.

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

Cleanup:

```powershell
cd C:\citu
git restore unitedStates_final-master/target unitedStates_storage-master/target
```

## Test Results

Wallet `.\mvnw.cmd test`:

- Result: PASS
- Tests run: 62
- Failures: 0
- Errors: 0
- Skipped: 0

Server `.\mvnw.cmd test`:

- Result: PASS
- Tests run: 62
- Failures: 0
- Errors: 0
- Skipped: 0

## Build Results

Wallet `.\mvnw.cmd clean package`:

- Result: PASS
- Tests run during package: 62
- Failures: 0
- Errors: 0
- Skipped: 0
- Jar built successfully: `target\unitedStates-0.32.216-SNAPSHOT.jar`

Server `.\mvnw.cmd clean package`:

- Result: PASS
- Tests run during package: 62
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

## Behavior Confirmation

- V35 remains disabled.
- Positive cumulative work cannot affect chain selection while `V35_RESCUE_FORK_INDEX = Long.MAX_VALUE`.
- Legacy comparison is preserved while V35 is disabled.
- Mining behavior did not change.
- Validation behavior did not change.
- Chain metadata fields did not change.
- Server sync and tournament services were not changed.

## Remaining Risks Before TournamentService Review

1. `TournamentService` candidate block priority still needs a separate V35 review.
2. The active V35 comparator gate needs future tests with a real non-`Long.MAX_VALUE` fork index in an activation phase.
3. `DataShortBlockchainInformation.getSize()` is the available short-chain height metadata; if a future DTO adds explicit tip index, the gate should use that exact index.
4. Full peer-sync fixtures are still needed before activation to prove old-node metadata cannot influence upgraded nodes unexpectedly.
5. The legacy comparator size comparison bug remains intentionally unchanged.
