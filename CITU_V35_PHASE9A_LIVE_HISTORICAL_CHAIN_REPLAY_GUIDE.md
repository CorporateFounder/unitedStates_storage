# CITU V35 Phase 9A Live Historical-Chain Replay Guide

Phase: 9A only  
Scope: operator guide for later live historical-chain replay  
Code changes: none  
V35 activation: not performed  
Real fork index: not selected  
Founder burn transaction: not created  
Jackpot payout transaction: not created

## 1. Exact Reports / Files Inspected

Reports read:

- `CITU_V35_PHASE6C2_HISTORICAL_CHAIN_VALIDATION_REPORT.md`
- `CITU_V35_PHASE6C1_OLD_CHAIN_VALIDATION_BASELINE_REPORT.md`
- `CITU_V35_PHASE6D1_SIMULATED_FORK_DRY_RUN_REPORT.md`
- `CITU_V35_PHASE6D2_LONG_WINDOW_DIFFICULTY_DRY_RUN_REPORT.md`
- `CITU_V35_PHASE6F1_FINAL_FORK_INDEX_ACTIVATION_PLAN.md`
- `CITU_V35_PHASE7E_FINAL_BURN_EXECUTION_CHECKLIST.md`
- `CITU_V35_PHASE8A_PUBLIC_RELEASE_AND_EXCHANGE_NOTICE_DRAFTS.md`

Files inspected:

- `unitedStates_final-master/src/test/java/International_Trade_Union/utils/HistoricalChainValidationRunnerTest.java`
- `unitedStates_storage-master/src/test/java/International_Trade_Union/utils/HistoricalChainValidationRunnerTest.java`
- `unitedStates_final-master/src/main/java/International_Trade_Union/setings/Seting.java`
- `unitedStates_storage-master/src/main/java/International_Trade_Union/setings/Seting.java`
- wallet/server `Blockchain.java` from prior replay reports
- wallet/server `UtilsBlock.java` from prior replay reports
- wallet/server `UtilsBalance.java` from prior replay reports
- server `src/main/resources` directory structure
- configured runtime paths in wallet/server `Seting.java`

Confirmed:

- Wallet `V35_RESCUE_FORK_INDEX = Long.MAX_VALUE`
- Server `V35_RESCUE_FORK_INDEX = Long.MAX_VALUE`
- Wallet `V35_BURN_ADDRESS = "BURN_ADDRESS"`
- Server `V35_BURN_ADDRESS = "BURN_ADDRESS"`

## 2. Current Replay Readiness Summary

Current state:

- Historical replay runner tests exist in wallet and server.
- The runners support an optional system property:

```text
-Dcitu.chain.path=C:\path\to\copied\resources\blockchain
```

- If no `citu.chain.path` is provided, the optional external replay returns without failing normal Maven tests.
- The server runner also validates the bundled small sample at `src/main/resources/blockchain`.
- Full canonical live-chain replay has not been run yet.
- The repository does not include a full live historical chain.
- V35 remains disabled.
- Real fork index has not been selected.
- Founder burn has not happened.

This guide does not run replay. It defines how an operator should later run replay against a copied live chain folder.

## 3. Expected Chain Data Locations

Do not treat any path below as final unless it is confirmed on the live node. The expected folder depends on how the node was launched and where its runtime `resources` directory lives.

### Configured wallet paths

Wallet `Seting.java` defines:

```text
ORIGINAL_BLOCKCHAIN_FILE = testPath + "/resources/blockchain/"
ORIGINAL_BALANCE_FILE = testPath + "/resources/balance/"
ORGINAL_ALL_TRANSACTION_FILE = testPath + "/resources/transactions/"
ORIGINAL_ALL_SENDED_TRANSACTION_FILE = testPath + "/resources/sendedTransaction/"
H2_DB = testPath + "/resources/h2DB/"
```

Likely wallet runtime chain folder:

```text
<wallet-runtime-root>\resources\blockchain
```

The wallet project repository did not include a full `src/main/resources/blockchain` historical chain fixture.

### Configured server paths

Server `Seting.java` defines:

```text
ORIGINAL_BLOCKCHAIN_FILE = testPath + "/server/resources/blockchain/"
ORIGINAL_BALANCE_FILE = testPath + "/server/resources/balance/"
ORGINAL_ALL_TRANSACTION_FILE = testPath + "/server/resources/transactions/"
ORIGINAL_ALL_SENDED_TRANSACTION_FILE = testPath + "/server/resources/sendedTransaction/"
H2_DB = testPath + "/server/resources/h2DB/"
```

Likely server runtime chain folder:

```text
<server-runtime-root>\server\resources\blockchain
```

The repository also contains a bundled sample folder:

```text
C:\citu\unitedStates_storage-master\src\main\resources\blockchain
```

That bundled folder is a small sample only. It is not a full canonical live chain.

### Required copied chain input for replay

The replay runner expects the copied folder that directly contains block JSON-line files such as:

```text
C:\REAL_CHAIN_COPY\resources\blockchain
```

or:

```text
C:\REAL_CHAIN_COPY\server\resources\blockchain
```

Use the real copied folder that contains the live node's block files. Do not invent a path and do not run against a live writable directory.

Recommended supporting folders to copy for audit comparison:

```text
resources\balance
resources\transactions
resources\sendedTransaction
resources\allLawsWithBalance
resources\federalLaws
resources\h2DB
```

For server runtime layout, use the same subfolders under `server\resources\`.

## 4. Safe Copy Procedure

The replay must run against a copied backup folder, not the live node's mutable data directory.

Manual operator steps:

1. Identify the live node that will be the authoritative source.
2. Record:
   - node role;
   - node version/commit;
   - current height;
   - current tip hash;
   - current time.
3. If required by the operator's node setup, stop or pause the live node safely.
   - This guide does not require shutdown automatically.
   - If hot copy is unsafe for that deployment, stop the node before copying.
4. Copy the runtime data to a backup validation folder, for example:

```text
C:\citu-validation\YYYYMMDD-HHMMSS\resources\blockchain
```

or for server layout:

```text
C:\citu-validation\YYYYMMDD-HHMMSS\server\resources\blockchain
```

5. Copy supporting folders when available:
   - balance;
   - transactions;
   - sendedTransaction;
   - allLawsWithBalance;
   - federalLaws;
   - h2DB or database snapshot if used.
6. Record copy timestamp.
7. Record copied folder size and file count.
8. Restart/resume live node if it was stopped.
9. Run replay against the copied folder only.

Never:

- run replay against the live writable `resources/blockchain` directory;
- write validation output into the chain copy;
- change `V35_RESCUE_FORK_INDEX`;
- create burn or jackpot transactions during replay;
- use external network during replay.

## 5. Server Replay Command

Replace `C:\REAL_CHAIN_COPY\resources\blockchain` with the real copied folder that directly contains block JSON-line files.

PowerShell:

```powershell
cd C:\citu\unitedStates_storage-master
.\mvnw.cmd -Dtest=International_Trade_Union.utils.HistoricalChainValidationRunnerTest "-Dcitu.chain.path=C:\REAL_CHAIN_COPY\resources\blockchain" test
```

If the copied live server uses `server\resources\blockchain`, use:

```powershell
cd C:\citu\unitedStates_storage-master
.\mvnw.cmd -Dtest=International_Trade_Union.utils.HistoricalChainValidationRunnerTest "-Dcitu.chain.path=C:\REAL_CHAIN_COPY\server\resources\blockchain" test
```

Expected runner behavior:

- validates the bundled server sample;
- validates the external chain path when provided;
- loads blocks with `UtilsBlock.readLineObject(...)`;
- runs `Blockchain.checkFromFile(...)`;
- asserts replay validation succeeds;
- asserts V35 cumulative work remains default while fork index is disabled.

## 6. Wallet Replay Command

Replace `C:\REAL_CHAIN_COPY\resources\blockchain` with the real copied folder that directly contains block JSON-line files.

PowerShell:

```powershell
cd C:\citu\unitedStates_final-master
.\mvnw.cmd -Dtest=International_Trade_Union.utils.HistoricalChainValidationRunnerTest "-Dcitu.chain.path=C:\REAL_CHAIN_COPY\resources\blockchain" test
```

If the copied data uses another confirmed runtime layout, pass the copied `blockchain` folder directly.

Expected runner behavior:

- validates only when `citu.chain.path` is supplied;
- loads blocks with `UtilsBlock.readLineObject(...)`;
- runs `Blockchain.checkFromFile(...)`;
- asserts replay validation succeeds;
- asserts V35 cumulative work remains default while fork index is disabled.

## 7. PASS Criteria

Historical replay is PASS only if all conditions below are met:

- The command points at a copied chain folder, not the live writable folder.
- The runner loads chain data.
- Loaded chain data is non-empty.
- All legacy/pre-V35 blocks validate.
- `Blockchain.checkFromFile(...)` returns validation success.
- Replay summary size matches loaded block count.
- No V35 reward rules activate.
- No old reward is rejected because of V35 `6 / 2 / 0` rules.
- No jackpot proof or payout rule is required for old blocks.
- Burn-address no-spend remains inactive for historical heights.
- V35 compact target PoW does not replace legacy old-chain validation.
- V35 cumulativeWork remains default/zero while `Long.MAX_VALUE` disables activation.
- No exception or crash occurs.
- `V35_RESCUE_FORK_INDEX` remains `Long.MAX_VALUE`.
- `V35_BURN_ADDRESS` remains `BURN_ADDRESS`.
- If copied balance data is compared manually, any differences are explained before release.

Optional stronger PASS evidence:

- Server runner passes.
- Wallet runner passes.
- Block count, first block index/hash, and last block index/hash are recorded.
- Full command output is archived.
- Git commit hash used for replay is recorded.

## 8. FAIL Criteria

Replay fails if any of the following happen:

- Chain path cannot load.
- Configured path is not a directory.
- Chain folder is empty.
- Block JSON-line parsing fails.
- Any old block fails validation.
- V35 reward, jackpot, burn, PoW, difficulty, or cumulativeWork behavior activates before the fork.
- Old legacy reward is rejected due to V35 reward shape.
- Burn-address no-spend blocks historical transactions before V35.
- Balance recalculation diverges when checked and the difference is unexplained.
- Runner mutates source data.
- Missing data prevents meaningful validation.
- Unknown exception occurs.
- `V35_RESCUE_FORK_INDEX` is not `Long.MAX_VALUE` during replay.
- The operator accidentally runs against live writable chain data.

If replay fails, treat the release gate as failed until classified and rerun successfully.

## 9. Evidence / Archive Checklist

Archive:

- [ ] Command used.
- [ ] Project path used.
- [ ] Chain copy path.
- [ ] Copy timestamp.
- [ ] Source node version/commit.
- [ ] Git commit hash of replay code.
- [ ] `V35_RESCUE_FORK_INDEX` value during replay.
- [ ] `V35_BURN_ADDRESS` value during replay.
- [ ] Block file count.
- [ ] Total loaded block count.
- [ ] First block index.
- [ ] First block hash, if available in output or from copied data.
- [ ] Last block index.
- [ ] Last block hash, if available in output or from copied data.
- [ ] Server runner output.
- [ ] Wallet runner output.
- [ ] PASS/FAIL result.
- [ ] Screenshots/logs.
- [ ] Any balance comparison notes.
- [ ] Any warnings or non-failing log noise.

Recommended archive folder:

```text
C:\citu-validation\YYYYMMDD-HHMMSS\replay-evidence
```

Recommended filenames:

```text
server-historical-replay-output.txt
wallet-historical-replay-output.txt
chain-copy-manifest.txt
replay-summary.md
seting-constants.txt
git-commit.txt
```

## 10. Failure Response Plan

If replay fails:

1. Stop release progression.
2. Do not perform founder burn.
3. Do not select final fork index.
4. Do not announce final activation.
5. Preserve the copied chain data.
6. Preserve full runner output.
7. Identify the failing block or loader step.
8. Classify the issue:
   - bad or partial copy;
   - wrong folder path;
   - unsupported historical format;
   - true legacy compatibility bug;
   - missing fixture/data;
   - runner limitation;
   - environmental issue;
   - operator command error.
9. If bad copy or wrong path:
   - create a fresh copy;
   - rerun replay.
10. If unsupported format or runner limitation:
   - open a separate scoped phase to improve replay tooling.
11. If true legacy compatibility bug:
   - open a separate scoped fix phase;
   - apply minimal fix only;
   - rerun old-chain tests;
   - rerun historical replay.
12. If unknown:
   - do not proceed;
   - investigate until classified.

Release rule:

```text
No burn, no fork-index selection, and no final activation announcement until historical replay passes or the release owner explicitly documents and accepts the remaining replay gate.
```

## 11. Relation To Burn / Fork Activation

Historical replay should happen before founder burn and before final fork-index activation.

Reason:

- The burn is irreversible.
- The final fork index creates a hard upgrade deadline.
- Old-chain replay proves the current V35-disabled code still accepts existing historical chain data.

Allowed before replay:

- draft public announcements;
- prepare exchange notices;
- prepare release artifact plan;
- prepare burn checklist;
- prepare proof pages.

Not allowed before replay PASS unless explicitly accepted as a release gate:

- manual founder burn;
- final fork-index selection;
- final activation commit;
- exchange publication with final fork index;
- final release announcement claiming readiness.

Burn/fork separation remains:

- Founder burn is a normal founder-signed transaction to `BURN_ADDRESS`.
- Fork activation is a later code/release change to wallet/server `Seting.java`.
- They must not be bundled into one operational step.

## 12. Final Live Replay Checklist

Pre-run:

- [ ] Clean git status or documented intentional changes.
- [ ] Latest V35 branch/commit checked out.
- [ ] Wallet tests pass.
- [ ] Wallet clean package passes.
- [ ] Server tests pass.
- [ ] Server clean package passes.
- [ ] Website tests/package pass if website release artifacts are involved.
- [ ] Live chain copy is available.
- [ ] Replay path points at copied `blockchain` folder.
- [ ] Real fork index still unselected.
- [ ] `V35_RESCUE_FORK_INDEX = Long.MAX_VALUE`.
- [ ] `V35_BURN_ADDRESS = BURN_ADDRESS`.

Run:

- [ ] Server replay command executed.
- [ ] Wallet replay command executed.
- [ ] Outputs saved.
- [ ] Any warnings reviewed.

Post-run:

- [ ] Server replay PASS.
- [ ] Wallet replay PASS.
- [ ] Block count recorded.
- [ ] First/last block index/hash recorded.
- [ ] Evidence archived.
- [ ] Release owner signs off.

Proceed only after PASS:

- [ ] Founder burn checklist may proceed.
- [ ] Release artifact/checksum/tag planning may proceed.
- [ ] Final fork-index activation checklist may proceed later.

## 13. Confirmation No Code Changed

No source code, tests, wallet/server/website files, `pom.xml`, protocol constants, mining behavior, validation behavior, balance behavior, transaction format, block format, API semantics, or UI templates were changed in this phase.

Only this Markdown report was created:

- `CITU_V35_PHASE9A_LIVE_HISTORICAL_CHAIN_REPLAY_GUIDE.md`

## 14. Confirmation V35 Remains Disabled

Confirmed:

- Wallet `V35_RESCUE_FORK_INDEX = Long.MAX_VALUE`
- Server `V35_RESCUE_FORK_INDEX = Long.MAX_VALUE`
- Wallet `V35_BURN_ADDRESS = "BURN_ADDRESS"`
- Server `V35_BURN_ADDRESS = "BURN_ADDRESS"`

No real fork index was selected.

## 15. Confirmation No Burn Transaction Was Created

No founder burn transaction was created, signed, broadcast, stored, or claimed.

No jackpot payout transaction was created.

No replay against a live chain path was performed in this phase.

## 16. Recommended Next Phase

Recommended next phase:

```text
Phase 9B - execute live historical replay once real chain copy path is available.
```

Alternative next phases:

```text
Phase 8B - release artifact/checksum/tag plan, no code changes.
Phase 6F-2 - final activation commit checklist, no code changes.
```

Use Phase 8B if packaging needs to be prepared before the live chain copy is available. Use Phase 6F-2 only after replay evidence, burn timing, exchange readiness, and artifact planning are settled.
