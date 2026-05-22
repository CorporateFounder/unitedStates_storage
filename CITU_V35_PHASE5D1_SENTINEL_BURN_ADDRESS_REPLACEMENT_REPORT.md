# CITU V35 Phase 5D-1 Sentinel Burn Address Replacement Report

Phase: 5D-1 only  
Scope: replace V35 burn placeholder with sentinel `BURN_ADDRESS`  
V35 activation: still disabled  
Burn transaction: not created  
Transaction/block format changes: none  
Private key generation/storage: none

## 1. Exact Files Changed

Wallet source:

- `unitedStates_final-master/src/main/java/International_Trade_Union/setings/Seting.java`

Wallet tests:

- `unitedStates_final-master/src/test/java/International_Trade_Union/protocol/v35/BurnAddressPolicyTest.java`
- `unitedStates_final-master/src/test/java/International_Trade_Union/protocol/v35/V35SupplyCalculatorTest.java`
- `unitedStates_final-master/src/test/java/International_Trade_Union/protocol/v35/V35SupplyChainAdapterTest.java`

Server source:

- `unitedStates_storage-master/src/main/java/International_Trade_Union/setings/Seting.java`

Server tests:

- `unitedStates_storage-master/src/test/java/International_Trade_Union/protocol/v35/BurnAddressPolicyTest.java`
- `unitedStates_storage-master/src/test/java/International_Trade_Union/protocol/v35/V35SupplyCalculatorTest.java`
- `unitedStates_storage-master/src/test/java/International_Trade_Union/protocol/v35/V35SupplyChainAdapterTest.java`
- `unitedStates_storage-master/src/test/java/International_Trade_Union/controllers/V35SupplyControllerTest.java`

Report:

- `CITU_V35_PHASE5D1_SENTINEL_BURN_ADDRESS_REPLACEMENT_REPORT.md`

## 2. Exact Methods / Constants Changed

Wallet and server:

- `Seting.V35_BURN_ADDRESS`
  - old value: `TODO_V35_BURN_ADDRESS`
  - new value: `BURN_ADDRESS`

No production validation, mining, balance, controller, DTO, block, or transaction format methods were changed.

## 3. Final Burn Address Value

Final configured V35 burn address:

```text
BURN_ADDRESS
```

The value is identical in wallet and server.

## 4. Wallet / Server Match Confirmation

Confirmed by source inspection and tests:

- wallet `Seting.V35_BURN_ADDRESS = "BURN_ADDRESS"`
- server `Seting.V35_BURN_ADDRESS = "BURN_ADDRESS"`

The existing V35 parity reflection test still passes in both projects, and the burn policy tests assert the sentinel value directly.

## 5. V35 Disabled Confirmation

Confirmed in wallet and server:

```text
Seting.V35_RESCUE_FORK_INDEX = Long.MAX_VALUE
```

No V35 activation change was made.

Because `BurnAddressPolicy.isNoSpendActive(...)` requires:

```text
forkIndex != Long.MAX_VALUE && index >= forkIndex
```

current live behavior remains disabled/legacy.

## 6. Burn Transaction Confirmation

No burn transaction was created.

This phase only configured the burn address constant and updated tests.

## 7. Private Key Confirmation

No private key was generated, added, stored, or committed.

The sentinel model intentionally uses a literal non-key account string. Tests sign a normal transaction to `BURN_ADDRESS` using the existing configured BASIS test key only to prove that recipient-side verification does not public-key-decode the burn recipient.

## 8. Placeholder Status After Replacement

After replacement:

- `BurnAddressPolicy.isPlaceholderBurnAddress()` returns `false`.
- supply/protocol/burn responses report `isBurnAddressPlaceholder = false`.
- supply/protocol/burn responses report `burnAddressConfigured = true`.
- `burnAddress = "BURN_ADDRESS"`.

The placeholder constant remains in `BurnAddressPolicy` only so code can continue to detect the old placeholder value if it ever appears.

## 9. Sending TO `BURN_ADDRESS`

Sending to `BURN_ADDRESS` remains allowed by policy.

Test coverage added in wallet/server:

- A normal signed transaction with:

```text
sender = Seting.BASIS_ADDRESS
customer = Seting.V35_BURN_ADDRESS
```

successfully verifies.

This proves the recipient/customer field is not public-key decoded by core `DtoTransaction.verify()` and can safely carry the sentinel string as signed transaction data.

No transaction format change was needed.

## 10. Spending FROM `BURN_ADDRESS`

After a simulated active V35 fork gate:

- `BurnAddressPolicy.isSpendForbidden(...)` returns `true` for sender `BURN_ADDRESS`.
- `BurnAddressPolicy.canSpendFromAddress(...)` returns `false`.
- block validation rejects a transaction whose sender is `BURN_ADDRESS` without crashing on sender decode.

The block-validation test uses the context-aware overload with an explicit simulated fork index. That keeps `V35_RESCUE_FORK_INDEX` disabled while proving the active-fork guard rejects the sentinel sender before normal transaction verification.

## 11. Sender Pre-Verify Path Coverage

Covered by tests:

- `BurnAddressPolicy` active-fork sender rule.
- `BurnAddressPolicy` disabled-fork legacy behavior.
- `UtilsBlock.validationOneBlockWithCanonicalContext(...)` rejects `BURN_ADDRESS` sender under simulated active fork without decode crash.
- `UtilsUse.balanceTransaction(...)` does not crash on `BURN_ADDRESS` sender while V35 remains disabled.
- recipient `BURN_ADDRESS` verifies normally when sender is valid.

Covered by source/path inspection:

- server `AllTransactions.addTransaction(...)` calls `BurnAddressPolicy.isSpendForbidden(...)` before `dtoTransaction.verify()`.
- server `AllTransactions` balance filtering path checks `BurnAddressPolicy.isSpendForbidden(...)` before `transaction.verify()`.

Direct active-fork transaction-pool simulation was not added because `AllTransactions` reads the real global `Seting.V35_RESCUE_FORK_INDEX`, which remains `Long.MAX_VALUE` by requirement. Adding a production test hook solely to override that constant would have exceeded this phase’s minimal-change scope. The active predicate itself is covered through `BurnAddressPolicy` tests, and the transaction-pool call order was inspected.

## 12. Supply / API Test Updates

Server `V35SupplyControllerTest` now expects:

- `burnAddress = "BURN_ADDRESS"`
- `isBurnAddressPlaceholder = false`
- `burnAddressConfigured = true`
- `burnedGold = "0.00"` while no burn transaction/account balance exists
- V35 remains inactive
- endpoint remains read-only

Wallet/server supply helper tests now treat `Seting.V35_BURN_ADDRESS` as configured:

- burn address account balance becomes burned supply
- circulating supply is `totalMinted - burned`
- placeholder behavior is no longer expected for the configured sentinel

## 13. Behavior Confirmation

Confirmed:

- V35 remains disabled.
- No burn transaction was created.
- No balances were changed.
- No private key was generated or stored.
- Transaction format was not changed.
- Block format was not changed.
- `Mining.java` was not changed.
- `DtoTransaction.java` was not changed.
- `Block.java` was not changed.
- `Blockchain.java` was not changed.
- `UtilsBlock.java` was not changed.
- `UtilsUse.java` was not changed.
- `UtilsBalance.java` was not changed.
- server `AllTransactions.java` was not changed in this phase.
- UI/templates were not changed.
- Website files were not changed.
- `pom.xml` was not changed.

## 14. Verification Commands Run

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

## 15. Test / Build Results

Wallet:

- `.\mvnw.cmd test`: PASS, 160 tests
- `.\mvnw.cmd clean package`: PASS, 160 tests during package

Server:

- `.\mvnw.cmd test`: PASS, 177 tests
- `.\mvnw.cmd clean package`: PASS, 177 tests during package

Maven still reports existing duplicate dependency warnings in both projects. No `pom.xml` changes were made.

During the burn-sender block-validation test, the existing validation code attempted to write to the configured error log path and printed a `FileNotFoundException` stack trace for `\server\resources\error\error.txt`. The exception is caught by existing logging code and did not fail tests or builds. No logging-path behavior was changed in this phase.

## 16. Remaining Steps Before Manual Founder Burn

1. Keep V35 disabled until the full activation checklist is complete.
2. Confirm founder address balance from canonical chain/account data.
3. Calculate exact 80% burn amount from the confirmed founder balance.
4. Prepare the founder-signed transaction:

```text
founderAddress -> BURN_ADDRESS
```

5. Verify transaction amount, recipient, signature, and fees/bonus behavior before broadcast.
6. Broadcast only after the operational burn checklist is approved.
7. Publish proof:
   - burn address
   - founder address
   - burn transaction hash/signature
   - block index
   - burned amount
   - founder remaining balance
   - total minted
   - burned supply
   - circulating supply
   - annual emission

## 17. Remaining Risks

1. Active-fork transaction-pool behavior is path-inspected, not simulated through `AllTransactions`, because the production class reads the disabled global fork constant.
2. External wallet/exchange tooling may still reject `BURN_ADDRESS` as a non-public-key recipient.
3. Direct callers of `DtoTransaction.verify()` with sender `BURN_ADDRESS` would still be unsafe if they bypass all policy-aware validation paths.
4. The burn transaction has not yet been created, broadcast, confirmed, or publicly proven.
5. V35 remains disabled, so live consensus no-spend enforcement is not active yet.

## 18. Recommended Next Phase

Phase 5E - founder balance snapshot / burn amount confirmation plan.

Recommended focus:

- confirm current founder balance from canonical chain data,
- calculate exact 80% burn amount,
- define manual burn transaction checklist,
- define public proof package,
- keep V35 disabled until burn and activation criteria are separately approved.

