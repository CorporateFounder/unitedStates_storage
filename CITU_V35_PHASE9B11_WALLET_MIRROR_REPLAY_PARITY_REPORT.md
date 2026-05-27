# CITU V35 Phase 9B-11 Wallet Mirror Replay Parity Report

## 1. Exact Server Files Compared

- `unitedStates_storage-master/src/main/java/International_Trade_Union/entity/blockchain/Blockchain.java`
- `unitedStates_storage-master/src/main/java/International_Trade_Union/utils/UtilsUse.java`
- `unitedStates_storage-master/src/main/java/International_Trade_Union/utils/UtilsBlock.java`
- `unitedStates_storage-master/src/main/java/International_Trade_Union/utils/UtilsBalance.java`
- `unitedStates_storage-master/src/test/java/International_Trade_Union/utils/HistoricalChainValidationRunnerTest.java`

Reports read:

- `CITU_V35_PHASE9B2_HISTORICAL_REPLAY_BALANCE_ORDERING_FIX_REPORT.md`
- `CITU_V35_PHASE9B4_HISTORICAL_REPLAY_STAKING_301409_FIX_REPORT.md`
- `CITU_V35_PHASE9B6_HISTORICAL_REPLAY_PRECISION_331090_FIX_REPORT.md`
- `CITU_V35_PHASE9B8_HISTORICAL_REPLAY_STOCK_335680_FIX_REPORT.md`
- `CITU_V35_PHASE9B10_HISTORICAL_REPLAY_UNSTAKING_341646_FIX_REPORT.md`

## 2. Exact Wallet Files Inspected

- `unitedStates_final-master/src/main/java/International_Trade_Union/entity/blockchain/Blockchain.java`
- `unitedStates_final-master/src/main/java/International_Trade_Union/utils/UtilsUse.java`
- `unitedStates_final-master/src/main/java/International_Trade_Union/utils/UtilsBlock.java`
- `unitedStates_final-master/src/main/java/International_Trade_Union/utils/UtilsBalance.java`
- `unitedStates_final-master/src/test/java/International_Trade_Union/utils/HistoricalChainValidationRunnerTest.java`
- `unitedStates_final-master/src/main/java/International_Trade_Union/setings/Seting.java`
- `unitedStates_storage-master/src/main/java/International_Trade_Union/setings/Seting.java`

## 3. Exact Wallet Files Changed

- `unitedStates_final-master/src/main/java/International_Trade_Union/entity/blockchain/Blockchain.java`
- `unitedStates_final-master/src/main/java/International_Trade_Union/utils/UtilsUse.java`
- `unitedStates_final-master/src/main/java/International_Trade_Union/utils/UtilsBlock.java`
- `unitedStates_final-master/src/test/java/International_Trade_Union/utils/HistoricalChainValidationRunnerTest.java`

## 4. Server Code Changed

No server code was changed in Phase 9B-11.

## 5. Server Replay Fixes Mirrored

Mirrored into wallet:

- 9B-2 ordering: `Blockchain.checkFromFile(...)` now validates each block against pre-block balances and applies the block only after validation succeeds.
- 9B-4 self-`STAKING`: wallet now has replay-only legacy self-`STAKING` compatibility through `UtilsUse.balanceTransactionForHistoricalReplay(...)`.
- 9B-6 precision dust: wallet now has the bounded `0.0000001` replay-only dust handling.
- 9B-8 cent drift: wallet now has the bounded `0.03` replay-only post-decimal cent-drift handling.
- 9B-10 self-`UNSTAKING`: wallet now has replay-only legacy self-`UNSTAKING` compatibility when combined dollar plus staking balance covers the declaration.

Also mirrored:

- `UtilsBlock.validationOneBlockWithCanonicalContext(...)` gained the same internal `historicalReplay` boolean overload.
- Existing live callers keep using the old overload, which defaults `historicalReplay` to `false`.

## 6. Wallet Live Validation Remains Strict

Wallet live validation remains strict:

- `UtilsUse.balanceTransaction(...)` was not loosened.
- Replay compatibility is only entered through `balanceTransactionForHistoricalReplay(...)` or replay adjustment calls from `Blockchain.checkFromFile(...)`.
- Focused wallet tests prove normal validation still rejects:
  - insufficient self-`STAKING`
  - meaningful insufficient `YES` transfers
  - insufficient stock under cent-drift conditions
  - insufficient self-`UNSTAKING`

## 7. Hardcoded Block Or Address Exceptions

No production hardcoded block exception was added.

No production hardcoded address exception was added.

Historical block numbers and addresses appear only in focused test fixtures.

## 8. Wallet Historical Replay Capability

Wallet already had `HistoricalChainValidationRunnerTest` with the optional external chain path property:

`-Dcitu.chain.path=C:\citu_chain_replay_full\resources\blockchain`

After mirroring server replay compatibility, the wallet runner validated the copied chain successfully and reached the end.

## 9. Test And Build Results

Passed:

- Wallet focused runner:
  - `.\mvnw.cmd "-Dtest=International_Trade_Union.utils.HistoricalChainValidationRunnerTest" test`
  - `Tests run: 11, Failures: 0, Errors: 0, Skipped: 0`
- Wallet external historical replay:
  - `.\mvnw.cmd "-Dtest=International_Trade_Union.utils.HistoricalChainValidationRunnerTest" "-Dcitu.chain.path=C:\citu_chain_replay_full\resources\blockchain" test`
  - Exit code `0`
  - Copied-chain replay reached the end successfully.
- Wallet full tests:
  - `.\mvnw.cmd test`
  - `Tests run: 199, Failures: 0, Errors: 0, Skipped: 0`
- Wallet package:
  - `.\mvnw.cmd clean package`
  - `BUILD SUCCESS`
  - `Tests run: 199, Failures: 0, Errors: 0, Skipped: 0`

Known non-fatal noise remains:

- Log4j warns that no appenders are configured for `International_Trade_Union.model.MyLogger`.
- Existing Maven duplicate dependency warnings remain.

## 10. V35 Disabled Confirmation

Confirmed in both wallet and server:

- `V35_RESCUE_FORK_INDEX = Long.MAX_VALUE`
- `V35_BURN_ADDRESS = BURN_ADDRESS`

No V35 activation was performed.

## 11. Burn/Fork/Jackpot Action Confirmation

No burn transaction was created.

No fork index was selected or changed.

No jackpot payout transaction was created.

No reward, PoW, jackpot, burn, transaction format, or block format behavior was changed.

## 12. Remaining Risks

- Replay compatibility now exists in both server and wallet; future changes should keep these paths replay-only and avoid leaking them into live block or transaction-pool validation.
- The wallet and server implementations are intentionally mirrored, but long-term maintenance would benefit from a shared helper or an explicit parity test if the projects remain split.
- Historical replay output is very noisy because `checkFromFile(...)` logs every block index; this is operational noise, not a validation failure.

## 13. Recommended Next Phase

Recommended next phase:

**Phase 9C - historical replay sign-off / release gate summary, no code changes**

Alternative:

**Phase 8C - release notes finalization and artifact checklist**
