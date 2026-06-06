# CITU V35 Phase 11G-7 Website API Timeout Fallback Hotfix Report

## Root Cause

Several website pages called the node API synchronously and then parsed the response without a safe fallback. When the node API was slow, unavailable, timed out, or returned unusable data, exceptions from `UtilUrl`, JSON parsing, numeric parsing, or Thymeleaf numeric formatting could bubble into Spring MVC and return HTTP 500.

`NumberFormattingAspect` was also present in page-rendering stack traces. Emergency mode requires this aspect to be disabled by default and unable to break rendering if explicitly enabled.

## Files Changed

- `src/main/java/com/example/website/citu/utils/UtilUrl.java`
- `src/main/java/com/example/website/citu/controller/ApiFallback.java`
- `src/main/java/com/example/website/citu/controller/ExplorerController.java`
- `src/main/java/com/example/website/citu/controller/TransactionCustomerController.java`
- `src/main/java/com/example/website/citu/controller/TransactionSenderController.java`
- `src/main/java/com/example/website/citu/controller/VotingController.java`
- `src/main/java/com/example/website/citu/controller/WebController.java`
- `src/main/java/com/example/website/citu/controller/WinnersController.java`
- `src/main/java/com/example/website/citu/config/NumberFormattingAspect.java`
- `src/main/resources/templates/conductor.html`
- `src/test/java/com/example/website/citu/ApplicationTests.java`
- `src/test/java/com/example/website/citu/controller/WebsiteApiFallbackRenderingTest.java`
- `src/test/java/com/example/website/citu/utils/UtilUrlTimeoutTest.java`

## What Changed

- `UtilUrl` now uses short emergency timeouts: 1500 ms connect and 3000 ms read.
- `UtilUrl` catches timeout, IO, runtime, and parsing-adjacent failures and returns safe fallback values instead of throwing into controllers.
- Node API parsing in high-risk controllers now falls back to empty lists, null objects, safe zero values, or `temporarily unavailable`.
- `/conductor`, `/customer`, `/sender`, `/`, `/voting`, `/winners`, and `/daily_rating` are hardened against unavailable node API data.
- `conductor.html` no longer indexes an empty block list for pagination.
- `NumberFormattingAspect` is conditional on `citu.number-formatting.enabled=true`, defaults off, and returns the original result if formatting ever fails.
- HTTP-only emergency defaults remain:
  - `server.ssl.enabled=false`
  - `citu.http.additional.enabled=false`
  - `citu.https.redirect.enabled=false`
  - `citu.number-formatting.enabled=false`

## Tests Run

- `.\mvnw.cmd test`
  - Result: success
  - Tests: 40 run, 0 failures, 0 errors
- `.\mvnw.cmd clean package`
  - Result: success
  - Tests during package: 40 run, 0 failures, 0 errors

## Final SHA256

`7419c2703c885b5685ac1756a8cc960b10d4379fd7c3e620d3adfd1a1040f621  citu-website-v35-activation-514291-1.0.0.jar`

## GitHub Release

https://github.com/CorporateFounder/website.citu-web/releases/tag/v35-website-activation-514291

Refreshed existing release assets with `--clobber`:

- `citu-website-v35-activation-514291-1.0.0.jar`
- `CITU_V35_514291_WEBSITE_SHA256SUMS.txt`

## Public Download Verification

Downloaded the refreshed release assets into:

`C:\citu\release-dryrun\v35-514291\public-verify`

Downloaded jar SHA256:

`7419c2703c885b5685ac1756a8cc960b10d4379fd7c3e620d3adfd1a1040f621`

Downloaded SHA file expected value:

`7419c2703c885b5685ac1756a8cc960b10d4379fd7c3e620d3adfd1a1040f621`

Result: public download verification passed. The downloaded jar contains the patched `UtilUrl`, fallback controllers, `ApiFallback`, `NumberFormattingAspect`, and `application.properties`.

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
