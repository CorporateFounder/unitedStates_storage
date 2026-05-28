# CITU V35 Phase 6F-2 Final Activation Commit Checklist

## 1. Executive summary

This is a checklist only.

V35 is not activated in this phase. No source code, tests, constants, release tags, build artifacts, fork index, founder burn transaction, or jackpot payout transaction are changed or created here.

Final activation requires separate explicit approval after artifact build, checksum, exchange/operator coordination, burn timing decision, and release owner go/no-go.

Current confirmed constants:

- Server `V35_RESCUE_FORK_INDEX = Long.MAX_VALUE`
- Wallet `V35_RESCUE_FORK_INDEX = Long.MAX_VALUE`
- Server/wallet `V35_BURN_ADDRESS = BURN_ADDRESS`

## 2. Preconditions before activation commit

Do not create the real activation commit until every required gate below is complete or explicitly accepted by the release owner.

- [ ] Clean git working tree.
- [ ] No generated `target/` files staged.
- [ ] Latest server historical replay: `PASS`, reached the end.
- [ ] Latest wallet historical replay: `PASS`, reached the end.
- [ ] Historical replay gate: `PASS`.
- [ ] Wallet mirror gate: `PASS`.
- [ ] Replay-only safety gate: `PASS`.
- [ ] Release notes completed.
- [ ] Artifact checklist completed.
- [ ] Founder burn execution checklist reviewed.
- [ ] Founder burn timing decision made.
- [ ] Exact V35 activation height selected.
- [ ] Activation height reviewed by at least two humans.
- [ ] Exchange/node operator communication ready.
- [ ] Rollback/emergency plan ready.
- [ ] Post-activation monitoring plan ready.
- [ ] Release artifacts and checksums plan ready.
- [ ] Support/contact channel ready.

## 3. Files that will be changed during real activation commit

Expected activation commit files only:

- `unitedStates_storage-master/src/main/java/International_Trade_Union/setings/Seting.java`
- `unitedStates_final-master/src/main/java/International_Trade_Union/setings/Seting.java`

Possible documentation files, if final release notes are updated in the same approved release branch:

- final activation release notes
- exchange/node activation notice
- checksum/artifact manifest

Do not include unrelated files.

Do not change mining, validation, balances, transaction format, block format, jackpot policy, reward economics, wallet dashboard logic, website pages, or `pom.xml` as part of the activation commit.

## 4. Constants to change during real activation commit

Only at activation time, after approval:

- Change wallet/server `V35_RESCUE_FORK_INDEX` from `Long.MAX_VALUE` to the approved fork height.
- Keep wallet and server fork index values exactly identical.
- Keep `V35_BURN_ADDRESS = BURN_ADDRESS` unless final approved policy explicitly requires replacing it.
- If `V35_BURN_ADDRESS` changes by approved final policy, it must change identically in wallet and server and must be documented separately.

All other V35 economics must remain already defined:

- target block time: `200 seconds`
- expected blocks/day: `432`
- total reward: `8 GOLD/block`
- miner reward: `6 GOLD`
- jackpot reserve: `2 GOLD`
- founder reward after V35: `0 GOLD`
- annual emission: `1,261,440 GOLD`

Activation commit rule:

```text
Change the fork index only. If anything else appears in the diff, stop and review.
```

## 5. Exact command checklist for future activation commit

Do not run these commands now.

### Pre-edit status

```powershell
cd C:\citu
git status --short
```

Expected:

```text
clean working tree, or only explicitly approved release documentation changes
```

### Edit constants

Do not edit now. During the approved activation phase only:

```text
Edit server Seting.java:
unitedStates_storage-master/src/main/java/International_Trade_Union/setings/Seting.java

Edit wallet Seting.java:
unitedStates_final-master/src/main/java/International_Trade_Union/setings/Seting.java

Change:
V35_RESCUE_FORK_INDEX = Long.MAX_VALUE

To:
V35_RESCUE_FORK_INDEX = <APPROVED_FORK_HEIGHT>
```

### Verify constants

```powershell
Select-String -Path C:\citu\unitedStates_storage-master\src\main\java\International_Trade_Union\setings\Seting.java -Pattern 'V35_RESCUE_FORK_INDEX|V35_BURN_ADDRESS'
Select-String -Path C:\citu\unitedStates_final-master\src\main\java\International_Trade_Union\setings\Seting.java -Pattern 'V35_RESCUE_FORK_INDEX|V35_BURN_ADDRESS'
```

Expected:

- server fork index equals approved fork height
- wallet fork index equals approved fork height
- server/wallet burn address match approved final burn address

### Verify diff

```powershell
git diff -- unitedStates_storage-master/src/main/java/International_Trade_Union/setings/Seting.java unitedStates_final-master/src/main/java/International_Trade_Union/setings/Seting.java
```

Expected:

- only `V35_RESCUE_FORK_INDEX` changed, unless final burn-address policy also explicitly approved a burn-address change
- no reward, PoW, jackpot, block, transaction, mining, balance, or validation logic changed

### Run server tests

```powershell
cd C:\citu\unitedStates_storage-master
.\mvnw.cmd test
```

### Run wallet tests

```powershell
cd C:\citu\unitedStates_final-master
.\mvnw.cmd test
```

### Run server clean package

```powershell
cd C:\citu\unitedStates_storage-master
.\mvnw.cmd clean package
```

### Run wallet clean package

```powershell
cd C:\citu\unitedStates_final-master
.\mvnw.cmd clean package
```

### Generate checksums after artifacts are built

Use actual release artifact paths. Do not invent hashes.

```powershell
Get-FileHash "C:\citu-release\v35-fork-<INDEX>\citu-server-v35-fork-<INDEX>.jar" -Algorithm SHA256
Get-FileHash "C:\citu-release\v35-fork-<INDEX>\citu-wallet-v35-fork-<INDEX>.jar" -Algorithm SHA256
```

### Create pre-final git commit

Do not commit now. During approved activation only:

```powershell
cd C:\citu
git status --short
git add unitedStates_storage-master/src/main/java/International_Trade_Union/setings/Seting.java unitedStates_final-master/src/main/java/International_Trade_Union/setings/Seting.java
git commit -m "Activate V35 at fork index <INDEX>"
```

### Create reviewed tag after approval

Do not tag now. During approved final release only:

```powershell
git tag v35-activation-fork-<INDEX>
```

Use signed or reviewed tag flow if project release policy requires it.

## 6. Founder burn separation

Founder burn is not part of this checklist execution.

Rules:

- Founder burn must be executed only through `CITU_V35_PHASE7E_FINAL_BURN_EXECUTION_CHECKLIST.md`.
- Activation commit must not create, sign, broadcast, or simulate the burn transaction.
- Burn transaction must not be created automatically by code, tests, release scripts, or activation commit.
- Burn execution must be separately verified, archived, and announced.
- Burn proof must include snapshot height/hash, founder balance, burn amount, burn tx hash, burn block index/hash, and before/after supply.

If founder burn is not yet complete when activation is planned, release owner must explicitly decide whether to delay activation or proceed with burn pending. Do not silently proceed.

## 7. Jackpot separation

Jackpot reserve logic exists, but jackpot payout creation is not part of the activation commit.

Rules:

- Do not manually create a jackpot payout transaction during activation.
- Do not add jackpot payout fixtures or mutate chain data.
- Jackpot payout generation must remain governed by normal V35 mining/proof logic after activation.
- Jackpot status/history APIs remain read-only.
- Any jackpot payout issue must be handled in a separate focused phase.

## 8. Release gate table

| Gate | Status |
| --- | --- |
| Historical replay gate | PASS |
| Wallet mirror gate | PASS |
| Replay-only safety gate | PASS |
| Release notes gate | PASS |
| Artifact checklist gate | PASS |
| Artifact build gate | NOT YET |
| Checksum gate | NOT YET |
| Exchange/operator notice gate | NOT YET |
| V35 activation gate | NOT YET |
| Founder burn gate | NOT YET |
| Final release gate | NOT YET |

## 9. Risks

Highest activation-commit risks:

- Wrong activation height.
- Server/wallet fork index mismatch.
- Accidental edit outside `Seting.java`.
- Accidental reward, PoW, jackpot, burn, transaction, block, mining, validation, or balance change.
- Accidental founder burn transaction creation.
- Accidental manual jackpot payout transaction creation.
- Unsigned or unverified artifacts.
- Exchanges/nodes running the wrong build.
- Release candidate mistaken for final activation build.
- Replay-only compatibility accidentally modified after sign-off.
- Checksums generated before final artifacts are stable.
- Insufficient fork-day monitoring.
- Poor rollback communication if activation has problems.

## 10. Recommended next phase

Recommended next phase:

- Phase 8D - artifact build/checksum dry-run, no activation

Alternative:

- Phase 7F - founder burn final review, no burn execution

Choose Phase 8D if release packaging is the next priority. Choose Phase 7F if burn timing and proof readiness need final human review before artifact staging.

## 11. Phase 6F-2 confirmation

No code changed.

No tests changed.

No full tests were run.

No clean package was run.

No artifacts were built.

No git commit or tag was created.

No V35 activation was performed.

No founder burn transaction was created.

No jackpot payout transaction was created.
