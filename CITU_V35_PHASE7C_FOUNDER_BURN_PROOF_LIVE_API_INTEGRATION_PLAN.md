# CITU V35 Phase 7C Founder Burn Proof Live/API Integration Plan

Phase: 7C only  
Scope: plan for future live/API integration on the public founder burn proof page  
Code changes: none  
V35 activation: not performed  
Founder burn transaction: not created  
Jackpot payout transaction: not created

## 1. Exact Reports / Files Inspected

Reports read:

- `CITU_V35_PHASE7A_FOUNDER_BURN_PROOF_PACKAGE_PLAN.md`
- `CITU_V35_PHASE7B_PUBLIC_BURN_PROOF_PAGE_REPORT.md`
- `CITU_V35_PHASE5C2C_SUPPLY_API_ENDPOINTS_REPORT.md`
- `CITU_V35_PHASE5E1_FOUNDER_BURN_SNAPSHOT_REPORT.md`
- `CITU_V35_PHASE5D1_SENTINEL_BURN_ADDRESS_REPLACEMENT_REPORT.md`
- `CITU_V35_PHASE6F1_FINAL_FORK_INDEX_ACTIVATION_PLAN.md`

Website files inspected:

- `website.citu-web-master/src/main/java/com/example/website/citu/controller/V35PublicController.java`
- `website.citu-web-master/src/main/resources/templates/v35-founder-burn-proof.html`
- `website.citu-web-master/src/main/resources/templates/v35.html`
- `website.citu-web-master/src/main/resources/templates/v35-burn.html`
- `website.citu-web-master/src/main/resources/templates/blocks/header.html`

Server API files inspected:

- `unitedStates_storage-master/src/main/java/International_Trade_Union/controllers/V35SupplyController.java`
- `unitedStates_storage-master/src/main/java/International_Trade_Union/protocol/v35/V35SupplyApiResponseBuilder.java`

Wallet reference inspected:

- `unitedStates_final-master/src/main/java/International_Trade_Union/controllers/V35DashboardApiClient.java`

Constants inspected:

- `unitedStates_final-master/src/main/java/International_Trade_Union/setings/Seting.java`
- `unitedStates_storage-master/src/main/java/International_Trade_Union/setings/Seting.java`

Confirmed:

- Wallet: `V35_RESCUE_FORK_INDEX = Long.MAX_VALUE`
- Server: `V35_RESCUE_FORK_INDEX = Long.MAX_VALUE`
- Wallet: `V35_BURN_ADDRESS = "BURN_ADDRESS"`
- Server: `V35_BURN_ADDRESS = "BURN_ADDRESS"`
- Founder address: `nNifuwmFZr7fnV1zvmpiyQDV5z7ETWvqR6GSeqeHTY43`

## 2. Recommended API Endpoints

Future live integration should consume only read-only server endpoints:

- `GET /v35/supply`
- `GET /v35/burn`
- `GET /v35/founder`
- `GET /v35/founder/burn-snapshot`

Do not call mutation endpoints.

Endpoint roles:

- `/v35/founder/burn-snapshot`: snapshot proof and modeled 75/80/85/90 burn options.
- `/v35/supply`: actual total minted, burned, circulating, founder reserve, jackpot paid, annual emission.
- `/v35/burn`: burn address status, actual burned amount, no-spend policy, burn transaction created flag.
- `/v35/founder`: founder reserve and modeled 80% burn summary.

## 3. Data Mapping By Page Section

### Current Burn Status

From `/v35/burn`:

- `burnAddress`
- `burnAddressConfigured`
- `isBurnAddressPlaceholder`
- `burnedGold`
- `burnTransactionCreated`
- `noSpendAfterV35`
- `v35Active`
- `v35ForkIndex`
- `note`

Display logic:

- If `burnTransactionCreated = false`, status is `Planned / Pending / Not executed yet`.
- If `burnedGold = 0.00`, status is not burned unless a future proof source confirms otherwise.
- If `v35ForkIndex = Long.MAX_VALUE`, V35 must display as disabled.

### Snapshot Proof

From `/v35/founder/burn-snapshot`:

- `snapshotHeight`
- `snapshotBlockHash`
- `founderAddress`
- `founderBalanceGold`
- `burnAddress`
- `recommendedBurnPercent`
- `annualEmissionGold`
- `v35Active`
- `v35ForkIndex`
- `burnOptions`
- `proofChecklist`
- `note`

Map burn option fields:

- `percent`
- `recommended`
- `burnGold`
- `founderReserveAfterBurnGold`
- `modeledBurnedGoldAfterBurn`
- `modeledCirculatingGoldAfterBurn`
- `modeledInflationAfterBurnPercent`

Display the 75/80/85/90 table when available. Mark `80%` as recommended.

### Supply Section

From `/v35/supply`:

- `totalMintedGold`
- `burnedGold`
- `circulatingGold`
- `founderReserveGold`
- `jackpotPaidGold`
- `annualEmissionGold`
- `valid`
- `invalidReason`
- `snapshotSource`

Display as actual chain-derived supply only. Do not overwrite these values with modeled burn options.

### Burn Section

From `/v35/burn` and `/v35/founder/burn-snapshot`:

- actual burn address;
- actual burned amount;
- modeled/recommended burn amount;
- burn transaction status;
- actual versus planned status.

Display rule:

- Actual burned amount comes from `/v35/burn` or `/v35/supply`.
- Modeled recommended amount comes only from `/v35/founder/burn-snapshot`.

### Burn Transaction Proof

Current APIs do not provide a real `burnTxHash`, `burnBlockHash`, or `burnBlockIndex` because the burn has not happened.

For Phase 7D, keep these fields manual/pending unless a future read-only proof endpoint is added or the proof is published as static verified data.

Do not infer transaction proof from `burnedGold` alone.

## 4. Actual Vs Planned Burn Display Rules

Strict rules for all future integration:

- If burn has not happened, show `Planned`, `Pending`, or `Not executed yet`.
- Do not mix modeled burn values into actual `burnedGold` or `circulatingGold`.
- Do not claim a burn transaction exists unless a verified proof source provides a transaction hash and block reference.
- Do not invent a transaction hash, block hash, snapshot hash, timestamp, or amount.
- If an API is unavailable, keep the static proof template visible and mark live fields `Unavailable` or `Pending`.
- If `/v35/founder/burn-snapshot` returns `UNKNOWN` snapshot hash, display `Unknown / unavailable`, not a fake hash.
- If `V35_RESCUE_FORK_INDEX = Long.MAX_VALUE`, display V35 as disabled.
- If endpoint responses conflict, prefer the most conservative public wording: `Verification unavailable`.
- Keep actual supply and modeled burn projections in separate tables.
- Keep the disclaimer visible after live data is added.

## 5. Recommended Integration Architecture

Options reviewed:

### Option A: Website Server-Side Fetches V35 APIs

Pros:

- No browser CORS issue.
- Server can normalize/fallback response before rendering.
- Similar to traditional Spring MVC template flow.
- Easier to test with injected fetcher/service.

Cons:

- Page render can slow if API endpoints are slow.
- Website server needs a configured V35 API base URL.
- If the API server is down, server-side timeout/fallback must be robust.

### Option B: Frontend Browser Fetches APIs

Pros:

- Static page renders immediately.
- Live section can update after load.
- API failures do not block initial page render.

Cons:

- CORS/routing issues are likely.
- More client-side error handling.
- Public browser can see endpoint availability and transient errors directly.
- Harder to test in current server-side template test style.

### Option C: Keep Static Until Burn, Then Manually Publish Proof Fields

Pros:

- Highest trust for the official proof page if values are reviewed and locked.
- No runtime dependency on API availability.
- No performance risk.

Cons:

- Manual updates required.
- Stale if post-burn balances change or API values reveal a mismatch.
- No automatic display of current supply.

### Option D: Hybrid Static + Optional API Status

Pros:

- Static reviewed proof fields remain authoritative.
- Optional API status can show live current values and endpoint health.
- If APIs fail, page remains useful.
- Avoids claiming live proof until manually verified.
- Matches the wallet dashboard philosophy of safe fallback.

Cons:

- Requires careful labeling of official proof versus live API status.
- Slightly more complex page model.

Recommended architecture:

```text
Option D - hybrid static proof page with optional server-side API status.
```

Implementation model for Phase 7D:

- Keep current static placeholder/proof template.
- Add a website-local read-only API client/service modeled after `V35DashboardApiClient`, but scoped to the four founder/burn endpoints.
- Use short timeouts.
- Tolerate malformed JSON and old servers.
- Add live data to the model only when safe.
- Display API data as `Live API status`, not as final proof.
- Keep transaction hash/block proof manual/pending until the real burn proof is explicitly published.

This gives public readers a durable proof page plus live context without making API availability a trust bottleneck.

## 6. Fallback / Unavailable Behavior

Required fallback cases:

- Old server missing `/v35/*`: page renders static placeholders and shows `Live V35 data unavailable`.
- Timeout: page renders static placeholders and shows `Live V35 data unavailable`.
- Malformed JSON: page renders static placeholders and logs/records unavailable status in tests if applicable.
- Partial data: show available fields only; missing fields remain `Unavailable` or `Pending`.
- Server says V35 disabled: show disabled status and do not claim activation.
- Burn not executed: show `Planned / Pending / Not executed yet`.
- Founder snapshot unavailable: show `Snapshot unavailable`; keep placeholders.
- Snapshot hash is `UNKNOWN`: show `UNKNOWN` and explain source did not provide a block hash.
- Conflicting fields: show `Verification unavailable` and preserve static warnings.

Fields that should never be synthesized:

- burn transaction hash;
- burn block index;
- burn block hash;
- snapshot block hash;
- actual burn amount;
- confirmation count.

## 7. Page Sections After Integration

The integrated page should include:

1. Current burn status
   - planned/pending/executed;
   - V35 active/disabled;
   - fork index;
   - API source status.

2. Snapshot proof
   - snapshot height/hash;
   - founder address;
   - founder balance;
   - 75/80/85/90 modeled options;
   - recommended 80% marker.

3. Burn transaction proof
   - transaction hash;
   - sender;
   - recipient;
   - amount;
   - block index/hash;
   - confirmations;
   - pending until real proof is available.

4. Supply before/after
   - actual total minted;
   - actual burned;
   - actual circulating;
   - founder reserve;
   - jackpot paid;
   - modeled post-burn values, clearly separated.

5. Founder reserve
   - actual founder reserve from `/v35/supply` or `/v35/founder`;
   - modeled after-burn reserve from burn snapshot options.

6. How to verify
   - snapshot height/hash;
   - founder address;
   - burn address;
   - tx sender/recipient;
   - API endpoint links.

7. API source status
   - server URL/source;
   - endpoint availability;
   - last fetch status if implemented.

8. Warning/disclaimer
   - not financial advice;
   - burn is irreversible;
   - pending values are not proof;
   - actual and modeled values are separate.

## 8. Required Tests For Future Implementation

Phase 7D tests should cover:

- `/v35/founder-burn-proof` renders when API unavailable.
- Static founder address appears.
- Static `BURN_ADDRESS` appears.
- Page displays pending status when burn is not executed.
- Page does not show fake transaction hash.
- Page does not show fake block hash.
- Page displays `/v35/supply`, `/v35/burn`, `/v35/founder`, `/v35/founder/burn-snapshot` as sources.
- Live `/v35/founder/burn-snapshot` data maps snapshot height/hash/founder balance safely.
- Live burn options show 75/80/85/90 and mark 80% recommended.
- Live actual supply values remain separate from modeled burn values.
- Malformed JSON does not crash.
- Timeout/missing endpoint does not crash.
- Partial JSON leaves missing fields `Unavailable`.
- `V35_RESCUE_FORK_INDEX = Long.MAX_VALUE` displays V35 disabled.
- API client calls only read-only endpoints.
- No mutation endpoints are called.

If Phase 7D uses server-side fetching, add helper/client tests with injected fetcher similar to wallet dashboard tests.

## 9. Exact Files Phase 7D May Edit

Allowed Phase 7D files:

- `website.citu-web-master/src/main/java/com/example/website/citu/controller/V35PublicController.java`
- new website read-only V35 proof API client/helper, if needed
- `website.citu-web-master/src/main/resources/templates/v35-founder-burn-proof.html`
- `website.citu-web-master/src/main/resources/templates/v35.html`, only if navigation/source links need adjustment
- `website.citu-web-master/src/main/resources/templates/v35-burn.html`, only if navigation/source links need adjustment
- `website.citu-web-master/src/main/resources/templates/blocks/header.html`, only if navigation needs adjustment
- `website.citu-web-master/src/test/java/com/example/website/citu/controller/V35PublicControllerTest.java`
- new website tests for API helper/client, if needed
- `CITU_V35_PHASE7D_...REPORT.md`

## 10. Exact Files Phase 7D Must NOT Edit

Phase 7D must not edit:

- wallet consensus code;
- server consensus code;
- `Mining.java`;
- `UtilsBlock.java`;
- `UtilsBalance.java`;
- `Blockchain.java`;
- `UtilsResolving.java`;
- `Seting.java` fork index values;
- transaction format classes;
- block format classes;
- jackpot mining/validation/accounting logic;
- burn-address consensus policy;
- server V35 API semantics, unless a separate explicitly scoped API phase authorizes it;
- wallet dashboard code;
- `pom.xml`;
- real chain data;
- any file that would create or broadcast a burn transaction.

## 11. Should Live Integration Happen Now Or Later?

Recommendation:

```text
Implement Phase 7D now only as optional live status integration, not as final proof publication.
```

Reasoning:

- The page can safely consume current read-only APIs while the burn remains pending.
- Current API responses already include disabled/pending/model-only fields.
- A hybrid status integration would improve public transparency before the burn.
- However, final burn transaction proof fields should remain manual/static until the real transaction is executed and independently verified.

If the team wants the simplest public launch path, delay Phase 7D until after the real snapshot/burn workflow is settled. The static proof page from Phase 7B is already safe because it makes no live claims.

Best sequencing:

1. Phase 7D optional: live API status integration with all proof fields still pending.
2. Manual founder snapshot and burn later, after hardening/replay/release gates.
3. Separate post-burn proof-fill phase to publish real transaction hash and block data.

## 12. Remaining Risks

1. Runtime API dependency can make public page data temporarily unavailable.
2. API data is only as authoritative as the configured server/node.
3. Full-chain scans in supply endpoints may be expensive on production data.
4. Users may confuse modeled burn values with actual burned supply if labels are weak.
5. There is no live burn transaction proof endpoint yet.
6. A future integration must not infer burn completion from `burnedGold` alone.
7. `BURN_ADDRESS` recipient support still needs operational confirmation before manual burn.
8. V35 remains disabled, so API active-state values should continue to show disabled.

## 13. Recommended Next Phase

Recommended next phase:

```text
Phase 7D - optional public founder burn proof live/status API integration, no actual burn.
```

Scope for Phase 7D:

- website-only;
- read-only;
- hybrid static + optional API status;
- keep all transaction proof fields pending;
- no protocol, wallet, server consensus, or API semantic changes.

Alternative:

```text
Phase 8A - release notes / public announcement draft.
```

Use Phase 8A if public communication copy is more urgent than live status integration.

Optional technical confidence:

```text
Phase 6D-2 - long-window difficulty simulation.
```

Use Phase 6D-2 first if the team wants more protocol dry-run confidence before additional public-facing work.
