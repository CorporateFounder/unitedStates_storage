# CITU V35 Phase 6C-2 Historical Chain Validation Report

## Scope

Phase 6C-2 investigated whether the repository already contains enough historical old-chain data for automated replay. It added test-only, read-only historical chain validation runners and a server sample-chain replay. No production code was changed.

## 1. Exact Files / Classes Inspected

Reports:

- `CITU_V35_PHASE6C1_OLD_CHAIN_VALIDATION_BASELINE_REPORT.md`
- `CITU_V35_PHASE6A_PRE_ACTIVATION_HARDENING_PLAN.md`
- `CITU_V35_PHASE6B1_LIVE_CONTROLLER_DATABASE_HARDENING_REPORT.md`

Wallet source:

- `unitedStates_final-master/src/main/java/International_Trade_Union/entity/blockchain/Blockchain.java`
- `unitedStates_final-master/src/main/java/International_Trade_Union/entity/blockchain/DataShortBlockchainInformation.java`
- `unitedStates_final-master/src/main/java/International_Trade_Union/utils/UtilsBlock.java`
- `unitedStates_final-master/src/main/java/International_Trade_Union/utils/UtilsBalance.java`
- `unitedStates_final-master/src/main/java/International_Trade_Union/utils/UtilsResolving.java`
- `unitedStates_final-master/src/main/java/International_Trade_Union/model/Mining.java`
- `unitedStates_final-master/src/main/java/International_Trade_Union/entity/services/BlockService.java`
- `unitedStates_final-master/src/main/java/International_Trade_Union/controllers/BasisController.java`
- `unitedStates_final-master/src/main/java/International_Trade_Union/setings/Seting.java`
- `unitedStates_final-master/src/main/java/International_Trade_Union/utils/UtilsFileSaveRead.java`
- `unitedStates_final-master/src/main/java/International_Trade_Union/utils/UtilsJson.java`
- wallet `src/main/resources`
- wallet `src/test/resources` availability

Server source:

- `unitedStates_storage-master/src/main/java/International_Trade_Union/entity/blockchain/Blockchain.java`
- `unitedStates_storage-master/src/main/java/International_Trade_Union/entity/blockchain/DataShortBlockchainInformation.java`
- `unitedStates_storage-master/src/main/java/International_Trade_Union/utils/UtilsBlock.java`
- `unitedStates_storage-master/src/main/java/International_Trade_Union/utils/UtilsBalance.java`
- `unitedStates_storage-master/src/main/java/International_Trade_Union/utils/UtilsResolving.java`
- `unitedStates_storage-master/src/main/java/International_Trade_Union/model/Mining.java`
- `unitedStates_storage-master/src/main/java/International_Trade_Union/entity/services/BlockService.java`
- `unitedStates_storage-master/src/main/java/International_Trade_Union/controllers/BasisController.java`
- `unitedStates_storage-master/src/main/java/International_Trade_Union/service/TournamentService.java`
- `unitedStates_storage-master/src/main/java/International_Trade_Union/setings/Seting.java`
- `unitedStates_storage-master/src/main/java/International_Trade_Union/utils/UtilsFileSaveRead.java`
- `unitedStates_storage-master/src/main/java/International_Trade_Union/utils/UtilsJson.java`
- server `src/main/resources`
- server `src/test/resources` availability

## 2. Historical Chain Data Candidates Found

### Wallet

- `unitedStates_final-master/src/main/resources/application.properties`
  - Classification: configuration.
- `unitedStates_final-master/src/main/resources/minerAccount/minerAccount.txt`
  - Classification: wallet/account sample, not chain data.
- Wallet templates/static resources.
  - Classification: UI resources, not chain data.
- No wallet `src/main/resources/blockchain` directory was found.
- No wallet `src/test/resources` historical chain fixture was found.

### Server

- `unitedStates_storage-master/src/main/resources/blockchain/0.txt`
  - Classification: small sample chain segment.
  - Contains 4 block JSON lines.
  - The segment uses early-chain legacy shape, including two index-1 records followed by index 2 and index 3.
  - This is not enough to represent the full real canonical historical chain.
- `unitedStates_storage-master/src/main/resources/balance/0.txt`
  - Classification: small balance resource, 7 lines.
- `unitedStates_storage-master/src/main/resources/transactions/0.txt`
  - Classification: small transaction resource, 2 lines.
- `unitedStates_storage-master/src/main/resources/sendedTransaction/0.txt`
  - Classification: transaction history-like resource, larger sample, not a complete block chain.
- `unitedStates_storage-master/src/main/resources/federalLaws/0.txt`
  - Classification: law/resource data, not chain validation input.
- `unitedStates_storage-master/src/main/resources/allLawsWithBalance/0.txt`
  - Classification: law/balance resource data, not chain validation input.
- No server `src/test/resources` historical chain fixture was found.

### Configured runtime paths

Both wallet and server `Seting.java` define file-backed runtime paths such as:

- `ORIGINAL_BLOCKCHAIN_FILE = testPath + "/resources/blockchain/"`
- `ORIGINAL_BALANCE_FILE = testPath + "/resources/balance/"`
- `ORIGINAL_ALL_CORPORATION_LAWS_FILE = testPath + "/resources/federalLaws/"`
- `ORGINAL_ALL_TRANSACTION_FILE = testPath + "/resources/transactions/"`
- `ORIGINAL_ALL_SENDED_TRANSACTION_FILE = testPath + "/resources/sendedTransaction/"`
- `H2_DB = testPath + "/resources/h2DB/"`

These indicate where a live node stores or expects runtime data, but the repository does not include a full canonical historical chain under those paths.

## 3. Was Real Historical Chain Data Found?

No full real historical canonical chain was found in the repository.

A small server sample chain was found and validated. It is useful as a read-only loader/replay smoke test, but it is not sufficient for final old-chain activation confidence.

## 4. Automated Replay Added

Yes, test-only replay/runner coverage was added.

Changed test files:

- `unitedStates_final-master/src/test/java/International_Trade_Union/utils/HistoricalChainValidationRunnerTest.java`
- `unitedStates_storage-master/src/test/java/International_Trade_Union/utils/HistoricalChainValidationRunnerTest.java`

### Wallet runner

The wallet runner is optional and external-path based. It does not fail normal Maven runs when no path is supplied.

It:

- Confirms `V35_RESCUE_FORK_INDEX = Long.MAX_VALUE`.
- Confirms `V35_BURN_ADDRESS = BURN_ADDRESS`.
- If `-Dcitu.chain.path=...` is supplied:
  - verifies the path is a directory,
  - loads block JSON lines with `UtilsBlock.readLineObject(...)`,
  - checks all loaded block heights remain below V35,
  - checks burn no-spend remains inactive,
  - runs `Blockchain.checkFromFile(...)`,
  - asserts validation succeeds,
  - asserts V35 cumulative work remains `0`.

### Server runner

The server runner includes:

1. A deterministic bundled sample-chain replay:
   - Loads `src/main/resources/blockchain`.
   - Confirms the bundled sample has 4 block lines.
   - Confirms indexes begin at 1 and end at 3.
   - Confirms V35 does not activate on those heights.
   - Confirms burn no-spend does not activate.
   - Runs `Blockchain.checkFromFile(...)`.
   - Asserts validation succeeds.
   - Asserts summary size matches the loaded block count.
   - Asserts V35 cumulative work remains `0`.

2. The same optional external-path runner used by wallet.

## 5. How To Run The Optional Replay

Run against a copied chain directory only. The directory must contain the same JSON-line block file format used by `ORIGINAL_BLOCKCHAIN_FILE`, for example files like `0.txt`, `1.txt`, etc.

Server:

```powershell
cd C:\citu\unitedStates_storage-master
.\mvnw.cmd -Dtest=International_Trade_Union.utils.HistoricalChainValidationRunnerTest -Dcitu.chain.path=C:\path\to\copied\resources\blockchain test
```

Wallet:

```powershell
cd C:\citu\unitedStates_final-master
.\mvnw.cmd -Dtest=International_Trade_Union.utils.HistoricalChainValidationRunnerTest -Dcitu.chain.path=C:\path\to\copied\resources\blockchain test
```

The runner is read-only. It uses the project loader/validator path:

- `UtilsBlock.readLineObject(...)`
- `Blockchain.checkFromFile(...)`
- `DataShortBlockchainInformation`

It does not set a fork index, does not activate V35, does not write chain data, and does not use network calls.

## 6. Data Still Needed For Full Historical Replay

To fully validate old-chain compatibility before activation, obtain a copy of a live node's canonical data directory, at minimum:

- `resources/blockchain/` containing complete block JSON-line files.

Recommended supporting copies for audit comparison:

- `resources/balance/`
- `resources/transactions/`
- `resources/sendedTransaction/`
- `resources/allLawsWithBalance/`
- `resources/federalLaws/`
- database export or H2 snapshot if the node relies on DB-backed block/account state.

The replay should run against a copied backup folder, never the live node's mutable data directory.

## 7. Manual Real-Node Validation Procedure

1. Stop the node cleanly.
2. Copy the node runtime data to a backup validation folder, for example:
   - `C:\citu-validation\YYYYMMDD\resources\blockchain\`
   - copy balance, transaction, and law resource folders too if available.
3. Do not run validation against the live mutable node folder.
4. Record:
   - validation timestamp,
   - source node version/commit,
   - source data directory,
   - block file count,
   - total block line count,
   - first block index/hash,
   - last block index/hash.
5. Run the optional replay:
   - server command preferred first,
   - wallet command second for parity.
6. Confirm:
   - `V35_RESCUE_FORK_INDEX = Long.MAX_VALUE`,
   - `V35_BURN_ADDRESS = BURN_ADDRESS`,
   - all historical heights remain pre-V35,
   - `Blockchain.checkFromFile(...)` returns validation true,
   - V35 cumulative work remains zero,
   - no V35 reward, jackpot, compact target PoW, or burn no-spend rule is required for old blocks.
7. If balance comparison data is available:
   - compare recalculated balances with copied `resources/balance/`,
   - investigate any differences before proceeding.
8. Preserve validation logs and the copied data checksum.
9. Only after successful old-chain replay should Phase 6D simulated fork dry-run begin.

## 8. Safety Checks Covered

- `V35_RESCUE_FORK_INDEX` remains `Long.MAX_VALUE`.
- `V35_BURN_ADDRESS` remains `BURN_ADDRESS`.
- Historical/sample block heights stay below the disabled V35 fork gate.
- Burn no-spend does not activate for old-chain heights.
- `Blockchain.checkFromFile(...)` remains usable with V35 disabled.
- V35 cumulative work remains zero for old-chain replay.
- Server bundled sample does not require V35 reward, jackpot, compact target PoW, or burn no-spend behavior.

## 9. Production Code Changed

None.

No production files were edited. No minimal production fixes were needed.

## 10. Burn / Jackpot Transaction Confirmation

- No founder burn transaction was created.
- No manual jackpot payout transaction was created.
- No chain, balance, setting, transaction, or block format changes were made.

## 11. V35 Disabled Confirmation

Confirmed after implementation:

- Wallet `Seting.V35_RESCUE_FORK_INDEX = Long.MAX_VALUE`
- Server `Seting.V35_RESCUE_FORK_INDEX = Long.MAX_VALUE`
- Wallet `Seting.V35_BURN_ADDRESS = "BURN_ADDRESS"`
- Server `Seting.V35_BURN_ADDRESS = "BURN_ADDRESS"`

## 12. Verification Results

- Server `.\mvnw.cmd test`: PASS
  - 201 tests run, 0 failures, 0 errors, 0 skipped.
- Server `.\mvnw.cmd clean package`: PASS
  - 201 tests run, 0 failures, 0 errors, 0 skipped.
- Wallet `.\mvnw.cmd test`: PASS
  - 177 tests run, 0 failures, 0 errors, 0 skipped.
- Wallet `.\mvnw.cmd clean package`: PASS
  - 177 tests run, 0 failures, 0 errors, 0 skipped.

Notes:

- Existing Maven duplicate-dependency warnings remain.
- Existing logger/file-path warnings for `\server\resources\error\error.txt` still appear in some negative validation paths and now also during server sample replay when old resource transactions trigger legacy balance logging. These warnings did not fail validation or builds and were not introduced as production behavior changes.

## 13. Remaining Old-Chain Validation Risks

- Full real historical canonical chain replay is still pending because the repository does not include live historical chain data.
- The server bundled sample validates, but it contains only 4 block lines and is not an activation-grade historical replay.
- Balance equivalence against a full live node balance export has not been automated.
- DB-backed historical block/account replay remains outside this phase unless a copied H2/database snapshot is provided.
- Historical chain validation output is noisy because existing logger paths can emit stack traces for missing local log files.

## 14. Recommended Next Phase

Because the repository now has:

- deterministic old-chain baseline tests,
- a validating bundled server sample replay,
- optional wallet/server external historical chain runners,
- and no production compatibility bugs found,

the recommended next phase is:

**Phase 6D - simulated V35 fork dry-run**.

If a complete live node chain export becomes available before Phase 6D, run the optional `-Dcitu.chain.path=...` replay first and archive the validation output. If that replay exposes a serious gap, pause Phase 6D and open **Phase 6C-3** for the specific old-chain replay fix.
