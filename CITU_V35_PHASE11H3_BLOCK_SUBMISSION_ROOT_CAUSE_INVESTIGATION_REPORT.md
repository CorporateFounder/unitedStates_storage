# CITU V35 Phase 11H-3 Block Submission Root Cause Investigation Report

## Executive summary

Phase 11H-3 traced the real wallet mining and server block-submission path. The observed symptom is not caused by the wallet submitting to port `8082`; the wallet submit path targets the configured node host and posts to `/nodes/resolve_from_to_block`.

The key finding is that the server receive endpoint does not directly persist a submitted block. It validates the candidate, adds it to `winnerList`, propagates it, and returns `OK`. The actual database/resource write and `/size` update happen later in `TournamentService.tournament(...)` through `utilsResolving.addBlock3(...)`. If the node runs out of heap before or during that tournament/persist cycle, `/size` will not increase even if the wallet found and submitted a block.

The likely heap-growth source is the server tournament/peer path: `TournamentService.getAllWinner(...)` and `TournamentService.getInstance(...)` fan out over peer hosts, fetch `/winnerList`, `/prevBlock`, and `/getTransactions`, parse JSON into block/transaction lists, and previously did so without a local host cap in those methods. This can consume heap independently of wallet block submission and can kill the server before the queued candidate is persisted.

## Wallet send path

Mining starts from:

- `unitedStates_final-master/src/main/java/International_Trade_Union/controllers/BasisController.java`
- `@GetMapping("/process-mining")`
- `@GetMapping("/miningblock")`
- `@GetMapping("/mine")`
- implementation: `BasisController.mining()`

Block mining happens through:

- legacy/pre-V35: `Block.findHash_MT2(...)`
- V35: `Mining.mineV35Block(...)`
- dispatcher: `Mining.miningDay(...)`

When a block is found, wallet code reaches:

- `BasisController.mining()`
- log point: `block to send: <index>`
- sender: `BasisController.sendAllBlocksToStorage(List<Block> blocks)`

Submit target:

- `BasisController.sendAllBlocksToStorage(...)`
- reads configured server from `Seting.YOUR_SERVER` if present, otherwise `Seting.ORIGINAL_ADDRESSES`
- production default in `Seting.ORIGINAL_ADDRESSES`: `http://194.87.236.238:82`
- endpoint: `/nodes/resolve_from_to_block`
- method: `POST`
- body type: `SendBlocksEndInfo`

The wallet does not submit mined blocks to wallet port `8082`. The expected production submit URL is:

`http://194.87.236.238:82/nodes/resolve_from_to_block`

## Wallet instrumentation added

The wallet now logs and exposes:

- `WALLET_BLOCK_FOUND`
- found block index/hash/previous hash/difficulty/random proof/tx count/miner
- `WALLET_BLOCK_SUBMIT_START`
- target URL, endpoint, method, block index/hash, body size, timeout values
- `WALLET_BLOCK_SUBMIT_RESPONSE`
- HTTP status and elapsed ms
- `WALLET_BLOCK_SUBMIT_ERROR`
- exception class/message, target URL, block index/hash

The wallet processing page now shows:

- last found block index/hash
- last submit URL
- last submit HTTP status
- last submit response
- last submit error
- last submit time
- node size last seen

If the page shows `not found yet`, the wallet is mining but has not found a valid block yet.

## Server receive path

The server endpoint capable of receiving wallet-mined blocks is:

- file: `unitedStates_storage-master/src/main/java/International_Trade_Union/controllers/BasisController.java`
- endpoint: `POST /nodes/resolve_from_to_block`
- method: `BasisController.resolve_conflict(...)`
- request type: `SendBlocksEndInfo`

This endpoint:

1. checks `blockedNewSendBlock`
2. checks `Seting.VERSION`
3. checks candidate timestamps and miner address
4. checks candidate index against `BasisController.getBlockchainSize()`
5. checks previous hash, minimum difficulty, timestamp bounds, and hash equality
6. validates via `Blockchain.shortCheckWithCanonicalContext(...)`
7. checks `shouldAcceptResolveCandidate(...)`
8. adds the block to `winnerList`
9. calls propagation through `utilsResolving.sendAllBlocksToStorage(...)`
10. returns `OK`

It does not directly call `utilsResolving.addBlock3(...)` for the received block.

## Server persistence path

The actual add/write path is:

- file: `unitedStates_storage-master/src/main/java/International_Trade_Union/model/TournamentService.java`
- method: `TournamentService.tournament(...)`
- winner selection path uses `winnerList`
- actual write: `utilsResolving.addBlock3(winner, balances, Seting.ORIGINAL_BLOCKCHAIN_FILE, new ArrayList<>())`
- after successful save, `BasisController.setShortDataBlockchain(...)`, `BasisController.setBlockcheinSize(...)`, and `BasisController.setPrevBlock(...)` update the in-memory state used by `/size`

Therefore `/size` increases only after the tournament winner is persisted, not merely after `/nodes/resolve_from_to_block` returns `OK`.

## Server instrumentation added

The server now logs:

- `SERVER_BLOCK_RECEIVED`
- endpoint, remote address, block index/hash/previous hash/miner/tx count
- server size before validation
- V35 active status according to server
- `SERVER_BLOCK_VALIDATE_START`
- expected current size and expected/actual previous hash
- `SERVER_BLOCK_VALIDATE_RESULT`
- `SERVER_BLOCK_ADD_REJECTED` with reason
- `SERVER_BLOCK_ADD_SUCCESS`
- queued-for-tournament state at receive time
- persisted-by-tournament state after `addBlock3(...)` succeeds

Added read-only endpoint:

`GET /debug/block-submit/status`

It returns:

- `currentSize`
- `lastReceivedBlockIndex`
- `lastReceivedBlockHash`
- `lastReceivedAt`
- `lastSubmitRemoteAddress`
- `lastValidationResult`
- `lastRejectReason`
- `lastAddedBlockIndex`
- `lastAddedBlockHash`
- `lastAddedAt`
- `lastWalletSubmitEndpoint`
- `lastServerException`
- `temporaryBlockQueueSize`
- `freeMemory`
- `totalMemory`
- `maxMemory`

The endpoint is read-only and does not load blockchain files.

## OOM investigation

The likely remaining heap source is the tournament/peer loop, not the `/size` endpoint.

Relevant code paths:

- `TournamentService.getAllWinner(...)`
- fetches peer `/winnerList` and `/prevBlock`
- parses winner JSON into `List<Block>`
- adds candidates into `BasisController.getWinnerList()`

- `TournamentService.getInstance(...)`
- fetches peer `/getTransactions`
- parses transactions into lists
- combines remote transactions with local transactions

These operations can happen even without a wallet block submission. If peers are slow, return large responses, or the host list is large, the server can consume heap before the queued candidate reaches the tournament persistence phase.

Phase 11H-3 added:

- `MAX_TOURNAMENT_HOSTS = 32`
- `TOURNAMENT_HTTP_TIMEOUT_MILLIS = 3000`
- bounded host selection for `getAllWinner(...)`
- bounded host selection for `getInstance(...)`
- short timeouts for `/winnerList`, `/prevBlock`, and `/getTransactions`

This does not change consensus, scoring, rewards, or block format.

## Answers to required questions

1. Does wallet actually find a block?

The code now distinguishes this. If `WALLET_BLOCK_FOUND` is absent and the processing page says `not found yet`, the wallet is mining but has not found a valid block yet. If it finds a block, the new logs and UI show the exact block index/hash.

2. If yes, where exactly does it submit it?

`BasisController.sendAllBlocksToStorage(...)` submits to `<node>/nodes/resolve_from_to_block`.

3. Is the submit URL correct?

The expected production URL is `http://194.87.236.238:82/nodes/resolve_from_to_block`. Tests assert the helper builds port `82` and not wallet port `8082`.

4. Which server endpoint receives the block?

`POST /nodes/resolve_from_to_block` in server `BasisController.resolve_conflict(...)`.

5. Does server receive anything from wallet?

This is now directly observable through server logs and `GET /debug/block-submit/status`. Before this patch there was not enough structured evidence to answer from logs alone.

6. If server receives it, why is it accepted/rejected?

The server now records `lastValidationResult` and `lastRejectReason`. Rejection reasons include version mismatch, wrong index, duplicate candidate, previous-hash mismatch, low difficulty, timestamp problems, hash mismatch, failed short check, or losing chain-score comparison.

7. Does `temporaryBlocks` / old block list prevent adding?

The receive endpoint uses `winnerList`, not `temporaryBlocks`, for incoming mined-block candidates. A duplicate already in `winnerList` causes rejection. The actual persist happens later in tournament selection.

8. Does blockchain/resource DB write happen?

Not in `/nodes/resolve_from_to_block`. DB/resource write happens later through `TournamentService.tournament(...)` and `utilsResolving.addBlock3(...)`.

9. Does `/size` increase after add?

`/size` increases only after successful tournament persistence updates `BasisController.blockcheinSize`. It will not increase merely because the receive endpoint returned `OK`.

10. Is node OOM happening before any block can be accepted?

It can. The peer/tournament loops can allocate and parse peer winner/transaction responses independently of wallet submission. If the JVM OOMs before tournament persistence, a received/queued block will not become `/size + 1`.

11. Exact root cause.

Root cause: block submission is tournament-mediated, while the server can OOM in peer/tournament fetch/parse loops before the queued candidate is persisted. The wallet may be mining correctly and submitting to the correct endpoint, but `/size` remains unchanged until the server tournament loop persists the winner. OOM interrupts that path.

12. Exact minimal fix proposal.

Keep the diagnostics from this phase, deploy a server build with:

- `/debug/block-submit/status`
- receive/add/reject logs
- tournament host cap
- tournament fetch timeouts

Then run the wallet and server together. Use:

- wallet processing page to confirm `WALLET_BLOCK_FOUND`
- wallet processing page to confirm submit URL/status
- server `/debug/block-submit/status` to confirm receive/validation/queue/persist
- server heap flags from 11H-0/11H-1 to capture any remaining OOM

If a block is received and queued but not persisted, inspect tournament timing and `winnerList`. If it is rejected, use `lastRejectReason`.

## Files changed

Wallet:

- `unitedStates_final-master/src/main/java/International_Trade_Union/controllers/BasisController.java`
- `unitedStates_final-master/src/main/resources/templates/processUpdating.html`
- `unitedStates_final-master/src/test/java/International_Trade_Union/controllers/WalletMiningProcessingTemplateTest.java`

Server:

- `unitedStates_storage-master/src/main/java/International_Trade_Union/controllers/BasisController.java`
- `unitedStates_storage-master/src/main/java/International_Trade_Union/model/TournamentService.java`
- `unitedStates_storage-master/src/test/java/International_Trade_Union/controllers/BlockSubmitDebugStatusTest.java`
- `unitedStates_storage-master/src/test/java/International_Trade_Union/model/TournamentServiceMemoryGuardTest.java`

## Tests

Wallet:

- command: `.\mvnw.cmd test`
- result: PASS
- tests run: `212`
- failures: `0`
- errors: `0`
- skipped: `0`

Wallet package:

- command: `.\mvnw.cmd clean package`
- result: PASS

Server:

- command: `.\mvnw.cmd test`
- result: PASS
- tests run: `245`
- failures: `0`
- errors: `0`
- skipped: `0`

Server package:

- command: `.\mvnw.cmd clean package`
- result: PASS

Note: server test output includes a stack trace from an existing negative test that intentionally verifies invalid V35 jackpot payout rejection. Maven reports the test suite as passing.

## Safety confirmation

- No `Seting.java` file changed.
- `V35_RESCUE_FORK_INDEX` remains `514291`.
- `V35_BURN_ADDRESS` remains `BURN_ADDRESS`.
- No consensus rule changed.
- No reward rule changed.
- No V35 activation height changed.
- No burn transaction was created.
- No jackpot payout transaction was created.
- No `C:\resources` or `C:\server\resources` files were touched.
- No VPS deploy, restart, or process kill was performed.
- No GitHub release/upload was performed.

## Recommended next safe patch

Phase 11H-4 should be a controlled server+wallet diagnostic release only if approved. It should package the instrumentation and memory guards from this phase, then run a short VPS observation window:

1. start node with heap flags and heap dump enabled
2. start wallet 1.0.1+diagnostic build
3. observe wallet `WALLET_BLOCK_FOUND` / submit state
4. query `/debug/block-submit/status`
5. confirm whether `/size` changes after tournament persistence
6. if OOM still occurs, use heap dump plus debug state to identify the remaining allocator
