# CITU V35 Phase 9B-2 Historical Replay Balance Ordering Fix Report

## 1. Exact files inspected

- `CITU_V35_PHASE9B1_HISTORICAL_REPLAY_FAILURE_301393_ANALYSIS.md`
- `CITU_V35_PHASE9A_LIVE_HISTORICAL_CHAIN_REPLAY_GUIDE.md`
- `CITU_V35_PHASE6C2_HISTORICAL_CHAIN_VALIDATION_REPORT.md`
- `unitedStates_storage-master/src/test/java/International_Trade_Union/utils/HistoricalChainValidationRunnerTest.java`
- `unitedStates_storage-master/src/main/java/International_Trade_Union/entity/blockchain/Blockchain.java`
- `unitedStates_storage-master/src/main/java/International_Trade_Union/utils/UtilsBlock.java`
- `unitedStates_storage-master/src/main/java/International_Trade_Union/utils/UtilsBalance.java`
- `unitedStates_storage-master/src/main/java/International_Trade_Union/utils/UtilsUse.java`
- `unitedStates_storage-master/src/main/java/International_Trade_Union/utils/UtilsResolving.java`
- `unitedStates_storage-master/src/main/java/International_Trade_Union/setings/Seting.java`

## 2. Exact files changed

- `unitedStates_storage-master/src/main/java/International_Trade_Union/entity/blockchain/Blockchain.java`
- `unitedStates_storage-master/src/test/java/International_Trade_Union/utils/HistoricalChainValidationRunnerTest.java`
- `CITU_V35_PHASE9B2_HISTORICAL_REPLAY_BALANCE_ORDERING_FIX_REPORT.md`

No wallet, website, protocol constants, transaction format, block format, reward, PoW, jackpot, or burn policy files were changed.

## 3. Exact root cause

`Blockchain.checkFromFile(...)` was applying the current block to replay balances before validating that same block.

The historical replay sequence was effectively:

1. Read current block.
2. Apply current block with `UtilsBalance.calculateBalanceWithV35JackpotContext(...)`.
3. Validate current block with `UtilsBlock.validationOneBlockWithCanonicalContext(...)`.
4. During validation, legacy balance checks used `UtilsUse.balanceTransaction(...)` against already-updated post-block balances.

At block `301393`, two self-STAKING transactions from and to:

`qDWgdtEBzbSGs7AdUVLYEiBP5Bs7nb3m3L8GaBGgbSEP`

were valid against the pre-block digital-dollar balance, but failed when rechecked against the post-block state where the funds had already moved into staking balance. This made replay reject the block with:

`The block contains transactions where the user's balance is insufficient.`

V35 was not involved. The failure was in the legacy historical replay balance ordering path.

## 4. Exact fix

`Blockchain.checkFromFile(...)` now validates each block against the pre-block balance state, and only applies the block to balances after validation succeeds.

The corrected replay sequence is:

1. Read current block.
2. Validate current block using current pre-block balances.
3. If validation passes, apply current block once with `UtilsBalance.calculateBalanceWithV35JackpotContext(...)`.
4. Add the block to canonical replay context.
5. Continue.

The first block in the replay stream is now also applied once before it becomes the initial previous/canonical block, so the replay balance state is initialized from the loaded chain data rather than skipping the first block's balance effects.

This preserves live validation behavior and changes only the historical replay ordering in `checkFromFile(...)`.

## 5. Confirmation no hardcoded block/address exception was added

No production exception was added for block `301393`.

No production exception was added for address `qDWgdtEBzbSGs7AdUVLYEiBP5Bs7nb3m3L8GaBGgbSEP`.

No STAKING transactions are skipped or weakened. The regression test uses the historical address and amounts only as a deterministic fixture to prove pre-block balance ordering.

## 6. Whether replay passed block 301393

Yes.

The external replay against:

`C:\citu_chain_replay_full\resources\blockchain`

progressed through block `301393` and continued through later blocks including `301394` through `301408`.

## 7. Whether replay reached the end or failed later

The replay did not reach the end.

After passing block `301393`, it failed later at block `301409`.

## 8. New failing index if any

New failing index: `301409`

Block `301409` is in:

`C:\citu_chain_replay_full\resources\blockchain\157.txt`

line `810`.

The failing block contains a non-BASIS STAKING transaction:

- sender/customer: `28eNM7pFKzTgYb8WyUHgttJK9v9oV3TH4jYMxB1xXAK4o`
- recipient: `28eNM7pFKzTgYb8WyUHgttJK9v9oV3TH4jYMxB1xXAK4o`
- digitalDollar: `1400000.0`
- digitalStockBalance: `0.0`
- bonusForMiner: `0.0`
- vote: `STAKING`

This is a separate historical replay issue and should be analyzed in a follow-up phase before claiming full live-chain replay success.

## 9. Confirmation V35 remains disabled

Confirmed in server `Seting.java`:

- `V35_RESCUE_FORK_INDEX = Long.MAX_VALUE`
- `V35_BURN_ADDRESS = "BURN_ADDRESS"`

The added regression test also asserts:

- `ProtocolVersion.isV35(301393L) == false`
- `BurnAddressPolicy.isNoSpendActive(301393L) == false`

No V35 reward, jackpot, burn, or PoW rule was involved in block `301393`.

## 10. Confirmation no burn/fork/jackpot action was performed

- No fork index was selected.
- No fork index was changed.
- No founder burn transaction was created.
- No jackpot payout transaction was created manually.
- No burn, jackpot, reward, PoW, or consensus policy was changed.

## 11. Test/build results

Server targeted historical runner test:

- Command: `.\mvnw.cmd "-Dtest=International_Trade_Union.utils.HistoricalChainValidationRunnerTest" test`
- Result: PASS
- Tests: 4 run, 0 failures, 0 errors, 0 skipped

Server external historical replay:

- Command: `.\mvnw.cmd "-Dtest=International_Trade_Union.utils.HistoricalChainValidationRunnerTest" "-Dcitu.chain.path=C:\citu_chain_replay_full\resources\blockchain" test`
- Result: FAIL later at block `301409`
- Important result: block `301393` now passes

Server full tests:

- Command: `.\mvnw.cmd test`
- Result: PASS
- Tests: 215 run, 0 failures, 0 errors, 0 skipped

Server package:

- Command: `.\mvnw.cmd clean package`
- Result: PASS
- Tests: 215 run, 0 failures, 0 errors, 0 skipped

Wallet tests/package were not run because only server-side historical replay code and server tests were changed.

## 12. Remaining risks

- Full live historical replay is not yet complete because the replay now stops at block `301409`.
- Block `301409` may represent another replay-context gap, a historical accepted legacy edge case, or a true old-chain compatibility issue. It should be analyzed separately before activation.
- The logger still emits `FileNotFoundException` noise for `\server\resources\error\error.txt` in some paths, but this remains secondary logging-path noise and was not the cause of the block `301393` failure.
- The replay fix is intentionally narrow and does not prove every later historical STAKING edge case is handled.

## 13. Recommended next phase

Recommended next phase:

Phase 9B-3 - Analyze historical replay failure at block 301409, no code changes first.

That phase should determine whether block `301409` fails for another replay balance-context issue, incomplete copied resource context, historical legacy behavior, or current-code regression.
