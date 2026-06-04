# CITU V35 Phase 11E Rebuild and Refresh GitHub Release Assets Report

## Executive summary

Phase 11E rebuilt the final V35 1.0.0 node and wallet artifacts from current HEAD `1bcc659` after the Phase 11D/11D-1 wallet and website UI cleanup. The existing GitHub Releases were not recreated and no new tags were created. Existing release assets were refreshed in place using the same approved asset names and `gh release upload --clobber`.

No source code was changed. No `Seting.java` file was changed. No burn transaction, jackpot payout, deployment, official server restart, source push, new tag, new release, or commit was performed.

## HEAD used

- Current HEAD: `1bcc659 phase 11D update release links and clean wallet website UI`
- Pre-flight HEAD check confirmed no `Seting.java` file in the HEAD file list.
- Working tree was cleaned before the release rebuild. Generated build outputs were restored after completion; final `git status --short` was clean.

## Constants audit

Confirmed source constants:

- Server `V35_RESCUE_FORK_INDEX = 514291`
- Server `V35_BURN_ADDRESS = "BURN_ADDRESS"`
- Wallet `V35_RESCUE_FORK_INDEX = 514291`
- Wallet `V35_BURN_ADDRESS = "BURN_ADDRESS"`

## Build and test results

Server/node:

- First `.\mvnw.cmd test` attempt failed before running tests due to JVM native memory allocation failure while leftover local smoke Java processes were still running.
- Temporary smoke Java processes were stopped.
- Retry passed:
  - `.\mvnw.cmd test`: PASS, `231` tests, `0` failures, `0` errors.
  - `.\mvnw.cmd clean package`: PASS.

Wallet:

- `.\mvnw.cmd test`: PASS, `203` tests, `0` failures, `0` errors.
- `.\mvnw.cmd clean package`: PASS.

Website:

- `.\mvnw.cmd test`: PASS, `13` tests, `0` failures, `0` errors.
- `.\mvnw.cmd clean package`: PASS.

Notes:

- Maven duplicate dependency warnings remain pre-existing and did not block builds.
- No source changes were made to address warnings.

## Release folder

Release folder recreated:

- `C:\citu\release-dryrun\v35-514291`

Final local release files:

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

Old `0.0.423` and `0.32.216` artifact names were not used.

## SHA256 result

Checksum verification passed for every release file and for `CITU_V35_514291_SHA256SUMS.txt.sha256`.

Key refreshed jar hashes:

- Node jar: `c692264b34b1a32e54f7a7acac4b518ae69fc561fc51de7cef9c2f67b803d501`
- Wallet jar: `7503ec85a903f7a5d5994804c8ac29880e1a54580aa612bd5c6bb69b69d07a52`
- SHA256SUMS file: `e1c311beb72f4fdf01541e004709d64d57127997345015111992602d6789b69b`

## Local smoke result

Temporary local node:

- Port: `8093`
- `/size`: HTTP 200
- `/v35/protocol`: HTTP 200
- `/v35/burn`: HTTP 200
- `/v35/jackpot/status`: HTTP 200

Temporary local wallet:

- Port: `8087`
- `/`: HTTP 200
- `/create-account`: HTTP 200
- `/statistics`: HTTP 200
- `/current-laws`: HTTP 200
- `/transactions`: HTTP 200
- `/mining`: HTTP 200
- `/v35-dashboard`: HTTP 200

Temporary local website:

- Port: `8085`
- `/`: HTTP 200
- `/v35`: HTTP 200
- `/wallet_and_node_url`: HTTP 200
- `/how_to_install`: HTTP 200
- `/how_to_install_server`: HTTP 200
- `/how_to_open_an_account`: HTTP 200
- `/how_mining_pool`: HTTP 200

Temporary smoke processes were stopped after checks.

## GitHub release refresh

GitHub account:

- `CorporateFounder`

Existing releases verified before upload:

- Node: `https://github.com/CorporateFounder/unitedStates_storage/releases/tag/v35-node-activation-514291`
- Wallet: `https://github.com/CorporateFounder/unitedStates_final/releases/tag/v35-wallet-activation-514291`

Assets refreshed with `--clobber`; no new GitHub Release and no new tag were created.

Node release refreshed assets, 9/9:

- `citu-node-v35-activation-514291-1.0.0.jar`
- `start-node-windows.bat`
- `start-node-linux.sh`
- `CITU_V35_514291_START_GUIDE_RU_EN.md`
- `CITU_V35_514291_RELEASE_NOTES_EN.md`
- `CITU_V35_514291_RELEASE_NOTES_RU.md`
- `CITU_V35_514291_RELEASE_MANIFEST.txt`
- `CITU_V35_514291_SHA256SUMS.txt`
- `CITU_V35_514291_SHA256SUMS.txt.sha256`

Wallet release refreshed assets, 9/9:

- `citu-wallet-v35-activation-514291-1.0.0.jar`
- `start-wallet-windows.bat`
- `start-wallet-linux.sh`
- `CITU_V35_514291_START_GUIDE_RU_EN.md`
- `CITU_V35_514291_RELEASE_NOTES_EN.md`
- `CITU_V35_514291_RELEASE_NOTES_RU.md`
- `CITU_V35_514291_RELEASE_MANIFEST.txt`
- `CITU_V35_514291_SHA256SUMS.txt`
- `CITU_V35_514291_SHA256SUMS.txt.sha256`

Post-upload verification confirmed:

- Node release has 9 assets.
- Wallet release has 9 assets.
- Refreshed node jar digest on GitHub: `sha256:c692264b34b1a32e54f7a7acac4b518ae69fc561fc51de7cef9c2f67b803d501`
- Refreshed wallet jar digest on GitHub: `sha256:7503ec85a903f7a5d5994804c8ac29880e1a54580aa612bd5c6bb69b69d07a52`

## Safety confirmation

Confirmed:

- No `Seting.java` change.
- No consensus logic change.
- No burn transaction.
- No jackpot payout.
- No deployment.
- No official server restart.
- No source push.
- No new tag.
- No new GitHub Release.
- No commit.
- No old `0.0.423` or `0.32.216` artifact upload.

## Recommended next step

Proceed to a post-refresh public verification phase: verify the two GitHub Release download URLs from a browser or clean download location, verify SHA256 from the published checksum file, then update any public website wording if it needs to mention that release assets were refreshed after the 11D UI cleanup.
