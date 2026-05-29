# CITU V35 Phase 8F - Internal Release Review Checklist

## Scope

This checklist is for internal review of the pre-activation exchange/operator notice package.

It does not activate V35, create a final tag, create a burn transaction, or create a jackpot payout transaction.

## Pre-Activation Review

- `[ ]` Confirm git working tree is clean before final release work.
- `[ ]` Confirm dry-run package path:
  `C:\citu\release-dryrun\v35-preactivation-dryrun-20260529-2102`
- `[ ]` Confirm `SHA256SUMS.txt` verification passed.
- `[ ]` Confirm `SHA256SUMS.txt.sha256` verification passed.
- `[ ]` Confirm V35 remains disabled.
- `[ ]` Confirm `V35_RESCUE_FORK_INDEX = Long.MAX_VALUE`.
- `[ ]` Confirm `V35_BURN_ADDRESS = BURN_ADDRESS`.
- `[ ]` Confirm no burn transaction was created.
- `[ ]` Confirm no fork activation was performed.
- `[ ]` Confirm no jackpot payout transaction was created.
- `[ ]` Confirm no final git tag was created.
- `[ ]` Confirm server historical replay PASS.
- `[ ]` Confirm wallet historical replay PASS.
- `[ ]` Confirm replay-only safety gate PASS.
- `[ ]` Confirm server/wallet parity.
- `[ ]` Confirm exchange notice reviewed.
- `[ ]` Confirm node operator notice reviewed.
- `[ ]` Confirm wallet user notice reviewed.
- `[ ]` Confirm final activation height has not been selected yet.
- `[ ]` Confirm founder burn remains separate and unexecuted.

## Notice Review

- `[ ]` Exchange notice clearly says pre-activation only.
- `[ ]` Exchange notice does not tell exchanges to upgrade yet.
- `[ ]` Exchange notice does not announce a fork height.
- `[ ]` Node operator notice says no upgrade is required yet.
- `[ ]` Node operator notice warns against manual constant changes.
- `[ ]` Wallet user notice says no user action is required yet.
- `[ ]` Wallet user notice warns against fake burn addresses and unofficial links.

## Required Final Activation Fields Later

- `[ ]` Exact fork height.
- `[ ]` Final release tag.
- `[ ]` Final artifact names.
- `[ ]` Final SHA256 checksums.
- `[ ]` Estimated activation date/time.
- `[ ]` Exchange upgrade deadline.
- `[ ]` Deposit/withdrawal caution window.
- `[ ]` Support/contact channel.
- `[ ]` Rollback/monitoring instructions.

## Approval Placeholders

- Technical reviewer: `[NAME / DATE / SIGNOFF]`
- Release reviewer: `[NAME / DATE / SIGNOFF]`
- Founder burn reviewer: `[NAME / DATE / SIGNOFF]`
- Exchange communication reviewer: `[NAME / DATE / SIGNOFF]`

## Current Gate Status

- Historical replay gate: PASS.
- Wallet mirror gate: PASS.
- Replay-only safety gate: PASS.
- Release notes gate: PASS.
- Artifact dry-run build/checksum gate: PASS.
- Exchange/operator dry-run package review: PASS.
- Checksum polish gate: PASS.
- V35 activation gate: NOT YET.
- Founder burn gate: NOT YET.
- Final release gate: NOT YET.
