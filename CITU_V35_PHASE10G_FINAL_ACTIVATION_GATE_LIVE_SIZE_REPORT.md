# CITU V35 Phase 10G Final Activation Gate Live Size Report

## 1. Executive summary

Phase 10G completed the final pre-activation gate using a fresh live `/size` read. This phase is report-only. No source code, tests, constants, transactions, deployment state, tags, release artifacts, checksums, or commits were changed or created.

The current planning candidate `514291` remains acceptable for real activation commit preparation:

- fresh live height: `506758`;
- remaining buffer: `7533` blocks;
- estimated remaining time: `17.4375` days at `432` blocks/day;
- absolute minimum required buffer: `6048` blocks / `14` days.

Decision:

```text
GO TO REAL ACTIVATION COMMIT PREPARATION ALLOWED
```

This is not activation approval and no activation was performed. A final live `/size` refresh remains mandatory immediately before any `Seting.java` edit.

## 2. Current HEAD and git status

Repository:

```text
C:\citu
```

Branch:

```text
master
```

Current HEAD:

```text
c431ce15f0512dddb07d5879f23a1748d20e607b
```

Current HEAD summary:

```text
c431ce1 phase 10F3 add local V35 activation mining dryrun evidence
```

`git status --short` returned no entries before this Phase 10G report was created. The main repository was clean at gate entry.

`git diff --name-only | findstr /i Seting.java` returned:

```text
NO_SETING_DIFF
```

## 3. Constants audit

Production constants remain disabled and aligned:

| Project | Constant | Value |
| --- | --- | --- |
| Server | `V35_RESCUE_FORK_INDEX` | `Long.MAX_VALUE` |
| Wallet | `V35_RESCUE_FORK_INDEX` | `Long.MAX_VALUE` |
| Server | `V35_BURN_ADDRESS` | `BURN_ADDRESS` |
| Wallet | `V35_BURN_ADDRESS` | `BURN_ADDRESS` |

No activation diff exists.

## 4. Fresh live `/size` result

| Item | Value |
| --- | --- |
| Endpoint | `http://194.87.236.238:82/size` |
| Read timestamp | `2026-06-01 21:04:18 +05:00` |
| Live height | `506758` |
| Planning rate | `432` blocks/day |

The live height is a moving value. This reading is a gate baseline only.

## 5. Candidate `514291` calculation

```text
remainingBlocks = 514291 - 506758 = 7533
remainingDays   = 7533 / 432 = 17.4375
```

| Check | Result |
| --- | --- |
| Candidate | `514291` |
| Remaining blocks | `7533` |
| Remaining days | `17.4375` |
| Meets absolute 14-day minimum (`6048` blocks) | YES |
| Above exact 17-day buffer (`7344` blocks) | YES |
| At fresh exact 18-day cushion (`7776` blocks) | NO, short by `243` blocks |
| Candidate stale under minimum policy | NO |

Candidate `514291` remains acceptable for real activation commit preparation. It should not be treated as immutable because the live chain continues moving.

## 6. Fresh candidate table

Calculated from live height `506758`:

| Buffer policy | Blocks | Exact candidate height | Relationship to current `514291` |
| --- | ---: | ---: | --- |
| Absolute minimum | `6048` / 14 days | `512806` | Current candidate is safer |
| Preferred lower cushion | `7344` / 17 days | `514102` | Current candidate is `189` blocks safer |
| Operational cushion | `7776` / 18 days | `514534` | Current candidate is `243` blocks lower |
| Conservative cushion | `9072` / 21 days | `515830` | Current candidate is `1539` blocks lower |

## 7. Decision

```text
GO TO REAL ACTIVATION COMMIT PREPARATION ALLOWED
```

Reason:

- `514291` remains above the absolute `6048`-block minimum;
- it remains above the preferred `17`-day planning threshold;
- Phase 10F-3 isolated local activation and mining dry-run passed;
- production constants remain disabled and aligned;
- the main repository was clean at gate entry.

This gate allows preparation only. It does not activate V35 and does not authorize an automatic constants edit.

## 8. Required next step

Proceed to a separately approved real activation commit preparation phase.

Immediately before any `Seting.java` edit:

1. Read live `http://194.87.236.238:82/size` again.
2. Recalculate remaining blocks to `514291`.
3. Reject `514291` if fewer than `6048` blocks remain.
4. Prefer refreshing to a new 18-day candidate if scheduling or review delay materially reduces the cushion.
5. Require explicit human approval for the final fork height.
6. Keep `V35_BURN_ADDRESS = BURN_ADDRESS`.
7. Keep founder burn separate from activation.

## 9. Remaining risks

- Live height continues moving, so the current calculation will age.
- Candidate `514291` is below a newly calculated exact 18-day cushion.
- Final activation still requires an explicit fork-height approval and isolated two-file activation diff.
- Server and wallet tests, clean packages, final artifacts, checksums, tag, multilingual notices, operator communication, and monitoring readiness remain future gated steps.
- Founder burn remains separate and must not be mixed into activation.

## 10. Explicit safety confirmation

- No source code changed in Phase 10G.
- No tests changed or ran in Phase 10G.
- No `Seting.java` changed.
- `V35_RESCUE_FORK_INDEX` remains `Long.MAX_VALUE` in server and wallet.
- `V35_BURN_ADDRESS` remains `BURN_ADDRESS` in server and wallet.
- No V35 activation occurred.
- No founder burn transaction was created, signed, simulated, or broadcast.
- No jackpot payout transaction was created.
- No deployment occurred.
- No tag was created.
- No release artifact was created.
- No checksum was created.
- No commit was created.
