# CITU V35 Phase 11B-4 Final 1.0.0 Release Artifacts Report

## Executive summary

The local V35 release candidate package was rebuilt from committed public version `1.0.0` sources. Stale `0.0.423` and `0.32.216` package files were removed. Fresh node and wallet JAR files, launch scripts, notes, manifest, and checksum chain were created and verified locally.

No upload, GitHub Release, tag, push, deployment, burn transaction, jackpot payout, or commit was performed.

## Source revisions

- Current HEAD: `cc8dd7ca5fc25c512267c1f67a537d0c441948dd`
- Current HEAD summary: `cc8dd7c phase 11B3 bump public V35 release versions to 1.0.0`
- Activation commit: `c755c153c1cd933c6c8a11ce09080578f35d5b98`

## Maven versions

| Project | Maven version |
| --- | --- |
| Server/node | `1.0.0` |
| Wallet | `1.0.0` |

## Constants audit

| Project | `V35_RESCUE_FORK_INDEX` | `V35_BURN_ADDRESS` |
| --- | ---: | --- |
| Server | `514291` | `BURN_ADDRESS` |
| Wallet | `514291` | `BURN_ADDRESS` |

No `Seting.java` file was modified.

## Local artifact folder

`C:\citu\release-dryrun\v35-514291`

The folder remains excluded from Git tracking. It is ready for a separate upload review, not uploaded or published.

## Exact artifact list

- `citu-node-v35-activation-514291-1.0.0.jar`
- `citu-wallet-v35-activation-514291-1.0.0.jar`
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

## Old artifact removal

- `0.0.423` references in release folder: none.
- `0.32.216` references in release folder: none.
- Old JAR files were removed before creating this package.

## Build result

| Project | Result | Tests |
| --- | --- | ---: |
| Server/node | `BUILD SUCCESS` | `231/231 PASS` |
| Wallet | `BUILD SUCCESS` | `200/200 PASS` |

## Checksum result

- `CITU_V35_514291_SHA256SUMS.txt` covers the ten release files other than the checksum files.
- `CITU_V35_514291_SHA256SUMS.txt.sha256` covers the checksum list.
- Files hashed: `10`
- SHA256 mismatches: `0`
- `SHA256SUMS.txt` SHA256: `902d83f53ba8c4286dc89bb6f69b01d6078b6795cbf2ec6ace258e70eba9a116`

## Required release wording

The local package states:

- V35 activation is scheduled at block `514291`.
- Before block `514291`, `v35Active=false` is expected.
- GitHub Release upload is pending until Phase 11C.
- No founder burn has occurred.
- `BURN_ADDRESS` is an internal placeholder, not a public transfer destination.
- Do not send funds to `BURN_ADDRESS`.
- Verify SHA256 before running.

## Local smoke result

Fresh `1.0.0` JAR files were started locally and stopped after verification.

| URL | Result |
| --- | --- |
| `http://127.0.0.1:8093/size` | HTTP `200` |
| `http://127.0.0.1:8093/v35/protocol` | HTTP `200` |
| `http://127.0.0.1:8093/v35/burn` | HTTP `200` |
| `http://127.0.0.1:8093/v35/jackpot/status` | HTTP `200` |
| `http://127.0.0.1:8092/` | HTTP `200` |
| `http://127.0.0.1:8092/v35-dashboard` | HTTP `200` |

Wallet dashboard checks:

- Fork height `514291`: present.
- `ACTIVATION SCHEDULED`: present.
- Stale `Long.MAX_VALUE` wording: absent.
- Premature `V35 active now: true` claim: absent.

The wallet home page performed its normal read-only status requests while running locally. No transaction action was performed.

## Safety confirmation

- No `Seting.java` file changed.
- `V35_RESCUE_FORK_INDEX` remains `514291`.
- `V35_BURN_ADDRESS` remains `BURN_ADDRESS`.
- No consensus logic changed.
- No burn transaction was created.
- No jackpot payout was created.
- No deployment was performed.
- No GitHub Release was created.
- No file was uploaded.
- No tag was created.
- No push was performed.
- No commit was created.

## Recommendation

The local `1.0.0` release package is ready for a renewed Phase 11C upload-readiness review after GitHub CLI authentication and write permissions are configured. Do not upload before explicit approval.
