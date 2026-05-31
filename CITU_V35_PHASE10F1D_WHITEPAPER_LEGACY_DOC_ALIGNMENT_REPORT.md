# CITU V35 Phase 10F-1D White Paper and Legacy Documentation Alignment Report

## 1. Executive summary

Phase 10F-1D aligned the public protocol documentation without activation or deployment. The website `/white_papper` route now presents the reviewed V35 pre-activation model instead of legacy reward and hybrid-points mechanics. Historical mining guides and charter variants that intentionally retain pre-V35 formulas now display clear legacy warnings.

Wallet mining controls remain functional and are explicitly labeled as legacy / pre-V35. No consensus logic, `Seting.java`, burn behavior, jackpot behavior, deployment, tag, artifact, checksum, or commit was changed.

## 2. Safety audit

- Branch reviewed: `master`
- Server `V35_RESCUE_FORK_INDEX`: `Long.MAX_VALUE`
- Wallet `V35_RESCUE_FORK_INDEX`: `Long.MAX_VALUE`
- Server `V35_BURN_ADDRESS`: `BURN_ADDRESS`
- Wallet `V35_BURN_ADDRESS`: `BURN_ADDRESS`
- `git diff` for both server and wallet `Seting.java`: empty
- Activation diff present: no

## 3. Outdated documentation found

The previous website `/white_papper` template presented legacy / pre-V35 mechanics as if they were current:

- multiplier reward formula
- hybrid PoW / PoS points text
- staking points
- transaction points
- deterministic random range `0..170`
- `difficulty * 15`
- manual difficulty workflow

Additional historical mechanics remain intentionally preserved in:

- website `how_to_mining.html`
- website `charter_eng.html`
- website `charter_rus.html`
- wallet `mining.html`
- wallet `charter_eng.html`
- wallet `charter_rus.html`
- wallet `corporate-charter.html`

Older explanatory strings also remain in website controller guide text. They are reached through pages now labeled as legacy / pre-V35. No production logic was changed.

## 4. White paper changes

Updated:

- `website.citu-web-master/src/main/resources/templates/white_papper.html`
- `website.citu-web-master/src/main/resources/templates/blocks/header.html`
- `website.citu-web-master/src/main/resources/templates/main.html`

The `/white_papper` route is now a V35 pre-activation protocol paper. Shared navigation and the homepage now label the route `V35 protocol paper`.

The replaced public paper documents:

- V35 is prepared but not active yet
- planning candidate `514291` is not final
- fresh `/size` approval is required before activation
- `200` second target block time
- `432` blocks per day
- `8 GOLD` total reward per block
- `6 GOLD` miner reward
- `2 GOLD` jackpot reserve
- founder reward becomes `0 GOLD`
- manual difficulty controls are legacy-only after V35
- stronger-chain selection after V35 uses `cumulativeWork`
- V35 endpoints become production-relevant only after reviewed code is deployed
- no founder burn has occurred
- official burn address has not been published
- final tag, artifacts, and checksums require approval

## 5. Legacy / pre-V35 labels added

Website:

- `website.citu-web-master/src/main/resources/templates/how_to_mining.html`
- `website.citu-web-master/src/main/resources/templates/charter_eng.html`
- `website.citu-web-master/src/main/resources/templates/charter_rus.html`

Wallet:

- `unitedStates_final-master/src/main/resources/templates/mining.html`
- `unitedStates_final-master/src/main/resources/templates/charter_eng.html`
- `unitedStates_final-master/src/main/resources/templates/charter_rus.html`
- `unitedStates_final-master/src/main/resources/templates/corporate-charter.html`

The banners state that preserved historical formulas are legacy / pre-V35 and are not the V35 public model.

## 6. Wallet mining legacy safety

The wallet `/mining` controls remain functional. The warning now makes these boundaries explicit:

- this is legacy / pre-V35 UI
- manual difficulty is legacy-only after V35
- default difficulty is `17`
- input placeholder is `17 legacy default`
- the controls do not define the V35 mining or reward model
- prepared V35 status belongs on `/v35-dashboard`

No mining logic was changed.

## 7. Candidate and burn wording safety

- Public planning candidate: `514291`
- Superseded candidates `513332` and `513858` are not present in the V35 protocol paper.
- No founder burn is claimed.
- The official burn address is explicitly described as not published.
- The internal sink placeholder is not presented as a public burn destination.

## 8. Test changes

Updated:

- `website.citu-web-master/src/test/java/com/example/website/citu/controller/V35PublicControllerTest.java`
- `unitedStates_final-master/src/test/java/International_Trade_Union/controllers/V35DashboardControllerTest.java`

Added regression coverage for:

- V35-first protocol paper content
- candidate `514291`
- V35 schedule and reward split
- `cumulativeWork`
- founder burn safety wording
- removal of legacy `difficulty * 15` and `0..170` presentation from the public paper
- website legacy-guide and charter banners
- wallet mining boundary wording

## 9. Website tests and package

Commands run:

```powershell
cd C:\citu\website.citu-web-master
.\mvnw.cmd test
.\mvnw.cmd clean package
```

Result:

- Tests: `11/11` passed
- Package: `BUILD SUCCESS`

The first clean-package attempt found a JAR held by a prior local smoke-test process. After local-only smoke processes were stopped, clean package passed.

## 10. Wallet tests and package

Commands run:

```powershell
cd C:\citu\unitedStates_final-master
.\mvnw.cmd test
.\mvnw.cmd clean package
```

Result:

- Tests: `200/200` passed
- Package: `BUILD SUCCESS`

The first clean-package attempt found a JAR held by a prior local smoke-test process. After local-only smoke processes were stopped, clean package passed. Tracked wallet `target/` churn was restored after verification.

## 11. Local visual verification

The packaged website was started locally with a localhost-only override:

```powershell
java -jar C:\citu\website.citu-web-master\target\website.citu-0.0.24-SNAPSHOT.jar --server.address=127.0.0.1 --server.port=8085
```

Visual checks completed:

- `/white_papper`: V35-first protocol paper renders with official branding, inactive status, candidate `514291`, and reward schedule.
- `/how_to_mining`: legacy / pre-V35 warning banner renders above historical calculators.
- `/charter_eng`: legacy / pre-V35 protocol banner renders above retained charter text.

The temporary local website process was stopped. No deployment occurred.

## 12. Exact files changed in Phase 10F-1D

Website:

- `website.citu-web-master/src/main/resources/templates/white_papper.html`
- `website.citu-web-master/src/main/resources/templates/blocks/header.html`
- `website.citu-web-master/src/main/resources/templates/main.html`
- `website.citu-web-master/src/main/resources/templates/how_to_mining.html`
- `website.citu-web-master/src/main/resources/templates/charter_eng.html`
- `website.citu-web-master/src/main/resources/templates/charter_rus.html`
- `website.citu-web-master/src/test/java/com/example/website/citu/controller/V35PublicControllerTest.java`

Wallet:

- `unitedStates_final-master/src/main/resources/templates/mining.html`
- `unitedStates_final-master/src/main/resources/templates/charter_eng.html`
- `unitedStates_final-master/src/main/resources/templates/charter_rus.html`
- `unitedStates_final-master/src/main/resources/templates/corporate-charter.html`
- `unitedStates_final-master/src/test/java/International_Trade_Union/controllers/V35DashboardControllerTest.java`

Report:

- `CITU_V35_PHASE10F1D_WHITEPAPER_LEGACY_DOC_ALIGNMENT_REPORT.md`

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

## 14. Manual retest URLs

Website:

- `http://127.0.0.1:8085/white_papper`
- `http://127.0.0.1:8085/how_to_mining`
- `http://127.0.0.1:8085/charter_eng`
- `http://127.0.0.1:8085/charter_rus`
- `http://127.0.0.1:8085/v35`

Wallet:

- `http://127.0.0.1:8082/mining`
- `http://127.0.0.1:8082/v35-dashboard`

## 15. Recommended next phase

Perform a manual screenshot review of the V35 protocol paper and legacy banners. Keep activation, deployment, and release operations separate until the documentation presentation is explicitly approved.
