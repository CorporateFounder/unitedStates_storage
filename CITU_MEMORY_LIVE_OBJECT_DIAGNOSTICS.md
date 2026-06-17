# CITU Memory Live Object Diagnostics

This note is for diagnosing node memory growth without changing consensus, rewards, PoW, staking, V35 constants, block validation, wallet behavior, or blockchain resources.

## Live State Endpoints

Use these endpoints on the running node:

- `GET /debug/memory/live-state`
- `GET /debug/peers/self`
- `GET /debug/peer/connections`
- `GET /debug/memory/status`
- `GET /debug/block-submit/status`

The live-state endpoint returns counts and cheap size estimates only. It must not return full `Block` objects, full transaction lists, full winner JSON, or serialized peer payloads.

Important fields:

- `currentBlockchainSize`: current local chain height/size.
- `winnerListSize`: temporary submitted block queue size. It is capped at 100.
- `allWinnersSize`: public winner DTO cache size. It is capped at 100.
- `currentCandidatesSize`: public current candidate count. It is capped at 100.
- `lastTournamentSnapshotCandidateCount`: candidate DTO count in the last public tournament snapshot. It is capped at 100.
- `lastTournamentSnapshotsCount`: retained public snapshots. It is capped at 10.
- `activeRequestsByEndpoint`: current outbound HTTP requests grouped by endpoint.
- `activeFuturesByPhase`: current async futures grouped by internal phase.
- `activePeerTasksCount`, `activeTournamentFuturesCount`, `activeResolveFuturesCount`: high-level active work counters.
- `currentThreadCount`: JVM thread count.
- `usedMemory`, `maxMemory`: current JVM heap pressure.
- `lastMemoryProbeStage`: last recorded memory probe stage.

## process-memory.csv

Use this file to correlate memory growth with node stages over time.

Read it by time order and look for:

- repeated growth at the same `stage`;
- growth after tournament finish;
- growth during peer propagation or resolve phases;
- growth that does not drop after candidate queues are capped.

If `usedMemory` grows while live-state counters stay low, collect a class histogram.

## live-class-histogram.txt

Prefer a live `jcmd GC.class_histogram` snapshot over automatic heap dumps for normal production diagnostics.

Look for classes whose instance count or byte count grows between snapshots:

- `Block`
- transaction DTO classes
- `ArrayList`
- `CopyOnWriteArrayList`
- `ConcurrentHashMap`
- `CompletableFuture`
- HTTP/Tomcat connection classes
- byte arrays and strings from large peer responses

If `Block` or transaction DTO counts grow while live-state queue counts are low, the owner is likely outside public snapshots and should be traced by phase counters.

## heap-info.txt

Use this to confirm heap configuration and pressure:

- max heap;
- current heap usage;
- GC behavior;
- whether heap is near the configured maximum before tournament finish.

Normal production startup should not create `.hprof` heap dumps by default. Heap dumps are diagnostic-only and must be explicitly enabled.

## tcp-connections.txt

Use this to detect connection or peer fan-out problems:

- many active connections to the same peer;
- many connections to self aliases;
- high connection counts during tournament or resolve phases;
- stale connections that do not close.

Compare with `/debug/peer/connections`:

- `activeRequestsByEndpoint`;
- `remoteCallsThisMinute`;
- `timeoutsThisMinute`;
- `selfSkipsThisMinute`;
- `quarantinedHostsCount`.

## Self-Peer Diagnostics

`/debug/peers/self` shows:

- bind address;
- public host;
- normalized self aliases;
- known host count;
- skipped self count;
- whether the public host is treated as self.

The node must skip `0.0.0.0`, `127.0.0.1`, `localhost`, and `194.87.236.238` when they resolve to the local public node. Self-peers should be logged as `SELF_PEER_SKIPPED` with counts only.

## Snapshot Safety

Public tournament snapshots store DTOs only:

- index;
- miner address;
- hash;
- previous hash;
- timestamp;
- difficulty;
- transaction count;
- random/staking summary fields already computed.

They must not store full `Block` objects or transaction lists long-term.

