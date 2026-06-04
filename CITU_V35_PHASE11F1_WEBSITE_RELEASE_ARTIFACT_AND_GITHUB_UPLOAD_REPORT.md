# CITU V35 Phase 11F-1 Website Release Artifact and GitHub Upload Report

## Executive summary

Phase 11F-1 built the official website deployment jar, copied it to the release folder with the final V35 1.0.0 artifact name, generated a website-specific SHA256 file, smoke-tested the final named jar locally, confirmed the correct website GitHub repository, and created the website-only GitHub Release.

No VPS deployment was performed. No official website or node process was restarted. No source code was pushed or committed.

## Source HEAD

Pre-flight source status:

- Local working tree was clean.
- Latest HEAD: `a1926b6 phase 11F0 audit official deployment readiness`
- `Seting.java` was not present in the latest commit file list.
- No `Seting.java` diff was present.

Note:

- The prompt listed `fffcedb` as current HEAD, but the local repository had advanced to `a1926b6` with the Phase 11F-0 readiness report commit.

## Website build and test result

Website source:

- `C:\citu\website.citu-web-master`

Commands run:

- `.\mvnw.cmd test`
- `.\mvnw.cmd clean package`

Results:

- Website tests passed: `13/13`
- Website clean package passed.
- Generated jar:
  - `C:\citu\website.citu-web-master\target\website.citu-0.0.24-SNAPSHOT.jar`

Build notes:

- Maven reported the existing duplicate `bcprov-jdk15on` dependency warning.
- The warning did not block tests or packaging.

## Final website artifact

Final artifact name:

- `citu-website-v35-activation-514291-1.0.0.jar`

Final artifact path:

- `C:\citu\release-dryrun\v35-514291\citu-website-v35-activation-514291-1.0.0.jar`

Artifact size:

- `36,505,482` bytes

Website SHA256 file:

- `C:\citu\release-dryrun\v35-514291\CITU_V35_514291_WEBSITE_SHA256SUMS.txt`

Website artifact SHA256:

- `7685b4354b74e50f549588e6333a55fbe30c3736ea3e3f056ac7e296d2792003`

## Local smoke result

The final named website jar was started temporarily on:

- `http://127.0.0.1:8085`

Smoke routes checked:

- `/`: HTTP 200
- `/v35`: HTTP 200
- `/wallet_and_node_url`: HTTP 200
- `/how_to_install`: HTTP 200
- `/how_to_install_server`: HTTP 200
- `/how_to_open_an_account`: HTTP 200
- `/how_mining_pool`: HTTP 200
- `/v35/checksums`: HTTP 200
- `/white_papper`: HTTP 200

Temporary local smoke process was stopped after verification.

## Website GitHub repository discovery

Local remotes:

- `C:\citu`: no remote output.
- `C:\citu\website.citu-web-master`: no remote output.

CorporateFounder repositories inspected:

- `CorporateFounder/website.citu-web`
- `CorporateFounder/website.citu`

Discovery result:

- Correct website repo identified as `CorporateFounder/website.citu-web`.

Reasons:

- Local folder name is `website.citu-web-master`.
- `CorporateFounder/website.citu-web` exists and viewer permission is `ADMIN`.
- Its `pom.xml` matches the local website project identity:
  - `artifactId = website.citu`
  - `version = 0.0.24-SNAPSHOT`
  - Spring Boot `3.1.2`
- `CorporateFounder/website.citu` appears older and uses `version = 0.0.1-SNAPSHOT`.

## GitHub website release

Website GitHub Release was created in:

- `CorporateFounder/website.citu-web`

Tag:

- `v35-website-activation-514291`

Title:

- `CITU Website V35 activation 514291`

Release URL:

- `https://github.com/CorporateFounder/website.citu-web/releases/tag/v35-website-activation-514291`

Uploaded assets:

- `citu-website-v35-activation-514291-1.0.0.jar`
- `CITU_V35_514291_WEBSITE_SHA256SUMS.txt`

Post-upload verification:

- Release exists.
- Website jar asset exists.
- Website SHA256 file exists.
- GitHub-reported jar digest:
  - `sha256:7685b4354b74e50f549588e6333a55fbe30c3736ea3e3f056ac7e296d2792003`

## Deployment status

No VPS deployment occurred.

The official VPS website is still expected to be running the old jar from the Phase 11F-0 inventory:

- `C:\website.citu-0.0.30-SNAPSHOT.jar`

The website release artifact is ready for a later website-only VPS deployment phase.

## Safety confirmation

Confirmed:

- No `Seting.java` change.
- No `V35_RESCUE_FORK_INDEX` change.
- No `V35_BURN_ADDRESS` change.
- No consensus logic change.
- No VPS deploy.
- No website restart.
- No node restart.
- No process killed except the temporary local smoke process started in this phase.
- No `C:\resources` touch.
- No burn transaction.
- No jackpot payout.
- No source push.
- No source commit.
- No `git add`.
- No upload to node or wallet release repositories.

## Recommended next phase

Proceed to Phase 11F-2: website-only VPS deployment dry-run command plan and backup checklist, or Phase 11F-2 website-only VPS deployment if explicitly approved.

Do not restart the public node until the separate Phase 11G controlled node restart plan with full `C:\resources` backup is approved.
