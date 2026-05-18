# CITU V35 Phase 4D-2C Jackpot Chain-Context Proof Plan

Phase: 4D-2C only  
Scope: design only, no source-code changes  
Project root: `C:\citu`  
V35 status: disabled in wallet and server because `Seting.V35_RESCUE_FORK_INDEX = Long.MAX_VALUE`

## 1. Files and Methods Inspected

### Wallet project

- `C:\citu\unitedStates_final-master\src\main\java\International_Trade_Union\utils\UtilsBlock.java`
  - `validationOneBlock(String addressFounder, Block previusblock, Block thisBlock, List<Block> lastBlock, BlockService blockService, Map<String, Account> balance, List<String> signs, List<String> SignaturesNotTakenIntoAccount)`
  - `validation(...)`
  - V35 BASIS classification branch using `ProtocolVersion.isV35(...)`, `V35RewardCalculator`, and `JackpotCalculator.classifyPayoutAmount(...)`
- `C:\citu\unitedStates_final-master\src\main\java\International_Trade_Union\utils\UtilsBalance.java`
  - `calculateBalances(List<Block> blocks)`
  - `calculateBalance(Map<String, Account> balances, Block block, List<String> sign, List<DtoTransaction> transactions)`
  - `rollbackCalculateBalance(Map<String, Account> balances, Block block)`
  - `sendMoney(...)`
  - `rollBackSendMoney(...)`
- `C:\citu\unitedStates_final-master\src\main\java\International_Trade_Union\entity\blockchain\Blockchain.java`
  - `shortCheck(...)`
  - `rollBackShortCheck(...)`
  - `getBlockchainList()`
  - `getBlock(int index)`
  - `subBlock(int startIndex, int finishIndex)`
  - `clone(...)`
  - `indexFromFile(...)`, `subFromFile(...)`
- `C:\citu\unitedStates_final-master\src\main\java\International_Trade_Union\entity\blockchain\block\Block.java`
  - Fields: `dtoTransactions`, `previousHash`, `minerAddress`, `founderAddress`, `hashCompexity`, `timestamp`, `index`, `hashBlock`
  - Constructors for mined blocks and reconstructed blocks
  - `hashForTransaction()`
  - `hashForBlockchain()`
  - `clone()`
- `C:\citu\unitedStates_final-master\src\main\java\International_Trade_Union\protocol\v35\JackpotCalculator.java`
  - `JackpotPeriod`
  - `JACKPOT_RANDOM_DELAY_BLOCKS`
  - `windowBlocksForPeriod(...)`
  - `periodStart(...)`
  - `periodEnd(...)`
  - `maturityBlock(...)`
  - `isMature(...)`
  - `classifyPayoutAmount(double amount)`
  - `expectedPayoutAmount(JackpotPeriod periodType)`
  - `eligibleTickets(...)`
  - `seedFromFutureHashes(...)`
  - `selectWinner(...)`
  - `isValidJackpotPayout(...)`
- `C:\citu\unitedStates_final-master\src\main\java\International_Trade_Union\protocol\v35\JackpotTicket.java`
  - `of(long blockIndex, String minerAddress)`
  - `getBlockIndex()`
  - `getMinerAddress()`
  - `getWeight()`
  - `isValid()`
- `C:\citu\unitedStates_final-master\src\main\java\International_Trade_Union\protocol\v35\JackpotWinnerProof.java`
  - `periodStart`
  - `periodEnd`
  - `maturityBlock`
  - `randomDelayBlocks`
  - `seedHex`
  - `selectedOffset`
  - `selectedBlockIndex`
  - `winnerMinerAddress`
  - `noWinner(...)`
  - `hasWinner()`
- `C:\citu\unitedStates_final-master\src\main\java\International_Trade_Union\model\Mining.java`
  - `miningDay(...)`
  - `selectV35MiningCompactTarget(List<Block> blockchain)`
  - `mineV35Block(...)`
  - V35 reward branch that creates only the 6.00 GOLD instant miner reward

### Server project

- `C:\citu\unitedStates_storage-master\src\main\java\International_Trade_Union\utils\UtilsBlock.java`
- `C:\citu\unitedStates_storage-master\src\main\java\International_Trade_Union\utils\UtilsBalance.java`
- `C:\citu\unitedStates_storage-master\src\main\java\International_Trade_Union\entity\blockchain\Blockchain.java`
- `C:\citu\unitedStates_storage-master\src\main\java\International_Trade_Union\entity\blockchain\block\Block.java`
- `C:\citu\unitedStates_storage-master\src\main\java\International_Trade_Union\protocol\v35\JackpotCalculator.java`
- `C:\citu\unitedStates_storage-master\src\main\java\International_Trade_Union\protocol\v35\JackpotTicket.java`
- `C:\citu\unitedStates_storage-master\src\main\java\International_Trade_Union\protocol\v35\JackpotWinnerProof.java`
- `C:\citu\unitedStates_storage-master\src\main\java\International_Trade_Union\model\Mining.java`

The wallet and server implementations match closely for the inspected V35 jackpot helper model and the current V35 mining/validation branches.

## 2. What Context UtilsBlock Has

`UtilsBlock.validationOneBlock(...)` has enough context for ordinary one-block validation:

- Founder address: `addressFounder`
- Previous block: `previusblock`
- Candidate/current block: `thisBlock`
- Current block index: `thisBlock.getIndex()`
- Current block transactions: `thisBlock.getDtoTransactions()`
- Current block miner address: `thisBlock.getMinerAddress()`
- Rolling block list: `lastBlock`
- Block persistence/service handle: `blockService`
- Balance map for validation: `balance`
- Signature tracking lists: `signs`, `SignaturesNotTakenIntoAccount`

It does not reliably have enough context for full jackpot proof validation:

- It does not receive a declared full canonical chain range.
- It does not receive a previous-payout index keyed by jackpot period.
- It cannot safely assume `lastBlock` contains the entire period range, the 60 future seed blocks, and all previous payout transactions.
- In `Blockchain.shortCheck(...)`, `validationOneBlock(...)` is called with a rolling `blockList`, not a proof-specific canonical context.
- When validating a block that contains a jackpot payout, the required seed hashes are historical relative to the payout block, but still require an exact range: `periodEnd + 1` through `periodEnd + 60`.

Current safe behavior from Phase 4D-2B is correct: `UtilsBlock` can classify a V35 jackpot payout candidate by canonical amount, reject noncanonical BASIS amounts, reject duplicate jackpot period candidates inside the same block, and reject ambiguous jackpot payout candidates because full chain-context proof is not available.

Conclusion: `UtilsBlock` should not own jackpot proof construction directly. It should call a shared chain-context proof service only when the caller can provide a complete canonical context.

## 3. What Context UtilsBalance Has

`UtilsBalance.calculateBalance(...)` and `UtilsBalance.rollbackCalculateBalance(...)` are per-block accounting methods. They have:

- Current block
- Current block transactions
- Rolling balances
- Signature tracking
- Existing BASIS credit/debit behavior

They do not have enough local context to independently prove jackpot payout validity:

- No period boundary context
- No seed hash range
- No full duplicate scan context
- No proof that recipient is deterministic winner

`UtilsBalance.calculateBalances(List<Block> blocks)` can scan a full canonical list, so it is the best existing balance entry point for chain-level jackpot accounting. The per-block balance methods should not decide jackpot validity alone. They should either:

1. Receive a prevalidated set of jackpot payout keys/results from a chain-level verifier, or
2. Be called only after `UtilsBlock` or a chain-validation layer has already accepted jackpot payouts.

Important current blocker: the existing per-block balance code still treats BASIS payouts as valid only when recipient is founder or current miner in some paths. Jackpot winners may be a historical miner whose address is not the current payout block miner. Phase 4D-2D or later must decide whether `UtilsBalance` should credit prevalidated jackpot payouts by proof key instead of applying the old founder/current-miner-only BASIS shortcut.

Conclusion: `UtilsBalance` has enough context only at the full-chain `calculateBalances(List<Block>)` layer, not inside one-block accounting.

## 4. What Context Mining Has

`Mining.miningDay(...)` receives:

- Current miner account
- Current canonical blockchain list: `List<Block> blockchain`
- Candidate block index: `index`
- Candidate transactions
- Balances
- Previous block
- V35 target selection helpers

In the V35 branch, mining currently creates only:

- `BASIS_ADDRESS -> minerAddress`
- `6.00 GOLD`
- `0.00 stock/silver`

Mining does not create jackpot payouts yet.

Mining can know matured jackpot periods if the payout block is after the seed range:

- Eligible blocks for period: from canonical prior blocks.
- Future seed hashes for that period: already known only after blocks `periodEnd + 1` through `periodEnd + 60` exist.
- Payout already exists: scan canonical prior blocks.
- Winner miner address: compute from eligible tickets and seed hashes.
- Payout amount: classify by period and expected full-window amount.

Mining cannot know the seed for a period that just ended. It must only create payouts for periods whose delayed randomness has fully matured before the block being mined.

Recommended payout timing for first implementation:

- Seed range: `periodEnd + 1` through `periodEnd + 60`
- Maturity block: `periodEnd + 60`
- First safe payout block: `periodEnd + 61`

This avoids circularity: the payout block hash is never part of the seed used to decide its own payout.

## 5. Recommended Shared Proof Helper/Service Design

Create the same new protocol helper in wallet and server in a future phase:

- Package: `International_Trade_Union.protocol.v35`
- Class: `V35JackpotProofService`

Alternative name: `JackpotChainProofVerifier`. Preferred name: `V35JackpotProofService`, because it will be used by validation, balance accounting, and mining preparation.

The service should be pure or near-pure:

- No static mutable state
- No network calls
- No direct file reads
- No balance mutation
- No transaction creation in the verifier
- Deterministic result for the same chain and payout candidate

Recommended support value objects:

- `JackpotPayoutCandidate`
  - `long payoutBlockIndex`
  - `String sender`
  - `String recipient`
  - `double digitalDollar`
  - `double digitalStockBalance`
  - `double bonusForMiner`
  - `JackpotCalculator.JackpotPeriod periodType`
- `JackpotPeriodInstance`
  - `JackpotPeriod periodType`
  - `long periodStart`
  - `long periodEnd`
  - `long maturityBlock`
  - `long firstPayoutBlock`
  - `String duplicateKey`
- `JackpotPayoutProofResult`
  - `boolean valid`
  - `String reason`
  - `JackpotWinnerProof winnerProof`
  - `JackpotPeriodInstance periodInstance`
  - `BigDecimal expectedAmount`
  - `String expectedWinnerAddress`
  - `String actualRecipient`

Recommended service methods:

- `JackpotPayoutProofResult verifyPayout(List<Block> canonicalBlocks, Block payoutBlock, DtoTransaction payoutTransaction, String founderAddress)`
- `List<JackpotTicket> ticketsForPeriod(List<Block> canonicalBlocks, long periodStart, long periodEnd)`
- `List<String> futureHashesForSeed(List<Block> canonicalBlocks, long periodEnd, int delayBlocks)`
- `boolean hasPreviousPayout(List<Block> canonicalBlocks, JackpotPeriodInstance periodInstance, long payoutBlockIndex)`
- `boolean hasDuplicatePayoutInBlock(Block payoutBlock, JackpotPeriodInstance periodInstance, DtoTransaction payoutTransaction)`
- `List<JackpotPeriodInstance> maturedUnpaidPeriods(List<Block> canonicalBlocks, long candidateBlockIndex)`

The helper should internally use existing pure helpers:

- `ProtocolVersion.isV35(long index)`
- `JackpotCalculator.classifyPayoutAmount(double amount)`
- `JackpotCalculator.expectedPayoutAmount(JackpotPeriod periodType)`
- `JackpotCalculator.seedFromFutureHashes(List<String> futureHashes)`
- `JackpotCalculator.selectWinner(...)`
- `JackpotTicket.of(...)`

## 6. Required Inputs for Proof Verification

Full proof verification requires:

- Canonical block list, ordered by index.
- Payout block.
- Payout transaction.
- V35 fork index.
- Founder address.
- Current payout block index.
- Payout recipient.
- Payout amount.
- Inferred jackpot period type from canonical amount.
- Period window size for the inferred period.
- Period start and period end.
- Seed hash blocks:
  - `periodEnd + 1`
  - through `periodEnd + 60`
- Eligible mined blocks:
  - all V35 blocks from `periodStart` through `periodEnd`
  - each ticket uses `block.getMinerAddress()`
- Previous payout scan range:
  - V35 fork index through `payoutBlockIndex - 1`
- Current block duplicate scan:
  - all BASIS payout candidates in the payout block

Validation should reject when any required range is missing, unsorted, duplicated, or inconsistent.

## 7. Period Instance and Transaction Format

Transaction format can remain unchanged only if the period instance is inferred deterministically.

Amount-based classification identifies the period type:

- `302.40` => daily
- `1209.60` => weekly
- `3888.00` => monthly
- `9720.00` => quarterly
- `15724.80` => half-year
- `23652.00` => yearly

But amount alone does not identify which daily/weekly/monthly period is being paid if late payouts are allowed. A daily payout of `302.40` in a later block could correspond to several unpaid daily periods.

Lowest-risk no-format-change rule:

- A jackpot payout is valid only in the first safe payout block for that exact period:
  - `firstPayoutBlock = periodEnd + JACKPOT_RANDOM_DELAY_BLOCKS + 1`
- The period instance is inferred from:
  - payout block index
  - period type inferred from amount
  - V35 fork index
  - fixed period window

This means a missed payout slot is not paid late in the first implementation. That is strict, easy to audit, and avoids adding period metadata to `DtoTransaction`.

If late payouts must be supported later, transaction format probably needs an explicit period marker or another deterministic assignment rule such as "oldest matured unpaid period of that type." That would be more complex and should not be mixed into Phase 4D-2D.

## 8. Fork-Relative Period Boundaries

Current `JackpotCalculator.periodStart(blockIndex, windowSize)` and `periodEnd(blockIndex, windowSize)` are absolute index helpers.

For V35 consensus, period boundaries should be fork-relative:

- `relativeIndex = blockIndex - Seting.V35_RESCUE_FORK_INDEX`
- `periodNumber = relativeIndex / windowSize`
- `periodStart = Seting.V35_RESCUE_FORK_INDEX + (periodNumber * windowSize)`
- `periodEnd = periodStart + windowSize - 1`

Reason: jackpot periods should not include pre-V35 blocks and should not depend on genesis-era calendar alignment.

Phase 4D-2D should add fork-relative helper methods or keep this logic inside `V35JackpotProofService`. It must not reinterpret legacy blocks as jackpot tickets.

## 9. Validation Rules

The shared proof service should enforce:

1. V35 is active:
   - `Seting.V35_RESCUE_FORK_INDEX != Long.MAX_VALUE`
   - payout block index is V35 or later
2. Payout transaction sender is `Seting.BASIS_ADDRESS`.
3. Payout amount maps to exactly one known `JackpotPeriod`.
4. Payout amount equals the canonical expected amount for that period.
5. `digitalStockBalance == 0.00`.
6. `bonusForMiner == 0.00`.
7. Recipient is not the founder address.
8. Recipient is not blank and not `Seting.BASIS_ADDRESS`.
9. Payout block is exactly `periodEnd + 61` for first implementation.
10. Period is wholly V35.
11. All period blocks exist and are valid V35 candidate ticket blocks.
12. Eligible tickets use `Block.getMinerAddress()` only.
13. Sender/customer addresses from user transactions are never used for jackpot eligibility.
14. Seed hashes are exactly the 60 block hashes after period end.
15. Winner proof is deterministic.
16. Payout recipient equals `JackpotWinnerProof.getWinnerMinerAddress()`.
17. No duplicate payout for the same period type and period range exists in previous canonical blocks.
18. No duplicate payout for the same period type and period range exists in the same payout block.
19. Invalid or missing context rejects the payout.

## 10. Duplicate Payout Prevention Design

Use a deterministic duplicate key:

`V35-JACKPOT:<periodType>:<periodStart>:<periodEnd>`

Since `DtoTransaction` cannot store the key, derive it during validation from payout amount and payout block index.

For previous-chain duplicate scan:

1. Iterate canonical blocks from V35 fork index to `payoutBlockIndex - 1`.
2. Inspect transactions whose sender is `Seting.BASIS_ADDRESS`.
3. Classify amount using `JackpotCalculator.classifyPayoutAmount(...)`.
4. For each canonical jackpot amount, derive the period instance from that historical payout block index.
5. If derived duplicate key equals the candidate duplicate key, reject.

For same-block duplicate scan:

1. Inspect all BASIS transactions in the payout block.
2. Classify each canonical jackpot amount.
3. Derive duplicate key for each.
4. Reject if the same key appears more than once.

This requires exact-slot payout timing. Without exact-slot timing, previous-chain scanning cannot know which period a transaction paid without changing the transaction format.

## 11. How UtilsBlock Should Use the Service Later

Future `UtilsBlock.validationOneBlock(...)` should keep the current V35 BASIS classification branch, but replace the current unconditional rejection of jackpot candidates with:

1. If no complete canonical context is available, reject jackpot payout candidates.
2. If complete canonical context is available, call `V35JackpotProofService.verifyPayout(...)`.
3. Accept the jackpot BASIS transaction only if the result is valid.
4. Keep instant miner reward validation separate:
   - exactly one `6.00 GOLD` miner reward to `thisBlock.getMinerAddress()`
5. Keep founder payout rejection before jackpot validation.

The method signature may need a new overload rather than altering the existing method immediately:

- Keep legacy `validationOneBlock(...)`.
- Add a V35-aware overload that receives `List<Block> canonicalBlocks`.
- Make whole-chain validation paths call the richer overload when available.

This avoids forcing network/sync paths with partial context to accidentally accept jackpot payouts.

## 12. How UtilsBalance Should Use the Service Later

Recommended balance approach:

1. Chain validation produces or can reproduce a set of valid jackpot payout duplicate keys.
2. `UtilsBalance.calculateBalances(List<Block> blocks)` may call `V35JackpotProofService` while scanning the full chain.
3. Per-block `calculateBalance(...)` should not independently validate jackpot proof unless it receives prevalidated context.
4. Once a jackpot payout is validated, `sendMoney(...)` may credit `BASIS_ADDRESS -> winner` even when the winner is not the current block miner.

Rollback must mirror forward accounting:

- If a jackpot payout was valid when applied, rollback must debit the jackpot winner.
- Rollback should not use founder/current-miner-only restrictions for prevalidated jackpot payouts.

Do not let `UtilsBalance` classify jackpot payouts differently than `UtilsBlock`.

## 13. How Mining Should Use the Service Later

Future mining payout creation should:

1. Receive canonical chain list and candidate block index.
2. Ask `V35JackpotProofService.maturedUnpaidPeriods(...)` for periods whose exact payout slot is the candidate block.
3. For each matured unpaid period:
   - compute winner proof
   - create one BASIS transaction to winner for canonical payout amount
   - use zero stock/silver
   - use zero bonus
   - sign with BASIS key
4. Add at most one payout per period type/period range.
5. Continue creating exactly one instant miner reward of `6.00 GOLD`.

Mining must not create payouts for:

- non-mature periods
- periods whose seed range includes the candidate block
- periods with missing seed hashes
- periods already paid
- sender-based "winners"

## 14. Safe Implementation Order for Phase 4D-2D

1. Add `V35JackpotProofService` and value objects in both wallet and server.
2. Add fork-relative period helper logic.
3. Add proof construction from canonical `List<Block>`.
4. Add exact-slot payout validation:
   - first safe payout block is `periodEnd + 61`
5. Add duplicate key derivation and same-block duplicate scan.
6. Add previous-chain duplicate scan.
7. Add unit tests for pure proof service only.
8. Add a `UtilsBlock` integration point that rejects jackpot payouts unless complete canonical context is provided.
9. Add richer whole-chain validation tests that pass a canonical list.
10. Do not update mining payout creation until a later phase.
11. Do not update API/UI/website until validation and accounting are stable.

## 15. Files Phase 4D-2D May Edit

Recommended:

- `C:\citu\unitedStates_final-master\src\main\java\International_Trade_Union\protocol\v35\V35JackpotProofService.java`
- `C:\citu\unitedStates_storage-master\src\main\java\International_Trade_Union\protocol\v35\V35JackpotProofService.java`
- Optional new wallet/server value objects under `International_Trade_Union.protocol.v35`:
  - `JackpotPayoutCandidate.java`
  - `JackpotPeriodInstance.java`
  - `JackpotPayoutProofResult.java`
- Wallet/server V35 jackpot tests under `src\test\java\International_Trade_Union\protocol\v35`
- `UtilsBlock.java` only if Phase 4D-2D explicitly wires the proof service into a context-aware validation overload.
- `CITU_V35_PHASE4D2D_...REPORT.md`

## 16. Files Phase 4D-2D Must Not Edit

Unless a later phase explicitly permits it:

- `Mining.java`
- `Block.java`
- `DtoTransaction.java`
- `Account.java`
- Controllers
- UI/templates
- Website files
- `pom.xml`

`UtilsBalance.java` should remain untouched until a dedicated accounting phase unless the Phase 4D-2D scope explicitly includes balance proof accounting.

## 17. Required Tests

Pure service tests in wallet and server:

- Fork gate:
  - payout before V35 rejected
  - payout while `V35_RESCUE_FORK_INDEX = Long.MAX_VALUE` rejected
- Period inference:
  - fork-relative daily period inferred correctly
  - fork-relative weekly period inferred correctly
  - pre-V35 blocks excluded from tickets
- Exact payout slot:
  - payout at `periodEnd + 60` rejected
  - payout at `periodEnd + 61` accepted if all other proof data matches
  - late payout rejected in first implementation
- Seed range:
  - exactly 60 future hashes required
  - missing hash rejected
  - seed uses `periodEnd + 1` through `periodEnd + 60`
  - payout block hash is not used in its own seed
- Eligibility:
  - only V35 blocks with nonblank `minerAddress` become tickets
  - sender addresses are ignored
  - founder recipient rejected
- Winner proof:
  - correct winner accepted
  - wrong winner rejected
  - different future hashes can produce different deterministic winner
- Amount:
  - canonical amount accepted for matching period
  - wrong amount rejected
  - `6.00` miner reward is not jackpot
- Duplicate prevention:
  - duplicate same period/type in same block rejected
  - previous canonical payout for same period rejected
  - different period types in same block allowed if each proof is valid
- Wallet/server parity:
  - same fixture chain gives same `JackpotWinnerProof`
  - same fixture chain gives same valid/invalid result

Integration tests later:

- `UtilsBlock` accepts a fully valid context-aware jackpot payout after V35.
- `UtilsBlock` rejects jackpot payout without canonical context.
- `UtilsBalance.calculateBalances(List<Block>)` credits only proof-valid jackpot payouts.
- Mining creates payout only at exact payout slot.

## 18. Risks

1. Transaction format ambiguity:
   - Amount identifies period type, not period instance. Exact-slot payout timing is required to keep the format unchanged.
2. Existing `UtilsBlock.validationOneBlock(...)` context is not enough:
   - Accepting payouts from this method without canonical context would be unsafe.
3. Existing `UtilsBalance` BASIS restrictions:
   - Current per-block balance paths are founder/current-miner oriented and may reject or skip historical jackpot winners unless updated carefully.
4. Fork-relative periods:
   - Absolute period helpers must not accidentally include pre-V35 blocks.
5. Payout timing:
   - Payout at `periodEnd + 60` is risky because the block at that index is part of the seed range. Use `periodEnd + 61`.
6. Duplicate scanning:
   - Must derive the same period key everywhere.
7. Wallet/server parity:
   - The proof service must be duplicated exactly or extracted in a shared way later.
8. Full-chain memory/runtime:
   - Long yearly scans are manageable by block count, but full-chain validation must avoid repeated expensive rescans where possible.

## 19. Rollback Plan

If Phase 4D-2D implementation causes problems:

1. Remove the new proof service wiring from `UtilsBlock`.
2. Keep pure helper classes if they are not referenced by consensus paths.
3. Restore the current Phase 4D-2B behavior where jackpot payout candidates are classified but rejected due to missing canonical context.
4. Keep `V35_RESCUE_FORK_INDEX = Long.MAX_VALUE`.
5. Re-run wallet and server tests/builds.

Because V35 is disabled, rollback should not affect live legacy behavior if no legacy paths are changed.

## 20. Recommendation

Proceed to Phase 4D-2D by implementing a shared `V35JackpotProofService` with exact-slot payout validation and fork-relative periods. Do not wire mining payout creation yet. Do not make `UtilsBalance` independently interpret jackpot payouts until proof validation has one canonical implementation.

The transaction format can remain unchanged for the first implementation if payout timing is strict:

- Period type from canonical amount.
- Period instance from payout block index and fixed window.
- Winner from deterministic delayed seed.
- Duplicate key from period type plus period start/end.

