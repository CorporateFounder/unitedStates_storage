# CITU V35 Phase 2C Reward Fixture Test Report

Date: 2026-05-13

Scope: Phase 2C only. Added V35 reward validation fixture tests and this report. No production Java source, mining behavior, validation behavior, fork index, transaction format, signature format, hash format, controllers, wallet UI, website files, or `pom.xml` files were changed.

## Files Changed

Wallet:

- `C:\citu\unitedStates_final-master\src\test\java\International_Trade_Union\protocol\v35\V35RewardValidationFixtureTest.java`

Server:

- `C:\citu\unitedStates_storage-master\src\test\java\International_Trade_Union\protocol\v35\V35RewardValidationFixtureTest.java`

Report:

- `C:\citu\CITU_V35_PHASE2C_REWARD_FIXTURE_TEST_REPORT.md`

## Tests Created

Both wallet and server received the same test class:

- `International_Trade_Union.protocol.v35.V35RewardValidationFixtureTest`

Test methods:

- `fixedV35RewardShapeIsLocked()`
  - Confirms total V35 reward is `10.00` GOLD.
  - Confirms instant miner reward is `8.00` GOLD.
  - Confirms jackpot reserve is `2.00` GOLD.
  - Confirms founder reward is `0.00` GOLD and `0.00` raw stock.
  - Confirms miner stock reward is `0.00`.
  - Confirms jackpot split validation returns true.

- `founderRewardTransactionIsRejectedByV35RewardRules()`
  - Simulates a V35 reward transaction set containing `BASIS_ADDRESS -> founderAddress`.
  - Confirms the V35 reward-shape fixture rejects it.

- `exactMinerRewardTransactionIsAcceptedByV35RewardRules()`
  - Simulates exactly one `BASIS_ADDRESS -> minerAddress` reward of `8.00` GOLD and `0.00` stock.
  - Confirms the V35 reward-shape fixture accepts it.

- `minerRewardOverpaymentIsRejectedByV35RewardRules()`
  - Confirms `8.01` GOLD is rejected.

- `minerRewardUnderpaymentAndStockRewardAreRejectedByV35RewardRules()`
  - Confirms `7.99` GOLD is rejected.
  - Confirms `8.00` GOLD with `0.01` stock is rejected.

- `missingOrExtraBasisRewardShapesAreRejectedByV35RewardRules()`
  - Confirms missing BASIS reward is rejected.
  - Confirms duplicate miner BASIS rewards are rejected.
  - Confirms BASIS reward to a non-miner, non-founder address is rejected.

- `normalIndexesStayOnLegacyPathWhileV35IsDisabled()`
  - Confirms `Seting.V35_RESCUE_FORK_INDEX == Long.MAX_VALUE`.
  - Confirms `ProtocolVersion.isV35(1_000_000L) == false`.
  - Confirms normal indexes remain legacy and the fixture does not apply V35 reward checks before the fork gate.

## Direct Signed Block Fixtures

Direct full `UtilsBlock.validationOneBlock(...)` signed block fixtures were not added in Phase 2C.

Exact blockers:

- `UtilsBlock.validationOneBlock(...)` first requires real `DtoTransaction.verify()` signatures. Unsigned fixture transactions never reach the Phase 2B V35 reward branch because the current branch is inside `if (transaction.verify() && transaction.getSender().equals(Seting.BASIS_ADDRESS))`.
- Full block validation also checks duplicate signatures, balances, previous hash, block hash, timestamp rules, current difficulty/target rules, decimal rules, and optional `BlockService` duplicate checks. A reward-only fixture must therefore build a coherent previous block, current block, balance map, and signed BASIS reward transaction.
- `Mining.miningDay(...)` V35 reward creation reads the genesis founder address from `Blockchain.indexFromFile(0, Seting.ORIGINAL_BLOCKCHAIN_FILE)`, so isolated mining fixtures are coupled to runtime blockchain files unless a test harness controls those resources.
- Phase 2C was not allowed to change production validation behavior. Extracting a public pure reward-validation helper from `UtilsBlock.validationOneBlock(...)` would be the safest future seam, but it would be a production-code edit and was intentionally deferred.

Recommended test seam for a later phase:

- Add a tiny pure helper, in both wallet and server, that validates only the V35 reward transaction shape after signatures have already been checked:
  - inputs: block index, founder address, miner address, list of already verified BASIS reward transactions.
  - output: accepted/rejected plus a short reason enum or boolean.
  - no block mutation, no signature verification, no hash checks, no filesystem reads.

## V35 Disabled Confirmation

V35 remains disabled:

- Wallet `Seting.java`: `long V35_RESCUE_FORK_INDEX = Long.MAX_VALUE;`
- Server `Seting.java`: `long V35_RESCUE_FORK_INDEX = Long.MAX_VALUE;`
- `ProtocolVersion.isV35(1_000_000L) == false` in both projects.

The tests use `Long.MAX_VALUE` only as a simulated V35 boundary to exercise the gated helper behavior while normal chain indexes remain legacy.

## Legacy Behavior Confirmation

No production code was changed in Phase 2C.

Not changed:

- `Mining.java`
- `UtilsBlock.java`
- `Blockchain.java`
- `Block.java`
- `DtoTransaction.java`
- `Account.java`
- controllers
- templates/static files
- website files
- `pom.xml`

The new fixture explicitly confirms that V35 reward-shape checks are skipped for normal pre-V35 indexes.

## Wallet / Server Parity

The new wallet and server test classes match by package, class name, test method names, helper method signatures, and fixture behavior.

Both projects now run:

- Existing V35 helper tests from Phase 1C.
- Phase 2B V35 reward helper tests.
- Phase 2C V35 reward validation fixture tests.

## Verification Commands Run

Wallet:

```powershell
cd C:\citu\unitedStates_final-master
.\mvnw.cmd test
.\mvnw.cmd clean package
```

Server:

```powershell
cd C:\citu\unitedStates_storage-master
.\mvnw.cmd test
.\mvnw.cmd clean package
```

Repository checks:

```powershell
cd C:\citu
git diff --stat
git status --short
```

## Test Results

Wallet `.\mvnw.cmd test`:

- Result: PASS
- Tests run: 19
- Failures: 0
- Errors: 0
- Skipped: 0

Server `.\mvnw.cmd test`:

- Result: PASS
- Tests run: 19
- Failures: 0
- Errors: 0
- Skipped: 0

## Build Results

Wallet `.\mvnw.cmd clean package`:

- Result: PASS
- Tests run during package: 19
- Failures: 0
- Errors: 0
- Skipped: 0
- Jar built successfully: `target\unitedStates-0.32.216-SNAPSHOT.jar`

Server `.\mvnw.cmd clean package`:

- Result: PASS
- Tests run during package: 19
- Failures: 0
- Errors: 0
- Skipped: 0
- Jar built successfully: `target\unitedStates-0.0.423-SNAPSHOT.jar`

Pre-existing Maven warnings remain:

- Duplicate `spring-boot-starter-test` dependency declarations.
- Duplicate or conflicting `h2` dependency declarations.
- Duplicate `spring-boot-starter-data-jpa` dependency declarations.
- Server also reports duplicate `log4j:log4j`.

No `pom.xml` files were changed.

## Diff / Status Notes

Maven regenerated tracked `target` build artifacts during the test and package runs. Those generated artifacts were restored after verification.

After cleanup, `git status --short` shows only Phase 2C-created files:

- `?? CITU_V35_PHASE2C_REWARD_FIXTURE_TEST_REPORT.md`
- `?? unitedStates_final-master/src/test/java/International_Trade_Union/protocol/v35/V35RewardValidationFixtureTest.java`
- `?? unitedStates_storage-master/src/test/java/International_Trade_Union/protocol/v35/V35RewardValidationFixtureTest.java`

The wallet and server `V35RewardValidationFixtureTest.java` files have matching SHA-256 hashes:

- `527D9C50E4191649CA720B259457DAB082050D627786EEF6B037E433DB005289`

## Coverage Added

Founder reward rejection:

- Covered by transaction-set fixture test `founderRewardTransactionIsRejectedByV35RewardRules()`.
- Not yet covered by full signed block validation through `UtilsBlock.validationOneBlock(...)`.

Miner reward exactness:

- Covered by transaction-set fixture tests for exact `8.00`, overpayment `8.01`, underpayment `7.99`, stock overpayment, duplicate reward, missing reward, and wrong recipient.
- Also covered by Phase 2B `V35RewardCalculatorTest` pure helper assertions.
- Not yet covered by full signed block validation through `UtilsBlock.validationOneBlock(...)`.

Legacy safety:

- Covered by `normalIndexesStayOnLegacyPathWhileV35IsDisabled()`.
- Broader historical block validation fixtures are still required before any real fork activation.

## Risks Before Phase 3

- Full signed V35 block validation fixtures are still missing.
- The Phase 2B V35 validation branch remains embedded inside the large `UtilsBlock.validationOneBlock(...)` transaction loop, making direct unit testing hard without full block construction.
- Jackpot reserve remains emission-policy-only. No deterministic reserve ledger, payout, winner selection, or reserve transaction has been implemented.
- V35 target-based PoW and cumulative-work chain selection are still not implemented.
- Wallet/server consensus code remains duplicated, so all future V35 behavior must continue to be patched and tested in lockstep.

## Recommended Next Phase

Phase 3A: Bitcoin-like PoW and difficulty adjustment mapping plan.

Before implementation, Phase 3A should map:

- current hash/difficulty validation entry points,
- target representation,
- target adjustment window data requirements,
- emergency difficulty drop inputs,
- cumulative work calculation,
- wallet/server parity tests,
- and how old hash validation remains untouched before V35.
