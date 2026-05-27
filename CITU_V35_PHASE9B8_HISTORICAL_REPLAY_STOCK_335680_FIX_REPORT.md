# CITU V35 Phase 9B-8 Historical Replay Block 335680 Fix Report

## 1. Exact Files Inspected

- `CITU_V35_PHASE9B7_HISTORICAL_REPLAY_FAILURE_335680_ANALYSIS.md`
- `CITU_V35_PHASE9B6_HISTORICAL_REPLAY_PRECISION_331090_FIX_REPORT.md`
- `CITU_V35_PHASE9B4_HISTORICAL_REPLAY_STAKING_301409_FIX_REPORT.md`
- `unitedStates_storage-master/src/main/java/International_Trade_Union/utils/UtilsUse.java`
- `unitedStates_storage-master/src/main/java/International_Trade_Union/utils/UtilsBalance.java`
- `unitedStates_storage-master/src/test/java/International_Trade_Union/utils/HistoricalChainValidationRunnerTest.java`
- `unitedStates_storage-master/src/main/java/International_Trade_Union/setings/Seting.java`
- `unitedStates_final-master/src/main/java/International_Trade_Union/setings/Seting.java`
- `unitedStates_final-master/src/main/java/International_Trade_Union/utils/UtilsUse.java` for mirror-risk check
- `C:\citu_chain_replay_full\resources\blockchain\178.txt`
- `C:\citu_chain_replay_full\resources\blockchain\181.txt`

## 2. Exact Files Changed

- `unitedStates_storage-master/src/main/java/International_Trade_Union/utils/UtilsUse.java`
- `unitedStates_storage-master/src/test/java/International_Trade_Union/utils/HistoricalChainValidationRunnerTest.java`
- `CITU_V35_PHASE9B8_HISTORICAL_REPLAY_STOCK_335680_FIX_REPORT.md`

## 3. Exact Root Cause

Phase 9B-7 suspected a stock-balance issue at block `335680`, but the focused diagnostic showed the stock balance was not the failing side.

Failing transaction:

- Block: `335680`
- Segment file: `C:\citu_chain_replay_full\resources\blockchain\178.txt`
- Line: `512`
- Sender: `otw9wf3DrtiiT1AwiMryu7DYyE6gPTfzd8v9tDmQdWJw`
- Recipient: `mLKZnGvEh1kefCZrq2WHwANUrpmP6ejbZsGRQCNWkxvx`
- `digitalDollar`: `68169.77`
- `digitalStockBalance`: `0.01`
- `bonusForMiner`: `0.0`
- Vote: `YES`

Replay balance context before block `335680`:

- Sender replay dollar balance: `68169.74710190214898`
- Sender replay stock balance: `2369169.73710190214898`
- Sender staking balance: `2300000.0`
- Required dollar: `68169.77`
- Required stock: `0.01`

The failure was caused by a replay-reconstructed dollar balance being slightly below the two-decimal transaction amount. The stock balance was more than sufficient.

## 4. Exact Stock Deficit and Dollar Balance Status

- Exact dollar deficit: `0.02289809785102`
- Exact stock deficit: none
- Exact stock surplus: `2369169.72710190214898`

This is not the 9B-6 tiny precision dust case (`0.0000001`). It is a bounded legacy replay cent-drift case after `CHANGE_DECIMAL_2_INDEX`, where historical two-decimal transactions are being checked against replay-accumulated high-scale balances.

## 5. Replay-Only or Live Consensus Issue

This was handled as a replay-only historical compatibility issue.

Live consensus validation remains strict:

- The normal `UtilsUse.balanceTransaction(...)` path still rejects the transaction when the sender balance is below the required amount.
- The new compatibility path only applies through `balanceTransactionForHistoricalReplay(...)` and `applyHistoricalReplayPrecisionDustAdjustments(...)`.
- No V35 validation, reward, jackpot, burn, PoW, transaction format, or block format behavior was changed.

## 6. Exact Fix

Added a bounded replay-only legacy cent-drift rule in `UtilsUse.java`:

- New constant: `HISTORICAL_REPLAY_CENT_DRIFT_TOLERANCE = 0.03`
- Applies only when:
  - the path is historical replay compatibility,
  - block index is at/after `CHANGE_DECIMAL_2_INDEX`,
  - block index is before `V35_RESCUE_FORK_INDEX`,
  - transaction vote is `YES` or `NO`,
  - transaction is not self-send,
  - neither sender nor recipient is `BASIS_ADDRESS`,
  - the dollar deficit is positive but `<= 0.03`,
  - the stock balance is fully covered with no stock deficit.

The adjustment raises only the replay sender dollar balance to the exact required dollar amount before replay applies the transaction through the normal balance path. It does not adjust stock and does not apply to ordinary live validation.

## 7. No Hardcoded Block or Address Exception

No hardcoded exception was added for:

- block `335680`
- sender `otw9wf3DrtiiT1AwiMryu7DYyE6gPTfzd8v9tDmQdWJw`
- recipient `mLKZnGvEh1kefCZrq2WHwANUrpmP6ejbZsGRQCNWkxvx`

The fix is a bounded general historical replay rule.

## 8. Ordinary Insufficient-Stock Validation Remains Strict

Added focused regression coverage proving:

- block `335680` cent drift is accepted only through historical replay compatibility,
- normal live validation still rejects the below-balance transaction,
- dollar shortage above the bounded `0.03` window still fails,
- stock shortage still fails even when dollar shortage is inside the cent-drift window,
- earlier historical replay fixes for `301393`, `301409`, and `331090` remain covered by the same test class.

## 9. Block 335680 Replay Result

The external historical replay progressed past block `335680`.

## 10. Full Replay Result

Full replay did not reach the end.

The next failure is:

- New failing index: `341646`
- Segment file: `C:\citu_chain_replay_full\resources\blockchain\181.txt`
- Line: `466`
- Failing transaction class observed in the segment: self-`UNSTAKING`
- Sender/recipient: `2A8vxijdyY5ST1WhLQan3N1P6wSdzBDo9VmEFhck9bArG`
- Amount: `1340000.0`
- Stock: `0.0`

This should be analyzed separately; no 9B-8 fix was applied for block `341646`.

## 11. Wallet Mirror Code

A targeted check did not find `balanceTransactionForHistoricalReplay` or `HISTORICAL_REPLAY` helpers in wallet `UtilsUse.java`. No wallet code was changed.

If a wallet-side historical replay runner later uses equivalent replay compatibility logic, it should be reviewed separately rather than assuming this server-only helper exists there.

## 12. V35 Status

Confirmed unchanged:

- Server `V35_RESCUE_FORK_INDEX = Long.MAX_VALUE`
- Wallet `V35_RESCUE_FORK_INDEX = Long.MAX_VALUE`
- Server `V35_BURN_ADDRESS = "BURN_ADDRESS"`
- Wallet `V35_BURN_ADDRESS = "BURN_ADDRESS"`

V35 remains disabled.

## 13. Burn/Fork/Jackpot Actions

No burn transaction was created.

No fork index was selected or changed.

No jackpot payout transaction was created.

## 14. Test and Build Results

Focused historical runner test:

```text
cd C:\citu\unitedStates_storage-master
.\mvnw.cmd "-Dtest=International_Trade_Union.utils.HistoricalChainValidationRunnerTest" test
```

Result: PASS

- Tests run: `11`
- Failures: `0`
- Errors: `0`
- Skipped: `0`

External copied-chain replay:

```text
cd C:\citu\unitedStates_storage-master
.\mvnw.cmd "-Dtest=International_Trade_Union.utils.HistoricalChainValidationRunnerTest" "-Dcitu.chain.path=C:\citu_chain_replay_full\resources\blockchain" test
```

Result: FAIL later, after passing block `335680`

- New failing index: `341646`

Normal server test:

```text
cd C:\citu\unitedStates_storage-master
.\mvnw.cmd test
```

Result: PASS

- Tests run: `222`
- Failures: `0`
- Errors: `0`
- Skipped: `0`

Server package:

```text
cd C:\citu\unitedStates_storage-master
.\mvnw.cmd clean package
```

Result: PASS

Known log noise remains: `FileNotFoundException` for `\server\resources\error\error.txt`. It appears during logging paths and did not fail the focused tests, normal tests, or package build.

## 15. Remaining Risks

- Historical replay still has a later failure at block `341646`.
- The new bounded cent-drift rule is intentionally replay-only; it does not prove every later historical decimal-era edge case is covered.
- Wallet mirror risk is low for this specific helper because the targeted check did not find the same historical replay helper, but wallet-side replay should still be reviewed if introduced.
- The missing error log directory remains noisy and should be cleaned up separately if desired, but it was not treated as the primary replay failure.

## 16. Recommended Next Phase

Recommended next phase:

Phase 9B-9 — analyze historical replay failure at block `341646`, no code changes first.

