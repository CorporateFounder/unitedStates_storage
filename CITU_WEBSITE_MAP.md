# CITU Website Map

Phase 0 audit only. No source code changes were made.

## Website Roots
- Project: `C:\citu\website.citu-web-master`
- Templates: `src\main\resources\templates`
- Static assets: `src\main\resources\static`
- CSS: `src\main\resources\static\css\styles.css`
- Main controller: `src\main\java\com\example\website\citu\controller\WebController.java`

## Pages/Routes to Update for V35
- `templates\main.html`, route `/`: first-viewport story must become CITU V35 final fork, CITU Gold/Silver, one-button mining, jackpot mining.
- `templates\summary_and_benefits.html`, route `/summary_and_benefits`: update tokenomics, fixed 10 GOLD/block, founder reward removed, jackpot reserve.
- `templates\economic_model.html`: primary tokenomics page; must explain Gold/Silver display, 8+2 reward split, no extra GOLD from activity/staking/difficulty.
- `templates\how_to_mining.html`, route `/how_to_mining`, and `templates\mining-app-guide-win.html`: update to one-button mining, Eco/Normal/Boost local modes, no manual difficulty after V35, tickets/jackpots.
- `templates\wallet_and_node_url.html`, route `/wallet_and_node_url`: wallet download and node links must match final wallet UX.
- `templates\white_papper.html`, route `/white_papper`: needs final fork statement, V35 consensus rules, reward and burn policy.
- `templates\vision.html`, route `/vision`: align with final fork story and Gold/Silver branding.
- `templates\winners.html`, route `/winners`, and `templates\daily_rating.html`, route `/daily_rating`: replace old winner/rating wording with jackpot mining periods and deterministic tickets.
- `templates\how_to_transaction.html`, `transactionsSender.html`, `transactionsCustomer.html`, `detail-transactions.html`: rename digital dollar/stock to Gold/Silver and clarify transaction boost/ticket weight.
- `templates\how_to_open_an_account.html`: update wallet/account wording.
- `templates\how_to_change_miner_account.html`: review under one-button mining UX.
- `templates\how_to_install.html`, `how_to_install_server.html`: update installation docs for final wallet/server commands and V35 status endpoints.
- `templates\voting.html`, `how_to_vote_and_what_voting_types_are_there.html`, `how_to_make_laws.html`, `how_to_apply_for_a_job.html`, `charter.html`, `charter_eng.html`, `charter_rus.html`: update currency/stock naming where user-facing.
- `templates\conductor.html`: explorer/search page must show Gold/Silver and latest V35 chain score where applicable.
- Shared partials: `templates\blocks\header.html`, `templates\blocks\footer.html`: navigation labels, docs links, wallet download, FAQ/tokenomics links.

## Static Assets to Review/Replace
- Logos: `static\logo\Gold Luxury Initial Circle Logo.png`, `reward.jpg`, `output.png`, `output2.png`, `output3.png`.
- Mining screenshots: `static\mining\*.png`, `static\1734101366_mining-app-guide-win\*.png`.
- Server screenshots: `static\server\*.png`.
- Install screenshots: `static\install\*.png`.
- Existing CSS: `static\css\styles.css`.

## Website Controllers
- `WebController`: routes `/`, `/summary_and_benefits`, `/how_to_install`, `/mining-app-guide-win`, `/how_to_install_server`, `/how_to_open_an_account`, `/how_to_change_miner_account`, `/how_to_mining`, `POST /how_to_mining`, `/how_to_transaction`, `/how_to_apply_for_a_job`, `/how_to_make_laws`, `/how_to_vote_and_what_voting_types_are_there`, `/solving_common_problems`, `/wallet_and_node_url`, `/charter`, `charter_rus`, `charter_eng`, `/vision`, `white_papper`, `POST /calculate_reward`.
- `WinnersController`: `/winners`, `/daily_rating`.
- `ExplorerController`: `/conductor`, `POST /conductor`, `detail-transactions`, `/detail-transactions/{index}`, `detail-laws`, `/detail-laws/{hex}`.
- `TransactionSenderController`, `TransactionCustomerController`, `VotingController` provide explorer/help views.

## Missing Website Content Blocks
- V35 overview page/section.
- Gold/Silver branding statement with display-only Silver multiplier.
- Founder burn and founder reward removal statement.
- Fixed reward: exactly 10 GOLD per block.
- 8 GOLD instant miner reward plus 2 GOLD jackpot reserve.
- Jackpot periods: daily, weekly, monthly, quarterly, half-year, yearly.
- Every mined block is a jackpot ticket.
- Bitcoin-like target PoW after V35.
- Automatic difficulty and emergency difficulty drop.
- Staking/transaction boost affect tickets/score only, not extra GOLD printing.
- One-button wallet mining and local Eco/Normal/Boost explanation.
- FAQ and docs links tied to protocol constants/API.

## Website Risk Notes
- Website currently appears independent from server constants. Final copy should be generated from or verified against constants/API to avoid mismatched tokenomics.
- Java version is 19 while wallet/server are Java 17. This is acceptable for website but should be documented for deployment.
