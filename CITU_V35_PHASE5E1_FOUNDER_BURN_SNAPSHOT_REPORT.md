# CITU V35 Phase 5E-1 Founder Burn Snapshot Report

Phase: 5E-1 only  
Scope: read-only founder burn snapshot endpoint/helper  
V35 activation: still disabled  
Burn transaction: not created  
Production mutation: none  
Transaction/block format changes: none

## 1. Exact Files Changed

Server source:

- `unitedStates_storage-master/src/main/java/International_Trade_Union/controllers/V35SupplyController.java`
- `unitedStates_storage-master/src/main/java/International_Trade_Union/protocol/v35/V35SupplyApiResponseBuilder.java`

Server tests:

- `unitedStates_storage-master/src/test/java/International_Trade_Union/controllers/V35SupplyControllerTest.java`

Report:

- `CITU_V35_PHASE5E1_FOUNDER_BURN_SNAPSHOT_REPORT.md`

No wallet source files were changed.

## 2. Endpoint / Helper Added

Added server read-only endpoint:

```text
GET /v35/founder/burn-snapshot
```

Added response-builder helper:

```text
V35SupplyApiResponseBuilder.founderBurnSnapshot(V35SupplySnapshot snapshot, long snapshotHeight, String snapshotBlockHash)
```

The endpoint reuses the existing read-only `V35SupplyController.supplyContext()` path and does not mutate chain, balances, settings, transactions, or blocks.

## 3. Response Fields

The endpoint returns:

- `founderAddress`
- `burnAddress`
- `burnAddressConfigured`
- `isBurnAddressPlaceholder`
- `snapshotHeight`
- `snapshotBlockHash`
- `founderBalanceGold`
- `totalMintedGold`
- `actualBurnedGold`
- `actualCirculatingGold`
- `annualEmissionGold`
- `v35Active`
- `v35ForkIndex`
- `recommendedBurnPercent`
- `burnTransactionCreated`
- `modelOnly`
- `burnOptions`
- `proofChecklist`
- `note`

Each burn option returns:

- `percent`
- `recommended`
- `burnGold`
- `founderReserveAfterBurnGold`
- `modeledBurnedGoldAfterBurn`
- `modeledCirculatingGoldAfterBurn`
- `modeledInflationAfterBurnPercent`

The proof checklist includes:

- `founderAddress`
- `burnAddress`
- `snapshotHeight`
- `snapshotBlockHash`
- `founderBalanceBeforeBurn`
- `burnAmount`
- `burnTxHash`
- `burnBlockIndex`
- `burnBlockHash`
- `founderBalanceAfterBurn`
- `burnedSupply`
- `circulatingSupply`
- `annualEmission`
- `inflationAfterBurn`

Pending burn transaction fields use:

```text
PENDING_MANUAL_BURN
```

## 4. Snapshot Height / Hash

`snapshotHeight` is derived from:

```text
blockService.countBlock() - 1
```

If no blocks exist, the endpoint reports:

```text
snapshotHeight = -1
```

`snapshotBlockHash` is derived from the highest-index block returned by the canonical chain read:

```text
blockService.findBySpecialIndexBetween(0, currentHeight)
```

If the chain is empty or the latest block hash is missing, the endpoint reports:

```text
snapshotBlockHash = UNKNOWN
```

## 5. Founder Balance Calculation

Founder balance is calculated from the existing `V35SupplySnapshot`:

```text
founderBalanceGold = snapshot.getFounderReserveGold()
```

The snapshot is built from:

- canonical chain blocks loaded through `BlockService`
- account map loaded through `blockService.findAllAccounts()`
- `V35SupplyChainAdapter.snapshotFromChainAndAccounts(...)`

The founder address remains:

```text
nNifuwmFZr7fnV1zvmpiyQDV5z7ETWvqR6GSeqeHTY43
```

## 6. Burn Option Calculation

The endpoint returns modeled options for:

- `75%`
- `80%`
- `85%`
- `90%`

`80%` is marked as the recommended option:

```text
recommendedBurnPercent = 80
```

Each option uses exact BigDecimal arithmetic:

```text
burnGold = founderBalanceGold * percent / 100
founderReserveAfterBurnGold = founderBalanceGold - burnGold
modeledBurnedGoldAfterBurn = actualBurnedGold + burnGold
modeledCirculatingGoldAfterBurn = totalMintedGold - modeledBurnedGoldAfterBurn
modeledInflationAfterBurnPercent = annualEmissionGold / modeledCirculatingGoldAfterBurn * 100
```

GOLD values are emitted as 2-decimal string-safe values. Percent outputs use 4 decimal places.

## 7. Actual Vs Modeled Supply

The endpoint keeps actual supply separate from modeled burn outcomes:

- `actualBurnedGold` remains the current burn-address balance.
- `actualCirculatingGold` remains current `totalMintedGold - actualBurnedGold`.
- modeled fields appear only inside `burnOptions` and `proofChecklist`.
- `burnTransactionCreated = false`.
- `modelOnly = true`.

The endpoint does not pretend the burn already happened.

## 8. V35 / Burn Address Confirmation

Confirmed in wallet and server:

```text
V35_RESCUE_FORK_INDEX = Long.MAX_VALUE
V35_BURN_ADDRESS = BURN_ADDRESS
```

The endpoint reports:

- `v35Active = false`
- `v35ForkIndex = Long.MAX_VALUE`
- `burnAddress = BURN_ADDRESS`
- `burnAddressConfigured = true`
- `isBurnAddressPlaceholder = false`

## 9. Burn Transaction Confirmation

No burn transaction was created, signed, broadcast, or stored.

No private key was generated or stored.

No balances, blocks, transactions, settings, or chain state were mutated.

## 10. Tests Added / Updated

Updated server `V35SupplyControllerTest`.

Coverage added:

- `/v35/founder/burn-snapshot` reports configured `BURN_ADDRESS`.
- endpoint reports `burnAddressConfigured = true`.
- endpoint reports `isBurnAddressPlaceholder = false`.
- endpoint reports `V35_RESCUE_FORK_INDEX = Long.MAX_VALUE`.
- endpoint reports `v35Active = false`.
- endpoint reports `annualEmissionGold = 1261440.00`.
- endpoint is read-only against the `BlockService` test double.
- helper returns `75/80/85/90` burn options.
- `80%` option is marked recommended.
- burn amount is based on snapshot founder balance.
- modeled values are separate from actual burned/circulating values.
- monetary JSON fields are string-safe.
- proof checklist fields exist.
- burn transaction fields remain pending.

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

- `.\mvnw.cmd test`: PASS, 180 tests
- `.\mvnw.cmd clean package`: PASS, 180 tests during package

Wallet:

- `.\mvnw.cmd test`: PASS, 160 tests
- `.\mvnw.cmd clean package`: PASS, 160 tests during package

The existing caught `FileNotFoundException` for `\server\resources\error\error.txt` still appears during server validation tests. It is caught by existing logging code and did not fail tests or builds.

No `pom.xml` changes were made.

## 13. Behavior Confirmation

Confirmed:

- V35 remains disabled.
- `BURN_ADDRESS` remains configured.
- No burn transaction was created.
- No mining behavior changed.
- No validation behavior changed.
- No live balance behavior changed.
- Transaction format unchanged.
- Block format unchanged.
- `Mining.java` was not changed.
- `UtilsBlock.java` was not changed.
- UI/templates were not changed.
- Website files were not changed.
- Wallet source was not changed.

## 14. Remaining Risks Before Manual Burn

1. The snapshot endpoint is only as authoritative as the node's canonical chain/account state.
2. `snapshotBlockHash` is `UNKNOWN` if the canonical chain read returns no block data.
3. The founder account should be operationally frozen between public snapshot and burn confirmation.
4. If founder balance changes after snapshot, the snapshot must be discarded and regenerated.
5. The endpoint is read-only and does not verify or create the eventual burn transaction.
6. External wallet/exchange tooling still needs to support recipient `BURN_ADDRESS`.
7. The actual founder burn remains a manual operational step.

## 15. Recommended Next Phase

Phase 5F - wallet/site V35 dashboard planning, no code changes.

Recommended focus:

- decide how wallet/site should display actual versus modeled supply,
- include the founder burn proof checklist,
- keep burn creation/manual signing separate from read-only reporting,
- keep V35 disabled until the burn and activation checklists are complete.
