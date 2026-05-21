# CITU V35 Phase 5C-1 Supply Accounting Helpers Report

Phase: 5C-1 only
Scope: pure V35 supply accounting helpers and tests
V35 activation: still disabled
Burn transaction: not created
API/UI/controller wiring: none
Transaction/block format changes: none

## 1. Exact files changed

Wallet:

- `unitedStates_final-master/src/main/java/International_Trade_Union/protocol/v35/V35SupplyCalculator.java`
- `unitedStates_final-master/src/main/java/International_Trade_Union/protocol/v35/V35SupplySnapshot.java`
- `unitedStates_final-master/src/test/java/International_Trade_Union/protocol/v35/V35SupplyCalculatorTest.java`
- `unitedStates_final-master/src/test/java/International_Trade_Union/protocol/v35/V35ParityTest.java`

Server:

- `unitedStates_storage-master/src/main/java/International_Trade_Union/protocol/v35/V35SupplyCalculator.java`
- `unitedStates_storage-master/src/main/java/International_Trade_Union/protocol/v35/V35SupplySnapshot.java`
- `unitedStates_storage-master/src/test/java/International_Trade_Union/protocol/v35/V35SupplyCalculatorTest.java`
- `unitedStates_storage-master/src/test/java/International_Trade_Union/protocol/v35/V35ParityTest.java`

Report:

- `CITU_V35_PHASE5C1_SUPPLY_ACCOUNTING_HELPERS_REPORT.md`

## 2. Helper/value classes added

Added `V35SupplyCalculator` in wallet and server.

Purpose:

- pure supply accounting helper
- no database access
- no controller access
- no network access
- no balance mutation
- no validation behavior change
- no mining behavior change

Added `V35SupplySnapshot` in wallet and server.

Snapshot fields:

- `totalMintedGold`
- `burnedGold`
- `circulatingGold`
- `founderReserveGold`
- `jackpotPaidGold`
- `annualEmissionGold`
- `inflationBeforeBurnPercent`
- `inflationAfterBurnPercent`
- `burnAddressConfigured`
- `valid`
- `invalidReason`

## 3. Exact helper methods added

`V35SupplyCalculator`:

- `annualEmissionGold()`
- `annualMinerEmissionGold()`
- `annualJackpotEmissionGold()`
- `circulatingGold(BigDecimal totalMintedGold, BigDecimal burnedGold)`
- `inflationPercent(BigDecimal annualEmissionGold, BigDecimal supplyGold)`
- `snapshot(BigDecimal totalMintedGold, BigDecimal burnedGold, BigDecimal founderReserveGold, BigDecimal jackpotPaidGold)`
- `snapshot(BigDecimal totalMintedGold, BigDecimal burnedGold, BigDecimal founderReserveGold, BigDecimal jackpotPaidGold, boolean burnAddressConfigured)`
- `snapshotFromBalances(BigDecimal totalMintedGold, Map<String, Account> balances, BigDecimal jackpotPaidGold)`
- `snapshotFromBalances(BigDecimal totalMintedGold, Map<String, Account> balances, BigDecimal jackpotPaidGold, String burnAddress, String founderAddress)`
- `totalMintedFromTransactions(List<DtoTransaction> transactions)`
- `jackpotPaidFromTransactions(List<DtoTransaction> transactions)`
- `accountGoldBalance(Map<String, Account> balances, String address)`
- `gold(double value)`
- `gold(String value)`

`V35SupplySnapshot`:

- getters for every immutable snapshot field

## 4. Supply formulas

Implemented formulas:

- `totalMintedGold`: supplied explicitly or summed from `BASIS_ADDRESS` GOLD outputs in an explicit transaction list.
- `burnedGold`: net GOLD balance of the configured burn address in an explicit account map.
- `circulatingGold = totalMintedGold - burnedGold`.
- `founderReserveGold`: GOLD balance of the founder address in an explicit account map.
- `jackpotPaidGold`: sum of canonical jackpot BASIS payout amounts already present in an explicit transaction list.
- `annualEmissionGold = 432 * 365 * 8.00 = 1,261,440.00 GOLD`.
- `annualMinerEmissionGold = 432 * 365 * 6.00 = 946,080.00 GOLD`.
- `annualJackpotEmissionGold = 432 * 365 * 2.00 = 315,360.00 GOLD`.
- inflation percent = `annualEmissionGold / supplyGold * 100`.

## 5. Precision model

The helpers use `BigDecimal` for supply values.

Rules:

- GOLD values are normalized to 2 decimal places.
- Percent values are normalized to 4 decimal places.
- Negative or non-finite numeric inputs are treated as invalid/safe-zero inputs.
- If burned supply exceeds total minted, the snapshot is marked invalid and circulating supply is clamped to `0.00`.
- Helper methods do not use unsafe direct double equality for supply decisions.

## 6. Burned supply calculation

For explicit input snapshots:

- burned supply is the supplied `burnedGold`.

For account-map snapshots:

- if the burn address is configured and not the TODO placeholder, burned supply is the burn address GOLD balance.
- if the burn address is still `TODO_V35_BURN_ADDRESS`, burned supply is `0.00` and `burnAddressConfigured=false`.

This prevents the TODO placeholder from silently becoming a real burn account in supply reports.

## 7. Circulating supply calculation

Circulating supply is:

`totalMintedGold - burnedGold`

The helper keeps founder reserve separate. It does not subtract founder reserve from circulating supply. A future dashboard may add a separate public-float value, but that is not wired in this phase.

## 8. Founder reserve calculation

For account-map snapshots:

- founder reserve is the current GOLD balance of the supplied founder address.
- the default helper uses `Seting.ADDRESS_FOUNDER`.

No founder balance is changed. No founder burn transaction was created.

## 9. Jackpot paid/emission handling

`jackpotPaidFromTransactions(...)` counts only transaction-list entries where:

- sender is `Seting.BASIS_ADDRESS`
- amount is one of the canonical jackpot payout amounts recognized by `JackpotCalculator`

Virtual jackpot reserve is not counted as minted supply until an actual payout transaction exists.

Annual jackpot emission is reported separately as policy emission:

- `315,360.00 GOLD/year`

This avoids double-counting the virtual reserve and jackpot payout transaction.

## 10. Tests added/updated

Added mirrored wallet/server `V35SupplyCalculatorTest`.

Covered:

- V35 remains disabled with `V35_RESCUE_FORK_INDEX = Long.MAX_VALUE`.
- basic supply snapshot values.
- burned supply from account map.
- founder reserve from account map.
- placeholder burn address does not become burned supply.
- modeled pre-burn supply of `159.55M GOLD`.
- modeled 80% founder burn of `60.824M GOLD`.
- modeled post-burn circulating supply of `98.726M GOLD`.
- modeled founder reserve of `15.206M GOLD`.
- inflation before burn is about `0.79%`.
- inflation after 80% founder burn is about `1.28%`.
- annual emission is `1,261,440.00 GOLD`.
- annual miner emission is `946,080.00 GOLD`.
- annual jackpot emission is `315,360.00 GOLD`.
- burned supply greater than total minted is invalid.
- negative inputs are invalid.
- total minted scans only `BASIS_ADDRESS` outputs.
- ordinary transfers do not increase total minted.
- jackpot paid scans only canonical jackpot payout amounts.
- virtual jackpot reserve is not counted until a payout transaction exists.

Updated wallet/server `V35ParityTest` to include:

- `V35SupplyCalculator`
- `V35SupplySnapshot`
- expected supply helper methods

## 11. Verification commands run

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

## 12. Test/build results

Wallet:

- `.\mvnw.cmd test`: PASS
- `.\mvnw.cmd clean package`: PASS

Server:

- `.\mvnw.cmd test`: PASS
- `.\mvnw.cmd clean package`: PASS

## 13. Behavior confirmation

- V35 remains disabled.
- No burn transaction was created.
- No API/controller wiring was added.
- No UI/template wiring was added.
- No website wiring was added.
- No mining behavior changed.
- No validation behavior changed.
- No live balance behavior changed.
- Transaction format unchanged.
- Block format unchanged.
- `Mining.java` was not changed.
- `UtilsBlock.java` was not changed.
- `pom.xml` was not changed.

## 14. Remaining risks before Phase 5C-2

1. `V35_BURN_ADDRESS` is still the placeholder `TODO_V35_BURN_ADDRESS`.
2. Helpers operate on explicit account maps and transaction lists; they are not yet connected to canonical chain/database data.
3. `totalMintedFromTransactions(...)` is a pure list helper, not a full historical chain scanner.
4. `jackpotPaidFromTransactions(...)` classifies jackpot payouts by canonical amount, but full proof-aware jackpot validation remains outside this helper.
5. Existing API/controller total endpoints still expose balance sums, not the new V35 supply snapshot.
6. Burn address staking/stock edge cases remain reporting-design risks for Phase 5C-2.
7. Founder balance must still be confirmed from canonical data before public burn numbers are finalized.

## 15. Recommended next phase

Phase 5C-2 - connect supply helpers to real chain/account data or produce an API/storage wiring plan.

Recommended scope:

- inspect canonical chain/account data sources,
- decide whether supply snapshots come from full block scan, account map, database query, or combined audit mode,
- keep API/UI wiring out unless explicitly requested,
- preserve V35 disabled state.
