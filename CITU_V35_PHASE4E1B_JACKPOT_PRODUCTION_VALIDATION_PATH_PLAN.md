# CITU V35 Phase 4E-1B Jackpot Production Validation Path Plan

Phase: 4E-1B  
Project root: `C:\citu`  
Scope: review production validation paths only. No Java source code changed.

## 1. Files and Methods Inspected

Wallet project:
- `C:\citu\unitedStates_final-master\src\main\java\International_Trade_Union\utils\UtilsBlock.java`
  - `validationOneBlock(...)`
  - `validateV35JackpotPayoutWithChainContext(...)`
- `C:\citu\unitedStates_final-master\src\main\java\International_Trade_Union\entity\blockchain\Blockchain.java`
  - `shortCheck(...)`
  - `checkFromFile(...)`
  - `addBlock(...)`
- `C:\citu\unitedStates_final-master\src\main\java\International_Trade_Union\controllers\BasisController.java`
  - local mining flow around `UtilsBlock.validationOneBlock(...)`
  - conflict/sync methods using `UtilsResolving.addBlock3(...)`
- `C:\citu\unitedStates_final-master\src\main\java\International_Trade_Union\utils\UtilsResolving.java`
  - `addBlock3(...)`
- `C:\citu\unitedStates_final-master\src\main\java\International_Trade_Union\model\Mining.java`
  - V35 miner reward creation branch
- `C:\citu\unitedStates_final-master\src\main\java\International_Trade_Union\utils\UtilsBalance.java`
  - context-aware jackpot accounting helpers
- `C:\citu\unitedStates_final-master\src\main\java\International_Trade_Union\protocol\v35\V35JackpotChainContextAdapter.java`
- `C:\citu\unitedStates_final-master\src\main\java\International_Trade_Union\protocol\v35\V35JackpotPayoutBuilder.java`

Server project:
- `C:\citu\unitedStates_storage-master\src\main\java\International_Trade_Union\utils\UtilsBlock.java`
  - `validationOneBlock(...)`
  - `validateV35JackpotPayoutWithChainContext(...)`
- `C:\citu\unitedStates_storage-master\src\main\java\International_Trade_Union\entity\blockchain\Blockchain.java`
  - `shortCheck(...)`
  - `checkFromFile(...)`
  - `addBlock(...)`
- `C:\citu\unitedStates_storage-master\src\main\java\International_Trade_Union\controllers\BasisController.java`
  - `resolve_conflict(...)`
  - `shouldAcceptResolveCandidate(...)`
  - `isResolveBlockBelowMinimumDifficulty(...)`
- `C:\citu\unitedStates_storage-master\src\main\java\International_Trade_Union\model\TournamentService.java`
  - winner validation and save flow
- `C:\citu\unitedStates_storage-master\src\main\java\International_Trade_Union\utils\UtilsResolving.java`
  - `addBlock3(...)`
- `C:\citu\unitedStates_storage-master\src\main\java\International_Trade_Union\model\Mining.java`
  - V35 miner reward creation branch
- `C:\citu\unitedStates_storage-master\src\main\java\International_Trade_Union\utils\UtilsBalance.java`
  - context-aware jackpot accounting helpers
- `C:\citu\unitedStates_storage-master\src\main\java\International_Trade_Union\protocol\v35\V35JackpotChainContextAdapter.java`
- `C:\citu\unitedStates_storage-master\src\main\java\International_Trade_Union\protocol\v35\V35JackpotPayoutBuilder.java`

## 2. Validation Call Sites Found

### Wallet

`UtilsBlock.validationOneBlock(...)` declaration:
- `unitedStates_final-master/src/main/java/International_Trade_Union/utils/UtilsBlock.java:419`

Call sites:
- `unitedStates_final-master/src/main/java/International_Trade_Union/utils/UtilsBlock.java:1248`
  - internal block/file validation path.
- `unitedStates_final-master/src/main/java/International_Trade_Union/entity/blockchain/Blockchain.java:185`
  - `Blockchain.shortCheck(...)` validates incoming candidate blocks.
- `unitedStates_final-master/src/main/java/International_Trade_Union/entity/blockchain/Blockchain.java:331`
  - `Blockchain.checkFromFile(...)` validates persisted chain blocks.
- `unitedStates_final-master/src/main/java/International_Trade_Union/controllers/BasisController.java:1060`
  - wallet local mining flow validates a freshly mined block before broadcasting.

### Server

`UtilsBlock.validationOneBlock(...)` declaration:
- `unitedStates_storage-master/src/main/java/International_Trade_Union/utils/UtilsBlock.java:419`

Call sites:
- `unitedStates_storage-master/src/main/java/International_Trade_Union/utils/UtilsBlock.java:1247`
  - internal block/file validation path.
- `unitedStates_storage-master/src/main/java/International_Trade_Union/entity/blockchain/Blockchain.java:185`
  - `Blockchain.shortCheck(...)` validates remote/tournament candidate blocks.
- `unitedStates_storage-master/src/main/java/International_Trade_Union/entity/blockchain/Blockchain.java:334`
  - `Blockchain.checkFromFile(...)` validates persisted chain blocks.

No production call site currently passes full V35 jackpot chain context into `validationOneBlock(...)`.

## 3. Remote Block Acceptance Paths

### Server `/nodes/resolve_from_to_block`

File:
- `unitedStates_storage-master/src/main/java/International_Trade_Union/controllers/BasisController.java`

Method:
- `resolve_conflict(@RequestBody SendBlocksEndInfo sendBlocksEndInfo)`

Flow:
1. Receives `sendBlocksEndInfo.getList()` into `blocks`.
2. Clones received blocks into `addlist`.
3. Checks the first candidate block against local `prevBlock`.
4. Applies pre-checks:
   - previous hash
   - `isResolveBlockBelowMinimumDifficulty(...)`
   - timestamp interval
   - block hash equals `hashForTransaction()`
5. Loads balances.
6. Calls `Blockchain.shortCheck(prevBlock, addlist, shortDataBlockchain, lastDiff, tempBalances, sign, UtilsUse.balancesClone(tempBalances), new ArrayList<>())`.
7. If `temp.isValidation()` and `shouldAcceptResolveCandidate(shortDataBlockchain, temp)` passes, adds blocks to `winnerList` and forwards to storage.

Important lines:
- receive endpoint: `BasisController.java:1103`
- clone blocks: `BasisController.java:1145`
- pre-checks: `BasisController.java:1183-1198`
- validation call: `BasisController.java:1202`
- accept decision: `BasisController.java:1246`

Context status:
- Has local `prevBlock`.
- Has candidate `addlist`.
- Has access to `blockService` and local chain metadata.
- Does not currently build or pass a canonical proof chain into `UtilsBlock.validationOneBlock(...)`.
- Current `lastDiff` is an empty list at the call site, so it is not jackpot proof context.

### Server Tournament Winner Path

File:
- `unitedStates_storage-master/src/main/java/International_Trade_Union/model/TournamentService.java`

Flow:
1. Chooses `winner` candidate blocks.
2. Calls `Blockchain.shortCheck(BasisController.prevBlock(), winner, BasisController.getShortDataBlockchain(), lastDiff, tempBalances, sign, UtilsUse.balancesClone(tempBalances), new ArrayList<>())`.
3. If valid, calls `utilsResolving.addBlock3(winner, balances, Seting.ORIGINAL_BLOCKCHAIN_FILE, new ArrayList<>())`.

Important lines:
- validation call: `TournamentService.java:529`
- save call: `TournamentService.java:547`

Context status:
- Has local `prevBlock`.
- Has candidate `winner` blocks.
- Has `blockService`.
- Does not currently pass full canonical chain context to `validationOneBlock(...)`.

### UtilsResolving Save Path

File:
- `unitedStates_storage-master/src/main/java/International_Trade_Union/utils/UtilsResolving.java`

Method:
- `addBlock3(...)`

Flow:
1. Converts each block to an entity.
2. Calls per-block balance calculation.
3. Saves changed accounts.
4. Saves blocks to DB and file.

Important lines:
- method start: `UtilsResolving.java:1048`
- per-block balance accounting: `UtilsResolving.java:1064-1071`
- DB save: `UtilsResolving.java:1088-1090`
- file save: `UtilsResolving.java:1110`

Context status:
- Receives the blocks being saved and current balances.
- Does not currently validate blocks before save.
- Does not currently use the full-chain `UtilsBalance.calculateBalances(...)` context-aware jackpot accounting path.
- This means jackpot-producing mining must still wait until validation and save/accounting paths are aligned.

## 4. Locally Mined Block Paths

### Wallet Local Mining Validation

File:
- `unitedStates_final-master/src/main/java/International_Trade_Union/controllers/BasisController.java`

Flow:
1. Mines a block.
2. Builds `testingValidationsBlock` from recent chain segment.
3. Calls `UtilsBlock.validationOneBlock(...)` directly.
4. Broadcasts the block if local validation does not fail.

Important lines:
- validation context construction: `BasisController.java:1033-1045`
- direct validation call: `BasisController.java:1060`

Context status:
- Has recent chain window plus candidate block.
- Does not have guaranteed full jackpot proof context.
- For jackpot payout blocks, direct `validationOneBlock(...)` must remain conservative unless an overload receives full canonical context.

### Mining.java V35 Reward Branch

Files:
- `unitedStates_final-master/src/main/java/International_Trade_Union/model/Mining.java`
- `unitedStates_storage-master/src/main/java/International_Trade_Union/model/Mining.java`

Current V35 flow:
- creates one BASIS miner instant reward
- amount comes from `V35RewardCalculator.getInstantMinerGoldReward()`
- no founder reward
- no jackpot payout creation

Server important lines:
- V35 branch starts: `Mining.java:243`
- miner reward transaction: `Mining.java:252-259`
- reward signed and added: `Mining.java:260-262`
- block mined: `Mining.java:275-276`

Context status:
- Receives `List<Block> blockchain`, so Mining can pass canonical chain context to `V35JackpotPayoutBuilder`.
- Mining must not create jackpot payouts until validation paths that receive those blocks can also verify them with the same canonical context.

## 5. Paths With Canonical Chain Context

Strong or potentially sufficient context:
- `Mining.miningDay(...)`
  - has `List<Block> blockchain` before the candidate block.
  - suitable for builder input once validation is ready.
- `UtilsBalance.calculateBalances(List<Block> blocks, String founderAddress, long forkIndex)`
  - full-chain context.
  - already designed to prove jackpot payouts before accounting.
- `Blockchain.checkFromFile(...)`
  - iterates persisted chain and can access the full canonical chain from file/DB context, but the current `validationOneBlock(...)` call receives only `tempList`, not full jackpot proof context.
- `BasisController.resolve_conflict(...)`
  - has DB/blockService access and candidate `addlist`; it can construct canonical context before each candidate block, but does not currently do so.
- `TournamentService`
  - has DB/blockService access and candidate winner list; it can construct canonical context before each candidate block, but does not currently do so.

Insufficient current context:
- Plain `UtilsBlock.validationOneBlock(...)`
  - receives `lastBlock`, a rolling difficulty/validation window, not a guaranteed canonical chain.
- Wallet local `BasisController` direct validation call
  - passes `testingValidationsBlock`, a recent segment only.
- `Blockchain.shortCheck(...)`
  - currently creates a rolling `blockList` and passes that to `validationOneBlock(...)`.
  - It sees candidate blocks sequentially, but does not receive a full canonical prefix list.
- `UtilsResolving.addBlock3(...)`
  - saves and accounts blocks after upstream validation; does not independently validate jackpot proof.

## 6. Recommended Safe Validation Wiring Model

Mining must continue to wait. The next implementation phase should wire production validation first.

Recommended model:

1. Add a context-aware validation overload in `UtilsBlock`.
   - Keep existing `validationOneBlock(...)` unchanged for legacy callers.
   - Add an overload or helper that accepts explicit canonical chain context:
     - `List<Block> canonicalChainBeforeBlock`
     - `Block thisBlock`
     - founder address
     - fork index
   - Existing method delegates with no context and keeps rejecting jackpot payouts.

2. In the V35 BASIS transaction branch:
   - miner instant reward remains exactly `6.00`.
   - jackpot payout candidates call `validateV35JackpotPayoutWithChainContext(...)`.
   - if context is missing, reject exactly as today.
   - if proof invalid, reject.
   - if proof valid, allow the jackpot payout transaction.

3. Add a context-aware `Blockchain.shortCheck(...)` overload or an internal context builder.
   - Existing `shortCheck(...)` remains legacy-compatible.
   - New path should pass canonical chain before each candidate block.
   - For a batch of received blocks, context for block `N` must include:
     - local canonical blocks before the batch
     - previously accepted candidate blocks in the same batch before `N`
     - not the payout block itself for prior duplicate scan

4. Update server acceptance paths to use the context-aware validation path:
   - `BasisController.resolve_conflict(...)`
   - `TournamentService` winner validation

5. Update wallet local mining validation path before mining creates jackpot payouts:
   - wallet `BasisController` direct validation must call context-aware validation using the full local chain before the mined block.

6. Update or confirm save/accounting path:
   - `UtilsResolving.addBlock3(...)` currently uses per-block `calculateBalance(...)`.
   - For jackpot payouts, either:
     - upstream validation plus existing `UtilsBalance.applyV35JackpotPayoutsWithChainContext(...)` must be invoked during save/accounting, or
     - `addBlock3(...)` must use a context-safe balance recalculation path for V35 jackpot payout blocks.
   - This is not strictly block validation, but Mining should still wait because accepted jackpot payout blocks must also credit balances consistently.

## 7. Whether Mining.java Can Be Wired After This

Not yet.

`V35JackpotPayoutBuilder` can build valid payout transactions, and `UtilsBlock` has an explicit proof entrypoint, but no production validation path currently calls that entrypoint with canonical context.

Mining must wait until Phase 4E-1C or equivalent completes:
- context-aware block validation exists
- remote candidate validation uses it
- local mined block validation uses it
- tournament winner validation uses it
- save/accounting path can account jackpot payouts consistently

## 8. Exact Files Phase 4E-1C May Edit

Recommended allowed files for Phase 4E-1C:

Wallet:
- `unitedStates_final-master/src/main/java/International_Trade_Union/utils/UtilsBlock.java`
- `unitedStates_final-master/src/main/java/International_Trade_Union/entity/blockchain/Blockchain.java`
- `unitedStates_final-master/src/main/java/International_Trade_Union/controllers/BasisController.java`
- `unitedStates_final-master/src/test/java/International_Trade_Union/protocol/v35/*`
- `unitedStates_final-master/src/test/java/International_Trade_Union/entity/blockchain/*` if needed
- `unitedStates_final-master/src/test/java/International_Trade_Union/controllers/*` if needed

Server:
- `unitedStates_storage-master/src/main/java/International_Trade_Union/utils/UtilsBlock.java`
- `unitedStates_storage-master/src/main/java/International_Trade_Union/entity/blockchain/Blockchain.java`
- `unitedStates_storage-master/src/main/java/International_Trade_Union/controllers/BasisController.java`
- `unitedStates_storage-master/src/main/java/International_Trade_Union/model/TournamentService.java`
- `unitedStates_storage-master/src/test/java/International_Trade_Union/protocol/v35/*`
- `unitedStates_storage-master/src/test/java/International_Trade_Union/entity/blockchain/*` if needed
- `unitedStates_storage-master/src/test/java/International_Trade_Union/controllers/*` if needed
- `unitedStates_storage-master/src/test/java/International_Trade_Union/model/*` if needed

Possible Phase 4E-1C or 4E-1D accounting follow-up:
- `unitedStates_final-master/src/main/java/International_Trade_Union/utils/UtilsResolving.java`
- `unitedStates_storage-master/src/main/java/International_Trade_Union/utils/UtilsResolving.java`
- `unitedStates_final-master/src/main/java/International_Trade_Union/utils/UtilsBalance.java`
- `unitedStates_storage-master/src/main/java/International_Trade_Union/utils/UtilsBalance.java`

## 9. Exact Files Phase 4E-1C Must NOT Edit

Unless a later phase explicitly permits it:
- `Mining.java`
- `Block.java`
- `DtoTransaction.java`
- `Account.java`
- transaction signing code
- block hash format code
- block format fields
- controllers unrelated to block acceptance
- UI/templates
- website
- `pom.xml`

Mining should remain untouched in Phase 4E-1C. The point is to make production validation capable before payout creation.

## 10. Required Implementation Order

1. Add `UtilsBlock.validationOneBlockWithChainContext(...)` or an overload with canonical context and fork index.
2. Preserve current `validationOneBlock(...)` as the no-context conservative path.
3. Add helper tests proving:
   - no-context ordinary validation still rejects jackpot payout candidates
   - context-aware validation accepts a valid jackpot payout
   - context-aware validation rejects wrong winner, immature period, duplicate payout, founder recipient, noncanonical amount
   - `V35_RESCUE_FORK_INDEX = Long.MAX_VALUE` still rejects production jackpot payouts
4. Add `Blockchain.shortCheck(...)` context-aware overload or internal context builder.
5. Wire server `BasisController.resolve_conflict(...)` to context-aware `shortCheck(...)`.
6. Wire server `TournamentService` validation to context-aware `shortCheck(...)`.
7. Wire wallet local mined block validation to the context-aware validation path.
8. Add tests for remote candidate validation and tournament/local mined validation using simulated V35 fork index.
9. Only after these pass, plan Mining jackpot payout creation.

## 11. Required Tests

Core `UtilsBlock` tests:
- ordinary `validationOneBlock(...)` rejects jackpot payout without context
- context-aware validation accepts a valid daily payout
- context-aware validation accepts multiple different period payouts in one block
- duplicate same period payout in same block rejected
- duplicate prior payout rejected
- wrong winner rejected
- founder recipient rejected
- immature payout rejected
- insufficient future hashes rejected
- V35 disabled rejects payout even with otherwise valid context

`Blockchain.shortCheck(...)` tests:
- simulated V35 batch with a valid jackpot payout passes only through context-aware path
- batch context includes previously accepted candidate blocks
- missing canonical prefix rejects jackpot payout
- legacy/pre-V35 shortCheck behavior remains unchanged

Server resolve tests:
- `/nodes/resolve_from_to_block` candidate with valid jackpot proof can reach validation success under simulated V35
- fake jackpot payout is rejected
- positive payout amount without proof is rejected
- V35 disabled preserves current behavior

Tournament tests:
- winner candidate with valid jackpot proof passes context-aware validation under simulated V35
- fake jackpot candidate is rejected
- V35 disabled preserves current tournament behavior

Wallet local mining validation tests:
- locally mined candidate with valid jackpot payout passes context-aware validation under simulated V35
- ordinary no-context path still rejects jackpot payout
- V35 disabled preserves current behavior

Accounting/save tests before Mining wiring:
- after a context-validated jackpot payout block is saved, winner balance increases exactly by payout amount
- invalid jackpot payout does not credit balance
- duplicate payout does not credit twice

## 12. Highest Risks

1. `validationOneBlock(...)` currently receives a short/rolling block list, not full jackpot proof context.
2. If Mining creates jackpot payout transactions before production validation is context-aware, locally mined or remote V35 blocks will be rejected.
3. `Blockchain.shortCheck(...)` is the shared remote/tournament validation funnel; an incorrect context there would affect peer block acceptance.
4. `UtilsResolving.addBlock3(...)` currently saves and accounts blocks after validation and uses per-block balance calculation; jackpot accounting must not be left behind.
5. Batch validation must handle candidate blocks in order so a later block can use earlier candidate blocks as context, while prior duplicate scans do not accidentally include the payout block itself.
6. Exact-slot-only payout policy means validation and mining must agree on `periodEnd + 60 + 1` exactly.
7. Wallet and server paths are similar but not identical. Wallet has a direct local validation call that server-side tournament storage does not mirror exactly.

## 13. Rollback Plan

If Phase 4E-1C creates any validation regression:
1. Revert the context-aware overload wiring in `UtilsBlock`, `Blockchain`, `BasisController`, and `TournamentService`.
2. Keep pure helper classes (`V35JackpotProofService`, `V35JackpotChainContextAdapter`, `V35JackpotPayoutBuilder`) because they are not wired into production behavior.
3. Ensure ordinary `validationOneBlock(...)` returns to the current conservative behavior:
   - legacy before V35
   - V35 jackpot payout candidates rejected without canonical chain context
4. Re-run wallet/server `.\mvnw.cmd test` and `.\mvnw.cmd clean package`.

## 14. Recommendation

Do not wire `Mining.java` yet.

The next phase should be:

Phase 4E-1C: add and wire context-aware jackpot validation into `UtilsBlock`, `Blockchain.shortCheck(...)`, server `BasisController.resolve_conflict(...)`, server `TournamentService`, and wallet local mined-block validation. Keep V35 disabled and keep Mining jackpot payout creation off until those validation paths pass tests.
