# CITU V35 Phase 8E Operator Package Checklist

## Purpose

This checklist is for exchange operators, node operators, wallet users, and internal release reviewers who receive a V35 dry-run or later final release package.

Current package status:

```text
PRE-ACTIVATION DRY-RUN ONLY
NOT FINAL RELEASE
NOT V35 ACTIVATION
```

## 1. Verify SHA-256 checksums

From PowerShell, replace the folder path with the actual package folder:

```powershell
cd C:\citu\release-dryrun\v35-preactivation-dryrun-20260529-2102
Get-Content .\SHA256SUMS.txt
Get-FileHash .\unitedStates-0.0.423-SNAPSHOT.jar -Algorithm SHA256
Get-FileHash .\unitedStates-0.32.216-SNAPSHOT.jar -Algorithm SHA256
```

Confirm:

- each listed file exists
- each actual hash matches `SHA256SUMS.txt`
- no artifact is renamed after checksums are generated
- no checksum is copied manually from an untrusted source

For final release packages, every distributed manifest/release-note file should also be included in the checksum policy.

## 2. Confirm package is pre-activation only

Read:

- `RELEASE_DRYRUN_MANIFEST.md`
- `CITU_V35_PHASE8C_RELEASE_NOTES_FINAL.md`
- `CITU_V35_PHASE6F2_FINAL_ACTIVATION_COMMIT_CHECKLIST.md`

Confirm the package says:

- V35 remains disabled
- no final fork index selected
- no final git tag created
- no burn transaction created
- no jackpot payout transaction created
- this is not final release

If the package does not clearly say this, stop and ask for a corrected package.

## 3. Confirm V35 is not activated yet

For this dry-run package, expected values are:

```text
V35_RESCUE_FORK_INDEX = Long.MAX_VALUE
V35_BURN_ADDRESS = BURN_ADDRESS
```

Do not treat the package as an activation release unless an official final activation notice provides:

- exact fork index
- final tag/version
- estimated activation time
- checksums for final artifacts
- exchange/node upgrade deadline

## 4. What operators must NOT do yet

Do not:

- announce V35 as active
- change local constants manually
- create a fork-index activation build
- create a founder burn transaction
- create a jackpot payout transaction
- treat dry-run checksums as final release checksums
- tag this package as final
- replace production exchange/node binaries without final release approval
- resume or pause deposits solely because of this dry-run package

## 5. What exchanges should wait for before upgrading

Exchanges should wait for:

- official final release notice
- final release tag/version
- exact V35 fork index
- estimated activation date/time
- final server/wallet artifacts
- final SHA-256 checksums
- support/contact channel
- deposit/withdrawal caution window
- rollback/emergency communication plan
- post-fork confirmation procedure

Dry-run package review can happen now, but production upgrade should wait for final release approval.

## 6. What node operators should wait for before upgrading

Node operators should wait for:

- final node/wallet artifact
- exact fork index
- final checksum file
- operator upgrade instructions
- chain verification instructions
- monitoring instructions for first V35 blocks

Do not manually change `V35_RESCUE_FORK_INDEX`.

## 7. What final activation notice must contain later

Final activation notice must include:

- release tag/version
- git commit hash
- exact fork index
- estimated activation date/time
- artifact names
- SHA-256 checksums
- upgrade deadline
- support/contact channel
- emergency communication channel
- summary of consensus changes
- founder burn status and proof link if burn already happened
- deposit/withdrawal caution recommendation for exchanges

## 8. Rollback and monitoring placeholders

Rollback placeholder:

```text
If activation problems appear before fork index, operators should remain on or return to the previous approved release according to official instructions.
After fork activation, do not silently roll back; coordinate through the emergency channel to avoid chain split.
```

Monitoring placeholder:

```text
Monitor last legacy block, first V35 block, reward split, PoW target validation, cumulativeWork selection, jackpot reserve/accounting, burn-address no-spend policy, APIs, wallet dashboard, and exchange sync status.
```

## 9. Support/contact placeholder

Final package must replace this placeholder:

```text
Support contact: <PENDING>
Emergency channel: <PENDING>
Exchange/operator coordination contact: <PENDING>
```

Do not publish a final exchange/operator package without real contact information.

## 10. Dry-run review sign-off

Before accepting this as a dry-run review package:

- [ ] checksums for listed files match
- [ ] package contains only expected public files
- [ ] no private key material present
- [ ] no wallet database present
- [ ] no blockchain resource data present
- [ ] no node local state present
- [ ] package says not final release
- [ ] package says not activation
- [ ] V35 disabled status is clear
- [ ] missing final-release items are understood

Dry-run acceptance does not approve production activation.
