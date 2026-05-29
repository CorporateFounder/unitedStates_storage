# CITU V35 Phase 8F - Exchange Notice Draft

## Subject

CITU V35 Pre-Activation Package Notice for Exchange Operators - Dry Run Only, Not Final Activation

## Current Status

This notice is a pre-activation draft for exchange operators. It describes the V35 dry-run package and the release gates that have passed so far.

This is not the final activation notice.

Do not treat this package as a production fork activation. The real V35 fork height has not been selected, final release artifacts have not been tagged, and V35 remains disabled with `V35_RESCUE_FORK_INDEX = Long.MAX_VALUE`.

## What Changes In V35

After the final V35 fork height is selected and activated in a later approved release:

- Target block time: 200 seconds.
- Expected blocks per day: 432.
- Total block reward: 8 GOLD.
- Miner reward: 6 GOLD.
- Jackpot reserve: 2 GOLD.
- Founder reward after V35: 0 GOLD.
- Burn address model: `BURN_ADDRESS` is used as the sentinel/protocol burn sink.
- Founder burn: separate manual operation, not part of this dry-run package, and not yet executed.

## Verification Status

The pre-activation verification gates currently recorded are:

- Server historical replay: PASS, reached the end.
- Wallet historical replay: PASS, reached the end.
- Replay-only safety gate: PASS.
- Server build dry-run: PASS, 225 tests, BUILD SUCCESS.
- Wallet build dry-run: PASS, 200 tests, BUILD SUCCESS.
- Dry-run package checksum verification: PASS.
- `SHA256SUMS.txt.sha256` verification: PASS.
- Dry-run package content review: PASS.

Dry-run package reviewed:

`C:\citu\release-dryrun\v35-preactivation-dryrun-20260529-2102`

## What Exchanges Must Not Do Yet

Until a final activation notice is published:

- Do not treat this dry-run package as final V35 activation.
- Do not change production deposit or withdrawal behavior based only on this dry-run package.
- Do not announce a fork height; no final fork height has been selected.
- Do not use unsigned, unfinalized, or unofficial artifacts for production upgrade.
- Do not manually change `V35_RESCUE_FORK_INDEX`.
- Do not manually change `V35_BURN_ADDRESS`.
- Do not create founder burn, fork activation, or jackpot payout transactions.

## What The Final Activation Notice Must Contain Later

The final exchange activation notice must include:

- Exact V35 fork height.
- Final release tag.
- Final artifact names.
- Final SHA256 checksums.
- Final `SHA256SUMS.txt` and `SHA256SUMS.txt.sha256`.
- Estimated activation date/time based on current chain height and 200 second target block time.
- Upgrade deadline.
- Deposit/withdrawal caution window recommendation.
- Rollback and emergency monitoring instructions.
- Post-fork chain verification instructions.
- Support/contact channel: `[SUPPORT CONTACT TO BE FILLED]`.

## Exchange Operator Checklist For Later Final Notice

- Verify final release tag.
- Verify final checksums against `SHA256SUMS.txt`.
- Verify `SHA256SUMS.txt` against `SHA256SUMS.txt.sha256`.
- Confirm final fork height.
- Confirm estimated activation time.
- Confirm whether deposits/withdrawals should be paused near activation.
- Confirm post-fork chain height, first V35 block hash, and sync status.

## Important Disclaimer

This is protocol upgrade preparation information only. It is not financial advice and it is not a final release or activation notice.
