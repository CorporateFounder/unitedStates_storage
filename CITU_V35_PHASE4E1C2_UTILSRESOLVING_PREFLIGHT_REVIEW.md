# CITU V35 Phase 4E-1C2 UtilsResolving Preflight Review

Phase: 4E-1C2  
Scope: review only. No Java source code changed.

## 1. Files And Methods Inspected

Wallet:
- `unitedStates_final-master/src/main/java/International_Trade_Union/utils/UtilsResolving.java`
  - `resolve3()`
  - `helpResolve(...)`
  - `rollBackAddBlock3(...)`
  - `addBlock3(...)`
- `unitedStates_final-master/src/main/java/International_Trade_Union/entity/blockchain/Blockchain.java`
  - `shortCheck(...)`
  - `shortCheckWithCanonicalContext(...)`
  - `checkFromFile(...)`
- `unitedStates_final-master/src/main/java/International_Trade_Union/utils/UtilsBlock.java`
  - `validationOneBlock(...)`
  - `validationOneBlockWithCanonicalContext(...)`
  - `validateV35JackpotPayoutWithChainContext(...)`
- `unitedStates_final-master/src/main/java/International_Trade_Union/controllers/BasisController.java`
  - wallet sync paths
  - local mined-block validation path
- `unitedStates_final-master/src/main/java/International_Trade_Union/utils/UtilsBalance.java`
  - `calculateBalance(...)`
  - `calculateBalances(...)`
  - `applyV35JackpotPayoutsWithChainContext(...)`
  - `isValidV35JackpotPayoutForBalance(...)`
- `unitedStates_final-master/src/main/java/International_Trade_Union/model/Mining.java`
  - inspected only for context

Server:
- `unitedStates_storage-master/src/main/java/International_Trade_Union/utils/UtilsResolving.java`
  - `resolve3(List<HostEndDataShortB> hostsList)`
  - `helpResolve(...)`
  - `rollBackAddBlock3(...)`
  - `addBlock3(...)`
- `unitedStates_storage-master/src/main/java/International_Trade_Union/entity/blockchain/Blockchain.java`
  - `shortCheck(...)`
  - `shortCheckWithCanonicalContext(...)`
  - `checkFromFile(...)`
- `unitedStates_storage-master/src/main/java/International_Trade_Union/utils/UtilsBlock.java`
  - `validationOneBlock(...)`
  - `validationOneBlockWithCanonicalContext(...)`
  - `validateV35JackpotPayoutWithChainContext(...)`
- `unitedStates_storage-master/src/main/java/International_Trade_Union/controllers/BasisController.java`
  - `resolve_conflict(...)`
- `unitedStates_storage-master/src/main/java/International_Trade_Union/model/TournamentService.java`
  - candidate validation
  - winner validation
- `unitedStates_storage-master/src/main/java/International_Trade_Union/utils/UtilsBalance.java`
  - `calculateBalance(...)`
  - `calculateBalances(...)`
  - `applyV35JackpotPayoutsWithChainContext(...)`
  - `isValidV35JackpotPayoutForBalance(...)`
- `unitedStates_storage-master/src/main/java/International_Trade_Union/model/Mining.java`
  - inspected only for context

V35 remains disabled:
- wallet `Seting.V35_RESCUE_FORK_INDEX = Long.MAX_VALUE`
- server `Seting.V35_RESCUE_FORK_INDEX = Long.MAX_VALUE`

## 2. UtilsResolving Validation Call Sites Found

Wallet `UtilsResolving.java`:
- `resolve3()` uses `Blockchain.shortCheck(...)` at:
  - line `280`
  - line `347`
  - line `452`
  - line `554`
- `helpResolve(...)` uses `Blockchain.shortCheck(...)` at:
  - line `798`

Server `UtilsResolving.java`:
- `resolve3(List<HostEndDataShortB> hostsList)` uses `Blockchain.shortCheck(...)` at:
  - line `300`
  - line `367`
  - line `473`
  - line `579`
- `helpResolve(...)` uses `Blockchain.shortCheck(...)` at:
  - line `833`

No direct `UtilsBlock.validationOneBlock(...)` call was found in `UtilsResolving`. The validation path is through `Blockchain.shortCheck(...)`.

## 3. UtilsResolving Balance / Accounting Call Sites Found

Wallet `UtilsResolving.java`:
- `addBlock3(...)` starts at line `1013`.
- `addBlock3(...)` uses legacy per-block `calculateBalance(...)` at line `1035`.
- `rollBackAddBlock3(...)` starts at line `917`.
- `rollBackAddBlock3(...)` uses `rollbackCalculateBalance(...)` at line `964`.
- `helpResolve(...)` calls `addBlock3(...)` at lines `831` and `893`.
- `resolve3()` calls `addBlock3(...)` at line `296`.

Server `UtilsResolving.java`:
- `addBlock3(...)` starts at line `1048`.
- `addBlock3(...)` uses legacy per-block `calculateBalance(...)` at line `1070`.
- `rollBackAddBlock3(...)` starts at line `950`.
- `rollBackAddBlock3(...)` uses `rollbackCalculateBalance(...)` at line `997`.
- `helpResolve(...)` calls `addBlock3(...)` at lines `868` and `927`.
- `resolve3(...)` calls `addBlock3(...)` at line `316`.

## 4. Paths With Canonical Chain Context

Potentially enough context, but not currently passed:
- `resolve3(...)` has local `BasisController.getPrevBlock()` / `BasisController.prevBlock()`, `blockService`, remote `subBlocks`, and can load the local canonical prefix with `blockService.findBySpecialIndexBetween(...)`.
- `helpResolve(...)` has:
  - local fork rollback data: `different`
  - candidate replacement blocks: `emptyList`
  - `tempPrevBlock`
  - `blockService`
  - enough data to build canonical context before the first replacement block after rollback.
- `addBlock3(...)` has the blocks being saved and the balances passed in, but it does not know by itself whether upstream validation used full jackpot proof context. It can load prior chain context via `blockService` if modified later.
- `rollBackAddBlock3(...)` has `deleteBlocks`, `saveBlocks`, and the rollback threshold, but currently uses legacy rollback accounting only.

Already wired outside `UtilsResolving` in Phase 4E-1C1:
- wallet/server `Blockchain.checkFromFile(...)`
- wallet/server `Blockchain.shortCheckWithCanonicalContext(...)`
- wallet `BasisController` sync and local mined-block validation paths
- server `BasisController.resolve_conflict(...)`
- server `TournamentService`

## 5. Paths Without Effective V35 Jackpot Context Today

All `UtilsResolving` `Blockchain.shortCheck(...)` calls still use the old no-context overload.

Because `Blockchain.shortCheck(...)` now delegates to `shortCheckWithCanonicalContext(...)` with an empty context and `Seting.V35_RESCUE_FORK_INDEX`, these paths remain legacy/conservative. After V35 activation, if left unchanged, they will still not provide the canonical chain prefix required for jackpot proof validation.

Risky validation paths:
- wallet `resolve3()` line `280`
- wallet `resolve3()` line `347`
- wallet `resolve3()` line `452`
- wallet `resolve3()` line `554`
- wallet `helpResolve(...)` line `798`
- server `resolve3(...)` line `300`
- server `resolve3(...)` line `367`
- server `resolve3(...)` line `473`
- server `resolve3(...)` line `579`
- server `helpResolve(...)` line `833`

## 6. Can UtilsResolving Bypass Context-Aware Jackpot Validation?

Yes, in the sense that it can route downloaded or fork-replacement blocks through `Blockchain.shortCheck(...)` instead of `Blockchain.shortCheckWithCanonicalContext(...)`.

This does not appear to create an acceptance bypass for invalid jackpot payouts, because the no-context path remains conservative and rejects jackpot payout candidates. The practical failure mode is the opposite:
- valid V35 jackpot payout blocks may be rejected by `UtilsResolving` sync/fork paths because canonical context is not supplied.

This is still consensus-critical because a node using `UtilsResolving` may fail to sync or resolve a valid V35 chain once mining starts creating jackpot payouts.

## 7. Can UtilsResolving Mishandle Jackpot Balance Accounting?

Yes.

`UtilsResolving.addBlock3(...)` uses the legacy per-block `UtilsBalance.calculateBalance(...)`:
- wallet line `1035`
- server line `1070`

Legacy `calculateBalance(...)` only allows BASIS_ADDRESS payments to founder or current block miner. It contains this rule:
- if sender is `Seting.BASIS_ADDRESS`, recipient must equal `block.getFounderAddress()` or `block.getMinerAddress()`.

A deterministic jackpot winner is the miner address of a selected historical eligible block, not necessarily the miner address of the payout block. Therefore a valid jackpot payout can be ignored or not credited by `addBlock3(...)` even after validation accepts it.

`UtilsBalance.calculateBalances(List<Block> blocks, String founderAddress, long forkIndex)` already has a context-aware jackpot path:
- removes jackpot payout candidates from the legacy accounting pass
- applies `applyV35JackpotPayoutsWithChainContext(...)`
- validates through `V35JackpotChainContextAdapter.verifyPayoutWithChainContext(...)`

But `UtilsResolving.addBlock3(...)` is not using that full-chain/context-aware path.

## 8. Is Phase 4E-1C3 Required Before Mining.java?

Yes.

Mining must not create jackpot payout transactions until `UtilsResolving` is updated. Two blockers remain:

1. Validation blocker:
   - `UtilsResolving` still calls no-context `Blockchain.shortCheck(...)`.
   - Valid jackpot payout blocks may be rejected during sync/fork resolution.

2. Accounting blocker:
   - `UtilsResolving.addBlock3(...)` still calls legacy per-block `calculateBalance(...)`.
   - Valid jackpot payouts may not credit the deterministic winner.

Phase 4E-1C3 should happen before Phase 4E-1D mining payout wiring.

## 9. Exact Files Phase 4E-1C3 May Edit

Recommended:
- `unitedStates_final-master/src/main/java/International_Trade_Union/utils/UtilsResolving.java`
- `unitedStates_storage-master/src/main/java/International_Trade_Union/utils/UtilsResolving.java`
- wallet/server tests under:
  - `src/test/java/International_Trade_Union/protocol/v35/`
  - `src/test/java/International_Trade_Union/utils/`

Possible only if needed:
- `unitedStates_final-master/src/main/java/International_Trade_Union/utils/UtilsBalance.java`
- `unitedStates_storage-master/src/main/java/International_Trade_Union/utils/UtilsBalance.java`

Report:
- `CITU_V35_PHASE4E1C3_UTILSRESOLVING_CONTEXT_WIRING_REPORT.md`

## 10. Exact Files Phase 4E-1C3 Must NOT Edit

Unless a later phase explicitly allows otherwise:
- `Mining.java`
- `UtilsBlock.java`
- `Blockchain.java`
- `BasisController.java`
- `TournamentService.java`
- `Block.java`
- `DtoTransaction.java`
- `Account.java`
- transaction signing code
- block format / hash format code
- controllers unrelated to resolving
- UI/templates
- website
- `pom.xml`

## 11. Safe Implementation Order If Fix Is Needed

1. Add private helper(s) in wallet/server `UtilsResolving.java`:
   - `v35CanonicalContextBefore(Block prevBlock)`
   - or a batch-aware helper that loads canonical blocks up to `prevBlock.getIndex()`.
   - Strictly return `Collections.emptyList()` while `V35_RESCUE_FORK_INDEX == Long.MAX_VALUE`.

2. Replace each `Blockchain.shortCheck(...)` call in `UtilsResolving` with `Blockchain.shortCheckWithCanonicalContext(...)`.
   - Keep identical legacy arguments.
   - Add canonical prefix context.
   - Pass `Seting.V35_RESCUE_FORK_INDEX`.
   - While V35 is disabled, behavior remains legacy.

3. In `helpResolve(...)`, ensure replacement-chain context is correct:
   - context for first `emptyList` block should represent canonical chain after rollback up to `tempPrevBlock`.
   - subsequent candidate blocks can be appended by `shortCheckWithCanonicalContext(...)` internally.

4. Add context-safe accounting to `addBlock3(...)`.
   - If no block in `originalBlocks` contains a V35 jackpot payout candidate, keep current per-block `calculateBalance(...)`.
   - If V35 is active and jackpot payout candidates exist, use a canonical context path that:
     - uses existing balances as starting point
     - excludes jackpot payout candidates from legacy `calculateBalance(...)`
     - applies `UtilsBalance.applyV35JackpotPayoutsWithChainContext(...)`
     - rejects or returns false if any jackpot payout cannot be proven.

5. Review `rollBackAddBlock3(...)`.
   - For V35 jackpot blocks, legacy `rollbackCalculateBalance(...)` may not reverse jackpot payouts if they were never applied through legacy accounting.
   - Either use a context-aware rollback helper or force full recalculation after rollback/replacement.

6. Add tests before mining:
   - no-context path still conservative
   - `UtilsResolving` calls context-aware shortCheck helper
   - `addBlock3(...)` accounts valid jackpot payouts only when proof-valid
   - invalid/duplicate/immature jackpot payouts are not credited
   - V35 disabled leaves legacy behavior unchanged

## 12. Required Tests

Validation tests:
- Wallet/server: verify `UtilsResolving` exposes or uses a context-aware `shortCheckWithCanonicalContext(...)` route.
- Simulated V35 valid jackpot block is rejected by no-context path but accepted by context-aware route where feasible.
- Simulated V35 wrong winner / immature / duplicate jackpot block is rejected.
- With `V35_RESCUE_FORK_INDEX = Long.MAX_VALUE`, fake jackpot-looking metadata does not alter current resolving behavior.

Accounting tests:
- `addBlock3(...)` or a new internal helper credits a valid jackpot payout winner.
- Wrong winner payout does not credit.
- Immature payout does not credit.
- Duplicate payout does not credit twice.
- Payout recipient can be a historical selected miner, not necessarily the payout block miner.
- `6.00` instant miner reward remains counted separately and is not classified as jackpot.
- Legacy blocks before V35 still use the existing per-block `calculateBalance(...)` behavior.

Rollback/reorg tests:
- Replacing a branch that contains a jackpot payout removes that payout from balances.
- Replacement branch with valid jackpot payout applies exactly once.
- Duplicate replacement payout for same period/type is rejected or not credited.

## 13. Highest Risks

1. `UtilsResolving` can still reject valid V35 jackpot payout blocks because it calls no-context `Blockchain.shortCheck(...)`.
2. `UtilsResolving.addBlock3(...)` can mishandle valid jackpot balances because it uses legacy per-block `calculateBalance(...)`.
3. `rollBackAddBlock3(...)` uses legacy rollback accounting and may not correctly undo future context-applied jackpot payouts.
4. Full signed end-to-end jackpot fixtures still do not exist.
5. Loading full canonical context from storage for each resolving call may be expensive; Phase 4E-1C3 should keep context loading fork-gated and lazy.

## 14. Rollback Plan

If Phase 4E-1C3 causes issues:
1. Revert only the `UtilsResolving.java` context-wiring changes and tests from that phase.
2. Keep previous Phase 4E-1C1 `UtilsBlock` / `Blockchain` context-aware overloads because they are inactive while V35 is disabled and already tested.
3. Do not wire Mining jackpot payout creation until validation and accounting paths pass again.
4. Confirm rollback with:
   - wallet `.\mvnw.cmd test`
   - wallet `.\mvnw.cmd clean package`
   - server `.\mvnw.cmd test`
   - server `.\mvnw.cmd clean package`

## 15. Final Recommendation

Phase 4E-1C3 is required before `Mining.java` creates jackpot payouts.

Recommended next phase:
- Phase 4E-1C3: wire `UtilsResolving` to context-aware `Blockchain.shortCheckWithCanonicalContext(...)` and add context-safe jackpot accounting support in the resolving save path, while keeping V35 disabled and mining untouched.
