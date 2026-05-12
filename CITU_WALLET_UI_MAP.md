# CITU Wallet UI Map

Phase 0 audit only. No source code changes were made.

## Wallet UI Roots
- Templates: `C:\citu\unitedStates_final-master\src\main\resources\templates`
- Static assets: `C:\citu\unitedStates_final-master\src\main\resources\static`
- Controllers feeding templates: `C:\citu\unitedStates_final-master\src\main\java\International_Trade_Union\controllers`

## Templates to Update for V35
- `templates\home.html`: landing/account overview route `/`; must use CITU Gold/Silver language, fixed reward summaries, founder burn status, jackpot summary, wallet UX entry point.
- `templates\mining.html`: route `/mining`; highest priority for one-button mining, Eco/Normal/Boost modes, hide manual difficulty after V35, show fixed `10 GOLD/block`, show `8 GOLD instant + 2 GOLD jackpot reserve`, tickets and jackpot status.
- `templates\temp-mining.html`: mining progress/temporary mining view; must match one-button mining language and post-V35 difficulty behavior.
- `templates\seting.html`: route `/seting`; contains miner/pool/difficulty settings. Must hide or disable manual difficulty after V35 and keep only safe local mining mode controls.
- `templates\statistics.html`: route `/statistics`; must rename digital dollar/stock displays to Gold/Silver and expose jackpot/ticket/supply/burn information if available.
- `templates\transactions.html`: route `/transactions`; must rename fields, add transaction boost calculator UX, clarify bonus affects ticket weight/score only after V35.
- `templates\result-sending.html`: transaction result copy must use Gold/Silver language.
- `templates\create-account.html`: route `/create-account`; key/account UX may need Gold/Silver explanatory labels.
- `templates\board-of-shareholders.html`, `voting.html`, `current-laws.html`, `detail-laws.html`, `all-laws.html`, `create-law.html`, `create-position.html`, `governments.html`, `fractions.html`, `sanction.html`, `budget_end_emission.html`, `budget_end_emission_15_day.html`: governance pages contain digital dollar/stock wording and should be checked for CITU Gold/Silver display language.
- Shared partials: `templates\blocks\header.html`, `templates\blocks\prevHedear.html`, `templates\blocks\footer.html`; navigation must match V35 wallet story and add jackpot/burn/protocol status links if implemented.

## Static Assets to Review
- `static\lor\Gold Luxury Initial Circle Logo.png` and related lore images. Keep or replace based on final Gold/Silver branding.
- Existing screenshots and docs under `documentation`, `documentationEng`, `screenshots` reference old mining and transaction flows. They are not UI runtime files, but should be regenerated after V35.

## Controller Routes Driving Wallet UI
- `MainController`: `/`, `/seting`, `/setMinner`, `/setPool`, `/setPoolAddress`, `/setMinDollarRewards`, `/changeMultiThread`, transaction form POST `/`.
- `MineController`: `/mining`, `/customDiff`, `/staking`, `/unstaking`.
- `BasisController`: `/mine`, `/process-mining`, `/stopMining`, `/moreMining`, `/constantMining`, `/status`, `/testCalculate`, `/showAccounts`.
- `WebBasisController`: `/isUpdating`, `/isMining`, `/minerShow`.
- `StorageController`: `/globalSize`, `/globalVersion`, `/difficulty`.
- `ConductorController`: `/account`, `/dollar`, `/stock`, `/sendCoin`, `/blockHash`, `/conductorBlock`, `/senderTransactions`, `/customerTransactions`, `/addresses`, `/TopAddress`.

## V35 UI Requirements Mapped to Files
- CITU Gold: all labels currently saying `digitalDollar`, `digital dollar`, or dollar-like balances in templates and controllers.
- CITU Silver: all labels currently saying `digitalStockBalance`, `digital stock`, `reputation`, `stock`, `shares`. Display rule only: `raw digitalStockBalance * 100 = Silver`.
- One-button mining: `mining.html`, `temp-mining.html`, `MineController`, `BasisController` mining endpoints.
- Eco/Normal/Boost local modes: `mining.html`, `seting.html`, mining controller state. Must not change consensus difficulty; modes should affect local CPU/thread behavior only.
- Staking calculator: `mining.html`, `transactions.html`, `seting.html`; API needed from server for ticket/score preview.
- Transaction boost calculator: `transactions.html`; explain bonus affects ticket/score only after V35, not new GOLD printing.
- Jackpot tickets: `mining.html`, `statistics.html`, `home.html`; needs server endpoints for current ticket count/weight.
- Jackpot periods: `mining.html`, `statistics.html`, `home.html`; daily/weekly/monthly/quarterly/half-year/yearly display.
- Founder burn status: `home.html`, `statistics.html`, maybe `seting.html`; needs API.
- Fixed 10 GOLD/block: `mining.html`, `home.html`, `statistics.html`.
- Hide manual difficulty after V35: `mining.html`, `seting.html`, `MineController.customDiff`, `Mining.customDiff` behavior.

## Do Not Change in UI Phase Without Consensus Support
- Do not show V35 jackpot or burn values as final unless server endpoints/constants exist.
- Do not multiply Silver in server/account math; only display raw stock as `raw * 100` in UI.
