# CITU V35 Phase 6F-8 - 18-Day Activation Diff Plan Update

## 1. Executive summary

Phase 6F-8 updates the future V35 activation diff plan to the `18-day /
7776-block` operational planning candidate:

`514291`

This is a planning report only. The diff below was not applied. V35 remains
disabled.

## 2. Current constants audit

Inspected files:

- `unitedStates_storage-master/src/main/java/International_Trade_Union/setings/Seting.java`
- `unitedStates_final-master/src/main/java/International_Trade_Union/setings/Seting.java`

| Project | `V35_RESCUE_FORK_INDEX` | `V35_BURN_ADDRESS` |
| --- | --- | --- |
| Server | `Long.MAX_VALUE` | `BURN_ADDRESS` |
| Wallet | `Long.MAX_VALUE` | `BURN_ADDRESS` |

Server and wallet match. V35 remains disabled.

## 3. Superseded candidates

| Candidate | Status | Reason |
| ---: | --- | --- |
| `513332` | SUPERSEDED | Stale under the `17-day` policy |
| `513858` | SUPERSEDED | Too close to the strict `17-day` minimum; stale after the next height read |
| `514291` | CURRENT PLANNING DEFAULT | Based on the `18-day / 7776-block` operational cushion |

The final fork height must be refreshed again against live `/size` immediately
before real activation approval.

## 4. New candidate `514291`

Phase 10D calculated:

```text
18_day_default_candidate = 506515 + 7776 = 514291
```

Status:

`PLANNING ONLY - NOT ACTIVATED - NOT COMMITTED TO CODE - NOT FINAL-APPROVED`

## 5. Exact future activation diff plan

Expected future activation files only:

- `unitedStates_storage-master/src/main/java/International_Trade_Union/setings/Seting.java`
- `unitedStates_final-master/src/main/java/International_Trade_Union/setings/Seting.java`

Illustrative future diff for each file:

```diff
-    long V35_RESCUE_FORK_INDEX = Long.MAX_VALUE;
+    long V35_RESCUE_FORK_INDEX = 514291;
```

Future diff table:

| File | Constant | Current value | Candidate future value | Apply now? |
| --- | --- | --- | --- | --- |
| Server `Seting.java` | `V35_RESCUE_FORK_INDEX` | `Long.MAX_VALUE` | `514291` | NO |
| Wallet `Seting.java` | `V35_RESCUE_FORK_INDEX` | `Long.MAX_VALUE` | `514291` | NO |
| Server/wallet `Seting.java` | `V35_BURN_ADDRESS` | `BURN_ADDRESS` | no change | NO |

## 6. Burn separation policy

- Founder burn is separate from fork activation.
- Do not create, sign, simulate, or broadcast a burn transaction during the
  activation commit.
- Founder burn should happen later only through the Phase 7J runbook unless a
  separate explicit approval selects another reviewed sequence.
- Keep `V35_BURN_ADDRESS = BURN_ADDRESS`.

## 7. Real activation commit safety rules

The future activation commit must:

- start from a clean `master` workspace;
- refresh live `/size` and obtain explicit human approval for the final height;
- change only the two server/wallet `Seting.java` fork-height constants;
- use the same final approved height in both projects;
- keep `V35_BURN_ADDRESS = BURN_ADDRESS`;
- not change rewards, PoW, difficulty, block format, transaction format,
  replay-only compatibility, or website content;
- not create a burn transaction;
- not create a jackpot payout transaction;
- not include artifacts, checksums, deployment files, or tags;
- stop for review if any unrelated diff appears.

## 8. Remaining blockers before real activation

- Refresh live `/size` immediately before activation approval.
- Confirm candidate `514291` still has at least `6048` blocks remaining.
- Prefer `16-17` days remaining at approval time where possible.
- Reject `514291` and calculate a new `18-day` candidate if it falls below the
  real-activation minimum.
- Explicitly approve one final fork height.
- Apply the two-file activation diff in a separate approved phase.
- Review the exact post-edit diff.
- Run server and wallet tests and clean packages after the edit.
- Generate and verify final artifacts and SHA-256 checksums.
- Create the final tag only after review.
- Fill, review, and publish RU / EN / ZH final notices.
- Assign monitoring owners, support contacts, and exchange coordination.
- Keep founder burn snapshot, address, amount, timing, and execution as
  separate approvals.

## 9. Confirmation no code changed

Confirmed. No source code was changed.

## 10. Confirmation no tests changed or run

Confirmed. No tests were changed or run.

## 11. Confirmation V35 remains disabled

Confirmed:

- server `V35_RESCUE_FORK_INDEX = Long.MAX_VALUE`
- wallet `V35_RESCUE_FORK_INDEX = Long.MAX_VALUE`
- server/wallet `V35_BURN_ADDRESS = BURN_ADDRESS`

## 12. Safety confirmation

No activation, founder burn transaction, fork action, jackpot payout,
deployment, tag, release artifact, checksum, build, historical replay, or test
run was performed.

## 13. Recommended next phase

Recommended next phase:

**Phase 6F-9 - final activation approval checkpoint with a fresh live `/size`
read, no activation.**

Use the Phase 10E execution runbook only after that checkpoint explicitly
approves a final height.
