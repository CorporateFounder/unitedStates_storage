# CITU V35 Phase 11B-2 Rebuilt Final Release Artifacts Report

## Executive summary

The local V35 release candidate package was rebuilt after the Phase 11B-1 wallet and website wording fix. This phase performed local build, packaging, checksum generation, and offline/local smoke verification only. No upload, GitHub Release, tag, deployment, push, or commit was performed.

## Source revisions

- Activation commit: `c755c153c1cd933c6c8a11ce09080578f35d5b98`
- Build source HEAD: `4555c4631fd1868ba16ed83edbe4d5918c26dff4`
- Build source summary: `4555c46 phase 11B1 fix post activation UI docs status wording`

## Constants audit

| Project | `V35_RESCUE_FORK_INDEX` | `V35_BURN_ADDRESS` |
| --- | ---: | --- |
| Server | `514291` | `BURN_ADDRESS` |
| Wallet | `514291` | `BURN_ADDRESS` |

No `Seting.java` file was modified in this phase.

## Build result

| Project | Result | Tests |
| --- | --- | ---: |
| Server | `BUILD SUCCESS` | `231/231 PASS` |
| Wallet | `BUILD SUCCESS` | `200/200 PASS` |

## Local artifact folder

`C:\citu\release-dryrun\v35-514291`

The folder is excluded from Git tracking through `.git/info/exclude`. It is a local release candidate package and must not be uploaded before Phase 11C approval.

## Artifact list

- `citu-node-v35-activation-514291-0.0.423.jar`
- `citu-wallet-v35-activation-514291-0.32.216.jar`
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

## Checksum result

- `CITU_V35_514291_SHA256SUMS.txt` covers the ten release files other than the checksum files.
- `CITU_V35_514291_SHA256SUMS.txt.sha256` covers `CITU_V35_514291_SHA256SUMS.txt`.
- Files verified: `10`
- SHA256 mismatches: `0`
- `SHA256SUMS.txt` SHA256: `e1f61b223a074fe02e076f903bce09b916ff05361cb1356804641f8fb0b6c0f3`

## Required release wording

The package states:

- V35 activation is scheduled at block `514291`.
- Before block `514291`, `v35Active=false` is expected.
- GitHub Release upload is pending until Phase 11C.
- No founder burn has occurred.
- `BURN_ADDRESS` is an internal placeholder, not a public transfer destination.
- Do not send funds to `BURN_ADDRESS`.
- Verify SHA256 before running.

## Local smoke result

Freshly rebuilt JAR files were started locally and stopped after verification.

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
- No burn transaction was created.
- No jackpot payout was created.
- No deployment was performed.
- No GitHub Release was created.
- No file was uploaded.
- No tag was created.
- No push was performed.
- No commit was created.

## Recommendation

Proceed to Phase 11C GitHub Release upload review only after an explicit approval. Before upload, review the artifact list and verify the published SHA256 values against this local package.
