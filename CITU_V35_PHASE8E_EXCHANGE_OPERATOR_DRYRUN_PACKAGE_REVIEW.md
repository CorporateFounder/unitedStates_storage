# CITU V35 Phase 8E Exchange / Operator Dry-Run Package Review

## 1. Executive summary

Phase 8E reviewed the Phase 8D dry-run preactivation release package as an exchange/node-operator handoff package.

Result:

- Package contents are clean and limited to expected public artifacts/docs.
- No private keys, wallet databases, blockchain resources, local node state, full `target/` directories, cache/temp files, or secret config files were found.
- All files listed in `SHA256SUMS.txt` match their actual SHA-256 hashes.
- No files listed in `SHA256SUMS.txt` are missing.
- Two generated package-control files are present but not listed in `SHA256SUMS.txt`: `RELEASE_DRYRUN_MANIFEST.md` and `SHA256SUMS.txt`.
- Package is understandable as a dry-run package, but it still needs final operator-facing upgrade instructions, support/contact details, exact activation height notice, final checksum package, and final monitoring instructions before production release.

No code, tests, V35 constants, builds, tags, burn transactions, fork activation, or jackpot payout transactions were changed or created in this phase.

## 2. Dry-run folder reviewed

```text
C:\citu\release-dryrun\v35-preactivation-dryrun-20260529-2102
```

## 3. Files found in package

The package contains 9 files:

| File | Bytes | Expected |
| --- | ---: | --- |
| `CITU_V35_PHASE6F2_FINAL_ACTIVATION_COMMIT_CHECKLIST.md` | `9303` | yes |
| `CITU_V35_PHASE8C_ARTIFACT_CHECKLIST.md` | `6807` | yes |
| `CITU_V35_PHASE8C_RELEASE_NOTES_FINAL.md` | `4760` | yes |
| `CITU_V35_PHASE9C_HISTORICAL_REPLAY_SIGNOFF_RELEASE_GATE_REPORT.md` | `7520` | yes |
| `CITU_V35_PHASE9E_FINAL_PRE_ACTIVATION_SAFETY_AUDIT_REPORT.md` | `8190` | yes |
| `RELEASE_DRYRUN_MANIFEST.md` | `1689` | yes |
| `SHA256SUMS.txt` | `799` | yes |
| `unitedStates-0.0.423-SNAPSHOT.jar` | `53109985` | yes |
| `unitedStates-0.32.216-SNAPSHOT.jar` | `58102597` | yes |

## 4. Files expected but missing

No required Phase 8D files are missing.

Package polish items for a real exchange/operator package:

- final exchange/operator notice with support contact
- explicit upgrade command/instructions
- rollback and monitoring one-page checklist
- final activation height notice, once selected
- final tag and final artifact checksums, once created

These are not expected in the current dry-run package because V35 is not activated and no final release tag exists yet.

## 5. Unexpected or suspicious files found

No suspicious files were found.

Specifically, the dry-run folder does not contain:

- private keys
- wallet databases
- blockchain resources
- local node state
- copied `target/` directories
- logs
- secret config files
- cache/temp folders

The only filename-pattern hits during a targeted private/state scan were the two expected `unitedStates` jar artifacts. That is a false positive from the project name, not a private/state file.

## 6. SHA256 verification result

`SHA256SUMS.txt` contains 7 listed files.

Verification result:

- matched checksum entries: `7`
- mismatched checksum entries: `0`
- missing listed files: `0`
- extra files not listed in `SHA256SUMS.txt`: `2`

Matching listed files:

```text
0081DD4876C621C5BF49E20FCDC6CA5EF0722BE39BA7F5C899C6EF0B20B00E5B  CITU_V35_PHASE6F2_FINAL_ACTIVATION_COMMIT_CHECKLIST.md
1D270640DCC8865FDA61F617EA9D5A0E821651564BB95F51A0A64B5F8188A05D  CITU_V35_PHASE8C_ARTIFACT_CHECKLIST.md
737A8E3B7E974B8B4556E19E72075DDAB9C416182A4B95BEAD1DD403E87BE5AD  CITU_V35_PHASE8C_RELEASE_NOTES_FINAL.md
A575874FB2FD90DC77CCBC1D1E560334DD6EE40742E90E97078664F45C2854E3  CITU_V35_PHASE9C_HISTORICAL_REPLAY_SIGNOFF_RELEASE_GATE_REPORT.md
7AD1F10E62D367682B50C6F05931F703E65F5EDC15596C7BCD7586BD9D284384  CITU_V35_PHASE9E_FINAL_PRE_ACTIVATION_SAFETY_AUDIT_REPORT.md
8434948BE10A3EE8B978753280E5E8A1704C1EF29583EC5621E07D30934B32DB  unitedStates-0.0.423-SNAPSHOT.jar
A349BCFAE3C8CD6FBA486010F2C46B589A8875D628F8B3F9F10F03C43F0ABC6A  unitedStates-0.32.216-SNAPSHOT.jar
```

Extra files not listed in `SHA256SUMS.txt`:

- `RELEASE_DRYRUN_MANIFEST.md`
- `SHA256SUMS.txt`

Recommendation:

- In the final release package, include every distributable file in the checksum file or provide a clear policy that the checksum file covers only artifacts/docs and not itself.
- Include `RELEASE_DRYRUN_MANIFEST.md` or final manifest in checksums.

## 7. Manifest review result

`RELEASE_DRYRUN_MANIFEST.md` includes:

- dry-run folder path
- git commit hash: `aa466242208848e25adc35c9dcf68fcb9bb47964`
- artifact/doc names
- artifact/doc sizes
- SHA-256 entries
- V35 disabled confirmation
- no burn/fork/jackpot confirmation
- clear warning that this is not a final release and not V35 activation

Manifest status:

- `PASS` for dry-run package clarity.
- Needs final-release replacement before production handoff because this manifest intentionally says dry-run only.

## 8. Secret/private data audit result

Secret/private data audit result: `PASS`.

No private keys, wallet databases, blockchain resource data, node local state, full build directories, logs, cache/temp files, or secret config files were found in the reviewed dry-run folder.

## 9. Operator-readiness status

Current operator-readiness status: `DRY-RUN READY`, not production-ready.

The package is understandable for an internal release reviewer and technically useful for exchange/node dry-run review because it includes:

- server jar
- wallet jar
- release notes
- activation checklist
- artifact checklist
- historical replay signoff
- safety audit report
- manifest
- checksums for listed files

It is not yet enough for a final exchange/operator release because it lacks:

- exact activation fork height
- final tag/version
- final release artifact names
- final support/contact channel
- final upgrade deadline
- final deposit/withdrawal caution window
- final post-fork monitoring instructions

## 10. Missing docs/instructions

Missing before production release:

- final exchange/operator notice with support contact
- checksum verification instructions in a short operator-facing checklist
- final fork index and estimated activation time
- upgrade deadline
- rollback/emergency communication instructions
- post-fork monitoring checklist
- final release tag and exact artifact/version names
- explicit "do not use this dry-run package as final activation release" warning in any external handoff email/message

Created in this phase:

- `CITU_V35_PHASE8E_OPERATOR_PACKAGE_CHECKLIST.md`

## 11. Confirmation V35 remains disabled

Confirmed from reviewed reports and manifest:

- `V35_RESCUE_FORK_INDEX = Long.MAX_VALUE`
- `V35_BURN_ADDRESS = BURN_ADDRESS`
- no activation height selected
- this package is preactivation/dry-run only

No V35 constant was changed in this phase.

## 12. Confirmation no burn/fork/jackpot action was performed

Confirmed:

- no burn transaction was created
- no fork activation was performed
- no jackpot payout transaction was created
- no build was rerun
- no historical replay was rerun
- no final release was declared

## 13. Confirmation no final git tag was created

Confirmed.

No git tag was created in Phase 8E.

## 14. Remaining risks

- Dry-run package is not signed or final.
- `SHA256SUMS.txt` does not list `RELEASE_DRYRUN_MANIFEST.md` or itself.
- Operator-facing upgrade and rollback instructions still need finalization.
- Support/contact channel is still a placeholder.
- Real fork index is still not selected.
- Founder burn is still not executed.
- Exchanges/operators must not mistake this dry-run for final activation release.
- Final package must be rebuilt from a clean tree and re-checksummed after final activation decisions.

## 15. Recommended next phase

Recommended next phase:

- Phase 8F - exchange/operator notice finalization, no activation

Alternative:

- Phase 7F - founder burn final review, no burn execution
