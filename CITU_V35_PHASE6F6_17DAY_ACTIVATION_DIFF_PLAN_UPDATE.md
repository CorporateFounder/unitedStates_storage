# CITU V35 Phase 6F-6 - 17-Day Activation Diff Plan Update

## 1. Executive summary

Phase 6F-6 updates the future V35 activation diff plan for the refreshed
`17-day / 7344-block` buffer policy.

This is a planning report only. The diff shown below was not applied.

The previous candidate `513332` is stale. The refreshed planning candidate is:

`513858`

This candidate is not final-approved. Read live `/size` again immediately
before any separately approved activation commit.

## 2. Current constants audit

Inspected files:

- `unitedStates_storage-master/src/main/java/International_Trade_Union/setings/Seting.java`
- `unitedStates_final-master/src/main/java/International_Trade_Union/setings/Seting.java`

| Project | `V35_RESCUE_FORK_INDEX` | `V35_BURN_ADDRESS` |
| --- | --- | --- |
| Server | `Long.MAX_VALUE` | `BURN_ADDRESS` |
| Wallet | `Long.MAX_VALUE` | `BURN_ADDRESS` |

Server and wallet match. V35 remains disabled.

## 3. Old candidate `513332` status

Phase 10C read live height `506514` from
`http://194.87.236.238:82/size` at `2026-05-30 19:09:06 +05:00`.

| Item | Value |
| --- | ---: |
| Old candidate | `513332` |
| Remaining blocks at Phase 10C baseline | `6818` |
| Estimated remaining days | `15.78` |
| Required buffer | `7344 blocks / 17 days` |
| Shortfall | `526 blocks` |
| Status | STALE |

Do not use `513332` for final activation planning.

## 4. New candidate `513858`

Phase 10C calculated:

```text
refreshed_17_day_candidate = 506514 + 7344 = 513858
```

Status:

`CANDIDATE ONLY - NOT APPLIED - NOT FINAL-APPROVED`

The `21-day / 9072-block` conservative alternative remains `515586`.

## 5. Exact future activation diff plan

Expected future activation files only:

- `unitedStates_storage-master/src/main/java/International_Trade_Union/setings/Seting.java`
- `unitedStates_final-master/src/main/java/International_Trade_Union/setings/Seting.java`

Illustrative future diff for each file:

```diff
-    long V35_RESCUE_FORK_INDEX = Long.MAX_VALUE;
+    long V35_RESCUE_FORK_INDEX = 513858;
```

Future diff table:

| File | Constant | Current value | Candidate future value | Apply now? |
| --- | --- | --- | --- | --- |
| Server `Seting.java` | `V35_RESCUE_FORK_INDEX` | `Long.MAX_VALUE` | `513858` | NO |
| Wallet `Seting.java` | `V35_RESCUE_FORK_INDEX` | `Long.MAX_VALUE` | `513858` | NO |
| Server/wallet `Seting.java` | `V35_BURN_ADDRESS` | `BURN_ADDRESS` | no change | NO |

## 6. Burn separation policy

- Founder burn is separate from fork activation.
- Founder burn is not required inside the activation commit.
- The activation commit must not create, sign, simulate, or broadcast a burn
  transaction.
- Founder burn should occur later only through the Phase 7J runbook unless a
  separate explicit approval selects another reviewed sequence.
- `V35_BURN_ADDRESS` remains `BURN_ADDRESS`.

## 7. Real activation commit safety rules

The future activation commit must:

- change only the server and wallet `Seting.java` fork-height constants;
- use the same explicitly approved height in server and wallet;
- keep `V35_BURN_ADDRESS = BURN_ADDRESS`;
- not change rewards;
- not change PoW or difficulty behavior;
- not change jackpot policy;
- not change block or transaction format;
- not change replay-only compatibility;
- not create a burn transaction;
- not create a jackpot payout transaction;
- not include website deployment;
- not include artifacts, checksums, or tags;
- stop for review if any unrelated diff appears.

## 8. Future verification commands

Do not run these commands in Phase 6F-6.

### Refresh live height

```powershell
Invoke-RestMethod -Uri 'http://194.87.236.238:82/size' -Method Get -TimeoutSec 15
```

### Verify constants and exact diff

```powershell
Select-String -Path `
  C:\citu\unitedStates_storage-master\src\main\java\International_Trade_Union\setings\Seting.java, `
  C:\citu\unitedStates_final-master\src\main\java\International_Trade_Union\setings\Seting.java `
  -Pattern 'V35_RESCUE_FORK_INDEX|V35_BURN_ADDRESS'

git diff -- `
  unitedStates_storage-master/src/main/java/International_Trade_Union/setings/Seting.java `
  unitedStates_final-master/src/main/java/International_Trade_Union/setings/Seting.java
```

### Server verification

```powershell
cd C:\citu\unitedStates_storage-master
.\mvnw.cmd test
.\mvnw.cmd clean package
```

### Wallet verification

```powershell
cd C:\citu\unitedStates_final-master
.\mvnw.cmd test
.\mvnw.cmd clean package
```

### Final artifact checksums and tag after review only

```powershell
Get-FileHash '<FINAL_SERVER_ARTIFACT>' -Algorithm SHA256
Get-FileHash '<FINAL_WALLET_ARTIFACT>' -Algorithm SHA256
git tag v35-activation-fork-<APPROVED_INDEX>
```

## 9. Remaining blockers before real activation

- Refresh live `/size` again immediately before activation approval.
- Confirm the selected height still has at least `7344` remaining blocks.
- Explicitly approve one fork height.
- Apply the two-file activation diff in a separate approved phase.
- Review the exact post-edit diff.
- Run server and wallet tests and clean packages after the edit.
- Generate and verify final artifacts and SHA-256 checksums.
- Create the final tag only after review.
- Fill and publish RU / EN / ZH final notices.
- Assign monitoring owners and support contacts.
- Keep founder burn snapshot, address, amount, timing, and execution as
  separate approvals.

## 10. Confirmation no code changed

Confirmed. No source code was changed.

## 11. Confirmation no tests changed

Confirmed. No tests were changed or run.

## 12. Confirmation V35 remains disabled

Confirmed:

- server `V35_RESCUE_FORK_INDEX = Long.MAX_VALUE`
- wallet `V35_RESCUE_FORK_INDEX = Long.MAX_VALUE`
- server/wallet `V35_BURN_ADDRESS = BURN_ADDRESS`

## 13. Safety confirmation

No activation, founder burn transaction, fork action, jackpot payout,
deployment, tag, release artifact, checksum, build, or historical replay was
performed.

## 14. Recommended next phase

Recommended next phase:

**Phase 6F-7 - real activation go/no-go review with a fresh live `/size` read,
no activation.**

That phase should confirm whether `513858` still satisfies the `17-day`
minimum. If not, calculate a new candidate before any activation approval.
