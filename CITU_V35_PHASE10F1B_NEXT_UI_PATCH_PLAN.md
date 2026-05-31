# CITU V35 Phase 10F-1B - Next UI Patch Plan

## Scope

Recommended next phase:

**Phase 10F-1C - unified navigation, API docs, and wallet visual polish patch; no activation and no deployment.**

Keep consensus, settings, rewards, PoW, block format, transaction format, burn, jackpot payout, deployment, tags, artifacts, and checksums untouched.

## 1. Website file groups likely to change

Shared website presentation:

- `website.citu-web-master/src/main/resources/templates/blocks/header.html`
- `website.citu-web-master/src/main/resources/templates/blocks/footer.html`
- `website.citu-web-master/src/main/resources/static/css/styles.css`
- `website.citu-web-master/src/test/java/com/example/website/citu/controller/V35PublicControllerTest.java`

Public API and discoverability:

- `website.citu-web-master/src/main/resources/templates/wallet_and_node_url.html`
- `website.citu-web-master/src/main/resources/templates/main.html`
- add a focused website navigation/API-doc test if practical

Optional low-risk V35 navigation cleanup:

- `website.citu-web-master/src/main/resources/templates/v35.html`

## 2. Wallet file groups likely to change

Wallet shared presentation:

- `unitedStates_final-master/src/main/resources/templates/blocks/header.html`
- `unitedStates_final-master/src/main/resources/templates/blocks/footer.html`
- add `unitedStates_final-master/src/main/resources/static/css/citu-wallet.css`

Wallet V35 and legacy mining polish:

- `unitedStates_final-master/src/main/resources/templates/v35-dashboard.html`
- `unitedStates_final-master/src/main/resources/templates/mining.html`
- `unitedStates_final-master/src/test/java/International_Trade_Union/controllers/V35DashboardControllerTest.java`

Do not change controller routes or form field names unless a real defect is proven.

## 3. Exact website navigation links to restore

Main nav:

- `/`
- `/conductor` as `Explorer`
- `/wallet_and_node_url` as `API / Exchanges`
- `/charter`
- `/white_papper` as `White paper`
- grouped guides:
  - `/how_to_install`
  - `/how_to_open_an_account`
  - `/how_to_mining`
  - `/how_to_transaction`
  - `/how_to_vote_and_what_voting_types_are_there`
  - `/solving_common_problems`
- `/v35`

V35 nav:

- `/v35`
- `/v35/preactivation-status`
- `/v35/operator-guide`
- `/v35/tokenomics`
- `/v35/mining`
- `/v35/burn`
- `/v35/burn-warning`
- `/v35/jackpot`
- `/v35/founder-burn-proof`
- `/v35/checksums`
- `/v35/faq`

Do not link nonexistent `/v35/burn-proof`; use `/v35/founder-burn-proof`.

## 4. Exact wallet pages to polish

### Shared wallet header

- replace Bootstrap sample SVG/title with official CITU emblem;
- use a compact branded header;
- preserve all existing operational links;
- keep `/v35-dashboard` visible.

### `/v35-dashboard`

- add shared dark CITU stylesheet;
- show official emblem and compact status badges;
- keep `V35 NOT ACTIVE YET`;
- keep `NO FOUNDER BURN YET`;
- add `PLANNING CANDIDATE: 514291`;
- retain all tables and warnings;
- keep `BURN_ADDRESS` clearly internal-only.

### `/mining`

- style as `Legacy / pre-V35 mining controls`;
- visually separate start mining, legacy difficulty, staking, and unstaking;
- show `Legacy default difficulty: 17`;
- do not encourage custom difficulty changes;
- preserve `/customDiff`, `/staking`, and `/unstaking` contracts.

## 5. Exact API documentation improvements

Update `/wallet_and_node_url` into readable sections:

1. Public node API for exchanges and explorers.
2. Exchange integration quick start.
3. Local wallet API.
4. V35 read-only API.
5. Security warnings.
6. Local smoke URLs versus production node URL.

Add examples for:

- `/size`
- `/prevBlock`
- `/balance?address=<PUBLIC_ADDRESS>`
- `/conductorBlock?index=<INDEX>`
- `/statusTransaction58`
- `/version`
- `/confirmReadiness`
- `/v35/protocol`
- `/v35/supply`
- `/v35/burn`
- `/v35/founder/burn-snapshot`
- `/v35/jackpot/status`
- `/v35/jackpot/history`
- `/v35/jackpot/proof/{periodType}/{periodEnd}`

Security copy:

- use placeholders only;
- never include real private keys;
- label wallet `/keys` and `/sendCoin?...password=...` as legacy local-only;
- warn that query-string private keys are unsafe for production integration;
- tell exchanges to use signed transaction submission and read-only node endpoints;
- state V35 endpoints may be unavailable until the reviewed server build is deployed;
- state V35 remains inactive until the approved fork height.

Fix the documented customer range path to:

```text
/findBlocksByCustomerInRange
```

## 6. Test/build commands for next patch

Website:

```powershell
cd C:\citu\website.citu-web-master
.\mvnw.cmd test
.\mvnw.cmd clean package
```

Wallet:

```powershell
cd C:\citu\unitedStates_final-master
.\mvnw.cmd test
.\mvnw.cmd clean package
```

After package verification, restore tracked generated wallet `target/` churn if Maven refreshes it.

Audit:

```powershell
cd C:\citu
git --no-pager diff --name-only -- `
  unitedStates_storage-master/src/main/java/International_Trade_Union/setings/Seting.java `
  unitedStates_final-master/src/main/java/International_Trade_Union/setings/Seting.java
```

Required result: no output.

## 7. Manual screenshot checklist

Website:

- `/`
- `/wallet_and_node_url`
- `/conductor`
- `/white_papper`
- `/charter`
- `/v35`
- `/v35/preactivation-status`
- `/v35/operator-guide`
- `/v35/checksums`
- `/v35/burn-warning`
- mobile-width header/menu

Wallet:

- `/`
- `/v35-dashboard`
- `/mining`
- `/seting`
- menu open and closed
- mobile-width layout

Confirm visually:

- official CITU emblem;
- consistent black/deep-navy/gold design;
- API docs and Explorer easy to find;
- `V35 NOT ACTIVE YET`;
- `NO FOUNDER BURN YET`;
- `PLANNING CANDIDATE: 514291`;
- no public fake burn address;
- legacy mining is clearly labeled;
- default legacy difficulty `17` is visible;
- forms still fit and remain usable.

## 8. Safety constraints

- Do not modify server code.
- Do not modify server or wallet `Seting.java`.
- Do not change V35 constants.
- Do not activate V35.
- Do not create burn or jackpot payout transactions.
- Do not deploy.
- Do not tag, checksum, package a release, or commit.
