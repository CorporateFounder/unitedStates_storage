# CITU Server Sync Tournament Map

Phase 0 audit only. No source code changes were made.

## Files Inspected
- `C:\citu\unitedStates_storage-master\src\main\java\International_Trade_Union\model\NodeChecker.java`
- `...\model\HostEndDataShortB.java`
- `...\entity\blockchain\DataShortBlockchainInformation.java`
- `...\model\comparator\HostEndDataShortBComparator.java`
- `...\utils\UtilsResolving.java`
- `...\model\Tournament.java`
- `...\model\TournamentService.java`
- `...\controllers\BasisController.java`
- `...\entity\blockchain\Blockchain.java`

## Data Exchanged Today
`DataShortBlockchainInformation` contains:
- `size`: chain size/height-like count.
- `isValidation`: whether the remote chain metadata claims validation passed.
- `hashCount`: accumulated in `Blockchain.shortCheck(...)` via `UtilsUse.powerDiff(block.getHashCompexity())`; `powerDiff` returns `diff`, so this is linear difficulty sum, not true cumulative work.
- `staking`: accumulated by adding `stakingForDataShort(miner.getDigitalStakingBalance())`, which divides staking by `100000`.
- `transactions`: accumulated transaction count.
- `bigRandomNumber`: accumulated via `UtilsUse.bigRandomWinner(block, miner)`.

Remote calls used:
- `/datashort`: fetches `DataShortBlockchainInformation`.
- `/getNodes`: fetches known peers.
- `/confirmReadiness`: readiness handshake.
- `/winnerList` and `/prevBlock`: candidate block exchange for tournament.
- `/sub-blocks`: download block ranges during resolve.
- `/timentp`: time sync check.

## Current Priority Host Sorting
`UtilsResolving.sortPriorityHost(Set<String> hosts)`:
- Adds `Seting.ORIGINAL_ADDRESSES`.
- Excludes own public domain.
- Randomly shuffles and limits to `Seting.RANDOM_HOSTS`.
- Fetches `/datashort` from each host.
- Keeps only hosts where `global != null && global.isValidation()`.
- Sorts with `HostEndDataShortBComparator`.

`HostEndDataShortBComparator.compare(...)`:
- Intended to compare size first, but has a bug: `if(d1.getSize() != d1.getSize())`, always false.
- Then compares `bigRandomNumber` descending.
- Then compares `transactions` descending.
- It does not compare `hashCount`, `staking`, cumulative work, validation beyond earlier filter, or deterministic hash.

## Current `shortCheck(...)`
`Blockchain.shortCheck(...)` validates supplied blocks against previous state and returns new `DataShortBlockchainInformation`.
- Calls `UtilsBlock.validationOneBlock(...)` for each block.
- Adds `UtilsUse.powerDiff(block.getHashCompexity())` to `hashCount`.
- Adds scaled miner staking to `staking`.
- Adds `UtilsUse.bigRandomWinner(block, miner)` to `bigRandomNumber`.
- Adds `block.getDtoTransactions().size()` to `transactions`.
- Applies `UtilsBalance.calculateBalance(...)` as it advances.

## Current Tournament Behavior
`Tournament.run()`:
- Calls `utilsResolving.sortPriorityHost(BasisController.getNodes())`.
- Calls `tournament.updatingNodeEndBlocks(hosts)` to resolve longer/better chains.
- Repeats loop: sort hosts, run `tournament.tournament(hosts)`, update nodes, collect transactions, send hosts, check sync time.

`TournamentService.tournament(...)`:
- Calls `getAllWinner(hostEndDataShortBS)`.
- `getAllWinner(...)` fetches remote `/winnerList` and `/prevBlock`, validates candidate blocks with `Blockchain.shortCheck(...)`, and appends valid candidates to `BasisController.getWinnerList()`.
- Filters candidates where `block.index == local blockchain size`, distinct hash, no duplicate transaction signatures.
- Computes temporary balances and calls `sortWinner(...)`.
- Saves the first sorted winner with `utilsResolving.addBlock3(...)`, then updates `shortDataBlockchain`.

`TournamentService.sortWinner(...)` ranking today:
1. Highest `UtilsUse.bigRandomWinner(block, miner)`.
2. Highest `Block.getHashCompexity()`.
3. Highest miner `digitalStakingBalance`.
4. Highest transaction count.

This means staking/transaction/random-style metrics can influence winning block selection. It is not V35 cumulative-work chain selection.

## Current Chain Resolve Helpers
- `UtilsResolving.isBig(actual, global)`: returns true only if `global.bigRandomNumber > actual.bigRandomNumber`.
- `UtilsResolving.isSmall(expected, actual)`: returns true if `actual.size < expected.size` or `actual.bigRandomNumber < expected.bigRandomNumber`.
- `UtilsResolving.helpResolve(...)`: downloads block ranges through `/sub-blocks`, identifies divergence, rolls back/adds blocks with validation and balance recalculation.

## V35 Proposed Behavior
Primary chain selection after V35 should be:
1. Valid chain under historical plus V35 rules.
2. Highest `cumulativeWork`, calculated from target-based PoW (`hashNumber <= target`).
3. Latest valid height only if cumulative work ties or is exactly indistinguishable.
4. Capped stake/transaction score as a narrow tie-breaker only, never enough to override much higher PoW cumulative work.
5. Deterministic hash tie-breaker, for example lexicographically lowest block hash or chain tip hash after all other rules tie.

## Required V35 Data Additions
- Add `cumulativeWork` to `DataShortBlockchainInformation` or a V35-specific metadata object.
- Add `latestBlockHash` and/or deterministic `chainTieBreakHash`.
- Add current target/difficulty representation.
- Keep legacy fields for old endpoints but stop using `bigRandomNumber`/`transactions` as primary chain quality.

## Migration Notes
- Preserve old `/datashort` behavior for old nodes if required.
- Add new `/api/v35/chain-score` or versioned metadata endpoint rather than silently changing old clients first.
- Make server and wallet use the same chain-score calculation.
