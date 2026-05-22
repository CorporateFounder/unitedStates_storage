# CITU V35 Phase 5D-0B Sentinel Burn Address Review

## 1. Scope

This phase reviewed whether the final V35 burn address should be a literal sentinel string such as `BURN_ADDRESS` instead of a generated valid CITU public-key address.

No source code was modified. No burn address was replaced. No address was generated. No burn transaction was created. V35 remains disabled.

## 2. Reports Read

- `CITU_V35_PHASE5D0_FINAL_BURN_ADDRESS_PLAN.md`
- `CITU_V35_PHASE5B0_BURN_ADDRESS_NO_SPEND_REVIEW.md`
- `CITU_V35_PHASE5B1_BURN_ADDRESS_NO_SPEND_REPORT.md`

## 3. Exact Files And Methods Inspected

### Wallet Project

- `unitedStates_final-master/src/main/java/International_Trade_Union/setings/Seting.java`
  - `BASIS_ADDRESS`
  - `BUDGET`
  - `EMISSION`
  - `ADDRESS_FOUNDER`
  - `FORK_ADDRESS_SPECIAL`
  - `V35_RESCUE_FORK_INDEX`
  - `V35_BURN_ADDRESS`
- `unitedStates_final-master/src/main/java/International_Trade_Union/protocol/v35/BurnAddressPolicy.java`
  - `getBurnAddress()`
  - `isPlaceholderBurnAddress()`
  - `isBurnAddress(...)`
  - `isNoSpendActive(...)`
  - `canSpendFromAddress(...)`
  - `isSpendForbidden(...)`
- `unitedStates_final-master/src/main/java/International_Trade_Union/entity/DtoTransaction/DtoTransaction.java`
  - `verify()`
  - `toSign()`
- `unitedStates_final-master/src/main/java/International_Trade_Union/utils/UtilsSecurity.java`
  - `generateKeyPair()`
  - `decodeKey(...)`
- `unitedStates_final-master/src/main/java/International_Trade_Union/utils/UtilsBlock.java`
  - `validationOneBlock(...)`
  - `validationOneBlockWithCanonicalContext(...)`
- `unitedStates_final-master/src/main/java/International_Trade_Union/utils/UtilsUse.java`
  - `balanceTransaction(...)`
- `unitedStates_final-master/src/main/java/International_Trade_Union/utils/UtilsBalance.java`
  - `calculateBalance(...)`
  - `rollbackCalculateBalance(...)`
  - `calculateBalances(...)`
  - `sendMoney(...)`
  - `sendFromBudget(...)`
  - `rollbackFromBudget(...)`
- `unitedStates_final-master/src/main/java/International_Trade_Union/controllers/CreateAccount.java`
  - `create()`
- `unitedStates_final-master/src/main/java/International_Trade_Union/controllers/CreateAccountController.java`
  - account generation/display flow

### Server Project

- `unitedStates_storage-master/src/main/java/International_Trade_Union/setings/Seting.java`
  - `BASIS_ADDRESS`
  - `BUDGET`
  - `EMISSION`
  - `ADDRESS_FOUNDER`
  - `FORK_ADDRESS_SPECIAL`
  - `V35_RESCUE_FORK_INDEX`
  - `V35_BURN_ADDRESS`
- `unitedStates_storage-master/src/main/java/International_Trade_Union/protocol/v35/BurnAddressPolicy.java`
  - same policy methods as wallet
- `unitedStates_storage-master/src/main/java/International_Trade_Union/entity/DtoTransaction/DtoTransaction.java`
  - `verify()`
  - `toSign()`
- `unitedStates_storage-master/src/main/java/International_Trade_Union/utils/UtilsSecurity.java`
  - `generateKeyPair()`
  - `decodeKey(...)`
- `unitedStates_storage-master/src/main/java/International_Trade_Union/utils/UtilsBlock.java`
  - `validationOneBlock(...)`
  - `validationOneBlockWithCanonicalContext(...)`
- `unitedStates_storage-master/src/main/java/International_Trade_Union/utils/UtilsUse.java`
  - `balanceTransaction(...)`
- `unitedStates_storage-master/src/main/java/International_Trade_Union/utils/UtilsBalance.java`
  - `calculateBalance(...)`
  - `rollbackCalculateBalance(...)`
  - `calculateBalances(...)`
  - `sendMoney(...)`
  - `sendFromBudget(...)`
  - `rollbackFromBudget(...)`
- `unitedStates_storage-master/src/main/java/International_Trade_Union/network/AllTransactions.java`
  - `addTransaction(...)`
  - transaction-pool balance checks
- `unitedStates_storage-master/src/main/java/International_Trade_Union/controllers/V35SupplyController.java`
  - `/v35/supply`
  - `/v35/protocol`
  - `/v35/burn`
  - `/v35/founder`
- `unitedStates_storage-master/src/main/java/International_Trade_Union/protocol/v35/V35SupplyApiResponseBuilder.java`
  - supply/protocol/burn/founder response construction

## 4. Current Constants And Special Account Findings

Both wallet and server currently define several system-level account strings:

- `Seting.BASIS_ADDRESS`
  - A valid-looking CITU/Base58 public-key style address.
  - Used as the mint/reward sender.
- `Seting.ADDRESS_FOUNDER`
  - A valid-looking public-key style address.
- `Seting.BUDGET = "BUDGET"`
  - A literal non-key system account.
  - Used in budget/government accounting paths.
- `Seting.EMISSION = "EMISSION"`
  - A literal non-key system account.
- `Seting.NAME_LAW_ADDRESS_START = "LIBER"`
  - Prefix used for law/special-address style logic.
- `Seting.FORK_ADDRESS_SPECIAL`
  - A valid-looking special fork address.
- `Seting.V35_BURN_ADDRESS = "TODO_V35_BURN_ADDRESS"`
  - Current placeholder burn address in both wallet and server.
- `Seting.V35_RESCUE_FORK_INDEX = Long.MAX_VALUE`
  - V35 remains disabled in both wallet and server.

The existence of `BUDGET`, `EMISSION`, and `LIBER...` style system identifiers shows that the codebase already has precedent for non-key account names in account maps and some internal accounting paths.

## 5. Budget / Special Account Behavior

Budget-like account behavior is not the same as a normal signed user transaction from a public-key address.

Relevant findings:

- `UtilsBalance.sendFromBudget(...)` and `rollbackFromBudget(...)` operate on the configured budget account string.
- Budget accounting works through account-map mutation and law/government logic, not through a normal ECDSA public-key sender flow.
- Account maps are keyed by `String`, so non-key strings can be represented as accounts.
- This supports the feasibility of storing/displaying `BURN_ADDRESS` as an account key.
- It does not by itself prove that a normal transaction with sender `BURN_ADDRESS` can safely pass through all verification paths. Sender-side verification is stricter.

## 6. Address And Signature Verification Findings

### Address Generation

`UtilsSecurity.generateKeyPair()` generates secp256k1 ECDSA keys and returns:

- `pubkey`: Base58-encoded compressed public key.
- `privKey`: Base58-encoded private key material.

Generated CITU addresses are therefore public-key-derived Base58 strings.

### Transaction Verification

`DtoTransaction.verify()` decodes only the sender:

1. `Base58.decode(sender)`
2. `UtilsSecurity.decodeKey(pub)`
3. ECDSA signature verification over `toSign()`

The recipient/customer address is not decoded as a public key in `DtoTransaction.verify()`.

`DtoTransaction.toSign()` includes the recipient string in the signed payload:

- sender
- customer
- dollar amount
- stock amount
- laws
- miner bonus

This means a sender can sign a transaction to a literal recipient string such as `BURN_ADDRESS` without changing transaction format.

## 7. Whether Non-Key Recipient Addresses Are Accepted

### Finding

A non-key recipient such as `BURN_ADDRESS` appears feasible for normal signed transactions.

Reasons:

- `DtoTransaction.verify()` verifies the sender public key and signature, not the recipient public key.
- `UtilsBalance.sendMoney(...)` credits `transaction.getCustomer()` as a string account key.
- Account/balance maps can hold arbitrary string account identifiers.
- The code already uses special non-key identifiers such as `BUDGET` and `EMISSION`.
- `toSign()` signs the literal recipient string, so the burn destination remains tamper-evident.

### Important Caveat

External wallet, exchange, or UI code may still assume recipients are valid Base58 public-key addresses. The core transaction and balance paths appear capable of handling a sentinel recipient, but user-facing send forms and third-party integrations need compatibility testing before replacing the placeholder.

## 8. Recipient Decoding Risk

Recipient decoding risk appears low in the inspected core validation paths.

Observed behavior:

- `DtoTransaction.verify()` decodes sender only.
- `UtilsBlock` validation checks transaction validity through sender verification.
- `UtilsBalance` credits recipient string account keys.
- `AllTransactions` validates sender signature and pool constraints, but the inspected path does not decode recipient as a public key.

Therefore, `BURN_ADDRESS` as a recipient should not trigger public-key decode failures in the inspected core paths.

## 9. Sender Decode / Verify Risk

Sender decode risk is the main sentinel-model risk.

A transaction spending from sentinel sender `BURN_ADDRESS` cannot be verified as a normal public-key transaction, because `BURN_ADDRESS` is not a Base58-encoded secp256k1 public key.

If a path calls `DtoTransaction.verify()` directly on a transaction whose sender is `BURN_ADDRESS`, sender decoding may fail before a policy rejection is applied.

The Phase 5B-1 no-spend work already added important pre-verify guards:

- `UtilsBlock.validationOneBlockWithCanonicalContext(...)`
  - Checks `BurnAddressPolicy.isSpendForbidden(...)` before `transaction.verify()`.
- `UtilsUse.balanceTransaction(...)`
  - Checks burn sender policy before applying balance transaction logic.
- `UtilsBalance.rollbackCalculateBalance(...)`
  - Checks burn sender policy before `transaction.verify()`.
- `UtilsBalance.calculateBalance(...)`
  - Builds account map entries first, then checks burn sender policy before `transaction.verify()`.
- Server `AllTransactions.addTransaction(...)`
  - Checks burn sender policy before `dtoTransaction.verify()`.
- Server transaction-pool balance path
  - Checks burn sender policy before `transaction.verify()`.

These guards make post-V35 no-spend enforcement practical, but Phase 5D-1 must add tests proving the guard always executes before sender decoding on every relevant path.

## 10. V35 Gate Implication

Current no-spend activation is fork-gated:

```text
forkIndex != Long.MAX_VALUE && blockIndex >= forkIndex
```

Because `V35_RESCUE_FORK_INDEX = Long.MAX_VALUE`, no-spend enforcement remains inactive today.

This is correct for preserving current live behavior, but it creates a sentinel-specific operational concern:

- After replacing the placeholder with `BURN_ADDRESS`, any malicious transaction using `BURN_ADDRESS` as sender before V35 activation may still reach normal sender verification unless the path has a non-fork-gated malformed-sender safety catch.
- No valid signature can exist for `BURN_ADDRESS`, but decode/exception safety still matters.

Recommendation: if the sentinel model is chosen, Phase 5D-1 should include tests that submit `BURN_ADDRESS -> anyRecipient` before and after V35 through block, mempool, balance, and helper paths. If any path throws instead of safely rejecting, add the smallest pre-verify guard or exception-safe handling in that exact path.

## 11. Transaction Pool Findings

Server `AllTransactions.addTransaction(...)` has a pre-verify burn sender check:

- sender is checked through `BurnAddressPolicy.isSpendForbidden(BasisController.getBlockchainSize(), dtoTransaction.getSender())`
- only then does it call `dtoTransaction.verify()`

This is good for post-V35 no-spend behavior.

However, because `isSpendForbidden(...)` is currently inactive while V35 is disabled, the transaction pool still needs a sentinel-sender safety test before final address replacement. A malformed sender should fail safely and must not destabilize the transaction pool.

Recipient-side transaction pool behavior appears compatible with a sentinel recipient, because the recipient is not public-key decoded in the inspected verification path.

## 12. Block Validation Findings

`UtilsBlock.validationOneBlockWithCanonicalContext(...)` checks burn sender policy before transaction signature verification.

This means that after V35 activation:

- `BURN_ADDRESS -> anyone` should be rejected before public-key decoding.
- `founderAddress -> BURN_ADDRESS` should remain eligible for normal signed transaction validation.

Ordinary recipient/account storage should handle `BURN_ADDRESS` as a string key.

Phase 5D-1 must add block-validation tests for:

- transaction TO sentinel is accepted by policy.
- transaction FROM sentinel is rejected after V35 before decode.
- V35 disabled behavior remains unchanged.

## 13. Balance / Account Map Findings

`UtilsBalance` works primarily with string-keyed account maps.

Relevant behavior:

- `getBalance(transaction.getCustomer(), balances)` can create/read a balance entry for arbitrary string customers.
- `sendMoney(...)` credits the recipient/customer account.
- For non-BASIS transactions, sender is debited and customer is credited.
- The code only specially rejects sending to `BASIS_ADDRESS`.
- Budget and government paths already use non-key special account names.

Therefore, `BURN_ADDRESS` can be stored as an account and can hold a burned GOLD balance.

Supply accounting can continue using burn address balance as burned supply once placeholder replacement happens and the burn transaction exists.

## 14. API / Export / Display Findings

The server V35 supply endpoints currently expose:

- burn address
- placeholder status
- configured status
- burned amount
- circulating amount
- founder reserve
- modeled burn fields

A sentinel string can be displayed in JSON safely.

Required follow-up if sentinel is chosen:

- update placeholder detection so `BURN_ADDRESS` is reported as configured, not placeholder.
- ensure account export and API responses can show `BURN_ADDRESS` as a normal account string.
- make clear in public documentation that `BURN_ADDRESS` is an intentional sentinel, not a public-key-derived user address.

## 15. Exchange / Wallet Compatibility Risk

Core validation and accounting appear compatible with sentinel recipient use, but external and UI-level compatibility is not fully proven.

Risks:

- Wallet send forms may apply recipient format checks outside the inspected core paths.
- Exchanges may reject withdrawals to non-public-key-looking addresses.
- Explorers may label unknown non-key account strings oddly.
- Existing user education may imply all account addresses are Base58 public-key addresses.

Mitigation:

- Add explicit tests for wallet-side transaction creation to `BURN_ADDRESS`.
- Publish burn procedure as a protocol/admin operation, not a normal user withdrawal pattern.
- If exchange compatibility is critical, generated valid address remains lower integration risk.

## 16. Model Comparison

### Model A: Generated Valid Burn Address

Advantages:

- Lowest compatibility risk.
- Looks like a normal CITU address.
- Recipient field works everywhere normal addresses work.
- Sender decode would be syntactically valid if someone had the key, avoiding malformed-sender edge cases.
- Easier for exchanges, wallet forms, explorers, and external tooling.

Disadvantages:

- Requires trusting that the private key was destroyed or never retained.
- Public observers cannot cryptographically prove the private key is unavailable.
- Consensus no-spend policy is still needed after V35 to make the burn irreversible.

### Model B: Sentinel Literal `BURN_ADDRESS`

Advantages:

- No private key can exist for the sentinel string under normal CITU address generation.
- Publicly understandable and audit-friendly.
- Stronger burn narrative: coins are sent to a clearly unspendable system account.
- Existing codebase has precedent for non-key system strings such as `BUDGET` and `EMISSION`.

Disadvantages:

- Higher integration risk.
- Any sender-side path that reaches `DtoTransaction.verify()` before policy rejection may decode/fail/throw.
- V35 no-spend policy is currently fork-gated, so pre-V35 malformed-sender behavior needs explicit safety tests.
- Wallet/exchange tooling may reject non-standard recipient strings.

## 17. Recommended Final Burn Address Model

Recommendation: **sentinel literal burn address is feasible and preferable for burn irreversibility, but only if Phase 5D-1 hardens and tests every pre-verify sender path before replacement.**

Recommended literal:

```text
BURN_ADDRESS
```

Alternative, more explicit literal:

```text
CITU_V35_BURN_ADDRESS
```

`BURN_ADDRESS` is shorter and matches the user’s proposed model. `CITU_V35_BURN_ADDRESS` is more namespace-specific and reduces accidental collision with any future generic account name. Either can work as long as it is centralized in `Seting.V35_BURN_ADDRESS` and referenced only through `BurnAddressPolicy`.

The sentinel model is stronger than a generated address for economic trust because there is no private key to retain. The generated-address model remains lower code-risk and lower integration-risk. If Phase 5D-1 tests find any unsafe decode path that is hard to fix narrowly, fall back to the generated valid address model.

## 18. Whether Sending TO Sentinel Works

Based on inspected core code, sending TO `BURN_ADDRESS` should work as a normal signed transaction:

```text
founderAddress -> BURN_ADDRESS
```

Why:

- sender is a normal valid public-key address.
- sender signs the recipient string as part of the transaction payload.
- recipient is not public-key decoded in `DtoTransaction.verify()`.
- balance/account maps can store string account keys.
- supply accounting can treat the burn address balance as burned supply.

Required proof before replacement:

- wallet/server tests must construct a normal signed transaction to the sentinel and confirm recipient-side validation/accounting does not reject it due to address format.

## 19. Whether Spending FROM Sentinel Can Be Blocked Safely

After V35, spending FROM sentinel can be blocked safely if all paths use `BurnAddressPolicy` before `DtoTransaction.verify()`.

The inspected major paths already contain pre-verify checks:

- block validation
- balance application
- rollback balance application
- transaction pool add path
- transaction-pool balance path

Remaining requirement:

- Phase 5D-1 must prove no other direct `DtoTransaction.verify()` caller can accept or crash on `BURN_ADDRESS` sender.
- Any uncovered path must be fixed with the smallest safe pre-verify rejection or exception-safe validation.

## 20. Required Implementation Steps For Phase 5D-1

If sentinel model is chosen:

1. Replace `Seting.V35_BURN_ADDRESS = "TODO_V35_BURN_ADDRESS"` with the final sentinel in both wallet and server.
2. Keep `V35_RESCUE_FORK_INDEX = Long.MAX_VALUE`.
3. Keep burn transaction creation out of code.
4. Keep transaction and block formats unchanged.
5. Update `BurnAddressPolicy` placeholder/configuration tests so the sentinel is treated as configured and not placeholder.
6. Add tests proving transaction TO sentinel is allowed by policy and core validation/accounting.
7. Add tests proving transaction FROM sentinel is rejected after V35 before sender decode.
8. Add tests proving V35 disabled behavior remains legacy.
9. Add tests for server transaction pool behavior with sentinel sender and sentinel recipient.
10. Add supply endpoint/response tests proving the sentinel is reported as configured and burned supply comes from sentinel balance.
11. Add wallet/server parity coverage.

Only if tests expose a real blocker:

- add a small pre-verify guard to the exact path that still decodes `BURN_ADDRESS` sender before policy rejection.
- avoid changing `DtoTransaction` format or signature semantics.

## 21. Files Phase 5D-1 May Need To Edit

Expected:

- `unitedStates_final-master/src/main/java/International_Trade_Union/setings/Seting.java`
- `unitedStates_storage-master/src/main/java/International_Trade_Union/setings/Seting.java`
- wallet/server `BurnAddressPolicy` tests
- wallet/server `UtilsBlock` or validation tests
- wallet/server `UtilsBalance` tests
- wallet/server `V35ParityTest.java` if parity constants/assertions exist
- server `AllTransactions` tests
- server V35 supply controller/response tests

Only if proven necessary by tests:

- `unitedStates_final-master/src/main/java/International_Trade_Union/protocol/v35/BurnAddressPolicy.java`
- `unitedStates_storage-master/src/main/java/International_Trade_Union/protocol/v35/BurnAddressPolicy.java`
- `unitedStates_final-master/src/main/java/International_Trade_Union/utils/UtilsBlock.java`
- `unitedStates_storage-master/src/main/java/International_Trade_Union/utils/UtilsBlock.java`
- `unitedStates_final-master/src/main/java/International_Trade_Union/utils/UtilsUse.java`
- `unitedStates_storage-master/src/main/java/International_Trade_Union/utils/UtilsUse.java`
- `unitedStates_final-master/src/main/java/International_Trade_Union/utils/UtilsBalance.java`
- `unitedStates_storage-master/src/main/java/International_Trade_Union/utils/UtilsBalance.java`
- `unitedStates_storage-master/src/main/java/International_Trade_Union/network/AllTransactions.java`
- `unitedStates_storage-master/src/main/java/International_Trade_Union/protocol/v35/V35SupplyApiResponseBuilder.java`

## 22. Files Phase 5D-1 Must Not Edit Unless A Later Phase Expands Scope

- `DtoTransaction.java`
- `Block.java`
- `Blockchain.java`
- `Mining.java`
- controllers unrelated to read-only supply reporting
- UI/templates
- website files
- transaction format definitions
- block format definitions
- `pom.xml`

## 23. Required Tests Before Replacement

### Sentinel Recipient Tests

- A normal signed transaction can use `BURN_ADDRESS` as customer/recipient.
- `DtoTransaction.verify()` succeeds for a valid sender signing TO `BURN_ADDRESS`.
- Block validation does not reject the transaction only because the recipient is non-key.
- Balance maps store and expose `BURN_ADDRESS`.

### Sentinel Sender Tests

- `BURN_ADDRESS -> anyRecipient` after V35 is rejected before sender public-key decode.
- Block validation rejects sentinel sender safely.
- Transaction pool rejects sentinel sender safely.
- Balance calculation rejects or ignores sentinel sender safely.
- Rollback path does not credit/debit sentinel sender incorrectly.
- No path throws an uncaught exception on sentinel sender.

### V35 Gate Tests

- With `V35_RESCUE_FORK_INDEX = Long.MAX_VALUE`, current behavior remains legacy.
- No-spend policy remains inactive while V35 is disabled.
- If helper tests simulate active V35, sentinel sender is forbidden.

### Supply / API Tests

- Burn address is no longer placeholder after replacement.
- `burnAddressConfigured = true`.
- burned supply equals sentinel account GOLD balance.
- circulating supply equals total minted minus burned supply.
- modeled burn remains separate from actual burned supply.

### Compatibility Tests

- Wallet and server constants match.
- Wallet and server `BurnAddressPolicy` behavior matches.
- Server transaction-pool behavior matches block-validation behavior.

## 24. Risks

Highest risks:

1. **Sender decode risk**
   - Any unguarded direct call to `DtoTransaction.verify()` with sender `BURN_ADDRESS` can fail before policy rejection.
2. **Pre-V35 operational window**
   - No-spend is fork-gated. Replacing the placeholder before V35 activation means malicious sentinel-sender transactions may test legacy verification paths.
3. **External integration compatibility**
   - Exchanges or wallet forms may reject non-public-key-looking recipient strings.
4. **Mixed source of truth**
   - `Seting.V35_BURN_ADDRESS` must remain the single configured value and `BurnAddressPolicy` must remain the policy entrypoint.
5. **Display confusion**
   - Users must understand `BURN_ADDRESS` is a protocol sentinel, not a wallet address with a lost key.

## 25. Rollback Plan

If sentinel replacement fails tests:

1. Keep `TODO_V35_BURN_ADDRESS` in place.
2. Do not create a burn transaction.
3. Do not activate V35.
4. Fall back to generated valid burn address plan from Phase 5D-0.
5. Keep no-spend enforcement behind V35 for the generated address.
6. Re-run burn-address, supply, block-validation, and transaction-pool tests against the generated address model.

If sentinel replacement succeeds but later external compatibility becomes unacceptable:

1. Revert the address constant before any burn transaction is created.
2. Publish no sentinel address as final.
3. Use a generated valid address instead.
4. Repeat Phase 5D-1 tests for generated address replacement.

## 26. Final Recommendation

`BURN_ADDRESS` sentinel is feasible and likely stronger than a generated address for public trust because no private key can exist for it.

The model is safe only if Phase 5D-1 proves:

- sending TO `BURN_ADDRESS` works as a normal signed transaction.
- spending FROM `BURN_ADDRESS` is rejected before public-key decoding after V35.
- malformed sentinel-sender attempts fail safely in transaction pool, block validation, balance, and rollback paths.
- supply/API/account export can display and account for the sentinel cleanly.

Recommended Phase 5D-1 path:

1. Replace the placeholder with sentinel only in wallet/server `Seting.java`.
2. Keep V35 disabled.
3. Add exhaustive sentinel recipient/sender/pool/block/balance/API tests.
4. Add minimal pre-verify guards only if tests prove an uncovered path.
5. Do not create the burn transaction until all tests pass and the final address is publicly announced.

