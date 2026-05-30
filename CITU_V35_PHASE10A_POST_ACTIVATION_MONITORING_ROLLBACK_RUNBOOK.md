# CITU V35 Phase 10A - Post-Activation Monitoring and Rollback Runbook

## 1. Executive Summary

This is a future-launch monitoring and rollback runbook only.

V35 is not active. Candidate fork height `513332` remains planning-only and must be refreshed against live `/size`, explicitly approved, and committed in a separate activation phase.

Current disabled constants remain:

- `V35_RESCUE_FORK_INDEX = Long.MAX_VALUE`
- `V35_BURN_ADDRESS = "BURN_ADDRESS"`

No source code, tests, artifacts, deployment, tag, transaction, fork activation, burn, or jackpot payout was changed or created in Phase 10A.

## 2. Files and Documents Reviewed

Reports read:

- `CITU_V35_PHASE6F4_ACTIVATION_COMMIT_DRYRUN_DIFF_PLAN.md`
- `CITU_V35_PHASE6F3_FINAL_FORK_HEIGHT_GO_NO_GO_CHECKLIST.md`
- `CITU_V35_PHASE8W_WEBSITE_PUBLIC_INSTRUCTION_AUDIT_REPORT.md`
- `CITU_V35_PHASE8F_EXCHANGE_NOTICE_DRAFT.md`
- `CITU_V35_PHASE8F_NODE_OPERATOR_NOTICE_DRAFT.md`
- `CITU_V35_PHASE8F_WALLET_USER_NOTICE_DRAFT.md`
- `CITU_V35_PHASE7H_FOUNDER_BURN_SNAPSHOT_SOURCE_FINALIZATION.md`
- `CITU_V35_PHASE7I_BURN_ADDRESS_APPROVAL_PROOF_EVIDENCE_CHECKLIST.md`
- `CITU_V35_PHASE9E_FINAL_PRE_ACTIVATION_SAFETY_AUDIT_REPORT.md`
- `CITU_V35_PHASE8G_FINAL_PRERELEASE_PACKAGE_APPROVAL_REPORT.md`
- `CITU_V35_PHASE8C_RELEASE_NOTES_FINAL.md`
- `CITU_V35_PHASE6E_RELEASE_ROLLBACK_EXCHANGE_CHECKLIST.md`

Files inspected:

- `unitedStates_storage-master/src/main/java/International_Trade_Union/setings/Seting.java`
- `unitedStates_final-master/src/main/java/International_Trade_Union/setings/Seting.java`
- `unitedStates_storage-master/src/main/java/International_Trade_Union/controllers/V35SupplyController.java`
- workspace status through `git status --short`

Reviewed server endpoints:

- `GET /v35/protocol`
- `GET /v35/supply`
- `GET /v35/burn`
- `GET /v35/founder`
- `GET /v35/founder/burn-snapshot`

## 3. Monitoring Timeline

| Window | Required checks | Escalation threshold |
| --- | --- | --- |
| T-minus 24h | Confirm approved fork height, blocks remaining, estimated time, final tag/checksums, server/wallet constants, operator acknowledgements, backups, support channel, and monitoring owners. | Any constant mismatch, artifact mismatch, missing operator notice, or unresolved consensus concern: pause release. |
| T-minus 1h | Read `/size` and `/prevBlock`; record height/hash/time; confirm expected remaining blocks; verify exchange caution window; confirm monitoring bridge staffed. | Unexpected release binary, node divergence, or poor operator readiness: issue hold notice before fork. |
| Fork block window | Record last legacy block and first V35 block height/hash/time; compare server and wallet tips; inspect first V35 reward, target PoW, and cumulativeWork. | Chain stall, differing tips, incorrect reward, invalid target behavior, or widespread rejection: declare investigation. |
| First 10 blocks | Record every height/hash, observed block interval, reward split, jackpot reserve behavior, endpoint status, node sync, and exchange reports. | Any repeat rejection, reorg, stall, or accounting mismatch: recommend exchange temporary hold. |
| First 100 blocks | Sample block intervals and tips continuously; review cumulativeWork growth, difficulty behavior, reserve accounting, APIs, dashboard, website wording, and operator reports. | Persistent instability, unexpected payout, incorrect burn behavior, or diverging tips: escalate to incident review. |
| First 24h | Summarize produced blocks, average intervals, reorgs, rejected blocks, sync reports, API health, and exchange deposit/withdrawal status. | Any unresolved consensus or accounting issue: keep caution measures active and prepare reviewed mitigation. |
| First 7 days | Daily health review: production rate, target adjustment, cumulativeWork, jackpot reserve, burn/supply reporting, wallet/server parity, exchange sync, and public guidance. | Trend deterioration, repeated reorgs, API scan load, or operator divergence: open follow-up hardening phase. |

## 4. Endpoints and Metrics To Monitor

### Chain health

- `/size`: accepted chain size and height progression.
- `/prevBlock` if available: latest index, hash, timestamp, reward transactions, and continuity.
- Server and wallet accepted tip height/hash.
- Block production intervals against the `200 second` target.
- Blocks remaining before the approved fork height.
- Rejected blocks, reorgs, peer divergence, and stall duration.

### V35 status

- `/v35/protocol`: V35 active status, fork index, and protocol model.
- `/v35/supply`: total minted, burned supply, circulating supply, founder reserve, jackpot paid, and annual emission.
- `/v35/founder`: founder reserve and founder reward status.
- `/v35/founder/burn-snapshot`: snapshot/proof status and modeled-versus-actual separation.
- `/v35/burn`: burn address, actual burn status, and actual burned supply.

The queried live node previously returned `404` for V35 endpoints because it did not yet run the reviewed V35 deployment. Before activation, verify these endpoints on the final pre-activation reviewed node. Endpoint failure must remain visible; do not replace it with invented values.

### Consensus checks after fork

- First V35 block index equals the approved fork height.
- Miner reward is exactly `6 GOLD`.
- Jackpot reserve accrual is exactly `2 GOLD`.
- Founder reward is exactly `0 GOLD`.
- Total V35 reward is exactly `8 GOLD`.
- V35 PoW target validation is active.
- cumulativeWork increases sanely and server/wallet select the same canonical chain.
- No replay-only compatibility path is reachable from normal live validation.
- No unexpected founder burn transaction exists.
- No unexpected jackpot payout transaction exists.
- `BURN_ADDRESS` behavior matches approved policy.

### Operational checks

- Server and wallet nodes report the same tip.
- Known node operators confirm sync.
- Exchanges confirm expected tip and deposit/withdrawal status.
- Wallet dashboard shows truthful live or unavailable status.
- Website wording matches the final release state and burn-proof state.
- API latency and supply-scan performance remain acceptable.

## 5. Success Criteria

Activation observation is healthy only when:

- The chain continues producing blocks without an unexplained stall.
- Server and wallet agree on canonical height and hash.
- V35 activates only at the approved fork height.
- The first V35 reward split is `6 miner / 2 jackpot reserve / 0 founder`.
- V35 target PoW and cumulativeWork behavior are sane.
- Replay-only compatibility remains isolated from live validation.
- V35 endpoints report the expected active status on reviewed nodes.
- Burn and supply endpoints separate planned/model values from actual values.
- No unexpected founder burn or jackpot payout transaction appears.
- Exchanges and operators report no sync issue.
- Public pages match the approved release notice.

## 6. Failure Criteria

Treat any of the following as a red flag:

- Server and wallet constants or fork heights differ.
- Server and wallet disagree on canonical tip height/hash.
- Blocks stop arriving beyond the expected interval without explanation.
- V35 activates before or after the approved height.
- Miner, jackpot reserve, founder, or total reward differs from `6 / 2 / 0 / 8`.
- Founder reward appears after V35.
- Jackpot reserve does not accumulate as expected.
- An unexpected jackpot payout transaction appears.
- Burn sink behavior is incorrect or an unexpected burn transaction appears.
- Many upgraded nodes reject the same block.
- Reorgs, chain splits, or cumulativeWork disagreement appear.
- Replay-only historical compatibility appears reachable during live validation.
- Exchange deposit/withdrawal issues appear.
- Website or operator instructions disagree with the final release.
- V35 endpoint failures or API load prevent reliable monitoring.

## 7. Rollback and Mitigation Procedure

Blockchain rollback after a consensus fork is not a routine rollback. Do not publish contradictory binaries or silently choose a chain.

### Before fork height

1. Pause public activation announcement if not yet published.
2. Notify operators and exchanges to stop production upgrade activity or remain on the previous reviewed release.
3. Record current `/size`, `/prevBlock`, binary version, constants, and checksums.
4. Classify the issue as configuration, artifact, deployment, API/UI, or consensus.
5. If needed, prepare a reviewed release that delays the fork or restores `Long.MAX_VALUE`.
6. Re-run the required release gates in a separate approved phase.
7. Do not execute founder burn while the issue is unresolved.

### At or after fork height

1. Open an incident bridge and publish an investigation notice.
2. Recommend exchanges pause deposits/withdrawals if consensus, balances, or accounting may be affected.
3. Preserve logs and record:
   - last agreed legacy block index/hash;
   - first V35 block index/hash;
   - current competing tip index/hash values;
   - server/wallet constants and artifact checksums;
   - cumulativeWork values;
   - rejected block messages;
   - peer and exchange sync reports.
4. Classify the issue:
   - configuration or deployment mismatch;
   - API/UI-only reporting;
   - reward/accounting;
   - PoW/difficulty/cumulativeWork;
   - burn policy;
   - jackpot policy;
   - broader consensus validation.
5. For API/UI-only issues, patch the read-only layer separately without changing consensus.
6. For consensus issues, prepare one reviewed emergency plan and one canonical recovery instruction set.
7. Do not create burn or jackpot payout transactions during investigation.
8. Publish recovery instructions, canonical checkpoint information, and resumption criteria only after review.

### Evidence archive

Preserve:

- timestamps with timezone;
- endpoint raw responses;
- block indexes, hashes, and timestamps;
- node versions and artifact SHA-256 values;
- wallet/server constant verification;
- operator and exchange acknowledgements;
- logs and screenshots;
- incident decisions and approver names.

## 8. Communication Templates

### Pre-fork all clear

> V35 pre-fork monitoring is healthy. The approved activation height is `[HEIGHT]`; current height is `[CURRENT HEIGHT]`. Final reviewed artifacts and checksums are `[LINK]`. Continue monitoring. The block height is authoritative; the time estimate is approximate.

### Activation reached

> V35 activation height `[HEIGHT]` has been reached. First V35 block: `[HASH]`. Validation of reward split, PoW target, cumulativeWork, node sync, and API status is in progress. Please continue the agreed caution window.

### Post-fork healthy

> V35 post-fork monitoring is healthy through block `[HEIGHT]`. Server and wallet tips agree at `[HASH]`. Reward split is confirmed as `6 miner / 2 jackpot reserve / 0 founder`. No unexpected burn or jackpot payout transaction has been observed.

### Investigation notice

> We are investigating a V35 launch issue affecting `[SCOPE]`. Do not publish independent fixes or change constants. Preserve logs and report node version, artifact checksum, height, hash, and error details to `[SUPPORT CONTACT]`.

### Operator action required

> Operator action is required for `[ISSUE]`. Follow only the reviewed instruction set at `[OFFICIAL LINK]`. Verify tag `[TAG]` and SHA-256 values before any change. Report resulting height/hash to `[SUPPORT CONTACT]`.

### Exchange temporary hold notice

> Exchanges should temporarily pause deposits and withdrawals while `[ISSUE]` is investigated. Trading policy remains each exchange's decision. Resume only after the official canonical-chain checkpoint and reviewed release instructions are published.

## 9. Roles and Approval Placeholders

| Role | Name / contact | Responsibility |
| --- | --- | --- |
| Launch lead | `[NAME / CONTACT]` | Go/no-go ownership and timeline |
| Consensus reviewer | `[NAME / CONTACT]` | Reward, PoW, cumulativeWork, validation checks |
| Server operator | `[NAME / CONTACT]` | Server tip, logs, API status |
| Wallet operator | `[NAME / CONTACT]` | Wallet tip, dashboard, parity |
| Exchange coordinator | `[NAME / CONTACT]` | Exchange acknowledgements and caution window |
| Public communications reviewer | `[NAME / CONTACT]` | Website and notice accuracy |
| Founder burn reviewer | `[NAME / CONTACT]` | Burn remains separate; proof review if later executed |
| Incident approver | `[NAME / CONTACT]` | Emergency mitigation approval |

## 10. Confirmation No Code Changed

Confirmed.

No source code was changed in Phase 10A.

## 11. Confirmation No Tests Changed

Confirmed.

No tests were changed or run in Phase 10A.

## 12. Confirmation V35 Remains Disabled

Confirmed in server and wallet:

- `V35_RESCUE_FORK_INDEX = Long.MAX_VALUE`
- `V35_BURN_ADDRESS = "BURN_ADDRESS"`

Candidate height `513332` remains planning-only. It was not applied or committed.

## 13. Confirmation No Burn / Fork / Jackpot Action Was Performed

Confirmed:

- no founder burn transaction was created, signed, simulated, or broadcast;
- no fork activation was performed;
- no jackpot payout transaction was created;
- no final git tag was created;
- no website deployment was performed.

## 14. Remaining Blockers

- Final fork height must be refreshed from live `/size` and explicitly approved.
- Founder burn address, snapshot, amount, and execution decision remain separate approvals.
- Final build, checksums, tag, and release notices remain pending.
- Final support contacts and monitoring owners must be filled.
- Exchange caution window and acknowledgement process must be finalized.
- Final reviewed node must expose the read-only V35 endpoints before launch monitoring.

## 15. Recommended Next Phase

Recommended next phase:

**Phase 8X - final public activation notice template, placeholders only, no activation.**

After that, use a separate explicitly approved phase to refresh live height, select the real fork index, and perform the activation commit.
