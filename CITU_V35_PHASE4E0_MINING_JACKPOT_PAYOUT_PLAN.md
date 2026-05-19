# CITU V35 Phase 4E-0 Mining Jackpot Payout Creation Plan

Phase: 4E-0  
Project root: `C:\citu`  
Scope: planning only. No source code, tests, transaction format, block format, mining behavior, validation behavior, or balance behavior were changed.

## 1. Exact Files And Methods Inspected

Reports read:

- `CITU_V35_PHASE4A_JACKPOT_DESIGN_PLAN.md`
- `CITU_V35_PHASE4B_JACKPOT_HELPERS_REPORT.md`
- `CITU_V35_PHASE4C_JACKPOT_WINNER_SELECTION_REPORT.md`
- `CITU_V35_PHASE4D_JACKPOT_PAYOUT_VALIDATION_REPORT.md`
- `CITU_V35_PHASE4D1_JACKPOT_PAYOUT_CLASSIFICATION_PLAN.md`
- `CITU_V35_PHASE4D1C_ECONOMICS_432_8GOLD_UPDATE_REPORT.md`
- `CITU_V35_PHASE4D2A_JACKPOT_PAYOUT_CLASSIFICATION_HELPERS_REPORT.md`
- `CITU_V35_PHASE4D2B_UTILSBLOCK_JACKPOT_VALIDATION_REPORT.md`
- `CITU_V35_PHASE4D2C_JACKPOT_CHAIN_CONTEXT_PROOF_PLAN.md`
- `CITU_V35_PHASE4D2D_JACKPOT_PROOF_SERVICE_REPORT.md`
- `CITU_V35_PHASE4D2E_JACKPOT_CHAIN_CONTEXT_ADAPTER_REPORT.md`
- `CITU_V35_PHASE4D2F_UTILSBLOCK_CHAIN_CONTEXT_JACKPOT_VALIDATION_REPORT.md`
- `CITU_V35_PHASE4D2G_UTILSBALANCE_JACKPOT_ACCOUNTING_REPORT.md`
- `CITU_V35_PHASE2B_REWARD_WIRING_REPORT.md`

Wallet:

- `unitedStates_final-master/src/main/java/International_Trade_Union/model/Mining.java`
  - `selectV35MiningCompactTarget(List<Block> blockchain)` lines `79-107`
  - `miningDay(...)` lines `153-488`
  - V35 branch begins at line `279`
  - V35 instant miner reward transaction is created at lines `288-295`
  - V35 instant miner reward is signed at lines `296-297`
  - V35 reward transaction is added at line `298`
  - final V35 de-duplication and signature sorting happens at lines `300-310`
  - V35 target selection and block mining call happen at lines `311-312`
  - legacy founder reward creation happens at lines `441-447`
  - legacy miner reward creation happens at lines `463-471`
  - `mineV35Block(...)` lines `490-514`
- `unitedStates_final-master/src/main/java/International_Trade_Union/utils/UtilsBlock.java`
  - `validateV35JackpotPayoutWithChainContext(...)` lines `373-407`
  - `validationOneBlock(...)`
  - V35 BASIS branch begins at line `628`
  - V35 instant miner reward classification lines `636-644`
  - jackpot payout candidate classification lines `646-675`
  - ordinary one-block validation still rejects jackpot candidates without canonical context at lines `671-674`
  - V35 target PoW validation begins at line `1051`
- `unitedStates_final-master/src/main/java/International_Trade_Union/utils/UtilsBalance.java`
  - `calculateBalances(List<Block> blocks)`
  - `calculateBalances(List<Block> blocks, String founderAddress, long forkIndex)`
  - `applyV35JackpotPayoutsWithChainContext(...)`
  - `isValidV35JackpotPayoutForBalance(...)`
- `unitedStates_final-master/src/main/java/International_Trade_Union/protocol/v35/V35JackpotProofService.java`
  - `verifyPayout(...)`
  - `periodInstanceForPayoutBlock(...)`
  - `markersForPayoutBlock(...)`
  - `ticketsForPeriod(...)`
  - `futureHashesForSeed(...)`
- `unitedStates_final-master/src/main/java/International_Trade_Union/protocol/v35/V35JackpotChainContextAdapter.java`
  - `buildPayoutCandidate(...)`
  - `buildPeriodInstance(...)`
  - `collectEligibleTickets(...)`
  - `collectFutureHashes(...)`
  - `collectPriorPayoutMarkers(...)`
  - `hasPriorPayoutForPeriod(...)`
  - `verifyPayoutWithChainContext(...)`
- `unitedStates_final-master/src/main/java/International_Trade_Union/protocol/v35/JackpotCalculator.java`
  - `JackpotPeriod`
  - `JACKPOT_RANDOM_DELAY_BLOCKS = 60`
  - `windowBlocksForPeriod(...)`
  - `expectedPayoutAmount(...)`
  - `classifyPayoutAmount(...)`
  - `selectWinner(...)`
- `unitedStates_final-master/src/main/java/International_Trade_Union/protocol/v35/V35RewardCalculator.java`
- `unitedStates_final-master/src/main/java/International_Trade_Union/entity/blockchain/block/Block.java`
  - reconstructed/V35 constructor at lines `143-154`
- `unitedStates_final-master/src/main/java/International_Trade_Union/entity/DtoTransaction/DtoTransaction.java`
  - fields and constructor at lines `28-48`

Server:

- `unitedStates_storage-master/src/main/java/International_Trade_Union/model/Mining.java`
  - `selectV35MiningCompactTarget(List<Block> blockchain)` lines `77-105`
  - `miningDay(...)` lines `121-451`
  - V35 branch begins at line `243`
  - V35 instant miner reward transaction is created at lines `252-259`
  - V35 instant miner reward is signed at lines `260-261`
  - V35 reward transaction is added at line `262`
  - final V35 de-duplication and signature sorting happens at lines `264-274`
  - V35 target selection and block mining call happen at lines `275-276`
  - legacy founder reward creation happens at lines `405-411`
  - legacy miner reward creation happens at lines `427-435`
  - `mineV35Block(...)` lines `454-478`
- Matching server copies of `UtilsBlock.java`, `UtilsBalance.java`, `V35JackpotProofService.java`, `V35JackpotChainContextAdapter.java`, `JackpotCalculator.java`, `V35RewardCalculator.java`, `Block.java`, and `DtoTransaction.java`.

Constants checked:

- wallet `Seting.V35_RESCUE_FORK_INDEX = Long.MAX_VALUE`
- server `Seting.V35_RESCUE_FORK_INDEX = Long.MAX_VALUE`
- wallet/server `V35_GOLD_REWARD = 8.00`
- wallet/server `V35_INSTANT_MINER_REWARD = 6.00`
- wallet/server `V35_JACKPOT_REWARD = 2.00`
- wallet/server `V35_TARGET_BLOCK_TIME_SECONDS = 200`

## 2. Current Mining Reward Creation Flow

Before V35:

1. `Mining.miningDay(...)` filters incoming user transactions.
2. User-submitted `BASIS_ADDRESS` sender transactions are rejected before block assembly.
3. Legacy reward formulas run.
4. A founder BASIS transaction is created and signed.
5. A miner BASIS transaction is created and signed.
6. Both rewards are appended to `forAdd`.
7. Transactions are de-duplicated by signature and sorted by encoded signature.
8. A legacy `Block` constructor mines the block using legacy difficulty.

After V35, currently:

1. `Mining.miningDay(...)` enters `ProtocolVersion.isV35(index)`.
2. The BASIS private key is loaded from `Seting.BASIS_PASSWORD`.
3. `sumRewards` is computed from user transaction `bonusForMiner`.
4. The founder address is read from genesis and checked against `prevBlock.getFounderAddress()`.
5. One instant miner reward transaction is created:
   - sender: `Seting.BASIS_ADDRESS`
   - recipient: `minner.getAccount()`
   - GOLD: `V35RewardCalculator.getInstantMinerGoldReward()` = `6.00`
   - stock: `V35RewardCalculator.getInstantMinerStockReward()` = `0.00`
   - bonus: `sumRewards`
   - vote: `VoteEnum.YES`
6. The miner reward is signed with the BASIS key.
7. The miner reward is appended to `forAdd`.
8. Transactions are de-duplicated by signature and sorted by encoded signature.
9. `selectV35MiningCompactTarget(blockchain)` chooses the compact target.
10. `mineV35Block(...)` mines using `hash <= target`.

Mining currently creates no jackpot payout transactions.

## 3. Whether Mining Has Enough Chain Context

`Mining.miningDay(...)` receives:

- current miner account
- canonical `List<Block> blockchain`
- candidate block `index`
- validated candidate user transactions in `forAdd`
- previous block through `blockchain.get(blockchain.size() - 1)`
- the founder address check
- BASIS signing key access

This is enough context for exact-slot jackpot payout creation if the implementation keeps the strict Phase 4D model:

- period type can be iterated over all six `JackpotCalculator.JackpotPeriod` values
- period instance can be inferred from candidate payout block index and fork index
- eligible period blocks can be collected from `blockchain`
- 60 future hashes can be collected from `blockchain`
- prior payout duplicates can be detected from `blockchain`
- winner miner address can be computed before building the payout transaction
- a candidate payout transaction can be created and verified through the proof adapter before adding it to `forAdd`

Important boundary:

- Mining has enough context only for periods whose first payout block is the current candidate `index`.
- It does not have the new block hash yet, but the jackpot seed must use only `periodEnd + 1` through `periodEnd + 60`; the payout block is `periodEnd + 61`, so the candidate block hash is not part of its own randomness.

## 4. Recommended Payout Creation Strategy

Phase 4E-1 should add a small V35-only helper in `Mining.java`, mirrored in wallet and server, for example:

- `private static List<DtoTransaction> createV35JackpotPayoutTransactions(List<Block> blockchain, long payoutBlockIndex, String founderAddress, PrivateKey basisPrivateKey)`

Recommended flow inside the existing V35 branch:

1. Keep the legacy path untouched.
2. In the V35 branch, create and sign the instant miner reward exactly as today.
3. For each period type:
   - `DAILY`
   - `WEEKLY`
   - `MONTHLY`
   - `QUARTERLY`
   - `HALF_YEAR`
   - `YEARLY`
4. Build `JackpotPeriodInstance` for the candidate block index:
   - `V35JackpotChainContextAdapter.buildPeriodInstance(periodType, index, Seting.V35_RESCUE_FORK_INDEX)`
5. Skip invalid instances.
6. Collect eligible tickets with `V35JackpotChainContextAdapter.collectEligibleTickets(...)`.
7. Collect future seed hashes with `V35JackpotChainContextAdapter.collectFutureHashes(...)`.
8. Compute winner proof through `JackpotCalculator.selectWinner(...)`.
9. Skip if no winner exists.
10. Create a jackpot payout transaction:
    - sender: `Seting.BASIS_ADDRESS`
    - recipient: `proof.getWinnerMinerAddress()`
    - GOLD: `JackpotCalculator.expectedPayoutAmount(periodType).doubleValue()`
    - stock: `0.00`
    - laws: `new Laws()`
    - bonusForMiner: `0.00`
    - vote: `VoteEnum.YES`
11. Sign it with the BASIS private key.
12. Before adding to `forAdd`, call:
    - `V35JackpotChainContextAdapter.verifyPayoutWithChainContext(blockchain, provisionalPayoutBlock, payoutTransaction, founderAddress, Seting.V35_RESCUE_FORK_INDEX)`
13. Add only if the proof result is valid.
14. If proof cannot be built or validation fails, skip the payout rather than creating an unsafe payout.
15. Run the existing de-duplication and signature sort after adding miner reward and any jackpot payouts.

The provisional payout block for proof validation should use:

- the candidate `index`
- current miner address
- founder address
- current transaction list including the jackpot payout candidate
- previous hash can be `prevBlock.getHashBlock()`
- compact target/hash fields do not affect jackpot proof, but use safe placeholders or the selected compact target if already available

Alternative: add a pure helper method in `V35JackpotChainContextAdapter` for mining, such as `buildValidPayoutTransactionCandidates(...)`, but Phase 4E-1 can stay narrower by keeping transaction creation in `Mining.java` and verification in the adapter.

## 5. How Many Jackpot Payouts Can Be Created In One Block

Maximum jackpot payouts in one block: six.

One payout may be created for each period type:

- daily
- weekly
- monthly
- quarterly
- half-year
- yearly

Because periods are fork-relative fixed windows, payout slots can align. A yearly payout slot can also be a half-year, quarterly, monthly, weekly, and daily payout slot depending on window alignment.

The maximum V35 BASIS transactions in a jackpot payout block should therefore be:

- one instant miner reward
- up to six jackpot payouts
- zero founder rewards

Total maximum: seven BASIS transactions.

Validation must continue to count exactly one instant miner reward while allowing only proof-valid jackpot payouts. Existing V35 `UtilsBlock.validationOneBlock(...)` already keeps jackpot candidates distinct from the instant miner count, but ordinary one-block validation still rejects jackpot candidates until canonical context is supplied.

## 6. BASIS Transaction Ordering

Logical creation order should be:

1. User transactions already accepted into `forAdd`
2. Instant miner reward
3. Jackpot payout transactions, ordered by `JackpotPeriod` enum order:
   - daily
   - weekly
   - monthly
   - quarterly
   - half-year
   - yearly

Actual persisted block transaction order currently becomes:

1. All non-null transactions
2. De-duplicated by signature
3. Sorted by `base.encode(t.getSign())`

Phase 4E-1 should preserve this existing final sort to avoid broad block assembly behavior changes. Therefore ordering for consensus should be treated as signature-sorted final order, not insertion order.

Tests should assert:

- all expected jackpot payout transactions exist after mining helper creation
- each payout validates by proof
- no founder reward exists
- exactly one instant miner reward exists
- final ordering may be signature-sorted

## 7. Missed Payout Policy

Recommendation for Phase 4E-1: do not allow late payouts.

Use the strict exact-slot model already implemented by the proof service:

- period end: `periodEnd`
- maturity: `periodEnd + 60`
- first and only payout block: `periodEnd + 61`

If the payout is missed at `periodEnd + 61`, do not create it later in the first implementation.

Why:

- `DtoTransaction` has no period/type marker.
- Amount identifies only period type, not period instance.
- Late payout support would need either transaction metadata or an "oldest matured unpaid period" rule, which is more complex and easier to fork.
- Exact-slot payout keeps transaction format unchanged and duplicate detection deterministic.

Late payout support can be designed later as a separate phase.

## 8. Duplicate Prevention Strategy

Mining should prevent duplicates in two layers:

1. Before transaction creation:
   - use `V35JackpotChainContextAdapter.hasPriorPayoutForPeriod(...)`
   - or collect prior payout markers for the candidate period instance from `blockchain`
   - skip if already paid

2. After transaction creation:
   - build a provisional payout block containing the candidate payout transactions
   - call `V35JackpotChainContextAdapter.verifyPayoutWithChainContext(...)`
   - only add proof-valid transactions

The proof service already rejects:

- previous payout marker duplicates
- same-period duplicate payout candidates inside the payout block
- wrong recipient
- wrong amount
- immature payout
- founder recipient
- invalid/missing seed data

Mining must not rely on amount-only classification.

## 9. Reorg Safety Notes

Jackpot winner and payout eligibility are canonical-chain functions.

If a reorg happens before payout creation:

- eligible tickets can change
- future hash seed can change
- winner can change
- duplicate payout state can change

This is acceptable if the payout is created only from the current canonical `blockchain` list passed into `miningDay(...)` and then validated by peers using their own canonical context.

If a reorg removes the payout block:

- the payout disappears with the old branch
- the new canonical branch recomputes the payout at the exact slot if applicable

Risk to document for Phase 4E-1:

- the miner's local `blockchain` argument must be the same canonical context used for block validation/sync
- mining should skip payout creation if the required period range or seed range is incomplete
- payout creation should not read hidden local state

## 10. Exact Files Phase 4E-1 May Edit

Recommended allowed files:

- `unitedStates_final-master/src/main/java/International_Trade_Union/model/Mining.java`
- `unitedStates_storage-master/src/main/java/International_Trade_Union/model/Mining.java`
- wallet/server protocol V35 helper classes only if a tiny pure helper is needed:
  - `V35JackpotChainContextAdapter.java`
  - `V35JackpotProofService.java`
  - possibly a new pure helper/value object under `International_Trade_Union.protocol.v35`
- wallet/server V35 tests under:
  - `src/test/java/International_Trade_Union/protocol/v35`
- `CITU_V35_PHASE4E1_MINING_JACKPOT_PAYOUT_REPORT.md`

Potentially needed in a follow-up, not preferred in Phase 4E-1:

- `UtilsBlock.java`, only if mining-created jackpot payouts cannot be validated by existing explicit chain-context entrypoint
- `UtilsBalance.java`, only if mining tests require full balance accounting beyond Phase 4D-2G coverage

## 11. Exact Files Phase 4E-1 Must Not Edit

Unless a later user request explicitly allows it:

- `Block.java`
- `Blockchain.java`
- `DtoTransaction.java`
- `Account.java`
- controllers
- UI/templates
- website
- `pom.xml`
- transaction signing format
- transaction field format
- block field format
- hash format

The fork index must remain:

- `Seting.V35_RESCUE_FORK_INDEX = Long.MAX_VALUE`

## 12. Required Tests Before / During Phase 4E-1

Add mirrored wallet/server tests.

Suggested tests:

1. V35 disabled safety:
   - `Seting.V35_RESCUE_FORK_INDEX == Long.MAX_VALUE`
   - normal `miningDay(...)` current indexes remain legacy
   - no jackpot payout helper creates production payouts with disabled fork index

2. Pure/mining-helper payout creation under simulated V35 fork index:
   - daily exact-slot payout transaction is created
   - weekly exact-slot payout transaction is created
   - no payout created before exact slot
   - no payout created after missed slot in first implementation
   - no payout created with insufficient 60 future hashes
   - no payout created with blank/invalid future hash
   - no payout created with blank miner ticket

3. Transaction shape:
   - payout sender is `Seting.BASIS_ADDRESS`
   - payout recipient is deterministic winner miner address
   - payout GOLD amount equals `JackpotCalculator.expectedPayoutAmount(periodType)`
   - payout stock is `0.00`
   - payout `bonusForMiner` is `0.00`
   - payout has BASIS signature
   - `6.00` instant miner reward remains separate and does not classify as jackpot

4. Multiple period payout:
   - if daily/weekly/monthly/etc. payout slots align, helper creates one payout per valid period type
   - at most one payout per period type
   - no founder reward
   - exactly one instant miner reward

5. Proof compatibility:
   - every mining-created jackpot payout passes `UtilsBlock.validateV35JackpotPayoutWithChainContext(...)`
   - every mining-created jackpot payout passes `UtilsBalance.isValidV35JackpotPayoutForBalance(...)`
   - wrong recipient mutation fails proof
   - duplicate same-period payout fails proof

6. Wallet/server parity:
   - same fixture chain produces the same payout period set
   - same fixture chain produces the same winner recipient and amount
   - same fixture chain produces no payout when context is incomplete

7. Build verification:
   - wallet `.\mvnw.cmd test`
   - wallet `.\mvnw.cmd clean package`
   - server `.\mvnw.cmd test`
   - server `.\mvnw.cmd clean package`

## 13. Safe Implementation Order For Phase 4E-1

1. Confirm `V35_RESCUE_FORK_INDEX = Long.MAX_VALUE` in both projects.
2. Add a small V35-only payout candidate creation helper in wallet/server `Mining.java`, or a pure protocol helper if that keeps tests cleaner.
3. Keep the existing legacy branch untouched.
4. In the V35 branch, preserve current instant miner reward creation.
5. Add jackpot payout candidate creation after instant miner reward creation and before final de-duplication/sorting.
6. Iterate period types in deterministic enum order.
7. For each period type, build an exact-slot `JackpotPeriodInstance` for the candidate `index`.
8. Skip invalid/not-due periods.
9. Use the current canonical `blockchain` list to collect tickets, future hashes, and prior payout markers.
10. Compute the winner and create a signed BASIS payout transaction.
11. Validate the candidate with `V35JackpotChainContextAdapter.verifyPayoutWithChainContext(...)`.
12. Add only valid payout transactions.
13. Preserve existing de-duplication by signature and final signature sort.
14. Mine through existing `mineV35Block(...)`.
15. Add tests for helper behavior first, then any feasible `miningDay(...)` integration tests.
16. Run wallet/server test and package commands.
17. Create the Phase 4E-1 implementation report.

## 14. Highest Risks

1. `UtilsBlock.validationOneBlock(...)` ordinary path still rejects jackpot candidates without explicit canonical context. Phase 4E-1 may create blocks that require a richer validation path to accept them.
2. Existing final transaction ordering is signature-sorted, not insertion-ordered. Tests must not assume visible BASIS insertion order after final sorting.
3. Exact-slot payout is strict. A node that misses payout creation at the exact block will not pay it later under the current design.
4. `DtoTransaction` has no period marker; late payout support must not be smuggled into Phase 4E-1.
5. Mining must use the canonical `blockchain` argument only; hidden local state or partial chain context would be unsafe.
6. BASIS signatures for multiple payout transactions must be unique because recipient/amount differ; final signature de-duplication could drop a duplicate if a bug creates two identical transactions.
7. Wallet/server mining code is duplicated with different line numbers; implementation must keep both copies behaviorally identical.

## 15. Rollback Plan

If Phase 4E-1 causes issues:

1. Revert only Phase 4E-1 edits in wallet/server `Mining.java` and any helper/test files created in that phase.
2. Keep Phase 4D-2D/2E/2F/2G proof, validation entrypoint, and balance accounting helpers unless they are directly implicated.
3. Confirm `V35_RESCUE_FORK_INDEX = Long.MAX_VALUE`.
4. Rerun:
   - wallet `.\mvnw.cmd test`
   - server `.\mvnw.cmd test`
5. Mining will return to current V35 behavior: only the `6.00 GOLD` instant miner reward is created after V35.

## 16. Recommendation

Proceed to Phase 4E-1 by adding V35-only jackpot payout creation in `Mining.java`, still disabled by `V35_RESCUE_FORK_INDEX = Long.MAX_VALUE`.

Use exact-slot payouts only:

- period type from fixed enum iteration
- period instance from candidate payout block index
- winner from `JackpotCalculator.selectWinner(...)`
- validation through `V35JackpotChainContextAdapter`
- no late payouts
- no transaction or block format changes

This keeps mining payout creation aligned with `UtilsBlock` explicit chain-context validation and `UtilsBalance` jackpot accounting.
