# CITU V35 Phase 5D-0 Final Burn Address Generation / Replacement Plan

Phase: 5D-0 only  
Scope: operational burn-address generation/replacement plan  
Source changes: none  
V35 activation: still disabled  
Burn transaction: not created  
Transaction/block format changes: none

## 1. Exact Files And Methods Inspected

Reports read:
- `CITU_V35_PHASE5A_FOUNDER_BURN_SUPPLY_POLICY_PLAN.md`
- `CITU_V35_PHASE5B0_BURN_ADDRESS_NO_SPEND_REVIEW.md`
- `CITU_V35_PHASE5B1_BURN_ADDRESS_NO_SPEND_REPORT.md`
- `CITU_V35_PHASE5C2C_SUPPLY_API_ENDPOINTS_REPORT.md`

Wallet/server constants and policy:
- `unitedStates_final-master/src/main/java/International_Trade_Union/setings/Seting.java`
- `unitedStates_storage-master/src/main/java/International_Trade_Union/setings/Seting.java`
- `unitedStates_final-master/src/main/java/International_Trade_Union/protocol/v35/BurnAddressPolicy.java`
- `unitedStates_storage-master/src/main/java/International_Trade_Union/protocol/v35/BurnAddressPolicy.java`

Validation/accounting paths:
- `unitedStates_final-master/src/main/java/International_Trade_Union/utils/UtilsBlock.java`
- `unitedStates_storage-master/src/main/java/International_Trade_Union/utils/UtilsBlock.java`
- `unitedStates_final-master/src/main/java/International_Trade_Union/utils/UtilsUse.java`
- `unitedStates_storage-master/src/main/java/International_Trade_Union/utils/UtilsUse.java`
- `unitedStates_final-master/src/main/java/International_Trade_Union/entity/DtoTransaction/DtoTransaction.java`
- `unitedStates_storage-master/src/main/java/International_Trade_Union/entity/DtoTransaction/DtoTransaction.java`

Supply API:
- `unitedStates_storage-master/src/main/java/International_Trade_Union/controllers/V35SupplyController.java`
- `unitedStates_storage-master/src/main/java/International_Trade_Union/protocol/v35/V35SupplyApiResponseBuilder.java`

Address/key generation:
- `unitedStates_final-master/src/main/java/International_Trade_Union/utils/UtilsSecurity.java`
- `unitedStates_storage-master/src/main/java/International_Trade_Union/utils/UtilsSecurity.java`
- `unitedStates_final-master/src/main/java/International_Trade_Union/model/CreateAccount.java`
- `unitedStates_final-master/src/main/java/International_Trade_Union/model/Keys.java`
- `unitedStates_final-master/src/main/java/International_Trade_Union/controllers/CreateAccountController.java`
- `unitedStates_final-master/documentation/create-account.md`
- `unitedStates_final-master/documentationEng/create-accountEng.md`

Key methods/fields inspected:
- `Seting.V35_BURN_ADDRESS`
- `Seting.V35_RESCUE_FORK_INDEX`
- `Seting.ADDRESS_FOUNDER`
- `Seting.BASIS_ADDRESS`
- `BurnAddressPolicy.getBurnAddress()`
- `BurnAddressPolicy.isPlaceholderBurnAddress()`
- `BurnAddressPolicy.isSpendForbidden(...)`
- `UtilsSecurity.generateKeyPair()`
- `UtilsSecurity.compressed(...)`
- `UtilsSecurity.decodeKey(...)`
- `CreateAccount.create()`
- `CreateAccountController.createAccount(...)`
- `DtoTransaction.verify()`
- `DtoTransaction.toSign()`
- `UtilsBlock.validationOneBlockWithCanonicalContext(...)`
- `UtilsUse.balanceTransaction(...)`
- `V35SupplyController` endpoint methods
- `V35SupplyApiResponseBuilder.burn(...)`
- `V35SupplyApiResponseBuilder.founder(...)`

## 2. Current Placeholder Burn Address Value

Both wallet and server currently define:

- `V35_RESCUE_FORK_INDEX = Long.MAX_VALUE`
- `V35_BURN_ADDRESS = "TODO_V35_BURN_ADDRESS"`

V35 remains disabled. The final burn address has not been set.

The server supply API currently reports:
- `isBurnAddressPlaceholder = true`
- `burnAddressConfigured = false`
- `burnedGold = "0.00"`
- `burnTransactionCreated = false`

## 3. Recommended Final Burn Address Model

Recommended model: generate a syntactically valid CITU address using the existing CITU key/address format, publish the public address as the final burn address, and permanently forbid spending from it after V35 through `BurnAddressPolicy`.

Do not use a vanity/impossible/sentinel string as the final burn address for the first production burn.

Reasons:
- Normal CITU addresses are Base58-encoded compressed secp256k1 public keys.
- `DtoTransaction.verify()` Base58-decodes and decodes the sender as a secp256k1 public key before later validation checks.
- Recipient/customer addresses do not need to sign and can receive funds as ordinary transaction outputs.
- A valid generated recipient address is least likely to break normal founder-to-burn transaction handling.
- No-spend enforcement already exists behind the V35 fork gate and should remain the consensus guarantee.

Recommended public model:
- Final burn address is a valid generated CITU public address.
- Founder burn is a normal signed transaction from `Seting.ADDRESS_FOUNDER` to final `Seting.V35_BURN_ADDRESS`.
- After V35, any transaction whose sender is final `Seting.V35_BURN_ADDRESS` is invalid.

## 4. Operational Generation Procedure

The final address must be generated outside the repository and outside normal production runtime.

Recommended ceremony:

1. Prepare an offline clean machine or disposable offline environment.
2. Use the project’s address algorithm only:
   - secp256k1 keypair
   - compressed public key
   - Base58-encoded compressed public key as the address
3. Generate multiple candidate addresses offline.
4. Select one final address.
5. Record only the final public address in the public proof package.
6. Destroy the private key material immediately.
7. Do not commit, upload, paste, log, screenshot, or store the private key.
8. Reboot/wipe the disposable environment if used.
9. Independently verify the final public address is syntactically valid by decoding it with the same code path used by `DtoTransaction.verify()` sender decode.
10. Replace `TODO_V35_BURN_ADDRESS` in both wallet/server `Seting.java` only in Phase 5D-1 or later approved implementation phase.

Important: do not use the current wallet `/create-account` page for the ceremony unless the environment is controlled. `CreateAccount.create()` prints both `pubkey` and `privKey`, and `CreateAccountController` displays both as login/password. That is useful for normal user accounts but risky for a burn ceremony because it increases accidental private-key retention.

## 5. Private-Key Destruction / Non-Retention Policy

Private-key destruction is necessary but not sufficient.

Required policy:
- The burn address private key must not be retained by any person, server, repo, wallet profile, browser history, shell history, screenshot, log file, CI output, or documentation artifact.
- The generated private key must never be committed to Git.
- The final public burn address must be the only generated value carried into the codebase.
- At least two reviewers should verify that only the public address appears in the Phase 5D-1 diff.

Why destruction is not enough:
- A valid generated address cannot mathematically prove that nobody retained the private key.
- V35 consensus no-spend enforcement is still required so that even if a private key later appears, spending from the burn address is invalid after V35.

Therefore the final trust model is layered:
- operational non-retention of private key,
- public audit trail,
- V35 consensus no-spend rule.

## 6. Valid Generated Address Vs Impossible Address

Recommended: valid generated CITU address.

Advantages:
- Compatible with existing recipient/customer handling.
- Easier to use in normal founder-signed burn transaction.
- Compatible with existing account/balance maps.
- Avoids decode/crash risks if the address is ever accidentally treated as a public key.

Impossible/sentinel address is not recommended for Phase 5D-1.

Risks of impossible address:
- If used as sender, `DtoTransaction.verify()` may try to Base58-decode and decode it as a public key.
- Every transaction-pool, validation, balance, and rollback path must reject it before sender verification.
- More consensus surface is needed to make it safe.

The existing no-spend policy reduces the risk of using a valid generated address, but it does not remove the need for private-key destruction/non-retention before V35 activation.

## 7. Manual Founder Burn Procedure

Recommended burn timing: before V35 activation, after final burn address is selected and after the replacement code has been shipped but while V35 remains disabled.

Manual steps:

1. Confirm wallet and server agree on canonical chain height.
2. Confirm `V35_RESCUE_FORK_INDEX = Long.MAX_VALUE`.
3. Confirm final `V35_BURN_ADDRESS` is no longer the placeholder in both wallet and server builds.
4. Confirm `/v35/burn` reports `isBurnAddressPlaceholder=false` and `burnAddressConfigured=true`.
5. Confirm founder GOLD balance from canonical account data at a published snapshot block.
6. Calculate burn amount:
   - recommended: 80% of confirmed founder GOLD balance
   - round to canonical 2-decimal GOLD precision
7. Construct a normal transaction:
   - sender: `Seting.ADDRESS_FOUNDER`
   - customer/recipient: final `Seting.V35_BURN_ADDRESS`
   - GOLD amount: calculated burn amount
   - stock/silver amount: `0.00`
   - normal miner fee/bonus according to current transaction policy
8. Sign with founder private key.
9. Broadcast through normal transaction submission path.
10. Wait for the transaction to be included in a block and confirmed by peers.
11. Record transaction hash/signature, block index, block hash, amount, founder pre/post balance, and burn address balance.
12. Publish the public proof package.
13. Keep V35 disabled until the full activation checklist is complete.

Missed or failed burn transaction:
- If not confirmed, do not claim burn happened.
- Recalculate founder balance and burn amount if another founder transaction changes the balance before confirmation.

## 8. Public Proof Checklist

Publish a proof package containing:

- final V35 burn address
- founder address
- burn transaction hash/signature
- burn block index
- burn block hash
- burn amount
- founder balance before burn
- founder balance after burn
- burn address balance after burn
- total minted at proof block
- burned supply at proof block
- circulating/outstanding supply at proof block
- founder reserve at proof block
- annual V35 emission: `1,261,440.00 GOLD`
- V35 block time: `200 seconds`
- V35 blocks/day: `432`
- V35 reward split: `8.00 total`, `6.00 miner`, `2.00 jackpot`, `0.00 founder`
- statement that burn address private key was destroyed/not retained
- statement that after V35, spending from the burn address is consensus-invalid
- commit hash or release tag where final burn address appears in `Seting.V35_BURN_ADDRESS`

The proof must distinguish actual burned supply from modeled burn. Do not publish modeled values as completed burn values until the transaction is confirmed.

## 9. Recommended Timing Relative To V35 Activation

Recommended sequence:

1. Generate final burn address.
2. Replace placeholder in wallet/server while V35 remains disabled.
3. Verify supply API reports configured burn address.
4. Confirm founder balance at a public snapshot block.
5. Broadcast and confirm founder burn transaction before V35 activation.
6. Publish proof package.
7. Keep no-spend fork gate disabled until activation.
8. Activate V35 only after burn proof, supply API, no-spend tests, and final release checklist are complete.

Reasoning:
- Burn before activation creates public market trust before relaunch.
- Replacement before burn lets supply endpoints report the real address and actual burn balance after confirmation.
- V35 no-spend enforcement later makes any future spend attempt invalid even if a private key is claimed.

Do not combine burn execution and V35 activation in the same operational moment. That couples too many irreversible steps.

## 10. Exact Implementation Order For Phase 5D-1

Phase 5D-1 should still keep V35 disabled.

Recommended order:

1. Generate final valid CITU burn address offline.
2. Independently verify the public address format.
3. Confirm private key is destroyed/not retained.
4. Replace `TODO_V35_BURN_ADDRESS` with the final public address in both wallet and server `Seting.java`.
5. Update tests that currently expect the placeholder.
6. Add tests proving:
   - final address is not the placeholder,
   - wallet/server constants match,
   - `BurnAddressPolicy.isPlaceholderBurnAddress()` is false,
   - `BurnAddressPolicy.isBurnAddress(finalAddress)` is true,
   - no-spend remains inactive while `V35_RESCUE_FORK_INDEX = Long.MAX_VALUE`,
   - no-spend rejects final burn address with simulated active fork.
7. Update supply API tests so `/v35/burn` reports `burnAddressConfigured=true`.
8. Run wallet/server test and package builds.
9. Create report documenting final address replacement and confirming no burn transaction was created.

## 11. Files Phase 5D-1 May Edit

Wallet:
- `unitedStates_final-master/src/main/java/International_Trade_Union/setings/Seting.java`
- `unitedStates_final-master/src/test/java/International_Trade_Union/protocol/v35/BurnAddressPolicyTest.java`
- `unitedStates_final-master/src/test/java/International_Trade_Union/protocol/v35/V35ParityTest.java`
- other wallet V35 supply/burn tests only if placeholder assertions exist

Server:
- `unitedStates_storage-master/src/main/java/International_Trade_Union/setings/Seting.java`
- `unitedStates_storage-master/src/test/java/International_Trade_Union/protocol/v35/BurnAddressPolicyTest.java`
- `unitedStates_storage-master/src/test/java/International_Trade_Union/protocol/v35/V35ParityTest.java`
- `unitedStates_storage-master/src/test/java/International_Trade_Union/controllers/V35SupplyControllerTest.java`
- other server V35 supply/burn tests only if placeholder assertions exist

Report:
- `CITU_V35_PHASE5D1_FINAL_BURN_ADDRESS_REPLACEMENT_REPORT.md`

## 12. Files Phase 5D-1 Must Not Edit

Unless a later phase explicitly changes scope, Phase 5D-1 should not edit:

- `Mining.java`
- `UtilsBlock.java`
- `UtilsUse.java`
- `UtilsBalance.java`
- `DtoTransaction.java`
- `Block.java`
- `Blockchain.java`
- controllers other than tests already covering supply response behavior
- UI/templates
- website files
- transaction format
- block format
- `pom.xml`

Phase 5D-1 should not create or broadcast the founder burn transaction. It should only replace the configured address and update tests/reporting.

## 13. Required Tests Before Replacement

Before replacement:
- Confirm `V35_RESCUE_FORK_INDEX = Long.MAX_VALUE` in wallet and server.
- Confirm current `V35_BURN_ADDRESS = "TODO_V35_BURN_ADDRESS"`.
- Confirm no-spend policy is inactive while fork index is disabled.

After replacement:
- Final burn address is not `TODO_V35_BURN_ADDRESS`.
- Wallet/server final burn address constants match exactly.
- Final burn address is nonblank.
- Final burn address is accepted by project address decode logic as a compressed secp256k1 public-key address.
- `BurnAddressPolicy.isPlaceholderBurnAddress()` is false.
- `BurnAddressPolicy.isBurnAddress(finalAddress)` is true.
- `BurnAddressPolicy.isSpendForbidden(index, finalAddress, Long.MAX_VALUE)` is false.
- `BurnAddressPolicy.isSpendForbidden(forkIndex, finalAddress, simulatedForkIndex)` is true.
- Sending to final burn address remains allowed by policy.
- Spending from final burn address after simulated V35 is rejected.
- Server `/v35/burn` reports `burnAddressConfigured=true`.
- Server `/v35/supply` uses final burn address balance for `burnedGold`, even if currently `0.00`.
- No burn transaction is created by tests or code.

## 14. Risks

Highest risks:

1. Accidentally retaining or committing the burn private key.
2. Using the normal wallet account-creation UI and leaving private-key traces in logs, browser history, screenshots, clipboard, or terminal scrollback.
3. Replacing the burn address in only wallet or only server.
4. Misreporting modeled founder burn as actual burned supply.
5. Broadcasting the burn transaction before final address replacement is shipped and verified.
6. Burning the wrong percentage because founder balance changed after snapshot.
7. Treating private-key destruction as sufficient without consensus no-spend enforcement.
8. Attempting to use an impossible address without proving every sender-verification path rejects before decode.
9. Human operational error: wrong recipient, wrong amount, wrong founder key, or wrong chain/node.

## 15. Rollback Plan

Before burn transaction broadcast:
- If the generated address is suspect, discard it and generate a new address.
- If the wrong address is committed, revert the `Seting.V35_BURN_ADDRESS` change while V35 remains disabled.
- Re-run all burn policy and supply endpoint tests.

After burn transaction broadcast but before confirmation:
- If possible, stop claiming the burn and wait for chain outcome.
- Do not publish proof until confirmed.
- If transaction is rejected or expires, generate a new plan and recalculate founder balance.

After burn transaction confirmation:
- Treat burn as irreversible.
- Do not attempt to spend from the burn address.
- If amount or recipient is wrong, disclose immediately; correction would require governance/hard-fork level action and would damage trust.

After V35 activation:
- If no-spend behavior is broken, use emergency release/fork procedure.

## 16. Recommendation

Use a valid generated CITU address, not an impossible address.

Generate it offline using the same secp256k1/compressed-public-key/Base58 format, destroy or never retain the private key, publish the public address, replace `TODO_V35_BURN_ADDRESS` in wallet and server, and keep V35 disabled until all tests and public proof steps are complete.

Private-key destruction is not enough by itself. The existing V35 no-spend policy remains required as the consensus backstop.
