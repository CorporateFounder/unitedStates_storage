# CITU V35 Phase 8C Artifact Checklist

## Purpose

This checklist defines the required V35 pre-activation release package artifacts and the commands to run later.

No artifacts were built in Phase 8C. No tests, packages, historical replay, fork activation, burn transaction, or jackpot payout transaction were executed in this phase.

## Required artifacts

The V35 release package should include:

- Server source archive.
- Wallet source archive.
- Server build artifact.
- Wallet build artifact.
- Website build artifact if public website deployment is included.
- SHA-256 checksum file.
- Final release notes:
  - `CITU_V35_PHASE8C_RELEASE_NOTES_FINAL.md`
- Historical replay sign-off:
  - `CITU_V35_PHASE9C_HISTORICAL_REPLAY_SIGNOFF_RELEASE_GATE_REPORT.md`
- Replay-only safety audit:
  - `CITU_V35_PHASE9E_FINAL_PRE_ACTIVATION_SAFETY_AUDIT_REPORT.md`
- Replay-only guard test report:
  - `CITU_V35_PHASE9D_REPLAY_ONLY_GUARD_TESTS_REPORT.md`
- Founder burn execution checklist:
  - `CITU_V35_PHASE7E_FINAL_BURN_EXECUTION_CHECKLIST.md`
- Exchange/public notice:
  - `CITU_V35_PHASE8A_PUBLIC_RELEASE_AND_EXCHANGE_NOTICE_DRAFTS.md`
- Artifact/checksum/tag plan:
  - `CITU_V35_PHASE8B_RELEASE_ARTIFACT_CHECKSUM_TAG_PLAN.md`

## Required commands to run later

Do not run these commands as part of Phase 8C. They are documented for the later release build phase.

### Server clean package

```powershell
cd C:\citu\unitedStates_storage-master
.\mvnw.cmd clean package
```

### Wallet clean package

```powershell
cd C:\citu\unitedStates_final-master
.\mvnw.cmd clean package
```

### Website clean package, if included

```powershell
cd C:\citu\website.citu-web-master
.\mvnw.cmd clean package
```

### Checksum generation

Use SHA-256 for every released artifact.

PowerShell examples:

```powershell
Get-FileHash "C:\citu-release\v35-rc1\citu-server-v35-rc1.jar" -Algorithm SHA256
Get-FileHash "C:\citu-release\v35-rc1\citu-wallet-v35-rc1.jar" -Algorithm SHA256
Get-FileHash "C:\citu-release\v35-rc1\citu-website-v35-rc1.jar" -Algorithm SHA256
Get-FileHash "C:\citu-release\v35-rc1\citu-v35-rc1-source.zip" -Algorithm SHA256
```

Do not invent checksums. Publish only hashes generated from final artifact files.

### Git tag creation

Pre-activation tag placeholder:

```powershell
git tag v35-final-disabled
```

Final activation tag placeholder, only after real fork index is set:

```powershell
git tag v35-activation-fork-<INDEX>
```

Do not create `<INDEX>` tags until the real fork index is selected and committed.

## Git tag plan

Recommended tag sequence:

- `v35-rc1`
  - first release candidate;
  - V35 code present but disabled;
  - fork index remains `Long.MAX_VALUE`.
- `v35-rc2`
  - only if RC fixes are required.
- `v35-final-disabled`
  - final pre-activation release with V35 still disabled.
- `v35-activation-fork-<INDEX>`
  - final activation release after wallet/server constants are set to the real fork index.

Rules:

- RC and pre-activation tags must not claim V35 is active.
- Final activation tag must include the real fork index.
- Final activation commit should change only wallet/server `V35_RESCUE_FORK_INDEX`, unless a separately approved release fix is required.

## Checksum plan

Required checksum file:

- `citu-v35-<TAG>-checksums-sha256.txt`

Recommended format:

```text
SHA256  citu-server-v35-<TAG>.jar  <REAL_HASH>
SHA256  citu-wallet-v35-<TAG>.jar  <REAL_HASH>
SHA256  citu-website-v35-<TAG>.jar  <REAL_HASH>
SHA256  citu-v35-<TAG>-source.zip  <REAL_HASH>
SHA256  CITU_V35_PHASE8C_RELEASE_NOTES_FINAL.md  <REAL_HASH_IF_INCLUDED>
```

Rules:

- Generate checksums after artifacts are built and copied to the release staging folder.
- Do not edit artifact files after checksum generation.
- If any artifact changes, regenerate the checksum file.
- Exchanges should verify checksums before running artifacts.

## Release gate table

| Gate | Status |
| --- | --- |
| Historical replay gate | PASS |
| Wallet mirror gate | PASS |
| Replay-only safety gate | PASS |
| Release notes gate | PASS after Phase 8C |
| Artifact build gate | NOT YET |
| Checksum gate | NOT YET |
| Exchange delivery gate | NOT YET |
| V35 activation gate | NOT YET |
| Founder burn gate | NOT YET |
| Final release gate | NOT YET |

## Pre-activation artifact checklist

- [ ] Confirm clean git status.
- [ ] Confirm no `target/` artifacts are staged.
- [ ] Confirm server `V35_RESCUE_FORK_INDEX = Long.MAX_VALUE`.
- [ ] Confirm wallet `V35_RESCUE_FORK_INDEX = Long.MAX_VALUE`.
- [ ] Confirm server/wallet `V35_BURN_ADDRESS = BURN_ADDRESS`.
- [ ] Confirm no real fork index selected.
- [ ] Confirm founder burn not claimed unless real proof exists.
- [ ] Run server clean package.
- [ ] Run wallet clean package.
- [ ] Run website clean package if website artifact is included.
- [ ] Copy artifacts to release staging folder.
- [ ] Copy source archive to release staging folder.
- [ ] Copy release notes and gate reports to release staging folder.
- [ ] Generate SHA-256 checksums.
- [ ] Verify checksums from staging folder.
- [ ] Create pre-activation tag.
- [ ] Publish exchange/public notice with disabled/pre-activation wording.

## Final activation artifact checklist

Do not start this checklist until final activation is approved.

- [ ] Real live height checked.
- [ ] Upgrade window selected.
- [ ] Real fork index calculated.
- [ ] Real fork index reviewed by two humans.
- [ ] Wallet/server `V35_RESCUE_FORK_INDEX` changed to the exact same value.
- [ ] No other constants changed.
- [ ] Server tests and clean package pass.
- [ ] Wallet tests and clean package pass.
- [ ] Website tests and clean package pass if website is included.
- [ ] Final artifacts built from final activation commit.
- [ ] Final checksums generated.
- [ ] Final activation tag created with fork index.
- [ ] Exchange notice updated with exact fork index and estimated activation time.
- [ ] Post-fork monitoring checklist ready.

## Remaining risks

- Artifact build gate is still open.
- Checksum gate is still open.
- Exchange delivery gate is still open.
- Real fork index is not selected.
- Founder burn has not happened.
- Founder burn is irreversible and must remain separate from fork-index activation.
- Final activation commit may introduce risk if wallet/server fork indexes do not match.
- Exchanges and node operators need a clear upgrade window and verified artifact checksums.
- Post-fork rollback remains dangerous and may split the chain.

## Recommended next phase

Recommended next phase:

- Phase 6F-2 - final activation commit checklist, no code changes

Alternative:

- Phase 8D - release artifact staging checklist execution plan, no artifact build yet

Do not proceed to artifact build or final activation until the release owner explicitly approves the next operational gate.
