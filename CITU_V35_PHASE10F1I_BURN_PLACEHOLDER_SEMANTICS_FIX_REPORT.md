# CITU V35 Phase 10F-1I Burn Placeholder Semantics Fix Report

## 1. Executive summary

Phase 10F-1I fixed misleading read-only V35 burn-address reporting without changing `Seting.java`, activating V35, or creating any transaction.

The internal sentinel `BURN_ADDRESS` is now treated as an unpublished placeholder. Read-only V35 responses report:

- `burnAddress = "BURN_ADDRESS"`
- `isBurnAddressPlaceholder = true`
- `burnAddressConfigured = false`
- `publicBurnAddressPublished = false`

## 2. Problem found in manual smoke

The local server smoke previously returned:

- `burnAddress = "BURN_ADDRESS"`
- `isBurnAddressPlaceholder = false`
- `burnAddressConfigured = true`

Root cause: `BurnAddressPolicy` recognized only `TODO_V35_BURN_ADDRESS` as a placeholder. The configured internal/default sentinel is `BURN_ADDRESS`, so the read-only API and supply snapshot treated that sentinel as a configured public burn address.

## 3. Files changed

Production read-only V35 code:

- `unitedStates_storage-master/src/main/java/International_Trade_Union/protocol/v35/BurnAddressPolicy.java`
- `unitedStates_storage-master/src/main/java/International_Trade_Union/protocol/v35/V35SupplyCalculator.java`
- `unitedStates_storage-master/src/main/java/International_Trade_Union/protocol/v35/V35SupplyApiResponseBuilder.java`
- `unitedStates_storage-master/src/main/java/International_Trade_Union/protocol/v35/V35JackpotApiResponseBuilder.java`

Focused tests:

- `unitedStates_storage-master/src/test/java/International_Trade_Union/protocol/v35/BurnAddressPolicyTest.java`
- `unitedStates_storage-master/src/test/java/International_Trade_Union/protocol/v35/V35SupplyCalculatorTest.java`
- `unitedStates_storage-master/src/test/java/International_Trade_Union/protocol/v35/V35SupplyChainAdapterTest.java`
- `unitedStates_storage-master/src/test/java/International_Trade_Union/controllers/V35SupplyControllerTest.java`
- `unitedStates_storage-master/src/test/java/International_Trade_Union/controllers/V35JackpotControllerTest.java`

No `Seting.java` file changed.

## 4. Exact response-field semantics before and after

| Field | Before | After |
| --- | --- | --- |
| `burnAddress` | `BURN_ADDRESS` | `BURN_ADDRESS` |
| `isBurnAddressPlaceholder` | `false` | `true` |
| `burnAddressConfigured` | `true` | `false` |
| `publicBurnAddressPublished` | absent | `false` |
| warning | absent | official public burn address is not published yet; sentinel is not a public transfer destination |

The supply calculator now also excludes a sentinel-address balance from actual burned supply. A future non-placeholder approved address remains treated as configured.

## 5. Tests added or updated

Focused tests now prove:

- `BURN_ADDRESS` is an internal placeholder.
- The placeholder is not reported as configured or publicly published.
- Protocol, supply, burn, founder snapshot, and jackpot status read-only responses preserve safe semantics.
- A sentinel-address balance is not counted as actual burned supply.
- A real non-placeholder address remains supported by the calculator fixtures.
- V35 remains inactive while the fork index is `Long.MAX_VALUE`.
- No burn transaction or jackpot payout is created by read-only builders.

## 6. Server test and package result

Focused verification:

- Command: `.\mvnw.cmd '-Dtest=International_Trade_Union.controllers.V35SupplyControllerTest,International_Trade_Union.controllers.V35JackpotControllerTest,International_Trade_Union.protocol.v35.BurnAddressPolicyTest,International_Trade_Union.protocol.v35.V35SupplyCalculatorTest,International_Trade_Union.protocol.v35.V35SupplyChainAdapterTest' test`
- Result: `BUILD SUCCESS`
- Tests: `33`, failures: `0`, errors: `0`, skipped: `0`

Full server verification:

- Command: `.\mvnw.cmd test`
- Result: `BUILD SUCCESS`
- Tests: `231`, failures: `0`, errors: `0`, skipped: `0`

Clean package:

- Command: `.\mvnw.cmd clean package`
- Result: `BUILD SUCCESS`
- Tests: `231`, failures: `0`, errors: `0`, skipped: `0`

## 7. Optional local smoke result

The packaged server was started locally with `--spring.profiles.active=smoke-offline` on `127.0.0.1:8083`, queried, and stopped.

`GET /v35/protocol` returned:

- `v35Active = false`
- `v35ForkIndex = "Long.MAX_VALUE"`
- `burnAddress = "BURN_ADDRESS"`
- `isBurnAddressPlaceholder = true`
- `burnAddressConfigured = false`
- `publicBurnAddressPublished = false`

`GET /v35/burn` returned the same burn-address semantics plus:

- `burnTransactionCreated = false`
- `burnedGold = "0.00"`

No local smoke server process remained running after verification.

## 8. Constants audit

Server:

- `V35_RESCUE_FORK_INDEX = Long.MAX_VALUE`
- `V35_BURN_ADDRESS = "BURN_ADDRESS"`

Wallet:

- `V35_RESCUE_FORK_INDEX = Long.MAX_VALUE`
- `V35_BURN_ADDRESS = "BURN_ADDRESS"`

## 9. Seting.java confirmation

No server or wallet `Seting.java` file changed. No activation diff exists.

## 10. V35 disabled confirmation

V35 remains disabled. The planning candidate `514291` was not written to code.

## 11. No action confirmation

No activation, burn transaction, jackpot payout transaction, deployment, tag, release artifact, or checksum action was performed.

## 12. Remaining risks

- `publicBurnAddressPublished` is derived from whether the configured address is still a placeholder. Final burn-address publication must still be reviewed alongside final operator notices.
- Maven continues to report pre-existing duplicate dependency declarations in `pom.xml`; this phase did not modify build configuration.
- A pre-existing untracked Phase 10F-2 report remained in the working tree and was not modified.

## 13. Recommended next phase

Proceed to **Phase 10F-2B — final local sign-off warning closure report**, report only.
