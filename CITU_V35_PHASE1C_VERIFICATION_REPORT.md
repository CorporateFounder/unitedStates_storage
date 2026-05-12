# CITU V35 Phase 1C Verification Report

Date: 2026-05-12

Scope: Phase 1C only. Added verification tests for the V35 constants and skeleton helper classes. No V35 behavior was wired into mining, validation, rewards, transaction formats, signatures, wallet UI, website, controllers, or chain sync.

## Files Created

Wallet project: `C:\citu\unitedStates_final-master`

- `src\test\java\International_Trade_Union\protocol\v35\ProtocolVersionTest.java`
- `src\test\java\International_Trade_Union\protocol\v35\V35RewardCalculatorTest.java`
- `src\test\java\International_Trade_Union\protocol\v35\JackpotCalculatorTest.java`
- `src\test\java\International_Trade_Union\protocol\v35\TicketWeightCalculatorTest.java`
- `src\test\java\International_Trade_Union\protocol\v35\AssetDisplayMapperTest.java`
- `src\test\java\International_Trade_Union\protocol\v35\BurnAddressPolicyTest.java`
- `src\test\java\International_Trade_Union\protocol\v35\V35TargetDifficultyTest.java`
- `src\test\java\International_Trade_Union\protocol\v35\V35PowValidatorTest.java`
- `src\test\java\International_Trade_Union\protocol\v35\FounderRewardPolicyTest.java`
- `src\test\java\International_Trade_Union\protocol\v35\V35ParityTest.java`

Server project: `C:\citu\unitedStates_storage-master`

- `src\test\java\International_Trade_Union\protocol\v35\ProtocolVersionTest.java`
- `src\test\java\International_Trade_Union\protocol\v35\V35RewardCalculatorTest.java`
- `src\test\java\International_Trade_Union\protocol\v35\JackpotCalculatorTest.java`
- `src\test\java\International_Trade_Union\protocol\v35\TicketWeightCalculatorTest.java`
- `src\test\java\International_Trade_Union\protocol\v35\AssetDisplayMapperTest.java`
- `src\test\java\International_Trade_Union\protocol\v35\BurnAddressPolicyTest.java`
- `src\test\java\International_Trade_Union\protocol\v35\V35TargetDifficultyTest.java`
- `src\test\java\International_Trade_Union\protocol\v35\V35PowValidatorTest.java`
- `src\test\java\International_Trade_Union\protocol\v35\FounderRewardPolicyTest.java`
- `src\test\java\International_Trade_Union\protocol\v35\V35ParityTest.java`

Report:

- `C:\citu\CITU_V35_PHASE1C_VERIFICATION_REPORT.md`

## Test Coverage Added

- `ProtocolVersionTest`: confirms V35 is disabled for normal indexes and `Seting.V35_RESCUE_FORK_INDEX == Long.MAX_VALUE`.
- `V35RewardCalculatorTest`: confirms 10.00 total GOLD, 8.00 instant miner GOLD, 2.00 jackpot GOLD, and jackpot split validation.
- `JackpotCalculatorTest`: confirms daily, weekly, monthly, quarterly, half-year, yearly jackpot per-block values and 2.00 total.
- `TicketWeightCalculatorTest`: confirms base/max ticket weight, tx/stake boost caps, max combined weight, and negative boost floor at base.
- `AssetDisplayMapperTest`: confirms raw digitalDollar maps 1:1 to Gold display and raw digitalStockBalance maps to 100 Silver per raw unit.
- `BurnAddressPolicyTest`: confirms placeholder burn address is read from `Seting`, exact matching works, normal legacy spend remains allowed, and pure V35 burn-address check returns false at `Long.MAX_VALUE`.
- `V35TargetDifficultyTest`: confirms target window 60, target block time 150 seconds, and emergency delay 1800 seconds.
- `V35PowValidatorTest`: confirms low hash under high target, high hash over low target, and safe false results for invalid, null, empty, or null-target inputs.
- `FounderRewardPolicyTest`: confirms founder reward remains disabled and expected founder reward is 0.00 in the V35 skeleton.
- `V35ParityTest`: confirms package names and method signatures for all V35 skeleton classes.

## Wallet / Server Parity

The wallet and server test files were created with matching package names, class names, method names, and contents.

Hash comparison was run for both V35 test directories. Matching file names produced matching SHA-256 hashes:

- `AssetDisplayMapperTest.java`
- `BurnAddressPolicyTest.java`
- `FounderRewardPolicyTest.java`
- `JackpotCalculatorTest.java`
- `ProtocolVersionTest.java`
- `TicketWeightCalculatorTest.java`
- `V35ParityTest.java`
- `V35PowValidatorTest.java`
- `V35RewardCalculatorTest.java`
- `V35TargetDifficultyTest.java`

## Commands Run

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
git status --short -- unitedStates_final-master/src/test/java/International_Trade_Union/protocol/v35 unitedStates_storage-master/src/test/java/International_Trade_Union/protocol/v35 CITU_V35_PHASE1C_VERIFICATION_REPORT.md unitedStates_final-master/target unitedStates_storage-master/target
```

## Test Results

Wallet `.\mvnw.cmd test`:

- Result: PASS
- Tests run: 11
- Failures: 0
- Errors: 0
- Skipped: 0

Server `.\mvnw.cmd test`:

- Result: PASS
- Tests run: 11
- Failures: 0
- Errors: 0
- Skipped: 0

Note: The first test attempt used JUnit4 imports and compiled successfully, but Surefire reported `Tests run: 0`. The test files were then switched to JUnit Jupiter imports already available through the existing Spring Boot test dependency. No `pom.xml` changes were made.

## Build Results

Wallet `.\mvnw.cmd clean package`:

- Result: PASS
- Tests run during package: 11
- Failures: 0
- Errors: 0
- Skipped: 0
- Jar built successfully: `target\unitedStates-0.32.216-SNAPSHOT.jar`

Server `.\mvnw.cmd clean package`:

- Result: PASS
- Tests run during package: 11
- Failures: 0
- Errors: 0
- Skipped: 0
- Jar built successfully: `target\unitedStates-0.0.423-SNAPSHOT.jar`

Maven warnings observed in both projects:

- Duplicate `spring-boot-starter-test` dependency declaration.
- Duplicate or conflicting `h2` dependency declaration.
- Duplicate `spring-boot-starter-data-jpa` dependency declaration.
- Server also reports duplicate `log4j:log4j`.

These warnings pre-existed Phase 1C and were not changed.

## V35 Disabled Confirmation

V35 remains disabled:

- `Seting.V35_RESCUE_FORK_INDEX == Long.MAX_VALUE`
- `ProtocolVersion.isV35(0) == false`
- `ProtocolVersion.isV35(1_000_000L) == false`
- `ProtocolVersion.isLegacy(0) == true`

The tests intentionally use `Long.MAX_VALUE` only to verify pure skeleton behavior at the configured placeholder fork index.

## No Behavior Wired

No legacy consensus files were edited in Phase 1C:

- `Mining.java`: not edited
- `UtilsBlock.java`: not edited
- `Blockchain.java`: not edited
- `Block.java`: not edited
- `DtoTransaction.java`: not edited
- `Account.java`: not edited
- Controllers: not edited
- `pom.xml`: not edited
- Wallet UI/templates: not edited
- Website: not edited

The new tests only call the Phase 1B skeleton classes and Phase 1A constants.

## Git Diff / Status Notes

After running builds, generated Maven `target` changes were restored/removed so they are not part of Phase 1C source changes.

`git diff --stat` after cleanup:

- No tracked file diff output.

Targeted status after cleanup shows only the new untracked V35 test directories before this report was added:

- `?? unitedStates_final-master/src/test/java/International_Trade_Union/protocol/v35/`
- `?? unitedStates_storage-master/src/test/java/International_Trade_Union/protocol/v35/`

This report is also a Phase 1C-created file:

- `?? CITU_V35_PHASE1C_VERIFICATION_REPORT.md`

## Risks Before Phase 2

- The burn address is still the placeholder `TODO_V35_BURN_ADDRESS`; enforcement is not wired and must not be treated as final.
- `ProtocolVersion.isV35(Long.MAX_VALUE)` returns true because the placeholder fork index is a real long value. Normal chain indexes remain legacy, but tests that use `Long.MAX_VALUE` are intentionally simulating the configured placeholder fork boundary.
- V35 target difficulty and cumulative work remain skeleton-only. They do not yet prove block validation or chain tournament behavior.
- Existing Maven dependency duplication should be cleaned in a later non-consensus build hygiene phase, not inside V35 consensus wiring unless required.
- The parity test verifies package/method signatures inside each project. Cross-project parity was additionally checked by comparing file hashes, but no shared test fixture exists yet.

## Recommended Next Phase

Phase 2A should be a founder reward policy wiring plan only, not implementation yet. It should identify the exact mining and validation call sites for founder reward creation and validation, define old-chain compatibility tests, and specify the V35 fork-gated rejection behavior before any consensus code is changed.
