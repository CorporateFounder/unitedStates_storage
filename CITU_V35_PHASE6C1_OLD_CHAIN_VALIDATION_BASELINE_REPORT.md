# CITU V35 Phase 6C-1 Old-Chain Validation Baseline Report

## Scope

Phase 6C-1 added deterministic old-chain compatibility tests only. No production consensus, mining, balance, controller, wallet dashboard, website, transaction format, or block format code was changed.

## Files / Classes Inspected

- `CITU_V35_PHASE6A_PRE_ACTIVATION_HARDENING_PLAN.md`
- `CITU_V35_PHASE6B1_LIVE_CONTROLLER_DATABASE_HARDENING_REPORT.md`
- Reward / PoW / chain scoring / jackpot / burn / supply V35 reports:
  - `CITU_V35_PHASE2B_REWARD_WIRING_REPORT.md`
  - `CITU_V35_PHASE3B4B0_CHAIN_SCORING_HELPERS_REPORT.md`
  - `CITU_V35_PHASE4E1G_JACKPOT_CONTROLLER_STORAGE_INTEGRATION_REPORT.md`
  - `CITU_V35_PHASE5D1_SENTINEL_BURN_ADDRESS_REPLACEMENT_REPORT.md`
  - `CITU_V35_PHASE5C2C_SUPPLY_API_ENDPOINTS_REPORT.md`
- Wallet:
  - `unitedStates_final-master/src/main/java/International_Trade_Union/entity/blockchain/Blockchain.java`
  - `unitedStates_final-master/src/main/java/International_Trade_Union/utils/UtilsBlock.java`
  - `unitedStates_final-master/src/main/java/International_Trade_Union/utils/UtilsBalance.java`
  - `unitedStates_final-master/src/main/java/International_Trade_Union/utils/UtilsResolving.java`
  - `unitedStates_final-master/src/main/java/International_Trade_Union/model/Mining.java`
  - `unitedStates_final-master/src/main/java/International_Trade_Union/controllers/BasisController.java`
  - `unitedStates_final-master/src/main/java/International_Trade_Union/setings/Seting.java`
  - `unitedStates_final-master/src/main/java/International_Trade_Union/protocol/v35/*`
  - existing wallet validation / V35 tests
- Server:
  - `unitedStates_storage-master/src/main/java/International_Trade_Union/entity/blockchain/Blockchain.java`
  - `unitedStates_storage-master/src/main/java/International_Trade_Union/utils/UtilsBlock.java`
  - `unitedStates_storage-master/src/main/java/International_Trade_Union/utils/UtilsBalance.java`
  - `unitedStates_storage-master/src/main/java/International_Trade_Union/utils/UtilsResolving.java`
  - `unitedStates_storage-master/src/main/java/International_Trade_Union/model/Mining.java`
  - `unitedStates_storage-master/src/main/java/International_Trade_Union/entity/services/BlockService.java`
  - `unitedStates_storage-master/src/main/java/International_Trade_Union/controllers/BasisController.java`
  - `unitedStates_storage-master/src/main/java/International_Trade_Union/service/TournamentService.java`
  - `unitedStates_storage-master/src/main/java/International_Trade_Union/setings/Seting.java`
  - `unitedStates_storage-master/src/main/java/International_Trade_Union/protocol/v35/*`
  - existing server validation / V35 tests

## Files Changed

- `unitedStates_final-master/src/test/java/International_Trade_Union/utils/OldChainV35IsolationTest.java`
- `unitedStates_storage-master/src/test/java/International_Trade_Union/utils/OldChainV35IsolationTest.java`
- `CITU_V35_PHASE6C1_OLD_CHAIN_VALIDATION_BASELINE_REPORT.md`

## Tests Added

Added mirrored wallet/server `OldChainV35IsolationTest` with seven old-chain safety tests:

1. `productionForkConstantsKeepNormalHeightsOnLegacyPath`
   - Confirms `V35_RESCUE_FORK_INDEX = Long.MAX_VALUE`.
   - Confirms `V35_BURN_ADDRESS = BURN_ADDRESS`.
   - Confirms normal heights, including `Long.MAX_VALUE - 1`, remain legacy.
   - Confirms jackpot payout allowance and burn no-spend policy are inactive at normal old-chain heights.

2. `legacyFounderAndMinerRewardsAreStillAccountedBeforeV35`
   - Builds a minimal signed legacy-style block.
   - Confirms old BASIS reward accounting still credits miner and founder recipients before V35.

3. `v35RewardShapeIsNotRequiredForLegacyRewardFixtures`
   - Confirms the V35 `6 / 2 / 0` reward shape exists, but is not required for legacy reward fixtures before the fork.

4. `burnAddressRecipientIsAllowedByLegacyTransactionPoolBalancePath`
   - Confirms `BURN_ADDRESS` can be used as a normal recipient in the legacy transaction-pool balance path before V35.

5. `burnAddressSenderIsNotBlockedByV35NoSpendBeforeForkGate`
   - Confirms V35 burn-address no-spend enforcement does not block a `BURN_ADDRESS` sender before the fork gate in the legacy balance-selection helper.

6. `jackpotLookingBasisAmountDoesNotRequireV35AccountingBeforeFork`
   - Confirms a BASIS transaction whose amount matches a canonical jackpot payout is detectable as a jackpot-looking candidate, but does not trigger V35 jackpot accounting while V35 is disabled.

7. `cumulativeWorkDoesNotAccumulateForLegacyBlocksBeforeFork`
   - Confirms V35 cumulative work remains zero for legacy blocks and does not replace old-chain selection before the fork.

## Old-Chain Behavior Covered

- V35 activation gate remains disabled for normal heights.
- V35 reward model does not become a legacy block requirement.
- Legacy founder/miner BASIS reward accounting still works in minimal deterministic fixtures.
- V35 burn no-spend policy does not unexpectedly block legacy/pre-V35 helper behavior.
- Sending to `BURN_ADDRESS` remains possible as a normal recipient before V35.
- Jackpot-looking BASIS amounts do not force proof/accounting while V35 is disabled.
- V35 cumulative-work helper returns zero for pre-fork blocks.
- Wallet/server behavior is mirrored by identical test coverage.

## Old-Chain Behavior Not Yet Covered

- Full real historical-chain validation was not executed in this phase.
- No controller/database-backed historical chain replay was added here; Phase 6B-1 covers live-path baseline tests separately.
- No simulated V35 fork activation was tested here by design; that remains Phase 6D.
- No external node/network path was exercised.

## Historical Chain Data Finding

The repository contains a server genesis/resource file at `unitedStates_storage-master/src/main/resources/blockchain/0.txt`, but no compact shared deterministic historical chain fixture suitable for mirrored wallet/server old-chain replay tests was found.

Phase 6C-1 therefore used minimal in-memory legacy-style fixtures. A full historical-chain validation runner or manual validation plan remains appropriate for Phase 6C-2 if real chain data should be replayed before dry-run activation.

## Production Code Changes

None.

No production classes were changed. No minimal fixes were needed because the new baseline tests passed.

## Constants Confirmed

- Wallet `Seting.V35_RESCUE_FORK_INDEX = Long.MAX_VALUE`
- Server `Seting.V35_RESCUE_FORK_INDEX = Long.MAX_VALUE`
- Wallet `Seting.V35_BURN_ADDRESS = "BURN_ADDRESS"`
- Server `Seting.V35_BURN_ADDRESS = "BURN_ADDRESS"`

## Safety Confirmations

- V35 remains disabled.
- No founder burn transaction was created.
- No jackpot payout transaction was manually created.
- No reward amounts changed.
- No PoW, difficulty, jackpot, burn, transaction, or block policy changed.
- No wallet dashboard, website, or `pom.xml` files were changed.

## Verification Results

- Server `.\mvnw.cmd test`: PASS
  - 199 tests run, 0 failures, 0 errors, 0 skipped.
- Server `.\mvnw.cmd clean package`: PASS
  - 199 tests run, 0 failures, 0 errors, 0 skipped.
- Wallet `.\mvnw.cmd test`: PASS
  - 176 tests run, 0 failures, 0 errors, 0 skipped.
- Wallet `.\mvnw.cmd clean package`: PASS
  - 176 tests run, 0 failures, 0 errors, 0 skipped.

Build output still contains existing Maven duplicate-dependency warnings and existing logger/file-path warnings from pre-existing test paths. They did not fail the tests or builds and were not introduced by Phase 6C-1.

## Remaining Risks

- A real historical-chain replay has not yet been run against production chain data.
- `ProtocolVersion.isV35(Long.MAX_VALUE)` itself returns true because it checks `index >= forkIndex`; practical current block heights remain below `Long.MAX_VALUE`, and burn/jackpot helpers add explicit `forkIndex != Long.MAX_VALUE` safeguards where required. A future activation phase should avoid using `Long.MAX_VALUE` as a real block index in tests or tooling.
- Legacy validation behavior is broad and historically branchy; this phase covers targeted regression gates, not every old fork-era reward/difficulty combination.
- Existing noisy logger file warnings can obscure test output and should be considered for cleanup in a non-consensus maintenance phase.

## Recommended Next Phase

Recommended next phase:

**Phase 6C-2 - full historical chain validation runner or manual validation plan**, if real canonical chain data is available or can be exported deterministically.

If no reliable historical-chain fixture is available for automation, proceed to:

**Phase 6D - simulated V35 fork dry-run**, with explicit documentation that Phase 6C-1 covered deterministic fixtures rather than full historical replay.
