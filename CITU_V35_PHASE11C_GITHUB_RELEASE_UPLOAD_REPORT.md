# CITU V35 Phase 11C GitHub Release Upload Report

## Executive summary

The approved public GitHub Releases for CITU V35 activation at block `514291` were created successfully. Final public version `1.0.0` node and wallet assets were uploaded from the reviewed local release folder only. Both releases were verified after upload.

No website deployment, source push, source-code change, burn transaction, jackpot payout, or commit was performed.

## GitHub account

- GitHub CLI account used: `CorporateFounder`
- Repository permission verified before upload: `ADMIN` for both repositories.

## Pre-upload verification

- Local release folder: `C:\citu\release-dryrun\v35-514291`
- Required local files found: `12/12`
- Files covered by `CITU_V35_514291_SHA256SUMS.txt`: `10`
- Checksum-chain errors: `0`
- Old `0.0.423` / `0.32.216` artifact references in local package: none.
- Existing node release before upload: absent.
- Existing wallet release before upload: absent.

## Node release

- Repository: `CorporateFounder/unitedStates_storage`
- Tag: `v35-node-activation-514291`
- Title: `CITU Node V35 activation 514291`
- URL: `https://github.com/CorporateFounder/unitedStates_storage/releases/tag/v35-node-activation-514291`
- Draft: `false`
- Prerelease: `false`

Uploaded node assets:

- `citu-node-v35-activation-514291-1.0.0.jar`
- `start-node-windows.bat`
- `start-node-linux.sh`
- `CITU_V35_514291_START_GUIDE_RU_EN.md`
- `CITU_V35_514291_RELEASE_NOTES_EN.md`
- `CITU_V35_514291_RELEASE_NOTES_RU.md`
- `CITU_V35_514291_RELEASE_MANIFEST.txt`
- `CITU_V35_514291_SHA256SUMS.txt`
- `CITU_V35_514291_SHA256SUMS.txt.sha256`

Post-upload verification: `9/9` assets present with state `uploaded`.

## Wallet release

- Repository: `CorporateFounder/unitedStates_final`
- Tag: `v35-wallet-activation-514291`
- Title: `CITU Wallet V35 activation 514291`
- URL: `https://github.com/CorporateFounder/unitedStates_final/releases/tag/v35-wallet-activation-514291`
- Draft: `false`
- Prerelease: `false`

Uploaded wallet assets:

- `citu-wallet-v35-activation-514291-1.0.0.jar`
- `start-wallet-windows.bat`
- `start-wallet-linux.sh`
- `CITU_V35_514291_START_GUIDE_RU_EN.md`
- `CITU_V35_514291_RELEASE_NOTES_EN.md`
- `CITU_V35_514291_RELEASE_NOTES_RU.md`
- `CITU_V35_514291_RELEASE_MANIFEST.txt`
- `CITU_V35_514291_SHA256SUMS.txt`
- `CITU_V35_514291_SHA256SUMS.txt.sha256`

Post-upload verification: `9/9` assets present with state `uploaded`.

## Public release wording

Uploaded notes and manifest state:

- V35 activation is scheduled at block `514291`.
- Before block `514291`, `v35Active=false` is expected.
- No founder burn has occurred.
- `BURN_ADDRESS` is an internal placeholder, not a public transfer destination.
- Do not send funds to `BURN_ADDRESS`.
- Verify SHA256 before running.

## Safety confirmation

- No `Seting.java` file changed.
- No source code changed.
- No burn transaction was created.
- No jackpot payout was created.
- No website deployment was performed.
- No source push was performed.
- No source commit was created.

## Recommendation

Proceed to Phase 11D website-link activation review. Update public website wording and links only after reviewing the newly created release URLs. Do not deploy until separately approved.
