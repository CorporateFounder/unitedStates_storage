# CITU V35 Phase 10H-0 Final Release Packaging Plan Report

## 1. Executive summary

Phase 10H-0 defines the final V35 release packaging plan for non-technical users. This phase is report-only. No source code, tests, constants, builds, artifacts, checksums, transactions, deployments, tags, GitHub releases, or commits were changed or created.

The jars currently present in local `target` directories are **not safe to publish as final V35 activation jars**. They were built while production constants remained:

```text
V35_RESCUE_FORK_INDEX = Long.MAX_VALUE
V35_BURN_ADDRESS = BURN_ADDRESS
```

Final public node and wallet jars must be built only after a separately approved activation commit sets the approved finite fork height in both server and wallet `Seting.java` files.

Current planning candidate:

```text
514291
```

The Phase 10G live-height gate found this candidate acceptable for activation commit preparation, subject to one final `/size` refresh immediately before editing constants.

## 2. Safety audit

Repository:

```text
C:\citu
```

Current branch:

```text
master
```

Current HEAD at gate entry:

```text
6764e70 phase 10G add final activation gate live size report
```

`git diff --name-only | findstr /i Seting.java` returned:

```text
NO_SETING_DIFF
```

Production constants remain disabled and aligned:

| Project | Constant | Value |
| --- | --- | --- |
| Server | `V35_RESCUE_FORK_INDEX` | `Long.MAX_VALUE` |
| Wallet | `V35_RESCUE_FORK_INDEX` | `Long.MAX_VALUE` |
| Server | `V35_BURN_ADDRESS` | `BURN_ADDRESS` |
| Wallet | `V35_BURN_ADDRESS` | `BURN_ADDRESS` |

No activation diff exists.

## 3. Project versions and current jar names

| Project | Maven artifact | Project version | Current local jar |
| --- | --- | --- | --- |
| Node/server | `unitedStates` | `0.0.423-SNAPSHOT` | `unitedStates-0.0.423-SNAPSHOT.jar` |
| Wallet | `unitedStates` | `0.32.216-SNAPSHOT` | `unitedStates-0.32.216-SNAPSHOT.jar` |
| Website | `website.citu` | `0.0.24-SNAPSHOT` | `website.citu-0.0.24-SNAPSHOT.jar` |

The current jars are pre-activation local builds. The `.jar.original` Maven repackaging intermediates must not be published.

## 4. Final public artifact naming plan

The final approved fork height must be inserted into release filenames. If `514291` remains approved after the activation-day live-height refresh, use:

### Required node release artifacts

```text
citu-node-v35-activation-514291-0.0.423.jar
start-citu-node-v35-514291.cmd
start-citu-node-v35-514291.sh
```

### Required wallet release artifacts

```text
citu-wallet-v35-activation-514291-0.32.216.jar
start-citu-wallet-v35-514291.cmd
start-citu-wallet-v35-514291.sh
```

### Optional website artifact

Publish only if a separately approved website deployment requires it:

```text
citu-website-v35-notice-514291-0.0.24.jar
```

### Shared documentation and checksum artifacts

```text
CITU_V35_514291_SHA256SUMS.txt
CITU_V35_514291_SHA256SUMS.txt.sha256
CITU_V35_514291_RELEASE_MANIFEST.md
CITU_V35_514291_RELEASE_NOTES_RU.md
CITU_V35_514291_RELEASE_NOTES_EN.md
CITU_V35_514291_START_GUIDE_RU_EN.md
```

If the final approved fork height changes, replace `514291` consistently in every filename, tag, manifest entry, checksum file, notice, and command example.

## 5. Artifact contents

### Node/server package

Required:

- final node jar built after activation commit;
- Windows start script;
- Linux start script;
- RU/EN short node start guide;
- SHA256 checksum entry;
- final release notes;
- final release manifest.

The node package must not contain:

- private keys;
- wallet databases;
- blockchain state;
- local H2 databases;
- logs;
- Maven `target` directories;
- `.jar.original` files;
- dry-run state;
- `release-dryrun` folders.

### Wallet package

Required:

- final wallet jar built after activation commit;
- Windows start script;
- Linux start script;
- RU/EN short wallet start guide;
- SHA256 checksum entry;
- final release notes;
- final release manifest.

The wallet package must not contain:

- user private keys;
- configured miner-account files;
- wallet databases;
- node state;
- logs;
- Maven `target` directories;
- `.jar.original` files;
- dry-run state.

### Optional website package

The website jar is optional. It should be packaged only if the final website notice update is separately approved for deployment. The website artifact must not be mixed into the activation commit.

## 6. Proposed launcher scripts

Create these scripts only after the final approved jars exist.

### Windows node launcher

Filename:

```text
start-citu-node-v35-514291.cmd
```

Proposed content:

```bat
@echo off
java -jar citu-node-v35-activation-514291-0.0.423.jar --server.port=8083
pause
```

### Linux node launcher

Filename:

```text
start-citu-node-v35-514291.sh
```

Proposed content:

```sh
#!/usr/bin/env sh
exec java -jar citu-node-v35-activation-514291-0.0.423.jar --server.port=8083
```

### Windows wallet launcher

Filename:

```text
start-citu-wallet-v35-514291.cmd
```

Proposed content:

```bat
@echo off
java -jar citu-wallet-v35-activation-514291-0.32.216.jar --server.port=8082
pause
```

### Linux wallet launcher

Filename:

```text
start-citu-wallet-v35-514291.sh
```

Proposed content:

```sh
#!/usr/bin/env sh
exec java -jar citu-wallet-v35-activation-514291-0.32.216.jar --server.port=8082
```

## 7. Simple user start commands

Java is required. The final start guide should specify the reviewed Java runtime version used for the final build and smoke test. Users should verify Java before launch:

```powershell
java -version
```

Windows node:

```powershell
java -jar citu-node-v35-activation-514291-0.0.423.jar --server.port=8083
```

Windows wallet:

```powershell
java -jar citu-wallet-v35-activation-514291-0.32.216.jar --server.port=8082
```

Linux node:

```sh
java -jar citu-node-v35-activation-514291-0.0.423.jar --server.port=8083
```

Linux wallet:

```sh
java -jar citu-wallet-v35-activation-514291-0.32.216.jar --server.port=8082
```

Users should download only official tagged artifacts and verify checksums before launch.

## 8. GitHub release structure

Server and wallet releases should be published only after activation commit review, tests, clean packages, artifact renaming, checksum generation, manifest review, and signed/reviewed tag creation.

### Server repository release

Proposed title:

```text
CITU Node V35 Activation at Block 514291
```

Proposed tag:

```text
v35-node-activation-514291
```

Attach:

```text
citu-node-v35-activation-514291-0.0.423.jar
start-citu-node-v35-514291.cmd
start-citu-node-v35-514291.sh
CITU_V35_514291_SHA256SUMS.txt
CITU_V35_514291_SHA256SUMS.txt.sha256
CITU_V35_514291_RELEASE_MANIFEST.md
CITU_V35_514291_RELEASE_NOTES_RU.md
CITU_V35_514291_RELEASE_NOTES_EN.md
CITU_V35_514291_START_GUIDE_RU_EN.md
```

Run command:

```powershell
java -jar citu-node-v35-activation-514291-0.0.423.jar --server.port=8083
```

### Wallet repository release

Proposed title:

```text
CITU Wallet V35 Activation at Block 514291
```

Proposed tag:

```text
v35-wallet-activation-514291
```

Attach:

```text
citu-wallet-v35-activation-514291-0.32.216.jar
start-citu-wallet-v35-514291.cmd
start-citu-wallet-v35-514291.sh
CITU_V35_514291_SHA256SUMS.txt
CITU_V35_514291_SHA256SUMS.txt.sha256
CITU_V35_514291_RELEASE_MANIFEST.md
CITU_V35_514291_RELEASE_NOTES_RU.md
CITU_V35_514291_RELEASE_NOTES_EN.md
CITU_V35_514291_START_GUIDE_RU_EN.md
```

Run command:

```powershell
java -jar citu-wallet-v35-activation-514291-0.32.216.jar --server.port=8082
```

## 9. Draft release warning text

Use this warning in both RU and EN release notes, start guides, manifests, GitHub release bodies, and operator notices.

### EN

```text
V35 activates only at block 514291. Before that block, v35Active=false is expected.

No founder burn has occurred. The official public burn address has not been published.
Do not send funds to BURN_ADDRESS. BURN_ADDRESS is an internal placeholder, not a public transfer destination.

Use only official tagged artifacts and verify SHA256 checksums before launch.
Old APIs remain backward-compatible.
Wallet private-key URL endpoints are legacy/local-only and must not be used for exchange integrations.
```

### RU

```text
V35 активируется только на блоке 514291. До этого блока значение v35Active=false является ожидаемым.

Сжигание средств основателя не выполнялось. Официальный публичный адрес сжигания пока не опубликован.
Не отправляйте средства на BURN_ADDRESS. BURN_ADDRESS является внутренним заполнителем протокола, а не публичным адресом для переводов.

Используйте только официальные артефакты с тегом релиза и проверяйте контрольные суммы SHA256 перед запуском.
Старые API остаются обратно совместимыми.
URL-эндпоинты кошелька с приватным ключом являются устаревшими локальными интерфейсами и не должны использоваться интеграциями бирж.
```

## 10. Required sequence before final jars are built

The following steps are future work. Do not execute them in this phase.

1. Read live `http://194.87.236.238:82/size` again immediately before editing.
2. Recalculate the remaining buffer to candidate `514291`.
3. Reject or refresh the candidate if the approved minimum buffer is no longer satisfied.
4. Obtain explicit human approval for the final fork height.
5. Create a dedicated activation branch from clean master.
6. Edit exactly:

```text
unitedStates_storage-master/src/main/java/International_Trade_Union/setings/Seting.java
unitedStates_final-master/src/main/java/International_Trade_Union/setings/Seting.java
```

7. Change only:

```text
V35_RESCUE_FORK_INDEX = Long.MAX_VALUE
```

to:

```text
V35_RESCUE_FORK_INDEX = FINAL_APPROVED_HEIGHT
```

8. Keep:

```text
V35_BURN_ADDRESS = BURN_ADDRESS
```

9. Do not create founder burn or jackpot payout transactions.
10. Run server and wallet tests.
11. Run server and wallet clean packages.
12. Rename only the reviewed final jars using the approved height.
13. Create launcher scripts and RU/EN documentation.
14. Generate SHA256 checksums and checksum-of-checksum.
15. Create and review the final release manifest.
16. Create reviewed tags only after artifact review.
17. Publish GitHub releases only after explicit release approval.
18. Deploy website notices only after separate deployment approval.

## 11. Release blockers

- Final `/size` refresh has not yet occurred.
- Final fork height has not yet been explicitly approved for editing.
- Activation commit has not been created.
- Post-activation-commit tests and packages have not been run.
- Final artifacts, launch scripts, checksums, manifest, tags, and GitHub releases do not exist.
- Founder burn remains separate and unexecuted.

## 12. Explicit safety confirmation

- No source code changed in Phase 10H-0.
- No tests changed or ran in Phase 10H-0.
- No `Seting.java` changed.
- `V35_RESCUE_FORK_INDEX` remains `Long.MAX_VALUE` in server and wallet.
- `V35_BURN_ADDRESS` remains `BURN_ADDRESS` in server and wallet.
- No V35 activation occurred.
- No founder burn transaction was created, signed, simulated, or broadcast.
- No jackpot payout transaction was created.
- No deployment occurred.
- No tag was created.
- No GitHub release was created.
- No final release artifact was created.
- No final checksum was created.
- No commit was created.

## 13. Recommended next phase

Proceed only with a separately approved **real activation commit preparation** phase:

1. refresh live `/size`;
2. confirm candidate `514291` still satisfies the approved buffer;
3. edit exactly the server and wallet `Seting.java` fork-height constants;
4. set `V35_RESCUE_FORK_INDEX = 514291` only if still approved;
5. keep `V35_BURN_ADDRESS = BURN_ADDRESS`;
6. build and verify final jars after the activation commit;
7. generate final SHA256 checksums and RU/EN release docs;
8. do not burn, do not create jackpot payout, and do not deploy unless separately approved.
