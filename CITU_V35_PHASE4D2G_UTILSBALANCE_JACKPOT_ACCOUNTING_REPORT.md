# CITU V35 Phase 4D-2G UtilsBalance Jackpot Accounting Report

Phase: 4D-2G  
Project root: `C:\citu`  
Scope: wire V35 jackpot payout accounting into `UtilsBalance` using explicit canonical chain context. No mining payout creation.

## Exact Files Changed

Wallet:

- `unitedStates_final-master/src/main/java/International_Trade_Union/utils/UtilsBalance.java`
- `unitedStates_final-master/src/test/java/International_Trade_Union/protocol/v35/UtilsBalanceV35JackpotAccountingTest.java`

Server:

- `unitedStates_storage-master/src/main/java/International_Trade_Union/utils/UtilsBalance.java`
- `unitedStates_storage-master/src/test/java/International_Trade_Union/protocol/v35/UtilsBalanceV35JackpotAccountingTest.java`

Report:

- `CITU_V35_PHASE4D2G_UTILSBALANCE_JACKPOT_ACCOUNTING_REPORT.md`

No changes were made to:

- `Mining.java`
- `UtilsBlock.java`
- `Block.java`
- `Blockchain.java`
- `DtoTransaction.java`
- `Account.java`
- controllers
- UI/templates
- website
- `pom.xml`

## Exact Methods Added / Changed

Changed in wallet and server `International_Trade_Union.utils.UtilsBalance`:

- `calculateBalances(List<Block> blocks)`
  - now delegates to the context-aware overload with `Seting.ADDRESS_FOUNDER` and `Seting.V35_RESCUE_FORK_INDEX`
  - because `V35_RESCUE_FORK_INDEX = Long.MAX_VALUE`, current behavior remains legacy

Added in wallet and server `UtilsBalance`:

- `calculateBalances(List<Block> blocks, String founderAddress, long forkIndex)`
- `applyV35JackpotPayoutsWithChainContext(Map<String, Account> balances, List<Block> canonicalChain, Block payoutBlock, String founderAddress, long forkIndex)`
- `isValidV35JackpotPayoutForBalance(List<Block> canonicalChain, Block payoutBlock, DtoTransaction payoutTransaction, String founderAddress, long forkIndex)`
- private `withoutV35JackpotPayoutCandidates(Block block)`
- private `chainBeforeBlock(List<Block> canonicalChain, long payoutBlockIndex)`
- private `containsV35JackpotPayoutCandidate(Block block)`
- private `isV35JackpotPayoutCandidate(DtoTransaction transaction)`
- private `isV35ActiveAt(long blockIndex, long forkIndex)`

## How UtilsBalance Uses Jackpot Proof Logic

The full-chain balance path is the context-safe path.

When V35 is active for a block:

1. `calculateBalances(..., founderAddress, forkIndex)` detects canonical jackpot-looking BASIS payouts.
2. It removes those jackpot payout candidates from legacy per-block accounting so they cannot be credited by amount-only or old founder/miner rules.
3. It then calls `applyV35JackpotPayoutsWithChainContext(...)`.
4. The accounting helper calls `V35JackpotChainContextAdapter.verifyPayoutWithChainContext(...)`.
5. Only a valid `JackpotProofResult` is credited through the existing `sendMoney(...)` BASIS accounting path.

Invalid jackpot candidates are skipped and do not increase recipient balances.

## Context-Safe Balance Paths

Full context:

- `calculateBalances(List<Block> blocks, String founderAddress, long forkIndex)`
- `applyV35JackpotPayoutsWithChainContext(...)`
- `isValidV35JackpotPayoutForBalance(...)`

These paths use an explicit canonical chain list and pass proof validation through `V35JackpotChainContextAdapter`.

Conservative / legacy context:

- `calculateBalance(Map<String, Account> balances, Block block, List<String> sign, List<String> signaturesNotTakenIntoAccount)`
- `rollbackCalculateBalance(Map<String, Account> balances, Block block)`

These per-block methods do not have enough canonical chain context to prove jackpot payouts. They were not changed to accept jackpot payouts independently.

## Duplicate Prevention Status

Duplicate prevention is handled through the shared proof path:

- same-period duplicate candidates inside the payout block are rejected by `V35JackpotProofService`
- prior payout markers are collected from the canonical chain context before the payout block
- duplicate same period/type payouts are not credited

The balance path intentionally passes only blocks before the payout block as prior context. The payout block itself is supplied separately so same-block duplicate detection remains deterministic and does not falsely mark the candidate as already paid.

## V35 Disabled Confirmation

Confirmed:

- wallet `Seting.V35_RESCUE_FORK_INDEX = Long.MAX_VALUE`
- server `Seting.V35_RESCUE_FORK_INDEX = Long.MAX_VALUE`

No real fork index was set. V35 remains disabled.

## Mining / Format Confirmation

`Mining.java` was not changed.

Mining still does not create jackpot payout transactions.

Transaction format was not changed.

Block format was not changed.

## Tests Added

Added in both wallet and server:

- `UtilsBalanceV35JackpotAccountingTest`

Coverage:

- valid jackpot payout increases winner GOLD balance through the explicit chain-context accounting path
- wrong winner payout does not increase balance
- immature payout does not increase balance
- duplicate same-period payout in the same block does not increase balance
- founder recipient does not increase balance
- V35 disabled with `Long.MAX_VALUE` does not credit jackpot payouts
- `6.00` instant miner reward is not classified/accounted as jackpot
- wrong noncanonical jackpot amount is not accepted by the jackpot accounting helper

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
- Tests run: 116
- Failures: 0
- Errors: 0
- Skipped: 0

Server `.\mvnw.cmd test`:

- PASS
- Tests run: 128
- Failures: 0
- Errors: 0
- Skipped: 0

## Build Results

Wallet `.\mvnw.cmd clean package`:

- PASS
- Tests run during package: 116
- Failures: 0
- Errors: 0
- Skipped: 0

Server `.\mvnw.cmd clean package`:

- PASS
- Tests run during package: 128
- Failures: 0
- Errors: 0
- Skipped: 0

Existing Maven warnings remain:

- duplicate `spring-boot-starter-test`
- duplicate/conflicting `h2`
- duplicate `spring-boot-starter-data-jpa`
- server duplicate `log4j:log4j`

No `pom.xml` files were changed.

## Risks Before Phase 4E

1. Mining still does not create jackpot payout transactions.
2. `UtilsBlock.validationOneBlock(...)` still rejects ambiguous jackpot payout candidates without explicit canonical chain context.
3. Per-block balance and rollback methods remain legacy/conservative because they do not receive full chain proof context.
4. The current first implementation keeps exact-slot payout timing. Late jackpot payout support would require a separate deterministic design or transaction metadata.
5. Full production safety still depends on future mining wiring using the same proof adapter before creating jackpot payout transactions.

## Recommended Next Phase

Phase 4E: mining creates jackpot payout transactions behind the V35 fork gate, using the same `V35JackpotChainContextAdapter` / `V35JackpotProofService` path and keeping V35 disabled until a real fork index is intentionally set.
