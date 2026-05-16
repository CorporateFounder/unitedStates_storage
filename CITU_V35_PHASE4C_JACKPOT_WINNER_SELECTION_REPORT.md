# CITU V35 Phase 4C Jackpot Winner Selection Report

## Scope

Phase 4C implemented pure deterministic V35 jackpot winner selection helpers only. No payout wiring, mining wiring, validation wiring, balance logic, BASIS transaction logic, API, UI, website, block format, transaction format, or hash format was changed.

V35 remains disabled in both projects:

- `unitedStates_final-master/src/main/java/International_Trade_Union/setings/Seting.java`: `V35_RESCUE_FORK_INDEX = Long.MAX_VALUE`
- `unitedStates_storage-master/src/main/java/International_Trade_Union/setings/Seting.java`: `V35_RESCUE_FORK_INDEX = Long.MAX_VALUE`

## Files Changed

Wallet project:

- `unitedStates_final-master/src/main/java/International_Trade_Union/protocol/v35/JackpotCalculator.java`
- `unitedStates_final-master/src/main/java/International_Trade_Union/protocol/v35/JackpotTicket.java`
- `unitedStates_final-master/src/main/java/International_Trade_Union/protocol/v35/JackpotWinnerProof.java`
- `unitedStates_final-master/src/test/java/International_Trade_Union/protocol/v35/JackpotWinnerSelectionTest.java`
- `unitedStates_final-master/src/test/java/International_Trade_Union/protocol/v35/V35ParityTest.java`

Server project:

- `unitedStates_storage-master/src/main/java/International_Trade_Union/protocol/v35/JackpotCalculator.java`
- `unitedStates_storage-master/src/main/java/International_Trade_Union/protocol/v35/JackpotTicket.java`
- `unitedStates_storage-master/src/main/java/International_Trade_Union/protocol/v35/JackpotWinnerProof.java`
- `unitedStates_storage-master/src/test/java/International_Trade_Union/protocol/v35/JackpotWinnerSelectionTest.java`
- `unitedStates_storage-master/src/test/java/International_Trade_Union/protocol/v35/V35ParityTest.java`

## Helper Classes Added

### `JackpotTicket`

Package:

- `International_Trade_Union.protocol.v35`

Fields:

- `long blockIndex`
- `String minerAddress`
- `double weight`

Methods:

- `JackpotTicket(long blockIndex, String minerAddress, double weight)`
- `static JackpotTicket of(long blockIndex, String minerAddress)`
- `long getBlockIndex()`
- `String getMinerAddress()`
- `double getWeight()`
- `boolean isValid()`

Rules:

- Miner address is trimmed and must be nonblank.
- Negative block indexes are invalid.
- Invalid, zero, negative, infinite, or NaN weights are invalid.
- Weight is capped at `TicketWeightCalculator.maxWeight()` for future weighted selection work.
- Phase 4C still treats each eligible mined block as one ticket.

### `JackpotWinnerProof`

Package:

- `International_Trade_Union.protocol.v35`

Fields:

- `long periodStart`
- `long periodEnd`
- `long maturityBlock`
- `int randomDelayBlocks`
- `String seedHex`
- `int selectedOffset`
- `long selectedBlockIndex`
- `String winnerMinerAddress`

Methods:

- `JackpotWinnerProof(...)`
- `static JackpotWinnerProof noWinner(...)`
- getters for all fields
- `boolean hasWinner()`

Rules:

- Proof contains enough data to re-check winner selection deterministically.
- Proof contains no payout amount and does not imply creation of a BASIS transaction.
- No hidden jackpot state was added.

## `JackpotCalculator` Methods Added

Period helpers:

- `periodStartForBlock(long blockIndex, long windowSize)`
- `periodEndForBlock(long blockIndex, long windowSize)`
- `maturityBlockForPeriod(long blockIndex, long windowSize)`

Maturity and seed helpers:

- `boolean isMature(long currentHeight, long periodEnd)`
- `String seedFromFutureHashes(List<String> futureHashes)`

Ticket helpers:

- `boolean isEligibleTicket(long blockIndex, String minerAddress, boolean isV35Block)`
- `List<JackpotTicket> eligibleTickets(List<JackpotTicket> tickets)`

Winner helpers:

- `JackpotWinnerProof selectWinner(long periodStart, long periodEnd, List<JackpotTicket> tickets, List<String> futureHashes)`
- `JackpotWinnerProof selectWinner(List<JackpotTicket> tickets, List<String> futureHashes)`

## Deterministic Winner Selection Rules

- Only V35 mined blocks with a nonblank miner address are eligible.
- Sender addresses are not inputs to the helper and cannot affect eligibility.
- Period-based selection filters tickets to `periodStart <= blockIndex <= periodEnd`.
- Tickets are sorted by `blockIndex` before selection.
- Random delay is `60` future blocks.
- `seedFromFutureHashes(...)` requires at least 60 future hashes.
- Seed is `SHA-256` over the first 60 delayed future hashes in order.
- Winner offset is `seedNumber mod eligibleTicketCount`.
- Empty tickets, invalid period, invalid seed, or insufficient future hashes return a no-winner proof.
- The same tickets and future hashes always produce the same proof.
- Different future hash order produces a different seed.

## Maturity Behavior

- Random delay: `JACKPOT_RANDOM_DELAY_BLOCKS = 60`
- `maturityBlock = periodEnd + 60`
- `isMature(currentHeight, periodEnd)` returns true only when `currentHeight >= maturityBlock`.
- No payout or validation behavior uses this yet.

## Tests Added / Updated

New wallet/server test file:

- `JackpotWinnerSelectionTest`

Coverage:

- V35 ticket eligibility requires V35 block and miner address.
- Non-V35 blocks are not eligible.
- Blank/null miner address is not eligible.
- Sender address is not used.
- Ticket weight is sanitized and capped.
- Period maturity requires 60 future blocks.
- Future-hash seed is deterministic and order-sensitive.
- Insufficient/invalid future hashes are safe.
- Eligible tickets are sorted by block index.
- Winner selection is deterministic.
- Winner proof fields are populated.
- Tickets outside the period are excluded.
- Empty ticket list returns no winner.
- Insufficient future hashes return no winner.
- Daily and weekly period examples select valid winners.

Updated wallet/server test file:

- `V35ParityTest`

Coverage:

- New `JackpotTicket` and `JackpotWinnerProof` classes use the shared `International_Trade_Union.protocol.v35` package.
- New `JackpotCalculator` winner-selection method signatures exist in both projects.
- New model getter/proof methods exist in both projects.

## Verification Results

Wallet:

- Command: `.\mvnw.cmd test`
- Result: PASS
- Tests: 80 run, 0 failures, 0 errors, 0 skipped

- Command: `.\mvnw.cmd clean package`
- Result: PASS
- Tests during package: 80 run, 0 failures, 0 errors, 0 skipped

Server:

- Command: `.\mvnw.cmd test`
- Result: PASS
- Tests: 92 run, 0 failures, 0 errors, 0 skipped

- Command: `.\mvnw.cmd clean package`
- Result: PASS
- Tests during package: 92 run, 0 failures, 0 errors, 0 skipped

Build notes:

- Maven still reports pre-existing duplicate dependency warnings in both projects.
- No `pom.xml` files were changed.

## Behavior Confirmation

- V35 remains disabled.
- No mining behavior changed.
- No validation behavior changed.
- No balance behavior changed.
- No BASIS payout wiring was added.
- No jackpot payout transaction creation was added.
- No block, transaction, signature, or hash format changed.
- Wallet and server helper classes/method signatures match.

## Risks Before Phase 4D

- Jackpot payout validation is not implemented yet.
- Jackpot payout representation is still deferred; Phase 4A recommended explicit deterministic payout validation before mining creates payouts.
- Full block-range integration is still missing because helpers intentionally avoid `Block`, `Blockchain`, mining, and validation classes.
- Weighted ticket selection is prepared only at the model level; Phase 4C selection remains one mined block equals one ticket.
- Future Phase 4D must ensure duplicate payout prevention, period-boundary validation, delayed seed validation, and no payout before V35.

## Recommended Next Phase

Phase 4D: V35 jackpot payout validation behind the V35 fork gate, still no mining payout creation.
