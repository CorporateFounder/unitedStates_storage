# CITU V35 Phase 6F-5 Local Activation Dry-Run Branch Report

## 1. Executive summary

Phase 6F-5 was performed only on the local dry-run branch:

`v35-local-activation-dryrun-6f5`

The branch safety check passed because the active branch was not `main` or `master`.

The temporary local activation diff was applied in the server and wallet settings files:

`V35_RESCUE_FORK_INDEX: Long.MAX_VALUE -> 513332`

`V35_BURN_ADDRESS` remains the sentinel `BURN_ADDRESS`.

This is not a production activation. Nothing was deployed, committed, tagged, released, signed, broadcast, or copied into a release folder.

The dry-run exposed a release blocker: the existing server and wallet suites intentionally assert the pre-activation `Long.MAX_VALUE` sentinel. After applying the candidate activation constant, both full test runs and both clean-package runs fail at those disabled-state guard assertions. No test or production fix was applied in this phase.

## 2. Branch name

| Item | Result |
| --- | --- |
| Active branch | `v35-local-activation-dryrun-6f5` |
| Is `main` or `master` | No |
| Branch safety gate | PASS |

## 3. Git status before and after

Before the temporary settings diff:

```text
?? release-dryrun/
```

The untracked `release-dryrun/` folder existed before Phase 6F-5 and was not modified.

The requested Maven commands changed tracked files under the two `target/` folders. Those generated build outputs were restored after the build attempts so the dry-run branch remains narrowly scoped.

Expected status after Phase 6F-5:

```text
 M unitedStates_final-master/src/main/java/International_Trade_Union/setings/Seting.java
 M unitedStates_storage-master/src/main/java/International_Trade_Union/setings/Seting.java
?? CITU_V35_PHASE6F5_LOCAL_ACTIVATION_DRYRUN_BRANCH_REPORT.md
?? release-dryrun/
```

## 4. Exact files changed

Production settings changed temporarily on the local dry-run branch:

- `unitedStates_storage-master/src/main/java/International_Trade_Union/setings/Seting.java`
- `unitedStates_final-master/src/main/java/International_Trade_Union/setings/Seting.java`

Documentation added:

- `CITU_V35_PHASE6F5_LOCAL_ACTIVATION_DRYRUN_BRANCH_REPORT.md`

No test files were changed.

## 5. Exact activation diff applied

The same one-line temporary diff was applied to server and wallet:

```diff
-    long V35_RESCUE_FORK_INDEX = Long.MAX_VALUE;
+    long V35_RESCUE_FORK_INDEX = 513332;
```

No other production source diff was applied.

## 6. Constants parity table

| Constant or policy | Server | Wallet | Result |
| --- | --- | --- | --- |
| `V35_RESCUE_FORK_INDEX` | `513332` | `513332` | MATCH |
| `V35_BURN_ADDRESS` | `"BURN_ADDRESS"` | `"BURN_ADDRESS"` | MATCH |
| V35 target block time | `200` seconds | `200` seconds | MATCH |
| Expected blocks/day | `432` | `432` | MATCH |
| Total V35 reward | `8.00 GOLD` | `8.00 GOLD` | MATCH |
| Instant miner reward | `6.00 GOLD` | `6.00 GOLD` | MATCH |
| Jackpot reserve | `2.00 GOLD` | `2.00 GOLD` | MATCH |
| Founder reward enabled | `false` | `false` | MATCH |
| Expected founder reward | `0.00 GOLD` | `0.00 GOLD` | MATCH |

## 7. Tests and builds run

| Project | Command | Result |
| --- | --- | --- |
| Server | `.\mvnw.cmd test` | FAIL: `225` tests, `46` failures, `0` errors, `0` skipped |
| Wallet | `.\mvnw.cmd test` | FAIL: `200` tests, `37` failures, `0` errors, `0` skipped |
| Server | `.\mvnw.cmd clean package` | FAIL during the test phase |
| Wallet | `.\mvnw.cmd clean package` | FAIL during the test phase |

No package artifact was copied or treated as a release artifact.

## 8. Failures and fixes

The failures are activation-mode test compatibility failures. Existing guard tests intentionally require the production pre-activation sentinel:

```text
expected: <9223372036854775807> but was: <513332>
```

Affected areas include disabled-state assertions for protocol version, rewards, PoW, cumulative work, jackpot, burn-address behavior, supply APIs, simulated fork checks, legacy isolation, replay-only safety guards, and wallet dashboard behavior.

This dry-run did not weaken, skip, remove, or rewrite those safety tests. No production fix or test fix was made.

The existing `\server\resources\error\error.txt` logging-path noise was also observed during failing validation tests. It is secondary to the activation-mode assertion failures and was not changed.

## 9. Historical replay confirmation

No historical replay was run.

No live production blockchain data was used for this local dry-run.

## 10. Deployment confirmation

No deployment was performed.

No website deployment was performed.

No node was started against live production data.

## 11. Burn transaction confirmation

No founder burn transaction was created, signed, broadcast, or simulated.

`V35_BURN_ADDRESS` remains `BURN_ADDRESS`.

The final burn address remains unapproved.

## 12. Jackpot confirmation

No jackpot payout transaction was created.

## 13. Release confirmation

No final tag was created.

No final release was created.

No final checksums were generated.

No final release artifacts were created or copied.

No commit was created.

## 14. Local dry-run warning

This branch and its build attempts are local dry-run material only.

They must not be deployed, published, tagged as final, copied into a production release package, or used by exchanges, node operators, or wallet users.

## 15. Recommended next phase

Recommended next phase:

**Phase 6F-5A - activation-mode test adaptation plan and focused dry-run test patch, local branch only, no real activation.**

That phase should classify disabled-state guard tests into:

- pre-activation sentinel tests that must remain unchanged on the real pre-activation branch;
- activation-mode equivalents needed only for the local activation dry-run branch;
- invariant safety tests that must pass in both modes.

After that focused test adaptation is reviewed, rerun server and wallet tests and clean packages on the local dry-run branch. Do not proceed to a real activation commit until the activation-mode dry-run passes.
