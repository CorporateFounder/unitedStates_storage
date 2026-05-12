# CITU V35 Phase 1B Skeleton Report

## Scope
Phase 1B created isolated V35 helper skeleton classes only. No legacy mining, validation, reward, founder, hash, difficulty, transaction, signature, wallet UI, website, controller, or `pom.xml` behavior was changed.

## Package Used
`International_Trade_Union.protocol.v35`

## Files Created

### Wallet
- `C:\citu\unitedStates_final-master\src\main\java\International_Trade_Union\protocol\v35\ProtocolVersion.java`
- `C:\citu\unitedStates_final-master\src\main\java\International_Trade_Union\protocol\v35\V35RewardCalculator.java`
- `C:\citu\unitedStates_final-master\src\main\java\International_Trade_Union\protocol\v35\FounderRewardPolicy.java`
- `C:\citu\unitedStates_final-master\src\main\java\International_Trade_Union\protocol\v35\JackpotCalculator.java`
- `C:\citu\unitedStates_final-master\src\main\java\International_Trade_Union\protocol\v35\TicketWeightCalculator.java`
- `C:\citu\unitedStates_final-master\src\main\java\International_Trade_Union\protocol\v35\AssetDisplayMapper.java`
- `C:\citu\unitedStates_final-master\src\main\java\International_Trade_Union\protocol\v35\BurnAddressPolicy.java`
- `C:\citu\unitedStates_final-master\src\main\java\International_Trade_Union\protocol\v35\V35TargetDifficulty.java`
- `C:\citu\unitedStates_final-master\src\main\java\International_Trade_Union\protocol\v35\V35PowValidator.java`
- `C:\citu\unitedStates_final-master\src\main\java\International_Trade_Union\protocol\v35\V35WorkCalculator.java`
- `C:\citu\unitedStates_final-master\src\main\java\International_Trade_Union\protocol\v35\ChainTournamentScorer.java`

### Server
- `C:\citu\unitedStates_storage-master\src\main\java\International_Trade_Union\protocol\v35\ProtocolVersion.java`
- `C:\citu\unitedStates_storage-master\src\main\java\International_Trade_Union\protocol\v35\V35RewardCalculator.java`
- `C:\citu\unitedStates_storage-master\src\main\java\International_Trade_Union\protocol\v35\FounderRewardPolicy.java`
- `C:\citu\unitedStates_storage-master\src\main\java\International_Trade_Union\protocol\v35\JackpotCalculator.java`
- `C:\citu\unitedStates_storage-master\src\main\java\International_Trade_Union\protocol\v35\TicketWeightCalculator.java`
- `C:\citu\unitedStates_storage-master\src\main\java\International_Trade_Union\protocol\v35\AssetDisplayMapper.java`
- `C:\citu\unitedStates_storage-master\src\main\java\International_Trade_Union\protocol\v35\BurnAddressPolicy.java`
- `C:\citu\unitedStates_storage-master\src\main\java\International_Trade_Union\protocol\v35\V35TargetDifficulty.java`
- `C:\citu\unitedStates_storage-master\src\main\java\International_Trade_Union\protocol\v35\V35PowValidator.java`
- `C:\citu\unitedStates_storage-master\src\main\java\International_Trade_Union\protocol\v35\V35WorkCalculator.java`
- `C:\citu\unitedStates_storage-master\src\main\java\International_Trade_Union\protocol\v35\ChainTournamentScorer.java`

## Skeleton Match Confirmation
Wallet and server skeleton classes match by package, class name, method signatures, and implementation intent. They read the same `Seting.V35_*` constants and do not depend on controllers or legacy consensus classes.

## V35 Disabled Confirmation
V35 remains disabled because both wallet and server still define `Seting.V35_RESCUE_FORK_INDEX = Long.MAX_VALUE`. `ProtocolVersion.isV35(long index)` uses that constant, so normal chain indexes remain legacy.

## No Behavior Wired
The new classes are not called from `Mining.java`, `UtilsBlock.java`, `Blockchain.java`, `Block.java`, `DtoTransaction.java`, `Account.java`, controllers, wallet templates, website files, or existing sync/tournament code.

## Build Commands Run
- Wallet: `cd C:\citu\unitedStates_final-master; .\mvnw.cmd clean package`
- Server: `cd C:\citu\unitedStates_storage-master; .\mvnw.cmd clean package`

## Build Results
- Wallet build: passed. Maven reported pre-existing duplicate dependency warnings. No tests were present.
- Server build: passed. Maven reported pre-existing duplicate dependency warnings. Test sources compiled; Surefire reported `Tests run: 0`.

## Risks Before Phase 1C
- These classes are skeletons and do not prove legacy chain safety by themselves.
- `V35_BURN_ADDRESS` is still a TODO placeholder.
- `V35WorkCalculator` and `ChainTournamentScorer` are intentionally placeholders; they do not yet calculate real cumulative work or select chains.
- No baseline tests have been added yet to lock transaction signatures, old block hashes, old validation, or V35 helper behavior.

## Recommended Next Phase
Phase 1C: add verification and baseline tests for legacy signatures/hashes/validation and simple V35 helper methods, still without activating V35 consensus behavior.
