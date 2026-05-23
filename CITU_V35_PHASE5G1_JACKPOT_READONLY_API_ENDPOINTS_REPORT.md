# CITU V35 Phase 5G-1 Jackpot Read-Only API Endpoints Report

Phase: 5G-1  
Project root: `C:\citu`  
Scope: server read-only V35 jackpot API endpoints. No protocol, mining, validation, balance, transaction format, block format, wallet, website, or `pom.xml` changes.

## 1. Exact Files Changed

Server source:

- `unitedStates_storage-master/src/main/java/International_Trade_Union/controllers/V35JackpotController.java`
- `unitedStates_storage-master/src/main/java/International_Trade_Union/protocol/v35/V35JackpotApiResponseBuilder.java`

Server tests:

- `unitedStates_storage-master/src/test/java/International_Trade_Union/controllers/V35JackpotControllerTest.java`

Report:

- `CITU_V35_PHASE5G1_JACKPOT_READONLY_API_ENDPOINTS_REPORT.md`

No wallet or website files were changed.

## 2. Endpoints Added

Added server read-only endpoints:

- `GET /v35/jackpot/status`
- `GET /v35/jackpot/periods`
- `GET /v35/jackpot/history`
- `GET /v35/jackpot/tickets/{address}`
- `GET /v35/jackpot/proof/{periodType}/{periodEnd}`

## 3. Response Fields

Shared jackpot metadata:

- `v35Active`
- `v35ForkIndex`
- `blockTimeSeconds`
- `blocksPerDay`
- `randomDelayBlocks`
- `firstPayoutBlockOffset`
- `burnAddress`
- `winnerSource`
- `senderEligibilityAllowed`
- `currentHeight`

`/v35/jackpot/status` adds:

- `jackpotReservePerBlockGold`
- `warning`
- `periods`
  - `periodType`
  - `windowBlocks`
  - `canonicalPayoutGold`
  - `currentPeriodStart`
  - `currentPeriodEnd`
  - `maturityBlock`
  - `mature`
  - `estimatedTickets`
  - `status`

`/v35/jackpot/periods` adds:

- `exactSlotOnly`
- `missedPayoutsPaidLater`
- `explanation`
- period definitions with:
  - `periodType`
  - `periodName`
  - `windowBlocks`
  - `canonicalPayoutGold`
  - `reservePerBlockGold`
  - `randomDelayBlocks`
  - `explanation`

`/v35/jackpot/history` adds:

- `status`
- `warning`
- `items`

`/v35/jackpot/tickets/{address}` adds:

- `address`
- `note`
- per-period ticket rows

`/v35/jackpot/proof/{periodType}/{periodEnd}` adds:

- `periodType`
- `periodStart`
- `periodEnd`
- `maturityBlock`
- `futureHashDelay`
- `seedHash`
- `selectedOffset`
- `selectedBlockIndex`
- `winnerMinerAddress`
- `payoutExpectedGold`
- `payoutPaid`
- `payoutBlockIndex`
- `payoutBlockHash`
- `payoutTxHash`
- `proofStatus`
- `warning`

## 4. Disabled V35 State

Confirmed constants remain:

- wallet `V35_RESCUE_FORK_INDEX = Long.MAX_VALUE`
- server `V35_RESCUE_FORK_INDEX = Long.MAX_VALUE`
- wallet `V35_BURN_ADDRESS = BURN_ADDRESS`
- server `V35_BURN_ADDRESS = BURN_ADDRESS`

While disabled:

- `v35Active = false`
- `v35ForkIndex = "Long.MAX_VALUE"`
- status rows report `DISABLED`
- history returns an empty list
- tickets return zero/empty rows
- proof returns `proofStatus = DISABLED`
- warning text is:
  - `V35 jackpot policy is configured but not active yet.`

## 5. Period / Payout Calculation

The API uses existing `JackpotCalculator` values:

| Period | Window blocks | Canonical payout |
|---|---:|---:|
| DAILY | 432 | 302.40 |
| WEEKLY | 3024 | 1209.60 |
| MONTHLY | 12960 | 3888.00 |
| QUARTERLY | 38880 | 9720.00 |
| HALF_YEAR | 78624 | 15724.80 |
| YEARLY | 157680 | 23652.00 |

The random delay is `60` blocks. The first payout block offset is `61`, matching the existing exact-slot rule:

`firstPayoutBlock = periodEnd + 60 + 1`

## 6. History / Proof / Tickets

The active-path builder derives jackpot data from canonical chain context.

History:

- scans canonical blocks after the V35 fork;
- finds `BASIS_ADDRESS` transactions with canonical jackpot amounts;
- verifies them through `V35JackpotProofService` before including them.

Proof:

- validates fork-relative period alignment;
- collects eligible tickets through `V35JackpotChainContextAdapter`;
- collects delayed future hashes;
- computes `JackpotWinnerProof`;
- reports safe unavailable/not mature/disabled states instead of throwing.

Tickets:

- counts only `JackpotTicket` entries derived from block `minerAddress`;
- never treats sender/customer transaction addresses as participants.

Because V35 is currently disabled, live history/proof/tickets are inactive and empty by default.

## 7. Behavior Change Confirmation

No protocol behavior changed.
No mining behavior changed.
No validation behavior changed.
No balance behavior changed.
No transaction or block format changed.
No jackpot payout transaction was created.
No burn transaction was created.
No wallet or website UI was changed.

The new controller is read-only and uses `BlockService` only for current height and canonical block reads.

## 8. Test / Build Results

Server:

- `.\mvnw.cmd test`: passed
  - Tests run: 184
  - Failures: 0
  - Errors: 0
  - Skipped: 0
- `.\mvnw.cmd clean package`: passed
  - Tests run during package: 184
  - Failures: 0
  - Errors: 0
  - Skipped: 0

Wallet:

- `.\mvnw.cmd test`: passed
  - Tests run: 169
  - Failures: 0
  - Errors: 0
  - Skipped: 0
- `.\mvnw.cmd clean package`: passed
  - Tests run during package: 169
  - Failures: 0
  - Errors: 0
  - Skipped: 0

Website:

- `.\mvnw.cmd test`: passed
  - Tests run: 4
  - Failures: 0
  - Errors: 0
  - Skipped: 0
- `.\mvnw.cmd clean package`: passed
  - Tests run during package: 4
  - Failures: 0
  - Errors: 0
  - Skipped: 0

Notes:

- Maven still reports pre-existing duplicate dependency warnings in server, wallet, and website POMs.
- Server test/package output still prints the pre-existing `FileNotFoundException` stack trace for `\server\resources\error\error.txt` during an expected burn-address negative-path test, but Maven reports success with zero failures/errors.

## 9. Performance Risks

- `/v35/jackpot/history` can become expensive on a large chain because it scans canonical blocks.
- `/v35/jackpot/tickets/{address}` can be expensive for long periods such as yearly.
- Proof building can require period blocks plus 60 future hashes.
- Future hardening should add pagination/range options and height/hash keyed caching if these endpoints become hot.

## 10. Remaining Risks Before Wallet/Site Jackpot Integration

1. Full active-V35 API behavior still relies on simulated fork testing because V35 is disabled.
2. History has no pagination yet in this first endpoint implementation.
3. The proof endpoint does not expose all 60 seed hashes yet, only the deterministic seed.
4. Wallet/site still need a later integration phase to consume these jackpot endpoints.

## 11. Recommended Next Phase

Phase 5G-2: wallet/dashboard jackpot live API integration or pre-activation hardening plan.
