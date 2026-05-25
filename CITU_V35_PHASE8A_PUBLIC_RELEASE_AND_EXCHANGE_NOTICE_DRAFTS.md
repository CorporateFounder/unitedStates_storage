# CITU V35 Phase 8A Public Release And Exchange Notice Drafts

Phase: 8A only  
Scope: public release announcement, exchange/node operator notice, timed notices, fork-day and post-fork drafts  
Code changes: none  
V35 activation: not performed  
Real fork index: not selected  
Founder burn transaction: not created  
Jackpot payout transaction: not created

## 1. Public Announcement Draft

### CITU V35 Protocol Upgrade Announcement

CITU V35 is the next planned protocol upgrade for the CITU network. It is designed to simplify the reward model, remove the founder reward after activation, introduce deterministic jackpot mining, strengthen proof-of-work validation, and make chain selection based on cumulative work.

V35 is not active yet. Activation will happen by exact block index, not by date alone. The final fork index is still pending and will be announced after release validation, exchange/node coordination, and founder burn proof preparation are complete.

### What Changes In V35

After V35 activates:

- Total block reward becomes `8 GOLD` per block.
- Miner instant reward becomes `6 GOLD` per block.
- Jackpot reserve becomes `2 GOLD` per block.
- Founder reward becomes `0 GOLD` per block.
- Target block time becomes `200 seconds`.
- Expected blocks per day becomes `432`.
- Annual emission becomes `1,261,440 GOLD`.
- Proof of work uses a Bitcoin-like target rule: `hash <= target`.
- Chain selection uses cumulativeWork.
- `BURN_ADDRESS` is the configured protocol burn sink.

Transaction and block formats are not changed by this announcement.

### Founder Reward Removed

Founder reward after V35 is `0 GOLD`. This means post-V35 block rewards are split between the miner instant reward and jackpot reserve only:

```text
8 GOLD/block = 6 GOLD miner + 2 GOLD jackpot + 0 GOLD founder
```

### Founder Burn Plan

The V35 burn address is:

```text
BURN_ADDRESS
```

`BURN_ADDRESS` is a protocol sink. Sending coins TO `BURN_ADDRESS` is allowed. Spending FROM `BURN_ADDRESS` is blocked after V35 by consensus policy.

The founder burn is planned, but it has not happened yet. The recommended burn is `80%` of the snapshot founder GOLD balance. The burn proof package will include:

- snapshot height;
- snapshot block hash;
- founder address;
- founder balance at snapshot;
- burn amount;
- burn transaction hash;
- burn block index;
- burn block hash;
- founder balance after burn;
- burned supply;
- circulating supply.

Until those proof fields are filled with real on-chain data, the founder burn must be treated as planned, not completed.

Public proof page:

```text
/v35/founder-burn-proof
```

### Jackpot Mining

Every V35 mined block is a jackpot ticket. Jackpot winners are selected deterministically from eligible mined blocks. Only the block `minerAddress` can win. Sender addresses are never jackpot participants.

V35 jackpot policy uses delayed future block hashes and fixed payout periods:

| Period | Payout |
| --- | ---: |
| Daily | `302.40 GOLD` |
| Weekly | `1209.60 GOLD` |
| Monthly | `3888.00 GOLD` |
| Quarterly | `9720.00 GOLD` |
| Half-year | `15724.80 GOLD` |
| Yearly | `23652.00 GOLD` |

Jackpot payout history should be treated as empty until real V35 jackpot payouts are created and validated on-chain.

### Upgrade Timing

The final V35 fork index is pending. Activation will be announced as:

```text
V35_RESCUE_FORK_INDEX = <REAL_FORK_INDEX_PENDING>
Estimated activation time = <ESTIMATED_DATE_TIME_PENDING>
```

The block index is authoritative. The date/time is an estimate based on block production.

Recommended public notice window:

- at least 14 days before activation;
- 21 days if extra exchange/node upgrade margin is needed.

### Who Needs To Act

Normal holders do not need to take action unless they run a node, miner, exchange integration, or wallet infrastructure.

Node operators, miners, pools, and exchanges must upgrade before the published fork index.

### Disclaimer

This is protocol upgrade information, not financial advice. The founder burn is irreversible once executed. The fork index and burn proof must be verified from final published release materials before relying on them.

## 2. Exchange / Node Operator Notice Draft

Subject:

```text
CITU V35 Protocol Upgrade Notice - Action Required Before Fork Index <REAL_FORK_INDEX_PENDING>
```

Body:

```text
Hello,

CITU is preparing the V35 protocol upgrade. This is a consensus upgrade and node/exchange operators must upgrade before the published fork block.

Current status:
- V35 is not active yet.
- Final fork index: <REAL_FORK_INDEX_PENDING>
- Estimated activation date/time: <ESTIMATED_DATE_TIME_PENDING>
- Release version: <RELEASE_VERSION_PENDING>
- Wallet artifact: <WALLET_ARTIFACT_LINK_PENDING>
- Server artifact: <SERVER_ARTIFACT_LINK_PENDING>
- Checksums: <CHECKSUMS_PENDING>
- Support contact: <SUPPORT_CONTACT_PENDING>

Consensus changes after V35:
- Total reward: 8 GOLD/block.
- Miner instant reward: 6 GOLD/block.
- Jackpot reserve: 2 GOLD/block.
- Founder reward: 0 GOLD/block.
- Target block time: 200 seconds.
- Expected blocks/day: 432.
- Annual emission: 1,261,440 GOLD.
- Proof of work: hash <= target.
- Chain selection: cumulativeWork.
- Burn address: BURN_ADDRESS.
- Spending FROM BURN_ADDRESS is blocked after V35.

What does not change:
- Existing user balances remain valid.
- Transaction format remains unchanged.
- Block format remains unchanged.
- Normal holders do not need to perform any action.

Recommended exchange action:
1. Upgrade wallet/server infrastructure before fork index <REAL_FORK_INDEX_PENDING>.
2. Verify release checksums.
3. Run an internal sync check.
4. Consider pausing deposits/withdrawals around the fork window according to your risk policy.
5. After fork, verify first V35 block hash and reward shape before resuming normal processing.

Post-fork verification:
- First V35 block index: <FIRST_V35_BLOCK_INDEX_PENDING>
- First V35 block hash: <FIRST_V35_BLOCK_HASH_PENDING>
- Miner reward must be 6 GOLD.
- Founder reward must be 0 GOLD.
- V35 target PoW must be active.
- cumulativeWork must be reported/used by upgraded nodes.

Founder burn:
- The founder burn is planned unless final proof is published.
- Burn address: BURN_ADDRESS.
- Burn proof page: /v35/founder-burn-proof
- The burn transaction hash will be published after execution.

Please confirm receipt and upgrade readiness by <CONFIRMATION_DEADLINE_PENDING>.
```

## 3. T-14 Notice

Subject:

```text
CITU V35 Upgrade: 14-Day Node/Exchange Notice
```

Draft:

```text
CITU V35 is planned for activation at block index <REAL_FORK_INDEX_PENDING>.

Estimated activation time: <ESTIMATED_DATE_TIME_PENDING>
Release version: <RELEASE_VERSION_PENDING>
Checksums: <CHECKSUMS_PENDING>

Operators should upgrade wallet/server infrastructure before the fork index.

Key changes:
- 8 GOLD/block total.
- 6 GOLD miner reward.
- 2 GOLD jackpot reserve.
- 0 GOLD founder reward.
- 200 sec/block target.
- cumulativeWork chain selection.
- Bitcoin-like PoW target: hash <= target.
- Burn address: BURN_ADDRESS.

Founder burn status:
- Planned unless final proof is published.
- Proof page: /v35/founder-burn-proof

Exchange guidance:
- Please test the new release in staging.
- Consider a deposit/withdrawal caution window around activation according to your risk policy.
- Support contact: <SUPPORT_CONTACT_PENDING>
```

## 4. T-7 Notice

Subject:

```text
CITU V35 Upgrade: 7-Day Reminder
```

Draft:

```text
CITU V35 activation is approaching.

Fork index: <REAL_FORK_INDEX_PENDING>
Estimated activation time: <UPDATED_ESTIMATED_DATE_TIME_PENDING>
Release version: <RELEASE_VERSION_PENDING>
Checksums: <CHECKSUMS_PENDING>

Please confirm:
- upgraded wallet/server installed;
- node fully synced;
- deposits/withdrawals policy reviewed;
- monitoring staff assigned for fork window.

Reminder:
- V35 activates by block index, not date alone.
- Founder reward becomes 0 GOLD.
- Miner reward becomes 6 GOLD.
- Jackpot reserve becomes 2 GOLD.
- Burn address is BURN_ADDRESS.

Founder burn proof:
- <BURN_PROOF_STATUS_PENDING>
- /v35/founder-burn-proof
```

## 5. T-3 Notice

Subject:

```text
CITU V35 Upgrade: Final Technical Reminder
```

Draft:

```text
CITU V35 activation is expected soon.

Current height: <CURRENT_HEIGHT_PENDING>
Fork index: <REAL_FORK_INDEX_PENDING>
Blocks remaining: <BLOCKS_REMAINING_PENDING>
Estimated activation time: <UPDATED_ESTIMATED_DATE_TIME_PENDING>

Final operator checks:
- release version installed: <RELEASE_VERSION_PENDING>
- artifact checksums verified: <CHECKSUMS_PENDING>
- node synced;
- monitoring ready;
- exchange deposit/withdrawal caution plan ready.

First V35 block checks:
- reward must be 6 GOLD miner / 0 GOLD founder;
- target PoW must be active;
- cumulativeWork must be sane;
- BURN_ADDRESS no-spend policy must be active;
- jackpot policy must remain deterministic.

Support contact: <SUPPORT_CONTACT_PENDING>
```

## 6. T-1 Notice

Subject:

```text
CITU V35 Upgrade: 24-Hour Fork Window Notice
```

Draft:

```text
CITU V35 is expected within approximately 24 hours, depending on block production.

Fork index: <REAL_FORK_INDEX_PENDING>
Current height: <CURRENT_HEIGHT_PENDING>
Blocks remaining: <BLOCKS_REMAINING_PENDING>
Estimated activation time: <UPDATED_ESTIMATED_DATE_TIME_PENDING>

Recommended exchange caution:
- Consider pausing deposits/withdrawals around the fork window if your risk policy requires it.
- Keep node logs and chain tip monitoring active.
- Resume normal processing after first V35 blocks are stable and verified.

No action is required from normal holders.

Founder burn proof status:
- <BURN_PROOF_STATUS_PENDING>
- /v35/founder-burn-proof
```

## 7. Fork-Day Status Draft

Subject:

```text
CITU V35 Fork-Day Status
```

Draft:

```text
CITU V35 fork monitoring is active.

Last legacy block:
- index: <LAST_LEGACY_BLOCK_INDEX_PENDING>
- hash: <LAST_LEGACY_BLOCK_HASH_PENDING>

First V35 block:
- index: <FIRST_V35_BLOCK_INDEX_PENDING>
- hash: <FIRST_V35_BLOCK_HASH_PENDING>
- timestamp: <FIRST_V35_BLOCK_TIME_PENDING>

Verification:
- miner reward: <MINER_REWARD_VERIFICATION_PENDING>
- founder reward: <FOUNDER_REWARD_VERIFICATION_PENDING>
- PoW target validation: <POW_VERIFICATION_PENDING>
- cumulativeWork: <CUMULATIVE_WORK_VERIFICATION_PENDING>
- BURN_ADDRESS no-spend policy: <BURN_POLICY_VERIFICATION_PENDING>
- jackpot policy: <JACKPOT_POLICY_VERIFICATION_PENDING>

Exchange/node sync:
- <EXCHANGE_SYNC_STATUS_PENDING>

Next update:
- <NEXT_STATUS_UPDATE_TIME_PENDING>
```

## 8. Post-Fork Confirmation Draft

Subject:

```text
CITU V35 Activated - Post-Fork Confirmation
```

Draft:

```text
CITU V35 has activated at block index <FIRST_V35_BLOCK_INDEX_PENDING>.

First V35 block:
- hash: <FIRST_V35_BLOCK_HASH_PENDING>
- time: <FIRST_V35_BLOCK_TIME_PENDING>

Observed status:
- first V35 blocks stable: <FIRST_BLOCKS_STABLE_PENDING>
- miner reward verified: <MINER_REWARD_VERIFICATION_PENDING>
- founder reward verified as 0: <FOUNDER_REWARD_ZERO_VERIFICATION_PENDING>
- PoW target active: <POW_VERIFICATION_PENDING>
- cumulativeWork active: <CUMULATIVE_WORK_VERIFICATION_PENDING>
- APIs verified: <API_VERIFICATION_PENDING>
- wallet dashboard verified: <WALLET_DASHBOARD_VERIFICATION_PENDING>

Founder burn proof:
- <BURN_PROOF_LINK_OR_STATUS_PENDING>

Exchange/node status:
- <EXCHANGE_SYNC_STATUS_PENDING>

Thank you to node operators, miners, exchanges, and the community for upgrading.
```

## 9. Founder Burn Public Note

Draft:

```text
Founder burn status: planned unless final proof fields are filled.

The V35 founder burn is planned as a normal founder-signed transaction:

sender: nNifuwmFZr7fnV1zvmpiyQDV5z7ETWvqR6GSeqeHTY43
recipient: BURN_ADDRESS
recommended amount: 80% of the snapshot founder GOLD balance

BURN_ADDRESS is a protocol sink. Sending TO BURN_ADDRESS is allowed. Spending FROM BURN_ADDRESS is blocked after V35 by consensus policy.

The burn proof package will include:
- snapshot height;
- snapshot block hash;
- founder balance before burn;
- burn amount;
- burn transaction hash;
- burn block index;
- burn block hash;
- founder balance after burn;
- burned supply;
- circulating supply;
- annual emission.

No burn should be treated as complete until the real transaction hash, block hash, and after-burn supply values are published.

Modeled burn values are projections. Actual burned supply is based on the chain balance of BURN_ADDRESS.
```

## 10. Placeholder Checklist

Fill these before publishing any final public or exchange notice:

- [ ] Real fork index.
- [ ] Estimated activation date/time.
- [ ] Current chain height at announcement.
- [ ] Blocks remaining at announcement.
- [ ] Release version.
- [ ] Wallet artifact link.
- [ ] Server artifact link.
- [ ] Website artifact/deploy reference, if applicable.
- [ ] Artifact checksums.
- [ ] Support contact.
- [ ] Emergency communication channel.
- [ ] Exchange/operator confirmation deadline.
- [ ] Exchange list/status.
- [ ] Historical replay result summary.
- [ ] Long-window difficulty dry-run status summary.
- [ ] Founder snapshot height.
- [ ] Founder snapshot block hash.
- [ ] Founder balance at snapshot.
- [ ] Selected burn percent.
- [ ] Burn amount.
- [ ] Burn transaction hash, if burn already happened.
- [ ] Burn block index/hash, if burn already happened.
- [ ] Founder balance after burn, if burn already happened.
- [ ] Burned supply after burn, if burn already happened.
- [ ] Circulating supply after burn, if burn already happened.
- [ ] First V35 block index/hash, post-fork only.
- [ ] Reward verification result, post-fork only.
- [ ] PoW/cumulativeWork verification result, post-fork only.
- [ ] Exchange sync confirmations, post-fork only.

Do not publish placeholders as final facts.

## 11. Confirmation No Code Changed

No source code, tests, protocol constants, wallet/server/website behavior, `pom.xml`, transaction format, block format, mining behavior, validation behavior, balance behavior, or API semantics were changed in this phase.

Only this Markdown report was created:

- `CITU_V35_PHASE8A_PUBLIC_RELEASE_AND_EXCHANGE_NOTICE_DRAFTS.md`

## 12. Confirmation V35 Remains Disabled

Confirmed:

- Wallet `V35_RESCUE_FORK_INDEX = Long.MAX_VALUE`
- Server `V35_RESCUE_FORK_INDEX = Long.MAX_VALUE`
- Wallet `V35_BURN_ADDRESS = "BURN_ADDRESS"`
- Server `V35_BURN_ADDRESS = "BURN_ADDRESS"`

The real fork index has not been selected.

Founder burn has not happened.

No founder burn transaction was created.

No jackpot payout transaction was created.

## 13. Recommended Next Phase

Recommended next phase:

```text
Phase 7E - final burn execution checklist, no actual burn.
```

Purpose:

- prepare the final human checklist for snapshot, burn amount confirmation, transaction review, proof capture, and post-burn verification;
- keep burn execution separate from planning;
- do not activate V35 or choose the real fork index yet.
