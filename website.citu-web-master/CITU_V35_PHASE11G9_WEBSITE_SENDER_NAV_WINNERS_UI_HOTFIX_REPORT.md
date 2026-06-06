# CITU V35 Phase 11G-9 Website Sender/Nav/Winners UI Hotfix Report

Date: 2026-06-06

## Scope

Website-only hotfix for the CITU public website package. No node, wallet, consensus, VPS, server resources, burn, jackpot, or blockchain protocol files were changed.

## Root Cause

The sender transaction page still followed the older website form contract. It required `pubkey`, preserved stale session values too aggressively, and could fall back into legacy paging behavior. This made normal explorer searches fragile and did not guarantee the node call used the sender transaction API with `address`, `from`, and `to`.

## Sender Route Fix

The website now accepts sender searches through:

- `/sender?address=<address>&from=0&to=10`
- `/sender?pubkey=<address>&from=0&to=10`
- `/send?address=<address>&from=0&to=10`
- `/send?pubkey=<address>&from=0&to=10`
- `/transactionsSender?address=<address>&from=0&to=10`

`pubkey` is treated as a legacy alias for the account address. Empty API responses render a safe `not found` state, and unavailable node responses render `temporarily unavailable`.

## Exact Sender Node API

The sender controller now calls exactly:

```text
GET /senderTransactions?address=<encoded-address>&from=<from>&to=<to>
```

Example:

```text
GET /senderTransactions?address=sender-address&from=0&to=10
```

## Customer Route Preserved

Customer transaction search remains separate and continues to use:

- `/customer?address=<address>&from=0&to=10`
- `/customer?pubkey=<address>&from=0&to=10`
- `/transactionsCustomer?address=<address>&from=0&to=10`

The customer controller calls exactly:

```text
GET /customerTransactions?address=<encoded-address>&from=<from>&to=<to>
```

## Navigation Cleanup

The shared public header was verified to keep a clean, unique navigation surface:

- Home: `/`
- Explorer: `/conductor`
- Blocks: `/blocks`
- Winners: `/winners`
- API / Exchanges: `/wallet_and_node_url`
- V35 protocol paper: `/white_papper`
- Charter: `/charter`
- V35: `/v35`

No public navigation link exposes the typo route `winerList`, and duplicate `/conductor`, `/blocks`, and `/winners` links are guarded by tests.

## Winners Design Cleanup

`winners.html` was restyled into the shared dark public website layout with `v35-public`, `citu-section`, metric cards, winner cards, and safe empty/unavailable states. UI links expose clean routes only: `/blocks`, `/winners`, and `/winnerList`.

## Files Changed

- `src/main/java/com/example/website/citu/controller/TransactionSenderController.java`
- `src/main/java/com/example/website/citu/controller/TransactionCustomerController.java`
- `src/main/java/com/example/website/citu/model/DtoTransaction.java`
- `src/main/resources/templates/transactionsSender.html`
- `src/main/resources/templates/transactionsCustomer.html`
- `src/main/resources/templates/winners.html`
- `src/main/resources/static/css/styles.css`
- `src/test/java/com/example/website/citu/controller/LegacyApiContractTest.java`
- `CITU_V35_PHASE11G9_WEBSITE_SENDER_NAV_WINNERS_UI_HOTFIX_REPORT.md`

## Tests Run

```text
.\mvnw.cmd test
.\mvnw.cmd clean package
.\mvnw.cmd test
```

Result: all 52 tests passed.

## Release Artifact

Local dry-run artifact:

```text
C:\citu\release-dryrun\v35-514291\website.citu-0.0.24-SNAPSHOT.jar
```

GitHub release:

```text
https://github.com/CorporateFounder/website.citu-web/releases/tag/v35-website-activation-514291
```

Final SHA256:

```text
E67426D36DC99470C6DE550B8973D3E801C5D8FBAAD75B8C9DF1D5932F33A645
```

## Public Verification

The release asset was downloaded from:

```text
https://github.com/CorporateFounder/website.citu-web/releases/download/v35-website-activation-514291/website.citu-0.0.24-SNAPSHOT.jar
```

Downloaded SHA256 matched the local dry-run artifact:

```text
E67426D36DC99470C6DE550B8973D3E801C5D8FBAAD75B8C9DF1D5932F33A645
```

The downloaded JAR contains the patched sender/customer controllers, transaction templates, winners template, DTO safety change, and shared CSS.

## Exact VPS Command

No VPS deploy was performed. When approved, use:

```bash
cd /root/website && wget -O website.citu-0.0.24-SNAPSHOT.jar https://github.com/CorporateFounder/website.citu-web/releases/download/v35-website-activation-514291/website.citu-0.0.24-SNAPSHOT.jar && sha256sum website.citu-0.0.24-SNAPSHOT.jar
```

Expected SHA256:

```text
e67426d36dc99470c6de550b8973d3e801c5d8fbaad75b8c9df1d5932f33a645
```

## Safety Confirmation

- Website repository only.
- No node source changed.
- No wallet source changed.
- No consensus or protocol files changed.
- No `Seting.java` changed.
- No `C:\server\resources` or `C:\resources` files changed.
- No VPS deployment, restart, process kill, or service mutation was performed.
- No burn or jackpot logic changed.
- No new GitHub tag was created.
