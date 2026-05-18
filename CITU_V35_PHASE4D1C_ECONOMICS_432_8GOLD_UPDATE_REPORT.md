# CITU V35 Phase 4D-1C Economics 432 / 8 GOLD Update Report

Phase: 4D-1C  
Project root: `C:\citu`  
Scope: V35 constants and pure-helper test correction only. V35 remains disabled.

## 1. Exact Files Changed

Wallet:

- `unitedStates_final-master/src/main/java/International_Trade_Union/setings/Seting.java`
- `unitedStates_final-master/src/test/java/International_Trade_Union/protocol/v35/V35RewardCalculatorTest.java`
- `unitedStates_final-master/src/test/java/International_Trade_Union/protocol/v35/V35RewardValidationFixtureTest.java`
- `unitedStates_final-master/src/test/java/International_Trade_Union/protocol/v35/JackpotCalculatorTest.java`
- `unitedStates_final-master/src/test/java/International_Trade_Union/protocol/v35/JackpotPayoutValidationTest.java`
- `unitedStates_final-master/src/test/java/International_Trade_Union/protocol/v35/JackpotWinnerSelectionTest.java`
- `unitedStates_final-master/src/test/java/International_Trade_Union/protocol/v35/V35TargetDifficultyTest.java`

Server:

- `unitedStates_storage-master/src/main/java/International_Trade_Union/setings/Seting.java`
- `unitedStates_storage-master/src/test/java/International_Trade_Union/protocol/v35/V35RewardCalculatorTest.java`
- `unitedStates_storage-master/src/test/java/International_Trade_Union/protocol/v35/V35RewardValidationFixtureTest.java`
- `unitedStates_storage-master/src/test/java/International_Trade_Union/protocol/v35/JackpotCalculatorTest.java`
- `unitedStates_storage-master/src/test/java/International_Trade_Union/protocol/v35/JackpotPayoutValidationTest.java`
- `unitedStates_storage-master/src/test/java/International_Trade_Union/protocol/v35/JackpotWinnerSelectionTest.java`
- `unitedStates_storage-master/src/test/java/International_Trade_Union/protocol/v35/V35TargetDifficultyTest.java`

Report:

- `CITU_V35_PHASE4D1C_ECONOMICS_432_8GOLD_UPDATE_REPORT.md`

No mining, validation, balance, DTO, block, controller, UI, website, transaction-format, block-format, or `pom.xml` files were changed.

## 2. Old Model

The previous V35 helper/report model was:

- `150` seconds per block
- `576` blocks per day
- `10.00 GOLD` total reward per block
- `8.00 GOLD` instant miner reward
- `2.00 GOLD` jackpot reserve
- founder reward disabled and expected founder reward `0.00`

## 3. New Model

The corrected canonical V35 model is now:

- `200` seconds per block
- `432` blocks per day
- `8.00 GOLD` total reward per block
- `6.00 GOLD` instant miner reward
- `2.00 GOLD` jackpot reserve
- founder reward disabled
- expected founder reward `0.00`

Updated constants in wallet and server:

- `V35_GOLD_REWARD = 8.00`
- `V35_INSTANT_MINER_REWARD = 6.00`
- `V35_JACKPOT_REWARD = 2.00`
- `V35_TARGET_BLOCK_TIME_SECONDS = 200`
- `V35_FOUNDER_REWARD_ENABLED = false`
- `V35_EXPECTED_FOUNDER_REWARD = 0.00`

Unchanged jackpot split:

- `V35_DAILY_JACKPOT_PER_BLOCK = 0.70`
- `V35_WEEKLY_JACKPOT_PER_BLOCK = 0.40`
- `V35_MONTHLY_JACKPOT_PER_BLOCK = 0.30`
- `V35_QUARTERLY_JACKPOT_PER_BLOCK = 0.25`
- `V35_HALF_YEAR_JACKPOT_PER_BLOCK = 0.20`
- `V35_YEARLY_JACKPOT_PER_BLOCK = 0.15`

The jackpot split remains exactly `2.00 GOLD` per block.

## 4. Annual Emission

Blocks per year:

- `432 * 365 = 157,680` blocks/year

Annual V35 GOLD emission:

- `157,680 * 8.00 = 1,261,440 GOLD/year`

Annual instant miner emission:

- `157,680 * 6.00 = 946,080 GOLD/year`

Annual jackpot reserve:

- `157,680 * 2.00 = 315,360 GOLD/year`

## 5. Inflation Estimate

Against `159.55M` supply:

- `1,261,440 / 159,550,000 = 0.007906`
- approximately `0.79%` annual emission

Against estimated post-80%-founder-burn supply around `98.73M`:

- `1,261,440 / 98,730,000 = 0.012777`
- approximately `1.28%` annual emission

These are emission estimates only; they do not activate V35 and do not account for future burns or lost coins.

## 6. Updated Jackpot Windows

Because `V35_TARGET_BLOCK_TIME_SECONDS = 200`, `86400 / 200 = 432` blocks per day.

Updated fixed block windows:

| Period | Blocks |
|---|---:|
| Daily | 432 |
| Weekly | 3,024 |
| Monthly | 12,960 |
| Quarterly | 38,880 |
| Half-year | 78,624 |
| Yearly | 157,680 |

`JackpotCalculator` already derives `BLOCKS_PER_DAY` from `Seting.V35_TARGET_BLOCK_TIME_SECONDS`, so no source change was needed in `JackpotCalculator.java`; tests were updated to lock the new derived values.

## 7. Updated Jackpot Payout Amounts

Using the unchanged jackpot split and the corrected windows:

| Period | Formula | Payout |
|---|---:|---:|
| Daily | `432 * 0.70` | `302.40 GOLD` |
| Weekly | `3,024 * 0.40` | `1,209.60 GOLD` |
| Monthly | `12,960 * 0.30` | `3,888.00 GOLD` |
| Quarterly | `38,880 * 0.25` | `9,720.00 GOLD` |
| Half-year | `78,624 * 0.20` | `15,724.80 GOLD` |
| Yearly | `157,680 * 0.15` | `23,652.00 GOLD` |

`JackpotCalculatorTest` now explicitly asserts these full-window payout amounts in both wallet and server projects.

## 8. V35 Disabled Confirmation

Wallet:

- `unitedStates_final-master/src/main/java/International_Trade_Union/setings/Seting.java`
- `V35_RESCUE_FORK_INDEX = Long.MAX_VALUE`

Server:

- `unitedStates_storage-master/src/main/java/International_Trade_Union/setings/Seting.java`
- `V35_RESCUE_FORK_INDEX = Long.MAX_VALUE`

V35 remains disabled. No real fork index was set.

## 9. Behavior Change Confirmation

No live mining, validation, balance, or payout behavior changed in this phase because:

- V35 remains disabled behind `V35_RESCUE_FORK_INDEX = Long.MAX_VALUE`.
- `Mining.java` was not edited.
- `UtilsBlock.java` was not edited.
- `UtilsBalance.java` was not edited.
- `Block.java`, `Blockchain.java`, `DtoTransaction.java`, `Account.java`, controllers, UI, website, and `pom.xml` were not edited.
- No jackpot payout wiring was added.

The only production changes are V35 constants in `Seting.java`; all other source helpers read those constants.

## 10. Tests Updated

Updated wallet/server tests cover:

- total V35 reward is `8.00`
- instant miner reward is `6.00`
- jackpot reserve remains `2.00`
- founder reward remains `0.00`
- instant miner reward validation accepts `6.00` and rejects over/under/stock variants
- target block time is `200`
- target adjustment expected span uses `60 * 200`
- jackpot windows are `432`, `3024`, `12960`, `38880`, `78624`, `157680`
- jackpot period boundaries and maturity values follow the 432-block daily window
- full-window jackpot payouts are `302.40`, `1209.60`, `3888.00`, `9720.00`, `15724.80`, `23652.00`

`V35ParityTest` continues to pass; package and method signatures remain matched between wallet and server.

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

Generated `target` build outputs were restored after verification so they do not remain as source changes.

## 12. Test Results

Wallet `.\mvnw.cmd test`:

- PASS
- Tests run: `86`
- Failures: `0`
- Errors: `0`
- Skipped: `0`

Server `.\mvnw.cmd test`:

- PASS
- Tests run: `98`
- Failures: `0`
- Errors: `0`
- Skipped: `0`

## 13. Build Results

Wallet `.\mvnw.cmd clean package`:

- PASS
- Tests run during package: `86`
- Failures: `0`
- Errors: `0`
- Skipped: `0`

Server `.\mvnw.cmd clean package`:

- PASS
- Tests run during package: `98`
- Failures: `0`
- Errors: `0`
- Skipped: `0`

Existing Maven model warnings remain:

- duplicate `spring-boot-starter-test`
- duplicate/conflicting `h2`
- duplicate `spring-boot-starter-data-jpa`
- server duplicate `log4j:log4j`

No `pom.xml` files were changed.

## 14. Risks Before Phase 4D-2

1. Prior reports still document the old `150 sec / 576 blocks/day / 10 GOLD` model. This report supersedes those economics for future V35 phases.
2. Phase 2B/4D validation wiring currently uses `V35RewardCalculator`, so it will read the new `6.00` instant reward only if V35 is later activated. Because V35 is disabled, live behavior is unchanged.
3. Jackpot payout classification plans based on the old payout amounts must be updated in Phase 4D-2 to use the new amount table.
4. Future jackpot payout classification should use integer cents or exact scale-2 decimal handling to avoid `double` consensus drift.
5. Mining still does not create jackpot payouts; payout classification/wiring remains pending.

## 15. Recommended Next Phase

Phase 4D-2 should implement jackpot payout classification using the corrected `432 blocks/day`, `8 GOLD/block`, `6 GOLD instant`, and updated payout amount table, still behind the disabled V35 fork gate.
