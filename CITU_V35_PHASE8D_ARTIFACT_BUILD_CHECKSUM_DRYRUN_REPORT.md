# CITU V35 Phase 8D Artifact Build and Checksum Dry-Run Report

## 1. Executive summary

Phase 8D performed a pre-activation artifact build and checksum dry-run.

This is not a final release and not V35 activation.

Completed:

- Pre-flight git status was clean before starting.
- Server clean package passed.
- Wallet clean package passed.
- Dry-run release folder was created.
- Selected server/wallet artifacts and public release docs were copied.
- `SHA256SUMS.txt` was generated.
- `RELEASE_DRYRUN_MANIFEST.md` was generated.

Not performed:

- no source code changes
- no test changes
- no V35 constant changes
- no fork activation
- no founder burn transaction
- no jackpot payout transaction
- no final git tag

## 2. Git commit hash used

Build commit:

```text
aa466242208848e25adc35c9dcf68fcb9bb47964
```

## 3. Pre-flight git tree status

Pre-flight command:

```powershell
cd C:\citu
git status --short
```

Result:

```text
clean
```

Because the working tree was clean, the dry-run proceeded.

## 4. Server build result

Command:

```powershell
cd C:\citu\unitedStates_storage-master
.\mvnw.cmd clean package
```

Result:

- `BUILD SUCCESS`
- Tests run: `225`
- Failures: `0`
- Errors: `0`
- Skipped: `0`

Built server artifact:

| Artifact | Path | Bytes |
| --- | --- | ---: |
| `unitedStates-0.0.423-SNAPSHOT.jar` | `C:\citu\unitedStates_storage-master\target\unitedStates-0.0.423-SNAPSHOT.jar` | `53109985` |

Known non-fatal log noise remains:

- `FileNotFoundException` for `\server\resources\error\error.txt` may print during logging paths, but the server build exited successfully.

## 5. Wallet build result

Command:

```powershell
cd C:\citu\unitedStates_final-master
.\mvnw.cmd clean package
```

Result:

- `BUILD SUCCESS`
- Tests run: `200`
- Failures: `0`
- Errors: `0`
- Skipped: `0`

Built wallet artifact:

| Artifact | Path | Bytes |
| --- | --- | ---: |
| `unitedStates-0.32.216-SNAPSHOT.jar` | `C:\citu\unitedStates_final-master\target\unitedStates-0.32.216-SNAPSHOT.jar` | `58102597` |

Known non-fatal log noise remains:

- log4j warns that no appenders are configured for wallet test logging.

## 6. Dry-run folder path

Dry-run release folder:

```text
C:\citu\release-dryrun\v35-preactivation-dryrun-20260529-2102
```

Only selected artifacts and public release docs were copied. No private keys, wallet databases, blockchain resource data, local node state, or full `target/` directories were copied.

## 7. Artifact list

Files in the dry-run folder:

| File | Bytes |
| --- | ---: |
| `CITU_V35_PHASE6F2_FINAL_ACTIVATION_COMMIT_CHECKLIST.md` | `9303` |
| `CITU_V35_PHASE8C_ARTIFACT_CHECKLIST.md` | `6807` |
| `CITU_V35_PHASE8C_RELEASE_NOTES_FINAL.md` | `4760` |
| `CITU_V35_PHASE9C_HISTORICAL_REPLAY_SIGNOFF_RELEASE_GATE_REPORT.md` | `7520` |
| `CITU_V35_PHASE9E_FINAL_PRE_ACTIVATION_SAFETY_AUDIT_REPORT.md` | `8190` |
| `RELEASE_DRYRUN_MANIFEST.md` | `1694` |
| `SHA256SUMS.txt` | `799` |
| `unitedStates-0.0.423-SNAPSHOT.jar` | `53109985` |
| `unitedStates-0.32.216-SNAPSHOT.jar` | `58102597` |

## 8. SHA256 checksum summary

`SHA256SUMS.txt` was created at:

```text
C:\citu\release-dryrun\v35-preactivation-dryrun-20260529-2102\SHA256SUMS.txt
```

Checksums for copied files:

```text
0081DD4876C621C5BF49E20FCDC6CA5EF0722BE39BA7F5C899C6EF0B20B00E5B  CITU_V35_PHASE6F2_FINAL_ACTIVATION_COMMIT_CHECKLIST.md
1D270640DCC8865FDA61F617EA9D5A0E821651564BB95F51A0A64B5F8188A05D  CITU_V35_PHASE8C_ARTIFACT_CHECKLIST.md
737A8E3B7E974B8B4556E19E72075DDAB9C416182A4B95BEAD1DD403E87BE5AD  CITU_V35_PHASE8C_RELEASE_NOTES_FINAL.md
A575874FB2FD90DC77CCBC1D1E560334DD6EE40742E90E97078664F45C2854E3  CITU_V35_PHASE9C_HISTORICAL_REPLAY_SIGNOFF_RELEASE_GATE_REPORT.md
7AD1F10E62D367682B50C6F05931F703E65F5EDC15596C7BCD7586BD9D284384  CITU_V35_PHASE9E_FINAL_PRE_ACTIVATION_SAFETY_AUDIT_REPORT.md
8434948BE10A3EE8B978753280E5E8A1704C1EF29583EC5621E07D30934B32DB  unitedStates-0.0.423-SNAPSHOT.jar
A349BCFAE3C8CD6FBA486010F2C46B589A8875D628F8B3F9F10F03C43F0ABC6A  unitedStates-0.32.216-SNAPSHOT.jar
```

Additional generated manifest checksum:

```text
CDAE736CDA94C670C1252CB3D3AA5D484A27F6C959E586EF550F8FB8A8ECDD00  RELEASE_DRYRUN_MANIFEST.md
```

## 9. V35 disabled confirmation

Confirmed before build:

- Server `V35_RESCUE_FORK_INDEX = Long.MAX_VALUE`
- Wallet `V35_RESCUE_FORK_INDEX = Long.MAX_VALUE`
- Server `V35_BURN_ADDRESS = BURN_ADDRESS`
- Wallet `V35_BURN_ADDRESS = BURN_ADDRESS`

No V35 constant was changed during this phase.

## 10. Burn/fork/jackpot confirmation

Confirmed:

- no burn transaction was created
- no fork activation was performed
- no jackpot payout transaction was created
- no final git tag was created
- no release was declared final

## 11. Final git status summary

Maven-generated tracked `target/` changes were restored after copying selected artifacts into the dry-run folder.

Expected final untracked/changed paths:

```text
CITU_V35_PHASE8D_ARTIFACT_BUILD_CHECKSUM_DRYRUN_REPORT.md
release-dryrun/
```

## 12. Remaining risks

- This is only a dry-run package; it is not signed, tagged, or final.
- Website artifact was not built or included in this dry-run because the requested Phase 8D scope specified server and wallet builds.
- Maven duplicate dependency warnings remain in server and wallet builds.
- Server logging still emits non-fatal `error.txt` path noise.
- Final activation still requires exact fork height selection, exchange/operator coordination, final artifact build, final checksums, and explicit release owner approval.
- Founder burn remains unexecuted and must stay separate from activation.

## 13. Recommended next phase

Recommended next phase:

- Phase 8E - exchange/operator dry-run package review, no activation

Alternative:

- Phase 7F - founder burn final review, no burn execution
