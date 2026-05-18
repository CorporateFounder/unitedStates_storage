# CITU V35 Phase 4D-1 Jackpot Payout Classification Plan

Phase: 4D-1  
Scope: planning only, no source-code changes  
Project root: `C:\citu`

## 1. Files And Methods Inspected

### Wallet project

- `unitedStates_final-master/src/main/java/International_Trade_Union/entity/DtoTransaction/DtoTransaction.java`
  - fields: `sender`, `customer`, `digitalDollar`, `digitalStockBalance`, `laws`, `bonusForMiner`, `voteEnum`, `sign`
  - constructor: `DtoTransaction(String sender, String customer, double digitalDollar, double digitalStockBalance, Laws laws, double bonusForMiner, VoteEnum voteEnum)`
  - methods inspected: `toSign()`, `verify()`, `equals(...)`, `hashCode()`, getters/setters
- `unitedStates_final-master/src/main/java/International_Trade_Union/protocol/v35/JackpotCalculator.java`
  - period constants: `PERIOD_DAILY`, `PERIOD_WEEKLY`, `PERIOD_MONTHLY`, `PERIOD_QUARTERLY`, `PERIOD_HALF_YEAR`, `PERIOD_YEARLY`
  - window helpers: `dailyWindowBlocks()`, `weeklyWindowBlocks()`, `monthlyWindowBlocks()`, `quarterlyWindowBlocks()`, `halfYearWindowBlocks()`, `yearlyWindowBlocks()`
  - reserve helpers: `reservePerBlockForPeriod(...)`, `totalReservePerBlock()`, `validateReserveSplit()`
  - winner helpers: `selectWinnerOffset(...)`, `combineHashesForSeed(...)`, `seedFromFutureHashes(...)`, `isMature(...)`, `selectWinner(...)`
  - payout helpers: `expectedPayoutAmount(...)`, `isV35JackpotPayoutAllowedAt(...)`, `isValidJackpotPayout(...)`
- `unitedStates_final-master/src/main/java/International_Trade_Union/protocol/v35/JackpotTicket.java`
  - fields: `blockIndex`, `minerAddress`, `weight`
  - method: `isValid()`
- `unitedStates_final-master/src/main/java/International_Trade_Union/protocol/v35/JackpotWinnerProof.java`
  - fields: `periodStart`, `periodEnd`, `maturityBlock`, `randomDelayBlocks`, `seedHex`, `selectedOffset`, `selectedBlockIndex`, `winnerMinerAddress`
  - method: `hasWinner()`
- `unitedStates_final-master/src/main/java/International_Trade_Union/utils/UtilsBlock.java`
  - method: `validationOneBlock(...)`
  - inspected V35 `BASIS_ADDRESS` reward validation branch
- `unitedStates_final-master/src/main/java/International_Trade_Union/utils/UtilsBalance.java`
  - methods: `calculateBalance(...)`, `rollbackCalculateBalance(...)`, `sendMoney(...)`, `rollBackSendMoney(...)`
- `unitedStates_final-master/src/main/java/International_Trade_Union/model/Mining.java`
  - context only: V35 branch creates one `BASIS_ADDRESS -> minerAddress` instant reward transaction

### Server project

- `unitedStates_storage-master/src/main/java/International_Trade_Union/entity/DtoTransaction/DtoTransaction.java`
- `unitedStates_storage-master/src/main/java/International_Trade_Union/protocol/v35/JackpotCalculator.java`
- `unitedStates_storage-master/src/main/java/International_Trade_Union/protocol/v35/JackpotTicket.java`
- `unitedStates_storage-master/src/main/java/International_Trade_Union/protocol/v35/JackpotWinnerProof.java`
- `unitedStates_storage-master/src/main/java/International_Trade_Union/utils/UtilsBlock.java`
- `unitedStates_storage-master/src/main/java/International_Trade_Union/utils/UtilsBalance.java`
- `unitedStates_storage-master/src/main/java/International_Trade_Union/model/Mining.java`

The wallet and server implementations are intended to remain mirrored for these protocol helpers and consensus-validation paths.

## 2. Recommended Payout Classification Model

Recommended model: keep the transaction format unchanged and classify V35 `BASIS_ADDRESS` transactions by strict V35 consensus rules:

1. `BASIS_ADDRESS -> founder` after V35 is always a forbidden founder reward.
2. `BASIS_ADDRESS -> current block miner` for exactly `8.00 GOLD`, `0.00 Silver/raw stock`, is the instant miner reward.
3. Any other `BASIS_ADDRESS` transaction after V35 is a jackpot payout candidate only if:
   - `digitalDollar` exactly matches one canonical jackpot period payout amount,
   - `digitalStockBalance` is exactly `0.00`,
   - `bonusForMiner` is exactly `0.00`,
   - recipient is not the founder address,
   - recipient matches the deterministic winner for the inferred period instance,
   - the period is mature,
   - the same period/type has not already been paid,
   - the payout is not before V35.
4. Any `BASIS_ADDRESS` transaction that is neither the one instant miner reward nor a valid jackpot payout is invalid.

This means period type is inferred by amount, but amount is not trusted alone. It must be combined with period maturity, deterministic winner proof, duplicate-payout scan, and recipient checks.

## 3. Expected Payout Amounts By Period

Using the fixed block windows from Phase 4B and the per-block reserves:

| Period | Window blocks | Reserve/block | Expected payout |
|---|---:|---:|---:|
| Daily | 576 | 0.70 | 403.20 GOLD |
| Weekly | 4,032 | 0.40 | 1,612.80 GOLD |
| Monthly | 17,280 | 0.30 | 5,184.00 GOLD |
| Quarterly | 51,840 | 0.25 | 12,960.00 GOLD |
| Half-year | 104,832 | 0.20 | 20,966.40 GOLD |
| Yearly | 210,240 | 0.15 | 31,536.00 GOLD |

Integer-cent equivalents:

| Period | Expected cents |
|---|---:|
| Daily | 40,320 |
| Weekly | 161,280 |
| Monthly | 518,400 |
| Quarterly | 1,296,000 |
| Half-year | 2,096,640 |
| Yearly | 3,153,600 |

The instant miner reward is `8.00 GOLD`, or `800` cents, and does not collide with any jackpot payout amount.

## 4. Whether Amount-Based Inference Is Safe

The six payout amounts are unique at two-decimal precision. Amount-based period inference is therefore usable as a compact classifier without adding a transaction marker.

However, amount alone is not safe enough for consensus. A malicious node could create a `BASIS_ADDRESS` transaction with one of the correct amounts but the wrong recipient, wrong period, premature timing, duplicate period, or no deterministic proof. Phase 4D-2 should treat amount as the first classification filter only.

Recommended rule:

- If amount matches exactly one canonical jackpot amount, classify it as a candidate for that period type.
- If amount matches no canonical jackpot amount, reject it as an invalid V35 `BASIS_ADDRESS` payout unless it is the exact instant miner reward.
- If any future change creates an amount collision, reject ambiguous classification until a safer marker is introduced.

## 5. Rounding And Two-Decimal Precision

Future code should avoid raw `double` equality for jackpot consensus classification.

Recommended implementation:

- Convert `digitalDollar` to integer cents before classification.
- Reject values that cannot be represented exactly at two decimal places.
- Compare against canonical integer-cent constants:
  - instant miner reward: `800`
  - daily jackpot: `40320`
  - weekly jackpot: `161280`
  - monthly jackpot: `518400`
  - quarterly jackpot: `1296000`
  - half-year jackpot: `2096640`
  - yearly jackpot: `3153600`
- Use the same helper in wallet and server.

If the current code style requires `double` in public helper methods, the helper should still normalize internally using `BigDecimal.valueOf(value).setScale(2, RoundingMode.UNNECESSARY)` or equivalent safe cents conversion.

## 6. Duplicate Payout Prevention

Duplicate prevention should key each payout by:

`periodType + ":" + periodStart + ":" + periodEnd`

Phase 4D-2 should scan:

1. The canonical chain from `Seting.V35_RESCUE_FORK_INDEX` through the previous accepted block.
2. Any earlier transactions in the candidate block currently being validated.

For each `BASIS_ADDRESS` transaction in that scan:

- Classify it using the same V35 jackpot classifier.
- If it is a valid jackpot payout for the same key, mark the key as already paid.
- If the candidate block attempts the same key again, reject it.

Legacy blocks before V35 must not be scanned as jackpot payouts. Legacy `hashCompexity`, reward format, founder reward, and historical `BASIS_ADDRESS` behavior must not be reinterpreted.

If full historical block access is unavailable inside `UtilsBlock.validationOneBlock(...)`, Phase 4D-2 should add helper-level duplicate tests and document the missing canonical-chain access path before wiring full validation.

## 7. How UtilsBlock Should Validate Jackpot BASIS Transactions

Current V35 behavior in `UtilsBlock.validationOneBlock(...)` accepts exactly one V35 `BASIS_ADDRESS` transaction: the `8.00 GOLD` instant miner reward to `thisBlock.getMinerAddress()`.

Phase 4D-2 should replace the V35 `BASIS_ADDRESS` branch with a classifier-driven flow:

1. Initialize:
   - `v35InstantMinerRewardCount = 0`
   - `v35JackpotPayoutCount = 0`
   - `paidJackpotKeysInCandidateBlock = Set<String>`
2. For each transaction where `sender == Seting.BASIS_ADDRESS`:
   - If recipient is founder address: reject.
   - Else if it is the instant miner reward:
     - require recipient equals `thisBlock.getMinerAddress()`
     - require `digitalDollar == 8.00` by cents comparison
     - require `digitalStockBalance == 0.00`
     - increment instant miner count
     - reject if count exceeds one
   - Else classify as jackpot payout candidate:
     - infer period type from exact payout amount
     - require `digitalStockBalance == 0.00`
     - require `bonusForMiner == 0.00`
     - require period maturity
     - compute deterministic winner proof from eligible V35 blocks and delayed future hashes
     - require recipient equals winner miner address
     - reject if duplicate key was already paid historically or earlier in the same block
     - increment jackpot payout count
   - Else reject as invalid `BASIS_ADDRESS` payout.
3. After transaction loop:
   - require exactly one instant miner reward for V35 non-genesis blocks
   - allow up to six jackpot payouts in one block, one per period type, only when due and unpaid
   - do not require jackpot payout transactions until Phase 4E defines mining payout timing

This preserves the Phase 2B rule that founder reward is forbidden after V35 and extends it to classify jackpot payouts safely.

## 8. How UtilsBalance Should Treat Jackpot Payouts

Current `UtilsBalance` logic restricts `BASIS_ADDRESS` recipients to founder or miner and limits the count to two in legacy paths. After V35, that would reject valid jackpot winners unless updated.

Phase 4D-2 should make `UtilsBalance` use the same V35 classifier as `UtilsBlock`:

- Before V35: preserve existing legacy balance behavior.
- After V35:
  - accept the instant miner reward only to the current block miner,
  - accept jackpot payouts only when the transaction classifies as a valid jackpot payout,
  - reject founder reward,
  - reject invalid `BASIS_ADDRESS` recipients,
  - allow a maximum of one instant miner reward plus up to six jackpot payouts per V35 block.

The lower-level balance application methods `sendMoney(...)` and `rollBackSendMoney(...)` can continue to add/subtract `digitalDollar` for `BASIS_ADDRESS` recipients once validation/classification has allowed the transaction.

To prevent wallet/server divergence, jackpot payout classification should live in shared V35 helper code, not duplicated independently in `UtilsBlock` and `UtilsBalance`.

## 9. Safe Implementation Order For Phase 4D-2

1. Add pure jackpot payout classification helpers in `JackpotCalculator` or small protocol-v35 model classes.
2. Add exact integer-cent amount mapping tests for all six periods and the `8.00 GOLD` instant reward.
3. Add helper tests for invalid amount, ambiguous amount, wrong stock amount, nonzero `bonusForMiner`, founder recipient, immature period, wrong winner, and duplicate key.
4. Wire `UtilsBlock.validationOneBlock(...)` V35 `BASIS_ADDRESS` branch to the classifier behind `ProtocolVersion.isV35(index)`.
5. Keep pre-V35 legacy reward validation untouched.
6. Wire `UtilsBalance` V35 `BASIS_ADDRESS` counting/recipient allowance to the same classifier.
7. Add wallet/server parity tests for classification and balance acceptance rules.
8. Run wallet and server tests/builds.
9. Document any remaining fixture blockers before moving to Phase 4E mining payout creation.

## 10. Files Phase 4D-2 May Edit

Recommended allowed files:

- `unitedStates_final-master/src/main/java/International_Trade_Union/protocol/v35/JackpotCalculator.java`
- `unitedStates_storage-master/src/main/java/International_Trade_Union/protocol/v35/JackpotCalculator.java`
- Optional new mirrored helper classes under:
  - `unitedStates_final-master/src/main/java/International_Trade_Union/protocol/v35/`
  - `unitedStates_storage-master/src/main/java/International_Trade_Union/protocol/v35/`
- `unitedStates_final-master/src/main/java/International_Trade_Union/utils/UtilsBlock.java`
- `unitedStates_storage-master/src/main/java/International_Trade_Union/utils/UtilsBlock.java`
- `unitedStates_final-master/src/main/java/International_Trade_Union/utils/UtilsBalance.java`
- `unitedStates_storage-master/src/main/java/International_Trade_Union/utils/UtilsBalance.java`
- V35 jackpot and validation tests under both projects
- `CITU_V35_PHASE4D2_JACKPOT_PAYOUT_CLASSIFICATION_REPORT.md`

## 11. Files Phase 4D-2 Must Not Edit

- `Mining.java`, until Phase 4E payout creation
- `DtoTransaction.java`
- `Block.java`
- `Blockchain.java`, unless a later phase explicitly permits canonical-chain helper access
- `Account.java`
- controllers/API files
- UI templates/static files
- website files
- `pom.xml`
- transaction signing code
- block/hash format code

## 12. Required Tests

Tests should be mirrored in wallet and server where practical.

Required classifier tests:

- `8.00 GOLD` classifies as instant miner reward only when recipient is the current miner.
- Daily `403.20`, weekly `1612.80`, monthly `5184.00`, quarterly `12960.00`, half-year `20966.40`, and yearly `31536.00` classify to the expected period types.
- All jackpot payout amounts are unique at integer-cent precision.
- Noncanonical amount is rejected.
- Amount with more than two decimals is rejected.
- Jackpot payout with nonzero `digitalStockBalance` is rejected.
- Jackpot payout with nonzero `bonusForMiner` is rejected.
- Founder recipient is rejected after V35.
- Sender-based jackpot eligibility is impossible.

Required validation tests:

- Valid instant miner reward plus no jackpot payout remains valid after V35 when no jackpot is due.
- Valid instant miner reward plus one due jackpot payout is accepted by helper-level validation.
- Wrong jackpot winner is rejected.
- Immature jackpot period is rejected.
- Duplicate jackpot payout in the same block is rejected.
- Duplicate jackpot payout already present in canonical chain is rejected, if fixture access is available.
- Invalid or missing future hashes reject jackpot proof.
- Pre-V35 legacy reward validation remains unchanged while `V35_RESCUE_FORK_INDEX = Long.MAX_VALUE`.

Required balance tests:

- V35 instant miner reward credits miner.
- V35 jackpot payout credits deterministic winner.
- V35 founder reward is not credited.
- Invalid V35 `BASIS_ADDRESS` recipient is not credited.
- Rollback subtracts accepted jackpot payout consistently.

## 13. Risks

- Amount-based classification depends on fixed full-window payout amounts. If a later design supports partial periods, weighted reserve accounting, or missed-block accounting, amount inference may become ambiguous.
- `double` reward fields are already part of transaction format. Jackpot classification should use integer cents internally to avoid consensus divergence.
- `UtilsBlock.validationOneBlock(...)` may not have easy access to every historical block needed for duplicate scans and future-hash winner proofs. This may require careful use of `BlockService` or a small read-only helper.
- `UtilsBalance` and `UtilsBlock` must not implement separate classification logic. Divergence would create wallet/server balance or validation disagreement.
- Jackpot payouts can be up to six extra `BASIS_ADDRESS` transactions in a block. Existing BASIS count limits must be changed only inside the V35 branch.
- Mining still does not create payouts in Phase 4D-2. Validation must allow valid payouts without requiring them until Phase 4E defines payout timing.

## 14. Rollback Plan

If Phase 4D-2 causes validation or balance risk:

1. Revert only the V35 jackpot classification wiring in `UtilsBlock.java` and `UtilsBalance.java`.
2. Keep pure helper tests and models if they are correct and unused by consensus paths.
3. Restore the Phase 2B V35 validation behavior: exactly one `8.00 GOLD` `BASIS_ADDRESS -> minerAddress` transaction and no jackpot payout transactions.
4. Confirm `V35_RESCUE_FORK_INDEX` remains `Long.MAX_VALUE`.
5. Re-run wallet and server tests/builds.

## 15. Conclusion

Transaction format can remain unchanged for the first jackpot payout implementation if V35 uses strict amount fingerprinting plus deterministic period, winner, maturity, and duplicate validation. The six jackpot payout amounts are unique at two-decimal precision, but amount alone must never authorize a payout. Phase 4D-2 should centralize classification in V35 helper code and then wire `UtilsBlock` and `UtilsBalance` behind the existing disabled V35 fork gate.
