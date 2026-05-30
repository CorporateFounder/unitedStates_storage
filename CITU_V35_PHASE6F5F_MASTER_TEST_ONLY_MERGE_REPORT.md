# CITU V35 Phase 6F-5F Master Test-Only Merge Report

## 1. Executive summary

Phase 6F-5F applied the reviewed test-only preparation commit to `master` by
cherry-picking one commit only. V35 remains disabled. No activation settings,
production source files, release artifacts, or deployment files were applied.

## 2. Source commit

- Source branch: `v35-test-only-prep-6f5d`
- Reviewed source commit: `9fc4a2e6c519ea3090944e08ca80e434f3356dd6`
- Cherry-picked master commit: `b8572e4cf5c73778455e1f37262f59b688de124e`
- Target branch: `master`

The old local activation dry-run branch `v35-local-activation-dryrun-6f5` was
not merged.

## 3. Files applied

The applied commit contains `63` files:

- mirrored server and wallet `V35TestMode.java` test helpers
- mode-aware server and wallet tests under `src/test/java`
- Phase 6F-5 through Phase 6F-5E reports

Mechanical post-apply review result:

| Check | Result |
| --- | --- |
| Applied files | `63` |
| Allowed files | `63` |
| Forbidden files | `0` |
| Production source files | None |
| `Seting.java` files | None |
| `target/`, `release-dryrun/`, JAR, checksum, or deployment files | None |

## 4. Constants audit

| Project | `V35_RESCUE_FORK_INDEX` | `V35_BURN_ADDRESS` |
| --- | --- | --- |
| Server | `Long.MAX_VALUE` | `BURN_ADDRESS` |
| Wallet | `Long.MAX_VALUE` | `BURN_ADDRESS` |

Server and wallet constants match. No `513332` activation height remains in
either server or wallet `Seting.java`.

## 5. Seting.java confirmation

Server and wallet `Seting.java` stayed at `Long.MAX_VALUE`. Neither file was
part of the cherry-picked commit.

## 6. V35 burn address confirmation

Server and wallet `V35_BURN_ADDRESS` stayed at `BURN_ADDRESS`.

## 7. Server verification

| Command | Result |
| --- | --- |
| `.\mvnw.cmd test` | PASS: `225` tests, `0` failures, `0` errors, `0` skipped |
| `.\mvnw.cmd clean package` | PASS: `BUILD SUCCESS` |

## 8. Wallet verification

| Command | Result |
| --- | --- |
| `.\mvnw.cmd test` | PASS: `200` tests, `0` failures, `0` errors, `0` skipped |
| `.\mvnw.cmd clean package` | PASS: `BUILD SUCCESS` |

The pre-existing missing `\server\resources\error\error.txt` logger noise
appeared during expected rejected-validation paths. It did not fail tests or
packages.

Generated tracked `target/` churn was inspected and restored after build
verification.

## 9. Safety confirmation

No historical replay was run. No activation, founder burn transaction, jackpot
payout, deployment, tag, release artifact, or checksum was created.

## 10. Master status

`master` now contains the safe test-only activation-mode preparation. V35
remains disabled.

## 11. Recommended next phase

Proceed with a fresh fork-height planning review against live `/size` before
any separately approved activation commit. Keep activation, burn execution,
artifact generation, and release tagging as separate gated phases.
