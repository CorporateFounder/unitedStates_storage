# CITU V35 Phase 11A-1 Activation-Aware Server Test Fix Report

## 1. Executive Summary

Phase 11A-1 updated exactly two server tests that still assumed the pre-activation disabled sentinel `Long.MAX_VALUE`.

The production activation preparation remains unchanged:

- server `V35_RESCUE_FORK_INDEX = 514291`
- wallet `V35_RESCUE_FORK_INDEX = 514291`
- server/wallet `V35_BURN_ADDRESS = BURN_ADDRESS`

No production logic, consensus logic, mining logic, jackpot logic, or burn logic was changed.

## 2. Root Cause

Phase 11A configured the approved future fork height `514291`. Two server hardening tests still asserted:

```java
assertEquals(Long.MAX_VALUE, Seting.V35_RESCUE_FORK_INDEX);
```

That expectation was correct only while V35 used the disabled sentinel. It became obsolete once the real future fork height was prepared.

## 3. Tests Fixed

Updated exactly:

- `unitedStates_storage-master/src/test/java/International_Trade_Union/model/TournamentOfflineSmokeModeTest.java`
- `unitedStates_storage-master/src/test/java/International_Trade_Union/utils/UtilsBlockEmptyChainBootstrapTest.java`

The updated tests now:

- call `V35TestMode.assertConfiguredForkSafety()`
- verify the configured fork is safe in disabled or finite-height mode
- verify block height `0` remains pre-fork with `ProtocolVersion.isV35(0L) == false`
- keep the explicit `V35_BURN_ADDRESS == "BURN_ADDRESS"` placeholder assertion

The shared helper also verifies the unchanged V35 economics:

- 200 second block target
- 8 GOLD total reward
- 6 GOLD miner reward
- 2 GOLD jackpot reserve
- founder reward disabled and equal to 0 after V35

For finite activation mode it verifies:

- fork height is greater than `0`
- the block immediately before the fork is not V35
- the fork block is V35

## 4. Focused Test Result

Command:

```powershell
cd C:\citu\unitedStates_storage-master
.\mvnw.cmd "-Dtest=International_Trade_Union.model.TournamentOfflineSmokeModeTest,International_Trade_Union.utils.UtilsBlockEmptyChainBootstrapTest" test
```

Result:

- tests run: `6`
- failures: `0`
- errors: `0`
- skipped: `0`
- result: `PASS`

## 5. Full Server Verification

Commands:

```powershell
cd C:\citu\unitedStates_storage-master
.\mvnw.cmd test
.\mvnw.cmd clean package
```

Result:

- full server tests: `231/231 PASS`
- server clean package: `PASS`

## 6. Wallet Retest

Commands:

```powershell
cd C:\citu\unitedStates_final-master
.\mvnw.cmd test
.\mvnw.cmd clean package
```

Result:

- wallet tests: `200/200 PASS`
- wallet clean package: `PASS`

## 7. Changed Files

Expected working-tree changes after Phase 11A and Phase 11A-1:

- `unitedStates_storage-master/src/main/java/International_Trade_Union/setings/Seting.java`
- `unitedStates_final-master/src/main/java/International_Trade_Union/setings/Seting.java`
- `unitedStates_storage-master/src/test/java/International_Trade_Union/model/TournamentOfflineSmokeModeTest.java`
- `unitedStates_storage-master/src/test/java/International_Trade_Union/utils/UtilsBlockEmptyChainBootstrapTest.java`
- `CITU_V35_PHASE11A_REAL_ACTIVATION_COMMIT_PREPARATION_REPORT.md`
- `CITU_V35_PHASE11A1_ACTIVATION_AWARE_SERVER_TEST_FIX_REPORT.md`

Tracked Maven `target/` output created by verification was restored and is not part of the intended diff.

## 8. Constants Audit

| Project | `V35_RESCUE_FORK_INDEX` | `V35_BURN_ADDRESS` |
| --- | ---: | --- |
| Server | `514291` | `BURN_ADDRESS` |
| Wallet | `514291` | `BURN_ADDRESS` |

## 9. Safety Confirmation

- no production logic changed beyond the Phase 11A `Seting.java` fork-height preparation
- no consensus logic changed
- no mining logic changed
- no jackpot logic changed
- `V35_RESCUE_FORK_INDEX` remains `514291`
- `V35_BURN_ADDRESS` remains `BURN_ADDRESS`
- no burn transaction was created, signed, or broadcast
- no jackpot payout was created
- no deployment occurred
- no GitHub Release was created
- no file upload occurred
- no tag was created
- no final checksums were generated or published
- no commit was created

## 10. Readiness

The Phase 11A activation preparation now passes server and wallet test/package gates. It is ready for a separate explicit final diff review and commit approval step.

