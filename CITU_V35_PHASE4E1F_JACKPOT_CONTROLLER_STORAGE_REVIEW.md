# CITU V35 Phase 4E-1F Jackpot Controller / Storage Integration Review

## Scope

This phase was review-only. No source code, tests, controllers, mining, validation, storage behavior, transaction format, or block format were changed.

V35 remains disabled in both projects:

- `C:\citu\unitedStates_final-master\src\main\java\International_Trade_Union\setings\Seting.java`
  - `V35_RESCUE_FORK_INDEX = Long.MAX_VALUE`
- `C:\citu\unitedStates_storage-master\src\main\java\International_Trade_Union\setings\Seting.java`
  - `V35_RESCUE_FORK_INDEX = Long.MAX_VALUE`

## Exact files / methods inspected

Wallet:

- `C:\citu\unitedStates_final-master\src\main\java\International_Trade_Union\controllers\BasisController.java`
  - `resolve_conflicts()`
  - `sendAllBlocksToStorage(...)`
  - `mining(...)`
  - `mine(...)`
  - `v35CanonicalContextBefore(...)`
  - `v35CanonicalContextForLocalValidation(...)`
- `C:\citu\unitedStates_final-master\src\main\java\International_Trade_Union\utils\UtilsResolving.java`
  - `resolve3(...)`
  - `helpResolve(...)`
  - `addBlock3(...)`
  - `rollBackAddBlock3(...)`
  - `calculateResolvingBalance(...)`
- `C:\citu\unitedStates_final-master\src\main\java\International_Trade_Union\entity\blockchain\Blockchain.java`
  - `shortCheck(...)`
  - `shortCheckWithCanonicalContext(...)`
  - `checkFromFile(...)`
- `C:\citu\unitedStates_final-master\src\main\java\International_Trade_Union\utils\UtilsBlock.java`
  - `validationOneBlock(...)`
  - `validationOneBlockWithCanonicalContext(...)`
  - `validateV35JackpotPayoutWithChainContext(...)`
  - `validateV35JackpotPayoutsWithCanonicalContext(...)`
  - full-chain `validation(...)`
- `C:\citu\unitedStates_final-master\src\main\java\International_Trade_Union\utils\UtilsBalance.java`
  - `calculateBalance(...)`
  - `calculateBalances(...)`
  - `applyV35JackpotPayoutsWithChainContext(...)`
  - `isValidV35JackpotPayoutForBalance(...)`
- `C:\citu\unitedStates_final-master\src\main\java\International_Trade_Union\model\Mining.java`
  - `buildV35JackpotPayoutTransactionsForMining(...)`
  - `miningDay(...)`

Server:

- `C:\citu\unitedStates_storage-master\src\main\java\International_Trade_Union\controllers\BasisController.java`
  - `resolve_conflict(...)` at `/nodes/resolve_from_to_block`
  - `shouldAcceptResolveCandidate(...)`
  - `isResolveBlockBelowMinimumDifficulty(...)`
  - `v35CanonicalContextBefore(...)`
  - `/datashort`, `/sub-blocks`, `/block`, `/size`
- `C:\citu\unitedStates_storage-master\src\main\java\International_Trade_Union\utils\UtilsResolving.java`
  - `resolve3(...)`
  - `helpResolve(...)`
  - `addBlock3(...)`
  - `rollBackAddBlock3(...)`
  - `calculateResolvingBalance(...)`
- `C:\citu\unitedStates_storage-master\src\main\java\International_Trade_Union\entity\blockchain\Blockchain.java`
  - `shortCheck(...)`
  - `shortCheckWithCanonicalContext(...)`
  - `checkFromFile(...)`
- `C:\citu\unitedStates_storage-master\src\main\java\International_Trade_Union\utils\UtilsBlock.java`
  - `validationOneBlock(...)`
  - `validationOneBlockWithCanonicalContext(...)`
  - `validateV35JackpotPayoutWithChainContext(...)`
  - `validateV35JackpotPayoutsWithCanonicalContext(...)`
  - full-chain `validation(...)`
- `C:\citu\unitedStates_storage-master\src\main\java\International_Trade_Union\utils\UtilsBalance.java`
  - `calculateBalance(...)`
  - `calculateBalances(...)`
  - `applyV35JackpotPayoutsWithChainContext(...)`
  - `isValidV35JackpotPayoutForBalance(...)`
- `C:\citu\unitedStates_storage-master\src\main\java\International_Trade_Union\model\TournamentService.java`
  - `sortWinner(...)`
  - `validateAndAddBlock(...)`
  - `tournament(...)`
  - `v35CanonicalContextBefore(...)`
- `C:\citu\unitedStates_storage-master\src\main\java\International_Trade_Union\model\Mining.java`
  - `buildV35JackpotPayoutTransactionsForMining(...)`
  - `miningDay(...)`

Storage conversion:

- `C:\citu\unitedStates_storage-master\src\main\java\International_Trade_Union\utils\UtilsBlockToEntityBlock.java`
  - `blockToEntityBlock(...)`
  - `entityBlockToBlock(...)`
  - `dtoTransactionToEntity(...)`
  - `entityDtoTransactionToDtoTransaction(...)`
- `C:\citu\unitedStates_storage-master\src\main\java\International_Trade_Union\entity\services\BlockService.java`
  - `saveAllBLockF(...)`
  - `saveAccountAllF(...)`
  - `findBySpecialIndex(...)`
  - `findBySpecialIndexBetween(...)`
  - `deleteEntityBlocksAndRelatedData(...)`
- `C:\citu\unitedStates_storage-master\src\main\java\International_Trade_Union\entity\entities\EntityBlock.java`
- `C:\citu\unitedStates_storage-master\src\main\java\International_Trade_Union\entity\entities\EntityDtoTransaction.java`

## Controller / block acceptance flow

### Server receive path

`BasisController.resolve_conflict(...)` receives remote blocks at:

- `POST /nodes/resolve_from_to_block`

Current flow:

1. checks version, timestamp, previous hash, minimum legacy difficulty gate, and hash-for-transaction;
2. builds `addlist`;
3. builds temporary balances from `UtilsUse.accounts(addlist, blockService)`;
4. validates with `Blockchain.shortCheckWithCanonicalContext(...)`;
5. supplies canonical prefix through `v35CanonicalContextBefore(prevBlock)`;
6. admits the candidate through `shouldAcceptResolveCandidate(...)`;
7. stores candidate blocks through `utilsResolving.sendAllBlocksToStorage(addlist)` / `utilsResolving.addBlock3(...)` path.

The V34 minimum difficulty precheck is already fork-gated through `isResolveBlockBelowMinimumDifficulty(...)`; it returns false for V35 indexes if a real non-`Long.MAX_VALUE` fork index is supplied.

### Wallet local mined block path

`BasisController.mining(...)` validates the locally mined block before propagation with:

- `UtilsBlock.validationOneBlockWithCanonicalContext(...)`
- `v35CanonicalContextForLocalValidation(...)`
- `Seting.V35_RESCUE_FORK_INDEX`

Then it propagates through `sendAllBlocksToStorage(...)`, which posts to the server `/nodes/resolve_from_to_block` endpoint.

### Wallet sync path

`BasisController.resolve_conflicts()` downloads missing blocks and validates batches with:

- `Blockchain.shortCheckWithCanonicalContext(...)`
- `v35CanonicalContextBefore(BasisController.getPrevBlock())`
- `utilsResolving.addBlock3(...)`

## Storage save / load flow

### Database storage

`UtilsResolving.addBlock3(...)` converts blocks to `EntityBlock` with:

- `UtilsBlockToEntityBlock.blockToEntityBlock(...)`

and saves:

- accounts with `BlockService.saveAccountAllF(...)`;
- blocks with `BlockService.saveAllBLockF(...)`.

`EntityDtoTransaction` stores ordinary transaction fields:

- `sender`
- `customer`
- `digitalDollar`
- `digitalStockBalance`
- `bonusForMiner`
- `voteEnum`
- `sign`

No jackpot marker is required because the V35 design infers the period from canonical payout amount. A jackpot payout should therefore survive database save/reload as an ordinary `BASIS_ADDRESS -> winner` transaction.

### File storage

`UtilsResolving.addBlock3(...)` also writes blocks to file with:

- `UtilsBlock.saveBlocks(originalBlocks, filename)`

`Blockchain.checkFromFile(...)` later reloads JSON lines with:

- `UtilsFileSaveRead.reads(...)`
- `UtilsJson.jsonToBLock(...)`

The JSON/block format is unchanged, so jackpot payout transactions should survive file save/reload format-wise.

## Can jackpot payout blocks survive save/reload?

Format-wise: yes.

Reason:

- jackpot payout transactions use existing `DtoTransaction` fields;
- database conversion preserves sender/customer/amount/stock/bonus/vote/sign;
- file JSON storage preserves the existing `Block` and `DtoTransaction` shape;
- no new transaction or block field is needed.

Behavior-wise after V35 activation: not fully proven.

Reason:

- `Blockchain.checkFromFile(...)` validates with context-aware `UtilsBlock.validationOneBlockWithCanonicalContext(...)`, but it still updates its running `balances` map using legacy `UtilsBalance.calculateBalance(...)` before validation;
- `Blockchain.shortCheckWithCanonicalContext(...)` also validates with context-aware `UtilsBlock`, then updates running balances with legacy `UtilsBalance.calculateBalance(...)`;
- legacy `calculateBalance(...)` does not credit jackpot payouts to historical winners because it still treats BASIS payouts as founder/miner-only legacy rewards;
- if a jackpot winner spends the jackpot in a later block in the same validation/reload run, the running validation balance may be wrong unless the path uses proof-aware jackpot accounting.

## Balances after save/reload

Confirmed safe in helper/E2E tests:

- `UtilsBalance.applyV35JackpotPayoutsWithChainContext(...)` credits a validation-approved jackpot payout winner.
- `UtilsBalance.calculateBalances(..., founderAddress, forkIndex)` is proof-aware and strips jackpot payout candidates from legacy accounting before applying the V35 proof path.
- `UtilsResolving.addBlock3(...)` uses `calculateResolvingBalance(...)`, which is proof-aware for V35 jackpot payout candidates.
- `UtilsResolving.rollBackAddBlock3(...)` recalculates with `UtilsBalance.calculateBalances(...)` when V35 jackpot accounting is required.

Remaining risk:

- `Blockchain.checkFromFile(...)` and `Blockchain.shortCheckWithCanonicalContext(...)` still use `UtilsBalance.calculateBalance(...)` internally for running validation state.
- `Mining.getBalances(...)` also uses legacy `UtilsBalance.calculateBalance(...)` for a last-block balance update.
- `UtilsBlock.validation(...)` full-chain helper still calls ordinary `validationOneBlock(...)` and legacy `UtilsBalance.calculateBalance(...)`, so it remains no-context/conservative and is not V35-jackpot-ready.

## Remaining no-context validation paths

Remaining explicit no-context methods:

- `UtilsBlock.validationOneBlock(...)`
  - delegates to `validationOneBlockWithCanonicalContext(...)` with empty context;
  - intentionally conservative for jackpot payout candidates.
- `Blockchain.shortCheck(...)`
  - delegates to `shortCheckWithCanonicalContext(...)` with empty context;
  - intentionally conservative for jackpot payout candidates.
- `UtilsBlock.validation(...)`
  - full-chain helper still calls `validationOneBlock(...)`, not the context-aware overload.

These are acceptable only if production paths avoid them for V35 jackpot payout blocks. The reviewed controller/resolving paths mostly do, but `UtilsBlock.validation(...)` should be considered unsafe for V35 jackpot payout acceptance unless updated or documented as legacy-only.

## Remaining legacy accounting paths

Key remaining legacy accounting call sites:

- `Blockchain.shortCheckWithCanonicalContext(...)`
  - after context-aware validation, calls `UtilsBalance.calculateBalance(...)`;
  - should become jackpot-aware for V35 payout candidates.
- `Blockchain.checkFromFile(...)`
  - calls `UtilsBalance.calculateBalance(...)` before context-aware validation;
  - should use the same strip-then-apply proof-aware accounting model as `UtilsResolving.calculateResolvingBalance(...)`.
- `UtilsBlock.validation(...)`
  - updates `balanceForValidation` with `UtilsBalance.calculateBalance(...)`;
  - also uses no-context validation path.
- `Mining.getBalances(...)`
  - calls `UtilsBalance.calculateBalance(...)` for the last block; likely not a controller acceptance gate, but it can report stale balances if the last block is a V35 jackpot payout block.
- `UtilsBalance.calculateBalance(...)`
  - remains legacy by design and is safe only for non-jackpot or stripped jackpot blocks.

## Is a storage-backed integration test feasible without production changes?

Partially feasible:

- A focused save/reload conversion test is feasible using `UtilsBlockToEntityBlock.blockToEntityBlock(...)` and `entityBlockToBlock(...)` without database infrastructure.
- A file JSON round-trip test is feasible using `UtilsJson.objToStringJson(...)` / `jsonToBLock(...)`.
- A full H2 `BlockService.saveAllBLockF(...)` / `findBySpecialIndex(...)` controller-style test may be feasible but requires Spring/JPA test context setup and careful isolation.

Not fully feasible as a quick no-production-change unit test:

- Real `/nodes/resolve_from_to_block` controller integration with DB, file writes, balance persistence, and jackpot proof chain context.
- Full live node/storage propagation scenario.

## Does Phase 4E-1G implementation/test need to happen?

Yes, recommended before moving to Phase 5A.

Reason:

1. Jackpot payout blocks likely survive serialization, but the proof-aware accounting path is not consistently used in reload/short-check running balances.
2. `Blockchain.checkFromFile(...)` is a canonical validation/reload path and still contains legacy balance accounting.
3. `Blockchain.shortCheckWithCanonicalContext(...)` is a core production admission path and still updates its post-block running balance through legacy `calculateBalance(...)`.
4. A jackpot winner spending the payout after the jackpot block is the test that can expose this.
5. The current E2E tests prove mining builder -> context-aware payout proof -> balance helper, but not save/reload nor a later spend after reload.

## Exact files Phase 4E-1G may edit if needed

Tests should be attempted first:

- `C:\citu\unitedStates_final-master\src\test\java\International_Trade_Union\...`
- `C:\citu\unitedStates_storage-master\src\test\java\International_Trade_Union\...`
- `C:\citu\CITU_V35_PHASE4E1G_...md`

If tests prove a production fix is needed, likely allowed candidates:

- `C:\citu\unitedStates_final-master\src\main\java\International_Trade_Union\entity\blockchain\Blockchain.java`
- `C:\citu\unitedStates_storage-master\src\main\java\International_Trade_Union\entity\blockchain\Blockchain.java`
- `C:\citu\unitedStates_final-master\src\main\java\International_Trade_Union\utils\UtilsBalance.java` only for a small reusable context-aware helper if needed
- `C:\citu\unitedStates_storage-master\src\main\java\International_Trade_Union\utils\UtilsBalance.java` only for a small reusable context-aware helper if needed

Avoid editing unless needed:

- `Mining.java`
- `UtilsBlock.java`
- `UtilsResolving.java`
- controllers
- `Block.java`
- `DtoTransaction.java`
- `Account.java`
- transaction/block format
- UI/templates
- website
- `pom.xml`

## Required tests for Phase 4E-1G

Minimum tests:

1. `DtoTransaction` / `Block` JSON round-trip preserves a mining-created jackpot payout.
2. `UtilsBlockToEntityBlock.blockToEntityBlock(...)` / `entityBlockToBlock(...)` preserves a mining-created jackpot payout.
3. `Blockchain.shortCheckWithCanonicalContext(...)` can validate a jackpot payout block and then validate a later spend from the jackpot winner using the credited jackpot balance.
4. `Blockchain.checkFromFile(...)` or a focused equivalent proves save/reload validation does not lose jackpot accounting.
5. `UtilsBalance.calculateBalances(..., founderAddress, forkIndex)` after reload credits valid jackpot payouts and ignores/rejects invalid ones.
6. Disabled V35 path remains legacy with `Long.MAX_VALUE`.

Nice-to-have tests:

- H2-backed `BlockService.saveAllBLockF(...)` / `findBySpecialIndex(...)` round-trip for a jackpot payout block.
- Controller-level `/nodes/resolve_from_to_block` test using mocked or embedded service context.

## Highest risks

1. `Blockchain.checkFromFile(...)` may reload and validate jackpot payout blocks with context-aware validation but update balances through legacy accounting.
2. `Blockchain.shortCheckWithCanonicalContext(...)` may validate a jackpot payout block but not credit the jackpot winner in the running balance used for later blocks in the same batch.
3. `UtilsBlock.validation(...)` remains no-context and should not be used for V35 jackpot payout blocks.
4. Controller/storage integration remains untested with actual `BlockService` persistence.
5. Because V35 remains disabled, all V35 assertions still rely on simulated fork indexes.

## Rollback plan

If Phase 4E-1G exposes regressions:

1. Keep `V35_RESCUE_FORK_INDEX = Long.MAX_VALUE`.
2. Revert only the Phase 4E-1G test/fix files.
3. Preserve existing helper-level jackpot tests and Phase 4E-1E E2E tests.
4. Re-run wallet/server `.\mvnw.cmd test` and `.\mvnw.cmd clean package`.
5. Do not activate V35 until storage/reload and running-balance paths are proven.

## Recommendation

Do Phase 4E-1G controller/storage integration tests before moving to Phase 5A burn/supply.

The controller acceptance path is mostly wired to context-aware validation, and jackpot payout blocks should survive serialization format-wise. But the review found remaining legacy accounting in `Blockchain.shortCheckWithCanonicalContext(...)` and `Blockchain.checkFromFile(...)`, which is enough risk to justify one more jackpot integration phase before burn/supply work.
