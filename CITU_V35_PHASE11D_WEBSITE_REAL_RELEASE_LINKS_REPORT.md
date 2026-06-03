# CITU V35 Phase 11D Website Real Release Links Report

## 1. Executive summary

Phase 11D updated the public website to use the published V35 1.0.0 GitHub Releases and direct JAR download links. The website now states that V35 activation is scheduled at block `514291`, remains inactive before that block, and expects `v35Active=false` until the fork height is reached.

No deployment, official server restart, commit, push, burn transaction, or jackpot payout was performed.

## 2. Published release links

- Node GitHub Release: `https://github.com/CorporateFounder/unitedStates_storage/releases/tag/v35-node-activation-514291`
- Wallet GitHub Release: `https://github.com/CorporateFounder/unitedStates_final/releases/tag/v35-wallet-activation-514291`
- Node JAR: `https://github.com/CorporateFounder/unitedStates_storage/releases/download/v35-node-activation-514291/citu-node-v35-activation-514291-1.0.0.jar`
- Wallet JAR: `https://github.com/CorporateFounder/unitedStates_final/releases/download/v35-wallet-activation-514291/citu-wallet-v35-activation-514291-1.0.0.jar`
- SHA256 checksums: `https://github.com/CorporateFounder/unitedStates_storage/releases/download/v35-node-activation-514291/CITU_V35_514291_SHA256SUMS.txt`
- Start guide: `https://github.com/CorporateFounder/unitedStates_storage/releases/download/v35-node-activation-514291/CITU_V35_514291_START_GUIDE_RU_EN.md`

## 3. Website pages updated

- `/`
- `/v35`
- `/wallet_and_node_url`
- `/v35/checksums`
- `/v35/preactivation-status`
- `/white_papper`
- `/v35/burn-warning`
- `/v35/jackpot`

The shared footer, `/v35/operator-guide`, and `/charter` were also updated so public navigation does not retain stale pending-release wording.

## 4. Files changed

- `website.citu-web-master/src/main/resources/templates/blocks/v35-release-links.html`
- `website.citu-web-master/src/main/resources/templates/blocks/footer.html`
- `website.citu-web-master/src/main/resources/templates/main.html`
- `website.citu-web-master/src/main/resources/templates/v35.html`
- `website.citu-web-master/src/main/resources/templates/wallet_and_node_url.html`
- `website.citu-web-master/src/main/resources/templates/v35-checksums.html`
- `website.citu-web-master/src/main/resources/templates/v35-preactivation-status.html`
- `website.citu-web-master/src/main/resources/templates/white_papper.html`
- `website.citu-web-master/src/main/resources/templates/v35-burn-warning.html`
- `website.citu-web-master/src/main/resources/templates/v35-jackpot.html`
- `website.citu-web-master/src/main/resources/templates/v35-operator-guide.html`
- `website.citu-web-master/src/main/resources/templates/charter.html`
- `website.citu-web-master/src/test/java/com/example/website/citu/controller/V35PublicControllerTest.java`

## 5. Public wording safety

- Official V35 1.0.0 release publication is stated clearly.
- Activation is scheduled at block `514291`.
- Before block `514291`, `v35Active=false` is explicitly expected.
- No founder burn has occurred.
- `BURN_ADDRESS` remains an internal placeholder, not a public transfer destination.
- Users are instructed to verify SHA256 before running downloaded JARs.
- Stale current-release wording such as pending upload and future release links was removed.

## 6. Tests and package

Executed from `C:\citu\website.citu-web-master`:

```powershell
.\mvnw.cmd test
.\mvnw.cmd clean package
```

Results:

- Website tests: `12/12 PASS`
- Website clean package: `PASS`
- Existing Maven warning remains: duplicate `org.bouncycastle:bcprov-jdk15on:1.70` dependency declaration in the website `pom.xml`. It did not block Phase 11D.

## 7. Local smoke result

A temporary local website process was started on `127.0.0.1:8086`, verified, and stopped. The existing process on `127.0.0.1:8085` and the official server were not restarted.

| Route | HTTP | Rendered official buttons | Safety wording |
| --- | ---: | ---: | --- |
| `/` | 200 | 6 | PASS |
| `/v35` | 200 | 6 | PASS |
| `/wallet_and_node_url` | 200 | 6 | PASS |
| `/v35/checksums` | 200 | 6 | PASS |
| `/v35/preactivation-status` | 200 | 6 | PASS |
| `/v35/burn-warning` | 200 | 6 | PASS |
| `/v35/jackpot` | 200 | 6 | PASS |
| `/white_papper` | 200 | 6 | PASS |

The six shared actions are:

- Download Node JAR
- Download Wallet JAR
- Node GitHub Release
- Wallet GitHub Release
- SHA256 checksums
- Start guide

## 8. Constants and safety confirmation

- Server `V35_RESCUE_FORK_INDEX` remains `514291`.
- Wallet `V35_RESCUE_FORK_INDEX` remains `514291`.
- Server and wallet `V35_BURN_ADDRESS` remain `BURN_ADDRESS`.
- No `Seting.java` file changed.
- No activation constant changed.
- No burn transaction was created.
- No jackpot payout was created.
- No deployment occurred.
- No official server restart occurred.
- No push occurred.
- No commit occurred.

## 9. Recommended next phase

Proceed to a separate website deployment approval phase. Before deployment, review this website-only diff and decide whether to commit it as a dedicated website release-link update.
