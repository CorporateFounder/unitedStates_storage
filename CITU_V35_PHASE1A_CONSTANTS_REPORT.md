# CITU V35 Phase 1A Constants Report

## Scope
Phase 1A added V35 constants only. No mining, validation, reward, founder, hash, transaction, signature, wallet UI, or website behavior was changed.

## Files Changed
- `C:\citu\unitedStates_final-master\src\main\java\International_Trade_Union\setings\Seting.java`
- `C:\citu\unitedStates_storage-master\src\main\java\International_Trade_Union\setings\Seting.java`
- `C:\citu\CITU_V35_PHASE1A_CONSTANTS_REPORT.md`

## Protocol Constants Files Confirmed
- Wallet protocol constants: `C:\citu\unitedStates_final-master\src\main\java\International_Trade_Union\setings\Seting.java`
- Server protocol constants: `C:\citu\unitedStates_storage-master\src\main\java\International_Trade_Union\setings\Seting.java`
- Both are `International_Trade_Union.setings.Seting` interfaces.

## Constants Added
- `long V35_RESCUE_FORK_INDEX = Long.MAX_VALUE;`
- `double V35_GOLD_REWARD = 10.00;`
- `double V35_INSTANT_MINER_REWARD = 8.00;`
- `double V35_JACKPOT_REWARD = 2.00;`
- `double V35_DAILY_JACKPOT_PER_BLOCK = 0.70;`
- `double V35_WEEKLY_JACKPOT_PER_BLOCK = 0.40;`
- `double V35_MONTHLY_JACKPOT_PER_BLOCK = 0.30;`
- `double V35_QUARTERLY_JACKPOT_PER_BLOCK = 0.25;`
- `double V35_HALF_YEAR_JACKPOT_PER_BLOCK = 0.20;`
- `double V35_YEARLY_JACKPOT_PER_BLOCK = 0.15;`
- `int V35_SILVER_DISPLAY_MULTIPLIER = 100;`
- `int V35_TARGET_WINDOW = 60;`
- `int V35_TARGET_BLOCK_TIME_SECONDS = 150;`
- `double V35_MAX_TARGET_UP = 1.25;`
- `double V35_MAX_TARGET_DOWN = 0.50;`
- `long V35_EMERGENCY_DELAY_SECONDS = 1800;`
- `double V35_BASE_TICKET_WEIGHT = 1.00;`
- `double V35_MAX_TICKET_WEIGHT = 1.50;`
- `double V35_MAX_TX_BOOST = 0.30;`
- `double V35_MAX_STAKE_BOOST = 0.20;`
- `String V35_BURN_ADDRESS = "TODO_V35_BURN_ADDRESS";`
- `boolean V35_FOUNDER_REWARD_ENABLED = false;`
- `double V35_EXPECTED_FOUNDER_REWARD = 0.00;`

The jackpot per-block constants sum to `2.00`, matching `V35_JACKPOT_REWARD`.

## V35 Disabled Confirmation
V35 remains disabled because `V35_RESCUE_FORK_INDEX = Long.MAX_VALUE` in both wallet and server. No code paths were changed to read or enforce these constants.

## Behavior Change Confirmation
No behavior changed in this phase. The constants are inert placeholders until later phases add explicit fork-gated logic.

## Build Commands Run
- Wallet: `cd C:\citu\unitedStates_final-master; .\mvnw.cmd clean package`
- Server: `cd C:\citu\unitedStates_storage-master; .\mvnw.cmd clean package`

## Build Results
- Wallet build: passed. Maven reported pre-existing duplicate dependency warnings in `pom.xml`; no tests were present.
- Server build: passed. Maven reported pre-existing duplicate dependency warnings in `pom.xml`; test sources compiled, but Surefire reported `Tests run: 0`.

## Diff Notes
Running the Maven builds updated tracked generated files under `target/`. Those generated build artifacts were restored so the intended source/report diff remains limited to the two `Seting.java` files and this report.

## Risks / Notes
- Wallet and server `Seting.java` files differ in legacy constants and layout, but the V35 constants added in this phase match by name, type, and value.
- `V35_BURN_ADDRESS` is a TODO placeholder and must not be enforced until a final burn address is chosen.
- `V35_FOUNDER_REWARD_ENABLED = false` is only a placeholder constant; founder reward behavior has not changed.
- The constants are public interface constants, matching the existing code style.

## Next Recommended Phase
Phase 1B: add skeleton helper/classes or read-only structures for V35 protocol metadata, target/work calculation, jackpot status, and burn policy without activating consensus behavior.
