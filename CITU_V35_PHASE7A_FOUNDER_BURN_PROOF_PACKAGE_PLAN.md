# CITU V35 Phase 7A Founder Burn Proof Package Plan

Phase: 7A only  
Scope: founder burn public proof package plan  
Code changes: none  
V35 activation: not performed  
Founder burn transaction: not created  
Jackpot payout transaction: not created

## 1. Exact Reports / Files Read

Required reports:

- `CITU_V35_PHASE6E_RELEASE_ROLLBACK_EXCHANGE_CHECKLIST.md`
- `CITU_V35_PHASE6F1_FINAL_FORK_INDEX_ACTIVATION_PLAN.md`
- `CITU_V35_PHASE5E1_FOUNDER_BURN_SNAPSHOT_REPORT.md`
- `CITU_V35_PHASE5D1_SENTINEL_BURN_ADDRESS_REPLACEMENT_REPORT.md`
- `CITU_V35_PHASE5C2C_SUPPLY_API_ENDPOINTS_REPORT.md`
- `CITU_V35_PHASE5G2B_WALLET_JACKPOT_LIVE_API_REPORT.md`

Related V35 reports reviewed:

- `CITU_V35_PHASE2B_REWARD_WIRING_REPORT.md`
- `CITU_V35_PHASE5A_FOUNDER_BURN_SUPPLY_POLICY_PLAN.md`
- `CITU_V35_PHASE5B1_BURN_ADDRESS_NO_SPEND_REPORT.md`
- `CITU_V35_PHASE5F1_WALLET_V35_DASHBOARD_REPORT.md`
- `CITU_V35_PHASE5F2_WEBSITE_V35_PUBLIC_PAGES_REPORT.md`

Constants inspected:

- `unitedStates_final-master/src/main/java/International_Trade_Union/setings/Seting.java`
- `unitedStates_storage-master/src/main/java/International_Trade_Union/setings/Seting.java`

Confirmed:

- Wallet: `V35_RESCUE_FORK_INDEX = Long.MAX_VALUE`
- Server: `V35_RESCUE_FORK_INDEX = Long.MAX_VALUE`
- Wallet: `V35_BURN_ADDRESS = "BURN_ADDRESS"`
- Server: `V35_BURN_ADDRESS = "BURN_ADDRESS"`
- Wallet founder address: `nNifuwmFZr7fnV1zvmpiyQDV5z7ETWvqR6GSeqeHTY43`
- Server founder address: `nNifuwmFZr7fnV1zvmpiyQDV5z7ETWvqR6GSeqeHTY43`

## 2. Current Burn Readiness Summary

Current state:

- V35 remains disabled.
- The real fork index has not been selected.
- `BURN_ADDRESS` is configured in wallet and server.
- `BURN_ADDRESS` is a sentinel/protocol sink, not a normal wallet key.
- Sending TO `BURN_ADDRESS` is allowed.
- Spending FROM `BURN_ADDRESS` is blocked after V35 by fork-gated consensus policy.
- Founder reward after V35 is `0.00`.
- The recommended founder burn is `80%` of the snapshot founder GOLD balance.
- The server exposes read-only supply, burn, founder, and founder burn snapshot endpoints.
- Wallet dashboard and website pages already explain actual versus modeled burn values.
- The actual founder burn transaction has not been created, signed, broadcast, confirmed, or publicly proven.

The burn proof package must be prepared before manual execution so the burn is auditable and does not depend on moving live balance claims.

## 3. Burn Prerequisites

Do not perform the manual founder burn until all prerequisites below are satisfied:

- Phase 6 hardening is accepted.
- Simulated V35 fork dry-run remains passing.
- Historical replay against a copied live chain is completed, or explicitly accepted as a release gate that must be completed before activation.
- Release/rollback checklist is ready.
- Final fork-index activation plan is ready.
- Exchange/node communication plan is ready.
- Founder address is confirmed:
  - `nNifuwmFZr7fnV1zvmpiyQDV5z7ETWvqR6GSeqeHTY43`
- Burn address is confirmed:
  - `BURN_ADDRESS`
- `/v35/founder/burn-snapshot` endpoint is verified on the selected authoritative node.
- `/v35/supply` endpoint is verified on the selected authoritative node.
- `/v35/burn` endpoint is verified on the selected authoritative node.
- `/v35/founder` endpoint is verified on the selected authoritative node.
- Founder account operations are frozen before snapshot.
- Clean git status is confirmed.
- Live node chain/account backup is created.
- Public announcement draft is ready.
- Public proof page/template is ready.
- Operators agree that burn is irreversible.
- The burn transaction creation path has been rehearsed with a non-production or test transaction where feasible.

## 4. Snapshot Proof Fields

The snapshot proof package must record the founder balance at one specific canonical chain point.

Required snapshot fields:

- `snapshotHeight`
- `snapshotBlockHash`
- `snapshotTimestamp`, if available from the chain/block data
- `snapshotSourceNode`
- `snapshotGeneratedAtUtc`
- `founderAddress`
- `founderBalanceGoldAtSnapshot`
- `burnAddress`
- `recommendedBurnPercent = 80`
- `calculatedBurnGold`
- `remainingFounderBalanceAfterBurnGold`
- `totalMintedGoldBeforeBurn`
- `burnedGoldBeforeBurn`
- `circulatingGoldBeforeBurn`
- `founderReserveGoldBeforeBurn`
- `annualEmissionGold = 1261440.00`
- `v35ForkIndex`
- `v35Active`

Required calculation:

```text
calculatedBurnGold = founderBalanceGoldAtSnapshot * 80 / 100
remainingFounderBalanceAfterBurnGold = founderBalanceGoldAtSnapshot - calculatedBurnGold
```

Rules:

- Use snapshot founder balance, not a moving live balance.
- If founder balance changes after snapshot and before burn, discard the snapshot and generate a new one.
- Use BigDecimal/string-safe 2-decimal GOLD values from the endpoint output.
- Archive raw API output from `/v35/founder/burn-snapshot`.

## 5. Burn Transaction Proof Fields

After the manual burn is executed, the public proof package must include:

- `burnTxHash`
- `burnBlockIndex`
- `burnBlockHash`
- `burnBlockTimestamp`, if available
- `sender = founderAddress`
- `recipient = BURN_ADDRESS`
- `amountGold`
- `feeGold` or miner bonus, if applicable
- `digitalStockBalance = 0.00`, if visible in transaction details
- `confirmationCount`
- `transactionValidationStatus`
- `blockValidationStatus`
- `burnAddressNoSpendPolicy`
- proof note that spending FROM `BURN_ADDRESS` is blocked after V35
- proof note that sending TO `BURN_ADDRESS` is allowed

The transaction must be a normal founder-signed transaction:

```text
founderAddress -> BURN_ADDRESS
```

Do not combine this burn transaction with fork-index activation changes. The burn transaction is an on-chain action; the fork-index change is a separate release action.

## 6. After-Burn Verification Fields

After the burn confirms, archive and publish after-burn values:

- `founderBalanceGoldAfterBurn`
- `burnAddressBalanceGoldAfterBurn`
- `totalMintedGold`
- `burnedGold`
- `circulatingGold`
- `founderReserveGold`
- `jackpotPaidGold`
- `annualEmissionGold`
- `inflationAfterBurnPercent`, if calculated
- `burnTransactionCreated = true`, if detectable from proof/explorer context
- `burnAddressConfigured = true`
- `isBurnAddressPlaceholder = false`
- `v35Active`
- `v35ForkIndex`

API outputs to archive as raw JSON:

- `GET /v35/supply`
- `GET /v35/burn`
- `GET /v35/founder`
- `GET /v35/founder/burn-snapshot`

Recommended archive filenames:

```text
v35-burn-snapshot-before.json
v35-supply-before.json
v35-burn-before.json
v35-founder-before.json
v35-burn-transaction.json
v35-supply-after.json
v35-burn-after.json
v35-founder-after.json
v35-founder-burn-snapshot-after.json
```

## 7. Public Proof Page Structure

Recommended public document/page sections:

### Summary

- One-paragraph statement of the founder burn.
- Burn amount.
- Burn address.
- Burn transaction hash.
- Burn block index/hash.
- Statement that burn is irreversible.

### Why The Burn Is Being Done

- Founder reward is removed after V35.
- Burn reduces founder-controlled supply.
- Burn improves transparency before activation.
- Burned coins remain historically minted but are excluded from circulating supply.

### Snapshot Proof

- Snapshot height.
- Snapshot block hash.
- Snapshot timestamp, if available.
- Founder address.
- Founder balance at snapshot.
- Recommended burn percent.
- Calculated burn amount.
- Remaining founder reserve after burn.
- Raw snapshot API JSON link.

### Burn Transaction Proof

- Burn transaction hash.
- Burn block index/hash.
- Sender.
- Recipient.
- Amount.
- Fee/miner bonus, if applicable.
- Confirmation count.
- Raw transaction/explorer/API link.

### Supply Before / After

- Total minted before/after.
- Burned supply before/after.
- Circulating supply before/after.
- Founder reserve before/after.
- Annual emission.
- Inflation estimate after burn.
- Clear statement that actual burned supply is based on `BURN_ADDRESS` balance, not modeled projections.

### Founder Reward Removed After V35

- V35 founder reward is `0.00`.
- Miner reward is `6.00`.
- Jackpot reserve is `2.00`.
- Total reward is `8.00`.

### Burn Address Explanation

- `BURN_ADDRESS` is a protocol sink.
- It is not a normal wallet key.
- Sending TO `BURN_ADDRESS` is allowed.
- Spending FROM `BURN_ADDRESS` is blocked after V35 by consensus policy.

### How Anyone Can Verify

- Check the founder address.
- Check the snapshot block.
- Check the transaction sender/recipient/amount.
- Check the burn block.
- Check `/v35/supply`.
- Check `/v35/burn`.
- Check `/v35/founder`.
- Confirm post-burn founder reserve and burn address balance.

### Risk / Disclaimer

- Burn is irreversible.
- Protocol upgrade notice, not financial advice.
- Activation still depends on block index and release process.
- If any proof field is inconsistent, fork activation should be delayed.

### Links / Placeholders

- Explorer link placeholder.
- Raw API JSON archive links.
- Screenshot archive links.
- Release announcement link.
- Exchange/operator notice link.

## 8. Exchange / Node Burn Note

Recommended exchange/node note:

```text
The founder burn is a normal founder-signed transaction from the founder address to BURN_ADDRESS.
It does not require user action and does not change user balances.
BURN_ADDRESS is a protocol sink. Sending TO BURN_ADDRESS is allowed. Spending FROM BURN_ADDRESS is blocked after V35 by consensus policy.
Founder reward after V35 is 0.00 GOLD.
The fork index is separate from the burn transaction and will be announced as an exact block index.
Exchanges may choose to pause deposits/withdrawals around the fork window according to their own risk policy.
```

Include in exchange package:

- Founder address.
- Burn address.
- Burn transaction hash after execution.
- Burn block index/hash after confirmation.
- Exact V35 fork index when selected later.
- Upgrade deadline.
- Supply API links.
- Support contact placeholder.

## 9. Timing Recommendation

Options:

### Option A: Burn Before V35 Announcement

Pros:

- Burn proof exists before announcement.
- Public supply messaging can include actual burn immediately.

Cons:

- Burn is irreversible before release schedule is public.
- If hardening or exchange coordination slips, burn has already happened.

### Option B: Announce V35 First, Then Burn Before Fork

Pros:

- Operators see the full plan before irreversible burn.
- Burn can happen during the upgrade window.
- Proof can be published before the fork block.
- Fork can still be delayed if proof is incomplete or inconsistent.

Cons:

- Requires careful sequencing during the release window.
- Public messaging must clearly say burn is planned until transaction confirms.

### Option C: Burn After Fork

Pros:

- Avoids pre-fork irreversible burn.

Cons:

- Weakens trust at activation.
- Leaves founder reserve unresolved when V35 starts.
- Conflicts with the stated goal of burn proof before activation.

Recommended safest sequence:

1. Finish hardening and replay gates.
2. Announce V35 and fork plan.
3. Freeze founder address operations.
4. Snapshot founder balance.
5. Burn `80%` of snapshot founder GOLD balance to `BURN_ADDRESS`.
6. Publish proof package.
7. Verify `/v35/supply`, `/v35/burn`, `/v35/founder`, and `/v35/founder/burn-snapshot`.
8. Continue toward final fork-index activation.

If any burn proof field is missing or inconsistent, delay fork activation.

## 10. Safety Checklist

Mistakes to avoid:

- Burning before hardening is accepted.
- Burning before historical replay is completed or accepted as a release gate.
- Burning from a moving live balance without a recorded snapshot.
- Using the wrong founder wallet.
- Sending to any recipient other than exact `BURN_ADDRESS`.
- Sending the wrong amount.
- Forgetting transaction fee/miner bonus effects.
- Creating a burn transaction before public proof structure is ready.
- Publishing modeled burn values as actual burned supply.
- Changing `V35_RESCUE_FORK_INDEX` in the same step as burn execution.
- Announcing a wall-clock date without exact block index.
- Treating explorer screenshots as sufficient without raw API/block data.
- Allowing founder balance changes between snapshot and burn.
- Not archiving raw before/after API responses.
- Not warning exchanges that burn and fork index are separate events.

Pre-broadcast checklist:

- [ ] Founder address confirmed.
- [ ] Burn address exact string confirmed: `BURN_ADDRESS`.
- [ ] Snapshot height/hash recorded.
- [ ] Founder balance at snapshot recorded.
- [ ] Burn amount calculated from snapshot balance.
- [ ] Remaining founder reserve calculated.
- [ ] Transaction recipient checked manually.
- [ ] Transaction amount checked manually.
- [ ] Fee/miner bonus checked.
- [ ] No fork-index code change is included in the burn step.
- [ ] Public proof draft is ready.

Post-confirmation checklist:

- [ ] Burn transaction hash recorded.
- [ ] Burn block index/hash recorded.
- [ ] Confirmation count recorded.
- [ ] Founder balance after burn verified.
- [ ] `BURN_ADDRESS` balance after burn verified.
- [ ] `/v35/supply` archived.
- [ ] `/v35/burn` archived.
- [ ] `/v35/founder` archived.
- [ ] `/v35/founder/burn-snapshot` archived.
- [ ] Proof page published.
- [ ] Exchange/node note updated.

## 11. Manual Verification Placeholders

Exact wallet commands are not defined in this phase. Use placeholders until the operator confirms the production wallet workflow.

Founder balance snapshot:

```text
PLACEHOLDER: query authoritative node /v35/founder/burn-snapshot
PLACEHOLDER: archive raw JSON response
PLACEHOLDER: record snapshot height/hash and founderBalanceGold
```

Supply before burn:

```text
PLACEHOLDER: query authoritative node /v35/supply
PLACEHOLDER: query authoritative node /v35/burn
PLACEHOLDER: query authoritative node /v35/founder
PLACEHOLDER: archive raw JSON responses
```

Create burn transaction:

```text
PLACEHOLDER: open founder wallet or signing tool
PLACEHOLDER: create normal signed transaction
sender: nNifuwmFZr7fnV1zvmpiyQDV5z7ETWvqR6GSeqeHTY43
recipient: BURN_ADDRESS
amount: calculatedBurnGold from snapshot
PLACEHOLDER: review fee/miner bonus
PLACEHOLDER: broadcast only after final human review
```

Verify burn transaction:

```text
PLACEHOLDER: query transaction by hash
PLACEHOLDER: verify sender, recipient, amount, block index, block hash
PLACEHOLDER: wait for selected confirmation count
```

Verify after-burn supply:

```text
PLACEHOLDER: query /v35/supply
PLACEHOLDER: query /v35/burn
PLACEHOLDER: query /v35/founder
PLACEHOLDER: compare burnedGold, circulatingGold, founderReserveGold
PLACEHOLDER: archive raw JSON and screenshots
```

Proof archive checklist:

```text
PLACEHOLDER: save raw API JSON files
PLACEHOLDER: save explorer screenshots if available
PLACEHOLDER: save wallet transaction screenshot if applicable
PLACEHOLDER: save release announcement text
PLACEHOLDER: save exchange/operator burn note
```

## 12. Remaining Risks

1. The burn transaction is irreversible.
2. The snapshot endpoint is only as authoritative as the selected node's canonical chain/account state.
3. Full historical replay against copied live chain remains an activation prerequisite unless explicitly waived as a release gate.
4. External wallet/exchange tooling must tolerate `BURN_ADDRESS` as a recipient.
5. Founder balance must not change between snapshot and burn.
6. API full-chain scans may be slow on large production nodes.
7. Public messaging must not mix modeled burn values with actual burned supply.
8. Fork index must remain separate from burn execution and proof publication.
9. If burn proof is inconsistent, activation must be delayed.

## 13. Recommended Next Phase

Recommended next phase:

```text
Phase 7B - founder burn proof page/template implementation, no actual burn.
```

Purpose:

- create the public proof page/template with placeholders;
- define exact copy, tables, and links;
- keep it read-only;
- do not perform the burn.

Optional technical-confidence phase:

```text
Phase 6D-2 - long-window difficulty / extended dry-run simulation.
```

Use Phase 6D-2 first if the team wants additional technical confidence before preparing public proof assets.
