# CITU V35 Phase 9B-6 Historical Replay Precision 331090 Fix Report

## 1. Exact files inspected

- `CITU_V35_PHASE9B2_HISTORICAL_REPLAY_BALANCE_ORDERING_FIX_REPORT.md`
- `CITU_V35_PHASE9B4_HISTORICAL_REPLAY_STAKING_301409_FIX_REPORT.md`
- `CITU_V35_PHASE9B5_HISTORICAL_REPLAY_FAILURE_331090_ANALYSIS.md`
- `CITU_V35_PHASE9A_LIVE_HISTORICAL_CHAIN_REPLAY_GUIDE.md`
- `unitedStates_storage-master/src/main/java/International_Trade_Union/entity/blockchain/Blockchain.java`
- `unitedStates_storage-master/src/main/java/International_Trade_Union/utils/UtilsBlock.java`
- `unitedStates_storage-master/src/main/java/International_Trade_Union/utils/UtilsUse.java`
- `unitedStates_storage-master/src/main/java/International_Trade_Union/utils/UtilsBalance.java`
- `unitedStates_storage-master/src/main/java/International_Trade_Union/utils/UtilsResolving.java`
- `unitedStates_storage-master/src/test/java/International_Trade_Union/utils/HistoricalChainValidationRunnerTest.java`
- `unitedStates_storage-master/src/main/java/International_Trade_Union/setings/Seting.java`
- `unitedStates_final-master/src/main/java/International_Trade_Union/setings/Seting.java`
- Wallet mirror `Blockchain.java` / replay-risk area reviewed for later parity risk.

## 2. Exact files changed

- `unitedStates_storage-master/src/main/java/International_Trade_Union/entity/blockchain/Blockchain.java`
- `unitedStates_storage-master/src/main/java/International_Trade_Union/utils/UtilsUse.java`
- `unitedStates_storage-master/src/test/java/International_Trade_Union/utils/HistoricalChainValidationRunnerTest.java`
- `CITU_V35_PHASE9B6_HISTORICAL_REPLAY_PRECISION_331090_FIX_REPORT.md`

## 3. Exact root cause

Historical replay reached block `331090` and rejected a legacy non-BASIS `YES` transfer after `BALANCE_CHEKING` because the replay-reconstructed sender balance was fractionally below the historical transaction amount.

The failing transaction was:

- sender: `hK6XwB7LxM5wkqbHV3WTpjhZG1TstggsxqEWMw8FnbGf`
- recipient: `yfudXXYdHx1W9QBZuDubr5ZjiqmdTMuKYkMdJyZYmevc`
- digitalDollar: `805179.0`
- digitalStock: `764179.0`
- bonusForMiner: `0.0`
- vote: `YES`
- segment file: `C:\citu_chain_replay_full\resources\blockchain\175.txt`

The transaction amount is stored through historical double fields at scale `1`, while replay accumulation produced higher-scale decimal balances. The strict post-`BALANCE_CHEKING` balance check treated the tiny replay dust as a real shortage.

This was not the Phase 9B-2 replay ordering bug and not the Phase 9B-4 historical self-STAKING case. It is a replay-only legacy precision/context issue.

## 4. Exact deficit / precision dust amount

For block `331090`, the focused regression test captured:

- required digitalDollar: `805179.0`
- required digitalStock: `764179.0`
- sender replay pre-block digitalDollar: `805178.9999999844`
- sender replay pre-block digitalStock: `764178.9999999857`
- digitalDollar deficit: `0.0000000156`
- digitalStock deficit: `0.0000000143`
- transaction amount scale: `1`
- replay balance scale: `10`

The deficits are tiny precision dust, not a meaningful insufficient-balance transfer.

## 5. Whether this was replay-only or live consensus issue

This fix is replay-only.

The live strict balance validator remains unchanged. The normal `UtilsUse.balanceTransaction(...)` path still rejects the `331090` fixture when presented with the fractional shortage. The compatibility handling is only available through historical replay helpers and is applied only while replaying historical blocks.

## 6. Exact fix

`UtilsUse.java` now has a bounded replay-only dust tolerance:

- `HISTORICAL_REPLAY_DUST_TOLERANCE = 0.0000001`

Replay-only precision normalization is allowed only when all of these are true:

- block index is after `BALANCE_CHEKING`
- block index is before `V35_RESCUE_FORK_INDEX`
- transaction is non-BASIS sender and non-BASIS recipient
- transaction vote is `YES` or `NO`
- sender and recipient are different
- dollar, stock, and miner bonus are nonnegative
- sender has a positive dollar or stock deficit
- each deficit is no larger than `0.0000001`

`Blockchain.checkFromFile(...)` now applies `UtilsUse.applyHistoricalReplayPrecisionDustAdjustments(...)` immediately before replay applying a validated block to the replay balance map. The adjustment tops the replay sender balance up only to the exact transaction-required dollar/stock amount, and only for bounded historical precision dust.

`UtilsUse.balanceTransactionForHistoricalReplay(...)` also recognizes the same bounded dust case so focused tests can prove the historical replay compatibility rule without changing live validation.

## 7. Confirmation no hardcoded block/address exception was added

No production code hardcodes block `331090`, sender `hK6XwB7...`, recipient `yfudXXY...`, or any historical address.

The block/address values appear only in the focused regression test fixture.

## 8. Confirmation normal insufficient-balance validation remains strict

Normal live validation remains strict:

- `UtilsUse.balanceTransaction(...)` still rejects the block `331090` fixture when run against the fractional shortage.
- A meaningful `YES` transfer shortage fixture (`99.999` balance attempting to spend `100.0`) is rejected by historical replay handling.
- The replay dust adjustment is not applied to meaningful shortages.

## 9. Whether block 331090 now passes

Yes. External historical replay against:

`C:\citu_chain_replay_full\resources\blockchain`

now passes block `331090`.

## 10. Whether full replay reached the end or failed later

Full replay did not reach the end. It advanced beyond `331090` and failed later at block `335680`.

New failure details captured from the replay output:

- new failing index: `335680`
- segment file: `C:\citu_chain_replay_full\resources\blockchain\178.txt`
- approximate segment line: `512`
- block hash: `681a86302256c2102004ae6f7190242180200164420120022b04352b08a2c082`
- failing branch: legacy balance validation, "The block contains transactions where user's balance is insufficient."
- visible non-BASIS transaction:
  - sender: `otw9wf3DrtiiT1AwiMryu7DYyE6gPTfzd8v9tDmQdWJw`
  - recipient: `mLKZnGvEh1kefCZrq2WHwANUrpmP6ejbZsGRQCNWkxvx`
  - digitalDollar: `68169.77`
  - digitalStock: `0.01`
  - bonusForMiner: `0.0`
  - vote: `YES`

This should be analyzed separately. No claim of full historical replay PASS is made in this phase.

## 11. New failing index if any

`335680`

Recommended next analysis target:

- `CITU_V35_PHASE9B7_HISTORICAL_REPLAY_FAILURE_335680_ANALYSIS.md`

## 12. Whether wallet mirror code needs later fix

Yes, likely later review is needed if wallet-side historical replay is expected to run against the same copied live chain.

This phase changed only the server replay path. The wallet mirror was reviewed as a risk area, but wallet code was not changed because the required fix and verification target were server-side historical replay.

## 13. Confirmation V35 remains disabled

Confirmed:

- server `Seting.V35_RESCUE_FORK_INDEX = Long.MAX_VALUE`
- wallet `Seting.V35_RESCUE_FORK_INDEX = Long.MAX_VALUE`
- server `Seting.V35_BURN_ADDRESS = "BURN_ADDRESS"`
- wallet `Seting.V35_BURN_ADDRESS = "BURN_ADDRESS"`

The block `331090` regression test asserts:

- `ProtocolVersion.isV35(331090L) == false`
- `BurnAddressPolicy.isNoSpendActive(331090L) == false`

## 14. Confirmation no burn/fork/jackpot action was performed

No founder burn transaction was created.

No jackpot payout transaction was manually created.

No fork index was selected or changed.

No V35 activation was performed.

## 15. Test/build results

Server focused historical runner without external path:

```text
cd C:\citu\unitedStates_storage-master
.\mvnw.cmd "-Dtest=International_Trade_Union.utils.HistoricalChainValidationRunnerTest" test
```

Result:

- PASS
- Tests run: `8`
- Failures: `0`
- Errors: `0`
- Skipped: `0`

Server external historical replay:

```text
cd C:\citu\unitedStates_storage-master
.\mvnw.cmd "-Dtest=International_Trade_Union.utils.HistoricalChainValidationRunnerTest" "-Dcitu.chain.path=C:\citu_chain_replay_full\resources\blockchain" test
```

Result:

- block `331090` passed
- replay failed later at block `335680`
- therefore external replay is not yet a full PASS

Server full tests:

```text
cd C:\citu\unitedStates_storage-master
.\mvnw.cmd test
```

Result:

- PASS
- Tests run: `219`
- Failures: `0`
- Errors: `0`
- Skipped: `0`

Server package:

```text
cd C:\citu\unitedStates_storage-master
.\mvnw.cmd clean package
```

Result:

- PASS / BUILD SUCCESS
- Tests run: `219`
- Failures: `0`
- Errors: `0`
- Skipped: `0`

Known secondary noise:

- `FileNotFoundException: \server\resources\error\error.txt` still appears from legacy logging paths during tests.
- It did not fail server tests or package.
- It remains secondary to historical validation failures.

Wallet tests/package were not run because wallet source was not changed in this phase.

## 16. Remaining risks

- Historical replay still fails later at block `335680`; full live-chain replay remains incomplete.
- The new `335680` failure may represent another replay precision/context edge case, a missing historical context rule, or a different legacy accepted behavior.
- Wallet mirror replay logic may need a later parity fix if wallet historical replay must validate the same copied chain.
- The legacy `error.txt` logging path still emits noisy stack traces and should remain separate from consensus/replay fixes unless a later phase chooses to clean it up.
- The replay-only dust tolerance is deliberately tiny and bounded; future failures with larger shortages must not be folded into this fix without a fresh analysis.

## 17. Recommended next phase

Recommended next phase:

**Phase 9B-7 — Analyze historical replay failure at block 335680, no code changes first.**

That phase should identify the exact sender balance, deficit, transaction fields, and whether the `335680` failure is another bounded precision case or a different historical replay-context issue.
