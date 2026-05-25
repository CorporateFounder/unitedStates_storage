# CITU V35 Phase 6D-2 Long-Window Difficulty Dry-Run Report

## 1. Exact reports/files read

Reports read:
- `CITU_V35_PHASE6D1_SIMULATED_FORK_DRY_RUN_REPORT.md`
- `CITU_V35_PHASE6A_PRE_ACTIVATION_HARDENING_PLAN.md`
- `CITU_V35_PHASE6B1_LIVE_CONTROLLER_DATABASE_HARDENING_REPORT.md`
- `CITU_V35_PHASE6C1_OLD_CHAIN_VALIDATION_BASELINE_REPORT.md`
- `CITU_V35_PHASE6C2_HISTORICAL_CHAIN_VALIDATION_REPORT.md`
- `CITU_V35_PHASE6E_RELEASE_ROLLBACK_EXCHANGE_CHECKLIST.md`
- `CITU_V35_PHASE6F1_FINAL_FORK_INDEX_ACTIVATION_PLAN.md`
- V35 PoW/target/cumulativeWork/mining/validation reports from earlier phases.

Files/classes inspected:
- Wallet `Seting.java`
- Server `Seting.java`
- Wallet/server `V35TargetDifficulty`
- Wallet/server `V35PowValidator`
- Wallet/server `V35WorkCalculator`
- Wallet/server `ProtocolVersion`
- Wallet/server `Blockchain.java`
- Wallet/server `Mining.java`
- Wallet/server `UtilsBlock.java`
- Wallet/server `DataShortBlockchainInformation.java`
- Server `HostEndDataShortBComparator.java`
- Server `TournamentService.java`
- Existing wallet/server V35 PoW, target difficulty, work, simulated fork, and old-chain tests.

Confirmed constants:
- Wallet/server `V35_RESCUE_FORK_INDEX = Long.MAX_VALUE`
- Wallet/server `V35_BURN_ADDRESS = "BURN_ADDRESS"`
- Wallet/server `V35_TARGET_BLOCK_TIME_SECONDS = 200`
- Wallet/server `V35_TARGET_WINDOW = 60`
- Expected blocks/day = `86400 / 200 = 432`

## 2. Exact files/classes inspected

Primary inspected implementation classes:
- `unitedStates_final-master/src/main/java/International_Trade_Union/protocol/v35/V35TargetDifficulty.java`
- `unitedStates_storage-master/src/main/java/International_Trade_Union/protocol/v35/V35TargetDifficulty.java`
- `unitedStates_final-master/src/main/java/International_Trade_Union/protocol/v35/V35PowValidator.java`
- `unitedStates_storage-master/src/main/java/International_Trade_Union/protocol/v35/V35PowValidator.java`
- `unitedStates_final-master/src/main/java/International_Trade_Union/protocol/v35/V35WorkCalculator.java`
- `unitedStates_storage-master/src/main/java/International_Trade_Union/protocol/v35/V35WorkCalculator.java`
- `unitedStates_storage-master/src/main/java/International_Trade_Union/model/HostEndDataShortBComparator.java`
- `unitedStates_storage-master/src/main/java/International_Trade_Union/entity/blockchain/DataShortBlockchainInformation.java`

## 3. Exact files changed

Production files changed:
- `unitedStates_final-master/src/main/java/International_Trade_Union/protocol/v35/V35TargetDifficulty.java`
- `unitedStates_storage-master/src/main/java/International_Trade_Union/protocol/v35/V35TargetDifficulty.java`

Test files changed:
- `unitedStates_final-master/src/test/java/International_Trade_Union/protocol/v35/V35LongWindowDifficultyDryRunTest.java`
- `unitedStates_storage-master/src/test/java/International_Trade_Union/protocol/v35/V35LongWindowDifficultyDryRunTest.java`
- `unitedStates_final-master/src/test/java/International_Trade_Union/protocol/v35/V35TargetDifficultyTest.java`
- `unitedStates_storage-master/src/test/java/International_Trade_Union/protocol/v35/V35TargetDifficultyTest.java`

Report created:
- `CITU_V35_PHASE6D2_LONG_WINDOW_DIFFICULTY_DRY_RUN_REPORT.md`

## 4. Tests added

Added mirrored wallet/server test class:
- `V35LongWindowDifficultyDryRunTest`

Test coverage added:
- Production constants remain disabled and V35 timing constants match policy.
- Test-only simulated fork index proves pre-fork and post-fork gate behavior without changing global constants.
- Near-perfect 200-second block spacing keeps the target stable over an extended 12-window sequence.
- Too-fast windows make target difficulty harder or keep it from incorrectly easing.
- Too-slow windows ease or remain stable while staying bounded by the protocol maximum.
- Emergency easing stays bounded and never produces invalid targets.
- Compact target encode/decode round-trip remains safe.
- Invalid compact/target inputs do not produce zero, negative, or overflowing targets.
- PoW helper behavior is checked at each adjusted target: `hash <= target` passes and `hash > target` fails.
- cumulativeWork remains positive and monotonic over the simulated sequence.

Updated existing mirrored wallet/server target tests to account for the protocol maximum target ceiling:
- At the easiest target, slow windows and emergency easing may remain stable instead of becoming easier.
- Targets must not exceed `V35TargetDifficulty.getInitialTarget()`.

## 5. How simulated fork index was used

The tests do not modify `Seting.V35_RESCUE_FORK_INDEX`.

The simulated fork index is local to tests:
- `SIMULATED_FORK_INDEX = 6000`

The tests prove:
- Production `ProtocolVersion.isV35(6000)` remains false because the real fork index is `Long.MAX_VALUE`.
- Test-only explicit gate checks using `V35JackpotProofService.isV35ActiveAt(height, simulatedForkIndex)` return false before 6000 and true at/after 6000.
- No simulated fork value leaks into production constants or later tests.

## 6. Target block time behavior proven

Target block time policy:
- 200 seconds per block.
- 60-block adjustment window in the current helper.
- 432 expected blocks/day.

Behavior proven:
- Near-perfect spacing at `60 * 200 = 12000` seconds keeps the compact target stable over 12 windows.
- Fast windows at half expected timespan harden difficulty by reducing target.
- Slow windows ease or remain stable, but never exceed the protocol maximum target.
- All produced targets remain positive and within safe `BigInteger` bounds.

## 7. Difficulty adjustment behavior proven

Proven:
- Compact target encode/decode round-trip remains stable for V35 compact targets.
- Target adjustment does not produce zero targets.
- Target adjustment does not produce negative targets.
- Target adjustment does not overflow into an unbounded/easier-than-initial target.
- Emergency easing is fork-policy-compatible and bounded by the same protocol maximum.
- Invalid previous compact target values safely fall back to the initial V35 target.

Bug exposed and fixed:
- Long-window tests proved that slow-window and emergency-easing calculations could produce a target easier than the configured initial/easiest V35 target.
- The smallest safe fix caps `clampTargetChange(...)` at `getInitialTarget()` in wallet and server.
- This preserves the intended initial compact target as the protocol maximum/easiest target.

## 8. PoW validation behavior proven

The long-window tests validate PoW helper behavior for each adjusted target:
- A hash equal to the target passes.
- A hash one greater than the target fails.
- This confirms the compact-target sequence remains compatible with `V35PowValidator.isHashUnderTarget(...)`.

The tests are helper-level and deterministic. They do not mine real blocks.

## 9. cumulativeWork behavior proven

Proven:
- Harder/lower targets produce equal or greater per-block work.
- cumulativeWork increases monotonically over the simulated post-fork sequence.
- cumulativeWork remains positive and larger than the last single-block work after multiple windows.

Existing Phase 6D-1 and server hardening tests already cover V35 candidate-selection wiring at a higher level. This phase focused on long-window target/work consistency.

## 10. What remains not covered

Remaining gaps:
- No full real mined 720+ block local node simulation was added.
- No real database-backed long-window node run was added.
- No active-state API/UI injection was added for simulated V35.
- Full historical live-chain replay still requires an external copied live chain path.
- Difficulty adjustment was tested at helper level, not through full live mining loops.

## 11. Whether production code was changed

Yes. Production code changed minimally in wallet and server:
- `V35TargetDifficulty.clampTargetChange(...)` now caps the final bounded target at `getInitialTarget()`.

Exact bug:
- Slow-window and emergency easing from the initial/easiest target could create a target larger than the initial maximum.

Exact minimal fix:
- After applying per-window min/max bounds, return `getInitialTarget()` if the bounded target exceeds the protocol maximum/easiest target.

No protocol constants, economics, fork index, reward amounts, jackpot policy, burn policy, transaction format, or block format were changed.

## 12. If production code changed, exact bug and minimal fix

Bug:
- `clampTargetChange(previousTarget, proposedTarget)` bounded changes relative to the previous target, but did not cap the result against the absolute V35 maximum/easiest target.
- With `previousTarget = getInitialTarget()`, slow windows and emergency easing could exceed the initial target.

Fix:
- Wallet/server `V35TargetDifficulty` now applies an absolute cap:
  - If the bounded target is greater than `getInitialTarget()`, return `getInitialTarget()`.
  - Otherwise return the bounded target.

This keeps V35 difficulty easing bounded while leaving hardening behavior unchanged.

## 13. Confirmation real V35_RESCUE_FORK_INDEX remains Long.MAX_VALUE

Confirmed:
- Wallet `Seting.V35_RESCUE_FORK_INDEX = Long.MAX_VALUE`
- Server `Seting.V35_RESCUE_FORK_INDEX = Long.MAX_VALUE`

V35 remains disabled.

## 14. Confirmation no burn transaction was created

No founder burn transaction was created.

## 15. Confirmation no real/manual jackpot payout transaction was created

No real/manual jackpot payout transaction was created.

The added tests do not broadcast, save, or create live jackpot payout transactions.

## 16. Test/build results

Wallet:
- `.\mvnw.cmd test` passed.
  - Tests run: 189
  - Failures: 0
  - Errors: 0
  - Skipped: 0
- `.\mvnw.cmd clean package` passed.
  - Tests run: 189
  - Failures: 0
  - Errors: 0
  - Skipped: 0

Server:
- `.\mvnw.cmd test` passed.
  - Tests run: 213
  - Failures: 0
  - Errors: 0
  - Skipped: 0
- `.\mvnw.cmd clean package` passed.
  - Tests run: 213
  - Failures: 0
  - Errors: 0
  - Skipped: 0

Notes:
- Existing Maven duplicate dependency warnings remain.
- Existing log4j/no-appender warnings remain.
- Existing server test log noise around `server/resources/error/error.txt` remains non-failing.

## 17. Remaining risks

Highest remaining risks:
- Full live historical chain replay still requires a copied real chain.
- Long-window difficulty is now covered at helper level, not by a full mined live-node simulation.
- Active V35 API/UI behavior remains mostly disabled-state verified unless a future simulated-active API mode is added.
- Exchange/node upgrade coordination remains operationally sensitive.
- Founder burn remains irreversible and must wait for the release/burn checklist.

## 18. Recommended next phase

Recommended next phase:
- `Phase 8A` - public release announcement / exchange notice draft.

Alternative if the user wants burn operations tightened first:
- `Phase 7E` - final burn execution checklist, no actual burn.
