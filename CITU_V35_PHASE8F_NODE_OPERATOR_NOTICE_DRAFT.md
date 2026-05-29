# CITU V35 Phase 8F - Node Operator Notice Draft

## Status

No node upgrade is required yet.

The current V35 package is a pre-activation dry-run package only. It exists to test artifact structure, checksums, operator documentation, and release readiness before a final V35 activation release is approved.

V35 remains disabled with `V35_RESCUE_FORK_INDEX = Long.MAX_VALUE`.

## Dry-Run Package Purpose

The dry-run package demonstrates what a future release package may look like:

- Server artifact.
- Wallet artifact.
- Release notes.
- Activation checklist.
- Historical replay signoff.
- Safety audit report.
- Manifest.
- SHA256 checksum files.

Dry-run package reviewed:

`C:\citu\release-dryrun\v35-preactivation-dryrun-20260529-2102`

This dry-run package is not a final release and must not be used as a production activation signal.

## Future Final Release Verification

When the final release is published later, operators should verify:

- Final signed/reviewed git tag.
- Release manifest.
- Artifact names and file sizes.
- `SHA256SUMS.txt`.
- `SHA256SUMS.txt.sha256`.
- Exact V35 fork height.
- Estimated activation date/time.
- Final public activation notice.

## Warnings

Operators must not:

- Manually change `V35_RESCUE_FORK_INDEX`.
- Manually change `V35_BURN_ADDRESS`.
- Run unofficial builds.
- Use unsigned or unfinalized artifacts.
- Create founder burn transactions.
- Create fork activation transactions.
- Create jackpot payout transactions manually.
- Treat the dry-run package as V35 activation.

## Future Activation Checklist Placeholder

Before upgrading for final activation later:

- `[ ]` Confirm final release tag.
- `[ ]` Confirm final checksums.
- `[ ]` Confirm exact fork height.
- `[ ]` Confirm estimated activation time.
- `[ ]` Confirm release notes.
- `[ ]` Confirm rollback/monitoring instructions.
- `[ ]` Confirm support/contact channel.
- `[ ]` Back up local node data.
- `[ ]` Upgrade only from final official artifacts.
- `[ ]` Monitor last legacy block and first V35 block.

## Current Gate Status

- Historical replay gate: PASS.
- Wallet mirror gate: PASS.
- Replay-only safety gate: PASS.
- Release notes gate: PASS.
- Artifact dry-run build/checksum gate: PASS.
- Exchange/operator package dry-run review: PASS.
- V35 activation gate: NOT YET.
- Founder burn gate: NOT YET.
- Final release gate: NOT YET.
