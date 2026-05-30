# CITU V35 Phase 10C - 17-Day Fork-Height Refresh Report

## 1. Executive summary

Phase 10C refreshed the V35 planning baseline from the live read-only `/size`
endpoint. This is a planning report only. No activation, code change, test
change, build, deployment, tag, release artifact, or checksum action was
performed.

The previous planning candidate `513332` is stale under the updated mandatory
`17-day / 7344-block` buffer policy.

Recommended refreshed planning candidate:

`513858`

Conservative `21-day` planning candidate:

`515586`

Both values remain candidate-only. Neither value is approved or applied.

## 2. Live height source

| Item | Value |
| --- | --- |
| Source URL | `http://194.87.236.238:82/size` |
| Read method | Read-only HTTP `GET` |
| Read time | `2026-05-30 19:09:06 +05:00` |
| Live height returned | `506514` |

The live height is moving. This reading is a planning baseline only.

## 3. Updated 17-day buffer policy

Planning assumptions:

| Policy | Days | Blocks at `432 blocks/day` |
| --- | ---: | ---: |
| Minimum and recommended buffer | `17` | `7344` |
| Conservative buffer | `21` | `9072` |

The previous `14-day` minimum is no longer acceptable for final activation
planning.

## 4. Old candidate `513332` review

Calculation:

```text
remaining_blocks = 513332 - 506514 = 6818
remaining_days = 6818 / 432 = 15.7824...
required_17_day_blocks = 7344
shortfall = 7344 - 6818 = 526
```

Assessment:

| Check | Result |
| --- | --- |
| Old candidate | `513332` |
| Remaining blocks | `6818` |
| Estimated remaining days | `15.78` |
| At least `17 days / 7344 blocks` | NO |
| Shortfall | `526 blocks` |
| Status under updated policy | STALE |

Do not use `513332` for final activation planning.

## 5. Refreshed candidate table

| Item | Calculation | Height / result |
| --- | --- | ---: |
| Current live height | live `/size` | `506514` |
| Old candidate | previous plan | `513332` |
| Remaining to old candidate | `513332 - 506514` | `6818 blocks` |
| Remaining days to old candidate | `6818 / 432` | `15.78 days` |
| Old candidate status | below `7344` blocks | `STALE` |
| Refreshed 17-day candidate | `506514 + 7344` | `513858` |
| Refreshed 21-day conservative candidate | `506514 + 9072` | `515586` |

## 6. Constants audit

| Project | `V35_RESCUE_FORK_INDEX` | `V35_BURN_ADDRESS` |
| --- | --- | --- |
| Server | `Long.MAX_VALUE` | `BURN_ADDRESS` |
| Wallet | `Long.MAX_VALUE` | `BURN_ADDRESS` |

Server and wallet constants match. V35 remains disabled. No activation commit
has been made.

## 7. Recommendation

- Retire `513332` as a stale planning candidate.
- Use `513858` as the refreshed default planning candidate.
- Keep `515586` available as the conservative `21-day` alternative.
- Refresh `/size` again immediately before any separately approved activation
  commit.
- Require at least `7344` remaining blocks at that final review.
- Keep founder burn execution separate from activation.
- Do not mix burn transaction creation, signing, simulation, or broadcast into
  the activation commit.

## 8. Confirmation no code changed

Confirmed. No source code was changed.

## 9. Confirmation no tests changed

Confirmed. No tests were changed or run.

## 10. Confirmation V35 remains disabled

Confirmed:

- server `V35_RESCUE_FORK_INDEX = Long.MAX_VALUE`
- wallet `V35_RESCUE_FORK_INDEX = Long.MAX_VALUE`
- server/wallet `V35_BURN_ADDRESS = BURN_ADDRESS`

## 11. Safety confirmation

No activation, founder burn transaction, fork action, jackpot payout,
deployment, tag, release artifact, checksum, build, or historical replay was
performed.

## 12. Recommended next phase

Recommended next phase:

**Phase 6F-6 - refreshed activation commit dry-run diff plan using candidate
`513858`, no activation.**

Before any real activation commit, read live `/size` again and recalculate the
17-day candidate if the remaining buffer has moved below policy.
