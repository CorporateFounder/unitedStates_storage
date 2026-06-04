# CITU V35 Phase 11F-0 Official Deployment Readiness Audit Report

## 1. Current local source status

Local project root:

- `C:\citu`

Current local HEAD:

- `fffcedb phase 11E refresh final V35 release assets after UI cleanup`

Local source status:

- `git status --short` returned clean.
- `git diff --name-only | Select-String -Pattern 'Seting.java'` returned no matches.
- No local source changes were made in this phase.

Current release state:

- Node release was refreshed after UI cleanup.
- Wallet release was refreshed after UI cleanup.
- Public SHA256 verification passed.
- Node SHA256: `c692264b34b1a32e54f7a7acac4b518ae69fc561fc51de7cef9c2f67b803d501`
- Wallet SHA256: `7503ec85a903f7a5d5994804c8ac29880e1a54580aa612bd5c6bb69b69d07a52`

V35 constants and safety state:

- `V35_RESCUE_FORK_INDEX = 514291`
- `V35_BURN_ADDRESS = BURN_ADDRESS`
- No founder burn has occurred.
- No jackpot payout has been created.

## 2. Current VPS process inventory

Manual VPS inventory supplied for this audit:

| Role | PID | Command / evidence | Listening ports | Assessment |
|---|---:|---|---|---|
| Official website | `4056` | `java -Dserver.port=80 -Dserver.address=194.87.236.238 -jar C:\website.citu-0.0.30-SNAPSHOT.jar` | `0.0.0.0:80`, `[::]:80`, `194.87.236.238:443` | Real live website process. Do not kill in this phase. |
| Extra website-looking process | `4032` | `java -jar C:\website.citu-0.0.30-SNAPSHOT.jar` | None found by `netstat findstr 4032` | Role unclear. Do not kill in this phase. |
| Official public node | `3592` | `java -Dserver.port=82 -Dserver.address=194.87.236.238 -jar C:\unitedStates-0.423-SNAPSHOT.jar` | Port `82` | Real public node. Do not restart in this phase. |
| Old wallet/local process | `1700` | `java -jar C:\unitedStates-0.32.216-SNAPSHOT.jar` | Port `8082` | Old local wallet process. Do not kill in this phase. |
| Port 8083 | n/a | No listener | None | Free at inventory time. |

## 3. Current live website inventory

The live website appears to be served by PID `4056`.

Evidence:

- Command includes `-Dserver.port=80`.
- Command includes `-Dserver.address=194.87.236.238`.
- Command uses `C:\website.citu-0.0.30-SNAPSHOT.jar`.
- Netstat shows PID `4056` listening on:
  - `0.0.0.0:80`
  - `[::]:80`
  - `194.87.236.238:443`

Important observation:

- PID `4056` appears to serve both HTTP `80` and HTTPS `443`.
- Any website deployment plan must treat PID `4056` as the live public website process and avoid casual termination.

Extra process:

- PID `4032` also runs `C:\website.citu-0.0.30-SNAPSHOT.jar`.
- No listening port was found for PID `4032`.
- Its role is unclear and must be investigated before cleanup.

## 4. Current live node inventory

The live public node appears to be PID `3592`.

Evidence:

- Command includes `-Dserver.port=82`.
- Command includes `-Dserver.address=194.87.236.238`.
- Command uses `C:\unitedStates-0.423-SNAPSHOT.jar`.
- Port `82` is listening on PID `3592`.

Important observation:

- The node is currently running an older jar: `C:\unitedStates-0.423-SNAPSHOT.jar`.
- The V35 release jar exists in GitHub Releases, but node restart/deployment must be a separate controlled phase.
- Node live data must be backed up before any restart.

## 5. Backup requirements before website deploy

Before any website-only deployment, preserve enough state to roll back quickly:

- Copy current live website jar:
  - `C:\website.citu-0.0.30-SNAPSHOT.jar`
- Record SHA256 of the current live website jar.
- Record PID and command line for PID `4056`.
- Record PID and command line for PID `4032`.
- Record port listeners for PIDs `4056` and `4032`.
- Export or save current website startup command.
- Preserve any website resource folder used by the current jar:
  - `C:\citucorp_com`, if active for website resources.
- Save IIS/reverse-proxy/certificate configuration if any exists outside the Java process.
- Capture a pre-deploy screenshot or HTTP response evidence for:
  - `/`
  - `/v35`
  - `/wallet_and_node_url`
  - `/white_papper`

Do not deploy until PID `4032` is explained or explicitly accepted as harmless.

## 6. Backup requirements before node restart

Before any node restart, back up live chain/resource state.

Critical folder:

- `C:\resources`

Manual inventory says it contains:

- `blockchain`
- `balance`
- `H2DB`
- `transactions`
- `tempBlockchain`
- `temporaryBlocks`
- `minerAccount`
- `poolAddress`
- `poolAddressAccount`
- `pools`
- `senderTransaction`
- `server`
- `share`
- `sliding`
- `vote`
- `federalLaws`
- `federalGovernment`
- `allLawsWithBalance`
- `allclassicLaws`
- `budgetEmission`
- `error`
- `notAdrressBlocked`
- `balanceReportOnDestroedCoins`

Additional folders to classify before node restart:

- `C:\server`
- `C:\citucorp_com`

Before node restart:

- Create a timestamped backup of `C:\resources`.
- Verify backup size and file count.
- Save SHA256 or archive checksum for the backup.
- Record current live `/size`.
- Record current live `/prevBlock` if available.
- Record current process command for PID `3592`.
- Record current live jar SHA256:
  - `C:\unitedStates-0.423-SNAPSHOT.jar`
- Confirm the target V35 node jar SHA256:
  - `c692264b34b1a32e54f7a7acac4b518ae69fc561fc51de7cef9c2f67b803d501`

## 7. Website-only deployment plan

Recommended next deployment phase is website-only first.

Proposed safe outline for Phase 11F-1:

1. Confirm no node restart is planned.
2. Confirm no burn or jackpot action is planned.
3. Confirm current live website PID and ports:
   - PID `4056`
   - ports `80` and `443`
4. Investigate PID `4032`:
   - command line
   - parent process
   - open files if practical
   - whether it is a stale process, helper, or duplicate instance
5. Back up current website jar and relevant website resources.
6. Copy new website jar to a versioned path.
7. Start replacement website on a temporary local-only port first if possible.
8. Smoke test:
   - `/`
   - `/v35`
   - `/wallet_and_node_url`
   - `/how_to_install`
   - `/how_mining_pool`
   - `/white_papper`
9. Switch live website only after smoke approval.
10. Verify public HTTP/HTTPS responses.
11. Keep old jar and exact rollback command available.

No node process should be touched during website-only deployment.

## 8. Node controlled restart plan

Recommended later phase is Phase 11G controlled node restart.

Proposed outline:

1. Announce node maintenance window.
2. Confirm V35 candidate and live height remain appropriate.
3. Confirm current live node PID:
   - PID `3592`
   - port `82`
4. Back up `C:\resources` completely.
5. Verify backup integrity.
6. Record live `/size` and `/prevBlock`.
7. Verify current and target jar SHA256.
8. Start the V35 node in a controlled manner using the existing live resources only after backup approval.
9. Verify:
   - `/size`
   - `/prevBlock`
   - `/v35/protocol`
   - `/v35/burn`
   - `/v35/jackpot/status`
10. Confirm `v35Active=false` before block `514291` if the fork height has not been reached.
11. Monitor logs and block progression.
12. Keep rollback jar and backup ready.

Node restart must not include any founder burn or jackpot payout action.

## 9. Rollback plan

Website rollback:

- Stop only the newly deployed website process.
- Restore the previous website jar:
  - `C:\website.citu-0.0.30-SNAPSHOT.jar`
- Restart using the captured previous PID `4056` command line pattern.
- Verify ports `80` and `443`.
- Verify public pages return expected responses.

Node rollback:

- Blockchain rollback is sensitive and must not be improvised.
- If the new node fails before accepting new chain state:
  - stop only the new node process,
  - restart old jar `C:\unitedStates-0.423-SNAPSHOT.jar`,
  - use the preserved `C:\resources` state.
- If chain state changes after restart:
  - stop,
  - preserve logs and current state,
  - do not overwrite data until root cause review,
  - compare backup and live state,
  - decide rollback only with explicit approval.

Communication rollback:

- Notify operators/users if public instructions or node availability are affected.
- Do not claim burn execution.
- Do not claim jackpot payout.

## 10. Blockers and open issues

Current blockers before deployment/restart:

1. PID `4032` exists but has no listening port.
   - Its role is unclear.
   - It should not be killed in this phase.
   - It should be investigated before website deployment.

2. PID `4056` appears to serve both `80` and `443`.
   - Website deployment may affect both HTTP and HTTPS.
   - A precise restart/switch procedure is required.

3. Node data in `C:\resources` must be backed up before node restart.
   - This is a hard blocker for Phase 11G.

4. Old jars remain in `C:\`.
   - They should be preserved for rollback but clearly distinguished from the final V35 release jars.

5. Port `8083` has no listener.
   - This is not a blocker by itself, but any planned local node test on `8083` must account for it.

## 11. Recommended next phase

Recommended next phase:

- Phase 11F-1 website-only deployment first.

Recommended later phase:

- Phase 11G controlled node restart after full `C:\resources` backup and explicit approval.

Rationale:

- Website deployment is lower risk than node restart.
- Public website can be updated without touching live chain data.
- Node restart involves live chain/resource state and should be isolated into its own controlled maintenance phase.

## 12. Safety confirmation

Confirmed for Phase 11F-0:

- No deployment.
- No restart.
- No process killed.
- No `Seting.java` change.
- No consensus change.
- No burn transaction.
- No jackpot payout.
- No source push.
- No tag.
- No release asset upload.
- No commit.
- No `git add`.
