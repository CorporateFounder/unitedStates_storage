# CITU V35 Phase 4D-2D Jackpot Proof Service Report

Phase: 4D-2D  
Project root: `C:\citu`  
Scope: pure V35 jackpot chain-context proof service and tests only. No production wiring.

## Exact Files Changed

Wallet:

- `unitedStates_final-master/src/main/java/International_Trade_Union/protocol/v35/JackpotPayoutCandidate.java`
- `unitedStates_final-master/src/main/java/International_Trade_Union/protocol/v35/JackpotPeriodInstance.java`
- `unitedStates_final-master/src/main/java/International_Trade_Union/protocol/v35/JackpotProofResult.java`
- `unitedStates_final-master/src/main/java/International_Trade_Union/protocol/v35/V35JackpotProofService.java`
- `unitedStates_final-master/src/test/java/International_Trade_Union/protocol/v35/V35JackpotProofServiceTest.java`

Server:

- `unitedStates_storage-master/src/main/java/International_Trade_Union/protocol/v35/JackpotPayoutCandidate.java`
- `unitedStates_storage-master/src/main/java/International_Trade_Union/protocol/v35/JackpotPeriodInstance.java`
- `unitedStates_storage-master/src/main/java/International_Trade_Union/protocol/v35/JackpotProofResult.java`
- `unitedStates_storage-master/src/main/java/International_Trade_Union/protocol/v35/V35JackpotProofService.java`
- `unitedStates_storage-master/src/test/java/International_Trade_Union/protocol/v35/V35JackpotProofServiceTest.java`

Report:

- `CITU_V35_PHASE4D2D_JACKPOT_PROOF_SERVICE_REPORT.md`

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

## Helper / Service Classes Added

Package:

- `International_Trade_Union.protocol.v35`

New classes:

- `V35JackpotProofService`
- `JackpotPayoutCandidate`
- `JackpotPeriodInstance`
- `JackpotProofResult`

The wallet and server classes are intentionally mirrored by name, package, method surface, and behavior.

## Proof Validation Inputs

`V35JackpotProofService.verifyPayout(...)` accepts:

- canonical block list
- payout block
- payout `DtoTransaction`
- founder address
- previous payout markers
- `alreadyPaid` boolean
- explicit fork index for pure/simulated tests

The production-facing overload uses:

- `Seting.V35_RESCUE_FORK_INDEX`

Because this constant is still `Long.MAX_VALUE`, the production-facing overload always rejects V35 jackpot payouts as inactive.

## Proof Validation Rules

The service validates:

- V35 is active for the payout block.
- Sender is `Seting.BASIS_ADDRESS`.
- Payout amount is canonical and maps to one jackpot period.
- Period type is inferred from amount, without transaction-format changes.
- `digitalStockBalance == 0.00`.
- `bonusForMiner == 0.00`.
- Recipient is nonblank.
- Recipient is not `Seting.BASIS_ADDRESS`.
- Recipient is not the founder.
- Period boundaries are fork-relative.
- Payout block is the deterministic first payout slot: `periodEnd + 60 + 1`.
- Period ticket blocks are complete and V35.
- Each eligible ticket uses `Block.getMinerAddress()`.
- The 60 future seed hashes are present and nonblank.
- Winner proof is deterministic through `JackpotCalculator.selectWinner(...)`.
- Recipient equals `JackpotWinnerProof.getWinnerMinerAddress()`.
- `alreadyPaid = true` rejects.
- A previous marker for the same period rejects.
- Same-period duplicate payout candidates in the payout block reject.
- Missing or invalid inputs return an invalid `JackpotProofResult`, not an exception.

## Duplicate Prevention Model

Pure helper-level duplicate prevention was added.

Duplicate key format:

```text
V35-JACKPOT:<periodType>:<periodStart>:<periodEnd>
```

Implemented now:

- `alreadyPaid = true` rejects.
- `previousPayoutMarkers` containing the candidate duplicate key rejects.
- Same-block duplicate payouts for the same period instance reject.
- Different period/type markers are not treated as duplicates.

Not wired yet:

- canonical-chain scanning in `UtilsBlock`
- balance-level duplicate awareness in `UtilsBalance`
- mining duplicate scan before payout creation

Those remain for later phases.

## Transaction Format Confirmation

Transaction format was not changed.

The service uses existing `DtoTransaction` fields only:

- `sender`
- `customer`
- `digitalDollar`
- `digitalStockBalance`
- `bonusForMiner`

Period type is inferred from the canonical payout amount table:

| Period | Amount |
|---|---:|
| Daily | 302.40 |
| Weekly | 1209.60 |
| Monthly | 3888.00 |
| Quarterly | 9720.00 |
| Half-year | 15724.80 |
| Yearly | 23652.00 |

The service relies on the strict two-decimal canonical amount classification already provided by `JackpotCalculator`.

## V35 Disabled Confirmation

Confirmed:

- wallet `Seting.V35_RESCUE_FORK_INDEX = Long.MAX_VALUE`
- server `Seting.V35_RESCUE_FORK_INDEX = Long.MAX_VALUE`

No real fork index was set. V35 remains disabled.

## No Production Wiring Confirmation

The new proof service is not called by:

- `UtilsBlock`
- `UtilsBalance`
- `Mining`
- controllers
- server sync/tournament code
- UI/templates
- website

Therefore production validation, mining, balance accounting, block format, transaction format, and legacy behavior are unchanged.

## Tests Added

Added in both wallet and server:

- `V35JackpotProofServiceTest`

Coverage:

- valid daily payout proof passes under simulated V35 fork index
- valid weekly payout proof passes under simulated V35 fork index
- wrong recipient rejected
- wrong amount rejected
- immature payout rejected
- insufficient future hashes rejected
- invalid future hash rejected
- incomplete/blank eligible ticket context rejected
- founder recipient rejected
- `alreadyPaid` duplicate rejected
- previous payout marker duplicate rejected
- same-period duplicate in payout block rejected
- sender-based address cannot become winner
- same inputs produce same proof result
- changed future hashes can change winner
- payout amount maps to exact period type
- period instance is calculated fork-relative
- payout before V35 rejected
- production-facing overload rejects while `V35_RESCUE_FORK_INDEX = Long.MAX_VALUE`

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
- Tests run: 100
- Failures: 0
- Errors: 0
- Skipped: 0

Server `.\mvnw.cmd test`:

- PASS
- Tests run: 112
- Failures: 0
- Errors: 0
- Skipped: 0

## Build Results

Wallet `.\mvnw.cmd clean package`:

- PASS
- Tests run during package: 100
- Failures: 0
- Errors: 0
- Skipped: 0

Server `.\mvnw.cmd clean package`:

- PASS
- Tests run during package: 112
- Failures: 0
- Errors: 0
- Skipped: 0

Existing Maven warnings remain:

- duplicate `spring-boot-starter-test`
- duplicate/conflicting `h2`
- duplicate `spring-boot-starter-data-jpa`
- server duplicate `log4j:log4j`

No `pom.xml` files were changed.

## Risks Before Phase 4D-2E

1. The proof service is pure and not wired into `UtilsBlock`; jackpot payout candidates are still rejected by current production validation.
2. `UtilsBlock.validationOneBlock(...)` still needs a canonical context path before it can safely call the service.
3. `UtilsBalance` still does not account jackpot payouts to non-current-miner recipients.
4. Mining still does not create jackpot payout transactions.
5. Exact-slot payout timing is strict. If late payouts are required later, transaction format or a more complex deterministic unpaid-period assignment rule must be revisited.
6. Previous payout marker scanning is helper-level only; canonical-chain scan integration is still pending.

## Recommended Next Phase

Phase 4D-2E: wire `V35JackpotProofService` into `UtilsBlock` only where complete canonical context is available, while keeping V35 disabled and leaving `UtilsBalance` and `Mining` untouched.

