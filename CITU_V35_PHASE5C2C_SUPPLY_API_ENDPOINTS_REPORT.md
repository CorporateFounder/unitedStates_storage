# CITU V35 Phase 5C-2C Supply API Endpoints Report

Phase: 5C-2C only  
Scope: server-first read-only V35 supply/protocol/burn/founder endpoints  
V35 activation: still disabled  
Burn transaction: not created  
UI/website wiring: none  
Transaction/block format changes: none

## 1. Exact Files Changed

Server source:
- `unitedStates_storage-master/src/main/java/International_Trade_Union/controllers/V35SupplyController.java`
- `unitedStates_storage-master/src/main/java/International_Trade_Union/protocol/v35/V35SupplyApiResponseBuilder.java`

Server tests:
- `unitedStates_storage-master/src/test/java/International_Trade_Union/controllers/V35SupplyControllerTest.java`

Report:
- `CITU_V35_PHASE5C2C_SUPPLY_API_ENDPOINTS_REPORT.md`

No wallet source files were changed.

## 2. Endpoints Added

Added a new server `@RestController`:

- `V35SupplyController`

Read-only endpoints:

- `GET /v35/supply`
- `GET /v35/protocol`
- `GET /v35/burn`
- `GET /v35/founder`

The controller does not mutate chain, blocks, balances, transactions, settings, or caches.

## 3. Response Fields

`/v35/supply` returns:
- `v35Active`
- `v35ForkIndex`
- `blockTimeSeconds`
- `blocksPerDay`
- `rewardPerBlockGold`
- `minerRewardGold`
- `jackpotReservePerBlockGold`
- `founderRewardGold`
- `burnAddress`
- `founderAddress`
- `isBurnAddressPlaceholder`
- `burnAddressConfigured`
- `totalMintedGold`
- `burnedGold`
- `circulatingGold`
- `founderReserveGold`
- `jackpotPaidGold`
- `annualEmissionGold`
- `valid`
- `invalidReason`
- `snapshotSource`

`/v35/protocol` returns:
- `v35Active`
- `v35ForkIndex`
- `blockTimeSeconds`
- `blocksPerDay`
- `rewardPerBlockGold`
- `minerRewardGold`
- `jackpotReservePerBlockGold`
- `founderRewardGold`
- `burnAddress`
- `founderAddress`
- `isBurnAddressPlaceholder`
- `burnAddressConfigured`
- `annualEmissionGold`

`/v35/burn` returns:
- `burnAddress`
- `isBurnAddressPlaceholder`
- `burnAddressConfigured`
- `burnedGold`
- `noSpendAfterV35`
- `burnTransactionCreated`
- `v35Active`
- `v35ForkIndex`
- `note`

`/v35/founder` returns:
- `founderAddress`
- `founderReserveGold`
- `founderRewardGold`
- `modeledBurnPercent`
- `modeledBurnGold`
- `actualBurnedGold`
- `modelOnly`
- `v35Active`
- `v35ForkIndex`
- `note`

Monetary values are emitted as string-safe 2-decimal values.

## 4. How Supply Snapshot Is Built

`V35SupplyController.supplyContext()` builds a read-only snapshot by:

1. Reading current chain height from `blockService.countBlock()`.
2. Reading canonical stored blocks with `blockService.findBySpecialIndexBetween(0, currentHeight)` when height is non-negative.
3. Converting entity blocks to domain blocks with `UtilsBlockToEntityBlock.entityBlocksToBlocks(...)`.
4. Reading stored accounts with `blockService.findAllAccounts()`.
5. Converting entity accounts to `Map<String, Account>` with `UtilsAccountToEntityAccount.entityAccountsToMapAccounts(...)`.
6. Calling `V35SupplyChainAdapter.snapshotFromChainAndAccounts(chain, accounts)`.

This means:
- `totalMintedGold` comes from canonical BASIS transactions in stored blocks.
- `jackpotPaidGold` comes from canonical jackpot BASIS payout amounts already present in stored blocks.
- `burnedGold` comes from burn-address account balance only when the burn address is configured.
- `founderReserveGold` comes from founder-address account balance.
- `circulatingGold = totalMintedGold - burnedGold`.

The existing `/totalDollars` style account aggregate endpoints are not used as supply totals.

## 5. Placeholder Burn Address Reporting

Current value remains:

`Seting.V35_BURN_ADDRESS = "TODO_V35_BURN_ADDRESS"`

The endpoints report:

- `isBurnAddressPlaceholder = true`
- `burnAddressConfigured = false`
- `burnedGold = "0.00"`

The placeholder is not treated as a real burn account.

## 6. Actual Vs Modeled Supply Separation

Actual supply appears in `/v35/supply` and `/v35/burn`.

Modeled founder burn appears only in `/v35/founder`:

- `modeledBurnPercent = "80"`
- `modeledBurnGold` is calculated from current founder reserve.
- `modelOnly = true`
- `actualBurnedGold` remains separate.

The modeled burn is not added to actual burned supply and is not subtracted from actual circulating supply.

## 7. V35 Disabled Confirmation

Confirmed in wallet and server:

- `Seting.V35_RESCUE_FORK_INDEX = Long.MAX_VALUE`

The endpoint response builder reports:

- `v35Active = false`
- `v35ForkIndex = "Long.MAX_VALUE"`

No V35 activation change was made.

## 8. Burn Transaction Confirmation

No burn transaction was created.

`/v35/burn` reports:

- `burnTransactionCreated = false`

The endpoint also includes a note that actual burned supply is based on burn address balance, not modeled burn.

## 9. UI / Website Confirmation

No UI, template, dashboard, or website files were changed.

No wallet controller or wallet UI wiring was added.

## 10. Tests Added

Added server `V35SupplyControllerTest`.

Covered:
- `/v35/protocol` exposes 200 sec/block, 432 blocks/day, and the 8/6/2/0 reward model.
- `V35_RESCUE_FORK_INDEX` remains `Long.MAX_VALUE`.
- `v35Active` remains `false`.
- placeholder burn address flags are reported correctly.
- burned supply is `0.00` while the burn address is placeholder.
- `circulatingGold = totalMintedGold - burnedGold` in the placeholder case.
- annual emission is `1,261,440.00`.
- endpoint methods do not call mutating `BlockService` methods in the test double.
- decimal fields are string-safe for JSON precision.
- `/v35/burn` does not pretend a burn transaction exists.
- `/v35/founder` separates modeled burn from actual burned supply.

Full Spring MVC integration tests were not added in this phase. The controller methods are covered directly with a read-only `BlockService` test double.

## 11. Verification Commands Run

Server:

```powershell
cd C:\citu\unitedStates_storage-master
.\mvnw.cmd test
.\mvnw.cmd clean package
```

Wallet:

```powershell
cd C:\citu\unitedStates_final-master
.\mvnw.cmd test
.\mvnw.cmd clean package
```

## 12. Test / Build Results

Server:
- `.\mvnw.cmd test`: PASS, 174 tests
- `.\mvnw.cmd clean package`: PASS, 174 tests during package

Wallet:
- `.\mvnw.cmd test`: PASS, 157 tests
- `.\mvnw.cmd clean package`: PASS, 157 tests during package

Maven still reports existing duplicate dependency warnings in both projects. No `pom.xml` changes were made.

## 13. Behavior Confirmation

- V35 remains disabled.
- No burn transaction was created.
- No mining behavior changed.
- No validation behavior changed.
- No balance behavior changed.
- Transaction format unchanged.
- Block format unchanged.
- `Mining.java` was not changed.
- `UtilsBlock.java` was not changed.
- `UtilsBalance.java` was not changed.
- UI/templates were not changed.
- Website files were not changed.
- Wallet source was not changed.
- `pom.xml` was not changed.

## 14. Remaining Risks Before Wallet / Site Dashboard Wiring

1. `V35_BURN_ADDRESS` is still `TODO_V35_BURN_ADDRESS`.
2. Full-chain scanning on every `/v35/supply` request may be expensive on large production nodes.
3. Future dashboard wording must clearly separate actual supply from modeled founder burn.
4. The endpoint reports jackpot paid from canonical stored transactions by canonical amount; it is a reporting view, not an independent jackpot proof validator.
5. No browser/UI wiring exists yet.
6. Wallet does not yet expose mirrored local V35 supply endpoints or UI data methods.
7. Full Spring MVC/HTTP tests can be added later if the project standardizes controller integration testing.

## 15. Recommended Next Phase

Phase 5D - final burn address generation/replacement plan, no code changes.

Recommended focus:
- generate/select final valid V35 burn address,
- document private-key destruction/non-retention process,
- plan exact replacement of `TODO_V35_BURN_ADDRESS`,
- keep V35 disabled until final burn-address and activation checklist are complete.
