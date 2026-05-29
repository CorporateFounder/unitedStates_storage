# CITU V35 Phase 7H - Founder Burn Snapshot Source Finalization

## 1. Executive Summary

Phase 7H finalized the founder burn snapshot source procedure at the policy/checklist level.

Result:

- Official preferred source: final pre-activation node running reviewed code with `GET /v35/founder/burn-snapshot`.
- Current live node status: V35 founder/supply/burn endpoints returned `404`.
- Codebase status: V35 founder/supply/burn endpoints are implemented in current server code.
- Interpretation: live `404` is most likely an old deployment or non-V35-deployed node gap, not a missing-code blocker.

No source code, tests, constants, builds, historical replay, transaction, fork activation, final tag, or commit were changed or created.

No founder burn was executed.

## 2. Founder Address

Founder address:

`nNifuwmFZr7fnV1zvmpiyQDV5z7ETWvqR6GSeqeHTY43`

## 3. Current Provisional Snapshot Summary

From Phase 7G, provisional planning snapshot:

- source: `http://194.87.236.238:82/balance?address=<founder>`
- size: `506420`
- latest block index: `506419`
- latest block hash: `1090c4a987e0a44112c4a30421008603033c89114033203c455366260020085b`
- latest block timestamp: `2026-05-29T16:39:12.000+00:00`
- read time: `2026-05-29 21:54:25 +05:00`
- founder reserve used for candidate planning: `30,910,616.63 GOLD`
- candidate 80% burn: `24,728,493.30 GOLD`
- candidate 20% remaining reserve: `6,182,123.33 GOLD`

Status:

`PLANNING ONLY`

The final burn amount must be recalculated at the final approved snapshot height. No burn transaction was created.

## 4. V35 Endpoint Availability Review

Current code contains read-only endpoint mappings in:

`unitedStates_storage-master/src/main/java/International_Trade_Union/controllers/V35SupplyController.java`

Implemented endpoints:

- `GET /v35/supply`
- `GET /v35/protocol`
- `GET /v35/burn`
- `GET /v35/founder`
- `GET /v35/founder/burn-snapshot`

Current live endpoint checks from Phase 7G:

- `GET http://194.87.236.238:82/v35/founder/burn-snapshot` returned `404`.
- `GET http://194.87.236.238:82/v35/founder` returned `404`.
- `GET http://194.87.236.238:82/v35/supply` returned `404`.
- `GET http://194.87.236.238:82/v35/burn` returned `404`.

Conclusion:

- The endpoints are implemented in the current codebase.
- The endpoints are absent on the currently queried live node.
- This is likely because the live node is running an older deployment or a deployment without the V35 read-only API controller.
- It is not a missing-code blocker in the current repository.

## 5. Official Snapshot Source Hierarchy

### A. Preferred Source

Final pre-activation node running reviewed code with:

`GET /v35/founder/burn-snapshot`

Required captured data:

- endpoint URL
- raw JSON response
- read time with timezone
- snapshot height
- snapshot block hash
- founder address
- founder balance
- recommended burn percent
- modeled 80% burn amount
- modeled founder reserve after burn
- actual burned supply
- actual circulating supply
- `v35Active`
- `v35ForkIndex`
- `burnTransactionCreated`
- proof checklist fields

Verifier:

- technical reviewer
- founder balance reviewer
- burn amount approver

### B. Fallback Source

Live legacy read-only endpoints captured together:

- `GET /size`
- `GET /prevBlock`
- `GET /balance?address=<founder>`
- optional cross-check: `GET /account?address=<founder>`
- optional cross-check: `GET /dollar?address=<founder>`
- optional cross-check: `GET /stock?address=<founder>`

Required captured data:

- endpoint URLs
- raw responses
- read time with timezone
- `size`
- latest block index from `/prevBlock`
- latest block hash from `/prevBlock`
- latest block timestamp from `/prevBlock`
- founder digital dollar balance
- founder digital stock balance
- founder staking balance
- calculated 80% candidate burn
- calculated 20% remaining reserve

Verifier:

- technical reviewer
- founder balance reviewer
- burn amount approver

### C. Emergency Fallback Source

Local validated chain replay/account state, only if live endpoints are unavailable.

Required captured data:

- copied chain path
- git commit hash of validation code
- replay result
- account-state source
- snapshot height
- snapshot block hash
- founder account balance
- raw command output/log archive
- calculated 80% candidate burn
- calculated 20% remaining reserve

Verifier:

- technical reviewer
- historical replay reviewer
- founder balance reviewer

## 6. Exact Final Snapshot Procedure

Future final snapshot procedure:

1. Confirm no burn transaction has been created.
2. Confirm V35 remains disabled unless the final plan explicitly says otherwise.
3. Confirm the chosen snapshot source:
   - preferred: `/v35/founder/burn-snapshot`
   - fallback: `/size`, `/prevBlock`, `/balance?address=<founder>`
   - emergency: local validated replay/account state
4. Read `/size`.
5. Read latest block hash using `/prevBlock` or approved equivalent.
6. Read founder balance.
7. Repeat the full snapshot read 3 times within a short window.
8. If height changes between reads:
   - record every height/hash/balance tuple;
   - select one explicit final snapshot height/hash for approval;
   - do not average or blend responses.
9. Save raw endpoint responses exactly as captured.
10. Calculate candidate 80% burn amount.
11. Calculate candidate 20% remaining reserve.
12. Independently recalculate amount by a second reviewer.
13. Approve snapshot height/hash.
14. Approve exact burn amount.
15. Approve exact burn address.
16. Only after separate explicit approval, proceed to a burn execution phase.

Do not create, sign, simulate, or broadcast a burn transaction during snapshot capture.

## 7. Required Raw Evidence To Preserve

For preferred V35 endpoint source:

- raw `/v35/founder/burn-snapshot` response
- raw `/v35/founder` response
- raw `/v35/supply` response
- raw `/v35/burn` response
- read timestamp
- node URL
- git commit/release tag of the node build if available
- screenshot or terminal transcript of each call

For legacy fallback source:

- raw `/size` response
- raw `/prevBlock` response
- raw `/balance?address=<founder>` response
- raw `/account?address=<founder>` response if used
- raw `/dollar?address=<founder>` response if used
- raw `/stock?address=<founder>` response if used
- read timestamp
- node URL
- selected snapshot height/hash

For local replay fallback:

- replay command
- chain copy path
- replay output
- account-state output
- git commit hash
- snapshot height/hash
- raw logs

## 8. Burn Amount Calculation Formula

Use final approved founder snapshot balance:

```text
candidate_burn_amount = founder_snapshot_balance * 0.80
candidate_remaining = founder_snapshot_balance * 0.20
```

For two-decimal V35 modeled reporting:

```text
candidate_burn_amount = round_half_up(founder_snapshot_balance * 80 / 100, 2)
candidate_remaining = founder_snapshot_balance - candidate_burn_amount
```

The exact final amount must be independently reviewed before any transaction is created.

## 9. Burn Address Approval Status

Current constants remain:

- server `V35_BURN_ADDRESS = "BURN_ADDRESS"`
- wallet `V35_BURN_ADDRESS = "BURN_ADDRESS"`

Burn address approval status:

`NOT FINAL-APPROVED FOR EXECUTION`

The burn address must be explicitly approved in the later burn execution phase. No burn address change was made in Phase 7H.

## 10. Remaining Blockers

- Live node does not currently expose V35 founder/supply/burn endpoints.
- Preferred final snapshot source requires a final pre-activation node running reviewed code with `/v35/founder/burn-snapshot`.
- Final snapshot height/hash is not selected.
- Final burn amount is not approved.
- Final burn address is not approved for execution.
- Founder burn execution is not approved.
- V35 fork height remains candidate-only.
- Final release and activation remain not yet.

## 11. Confirmation No Code Changed

Confirmed.

No source code was changed in Phase 7H.

## 12. Confirmation No Tests Changed

Confirmed.

No tests were changed in Phase 7H.

## 13. Confirmation No Burn Transaction Was Created / Signed / Broadcast / Simulated

Confirmed:

- no burn transaction was created
- no burn transaction was signed
- no burn transaction was broadcast
- no burn transaction was simulated
- no funds were moved

## 14. Confirmation V35 Remains Disabled

Confirmed:

- `V35_RESCUE_FORK_INDEX = Long.MAX_VALUE`
- `V35_BURN_ADDRESS = BURN_ADDRESS`

No V35 activation was performed.

## 15. Confirmation No Fork / Jackpot Action Was Performed

Confirmed:

- no fork activation was performed
- no final fork index was committed
- no jackpot payout transaction was created
- no final git tag was created

## 16. Recommended Next Phase

Recommended next phase:

Phase 7I - final burn address approval and proof evidence checklist, no burn execution.

Alternative if activation planning should proceed first:

Phase 6F-4 - final activation commit dry-run diff plan, no code changes.
