# CITU V35 Phase 6B-1 Live Controller/Database Hardening Report

Phase: 6B-1 only  
Scope: server-side live/controller/database hardening baseline  
V35 activation: still disabled  
Burn transaction: not created  
Manual jackpot payout transaction: not created  
Transaction/block format changes: none

## 1. Exact Files Inspected

Reports read:

- `CITU_V35_PHASE6A_PRE_ACTIVATION_HARDENING_PLAN.md`
- `CITU_V35_PHASE4E1G_JACKPOT_CONTROLLER_STORAGE_INTEGRATION_REPORT.md`
- `CITU_V35_PHASE5G2B_WALLET_JACKPOT_LIVE_API_REPORT.md`
- `CITU_V35_PHASE5D1_SENTINEL_BURN_ADDRESS_REPLACEMENT_REPORT.md`

Server source inspected:

- `unitedStates_storage-master/src/main/java/International_Trade_Union/controllers/BasisController.java`
- `unitedStates_storage-master/src/main/java/International_Trade_Union/entity/services/BlockService.java`
- `unitedStates_storage-master/src/main/java/International_Trade_Union/model/TournamentService.java`
- `unitedStates_storage-master/src/main/java/International_Trade_Union/network/AllTransactions.java`
- `unitedStates_storage-master/src/main/java/International_Trade_Union/entity/blockchain/Blockchain.java`
- `unitedStates_storage-master/src/main/java/International_Trade_Union/utils/UtilsBlock.java`
- `unitedStates_storage-master/src/main/java/International_Trade_Union/utils/UtilsBalance.java`
- `unitedStates_storage-master/src/main/java/International_Trade_Union/utils/UtilsResolving.java`
- `unitedStates_storage-master/src/main/java/International_Trade_Union/utils/UtilsUse.java`
- `unitedStates_storage-master/src/main/java/International_Trade_Union/entity/DtoTransaction/DtoTransaction.java`
- `unitedStates_storage-master/src/main/java/International_Trade_Union/protocol/v35/BurnAddressPolicy.java`
- `unitedStates_storage-master/src/main/java/International_Trade_Union/protocol/v35/JackpotCalculator.java`
- `unitedStates_storage-master/src/main/java/International_Trade_Union/protocol/v35/V35JackpotChainContextAdapter.java`
- `unitedStates_storage-master/src/main/java/International_Trade_Union/protocol/v35/V35JackpotPayoutBuilder.java`
- `unitedStates_storage-master/src/main/java/International_Trade_Union/protocol/v35/V35JackpotProofService.java`
- `unitedStates_storage-master/src/main/java/International_Trade_Union/protocol/v35/V35RewardCalculator.java`
- `unitedStates_storage-master/src/main/java/International_Trade_Union/protocol/v35/V35TargetDifficulty.java`
- `unitedStates_storage-master/src/main/java/International_Trade_Union/protocol/v35/V35WorkCalculator.java`

Server tests inspected:

- `unitedStates_storage-master/src/test/java/International_Trade_Union/controllers/BasisControllerResolveAdmissionTest.java`
- `unitedStates_storage-master/src/test/java/International_Trade_Union/controllers/V35JackpotControllerTest.java`
- `unitedStates_storage-master/src/test/java/International_Trade_Union/controllers/V35SupplyControllerTest.java`
- `unitedStates_storage-master/src/test/java/International_Trade_Union/model/MiningV35JackpotControllerStorageIntegrationTest.java`
- `unitedStates_storage-master/src/test/java/International_Trade_Union/model/MiningV35JackpotPayoutE2ETest.java`
- `unitedStates_storage-master/src/test/java/International_Trade_Union/model/MiningV35JackpotPayoutWiringTest.java`
- `unitedStates_storage-master/src/test/java/International_Trade_Union/model/TournamentServiceTest.java`
- `unitedStates_storage-master/src/test/java/International_Trade_Union/protocol/v35/BurnAddressPolicyTest.java`
- `unitedStates_storage-master/src/test/java/International_Trade_Union/protocol/v35/UtilsBlockV35JackpotChainContextTest.java`
- `unitedStates_storage-master/src/test/java/International_Trade_Union/protocol/v35/UtilsBalanceV35JackpotAccountingTest.java`
- `unitedStates_storage-master/src/test/java/International_Trade_Union/utils/UtilsResolvingV35JackpotWiringTest.java`

Constants confirmed in server:

- `V35_RESCUE_FORK_INDEX = Long.MAX_VALUE`
- `V35_BURN_ADDRESS = "BURN_ADDRESS"`

## 2. Exact Files Changed

Server production:

- `unitedStates_storage-master/src/main/java/International_Trade_Union/network/AllTransactions.java`

Server tests:

- `unitedStates_storage-master/src/test/java/International_Trade_Union/network/AllTransactionsV35BurnHardeningTest.java`
- `unitedStates_storage-master/src/test/java/International_Trade_Union/model/Phase6B1LiveControllerDatabaseHardeningTest.java`

Report:

- `CITU_V35_PHASE6B1_LIVE_CONTROLLER_DATABASE_HARDENING_REPORT.md`

No wallet, website, protocol helper, mining, validation, balance, block, DTO, controller, or `pom.xml` files were changed.

## 3. Tests Added

### `AllTransactionsV35BurnHardeningTest`

Added transaction-pool hardening tests:

1. `transactionPoolRejectsBurnSenderBeforeVerifyWhenV35IsSimulatedActive`

   Proves the server transaction pool rejects `sender = BURN_ADDRESS` after a simulated active V35 fork gate before `DtoTransaction.verify()` is called.

2. `transactionPoolAllowsRecipientBurnAddressAndNormalTransactionsWhenV35IsSimulatedActive`

   Proves sending TO `BURN_ADDRESS` remains allowed, and normal transactions are not blocked by the burn no-spend policy after simulated V35 activation.

3. `transactionPoolKeepsLegacyDisabledBehaviorBeforeV35`

   Proves the burn-address no-spend rule is not applied before the simulated fork index and confirms the production fork constant remains disabled.

### `Phase6B1LiveControllerDatabaseHardeningTest`

Added server-side storage/controller-style baseline tests:

1. `contextAwareStorageStyleShortCheckAcceptsReloadedJackpotPayoutBlock`

   Builds a deterministic V35 jackpot payout through the existing mining builder, round-trips the payout block through JSON save/reload-style serialization, validates it through `Blockchain.shortCheckWithCanonicalContext(...)`, and confirms the jackpot winner balance is credited.

2. `contextAwareStorageStyleShortCheckRejectsInvalidJackpotPayoutBlock`

   Proves the same context-aware storage-style path rejects a wrong-winner jackpot payout candidate.

3. `tournamentSelectionStaysLegacyWhileV35IsDisabled`

   Proves tournament candidate priority remains legacy while `V35_RESCUE_FORK_INDEX = Long.MAX_VALUE`.

4. `simulatedV35TournamentPrefersCumulativeWorkPath`

   Proves the tournament path can prefer the V35 cumulative-work ordering when a test-only fork index is active.

5. `v35RulesDoNotAccidentallyActivateWithProductionForkConstant`

   Proves the production disabled fork constant prevents V35 jackpot payout creation from the mining builder.

Existing server tests still cover:

- `BasisController` resolve admission rules.
- V35 jackpot controller disabled-state endpoints.
- V35 supply controller disabled-state endpoints.
- `UtilsResolving` jackpot candidate detection and conservative accounting gate.
- `UtilsBlock` context-aware jackpot validation.
- `UtilsBalance` proof-aware jackpot accounting.
- mining jackpot payout builder and E2E helper paths.

## 4. Live/Controller/Database Paths Now Covered

This phase now covers the following server-side paths more directly:

- Transaction pool `AllTransactions.addTransaction(...)` burn-sender rejection before `DtoTransaction.verify()`.
- Transaction pool acceptance of transactions TO `BURN_ADDRESS`.
- Transaction pool acceptance of ordinary non-burn transactions under simulated active V35 fork inputs.
- Storage-style block save/reload format safety through JSON round-trip of a mined jackpot payout block.
- `Blockchain.shortCheckWithCanonicalContext(...)` with explicit canonical jackpot context.
- Proof-aware balance credit after valid jackpot payout in the short-check path.
- Wrong-winner jackpot payout rejection in the same context-aware path.
- Tournament candidate priority before V35 and under simulated V35 cumulative-work rules.
- Disabled production fork guard for mining jackpot payout creation.

## 5. What Remains Not Covered

Still not fully covered in this baseline phase:

1. Real HTTP controller request/response flow

   The new tests exercise controller/storage-style service logic and transaction-pool classes directly, but they do not start a server or send HTTP requests to `BasisController.resolve_conflict(...)`.

2. Real database-backed `BlockService` persistence

   `BlockService` was inspected, and existing/fake service patterns were used, but this phase does not use a real H2 repository-backed save/reload sequence.

3. Full `UtilsResolving.resolve3(...)` network download path

   Existing tests and source inspection cover the context-aware calls, but this phase does not perform real peer download/resolve calls.

4. Full tournament service network candidate path

   Tournament ordering is tested, but the full live node candidate-fetching path is still outside this baseline.

5. Full historical old-chain validation

   This remains the next planned phase, Phase 6C.

6. Full dry-run across a simulated fork with running nodes

   This remains planned for Phase 6D.

## 6. Production Code Changed

Production code changed: yes, minimally.

Changed file:

- `AllTransactions.java`

Exact change:

- Added package-private constructor injection for:
  - blockchain height supplier,
  - V35 fork index supplier.
- The existing public Spring constructor still uses:
  - `BasisController::getBlockchainSize`,
  - `() -> Seting.V35_RESCUE_FORK_INDEX`.
- Replaced direct transaction-pool burn checks with the injected suppliers.

Reason:

- `Seting.V35_RESCUE_FORK_INDEX` is an interface constant and must remain `Long.MAX_VALUE`.
- Without a narrow test seam, the active-fork transaction-pool no-spend path could only be inspected, not executed.
- This was the exact Phase 6A/5D-1 remaining gap for `AllTransactions`.

Behavior impact:

- Production default behavior is unchanged.
- No protocol, consensus, mining, validation, balance, transaction format, or block format behavior changed.
- The change only allows deterministic tests to simulate an active fork for the transaction-pool class.

## 7. V35 Disabled Confirmation

Confirmed in server:

- `Seting.V35_RESCUE_FORK_INDEX = Long.MAX_VALUE`
- `Seting.V35_BURN_ADDRESS = "BURN_ADDRESS"`

The new tests explicitly assert the disabled production fork constant.

## 8. Burn / Jackpot Transaction Confirmation

No founder burn transaction was created.

No manual live jackpot payout transaction was created.

Tests construct deterministic in-memory V35 jackpot fixtures only through existing helper/mining builder paths to validate storage-style behavior. No chain state, database state, network state, or live balances were mutated.

## 9. Test / Build Results

Server verification:

- `.\mvnw.cmd test`
  - PASS
  - Tests run: `192`
  - Failures: `0`
  - Errors: `0`
  - Skipped: `0`

- `.\mvnw.cmd clean package`
  - PASS
  - Tests run during package: `192`
  - Failures: `0`
  - Errors: `0`
  - Skipped: `0`

Notes:

- Maven still reports pre-existing duplicate dependency warnings in `pom.xml`.
- The known pre-existing `\server\resources\error\error.txt` `FileNotFoundException` logging noise still appears during tests, but Maven reports success.

Wallet and website tests were not run because this phase changed only server transaction-pool production code and server tests. No shared wallet/website code was changed.

## 10. Remaining Risks

1. Real database-backed persistence remains untested.

   A future test should use repository-backed `BlockService` with isolated H2/temp storage.

2. Real HTTP controller admission remains untested.

   A future test should call `BasisController.resolve_conflict(...)` through a Spring MVC/controller test or a narrow service-level harness.

3. Full network resolve path remains untested.

   `UtilsResolving.resolve3(...)` still needs a deterministic no-network harness or mocked peer responses.

4. Full old-chain validation remains pending.

   This baseline does not replace Phase 6C.

5. Error logging path is still noisy.

   Existing tests still print missing `\server\resources\error\error.txt` stack traces during expected negative validation paths.

## 11. Recommended Next Phase

Proceed to:

**Phase 6C - full old-chain validation tests**

Primary goal:

- prove the actual historical chain still validates under legacy rules with V35 disabled,
- prove old balances and transactions remain unchanged,
- prove V35 logic does not accidentally activate before the real fork index.
