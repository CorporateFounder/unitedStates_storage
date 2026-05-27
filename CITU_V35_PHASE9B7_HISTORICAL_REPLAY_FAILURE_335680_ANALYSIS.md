# CITU V35 Phase 9B-7 Historical Replay Failure 335680 Analysis

Phase: 9B-7 only  
Scope: analysis only, no code/test changes  
Failing block: `335680`  
Chain path: `C:\citu_chain_replay_full\resources\blockchain`  
V35 activation: not performed  
Burn/jackpot action: not performed

## 1. Exact files inspected

Reports read:

- `CITU_V35_PHASE9B4_HISTORICAL_REPLAY_STAKING_301409_FIX_REPORT.md`
- `CITU_V35_PHASE9B5_HISTORICAL_REPLAY_FAILURE_331090_ANALYSIS.md`
- `CITU_V35_PHASE9B6_HISTORICAL_REPLAY_PRECISION_331090_FIX_REPORT.md`
- `CITU_V35_PHASE9A_LIVE_HISTORICAL_CHAIN_REPLAY_GUIDE.md`

Files inspected:

- `unitedStates_storage-master/src/main/java/International_Trade_Union/utils/UtilsBlock.java`
- `unitedStates_storage-master/src/main/java/International_Trade_Union/utils/UtilsUse.java`
- `unitedStates_storage-master/src/main/java/International_Trade_Union/utils/UtilsBalance.java`
- `unitedStates_storage-master/src/main/java/International_Trade_Union/setings/Seting.java`
- `unitedStates_final-master/src/main/java/International_Trade_Union/setings/Seting.java`
- copied chain segment `C:\citu_chain_replay_full\resources\blockchain\178.txt`
- copied error file path `C:\citu_chain_replay_full\resources\error\error.txt`

## 2. Exact failing block/index

- failing block/index: `335680`
- block hash: `681a86302256c2102004ae6f7190242180200164420120022b04352b08a2c082`
- previous hash: `1a4b6001850104808002918e241024ac003480004b30c262b010a5127f200518`
- miner address: `j7s5DakAbj5D5GnTCN2RpjccmYaMv2Rh5pi2fxTTzWmk`
- founder address: `nNifuwmFZr7fnV1zvmpiyQDV5z7ETWvqR6GSeqeHTY43`
- timestamp: `1733144319000`

## 3. Segment file and line

Block `335680` was found at:

```text
C:\citu_chain_replay_full\resources\blockchain\178.txt:512
```

Nearby local context in the same segment:

- line `476`, block `335644`: same sender sends `1000.0` digital dollars and `0.01` digital stock to the same recipient.
- line `482`, block `335650`: recipient sends `1000.0` digital dollars and `0.0` digital stock onward.
- line `512`, block `335680`: same sender sends `68169.77` digital dollars and `0.01` digital stock to the same recipient.
- line `518`, block `335686`: recipient sends `68169.77` digital dollars and `0.0` digital stock onward.

No inbound stock to the failing sender was found between blocks `335644` and `335680` in the inspected local segment window.

## 4. Exact failing validation branch

The failing branch is the legacy post-`BALANCE_CHEKING` balance-validation branch in:

```text
UtilsBlock.validationOneBlockWithCanonicalContext(...)
```

Relevant code path:

1. `Blockchain.checkFromFile(...)` validates block `335680` in historical replay mode.
2. `UtilsBlock.validationOneBlockWithCanonicalContext(...)` enters `if (thisBlock.getIndex() > BALANCE_CHEKING)`.
3. BASIS transactions are filtered out.
4. One non-BASIS transaction remains.
5. Historical replay calls:

```text
UtilsUse.balanceTransactionForHistoricalReplay(transactions, balances, thisBlock.getIndex())
```

6. Normal `balanceTransaction(...)` does not accept the transaction.
7. The 9B-4 self-STAKING compatibility rule does not apply because this is `YES`, not `STAKING`.
8. The 9B-6 bounded precision-dust compatibility rule does not appear to apply; the local transaction pattern points to a stock-side shortage around `0.01`, which is much larger than the `0.0000001` dust bound.
9. `UtilsBlock` rejects the block with:

```text
The block contains transactions where the user's balance is insufficient.
```

## 5. Exact failing transaction(s)

Block `335680` contains two BASIS reward transactions and one non-BASIS transaction. The BASIS transactions are excluded from the failing balance branch.

Failing non-BASIS transaction:

```text
sender: otw9wf3DrtiiT1AwiMryu7DYyE6gPTfzd8v9tDmQdWJw
recipient/customer: mLKZnGvEh1kefCZrq2WHwANUrpmP6ejbZsGRQCNWkxvx
digitalDollar: 68169.77
digitalStockBalance: 0.01
bonusForMiner: 0.0
voteEnum: YES
sign: MEUCIEUGt+SKyOisLLq+ZUiCArMyvQ4oCcjWuWhm/E3xuIWXAiEAvnZtcvOBD9rRQHdK/G2SAF1XKMuTgC29amdz1LRyyw8=
laws.packetLawName: ""
laws.laws: []
laws.hashLaw: ""
```

BASIS transactions in the same block:

```text
sender: faErFrDnBhfSfNnj1hYjxydKNH28cRw1PBwDQEXH3QsJ
recipient/customer: nNifuwmFZr7fnV1zvmpiyQDV5z7ETWvqR6GSeqeHTY43
digitalDollar: 43.45
digitalStockBalance: 43.45
voteEnum: YES
```

```text
sender: faErFrDnBhfSfNnj1hYjxydKNH28cRw1PBwDQEXH3QsJ
recipient/customer: j7s5DakAbj5D5GnTCN2RpjccmYaMv2Rh5pi2fxTTzWmk
digitalDollar: 434.45
digitalStockBalance: 434.45
voteEnum: YES
```

## 6. Whether V35 was involved

V35 was not involved.

Confirmed constants:

- server `V35_RESCUE_FORK_INDEX = Long.MAX_VALUE`
- server `V35_BURN_ADDRESS = "BURN_ADDRESS"`
- wallet `V35_RESCUE_FORK_INDEX = Long.MAX_VALUE`
- wallet `V35_BURN_ADDRESS = "BURN_ADDRESS"`
- server `BALANCE_CHEKING = 300983`
- wallet `BALANCE_CHEKING = 300983`

Block `335680` is processed under legacy rules. No V35 reward, jackpot, burn-address, PoW target, difficulty, or cumulativeWork rule is active.

## 7. Same root cause class as 301393, 301409, or 331090, or new

This appears to be a new historical replay-context edge case.

It is not the `301393` ordering issue:

- 9B-2 fixed server replay ordering so validation uses pre-block balances and applies each block once.
- Replay has already progressed far beyond `301393`.

It is not the `301409` self-STAKING issue:

- 9B-4 covers only narrow legacy self-`STAKING` declarations.
- The failing `335680` transaction is a non-self `YES` transfer.

It does not look like the same tiny precision dust class as `331090`:

- 9B-6 covers bounded replay dust up to `0.0000001`.
- The failing transaction requires `0.01` stock.
- The local chain window shows the same sender spent `0.01` stock at block `335644` and had no observed stock replenishment before trying another `0.01` stock transfer at block `335680`.

Most likely classification:

```text
legacy replay-context / stock-balance edge case after BALANCE_CHEKING
```

The current evidence is not strong enough to call this real chain corruption. The block is present in the copied canonical segment, the transaction is normal-shaped, and the failure is in replay-reconstructed balance context.

## 8. Whether wallet mirror code needs later attention

Yes.

Prior phases already found the wallet historical replay path does not mirror the server's 9B-2 / 9B-4 / 9B-6 replay compatibility work. If wallet historical replay is expected to validate the copied live chain, wallet mirror code should receive a dedicated later review/fix phase.

No wallet code was changed in this phase.

## 9. Whether missing `error.txt` is primary or secondary

The missing runtime path remains secondary logging noise.

The copied chain folder has:

```text
C:\citu_chain_replay_full\resources\error\error.txt
```

The runtime logger still may try:

```text
\server\resources\error\error.txt
```

The primary failure is the legacy balance-validation rejection at block `335680`, not the logging-path issue.

## 10. Most likely root cause

Most likely root cause:

```text
Block 335680 exposes a legacy replay-context stock-balance mismatch. The replay state rejects a normal non-BASIS YES transfer because the sender cannot satisfy the 0.01 digital-stock requirement after a prior 0.01 stock transfer at block 335644 and no observed stock replenishment before block 335680.
```

This is probably not a simple extension of the 9B-6 precision dust fix. The failing stock amount is `0.01`, which is economically meaningful relative to the current tiny replay dust tolerance.

The safest assumption is that this is either:

- a historical accepted legacy stock-balance edge case,
- a replay reconstruction/context mismatch,
- or missing historical balance/accounting context not represented by blockchain-only replay.

It should not be fixed by expanding the dust tolerance without a dedicated diagnostic.

## 11. Recommended next action

Recommended next phase:

**Phase 9B-8 — diagnose/minimal fix for historical replay stock-balance context at block 335680.**

Suggested 9B-8 scope:

1. Add a focused diagnostic around block `335680`.
2. Capture the sender's exact replay pre-block dollar, stock, and staking balances.
3. Capture the sender's balance after block `335644`.
4. Confirm whether the failure is stock-side `0.01`, dollar-side, or both.
5. Determine whether old live validation accepted this pattern through persisted balance context, ordering, duplicate-signature handling, stock/vote semantics, or another legacy shortcut.
6. Do not hardcode block `335680`.
7. Do not hardcode any address.
8. Do not broaden the 9B-6 dust tolerance unless a tiny bounded precision deficit is proven.
9. Keep live consensus validation strict.
10. Keep V35 behavior unchanged.

## 12. Confirmation no code changed

No source code or tests were modified in this phase.

Only this Markdown report was created:

```text
CITU_V35_PHASE9B7_HISTORICAL_REPLAY_FAILURE_335680_ANALYSIS.md
```

## 13. Confirmation no burn/fork/jackpot action was performed

Confirmed:

- V35 was not activated.
- `V35_RESCUE_FORK_INDEX` was not changed.
- No real fork index was selected.
- No founder burn transaction was created.
- No jackpot payout transaction was created.
- No reward, PoW, jackpot, burn, transaction format, or block format behavior was changed.
