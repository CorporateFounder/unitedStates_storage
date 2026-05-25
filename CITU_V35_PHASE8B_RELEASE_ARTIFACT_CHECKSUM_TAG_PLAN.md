# CITU V35 Phase 8B Release Artifact / Checksum / Tag Plan

Phase: 8B only  
Scope: release artifact, checksum, version, tag, and exchange delivery plan  
Code changes: none  
V35 activation: not performed  
Real fork index: not selected  
Founder burn transaction: not created  
Jackpot payout transaction: not created

## 1. Exact Reports / Files Read

Reports read:

- `CITU_V35_PHASE8A_PUBLIC_RELEASE_AND_EXCHANGE_NOTICE_DRAFTS.md`
- `CITU_V35_PHASE9A_LIVE_HISTORICAL_CHAIN_REPLAY_GUIDE.md`
- `CITU_V35_PHASE7E_FINAL_BURN_EXECUTION_CHECKLIST.md`
- `CITU_V35_PHASE6F1_FINAL_FORK_INDEX_ACTIVATION_PLAN.md`
- `CITU_V35_PHASE6E_RELEASE_ROLLBACK_EXCHANGE_CHECKLIST.md`
- `CITU_V35_PHASE6D2_LONG_WINDOW_DIFFICULTY_DRY_RUN_REPORT.md`
- `CITU_V35_PHASE7D_PUBLIC_BURN_PROOF_LIVE_STATUS_REPORT.md`

Project structure inspected:

- `unitedStates_final-master/pom.xml`
- `unitedStates_storage-master/pom.xml`
- `website.citu-web-master/pom.xml`
- existing `target/` folders and current jar naming
- wallet/server `Seting.java` constants

Observed current build output names from existing `target/` folders:

- wallet: `unitedStates-0.32.216-SNAPSHOT.jar`
- server/storage: `unitedStates-0.0.423-SNAPSHOT.jar`
- website: `website.citu-0.0.24-SNAPSHOT.jar`

These existing outputs were inspected only. No new release artifact was created in this phase.

Confirmed:

- Wallet `V35_RESCUE_FORK_INDEX = Long.MAX_VALUE`
- Server `V35_RESCUE_FORK_INDEX = Long.MAX_VALUE`
- Wallet `V35_BURN_ADDRESS = "BURN_ADDRESS"`
- Server `V35_BURN_ADDRESS = "BURN_ADDRESS"`
- Real fork index has not been selected.
- Founder burn has not happened.

## 2. Current Release Readiness Summary

Current state:

- V35 code is implemented but disabled.
- Simulated V35 fork dry-run passed.
- Long-window difficulty dry-run passed.
- Public release and exchange notice drafts exist.
- Burn execution checklist exists.
- Public founder burn proof page exists with optional live/status integration.
- Historical replay guide exists.
- Full live historical-chain replay still must be executed later against copied live-chain data.
- Real fork index has not been selected.
- Founder burn has not happened.

Release packaging can be planned now, but final release publication remains gated by:

- live historical-chain replay evidence;
- release owner go/no-go approval;
- exchange/node upgrade coordination;
- founder burn timing/proof decision;
- final fork-index activation plan.

## 3. Artifact Inventory

Expected V35 release package should include:

### Runtime artifacts

- Wallet build artifact:
  - current observed jar pattern: `unitedStates-0.32.216-SNAPSHOT.jar`
  - source project: `unitedStates_final-master`
- Server/storage build artifact:
  - current observed jar pattern: `unitedStates-0.0.423-SNAPSHOT.jar`
  - source project: `unitedStates_storage-master`
- Website build artifact:
  - current observed jar pattern: `website.citu-0.0.24-SNAPSHOT.jar`
  - source project: `website.citu-web-master`

### Source / documentation artifacts

- Source archive for the release commit or tag.
- Release notes.
- Public V35 announcement.
- Exchange/node operator technical notice.
- Founder burn proof page/status link.
- Live historical replay evidence package.
- Long-window difficulty dry-run report.
- Checksums file.
- Optional signature file if the project has an approved signing workflow.

### Suggested artifact bundle names

For RC while V35 remains disabled:

```text
citu-wallet-v35-rc1.jar
citu-server-v35-rc1.jar
citu-website-v35-rc1.jar
citu-v35-rc1-source.zip
citu-v35-rc1-checksums-sha256.txt
citu-v35-rc1-release-notes.md
citu-v35-rc1-exchange-notice.md
```

For final activation release after real fork index is selected:

```text
citu-wallet-v35-fork-<INDEX>.jar
citu-server-v35-fork-<INDEX>.jar
citu-website-v35-fork-<INDEX>.jar
citu-v35-fork-<INDEX>-source.zip
citu-v35-fork-<INDEX>-checksums-sha256.txt
citu-v35-fork-<INDEX>-release-notes.md
citu-v35-fork-<INDEX>-exchange-notice.md
```

Do not publish placeholder `<INDEX>` names as final artifacts.

## 4. Build Command Plan

These are the safe build commands to run during the actual release packaging phase. They were not run in Phase 8B.

### Wallet

```powershell
cd C:\citu\unitedStates_final-master
.\mvnw.cmd clean package
```

Expected jar location after build:

```text
C:\citu\unitedStates_final-master\target\
```

Current observed jar pattern:

```text
unitedStates-0.32.216-SNAPSHOT.jar
```

### Server / storage

```powershell
cd C:\citu\unitedStates_storage-master
.\mvnw.cmd clean package
```

Expected jar location after build:

```text
C:\citu\unitedStates_storage-master\target\
```

Current observed jar pattern:

```text
unitedStates-0.0.423-SNAPSHOT.jar
```

### Website

```powershell
cd C:\citu\website.citu-web-master
.\mvnw.cmd clean package
```

Expected jar location after build:

```text
C:\citu\website.citu-web-master\target\
```

Current observed jar pattern:

```text
website.citu-0.0.24-SNAPSHOT.jar
```

## 5. Checksum Generation Plan

Generate SHA-256 checksums after the final artifact files are built and copied to a release staging folder.

PowerShell placeholder examples:

```powershell
Get-FileHash "C:\citu-release\v35-rc1\citu-wallet-v35-rc1.jar" -Algorithm SHA256
Get-FileHash "C:\citu-release\v35-rc1\citu-server-v35-rc1.jar" -Algorithm SHA256
Get-FileHash "C:\citu-release\v35-rc1\citu-website-v35-rc1.jar" -Algorithm SHA256
Get-FileHash "C:\citu-release\v35-rc1\citu-v35-rc1-source.zip" -Algorithm SHA256
```

Final activation placeholder examples:

```powershell
Get-FileHash "C:\citu-release\v35-fork-<INDEX>\citu-wallet-v35-fork-<INDEX>.jar" -Algorithm SHA256
Get-FileHash "C:\citu-release\v35-fork-<INDEX>\citu-server-v35-fork-<INDEX>.jar" -Algorithm SHA256
Get-FileHash "C:\citu-release\v35-fork-<INDEX>\citu-website-v35-fork-<INDEX>.jar" -Algorithm SHA256
Get-FileHash "C:\citu-release\v35-fork-<INDEX>\citu-v35-fork-<INDEX>-source.zip" -Algorithm SHA256
```

Do not invent checksums. Only publish checksums generated from actual final files.

Recommended checksum file format:

```text
SHA256  citu-wallet-v35-rc1.jar  <REAL_HASH_PENDING>
SHA256  citu-server-v35-rc1.jar  <REAL_HASH_PENDING>
SHA256  citu-website-v35-rc1.jar  <REAL_HASH_PENDING>
SHA256  citu-v35-rc1-source.zip  <REAL_HASH_PENDING>
```

## 6. Version / Tag Naming Plan

Recommended tag sequence:

```text
v35-rc1
v35-rc2
v35-final-disabled
v35-activation-fork-<INDEX>
```

Meaning:

- `v35-rc1`
  - first release candidate;
  - V35 code present but disabled;
  - `V35_RESCUE_FORK_INDEX = Long.MAX_VALUE`;
  - used for exchange/node staging and validation.
- `v35-rc2`
  - only if RC fixes are needed;
  - still disabled unless explicitly scoped otherwise.
- `v35-final-disabled`
  - final pre-activation code with all V35 behavior present but disabled;
  - useful if the team wants a stable public build before fork index selection.
- `v35-activation-fork-<INDEX>`
  - final activation tag;
  - created only after real fork index is selected;
  - activation commit changes only wallet/server `Seting.java` fork index values.

Optional burn proof tag/report marker:

```text
v35-founder-burn-proof-<BURN_BLOCK_INDEX>
```

Use only after the real founder burn transaction confirms and proof values are published. Do not create this tag before the burn.

## 7. Git Release Flow

Safe release sequence:

1. Confirm clean git status.
2. Confirm no generated `target/` outputs are staged.
3. Confirm wallet/server `V35_RESCUE_FORK_INDEX = Long.MAX_VALUE`.
4. Confirm wallet/server `V35_BURN_ADDRESS = BURN_ADDRESS`.
5. Run wallet tests and package.
6. Run server tests and package.
7. Run website tests and package.
8. Execute live historical replay against copied live chain, or document it as a final gate if not yet available.
9. Confirm simulated fork dry-run and long-window difficulty dry-run reports are passing/current.
10. Create RC tag:

```text
v35-rc1
```

11. Build artifacts from the tagged commit.
12. Copy artifacts to release staging folder.
13. Generate SHA-256 checksums.
14. Create release notes.
15. Attach exchange technical notice.
16. Publish RC to exchanges/nodes for staging.
17. Wait for operator feedback.
18. If fixes are required, create `v35-rc2`.
19. Do not set real fork index until replay, burn timing, exchange readiness, and release owner go/no-go are complete.
20. Later, create final activation commit:
    - wallet `Seting.java`;
    - server `Seting.java`;
    - only `V35_RESCUE_FORK_INDEX`.
21. Re-run all tests/packages.
22. Build final activation artifacts.
23. Generate final checksums.
24. Tag:

```text
v35-activation-fork-<INDEX>
```

25. Publish final release and exchange notice.

## 8. Exchange Delivery Package

Exchange/node delivery package should include:

- wallet release artifact;
- server/storage release artifact;
- exact version/tag;
- checksum file;
- source archive or source tag link;
- fork index placeholder for RC, exact fork index for final activation release;
- estimated activation date/time placeholder for RC, exact estimate for final release;
- exchange technical notice;
- public V35 announcement;
- founder burn proof status link:
  - `/v35/founder-burn-proof`;
- rollback/emergency plan;
- chain verification instructions;
- deposit/withdrawal caution recommendation;
- support contact placeholder;
- emergency communication channel placeholder.

Exchange verification instructions:

- verify checksums;
- start upgraded node in staging;
- sync to current chain;
- confirm V35 reports disabled for RC while fork index remains `Long.MAX_VALUE`;
- confirm final activation release reports the published fork index;
- at fork, verify first V35 block reward:
  - `6 GOLD` miner;
  - `0 GOLD` founder;
- verify target PoW active;
- verify cumulativeWork sane;
- verify deposits/withdrawals according to exchange policy.

## 9. Release Notes Structure

Recommended release notes outline:

```text
# CITU V35 Release Notes

## Summary
- V35 protocol upgrade status.
- RC or final activation release.
- Whether V35 is active or still disabled.

## Consensus Changes
- Activation by block index.
- Transaction/block formats unchanged.

## Reward Changes
- 8 GOLD/block total.
- 6 GOLD miner reward.
- 2 GOLD jackpot reserve.
- 0 GOLD founder reward.

## PoW / Difficulty Changes
- 200-second target block time.
- Bitcoin-like hash <= target validation after V35.
- Difficulty adjustment and bounded emergency easing.

## cumulativeWork Chain Selection
- cumulativeWork metadata.
- post-V35 chain/candidate preference.

## Jackpot
- minerAddress-only tickets.
- sender addresses never participate.
- deterministic delayed future hash proof.
- canonical payout periods and amounts.

## Burn / Founder
- BURN_ADDRESS.
- founder reward removed after V35.
- founder burn status: planned / executed / proof link.
- actual vs modeled supply warning.

## APIs / Dashboard / Website
- /v35/protocol
- /v35/supply
- /v35/burn
- /v35/founder
- /v35/founder/burn-snapshot
- /v35/jackpot/*
- wallet /v35-dashboard
- website /v35 pages

## Upgrade Instructions
- who must upgrade.
- build/artifact names.
- checksum verification.
- fork index and estimated date/time.

## Exchange Instructions
- upgrade deadline.
- deposit/withdrawal caution.
- post-fork verification.

## Known Remaining Gates
- live historical replay status.
- founder burn proof status.
- exchange readiness status.

## Disclaimer
- protocol upgrade notice, not financial advice.
```

## 10. RC Vs Final Activation Release Explanation

### Release candidate

An RC may be published while:

- `V35_RESCUE_FORK_INDEX = Long.MAX_VALUE`;
- V35 code exists but remains disabled;
- founder burn may still be pending;
- real fork index is not selected.

Purpose:

- exchange/node staging;
- artifact verification;
- API/dashboard review;
- old-chain replay;
- public communication rehearsal.

RCs must not claim V35 is active.

### Final activation release

The final activation release is separate and later.

It must:

- set the real fork index in wallet/server `Seting.java`;
- keep all other protocol constants unchanged;
- run all tests/packages again;
- regenerate artifacts/checksums;
- publish exact fork index and estimated activation date/time;
- provide exchanges/nodes the agreed upgrade window.

The final activation release must be tagged with the fork index:

```text
v35-activation-fork-<INDEX>
```

## 11. RC Go / No-Go Checklist

Before publishing RC:

- [ ] Clean git status or documented intentional report-only changes.
- [ ] No `target/` build junk staged.
- [ ] Wallet `.\mvnw.cmd test` passes.
- [ ] Wallet `.\mvnw.cmd clean package` passes.
- [ ] Server `.\mvnw.cmd test` passes.
- [ ] Server `.\mvnw.cmd clean package` passes.
- [ ] Website `.\mvnw.cmd test` passes.
- [ ] Website `.\mvnw.cmd clean package` passes.
- [ ] V35 fork index remains `Long.MAX_VALUE`.
- [ ] `BURN_ADDRESS` remains configured.
- [ ] Real fork index not selected.
- [ ] Founder burn not claimed unless proof exists.
- [ ] Historical replay passed, or clearly listed as a final release gate.
- [ ] Long-window difficulty dry-run status included.
- [ ] Release notes complete.
- [ ] Exchange notice complete.
- [ ] Public pages render.
- [ ] Checksum file generated from actual artifacts.
- [ ] Support contact ready.
- [ ] Emergency communication channel ready.

Do not publish RC if artifacts and checksums do not match the intended tag.

## 12. Final Activation Go / No-Go Checklist

Before final activation release:

- [ ] Live historical replay against copied live chain passed.
- [ ] Exchange/node upgrade window selected.
- [ ] Current live height checked.
- [ ] Fork index calculated from current height plus selected block offset.
- [ ] Fork index reviewed by two humans.
- [ ] Founder burn status decided.
- [ ] Burn proof published if burn has already happened.
- [ ] Final `Seting.java` fork-index change isolated to wallet/server.
- [ ] Wallet/server fork index values match exactly.
- [ ] No reward, jackpot, burn, PoW, difficulty, block, or transaction format constants changed.
- [ ] Wallet tests/package pass after fork-index change.
- [ ] Server tests/package pass after fork-index change.
- [ ] Website tests/package pass after fork-index change.
- [ ] Final artifacts built from final activation commit.
- [ ] Final checksums generated.
- [ ] Final release notes include exact fork index.
- [ ] Exchange notice includes exact fork index and estimated activation date/time.
- [ ] Rollback/emergency plan ready.

Do not publish final activation release if:

- fork index is stale or mistyped;
- live replay failed;
- exchange readiness is unclear;
- artifact checksums mismatch;
- founder burn proof is inconsistent, if burn was performed before fork.

## 13. Remaining Risks

Highest remaining risks:

1. Real historical replay has not yet been executed against copied live-chain data.
2. Exchanges/nodes may not upgrade in time.
3. Artifact mismatch or checksum confusion can cause operational failures.
4. Generated `target/` outputs could be accidentally committed if not reviewed.
5. Founder burn timing remains unresolved and irreversible.
6. Final fork index typo could activate at the wrong height.
7. Publishing a wall-clock date without the block index could mislead operators.
8. RC artifacts may be mistaken for final activation artifacts unless clearly labeled disabled.
9. Existing Maven warnings may distract release verification logs.

## 14. Recommended Next Phase

Recommended next phase:

```text
Phase 9B - execute live historical replay once real copied chain path is available.
```

Alternative next phases:

```text
Phase 8C - release notes finalization and artifact checklist.
Phase 6F-2 - final activation commit checklist, no code changes.
```

Use Phase 8C if release notes and checklist polish are needed before a live chain copy is ready. Use Phase 6F-2 only after live replay evidence, burn timing, exchange readiness, and artifact/checksum planning are settled.

## 15. Confirmation No Code Changed

No source code, tests, wallet/server/website files, `pom.xml`, protocol constants, mining behavior, validation behavior, balance behavior, transaction format, block format, API semantics, or UI templates were changed in this phase.

Only this Markdown report was created:

- `CITU_V35_PHASE8B_RELEASE_ARTIFACT_CHECKSUM_TAG_PLAN.md`

V35 remains disabled. No real fork index was selected. No founder burn transaction was created. No jackpot payout transaction was created.
