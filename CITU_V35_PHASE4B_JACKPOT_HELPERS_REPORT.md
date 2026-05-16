# CITU V35 Phase 4B Jackpot Helpers Report

Phase: 4B  
Project root: `C:\citu`  
Created: 2026-05-16

## Scope

Implemented pure V35 jackpot helper logic and tests in wallet and server.

No jackpot payout wiring was added. No mining, validation, balance, BASIS transaction, API, controller, UI, website, transaction format, block format, or `pom.xml` behavior was changed.

## V35 Disabled Confirmation

V35 remains disabled in both projects:

- Wallet: `C:\citu\unitedStates_final-master\src\main\java\International_Trade_Union\setings\Seting.java`
  - `V35_RESCUE_FORK_INDEX = Long.MAX_VALUE`
- Server: `C:\citu\unitedStates_storage-master\src\main\java\International_Trade_Union\setings\Seting.java`
  - `V35_RESCUE_FORK_INDEX = Long.MAX_VALUE`

## Exact Files Changed

Wallet:

- `C:\citu\unitedStates_final-master\src\main\java\International_Trade_Union\protocol\v35\JackpotCalculator.java`
- `C:\citu\unitedStates_final-master\src\test\java\International_Trade_Union\protocol\v35\JackpotCalculatorTest.java`
- `C:\citu\unitedStates_final-master\src\test\java\International_Trade_Union\protocol\v35\V35ParityTest.java`

Server:

- `C:\citu\unitedStates_storage-master\src\main\java\International_Trade_Union\protocol\v35\JackpotCalculator.java`
- `C:\citu\unitedStates_storage-master\src\test\java\International_Trade_Union\protocol\v35\JackpotCalculatorTest.java`
- `C:\citu\unitedStates_storage-master\src\test\java\International_Trade_Union\protocol\v35\V35ParityTest.java`

Report:

- `C:\citu\CITU_V35_PHASE4B_JACKPOT_HELPERS_REPORT.md`

## Helper Methods Added

Wallet and server `JackpotCalculator.java` now expose:

- `reservePerBlockForPeriod(String period)`
- `totalReservePerBlock()`
- `validateReserveSplit()`
- `dailyWindowBlocks()`
- `weeklyWindowBlocks()`
- `monthlyWindowBlocks()`
- `quarterlyWindowBlocks()`
- `halfYearWindowBlocks()`
- `yearlyWindowBlocks()`
- `periodStart(long blockIndex, long windowSize)`
- `periodEnd(long blockIndex, long windowSize)`
- `isPeriodEnd(long blockIndex, long windowSize)`
- `maturityBlock(long periodEnd, long delayBlocks)`
- `selectWinnerOffset(String seedHex, int eligibleTicketCount)`
- `combineHashesForSeed(List<String> futureHashes)`
- `baseTicketCountForBlock()`
- `isEligibleTicketBlock(boolean isV35Block, String minerAddress)`

Existing methods were preserved:

- `getDailyPerBlock()`
- `getWeeklyPerBlock()`
- `getMonthlyPerBlock()`
- `getQuarterlyPerBlock()`
- `getHalfYearPerBlock()`
- `getYearlyPerBlock()`
- `getTotalPerBlock()`
- `validateSplit()`

## Period Window Values

V35 target block time remains `150` seconds. The fixed jackpot windows are:

- daily: `576` blocks
- weekly: `4032` blocks
- monthly: `17280` blocks
- quarterly: `51840` blocks
- half-year: `104832` blocks
- yearly: `210240` blocks

## Random Delay Value

`JackpotCalculator.JACKPOT_RANDOM_DELAY_BLOCKS = 60`

This is local to `JackpotCalculator` for Phase 4B. `Seting.java` was not changed.

## Reserve Split Validation

Per-block reserve constants remain sourced from `Seting`:

- daily: `0.70`
- weekly: `0.40`
- monthly: `0.30`
- quarterly: `0.25`
- half-year: `0.20`
- yearly: `0.15`
- total: `2.00`

`validateReserveSplit()` confirms the split equals `Seting.V35_JACKPOT_REWARD`.

## Winner Offset And Seed Helpers

`selectWinnerOffset(seedHex, eligibleTicketCount)`:

- returns `-1` for null, empty, invalid hex, or non-positive ticket count
- parses the seed as unsigned hexadecimal
- returns `seedNumber mod eligibleTicketCount`
- always returns an offset inside `[0, eligibleTicketCount - 1]` for valid input

`combineHashesForSeed(futureHashes)`:

- concatenates non-null, non-empty trimmed hashes in list order
- returns SHA-256 hex of the concatenated text
- returns `""` for null, empty, or all-empty inputs
- uses no external randomness

This phase does not inspect actual blocks and does not select a miner address. That remains Phase 4C.

## Ticket Eligibility

Phase 4B implements the base unweighted model:

- `baseTicketCountForBlock() = 1`
- `isEligibleTicketBlock(...)` requires:
  - V35 block flag is true
  - non-blank miner address

Sender addresses are not used by these helpers.

Advanced weighted ticket selection was not implemented in this phase.

## Tests Added Or Updated

Wallet and server `JackpotCalculatorTest` now cover:

- reserve constants and total split
- reserve lookup by period name
- fixed window sizes
- deterministic period start/end
- period-end detection
- maturity block = period end + 60
- safe invalid period inputs
- deterministic winner offset
- bounded winner offset
- invalid seed/ticket inputs
- deterministic seed combination
- seed ordering sensitivity
- safe invalid seed inputs
- base ticket eligibility using V35 flag and miner address

Wallet and server `V35ParityTest` were updated to include the new helper method signatures.

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

Cleanup:

```powershell
cd C:\citu
git restore unitedStates_final-master/target unitedStates_storage-master/target
```

## Test Results

Wallet `.\mvnw.cmd test`:

- Result: PASS
- Tests run: 71
- Failures: 0
- Errors: 0
- Skipped: 0

Server `.\mvnw.cmd test`:

- Result: PASS
- Tests run: 83
- Failures: 0
- Errors: 0
- Skipped: 0

## Build Results

Wallet `.\mvnw.cmd clean package`:

- Result: PASS
- Tests run during package: 71
- Failures: 0
- Errors: 0
- Skipped: 0
- Jar built successfully: `target\unitedStates-0.32.216-SNAPSHOT.jar`

Server `.\mvnw.cmd clean package`:

- Result: PASS
- Tests run during package: 83
- Failures: 0
- Errors: 0
- Skipped: 0
- Jar built successfully: `target\unitedStates-0.0.423-SNAPSHOT.jar`

Pre-existing Maven warnings remain:

- duplicate `spring-boot-starter-test`
- duplicate/conflicting `h2`
- duplicate `spring-boot-starter-data-jpa`
- server duplicate `log4j:log4j`

No `pom.xml` files were changed.

## Behavior Confirmation

- V35 remains disabled.
- No mining behavior changed.
- No validation behavior changed.
- No balance behavior changed.
- No BASIS payout wiring was added.
- No transaction format changed.
- No block format changed.
- No controller/API/UI/website files were changed.
- Wallet and server helper behavior is mirrored by matching code and tests.

## Risks Before Phase 4C

1. Phase 4B selects only an offset from a seed and ticket count; it does not inspect block ranges or miner addresses yet.
2. Future Phase 4C must define block-range fixtures and prove the selected offset maps to the correct mined block and miner address.
3. Jackpot seed hashing currently concatenates hash strings directly; Phase 4C should lock exact canonical hash normalization for real block hashes.
4. Weighted ticket selection is intentionally deferred; adding it later must not create spam incentives.
5. Payout validation and balance logic still reject jackpot-style BASIS payouts today, as intended for this phase.

## Recommended Next Phase

Phase 4C: deterministic jackpot winner selection using block ranges, still no payout wiring.
