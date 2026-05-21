# CITU V35 Phase 5C-0 Burned Supply / Circulating Supply Accounting Plan

Phase: 5C-0 only
Scope: planning/report only
Source changes: none
V35 activation: still disabled
Burn transaction: not created

## 1. Exact files and methods inspected

Reports read:

- `CITU_V35_PHASE5A_FOUNDER_BURN_SUPPLY_POLICY_PLAN.md`
- `CITU_V35_PHASE5B0_BURN_ADDRESS_NO_SPEND_REVIEW.md`
- `CITU_V35_PHASE5B1_BURN_ADDRESS_NO_SPEND_REPORT.md`
- `CITU_V35_PHASE4E1G_JACKPOT_CONTROLLER_STORAGE_INTEGRATION_REPORT.md`

Wallet files inspected:

- `unitedStates_final-master/src/main/java/International_Trade_Union/setings/Seting.java`
- `unitedStates_final-master/src/main/java/International_Trade_Union/protocol/v35/BurnAddressPolicy.java`
- `unitedStates_final-master/src/main/java/International_Trade_Union/model/Account.java`
- `unitedStates_final-master/src/main/java/International_Trade_Union/entity/DtoTransaction/DtoTransaction.java`
- `unitedStates_final-master/src/main/java/International_Trade_Union/utils/UtilsBalance.java`
- `unitedStates_final-master/src/main/java/International_Trade_Union/utils/UtilsBlock.java`
- `unitedStates_final-master/src/main/java/International_Trade_Union/utils/UtilAccounts.java`
- `unitedStates_final-master/src/main/java/International_Trade_Union/entity/blockchain/Blockchain.java`
- `unitedStates_final-master/src/main/java/International_Trade_Union/entity/services/BlockService.java`
- `unitedStates_final-master/src/main/java/International_Trade_Union/entity/repository/EntityAccountRepository.java`
- `unitedStates_final-master/src/main/java/International_Trade_Union/controllers/BasisController.java`
- `unitedStates_final-master/src/main/java/International_Trade_Union/controllers/ConductorController.java`

Server files inspected:

- `unitedStates_storage-master/src/main/java/International_Trade_Union/setings/Seting.java`
- `unitedStates_storage-master/src/main/java/International_Trade_Union/protocol/v35/BurnAddressPolicy.java`
- `unitedStates_storage-master/src/main/java/International_Trade_Union/model/Account.java`
- `unitedStates_storage-master/src/main/java/International_Trade_Union/entity/DtoTransaction/DtoTransaction.java`
- `unitedStates_storage-master/src/main/java/International_Trade_Union/utils/UtilsBalance.java`
- `unitedStates_storage-master/src/main/java/International_Trade_Union/utils/UtilsBlock.java`
- `unitedStates_storage-master/src/main/java/International_Trade_Union/utils/UtilAccounts.java`
- `unitedStates_storage-master/src/main/java/International_Trade_Union/entity/blockchain/Blockchain.java`
- `unitedStates_storage-master/src/main/java/International_Trade_Union/entity/services/BlockService.java`
- `unitedStates_storage-master/src/main/java/International_Trade_Union/entity/repository/EntityAccountRepository.java`
- `unitedStates_storage-master/src/main/java/International_Trade_Union/controllers/BasisController.java`
- `unitedStates_storage-master/src/main/java/International_Trade_Union/controllers/ConductorController.java`

Methods and fields inspected:

- `Seting.V35_RESCUE_FORK_INDEX`
- `Seting.V35_BURN_ADDRESS`
- `Seting.ADDRESS_FOUNDER`
- `Seting.BASIS_ADDRESS`
- `Seting.V35_GOLD_REWARD`
- `Seting.V35_INSTANT_MINER_REWARD`
- `Seting.V35_JACKPOT_REWARD`
- `BurnAddressPolicy.getBurnAddress()`
- `BurnAddressPolicy.isPlaceholderBurnAddress()`
- `BurnAddressPolicy.isBurnAddress(...)`
- `BurnAddressPolicy.isNoSpendActive(...)`
- `BurnAddressPolicy.canSpendFromAddress(...)`
- `BurnAddressPolicy.isSpendForbidden(...)`
- `Account.getDigitalDollarBalance()`
- `Account.getDigitalStockBalance()`
- `Account.getDigitalStakingBalance()`
- `UtilsBalance.calculateBalance(...)`
- `UtilsBalance.calculateBalances(...)`
- `UtilsBalance.calculateBalanceWithV35JackpotContext(...)`
- `UtilsBalance.applyV35JackpotPayoutsWithChainContext(...)`
- `UtilsBalance.sendMoney(...)`
- `Blockchain.shortCheckWithCanonicalContext(...)`
- `Blockchain.checkFromFile(...)`
- `UtilAccounts.getAllBalance(...)`
- `BlockService.getTotalDigitalDollarBalance()`
- `BlockService.getTotalDigitalStakingBalance()` in server
- `BlockService.getTotalDigitalAllBalance()` in server
- `BlockService.findByAccount(...)`
- `BlockService.findAllAccounts()`
- `BlockService.countAccount()`
- `EntityAccountRepository.getTotalDigitalDollarBalance()`
- `EntityAccountRepository.getTotalDigitalStakingBalance()` in server
- `BasisController.getTotalDollars()` in server
- `BasisController.getTotalStaking()` in server
- `BasisController.getTotalAll()` in server
- `BasisController.getBalance(...)` in server

Both wallet and server still have:

- `V35_RESCUE_FORK_INDEX = Long.MAX_VALUE`
- `V35_BURN_ADDRESS = "TODO_V35_BURN_ADDRESS"`

## 2. Current balance/accounting flow

The current chain accounting flow is balance-map based, not supply-snapshot based.

`UtilsBalance.calculateBalance(...)` applies one block of transactions to a `Map<String, Account>`. It handles ordinary transfers, BASIS reward transactions, staking/stock balances, signatures, and the V35 burn-address no-spend guard added in Phase 5B-1.

`UtilsBalance.calculateBalances(...)` recalculates an account map from a block list. It delegates through the V35 jackpot-aware path when a fork index/founder address is supplied.

`UtilsBalance.calculateBalanceWithV35JackpotContext(...)` is now the safest V35-aware balance path for jackpot payout blocks. It strips jackpot payout candidates from legacy accounting and applies them only after proof-aware validation through the jackpot chain-context helpers.

`Blockchain.shortCheckWithCanonicalContext(...)` and `Blockchain.checkFromFile(...)` use the jackpot-aware balance path after Phase 4E-1G, so valid jackpot payout blocks can be credited and later spent in simulated V35 validation.

`UtilAccounts.getAllBalance(...)` sums `digitalDollarBalance` for all accounts except `Seting.BASIS_ADDRESS`. It does not exclude `V35_BURN_ADDRESS`, does not identify founder reserve, and does not distinguish total minted from circulating supply.

Account maps are exposed through `BlockService.findAllAccounts()`, `BlockService.findByAccount(...)`, and controller endpoints such as server `BasisController.getBalance(...)`.

## 3. Whether current total supply calculation exists

No explicit protocol supply calculation was found for:

- total minted
- burned supply
- circulating supply
- founder reserve
- jackpot paid
- miner rewards
- annual emission

Current totals are account-balance sums:

- Wallet `BlockService.getTotalDigitalDollarBalance()` calls `EntityAccountRepository.getTotalDigitalDollarBalance()`, which runs `SELECT SUM(e.digitalDollarBalance) FROM EntityAccount e`.
- Server `BlockService.getTotalDigitalDollarBalance()` does the same.
- Server `BlockService.getTotalDigitalStakingBalance()` sums `digitalStakingBalance`.
- Server `BlockService.getTotalDigitalAllBalance()` adds staking and dollar repository sums.
- Server `BasisController.getTotalDollars()`, `getTotalStaking()`, and `getTotalAll()` cache and expose those repository sums.

These values are useful account totals, but they are not a V35 supply model. They do not exclude the burn address, do not show burned supply, do not isolate founder reserve, and do not prove total minted from chain history.

## 4. Recommended supply accounting model

Create a V35 supply accounting model with clear public terms:

- `totalMinted`: all confirmed GOLD created by valid `BASIS_ADDRESS` emissions. This includes genesis/founder allocation, legacy rewards, V35 instant miner rewards, and V35 jackpot payouts when actually paid as BASIS transactions.
- `burnedSupply`: GOLD locked at `V35_BURN_ADDRESS`.
- `circulatingSupply`: `totalMinted - burnedSupply`.
- `founderReserve`: current GOLD balance of `Seting.ADDRESS_FOUNDER`.
- `publicFloat`: optional dashboard value, `circulatingSupply - founderReserve`.
- `jackpotPaid`: sum of valid V35 jackpot BASIS payout transactions.
- `scheduledAnnualEmission`: policy value, `1,261,440 GOLD/year`.
- `scheduledJackpotReserve`: policy value for the virtual 2 GOLD/block jackpot reserve.
- `unpaidVirtualJackpotReserve`: optional informational value, expected virtual jackpot reserve minus `jackpotPaid`.

Important semantic rule: do not double-count jackpot reserve and jackpot payout.

The project now treats the jackpot reserve as virtual until payout. Therefore the canonical ledger `totalMinted` should count actual `BASIS_ADDRESS` outputs, including jackpot payouts only when they appear as valid BASIS transactions. The annual emission policy can still show the scheduled V35 total of `8.00 GOLD/block`, but supply accounting must label scheduled emission separately from actual ledger-minted payouts.

Use `BigDecimal` or exact two-decimal normalization for monetary fields. Do not introduce new supply math with `double`.

## 5. How to calculate burned supply

Recommended primary calculation:

`burnedSupply = current GOLD balance of Seting.V35_BURN_ADDRESS in the canonical account map`

Rationale:

- The burn transaction is a normal founder-signed transfer to the burn address.
- Burn-address no-spend is enforced after V35.
- The account balance represents the current net unspendable GOLD at the burn address.
- It handles any pre-V35 incoming/outgoing edge cases by measuring current net burn balance, not just gross deposits.

Recommended audit cross-check:

- Scan canonical transaction history for valid transfers to `V35_BURN_ADDRESS`.
- Scan any outgoing transactions from `V35_BURN_ADDRESS`.
- Confirm net transaction-history burn equals the burn address account balance.

Placeholder rule:

- While `V35_BURN_ADDRESS` is `"TODO_V35_BURN_ADDRESS"`, supply accounting must not silently treat that as a final burn address.
- A Phase 5C-1 helper should return burned supply as `0` or a status such as `burnAddressConfigured=false`, and the report/API should warn that the final burn address is not configured.

Staking edge case:

- The intended founder burn is a liquid GOLD transfer to the burn address, so `digitalDollarBalance` is the expected burned amount.
- If the burn address ever has `digitalStakingBalance`, Phase 5C-1 must decide whether burned supply includes liquid only or liquid plus staking. The safest public accounting rule is to report both fields separately and define `burnedSupplyGold = burnAddress.digitalDollarBalance + burnAddress.digitalStakingBalance` only if staking at the burn address can exist in valid history.

## 6. How to calculate circulating supply

Recommended canonical formula:

`circulatingSupply = totalMinted - burnedSupply`

Recommended dashboard terms:

- `totalMinted`: all valid historical BASIS-created GOLD.
- `burnedSupply`: current net GOLD at the burn address.
- `circulatingSupply`: minted minus burned.
- `founderReserve`: founder address current balance, reported separately.
- `publicFloat`: optional stricter market-float number, `circulatingSupply - founderReserve`.

Do not hide founder reserve inside the circulating calculation unless the UI labels it clearly as a separate market-float estimate. The requested V35 model uses circulating supply as minted minus burned; founder reserve should be transparent, not silently subtracted.

## 7. How to calculate founder reserve

Recommended calculation:

`founderReserve = current GOLD balance of Seting.ADDRESS_FOUNDER in the canonical account map`

The report should also expose:

- founder address
- founder balance before burn snapshot, once chosen
- founder burn transaction hash, once broadcast
- founder balance after burn
- founder reserve percent of outstanding supply

The current founder address found in both projects is:

`nNifuwmFZr7fnV1zvmpiyQDV5z7ETWvqR6GSeqeHTY43`

The Phase 5A model used an approximate founder balance of `76.03M GOLD`, but Phase 5C-1 should calculate the exact value from the canonical account map or a chosen snapshot block before publishing supply numbers.

## 8. How to calculate jackpot emission

V35 policy after the 432-block/day correction:

- `V35_GOLD_REWARD = 8.00`
- `V35_INSTANT_MINER_REWARD = 6.00`
- `V35_JACKPOT_REWARD = 2.00`
- `432 blocks/day`
- `157,680 blocks/year`
- annual scheduled emission: `1,261,440 GOLD/year`
- annual scheduled miner emission: `946,080 GOLD/year`
- annual scheduled jackpot reserve: `315,360 GOLD/year`

Recommended accounting split:

- `jackpotPaid`: sum valid jackpot BASIS payout transactions by canonical payout amount and proof validation.
- `scheduledJackpotReserve`: blocks after V35 times `2.00 GOLD`, for policy reporting.
- `unpaidVirtualJackpotReserve`: `scheduledJackpotReserve - jackpotPaid`, if the dashboard wants to show accrued but unpaid jackpot reserve.

Do not count `scheduledJackpotReserve` as current circulating supply unless and until it is represented by a valid BASIS payout transaction. Otherwise jackpot reserve and payout would be counted twice.

## 9. How to handle burn address balance visibility

The burn address should remain visible as an account balance.

Recommended UI/API behavior:

- Show the burn address in account exports.
- Label it as `burn address` or `unspendable after V35`.
- Exclude it from `circulatingSupply`.
- Include it in `burnedSupply`.
- Keep its transaction history visible for audit proof.

Hiding the burn address from account lists would reduce auditability and make the burn harder to verify. The right fix is not to hide it, but to expose a supply snapshot that clearly subtracts it from circulation.

## 10. Required API/UI/dashboard needs

Future API/dashboard should expose a supply snapshot with:

- final burn address
- burn address configured status
- burn address current balance
- total minted
- burned supply
- circulating supply
- founder reserve
- optional public float
- jackpot paid
- scheduled jackpot reserve
- unpaid virtual jackpot reserve
- annual scheduled V35 emission
- annual miner emission
- annual jackpot reserve
- founder burn transaction hash, after execution
- founder burn block index/hash, after execution
- founder balance before and after burn

Existing endpoints such as server `/totalDollars`, `/totalStaking`, and `/totalAll` should either remain as legacy account-total endpoints or be clearly relabeled. They should not be presented as V35 circulating supply unless they exclude the burn address and define exactly what they include.

## 11. Exact implementation order for Phase 5C-1

Recommended Phase 5C-1 order:

1. Add pure V35 supply value objects, for example `V35SupplySnapshot` and `V35SupplyAccounting`.
2. Add helper methods to calculate supply from an explicit canonical `List<Block>` and/or canonical `Map<String, Account>`.
3. Add placeholder-safe burn address handling through `BurnAddressPolicy.isPlaceholderBurnAddress()`.
4. Add `totalMinted` scan from valid `BASIS_ADDRESS` GOLD outputs.
5. Add `burnedSupply` from burn address account balance plus an optional transaction-history audit cross-check.
6. Add `circulatingSupply = totalMinted - burnedSupply`.
7. Add `founderReserve` from `Seting.ADDRESS_FOUNDER` account balance.
8. Add `jackpotPaid` from canonical jackpot BASIS payouts.
9. Add scheduled V35 emission constants/reporting helpers.
10. Add wallet/server parity tests.
11. Only after pure helpers are proven, add optional service/controller endpoints.
12. Keep all behavior passive/reporting-only while V35 remains disabled.

## 12. Exact files Phase 5C-1 may edit

Recommended files for Phase 5C-1:

Wallet:

- `unitedStates_final-master/src/main/java/International_Trade_Union/protocol/v35/BurnAddressPolicy.java`
- `unitedStates_final-master/src/main/java/International_Trade_Union/protocol/v35/V35SupplyAccounting.java` new
- `unitedStates_final-master/src/main/java/International_Trade_Union/protocol/v35/V35SupplySnapshot.java` new
- `unitedStates_final-master/src/main/java/International_Trade_Union/utils/UtilsBalance.java`, only if a shared account-map helper belongs there
- `unitedStates_final-master/src/main/java/International_Trade_Union/utils/UtilAccounts.java`, only for a clearly named circulating/burn-aware helper
- `unitedStates_final-master/src/main/java/International_Trade_Union/entity/services/BlockService.java`, only if adding a supply snapshot service
- `unitedStates_final-master/src/main/java/International_Trade_Union/entity/repository/EntityAccountRepository.java`, only if adding burn/founder balance queries
- wallet tests under `src/test/java/International_Trade_Union/protocol/v35` or supply-specific test package

Server:

- `unitedStates_storage-master/src/main/java/International_Trade_Union/protocol/v35/BurnAddressPolicy.java`
- `unitedStates_storage-master/src/main/java/International_Trade_Union/protocol/v35/V35SupplyAccounting.java` new
- `unitedStates_storage-master/src/main/java/International_Trade_Union/protocol/v35/V35SupplySnapshot.java` new
- `unitedStates_storage-master/src/main/java/International_Trade_Union/utils/UtilsBalance.java`, only if a shared account-map helper belongs there
- `unitedStates_storage-master/src/main/java/International_Trade_Union/utils/UtilAccounts.java`, only for a clearly named circulating/burn-aware helper
- `unitedStates_storage-master/src/main/java/International_Trade_Union/entity/services/BlockService.java`, only if adding a supply snapshot service
- `unitedStates_storage-master/src/main/java/International_Trade_Union/entity/repository/EntityAccountRepository.java`, only if adding burn/founder balance queries
- `unitedStates_storage-master/src/main/java/International_Trade_Union/controllers/BasisController.java`, only if Phase 5C-1 includes supply API endpoints
- server tests under `src/test/java/International_Trade_Union/protocol/v35` or supply-specific test package

## 13. Exact files Phase 5C-1 should not edit

Phase 5C-1 should avoid:

- `Mining.java`
- `Block.java`
- `DtoTransaction.java`
- `Account.java`
- `UtilsBlock.java`, unless a test proves supply helpers need a validation hook
- transaction signing code
- jackpot winner/proof math, unless only reading existing constants
- PoW/tournament/sync comparators
- UI/templates
- website
- `pom.xml`

Supply accounting should be reporting/accounting logic, not a new consensus or transaction-format change.

## 14. Required tests

Required pure helper tests in wallet and server:

- `V35_RESCUE_FORK_INDEX == Long.MAX_VALUE`.
- Placeholder burn address is detected and does not silently become a real burn address.
- Burned supply is `0` or explicitly marked unconfigured while the burn address is the TODO placeholder.
- Given a final simulated burn address and account map, burned supply equals that address balance.
- Burn address remains visible in account map/account export.
- Circulating supply equals `totalMinted - burnedSupply`.
- Founder reserve equals the founder address GOLD balance.
- Optional public float equals `circulatingSupply - founderReserve`.
- Total minted sums only valid `BASIS_ADDRESS` GOLD emissions.
- Ordinary transfers do not change total minted.
- Founder-to-burn transaction changes burned/circulating calculations but not historical total minted.
- Jackpot paid sums valid canonical jackpot BASIS payouts.
- Virtual jackpot reserve is not double-counted as minted supply.
- Annual V35 emission is `1,261,440 GOLD/year`.
- Wallet/server supply helper behavior matches.

Recommended service/API tests if endpoints are added:

- Existing total-balance endpoints remain backward compatible.
- New supply endpoint returns burned, circulating, founder reserve, jackpot paid, and placeholder/final burn address status.
- Burn address account remains queryable through existing balance endpoints.

## 15. Risks

Highest risks:

1. `V35_BURN_ADDRESS` is still a placeholder, so any live burned-supply number must be marked unconfigured until Phase 5B finalizes it.
2. Existing total endpoints are account-balance totals, not circulating supply. Reusing them without relabeling would mislead users.
3. Counting jackpot virtual reserve and jackpot payout together would double-count supply.
4. Calculating burned supply from gross transfers to the burn address can be wrong if any pre-V35 outgoing burn-address transaction exists; the primary value should be net burn address balance.
5. Calculating total minted from current account balances can be wrong once burned coins are excluded. Total minted should be derived from valid BASIS emissions or from a proven equivalent snapshot.
6. Founder balance remains approximate until confirmed from canonical chain/account export.
7. Burn address staking/stock edge cases need explicit handling or clear exclusion.
8. Static cached totals in server `BasisController` can become stale and should not be used as authoritative supply snapshots.

## 16. Rollback plan

Before the real founder burn transaction:

- Supply accounting helper/API changes can be reverted safely while V35 remains disabled.
- Dashboard labels can be corrected without chain impact.
- Placeholder burn address behavior should remain conservative.

After the real founder burn transaction:

- The transaction is irreversible in normal consensus terms.
- If supply reporting is wrong, fix reporting/accounting helpers; do not attempt to spend from the burn address.
- If the burn address or no-spend policy is wrong, use a narrowly scoped emergency fork plan and public disclosure.

After V35 activation:

- If supply helpers misreport values, patch helpers and dashboards.
- If a consensus path incorrectly permits burn-address spending, pause activation rollout if possible and patch validation/admission paths before continuing.

## 17. Recommendation

Proceed to Phase 5C-1 as a pure helper/reporting implementation phase.

Phase 5C-1 should create wallet/server V35 supply helpers that calculate:

- total minted from valid BASIS emissions
- burned supply from burn address balance, with transaction-history audit cross-check
- circulating supply as total minted minus burned supply
- founder reserve from founder account balance
- jackpot paid from valid jackpot payout transactions
- scheduled annual V35 emission

The burn address must remain visible as an account. Existing balance totals should remain available but must not be treated as V35 circulating supply without burn-aware labeling and tests.
