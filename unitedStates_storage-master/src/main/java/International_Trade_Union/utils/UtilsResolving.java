package International_Trade_Union.utils;

import International_Trade_Union.controllers.BasisController;
import International_Trade_Union.entity.DtoTransaction.DtoTransaction;
import International_Trade_Union.entity.EntityChain;
import International_Trade_Union.entity.SendBlocksEndInfo;
import International_Trade_Union.entity.SubBlockchainEntity;
import International_Trade_Union.entity.blockchain.Blockchain;
import International_Trade_Union.entity.blockchain.DataShortBlockchainInformation;
import International_Trade_Union.entity.blockchain.block.Block;
import International_Trade_Union.entity.entities.EntityAccount;
import International_Trade_Union.entity.entities.EntityBlock;
import International_Trade_Union.entity.services.BlockService;
import International_Trade_Union.logger.MyLogger;
import International_Trade_Union.model.*;
import International_Trade_Union.model.comparator.HostEndDataShortBComparator;
import International_Trade_Union.protocol.v35.JackpotCalculator;
import International_Trade_Union.setings.Seting;
import International_Trade_Union.utils.base.Base;
import International_Trade_Union.utils.base.Base58;
import International_Trade_Union.vote.LawEligibleForParliamentaryApproval;
import International_Trade_Union.vote.Laws;
import International_Trade_Union.vote.UtilsLaws;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.json.JSONException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;


import javax.annotation.PostConstruct;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SignatureException;
import java.security.spec.InvalidKeySpecException;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static International_Trade_Union.controllers.BasisController.*;
import static International_Trade_Union.setings.Seting.RANDOM_HOSTS;
import static International_Trade_Union.utils.UtilsBalance.calculateBalance;
import static International_Trade_Union.utils.UtilsBalance.rollbackCalculateBalance;

@Component
public class UtilsResolving {
    public static final int MAX_PROPAGATION_HOSTS = PeerNetworkPolicy.maxPeersPerPropagationCycle();
    static final long MIN_PROPAGATION_AVAILABLE_MEMORY_BYTES = 128L * 1024L * 1024L;
    static final long MIN_PROPAGATION_MAX_MEMORY_DIVISOR = 10L;
    public static final int PEER_PROPAGATION_TIMEOUT_MILLIS = 3000;
    private static final PeerErrorRateLimiter PEER_ERROR_RATE_LIMITER = new PeerErrorRateLimiter();
    private static volatile long lastResolve3StartMillis = -1;
    private static volatile long lastResolve3EndMillis = -1;
    private static volatile long lastResolve3DurationMillis = -1;
    public static final String RESOLVE_SKIPPED_SELF_PEER = "RESOLVE_SKIPPED_SELF_PEER";
    public static final String RESOLVE_SKIPPED_STALE_PEER = "RESOLVE_SKIPPED_STALE_PEER";
    public static final String RESOLVE_SKIPPED_EMPTY_DOWNLOAD = "RESOLVE_SKIPPED_EMPTY_DOWNLOAD";
    public static final String RESOLVE_SKIPPED_INVALID_ROLLBACK_TEMP = "RESOLVE_SKIPPED_INVALID_ROLLBACK_TEMP";
    public static final String RESOLVE_SKIPPED_BOUNDS_GUARD = "RESOLVE_SKIPPED_BOUNDS_GUARD";
    public static final String RESOLVE_SKIPPED_PEER_CANNOT_SUPPLY_BLOCKS = "RESOLVE_SKIPPED_PEER_CANNOT_SUPPLY_BLOCKS";
    private static volatile String lastResolveSkippedReason = "none";
    private static volatile String lastResolvePeer = "none";
    private static volatile long lastResolveLocalSize = -1;
    private static volatile long lastResolvePeerSize = -1;
    private static volatile long lastResolveRequestedDownloadCount = -1;
    private static volatile long lastResolveDownloadedCount = -1;
    private static volatile boolean lastResolveRollbackTempValid = false;
    private static volatile String lastResolveException = "none";
    private static final AtomicLong resolveSelfPeerSkippedCount = new AtomicLong();
    private static final AtomicLong resolveStalePeerSkippedCount = new AtomicLong();
    private static final AtomicLong resolveEmptyDownloadSkippedCount = new AtomicLong();
    private static final AtomicLong resolveInvalidRollbackSkippedCount = new AtomicLong();
    private static final AtomicLong resolveBoundsGuardCount = new AtomicLong();

    public static long getLastResolve3StartMillis() {
        return lastResolve3StartMillis;
    }

    public static long getLastResolve3EndMillis() {
        return lastResolve3EndMillis;
    }

    public static long getLastResolve3DurationMillis() {
        return lastResolve3DurationMillis;
    }

    public static int getTrackedPeerErrorCount() {
        return PEER_ERROR_RATE_LIMITER.trackedErrorCount();
    }

    public static Map<String, Object> resolveSyncDebugSnapshot() {
        Map<String, Object> status = new LinkedHashMap<>();
        status.put("lastResolveSkippedReason", lastResolveSkippedReason);
        status.put("lastResolvePeer", lastResolvePeer);
        status.put("lastResolveLocalSize", lastResolveLocalSize);
        status.put("lastResolvePeerSize", lastResolvePeerSize);
        status.put("lastResolveRequestedDownloadCount", lastResolveRequestedDownloadCount);
        status.put("lastResolveDownloadedCount", lastResolveDownloadedCount);
        status.put("lastResolveRollbackTempValid", lastResolveRollbackTempValid);
        status.put("resolveSelfPeerSkippedCount", resolveSelfPeerSkippedCount.get());
        status.put("resolveStalePeerSkippedCount", resolveStalePeerSkippedCount.get());
        status.put("resolveEmptyDownloadSkippedCount", resolveEmptyDownloadSkippedCount.get());
        status.put("resolveInvalidRollbackSkippedCount", resolveInvalidRollbackSkippedCount.get());
        status.put("resolveBoundsGuardCount", resolveBoundsGuardCount.get());
        status.put("lastResolveException", lastResolveException);
        status.putAll(PeerHealthRegistry.memoryStatusFields());
        return status;
    }

    public static void resetResolveSyncDebugForTests() {
        lastResolveSkippedReason = "none";
        lastResolvePeer = "none";
        lastResolveLocalSize = -1;
        lastResolvePeerSize = -1;
        lastResolveRequestedDownloadCount = -1;
        lastResolveDownloadedCount = -1;
        lastResolveRollbackTempValid = false;
        lastResolveException = "none";
        resolveSelfPeerSkippedCount.set(0);
        resolveStalePeerSkippedCount.set(0);
        resolveEmptyDownloadSkippedCount.set(0);
        resolveInvalidRollbackSkippedCount.set(0);
        resolveBoundsGuardCount.set(0);
    }

    static String classifyResolvePeerForTests(String peer, long localSize, long peerSize) {
        if (isSelfPeer(peer)) {
            return RESOLVE_SKIPPED_SELF_PEER;
        }
        if (peerSize < localSize) {
            return RESOLVE_SKIPPED_STALE_PEER;
        }
        return "RESOLVE_ALLOWED";
    }

    static boolean shouldSkipResolvePeerForTests(String peer, long localSize, long peerSize) {
        return shouldSkipResolvePeer(peer, localSize, peerSize);
    }

    private static boolean shouldSkipResolvePeer(String peer, long localSize, long peerSize) {
        String reason = classifyResolvePeerForTests(peer, localSize, peerSize);
        if ("RESOLVE_ALLOWED".equals(reason)) {
            return false;
        }
        recordResolveSkipped(reason, peer, localSize, peerSize, 0, 0, false);
        return true;
    }

    private static void recordResolveSkipped(String reason, String peer, long localSize, long peerSize,
                                             long requestedDownloadCount, long downloadedCount,
                                             boolean rollbackTempValid) {
        lastResolveSkippedReason = reason;
        lastResolvePeer = String.valueOf(peer);
        lastResolveLocalSize = localSize;
        lastResolvePeerSize = peerSize;
        lastResolveRequestedDownloadCount = requestedDownloadCount;
        lastResolveDownloadedCount = downloadedCount;
        lastResolveRollbackTempValid = rollbackTempValid;
        if (RESOLVE_SKIPPED_SELF_PEER.equals(reason)) {
            resolveSelfPeerSkippedCount.incrementAndGet();
        } else if (RESOLVE_SKIPPED_STALE_PEER.equals(reason)) {
            resolveStalePeerSkippedCount.incrementAndGet();
        } else if (RESOLVE_SKIPPED_EMPTY_DOWNLOAD.equals(reason)) {
            resolveEmptyDownloadSkippedCount.incrementAndGet();
        } else if (RESOLVE_SKIPPED_INVALID_ROLLBACK_TEMP.equals(reason)) {
            resolveInvalidRollbackSkippedCount.incrementAndGet();
        } else if (RESOLVE_SKIPPED_BOUNDS_GUARD.equals(reason)
                || RESOLVE_SKIPPED_PEER_CANNOT_SUPPLY_BLOCKS.equals(reason)) {
            resolveBoundsGuardCount.incrementAndGet();
        }
        MyLogger.warnLog(reason
                + " peer=" + peer
                + " localSize=" + localSize
                + " peerSize=" + peerSize
                + " requestedDownloadCount=" + requestedDownloadCount
                + " downloadedCount=" + downloadedCount
                + " rollbackTempValid=" + rollbackTempValid);
    }

    private static void recordResolveException(String peer, Exception exception) {
        lastResolveException = exception == null
                ? "none"
                : exception.getClass().getSimpleName() + ": " + String.valueOf(exception.getMessage());
        lastResolvePeer = String.valueOf(peer);
    }

    static boolean shouldSkipInvalidRollbackForTests(List<Block> different,
                                                     List<Block> downloadedBlocks,
                                                     DataShortBlockchainInformation temp,
                                                     String peer,
                                                     long localSize,
                                                     long peerSize) {
        return shouldSkipInvalidRollback(different, downloadedBlocks, temp, peer, localSize, peerSize);
    }

    private static boolean shouldSkipInvalidRollback(List<Block> different,
                                                     List<Block> downloadedBlocks,
                                                     DataShortBlockchainInformation temp,
                                                     String peer,
                                                     long localSize,
                                                     long peerSize) {
        int downloadedCount = downloadedBlocks == null ? 0 : downloadedBlocks.size();
        boolean rollbackTempValid = temp != null && temp.isValidation();
        if (downloadedCount == 0) {
            recordResolveSkipped(RESOLVE_SKIPPED_EMPTY_DOWNLOAD, peer, localSize, peerSize,
                    Math.max(0, peerSize - localSize), 0, rollbackTempValid);
            return true;
        }
        if (different == null || different.isEmpty()) {
            recordResolveSkipped(RESOLVE_SKIPPED_INVALID_ROLLBACK_TEMP, peer, localSize, peerSize,
                    Math.max(0, peerSize - localSize), downloadedCount, rollbackTempValid);
            return true;
        }
        return false;
    }

    @Autowired
    BlockService blockService;

    @PostConstruct
    public void init() {
        Blockchain.setBlockService(blockService);
        UtilsBalance.setBlockService(blockService);
        UtilsBlock.setBlockService(blockService);
    }

    private List<Block> v35CanonicalContextBefore(Block prevBlock) throws IOException {
        if (prevBlock == null) {
            return new ArrayList<>();
        }
        return v35CanonicalContextBeforeIndex(prevBlock.getIndex());
    }

    private List<Block> v35CanonicalContextBeforeIndex(long index) throws IOException {
        if (Seting.V35_RESCUE_FORK_INDEX == Long.MAX_VALUE || index < Seting.V35_RESCUE_FORK_INDEX) {
            return new ArrayList<>();
        }
        return UtilsBlockToEntityBlock.entityBlocksToBlocks(blockService.findBySpecialIndexBetween(0, index));
    }

    static boolean isV35JackpotAccountingRequired(Block block) {
        return Seting.V35_RESCUE_FORK_INDEX != Long.MAX_VALUE
                && block != null
                && block.getIndex() >= Seting.V35_RESCUE_FORK_INDEX
                && containsV35JackpotPayoutCandidate(block);
    }

    static boolean containsV35JackpotPayoutCandidate(List<Block> blocks) {
        if (blocks == null) {
            return false;
        }
        for (Block block : blocks) {
            if (containsV35JackpotPayoutCandidate(block)) {
                return true;
            }
        }
        return false;
    }

    static boolean requiresV35JackpotAccounting(List<Block> blocks) {
        if (blocks == null) {
            return false;
        }
        for (Block block : blocks) {
            if (isV35JackpotAccountingRequired(block)) {
                return true;
            }
        }
        return false;
    }

    static boolean containsV35JackpotPayoutCandidate(Block block) {
        if (block == null || block.getDtoTransactions() == null) {
            return false;
        }
        for (DtoTransaction transaction : block.getDtoTransactions()) {
            if (transaction == null) {
                continue;
            }
            if (Seting.BASIS_ADDRESS.equals(transaction.getSender())
                    && JackpotCalculator.classifyPayoutAmount(transaction.getDigitalDollar()) != JackpotCalculator.JackpotPeriod.UNKNOWN) {
                return true;
            }
        }
        return false;
    }

    static Block withoutV35JackpotPayoutCandidates(Block block) {
        if (!containsV35JackpotPayoutCandidate(block)) {
            return block;
        }
        List<DtoTransaction> transactions = block.getDtoTransactions().stream()
                .filter(transaction -> !(Seting.BASIS_ADDRESS.equals(transaction.getSender())
                        && JackpotCalculator.classifyPayoutAmount(transaction.getDigitalDollar()) != JackpotCalculator.JackpotPeriod.UNKNOWN))
                .collect(Collectors.toList());
        return new Block(
                transactions,
                block.getPreviousHash(),
                block.getMinerAddress(),
                block.getFounderAddress(),
                block.getRandomNumberProof(),
                block.getMinerRewards(),
                block.getHashCompexity(),
                block.getTimestamp(),
                block.getIndex(),
                block.getHashBlock()
        );
    }

    private Map<String, Account> calculateResolvingBalance(
            Map<String, Account> balances,
            Block block,
            List<String> signs,
            List<String> signaturesNotTakenIntoAccount,
            List<Block> acceptedBlocksForContext
    ) throws NoSuchAlgorithmException, InvalidKeySpecException, NoSuchProviderException, SignatureException, InvalidKeyException, IOException {
        if (!isV35JackpotAccountingRequired(block)) {
            return calculateBalance(balances, block, signs, signaturesNotTakenIntoAccount);
        }

        Block blockWithoutJackpotPayouts = withoutV35JackpotPayoutCandidates(block);
        Map<String, Account> updatedBalances = calculateBalance(balances, blockWithoutJackpotPayouts, signs, signaturesNotTakenIntoAccount);
        List<Block> canonicalContext = v35CanonicalContextBeforeIndex(block.getIndex() - 1);
        if (acceptedBlocksForContext != null) {
            canonicalContext.addAll(acceptedBlocksForContext);
        }
        if (!UtilsBalance.applyV35JackpotPayoutsWithChainContext(
                updatedBalances,
                canonicalContext,
                block,
                Seting.ADDRESS_FOUNDER,
                Seting.V35_RESCUE_FORK_INDEX)) {
            throw new IllegalStateException("Invalid V35 jackpot payout in resolving balance path at block " + block.getIndex());
        }
        return updatedBalances;
    }

    @Autowired
    DomainConfiguration domainConfiguration;


    public Map<String, DataShortBlockchainInformation> fetchDataFromHosts(List<HostEndDataShortB> hostsList) {
        if (PeerWorkMemoryGuard.shouldSkipNonCriticalPeerWork("fetchDataFromHosts")) {
            return Collections.emptyMap();
        }
        List<HostEndDataShortB> boundedHosts = hostsList == null
                ? new ArrayList<>()
                : hostsList.stream().limit(MAX_PROPAGATION_HOSTS).collect(Collectors.toList());
        ExecutorService executorService = Executors.newFixedThreadPool(boundedPeerPoolSize(boundedHosts.size())); // ÐÐ°ÑÑ‚Ñ€Ð¾Ð¹ÐºÐ° Ð¿ÑƒÐ»Ð° Ð¿Ð¾Ñ‚Ð¾ÐºÐ¾Ð²
        Map<String, DataShortBlockchainInformation> hostDataMap = new ConcurrentHashMap<>();

        // ÐÑÐ¸Ð½Ñ…Ñ€Ð¾Ð½Ð½Ð°Ñ Ð¾Ð±Ñ€Ð°Ð±Ð¾Ñ‚ÐºÐ° ÑƒÐ·Ð»Ð¾Ð²
        List<CompletableFuture<Void>> futures = boundedHosts.stream()
                .map(host -> CompletableFuture.runAsync(() -> {
                    String s = host.getHost();
                    try {
                        // ÐŸÐ¾Ð»ÑƒÑ‡Ð°ÐµÐ¼ Ð¼ÐµÑ‚Ð°-Ð´Ð°Ð½Ð½Ñ‹Ðµ
                        if (isSelfPeer(s)) {
                            BasisController.recordSelfPeerSkipped();
                            MyLogger.warnLog("SELF_PEER_SKIPPED phase=fetchDataFromHosts peer=" + s);
                            return;
                        }
                        String jsonGlobalData = UtilUrl.readJsonFromUrl(s + "/datashort");
                        System.out.println("jsonGlobalData for host " + s + ": " + jsonGlobalData);

                        // ÐŸÑ€Ð¾Ð²ÐµÑ€ÑÐµÐ¼ Ð½Ð° Ð¿ÑƒÑÑ‚Ð¾Ñ‚Ñƒ
                        if (jsonGlobalData == null || jsonGlobalData.isEmpty() || jsonGlobalData.isBlank()) {
                            System.out.println("jsonGlobalData is empty for host: " + s);
                            return;
                        }

                        // ÐšÐ¾Ð½Ð²ÐµÑ€Ñ‚Ð¸Ñ€ÑƒÐµÐ¼ Ð² Ð¾Ð±ÑŠÐµÐºÑ‚
                        DataShortBlockchainInformation global = UtilsJson.jsonToDataShortBlockchainInformation(jsonGlobalData);
                        hostDataMap.put(s, global); // Ð¡Ð¾Ñ…Ñ€Ð°Ð½ÑÐµÐ¼ Ð² Map
                    } catch (Exception e) {
                        System.err.println("Error fetching data from host " + s + ": " + e.getMessage());
                    }
                }, executorService))
                .collect(Collectors.toList());

        // ÐžÐ¶Ð¸Ð´Ð°Ð½Ð¸Ðµ Ð·Ð°Ð²ÐµÑ€ÑˆÐµÐ½Ð¸Ñ Ð²ÑÐµÑ… Ð·Ð°Ð´Ð°Ñ‡
        try {
            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
                    .orTimeout(PeerNetworkPolicy.maxRemoteCallDurationMs(), TimeUnit.MILLISECONDS)
                    .join();
        } catch (CompletionException e) {
            PEER_ERROR_RATE_LIMITER.logPeerError("fetchDataFromHosts_batch", "/datashort", e);
        } finally {
            futures.clear();
            boundedHosts.clear();
            executorService.shutdownNow();
        }
        return hostDataMap;
    }

    @Transactional
    public int resolve3(List<HostEndDataShortB> hostsList) {
        lastResolve3StartMillis = System.currentTimeMillis();
        boolean resolveBudgetExhausted = false;
        BasisController.setUpdating(true);
        NodeMemoryProfiler.beginTrace("resolve3 start", null);
        NodeMemoryProfiler.recordStage("resolve3 start", NodeMemoryProfiler.metrics(
                "hostsListSize", hostsList == null ? 0 : hostsList.size(),
                "localBlockchainSize", BasisController.getBlockchainSize()
        ));

        //Ð¿ÐµÑ€ÐµÐ´Ð°ÐµÑ‚ Ð² Ð´Ð¾ÑÑ‚ÑƒÐ¿ Ðº Ð±Ð°Ð·Ðµ Ð´Ð°Ð½Ð½Ñ‹Ñ… Ð² h2,
        Blockchain.setBlockService(blockService);
        UtilsBalance.setBlockService(blockService);
        UtilsBlock.setBlockService(blockService);


        //ÑƒÐ´Ð°Ð»ÑÐµÑ‚ Ñ„Ð°Ð¹Ð»Ñ‹ ÐºÐ¾Ñ‚Ð¾Ñ€Ñ‹Ðµ Ñ…Ñ€Ð°Ð½ÑÑ‚ÑŒ Ð·Ð°Ð±Ð»Ð¾ÐºÐ¸Ñ€Ð¾Ð²Ð°Ð½Ð½Ñ‹Ðµ Ñ…Ð¾ÑÑ‚Ñ‹

        int bigSize = 0;
        try {
            System.out.println(" :start resolve");
            utilsMethod();

            //local blockchain size
            //Ñ€Ð°Ð·Ð¼ÐµÑ€ Ð»Ð¾ÐºÐ°Ð»ÑŒÐ½Ð¾Ð³Ð¾ Ð±Ð»Ð¾ÐºÑ‡ÐµÐ¹Ð½Ð°
            int blocks_current_size = BasisController.getBlockchainSize();
            if (PeerWorkMemoryGuard.shouldSkipNonCriticalPeerWork("resolve3")) {
                lastResolveSkippedReason = "MEMORY_GUARD_SKIPPED_NON_CRITICAL_PEER_WORK";
                NodeMemoryProfiler.recordStage("resolve3 skip", NodeMemoryProfiler.metrics(
                        "skipReason", lastResolveSkippedReason,
                        "localBlockchainSize", blocks_current_size
                ));
                bigSize = blocks_current_size;
                return 0;
            }

            EntityChain entityChain = null;
            System.out.println(" resolve3:local size: " + blocks_current_size);


            System.out.println(":resolve3: size nodes: " + getNodes().size());
            //goes through all hosts (repositories) in search of the most up-to-date blockchain
            //Ð¿Ñ€Ð¾Ñ…Ð¾Ð´Ð¸Ñ‚ Ð¿Ð¾ Ð²ÑÐµÐ¼ Ñ…Ð¾ÑÑ‚Ð°Ð¼(Ñ…Ñ€Ð°Ð½Ð¸Ð»Ð¸Ñ‰Ð°Ð¼) Ð² Ð¿Ð¾Ð¸ÑÐºÐ°Ñ… ÑÐ°Ð¼Ð¾Ð³Ð¾ Ð°ÐºÑ‚ÑƒÐ°Ð»ÑŒÐ½Ð¾Ð³Ð¾ Ð±Ð»Ð¾ÐºÑ‡ÐµÐ¹Ð½Ð°


            List<HostEndDataShortB> boundedResolveHosts = PeerHealthRegistry.selectHosts(
                    hostsList == null ? Collections.emptyList() : hostsList,
                    PeerNetworkPolicy.maxPeersPerResolveCycle(),
                    "resolve3",
                    blocks_current_size);
            Set<String> newAddress = newHostsLoop(boundedResolveHosts.stream().map(t -> t.getHost()).collect(Collectors.toSet()));
            NodeMemoryProfiler.recordStage("resolve3 after newHostsLoop", NodeMemoryProfiler.metrics(
                    "newHostsCount", newAddress == null ? 0 : newAddress.size()
            ));

            for (String s : newAddress) {
                UtilsAllAddresses.putHost(s);
            }


            NodeMemoryProfiler.recordStage("resolve3 before fetchDataFromHosts", NodeMemoryProfiler.metrics(
                    "hostsListSize", hostsList == null ? 0 : hostsList.size(),
                    "boundedResolveHostsSize", boundedResolveHosts == null ? 0 : boundedResolveHosts.size()
            ));
            Map<String, DataShortBlockchainInformation> hostDataMap = fetchDataFromHosts(boundedResolveHosts);
            NodeMemoryProfiler.recordStage("resolve3 after fetchDataFromHosts", NodeMemoryProfiler.metrics(
                    "hostDataMapSize", hostDataMap == null ? 0 : hostDataMap.size()
            ));
            // Ð¢ÐµÐ¿ÐµÑ€ÑŒ Ð¼Ð¾Ð¶Ð½Ð¾ ÑÐ¾Ñ€Ñ‚Ð¸Ñ€Ð¾Ð²Ð°Ñ‚ÑŒ Ð¸Ð»Ð¸ Ñ€Ð°Ð±Ð¾Ñ‚Ð°Ñ‚ÑŒ Ñ ÑƒÐ·Ð»Ð°Ð¼Ð¸ Ð¿Ð¾ `bigRandomNumber`
            List<Map.Entry<String, DataShortBlockchainInformation>> sortedHosts = hostDataMap.entrySet().stream()
                    .sorted((entry1, entry2) -> Integer.compare(entry2.getValue().getBigRandomNumber(), entry1.getValue().getBigRandomNumber()))
                    .collect(Collectors.toList());
            //ÐŸÑ€Ð¾Ñ…Ð¾Ð´Ð¸Ñ‚ Ð¿Ð¾ Ð²ÑÐµÐ¼ Ñ…Ð¾ÑÑ‚Ð°Ð¼, Ð² Ð¿Ð¾Ð¸ÑÐºÐ°Ñ…, Ð³Ð´Ðµ ÐºÐ¾Ð»Ð¸Ñ‡ÐµÑÑ‚Ð²Ð¾ big random Ð±Ð¾Ð»ÑŒÑˆÐµ
            //P.S. Ð½Ð° ÐºÐ¾ÑˆÐµÐ»ÑŒÐºÐµ Ð¿Ð¾Ð´ÐºÐ»ÑŽÑ‡Ð°ÐµÑ‚ÑÑ Ñ‚Ð¾Ð»ÑŒÐºÐ¾ Ðº ÑÐ²Ð¾ÐµÐ¼Ñƒ Ñ…Ð¾ÑÑ‚Ñƒ Ð¸ Ð¾Ð±Ð½Ð¾Ð²Ð»ÑÐµÑ‚ÑÑ Ñ‚Ð¾Ð»ÑŒÐºÐ¾ Ñƒ Ð½ÐµÐ³Ð¾
            boolean isFirst = true;
            hostContinue:
            for (Map.Entry<String, DataShortBlockchainInformation> entry : sortedHosts) {
                if (System.currentTimeMillis() - lastResolve3StartMillis > PeerNetworkPolicy.resolveMaxDurationMs()) {
                    resolveBudgetExhausted = true;
                    PeerHealthRegistry.recordResolveBudgetExhausted(System.currentTimeMillis() - lastResolve3StartMillis);
                    lastResolveSkippedReason = "RESOLVE_BUDGET_EXHAUSTED";
                    break;
                }
                String s = entry.getKey();

                //if the local address matches the host address, it skips
                //ÐµÑÐ»Ð¸ Ð»Ð¾ÐºÐ°Ð»ÑŒÐ½Ñ‹Ð¹ Ð°Ð´Ñ€ÐµÑ ÑÐ¾Ð²Ð¿Ð°Ð´Ð°ÐµÑ‚ Ñ Ð°Ð´Ñ€ÐµÑÐ¾Ð¼ Ñ…Ð¾ÑÑ‚Ð°, Ð¾Ð½ Ð¿Ñ€Ð¾Ð¿ÑƒÑÐºÐ°ÐµÑ‚
                if (BasisController.getExcludedAddresses().contains(s)) {
                    System.out.println(":its your address or excluded address: " + s);
                    continue;
                }
                try {
                    //if the address is localhost, it skips
                    //ÐµÑÐ»Ð¸ Ð°Ð´Ñ€ÐµÑ Ð»Ð¾ÐºÐ°Ð»ÑŒÐ½Ð¾Ð³Ð¾ Ñ…Ð¾ÑÑ‚Ð°, Ð¾Ð½ Ð¿Ñ€Ð¾Ð¿ÑƒÑÐºÐ°ÐµÑ‚
                    if (Seting.IS_TEST == false && (s.contains("localhost") || s.contains("127.0.0.1")))
                        continue;
                    if (isSelfPeer(s)) {
                        BasisController.recordSelfPeerSkipped();
                        PeerHealthRegistry.recordSelfSkipped(s, "resolve3");
                        recordResolveSkipped(RESOLVE_SKIPPED_SELF_PEER, s, blocks_current_size, -1, 0, 0, false);
                        MyLogger.warnLog("SELF_PEER_SKIPPED phase=resolve3 peer=" + s);
                        continue;
                    }
                    String sizeStr = UtilUrl.readJsonFromUrl(s + "/size");
                    Integer size = Integer.valueOf(sizeStr);
                    if (size < blocks_current_size) {
                        PeerHealthRegistry.markStale(s, blocks_current_size, size, "resolve3");
                        recordResolveSkipped(RESOLVE_SKIPPED_STALE_PEER, s, blocks_current_size, size, 0, 0, false);
                        continue;
                    }
                    PeerHealthRegistry.recordSuccess(s, "/size", 0, blocks_current_size, size);

                    MyLogger.saveLog("resolve3: " + sizeStr + " s " + s);
                    //Ð·Ð´ÐµÑÑŒ ÑƒÑÑ‚Ð°Ð½Ð°Ð²Ð»Ð¸Ð²Ð°ÐµÑ‚ ÑÐ°Ð¼Ñ‹Ð¹ Ð´Ð»Ð¸Ð½Ð½Ñ‹Ð¹ Ð±Ð»Ð¾ÐºÑ‡ÐµÐ¹Ð½.
                    if (size > bigSize) {
                        bigSize = size;

                    }

                    //ÑÐºÐ°Ñ‡Ð¸Ð²Ð°ÐµÐ¼ Ð¼ÐµÑ‚Ð° Ð´Ð°Ð½Ð½Ñ‹Ðµ Ñ ÑÐµÑ€Ð²ÐµÑ€Ð°
                    String jsonGlobalData = UtilUrl.readJsonFromUrl(s + "/datashort");
                    System.out.println("jsonGlobalData: " + jsonGlobalData);
                    //ÐµÑÐ»Ð¸ Ð¼ÐµÑ‚Ð° Ð´Ð°Ð½Ð½Ñ‹Ðµ ÑÐµÑ€Ð²ÐµÑ€Ð° Ð¿ÑƒÑÑ‚Ð½Ñ‹Ðµ, Ñ‚Ð¾ Ð¾Ð½ Ð¿Ñ€Ð¾Ð¿ÑƒÑÐºÐ°ÐµÑ‚ ÑÑ‚Ð¾Ñ‚ ÑÐµÑ€Ð²ÐµÑ€
                    if (jsonGlobalData == null || jsonGlobalData.isEmpty() || jsonGlobalData.isBlank()) {
                        System.out.println("*********************************************************");
                        System.out.println("jsonGlobalData: error: " + jsonGlobalData);
                        System.out.println("*********************************************************");
                        continue;
                    }
                    //Ð¼ÐµÑ‚Ð° Ð´Ð°Ð½Ð½Ñ‹Ðµ Ñ ÑÐµÑ€Ð²ÐµÑ€Ð°
                    DataShortBlockchainInformation global = UtilsJson.jsonToDataShortBlockchainInformation(jsonGlobalData);
//
                    System.out.println("resolve3 size: " + size + " blocks_current_size: " + blocks_current_size);

                    if (isBig(BasisController.getShortDataBlockchain(), global)) {
                        System.out.println(":size from address: " + s + " upper than: " + size + ":blocks_current_size " + blocks_current_size);
                        //Test start algorithm
                        //600 Ð¿Ð¾ÑÐ»ÐµÐ´Ð½Ð¸Ñ… Ð±Ð»Ð¾ÐºÐ¾Ð², Ð´Ð»Ñ Ð¿Ð¾Ð´ÑÑ‡ÐµÑ‚Ð° ÑÐ»Ð¾Ð¶Ð½Ð¾ÑÑ‚Ð¸, Ð´Ð»Ñ Ð¿Ð¾ÑÐ»ÐµÐ´Ð½ÐµÐ³Ð¾ Ð±Ð»Ð¾ÐºÐ°.
                        List<Block> lastDiff = new ArrayList<>();
                        SubBlockchainEntity subBlockchainEntity = null;
                        String subBlockchainJson = null;

                        Map<String, Account> balances = new HashMap<>();
                        Map<String, Account> tempBalances = new HashMap<>();


                        //if the local one lags behind the global one by more than PORTION_DOWNLOAD (500 blocks), then you need to download in portions from the storage
                        //ÐµÑÐ»Ð¸ Ð»Ð¾ÐºÐ°Ð»ÑŒÐ½Ñ‹Ð¹ Ð¾Ñ‚ÑÑ‚Ð°ÐµÑ‚ Ð¾Ñ‚ Ð³Ð»Ð¾Ð±Ð°Ð»ÑŒÐ½Ð¾Ð³Ð¾ Ð±Ð¾Ð»ÑŒÑˆÐµ Ñ‡ÐµÐ¼ PORTION_DOWNLOAD (500 Ð±Ð»Ð¾ÐºÐ¾Ð²), Ñ‚Ð¾ Ð½ÑƒÐ¶Ð½Ð¾ ÑÐºÐ°Ñ‡Ð¸Ð²Ð°Ñ‚ÑŒ Ð¿Ð¾Ñ€Ñ†Ð¸ÑÐ¼Ð¸ Ð¸Ð· Ñ…Ñ€Ð°Ð½Ð¸Ð»Ð¸Ñ‰Ð°
                        if (size - blocks_current_size > Seting.PORTION_DOWNLOAD) {
                            boolean downloadPortion = true;
                            int finish = blocks_current_size + Seting.PORTION_DOWNLOAD;
                            int start = blocks_current_size;

                            //while the difference in the size of the local blockchain is greater than from the host, it will continue to download in portions to download the entire blockchain
                            //Ð¿Ð¾ÐºÐ° Ñ€Ð°Ð·Ð½Ð¸Ñ†Ð° Ñ€Ð°Ð·Ð¼ÐµÑ€Ð° Ð»Ð¾ÐºÐ°Ð»ÑŒÐ½Ð¾Ð³Ð¾ Ð±Ð»Ð¾ÐºÑ‡ÐµÐ¹Ð½Ð° Ð±Ð¾Ð»ÑŒÑˆÐµ Ñ‡ÐµÐ¼ Ñ Ñ…Ð¾ÑÑ‚Ð° Ð±ÑƒÐ´ÐµÑ‚ Ð¿Ñ€Ð¾Ð´Ð¾Ð»Ð¶Ð°Ñ‚ÑŒÑÑ ÑÐºÐ°Ñ‡Ð¸Ð²Ð°Ñ‚ÑŒ Ð¿Ð¾Ñ€Ñ†Ð¸ÑÐ¼Ð¸, Ñ‡Ñ‚Ð¾Ð±Ñ‹ ÑÐºÐ°Ñ‡Ð°Ñ‚ÑŒ Ð²ÐµÑÑŒ Ð±Ð»Ð¾ÐºÑ‡ÐµÐ¹Ð½
                            stop:
                            while (downloadPortion) {


                                boolean local_size_upper = false;
                                //TODO Ð²Ð¾Ð·Ð¼Ð¾Ð¶Ð½Ð¾ ÑÑ‚Ð¾ Ñ€ÐµÑˆÐ¸Ñ‚ Ð¿Ñ€Ð¾Ð±Ð»ÐµÐ¼Ñƒ, ÐµÑÐ»Ð¸ Ð±Ð»Ð¾ÐºÐ¸ Ð±ÑƒÐ´ÑƒÑ‚ Ñ€Ð°Ð²Ð½Ñ‹
                                //ÐµÑÐ»Ð¸ Ð²Ñ‹ÑÐ¾Ñ‚Ð° Ð¾Ð´Ð¸Ð½Ð°ÐºÐ¾Ð²Ð°Ñ, Ð¸Ð»Ð¸ Ð»Ð¾ÐºÐ°Ð»ÑŒÐ½Ð°Ñ blocks_current_size Ð¼ÐµÐ½ÑŒÑˆÐµ Ð³Ð»Ð¾Ð±Ð°Ð»ÑŒÐ½Ð¾Ð¹ size,
                                //Ñ‚Ð¾ ÑÑ€Ð°Ð±Ð¾Ñ‚Ð°ÐµÑ‚ Ð¼ÐµÑ‚Ð¾Ð´ helpResolve4
                                if (blocks_current_size == size) {
                                    //Ð·Ð´ÐµÑÑŒ Ð³Ð¾Ð²Ð¾Ñ€Ð¸Ñ‚ÑŒÑÑ, Ñ ÐºÐ°ÐºÐ¾Ð³Ð¾ Ð±Ð»Ð¾ÐºÐ° Ð¿Ð¾ ÐºÐ°ÐºÐ¾Ð¹ Ð±Ð»Ð¾Ðº ÑÐºÐ°Ñ‡Ð¸Ð²Ð°Ñ‚ÑŒ.
                                    subBlockchainEntity = new SubBlockchainEntity(blocks_current_size - 1, size);

                                }

                                //Ð•ÑÐ»Ð¸ Ð»Ð¾ÐºÐ°Ð»ÑŒÐ½Ñ‹Ð¹ ÑÐµÑ€Ð²ÐµÑ€ Ð²Ñ‹ÑÐ¾Ñ‚Ð° Ð±Ð¾Ð»ÑŒÑˆÐµ, Ð½Ð¾ Ñ†ÐµÐ½Ð½Ð¾ÑÑ‚ÑŒ Ð½Ð¸Ð¶Ðµ Ñ‡ÐµÐ¼ Ñƒ Ð³Ð»Ð¾Ð±Ð°Ð»ÑŒÐ½Ð¾Ð³Ð¾
                                //ÑÑ€Ð°Ð±Ð°Ñ‚Ñ‹Ð²Ð°ÐµÑ‚ Ð¼ÐµÑ‚Ð¾Ð´ helpResolve5
                                if (size < blocks_current_size) {
                                    subBlockchainEntity = new SubBlockchainEntity(size - Seting.IS_BIG_DIFFERENT, size);
                                    System.out.println("subBlockchainEntity: size < blocks_current_size: " + subBlockchainEntity);
                                    local_size_upper = true;
                                }


                                subBlockchainEntity = new SubBlockchainEntity(start, finish);

                                System.out.println("1:shortDataBlockchain:  " + BasisController.getShortDataBlockchain());
                                System.out.println("1:sublockchainEntity: " + subBlockchainEntity);
                                subBlockchainJson = UtilsJson.objToStringJson(subBlockchainEntity);
                                System.out.println("1:sublockchainJson: " + subBlockchainJson);
                                //ÑÐºÐ°Ñ‡Ð¸Ð²Ð°ÐµÑ‚ Ð±Ð»Ð¾ÐºÐ¸ Ñ ÑÐµÑ€Ð²ÐµÑ€Ð° Ð¿Ð¾Ñ€Ñ†Ð¸ÑÐ¼Ð¸.
                                String str = UtilUrl.getObject(subBlockchainJson, s + "/sub-blocks");
                                if (str.isEmpty() || str.isBlank()) {
                                    System.out.println("-------------------------------------");
                                    System.out.println("sublocks:  str: empty " + str);
                                    System.out.println("-------------------------------------");
                                    continue;
                                }
                                List<Block> subBlocks = UtilsJson.jsonToObject(str);
                                subBlocks = subBlocks.stream().filter(UtilsUse.distinctByKey(Block::getIndex)).sorted(Comparator.comparing(Block::getIndex)).collect(Collectors.toList());
                                //ÐµÑÐ»Ð¸ Ð¿Ð¾Ñ€Ñ†Ð¸Ñ Ð±Ð»Ð¾ÐºÐ¾Ð² Ð¿ÑƒÑÑ‚Ð°Ñ, Ñ‚Ð¾ Ð¿Ñ€Ð¾Ð¿ÑƒÑÐºÐ°ÐµÑ‚ÑÑ.
                                if (subBlocks.isEmpty() || subBlocks.size() == 0) {
                                    System.out.println("-------------------------------------");
                                    System.out.println("sublocks: " + subBlocks.size());
                                    System.out.println("-------------------------------------");
                                    continue;
                                }
                                System.out.println("1:download sub block: " + subBlocks.size());
                                System.out.println("1: host: " + s);

                                //Ð•ÑÐ»Ð¸ ÐºÐ¾Ð»Ð¸Ñ‡ÐµÑÑ‚Ð²Ð¾ Ð¾ÑÑ‚Ð°Ð²ÑˆÐ¸Ñ…ÑÑ Ð±Ð»Ð¾ÐºÐ¾Ð² Ð½Ð° ÑÐµÑ€Ð²ÐµÑ€Ðµ Ð¼ÐµÐ½ÑŒÑˆÐµ 500, Ñ‚Ð¾ ÑÑ€Ð°Ð±Ð°Ñ‚Ñ‹Ð²Ð°ÐµÑ‚,
                                //Ð° Ð¿Ñ€Ð¸ ÑÑ‚Ð¾Ð¼ ÑÐµÑ€Ð²ÐµÑ€ Ð½Ð°Ð¼ Ð·Ð°ÑÐ²Ð»ÑÐ» Ñ‡Ñ‚Ð¾ Ð½Ð° ÐµÐ³Ð¾ ÑÐµÑ€Ð²ÐµÑ€Ðµ Ð±Ð¾Ð»ÑŒÑˆÐµ 500 Ð±Ð»Ð¾ÐºÐ¾Ð², Ñ‚Ð¾ ÑÑ‚Ð¾Ñ‚
                                //ÑÐµÑ€Ð²ÐµÑ€ Ð±Ð»Ð¾ÐºÐ¸Ñ€ÑƒÐµÑ‚ÑÑ, Ð¸ Ð¿Ñ€Ð¾Ð¿ÑƒÑÐºÐ°ÐµÑ‚ÑÑ. Ð¡Ñ€Ð°Ð±Ð°Ñ‚Ñ‹Ð²Ð°ÐµÑ‚ ÑÐ¸ÑÑ‚ÐµÐ¼Ð° Ð±ÐµÐ·Ð¾Ð¿Ð°ÑÐ½Ð¾ÑÑ‚Ð¸.
                                if (Seting.IS_SECURITY == true && subBlocks.size() < Seting.PORTION_DOWNLOAD) {
                                    System.out.println("Blocked host: " + subBlocks.size());
                                    //TODO Ð·Ð°Ð¿Ð¸ÑÑ‹Ð²Ð°Ñ‚ÑŒ ÑÑŽÐ´Ð° Ð·Ð°Ð±Ð»Ð¾ÐºÐ¸Ñ€Ð¾Ð²Ð°Ð½Ð½Ñ‹Ðµ Ñ…Ð¾ÑÑ‚Ñ‹
                                    UtilsAllAddresses.saveAllAddresses(entry.getKey(), Seting.ORIGINAL_POOL_URL_ADDRESS_BLOCKED_FILE);
                                    continue hostContinue;
                                }

                                System.out.println("1. subBlocks: subBLock size - 1:" + (subBlocks.size() - 1));
                                System.out.println("1. finish: subBlocks: subBLock getIndex() + Seting.PORTION_DOWNLOAD + 1:" + (subBlocks.get(subBlocks.size() - 1).getIndex() + Seting.PORTION_DOWNLOAD + 1));
                                System.out.println("1. start: subBlocks: subBLockstart = (int) subBlocks.get(subBlocks.size() - 1).getIndex() + 1;:" + (subBlocks.get(subBlocks.size() - 1).getIndex() + 1));

                                finish = (int) subBlocks.get(subBlocks.size() - 1).getIndex() + Seting.PORTION_DOWNLOAD + 1;
                                start = (int) subBlocks.get(subBlocks.size() - 1).getIndex() + 1;

                                //Ð¿Ð¾Ð»ÑƒÑ‡Ð°ÐµÑ‚ ÑÐ¿Ð¸ÑÐ¾Ðº Ð±Ð°Ð»Ð°Ð½ÑÐ¾Ð² Ð¸Ð· Ð±Ð°Ð·Ñ‹ Ð´Ð°Ð½Ð½Ñ‹Ñ… h2, Ð»Ð¾ÐºÐ°Ð»ÑŒÐ½Ð¾ Ð¿Ð¾ ÑÐ¿Ð¸ÑÐºÑƒ Ð°Ð´Ñ€ÐµÑÐ¾Ð² Ð² Ð±Ð»Ð¾ÐºÐµ.
                                balances = UtilsAccountToEntityAccount.entityAccountsToMapAccounts(UtilsUse.accounts(subBlocks, blockService));
                                //ÑÑ‚Ð¾ Ð°Ð½Ð°Ð»Ð¾Ð³Ð¸Ñ‡Ð½Ñ‹Ð¹ Ð±Ð°Ð»Ð°Ð½Ñ, Ñ‚Ð¾Ð»ÑŒÐºÐ¾ Ð¸ÑÐ¿Ð¾Ð»ÑŒÐ·ÑƒÐµÑ‚ÑÑ Ð´Ð»Ñ Ð¿Ñ€Ð¾Ð²ÐµÑ€ÐºÐ¸.
                                tempBalances = UtilsAccountToEntityAccount.entityAccountsToMapAccounts(UtilsUse.accounts(subBlocks, blockService));


                                //ÑƒÑÑ‚Ð°Ñ€ÐµÐ²ÑˆÑ‹Ð¹ ÑƒÑ‡Ð°ÑÑ‚Ð¾Ðº ÐºÐ¾Ð´Ð°, Ð³Ð´Ðµ Ñ€Ð°Ð½ÐµÐµ ÑÐ»Ð¾Ð¶Ð½Ð¾ÑÑ‚ÑŒ Ð²Ñ‹Ñ‡Ð¸ÑÐ»ÑÐ»Ð¾ÑÑŒ Ð°Ð»Ð³Ð¾Ñ€Ð¸Ñ‚Ð¼Ð¾Ð¼.
                                if (BasisController.getBlockchainSize() > Seting.PORTION_BLOCK_TO_COMPLEXCITY && BasisController.getBlockchainSize() < Seting.V34_NEW_ALGO) {
                                    lastDiff = UtilsBlockToEntityBlock.entityBlocksToBlocks(
                                            blockService.findBySpecialIndexBetween(
                                                    (BasisController.prevBlock().getIndex() + 1) - Seting.PORTION_BLOCK_TO_COMPLEXCITY,
                                                    BasisController.prevBlock().getIndex() + 1
                                            )
                                    );
                                }


                                //ÐºÐ»Ð°ÑÑ Ð¼ÐµÑ‚Ð° Ð´Ð°Ð½Ð½Ñ‹Ñ… Ð±Ð»Ð¾ÐºÑ‡ÐµÐ¹Ð½Ð°.
                                DataShortBlockchainInformation temp = new DataShortBlockchainInformation();

                                //Ð·Ð°Ð³Ñ€ÑƒÐ¶Ð°ÐµÑ‚ Ð±Ð°Ð»Ð°Ð½Ñ Ð²ÑÐµÑ… ÑÑ‡ÐµÑ‚Ð¾Ð² Ð´Ð»Ñ Ñ‚ÐµÐºÑƒÑ‰ÐµÐ³Ð¾ Ð±Ð»Ð¾ÐºÑ‡ÐµÐ¹Ð½Ð°.
                                List<String> sign = new ArrayList<>();
                                if (BasisController.getBlockchainSize() > 1) {
                                    //Ð¿Ñ€Ð¾Ð²ÐµÑ€ÑÐµÑ‚ ÑÐºÐ°Ñ‡ÐµÐ½Ð½Ñ‹Ðµ Ð±Ð»Ð¾ÐºÐ¸ Ð½Ð° Ñ†ÐµÐ»Ð¾ÑÑ‚Ð½Ð¾ÑÑ‚ÑŒ
                                    //checks downloaded blocks for integrity
                                    Map<String, Account> balanceForValid = UtilsUse.balancesClone(balances);
                                    temp = Blockchain.shortCheckWithCanonicalContext(
                                            BasisController.prevBlock(), subBlocks, BasisController.getShortDataBlockchain(),
                                            lastDiff, tempBalances, sign, balanceForValid, new ArrayList<>(),
                                            v35CanonicalContextBefore(BasisController.prevBlock()), Seting.V35_RESCUE_FORK_INDEX);
                                    System.out.println("prevBlock: " + BasisController.prevBlock().getIndex());
                                } else {

                                    //ÐµÑÐ»Ð¸ Ð²Ñ‹ÑÐ¾Ñ‚Ð° 1 Ð¸Ð»Ð¸ Ð¼ÐµÐ½ÑŒÑˆÐµ, Ð¾Ð½ Ð¸Ð½Ð¸Ñ†Ð¸Ð°Ð»Ð¸Ð·Ð¸Ñ€ÑƒÐµÑ‚ Ð±Ð°Ð·Ð¾Ð²Ñ‹Ð¹ Ð±Ð»Ð¾ÐºÑ‡ÐµÐ¹Ð½, Ð³ÐµÐ½ÐµÐ·Ð¸Ñ Ð±Ð»Ð¾ÐºÐ¾Ð¼ Ð´Ð»Ñ Ð²ÑÐµÑ…
                                    Block genesis = UtilsJson.jsonToBLock("{\"dtoTransactions\":[{\"sender\":\"faErFrDnBhfSfNnj1hYjxydKNH28cRw1PBwDQEXH3QsJ\",\"customer\":\"nNifuwmFZr7fnV1zvmpiyQDV5z7ETWvqR6GSeqeHTY43\",\"digitalDollar\":6.5E7,\"digitalStockBalance\":6.5E7,\"laws\":{\"packetLawName\":null,\"laws\":null,\"hashLaw\":null},\"bonusForMiner\":0.0,\"voteEnum\":\"YES\",\"sign\":\"MEUCIDDW9fKvwUY0aXpvamxOU6pypicO3eCqEVM9LDFrIpjIAiEA81Zh7yCBbJOLrAzx4mg5HS0hMdqvB0obO2CZARczmfY=\"}],\"previousHash\":\"0234a350f4d56ae45c5ece57b08c54496f372bc570bd83a465fb6d2d85531479\",\"minerAddress\":\"nNifuwmFZr7fnV1zvmpiyQDV5z7ETWvqR6GSeqeHTY43\",\"founderAddress\":\"nNifuwmFZr7fnV1zvmpiyQDV5z7ETWvqR6GSeqeHTY43\",\"randomNumberProof\":12,\"minerRewards\":0.0,\"hashCompexity\":1,\"timestamp\":1685942742706,\"index\":1,\"hashBlock\":\"08b1e6634457a40d3481e76ebd377e76322706e4ea27013b773686f7df8f8a4c\"}");
                                    Block firstBlock = UtilsJson.jsonToBLock("{\"dtoTransactions\":[{\"sender\":\"faErFrDnBhfSfNnj1hYjxydKNH28cRw1PBwDQEXH3QsJ\",\"customer\":\"nNifuwmFZr7fnV1zvmpiyQDV5z7ETWvqR6GSeqeHTY43\",\"digitalDollar\":400.0,\"digitalStockBalance\":400.0,\"laws\":{\"packetLawName\":null,\"laws\":null,\"hashLaw\":null},\"bonusForMiner\":0.0,\"voteEnum\":\"YES\",\"sign\":\"MEUCIQDfQ3TAOyuWi4NGr0hNuXjqzxDCL0U8DzwAmedSOw9eiwIgdRlZwmudMZJZURMtgmOwpT+wk569jo/Ok/fAGv0x/NE=\"},{\"sender\":\"faErFrDnBhfSfNnj1hYjxydKNH28cRw1PBwDQEXH3QsJ\",\"customer\":\"nNifuwmFZr7fnV1zvmpiyQDV5z7ETWvqR6GSeqeHTY43\",\"digitalDollar\":8.0,\"digitalStockBalance\":8.0,\"laws\":{\"packetLawName\":null,\"laws\":null,\"hashLaw\":null},\"bonusForMiner\":0.0,\"voteEnum\":\"YES\",\"sign\":\"MEYCIQDV9MbTMPl/dWBTfc87rMcRBBKcNZsGtkuRx1pdzGrSKQIhALOFpX81JEyFCC8uQ//bZkSW9CaOODSgOaMkYgTHn5HC\"}],\"previousHash\":\"08b1e6634457a40d3481e76ebd377e76322706e4ea27013b773686f7df8f8a4c\",\"minerAddress\":\"nNifuwmFZr7fnV1zvmpiyQDV5z7ETWvqR6GSeqeHTY43\",\"founderAddress\":\"nNifuwmFZr7fnV1zvmpiyQDV5z7ETWvqR6GSeqeHTY43\",\"randomNumberProof\":4,\"minerRewards\":0.0,\"hashCompexity\":1,\"timestamp\":1685942784960,\"index\":1,\"hashBlock\":\"06b932aadd602056b0fb7294ef693535009cb3ba54581f32fd4aa1d93108703f\"}");

                                    if (!subBlocks.get(0).equals(genesis) || !subBlocks.get(1).equals(firstBlock)) {
                                        System.out.println("error basis block: ");
                                        System.out.println("genesis: " + subBlocks.get(0).getIndex());
                                        System.out.println("first: " + subBlocks.get(1).getIndex());
                                        System.out.println("temp: " + temp);
                                    }

                                    //Ð·Ð°Ð¿Ð¸ÑÑ‹Ð²Ð°ÐµÑ‚ Ð±Ð»Ð¾ÐºÐ¸ Ð² Ð±Ð°Ð·Ñƒ Ð´Ð°Ð½Ð½Ñ‹Ñ…
                                    boolean save = addBlock3(subBlocks, balances, Seting.ORIGINAL_BLOCKCHAIN_FILE, new ArrayList<>());
                                    temp = Blockchain.checkFromFile(Seting.ORIGINAL_BLOCKCHAIN_FILE);
                                    if (!temp.isValidation()) {
                                        System.out.println("error validation: " + temp);
                                    }
                                    //Ð¿Ñ€Ð¾Ð¸Ð·Ð²Ð¾Ð´Ð¸Ñ‚ Ð·Ð°Ð¿Ð¸ÑÑŒ Ð¼ÐµÑ‚Ð° Ð´Ð°Ð½Ð½Ñ‹Ñ….
                                    if (save) {
                                        BasisController.setShortDataBlockchain(temp);
                                        BasisController.setBlockcheinSize((int) temp.getSize());
                                        BasisController.setBlockchainValid(temp.isValidation());

                                        EntityBlock tempBlock = blockService.findBySpecialIndex(BasisController.getBlockchainSize() - 1);
                                        BasisController.setPrevBlock(UtilsBlockToEntityBlock.entityBlockToBlock(tempBlock));
                                        System.out.println("prevBlock: " + BasisController.prevBlock().getIndex() + " shortDataBlockchain: " + BasisController.getShortDataBlockchain());
                                        String json = UtilsJson.objToStringJson(BasisController.getShortDataBlockchain());
                                        UtilsFileSaveRead.save(json, Seting.TEMPORARY_BLOCKCHAIN_FILE, false);
//
                                    }
                                    continue hostContinue;

                                }


                                System.out.println("++++++++++++++++++++++++++++++++++++++++++++++++");
                                System.out.println("1: temp: " + temp);
                                System.out.println("1: blockchainsize: " + BasisController.getBlockchainSize());
                                System.out.println("1: sublocks: size: " + subBlocks.size());
                                System.out.println("1: shortDataBlockchain: " + BasisController.getShortDataBlockchain());
                                System.out.println("1: host: " + s);

                                //Ð¿Ð¾Ð»ÑƒÑ‡ÐµÐ½Ð¸Ðµ Ð¼ÐµÑ‚Ð° Ð´Ð°Ð½Ð½Ñ‹Ñ… Ñ ÑÐµÑ€Ð²ÐµÑ€Ð°
                                jsonGlobalData = UtilUrl.readJsonFromUrl(s + "/datashort");
                                if (jsonGlobalData == null || jsonGlobalData.isEmpty() || jsonGlobalData.isBlank()) {
                                    System.out.println("*********************************************************");
                                    System.out.println(" 2 jsonGlobalData: error: " + jsonGlobalData);
                                    System.out.println("*********************************************************");
                                    continue;
                                }
                                System.out.println("2: jsonGlobalData: " + jsonGlobalData);
                                global = UtilsJson.jsonToDataShortBlockchainInformation(jsonGlobalData);
                                temp = new DataShortBlockchainInformation();
                                sign = new ArrayList<>();

                                //Ð¿Ð¾Ð»ÑƒÑ‡Ð°ÐµÑ‚ Ð±Ð°Ð»Ð°Ð½Ñ Ñ Ð±Ð°Ð·Ñ‹ Ð´Ð°Ð½Ð½Ñ‹Ñ… h2, Ð»Ð¾ÐºÐ°Ð»ÑŒÐ½Ð¾ Ð¸Ð· ÑÑ‡ÐµÑ‚Ð¾Ð² Ð² Ð±Ð»Ð¾ÐºÐµ.
                                balances = UtilsAccountToEntityAccount.entityAccountsToMapAccounts(UtilsUse.accounts(subBlocks, blockService));
                                tempBalances = UtilsAccountToEntityAccount.entityAccountsToMapAccounts(UtilsUse.accounts(subBlocks, blockService));
                                sign = new ArrayList<>();
                                temp = new DataShortBlockchainInformation();
                                Map<String, Account> balanceForValidation = UtilsUse.balancesClone(balances);

                                //Ð²Ñ‹Ñ‡Ð¸ÑÐ»ÑÐµÑ‚ Ð¼ÐµÑ‚Ð° Ð´Ð°Ð½Ð½Ñ‹Ðµ, Ð¸ Ð´ÐµÐ»Ð°ÐµÑ‚ Ð¿Ñ€Ð¾Ð²ÐµÑ€ÐºÑƒ Ñ†ÐµÐ»Ð¾ÑÑ‚Ð½Ð¾ÑÑ‚Ð¸ Ð±Ð»Ð¾ÐºÑ‡ÐµÐ¹Ð½Ð°, ÐµÑÐ»Ð¸ Ð±Ð»Ð¾ÐºÑ‡ÐµÐ¹Ð½ Ð¿Ñ€Ð°Ð²Ð¸Ð»ÑŒÐ½Ñ‹Ð¹ Ñ‚Ð¾ ÑÑ‚Ð¾ Ð·Ð°Ð¿Ð¸ÑÑ‹Ð²Ð°ÐµÑ‚ÑÑ Ð² Ð¼ÐµÑ‚Ð° Ð´Ð°Ð½Ð½Ñ‹Ñ…
                                temp = Blockchain.shortCheckWithCanonicalContext(
                                        BasisController.prevBlock(), subBlocks, BasisController.getShortDataBlockchain(),
                                        lastDiff, tempBalances, sign, balanceForValidation, new ArrayList<>(),
                                        v35CanonicalContextBefore(BasisController.prevBlock()), Seting.V35_RESCUE_FORK_INDEX);

                                //Ð¿Ð¾Ð»ÑƒÑ‡Ð°ÐµÑ‚ Ð±Ð°Ð»Ð°Ð½Ñ Ñ Ð±Ð°Ð·Ñ‹ Ð´Ð°Ð½Ð½Ñ‹Ñ… h2, Ð»Ð¾ÐºÐ°Ð»ÑŒÐ½Ð¾ Ð¸Ð· ÑÑ‡ÐµÑ‚Ð¾Ð² Ð² Ð±Ð»Ð¾ÐºÐµ.
                                balances = UtilsAccountToEntityAccount.entityAccountsToMapAccounts(UtilsUse.accounts(subBlocks, blockService));
                                tempBalances = UtilsAccountToEntityAccount.entityAccountsToMapAccounts(UtilsUse.accounts(subBlocks, blockService));
                                sign = new ArrayList<>();
                                if (!local_size_upper) {
                                    System.out.println("===========================");
                                    System.out.println("!local_size_upper: " + !local_size_upper);
                                    System.out.println("===========================");
                                    //Ð¼ÐµÑ‚Ð¾Ð´ Ñ‚Ð°Ðº Ð¶Ðµ Ð²Ñ‹Ñ‡Ð¸ÑÐ»ÑÐµÑ‚ Ð¼ÐµÑ‚Ð° Ð´Ð°Ð½Ð½Ñ‹Ðµ Ð¸ Ð·Ð°Ð¿Ð¸ÑÑ‹Ð²Ð°ÐµÑ‚ Ð² Ð»Ð¾ÐºÐ°Ð»ÑŒÐ½ÑƒÑŽ Ð±Ð°Ð·Ñƒ Ð´Ð°Ð½Ð½Ñ‹Ñ… ÐºÐ°Ðº ÑÐ°Ð¼Ð¸ Ð±Ð»Ð¾ÐºÐ¸,
                                    //Ñ‚Ð°Ðº Ð¿Ð¾Ð´ÑÑ‡Ð¸Ñ‚Ñ‹Ð²Ð°ÐµÑ‚ Ð±Ð°Ð»Ð°Ð½ÑÑ‹ ÑÑ‡ÐµÑ‚Ð¾Ð².
                                    temp = helpResolve(temp, global, s, lastDiff, tempBalances, sign, balances, subBlocks, false);

                                }

                                if (local_size_upper) {
                                    System.out.println("===========================");
                                    System.out.println("local_size_upper: " + local_size_upper);
                                    System.out.println("===========================");
                                    //Ð¼ÐµÑ‚Ð¾Ð´ Ñ‚Ð°Ðº Ð¶Ðµ Ð²Ñ‹Ñ‡Ð¸ÑÐ»ÑÐµÑ‚ Ð¼ÐµÑ‚Ð° Ð´Ð°Ð½Ð½Ñ‹Ðµ Ð¸ Ð·Ð°Ð¿Ð¸ÑÑ‹Ð²Ð°ÐµÑ‚ Ð² Ð»Ð¾ÐºÐ°Ð»ÑŒÐ½ÑƒÑŽ Ð±Ð°Ð·Ñƒ Ð´Ð°Ð½Ð½Ñ‹Ñ… ÐºÐ°Ðº ÑÐ°Ð¼Ð¸ Ð±Ð»Ð¾ÐºÐ¸,
                                    //Ñ‚Ð°Ðº Ð¿Ð¾Ð´ÑÑ‡Ð¸Ñ‚Ñ‹Ð²Ð°ÐµÑ‚ Ð±Ð°Ð»Ð°Ð½ÑÑ‹ ÑÑ‡ÐµÑ‚Ð¾Ð².
                                    temp = helpResolve(temp, global, s, lastDiff, tempBalances, sign, balances, subBlocks, false);
                                }

                                System.out.println("++++++++++++++++++++++++++++++++++++++++++++++++");
                                //ÐµÑÐ»Ð¸ ÑÐºÐ°Ñ‡Ð°Ð½Ð½Ñ‹Ð¹ Ð±Ð»Ð¾ÐºÑ‡ÐµÐ¹Ð½ Ð½Ðµ Ð²Ð°Ð»Ð¸Ð´Ð½Ñ‹Ð¹, Ñ‚Ð¾ Ð½Ðµ Ð´Ð¾Ð±Ð°Ð²Ð»ÑÐµÑ‚ Ð² Ð±Ð»Ð¾ÐºÑ‡ÐµÐ¹Ð½, Ð²Ð¾Ð·Ð²Ñ€Ð°Ñ‰Ð°ÐµÑ‚ -10
                                if (BasisController.getBlockchainSize() > 1 && !temp.isValidation()) {
                                    System.out.println("error resolve 2 in portion upper > 500");
                                    return -10;
                                }


                                //ÐµÑÐ»Ð¸ ÐºÐ¾Ð»Ð¸Ñ‡ÐµÑÑ‚Ð²Ð¾ Ð½Ð¾Ð²Ñ‹Ñ… Ð±Ð»Ð¾ÐºÐ¾Ð², Ð¾Ñ‚Ð½Ð¾ÑÐ¸Ñ‚ÐµÐ»ÑŒÐ½Ð¾ Ð»Ð¾ÐºÐ°Ð»ÑŒÐ½Ð¾Ð³Ð¾ Ð±Ð»Ð¾ÐºÑ‡ÐµÐ¹Ð½Ð° Ð¼ÐµÐ½ÑŒÑˆÐµ 500,
                                //Ñ‚Ð¾ ÑÐºÐ°Ñ‡Ð°Ñ‚ÑŒ ÑÑ‚Ð¸ Ð±Ð»Ð¾ÐºÐ¸ Ð¸ Ð¿Ñ€ÐµÐºÑ€Ð°Ñ‚Ð¸Ñ‚ÑŒ Ð¿Ð¾Ð¿Ñ‹Ñ‚ÐºÐ¸ ÑÐºÐ°Ñ‡Ð¸Ð²Ð°Ð½Ð¸Ñ Ñ Ð´Ð°Ð½Ð½Ð¾Ð³Ð¾ ÑƒÐ·Ð»Ð°.
                                //if the number of new blocks relative to the local blockchain is less than 500,
                                //then download these blocks and stop trying to download from this node.
                                if (size - BasisController.prevBlock().getIndex() < Seting.PORTION_DOWNLOAD) {


                                    local_size_upper = false;
                                    //ÑÑ€Ð°Ð±Ð°Ñ‚Ñ‹Ð²Ð°ÐµÑ‚ helpResolve4 ÐµÑÐ»Ð¸ Ð²Ñ‹ÑÐ¾Ñ‚Ð° Ð»Ð¾ÐºÐ°Ð»ÑŒÐ½Ð°Ñ Ð¸ Ð³Ð»Ð¾Ð±Ð°Ð»ÑŒÐ½Ð°Ñ Ð¾Ð´Ð¸Ð½Ð°ÐºÐ¾Ð²Ð°Ñ,
                                    //Ð¸Ð»Ð¸ ÐµÑÐ»Ð¸ Ð»Ð¾ÐºÐ°Ð»ÑŒÐ½Ð°Ñ Ð¼ÐµÐ½ÑŒÑˆÐµ.
                                    if (blocks_current_size == size) {
                                        subBlockchainEntity = new SubBlockchainEntity(blocks_current_size - 1, size);

                                    }
                                    //ÑÑ€Ð°Ð±Ð°Ñ‚Ñ‹Ð²Ð°ÐµÑ‚ helpResolve5 ÐµÑÐ»Ð¸ Ð»Ð¾ÐºÐ°Ð»ÑŒÐ½Ð°Ñ Ð¸Ð¼ÐµÐµÑ‚ Ð±Ð¾Ð»ÑŒÑˆÑƒÑŽ Ð²Ñ‹ÑÐ¾Ñ‚Ñƒ, Ð½Ð¾ Ð¼ÐµÐ½ÑŒÑˆÑƒÑŽ Ñ†ÐµÐ½Ð½Ð¾ÑÑ‚ÑŒ.
                                    if (size < blocks_current_size) {
                                        subBlockchainEntity = new SubBlockchainEntity(size - Seting.IS_BIG_DIFFERENT, size);
                                        System.out.println("subBlockchainEntity: size < blocks_current_size: " + subBlockchainEntity);
                                        local_size_upper = true;
                                    }

                                    downloadPortion = false;
                                    finish = size;
                                    subBlockchainEntity = new SubBlockchainEntity(start, finish);
                                    System.out.println("2:sublockchainEntity: " + subBlockchainEntity);
                                    subBlockchainJson = UtilsJson.objToStringJson(subBlockchainEntity);
                                    System.out.println("2:sublockchainJson: " + subBlockchainJson);
                                    str = UtilUrl.getObject(subBlockchainJson, s + "/sub-blocks");
                                    if (str.isEmpty() || str.isBlank()) {
                                        System.out.println("-------------------------------------");
                                        System.out.println("sublocks:  str: empty " + str);
                                        System.out.println("-------------------------------------");
                                        continue;
                                    }
                                    subBlocks = UtilsJson.jsonToObject(str);
                                    subBlocks = subBlocks.stream().filter(UtilsUse.distinctByKey(Block::getIndex)).sorted(Comparator.comparing(Block::getIndex)).collect(Collectors.toList());

                                    if (subBlocks == null || subBlocks.isEmpty() || subBlocks.size() == 0) {
                                        System.out.println("-------------------------------------");
                                        System.out.println("sublocks: " + subBlocks.size());
                                        System.out.println("-------------------------------------");
                                        continue;
                                    }
                                    System.out.println("2:download sub block: " + subBlocks.size());
                                    System.out.println("2: host: " + s);

                                    if (BasisController.getBlockchainSize() > Seting.PORTION_BLOCK_TO_COMPLEXCITY && BasisController.getBlockchainSize() < Seting.V34_NEW_ALGO) {
                                        lastDiff = UtilsBlockToEntityBlock.entityBlocksToBlocks(
                                                blockService.findBySpecialIndexBetween(
                                                        (BasisController.prevBlock().getIndex() + 1) - Seting.PORTION_BLOCK_TO_COMPLEXCITY,
                                                        BasisController.prevBlock().getIndex() + 1
                                                )
                                        );
                                    }


                                    jsonGlobalData = UtilUrl.readJsonFromUrl(s + "/datashort");
                                    if (jsonGlobalData == null || jsonGlobalData.isEmpty() || jsonGlobalData.isBlank()) {
                                        System.out.println("*********************************************************");
                                        System.out.println(" 2 jsonGlobalData: error: " + jsonGlobalData);
                                        System.out.println("*********************************************************");
                                        continue;
                                    }
                                    System.out.println("2: jsonGlobalData: " + jsonGlobalData);
                                    global = UtilsJson.jsonToDataShortBlockchainInformation(jsonGlobalData);

                                    //Ð¿Ð¾Ð»ÑƒÑ‡ÐµÐ½Ð¸Ðµ Ð±Ð°Ð»Ð°Ð½ÑÐ° Ð¸Ð· Ð±Ð°Ð·Ñ‹ Ð´Ð°Ð½Ð½Ñ‹Ñ… Ð»Ð¾ÐºÐ°Ð»ÑŒÐ½Ð¾ h2, Ð½Ð° Ð±Ð°Ð·Ðµ Ð°Ð´Ñ€ÐµÑÐ¾Ð² Ð² ÑÐ¿Ð¸ÑÐºÐµ Ð±Ð»Ð¾ÐºÐ¾Ð² subBlocks
                                    balances = UtilsAccountToEntityAccount.entityAccountsToMapAccounts(UtilsUse.accounts(subBlocks, blockService));
                                    tempBalances = UtilsAccountToEntityAccount.entityAccountsToMapAccounts(UtilsUse.accounts(subBlocks, blockService));
                                    sign = new ArrayList<>();
                                    temp = new DataShortBlockchainInformation();
                                    balanceForValidation = UtilsUse.balancesClone(balances);
                                    //Ð¿Ð¾Ð´ÑÑ‡ÐµÑ‚ Ð¼ÐµÑ‚Ð° Ð´Ð°Ð½Ð½Ñ‹Ñ… Ð¸ Ð¿Ñ€Ð¾Ð²ÐµÑ€ÐºÐ° Ñ†ÐµÐ»Ð¾ÑÑ‚Ð½Ð¾ÑÑ‚Ð¸ Ð±Ð»Ð¾ÐºÑ‡ÐµÐ¹Ð½Ð°, ÑÐºÐ°Ñ‡Ð¸Ð²Ð°ÐµÐ¼Ð¾Ð³Ð¾ Ð¸Ð· ÑÐµÑ€Ð²ÐµÑ€Ð°.
                                    temp = Blockchain.shortCheckWithCanonicalContext(
                                            BasisController.prevBlock(), subBlocks, BasisController.getShortDataBlockchain(),
                                            lastDiff, tempBalances, sign, balanceForValidation, new ArrayList<>(),
                                            v35CanonicalContextBefore(BasisController.prevBlock()), Seting.V35_RESCUE_FORK_INDEX);


                                    System.out.println("2: temp: " + temp);
                                    System.out.println("2: blockchainsize: " + BasisController.getBlockchainSize());
                                    System.out.println("2: sublocks: " + subBlocks.size());
                                    System.out.println("2: host: " + s);

                                    temp = new DataShortBlockchainInformation();
                                    balances = UtilsAccountToEntityAccount.entityAccountsToMapAccounts(UtilsUse.accounts(subBlocks, blockService));
                                    tempBalances = UtilsAccountToEntityAccount.entityAccountsToMapAccounts(UtilsUse.accounts(subBlocks, blockService));
                                    sign = new ArrayList<>();
                                    if (!local_size_upper) {
                                        System.out.println("===========================");
                                        System.out.println("!local_size_upper: " + !local_size_upper);
                                        System.out.println("===========================");
                                        temp = helpResolve(temp, global, s, lastDiff, tempBalances, sign, balances, subBlocks, true);

                                    }

                                    if (local_size_upper) {
                                        System.out.println("===========================");
                                        System.out.println("local_size_upper: " + local_size_upper);
                                        System.out.println("===========================");
                                        temp = helpResolve(temp, global, s, lastDiff, tempBalances, sign, balances, subBlocks, true);
                                    }


                                    if (Seting.IS_SECURITY && BasisController.getBlockchainSize() > 1 && !temp.isValidation()) {
                                        //TODO Ð´Ð¾Ð±Ð°Ð²Ð¸Ñ‚ÑŒ Ñ…Ð¾ÑÑ‚ Ð² Ð·Ð°Ð±Ð»Ð¾ÐºÐ¸Ñ€Ð¾Ð²Ð°Ð½Ð½Ñ‹Ð¹ Ñ„Ð°Ð¹Ð»

                                        System.out.println("-------------------------------------------------");
                                        System.out.println("Blocked host: ");
                                        System.out.println("expected host: " + entry.getValue());
                                        System.out.println("host: " + entry.getKey());

                                        System.out.println("-------------------------------------------------");
                                        UtilsAllAddresses.saveAllAddresses(s, Seting.ORIGINAL_POOL_URL_ADDRESS_BLOCKED_FILE);
                                        continue;
                                    }

                                }
                            }
                        } else {

                            //ÑÑ‚Ð¾Ñ‚ Ð±Ð»Ð¾Ðº ÑÑ€Ð°Ð±Ð°Ñ‚Ñ‹Ð²Ð°ÐµÑ‚ Ð¸Ð·Ð½Ð°Ñ‡Ð°Ð»ÑŒÐ½Ð¾ ÐµÑÐ»Ð¸ Ð¿Ð¾Ñ€Ñ†Ð¸Ñ Ð±Ñ‹Ð»Ð° Ð¼ÐµÐ½ÑŒÑˆÐµ 500.
                            //Ð’ÐµÑÑŒ ÐºÐ¾Ð´ ÑÑ…Ð¾Ð¶Ð¸Ð¹ Ð°Ð½Ð°Ð»Ð¾Ð³Ð¸Ñ‡Ð½Ð¾ Ð¿Ñ€ÐµÐ´Ñ‹Ð´ÑƒÑ‰Ð¸Ð¼ Ð±Ð»Ð¾ÐºÐ°Ð¼.
                            subBlockchainEntity = new SubBlockchainEntity(blocks_current_size, size);

                            boolean different_value = false;
                            boolean local_size_upper = false;
                            //TODO Ð²Ð¾Ð·Ð¼Ð¾Ð¶Ð½Ð¾ ÑÑ‚Ð¾ Ñ€ÐµÑˆÐ¸Ñ‚ Ð¿Ñ€Ð¾Ð±Ð»ÐµÐ¼Ñƒ, ÐµÑÐ»Ð¸ Ð±Ð»Ð¾ÐºÐ¸ Ð±ÑƒÐ´ÑƒÑ‚ Ñ€Ð°Ð²Ð½Ñ‹
                            if (blocks_current_size == size) {
                                subBlockchainEntity = new SubBlockchainEntity(blocks_current_size - 1, size);

                            }
                            if (size < blocks_current_size) {
                                subBlockchainEntity = new SubBlockchainEntity(size - Seting.IS_BIG_DIFFERENT, size);
                                System.out.println("subBlockchainEntity: size < blocks_current_size: " + subBlockchainEntity);
                                local_size_upper = true;
                            }


                            subBlockchainJson = UtilsJson.objToStringJson(subBlockchainEntity);


                            String str = UtilUrl.getObject(subBlockchainJson, s + "/sub-blocks");
                            if (str.isEmpty() || str.isBlank()) {
                                System.out.println("-------------------------------------");
                                System.out.println("sublocks:  str: empty " + str);
                                MyLogger.saveLog("str.isEmpty() || str.isBlank()");
                                System.out.println("-------------------------------------");
                                continue;
                            }
                            List<Block> subBlocks = UtilsJson.jsonToObject(str);
                            subBlocks = subBlocks.stream().filter(UtilsUse.distinctByKey(Block::getIndex)).sorted(Comparator.comparing(Block::getIndex)).collect(Collectors.toList());


                            if (subBlocks == null || subBlocks.isEmpty() || subBlocks.size() == 0) {
                                System.out.println("-------------------------------------");
                                System.out.println("sublocks: " + subBlocks.size());
                                MyLogger.saveLog("subBlocks.isEmpty() || subBlocks.size() == 0");
                                System.out.println("-------------------------------------");
                                continue;
                            }
                            System.out.println("3:download sub block: " + subBlocks.size());


                            tempBalances = UtilsAccountToEntityAccount.entityAccountsToMapAccounts(UtilsUse.accounts(subBlocks, blockService));
                            Map<String, Account> validationBalance = UtilsUse.balancesClone(tempBalances);

                            if (tempBalances == null || tempBalances.isEmpty()) {
                                continue;
                            }
                            List<String> sign = new ArrayList<>();

                            if (BasisController.getBlockchainSize() > Seting.PORTION_BLOCK_TO_COMPLEXCITY && BasisController.getBlockchainSize() < Seting.V34_NEW_ALGO) {
                                lastDiff = UtilsBlockToEntityBlock.entityBlocksToBlocks(
                                        blockService.findBySpecialIndexBetween(
                                                (BasisController.prevBlock().getIndex() + 1) - Seting.PORTION_BLOCK_TO_COMPLEXCITY,
                                                BasisController.prevBlock().getIndex() + 1
                                        )
                                );
                            }

                            DataShortBlockchainInformation temp = new DataShortBlockchainInformation();
                            temp = Blockchain.shortCheckWithCanonicalContext(
                                    BasisController.prevBlock(), subBlocks, BasisController.getShortDataBlockchain(),
                                    lastDiff, tempBalances, sign, validationBalance, new ArrayList<>(),
                                    v35CanonicalContextBefore(BasisController.prevBlock()), Seting.V35_RESCUE_FORK_INDEX);

                            jsonGlobalData = UtilUrl.readJsonFromUrl(s + "/datashort");
                            if (jsonGlobalData == null || jsonGlobalData.isEmpty() || jsonGlobalData.isBlank()) {
                                System.out.println("*********************************************************");
                                System.out.println("jsonGlobalData: error: " + jsonGlobalData);
                                MyLogger.saveLog("jsonGlobalData == null || jsonGlobalData.isEmpty() || jsonGlobalData.isBlank():" + jsonGlobalData);
                                System.out.println("*********************************************************");
                                continue;
                            }
                            System.out.println("3: jsonGlobalData: " + jsonGlobalData);
                            global = UtilsJson.jsonToDataShortBlockchainInformation(jsonGlobalData);


                            System.out.println("3: temp: " + temp);
                            System.out.println("3: blockchainsize: " + BasisController.getBlockchainSize());
                            System.out.println("3: sublocks: " + subBlocks.size());


                            balances = UtilsAccountToEntityAccount.entityAccountsToMapAccounts(UtilsUse.accounts(subBlocks, blockService));
                            tempBalances = UtilsAccountToEntityAccount.entityAccountsToMapAccounts(UtilsUse.accounts(subBlocks, blockService));
                            sign = new ArrayList<>();
                            if (!local_size_upper) {
                                System.out.println("===========================");
                                System.out.println("!local_size_upper: " + !local_size_upper);
                                System.out.println("===========================");
                                temp = helpResolve(temp, global, s, lastDiff, tempBalances, sign, balances, subBlocks, true);
                                MyLogger.saveLog("!local_size_upper temp: after" + temp);
                                MyLogger.saveLog("!local_size_upper global: after" + global);


                            }

                            if (local_size_upper) {
                                System.out.println("===========================");
                                System.out.println("local_size_upper: " + local_size_upper);
                                System.out.println("===========================");
                                temp = helpResolve(temp, global, s, lastDiff, tempBalances, sign, balances, subBlocks, true);
                                MyLogger.saveLog("local_size_upper temp: after" + temp);
                                MyLogger.saveLog("local_size_upper global: after" + global);
                            }


                            if (!temp.isValidation()) {
                                continue;
                            }

                        }
                        System.out.println("size temporaryBlockchain: ");
                        System.out.println("resolve: temporaryBlockchain: ");
                    } else {
                        System.out.println(":BasisController: resove: size less: " + size + " address: " + s);
                        continue;
                    }
                } catch (Exception e) {
                    recordResolveException(s, e);
                    e.printStackTrace();
                    MyLogger.saveLog("resolve3: error: "  + entry.getKey(), e);
                    String stack = "";
                    for (StackTraceElement stack1 : e.getStackTrace()) {
                        stack += stack1.toString() + "\n";
                    }



                    continue;
                }
            }

        } finally {

            BasisController.setUpdating(false);
            lastResolve3EndMillis = System.currentTimeMillis();
            lastResolve3DurationMillis = lastResolve3EndMillis - lastResolve3StartMillis;
            PeerHealthRegistry.recordResolveFinished(lastResolve3DurationMillis, resolveBudgetExhausted);
            NodeMemoryProfiler.recordStage("resolve3 finish", NodeMemoryProfiler.metrics(
                    "durationMs", lastResolve3DurationMillis,
                    "localBlockchainSize", BasisController.getBlockchainSize(),
                    "bigSize", bigSize
            ));
            NodeMemoryProfiler.endTrace("resolve3 finish");
            if (BasisController.getBlockchainSize() > bigSize) {
                return 1;
            } else if (BasisController.getBlockchainSize() < bigSize) {
                return -1;
            } else {
                return 0;
            }

        }


    }

    /**
     * Ð¼ÐµÑ‚Ð¾Ð´ Ð² ÐºÐ¾ÑˆÐµÐ»ÑŒÐºÐµ Ð¾Ð±Ð½Ð¾Ð²Ð»ÑÐµÑ‚ÑÑ ÐµÑÐ»Ð¸ Ñ†ÐµÐ½Ð½Ð¾ÑÑ‚ÑŒ ÐºÐ¾ÑˆÐµÐ»ÑŒÐºÐ° Ð¾Ñ‚Ð»Ð¸Ñ‡Ð°ÐµÑ‚ÑÑ Ð¾Ñ‚ ÑÐµÑ€Ð²ÐµÑ€Ð°
     */
    public boolean isBig(
            DataShortBlockchainInformation actual,
            DataShortBlockchainInformation global) {
        if (HostEndDataShortBComparator.shouldUseV35Comparison(actual, global)) {
            return HostEndDataShortBComparator.compareV35(global, actual) > 0;
        }
        if (global.getBigRandomNumber() > actual.getBigRandomNumber()) {
            return true;
        }
        return false;

    }


    public boolean isSmall(DataShortBlockchainInformation expected, DataShortBlockchainInformation actual) {
        if (HostEndDataShortBComparator.shouldUseV35Comparison(expected, actual)) {
            return HostEndDataShortBComparator.compareV35(expected, actual) > 0;
        }
        if (
                actual.getSize() < expected.getSize()
                        || actual.getBigRandomNumber() < expected.getBigRandomNumber()
        ) {
            return true;
        }
        return false;
    }




    /**
     * Ð¼ÐµÑ‚Ð¾Ð´ ÑÑ€Ð°Ð±Ð°Ñ‚Ñ‹Ð²Ð°ÐµÑ‚ ÐµÑÐ»Ð¸ Ð²Ñ‹ÑÐ¾Ñ‚ÐºÐ° Ð»Ð¾ÐºÐ°Ð»ÑŒÐ½Ð¾Ð³Ð¾ Ð±Ð»Ð¾ÐºÑ‡ÐµÐ¹Ð½Ð° Ð¼ÐµÐ½ÑŒÑˆÐµ Ð¸Ð»Ð¸ Ñ€Ð°Ð²Ð½Ð¾ Ð¾Ñ‚Ð½Ð¾ÑÐ¸Ñ‚ÐµÐ»ÑŒÐ½Ð¾ Ð³Ð»Ð¾Ð±Ð°Ð»ÑŒÐ½Ð¾Ð³Ð¾ ÑÐµÑ€Ð²ÐµÑ€Ð°
     */
    public DataShortBlockchainInformation helpResolve(DataShortBlockchainInformation temp,
                                                      DataShortBlockchainInformation global,
                                                      String s,
                                                      List<Block> lastDiff,
                                                      Map<String, Account> tempBalances,
                                                      List<String> sign,
                                                      Map<String, Account> balances,
                                                      List<Block> subBlocks,
                                                      boolean checking)
            throws CloneNotSupportedException, IOException, NoSuchAlgorithmException, SignatureException,
            InvalidKeySpecException, NoSuchProviderException, InvalidKeyException {
        //TODO ÑÐ½Ð°Ñ‡Ð°Ð»Ð° Ð½Ð°Ð¹Ñ‚Ð¸ Ð±Ð»Ð¾Ðº Ð¾Ñ‚ÐºÑƒÐ´Ð° Ð½Ð°Ñ‡Ð¸Ð½Ð°ÐµÑ‚ÑÑ Ð¾Ñ‚Ð²ÐµÑ‚Ð»ÐµÐ½Ð¸Ðµ Ð¸ Ð´Ð¾ÐºÑƒÐ´Ð°

        NodeMemoryProfiler.recordStage("helpResolve start", NodeMemoryProfiler.metrics(
                "peer", s,
                "localSize", BasisController.getBlockchainSize(),
                "peerSize", global == null ? -1 : global.getSize(),
                "lastDiffSize", lastDiff == null ? 0 : lastDiff.size(),
                "subBlocksSize", subBlocks == null ? 0 : subBlocks.size()
        ));
        Map<String, Account> tempBalance = UtilsUse.balancesClone(tempBalances);

        //ÐµÑÐ»Ð¸ Ð¼ÐµÑ‚Ð° Ð´Ð°Ð½Ð½Ñ‹Ðµ Ð¿Ð¾Ð»ÑƒÑ‡ÐµÐ½Ð½Ñ‹Ðµ Ð½Ðµ Ð¿Ñ€Ð°Ð²Ð¸Ð»ÑŒÐ½Ñ‹, Ñ‚Ð¾ Ð¾Ð½ Ð´ÐµÐ»Ð°ÐµÑ‚ Ð´Ð¾Ð¿Ð¾Ð»Ð½Ð¸Ñ‚ÐµÐ»ÑŒÐ½ÑƒÑŽ Ð¿Ñ€Ð¾Ð²ÐµÑ€ÐºÑƒ,
        //Ñ‡Ñ‚Ð¾Ð±Ñ‹ Ð²Ñ‹ÑÑÐ½Ð¸Ñ‚ÑŒ, ÐºÐ°ÐºÐ¸Ðµ Ð±Ð»Ð¾ÐºÐ¸ ÑÑ‚Ð¾Ð¸Ñ‚ ÑƒÐ´Ð°Ð»Ð¸Ñ‚ÑŒ Ð¸Ð· Ð»Ð¾ÐºÐ°Ð»ÑŒÐ½Ð¾Ð¹ Ñ†ÐµÐ¿Ð¾Ñ‡ÐºÐ¸ Ð¸ Ð¾Ñ‚ÐºÐ°Ñ‚Ð¸Ñ‚ÑŒ, Ñ‡Ñ‚Ð¾Ð±Ñ‹
        //Ð¾Ñ‚ÐºÐ°Ñ‚Ð¸Ñ‚ÑŒ Ð±Ð»Ð¾ÐºÐ¸, Ð¼ÐµÑ‚Ð° Ð´Ð°Ð½Ð½Ñ‹Ðµ Ð¸ Ð±Ð°Ð»Ð°Ð½Ñ, Ð¸ Ð¿ÐµÑ€ÐµÐ¹Ñ‚Ð¸ Ð½Ð° Ð±Ð¾Ð»ÐµÐµ Ñ†ÐµÐ½Ð½ÑƒÑŽ Ð²ÐµÑ‚ÐºÑƒ.
        if (BasisController.getShortDataBlockchain().getSize() > 1 && !temp.isValidation()) {
            System.out.println("__________________________________________________________");

            List<Block> emptyList = new ArrayList<>();
            List<Block> different = new ArrayList<>();

            int lastBlockIndex = (int) (global.getSize() - 1);
            int currentIndex = lastBlockIndex;
            //TODO Ñ‚ÐµÑÑ‚Ð¾Ð²Ð°Ñ Ð²ÐµÑ€ÑÐ¸Ñ, Ð¼Ñ‹ Ð¿Ñ€Ð¾Ð²ÐµÑ€ÑÐµÐ¼ ÐµÑÐ»Ð¸ Ð±Ð»Ð¾ÐºÑ‡ÐµÐ¹Ð½ Ñ†ÐµÐ½ÐµÐµ, Ð½Ð¾ Ð¿Ñ€Ð¸ ÑÑ‚Ð¾Ð¼ Ð¼ÐµÐ½ÑŒÑˆÐµ
            if (global.getSize() < BasisController.getBlockchainSize()) {
                List<EntityBlock> entityBlocks = blockService.findBySpecialIndexBetween(global.getSize(), BasisController.getBlockchainSize() - 1);
                different.addAll(UtilsBlockToEntityBlock.entityBlocksToBlocks(entityBlocks));
            }
            try {

                //Ð·Ð´ÐµÑÑŒ ÑÐºÐ°Ñ‡Ð¸Ð²Ð°ÑŽÑ‚ÑÑ Ð±Ð»Ð¾ÐºÐ¸ Ð¿Ð¾Ñ€Ñ†Ð¸ÑÐ¼Ð¸, Ñ‡Ñ‚Ð¾Ð±Ñ‹ Ð²Ñ‹Ñ‡Ð¸ÑÐ»Ð¸Ñ‚ÑŒ Ñ‚Ð¾Ñ‡ÐºÑƒ Ð¿ÐµÑ€ÐµÑÐµÑ‡ÐµÐ½Ð¸Ñ, ÐºÐ¾Ð³Ð´Ð° Ð¾Ð½Ð¸ Ñ€Ð°Ð·Ð´ÐµÐ»Ð¸Ð»Ð¸ÑÑŒ
                stop:
                while (currentIndex >= 0) {

                    int startIndex = Math.max(currentIndex - 499, 0);
                    int endIndex = currentIndex;


                    SubBlockchainEntity subBlockchainEntity = new SubBlockchainEntity(startIndex, endIndex);
                    String subBlockchainJson = UtilsJson.objToStringJson(subBlockchainEntity);

                    NodeMemoryProfiler.recordStage("helpResolve before sub-blocks download", NodeMemoryProfiler.metrics(
                            "peer", s,
                            "startIndex", startIndex,
                            "endIndex", endIndex,
                            "requestBytes", subBlockchainJson == null ? 0 : subBlockchainJson.length()
                    ));
                    List<Block> blockList = UtilsJson.jsonToObject(UtilUrl.getObject(subBlockchainJson, s + "/sub-blocks"));
                    NodeMemoryProfiler.recordStage("helpResolve after sub-blocks download/parse", NodeMemoryProfiler.metrics(
                            "peer", s,
                            "startIndex", startIndex,
                            "endIndex", endIndex,
                            "downloadedBlockListSize", blockList == null ? 0 : blockList.size()
                    ));
                    System.out.println("subBlockchainEntity: " + subBlockchainEntity);
                    if (blockList == null || blockList.isEmpty()) {
                        recordResolveSkipped(RESOLVE_SKIPPED_PEER_CANNOT_SUPPLY_BLOCKS, s,
                                BasisController.getBlockchainSize(), global.getSize(),
                                Math.max(0, global.getSize() - BasisController.getBlockchainSize()), 0,
                                temp != null && temp.isValidation());
                        return temp;
                    }

                    blockList = blockList.stream().sorted(Comparator.comparing(Block::getIndex).reversed()).collect(Collectors.toList());
                    for (Block block : blockList) {
                        System.out.println("helpResolve4: block index: " + block.getIndex());

                        //Ð•ÑÐ»Ð¸ Ð±Ð»Ð¾Ðº Ð¸Ð¼ÐµÐµÑ‚ Ð¸Ð½Ð´ÐµÐºÑ Ð²Ñ‹ÑˆÐµ Ð²Ñ‹ÑÐ¾Ñ‚Ñ‹ Ð»Ð¾ÐºÐ°Ð»ÑŒÐ½Ð¾Ð³Ð¾, Ð¾Ð½ Ð°Ð²Ñ‚Ð¾Ð¼Ð°Ñ‚Ð¸Ñ‡ÐµÑÐºÐ¸ Ð´Ð¾Ð±Ð°Ð²Ð»ÑÐµÑ‚ÑÑ Ð² ÑÐ¿Ð¸ÑÐ¾Ðº.
                        //Ð•ÑÐ»Ð¸ Ð² Ð»Ð¾ÐºÐ°Ð»ÑŒÐ½Ð¾Ð¼ ÑƒÐ¶Ðµ ÐµÑÑ‚ÑŒ Ð±Ð»Ð¾Ðº Ñ Ñ‚Ð°ÐºÐ¸Ð¼ Ð¸Ð½Ð´ÐµÐºÑÐ¾Ð¼, Ð¾Ð½Ð¸ Ð¾Ð±Ð° Ð´Ð¾Ð±Ð°Ð²Ð»ÑÑŽÑ‚ÑÑ Ð² ÑÐ²Ð¾Ð¸ ÑÐ¿Ð¸ÑÐºÐ¸.
                        //Ð¡Ð¿Ð¸ÑÐ¾Ðº emptyList ÑÑ‚Ð¾ Ð±Ð»Ð¾ÐºÐ¸ Ð´Ð»Ñ ÑÐ¾Ñ…Ñ€Ð°Ð½ÐµÐ½Ð¸Ñ Ð±Ð»Ð¾ÐºÐ¾Ð² Ð¸Ð· Ñ†ÐµÐ¿Ð¾Ñ‡ÐºÐ¸ Ð³Ð»Ð¾Ð±Ð°Ð»ÑŒÐ½Ð¾Ð³Ð¾ Ð±Ð»Ð¾ÐºÑ‡ÐµÐ¹Ð½Ð°
                        //Ð¡Ð¿Ð¸ÑÐ¾Ðº different ÑÑ‚Ð¾ Ð±Ð»Ð¾ÐºÐ¸ ÐºÐ¾Ñ‚Ð¾Ñ€Ñ‹Ðµ Ð½ÑƒÐ¶Ð½Ð¾ ÑƒÐ´Ð°Ð»Ð¸Ñ‚ÑŒ Ð¸Ð· Ð»Ð¾ÐºÐ°Ð»ÑŒÐ½Ð¾Ð³Ð¾ Ð±Ð»Ð¾ÐºÑ‡ÐµÐ¹Ð½Ð°.
                        if (block.getIndex() > BasisController.getBlockchainSize() - 1) {
                            System.out.println("helpResolve4 :download blocks: " + block.getIndex() +
                                    " your block : " + (BasisController.getBlockchainSize()) + ":waiting need download blocks: " + (block.getIndex() - BasisController.getBlockchainSize())
                                    + " host: " + s);
                            emptyList.add(block);
                        } else if (!blockService.findBySpecialIndex(block.getIndex()).getHashBlock().equals(block.getHashBlock())) {
                            emptyList.add(block);
                            different.add(UtilsBlockToEntityBlock.entityBlockToBlock(blockService.findBySpecialIndex(block.getIndex())));
                            System.out.println("********************************");
                            System.out.println(":dowdnload block index: " + block.getIndex());
                            System.out.println(":block original index: " + blockService.findBySpecialIndex(block.getIndex()).getIndex());
                            System.out.println(":block from index: " + block.getIndex());
                        } else {
                            // ÐžÑÑ‚Ð°Ð½Ð°Ð²Ð»Ð¸Ð²Ð°ÐµÐ¼ Ð¸Ñ‚ÐµÑ€Ð°Ñ†Ð¸ÑŽ, Ñ‚.Ðº. Ð´Ð°Ð»ÑŒÐ½ÐµÐ¹ÑˆÐ¸Ðµ Ð±Ð»Ð¾ÐºÐ¸ Ð±ÑƒÐ´ÑƒÑ‚ Ð¸Ð´ÐµÐ½Ñ‚Ð¸Ñ‡Ð½Ñ‹Ð¼Ð¸
                            break stop;
                        }
                    }

                    // ÐžÐ±Ð½Ð¾Ð²Ð»ÑÐµÐ¼ Ð¸Ð½Ð´ÐµÐºÑ Ð´Ð»Ñ ÑÐ»ÐµÐ´ÑƒÑŽÑ‰ÐµÐ¹ Ð¸Ñ‚ÐµÑ€Ð°Ñ†Ð¸Ð¸
                    currentIndex = startIndex - 1;
                }

            } catch (Exception e) {
                System.out.println("******************************");
                System.out.println("connecting exeption helpresolve4: address: " + s);
                System.out.println("******************************");
                return temp;
            }

            if(emptyList.isEmpty()){
                recordResolveSkipped(RESOLVE_SKIPPED_EMPTY_DOWNLOAD, s,
                        BasisController.getBlockchainSize(), global.getSize(),
                        Math.max(0, global.getSize() - BasisController.getBlockchainSize()), 0,
                        temp != null && temp.isValidation());
                NodeMemoryProfiler.recordStage("helpResolve skip empty download", NodeMemoryProfiler.metrics(
                        "peer", s,
                        "emptyListSize", 0,
                        "differentListSize", different == null ? 0 : different.size()
                ));
                return temp;
            }
            System.out.println("different: ");
            if (different.isEmpty() && emptyList.isEmpty()) {
                return temp;
            }

            //Ð¿Ð¾Ð»ÑƒÑ‡Ð°ÐµÐ¼ Ð±Ð°Ð»Ð°Ð½Ñ Ð²ÑÐµÑ… ÑÑ‡ÐµÑ‚Ð¾Ð² Ð¸Ð· Ð»Ð¾ÐºÐ°Ð»ÑŒÐ½Ð¾Ð¹ Ð±Ð°Ð·Ñ‹ h2, Ð½Ð° Ð¾ÑÐ½Ð¾Ð²Ðµ ÑÑ‡ÐµÑ‚Ð¾Ð² ÐºÐ¾Ñ‚Ð¾Ñ€Ñ‹Ðµ Ð¿Ñ€Ð¸ÑÑƒÑ‚ÑÑ‚Ð²ÑƒÑŽÑ‚ Ð² ÑÐ¿Ð¸ÑÐºÐµ Ð±Ð»Ð¾ÐºÐ¾Ð².
            balances.putAll(UtilsAccountToEntityAccount.entityAccountsToMapAccounts(UtilsUse.accounts(different, blockService)));
            balances.putAll(UtilsAccountToEntityAccount.entityAccountsToMapAccounts(UtilsUse.accounts(emptyList, blockService)));
            balances.putAll(UtilsAccountToEntityAccount.entityAccountsToMapAccounts(UtilsUse.accounts(lastDiff, blockService)));
            tempBalance.putAll(UtilsAccountToEntityAccount.entityAccountsToMapAccounts(UtilsUse.accounts(different, blockService)));
            tempBalance.putAll(UtilsAccountToEntityAccount.entityAccountsToMapAccounts(UtilsUse.accounts(emptyList, blockService)));
            tempBalance.putAll(UtilsAccountToEntityAccount.entityAccountsToMapAccounts(UtilsUse.accounts(lastDiff, blockService)));

            System.out.println("shortDataBlockchain: " + BasisController.getShortDataBlockchain());
            System.out.println("rollback temp: " + temp);

            //ÑÐ¾Ñ€Ñ‚Ð¸Ñ€ÑƒÐµÐ¼ Ð±Ð»Ð¾ÐºÐ¸ Ð¿Ð¾ Ð²Ð¾Ð·Ñ€Ð°ÑÑ‚Ð°Ð½Ð¸ÑŽ
            different = different.stream().filter(UtilsUse.distinctByKey(Block::getIndex)).sorted(Comparator.comparing(Block::getIndex)).collect(Collectors.toList());
            emptyList = emptyList.stream().filter(UtilsUse.distinctByKey(Block::getIndex)).sorted(Comparator.comparing(Block::getIndex)).collect(Collectors.toList());
            NodeMemoryProfiler.recordStage("helpResolve before rollback comparison", NodeMemoryProfiler.metrics(
                    "peer", s,
                    "differentListSize", different == null ? 0 : different.size(),
                    "downloadedBlockListSize", emptyList == null ? 0 : emptyList.size(),
                    "balancesSize", balances == null ? 0 : balances.size(),
                    "tempBalanceSize", tempBalance == null ? 0 : tempBalance.size()
            ));
            if (shouldSkipInvalidRollback(different, emptyList, temp, s,
                    BasisController.getBlockchainSize(), global.getSize())) {
                return temp;
            }
            Block tempPrevBlock = UtilsBlockToEntityBlock.entityBlockToBlock(blockService.findBySpecialIndex(different.get(0).getIndex() - 1));

            //Ð¾Ñ‚ÐºÐ°Ñ‚Ñ‹Ð²Ð°ÐµÐ¼ Ð¼ÐµÑ‚Ð° Ð´Ð°Ð½Ð½Ñ‹Ðµ
            temp = Blockchain.rollBackShortCheck(different, BasisController.getShortDataBlockchain(), tempBalance);
            //Ð±Ð°Ð»Ð°Ð½Ñ Ð´Ð»Ñ Ð²Ð°Ð»Ð¸Ð´Ð°Ñ†Ð¸Ð¸ Ð¼ÐµÑ‚Ð° Ð´Ð°Ð½Ð½Ñ‹Ñ…, ÑÑ‚Ð¾Ñ‚ ÑÐ¿Ð¸ÑÐ¾Ðº Ð±Ð°Ð»Ð°Ð½ÑÐ° Ð¸ÑÐ¿Ð¾Ð»ÑŒÐ·ÑƒÐµÑ‚ÑÑ Ð´Ð»Ñ Ð¿Ñ€Ð¾Ð²ÐµÑ€ÐºÐ¸, Ð´Ð¾ÑÑ‚Ð°Ñ‚Ð¾Ñ‡Ð½Ð¾ Ð»Ð¸ Ð±Ñ‹Ð»Ð¾ Ð´ÐµÐ½ÐµÐ³ Ñƒ Ð¾Ñ‚Ð¿Ñ€Ð°Ð²Ð¸Ñ‚ÐµÐ»ÐµÐ¹
            //Ð²Ð¾ Ð²Ñ€ÐµÐ¼Ñ Ð¾Ñ‚Ð¿Ñ€Ð°Ð²ÐºÐ¸ Ð´ÐµÐ½ÐµÐ³.
            Map<String, Account> balanceForValidation = UtilsUse.balancesClone(balances);

            if (requiresV35JackpotAccounting(different)) {
                balanceForValidation = UtilsBalance.calculateBalances(
                        v35CanonicalContextBeforeIndex(different.get(0).getIndex() - 1),
                        Seting.ADDRESS_FOUNDER,
                        Seting.V35_RESCUE_FORK_INDEX
                );
            } else {
                for (int i = different.size() - 1; i >= 0; i--) {
                    Block block = different.get(i);

                //Ð¾Ñ‚ÐºÐ°Ñ‚Ñ‹Ð²Ð°ÐµÑ‚ Ð±Ð°Ð»Ð°Ð½Ñ Ð½Ð° Ð¾ÑÐ½Ð¾Ð²Ðµ Ð±Ð»Ð¾ÐºÐ¾Ð².
                    balanceForValidation = rollbackCalculateBalance(balanceForValidation, block);

                }
            }

            //Ð·Ð´ÐµÑÑŒ Ð¿Ð¾Ð´ÑÑ‡Ð¸Ñ‚Ñ‹Ð²Ð°ÐµÑ‚ Ñ†ÐµÐ»Ð¾ÑÑ‚Ð½Ð¾ÑÑ‚ÑŒ Ð¼ÐµÑ‚Ð° Ð´Ð°Ð½Ð½Ñ‹Ñ…, Ð° Ñ‚Ð°ÐºÐ¶Ðµ Ñ†ÐµÐ»Ð¾ÑÑ‚Ð½Ð¾ÑÑ‚ÑŒ Ð±Ð»Ð¾ÐºÑ‡ÐµÐ¹Ð½Ð°, Ñ‚Ð°Ðº Ð¶Ðµ balanceForValidation Ð¸ÑÐ¿Ð¾Ð»ÑŒÐ·ÑƒÐµÑ‚ÑÑ
            //Ð´Ð»Ñ Ð¿Ñ€Ð¾Ð²ÐµÑ€ÐºÐ¸ Ð´Ð¾ÑÑ‚Ð°Ñ‚Ð¾Ñ‡Ð½Ð¾ Ð»Ð¸ Ð±Ð°Ð»Ð°Ð½ÑÐ° Ð±Ñ‹Ð»Ð¾ Ñƒ Ð¾Ñ‚Ð¿Ñ€Ð°Ð²Ð¸Ñ‚ÐµÐ»ÐµÐ¹ Ð² Ð±Ð»Ð¾ÐºÐ°Ñ….
            Base base = new Base58();
            List<String> signaturesNotTakenIntoAccount = new ArrayList<>();
            for (Block block : different) {
                List<String> tempSign = block.getDtoTransactions().stream().map(t -> base.encode(t.getSign())).collect(Collectors.toList());
                signaturesNotTakenIntoAccount.addAll(tempSign);
            }

            List<Block> v35ReplacementContext = v35CanonicalContextBefore(tempPrevBlock);
            for (Block block : emptyList) {
                List<Block> tempList = new ArrayList<>();
                tempList.add(block);
                temp = Blockchain.shortCheckWithCanonicalContext(tempPrevBlock, tempList, temp, lastDiff, tempBalance, sign, balanceForValidation, signaturesNotTakenIntoAccount, v35ReplacementContext, Seting.V35_RESCUE_FORK_INDEX);
                if (temp.isValidation()) {
                    v35ReplacementContext.add(block);
                }
                tempPrevBlock = block;
            }

            //Ð±Ð»Ð¾ÐºÐ¸Ñ€ÑƒÐµÑ‚ Ñ…Ð¾ÑÑ‚Ñ‹ ÑÐµÑ€Ð²ÐµÑ€Ð¾Ð², ÐºÐ¾Ñ‚Ð¾Ñ€Ñ‹Ðµ Ð½Ð°Ð¼ÐµÑ€ÐµÐ½Ð¾ Ð¸Ð»Ð¸ ÑÐ»ÑƒÑ‡Ð°Ð¹Ð½Ð¾ ÑÐ¾Ð²Ñ€Ð°Ð»Ð¸ Ð¾ ÑÐ²Ð¾ÐµÐ¹ Ñ†ÐµÐ½Ð½Ð¾ÑÑ‚Ð¸.
            if (Seting.IS_SECURITY == true && checking && isSmall(global, temp)) {

                String expectedJson = "is small roll back global: " + UtilsJson.objToStringJson(global);
                String actualJson = "is small roll back actual: " + UtilsJson.objToStringJson(temp);
                UtilsFileSaveRead.save(expectedJson + "\n " + actualJson + "\n host: " + s, Seting.ERROR_FILE, true);

                UtilsAllAddresses.saveAllAddresses(s, Seting.ORIGINAL_POOL_URL_ADDRESS_BLOCKED_FILE);

                temp.setValidation(false);
                return temp;
            }


            System.out.println("after rollback: " + temp);
            //ÐµÑÐ»Ð¸ Ð¿Ð¾ÑÐ»Ðµ Ð¿ÐµÑ€ÐµÑ€Ð°ÑÑ‡ÐµÑ‚Ð°, Ð¿Ð¾Ð´Ñ‚Ð²ÐµÑ€Ð´Ð¸Ñ‚ÑŒÑÑ Ñ‡Ñ‚Ð¾ Ð´Ñ€ÑƒÐ³Ð°Ñ Ð²ÐµÑ‚ÐºÐ° Ð±Ð¾Ð»ÐµÐµ Ñ†ÐµÐ½Ð½Ð°Ñ
            // Ð¸ Ð¾Ð½Ð° Ñ†ÐµÐ»Ð°Ñ, Ñ‚Ð¾ Ð²ÐºÐ»ÑŽÑ‡Ð°ÐµÑ‚ÑÑ ÑÑ‚Ð¾Ñ‚ Ð±Ð»Ð¾Ðº, ÐºÐ¾Ñ‚Ð¾Ñ€Ñ‹Ð¹ Ð·Ð°Ð¿Ð¸ÑÑ‹Ð²Ð°ÐµÑ‚ Ð¾ÐºÐ¾Ð½Ñ‡Ð°Ñ‚ÐµÐ»ÑŒÐ½Ð¾ Ð½Ð¾Ð²ÑƒÑŽ Ñ†ÐµÐ¿Ð¾Ñ‡ÐºÑƒ,
            //ÑƒÐ´Ð°Ð»ÑÑ Ð¿Ñ€Ð¸ ÑÑ‚Ð¾Ð¼ Ð±Ð»Ð¾ÐºÐ¸, Ð³Ð´Ðµ Ð¿Ñ€Ð¾Ð¸Ð·Ð¾ÑˆÐ»Ð¾ Ð¾Ñ‚Ð²ÐµÑ‚Ð²Ð»ÐµÐ½Ð¸Ðµ Ñ†ÐµÐ¿Ð¾Ñ‡ÐµÐº.
            if (temp.isValidation()) {
                System.out.println("------------------------------------------");
                System.out.println("rollback");
                try {
                    boolean save;
                    boolean result = true;
                    //Ð¼ÐµÑ‚Ð¾Ð´ rollBackAddBlock3 Ð¾Ñ‚ÐºÐ°Ñ‚Ñ‹Ð²Ð°ÐµÑ‚ Ð±Ð°Ð»Ð°Ð½Ñ Ð»Ð¾ÐºÐ°Ð»ÑŒÐ½Ñ‹Ð¹, ÐµÑÐ»Ð¸ Ð¾Ð½ Ð¿Ñ€Ð°Ð²Ð¸Ð»ÑŒÐ½Ð¾ Ð²Ñ‹Ð¿Ð¾Ð»Ð½Ð¸Ð»,
                    //Ñ‚Ð¾ Ð¾Ð½ Ð²ÐµÑ€Ð½ÐµÑ‚ ÑÐ¿Ð¸ÑÐ¾Ðº Ð±Ð°Ð»Ð°Ð½ÑÐ¾Ð² ÐºÐ¾Ñ‚Ð¾Ñ€Ñ‹Ðµ Ð¾Ñ‚ÐºÐ°Ñ‚Ð¸Ð»Ð¸ÑÑŒ.
                    Map<String, Account> tempBalanc = rollBackAddBlock3(different, emptyList, balances, Seting.ORIGINAL_BLOCKCHAIN_FILE);
                    if (tempBalanc.size() > 0) {
                        //ÐµÑÐ»Ð¸ Ð¼ÐµÑ‚Ð¾Ð´ rollBackAddBlock3 Ð¾Ñ‚ÐºÐ°Ñ‚Ð¸Ð» Ð±Ð°Ð»Ð°Ð½ÑÑ‹, Ñ‚Ð¾ Ð¾Ð½ ÑÐ¾Ñ…Ñ€Ð°Ð½Ð¸Ñ‚ ÑÑ‚Ð¾Ñ‚ ÑÐ¿Ð¸ÑÐ¾Ðº Ð±Ð°Ð»Ð°Ð½ÑÐ¾Ð² Ñ Ð¸Ð·Ð¼ÐµÐ½ÐµÐ½Ð¸ÑÐ¼Ð¸,
                        //Ð° Ñ‚Ð°Ðº Ð¶Ðµ ÑÐ¿Ð¸ÑÐ¾Ðº Ð±Ð»Ð¾ÐºÐ¾Ð².
                        MyLogger.saveLog("addBllock3 from rollback: ");
                        result = addBlock3(emptyList, tempBalanc, Seting.ORIGINAL_BLOCKCHAIN_FILE, signaturesNotTakenIntoAccount);

                    } else {
                        result = false;
                        TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
                        MyLogger.saveLog("wrong: addblock3 from rollback");
                    }

                    //ÐµÑÐ»Ð¸ ÑÐ¾Ñ…Ñ€Ð°Ð½ÐµÐ½Ð¸Ðµ Ð¿Ñ€Ð¾Ð¸Ð·Ð¾ÑˆÐ»Ð¾ ÑƒÑÐ¿ÐµÑˆÐ½Ð¾, Ñ‚Ð¾ Ð¼ÐµÑ‚Ð° Ð´Ð°Ð½Ð½Ñ‹Ðµ Ñ‚Ð°Ðº Ð¶Ðµ Ð·Ð°Ð¿Ð¸ÑÑ‹Ð²Ð°ÑŽÑ‚ÑÑ ÑƒÑÐ¿ÐµÑˆÐ½Ð¾
                    if (result) {
                        BasisController.setShortDataBlockchain(temp);
                        BasisController.setBlockcheinSize((int) temp.getSize());
                        BasisController.setBlockchainValid(temp.isValidation());
                        EntityBlock tempBlock = blockService.findBySpecialIndex(BasisController.getBlockchainSize() - 1);
                        BasisController.setPrevBlock(UtilsBlockToEntityBlock.entityBlockToBlock(tempBlock));
                        String json = UtilsJson.objToStringJson(BasisController.getShortDataBlockchain());
                        UtilsFileSaveRead.save(json, Seting.TEMPORARY_BLOCKCHAIN_FILE, false);

                        return temp;
                    }


                    System.out.println("------------------------------------------");
                    System.out.println("emptyList start index: " + emptyList.get(0).getIndex());
                    System.out.println("emptyList finish index: " + emptyList.get(emptyList.size() - 1).getIndex());
                    System.out.println("==========================================");
                    System.out.println("different start index: " + different.get(0).getIndex());
                    System.out.println("different finish index: " + different.get(different.size() - 1).getIndex());
                    System.out.println("------------------------------------------");
                } catch (Exception e) {
                    MyLogger.saveLog("error helpResolve: ", e);
                }
                System.out.println("------------------------------------------");
                System.out.println("helpResolve4: temp: " + temp);
                System.out.println("------------------------------------------");
            } else {
                return temp;
            }


        } else if (BasisController.getShortDataBlockchain().getSize() > 1 && temp.isValidation()) {
            //Ð²Ñ‹Ð·Ñ‹Ð²Ð°ÐµÑ‚ Ð¼ÐµÑ‚Ð¾Ð´Ñ‹, Ð´Ð»Ñ ÑÐ¾Ñ…Ñ€Ð°Ð½ÐµÐ½Ð¸Ñ ÑÐ¿Ð¸ÑÐºÐ° Ð±Ð»Ð¾ÐºÐ¾Ð² Ð² Ñ‚ÐµÐºÑƒÑ‰Ð¸Ð¹ Ð±Ð»Ð¾ÐºÑ‡ÐµÐ¹Ð½,
            //Ñ‚Ð°Ðº Ð¶Ðµ Ð·Ð°Ð¿Ð¸ÑÑ‹Ð²Ð°ÐµÑ‚ Ð² Ð±Ð°Ð·Ñƒ h2, Ð´ÐµÐ»Ð°ÐµÑ‚ Ð¿ÐµÑ€ÐµÑ€Ð°ÑÑ‡ÐµÑ‚ Ð²ÑÐµÑ… Ð±Ð°Ð»Ð°Ð½ÑÐ¾Ð²,
            //Ð¸ Ñ‚Ð°Ðº Ð¶Ðµ Ð¸Ñ… Ð·Ð°Ð¿Ð¸ÑÑ‹Ð²Ð°ÐµÑ‚, Ð° Ñ‚Ð°Ðº Ð¶Ðµ Ð·Ð°Ð¿Ð¸ÑÑ‹Ð²Ð°ÐµÑ‚ Ð´Ñ€ÑƒÐ³Ð¸Ðµ Ð´Ð°Ð½Ð½Ñ‹Ðµ.
            //Ð­Ñ‚Ð¾Ñ‚ Ð±Ð»Ð¾Ðº ÑÐ¾Ñ…Ñ€Ð°Ð½ÑÐµÑ‚ Ð½Ðµ Ð´Ð¾ÑÑ‚Ð°ÑŽÑ‰Ð¸Ðµ Ð±Ð»Ð¾ÐºÐ¸, ÐµÑÐ»Ð¸ Ð¸Ð·Ð½Ð°Ñ‡Ð°Ð»ÑŒÐ½Ð¾ Ð½Ðµ Ð±Ñ‹Ð»Ð¾ Ñ€Ð°Ð·Ð´ÐµÐ»ÐµÐ½Ð¸Ðµ Ð²ÐµÑ‚Ð¾Ðº,
            //Ð½Ð¾ Ð¿Ñ€Ð¸ ÑÑ‚Ð¾Ð¼ Ð²Ñ‹ÑÐ¾Ñ‚Ð° Ð³Ð»Ð¾Ð±Ð°Ð»ÑŒÐ½Ð¾Ð³Ð¾ Ð±Ð»Ð¾ÐºÑ‡ÐµÐ¹Ð½Ð° Ð±Ñ‹Ð»Ð° Ð²Ñ‹ÑˆÐµ Ð¸ Ñ‚ÐµÐ¼ ÑÐ°Ð¼Ð¼Ñ‹Ð¼ Ñ†ÐµÐ½ÐµÐµ.

            //TODO Ð¿Ñ€Ð¾Ð²ÐµÑ€ÐºÐ° Ñ‚ÐµÐ¿ÐµÑ€ÑŒ Ð±ÑƒÐ´ÐµÑ‚ Ð¿Ñ€Ð¾Ð¸ÑÑ…Ð¾Ð´Ð¸Ñ‚ ÑƒÐ¶Ðµ ÑÑ€Ð°Ð·Ñƒ Ð¸ Ð¿Ñ€Ð¸ ÑÐºÐ°Ñ‡Ð¸Ð²Ð°Ð½Ð¸Ð¸.
            if (Seting.IS_SECURITY == true && checking && isSmall(global, temp)) {
                System.out.println("is small host: " + s);
                String expectedJson = "is small global: " + UtilsJson.objToStringJson(global);
                String actualJson = "is small actual: " + UtilsJson.objToStringJson(temp);
                UtilsFileSaveRead.save(expectedJson + "\n " + actualJson, Seting.ERROR_FILE, true);

                UtilsAllAddresses.saveAllAddresses(s, Seting.ORIGINAL_POOL_URL_ADDRESS_BLOCKED_FILE);
                temp.setValidation(false);
                return temp;
            }

            boolean save = addBlock3(subBlocks, balances, Seting.ORIGINAL_BLOCKCHAIN_FILE, new ArrayList<>());
            if (save) {
                BasisController.setShortDataBlockchain(temp);
                BasisController.setBlockcheinSize((int) temp.getSize());
                BasisController.setBlockchainValid(temp.isValidation());

                EntityBlock tempBlock = blockService.findBySpecialIndex(BasisController.getBlockchainSize() - 1);
                BasisController.setPrevBlock(UtilsBlockToEntityBlock.entityBlockToBlock(tempBlock));

                String json = UtilsJson.objToStringJson(BasisController.getShortDataBlockchain());
                UtilsFileSaveRead.save(json, Seting.TEMPORARY_BLOCKCHAIN_FILE, false);
                return temp;
            }
        }


        System.out.println("__________________________________________________________");

        return temp;
    }


    @Transactional
    public Map<String, Account> rollBackAddBlock3(List<Block> deleteBlocks, List<Block> saveBlocks, Map<String, Account> balances, String filename) throws IOException, NoSuchAlgorithmException, InvalidKeySpecException, SignatureException, NoSuchProviderException, InvalidKeyException, CloneNotSupportedException {

        MyLogger.saveLog("start: rollBackAddBlock3: " + deleteBlocks.get(0).getIndex());
        Map<String, Laws> allLaws = new HashMap<>();
        List<LawEligibleForParliamentaryApproval> allLawsWithBalance = new ArrayList<>();

        //ÑÐ¾Ñ€Ñ‚Ð¸Ñ€ÑƒÐµÑ‚ Ð±Ð»Ð¾ÐºÐ¸ Ð¿Ð¾ Ð¸Ð½Ð´ÐµÐºÑÑƒ Ð²Ð¾Ð·Ñ€Ð°ÑÑ‚Ð°Ð½Ð¸Ñ.
        deleteBlocks = deleteBlocks.stream().sorted(Comparator.comparing(Block::getIndex)).collect(Collectors.toList());

        //ÑÐ¿Ð¸ÑÐ¾Ðº Ð±Ð»Ð¾ÐºÐ¾Ð², ÐºÐ¾Ñ‚Ð¾Ñ€Ñ‹Ðµ Ð½ÑƒÐ¶Ð½Ð¾ ÑƒÐ´Ð°Ð»Ð¸Ñ‚ÑŒ Ð¸Ð· Ñ„Ð°Ð¹Ð»Ð° Ð±Ð»Ð¾ÐºÑ‡ÐµÐ¹Ð½Ð°, Ñ‚Ð°Ðº ÐºÐ°Ðº Ð±Ð»Ð¾ÐºÑ‡ÐµÐ¹Ð½ Ñ…Ñ€Ð°Ð½Ð¸Ñ‚ÑŒÑÑ Ð² Ð´Ð²ÑƒÑ… ÑÐºÐ·ÐµÐ¼Ð¿Ð»ÑÑ€Ð°Ñ…
        //ÐºÐ°Ðº Ð² Ð±Ð°Ð·Ðµ Ð´Ð°Ð½Ð½Ñ‹Ñ… h2, Ñ‚Ð°Ðº Ð¸ Ð² Ñ„Ð°Ð¹Ð»Ðµ.
        long threshold = deleteBlocks.get(0).getIndex();
        if (threshold <= 0) {

            return new HashMap<>();
        }

        //Ð¿Ð¾Ð»ÑƒÑ‡Ð°ÐµÑ‚ Ð½Ð° Ð±Ð°Ð·Ðµ Ð¸Ð½Ð´ÐµÐºÑÐ° Ð±Ð»Ð¾ÐºÐ°, Ñ ÐºÐ°ÐºÐ¾Ð³Ð¾ Ñ„Ð°Ð¹Ð»Ð°, Ð½Ð°Ñ‡Ð°Ñ‚ÑŒ ÑƒÐ´Ð°Ð»ÑÑ‚ÑŒ Ð²ÑÐµ Ð¿Ð¾ÑÐ»ÐµÐ´ÑƒÑŽÑ‰Ð¸Ðµ Ñ„Ð°Ð¹Ð»Ñ‹
        //ÐºÐ°Ð¶Ð´Ñ‹Ð¹ Ñ„Ð°Ð¹Ð» Ð¸Ð¼ÐµÐµÑ‚ Ð¸Ð½Ð´ÐµÐºÑ Ð¿Ð¾ Ð²Ð¾Ð·Ñ€Ð°ÑÑ‚Ð°Ð½Ð¸ÑŽ, Ð¸ Ñ€Ð°Ð·Ð¼ÐµÑ€ Ð½Ðµ Ð±Ð¾Ð»ÑŒÑˆÐµ 2 Ð¼ÐµÐ³Ð°Ð±Ð°Ð¹Ñ‚.
        File file = Blockchain.indexNameFileBlock((int) threshold, filename);

        if (file == null) {

            return new HashMap<>();
        }

        List<Block> tempBlock = Collections.synchronizedList(new ArrayList<>());
        try (Stream<String> lines = Files.lines(file.toPath())) {
            List<Block> finalTempBlock = tempBlock;
            lines.parallel().forEach(line -> {
                try {
                    Block block = UtilsJson.jsonToBLock(line);
                    if (block.getIndex() < threshold) {
                        finalTempBlock.add(block);
                    }
                } catch (JsonProcessingException e) {
                }
            });
        }

        try {
            //Ð¾Ñ‚ÐºÐ°Ñ‚Ñ‹Ð²Ð°ÐµÑ‚ Ð±Ð°Ð»Ð°Ð½Ñ, Ð¸ÑÑ…Ð¾Ð´Ñ Ð¸Ð· Ð±Ð»Ð¾ÐºÐ¾Ð² Ð¿Ñ€ÐµÐ´Ð½Ð°Ð·Ð½Ð°Ñ‡ÐµÐ½Ð½Ñ‹Ñ… Ð´Ð»Ñ ÑƒÐ´Ð°Ð»ÐµÐ½Ð¸Ñ.
            if (requiresV35JackpotAccounting(deleteBlocks)) {
                balances = UtilsBalance.calculateBalances(tempBlock, Seting.ADDRESS_FOUNDER, Seting.V35_RESCUE_FORK_INDEX);
            } else {
                for (int i = deleteBlocks.size() - 1; i >= 0; i--) {
                    Block block = deleteBlocks.get(i);
                    MyLogger.saveLog("----------------------------------");
                    MyLogger.saveLog("rollBackAddBlock3 index: " + block.getIndex());
                    MyLogger.saveLog("balance before: " + balances);
                    balances = rollbackCalculateBalance(balances, block);
                    MyLogger.saveLog("after before: " + balances);
                    MyLogger.saveLog("----------------------------------");
                }
            }


            //Ð·Ð°Ð¿Ð¸ÑÑ‹Ð²Ð°ÐµÑ‚ Ð½Ð¾Ð²Ñ‹Ð¹ Ð±Ð°Ð»Ð°Ð½Ñ, ÑƒÐ¶Ðµ ÑÐ´ÐµÐ»Ð°Ð²ÑˆÐ¸Ð¹ Ð¾Ñ‚ÐºÐ°Ñ‚
            blockService.saveAccountAllF(UtilsAccountToEntityAccount.accountsToEntityAccounts(balances));
            //ÑƒÐ´Ð°Ð»ÑÐµÑ‚ Ð±Ð»Ð¾ÐºÐ¸ Ð¸Ð· Ð±Ð°Ð·Ñ‹ Ð´Ð°Ð½Ð½Ñ‹Ñ… h2,
            blockService.deleteEntityBlocksAndRelatedData(threshold);

        } catch (Throwable e) {
            MyLogger.saveLog("error tournament: ", e);
            MyLogger.saveLog("error rollback4: tempBlock index 0: " + tempBlock.get(0).getIndex());
            MyLogger.saveLog("error rollback4: saveBlocks index 0: " + saveBlocks.get(0).getIndex());
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return new HashMap<>();
        }


        allLawsWithBalance = UtilsLaws.getCurrentLaws(allLaws, balances, Seting.ORIGINAL_ALL_CORPORATION_LAWS_WITH_BALANCE_FILE);
        Mining.deleteFiles(Seting.ORIGINAL_ALL_CORPORATION_LAWS_WITH_BALANCE_FILE);
        UtilsLaws.saveCurrentsLaws(allLawsWithBalance, Seting.ORIGINAL_ALL_CORPORATION_LAWS_WITH_BALANCE_FILE);

        java.sql.Timestamp actualTime = new java.sql.Timestamp(UtilsTime.getUniversalTimestamp());

        Blockchain.deleteFileBlockchain(Integer.parseInt(file.getName().replace(".txt", "")), Seting.ORIGINAL_BLOCKCHAIN_FILE);
        tempBlock = tempBlock.stream().sorted(Comparator.comparing(Block::getIndex)).collect(Collectors.toList());

        //Ñ„Ð¸ÐºÑÐ¸Ñ€ÑƒÐµÑ‚ Ð² Ñ„Ð°Ð¹Ð»Ðµ Ð±Ð»Ð¾ÐºÐ¸ ÐºÐ¾Ñ‚Ð¾Ñ€Ñ‹Ðµ Ð¾ÑÑ‚Ð°Ð»Ð¸ÑÑŒ, Ñ‚Ð¾ ÐµÑÑ‚ÑŒ Ð¾Ð½ Ð·Ð°Ð¿Ð¸ÑÑ‹Ð²Ð°ÐµÑ‚ Ð² Ñ„Ð°Ð¹Ð» Ð±ÐµÐ· Ð¿ÐµÑ€ÐµÑ€Ð°ÑÑ‡ÐµÑ‚Ð° Ð±Ð»Ð¾ÐºÐ¸ Ð² Ñ„Ð°Ð¹Ð»Ð¾Ð²ÑƒÑŽ ÑÐ¸ÑÑ‚ÐµÐ¼Ñƒ
        UtilsBlock.saveBlocks(tempBlock, filename);


        MyLogger.saveLog("rollBackAddBlock3 finish");

        //Ð²Ð¾Ð·Ð²Ñ€Ð°Ñ‰Ð°ÐµÑ‚ Ð±Ð°Ð»Ð°Ð½Ñ ÐºÐ¾Ñ‚Ð¾Ñ€Ñ‹Ð¹ Ð¾Ñ‚ÐºÐ°Ñ‚Ð¸Ð»ÑÑ.
        return balances;
    }


    /**
     * rewrites the blockchain into files and into the h2 database. From here they are called
     * * methods that calculate balance and other calculations.
     * Ð¿Ñ€Ð¾Ð¸Ð·Ð²Ð¾Ð´Ð¸Ñ‚ Ð¿ÐµÑ€ÐµÐ·Ð°Ð¿Ð¸ÑÑŒ Ð±Ð»Ð¾ÐºÑ‡ÐµÐ¹Ð½Ð° Ð² Ñ„Ð°Ð¹Ð»Ñ‹ Ð¸ Ð² Ð±Ð°Ð·Ñƒ h2. ÐžÑ‚ÑÑŽÐ´Ð° Ð²Ñ‹Ð·Ñ‹Ð²Ð°ÑŽÑ‚ÑÑ
     * Ð¼ÐµÑ‚Ð¾Ð´Ñ‹ ÐºÐ¾Ñ‚Ð¾Ñ€Ñ‹Ðµ, Ð²Ñ‹Ñ‡Ð¸ÑÐ»ÑÑŽÑ‚ Ð±Ð°Ð»Ð°Ð½Ñ Ð¸ Ð´Ñ€ÑƒÐ³Ð¸Ðµ Ð²Ñ‹Ñ‡Ð¸ÑÐ»ÐµÐ½Ð¸Ñ.
     */




    @Transactional(isolation = Isolation.SERIALIZABLE)
    public boolean addBlock3(List<Block> originalBlocks, Map<String, Account> balances, String filename, List<String> signaturesNotTakenIntoAccount) throws IOException, NoSuchAlgorithmException, InvalidKeySpecException, SignatureException, NoSuchProviderException, InvalidKeyException, CloneNotSupportedException {
        java.sql.Timestamp lastIndex = new java.sql.Timestamp(UtilsTime.getUniversalTimestamp());
        UtilsBalance.setBlockService(blockService);
        Blockchain.setBlockService(blockService);
        UtilsBlock.setBlockService(blockService);
        NodeMemoryProfiler.recordStage("addBlock3 start", NodeMemoryProfiler.metrics(
                "blockListSize", originalBlocks == null ? 0 : originalBlocks.size(),
                "balancesSize", balances == null ? 0 : balances.size(),
                "signaturesNotTakenIntoAccountSize", signaturesNotTakenIntoAccount == null ? 0 : signaturesNotTakenIntoAccount.size()
        ));
        String addBlock3TraceId = BlockGrowthDiagnostics.startAddBlock3(originalBlocks, BasisController.getBlockchainSize());
        List<EntityBlock> list = new ArrayList<>();
        List<String> signs = new ArrayList<>();
        Map<String, Laws> allLaws = new HashMap<>();
        List<LawEligibleForParliamentaryApproval> allLawsWithBalance = new ArrayList<>();

        originalBlocks = originalBlocks.stream().filter(UtilsUse.distinctByKey(Block::getIndex)).sorted(Comparator.comparing(Block::getIndex)).collect(Collectors.toList());

        Map<String, Account> tempBalances = UtilsUse.balancesClone(balances);
        long start = UtilsTime.getUniversalTimestamp();


        List<Block> acceptedBlocksForContext = new ArrayList<>();
        BlockGrowthDiagnostics.addBlock3Stage(addBlock3TraceId, "balance calculation started", Map.of(
                "blockListSize", originalBlocks == null ? 0 : originalBlocks.size(),
                "balancesSize", balances == null ? 0 : balances.size()
        ));
        for (Block block : originalBlocks) {
            System.out.println(" :BasisController: addBlock3: blockchain is being updated: index" + block.getIndex());

            EntityBlock entityBlock = UtilsBlockToEntityBlock.blockToEntityBlock(block);
            list.add(entityBlock);
            //Ð¿Ð¾ÑÑ‡Ð¸Ñ‚Ñ‹Ð²Ð°ÐµÑ‚ Ð±Ð°Ð»Ð°Ð½Ñ Ð½Ð° Ð¾ÑÐ½Ð¾Ð²Ðµ Ð±Ð»Ð¾ÐºÐ°
            balances = calculateResolvingBalance(balances, block, signs, signaturesNotTakenIntoAccount, acceptedBlocksForContext);
            acceptedBlocksForContext.add(block);
        }
        NodeMemoryProfiler.recordStage("addBlock3 after balance calculation", NodeMemoryProfiler.metrics(
                "blockListSize", originalBlocks == null ? 0 : originalBlocks.size(),
                "entityBlockListSize", list == null ? 0 : list.size(),
                "balancesSize", balances == null ? 0 : balances.size(),
                "acceptedBlocksForContextSize", acceptedBlocksForContext == null ? 0 : acceptedBlocksForContext.size()
        ));
        BlockGrowthDiagnostics.addBlock3Stage(addBlock3TraceId, "balance calculation finished", Map.of(
                "entityBlockListSize", list == null ? 0 : list.size(),
                "balancesSize", balances == null ? 0 : balances.size(),
                "acceptedBlocksForContextSize", acceptedBlocksForContext == null ? 0 : acceptedBlocksForContext.size()
        ));
        list = list.stream().sorted(Comparator.comparing(EntityBlock::getSpecialIndex)).collect(Collectors.toList());

        long finish = UtilsTime.getUniversalTimestamp();
        System.out.println("UtilsResolving: addBlock3: for: time different: " + UtilsTime.differentMillSecondTime(start, finish));
        try {


            //Ð½Ð°Ñ…Ð¾Ð´Ð¸Ñ‚ ÑÑ‡ÐµÑ‚Ð°, ÐºÐ¾Ñ‚Ð¾Ñ€Ñ‹Ðµ Ð¸Ð·Ð¼ÐµÐ½Ð¸Ð»Ð¸ÑÑŒ Ð¿Ð¾ÑÐ»Ðµ Ð¿ÐµÑ€ÐµÑ€Ð°ÑÑ‡ÐµÑ‚Ð°
            tempBalances = UtilsUse.differentAccount(tempBalances, balances);
            //Ð¸Ð· Ð±Ð°Ð·Ñ‹ Ð´Ð°Ð½Ð½Ñ‹Ñ… Ð²Ñ‹Ñ‚Ð°ÑÐºÐ¸Ð²Ð°ÐµÑ‚ ÑÑ‡ÐµÑ‚Ð° ÐºÐ¾Ñ‚Ð¾Ñ€Ñ‹Ðµ Ð¸Ð·Ð¼ÐµÐ½Ð¸Ð»Ð¸ÑÑŒ Ð²Ð¾ Ð²Ñ€ÐµÐ¼Ñ Ð¿ÐµÑ€ÐµÑ€Ð°ÑÑ‡ÐµÑ‚Ð°
            List<EntityAccount> accountList = blockService.findByAccountIn(tempBalances);
            //Ð¾Ð±ÑŠÐµÐ´Ð¸Ð½ÑÐµÑ‚ Ð´Ð²Ð° Ñ‚Ð¸Ð¿Ð°, ÑÑ‡ÐµÑ‚Ð¾Ð², ÑÑ‡ÐµÑ‚Ð° Ð¸Ð· Ð±Ð°Ð·Ñ‹ Ð´Ð°Ð½Ð½Ñ‹Ñ… Ð¸Ð·Ð¼ÐµÐ½ÑÑŽÑ‚ÑÑ Ð² ÑÐ¾Ð¾Ñ‚Ð²ÐµÑ‚ÑÑ‚Ð²Ð¸Ð¸ Ñ Ð¸Ð·Ð¼ÐµÐ½ÐµÐ½Ð¸ÑÐ¼Ð¸, Ð½Ð¾ ÑÐ¾Ñ…Ñ€Ð°Ð½ÑÐµÑ‚ÑÑ ID
            accountList = UtilsUse.mergeAccounts(tempBalances, accountList);

            start = UtilsTime.getUniversalTimestamp();
            BlockGrowthDiagnostics.addBlock3Stage(addBlock3TraceId, "database save started", Map.of(
                    "accountListSize", accountList == null ? 0 : accountList.size(),
                    "entityBlockListSize", list == null ? 0 : list.size()
            ));
            //Ð·Ð°Ð¿Ð¸ÑÑ‹Ð²Ð°ÐµÑ‚ Ð² Ð±Ð°Ð·Ñƒ Ð´Ð°Ð½Ð½Ñ‹Ñ… ÑÑ‡ÐµÑ‚Ð°
            blockService.saveAccountAllF(accountList);
            //Ð·Ð°Ð¿Ð¸ÑÑ‹Ð²Ð°ÐµÑ‚ Ð±Ð»Ð¾ÐºÐ¸ Ð² Ð±Ð°Ð·Ñƒ Ð´Ð°Ð½Ð½Ñ‹Ñ…
            blockService.saveAllBLockF(list);
            finish = UtilsTime.getUniversalTimestamp();
            NodeMemoryProfiler.recordStage("addBlock3 after database save", NodeMemoryProfiler.metrics(
                    "entityBlockListSize", list == null ? 0 : list.size(),
                    "accountListSize", accountList == null ? 0 : accountList.size(),
                    "saveDurationMs", UtilsTime.differentMillSecondTime(start, finish)
            ));
            BlockGrowthDiagnostics.addBlock3Stage(addBlock3TraceId, "database save finished", Map.of(
                    "entityBlockListSize", list == null ? 0 : list.size(),
                    "accountListSize", accountList == null ? 0 : accountList.size(),
                    "saveDurationMs", UtilsTime.differentMillSecondTime(start, finish)
            ));
        } catch (Exception e) {

            String stackerror = "";
            for (StackTraceElement stackTraceElement : e.getStackTrace()) {
                stackerror += stackTraceElement.toString() + "\n";
            }
            MyLogger.saveLog("stackerror: " + stackerror);
            BlockGrowthDiagnostics.finishAddBlock3(
                    addBlock3TraceId,
                    false,
                    BasisController.getBlockchainSize(),
                    originalBlocks == null || originalBlocks.isEmpty() ? null : originalBlocks.get(originalBlocks.size() - 1),
                    e);
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return false;

        }

        System.out.println("UtilsResolving: addBlock3: time save accounts: " + UtilsTime.differentMillSecondTime(start, finish));
        System.out.println("UtilsResolving: addBlock3: total different balance: " + tempBalances.size());
        System.out.println("UtilsResolving: addBlock3: total original balance: " + balances.size());

        //Ð·Ð°Ð¿Ð¸ÑÑ‹Ð²Ð°ÐµÑ‚ Ð±Ð»Ð¾Ðº Ð² Ð±Ð°Ð·Ñƒ Ð´Ð°Ð½Ð½Ñ‹Ñ…

        BlockGrowthDiagnostics.addBlock3Stage(addBlock3TraceId, "file save started", Map.of(
                "blockListSize", originalBlocks == null ? 0 : originalBlocks.size(),
                "filename", filename
        ));
        UtilsBlock.saveBlocks(originalBlocks, filename);
        BlockGrowthDiagnostics.addBlock3Stage(addBlock3TraceId, "file save finished", Map.of(
                "blockListSize", originalBlocks == null ? 0 : originalBlocks.size(),
                "filename", filename
        ));
        NodeMemoryProfiler.recordStage("addBlock3 after file save", NodeMemoryProfiler.metrics(
                "blockListSize", originalBlocks == null ? 0 : originalBlocks.size(),
                "filename", filename
        ));
        allLaws = UtilsLaws.getLaws(originalBlocks, Seting.ORIGINAL_ALL_CORPORATION_LAWS_FILE, allLaws);
        allLawsWithBalance = UtilsLaws.getCurrentLaws(allLaws, balances, Seting.ORIGINAL_ALL_CORPORATION_LAWS_WITH_BALANCE_FILE);

        Mining.deleteFiles(Seting.ORIGINAL_ALL_CORPORATION_LAWS_WITH_BALANCE_FILE);
        UtilsLaws.saveCurrentsLaws(allLawsWithBalance, Seting.ORIGINAL_ALL_CORPORATION_LAWS_WITH_BALANCE_FILE);

        java.sql.Timestamp actualTime = new java.sql.Timestamp(UtilsTime.getUniversalTimestamp());
        Long result = actualTime.toInstant().until(lastIndex.toInstant(), ChronoUnit.MILLIS);
        System.out.println("addBlock 3: time: result: " + result);
        System.out.println(":BasisController: addBlock3: finish: " + originalBlocks.size());
        NodeMemoryProfiler.recordStage("addBlock3 finish", NodeMemoryProfiler.metrics(
                "blockListSize", originalBlocks == null ? 0 : originalBlocks.size(),
                "balancesSize", balances == null ? 0 : balances.size(),
                "changedBalanceSize", tempBalances == null ? 0 : tempBalances.size()
        ));
        BlockGrowthDiagnostics.finishAddBlock3(
                addBlock3TraceId,
                true,
                BasisController.getBlockchainSize(),
                originalBlocks == null || originalBlocks.isEmpty() ? null : originalBlocks.get(originalBlocks.size() - 1),
                null);
        return true;
    }


    /**
     * Ð—Ð°Ð¿Ð¸ÑÑ‹Ð²Ð°ÐµÑ‚ Ð‘Ð»Ð¾ÐºÐ¸ Ð¸ Ð±Ð°Ð»Ð°Ð½Ñ Ð²Ð¾ Ð²Ñ€ÐµÐ¼ÐµÐ½Ð½Ñ‹Ð¹ Ñ„Ð°Ð¹Ð».
     */
    public List<HostEndDataShortB> sortPriorityHost(Set<String> hosts) {
        if (PeerWorkMemoryGuard.shouldSkipNonCriticalPeerWork("sortPriorityHost")) {
            return new ArrayList<>();
        }
        // Ð£Ð½Ð¸ÐºÐ°Ð»ÑŒÐ½Ñ‹Ðµ Ñ…Ð¾ÑÑ‚Ñ‹ Ñ Ð´Ð¾Ð±Ð°Ð²Ð»ÐµÐ½Ð¸ÐµÐ¼ Ð¾Ñ€Ð¸Ð³Ð¸Ð½Ð°Ð»ÑŒÐ½Ñ‹Ñ… Ð°Ð´Ñ€ÐµÑÐ¾Ð²
        Set<String> modifiedHosts = new HashSet<>(hosts);
        modifiedHosts.addAll(Seting.ORIGINAL_ADDRESSES);

        String myHost = domainConfiguration.getPubllc_domain(); // Ð˜ÑÐ¿Ð¾Ð»ÑŒÐ·ÑƒÐµÐ¼ Ð°Ð´Ñ€ÐµÑ Ð±ÐµÐ· Ð½Ð¾Ñ€Ð¼Ð°Ð»Ð¸Ð·Ð°Ñ†Ð¸Ð¸

        // Ð’Ñ‹Ð±Ð¾Ñ€ ÑÐ»ÑƒÑ‡Ð°Ð¹Ð½Ñ‹Ñ… Ñ…Ð¾ÑÑ‚Ð¾Ð², Ð¸ÑÐºÐ»ÑŽÑ‡Ð°Ñ ÑÐ¾Ð±ÑÑ‚Ð²ÐµÐ½Ð½Ñ‹Ð¹
        List<String> selectedHosts = modifiedHosts.stream()
                .filter(host -> !host.equals(myHost)) // Ð˜ÑÐºÐ»ÑŽÑ‡Ð°ÐµÐ¼ ÑÐ²Ð¾Ð¹ Ñ…Ð¾ÑÑ‚
                .collect(Collectors.collectingAndThen(
                        Collectors.toList(),
                        list -> {
                            Collections.shuffle(list);
                            return new ArrayList<>(list.stream()
                                    .filter(peer -> !PeerHealthRegistry.isSelfPeer(peer))
                                    .filter(peer -> !PeerHealthRegistry.isQuarantined(peer))
                                    .limit(Math.min(PeerNetworkPolicy.maxPeersPerReadinessCycle(), list.size()))
                                    .toList());
                        }
                ));

        if (selectedHosts.isEmpty()) {
            return new ArrayList<>();
        }

        // ÐŸÐ¾Ñ‚Ð¾ÐºÐ¾Ð±ÐµÐ·Ð¾Ð¿Ð°ÑÐ½Ñ‹Ð¹ Ð½Ð°Ð±Ð¾Ñ€ Ð´Ð»Ñ Ð¾Ñ‚ÑÐ»ÐµÐ¶Ð¸Ð²Ð°Ð½Ð¸Ñ Ð¾Ð±Ñ€Ð°Ð±Ð¾Ñ‚Ð°Ð½Ð½Ñ‹Ñ… ÑƒÐ·Ð»Ð¾Ð²
        Set<String> processedHosts = ConcurrentHashMap.newKeySet();

        // ÐŸÑƒÐ» Ð¿Ð¾Ñ‚Ð¾ÐºÐ¾Ð² Ñ Ð´Ð¸Ð½Ð°Ð¼Ð¸Ñ‡ÐµÑÐºÐ¸Ð¼ Ñ€Ð°Ð·Ð¼ÐµÑ€Ð¾Ð¼
        ExecutorService executor = Executors.newFixedThreadPool(boundedPeerPoolSize(selectedHosts.size()));

        // ÐÑÐ¸Ð½Ñ…Ñ€Ð¾Ð½Ð½Ð°Ñ Ð¾Ð±Ñ€Ð°Ð±Ð¾Ñ‚ÐºÐ° ÑƒÐ·Ð»Ð¾Ð²
        List<CompletableFuture<HostEndDataShortB>> futures = selectedHosts.stream()
                .filter(processedHosts::add) // ÐžÐ±Ñ€Ð°Ð±Ð°Ñ‚Ñ‹Ð²Ð°ÐµÐ¼ ÐºÐ°Ð¶Ð´Ñ‹Ð¹ ÑƒÐ·ÐµÐ» Ñ‚Ð¾Ð»ÑŒÐºÐ¾ Ð¾Ð´Ð¸Ð½ Ñ€Ð°Ð·
                .map(host -> CompletableFuture.supplyAsync(() -> {
                    try {
                        DataShortBlockchainInformation global = fetchDataShortBlockchainInformation(host);
                        if (global != null && global.isValidation()) {
                            return new HostEndDataShortB(host, global);
                        }
                    } catch (IOException | JSONException e) {
                        logError("Error while retrieving data for host: " + host, e);
                    }
                    return null;
                }, executor).orTimeout(15, TimeUnit.SECONDS)) // Ð¢Ð°Ð¹Ð¼Ð°ÑƒÑ‚ Ð´Ð»Ñ ÐºÐ°Ð¶Ð´Ð¾Ð¹ Ð·Ð°Ð´Ð°Ñ‡Ð¸
                .collect(Collectors.toList());

        try {
            // ÐžÐ¶Ð¸Ð´Ð°Ð½Ð¸Ðµ Ð·Ð°Ð²ÐµÑ€ÑˆÐµÐ½Ð¸Ñ Ð²ÑÐµÑ… Ð·Ð°Ð´Ð°Ñ‡
            return futures.stream()
                    .map(future -> {
                        try {
                            return future.get(); // ÐŸÐ¾Ð»ÑƒÑ‡Ð°ÐµÐ¼ Ñ€ÐµÐ·ÑƒÐ»ÑŒÑ‚Ð°Ñ‚ Ð°ÑÐ¸Ð½Ñ…Ñ€Ð¾Ð½Ð½Ð¾Ð¹ Ð·Ð°Ð´Ð°Ñ‡Ð¸
                        } catch (Exception e) {
                            logError("Error while processing future", e);
                            return null;
                        }
                    })
                    .filter(Objects::nonNull) // Ð£Ð±Ð¸Ñ€Ð°ÐµÐ¼ null-Ð·Ð½Ð°Ñ‡ÐµÐ½Ð¸Ñ
                    .sorted(new HostEndDataShortBComparator()) // Ð¡Ð¾Ñ€Ñ‚Ð¸Ñ€Ð¾Ð²ÐºÐ° Ñ€ÐµÐ·ÑƒÐ»ÑŒÑ‚Ð°Ñ‚Ð¾Ð²
                    .toList();
        } finally {
            // ÐšÐ¾Ñ€Ñ€ÐµÐºÑ‚Ð½Ð¾Ðµ Ð·Ð°Ð²ÐµÑ€ÑˆÐµÐ½Ð¸Ðµ Ð¿ÑƒÐ»Ð° Ð¿Ð¾Ñ‚Ð¾ÐºÐ¾Ð²
            futures.clear();
            processedHosts.clear();
            selectedHosts.clear();
            modifiedHosts.clear();
            executor.shutdown();
            try {
                if (!executor.awaitTermination(36, TimeUnit.SECONDS)) {
                    executor.shutdownNow();
                }
            } catch (InterruptedException e) {
                executor.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }
    }

    private DataShortBlockchainInformation fetchDataShortBlockchainInformation(String host) throws IOException, JSONException {
        String jsonGlobalData = UtilUrl.readJsonFromUrl(host + "/datashort");
        return UtilsJson.jsonToDataShortBlockchainInformation(jsonGlobalData);
    }



    //ÑÐºÐ°Ñ‡Ð¸Ð²Ð°ÐµÑ‚ Ñ…Ð¾ÑÑ‚Ñ‹ Ð¸Ð· Ð´Ñ€ÑƒÐ³Ð¸Ñ… ÑƒÐ·Ð»Ð¾Ð²
    public Set<String> newHosts(String host) throws JSONException, IOException {
        long requestStart = System.currentTimeMillis();
        NodeMemoryProfiler.recordStage("newHosts start", NodeMemoryProfiler.metrics(
                "peer", host
        ));
        if (isSelfPeer(host)) {
            BasisController.recordSelfPeerSkipped();
            MyLogger.warnLog("SELF_PEER_SKIPPED phase=newHosts peer=" + host);
            NodeMemoryProfiler.recordStage("newHosts skip", NodeMemoryProfiler.metrics(
                    "peer", host,
                    "skipReason", "self_peer"
            ));
            return Collections.emptySet();
        }
        String addresses = UtilUrl.readJsonFromUrl(
                host + "/getNodes",
                PEER_PROPAGATION_TIMEOUT_MILLIS,
                UtilUrl.MAX_GET_NODES_RESPONSE_CHARS);
        // ÐŸÑ€ÐµÐ¾Ð±Ñ€Ð°Ð·Ð¾Ð²Ð°Ð½Ð¸Ðµ JSON Ð´Ð°Ð½Ð½Ñ‹Ñ… Ð² Ð¾Ð±ÑŠÐµÐºÑ‚
        Set<String> parsedAddresses = UtilsJson.jsonToSetAddresses(addresses);
        MyLogger.saveLog("newHosts host=" + host + " bytes=" + addresses.length() + " parsedCount=" + parsedAddresses.size() + " durationMs=" + (System.currentTimeMillis() - requestStart));
        NodeMemoryProfiler.recordStage("newHosts finish", NodeMemoryProfiler.metrics(
                "peer", host,
                "responseBytes", addresses == null ? 0 : addresses.length(),
                "parsedAddressCount", parsedAddresses == null ? 0 : parsedAddresses.size(),
                "durationMs", System.currentTimeMillis() - requestStart
        ));
        return parsedAddresses.stream()
                .filter(candidate -> !isSelfPeer(candidate))
                .limit(MAX_PROPAGATION_HOSTS)
                .collect(Collectors.toSet());
    }

    public Set<String> newHostsLoop(Set<String> hosts) throws JSONException, IOException {
        Set<String> addresses = new HashSet<>();
        for (String s : hosts) {
            if (isSelfPeer(s)) {
                BasisController.recordSelfPeerSkipped();
                MyLogger.warnLog("SELF_PEER_SKIPPED phase=newHostsLoop peer=" + s);
                continue;
            }
            addresses.addAll(newHosts(s));
            if (addresses.size() >= MAX_PROPAGATION_HOSTS) {
                break;
            }
        }
        return addresses.stream().limit(MAX_PROPAGATION_HOSTS).collect(Collectors.toSet());
    }

    // ÐœÐµÑ‚Ð¾Ð´ Ð´Ð»Ñ Ð»Ð¾Ð³Ð¸Ñ€Ð¾Ð²Ð°Ð½Ð¸Ñ Ð¾ÑˆÐ¸Ð±ÐºÐ¸
    private void logError(String message, Exception e) {
        PEER_ERROR_RATE_LIMITER.logPeerError(message, e);
        if (System.currentTimeMillis() >= 0) {
            return;
        }
        // Ð’Ñ‹Ð²Ð¾Ð´ Ð¾ÑˆÐ¸Ð±ÐºÐ¸ Ð¸ ÑÐ¾Ð¾Ð±Ñ‰ÐµÐ½Ð¸Ñ
        System.out.println("-----------------------------------");
        System.out.println("ÐžÑˆÐ¸Ð±ÐºÐ°: " + message);
        // Ð’Ñ‹Ð²Ð¾Ð´ ÑÑ‚ÐµÐºÐ° Ð²Ñ‹Ð·Ð¾Ð²Ð¾Ð² Ð¸ÑÐºÐ»ÑŽÑ‡ÐµÐ½Ð¸Ñ
        if (e != null) {
            e.printStackTrace();
        }
        // Ð—Ð°Ð²ÐµÑ€ÑˆÐµÐ½Ð¸Ðµ Ð»Ð¾Ð³Ð¸Ñ€Ð¾Ð²Ð°Ð½Ð¸Ñ
        System.out.println("-----------------------------------");
    }

    public int sendAllBlocksToStorage(List<Block> blocks) throws IOException, NoSuchAlgorithmException, InvalidKeySpecException, SignatureException, NoSuchProviderException, InvalidKeyException {
        System.out.println(new Date() + ":BasisController: sendAllBlocksToStorage: start: ");
        NodeMemoryProfiler.recordStage("sendAllBlocksToStorage start", NodeMemoryProfiler.metrics(
                "blockListSize", blocks == null ? 0 : blocks.size(),
                "tournamentHostsSize", Tournament.hostsG == null ? 0 : Tournament.hostsG.size()
        ));
        if (blocks == null || blocks.isEmpty()) {
            PeerPropagationMonitor.recordSkipped("empty_block_payload", 0, 0);
            NodeMemoryProfiler.recordStage("sendAllBlocksToStorage skip", NodeMemoryProfiler.metrics(
                    "skipReason", "empty_block_payload"
            ));
            return BasisController.getBlockchainSize() > 0 ? 1 : 0;
        }

        if (PeerWorkMemoryGuard.shouldSkipNonCriticalPeerWork("sendAllBlocksToStorage") || shouldSkipPeerPropagationForCurrentMemory()) {
            PeerPropagationMonitor.recordSkipped("low_memory", 0, 0);
            MyLogger.warnLog("PEER_PROPAGATION_SKIPPED_LOW_MEMORY " + memoryGuardStatusForLog());
            NodeMemoryProfiler.recordStage("sendAllBlocksToStorage skip", NodeMemoryProfiler.metrics(
                    "skipReason", "low_memory",
                    "memoryGuardStatus", memoryGuardStatusForLog()
            ));
            return BasisController.getBlockchainSize() > 0 ? 1 : 0;
        }

        int blocksCurrentSize = (int) blocks.get(blocks.size() - 1).getIndex() + 1;
        System.out.println(":BasisController: start sendAllBlocksToStorage: hash: " + blocks.get(0).getHashBlock());

        // ÐŸÐ¾Ð»ÑƒÑ‡Ð°ÐµÐ¼ ÑÐ¿Ð¸ÑÐ¾Ðº ÑƒÐ·Ð»Ð¾Ð² Ð¸ ÑÐ¾Ñ€Ñ‚Ð¸Ñ€ÑƒÐµÐ¼ Ð¸Ñ…

        List<HostEndDataShortB> sortPriorityHost = Tournament.hostsG == null
                ? Collections.emptyList()
                : Tournament.hostsG;

        // Ð£Ð±Ð¸Ñ€Ð°ÐµÐ¼ Ð¸ÑÐºÐ»ÑŽÑ‡ÐµÐ½Ð½Ñ‹Ðµ Ð°Ð´Ñ€ÐµÑÐ° Ð¸ ÑƒÐ´Ð°Ð»ÑÐµÐ¼ Ð´ÑƒÐ±Ð»Ð¸
        Set<String> processedAddresses = new HashSet<>();
        List<HostEndDataShortB> uniqueHosts = sortPriorityHost.stream()
                .filter(Objects::nonNull)
                .filter(host -> host.getHost() != null && !host.getHost().isBlank())
                .filter(host -> !BasisController.getExcludedAddresses().contains(host.getHost()))
                .filter(host -> processedAddresses.add(host.getHost()))
                .filter(host -> !PeerHealthRegistry.isQuarantined(host.getHost()))
                .filter(host -> !isSelfPeer(host.getHost()))
                .collect(Collectors.toList());
        uniqueHosts = PeerHealthRegistry.selectHosts(
                uniqueHosts,
                PeerNetworkPolicy.maxPeersPerPropagationCycle(),
                "propagation",
                blocksCurrentSize);
        int selfSkipped = (int) processedAddresses.stream().filter(UtilsResolving::isSelfPeer).count();

        if (uniqueHosts.isEmpty()) {
            PeerPropagationMonitor.recordSkipped("no_remote_peers", 0, selfSkipped);
            NodeMemoryProfiler.recordStage("sendAllBlocksToStorage skip", NodeMemoryProfiler.metrics(
                    "skipReason", "no_remote_peers",
                    "selfPeersSkippedCount", selfSkipped
            ));
            return BasisController.getBlockchainSize() > 0 ? 1 : 0;
        }

        PeerPropagationMonitor.recordStarted(uniqueHosts.size(), selfSkipped);
        NodeMemoryProfiler.recordStage("sendAllBlocksToStorage after peer filtering", NodeMemoryProfiler.metrics(
                "uniqueHostsSize", uniqueHosts == null ? 0 : uniqueHosts.size(),
                "selfPeersSkippedCount", selfSkipped,
                "blockListSize", blocks == null ? 0 : blocks.size()
        ));
        ExecutorService executor = Executors.newFixedThreadPool(boundedPeerPoolSize(uniqueHosts.size()));
        List<CompletableFuture<Void>> futures = uniqueHosts.stream()
                .map(host -> processNodeAsync(host, blocks, blocksCurrentSize, executor))
                .collect(Collectors.toList());

        // Ð”Ð¾Ð¶Ð¸Ð´Ð°ÐµÐ¼ÑÑ Ð·Ð°Ð²ÐµÑ€ÑˆÐµÐ½Ð¸Ñ Ð²ÑÐµÑ… Ð¾Ð¿ÐµÑ€Ð°Ñ†Ð¸Ð¹
        CompletableFuture<Void> allFutures = CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));
        try {
            allFutures.orTimeout(PeerNetworkPolicy.maxRemoteCallDurationMs(), TimeUnit.MILLISECONDS).join();
        } catch (CompletionException e) {
            PEER_ERROR_RATE_LIMITER.logPeerError("peer_propagation_batch", "/nodes/resolve_from_to_block", e);
        } finally {
            executor.shutdownNow();
        }
        long failures = futures.stream().filter(CompletableFuture::isCompletedExceptionally).count();
        PeerPropagationMonitor.recordFinished(uniqueHosts.size(), uniqueHosts.size() - (int) failures, (int) failures, selfSkipped);
        NodeMemoryProfiler.recordStage("sendAllBlocksToStorage finish", NodeMemoryProfiler.metrics(
                "uniqueHostsSize", uniqueHosts == null ? 0 : uniqueHosts.size(),
                "futuresCreatedCount", futures == null ? 0 : futures.size(),
                "futuresFailedCount", failures,
                "selfPeersSkippedCount", selfSkipped
        ));
        futures.clear();
        uniqueHosts.clear();
        System.out.println(":BasisController: finish: sendAllBlocksToStorage: hash: " + blocks.get(0).getHashBlock());
        // Ð›Ð¾Ð³Ð¸ÐºÐ° ÑÑ€Ð°Ð²Ð½ÐµÐ½Ð¸Ñ Ñ€Ð°Ð·Ð¼ÐµÑ€Ð¾Ð² Ð±Ð»Ð¾ÐºÑ‡ÐµÐ¹Ð½Ð°
        int bigsize = 0; // Ð’Ð¾Ð·Ð¼Ð¾Ð¶Ð½Ð¾, Ð²Ð°Ð¼ Ð½ÑƒÐ¶Ð½Ð¾ Ð¸Ð·Ð¼ÐµÐ½Ð¸Ñ‚ÑŒ ÑÐ¿Ð¾ÑÐ¾Ð± Ð¾Ð¿Ñ€ÐµÐ´ÐµÐ»ÐµÐ½Ð¸Ñ Ð·Ð½Ð°Ñ‡ÐµÐ½Ð¸Ñ bigsize
        if (BasisController.getBlockchainSize() > bigsize) {
            return 1;
        } else if (BasisController.getBlockchainSize() < bigsize) {
            return -1;
        } else if (BasisController.getBlockchainSize() == bigsize) {
            return 0;
        } else {
            return -4;
        }
    }

    private CompletableFuture<Void> processNodeAsync(HostEndDataShortB host, List<Block> blocks, int blocksCurrentSize, ExecutorService executor) {
        return CompletableFuture.runAsync(() -> {
            String address = host.getHost();
            try {
                if (isSelfPeer(address)) {
                    PeerHealthRegistry.recordSelfSkipped(address, "propagation");
                    MyLogger.warnLog("PEER_PROPAGATION_SKIPPED_SELF peer=" + address);
                    return;
                }

                String sizeStr = UtilUrl.readJsonFromUrl(address + "/size", PEER_PROPAGATION_TIMEOUT_MILLIS);
                Integer size = Integer.valueOf(sizeStr);
                if (blocksCurrentSize - size >= PeerNetworkPolicy.staleBehindBlocks()) {
                    PeerHealthRegistry.markStale(address, blocksCurrentSize, size, "propagation");
                    return;
                }
                PeerHealthRegistry.recordSuccess(address, "/size", 0, blocksCurrentSize, size);

                List<Block> fromToTempBlock = minimalPropagationPayload(blocks);
                SendBlocksEndInfo infoBlocks = new SendBlocksEndInfo(Seting.VERSION, fromToTempBlock);
                String jsonFromTo = UtilsJson.objToStringJson(infoBlocks);

                String urlFrom = address + "/nodes/resolve_from_to_block";
                int response = UtilUrl.sendPost(jsonFromTo, urlFrom, PEER_PROPAGATION_TIMEOUT_MILLIS);
                System.out.println(":BasisController: send: block: hash: " + blocks.get(0).getHashBlock() + " host: " + host.getHost());
                System.out.println(":response: " + response + " address: " + address);

            } catch (Exception e) {
                PeerHealthRegistry.recordFailure(address, "/nodes/resolve_from_to_block", e);
                PEER_ERROR_RATE_LIMITER.logPeerError(address, "/nodes/resolve_from_to_block", e);
                throw new CompletionException(e);
            }
        }, executor);
    }

    static List<Block> minimalPropagationPayload(List<Block> blocks) {
        if (blocks == null || blocks.isEmpty()) {
            return new ArrayList<>();
        }
        List<Block> payload = new ArrayList<>(1);
        payload.add(blocks.get(blocks.size() - 1));
        return payload;
    }

    static int boundedPeerPoolSize(int hostCount) {
        if (hostCount <= 0) {
            return 1;
        }
        return Math.max(1, Math.min(hostCount, Math.min(Runtime.getRuntime().availableProcessors(), MAX_PROPAGATION_HOSTS)));
    }

    public static boolean shouldSkipPeerPropagationForCurrentMemory() {
        Runtime runtime = Runtime.getRuntime();
        long freeMemory = runtime.freeMemory();
        long totalMemory = runtime.totalMemory();
        long maxMemory = runtime.maxMemory();
        boolean skip = shouldSkipPeerPropagationForMemory(freeMemory, totalMemory, maxMemory);
        PeerPropagationMonitor.recordMemoryGuard(skip ? "skip_low_memory" : "allow", freeMemory, maxMemory);
        return skip;
    }

    static boolean shouldSkipPeerPropagationForMemory(long freeMemory, long totalMemory, long maxMemory) {
        long usedMemory = Math.max(0, totalMemory - freeMemory);
        long availableToMax = maxMemory <= 0 ? freeMemory : Math.max(0, maxMemory - usedMemory);
        long threshold = Math.max(MIN_PROPAGATION_AVAILABLE_MEMORY_BYTES, maxMemory / MIN_PROPAGATION_MAX_MEMORY_DIVISOR);
        return availableToMax < threshold || freeMemory < Math.min(MIN_PROPAGATION_AVAILABLE_MEMORY_BYTES, threshold);
    }

    private static String memoryGuardStatusForLog() {
        Runtime runtime = Runtime.getRuntime();
        long freeMemory = runtime.freeMemory();
        long totalMemory = runtime.totalMemory();
        long maxMemory = runtime.maxMemory();
        long usedMemory = Math.max(0, totalMemory - freeMemory);
        long threshold = Math.max(MIN_PROPAGATION_AVAILABLE_MEMORY_BYTES, maxMemory / MIN_PROPAGATION_MAX_MEMORY_DIVISOR);
        return "freeMemory=" + freeMemory
                + ", totalMemory=" + totalMemory
                + ", maxMemory=" + maxMemory
                + ", usedMemory=" + usedMemory
                + ", threshold=" + threshold;
    }

    public static boolean isSelfPeer(String host) {
        if (host == null || host.isBlank()) {
            return false;
        }
        String normalized = host.trim().replace("\"", "").toLowerCase(Locale.ROOT);
        while (normalized.endsWith("/")) {
            normalized = normalized.substring(0, normalized.length() - 1);
        }
        return normalized.equals("http://194.87.236.238:82")
                || normalized.equals("https://194.87.236.238:82")
                || normalized.equals("194.87.236.238:82")
                || normalized.equals("http://localhost:82")
                || normalized.equals("https://localhost:82")
                || normalized.equals("localhost:82")
                || normalized.equals("http://127.0.0.1:82")
                || normalized.equals("https://127.0.0.1:82")
                || normalized.equals("127.0.0.1:82");
    }




}

