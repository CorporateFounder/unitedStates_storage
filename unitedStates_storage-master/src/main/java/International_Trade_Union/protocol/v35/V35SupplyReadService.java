package International_Trade_Union.protocol.v35;

import International_Trade_Union.setings.Seting;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

@Service
public class V35SupplyReadService implements V35SupplySnapshotProvider {
    private static final Logger LOGGER = LoggerFactory.getLogger(V35SupplyReadService.class);
    private static final BigDecimal ZERO_GOLD = BigDecimal.ZERO.setScale(2);

    private final V35SupplyDataSource dataSource;
    private final int pageSize;
    private final long timeoutMillis;
    private final int maxBootstrapHeapPercent;
    private final Object rebuildLock = new Object();
    private final AtomicReference<V35SupplyContext> lastSuccessful = new AtomicReference<>();
    private final AtomicLong cacheHits = new AtomicLong();
    private final AtomicLong cacheMisses = new AtomicLong();
    private final AtomicLong rebuildWaitersTotal = new AtomicLong();
    private final AtomicInteger waitingRequests = new AtomicInteger();
    private final AtomicInteger activeRebuilds = new AtomicInteger();
    private final AtomicLong lastRebuildDurationMs = new AtomicLong();
    private final AtomicLong lastSuccessfulHeight = new AtomicLong(-1L);
    private final AtomicReference<String> lastErrorClass = new AtomicReference<>("");
    private final AtomicReference<String> lastErrorMessage = new AtomicReference<>("");
    private volatile InFlight inFlight;

    public V35SupplyReadService(
            V35SupplyDataSource dataSource,
            @Value("${citu.v35.supply.page-size:1000}") int pageSize,
            @Value("${citu.v35.supply.timeout-ms:30000}") long timeoutMillis,
            @Value("${citu.v35.supply.max-bootstrap-heap-percent:80}") int maxBootstrapHeapPercent) {
        this.dataSource = dataSource;
        this.pageSize = Math.max(1, Math.min(1000, pageSize));
        this.timeoutMillis = Math.max(1000L, timeoutMillis);
        this.maxBootstrapHeapPercent = Math.max(50, Math.min(95, maxBootstrapHeapPercent));
    }

    @Override
    public V35SupplyContext snapshot() {
        V35ChainTip requestedTip = dataSource.currentTip();
        V35SupplyContext cached = lastSuccessful.get();
        if (requestedTip.matches(cached)) {
            cacheHits.incrementAndGet();
            return cached;
        }

        InFlight flight;
        boolean rebuildOwner = false;
        synchronized (rebuildLock) {
            cached = lastSuccessful.get();
            if (requestedTip.matches(cached)) {
                cacheHits.incrementAndGet();
                return cached;
            }
            if (inFlight == null) {
                flight = new InFlight(requestedTip);
                inFlight = flight;
                rebuildOwner = true;
                cacheMisses.incrementAndGet();
                activeRebuilds.incrementAndGet();
            } else {
                flight = inFlight;
                rebuildWaitersTotal.incrementAndGet();
                waitingRequests.incrementAndGet();
            }
        }

        if (rebuildOwner) {
            return rebuild(flight);
        }
        try {
            return await(flight.future);
        } finally {
            waitingRequests.decrementAndGet();
        }
    }

    @Override
    public long currentHeight() {
        try {
            return dataSource.currentTip().getHeight();
        } catch (RuntimeException e) {
            V35SupplyContext cached = lastSuccessful.get();
            return cached == null ? -1L : cached.getCurrentHeight();
        }
    }

    @Override
    public Map<String, Object> diagnostics() {
        V35SupplyContext cached = lastSuccessful.get();
        Map<String, Object> status = new LinkedHashMap<>();
        status.put("activeSupplyRebuilds", activeRebuilds.get());
        status.put("waitingSupplyRequests", waitingRequests.get());
        status.put("supplyRebuildWaitersTotal", rebuildWaitersTotal.get());
        status.put("supplyCacheHits", cacheHits.get());
        status.put("supplyCacheMisses", cacheMisses.get());
        status.put("lastSupplyRebuildDurationMs", lastRebuildDurationMs.get());
        status.put("lastSuccessfulSupplyHeight", lastSuccessfulHeight.get());
        status.put("lastSupplyErrorClass", lastErrorClass.get());
        status.put("lastSupplyErrorMessage", lastErrorMessage.get());
        status.put("supplyCachePresent", cached != null);
        status.put("supplyCacheBlockHash", cached == null ? "UNKNOWN" : cached.getLatestBlockHash());
        status.put("supplyCacheCreatedAt", cached == null ? "" : cached.getCreatedAt().toString());
        status.put("supplyRowsScannedLastRebuild", cached == null ? 0L : cached.getRowsScanned());
        status.put("supplyPagesProcessedLastRebuild", cached == null ? 0 : cached.getPagesProcessed());
        return status;
    }

    private V35SupplyContext rebuild(InFlight flight) {
        long started = System.nanoTime();
        long heapBefore = usedMemoryMb();
        boolean success = false;
        V35SupplyContext result = null;
        Exception failure = null;
        try {
            result = compute(flight.tip, started);
            lastSuccessful.set(result);
            lastSuccessfulHeight.set(result.getCurrentHeight());
            lastErrorClass.set("");
            lastErrorMessage.set("");
            flight.future.complete(result);
            success = true;
            return result;
        } catch (Exception e) {
            failure = e;
            rememberError(e);
            flight.future.completeExceptionally(e);
            return fallbackOrThrow(e);
        } finally {
            long durationMs = elapsedMillis(started);
            lastRebuildDurationMs.set(durationMs);
            activeRebuilds.decrementAndGet();
            synchronized (rebuildLock) {
                if (inFlight == flight) {
                    inFlight = null;
                }
            }
            logComputation(flight.tip, result, durationMs, heapBefore, usedMemoryMb(), success, failure);
        }
    }

    private V35SupplyContext compute(V35ChainTip tip, long started) {
        V35SupplyContext previous = lastSuccessful.get();
        boolean incremental = canIncrement(previous, tip);
        if (!incremental && heapPressurePercent() >= maxBootstrapHeapPercent) {
            throw new V35SupplyUnavailableException(
                    "V35 supply bootstrap skipped because heap pressure is above safe threshold");
        }

        long fromHeight = incremental ? previous.getCurrentHeight() + 1L : 0L;
        BigDecimal totalMinted = incremental ? previous.getTotalMintedGold() : ZERO_GOLD;
        BigDecimal jackpotPaid = incremental ? previous.getJackpotPaidGold() : ZERO_GOLD;
        long rowsScanned = 0L;
        int pagesProcessed = 0;

        if (tip.getHeight() >= fromHeight) {
            int pageNumber = 0;
            boolean hasNext;
            do {
                ensureWithinTimeout(started);
                V35SupplyTransactionPage page = dataSource.basisTransactions(
                        fromHeight, tip.getHeight(), pageNumber, pageSize);
                pagesProcessed++;
                for (V35SupplyTransactionRow row : page.getRows()) {
                    if (row != null && Seting.BASIS_ADDRESS.equals(row.getSender())) {
                        BigDecimal amount = V35SupplyCalculator.gold(row.getDigitalDollar());
                        totalMinted = totalMinted.add(amount);
                        if (JackpotCalculator.isCanonicalPayoutAmount(row.getDigitalDollar())) {
                            jackpotPaid = jackpotPaid.add(amount);
                        }
                        rowsScanned++;
                    }
                }
                hasNext = page.hasNext();
                pageNumber++;
            } while (hasNext);
        }

        ensureWithinTimeout(started);
        BigDecimal founderBalance = dataSource.accountGoldBalance(Seting.ADDRESS_FOUNDER);
        boolean burnConfigured = BurnAddressPolicy.isBurnAddressConfigured();
        BigDecimal burnBalance = burnConfigured
                ? dataSource.accountGoldBalance(Seting.V35_BURN_ADDRESS)
                : ZERO_GOLD;
        V35SupplySnapshot snapshot = V35SupplyCalculator.snapshot(
                totalMinted,
                burnBalance,
                founderBalance,
                jackpotPaid,
                burnConfigured);
        return new V35SupplyContext(
                snapshot,
                tip.getHeight(),
                tip.getBlockHash(),
                Instant.now(),
                incremental ? "bounded_incremental_projection" : "bounded_projection_bootstrap",
                false,
                tip.getHeight(),
                rowsScanned,
                pagesProcessed);
    }

    private boolean canIncrement(V35SupplyContext previous, V35ChainTip tip) {
        if (previous == null
                || previous.getCurrentHeight() < 0
                || tip.getHeight() <= previous.getCurrentHeight()) {
            return false;
        }
        String canonicalPreviousHash = dataSource.blockHashAt(previous.getCurrentHeight());
        return previous.getLatestBlockHash().equals(canonicalPreviousHash);
    }

    private V35SupplyContext await(CompletableFuture<V35SupplyContext> future) {
        try {
            return future.get(timeoutMillis, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return fallbackOrThrow(e);
        } catch (ExecutionException | TimeoutException e) {
            return fallbackOrThrow(e);
        }
    }

    private V35SupplyContext fallbackOrThrow(Exception failure) {
        V35SupplyContext cached = lastSuccessful.get();
        if (cached != null) {
            return cached.asStale("last_successful_snapshot_after_error");
        }
        throw new V35SupplyUnavailableException(
                "V35 supply snapshot is temporarily unavailable", failure);
    }

    private void ensureWithinTimeout(long started) {
        if (elapsedMillis(started) > timeoutMillis) {
            throw new V35SupplyUnavailableException("V35 supply snapshot computation timed out");
        }
    }

    private void rememberError(Exception error) {
        lastErrorClass.set(error.getClass().getSimpleName());
        String message = error.getMessage() == null ? "" : error.getMessage();
        lastErrorMessage.set(message.length() <= 200 ? message : message.substring(0, 200));
    }

    private void logComputation(V35ChainTip tip,
                                V35SupplyContext result,
                                long durationMs,
                                long heapBeforeMb,
                                long heapAfterMb,
                                boolean success,
                                Exception failure) {
        LOGGER.info("V35_SUPPLY_SNAPSHOT height={} blockHash={} cacheHit=false rebuildStarted=true "
                        + "rebuildWaiters={} durationMs={} heapBeforeMb={} heapAfterMb={} rowsScanned={} "
                        + "pagesProcessed={} lastProcessedHeight={} snapshotStale={} success={} error={}",
                tip.getHeight(),
                tip.getBlockHash(),
                rebuildWaitersTotal.get(),
                durationMs,
                heapBeforeMb,
                heapAfterMb,
                result == null ? 0L : result.getRowsScanned(),
                result == null ? 0 : result.getPagesProcessed(),
                result == null ? -1L : result.getLastProcessedHeight(),
                result != null && result.isStale(),
                success,
                failure == null ? "" : failure.getClass().getSimpleName());
    }

    private static long elapsedMillis(long started) {
        return TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - started);
    }

    private static long usedMemoryMb() {
        Runtime runtime = Runtime.getRuntime();
        return (runtime.totalMemory() - runtime.freeMemory()) / (1024L * 1024L);
    }

    private static int heapPressurePercent() {
        Runtime runtime = Runtime.getRuntime();
        long max = runtime.maxMemory();
        if (max <= 0L) {
            return 0;
        }
        long used = runtime.totalMemory() - runtime.freeMemory();
        return (int) Math.min(100L, (used * 100L) / max);
    }

    private static final class InFlight {
        private final V35ChainTip tip;
        private final CompletableFuture<V35SupplyContext> future = new CompletableFuture<>();

        private InFlight(V35ChainTip tip) {
            this.tip = tip;
        }
    }
}
