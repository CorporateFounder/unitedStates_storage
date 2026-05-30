# CITU V35 Phase 6F-5A Activation-Mode Test Adaptation Report

## 1. Executive summary

Phase 6F-5A was completed only on the local dry-run branch:

`v35-local-activation-dryrun-6f5`

The existing temporary dry-run activation height remains:

`V35_RESCUE_FORK_INDEX = 513332`

The test suites were adapted to validate both supported configuration modes:

- pre-activation mode: `V35_RESCUE_FORK_INDEX = Long.MAX_VALUE`
- explicit activation dry-run mode: a finite positive fork height, currently `513332`

The adaptations are test-only. No production logic was changed beyond the existing Phase 6F-5 `Seting.java` fork-height diff.

Final result:

- server tests: `225` passed
- wallet tests: `200` passed
- server clean package: `BUILD SUCCESS`
- wallet clean package: `BUILD SUCCESS`

This remains a local dry-run branch. It is not a production activation or release.

## 2. Branch name

| Item | Result |
| --- | --- |
| Active branch | `v35-local-activation-dryrun-6f5` |
| Is `main` or `master` | No |
| Branch safety gate | PASS |

## 3. Git status before and after

Before Phase 6F-5A:

```text
 M unitedStates_final-master/src/main/java/International_Trade_Union/setings/Seting.java
 M unitedStates_storage-master/src/main/java/International_Trade_Union/setings/Seting.java
?? CITU_V35_PHASE6F5_LOCAL_ACTIVATION_DRYRUN_BRANCH_REPORT.md
```

After Phase 6F-5A:

- the two existing local dry-run `Seting.java` changes remain;
- mirrored server and wallet test adaptations are present;
- `CITU_V35_PHASE6F5_LOCAL_ACTIVATION_DRYRUN_BRANCH_REPORT.md` remains untracked;
- this report is added;
- generated tracked `target/` churn was restored after verification.

No commit was created.

## 4. Failure classification

Phase 6F-5 baseline:

| Project | Tests | Failures | Errors |
| --- | --- | --- | --- |
| Server | `225` | `46` | `0` |
| Wallet | `200` | `37` | `0` |

Classification:

| Group | Result |
| --- | --- |
| Direct disabled-sentinel expectations | Present: tests asserted `Long.MAX_VALUE` even in the explicit local activation branch |
| Legacy fixture index assumptions | Present: some tests used `1_000_000` or `Long.MAX_VALUE` as an always-legacy index |
| API/dashboard representation assumptions | Present: finite fork indexes are represented as numeric values by server APIs, while disabled mode uses `Long.MAX_VALUE` text |
| Unrelated production failures | None found |
| Package blockers | Test-phase failures only; resolved by the focused test adaptation |

The existing `\server\resources\error\error.txt` logging-path noise still appears during expected rejected-validation paths. It is secondary noise and did not fail the verified builds.

## 5. Files changed

Existing local dry-run production settings retained:

- `unitedStates_storage-master/src/main/java/International_Trade_Union/setings/Seting.java`
- `unitedStates_final-master/src/main/java/International_Trade_Union/setings/Seting.java`

New mirrored test helpers:

- `unitedStates_storage-master/src/test/java/International_Trade_Union/protocol/v35/V35TestMode.java`
- `unitedStates_final-master/src/test/java/International_Trade_Union/protocol/v35/V35TestMode.java`

Focused mirrored test adaptations were made in server and wallet suites covering:

- V35 controller/dashboard status
- blockchain V35 metadata
- cumulative-work comparison
- mining jackpot wiring and end-to-end checks
- simulated V35 fork checks
- burn-address policy
- jackpot validation, proof, builder, and context checks
- protocol-version checks
- PoW and mining-target gates
- reward validation fixtures
- supply calculator and adapter checks
- historical replay guard fixtures
- old-chain V35 isolation
- resolving/jackpot wiring

Additional server-only focused adaptations were made where server-only tests exist:

- resolve-admission checks
- supply and jackpot controller checks
- live controller/database hardening
- tournament selection
- transaction-pool burn hardening

Additional wallet-only focused adaptation:

- wallet V35 dashboard controller checks

## 6. Test adaptation strategy

The mirrored `V35TestMode` helper now enforces a strict mode-aware configuration audit:

- `V35_BURN_ADDRESS` must remain `BURN_ADDRESS`.
- target block time must remain `200` seconds.
- total reward must remain `8 GOLD`.
- miner reward must remain `6 GOLD`.
- jackpot reserve must remain `2 GOLD`.
- founder reward must remain disabled and `0 GOLD`.
- disabled mode must keep V35 inactive.
- finite mode must use a positive fork height.
- finite mode must be legacy immediately before the fork.
- finite mode must activate V35 exactly at the configured fork.

Legacy-behavior fixtures use a computed `preForkIndex()` rather than assuming `1_000_000` is always legacy.

No assertions were deleted merely to make builds pass. No test class was skipped.

## 7. Production code guard

Confirmed:

- no production logic changed in Phase 6F-5A;
- only the two existing Phase 6F-5 temporary `Seting.java` changes remain;
- no reward, PoW, difficulty, jackpot, burn, transaction-format, block-format, or replay-only compatibility production code changed.

## 8. Constants parity table

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

## 11. Remaining failures

None.

Remaining non-blocking operational noise:

- Maven reports pre-existing duplicate dependency declarations.
- Rejected validation paths may print the pre-existing missing `\server\resources\error\error.txt` logging-path noise.

## 12. Historical replay confirmation

No historical replay was run.

No live production blockchain data was used.

## 13. Deployment confirmation

No deployment was performed.

No website deployment was performed.

No production node was started.

## 14. Burn transaction confirmation

No founder burn transaction was created, signed, broadcast, or simulated.

The final burn address remains unapproved.

## 15. Jackpot confirmation

No jackpot payout transaction was created.

## 16. Release confirmation

No final tag was created.

No final release was created.

No final checksums were generated.

No release package was created or modified.

No commit was created.

## 17. Local dry-run warning

This branch and its locally built artifacts are dry-run material only.

They must not be deployed, published, tagged as final, copied into a production release package, or used by exchanges, node operators, or wallet users.

## 18. Recommended next phase

Recommended next phase:

**Phase 6F-5B - local activation dry-run review and revert plan, no real activation.**

That phase should review the exact local diff, confirm test-only adaptations remain symmetric, document how to restore the production-disabled sentinel, and decide which test adaptations should later be retained in a separately reviewed pre-activation branch.
