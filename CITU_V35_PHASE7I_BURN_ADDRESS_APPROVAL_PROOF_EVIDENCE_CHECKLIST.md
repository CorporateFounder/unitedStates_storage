# CITU V35 Phase 7I - Burn Address Approval and Proof Evidence Checklist

## 1. Executive Summary

Phase 7I created a final burn-address approval and proof-evidence checklist.

This document is an approval checklist only. It does not approve a final burn address for execution and does not execute a burn.

No source code, tests, constants, artifacts, historical replay, transactions, signatures, broadcasts, simulations, fork activation, jackpot payout, final tag, or commit were changed or created.

## 2. Current Burn Address Status

Current server constant:

`V35_BURN_ADDRESS = "BURN_ADDRESS"`

Current wallet constant:

`V35_BURN_ADDRESS = "BURN_ADDRESS"`

Current status:

`SENTINEL CONFIGURED / NOT FINAL-APPROVED FOR EXECUTION`

The current sentinel is protocol-recognized by the V35 burn-address policy. The final burn recipient string still requires explicit human approval before any transaction is created or signed.

No burn address change was made in this phase.

No burn transaction was created.

## 3. Final Burn Address Approval Requirements

Before any founder burn transaction is created, all of the following must be approved and archived:

- Final burn address string.
- Burn address approver name.
- Approval date/time with timezone.
- Exact chain/network context.
- Exact wallet/server release version or git commit.
- Confirmation that the address is intentionally unspendable or protocol-recognized as the burn address.
- Confirmation that wallet and server constants match exactly.
- Confirmation that public notices show the exact same burn address string.
- Confirmation that proof page/template shows the exact same burn address string.
- Confirmation that exchanges, node operators, and wallet users are warned not to send funds manually to fake or unofficial burn addresses.
- Confirmation that no private key is expected or retained for the sentinel sink.
- Confirmation that burn execution is separate from fork activation.

Approval record template:

| Field | Value |
| --- | --- |
| Final burn address | `[EXACT ADDRESS]` |
| Network | `[NETWORK]` |
| Wallet constant verified | `[YES / NO]` |
| Server constant verified | `[YES / NO]` |
| Public notice verified | `[YES / NO]` |
| Protocol no-spend recognition verified | `[YES / NO]` |
| Approver | `[NAME]` |
| Approval timestamp | `[DATE / TIME / TIMEZONE]` |

## 4. Proof Evidence Checklist

Preserve raw evidence before burn execution:

- `[ ]` Final founder snapshot raw response.
- `[ ]` Snapshot source URL or local command.
- `[ ]` Snapshot read timestamp with timezone.
- `[ ]` Snapshot height.
- `[ ]` Latest block hash.
- `[ ]` Latest block timestamp if available.
- `[ ]` Founder address.
- `[ ]` Founder balance before burn.
- `[ ]` Calculated 80% burn amount.
- `[ ]` Calculated 20% remaining reserve.
- `[ ]` Final approved burn address.
- `[ ]` Wallet/server constant verification output.
- `[ ]` Public notice burn-address verification.
- `[ ]` Independent reviewer calculation.

Preserve raw evidence only if a transaction is later created:

- `[ ]` Unsigned transaction draft.
- `[ ]` Draft sender.
- `[ ]` Draft recipient.
- `[ ]` Draft amount.
- `[ ]` Draft fee.
- `[ ]` Transaction reviewer signoff before signing.

Preserve raw evidence only after later execution:

- `[ ]` Signed transaction hash.
- `[ ]` Burn inclusion block index.
- `[ ]` Burn inclusion block hash.
- `[ ]` Confirmation count.
- `[ ]` Post-burn founder balance.
- `[ ]` Post-burn burn-address received balance.
- `[ ]` Post-burn `/v35/supply` raw response if available.
- `[ ]` Post-burn `/v35/burn` raw response if available.
- `[ ]` Post-burn `/v35/founder` raw response if available.
- `[ ]` Post-burn `/v35/founder/burn-snapshot` raw response if available.
- `[ ]` Explorer or node API proof links.
- `[ ]` Screenshots or terminal transcripts.
- `[ ]` SHA-256 checksum for the final public proof document if published as a file.

## 5. Future Public Proof Format

The future public founder-burn proof should contain:

### Summary

- Burn status: `[PENDING / CONFIRMED]`
- Network: `[NETWORK]`
- Verification timestamp: `[DATE / TIME / TIMEZONE]`

### Snapshot

- Founder address: `nNifuwmFZr7fnV1zvmpiyQDV5z7ETWvqR6GSeqeHTY43`
- Burn address: `[FINAL APPROVED ADDRESS]`
- Snapshot height: `[HEIGHT]`
- Snapshot block hash: `[HASH]`
- Snapshot time: `[DATE / TIME / TIMEZONE]`
- Founder balance before burn: `[AMOUNT] GOLD`

### Burn Amount

- Approved burn percent: `80%`
- Burn amount: `[AMOUNT] GOLD`
- Remaining founder reserve: `[AMOUNT] GOLD`

### Transaction Proof

- Transaction hash: `[TX HASH]`
- Burn inclusion block index: `[BLOCK INDEX]`
- Burn inclusion block hash: `[BLOCK HASH]`
- Confirmations: `[COUNT]`

### Verification Sources

- `/v35/founder/burn-snapshot`: `[URL]`
- `/v35/founder`: `[URL]`
- `/v35/supply`: `[URL]`
- `/v35/burn`: `[URL]`
- Explorer or node proof link: `[URL]`
- Public proof document SHA-256: `[HASH IF USED]`

### Warning

Verify exact official links only. Do not send funds manually to addresses copied from unofficial messages, websites, or social posts.

## 6. Provisional Planning Values

Phase 7G provisional planning snapshot:

- founder reserve: `30,910,616.63 GOLD`
- candidate 80% burn: `24,728,493.30 GOLD`
- candidate 20% remaining reserve: `6,182,123.33 GOLD`
- size: `506420`
- latest block index: `506419`
- latest block hash: `1090c4a987e0a44112c4a30421008603033c89114033203c455366260020085b`
- read time: `2026-05-29 21:54:25 +05:00`

Status:

`PLANNING ONLY`

These values must be recalculated from the final approved snapshot before any burn execution.

## 7. Risk Table

| Risk | Impact | Required Control |
| --- | --- | --- |
| Wrong burn address | Permanent loss or invalid proof | Verify exact final address independently before signing |
| Typo or lookalike address | Funds sent to unintended destination | Compare exact string from code, notice, and draft transaction |
| Fake public burn-address attack | Users may send funds to attacker-controlled address | Publish official links and warn users not to send manually |
| Wrong amount | Irreversible overburn or underburn | Recalculate twice from fixed snapshot; require amount approver |
| Wrong snapshot height | Amount and proof may be disputed | Preserve height, hash, time, and raw responses |
| Accidental early burn | Release sequencing failure | Require separate explicit burn-execution approval |
| Burn before exchange notice | Operator confusion | Review exchange/operator communication before execution |
| Irreversible transaction | Cannot undo mistakes | Final human confirmation before signing and broadcasting |
| Code/public notice mismatch | Public proof becomes unreliable | Compare wallet/server constants and public notice exactly |
| User confusion | Users may act on unofficial information | Publish plain-language notice and fake-address warning |

## 8. Approval Checklist

Required approvals before later burn execution:

- `[ ]` Burn address approver confirms exact final recipient.
- `[ ]` Founder snapshot reviewer confirms final height/hash/time and balance.
- `[ ]` Burn amount approver confirms 80% amount and remaining reserve.
- `[ ]` Transaction reviewer confirms sender, recipient, amount, and fee before signing.
- `[ ]` Public proof reviewer confirms publication fields and official links.
- `[ ]` Exchange communication reviewer confirms operator/user wording.
- `[ ]` Final release reviewer confirms burn remains separate from activation.
- `[ ]` Wallet/server constant verification is archived.
- `[ ]` Public notice exact-address verification is archived.
- `[ ]` Final human go/no-go is recorded.

Approval placeholders:

- Burn address approver: `[NAME / DATE / SIGNOFF]`
- Founder snapshot reviewer: `[NAME / DATE / SIGNOFF]`
- Burn amount approver: `[NAME / DATE / SIGNOFF]`
- Transaction reviewer: `[NAME / DATE / SIGNOFF]`
- Public proof reviewer: `[NAME / DATE / SIGNOFF]`
- Exchange communication reviewer: `[NAME / DATE / SIGNOFF]`
- Final release reviewer: `[NAME / DATE / SIGNOFF]`

## 9. Remaining Blockers

- Final burn address is not approved for execution.
- Final founder snapshot height/hash is not selected.
- Final burn amount is not approved.
- Live V35 founder/supply/burn endpoints are not deployed on the queried node.
- Founder burn execution remains not approved.
- Final V35 fork height remains candidate-only.
- Final activation commit, final build, final checksums, final tag, and final release remain not yet.

## 10. Confirmation No Code Changed

Confirmed.

No source code was changed in Phase 7I.

## 11. Confirmation No Tests Changed

Confirmed.

No tests were changed in Phase 7I.

## 12. Confirmation No Burn Transaction Was Created / Signed / Broadcast / Simulated

Confirmed:

- no burn transaction was created
- no burn transaction was signed
- no burn transaction was broadcast
- no burn transaction was simulated
- no funds were moved

## 13. Confirmation V35 Remains Disabled

Confirmed:

- `V35_RESCUE_FORK_INDEX = Long.MAX_VALUE`
- `V35_BURN_ADDRESS = BURN_ADDRESS`

No V35 activation was performed.

## 14. Confirmation No Fork / Jackpot Action Was Performed

Confirmed:

- no fork activation was performed
- no final fork index was committed
- no jackpot payout transaction was created
- no final git tag was created

## 15. Recommended Next Phase

Recommended next phase:

Phase 6F-4 - final activation commit dry-run diff plan, no code changes.

Alternative if burn execution documentation must be completed first:

Phase 7J - founder burn execution runbook finalization, no burn execution.
