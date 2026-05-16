# CITU V35 Phase 3B-4B4A Tournament Service Review Plan

Phase: 3B-4B4A planning only  
Project root: `C:\citu`  
Created: 2026-05-16

## Scope

Reviewed server `TournamentService` and related block-candidate priority code to determine whether V35 must change it. No source code, tests, comparator behavior, mining behavior, validation behavior, block format, transaction format, DTO fields, UI/templates, website files, or `pom.xml` files were changed.

V35 remains disabled:

- Wallet: `C:\citu\unitedStates_final-master\src\main\java\International_Trade_Union\setings\Seting.java`
  - `V35_RESCUE_FORK_INDEX = Long.MAX_VALUE`
- Server: `C:\citu\unitedStates_storage-master\src\main\java\International_Trade_Union\setings\Seting.java`
  - `V35_RESCUE_FORK_INDEX = Long.MAX_VALUE`

## Reports Read

- `C:\citu\CITU_V35_PHASE3B4A_CUMULATIVE_WORK_TOURNAMENT_PLAN.md`
- `C:\citu\CITU_V35_PHASE3B4B0_CHAIN_SCORING_HELPERS_REPORT.md`
- `C:\citu\CITU_V35_PHASE3B4B1_CUMULATIVE_WORK_METADATA_REPORT.md`
- `C:\citu\CITU_V35_PHASE3B4B2_CUMULATIVE_WORK_METADATA_POPULATION_REPORT.md`
- `C:\citu\CITU_V35_PHASE3B4B3_CUMULATIVE_WORK_COMPARATOR_REPORT.md`
- `C:\citu\CITU_V35_PHASE3B4B3_FORK_GATE_FIX_REPORT.md`

## Exact Files And Methods Found

### Server Tournament Files

Server root: `C:\citu\unitedStates_storage-master`

File: `src\main\java\International_Trade_Union\model\TournamentService.java`

- `sortWinner(Map<String, Account> finalBalances, List<Block> list)` around lines 79-101
  - Sorts candidate blocks.
  - Current priority:
    1. `UtilsUse.bigRandomWinner(block, miner)` descending
    2. `Block.getHashCompexity()` descending
    3. miner `Account.getDigitalStakingBalance()` descending
    4. `block.getDtoTransactions().size()` descending

- `blockToLiteVersion(List<Block> list, Map<String, Account> balances)` around lines 104-122
  - Converts winner blocks to UI/API summary objects.
  - Includes transaction count, staking balance, `bigRandomWinner`, and `hashCompexity`.
  - This is display/reporting support, not the primary consensus decision, but it mirrors legacy metrics.

- `getAllWinner(List<HostEndDataShortB> hostEndDataShortBS)` around lines 181-259
  - Fetches `/winnerList` and `/prevBlock` from priority hosts.
  - Deserializes remote candidate blocks.
  - Calls `validateAndAddBlock(...)`.
  - Adds valid candidates to `BasisController.getWinnerList()`.

- `validateAndAddBlock(Block block, Set<Block> tempWinnerSet, String host)` around lines 261-281
  - Builds one-block validation context.
  - Calls `Blockchain.shortCheck(...)`.
  - Adds the block to `tempWinnerSet` only if `temp.isValidation()` is true.

- `tournament(List<HostEndDataShortB> hostEndDataShortBS)` around lines 284-575
  - Calls `getAllWinner(...)`.
  - Calls `nodeChecker.initiateProcess(...)`.
  - Filters `BasisController.getWinnerList()` to current height, distinct hash, and no duplicate transactions.
  - Calls `sortWinner(...)`.
  - Selects `winnerList.get(0)`.
  - Validates the selected winner again with `Blockchain.shortCheck(...)`.
  - Saves the selected block through `utilsResolving.addBlock3(...)`.

- `updatingNodeEndBlocks(List<HostEndDataShortB> hostEndDataShortBS)` around lines 617-643
  - Calls `utilsResolving.resolve3(...)`.
  - Updates from other nodes after tournament.
  - This is sync-related, but it delegates chain choice to `UtilsResolving`, not `sortWinner(...)`.

File: `src\main\java\International_Trade_Union\model\Tournament.java`

- `run()` around lines 112-233
  - Main scheduled server tournament loop.
  - Gets hosts via `utilsResolving.sortPriorityHost(...)`.
  - Calls `nodeChecker.checkNodes(hosts)`.
  - Calls `tournament.updatingNodeEndBlocks(hosts)`.
  - Calls `nodeChecker.initiateProcess(hosts)`.
  - Calls `tournament.tournament(hosts)`.
  - Calls `tournament.updatingNodeEndBlocks(hosts)` again.
  - Later collects transactions and sync-time checks.

File: `src\main\java\International_Trade_Union\controllers\BasisController.java`

- `winnerList()` endpoint around lines 173-213
  - Returns the first cached winner block to peers.

- `updateWinnerListCache(List<Block> currentWinnerList)` around lines 231-271
  - Re-sorts current local winner list using `TournamentService.sortWinner(...)`.
  - Updates the winner-list cache returned by `/winnerList`.

- `resolve_from_to_block(...)` path around lines 1120-1270
  - Accepts blocks from peers.
  - Checks previous hash, difficulty floor, timestamp, and block hash.
  - Calls `Blockchain.shortCheck(...)`.
  - Adds valid incoming blocks to `winnerList` when `temp.getSize() > shortDataBlockchain.getSize()` and `temp.getHashCount() >= shortDataBlockchain.getHashCount()`.
  - This is not `TournamentService`, but it is related peer candidate admission and still uses legacy `hashCount`.

File: `src\main\java\International_Trade_Union\utils\UtilsUse.java`

- `powerDiff(long diff)` around lines 384-387
  - Currently returns `diff`.
  - Legacy `hashCount` is therefore a linear sum, not Bitcoin-like cumulative work.

- `bigRandomWinner(Block actual, Account miner)` around lines 396-510
  - Creates deterministic random from block hash.
  - Adds difficulty-like score from `hashCompexity`.
  - Adds staking-derived `mineScore`.
  - Adds transaction-count and transaction-sum points after newer legacy forks.
  - This is a legacy score function and must not be primary after V35.

### Wallet Search Result

Wallet root: `C:\citu\unitedStates_final-master`

No wallet `TournamentService.java` or `Tournament.java` was found. The wallet has:

- `src\main\java\International_Trade_Union\protocol\v35\ChainTournamentScorer.java`
- `src\main\java\International_Trade_Union\utils\UtilsUse.java`
  - Contains `bigRandomWinner(...)`, but no matching server tournament loop.

Conclusion: server block-candidate tournament is server-only. Wallet participates in shared chain metadata and host comparison, but not in this `TournamentService` candidate selection path.

## Current TournamentService Flow

1. `Tournament.run()` builds/sorts host priority with `UtilsResolving.sortPriorityHost(...)`.
2. `TournamentService.updatingNodeEndBlocks(...)` syncs node end blocks through `UtilsResolving.resolve3(...)`.
3. `TournamentService.tournament(...)` begins the candidate tournament.
4. `getAllWinner(...)` asks hosts for `/winnerList` and `/prevBlock`.
5. Candidate blocks are validated by `validateAndAddBlock(...)` using `Blockchain.shortCheck(...)`.
6. Valid candidates are added to `BasisController.getWinnerList()`.
7. `tournament(...)` filters candidates to the current expected index, unique block hash, and no duplicate transactions.
8. `sortWinner(...)` ranks candidates using legacy random/stake/tx/hashComplexity priority.
9. `winnerList.get(0)` becomes the selected block.
10. The selected block is validated again with `Blockchain.shortCheck(...)`.
11. The selected block is saved through `utilsResolving.addBlock3(...)`.
12. Winner display lists are updated using `blockToLiteVersion(...)`.

## Consensus-Critical Assessment

`TournamentService.sortWinner(...)` is consensus-critical or consensus-adjacent.

Reason:

- It chooses the actual candidate block saved by this server when more than one valid block exists for the same height.
- It can influence what block the server publishes through `/winnerList`.
- Other nodes fetch `/winnerList` and may validate/add those candidates.
- The selected winner is persisted through `utilsResolving.addBlock3(...)`.

It is not the same as full chain selection:

- Full server-chain priority is handled by `UtilsResolving`, `HostEndDataShortBComparator`, short-chain metadata, and `NodeChecker`.
- Candidate tournament chooses among peer blocks competing for one current height.

But because candidate choice can become the next accepted block, V35 must prevent random, staking, and transaction count from overriding PoW/target work in this path.

## Chain Selection Versus Candidate Sorting

Current chain selection paths:

- `UtilsResolving.sortPriorityHost(...)`
- `HostEndDataShortBComparator.compare(...)`
- `UtilsResolving.isBig(...)`
- `UtilsResolving.isSmall(...)`
- `NodeChecker` consumes sorted hosts but does not directly score chains.

Current candidate sorting paths:

- `TournamentService.sortWinner(...)`
- `BasisController.updateWinnerListCache(...)` indirectly through `TournamentService.sortWinner(...)`
- `TournamentService.tournament(...)` selects `winnerList.get(0)` after sorting.

The V35 cumulative-work comparator added in previous phases protects short-chain/server priority. It does not protect `TournamentService.sortWinner(...)` because candidate blocks do not carry short-chain `cumulativeWork` metadata.

## Current Use Of Random / Stake / Tx / hashCount

`TournamentService.sortWinner(...)` uses:

- random:
  - primary sort key via `UtilsUse.bigRandomWinner(...)`;
- hash/difficulty:
  - secondary sort key via `Block.getHashCompexity()`;
- staking:
  - tertiary sort key via miner `Account.getDigitalStakingBalance()`;
- transactions:
  - final sort key via `block.getDtoTransactions().size()`.

`UtilsUse.bigRandomWinner(...)` itself also uses:

- deterministic hash-derived random;
- `Block.getHashCompexity()`;
- miner staking balance;
- transaction count;
- transaction sum.

`hashCount` is not directly used by `TournamentService.sortWinner(...)`, but related peer admission in `BasisController.resolve_from_to_block(...)` checks `temp.getHashCount() >= shortDataBlockchain.getHashCount()` while deciding whether to add incoming blocks to `winnerList`.

## Does V35 Need To Change TournamentService?

Yes, if `TournamentService` remains active after V35.

Reason:

- V35 PoW rules make `hashNumber <= target` the validation rule.
- V35 mining target is encoded in `Block.hashCompexity`.
- A valid V35 block with harder target should not lose to another valid same-height block because the other block has more random/stake/transaction score.
- The current `sortWinner(...)` makes `bigRandomWinner(...)` the primary key, and that score includes random, stake, and transaction effects.
- Therefore the current server candidate ranking conflicts with V35's requirement that stake/tx/random must not override PoW work where candidate priority impacts consensus.

Before V35, the current behavior should remain unchanged.

## Proposed V35 Behavior

Add a strict V35 branch to server candidate sorting:

1. Before V35:
   - `TournamentService.sortWinner(...)` keeps the exact current legacy comparator.

2. After V35:
   - validate candidates before sorting as already done by `validateAndAddBlock(...)`;
   - sort valid same-height candidate blocks by:
     1. valid block already required by pre-filter/validation;
     2. higher per-block work from `V35WorkCalculator.workFromCompactTarget(block.getHashCompexity())`;
     3. lower hash number or deterministic `hashBlock` tie-breaker;
     4. capped local tie-breaker from stake/tx only if needed and only after work equality;
     5. deterministic final hash comparison.

Recommended candidate priority after V35:

```text
candidate index is V35
valid block
higher workFromCompactTarget(hashCompexity)
hashNumber <= decodedTarget already enforced by validation
lower hash number or deterministic tip hash tie-break
capped stake/tx tie-break only after equal work/hash policy, if desired
```

Do not use `UtilsUse.bigRandomWinner(...)` as primary after V35.

Do not use raw transaction count or staking as primary after V35.

Do not add candidate stake/tx score to PoW work.

## Required Fork Gate

Any V35 candidate comparator must be inactive while V35 is disabled.

Required gate:

```text
Seting.V35_RESCUE_FORK_INDEX != Long.MAX_VALUE
AND candidate block index >= Seting.V35_RESCUE_FORK_INDEX
AND compact target decodes to positive target
AND block hash parses safely
```

If any condition cannot be determined safely:

- use the legacy `sortWinner(...)` path before V35;
- reject or rank invalid V35 compact-target data only inside the V35 branch, according to the validation contract already established by `UtilsBlock`.

Important: do not use `ProtocolVersion.isV35(index)` alone unless the implementation also checks `Seting.V35_RESCUE_FORK_INDEX != Long.MAX_VALUE`. The Phase 3B-4B3-FIX report established this strict disabled-gate pattern for remote metadata safety.

## Files Phase 3B-4B4B May Edit

Server only:

- `C:\citu\unitedStates_storage-master\src\main\java\International_Trade_Union\model\TournamentService.java`
  - Add fork-gated V35 candidate comparator.
  - Preserve legacy `sortWinner(...)` behavior before V35.

- `C:\citu\unitedStates_storage-master\src\test\java\International_Trade_Union\model\TournamentServiceTest.java`
  - Or a clearly named server test under an existing test package.

- `C:\citu\unitedStates_storage-master\src\main\java\International_Trade_Union\protocol\v35\V35WorkCalculator.java`
  - Only if a small pure helper is needed for candidate work parsing.

- `C:\citu\unitedStates_storage-master\src\main\java\International_Trade_Union\protocol\v35\V35PowValidator.java`
  - Only if a pure hash parsing helper is needed and already belongs there.

- `C:\citu\unitedStates_storage-master\src\main\java\International_Trade_Union\protocol\v35\V35TargetDifficulty.java`
  - Only if candidate tests require existing compact-target helper behavior.

- `C:\citu\CITU_V35_PHASE3B4B4B_TOURNAMENT_SERVICE_REPORT.md`

Possible but avoid unless required:

- `C:\citu\unitedStates_storage-master\src\main\java\International_Trade_Union\controllers\BasisController.java`
  - Only if `/winnerList` cache sorting must explicitly call a new method name.
  - Prefer preserving `TournamentService.sortWinner(...)` signature so `BasisController` does not need changes.

## Files Phase 3B-4B4B Must Not Edit

- Wallet project files unless a matching wallet candidate tournament is later discovered.
- `Block.java`
- `Blockchain.java`
- `BlockchainDifficulty.java`
- `DataShortBlockchainInformation.java`
- `DtoTransaction.java`
- `Account.java`
- `Mining.java`
- `UtilsBlock.java`
- `UtilsResolving.java`
- `HostEndDataShortBComparator.java`
- `NodeChecker.java`
- transaction signing code
- signature verification code
- block/transaction format code
- controllers unless strictly required for existing `sortWinner(...)` signature compatibility
- UI/templates
- website
- `pom.xml`

## Required Tests Before Implementation

Server tests should cover:

1. V35 disabled safety:
   - with `V35_RESCUE_FORK_INDEX = Long.MAX_VALUE`, `sortWinner(...)` uses legacy priority;
   - a candidate with higher V35 work must not change ordering while V35 is disabled if legacy score prefers another block.

2. Legacy preservation:
   - before V35, `bigRandomWinner(...)`, `hashCompexity`, staking, and transaction count order remains unchanged.

3. V35 candidate priority helper:
   - if a pure helper can accept an explicit fork index for tests, prove higher work wins after a simulated active fork;
   - if the production constants cannot be changed, test the pure comparator helper directly and document that production gate remains disabled.

4. V35 work priority:
   - higher `workFromCompactTarget(hashCompexity)` wins over higher random score;
   - higher work wins over higher staking;
   - higher work wins over more transactions;
   - invalid compact target does not crash.

5. Deterministic tie-break:
   - equal work candidates use deterministic hash comparison;
   - null/empty/invalid hash is handled safely.

6. Integration boundary:
   - `BasisController.updateWinnerListCache(...)` still calls `TournamentService.sortWinner(...)` and remains compatible if no controller change is made.

7. Wallet/server parity:
   - not required for `TournamentService` itself because it is server-only;
   - V35 helper methods used by server should remain covered by existing wallet/server parity tests if they are shared helper classes.

## Safe Implementation Order For Phase 3B-4B4B

1. Confirm both `Seting.V35_RESCUE_FORK_INDEX` constants remain `Long.MAX_VALUE`.
2. Add private helper in server `TournamentService`:
   - `shouldUseV35CandidatePriority(List<Block> candidates)` or per-block equivalent;
   - require `Seting.V35_RESCUE_FORK_INDEX != Long.MAX_VALUE`;
   - require candidate index at or after fork.
3. Extract current legacy comparator inside `sortWinner(...)` without changing behavior.
4. Add V35 comparator branch inside `sortWinner(...)` only after the strict fork gate.
5. Use `V35WorkCalculator.workFromCompactTarget(block.getHashCompexity())` for per-block work.
6. Use safe hash parsing for deterministic tie-break.
7. Keep `sortWinner(...)` method signature unchanged so `BasisController.updateWinnerListCache(...)` and `TournamentService.tournament(...)` keep calling it.
8. Add disabled-safety tests proving positive V35 work cannot affect ordering while the fork index is `Long.MAX_VALUE`.
9. Add pure V35 comparator tests for the active logic without changing production constants.
10. Run server tests/build.
11. Run wallet tests/build only if shared V35 helper files are edited; otherwise at least confirm wallet remains untouched.
12. Create Phase 3B-4B4B implementation report.

## Risk List

1. `TournamentService.sortWinner(...)` is consensus-adjacent because it chooses the block that may be persisted by the server.
2. Current legacy priority lets random/stake/transaction score dominate candidate choice.
3. Candidate blocks do not carry short-chain `cumulativeWork`, so the previous `HostEndDataShortBComparator` fix does not protect this path.
4. Full active-path tests are hard while `V35_RESCUE_FORK_INDEX = Long.MAX_VALUE`; production fork activation must remain separate.
5. `BasisController.resolve_from_to_block(...)` still uses legacy `hashCount` admission logic and should be separately reviewed after `TournamentService`.
6. `blockToLiteVersion(...)` displays legacy winner metrics; future UI/API phases may need V35 Gold/target/work display changes.
7. Changing `sortWinner(...)` globally would be dangerous; it must be fork-gated.
8. Invalid compact target or hash handling must not crash tournament sorting.

## Rollback Plan

If Phase 3B-4B4B causes candidate selection or build issues:

1. Keep `V35_RESCUE_FORK_INDEX = Long.MAX_VALUE`.
2. Revert only Phase 3B-4B4B edits to:
   - server `TournamentService.java`;
   - any server V35 helper changes made in that phase;
   - Phase 3B-4B4B tests;
   - Phase 3B-4B4B report if necessary.
3. Do not revert prior V35 helper, metadata, comparator, mining, or validation phases.
4. Re-run:
   - server `.\mvnw.cmd test`
   - server `.\mvnw.cmd clean package`
   - wallet tests/build only if shared helper files were touched.
5. Confirm legacy `sortWinner(...)` order is restored.

## Recommendation

Phase 3B-4B4B should change server `TournamentService.sortWinner(...)`, but only behind a strict V35 fork gate.

Before V35, leave the legacy candidate tournament untouched.

After V35, candidate sorting should stop using `bigRandomWinner(...)`, staking, and transaction count as primary priority. Candidate PoW work from the compact target must dominate, with deterministic hash tie-breakers and only capped stake/tx tie-breaks after equal work.
