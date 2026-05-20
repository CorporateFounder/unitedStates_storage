# CITU V35 Phase 4E-1E Jackpot E2E Test Report

## Scope

Phase 4E-1E added end-to-end tests only. No production Java source was changed, V35 was not activated, and no transaction, block, hash, UI, website, controller, or `pom.xml` files were changed.

`V35_RESCUE_FORK_INDEX` remains `Long.MAX_VALUE` in both wallet and server, so the live runtime path remains legacy.

## Exact test files changed

Created matching wallet/server tests:

- `C:\citu\unitedStates_final-master\src\test\java\International_Trade_Union\model\MiningV35JackpotPayoutE2ETest.java`
- `C:\citu\unitedStates_storage-master\src\test\java\International_Trade_Union\model\MiningV35JackpotPayoutE2ETest.java`

No production files were changed in this phase.

## Full path covered

The new tests use the existing test-only simulated fork helper in `Mining.buildV35JackpotPayoutTransactionsForMining(..., forkIndex)` so V35 behavior can be exercised without changing `Seting.V35_RESCUE_FORK_INDEX`.

Covered path:

1. Mining builder path:
   - `Mining.buildV35JackpotPayoutTransactionsForMining(...)`
   - `V35JackpotPayoutBuilder`
   - `V35JackpotChainContextAdapter`
   - `V35JackpotProofService`

2. Context-aware validation:
   - `UtilsBlock.validateV35JackpotPayoutWithChainContext(...)`
   - `UtilsBlock.validateV35JackpotPayoutsWithCanonicalContext(...)`

3. Balance accounting:
   - `UtilsBalance.applyV35JackpotPayoutsWithChainContext(...)`

4. Disabled fork safety:
   - public mining helper with `Seting.V35_RESCUE_FORK_INDEX = Long.MAX_VALUE` returns no jackpot payouts.

## Assertions added

The tests prove:

- while V35 is disabled, mining creates no jackpot payouts;
- a simulated V35 daily payout built by mining is `BASIS_ADDRESS -> deterministic winner`;
- payout amount is canonical and stock/silver is `0.00`;
- generated payout passes the context-aware `UtilsBlock` proof path;
- missing canonical context is conservative and rejects jackpot payout validation;
- valid payout credits the winner through `UtilsBalance.applyV35JackpotPayoutsWithChainContext(...)`;
- wrong winner, wrong amount, immature payout, founder recipient, and duplicate same-period payout are rejected or not credited;
- sender addresses are not jackpot winners;
- missed exact-slot payout is not created later;
- daily and weekly matured payouts can be generated, validated, and accounted together;
- duplicate same-period payout in a block is rejected by `UtilsBlock`.

## Helper-level only / not fully covered

No full runtime `BasisController`, persistent storage, or live `UtilsResolving.addBlock3(...)` database integration test was added in this phase. Those paths are still covered by the context-aware helper tests added in earlier phases plus the new mining-to-validation-to-balance test.

The ordinary one-block validation path remains intentionally conservative for jackpot payouts without canonical context. This phase proves the explicit no-context jackpot helper rejects missing context; it does not attempt to build a full signed mined block through every legacy `validationOneBlock(...)` requirement.

Duplicate same-period payouts are rejected at the validation gate. Balance accounting is tested only on validation-approved payout blocks, which is the intended production flow.

## Production behavior

Production behavior changed: no.

Files intentionally not changed:

- `Mining.java`
- `UtilsBlock.java`
- `Blockchain.java`
- `UtilsResolving.java`
- `UtilsBalance.java`
- `Block.java`
- `DtoTransaction.java`
- `Account.java`
- controllers
- templates/UI
- website
- `pom.xml`

## Verification commands and results

Wallet:

- Command: `.\mvnw.cmd test`
- Result: passed, `137` tests, `0` failures, `0` errors

- Command: `.\mvnw.cmd clean package`
- Result: passed, `137` tests, `0` failures, `0` errors

Server:

- Command: `.\mvnw.cmd test`
- Result: passed, `149` tests, `0` failures, `0` errors

- Command: `.\mvnw.cmd clean package`
- Result: passed, `149` tests, `0` failures, `0` errors

Build warnings observed:

- Existing duplicate Maven dependency warnings remain in both wallet and server `pom.xml`.
- No `pom.xml` changes were made.

## Remaining blockers before burn/founder phase

1. Full controller/storage integration tests for jackpot payout blocks are still not present.
2. Full `UtilsResolving` database-backed add/rollback tests remain difficult because they require more runtime infrastructure than the pure/context helper tests.
3. Because V35 remains disabled, simulated fork-index tests are the only safe way to exercise V35 jackpot payout behavior.
4. Before activation, run an end-to-end local node scenario with a real temporary V35 fork index on an isolated test chain.

## Recommended next phase

Phase 5A - founder burn / supply policy plan.
