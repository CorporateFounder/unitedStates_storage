# CITU V35 Phase 5E-0 Founder Balance Snapshot / Burn Amount Confirmation Plan

Phase: 5E-0 only  
Scope: planning/report only  
Source changes: none  
V35 activation: still disabled  
Burn transaction: not created  
Transaction/block format changes: none

## 1. Exact Files And Methods Inspected

Reports read:

- `CITU_V35_PHASE5A_FOUNDER_BURN_SUPPLY_POLICY_PLAN.md`
- `CITU_V35_PHASE5C2C_SUPPLY_API_ENDPOINTS_REPORT.md`
- `CITU_V35_PHASE5D1_SENTINEL_BURN_ADDRESS_REPLACEMENT_REPORT.md`

Wallet source inspected:

- `unitedStates_final-master/src/main/java/International_Trade_Union/setings/Seting.java`
  - `ADDRESS_FOUNDER`
  - `ADDRESS_FOUNDER_TEST`
  - `V35_RESCUE_FORK_INDEX`
  - `V35_BURN_ADDRESS`
  - V35 reward/emission constants
- `unitedStates_final-master/src/main/java/International_Trade_Union/protocol/v35/V35SupplyCalculator.java`
  - `annualEmissionGold()`
  - `annualMinerEmissionGold()`
  - `annualJackpotEmissionGold()`
  - `circulatingGold(...)`
  - `inflationPercent(...)`
  - `snapshot(...)`
  - `snapshotFromBalances(...)`
  - `totalMintedFromTransactions(...)`
  - `jackpotPaidFromTransactions(...)`
  - `accountGoldBalance(...)`
- `unitedStates_final-master/src/main/java/International_Trade_Union/protocol/v35/V35SupplyChainAdapter.java`
  - `snapshotFromChainAndAccounts(...)`
  - `snapshotFromTransactionsAndAccounts(...)`
  - `totalMintedGold(...)`
  - `jackpotPaidGold(...)`
  - `transactionsFromChain(...)`
- `unitedStates_final-master/src/main/java/International_Trade_Union/protocol/v35/V35SupplySnapshot.java`
  - `getTotalMintedGold()`
  - `getBurnedGold()`
  - `getCirculatingGold()`
  - `getFounderReserveGold()`
  - `getAnnualEmissionGold()`
  - `getInflationBeforeBurnPercent()`
  - `getInflationAfterBurnPercent()`
- `unitedStates_final-master/src/main/java/International_Trade_Union/utils/UtilsBalance.java`
  - `calculateBalance(...)`
  - `calculateBalances(...)`
  - `calculateBalanceWithV35JackpotContext(...)`
  - `getBalance(...)`
- `unitedStates_final-master/src/main/java/International_Trade_Union/entity/blockchain/Blockchain.java`
  - `shortCheckWithCanonicalContext(...)`
  - `checkFromFile(...)`
- `unitedStates_final-master/src/main/java/International_Trade_Union/model/Account.java`
  - `getAccount()`
  - `getDigitalDollarBalance()`
  - `send(...)`

Server source inspected:

- `unitedStates_storage-master/src/main/java/International_Trade_Union/setings/Seting.java`
  - same constants as wallet
- `unitedStates_storage-master/src/main/java/International_Trade_Union/controllers/V35SupplyController.java`
  - `supply()`
  - `protocol()`
  - `burn()`
  - `founder()`
  - `supplyContext()`
  - `canonicalChain(...)`
  - `currentHeight()`
- `unitedStates_storage-master/src/main/java/International_Trade_Union/protocol/v35/V35SupplyApiResponseBuilder.java`
  - `supply(...)`
  - `protocol(...)`
  - `burn(...)`
  - `founder(...)`
  - `baseProtocolFields(...)`
  - `v35Active(...)`
  - `forkIndexValue()`
- `unitedStates_storage-master/src/main/java/International_Trade_Union/entity/services/BlockService.java`
  - `countBlock()`
  - `findBySpecialIndexBetween(...)`
  - `findAllAccounts()`
  - `findByAccount(...)`
  - `getTotalDigitalDollarBalance()`
  - `getTotalDigitalStakingBalance()`
  - `getTotalDigitalAllBalance()`
- `unitedStates_storage-master/src/main/java/International_Trade_Union/controllers/BasisController.java`
  - `/totalDollars`
  - `/totalStaking`
  - `/totalAll`
  - `/balance`
  - cached balance/total fields
- `unitedStates_storage-master/src/main/java/International_Trade_Union/utils/UtilAccounts.java`
  - `serchAccountByAddress(...)`
  - `getAllBalance(...)`
- `unitedStates_storage-master/src/main/java/International_Trade_Union/utils/UtilsAccountToEntityAccount.java`
  - `entityAccountsToMapAccounts(...)`
  - `entityAccountToAccount(...)`

## 2. Founder Address Finding

Wallet and server match:

```text
ADDRESS_FOUNDER = nNifuwmFZr7fnV1zvmpiyQDV5z7ETWvqR6GSeqeHTY43
```

Both projects also define:

```text
ADDRESS_FOUNDER_TEST = nNifuwmFZr7fnV1zvmpiyQDV5z7ETWvqR6GSeqeHTY43
```

The founder reward after V35 remains disabled:

```text
V35_FOUNDER_REWARD_ENABLED = false
V35_EXPECTED_FOUNDER_REWARD = 0.00
```

## 3. V35 Status Confirmation

Wallet:

```text
V35_RESCUE_FORK_INDEX = Long.MAX_VALUE
V35_BURN_ADDRESS = BURN_ADDRESS
```

Server:

```text
V35_RESCUE_FORK_INDEX = Long.MAX_VALUE
V35_BURN_ADDRESS = BURN_ADDRESS
```

V35 remains disabled. The final sentinel burn address now matches in both projects.

## 4. How Founder Balance Should Be Calculated

Founder current GOLD balance should be calculated from canonical chain/account data, not from modeled inputs.

Recommended authoritative source:

1. Choose canonical node and snapshot height.
2. Read canonical stored blocks up to that height.
3. Read current account map from storage.
4. Convert entity accounts to `Map<String, Account>`.
5. Use `V35SupplyChainAdapter.snapshotFromChainAndAccounts(chain, accounts)`.
6. Read `snapshot.getFounderReserveGold()`.

Current server code already follows this shape in `V35SupplyController.supplyContext()`:

1. `blockService.countBlock()` determines current height.
2. `blockService.findBySpecialIndexBetween(0, currentHeight)` loads canonical blocks.
3. `UtilsBlockToEntityBlock.entityBlocksToBlocks(...)` converts stored blocks.
4. `blockService.findAllAccounts()` loads account balances.
5. `UtilsAccountToEntityAccount.entityAccountsToMapAccounts(...)` converts account entities.
6. `V35SupplyChainAdapter.snapshotFromChainAndAccounts(...)` builds the supply snapshot.

The founder balance field is:

```text
V35SupplySnapshot.getFounderReserveGold()
```

The server `/v35/founder` endpoint already exposes:

- `founderAddress`
- `founderReserveGold`
- `modeledBurnPercent`
- `modeledBurnGold`
- `actualBurnedGold`

## 5. Current API / Helper Sufficiency

Current API/helpers are enough to expose the current founder reserve and modeled 80% burn amount.

Current API/helpers are not enough for a final public burn confirmation package because they do not expose all of these in one immutable snapshot response:

- snapshot height
- snapshot block hash
- 75%, 80%, 85%, 90% options
- post-burn circulating supply for each option
- post-burn inflation for each option
- explicit burn-address configured status in the founder response
- exact recommended burn amount as a fixed 2-decimal value anchored to a specific height

Recommendation: do Phase 5E-1 as a small read-only founder burn snapshot helper/endpoint before manual burn.

## 6. Recommended Snapshot Procedure

### Step 1: Choose Snapshot Node

Use the canonical server node intended to publish the burn proof. The node must be fully synced and must expose consistent:

- `/v35/supply`
- `/v35/founder`
- `/v35/burn`
- `/v35/protocol`
- existing `/balance?address=<founderAddress>` if needed for cross-check

### Step 2: Choose Snapshot Height

Use the current canonical height at a specific moment.

Required data:

- `snapshotHeight`
- `snapshotBlockHash`
- `snapshotTimestamp` if available from the latest block
- `founderAddress`
- `burnAddress`

The current `/v35/founder` response does not expose height/hash. Phase 5E-1 should add a read-only snapshot endpoint or report helper that includes these fields.

### Step 3: Calculate Founder Balance

Use:

```text
founderBalance = V35SupplySnapshot.founderReserveGold
```

Cross-check with:

```text
/balance?address=ADDRESS_FOUNDER
```

and, if available, a direct storage/account export.

### Step 4: Calculate Burn Options

For each option:

```text
burnAmount = founderBalance * burnPercent / 100
remainingFounderReserve = founderBalance - burnAmount
postBurnBurnedSupply = currentBurnedSupply + burnAmount
postBurnCirculatingSupply = totalMintedGold - postBurnBurnedSupply
postBurnInflationPercent = annualEmissionGold / postBurnCirculatingSupply * 100
```

All values must be rounded/canonicalized to 2 decimal places for GOLD.

### Step 5: Publish Snapshot

Before signing the burn transaction, publish:

- snapshot height/hash
- founder balance
- burn amount
- final burn address
- burn percentage
- expected post-burn supply
- expected post-burn inflation

### Step 6: Create Manual Burn Transaction

Create a normal signed transaction:

```text
sender = ADDRESS_FOUNDER
recipient = BURN_ADDRESS
gold = confirmed burn amount
stock/silver = 0.00
```

### Step 7: Confirm And Publish Final Proof

After confirmation:

- publish burn transaction hash/signature
- publish burn block index/hash
- publish actual burned supply and founder remaining balance
- compare actual values to snapshot plan

## 7. Burn Percentage Comparison

Using the planning model from Phase 5A:

```text
modeled total minted supply = 159,550,000.00 GOLD
modeled founder balance = 76,030,000.00 GOLD
annual V35 emission = 1,261,440.00 GOLD
current burned supply before founder burn = 0.00 GOLD
```

| Burn Percent | Burn Amount | Founder Reserve After Burn | Post-Burn Circulating Supply | Annual Inflation After Burn |
| --- | ---: | ---: | ---: | ---: |
| 75% | 57,022,500.00 | 19,007,500.00 | 102,527,500.00 | ~1.23% |
| 80% | 60,824,000.00 | 15,206,000.00 | 98,726,000.00 | ~1.28% |
| 85% | 64,625,500.00 | 11,404,500.00 | 94,924,500.00 | ~1.33% |
| 90% | 68,427,000.00 | 7,603,000.00 | 91,123,000.00 | ~1.38% |

These are planning values only. The final burn must use the confirmed founder GOLD balance at the chosen snapshot height.

## 8. Recommended Final Burn Percentage

Recommendation: burn 80% of confirmed founder GOLD balance.

Rationale:

- Large enough to visibly reset supply trust.
- Leaves a meaningful but transparent founder reserve.
- Matches the policy already used by `/v35/founder` modeled burn response.
- Keeps post-burn annual emission near ~1.28% using the current model.

Alternatives:

- 75% is less aggressive and leaves a larger founder reserve, but weaker trust signal.
- 85% is stronger, but reduces operational reserve.
- 90% is the strongest burn signal, but leaves a much smaller reserve and may reduce flexibility.

## 9. Burn Amount Calculation Rule

Recommended rule:

```text
burnAmount = floor_or_canonical_2dp(founderBalanceAtSnapshotHeight * 0.80)
```

Use exact BigDecimal arithmetic and 2-decimal GOLD precision.

The burn amount should be based on the snapshot balance, not a moving live balance at some later moment.

Reason:

- A snapshot-height rule is publicly auditable.
- It avoids ambiguity if the founder balance changes after announcement.
- It makes the planned burn transaction deterministic.
- It allows users to independently verify the exact percentage.

Operational rule:

- After the snapshot is published, founder address should be frozen for discretionary outgoing transactions until the burn transaction confirms.
- If any balance-changing founder transaction occurs before the burn confirms, discard the old snapshot and take a new snapshot.

## 10. Whether Founder Balance Must Be Frozen Before Burn

Yes, operationally.

The founder account should not send or receive any discretionary transaction between:

1. published snapshot
2. burn transaction confirmation

If a balance change occurs in that window:

- publish that the snapshot is stale,
- take a new snapshot,
- recalculate burn amount,
- restart the public proof procedure.

This does not require consensus changes. It is an operational control.

## 11. Snapshot Balance Vs Live Balance

Use snapshot balance.

Do not define the burn as “80% of whatever the live founder balance is when the transaction is created,” because that makes the public plan harder to verify.

Recommended statement:

```text
The founder burn amount is exactly 80% of ADDRESS_FOUNDER GOLD balance at snapshot block <height>/<hash>, rounded to canonical 2-decimal GOLD precision.
```

## 12. Post-Burn Supply / Inflation Calculation

Use:

```text
postBurnBurnedGold = currentBurnedGold + burnAmount
postBurnCirculatingGold = totalMintedGold - postBurnBurnedGold
postBurnFounderReserveGold = founderBalanceAtSnapshot - burnAmount
postBurnInflationPercent = annualEmissionGold / postBurnCirculatingGold * 100
```

Important:

- Burned GOLD remains historically minted.
- Burned GOLD is excluded from circulating supply.
- Founder reserve remains visible separately.
- Jackpot virtual reserve is not counted as minted until payout transactions exist.

## 13. Public Proof Checklist

Before burn:

- founder address
- burn address: `BURN_ADDRESS`
- snapshot height
- snapshot block hash
- snapshot timestamp if available
- founder balance at snapshot
- total minted at snapshot
- burned supply before founder burn
- circulating supply before founder burn
- annual emission
- selected burn percentage
- exact planned burn amount
- expected founder reserve after burn
- expected burned supply after burn
- expected circulating supply after burn
- expected annual inflation after burn

Burn transaction:

- transaction sender: founder address
- transaction recipient: `BURN_ADDRESS`
- transaction GOLD amount
- transaction stock/silver amount: `0.00`
- transaction signature/hash
- transaction block index
- transaction block hash

After burn:

- founder balance after burn
- burn address balance after burn
- burned supply after burn
- circulating supply after burn
- founder reserve after burn
- actual annual inflation after burn
- confirmation count at publication time
- note that no V35 activation occurred automatically because of the burn

## 14. Whether Phase 5E-1 Is Needed

Recommendation: Phase 5E-1 is needed.

Current API/helpers are enough for a rough founder reserve and modeled 80% view, but not enough for a clean public burn snapshot.

Phase 5E-1 should add a read-only founder burn snapshot helper and/or endpoint that returns:

- `snapshotHeight`
- `snapshotBlockHash`
- `snapshotSource`
- `founderAddress`
- `burnAddress`
- `burnAddressConfigured`
- `v35Active`
- `v35ForkIndex`
- `founderBalanceGold`
- `totalMintedGold`
- `currentBurnedGold`
- `currentCirculatingGold`
- `annualEmissionGold`
- options for 75%, 80%, 85%, 90%:
  - `burnPercent`
  - `burnAmountGold`
  - `remainingFounderReserveGold`
  - `postBurnBurnedGold`
  - `postBurnCirculatingGold`
  - `postBurnInflationPercent`
- `recommendedBurnPercent = 80`
- `recommendedBurnAmountGold`
- `modelOnly = true`
- `burnTransactionCreated = false`

It should be read-only and must not create the burn transaction.

## 15. Exact Implementation Order If Phase 5E-1 Is Needed

1. Add pure value/helper class in `International_Trade_Union.protocol.v35`, for example:
   - `V35FounderBurnSnapshot`
   - `V35FounderBurnSnapshotCalculator`
2. Reuse `V35SupplySnapshot` and `V35SupplyCalculator`.
3. Add a server read-only endpoint, for example:
   - `GET /v35/founder/burn-snapshot`
4. Build the snapshot from the same canonical chain/account context as `V35SupplyController.supplyContext()`.
5. Include snapshot height and latest block hash.
6. Return all burn options and the 80% recommendation.
7. Add server tests for:
   - all percentage options,
   - exact 2-decimal amounts,
   - snapshot height/hash fields,
   - V35 disabled status,
   - `burnAddress = BURN_ADDRESS`,
   - no mutation / read-only behavior.
8. Add wallet/server pure helper parity tests if the helper is mirrored in wallet.
9. Do not create or sign the burn transaction.

## 16. Files Phase 5E-1 May Edit

Likely allowed:

- `unitedStates_final-master/src/main/java/International_Trade_Union/protocol/v35/V35FounderBurnSnapshot.java`
- `unitedStates_storage-master/src/main/java/International_Trade_Union/protocol/v35/V35FounderBurnSnapshot.java`
- `unitedStates_final-master/src/main/java/International_Trade_Union/protocol/v35/V35FounderBurnSnapshotCalculator.java`
- `unitedStates_storage-master/src/main/java/International_Trade_Union/protocol/v35/V35FounderBurnSnapshotCalculator.java`
- `unitedStates_storage-master/src/main/java/International_Trade_Union/controllers/V35SupplyController.java`
- `unitedStates_storage-master/src/main/java/International_Trade_Union/protocol/v35/V35SupplyApiResponseBuilder.java`, if extending existing response builder
- wallet/server tests under `src/test/java/International_Trade_Union/protocol/v35`
- server controller tests under `src/test/java/International_Trade_Union/controllers`
- `CITU_V35_PHASE5E1_...REPORT.md`

## 17. Files Phase 5E-1 Must Not Edit

- `Mining.java`
- `DtoTransaction.java`
- `Block.java`
- `Blockchain.java`, unless a read-only latest hash accessor is impossible elsewhere
- `UtilsBlock.java`
- `UtilsBalance.java`, unless a tiny pure helper is proven necessary
- transaction format classes
- block format classes
- UI/templates
- website
- `pom.xml`

## 18. Required Tests

Pure helper tests:

- 75%, 80%, 85%, 90% burn options are calculated correctly.
- recommended burn percent is 80.
- burn amount uses 2-decimal BigDecimal precision.
- remaining founder reserve is `founderBalance - burnAmount`.
- post-burn circulating supply is `totalMinted - (currentBurned + burnAmount)`.
- post-burn inflation uses `annualEmission / postBurnCirculating * 100`.
- negative/null inputs produce safe invalid result or zeroed model.

Controller/service tests:

- endpoint is read-only.
- endpoint reports `V35_RESCUE_FORK_INDEX = Long.MAX_VALUE`.
- endpoint reports `V35_BURN_ADDRESS = BURN_ADDRESS`.
- endpoint includes snapshot height.
- endpoint includes latest block hash when available.
- endpoint separates actual supply from modeled burn.
- endpoint does not mark burn transaction as created.
- endpoint handles empty chain/account data safely.

Parity tests:

- wallet/server helper behavior matches if helpers are mirrored.

## 19. Risks

Highest risks:

1. Founder balance can change between snapshot and burn transaction confirmation.
2. Current `/v35/founder` lacks snapshot height/hash, so it is not ideal as final proof by itself.
3. Full-chain scanning in `/v35/supply` style endpoints may be expensive on production nodes.
4. Public supply language can confuse total minted, burned supply, circulating supply, and founder reserve.
5. Sentinel `BURN_ADDRESS` is configured, but live no-spend enforcement remains disabled until V35 activation.
6. If any undisclosed founder transaction occurs after snapshot, burn amount trust is weakened unless snapshot is restarted.
7. The manual burn transaction is irreversible once confirmed.

## 20. Rollback Plan

Before manual burn:

- abandon the snapshot if any input is wrong,
- publish a corrected snapshot,
- do not create or broadcast burn transaction,
- keep V35 disabled.

If founder balance changes after snapshot:

- declare the snapshot stale,
- take a new snapshot,
- recalculate all burn options,
- use the new snapshot as the public proof anchor.

After burn transaction confirmation:

- treat the burn as final,
- do not attempt reversal,
- publish corrected proof if only reporting metadata was wrong.

If Phase 5E-1 endpoint/helper is wrong:

- disable use of that endpoint for public proof,
- rely on manually audited account export while fixing the read-only helper,
- do not broadcast burn until fixed.

## 21. Final Recommendation

Use a snapshot-height rule:

```text
Burn exactly 80% of ADDRESS_FOUNDER GOLD balance at the published snapshot block height/hash.
```

Do Phase 5E-1 before manual burn:

- add a read-only founder burn snapshot helper/endpoint,
- include 75/80/85/90 options,
- include snapshot height/hash,
- keep V35 disabled,
- do not create the burn transaction yet.

