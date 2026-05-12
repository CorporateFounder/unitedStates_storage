# CITU V35 Final Fork Plan

Phase 0 audit only. No source code changes were made.

## Goal
Implement V35 as the final major hard fork while preserving validation of the historical chain from genesis through all old forks.

## Non-Negotiable Constraints
- Do not change transaction format.
- Do not change signature preimage or signature verification.
- Do not change block hash serialization for old blocks.
- Do not change old block validation rules.
- Every V35 rule must be gated by an explicit fork index/height.

## Recommended Implementation Order
1. Add shared V35 constants to wallet and server `Seting.java`: `V35_INDEX`, reward constants, jackpot constants, burn address, silver display multiplier, target/difficulty constants, emergency drop constants.
2. Add read-only V35 protocol/API endpoints on server, returning constants and current status without changing consensus.
3. Add consensus tests for existing historical validation before changing mining/validation.
4. Add target-based PoW helpers in shared utility class, preferably `BlockchainDifficulty` or a new shared helper called from both wallet and server.
5. Add cumulative-work calculation and metadata; expose it in a V35 chain-score endpoint.
6. Update `UtilsBlock.difficulty(...)` and related difficulty helpers to branch to V35 automatic target adjustment after `V35_INDEX`.
7. Add emergency difficulty drop logic, gated after V35, with tests around stalled block timestamps.
8. Update `Mining.miningDay(...)` post-V35 reward creation: remove founder reward, create 8 GOLD miner reward and 2 GOLD jackpot reserve transaction/accounting according to final reserve design.
9. Update `UtilsBlock.validationOneBlock(...)` post-V35 reward validation: reject founder reward, enforce exactly 10 GOLD total issuance, enforce 8+2 split.
10. Add burn address policy in transaction validation/balance flow: after V35, burn address cannot spend; inbound burn transactions allowed; burned amounts tracked.
11. Add jackpot ticket/reserve/draw logic. Every mined V35 block becomes a ticket. Winners must be deterministic from chain data.
12. Replace server sync/tournament chain choice with valid-chain then highest cumulativeWork, then height, capped stake/tx tie-breaker, deterministic hash tie-breaker.
13. Update wallet mining UX after consensus/API is stable: one-button mining, Eco/Normal/Boost local modes, hide manual difficulty after V35.
14. Update website after constants/API are final; verify text against protocol endpoint.
15. Run full verification plan, including old chain validation, wallet/server rule parity, and chain tournament tests.

## Files Most Likely to Change Later
- Server and wallet `setings\Seting.java`
- Server and wallet `model\Mining.java`
- Server and wallet `utils\UtilsBlock.java`
- Server and wallet `utils\UtilsUse.java`
- Server and wallet `utils\UtilsDIfficult.java`
- Server and wallet `utils\BlockchainDifficulty.java`
- Server and wallet `entity\blockchain\Blockchain.java`
- Server and wallet `entity\blockchain\block\Block.java` only if adding post-V35 helper behavior without changing serialization.
- Server and wallet `entity\DtoTransaction\DtoTransaction.java` should probably not change except tests/comments; signature format must remain stable.
- Server `model\TournamentService.java`, `model\Tournament.java`, `utils\UtilsResolving.java`, `model\comparator\HostEndDataShortBComparator.java`, `entity\blockchain\DataShortBlockchainInformation.java`.
- Wallet templates/controllers listed in `CITU_WALLET_UI_MAP.md`.
- Website templates listed in `CITU_WEBSITE_MAP.md`.

## V35 Constants to Define
- `V35_INDEX`.
- `V35_FIXED_BLOCK_REWARD_GOLD = 10`.
- `V35_MINER_INSTANT_REWARD_GOLD = 8`.
- `V35_JACKPOT_RESERVE_REWARD_GOLD = 2`.
- `V35_FOUNDER_REWARD_GOLD = 0`.
- `V35_BURN_ADDRESS`.
- `V35_SILVER_DISPLAY_MULTIPLIER = 100`.
- Jackpot period block counts: daily, weekly, monthly, quarterly, half-year, yearly.
- Target adjustment interval, max adjustment factor, emergency stall window, emergency drop factor, minimum/maximum target.

## Compatibility Strategy
- Add methods with names that clearly indicate V35 behavior; route calls through fork-index branching.
- Keep old methods intact when possible and call them for `index < V35_INDEX`.
- Avoid replacing existing constants used by old forks.
- Do not remove `founderAddress` from `Block`; old blocks require it.
- Do not remove `digitalStockBalance`; Silver is display-only.
