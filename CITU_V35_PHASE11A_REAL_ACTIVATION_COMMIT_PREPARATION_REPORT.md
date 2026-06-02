# CITU V35 Phase 11A Real Activation Commit Preparation Report

## Executive summary

Phase 11A performed the mandatory live-height refresh and applied the approved real activation preparation diff to the two mirrored `Seting.java` files only.

Candidate fork height `514291` had a sufficient live buffer at the time of the check. Wallet verification passed. Server verification is blocked by two existing tests that still require the pre-activation disabled sentinel `Long.MAX_VALUE`. No automatic commit was created because the server test gate did not pass.

This is an uncommitted activation preparation state. No deployment, public release, burn, or jackpot payout occurred.

## Fresh live height check

| Field | Value |
| --- | --- |
| Read timestamp | `2026-06-02 18:47:10 +05:00` |
| Source | `http://194.87.236.238:82/size` |
| Live height | `506857` |
| Approved candidate | `514291` |
| Remaining blocks | `7434` |
| Remaining days at 432 blocks/day | `17.2083` |
| Required minimum | `6048` blocks / `14` days |
| Buffer decision | `PROCEED` |

## Files changed

Exactly two production files were intentionally changed:

- `unitedStates_storage-master/src/main/java/International_Trade_Union/setings/Seting.java`
- `unitedStates_final-master/src/main/java/International_Trade_Union/setings/Seting.java`

Generated tracked files inside `target/` were restored after verification runs and are not part of the activation diff.

## Exact constants before and after

| Project | Constant | Before | After |
| --- | --- | --- | --- |
| Server | `V35_RESCUE_FORK_INDEX` | `Long.MAX_VALUE` | `514291` |
| Wallet | `V35_RESCUE_FORK_INDEX` | `Long.MAX_VALUE` | `514291` |
| Server | `V35_BURN_ADDRESS` | `BURN_ADDRESS` | `BURN_ADDRESS` |
| Wallet | `V35_BURN_ADDRESS` | `BURN_ADDRESS` | `BURN_ADDRESS` |

## Diff audit

Before this report was created, `git diff --name-only` contained only the two approved `Seting.java` files.

The reviewed diff contains one line changed in each file:

```text
V35_RESCUE_FORK_INDEX = Long.MAX_VALUE
```

to:

```text
V35_RESCUE_FORK_INDEX = 514291
```

No burn address, reward, PoW, block format, transaction format, or replay compatibility logic changed.

## Server verification result

Command attempted:

```powershell
cd C:\citu\unitedStates_storage-master
.\mvnw.cmd test
```

Result:

- tests run: `231`;
- failures: `2`;
- errors: `0`;
- server package: `NOT RUN` because the test gate failed.

Blocking tests:

1. `International_Trade_Union.model.TournamentOfflineSmokeModeTest.v35RemainsDisabledDuringOfflineSmokeHardening`
2. `International_Trade_Union.utils.UtilsBlockEmptyChainBootstrapTest.v35RemainsDisabledDuringBootstrapHardening`

Both failures are disabled-sentinel expectation failures:

```text
expected: <9223372036854775807> but was: <514291>
```

The two tests still assert `Seting.V35_RESCUE_FORK_INDEX == Long.MAX_VALUE`. They must be reviewed in a separate explicitly approved test-adaptation phase. They were not modified in Phase 11A.

## Wallet verification result

Commands run:

```powershell
cd C:\citu\unitedStates_final-master
.\mvnw.cmd test
.\mvnw.cmd clean package
```

Result:

- wallet tests: `PASS`, `200 / 200`;
- wallet clean package: `PASS`;
- generated tracked `target/` changes were restored and are not part of this diff.

## Activation commit readiness

Status: **NOT READY TO COMMIT**

Reason: server tests do not pass in finite activation-height mode because two server hardening tests still require the disabled sentinel.

Required next step: run a focused, separately approved server test-only adaptation phase. Preserve the safety intent while making these tests mode-aware, then rerun server tests and server clean package. Do not commit the activation diff until the server gate passes.

## Safety confirmation

- Server and wallet `V35_RESCUE_FORK_INDEX` are now prepared as `514291` in the uncommitted working tree.
- Server and wallet `V35_BURN_ADDRESS` remain `BURN_ADDRESS`.
- No burn transaction was created, signed, broadcast, or simulated.
- No jackpot payout was created.
- No deployment occurred.
- No GitHub Release was created.
- No file was uploaded.
- No tag was created.
- No release artifact was uploaded.
- No checksum was generated or published.
- No commit was created.

## Recommended next phase

Run a focused Phase 11A-1 server activation-mode test adaptation review for the two blocked tests only. After that, rerun server tests and server clean package, restore generated `target/` changes, and reassess whether the activation preparation diff is ready to commit.
