# CITU V35 Phase 4D-2F UtilsBlock Chain-Context Jackpot Validation Report

Phase: 4D-2F  
Project root: `C:\citu`  
Scope: add an explicit `UtilsBlock` jackpot validation entrypoint with canonical chain context. No production wiring into mining or balances.

## Exact Files Changed

Wallet:

- `unitedStates_final-master/src/main/java/International_Trade_Union/utils/UtilsBlock.java`
- `unitedStates_final-master/src/test/java/International_Trade_Union/protocol/v35/UtilsBlockV35JackpotChainContextTest.java`

Server:

- `unitedStates_storage-master/src/main/java/International_Trade_Union/utils/UtilsBlock.java`
- `unitedStates_storage-master/src/test/java/International_Trade_Union/protocol/v35/UtilsBlockV35JackpotChainContextTest.java`

Report:

- `CITU_V35_PHASE4D2F_UTILSBLOCK_CHAIN_CONTEXT_JACKPOT_VALIDATION_REPORT.md`

No changes were made to:

- `Mining.java`
- `UtilsBalance.java`
- `Block.java`
- `Blockchain.java`
- `DtoTransaction.java`
- `Account.java`
- controllers
- UI/templates
- website
- `pom.xml`

## Exact Methods Added

Added to wallet and server `International_Trade_Union.utils.UtilsBlock`:

- `validateV35JackpotPayoutWithChainContext(List<Block> canonicalChain, Block payoutBlock, DtoTransaction payoutTransaction, String founderAddress)`
- `validateV35JackpotPayoutWithChainContext(List<Block> canonicalChain, Block payoutBlock, DtoTransaction payoutTransaction, String founderAddress, long forkIndex)`
- private helper `invalidV35JackpotProof(String reason, DtoTransaction payoutTransaction)`

## How Explicit Chain-Context Validation Works

The new `UtilsBlock` entrypoint requires explicit canonical context:

- canonical chain list
- payout block
- payout transaction
- founder address
- fork index, for pure simulated tests

Validation flow:

1. Reject missing or empty canonical chain context.
2. Reject missing payout block or transaction.
3. Reject noncanonical jackpot payout amounts.
4. Delegate to `V35JackpotChainContextAdapter.verifyPayoutWithChainContext(...)`.
5. Return the adapter/proof result as `JackpotProofResult`.

The production-facing overload uses:

- `Seting.V35_RESCUE_FORK_INDEX`

Since V35 is still disabled, the production-facing overload rejects jackpot payouts.

## Ordinary One-Block Validation Safety

`UtilsBlock.validationOneBlock(...)` remains safely conservative.

Current behavior is unchanged:

- pre-V35 legacy validation path remains legacy
- while `V35_RESCUE_FORK_INDEX = Long.MAX_VALUE`, current live blocks stay on legacy path
- if the V35 branch sees a jackpot payout candidate without full canonical context, it still rejects it with the existing message:
  - `V35 jackpot payout proof validation requires canonical chain context`

The new helper is not called by ordinary one-block validation.

## V35 Disabled Confirmation

Confirmed:

- wallet `Seting.V35_RESCUE_FORK_INDEX = Long.MAX_VALUE`
- server `Seting.V35_RESCUE_FORK_INDEX = Long.MAX_VALUE`

No real fork index was set. V35 remains disabled.

## Production Behavior Confirmation

No production path was wired to accept jackpot payouts.

The new entrypoint is callable only by future code/tests that provide explicit context. It does not alter:

- mining behavior
- balance behavior
- ordinary block validation behavior
- transaction format
- block format
- API/controllers
- UI/templates
- website

## Mining / Balance Confirmation

`Mining.java` was not changed.

Mining still does not create jackpot payout transactions.

`UtilsBalance.java` was not changed.

Balance accounting still does not credit jackpot payouts to historical winner miners.

## Tests Added

Added in both wallet and server:

- `UtilsBlockV35JackpotChainContextTest`

Coverage:

- explicit chain-context helper accepts valid jackpot proof under simulated V35 fork index
- missing canonical context rejects ambiguous jackpot payout
- wrong recipient rejected
- immature payout rejected
- duplicate payout rejected
- founder recipient rejected
- wrong amount rejected
- disabled production fork gate rejects even with otherwise valid context
- miner instant reward `6.00` remains distinct from jackpot payout amounts

Note:

- Full `validationOneBlock(...)` signed block fixtures remain out of scope here. The ordinary one-block V35 jackpot candidate rejection path remains unchanged from Phase 4D-2B and is covered there by reward-shape fixture tests.

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

Cleanup after build artifact generation:

```powershell
cd C:\citu
git restore unitedStates_final-master/target unitedStates_storage-master/target
```

## Test Results

Wallet `.\mvnw.cmd test`:

- PASS
- Tests run: 113
- Failures: 0
- Errors: 0
- Skipped: 0

Server `.\mvnw.cmd test`:

- PASS
- Tests run: 125
- Failures: 0
- Errors: 0
- Skipped: 0

## Build Results

Wallet `.\mvnw.cmd clean package`:

- PASS
- Tests run during package: 113
- Failures: 0
- Errors: 0
- Skipped: 0

Server `.\mvnw.cmd clean package`:

- PASS
- Tests run during package: 125
- Failures: 0
- Errors: 0
- Skipped: 0

Existing Maven warnings remain:

- duplicate `spring-boot-starter-test`
- duplicate/conflicting `h2`
- duplicate `spring-boot-starter-data-jpa`
- server duplicate `log4j:log4j`

No `pom.xml` files were changed.

## Remaining Risks Before UtilsBalance Wiring

1. `UtilsBlock.validationOneBlock(...)` still does not receive complete canonical context, so it must continue rejecting ambiguous jackpot payouts.
2. Future wiring must call the new entrypoint only from a validation path with the complete canonical chain range.
3. `UtilsBalance` still rejects or ignores BASIS payouts that are not founder/current-miner shaped; jackpot winner accounting needs its own phase.
4. Mining still does not create jackpot payouts.
5. Exact-slot payout timing remains strict. Late payout support would require a separate design.

## Recommended Next Phase

Phase 4D-2G: wire `UtilsBalance` jackpot payout accounting using the same proof adapter, still behind V35 and still without mining payout creation.

