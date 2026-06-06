# CITU V35 Phase 11G-11 Website Block Details + Explorer List Report

Date: 2026-06-06

## Scope

Website-only hotfix for `C:\citu\website.citu-web-master`. No VPS deployment or restart was performed.

## Root Cause

`/blocks` linked to `/detail-transactions/{index}`, but that route redirected through flash attributes into `/detail-transactions`. Links opened from normal pages could therefore land on a page without the required model data. 11G10 also separated `/blocks` from `/conductor`, which made Explorer a search-only page and removed the recent block preview users expected from the old explorer workflow.

## Exact Details Routes Fixed

Canonical detail route:

```text
GET /detail-transactions/{index}
```

Compatibility aliases:

```text
GET /detail/{index}
GET /details/{index}
GET /detail-block/{index}
```

Friendly direct routes without parameters:

```text
GET /detail-transactions
GET /detail
GET /details
GET /detail-block
```

Direct no-parameter routes now return HTTP 200 with:

```text
Select a block or transaction from Explorer.
```

Bad or unavailable block indexes return HTTP 200 with:

```text
Block not found or temporarily unavailable.
```

## Blocks Details Links

Every block shown on `/blocks` now links to:

```text
/detail-transactions/{index}
```

The route calls:

```text
GET /conductorBlock?index={index}
```

It renders the block details directly instead of redirecting to a flash-only page. Duplicate indexes are handled by showing the first block as the primary detail and showing a note plus all matching blocks.

## Conductor Latest Block List

`/conductor` remains the Explorer/search page and now includes:

- search by block index / transaction signature
- sender/customer address history forms
- recent/latest block preview list

Recent preview flow:

```text
GET /size
POST /sub-blocks {"start": max(0, size - 10), "finish": size}
```

If `/size` or `/sub-blocks` fails, `/conductor` still returns HTTP 200 and shows:

```text
latest blocks temporarily unavailable
```

Each recent block preview item has a working Details link to `/detail-transactions/{index}`.

## Null Display Cleanup

Block and detail templates now render missing strings/timestamps as:

```text
not available
```

The updated tests guard against literal `>null<` rendering in block/detail UI.

## Files Changed

- `src/main/java/com/example/website/citu/controller/ExplorerController.java`
- `src/main/resources/templates/blocks.html`
- `src/main/resources/templates/conductor.html`
- `src/main/resources/templates/detail-transactions.html`
- `src/test/java/com/example/website/citu/controller/LegacyApiContractTest.java`
- `src/test/java/com/example/website/citu/controller/WebsiteApiFallbackRenderingTest.java`
- `CITU_V35_PHASE11G11_WEBSITE_BLOCK_DETAILS_EXPLORER_LIST_REPORT.md`

## Tests Run

```text
.\mvnw.cmd test
.\mvnw.cmd clean package
```

Result: all 60 tests passed.

## Release

GitHub release:

```text
https://github.com/CorporateFounder/website.citu-web/releases/tag/v35-website-activation-514291
```

Release assets:

```text
citu-website-v35-activation-514291-1.0.0.jar
CITU_V35_514291_WEBSITE_SHA256SUMS.txt
```

Final SHA256:

```text
570551eeff96ed61b0dbf11e3663a78f23630aa3cb14e7c32cc283156cde9769
```

## Public Download Verification

Downloaded assets:

```text
https://github.com/CorporateFounder/website.citu-web/releases/download/v35-website-activation-514291/citu-website-v35-activation-514291-1.0.0.jar
https://github.com/CorporateFounder/website.citu-web/releases/download/v35-website-activation-514291/CITU_V35_514291_WEBSITE_SHA256SUMS.txt
```

Downloaded JAR SHA256 matched the downloaded SHA file:

```text
570551eeff96ed61b0dbf11e3663a78f23630aa3cb14e7c32cc283156cde9769
```

The downloaded JAR contains:

- `ExplorerController.class`
- `templates/blocks.html`
- `templates/conductor.html`
- `templates/detail-transactions.html`
- `templates/winners.html`
- `static/css/styles.css`

## Exact Windows VPS Download Command

```powershell
powershell -NoProfile -ExecutionPolicy Bypass -Command "[Net.ServicePointManager]::SecurityProtocol=[Net.SecurityProtocolType]::Tls12; Invoke-WebRequest -UseBasicParsing -Uri 'https://github.com/CorporateFounder/website.citu-web/releases/download/v35-website-activation-514291/citu-website-v35-activation-514291-1.0.0.jar' -OutFile 'C:\citu-website-v35-activation-514291-1.0.0-11G11.jar'"
```

## Exact VPS HTTP-Only Start Command

```powershell
java -Dserver.address=194.87.236.238 -Dserver.port=80 -Dserver.ssl.enabled=false -Dcitu.http.additional.enabled=false -Dcitu.https.redirect.enabled=false -Dcitu.number-formatting.enabled=false -jar C:\citu-website-v35-activation-514291-1.0.0-11G11.jar
```

## Safety Confirmation

- No server/node project changed.
- No wallet project changed.
- No `Seting.java` changed.
- No consensus changed.
- No `C:\server\resources` or `C:\resources` touched.
- No VPS deploy, restart, or process kill performed.
- No burn transaction or jackpot payout created.
- No node/wallet release asset uploaded.
- No new GitHub tag created.
