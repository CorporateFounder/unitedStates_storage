# CITU V35 Phase 6F-1 Final Fork-Index Activation Plan

Phase: 6F-1 only  
Scope: final V35 fork-index activation plan  
Code changes: none  
V35 activation: not performed  
Real fork index: not selected  
Founder burn transaction: not created  
Jackpot payout transaction: not created

## 1. Exact Reports / Files Read

Required Phase 6 reports:

- `CITU_V35_PHASE6A_PRE_ACTIVATION_HARDENING_PLAN.md`
- `CITU_V35_PHASE6B1_LIVE_CONTROLLER_DATABASE_HARDENING_REPORT.md`
- `CITU_V35_PHASE6C1_OLD_CHAIN_VALIDATION_BASELINE_REPORT.md`
- `CITU_V35_PHASE6C2_HISTORICAL_CHAIN_VALIDATION_REPORT.md`
- `CITU_V35_PHASE6D1_SIMULATED_FORK_DRY_RUN_REPORT.md`
- `CITU_V35_PHASE6E_RELEASE_ROLLBACK_EXCHANGE_CHECKLIST.md`

Related V35 reports reviewed for activation dependencies:

- `CITU_V35_PHASE2B_REWARD_WIRING_REPORT.md`
- `CITU_V35_PHASE3B0_POW_HELPERS_REPORT.md`
- `CITU_V35_PHASE3B4B0_CHAIN_SCORING_HELPERS_REPORT.md`
- `CITU_V35_PHASE3B4B4B_TOURNAMENT_SERVICE_REPORT.md`
- `CITU_V35_PHASE4E1G_JACKPOT_CONTROLLER_STORAGE_INTEGRATION_REPORT.md`
- `CITU_V35_PHASE5C2C_SUPPLY_API_ENDPOINTS_REPORT.md`
- `CITU_V35_PHASE5D1_SENTINEL_BURN_ADDRESS_REPLACEMENT_REPORT.md`
- `CITU_V35_PHASE5E1_FOUNDER_BURN_SNAPSHOT_REPORT.md`
- `CITU_V35_PHASE5F2_WEBSITE_V35_PUBLIC_PAGES_REPORT.md`
- `CITU_V35_PHASE5F3B_WALLET_DASHBOARD_LIVE_API_REPORT.md`

Constants inspected:

- `unitedStates_final-master/src/main/java/International_Trade_Union/setings/Seting.java`
- `unitedStates_storage-master/src/main/java/International_Trade_Union/setings/Seting.java`

Confirmed:

- Wallet: `V35_RESCUE_FORK_INDEX = Long.MAX_VALUE`
- Server: `V35_RESCUE_FORK_INDEX = Long.MAX_VALUE`
- Wallet: `V35_BURN_ADDRESS = "BURN_ADDRESS"`
- Server: `V35_BURN_ADDRESS = "BURN_ADDRESS"`

## 2. Current Readiness Summary

V35 core implementation is ready for final operational hardening, but not ready for activation today.

Implemented and tested in prior phases:

- V35 reward model: `8.00 GOLD/block = 6.00 miner + 2.00 jackpot + 0.00 founder`.
- V35 target-based PoW helpers and fork-gated validation.
- V35 cumulativeWork chain scoring and server tournament priority paths.
- Jackpot proof, builder, mining creation, validation, accounting, storage-style integration, APIs, and wallet dashboard live jackpot integration.
- `BURN_ADDRESS` sentinel burn sink and after-V35 no-spend rule.
- Read-only supply, burn, founder, founder burn snapshot, and jackpot APIs.
- Wallet V35 dashboard and public website V35 pages.
- Simulated V35 fork dry-run tests.
- Old-chain baseline compatibility tests.
- Historical-chain runner/manual replay plan.
- Release, rollback, and exchange coordination checklist.

Still not complete before activation:

- Full replay against a copied live historical chain.
- Exchange/node coordination and upgrade window.
- Manual founder burn proof package and burn execution decision.
- Final release artifacts/checksums.
- Real fork-index selection.
- Final activation commit that changes only wallet/server `Seting.java`.

## 3. Activation Prerequisites

Do not select or commit a real fork index until all of the following are complete:

- Git worktree is clean except for the intentional activation commit when that phase begins.
- All V35 reports are committed.
- Wallet `.\mvnw.cmd test` passes.
- Wallet `.\mvnw.cmd clean package` passes.
- Server `.\mvnw.cmd test` passes.
- Server `.\mvnw.cmd clean package` passes.
- Website `.\mvnw.cmd test` passes.
- Website `.\mvnw.cmd clean package` passes.
- Simulated V35 fork dry-run remains passing.
- Historical replay against a copied live chain is completed, or the team explicitly accepts it as a manual release gate that must be run before publication.
- Release/rollback/exchange checklist is approved.
- Exchange/node operator coordination plan is ready.
- Founder burn timing decision is made.
- Founder snapshot endpoint has been verified against the chosen node.
- Release candidate artifacts are prepared.
- Release artifact hashes/checksums are ready to publish.
- Emergency communication channel and support contact are ready.
- Monitoring plan for first V35 blocks is staffed.

## 4. Fork-Index Selection Formula

Activation must be by block index, not by date alone.

V35 timing constants:

- Target block time: `200` seconds.
- Blocks per day: `432`.
- `7` days is approximately `3024` blocks.
- `14` days is approximately `6048` blocks.
- `21` days is approximately `9072` blocks.

Selection formula:

```text
currentHeight = latest confirmed live chain height
chosenBlockOffset = upgradeWindowDays * 432
V35_RESCUE_FORK_INDEX = currentHeight + chosenBlockOffset
```

Examples:

```text
7-day window:  V35_RESCUE_FORK_INDEX = currentHeight + 3024
14-day window: V35_RESCUE_FORK_INDEX = currentHeight + 6048
21-day window: V35_RESCUE_FORK_INDEX = currentHeight + 9072
```

The announcement must publish both:

- exact `V35_RESCUE_FORK_INDEX`;
- estimated activation date/time, calculated from current height and observed block cadence.

The block index is authoritative. The date/time estimate is informational.

## 5. Recommended Upgrade Window

Minimum recommended upgrade window:

```text
14 days = currentHeight + 6048 blocks
```

Use a longer window if any coordination risk remains:

```text
21 days = currentHeight + 9072 blocks
```

Recommendation:

- Use `14 days` only if full historical replay passes, release artifacts are stable, and exchanges/nodes acknowledge the schedule quickly.
- Use `21 days` if exchange readiness, live-chain replay, or public communication needs more margin.

Do not choose the final index in this phase.

## 6. Exact Activation Workflow

1. Finish all hardening phases and commit all reports.
2. Copy live node chain data to a backup/replay directory.
3. Run the historical validation runner against the copied live chain only.
4. Record replay result: block count, first height/hash, last height/hash, and validation result.
5. Run wallet, server, and website tests/packages.
6. Prepare a release candidate while `V35_RESCUE_FORK_INDEX` remains `Long.MAX_VALUE`.
7. Verify read-only V35 APIs, wallet dashboard, and website V35 pages.
8. Select a proposed upgrade window from current live height, but do not commit it yet.
9. Notify exchanges and node operators with the proposed index/date package.
10. Publish public V35 upgrade notice.
11. Wait for exchange/node feedback.
12. Perform founder burn according to the approved timing plan, if the burn is scheduled before fork activation.
13. Publish founder burn proof package.
14. Perform final pre-fork go/no-go review.
15. Create the final activation commit by changing only wallet/server `Seting.java`.
16. Re-run wallet/server/website tests and packages.
17. Tag and publish final release artifacts and checksums.
18. Confirm exchanges/nodes are upgraded before the fork window.
19. Monitor the last legacy blocks.
20. Monitor the first V35 block and first post-fork confirmations.
21. Publish post-fork confirmation with first V35 block hash and observed reward model.

## 7. Exchange / Node Operator Instructions

The technical package for exchanges and node operators must include:

- Release version.
- Download links.
- Artifact hashes/checksums.
- Exact `V35_RESCUE_FORK_INDEX`.
- Estimated activation date/time.
- Upgrade deadline.
- Wallet and server components that must be upgraded.
- Summary of consensus changes:
  - `8.00 GOLD/block` total emission.
  - `6.00 GOLD` instant miner reward.
  - `2.00 GOLD` jackpot reserve/payout policy.
  - `0.00 GOLD` founder reward.
  - target PoW: `hash <= target`.
  - cumulativeWork chain selection.
  - `BURN_ADDRESS` no-spend after V35.
- Statement that transaction and block formats are unchanged.
- Deposit/withdrawal caution recommendation around fork time.
- How to verify they are on the correct version.
- How to verify post-fork chain:
  - first V35 block height;
  - first V35 block hash;
  - first V35 block reward;
  - no founder reward;
  - cumulativeWork and target PoW active.
- Support contact placeholder.
- Emergency communication channel.

Suggested exchange timeline:

- T-14 days or earlier: initial technical notice.
- T-7 days: upgrade reminder.
- T-3 days: final technical reminder.
- T-1 day: optional deposit/withdrawal caution notice.
- Fork day: live monitoring and support.
- T+1 day: post-fork confirmation package.

## 8. Founder Burn Timing Recommendation

Founder burn options:

### Option A: Burn Before Fork Announcement

Pros:

- Public supply proof exists before upgrade marketing.
- Simplifies announcement numbers.

Cons:

- Founder burn is irreversible before exchange/node schedule is locked.
- If release is delayed, the burn has already happened.
- Harder to coordinate burn proof with final fork date.

### Option B: Announce V35 First, Then Burn Before Fork

Pros:

- Exchanges and nodes receive the protocol plan before the irreversible burn.
- Burn can occur during the upgrade window.
- Burn proof can be published before activation.
- If burn proof is inconsistent, the fork can still be delayed before activation.

Cons:

- Requires precise operational coordination during the release window.
- The final activation must pause if the burn proof is not clean.

### Option C: Burn After Fork

Pros:

- Avoids pre-fork irreversible burn.

Cons:

- Weakens trust at activation.
- Leaves founder reserve unresolved when V35 starts.
- Public supply messaging remains incomplete.

Recommended timing:

```text
Option B: announce V35 first, then perform the manual founder burn before the fork block.
```

Recommended operating rule:

- Perform the burn after release candidate validation and initial exchange/node announcement.
- Complete and publish burn proof before the final activation commit or, at latest, before the final T-3 day reminder.
- If the burn proof is missing, inconsistent, or disputed, delay the fork index in a new release before activation.

Founder burn proof must include:

- founder address;
- `BURN_ADDRESS`;
- snapshot height;
- snapshot block hash;
- founder balance before burn;
- recommended `80%` burn amount;
- burn transaction hash;
- burn block index;
- burn block hash;
- founder balance after burn;
- burned supply;
- circulating supply;
- annual emission;
- inflation after burn.

## 9. Final `Seting.java` Change Plan

The final activation commit must be tiny and explicit.

Files to edit later:

- `unitedStates_final-master/src/main/java/International_Trade_Union/setings/Seting.java`
- `unitedStates_storage-master/src/main/java/International_Trade_Union/setings/Seting.java`

Only change:

```java
long V35_RESCUE_FORK_INDEX = Long.MAX_VALUE;
```

to:

```java
long V35_RESCUE_FORK_INDEX = <selected_real_fork_index>;
```

Do not change in that activation commit:

- `V35_BURN_ADDRESS`
- reward amounts
- jackpot rules
- PoW rules
- difficulty rules
- transaction format
- block format
- mining code
- validation code
- controllers
- UI/templates
- website content
- `pom.xml`

After the final `Seting.java` change:

- run wallet tests and package;
- run server tests and package;
- run website tests and package;
- confirm wallet/server fork index values match exactly;
- create the final activation commit;
- tag the release;
- publish artifacts and checksums.

## 10. Pre-Fork Checklist

Final 24-hour checklist:

- Exchanges reminded.
- Node operators reminded.
- Upgrade deadline re-posted.
- Current live height checked.
- Estimated activation time recalculated.
- Final release hashes/checksums published.
- Deposit/withdrawal caution notice issued if exchanges choose to pause.
- Live chain backup created.
- Monitoring dashboard ready.
- Support contact staffed.
- Emergency communication channel active.
- Founder burn proof package published, if burn is scheduled before fork.
- `/v35/supply`, `/v35/protocol`, `/v35/burn`, `/v35/founder`, `/v35/founder/burn-snapshot`, and jackpot endpoints sanity-checked.
- Wallet `/v35-dashboard` sanity-checked.
- Website `/v35` pages sanity-checked.
- Go/no-go decision recorded.

## 11. Fork-Day Monitoring Checklist

Monitor and record:

- Last legacy block index/hash.
- First V35 block index/hash.
- First V35 block reward:
  - `6.00 GOLD` miner reward.
  - `0.00 GOLD` founder reward.
- No invalid founder reward accepted.
- V35 target PoW active.
- Invalid post-fork PoW rejected.
- cumulativeWork chain selection sane.
- difficulty target values sane.
- jackpot ticket/payout accounting sane.
- jackpot payout creation skipped unless proof context is available.
- `BURN_ADDRESS` no-spend active.
- sending to `BURN_ADDRESS` remains allowed.
- supply API sane.
- founder/burn API sane.
- jackpot API sane.
- wallet dashboard sane.
- website info matches actual activation.
- exchanges confirm sync.
- first post-fork block hashes and cumulativeWork recorded.

## 12. Go / No-Go Criteria

Do not activate if any of these are true:

- Historical replay against copied live chain fails.
- Simulated fork dry-run fails.
- Wallet tests or package fail.
- Server tests or package fail.
- Website tests or package fail.
- Release artifacts or checksums mismatch.
- Exchange readiness is unclear for critical operators.
- Founder burn proof is inconsistent, if burn is scheduled before fork.
- Live chain is unstable.
- Consensus bug remains unresolved.
- V35 fork index was calculated from stale height data.
- Wallet/server `Seting.java` values do not match exactly.
- Emergency communication channel is not ready.

## 13. Remaining Risks

Highest remaining risks:

1. Full real historical live-chain replay still needs a copied chain from a live node.
2. Optional long-window difficulty simulation remains a possible extra safety phase.
3. Exchanges and public nodes may not upgrade in time.
4. Founder burn is irreversible and must not be rushed.
5. API full-chain scans may need caching/pagination on large production nodes.
6. A chain split is possible if a meaningful number of nodes remain on old software after the fork block.
7. Estimated activation time can drift because block index, not wall-clock date, controls activation.
8. External wallet/exchange tooling must tolerate `BURN_ADDRESS` as a recipient before the manual burn.

## 14. Recommended Next Phase

Recommended next phase:

```text
Phase 7A - founder burn proof package plan, no code changes.
```

Reason:

- The fork-index mechanics are now planned.
- The remaining high-trust public step is the founder burn proof package.
- The real fork-index activation commit should wait until burn timing, proof package, exchange readiness, and live-chain replay are settled.

Optional extra safety phase before Phase 7A:

```text
Phase 6D-2 - long-window difficulty / extended dry-run simulation.
```

Use Phase 6D-2 if the team wants more confidence in extended difficulty behavior before public scheduling.

Phase 6F-2 should come later, after live-chain replay, exchange coordination, burn proof readiness, and the final upgrade window are all ready.
