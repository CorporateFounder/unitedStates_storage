# CITU V35 Phase 10B - Final Activation Readiness Review

## 1. Executive Summary

Phase 10B consolidated the completed V35 pre-activation gates and refreshed the candidate fork-height planning buffer.

Result:

`READY FOR LOCAL ACTIVATION DRY-RUN BRANCH`

This is not approval for a real activation commit.

V35 remains disabled:

- `V35_RESCUE_FORK_INDEX = Long.MAX_VALUE`
- `V35_BURN_ADDRESS = "BURN_ADDRESS"`

Live height refresh:

- source: `http://194.87.236.238:82/size`
- read time: `2026-05-30 14:29:21 +05:00`
- live height: `506496`
- planning-only candidate: `513332`
- remaining blocks: `6836`
- estimated remaining notice: `15.82 days`

Candidate `513332` remains acceptable as a planning candidate. It is still above the 14-day minimum and close to the recommended 16-day window. It must still be refreshed and explicitly approved before any real activation commit.

## 2. Readiness Gate Table

| Gate | Status | Notes |
| --- | --- | --- |
| Historical replay - server | PASS | External replay reached the end |
| Historical replay - wallet | PASS | External replay reached the end |
| Replay-only safety | PASS | Live validation remains strict |
| Server/wallet parity | PASS | Replay compatibility mirrored |
| Release notes | PASS | Phase 8C complete |
| Dry-run artifacts/checksums | PASS | Phase 8D complete |
| Package review | PASS | Phase 8E complete |
| Checksum polish | PASS | Phase 8E-2 complete |
| Exchange/operator/user notice drafts | PASS | Phase 8F complete |
| Website V35 public pages | PASS | Phase 8W complete |
| Multilingual website RU / EN / ZH | PASS | Phase 8W-2 localized four operational pages |
| Multilingual activation templates RU / EN / ZH | PASS | Phase 8X complete |
| Founder snapshot source procedure | PASS / PROCEDURE READY | Final snapshot not captured yet |
| Burn address checklist | PASS / CHECKLIST READY | Final recipient not approved |
| Burn execution runbook | PASS / RUNBOOK READY | No execution performed |
| Activation dry-run diff plan | PASS | Candidate-only diff documented |
| Monitoring/rollback runbook | PASS | Phase 10A complete |
| Final fork-height approval | NOT YET | `513332` remains candidate-only |
| Final founder-burn decision | NOT YET | Timing and execution require explicit approval |
| Final activation commit | NOT YET | Constants remain disabled |
| Final build/checksum/tag | NOT YET | Must follow approved activation commit |
| Final deployment/public announcement | NOT YET | Placeholders only |

## 3. Fork Height Freshness Review

Planning assumptions:

- candidate fork height: `513332`
- expected production: `432 blocks/day`
- minimum notice buffer: `6048 blocks / 14 days`
- recommended buffer: `6912 blocks / 16 days`

Read-only live check:

```text
URL: http://194.87.236.238:82/size
Read time: 2026-05-30 14:29:21 +05:00
Live height: 506496
```

Calculation:

```text
remaining_blocks = 513332 - 506496 = 6836
remaining_days = 6836 / 432 = 15.824...
```

Assessment:

| Check | Result |
| --- | --- |
| Remaining blocks | `6836` |
| Estimated remaining days | `15.82` |
| At least 14 days | YES |
| Near recommended 16 days | YES |
| Candidate stale | NO, planning candidate remains acceptable |
| Final-approved | NO |

Rule:

Refresh `/size` again immediately before the local dry-run and again before any real activation approval. If remaining buffer falls below `6048` blocks, recalculate a new candidate.

## 4. Constants Audit

Server:

- `unitedStates_storage-master/src/main/java/International_Trade_Union/setings/Seting.java`
- `V35_RESCUE_FORK_INDEX = Long.MAX_VALUE`
- `V35_BURN_ADDRESS = "BURN_ADDRESS"`

Wallet:

- `unitedStates_final-master/src/main/java/International_Trade_Union/setings/Seting.java`
- `V35_RESCUE_FORK_INDEX = Long.MAX_VALUE`
- `V35_BURN_ADDRESS = "BURN_ADDRESS"`

Assessment:

- Server and wallet match.
- No fork index was applied.
- V35 remains disabled.

## 5. Website / Public Communication Audit

Existing V35 website operational routes:

- `/v35/preactivation-status`
- `/v35/operator-guide`
- `/v35/checksums`
- `/v35/burn-warning`

Phase 8W-2 confirmed each operational page includes:

- English (`EN`)
- Russian (`RU`)
- Chinese (`ZH`)

Multilingual activation templates exist:

- `CITU_V35_PHASE8X_FINAL_PUBLIC_ACTIVATION_NOTICE_TEMPLATE.md`
- `CITU_V35_PHASE8X_EXCHANGE_FINAL_NOTICE_TEMPLATE.md`
- `CITU_V35_PHASE8X_NODE_OPERATOR_FINAL_NOTICE_TEMPLATE.md`
- `CITU_V35_PHASE8X_WALLET_USER_FINAL_NOTICE_TEMPLATE.md`

Audit result:

- Public operational pages still say V35 is not active.
- Candidate height `513332` is described as planning-only.
- No final tag or checksum is claimed.
- No founder burn is claimed.
- Users are warned against fake burn addresses.
- Operators and exchanges are told to wait for final tagged artifacts.

Remaining public-communication work:

- Fill final placeholders only after approval.
- Consider a later localization pass for older English-only V35 policy pages.

## 6. Burn Readiness Audit

Current burn status:

- Final snapshot source procedure: documented.
- Preferred source: reviewed node `/v35/founder/burn-snapshot`.
- Final snapshot captured: `NO`.
- Final founder balance approved: `NO`.
- Final 80% burn amount approved: `NO`.
- Final burn address approved: `NO`.
- Burn execution runbook: documented.
- Burn transaction created, signed, simulated, or broadcast: `NO`.

Burn/fork separation:

- Founder burn is a separate operation.
- Activation commit must not create or broadcast a burn transaction.
- Burn is a blocker before activation only if the release owner decides burn must occur before activation.
- If burn is deferred, that decision must be explicit and public wording must continue to say burn is pending.

## 7. Remaining Blockers Before Real Activation

- Final fork height must be refreshed from live `/size` and explicitly approved.
- Local activation dry-run branch should be completed first.
- Final activation commit must change server/wallet fork constants consistently.
- Server and wallet tests must pass after the activation commit.
- Server and wallet clean packages must be built after the activation commit.
- Final artifacts and SHA-256 checksums must be generated and verified.
- Final tag must be created only after review.
- RU / EN / ZH website and public notice placeholders must be filled and reviewed.
- Exchange/operator final notice must be sent.
- Deposit/withdrawal caution window must be finalized.
- Monitoring owners and support contacts must be assigned.
- Burn timing decision must be explicit: execute before activation, defer until after activation, or keep pending.
- If burn proceeds, snapshot, amount, recipient, transaction draft, signing, broadcast, and proof require separate approvals.

## 8. Recommendation

Recommended:

`A. LOCAL ACTIVATION DRY-RUN BRANCH`

Do not perform a real activation commit yet.

The local dry-run should:

1. Create an isolated local branch.
2. Refresh live `/size`.
3. Use a clearly documented test-only candidate height for the local exercise.
4. Change wallet/server fork constants together only inside that local branch.
5. Review the exact diff.
6. Run server and wallet tests/packages.
7. Generate dry-run artifacts/checksums.
8. Confirm no deployment, tag, burn, or public announcement.
9. Discard or preserve the branch only according to an explicit follow-up decision.

Real activation remains a later, separately approved operation.

## 9. Confirmation No Code Changed

Confirmed.

No source code was changed in Phase 10B.

## 10. Confirmation No Tests Changed

Confirmed.

No tests were changed or run in Phase 10B.

## 11. Confirmation V35 Remains Disabled

Confirmed:

- server `V35_RESCUE_FORK_INDEX = Long.MAX_VALUE`
- wallet `V35_RESCUE_FORK_INDEX = Long.MAX_VALUE`
- server/wallet `V35_BURN_ADDRESS = BURN_ADDRESS`

## 12. Confirmation No Burn / Fork / Jackpot Action

Confirmed:

- no founder burn transaction was created, signed, simulated, or broadcast;
- no fork activation was performed;
- no jackpot payout transaction was created.

## 13. Confirmation No Deployment / Tag / Final Release

Confirmed:

- no website deployment was performed;
- no final git tag was created;
- no final release was published;
- no production-upgrade instruction was issued.

## 14. Recommended Next Phase

Recommended next phase:

**Phase 6F-5 - local activation dry-run branch, no real activation and no deployment.**
