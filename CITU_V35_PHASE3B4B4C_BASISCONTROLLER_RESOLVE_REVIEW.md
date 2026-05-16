# CITU V35 Phase 3B-4B4C BasisController Resolve / HashCount Admission Review

Scope: review only. No source code, validation, mining, sync, DTO, UI, website, or comparator behavior was changed.

## Documents Read

- `C:\citu\CITU_V35_PHASE3B4B4A_TOURNAMENT_SERVICE_REVIEW_PLAN.md`
- `C:\citu\CITU_V35_PHASE3B4B4B_TOURNAMENT_SERVICE_REPORT.md`
- `C:\citu\CITU_V35_PHASE3B4B3_FORK_GATE_FIX_REPORT.md`

## V35 Disabled Confirmation

- Wallet: `C:\citu\unitedStates_final-master\src\main\java\International_Trade_Union\setings\Seting.java:362`
  - `long V35_RESCUE_FORK_INDEX = Long.MAX_VALUE;`
- Server: `C:\citu\unitedStates_storage-master\src\main\java\International_Trade_Union\setings\Seting.java:348`
  - `long V35_RESCUE_FORK_INDEX = Long.MAX_VALUE;`

V35 remains disabled.

## Exact Files and Methods Found

### Server Receive / Resolve Admission Path

- `C:\citu\unitedStates_storage-master\src\main\java\International_Trade_Union\controllers\BasisController.java`
  - `winnerList()` at `:173`
  - `updateWinnerListCache(List<Block> currentWinnerList)` at `:232`
  - `subBlocks(SubBlockchainEntity entity)` at `:972`
  - `getBlock(Integer index)` at `:1038`
  - `getPrevBlock()` at `:1093`
  - `resolve_conflict(SendBlocksEndInfo sendBlocksEndInfo)` at `:1102`
  - `dificultyBlockchain()` at `:1412`

The method named by the endpoint is:

- `@PostMapping("/nodes/resolve_from_to_block")`
- Java method: `BasisController.resolve_conflict(@RequestBody SendBlocksEndInfo sendBlocksEndInfo)`
- File: `C:\citu\unitedStates_storage-master\src\main\java\International_Trade_Union\controllers\BasisController.java:1101`

### Server Short Chain Metadata

- `C:\citu\unitedStates_storage-master\src\main\java\International_Trade_Union\entity\blockchain\DataShortBlockchainInformation.java`
  - fields at `:13-19`
    - `isValidation`
    - `hashCount`
    - `staking`
    - `transactions`
    - `bigRandomNumber`
    - `cumulativeWork`
    - `tipHash`
  - constructor preserving legacy fields at `:24-25`
  - V35 metadata constructor at `:29-43`
  - `clone()` at `:48-49`

### Server Short Check / Metadata Population

- `C:\citu\unitedStates_storage-master\src\main\java\International_Trade_Union\entity\blockchain\Blockchain.java`
  - `shortCheck(...)` at `:132`
  - existing metadata read at `:141-153`
  - legacy `hashcount += UtilsUse.powerDiff(block.getHashCompexity())` at `:197`
  - V35 `cumulativeWork = addV35Work(cumulativeWork, block)` at `:198`
  - V35 `tipHash = latestTipHash(tipHash, block)` at `:199`
  - `checkFromFile(...)` accumulation of `cumulativeWork`, `tipHash`, `hashCount`, `staking`, `transactions`, `bigRandomNumber` at `:240-324`
  - `shortInfo(...)` at `:793-809`
  - `addV35Work(BigInteger currentWork, Block block)` at `:812`
  - `v35Work(Block block)` at `:817`
  - `v35Work` gate: `!ProtocolVersion.isV35(block.getIndex())` returns zero at `:818`
  - `latestTipHash(...)` at `:824`
  - `parseCumulativeWork(...)` at `:831`

### Server Comparator / Resolving Protected Path

- `C:\citu\unitedStates_storage-master\src\main\java\International_Trade_Union\model\comparator\HostEndDataShortBComparator.java`
  - `compare(HostEndDataShortB h1, HostEndDataShortB h2)` at `:15`
  - V35 branch call `shouldUseV35Comparison(d1, d2)` at `:19`
  - legacy fallback uses `bigRandomNumber` and `transactions` at `:27-31`
  - strict disabled gate `Seting.V35_RESCUE_FORK_INDEX == Long.MAX_VALUE` at `:34-37`
  - height gate `data.getSize() >= Seting.V35_RESCUE_FORK_INDEX` at `:44-45`
  - V35 scorer call `ChainTournamentScorer.compare(...)` at `:48-49`
  - cumulative work parsing at `:64-72`
  - capped tie-breaker from staking, transactions, random at `:76-80`

- `C:\citu\unitedStates_storage-master\src\main\java\International_Trade_Union\utils\UtilsResolving.java`
  - `resolve3(List<HostEndDataShortB> hostsList)` at `:103`
  - host data pre-sort by `bigRandomNumber` at `:139-142`
  - `isBig(DataShortBlockchainInformation actual, DataShortBlockchainInformation global)` at `:666`
  - `isBig` V35 comparator gate at `:669-670`
  - `isBig` legacy fallback `global.getBigRandomNumber() > actual.getBigRandomNumber()` at `:672`
  - `isSmall(DataShortBlockchainInformation expected, DataShortBlockchainInformation actual)` at `:680`
  - `isSmall` V35 comparator gate at `:681-682`
  - `isSmall` legacy fallback using `size` or `bigRandomNumber` at `:684-689`
  - `helpResolve(...)` at `:699`
  - security rollback checks through `isSmall(...)` at `:838` and `:916`
  - `sortPriorityHost(Set<String> hosts)` at `:1128`
  - `sortPriorityHost` uses `new HostEndDataShortBComparator()` at `:1182`
  - `sendAllBlocksToStorage(List<Block> blocks)` at `:1235`
  - sends to `/nodes/resolve_from_to_block` at `:1287-1288`

### Wallet Matching / Non-Matching Logic

- `C:\citu\unitedStates_final-master\src\main\java\International_Trade_Union\controllers\BasisController.java`
  - `/sub-blocks` at `:346`
  - `/block` at `:357`
  - `/nodes/resolve` at `:370`
  - local `Blockchain.shortCheck(...)` usage at `:451` and `:479`
  - local `/addBlock` path at `:516`
  - `sendAllBlocksToStorage(List<Block> blocks)` at `:687`
  - wallet sends to server `/nodes/resolve_from_to_block` at `:742-743`
  - shortData rebuild uses `shortDataBlockchain.getHashCount() == 0` at `:1199-1200`

No wallet `@PostMapping("/nodes/resolve_from_to_block")` receiver was found. The wallet sends block segments to server storage nodes but does not expose the same server admission endpoint.

## Current Resolve / Admission Flow

Server `BasisController.resolve_conflict(...)` receives `SendBlocksEndInfo` from another node at `/nodes/resolve_from_to_block`.

Observed flow:

1. Rejects incompatible protocol version.
   - `BasisController.java:1121-1124`
2. Reads incoming block segment from `sendBlocksEndInfo.getList()`.
   - `:1126`
3. Rejects stale timestamp against local `prevBlock`.
   - `:1127-1130`
4. Rejects missing miner address.
   - `:1132-1139`
5. Clones incoming blocks into `addlist`.
   - `:1143`
6. Rejects empty list, wrong next index, or duplicate winner candidate.
   - `:1145-1149`
7. Rebuilds local `shortDataBlockchain` if absent, invalid, or `hashCount == 0`.
   - `:1165-1168`
8. Performs first-block pre-check:
   - previous hash must match local tip
   - `hashCompexity` must be at least `Seting.V34_MIN_DIFF`
   - timestamp must be within legacy bounds
   - `hashBlock` must equal `hashForTransaction()`
   - `:1181-1196`
9. Validates the incoming segment and derives temporary metadata with:
   - `Blockchain.shortCheck(prevBlock, addlist, shortDataBlockchain, lastDiff, tempBalances, sign, UtilsUse.balancesClone(tempBalances), new ArrayList<>())`
   - `:1200`
10. Rejects invalid temp metadata unless special conflict handling applies.
    - `:1229-1241`
11. Admits and propagates blocks if:
    - `temp.getSize() > shortDataBlockchain.getSize()`
    - `temp.getHashCount() >= shortDataBlockchain.getHashCount()`
    - `:1244-1245`
12. On admission it:
    - adds incoming blocks to `winnerList`
    - calls `utilsResolving.sendAllBlocksToStorage(addlist)`
    - calls `winnerList()` and propagates any returned block list
    - updates `dificultyOneBlock`
    - returns `OK`
    - `:1253-1267`

## Current Use of HashCount / Random / Stake / Transactions

### In `resolve_from_to_block`

Direct admission uses:

- `temp.getSize() > shortDataBlockchain.getSize()`
- `temp.getHashCount() >= shortDataBlockchain.getHashCount()`

It does not directly use:

- `cumulativeWork`
- `tipHash`
- `HostEndDataShortBComparator`
- `ChainTournamentScorer`
- `UtilsResolving.isBig(...)`
- `UtilsResolving.isSmall(...)`

### In `Blockchain.shortCheck(...)`

`Blockchain.shortCheck(...)` computes all short metadata:

- `hashCount`: legacy `UtilsUse.powerDiff(block.getHashCompexity())`
- `staking`: miner staking balance contribution
- `transactions`: block transaction count
- `bigRandomNumber`: `UtilsUse.bigRandomWinner(block, miner)`
- `cumulativeWork`: V35-only `addV35Work(...)`
- `tipHash`: latest block hash

Since `ProtocolVersion.isV35(block.getIndex())` is false while `V35_RESCUE_FORK_INDEX = Long.MAX_VALUE`, current `cumulativeWork` remains `"0"` for live chain data.

### In `UtilsResolving`

Protected comparator paths now have V35 gates:

- `isBig(...)` uses `HostEndDataShortBComparator.compareV35(...)` only when `shouldUseV35Comparison(...)` allows it.
- `isSmall(...)` does the same.
- `sortPriorityHost(...)` sorts with `HostEndDataShortBComparator`.

Legacy fallback still uses `bigRandomNumber`, size, and transactions.

## Is This Path Consensus-Critical?

Yes.

`BasisController.resolve_conflict(...)` is consensus/admission critical because it receives block segments from other nodes and, after validation, can:

- accept the segment into local winner candidates via `winnerList.addAll(addlist)`
- propagate the segment to storage peers via `utilsResolving.sendAllBlocksToStorage(addlist)`
- influence future candidate block exposure through `winnerList()`
- update local difficulty metadata

Even though full block validation still occurs through `Blockchain.shortCheck(...)`, the final admission decision is a chain-priority decision. After V35, that priority must not be based on legacy `hashCount` when cumulative work is available and active.

## Can This Path Bypass CumulativeWork Comparator?

Yes.

The direct admission rule in `BasisController.resolve_conflict(...)` bypasses the V35 cumulativeWork comparator:

```java
if (temp.getSize() > shortDataBlockchain.getSize()
        && temp.getHashCount() >= shortDataBlockchain.getHashCount()) {
    winnerList.addAll(addlist);
    utilsResolving.sendAllBlocksToStorage(addlist);
    ...
}
```

This bypasses:

- `HostEndDataShortBComparator.shouldUseV35Comparison(...)`
- `HostEndDataShortBComparator.compareV35(...)`
- `ChainTournamentScorer`
- `UtilsResolving.isBig(...)`
- `UtilsResolving.isSmall(...)`

While V35 is disabled, this is expected legacy behavior. After V35 activation, this can become unsafe because a longer chain segment with acceptable legacy `hashCount` could be admitted even if its V35 cumulativeWork is lower than the local chain.

## Additional V35 Admission Risk: V34 Difficulty Floor

`resolve_conflict(...)` currently pre-checks:

```java
checkBlock.getHashCompexity() < Seting.V34_MIN_DIFF
```

That is safe for legacy difficulty but risky for V35 compact target encoding:

- V35 reuses `hashCompexity` as compact target.
- Compact target values are not legacy 100-level difficulty values.
- A raw `V34_MIN_DIFF` floor could reject a valid V35 compact target or accept/interpret a target incorrectly.

Phase 3B-4B4D should review this first-block pre-check and fork-gate it:

- before V35: keep current `V34_MIN_DIFF` check
- after V35: do not use legacy min-difficulty semantics; rely on V35 validation helpers / `Blockchain.shortCheck(...)` / `UtilsBlock` V35 PoW validation

## Does It Already Use HostEndDataShortBComparator / UtilsResolving?

Not for the final `resolve_from_to_block` admission decision.

Related protected paths exist:

- `UtilsResolving.sortPriorityHost(...)` uses `HostEndDataShortBComparator`.
- `UtilsResolving.isBig(...)` and `isSmall(...)` use V35 comparator logic when strictly gated.
- `BasisController.winnerList()` uses `TournamentService.sortWinner(...)`, which was reviewed/wired in Phase 3B-4B4B.

But `BasisController.resolve_conflict(...)` admits blocks using direct local metadata comparison and does not call those protected comparator paths.

## Whether Phase 3B-4B4D Is Needed

Yes. Phase 3B-4B4D should change the server `resolve_from_to_block` admission rule behind a strict V35 fork gate.

Reason:

- Current comparator protections do not cover this direct admission path.
- After V35, direct `size` + `hashCount` admission can conflict with the intended cumulativeWork priority.
- This path is server-side and consensus/admission critical.

Wallet code does not expose the matching receiver, so the implementation is likely server-only unless tests or shared helper parity require otherwise.

## Proposed V35 Behavior If Fix Is Needed

Before V35:

- preserve the exact current legacy admission behavior:
  - `temp.getSize() > shortDataBlockchain.getSize()`
  - `temp.getHashCount() >= shortDataBlockchain.getHashCount()`

After V35 only:

1. Require `Seting.V35_RESCUE_FORK_INDEX != Long.MAX_VALUE`.
2. Require the candidate segment or resulting short metadata to indicate height/index at or after V35.
3. Require `temp.isValidation()`.
4. Parse both `temp.getCumulativeWork()` and `shortDataBlockchain.getCumulativeWork()` as positive `BigInteger` values.
5. Prefer the chain with higher cumulativeWork.
6. If cumulativeWork is equal, prefer higher valid height/size.
7. If still equal, use only capped stake/tx/random tie-breaker logic consistent with `ChainTournamentScorer`.
8. If still equal, use deterministic `tipHash`.
9. Invalid/missing/zero cumulativeWork after V35 must not let legacy `hashCount` override a valid V35 cumulativeWork chain.
10. Do not use random/stake/tx/hashCount as primary priority after V35.

Suggested implementation shape:

- Add a small private helper inside server `BasisController` or use an existing V35 comparator helper if it can be applied without changing DTO behavior:
  - `shouldUseV35ResolveAdmission(DataShortBlockchainInformation local, DataShortBlockchainInformation incoming)`
  - `isIncomingV35ChainPreferred(DataShortBlockchainInformation local, DataShortBlockchainInformation incoming)`
- Keep the current legacy `if` branch intact for disabled/pre-V35 cases.
- Route only the V35-active branch through cumulativeWork comparison.

## Required Fork Gate

The V35 admission branch must be impossible unless all are true:

- `Seting.V35_RESCUE_FORK_INDEX != Long.MAX_VALUE`
- candidate/incoming height is at or beyond `Seting.V35_RESCUE_FORK_INDEX`
- local or incoming metadata contains valid positive V35 `cumulativeWork`
- metadata can be parsed safely

If height/index cannot be determined safely:

- while V35 disabled: use legacy behavior
- after V35: avoid accepting by legacy hashCount as a primary chain-priority metric; fail closed or require valid cumulativeWork metadata, depending on the exact final implementation design

## Exact Files Phase 3B-4B4D May Edit

Recommended allowed edit set:

- `C:\citu\unitedStates_storage-master\src\main\java\International_Trade_Union\controllers\BasisController.java`
  - `resolve_conflict(...)`
  - small private helper methods for V35-gated admission, if needed
- `C:\citu\unitedStates_storage-master\src\main\java\International_Trade_Union\model\comparator\HostEndDataShortBComparator.java`
  - only if existing parsing/comparison helpers need to be exposed safely
- `C:\citu\unitedStates_storage-master\src\main\java\International_Trade_Union\protocol\v35\ChainTournamentScorer.java`
  - only if a pure reusable helper is missing
- Server tests under:
  - `C:\citu\unitedStates_storage-master\src\test\java\...`
- Phase report:
  - `C:\citu\CITU_V35_PHASE3B4B4D_BASISCONTROLLER_RESOLVE_REPORT.md`

## Exact Files Phase 3B-4B4D Must Not Edit

- Wallet code, unless a parity test/report absolutely requires it
- `Block.java`
- `Blockchain.java`, unless a test fixture proves metadata is wrong; no change is expected for 4B4D
- `DataShortBlockchainInformation.java`
- `UtilsResolving.java`, unless direct evidence shows resolve routing needs a shared helper; current bypass is in `BasisController`
- `TournamentService.java`
- `NodeChecker`
- `Mining.java`
- `UtilsBlock.java`
- controllers unrelated to `BasisController.resolve_conflict(...)`
- UI/templates
- website
- `pom.xml`
- transaction, signature, block format, or DTO schema files

## Required Tests Before Implementation

Minimum server tests for Phase 3B-4B4D:

1. V35 disabled legacy preservation
   - With `V35_RESCUE_FORK_INDEX = Long.MAX_VALUE`, positive or fake `cumulativeWork` must not change admission helper behavior.
   - Existing legacy `size` + `hashCount` rule remains the decision rule.

2. Malicious metadata ignored before V35
   - Incoming metadata with huge positive `cumulativeWork` below V35 must not win by V35 logic.
   - Invalid `cumulativeWork` strings must not crash.

3. V35 active simulated helper path
   - If helper accepts explicit fork/height inputs, test:
     - higher cumulativeWork wins
     - lower cumulativeWork loses even with higher `hashCount`, random, stake, or tx count
     - equal cumulativeWork falls to higher size
     - final tie uses deterministic `tipHash`

4. V34 min difficulty pre-check fork behavior
   - Before V35: raw `hashCompexity < V34_MIN_DIFF` remains rejected.
   - After V35: compact target is not judged by the legacy V34 min-diff floor.
   - If direct endpoint tests are too heavy, document fixture blockers and add pure helper tests.

5. Endpoint fixture if practical
   - Build `SendBlocksEndInfo` with candidate blocks and confirm `resolve_conflict(...)` uses legacy path while V35 disabled.
   - Full endpoint tests may be blocked by service dependencies, signatures, balances, database state, and file-backed chain state.

## Safe Implementation Order for Phase 3B-4B4D

1. Add or expose a pure server-side helper to decide whether V35 resolve admission is active.
2. Add tests proving V35 disabled ignores fake positive cumulativeWork.
3. Add tests for helper-level V35 active behavior with simulated metadata.
4. Fork-gate the current `temp.size/hashCount` admission condition in `BasisController.resolve_conflict(...)`.
5. Keep the legacy branch byte-for-byte close to current behavior for pre-V35/disabled cases.
6. Add V35 branch using cumulativeWork-first comparison.
7. Gate or replace the `V34_MIN_DIFF` pre-check for V35 compact targets.
8. Run server tests/build.
9. Run wallet tests/build to confirm no cross-project regression.
10. Create Phase 3B-4B4D report.

## Risk List

1. High: Direct `size` + `hashCount` admission can bypass cumulativeWork priority after V35.
2. High: The raw `Seting.V34_MIN_DIFF` pre-check can conflict with compact target semantics after V35.
3. Medium: Full endpoint tests may be hard because `resolve_conflict(...)` depends on block signatures, balances, service state, file-backed chain state, `winnerList`, and network propagation.
4. Medium: `Blockchain.shortCheck(...)` currently calculates both legacy `hashCount` and V35 `cumulativeWork`; Phase 3B-4B4D must not accidentally reinterpret pre-V35 `hashCompexity`.
5. Medium: Missing or malformed remote metadata must fail safely and must not crash the controller.
6. Low/Medium: Wallet sends to `/nodes/resolve_from_to_block` but does not receive it; server-side fix should be enough, but wallet behavior should be smoke-checked after server changes.

## Rollback Plan

If Phase 3B-4B4D causes regressions:

1. Revert only the Phase 3B-4B4D server `BasisController` admission changes and related tests/report.
2. Leave prior V35 helper, comparator, metadata, and TournamentService phases untouched.
3. Confirm `V35_RESCUE_FORK_INDEX` remains `Long.MAX_VALUE`.
4. Rerun server and wallet `.\mvnw.cmd test` and `.\mvnw.cmd clean package`.
5. Re-open the plan with a smaller pure-helper-only implementation if endpoint-level wiring proves too risky.

## Conclusion

`BasisController.resolve_conflict(...)` is a remaining server-side chain admission bypass. It validates incoming blocks through `Blockchain.shortCheck(...)`, but then admits them with direct legacy `size` and `hashCount` checks instead of the V35 cumulativeWork comparator. While V35 is disabled this preserves legacy behavior. Before V35 activation, Phase 3B-4B4D should fork-gate this admission path so cumulativeWork is primary after V35 and `hashCount`, random, staking, and transactions cannot override stronger V35 PoW work.
