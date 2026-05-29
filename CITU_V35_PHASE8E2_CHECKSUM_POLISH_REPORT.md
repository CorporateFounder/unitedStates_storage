# CITU V35 Phase 8E-2 Checksum Polish Report

## 1. Executive summary

Phase 8E-2 polished the dry-run package checksum structure.

Completed:

- Reviewed current dry-run folder contents.
- Regenerated `SHA256SUMS.txt`.
- Added `RELEASE_DRYRUN_MANIFEST.md` to `SHA256SUMS.txt`.
- Kept `SHA256SUMS.txt` out of itself to avoid circular checksum logic.
- Created `SHA256SUMS.txt.sha256` to cover `SHA256SUMS.txt`.
- Updated `RELEASE_DRYRUN_MANIFEST.md` to document the checksum structure and dry-run safety status.
- Verified every file listed in `SHA256SUMS.txt`.
- Verified `SHA256SUMS.txt` using `SHA256SUMS.txt.sha256`.

No code, tests, jars, builds, historical replay, V35 constants, fork activation, burn transaction, jackpot payout transaction, final tag, or commit were changed or created.

## 2. Dry-run folder path

```text
C:\citu\release-dryrun\v35-preactivation-dryrun-20260529-2102
```

## 3. Files included in SHA256SUMS.txt

`SHA256SUMS.txt` now includes 8 package files:

```text
0081DD4876C621C5BF49E20FCDC6CA5EF0722BE39BA7F5C899C6EF0B20B00E5B  CITU_V35_PHASE6F2_FINAL_ACTIVATION_COMMIT_CHECKLIST.md
1D270640DCC8865FDA61F617EA9D5A0E821651564BB95F51A0A64B5F8188A05D  CITU_V35_PHASE8C_ARTIFACT_CHECKLIST.md
737A8E3B7E974B8B4556E19E72075DDAB9C416182A4B95BEAD1DD403E87BE5AD  CITU_V35_PHASE8C_RELEASE_NOTES_FINAL.md
A575874FB2FD90DC77CCBC1D1E560334DD6EE40742E90E97078664F45C2854E3  CITU_V35_PHASE9C_HISTORICAL_REPLAY_SIGNOFF_RELEASE_GATE_REPORT.md
7AD1F10E62D367682B50C6F05931F703E65F5EDC15596C7BCD7586BD9D284384  CITU_V35_PHASE9E_FINAL_PRE_ACTIVATION_SAFETY_AUDIT_REPORT.md
00416DA8C0241AEFB4C3C8D6D21D2548799DBBC45378EA3074F2173474B634C1  RELEASE_DRYRUN_MANIFEST.md
8434948BE10A3EE8B978753280E5E8A1704C1EF29583EC5621E07D30934B32DB  unitedStates-0.0.423-SNAPSHOT.jar
A349BCFAE3C8CD6FBA486010F2C46B589A8875D628F8B3F9F10F03C43F0ABC6A  unitedStates-0.32.216-SNAPSHOT.jar
```

## 4. Files excluded and why

Excluded from `SHA256SUMS.txt`:

- `SHA256SUMS.txt`
- `SHA256SUMS.txt.sha256`

Reason:

- Including `SHA256SUMS.txt` inside itself creates a circular checksum problem.
- `SHA256SUMS.txt.sha256` exists specifically to cover `SHA256SUMS.txt` after it is finalized.

## 5. SHA256SUMS.txt.sha256 result

Created:

```text
C:\citu\release-dryrun\v35-preactivation-dryrun-20260529-2102\SHA256SUMS.txt.sha256
```

Contents:

```text
A8C6B03B4616DEC6B681B21F684EDB62AAF7F4D510ECBED3761F397C860F1A5D  SHA256SUMS.txt
```

## 6. Verification result

Verification passed.

Results:

- files listed in `SHA256SUMS.txt`: `8`
- checksum matches: `8`
- checksum mismatches: `0`
- missing listed files: `0`
- `SHA256SUMS.txt.sha256` verification: `MATCH`

`SHA256SUMS.txt` actual hash:

```text
A8C6B03B4616DEC6B681B21F684EDB62AAF7F4D510ECBED3761F397C860F1A5D
```

## 7. Manifest update summary

Updated:

```text
C:\citu\release-dryrun\v35-preactivation-dryrun-20260529-2102\RELEASE_DRYRUN_MANIFEST.md
```

Manifest now states:

- `SHA256SUMS.txt` covers every release package file except itself and `SHA256SUMS.txt.sha256`.
- `SHA256SUMS.txt.sha256` covers `SHA256SUMS.txt` after it is finalized.
- This avoids a circular checksum problem.
- Manifest lists package filenames only; checksums live in checksum files.
- Package is dry-run only.
- Package is not final release and not activation.
- V35 remains disabled.
- No burn/fork/jackpot action was performed.

## 8. Confirmation no code changed

Confirmed.

No source code was changed.

## 9. Confirmation no tests changed

Confirmed.

No tests were changed.

## 10. Confirmation no build/replay was run

Confirmed.

No server build, wallet build, clean package, tests, or historical replay was run in Phase 8E-2.

## 11. Confirmation V35 remains disabled

Confirmed from unchanged package metadata:

- `V35_RESCUE_FORK_INDEX = Long.MAX_VALUE`
- `V35_BURN_ADDRESS = BURN_ADDRESS`

No V35 constants were changed.

## 12. Confirmation no burn/fork/jackpot action was performed

Confirmed:

- no burn transaction was created
- no fork activation was performed
- no jackpot payout transaction was created
- no final release was claimed

## 13. Confirmation no final tag was created

Confirmed.

No git tag was created.

## 14. Remaining risks

- This remains a dry-run package, not a final release package.
- Package is not signed.
- Final operator package still needs real support/contact information.
- Final activation still needs exact fork index, final tag, final build, final checksums, and explicit release approval.
- Founder burn remains unexecuted and must stay separate from activation.

## 15. Recommended next phase

Recommended next phase:

- Phase 8F - exchange/operator notice finalization, no activation

Alternative:

- Phase 7F - founder burn final review, no burn execution
