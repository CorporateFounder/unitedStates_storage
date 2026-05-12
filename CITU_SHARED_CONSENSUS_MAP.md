# CITU Shared Consensus Map

Phase 0 audit only. No source code changes were made.

## Highest-Risk Shared Classes
These files exist in both wallet and server and must remain synchronized for V35 consensus.

### Settings and Fork Constants
- Wallet: `C:\citu\unitedStates_final-master\src\main\java\International_Trade_Union\setings\Seting.java`
- Server: `C:\citu\unitedStates_storage-master\src\main\java\International_Trade_Union\setings\Seting.java`
- Critical constants: `ADDRESS_FOUNDER`, `BASIS_ADDRESS`, `BASIS_PASSWORD`, `FOUNDERS_REWARD`, `DIGITAL_DOLLAR_REWARDS_BEFORE`, `DIGITAL_STOCK_REWARDS_BEFORE`, `DIGITAL_DOLLAR_FOUNDER_REWARDS_BEFORE`, `DIGITAL_REPUTATION_FOUNDER_REWARDS_BEFORE`, `BLOCK_TIME`, `COUNT_BLOCK_IN_DAY`, `BLOCK_GENERATION_INTERVAL`, `DIFFICULTY_ADJUSTMENT_INTERVAL`, `CHECK_UPDATING_VERSION`, `NEW_START_DIFFICULT`, `CHANGE_MEET_DIFFICULTY`, `SPECIAL_BLOCK_FORK`, `ORIGINAL_HASH`, `V28_CHANGE_ALGORITH_DIFF_INDEX`, `V29_CHANGE_ALGO_DIFF_INDEX`, `V30_INDEX_ALGO`, `V30_INDEX_DIFF`, `V30_1_FIXED_DIFF`, `V31_DIFF_END_MINING`, `V31_FIX_DIFF`, `V32_FIX_DIFF`, `V34_NEW_ALGO`, `V34_MINING_REWARD`, `V34_MIN_DIFF`, `ALGORITM_MINING`, `ALGORITM_MINING_2`, `BALANCE_CHEKING`, `OPTIMAL_SCORE_INDEX`, `CHANGE_DECIMAL_2_INDEX`.
- V35 needs new constants for `V35_INDEX`, fixed 10 GOLD reward, 8 GOLD instant reward, 2 GOLD jackpot reserve, burn address, cumulative-work policy, emergency drop thresholds, jackpot periods, and display conversion `1 raw digitalStockBalance = 100 Silver`.

### Block Model and Hash Format
- Wallet/server: `...\entity\blockchain\block\Block.java`
- Critical methods: `hashForTransaction()`, `hashForBlockchain()`, `verifyesTransSign()`, `multipleFindHash(long)`, `chooseMultiString(long, boolean)`, `findHash(long)`, `findHash_MT2(long)`, `findHash_org(long)`, `jsonString()`, inner `BlockForHash.jsonStringWithoutProof()`.
- Critical fields: `dtoTransactions`, `previousHash`, `minerAddress`, `founderAddress`, `randomNumberProof`, `minerRewards`, `hashCompexity`, `timestamp`, `index`, `hashBlock`.
- V35 risk: do not change serialized fields, hash serialization, old proof search, `randomNumberProof`, or pre-V35 validation. New target-based PoW must be gated after V35.

### Transaction Model and Signature Format
- Wallet/server: `...\entity\DtoTransaction\DtoTransaction.java`
- Critical fields: `sender`, `customer`, `digitalDollar`, `digitalStockBalance`, `laws`, `bonusForMiner`, `voteEnum`, `sign`.
- Critical methods: `verify()`, `toSign()`, `jsonString()`, `equals()`, `hashCode()`.
- Signature preimage: `sender + customer + digitalDollar + digitalStockBalance + laws + bonusForMiner`, then `UtilsUse.sha256hash(...)`.
- V35 rule: do not change transaction fields or `toSign()`; burn support must be policy validation, not format mutation.

### Mining and Reward Creation
- Wallet/server: `...\model\Mining.java`
- Critical methods: `getBlockchain(String, BlockchainFactoryEnum)`, `getCustomDiff()`, `setCustomDiff(int)`, `miningDay(...)`.
- Current behavior: after `V34_NEW_ALGO`, `difficulty = Mining.customDiff`; manual difficulty directly affects block production.
- Founder reward creation: `founderRew = new DtoTransaction(BASIS_ADDRESS, addressFounrder, founderReward, founderDigigtalReputationReward, ...)`.
- Miner reward creation: `minerRew = new DtoTransaction(BASIS_ADDRESS, minner.getAccount(), minerRewards, digitalReputationForMiner, ..., sumRewards, ...)`.
- V35 needs: remove founder reward after V35, create exactly 8 GOLD miner reward plus 2 GOLD jackpot reserve, and remove manual `customDiff` from post-V35 consensus.

### Difficulty and PoW Validation
- Wallet/server: `...\utils\UtilsBlock.java`
  - Critical methods: `difficulty(List<Block>, long, int)`, `validationOneBlock(...)`, `validation(...)`.
- Wallet/server: `...\utils\UtilsUse.java`
  - Critical methods: `chooseComplexity(String, long, long, String, BigInteger)`, `hashComplexity(String, long)`, `hashCount(String, long)`, `powerDiff(long)`, `blocksReward(...)`, `balanceTransaction(...)`, `isTransactionValid(...)`, `checkSendBalance(...)`, `calculateMinedMoneyFridman(...)`, `bigRandomWinner(...)`.
- Wallet/server: `...\utils\UtilsDIfficult.java`
  - Critical methods: `getAdjustedDifficulty(...)`, `v28_changeAlgorith_diff(...)`, `v30_1_changeAlgorith_diff(...)`, `v30_changeAlgorith_diff(...)`, `v2getAdjustedDifficultyMedian(...)`, `getAdjustedDifficultyMedian(...)`, `adjustDifficulty03112(...)`.
- Wallet/server: `...\utils\BlockchainDifficulty.java`
  - Critical methods: `meetsDifficulty(...)`, `v2MeetsDifficulty(...)`, `v3MeetsDifficulty(...)`, `v4MeetsDifficulty(...)`, `calculateTarget(...)`, `isValidHash(...)`, `getBitSum(...)`, `getBitSum2(...)`, `isValidHashV29(...)`, `isValidBlockV29(...)`, `calculateTargetV30(...)`, `isValidHashV30(...)`.
- V35 target rule belongs here: `hashNumber <= target`, with cumulative work derived from target/difficulty.

### Block Validation and Balance Calculation
- Wallet/server: `...\utils\UtilsBlock.java`
- `validationOneBlock(...)` checks previous hash, duplicate transaction signatures, balances, transaction decimal policy, transaction signatures, reward validation, difficulty and hash rules.
- `validation(...)` iterates chain from old blocks and calls `validationOneBlock(...)`.
- Wallet/server: `...\utils\UtilsBalance.java`, `...\utils\UtilAccounts.java`, `...\utils\SaveBalances.java`, `...\utils\UtilsAccountToEntityAccount.java`.
- V35 needs strict pre/post fork branching: old blocks must validate under old rules; V35+ blocks must reject founder reward and enforce fixed total 10 GOLD.

### Blockchain State and Metadata
- Wallet/server: `...\entity\blockchain\Blockchain.java`
- Critical methods: `genesisBlock()`, `shortCheck(...)`, `checkFromFile(...)`, `rollBackShortCheck(...)`, `indexFromFile(...)`, `indexFromFileBing(...)`, `subFromFile(...)`, `subFromFileBing(...)`, `hashFromFile(...)`, `compareLists(...)`, `deleteFileBlockchain(...)`.
- `shortCheck(...)` currently accumulates `hashCount += UtilsUse.powerDiff(block.getHashCompexity())`, `staking += stakingForDataShort(miner staking)`, `bigRandomNumber += UtilsUse.bigRandomWinner(block, miner)`, and transaction count.
- V35 needs `cumulativeWork` added and made primary for chain selection.

### Transaction Pools and Accounts
- Wallet: `...\network\AllTransactions.java` uses static transaction lists and file persistence.
- Server: `...\network\AllTransactions.java` is a Spring component with `addTransaction(...)`, `addAllTransactions(...)`, `validateAndFilterTransactions(...)`, `balanceTransaction(...)`.
- Server-only pool/service: `...\entity\services\PoolBlockService.java`, `...\entity\services\RewardDistributionService.java`, `...\controllers\PoolBlockController.java`.
- Wallet/server: `...\model\Account.java`; critical fields are `account`, `digitalDollarBalance`, `digitalStockBalance`, `digitalStakingBalance`; critical method `send(...)`.
- V35 branding: `digitalDollarBalance` displays as CITU Gold; `digitalStockBalance` displays as CITU Silver at `raw * 100` in UI only. Consensus balances remain raw.

## Server-Only Consensus/Sync Files
- `C:\citu\unitedStates_storage-master\src\main\java\International_Trade_Union\model\Tournament.java`
- `...\model\TournamentService.java`
- `...\model\NodeChecker.java`
- `...\model\HostEndDataShortB.java`
- `...\model\comparator\HostEndDataShortBComparator.java`
- `...\entity\blockchain\DataShortBlockchainInformation.java`
- `...\utils\UtilsResolving.java`
- `...\controllers\BasisController.java`

## Audit Red Flags
- `HostEndDataShortBComparator.compare(...)` contains `if(d1.getSize() != d1.getSize())`, which is always false; size is not actually compared.
- `TournamentService.sortWinner(...)` prioritizes `bigRandomWinner`, then `hashCompexity`, then staking, then transaction count. This is not cumulative PoW.
- `UtilsUse.powerDiff(long diff)` currently returns `diff`, not exponential or target-derived work.
- Manual mining difficulty after V34 is exposed through `Mining.customDiff` and wallet route `/customDiff`.
- Founder reward creation is unconditional in `Mining.miningDay(...)`; V35 must gate it off after the fork index.
