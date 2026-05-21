# CITU V35 Phase 5C-2A Supply Chain/Account Adapter Report

Phase: 5C-2A only
Scope: pure V35 supply chain/account adapter and tests
V35 activation: still disabled
Burn transaction: not created
API/UI/controller wiring: none
Transaction/block format changes: none

## 1. Exact files changed

Wallet:

- `unitedStates_final-master/src/main/java/International_Trade_Union/protocol/v35/V35SupplyChainAdapter.java`
- `unitedStates_final-master/src/test/java/International_Trade_Union/protocol/v35/V35SupplyChainAdapterTest.java`
- `unitedStates_final-master/src/test/java/International_Trade_Union/protocol/v35/V35ParityTest.java`

Server:

- `unitedStates_storage-master/src/main/java/International_Trade_Union/protocol/v35/V35SupplyChainAdapter.java`
- `unitedStates_storage-master/src/test/java/International_Trade_Union/protocol/v35/V35SupplyChainAdapterTest.java`
- `unitedStates_storage-master/src/test/java/International_Trade_Union/protocol/v35/V35ParityTest.java`

Report:

- `CITU_V35_PHASE5C2A_SUPPLY_CHAIN_ACCOUNT_ADAPTER_REPORT.md`

## 2. Adapter class added

Added `V35SupplyChainAdapter` in wallet and server.

Purpose:

- build `V35SupplySnapshot` from explicit chain/account inputs
- flatten transaction lists from explicit `List<Block>`
- calculate total minted from explicit transactions
- calculate jackpot paid from explicit transactions
- calculate burned/founder reserve from explicit account maps

This is pure helper code only. It does not read files, query databases, call controllers, mutate balances, validate blocks, mine blocks, or change live behavior.

## 3. Exact methods added

`V35SupplyChainAdapter`:

- `snapshotFromChainAndAccounts(List<Block> chain, Map<String, Account> balances)`
- `snapshotFromChainAndAccounts(List<Block> chain, Map<String, Account> balances, String burnAddress, String founderAddress)`
- `snapshotFromTransactionsAndAccounts(List<DtoTransaction> transactions, Map<String, Account> balances)`
- `snapshotFromTransactionsAndAccounts(List<DtoTransaction> transactions, Map<String, Account> balances, String burnAddress, String founderAddress)`
- `totalMintedGold(List<DtoTransaction> transactions)`
- `jackpotPaidGold(List<DtoTransaction> transactions)`
- `totalMintedGoldFromChain(List<Block> chain)`
- `jackpotPaidGoldFromChain(List<Block> chain)`
- `transactionsFromChain(List<Block> chain)`

## 4. Supply calculations

The adapter delegates calculation to `V35SupplyCalculator`.

Rules implemented:

- `totalMintedGold` = sum of `BASIS_ADDRESS` GOLD outputs from explicit transactions.
- `burnedGold` = burn address GOLD balance from the explicit account map, unless burn address is the TODO placeholder.
- `circulatingGold = totalMintedGold - burnedGold`.
- `founderReserveGold` = founder address GOLD balance from the explicit account map.
- `jackpotPaidGold` = sum of canonical jackpot BASIS payouts already present in explicit transactions.
- normal user transfers do not create minted supply.
- `6.00` miner reward is minted but not jackpot paid.
- non-canonical BASIS amounts are minted but not jackpot paid.
- virtual jackpot reserve is not minted until an actual payout transaction exists.
- annual emission remains `1,261,440.00 GOLD`.

## 5. Precision model

The adapter uses `BigDecimal` through `V35SupplyCalculator`.

GOLD values are kept at canonical 2-decimal precision. The adapter does not use direct double equality for supply decisions; jackpot classification is delegated to the existing canonical payout helpers.

## 6. Tests added/updated

Added mirrored wallet/server `V35SupplyChainAdapterTest`.

Covered:

- V35 remains disabled with `V35_RESCUE_FORK_INDEX = Long.MAX_VALUE`.
- BASIS miner reward adds to `totalMintedGold`.
- BASIS jackpot payout adds to both `totalMintedGold` and `jackpotPaidGold`.
- normal user transfer does not add to minted supply.
- burn address balance reduces circulating supply.
- `6.00` miner reward is not jackpot paid.
- non-canonical BASIS amount is not jackpot paid.
- placeholder burn address returns burned `0.00`.
- chain input is flattened from `Block.getDtoTransactions()`.
- null blocks and null transaction entries are ignored safely.
- annual emission in the resulting snapshot is `1,261,440.00`.

Updated wallet/server `V35ParityTest` to include:

- `V35SupplyChainAdapter`
- expected adapter methods

## 7. Verification commands run

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

## 8. Test/build results

Wallet:

- `.\mvnw.cmd test`: PASS, 157 tests
- `.\mvnw.cmd clean package`: PASS, 157 tests during package

Server:

- `.\mvnw.cmd test`: PASS, 169 tests
- `.\mvnw.cmd clean package`: PASS, 169 tests during package

Maven still reports existing duplicate-dependency model warnings in both projects. No `pom.xml` changes were made.

## 9. Behavior confirmation

- V35 remains disabled.
- No burn transaction was created.
- No production behavior changed.
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

## 10. Remaining risks before API/controller wiring

1. `V35_BURN_ADDRESS` is still `TODO_V35_BURN_ADDRESS`.
2. The adapter consumes explicit chain/account inputs only; it is not wired to repository, controller, database, or node runtime state.
3. Total minted scanning is transaction-list based and does not itself validate whether the provided blocks are canonical or valid.
4. Jackpot paid classification is amount-based and does not perform full jackpot proof validation.
5. Existing `/totalDollars`, `/totalStaking`, `/totalAll`, and account-list endpoints still expose legacy balance totals rather than V35 supply snapshots.
6. A future API/storage phase must decide whether supply snapshots are computed from full chain scans, current account maps, database queries, or a combined audit mode.

## 11. Recommended next phase

Phase 5C-2B - supply API/controller/storage wiring plan, no code changes.

Recommended focus:

- identify exact controller/service/repository paths for exposing a supply snapshot,
- decide authoritative source for total minted and burned supply,
- keep burn address placeholder safety visible,
- do not activate V35 until final burn address and supply reporting are ready.
