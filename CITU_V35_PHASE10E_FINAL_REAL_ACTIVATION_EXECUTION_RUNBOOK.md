# CITU V35 Phase 10E - Final Real Activation Execution Runbook

## 1. Executive summary

This is the future real-activation execution runbook only. Do not run its
activation commands without a separate explicit approval.

Current planning candidate:

`514291`

This candidate is planning-only. V35 remains disabled.

## 2. Current safety audit

| Check | Result |
| --- | --- |
| Current branch | `master` |
| Current HEAD | `13c34a2362acfa88b0c12f73b150b2af1205596a` |
| Workspace status | One untracked planning report: `CITU_V35_PHASE10D_18DAY_OPERATIONAL_CUSHION_POLICY_REPORT.md` |
| Server `V35_RESCUE_FORK_INDEX` | `Long.MAX_VALUE` |
| Wallet `V35_RESCUE_FORK_INDEX` | `Long.MAX_VALUE` |
| Server/wallet `V35_BURN_ADDRESS` | `BURN_ADDRESS` |
| V35 state | DISABLED |

Current V35 economics remain aligned:

- target block time: `200 seconds`
- total reward: `8 GOLD`
- miner reward: `6 GOLD`
- jackpot reserve: `2 GOLD`
- founder reward after V35: `0 GOLD`

## 3. Activation-day fork-height refresh rule

Before any activation commit:

```powershell
cd C:\citu
git status --short
git branch --show-current
$liveSize = Invoke-RestMethod -Uri 'http://194.87.236.238:82/size' -Method Get -TimeoutSec 15
$remaining = 514291 - [long]$liveSize
"LIVE_SIZE=$liveSize"
"REMAINING_BLOCKS=$remaining"
"REMAINING_DAYS=$([math]::Round($remaining / 432, 4))"
```

Decision rule:

| Remaining blocks to `514291` | Required action |
| --- | --- |
| `>= 6048` | Candidate may proceed to explicit human review |
| `< 6048` | Reject candidate `514291`; do not edit constants |

If rejected:

```powershell
$newDefault = [long]$liveSize + 7776
$newConservative = [long]$liveSize + 9072
"NEW_18_DAY_DEFAULT=$newDefault"
"NEW_21_DAY_CONSERVATIVE=$newConservative"
```

The real-activation minimum is `14 days / 6048 blocks`. Prefer `16-17 days`
remaining at approval time where possible. The final height always requires
explicit human approval.

## 4. Real activation branch procedure

Run only after explicit approval:

```powershell
cd C:\citu
git switch master
git status --short
git branch --show-current
git switch -c v35-activation-final-<FINAL_APPROVED_HEIGHT>
```

Abort if the workspace is not clean or the starting branch is not `master`.

Edit only:

- `unitedStates_storage-master/src/main/java/International_Trade_Union/setings/Seting.java`
- `unitedStates_final-master/src/main/java/International_Trade_Union/setings/Seting.java`

## 5. Exact activation diff rule

Apply the same one-line change in both files:

```diff
-    long V35_RESCUE_FORK_INDEX = Long.MAX_VALUE;
+    long V35_RESCUE_FORK_INDEX = <FINAL_APPROVED_HEIGHT>;
```

Keep unchanged:

```java
String V35_BURN_ADDRESS = "BURN_ADDRESS";
```

Do not change anything else.

## 6. Post-edit verification

```powershell
cd C:\citu
git --no-pager diff --stat
git --no-pager diff --name-only
git --no-pager diff -- `
  unitedStates_storage-master/src/main/java/International_Trade_Union/setings/Seting.java `
  unitedStates_final-master/src/main/java/International_Trade_Union/setings/Seting.java

Select-String -Path `
  C:\citu\unitedStates_storage-master\src\main\java\International_Trade_Union\setings\Seting.java, `
  C:\citu\unitedStates_final-master\src\main\java\International_Trade_Union\setings\Seting.java `
  -Pattern 'V35_RESCUE_FORK_INDEX|V35_BURN_ADDRESS|V35_GOLD_REWARD|V35_INSTANT_MINER_REWARD|V35_JACKPOT_REWARD|V35_TARGET_BLOCK_TIME_SECONDS|V35_FOUNDER_REWARD_ENABLED|V35_EXPECTED_FOUNDER_REWARD'
```

Required result:

- exactly two changed files;
- server/wallet fork height values match exactly;
- `V35_BURN_ADDRESS = BURN_ADDRESS`;
- rewards remain `8 / 6 / 2 / 0`;
- target remains `200 seconds`;
- no PoW, block-format, transaction-format, replay-compatibility, website, or
  artifact file changes.

## 7. Test and build sequence

Run after the approved edit:

```powershell
cd C:\citu\unitedStates_storage-master
.\mvnw.cmd test
.\mvnw.cmd clean package

cd C:\citu\unitedStates_final-master
.\mvnw.cmd test
.\mvnw.cmd clean package
```

Website:

- run its existing test/build commands only if final website notices change;
- do not deploy until final approval.

## 8. Commit rule

Do not use `git add .`.

Stage only:

```powershell
cd C:\citu
git add -- `
  unitedStates_storage-master/src/main/java/International_Trade_Union/setings/Seting.java `
  unitedStates_final-master/src/main/java/International_Trade_Union/setings/Seting.java

git --no-pager diff --cached --stat
git --no-pager diff --cached --name-only
git --no-pager diff --cached
git commit -m "phase 10E activate V35 at fork height <FINAL_APPROVED_HEIGHT>"
```

The activation commit must contain no artifacts, checksums, tag, website
deployment, burn transaction, or jackpot payout.

## 9. Final release sequence

After the approved activation commit:

1. Run server and wallet tests and clean packages.
2. Create the final artifact package.
3. Generate `SHA256SUMS.txt`.
4. Generate `SHA256SUMS.txt.sha256`.
5. Create the final release manifest.
6. Review the package contents and checksums.
7. Create the final tag only after review.
8. Fill and review RU / EN / ZH public notices.
9. Update and deploy the website only after approval.
10. Notify exchanges and node operators with final tagged artifacts.
11. Start the Phase 10A monitoring runbook.

## 10. Abort conditions

Stop activation immediately if:

- git workspace is not clean;
- starting branch is not `master`;
- live `/size` leaves fewer than `6048` blocks to the candidate;
- final height lacks explicit human approval;
- server and wallet `Seting.java` values differ;
- `V35_BURN_ADDRESS` changes;
- any production file beyond the two settings files changes;
- reward, PoW, block-format, transaction-format, or replay-only logic changes;
- server or wallet tests fail;
- server or wallet package fails;
- a burn transaction appears;
- a jackpot payout transaction appears;
- final notices, checksums, operator communication, or exchange communication
  are not ready;
- monitoring owners or support contacts are not assigned.

## 11. Burn separation policy

- Founder burn is not part of the activation commit.
- Do not create, sign, simulate, or broadcast a burn transaction during
  activation.
- Founder burn should happen later through the Phase 7J runbook unless a
  separate explicit approval selects another reviewed sequence.
- Do not combine an irreversible burn with the fork activation window.

## 12. Confirmation no code changed

Confirmed. No source code was changed.

## 13. Confirmation no tests changed or run

Confirmed. No tests were changed or run.

## 14. Confirmation V35 remains disabled

Confirmed:

- server `V35_RESCUE_FORK_INDEX = Long.MAX_VALUE`
- wallet `V35_RESCUE_FORK_INDEX = Long.MAX_VALUE`
- server/wallet `V35_BURN_ADDRESS = BURN_ADDRESS`

## 15. Safety confirmation

No activation, founder burn transaction, fork action, jackpot payout,
deployment, tag, release artifact, checksum, build, historical replay, or test
run was performed.

## 16. Recommended next phase

Recommended next phase:

**Phase 6F-8 - update the candidate-only activation diff plan to `514291`, no
activation.**

Before any real activation approval, clear or intentionally commit planning
reports so the workspace is clean, then refresh live `/size` again.
