# CITU V35 Phase 3B-4A Cumulative Work / Tournament Wiring Plan

Phase: 3B-4A planning only  
Project root: `C:\citu`  
Created: 2026-05-15

## Scope

This report maps the current chain scoring, server sync, and tournament behavior and proposes the safest Phase 3B-4B implementation path for V35 cumulative work. No Java source code, tests, UI, website files, block format, transaction format, mining behavior, validation behavior, or tournament behavior is changed in this phase.

V35 remains disabled by the existing fork gate:

- Wallet: `unitedStates_final-master\src\main\java\International_Trade_Union\Seting.java`
- Server: `unitedStates_storage-master\src\main\java\International_Trade_Union\Seting.java`
- `V35_RESCUE_FORK_INDEX = Long.MAX_VALUE`

## Reports Read

- `C:\citu\CITU_V35_PHASE3A_POW_TARGET_MAPPING_PLAN.md`
- `C:\citu\CITU_V35_PHASE3B0_POW_HELPERS_REPORT.md`
- `C:\citu\CITU_V35_PHASE3B1_POW_VALIDATION_REPORT.md`
- `C:\citu\CITU_V35_PHASE3B2_MINING_TARGET_REPORT.md`
- `C:\citu\CITU_V35_PHASE3B3_TARGET_ADJUSTMENT_REPORT.md`

Relevant established V35 helper behavior:

- `V35TargetDifficulty.decodeCompactTarget(long compact)` decodes the V35 compact target stored in `Block.hashCompexity`.
- `V35PowValidator.isHashUnderTarget(String hashHex, BigInteger target)` validates `hashNumber <= target`.
- `V35WorkCalculator.workFromTarget(BigInteger target)` calculates per-block work as `2^256 / (target + 1)`.
- V35 mining and validation branches are behind `ProtocolVersion.isV35(index)`.

## Exact Files And Methods Found

### Server Project

Root: `C:\citu\unitedStates_storage-master`

#### Chain Metadata And Validation

File: `src\main\java\International_Trade_Union\entity\blockchain\Blockchain.java`

- `shortCheck(Block prevBlock, List<Block> blocks, DataShortBlockchainInformation data, List<Block> tempList, Map<String, Account> balances, List<String> sign, Map<String, Account> balanceForValidation, List<String> signaturesNotTakenIntoAccount)` around line 129.
  - Uses incoming `DataShortBlockchainInformation`.
  - Accumulates:
    - `hashcount += UtilsUse.powerDiff(block.getHashCompexity())`
    - `staking = staking.add(stakingForDataShort(miner.getDigitalStakingBalance()))`
    - `bigRandomNumber += UtilsUse.bigRandomWinner(block, miner)`
    - `transactions += block.getDtoTransactions().size()`
  - Calls `UtilsBlock.validationOneBlock(...)` before updating metadata.
  - Returns a new `DataShortBlockchainInformation(size, validation, hashcount, staking, transactions, bigRandomNumber)`.

- `checkFromFile(...)` around line 250.
  - Rebuilds local short chain metadata from local block files.
  - Accumulates `hashCount += UtilsUse.powerDiff(block.getHashCompexity())`.
  - Must calculate the same V35 cumulative work as `shortCheck(...)` in Phase 3B-4B.

- `rollBackShortCheck(List<Block> blocks, DataShortBlockchainInformation data, Map<String, Account> balances)` around line 749.
  - Subtracts legacy metadata during rollback:
    - `hashcount -= UtilsUse.powerDiff(blocks.get(i).getHashCompexity())`
    - subtracts staking, transaction count, and big-random score.
  - Must subtract V35 work when Phase 3B-4B adds cumulative work.

#### Short Chain Information DTO

File: `src\main\java\International_Trade_Union\entity\blockchain\DataShortBlockchainInformation.java`

Current fields:

- `long size`
- `boolean isValidation`
- `long hashCount`
- `BigDecimal staking`
- `long transactions`
- `int bigRandomNumber`

Current constructor:

- `DataShortBlockchainInformation(long size, boolean isValidation, long hashCount, BigDecimal staking, long transactions, int bigRandomNumber)`

Current helper:

- `clone()`

There is no cumulative work field today.

#### Server Sync / Host Priority

File: `src\main\java\International_Trade_Union\utils\UtilsResolving.java`

- `isBig(DataShortBlockchainInformation actual, DataShortBlockchainInformation global)` around line 666.
  - Current behavior: returns true when `global.getBigRandomNumber() > actual.getBigRandomNumber()`.
  - Does not compare cumulative work.
  - Does not compare true PoW work.

- `isSmall(DataShortBlockchainInformation expected, DataShortBlockchainInformation actual)` around line 677.
  - Current behavior: returns true when actual local size is lower than expected or local big-random score is lower.

- `sortPriorityHost(Set<String> hosts)` around line 1122.
  - Builds a list of remote hosts.
  - Fetches `/datashort` from each host via `fetchDataShortBlockchainInformation(host)`.
  - Filters to `global != null && global.isValidation()`.
  - Sorts with `new HostEndDataShortBComparator()`.

File: `src\main\java\International_Trade_Union\model\HostEndDataShortB.java`

- Holds:
  - `String host`
  - `DataShortBlockchainInformation dataShortBlockchainInformation`

File: `src\main\java\International_Trade_Union\model\comparator\HostEndDataShortBComparator.java`

- `compare(HostEndDataShortB h1, HostEndDataShortB h2)` around line 11.
- Current comparator behavior:
  - Intended to compare size first, but the condition is `if(d1.getSize() != d1.getSize())`, which is always false.
  - Then compares `bigRandomNumber` descending.
  - Then compares `transactions` descending.
- This means host priority is currently dominated by random score and transaction count, not height or work.

File: `src\main\java\International_Trade_Union\model\NodeChecker.java`

- `checkNodes(List<HostEndDataShortB> hosts)` around line 29.
  - Fetches `/getNodes` from priority hosts and readiness-checks nodes.
- `initiateProcess(List<HostEndDataShortB> sortPriorityHost)` around line 95.
  - Uses the already sorted host list to coordinate readiness.
  - Does not itself score chains.

#### Block Candidate Tournament

File: `src\main\java\International_Trade_Union\model\TournamentService.java`

- `sortWinner(Map<String, Account> finalBalances, List<Block> list)` around line 79.
  - Sorts block candidates by:
    1. `UtilsUse.bigRandomWinner(block, miner)` descending
    2. `Block::getHashCompexity` descending
    3. miner `digitalStakingBalance` descending
    4. transaction count descending
  - This is the current one-block tournament policy.

- `validateAndAddBlock(...)` around line 261.
  - Validates a candidate block by calling `Blockchain.shortCheck(...)`.
  - Adds valid candidate blocks to `tempWinnerSet`.

- Main tournament selection around line 374.
  - Calls `winnerList = sortWinner(finalBalances, list)`.
  - Picks `winnerList.get(0)`.
  - Validates the selected winner again through `Blockchain.shortCheck(...)`.

#### Score Helpers

File: `src\main\java\International_Trade_Union\utils\UtilsUse.java`

- `powerDiff(long diff)` around line 384.
  - Currently returns `diff`.
  - It does not calculate Bitcoin-like work.

- `bigRandomWinner(Block actual, Account miner)` around line 396.
  - Produces a score from block hash-derived randomness, `hashCompexity`, miner staking, and transaction count.
  - Current behavior can allow random/staking/transaction scores to dominate priority decisions.
  - Must not be primary after V35.

#### API Exposure

File: `src\main\java\International_Trade_Union\controllers\BasisController.java`

- `/datashort` endpoint returns the current `DataShortBlockchainInformation`.
- Current code also has checks around `shortDataBlockchain.getHashCount()`.
- Phase 3B-4B should avoid changing controller behavior unless a minimal compatibility guard is required for the new field.

#### Existing V35 Work Helper

File: `src\main\java\International_Trade_Union\protocol\v35\V35WorkCalculator.java`

- `workFromTarget(BigInteger target)`
- Must be reused for per-block V35 cumulative work.

### Wallet Project

Root: `C:\citu\unitedStates_final-master`

The wallet project contains duplicated or parallel versions of several chain metadata and sync classes:

- `src\main\java\International_Trade_Union\entity\blockchain\Blockchain.java`
- `src\main\java\International_Trade_Union\entity\blockchain\DataShortBlockchainInformation.java`
- `src\main\java\International_Trade_Union\utils\UtilsResolving.java`
- `src\main\java\International_Trade_Union\model\HostEndDataShortB.java`
- `src\main\java\International_Trade_Union\model\comparator\HostEndDataShortBComparator.java`
- `src\main\java\International_Trade_Union\model\NodeChecker.java`
- `src\main\java\International_Trade_Union\utils\UtilsUse.java`
- `src\main\java\International_Trade_Union\protocol\v35\V35WorkCalculator.java`

No wallet-side `TournamentService.java` was identified in the expected server tournament location. The wallet therefore appears to participate in chain metadata, validation, and host priority logic, while server-side block-candidate tournament selection is server-only.

Phase 3B-4B must keep wallet/server shared chain metadata behavior synchronized wherever the duplicated classes exist.

## Current Legacy Tournament Flow

### Candidate Block Tournament

The server tournament flow is:

1. Candidate blocks are gathered by `TournamentService`.
2. Each candidate is validated through `Blockchain.shortCheck(...)`.
3. Valid candidates enter `tempWinnerSet`.
4. `TournamentService.sortWinner(...)` sorts candidates by:
   - `bigRandomWinner`
   - `hashCompexity`
   - miner staking
   - transaction count
5. The first sorted block is selected.
6. The selected block is validated again through `Blockchain.shortCheck(...)`.

This is not Bitcoin-like longest-work selection. It is a legacy tournament policy using random, staking, transactions, and a linear difficulty-like field.

### Current Tournament Metrics

- `bigRandomWinner`: hash-derived random score plus difficulty/stake/tx scoring.
- `hashCompexity`: current legacy difficulty-like block field.
- `digitalStakingBalance`: miner staking balance.
- transaction count: number of DTO transactions in the block.

## Current Server Sync Flow

The server sync and host priority flow is:

1. `UtilsResolving.sortPriorityHost(Set<String> hosts)` builds candidate remote hosts.
2. Each host is queried for `/datashort`.
3. Remote `DataShortBlockchainInformation` must exist and be valid.
4. `HostEndDataShortBComparator.compare(...)` sorts hosts.
5. `NodeChecker.initiateProcess(...)` receives that sorted list and readiness-checks hosts.
6. Other logic can use `UtilsResolving.isBig(...)` and `UtilsResolving.isSmall(...)` to determine whether remote short-chain data is better or worse.

Current sync priority is not cumulative-work based.

## Current Short Blockchain Info Structure

`DataShortBlockchainInformation` currently carries:

- `size`: chain height/size summary.
- `isValidation`: whether the summarized chain segment is valid.
- `hashCount`: sum of `UtilsUse.powerDiff(hashCompexity)`, currently linear.
- `staking`: accumulated staking score.
- `transactions`: accumulated transaction count.
- `bigRandomNumber`: accumulated random/tournament score.

Current limitations:

- No cumulative PoW work.
- No V35 compact target work summary.
- No deterministic tip hash tie-breaker field.
- `hashCount` cannot safely represent V35 work because V35 work is a large `BigInteger`, while `hashCount` is a `long` and currently has legacy semantics.

## Current Chain Priority Decisions

Current chain or host priority is decided in these places:

- `HostEndDataShortBComparator.compare(...)`
  - Sorts priority hosts.
  - Has a size comparison bug: `d1.getSize() != d1.getSize()` is always false.
  - Then uses `bigRandomNumber` and transaction count.

- `UtilsResolving.isBig(...)`
  - Uses `bigRandomNumber` only.

- `UtilsResolving.isSmall(...)`
  - Uses size and `bigRandomNumber`.

- `TournamentService.sortWinner(...)`
  - Chooses a block candidate using big-random, `hashCompexity`, staking, and transaction count.

These are the exact decision points Phase 3B-4B must gate for V35.

## Proposed V35 Cumulative Work Design

### Per-Block Work

For each V35 block:

1. Read the compact target from `block.getHashCompexity()`.
2. Decode it using `V35TargetDifficulty.decodeCompactTarget(long compact)`.
3. If decoding fails or the target is invalid, the block/summary must be invalid.
4. Calculate work:

```text
work = 2^256 / (target + 1)
```

5. Use `V35WorkCalculator.workFromTarget(BigInteger target)` for this calculation.

### Summed Cumulative Work

For a chain summary:

```text
cumulativeWork = sum(workFromTarget(decodedTarget)) for V35 blocks only
```

Recommended semantics:

- Before `V35_RESCUE_FORK_INDEX`, keep legacy scoring exactly unchanged.
- At and after `V35_RESCUE_FORK_INDEX`, calculate V35 cumulative work only for V35 blocks.
- Do not reinterpret old pre-V35 blocks as target-based PoW.
- Do not use legacy `hashCount` as cumulative work.
- Continue populating `hashCount`, `staking`, `transactions`, and `bigRandomNumber` for legacy compatibility and diagnostics, but do not use them as primary V35 chain priority.

### Legacy Pre-V35 Treatment

Old blocks must remain valid from genesis. Therefore:

- Pre-V35 blocks keep legacy validation and legacy metadata behavior.
- V35 cumulative work starts at zero at the fork boundary unless a deliberate bootstrap value is introduced.
- Chain comparisons before V35 use legacy behavior.
- Chain comparisons after V35 compare V35 cumulative work first.
- Mixed old/new summaries must not reinterpret legacy `hashCompexity` values as compact targets before V35.

## Proposed V35 DataShortBlockchainInformation Changes

Phase 3B-4B should add a new cumulative work field. Recommended field:

```java
private String cumulativeWork;
```

Rationale:

- Cumulative work is a large integer and can exceed `long`.
- A decimal string is safer over JSON than a raw JSON number because browser clients and mixed tooling may lose precision for large integers.
- Existing legacy fields can remain unchanged.
- Older nodes that do not know the field can continue using legacy fields before V35.

Recommended additional field:

```java
private String tipHash;
```

Rationale:

- V35 requires a deterministic final tie-breaker.
- The current `bigRandomNumber` is not suitable as a deterministic final chain tie-breaker after V35.
- `tipHash` allows a stable lexicographic tie-break without changing block format.

Constructor compatibility plan:

- Preserve the current constructor signature.
- Add an overloaded constructor including `cumulativeWork` and `tipHash`.
- Update `clone()` to preserve both new fields.
- Default missing/null cumulative work to `"0"` in helper code, not by changing legacy behavior.

Backward compatibility:

- Keep `size`, `isValidation`, `hashCount`, `staking`, `transactions`, and `bigRandomNumber`.
- `/datashort` can include the new fields for upgraded nodes.
- If old remote nodes omit `cumulativeWork`, upgraded nodes should:
  - use legacy comparison before V35;
  - after V35, treat missing `cumulativeWork` as unavailable and avoid letting legacy random/stake/tx scores beat a valid V35 cumulative-work chain.

## Proposed V35 Comparator Priority

After V35, priority should be:

1. Valid chain: `isValidation == true`.
2. Highest `cumulativeWork`.
3. Latest valid height: `size`.
4. Capped stake/transaction score as tie-breaker only.
5. Deterministic hash tie-breaker using `tipHash`.

Important constraints:

- `bigRandomNumber` must not override higher cumulative work.
- Staking must not override higher cumulative work.
- Transaction count must not override higher cumulative work.
- Random score must not be used as a primary V35 chain comparator.
- Legacy comparator behavior remains available before V35.

Recommended comparator split:

- Add a small V35 comparator helper or private comparator branch.
- Keep legacy comparison branch intact for pre-V35.
- Use `ProtocolVersion.isV35(size or tip index)` to select the branch.

Open design detail for Phase 3B-4B:

- `DataShortBlockchainInformation` currently stores `size`, not explicit tip index.
- If `size` corresponds to latest chain height + 1 in this codebase, use the exact existing convention consistently.
- If uncertain, compare using the same value currently used for chain size and document the height convention in tests.

## Proposed Capped Stake / Transaction Tie-Breaker

Tie-breaker only after equal cumulative work and equal height:

```text
cappedTieScore = min(stakeScoreCap, normalizedStakeScore) + min(txScoreCap, normalizedTxScore)
```

Rules:

- The cap must be small and only applied after cumulative work and height are equal.
- This score must never be added to cumulative work.
- The tie-breaker can reuse existing `staking` and `transactions` summaries only after both higher-priority checks are equal.
- If exact cap constants are not yet defined for chain tournament, Phase 3B-4B should add V35-specific constants in a constants-only phase or reuse existing V35 ticket boost caps only if that semantic reuse is explicitly accepted.

## Proposed Backward Compatibility Plan

1. Preserve old fields and current constructor.
2. Add nullable/string new fields instead of replacing `hashCount`.
3. Preserve legacy comparator path before V35.
4. Use V35 comparator only when the summarized chain has crossed V35.
5. Keep `/datashort` old fields populated.
6. Treat missing remote `cumulativeWork` as legacy-only metadata.
7. Do not reject an old node solely for missing `cumulativeWork` before V35.
8. After V35, do not allow missing `cumulativeWork` plus high random/stake/tx score to beat a valid upgraded V35 chain.
9. Add JSON serialization/deserialization tests for old and new short-info payloads.

## Wallet / Server Differences

Shared or duplicated in both wallet and server:

- `Blockchain.java`
- `DataShortBlockchainInformation.java`
- `UtilsResolving.java`
- `HostEndDataShortB.java`
- `HostEndDataShortBComparator.java`
- `NodeChecker.java`
- `UtilsUse.java`
- `V35WorkCalculator.java`

Server-only or server-primary:

- `TournamentService.java` was identified in the server project.
- Server block-candidate tournament selection appears to live in the server project only.

Implication:

- Chain metadata and host-priority behavior must be changed in both wallet and server to prevent drift.
- Candidate block tournament behavior must be changed in the server.
- If the wallet has no `TournamentService`, it should not receive a new tournament subsystem in Phase 3B-4B.

## Exact Files Phase 3B-4B May Edit

### Both Wallet And Server

- `src\main\java\International_Trade_Union\entity\blockchain\DataShortBlockchainInformation.java`
  - Add `cumulativeWork` and likely `tipHash`.
  - Preserve old constructor.
  - Update clone behavior.

- `src\main\java\International_Trade_Union\entity\blockchain\Blockchain.java`
  - Update `shortCheck(...)` to accumulate V35 cumulative work after the fork gate.
  - Update `checkFromFile(...)` to rebuild the same cumulative work from disk.
  - Update `rollBackShortCheck(...)` to subtract V35 work.
  - Preserve legacy metadata behavior before V35.

- `src\main\java\International_Trade_Union\utils\UtilsResolving.java`
  - Gate `isBig(...)`, `isSmall(...)`, and host priority behavior for V35.
  - Keep legacy logic before V35.

- `src\main\java\International_Trade_Union\model\comparator\HostEndDataShortBComparator.java`
  - Add V35 comparator branch.
  - Fix or bypass the legacy size bug only inside the V35 branch unless a separate legacy bugfix phase is approved.

- `src\main\java\International_Trade_Union\protocol\v35\V35WorkCalculator.java`
  - Add helper methods only if current `workFromTarget(...)` is insufficient for safe parsing/addition.

- Tests under:
  - `src\test\java\International_Trade_Union\protocol\v35\`
  - or a clearly named chain scoring test package.

### Server Only

- `src\main\java\International_Trade_Union\model\TournamentService.java`
  - Add V35 candidate priority branch so block selection does not use random/stake/tx as primary after V35.

- `src\main\java\International_Trade_Union\controllers\BasisController.java`
  - Only if `/datashort` or `hashCount` guard logic requires minimal compatibility handling.

- `src\main\java\International_Trade_Union\model\NodeChecker.java`
  - Prefer no changes. It consumes sorted hosts but does not score them directly.

### Serialization Support If Tests Require It

- `src\main\java\International_Trade_Union\utils\UtilsJson.java`
  - Only if JSON compatibility tests show the new fields require explicit serializer/deserializer handling.

## Exact Files Phase 3B-4B Must Not Edit

- `Block.java`
- `DtoTransaction.java`
- `Account.java`
- transaction signing code
- signature verification code
- hash format code
- block format fields
- `Mining.java`
- `UtilsBlock.java`, unless a later explicitly approved validation integration requires it
- `BlockchainDifficulty.java`
- controllers other than a minimal `/datashort` compatibility change if tests require it
- wallet UI/templates/static files
- website files
- `pom.xml`, unless tests cannot compile and approval is requested first

## Safe Implementation Order For Phase 3B-4B

1. Confirm V35 remains disabled in both `Seting.java` files.
2. Add `cumulativeWork` and `tipHash` fields to `DataShortBlockchainInformation` in wallet and server.
3. Preserve the existing constructor and add an overload for new fields.
4. Update `clone()` in wallet and server.
5. Add pure parse/add helpers in `V35WorkCalculator` if needed:
   - parse decimal string to `BigInteger`
   - safe add
   - safe compare
6. Update `Blockchain.shortCheck(...)` in wallet and server:
   - before V35: unchanged behavior;
   - after V35: decode compact target and add `workFromTarget(target)`;
   - set `tipHash` from the latest valid block.
7. Update `Blockchain.checkFromFile(...)` in wallet and server to produce the same summary as `shortCheck(...)`.
8. Update `Blockchain.rollBackShortCheck(...)` in wallet and server to subtract V35 work.
9. Add V35 branch to `HostEndDataShortBComparator` in wallet and server:
   - valid chain;
   - cumulative work;
   - height;
   - capped stake/tx tie-break;
   - deterministic tip hash.
10. Add V35 branch to `UtilsResolving.isBig(...)`, `isSmall(...)`, and any host priority checks that bypass the comparator.
11. Update server `TournamentService.sortWinner(...)` for V35 candidate selection:
   - do not let big-random/staking/tx dominate after V35;
   - use V35 block work/target and deterministic tie-breaks.
12. Add compatibility tests for old JSON short-info without `cumulativeWork`.
13. Add parity tests proving wallet/server comparator and metadata helpers match.
14. Run wallet and server tests/builds.
15. Create Phase 3B-4B implementation report.

## Required Tests Before Implementation

### DataShortBlockchainInformation Tests

- Old constructor still works.
- New fields default safely when absent.
- `clone()` preserves `cumulativeWork` and `tipHash`.
- Old JSON without `cumulativeWork` deserializes.
- New JSON with `cumulativeWork` deserializes.

### Cumulative Work Tests

- V35 per-block work equals `V35WorkCalculator.workFromTarget(decodedTarget)`.
- Lower target produces higher work.
- Invalid compact target produces invalid summary or zero work according to the selected helper contract.
- `shortCheck(...)` and `checkFromFile(...)` produce the same cumulative work for the same V35 blocks.
- `rollBackShortCheck(...)` subtracts V35 work correctly.
- Pre-V35 blocks do not contribute V35 target work.

### Comparator Tests

- Valid chain beats invalid chain.
- Higher cumulative work beats higher `bigRandomNumber`.
- Higher cumulative work beats higher staking.
- Higher cumulative work beats higher transaction count.
- Higher cumulative work beats greater height if the design follows the required priority strictly.
- Equal cumulative work uses latest valid height.
- Equal work and height uses capped stake/tx tie-breaker.
- Equal work, height, and capped score uses deterministic `tipHash`.
- Missing `cumulativeWork` uses legacy fallback only before V35.

### Tournament Tests

- V35 candidate with higher work wins over candidate with higher big-random score.
- V35 candidate with higher work wins over candidate with more transactions.
- V35 candidate with higher work wins over candidate with more staking.
- Equal-work V35 candidates use deterministic tie-breaks.

### Wallet / Server Parity Tests

- Same compact targets produce the same cumulative work in wallet and server helpers.
- Same short-info inputs sort in the same order in wallet and server.
- Same legacy inputs remain sorted by the legacy path while V35 is disabled.

### Regression Tests

- `V35_RESCUE_FORK_INDEX` remains `Long.MAX_VALUE`.
- Current indexes remain legacy.
- Existing legacy short metadata fields are still populated.
- Legacy build/test behavior is unchanged.

## Highest Risks

1. `DataShortBlockchainInformation` compatibility: adding large work data can affect JSON, constructors, clone behavior, or old-node compatibility.
2. Existing comparator bug: `d1.getSize() != d1.getSize()` prevents the intended size comparison. Fixing it globally could change legacy behavior, so Phase 3B-4B should gate changes carefully.
3. `hashCount` semantic mismatch: current `hashCount` is a `long` linear sum of `hashCompexity`; V35 work is a large `BigInteger`. Reusing `hashCount` would be unsafe.
4. Wallet/server drift: duplicated chain metadata and comparator classes must stay synchronized.
5. Tournament vs chain selection mismatch: server candidate tournament and host chain selection are separate decision paths and both need V35 gating.
6. Old nodes after V35: nodes without `cumulativeWork` must not be allowed to win via random/stake/tx metadata.
7. Deterministic tie-breaker availability: current short-info data does not include a tip hash, so Phase 3B-4B likely needs a new `tipHash` field.
8. Rollback correctness: cumulative work must be added and subtracted consistently across `shortCheck`, `checkFromFile`, and `rollBackShortCheck`.

## Rollback Plan

If Phase 3B-4B causes sync or tournament issues:

1. Keep `V35_RESCUE_FORK_INDEX = Long.MAX_VALUE`.
2. Revert only Phase 3B-4B changes to:
   - `DataShortBlockchainInformation.java`
   - `Blockchain.java`
   - `UtilsResolving.java`
   - `HostEndDataShortBComparator.java`
   - server `TournamentService.java`
   - any Phase 3B-4B tests
3. Preserve earlier Phase 3B-0 through Phase 3B-3 pure helper and disabled-gated mining/validation work.
4. Re-run:
   - wallet `.\mvnw.cmd test`
   - wallet `.\mvnw.cmd clean package`
   - server `.\mvnw.cmd test`
   - server `.\mvnw.cmd clean package`
5. Confirm `/datashort` returns the legacy short-info shape.
6. Confirm legacy host priority and tournament behavior are restored.

## Phase 3B-4B Recommendation

Proceed with a narrow gated implementation:

1. Add `cumulativeWork` as decimal string and `tipHash` to short chain metadata.
2. Keep all legacy fields and constructors.
3. Compute V35 work only after `ProtocolVersion.isV35(index)`.
4. Apply V35 comparator priority only after V35.
5. Update both wallet and server metadata/comparator code together.
6. Update server tournament selection separately.
7. Prove with tests that random, staking, and transaction count cannot beat much higher cumulative PoW work after V35.
