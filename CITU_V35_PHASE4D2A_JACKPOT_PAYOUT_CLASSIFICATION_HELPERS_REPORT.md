# CITU V35 Phase 4D-2A Jackpot Payout Classification Helpers Report

Phase: 4D-2A  
Project root: `C:\citu`  
Scope: pure V35 jackpot payout amount classification helpers and tests only. No consensus wiring.

## 1. Exact Files Changed

Wallet:

- `unitedStates_final-master/src/main/java/International_Trade_Union/protocol/v35/JackpotCalculator.java`
- `unitedStates_final-master/src/test/java/International_Trade_Union/protocol/v35/JackpotCalculatorTest.java`
- `unitedStates_final-master/src/test/java/International_Trade_Union/protocol/v35/V35ParityTest.java`

Server:

- `unitedStates_storage-master/src/main/java/International_Trade_Union/protocol/v35/JackpotCalculator.java`
- `unitedStates_storage-master/src/test/java/International_Trade_Union/protocol/v35/JackpotCalculatorTest.java`
- `unitedStates_storage-master/src/test/java/International_Trade_Union/protocol/v35/V35ParityTest.java`

Report:

- `CITU_V35_PHASE4D2A_JACKPOT_PAYOUT_CLASSIFICATION_HELPERS_REPORT.md`

No `UtilsBlock.java`, `UtilsBalance.java`, `Mining.java`, `Block.java`, `Blockchain.java`, `DtoTransaction.java`, `Account.java`, controller, UI/template, website, transaction-format, block-format, mining, validation, balance, or `pom.xml` changes were made.

## 2. Helper Methods And Types Added

Added to wallet/server `International_Trade_Union.protocol.v35.JackpotCalculator`:

- `enum JackpotPeriod`
  - `DAILY`
  - `WEEKLY`
  - `MONTHLY`
  - `QUARTERLY`
  - `HALF_YEAR`
  - `YEARLY`
  - `UNKNOWN`
- `BigDecimal expectedPayoutAmount(JackpotPeriod periodType)`
- `JackpotPeriod classifyPayoutAmount(double amount)`
- `boolean isCanonicalPayoutAmount(double amount)`
- `Map<JackpotPeriod, BigDecimal> allExpectedPayoutAmounts()`
- `boolean validatePayoutAmountsAreUnique()`

Updated wallet/server `V35ParityTest` to assert the new helper method surface exists in both projects.

## 3. Payout Amount Table

The helpers use the corrected 432 blocks/day economics from Phase 4D-1C:

| Period | Window | Per-block reserve | Canonical payout |
|---|---:|---:|---:|
| Daily | 432 | 0.70 | 302.40 GOLD |
| Weekly | 3,024 | 0.40 | 1,209.60 GOLD |
| Monthly | 12,960 | 0.30 | 3,888.00 GOLD |
| Quarterly | 38,880 | 0.25 | 9,720.00 GOLD |
| Half-year | 78,624 | 0.20 | 15,724.80 GOLD |
| Yearly | 157,680 | 0.15 | 23,652.00 GOLD |

The payout values are derived from existing jackpot window helpers and per-block split constants rather than duplicated as independent hard-coded amounts.

## 4. Precision And Rounding Decision

Classification does not use raw direct `double` equality.

Precision rule:

- Incoming `double` amounts are converted with `BigDecimal.valueOf(amount)`.
- The value must normalize exactly to scale 2 via `setScale(2, RoundingMode.UNNECESSARY)`.
- Values with fewer decimals are accepted if exactly representable at scale 2.
- `302.400` classifies as `302.40`.
- `302.41` does not classify as daily.
- `302.399` does not silently round and classifies as `UNKNOWN`.
- `NaN` and infinite values classify as `UNKNOWN`.

This is intentionally strict so payout classification cannot drift by accidental rounding.

## 5. Uniqueness And Miner Reward Collision

`validatePayoutAmountsAreUnique()` confirms:

- all six jackpot payout amounts are unique
- `6.00 GOLD` instant miner reward does not classify as a jackpot payout
- `0.00 GOLD` founder reward does not classify as a jackpot payout

Tests also directly cover:

- `6.00` => `UNKNOWN`
- `0.00` => `UNKNOWN`
- wrong amount => `UNKNOWN`
- unsafe decimal => `UNKNOWN`

## 6. V35 Disabled Confirmation

Confirmed in both projects:

- wallet `Seting.V35_RESCUE_FORK_INDEX = Long.MAX_VALUE`
- server `Seting.V35_RESCUE_FORK_INDEX = Long.MAX_VALUE`

No real fork index was set. V35 remains disabled.

## 7. No Behavior Wiring Confirmation

This phase added pure helper logic only.

No changes were made to:

- `UtilsBlock.java`
- `UtilsBalance.java`
- `Mining.java`
- controllers
- UI/templates
- website
- transaction format
- block format
- validation behavior
- mining behavior
- balance behavior

The new classifier is not called from legacy or V35 validation/mining paths yet.

## 8. Tests Added / Updated

Wallet and server `JackpotCalculatorTest` now verify:

- all six canonical payout amounts classify to the expected `JackpotPeriod`
- `302.400` safely normalizes to daily `302.40`
- `302.41` does not classify
- `302.399` does not silently round
- `6.00` instant miner reward does not classify as jackpot
- `0.00` founder reward does not classify as jackpot
- `NaN` and infinity do not classify
- `allExpectedPayoutAmounts()` contains six unique values
- `validatePayoutAmountsAreUnique()` returns true
- reserve split remains `2.00`

Wallet and server `V35ParityTest` now verify:

- `expectedPayoutAmount(JackpotPeriod)`
- `classifyPayoutAmount(double)`
- `isCanonicalPayoutAmount(double)`
- `allExpectedPayoutAmounts()`
- `validatePayoutAmountsAreUnique()`

## 9. Verification Commands Run

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

## 10. Test Results

Wallet `.\mvnw.cmd test`:

- PASS
- Tests run: `89`
- Failures: `0`
- Errors: `0`
- Skipped: `0`

Server `.\mvnw.cmd test`:

- PASS
- Tests run: `101`
- Failures: `0`
- Errors: `0`
- Skipped: `0`

## 11. Build Results

Wallet `.\mvnw.cmd clean package`:

- PASS
- Tests run during package: `89`
- Failures: `0`
- Errors: `0`
- Skipped: `0`

Server `.\mvnw.cmd clean package`:

- PASS
- Tests run during package: `101`
- Failures: `0`
- Errors: `0`
- Skipped: `0`

Existing Maven model warnings remain:

- duplicate `spring-boot-starter-test`
- duplicate/conflicting `h2`
- duplicate `spring-boot-starter-data-jpa`
- server duplicate `log4j:log4j`

No `pom.xml` files were changed.

## 12. Risks Before Phase 4D-2B

1. Classification is amount-only at this helper layer. `UtilsBlock` wiring must still validate maturity, deterministic winner, recipient, duplicate period, founder rejection, and V35 fork gate.
2. The current transaction format still has no jackpot period/type marker; Phase 4D-2B must treat amount classification as a first filter, not complete authorization.
3. Full duplicate prevention still requires canonical-chain scanning for the same period/type.
4. `UtilsBalance` still does not recognize jackpot winners; this should remain unchanged until validation can classify payouts safely.
5. Future changes to jackpot windows or reserve splits must update this classifier and tests together.

## 13. Recommended Next Phase

Phase 4D-2B: wire jackpot payout classification into `UtilsBlock.validationOneBlock(...)` behind the strict V35 fork gate, still without changing mining payout creation.
