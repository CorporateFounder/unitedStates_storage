# CITU V35 Phase 3B-4B4B Tournament Service Report

Phase: 3B-4B4B  
Project root: `C:\citu`  
Created: 2026-05-16

## Scope

Wired V35 candidate block priority into the server `TournamentService.sortWinner(...)` path behind a strict fork gate. V35 remains disabled, so live tournament behavior remains legacy.

No wallet source code, mining code, validation code, block format, transaction format, DTO fields, `NodeChecker`, `UtilsResolving`, `HostEndDataShortBComparator`, `Blockchain.java`, `DataShortBlockchainInformation.java`, controllers, UI/templates, website, or `pom.xml` files were changed.

## V35 Disabled Confirmation

V35 remains disabled in both projects:

- Wallet: `C:\citu\unitedStates_final-master\src\main\java\International_Trade_Union\setings\Seting.java`
  - `V35_RESCUE_FORK_INDEX = Long.MAX_VALUE`
- Server: `C:\citu\unitedStates_storage-master\src\main\java\International_Trade_Union\setings\Seting.java`
  - `V35_RESCUE_FORK_INDEX = Long.MAX_VALUE`

## Exact Files Changed

Server:

- `C:\citu\unitedStates_storage-master\src\main\java\International_Trade_Union\model\TournamentService.java`
- `C:\citu\unitedStates_storage-master\src\test\java\International_Trade_Union\model\TournamentServiceTest.java`

Report:

- `C:\citu\CITU_V35_PHASE3B4B4B_TOURNAMENT_SERVICE_REPORT.md`

Wallet:

- No wallet files were changed in this phase.

## Exact Methods Changed

Server `TournamentService.java`:

- `sortWinner(Map<String, Account> finalBalances, List<Block> list)`
  - Keeps existing legacy ranking unless the strict V35 candidate gate is active.
  - Calls V35 sorting only when `shouldUseV35CandidatePriority(...)` returns true.

Added helpers:

- `sortWinnerForForkIndex(Map<String, Account> finalBalances, List<Block> list, long forkIndex)`
  - Test helper for simulated active fork behavior without changing production constants.

- `shouldUseV35CandidatePriority(List<Block> list, long forkIndex)`
  - Requires `forkIndex != Long.MAX_VALUE`.
  - Requires all candidate blocks to be non-null.
  - Requires each candidate index to be at or after the provided fork index.
  - Requires positive V35 work from `V35WorkCalculator.workFromCompactTarget(block.getHashCompexity())`.
  - Requires safely parseable hexadecimal block hash.

- `compareV35Candidate(Map<String, Account> finalBalances, Block left, Block right)`
  - Positive return means the left candidate is better.

- `sortWinnerV35(...)`
- `hasValidV35CandidateMetadata(...)`
- `candidateWork(...)`
- `hashNumber(...)`
- `candidateTieBreaker(...)`
- `safeBigRandomWinner(...)`
- `normalized(BigDecimal value)`
- `normalized(long value)`
- `compareHashText(...)`

## Legacy Behavior Preservation

While `Seting.V35_RESCUE_FORK_INDEX = Long.MAX_VALUE`, `sortWinner(...)` cannot enter the V35 branch.

For valid legacy candidate blocks, the ranking remains:

1. `UtilsUse.bigRandomWinner(...)` descending;
2. `Block.getHashCompexity()` descending;
3. miner staking balance descending;
4. transaction count descending.

`safeBigRandomWinner(...)` wraps the legacy big-random calculation to prevent malformed candidate data from crashing sorting. For valid legacy candidates it returns the same value as the old direct `bigRandomWinner(...)` call.

## Strict V35 Fork Gate

V35 candidate priority is used only if:

1. the fork index passed to the gate is not `Long.MAX_VALUE`;
2. the candidate list is non-null and non-empty;
3. every candidate block is non-null;
4. every candidate block index is `>= forkIndex`;
5. every candidate block has positive V35 work from its compact target;
6. every candidate block hash parses safely as hexadecimal.

Production `sortWinner(...)` passes `Seting.V35_RESCUE_FORK_INDEX`, so the V35 branch is inactive while V35 is disabled.

If the gate cannot determine V35 metadata safely, the method uses legacy ranking.

## V35 Candidate Priority

After V35 only, candidate ordering is:

1. Higher V35 PoW work from `V35WorkCalculator.workFromCompactTarget(block.getHashCompexity())`.
2. Lower numeric block hash.
3. Capped tie-breaker from staking and transaction count.
4. Deterministic hash-text tie-breaker.

Random score is not used in the V35 branch.

Staking and transactions are normalized and capped through `ChainTournamentScorer.capTieBreakerScore(...)`, and they are only compared after equal PoW work and equal numeric hash ordering. They cannot override higher V35 PoW work.

## Tests Added

Server:

- `TournamentServiceTest`
  - `v35DisabledKeepsLegacyTournamentPriority`
    - proves `V35_RESCUE_FORK_INDEX = Long.MAX_VALUE`;
    - proves the V35 gate is false;
    - proves `sortWinner(...)` keeps legacy ordering while V35 is disabled.
  - `simulatedV35CandidatePriorityUsesPowBeforeStake`
    - uses explicit simulated fork index;
    - proves higher V35 work beats a candidate with much higher staking.
  - `simulatedV35CandidatePriorityUsesLowerHashAsTieBreaker`
    - proves deterministic lower hash wins when work is equal.
  - `invalidV35CandidateDataDoesNotCrash`
    - proves invalid hash metadata does not activate V35 priority;
    - proves sorting and direct V35 comparison helper calls do not crash.

No wallet tests were added because `TournamentService` is server-only.

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
git restore unitedStates_final-master/target unitedStates_storage-master/target
```

## Test Results

Server `.\mvnw.cmd test`:

- Result: PASS
- Tests run: 66
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
- Tests run during package: 66
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

- duplicate `spring-boot-starter-test`;
- duplicate/conflicting `h2`;
- duplicate `spring-boot-starter-data-jpa`;
- server duplicate `log4j:log4j`.

No `pom.xml` files were changed.

## Behavior Confirmation

- V35 remains disabled.
- Server live `TournamentService.sortWinner(...)` remains on the legacy path while V35 is disabled.
- Random, stake, and transaction metrics cannot override V35 PoW candidate priority in the V35 branch.
- Mining behavior did not change.
- Block validation behavior did not change.
- Chain selection comparator behavior did not change.
- Wallet code was not changed.

## Remaining Risks Before Jackpot Phase

1. `BasisController.resolve_from_to_block(...)` still has legacy `hashCount` admission logic and should be reviewed separately before activation.
2. Full network/tournament fixtures with signed blocks are still needed before activation.
3. The V35 candidate priority branch is tested through a simulated fork index helper because the real fork index remains disabled.
4. `blockToLiteVersion(...)` still reports legacy winner metrics; future UI/API phases may need V35 target/work display.
5. Jackpot phases must ensure jackpot tickets/payout logic does not reintroduce stake/tx dominance over PoW candidate priority.
