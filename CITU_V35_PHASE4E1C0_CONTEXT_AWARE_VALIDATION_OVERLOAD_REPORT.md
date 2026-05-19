# CITU V35 Phase 4E-1C0 Context-Aware Validation Overload Report

Phase: 4E-1C0  
Project root: `C:\citu`  
Scope: add context-aware jackpot validation helpers only. No production call sites wired.

## Exact Files Changed

Wallet:
- `C:\citu\unitedStates_final-master\src\main\java\International_Trade_Union\utils\UtilsBlock.java`
- `C:\citu\unitedStates_final-master\src\test\java\International_Trade_Union\protocol\v35\UtilsBlockV35JackpotChainContextTest.java`

Server:
- `C:\citu\unitedStates_storage-master\src\main\java\International_Trade_Union\utils\UtilsBlock.java`
- `C:\citu\unitedStates_storage-master\src\test\java\International_Trade_Union\protocol\v35\UtilsBlockV35JackpotChainContextTest.java`

Report:
- `C:\citu\CITU_V35_PHASE4E1C0_CONTEXT_AWARE_VALIDATION_OVERLOAD_REPORT.md`

No changes were made to:
- `Mining.java`
- `Blockchain.java`
- `BasisController.java`
- `TournamentService.java`
- `UtilsBalance.java`
- `Block.java`
- `DtoTransaction.java`
- `Account.java`
- controllers
- UI/templates
- website
- `pom.xml`

## Overloads / Helpers Added

Added to wallet and server `International_Trade_Union.utils.UtilsBlock`:

- `validateV35JackpotPayoutsWithCanonicalContext(List<Block> canonicalChain, Block payoutBlock, String founderAddress)`
- `validateV35JackpotPayoutsWithCanonicalContext(List<Block> canonicalChain, Block payoutBlock, String founderAddress, long forkIndex)`

These are block-level helpers for future production validation wiring. They scan BASIS jackpot payout candidates inside `payoutBlock` and validate each candidate through the existing proof path:

- `validateV35JackpotPayoutWithChainContext(...)`
- `V35JackpotChainContextAdapter.verifyPayoutWithChainContext(...)`
- `V35JackpotProofService.verifyPayout(...)`

The existing single-transaction entrypoints remain unchanged:

- `validateV35JackpotPayoutWithChainContext(List<Block>, Block, DtoTransaction, String)`
- `validateV35JackpotPayoutWithChainContext(List<Block>, Block, DtoTransaction, String, long)`

## Behavior of New Helper

When a block has jackpot payout candidates:

1. Requires V35 to be active for the block using the supplied fork index.
2. Requires explicit canonical chain context.
3. Classifies only canonical jackpot payout amounts.
4. Rejects duplicate same-period payouts in the same block.
5. Delegates each payout candidate to the existing chain-context proof service.
6. Rejects wrong winner, immature payout, prior duplicate, founder recipient, invalid amount, missing context, and disabled V35.

When a block has no jackpot payout candidates:

- The helper returns true for a simulated active V35 fork path.
- This keeps the `6.00` instant miner reward distinct from jackpot payout validation.

## Existing Behavior Preserved

`UtilsBlock.validationOneBlock(...)` was not changed.

Existing production call sites were not changed:
- `Blockchain.shortCheck(...)`
- `Blockchain.checkFromFile(...)`
- wallet `BasisController` local mined-block validation
- server `BasisController.resolve_conflict(...)`
- server `TournamentService`
- `UtilsResolving.addBlock3(...)`

Ordinary one-block validation without canonical context remains conservative. It still rejects V35 jackpot-looking BASIS payouts with the existing context-required behavior.

## V35 Disabled Confirmation

Confirmed before implementation:
- wallet `Seting.V35_RESCUE_FORK_INDEX = Long.MAX_VALUE`
- server `Seting.V35_RESCUE_FORK_INDEX = Long.MAX_VALUE`

No real fork index was set. V35 remains disabled.

## Tests Added / Updated

Updated in both wallet and server:
- `UtilsBlockV35JackpotChainContextTest`

Added coverage:
- block-level context-aware helper accepts a valid jackpot payout with full context
- block-level context-aware helper rejects missing context
- block-level context-aware helper rejects disabled production fork gate
- block-level context-aware helper rejects wrong recipient
- block-level context-aware helper rejects immature payout
- block-level context-aware helper rejects duplicate prior payout
- block-level context-aware helper rejects founder recipient
- block-level context-aware helper rejects duplicate same-period payout in one block
- block-level context-aware helper accepts multiple different period payouts in one block when proof-valid
- miner instant reward `6.00` remains non-jackpot and valid under simulated V35 helper logic

Full production `validationOneBlock(...)` call-site tests were not added in this phase because production wiring is intentionally deferred.

## Verification Commands

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

Cleanup after build artifacts:

```powershell
cd C:\citu
git restore unitedStates_final-master/target unitedStates_storage-master/target
```

## Test Results

Wallet:
- `.\mvnw.cmd test`: PASS, `125` tests, `0` failures, `0` errors
- `.\mvnw.cmd clean package`: PASS, `125` tests, `0` failures, `0` errors

Server:
- `.\mvnw.cmd test`: PASS, `137` tests, `0` failures, `0` errors
- `.\mvnw.cmd clean package`: PASS, `137` tests, `0` failures, `0` errors

Existing Maven model warnings remain:
- duplicate `spring-boot-starter-test`
- duplicate/conflicting `h2`
- duplicate `spring-boot-starter-data-jpa`
- server duplicate `log4j:log4j`

No `pom.xml` files were changed.

## Blockers Before Production Wiring

1. `Blockchain.shortCheck(...)` still calls `validationOneBlock(...)` without full jackpot context.
2. Server `BasisController.resolve_conflict(...)` still uses the old `Blockchain.shortCheck(...)` call.
3. Server `TournamentService` still uses the old `Blockchain.shortCheck(...)` call.
4. Wallet local mined-block validation still calls ordinary `validationOneBlock(...)`.
5. `UtilsResolving.addBlock3(...)` still saves and accounts blocks through existing per-block balance logic; jackpot payout blocks need context-safe accounting during save before mining creates payouts.
6. Mining still must not create jackpot payout transactions until the production validation and save/accounting paths are wired.

## Recommended Next Phase

Phase 4E-1C1: wire context-aware validation into `Blockchain.shortCheck(...)`, server `BasisController.resolve_conflict(...)`, server `TournamentService`, and wallet local mined-block validation where canonical context exists. Keep V35 disabled and do not wire Mining payout creation yet.
