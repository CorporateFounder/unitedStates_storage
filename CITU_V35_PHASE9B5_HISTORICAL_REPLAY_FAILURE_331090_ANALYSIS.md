# CITU V35 Phase 9B-5 Historical Replay Failure 331090 Analysis

Phase: 9B-5 only  
Scope: analysis only, no code changes  
Failing block: `331090`  
V35 activation: not performed  
Burn/jackpot action: not performed

## 1. Exact Files Inspected

Reports read:

- `CITU_V35_PHASE9B2_HISTORICAL_REPLAY_BALANCE_ORDERING_FIX_REPORT.md`
- `CITU_V35_PHASE9B3_HISTORICAL_REPLAY_FAILURE_301409_ANALYSIS.md`
- `CITU_V35_PHASE9B4_HISTORICAL_REPLAY_STAKING_301409_FIX_REPORT.md`
- `CITU_V35_PHASE9A_LIVE_HISTORICAL_CHAIN_REPLAY_GUIDE.md`

Server files inspected:

- `unitedStates_storage-master/src/main/java/International_Trade_Union/entity/blockchain/Blockchain.java`
- `unitedStates_storage-master/src/main/java/International_Trade_Union/utils/UtilsBlock.java`
- `unitedStates_storage-master/src/main/java/International_Trade_Union/utils/UtilsUse.java`
- `unitedStates_storage-master/src/main/java/International_Trade_Union/utils/UtilsBalance.java`
- `unitedStates_storage-master/src/main/java/International_Trade_Union/utils/UtilsResolving.java`
- `unitedStates_storage-master/src/test/java/International_Trade_Union/utils/HistoricalChainValidationRunnerTest.java`
- `unitedStates_storage-master/src/main/java/International_Trade_Union/setings/Seting.java`

Wallet files inspected for mirror-risk review:

- `unitedStates_final-master/src/main/java/International_Trade_Union/entity/blockchain/Blockchain.java`
- `unitedStates_final-master/src/main/java/International_Trade_Union/setings/Seting.java`

Copied replay data inspected:

- `C:\citu_chain_replay_full\resources\blockchain\175.txt`
- `C:\citu_chain_replay_full\resources\error\error.txt`
- `C:\citu_chain_replay_full\resources\balance`
- `C:\citu_chain_replay_full\resources` directory structure

## 2. Exact Failing Block / Index

Failing block:

- index: `331090`
- segment file: `C:\citu_chain_replay_full\resources\blockchain\175.txt`
- line: `1934`
- previousHash: `0b162c010321008e0140f203b5800a41134520611646a210929706028480ca68`
- hashBlock: `84034308a905286107641682b00180980201122a19106438024ad899d1c0a900`
- minerAddress: `e7WgYXfuZxzrkncoXsJvAiFPS6YMpTTTQxUXYmhVXZgU`
- founderAddress: `nNifuwmFZr7fnV1zvmpiyQDV5z7ETWvqR6GSeqeHTY43`
- randomNumberProof: `14411518830311517`
- minerRewards: `0.0`
- hashCompexity: `22`
- timestamp: `1732211406000`

## 3. Segment File And Line

Block `331090` was found at:

```text
C:\citu_chain_replay_full\resources\blockchain\175.txt:1934
```

This confirms the replay is still walking normal segment files. The copied chain using `270` segment files is expected and does not imply only `270` blocks.

## 4. Exact Failing Validation Branch

The failing branch is the legacy post-`BALANCE_CHEKING` balance validation branch in:

```text
UtilsBlock.validationOneBlockWithCanonicalContext(...)
```

The relevant path is:

1. `Blockchain.checkFromFile(...)` validates block `331090` with `historicalReplay = true`.
2. `UtilsBlock.validationOneBlockWithCanonicalContext(...)` sees `331090 > BALANCE_CHEKING`.
3. It filters out BASIS transactions and validates only non-BASIS transactions.
4. It calls:

```text
UtilsUse.balanceTransactionForHistoricalReplay(transactions, balances, thisBlock.getIndex())
```

5. `balanceTransactionForHistoricalReplay(...)` first tries normal `balanceTransaction(...)`.
6. The block has one non-BASIS transaction, and normal validation accepts `0` of `1`.
7. The narrow 9B-4 historical self-STAKING compatibility rule does not apply because this transaction is `YES`, not self-`STAKING`.
8. `UtilsBlock` rejects the block with:

```text
The block contains transactions where the user's balance is insufficient.
```

Inside `UtilsUse.balanceTransaction(...)`, this transaction enters the `VoteEnum.YES || VoteEnum.NO` branch:

```text
sender.digitalStock >= transaction.digitalStock
sender.digitalDollar >= transaction.digitalDollar + bonus
```

The failing condition is the strict legacy balance check for a normal non-BASIS `YES` transfer.

## 5. Exact Failing Transaction

Block `331090` contains two BASIS reward transactions and one non-BASIS transaction. The BASIS transactions are excluded from the failing balance branch.

The failing non-BASIS transaction is:

```text
sender: hK6XwB7LxM5wkqbHV3WTpjhZG1TstggsxqEWMw8FnbGf
customer: yfudXXYdHx1W9QBZuDubr5ZjiqmdTMuKYkMdJyZYmevc
digitalDollar: 805179.0
digitalStockBalance: 764179.0
bonusForMiner: 0.0
voteEnum: YES
sign: MEUCIGpy1MeYxSxXYDYMZ63vd36DVzPl2aqR8ZksvwqyBKGpAiEA2QVdYwl/SJOOesq/tSgAuAR7VTtJbey5JM+F6EJDJDs=
laws.packetLawName: ""
laws.laws: []
laws.hashLaw: ""
```

The BASIS reward transactions in the same block are:

```text
BASIS -> e7WgYXfuZxzrkncoXsJvAiFPS6YMpTTTQxUXYmhVXZgU
digitalDollar: 296.6
digitalStockBalance: 296.6
voteEnum: YES
```

```text
BASIS -> nNifuwmFZr7fnV1zvmpiyQDV5z7ETWvqR6GSeqeHTY43
digitalDollar: 29.66
digitalStockBalance: 29.66
voteEnum: YES
```

## 6. Whether V35 Was Involved

V35 was not involved.

Confirmed constants:

- server `V35_RESCUE_FORK_INDEX = Long.MAX_VALUE`
- server `V35_BURN_ADDRESS = "BURN_ADDRESS"`
- wallet `V35_RESCUE_FORK_INDEX = Long.MAX_VALUE`
- wallet `V35_BURN_ADDRESS = "BURN_ADDRESS"`

Block `331090` is processed under legacy rules. No V35 reward, jackpot, burn-address, PoW target, difficulty, or cumulativeWork rule is active in this failure.

## 7. Same Root Cause Class As 301393 / 301409 Or New

This is a new root cause class.

It is not the block `301393` ordering issue:

- 9B-2 fixed server `checkFromFile(...)` so each block is validated against pre-block balances and applied only after validation succeeds.
- Block `331090` is failing after that ordering fix, in the corrected pre-block validation sequence.

It is not the block `301409` self-STAKING compatibility issue:

- 9B-4 added a narrow historical replay rule for legacy self-`STAKING` declarations.
- Block `331090` is a non-self normal `YES` transfer.
- The 9B-4 compatibility branch deliberately does not apply.

The most likely class is a historical legacy precision/replay-context issue for normal `YES` transfer validation.

A read-only diagnostic replay estimate mirroring the current balance arithmetic found the sender immediately before block `331090` at approximately:

```text
sender: hK6XwB7LxM5wkqbHV3WTpjhZG1TstggsxqEWMw8FnbGf
digitalDollarBalance: 805178.9999999844
digitalStockBalance: 764178.9999999857
digitalStakingBalance: 0
```

The transaction requires:

```text
digitalDollar: 805179.0
digitalStockBalance: 764179.0
bonusForMiner: 0.0
```

So the replay state is short by tiny fractional dust:

```text
dollar shortfall: about 0.0000000156
stock shortfall: about 0.0000000143
```

That explains why the strict `compareTo(...) >= 0` style balance validation can reject the transaction even though the historical chain accepted it.

## 8. Whether Wallet Mirror Code Needs Attention

Yes.

Wallet `Blockchain.checkFromFile(...)` still appears to retain the older replay behavior:

- it skips applying the first block to balances when `prevBlock == null`;
- it applies later blocks before validation;
- it calls the normal `validationOneBlockWithCanonicalContext(...)` overload rather than the server's historical replay overload;
- it does not include the server-side 9B-2 ordering fix or the 9B-4 historical replay helper path.

This phase did not change wallet code. A later dedicated wallet mirror/parity phase should decide whether to port the server replay-ordering and historical replay compatibility work into wallet `checkFromFile(...)`.

## 9. Whether Missing `error.txt` Is Primary Or Secondary

The missing runtime path remains secondary logging noise.

The copied replay folder contains:

```text
C:\citu_chain_replay_full\resources\error\error.txt
```

but the server logger may still try to write to the configured runtime path:

```text
\server\resources\error\error.txt
```

The primary failure happens before logging, in the legacy balance validation branch. The missing runtime `error.txt` path can obscure diagnostics, but it does not cause block `331090` to fail validation.

## 10. Most Likely Root Cause

Most likely root cause:

```text
Block 331090 exposes a historical replay precision/context mismatch for a normal YES transfer. The replay-reconstructed sender balance is fractionally below the transaction amount, likely because historical amounts and rewards include non-canonical floating decimal values and replay performs strict BigDecimal comparisons without a legacy dust tolerance or canonicalization step.
```

This does not look like real chain corruption based on current evidence:

- the block is present in the copied historical chain segment;
- the transaction is normal-shaped and signed;
- the gap is tiny fractional dust rather than a large missing balance;
- prior fixes correctly allowed replay to progress from `301393` through `301409` to this later, distinct edge case.

It also does not look like incomplete copied blockchain segment data. However, copied persisted balance context is not available in `resources/balance` for direct height comparison; that folder appears empty in the inspected copy. Supporting resource folders exist, but the current historical replay runner primarily reconstructs balances from `resources/blockchain`.

## 11. Recommended Next Action

Recommended next phase:

```text
Phase 9B-6 - minimal diagnostic/fix for historical replay precision dust at block 331090.
```

Recommended 9B-6 scope:

1. Add a focused diagnostic test for the `331090` transaction using the replay-reconstructed sender balance.
2. Prove the failure is only the tiny fractional dust gap, not a large missing balance.
3. Decide whether historical replay needs a narrow legacy-compatible precision rule for pre-V35 old-chain validation.
4. Keep any tolerance replay-only and tightly bounded.
5. Do not hardcode block `331090`.
6. Do not hardcode either address.
7. Do not skip validation.
8. Do not weaken ordinary insufficient-balance checks.
9. Consider canonical two-decimal or tiny-epsilon comparison only for legacy historical replay if tests prove it is safe.
10. Keep live consensus and V35 behavior unchanged.

Also recommended:

- keep the 9B-2 server ordering fix;
- keep the 9B-4 self-STAKING replay compatibility fix;
- plan a separate wallet mirror phase because wallet replay code still differs from the server replay path;
- leave the logging-path cleanup separate unless it blocks diagnostics.

## 12. Confirmation No Code Changed

No source code or tests were modified in this phase.

Only this Markdown report was created:

```text
CITU_V35_PHASE9B5_HISTORICAL_REPLAY_FAILURE_331090_ANALYSIS.md
```

## 13. Confirmation No Burn / Fork / Jackpot Action Was Performed

Confirmed:

- V35 was not activated.
- `V35_RESCUE_FORK_INDEX` was not changed.
- No real fork index was selected.
- No founder burn transaction was created.
- No jackpot payout transaction was created.
- No reward, PoW, jackpot, burn, transaction format, or block format behavior was changed.
