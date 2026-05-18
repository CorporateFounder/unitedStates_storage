# CITU V35 Phase 4D Jackpot Payout Validation Report

## Scope

Phase 4D added pure V35 jackpot payout validation helpers and tests in both wallet and server projects. The helpers are behind a strict V35 activation gate and do not create jackpot payouts.

No mining, block validation, balance calculation, controllers, UI, website, block format, transaction format, signature format, or hash format was changed.

## Files Changed

Wallet:

- `unitedStates_final-master/src/main/java/International_Trade_Union/protocol/v35/JackpotCalculator.java`
- `unitedStates_final-master/src/test/java/International_Trade_Union/protocol/v35/JackpotPayoutValidationTest.java`
- `unitedStates_final-master/src/test/java/International_Trade_Union/protocol/v35/V35ParityTest.java`

Server:

- `unitedStates_storage-master/src/main/java/International_Trade_Union/protocol/v35/JackpotCalculator.java`
- `unitedStates_storage-master/src/test/java/International_Trade_Union/protocol/v35/JackpotPayoutValidationTest.java`
- `unitedStates_storage-master/src/test/java/International_Trade_Union/protocol/v35/V35ParityTest.java`

Report:

- `CITU_V35_PHASE4D_JACKPOT_PAYOUT_VALIDATION_REPORT.md`

## Files Inspected But Not Changed

Wallet and server:

- `UtilsBlock.java`
- `UtilsBalance.java`
- `Mining.java`
- `JackpotTicket.java`
- `JackpotWinnerProof.java`
- `V35RewardCalculator.java`
- `Seting.java`

## Methods Added

`International_Trade_Union.protocol.v35.JackpotCalculator`:

- `boolean isKnownJackpotPeriod(String period)`
- `long windowBlocksForPeriod(String period)`
- `double expectedPayoutAmount(String period, int eligibleTicketCount)`
- `boolean isV35JackpotPayoutAllowedAt(long payoutBlockIndex)`
- `boolean isValidJackpotPayout(long payoutBlockIndex, String period, double payoutAmount, String recipientAddress, String founderAddress, JackpotWinnerProof proof, int eligibleTicketCount, boolean alreadyPaid)`
- `boolean isValidJackpotPayout(boolean v35Active, long payoutBlockIndex, String period, double payoutAmount, String recipientAddress, String founderAddress, JackpotWinnerProof proof, int eligibleTicketCount, boolean alreadyPaid)`

`V35ParityTest` was updated to assert the wallet/server method surface matches.

## Validation Behavior Implemented

The helper accepts a jackpot payout only when all of these are true:

- V35 is active.
- The period name is one of: `daily`, `weekly`, `monthly`, `quarterly`, `half-year`, `yearly`.
- The payout proof exists and has a deterministic winner.
- The payout was not already paid.
- The recipient address is nonblank.
- The recipient is not the founder address.
- The payout block is at or after the proof maturity block.
- The recipient equals `proof.getWinnerMinerAddress()`.
- The payout amount equals `reservePerBlockForPeriod(period) * eligibleTicketCount`.

The normal overload uses the real fork gate:

- `Seting.V35_RESCUE_FORK_INDEX != Long.MAX_VALUE`
- `payoutBlockIndex >= Seting.V35_RESCUE_FORK_INDEX`

Because both projects still set `V35_RESCUE_FORK_INDEX = Long.MAX_VALUE`, real jackpot payout validation remains inactive.

The simulation overload accepts an explicit `v35Active` boolean so tests can prove the payout rules without activating V35.

## Fully Enforced Now

At helper level:

- valid jackpot proof accepted when V35 is simulated active
- payout before V35 rejected by the real gate
- wrong winner rejected
- wrong amount rejected
- immature payout rejected
- duplicate payout rejected when caller provides `alreadyPaid = true`
- sender-based/non-winner recipient rejected
- founder payout rejected
- invalid period rejected
- instant miner reward remains exactly `8.00` GOLD and `0.00` stock

## Helper-Level Only / Not Wired Yet

`UtilsBlock.validationOneBlock(...)` was not changed in this phase.

Reason: the existing transaction format has no jackpot period/type marker. A full block-level integration would have to infer jackpot period identity from only transaction amount, recipient, and surrounding chain context. That is ambiguous and unsafe to wire directly into consensus validation without a dedicated classification plan.

`UtilsBalance.calculateBalance(...)` and rollback balance logic were not changed.

Reason: balance code still assumes BASIS payouts can only go to founder or current miner. It should not be loosened until `UtilsBlock` can deterministically classify a jackpot payout and prevent duplicates on the canonical chain.

`Mining.java` was not changed.

Reason: Phase 4D explicitly forbids creating jackpot payout transactions. Mining still creates only the V35 instant miner reward in the disabled V35 branch.

## Duplicate Prevention Status

Implemented now:

- helper-level duplicate rejection via the `alreadyPaid` parameter

Not implemented yet:

- canonical-chain duplicate scan for `(periodType, periodStart, periodEnd)`
- transaction-level jackpot period/type classification
- balance-level duplicate awareness

Block-level duplicate prevention remains blocked until a future phase defines how `UtilsBlock` identifies the exact jackpot period represented by a BASIS payout transaction without changing transaction format.

## V35 Disabled Confirmation

Confirmed:

- `unitedStates_final-master/src/main/java/International_Trade_Union/setings/Seting.java`: `V35_RESCUE_FORK_INDEX = Long.MAX_VALUE`
- `unitedStates_storage-master/src/main/java/International_Trade_Union/setings/Seting.java`: `V35_RESCUE_FORK_INDEX = Long.MAX_VALUE`

## Mining Confirmation

Mining does not create jackpot payouts in Phase 4D.

No edits were made to:

- `unitedStates_final-master/src/main/java/International_Trade_Union/model/Mining.java`
- `unitedStates_storage-master/src/main/java/International_Trade_Union/model/Mining.java`

## Old Behavior Confirmation

Before V35:

- legacy BASIS reward validation remains unchanged
- jackpot payout validation is inactive
- old block validation behavior is unchanged
- transaction format is unchanged
- balance behavior is unchanged

## Tests Added / Updated

New wallet/server test:

- `JackpotPayoutValidationTest`

Coverage:

- valid proof accepted by helper when V35 is simulated active
- real disabled fork gate rejects payout while `V35_RESCUE_FORK_INDEX = Long.MAX_VALUE`
- wrong winner rejected
- wrong amount rejected
- immature period rejected
- duplicate payout rejected with `alreadyPaid = true`
- sender-based/non-winner recipient rejected
- founder payout rejected
- invalid period rejected
- miner instant reward remains `8.00` GOLD

Updated wallet/server test:

- `V35ParityTest`

Coverage:

- new payout validation helper methods exist in both projects with matching signatures

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

Cleanup:

```powershell
cd C:\citu
git restore unitedStates_final-master/target unitedStates_storage-master/target
```

## Test Results

Wallet `.\mvnw.cmd test`:

- PASS
- Tests run: 85
- Failures: 0
- Errors: 0
- Skipped: 0

Server `.\mvnw.cmd test`:

- PASS
- Tests run: 97
- Failures: 0
- Errors: 0
- Skipped: 0

## Build Results

Wallet `.\mvnw.cmd clean package`:

- PASS
- Tests run during package: 85
- Failures: 0
- Errors: 0
- Skipped: 0

Server `.\mvnw.cmd clean package`:

- PASS
- Tests run during package: 97
- Failures: 0
- Errors: 0
- Skipped: 0

Pre-existing Maven warnings remain:

- duplicate `spring-boot-starter-test`
- duplicate/conflicting `h2`
- duplicate `spring-boot-starter-data-jpa`
- server duplicate `log4j:log4j`

No `pom.xml` files were changed.

## Risks Before Phase 4E

1. Jackpot payout validation is helper-level only and not yet wired into `UtilsBlock`.
2. The existing transaction format has no jackpot period/type marker, so block-level classification must be designed carefully.
3. `UtilsBalance` still rejects BASIS recipients other than founder/current miner; it cannot safely accept jackpot winners until block validation can classify jackpot payouts.
4. A payout at the exact maturity block may be circular if the seed includes the payout block hash. Phase 4E should prefer payout only after the maturity seed block exists on-chain.
5. Full duplicate prevention requires canonical-chain scanning for prior payouts of the same period/type.
6. Mining still does not create payout transactions, by design.

## Recommended Next Phase

Phase 4E: mining/payout transaction creation behind the V35 fork gate, preceded by a short classification decision for how `UtilsBlock` will identify jackpot payout period/type without changing transaction format.
