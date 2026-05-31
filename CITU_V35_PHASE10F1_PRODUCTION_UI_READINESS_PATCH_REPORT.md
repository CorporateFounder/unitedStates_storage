# CITU V35 Phase 10F-1 - Production UI Readiness Patch Report

## 1. Executive summary

Phase 10F-1 updated the public V35 website and wallet-facing V35 guidance without activating V35.

The website V35 pages now use a shared dark branded CITU presentation layer, the existing CITU logo asset, a clean V35 navigation bar, the current planning candidate `514291`, and safer public burn wording.

The wallet keeps its existing visible `V35 dashboard` menu item and now labels legacy mining controls clearly. The wallet dashboard explains expected pre-activation endpoint behavior and treats `BURN_ADDRESS` as an internal placeholder rather than a public destination.

## 2. Manual screenshot issues found

The patch addressed:

- V35 public pages looked like older technical templates.
- public pages still displayed stale candidate `513332`.
- public burn wording could make `BURN_ADDRESS` look like a final destination.
- V35 pre-activation and no-burn warnings needed stronger prominence.
- wallet mining controls needed legacy / pre-V35 labeling.
- wallet dashboard needed clearer explanation of expected `false` endpoint state.
- website local smoke instructions needed an `8085` fallback.

## 3. Files changed

Website:

- `website.citu-web-master/src/main/java/com/example/website/citu/controller/V35PublicController.java`
- `website.citu-web-master/src/main/resources/static/css/styles.css`
- `website.citu-web-master/src/main/resources/templates/blocks/header.html`
- `website.citu-web-master/src/main/resources/templates/blocks/footer.html`
- `website.citu-web-master/src/main/resources/templates/v35.html`
- `website.citu-web-master/src/main/resources/templates/v35-tokenomics.html`
- `website.citu-web-master/src/main/resources/templates/v35-burn.html`
- `website.citu-web-master/src/main/resources/templates/v35-founder-burn-proof.html`
- `website.citu-web-master/src/main/resources/templates/v35-jackpot.html`
- `website.citu-web-master/src/main/resources/templates/v35-mining.html`
- `website.citu-web-master/src/main/resources/templates/v35-faq.html`
- `website.citu-web-master/src/main/resources/templates/v35-preactivation-status.html`
- `website.citu-web-master/src/main/resources/templates/v35-operator-guide.html`
- `website.citu-web-master/src/main/resources/templates/v35-checksums.html`
- `website.citu-web-master/src/main/resources/templates/v35-burn-warning.html`
- `website.citu-web-master/src/test/java/com/example/website/citu/controller/V35PublicControllerTest.java`

Wallet:

- `unitedStates_final-master/src/main/java/International_Trade_Union/controllers/V35DashboardController.java`
- `unitedStates_final-master/src/main/resources/templates/mining.html`
- `unitedStates_final-master/src/main/resources/templates/v35-dashboard.html`
- `unitedStates_final-master/src/test/java/International_Trade_Union/controllers/V35DashboardControllerTest.java`

Local instructions:

- `CITU_V35_PHASE10F0_LOCAL_THREE_APP_SMOKE_TEST_INSTRUCTIONS.md`

## 4. Website V35 redesign summary

- Added shared `v35-public` styling for all exposed public V35 templates.
- Reused the existing CITU logo asset.
- Added a restrained dark protocol-information layout with readable cards and tables.
- Replaced the older technical header with a clean CITU navigation bar.
- Updated the footer with a clear pre-activation warning.

## 5. Candidate height update

Visible public V35 candidate references were updated:

```text
513332 / 513858 -> 514291
```

Public copy states that `514291` is planning-only. Final height still requires explicit approval and a fresh `/size` check.

## 6. Burn placeholder safety changes

Public wording now states:

- official burn address has not been published yet;
- do not send coins to any unofficial burn address;
- founder burn has not occurred yet;
- final burn proof requires published transaction and block proof.

Where retained for protocol explanation, `BURN_ADDRESS` is labeled as an internal protocol sink name or placeholder, not a final public instruction.

## 7. Website navigation fixes

The V35 header now exposes:

- V35 overview
- Pre-activation status
- Operator guide
- Tokenomics
- Burn warning
- Checksums
- Jackpot
- Burn proof

## 8. Wallet menu fix

The wallet menu already contained a visible:

```text
V35 dashboard -> /v35-dashboard
```

No duplicate link was added. Focused wallet tests continue to protect its presence.

## 9. Wallet legacy mining / custom difficulty warning

The wallet `/mining` page now displays a visible legacy / pre-V35 warning:

- manual difficulty is legacy-only after V35;
- V35 remains disabled while `V35_RESCUE_FORK_INDEX = Long.MAX_VALUE`;
- staking, unstaking, and legacy controls must not be confused with V35 reward rules.

## 10. Wallet V35 dashboard explanation update

The wallet dashboard now explains:

- `V35 active: false` is expected before fork activation;
- unavailable or `false` read-only endpoint state from `http://194.87.236.238:82` is expected until the reviewed V35 server build is deployed;
- endpoint state does not mean the local wallet build failed;
- no founder burn occurred until signed transaction hash and burn block proof are published;
- `BURN_ADDRESS` is not a public burn instruction.

## 11. Local port 8085 fallback instruction

The local smoke instructions now include:

```powershell
java -jar target\website.citu-0.0.24-SNAPSHOT.jar --server.address=localhost --server.port=8085 --server.ssl.enabled=false
```

The `8085` fallback was exercised locally. Required V35 routes returned HTTP `200`.

## 12. Website test / package result

```text
.\mvnw.cmd test          PASS: 9 tests
.\mvnw.cmd clean package PASS: BUILD SUCCESS
```

## 13. Wallet test / package result

```text
.\mvnw.cmd test          PASS: 200 tests
.\mvnw.cmd clean package PASS: BUILD SUCCESS
```

## 14. Constants audit

| Project | `V35_RESCUE_FORK_INDEX` | `V35_BURN_ADDRESS` |
| --- | --- | --- |
| Server | `Long.MAX_VALUE` | `BURN_ADDRESS` |
| Wallet | `Long.MAX_VALUE` | `BURN_ADDRESS` |

## 15. Confirmation no Seting.java changed

Confirmed. Neither server nor wallet `Seting.java` was changed.

## 16. Confirmation V35 remains disabled

Confirmed. V35 remains disabled. Candidate `514291` is planning-only.

## 17. Confirmation no activation or release action

No burn transaction, fork activation, jackpot payout, deployment, final tag, release artifact package, or checksum generation was performed.

## 18. Manual screenshot checklist for retest

Retest website:

- `http://localhost:8085/v35`
- `http://localhost:8085/v35/preactivation-status`
- `http://localhost:8085/v35/operator-guide`
- `http://localhost:8085/v35/checksums`
- `http://localhost:8085/v35/burn-warning`
- RU / EN / ZH sections
- CITU logo and header navigation
- `514291` planning-only text
- official burn address not published warning

Retest wallet:

- `http://localhost:8082/v35-dashboard`
- `http://localhost:8082/mining`
- visible menu link to `/v35-dashboard`
- expected inactive-state explanation
- legacy mining / custom difficulty warning

Automated browser screenshot capture was not run because Playwright is not installed in this workspace. Manual screenshot review remains required.

## 19. Recommended next phase

Recommended next phase: **Phase 10F-2 - manual screenshot retest and UI sign-off, no activation and no deployment**.

