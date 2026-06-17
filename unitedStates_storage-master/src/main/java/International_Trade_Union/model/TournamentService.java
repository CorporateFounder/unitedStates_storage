package International_Trade_Union.model;

import International_Trade_Union.controllers.BasisController;
import International_Trade_Union.controllers.NodeController;

import International_Trade_Union.entity.DtoTransaction.DtoTransaction;
import International_Trade_Union.entity.blockchain.Blockchain;
import International_Trade_Union.entity.blockchain.DataShortBlockchainInformation;
import International_Trade_Union.entity.blockchain.block.Block;
import International_Trade_Union.entity.entities.EntityBlock;
import International_Trade_Union.entity.services.BlockService;
import International_Trade_Union.entity.services.PoolBlockService;
import International_Trade_Union.entity.services.RewardDistributionService;
import International_Trade_Union.logger.MyLogger;
import International_Trade_Union.protocol.v35.ChainTournamentScorer;
import International_Trade_Union.protocol.v35.V35WorkCalculator;
import International_Trade_Union.setings.Seting;
import International_Trade_Union.utils.*;
import org.json.JSONException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.transaction.Transactional;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.util.*;
import java.util.concurrent.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static International_Trade_Union.controllers.BasisController.getNodes;
import static International_Trade_Union.utils.UtilsUse.bigRandomWinner;

@Component
@Scope("singleton")
public class TournamentService {
    static final int MAX_TOURNAMENT_HOSTS = PeerNetworkPolicy.maxPeersPerTournamentCycle();
    static final int TOURNAMENT_HTTP_TIMEOUT_MILLIS = PeerNetworkPolicy.readTimeoutMs();
    static final int MAX_PEER_WINNER_BLOCKS = 16;
    private static final int MAX_TOURNAMENT_THREADS = 8;
    private static final PeerErrorRateLimiter PEER_ERROR_RATE_LIMITER = new PeerErrorRateLimiter();
    private static volatile long lastTournamentStartMillis = -1;
    private static volatile long lastTournamentEndMillis = -1;
    private static volatile long lastTournamentDurationMillis = -1;
    private static volatile String lastOomRiskWarning = "none";

    @PostConstruct
    public void init() {
        Blockchain.setBlockService(blockService);
        UtilsBalance.setBlockService(blockService);
        UtilsBlock.setBlockService(blockService);
    }

    @Autowired
    NodeChecker nodeChecker;
    @Autowired
    UtilsTransactions utilsTransactions;

    @Autowired
    UtilsResolving utilsResolving;

    @Autowired
    BlockService blockService;

    @Autowired
    DomainConfiguration domainConfiguration;

    @Autowired
    PoolBlockService poolBlockService;

    @Autowired
    RewardDistributionService rewardDistributionService;

    private List<Block> winnerDiff = new ArrayList<>();
    private List<Block> winnerCountTransaction = new ArrayList<>();
    private List<Block> winnerStaking = new ArrayList<>();
    private List<Block> winner = new ArrayList<>();

    static List<HostEndDataShortB> boundedHostsForTournament(List<HostEndDataShortB> hosts) {
        if (PeerWorkMemoryGuard.shouldSkipNonCriticalPeerWork("tournament")) {
            return new ArrayList<>();
        }
        if (hosts == null || hosts.isEmpty()) {
            return new ArrayList<>();
        }
        return PeerHealthRegistry.selectHosts(hosts.stream()
                .filter(Objects::nonNull)
                .filter(host -> host.getHost() != null && !host.getHost().isBlank())
                .collect(Collectors.toList()),
                PeerNetworkPolicy.maxPeersPerTournamentCycle(),
                "tournament",
                -1);
    }

    static String memoryStatusForLog() {
        Runtime runtime = Runtime.getRuntime();
        return "freeMemory=" + runtime.freeMemory()
                + ", totalMemory=" + runtime.totalMemory()
                + ", maxMemory=" + runtime.maxMemory();
    }

    public static long getLastTournamentStartMillis() {
        return lastTournamentStartMillis;
    }

    public static long getLastTournamentEndMillis() {
        return lastTournamentEndMillis;
    }

    public static long getLastTournamentDurationMillis() {
        return lastTournamentDurationMillis;
    }

    public static String getLastOomRiskWarning() {
        return lastOomRiskWarning;
    }

    public static int getTrackedPeerErrorCount() {
        return PEER_ERROR_RATE_LIMITER.trackedErrorCount();
    }

    static void resetDiagnosticsForTests() {
        lastTournamentStartMillis = -1;
        lastTournamentEndMillis = -1;
        lastTournamentDurationMillis = -1;
        lastOomRiskWarning = "none";
        PEER_ERROR_RATE_LIMITER.resetForTests();
    }

    private static ExecutorService newTournamentExecutor(int taskCount) {
        int threads = Math.max(1, Math.min(Math.min(taskCount, MAX_TOURNAMENT_THREADS), Runtime.getRuntime().availableProcessors()));
        return Executors.newFixedThreadPool(threads);
    }

    private static void logPeerError(String peer, Exception error) {
        PEER_ERROR_RATE_LIMITER.logPeerError(peer, error);
        lastOomRiskWarning = PEER_ERROR_RATE_LIMITER.lastWarning();
    }

    public List<Block> getWinnerDiff() {
        return winnerDiff;
    }

    public void setWinnerDiff(List<Block> winnerDiff) {
        this.winnerDiff = winnerDiff;
    }

    public static List<Block> sortWinner(Map<String, Account> finalBalances, List<Block> list) {
        if (shouldUseV35CandidatePriority(list, Seting.V35_RESCUE_FORK_INDEX)) {
            return sortWinnerV35(finalBalances, list);
        }
        //TODO start test ---------------------------------------------------------
        // Получение big random значения для блока

        Function<Block, Integer> bigRandomValue = block -> safeBigRandomWinner(block, finalBalances);

// Создание компаратора с учетом big random, hashComplexity, staking и transactionCount
        Comparator<Block> blockComparator = Comparator
                .comparing(bigRandomValue, Comparator.reverseOrder()) // Сначала по bigRandom
                .thenComparing(Block::getHashCompexity, Comparator.reverseOrder()) // Затем по hashComplexity
                .thenComparing(block -> Optional.ofNullable(finalBalances.get(block.getMinerAddress()))
                        .map(Account::getDigitalStakingBalance)
                        .orElse(BigDecimal.ZERO), Comparator.reverseOrder()) // Затем по staking
                .thenComparing(block -> block.getDtoTransactions().size(), Comparator.reverseOrder()); // И наконец, по количеству транзакций

// Применение компаратора для сортировки списка
        List<Block> sortedList = list.stream()
                .sorted(blockComparator)
                .collect(Collectors.toList());

        return sortedList;
        //TODO finish test ---------------------------------------------------------
    }

    static List<Block> sortWinnerForForkIndex(Map<String, Account> finalBalances, List<Block> list, long forkIndex) {
        if (shouldUseV35CandidatePriority(list, forkIndex)) {
            return sortWinnerV35(finalBalances, list);
        }
        return sortWinner(finalBalances, list);
    }

    static boolean shouldUseV35CandidatePriority(List<Block> list, long forkIndex) {
        if (forkIndex == Long.MAX_VALUE || list == null || list.isEmpty()) {
            return false;
        }
        for (Block block : list) {
            if (!hasValidV35CandidateMetadata(block, forkIndex)) {
                return false;
            }
        }
        return true;
    }

    private static boolean hasValidV35CandidateMetadata(Block block, long forkIndex) {
        return block != null
                && block.getIndex() >= forkIndex
                && candidateWork(block).signum() > 0
                && hashNumber(block).signum() >= 0;
    }

    private static List<Block> sortWinnerV35(Map<String, Account> finalBalances, List<Block> list) {
        return list.stream()
                .sorted((left, right) -> -compareV35Candidate(finalBalances, left, right))
                .collect(Collectors.toList());
    }

    static int compareV35Candidate(Map<String, Account> finalBalances, Block left, Block right) {
        int workCompare = candidateWork(left).compareTo(candidateWork(right));
        if (workCompare != 0) {
            return workCompare;
        }

        int hashCompare = hashNumber(right).compareTo(hashNumber(left));
        if (hashCompare != 0) {
            return hashCompare;
        }

        int tieBreakerCompare = Double.compare(candidateTieBreaker(finalBalances, left), candidateTieBreaker(finalBalances, right));
        if (tieBreakerCompare != 0) {
            return tieBreakerCompare;
        }

        return compareHashText(left == null ? null : left.getHashBlock(), right == null ? null : right.getHashBlock());
    }

    private static BigInteger candidateWork(Block block) {
        if (block == null) {
            return BigInteger.ZERO;
        }
        return V35WorkCalculator.workFromCompactTarget(block.getHashCompexity());
    }

    private static BigInteger hashNumber(Block block) {
        if (block == null || block.getHashBlock() == null || block.getHashBlock().trim().isEmpty()) {
            return BigInteger.valueOf(-1);
        }
        try {
            return new BigInteger(block.getHashBlock(), 16);
        } catch (NumberFormatException e) {
            return BigInteger.valueOf(-1);
        }
    }

    private static double candidateTieBreaker(Map<String, Account> finalBalances, Block block) {
        if (block == null) {
            return 0.0;
        }
        Account account = finalBalances == null ? null : finalBalances.get(block.getMinerAddress());
        double stake = normalized(account == null ? BigDecimal.ZERO : account.getDigitalStakingBalance());
        double transactions = normalized(block.getDtoTransactions() == null ? 0L : block.getDtoTransactions().size());
        return ChainTournamentScorer.capTieBreakerScore((stake + transactions) / 2.0);
    }

    private static int safeBigRandomWinner(Block block, Map<String, Account> finalBalances) {
        if (block == null) {
            return 0;
        }
        try {
            Account account = finalBalances == null ? null : finalBalances.get(block.getMinerAddress());
            return bigRandomWinner(block, account);
        } catch (RuntimeException e) {
            return 0;
        }
    }

    private static double normalized(BigDecimal value) {
        if (value == null || value.compareTo(BigDecimal.ZERO) <= 0) {
            return 0.0;
        }
        double number = value.doubleValue();
        if (Double.isNaN(number) || Double.isInfinite(number)) {
            return 1.0;
        }
        return number / (number + 1.0);
    }

    private static double normalized(long value) {
        if (value <= 0) {
            return 0.0;
        }
        return (double) value / ((double) value + 1.0);
    }

    private static int compareHashText(String leftHash, String rightHash) {
        boolean leftEmpty = leftHash == null || leftHash.isEmpty();
        boolean rightEmpty = rightHash == null || rightHash.isEmpty();
        if (leftEmpty && rightEmpty) {
            return 0;
        }
        if (leftEmpty) {
            return -1;
        }
        if (rightEmpty) {
            return 1;
        }
        return rightHash.compareTo(leftHash);
    }


    public List<LiteVersionWiner> blockToLiteVersion(List<Block> list, Map<String, Account> balances) {
        List<LiteVersionWiner> list1 = new ArrayList<>();
        for (Block block : list) {
            Account account = balances.get(block.getMinerAddress());
            if (account == null)
                account = new Account(block.getMinerAddress(), BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO);
            LiteVersionWiner liteVersionWiner = new LiteVersionWiner(
                    block.getIndex(),
                    block.getMinerAddress(),
                    block.getHashBlock(),
                    block.getDtoTransactions().size(),
                    account.getDigitalStakingBalance().doubleValue(),
                    bigRandomWinner(block, account),
                    block.getHashCompexity()
            );
            liteVersionWiner.setPreviousHash(block.getPreviousHash());
            liteVersionWiner.setTimestamp(block.getTimestamp() == null ? null : block.getTimestamp().toString());
            list1.add(liteVersionWiner);
        }
        return list1;
    }

    public void getCheckSyncTime(List<HostEndDataShortB> hosts) {
        if (PeerWorkMemoryGuard.shouldSkipNonCriticalPeerWork("getCheckSyncTime")) {
            return;
        }
        List<HostEndDataShortB> sortPriorityHost = null;


        try {

            sortPriorityHost = hosts;
        } catch (Exception e) {
            MyLogger.saveLog("getCheckSyncTime: ", e);
            return;
        }

        sortPriorityHost = boundedHostsForTournament(sortPriorityHost);
        ExecutorService executor = newTournamentExecutor(sortPriorityHost.size());
        List<CompletableFuture<Void>> futures = sortPriorityHost.stream().map(hostEndDataShortB -> CompletableFuture.runAsync(() -> {
            String s = hostEndDataShortB.getHost();
            try {
                if (BasisController.getExcludedAddresses().contains(s)) {
                    System.out.println(":its your address or excluded address: " + s);
                    return;
                }

                //TODO здесь должен он получить время и сравнить его с pool.ntp.org
                //TODO так как каждый сервер ntp настраивает на уровне виндовса
                //TODO как мы можем убедиться здесь чтобы этот сервер настроил свой ntp
                //TODO или нет, чтобы если он не настроил, у него не брать блоки
                //TODO если нет, то он должен так заблокировать
                // UtilsAllAddresses.saveAllAddresses(hostEndDataShortB.getHost(), Seting.ORIGINAL_POOL_URL_ADDRESS_BLOCKED_FILE);
                String timestr = UtilUrl.readJsonFromUrl(s + "/timentp");
                if (timestr == null || timestr.isEmpty() || timestr.isBlank()) {
                    return;
                }
                long localTime = UtilsTime.getUniversalTimestamp();
                long serverTime = (long) UtilsJson.jsonToObject(timestr, Long.class);
                if (!UtilsTime.isTimeSynchronized(localTime, serverTime)) {
                    MyLogger.saveLog("pool.ntp.org different time in server");
                    UtilsAllAddresses.saveAllAddresses(hostEndDataShortB.getHost(), Seting.ORIGINAL_POOL_URL_ADDRESS_BLOCKED_FILE);
                }


            } catch (IOException | JSONException e) {
                logPeerError(s, e);
            } catch (Exception e) {
                logPeerError(s, e);
            }
        }, executor)).collect(Collectors.toList());
        PeerConnectionDiagnostics.beginFutures("tournament_getCheckSyncTime", futures.size());

        CompletableFuture<Void> allOf = CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));
        try {
            allOf.get(PeerNetworkPolicy.tournamentPeerBudgetMs(), TimeUnit.MILLISECONDS);
        } catch (InterruptedException | ExecutionException e) {
            MyLogger.saveLog("getCheckSyncTime: ", e);
        } catch (TimeoutException e) {
            PeerHealthRegistry.recordPeerSync("TOURNAMENT_PEER_BUDGET_SKIPPED", PeerNetworkPolicy.tournamentPeerBudgetMs());
            PEER_ERROR_RATE_LIMITER.logPeerError("getCheckSyncTime_batch", "/timentp", e);
        } finally {
            PeerConnectionDiagnostics.endFutures("tournament_getCheckSyncTime", futures.size());
            futures.clear();
            sortPriorityHost.clear();
            executor.shutdownNow();
        }


    }

    public void getAllWinner(List<HostEndDataShortB> hostEndDataShortBS) {
        if (PeerWorkMemoryGuard.shouldSkipNonCriticalPeerWork("getAllWinner")) {
            return;
        }
        hostEndDataShortBS = boundedHostsForTournament(hostEndDataShortBS);
        NodeMemoryProfiler.recordStage("getAllWinner / winnerList remote fetch start", NodeMemoryProfiler.metrics(
                "hostsListSize", hostEndDataShortBS == null ? 0 : hostEndDataShortBS.size(),
                "winnerListSize", BasisController.getWinnerList() == null ? 0 : BasisController.getWinnerList().size()
        ));
        ExecutorService executor = newTournamentExecutor(hostEndDataShortBS.size());
        // Временное потокобезопасное множество для хранения уникальных блоков
        Set<Block> tempWinnerSet = ConcurrentHashMap.newKeySet();

        // Создаём список задач
        List<CompletableFuture<Void>> futures = hostEndDataShortBS.stream()
                .map(hostEndDataShortB -> CompletableFuture.runAsync(() -> {
                    String s = hostEndDataShortB.getHost();
                    try {
                        if (UtilsResolving.isSelfPeer(s)) {
                            BasisController.recordSelfPeerSkipped();
                            MyLogger.warnLog("SELF_PEER_SKIPPED phase=getAllWinner peer=" + s);
                            return;
                        }
                        if (BasisController.getExcludedAddresses().contains(s)) {
                            MyLogger.saveLog(":its your address or excluded address: " + s);
                            return;
                        }

                        String winnerListJson = UtilUrl.readJsonFromUrl(
                                s + "/winnerList",
                                TOURNAMENT_HTTP_TIMEOUT_MILLIS,
                                UtilUrl.MAX_WINNER_LIST_RESPONSE_CHARS);
                        String prevBlockJson = UtilUrl.readJsonFromUrl(s + "/prevBlock", TOURNAMENT_HTTP_TIMEOUT_MILLIS);
                        NodeMemoryProfiler.recordStage("getAllWinner / winnerList remote fetch finish", NodeMemoryProfiler.metrics(
                                "peer", s,
                                "winnerListJsonLength", winnerListJson == null ? 0 : winnerListJson.length(),
                                "prevBlockJsonLength", prevBlockJson == null ? 0 : prevBlockJson.length()
                        ));

                        if (winnerListJson.isEmpty() || prevBlockJson.isEmpty()) {
                            return;
                        }

                        List<Block> blocks = UtilsJson.jsonToObject(winnerListJson);
                        NodeMemoryProfiler.recordStage("getAllWinner after winnerList parse", NodeMemoryProfiler.metrics(
                                "peer", s,
                                "parsedWinnerListSize", blocks == null ? 0 : blocks.size(),
                                "jsonStringLength", winnerListJson.length()
                        ));
                        if (blocks.size() > MAX_PEER_WINNER_BLOCKS) {
                            lastOomRiskWarning = "peer=" + s + " winnerList too large: " + blocks.size();
                            blocks = blocks.stream().limit(MAX_PEER_WINNER_BLOCKS).collect(Collectors.toList());
                        }
                        Block prevBlock = UtilsJson.jsonToBLock(prevBlockJson);

                        if (BasisController.getBlockchainSize() == prevBlock.getIndex()) {
                            blocks.add(prevBlock);
                        }

                        for (Block block : blocks) {
                            MyLogger.saveLog("Processing block with index: " + block.getIndex());
                            validateAndAddBlock(block, tempWinnerSet, s);
                        }
                    } catch (Exception e) {
                        logPeerError(s, e);
                    }
                }, executor))
                .collect(Collectors.toList());
        PeerConnectionDiagnostics.beginFutures("tournament_getAllWinner", futures.size());

        // Ограничиваем общее время выполнения всех задач
        CompletableFuture<Void> allOf = CompletableFuture
                .allOf(futures.toArray(new CompletableFuture[0]))
                .orTimeout(36, TimeUnit.SECONDS); // Ограничение времени в 32 секунды

        try {
            allOf.join(); // Ждём завершения всех задач
        } catch (Exception e) {
            MyLogger.saveLog("Unexpected error in getAllWinner: " + e.getMessage());
        } finally {
            PeerConnectionDiagnostics.endFutures("tournament_getAllWinner", futures.size());
            futures.clear();
            hostEndDataShortBS.clear();
            executor.shutdownNow();
        }

        // Перед выходом обновляем winnerList атомарно

        for (Block block : tempWinnerSet) {
            if (BasisController.getWinnerList().size() >= BasisController.MAX_TEMPORARY_SUBMITTED_BLOCKS) {
                MyLogger.warnLog("CANDIDATE_LIMIT_REACHED collection=winnerList originalCount="
                        + (BasisController.getWinnerList().size() + tempWinnerSet.size())
                        + " retained=" + BasisController.MAX_TEMPORARY_SUBMITTED_BLOCKS);
                break;
            }
            if (!BasisController.getWinnerList().contains(block)) {
                BasisController.getWinnerList().add(block);
            }
        }
        NodeMemoryProfiler.recordStage("getAllWinner finish", NodeMemoryProfiler.metrics(
                "tempWinnerSetSize", tempWinnerSet.size(),
                "winnerListSize", BasisController.getWinnerList() == null ? 0 : BasisController.getWinnerList().size(),
                "futuresCreatedCount", futures.size()
        ));
        tempWinnerSet.clear();

    }

    private List<Block> v35CanonicalContextBefore(Block prevBlock) throws IOException {
        if (Seting.V35_RESCUE_FORK_INDEX == Long.MAX_VALUE
                || prevBlock == null
                || prevBlock.getIndex() < Seting.V35_RESCUE_FORK_INDEX) {
            return Collections.emptyList();
        }
        return UtilsBlockToEntityBlock.entityBlocksToBlocks(
                blockService.findBySpecialIndexBetween(0, prevBlock.getIndex()));
    }

    private void validateAndAddBlock(Block block, Set<Block> tempWinnerSet, String host) {
        try {
            List<String> sign = new ArrayList<>();
            List<Block> tempBlock = new ArrayList<>();
            tempBlock.add(block);

            Map<String, Account> tempBalances = UtilsAccountToEntityAccount.entityAccountsToMapAccounts(
                    UtilsUse.accounts(tempBlock, blockService));

            DataShortBlockchainInformation temp = Blockchain.shortCheckWithCanonicalContext(
                    BasisController.prevBlock(), tempBlock, BasisController.getShortDataBlockchain(),
                    new ArrayList<>(), tempBalances, sign, UtilsUse.balancesClone(tempBalances), new ArrayList<>(),
                    v35CanonicalContextBefore(BasisController.prevBlock()), Seting.V35_RESCUE_FORK_INDEX);

            if (temp.isValidation()) {
                MyLogger.saveLog("Block is valid: " + block.getIndex() + " from host: " + host);
                tempWinnerSet.add(block);
            }
        } catch (Exception e) {
            MyLogger.saveLog("Error validating block with index: " + block.getIndex() + " from host: " + host + ": " + e.getMessage());
        }
    }


    public void tournament(List<HostEndDataShortB> hostEndDataShortBS) {

        lastTournamentStartMillis = System.currentTimeMillis();
        DiagnosticLogRetention.cleanupIfDue(BasisController.getBlockchainSize());
        String tournamentTraceId = BlockGrowthDiagnostics.startTournament(
                BasisController.getBlockchainSize(),
                BasisController.getWinnerList() == null ? 0 : BasisController.getWinnerList().size());
        NodeMemoryProfiler.beginTrace("tournament start", null);
        NodeMemoryProfiler.recordStage("tournament start", NodeMemoryProfiler.metrics(
                "hostsListSize", hostEndDataShortBS == null ? 0 : hostEndDataShortBS.size(),
                "winnerListSize", BasisController.getWinnerList() == null ? 0 : BasisController.getWinnerList().size()
        ));
        long timeBefore = UtilsTime.getUniversalTimestamp();
        System.out.println("SERVER_TOURNAMENT_MEMORY_BEFORE=" + memoryStatusForLog());
        System.out.println("SERVER_TOURNAMENT_WINNERLIST_SIZE_BEFORE=" + BasisController.getWinnerList().size());
        // Сначала вызываем getAllWinner
        getAllWinner(hostEndDataShortBS);
        NodeMemoryProfiler.recordStage("tournament after winner selection", NodeMemoryProfiler.metrics(
                "winnerListSize", BasisController.getWinnerList() == null ? 0 : BasisController.getWinnerList().size()
        ));
        System.out.println("SERVER_TOURNAMENT_WINNERLIST_SIZE_AFTER_GET_ALL_WINNER=" + BasisController.getWinnerList().size());
        System.out.println("SERVER_TOURNAMENT_MEMORY_AFTER_GET_ALL_WINNER=" + memoryStatusForLog());
        long timeAfter = UtilsTime.getUniversalTimestamp();
        MyLogger.saveLog("getAllWinner: millisecond: " + (timeAfter - timeBefore) + " second: " + ((timeAfter - timeBefore) / 1000));

        // Меняем состояние на "готов"
        NodeController.setReady();

        timeBefore = UtilsTime.getUniversalTimestamp();
        // Затем вызываем initiateProcess
        nodeChecker.initiateProcess(hostEndDataShortBS);
        timeAfter = UtilsTime.getUniversalTimestamp();
        MyLogger.saveLog("initiateProcess: millisecond: " + (timeAfter - timeBefore) + " second: " + ((timeAfter - timeBefore) / 1000));

        try {
            timeBefore = UtilsTime.getUniversalTimestamp();
            List<Block> list = BasisController.getWinnerList();
            NodeMemoryProfiler.recordStage("tournament before winner selection", NodeMemoryProfiler.metrics(
                    "candidateWinnerListSize", list == null ? 0 : list.size()
            ));
            timeAfter = UtilsTime.getUniversalTimestamp();
            MyLogger.saveLog("getWinnerList: millisecond: " + (timeAfter - timeBefore) + " second: " + ((timeAfter - timeBefore) / 1000));

            list = list.stream()
                    .filter(t -> t.getIndex() == BasisController.getBlockchainSize())
                    .filter(UtilsUse.distinctByKey(Block::getHashBlock))
                    .filter(t -> UtilsUse.getDuplicateTransactions(t).size() == 0)
                    .collect(Collectors.toList());
            BlockGrowthDiagnostics.tournamentCandidates(tournamentTraceId, list == null ? 0 : list.size());


            if (list == null || list.isEmpty() || list.size() == 0) {
                BasisController.setIsSaveFile(true);
                System.out.println("-----------------------");
                System.out.println("you can safely shut down the server.: " + list.size());
                System.out.println("-----------------------");
                BlockGrowthDiagnostics.tournamentSkip(tournamentTraceId, "NO_CANDIDATE_BLOCKS");
                return;
            }

            UtilsBalance.setBlockService(blockService);
            Blockchain.setBlockService(blockService);
            UtilsBlock.setBlockService(blockService);


            System.out.println("+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++");
            System.out.println("start tournament:");
            long startTournament = UtilsTime.getUniversalTimestamp();


            System.out.println("+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++");

            if (winner == null) {
                winner = new ArrayList<>();
            } else {
                winner.clear();
            }
            if (winnerDiff == null) {
                winnerDiff = new ArrayList<>();
            } else {
                winnerDiff.clear();
            }
            if (winnerCountTransaction == null) {
                winnerCountTransaction = new ArrayList<>();
            } else {
                winnerCountTransaction.clear();
            }
            if (winnerStaking == null) {
                winnerStaking = new ArrayList<>();
            } else {
                winnerStaking.clear();
            }


            System.out.println("tournament: winner: " + winner.size());
            Map<String, Account> balances = new HashMap<>();

            balances = UtilsAccountToEntityAccount.entityAccountsToMapAccounts(UtilsUse.accounts(list, blockService));
            BasisController.setIsSaveFile(false);


            Map<String, Account> finalBalances = UtilsUse.balancesClone(balances);
            // Обеспечение наличия всех аккаунтов в finalBalances
            list.forEach(block -> finalBalances.computeIfAbsent(block.getMinerAddress(), address -> new Account(address, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO)));

            List<Block> winnerList = new ArrayList<>();

            if (list.isEmpty()) {
                return;
            }


            winnerList = sortWinner(finalBalances, list);
            BlockGrowthDiagnostics.tournamentWinnerSelected(
                    tournamentTraceId,
                    winnerList == null || winnerList.isEmpty() ? null : winnerList.get(0),
                    winnerList == null ? 0 : winnerList.size());
            NodeMemoryProfiler.recordStage("tournament after sortWinner", NodeMemoryProfiler.metrics(
                    "candidateListSize", list == null ? 0 : list.size(),
                    "sortedWinnerListSize", winnerList == null ? 0 : winnerList.size(),
                    "balancesSize", balances == null ? 0 : balances.size()
            ));


            Block prevBlock = BasisController.prevBlock();
            if (winner == null) {
                winner = new ArrayList<>();
            } else {
                winner.clear();
            }
            winner.add(winnerList.get(0));
            if (winner == null || winner.size() == 0 || winner.get(0) == null || winner.get(0).getTimestamp() == null) {
                System.out.println("--------------------------------------------");

                System.out.println("error null: winner: " + winner);
                System.out.println("--------------------------------------------");
                BlockGrowthDiagnostics.tournamentSkip(tournamentTraceId, "WINNER_NULL_OR_INCOMPLETE");
                return;
            }


            List<Block> lastDiff = new ArrayList<>();


            Map<String, Account> tempBalances = UtilsAccountToEntityAccount.entityAccountsToMapAccounts(UtilsUse.accounts(winner, blockService));
            List<String> sign = new ArrayList<>();
            //Вычисляет мета данные блокчейна, с учетом нового блока, его целостность, длину, а также другие параметры
            DataShortBlockchainInformation temp = Blockchain.shortCheckWithCanonicalContext(
                    BasisController.prevBlock(), winner, BasisController.getShortDataBlockchain(), lastDiff,
                    tempBalances, sign, UtilsUse.balancesClone(tempBalances), new ArrayList<>(),
                    v35CanonicalContextBefore(BasisController.prevBlock()), Seting.V35_RESCUE_FORK_INDEX);

            if (temp == null || !temp.isValidation()) {
                System.out.println("wrong validation short: " + temp);
                BlockGrowthDiagnostics.tournamentSkip(tournamentTraceId, "WINNER_SHORT_VALIDATION_FAILED");
                return;
            }

            System.out.println("save winner: " + winner.size() + " balances: " + balances.size());
            //TODO прекратить давать блоки через sub block, если происходит запись

//                balances = UtilsAccountToEntityAccount.entityAccountsToMapAccounts(blockService.findAllAccounts());


            boolean save = false;
            long tournamentSizeBeforeAdd = BasisController.getBlockchainSize();
            //производит запись блока в файл и в базу данных, а также подсчитывает новый баланс.
            if (winner != null && balances != null) {
                balances = UtilsAccountToEntityAccount.entityAccountsToMapAccounts(UtilsUse.accounts(winner, blockService));
                winner = winner.stream().filter(UtilsUse.distinctByKey(Block::getIndex)).collect(Collectors.toList());
                System.out.println("SERVER_TOURNAMENT_PERSIST_START");
                System.out.println("SERVER_TOURNAMENT_PERSIST_WINNER_INDEX=" + (winner.isEmpty() ? "none" : winner.get(0).getIndex()));
                System.out.println("SERVER_TOURNAMENT_MEMORY_BEFORE_ADD_BLOCK3=" + memoryStatusForLog());
                BasisController.recordLocalPersistenceStartedFromTournament(winner.isEmpty() ? null : winner.get(0));
                NodeMemoryProfiler.recordStage("tournament before addBlock3", NodeMemoryProfiler.metrics(
                        "winnerSize", winner == null ? 0 : winner.size(),
                        "balancesSize", balances == null ? 0 : balances.size(),
                        "winnerBlockIndex", winner == null || winner.isEmpty() ? null : winner.get(0).getIndex()
                ));
                save = utilsResolving.addBlock3(winner, balances, Seting.ORIGINAL_BLOCKCHAIN_FILE, new ArrayList<>());
                BlockGrowthDiagnostics.tournamentAddBlock3Result(
                        tournamentTraceId,
                        save,
                        tournamentSizeBeforeAdd,
                        BasisController.getBlockchainSize());
                NodeMemoryProfiler.recordStage("tournament after addBlock3", NodeMemoryProfiler.metrics(
                        "save", save,
                        "winnerSize", winner == null ? 0 : winner.size(),
                        "balancesSize", balances == null ? 0 : balances.size()
                ));
                System.out.println("SERVER_TOURNAMENT_PERSIST_RESULT=" + save);
                System.out.println("SERVER_TOURNAMENT_MEMORY_AFTER_ADD_BLOCK3=" + memoryStatusForLog());
            }

            //TODO отправка награды пула
//            rewardDistributionService.loadState();
            rewardDistributionService.checkPending();
            rewardDistributionService.sendReward();

            if (save) {
                //делает запись мета данных блокчейна.

                //Добавляет мета данные в статическую переменную.
                BasisController.setShortDataBlockchain(temp);
                BasisController.setBlockcheinSize((int) temp.getSize());
                BasisController.setBlockchainValid(temp.isValidation());
                String json = UtilsJson.objToStringJson(BasisController.getShortDataBlockchain());
                UtilsFileSaveRead.save(json, Seting.TEMPORARY_BLOCKCHAIN_FILE, false);


                EntityBlock entityBlock = blockService.findBySpecialIndex(temp.getSize() - 1);
                System.out.println("entityBlock: " + entityBlock + " temp size: " + (temp.getSize() - 1));
                //берет последний блок, и добавляет его в статистическую переменную.

                prevBlock = UtilsBlockToEntityBlock.entityBlockToBlock(entityBlock);
                BasisController.setPrevBlock(prevBlock);
                BasisController.recordPersistedBlockFromTournament(prevBlock);
                BlockGrowthDiagnostics.tournamentAddBlock3Result(
                        tournamentTraceId,
                        true,
                        tournamentSizeBeforeAdd,
                        BasisController.getBlockchainSize());
                if (prevBlock == null) {
                    System.out.println("----------");
                    System.out.println("prevBlock: " + prevBlock);
                    System.out.println("----------");
                    System.exit(1);
                }

            } else {
                System.out.println("SERVER_BLOCK_ADD_REJECTED=Tournament addBlock3 returned false");
                MyLogger.saveLog("Tournament addBlock3 has error: " + winner.get(0).getIndex());
                BlockGrowthDiagnostics.tournamentSkip(tournamentTraceId, "ADD_BLOCK3_FAILED");
            }
            balances = UtilsAccountToEntityAccount.entityAccountsToMapAccounts(UtilsUse.accounts(list, blockService));

            BasisController.setIsSaveFile(true);

            List<LiteVersionWiner> rankedTournamentCandidates = blockToLiteVersion(winnerList, balances);
            BasisController.setAllWiners(rankedTournamentCandidates);
            BasisController.publishTournamentSnapshot(
                    rankedTournamentCandidates,
                    lastTournamentStartMillis,
                    System.currentTimeMillis(),
                    "tournament after sortWinner",
                    "ranked_by_tournament_sort");
            BasisController.getCountTransactionsWiner().clear();
            BasisController.getStakingWiners().clear();
            BasisController.getBigRandomWiner().clear();
            BasisController.getPowerWiners().clear();
            BasisController.setCountTransactionsWiner(null);
            BasisController.setStakingWiners(null);
            BasisController.setBigRandomWiner(null);
            BasisController.setPowerWiners(null);

            BasisController.setCountTransactionsWiner(blockToLiteVersion(new ArrayList<>(), balances));
            BasisController.setStakingWiners(blockToLiteVersion(new ArrayList<>(), balances));
            BasisController.setBigRandomWiner(blockToLiteVersion(winner, balances));

            BasisController.setPowerWiners(blockToLiteVersion(new ArrayList<>(), balances));
            if (winner.get(0).getIndex() % 432 == 0) {
                BasisController.setTotalTransactionsDays(0);
                BasisController.setTotalTransactionsSumDllar(0);
            }
            BasisController.setTotalTransactionsDays(
                    (int) (BasisController.totalTransactionsDays()
                            + winner.get(0).getDtoTransactions().size())
            );

            BasisController.setTotalTransactionsSumDllar(
                    BasisController.totalTransactionsSumDollar() +
                            winner.get(0).getDtoTransactions().stream()
                                    .mapToDouble(t -> t.getDigitalDollar())
                                    .sum()
            );

            if (BasisController.getBlockchainSize() % 576 == 0) {
                BasisController.setTotalDollars(blockService.getTotalDigitalDollarBalance());
                BasisController.setTotalStaking(blockService.getTotalDigitalStakingBalance());
                BasisController.setTotalAll(blockService.getTotalDigitalAllBalance());


            }


            System.out.println("___________________________________________________");
            long finishTournament = UtilsTime.getUniversalTimestamp();
            System.out.println("finish time: " + UtilsTime.differentMillSecondTime(startTournament, finishTournament));
            System.out.println("___________________________________________________");
            BasisController.setIsSaveFile(true);


        } catch (IOException e) {
            System.out.println("TournamentService: IOException");
            e.printStackTrace();
            MyLogger.saveLog("TournamentService: " + " message: " + e.getMessage() + " ", e);

        } catch (NoSuchAlgorithmException e) {
            System.out.println("TournamentService: NoSuchAlgorithmException");
            e.printStackTrace();
            MyLogger.saveLog("TournamentService: " + " message: " + e.getMessage() + " ", e);

        } catch (InvalidKeySpecException e) {
            System.out.println("TournamentService: InvalidKeySpecException");

            e.printStackTrace();
            MyLogger.saveLog("TournamentService: " + " message: " + e.getMessage() + " ", e);

        } catch (SignatureException e) {
            System.out.println("TournamentService: SignatureException");
            e.printStackTrace();
            MyLogger.saveLog("TournamentService: " + " message: " + e.getMessage() + " ", e);

        } catch (NoSuchProviderException e) {
            System.out.println("TournamentService: NoSuchProviderException");
            e.printStackTrace();
            MyLogger.saveLog("TournamentService: " + " message: " + e.getMessage() + " ", e);

        } catch (InvalidKeyException e) {
            System.out.println("TournamentService: InvalidKeyException");
            e.printStackTrace();
            MyLogger.saveLog("TournamentService: " + " message: " + e.getMessage() + " ", e);

        } catch (CloneNotSupportedException e) {
            System.out.println("TournamentService: CloneNotSupportedException");
            e.printStackTrace();
            MyLogger.saveLog("TournamentService: " + " message: " + e.getMessage() + " ", e);

        } catch (Exception e) {

            MyLogger.saveLog("TournamentService: pool error " + " message: " + e.getMessage() + " ", e);
        } finally {
            NodeMemoryProfiler.recordStage("tournament finish", NodeMemoryProfiler.metrics(
                    "winnerListSize", BasisController.getWinnerList() == null ? 0 : BasisController.getWinnerList().size()
            ));
            NodeMemoryProfiler.endTrace("tournament finish");
            BlockGrowthDiagnostics.finishTournament(
                    tournamentTraceId,
                    BasisController.getBlockchainSize(),
                    BasisController.getWinnerList() == null ? 0 : BasisController.getWinnerList().size());
            clearTemporaryTournamentState();
            lastTournamentEndMillis = System.currentTimeMillis();
            lastTournamentDurationMillis = lastTournamentEndMillis - lastTournamentStartMillis;
            NodeController.setNotReady();
            BasisController.setIsSaveFile(true);
        }

    }

    void clearTemporaryTournamentState() {
        TournamentSnapshot snapshot = BasisController.getLastTournamentSnapshot();
        MyLogger.saveLog("TOURNAMENT_PUBLIC_SNAPSHOT_BEFORE_CLEAR currentWinnerListSize="
                + (BasisController.getWinnerList() == null ? 0 : BasisController.getWinnerList().size())
                + " allWinnersSize=" + (BasisController.getAllWiners() == null ? 0 : BasisController.getAllWiners().size())
                + " snapshotCandidateCount=" + (snapshot == null ? 0 : snapshot.getCandidateCount())
                + " selectedWinnerIndex=" + (snapshot == null ? -1 : snapshot.getBlockIndex())
                + " selectedWinnerMiner=" + (snapshot == null ? "none" : String.valueOf(snapshot.getSelectedWinnerMinerAddress())));
        clearWinners();
        BasisController.setCountTransactionsWiner(new ArrayList<>());
        BasisController.setStakingWiners(new ArrayList<>());
        BasisController.setBigRandomWiner(new ArrayList<>());
        BasisController.setPowerWiners(new ArrayList<>());
        MyLogger.saveLog("TOURNAMENT_PUBLIC_SNAPSHOT_AFTER_CLEAR currentWinnerListSize="
                + (BasisController.getWinnerList() == null ? 0 : BasisController.getWinnerList().size())
                + " snapshotStillAvailable=" + (BasisController.getLastTournamentSnapshot() != null));
        if (poolBlockService != null) {
            poolBlockService.clearPoolBlocks();
        }
    }

    public void sendAndPutHost(Set<String> nodes) {
        // Отправка собственного хоста
        MyHost myHost = new MyHost(domainConfiguration.getPubllc_domain(), Seting.NAME_SERVER, Seting.PUBLIC_KEY);
        MyLogger.saveLog("sendAndPutHost start nodesCount=" + (nodes == null ? 0 : nodes.size()) + " myHost=" + myHost.getHost());
        NodeMemoryProfiler.recordStage("sendAndPutHost start", NodeMemoryProfiler.metrics(
                "nodesCount", nodes == null ? 0 : nodes.size(),
                "myHost", myHost.getHost()
        ));
        if (nodes == null || nodes.isEmpty()) {
            PeerPropagationMonitor.recordSkipped("no_hosts_for_put", 0, 0);
            NodeMemoryProfiler.recordStage("sendAndPutHost skip", NodeMemoryProfiler.metrics("skipReason", "no_hosts_for_put"));
            return;
        }
        if (UtilsResolving.shouldSkipPeerPropagationForCurrentMemory()) {
            PeerPropagationMonitor.recordSkipped("low_memory_before_put_host", 0, 0);
            MyLogger.warnLog("PEER_PROPAGATION_SKIPPED_LOW_MEMORY phase=sendAndPutHost");
            NodeMemoryProfiler.recordStage("sendAndPutHost skip", NodeMemoryProfiler.metrics("skipReason", "low_memory_before_put_host"));
            return;
        }
        int selfSkipped = (int) nodes.stream().filter(UtilsResolving::isSelfPeer).count();
        Set<String> boundedNodes = nodes.stream()
                .filter(s -> s != null && !s.isBlank())
                .map(s -> s.contains("\"") ? s.replace("\"", "") : s)
                .filter(s -> !UtilsResolving.isSelfPeer(s))
                .filter(s -> !BasisController.getExcludedAddresses().contains(s))
                .distinct()
                .limit(UtilsResolving.MAX_PROPAGATION_HOSTS)
                .collect(Collectors.toCollection(HashSet::new));
        if (boundedNodes.isEmpty()) {
            PeerPropagationMonitor.recordSkipped("no_remote_put_hosts", 0, selfSkipped);
            MyLogger.warnLog("PEER_PROPAGATION_SKIPPED_SELF phase=sendAndPutHost selfSkipped=" + selfSkipped);
            NodeMemoryProfiler.recordStage("sendAndPutHost skip", NodeMemoryProfiler.metrics(
                    "skipReason", "no_remote_put_hosts",
                    "selfPeersSkippedCount", selfSkipped
            ));
            return;
        }
        nodes = boundedNodes;
        NodeMemoryProfiler.recordStage("sendAndPutHost after host filtering", NodeMemoryProfiler.metrics(
                "hostsListSize", nodes.size(),
                "selfPeersSkippedCount", selfSkipped
        ));
        PeerPropagationMonitor.recordStarted(nodes.size(), selfSkipped);
        try {
            UtilsAllAddresses.sendAddress(nodes, myHost);
        } catch (IOException e) {
            PEER_ERROR_RATE_LIMITER.logPeerError("sendAddress_batch", "/putNode", e);
        }

        // Скачивание всех хостов

        nodes.remove(myHost.getHost());

        // Параллельная обработка узлов
        ExecutorService getNodesExecutor = newTournamentExecutor(nodes.size());
        List<CompletableFuture<Void>> futures = new ArrayList<>(nodes.stream()
                .filter(s -> s != null && !s.isBlank()) // Удаляем пустые строки и null
                .map(s -> CompletableFuture.runAsync(() -> {
                    try {
                        Set<String> tempNode = UtilsJson.jsonToSetAddresses(UtilUrl.readJsonFromUrl(
                                s + "/getNodes",
                                TOURNAMENT_HTTP_TIMEOUT_MILLIS,
                                UtilUrl.MAX_GET_NODES_RESPONSE_CHARS));

                        if (BasisController.getExcludedAddresses().contains(s) || s.equals(myHost.getHost())) {
                            MyLogger.saveLog("sendAndPutHost skipped self/excluded host=" + s);
                            return;
                        }

                        tempNode.forEach(s1 -> {
                            UtilsAllAddresses.putHost(s1);
                        });
                        MyLogger.saveLog("sendAndPutHost updated host=" + s + " discoveredCount=" + tempNode.size());
                    } catch (Exception e) {
                        PEER_ERROR_RATE_LIMITER.logPeerError(s, e);
                    }
                }, getNodesExecutor))
                .toList());
        PeerConnectionDiagnostics.beginFutures("tournament_sendAndPutHost", futures.size());
        NodeMemoryProfiler.recordStage("sendAndPutHost futures created", NodeMemoryProfiler.metrics(
                "futuresCreatedCount", futures.size(),
                "hostsListSize", nodes.size()
        ));

        // Ожидание завершения всех задач
        CompletableFuture<Void> allFutures = CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));
        try {
            allFutures.orTimeout(12, TimeUnit.SECONDS).join(); // Дождаться завершения всех задач
        } catch (Exception e) {
            PEER_ERROR_RATE_LIMITER.logPeerError("sendAndPutHost_batch", "/getNodes", e);
        } finally {
            PeerConnectionDiagnostics.endFutures("tournament_sendAndPutHost", futures.size());
            getNodesExecutor.shutdownNow();
        }
        long failures = futures.stream().filter(CompletableFuture::isCompletedExceptionally).count();
        PeerPropagationMonitor.recordFinished(nodes.size(), nodes.size() - (int) failures, (int) failures, selfSkipped);
        MyLogger.saveLog("sendAndPutHost finish nodesCount=" + nodes.size());
        NodeMemoryProfiler.recordStage("sendAndPutHost finish", NodeMemoryProfiler.metrics(
                "hostsListSize", nodes.size(),
                "futuresCreatedCount", futures.size(),
                "futuresCompletedCount", futures.size() - (int) failures,
                "futuresFailedCount", failures
        ));
        futures.clear();
    }

    public void updatingNodeEndBlocks(List<HostEndDataShortB> hostEndDataShortBS) {
        int result = -10;
        try {


            // Обновление через utilsResolving
            result = utilsResolving.resolve3(hostEndDataShortBS);
            System.out.println("finish updating --------------------------------------------");


            // Очистка списков победителей
            clearWinners();


        } finally {
            BasisController.setIsSaveFile(true);
        }
    }

    // Очистка списков победителей
    private void clearWinners() {
        if (winner == null) winner = new ArrayList<>();
        else winner.clear();

        if (winnerDiff == null) winnerDiff = new ArrayList<>();
        else winnerDiff.clear();

        if (winnerCountTransaction == null) winnerCountTransaction = new ArrayList<>();
        else winnerCountTransaction.clear();

        if (winnerStaking == null) winnerStaking = new ArrayList<>();
        else winnerStaking.clear();

        if (BasisController.getWinnerList() == null) {
            BasisController.setWinnerList(new CopyOnWriteArrayList<>());
            BasisController.setSizeWinnerList(0);
        } else {
            BasisController.getWinnerList().clear();
            BasisController.setSizeWinnerList(0);
        }
    }

    public List<DtoTransaction> getInstance(List<HostEndDataShortB> hosts) throws IOException, NoSuchAlgorithmException, InvalidKeySpecException, SignatureException, NoSuchProviderException, InvalidKeyException {
        hosts = boundedHostsForTournament(hosts);
        NodeMemoryProfiler.recordStage("getTransactions start", NodeMemoryProfiler.metrics(
                "hostsListSize", hosts == null ? 0 : hosts.size()
        ));
        ExecutorService executor = newTournamentExecutor(hosts.size());
        // Асинхронная обработка для получения транзакций с каждого хоста
        List<CompletableFuture<List<DtoTransaction>>> futures = new ArrayList<>(hosts.stream()
                .map(hostEndDataShortB -> CompletableFuture.supplyAsync(() -> {
                    String host = hostEndDataShortB.getHost();
                    try {
                        if (UtilsResolving.isSelfPeer(host)) {
                            BasisController.recordSelfPeerSkipped();
                            MyLogger.warnLog("SELF_PEER_SKIPPED phase=getTransactions peer=" + host);
                            return new ArrayList<DtoTransaction>();
                        }
                        long requestStart = System.currentTimeMillis();
                        NodeMemoryProfiler.recordStage("getTransactions before remote GET", NodeMemoryProfiler.metrics("peer", host));
                        String json = UtilUrl.readJsonFromUrl(
                                host + "/getTransactions",
                                TOURNAMENT_HTTP_TIMEOUT_MILLIS,
                                UtilUrl.MAX_GET_TRANSACTIONS_RESPONSE_CHARS);
                        MyLogger.saveLog("getTransactions host=" + host + " bytes=" + json.length() + " durationMs=" + (System.currentTimeMillis() - requestStart));
                        NodeMemoryProfiler.recordStage("getTransactions after remote GET", NodeMemoryProfiler.metrics(
                                "peer", host,
                                "responseBytes", json == null ? 0 : json.length(),
                                "jsonStringLength", json == null ? 0 : json.length(),
                                "durationMs", System.currentTimeMillis() - requestStart
                        ));
                        if (!json.isEmpty()) {
                            List<DtoTransaction> list = UtilsJson.jsonToDtoTransactionList(json);
                            MyLogger.saveLog("getTransactions host=" + host + " txCount=" + list.size());
                            NodeMemoryProfiler.recordStage("getTransactions after parse", NodeMemoryProfiler.metrics(
                                    "peer", host,
                                    "txCount", list == null ? 0 : list.size(),
                                    "jsonStringLength", json.length()
                            ));
                            return utilsTransactions.balanceTransaction(list, blockService);
                        }
                    } catch (IOException | JSONException e) {
                        PEER_ERROR_RATE_LIMITER.logPeerError(host, e);
                    }
                    return new ArrayList<DtoTransaction>();
                }, executor))
                .toList());
        PeerConnectionDiagnostics.beginFutures("tournament_getTransactions", futures.size());

        // Сбор всех результатов
        List<DtoTransaction> instance;
        try {
            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
                    .orTimeout(12, TimeUnit.SECONDS)
                    .join();
            instance = futures.stream()
                    .map(future -> future.isCompletedExceptionally() ? new ArrayList<DtoTransaction>() : future.join())
                    .flatMap(List::stream)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            PEER_ERROR_RATE_LIMITER.logPeerError("getTransactions_batch", "/getTransactions", e);
            instance = new ArrayList<>();
        } finally {
            PeerConnectionDiagnostics.endFutures("tournament_getTransactions", futures.size());
            executor.shutdownNow();
        }

        // Добавление транзакций из локального хранилища и удаление дубликатов
        NodeMemoryProfiler.recordStage("getTransactions finish", NodeMemoryProfiler.metrics(
                "hostsListSize", hosts == null ? 0 : hosts.size(),
                "futuresCreatedCount", futures.size(),
                "transactionListSize", instance == null ? 0 : instance.size()
        ));
        futures.clear();
        instance.addAll(UtilsTransaction.readLineObject(Seting.ORGINAL_ALL_TRANSACTION_FILE));
        return instance.stream().distinct().collect(Collectors.toList());
    }


}
