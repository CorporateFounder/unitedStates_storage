# CITU V35 Phase 8W - Website and Public Instruction Audit Report

## 1. Executive Summary

Phase 8W audited the public website V35 pages and added minimal pre-activation operational guidance pages.

The website already contained public V35 policy pages with strong disabled/pending wording. The missing public guidance areas were:

- consolidated pre-activation status;
- node and exchange operator instructions;
- checksum verification instructions;
- a focused fake burn-address warning.

Four public website pages were added. They state that V35 is prepared but not active yet, the candidate fork height is planning-only, no action is required yet, and only official artifacts/checksums should be used.

No server or wallet code was changed. No deployment was performed.

## 2. Website Files Inspected

Website structure:

- `website.citu-web-master/pom.xml`
- `website.citu-web-master/src/main/java/com/example/website/citu/controller/V35PublicController.java`
- `website.citu-web-master/src/main/java/com/example/website/citu/controller/V35FounderBurnProofApiClient.java`
- `website.citu-web-master/src/test/java/com/example/website/citu/controller/V35PublicControllerTest.java`
- `website.citu-web-master/src/main/resources/templates/blocks/header.html`
- `website.citu-web-master/src/main/resources/templates/v35.html`
- `website.citu-web-master/src/main/resources/templates/v35-tokenomics.html`
- `website.citu-web-master/src/main/resources/templates/v35-burn.html`
- `website.citu-web-master/src/main/resources/templates/v35-founder-burn-proof.html`
- `website.citu-web-master/src/main/resources/templates/v35-jackpot.html`
- `website.citu-web-master/src/main/resources/templates/v35-mining.html`
- `website.citu-web-master/src/main/resources/templates/v35-faq.html`

Public planning documents reviewed:

- `CITU_V35_PHASE8F_EXCHANGE_NOTICE_DRAFT.md`
- `CITU_V35_PHASE8F_NODE_OPERATOR_NOTICE_DRAFT.md`
- `CITU_V35_PHASE8F_WALLET_USER_NOTICE_DRAFT.md`
- `CITU_V35_PHASE8C_RELEASE_NOTES_FINAL.md`
- `CITU_V35_PHASE8G_FINAL_PRERELEASE_PACKAGE_APPROVAL_REPORT.md`
- `CITU_V35_PHASE6F4_ACTIVATION_COMMIT_DRYRUN_DIFF_PLAN.md`
- `CITU_V35_PHASE7H_FOUNDER_BURN_SNAPSHOT_SOURCE_FINALIZATION.md`
- `CITU_V35_PHASE7I_BURN_ADDRESS_APPROVAL_PROOF_EVIDENCE_CHECKLIST.md`

## 3. Website Files Changed

Updated:

- `website.citu-web-master/src/main/java/com/example/website/citu/controller/V35PublicController.java`
- `website.citu-web-master/src/main/resources/templates/blocks/header.html`
- `website.citu-web-master/src/main/resources/templates/v35.html`
- `website.citu-web-master/src/test/java/com/example/website/citu/controller/V35PublicControllerTest.java`

Added:

- `website.citu-web-master/src/main/resources/templates/v35-preactivation-status.html`
- `website.citu-web-master/src/main/resources/templates/v35-operator-guide.html`
- `website.citu-web-master/src/main/resources/templates/v35-checksums.html`
- `website.citu-web-master/src/main/resources/templates/v35-burn-warning.html`

Report:

- `CITU_V35_PHASE8W_WEBSITE_PUBLIC_INSTRUCTION_AUDIT_REPORT.md`

## 4. Existing V35 Website Instructions

The website already contained:

- V35 overview page.
- Tokenomics page.
- Mining page.
- Jackpot page.
- Burn policy page.
- Founder burn proof page with pending placeholders.
- FAQ page.
- Header links for the main V35 pages.
- Optional read-only founder burn API status integration.
- Warnings that V35 is not active while `V35_RESCUE_FORK_INDEX` is `Long.MAX_VALUE`.
- Warnings that founder burn has not happened yet.
- Separation of modeled burn values from actual burned supply.

The website did not yet provide a focused public page for:

- exchange/node operator pre-activation instructions;
- checksum verification;
- fake burn-address safety;
- candidate fork-height planning status.

## 5. Public Pages Added

### `/v35/preactivation-status`

Explains:

- V35 is prepared but not active yet.
- Candidate fork height `513332` is planning-only.
- Final height, tag, artifacts, and checksums will be announced later.
- No user, exchange, or node-operator action is required yet.

### `/v35/operator-guide`

Explains:

- No production upgrade is required yet.
- Operators must wait for the final official notice.
- Operators must not manually change constants.
- Operators must not use unofficial or dry-run artifacts.
- Final notice must include fork height, tag, checksums, timing, rollback, monitoring, and support details.

### `/v35/checksums`

Explains:

- Final checksums are not published yet.
- Future releases should include `SHA256SUMS.txt`.
- `SHA256SUMS.txt` should be verified using `SHA256SUMS.txt.sha256`.
- Artifacts must be checked against the final SHA-256 list.

### `/v35/burn-warning`

Explains:

- Founder burn has not been executed.
- No user action is required yet.
- Users must not send coins to unofficial or fake burn addresses.
- Public proof must include the exact approved address, snapshot, transaction hash, block, and official verification links.

## 6. Build / Test Results

Website test command:

```powershell
cd C:\citu\website.citu-web-master
.\mvnw.cmd test
```

Result:

- PASS
- tests: `9`
- failures: `0`
- errors: `0`

Website package command:

```powershell
cd C:\citu\website.citu-web-master
.\mvnw.cmd clean package
```

Result:

- PASS on sequential rerun
- tests during package: `9`
- artifact: `target/website.citu-0.0.24-SNAPSHOT.jar`

Operational note:

- The first package attempt ran concurrently with `mvnw test` and failed while cleaning `target/classes`.
- The package command was rerun sequentially after tests completed and passed.

Existing Maven warning remains:

- duplicate `org.bouncycastle:bcprov-jdk15on:1.70` dependency in website `pom.xml`.

No `pom.xml` change was made in Phase 8W.

## 7. Confirmation No Deployment

Confirmed.

No website deployment, server restart, public publication, or external release action was performed.

## 8. Confirmation V35 Remains Disabled

Confirmed:

- `V35_RESCUE_FORK_INDEX = Long.MAX_VALUE`
- `V35_BURN_ADDRESS = BURN_ADDRESS`

The website pages describe pre-activation status only.

## 9. Confirmation No Burn / Fork / Jackpot Action Was Performed

Confirmed:

- no founder burn transaction was created
- no founder burn transaction was signed
- no founder burn transaction was broadcast
- no V35 fork activation was performed
- no jackpot payout transaction was created
- no final git tag was created

## 10. Remaining Website / Public Instruction Gaps

- Final fork height is not approved or published.
- Final release tag is not created.
- Final artifact URLs and SHA-256 values are not published.
- Support/contact channel is still a placeholder.
- Deposit/withdrawal caution window is not finalized.
- Live V35 founder/supply/burn endpoints are not deployed on the queried node.
- Final public announcement must be created only after activation details are approved.

## 11. Recommended Next Phase

Recommended next phase:

Phase 10A - post-activation monitoring and rollback runbook finalization, no activation.

Alternative if public release wording should be finalized first:

Phase 8X - final public activation notice template, placeholders only, no activation.
