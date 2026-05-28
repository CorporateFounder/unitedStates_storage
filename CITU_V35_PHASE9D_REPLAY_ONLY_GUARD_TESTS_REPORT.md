# CITU V35 Phase 9D Replay-Only Guard Tests Report

## 1. Exact files inspected

Reports read:
- `CITU_V35_PHASE9C_HISTORICAL_REPLAY_SIGNOFF_RELEASE_GATE_REPORT.md`
- `CITU_V35_PHASE9B11_WALLET_MIRROR_REPLAY_PARITY_REPORT.md`
- `CITU_V35_PHASE9B10_HISTORICAL_REPLAY_UNSTAKING_341646_FIX_REPORT.md`
- `CITU_V35_PHASE9B8_HISTORICAL_REPLAY_STOCK_335680_FIX_REPORT.md`
- `CITU_V35_PHASE9B6_HISTORICAL_REPLAY_PRECISION_331090_FIX_REPORT.md`
- `CITU_V35_PHASE9B4_HISTORICAL_REPLAY_STAKING_301409_FIX_REPORT.md`

Code/test files inspected:
- `unitedStates_storage-master/src/test/java/International_Trade_Union/utils/HistoricalChainValidationRunnerTest.java`
- `unitedStates_final-master/src/test/java/International_Trade_Union/utils/HistoricalChainValidationRunnerTest.java`
- `unitedStates_storage-master/src/main/java/International_Trade_Union/setings/Seting.java`
- `unitedStates_final-master/src/main/java/International_Trade_Union/setings/Seting.java`

## 2. Exact files changed

- `unitedStates_storage-master/src/test/java/International_Trade_Union/utils/HistoricalChainValidationRunnerTest.java`
- `unitedStates_final-master/src/test/java/International_Trade_Union/utils/HistoricalChainValidationRunnerTest.java`
- `CITU_V35_PHASE9D_REPLAY_ONLY_GUARD_TESTS_REPORT.md`

## 3. Whether production code changed

No production code was changed.

Only test files and this report were changed.

## 4. Whether server tests were added

Yes. Added server test:

- `replayCompatibilityCannotWeakenNormalLiveBalanceValidation`

The test proves normal live validation still rejects:
- insufficient digital dollar balance
- insufficient digital stock balance
- replay-only precision dust shortage
- replay-only cent drift shortage
- replay-only self-STAKING compatibility when spendable balance is insufficient
- replay-only self-UNSTAKING compatibility when staking balance is insufficient

It also proves normal live validation still accepts:
- self-STAKING when spendable balance is sufficient
- self-UNSTAKING when staking balance is sufficient

## 5. Whether wallet tests were added

Yes. Added matching wallet test:

- `replayCompatibilityCannotWeakenNormalLiveBalanceValidation`

The wallet guard mirrors the server guard so wallet/server replay parity remains aligned.

## 6. Which replay-only compatibility gates were protected

The new guards cover these replay-only compatibility areas:
- Phase 9B-4 legacy self-STAKING compatibility
- Phase 9B-6 tiny precision dust handling
- Phase 9B-8 bounded cent drift handling
- Phase 9B-10 legacy self-UNSTAKING compatibility

Phase 9B-2 validate/apply ordering remains covered by the existing historical replay tests and was not changed in this phase.

## 7. Confirmation live validation remains strict

Confirmed by focused server and wallet tests.

The guards call normal `UtilsUse.balanceTransaction(...)`, not the historical replay compatibility entrypoint. Normal live validation still rejects ordinary insufficient-balance, insufficient-stock, insufficient-STAKING, and insufficient-UNSTAKING cases.

No hardcoded historical block exception was added.
No hardcoded historical address exception was added.
No broad insufficient-balance bypass was added.

## 8. Confirmation V35 remains disabled

Confirmed in both server and wallet tests:

- `V35_RESCUE_FORK_INDEX = Long.MAX_VALUE`
- `V35_BURN_ADDRESS = BURN_ADDRESS`
- `ProtocolVersion.isV35(341646L) == false`
- `BurnAddressPolicy.isNoSpendActive(341646L) == false`

No V35 activation was performed.

## 9. Confirmation no burn/fork/jackpot action was performed

Confirmed:
- no founder burn transaction was created
- no fork index was selected or activated
- no jackpot payout transaction was created
- no reward, PoW, jackpot, burn, transaction, or block format behavior was changed

## 10. Server test results

Targeted server historical runner test:

Command:

```powershell
cd C:\citu\unitedStates_storage-master
.\mvnw.cmd "-Dtest=International_Trade_Union.utils.HistoricalChainValidationRunnerTest" test
```

Result:
- `Tests run: 14, Failures: 0, Errors: 0, Skipped: 0`
- `BUILD SUCCESS`

Full server test suite:

Command:

```powershell
cd C:\citu\unitedStates_storage-master
.\mvnw.cmd test
```

Result:
- `Tests run: 225, Failures: 0, Errors: 0, Skipped: 0`
- `BUILD SUCCESS`

Known non-fatal logging noise remains: `\server\resources\error\error.txt` may be missing during some tests, producing log stack traces without failing the build.

## 11. Wallet test results

Targeted wallet historical runner test:

Command:

```powershell
cd C:\citu\unitedStates_final-master
.\mvnw.cmd "-Dtest=International_Trade_Union.utils.HistoricalChainValidationRunnerTest" test
```

Result:
- `Tests run: 12, Failures: 0, Errors: 0, Skipped: 0`
- `BUILD SUCCESS`

Full wallet test suite:

Command:

```powershell
cd C:\citu\unitedStates_final-master
.\mvnw.cmd test
```

Result:
- `Tests run: 200, Failures: 0, Errors: 0, Skipped: 0`
- `BUILD SUCCESS`

Known non-fatal log4j warning remains: no appenders configured for some wallet test logging.

## 12. Clean package results if run

Clean package was not run in Phase 9D because production code was not changed.

## 13. Remaining risks

- The replay-only compatibility boundary is now guarded at focused unit-test level, but future edits to `UtilsUse.balanceTransaction(...)` or replay entrypoints should keep these guards in place.
- Existing non-fatal logging path noise around `\server\resources\error\error.txt` still exists and may clutter future replay/test output.
- Final release gates are still separate: V35 activation, founder burn, artifact/tag/checksum release, exchange notice, and post-activation monitoring are not complete.

## 14. Recommended next phase

Recommended next phase:

- Phase 8C — release notes finalization and artifact checklist, no code changes

Alternative if the release process should stay strictly operational first:

- Phase 6F-2 — final activation commit checklist, no code changes
