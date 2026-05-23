# CITU V35 Phase 5F-0 Wallet / Website Dashboard Plan

Phase: 5F-0 only  
Scope: planning/report only  
Source changes: none  
V35 activation: still disabled  
Burn transaction: not created  
API changes: none

## 1. Exact Files / Templates / Controllers Inspected

Reports read:

- all `CITU_V35_PHASE*.md` reports were inventoried
- `CITU_V35_PHASE5C2C_SUPPLY_API_ENDPOINTS_REPORT.md`
- `CITU_V35_PHASE5E1_FOUNDER_BURN_SNAPSHOT_REPORT.md`
- `CITU_V35_PHASE5D1_SENTINEL_BURN_ADDRESS_REPLACEMENT_REPORT.md`
- `CITU_V35_PHASE4E1G_JACKPOT_CONTROLLER_STORAGE_INTEGRATION_REPORT.md`

Wallet source inspected:

- `unitedStates_final-master/src/main/java/International_Trade_Union/controllers/MainController.java`
- `unitedStates_final-master/src/main/java/International_Trade_Union/controllers/MineController.java`
- `unitedStates_final-master/src/main/java/International_Trade_Union/controllers/BasisController.java`
- `unitedStates_final-master/src/main/java/International_Trade_Union/controllers/StatisticsController.java`
- `unitedStates_final-master/src/main/java/International_Trade_Union/controllers/TransactionsController.java`
- `unitedStates_final-master/src/main/java/International_Trade_Union/controllers/CreateAccountController.java`
- `unitedStates_final-master/src/main/java/International_Trade_Union/controllers/ServerController.java`
- `unitedStates_final-master/src/main/java/International_Trade_Union/setings/Seting.java`

Wallet templates inspected:

- `unitedStates_final-master/src/main/resources/templates/home.html`
- `unitedStates_final-master/src/main/resources/templates/mining.html`
- `unitedStates_final-master/src/main/resources/templates/statistics.html`
- `unitedStates_final-master/src/main/resources/templates/transactions.html`
- `unitedStates_final-master/src/main/resources/templates/seting.html`
- `unitedStates_final-master/src/main/resources/templates/create-account.html`
- `unitedStates_final-master/src/main/resources/templates/blocks/header.html`

Server source inspected:

- `unitedStates_storage-master/src/main/java/International_Trade_Union/controllers/V35SupplyController.java`
- `unitedStates_storage-master/src/main/java/International_Trade_Union/protocol/v35/V35SupplyApiResponseBuilder.java`
- `unitedStates_storage-master/src/main/java/International_Trade_Union/setings/Seting.java`

Website source inspected:

- `website.citu-web-master/src/main/java/com/example/website/citu/controller/WebController.java`
- `website.citu-web-master/src/main/java/com/example/website/citu/controller/ExplorerController.java`
- `website.citu-web-master/src/main/java/com/example/website/citu/controller/WinnersController.java`
- `website.citu-web-master/src/main/java/com/example/website/citu/controller/TransactionSenderController.java`
- `website.citu-web-master/src/main/java/com/example/website/citu/controller/TransactionCustomerController.java`
- `website.citu-web-master/src/main/java/com/example/website/citu/utils/UtilUrl.java`

Website templates/static inspected:

- `website.citu-web-master/src/main/resources/templates/main.html`
- `website.citu-web-master/src/main/resources/templates/economic_model.html`
- `website.citu-web-master/src/main/resources/templates/how_to_mining.html`
- `website.citu-web-master/src/main/resources/templates/winners.html`
- `website.citu-web-master/src/main/resources/templates/white_papper.html`
- `website.citu-web-master/src/main/resources/templates/wallet_and_node_url.html`
- `website.citu-web-master/src/main/resources/templates/blocks/header.html`
- `website.citu-web-master/src/main/resources/static/css/styles.css`

## 2. V35 Status Confirmation

Confirmed wallet and server:

```text
V35_RESCUE_FORK_INDEX = Long.MAX_VALUE
V35_BURN_ADDRESS = BURN_ADDRESS
```

V35 remains disabled. The final sentinel burn address is configured. No burn transaction exists yet.

## 3. Current Wallet UI Structure

The wallet is a Spring MVC / Thymeleaf application with Bootstrap templates.

Important current pages:

- `/` via `MainController.home(...)` -> `home.html`
  - shows miner account balance, local/global chain size, local/global version, validation status, current difficulty, chain complexity, send-money form, and update-blockchain button.
- `/mining` via `MineController.miming(...)` -> `mining.html`
  - shows start mining button, current custom difficulty, custom difficulty form, staking form, and unstaking form.
- `/processUpdating` via `BasisController.processUpdating(...)` -> `processUpdating.html`
  - shows update/mining state and miner balance while chain is updating or mining.
- `/statistics` via `StatisticsController.getStatistics(...)` -> `statistics.html`
  - period statistics for chain/account/transaction aggregates.
- `/transactions` via `TransactionsController.transactions(...)` -> `transactions.html`
  - block-range transaction history.
- `/seting` via `MainController.seting(...)` -> `seting.html`
  - server, pool host, miner, pool address, and settings controls.
- `/create-account` via `CreateAccountController` -> `create-account.html`
  - account creation and key display.

Header/navigation is centralized in:

- `unitedStates_final-master/src/main/resources/templates/blocks/header.html`

Current wallet UI is operational and form-heavy. It has no V35 dashboard page, no supply panel, no jackpot panel, and no burn snapshot panel yet.

## 4. Current Website Structure

The website is a separate Spring MVC / Thymeleaf project.

Important current pages:

- `/` via `WebController.mainPage(...)` -> `main.html`
  - public homepage with chain stats, exchange links, social links, wallet/server GitHub links, and summary text.
- `/how_to_mining` via `WebController.howToMining(...)` -> `how_to_mining.html`
  - mining documentation and old mining reward explanation.
- `/winners` via `WinnersController.winners(...)` -> `winners.html`
  - historical winner display based on legacy winner/random data.
- `/daily_rating` via `WinnersController.raitings(...)` -> `daily_rating.html`
  - daily rating view.
- `/economic_model` template exists but currently only contains a short heading.
- `/white_papper` via `WebController.white_papper(...)` -> `white_papper.html`
- `/wallet_and_node_url` via `WebController.wallet_and_node_url(...)` -> `wallet_and_node_url.html`
- `/conductor` via `ExplorerController` -> `conductor.html`

Website header is:

- `website.citu-web-master/src/main/resources/templates/blocks/header.html`

The website already calls server endpoints through `UtilUrl` and a configured server address, so it can consume new read-only V35 endpoints later without changing server API.

## 5. Recommended Wallet Dashboard Design

Add a new read-only wallet page first:

```text
/v35-dashboard
```

Recommended controller:

```text
V35DashboardController
```

Recommended template:

```text
v35-dashboard.html
```

Recommended wallet panels:

1. Protocol Status
   - V35 active/inactive.
   - Fork index.
   - Current local height.
   - Warning when fork index is `Long.MAX_VALUE`.
   - Block time: 200 seconds.
   - Blocks/day: 432.

2. Reward Model
   - Total reward: 8.00 GOLD/block.
   - Miner reward: 6.00 GOLD/block.
   - Jackpot reserve: 2.00 GOLD/block.
   - Founder reward: 0.00 GOLD.
   - Founder reward removed after V35.

3. Supply / Burn
   - Total minted.
   - Actual burned.
   - Actual circulating.
   - Founder reserve.
   - Jackpot paid.
   - Annual emission: 1,261,440.00 GOLD.
   - Burn address: `BURN_ADDRESS`.

4. Founder Burn Snapshot
   - Snapshot height.
   - Snapshot block hash.
   - Founder balance.
   - 75/80/85/90 burn options.
   - Recommended 80%.
   - Proof checklist.
   - Clear `burnTransactionCreated = false` until the manual burn is confirmed.

5. Jackpot Status
   - Daily, weekly, monthly, quarterly, half-year, yearly periods.
   - Estimated pool values from configured per-block reserve and current period progress.
   - Maturity/countdown based on fixed windows.
   - Later: last winners and deterministic proof once endpoint support exists.

6. Mining Status / UX
   - Current V35 reward model.
   - “Mine one block / start mining” UX can remain a future phase.
   - Manual/custom difficulty should be hidden after V35 or labeled `legacy-only`.
   - Before V35, current mining controls should remain visible and unchanged.

Recommended wallet integration source:

- First use local helper data and constants where possible.
- For canonical supply/founder snapshot, consume the server read-only endpoints if wallet has a configured server URL.
- If endpoint unavailable, show a clear unavailable state and keep the page read-only.

## 6. Recommended Website / Dashboard Design

Add public website pages after wallet read-only dashboard is stable:

- `/v35`
- `/v35/tokenomics`
- `/v35/burn`
- `/v35/jackpot`
- `/v35/mining`
- `/v35/faq`

Recommended content:

1. V35 Overview
   - V35 is not active until fork index is set.
   - Bitcoin-like PoW rule: `hash <= target`.
   - cumulativeWork chain selection after V35.
   - no founder reward after V35.

2. Tokenomics
   - 200 seconds/block.
   - 432 blocks/day.
   - 8 GOLD/block total.
   - 6 GOLD miner reward.
   - 2 GOLD jackpot reserve.
   - 0 founder reward.
   - annual emission 1,261,440 GOLD/year.

3. Burn Proof
   - Burn address: `BURN_ADDRESS`.
   - Actual burned supply from `/v35/burn`.
   - Founder snapshot from `/v35/founder/burn-snapshot`.
   - 80% founder burn planned.
   - Manual burn will happen before V35 activation.
   - Actual burn transaction hash/block fields remain pending until broadcast/confirmation.

4. Jackpot
   - Every mined V35 block is a ticket.
   - Sender addresses are not eligible.
   - Winner is miner address of selected block.
   - Delayed randomness uses future block hashes.
   - Fixed periods: daily, weekly, monthly, quarterly, half-year, yearly.
   - Jackpot reserve split totals 2.00 GOLD/block.

5. Mining
   - One-button mining goal for V35 wallet.
   - Manual difficulty/custom diff is legacy-only after V35.
   - PoW target/cumulativeWork should be explained plainly.

6. FAQ
   - Is V35 active? No, not while fork index is `Long.MAX_VALUE`.
   - Has the burn happened? No until burn transaction hash is published.
   - Is modeled burn actual supply? No.
   - Can `BURN_ADDRESS` spend? After V35, spending from it is blocked by consensus policy.

## 7. API Endpoints To Consume

Existing server read-only endpoints:

- `GET /v35/protocol`
- `GET /v35/supply`
- `GET /v35/burn`
- `GET /v35/founder`
- `GET /v35/founder/burn-snapshot`

Recommended future endpoints, if needed:

- `GET /v35/jackpot/status`
- `GET /v35/jackpot/history`
- `GET /v35/jackpot/period/{type}`
- `GET /v35/jackpot/tickets/{address}`
- `GET /v35/jackpot/winner/{period}`

Do not add new endpoints in Phase 5F-1 unless the dashboard cannot be built safely from existing data.

## 8. Safety Wording Required

Wallet and website must use consistent wording:

- `V35 is not active while V35_RESCUE_FORK_INDEX is Long.MAX_VALUE.`
- `Modeled founder burn is not actual burned supply.`
- `The founder burn transaction has not happened until a burn transaction hash and burn block are published.`
- `Actual burned supply is based on the BURN_ADDRESS balance.`
- `BURN_ADDRESS is a protocol sink. Sending to it is allowed; spending from it is blocked after V35.`
- `Founder reward after V35 is 0.00 GOLD.`
- `Jackpot reserve is not minted until a valid jackpot payout transaction exists.`
- `Sender addresses are never jackpot participants. Jackpot eligibility is miner-address based.`

Avoid wording that implies:

- V35 is already active.
- the burn already happened.
- modeled post-burn supply is current supply.
- jackpot reserve is already minted before payout.
- sender transaction volume creates jackpot eligibility.

## 9. Actual Vs Modeled Supply Display Rules

Display actual and modeled values in separate sections.

Actual values:

- `totalMintedGold`
- `burnedGold`
- `circulatingGold`
- `founderReserveGold`
- `jackpotPaidGold`
- `annualEmissionGold`

Modeled values:

- 75/80/85/90 burn options.
- modeled burned after burn.
- modeled circulating after burn.
- modeled inflation after burn.

Rules:

- Actual burned supply must come from burn address balance.
- Modeled burn must be labeled as a projection.
- Burn snapshot must be anchored to `snapshotHeight` and `snapshotBlockHash`.
- If snapshot hash is `UNKNOWN`, show a warning and do not treat the snapshot as final public proof.

## 10. Jackpot Display Rules

Initial wallet/site jackpot display can be policy/status only:

- period names and fixed windows,
- per-block reserve split,
- estimated current pools,
- maturity/countdown,
- V35 disabled warning.

Do not show last winners or proof details until a read-only jackpot status/history API is available.

Display period windows using 432 blocks/day:

- daily: 432
- weekly: 3024
- monthly: 12960
- quarterly: 38880
- half-year: 78624
- yearly: 157680

Display payout amounts:

- daily: 302.40
- weekly: 1209.60
- monthly: 3888.00
- quarterly: 9720.00
- half-year: 15724.80
- yearly: 23652.00

## 11. Burn / Founder Snapshot Display Rules

Wallet and website should display:

- founder address,
- burn address,
- configured/not-placeholder status,
- snapshot height/hash,
- founder balance at snapshot,
- recommended 80% burn,
- 75/80/85/90 options,
- proof checklist,
- pending burn transaction fields.

The proof checklist should remain visibly incomplete until:

- burn transaction hash is known,
- burn block index is known,
- burn block hash is known,
- founder post-burn balance is confirmed,
- burned/circulating supply after burn is confirmed.

## 12. API Usage Strategy

Wallet:

- Phase 5F-1 should prefer a local read-only wallet page that can display constants and fetch server V35 endpoints using the existing configured server address.
- If the server endpoint fails, show unavailable fields rather than blocking wallet use.
- Do not mutate wallet state from dashboard loads.
- Do not create or broadcast burn transactions from the dashboard.

Website:

- Use existing `UtilUrl` server-fetch pattern.
- Cache or memoize responses later if full-chain supply scans are expensive.
- Show endpoint unavailable status if server is down.
- Keep public website pages informational/read-only.

## 13. Safe Implementation Order

Recommended phases:

1. Phase 5F-1: wallet V35 dashboard read-only page
   - add wallet controller/template for `/v35-dashboard`
   - display protocol/supply/burn/founder snapshot
   - consume existing server endpoints or local constants
   - no mining behavior changes

2. Phase 5F-2: website V35 tokenomics/burn/jackpot pages
   - add public pages and navigation
   - consume existing server endpoints read-only
   - no API changes unless absolutely needed

3. Phase 5F-3: wallet mining UX update
   - clearly mark custom difficulty as legacy-only after V35
   - plan one-button mining UI
   - keep current legacy mining behavior while V35 disabled

4. Phase 5F-4: API/dashboard integration tests
   - controller/template smoke tests
   - endpoint unavailable handling
   - actual vs modeled value assertions

5. Phase 5F-5: public docs/FAQ
   - unify wording across wallet/site
   - publish burn proof instructions
   - add jackpot and cumulativeWork explanations

## 14. Files Phase 5F-1 May Edit

Wallet-only first phase:

- `unitedStates_final-master/src/main/java/International_Trade_Union/controllers/V35DashboardController.java`
- `unitedStates_final-master/src/main/resources/templates/v35-dashboard.html`
- `unitedStates_final-master/src/main/resources/templates/blocks/header.html`
- wallet read-only DTO/helper classes if endpoint consumption needs them
- wallet tests for controller/template/helper behavior
- `CITU_V35_PHASE5F1_...REPORT.md`

Only if needed:

- a small wallet read-only V35 API client/helper
- local constants display helper under `protocol/v35`

## 15. Files Phase 5F-1 Must Not Edit

- `Mining.java`
- `UtilsBlock.java`
- `UtilsBalance.java`
- `Blockchain.java`
- `DtoTransaction.java`
- `Block.java`
- server V35 API behavior
- website project files
- transaction/block format classes
- `pom.xml`

## 16. Required Tests

Phase 5F-1 wallet tests:

- `/v35-dashboard` renders while V35 disabled.
- dashboard shows `V35_RESCUE_FORK_INDEX = Long.MAX_VALUE`.
- dashboard shows `BURN_ADDRESS`.
- dashboard shows reward model: 8/6/2/0.
- dashboard separates actual and modeled supply sections.
- dashboard shows burn transaction pending when no burn hash exists.
- dashboard handles unavailable server V35 endpoints gracefully.
- dashboard does not mutate chain, balances, settings, transactions, or mining state.
- header contains a V35 dashboard link if navigation is updated.

Phase 5F-2 website tests:

- public V35 pages render.
- tokenomics page shows 432 blocks/day and 8/6/2/0 reward model.
- burn page labels actual versus modeled supply.
- jackpot page shows period windows and payout amounts.
- endpoint-unavailable state renders without exception.

Integration/browser checks:

- desktop/mobile layout does not overlap.
- long addresses like `BURN_ADDRESS` and founder address wrap safely.
- no page says V35 is active while fork index is `Long.MAX_VALUE`.

## 17. Highest Risks

1. UI wording could accidentally imply the burn or V35 activation already happened.
2. Actual and modeled supply values could be visually mixed, causing public confusion.
3. Public website may call expensive full-chain supply endpoints too often.
4. Wallet dashboard could fail hard if server V35 endpoints are unavailable unless fallback is designed.
5. Existing mining and website docs still describe legacy random/stake/founder reward behavior and need careful V35 labeling.
6. Jackpot winner/history display needs future API support; showing guessed winners would be unsafe.
7. External users may misunderstand `BURN_ADDRESS` as a normal wallet address unless the protocol-sink warning is prominent.

## 18. Rollback Plan

Because Phase 5F-1 should be read-only UI:

- remove or hide the `/v35-dashboard` navigation link,
- keep backend V35 endpoints unchanged,
- keep V35 disabled,
- do not touch mining or validation code,
- revert only dashboard controller/template/helper files if wording or layout is wrong.

Website rollback:

- remove V35 public navigation links,
- keep static pages unpublished or behind a draft route,
- keep API endpoints available for internal verification.

## 19. Final Recommendation

Proceed with Phase 5F-1 as a wallet-only read-only V35 dashboard.

Do not change mining UX, website pages, server APIs, or production protocol behavior in Phase 5F-1. The wallet dashboard should first prove the information architecture: protocol status, supply, burn, founder snapshot, jackpot policy, and clear disabled-state warnings.
