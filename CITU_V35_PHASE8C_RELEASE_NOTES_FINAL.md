# CITU V35 Phase 8C Final Release Notes

## Executive summary

This is the pre-activation V35 release notes package.

V35 code is implemented and safety-hardened, but V35 is not active yet. The fork index remains disabled in both wallet and server:

- `V35_RESCUE_FORK_INDEX = Long.MAX_VALUE`
- `V35_BURN_ADDRESS = BURN_ADDRESS`

Historical replay, wallet mirror parity, and replay-only safety gates have passed. Final activation, founder burn, artifact build, exchange delivery, and final release tagging remain separate gates.

## What V35 changes

V35 introduces the following protocol model after activation by a future block index:

- Target block time: `200 seconds`
- Expected blocks per day: `432`
- Total reward per V35 block: `8 GOLD`
- Miner reward: `6 GOLD`
- Jackpot reserve: `2 GOLD`
- Founder reward after V35: `0 GOLD`
- Annual emission: `1,261,440 GOLD`
- Burn address sentinel: `BURN_ADDRESS`
- Founder burn plan: separate manual operation, not executed yet

V35 also includes:

- Bitcoin-like proof-of-work target validation after activation.
- cumulativeWork chain/candidate selection after activation.
- Jackpot policy for minerAddress-only tickets.
- Burn-address no-spend policy after activation.
- Public V35 wallet/dashboard/site/API documentation and status pages.

## Founder burn status

The founder burn has not happened yet.

The current plan keeps founder burn separate from fork activation:

- Founder burn should use a fixed snapshot height and block hash.
- Recommended burn is `80%` of snapshot founder balance.
- Burn recipient is exactly `BURN_ADDRESS`.
- Public proof must include snapshot, transaction hash, burn block hash/index, and before/after supply values.
- Modeled burn values must not be displayed as actual burned supply.

## What was verified

Historical replay and safety gates:

- Server historical replay: `PASS`, reached the end.
- Wallet historical replay: `PASS`, reached the end.
- Historical replay gate: `PASS`.
- Wallet mirror gate: `PASS`.
- Replay-only safety gate: `PASS`.

Validation boundary:

- Replay-only compatibility is isolated to historical replay paths.
- Normal live validation remains strict.
- No hardcoded production block exception was added.
- No hardcoded production address exception was added.

Test/build status from completed reports:

- Server tests after Phase 9D: `225` passed.
- Wallet tests after Phase 9D: `200` passed.
- Server clean package previously passed after replay fixes.
- Wallet clean package previously passed after mirror fixes.

## What is not activated yet

The following are not complete and must not be represented as complete:

- V35 fork height: `NOT SELECTED`
- V35 activation: `NOT PERFORMED`
- Founder burn: `NOT EXECUTED`
- Jackpot payout transaction: `NOT CREATED`
- Final release tag: `NOT CREATED`
- Final release artifacts/checksums: `NOT CREATED IN THIS PHASE`

## User and operator warnings

Do not activate V35 until the final activation plan is approved.

Do not manually change `V35_RESCUE_FORK_INDEX` outside the activation procedure.

Do not change wallet/server constants independently. The wallet and server fork index must match exactly in the final activation commit.

Do not claim founder burn is complete until the burn transaction is confirmed and public proof fields are filled with real values.

Exchanges and node operators should wait for a signed or otherwise authenticated final release artifact package with checksums before production upgrade.

Release candidates may contain V35 code while still leaving V35 disabled. RC builds must be clearly labeled as pre-activation or disabled.

## Operational release notes outline

When packaging the RC or final release, include:

- Summary of V35 status.
- Whether release is RC/pre-activation or final activation.
- Exact fork index if final activation.
- Reward model: `8 = 6 miner + 2 jackpot + 0 founder`.
- Block target: `200 seconds`.
- Expected blocks/day: `432`.
- Annual emission: `1,261,440 GOLD`.
- PoW and cumulativeWork notes.
- Jackpot policy notes.
- Burn/founder notes and proof link.
- API/dashboard/website notes.
- Upgrade instructions.
- Exchange/node instructions.
- Known remaining gates.
- Protocol upgrade disclaimer, not financial advice.

## Current gate status

- Historical replay gate: `PASS`
- Wallet mirror gate: `PASS`
- Replay-only safety gate: `PASS`
- Release notes gate: `PASS AFTER PHASE 8C`
- Artifact build gate: `NOT YET`
- V35 activation gate: `NOT YET`
- Founder burn gate: `NOT YET`
- Final release gate: `NOT YET`

## Recommended next phase

Recommended next phase:

- Phase 6F-2 - final activation commit checklist, no code changes

Alternative:

- Phase 8D - release artifact staging checklist execution plan, no artifact build yet
