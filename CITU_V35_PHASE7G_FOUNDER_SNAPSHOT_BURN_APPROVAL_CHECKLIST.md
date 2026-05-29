# CITU V35 Phase 7G - Founder Snapshot and Burn Approval Checklist

## 1. Executive Summary

Phase 7G created a final founder snapshot and burn approval checklist.

This phase did not execute a burn. No transaction was created, signed, broadcast, simulated, or stored. No V35 activation was performed. No source code, tests, V35 constants, build artifacts, final tag, commit, or jackpot payout transaction were changed or created.

The live read-only legacy balance endpoints returned a founder account balance. Because the live V35 founder snapshot endpoints returned `404`, the values below are treated as a provisional read-only planning snapshot, not an approved final burn snapshot.

Final burn execution still requires explicit later approval.

## 2. Founder Address

Founder address:

`nNifuwmFZr7fnV1zvmpiyQDV5z7ETWvqR6GSeqeHTY43`

## 3. Documents / Files Reviewed

- `CITU_V35_PHASE6F3_FINAL_FORK_HEIGHT_GO_NO_GO_CHECKLIST.md`
- `CITU_V35_PHASE7F_FOUNDER_BURN_FINAL_REVIEW_NO_EXECUTION.md`
- `CITU_V35_PHASE8C_RELEASE_NOTES_FINAL.md`
- `CITU_V35_PHASE8F_EXCHANGE_NOTICE_DRAFT.md`
- `CITU_V35_PHASE8F_NODE_OPERATOR_NOTICE_DRAFT.md`
- `CITU_V35_PHASE8F_WALLET_USER_NOTICE_DRAFT.md`
- `CITU_V35_PHASE9E_FINAL_PRE_ACTIVATION_SAFETY_AUDIT_REPORT.md`
- `CITU_V35_PHASE8G_FINAL_PRERELEASE_PACKAGE_APPROVAL_REPORT.md`
- `CITU_V35_PHASE5E1_FOUNDER_BURN_SNAPSHOT_REPORT.md`
- `CITU_V35_PHASE5C2C_SUPPLY_API_ENDPOINTS_REPORT.md`
- `unitedStates_storage-master/src/main/java/International_Trade_Union/controllers/V35SupplyController.java`
- `unitedStates_storage-master/src/main/java/International_Trade_Union/protocol/v35/V35SupplyApiResponseBuilder.java`
- `unitedStates_storage-master/src/main/java/International_Trade_Union/protocol/v35/V35SupplyCalculator.java`
- `unitedStates_storage-master/src/main/java/International_Trade_Union/controllers/BasisController.java`
- `unitedStates_storage-master/src/main/java/International_Trade_Union/controllers/ConductorController.java`
- server `Seting.java`
- wallet `Seting.java`

## 4. Snapshot Source And Confidence

Read-only live endpoints attempted:

- `GET http://194.87.236.238:82/v35/founder/burn-snapshot` returned `404`.
- `GET http://194.87.236.238:82/v35/founder` returned `404`.
- `GET http://194.87.236.238:82/v35/supply` returned `404`.
- `GET http://194.87.236.238:82/v35/burn` returned `404`.

Read-only live legacy endpoints used:

- `GET http://194.87.236.238:82/balance?address=<founder>`
- `GET http://194.87.236.238:82/account?address=<founder>`
- `GET http://194.87.236.238:82/dollar?address=<founder>`
- `GET http://194.87.236.238:82/stock?address=<founder>`
- `GET http://194.87.236.238:82/size`
- `GET http://194.87.236.238:82/prevBlock`

Confidence:

`MEDIUM`

Reason:

- The live legacy account endpoints returned consistent founder balance data.
- The live size and previous-block endpoints returned usable height/hash context.
- The intended V35 burn-snapshot endpoint exists in local code and reports modeled burn options, but it is not available on the queried live node yet.
- Therefore this is a provisional planning snapshot only. The final approved burn snapshot should be taken from the final release node using the V35 founder burn snapshot endpoint if available, or through another explicitly approved read-only method.

## 5. Snapshot Height / Time

Live size returned:

`506420`

Latest block from `/prevBlock`:

- block index: `506419`
- block hash: `1090c4a987e0a44112c4a30421008603033c89114033203c455366260020085b`
- block timestamp: `2026-05-29T16:39:12.000+00:00`

Read time:

`2026-05-29 21:54:25 +05:00`

Snapshot interpretation:

- `size = 506420` appears to be the current chain count.
- latest block index from `/prevBlock` is `506419`.
- For burn proof language, use the latest block index/hash as the provisional snapshot block context.

## 6. Founder Balance

Legacy `/balance` and `/account` returned:

```json
{
  "account": "nNifuwmFZr7fnV1zvmpiyQDV5z7ETWvqR6GSeqeHTY43",
  "digitalDollarBalance": 30910616.63,
  "digitalStockBalance": 30910622.45,
  "digitalStakingBalance": 0.00
}
```

Legacy `/dollar` returned:

`30910616.63`

Legacy `/stock` returned:

`30910622.45`

V35 supply code uses founder reserve GOLD from `digitalDollarBalance` through `V35SupplyCalculator.accountGoldBalance(...)`.

Founder reserve used for candidate burn planning:

`30,910,616.63 GOLD`

Founder stock/staking values are recorded for review context only:

- digital stock: `30,910,622.45`
- digital staking: `0.00`

## 7. Candidate 80% Burn Amount

Candidate burn policy:

`80%` of snapshot founder reserve.

Candidate founder reserve:

`30,910,616.63 GOLD`

Candidate 80% burn amount, using the existing V35 two-decimal modeled burn semantics:

`24,728,493.30 GOLD`

This is candidate-only and must not be used to create a transaction without explicit approval.

## 8. Candidate 20% Remaining Amount

Candidate remaining founder reserve after the modeled 80% burn:

`6,182,123.33 GOLD`

This value is candidate-only and must be recalculated from the final approved snapshot before any burn execution.

## 9. Formula If Balance Must Be Recomputed

If a new final snapshot is taken later, use:

```text
candidate_burn_amount = founder_snapshot_balance * 0.80
candidate_remaining = founder_snapshot_balance * 0.20
```

The final arithmetic and rounding rule must match the transaction precision and the V35 burn snapshot response builder semantics.

## 10. Burn Address Approval Status

Current constants remain:

- `V35_BURN_ADDRESS = BURN_ADDRESS`

Burn address approval status:

`NOT YET FINAL-APPROVED FOR EXECUTION`

The final burn address must be explicitly approved later before signing any transaction. No burn address change was made in this phase.

## 11. Confirmation No Burn Transaction Was Created / Signed / Broadcast / Simulated

Confirmed:

- No founder burn transaction was created.
- No founder burn transaction was signed.
- No founder burn transaction was broadcast.
- No founder burn transaction was simulated.
- No funds were moved.

## 12. Confirmation V35 Remains Disabled

Confirmed in server:

- `V35_RESCUE_FORK_INDEX = Long.MAX_VALUE`
- `V35_BURN_ADDRESS = "BURN_ADDRESS"`

Confirmed in wallet:

- `V35_RESCUE_FORK_INDEX = Long.MAX_VALUE`
- `V35_BURN_ADDRESS = "BURN_ADDRESS"`

V35 activation remains `NOT YET`.

## 13. Confirmation No Fork / Jackpot Action Was Performed

Confirmed:

- No fork activation was performed.
- No final fork index was selected.
- No jackpot payout transaction was created.
- No final git tag was created.
- No final release was created.

## 14. Burn / Fork Separation Statement

Founder burn is separate from V35 fork activation.

Rules:

- The activation commit must not automatically create or broadcast a burn transaction.
- The burn must require separate explicit approval.
- The burn amount must come from a fixed, reviewed snapshot.
- The burn address must be explicitly approved.
- The burn proof must be independently verified after execution.
- The fork height must not be changed in the same human action as burn execution.

## 15. Risk Table

| Risk | Impact | Required Control |
| --- | --- | --- |
| Wrong founder balance snapshot | Burn amount can be disputed or wrong | Use fixed height/hash; archive endpoint output; independent review |
| Wrong burn amount | Irreversible overburn or underburn | Recalculate twice; approve exact amount before signing |
| Wrong burn address | Permanent loss or invalid proof | Verify exact `BURN_ADDRESS`; require burn address approver |
| Early burn before final approval | Release sequencing and public trust risk | Require explicit burn approval phase |
| Irreversible transaction | Cannot undo mistake | Require final human confirmation before signing |
| Exchange/user confusion | Operational mistakes or false public claims | Publish clear final notice that burn and fork activation are separate |
| Public notice mismatch | Proof page may contradict actual transaction | Update proof only after confirmed transaction data exists |
| Fake burn links/address attacks | Users may send funds to fake addresses | Wallet/user notices must warn against unofficial links and burn addresses |
| V35 snapshot endpoint unavailable | Final proof may lack intended endpoint output | Restore/use approved V35 endpoint or document alternate read-only snapshot source |

## 16. Approval Checklist

Before any burn execution later:

- `[ ]` Technical reviewer approves final snapshot method.
- `[ ]` Founder balance reviewer approves snapshot height/hash and account balance.
- `[ ]` Burn amount approver approves exact burn amount.
- `[ ]` Burn address approver approves exact recipient address.
- `[ ]` Release communication reviewer approves exchange/user wording.
- `[ ]` Post-burn verification reviewer approves proof archive plan.
- `[ ]` Founder wallet operator confirms wallet access and transaction procedure.
- `[ ]` Support/contact channel is ready.
- `[ ]` Public proof page is ready to distinguish pending/model/actual values.
- `[ ]` Final human go/no-go is recorded.

Approval placeholders:

- Technical reviewer: `[NAME / DATE / SIGNOFF]`
- Founder balance reviewer: `[NAME / DATE / SIGNOFF]`
- Burn amount approver: `[NAME / DATE / SIGNOFF]`
- Burn address approver: `[NAME / DATE / SIGNOFF]`
- Release communication reviewer: `[NAME / DATE / SIGNOFF]`
- Post-burn verification reviewer: `[NAME / DATE / SIGNOFF]`
- Founder wallet operator: `[NAME / DATE / SIGNOFF]`

## 17. Remaining Blockers

- The live V35 founder/supply/burn endpoints returned `404` and should not be treated as the final snapshot source until available.
- Final founder snapshot height/hash must be explicitly approved.
- Final burn amount must be explicitly approved.
- Final burn address policy must be explicitly approved.
- Exact transaction procedure remains outside this phase.
- Founder burn execution remains `NOT YET`.
- Final fork height remains candidate-only.
- Final activation and final release remain `NOT YET`.

## 18. Recommended Next Phase

Recommended next phase:

Phase 7H - founder burn snapshot source finalization, no burn execution.

Alternative if activation planning should proceed first:

Phase 6F-4 - final activation commit dry-run diff plan, no code changes.
