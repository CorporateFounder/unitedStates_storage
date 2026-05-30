# CITU V35 Phase 10D - 18-Day Operational Cushion Policy Report

## 1. Executive summary

Phase 10D updates the V35 fork-height planning policy to include an operational
cushion above the strict `17-day` minimum.

This is a planning report only. No activation was approved or performed.

Recommended default planning candidate:

`514291`

This candidate is based on an `18-day / 7776-block` operational buffer. It is
not activated, not committed to code, and not final-approved.

## 2. Live height source

| Item | Value |
| --- | --- |
| Source URL | `http://194.87.236.238:82/size` |
| Read method | Read-only HTTP `GET` |
| Read time | `2026-05-30 19:22:06 +05:00` |
| Live height returned | `506515` |

The chain height is moving. This value is a planning baseline only.

## 3. Updated buffer policy

| Policy | Days | Blocks at `432 blocks/day` | Purpose |
| --- | ---: | ---: | --- |
| Minimum buffer | `17` | `7344` | Hard lower bound |
| Default operational buffer | `18` | `7776` | Recommended planning default |
| Conservative buffer | `21` | `9072` | Additional review and notice time |

Reason for the policy update:

An exact `17-day` candidate can become stale as soon as the next block lands.
The `18-day` default provides a one-day operational cushion while preserving
the `17-day` hard minimum.

## 4. Candidate table

| Candidate type | Calculation | Height | Status |
| --- | --- | ---: | --- |
| `17-day` minimum | `506515 + 7344` | `513859` | Minimum only |
| `18-day` default operational | `506515 + 7776` | `514291` | Recommended planning default |
| `21-day` conservative | `506515 + 9072` | `515587` | Conservative alternative |

## 5. Recommended default candidate

Use:

`514291`

Status:

`PLANNING ONLY - NOT ACTIVATED - NOT COMMITTED TO CODE - NOT FINAL-APPROVED`

Read live `/size` again immediately before any real activation approval. At
that review, the selected fork height must still have at least `7344` blocks
remaining.

## 6. Constants audit

| Project | `V35_RESCUE_FORK_INDEX` | `V35_BURN_ADDRESS` |
| --- | --- | --- |
| Server | `Long.MAX_VALUE` | `BURN_ADDRESS` |
| Wallet | `Long.MAX_VALUE` | `BURN_ADDRESS` |

Server and wallet match. V35 remains disabled.

## 7. Burn separation policy

- Founder burn remains separate from fork activation.
- Do not create, sign, simulate, or broadcast a burn transaction during the
  activation commit.
- Founder burn should happen later through the Phase 7J runbook unless a
  separate explicit approval selects another reviewed sequence.
- Keep `V35_BURN_ADDRESS = BURN_ADDRESS` unless a separate burn-policy approval
  explicitly changes it.

## 8. Remaining blockers before real activation

- Update the future activation diff plan to use planning candidate `514291`.
- Refresh live `/size` again immediately before final activation approval.
- Confirm at least `7344` blocks remain at that final review.
- Explicitly approve one final fork height.
- Apply only matching server/wallet `Seting.java` fork-height changes in a
  separate approved activation phase.
- Run server and wallet tests and clean packages after the future edit.
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
deployment, tag, release artifact, checksum, build, historical replay, or full
test run was performed.

## 13. Recommended next phase

Recommended next phase:

**Phase 6F-8 - update the future activation diff plan to candidate `514291`,
no activation.**

Before any real activation commit, refresh live `/size` again and enforce the
`17-day / 7344-block` hard minimum.
