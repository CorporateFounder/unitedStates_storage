# CITU V35 Phase 10F-3 Isolated Local Activation and Mining Dry-Run Report

## 1. Executive summary

Phase 10F-3 completed an isolated local V35 activation and mining dry-run without touching the main `C:\citu` working tree. A detached worktree was created at `C:\citu_v35_local_dryrun` from master commit `11d577b5603a732f62d2669c18e9dd3444498f6c`.

Mode A was completed using an isolated worktree-local empty chain, isolated H2 database, offline smoke profile, localhost-only server, read-only endpoint smoke checks, and focused in-memory block mining across a test-only V35 fork boundary. Three sequential V35 blocks were mined and validated after the local fork. No crash occurred.

Mode B was skipped because copying and binding the full validated historical chain was unnecessary for this immediate post-fork safety question and would add avoidable state-handling risk.

## 2. Exact worktree path

```text
C:\citu_v35_local_dryrun
```

The worktree is detached at:

```text
11d577b5603a732f62d2669c18e9dd3444498f6c
```

## 3. Main repository was not modified

Before the worktree was created, `C:\citu` was clean on `master`:

```text
11d577b phase 10F1J polish API compatibility and wallet UI
```

All temporary activation edits, generated local state, builds, tests, and this report were kept inside `C:\citu_v35_local_dryrun`.

## 4. Temporary test-only constants used

Temporary worktree-only overrides:

| Project | Constant | Test-only value |
| --- | --- | --- |
| Server | `V35_RESCUE_FORK_INDEX` | `1L` |
| Wallet | `V35_RESCUE_FORK_INDEX` | `1L` |
| Server | `V35_BURN_ADDRESS` | `BURN_ADDRESS` |
| Wallet | `V35_BURN_ADDRESS` | `BURN_ADDRESS` |

The fork-height overrides are marked `TEST-ONLY isolated local dry-run; never copy to master.` They must not be copied back to `C:\citu`.

## 5. Offline/local startup commands

The server resource root is fixed as `/server/resources`. To keep that path isolated, a temporary `V:` drive was mapped to the worktree before startup. This caused `/server/resources` to resolve inside `C:\citu_v35_local_dryrun\server\resources`.

```powershell
subst V: C:\citu_v35_local_dryrun
cd V:\unitedStates_storage-master
java -jar target\unitedStates-0.0.423-SNAPSHOT.jar `
  --server.address=127.0.0.1 `
  --server.port=8093 `
  --spring.profiles.active=smoke-offline `
  "--spring.datasource.url=jdbc:h2:file:./target/smoke/server-db;DB_CLOSE_ON_EXIT=FALSE;AUTO_RECONNECT=TRUE"
```

After smoke checks, the process was stopped, the `V:` mapping was removed, and generated worktree-local `server\` state was deleted.

## 6. Mode A result

**Mode A: RUN / PASS**

Runtime empty-chain bootstrap:

| Check | Result |
| --- | --- |
| Isolated resource tree | PASS: worktree-local `server\resources` |
| Isolated H2 database | PASS: `target\smoke\server-db` |
| Server binding | PASS: `127.0.0.1:8093` |
| Offline profile | PASS: `smoke-offline` |
| Empty-chain startup | PASS |
| Tournament network services | Disabled |
| Public peer discovery or synchronization | Not started |

Log evidence:

```text
empty local chain: bootstrap state
offline smoke mode: tournament network services disabled
Tomcat started on port(s): 8093 (http)
```

## 7. Mode B result

**Mode B: SKIPPED**

The validated historical chain was not copied or bound into the dry-run worktree. The full chain copy is large, the server resource root requires careful isolated binding, and Mode A plus focused in-memory fork mining was sufficient to test immediate V35 activation behavior without risking historical-state mutation.

## 8. Test results

Focused pre-runtime suite:

```text
Tests run: 19, Failures: 0, Errors: 0, Skipped: 0
BUILD SUCCESS
```

Covered:

- pre-fork legacy block mining and validation;
- V35 fork-block mining and validation;
- rejection of V35 founder reward;
- rejection of incorrect miner reward;
- rejection of invalid V35 PoW;
- fixed `8 / 6 / 2 / 0` reward helpers;
- rolling V35 target selection;
- positive cumulative work and work ordering;
- empty-chain difficulty bootstrap;
- offline tournament network-service suppression;
- burn-address no-spend fork gate.

Temporary worktree-only sequential mining test:

```text
International_Trade_Union.protocol.v35.IsolatedLocalV35ActivationMiningDryRunTest
Tests run: 1, Failures: 0, Errors: 0, Skipped: 0
BUILD SUCCESS
```

The temporary test mined and validated three sequential V35 blocks at indexes `1`, `2`, and `3`.

Build for local runtime smoke:

```powershell
.\mvnw.cmd -DskipTests clean package
```

Result:

```text
BUILD SUCCESS
```

The full server test suite was not run with the temporary finite fork-height override because some safety tests intentionally assert the production disabled sentinel. The selected focused suite was used to validate activation mode without weakening those production safety tests.

## 9. Runtime endpoint results

The isolated empty runtime chain remained at size `0`, so runtime V35 status correctly remained inactive before a mined block exists:

| Endpoint | HTTP | Result |
| --- | --- | --- |
| `/size` | `200` | `0` |
| `/v35/protocol` | `200` | `v35Active=false`, `v35ForkIndex=1` |
| `/v35/supply` | `200` | Read-only response returned |
| `/v35/burn` | `200` | Placeholder semantics correct; no burn transaction |
| `/v35/founder` | `200` | Read-only response returned |
| `/v35/founder/burn-snapshot` | `200` | Read-only modeled snapshot returned |
| `/v35/jackpot/status` | `200` | Jackpot status disabled before fork |

The storage server does not expose a mining endpoint. It therefore cannot advance its empty runtime chain through HTTP. Active post-fork endpoint semantics were verified through the temporary worktree-only sequential mining test using the same response builders.

## 10. Mining result

| Item | Result |
| --- | --- |
| Crossed fork boundary | YES |
| Mining path | Focused in-memory server consensus validation |
| Local V35 blocks mined | `3` |
| Block indexes mined | `1`, `2`, `3` |
| Latest local simulated height | `3` |
| Miner reward | `6.00 GOLD` |
| Jackpot reserve per block | `2.00 GOLD` |
| Founder reward | `0.00 GOLD` |
| Total V35 reward model | `8.00 GOLD` |
| Positive cumulative work | YES |
| Chain-selection/work helper crash | NO |

No manual jackpot payout transaction was created. Jackpot checks were status and policy checks only.

## 11. V35 endpoint results before and after local fork

Before local fork in runtime empty-chain smoke:

```json
{"v35Active":false,"v35ForkIndex":1}
```

After local fork in the focused sequential mining test:

| Response field | Verified value |
| --- | --- |
| `v35Active` | `true` |
| `v35ForkIndex` | `1` |
| `rewardPerBlockGold` | `8.00` |
| `minerRewardGold` | `6.00` |
| `jackpotReservePerBlockGold` | `2.00` |
| `founderRewardGold` | `0.00` |
| `burnAddress` | `BURN_ADDRESS` |
| `isBurnAddressPlaceholder` | `true` |
| `burnAddressConfigured` | `false` |
| `publicBurnAddressPublished` | `false` |
| `burnTransactionCreated` | `false` |

## 12. Legacy endpoint compatibility smoke

| Endpoint | HTTP | Result |
| --- | --- | --- |
| `/size` | `200` | `0` |
| `/prevBlock` | `200` | Empty bootstrap response |
| `/datashort` | `200` | Valid empty bootstrap summary |
| `/balance?address=test-local-address` | `200` | Zero-valued local test account |

## 13. Logs and network safety

Socket audit showed the dry-run server listening only on:

```text
127.0.0.1:8093
```

The offline profile prevented tournament network-service startup. Logs contained no public peer discovery, synchronization, deployment, or outbound-node activity. No public blockchain data was used.

## 14. Safety confirmation

- No production activation occurred.
- No main `C:\citu` `Seting.java` file changed.
- No worktree-only fork-height override was copied back.
- `V35_BURN_ADDRESS` remained `BURN_ADDRESS`.
- No founder burn transaction was created, signed, simulated, or broadcast.
- No manual jackpot payout transaction was created.
- No production wallet private key or real user funds were used.
- No deployment, push, commit, tag, release artifact, or checksum action occurred.
- No public peers were contacted.

## 15. Remaining risks

- Runtime HTTP smoke validates empty-chain startup and endpoint shapes, but storage server HTTP cannot mine blocks.
- The post-fork mining proof is focused in-memory consensus validation, not a multi-process node network run.
- Mode B near-current-chain simulation remains untested.
- Full-suite activation-mode execution remains intentionally constrained by production-disabled safety assertions.
- Final activation still requires fresh live-height approval, a separate reviewed activation commit, final builds, checksums, tag, communications, and monitoring.

## 16. Recommended next phase

Proceed to a report-only Phase 10F-3A review of the isolated dry-run evidence. If stronger runtime confidence is required, define a separate Mode B disposable-environment plan for a copied validated chain and a local candidate-block injection harness. Do not activate V35 and do not merge the temporary worktree-only fork override.
