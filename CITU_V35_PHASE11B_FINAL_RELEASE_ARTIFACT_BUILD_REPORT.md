# CITU V35 Phase 11B Final Local Release Artifact Build Report

## 1. Executive Summary

Phase 11B built and packaged the final local V35 activation artifacts from committed activation code.

This phase prepared local files only. Nothing was uploaded, tagged, pushed, deployed, or published.

## 2. Activation Commit

- branch: `master`
- activation commit: `c755c153c1cd933c6c8a11ce09080578f35d5b98`
- short commit: `c755c15 activate V35 at fork height 514291`
- working tree before build: clean

## 3. Constants Verified

| Project | `V35_RESCUE_FORK_INDEX` | `V35_BURN_ADDRESS` |
| --- | ---: | --- |
| Server | `514291` | `BURN_ADDRESS` |
| Wallet | `514291` | `BURN_ADDRESS` |

No `Seting.java` file was changed in Phase 11B.

## 4. Build Results

### Server

Command:

```powershell
cd C:\citu\unitedStates_storage-master
.\mvnw.cmd clean package
```

Result:

- tests: `231/231 PASS`
- package: `BUILD SUCCESS`
- source JAR: `target/unitedStates-0.0.423-SNAPSHOT.jar`

### Wallet

Command:

```powershell
cd C:\citu\unitedStates_final-master
.\mvnw.cmd clean package
```

Result:

- tests: `200/200 PASS`
- package: `BUILD SUCCESS`
- source JAR: `target/unitedStates-0.32.216-SNAPSHOT.jar`

### Build Runtime

- Java: `19.0.2`
- Maven: `3.8.6`
- build timestamp: `2026-06-02 19:11:15 +05:00`

## 5. Local Artifact Folder

```text
C:\citu\release-dryrun\v35-514291
```

`release-dryrun/` is locally excluded from Git tracking.

## 6. Artifact List

| File | Size bytes |
| --- | ---: |
| `citu-node-v35-activation-514291-0.0.423.jar` | `53111403` |
| `citu-wallet-v35-activation-514291-0.32.216.jar` | `58153658` |
| `start-node-windows.bat` | `83` |
| `start-wallet-windows.bat` | `86` |
| `start-node-linux.sh` | `93` |
| `start-wallet-linux.sh` | `96` |
| `CITU_V35_514291_START_GUIDE_RU_EN.md` | `1962` |
| `CITU_V35_514291_RELEASE_NOTES_RU.md` | `1511` |
| `CITU_V35_514291_RELEASE_NOTES_EN.md` | `1116` |
| `CITU_V35_514291_RELEASE_MANIFEST.txt` | `1037` |
| `CITU_V35_514291_SHA256SUMS.txt` | `997` |
| `CITU_V35_514291_SHA256SUMS.txt.sha256` | `98` |

## 7. SHA256 Verification

Created:

- `CITU_V35_514291_SHA256SUMS.txt`
- `CITU_V35_514291_SHA256SUMS.txt.sha256`

Verification result:

- ten listed package files: `PASS`
- checksum-of-checksum: `PASS`

Runnable JAR SHA256:

```text
ca6cf64a6b2db0091e6f249bb42c2f2603ca812fad0ab2a4417d389955fea0a0  citu-node-v35-activation-514291-0.0.423.jar
14d798cdb9805e75696dad46f9f3d2f2323e3c3eccbc1609171fc403fe085282  citu-wallet-v35-activation-514291-0.32.216.jar
```

Checksum-list SHA256:

```text
1c844d0a5a201d72b75ecdcf7de3db7d79b1348ef1b637fd90b24ad092e82065  CITU_V35_514291_SHA256SUMS.txt
```

## 8. Optional Local Offline Smoke

The packaged node JAR was started locally with:

```powershell
java -jar citu-node-v35-activation-514291-0.0.423.jar --spring.profiles.active=smoke-offline --server.address=127.0.0.1 --server.port=8093
```

Results:

| Endpoint | Result |
| --- | --- |
| `/size` | `HTTP 200` |
| `/v35/protocol` | `HTTP 200` |
| `/v35/burn` | `HTTP 200` |
| `/v35/jackpot/status` | `HTTP 200` |

Observed safety state:

- `v35Active=false`
- `v35ForkIndex=514291`
- `burnAddress=BURN_ADDRESS`
- `isBurnAddressPlaceholder=true`
- `burnAddressConfigured=false`
- `publicBurnAddressPublished=false`
- `burnTransactionCreated=false`
- jackpot periods: `DISABLED`
- offline smoke network services: disabled

The local smoke process was stopped after verification.

## 9. Safety Confirmation

- no `Seting.java` change
- no production source-code change
- no burn transaction created
- no jackpot payout transaction created
- no deployment
- no GitHub Release
- no upload
- no tag
- no push
- no commit
- no release-dryrun artifacts added to Git tracking

## 10. Recommendation

The local artifact folder is ready for Phase 11C release upload review. Phase 11C must explicitly review the artifact list, checksums, tag plan, and publication text before any upload or GitHub Release action.

