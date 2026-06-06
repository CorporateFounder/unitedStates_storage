# CITU V35 Phase 11G-10 Website Winners Layout + Latest Blocks Report

Date: 2026-06-06

## Scope

Website-only hotfix for `C:\citu\website.citu-web-master`. No VPS deployment or restart was performed.

## Root Cause

Winner compatibility routes all rendered `winners.html`, but the page still carried public text and inner links that made compatibility endpoints look like separate old legacy pages. `/blocks` also reused `conductor.html` and defaulted to `start=0&finish=10`, so the public block list opened on genesis/early blocks instead of the latest chain range.

## Actual Route and Template Mapping

- `/winners`, `/allwinners`, `/allWinners`: `WinnersController#winners`, template `winners.html`.
- `/winnerList`, `/winerList`: `WinnersController#winnerList`, template `winners.html`.
- `/blocks`, `/conductor/blocks`: `ExplorerController#blocks`, template `blocks.html`.
- `/conductor`: `ExplorerController#conductors`, template `conductor.html`.

## Winners Layout Fix

All winner routes now render the same dark CITU/V35 shared layout through `winners.html`. The compatibility route `/winerList` remains available, but the typo is not exposed in the public page title, source label, or shared navigation. The winner page keeps clean links to `/blocks`, `/winners`, and `/winnerList`, and uses styled fallback cards when node data is unavailable.

## Latest Blocks Default

`/blocks` now defaults to the latest 10 blocks:

1. `GET /size`
2. Calculate `finish = currentSize`
3. Calculate `start = max(0, currentSize - 10)`
4. `POST /sub-blocks {"start": start, "finish": finish}`

If `/size` fails or returns an invalid value, `/blocks` falls back to `0..10` and shows:

```text
latest size temporarily unavailable; showing fallback range
```

Manual `start`/`finish` submissions remain preserved and do not call `/size`.

## Blocks UI

`blocks.html` is now a dedicated dark block list/range page. It shows displayed range, current size when available, block count, and null-safe block cards with:

- `index`
- `hashBlock`
- `previousHash`
- `minerAddress`
- `founderAddress`
- `randomNumberProof`
- `minerRewards`
- `hashCompexity`
- `timestamp`
- `dtoTransactions` count

Duplicate indexes are displayed safely as normal rows/cards.

## Conductor Clarity

`/conductor` remains the explorer search page for block index, transaction signature, and address history. `/blocks` is now the block list/range page. Cross-links remain, but labels are simplified to `Explorer search`, `Blocks`, and `Winners`.

## Files Changed

- `src/main/java/com/example/website/citu/controller/ExplorerController.java`
- `src/main/java/com/example/website/citu/controller/WinnersController.java`
- `src/main/resources/templates/blocks.html`
- `src/main/resources/templates/conductor.html`
- `src/main/resources/static/css/styles.css`
- `src/test/java/com/example/website/citu/controller/LegacyApiContractTest.java`
- `src/test/java/com/example/website/citu/controller/WebsiteApiFallbackRenderingTest.java`
- `CITU_V35_PHASE11G10_WEBSITE_WINNERS_LAYOUT_LATEST_BLOCKS_REPORT.md`

## Tests Run

```text
.\mvnw.cmd test
.\mvnw.cmd clean package
```

Result: all 56 tests passed.

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

The old snapshot-named website asset was removed from the release.

Final SHA256:

```text
5a82f14d075b5fb353be732bc7314a0d2d5f010c8d292a0f2609959a46cda235
```

## Public Download Verification

Downloaded assets:

```text
https://github.com/CorporateFounder/website.citu-web/releases/download/v35-website-activation-514291/citu-website-v35-activation-514291-1.0.0.jar
https://github.com/CorporateFounder/website.citu-web/releases/download/v35-website-activation-514291/CITU_V35_514291_WEBSITE_SHA256SUMS.txt
```

Downloaded JAR SHA256 matched the downloaded SHA file:

```text
5a82f14d075b5fb353be732bc7314a0d2d5f010c8d292a0f2609959a46cda235
```

The downloaded JAR contains:

- `ExplorerController.class`
- `WinnersController.class`
- `templates/blocks.html`
- `templates/conductor.html`
- `templates/winners.html`
- `static/css/styles.css`

## Exact Windows VPS Download Command

```powershell
powershell -NoProfile -ExecutionPolicy Bypass -Command "[Net.ServicePointManager]::SecurityProtocol=[Net.SecurityProtocolType]::Tls12; Invoke-WebRequest -UseBasicParsing -Uri 'https://github.com/CorporateFounder/website.citu-web/releases/download/v35-website-activation-514291/citu-website-v35-activation-514291-1.0.0.jar' -OutFile 'C:\citu-website-v35-activation-514291-1.0.0-11G10.jar'"
```

## Exact VPS HTTP-Only Start Command

```powershell
java -Dserver.address=194.87.236.238 -Dserver.port=80 -Dserver.ssl.enabled=false -Dcitu.http.additional.enabled=false -Dcitu.https.redirect.enabled=false -Dcitu.number-formatting.enabled=false -jar C:\citu-website-v35-activation-514291-1.0.0-11G10.jar
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
