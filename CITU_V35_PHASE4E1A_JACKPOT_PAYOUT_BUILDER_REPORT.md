# CITU V35 Phase 4E-1A Jackpot Payout Builder Report

## Scope

Phase 4E-1A added a pure jackpot payout builder in the wallet and server projects. It is not wired into `Mining.java`, `UtilsBlock.java`, `UtilsBalance.java`, controllers, API, UI, website, persistence, or chain validation.

V35 remains disabled in both projects because `Seting.V35_RESCUE_FORK_INDEX = Long.MAX_VALUE`.

## Files Changed

Wallet:
- `C:\citu\unitedStates_final-master\src\main\java\International_Trade_Union\protocol\v35\V35JackpotPayoutBuilder.java`
- `C:\citu\unitedStates_final-master\src\test\java\International_Trade_Union\protocol\v35\V35JackpotPayoutBuilderTest.java`

Server:
- `C:\citu\unitedStates_storage-master\src\main\java\International_Trade_Union\protocol\v35\V35JackpotPayoutBuilder.java`
- `C:\citu\unitedStates_storage-master\src\test\java\International_Trade_Union\protocol\v35\V35JackpotPayoutBuilderTest.java`

Report:
- `C:\citu\CITU_V35_PHASE4E1A_JACKPOT_PAYOUT_BUILDER_REPORT.md`

No production mining, validation, balance, DTO, block, blockchain, controller, UI, website, or `pom.xml` files were changed.

## Builder Added

Class:
- `International_Trade_Union.protocol.v35.V35JackpotPayoutBuilder`

Public methods:
- `buildPayoutTransactions(List<Block> canonicalChain, long payoutBlockIndex, String minerAddress, String founderAddress)`
- `buildPayoutTransactions(List<Block> canonicalChain, long payoutBlockIndex, String minerAddress, String founderAddress, long forkIndex)`
- `buildPayoutCandidates(List<Block> canonicalChain, long payoutBlockIndex, String minerAddress, String founderAddress)`
- `buildPayoutCandidates(List<Block> canonicalChain, long payoutBlockIndex, String minerAddress, String founderAddress, long forkIndex)`
- `isExactPayoutSlot(JackpotCalculator.JackpotPeriod period, long payoutBlockIndex, long forkIndex)`

Private helper methods:
- `buildPayoutTransactionForPeriod(...)`
- `provisionalBlock(...)`
- `latestHash(...)`
- `supportedPeriods()`
- `normalize(String value)`

## Output Model

The builder outputs actual existing `DtoTransaction` objects when proof validation succeeds:
- `sender = Seting.BASIS_ADDRESS`
- `customer = deterministic winner minerAddress`
- `digitalDollar = canonical jackpot payout amount`
- `digitalStockBalance = 0.0`
- `laws = new Laws()`
- `bonusForMiner = 0.0`
- `voteEnum = VoteEnum.YES`

It can also return transaction-format-neutral `JackpotPayoutCandidate` views derived from the same built transactions.

Transaction format remains unchanged.

## Payout Rules

The builder supports all six V35 jackpot periods:
- `DAILY`
- `WEEKLY`
- `MONTHLY`
- `QUARTERLY`
- `HALF_YEAR`
- `YEARLY`

Canonical payout amounts:
- Daily: `302.40`
- Weekly: `1209.60`
- Monthly: `3888.00`
- Quarterly: `9720.00`
- Half-year: `15724.80`
- Yearly: `23652.00`

Rules enforced by the builder:
- If `forkIndex == Long.MAX_VALUE`, return no payouts.
- If payout block is before V35, return no payouts.
- Only exact-slot payouts are created.
- Missed payouts are not paid later in this first implementation.
- A payout is skipped if the proof cannot be built or verified.
- Immature periods are skipped.
- Periods with insufficient future hashes are skipped.
- Already-paid duplicate periods are skipped.
- Founder recipients are rejected through the proof service.
- Sender addresses are never used as jackpot participants.
- One payout per matured period type.
- Multiple different period types may be returned in the same payout block when they mature together.

## Proof Path

The builder reuses the existing pure jackpot proof path:
- `V35JackpotChainContextAdapter.buildPeriodInstance(...)`
- `V35JackpotChainContextAdapter.collectEligibleTickets(...)`
- `V35JackpotChainContextAdapter.collectFutureHashes(...)`
- `V35JackpotChainContextAdapter.hasPriorPayoutForPeriod(...)`
- `V35JackpotChainContextAdapter.verifyPayoutWithChainContext(...)`
- `V35JackpotProofService.verifyPayout(...)`

It creates a provisional in-memory `Block` only to let the proof service validate the candidate payout shape. This does not change block format and is not connected to mining or persistence.

## Exact-Slot Policy

For Phase 4E-1A, payouts are only built at:

`firstPayoutBlock = periodEnd + JACKPOT_RANDOM_DELAY_BLOCKS + 1`

With `JACKPOT_RANDOM_DELAY_BLOCKS = 60`, the builder refuses payout attempts one or more blocks after the exact payout slot. Late/missed payout support is intentionally deferred because it needs additional consensus design.

## Duplicate Prevention

The builder uses `V35JackpotChainContextAdapter.hasPriorPayoutForPeriod(...)` and proof-service duplicate markers. If a canonical chain context already contains a payout marker for the same period/type, the builder returns no payout for that period.

Same-block duplicate creation is avoided by iterating each supported period exactly once.

## Tests Added

Wallet:
- `V35JackpotPayoutBuilderTest`

Server:
- `V35JackpotPayoutBuilderTest`

Test coverage includes:
- Disabled fork returns no payouts with `Seting.V35_RESCUE_FORK_INDEX = Long.MAX_VALUE`.
- Exact daily payout slot builds a valid `DtoTransaction`.
- Built payout recipient matches deterministic winner miner address.
- Built payout amount matches canonical period amount.
- Built payout stock/silver and miner bonus are `0.0`.
- Built payout is accepted by `V35JackpotChainContextAdapter.verifyPayoutWithChainContext(...)` when a simulated fork index is used.
- Immature period returns no payout.
- Insufficient future hashes return no payout.
- Duplicate prior payout returns no payout.
- Founder winner/recipient path returns no payout.
- No eligible tickets returns no payout.
- Daily and weekly payouts can both be returned at the same exact payout block.
- Missed daily payout is not paid one block later.

## Verification Commands

Wallet:
- `C:\citu\unitedStates_final-master> .\mvnw.cmd test`
- `C:\citu\unitedStates_final-master> .\mvnw.cmd clean package`

Server:
- `C:\citu\unitedStates_storage-master> .\mvnw.cmd test`
- `C:\citu\unitedStates_storage-master> .\mvnw.cmd clean package`

## Verification Results

Wallet:
- `.\mvnw.cmd test`: passed, `121` tests, `0` failures, `0` errors
- `.\mvnw.cmd clean package`: passed, `121` tests, `0` failures, `0` errors

Server:
- `.\mvnw.cmd test`: passed, `133` tests, `0` failures, `0` errors
- `.\mvnw.cmd clean package`: passed, `133` tests, `0` failures, `0` errors

Build warnings remain pre-existing Maven model warnings about duplicate dependencies. No `pom.xml` files were changed.

## Behavior Confirmation

- V35 remains disabled in wallet and server.
- Wallet and server builder files match.
- Wallet and server builder test files match.
- Production mining behavior did not change.
- Production validation behavior did not change.
- Production balance behavior did not change.
- `Mining.java` was not changed.
- `UtilsBlock.java` was not changed.
- `UtilsBalance.java` was not changed.
- `DtoTransaction.java` was not changed.
- `Block.java` was not changed.
- Transaction and block formats remain unchanged.

## Risks Before Phase 4E-1B

1. Mining integration must sign builder-created jackpot payout transactions with the BASIS key before including them in a candidate block.
2. Mining must pass the correct canonical chain context before the candidate payout block.
3. Exact-slot-only payout policy means missed jackpot payouts are intentionally skipped; if catch-up payouts are desired, they need a separate consensus rule before activation.
4. The builder currently creates provisional blocks with deterministic placeholder metadata only for proof validation. Mining must use real candidate block metadata when wiring.
5. Multi-period payout ordering must be kept deterministic when Mining appends these transactions to the reward transaction set.
6. Existing V35 is still disabled, so simulated-fork tests cover future behavior while live paths remain inactive.

## Recommended Next Phase

Phase 4E-1B: wire `V35JackpotPayoutBuilder` into `Mining.java` behind the strict V35 fork gate, still disabled, so mining can append signed jackpot payout transactions only when the builder returns proven payouts.
