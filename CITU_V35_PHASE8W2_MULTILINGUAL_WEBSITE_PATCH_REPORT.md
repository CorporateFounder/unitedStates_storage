# CITU V35 Phase 8W-2 - Multilingual Website Patch Report

## 1. Executive Summary

Phase 8W-2 localized the four V35 pre-activation public instruction pages into Russian (`RU`), English (`EN`), and Chinese (`ZH`).

The website does not have one consistent localization framework. To keep the patch small and safe, each affected page now contains clearly labeled `RU / EN / ZH` sections at the same route.

No server or wallet code was changed. V35 was not activated. No burn, fork, jackpot payout, deployment, final tag, or final release action was performed.

## 2. Existing Localization Structure Found

The website has partial legacy localization:

- `charter.html`
- `charter_eng.html`
- `charter_rus.html`

No shared locale router, message-bundle structure, or established Chinese template pattern was found for V35 pages.

Decision:

- Keep existing V35 routes unchanged.
- Use language-labeled sections inside each V35 public instruction page.
- Avoid introducing a new localization framework during pre-activation hardening.

## 3. Website Files Changed

Updated templates:

- `website.citu-web-master/src/main/resources/templates/v35-preactivation-status.html`
- `website.citu-web-master/src/main/resources/templates/v35-operator-guide.html`
- `website.citu-web-master/src/main/resources/templates/v35-checksums.html`
- `website.citu-web-master/src/main/resources/templates/v35-burn-warning.html`

Updated test:

- `website.citu-web-master/src/test/java/com/example/website/citu/controller/V35PublicControllerTest.java`

Added report:

- `CITU_V35_PHASE8W2_MULTILINGUAL_WEBSITE_PATCH_REPORT.md`

## 4. RU / EN / ZH Coverage Summary

Every localized public instruction page states in RU, EN, and ZH:

- V35 is prepared but not active yet.
- Candidate fork height `513332` is planning-only.
- Final fork height, tag, and checksums will be announced later.
- No founder burn has occurred.
- No user action is required yet.
- Do not send coins to unofficial or fake burn addresses.
- Use only official final release artifacts and checksums.
- Operators and exchanges must wait for final tagged artifacts.
- Dry-run artifacts are not a final production release.

## 5. Routes / Pages Updated

Existing routes remain unchanged:

- `/v35/preactivation-status`
- `/v35/operator-guide`
- `/v35/checksums`
- `/v35/burn-warning`

No header route change was required. Existing links continue to resolve to the localized pages.

## 6. Tests / Build Run And Result

Website tests:

```powershell
cd C:\citu\website.citu-web-master
.\mvnw.cmd test
```

Result:

- `PASS`
- tests: `9`
- failures: `0`
- errors: `0`

Website package:

```powershell
cd C:\citu\website.citu-web-master
.\mvnw.cmd clean package
```

Result:

- `PASS`
- tests during package: `9`
- artifact: `target/website.citu-0.0.24-SNAPSHOT.jar`

Known pre-existing Maven warning remains:

- duplicate `org.bouncycastle:bcprov-jdk15on:1.70` dependency declaration in website `pom.xml`

No `pom.xml` change was made.

## 7. Remaining Localization Gaps

- Earlier V35 overview, tokenomics, burn-policy, founder-proof, jackpot, mining, and FAQ pages remain English-only.
- Header navigation labels remain English-only.
- The site still has no unified localization framework.
- Final activation notices remain placeholders and must be published in RU / EN / ZH.

These gaps do not change protocol behavior, but they should be addressed before final public activation announcement.

## 8. Confirmation No Server / Wallet Code Changed

Confirmed.

No server or wallet code was changed in Phase 8W-2.

## 9. Confirmation V35 Remains Disabled

Confirmed:

- `V35_RESCUE_FORK_INDEX = Long.MAX_VALUE`
- `V35_BURN_ADDRESS = BURN_ADDRESS`

Candidate height `513332` remains planning-only.

## 10. Confirmation No Burn / Fork / Jackpot Action Was Performed

Confirmed:

- no founder burn transaction was created, signed, simulated, or broadcast;
- no fork activation was performed;
- no jackpot payout transaction was created.

## 11. Confirmation No Deployment / Tag / Final Release

Confirmed:

- no website deployment was performed;
- no final git tag was created;
- no final release was published;
- no operator or exchange production-upgrade instruction was issued.

## 12. Recommended Next Phase

Recommended next phase:

**Phase 8X - multilingual final public activation notice templates, placeholders only, no activation.**

Before final publication, consider a separate localization pass for the earlier V35 overview and policy pages.
