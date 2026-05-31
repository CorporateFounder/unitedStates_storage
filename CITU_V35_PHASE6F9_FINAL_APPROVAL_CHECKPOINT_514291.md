# CITU V35 Phase 6F-9 - Final Approval Checkpoint for Candidate 514291

## 1. Executive summary

Phase 6F-9 performed a read-only final approval checkpoint for planning candidate `514291`.

Recommendation: **GO FOR REAL ACTIVATION COMMIT PREPARATION ONLY**.

The candidate remains acceptable for preparation: it has `7690` blocks remaining, approximately `17.80` days at `432` blocks/day. This exceeds the real activation approval minimum of `6048` blocks (`14` days).

This is not automatic approval to activate V35. The live height must be refreshed again immediately before any activation edit, and the real activation commit still requires explicit human approval.

## 2. Fresh live height

| Field | Value |
| --- | --- |
| Source URL | `http://194.87.236.238:82/size` |
| Method | Read-only HTTP GET |
| Read time | `2026-05-31 12:17:35 +05:00` |
| Live height | `506601` |

## 3. Candidate 514291 remaining buffer

```text
remaining_blocks = 514291 - 506601 = 7690
remaining_days = 7690 / 432 = 17.8009 days
minimum_blocks = 6048
margin_above_minimum = 7690 - 6048 = 1642 blocks
```

| Check | Result |
| --- | --- |
| Candidate height | `514291` |
| Remaining blocks | `7690` |
| Estimated remaining days | `17.80` |
| Real activation minimum | `6048` blocks / `14` days |
| Margin above minimum | `1642` blocks / approximately `3.80` days |
| Candidate acceptable for activation planning | **YES** |
| Candidate refresh required now | **NO** |

The planning default remains based on the `18`-day operational cushion policy. Immediately before a real activation commit, `/size` must be refreshed. If fewer than `6048` blocks remain, reject `514291` and calculate:

```text
new_18_day_candidate = current_live_height + 7776
new_21_day_candidate = current_live_height + 9072
```

## 4. Approval checklist

| Check | Status | Evidence / note |
| --- | --- | --- |
| Working tree clean before report creation | **PASS** | `git status --short` returned no changes |
| Current branch safe | **PASS** | Current branch is `master`; no activation edit was made |
| Candidate height still acceptable | **PASS** | `7690 >= 6048` blocks |
| Activation diff plan ready | **PASS** | Phase 6F-8 defines the future two-file diff for `514291` |
| Test-only preparation in master | **PASS** | Phase 6F-5F merged test-only activation-mode preparation |
| Website RU / EN / ZH ready | **PASS** | Phase 8W-2 multilingual website patch report exists |
| Public notices RU / EN / ZH ready | **PASS** | Phase 8X multilingual notice templates report exists |
| Monitoring / rollback ready | **PASS** | Phase 10A runbook exists |
| Burn separated from activation | **PASS** | Phase 7J burn runbook remains separate |
| Explicit human approval to execute activation | **NOT YET** | Required before activation branch edits |
| Final activation commit | **NOT YET** | No activation diff applied |
| Final build / checksum / tag | **NOT YET** | Must follow reviewed activation commit |
| Deployment | **NOT YET** | No deployment performed |

## 5. Constants audit

| Project | Constant | Current value | Status |
| --- | --- | --- | --- |
| Server | `V35_RESCUE_FORK_INDEX` | `Long.MAX_VALUE` | Disabled |
| Wallet | `V35_RESCUE_FORK_INDEX` | `Long.MAX_VALUE` | Disabled |
| Server | `V35_BURN_ADDRESS` | `BURN_ADDRESS` | Sentinel unchanged |
| Wallet | `V35_BURN_ADDRESS` | `BURN_ADDRESS` | Sentinel unchanged |

Server and wallet constants match. V35 remains disabled.

## 6. Remaining blockers

- Obtain explicit human approval before creating the real activation branch or editing constants.
- Refresh live `/size` immediately before any activation edit and confirm at least `6048` blocks remain.
- Confirm a clean working tree before the real activation branch is created.
- Apply and review only the two intended `Seting.java` fork-height edits.
- Run server and wallet tests and clean packages after the future activation edit.
- Generate final artifacts, checksums, manifest, and tag only after activation diff review.
- Fill and publish final RU / EN / ZH notices only after approval.
- Confirm exchange/operator communication and monitoring ownership.
- Keep founder burn separate and execute it later only under the Phase 7J runbook if explicitly approved.

## 7. Recommendation

Proceed with **real activation commit preparation only**.

Do not activate automatically. The next execution phase must start with a fresh `/size` read and explicit human authorization before any settings edit.

## 8. Confirmation no code changed

No source code was changed in Phase 6F-9.

## 9. Confirmation no tests changed or run

No tests were changed or run in Phase 6F-9.

## 10. Confirmation V35 remains disabled

V35 remains disabled in server and wallet:

```text
V35_RESCUE_FORK_INDEX = Long.MAX_VALUE
V35_BURN_ADDRESS = BURN_ADDRESS
```

## 11. Confirmation no activation or release action

Phase 6F-9 performed no burn transaction, fork activation, jackpot payout, deployment, final tag, release artifact creation, or checksum generation.

## 12. Recommended next phase

Recommended next phase: **Phase 6F-10 - explicitly approved real activation branch preparation and two-file settings diff**, following the Phase 10E runbook. Refresh `/size` before any edit and stop if fewer than `6048` blocks remain.
