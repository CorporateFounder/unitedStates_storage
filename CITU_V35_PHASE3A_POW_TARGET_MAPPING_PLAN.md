# CITU V35 Phase 3A PoW Target Mapping Plan

Date: 2026-05-13

Scope: planning only. No Java source, consensus behavior, mining behavior, validation behavior, hash format, block format, transaction format, difficulty behavior, UI, website, or `pom.xml` files were changed.

## Goal For Phase 3B

Implement V35 Bitcoin-like target-based proof of work behind `Seting.V35_RESCUE_FORK_INDEX`, while V35 remains disabled until a later activation phase.

After V35:

- miners no longer choose network difficulty manually;
- block hash validation is `hashNumber <= target`;
- target adjusts automatically over a rolling 60-block window;
- emergency difficulty drop prevents long stalls;
- chain selection uses cumulative work first;
- staking and transaction score can only be capped tie-breakers;
- all pre-V35 blocks continue to use their legacy PoW and difficulty rules.

## Exact Files And Methods Found

### Wallet PoW And Difficulty

Project: `C:\citu\unitedStates_final-master`

- `src\main\java\International_Trade_Union\model\Mining.java`
  - `private static int customDiff`, line 49.
  - `getCustomDiff()`, lines 66-68.
  - `setCustomDiff(int)`, lines 70-72.
  - `miningDay(...)`, line 117.
  - legacy difficulty selection: `long difficulty = UtilsBlock.difficulty(...)`, line 238.
  - manual override after V34: `if (index >= Seting.V34_NEW_ALGO) difficulty = Mining.customDiff;`, lines 239-240.
  - block construction uses selected `difficulty`: `new Block(..., difficulty, index)`, near line 448 after Phase 2B reward wiring.

- `src\main\java\International_Trade_Union\controllers\MineController.java`
  - `/mining` adds `customDiff` to the template, line 46.
  - `POST /customDiff` calls `Mining.setCustomDiff(...)`, lines 50-53.

- `src\main\java\International_Trade_Union\utils\UtilsBlock.java`
  - `difficulty(List<Block>, long, int)`, lines 262-365.
  - legacy hash validation in `validationOneBlock(...)`, lines 982-984:
    - `BlockchainDifficulty.calculateTarget(thisBlock.getHashCompexity())`
    - `BlockchainDifficulty.calculateTargetV30(thisBlock.getHashCompexity())`
    - `UtilsUse.chooseComplexity(...)`
  - legacy expected-difficulty validation for old range, lines 996-1008.
  - V34 minimum difficulty floor check, lines 1009-1014.
  - block hash serialization check, lines 1017-1029.

- `src\main\java\International_Trade_Union\utils\UtilsUse.java`
  - `chooseComplexity(String, long, long, String, BigInteger)`, lines 238-267.
  - `hashComplexity(String, long)`, starts line 270.
  - `hashCount(String, long)`, lines 330-338.
  - `powerDiff(long)`, lines 384-386; currently returns `diff`, not true work.
  - `bigRandomWinner(Block, Account)`, starts line 396.

- `src\main\java\International_Trade_Union\utils\BlockchainDifficulty.java`
  - `meetsDifficulty(byte[], long)`, line 22.
  - `v2MeetsDifficulty(byte[], long)`, line 48.
  - `v3MeetsDifficulty(byte[], long)`, line 52.
  - `v4MeetsDifficulty(String, long)`, line 58.
  - `calculateTarget(long)`, line 67.
  - `isValidHash(String, String)`, line 86.
  - `getBitSum(String)`, line 94.
  - `getBitSum2(String)`, line 110.
  - `isValidHashV29(String, int)`, line 126.
  - `isValidBlockV29(Block, int)`, line 134.
  - `calculateTargetV30(long)`, line 167.
  - `isValidHashV30(String, BigInteger)`, line 173.

- `src\main\java\International_Trade_Union\utils\UtilsDIfficult.java`
  - `getAdjustedDifficulty(...)`, line 28.
  - `v28_changeAlgorith_diff(...)`, line 52.
  - `v30_1_changeAlgorith_diff(...)`, line 90.
  - `v30_changeAlgorith_diff(...)`, line 127.
  - `v2getAdjustedDifficultyMedian(...)`, line 167.
  - `getAdjustedDifficultyMedian(...)`, line 203.
  - `adjustDifficulty03112(...)`, line 239.

- `src\main\java\International_Trade_Union\entity\blockchain\block\Block.java`
  - field `randomNumberProof`, line 84.
  - field `hashCompexity`, line 89.
  - `hashForTransaction()`, lines 94-115.
  - constructor with `hashCompexity`, lines 129-139.
  - full constructor with `hashCompexity`, lines 143-154.
  - inner `BlockForHash`, lines 158-213.
  - `multipleFindHash(long)`, starts around line 258.
  - `chooseMultiString(long, boolean)`, starts around line 369.
  - `findHash(long)`, starts around line 385.
  - `findHash_MT2(long)`, starts around line 422.
  - `findHash_org(long)`, starts around line 589.

### Server PoW, Difficulty, And Sync

Project: `C:\citu\unitedStates_storage-master`

The server has matching wallet PoW/difficulty classes and methods with small line offsets:

- `src\main\java\International_Trade_Union\model\Mining.java`
  - `customDiff`, `getCustomDiff()`, `setCustomDiff(int)`, and `miningDay(...)`.
  - manual V34+ override is the same concept as wallet.

- `src\main\java\International_Trade_Union\utils\UtilsBlock.java`
  - `difficulty(...)`.
  - `validationOneBlock(...)`.
  - same `chooseComplexity(...)` hash validation path.

- `src\main\java\International_Trade_Union\utils\UtilsUse.java`
  - same `chooseComplexity(...)`, `hashCount(...)`, `powerDiff(...)`, `bigRandomWinner(...)`.

- `src\main\java\International_Trade_Union\utils\BlockchainDifficulty.java`
  - same public PoW helper names.

- `src\main\java\International_Trade_Union\utils\UtilsDIfficult.java`
  - same legacy adjustment method names.

- `src\main\java\International_Trade_Union\entity\blockchain\block\Block.java`
  - same `hashCompexity`, nonce, hash serialization, and mining helper method names.

Server-only sync/tournament:

- `src\main\java\International_Trade_Union\entity\blockchain\DataShortBlockchainInformation.java`
  - fields: `size`, `isValidation`, `hashCount`, `staking`, `transactions`, `bigRandomNumber`, lines 9-14.
  - constructor and clone preserve those fields, lines 19-31.

- `src\main\java\International_Trade_Union\entity\blockchain\Blockchain.java`
  - `shortCheck(...)`, lines 129-217.
  - `checkFromFile(...)`, starts line 221.
  - `rollBackShortCheck(...)`, starts line 749.
  - `shortCheck(...)` and `checkFromFile(...)` add `UtilsUse.powerDiff(block.getHashCompexity())` to `hashCount`, lines 192 and 285.
  - `shortCheck(...)` adds staking, big random, and transaction count, lines 197-201.
  - `rollBackShortCheck(...)` subtracts the same metrics, lines 765-769.

- `src\main\java\International_Trade_Union\model\TournamentService.java`
  - `sortWinner(Map<String, Account>, List<Block>)`, lines 79-100.
  - ranking today: `bigRandomWinner`, then `hashCompexity`, then miner staking, then transaction count, lines 83-92.
  - `validateAndAddBlock(...)` validates candidates through `Blockchain.shortCheck(...)`, lines 261-277.
  - tournament winner selection calls `sortWinner(...)`, line 374.
  - selected winner is validated with `Blockchain.shortCheck(...)`, line 399.

- `src\main\java\International_Trade_Union\utils\UtilsResolving.java`
  - `isBig(...)`, lines 666-672; currently compares only `bigRandomNumber`.
  - `isSmall(...)`, lines 677-684; compares size and `bigRandomNumber`.
  - `sortPriorityHost(Set<String>)`, lines 1122-1180; fetches `/datashort`, requires validation, sorts with `HostEndDataShortBComparator`.

- `src\main\java\International_Trade_Union\model\comparator\HostEndDataShortBComparator.java`
  - `compare(...)`, lines 11-27.
  - bug: `if(d1.getSize() != d1.getSize())`, line 15, is always false.
  - then compares `bigRandomNumber`, lines 20-23.
  - then compares `transactions`, line 26.

## Current Legacy PoW / Difficulty Flow

1. Mining calls `UtilsBlock.difficulty(...)` to get a legacy difficulty value.
2. After V34, `Mining.miningDay(...)` overrides that value with `Mining.customDiff`.
3. The selected value is stored in the block field `hashCompexity`.
4. `Block` mining methods search nonce values in `randomNumberProof`.
5. For each candidate nonce, block hash is computed using existing `BlockForHash` serialization and `hashForTransaction()` / `hashForBlockchain()` behavior.
6. Candidate hashes are tested through `UtilsUse.chooseComplexity(...)`.
7. `UtilsUse.chooseComplexity(...)` dispatches by historical fork index:
   - before `NEW_START_DIFFICULT`: string leading-zero count through `hashComplexity(...)`;
   - early era: `BlockchainDifficulty.meetsDifficulty(...)`;
   - after `CHANGE_MEET_DIFFICULTY`: exact leading-zero count via `v2MeetsDifficulty(...)`;
   - V3/V4 era: `v3MeetsDifficulty(...)` / `v4MeetsDifficulty(...)`;
   - V28 era: `isValidHash(hash, target)`;
   - V29 era: bit-sum check through `isValidHashV29(...)`;
   - V30 era: `isValidHashV30(hash, calculateTargetV30(diff))`;
   - after `V31_DIFF_END_MINING`: returns to `isValidHashV29(...)`.
8. Validation repeats the same hash check in `UtilsBlock.validationOneBlock(...)`.

## Current Manual Difficulty Flow

Wallet:

- `MineController.miming(...)` exposes `Mining.getCustomDiff()` to `templates\mining.html`.
- `POST /customDiff` calls `Mining.setCustomDiff(Integer.valueOf(customDiff))`.
- `Mining.setCustomDiff(int)` floors the value to `Seting.V34_MIN_DIFF`.
- `Mining.miningDay(...)` ignores automatic `UtilsBlock.difficulty(...)` after `Seting.V34_NEW_ALGO` and uses `Mining.customDiff`.

Server:

- Server `Mining.java` has the same manual/static `customDiff` behavior.
- Server mining/tournament uses candidate blocks from peers, but local block creation still depends on the same `Mining.miningDay(...)` path where applicable.

V35 requirement:

- After V35, `Mining.customDiff` must not determine network target.
- The UI may still offer local Eco/Normal/Boost modes later, but those modes must only affect local CPU/thread behavior, not block target.

## Current Hash Validation Flow

Validation call stack:

1. `Blockchain.shortCheck(...)` and `Blockchain.checkFromFile(...)` call `UtilsBlock.validationOneBlock(...)`.
2. `UtilsBlock.validationOneBlock(...)` recomputes target helpers from `thisBlock.getHashCompexity()`.
3. It calls `UtilsUse.chooseComplexity(thisBlock.getHashBlock(), thisBlock.getHashCompexity(), thisBlock.getIndex(), target, bigTarget)`.
4. If false, validation rejects the block as `wrong difficulty hash`.
5. For older ranges, validation also checks that `thisBlock.getHashCompexity()` equals the expected result of `UtilsBlock.difficulty(...)`.
6. After V34, validation only checks a minimum floor: `thisBlock.getHashCompexity() >= Seting.V34_MIN_DIFF` unless test mode is enabled.
7. Validation separately verifies `thisBlock.getHashBlock().equals(thisBlock.hashForTransaction())` after `NEW_CHECK_UTILS_BLOCK`.

Important compatibility point:

- `hashCompexity` participates in block hash serialization through `BlockForHash`. Changing the meaning of the field after V35 is possible, but changing its name, type, or serialization is not safe.

## Current Chain Tournament / Scoring Flow

Server chain metadata:

- `DataShortBlockchainInformation.hashCount` is derived from `UtilsUse.powerDiff(block.getHashCompexity())`.
- `UtilsUse.powerDiff(long)` currently returns the input `diff`; it is not cumulative work.
- `staking` is accumulated from miner staking.
- `transactions` is accumulated from transaction count.
- `bigRandomNumber` is accumulated from `UtilsUse.bigRandomWinner(...)`.

Server priority host sorting:

- `UtilsResolving.sortPriorityHost(...)` fetches `/datashort` from peers.
- Invalid peers are filtered out.
- `HostEndDataShortBComparator` sorts by `bigRandomNumber` and then `transactions`; intended size comparison is broken by `d1.getSize() != d1.getSize()`.

Server winner selection:

- `TournamentService.sortWinner(...)` ranks block candidates by:
  1. `bigRandomWinner`;
  2. `hashCompexity`;
  3. miner staking;
  4. transaction count.

V35 problem:

- These metrics can let random/stake/transaction score influence or override PoW. V35 must make valid-chain cumulative work primary.

## Proposed V35 Target-Based PoW Design

### Keep Block Format Unchanged

Do not add a target field to `Block`.

Use the existing `hashCompexity` long field after V35 as a compact target encoding. This is the least invasive option because:

- the field is already serialized;
- the field is already included in block hashing;
- mining and validation already pass the field into PoW checks;
- old blocks can still treat the same field as legacy difficulty because interpretation is fork-gated.

### Compact Target Representation

Recommended representation:

- Use a Bitcoin-style compact target value stored in the lower 32 bits of Java `long`.
- Decode it to `BigInteger target` for validation.
- Encode adjusted `BigInteger target` back to compact form for storage in `hashCompexity`.
- Keep the misspelled field name `hashCompexity`; do not rename it.

Example semantics:

- pre-V35: `hashCompexity` means legacy difficulty number.
- V35+: `hashCompexity` means compact target bits.

Migration risk:

- Existing code and logs call this field difficulty and compare it numerically. Phase 3B must make every post-V35 path decode it as target bits and must not use larger numeric `hashCompexity` as "harder."
- `TournamentService.sortWinner(...)` currently prefers higher `hashCompexity`; after V35 this is wrong because compact target ordering is not the same as cumulative work.

### Hash Validation Rule

Add or complete pure helpers in `International_Trade_Union.protocol.v35`:

- `V35TargetDifficulty.decodeCompactTarget(long compactTarget)`
- `V35TargetDifficulty.encodeCompactTarget(BigInteger target)`
- `V35PowValidator.isHashUnderTarget(String hashHex, BigInteger target)`
- `V35WorkCalculator.workForTarget(BigInteger target)`

Validation rule:

```text
hashNumber = new BigInteger(hashHex, 16)
valid = hashNumber <= decodedTarget
```

Hash format must remain the existing `Block.hashForTransaction()` output. Do not alter `BlockForHash`, `randomNumberProof`, or transaction serialization.

## Proposed Automatic Difficulty Adjustment Design

Constants already exist in both wallet and server:

- `V35_TARGET_WINDOW = 60`
- `V35_TARGET_BLOCK_TIME_SECONDS = 150`
- `V35_MAX_TARGET_UP = 1.25`
- `V35_MAX_TARGET_DOWN = 0.50`
- `V35_EMERGENCY_DELAY_SECONDS = 1800`

Additional constants likely needed in Phase 3B:

- `V35_MAX_TARGET_HEX` or reuse `MAX_TARGET_v30` only if explicitly accepted.
- `V35_INITIAL_COMPACT_TARGET`, used for the first V35 block.
- optional `V35_MIN_TARGET_HEX` or max difficulty guard.

Recommended formula:

```text
window = last 60 V35-aware blocks, or available blocks at fork start
expectedSeconds = windowBlockCount * 150
actualSeconds = latest.timestamp - first.timestamp
ratio = actualSeconds / expectedSeconds
clampedRatio = clamp(ratio, 0.50, 1.25)
newTarget = previousTarget * clampedRatio
newTarget = clamp(newTarget, minTarget, maxTarget)
compact = encodeCompactTarget(newTarget)
```

Interpretation:

- If blocks are too fast, `actualSeconds` is smaller, ratio approaches `0.50`, target goes down, mining becomes harder.
- If blocks are too slow, ratio approaches `1.25`, target goes up, mining becomes easier.
- Adjustment should happen for every V35 block using the rolling window, not only at old `DIFFICULTY_ADJUSTMENT_INTERVAL` boundaries, unless Phase 3B deliberately chooses boundary updates and documents why.

Implementation location:

- Put pure target math in `V35TargetDifficulty`.
- Call it from `UtilsBlock.difficulty(...)` or a new `UtilsBlock.v35CompactTarget(...)` branch.
- Do not edit old `UtilsDIfficult` formulas except to avoid calling them after V35.

## Proposed Emergency Difficulty Drop Design

Purpose:

- If block production stalls longer than `V35_EMERGENCY_DELAY_SECONDS = 1800`, allow a bounded easier target without waiting for a full adjustment window.

Suggested rule:

```text
lastBlockTime = previousBlock.timestamp
candidateTime = candidateBlock.timestamp
delaySeconds = candidateTime - lastBlockTime

if delaySeconds >= 1800:
    emergencyRatio = min(1.25, delaySeconds / 1800)
    emergencyTarget = previousTarget * emergencyRatio
    target = min(emergencyTarget, maxTarget)
else:
    target = normalAdjustedTarget
```

Safety constraints:

- Candidate timestamp validation must already reject impossible future timestamps before emergency easing is trusted.
- Emergency easing must be deterministic from chain data only.
- Emergency easing should be bounded per block by `V35_MAX_TARGET_UP`.
- Emergency easing must not apply before V35.

## Proposed Cumulative Work Design

For target-based PoW, work should be derived from target, not from `hashCompexity` as a linear number.

Recommended per-block work:

```text
maxHash = 2^256 - 1
work = floor(maxHash / (target + 1)) + 1
```

Use:

- `BigInteger` for exact work.
- cumulativeWork = previous cumulativeWork + blockWork.
- expose as decimal string in API/metadata if JSON integer size compatibility is a concern.

Where to wire later:

- `V35WorkCalculator`: pure work math.
- `Blockchain.shortCheck(...)`: accumulate V35 cumulativeWork after the fork.
- `Blockchain.checkFromFile(...)`: compute cumulativeWork for local chain metadata.
- `Blockchain.rollBackShortCheck(...)`: subtract V35 work on rollback.
- `DataShortBlockchainInformation`: add `cumulativeWork` and tip hash fields, or create a versioned V35 metadata object.
- `UtilsResolving.isBig(...)` / `isSmall(...)`: compare valid chains by cumulativeWork first after V35.
- `HostEndDataShortBComparator`: sort valid hosts by cumulativeWork, then height, then deterministic tie-breaker.
- `TournamentService.sortWinner(...)`: after V35, choose candidate block by resulting chain cumulativeWork / valid target work, not `bigRandomWinner`.

Tie-break order after V35:

1. valid chain;
2. highest cumulativeWork;
3. latest valid height;
4. capped stake/transaction score only if work and height tie;
5. deterministic hash tie-breaker, such as lexicographically lowest tip hash.

## Wallet / Server Differences

- Wallet and server PoW classes have the same method names but file hashes differ, so Phase 3B must patch both intentionally and not assume byte-identical files.
- Wallet exposes manual difficulty through `MineController` and the wallet mining template. Phase 3B should only make consensus ignore manual difficulty after V35; UI hiding belongs to a later UI phase unless explicitly allowed.
- Server has tournament and chain-resolve classes that wallet does not rely on in the same way.
- Both projects duplicate `Block`, `UtilsBlock`, `UtilsUse`, `UtilsDIfficult`, and `BlockchainDifficulty`, so target math and tests must be mirrored exactly.

## Exact Implementation Order For Phase 3B

1. Add tests first in both wallet and server:
   - compact target encode/decode;
   - hash under/equal/over target;
   - work calculation;
   - 60-block target adjustment;
   - max target up/down caps;
   - emergency drop;
   - pre-V35 legacy path remains untouched.

2. Extend pure V35 helper classes:
   - `V35TargetDifficulty` for compact target and target adjustment math;
   - `V35PowValidator` for `hashNumber <= target`;
   - `V35WorkCalculator` for per-block work and cumulative work.

3. Add any missing V35 target constants in both `Seting.java` files, only if required:
   - initial compact target;
   - max target hex;
   - min target hex or max difficulty guard.

4. Add a fork-gated V35 target-selection helper:
   - Keep `UtilsBlock.difficulty(...)` legacy behavior for pre-V35.
   - For V35+, return compact target bits, not a legacy difficulty number.
   - Consider naming a new helper `v35NextCompactTarget(...)` internally to avoid confusing old difficulty methods.

5. Update mining only after pure helper tests pass:
   - In `Mining.miningDay(...)`, do not use `Mining.customDiff` when `ProtocolVersion.isV35(index)` is true.
   - Use the V35 compact target from chain history.
   - Pass the compact target through the existing `Block` constructor as `hashCompexity`.

6. Update block nonce search after V35:
   - In `Block.findHash_org(...)`, `findHash_MT2(...)`, and `multipleFindHash(...)`, branch to V35 hash-under-target validation when `ProtocolVersion.isV35(index)`.
   - Do not alter old hash serialization or old mining loops except for the fork-gated predicate.

7. Update validation after V35:
   - In `UtilsBlock.validationOneBlock(...)`, if V35, decode `thisBlock.getHashCompexity()` as compact target.
   - Confirm block hash equals `hashForTransaction()` as before.
   - Confirm `hashNumber <= target`.
   - Confirm compact target equals the expected target from the previous 60-block window and emergency rule.
   - Leave all pre-V35 branches unchanged.

8. Add cumulativeWork metadata:
   - Prefer a V35-specific chain score DTO if API compatibility is risky.
   - Otherwise add backward-compatible nullable/string fields to `DataShortBlockchainInformation`.
   - Update `Blockchain.shortCheck(...)`, `checkFromFile(...)`, and `rollBackShortCheck(...)`.

9. Update server sync/tournament after cumulativeWork tests exist:
   - `UtilsResolving.isBig(...)`
   - `UtilsResolving.isSmall(...)`
   - `HostEndDataShortBComparator.compare(...)`
   - `TournamentService.sortWinner(...)`
   - keep legacy behavior before V35.

10. Run wallet and server tests/builds:
    - `cd C:\citu\unitedStates_final-master; .\mvnw.cmd test`
    - `cd C:\citu\unitedStates_final-master; .\mvnw.cmd clean package`
    - `cd C:\citu\unitedStates_storage-master; .\mvnw.cmd test`
    - `cd C:\citu\unitedStates_storage-master; .\mvnw.cmd clean package`

## Files Phase 3B May Edit

Wallet and server:

- `src\main\java\International_Trade_Union\protocol\v35\V35TargetDifficulty.java`
- `src\main\java\International_Trade_Union\protocol\v35\V35PowValidator.java`
- `src\main\java\International_Trade_Union\protocol\v35\V35WorkCalculator.java`
- `src\main\java\International_Trade_Union\setings\Seting.java`, only for missing V35 target constants.
- `src\main\java\International_Trade_Union\model\Mining.java`
- `src\main\java\International_Trade_Union\entity\blockchain\block\Block.java`, only for V35-gated mining predicate, not serialization.
- `src\main\java\International_Trade_Union\utils\UtilsBlock.java`
- `src\main\java\International_Trade_Union\utils\BlockchainDifficulty.java`, preferably only to add pure V35 helpers if not kept under `protocol.v35`.
- `src\main\java\International_Trade_Union\utils\UtilsUse.java`, only if `chooseComplexity(...)` must dispatch to V35.
- tests under `src\test\java\International_Trade_Union\protocol\v35\`.

Server only:

- `src\main\java\International_Trade_Union\entity\blockchain\DataShortBlockchainInformation.java`, or a new V35 chain-score DTO.
- `src\main\java\International_Trade_Union\entity\blockchain\Blockchain.java`
- `src\main\java\International_Trade_Union\utils\UtilsResolving.java`
- `src\main\java\International_Trade_Union\model\comparator\HostEndDataShortBComparator.java`
- `src\main\java\International_Trade_Union\model\TournamentService.java`

## Files Phase 3B Must Not Edit

Unless a later phase explicitly expands scope:

- `DtoTransaction.java`
- transaction signing or `DtoTransaction.toSign()`
- `Account.java`
- controllers, except no UI/API changes in Phase 3B
- wallet templates/static files
- website files
- `pom.xml`
- old block serialization logic in `BlockForHash`
- old pre-V35 difficulty formula internals in `UtilsDIfficult`

## Required Tests Before Implementation

Pure target tests:

- compact target decode known values.
- compact target encode/decode round trip.
- invalid compact target rejected safely.
- `hashNumber <= target` accepts lower and equal hashes.
- hash greater than target rejects.
- invalid/null/empty hashes reject.

Difficulty adjustment tests:

- 60 blocks at 150 seconds keep target stable.
- fast 60-block window clamps target by `V35_MAX_TARGET_DOWN = 0.50`.
- slow 60-block window clamps target by `V35_MAX_TARGET_UP = 1.25`.
- first V35 block uses deterministic initial target.
- target never exceeds max target.
- target never goes below min target if min is defined.

Emergency tests:

- delay below 1800 seconds does not trigger emergency rule.
- delay at or above 1800 seconds eases target.
- emergency easing is capped by `1.25`.
- impossible future timestamp remains rejected by existing timestamp validation.

Mining/validation tests:

- V35 mining ignores `Mining.customDiff`.
- V35 mined block stores compact target in `hashCompexity`.
- V35 validation rejects hash over target.
- V35 validation rejects wrong compact target even if hash is under that easier target.
- pre-V35 block still validates with legacy `chooseComplexity(...)`.
- V34 manual difficulty remains unchanged before V35.

Cumulative work and tournament tests:

- per-block work increases when target becomes harder.
- cumulativeWork is sum of V35 block work.
- higher cumulativeWork beats higher height with lower work.
- staking/tx tie-breaker cannot beat much higher cumulativeWork.
- deterministic hash tie-breaker is stable.
- `HostEndDataShortBComparator` no longer ignores size due to the existing `d1.getSize() != d1.getSize()` bug.

Parity tests:

- wallet/server compact target helpers have matching signatures and outputs.
- wallet/server V35 target adjustment fixtures produce the same compact target.
- wallet/server V35 work fixtures produce the same cumulativeWork.

## Risk List

- `hashCompexity` is misspelled and historically means several different difficulty concepts. V35 can reuse it only if every V35 path decodes it as compact target.
- Full 256-bit target cannot fit directly in a Java `long`; compact encoding is required if block format remains unchanged.
- Existing `BlockchainDifficulty.isValidHash(String, String)` uses string comparison; V35 should use `BigInteger` comparison only.
- Existing post-V31 `chooseComplexity(...)` uses bit-sum `isValidHashV29(...)`, not target comparison.
- Manual `customDiff` currently overrides network difficulty after V34. Forgetting one branch would let users choose easier V35 targets.
- Existing chain scoring uses `bigRandomNumber`, staking, transactions, and linear `hashCount`; none are true cumulative work.
- `DataShortBlockchainInformation` currently stores `hashCount` as `long`; true cumulativeWork may exceed `long`.
- Adding fields to `/datashort` may affect old nodes; a versioned V35 chain-score endpoint or nullable fields may be safer.
- Emergency difficulty depends on timestamps, so timestamp validation must be reviewed before activation.
- Wallet/server file hashes differ for core consensus files, so patches must be reviewed semantically in both trees.

## Rollback Plan

If Phase 3B introduces a regression:

1. Revert Phase 3B edits only in V35 helper classes, tests, and any fork-gated call sites.
2. Keep Phase 1A constants, Phase 1B skeletons, Phase 1C tests, Phase 2 reward wiring, and Phase 2C fixture tests unless directly implicated.
3. Confirm `V35_RESCUE_FORK_INDEX = Long.MAX_VALUE` in both wallet and server.
4. Rerun:
   - `cd C:\citu\unitedStates_final-master; .\mvnw.cmd test`
   - `cd C:\citu\unitedStates_storage-master; .\mvnw.cmd test`
5. Reattempt Phase 3B in smaller substeps:
   - pure target helpers only;
   - mining predicate only;
   - validation only;
   - cumulativeWork metadata only;
   - tournament selection only.

## Phase 3B Recommendation

Do not start with mining. Start with pure helpers and tests:

1. compact target encode/decode;
2. hash-under-target;
3. target adjustment and emergency drop;
4. work calculation;
5. only then wire V35 mining and validation;
6. update cumulativeWork and tournament after block-level V35 PoW is tested.
