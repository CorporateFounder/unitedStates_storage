# CITU V35 Phase 6E Release / Rollback / Exchange Coordination Checklist

## Scope

Phase 6E is a planning-only release, rollback, exchange coordination, founder burn timing, and post-activation monitoring checklist. No source code, tests, constants, protocol behavior, mining behavior, validation behavior, balance behavior, transaction format, block format, website, wallet dashboard, or `pom.xml` files were changed.

## 1. Exact Reports / Files Read

Required Phase 6 reports:

- `CITU_V35_PHASE6A_PRE_ACTIVATION_HARDENING_PLAN.md`
- `CITU_V35_PHASE6B1_LIVE_CONTROLLER_DATABASE_HARDENING_REPORT.md`
- `CITU_V35_PHASE6C1_OLD_CHAIN_VALIDATION_BASELINE_REPORT.md`
- `CITU_V35_PHASE6C2_HISTORICAL_CHAIN_VALIDATION_REPORT.md`
- `CITU_V35_PHASE6D1_SIMULATED_FORK_DRY_RUN_REPORT.md`

Representative V35 readiness reports:

- `CITU_V35_PHASE2B_REWARD_WIRING_REPORT.md`
- `CITU_V35_PHASE3B4B0_CHAIN_SCORING_HELPERS_REPORT.md`
- `CITU_V35_PHASE3B4B4B_TOURNAMENT_SERVICE_REPORT.md`
- `CITU_V35_PHASE4E1G_JACKPOT_CONTROLLER_STORAGE_INTEGRATION_REPORT.md`
- `CITU_V35_PHASE5C2C_SUPPLY_API_ENDPOINTS_REPORT.md`
- `CITU_V35_PHASE5D1_SENTINEL_BURN_ADDRESS_REPLACEMENT_REPORT.md`
- `CITU_V35_PHASE5E1_FOUNDER_BURN_SNAPSHOT_REPORT.md`
- `CITU_V35_PHASE5F3B_WALLET_DASHBOARD_LIVE_API_REPORT.md`
- `CITU_V35_PHASE5G2B_WALLET_JACKPOT_LIVE_API_REPORT.md`

Project files inspected for constants:

- `unitedStates_final-master/src/main/java/International_Trade_Union/setings/Seting.java`
- `unitedStates_storage-master/src/main/java/International_Trade_Union/setings/Seting.java`

Confirmed:

- Wallet `V35_RESCUE_FORK_INDEX = Long.MAX_VALUE`
- Server `V35_RESCUE_FORK_INDEX = Long.MAX_VALUE`
- Wallet `V35_BURN_ADDRESS = "BURN_ADDRESS"`
- Server `V35_BURN_ADDRESS = "BURN_ADDRESS"`

## 2. Current Readiness Summary

V35 core is implemented and hardening is substantially complete, but V35 is not ready for a real fork-index release until final operational steps are complete.

Ready or covered:

- V35 remains disabled by `Long.MAX_VALUE`.
- Reward model implemented behind fork gate:
  - `8.00` GOLD per block.
  - `6.00` GOLD instant miner reward.
  - `2.00` GOLD jackpot reserve.
  - `0.00` GOLD founder reward.
- V35 target PoW, target difficulty helpers, cumulative work metadata/comparison, and tournament priority are implemented and tested behind fork gates.
- Jackpot proof, chain-context validation, mining payout builder, accounting, controller/storage-style tests, read-only jackpot APIs, and wallet jackpot dashboard integration exist.
- `BURN_ADDRESS` sentinel is configured in wallet/server.
- Burn-address no-spend policy is implemented behind V35 gate.
- Supply, burn, founder, and founder burn snapshot APIs exist.
- Wallet dashboard and public website V35 pages exist.
- Phase 6B-1 live/controller/database baseline passed with minimal transaction-pool test seam.
- Phase 6C-1 old-chain fixture baseline passed.
- Phase 6C-2 historical replay runner exists; server bundled sample replay passed.
- Phase 6D-1 simulated fork dry-run passed.

Not complete:

- Full real historical live-chain replay requires an external copied chain directory.
- Founder burn has not happened.
- Exchanges and node operators have not been notified.
- Real fork index has not been selected.
- Optional long-window V35 difficulty simulation remains an extra safety gap, not a blocker if accepted explicitly.

## 3. Release Readiness Checklist

### Repository / reports

- [ ] Clean git status except intentional release artifacts.
- [ ] All V35 phase reports committed.
- [ ] All source/test/report changes reviewed.
- [ ] No generated `target/` outputs staged.
- [ ] `V35_RESCUE_FORK_INDEX` still `Long.MAX_VALUE` before release candidate verification.
- [ ] `V35_BURN_ADDRESS` still `BURN_ADDRESS`.

### Build verification

- [ ] Wallet `.\mvnw.cmd test` passes.
- [ ] Wallet `.\mvnw.cmd clean package` passes.
- [ ] Server `.\mvnw.cmd test` passes.
- [ ] Server `.\mvnw.cmd clean package` passes.
- [ ] Website `.\mvnw.cmd test` passes.
- [ ] Website `.\mvnw.cmd clean package` passes.
- [ ] Maven duplicate dependency warnings reviewed as known pre-existing warnings or resolved in separate non-consensus maintenance.
- [ ] Existing `\server\resources\error\error.txt` test-output noise reviewed as known non-failing logging behavior.

### Chain validation

- [ ] Historical chain replay run against copied live chain data, not live mutable data.
- [ ] Replay records block count, first index/hash, last index/hash, validation result.
- [ ] Old-chain validation confirms legacy path before fork.
- [ ] Old balances unchanged or differences fully explained.
- [ ] No V35 reward, jackpot, compact target PoW, burn no-spend, or cumulativeWork requirement invalidates old blocks.

### V35 dry-run

- [ ] Phase 6D-1 simulated fork dry-run remains passing.
- [ ] Optional long-window difficulty simulation either completed or explicitly accepted as a known remaining gap.
- [ ] Simulated V35 block reward: `6.00` miner, `0.00` founder.
- [ ] Simulated V35 target PoW active.
- [ ] Simulated cumulativeWork selection sane.
- [ ] Simulated jackpot payout generation/validation/accounting sane.
- [ ] Simulated `BURN_ADDRESS` no-spend active after fork.

### API / UI sanity

- [ ] `/v35/protocol` sane.
- [ ] `/v35/supply` sane.
- [ ] `/v35/burn` sane.
- [ ] `/v35/founder` sane.
- [ ] `/v35/founder/burn-snapshot` sane.
- [ ] `/v35/jackpot/status` sane.
- [ ] `/v35/jackpot/periods` sane.
- [ ] `/v35/jackpot/history` sane.
- [ ] Wallet `/v35-dashboard` renders, shows disabled warning before activation, and handles endpoint fallback.
- [ ] Website `/v35`, `/v35/tokenomics`, `/v35/burn`, `/v35/jackpot`, `/v35/mining`, `/v35/faq` render and do not claim activation/burn happened early.

### Artifacts / release metadata

- [ ] Release candidate build names chosen, for example:
  - `citu-wallet-v35-rc1`
  - `citu-server-v35-rc1`
  - `citu-website-v35-rc1`
- [ ] Final release tag naming chosen, for example:
  - `v35-activation-rc1`
  - `v35-mainnet-fork-<forkIndex>`
- [ ] Artifact checksums generated after final build.
- [ ] Release notes include exact fork index, estimated activation date/time, reward model, burn address, and upgrade instructions.

## 4. Real Fork Index Selection Formula And Recommendation

Fork activation must be by block index, not only date/time.

Constants:

- Target block time: `200` seconds.
- Blocks per day: `432`.
- `7` days approximately `3024` blocks.
- `14` days approximately `6048` blocks.
- `21` days approximately `9072` blocks.

Formula:

```text
estimatedForkTime = currentObservedTime + ((V35_RESCUE_FORK_INDEX - currentHeight) * 200 seconds)
```

Recommended selection:

```text
V35_RESCUE_FORK_INDEX = currentHeight + 6048 or more
```

This gives exchanges and node operators roughly a 14-day upgrade window. If exchange coordination is slow or the live historical replay is not yet archived, use `currentHeight + 9072` or more for a 21-day window.

Rules:

- Do not set the fork index until the release candidate is verified.
- Publish the exact block index and estimated date/time together.
- Recalculate the estimated date/time shortly before announcement using current chain height.
- Treat the date/time as an estimate; block index is authoritative.
- If a blocking issue appears before fork height, ship a patched release that delays the fork index.

## 5. Exchange / Node Operator Coordination Timeline

### T-14 days announcement

Send initial technical notice to exchanges, pools, seed nodes, and known operators.

Include:

- New wallet/server release download links.
- Exact `V35_RESCUE_FORK_INDEX`.
- Estimated date/time of fork based on current height.
- Required upgrade deadline.
- Release artifact checksums if available.
- V35 summary:
  - 200 sec/block target.
  - 8 GOLD/block.
  - 6 GOLD miner reward.
  - 2 GOLD jackpot reserve.
  - 0 founder reward.
  - Bitcoin-like target PoW.
  - cumulativeWork chain selection.
  - `BURN_ADDRESS` burn sink.
- Recommendation to test on their own staging node.
- Support contact placeholder: `SUPPORT_CONTACT_TBD`.

### T-7 days reminder

Send reminder:

- Confirm fork index and updated estimated date/time.
- Ask operators to confirm upgraded node version.
- Ask exchanges to verify deposits/withdrawals against upgraded wallet/server.
- Ask exchanges to review optional pause window.

### T-3 days final technical reminder

Send final technical notice:

- Current height.
- Blocks remaining.
- Updated estimated activation time.
- Checksums and release tag.
- Known monitoring endpoints.
- Recommended node log checks.

### T-1 day deposit/withdrawal caution

Recommend exchanges consider:

- Pausing deposits/withdrawals around the fork window if their internal risk policy prefers safety.
- Keeping trading open only if they are comfortable with chain monitoring.
- Waiting for a chosen number of post-fork confirmations before resuming full operations.

### Fork day

Monitor:

- First V35 block.
- Reward shape.
- PoW target validation.
- cumulativeWork metadata.
- peer sync.
- exchange node status.

### Post-fork confirmation

Publish:

- First V35 block index/hash.
- First V35 block reward transaction summary.
- Confirmation no founder reward exists.
- Node/exchange sync status.
- Any jackpot/burn/supply API sanity notes.

## 6. Public Announcement Checklist

Public announcement sections:

- Why V35 exists:
  - simplifies emission,
  - removes founder reward after V35,
  - adds deterministic jackpot mining,
  - improves PoW/chain-selection clarity.
- What changes:
  - `8.00` GOLD/block total.
  - `6.00` GOLD instant miner reward.
  - `2.00` GOLD jackpot reserve.
  - `0.00` GOLD founder reward after V35.
  - `200` sec/block target.
  - `432` blocks/day.
  - annual emission `1,261,440` GOLD/year.
  - Bitcoin-like PoW: `hash <= target`.
  - cumulativeWork chain selection.
- Burn address:
  - `BURN_ADDRESS`.
  - protocol sink.
  - sending TO it is allowed.
  - spending FROM it is blocked after V35 by consensus policy.
- Founder burn plan:
  - recommended 80% founder burn.
  - manual burn planned before activation.
  - actual burn proof will be published.
  - burn is not actual until confirmed on-chain.
- Jackpot explanation:
  - each V35 mined block is a ticket.
  - winner is selected from `minerAddress` only.
  - sender addresses are never jackpot participants.
  - delayed future hashes are used.
  - daily/weekly/monthly/quarterly/half-year/yearly pools.
- User safety notice:
  - no action required for normal holders unless they run a node, wallet integration, exchange, or miner.
  - miners and node operators must upgrade before fork block.
  - exchanges should follow technical coordination notice.
- Disclaimer:
  - protocol upgrade notice,
  - not financial advice.

## 7. Founder Burn Timing Plan

Founder burn must wait until:

- Phase 6B/6C/6D hardening and dry-run are accepted.
- Release checklist passes.
- Live historical chain replay against copied chain data passes, or the project explicitly accepts it as a manual pre-release step with recorded risk.
- Snapshot height/hash selected.
- Founder balance verified.
- Burn amount calculated.
- Burn transaction reviewed before signing/broadcast.

Procedure:

1. Freeze founder address operations.
2. Pick snapshot block height and hash.
3. Query `/v35/founder/burn-snapshot`.
4. Record:
   - founder address,
   - `BURN_ADDRESS`,
   - snapshot height,
   - snapshot block hash,
   - founder balance,
   - actual burned supply before burn,
   - actual circulating supply before burn.
5. Calculate burn options:
   - 75%,
   - 80% recommended,
   - 85%,
   - 90%.
6. Select 80% unless governance/project owner explicitly chooses another amount.
7. Manually create normal signed transaction:

```text
founderAddress -> BURN_ADDRESS
```

8. Verify amount, recipient, signature, and fee/bonus behavior.
9. Broadcast.
10. Wait for confirmation.
11. Publish:
    - burn transaction hash/signature,
    - burn block index,
    - burn block hash,
    - burned amount,
    - founder balance before burn,
    - founder balance after burn,
    - total minted,
    - burned supply,
    - circulating supply,
    - annual emission,
    - estimated inflation after burn.
12. Verify `/v35/supply`, `/v35/burn`, `/v35/founder`, and `/v35/founder/burn-snapshot` after burn.

Recommended burn amount basis:

- Use the snapshot founder balance, not a moving live balance.
- If founder balance changes after snapshot and before burn, discard the snapshot and generate a new one.

## 8. Pre-Fork Rollback Plan

Before the fork block, rollback is straightforward because no V35 consensus change has activated.

If a problem appears before the fork block:

- Tell nodes/exchanges to stay on the previous release or upgrade to patched release.
- Publish urgent operator notice.
- If needed, release a patched build with:
  - `V35_RESCUE_FORK_INDEX = Long.MAX_VALUE`, or
  - a later fork index.
- Re-run:
  - wallet tests/package,
  - server tests/package,
  - website tests/package,
  - historical replay,
  - simulated fork dry-run.
- Do not proceed with founder burn if the issue affects consensus, supply reporting, burn handling, or exchange confidence.

If the founder burn has already happened but fork has not activated:

- The burn is irreversible.
- Do not rollback the burn.
- Communicate the burn proof and delay fork if needed.
- Keep V35 disabled until patched release is ready.

## 9. Post-Fork Emergency Plan

After fork, rollback is dangerous and can split the chain.

Preferred action:

- Emergency patch with coordinated operator/exchange communication.
- Avoid silent rollback.
- Avoid multiple competing binaries.
- Coordinate canonical chain selection based on V35 cumulativeWork and block validity.

Emergency steps:

1. Announce issue and ask exchanges to pause deposits/withdrawals if consensus or accounting is affected.
2. Preserve node logs and block data.
3. Record:
   - first V35 block index/hash,
   - current best tip index/hash,
   - cumulativeWork,
   - peer status,
   - any invalid block/reorg symptoms.
4. Classify issue:
   - consensus validation,
   - mining reward,
   - jackpot payout,
   - burn policy,
   - balance/accounting,
   - resolving/tournament,
   - API/UI only.
5. If API/UI only:
   - patch read-only layer without changing consensus.
6. If consensus-affecting:
   - prepare emergency consensus patch,
   - publish exact recovery instructions,
   - coordinate exchanges and major nodes before resuming deposits/withdrawals.
7. Publish postmortem and final canonical chain checkpoint.

Exchange recommendation:

- Pause deposits/withdrawals during emergency investigation.
- Resume only after patched binaries and canonical chain checkpoint are confirmed.

## 10. Post-Activation Monitoring Checklist

### First V35 block

- [ ] First V35 block accepted by wallet.
- [ ] First V35 block accepted by server.
- [ ] Block index equals published fork index.
- [ ] Block hash satisfies V35 target.
- [ ] Compact target metadata sane.
- [ ] cumulativeWork increments.
- [ ] No legacy difficulty shortcut accepted invalid V35 PoW.

### Rewards

- [ ] Miner reward exactly `6.00` GOLD.
- [ ] Miner stock/raw reward `0.00`.
- [ ] Founder reward absent.
- [ ] Any `BASIS -> founder` post-fork reward rejected.
- [ ] Invalid miner reward rejected.

### Difficulty / chain selection

- [ ] Target block time trend around 200 seconds.
- [ ] Difficulty adjustment sane at window boundaries.
- [ ] Emergency easing sane if delay occurs.
- [ ] Chain selection follows cumulativeWork.
- [ ] Tournament candidate selection does not prefer stake/tx over V35 work.

### Jackpot

- [ ] MinerAddress-only ticket eligibility.
- [ ] Sender addresses never counted.
- [ ] 60-block future hash delay enforced.
- [ ] Exact-slot payout only.
- [ ] Wrong winner rejected.
- [ ] Duplicate same-period payout rejected.
- [ ] Paid history appears only after valid payout.

### Burn / supply

- [ ] `BURN_ADDRESS` cannot spend after V35.
- [ ] Sending TO `BURN_ADDRESS` still works.
- [ ] Burned supply equals burn address balance.
- [ ] Circulating supply equals total minted minus burned.
- [ ] Founder reserve matches post-burn account state.
- [ ] Actual and modeled supply remain separate.

### APIs / dashboards

- [ ] `/v35/protocol` reports active after fork.
- [ ] `/v35/supply` sane.
- [ ] `/v35/burn` sane.
- [ ] `/v35/founder` sane.
- [ ] `/v35/founder/burn-snapshot` still model-only/past-proof aware.
- [ ] `/v35/jackpot/status` sane.
- [ ] `/v35/jackpot/periods` sane.
- [ ] `/v35/jackpot/history` sane.
- [ ] Wallet `/v35-dashboard` shows live data or safe fallback.
- [ ] Website wording matches activation and burn proof state.

### Exchanges / nodes

- [ ] Major nodes synced.
- [ ] Exchanges confirm upgraded version.
- [ ] Exchanges confirm canonical chain tip.
- [ ] Deposit/withdrawal status monitored.
- [ ] Public status update published after stable confirmation window.

## 11. Remaining Risks

1. Full real historical live-chain replay is still pending unless a live node chain copy is provided and archived.
2. Long-window V35 difficulty simulation remains optional but useful before final fork index selection.
3. API/UI simulated active-fork rendering is not injected; API/UI currently report real disabled state until real fork constant changes.
4. Exchange upgrade timing risk remains high; a short announcement window could fragment node versions.
5. Founder burn is irreversible and must wait until hardening, replay, release, and snapshot processes are complete.
6. Read-only API performance/pagination/caching remains a scale risk, especially jackpot history/tickets/proof and supply scans.
7. External wallet/exchange tooling must accept `BURN_ADDRESS` as a recipient for the manual founder burn.
8. Existing Maven duplicate dependency warnings and noisy logger file warnings can obscure release logs, even though they are not currently failing builds.

## 12. Recommended Next Phase

Recommended next phase:

**Phase 6F-1 - final fork-index activation plan, still no code changes.**

Phase 6F-1 should:

- require a current chain height snapshot,
- calculate candidate fork indexes for 14-day and 21-day windows,
- define exact release artifact names/checksums,
- define exact exchange/operator notice text,
- define the final go/no-go gate before changing `V35_RESCUE_FORK_INDEX`.

Optional extra safety phase:

**Phase 6D-2 - long-window difficulty / extended dry-run simulation.**

Use Phase 6D-2 only if the project wants a larger simulated post-fork chain for difficulty adjustment windows, API active-state injection tests, or persistent database dry-run before final fork-index planning.
