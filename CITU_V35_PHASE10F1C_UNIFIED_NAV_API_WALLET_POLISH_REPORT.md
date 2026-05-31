# CITU V35 Phase 10F-1C Unified Navigation, API Documentation, and Wallet Polish Report

## 1. Executive summary

Phase 10F-1C completed a targeted public UI and documentation polish pass without activation or deployment. The website now exposes the production resources alongside V35 pages through one branded navigation header, the API page has been rewritten for exchange and operator use, the homepage and Explorer have a cleaner production presentation, and the wallet has a branded operational shell with safer legacy mining wording.

V35 remains disabled. No consensus constants, server code, wallet production Java code, burn behavior, jackpot behavior, deployment, tag, artifact package, checksum, or commit were changed.

## 2. Safety audit

- Branch reviewed: `master`
- Server `V35_RESCUE_FORK_INDEX`: `Long.MAX_VALUE`
- Wallet `V35_RESCUE_FORK_INDEX`: `Long.MAX_VALUE`
- Server `V35_BURN_ADDRESS`: `BURN_ADDRESS`
- Wallet `V35_BURN_ADDRESS`: `BURN_ADDRESS`
- `git diff` for both server and wallet `Seting.java`: empty
- Activation diff present: no

## 3. Website navigation changes

Updated:

- `website.citu-web-master/src/main/resources/templates/blocks/header.html`
- `website.citu-web-master/src/main/resources/static/css/styles.css`

The shared branded header now exposes:

- Home
- Explorer: `/conductor`
- API / Exchanges: `/wallet_and_node_url`
- White paper: `/white_papper`
- Charter: `/charter`
- compact Guides menu
- compact V35 menu for public pre-activation pages

The official CITU emblem from the prior branding phase remains visible in the header.

## 4. Homepage and Explorer polish

Updated:

- `website.citu-web-master/src/main/resources/templates/main.html`
- `website.citu-web-master/src/main/resources/templates/conductor.html`

The homepage now presents a CITU network summary, production resource links, network indicators, and legacy chain details in a cleaner layout. An old duplicate Bootstrap-style footer block and raw SVG hook were removed.

The Explorer route now has a clear heading and framed search controls without rewriting its underlying logic.

## 5. API documentation changes

Updated:

- `website.citu-web-master/src/main/resources/templates/wallet_and_node_url.html`

The API documentation now separates:

- public node status endpoints
- block and balance endpoints
- transaction lookup and signed transaction submission
- legacy wallet-local endpoints
- V35 read-only status endpoints

Safety wording explicitly states that private keys and passwords must not be placed in URLs, logs, screenshots, support messages, or exchange integrations. Legacy wallet endpoints such as `/keys` and `/sendCoin?...password=...` are documented as local-only, not exchange integration APIs.

## 6. Wallet visual polish

Updated:

- `unitedStates_final-master/src/main/resources/templates/blocks/header.html`
- `unitedStates_final-master/src/main/resources/templates/blocks/footer.html`
- `unitedStates_final-master/src/main/resources/templates/v35-dashboard.html`
- `unitedStates_final-master/src/main/resources/templates/mining.html`
- `unitedStates_final-master/src/main/resources/static/css/citu-wallet.css`

The wallet now has a branded CITU header/footer shell. The V35 dashboard keeps the official logo and clearly shows:

- `V35 NOT ACTIVE YET`
- `PLANNING CANDIDATE: 514291`
- `NO FOUNDER BURN YET`
- pre-deployment endpoint availability warning

The legacy mining page now displays `Legacy default difficulty: 17` and the input placeholder `17 legacy default`. Existing operational form behavior was not changed.

## 7. Candidate and burn wording safety

- Public V35 planning candidate: `514291`
- Superseded candidates `513332` and `513858` were not found in public V35 templates.
- Founder burn is described as not having occurred.
- `BURN_ADDRESS` is described only as an internal protocol sink placeholder, not as a public address or instruction to send funds.

## 8. Test changes

Updated:

- `website.citu-web-master/src/test/java/com/example/website/citu/controller/V35PublicControllerTest.java`
- `unitedStates_final-master/src/test/java/International_Trade_Union/controllers/V35DashboardControllerTest.java`

Coverage added for:

- unified website navigation
- safe exchange/operator API documentation wording
- wallet branded CSS and logo usage
- V35 inactive dashboard badges
- legacy mining default difficulty wording
- removal of wallet Bootstrap sample title markup

## 9. Website verification

Commands run:

```powershell
cd C:\citu\website.citu-web-master
.\mvnw.cmd test
.\mvnw.cmd clean package
```

Result:

- Tests: `10/10` passed
- Package: `BUILD SUCCESS`

Maven reported the pre-existing duplicate `bcprov-jdk15on` dependency warning. It did not block verification.

## 10. Wallet verification

Commands run:

```powershell
cd C:\citu\unitedStates_final-master
.\mvnw.cmd test
.\mvnw.cmd clean package
```

Result:

- Tests: `200/200` passed
- Package: `BUILD SUCCESS`

Tracked wallet `target/` build-output churn was restored after verification. Maven reported pre-existing duplicate dependency warnings. They did not block verification.

## 11. Local website route smoke test

The packaged website was started locally with:

```powershell
java -jar C:\citu\website.citu-web-master\target\website.citu-0.0.24-SNAPSHOT.jar --server.address=127.0.0.1 --server.port=8085
```

The explicit localhost address was required because the default runtime address is not available on this machine. The following routes returned HTTP `200`:

- `/v35`
- `/v35/preactivation-status`
- `/v35/operator-guide`
- `/v35/checksums`
- `/v35/burn-warning`
- `/wallet_and_node_url`
- `/white_papper`
- `/charter`

The temporary website process was stopped after the probe. No deployment occurred.

## 12. Files changed in Phase 10F-1C

Website:

- `website.citu-web-master/src/main/resources/static/css/styles.css`
- `website.citu-web-master/src/main/resources/templates/blocks/header.html`
- `website.citu-web-master/src/main/resources/templates/conductor.html`
- `website.citu-web-master/src/main/resources/templates/main.html`
- `website.citu-web-master/src/main/resources/templates/wallet_and_node_url.html`
- `website.citu-web-master/src/test/java/com/example/website/citu/controller/V35PublicControllerTest.java`

Wallet:

- `unitedStates_final-master/src/main/resources/static/css/citu-wallet.css`
- `unitedStates_final-master/src/main/resources/templates/blocks/footer.html`
- `unitedStates_final-master/src/main/resources/templates/blocks/header.html`
- `unitedStates_final-master/src/main/resources/templates/mining.html`
- `unitedStates_final-master/src/main/resources/templates/v35-dashboard.html`
- `unitedStates_final-master/src/test/java/International_Trade_Union/controllers/V35DashboardControllerTest.java`

Report:

- `CITU_V35_PHASE10F1C_UNIFIED_NAV_API_WALLET_POLISH_REPORT.md`

## 13. Explicit confirmations

- Server consensus code changed: no
- Server `Seting.java` changed: no
- Wallet `Seting.java` changed: no
- V35 activated: no
- Burn transaction created, signed, simulated, or broadcast: no
- Jackpot payout transaction created: no
- Deployment performed: no
- Final tag created: no
- Release artifacts or checksums created: no
- Commit created: no

## 14. Manual retest checklist

Website:

- Open `/`
- Open `/conductor`
- Open `/wallet_and_node_url`
- Open `/white_papper`
- Open `/charter`
- Open `/v35` and the V35 menu pages
- Confirm header logo, compact menus, candidate `514291`, inactive warnings, and burn placeholder wording

Wallet:

- Open `/v35-dashboard`
- Open `/mining`
- Confirm branded header/footer, dashboard warning badges, logo, and `17 legacy default`

## 15. Recommended next phase

Perform a manual screenshot review of the locally run website and wallet UI. Keep activation, deployment, and release operations separate until the visual review is explicitly approved.

## 16. Residual cleanup note

The wallet still contains the unused legacy fragment `src/main/resources/templates/blocks/prevHedear.html` with old Bootstrap sample markup. A reference search found no active template inclusion of that fragment. It was intentionally left unchanged to keep this patch scoped.
