# CITU V35 Phase 4D-2E Jackpot Chain Context Adapter Report

Phase: 4D-2E  
Project root: `C:\citu`  
Scope: pure V35 jackpot chain-context adapter and tests only. No production validation wiring.

## Exact Files Changed

Wallet:

- `unitedStates_final-master/src/main/java/International_Trade_Union/protocol/v35/V35JackpotChainContextAdapter.java`
- `unitedStates_final-master/src/test/java/International_Trade_Union/protocol/v35/V35JackpotChainContextAdapterTest.java`

Server:

- `unitedStates_storage-master/src/main/java/International_Trade_Union/protocol/v35/V35JackpotChainContextAdapter.java`
- `unitedStates_storage-master/src/test/java/International_Trade_Union/protocol/v35/V35JackpotChainContextAdapterTest.java`

Report:

- `CITU_V35_PHASE4D2E_JACKPOT_CHAIN_CONTEXT_ADAPTER_REPORT.md`

No changes were made to:

- `UtilsBlock.java`
- `UtilsBalance.java`
- `Mining.java`
- `Block.java`
- `Blockchain.java`
- `DtoTransaction.java`
- `Account.java`
- controllers
- UI/templates
- website
- `pom.xml`

## Helper / Adapter Class Added

Package:

- `International_Trade_Union.protocol.v35`

Class:

- `V35JackpotChainContextAdapter`

Purpose:

- Convert an explicit canonical `List<Block>` into inputs for `V35JackpotProofService`.
- Keep chain-context preparation pure and testable.
- Avoid database, network, controller, mining, validation, and balance dependencies.

## Adapter Methods Added

- `buildPayoutCandidate(Block payoutBlock, DtoTransaction payoutTransaction)`
- `buildPeriodInstance(JackpotPayoutCandidate candidate, long forkIndex)`
- `buildPeriodInstance(JackpotCalculator.JackpotPeriod periodType, long payoutBlockIndex, long forkIndex)`
- `collectEligibleTickets(List<Block> canonicalChain, JackpotPeriodInstance periodInstance, long forkIndex)`
- `collectFutureHashes(List<Block> canonicalChain, JackpotPeriodInstance periodInstance)`
- `collectPriorPayoutMarkers(List<Block> canonicalChain, long payoutBlockIndex, long forkIndex)`
- `hasPriorPayoutForPeriod(List<Block> canonicalChain, JackpotPeriodInstance periodInstance, long payoutBlockIndex, long forkIndex)`
- `verifyPayoutWithChainContext(List<Block> canonicalChain, Block payoutBlock, DtoTransaction payoutTransaction, String founderAddress)`
- `verifyPayoutWithChainContext(List<Block> canonicalChain, Block payoutBlock, DtoTransaction payoutTransaction, String founderAddress, long forkIndex)`

## Eligible Ticket Extraction

`collectEligibleTickets(...)` delegates to the pure proof service ticket extraction.

Rules:

- Only blocks at or after the supplied V35 fork index are eligible.
- Only `Block.getMinerAddress()` is used for eligibility.
- Sender/customer addresses from transactions are never used.
- Blank miner addresses invalidate the period ticket set.
- Missing period blocks return an empty ticket list.
- Returned tickets are sorted by block index through `JackpotCalculator.eligibleTickets(...)`.

## Future Hash Extraction

`collectFutureHashes(...)` delegates to the pure proof service seed hash extraction.

Rules:

- Uses the deterministic range after period end:
  - `periodEnd + 1`
  - through `periodEnd + 60`
- Requires 60 hashes.
- Missing blocks return an empty list.
- Blank hashes return an empty list.
- Ordering follows increasing block index.

## Duplicate Payout Detection

`collectPriorPayoutMarkers(...)` scans the supplied canonical chain and builds jackpot payout markers by calling:

- `V35JackpotProofService.markersForPayoutBlock(...)`

Duplicate marker format remains:

```text
V35-JACKPOT:<periodType>:<periodStart>:<periodEnd>
```

Rules:

- Canonical jackpot payout amounts are used to infer period type.
- Period instance is inferred from block index and fixed exact-slot timing.
- Same period/type marker blocks a candidate.
- Different period/type marker does not block.
- If the caller includes an already-paid payout block in the canonical list, the adapter detects the duplicate marker.

Important note:

- With exact-slot payout timing, a true same-period duplicate normally has the same payout block index. This adapter treats a supplied canonical block at that payout index as already-paid context and rejects a second candidate for the same marker.

## Proof Service Call

`verifyPayoutWithChainContext(...)` builds:

- `JackpotPayoutCandidate`
- `JackpotPeriodInstance`
- previous payout marker set
- `alreadyPaid` boolean

Then it calls:

- `V35JackpotProofService.verifyPayout(...)`

The production-facing overload uses:

- `Seting.V35_RESCUE_FORK_INDEX`

Since V35 remains disabled, that overload rejects jackpot payout validation as inactive.

## Transaction / Block Format Confirmation

No transaction format changes were made.

The adapter uses existing fields only:

- `Block.getIndex()`
- `Block.getHashBlock()`
- `Block.getMinerAddress()`
- `Block.getDtoTransactions()`
- `DtoTransaction.getSender()`
- `DtoTransaction.getCustomer()`
- `DtoTransaction.getDigitalDollar()`
- `DtoTransaction.getDigitalStockBalance()`
- `DtoTransaction.getBonusForMiner()`

No block format changes were made.

## V35 Disabled Confirmation

Confirmed:

- wallet `Seting.V35_RESCUE_FORK_INDEX = Long.MAX_VALUE`
- server `Seting.V35_RESCUE_FORK_INDEX = Long.MAX_VALUE`

No real fork index was set. V35 remains disabled.

## No Production Wiring Confirmation

The adapter is not called by:

- `UtilsBlock`
- `UtilsBalance`
- `Mining`
- controllers
- API
- UI/templates
- website

Therefore production validation, mining, balance accounting, transaction format, block format, and legacy behavior are unchanged.

## Tests Added

Added in both wallet and server:

- `V35JackpotChainContextAdapterTest`

Coverage:

- V35 mined blocks with miner address become tickets.
- Pre-V35 blocks do not become tickets.
- Blank miner address invalidates the period ticket set.
- Tickets are sorted by block index.
- 60 future hashes are collected deterministically.
- Insufficient future hashes return empty/invalid context.
- Blank future hash returns empty/invalid context.
- No previous payout means no duplicate marker.
- Same period/type previous payout marker is detected.
- Different period/type marker does not block.
- Valid daily chain-context payout proof passes with simulated V35 fork index.
- Wrong recipient rejected.
- Immature payout rejected.
- Duplicate payout rejected.
- Wrong amount rejected.
- Founder recipient rejected.
- Sender-based winner rejected.
- Candidate and period instance are built correctly.
- Production-facing overload remains inactive while `V35_RESCUE_FORK_INDEX = Long.MAX_VALUE`.

## Verification Commands Run

Wallet:

```powershell
cd C:\citu\unitedStates_final-master
.\mvnw.cmd test
.\mvnw.cmd clean package
```

Server:

```powershell
cd C:\citu\unitedStates_storage-master
.\mvnw.cmd test
.\mvnw.cmd clean package
```

Cleanup after build artifact generation:

```powershell
cd C:\citu
git restore unitedStates_final-master/target unitedStates_storage-master/target
```

## Test Results

Wallet `.\mvnw.cmd test`:

- PASS
- Tests run: 108
- Failures: 0
- Errors: 0
- Skipped: 0

Server `.\mvnw.cmd test`:

- PASS
- Tests run: 120
- Failures: 0
- Errors: 0
- Skipped: 0

## Build Results

Wallet `.\mvnw.cmd clean package`:

- PASS
- Tests run during package: 108
- Failures: 0
- Errors: 0
- Skipped: 0

Server `.\mvnw.cmd clean package`:

- PASS
- Tests run during package: 120
- Failures: 0
- Errors: 0
- Skipped: 0

Existing Maven warnings remain:

- duplicate `spring-boot-starter-test`
- duplicate/conflicting `h2`
- duplicate `spring-boot-starter-data-jpa`
- server duplicate `log4j:log4j`

No `pom.xml` files were changed.

## Blockers

No `Block` or `DtoTransaction` field blockers were found for a pure adapter.

Remaining integration blocker:

- `UtilsBlock.validationOneBlock(...)` still does not receive a guaranteed complete canonical chain context. Production wiring must happen only in a validation path that can supply the complete period range, seed range, and prior payout marker context.

## Risks Before Phase 4D-2F

1. The adapter is pure and not wired into `UtilsBlock`; current production validation still rejects jackpot payouts that need chain-context proof.
2. `UtilsBalance` still cannot account jackpot payouts to historical winner miners.
3. Mining still does not create jackpot payout transactions.
4. Exact-slot payout timing remains strict. Late payout support would need a separate deterministic rule or transaction metadata discussion.
5. Duplicate detection depends on the caller providing the correct canonical chain context.

## Recommended Next Phase

Phase 4D-2F: wire `V35JackpotChainContextAdapter` into `UtilsBlock` only where complete canonical chain context is available, while keeping V35 disabled and leaving `UtilsBalance` and `Mining` untouched.

