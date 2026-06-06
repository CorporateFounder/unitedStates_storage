# CITU V35 Phase 11G-8 Website Legacy API Contract Restore Report

## Root Cause

The website explorer had drifted toward emergency fallback behavior and was still doing an initial `/size` lookup before loading blocks. The old node API contract itself was still working, but the website did not reliably use the bounded legacy endpoints in the safest way for page rendering.

The key issue was website-side compatibility: `/conductor` could show fallback before a user action, block range rendering depended on `/size`, and old winner compatibility routes were not exposed as direct website routes.

## Exact Old API Endpoints Restored

- `GET /conductorBlock?index=<index>` for block lookup by index.
- `POST /sub-blocks` with JSON body `{"start":0,"finish":10}` for bounded block ranges.
- `GET /senderTransactions?address=<pubkey>&from=<from>&to=<to>` for sender history.
- `GET /customerTransactions?address=<pubkey>&from=<from>&to=<to>` for customer history.
- `GET /senderCountDto?address=<pubkey>` for sender count fallback.
- `GET /customerCountDto?address=<pubkey>` for customer count fallback.
- `GET /allwinners` and route alias `/allWinners` for all winner display.
- `GET /winnerList` with fallback to `GET /winerList` for winner list compatibility.
- `GET /bigRandomWiner` with fallback to `GET /bigRandomWinner` for big-random winner compatibility.

## Restored Website Routes

- `/conductor` now renders the explorer/search form without a heavy initial API lookup.
- `/conductor/lookup?index=<index>` performs `GET /conductorBlock?index=<index>`.
- `/blocks` and `/conductor/blocks` perform bounded `POST /sub-blocks`.
- `/winners`, `/allwinners`, and `/allWinners` render all winners.
- `/winnerList` and `/winerList` render the legacy winner-list view and preserve the old typo route.

## Duplicate Index Handling

Duplicate `index=1` is handled safely. The website accepts either a single block object or an array/list response from `/conductorBlock?index=1`. If the node returns multiple blocks for a duplicate index, the page renders the returned list instead of assuming exactly one object.

## Files Changed

- `src/main/java/com/example/website/citu/utils/UtilUrl.java`
- `src/main/java/com/example/website/citu/controller/ApiFallback.java`
- `src/main/java/com/example/website/citu/controller/ExplorerController.java`
- `src/main/java/com/example/website/citu/controller/WinnersController.java`
- `src/main/resources/templates/conductor.html`
- `src/main/resources/templates/winners.html`
- `src/main/resources/templates/blocks/header.html`
- `src/test/java/com/example/website/citu/controller/LegacyApiContractTest.java`
- `src/test/java/com/example/website/citu/controller/WebsiteApiFallbackRenderingTest.java`

## Timeout Policy

- Lightweight endpoints keep the default `1500 ms` connect timeout and `3000 ms` read timeout.
- Explorer lookup/range endpoints use the longer explorer read timeout: `10000 ms`.
- All API failures still render safe fallback content instead of HTTP 500.

## Emergency Defaults

Confirmed unchanged:

- `server.ssl.enabled=false`
- `citu.http.additional.enabled=false`
- `citu.https.redirect.enabled=false`
- `citu.number-formatting.enabled=false`

`NumberFormattingAspect` remains disabled by default and guarded so it cannot break rendering if explicitly enabled.

## Tests Run

- `.\mvnw.cmd test`
  - Result: success
  - Tests: 47 run, 0 failures, 0 errors
- `.\mvnw.cmd clean package`
  - Result: success
  - Tests during package: 47 run, 0 failures, 0 errors

## Final SHA256

`ac0c350832dc72881bf38f1ccf1c28abba7fdfc0667d8d7a7207944d89dbb8e9  citu-website-v35-activation-514291-1.0.0.jar`

## GitHub Release

https://github.com/CorporateFounder/website.citu-web/releases/tag/v35-website-activation-514291

Release assets to refresh with `--clobber`:

- `citu-website-v35-activation-514291-1.0.0.jar`
- `CITU_V35_514291_WEBSITE_SHA256SUMS.txt`

## Public Download Verification

Downloaded the refreshed release assets into:

`C:\citu\release-dryrun\v35-514291\public-verify-11g8`

Downloaded jar SHA256:

`ac0c350832dc72881bf38f1ccf1c28abba7fdfc0667d8d7a7207944d89dbb8e9`

Downloaded SHA file expected value:

`ac0c350832dc72881bf38f1ccf1c28abba7fdfc0667d8d7a7207944d89dbb8e9`

Result: public download verification passed. The downloaded jar contains the patched legacy API logic in `UtilUrl`, `ApiFallback`, `ExplorerController`, `WinnersController`, the restored explorer/winners/header templates, and `application.properties`.

## Exact VPS HTTP-Only Command

```powershell
java -Dserver.address=194.87.236.238 -Dserver.port=80 -Dserver.ssl.enabled=false -Dcitu.http.additional.enabled=false -Dcitu.https.redirect.enabled=false -Dcitu.number-formatting.enabled=false -jar C:\citu-website-v35-activation-514291-1.0.0.jar
```

## Safety Confirmation

- No server/node project changed.
- No wallet project changed.
- `Seting.java` was not changed.
- Consensus code was not changed.
- `C:\server\resources` was not touched.
- `C:\resources` was not touched.
- No VPS deploy, restart, or process kill was performed.
- No burn transaction was created.
- No jackpot payout was created.
- No node/wallet release assets were uploaded.
- No new tag was created.
- Release dry-run files were not added to git.
