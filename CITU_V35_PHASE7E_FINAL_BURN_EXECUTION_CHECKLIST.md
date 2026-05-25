# CITU V35 Phase 7E Final Founder Burn Execution Checklist

Phase: 7E only  
Scope: final operational checklist for a later manual founder burn  
Code changes: none  
V35 activation: not performed  
Real fork index: not selected  
Founder burn transaction: not created  
Jackpot payout transaction: not created

## 1. Exact Reports / Files Read

Reports read:

- `CITU_V35_PHASE7A_FOUNDER_BURN_PROOF_PACKAGE_PLAN.md`
- `CITU_V35_PHASE7B_PUBLIC_BURN_PROOF_PAGE_REPORT.md`
- `CITU_V35_PHASE7C_FOUNDER_BURN_PROOF_LIVE_API_INTEGRATION_PLAN.md`
- `CITU_V35_PHASE7D_PUBLIC_BURN_PROOF_LIVE_STATUS_REPORT.md`
- `CITU_V35_PHASE8A_PUBLIC_RELEASE_AND_EXCHANGE_NOTICE_DRAFTS.md`
- `CITU_V35_PHASE6F1_FINAL_FORK_INDEX_ACTIVATION_PLAN.md`
- `CITU_V35_PHASE6E_RELEASE_ROLLBACK_EXCHANGE_CHECKLIST.md`
- `CITU_V35_PHASE6C2_HISTORICAL_CHAIN_VALIDATION_REPORT.md`
- `CITU_V35_PHASE6D2_LONG_WINDOW_DIFFICULTY_DRY_RUN_REPORT.md`

Constants inspected:

- `unitedStates_final-master/src/main/java/International_Trade_Union/setings/Seting.java`
- `unitedStates_storage-master/src/main/java/International_Trade_Union/setings/Seting.java`

Confirmed:

- Wallet `V35_RESCUE_FORK_INDEX = Long.MAX_VALUE`
- Server `V35_RESCUE_FORK_INDEX = Long.MAX_VALUE`
- Wallet `V35_BURN_ADDRESS = "BURN_ADDRESS"`
- Server `V35_BURN_ADDRESS = "BURN_ADDRESS"`
- Wallet founder address `nNifuwmFZr7fnV1zvmpiyQDV5z7ETWvqR6GSeqeHTY43`
- Server founder address `nNifuwmFZr7fnV1zvmpiyQDV5z7ETWvqR6GSeqeHTY43`

## 2. Current Burn Readiness Summary

Current state:

- Founder burn has not happened.
- V35 remains disabled.
- The real fork index has not been selected.
- `BURN_ADDRESS` is configured in wallet and server.
- `BURN_ADDRESS` is the final sentinel protocol sink.
- Public founder burn proof page exists at `/v35/founder-burn-proof`.
- Public founder burn proof page has optional live/status API integration.
- Public V35 announcement and exchange/node notice drafts exist.
- Long-window difficulty dry-run passed and fixed a bounded-target issue.
- Historical-chain validation runner exists, but full live historical-chain replay still requires copied live-chain data.
- Founder burn must be a separate manual on-chain operation, not bundled with fork-index activation.

This report is an operational checklist only. It does not perform the burn.

## 3. Burn Go / No-Go Checklist

Do not perform the manual founder burn unless every required gate is complete or explicitly accepted by the release owner.

### Repository / release gate

- [ ] Git status is clean except intentionally staged release/report artifacts.
- [ ] All V35 reports are committed.
- [ ] No generated `target/` files are staged.
- [ ] Wallet tests pass.
- [ ] Wallet clean package passes.
- [ ] Server tests pass.
- [ ] Server clean package passes.
- [ ] Website tests pass.
- [ ] Website clean package passes.
- [ ] Simulated V35 fork dry-run passed.
- [ ] Long-window difficulty dry-run passed.
- [ ] Historical-chain replay against copied live chain completed, or explicitly marked as final pre-release gate before activation.

### Communication / proof gate

- [ ] Public V35 release announcement draft is ready.
- [ ] Exchange/node operator notice draft is ready.
- [ ] Public burn proof page is ready.
- [ ] Optional live/status API integration is working or safely falling back.
- [ ] Support/contact channel is ready.
- [ ] Emergency communication channel is ready.
- [ ] Operators understand the burn is irreversible.

### Burn execution gate

- [ ] Founder wallet access is confirmed.
- [ ] Founder address is confirmed:
  - `nNifuwmFZr7fnV1zvmpiyQDV5z7ETWvqR6GSeqeHTY43`
- [ ] Burn address is confirmed exactly:
  - `BURN_ADDRESS`
- [ ] Authoritative node/source for snapshot is selected.
- [ ] Backup of live node chain/account data is created.
- [ ] Founder account operations are frozen before snapshot.
- [ ] Wallet/tool can send TO `BURN_ADDRESS`.
- [ ] Fee/miner-bonus behavior is understood.
- [ ] Two humans independently review recipient and amount before broadcast.

Go/no-go rule:

```text
If any consensus, replay, wallet/tool, recipient, amount, or proof concern remains unresolved, do not burn.
```

## 4. Snapshot Procedure

The snapshot must capture a single canonical chain point. The burn amount must be calculated from that snapshot, not from a moving live balance.

### Step 1: Freeze founder address activity

- Stop any planned founder-address outgoing transfers.
- Confirm no automated process will spend from founder address during snapshot/burn window.
- Record the freeze start time.

### Step 2: Select authoritative source

Use one selected authoritative node/API source for the snapshot.

Record:

- source node URL or host label;
- node version;
- current sync status;
- current chain height;
- current tip hash.

### Step 3: Record chain snapshot

Record:

- snapshot height;
- snapshot block hash;
- snapshot timestamp, if available;
- snapshot generated time;
- current V35 active status;
- current fork index value.

### Step 4: Record founder and supply values

Record:

- founder address;
- founder balance at snapshot;
- `BURN_ADDRESS`;
- total minted;
- burned supply;
- circulating supply;
- founder reserve;
- jackpot paid;
- annual emission.

### Step 5: Archive raw API outputs

Archive raw responses:

```text
GET /v35/supply
GET /v35/burn
GET /v35/founder
GET /v35/founder/burn-snapshot
```

Recommended filenames:

```text
v35-burn-snapshot-before.json
v35-supply-before.json
v35-burn-before.json
v35-founder-before.json
```

### Step 6: Validate snapshot consistency

Confirm:

- `/v35/founder/burn-snapshot` founder address matches `nNifuwmFZr7fnV1zvmpiyQDV5z7ETWvqR6GSeqeHTY43`.
- burn address matches exact `BURN_ADDRESS`.
- actual burned supply is actual chain/account value, not modeled burn.
- modeled 75/80/85/90 options remain separate from actual burned/circulating supply.
- V35 remains disabled unless a real fork index has already been published in a later phase.

If any field is missing, contradictory, or stale, stop and regenerate the snapshot after investigation.

## 5. Burn Amount Calculation Procedure

Recommended burn percent:

```text
80%
```

Formula:

```text
burnAmountGold = founderSnapshotBalanceGold * 0.80
remainingFounderReserveGold = founderSnapshotBalanceGold - burnAmountGold
```

Precision rules:

- Use existing transaction precision and canonical GOLD scale.
- Use BigDecimal/string-safe decimal arithmetic where tooling permits.
- Do not use floating-point mental math for final amount.
- Round only according to existing transaction creation precision.
- Record exact rounded amount and rounding rule.

Independent calculation checklist:

- [ ] Operator A calculates `80%` from snapshot founder balance.
- [ ] Operator B independently calculates `80%` from the same snapshot founder balance.
- [ ] Both compare exact decimal amount.
- [ ] Both calculate remaining `20%` founder reserve.
- [ ] Both confirm modeled values are not actual burned supply yet.
- [ ] Both sign off before transaction preparation.

Never:

- calculate from a moving balance after snapshot;
- recalculate from a different node without creating a new snapshot;
- adjust amount silently for convenience;
- treat the 80% modeled value as actual burn before the transaction confirms.

If founder balance changes after snapshot and before broadcast:

```text
Cancel this burn attempt.
Create a new snapshot.
Recalculate the burn amount.
Restart human review.
```

## 6. Manual Burn Transaction Checklist

Do not use this checklist as an automatic command script. The exact production wallet/signing flow must be confirmed by the operator.

### Preparation

- [ ] Confirm this is a normal signed transaction, not a protocol constant change.
- [ ] Confirm sender is the founder address:
  - `nNifuwmFZr7fnV1zvmpiyQDV5z7ETWvqR6GSeqeHTY43`
- [ ] Confirm recipient is exactly:
  - `BURN_ADDRESS`
- [ ] Confirm amount equals the independently calculated 80% snapshot amount.
- [ ] Confirm fee/miner bonus behavior.
- [ ] Confirm transaction does not include any extra recipients.
- [ ] Confirm transaction does not include fork-index activation.
- [ ] Confirm transaction does not require any source/test code change.

### Wallet / CLI placeholders

Use the real production wallet flow when known:

```text
PLACEHOLDER: open founder wallet or approved signing tool
PLACEHOLDER: select sender = founder address
PLACEHOLDER: set recipient = BURN_ADDRESS
PLACEHOLDER: set amount = calculated burnAmountGold
PLACEHOLDER: review fee/miner bonus
PLACEHOLDER: preview transaction details
PLACEHOLDER: export or screenshot unsigned/preview details if supported
PLACEHOLDER: request final human approval
PLACEHOLDER: sign transaction
PLACEHOLDER: broadcast transaction
```

### Final pre-broadcast confirmation

- [ ] Recipient string is exactly `BURN_ADDRESS`.
- [ ] There is no hidden whitespace in recipient.
- [ ] Sender is the founder address, not another project account.
- [ ] Amount is exactly the approved amount.
- [ ] Fee/miner bonus is acceptable.
- [ ] Transaction is irreversible.
- [ ] Burn cannot be undone if sent correctly.
- [ ] If sent incorrectly, fork activation must pause.
- [ ] Two humans verbally confirm: `send founder burn now`.

Broadcast rule:

```text
Broadcast only after final human confirmation. Do not retry automatically if the transaction fails.
```

## 7. Post-Burn Verification Checklist

After the burn is later broadcast and confirmed, verify before public final proof:

### Transaction verification

- [ ] Burn transaction hash recorded.
- [ ] Burn transaction is included in a block.
- [ ] Burn block index recorded.
- [ ] Burn block hash recorded.
- [ ] Burn block timestamp recorded, if available.
- [ ] Sender equals founder address.
- [ ] Recipient equals `BURN_ADDRESS`.
- [ ] Amount equals calculated burn amount.
- [ ] Fee/miner bonus matches expected behavior.
- [ ] Confirmation count recorded.
- [ ] Transaction validation status recorded.

### Balance verification

- [ ] Founder balance decreased by burn amount plus any applicable fee/miner-bonus effect.
- [ ] `BURN_ADDRESS` balance increased by burn amount.
- [ ] Total minted remains historically minted and is not reduced by burn.
- [ ] Burned supply equals `BURN_ADDRESS` balance.
- [ ] Circulating supply equals total minted minus burned supply.
- [ ] Founder reserve equals post-burn founder balance.
- [ ] Jackpot paid remains separately reported.

### API verification

Archive raw outputs:

```text
GET /v35/supply
GET /v35/burn
GET /v35/founder
GET /v35/founder/burn-snapshot
```

Confirm:

- [ ] `/v35/supply` reports actual burned supply from chain/account data.
- [ ] `/v35/burn` reports `BURN_ADDRESS` and actual burn status consistently.
- [ ] `/v35/founder` reports updated founder reserve.
- [ ] `/v35/founder/burn-snapshot` remains clear about actual versus modeled values.
- [ ] Website proof page still separates actual, modeled, and pending fields.

Recommended after-burn archive filenames:

```text
v35-burn-transaction.json
v35-supply-after.json
v35-burn-after.json
v35-founder-after.json
v35-founder-burn-snapshot-after.json
```

## 8. Public Proof Publication Checklist

Publish only after transaction confirmation and API verification.

Required proof fields:

- [ ] Snapshot height.
- [ ] Snapshot block hash.
- [ ] Snapshot timestamp, if available.
- [ ] Founder address.
- [ ] `BURN_ADDRESS`.
- [ ] Founder balance before burn.
- [ ] Selected burn percent.
- [ ] Calculated burn amount.
- [ ] Burn transaction hash.
- [ ] Burn block index.
- [ ] Burn block hash.
- [ ] Burn block timestamp, if available.
- [ ] Founder balance after burn.
- [ ] `BURN_ADDRESS` balance after burn.
- [ ] Total minted before/after.
- [ ] Burned supply before/after.
- [ ] Circulating supply before/after.
- [ ] Founder reserve before/after.
- [ ] Annual emission.
- [ ] Link or label for `/v35/supply`.
- [ ] Link or label for `/v35/burn`.
- [ ] Link or label for `/v35/founder`.
- [ ] Link or label for `/v35/founder/burn-snapshot`.

Required explanatory text:

- [ ] Burn is irreversible.
- [ ] Burned coins remain historically minted.
- [ ] Burned coins are excluded from circulating supply.
- [ ] `BURN_ADDRESS` is a protocol sink.
- [ ] Sending TO `BURN_ADDRESS` is allowed.
- [ ] Spending FROM `BURN_ADDRESS` is blocked after V35 by consensus policy.
- [ ] Founder reward after V35 is `0 GOLD`.
- [ ] Fork activation remains by block index.
- [ ] Burn and fork activation are separate operations.

Archive checklist:

- [ ] Raw API JSON files saved.
- [ ] Transaction proof saved.
- [ ] Block proof saved.
- [ ] Screenshots saved if useful.
- [ ] Public announcement updated.
- [ ] Exchange/node notice updated.
- [ ] Website proof page updated from pending to proven fields only where real values exist.

## 9. Mistake-Prevention Checklist

Critical mistakes to avoid:

- Burning before hardening is accepted.
- Burning before live historical replay is completed or explicitly kept as final pre-release gate.
- Burning before snapshot.
- Using a live moving balance instead of snapshot balance.
- Using the wrong founder wallet.
- Sending to a recipient other than exact `BURN_ADDRESS`.
- Adding hidden whitespace or typo in `BURN_ADDRESS`.
- Sending the wrong amount.
- Ignoring fee/miner-bonus effects.
- Claiming burn before transaction confirms.
- Mixing modeled 80% burn with actual burned supply.
- Updating public proof with fake transaction or block hashes.
- Changing `V35_RESCUE_FORK_INDEX` in the same step.
- Announcing a date without block index.
- Forgetting exchange/node notice.
- Retrying blindly after a failed transaction.
- Proceeding to fork activation after a disputed or incorrect burn.

Human review line:

```text
If the burn transaction preview does not exactly match sender, recipient, and amount, stop.
```

## 10. Relation To Fork Activation

Burn and fork-index activation are separate operations.

Founder burn:

- normal founder-signed on-chain transaction;
- sender is founder address;
- recipient is `BURN_ADDRESS`;
- recommended amount is 80% of snapshot founder GOLD balance;
- can happen before V35 activation;
- must be publicly proven after confirmation.

Fork activation:

- later code/release operation;
- changes only wallet/server `Seting.java` in the final activation commit;
- replaces `Long.MAX_VALUE` with the selected real fork index;
- must provide exchanges/nodes an upgrade window;
- must not be bundled with burn execution.

Current state:

- V35 remains disabled.
- Real fork index remains unselected.
- Founder burn remains unexecuted.

Recommended sequencing:

1. Finish hardening and live-chain replay gate.
2. Prepare release/exchange package.
3. Announce V35 plan.
4. Snapshot founder balance.
5. Execute founder burn manually.
6. Publish burn proof.
7. Continue exchange/node upgrade window.
8. Later create final fork-index activation commit and release.

## 11. Emergency Plan

### If burn transaction creation fails

- Stop.
- Do not retry blindly.
- Save exact error message.
- Verify sender address.
- Verify recipient `BURN_ADDRESS`.
- Verify wallet/tool supports non-key recipient.
- Verify founder balance.
- Verify fee/miner-bonus requirements.
- Verify node sync.
- Verify transaction pool policy.
- Do not change fork index.
- Document issue and open a focused fix/review phase if needed.

### If broadcast fails

- Stop.
- Confirm whether transaction was actually accepted by any node before recreating.
- Check mempool/transaction pool.
- Check logs.
- Check transaction hash if one was produced.
- Do not create a second transaction until first attempt status is known.

### If wrong transaction is created but not broadcast

- Discard it.
- Record the mistake internally.
- Restart review from snapshot or transaction checklist as needed.

### If wrong transaction is broadcast

- Stop all activation work.
- Do not proceed to fork activation.
- Preserve transaction details and logs.
- Notify release owner immediately.
- Analyze whether funds are recoverable.
- Prepare public correction plan only after analysis.
- Update exchange/operator timeline if needed.

### If proof values conflict

- Do not publish final proof.
- Do not change fork index.
- Reconcile API, block, transaction, and balance data.
- If unresolved, delay activation.

## 12. Confirmation No Code Changed

No source code, tests, wallet/server/website files, `pom.xml`, protocol constants, mining behavior, validation behavior, balance behavior, transaction format, block format, API semantics, or UI templates were changed in this phase.

Only this Markdown report was created:

- `CITU_V35_PHASE7E_FINAL_BURN_EXECUTION_CHECKLIST.md`

## 13. Confirmation No Burn Transaction Was Created

No founder burn transaction was created, signed, broadcast, stored, or claimed.

No balance was changed.

## 14. Confirmation V35 Remains Disabled

Confirmed:

- Wallet `V35_RESCUE_FORK_INDEX = Long.MAX_VALUE`
- Server `V35_RESCUE_FORK_INDEX = Long.MAX_VALUE`
- Wallet `V35_BURN_ADDRESS = "BURN_ADDRESS"`
- Server `V35_BURN_ADDRESS = "BURN_ADDRESS"`

The real fork index has not been selected.

## 15. Remaining Risks

Highest remaining risks:

1. Founder burn is irreversible.
2. Full live historical-chain replay still requires copied live-chain data.
3. Snapshot correctness depends on the selected authoritative node being synced and canonical.
4. External wallet/tooling must support sending TO `BURN_ADDRESS`.
5. Founder balance must not change between snapshot and burn.
6. Fee/miner-bonus behavior must be reviewed before final amount confirmation.
7. Public proof must not mix modeled values with actual burned supply.
8. Exchange/node timing remains separate and still requires a real fork-index upgrade window.
9. Any wrong burn transaction must halt fork activation until reviewed.

## 16. Recommended Next Phase

Recommended next phase:

```text
Phase 9A - live historical-chain replay execution guide.
```

Reason:

- The burn checklist is ready, but the full live historical-chain replay still requires copied live-chain data.
- Completing the replay guide before burn/fork operations keeps the irreversible burn and activation steps behind the strongest old-chain safety gate.

Alternative next phases:

```text
Phase 8B - release artifact/checksum/tag plan, no code changes.
Phase 6F-2 - final activation commit checklist, no code changes.
```

Use Phase 8B if release packaging is the priority. Use Phase 6F-2 only after replay, burn timing, exchange readiness, and release artifacts are settled.
