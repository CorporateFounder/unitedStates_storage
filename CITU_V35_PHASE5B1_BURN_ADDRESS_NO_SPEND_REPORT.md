# CITU V35 Phase 5B-1 Burn Address No-Spend Report

Phase: 5B-1 only
Scope: V35 burn-address no-spend enforcement behind fork gate
V35 activation: still disabled
Burn transaction: not created
Transaction/block format changes: none

## 1. Exact files changed

Wallet:

- `unitedStates_final-master/src/main/java/International_Trade_Union/protocol/v35/BurnAddressPolicy.java`
- `unitedStates_final-master/src/main/java/International_Trade_Union/utils/UtilsBlock.java`
- `unitedStates_final-master/src/main/java/International_Trade_Union/utils/UtilsUse.java`
- `unitedStates_final-master/src/main/java/International_Trade_Union/utils/UtilsBalance.java`
- `unitedStates_final-master/src/test/java/International_Trade_Union/protocol/v35/BurnAddressPolicyTest.java`
- `unitedStates_final-master/src/test/java/International_Trade_Union/protocol/v35/V35ParityTest.java`

Server:

- `unitedStates_storage-master/src/main/java/International_Trade_Union/protocol/v35/BurnAddressPolicy.java`
- `unitedStates_storage-master/src/main/java/International_Trade_Union/utils/UtilsBlock.java`
- `unitedStates_storage-master/src/main/java/International_Trade_Union/utils/UtilsUse.java`
- `unitedStates_storage-master/src/main/java/International_Trade_Union/utils/UtilsBalance.java`
- `unitedStates_storage-master/src/main/java/International_Trade_Union/network/AllTransactions.java`
- `unitedStates_storage-master/src/test/java/International_Trade_Union/protocol/v35/BurnAddressPolicyTest.java`
- `unitedStates_storage-master/src/test/java/International_Trade_Union/protocol/v35/V35ParityTest.java`

Report:

- `CITU_V35_PHASE5B1_BURN_ADDRESS_NO_SPEND_REPORT.md`

## 2. Exact methods changed

Wallet and server:

- `BurnAddressPolicy.getBurnAddress()`
- `BurnAddressPolicy.isPlaceholderBurnAddress()`
- `BurnAddressPolicy.isBurnAddress(String)`
- `BurnAddressPolicy.isNoSpendActive(long)`
- `BurnAddressPolicy.isNoSpendActive(long, long)`
- `BurnAddressPolicy.canSpendFromAddress(long, String)`
- `BurnAddressPolicy.canSpendFromAddress(long, String, long)`
- `BurnAddressPolicy.isSpendForbidden(long, String)`
- `BurnAddressPolicy.isSpendForbidden(long, String, long)`
- `UtilsBlock.validationOneBlockWithCanonicalContext(...)`
- `UtilsUse.balanceTransaction(...)`
- `UtilsBalance.rollbackCalculateBalance(...)`
- `UtilsBalance.calculateBalance(...)`

Server only:

- `AllTransactions` transaction filtering path before `DtoTransaction.verify()`
- `AllTransactions` balance filtering path before `transaction.verify()`

Tests:

- `BurnAddressPolicyTest`
- `V35ParityTest`

## 3. Current burn address value

Current wallet/server value:

- `V35_BURN_ADDRESS = "TODO_V35_BURN_ADDRESS"`

The placeholder remains intentionally. Phase 5B-1 did not set the final burn address.

Final activation requires replacing the placeholder with a real generated valid CITU address whose private key is destroyed or not retained. This remains a blocking operational requirement before any real V35 activation or public founder burn.

## 4. V35 disabled confirmation

Confirmed after implementation:

- Wallet: `V35_RESCUE_FORK_INDEX = Long.MAX_VALUE`
- Server: `V35_RESCUE_FORK_INDEX = Long.MAX_VALUE`

`BurnAddressPolicy.isNoSpendActive(...)` now explicitly requires `forkIndex != Long.MAX_VALUE`. Therefore even `index == Long.MAX_VALUE` does not make the no-spend rule active while the configured fork index is still disabled.

## 5. No-spend enforcement

The no-spend rule is:

- after V35 only, any transaction whose sender equals `Seting.V35_BURN_ADDRESS` is invalid.

The rule is centralized through `BurnAddressPolicy`.

Implemented checks:

- `UtilsBlock.validationOneBlockWithCanonicalContext(...)` rejects burn-address sender before `transaction.verify()` / public-key decoding.
- `UtilsUse.balanceTransaction(...)` skips burn-address sender after V35 before balance mutation.
- `UtilsBalance.rollbackCalculateBalance(...)` skips burn-address sender after V35 before signature verification.
- `UtilsBalance.calculateBalance(...)` skips burn-address sender after V35 before signature verification.
- Server `AllTransactions` rejects burn-address sender in transaction-pool filtering before `dtoTransaction.verify()`.
- Server `AllTransactions` rejects burn-address sender in its balance filtering path before `transaction.verify()`.

This preserves sender-decode safety for the burn address in the main validation and transaction-pool paths.

## 6. Sending TO burn address

Sending to the burn address remains allowed by policy.

This phase only blocks transactions whose sender is the burn address after V35. It does not reject transactions whose customer/recipient is the burn address.

Therefore the intended founder burn model remains:

`founderAddress -> V35_BURN_ADDRESS`

as a normal signed transaction, once the final burn address is set.

## 7. Spending FROM burn address

Spending from the burn address is blocked after V35 by:

- `BurnAddressPolicy.isSpendForbidden(...)`
- validation path checks before signature decode
- balance helper checks before mutation
- server transaction-pool checks before signature decode

Before V35, legacy behavior is preserved. With `V35_RESCUE_FORK_INDEX = Long.MAX_VALUE`, the rule is inactive and current live behavior remains unchanged.

## 8. Tests added/updated

Wallet/server `BurnAddressPolicyTest` now verifies:

- `V35_BURN_ADDRESS` is exposed through policy.
- current value is still the placeholder.
- sending/spending policy is inactive while `V35_RESCUE_FORK_INDEX = Long.MAX_VALUE`.
- burn-address spend is rejected only under an explicit simulated fork index.
- normal addresses remain spendable under the simulated fork.

Wallet/server `V35ParityTest` now verifies the new burn policy helper methods exist in both projects.

## 9. Verification commands run

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

## 10. Test/build results

Wallet:

- `.\mvnw.cmd test`: PASS
- `.\mvnw.cmd clean package`: PASS

Server:

- `.\mvnw.cmd test`: PASS
- `.\mvnw.cmd clean package`: PASS

Build-generated tracked `target/` artifacts were restored after verification so they are not part of the intended source diff.

## 11. Behavior confirmation

- V35 remains disabled.
- No burn transaction was created.
- No balances were changed.
- Transaction format was not changed.
- Block format was not changed.
- `Mining.java` was not changed.
- UI/templates were not changed.
- Website was not changed.
- `pom.xml` was not changed.

## 12. Remaining risks before supply accounting

1. `V35_BURN_ADDRESS` is still `TODO_V35_BURN_ADDRESS`.
2. A final generated burn address and private-key destruction/non-retention ceremony are still required.
3. Wallet UI/controller send paths were not changed in this phase; consensus and server pool paths reject after V35, but UI can still be improved later to prevent creating burn-address sender transactions.
4. Supply accounting still does not exclude the burn address from circulating/outstanding supply views.
5. The actual founder burn transaction has not been built, signed, broadcast, or proven.
6. Any path that bypasses both block validation and V35-aware balance helpers would still need review before activation.

## 13. Recommended next phase

Phase 5C - burned supply / circulating supply accounting helpers.
