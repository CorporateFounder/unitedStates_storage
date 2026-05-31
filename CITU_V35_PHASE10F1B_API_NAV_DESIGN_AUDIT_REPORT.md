# CITU V35 Phase 10F-1B - API, Navigation, and Unified Design Audit

## 1. Executive summary

Phase 10F-1B is a report-only audit. No source, template, test, static asset, settings, deployment, or release action was performed.

The three applications expose the required operational surfaces, but the public product experience is not yet ready for activation:

- the public website has useful legacy resources, but the shared header now exposes mostly V35 links and makes API docs, Explorer, White paper, Charter, and onboarding guides less discoverable;
- the public API documentation exists at `/wallet_and_node_url`, but it does not document the V35 read-only endpoints and contains legacy examples that need security warnings;
- the wallet still has a Bootstrap sample icon, raw technical forms, and legacy mining controls;
- wallet mining custom difficulty is correctly initialized from `Seting.V34_MIN_DIFF`, which is `17` outside tests, but the UI should present this as a legacy default rather than encourage arbitrary changes;
- V35 website branding is stronger after Phase 10F-1A, but website and wallet do not yet read as one product.

Activation should wait for a focused UI/docs/navigation patch and manual screenshot sign-off.

## 2. Safety audit

| Check | Result |
| --- | --- |
| Branch | `master` |
| Working tree | Already contains uncommitted Phase 10F-1 / 10F-1A UI, tests, reports, and logo assets |
| Server `V35_RESCUE_FORK_INDEX` | `Long.MAX_VALUE` |
| Wallet `V35_RESCUE_FORK_INDEX` | `Long.MAX_VALUE` |
| Server `V35_BURN_ADDRESS` | `BURN_ADDRESS` |
| Wallet `V35_BURN_ADDRESS` | `BURN_ADDRESS` |
| `Seting.java` activation diff | None |
| Planning candidate | `514291`, planning-only |
| Activation state | Disabled |

No burn transaction, fork activation, jackpot payout, deployment, final tag, release artifact, or checksum action occurred.

## 3. Server API inventory

Primary production node port: `82`. Local isolated smoke port: `8083`.

### Core exchange, wallet, explorer, and operator endpoints

| Method | Path | Controller | Purpose and parameters | Example | Response | Audience | Notes |
| --- | --- | --- | --- | --- | --- | --- | --- |
| GET | `/size` | `BasisController` | Chain size | `/size` | integer | exchange, explorer, operator, wallet | Core readiness metric |
| GET | `/prevBlock` | `BasisController` | Latest block | `/prevBlock` | `Block` JSON | exchange, explorer, operator | Use with `/size` for snapshot evidence |
| POST | `/sub-blocks` | `BasisController` | Block range; body `{"start":0,"finish":10}` | `/sub-blocks` | list of `Block` | explorer, sync operator | Range is bounded internally |
| POST | `/block` | `BasisController` | Block lookup; body integer index | `/block` body `100` | `Block` JSON | operator | Internal-style block endpoint |
| GET | `/conductorBlock?index=<n>` | `ConductorController` | Block lookup by index | `/conductorBlock?index=100` | `Block` JSON | explorer, exchange | Prefer for public explorer docs |
| GET | `/blockHash?hash=<hash>` | `ConductorController` | Block lookup by hash | `/blockHash?hash=<HASH>` | `Block` JSON | explorer, operator | Read-only |
| GET | `/balance?address=<address>` | `BasisController` | Account balance snapshot | `/balance?address=<PUBLIC_ADDRESS>` | `Account` JSON | exchange, user, operator | Public address only |
| GET | `/account?address=<address>` | `ConductorController` | Full account balances | `/account?address=<PUBLIC_ADDRESS>` | `Account` JSON | exchange, wallet | Public address only |
| GET | `/dollar?address=<address>` | `ConductorController` | GOLD balance | `/dollar?address=<PUBLIC_ADDRESS>` | number | exchange, wallet | Legacy naming |
| GET | `/stock?address=<address>` | `ConductorController` | Stock balance | `/stock?address=<PUBLIC_ADDRESS>` | number | wallet | Legacy |
| GET | `/conductorHashTran?hash=<signature>` | `ConductorController` | Transaction lookup by signature | `/conductorHashTran?hash=<SIGNATURE>` | transaction JSON | explorer, exchange | Read-only |
| POST | `/statusTransaction58` | `ConductorController` | Transaction state; body `{"sign":"<BASE58_SIGNATURE>"}` | `/statusTransaction58` | status object | exchange, wallet | States include updating, incorrect, success, pending, absent |
| POST | `/statusTransaction64` | `ConductorController` | Base64 transaction state | `/statusTransaction64` | status object | exchange, wallet | Use placeholders in docs |
| GET | `/isTransactionAdd?sign=<signature>` | `ConductorController` | Boolean inclusion check | `/isTransactionAdd?sign=<BASE58_SIGNATURE>` | boolean | exchange | Signature is public proof data |
| POST | `/isTransactionAddBase64` | `ConductorController` | Boolean inclusion check | body `{"sign":"<BASE64_SIGNATURE>"}` | boolean | exchange | Read-only |
| POST | `/TransactionAddBase` | `ConductorController` | Fetch transaction by Base58 signature | body `{"sign":"<BASE58_SIGNATURE>"}` | transaction or null | exchange, explorer | Read-only |
| POST | `/TransactionAddBase64` | `ConductorController` | Fetch transaction by Base64 signature | body `{"sign":"<BASE64_SIGNATURE>"}` | transaction or null | exchange, explorer | Read-only |
| POST | `/findBlocksFromSign58` | `ConductorController` | Blocks containing Base58 signature | body `{"sign":"<BASE58_SIGNATURE>"}` | list of blocks | explorer | Read-only |
| POST | `/findBlocksFromSign64` | `ConductorController` | Blocks containing Base64 signature | body `{"sign":"<BASE64_SIGNATURE>"}` | list of blocks | explorer | Read-only |
| GET | `/senderTransactions?address=<a>&from=<n>&to=<n>` | `ConductorController` | Sent transactions in range | `/senderTransactions?address=<ADDRESS>&from=0&to=10` | transaction list | exchange, explorer | Range limited to 500 |
| GET | `/customerTransactions?address=<a>&from=<n>&to=<n>` | `ConductorController` | Received transactions in range | `/customerTransactions?address=<ADDRESS>&from=0&to=10` | transaction list | exchange, explorer | Range limited to 500 |
| GET | `/senderCountDto?address=<a>` | `ConductorController` | Sent transaction count | `/senderCountDto?address=<ADDRESS>` | integer | exchange, explorer | Read-only |
| GET | `/customerCountDto?address=<a>` | `ConductorController` | Received transaction count | `/customerCountDto?address=<ADDRESS>` | integer | exchange, explorer | Read-only |
| GET | `/confirmReadiness` | `NodeController` | Node readiness | `/confirmReadiness` | `ready` or `not_ready` | operator | Add to operator docs |
| GET | `/status` | `BasisController` | Node status | `/status` | status payload | operator | Add shape example later |
| GET | `/version` | `BasisController` | Node version | `/version` | number | exchange, operator | Release verification |
| GET | `/difficultyBlockchain` | `BasisController` | Latest and aggregate difficulty | `/difficultyBlockchain` | difficulty payload | explorer, operator | Legacy chain metric |
| POST | `/addTransaction` | `TransactionController` | Submit signed transaction JSON | `/addTransaction` | HTTP result | wallet, exchange custody backend | Signed transaction only; document carefully |

### V35 read-only endpoints

| Method | Path | Controller | Purpose | Example | Response | Audience | Safety notes |
| --- | --- | --- | --- | --- | --- | --- | --- |
| GET | `/v35/protocol` | `V35SupplyController` | V35 protocol policy and active state | `/v35/protocol` | map JSON | exchange, operator, wallet | Expected inactive until reviewed build is deployed and fork activates |
| GET | `/v35/supply` | `V35SupplyController` | Supply snapshot | `/v35/supply` | map JSON | exchange, operator, wallet | Read-only canonical-chain/account view |
| GET | `/v35/burn` | `V35SupplyController` | Burn status | `/v35/burn` | map JSON | exchange, operator, wallet | Must not be interpreted as instruction to send funds |
| GET | `/v35/founder` | `V35SupplyController` | Founder reserve status | `/v35/founder` | map JSON | internal reviewer, operator | Read-only |
| GET | `/v35/founder/burn-snapshot` | `V35SupplyController` | Founder burn snapshot evidence | `/v35/founder/burn-snapshot` | map JSON | internal reviewer, proof publisher | Preferred future snapshot source |
| GET | `/v35/jackpot/status` | `V35JackpotController` | Jackpot state | `/v35/jackpot/status` | map JSON | operator, wallet | No payout should exist before valid V35 conditions |
| GET | `/v35/jackpot/periods` | `V35JackpotController` | Jackpot windows | `/v35/jackpot/periods` | map JSON | operator, wallet | Static/read-only policy surface |
| GET | `/v35/jackpot/history` | `V35JackpotController` | Payout history | `/v35/jackpot/history` | map JSON | operator, explorer | Read-only |
| GET | `/v35/jackpot/tickets/{address}` | `V35JackpotController` | Ticket view for public address | `/v35/jackpot/tickets/<ADDRESS>` | map JSON | user, explorer | Public address only |
| GET | `/v35/jackpot/proof/{periodType}/{periodEnd}` | `V35JackpotController` | Jackpot proof | `/v35/jackpot/proof/DAILY/123456` | map JSON | exchange, explorer, reviewer | Verify against official chain |

### Additional node/admin/sync endpoints

The server also exposes `/getNodes`, `/nodes/register`, `/nodes/resolve_from_to_block`, `/putNode`, `/pool/add`, `/pool/getPoolBlockMap`, `/pool/ConfirmedBalances`, `/pool/getPendingTxMap`, `/pool/getPoolAddress`, `/getTransactions`, `/allAccounts`, `/addresses`, totals, winners, and range lookup endpoints. These need a separate operator/admin section rather than prominent public exchange docs.

## 4. Wallet API inventory

Wallet port: `8082`. Wallet endpoints are local operational surfaces, not a public exchange API.

| Method | Path | Controller | Purpose and parameters | Example | Response/UI | Audience | Safety notes |
| --- | --- | --- | --- | --- | --- | --- | --- |
| GET | `/` | `MainController` | Wallet home | `/` | HTML | wallet user | Needs visual polish |
| GET | `/keys` | `ConductorController` | Generate/return key material | `/keys` | key payload | local user only | High risk: keep local-only; never publish real private keys |
| GET | `/account?address=<a>` | `ConductorController` | Account balances | `/account?address=<PUBLIC_ADDRESS>` | `Account` JSON | wallet user | Public address only |
| GET | `/dollar?address=<a>` | `ConductorController` | GOLD balance | `/dollar?address=<PUBLIC_ADDRESS>` | number | wallet user | Legacy naming |
| GET | `/stock?address=<a>` | `ConductorController` | Stock balance | `/stock?address=<PUBLIC_ADDRESS>` | number | wallet user | Legacy |
| GET | `/sendCoin?...&password=<private-key>` | `ConductorController` | Sign and submit transaction | `/sendCoin?sender=<ADDRESS>&recipient=<ADDRESS>&dollar=1&stock=0&reward=0&password=<PRIVATE_KEY>` | send result | local user only | Legacy/risky: private key in query URL and currently logged in controller; do not recommend for production integrations |
| GET | `/isTransactionAdd?sign=<signature>` | `ConductorController` | Transaction inclusion | `/isTransactionAdd?sign=<SIGNATURE>` | boolean | wallet user | Read-only |
| POST | `/TransactionAddBase`, `/isTransactionAddBase64`, `/TransactionAddBase64` | `ConductorController` | Transaction lookup/status | body `{"sign":"<SIGNATURE>"}` | JSON | wallet user | Prefer POST examples |
| GET | `/blockHash`, `/conductorBlock`, `/conductorHashTran` | `ConductorController` | Local explorer-style lookup | placeholder query values | JSON | local admin | Read-only |
| GET | `/senderTransactions`, `/customerTransactions`, counts, address ranges | `ConductorController` | Local history lookup | placeholder address/ranges | JSON | wallet user, local admin | Read-only |
| GET | `/transactions`; POST `/transactions` | `TransactionsController` | Wallet transaction UI | `/transactions` | HTML/form | wallet user | Preserve working form behavior |
| GET | `/create-account`; POST `/create-account` | `CreateAccountController` | Account creation UI | `/create-account` | HTML/form | wallet user | Private key handling must remain local |
| GET | `/v35-dashboard` | `V35DashboardController` | Prepared V35 dashboard | `/v35-dashboard` | HTML | wallet user, reviewer | Correctly states inactive status |
| GET | `/mining` | `MineController` | Legacy mining/staking/unstaking UI | `/mining` | HTML | wallet user | Must remain visibly legacy/pre-V35 |
| POST | `/customDiff` | `MineController` | Change legacy custom difficulty | body `customDiff=17` | redirect | local miner | `Mining.customDiff` starts at `Seting.V34_MIN_DIFF`; production default is `17` |
| POST | `/staking`, `/unstaking` | `MineController` | Sign local staking lifecycle transaction | form body | redirect | wallet user | Private key form; do not confuse with V35 reward policy |
| GET | `/seting` | `MainController` | Settings UI | `/seting` | HTML | local admin | Keep legacy route spelling |
| GET/POST | `/server`, `/poolhost` | `ServerController` | Local upstream/pool settings | form body `host=<URL>` | redirect | local admin | Document trusted endpoints only |
| GET | `/globalSize`, `/globalVersion`, `/difficulty` | `StorageController` | Upstream status | respective path | JSON | local admin | Useful diagnostics |
| GET | `/status`, `/size`, `/getNodes`, `/checkValidation` | wallet controllers | Local node state | respective path | JSON | local admin | Useful diagnostics |

## 5. API docs / exchange integration assessment

Existing public API documentation:

```text
Route:    /wallet_and_node_url
Template: website.citu-web-master/src/main/resources/templates/wallet_and_node_url.html
```

Assessment:

| Question | Result |
| --- | --- |
| Visible from home page | Yes, in `main.html` footer list |
| Visible in shared header | No |
| Node API included | Yes, substantial legacy list |
| Wallet API included | Yes |
| Exchange-relevant examples | Partly |
| Production/local host distinction | Inconsistent |
| V35 endpoints included | No |
| V35 deployment-state warning | No |
| Security warnings for wallet private keys | Insufficient |
| Markup quality | Needs cleanup; malformed closing tags and route-label inconsistencies exist |

Required next improvements:

1. Restore a visible `API / Exchanges` header link.
2. Split content into `Public node API`, `Exchange integration`, `Local wallet API`, and `V35 read-only API`.
3. Add `/size`, `/prevBlock`, `/balance`, transaction status, version, readiness, and V35 endpoint examples.
4. State production node base URL and local smoke URLs separately.
5. Mark `/keys` and wallet `/sendCoin` as local-only legacy surfaces.
6. Do not show real private keys or recommend private keys in URLs.
7. Warn that V35 endpoint availability depends on deployment of the reviewed server build and V35 remains inactive until the approved fork height.
8. Correct the customer range route label: implementation is `/findBlocksByCustomerInRange`, not `/findBlocksBySenderInRange`.

## 6. Local run instructions

### Server

```powershell
cd C:\citu\unitedStates_storage-master
.\mvnw.cmd test
.\mvnw.cmd clean package
java -jar target\unitedStates-0.0.423-SNAPSHOT.jar `
  --server.address=localhost `
  --server.port=8083 `
  "--spring.datasource.url=jdbc:h2:file:./target/smoke/server-db;DB_CLOSE_ON_EXIT=FALSE;AUTO_RECONNECT=TRUE"
```

Open `/size`, `/confirmReadiness`, `/version`, `/v35/protocol`, `/v35/supply`, `/v35/burn`, and `/v35/jackpot/status`.

### Wallet

```powershell
cd C:\citu\unitedStates_final-master
.\mvnw.cmd test
.\mvnw.cmd clean package
java -jar target\unitedStates-0.32.216-SNAPSHOT.jar `
  --server.address=localhost `
  --server.port=8082 `
  "--spring.datasource.url=jdbc:h2:file:./target/smoke/wallet-db;DB_CLOSE_ON_EXIT=FALSE;AUTO_RECONNECT=TRUE"
```

Open `/`, `/v35-dashboard`, `/mining`, `/seting`, `/status`, and `/size`.

### Website

```powershell
cd C:\citu\website.citu-web-master
.\mvnw.cmd test
.\mvnw.cmd clean package
java -jar target\website.citu-0.0.24-SNAPSHOT.jar `
  --server.address=localhost `
  --server.port=8080 `
  --server.ssl.enabled=false
```

Fallback: use port `8085`.

Important notes:

- use isolated local H2 paths for server and wallet;
- do not use live blockchain data for smoke tests;
- website runtime properties contain deployment-specific configuration and must be reviewed separately before publication or packaging;
- do not expose datasource files, keystores, or credentials.

## 7. Production release flow overview

1. Approve final UI/docs/navigation patch.
2. Retest screenshots and links locally.
3. Refresh live height and explicitly approve final fork height.
4. Create activation branch from clean `master`.
5. Change only server/wallet `V35_RESCUE_FORK_INDEX`.
6. Run server/wallet tests and packages.
7. Build final selected artifacts.
8. Generate SHA256 files and manifest.
9. Review and create final tag.
10. Fill RU / EN / ZH notices.
11. Deploy only after approval.
12. Notify operators/exchanges and follow monitoring runbook.

Founder burn remains separate.

## 8. Website route/navigation map

| Surface | Route | Template | Current header nav | Recommended nav | Readiness |
| --- | --- | --- | --- | --- | --- |
| Home | `/` | `main.html` | Yes | Main | Legacy home needs polish |
| API docs | `/wallet_and_node_url` | `wallet_and_node_url.html` | No | Main: `API / Exchanges` | Exists, needs rewrite |
| Explorer | `/conductor` | `conductor.html` | No | Main | Exists, preserve function |
| White paper | `/white_papper` | `white_papper.html` | No | Main/resources | Route spelling legacy |
| Charter | `/charter` | `charter.html` | No | Main/resources | Exists |
| Install | `/how_to_install` | `how_to_install.html` | No | Guides | Exists |
| Open account | `/how_to_open_an_account` | `how_to_open_an_account.html` | No | Guides | Exists |
| Mining guide | `/how_to_mining` | `how_to_mining.html` | No | Guides, mark legacy | Needs V35 boundary warning |
| Transaction guide | `/how_to_transaction` | `how_to_transaction.html` | No | Guides | Exists |
| Vote guide | `/how_to_vote_and_what_voting_types_are_there` | matching template | No | Guides | Exists |
| Problems | `/solving_common_problems` | matching template | No | Guides/support | Exists |
| Winners | `/winners` | `winners.html` | No | Explorer/resources | Exists |
| Daily rating | `/daily_rating` | `daily_rating.html` | No | Explorer/resources | Exists |
| V35 overview | `/v35` | `v35.html` | Yes | V35 | Strong base |
| Pre-activation | `/v35/preactivation-status` | matching template | Yes | V35 | Ready |
| Operator guide | `/v35/operator-guide` | matching template | Yes | V35 | Useful, expand API links |
| Tokenomics | `/v35/tokenomics` | matching template | Yes | V35 | Ready |
| Burn warning | `/v35/burn-warning` | matching template | Yes | V35 | Ready |
| Checksums | `/v35/checksums` | matching template | Yes | V35 | Placeholder-safe |
| Jackpot | `/v35/jackpot` | matching template | Yes | V35 | Ready |
| Founder burn proof | `/v35/founder-burn-proof` | matching template | Yes | V35 | Actual route; no `/v35/burn-proof` alias |
| FAQ | `/v35/faq` | matching template | No | V35 | Add |
| Mining | `/v35/mining` | matching template | No | V35 | Add |
| Burn policy | `/v35/burn` | matching template | No | V35 | Add |

Key issue: the shared website header currently favors V35-only links. Reintroduce the legacy public product surfaces without making the header crowded.

## 9. Unified design audit

### Production-ready or close

- official CITU emblem on shared V35 website header;
- dark V35 public styling;
- multilingual V35 safety wording;
- clear planning-only candidate `514291`;
- strong burn warnings and inactive status copy.

### Still demo-like

- wallet header contains the Bootstrap example SVG and `<title>Bootstrap</title>`;
- wallet navigation is a single raw dropdown;
- wallet mining is a sequence of raw Bootstrap forms with spelling errors;
- wallet V35 dashboard is functionally useful but visually a raw technical report;
- public API docs are a long light template with inconsistent examples and no V35 section;
- legacy website home/footer navigation is disconnected from the new V35 header.

### Keep as legacy but label clearly

- wallet `/mining`;
- wallet `/customDiff`;
- staking/unstaking forms;
- old mining guide details;
- Explorer light layout if redesign risk is high.

## 10. Unified design specification

### Visual direction

| Element | Specification |
| --- | --- |
| Background | deep navy/black: `#071018`, `#0b1620` |
| Surface | `#111f2a` cards, restrained `1px` border `#2a3942` |
| Gold accent | `#d6b45a`, hover `#f0cf73` |
| Text | primary `#f6f2e8`, secondary `#b7c0c6` |
| Danger/warning | amber panel for pre-activation; red only for burn-address warnings |
| Typography | system sans-serif; compact headings; no oversized marketing typography |
| Cards | maximum `8px` radius; no nested card stacks |
| Buttons | compact, clear commands; gold primary, neutral secondary |
| Responsive | wrap nav into grouped menu on narrow screens; tables horizontally scroll |

### Shared status badges

Show prominently:

- `V35 NOT ACTIVE YET`
- `NO FOUNDER BURN YET`
- `PLANNING CANDIDATE: 514291`

Never display `BURN_ADDRESS` as a public destination.

### Navigation structure

Main navigation:

- Home
- Explorer
- API / Exchanges
- Guides
- Charter
- White paper
- V35

V35 sub-navigation:

- Overview
- Status
- Operators
- Tokenomics
- Mining
- Burn policy
- Burn warning
- Jackpot
- Proof
- Checksums
- FAQ

### Redesign priority

1. Website shared header/navigation and API docs.
2. Wallet shared header and V35 dashboard.
3. Wallet mining legacy panel with visible default `17`.
4. Website home resource visibility.
5. Leave Explorer internals mostly unchanged, but add consistent navigation.

## 11. Risk analysis

| Risk | Mitigation |
| --- | --- |
| Accidental `Seting.java` edit | Audit diff before and after patch |
| Consensus logic changes | Limit next patch to templates, CSS, docs tests |
| Breaking wallet forms | Preserve routes, names, methods, and controller contracts |
| Hiding API docs | Restore `API / Exchanges` in main nav |
| Linking dead routes | Add route assertions for every nav link |
| Publishing `BURN_ADDRESS` as destination | Treat only as internal placeholder |
| Making V35 appear active | Keep badges and `Long.MAX_VALUE` language |
| Dry-run artifacts confused with release | Keep final tag/checksum placeholders explicit |
| Private key exposure | Mark wallet `/keys` and `/sendCoin` local-only; never use real key examples |
| Deployment config leakage | Review website runtime configuration separately; do not publish credentials |

## 12. Recommended exact next patch phase

Recommended next phase:

**Phase 10F-1C - unified navigation, API docs, and wallet visual polish patch; no activation and no deployment.**

Use the companion patch plan:

`CITU_V35_PHASE10F1B_NEXT_UI_PATCH_PLAN.md`

## 13. Confirmation no source code changed

Confirmed. Phase 10F-1B changed reports only.

## 14. Confirmation no tests changed or run

Confirmed. No tests were changed or run in Phase 10F-1B.

## 15. Confirmation no Seting.java changed

Confirmed. Neither server nor wallet `Seting.java` changed.

## 16. Confirmation V35 remains disabled

Confirmed:

```text
V35_RESCUE_FORK_INDEX = Long.MAX_VALUE
V35_BURN_ADDRESS = BURN_ADDRESS
```

## 17. Confirmation no activation or release action

No burn transaction, fork activation, jackpot payout, deployment, final tag, release artifact, or checksum action occurred.
