# CITU V35 Phase 5F-2 Website V35 Public Pages Report

Phase: 5F-2 only  
Scope: public website read-only V35 overview/tokenomics/burn/jackpot/mining/FAQ pages  
V35 activation: still disabled  
Burn transaction: not created  
Protocol/mining/validation/balance changes: none  
Transaction/block format changes: none

## 1. Exact Files Changed

Website source:

- `website.citu-web-master/src/main/java/com/example/website/citu/controller/V35PublicController.java`

Website templates:

- `website.citu-web-master/src/main/resources/templates/v35.html`
- `website.citu-web-master/src/main/resources/templates/v35-tokenomics.html`
- `website.citu-web-master/src/main/resources/templates/v35-burn.html`
- `website.citu-web-master/src/main/resources/templates/v35-jackpot.html`
- `website.citu-web-master/src/main/resources/templates/v35-mining.html`
- `website.citu-web-master/src/main/resources/templates/v35-faq.html`
- `website.citu-web-master/src/main/resources/templates/blocks/header.html`

Website tests:

- `website.citu-web-master/src/test/java/com/example/website/citu/controller/V35PublicControllerTest.java`

Report:

- `CITU_V35_PHASE5F2_WEBSITE_V35_PUBLIC_PAGES_REPORT.md`

No wallet source files were changed. No server protocol source files were changed.

## 2. Routes / Pages Added

Added public website routes:

- `GET /v35` -> `v35.html`
- `GET /v35/tokenomics` -> `v35-tokenomics.html`
- `GET /v35/burn` -> `v35-burn.html`
- `GET /v35/jackpot` -> `v35-jackpot.html`
- `GET /v35/mining` -> `v35-mining.html`
- `GET /v35/faq` -> `v35-faq.html`

Updated website header navigation with links to:

- `/v35`
- `/v35/tokenomics`
- `/v35/burn`
- `/v35/jackpot`

## 3. Content Shown On Each Page

`/v35` overview:

- V35 disabled warning.
- Fork index shown as `Long.MAX_VALUE`.
- Founder reward removed.
- Reward model: `8 GOLD/block = 6 GOLD miner + 2 GOLD jackpot + 0 founder`.
- `200 sec/block`.
- `432 blocks/day`.
- Bitcoin-like PoW: `hash <= target`.
- cumulativeWork chain selection.
- burn address: `BURN_ADDRESS`.
- Links to the related public V35 pages.

`/v35/tokenomics`:

- actual vs modeled supply warning.
- burn is not actual until transaction happens.
- emission table:
  - `200 sec/block`
  - `432 blocks/day`
  - `8 GOLD/block`
  - `6 GOLD`
  - `2 GOLD`
  - `0 GOLD`
  - `1,261,440 GOLD/year`
- supply field definitions for total minted, burned, circulating, founder reserve, and jackpot paid.
- clear message that live supply data is unavailable on this static public page until read-only API integration is added.

`/v35/burn`:

- `BURN_ADDRESS` is a protocol sink.
- sending TO `BURN_ADDRESS` is allowed.
- spending FROM `BURN_ADDRESS` is blocked after V35 by consensus policy.
- founder burn is planned manually before V35 activation.
- recommended founder burn is `80%`.
- public proof checklist:
  - founder address
  - burn address
  - snapshot height/hash
  - burn amount
  - burn tx hash
  - burn block index/hash
  - after-burn founder balance
  - burned/circulating supply

`/v35/jackpot`:

- every V35 mined block is a jackpot ticket.
- winner is `minerAddress` of selected eligible block.
- sender addresses are never jackpot participants.
- randomness uses deterministic delayed future block hashes.
- no manual winner selection.
- no live winners are shown because no read-only jackpot winner endpoint exists yet.
- payout table:
  - daily: `302.40 GOLD`
  - weekly: `1209.60 GOLD`
  - monthly: `3888.00 GOLD`
  - quarterly: `9720.00 GOLD`
  - half-year: `15724.80 GOLD`
  - yearly: `23652.00 GOLD`

`/v35/mining`:

- V35 disabled warning.
- manual difficulty is legacy-only after V35.
- one block reward is `6 GOLD instant + jackpot ticket`.
- no founder reward.
- Bitcoin-like target difficulty: `hash <= target`.
- emergency difficulty easing.
- cumulativeWork decides stronger chain after V35.
- `200 sec/block` and `432 blocks/day`.

`/v35/faq`:

- Is V35 active?
- Has the founder burn happened?
- What is `BURN_ADDRESS`?
- Can `BURN_ADDRESS` spend?
- What is actual vs modeled supply?
- How does jackpot choose winners?
- Can senders win jackpot?
- Why `8 GOLD/block`?
- Why `432 blocks/day`?
- What happens to old blocks?

## 4. Actual Vs Modeled Supply Handling

The public pages keep actual and modeled values separate:

- Actual burned supply is described as based on `BURN_ADDRESS` balance.
- Modeled founder burn is explicitly labeled as a projection.
- The tokenomics page states that burn is not actual until the transaction happens.
- The burn page states that the founder burn has not happened until a burn transaction hash and burn block are published.
- No modeled burn value is presented as current burned supply.
- No live server API values are fetched or displayed in this phase.

## 5. V35 Disabled State

Each V35 public page states:

```text
V35 is not active while V35_RESCUE_FORK_INDEX is Long.MAX_VALUE.
```

Confirmed in wallet and server source:

- `V35_RESCUE_FORK_INDEX = Long.MAX_VALUE`
- `V35_BURN_ADDRESS = BURN_ADDRESS`

No activation setting was changed.

## 6. BURN_ADDRESS Explanation

The public pages explain:

- `BURN_ADDRESS` is configured.
- `BURN_ADDRESS` is a protocol sink.
- sending TO `BURN_ADDRESS` is allowed.
- spending FROM `BURN_ADDRESS` is blocked after V35 by consensus policy.
- manual founder burn is a later operational step.

## 7. Confirmations

Confirmed:

- V35 remains disabled.
- `V35_RESCUE_FORK_INDEX` was not changed.
- `V35_BURN_ADDRESS` remains `BURN_ADDRESS`.
- No burn transaction was created.
- No protocol behavior changed.
- No mining behavior changed.
- No validation behavior changed.
- No balance behavior changed.
- No transaction format changed.
- No block format changed.
- `Mining.java` was not changed.
- `UtilsBlock.java` was not changed.
- `UtilsBalance.java` was not changed.
- `Blockchain.java` was not changed.
- Wallet project source was not edited in this phase.
- Server protocol logic was not edited in this phase.
- `pom.xml` was not edited.

## 8. Tests Added

Added website test:

- `V35PublicControllerTest`

Coverage:

- all six routes return expected templates.
- model exposes static V35 disabled state and canonical V35 values.
- templates contain disabled-state wording.
- templates contain `BURN_ADDRESS`.
- templates contain reward model `8/6/2/0`.
- templates contain `200 sec/block`, `432 blocks/day`, and `1,261,440 GOLD/year`.
- templates contain actual-vs-modeled wording.
- templates state the founder burn has not happened.
- jackpot payout amounts appear.
- sender addresses are not jackpot participants.
- no live winners are claimed.
- header links to public V35 pages.

## 9. Verification

Website:

- `.\mvnw.cmd test` passed: `4` tests, `0` failures, `0` errors, `0` skipped.
- `.\mvnw.cmd clean package` passed: `4` tests, `0` failures, `0` errors, `0` skipped.

Wallet:

- `.\mvnw.cmd test` passed: `164` tests, `0` failures, `0` errors, `0` skipped.
- `.\mvnw.cmd clean package` passed: `164` tests, `0` failures, `0` errors, `0` skipped.

Server:

- `.\mvnw.cmd test` passed: `180` tests, `0` failures, `0` errors, `0` skipped.
- `.\mvnw.cmd clean package` passed: `180` tests, `0` failures, `0` errors, `0` skipped.

Notes:

- Maven still reports existing duplicate dependency warnings in website, wallet, and server `pom.xml` files.
- Server tests still print the existing caught `\server\resources\error\error.txt` file-save stack trace during burn-address validation tests, but Maven reports test and package success.

## 10. Remaining Risks Before Live API / Dashboard Integration

1. Public website pages are static informational pages; they do not yet consume live `/v35/*` server API data.
2. Actual supply, founder snapshot, and burn proof values still require live API/dashboard integration.
3. No browser screenshot/mobile layout verification was performed in this phase.
4. No read-only jackpot winner/history endpoint exists yet, so live winners are intentionally not shown.
5. Existing legacy pages such as old mining/economic pages may still contain old reward descriptions and should be reconciled in later documentation phases.
6. Public copy must continue separating actual chain values from modeled burn projections.

## 11. Recommended Next Phase

Phase 5F-3 — wallet/dashboard live API integration or jackpot read-only API planning.
