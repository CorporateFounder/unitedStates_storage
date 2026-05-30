# CITU V35 Phase 7J - Founder Burn Execution Runbook

## 1. Executive Summary

This is a future founder-burn execution runbook only.

No burn transaction was created, signed, simulated, or broadcast. No funds were moved.

Current planning context:

- Founder address: `nNifuwmFZr7fnV1zvmpiyQDV5z7ETWvqR6GSeqeHTY43`
- Provisional founder reserve: `30,910,616.63 GOLD`
- Provisional 80% burn candidate: `24,728,493.30 GOLD`
- Provisional remaining 20% reserve: `6,182,123.33 GOLD`

These values are planning-only. The final amount must be recalculated from a final approved snapshot.

Current disabled constants remain:

- `V35_RESCUE_FORK_INDEX = Long.MAX_VALUE`
- `V35_BURN_ADDRESS = "BURN_ADDRESS"`

The burn recipient is not final-approved for execution.

## 2. Burn Execution Prerequisites

Do not create an unsigned transaction draft until all hard prerequisites are complete:

- `[ ]` Final snapshot source approved.
- `[ ]` Preferred reviewed-node endpoint `/v35/founder/burn-snapshot` available, or fallback explicitly approved.
- `[ ]` Final snapshot captured three times within a short window.
- `[ ]` Raw endpoint responses archived.
- `[ ]` One exact snapshot height/hash/time tuple selected.
- `[ ]` Founder balance independently verified.
- `[ ]` Candidate burn amount recalculated as 80% of final snapshot balance.
- `[ ]` Candidate remaining reserve recalculated as final balance minus approved burn amount.
- `[ ]` Rounding rule independently verified against transaction precision.
- `[ ]` Final burn address string explicitly approved.
- `[ ]` Wallet/server burn-address constants and public notices compared exactly.
- `[ ]` Transaction reviewer assigned.
- `[ ]` Public proof reviewer assigned.
- `[ ]` Exchange/operator communication reviewer assigned.
- `[ ]` Public RU / EN / ZH proof template ready.
- `[ ]` V35 activation relationship explicitly recorded: burn remains separate from fork activation.
- `[ ]` Final human go/no-go recorded before transaction drafting.

## 3. Burn Timing Policy

Founder burn and V35 activation are separate operations. Do not bundle them into one human action, one commit, or one incident window.

### Option A - Before activation

Advantages:

- Burn proof can be published before the V35 fork.
- Public supply state is easier to explain before launch.
- Fork-day monitoring remains focused on consensus activation.

Risks:

- Burn is irreversible even if activation is later delayed.
- Early execution can cause public confusion if notices are incomplete.
- Exchange operators may need time to understand the burn proof.

### Option B - At or near activation

Advantages:

- Burn and launch communications can appear closely coordinated.

Risks:

- This combines an irreversible transaction with the highest-risk monitoring window.
- Exchange sync questions and burn-proof questions arrive at the same time.
- Operational mistakes are harder to isolate.

Recommendation:

`AVOID`

### Option C - After activation

Advantages:

- Burn occurs after V35 status endpoints and no-spend policy can be observed on the activated chain.
- Launch monitoring can complete before funds move.

Risks:

- Public supply transition remains pending after activation.
- Users may confuse the planned burn with an already completed burn.

### Recommended Policy

Recommended sequence:

1. Finish all approvals and capture the final snapshot.
2. Execute the burn in a separately staffed and announced window.
3. Avoid the fork block window and other periods of network instability.
4. Publish proof promptly after confirmation.

Preferred timing is `before activation` only if all approvals, endpoint evidence, communications, and operator readiness are complete with enough time for review. Otherwise, defer until after activation stabilizes. Never execute at or near the fork window merely to align dates.

This is a recommendation only. Final timing requires explicit approval.

## 4. Exact Future Burn Procedure

### A. Capture final snapshot

1. Confirm no burn transaction exists.
2. Confirm chosen snapshot source:
   - preferred: reviewed node `GET /v35/founder/burn-snapshot`;
   - fallback: `GET /size`, `GET /prevBlock`, and `GET /balance?address=<founder>` captured together;
   - emergency: approved local validated replay/account state.
3. Capture the full snapshot three times within a short window.
4. Record every height/hash/time/balance tuple.
5. Select one exact tuple. Do not average or blend values.

### B. Save raw evidence

1. Save raw endpoint responses exactly as returned.
2. Record node URL, release tag or commit, timestamp with timezone, and reviewer.
3. Archive screenshots or terminal transcripts if used.

### C. Calculate amount

Use:

```text
burn_amount = round_half_up(final_snapshot_balance * 80 / 100, 2)
remaining_reserve = final_snapshot_balance - burn_amount
```

Two reviewers must calculate independently and compare exact results.

### D. Approve amount

Record:

- final snapshot balance;
- exact burn amount;
- exact remaining reserve;
- rounding rule;
- burn amount approver name and timestamp.

### E. Approve burn address

Record:

- exact recipient string;
- chain/network;
- wallet/server constants;
- protocol sink/no-spend recognition;
- public notice match;
- approver name and timestamp.

Do not assume the current sentinel is execution-approved merely because it is configured.

### F. Create unsigned transaction draft

Only after approvals, create an unsigned draft using the approved wallet/tool procedure:

```text
sender    = nNifuwmFZr7fnV1zvmpiyQDV5z7ETWvqR6GSeqeHTY43
recipient = [FINAL APPROVED BURN ADDRESS]
amount    = [APPROVED BURN AMOUNT]
fee       = [APPROVED FEE]
```

### G. Review unsigned transaction

Two reviewers must verify:

- sender;
- recipient, character by character;
- amount;
- fee;
- network;
- snapshot reference;
- no extra outputs;
- no unrelated transaction;
- burn remains separate from activation.

### H. Sign only after approval

Record explicit signing approval. Sign only the reviewed unsigned draft.

### I. Broadcast only after approval

Record explicit broadcast approval. Broadcast only the reviewed signed transaction.

### J. Capture transaction hash

Immediately record:

- transaction hash;
- broadcast timestamp;
- broadcasting tool;
- operator;
- raw response.

### K. Verify block inclusion

Record:

- inclusion block index;
- inclusion block hash;
- inclusion timestamp;
- confirmation count;
- sender;
- recipient;
- amount;
- fee.

### L. Verify post-burn founder balance

Capture founder balance after confirmation and compare:

```text
expected_founder_balance = snapshot_balance - burn_amount - approved_fee_effect
```

### M. Verify burn-address received balance

Capture burn-address received balance and verify expected increase.

### N. Publish proof

Publish the reviewed RU / EN / ZH proof package only after confirmation. Clearly distinguish:

- planned/model values;
- approved transaction values;
- actual confirmed on-chain values.

## 5. Evidence Archive Checklist

Before transaction drafting:

- `[ ]` Snapshot source URL or local command.
- `[ ]` Raw `/v35/founder/burn-snapshot` response if preferred source is used.
- `[ ]` Raw `/v35/founder`, `/v35/supply`, and `/v35/burn` responses if available.
- `[ ]` Raw `/size`, `/prevBlock`, and `/balance?address=<founder>` responses if fallback is used.
- `[ ]` Snapshot height, block hash, block time, and read time.
- `[ ]` Founder balance before burn.
- `[ ]` Calculated 80% amount.
- `[ ]` Calculated remaining reserve.
- `[ ]` Final approved burn address.
- `[ ]` Wallet/server constant comparison.
- `[ ]` Reviewer approvals.

After later drafting/execution only:

- `[ ]` Unsigned transaction details.
- `[ ]` Signed transaction hash.
- `[ ]` Broadcast response and time.
- `[ ]` Inclusion block index/hash/time.
- `[ ]` Confirmation count.
- `[ ]` Post-burn founder balance.
- `[ ]` Post-burn burn-address received balance.
- `[ ]` Post-burn V35 API raw responses.
- `[ ]` Screenshots or terminal transcripts if used.
- `[ ]` Final public proof document.
- `[ ]` SHA-256 checksum of the final proof document.

## 6. Abort Conditions

Stop immediately and do not sign or broadcast if:

- burn address differs anywhere between code, approval record, public proof template, and draft;
- snapshot height/hash does not match the archived raw evidence;
- repeated snapshot reads disagree without an explicitly selected tuple;
- founder balance differs from the approved snapshot;
- amount calculations or rounding differ between reviewers;
- unsigned transaction sender, recipient, amount, fee, or network differs from approval;
- unexpected outputs or unrelated transactions appear;
- network instability, reorg, or node divergence is active;
- an exchange/operator caution or incident warning is active;
- V35 activation status is unclear;
- any assigned reviewer rejects or withholds approval.

After any abort:

1. Preserve evidence.
2. Do not retry blindly.
3. Classify the discrepancy.
4. Capture a new snapshot if required.
5. Repeat approval from the affected gate.

## 7. Public Proof Structure

The public proof must contain clearly labeled `RU`, `EN`, and `ZH` sections with identical facts.

Required fields:

- Burn status: `[PENDING / CONFIRMED]`
- Network: `[NETWORK]`
- Founder address: `nNifuwmFZr7fnV1zvmpiyQDV5z7ETWvqR6GSeqeHTY43`
- Burn address: `[FINAL APPROVED BURN ADDRESS]`
- Snapshot height: `[HEIGHT]`
- Snapshot block hash: `[HASH]`
- Snapshot time: `[DATE / TIME / TIMEZONE]`
- Founder balance before burn: `[AMOUNT] GOLD`
- Approved burn percent: `80%`
- Burn amount: `[AMOUNT] GOLD`
- Remaining reserve: `[AMOUNT] GOLD`
- Transaction hash: `[TX HASH]`
- Inclusion block index: `[BLOCK INDEX]`
- Inclusion block hash: `[BLOCK HASH]`
- Confirmations: `[COUNT]`
- Verification endpoints: `[OFFICIAL URLS]`
- Proof-document SHA-256: `[HASH]`

Every language section must warn:

- verify official links only;
- do not send funds manually to any burn address;
- ignore unofficial messages, links, and lookalike addresses.

## 8. Roles And Approval Placeholders

| Role | Name / timestamp / signoff |
| --- | --- |
| Technical snapshot reviewer | `[PLACEHOLDER]` |
| Founder balance reviewer | `[PLACEHOLDER]` |
| Burn amount approver | `[PLACEHOLDER]` |
| Burn address approver | `[PLACEHOLDER]` |
| Transaction reviewer 1 | `[PLACEHOLDER]` |
| Transaction reviewer 2 | `[PLACEHOLDER]` |
| Signing approver | `[PLACEHOLDER]` |
| Broadcast approver | `[PLACEHOLDER]` |
| Exchange/operator communication reviewer | `[PLACEHOLDER]` |
| Public RU / EN / ZH proof reviewer | `[PLACEHOLDER]` |
| Post-burn verification reviewer | `[PLACEHOLDER]` |

## 9. Remaining Blockers

- Final reviewed-node snapshot endpoint evidence is not captured.
- Final snapshot height/hash/time is not approved.
- Final founder balance is not approved.
- Final 80% burn amount is not approved.
- Final remaining reserve is not approved.
- Final burn address is not approved for execution.
- Transaction tooling procedure is not approved.
- Signing and broadcasting approvals are not assigned.
- Exchange/operator communication timing is not approved.
- Burn timing relative to activation is not approved.
- Founder burn execution remains `NOT YET`.

## 10. Confirmation No Code Changed

Confirmed.

No source code was changed in Phase 7J.

## 11. Confirmation No Tests Changed

Confirmed.

No tests were changed or run in Phase 7J.

## 12. Confirmation No Burn Transaction Was Created / Signed / Broadcast / Simulated

Confirmed:

- no burn transaction was created;
- no burn transaction was signed;
- no burn transaction was broadcast;
- no burn transaction was simulated;
- no funds were moved.

## 13. Confirmation V35 Remains Disabled

Confirmed:

- `V35_RESCUE_FORK_INDEX = Long.MAX_VALUE`
- `V35_BURN_ADDRESS = BURN_ADDRESS`

Candidate height `513332` remains planning-only.

## 14. Confirmation No Fork / Jackpot Action Was Performed

Confirmed:

- no fork activation was performed;
- no jackpot payout transaction was created;
- no final git tag was created;
- no website deployment was performed.

## 15. Recommended Next Phase

Recommended next phase:

**Phase 10B - final activation readiness review, no activation.**

Alternative if the preferred snapshot source should be operationally rehearsed first:

**Phase 7K - founder snapshot capture rehearsal, read-only only, no burn execution.**
