# CITU V35 Risk Register

Phase 0 audit only. No source code changes were made.

## Critical Risks
1. Wallet/server consensus drift
- Files: duplicated `Seting.java`, `Mining.java`, `UtilsBlock.java`, `UtilsUse.java`, `UtilsDIfficult.java`, `BlockchainDifficulty.java`, `Blockchain.java`, `Block.java`, `DtoTransaction.java`.
- Risk: wallet mines blocks server rejects, or server accepts blocks wallet cannot reproduce.
- Mitigation: shared golden tests and synchronized patches.

2. Historical chain invalidation
- Files: `UtilsBlock.validationOneBlock(...)`, `UtilsBlock.validation(...)`, `Block.hashForBlockchain()`, `DtoTransaction.toSign()`, `BlockchainDifficulty`.
- Risk: old forks fail after V35 changes.
- Mitigation: explicit `index < V35_INDEX` old-rule branching and historical fixture validation.

3. Founder reward removal breaks old reward validation
- Files: `Mining.miningDay(...)`, `UtilsBlock.validationOneBlock(...)`, `Seting.java`.
- Risk: removing founder logic globally invalidates old blocks.
- Mitigation: only reject/create no founder reward at `index >= V35_INDEX`.

4. Transaction/signature format accidental change
- Files: `DtoTransaction.java`, `Laws.java`, `UtilsJson.java`, `UtilsSecurity.java`.
- Risk: all historical signatures become invalid.
- Mitigation: do not change `DtoTransaction.toSign()` or transaction fields.

5. Hash serialization accidental change
- Files: `Block.java`, inner `BlockForHash`, `UtilsJson.java`.
- Risk: historical block hashes become invalid.
- Mitigation: golden block hash tests before and after.

6. Manual difficulty remains consensus-active
- Files: `Mining.customDiff`, `MineController.customDiff`, `UtilsBlock.difficulty(...)`.
- Risk: users can select easier V35 difficulty.
- Mitigation: post-V35 consensus ignores manual difficulty; UI only controls local CPU mode.

7. Tournament can be gamed by staking/transactions/random
- Files: `TournamentService.sortWinner(...)`, `HostEndDataShortBComparator`, `UtilsResolving.isBig/isSmall`, `Blockchain.shortCheck(...)`.
- Risk: lower-work chains win.
- Mitigation: cumulativeWork primary, capped score only tie-breaker.

8. Current comparator bug ignores size
- File: `HostEndDataShortBComparator.java`.
- Detail: `if(d1.getSize() != d1.getSize())` is always false.
- Risk: priority host sorting ignores height.
- Mitigation: fix during V35 sync work with tests.

9. `hashCount` is not cumulative work
- Files: `Blockchain.shortCheck(...)`, `UtilsUse.powerDiff(...)`.
- Detail: `powerDiff(diff)` returns `diff`.
- Risk: chain quality metric is weak.
- Mitigation: add target-derived cumulativeWork.

10. Website/protocol story mismatch
- Files: website templates and server constants/API.
- Risk: public website says tokenomics different from code.
- Mitigation: website verification against `/api/v35/protocol`.

## Medium Risks
- Silver multiplier applied to consensus balances instead of display only.
- Jackpot reserve implemented as spendable ordinary account without deterministic policy.
- Burn address policy blocks historical or legitimate inbound burn transactions.
- Emergency difficulty drop can be abused if timestamp validation is weak.
- Java version mismatch: wallet/server Java 17, website Java 19.
- Existing tests are sparse: wallet has no test directory; server has only two test files discovered.

## Files Requiring Extra Review Before Any Commit
- `C:\citu\unitedStates_storage-master\src\main\java\International_Trade_Union\utils\UtilsBlock.java`
- `C:\citu\unitedStates_storage-master\src\main\java\International_Trade_Union\model\Mining.java`
- `C:\citu\unitedStates_storage-master\src\main\java\International_Trade_Union\model\TournamentService.java`
- `C:\citu\unitedStates_storage-master\src\main\java\International_Trade_Union\utils\UtilsResolving.java`
- `C:\citu\unitedStates_storage-master\src\main\java\International_Trade_Union\entity\blockchain\Blockchain.java`
- Same consensus files under `C:\citu\unitedStates_final-master`.
