# CITU V35 Phase 11C-0 GitHub Release Upload Readiness Review

## Executive summary

The previous rebuilt local V35 release package existed and its SHA256 chain was valid at the time of this review. That package is now superseded by the public `1.0.0` version normalization and must be rebuilt in Phase 11B-4 before upload. Phase 11C upload is also currently blocked: GitHub CLI (`gh`) is not installed or is not available in `PATH`, local Git remotes are not configured, and the connected GitHub account currently reports read-only repository permissions (`push=false`).

No GitHub Release, upload, tag, push, deployment, or commit was performed.

## Git state

- Current HEAD: `fd2df29 phase 11B2 rebuild final V35 release artifacts`
- Working tree before this report: clean.
- Root repository remotes: none configured.
- `unitedStates_storage-master` remotes: none configured.
- `unitedStates_final-master` remotes: none configured.

## Constants audit

| Project | `V35_RESCUE_FORK_INDEX` | `V35_BURN_ADDRESS` |
| --- | ---: | --- |
| Server | `514291` | `BURN_ADDRESS` |
| Wallet | `514291` | `BURN_ADDRESS` |

No `Seting.java` file was modified.

## Release folder verification

Verified folder:

`C:\citu\release-dryrun\v35-514291`

Required files found:

- `citu-node-v35-activation-514291-1.0.0.jar` (required after Phase 11B-4 rebuild)
- `citu-wallet-v35-activation-514291-1.0.0.jar` (required after Phase 11B-4 rebuild)
- `start-node-windows.bat`
- `start-wallet-windows.bat`
- `start-node-linux.sh`
- `start-wallet-linux.sh`
- `CITU_V35_514291_START_GUIDE_RU_EN.md`
- `CITU_V35_514291_RELEASE_NOTES_RU.md`
- `CITU_V35_514291_RELEASE_NOTES_EN.md`
- `CITU_V35_514291_RELEASE_MANIFEST.txt`
- `CITU_V35_514291_SHA256SUMS.txt`
- `CITU_V35_514291_SHA256SUMS.txt.sha256`

## Checksum verification

- Files covered by `CITU_V35_514291_SHA256SUMS.txt`: `10`
- `CITU_V35_514291_SHA256SUMS.txt.sha256` verified against the checksum list.
- Missing files: `0`
- SHA256 mismatches: `0`

## GitHub CLI status

`gh --version` and `gh auth status` could not run because `gh` is not recognized as an installed command.

Upload blocker:

- Install GitHub CLI or make it available in `PATH`.
- Authenticate using an account or token with release/tag write permission for both target repositories.
- Re-run `gh auth status` before any upload.

## Target repository review

The connected GitHub service confirmed that both public repositories exist:

| Repository | Default branch | Connected permission |
| --- | --- | --- |
| `CorporateFounder/unitedStates_storage` | `master` | `pull=true`, `push=false` |
| `CorporateFounder/unitedStates_final` | `master` | `pull=true`, `push=false` |

The repositories can host releases in principle, but the currently connected account cannot be treated as release-write ready. A write-authorized `gh` login must be confirmed before Phase 11C.

## Proposed server release command

Prepare only. **DO NOT RUN before explicit Phase 11C approval.**

```powershell
cd C:\citu\release-dryrun\v35-514291
gh release create v35-node-activation-514291 `
  --repo CorporateFounder/unitedStates_storage `
  --title "CITU Node V35 activation 514291" `
  --notes-file CITU_V35_514291_RELEASE_NOTES_EN.md `
  citu-node-v35-activation-514291-1.0.0.jar `
  start-node-windows.bat `
  start-node-linux.sh `
  CITU_V35_514291_START_GUIDE_RU_EN.md `
  CITU_V35_514291_RELEASE_NOTES_EN.md `
  CITU_V35_514291_RELEASE_NOTES_RU.md `
  CITU_V35_514291_RELEASE_MANIFEST.txt `
  CITU_V35_514291_SHA256SUMS.txt `
  CITU_V35_514291_SHA256SUMS.txt.sha256
```

## Proposed wallet release command

Prepare only. **DO NOT RUN before explicit Phase 11C approval.**

```powershell
cd C:\citu\release-dryrun\v35-514291
gh release create v35-wallet-activation-514291 `
  --repo CorporateFounder/unitedStates_final `
  --title "CITU Wallet V35 activation 514291" `
  --notes-file CITU_V35_514291_RELEASE_NOTES_EN.md `
  citu-wallet-v35-activation-514291-1.0.0.jar `
  start-wallet-windows.bat `
  start-wallet-linux.sh `
  CITU_V35_514291_START_GUIDE_RU_EN.md `
  CITU_V35_514291_RELEASE_NOTES_EN.md `
  CITU_V35_514291_RELEASE_NOTES_RU.md `
  CITU_V35_514291_RELEASE_MANIFEST.txt `
  CITU_V35_514291_SHA256SUMS.txt `
  CITU_V35_514291_SHA256SUMS.txt.sha256
```

## Safety confirmation

- No file was uploaded.
- No GitHub Release was created.
- No tag was created.
- No push was performed.
- No deployment was performed.
- No source code changed.
- No `Seting.java` file changed.
- No burn transaction was created.
- No jackpot payout was created.
- No commit was created.

## Required next step

Resolve GitHub write-access readiness first:

1. Install or expose `gh` in `PATH`.
2. Authenticate `gh` with release/tag write access for both repositories.
3. Verify `gh auth status`.
4. Re-run a report-only readiness check.
5. Proceed to Phase 11C upload only after explicit approval.
