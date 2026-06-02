# CITU V35 Phase 11B-1 Post-Activation-Commit UI and Docs Status Fix Report

## 1. Executive Summary

Phase 11B-1 corrected wallet and website wording after the activation commit configured fork height `514291`.

The public state is now represented accurately:

- V35 activation is scheduled in the reviewed build at block `514291`
- V35 is not active yet while the live chain remains below `514291`
- before block `514291`, `v35Active=false` is expected
- GitHub Release upload and public checksum publication are pending
- no founder burn occurred
- `BURN_ADDRESS` is an internal placeholder, not a public transfer destination

No consensus, mining, jackpot payout, server, or `Seting.java` logic was changed.

## 2. Wallet Dashboard Fix

Updated wallet dashboard behavior:

- separates `activationScheduled=true` from `v35Active=false`
- no longer treats any finite fork index as proof that V35 is active now
- uses a safe inactive fallback when live chain status is unavailable
- displays fork height `514291`
- labels `BURN_ADDRESS` as placeholder and not configured for public use
- keeps the no-founder-burn warning

Wallet files changed:

- `unitedStates_final-master/src/main/java/International_Trade_Union/controllers/V35DashboardController.java`
- `unitedStates_final-master/src/main/resources/templates/home.html`
- `unitedStates_final-master/src/main/resources/templates/mining.html`
- `unitedStates_final-master/src/main/resources/templates/v35-dashboard.html`
- `unitedStates_final-master/src/test/java/International_Trade_Union/controllers/V35DashboardControllerTest.java`

## 3. Website Wording Fix

Updated website model and public templates:

- static public fork index is now `514291`, not `Long.MAX_VALUE`
- obsolete `planning-only`, candidate-not-final, and disabled-mode-JAR wording was removed
- pages state that activation is scheduled at block `514291`
- pages state that V35 remains inactive until the chain reaches `514291`
- GitHub Release upload and public checksum publication remain pending
- no founder burn occurred
- `BURN_ADDRESS` remains an internal placeholder

Website files changed:

- `website.citu-web-master/src/main/java/com/example/website/citu/controller/V35PublicController.java`
- `website.citu-web-master/src/main/resources/templates/blocks/footer.html`
- `website.citu-web-master/src/main/resources/templates/charter.html`
- `website.citu-web-master/src/main/resources/templates/main.html`
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
- `website.citu-web-master/src/main/resources/templates/wallet_and_node_url.html`
- `website.citu-web-master/src/main/resources/templates/white_papper.html`
- `website.citu-web-master/src/test/java/com/example/website/citu/controller/V35PublicControllerTest.java`

## 4. API Documentation Example

`/wallet_and_node_url` now shows:

```json
{"v35Active":false,"v35ForkIndex":514291}
```

The documentation explains that `false` is expected before block `514291`.

The legacy `/sendCoin?...password=...` flow remains labeled local-only and unsuitable for exchange integration.

## 5. Removed Stale Phrases

Removed from normal public pages:

- `V35_RESCUE_FORK_INDEX = Long.MAX_VALUE`
- `V35 is not active while V35_RESCUE_FORK_INDEX is Long.MAX_VALUE`
- `planning-only` for height `514291`
- `candidate is not final` for height `514291`
- `disabled-mode JAR` wording for the newly built release candidate

## 6. Verification

### Wallet

Commands:

```powershell
cd C:\citu\unitedStates_final-master
.\mvnw.cmd test
.\mvnw.cmd clean package
```

Result:

- wallet tests: `200/200 PASS`
- wallet clean package: `PASS`

### Website

Commands:

```powershell
cd C:\citu\website.citu-web-master
.\mvnw.cmd test
.\mvnw.cmd clean package
```

Result:

- website tests: `12/12 PASS`
- website clean package: `PASS`

Server build was not rerun because no server file changed.

## 7. Local Website Smoke

The rebuilt website JAR was started locally on `127.0.0.1:8095`, then stopped after verification.

All routes returned `HTTP 200`:

- `/`
- `/v35`
- `/wallet_and_node_url`
- `/v35/jackpot`
- `/v35/checksums`
- `/v35/burn-warning`
- `/white_papper`
- `/v35/preactivation-status`

For every checked route:

- `Long.MAX_VALUE` stale wording: absent
- `planning-only` stale wording: absent
- `514291` scheduled height: present
- `BURN_ADDRESS` warning: present

## 8. Constants Audit

| Project | `V35_RESCUE_FORK_INDEX` | `V35_BURN_ADDRESS` |
| --- | ---: | --- |
| Server | `514291` | `BURN_ADDRESS` |
| Wallet | `514291` | `BURN_ADDRESS` |

No `Seting.java` changed.

## 9. Phase 11B Artifact Impact

The Phase 11B node JAR remains valid because server code did not change.

The Phase 11B wallet JAR and release-folder checksums must be rebuilt before upload because wallet UI/controller code changed in Phase 11B-1.

Do not upload the existing Phase 11B package as final.

## 10. Safety Confirmation

- no `Seting.java` changed
- no consensus logic changed
- no mining logic changed
- no jackpot payout logic changed
- no burn transaction created
- no jackpot payout created
- no deploy
- no GitHub Release
- no upload
- no tag
- no push
- no commit
- existing `release-dryrun` artifacts were not modified in this phase

## 11. Recommendation

Run a separate Phase 11B-2 artifact refresh:

- rebuild the wallet JAR from the corrected UI code
- refresh the local release folder
- regenerate manifest and SHA256 files
- verify checksums
- optionally repeat local smoke
- do not upload until a separate Phase 11C approval

