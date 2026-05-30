# CITU V35 Phase 6F-5B: Local Dry-Run Review and Revert Plan

## 1. Executive summary

Phase 6F-5B reviewed the successful local activation dry-run on branch
`v35-local-activation-dryrun-6f5`. This is a review and planning document only.
No revert was executed in this phase.

The branch must not be merged or released as-is. It intentionally contains a
finite local dry-run activation height in both server and wallet:

`V35_RESCUE_FORK_INDEX = 513332`

The two `Seting.java` edits are activation-only changes. The mirrored
`V35TestMode.java` helpers and mode-aware test adaptations are test-only safety
improvements that may be extracted onto a separate clean branch while keeping
production constants disabled.

## 2. Branch name

| Check | Result |
| --- | --- |
| Current branch | `v35-local-activation-dryrun-6f5` |
| Is branch `main` or `master`? | No |
| Branch permitted for local activation dry-run review? | Yes |

## 3. Dry-run result summary

Phase 6F-5 and Phase 6F-5A established the following:

| Item | Result |
| --- | --- |
| Server dry-run fork height | `513332` |
| Wallet dry-run fork height | `513332` |
| Server/wallet fork-height parity | PASS |
| Server/wallet `V35_BURN_ADDRESS` | `BURN_ADDRESS` |
| Server tests | PASS: `225/225` |
| Server clean package | PASS: `BUILD SUCCESS` |
| Wallet tests | PASS: `200/200` |
| Wallet clean package | PASS: `BUILD SUCCESS` |
| Historical replay | Not run |
| Deployment | Not performed |
| Production blockchain data | Not used |
| Burn transaction | Not created, signed, broadcast, or simulated |
| Jackpot payout transaction | Not created |
| Git tag, checksum, or release package | Not created |
| Commit | Not created |

The first activation-mode run correctly exposed sentinel-only test assumptions.
Phase 6F-5A adapted those tests to validate both disabled mode and explicit
finite activation mode without changing production logic beyond the temporary
dry-run fork height.

## 4. Change classification table

### A. Dangerous activation-only changes

These changes must be reverted before any test-only preparation is proposed for
master:

| File | Current local dry-run change | Classification |
| --- | --- | --- |
| `unitedStates_storage-master/src/main/java/International_Trade_Union/setings/Seting.java` | `V35_RESCUE_FORK_INDEX`: `Long.MAX_VALUE` -> `513332` | Dangerous activation-only |
| `unitedStates_final-master/src/main/java/International_Trade_Union/setings/Seting.java` | `V35_RESCUE_FORK_INDEX`: `Long.MAX_VALUE` -> `513332` | Dangerous activation-only |

`V35_BURN_ADDRESS` remains `BURN_ADDRESS` in both projects. No reward, PoW,
jackpot, burn, block-format, transaction-format, or replay-compatibility
production logic changed.

### B. Safe test-only changes that may be useful later

The following mirrored helpers are new:

| File | Purpose |
| --- | --- |
| `unitedStates_storage-master/src/test/java/International_Trade_Union/protocol/v35/V35TestMode.java` | Validate disabled or explicit finite activation mode while preserving V35 safety assertions |
| `unitedStates_final-master/src/test/java/International_Trade_Union/protocol/v35/V35TestMode.java` | Wallet mirror of the same test-only helper |

The helpers assert:

- `V35_BURN_ADDRESS = BURN_ADDRESS`
- target block time `200` seconds
- total reward `8 GOLD`
- instant miner reward `6 GOLD`
- jackpot reserve `2 GOLD`
- founder reward disabled and expected founder reward `0`
- disabled mode remains inactive
- finite activation mode uses a positive height, remains inactive immediately
  before the height, and activates at the configured height

Mode-aware adaptations were made in mirrored server/wallet tests for protocol
versioning, burn policy, jackpot validation/accounting/wiring, supply,
difficulty, mining target, reward fixtures, chain metadata, old-chain
isolation, historical replay guards, resolving wiring, mining payout behavior,
controller/storage integration, comparator behavior, and simulated fork
coverage. Server-specific controller, network, database-hardening, and
tournament tests were adapted where applicable.

These test-only changes require a separate disabled-mode verification before
they are retained for master.

### C. Reports and documentation

| File | Status |
| --- | --- |
| `CITU_V35_PHASE6F5_LOCAL_ACTIVATION_DRYRUN_BRANCH_REPORT.md` | Created in Phase 6F-5 |
| `CITU_V35_PHASE6F5A_ACTIVATION_MODE_TEST_ADAPTATION_REPORT.md` | Created in Phase 6F-5A |
| `CITU_V35_PHASE6F5B_LOCAL_DRYRUN_REVIEW_REVERT_PLAN.md` | Created in Phase 6F-5B |

## 5. Revert plan

Do not run these commands as part of Phase 6F-5B. For the future local
disabled-mode restoration, revert only the two activation-only settings edits:

```powershell
cd C:\citu

git restore --source=HEAD -- `
  unitedStates_storage-master/src/main/java/International_Trade_Union/setings/Seting.java `
  unitedStates_final-master/src/main/java/International_Trade_Union/setings/Seting.java
```

Then confirm both projects are back in pre-activation disabled mode:

```powershell
Select-String -Path `
  C:\citu\unitedStates_storage-master\src\main\java\International_Trade_Union\setings\Seting.java, `
  C:\citu\unitedStates_final-master\src\main\java\International_Trade_Union\setings\Seting.java `
  -Pattern 'V35_RESCUE_FORK_INDEX|V35_BURN_ADDRESS'

git diff -- `
  unitedStates_storage-master/src/main/java/International_Trade_Union/setings/Seting.java `
  unitedStates_final-master/src/main/java/International_Trade_Union/setings/Seting.java

git status --short
```

Expected disabled values:

```java
long V35_RESCUE_FORK_INDEX = Long.MAX_VALUE;
String V35_BURN_ADDRESS = BURN_ADDRESS;
```

Keep the test-only helpers and mode-aware adaptations if they are being
evaluated for later extraction. Verify disabled mode again:

```powershell
cd C:\citu\unitedStates_storage-master
.\mvnw.cmd test
.\mvnw.cmd clean package

cd C:\citu\unitedStates_final-master
.\mvnw.cmd test
.\mvnw.cmd clean package
```

If tracked `target/` files change during verification, inspect them first and
restore only confirmed generated output before preparing any test-only patch.

## 6. Safe merge path to master

Do not merge `v35-local-activation-dryrun-6f5` as-is.

Use this future sequence:

1. Restore the two `Seting.java` files to `Long.MAX_VALUE` on the local dry-run
   branch and verify disabled mode.
2. Review the remaining diff so it contains only test-only helpers,
   mode-aware test adaptations, and approved reports.
3. Create a clean branch from master, preferably using a separate clean
   worktree.
4. Apply only the reviewed test-only changes and reports to that clean branch.
5. Confirm server and wallet still use:
   `V35_RESCUE_FORK_INDEX = Long.MAX_VALUE`.
6. Confirm server and wallet still use:
   `V35_BURN_ADDRESS = BURN_ADDRESS`.
7. Run server and wallet tests and clean packages in disabled mode.
8. Commit the test-only preparation separately.
9. Refresh live `/size` before any future real activation decision.
10. Make a later activation-height change only in a separately approved
    activation commit.

The local candidate `513332` was suitable for dry-run validation. It is not a
final approved production height and must be refreshed against live height
before any real activation.

## 7. Production safety warning

- Do not deploy or release jars built from this branch.
- Do not merge this branch as-is.
- This branch intentionally contains a finite fork height.
- Do not copy local dry-run jars into a release package.
- Do not create final checksums or tags from this branch.
- Keep founder burn execution separate from activation.
- Keep jackpot payout execution separate from activation.
- Refresh the candidate fork height from live `/size` before any real
  activation commit.

## 8. Phase 6F-5B change confirmation

Phase 6F-5B changed no source code and no tests. It created only this report.
No revert was executed.

## 9. Safety confirmations

| Item | Confirmation |
| --- | --- |
| Deployment | Not performed |
| Historical replay | Not run |
| Live production blockchain data | Not used |
| Founder burn transaction | Not created, signed, broadcast, or simulated |
| Jackpot payout transaction | Not created |
| Final tag | Not created |
| Final release | Not created |
| Final checksums | Not created |
| Commit | Not created |

## 10. Recommended next phase

Recommended next phase:

**Phase 6F-5C - local disabled-mode restoration and test-only extraction
verification.**

That phase should restore only the two `Seting.java` activation-height edits,
keep the test-only improvements under review, run disabled-mode server/wallet
tests and packages, and prepare a clean test-only patch without activation.
