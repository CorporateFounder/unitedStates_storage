# CITU V35 Phase 5C-2B Supply API / Controller Wiring Plan

Phase: 5C-2B only  
Scope: read-only API/controller implementation plan  
Source behavior changes: none  
V35 activation: disabled  
Burn transaction: not created  
Transaction/block format: unchanged

## 1. Exact Files And Methods Inspected

Reports read:
- `CITU_V35_PHASE5C0_SUPPLY_ACCOUNTING_PLAN.md`
- `CITU_V35_PHASE5C1_SUPPLY_ACCOUNTING_HELPERS_REPORT.md`
- `CITU_V35_PHASE5C2A_SUPPLY_CHAIN_ACCOUNT_ADAPTER_REPORT.md`
- `CITU_V35_PHASE5A_FOUNDER_BURN_SUPPLY_POLICY_PLAN.md`
- `CITU_V35_PHASE5B1_BURN_ADDRESS_NO_SPEND_REPORT.md`

Wallet files inspected:
- `unitedStates_final-master/src/main/java/International_Trade_Union/setings/Seting.java`
- `unitedStates_final-master/src/main/java/International_Trade_Union/protocol/v35/V35SupplyCalculator.java`
- `unitedStates_final-master/src/main/java/International_Trade_Union/protocol/v35/V35SupplySnapshot.java`
- `unitedStates_final-master/src/main/java/International_Trade_Union/protocol/v35/V35SupplyChainAdapter.java`
- `unitedStates_final-master/src/main/java/International_Trade_Union/protocol/v35/BurnAddressPolicy.java`
- `unitedStates_final-master/src/main/java/International_Trade_Union/controllers/BasisController.java`
- `unitedStates_final-master/src/main/java/International_Trade_Union/controllers/ConductorController.java`
- `unitedStates_final-master/src/main/java/International_Trade_Union/controllers/BlockchainCheckController.java`
- `unitedStates_final-master/src/main/java/International_Trade_Union/entity/services/BlockService.java`
- `unitedStates_final-master/src/main/java/International_Trade_Union/entity/repository/EntityAccountRepository.java`

Server files inspected:
- `unitedStates_storage-master/src/main/java/International_Trade_Union/setings/Seting.java`
- `unitedStates_storage-master/src/main/java/International_Trade_Union/protocol/v35/V35SupplyCalculator.java`
- `unitedStates_storage-master/src/main/java/International_Trade_Union/protocol/v35/V35SupplySnapshot.java`
- `unitedStates_storage-master/src/main/java/International_Trade_Union/protocol/v35/V35SupplyChainAdapter.java`
- `unitedStates_storage-master/src/main/java/International_Trade_Union/protocol/v35/BurnAddressPolicy.java`
- `unitedStates_storage-master/src/main/java/International_Trade_Union/controllers/BasisController.java`
- `unitedStates_storage-master/src/main/java/International_Trade_Union/controllers/TransactionController.java`
- `unitedStates_storage-master/src/main/java/International_Trade_Union/entity/services/BlockService.java`
- `unitedStates_storage-master/src/main/java/International_Trade_Union/entity/repository/EntityAccountRepository.java`

Important constants confirmed in both wallet and server:
- `Seting.V35_RESCUE_FORK_INDEX = Long.MAX_VALUE`
- `Seting.V35_BURN_ADDRESS = "TODO_V35_BURN_ADDRESS"`
- `Seting.V35_TARGET_BLOCK_TIME_SECONDS = 200`
- `Seting.V35_GOLD_REWARD = 8.00`
- `Seting.V35_INSTANT_MINER_REWARD = 6.00`
- `Seting.V35_JACKPOT_REWARD = 2.00`
- `Seting.V35_EXPECTED_FOUNDER_REWARD = 0.00`
- `Seting.V35_FOUNDER_REWARD_ENABLED = false`

## 2. Existing Balance / Account / Block Info Endpoints

Server `BasisController` already exposes several read-only chain and balance-style endpoints:
- `/allAccounts`: returns account count through `blockService.countAccount()`.
- `/totalDollars`: returns cached total digital dollar/GOLD balance through `blockService.getTotalDigitalDollarBalance()`.
- `/totalStaking`: returns total staking balance through `blockService.getTotalDigitalStakingBalance()`.
- `/totalAll`: returns combined balance total through `blockService.getTotalDigitalAllBalance()`.
- `/size`: returns current blockchain size.
- `/sub-blocks`: returns stored blocks in an index range using `blockService.findBySpecialIndexBetween(...)`.
- `/block`: returns a block by special index using `blockService.findBySpecialIndex(...)`.
- `/balance`: returns one account using `blockService.findByAccount(...)`.
- `/prevBlock`: returns the latest block.
- `/version`: returns node version.

Server `TransactionController` exposes transaction-pool/history endpoints such as:
- `/addTransaction`
- `/isWait58`
- `/getTransactions`

Wallet `ConductorController` and `BasisController` expose local wallet/account-oriented data paths:
- account lookup through `blockService.findByAccount(...)`
- account-map access through `blockService.findAllAccounts()`
- local chain update and sync flows
- wallet UI/model data endpoints such as `/account`, `/dollar`, and wallet key/account views

Wallet `BlockchainCheckController` exposes validation-style checks, including `/checkValidation`.

`BlockService` in both projects provides the key data access needed for future supply endpoints:
- `findByAccount(String)`
- `findByAccountIn(...)`
- `findBYAccountString(...)`
- `findByDtoAccounts(...)`
- `findAllAccounts()`
- `countBlock()`
- `countAccount()`

Server `BlockService` also exposes aggregate repository totals:
- `getTotalDigitalDollarBalance()`
- `getTotalDigitalStakingBalance()`
- `getTotalDigitalAllBalance()`

These aggregate totals are useful existing dashboard values, but they must not be treated as V35 supply accounting by themselves.

## 3. Recommended Read-Only Supply Endpoint Design

Implement the server endpoint first as the authoritative public API:

- `GET /v35/supply`

Recommended responsibility:
- Return an actual live supply snapshot derived from canonical stored chain/account data.
- Return protocol constants needed to interpret the snapshot.
- Return burn/founder address status.
- Return clear warnings while the burn address is still a placeholder and V35 is disabled.
- Never mutate chain, settings, account balances, transactions, caches, or storage.

Recommended implementation model:
1. Controller obtains canonical account map from `blockService.findAllAccounts()`.
2. Controller obtains canonical chain blocks from storage using the current height and `blockService.findBySpecialIndexBetween(0, currentHeight)` or equivalent existing safe range access.
3. Convert entity blocks to `Block` objects using the existing block conversion utilities already used by controller/sync flows.
4. Convert stored entity accounts to `Map<String, Account>` using the existing account conversion utilities.
5. Build the snapshot with `V35SupplyChainAdapter.snapshotFromChainAndAccounts(chain, accounts)`.
6. Serialize `BigDecimal` values as strings with canonical 2-decimal GOLD precision.

Do not compute V35 supply from `/totalDollars` alone. That endpoint returns an account-balance aggregate and can include burned funds, staking, or legacy semantics. V35 supply needs:
- total minted from BASIS emissions in canonical blocks
- burned amount from burn address balance, when configured
- circulating supply as `totalMinted - burned`
- founder reserve from founder address balance
- jackpot paid from canonical jackpot BASIS payout classification

If full chain scan is too expensive, Phase 5C-2C can add a read-only supply snapshot service with caching. That optimization must be invalidated on accepted block changes and should come after the correctness-first implementation.

## 4. Recommended Protocol Endpoint Design

Add a read-only endpoint:

- `GET /v35/protocol`

Recommended fields:
- `v35Active`
- `v35ForkIndex`
- `v35ForkIndexStatus`
- `blockTimeSeconds`
- `blocksPerDay`
- `rewardPerBlock`
- `minerReward`
- `jackpotReservePerBlock`
- `founderReward`
- `founderRewardEnabled`
- `annualEmissionGold`
- `annualMinerEmissionGold`
- `annualJackpotEmissionGold`
- `burnAddress`
- `isBurnAddressPlaceholder`
- `burnAddressConfigured`

While V35 is disabled, return:
- `v35Active = false`
- `v35ForkIndex = "Long.MAX_VALUE"` or the numeric value with a separate `v35ForkIndexStatus = "DISABLED"`
- protocol constants as planned values, not active live consensus changes

This endpoint should make it easy for UI/API clients to distinguish configured protocol policy from currently active chain behavior.

## 5. Recommended Burn / Founder Endpoint Design

Recommended burn endpoint:

- `GET /v35/burn`

Recommended fields:
- `burnAddress`
- `isBurnAddressPlaceholder`
- `burnAddressConfigured`
- `burnedGold`
- `burnNoSpendActive`
- `burnNoSpendForkGated`
- `v35Active`
- `v35ForkIndex`
- `warnings`

Recommended founder endpoint:

- `GET /v35/founder`

Recommended fields:
- `founderAddress`
- `founderReserveGold`
- `founderReward`
- `founderRewardEnabled`
- `recommendedBurnPercent`
- `modeledBurn`
- `warnings`

The founder endpoint may include a clearly separated modeled burn object, but it must not merge modeled burn values into actual burned or circulating supply.

Recommended modeled burn object:
- `modelOnly = true`
- `burnPercent`
- `sourceFounderReserveGold`
- `modeledBurnGold`
- `modeledRemainingFounderReserveGold`
- `modeledPostBurnCirculatingGold`
- `warning = "Model only; no burn transaction has occurred."`

## 6. Response Field Schema

Recommended `/v35/supply` response fields:

- `totalMintedGold`
- `burnedGold`
- `circulatingGold`
- `founderReserveGold`
- `jackpotPaidGold`
- `annualEmissionGold`
- `annualMinerEmissionGold`
- `annualJackpotEmissionGold`
- `inflationOnActualCirculatingPercent`
- `burnAddress`
- `founderAddress`
- `basisAddress`
- `isBurnAddressPlaceholder`
- `burnAddressConfigured`
- `v35Active`
- `v35ForkIndex`
- `v35ForkIndexStatus`
- `currentHeight`
- `blockTimeSeconds`
- `blocksPerDay`
- `rewardPerBlock`
- `minerReward`
- `jackpotReservePerBlock`
- `founderReward`
- `founderRewardEnabled`
- `jackpotReserveIsVirtualUntilPayout`
- `snapshotSource`
- `generatedAt`
- `valid`
- `invalidReason`
- `warnings`

Precision policy:
- Return monetary values as decimal strings, not JSON floating-point numbers.
- Use 2-decimal canonical GOLD precision for public response fields.
- Keep internal calculations in `BigDecimal`.

Suggested actual values for V35 protocol constants:
- `blockTimeSeconds = 200`
- `blocksPerDay = 432`
- `rewardPerBlock = "8.00"`
- `minerReward = "6.00"`
- `jackpotReservePerBlock = "2.00"`
- `founderReward = "0.00"`
- `annualEmissionGold = "1261440.00"`
- `jackpotReserveIsVirtualUntilPayout = true`

## 7. Behavior While V35 Is Disabled

The endpoint should work before V35 activation.

While `Seting.V35_RESCUE_FORK_INDEX = Long.MAX_VALUE`:
- `v35Active` must be `false`.
- `v35ForkIndexStatus` should be `DISABLED`.
- The endpoint should not claim that V35 rewards, burn, or jackpot payouts are active.
- The endpoint may still expose V35 planned protocol constants.
- Actual supply fields must be based on current chain/account data only.
- If the burn address is still the placeholder, `burnedGold` should be `0.00`.
- Return a warning that the final V35 burn address has not been configured.

This avoids confusing planned V35 economics with already-active supply state.

## 8. Placeholder Burn Address Reporting

`Seting.V35_BURN_ADDRESS` is still:

`TODO_V35_BURN_ADDRESS`

The API must report this explicitly:
- `isBurnAddressPlaceholder = true`
- `burnAddressConfigured = false`
- `burnedGold = "0.00"`
- warning: final V35 burn address must be replaced before activation

Do not silently treat the placeholder as a real burn address. Do not read a placeholder account balance as burned supply. Do not show modeled burn as actual burned supply.

## 9. Actual Vs Modeled Supply Separation

Actual supply endpoint:
- Must show only what exists on the canonical chain and account map.
- Burned supply is the configured burn address GOLD balance.
- Circulating supply is `totalMintedGold - burnedGold`.
- Founder reserve is the founder address current GOLD balance.
- Jackpot paid is actual canonical jackpot BASIS payout total.

Modeled supply:
- May be exposed under a separate endpoint or clearly nested object.
- Must be labeled as model-only.
- Must not alter actual `burnedGold` or `circulatingGold`.

Recommended separation:
- `/v35/supply`: actual live values only.
- `/v35/founder`: actual founder reserve plus optional modeled burn section.
- `/v35/founder/model?percent=80`: optional future endpoint for model-only output.

This prevents users from mistaking the proposed 80% founder burn for a completed burn transaction.

## 10. Exact Implementation Order For Phase 5C-2C

Recommended first implementation should be server-first:

1. Add response DTOs or simple response maps for:
   - `V35SupplyResponse`
   - `V35ProtocolResponse`
   - `V35BurnResponse`
   - `V35FounderResponse`
2. Add a server read-only controller, preferably `V35Controller` or `V35SupplyController`, instead of expanding `BasisController`.
3. Implement `GET /v35/supply` using:
   - stored canonical chain range
   - stored account map
   - `V35SupplyChainAdapter`
   - `BurnAddressPolicy`
   - `Seting` constants
4. Implement `GET /v35/protocol`.
5. Implement `GET /v35/burn`.
6. Implement `GET /v35/founder`.
7. Add server controller tests for disabled V35, placeholder burn address, actual-vs-modeled separation, and chain/account-derived values.
8. Mirror wallet implementation only after server API semantics are stable, or add a wallet-internal read-only supply data method for UI use.

Files Phase 5C-2C may edit:
- Server:
  - `unitedStates_storage-master/src/main/java/International_Trade_Union/controllers/V35SupplyController.java` or similar new controller
  - `unitedStates_storage-master/src/main/java/International_Trade_Union/controllers/BasisController.java` only if adding a new controller is not compatible with project wiring
  - `unitedStates_storage-master/src/main/java/International_Trade_Union/entity/services/BlockService.java` only if a small read-only range/account helper is needed
  - `unitedStates_storage-master/src/main/java/International_Trade_Union/protocol/v35/V35SupplyChainAdapter.java` only for pure adapter convenience helpers
  - server tests
- Wallet:
  - `unitedStates_final-master/src/main/java/International_Trade_Union/controllers/V35SupplyController.java` or wallet-local equivalent later
  - `unitedStates_final-master/src/main/java/International_Trade_Union/entity/services/BlockService.java` only if a read-only helper is required
  - `unitedStates_final-master/src/main/java/International_Trade_Union/protocol/v35/V35SupplyChainAdapter.java` only for pure adapter convenience helpers
  - wallet tests

Files Phase 5C-2C should not edit:
- `Mining.java`
- `UtilsBlock.java`
- `UtilsBalance.java`, unless a tiny read-only helper is proven necessary
- `Block.java`
- `DtoTransaction.java`
- `Account.java`
- `Seting.java`, unless a later burn-address finalization phase explicitly allows it
- transaction validation/signature code
- UI/templates
- website files
- `pom.xml`

## 11. Required Tests

Server tests:
- `/v35/supply` returns successfully while V35 is disabled.
- `v35Active = false` when `V35_RESCUE_FORK_INDEX = Long.MAX_VALUE`.
- placeholder burn address reports `isBurnAddressPlaceholder = true`.
- placeholder burn address causes actual `burnedGold = "0.00"`.
- monetary fields serialize as decimal strings with 2 decimal places.
- `totalMintedGold` is derived from canonical BASIS transactions, not account-balance totals.
- normal transfers do not increase `totalMintedGold`.
- canonical jackpot payout increases `totalMintedGold` and `jackpotPaidGold`.
- `6.00` miner reward is not counted as jackpot paid.
- simulated configured burn address balance reduces circulating supply.
- actual and modeled supply fields remain separated.
- endpoint does not mutate chain/account state.

Wallet tests, if wallet endpoint or local data helper is added:
- same snapshot fields match server helper behavior.
- placeholder burn behavior matches server.
- disabled V35 behavior matches server.
- parity test for response-builder values, if response builders are mirrored.

Performance tests or manual checks:
- endpoint does not time out on a realistic block range.
- if full scan is too slow, document caching requirement before public deployment.

## 12. Risks And Rollback Plan

Highest risks:
- Full chain scanning on every API request may be expensive.
- Existing `/totalDollars` and related aggregate endpoints are not equivalent to circulating supply and could mislead if reused directly.
- Placeholder burn address must not be interpreted as a real burn account.
- Modeled founder burn values could be confused with actual burned supply unless clearly separated.
- JSON numeric precision can be unsafe if large `BigDecimal` values are emitted as floating-point numbers.
- Jackpot paid classification is amount-based on already-accepted canonical chain transactions; this is acceptable for reporting but should be documented as chain-derived reporting, not independent consensus validation.
- Wallet and server may have different controller surface areas, so server should be authoritative first.

Rollback plan:
- Remove or disable the new `/v35/*` read-only controller endpoints.
- Leave pure `V35SupplyCalculator`, `V35SupplySnapshot`, and `V35SupplyChainAdapter` helpers intact.
- No chain data, balances, transaction format, block format, mining, or validation behavior should be affected by rollback because Phase 5C-2C should remain read-only.

## Recommendation

Proceed to Phase 5C-2C with a server-first read-only `/v35/supply` endpoint plus `/v35/protocol`, `/v35/burn`, and `/v35/founder` if controller wiring is straightforward. Wallet can mirror the endpoint later or expose an internal UI supply-data method after server semantics are stable.

The endpoint must show actual live supply separately from modeled post-burn projections, and it must explicitly report that the burn address is still a placeholder while V35 remains disabled.
