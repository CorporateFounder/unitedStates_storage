# CITU V35 Phase 2B Reward Wiring Report

Date: 2026-05-12

Scope: V35 founder reward removal and fixed reward wiring behind the V35 fork gate only. V35 remains disabled because both wallet and server still define `V35_RESCUE_FORK_INDEX = Long.MAX_VALUE`.

## Files Changed

Wallet:

- `C:\citu\unitedStates_final-master\src\main\java\International_Trade_Union\model\Mining.java`
- `C:\citu\unitedStates_final-master\src\main\java\International_Trade_Union\utils\UtilsBlock.java`
- `C:\citu\unitedStates_final-master\src\main\java\International_Trade_Union\protocol\v35\V35RewardCalculator.java`
- `C:\citu\unitedStates_final-master\src\test\java\International_Trade_Union\protocol\v35\V35RewardCalculatorTest.java`
- `C:\citu\unitedStates_final-master\src\test\java\International_Trade_Union\protocol\v35\V35ParityTest.java`

Server:

- `C:\citu\unitedStates_storage-master\src\main\java\International_Trade_Union\model\Mining.java`
- `C:\citu\unitedStates_storage-master\src\main\java\International_Trade_Union\utils\UtilsBlock.java`
- `C:\citu\unitedStates_storage-master\src\main\java\International_Trade_Union\protocol\v35\V35RewardCalculator.java`
- `C:\citu\unitedStates_storage-master\src\test\java\International_Trade_Union\protocol\v35\V35RewardCalculatorTest.java`
- `C:\citu\unitedStates_storage-master\src\test\java\International_Trade_Union\protocol\v35\V35ParityTest.java`

Report:

- `C:\citu\CITU_V35_PHASE2B_REWARD_WIRING_REPORT.md`

## Methods Changed

- `International_Trade_Union.model.Mining.miningDay(...)`
  - Added a fork-gated `ProtocolVersion.isV35(index)` branch after previous-block lookup and before legacy reward formulas.
  - The V35 branch creates only one BASIS reward transaction to the miner.
  - The V35 branch uses `V35RewardCalculator.getInstantMinerGoldReward()` and `getInstantMinerStockReward()`.
  - The V35 branch returns before legacy difficulty/activity/Friedman/multiplier reward formulas run.

- `International_Trade_Union.utils.UtilsBlock.validationOneBlock(...)`
  - Added a fork-gated `ProtocolVersion.isV35(thisBlock.getIndex())` branch inside BASIS reward transaction validation.
  - The V35 branch rejects any BASIS transaction to the founder address.
  - The V35 branch allows only a BASIS transaction to the miner for Phase 2B.
  - The V35 branch requires exact instant miner reward shape: `8.00` GOLD and `0.00` raw stock.
  - Added a post-loop V35 guard requiring exactly one BASIS miner reward transaction for non-genesis V35 blocks.

- `International_Trade_Union.protocol.v35.V35RewardCalculator`
  - Added `getInstantMinerStockReward()`.
  - Added `getFounderGoldReward()`.
  - Added `getFounderStockReward()`.
  - Added `isValidInstantMinerReward(double, double)`.
  - Added `isValidFounderReward(double, double)`.

## V35 Disabled Confirmation

Confirmed after implementation:

- Wallet `Seting.java`: `long V35_RESCUE_FORK_INDEX = Long.MAX_VALUE;`
- Server `Seting.java`: `long V35_RESCUE_FORK_INDEX = Long.MAX_VALUE;`

Normal chain indexes remain legacy. The new code is inert unless a block index reaches `Long.MAX_VALUE`.

## Legacy Path Confirmation

Before V35:

- `Mining.miningDay(...)` falls through to the existing legacy reward code.
- `UtilsBlock.validationOneBlock(...)` falls through to the existing legacy BASIS/founder/miner reward validation code.
- Existing transaction format, signature format, block hash format, difficulty logic, controllers, UI, website, and `pom.xml` were not changed.

## Founder Transaction Behavior

Mining:

- After V35, `Mining.miningDay(...)` does not create or add the founder reward transaction.
- The legacy founder transaction creation remains untouched in the non-V35 path.

Validation:

- After V35, `UtilsBlock.validationOneBlock(...)` rejects `BASIS_ADDRESS -> founderAddress`.
- Before V35, the existing founder reward validation remains in place.

## Fixed Reward Behavior

After V35, the wired mining path creates a single miner reward transaction with:

- `digitalDollar = 8.00`
- `digitalStockBalance = 0.00`
- `sender = BASIS_ADDRESS`
- `customer = miner address`

The V35 validation path accepts only that miner reward shape for Phase 2B and rejects:

- founder reward transactions,
- BASIS rewards to any non-miner recipient,
- miner rewards above or below `8.00`,
- miner reward raw stock above or below `0.00`,
- more than one BASIS reward transaction.

## Jackpot Reserve Handling

The V35 jackpot reserve remains reserved by emission policy only in Phase 2B.

- `V35_GOLD_REWARD = 10.00`
- `V35_INSTANT_MINER_REWARD = 8.00`
- `V35_JACKPOT_REWARD = 2.00`

No jackpot payout, jackpot winner selection, reserve address, or reserve transaction was implemented. This avoids creating a spendable or ambiguous reserve destination before the jackpot phase defines deterministic accounting.

## Tests Added Or Updated

Wallet and server:

- `V35RewardCalculatorTest`
  - Confirms total reward `10.00`.
  - Confirms instant miner reward `8.00`.
  - Confirms instant miner raw stock reward `0.00`.
  - Confirms jackpot reserve `2.00`.
  - Confirms founder GOLD and raw stock reward are `0.00`.
  - Confirms `isValidInstantMinerReward(8.00, 0.00)` is true.
  - Confirms overpaying miner GOLD or raw stock is false.
  - Confirms nonzero founder reward is false.

- `V35ParityTest`
  - Updated to check the new V35 reward helper method signatures.

Existing tests already cover:

- Founder reward policy disabled after V35.
- `V35_RESCUE_FORK_INDEX == Long.MAX_VALUE`.
- Normal indexes remain legacy.

Full object-level V35 block validation fixtures were not added in Phase 2B because constructing a signed block fixture through existing `Block`, `DtoTransaction`, and BASIS private key paths needs a broader fixture harness. The validation branch is compiled and covered indirectly by build verification; direct fixture tests should be added in Phase 2C before selecting a real fork index.

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

Targeted checks:

```powershell
cd C:\citu
git diff --stat -- <Phase 2B files>
git status --short -- <Phase 2B files and target directories>
```

## Test And Build Results

Wallet:

- `.\mvnw.cmd test`: PASS
- Tests run: 12
- Failures: 0
- Errors: 0
- Skipped: 0
- `.\mvnw.cmd clean package`: PASS

Server:

- `.\mvnw.cmd test`: PASS
- Tests run: 12
- Failures: 0
- Errors: 0
- Skipped: 0
- `.\mvnw.cmd clean package`: PASS

Maven warnings remain pre-existing duplicate dependency warnings in both projects. No `pom.xml` files were changed.

## Parity Checks

- Wallet/server V35 protocol helper files compare cleanly by SHA-256 list.
- Wallet/server V35 test files compare cleanly by SHA-256 list.
- The same V35 branch structure was added to wallet and server `Mining.java`.
- The same V35 validation branch structure was added to wallet and server `UtilsBlock.java`.

## Risks Before Phase 3

- `V35_RESCUE_FORK_INDEX` is still `Long.MAX_VALUE`; real activation requires a separate phase and more fixture tests.
- Direct signed V35 block validation tests are still missing.
- Jackpot reserve is not represented as a transaction or ledger entry yet; Phase 3 must define deterministic reserve accounting.
- `UtilsBalance` was not changed because Phase 2B creates only a miner BASIS transaction after V35. If a future jackpot reserve transaction is added, `UtilsBalance` must be V35-gated to allow that recipient.
- V35 still uses the existing block fields and existing difficulty/hash validation. Target-based PoW is not implemented yet.
- Manual difficulty still exists in mining because this phase did not implement V35 target difficulty.

## Rollback Plan

If a regression appears:

1. Revert Phase 2B edits in both wallet and server:
   - `Mining.java`
   - `UtilsBlock.java`
   - `V35RewardCalculator.java`
   - `V35RewardCalculatorTest.java`
   - `V35ParityTest.java`
   - this report
2. Keep Phase 1A constants, Phase 1B skeleton classes, Phase 1C tests, and Phase 2A plan unless a specific issue is found there.
3. Rerun:
   - `cd C:\citu\unitedStates_final-master; .\mvnw.cmd test`
   - `cd C:\citu\unitedStates_storage-master; .\mvnw.cmd test`
4. Confirm `V35_RESCUE_FORK_INDEX` is still `Long.MAX_VALUE`.

## Recommended Next Phase

Phase 2C should add signed V35 block reward fixtures before any broader consensus work:

- valid V35 miner-only reward block,
- V35 block with founder reward rejected,
- V35 block with miner reward above 8 rejected,
- V35 block with missing BASIS reward rejected,
- legacy block fixture still accepted before V35.
