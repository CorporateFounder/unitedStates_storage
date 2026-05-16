# CITU V35 Phase 4A Jackpot Design / Wiring Plan

Phase: 4A  
Project root: `C:\citu`  
Scope: design and wiring plan only. No source code, tests, mining, validation, controllers, UI, website, transaction format, block format, or hash format were changed.

## Reports Read

Read and searched all `C:\citu\CITU_V35_PHASE*.md` reports, with special attention to:

- `C:\citu\CITU_V35_PHASE2B_REWARD_WIRING_REPORT.md`
- `C:\citu\CITU_V35_PHASE2C_REWARD_FIXTURE_TEST_REPORT.md`
- `C:\citu\CITU_V35_PHASE3B0_POW_HELPERS_REPORT.md`
- `C:\citu\CITU_V35_PHASE3B1_POW_VALIDATION_REPORT.md`
- `C:\citu\CITU_V35_PHASE3B2_MINING_TARGET_REPORT.md`
- `C:\citu\CITU_V35_PHASE3B3_TARGET_ADJUSTMENT_REPORT.md`
- `C:\citu\CITU_V35_PHASE3B4A_CUMULATIVE_WORK_TOURNAMENT_PLAN.md`
- `C:\citu\CITU_V35_PHASE3B4B0_CHAIN_SCORING_HELPERS_REPORT.md`
- `C:\citu\CITU_V35_PHASE3B4B1_CUMULATIVE_WORK_METADATA_REPORT.md`
- `C:\citu\CITU_V35_PHASE3B4B2_CUMULATIVE_WORK_METADATA_POPULATION_REPORT.md`
- `C:\citu\CITU_V35_PHASE3B4B3_CUMULATIVE_WORK_COMPARATOR_REPORT.md`
- `C:\citu\CITU_V35_PHASE3B4B3_FORK_GATE_FIX_REPORT.md`
- `C:\citu\CITU_V35_PHASE3B4B4A_TOURNAMENT_SERVICE_REVIEW_PLAN.md`
- `C:\citu\CITU_V35_PHASE3B4B4B_TOURNAMENT_SERVICE_REPORT.md`
- `C:\citu\CITU_V35_PHASE3B4B4C_BASISCONTROLLER_RESOLVE_REVIEW.md`
- `C:\citu\CITU_V35_PHASE3B4B4D_BASISCONTROLLER_RESOLVE_FIX_REPORT.md`

## Exact Files And Methods Inspected

### Constants

Wallet:

- `C:\citu\unitedStates_final-master\src\main\java\International_Trade_Union\setings\Seting.java`
  - `BASIS_ADDRESS`: line `68`
  - `BUDGET`: line `110`
  - `V35_RESCUE_FORK_INDEX = Long.MAX_VALUE`: line `362`
  - `V35_INSTANT_MINER_REWARD = 8.00`: line `365`
  - `V35_JACKPOT_REWARD = 2.00`: line `366`
  - jackpot split constants: lines `368-373`
  - `V35_TARGET_BLOCK_TIME_SECONDS = 150`: line `378`

Server:

- `C:\citu\unitedStates_storage-master\src\main\java\International_Trade_Union\setings\Seting.java`
  - `BASIS_ADDRESS`: line `63`
  - `BUDGET`: line `104`
  - `V35_RESCUE_FORK_INDEX = Long.MAX_VALUE`: line `348`
  - `V35_INSTANT_MINER_REWARD = 8.00`: line `351`
  - `V35_JACKPOT_REWARD = 2.00`: line `352`
  - jackpot split constants: lines `354-359`
  - `V35_TARGET_BLOCK_TIME_SECONDS = 150`: line `364`

### V35 Helper Classes

Wallet and server have matching package:

- `International_Trade_Union.protocol.v35`

Inspected server copies:

- `C:\citu\unitedStates_storage-master\src\main\java\International_Trade_Union\protocol\v35\JackpotCalculator.java`
  - `getDailyPerBlock()`
  - `getWeeklyPerBlock()`
  - `getMonthlyPerBlock()`
  - `getQuarterlyPerBlock()`
  - `getHalfYearPerBlock()`
  - `getYearlyPerBlock()`
  - `getTotalPerBlock()`
  - `validateSplit()`
- `C:\citu\unitedStates_storage-master\src\main\java\International_Trade_Union\protocol\v35\TicketWeightCalculator.java`
  - `baseWeight()`
  - `maxWeight()`
  - `capTxBoost(double txBoost)`
  - `capStakeBoost(double stakeBoost)`
  - `combine(double txBoost, double stakeBoost)`
- `C:\citu\unitedStates_storage-master\src\main\java\International_Trade_Union\protocol\v35\V35RewardCalculator.java`
  - `getTotalGoldReward()`
  - `getInstantMinerGoldReward()`
  - `getInstantMinerStockReward()`
  - `getJackpotGoldReward()`
  - `validateJackpotSplit()`
  - `isValidInstantMinerReward(...)`

### Reward Creation

Wallet:

- `C:\citu\unitedStates_final-master\src\main\java\International_Trade_Union\model\Mining.java`
  - pre-filter rejects user-submitted BASIS sender transactions: lines `233-239`
  - V35 reward branch begins: line `279`
  - V35 miner reward transaction creation: lines `288-295`
  - V35 BASIS signing: lines `296-297`
  - V35 block mining call: lines `311-312`
  - legacy reward calculation follows: lines `316+`

Server:

- `C:\citu\unitedStates_storage-master\src\main\java\International_Trade_Union\model\Mining.java`
  - pre-filter rejects user-submitted BASIS sender transactions: lines `201-208`
  - V35 reward branch begins: line `243`
  - V35 miner reward transaction creation: lines `252-259`
  - V35 BASIS signing: lines `260-261`
  - V35 block mining call: lines `275-276`
  - legacy reward calculation follows: lines `280+`

### Reward Validation

Server inspected directly:

- `C:\citu\unitedStates_storage-master\src\main\java\International_Trade_Union\utils\UtilsBlock.java`
  - transaction loop begins: lines `566-569`
  - BASIS branch begins: line `577`
  - V35 branch begins: line `578`
  - founder reward rejected after V35: lines `579-584`
  - V35 BASIS reward restricted to current block miner: lines `586-591`
  - V35 instant miner reward exactness: lines `593-601`
  - V35 BASIS count limit over one: lines `603-608`
  - legacy reward validation branch begins: line `609`
  - legacy miner reward cap checks: lines `752-780`
  - legacy founder reward detection: lines `783-898`
  - legacy non-founder BASIS count: lines `901-905`
  - legacy founder BASIS count limit: lines `907-915`
  - legacy non-founder BASIS count limit: lines `918-926`

Phase 2A reports show wallet and server `UtilsBlock.validationOneBlock(...)` use matching reward-validation structure.

### Balance Calculation

Server inspected directly:

- `C:\citu\unitedStates_storage-master\src\main\java\International_Trade_Union\utils\UtilsBalance.java`
  - rollback balance BASIS send count: lines `67-78`
  - rollback BASIS recipient restriction to founder or miner: lines `107-118`
  - forward balance BASIS send count: lines `234-240`
  - forward BASIS recipient restriction to founder or miner: lines `274-289`

Important current rule: balance calculation assumes BASIS rewards are only for the block founder or current block miner. A jackpot winner from an earlier period will often not be the current block miner, so balance code must be updated in a future V35-gated phase before explicit jackpot payouts can validate and calculate balances safely.

## Current BASIS Reward Flow

Current V35 mining creates one BASIS transaction:

- sender: `Seting.BASIS_ADDRESS`
- customer: current block `minerAddress`
- digitalDollar: `V35RewardCalculator.getInstantMinerGoldReward()` = `8.00`
- digitalStockBalance: `0.00`
- bonusForMiner: accumulated user transaction fees
- signed by `Seting.BASIS_PASSWORD`

No founder transaction is created after V35. No jackpot payout transaction is created yet.

Current V35 validation accepts only the safe Phase 2B shape:

- no BASIS to founder
- exactly one BASIS transaction for normal V35 blocks
- BASIS customer must equal `thisBlock.getMinerAddress()`
- reward must equal 8 GOLD and 0 stock

## Can Current Validation Accept Jackpot Payouts?

No.

An explicit jackpot payout as a second BASIS transaction would currently be rejected after V35 because:

- `UtilsBlock.validationOneBlock(...)` rejects any V35 BASIS transaction whose customer is not the current block miner.
- `UtilsBlock.validationOneBlock(...)` rejects more than one BASIS send in a V35 block.
- `UtilsBalance.calculateBalance(...)` and rollback logic reject BASIS recipients other than founder or current block miner.

Therefore a jackpot payout can use the existing transaction shape only after future V35-gated validation and balance rules explicitly recognize deterministic jackpot payout transactions.

## Jackpot Accounting Model Options

### Option 1: Virtual Reserve Accounting Only

Description:

- Do not create per-block reserve transactions.
- Reserve amount is derived from block indexes and constants.
- Example: daily pool = eligible block count in period times `V35_DAILY_JACKPOT_PER_BLOCK`.

Complexity:

- Low for pure helpers.
- Medium for explorer/API proof views.

Validation safety:

- High, because no new per-block transaction is added.
- Avoids touching transaction format.

Old-chain risk:

- Low when fork-gated.

Explorer/UI clarity:

- Good if API exposes period proofs.
- Weaker than explicit reserve transactions because the reserve is computed rather than visible as a balance transfer.

Auditability:

- Good if deterministic helper exposes period ranges, constants, pool size, seed, and winner.

### Option 2: Explicit Reserve Address

Description:

- Send 2 GOLD per block or split amounts to a jackpot reserve address.
- Later pay winners from that reserve address.

Complexity:

- High.

Validation safety:

- Risky.
- Requires a spendable or unspendable reserve policy and new validation rules.
- Raises private-key/keyless address questions.

Old-chain risk:

- Medium/high if balance and BASIS assumptions are disturbed.

Explorer/UI clarity:

- Strong: pool balance is visible.

Auditability:

- Strong if implemented perfectly, but implementation risk is high.

### Option 3: Explicit Payout Transaction At Maturity

Description:

- Do not create per-block reserve transactions.
- At deterministic maturity block, include a BASIS to winner payout transaction for the computed accumulated pool.

Complexity:

- Medium.

Validation safety:

- Good if validation is strict:
  - period range
  - winner
  - amount
  - one payout per period
  - no premature payout
  - fork-gated

Old-chain risk:

- Low/medium when fork-gated.

Explorer/UI clarity:

- Strong: winner payout is visible on-chain.

Auditability:

- Strong: reserve is computed; payout is explicit.

### Option 4: Special Jackpot BASIS Transaction

Description:

- Use a BASIS transaction with existing fields but treat it as jackpot by deterministic context.
- No transaction format change.

Complexity:

- Medium/high.

Validation safety:

- Depends on whether the transaction can be uniquely classified without hidden state.
- If classification is only by amount/customer/block index, duplicate and ambiguity risks must be handled carefully.

Old-chain risk:

- Low when V35-gated, but higher than pure virtual accounting.

Explorer/UI clarity:

- Medium unless API labels it.

Auditability:

- Good with deterministic proof APIs.

## Recommended Accounting Model

Use a hybrid:

1. Virtual reserve accounting for every V35 block.
2. Explicit deterministic BASIS payout transaction at maturity.
3. No explicit reserve address.
4. No per-block jackpot reserve transaction.
5. No transaction format or block format change.

This keeps the 2 GOLD reserve computable from protocol constants while making each actual jackpot payment visible as a normal signed BASIS transaction. The payout transaction must be recognized by future V35 validation as a jackpot payout, not as the instant miner reward.

The initial implementation should support only deterministic unweighted tickets first. Weighted ticket boosts should be a later phase.

## Period Boundary Recommendation

Recommended: fixed block windows, not wall-clock calendar periods.

Rationale:

- Block indexes are deterministic and stable across all nodes.
- Wall-clock calendar periods require timezone/calendar interpretation.
- Timestamp manipulation and reorgs can cause boundary disputes.
- V35 target block time is `150` seconds, so expected blocks per day are:
  - `86400 / 150 = 576`
- Older legacy code/docs mention `432` blocks/day in some V34/optimal-score branches, but V35 constants explicitly use `V35_TARGET_BLOCK_TIME_SECONDS = 150`, so V35 jackpot windows should use 576 blocks/day.

Recommended V35 windows:

- daily: `576` blocks
- weekly: `576 * 7 = 4032` blocks
- monthly: `576 * 30 = 17280` blocks
- quarterly: `576 * 90 = 51840` blocks
- half-year: `576 * 182 = 104832` blocks
- yearly: `576 * 365 = 210240` blocks

Use constants/helpers for these. Avoid relying on `Seting.YEAR` legacy meaning unless a future constants phase explicitly maps it.

Period start:

- `firstV35Index = Seting.V35_RESCUE_FORK_INDEX`
- `periodNumber = (blockIndex - firstV35Index) / windowSize`
- `periodStart = firstV35Index + periodNumber * windowSize`
- `periodEnd = periodStart + windowSize - 1`

Partial first periods should not exist if the fork starts exactly at `firstV35Index`; the first period begins with the first V35 block.

## Deterministic Randomness Design

Recommended delayed randomness:

- Eligible ticket blocks: `[periodStart, periodEnd]`
- Period ends at `periodEnd`
- Delay: `K = 60` future blocks
- Maturity block: `periodEnd + K`
- Seed source: hashes of future blocks `[periodEnd + 1, periodEnd + K]`
- Seed:
  - `sha256(hash(periodEnd + 1) + hash(periodEnd + 2) + ... + hash(periodEnd + K))`
- Winner offset:
  - `seedNumber % ticketCount`
- Winner block:
  - `eligibleBlocks[winnerOffset]`
- Winner address:
  - `winnerBlock.getMinerAddress()`

Why `K = 60`:

- It matches `V35_TARGET_WINDOW = 60`.
- Expected delay is `60 * 150 = 9000` seconds, about 2.5 hours.
- This makes manipulation harder because a miner at period end cannot know all future seed hashes.
- It is short enough for daily UX.

Insufficient future blocks:

- Do not select or pay a winner yet.
- Jackpot status should show `pending randomness`.
- Validation must reject payout before maturity.

Reorg handling:

- Winner and payout are functions of the canonical chain.
- If a reorg changes blocks before payout maturity, the computed winner may change.
- If a reorg removes a payout block, the payout is recalculated on the new canonical chain.
- Once finalized by normal chain selection, the payout remains part of valid chain history.

Empty periods:

- With fixed windows after V35, every valid block in the range is a ticket.
- If a pathological period has no valid blocks, no payout is created and the reserve calculation for that period should be zero.
- In practice, no blocks means no chain progress and no maturity block, so payout cannot occur.

## Ticket Eligibility Design

Rules:

- Every mined V35 block in the period is one ticket.
- Eligibility address is `block.getMinerAddress()`.
- Sender addresses from transactions are never eligible.
- Founder address is not eligible unless it is also the miner of an eligible block by normal mining rules.
- Invalid blocks are not tickets.
- Blocks before V35 are not tickets.
- Jackpot ticket does not create extra GOLD beyond the reserved pool.

## Ticket Weight Design

### Phase 4 First Implementation

Recommended first implementation:

- Unweighted tickets only.
- Each eligible mined block has weight `1.00`.
- Ignore tx/stake boosts for payout selection initially.

Why:

- Simpler validation.
- Easier deterministic tests.
- Lower spam incentive.
- Avoids accidentally reintroducing stake/transaction dominance after Phase 3 work removed it from chain priority.

### Later Weighted Version

Optional later version:

- Base ticket weight: `Seting.V35_BASE_TICKET_WEIGHT = 1.00`
- Maximum ticket weight: `Seting.V35_MAX_TICKET_WEIGHT = 1.50`
- Maximum tx boost: `Seting.V35_MAX_TX_BOOST = 0.30`
- Maximum stake/Silver boost: `Seting.V35_MAX_STAKE_BOOST = 0.20`
- Use `TicketWeightCalculator.combine(txBoost, stakeBoost)`.

Rules for later weighted version:

- Boosts must be capped.
- Boosts must not create extra GOLD.
- Boosts must be calculated from the mined block context, not transaction senders as lottery participants.
- Transaction boost must resist spam, likely by using capped fee/unique-valid-transaction metrics.
- Staking/Silver boost must be snapshot-based and capped.
- Weighted selection must be deterministic and tested with exact decimal/integer scaling.

Recommendation: defer weighted tickets until after unweighted jackpot payout is live and tested.

## Jackpot Payout Validation Design

Future validation must check:

1. V35 gate:
   - no jackpot payout before `Seting.V35_RESCUE_FORK_INDEX`
   - no jackpot logic while fork index is `Long.MAX_VALUE`

2. Period type:
   - daily, weekly, monthly, quarterly, half-year, yearly
   - derived by block index window

3. Payout maturity:
   - payout block index must be at or after `periodEnd + K`
   - recommended first version: require payout exactly at `periodEnd + K` if the maturity block is mined by the node; alternatively allow first block after maturity that has not already paid the period

4. Payout amount:
   - `eligibleBlockCount * perBlockSplit`
   - daily uses `0.70`, weekly `0.40`, monthly `0.30`, quarterly `0.25`, half-year `0.20`, yearly `0.15`
   - amount must use the same rounding policy on wallet and server

5. Winner:
   - compute seed from delayed future hashes
   - compute winner ticket
   - `transaction.customer` must equal selected `winnerBlock.getMinerAddress()`

6. Sender:
   - jackpot payout sender must be `Seting.BASIS_ADDRESS`

7. Signature:
   - normal transaction signature verification still applies

8. Duplicate prevention:
   - only one payout per `(periodType, periodNumber)` on canonical chain
   - because transaction format has no explicit jackpot period field, validation must infer period from payout amount and maturity block, or future phases must add a deterministic classification helper

9. Founder involvement:
   - no founder reward
   - no founder-specific jackpot recipient rule

10. BASIS count:
   - V35 validation must allow:
     - one instant miner BASIS reward
     - zero or more matured jackpot BASIS payouts in blocks where jackpot periods mature
   - count must be bounded by the maximum number of period types maturing in one block: six jackpot payouts plus one instant miner reward

11. Balance calculation:
   - `UtilsBalance.calculateBalance(...)` and rollback must allow validated V35 jackpot BASIS payouts to deterministic winners, not only current miner/founder.

## Can Jackpot Payout Use BASIS Transactions Safely?

Not safely today.

It can become safe if Phase 4D modifies validation and balance calculation behind a strict V35 gate.

Required future changes:

- `UtilsBlock.validationOneBlock(...)`
  - classify V35 BASIS transactions into:
    - instant miner reward
    - jackpot payout
  - reject unclassified BASIS transactions
  - validate jackpot period, amount, winner, maturity, and duplicates
- `UtilsBalance.calculateBalance(...)`
  - allow V35 jackpot BASIS payout recipients only when the payout validates deterministically
- rollback balance logic
  - mirror the same V35 jackpot recipient allowance
- `Mining.java`
  - include matured jackpot payout transactions only after validation helpers are implemented and tested

## API Needs

Future server endpoints:

- `GET /api/v35/protocol/jackpot-config`
  - window sizes, per-block splits, delay K, fork index, active/disabled
- `GET /api/v35/jackpot/status`
  - all periods, current period range, pool amounts, maturity status
- `GET /api/v35/jackpot/history`
  - paid periods and winners
- `GET /api/v35/jackpot/period/{type}/{periodNumber}`
  - period start/end, maturity, eligible ticket count, seed block range, pool amount
- `GET /api/v35/jackpot/tickets/{address}`
  - miner tickets for address by active period
- `GET /api/v35/jackpot/winner/{type}/{periodNumber}`
  - deterministic winner proof when mature
- `GET /api/v35/jackpot/proof/{type}/{periodNumber}`
  - seed hash inputs, seed, selected ticket index, winner block hash/miner

Do not add endpoints until protocol helpers and validation are stable.

## Wallet UI Needs

Future wallet views:

- daily/weekly/monthly/quarterly/half-year/yearly countdowns
- current pool amounts
- user's mined tickets by period
- last winners
- pending randomness state
- payout proof view:
  - period range
  - future seed blocks
  - seed
  - selected ticket
  - winner miner address
- clear text that sender addresses are not jackpot participants
- clear text that tx/stake boosts, if later enabled, affect capped ticket weight only and do not create extra GOLD

## Website Needs

Future website content:

- V35 jackpot overview
- fixed 10 GOLD/block split:
  - 8 GOLD instant miner
  - 2 GOLD jackpot reserve
- period split table
- every mined block is a ticket
- winner is selected from minerAddress of selected block
- deterministic delayed randomness explanation
- no manual winner selection
- no sender-address lottery
- V35 disabled until final fork index is set
- proof/audit explanation

## Safe Implementation Order

### Phase 4B: Pure Jackpot Helpers + Tests

Allowed likely files:

- wallet/server `International_Trade_Union.protocol.v35.JackpotCalculator`
- new wallet/server helper classes if useful:
  - `JackpotPeriod`
  - `JackpotPeriodType`
  - `JackpotPeriodCalculator`
  - `JackpotSeedCalculator`
  - `JackpotTicket`
  - `JackpotWinnerCalculator`

No mining or validation wiring.

Tests:

- split totals
- window sizes
- period start/end
- maturity index
- seed calculation from hashes
- unweighted winner selection
- empty/insufficient future blocks
- wallet/server parity

### Phase 4C: Deterministic Period/Winner Fixtures

Build fixture blocks and prove:

- every V35 block in range is a ticket
- sender addresses ignored
- minerAddress selected
- delayed seed is deterministic
- reorg input changes winner predictably
- K=60 maturity behavior

No payout transaction validation yet.

### Phase 4D: Jackpot Payout Validation Behind V35 Gate

Likely files:

- wallet/server `UtilsBlock.java`
- wallet/server `UtilsBalance.java`
- wallet/server jackpot helper classes/tests

Validation must allow deterministic jackpot BASIS payouts only after V35 and only when mature.

Do not change transaction format.

### Phase 4E: Mining/Payout Transaction Wiring Behind V35 Gate

Likely files:

- wallet/server `Mining.java`
- wallet/server jackpot helper classes/tests

Mining should add matured jackpot payout transactions only after validation can reject wrong payouts.

### Phase 4F: API Endpoints

Likely files:

- server controllers
- DTOs if needed, but avoid consensus DTO changes

Endpoints should expose status/proof/history, not define consensus.

### Phase 4G: Wallet/Site Display

Likely files:

- wallet templates/static files
- website pages/components/assets/text

Display only after protocol/API shape is stable.

## Required Tests

Pure helper tests:

- constants sum to 2.00
- 576 blocks/day from 150-second target
- all period windows
- period range from fork index
- maturity index = `periodEnd + 60`
- seed hash concatenation and SHA-256 result
- winner index = seed mod ticket count
- minerAddress is winner identity
- sender addresses ignored
- insufficient future blocks means pending

Validation tests:

- no jackpot before V35
- no payout while fork index is `Long.MAX_VALUE`
- wrong amount rejected
- wrong winner rejected
- wrong period rejected
- premature payout rejected
- duplicate period payout rejected
- founder reward still rejected
- instant miner reward remains exactly 8 GOLD
- jackpot payout does not alter transaction format
- balance calculation accepts only deterministic jackpot BASIS payout recipients
- rollback mirrors forward balance behavior

Mining tests:

- no payout before maturity
- payout included at maturity
- multiple period payouts in same block if several periods mature
- invalid payout cannot be mined by honest node

Integration tests:

- wallet/server helper parity
- old pre-V35 blocks still validate
- V35 block with only instant miner reward still validates when no jackpot is mature
- V35 block with mature jackpot payout validates
- V35 block with fake jackpot payout is rejected

## Highest Risks

1. Current V35 validation allows only one BASIS transaction to the current miner, so jackpot payout needs careful V35-gated validation changes.
2. Balance calculation currently restricts BASIS recipients to founder or current miner; jackpot winners may be earlier miners.
3. Transaction format has no explicit jackpot period field, so payout classification must be deterministic and unambiguous.
4. Duplicate payout prevention requires scanning or derived state across the canonical chain.
5. Wall-clock periods would be fragile; fixed block windows are strongly recommended.
6. Weighted tickets can create spam/stake dominance if introduced too early.
7. Rounding of fractional pool amounts must match exactly between wallet and server.
8. Reorg behavior must be tested before activation.

## Rollback Plan

If a future jackpot phase fails:

1. Keep `V35_RESCUE_FORK_INDEX = Long.MAX_VALUE`.
2. Revert only that phase's jackpot helper/validation/mining/API/UI changes.
3. Preserve prior V35 reward, PoW, cumulativeWork, comparator, tournament, and resolve-admission phases.
4. Rerun wallet/server tests and builds.
5. Return to pure-helper tests before attempting wiring again.

## Recommendation Summary

Use fixed block windows, unweighted mined-block tickets first, delayed deterministic randomness with `K = 60` future blocks, virtual reserve accounting, and explicit deterministic BASIS payout transactions at maturity. Do not add a reserve address and do not change transaction format. Future validation must classify jackpot BASIS payouts behind the V35 fork gate before mining is allowed to create them.
