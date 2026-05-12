# CITU V35 Phase 2A Reward Wiring Plan

Date: 2026-05-12

Scope: planning only. No Java source, consensus behavior, mining behavior, validation behavior, UI, website, transaction format, signature format, or hash format was changed.

## Goal For Phase 2B

Wire V35 founder reward removal and fixed block reward safely after `Seting.V35_RESCUE_FORK_INDEX`.

Post-V35 intended reward behavior:

- Founder reward is zero.
- Total block GOLD emission is exactly `10.00`.
- Instant miner GOLD reward is exactly `8.00`.
- Jackpot reserve GOLD is exactly `2.00`.
- Difficulty, activity, Friedman growth, and multiplier/reduction logic must not add extra GOLD.
- Blocks before V35 continue to validate through existing legacy reward rules.

## Exact Files And Methods Found

### Wallet Reward Creation

Project: `C:\citu\unitedStates_final-master`

- `src\main\java\International_Trade_Union\model\Mining.java`
  - `miningDay(...)`, line 117
  - Difficulty selection: lines 236-239
  - Legacy miner/founder base values: lines 244-251
  - Difficulty-derived founder reward: lines 252-261
  - Difficulty-derived miner reward: lines 262-267
  - V28 multiplier/activity reward branch: lines 269-280
  - V34 reward branch with multiplier, activity, difficulty bonus, and Friedman growth: lines 281-355
  - Founder transaction creation: lines 363-375
  - Special fork miner override: lines 385-388
  - Miner transaction creation: lines 390-399
  - Block construction: line 412

### Server Reward Creation

Project: `C:\citu\unitedStates_storage-master`

- `src\main\java\International_Trade_Union\model\Mining.java`
  - `miningDay(...)`, line 85
  - Difficulty selection: line 201 and nearby branch
  - Legacy miner/founder reward formulas: lines 216-319
  - Founder transaction creation: lines 327-339
  - Special fork miner override: lines 349-352
  - Miner transaction creation: lines 354-363
  - Block construction: line 376

### Wallet And Server Reward Validation

Both projects have matching validation logic in:

- Wallet: `C:\citu\unitedStates_final-master\src\main\java\International_Trade_Union\utils\UtilsBlock.java`
- Server: `C:\citu\unitedStates_storage-master\src\main\java\International_Trade_Union\utils\UtilsBlock.java`

Methods:

- `difficulty(List<Block>, long, int)`, line 260
- `validationOneBlock(...)`, wallet line 366, server line 366
- `validation(...)`, wallet line 1072, server line 1071

Critical reward validation ranges:

- Transaction loop and BASIS reward validation begins around line 562.
- Per-BASIS transaction reward recomputation starts at line 573.
- Legacy difficulty-derived miner reward: lines 573-581.
- V28 multiplier/activity reward branch: lines 582-592.
- V34 multiplier/activity/difficulty/Friedman branch: lines 597-713.
- Miner reward cap validation:
  - GOLD: lines 716-729.
  - Stock/Silver raw balance: lines 730-744.
- Founder reward validation:
  - BASIS to founder detection/counting: lines 747-749.
  - CHECK_UPDATING_VERSION to V28 branch: lines 750-787.
  - Post-V28 expected founder reward from `minerReward / Seting.DOLLAR` and `minerPowerReward / Seting.STOCK`: lines 789-858.
- BASIS transaction count limits:
  - Founder BASIS sends over limit: lines 871-880.
  - Non-founder BASIS sends over limit: lines 882-890.
- Hash/difficulty validation still occurs later:
  - `UtilsUse.chooseComplexity(...)`: lines 941-953.
  - Expected difficulty check for older forks: lines 955-967.

### Wallet And Server Balance/Emission Guards

Both projects have matching balance guard logic in:

- Wallet: `C:\citu\unitedStates_final-master\src\main\java\International_Trade_Union\utils\UtilsBalance.java`
- Server: `C:\citu\unitedStates_storage-master\src\main\java\International_Trade_Union\utils\UtilsBalance.java`

Methods:

- `rollbackCalculateBalance(...)`, wallet line 46, server line 45
- `calculateBalance(...)`, wallet line 164, server line 163
- `calculateBalances(...)`, wallet line 345, server line 344
- `sendMoney(...)`, wallet line 374, server line 373
- `rollBackSendMoney(...)`, wallet line 453, server line 452

Critical reward/basis ranges:

- BASIS send count and reward cap in rollback flow: wallet lines 68-119, server lines 67-118.
- BASIS send count and reward cap in forward balance flow: wallet lines 235-290, server lines 234-289.
- Existing balance code assumes BASIS can send only to founder or miner and at most two times per block.

### Activity Bonus

Both projects:

- `src\main\java\International_Trade_Union\utils\UtilsUse.java`
  - `blocksReward(List<DtoTransaction> actual, List<DtoTransaction> prev, long index)`, line 95
  - It filters out BASIS transactions, counts unique senders, sums `digitalDollar`, and returns `Seting.COEFFICIENT` when current block activity is higher than previous block activity.

Call sites:

- Wallet `Mining.miningDay(...)`: lines 275 and 298.
- Server `Mining.miningDay(...)`: lines 239 and 262.
- Wallet/server `UtilsBlock.validationOneBlock(...)`: lines 588 and 614.

V35 rule: this value must not increase GOLD emission after V35. It may later affect ticket weight only, but not Phase 2B unless explicitly scoped.

### Difficulty Bonus

Reward-related difficulty bonuses exist in:

- Wallet `Mining.miningDay(...)`: lines 252-267, 300-310.
- Server `Mining.miningDay(...)`: lines 216-230, 264-274.
- Wallet/server `UtilsBlock.validationOneBlock(...)`: lines 576-581, 616-627.
- Wallet/server `UtilsUse.calculateMinedMoneyFridman(...)`: lines 835-878 wallet, 846-889 server, where `diffMoney` contributes to Friedman-derived emission.

V35 rule: post-V35 difficulty must not print extra GOLD.

### Multiplier / Reward Reduction

Multiplier and 4-month or period reduction are in:

- Wallet `Mining.miningDay(...)`: lines 270-272, 283-295, 313-325.
- Server `Mining.miningDay(...)`: lines 234-236, 247-259, 277-289.
- Wallet/server `UtilsBlock.validationOneBlock(...)`: lines 583-586, 599-611, 636-650, 670-713.
- Constants:
  - Wallet `Seting.MULTIPLIER`, line 268.
  - Wallet `Seting.MULTIPLIER2`, line 269.
  - Server `Seting.MULTIPLIER`, line 266.
  - Server `Seting.MULTIPLIER2`, line 267.

V35 rule: multiplier/reduction must not affect post-V35 fixed 10 GOLD emission.

### Friedman Growth

Both projects:

- Wallet `UtilsUse.calculateMinedMoneyFridman(...)`: lines 835-878.
- Server `UtilsUse.calculateMinedMoneyFridman(...)`: lines 846-889.

Call sites:

- Wallet `Mining.miningDay(...)`: lines 320-321.
- Server `Mining.miningDay(...)`: lines 284-285.
- Wallet/server `UtilsBlock.validationOneBlock(...)`: lines 643-644 and 706-707.

V35 rule: Friedman growth must not affect post-V35 GOLD emission.

### Index / Fork Checks Related To Reward Logic

Wallet `Seting.java`:

- `CHECK_UPDATING_VERSION = 24281`, line 231.
- `SPECIAL_BLOCK_FORK = 24281`, line 239.
- `V28_CHANGE_ALGORITH_DIFF_INDEX = 133750`, line 264.
- `V34_NEW_ALGO = 187200`, line 283.
- `START_BLOCK_DECIMAL_PLACES = 268765`, line 317.
- `NEW_ALGO_MINING = 286892`, line 318.
- `ALGORITM_MINING = 295316`, line 333.
- `ALGORITM_MINING_2 = 296583`, line 336.
- `CHANGE_DECIMAL_2_INDEX = 321739`, line 328.
- `MONEY_MILTON_FRIDMAN_INDEX = 326840`, line 348.
- `OPTIMAL_SCORE_INDEX = 342201`, line 355.
- `V35_RESCUE_FORK_INDEX = Long.MAX_VALUE`, line 362.

Server `Seting.java`:

- `CHECK_UPDATING_VERSION = 24281`, line 240.
- `SPECIAL_BLOCK_FORK = 24281`, line 249.
- `V28_CHANGE_ALGORITH_DIFF_INDEX = 133750`, line 262.
- `V34_NEW_ALGO = 187200`, line 282.
- `START_BLOCK_DECIMAL_PLACES = 268765`, line 312.
- `NEW_ALGO_MINING = 286892`, line 314.
- `ALGORITM_MINING = 295316`, line 329.
- `ALGORITM_MINING_2 = 296583`, line 331.
- `CHANGE_DECIMAL_2_INDEX = 321739`, line 324.
- `MONEY_MILTON_FRIDMAN_INDEX = 326840`, line 338.
- `OPTIMAL_SCORE_INDEX = 342201`, line 344.
- `V35_RESCUE_FORK_INDEX = Long.MAX_VALUE`, line 348.

`UtilsBlock.validationOneBlock(...)` also has local `FIX_FRIDMAN_ROUNDING_INDEX = 482360` at line 378 in both projects.

## Current Legacy Reward Flow

1. `Mining.miningDay(...)` filters pending transactions and calculates block difficulty.
2. After V34, mining overrides calculated difficulty with `Mining.customDiff`.
3. Miner and founder rewards start from old constants.
4. Depending on block index, reward formulas use:
   - hash complexity/difficulty,
   - `Seting.MONEY`,
   - parity bonus `index % 2`,
   - activity coefficient from `UtilsUse.blocksReward(...)`,
   - multiplier/reduction via `MULTIPLIER` or `MULTIPLIER2`,
   - difficulty bonus through `moneyFromDif`,
   - Friedman growth through `UtilsUse.calculateMinedMoneyFridman(...)`.
5. Founder reward is set as a fraction of miner reward in later branches: `minerRewards / Seting.DOLLAR`, stock side `digitalReputationForMiner / Seting.STOCK`.
6. Founder reward transaction is signed by BASIS and added to the block.
7. Miner reward transaction is signed by BASIS and added to the block.
8. Transactions are deduplicated, sorted by signature, and the block is constructed.

## Current Founder Reward Flow

Creation:

- Wallet `Mining.miningDay(...)`, lines 363-375.
- Server `Mining.miningDay(...)`, lines 327-339.

The founder address is loaded from genesis block zero with `Blockchain.indexFromFile(0, Seting.ORIGINAL_BLOCKCHAIN_FILE).getFounderAddress()`. Mining rejects if this address does not equal the previous block founder address. Then it creates:

```java
new DtoTransaction(Seting.BASIS_ADDRESS, addressFounrder, founderReward, founderDigigtalReputationReward, new Laws(), 0.0, VoteEnum.YES)
```

Validation:

- Wallet/server `UtilsBlock.validationOneBlock(...)`, lines 747-858.

The validator detects `BASIS_ADDRESS -> addressFounder`, counts founder BASIS sends, and checks amount against legacy formulas. For post-V28 blocks it expects founder GOLD as `minerReward / Seting.DOLLAR` and stock as `minerPowerReward / Seting.STOCK`, with decimal rounding after later forks.

## Current Miner Reward Flow

Creation:

- Wallet `Mining.miningDay(...)`, lines 390-399.
- Server `Mining.miningDay(...)`, lines 354-363.

The miner transaction is:

```java
new DtoTransaction(Seting.BASIS_ADDRESS, minner.getAccount(), minerRewards, digitalReputationForMiner, new Laws(), sumRewards, VoteEnum.YES)
```

`sumRewards` is the sum of user transaction `bonusForMiner` values. The miner reward transaction uses that sum as its own `bonusForMiner`.

Validation:

- Wallet/server `UtilsBlock.validationOneBlock(...)`, lines 716-744.

The validator recalculates `minerReward` and `minerPowerReward` from legacy formulas and rejects a BASIS-to-miner transaction only when actual GOLD or stock is greater than the calculated maximum. This is an upper-bound check, not an exact equality check.

Balance constraints:

- Wallet `UtilsBalance.rollbackCalculateBalance(...)`, lines 68-119.
- Wallet `UtilsBalance.calculateBalance(...)`, lines 235-290.
- Server same logic one line earlier.

Balance code also applies upper-bound checks for BASIS transactions and restricts BASIS recipients to founder or miner.

## Current Validation Flow

1. `UtilsBlock.validation(...)` iterates blocks and calls `validationOneBlock(...)`.
2. `validationOneBlock(...)` checks timestamp, transaction duplication, transaction format, transaction signatures, balances, BASIS reward transactions, hash difficulty, expected difficulty for old forks, block hash, and previous hash.
3. BASIS reward validation is embedded inside the transaction loop, not isolated in a helper.
4. For every BASIS transaction, the validator recomputes legacy miner/founder expected values using formulas duplicated from `Mining.miningDay(...)`.
5. Founder reward is validated by recognizing customer `addressFounder`.
6. Miner reward is validated by recognizing customer `thisBlock.getMinerAddress()`.
7. BASIS send count limits enforce more than two founder sends and more than one non-founder send as invalid for normal blocks.

## Wallet / Server Differences

- Reward logic is functionally parallel but line offsets differ.
- Wallet `Mining.miningDay(...)` starts at line 117; server starts at line 85.
- Wallet `UtilsUse.calculateMinedMoneyFridman(...)` starts at line 835; server starts at line 846 because server has extra helper content.
- Wallet `Seting.java` and server `Seting.java` differ in layout and some legacy path constants, but V35 constants match by name/type/value from Phase 1A.
- Phase 1C tests were added to both projects with identical V35 test files and passing test counts.
- No meaningful reward formula difference was found in the inspected mining and validation paths. The main risk is duplicated code, not known intentional divergence.

## Exact Methods Phase 2B Must Change

Phase 2B should change the same methods in wallet and server, in lockstep:

- `International_Trade_Union.model.Mining.miningDay(...)`
  - Add a fork-gated V35 reward branch after difficulty selection and before legacy founder/miner reward transaction creation.
  - For `ProtocolVersion.isV35(index)`, skip founder reward transaction creation.
  - Create miner instant reward of exactly `V35_INSTANT_MINER_REWARD`.
  - Add jackpot reserve handling of exactly `V35_JACKPOT_REWARD` according to the final reserve address/transaction design.

- `International_Trade_Union.utils.UtilsBlock.validationOneBlock(...)`
  - Add a fork-gated V35 validation branch before legacy founder and miner reward checks.
  - Reject any `BASIS_ADDRESS -> addressFounder` founder reward after V35.
  - Enforce exact post-V35 issuance: 8.00 miner + 2.00 jackpot reserve = 10.00 total.
  - Ensure difficulty/activity/Friedman/multiplier formulas are bypassed for V35 reward validation.
  - Preserve all legacy branches for `index < V35_RESCUE_FORK_INDEX`.

- `International_Trade_Union.utils.UtilsBalance.rollbackCalculateBalance(...)`
  - Adjust BASIS recipient/count assumptions after V35 if jackpot reserve is represented as a BASIS transaction.

- `International_Trade_Union.utils.UtilsBalance.calculateBalance(...)`
  - Same as rollback path: allow the V35 jackpot reserve recipient if implemented as a transaction, and enforce V35 BASIS send limits.

- `International_Trade_Union.protocol.v35.V35RewardCalculator`
  - May be extended with pure helper methods for expected miner/jackpot/founder values, but only if tests are added first.

- `International_Trade_Union.protocol.v35.FounderRewardPolicy`
  - May be used by mining/validation to centralize the V35 founder-disabled decision.

Potentially avoid editing `UtilsUse.blocksReward(...)` and `UtilsUse.calculateMinedMoneyFridman(...)` in Phase 2B. Prefer bypassing their call sites after V35 so legacy behavior remains untouched.

## Files Phase 2B May Edit

Only if tests are in place first:

- `C:\citu\unitedStates_final-master\src\main\java\International_Trade_Union\model\Mining.java`
- `C:\citu\unitedStates_storage-master\src\main\java\International_Trade_Union\model\Mining.java`
- `C:\citu\unitedStates_final-master\src\main\java\International_Trade_Union\utils\UtilsBlock.java`
- `C:\citu\unitedStates_storage-master\src\main\java\International_Trade_Union\utils\UtilsBlock.java`
- `C:\citu\unitedStates_final-master\src\main\java\International_Trade_Union\utils\UtilsBalance.java`
- `C:\citu\unitedStates_storage-master\src\main\java\International_Trade_Union\utils\UtilsBalance.java`
- `C:\citu\unitedStates_final-master\src\main\java\International_Trade_Union\protocol\v35\V35RewardCalculator.java`
- `C:\citu\unitedStates_storage-master\src\main\java\International_Trade_Union\protocol\v35\V35RewardCalculator.java`
- `C:\citu\unitedStates_final-master\src\main\java\International_Trade_Union\protocol\v35\FounderRewardPolicy.java`
- `C:\citu\unitedStates_storage-master\src\main\java\International_Trade_Union\protocol\v35\FounderRewardPolicy.java`
- New or updated tests under both projects' `src\test\java\International_Trade_Union\protocol\v35\`.

## Files Phase 2B Must Not Edit

Unless a later phase explicitly expands scope:

- `Block.java`
- `DtoTransaction.java`
- `Account.java`
- `Blockchain.java`
- `BlockchainDifficulty.java`
- `UtilsDIfficult.java`
- `UtilsUse.java`, except only if a pure helper extraction is unavoidable and fully tested
- Controllers
- Wallet templates/static files
- Website files
- `pom.xml`
- Chain sync/tournament files
- Transaction serialization/signature/hash code

## Safe Implementation Order For Phase 2B

1. Add failing tests first in both wallet and server for V35 reward validation and mining reward creation.
2. Add pure expected-value helpers if needed in `V35RewardCalculator`, keeping existing Phase 1C tests green.
3. In `Mining.miningDay(...)`, introduce the smallest possible fork branch:
   - `if (ProtocolVersion.isV35(index)) { ... } else { existing legacy flow }`.
   - Keep all pre-V35 formulas byte-for-byte where possible.
4. In the V35 branch, set miner reward values from `Seting.V35_INSTANT_MINER_REWARD`.
5. Do not add founder reward transaction in the V35 branch.
6. Add jackpot reserve transaction or accounting only after choosing the final reserve address/design. If the design is not final, Phase 2B should stop at founder removal + exact miner reward tests and defer jackpot transaction wiring.
7. In `UtilsBlock.validationOneBlock(...)`, add a V35 reward validation branch before legacy founder/miner checks.
8. V35 validation should:
   - count BASIS reward transactions,
   - reject any BASIS transaction to founder,
   - require exact miner reward amount,
   - require exact jackpot reserve amount if represented as transaction,
   - require exact total GOLD issuance of 10.00,
   - ignore activity/difficulty/Friedman/multiplier emission formulas.
9. Update `UtilsBalance` only if the chosen jackpot reserve design requires allowing a non-founder, non-miner BASIS recipient after V35.
10. Run wallet and server tests/builds.
11. Compare wallet/server changed files for parity.

## Required Tests Before Phase 2B

Add these before implementation:

- Old legacy block fixture validates unchanged before V35.
- Legacy reward formula fixture still accepts founder reward before V35.
- V35 block with founder reward is rejected.
- V35 block without founder reward is accepted when reward split is correct.
- V35 miner reward must equal exactly `8.00`.
- V35 total block GOLD issuance must equal exactly `10.00`.
- V35 jackpot reserve must equal exactly `2.00` if represented as transaction.
- V35 activity coefficient from `UtilsUse.blocksReward(...)` does not increase GOLD emission.
- V35 difficulty and `moneyFromDif` do not increase GOLD emission.
- V35 Friedman growth does not increase GOLD emission.
- V35 multiplier/reduction does not change fixed reward.
- Wallet/server test fixtures are identical.

## Required Tests After Phase 2B

Run:

```powershell
cd C:\citu\unitedStates_final-master
.\mvnw.cmd test
.\mvnw.cmd clean package

cd C:\citu\unitedStates_storage-master
.\mvnw.cmd test
.\mvnw.cmd clean package
```

Also run targeted parity checks:

- Compare wallet/server `Mining.java` V35 branch logic.
- Compare wallet/server `UtilsBlock.java` V35 reward validation branch.
- Compare wallet/server `UtilsBalance.java` V35 BASIS recipient/count handling if edited.
- Confirm `git diff --stat` only includes approved Phase 2B files.

## Risks

- Highest risk: legacy reward formulas are duplicated in mining and validation. A partial change can mine blocks that validation rejects.
- Founder reward validation is embedded in the transaction loop; adding V35 logic in the wrong location may allow founder rewards to pass or may reject old blocks.
- Current miner validation is an upper-bound check, not exact equality. V35 must become exact without changing legacy behavior.
- BASIS send count currently assumes two BASIS sends per block: founder plus miner. V35 with miner plus jackpot reserve may still be two sends, but the "non-founder" count currently allows only one. If jackpot reserve is not the miner, this must be changed in a V35-gated way.
- `UtilsBalance` currently allows BASIS only to founder or miner. A jackpot reserve address will be rejected unless this is V35-gated.
- Activity bonus `G`, difficulty bonus `moneyFromDif`, Friedman growth, and multiplier logic are intertwined. Bypass them for V35 rather than editing their internals.
- `V35_RESCUE_FORK_INDEX` is still `Long.MAX_VALUE`; tests that simulate V35 may need direct boundary blocks at `Long.MAX_VALUE` or a test-only seam.
- Jackpot reserve address/design is not final in Phase 2A. Do not wire a spendable reserve destination casually.

## Rollback Plan

If Phase 2B fails tests or introduces reward mismatch:

1. Revert only the Phase 2B edits in:
   - `Mining.java`
   - `UtilsBlock.java`
   - `UtilsBalance.java`
   - V35 helper classes edited in Phase 2B
   - Phase 2B tests
2. Keep Phase 1A constants, Phase 1B skeleton classes, and Phase 1C tests unless they are directly proven faulty.
3. Rerun:
   - `cd C:\citu\unitedStates_final-master; .\mvnw.cmd test`
   - `cd C:\citu\unitedStates_storage-master; .\mvnw.cmd test`
4. Confirm `V35_RESCUE_FORK_INDEX` remains `Long.MAX_VALUE`.
5. Confirm old reward validation tests pass before attempting a smaller Phase 2B patch.

## Phase 2B Recommendation

Do Phase 2B in two commits or substeps:

1. Reward validation first: add V35 validation tests, then reject founder reward and require exact fixed reward in `UtilsBlock.validationOneBlock(...)`.
2. Mining creation second: update `Mining.miningDay(...)` to produce the exact V35-valid reward transactions.

This order prevents mining from creating a new format before validation rules are locked.
