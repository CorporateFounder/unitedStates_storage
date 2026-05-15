# CITU V35 Phase 3B-4B1 Cumulative Work Metadata Report

Phase: 3B-4B1  
Project root: `C:\citu`  
Created: 2026-05-15

## Scope

Added V35 cumulative work metadata fields to the short chain information DTO in both wallet and server projects. This phase does not change chain selection, host comparator behavior, tournament behavior, mining, validation, controllers, UI, website, block format, transaction format, or `pom.xml`.

V35 remains disabled:

- Wallet: `Seting.V35_RESCUE_FORK_INDEX = Long.MAX_VALUE`
- Server: `Seting.V35_RESCUE_FORK_INDEX = Long.MAX_VALUE`

## Files Changed

Wallet:

- `C:\citu\unitedStates_final-master\src\main\java\International_Trade_Union\entity\blockchain\DataShortBlockchainInformation.java`
- `C:\citu\unitedStates_final-master\src\test\java\International_Trade_Union\entity\blockchain\DataShortBlockchainInformationTest.java`

Server:

- `C:\citu\unitedStates_storage-master\src\main\java\International_Trade_Union\entity\blockchain\DataShortBlockchainInformation.java`
- `C:\citu\unitedStates_storage-master\src\test\java\International_Trade_Union\entity\blockchain\DataShortBlockchainInformationTest.java`

Report:

- `C:\citu\CITU_V35_PHASE3B4B1_CUMULATIVE_WORK_METADATA_REPORT.md`

Note: Phase 3B-4B0 helper files are still present as uncommitted workspace changes from the immediately previous phase. This phase itself did not add new edits to those helper files.

## Fields Added

Added to wallet and server `DataShortBlockchainInformation`:

- `public static final String DEFAULT_CUMULATIVE_WORK = "0"`
- `public static final String DEFAULT_TIP_HASH = ""`
- `private String cumulativeWork = DEFAULT_CUMULATIVE_WORK`
- `private String tipHash = DEFAULT_TIP_HASH`

Rationale:

- `cumulativeWork` is a string because V35 cumulative work is a large integer that may exceed JSON-safe numeric size.
- `"0"` is the chosen backward-compatible default for legacy/pre-V35 chains.
- `tipHash` defaults to an empty string until chain metadata population is safely wired.

## Constructor And Clone Compatibility

Preserved existing legacy constructor:

```java
DataShortBlockchainInformation(long size, boolean isValidation, long hashCount, BigDecimal staking, long transactions, int bigRandomNumber)
```

Added overload:

```java
DataShortBlockchainInformation(long size,
                               boolean isValidation,
                               long hashCount,
                               BigDecimal staking,
                               long transactions,
                               int bigRandomNumber,
                               String cumulativeWork,
                               String tipHash)
```

Updated `clone()` to preserve:

- all legacy fields;
- `cumulativeWork`;
- `tipHash`.

Null `cumulativeWork` is normalized to `"0"`. Null `tipHash` is normalized to `""`.

## Blockchain.shortCheck Status

`Blockchain.shortCheck(...)` was not changed in this phase.

Reason:

- V35 remains disabled with `V35_RESCUE_FORK_INDEX = Long.MAX_VALUE`.
- There is no active real fork boundary for computing mixed legacy/V35 cumulative work from chain context.
- Computing true V35 cumulative work now would require wiring block-index gated metadata population across `shortCheck(...)`, `checkFromFile(...)`, and `rollBackShortCheck(...)`.
- That work belongs in a later phase after DTO compatibility is verified.

## cumulativeWork Population

Current behavior:

- Default value: `"0"`.
- Legacy constructor value: `"0"`.
- Extended constructor can carry a large decimal cumulative work string.
- Old JSON without `cumulativeWork` deserializes to `"0"`.

Phase 3B-4B2 or a later metadata population phase must:

- compute V35 per-block work from compact target only after `ProtocolVersion.isV35(index)`;
- avoid reinterpreting legacy pre-V35 `hashCompexity`;
- update `shortCheck(...)`, `checkFromFile(...)`, and `rollBackShortCheck(...)` consistently.

## tipHash Population

Current behavior:

- Default value: `""`.
- Legacy constructor value: `""`.
- Extended constructor can carry a deterministic tip hash string.
- Old JSON without `tipHash` deserializes to `""`.

`tipHash` is not populated from `Blockchain.shortCheck(...)` yet. Future wiring should set it to the latest valid block hash when available, without changing validation or comparator behavior.

## Chain Selection Behavior

No chain selection behavior changed.

Not edited:

- `UtilsResolving.java`
- `HostEndDataShortBComparator.java`
- `TournamentService.java`
- `NodeChecker.java`
- controllers

The existing legacy host priority, random/staking/transaction scoring, and tournament behavior remain unchanged.

## Tests Added

Wallet and server:

- `DataShortBlockchainInformationTest`
  - default construction uses safe V35 metadata defaults;
  - legacy constructor preserves old fields and defaults V35 metadata;
  - extended constructor stores a very large `BigInteger` decimal string and `tipHash`;
  - `clone()` preserves V35 metadata;
  - JSON round trip preserves V35 metadata;
  - old JSON without V35 metadata uses defaults.

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
- Tests run: 52
- Failures: 0
- Errors: 0
- Skipped: 0

Server `.\mvnw.cmd test`:

- Result: PASS
- Tests run: 52
- Failures: 0
- Errors: 0
- Skipped: 0

## Build Results

Wallet `.\mvnw.cmd clean package`:

- Result: PASS
- Tests run during package: 52
- Failures: 0
- Errors: 0
- Skipped: 0
- Jar built successfully: `target\unitedStates-0.32.216-SNAPSHOT.jar`

Server `.\mvnw.cmd clean package`:

- Result: PASS
- Tests run during package: 52
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

## Backward Compatibility Risks

1. New JSON fields may be visible to older nodes. The DTO keeps old fields and old constructor behavior, but mixed-version network behavior still needs integration testing.
2. Old JSON without `cumulativeWork` and `tipHash` is tested and deserializes safely to `"0"` and `""`.
3. The new fields are not yet populated from chain scan logic, so consumers must not rely on them for selection until later wiring.
4. `tipHash = ""` is a placeholder for legacy/default DTOs; future comparator code must handle empty tip hash deterministically.
5. `cumulativeWork = "0"` is safe for legacy/pre-V35, but after V35 upgraded nodes must avoid allowing missing/zero work from old nodes to beat a valid cumulative-work chain.

## Risks Before Phase 3B-4B2

1. `Blockchain.shortCheck(...)`, `checkFromFile(...)`, and `rollBackShortCheck(...)` do not populate or adjust V35 work yet.
2. Chain comparators still ignore `cumulativeWork` and `tipHash`.
3. The existing legacy comparator size comparison bug remains untouched.
4. Wallet and server DTOs must remain synchronized when metadata population begins.
5. Future work must compute V35 cumulative work only for V35 blocks and must not reinterpret old `hashCompexity` values.

## Recommended Next Phase

Phase 3B-4B2: wire V35 comparator priority behind the fork gate, still disabled.

Recommended constraints:

- keep `V35_RESCUE_FORK_INDEX = Long.MAX_VALUE`;
- do not change legacy comparator behavior before V35;
- use `ChainTournamentScorer` only in V35 branch;
- handle `"0"`/empty metadata safely;
- add comparator tests proving higher cumulative work beats random, staking, and transaction tie-breakers.
