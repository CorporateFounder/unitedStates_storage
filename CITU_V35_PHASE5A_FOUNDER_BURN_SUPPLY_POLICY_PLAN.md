# CITU V35 Phase 5A Founder Burn / Supply Policy Plan

Phase: 5A only
Scope: planning/report only
Source changes: none
V35 activation: still disabled

## 1. Exact files and methods inspected

Reports read:

- `CITU_V35_PHASE2A_REWARD_WIRING_PLAN.md`
- `CITU_V35_PHASE2B_REWARD_WIRING_REPORT.md`
- `CITU_V35_PHASE2C_REWARD_FIXTURE_TEST_REPORT.md`
- `CITU_V35_PHASE3A_POW_TARGET_MAPPING_PLAN.md`
- `CITU_V35_PHASE3B*.md`
- `CITU_V35_PHASE4*.md`, including jackpot reports through `CITU_V35_PHASE4E1G_JACKPOT_CONTROLLER_STORAGE_INTEGRATION_REPORT.md`

Wallet/server Java areas inspected:

- `unitedStates_final-master/src/main/java/International_Trade_Union/setings/Seting.java`
- `unitedStates_storage-master/src/main/java/International_Trade_Union/setings/Seting.java`
- `unitedStates_final-master/src/main/java/International_Trade_Union/model/Account.java`
- `unitedStates_storage-master/src/main/java/International_Trade_Union/model/Account.java`
- `unitedStates_final-master/src/main/java/International_Trade_Union/entity/DtoTransaction/DtoTransaction.java`
- `unitedStates_storage-master/src/main/java/International_Trade_Union/entity/DtoTransaction/DtoTransaction.java`
- `unitedStates_final-master/src/main/java/International_Trade_Union/utils/UtilsBalance.java`
- `unitedStates_storage-master/src/main/java/International_Trade_Union/utils/UtilsBalance.java`
- `unitedStates_final-master/src/main/java/International_Trade_Union/utils/UtilsBlock.java`
- `unitedStates_storage-master/src/main/java/International_Trade_Union/utils/UtilsBlock.java`
- `unitedStates_final-master/src/main/java/International_Trade_Union/utils/UtilAccounts.java`
- `unitedStates_storage-master/src/main/java/International_Trade_Union/utils/UtilAccounts.java`
- `unitedStates_final-master/src/main/java/International_Trade_Union/controllers/BasisController.java`
- `unitedStates_storage-master/src/main/java/International_Trade_Union/controllers/BasisController.java`
- `unitedStates_final-master/src/main/java/International_Trade_Union/mining/Mining.java`
- `unitedStates_storage-master/src/main/java/International_Trade_Union/mining/Mining.java`
- V35 helper package `International_Trade_Union.protocol.v35`, especially `BurnAddressPolicy`, `FounderRewardPolicy`, and `V35RewardCalculator`

Methods/fields inspected:

- `Seting.ADDRESS_FOUNDER`
- `Seting.BASIS_ADDRESS`
- `Seting.V35_BURN_ADDRESS`
- `Seting.V35_RESCUE_FORK_INDEX`
- `Seting.V35_FOUNDER_REWARD_ENABLED`
- `Seting.V35_EXPECTED_FOUNDER_REWARD`
- `DtoTransaction.verify()`
- `DtoTransaction.toSign()`
- `UtilsBalance.calculateBalance(...)`
- `UtilsBalance.calculateBalances(...)`
- `UtilsBalance.calculateBalanceWithV35JackpotContext(...)`
- `UtilsBalance.sendMoney(...)`
- `UtilsBalance.rollbackCalculateBalance(...)`
- `UtilsBlock.validationOneBlock(...)`
- `UtilsBlock.validationOneBlockWithCanonicalContext(...)`
- `BurnAddressPolicy.getBurnAddress()`
- `BurnAddressPolicy.isBurnAddress(...)`
- `BurnAddressPolicy.canSpendFromAddress(...)`
- `BasisController.getTotalDollars()`
- `BasisController.getTotalStaking()`
- `BasisController.getTotalAll()`
- `UtilAccounts.allBalance(...)`

## 2. Founder address / burn address findings

Both wallet and server currently define the same founder and BASIS constants:

- Founder address: `nNifuwmFZr7fnV1zvmpiyQDV5z7ETWvqR6GSeqeHTY43`
- BASIS address: `faErFrDnBhfSfNnj1hYjxydKNH28cRw1PBwDQEXH3QsJ`

Both projects currently define:

- `V35_RESCUE_FORK_INDEX = Long.MAX_VALUE`
- `V35_BURN_ADDRESS = "TODO_V35_BURN_ADDRESS"`
- `V35_FOUNDER_REWARD_ENABLED = false`
- `V35_EXPECTED_FOUNDER_REWARD = 0.00`

This confirms V35 remains disabled in both projects and the burn address is not final.

`BurnAddressPolicy` exists in both projects and already models the intended rule:

- `getBurnAddress()` returns `Seting.V35_BURN_ADDRESS`
- `isBurnAddress(address)` checks exact address equality
- `canSpendFromAddress(index, address)` returns false for the burn address after V35

However, this is not yet a consensus rule. It is a helper only; it is not wired into normal transaction validation, block validation, or transaction admission.

## 3. Recommended burn model

Recommended model: execute the founder burn as a normal signed GOLD transaction:

`ADDRESS_FOUNDER -> final V35_BURN_ADDRESS`

Recommended burn amount: 80% of the confirmed founder GOLD balance at a published snapshot block.

This is the cleanest model because it avoids special block formats, special transaction formats, and hidden state. It also creates a public on-chain proof that can be audited by every node, explorer, and user.

Important requirement: the final burn address must be selected before Phase 5B implementation. The placeholder `TODO_V35_BURN_ADDRESS` must not be used for any real burn or activation.

## 4. Whether burn can be a normal signed transaction

Yes, with one condition: the final burn recipient must be accepted by existing transaction and balance logic.

`DtoTransaction` signs/verifies the sender, customer, GOLD amount, stock/silver amount, law payload, and miner bonus. The recipient/customer address is part of the signed payload, but the recipient does not need to sign. `UtilsBalance.sendMoney(...)` allows a non-BASIS sender to send to a non-BASIS recipient if the sender has enough balance and the transaction signature is valid.

Therefore a founder burn can be represented as a normal transaction:

- sender: founder address
- customer: burn address
- GOLD amount: burn amount
- stock/silver amount: `0.00`
- miner bonus: normal fee/bonus as required by current transaction policy
- signature: founder key signs the existing transaction payload

No transaction format change is needed for the burn transaction itself.

## 5. Whether consensus changes are needed

The burn transaction itself does not require a consensus change if it is a normal signed transaction.

Consensus changes are still recommended for trust and safety:

1. Replace the placeholder burn address with a final, publicly documented burn address.
2. Wire `BurnAddressPolicy.canSpendFromAddress(...)` into validation so transactions whose sender is the burn address are rejected after V35.
3. Add tests proving burn-address spending cannot pass after V35 and cannot crash validation.
4. Add supply accounting helpers/endpoints so burned GOLD is excluded from circulating/outstanding supply views.

If the final burn address is not a syntactically valid public-key address, validators must reject burn-address sender transactions before `DtoTransaction.verify()` attempts to decode the sender. Otherwise a malicious burn-address spend attempt could become a decode/crash risk.

## 6. Recommended burn timing

Recommended timing: before V35 activation, after a public snapshot and final burn-address announcement.

Rationale:

- The burn can be broadcast and confirmed using existing transaction rules.
- No fork-block special case is needed.
- The burn transaction hash and block index are public before relaunch.
- V35 activation can then add the explicit no-spend rule and supply dashboard definitions.

Alternatives:

- At V35 fork block: possible, but higher risk because it couples activation, burn execution, and consensus changes.
- After V35: possible, but market trust is weaker unless no-spend enforcement and supply dashboards are already live.

## 7. Recommended burn amount

Model inputs from the requested policy:

- Total supply: about `159.55M GOLD`
- Founder balance: about `76.03M GOLD`, to be confirmed from canonical account export before execution
- Annual V35 emission: `1,261,440 GOLD/year`

Recommended:

- 80% burn of confirmed founder GOLD balance
- Approximate burn: `60.824M GOLD`
- Approximate founder reserve after burn: `15.206M GOLD`
- Approximate post-burn outstanding supply: `98.726M GOLD`

Alternative founder-balance burn models:

| Burn percent | Burn amount | Founder reserve | Post-burn outstanding supply | Inflation on post-burn supply |
| --- | ---: | ---: | ---: | ---: |
| 75% | 57.0225M | 19.0075M | 102.5275M | about 1.23% |
| 80% | 60.8240M | 15.2060M | 98.7260M | about 1.28% |
| 85% | 64.6255M | 11.4045M | 94.9245M | about 1.33% |
| 90% | 68.4270M | 7.6030M | 91.1230M | about 1.38% |

The 80% option is the recommended balance: it makes the burn meaningful while leaving a transparent reserve for liquidity, exchange support, ecosystem grants, or operational commitments.

## 8. Supply accounting model

Definitions recommended for public dashboards and API:

- Total minted: all GOLD ever minted by genesis, legacy rewards, founder rewards, miner rewards, V35 miner rewards, and V35 jackpot emission.
- Burned: sum of confirmed GOLD sent to the final burn address.
- Outstanding supply: `total minted - burned`.
- Founder reserve: current GOLD balance of `ADDRESS_FOUNDER`, reported separately.
- Public circulating float: `outstanding supply - founder reserve`, if the project wants a stricter market-float number.
- Jackpot emission: V35 jackpot reserve/payout portion; do not double-count reserve and payout.

Under the V35 432-block/day economics:

- Blocks/year: `432 * 365 = 157,680`
- Miner emission/year: `157,680 * 6 = 946,080 GOLD`
- Jackpot emission/year: `157,680 * 2 = 315,360 GOLD`
- Total annual emission: `1,261,440 GOLD`

Inflation estimates:

- Before founder burn: `1,261,440 / 159,550,000 = about 0.79%/year`
- After 80% founder burn: `1,261,440 / 98,726,000 = about 1.28%/year`

Note: the post-burn percentage is higher because the denominator is smaller, even though nominal annual emission is unchanged.

## 9. Founder reserve trust policy

Recommended founder reserve policy:

- Burn 80% of confirmed founder GOLD balance.
- Keep about 20% as a public founder reserve.
- Keep founder reward disabled after V35.
- Publish the founder reserve address and balance.
- Publish a non-consensus reserve policy: for example sale limits, vesting language, market-making limits, ecosystem grant rules, or public notice before large movements.
- Make dashboards show founder reserve separately from public circulating float.

The founder reserve policy should be social/disclosure policy unless a future phase explicitly decides to add consensus vesting or lock rules. Adding on-chain vesting now would increase consensus risk and likely require transaction-format or validation policy work beyond the burn itself.

## 10. Required UI/API/dashboard needs

Future dashboard/API should expose:

- Final burn address
- Burn transaction hash
- Burn block index and block hash
- Burned amount
- Founder balance before burn
- Founder balance after burn
- Total minted
- Burned supply
- Outstanding supply
- Founder reserve
- Public circulating float
- Annual V35 emission
- Miner annual emission
- Jackpot annual emission

Current endpoints such as `/totalDollars`, `/totalStaking`, and `/totalAll` are total-balance style endpoints backed by `BlockService` totals and static controller fields. They do not currently expose a clear burned/outstanding/founder-reserve split. `UtilAccounts.allBalance(...)` excludes only `BASIS_ADDRESS`; it does not currently exclude the burn address.

## 11. Safe implementation order for Phase 5B

Recommended Phase 5B order:

1. Confirm canonical founder GOLD balance from wallet and server account export at a chosen snapshot block.
2. Choose and publish final `V35_BURN_ADDRESS`; do not leave the TODO placeholder.
3. Add tests proving the chosen address is safe for recipient accounting and safe when used as a sender in attempted invalid spends.
4. Add or harden `BurnAddressPolicy` tests in both projects.
5. Wire burn-address no-spend enforcement behind the V35 fork gate in block validation.
6. Add transaction-admission/mempool guard if any transaction pool path can accept a burn-address sender before block validation.
7. Add pure supply accounting helpers for `totalMinted`, `burned`, `outstanding`, `founderReserve`, and optional `publicCirculatingFloat`.
8. Add wallet/server parity tests.
9. Only after code is tested and still disabled, prepare a separate operational burn transaction plan.

## 12. Exact files Phase 5B may edit

Likely allowed Phase 5B files:

- `unitedStates_final-master/src/main/java/International_Trade_Union/setings/Seting.java`
- `unitedStates_storage-master/src/main/java/International_Trade_Union/setings/Seting.java`
- `unitedStates_final-master/src/main/java/International_Trade_Union/protocol/v35/BurnAddressPolicy.java`
- `unitedStates_storage-master/src/main/java/International_Trade_Union/protocol/v35/BurnAddressPolicy.java`
- `unitedStates_final-master/src/main/java/International_Trade_Union/utils/UtilsBlock.java`
- `unitedStates_storage-master/src/main/java/International_Trade_Union/utils/UtilsBlock.java`
- `unitedStates_final-master/src/main/java/International_Trade_Union/utils/UtilsBalance.java`, only for supply/burn accounting helpers
- `unitedStates_storage-master/src/main/java/International_Trade_Union/utils/UtilsBalance.java`, only for supply/burn accounting helpers
- Transaction admission/pool files if burn-address sender can enter pending transaction pools before validation
- Supply/API controller files only if Phase 5B includes reporting endpoints
- Tests under wallet/server `src/test/java`

## 13. Exact files Phase 5B should avoid unless proven necessary

- `Mining.java`
- `Block.java`
- `Blockchain.java`, unless a supply scan/reload test proves this is the correct place
- `DtoTransaction.java`, unless burn-address sender decode safety cannot be handled earlier
- `Account.java`
- Jackpot helper classes
- PoW/tournament helper classes
- UI/templates
- website
- controller/API files if Phase 5B is limited to consensus burn enforcement
- `pom.xml`

## 14. Required tests

Required before implementation:

- Confirm `V35_RESCUE_FORK_INDEX == Long.MAX_VALUE` in both projects.
- Confirm final burn address is not the TODO placeholder.
- Confirm founder-to-burn normal transaction can be represented without changing `DtoTransaction`.
- Confirm burn transaction does not require BASIS privileges.

Required after implementation:

- Burn address cannot spend after V35.
- Burn address spend attempt before V35 preserves legacy behavior according to chosen policy.
- Normal non-burn accounts can still spend after V35.
- Founder-to-burn transaction validates as a normal signed transaction at the chosen burn timing.
- Validation rejects burn-address sender before signature decode if final burn address is a sentinel/non-public-key address.
- `totalMinted`, `burned`, `outstanding`, `founderReserve`, and circulating-float helpers produce expected values.
- Wallet/server behavior matches.
- Old pre-V35 blocks validate unchanged.

## 15. Risks

Highest risks:

1. `V35_BURN_ADDRESS` is still a placeholder. No real burn can be planned until the final address strategy is chosen.
2. If the burn address is syntactically valid, proving no private key exists is difficult. If it is syntactically invalid, sender-spend attempts must be rejected before `DtoTransaction.verify()` decodes the sender.
3. A normal burn transaction is irreversible once confirmed. There is no rollback without a hard fork.
4. Founder balance `76.03M` is a model input until confirmed from canonical chain/account export.
5. Supply terminology can confuse users. Dashboards must distinguish total minted, burned, outstanding supply, founder reserve, and public circulating float.
6. Current total endpoints and account-total utilities do not exclude the burn address from supply views.
7. If no-spend enforcement is delayed until after the public burn, market trust depends on the final burn address being demonstrably unspendable.
8. The burn policy addresses GOLD only. Founder stock/silver treatment should be documented separately.

## 16. Rollback plan

Before broadcast:

- The burn plan can be changed by choosing a new address, burn percentage, or snapshot block.
- Code changes can be reverted safely while V35 remains disabled.

After a normal burn transaction is broadcast and confirmed:

- Treat the burn as final.
- Do not attempt a compensating transaction unless the burn address is spendable, which would undermine the burn claim.
- Any mistake would require public disclosure and likely a hard fork if reversal is desired.

After V35 activation:

- If burn-address no-spend enforcement is wrong, pause activation rollout if possible.
- If already activated, use emergency fork governance and a narrowly scoped patch.

## 17. Recommendation

Proceed to Phase 5B only as a disabled, fork-gated implementation phase:

- finalize `V35_BURN_ADDRESS`
- wire burn-address no-spend enforcement after V35
- add supply accounting helpers/tests
- do not broadcast the actual founder burn transaction until founder balance, final address, and public proof process are confirmed

Recommended next phase:

Phase 5B - implement burn-address no-spend enforcement and supply accounting helpers behind the V35 fork gate, with V35 still disabled.
