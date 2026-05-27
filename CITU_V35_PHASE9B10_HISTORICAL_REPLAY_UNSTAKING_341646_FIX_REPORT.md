# CITU V35 Phase 9B-10 Historical Replay UNSTAKING 341646 Fix Report

## 1. Exact Files Inspected

- `CITU_V35_PHASE9B9_HISTORICAL_REPLAY_FAILURE_341646_ANALYSIS.md`
- `CITU_V35_PHASE9B8_HISTORICAL_REPLAY_STOCK_335680_FIX_REPORT.md`
- `CITU_V35_PHASE9B4_HISTORICAL_REPLAY_STAKING_301409_FIX_REPORT.md`
- `unitedStates_storage-master/src/main/java/International_Trade_Union/entity/blockchain/Blockchain.java`
- `unitedStates_storage-master/src/main/java/International_Trade_Union/utils/UtilsUse.java`
- `unitedStates_storage-master/src/main/java/International_Trade_Union/utils/UtilsBlock.java`
- `unitedStates_storage-master/src/main/java/International_Trade_Union/utils/UtilsBalance.java`
- `unitedStates_storage-master/src/test/java/International_Trade_Union/utils/HistoricalChainValidationRunnerTest.java`
- `unitedStates_storage-master/src/main/java/International_Trade_Union/setings/Seting.java`

## 2. Exact Files Changed

- `unitedStates_storage-master/src/main/java/International_Trade_Union/utils/UtilsUse.java`
- `unitedStates_storage-master/src/test/java/International_Trade_Union/utils/HistoricalChainValidationRunnerTest.java`

No wallet, website, protocol, mining, reward, PoW, jackpot, burn policy, transaction format, or block format files were changed.

## 3. Exact Root Cause

Historical replay failed at block `341646` on a legacy self-`UNSTAKING` transaction:

- Segment file: `C:\citu_chain_replay_full\resources\blockchain\181.txt`
- Approximate line: `466`
- Sender/customer: `2A8vxijdyY5ST1WhLQan3N1P6wSdzBDo9VmEFhck9bArG`
- `digitalDollar`: `1,340,000.0`
- `digitalStock`: `0.0`
- `bonusForMiner`: `0.0`
- vote/type: `UNSTAKING`

Replay balance before block `341646`:

- dollar: `125562.28003514555213`
- stock: `1352479.11003514555213`
- staking: `1330000.0`

The strict legacy `UNSTAKING` validation required the full `1,340,000.0` to already be in staking. Replay had `1,330,000.0` staked, a `10,000.0` staking deficit, but the same account's spendable dollar balance plus staking balance covered the declared unstake amount:

- `125562.28003514555213 + 1330000.0 >= 1340000.0`

This was a historical replay-only legacy lifecycle mismatch for self-`UNSTAKING`, not precision dust and not a V35 issue.

## 4. Exact Staking Deficit

- Required staking for the transaction: `1,340,000.0`
- Replay reconstructed staking balance: `1,330,000.0`
- Exact staking deficit: `10,000.0`
- Dollar balance status: sufficient combined account value existed when dollar plus staking were considered.

## 5. Replay-Only Or Live Consensus Issue

This fix is replay-only.

The normal live balance validation path remains strict. The compatibility path is limited to historical replay helpers and only applies to legacy self-`UNSTAKING` declarations after `BALANCE_CHEKING` and before V35 activation.

## 6. Exact Fix

Added a narrow historical replay compatibility rule in `UtilsUse`:

- Detects legacy self-`UNSTAKING` declarations where:
  - `sender == customer`
  - vote is `UNSTAKING`
  - index is after `BALANCE_CHEKING`
  - index is before `V35_RESCUE_FORK_INDEX`
  - stock is exactly zero
  - amount/stock/bonus are nonnegative
  - staking alone is below the required amount
  - dollar plus staking covers the required amount
- Accepts this case only through `balanceTransactionForHistoricalReplay`.
- During replay adjustment, raises the replay staking balance to the declared required unstake amount so the existing normal `UNSTAKING` application path can apply the transaction once.

This mirrors the earlier replay-only self-`STAKING` compatibility style without changing live validation.

## 7. No Hardcoded Block Or Address Exception

No hardcoded exception was added for:

- block `341646`
- address `2A8vxijdyY5ST1WhLQan3N1P6wSdzBDo9VmEFhck9bArG`

The rule is data-driven and constrained by transaction type, self-recipient semantics, legacy height gate, and combined balance coverage.

## 8. Ordinary Insufficient UNSTAKING Remains Strict

Added regression coverage proving a self-`UNSTAKING` declaration with meaningful combined balance shortage is still rejected by historical replay and is not normalized.

The normal live `balanceTransaction(...)` path still rejects the block `341646` style transaction before replay compatibility is applied.

## 9. Whether Block 341646 Now Passes

Yes. The focused historical replay test and the external copied-chain replay both pass block `341646`.

## 10. Whether Full Replay Reached The End Or Failed Later

The external historical replay against:

`C:\citu_chain_replay_full\resources\blockchain`

reached the end successfully.

No new failing index was found.

## 11. New Failing Index

None.

## 12. Wallet Mirror Code

This phase changed the server historical replay path only. Wallet mirror replay code may still need a later parity review if wallet-side historical replay against the same copied chain is required.

No wallet code was changed in this phase.

## 13. V35 Disabled Confirmation

Confirmed:

- `V35_RESCUE_FORK_INDEX = Long.MAX_VALUE`
- `V35_BURN_ADDRESS = BURN_ADDRESS`

The new compatibility rule is also gated before `V35_RESCUE_FORK_INDEX`, and test coverage confirms `ProtocolVersion.isV35(341646L)` is false.

## 14. Burn/Fork/Jackpot Action Confirmation

No burn transaction was created.

No fork index was selected or activated.

No jackpot payout transaction was created.

No reward, PoW, jackpot, burn, transaction, or block format behavior was changed.

## 15. Test And Build Results

Passed:

- Server focused historical runner:
  - `.\mvnw.cmd "-Dtest=International_Trade_Union.utils.HistoricalChainValidationRunnerTest" test`
  - `Tests run: 13, Failures: 0, Errors: 0, Skipped: 0`
- External historical replay:
  - `.\mvnw.cmd "-Dtest=International_Trade_Union.utils.HistoricalChainValidationRunnerTest" "-Dcitu.chain.path=C:\citu_chain_replay_full\resources\blockchain" test`
  - `Tests run: 13, Failures: 0, Errors: 0, Skipped: 0`
  - Full copied-chain replay reached the end.
- Server full tests:
  - `.\mvnw.cmd test`
  - `Tests run: 224, Failures: 0, Errors: 0, Skipped: 0`
- Server package:
  - `.\mvnw.cmd clean package`
  - `BUILD SUCCESS`

Known non-fatal noise remains: attempts to write `\server\resources\error\error.txt` can print `FileNotFoundException` during logging, but Maven exits successfully and validation is not failing on that path.

## 16. Remaining Risks

- The server copied-chain historical replay now passes, but wallet mirror replay parity has not been fixed or proven in this phase.
- The replay compatibility path now contains several narrow legacy accommodations; future cleanup should keep them isolated from live consensus.
- The `error.txt` logging path still emits noisy non-fatal stack traces and can be cleaned in a separate logging-only phase if desired.

## 17. Recommended Next Phase

Recommended next phase:

**Phase 9B-11 - wallet mirror historical replay parity review, no code changes first**

Alternative if wallet replay is not required before release:

**Phase 8C - release notes finalization and artifact checklist**
