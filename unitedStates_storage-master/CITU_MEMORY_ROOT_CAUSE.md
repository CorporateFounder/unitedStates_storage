# CITU Node 1.0.14 Memory Diagnostic

## Release scope

`1.0.14` is a MEMORY DIAGNOSTIC release. It is not a consensus, reward,
PoW, staking, fork-height, or fail-closed blockchain validation fix.

The release keeps the existing diagnostic path around:

- `MemoryProbe` / `NodeMemoryProfiler`
- tournament `validateAndAddBlock`
- final tournament validation before `addBlock3`
- `/sub-blocks`
- `/nodes/resolve_from_to_block`
- `UtilsResolving.resolve3`
- `UtilsResolving.helpResolve`
- `Blockchain.checkFromFile`
- `BlockService.findBySpecialIndexBetween`
- `UtilsBlockToEntityBlock.entityBlocksToBlocks`

## What to inspect before OOM

Search node logs for `[MEMORY_PROBE]` and compare `traceId`, `stage`,
`usedMemory`, and `usedDeltaFromPreviousStage`. The most useful stages before
an OOM are:

- `resolve_conflict request received`
- `resolve_conflict after request body/block parsed`
- `resolve_conflict before tournament/local persistence`
- `tournament start`
- `getAllWinner / winnerList remote fetch start`
- `getAllWinner after winnerList parse`
- `tournament before winner selection`
- `tournament after sortWinner`
- `tournament before addBlock3`
- `addBlock3 start`
- `addBlock3 after balance calculation`
- `addBlock3 after database save`
- `addBlock3 after file save`
- `tournament after addBlock3`
- `tournament finish`

The suspected root-cause stage is usually the last stage before shutdown or
the stage with the largest positive `usedDeltaFromPreviousStage` in
`/debug/memory/forensics`.

## Bounded diagnostics

Diagnostic traces are compact: sizes, counters, stage names, trace ids, and
memory deltas are retained. Large payloads and collections are not kept.
`lastTraces` is a bounded ring buffer.

File diagnostics append JSONL line by line and are bounded by retention
settings:

- `citu.logs.cleanup.enabled=true`
- `citu.logs.cleanupEveryBlocks=100`
- `citu.logs.keepBlocks=5000`
- `citu.logs.maxFileBytes=52428800`
- `citu.logs.maxFiles=20`
- `citu.logs.dir=C:\server\logs`

Cleanup runs no more often than every `cleanupEveryBlocks` by latest block
index. It deletes only whitelisted diagnostic files older than the approximate
`keepBlocks` window and keeps no more than `maxFiles` per pattern. Oversized
JSONL/log diagnostic files are rotated safely. Blockchain data, `C:\resources`,
and business logs are not cleanup targets.
