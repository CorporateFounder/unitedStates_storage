# CITU V35 Phase 6F-5D: Clean Test-Only Preparation Branch Report

## 1. Executive summary

Phase 6F-5D created a clean master-derived branch:

`v35-test-only-prep-6f5d`

The branch was created directly from current `master` at:

`2a188f76f38d4626f62ccbb1e1b4fc00fdd77e01`

Only the reviewed test-only V35 mode-awareness patch and relevant Phase 6F-5
reports were applied. No activation-height diff, production-source diff,
build output, release artifact, checksum, deployment file, or tag was included.

V35 remains disabled in server and wallet:

`V35_RESCUE_FORK_INDEX = Long.MAX_VALUE`

`V35_BURN_ADDRESS = "BURN_ADDRESS"`

Server and wallet tests and clean packages passed.

## 2. Source dry-run branch

Source branch:

`v35-local-activation-dryrun-6f5`

Before extraction, the source branch was audited and confirmed to contain no
remaining production activation diff. Its uncommitted safe patch was preserved
in a named stash:

`phase-6f5d-safe-test-only-extraction`

The stash remains as a backup because dropping it was not requested.

## 3. New clean branch name

New branch:

`v35-test-only-prep-6f5d`

Base verification:

| Check | Result |
| --- | --- |
| New branch `HEAD` | `2a188f76f38d4626f62ccbb1e1b4fc00fdd77e01` |
| Current `master` | `2a188f76f38d4626f62ccbb1e1b4fc00fdd77e01` |
| Master-derived directly | YES |
| Based on dry-run branch | NO |

No commit was created.

## 4. Files included in safe test-only patch

### New mirrored helpers

- `unitedStates_storage-master/src/test/java/International_Trade_Union/protocol/v35/V35TestMode.java`
- `unitedStates_final-master/src/test/java/International_Trade_Union/protocol/v35/V35TestMode.java`

### Mode-aware test adaptations

The retained server and wallet test updates cover:

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
- server-specific controller, database-hardening, tournament, and
  transaction-pool checks
- wallet dashboard checks

### Reports

- `CITU_V35_PHASE6F5_LOCAL_ACTIVATION_DRYRUN_BRANCH_REPORT.md`
- `CITU_V35_PHASE6F5A_ACTIVATION_MODE_TEST_ADAPTATION_REPORT.md`
- `CITU_V35_PHASE6F5B_LOCAL_DRYRUN_REVIEW_REVERT_PLAN.md`
- `CITU_V35_PHASE6F5C_DISABLED_MODE_TEST_ONLY_PATCH_REPORT.md`
- `CITU_V35_PHASE6F5D_CLEAN_TEST_ONLY_PREPARATION_BRANCH_REPORT.md`

## 5. Files explicitly excluded

The extraction excludes:

- any `Seting.java` activation-height change
- any production source-code change
- any reward, PoW, jackpot, burn, block-format, transaction-format, or
  replay-compatibility change
- tracked Maven `target/` build output
- jar files
- `release-dryrun/`
- checksum files
- deployment files
- private keys, wallet databases, blockchain resources, and node state

Generated tracked `target/` output produced during verification was restored
after the clean-package runs.

## 6. Constants audit

| Constant | Server | Wallet | Result |
| --- | --- | --- | --- |
| `V35_RESCUE_FORK_INDEX` | `Long.MAX_VALUE` | `Long.MAX_VALUE` | MATCH, DISABLED |
| `V35_BURN_ADDRESS` | `"BURN_ADDRESS"` | `"BURN_ADDRESS"` | MATCH |

The post-extraction audit command:

```powershell
git diff -- unitedStates_storage-master/src/main unitedStates_final-master/src/main
```

returned no diff.

## 7. Confirmation V35_RESCUE_FORK_INDEX remains Long.MAX_VALUE

Confirmed in server and wallet:

```java
long V35_RESCUE_FORK_INDEX = Long.MAX_VALUE;
```

No finite activation height remains in either `Seting.java`.

## 8. Confirmation V35_BURN_ADDRESS remains BURN_ADDRESS

Confirmed in server and wallet:

```java
String V35_BURN_ADDRESS = "BURN_ADDRESS";
```

No burn address was approved or changed.

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

## 11. Safety confirmations

| Action | Result |
| --- | --- |
| V35 activation | Not performed |
| Founder burn transaction | Not created, signed, broadcast, or simulated |
| Jackpot payout transaction | Not created |
| Historical replay | Not run |
| Live production blockchain data | Not used |
| Deployment | Not performed |
| Website deployment | Not performed |
| Final tag | Not created |
| Final release | Not created |
| Final checksums | Not created |
| Final release artifacts | Not created or copied |
| Commit | Not created |

## 12. Is this branch safe to commit as test-only preparation?

Yes, subject to a final human diff review.

The branch is master-derived, production `src/main` is unchanged, V35 remains
disabled, Maven residue was removed, and the retained patch is limited to
tests, test helpers, and reports.

Before committing, review the exact staged file list and confirm it contains no
`target/`, jar, checksum, deployment, release-dryrun, or production-source
paths.

## 13. Remaining notes

- Maven still reports pre-existing duplicate dependency declarations.
- Expected rejection paths still print the pre-existing missing
  `\server\resources\error\error.txt` logging-path noise.
- The preserved stash is a backup copy of the extracted patch and may be
  removed only in a later explicitly approved cleanup step.

## 14. Recommended next phase

Recommended next phase:

**Phase 6F-5E - final test-only diff review and optional commit approval.**

That phase should review the exact file list, stage only tests, helpers, and
reports, verify constants remain disabled, and commit only if explicitly
approved.
