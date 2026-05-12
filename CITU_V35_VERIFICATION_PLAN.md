# CITU V35 Verification Plan

Phase 0 audit only. No source code changes were made.

## Discovered Verification Commands
- Wallet build: `cd C:\citu\unitedStates_final-master; .\mvnw.cmd clean package`
- Wallet tests: `cd C:\citu\unitedStates_final-master; .\mvnw.cmd test` (no tests discovered)
- Wallet run: `cd C:\citu\unitedStates_final-master; .\mvnw.cmd spring-boot:run`
- Server build: `cd C:\citu\unitedStates_storage-master; .\mvnw.cmd clean package`
- Server tests: `cd C:\citu\unitedStates_storage-master; .\mvnw.cmd test`
- Server run: `cd C:\citu\unitedStates_storage-master; .\mvnw.cmd spring-boot:run`
- Website build: `cd C:\citu\website.citu-web-master; .\mvnw.cmd clean package`
- Website tests: `cd C:\citu\website.citu-web-master; .\mvnw.cmd test`
- Website run: `cd C:\citu\website.citu-web-master; .\mvnw.cmd spring-boot:run`

## Required Proofs Before Code Release
1. Old blocks still validate before V35: run `UtilsBlock.validation(...)` and `Blockchain.checkFromFile(...)` against historical files, especially old fork boundaries.
2. V35 blocks reject founder reward: construct a V35 block with `BASIS_ADDRESS -> ADDRESS_FOUNDER`; assert `UtilsBlock.validationOneBlock(...)` returns false.
3. V35 fixed reward is exactly 10 GOLD: test total issuance values below, equal to, and above 10.
4. 8 GOLD instant plus 2 GOLD jackpot reserve: assert miner receives exactly 8, reserve receives exactly 2, and total issuance equals 10.
5. Target-based PoW works: unit-test `hashNumber <= target` with hashes below, equal to, and above target.
6. Automatic difficulty changes smoothly: synthetic fast/on-target/slow chains must adjust within max step bounds.
7. Emergency difficulty drop works: stalled block production must ease target within configured bounds.
8. Burn address cannot spend after V35: inbound burn allowed, outbound spend rejected, old blocks unaffected.
9. Jackpot winner is deterministic: same chain/tickets/seed produce same winner in wallet and server.
10. Wallet and server calculate the same rules: run shared fixture tests for rewards, target, burn policy, ticket weight, jackpot result, and chain score.
11. Website text matches protocol constants: compare rendered text or generated constants against `/api/v35/protocol`.
12. Manual difficulty is hidden after V35: UI hides/disables controls and consensus ignores manual difficulty.
13. One-button mining still creates valid blocks: Eco/Normal/Boost produce server-valid blocks and only change local resource behavior.
14. Chain tournament chooses highest cumulativeWork: lower-height higher-work chain beats higher-height lower-work chain.
15. Staking/tx boost cannot beat much higher PoW work: high stake/tx lower-work chain loses to much higher cumulativeWork.

## Minimum Regression Suite
- `DtoTransaction.toSign()` golden-output test.
- `DtoTransaction.verify()` golden signature test.
- `Block.hashForBlockchain()` golden old block hash test.
- `UtilsBlock.validationOneBlock(...)` old fixture block test.
- `Mining.miningDay(...)` V35 reward creation test.
- `Blockchain.shortCheck(...)` V35 cumulativeWork metadata test.
- `TournamentService` or successor chain selection test.
