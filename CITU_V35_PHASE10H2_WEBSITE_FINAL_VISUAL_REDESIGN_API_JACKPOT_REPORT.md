# CITU V35 Phase 10H-2 Website Final Visual Redesign, API UX, Homepage Metrics, and Jackpot Presentation Report

## Executive summary

Phase 10H-2 completed a website-only public presentation redesign. The website now uses a unified dark CITU visual layer with gold accents, compact branded navigation, a complete dark footer, responsive metric cards, clearer V35 warnings, professional API examples, and a pre-activation jackpot presentation.

V35 was not activated. No server or wallet consensus code was changed. No `Seting.java` file was changed.

## Pages changed

- `website.citu-web-master/src/main/resources/templates/main.html`
- `website.citu-web-master/src/main/resources/templates/blocks/footer.html`
- `website.citu-web-master/src/main/resources/templates/detail-transactions.html`
- `website.citu-web-master/src/main/resources/templates/v35.html`
- `website.citu-web-master/src/main/resources/templates/v35-jackpot.html`
- `website.citu-web-master/src/main/resources/templates/wallet_and_node_url.html`
- `website.citu-web-master/src/main/resources/static/css/styles.css`
- `website.citu-web-master/src/test/java/com/example/website/citu/controller/V35PublicControllerTest.java`

## Design improvements

- Added a unified CITU dark navy / near-black public visual layer with gold accents.
- Kept the official CITU logo visible without oversized branding.
- Added responsive cards, code cards, metric grids, jackpot grids, API cards, and consistent link groups.
- Replaced the unfinished pale footer with a dark branded footer containing protocol status, official source links, API / V35 / checksum links, and the burn-address warning.
- Confirmed mobile layouts do not horizontally overflow during local visual review.

## Homepage metrics changes

The homepage now prioritizes:

- blockchain size;
- legacy total GOLD endpoint value, explicitly not mislabeled as circulating supply;
- burned GOLD: `0.00 / no founder burn yet`;
- active / circulating GOLD supply: pending official burn proof;
- total accounts;
- transactions today;
- current legacy / pre-V35 block difficulty.

Obsolete multiplier, reduction countdown, previous-block sender targets, and older transaction-detail metrics were moved into the collapsed `Legacy / pre-V35 technical details` secondary section. Old APIs were not removed.

## Jackpot presentation

The homepage and `/v35/jackpot` now show attractive expected-after-V35 cards:

| Period | Expected payout |
| --- | ---: |
| Daily | 302.40 GOLD |
| Weekly | 1209.60 GOLD |
| Monthly | 3888.00 GOLD |
| Quarterly | 9720.00 GOLD |
| Half-year | 15724.80 GOLD |
| Yearly | 23652.00 GOLD |

The pages clearly state:

- jackpot is disabled until V35 activation;
- no live winner exists before activation;
- winners will be shown only after protocol payout data exists;
- no jackpot payout was created in this phase.

## API documentation improvements

`/wallet_and_node_url` retains all backward-compatible route tables and compatibility appendices. It now also includes:

- release-download preparation links;
- future GitHub Release tag links;
- future node and wallet JAR links and launch commands;
- checksum warnings;
- dedicated API request / response cards;
- safety badges: `SAFE READ-ONLY`, `V35 READ-ONLY`, and `REVIEW SIGNED JSON`;
- examples for `/size`, `/balance`, `/account`, `/customerTransactions`, `/addTransaction`, `/isTransactionAdd`, `/v35/protocol`, `/v35/burn`, and `/v35/jackpot/status`;
- explicit warning that `/sendCoin?...&password=...` is legacy local-only and not recommended for production exchange integration.

No private key appears in examples.

## Transaction detail improvements

`detail-transactions.html` now uses the dark CITU layout with structured transaction cards. Sender, customer, signature, vote, miner bonus, GOLD, and stock values use aligned labels and wrap safely for long content. Route behavior and transaction data were not changed.

## Future release links status

Future links were added for preparation only:

- server source: `https://github.com/CorporateFounder/unitedStates_storage`
- wallet source: `https://github.com/CorporateFounder/unitedStates_final`
- future server release: `https://github.com/CorporateFounder/unitedStates_storage/releases/tag/v35-node-activation-514291`
- future wallet release: `https://github.com/CorporateFounder/unitedStates_final/releases/tag/v35-wallet-activation-514291`

The website clearly states that these future release links work only after the final GitHub Releases are published. Old or dry-run JAR files must not be used as V35 activation releases.

## Tests and package result

Commands run:

```powershell
cd C:\citu\website.citu-web-master
.\mvnw.cmd test
.\mvnw.cmd clean package
```

Result:

- website tests: PASS, 12 / 12;
- website clean package: PASS;
- known existing Maven warning remains: duplicate `org.bouncycastle:bcprov-jdk15on` declaration in `pom.xml`.

## Local smoke results

Fresh local website JAR was started on `127.0.0.1:8085`.

| Route | Result |
| --- | --- |
| `/` | HTTP 200 |
| `/v35` | HTTP 200 |
| `/wallet_and_node_url` | HTTP 200 |
| `/v35/preactivation-status` | HTTP 200 |
| `/v35/checksums` | HTTP 200 |
| `/v35/burn-warning` | HTTP 200 |
| `/white_papper` | HTTP 200 |
| `/v35/jackpot` | HTTP 200 |
| `/detail-transactions?title=detail%20transactions` | HTTP 200 |

Local browser review covered desktop and narrow-screen layouts for homepage, API documentation, and jackpot presentation.

## Safety confirmation

- No `Seting.java` file changed.
- `V35_RESCUE_FORK_INDEX` remains `Long.MAX_VALUE` in server and wallet.
- `V35_BURN_ADDRESS` remains `BURN_ADDRESS` in server and wallet.
- Candidate height `514291` remains planning-only.
- No activation occurred.
- No founder burn transaction was created.
- No jackpot payout was created.
- No GitHub Release was created.
- No file was uploaded to GitHub.
- No deployment occurred.
- No tag was created.
- No final release artifact was created.
- No final checksum was generated.
- No commit was created.

## Recommended next phase

Perform manual visual review of the local website routes, then run a report-only final website sign-off checkpoint. Keep real activation preparation separate and refresh live `/size` before any future `Seting.java` edit.
