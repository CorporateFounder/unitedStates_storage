# CITU V35 Phase 6F-7 - Go/No-Go Review for Candidate 513858

## 1. Executive summary

Phase 6F-7 performed a fresh read-only live-height check for planning candidate
`513858`.

Result:

`WAIT - REFRESH HEIGHT BEFORE NEXT PLANNING STEP`

Candidate `513858` is stale under the strict `17-day / 7344-block` policy. It
has `7343` blocks remaining, a shortfall of one block.

Refreshed planning-only candidates:

- default `17-day` candidate: `513859`
- conservative `21-day` candidate: `515587`

No activation was approved or performed.

## 2. Fresh live height

| Item | Value |
| --- | --- |
| Source URL | `http://194.87.236.238:82/size` |
| Read method | Read-only HTTP `GET` |
| Read time | `2026-05-30 19:19:10 +05:00` |
| Live height returned | `506515` |

The chain height is moving. This is a planning baseline only.

## 3. Candidate `513858` freshness calculation

```text
remaining_blocks = 513858 - 506515 = 7343
remaining_days = 7343 / 432 = 16.997685...
required_buffer = 7344 blocks / 17 days
shortfall = 7344 - 7343 = 1 block
```

| Check | Result |
| --- | --- |
| Candidate | `513858` |
| Remaining blocks | `7343` |
| Estimated remaining days | `16.9977` |
| Required minimum | `7344 blocks / 17 days` |
| Meets policy | NO |
| Shortfall | `1 block` |
| Classification | STALE |

Refreshed candidates:

| Policy | Calculation | Candidate |
| --- | --- | ---: |
| Default `17-day` | `506515 + 7344` | `513859` |
| Conservative `21-day` | `506515 + 9072` | `515587` |

## 4. Constants audit

| Project | `V35_RESCUE_FORK_INDEX` | `V35_BURN_ADDRESS` |
| --- | --- | --- |
| Server | `Long.MAX_VALUE` | `BURN_ADDRESS` |
| Wallet | `Long.MAX_VALUE` | `BURN_ADDRESS` |

Server and wallet match. V35 remains disabled.

## 5. Go/no-go table

| Gate | Status | Notes |
| --- | --- | --- |
| Candidate `513858` freshness | NO-GO | One block below `7344`-block minimum |
| Refreshed candidate calculation | READY | Default `513859`; conservative `515587` |
| Activation diff plan | NEEDS REFRESH | Replace stale candidate before any approval |
| Test-only preparation in `master` | PASS | Phase 6F-5F complete |
| Website RU / EN / ZH pages | PASS | Pre-activation pages prepared |
| Public notice templates RU / EN / ZH | PASS | Placeholders prepared |
| Monitoring / rollback runbook | PASS | Phase 10A complete |
| Founder burn runbook | PASS / SEPARATE | Phase 7J ready; no execution |
| Final activation commit | NOT YET | Constants remain disabled |
| Final tests and clean packages after activation commit | NOT YET | Must follow approved edit |
| Final artifacts / checksums / tag | NOT YET | Must follow approved edit and review |
| Deployment | NOT YET | No deployment performed |

## 6. Remaining blockers before real activation

- Replace stale `513858` in the planning diff with a freshly calculated
  candidate.
- Read live `/size` again immediately before any activation approval.
- Require at least `7344` remaining blocks at that review.
- Explicitly approve one final fork height.
- Apply only the two matching server/wallet `Seting.java` fork-height edits in
  a separate approved activation phase.
- Run server and wallet tests and clean packages after the future edit.
- Generate and verify final artifacts and SHA-256 checksums.
- Create the final tag only after review.
- Fill, review, and publish RU / EN / ZH final notices.
- Assign monitoring owners, support contacts, and exchange coordination.
- Keep founder burn snapshot, address, amount, timing, and execution separate
  from activation.

## 7. Recommendation

Recommendation:

`WAIT AND REFRESH HEIGHT`

Do not use candidate `513858`.

Use `513859` only as the next planning baseline. Because the chain height is
moving, refresh `/size` again before the next go/no-go review. Do not approve a
real activation commit automatically.

## 8. Confirmation no code changed

Confirmed. No source code was changed.

## 9. Confirmation no tests changed or run

Confirmed. No tests were changed or run.

## 10. Confirmation V35 remains disabled

Confirmed:

- server `V35_RESCUE_FORK_INDEX = Long.MAX_VALUE`
- wallet `V35_RESCUE_FORK_INDEX = Long.MAX_VALUE`
- server/wallet `V35_BURN_ADDRESS = BURN_ADDRESS`

## 11. Safety confirmation

No activation, founder burn transaction, fork action, jackpot payout,
deployment, tag, release artifact, checksum, build, historical replay, or full
test run was performed.

## 12. Recommended next phase

Recommended next phase:

**Phase 10D - select a stable activation candidate with an operational cushion
above the strict 17-day minimum, no activation.**

Using a candidate exactly at the moving minimum causes immediate staleness.
Choose a reviewed planning buffer above `7344` blocks before returning to the
activation go/no-go gate.
