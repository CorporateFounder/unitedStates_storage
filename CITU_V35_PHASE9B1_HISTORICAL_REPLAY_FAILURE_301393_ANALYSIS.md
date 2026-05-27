# CITU V35 Phase 9B-1 Historical Replay Failure 301393 Analysis

## 1. Exact files inspected

Reports read:
- `CITU_V35_PHASE9A_LIVE_HISTORICAL_CHAIN_REPLAY_GUIDE.md`
- `CITU_V35_PHASE6C2_HISTORICAL_CHAIN_VALIDATION_REPORT.md`
- `CITU_V35_PHASE6C1_OLD_CHAIN_VALIDATION_BASELINE_REPORT.md`
- `CITU_V35_PHASE6D2_LONG_WINDOW_DIFFICULTY_DRY_RUN_REPORT.md`

Server files inspected:
- `unitedStates_storage-master/src/test/java/International_Trade_Union/utils/HistoricalChainValidationRunnerTest.java`
- `unitedStates_storage-master/src/main/java/International_Trade_Union/entity/blockchain/Blockchain.java`
- `unitedStates_storage-master/src/main/java/International_Trade_Union/utils/UtilsBlock.java`
- `unitedStates_storage-master/src/main/java/International_Trade_Union/utils/UtilsBalance.java`
- `unitedStates_storage-master/src/main/java/International_Trade_Union/utils/UtilsUse.java`
- `unitedStates_storage-master/src/main/java/International_Trade_Union/utils/UtilsResolving.java`
- `unitedStates_storage-master/src/main/java/International_Trade_Union/setings/Seting.java`
- `unitedStates_storage-master/src/main/java/International_Trade_Union/logger/MyLogger.java`
- `unitedStates_storage-master/src/main/java/International_Trade_Union/utils/UtilsFileSaveRead.java`
- `unitedStates_storage-master/src/main/java/International_Trade_Union/entity/DtoTransaction/DtoTransaction.java`

Copied replay data/logs inspected:
- `C:\citu_chain_replay_full\resources\blockchain`
- `C:\citu_chain_replay_full\resources`
- `C:\citu_replay_logs\PHASE9B_REPLAY_FAILURE_301393_SUREFIRE.txt`
- `unitedStates_storage-master/target/surefire-reports/TEST-International_Trade_Union.utils.HistoricalChainValidationRunnerTest.xml`

## 2. Exact failing index/block

Failing block:
- index: `301393`
- segment file: `C:\citu_chain_replay_full\resources\blockchain\157.txt`
- segment line: `794`
- hashBlock: `28304045b5002b8a1180081bf4279504e1180306a401124a8281104041304009`
- previousHash: `c0a3868a9299d40d2182d446118080000859105a200000226820d89031422aba`
- minerAddress: `zraD2LuUah83KXTtYP33pwbUuS62dmp21CxPYXDNGWqW`
- founderAddress: `nNifuwmFZr7fnV1zvmpiyQDV5z7ETWvqR6GSeqeHTY43`
- hashCompexity: `22`
- timestamp: `1725967928000`

Replay output around the failure:

```text
calculateBalance: index: 301393
STAKING:
STAKING:
transactionsCount: 2
after: 0
The block contains transactions where the user's balance is insufficient.
checkfromfile: index:  301393
```

## 3. Segment file containing block 301393

Block `301393` was found in:

```text
C:\citu_chain_replay_full\resources\blockchain\157.txt
```

The copied full replay blockchain folder contains `270` segment files. This is normal for this storage layout; the segment files contain many block JSON lines and are not one file per block.

## 4. Exact failing validation branch

The failure occurs in the legacy balance validation branch of:

```text
UtilsBlock.validationOneBlockWithCanonicalContext(...)
```

The exact branch is:

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

At block `301393`:
- non-BASIS transaction count: `2`
- accepted by `UtilsUse.balanceTransaction(...)`: `0`
- result: block rejected.

Inside `UtilsUse.balanceTransaction(...)`, both failing transactions enter the `VoteEnum.STAKING` path. That path only accepts a staking transaction when:

```java
sender.getDigitalDollarBalance().compareTo(transactionDigitalDollar.add(transactionBonusForMiner)) >= 0
```

Both staking transactions failed that check in the replay context.

## 5. Exact failing transaction(s)

The balance branch filters out the two BASIS reward transactions, so the only checked transactions are the two non-BASIS staking transactions below. Because `transactionsCount = 2` and `after = 0`, both were rejected by `UtilsUse.balanceTransaction(...)`.

Failing transaction 1:

```text
sender: qDWgdtEBzbSGs7AdUVLYEiBP5Bs7nb3m3L8GaBGgbSEP
customer: qDWgdtEBzbSGs7AdUVLYEiBP5Bs7nb3m3L8GaBGgbSEP
digitalDollar: 60.0
digitalStockBalance: 0.0
bonusForMiner: 0.0
voteEnum: STAKING
sign: MEQCIAhwKXIqj3siMqp3qig4Wz71gKmjnEs3FYvC6u/syIA9AiAguahoS4D3nqoMlQlOC+xjhFZxlbG4nNqCIuXCGBqRCQ==
```

Failing transaction 2:

```text
sender: qDWgdtEBzbSGs7AdUVLYEiBP5Bs7nb3m3L8GaBGgbSEP
customer: qDWgdtEBzbSGs7AdUVLYEiBP5Bs7nb3m3L8GaBGgbSEP
digitalDollar: 1000.0
digitalStockBalance: 0.0
bonusForMiner: 0.0
voteEnum: STAKING
sign: MEYCIQC78aCHQWL23OM2LsRm/QDi31ouWUlYugug6v78s76OEQIhANUetqcjGOtUSkpwAoLUXMS7UwgsOSkWYt6f+3AO85nc
```

Notes:
- `DtoTransaction` does not have a separate `digitalStakingBalance` field. The staking behavior is represented by `voteEnum = STAKING` and the `digitalDollar` amount.
- Both transactions are self-directed staking transactions: sender and customer are the same address.
- Block `301393` also contains two BASIS reward transactions:
  - BASIS -> miner `zraD2LuUah83KXTtYP33pwbUuS62dmp21CxPYXDNGWqW`, `359.6` dollar/stock, `YES`
  - BASIS -> founder `nNifuwmFZr7fnV1zvmpiyQDV5z7ETWvqR6GSeqeHTY43`, `35.96` dollar/stock, `YES`
- Those BASIS transactions are excluded by the failing balance branch and are not the immediate cause of this failure.

## 6. Whether V35 was involved

V35 was not involved in the rejecting branch.

Confirmed constants:
- `Seting.V35_RESCUE_FORK_INDEX = Long.MAX_VALUE`
- `Seting.V35_BURN_ADDRESS = "BURN_ADDRESS"`

The relevant gates remain disabled:
- `UtilsBlock.isV35ActiveForValidation(block, forkIndex)` returns false when `forkIndex == Long.MAX_VALUE`.
- `UtilsBalance.calculateBalanceWithV35JackpotContext(...)` falls back to `calculateBalance(...)` when V35 is inactive.
- `UtilsResolving.isV35JackpotAccountingRequired(...)` also requires `V35_RESCUE_FORK_INDEX != Long.MAX_VALUE`.
- Burn-address no-spend policy is gated and does not affect these staking transactions.

This is a pre-V35 legacy balance replay failure, not a V35 reward, jackpot, burn, PoW, or cumulativeWork failure.

## 7. Whether FileNotFoundException is primary or secondary

The `FileNotFoundException` for:

```text
\server\resources\error\error.txt
```

is secondary logging noise.

`MyLogger.saveLog(...)` writes to `Seting.ERROR_FILE`, which is:

```text
/server/resources/error/error.txt
```

`UtilsFileSaveRead.save(...)` uses `new FileWriter(fileName, append)` and does not create missing parent directories. When validation tries to log the failure, the missing error path emits `FileNotFoundException`.

The primary failure already happened before this logging attempt:

```text
The block contains transactions where the user's balance is insufficient.
```

Creating the missing error directory/file would improve diagnostics, but it would not make block `301393` validate.

## 8. Whether copied data appears sufficient

The copied full resources directory exists and includes more than just blockchain segments:

```text
C:\citu_chain_replay_full\resources\blockchain
C:\citu_chain_replay_full\resources\balance
C:\citu_chain_replay_full\resources\allLawsWithBalance
C:\citu_chain_replay_full\resources\transactions
C:\citu_chain_replay_full\resources\sendedTransaction
C:\citu_chain_replay_full\resources\vote
C:\citu_chain_replay_full\resources\h2DB
...
```

However, the current historical runner passes only the blockchain directory to:

```java
Blockchain.checkFromFile(chainPath.toString())
```

`checkFromFile(...)` reconstructs `balances` from an empty `HashMap` by replaying blocks. It does not load the copied `resources/balance` snapshot before validation. It also does not use copied `allLawsWithBalance`, transaction-pool, staking, or H2 state for this replay path.

For the current runner design, the blockchain segment data is enough to reach and parse block `301393`. The failure is not caused by the folder having only `270` segment files. The available evidence points instead to the replay validation/balance-ordering behavior.

## 9. Most likely root cause

Most likely root cause:

```text
Blockchain.checkFromFile(...) updates balances through the current block before calling
UtilsBlock.validationOneBlockWithCanonicalContext(...) for that same block, then validation replays
the same non-BASIS transactions again through UtilsUse.balanceTransaction(...).
```

In `Blockchain.checkFromFile(...)`, the order is:

1. Recalculate/apply the current block:

```java
balances = UtilsBalance.calculateBalanceWithV35JackpotContext(
        balances,
        canonicalContext,
        block,
        Seting.ADDRESS_FOUNDER,
        forkIndex,
        sign,
        new ArrayList<>());
```

2. Then validate the current block using a clone of that already-updated balance map:

```java
valid = UtilsBlock.validationOneBlockWithCanonicalContext(
        Seting.ADDRESS_FOUNDER,
        prevBlock,
        block,
        tempList,
        blockService,
        UtilsUse.balancesClone(balances),
        signs,
        new ArrayList<>(),
        canonicalContext,
        forkIndex);
```

At block `301393`, the current block contains two self-staking transactions from the same sender for `60.0` and `1000.0`. If the block is first applied to balances, the sender's spendable dollar balance is reduced by the staking amounts. The later validation pass then attempts to apply those staking transactions again against the post-block balance. In this replay, that second application rejects both transactions, producing `transactionsCount = 2` and `after = 0`.

That makes this look like a historical replay/checkFromFile balance-ordering limitation or legacy replay bug, not copied-chain corruption and not V35 activation.

Classification:
- incomplete context: possible only in the broad sense that `checkFromFile` ignores copied balance snapshots; not the direct evidence.
- runner limitation: likely.
- historical accepted legacy edge-case: likely, specifically repeated same-sender self-STAKING in one block interacting with post-block validation.
- real chain corruption: unlikely from the evidence; the block is present, parseable, sequential, and reached after 301392 valid blocks.
- current-code legacy regression: possible, because current `checkFromFile` validates against post-block balances and may not match the live acceptance path for this legacy block.

## 10. Recommended next action

Recommended next action:

```text
Open Phase 9B-2 for a diagnostic/minimal-fix investigation of checkFromFile balance ordering around block 301393.
```

Phase 9B-2 should stay narrow:

1. Add or run a focused diagnostic around segment `157.txt`, line `794`, block `301393`.
2. Capture sender `qDWgdtEBzbSGs7AdUVLYEiBP5Bs7nb3m3L8GaBGgbSEP` balance immediately before applying block `301393`.
3. Capture balance immediately after `calculateBalance(...)` applies block `301393`.
4. Run `UtilsUse.balanceTransaction(...)` once against pre-block balances and once against post-block balances.
5. Confirm whether pre-block validation accepts both STAKING transactions while post-block validation rejects them.
6. If confirmed, apply the smallest safe replay-only fix: validate with the pre-block balance snapshot, then apply the block once.
7. Separately create the missing logging directory/file only as a diagnostics cleanup, not as the validation fix.

Do not treat this as a V35 issue and do not change V35 constants, economics, transaction format, or block format.

## 11. Confirmation no code changed

No source code or tests were changed in this phase.

Only this Markdown analysis report was created:

```text
CITU_V35_PHASE9B1_HISTORICAL_REPLAY_FAILURE_301393_ANALYSIS.md
```

## 12. Confirmation no burn/fork/jackpot action was performed

Confirmed:
- V35 was not activated.
- `V35_RESCUE_FORK_INDEX` was not changed.
- No real fork index was selected.
- No founder burn transaction was created.
- No jackpot payout transaction was created.
- No reward, PoW, jackpot, burn, transaction, or block format behavior was changed.
