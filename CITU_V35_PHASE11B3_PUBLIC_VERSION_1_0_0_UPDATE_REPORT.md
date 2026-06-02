# CITU V35 Phase 11B-3 Public Version 1.0.0 Update Report

## Executive summary

The public Maven/project versions for the V35 node and wallet were normalized to `1.0.0`. Current public website download planning and the Phase 11C-0 future upload commands now use the `1.0.0` artifact names. No release package was replaced in this phase. The Phase 11B-2 package remains stale and must not be uploaded.

## Version update

| Project | Old Maven version | New Maven version |
| --- | --- | --- |
| Server/node | `0.0.423-SNAPSHOT` | `1.0.0` |
| Wallet | `0.32.216-SNAPSHOT` | `1.0.0` |

New required public artifact names:

- `citu-node-v35-activation-514291-1.0.0.jar`
- `citu-wallet-v35-activation-514291-1.0.0.jar`

## Files changed

- `unitedStates_storage-master/pom.xml`
- `unitedStates_final-master/pom.xml`
- `website.citu-web-master/src/main/resources/templates/wallet_and_node_url.html`
- `website.citu-web-master/src/test/java/com/example/website/citu/controller/V35PublicControllerTest.java`
- `CITU_V35_PHASE11C0_GITHUB_RELEASE_UPLOAD_READINESS_REVIEW.md`
- `CITU_V35_PHASE11B3_PUBLIC_VERSION_1_0_0_UPDATE_REPORT.md`

## Website update

The public `/wallet_and_node_url` page now presents future GitHub Release URLs and launch commands for:

- `citu-node-v35-activation-514291-1.0.0.jar`
- `citu-wallet-v35-activation-514291-1.0.0.jar`

The page still labels these names as planned future release filenames. It does not claim that GitHub Release upload has occurred.

## Version endpoint review

The server `/version` endpoint returns `Seting.VERSION`, currently protocol version `39`. This is distinct from the Maven/public JAR version. It was intentionally not changed because this phase must not modify `Seting.java` or protocol behavior.

## Search result

No old `0.0.423`, `0.32.216`, or old V35 public artifact names remain in active server, wallet, or website `src` files and `pom.xml` files.

Old version references remain only in historical material:

- Earlier Phase reports that record builds, smoke runs, checksums, or superseded packaging plans.
- `CITU_PROJECT_MAP.md`, which is explicitly a Phase 0 audit snapshot.
- The ignored `release-dryrun\v35-514291` Phase 11B-2 local package, which must remain untouched in this phase and is now stale.

Historical reports were not rewritten except for `CITU_V35_PHASE11C0_GITHUB_RELEASE_UPLOAD_READINESS_REVIEW.md`, because it contains operational future upload commands. Those planned commands were updated to `1.0.0` and the need for a Phase 11B-4 rebuild was added.

## Verification result

| Project | Test result | Package result | Local JAR produced |
| --- | --- | --- | --- |
| Server/node | `231/231 PASS` | `BUILD SUCCESS` | `target/unitedStates-1.0.0.jar` |
| Wallet | `200/200 PASS` | `BUILD SUCCESS` | `target/unitedStates-1.0.0.jar` |
| Website | `12/12 PASS` | `BUILD SUCCESS` | `target/website.citu-0.0.24-SNAPSHOT.jar` |

The new node and wallet `target/unitedStates-1.0.0.jar` files are local build outputs only. They were not copied into `release-dryrun` and were not uploaded.

## Constants audit

| Project | `V35_RESCUE_FORK_INDEX` | `V35_BURN_ADDRESS` |
| --- | ---: | --- |
| Server | `514291` | `BURN_ADDRESS` |
| Wallet | `514291` | `BURN_ADDRESS` |

## Safety confirmation

- No `Seting.java` file changed.
- `V35_RESCUE_FORK_INDEX` remains `514291`.
- `V35_BURN_ADDRESS` remains `BURN_ADDRESS`.
- No consensus logic changed.
- No mining logic changed.
- No jackpot logic changed.
- No burn transaction was created.
- No jackpot payout was created.
- No deployment was performed.
- No GitHub Release was created.
- No file was uploaded.
- No tag was created.
- No push was performed.
- No commit was created.

## Recommendation

Proceed to Phase 11B-4: rebuild the ignored local release package with the two `1.0.0` public JAR names, recreate start scripts, notes, manifest, and SHA256 files, then repeat local smoke verification before returning to Phase 11C upload readiness.
