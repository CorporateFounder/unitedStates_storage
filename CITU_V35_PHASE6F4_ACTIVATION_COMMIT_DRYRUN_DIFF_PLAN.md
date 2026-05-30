# CITU V35 Phase 6F-4 - Activation Commit Dry-Run Diff Plan

## 1. Executive Summary

Phase 6F-4 created an exact dry-run diff plan for a future V35 activation commit.

This is a planning document only.

No source code, tests, settings, artifacts, transactions, signatures, broadcasts, simulations, fork activation, jackpot payout, final release, tag, or commit were changed or created.

Candidate future fork height:

`513332`

This value is candidate-only. It is not applied, not committed, and not final-approved. Before any real activation commit, it must be refreshed against live `/size` if time has passed and explicitly approved.

## 2. Exact Files Inspected

Settings files:

- `unitedStates_storage-master/src/main/java/International_Trade_Union/setings/Seting.java`
- `unitedStates_final-master/src/main/java/International_Trade_Union/setings/Seting.java`

Duplicate settings audit:

- Recursive search found exactly the two `Seting.java` files listed above.
- No additional `Seting.java` copies were found.

Reports reviewed:

- `CITU_V35_PHASE6F3_FINAL_FORK_HEIGHT_GO_NO_GO_CHECKLIST.md`
- `CITU_V35_PHASE7G_FOUNDER_SNAPSHOT_BURN_APPROVAL_CHECKLIST.md`
- `CITU_V35_PHASE7H_FOUNDER_BURN_SNAPSHOT_SOURCE_FINALIZATION.md`
- `CITU_V35_PHASE7I_BURN_ADDRESS_APPROVAL_PROOF_EVIDENCE_CHECKLIST.md`
- `CITU_V35_PHASE8G_FINAL_PRERELEASE_PACKAGE_APPROVAL_REPORT.md`
- `CITU_V35_PHASE9E_FINAL_PRE_ACTIVATION_SAFETY_AUDIT_REPORT.md`
- `CITU_V35_PHASE8C_RELEASE_NOTES_FINAL.md`
- `CITU_V35_PHASE6F2_FINAL_ACTIVATION_COMMIT_CHECKLIST.md`

Workspace check:

- `git status --short`

## 3. Current Constants Table

| Constant | Server | Wallet | Status |
| --- | --- | --- | --- |
| `V35_RESCUE_FORK_INDEX` | `Long.MAX_VALUE` | `Long.MAX_VALUE` | Match, V35 disabled |
| `V35_BURN_ADDRESS` | `"BURN_ADDRESS"` | `"BURN_ADDRESS"` | Match, sentinel configured |
| `V35_TARGET_BLOCK_TIME_SECONDS` | `200` | `200` | Match |
| Expected blocks/day | `432` planning model | `432` planning model | Match |
| `V35_GOLD_REWARD` | `8.00` | `8.00` | Match |
| `V35_INSTANT_MINER_REWARD` | `6.00` | `6.00` | Match |
| `V35_JACKPOT_REWARD` | `2.00` | `2.00` | Match |
| `V35_FOUNDER_REWARD_ENABLED` | `false` | `false` | Match |
| `V35_EXPECTED_FOUNDER_REWARD` | `0.00` | `0.00` | Match |

V35 economics remain:

- target block time: `200 seconds`
- expected blocks/day: `432`
- total reward: `8 GOLD/block`
- miner reward: `6 GOLD`
- jackpot reserve: `2 GOLD`
- founder reward after V35: `0 GOLD`

## 4. Proposed Future Diff Table

Future activation commit candidate diff:

| File | Constant | Current Value | Candidate Future Value | Apply Now? |
| --- | --- | --- | --- | --- |
| `unitedStates_storage-master/src/main/java/International_Trade_Union/setings/Seting.java` | `V35_RESCUE_FORK_INDEX` | `Long.MAX_VALUE` | `513332` | NO |
| `unitedStates_final-master/src/main/java/International_Trade_Union/setings/Seting.java` | `V35_RESCUE_FORK_INDEX` | `Long.MAX_VALUE` | `513332` | NO |
| server/wallet `Seting.java` | `V35_BURN_ADDRESS` | `"BURN_ADDRESS"` | no change proposed | NO |

Illustrative dry-run diff only:

```diff
-    long V35_RESCUE_FORK_INDEX = Long.MAX_VALUE;
+    long V35_RESCUE_FORK_INDEX = 513332;
```

This illustrative diff must not be applied until:

- the height is refreshed against live `/size`;
- the notice buffer remains at least 14 days, preferably 16 days;
- the height is explicitly approved;
- server and wallet values are reviewed together.

## 5. Candidate Fork Height Used

Phase 6F-3 baseline:

- live baseline height: `506420`
- read time: `2026-05-29 21:49:24 +05:00`
- recommended buffer: `16 days / 6912 blocks`
- candidate future height: `513332`

Status:

`CANDIDATE ONLY`

If significant time passes before the real activation commit, recalculate:

```text
candidate_fork_height = latest_live_size + 6912
```

Do not use a candidate that has fallen below the 14-day minimum notice buffer.

## 6. Burn Address Status

Current status:

- server `V35_BURN_ADDRESS = "BURN_ADDRESS"`
- wallet `V35_BURN_ADDRESS = "BURN_ADDRESS"`
- sentinel is configured
- final burn address is not approved for burn execution

Planned activation diff:

- Do not change `V35_BURN_ADDRESS` in the activation commit unless a separate final policy explicitly approves a replacement.
- Burn address approval remains a blocker before any real founder burn transaction.
- Founder burn execution remains separate from fork activation.

## 7. Exact Future Files To Change

Expected activation commit files only:

- `unitedStates_storage-master/src/main/java/International_Trade_Union/setings/Seting.java`
- `unitedStates_final-master/src/main/java/International_Trade_Union/setings/Seting.java`

Optional documentation updates:

- final activation notice
- exchange/operator notice
- wallet user notice
- release manifest
- final checksum document

Do not include unrelated files.

Do not include:

- `release-dryrun/`
- private keys
- wallet databases
- blockchain resources
- node state
- logs
- cache/temp files
- copied `target/` directories

## 8. Safety Rules For Real Activation Commit

The real activation commit must:

- change server and wallet fork height consistently;
- use one explicitly approved fork height;
- keep wallet/server burn address values consistent;
- not change reward values;
- not change PoW policy;
- not change difficulty policy;
- not change jackpot policy;
- not change transaction format;
- not change block format;
- not change mining behavior;
- not change validation behavior;
- not change balance behavior;
- not create or broadcast a founder burn transaction;
- not create a jackpot payout transaction;
- not include dry-run artifacts or sensitive state.

Stop and review if any unexpected diff appears.

## 9. Future Verification Commands

Do not run these commands now.

### Pre-edit status

```powershell
cd C:\citu
git status --short
```

### Refresh live height

```powershell
Invoke-RestMethod -Uri 'http://194.87.236.238:82/size' -TimeoutSec 15
```

### Verify constants after approved edit

```powershell
Select-String -Path C:\citu\unitedStates_storage-master\src\main\java\International_Trade_Union\setings\Seting.java -Pattern 'V35_RESCUE_FORK_INDEX|V35_BURN_ADDRESS'
Select-String -Path C:\citu\unitedStates_final-master\src\main\java\International_Trade_Union\setings\Seting.java -Pattern 'V35_RESCUE_FORK_INDEX|V35_BURN_ADDRESS'
```

### Verify exact diff

```powershell
git diff -- unitedStates_storage-master/src/main/java/International_Trade_Union/setings/Seting.java unitedStates_final-master/src/main/java/International_Trade_Union/setings/Seting.java
```

### Server tests

```powershell
cd C:\citu\unitedStates_storage-master
.\mvnw.cmd test
```

### Wallet tests

```powershell
cd C:\citu\unitedStates_final-master
.\mvnw.cmd test
```

### Server clean package

```powershell
cd C:\citu\unitedStates_storage-master
.\mvnw.cmd clean package
```

### Wallet clean package

```powershell
cd C:\citu\unitedStates_final-master
.\mvnw.cmd clean package
```

### SHA-256 generation

Use final artifact paths only:

```powershell
Get-FileHash '<FINAL_SERVER_ARTIFACT>' -Algorithm SHA256
Get-FileHash '<FINAL_WALLET_ARTIFACT>' -Algorithm SHA256
```

### Final tag

Only after review:

```powershell
git tag v35-activation-fork-<APPROVED_INDEX>
```

## 10. Real Activation Go/No-Go Checklist

- `[ ]` Exact fork height explicitly approved.
- `[ ]` Latest `/size` captured immediately before approval.
- `[ ]` Fork height remains at least 14 days ahead.
- `[ ]` Prefer 16-day buffer unless 21-day conservative window is selected.
- `[ ]` Server/wallet fork index values match exactly.
- `[ ]` Founder burn policy approved.
- `[ ]` Burn address approved or explicitly deferred by release owner.
- `[ ]` Founder burn execution remains separate.
- `[ ]` Exchange/operator final notice ready.
- `[ ]` Wallet user final notice ready.
- `[ ]` Deposit/withdrawal caution window defined.
- `[ ]` Support/contact channel defined.
- `[ ]` Monitoring plan ready.
- `[ ]` Rollback/emergency communication plan ready.
- `[ ]` Git clean before edit.
- `[ ]` No dry-run artifacts staged.
- `[ ]` Exact post-edit diff reviewed by two humans.
- `[ ]` Server tests pass.
- `[ ]` Wallet tests pass.
- `[ ]` Server clean package passes.
- `[ ]` Wallet clean package passes.
- `[ ]` Final checksums generated and verified.
- `[ ]` Final tag created only after review.

## 11. Remaining Blockers

- Candidate height `513332` is not final-approved.
- Live `/size` must be refreshed before any real activation commit.
- Final burn address is not approved for execution.
- Final founder snapshot and burn amount are not approved.
- Founder burn execution decision remains separate and not yet.
- Final monitoring plan is not yet approved.
- Final rollback/emergency communication plan is not yet approved.
- Final build, checksums, tag, notices, and release remain not yet.

## 12. Confirmation No Code Changed

Confirmed.

No source code was changed in Phase 6F-4.

## 13. Confirmation No Tests Changed

Confirmed.

No tests were changed in Phase 6F-4.

## 14. Confirmation V35 Remains Disabled

Confirmed:

- `V35_RESCUE_FORK_INDEX = Long.MAX_VALUE`
- `V35_BURN_ADDRESS = BURN_ADDRESS`

No fork height was applied.

## 15. Confirmation No Burn / Fork / Jackpot Action Was Performed

Confirmed:

- no founder burn transaction was created
- no founder burn transaction was signed
- no founder burn transaction was broadcast
- no founder burn transaction was simulated
- no fork activation was performed
- no jackpot payout transaction was created

## 16. Confirmation No Final Tag / Release Was Created

Confirmed:

- no final git tag was created
- no final release was created
- no final release was announced
- exchanges, operators, and users were not told to upgrade

## 17. Recommended Next Phase

Recommended next phase:

Phase 10A - post-activation monitoring and rollback runbook finalization, no activation.

Alternative if founder-burn operational documentation is the next priority:

Phase 7J - founder burn execution runbook finalization, no burn execution.
