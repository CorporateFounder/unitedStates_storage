# CITU V35 Phase 9B-4 Historical Replay STAKING 301409 Fix Report

## 1. Exact Files Inspected

- `CITU_V35_PHASE9B2_HISTORICAL_REPLAY_BALANCE_ORDERING_FIX_REPORT.md`
- `CITU_V35_PHASE9B3_HISTORICAL_REPLAY_FAILURE_301409_ANALYSIS.md`
- `CITU_V35_PHASE9A_LIVE_HISTORICAL_CHAIN_REPLAY_GUIDE.md`
- `unitedStates_storage-master/src/main/java/International_Trade_Union/entity/blockchain/Blockchain.java`
- `unitedStates_storage-master/src/main/java/International_Trade_Union/utils/UtilsBlock.java`
- `unitedStates_storage-master/src/main/java/International_Trade_Union/utils/UtilsBalance.java`
- `unitedStates_storage-master/src/main/java/International_Trade_Union/utils/UtilsUse.java`
- `unitedStates_storage-master/src/main/java/International_Trade_Union/utils/UtilsResolving.java`
- `unitedStates_storage-master/src/test/java/International_Trade_Union/utils/HistoricalChainValidationRunnerTest.java`
- `unitedStates_final-master/src/main/java/International_Trade_Union/entity/blockchain/Blockchain.java`
- `unitedStates_storage-master/src/main/java/International_Trade_Union/setings/Seting.java`
- `unitedStates_final-master/src/main/java/International_Trade_Union/setings/Seting.java`
- External copied chain segment:
  `C:\citu_chain_replay_full\resources\blockchain\157.txt`

## 2. Exact Files Changed

- `unitedStates_storage-master/src/main/java/International_Trade_Union/entity/blockchain/Blockchain.java`
- `unitedStates_storage-master/src/main/java/International_Trade_Union/utils/UtilsBlock.java`
- `unitedStates_storage-master/src/main/java/International_Trade_Union/utils/UtilsUse.java`
- `unitedStates_storage-master/src/test/java/International_Trade_Union/utils/HistoricalChainValidationRunnerTest.java`
- `CITU_V35_PHASE9B4_HISTORICAL_REPLAY_STAKING_301409_FIX_REPORT.md`

No wallet, website, `pom.xml`, transaction format, or block format files were changed.

## 3. Exact Root Cause

Block `301409` contains a legacy non-BASIS self-STAKING transaction:

- segment file: `C:\citu_chain_replay_full\resources\blockchain\157.txt`
- approximate line: `810`
- sender/customer: `28eNM7pFKzTgYb8WyUHgttJK9v9oV3TH4jYMxB1xXAK4o`
- `digitalDollar`: `1,400,000.0`
- `digitalStockBalance`: `0.0`
- `bonusForMiner`: `0.0`
- `voteEnum`: `STAKING`

During historical replay, normal legacy `balanceTransaction(...)` treated the STAKING declaration as if the sender needed the full `1,400,000.0` in spendable digital-dollar balance. The replay balance before the block had much lower spendable dollars but enough declared stake coverage when digital-dollar and current staking balances were considered together.

This is a legacy replay-context mismatch for self-STAKING declarations, not a V35 issue.

## 4. Replay-Only or Consensus/Live-Path Issue

This fix is replay-only.

The existing live validation path still calls the normal `UtilsUse.balanceTransaction(...)` behavior. The historical compatibility mode is only selected by `Blockchain.checkFromFile(...)` when validating copied historical chain data.

The fix does not change transaction-pool behavior, live block admission behavior, mining, reward validation, V35 validation, or balance accounting.

## 5. Exact Fix

Implemented a narrow historical replay helper:

- `UtilsUse.balanceTransactionForHistoricalReplay(...)`
  - first tries the existing whole-block `balanceTransaction(...)`;
  - if normal validation accepts the full block, it returns that normal result;
  - only if normal validation rejects, it permits a narrow legacy self-STAKING declaration case.

The narrow legacy self-STAKING compatibility rule requires all of the following:

- `voteEnum == STAKING`
- sender equals recipient/customer
- block index is after legacy balance checking starts
- block index is before `V35_RESCUE_FORK_INDEX`
- amount, stock, and bonus are nonnegative
- `digitalStockBalance == 0`
- spendable digital dollars are lower than the declared staking amount
- spendable digital dollars plus existing staking balance cover the declared staking amount

`UtilsBlock.validationOneBlockWithCanonicalContext(...)` now has an overload with a `historicalReplay` boolean. Existing callers use the old overload and therefore keep normal behavior. `Blockchain.checkFromFile(...)` passes `historicalReplay = true` for historical replay validation.

## 6. No Hardcoded Block or Address Exception

No hardcoded production exception was added for:

- block `301409`
- address `28eNM7pFKzTgYb8WyUHgttJK9v9oV3TH4jYMxB1xXAK4o`
- block `301393`
- address `qDWgdtEBzbSGs7AdUVLYEiBP5Bs7nb3m3L8GaBGgbSEP`

The actual historical addresses appear only in regression tests as fixtures.

The fix does not skip validation globally, does not accept ordinary insufficient transfers, and does not weaken normal live insufficient-balance checks.

## 7. Whether Block 301409 Now Passes

Yes.

The external replay against:

`C:\citu_chain_replay_full\resources\blockchain`

progressed past block `301409`.

## 8. Whether Full Replay Reached the End or Failed Later

Full replay did not reach the end.

After passing block `301409`, replay later failed at block `331090`.

New later failure:

- block/index: `331090`
- segment file: `C:\citu_chain_replay_full\resources\blockchain\175.txt`
- line: `1934`
- failing visible non-BASIS transaction:
  - sender: `hK6XwB7LxM5wkqbHV3WTpjhZG1TstggsxqEWMw8FnbGf`
  - recipient/customer: `yfudXXYdHx1W9QBZuDubr5ZjiqmdTMuKYkMdJyZYmevc`
  - `digitalDollar`: `805179.0`
  - `digitalStockBalance`: `764179.0`
  - `bonusForMiner`: `0.0`
  - `voteEnum`: `YES`

This later failure was not fixed in Phase 9B-4. It should be handled in a separate analysis/fix phase if needed.

## 9. New Failing Index

`331090`

## 10. Wallet Mirror Code

Wallet `Blockchain.java` still needs attention later.

The wallet mirror review found that wallet `checkFromFile(...)` still has the older replay-ordering behavior from before the server-side 9B-2 fix. This phase did not modify wallet code because the requested fix targeted the server historical replay failure at block `301409`.

Recommended later action: mirror the server historical replay ordering/fallback design into wallet only after a dedicated wallet replay phase or explicit parity phase.

## 11. V35 Disabled Confirmation

Confirmed:

- server `V35_RESCUE_FORK_INDEX = Long.MAX_VALUE`
- wallet `V35_RESCUE_FORK_INDEX = Long.MAX_VALUE`
- server `V35_BURN_ADDRESS = "BURN_ADDRESS"`
- wallet `V35_BURN_ADDRESS = "BURN_ADDRESS"`

V35 was not activated.

## 12. Burn/Fork/Jackpot Action Confirmation

No burn transaction was created.

No fork index was selected or changed.

No jackpot payout transaction was created manually.

No V35 reward, PoW, jackpot, burn, transaction format, or block format behavior was changed.

## 13. Test and Build Results

Focused server historical runner tests:

```text
cd C:\citu\unitedStates_storage-master
.\mvnw.cmd "-Dtest=International_Trade_Union.utils.HistoricalChainValidationRunnerTest" test
Result: PASS
Tests run: 6, Failures: 0, Errors: 0, Skipped: 0
```

External historical replay:

```text
cd C:\citu\unitedStates_storage-master
.\mvnw.cmd "-Dtest=International_Trade_Union.utils.HistoricalChainValidationRunnerTest" "-Dcitu.chain.path=C:\citu_chain_replay_full\resources\blockchain" test
Result: FAIL later at block 331090
Block 301409: passed
```

Server full tests:

```text
cd C:\citu\unitedStates_storage-master
.\mvnw.cmd test
Result: PASS
Tests run: 217, Failures: 0, Errors: 0, Skipped: 0
```

Server clean package:

```text
cd C:\citu\unitedStates_storage-master
.\mvnw.cmd clean package
Result: PASS
Tests run: 217, Failures: 0, Errors: 0, Skipped: 0
```

Known non-fatal log noise remains:

- `java.io.FileNotFoundException: \server\resources\error\error.txt`

This appears during logging and did not cause the focused tests, full server test suite, or clean package to fail.

## 14. Remaining Risks

- Historical replay still does not complete; it now stops later at block `331090`.
- The later `331090` failure appears to be a different legacy balance/accounting case involving a large non-self transfer.
- Wallet historical replay code has not yet received the server replay-ordering mirror fix.
- The missing `\server\resources\error\error.txt` logging path remains noisy and may obscure future diagnostics, although it is not the primary validation failure.
- Full live-chain replay remains incomplete until the later `331090` issue is analyzed and resolved or classified.

## 15. Recommended Next Phase

Recommended next phase:

**Phase 9B-5 — analyze historical replay failure at block 331090, no code changes first.**

That phase should inspect block `331090`, compare replay balances against legacy/live accepted context, and decide whether it is incomplete copied context, another replay-only balance reconstruction mismatch, or a true legacy compatibility bug.
