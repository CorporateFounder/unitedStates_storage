# CITU Server API Map

Phase 0 audit only. No source code changes were made.

## Existing Server API Endpoints
Source root: `C:\citu\unitedStates_storage-master\src\main\java\International_Trade_Union\controllers`.

### Chain/Node/Storage Endpoints
- `BasisController`: `GET /winnerList`, `GET /allwinners`, `GET /powerWiners`, `GET /countTransactionsWiner`, `GET /stakingWiners`, `GET /bigRandomWiner`, `GET /datashort`, `GET /status`, `GET /v28Index`, `GET /allAccounts`, `GET /totalDollars`, `GET /totalStaking`, `GET /totalAll`, `GET /totalTransactionsDay`, `GET /totalTransactionsSum`, `GET /multiplier`, `GET /dayReduce`, `GET /getNodes`, `GET /size`, `POST /sub-blocks`, `GET /version`, `POST /block`, `GET /isSaveFile`, `GET /balance`, `GET /prevBlock`, `POST /nodes/resolve_from_to_block`, `POST /nodes/register`, `GET /findAddresses`, `GET /difficultyBlockchain`, `GET /timentp`.
- `NodeController`: `GET /confirmReadiness`.
- `NodesController`: `POST /putNode`.

### Transaction/Explorer Endpoints
- `TransactionController`: `POST /addTransaction`, `GET /isWait58`, `GET /getTransactions`.
- `ConductorController`: `GET /keys`, `GET /time`, `POST /statusTransaction64`, `POST /statusTransaction58`, `GET /account`, `GET /dollar`, `GET /stock`, `GET /isTransactionAdd`, `POST /TransactionAddBase`, `POST /isTransactionAddBase64`, `POST /TransactionAddBase64`, `GET /blockHash`, `POST /findBlocksFromSign58`, `POST /findBlocksFromSign64`, `GET /conductorBlock`, `GET /conductorHashTran`, `GET /senderTransactions`, `GET /customerTransactions`, `GET /senderCountDto`, `GET /customerCountDto`, `GET /addresses`, `GET /getBlocksBySenderInRange`, `GET /findBlocksByCustomerInRange`.

### Pool/Mining Support Endpoints
- `PoolBlockController` under `/pool`: `POST /pool/add`, `GET /pool/getPoolBlockMap`, `GET /pool/ConfirmedBalances`, `GET /pool/getPendingTxMap`, `GET /pool/getPoolAddress`.
- `BasisController` winner endpoints expose current tournament candidate/winner data.

## Existing Wallet API/Local Endpoints Relevant to V35
- `MineController`: `GET /mining`, `POST /customDiff`, `POST /staking`, `POST /unstaking`.
- `BasisController`: `GET /mine`, `GET /process-mining`, `GET /stopMining`, `GET /moreMining`, `GET /constantMining`, `GET /status`, `GET /addBlock`, `GET /miningblock`, `POST /block`, `POST /sub-blocks`, `GET /nodes/resolve`, `GET /getNodes`, `POST /nodes/register`.
- `StorageController`: `GET /globalSize`, `GET /globalVersion`, `GET /difficulty`.

## V35 Endpoint Coverage
- Protocol info: partial via `GET /version`, missing structured V35 constants endpoint.
- Current fork/index info: partial via `GET /size`, `GET /v28Index`, missing `/protocol/fork` or `/protocol/info` with V35 active status.
- Supply info: partial via `GET /totalDollars`, `GET /totalStaking`, `GET /totalAll`, missing structured Gold/Silver/raw supply and jackpot reserve supply.
- Burn status: missing.
- Founder lock status: missing.
- Jackpot status: missing.
- Mining status: partial via wallet `GET /status`, server winner endpoints; missing V35 mining state endpoint with target, difficulty, estimated work, stalled status.
- Wallet balance: partial via `GET /account`, `GET /dollar`, `GET /stock`, `GET /balance`; missing Gold/Silver display normalization.
- Ticket weight: missing.
- Staking calculator: missing.
- Transaction boost calculator: missing.
- Node sync status: partial via `GET /datashort`, `GET /status`, `GET /isSaveFile`, `GET /difficultyBlockchain`; missing structured sync/peer/cumulativeWork endpoint.
- Latest block / chain score: partial via `GET /prevBlock`, `GET /datashort`; missing cumulativeWork and deterministic chain score endpoint.

## Recommended V35 API Additions
- `GET /api/v35/protocol`: fork index, active flag, reward constants, burn address, jackpot periods, silver display multiplier, pow mode.
- `GET /api/v35/fork`: current height, current fork name/version, V35 active boolean.
- `GET /api/v35/supply`: Gold supply, raw Silver supply, displayed Silver supply, burned Gold/Silver, jackpot reserve.
- `GET /api/v35/burn`: burn address, founder burn amount/status, spend-lock active boolean.
- `GET /api/v35/founder-lock`: founder reward disabled boolean, founder spend policy, founder burn status.
- `GET /api/v35/jackpot`: current reserve, period windows, next draw heights/times, current tickets, deterministic seed/source.
- `GET /api/v35/mining`: current target, difficulty, cumulativeWork, emergency-drop state, expected block interval, no-manual-difficulty flag.
- `GET /api/v35/wallet/{address}/balance`: raw and display balances.
- `GET /api/v35/wallet/{address}/ticket-weight`: current tickets/weight and contributing mined blocks/stake/tx score.
- `POST /api/v35/calculators/staking`: preview score/weight impact.
- `POST /api/v35/calculators/transaction-boost`: preview score/weight impact.
- `GET /api/v35/sync`: peer count, local height, best peer height, cumulativeWork, validation status.
- `GET /api/v35/chain-score`: latest block, cumulativeWork, height, tie-break hash.

## API Risk Notes
- Keep old endpoints stable for wallet compatibility.
- New V35 endpoints should expose read-only protocol state first. Mutation endpoints should wait until consensus code is implemented and tested.
