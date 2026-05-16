# CITU V35 Phase 3B-4B2 Cumulative Work Metadata Population Report

Phase: 3B-4B2  
Project root: `C:\citu`  
Created: 2026-05-15

## Scope

Populated V35 short-chain metadata in wallet and server `Blockchain.java` without changing chain selection, comparator behavior, server sync, tournament behavior, mining, validation, controllers, UI, website, block format, transaction format, or `pom.xml`.

V35 remains disabled:

- Wallet: `Seting.V35_RESCUE_FORK_INDEX = Long.MAX_VALUE`
- Server: `Seting.V35_RESCUE_FORK_INDEX = Long.MAX_VALUE`

## Files Changed

Wallet:

- `C:\citu\unitedStates_final-master\src\main\java\International_Trade_Union\entity\blockchain\Blockchain.java`
- `C:\citu\unitedStates_final-master\src\test\java\International_Trade_Union\entity\blockchain\BlockchainV35MetadataTest.java`

Server:

- `C:\citu\unitedStates_storage-master\src\main\java\International_Trade_Union\entity\blockchain\Blockchain.java`
- `C:\citu\unitedStates_storage-master\src\test\java\International_Trade_Union\entity\blockchain\BlockchainV35MetadataTest.java`

Report:

- `C:\citu\CITU_V35_PHASE3B4B2_CUMULATIVE_WORK_METADATA_POPULATION_REPORT.md`

Note: uncommitted Phase 3B-4B0 helper changes are still present in the workspace. This phase added metadata population in `Blockchain.java` and new metadata helper tests.

## Exact Methods Changed

Wallet and server `Blockchain.java`:

- `shortCheck(...)`
  - Carries incoming `DataShortBlockchainInformation.cumulativeWork`.
  - Updates `tipHash` to the latest processed block hash when a block hash is available.
  - Adds V35 work only for blocks where `ProtocolVersion.isV35(block.getIndex())` is true.
  - Returns short-info DTOs with legacy fields plus `cumulativeWork` and `tipHash`.

- `checkFromFile(...)`
  - Initializes `cumulativeWork` as `BigInteger.ZERO`.
  - Initializes `tipHash` as `DataShortBlockchainInformation.DEFAULT_TIP_HASH`.
  - Updates `tipHash` from each parsed block hash when available.
  - Adds V35 work only for V35 blocks.
  - Returns short-info DTOs with metadata populated.

- `rollBackShortCheck(...)`
  - Parses incoming `cumulativeWork`.
  - Subtracts V35 work for rolled-back V35 blocks.
  - Clamps negative cumulative work back to zero.
  - Returns `tipHash` as empty because the remaining latest block is not available from the rollback method inputs.

Helper methods added to wallet and server `Blockchain.java`:

- `addV35Work(BigInteger currentWork, Block block)`
- `v35Work(Block block)`
- `latestTipHash(String currentTipHash, Block block)`
- `shortInfo(...)`
- `parseCumulativeWork(String cumulativeWork)`
- `cumulativeWorkString(BigInteger cumulativeWork)`
- `safeMetadataString(String value)`

## cumulativeWork Population

`cumulativeWork` is carried as a decimal string in `DataShortBlockchainInformation`.

Population rule:

```text
if ProtocolVersion.isV35(block.index):
    cumulativeWork += V35WorkCalculator.workFromCompactTarget(block.hashCompexity)
else:
    cumulativeWork += 0
```

Current result while V35 is disabled:

- `V35_RESCUE_FORK_INDEX = Long.MAX_VALUE`
- normal current and historical blocks are legacy
- `ProtocolVersion.isV35(index)` is false
- current chains produce `cumulativeWork = "0"`

Invalid or undecodable compact target behavior:

- `V35WorkCalculator.workFromCompactTarget(...)` returns `BigInteger.ZERO`.
- This phase does not change validation logic or reject blocks based on metadata population.

## tipHash Population

`tipHash` is populated as the latest block hash seen by the metadata path:

- if `block.getHashBlock()` is non-null, `tipHash = block.getHashBlock()`;
- if block or hash is unavailable, the previous metadata value is preserved;
- rollback returns empty `tipHash` because the method does not receive the new latest remaining block.

## Chain Selection Behavior

No chain selection behavior changed.

Not edited:

- `UtilsResolving.java`
- `HostEndDataShortBComparator.java`
- `TournamentService.java`
- `NodeChecker.java`
- controllers

Comparator, sync, and tournament code still ignore `cumulativeWork` and `tipHash`.

## Validation And Mining Behavior

No validation or mining behavior changed.

Legacy metrics remain unchanged:

- `hashCount`
- `staking`
- `transactions`
- `bigRandomNumber`
- `size`
- `isValidation`

No legacy `hashCompexity` value is reinterpreted as compact target before V35.

## Tests Added

Wallet and server:

- `BlockchainV35MetadataTest`
  - `v35Work(...)` returns zero while V35 is disabled;
  - `addV35Work(...)` preserves current work while V35 is disabled;
  - `latestTipHash(...)` uses block hash when available;
  - `latestTipHash(...)` preserves current value when block/hash is unavailable.

Existing tests also continue to cover:

- short-info default `cumulativeWork = "0"`;
- large decimal cumulative work strings;
- JSON round-trip behavior;
- V35 work calculator compact-target summing.

## Direct shortCheck Fixture Status

Direct full `Blockchain.shortCheck(...)` fixture tests were not added in this phase.

Blockers:

- `shortCheck(...)` calls full `UtilsBlock.validationOneBlock(...)`;
- direct fixtures require valid signed transactions, balances, previous block context, and validation-side state;
- constructing that fixture safely would be broader than metadata-only work.

Instead, the pure metadata helpers used by `shortCheck(...)`, `checkFromFile(...)`, and rollback were covered directly.

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
- Tests run: 55
- Failures: 0
- Errors: 0
- Skipped: 0

Server `.\mvnw.cmd test`:

- Result: PASS
- Tests run: 55
- Failures: 0
- Errors: 0
- Skipped: 0

## Build Results

Wallet `.\mvnw.cmd clean package`:

- Result: PASS
- Tests run during package: 55
- Failures: 0
- Errors: 0
- Skipped: 0
- Jar built successfully: `target\unitedStates-0.32.216-SNAPSHOT.jar`

Server `.\mvnw.cmd clean package`:

- Result: PASS
- Tests run during package: 55
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

## Risks Before Phase 3B-4B3

1. `cumulativeWork` is populated but still unused by comparators and sync.
2. Full signed `shortCheck(...)` fixtures are still needed before activation-level confidence.
3. `rollBackShortCheck(...)` cannot know the new latest remaining block hash from current inputs, so it returns an empty `tipHash`.
4. Missing/zero cumulative work from old nodes must be handled carefully before V35 comparator wiring.
5. Existing legacy comparator behavior and the size comparison bug remain untouched.

## Recommended Next Phase

Phase 3B-4B3: wire V35 comparator priority behind the fork gate, still disabled.

Recommended constraints:

- keep `V35_RESCUE_FORK_INDEX = Long.MAX_VALUE`;
- do not change legacy comparator behavior before V35;
- use `ChainTournamentScorer` only in the V35 branch;
- ensure random, staking, and transaction metrics cannot beat higher cumulative work after V35;
- add comparator tests for missing/zero cumulative work and deterministic `tipHash`.
