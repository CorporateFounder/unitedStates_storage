# CITU V35 Phase 6F-5C: Disabled-Mode Test-Only Patch Report

## 1. Executive summary

Phase 6F-5C was completed only on the local dry-run branch:

`v35-local-activation-dryrun-6f5`

The temporary local activation height was restored in both projects:

`V35_RESCUE_FORK_INDEX: 513332 -> Long.MAX_VALUE`

`V35_BURN_ADDRESS` remains `BURN_ADDRESS`.

After restoration, no production source diff remains. The branch now contains
test-only V35 mode-awareness preparation and Phase 6F-5 documentation. Both
projects passed their disabled-mode tests and clean packages.

This branch is not an activation branch anymore. It must still be reviewed and
extracted carefully before any test-only changes are proposed for master.

## 2. Branch name

| Check | Result |
| --- | --- |
| Current branch | `v35-local-activation-dryrun-6f5` |
| Is branch `main` or `master`? | No |
| Branch safety gate | PASS |

## 3. Git status before and after

Before restoration, the branch contained:

- server `Seting.java` with `V35_RESCUE_FORK_INDEX = 513332`
- wallet `Seting.java` with `V35_RESCUE_FORK_INDEX = 513332`
- mirrored mode-aware server and wallet test adaptations
- mirrored test-only `V35TestMode.java` helpers
- Phase 6F-5, 6F-5A, and 6F-5B reports

After restoration and package verification:

- both `Seting.java` files match `HEAD`
- `git diff -- unitedStates_storage-master/src/main unitedStates_final-master/src/main`
  is empty
- generated tracked `target/` output produced by Maven was restored
- remaining tracked diffs are limited to test files
- remaining untracked code files are limited to the two test-only helpers
- remaining untracked documentation files are the Phase 6F-5 reports,
  including this report

## 4. Exact Seting.java revert performed

Server:

`unitedStates_storage-master/src/main/java/International_Trade_Union/setings/Seting.java`

Wallet:

`unitedStates_final-master/src/main/java/International_Trade_Union/setings/Seting.java`

The same one-line local restoration was applied to each file:

```diff
-    long V35_RESCUE_FORK_INDEX = 513332;
+    long V35_RESCUE_FORK_INDEX = Long.MAX_VALUE;
```

No burn-address or economics constant changed.

## 5. Current constants table

| Constant or policy | Server | Wallet | Result |
| --- | --- | --- | --- |
| `V35_RESCUE_FORK_INDEX` | `Long.MAX_VALUE` | `Long.MAX_VALUE` | MATCH, DISABLED |
| `V35_BURN_ADDRESS` | `"BURN_ADDRESS"` | `"BURN_ADDRESS"` | MATCH |
| V35 target block time | `200` seconds | `200` seconds | MATCH |
| Expected blocks/day | `432` | `432` | MATCH |
| Total V35 reward | `8.00 GOLD` | `8.00 GOLD` | MATCH |
| Instant miner reward | `6.00 GOLD` | `6.00 GOLD` | MATCH |
| Jackpot reserve | `2.00 GOLD` | `2.00 GOLD` | MATCH |
| Founder reward enabled | `false` | `false` | MATCH |
| Expected founder reward | `0.00 GOLD` | `0.00 GOLD` | MATCH |

V35 is disabled.

## 6. Remaining diff classification

### Production source

| Scope | Result |
| --- | --- |
| Server `src/main` diff | None |
| Wallet `src/main` diff | None |
| Production activation diff | None |
| Reward, PoW, jackpot, burn, format, or replay logic diff | None |

### Test-only preparation retained

New mirrored helpers:

- `unitedStates_storage-master/src/test/java/International_Trade_Union/protocol/v35/V35TestMode.java`
- `unitedStates_final-master/src/test/java/International_Trade_Union/protocol/v35/V35TestMode.java`

Retained mode-aware test adaptations cover:

- protocol version and configured-fork safety
- burn-address policy
- jackpot validation, accounting, proof, builder, and wiring
- reward fixtures
- mining target, PoW, and long-window difficulty
- supply calculator and adapter
- blockchain metadata and comparator behavior
- simulated V35 fork coverage
- historical replay guards and old-chain isolation
- resolving wiring and payout integration
- server-specific controller, database-hardening, tournament, and transaction-pool checks
- wallet dashboard checks

### Reports retained

- `CITU_V35_PHASE6F5_LOCAL_ACTIVATION_DRYRUN_BRANCH_REPORT.md`
- `CITU_V35_PHASE6F5A_ACTIVATION_MODE_TEST_ADAPTATION_REPORT.md`
- `CITU_V35_PHASE6F5B_LOCAL_DRYRUN_REVIEW_REVERT_PLAN.md`
- `CITU_V35_PHASE6F5C_DISABLED_MODE_TEST_ONLY_PATCH_REPORT.md`

## 7. Confirmation no production activation diff remains

Confirmed.

The post-restoration command:

```powershell
git diff -- unitedStates_storage-master/src/main unitedStates_final-master/src/main
```

returned no diff.

Both `Seting.java` files now use:

```java
long V35_RESCUE_FORK_INDEX = Long.MAX_VALUE;
String V35_BURN_ADDRESS = "BURN_ADDRESS";
```

## 8. Test-only files retained

The two `V35TestMode.java` helpers remain test-only. They enforce:

- burn sentinel remains `BURN_ADDRESS`
- target block time remains `200` seconds
- rewards remain `8 / 6 / 2 / 0`
- disabled mode remains inactive
- finite activation mode, when intentionally configured in a later isolated
  exercise, must use a positive height and activate exactly at that height

No test class was skipped. No safety assertion was removed merely to make the
suite pass.

## 9. Server test and package result

| Command | Result |
| --- | --- |
| `.\mvnw.cmd test` | PASS: `225` tests, `0` failures, `0` errors, `0` skipped |
| `.\mvnw.cmd clean package` | PASS: `BUILD SUCCESS` |

## 10. Wallet test and package result

| Command | Result |
| --- | --- |
| `.\mvnw.cmd test` | PASS: `200` tests, `0` failures, `0` errors, `0` skipped |
| `.\mvnw.cmd clean package` | PASS: `BUILD SUCCESS` |

## 11. Remaining blockers

- The retained test-only patch still requires a focused review before it is
  proposed for master.
- A clean branch from master should receive only reviewed tests, helpers, and
  reports.
- The real activation height remains unapproved and must be refreshed from
  live `/size` before any future activation decision.
- The founder burn address remains unapproved.
- Maven still reports pre-existing duplicate dependency declarations.
- Expected rejection paths still print the pre-existing missing
  `\server\resources\error\error.txt` logging-path noise.

## 12. Historical replay confirmation

No historical replay was run in Phase 6F-5C.

No live production blockchain data was used.

## 13. Deployment confirmation

No deployment was performed.

No website deployment was performed.

No production node was started.

## 14. Burn transaction confirmation

No founder burn transaction was created, signed, broadcast, or simulated.

## 15. Jackpot confirmation

No jackpot payout transaction was created.

## 16. Release confirmation

No final tag was created.

No final release was created.

No final checksums were generated.

No release artifacts were copied or published.

No commit was created.

## 17. Disabled-mode branch warning

This branch now contains test-only preparation and reports, not activation.

Do not treat locally generated jars as release artifacts. Do not deploy,
publish, tag, or copy them into any production release package.

## 18. Recommended next phase

Recommended next phase:

**Phase 6F-5D - clean master-derived test-only preparation branch plan and
review, no activation.**

That phase should create or document a clean master-derived branch, apply only
the reviewed helpers, mode-aware tests, and reports, confirm
`V35_RESCUE_FORK_INDEX = Long.MAX_VALUE`, and run the disabled-mode verification
again before any test-only commit is considered.
