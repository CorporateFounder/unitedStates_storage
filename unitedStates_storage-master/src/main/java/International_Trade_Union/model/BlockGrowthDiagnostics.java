package International_Trade_Union.model;

import International_Trade_Union.entity.blockchain.block.Block;

import java.time.Instant;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public final class BlockGrowthDiagnostics {
    static final String DIAGNOSTIC_TRACES_PROPERTY = "citu.diagnostic.traces.enabled";
    private static final String DISABLED_TRACE_ID = "disabled";
    private static final int MAX_TRACES = 3;
    private static final int MAX_TEXT = 96;
    private static final Object LOCK = new Object();

    private static final Deque<Map<String, Object>> SUBMIT_TRACES = new ArrayDeque<>();
    private static final Deque<Map<String, Object>> TOURNAMENT_TRACES = new ArrayDeque<>();
    private static final Deque<Map<String, Object>> ADD_BLOCK_TRACES = new ArrayDeque<>();

    private static volatile String lastSubmitTraceId = "none";
    private static volatile String lastTournamentTraceId = "none";
    private static volatile String lastAddBlock3TraceId = "none";
    private static volatile long lastReceivedBlockIndex = -1;
    private static volatile String lastValidationResult = "none";
    private static volatile String lastRejectReason = "none";
    private static volatile long lastQueuedBlockIndex = -1;
    private static volatile int lastQueueSizeAfterAdd = -1;
    private static volatile int lastTournamentCandidateCount = -1;
    private static volatile long lastTournamentWinnerIndex = -1;
    private static volatile String lastAddBlock3StartedAt = "never";
    private static volatile String lastAddBlock3FinishedAt = "never";
    private static volatile String lastAddBlock3Result = "none";
    private static volatile long lastAddedBlockIndex = -1;
    private static volatile long lastSizeBeforeAdd = -1;
    private static volatile long lastSizeAfterAdd = -1;
    private static volatile long lastSizeDelta = 0;
    private static volatile String lastSubmitError = "none";
    private static volatile String lastNoGrowthReason = "none";

    private BlockGrowthDiagnostics() {
    }

    public static String startSubmit(String endpointPath,
                                     String remoteIp,
                                     long requestBytes,
                                     Block candidateBlock,
                                     long sizeBefore,
                                     int queueSizeBefore) {
        String traceId = newTraceId("submit");
        Map<String, Object> trace = baseTrace(traceId, "wallet-submit");
        trace.put("receivedAt", trace.get("startedAt"));
        trace.put("endpointPath", safeText(endpointPath));
        trace.put("remoteIp", safeText(remoteIp));
        trace.put("requestBytes", requestBytes);
        trace.put("candidateBlockIndex", blockIndex(candidateBlock));
        trace.put("candidateHashPrefix", prefix(candidateBlock == null ? null : candidateBlock.getHashBlock()));
        trace.put("candidatePreviousHashPrefix", prefix(candidateBlock == null ? null : candidateBlock.getPreviousHash()));
        trace.put("candidateMinerPrefix", prefix(candidateBlock == null ? null : candidateBlock.getMinerAddress()));
        trace.put("candidateTxCount", candidateBlock == null || candidateBlock.getDtoTransactions() == null ? 0 : candidateBlock.getDtoTransactions().size());
        trace.put("sizeBefore", sizeBefore);
        trace.put("queueSizeBefore", queueSizeBefore);
        trace.put("memoryBefore", usedMemory());
        trace.put("validationResult", "pending");
        trace.put("rejectReason", "none");
        if (tracesEnabled()) {
            synchronized (LOCK) {
                SUBMIT_TRACES.addLast(trace);
                trim(SUBMIT_TRACES);
            }
        } else {
            traceId = DISABLED_TRACE_ID;
        }
        lastSubmitTraceId = traceId;
        lastReceivedBlockIndex = blockIndex(candidateBlock);
        lastSizeBeforeAdd = sizeBefore;
        lastValidationResult = "received";
        lastRejectReason = "validation pending";
        lastSubmitError = "none";
        lastNoGrowthReason = "SUBMIT_RECEIVED_NOT_QUEUED";
        return traceId;
    }

    public static void submitValidationStarted(String traceId) {
        updateTrace(SUBMIT_TRACES, traceId, trace -> {
            trace.put("validationStartedAt", now());
            trace.put("memoryAtValidationStart", usedMemory());
        });
    }

    public static void submitValidationFinished(String traceId, String result, String reason) {
        updateTrace(SUBMIT_TRACES, traceId, trace -> {
            trace.put("validationFinishedAt", now());
            trace.put("validationResult", safeText(result));
            trace.put("rejectReason", safeText(reason));
            trace.put("memoryAtValidationFinish", usedMemory());
        });
        lastValidationResult = safeText(result);
        lastRejectReason = safeText(reason);
        if (!"valid".equalsIgnoreCase(String.valueOf(result))) {
            lastNoGrowthReason = "SUBMIT_RECEIVED_VALIDATION_FAILED";
        }
    }

    public static void submitValidationFinished(String result, String reason) {
        submitValidationFinished(lastSubmitTraceId, result, reason);
    }

    public static void submitQueued(String traceId, Block queuedBlock, int queueSizeAfterAdd) {
        updateTrace(SUBMIT_TRACES, traceId, trace -> {
            trace.put("queuedAt", now());
            trace.put("queuedBlockIndex", blockIndex(queuedBlock));
            trace.put("queuedBlockHashPrefix", prefix(queuedBlock == null ? null : queuedBlock.getHashBlock()));
            trace.put("queueSizeAfterAdd", queueSizeAfterAdd);
            trace.put("memoryAfterQueue", usedMemory());
            trace.put("noGrowthReason", "QUEUED_NOT_CONSUMED");
        });
        lastQueuedBlockIndex = blockIndex(queuedBlock);
        lastQueueSizeAfterAdd = queueSizeAfterAdd;
        lastNoGrowthReason = "QUEUED_NOT_CONSUMED";
    }

    public static void localPersistenceStarted(Block block) {
        updateTrace(SUBMIT_TRACES, lastSubmitTraceId, trace -> {
            trace.put("localPersistenceStartedAt", now());
            trace.put("localPersistenceResult", "started");
            trace.put("localPersistenceBlockIndex", blockIndex(block));
            trace.put("memoryAtLocalPersistenceStart", usedMemory());
        });
    }

    public static void localPersistenceFinished(Block block, long sizeBefore, long sizeAfter, String result) {
        updateTrace(SUBMIT_TRACES, lastSubmitTraceId, trace -> {
            trace.put("localPersistenceFinishedAt", now());
            trace.put("localPersistenceResult", safeText(result));
            trace.put("addedBlockIndex", blockIndex(block));
            trace.put("sizeAfter", sizeAfter);
            trace.put("sizeDelta", sizeAfter - sizeBefore);
            trace.put("memoryAtLocalPersistenceFinish", usedMemory());
            trace.put("noGrowthReason", sizeAfter > sizeBefore ? "BLOCK_GROWTH_CONFIRMED" : "ADD_SUCCEEDED_SIZE_NOT_CHANGED");
        });
        lastAddedBlockIndex = blockIndex(block);
        lastSizeBeforeAdd = sizeBefore;
        lastSizeAfterAdd = sizeAfter;
        lastSizeDelta = sizeAfter - sizeBefore;
        lastNoGrowthReason = sizeAfter > sizeBefore ? "BLOCK_GROWTH_CONFIRMED" : "ADD_SUCCEEDED_SIZE_NOT_CHANGED";
    }

    public static void localPersistenceFinished(Block block, long sizeAfter, String result) {
        localPersistenceFinished(block, lastSizeBeforeAdd < 0 ? sizeAfter : lastSizeBeforeAdd, sizeAfter, result);
    }

    public static String startTournament(long sizeBefore, int queueSizeAtStart) {
        String traceId = newTraceId("tournament");
        Map<String, Object> trace = baseTrace(traceId, "tournament");
        trace.put("tournamentStartAt", trace.get("startedAt"));
        trace.put("sizeBefore", sizeBefore);
        trace.put("queueSizeAtStart", queueSizeAtStart);
        trace.put("candidateQueueSizeAtStart", queueSizeAtStart);
        trace.put("memoryBefore", usedMemory());
        trace.put("skipReason", "none");
        trace.put("noGrowthReason", queueSizeAtStart == 0 ? "NO_CANDIDATE_BLOCKS" : "QUEUED_NOT_CONSUMED");
        if (tracesEnabled()) {
            synchronized (LOCK) {
                TOURNAMENT_TRACES.addLast(trace);
                trim(TOURNAMENT_TRACES);
            }
        } else {
            traceId = DISABLED_TRACE_ID;
        }
        lastTournamentTraceId = traceId;
        lastTournamentCandidateCount = queueSizeAtStart;
        if (queueSizeAtStart == 0) {
            lastNoGrowthReason = "NO_CANDIDATE_BLOCKS";
        } else {
            lastNoGrowthReason = "QUEUED_NOT_CONSUMED";
        }
        return traceId;
    }

    public static void tournamentCandidates(String traceId, int candidateCount) {
        updateTrace(TOURNAMENT_TRACES, traceId, trace -> {
            trace.put("tournamentSeenAt", now());
            trace.put("tournamentCandidateCount", candidateCount);
            trace.put("candidateCount", candidateCount);
            trace.put("memoryAfterCandidateFilter", usedMemory());
        });
        lastTournamentCandidateCount = candidateCount;
        if (candidateCount == 0) {
            lastNoGrowthReason = "NO_CANDIDATE_BLOCKS";
        }
    }

    public static void tournamentWinnerSelected(String traceId, Block winner, int sortedWinnerCount) {
        updateTrace(TOURNAMENT_TRACES, traceId, trace -> {
            trace.put("winnerSelectedAt", now());
            trace.put("winnerSelected", winner != null);
            trace.put("winnerBlockIndex", blockIndex(winner));
            trace.put("winnerIndex", blockIndex(winner));
            trace.put("winnerHashPrefix", prefix(winner == null ? null : winner.getHashBlock()));
            trace.put("sortedWinnerCount", sortedWinnerCount);
            trace.put("memoryAfterWinnerSelection", usedMemory());
        });
        lastTournamentWinnerIndex = blockIndex(winner);
    }

    public static void tournamentSkip(String traceId, String reason) {
        updateTrace(TOURNAMENT_TRACES, traceId, trace -> {
            trace.put("skipReason", safeText(reason));
            trace.put("endedAt", now());
            trace.put("tournamentEndAt", trace.get("endedAt"));
            trace.put("memoryAfterSkip", usedMemory());
        });
        lastNoGrowthReason = safeText(reason);
    }

    public static void tournamentAddBlock3Result(String traceId, boolean result, long sizeBefore, long sizeAfter) {
        updateTrace(TOURNAMENT_TRACES, traceId, trace -> {
            trace.put("addBlock3FinishedAt", now());
            trace.put("addBlock3Called", true);
            trace.put("addBlock3Result", result);
            trace.put("sizeBeforeAdd", sizeBefore);
            trace.put("sizeAfterAdd", sizeAfter);
            trace.put("sizeDelta", sizeAfter - sizeBefore);
            trace.put("memoryAfterAddBlock3", usedMemory());
        });
        lastAddBlock3Result = String.valueOf(result);
        lastSizeBeforeAdd = sizeBefore;
        lastSizeAfterAdd = sizeAfter;
        lastSizeDelta = sizeAfter - sizeBefore;
        if (!result) {
            lastNoGrowthReason = "ADD_BLOCK3_FAILED";
        } else if (sizeAfter == sizeBefore) {
            lastNoGrowthReason = "ADD_SUCCEEDED_SIZE_NOT_CHANGED";
        } else {
            lastNoGrowthReason = "BLOCK_GROWTH_CONFIRMED";
        }
    }

    public static void finishTournament(String traceId, long sizeAfter, int queueSizeAtEnd) {
        updateTrace(TOURNAMENT_TRACES, traceId, trace -> {
            trace.put("endedAt", now());
            trace.put("tournamentEndAt", trace.get("endedAt"));
            trace.put("sizeAfter", sizeAfter);
            trace.put("queueSizeAtEnd", queueSizeAtEnd);
            trace.put("candidateQueueSizeAtEnd", queueSizeAtEnd);
            trace.put("memoryAfter", usedMemory());
            Object memoryBefore = trace.get("memoryBefore");
            if (memoryBefore instanceof Number) {
                trace.put("memoryDelta", usedMemory() - ((Number) memoryBefore).longValue());
            }
            trace.put("nextTournamentWaitMs", "unknown");
        });
    }

    public static String startAddBlock3(List<Block> blocks, long chainSizeBefore) {
        String traceId = newTraceId("addBlock3");
        Map<String, Object> trace = baseTrace(traceId, "addBlock3");
        trace.put("startAt", trace.get("startedAt"));
        Block first = blocks == null || blocks.isEmpty() ? null : blocks.get(0);
        trace.put("inputBlockCount", blocks == null ? 0 : blocks.size());
        trace.put("inputBlockIndex", blockIndex(first));
        trace.put("inputHashPrefix", prefix(first == null ? null : first.getHashBlock()));
        trace.put("chainSizeBefore", chainSizeBefore);
        trace.put("memoryBefore", usedMemory());
        if (tracesEnabled()) {
            synchronized (LOCK) {
                ADD_BLOCK_TRACES.addLast(trace);
                trim(ADD_BLOCK_TRACES);
            }
        } else {
            traceId = DISABLED_TRACE_ID;
        }
        lastAddBlock3TraceId = traceId;
        lastAddBlock3StartedAt = now();
        lastSizeBeforeAdd = chainSizeBefore;
        lastNoGrowthReason = "ADD_BLOCK3_STARTED";
        return traceId;
    }

    public static void addBlock3Stage(String traceId, String stageName, Map<String, ?> metrics) {
        updateTrace(ADD_BLOCK_TRACES, traceId, trace -> {
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> stages = (List<Map<String, Object>>) trace.computeIfAbsent("stages", ignored -> new ArrayList<Map<String, Object>>());
            Map<String, Object> stage = new LinkedHashMap<>();
            String safeStageName = safeText(stageName);
            stage.put("stageName", safeStageName);
            stage.put("at", now());
            stage.put("usedMemory", usedMemory());
            stage.put("metrics", sanitizeMap(metrics));
            stages.add(stage);
            trace.put("lastStage", safeStageName);
            while (stages.size() > 12) {
                stages.remove(0);
            }
            if ("balance calculation started".equals(safeStageName)) {
                trace.put("calculateBalanceStartedAt", stage.get("at"));
            } else if ("balance calculation finished".equals(safeStageName)) {
                trace.put("calculateBalanceFinishedAt", stage.get("at"));
            } else if ("database save started".equals(safeStageName)) {
                trace.put("dbSaveStartedAt", stage.get("at"));
                Object count = null;
                if (stage.get("metrics") instanceof Map) {
                    Map<?, ?> metricMap = (Map<?, ?>) stage.get("metrics");
                    count = metricMap.containsKey("accountListSize") ? metricMap.get("accountListSize") : metricMap.get("accountSaveCount");
                }
                trace.put("accountSaveCount", count == null ? "unknown" : count);
            } else if ("database save finished".equals(safeStageName)) {
                trace.put("dbSaveFinishedAt", stage.get("at"));
            } else if ("file save started".equals(safeStageName)) {
                trace.put("fileSaveStartedAt", stage.get("at"));
            } else if ("file save finished".equals(safeStageName)) {
                trace.put("fileSaveFinishedAt", stage.get("at"));
            }
        });
    }

    public static void finishAddBlock3(String traceId, boolean result, long chainSizeAfter, Block addedBlock, Throwable error) {
        updateTrace(ADD_BLOCK_TRACES, traceId, trace -> {
            trace.put("endedAt", now());
            trace.put("endAt", trace.get("endedAt"));
            trace.put("result", result);
            trace.put("chainSizeAfter", chainSizeAfter);
            Object before = trace.get("chainSizeBefore");
            long beforeSize = before instanceof Number ? ((Number) before).longValue() : -1L;
            trace.put("chainSizeDelta", beforeSize < 0 ? "unknown" : chainSizeAfter - beforeSize);
            trace.put("addedBlockIndex", blockIndex(addedBlock));
            trace.put("addedBlockHashPrefix", prefix(addedBlock == null ? null : addedBlock.getHashBlock()));
            trace.put("memoryAfter", usedMemory());
            Object memoryBefore = trace.get("memoryBefore");
            if (memoryBefore instanceof Number) {
                trace.put("memoryDelta", usedMemory() - ((Number) memoryBefore).longValue());
            }
            trace.put("temporaryBlockchainSize", "unknown");
            if (error != null) {
                trace.put("errorClass", error.getClass().getSimpleName());
                trace.put("errorMessage", safeText(error.getMessage()));
            }
        });
        lastAddBlock3FinishedAt = now();
        lastAddBlock3Result = String.valueOf(result);
        lastAddedBlockIndex = blockIndex(addedBlock);
        lastSizeAfterAdd = chainSizeAfter;
        lastSizeDelta = lastSizeBeforeAdd < 0 ? 0 : chainSizeAfter - lastSizeBeforeAdd;
        if (error != null) {
            lastSubmitError = error.getClass().getSimpleName() + ": " + safeText(error.getMessage());
            lastNoGrowthReason = "ADD_BLOCK3_FAILED";
        } else if (!result) {
            lastNoGrowthReason = "ADD_BLOCK3_FAILED";
        } else if (lastSizeDelta == 0) {
            lastNoGrowthReason = "ADD_SUCCEEDED_SIZE_NOT_CHANGED";
        } else {
            lastNoGrowthReason = "BLOCK_GROWTH_CONFIRMED";
        }
    }

    public static Map<String, Object> submitStatus() {
        Map<String, Object> status = new LinkedHashMap<>();
        status.put("lastSubmitTraceId", lastSubmitTraceId);
        status.put("lastTournamentTraceId", lastTournamentTraceId);
        status.put("lastAddBlock3TraceId", lastAddBlock3TraceId);
        status.put("lastReceivedBlockIndex", lastReceivedBlockIndex);
        status.put("lastValidationResult", lastValidationResult);
        status.put("lastRejectReason", lastRejectReason);
        status.put("lastQueuedBlockIndex", lastQueuedBlockIndex);
        status.put("lastQueueSizeAfterAdd", lastQueueSizeAfterAdd);
        status.put("lastTournamentCandidateCount", lastTournamentCandidateCount);
        status.put("lastTournamentWinnerIndex", lastTournamentWinnerIndex);
        status.put("lastAddBlock3StartedAt", lastAddBlock3StartedAt);
        status.put("lastAddBlock3FinishedAt", lastAddBlock3FinishedAt);
        status.put("lastAddBlock3Result", lastAddBlock3Result);
        status.put("lastAddedBlockIndex", lastAddedBlockIndex);
        status.put("lastSizeBeforeAdd", lastSizeBeforeAdd);
        status.put("lastSizeAfterAdd", lastSizeAfterAdd);
        status.put("lastSizeDelta", lastSizeDelta);
        status.put("lastSubmitError", lastSubmitError);
        status.put("lastNoGrowthReason", lastNoGrowthReason);
        return status;
    }

    public static List<Map<String, Object>> submitTraces() {
        if (!tracesEnabled()) {
            return Collections.emptyList();
        }
        return copy(SUBMIT_TRACES);
    }

    public static List<Map<String, Object>> tournamentTraces() {
        if (!tracesEnabled()) {
            return Collections.emptyList();
        }
        return copy(TOURNAMENT_TRACES);
    }

    public static List<Map<String, Object>> addBlock3Traces() {
        if (!tracesEnabled()) {
            return Collections.emptyList();
        }
        return copy(ADD_BLOCK_TRACES);
    }

    public static Map<String, Object> memoryForensicsFields() {
        Map<String, Object> fields = new LinkedHashMap<>();
        fields.put("submitTraceId", lastSubmitTraceId);
        fields.put("tournamentTraceId", lastTournamentTraceId);
        fields.put("addBlock3TraceId", lastAddBlock3TraceId);
        fields.put("suspectedNoGrowthReason", lastNoGrowthReason);
        fields.put("lastSubmitError", lastSubmitError);
        fields.put("diagnosticTracesEnabled", tracesEnabled());
        return fields;
    }

    public static void clearForTests() {
        synchronized (LOCK) {
            SUBMIT_TRACES.clear();
            TOURNAMENT_TRACES.clear();
            ADD_BLOCK_TRACES.clear();
        }
        lastSubmitTraceId = "none";
        lastTournamentTraceId = "none";
        lastAddBlock3TraceId = "none";
        lastReceivedBlockIndex = -1;
        lastValidationResult = "none";
        lastRejectReason = "none";
        lastQueuedBlockIndex = -1;
        lastQueueSizeAfterAdd = -1;
        lastTournamentCandidateCount = -1;
        lastTournamentWinnerIndex = -1;
        lastAddBlock3StartedAt = "never";
        lastAddBlock3FinishedAt = "never";
        lastAddBlock3Result = "none";
        lastAddedBlockIndex = -1;
        lastSizeBeforeAdd = -1;
        lastSizeAfterAdd = -1;
        lastSizeDelta = 0;
        lastSubmitError = "none";
        lastNoGrowthReason = "none";
    }

    private static Map<String, Object> baseTrace(String traceId, String type) {
        Map<String, Object> trace = new LinkedHashMap<>();
        trace.put("traceId", traceId);
        trace.put("type", type);
        trace.put("startedAt", now());
        return trace;
    }

    private static String newTraceId(String prefix) {
        return prefix + "-" + UUID.randomUUID();
    }

    private static void updateTrace(Deque<Map<String, Object>> traces, String traceId, TraceUpdate update) {
        if (!tracesEnabled() || traceId == null || traceId.isBlank() || DISABLED_TRACE_ID.equals(traceId)) {
            return;
        }
        synchronized (LOCK) {
            for (Map<String, Object> trace : traces) {
                if (traceId.equals(trace.get("traceId"))) {
                    update.apply(trace);
                    return;
                }
            }
        }
    }

    public static boolean tracesEnabled() {
        return Boolean.getBoolean(DIAGNOSTIC_TRACES_PROPERTY);
    }

    private static List<Map<String, Object>> copy(Deque<Map<String, Object>> traces) {
        synchronized (LOCK) {
            List<Map<String, Object>> result = new ArrayList<>();
            for (Map<String, Object> trace : traces) {
                result.add(new LinkedHashMap<>(trace));
            }
            return result;
        }
    }

    private static void trim(Deque<Map<String, Object>> traces) {
        while (traces.size() > MAX_TRACES) {
            traces.removeFirst();
        }
    }

    private static long blockIndex(Block block) {
        return block == null ? -1 : block.getIndex();
    }

    private static String prefix(Object value) {
        if (value == null) {
            return "none";
        }
        String text = String.valueOf(value);
        if (text.length() <= 16) {
            return text;
        }
        return text.substring(0, 16);
    }

    private static String safeText(Object value) {
        if (value == null) {
            return "none";
        }
        String text = String.valueOf(value).replace('\n', ' ').replace('\r', ' ');
        if (text.length() <= MAX_TEXT) {
            return text;
        }
        return text.substring(0, MAX_TEXT) + "...[truncated]";
    }

    private static Map<String, Object> sanitizeMap(Map<String, ?> metrics) {
        if (metrics == null || metrics.isEmpty()) {
            return Collections.emptyMap();
        }
        Map<String, Object> result = new LinkedHashMap<>();
        metrics.forEach((key, value) -> {
            if (value instanceof Number || value instanceof Boolean) {
                result.put(String.valueOf(key), value);
            } else {
                result.put(String.valueOf(key), safeText(value));
            }
        });
        return result;
    }

    private static long usedMemory() {
        Runtime runtime = Runtime.getRuntime();
        return runtime.totalMemory() - runtime.freeMemory();
    }

    private static String now() {
        return Instant.now().toString();
    }

    private interface TraceUpdate {
        void apply(Map<String, Object> trace);
    }
}
