package International_Trade_Union.protocol.v35;

import International_Trade_Union.controllers.V35SupplyController;
import International_Trade_Union.setings.Seting;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class V35SupplyReadServiceTest {
    private ExecutorService executor;

    @AfterEach
    void stopExecutor() {
        if (executor != null) {
            executor.shutdownNow();
        }
    }

    @Test
    void fourConcurrentV35RequestsTriggerExactlyOneCalculation() throws Exception {
        FakeDataSource source = FakeDataSource.withRows(5L, "hash5",
                row(5L, 8.0));
        source.blockNextPage();
        V35SupplyReadService service = service(source);
        V35SupplyController controller = new V35SupplyController(service);
        executor = Executors.newFixedThreadPool(4);

        List<Future<Map<String, Object>>> futures = List.of(
                executor.submit(controller::supply),
                executor.submit(controller::burn),
                executor.submit(controller::founder),
                executor.submit(controller::founderBurnSnapshot));

        assertTrue(source.pageStarted.await(5, TimeUnit.SECONDS));
        source.releasePage.countDown();
        for (Future<Map<String, Object>> future : futures) {
            assertFalse(future.get(5, TimeUnit.SECONDS).isEmpty());
        }

        assertEquals(1, source.pageRequests.get());
        assertEquals(1L, ((Number) service.diagnostics().get("supplyCacheMisses")).longValue());
        assertTrue(((Number) service.diagnostics().get("supplyRebuildWaitersTotal")).longValue() >= 1L);
    }

    @Test
    void identicalTipUsesCacheWithoutSecondScan() {
        FakeDataSource source = FakeDataSource.withRows(1L, "hash1", row(1L, 10.0));
        V35SupplyReadService service = service(source);

        V35SupplyContext first = service.snapshot();
        V35SupplyContext second = service.snapshot();

        assertEquals(first, second);
        assertEquals(1, source.pageRequests.get());
        assertEquals(1L, ((Number) service.diagnostics().get("supplyCacheHits")).longValue());
    }

    @Test
    void heightChangeProcessesOnlyNewProjectionRows() {
        FakeDataSource source = FakeDataSource.withRows(1L, "hash1", row(1L, 10.0));
        V35SupplyReadService service = service(source);
        assertEquals(new BigDecimal("10.00"), service.snapshot().getTotalMintedGold());

        source.addRow(row(2L, 5.0));
        source.setTip(2L, "hash2");
        V35SupplyContext updated = service.snapshot();

        assertEquals(new BigDecimal("15.00"), updated.getTotalMintedGold());
        assertEquals(2L, source.lastFromHeight);
        assertEquals("bounded_incremental_projection", updated.getSource());
    }

    @Test
    void blockHashChangeAtSameHeightInvalidatesCacheAndRebuilds() {
        FakeDataSource source = FakeDataSource.withRows(1L, "hash1", row(1L, 10.0));
        V35SupplyReadService service = service(source);
        service.snapshot();

        source.setTip(1L, "replacement-hash1");
        V35SupplyContext rebuilt = service.snapshot();

        assertEquals(0L, source.lastFromHeight);
        assertEquals("replacement-hash1", rebuilt.getLatestBlockHash());
        assertEquals("bounded_projection_bootstrap", rebuilt.getSource());
        assertEquals(2, source.pageRequests.get());
    }

    @Test
    void failedRebuildReturnsLastSuccessfulSnapshotAsStale() {
        FakeDataSource source = FakeDataSource.withRows(1L, "hash1", row(1L, 10.0));
        V35SupplyReadService service = service(source);
        V35SupplyContext first = service.snapshot();

        source.setTip(2L, "hash2");
        source.failTransactions = true;
        V35SupplyContext fallback = service.snapshot();

        assertEquals(first.getTotalMintedGold(), fallback.getTotalMintedGold());
        assertTrue(fallback.isStale());
        assertEquals("last_successful_snapshot_after_error", fallback.getSource());
        assertEquals("IllegalStateException", service.diagnostics().get("lastSupplyErrorClass"));
    }

    @Test
    void failedInitialRebuildReturnsControlledUnavailableAndFutureIsRemoved() {
        FakeDataSource source = FakeDataSource.withRows(1L, "hash1", row(1L, 10.0));
        source.failTransactions = true;
        V35SupplyReadService service = service(source);

        assertThrows(V35SupplyUnavailableException.class, service::snapshot);

        source.failTransactions = false;
        assertEquals(new BigDecimal("10.00"), service.snapshot().getTotalMintedGold());
        assertEquals(2L, ((Number) service.diagnostics().get("supplyCacheMisses")).longValue());
    }

    @Test
    void largeSyntheticHistoryIsProcessedInBoundedPages() {
        List<V35SupplyTransactionRow> rows = new ArrayList<>();
        for (int i = 0; i < 2500; i++) {
            rows.add(row(i, 1.0));
        }
        FakeDataSource source = FakeDataSource.withRows(2499L, "hash2499",
                rows.toArray(new V35SupplyTransactionRow[0]));
        V35SupplyReadService service = new V35SupplyReadService(source, 1000, 30_000L, 95);

        V35SupplyContext context = service.snapshot();

        assertEquals(new BigDecimal("2500.00"), context.getTotalMintedGold());
        assertEquals(2500L, context.getRowsScanned());
        assertEquals(3, context.getPagesProcessed());
        assertEquals(1000, source.maxRequestedPageSize);
        assertEquals(3, source.pageRequests.get());
    }

    @Test
    void projectionAggregationPreservesExistingMintAndJackpotSemantics() {
        double canonicalJackpot = JackpotCalculator.allExpectedPayoutAmounts()
                .values().iterator().next().doubleValue();
        FakeDataSource source = FakeDataSource.withRows(2L, "hash2",
                row(1L, 1.005),
                row(2L, canonicalJackpot));
        V35SupplyContext context = service(source).snapshot();

        BigDecimal expectedTotal = V35SupplyCalculator.gold(1.005)
                .add(V35SupplyCalculator.gold(canonicalJackpot));
        assertEquals(expectedTotal, context.getTotalMintedGold());
        assertEquals(V35SupplyCalculator.gold(canonicalJackpot), context.getJackpotPaidGold());
    }

    @Test
    void accountReadsAreAddressSpecificAndPlaceholderBurnIsNotQueried() {
        FakeDataSource source = FakeDataSource.withRows(0L, "hash0", row(0L, 1.0));
        source.founderBalance = new BigDecimal("100.00");

        V35SupplyContext context = service(source).snapshot();

        assertEquals(new BigDecimal("100.00"), context.getSnapshot().getFounderReserveGold());
        assertEquals(Collections.singletonList(Seting.ADDRESS_FOUNDER), source.accountRequests);
        assertFalse(source.accountRequests.contains(Seting.V35_BURN_ADDRESS));
    }

    @Test
    void jpaReadModelUsesScalarQueriesAndNeverEntityGraphs() throws IOException {
        String source = Files.readString(Path.of(
                "src", "main", "java", "International_Trade_Union", "protocol", "v35",
                "JpaV35SupplyDataSource.java"));

        assertTrue(source.contains("SELECT t.sender, t.digitalDollar, t.entityBlock.specialIndex"));
        assertTrue(source.contains("SELECT a.digitalDollarBalance"));
        assertTrue(source.contains("SELECT b.specialIndex, b.hashBlock"));
        assertFalse(source.contains("findBySpecialIndexBetween"));
        assertFalse(source.contains("findAllAccounts"));
        assertFalse(source.contains("List<EntityBlock>"));
        assertFalse(source.contains("List<Block>"));
        assertFalse(source.contains("List<DtoTransaction>"));
    }

    private static V35SupplyReadService service(FakeDataSource source) {
        return new V35SupplyReadService(source, 1000, 30_000L, 95);
    }

    private static V35SupplyTransactionRow row(long height, double amount) {
        return new V35SupplyTransactionRow(Seting.BASIS_ADDRESS, amount, height);
    }

    private static final class FakeDataSource implements V35SupplyDataSource {
        private final List<V35SupplyTransactionRow> rows =
                Collections.synchronizedList(new ArrayList<>());
        private final Map<Long, String> hashes = new java.util.concurrent.ConcurrentHashMap<>();
        private final AtomicInteger pageRequests = new AtomicInteger();
        private final List<String> accountRequests =
                Collections.synchronizedList(new ArrayList<>());
        private volatile V35ChainTip tip;
        private volatile boolean failTransactions;
        private volatile BigDecimal founderBalance = BigDecimal.ZERO.setScale(2);
        private volatile long lastFromHeight = -1L;
        private volatile int maxRequestedPageSize;
        private volatile CountDownLatch pageStarted = new CountDownLatch(0);
        private volatile CountDownLatch releasePage = new CountDownLatch(0);

        static FakeDataSource withRows(long height,
                                       String hash,
                                       V35SupplyTransactionRow... initialRows) {
            FakeDataSource source = new FakeDataSource();
            source.setTip(height, hash);
            Collections.addAll(source.rows, initialRows);
            return source;
        }

        void setTip(long height, String hash) {
            this.tip = new V35ChainTip(height, hash);
            hashes.put(height, hash);
        }

        void addRow(V35SupplyTransactionRow row) {
            rows.add(row);
        }

        void blockNextPage() {
            pageStarted = new CountDownLatch(1);
            releasePage = new CountDownLatch(1);
        }

        @Override
        public V35ChainTip currentTip() {
            return tip;
        }

        @Override
        public String blockHashAt(long height) {
            return hashes.getOrDefault(height, "UNKNOWN");
        }

        @Override
        public V35SupplyTransactionPage basisTransactions(long fromHeight,
                                                          long toHeight,
                                                          int pageNumber,
                                                          int pageSize) {
            pageRequests.incrementAndGet();
            lastFromHeight = fromHeight;
            maxRequestedPageSize = Math.max(maxRequestedPageSize, pageSize);
            pageStarted.countDown();
            try {
                if (!releasePage.await(5, TimeUnit.SECONDS)) {
                    throw new IllegalStateException("test page release timed out");
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new IllegalStateException(e);
            }
            if (failTransactions) {
                throw new IllegalStateException("synthetic read failure");
            }
            List<V35SupplyTransactionRow> filtered = new ArrayList<>();
            synchronized (rows) {
                for (V35SupplyTransactionRow row : rows) {
                    if (row.getBlockIndex() >= fromHeight && row.getBlockIndex() <= toHeight) {
                        filtered.add(row);
                    }
                }
            }
            int start = Math.min(filtered.size(), pageNumber * pageSize);
            int end = Math.min(filtered.size(), start + pageSize);
            return new V35SupplyTransactionPage(
                    filtered.subList(start, end),
                    end < filtered.size());
        }

        @Override
        public BigDecimal accountGoldBalance(String address) {
            accountRequests.add(address);
            return Seting.ADDRESS_FOUNDER.equals(address)
                    ? founderBalance
                    : BigDecimal.ZERO.setScale(2);
        }
    }
}
