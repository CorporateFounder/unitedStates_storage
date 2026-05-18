# CITU V35 Phase 4D-2B UtilsBlock Jackpot Validation Report

Phase: 4D-2B  
Project root: `C:\citu`  
Scope: wire V35 BASIS payout classification into `UtilsBlock.validationOneBlock(...)` behind the disabled V35 fork gate. No jackpot payout creation.

## 1. Exact Files Changed

Wallet:

- `unitedStates_final-master/src/main/java/International_Trade_Union/utils/UtilsBlock.java`
- `unitedStates_final-master/src/test/java/International_Trade_Union/protocol/v35/V35RewardValidationFixtureTest.java`

Server:

- `unitedStates_storage-master/src/main/java/International_Trade_Union/utils/UtilsBlock.java`
- `unitedStates_storage-master/src/test/java/International_Trade_Union/protocol/v35/V35RewardValidationFixtureTest.java`

Report:

- `CITU_V35_PHASE4D2B_UTILSBLOCK_JACKPOT_VALIDATION_REPORT.md`

No `Mining.java`, `UtilsBalance.java`, `Block.java`, `Blockchain.java`, `DtoTransaction.java`, `Account.java`, controller, UI/template, website, transaction-format, block-format, hash-format, mining, or `pom.xml` changes were made.

## 2. Exact Methods Changed

Wallet and server:

- `International_Trade_Union.utils.UtilsBlock.validationOneBlock(...)`

Test fixture:

- `International_Trade_Union.protocol.v35.V35RewardValidationFixtureTest`

## 3. V35 Disabled Confirmation

Confirmed:

- wallet `Seting.V35_RESCUE_FORK_INDEX = Long.MAX_VALUE`
- server `Seting.V35_RESCUE_FORK_INDEX = Long.MAX_VALUE`

V35 remains disabled. No real fork index was set.

## 4. How BASIS Transactions Are Classified After V35

The V35-only `BASIS_ADDRESS` branch in `UtilsBlock.validationOneBlock(...)` now distinguishes:

1. Forbidden founder reward:
   - any `BASIS_ADDRESS -> addressFounder` transaction after V35 is rejected.

2. Instant miner reward:
   - recipient must equal `thisBlock.getMinerAddress()`
   - amount must pass `V35RewardCalculator.isValidInstantMinerReward(...)`
   - current expected shape is `6.00 GOLD` and `0.00` stock
   - exactly one instant miner reward is still required for normal V35 blocks

3. Jackpot payout candidate:
   - amount is classified using `JackpotCalculator.classifyPayoutAmount(...)`
   - the amount must map to one canonical `JackpotPeriod`
   - stock must be `0.00`
   - `bonusForMiner` must be `0.00`
   - at most one payout candidate per `JackpotPeriod` is permitted in a block

4. Invalid BASIS transaction:
   - non-canonical amount
   - jackpot candidate with stock
   - jackpot candidate with miner bonus
   - duplicate jackpot period candidate in the same block

`6.00 GOLD` is accepted only as the instant miner reward when paid to the current miner. `6.00` and `0.00` do not classify as jackpot payout amounts.

## 5. Jackpot Payout Validation Status

Fully enforced now in `UtilsBlock` after V35:

- founder reward is rejected
- instant miner reward must be exactly `6.00 GOLD` and `0.00` stock
- duplicate instant miner reward is rejected
- non-canonical BASIS amount is rejected
- canonical jackpot amounts are recognized as jackpot payout candidates
- jackpot payout candidates with stock are rejected
- jackpot payout candidates with nonzero `bonusForMiner` are rejected
- duplicate jackpot period candidates in the same block are rejected before proof validation would run

Not fully enforced yet:

- deterministic winner proof from eligible period blocks
- 60-block delayed future hash seed proof
- maturity verification against canonical chain ranges
- canonical-chain duplicate payout scan
- balance accounting for jackpot winner recipients

Because those require reliable chain context and/or balance-path support, canonical jackpot payout candidates are currently rejected with an explicit message:

`V35 jackpot payout proof validation requires canonical chain context`

This is intentional. The code now classifies the transaction safely but does not falsely accept a jackpot payout without proof context.

## 6. Chain Context Blockers

`UtilsBlock.validationOneBlock(...)` receives the current block, previous block, a `lastBlock` list, a `BlockService`, and balance/signature context, but this phase did not prove that the method can safely and consistently fetch:

- every eligible ticket block for the inferred jackpot period
- all 60 delayed future seed hashes
- prior canonical payouts for the same period/type
- earlier same-chain payout records across file/database boundaries

Until that is implemented and tested, full jackpot payout acceptance remains blocked.

## 7. Duplicate Prevention Status

Implemented now:

- same-block duplicate jackpot period candidate detection via an `EnumSet<JackpotCalculator.JackpotPeriod>`

Not implemented yet:

- canonical-chain duplicate scan for `(periodType, periodStart, periodEnd)`

Canonical-chain duplicate prevention is deferred to Phase 4D-2C or a dedicated chain-context helper phase.

## 8. Mining Confirmation

`Mining.java` was not changed.

Mining still does not create jackpot payout transactions. This phase only changes V35 validation classification behavior behind the disabled fork gate.

## 9. Old Behavior Confirmation

Before V35:

- legacy BASIS reward validation remains unchanged
- jackpot payout validation is inactive
- old blocks retain legacy validation behavior

Because `V35_RESCUE_FORK_INDEX = Long.MAX_VALUE`, current live behavior remains legacy.

## 10. Tests Added / Updated

Wallet and server `V35RewardValidationFixtureTest` now cover:

- fixed reward constants: `8.00` total, `6.00` instant miner, `2.00` jackpot, `0.00` founder
- founder reward transaction rejected under simulated V35 rules
- exact miner reward accepted under simulated V35 rules
- miner overpayment/underpayment/stock reward rejected
- missing or duplicate instant miner rewards rejected
- `6.00` to a non-miner rejected
- canonical daily jackpot payout candidate is classified as `DAILY`
- canonical jackpot payout candidate is rejected until proof context exists
- jackpot candidate with stock is rejected
- jackpot candidate with miner bonus is rejected
- duplicate jackpot period candidates are detectable before full proof validation
- normal indexes remain legacy while V35 is disabled

Existing `JackpotCalculatorTest` and `JackpotPayoutValidationTest` continue to cover canonical amount classification, disabled fork gate, wrong winner, wrong amount, immature proof, duplicate helper flag, founder payout rejection, and wallet/server parity.

## 11. Verification Commands Run

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

Generated `target` build output was restored after verification.

## 12. Test Results

Wallet `.\mvnw.cmd test`:

- PASS
- Tests run: `92`
- Failures: `0`
- Errors: `0`
- Skipped: `0`

Server `.\mvnw.cmd test`:

- PASS
- Tests run: `104`
- Failures: `0`
- Errors: `0`
- Skipped: `0`

## 13. Build Results

Wallet `.\mvnw.cmd clean package`:

- PASS
- Tests run during package: `92`
- Failures: `0`
- Errors: `0`
- Skipped: `0`

Server `.\mvnw.cmd clean package`:

- PASS
- Tests run during package: `104`
- Failures: `0`
- Errors: `0`
- Skipped: `0`

Existing Maven model warnings remain:

- duplicate `spring-boot-starter-test`
- duplicate/conflicting `h2`
- duplicate `spring-boot-starter-data-jpa`
- server duplicate `log4j:log4j`

No `pom.xml` files were changed.

## 14. Risks Before Phase 4D-2C

1. Jackpot payouts are still not fully accepted by `UtilsBlock`; they are classified then rejected until canonical proof context exists.
2. `UtilsBalance` still cannot account jackpot payouts to non-current-miner recipients. It must stay unchanged until validation acceptance is complete.
3. Canonical-chain duplicate scan is not implemented yet.
4. Full block-level tests remain limited because constructing signed full jackpot fixture blocks with canonical future hash context is still pending.
5. Mining still does not create jackpot payout transactions, by design.

## 15. Recommended Next Phase

Phase 4D-2C: add `UtilsBalance` jackpot payout accounting / canonical duplicate scan support if needed, or first introduce a shared chain-context proof helper that `UtilsBlock`, `UtilsBalance`, and later mining can all use consistently.
