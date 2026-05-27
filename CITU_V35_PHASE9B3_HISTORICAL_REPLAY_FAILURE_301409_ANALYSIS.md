# CITU V35 Phase 9B-3 Historical Replay Failure 301409 Analysis

## 1. Exact files inspected

Reports read:

- `CITU_V35_PHASE9B1_HISTORICAL_REPLAY_FAILURE_301393_ANALYSIS.md`
- `CITU_V35_PHASE9B2_HISTORICAL_REPLAY_BALANCE_ORDERING_FIX_REPORT.md`
- `CITU_V35_PHASE9A_LIVE_HISTORICAL_CHAIN_REPLAY_GUIDE.md`

Server files inspected:

- `unitedStates_storage-master/src/main/java/International_Trade_Union/entity/blockchain/Blockchain.java`
- `unitedStates_storage-master/src/test/java/International_Trade_Union/utils/HistoricalChainValidationRunnerTest.java`
- `unitedStates_storage-master/src/main/java/International_Trade_Union/utils/UtilsBlock.java`
- `unitedStates_storage-master/src/main/java/International_Trade_Union/utils/UtilsBalance.java`
- `unitedStates_storage-master/src/main/java/International_Trade_Union/utils/UtilsUse.java`
- `unitedStates_storage-master/src/main/java/International_Trade_Union/utils/UtilsResolving.java`
- `unitedStates_storage-master/src/main/java/International_Trade_Union/setings/Seting.java`
- `unitedStates_storage-master/src/main/java/International_Trade_Union/logger/MyLogger.java`
- `unitedStates_storage-master/src/main/java/International_Trade_Union/utils/UtilsFileSaveRead.java`

Wallet files inspected:

- `unitedStates_final-master/src/main/java/International_Trade_Union/entity/blockchain/Blockchain.java`
- `unitedStates_final-master/src/main/java/International_Trade_Union/setings/Seting.java`

Copied replay data inspected:

- `C:\citu_chain_replay_full\resources\blockchain\157.txt`

## 2. Exact failing block/index

Failing block:

- index: `301409`
- hashBlock: `242643a4496d410012a8811238724caeb32e9267602302040480a0100011daa0`
- previousHash: `04034e09340048a122140631002a558335009680803808cbf4913e738e124200`
- minerAddress: `21Qsp2EjJhYhqP1fnWm6UufnEtavocHXJbS8MhAV9UKwJ`
- founderAddress: `nNifuwmFZr7fnV1zvmpiyQDV5z7ETWvqR6GSeqeHTY43`
- hashCompexity: `17`
- timestamp: `1725971939000`

## 3. Segment file and line if found

Block `301409` was found in:

`C:\citu_chain_replay_full\resources\blockchain\157.txt`

Approximate line:

`810`

## 4. Exact failing validation branch

The failure is in the same legacy balance-validation branch as the previous 301393 failure, but not for the same ordering reason.

The branch is in:

`UtilsBlock.validationOneBlockWithCanonicalContext(...)`

Relevant logic:

```java
if (thisBlock.getIndex() > BALANCE_CHEKING) {
    Map<String, Account> balances = balance;
    ...
    List<DtoTransaction> transactions = thisBlock.getDtoTransactions()
            .stream()
            .filter(t -> !t.getSender().equals(BASIS_ADDRESS))
            .collect(Collectors.toList());
    int transactionsCount = transactions.size();
    List<DtoTransaction> temp = UtilsUse.balanceTransaction(transactions, balances, thisBlock.getIndex());
    int after = temp.size();

    if (after != transactionsCount) {
        System.out.println("The block contains transactions where the user's balance is insufficient.");
        validated = false;
        return validated;
    }
}
```

At `301409`, `BALANCE_CHEKING = 300983`, so this branch is active.

The block has one non-BASIS transaction. Replay output after the 9B-2 ordering fix indicates:

- `transactionsCount: 1`
- `after: 0`
- rejection reason: `The block contains transactions where the user's balance is insufficient.`

Inside `UtilsUse.balanceTransaction(...)`, the transaction enters the `VoteEnum.STAKING` path:

```java
if (sender.getDigitalDollarBalance().compareTo(transactionDigitalDollar.add(transactionBonusForMiner)) >= 0) {
    result = UtilsBalance.sendMoney(...);
}
```

So the failing condition is insufficient spendable digital-dollar balance for a self-STAKING transaction.

## 5. Exact failing transaction(s), if found

The failing non-BASIS transaction is:

```text
sender: 28eNM7pFKzTgYb8WyUHgttJK9v9oV3TH4jYMxB1xXAK4o
customer: 28eNM7pFKzTgYb8WyUHgttJK9v9oV3TH4jYMxB1xXAK4o
digitalDollar: 1400000.0
digitalStockBalance: 0.0
bonusForMiner: 0.0
voteEnum: STAKING
sign: MEUCICBSkgCXHbBV8NDwoj/6c+7wUcNceT8bReHJLjbkufKoAiEAsOk0zYm0RX4k9ZnO3EvJxOKeSVc5fgMtQcAjOi/lHKM=
```

The block also contains two BASIS reward transactions, which are excluded from the failing balance branch:

```text
sender: faErFrDnBhfSfNnj1hYjxydKNH28cRw1PBwDQEXH3QsJ
customer: 21Qsp2EjJhYhqP1fnWm6UufnEtavocHXJbS8MhAV9UKwJ
digitalDollar: 330.6
digitalStockBalance: 330.6
bonusForMiner: 0.0
voteEnum: YES
```

```text
sender: faErFrDnBhfSfNnj1hYjxydKNH28cRw1PBwDQEXH3QsJ
customer: nNifuwmFZr7fnV1zvmpiyQDV5z7ETWvqR6GSeqeHTY43
digitalDollar: 33.06
digitalStockBalance: 33.06
bonusForMiner: 0.0
voteEnum: YES
```

## 6. Whether V35 was involved

V35 was not involved.

Confirmed constants:

- server `V35_RESCUE_FORK_INDEX = Long.MAX_VALUE`
- server `V35_BURN_ADDRESS = "BURN_ADDRESS"`
- wallet `V35_RESCUE_FORK_INDEX = Long.MAX_VALUE`
- wallet `V35_BURN_ADDRESS = "BURN_ADDRESS"`

At block `301409`, V35 activation helpers remain inactive because the configured fork index is `Long.MAX_VALUE`.

This is not a V35 reward, jackpot, burn-address, PoW, difficulty, or cumulativeWork failure.

## 7. Whether this is same root cause class as 301393 or new root cause

This is a new root cause class.

Block `301393` failed because `checkFromFile(...)` validated the block against post-block balances after applying the block first. After the 9B-2 server fix, `checkFromFile(...)` validates against pre-block balances and applies the block only after validation succeeds.

Block `301409` is now failing with the corrected ordering. The failing transaction tries to move `1,400,000.0` digital dollars into staking, but the replay-reconstructed pre-block spendable digital-dollar balance for that address appears far below that amount.

A read-only diagnostic scan of the copied blockchain data using the same STAKING balance model estimated the address immediately before block `301409` as approximately:

```text
digitalDollar: 71,365.20885803517
digitalStock: 1,412,196.2788580486
digitalStaking: 1,370,831.0700000033
```

That makes the current-code STAKING check fail because it requires:

```text
digitalDollar >= 1,400,000.0
```

The address appears to have enough stock-like or staking-like balance near that value, but not enough spendable digital-dollar balance under the current `UtilsUse.balanceTransaction(...)` and `UtilsBalance.sendMoney(...)` STAKING rules.

Most likely classification:

- not the 301393 double-apply ordering bug;
- likely a historical legacy STAKING compatibility edge case or replay-context gap;
- possibly related to old accepted behavior around the `BALANCE_CHEKING = 300983` era;
- not enough evidence yet to call it real chain corruption.

## 8. Whether wallet mirror code needs attention

Yes.

The wallet `Blockchain.checkFromFile(...)` still appears to have the old replay ordering pattern:

1. If `prevBlock == null`, it sets `prevBlock` and adds the block to canonical context without applying that first block to balances.
2. For later blocks, it calls `UtilsBalance.calculateBalanceWithV35JackpotContext(...)` before `UtilsBlock.validationOneBlockWithCanonicalContext(...)`.
3. It then validates the same block after the balance state has already been updated.

This is the same ordering pattern that Phase 9B-2 fixed on the server side.

No wallet code was changed in this phase, but a later mirror-fix phase should align wallet `checkFromFile(...)` with the corrected server ordering before relying on wallet historical replay.

## 9. Whether missing error.txt is primary or secondary

The missing `\server\resources\error\error.txt` path remains secondary logging noise.

`MyLogger.saveLog(...)` writes to `Seting.ERROR_FILE`, and `UtilsFileSaveRead.save(...)` uses `new FileWriter(fileName, append)` without creating missing parent directories.

The validation failure happens first in `UtilsBlock.validationOneBlockWithCanonicalContext(...)`; the missing log file only makes error reporting noisy after the rejection path tries to log diagnostics.

## 10. Most likely root cause

Most likely root cause:

```text
Block 301409 exposes a historical legacy STAKING/replay-context mismatch: current replay validation requires the sender to have 1,400,000.0 spendable digital dollars for a STAKING self-transaction, but the reconstructed pre-block replay state has far less spendable digital-dollar balance.
```

The transaction is not invalid because of V35 and not because of the 301393 post-block ordering bug. The 9B-2 fix appears correct and merely revealed the next historical edge case.

The strongest open question is why the live historical chain accepted block `301409`:

- the live node may have had additional balance/accounting context not reproduced by `checkFromFile(...)`;
- older legacy STAKING semantics may have differed from the current replay check;
- copied resource folders such as `balance` or `allLawsWithBalance` may need to be incorporated for a faithful replay mode;
- or current code may contain a legacy compatibility regression around large self-STAKING transactions shortly after `BALANCE_CHEKING`.

## 11. Recommended next action

Recommended next phase:

Phase 9B-4 - focused diagnostic/minimal fix plan for block `301409`, no code changes first or minimal tests only.

That phase should:

1. Add a diagnostic test or runner mode around block `301409`.
2. Capture the exact Java replay `Account` for `28eNM7pFKzTgYb8WyUHgttJK9v9oV3TH4jYMxB1xXAK4o` immediately before validation.
3. Compare that account to copied `resources/balance` if a height-relevant balance snapshot exists.
4. Determine whether historical STAKING at this height should use dollar balance, staking balance, stock balance, or a legacy rule.
5. Determine whether a general legacy-compatible replay mode is needed for blocks around `BALANCE_CHEKING`.
6. Mirror the 9B-2 replay-ordering fix into wallet `Blockchain.checkFromFile(...)` in a separate scoped phase.
7. Avoid any hardcoded exception for block `301409` or the failing address.

Do not skip validation and do not weaken live consensus rules.

## 12. Confirmation no code changed

No source code or tests were modified in this phase.

Only this Markdown report was created:

- `CITU_V35_PHASE9B3_HISTORICAL_REPLAY_FAILURE_301409_ANALYSIS.md`

## 13. Confirmation no burn/fork/jackpot action was performed

Confirmed:

- V35 was not activated.
- `V35_RESCUE_FORK_INDEX` was not changed.
- No real fork index was selected.
- No founder burn transaction was created.
- No jackpot payout transaction was created.
- No reward, PoW, jackpot, burn, transaction, or block format behavior was changed.
