# CITU V35 Phase 6F-3 - Final Fork Height Selection and Activation Go/No-Go Checklist

## 1. Executive Summary

Phase 6F-3 created a final fork-height planning baseline and activation go/no-go checklist.

This phase did not activate V35, did not select a final committed fork height, did not change source code or tests, did not build artifacts, did not create a burn transaction, did not create a jackpot payout transaction, and did not create a final git tag.

Live height was read from the requested endpoint and used only as a moving planning baseline.

Recommended planning candidate:

- Baseline height: `506420`
- Recommended 16-day candidate: `513332`

This is a candidate only. It is not activated, not committed to code, and not final until explicitly approved in a later phase.

## 2. Baseline Height Source

Source URL:

`http://194.87.236.238:82/size`

Height returned:

`506420`

Reading timestamp:

`2026-05-29 21:49:24 +05:00`

Important note:

The live chain height is moving. This height is only a planning baseline. The final activation fork height must be recalculated from the live height at the time of final approval.

If the endpoint had been unavailable, the manual fallback height would have been `506419`. The endpoint was available, so the live height `506420` was used.

## 3. Candidate Fork Height Table

Planning assumptions:

- `432` blocks/day
- 14 days = `6048` blocks
- 16 days = `6912` blocks
- 21 days = `9072` blocks

| Baseline Height | Buffer | Candidate Fork Height | Estimated Notice | Approx Target Date/Time From Baseline | Risk Level |
| --- | ---: | ---: | --- | --- | --- |
| `506420` | `6048` blocks | `512468` | 14 days | 2026-06-12 21:49 +05:00 | Minimum acceptable buffer |
| `506420` | `6912` blocks | `513332` | 16 days | 2026-06-14 21:49 +05:00 | Recommended default |
| `506420` | `9072` blocks | `515492` | 21 days | 2026-06-19 21:49 +05:00 | Conservative, lower coordination risk |

These heights are candidates only. They must not be treated as activated or final.

## 4. Recommended Buffer

Recommended default:

`16 days / 6912 blocks`

Recommended candidate fork height from the current planning baseline:

`513332`

Reason:

- It is more conservative than the 14-day minimum.
- It gives exchanges and node operators more notice than the minimum window.
- It avoids a short 3-day or 7-day activation plan.
- It is less slow than the 21-day conservative option while still adding buffer.

Use the 21-day candidate `515492` if exchange readiness, final artifact approval, support coverage, founder burn sequencing, or monitoring staffing needs additional time.

## 5. Go/No-Go Checklist

| Item | Status | Notes |
| --- | --- | --- |
| Git clean | NEEDS REVIEW | `git status --short` showed untracked `release-dryrun/`; no source/test changes were reported before this report |
| Server historical replay | PASS | Reached the end |
| Wallet historical replay | PASS | Reached the end |
| Replay-only safety gate | PASS | Live validation remains strict |
| Server/wallet parity | PASS | Replay behavior mirrored |
| Release notes | PASS | Created |
| Artifact dry-run | PASS | Server/wallet dry-run builds passed |
| Package review | PASS | Dry-run package reviewed clean |
| Checksum polish | PASS | `SHA256SUMS.txt` and `.sha256` verified |
| Notice drafts | PASS | Exchange/operator/wallet notices created |
| Founder burn review | PASS / REVIEWED ONLY | Burn not executed |
| Server/wallet final build | NOT YET | Must run after activation commit |
| Final checksums | NOT YET | Must be generated from final artifacts |
| Final tag | NOT YET | Must wait for final approval |
| Exchange/operator final notice | NOT YET | Must include exact fork height and final checksums |
| Founder burn execution | NOT YET | Separate explicit approval required |
| Final monitoring plan | NOT YET | Must be ready before activation |
| V35 constants still disabled | YES | `Long.MAX_VALUE`, `BURN_ADDRESS` |

## 6. Activation Blockers

Do not perform the activation commit until these blockers are resolved:

- Exact fork height must be explicitly approved.
- Final founder burn policy must be approved.
- Final burn address policy must be approved.
- Founder burn execution decision must be made separately.
- Final build/checksum must be completed after the activation commit.
- Final git tag must be created only after review.
- Exchanges and node operators must receive the final notice.
- Final notice must include exact fork height, final tag, final checksums, estimated activation time, support/contact channel, and monitoring instructions.
- Post-activation monitoring plan must be ready.
- Deposit/withdrawal caution window must be defined for exchanges.
- Final git working tree must be clean except explicitly approved release files.

## 7. Future Activation Sequence

Safe future sequence:

A. Choose the final fork height from a fresh live height baseline.

B. Make the final founder burn execution decision.

C. Create the activation commit by changing only wallet/server `Seting.java`:

- server `V35_RESCUE_FORK_INDEX`
- wallet `V35_RESCUE_FORK_INDEX`

D. Run server tests and wallet tests.

E. Run server and wallet clean package.

F. Generate final artifacts and SHA-256 checksums.

G. Create final reviewed tag.

H. Publish exchange, node operator, and wallet user final notices.

I. Monitor chain before and after fork height:

- last legacy block
- first V35 block
- V35 reward split
- PoW target validation
- cumulativeWork behavior
- jackpot reserve/accounting
- burn no-spend behavior
- APIs/dashboard/website
- exchange sync confirmations

## 8. Confirmation No Code Changed

Confirmed.

No source code was changed in Phase 6F-3.

## 9. Confirmation No Tests Changed

Confirmed.

No tests were changed in Phase 6F-3.

## 10. Confirmation V35 Remains Disabled

Confirmed in server:

- `V35_RESCUE_FORK_INDEX = Long.MAX_VALUE`
- `V35_BURN_ADDRESS = "BURN_ADDRESS"`

Confirmed in wallet:

- `V35_RESCUE_FORK_INDEX = Long.MAX_VALUE`
- `V35_BURN_ADDRESS = "BURN_ADDRESS"`

No real fork height was committed.

## 11. Confirmation No Burn / Fork / Jackpot Action Was Performed

Confirmed:

- No founder burn transaction was created.
- No founder burn transaction was signed.
- No founder burn transaction was broadcast.
- No V35 fork activation was performed.
- No jackpot payout transaction was created.

## 12. Confirmation No Final Tag / Release Was Created

Confirmed:

- No final git tag was created.
- No final release was created.
- No final release was announced.
- Operators, exchanges, and users were not told to upgrade.

## 13. Recommended Next Phase

Recommended next phase:

Phase 7G - final founder snapshot and burn approval checklist, no burn execution.

Alternative if activation planning should proceed before burn sequencing:

Phase 6F-4 - final activation commit dry-run diff plan, no code changes.
