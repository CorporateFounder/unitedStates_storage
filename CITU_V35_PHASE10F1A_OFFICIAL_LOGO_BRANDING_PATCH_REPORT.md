# CITU V35 Phase 10F-1A - Official Logo Branding Patch Report

## 1. Executive summary

Phase 10F-1A applied the approved official black-and-gold CITU emblem to the public V35 website header and the wallet V35 dashboard. This is a UI branding patch only. V35 remains disabled.

The originally supplied path was absent. The user explicitly approved the intended near-match:

```text
C:\логотип\photo_2024-10-17_01-46-17.jpg
```

## 2. Official logo source path

Approved source:

```text
C:\логотип\photo_2024-10-17_01-46-17.jpg
```

Source image:

```text
Dimensions: 547 x 539 pixels
File size: 48,763 bytes
```

## 3. Copied asset paths

The approved source was copied without external hotlinking:

```text
website.citu-web-master/src/main/resources/static/images/citu-v35-logo.jpg
unitedStates_final-master/src/main/resources/static/images/citu-v35-logo.jpg
```

## 4. Website V35 logo/header changes

The shared website V35 header now uses:

```html
<img class="v35-brand-logo" th:src="@{/images/citu-v35-logo.jpg}" alt="Official CITU logo"/>
```

Because every exposed public V35 page uses the shared header, the official logo appears on:

- `/v35`
- `/v35/preactivation-status`
- `/v35/operator-guide`
- `/v35/checksums`
- `/v35/burn-warning`
- `/v35/tokenomics`
- `/v35/burn`
- `/v35/founder-burn-proof`
- `/v35/jackpot`
- `/v35/mining`
- `/v35/faq`

The shared header remains clean and branded. No raw example header or public fake burn address was added.

## 5. Wallet V35 logo/dashboard changes

Wallet static asset:

```text
unitedStates_final-master/src/main/resources/static/images/citu-v35-logo.jpg
```

The wallet `/v35-dashboard` template now displays the official emblem above the dashboard title without changing the general wallet layout.

## 6. Candidate 514291 confirmation

Public V35 templates continue to show planning candidate:

```text
514291
```

A targeted stale-value sweep found no public V35 template references to:

```text
513332
513858
```

## 7. Burn wording safety confirmation

Public V35 wording still states:

- V35 is not active yet;
- the founder burn has not occurred;
- the official burn address has not been published;
- users must not send funds to unofficial or fake burn addresses;
- `BURN_ADDRESS` is an internal sink placeholder, not a final user-facing burn address.

## 8. Files changed in Phase 10F-1A

Added:

- `website.citu-web-master/src/main/resources/static/images/citu-v35-logo.jpg`
- `unitedStates_final-master/src/main/resources/static/images/citu-v35-logo.jpg`
- `CITU_V35_PHASE10F1A_OFFICIAL_LOGO_BRANDING_PATCH_REPORT.md`

Updated:

- `website.citu-web-master/src/main/resources/templates/blocks/header.html`
- `website.citu-web-master/src/test/java/com/example/website/citu/controller/V35PublicControllerTest.java`
- `unitedStates_final-master/src/main/resources/templates/v35-dashboard.html`
- `unitedStates_final-master/src/test/java/International_Trade_Union/controllers/V35DashboardControllerTest.java`

The working tree also retains the earlier uncommitted Phase 10F-1 UI readiness patch.

## 9. Website test/package result

```text
.\mvnw.cmd test          PASS: 9 tests
.\mvnw.cmd clean package PASS: BUILD SUCCESS
```

## 10. Wallet test/package result

```text
.\mvnw.cmd test          PASS: 200 tests
.\mvnw.cmd clean package PASS: BUILD SUCCESS
```

Generated tracked wallet `target/` churn was restored after verification and is not part of this patch.

## 11. Constants audit

| Project | `V35_RESCUE_FORK_INDEX` | `V35_BURN_ADDRESS` |
| --- | --- | --- |
| Server | `Long.MAX_VALUE` | `BURN_ADDRESS` |
| Wallet | `Long.MAX_VALUE` | `BURN_ADDRESS` |

## 12. Confirmation no Seting.java changed

Confirmed. Neither server nor wallet `Seting.java` has a diff.

## 13. Confirmation V35 remains disabled

Confirmed. V35 remains disabled. Candidate `514291` remains planning-only.

## 14. Confirmation no activation or release action

No burn transaction, fork activation, jackpot payout, deployment, final tag, release artifact package, or checksum generation was performed.

## 15. Manual screenshot retest checklist

Website:

- `http://localhost:8085/v35`
- `http://localhost:8085/v35/preactivation-status`
- `http://localhost:8085/v35/operator-guide`
- `http://localhost:8085/v35/checksums`
- `http://localhost:8085/v35/burn-warning`
- confirm official black-and-gold emblem appears in the shared header;
- confirm candidate `514291` is planning-only;
- confirm inactive and no-burn warnings remain visible;
- confirm RU / EN / ZH sections remain readable.

Wallet:

- `http://localhost:8082/v35-dashboard`
- confirm official black-and-gold emblem appears above the dashboard title;
- confirm V35 inactive state remains visible;
- confirm `BURN_ADDRESS` is described as an internal placeholder.

## 16. Recommended next phase

Recommended next phase: **Phase 10F-2 - manual screenshot retest and UI sign-off, no activation and no deployment**.
