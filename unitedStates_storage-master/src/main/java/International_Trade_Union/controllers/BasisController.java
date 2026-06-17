package International_Trade_Union.controllers;

import International_Trade_Union.controllers.config.BlockchainFactoryEnum;
import International_Trade_Union.entity.*;
import International_Trade_Union.entity.DtoTransaction.DtoTransaction;
import International_Trade_Union.entity.blockchain.DataShortBlockchainInformation;
import International_Trade_Union.entity.entities.EntityAccount;
import International_Trade_Union.entity.entities.EntityBlock;
import International_Trade_Union.entity.services.BlockService;
import International_Trade_Union.entity.services.PoolBlockService;
import International_Trade_Union.logger.MyLogger;
import International_Trade_Union.model.*;
import International_Trade_Union.network.AllTransactions;
import International_Trade_Union.protocol.v35.ChainTournamentScorer;
import International_Trade_Union.vote.LawEligibleForParliamentaryApproval;
import International_Trade_Union.vote.Laws;
import International_Trade_Union.vote.UtilsLaws;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.json.JSONException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import International_Trade_Union.entity.blockchain.Blockchain;
import International_Trade_Union.entity.blockchain.block.Block;
import International_Trade_Union.setings.Seting;
import International_Trade_Union.utils.*;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

import java.lang.management.ManagementFactory;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.URL;
import java.security.*;
import java.security.spec.InvalidKeySpecException;

import java.sql.Timestamp;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

import static International_Trade_Union.setings.Seting.*;
import static International_Trade_Union.setings.Seting.MULTIPLIER2;
import static International_Trade_Union.utils.UtilsBalance.calculateBalance;

@RestController
public class BasisController {

    private static final PeerErrorRateLimiter RESOLVE_CONFLICT_ERROR_LIMITER = new PeerErrorRateLimiter();


    @PostConstruct
    public void init() {
        Blockchain.setBlockService(blockService);
        UtilsBalance.setBlockService(blockService);
        UtilsBlock.setBlockService(blockService);

    }

    private BlockService blockService;

    @Autowired
    public BasisController(BlockService blockService) {
        this.blockService = blockService;
        initializeBlockchain();
        Blockchain.setBlockService(blockService);
        UtilsBalance.setBlockService(blockService);
        UtilsBlock.setBlockService(blockService);

    }


    private void initializeBlockchain() {
        try {

            UtilsCreatedDirectory.createPackages();
//            blockchain = BLockchainFactory.getBlockchain(BlockchainFactoryEnum.ORIGINAL);
//            blockchain = Mining.getBlockchain(
//                    Seting.ORIGINAL_BLOCKCHAIN_FILE,
//                    BlockchainFactoryEnum.ORIGINAL);
            String json = UtilsFileSaveRead.read(Seting.TEMPORARY_BLOCKCHAIN_FILE);
            if (!json.isEmpty() || !json.isBlank()) {
                shortDataBlockchain = UtilsJson.jsonToDataShortBlockchainInformation(json);

            } else {
                shortDataBlockchain = Blockchain.checkFromFile(Seting.ORIGINAL_BLOCKCHAIN_FILE);

//            prevBlock = UtilsBlockToEntityBlock.entityBlockToBlock(BlockService.findById((long) blockchainSize+1));

                json = UtilsJson.objToStringJson(shortDataBlockchain);
                UtilsFileSaveRead.save(json, Seting.TEMPORARY_BLOCKCHAIN_FILE, false);
            }
            System.out.println("static: shortDataBlockchain: " + shortDataBlockchain);
            blockcheinSize = (int) shortDataBlockchain.getSize();
            blockchainValid = shortDataBlockchain.isValidation();
            if (blockcheinSize == 0) {
                prevBlock = null;
                dificultyOneBlock = 1;
                System.out.println("empty local chain: bootstrap state");
                return;
            }
            prevBlock = Blockchain.indexFromFile(blockcheinSize - 1, Seting.ORIGINAL_BLOCKCHAIN_FILE);


            if (blockcheinSize < Seting.V34_NEW_ALGO) {
                if (blockcheinSize > 600) {
                    dificultyOneBlock = UtilsBlock.difficulty(Blockchain.subFromFile(
                                    blockcheinSize - 600, blockcheinSize, Seting.ORIGINAL_BLOCKCHAIN_FILE),
                            Seting.BLOCK_GENERATION_INTERVAL,
                            Seting.DIFFICULTY_ADJUSTMENT_INTERVAL);
                } else {
                    dificultyOneBlock = UtilsBlock.difficulty(Blockchain.subFromFile(
                                    blockcheinSize - 600, blockcheinSize, Seting.ORIGINAL_BLOCKCHAIN_FILE),
                            Seting.BLOCK_GENERATION_INTERVAL,
                            Seting.DIFFICULTY_ADJUSTMENT_INTERVAL);
                }
            } else {
                dificultyOneBlock = prevBlock().getHashCompexity();
            }


        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        } catch (InvalidKeySpecException e) {
            throw new RuntimeException(e);
        } catch (SignatureException e) {
            throw new RuntimeException(e);
        } catch (NoSuchProviderException e) {
            throw new RuntimeException(e);
        } catch (InvalidKeyException e) {
            throw new RuntimeException(e);
        }
    }


    /**
     * Каждые 100 секунд происходить турнир для отбора победителя.
     */
    @Autowired
    Tournament tournament;
    @Autowired
    UtilsResolving utilsResolving;
    @Autowired(required = false)
    AllTransactions allTransactions;
    @Autowired(required = false)
    PoolBlockService poolBlockService;

    private static volatile AtomicBoolean blockedNewSendBlock = new AtomicBoolean(true);
    public static final int MAX_TEMPORARY_SUBMITTED_BLOCKS = 100;
    static final int MAX_TOURNAMENT_CANDIDATE_SNAPSHOT_SIZE = 100;

    public static AtomicBoolean getBlockedNewSendBlock() {
        return blockedNewSendBlock;
    }

    public static void setBlockedNewSendBlock(AtomicBoolean blockedNewSendBlock) {
        BasisController.blockedNewSendBlock = blockedNewSendBlock;
    }

    /**
     * список кандидатов с данного сервера в качестве победителей
     */
    private static CopyOnWriteArrayList<Block> winnerList = new CopyOnWriteArrayList<>();
    private static AtomicInteger sizeWinnerList = new AtomicInteger(0);
    private static final ReentrantLock lock = new ReentrantLock();
    private static final int MAX_PUBLIC_TOURNAMENT_SNAPSHOTS = 10;
    private static volatile TournamentSnapshot lastTournamentSnapshot = null;
    private static final Deque<TournamentSnapshot> lastTournamentSnapshots = new ArrayDeque<>();

    public static AtomicInteger getSizeWinnerList() {
        return sizeWinnerList;
    }

    public static void setSizeWinnerList(int size) {
        sizeWinnerList.set(size);
    }

    @GetMapping(value = "/winnerList", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public String winnerList() {
        if (System.currentTimeMillis() >= 0) {
            long start = System.currentTimeMillis();
            long memoryBefore = usedMemory();
            List<Block> result = currentCandidatesSnapshot();
            MyLogger.saveLog("PUBLIC_TOURNAMENT_ENDPOINT endpoint=/winnerList resultSize=" + result.size()
                    + " durationMs=" + (System.currentTimeMillis() - start)
                    + " memoryDelta=" + (usedMemory() - memoryBefore));
            try {
                return UtilsJson.objToStringJson(result);
            } catch (IOException e) {
                MyLogger.saveLog("Error serializing current candidates JSON: " + e.getMessage());
                return "[]";
            }
        }
        try {
            // Получаем текущий список победителей
            List<Block> currentWinnerList = BasisController.getWinnerList();
            CopyOnWriteArrayList<Block> tempList = new CopyOnWriteArrayList<>();

            // Быстрая проверка кэша
            if (isCacheValid(currentWinnerList)) {
                tempList.addAll(winnerList);
                return UtilsJson.objToStringJson(tempList);
            }

            // Попытка захватить блокировку для обновления
            if (lock.tryLock()) {
                try {
                    // Повторная проверка после захвата блокировки
                    if (!isCacheValid(currentWinnerList)) {
                        updateWinnerListCache(currentWinnerList);
                    }
                } finally {
                    lock.unlock(); // Освобождаем блокировку
                }
            }

            // После обновления или если блокировка недоступна, возвращаем результат
            if (!winnerList.isEmpty()) {
                tempList.addAll(winnerList);
            }
            return UtilsJson.objToStringJson(tempList);
        } catch (IOException e) {
            MyLogger.saveLog("Error serializing JSON: " + e.getMessage());
            e.printStackTrace();
            return fallbackJson();
        } catch (Exception e) {
            MyLogger.saveLog("Unexpected error in winnerList method: " + e.getMessage());
            e.printStackTrace();
            return fallbackJson();
        }
    }

    @GetMapping(value = "/currentCandidates", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public List<Block> currentCandidates() {
        long start = System.currentTimeMillis();
        long memoryBefore = usedMemory();
        List<Block> result = currentCandidatesSnapshot();
        MyLogger.saveLog("PUBLIC_TOURNAMENT_ENDPOINT endpoint=/currentCandidates resultSize=" + result.size()
                + " durationMs=" + (System.currentTimeMillis() - start)
                + " memoryDelta=" + (usedMemory() - memoryBefore));
        return result;
    }

    // Возвращает JSON пустого массива в случае ошибки
    private String fallbackJson() {
        try {
            return UtilsJson.objToStringJson(new CopyOnWriteArrayList<>());
        } catch (IOException e) {
            MyLogger.saveLog("Error serializing fallback JSON: " + e.getMessage());
            e.printStackTrace();
            return "[]"; // Последний уровень защиты: возвращаем строку с пустым массивом
        }
    }

    // Проверяет, актуален ли кэш
    private boolean isCacheValid(List<Block> currentWinnerList) {
        return sizeWinnerList.get() == currentWinnerList.size() && !winnerList.isEmpty();
    }

    // Обновляет кэш winnerList
    private void updateWinnerListCache(List<Block> currentWinnerList) {
//        long timeBefore = UtilsTime.getUniversalTimestamp();
        Map<String, Account> balances;
        try {
            // Обработка IOException при получении accounts
            balances = UtilsAccountToEntityAccount.entityAccountsToMapAccounts(
                    UtilsUse.accounts(currentWinnerList, blockService));
        } catch (IOException e) {
            MyLogger.saveLog("Error fetching accounts: " + e.getMessage());
            e.printStackTrace();
            return; // Прекращаем обновление кэша, если невозможно получить accounts
        }

        Map<String, Account> finalBalances;
        try {
            // Обработка CloneNotSupportedException при клонировании balances
            finalBalances = UtilsUse.balancesClone(balances);
        } catch (CloneNotSupportedException e) {
            MyLogger.saveLog("Error cloning balances: " + e.getMessage());
            e.printStackTrace();
            return; // Прекращаем обновление кэша, если невозможно клонировать balances
        }

        // Добавляем недостающие записи в finalBalances
        currentWinnerList.forEach(block -> finalBalances.computeIfAbsent(
                block.getMinerAddress(),
                address -> new Account(address, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO)
        ));

        // Сортировка победителей
        List<Block> tempWinner = TournamentService.sortWinner(finalBalances, currentWinnerList);

        // Обновляем кэш
        winnerList.clear();
        winnerList.addAll(tempWinner);

        sizeWinnerList.set(currentWinnerList.size());
//        long timeAfter = UtilsTime.getUniversalTimestamp();
//        MyLogger.saveLog("updateWinnerListCache: millisecond: " + (timeAfter - timeBefore) + " second: " +((timeAfter-timeBefore)/1000));
    }


    private static boolean updating;
    /**
     * список всех победителей, данные могут не отражать реального победителя
     * если победитель был получен из другого сервера
     */
    private static List<LiteVersionWiner> allWiners = new ArrayList<>();
    /**
     * на данный момент присутствуетЮ в качестве анализа сложности блока, но особо не используется
     */
    private static List<LiteVersionWiner> powerWiners = new ArrayList<>();
    /**
     * на данный момент присутствуетЮ в качестве анализа сложности блока, но особо не используется
     */

    private static List<LiteVersionWiner> countTransactionsWiner = new ArrayList<>();
    /**
     * на данный момент присутствуетЮ в качестве анализа сложности блока, но особо не используется
     */
    private static List<LiteVersionWiner> stakingWiners = new ArrayList<>();
    /**
     * окончательный победитель по показателю big random,
     * но может не учитывать реального победителя, если он получен из другого сервера.
     */
    private static List<LiteVersionWiner> bigRandomWiner = new ArrayList<>();

    public static void setTotalTransactionsDays(int totalTransactionsDays) {
        BasisController.totalTransactionsDays = totalTransactionsDays;
    }

    /**
     * общее количество транзакций в сутки
     */
    public static long totalTransactionsDays() {
        return BasisController.totalTransactionsDays;
    }

    /**
     * общее сумма долларов переведенных в сутки
     */
    public static double totalTransactionsSumDollar() {
        return BasisController.totalTransactionsSumDllar;
    }

    public static void setTotalTransactionsSumDllar(double totalTransactionsSumDllar) {
        BasisController.totalTransactionsSumDllar = totalTransactionsSumDllar;
    }

    public static void setTotalDollars(double totalDollars) {
        BasisController.totalDollars = totalDollars;
    }

    public static void setBalances(Map<String, Account> balances) {
        BasisController.balances = balances;
    }

    public static void setTotalStaking(double totalStaking) {
        BasisController.totalStaking = totalStaking;
    }

    public static void setTotalAll(double totalAll) {
        BasisController.totalAll = totalAll;
    }

    public static boolean isBlockchainValid() {
        return blockchainValid;
    }

    public static double totalDollars() {
        return totalDollars;
    }

    public static void setUpdating(boolean b) {
        updating = b;
    }

    public static boolean getUpdating() {
        return updating;
    }

    /**
     * Список кандидатов из которых был выбран победитель в данном сервере
     */
    @GetMapping(value = {"/allwinners", "/lastTournamentCandidates"}, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public List<LiteVersionWiner> allWinners() {
        long start = System.currentTimeMillis();
        long memoryBefore = usedMemory();
        List<LiteVersionWiner> result = lastTournamentCandidatesSnapshot();
        MyLogger.saveLog("PUBLIC_TOURNAMENT_ENDPOINT endpoint=/allwinners resultSize=" + result.size()
                + " durationMs=" + (System.currentTimeMillis() - start)
                + " memoryDelta=" + (usedMemory() - memoryBefore));
        return result;
    }

    @GetMapping(value = "/lastTournament", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public TournamentSnapshot lastTournament() {
        long start = System.currentTimeMillis();
        long memoryBefore = usedMemory();
        TournamentSnapshot snapshot = lastTournamentSnapshot == null ? TournamentSnapshot.empty() : lastTournamentSnapshot;
        MyLogger.saveLog("PUBLIC_TOURNAMENT_ENDPOINT endpoint=/lastTournament candidateCount=" + snapshot.getCandidateCount()
                + " winnerMiner=" + String.valueOf(snapshot.getSelectedWinnerMinerAddress())
                + " durationMs=" + (System.currentTimeMillis() - start)
                + " memoryDelta=" + (usedMemory() - memoryBefore));
        return snapshot;
    }

    @GetMapping(value = "/lastTournamentWinner", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public Object lastTournamentWinner() {
        TournamentSnapshot snapshot = lastTournamentSnapshot;
        return snapshot == null || snapshot.getSelectedWinner() == null ? Collections.emptyMap() : snapshot.getSelectedWinner();
    }

    /**
     * Устарел и не используется
     */
    @GetMapping("/powerWiners")
    @ResponseBody
    public String powerWiners() {
        String json = "";
        try {
            json = UtilsJson.objToStringJson(powerWiners);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return json;
    }

    /**
     * Устарел и не используется
     */
    @GetMapping("/countTransactionsWiner")
    @ResponseBody
    public String countTransactionsWiner() {
        String json = "";
        try {
            json = UtilsJson.objToStringJson(countTransactionsWiner);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return json;
    }

    /**
     * Устарел и не используется
     */
    @GetMapping("/stakingWiners")
    @ResponseBody
    public String stakingWiners() {
        String json = "";
        try {
            json = UtilsJson.objToStringJson(stakingWiners);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return json;
    }

    /**
     * Параметр который определяет победителя, складывается из трех параметров.
     * 1. Хэш блока является семенем для генерации случайного числа от нуля до 135.
     * 2. сложность блока умноженая на число на данный момент 35, но может быть изменен
     * 3. Стэйкинг, где спомощью логарифма вычисляется баллы, которые указаны в таблице.
     * Все эти числа суммируется и получается результат, тот который получил наибольшее
     * значение, становиться победителем.
     */
    @GetMapping("/bigRandomWiner")
    @ResponseBody
    public String bigRandomWiner() {
        String json = "";
        try {

            json = UtilsJson.objToStringJson(bigRandomWiner);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return json;
    }

    public static List<LiteVersionWiner> getCountTransactionsWiner() {
        return countTransactionsWiner;
    }

    public static void setCountTransactionsWiner(List<LiteVersionWiner> countTransactionsWiner) {
        BasisController.countTransactionsWiner = countTransactionsWiner;
    }

    public static List<LiteVersionWiner> getBigRandomWiner() {
        return bigRandomWiner;
    }

    public static void setBigRandomWiner(List<LiteVersionWiner> bigRandomWiner) {
        BasisController.bigRandomWiner = bigRandomWiner;
    }

    public static List<LiteVersionWiner> getStakingWiners() {
        return stakingWiners;
    }

    public static void setStakingWiners(List<LiteVersionWiner> stakingWiners) {
        BasisController.stakingWiners = stakingWiners;
    }

    public static List<LiteVersionWiner> getPowerWiners() {
        return powerWiners;
    }

    public static void setPowerWiners(List<LiteVersionWiner> powerWiners) {
        BasisController.powerWiners = powerWiners;
    }

    public static List<LiteVersionWiner> getAllWiners() {
        return allWiners;
    }

    public static void setAllWiners(List<LiteVersionWiner> allWiners) {
        BasisController.allWiners = capLiteCandidates(allWiners, "allWiners");
    }

    public static List<Block> currentCandidatesSnapshot() {
        CopyOnWriteArrayList<Block> current = winnerList;
        if (current == null) {
            return new ArrayList<>();
        }
        return current.stream().limit(MAX_TOURNAMENT_CANDIDATE_SNAPSHOT_SIZE).collect(Collectors.toList());
    }

    private static CopyOnWriteArrayList<Block> capBlockCandidates(List<Block> candidates, String collectionName) {
        CopyOnWriteArrayList<Block> capped = new CopyOnWriteArrayList<>();
        if (candidates == null || candidates.isEmpty()) {
            return capped;
        }
        int originalSize = candidates.size();
        candidates.stream().limit(MAX_TOURNAMENT_CANDIDATE_SNAPSHOT_SIZE).forEach(capped::add);
        if (originalSize > MAX_TOURNAMENT_CANDIDATE_SNAPSHOT_SIZE) {
            MyLogger.warnLog("CANDIDATE_LIMIT_REACHED collection=" + collectionName
                    + " originalCount=" + originalSize
                    + " retained=" + MAX_TOURNAMENT_CANDIDATE_SNAPSHOT_SIZE);
        }
        return capped;
    }

    private static List<LiteVersionWiner> capLiteCandidates(List<LiteVersionWiner> candidates, String collectionName) {
        if (candidates == null || candidates.isEmpty()) {
            return new ArrayList<>();
        }
        int originalSize = candidates.size();
        List<LiteVersionWiner> capped = candidates.stream()
                .limit(MAX_TOURNAMENT_CANDIDATE_SNAPSHOT_SIZE)
                .collect(Collectors.toList());
        if (originalSize > MAX_TOURNAMENT_CANDIDATE_SNAPSHOT_SIZE) {
            MyLogger.warnLog("CANDIDATE_LIMIT_REACHED collection=" + collectionName
                    + " originalCount=" + originalSize
                    + " retained=" + MAX_TOURNAMENT_CANDIDATE_SNAPSHOT_SIZE);
        }
        return capped;
    }

    public static List<LiteVersionWiner> lastTournamentCandidatesSnapshot() {
        TournamentSnapshot snapshot = lastTournamentSnapshot;
        if (snapshot == null || snapshot.getCandidates() == null) {
            return new ArrayList<>();
        }
        return new ArrayList<>(snapshot.getCandidates());
    }

    public static TournamentSnapshot getLastTournamentSnapshot() {
        return lastTournamentSnapshot;
    }

    public static synchronized void publishTournamentSnapshot(List<LiteVersionWiner> rankedCandidates,
                                                              long tournamentStartTime,
                                                              long tournamentEndTime,
                                                              String selectionStage,
                                                              String selectionReason) {
        List<LiteVersionWiner> candidates = rankedCandidates == null
                ? new ArrayList<>()
                : rankedCandidates.stream().limit(MAX_TOURNAMENT_CANDIDATE_SNAPSHOT_SIZE).collect(Collectors.toList());
        if (rankedCandidates != null && rankedCandidates.size() > MAX_TOURNAMENT_CANDIDATE_SNAPSHOT_SIZE) {
            MyLogger.warnLog("CANDIDATE_LIMIT_REACHED collection=lastTournamentSnapshot originalCount="
                    + rankedCandidates.size() + " retained=" + MAX_TOURNAMENT_CANDIDATE_SNAPSHOT_SIZE);
        }
        LiteVersionWiner selected = candidates.isEmpty() ? null : candidates.get(0);
        TournamentSnapshot snapshot = new TournamentSnapshot();
        snapshot.setBlockIndex(selected == null ? -1L : selected.getIndex());
        snapshot.setTournamentStartTime(tournamentStartTime);
        snapshot.setTournamentEndTime(tournamentEndTime);
        snapshot.setCandidateCount(candidates.size());
        snapshot.setCandidates(candidates);
        snapshot.setSelectedWinner(selected);
        snapshot.setSelectedWinnerMinerAddress(selected == null ? null : selected.getAddress());
        snapshot.setSelectedWinnerHash(selected == null ? null : selected.getHash());
        snapshot.setSelectedWinnerBigRandom(selected == null ? 0 : selected.getBigRandom());
        snapshot.setSelectedWinnerDifficulty(selected == null ? 0L : selected.getDiff());
        snapshot.setSelectedWinnerTxCount(selected == null ? 0 : selected.getCountTransactions());
        snapshot.setSelectedWinnerStakingScore(selected == null ? 0.0 : selected.getStaking());
        snapshot.setSelectionStage(selectionStage == null ? "tournament after sortWinner" : selectionStage);
        snapshot.setSelectionReason(selectionReason == null ? "ranked_by_tournament_sort" : selectionReason);
        snapshot.setCreatedAt(new Date().toString());
        lastTournamentSnapshot = snapshot;
        lastTournamentSnapshots.addLast(snapshot);
        while (lastTournamentSnapshots.size() > MAX_PUBLIC_TOURNAMENT_SNAPSHOTS) {
            lastTournamentSnapshots.removeFirst();
        }
        allWiners = new ArrayList<>(candidates);
    }

    static void resetTournamentSnapshotsForTests() {
        lastTournamentSnapshot = null;
        lastTournamentSnapshots.clear();
        allWiners = new ArrayList<>();
        if (winnerList == null) {
            winnerList = new CopyOnWriteArrayList<>();
        } else {
            winnerList.clear();
        }
        sizeWinnerList.set(0);
    }

    private static long usedMemory() {
        Runtime runtime = Runtime.getRuntime();
        return runtime.totalMemory() - runtime.freeMemory();
    }

    private static int totalTransactionsDays = 0;
    private static double totalTransactionsSumDllar = 0.0;


    /**
     * Все акаунты
     */
    private static Map<String, Account> balances = new HashMap<>();

    /**
     * Общее сумма долларов, не находящиеся в стэйкинге
     */
    private static double totalDollars = 0;
    private static double totalStaking = 0;
    private static double totalAll = 0;
    private static long dificultyOneBlock;

    /**
     * Если происходит запись, то не дает доступа к базе данных
     */
    private volatile static boolean isSaveFile = true;

    /**
     * Последний блок в базе данных
     */
    private static Block prevBlock = null;

    /**
     * Определяет в ценость блокчейна за счет мета данных, некоторые части уже не используется
     * на данный момент используется только big random, size и количество транзакций. Оставшиеся
     * параметры могут быть искажены. Если кошелек настроен на сервер и на сервере и на кошельке отличается
     * параметр staking при одинаковой высоте, это может быть что поврежден файл resources либо
     * на кошельке или сервере. Решение скачать с нуля папку ресурсы.
     */
    private static DataShortBlockchainInformation shortDataBlockchain = null;

    /**
     * Высота блокчейна
     */
    private static int blockcheinSize = 0;
    private static volatile long lastReceivedBlockIndex = -1;
    private static volatile String lastReceivedBlockHash = "none";
    private static volatile String lastReceivedAt = "never";
    private static volatile String lastSubmitRemoteAddress = "none";
    private static volatile String lastValidationResult = "none";
    private static volatile String lastRejectReason = "none";
    private static volatile long lastAddedBlockIndex = -1;
    private static volatile String lastAddedBlockHash = "none";
    private static volatile String lastAddedAt = "never";
    private static volatile String lastWalletSubmitEndpoint = "none";
    private static volatile String lastServerException = "none";
    private static volatile String lastPrePersistPeerPropagationSkippedReason = "never";
    private static volatile boolean lastPrePersistPeerPropagationAttempted = false;
    private static volatile String lastPrePersistPeerPropagationSkippedAt = "never";
    private static volatile long lastResolveConflictReceivedBlockIndex = -1;
    private static volatile long lastResolveConflictQueuedBlockIndex = -1;
    private static volatile long lastResolveConflictBeforeTournamentMemoryUsed = -1;
    private static volatile long lastResolveConflictBeforeTournamentMemoryMax = -1;
    private static volatile String lastLocalPersistenceStartedAt = "never";
    private static volatile String lastLocalPersistenceFinishedAt = "never";
    private static volatile String lastLocalPersistenceResult = "none";
    private static volatile long selfPeerSkippedCount = 0;
    private static volatile long prePersistOutboundTasksBlockedCount = 0;
    /**
     * целостность блокчейна, если блокчейн поврежден будет false, и папка ресурсы на кошельке
     * удаляется, а сервер автоматически выключается System.exit(0)
     */
    private static boolean blockchainValid = false;
    //    private static Blockchain blockchain;
    private static Set<String> excludedAddresses = new HashSet<>();
    private static boolean isSave = true;

    /**
     * целостность блокчейна, если блокчейн поврежден будет false, и папка ресурсы на кошельке
     * удаляется, а сервер автоматически выключается System.exit(0)
     */
    public static void setBlockchainValid(boolean blockchainValid) {
        BasisController.blockchainValid = blockchainValid;
    }

    public static void setShortDataBlockchain(DataShortBlockchainInformation shortDataBlockchain) {
        BasisController.shortDataBlockchain = shortDataBlockchain;
    }

    /**
     * Последний блок в базе данных
     */
    public static Block prevBlock() {
        return prevBlock;
    }

    public static void changePrevBlock(Block block) {
        prevBlock = block;
    }

    /**
     * Определяет в ценость блокчейна за счет мета данных, некоторые части уже не используется
     * на данный момент используется только big random, size и количество транзакций. Оставшиеся
     * параметры могут быть искажены. Если кошелек настроен на сервер и на сервере и на кошельке отличается
     * параметр staking при одинаковой высоте, это может быть что поврежден файл resources либо
     * на кошельке или сервере. Решение скачать с нуля папку ресурсы.
     */
    @GetMapping("/datashort")
    public DataShortBlockchainInformation dataShortBlockchainInformation() {
//        System.out.println("get /datashort");
        DataShortBlockchainInformation temp = shortDataBlockchain;
//        System.out.println("/datashort: " + temp);
        return temp;
    }

    public static void setPrevBlock(Block prevBlock) {
        BasisController.prevBlock = prevBlock;
    }


    /**
     * Список потенциальных кандидатов на сервере за блок, но еще результат не предрешён
     * и сюда будут добавляться блоки в течение 100 секунд, но блоки, если их время
     * больше по времени от предыдущего блока на 100 секунд или больше
     */
    public static CopyOnWriteArrayList<Block> getWinnerList() {
        return winnerList;
    }

    public static void setWinnerList(CopyOnWriteArrayList<Block> winnerList) {
        BasisController.winnerList = capBlockCandidates(winnerList, "winnerList");
    }

    public static boolean utilsMethod() throws IOException, NoSuchAlgorithmException, InvalidKeySpecException, SignatureException, NoSuchProviderException, InvalidKeyException {
        boolean result = false;
        if (shortDataBlockchain.getSize() == 0
                || !shortDataBlockchain.isValidation()
                || shortDataBlockchain.getHashCount() == 0
                || prevBlock == null) {

            shortDataBlockchain = Blockchain.checkFromFile(Seting.ORIGINAL_BLOCKCHAIN_FILE);


            blockcheinSize = (int) shortDataBlockchain.getSize();
            blockchainValid = shortDataBlockchain.isValidation();

            result = true;
        }
        return result;
    }

    /**
     * Если блок до и после ********* одинаковый, то файл последний на сервере не поврежден.
     * Так как первый блок отбирается из базы данных h2, а второй тот же блок из уже файла.
     */
    @GetMapping("/status")
    @ResponseBody
    public String status() {
        String result = "";
        try {
            String strIsSave = "isSave: " + isIsSave() + "\n";
            String strBlockchainSize = "blockchainSize: " + getBlockchainSize() + "\n";
            String isSaveFile = "isSaveFile: " + isSaveFile() + "\n";
            String blockFromDb =
                    "blockFromDb: " + String.valueOf(blockService.findBySpecialIndex(blockcheinSize - 1))
                            + "\n";
            String blockFromFile = "*********************************\nblockFromFile: " + Blockchain.indexFromFileBing(blockcheinSize - 1, Seting.ORIGINAL_BLOCKCHAIN_FILE)
                    + "\n";


            result = strIsSave + strBlockchainSize + isSaveFile + blockFromDb + blockFromFile;
            result += "prevBlock: " + BasisController.prevBlock() + "\n";
            result += "**********************************************\n";
            result += "getAllWiners: " + BasisController.getAllWiners().size() + "\n";
            result += "getBigRandomWiner: " + BasisController.getBigRandomWiner().size() + "\n";
            result += "getCountTransactionsWiner: " + BasisController.getCountTransactionsWiner().size() + "\n";
            result += "getWinnerList: " + BasisController.getWinnerList().size() + "\n";
            result += "getStakingWiners: " + BasisController.getStakingWiners().size() + "\n";
            result += "**********************************************\n";
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }


        return result;
    }

    public static boolean isIsSaveFile() {
        return isSaveFile;
    }

    public static void setIsSaveFile(boolean isSaveFile) {
        BasisController.isSaveFile = isSaveFile;
    }

    public static boolean isIsSave() {
        return isSave;
    }

    public static void setIsSave(boolean isSave) {
        BasisController.isSave = isSave;
    }

    public static long getDificultyOneBlock() {
        return dificultyOneBlock;
    }

    public static DataShortBlockchainInformation getShortDataBlockchain() {
        return shortDataBlockchain;
    }

    /**
     * Высота локального блокчейна
     */
    public static int getBlockchainSize() {
        return blockcheinSize;
    }

    public static void setBlockcheinSize(int blockcheinSize) {
        BasisController.blockcheinSize = blockcheinSize;
    }

    static void resetBlockSubmitDebugForTests() {
        lastReceivedBlockIndex = -1;
        lastReceivedBlockHash = "none";
        lastReceivedAt = "never";
        lastSubmitRemoteAddress = "none";
        lastValidationResult = "none";
        lastRejectReason = "none";
        lastAddedBlockIndex = -1;
        lastAddedBlockHash = "none";
        lastAddedAt = "never";
        lastWalletSubmitEndpoint = "none";
        lastServerException = "none";
        lastPrePersistPeerPropagationSkippedReason = "never";
        lastPrePersistPeerPropagationAttempted = false;
        lastPrePersistPeerPropagationSkippedAt = "never";
        lastResolveConflictReceivedBlockIndex = -1;
        lastResolveConflictQueuedBlockIndex = -1;
        lastResolveConflictBeforeTournamentMemoryUsed = -1;
        lastResolveConflictBeforeTournamentMemoryMax = -1;
        lastLocalPersistenceStartedAt = "never";
        lastLocalPersistenceFinishedAt = "never";
        lastLocalPersistenceResult = "none";
        selfPeerSkippedCount = 0;
        prePersistOutboundTasksBlockedCount = 0;
        BlockGrowthDiagnostics.clearForTests();
        PeerPropagationMonitor.resetForTests();
        UtilsResolving.resetResolveSyncDebugForTests();
        resetTournamentSnapshotsForTests();
    }

    static void recordReceivedBlockForTests(long index, String hash, String remoteAddress) {
        recordReceivedBlock(index, hash, remoteAddress, "/nodes/resolve_from_to_block");
    }

    static void recordValidationResultForTests(String result, String rejectReason) {
        recordValidationResult(result, rejectReason);
    }

    static void recordAddedBlockForTests(long index, String hash) {
        recordAddedBlock(index, hash);
    }

    public static void recordPersistedBlockFromTournament(Block block) {
        if (block == null) {
            return;
        }
        DiagnosticLogRetention.cleanupIfDue(block.getIndex());
        lastAddedBlockIndex = block.getIndex();
        lastAddedBlockHash = String.valueOf(block.getHashBlock());
        lastAddedAt = new Date().toString();
        lastValidationResult = "persisted";
        lastRejectReason = "none";
        lastLocalPersistenceFinishedAt = new Date().toString();
        lastLocalPersistenceResult = "persisted";
        BlockGrowthDiagnostics.localPersistenceFinished(block, blockcheinSize, "persisted");
        PeerPropagationMonitor.recordPersistedBlock(block.getIndex(), String.valueOf(block.getHashBlock()));
        System.out.println("SERVER_BLOCK_ADD_SUCCESS");
        System.out.println("SERVER_BLOCK_ADD_MODE=persisted_by_tournament");
        System.out.println("SERVER_BLOCK_ADD_SIZE_AFTER=" + blockcheinSize);
        System.out.println("SERVER_BLOCK_ADD_PERSISTED_INDEX=" + block.getIndex());
        System.out.println("SERVER_BLOCK_ADD_PERSISTED_HASH=" + block.getHashBlock());
    }

    public static void recordLocalPersistenceStartedFromTournament(Block block) {
        lastLocalPersistenceStartedAt = new Date().toString();
        lastLocalPersistenceResult = "started";
        BlockGrowthDiagnostics.localPersistenceStarted(block);
        if (block != null) {
            lastResolveConflictQueuedBlockIndex = block.getIndex();
        }
    }

    static Map<String, Object> blockSubmitStatusSnapshotForTests() {
        return blockSubmitStatusSnapshot();
    }

    private static void recordReceivedBlock(long index, String hash, String remoteAddress, String endpoint) {
        lastReceivedBlockIndex = index;
        lastResolveConflictReceivedBlockIndex = index;
        lastReceivedBlockHash = String.valueOf(hash);
        lastReceivedAt = new Date().toString();
        lastSubmitRemoteAddress = String.valueOf(remoteAddress);
        lastWalletSubmitEndpoint = endpoint;
        lastValidationResult = "received";
        lastRejectReason = "validation pending";
        lastServerException = "none";
    }

    private static void recordValidationResult(String result, String rejectReason) {
        lastValidationResult = result;
        lastRejectReason = rejectReason == null || rejectReason.isBlank() ? "none" : rejectReason;
        BlockGrowthDiagnostics.submitValidationFinished(result, lastRejectReason);
    }

    private static void recordAddedBlock(long index, String hash) {
        lastAddedBlockIndex = index;
        lastResolveConflictQueuedBlockIndex = index;
        lastAddedBlockHash = String.valueOf(hash);
        lastAddedAt = new Date().toString();
        lastValidationResult = "accepted/queued";
        lastRejectReason = "none";
    }

    static boolean isPrePersistPeerPropagationEnabled() {
        return Boolean.getBoolean("citu.prePersistPeerPropagation.enabled");
    }

    private static void recordPrePersistPeerPropagationSkipped(String reason) {
        Runtime runtime = Runtime.getRuntime();
        lastPrePersistPeerPropagationAttempted = false;
        lastPrePersistPeerPropagationSkippedReason = reason;
        lastPrePersistPeerPropagationSkippedAt = new Date().toString();
        lastResolveConflictBeforeTournamentMemoryUsed = runtime.totalMemory() - runtime.freeMemory();
        lastResolveConflictBeforeTournamentMemoryMax = runtime.maxMemory();
        prePersistOutboundTasksBlockedCount++;
        System.out.println("PRE_PERSIST_PEER_PROPAGATION_SKIPPED reason=" + reason);
    }

    public static void recordSelfPeerSkipped() {
        selfPeerSkippedCount++;
    }

    private void handlePrePersistPeerPropagation(List<Block> addlist) throws IOException, NoSuchAlgorithmException,
            InvalidKeySpecException, SignatureException, NoSuchProviderException, InvalidKeyException {
        if (!isPrePersistPeerPropagationEnabled()) {
            recordPrePersistPeerPropagationSkipped("disabled_by_default");
            return;
        }
        lastPrePersistPeerPropagationAttempted = true;
        utilsResolving.sendAllBlocksToStorage(addlist);
    }

    private static Map<String, Object> blockSubmitStatusSnapshot() {
        Runtime runtime = Runtime.getRuntime();
        Map<String, Object> status = new LinkedHashMap<>();
        status.put("currentSize", blockcheinSize);
        status.put("lastReceivedBlockIndex", lastReceivedBlockIndex);
        status.put("lastReceivedBlockHash", lastReceivedBlockHash);
        status.put("lastReceivedAt", lastReceivedAt);
        status.put("lastSubmitRemoteAddress", lastSubmitRemoteAddress);
        status.put("lastValidationResult", lastValidationResult);
        status.put("lastRejectReason", lastRejectReason);
        status.put("lastAddedBlockIndex", lastAddedBlockIndex);
        status.put("lastAddedBlockHash", lastAddedBlockHash);
        status.put("lastAddedAt", lastAddedAt);
        status.put("lastWalletSubmitEndpoint", lastWalletSubmitEndpoint);
        status.put("lastServerException", lastServerException);
        status.put("temporaryBlockQueueSize", winnerList == null ? 0 : winnerList.size());
        status.put("prePersistPeerPropagationEnabled", isPrePersistPeerPropagationEnabled());
        status.put("lastPrePersistPeerPropagationSkippedReason", lastPrePersistPeerPropagationSkippedReason);
        status.put("lastPrePersistPeerPropagationAttempted", lastPrePersistPeerPropagationAttempted);
        status.put("lastPrePersistPeerPropagationSkippedAt", lastPrePersistPeerPropagationSkippedAt);
        status.put("lastResolveConflictReceivedBlockIndex", lastResolveConflictReceivedBlockIndex);
        status.put("lastResolveConflictQueuedBlockIndex", lastResolveConflictQueuedBlockIndex);
        status.put("lastResolveConflictBeforeTournamentMemoryUsed", lastResolveConflictBeforeTournamentMemoryUsed);
        status.put("lastResolveConflictBeforeTournamentMemoryMax", lastResolveConflictBeforeTournamentMemoryMax);
        status.put("lastLocalPersistenceStartedAt", lastLocalPersistenceStartedAt);
        status.put("lastLocalPersistenceFinishedAt", lastLocalPersistenceFinishedAt);
        status.put("lastLocalPersistenceResult", lastLocalPersistenceResult);
        status.put("selfPeerSkippedCount", selfPeerSkippedCount);
        status.put("prePersistOutboundTasksBlockedCount", prePersistOutboundTasksBlockedCount);
        status.put("freeMemory", runtime.freeMemory());
        status.put("totalMemory", runtime.totalMemory());
        status.put("maxMemory", runtime.maxMemory());
        BlockGrowthDiagnostics.submitStatus().forEach(status::putIfAbsent);
        status.putAll(PeerPropagationMonitor.snapshot());
        status.putAll(UtilsResolving.resolveSyncDebugSnapshot());
        return status;
    }

    @GetMapping("/debug/block-submit/status")
    @ResponseBody
    public Map<String, Object> blockSubmitStatus() {
        return blockSubmitStatusSnapshot();
    }

    @GetMapping("/debug/block-submit/traces")
    @ResponseBody
    public Map<String, Object> blockSubmitTraces() {
        Map<String, Object> traces = new LinkedHashMap<>();
        traces.put("lastSubmitTraces", BlockGrowthDiagnostics.submitTraces());
        traces.put("lastAddBlock3Traces", BlockGrowthDiagnostics.addBlock3Traces());
        traces.put("status", BlockGrowthDiagnostics.submitStatus());
        return traces;
    }

    @GetMapping("/debug/tournament/traces")
    @ResponseBody
    public Map<String, Object> tournamentTraces() {
        Map<String, Object> traces = new LinkedHashMap<>();
        traces.put("lastTournamentTraces", BlockGrowthDiagnostics.tournamentTraces());
        traces.put("status", BlockGrowthDiagnostics.submitStatus());
        return traces;
    }

    @GetMapping("/debug/add-block/traces")
    @ResponseBody
    public Map<String, Object> addBlockTraces() {
        Map<String, Object> traces = new LinkedHashMap<>();
        traces.put("lastAddBlock3Traces", BlockGrowthDiagnostics.addBlock3Traces());
        traces.put("status", BlockGrowthDiagnostics.submitStatus());
        return traces;
    }

    @GetMapping("/debug/node/runtime")
    @ResponseBody
    public Map<String, Object> nodeRuntime() {
        Runtime runtime = Runtime.getRuntime();
        Map<String, Object> status = new LinkedHashMap<>();
        status.put("nodeVersion", Optional.ofNullable(BasisController.class.getPackage().getImplementationVersion()).orElse("development"));
        status.put("processId", ProcessHandle.current().pid());
        status.put("startTime", new Date(ManagementFactory.getRuntimeMXBean().getStartTime()).toString());
        status.put("uptimeMs", ManagementFactory.getRuntimeMXBean().getUptime());
        status.put("uptimeSeconds", ManagementFactory.getRuntimeMXBean().getUptime() / 1000L);
        status.put("javaVersion", System.getProperty("java.version"));
        status.put("serverPort", System.getProperty("server.port", System.getenv().getOrDefault("SERVER_PORT", "82")));
        status.put("serverAddress", System.getProperty("server.address", System.getenv().getOrDefault("SERVER_ADDRESS", "localhost")));
        status.put("activeProfiles", System.getProperty("spring.profiles.active", "default"));
        status.put("maxMemory", runtime.maxMemory());
        status.put("usedMemory", runtime.totalMemory() - runtime.freeMemory());
        status.put("prePersistPeerPropagationEnabled", isPrePersistPeerPropagationEnabled());
        status.put("memoryForensicsEnabled", NodeMemoryProfiler.isEnabled());
        status.put("h2DatabasePath", Seting.H2_DB + "db");
        status.put("h2DatabaseOpen", "unknown");
        status.put("h2LockStatus", "unknown");
        status.put("currentChainSize", blockcheinSize);
        status.put("latestBlockIndex", prevBlock == null ? -1 : prevBlock.getIndex());
        status.put("v35ForkIndex", Seting.V35_RESCUE_FORK_INDEX);
        status.put("burnAddressPlaceholder", "BURN_ADDRESS".equals(Seting.V35_BURN_ADDRESS));
        return status;
    }

    @GetMapping("/debug/memory/status")
    @ResponseBody
    public Map<String, Object> memoryStatus() {
        return memoryStatusSnapshot(allTransactions, poolBlockService);
    }

    @GetMapping("/debug/memory/live-state")
    @ResponseBody
    public Map<String, Object> memoryLiveState() {
        return memoryLiveStateSnapshot(allTransactions, poolBlockService);
    }

    @GetMapping("/debug/peer/health")
    @ResponseBody
    public Map<String, Object> peerHealth() {
        return PeerHealthRegistry.snapshot();
    }

    @GetMapping("/debug/peers/self")
    @ResponseBody
    public Map<String, Object> selfPeerDiagnostics() {
        return PeerHealthRegistry.selfPeerSnapshot();
    }

    @GetMapping("/debug/peer/connections")
    @ResponseBody
    public Map<String, Object> peerConnectionDiagnostics() {
        return PeerHealthRegistry.connectionSnapshot();
    }

    @GetMapping("/debug/memory/forensics")
    @ResponseBody
    public Map<String, Object> memoryForensics() {
        Map<String, Object> snapshot = new LinkedHashMap<>(NodeMemoryProfiler.snapshot());
        snapshot.putAll(BlockGrowthDiagnostics.memoryForensicsFields());
        return snapshot;
    }

    static Map<String, Object> memoryStatusSnapshotForTests() {
        return memoryStatusSnapshot(null, null);
    }

    static Map<String, Object> memoryLiveStateSnapshotForTests() {
        return memoryLiveStateSnapshot(null, null);
    }

    private static Map<String, Object> memoryStatusSnapshot(AllTransactions allTransactions, PoolBlockService poolBlockService) {
        Runtime runtime = Runtime.getRuntime();
        long totalMemory = runtime.totalMemory();
        long freeMemory = runtime.freeMemory();
        long usedMemory = totalMemory - freeMemory;
        Map<String, Object> status = new LinkedHashMap<>();
        status.put("currentChainSize", blockcheinSize);
        status.put("freeMemory", freeMemory);
        status.put("totalMemory", totalMemory);
        status.put("maxMemory", runtime.maxMemory());
        status.put("usedMemory", usedMemory);
        status.put("winnerListSize", winnerList == null ? 0 : winnerList.size());
        status.put("temporaryBlocksSize", winnerList == null ? 0 : winnerList.size());
        status.put("pendingSubmittedBlocksSize", winnerList == null ? 0 : winnerList.size());
        status.put("temporarySubmittedBlocksCap", MAX_TEMPORARY_SUBMITTED_BLOCKS);
        status.put("poolBlockMapSize", poolBlockService == null ? "unavailable" : poolBlockService.size());
        status.put("pendingTxMapSize", allTransactions == null ? "unavailable" : allTransactions.pendingSize());
        status.put("activePeerCount", getNodes().size());
        status.put("unreachablePeerCount", getExcludedAddresses().size());
        status.put("lastTournamentStartTime", TournamentService.getLastTournamentStartMillis());
        status.put("lastTournamentEndTime", TournamentService.getLastTournamentEndMillis());
        status.put("lastTournamentDuration", TournamentService.getLastTournamentDurationMillis());
        status.put("lastResolve3StartTime", UtilsResolving.getLastResolve3StartMillis());
        status.put("lastResolve3EndTime", UtilsResolving.getLastResolve3EndMillis());
        status.put("lastResolve3Duration", UtilsResolving.getLastResolve3DurationMillis());
        status.put("trackedTournamentPeerErrors", TournamentService.getTrackedPeerErrorCount());
        status.put("trackedResolvePeerErrors", UtilsResolving.getTrackedPeerErrorCount());
        status.put("lastOomRiskWarning", TournamentService.getLastOomRiskWarning());
        status.putAll(NodeMemoryProfiler.memoryStatusFields());
        status.putAll(PeerPropagationMonitor.snapshot());
        status.putAll(PeerHealthRegistry.memoryStatusFields());
        PeerHealthRegistry.maybeLogHealthSummary(
                blockcheinSize,
                prevBlock == null ? -1 : prevBlock.getIndex(),
                TournamentService.getLastTournamentDurationMillis(),
                String.valueOf(BlockGrowthDiagnostics.submitStatus().get("lastNoGrowthReason")),
                longValue(BlockGrowthDiagnostics.submitStatus().get("lastAddedBlockIndex")));
        return status;
    }

    private static Map<String, Object> memoryLiveStateSnapshot(AllTransactions allTransactions, PoolBlockService poolBlockService) {
        Runtime runtime = Runtime.getRuntime();
        long totalMemory = runtime.totalMemory();
        long freeMemory = runtime.freeMemory();
        long usedMemory = totalMemory - freeMemory;
        TournamentSnapshot snapshot = lastTournamentSnapshot;
        int snapshotCandidateCount = snapshot == null || snapshot.getCandidates() == null ? 0 : snapshot.getCandidates().size();
        Map<String, Object> status = new LinkedHashMap<>();
        status.put("currentBlockchainSize", blockcheinSize);
        status.put("winnerListSize", winnerList == null ? 0 : winnerList.size());
        status.put("allWinnersSize", allWiners == null ? 0 : allWiners.size());
        status.put("currentCandidatesSize", winnerList == null ? 0 : Math.min(winnerList.size(), MAX_TOURNAMENT_CANDIDATE_SNAPSHOT_SIZE));
        status.put("lastTournamentSnapshotCandidateCount", snapshotCandidateCount);
        synchronized (BasisController.class) {
            status.put("lastTournamentSnapshotsCount", lastTournamentSnapshots.size());
        }
        status.put("addListSize", "not_retained_after_request");
        status.put("transactionPoolSize", allTransactions == null ? "unavailable" : allTransactions.pendingSize());
        status.put("pendingTransactionsCount", allTransactions == null ? "unavailable" : allTransactions.pendingSize());
        status.put("cachedBalancesCount", balances == null ? 0 : balances.size());
        status.put("blockedHostsCount", getExcludedAddresses().size());
        status.put("knownHostsCount", getNodes().size());
        status.put("poolBlockMapSize", poolBlockService == null ? "unavailable" : poolBlockService.size());
        status.put("currentThreadCount", ManagementFactory.getThreadMXBean().getThreadCount());
        status.put("usedMemory", usedMemory);
        status.put("maxMemory", runtime.maxMemory());
        status.put("lastMemoryProbeStage", NodeMemoryProfiler.lastStageName());
        status.put("winnerList", collectionInfo("winnerList", winnerList == null ? 0 : winnerList.size(), MAX_TEMPORARY_SUBMITTED_BLOCKS, winnerListOldestAgeMillis(), winnerListNewestAgeMillis(), 512));
        status.put("allWinners", collectionInfo("allWinners", allWiners == null ? 0 : allWiners.size(), MAX_TOURNAMENT_CANDIDATE_SNAPSHOT_SIZE, -1, -1, 256));
        status.put("currentCandidates", collectionInfo("currentCandidates", winnerList == null ? 0 : Math.min(winnerList.size(), MAX_TOURNAMENT_CANDIDATE_SNAPSHOT_SIZE), MAX_TOURNAMENT_CANDIDATE_SNAPSHOT_SIZE, winnerListOldestAgeMillis(), winnerListNewestAgeMillis(), 512));
        status.put("lastTournamentSnapshotCandidates", collectionInfo("lastTournamentSnapshotCandidates", snapshotCandidateCount, MAX_TOURNAMENT_CANDIDATE_SNAPSHOT_SIZE, -1, -1, 256));
        synchronized (BasisController.class) {
            status.put("lastTournamentSnapshots", collectionInfo("lastTournamentSnapshots", lastTournamentSnapshots.size(), MAX_PUBLIC_TOURNAMENT_SNAPSHOTS, -1, -1, 1024));
        }
        status.putAll(PeerConnectionDiagnostics.snapshot());
        status.putAll(PeerHealthRegistry.connectionSnapshot());
        return status;
    }

    private static Map<String, Object> collectionInfo(String collectionName,
                                                      int size,
                                                      int maxSize,
                                                      long oldestEntryAgeMillis,
                                                      long newestEntryAgeMillis,
                                                      int estimatedBytesPerEntry) {
        Map<String, Object> info = new LinkedHashMap<>();
        info.put("collectionName", collectionName);
        info.put("size", Math.max(0, size));
        info.put("maxSize", Math.max(0, maxSize));
        info.put("estimatedBytes", Math.max(0L, (long) Math.max(0, size) * estimatedBytesPerEntry));
        info.put("oldestEntryAgeMillis", oldestEntryAgeMillis);
        info.put("newestEntryAgeMillis", newestEntryAgeMillis);
        return info;
    }

    private static long winnerListOldestAgeMillis() {
        CopyOnWriteArrayList<Block> current = winnerList;
        if (current == null || current.isEmpty()) {
            return -1L;
        }
        long now = System.currentTimeMillis();
        return current.stream()
                .filter(Objects::nonNull)
                .map(Block::getTimestamp)
                .filter(Objects::nonNull)
                .mapToLong(timestamp -> Math.max(0L, now - timestamp.getTime()))
                .max()
                .orElse(-1L);
    }

    private static long winnerListNewestAgeMillis() {
        CopyOnWriteArrayList<Block> current = winnerList;
        if (current == null || current.isEmpty()) {
            return -1L;
        }
        long now = System.currentTimeMillis();
        return current.stream()
                .filter(Objects::nonNull)
                .map(Block::getTimestamp)
                .filter(Objects::nonNull)
                .mapToLong(timestamp -> Math.max(0L, now - timestamp.getTime()))
                .min()
                .orElse(-1L);
    }

    private static long longValue(Object value) {
        if (value instanceof Number) {
            return ((Number) value).longValue();
        }
        try {
            return Long.parseLong(String.valueOf(value));
        } catch (Exception ignored) {
            return -1L;
        }
    }

    public static HttpServletRequest getCurrentRequest() {
        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
        if (requestAttributes == null)
            return null;
        Assert.state(requestAttributes != null, "Could not find current request via RequestContextHolder");
        Assert.isInstanceOf(ServletRequestAttributes.class, requestAttributes);
        HttpServletRequest servletRequest = ((ServletRequestAttributes) requestAttributes).getRequest();
        Assert.state(servletRequest != null, "Could not find current HttpServletRequest");
        return servletRequest;
    }

    /**
     * Возвращает список балансов
     */

    @GetMapping("/v28Index")
    public int v28Start() {
        return Seting.V28_CHANGE_ALGORITH_DIFF_INDEX;
    }

    /**
     * Общее количество всех балансов.
     */
    @GetMapping("/allAccounts")
    public long accounts() {
        if (!isSaveFile) {
            System.out.println("saving file: resolve_from_to_block: sub block");
            return -1;
        }
        return blockService.countAccount();
    }

    /**
     * Общее количество долларов в обороте, не находящиеся в стэйкинге.
     */
    @GetMapping("/totalDollars")
    public double getTotalDollars() {
        try {

            if (totalDollars == 0) {
                BasisController.setTotalDollars(blockService.getTotalDigitalDollarBalance());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return totalDollars;
    }
    @GetMapping("/totalStaking")
    public double getTotalStaking() {
        try {

            if (totalStaking == 0) {
                BasisController.setTotalStaking(blockService.getTotalDigitalStakingBalance());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return totalStaking;
    }

    @GetMapping("/totalAll")
    public double getTotalAll() {
        try {

            if (totalAll == 0) {
                BasisController.setTotalAll(blockService.getTotalDigitalAllBalance());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return totalAll;
    }
    /**
     * Общее количество транзакций совершенных в день
     */
    @GetMapping("/totalTransactionsDay")
    public int getTotalTransactionsDays() {
        return totalTransactionsDays;
    }

    /**
     * Общее количество денег переведенных в день.
     */
    @GetMapping("/totalTransactionsSum")
    public double getTotalTransactionsSumDllar() {
        return totalTransactionsSumDllar;
    }

    /**
     * Мультипликатор, который определяет какой максимальный доход может быть добыт в этом году,
     * каждый год он уменьшается, но не будет ниже единицы. Аналог халвинга для биткоина, но более
     * плавный. В первый год начал работать с 29
     */
    @GetMapping("/multiplier")
    public long multiplier() {
        long index = prevBlock.getIndex();
        int day = 576;
        int period = YEAR;
        int mulptipleperiod = MULTIPLIER;
        if (index > OPTIMAL_SCORE_INDEX) {
            day = 432;
            period = 120;
            mulptipleperiod = MULTIPLIER2;
        }

        long money = (index - Seting.V28_CHANGE_ALGORITH_DIFF_INDEX)
                / (day * period);
        money = (long) (mulptipleperiod - money);
        money = money < 1 ? 1 : money;
        return money;
    }


    /**
     * Обратный отсчет для снижения мультипликатора, вычисляет сколько дней осталось до
     * следующего снижения
     */
    @GetMapping("/dayReduce")
    public long daysReduce() {


        long index = prevBlock.getIndex();
        int day = 576;
        int period = YEAR;
        int mulptipleperiod = MULTIPLIER;
        if (index > OPTIMAL_SCORE_INDEX) {
            day = 432;
            period = 120;
            mulptipleperiod = MULTIPLIER2;
        }

        long money = (index - Seting.V28_CHANGE_ALGORITH_DIFF_INDEX)
                / (day * period);
        money = (long) (mulptipleperiod - money);
        money = money < 1 ? 1 : money;
        long reduceDays = 0;
        if (prevBlock.getIndex() > Seting.V28_CHANGE_ALGORITH_DIFF_INDEX) {
            int blocksSinceReduction = (int) ((prevBlock.getIndex() - Seting.V28_CHANGE_ALGORITH_DIFF_INDEX)
                    % (day * period));

            // Оставшиеся блоки до следующего снижения
            reduceDays = (day * period) - blocksSinceReduction;

        }
        return reduceDays;
    }

    public static Set<String> getExcludedAddresses() {
        HttpServletRequest request = getCurrentRequest();
        if (request == null)
            return excludedAddresses;

        String scheme = request.getScheme();
        String serverName = request.getServerName();
        int serverPort = request.getServerPort();
        String contextPath = request.getContextPath();  // includes leading forward slash

        String localaddress = scheme + "://" + serverName + ":" + serverPort;

        excludedAddresses.add(localaddress);
        return excludedAddresses;
    }

    public static void setExcludedAddresses(Set<String> excludedAddresses) {
        BasisController.excludedAddresses = excludedAddresses;
    }


    /**
     * Возвращает список хостов
     */
    public static Set<String> getNodes() {


        Set<String> nodes = new HashSet<>();


        try {

            Set<String> temporary = UtilsAllAddresses.readLineObject(Seting.ORIGINAL_POOL_URL_ADDRESS_FILE);

            nodes.addAll(temporary);

            nodes = nodes.stream()
                    .filter(t -> !t.isBlank())
                    .map(t -> t.replaceAll("\"", ""))
                    .filter(BasisController::isValidUrl) // фильтрация некорректных URL
                    .collect(Collectors.toSet());

            Set<String> bloked = UtilsAllAddresses.readLineObject(Seting.ORIGINAL_POOL_URL_ADDRESS_BLOCKED_FILE);
            nodes.removeAll(bloked);
            nodes.removeAll(Seting.ORIGINAL_BLOCKED_ADDRESS);
            nodes.addAll(Seting.ORIGINAL_ADDRESSES);

            System.out.println("standard nodes: " + nodes);
        } catch (Exception e) {
            e.printStackTrace();
        }


        return nodes;
    }

    private static boolean isValidUrl(String url) {
        try {
            URL netUrl = new URL(url);
            netUrl.toURI();

            // Проверяем, что хост не 0.0.0.0
            String host = netUrl.getHost();
            if (host.equals("0.0.0.0")) {
                return false;
            }
            return true;
        } catch (Exception e) {
            System.err.println("Invalid URL: " + url + " - " + e.getMessage());
            return false;
        }
    }

    @GetMapping("/getNodes")
    @ResponseBody
    public Set<String> getAllNodes() {


        Set<String> nodes = new HashSet<>();

        try {
            Set<String> temporary = UtilsAllAddresses.readLineObject(Seting.ORIGINAL_POOL_URL_ADDRESS_FILE);

            nodes.addAll((new HashSet<>(temporary)));

            nodes = nodes.stream()
                    .filter(t -> !t.isBlank())
                    .map(t -> t.replaceAll("\"", ""))
                    .filter(BasisController::isValidUrl)
                    .collect(Collectors.toSet());


            Set<String> bloked = UtilsAllAddresses.readLineObject(Seting.ORIGINAL_POOL_URL_ADDRESS_BLOCKED_FILE);
            nodes.removeAll((new HashSet<>(bloked)));
            nodes.removeAll(Seting.ORIGINAL_BLOCKED_ADDRESS);
            nodes.addAll((new HashSet<>(Seting.ORIGINAL_ADDRESSES)));

            System.out.println("nodes: " + nodes);
        } catch (Exception e) {
            e.printStackTrace();
        }


        return nodes;
    }


    public BasisController() {
    }

    //TODO если вы прервали mine, то перед следующим вызовом перезапустите сервер и вызовите /addBlock перед mine
    //TODO if you interrupted mine, restart the server before next call and call /addBlock before mine
    //TODO иначе будет расождение в файле балансов
    //TODO otherwise there will be a discrepancy in the balance file


    /**
     * возвращяет размер локального блокчейна
     */
    @GetMapping("/size")
    @ResponseBody
    public Integer sizeBlockchain() throws IOException, NoSuchAlgorithmException, InvalidKeySpecException, SignatureException, NoSuchProviderException, InvalidKeyException, InterruptedException, CloneNotSupportedException {
//        System.out.println("start /size");


        if (blockcheinSize == 0) {
            System.out.println("blockchain is 0 blockchainSize " + blockcheinSize);
//
            shortDataBlockchain = Blockchain.checkFromFile(Seting.ORIGINAL_BLOCKCHAIN_FILE);
            blockcheinSize = (int) shortDataBlockchain.getSize();
            blockchainValid = shortDataBlockchain.isValidation();
            prevBlock = blockcheinSize == 0
                    ? null
                    : Blockchain.indexFromFile(blockcheinSize - 1, Seting.ORIGINAL_BLOCKCHAIN_FILE);

        }


        if (blockchainValid == false) {
            System.out.println("/size blockchain not valid: " + blockchainValid);

//            UtilsBlock.deleteFiles();
//            return 1;
            System.exit(0);
        }
//        System.out.println("finish /size");
        return blockcheinSize; //blockchain.sizeBlockhain();
    }

    /**
     * Возвращает список блоков ОТ до ДО,
     */
    @PostMapping("/sub-blocks")
    @ResponseBody
    public List<Block> subBlocks(@RequestBody SubBlockchainEntity entity) {
        List<Block> blocksDb = new ArrayList<>();
        try {
            System.out.println("************************************");
            System.out.println("subBlocks");
            System.out.println("valid: " + blockchainValid);
            System.out.println("size:" + blockcheinSize);


            if (blockchainValid == false || blockcheinSize == 0) {

                shortDataBlockchain = Blockchain.checkFromFile(Seting.ORIGINAL_BLOCKCHAIN_FILE);
                blockcheinSize = (int) shortDataBlockchain.getSize();
                blockchainValid = shortDataBlockchain.isValidation();
            }


            int start = entity.getStart() >= 1 ? entity.getStart() : 0;
            int finish = entity.getFinish() > start ? entity.getFinish() : blockcheinSize - 1;
            if (start >= blockcheinSize || finish > blockcheinSize) {

                System.out.println("start >= blockcheinSize || finish > blockcheinSize");
                return new ArrayList<>();
            }

            //проверить на ограничение порций
            if (finish - start > Seting.PORTION_BLOCK_TO_COMPLEXCITY) {
                if (finish - start < blockcheinSize) {
                    finish = start + Seting.PORTION_BLOCK_TO_COMPLEXCITY;
                } else {
                    finish = blockcheinSize - 1;
                }
            }


            List<EntityBlock> entityBlocks =
                    blockService.findBySpecialIndexBetween(start, finish);
            blocksDb = UtilsBlockToEntityBlock.entityBlocksToBlocks(entityBlocks);

//        return Blockchain.subFromFileBing(start,finish, Seting.ORIGINAL_BLOCKCHAIN_FILE);
            System.out.println("finish sub: " + finish);
            System.out.println("******************************************************");
        } catch (IOException e) {
            System.out.println("exception sub");
            return new ArrayList<>();

        } finally {
            return blocksDb;
        }

    }


    @GetMapping("/version")
    @ResponseBody
    public double version() {
        return Seting.VERSION;
    }


    /**
     * Возвращяет блок по индексу
     */

    @PostMapping("/block")
    @ResponseBody
    @Async("threadPoolTaskExecutor")
    public Block getBlock(@RequestBody Integer index) {
        try {
//        System.out.println("start getBlock");
            if (blockchainValid == false || blockcheinSize == 0) {
//            blockchain = Mining.getBlockchain(
//                    Seting.ORIGINAL_BLOCKCHAIN_FILE,
//                    BlockchainFactoryEnum.ORIGINAL);
                shortDataBlockchain = Blockchain.checkFromFile(Seting.ORIGINAL_BLOCKCHAIN_FILE);
                blockcheinSize = (int) shortDataBlockchain.getSize();
                blockchainValid = shortDataBlockchain.isValidation();
            }
//        System.out.println("finish getBlock");
//        return Blockchain.indexFromFileBing(index, Seting.ORIGINAL_BLOCKCHAIN_FILE);

            if (index < 0) {
                index = 0;
            }
            if (index > blockcheinSize - 1) {
                index = blockcheinSize - 1;
            }
            return UtilsBlockToEntityBlock.entityBlockToBlock(
                    blockService.findBySpecialIndex(index)
            );
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }


    @GetMapping("/isSaveFile")
    @ResponseBody
    public boolean isSaveFile() {
        return isSaveFile;
    }

    /**
     * возвращает баланс аккаунта
     */
    @GetMapping("/balance")
    @ResponseBody
    public Account getBalance(@RequestParam String address) {
        try {
            return UtilsAccountToEntityAccount.entityAccountToAccount(blockService.findByAccount(address));

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }


    @GetMapping("/prevBlock")
    @ResponseBody
    public Block getPrevBlock() {
        return prevBlock;
    }


    /**
     * метод добавляет блоки в список ожидания, после чего их них уже формируется кандидаты и победитель
     */
    @PostMapping("/nodes/resolve_from_to_block")
    public ResponseEntity<String> resolve_conflict(@RequestBody SendBlocksEndInfo sendBlocksEndInfo,
                                                   HttpServletRequest request) {
        Long profilerBlockIndex = null;
        int profilerBlockCount = 0;
        if (sendBlocksEndInfo != null && sendBlocksEndInfo.getList() != null && !sendBlocksEndInfo.getList().isEmpty()) {
            profilerBlockCount = sendBlocksEndInfo.getList().size();
            profilerBlockIndex = sendBlocksEndInfo.getList().get(0).getIndex();
        }
        NodeMemoryProfiler.beginTrace("resolve_conflict start", profilerBlockIndex);
        NodeMemoryProfiler.recordStage("resolve_conflict request received", NodeMemoryProfiler.metrics(
                "candidateBlockListSize", profilerBlockCount,
                "winnerListSize", winnerList == null ? 0 : winnerList.size(),
                "remoteAddress", request == null ? "unknown" : request.getRemoteAddr()
        ));

        try {

            if (!blockedNewSendBlock.get()) {
                recordValidationResult("rejected", "START SYNCHORNIZATION: blockedNewSendBlock=false");
                return new ResponseEntity<>("START SYNCHORNIZATION", HttpStatus.OK);
            }
            if (UtilsBalance.getBlockService() == null)
                UtilsBalance.setBlockService(blockService);
            if (Blockchain.getBlockService() == null)
                Blockchain.setBlockService(blockService);
            if (UtilsBlock.getBlockService() == null)
                UtilsBlock.setBlockService(blockService);


            System.out.println("start resolve_from_to_block: " + sendBlocksEndInfo.getList().get(0).getMinerAddress());
            System.out.println("isSave: " + isSaveFile);


            if (sendBlocksEndInfo.getVersion() != Seting.VERSION) {
                System.out.println("wrong version version " + Seting.VERSION + " but: " + sendBlocksEndInfo.getVersion());
                recordValidationResult("rejected", "wrong version");
                return new ResponseEntity<>("FALSE", HttpStatus.FAILED_DEPENDENCY);
            }

            List<Block> blocks = sendBlocksEndInfo.getList();
            Block receivedBlock = blocks == null || blocks.isEmpty() ? null : blocks.get(0);
            NodeMemoryProfiler.recordStage("resolve_conflict after request body/block parsed", NodeMemoryProfiler.metrics(
                    "candidateBlockListSize", blocks == null ? 0 : blocks.size(),
                    "txCount", receivedBlock == null || receivedBlock.getDtoTransactions() == null ? 0 : receivedBlock.getDtoTransactions().size(),
                    "blockIndex", receivedBlock == null ? null : receivedBlock.getIndex()
            ));
            if (receivedBlock != null) {
                recordReceivedBlock(
                        receivedBlock.getIndex(),
                        receivedBlock.getHashBlock(),
                        request == null ? "unknown" : request.getRemoteAddr(),
                        "/nodes/resolve_from_to_block");
                String blockGrowthTraceId = BlockGrowthDiagnostics.startSubmit(
                        "/nodes/resolve_from_to_block",
                        request == null ? "unknown" : request.getRemoteAddr(),
                        request == null ? -1L : request.getContentLengthLong(),
                        receivedBlock,
                        blockcheinSize,
                        winnerList == null ? 0 : winnerList.size());
                BlockGrowthDiagnostics.submitValidationStarted(blockGrowthTraceId);
                System.out.println("SERVER_BLOCK_RECEIVED");
                System.out.println("SERVER_BLOCK_RECEIVED_ENDPOINT=/nodes/resolve_from_to_block");
                System.out.println("SERVER_BLOCK_RECEIVED_REMOTE=" + (request == null ? "unknown" : request.getRemoteAddr()));
                System.out.println("SERVER_BLOCK_RECEIVED_INDEX=" + receivedBlock.getIndex());
                System.out.println("SERVER_BLOCK_RECEIVED_HASH=" + receivedBlock.getHashBlock());
                System.out.println("SERVER_BLOCK_RECEIVED_PREVIOUS_HASH=" + receivedBlock.getPreviousHash());
                System.out.println("SERVER_BLOCK_RECEIVED_MINER=" + receivedBlock.getMinerAddress());
                System.out.println("SERVER_BLOCK_RECEIVED_TX_COUNT=" + (receivedBlock.getDtoTransactions() == null ? 0 : receivedBlock.getDtoTransactions().size()));
                System.out.println("SERVER_BLOCK_RECEIVED_SIZE_BEFORE=" + blockcheinSize);
                System.out.println("SERVER_BLOCK_RECEIVED_V35_ACTIVE=" + (blockcheinSize >= Seting.V35_RESCUE_FORK_INDEX));
            }
            ///последовательность временных меток
            if (prevBlock.getTimestamp().getTime() > blocks.get(blocks.size() - 1).getTimestamp().getTime()) {
                System.out.println("wrong time: prev uper now");
                recordValidationResult("rejected", "candidate timestamp is below previous block timestamp");
                return new ResponseEntity<>("FALSE", HttpStatus.EXPECTATION_FAILED);
            }
            String addressMiner = null;
            if (blocks.get(blocks.size() - 1).getMinerAddress() != null && !blocks.get(blocks.size() - 1).getMinerAddress().isEmpty()) {
                addressMiner = blocks.get(blocks.size() - 1).getMinerAddress();

            } else {
                System.out.println("wrong: empty address;");
                recordValidationResult("rejected", "empty miner address");
                return new ResponseEntity<>("FALSE", HttpStatus.EXPECTATION_FAILED);
            }

            try {

                List<Block> addlist = Blockchain.clone(0, blocks.size(), blocks);

                if (addlist.isEmpty()
                        || addlist.get(0).getIndex() != BasisController.getBlockchainSize()
                        || winnerList.contains(addlist.get(0))) {
                    System.out.println("this block has in system: ");
                    recordValidationResult("rejected", "empty list, wrong next index, or duplicate winner candidate");
                    return new ResponseEntity<>("FALSE", HttpStatus.OK);
                }
                System.out.println("account: " + addressMiner);


                Timestamp actualTime = new Timestamp(UtilsTime.getUniversalTimestamp());
                Timestamp lastIndex = addlist.get(addlist.size() - 1).getTimestamp();

                Long result = actualTime.toInstant().until(lastIndex.toInstant(), ChronoUnit.MINUTES);
                System.out.println("different time: " + result);


                if (prevBlock == null) {
                    EntityBlock tempBlock = blockService.findBySpecialIndex(blockcheinSize - 1);
                    prevBlock = UtilsBlockToEntityBlock.entityBlockToBlock(tempBlock);
                }
                if (shortDataBlockchain.getSize() == 0
                        || !shortDataBlockchain.isValidation()
                        || shortDataBlockchain.getHashCount() == 0) {
                    shortDataBlockchain = Blockchain.checkFromFile(Seting.ORIGINAL_BLOCKCHAIN_FILE);
                }
                List<Block> lastDiff = new ArrayList<>();


                //удаление транзакций
                if (prevBlock.getIndex() % 288 == 0)
                    Mining.deleteFiles(Seting.ORGINAL_ALL_TRANSACTION_FILE);
                if (prevBlock.getIndex() % 288 == 0)
                    Mining.deleteFiles(Seting.ORIGINAL_ALL_SENDED_TRANSACTION_FILE);

                List<String> sign = new ArrayList<>();

                Block checkBlock = addlist.get(0);
                long timeDifferenceSeconds = (checkBlock.getTimestamp().getTime() - prevBlock().getTimestamp().getTime()) / 1000;
                long actualTimeCheck = UtilsTime.getUniversalTimestamp() / 1000L;
                System.out.println("SERVER_BLOCK_VALIDATE_START");
                System.out.println("SERVER_BLOCK_VALIDATE_EXPECTED_SIZE=" + blockcheinSize);
                System.out.println("SERVER_BLOCK_VALIDATE_EXPECTED_PREVIOUS_HASH=" + prevBlock.getHashBlock());
                System.out.println("SERVER_BLOCK_VALIDATE_ACTUAL_PREVIOUS_HASH=" + checkBlock.getPreviousHash());
                if (!checkBlock.getPreviousHash().equals(prevBlock.getHashBlock())
                        || isResolveBlockBelowMinimumDifficulty(checkBlock)
                        || timeDifferenceSeconds < 100
                        || timeDifferenceSeconds > actualTimeCheck
                        || !checkBlock.getHashBlock().equals(checkBlock.hashForTransaction())) {
                    String reason = "previousHashMismatch=" + !checkBlock.getPreviousHash().equals(prevBlock.getHashBlock())
                            + ", belowMinimumDifficulty=" + isResolveBlockBelowMinimumDifficulty(checkBlock)
                            + ", timeBelow100s=" + (timeDifferenceSeconds < 100)
                            + ", futureTimestamp=" + (timeDifferenceSeconds > actualTimeCheck)
                            + ", hashMismatch=" + (!checkBlock.getHashBlock().equals(checkBlock.hashForTransaction()));
                    System.out.println("wrong block: resolve_from_to_block: "
                            + !checkBlock.getPreviousHash().equals(prevBlock.getHashBlock() + ":" +
                            isResolveBlockBelowMinimumDifficulty(checkBlock)) + ":" +
                            (timeDifferenceSeconds < 100) + ":" +
                            (timeDifferenceSeconds > actualTimeCheck) + ":" +
                            (!checkBlock.getHashBlock().equals(checkBlock.hashForTransaction())));
                    System.out.println("SERVER_BLOCK_ADD_REJECTED=" + reason);
                    recordValidationResult("rejected", reason);
                    return new ResponseEntity<>("FALSE", HttpStatus.EXPECTATION_FAILED);
                }

                Map<String, Account> tempBalances = UtilsAccountToEntityAccount.entityAccountsToMapAccounts(UtilsUse.accounts(addlist, blockService));

                DataShortBlockchainInformation temp = Blockchain.shortCheckWithCanonicalContext(
                        prevBlock,
                        addlist,
                        shortDataBlockchain,
                        lastDiff,
                        tempBalances,
                        sign,
                        UtilsUse.balancesClone(tempBalances),
                        new ArrayList<>(),
                        v35CanonicalContextBefore(prevBlock),
                        Seting.V35_RESCUE_FORK_INDEX);// Blockchain.checkEqualsFromToBlockFile(Seting.ORIGINAL_BLOCKCHAIN_FILE, addlist);

                System.out.println("<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<");
                System.out.println("original: " + shortDataBlockchain);
                System.out.println("temp: " + temp);


                System.out.println("address mininer: " + blocks.get(blocks.size() - 1).getMinerAddress());
                System.out.println("<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<");
                System.out.println("addList size: " + addlist.size());
                if (blockcheinSize == 0 || blockchainValid == false) {

                    shortDataBlockchain = Blockchain.checkFromFile(Seting.ORIGINAL_BLOCKCHAIN_FILE);
                    blockcheinSize = (int) shortDataBlockchain.getSize();
                    blockchainValid = shortDataBlockchain.isValidation();
//                    prevBlock = Blockchain.indexFromFileBing(blockcheinSize - 1, Seting.ORIGINAL_BLOCKCHAIN_FILE);
                    EntityBlock tempBlock = blockService.findBySpecialIndex(blockcheinSize - 1);
                    prevBlock = UtilsBlockToEntityBlock.entityBlockToBlock(tempBlock);
                    String json = UtilsJson.objToStringJson(shortDataBlockchain);
                    UtilsFileSaveRead.save(json, Seting.TEMPORARY_BLOCKCHAIN_FILE, false);
                }

                if (!shortDataBlockchain.isValidation()) {
                    System.out.println("wrong block chain, delete blocks: from to block:");
                    recordValidationResult("rejected", "local shortDataBlockchain invalid");
                    System.exit(0);
                    return new ResponseEntity<>("please retry  wrong blockchain in storage", HttpStatus.CONFLICT);
                }


                if (temp.isValidation()) {
                    System.out.println("from to block is valid");
                    System.out.println("SERVER_BLOCK_VALIDATE_RESULT=true");
                    recordValidationResult("valid", "candidate passed shortCheckWithCanonicalContext");

                } else {
                    if (temp.getSize() > shortDataBlockchain.getSize()) {
                        System.out.println("code error: " + HttpStatus.CONFLICT);
                        System.out.println("SERVER_BLOCK_VALIDATE_RESULT=false");
                        recordValidationResult("rejected", "candidate has larger invalid temp size");

//                        MyLogger.saveLog("CONFLICT: " + account + " temp: " + temp);
//                        MyLogger.saveLog("CONFLICT: " + account + " shortDataBlockchain: " + shortDataBlockchain);
                        return new ResponseEntity<>("FALSE", HttpStatus.CONFLICT);
                    }
                    System.out.println("SERVER_BLOCK_VALIDATE_RESULT=false");
                    recordValidationResult("rejected", "candidate short check failed");
                    return new ResponseEntity<>("FALSE", HttpStatus.EXPECTATION_FAILED);
                }


                if (shouldAcceptResolveCandidate(shortDataBlockchain, temp)) {


                    System.out.println("*************************************");
                    System.out.println("before original: " + shortDataBlockchain);
                    System.out.println("before temp: " + temp);

                    if (winnerList.size() + addlist.size() > MAX_TEMPORARY_SUBMITTED_BLOCKS) {
                        String reason = "temporary submitted block queue cap reached: "
                                + winnerList.size() + " + " + addlist.size()
                                + " > " + MAX_TEMPORARY_SUBMITTED_BLOCKS;
                        System.out.println("SERVER_BLOCK_ADD_REJECTED=" + reason);
                        recordValidationResult("rejected", reason);
                        return new ResponseEntity<>("FALSE", HttpStatus.TOO_MANY_REQUESTS);
                    }

                    winnerList.addAll(addlist);
                    BlockGrowthDiagnostics.submitQueued(
                            BlockGrowthDiagnostics.submitStatus().get("lastSubmitTraceId").toString(),
                            addlist.get(addlist.size() - 1),
                            winnerList.size());
                    NodeMemoryProfiler.recordStage("resolve_conflict after candidate queued", NodeMemoryProfiler.metrics(
                            "addlistSize", addlist.size(),
                            "winnerListSize", winnerList.size(),
                            "queuedBlockIndex", addlist.get(addlist.size() - 1).getIndex()
                    ));
                    recordAddedBlock(addlist.get(addlist.size() - 1).getIndex(), addlist.get(addlist.size() - 1).getHashBlock());
                    System.out.println("SERVER_BLOCK_ADD_SUCCESS");
                    System.out.println("SERVER_BLOCK_ADD_MODE=queued_for_tournament");
                    System.out.println("SERVER_BLOCK_ADD_SIZE_BEFORE=" + blockcheinSize);
                    System.out.println("SERVER_BLOCK_ADD_QUEUED_INDEX=" + addlist.get(addlist.size() - 1).getIndex());
                    System.out.println("SERVER_BLOCK_ADD_QUEUED_HASH=" + addlist.get(addlist.size() - 1).getHashBlock());
                    System.out.println("SERVER_BLOCK_ADD_TEMPORARY_QUEUE_SIZE=" + winnerList.size());

                    NodeMemoryProfiler.recordStage("resolve_conflict before tournament/local persistence", NodeMemoryProfiler.metrics(
                            "addlistSize", addlist.size(),
                            "winnerListSize", winnerList.size(),
                            "prePersistPeerPropagationEnabled", isPrePersistPeerPropagationEnabled()
                    ));
                    handlePrePersistPeerPropagation(addlist);
                    NodeMemoryProfiler.recordStage("resolve_conflict after tournament/local persistence handoff", NodeMemoryProfiler.metrics(
                            "addlistSize", addlist.size(),
                            "winnerListSize", winnerList.size(),
                            "prePersistPeerPropagationEnabled", isPrePersistPeerPropagationEnabled()
                    ));


                    //прибавить к общей сумме денег

                    dificultyOneBlock = prevBlock().getHashCompexity();

                    System.out.println("after original: " + shortDataBlockchain);
                    System.out.println("after temp: " + temp);

                    return new ResponseEntity<>("OK", HttpStatus.OK);
                } else {
                    recordValidationResult("rejected", "candidate did not beat local chain score");
                    return new ResponseEntity<>("FALSE", HttpStatus.EXPECTATION_FAILED);
                }


            } catch (Exception e) {
                lastServerException = e.getClass().getName() + ": " + e.getMessage();
                recordValidationResult("error", lastServerException);
                System.out.println("SERVER_BLOCK_ADD_ERROR");
                System.out.println("SERVER_BLOCK_ADD_ERROR_CLASS=" + e.getClass().getName());
                System.out.println("SERVER_BLOCK_ADD_ERROR_MESSAGE=" + e.getMessage());
                throw new RuntimeException(e);
            } finally {

                System.out.println("finish resolve_from_to_block");
            }

        } catch (Exception e) {
            lastServerException = e.getClass().getName() + ": " + e.getMessage();
            recordValidationResult("error", lastServerException);
            System.out.println("SERVER_BLOCK_ADD_ERROR");
            System.out.println("SERVER_BLOCK_ADD_ERROR_CLASS=" + e.getClass().getName());
            System.out.println("SERVER_BLOCK_ADD_ERROR_MESSAGE=" + e.getMessage());
            logResolveConflictException(e, request);

            return new ResponseEntity<>("FALSE", HttpStatus.EXPECTATION_FAILED);
        } finally {

            System.out.println("finish resolve_from_to_block");
            NodeMemoryProfiler.endTrace("resolve_conflict finish");
        }
    }

    private static void logResolveConflictException(Exception e, HttpServletRequest request) {
        String peer = request == null ? "unknown" : request.getRemoteAddr();
        String endpoint = "/nodes/resolve_from_to_block";
        if (PeerErrorRateLimiter.isNetworkTimeoutLike(e)) {
            RESOLVE_CONFLICT_ERROR_LIMITER.logPeerError(peer, endpoint, e);
            return;
        }
        MyLogger.saveLog("resolve_conflict unexpected error class=" + e.getClass().getName()
                + ", message=" + e.getMessage(), e);
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

    static boolean shouldAcceptResolveCandidate(DataShortBlockchainInformation local,
                                                DataShortBlockchainInformation candidate) {
        return shouldAcceptResolveCandidate(local, candidate, Seting.V35_RESCUE_FORK_INDEX);
    }

    static boolean shouldAcceptResolveCandidate(DataShortBlockchainInformation local,
                                                DataShortBlockchainInformation candidate,
                                                long forkIndex) {
        if (shouldUseV35ResolveAdmission(local, candidate, forkIndex)) {
            return compareV35ResolveAdmission(candidate, local) > 0;
        }
        return isLegacyResolveAdmissionPreferred(local, candidate);
    }

    static boolean isLegacyResolveAdmissionPreferred(DataShortBlockchainInformation local,
                                                     DataShortBlockchainInformation candidate) {
        if (local == null || candidate == null) {
            return false;
        }
        return candidate.getSize() > local.getSize()
                && candidate.getHashCount() >= local.getHashCount();
    }

    static boolean shouldUseV35ResolveAdmission(DataShortBlockchainInformation local,
                                                DataShortBlockchainInformation candidate,
                                                long forkIndex) {
        if (forkIndex == Long.MAX_VALUE) {
            return false;
        }
        if (!isShortInfoAtOrAfterFork(local, forkIndex) && !isShortInfoAtOrAfterFork(candidate, forkIndex)) {
            return false;
        }
        return parseResolveCumulativeWork(local).signum() > 0
                || parseResolveCumulativeWork(candidate).signum() > 0;
    }

    static int compareV35ResolveAdmission(DataShortBlockchainInformation left,
                                          DataShortBlockchainInformation right) {
        return ChainTournamentScorer.compare(toResolveChainScore(left), toResolveChainScore(right));
    }

    static boolean isResolveBlockBelowMinimumDifficulty(Block block) {
        return isResolveBlockBelowMinimumDifficulty(block, Seting.V35_RESCUE_FORK_INDEX);
    }

    static boolean isResolveBlockBelowMinimumDifficulty(Block block, long forkIndex) {
        if (block == null) {
            return true;
        }
        if (forkIndex != Long.MAX_VALUE && block.getIndex() >= forkIndex) {
            return false;
        }
        return block.getHashCompexity() < Seting.V34_MIN_DIFF;
    }

    private static ChainTournamentScorer.ChainScore toResolveChainScore(DataShortBlockchainInformation data) {
        if (data == null) {
            return ChainTournamentScorer.ChainScore.invalid();
        }
        return ChainTournamentScorer.score(
                data.isValidation(),
                parseResolveCumulativeWork(data),
                data.getSize(),
                0.0,
                data.getTipHash());
    }

    private static boolean isShortInfoAtOrAfterFork(DataShortBlockchainInformation data, long forkIndex) {
        return data != null && data.getSize() >= forkIndex;
    }

    private static BigInteger parseResolveCumulativeWork(DataShortBlockchainInformation data) {
        if (data == null || data.getCumulativeWork() == null || data.getCumulativeWork().trim().isEmpty()) {
            return BigInteger.ZERO;
        }
        try {
            BigInteger parsed = new BigInteger(data.getCumulativeWork());
            return parsed.signum() <= 0 ? BigInteger.ZERO : parsed;
        } catch (NumberFormatException e) {
            return BigInteger.ZERO;
        }
    }


    @RequestMapping(method = RequestMethod.POST, value = "/nodes/register", consumes = MediaType.APPLICATION_JSON_VALUE)
    public synchronized void register_node(@RequestBody AddressUrl urlAddrress) {

        try {

            if (BasisController.getNodes().contains(urlAddrress.getAddress())) {
                return;
            }

            for (String s : BasisController.getNodes()) {
                String original = s;
                String url = s + "/nodes/register";

                try {
                    UtilUrl.sendPost(urlAddrress.getAddress(), url);
                    sendAddress();


                } catch (Exception e) {
                    System.out.println("BasisController: register node: wrong node: " + original);
                    BasisController.getNodes().remove(original);
                    continue;
                }
            }

            Set<String> nodes = BasisController.getNodes();
            nodes = nodes.stream()
                    .map(t -> t.replaceAll("\"", ""))
                    .map(t -> t.replaceAll("\\\\", ""))
                    .collect(Collectors.toSet());
            nodes.add(urlAddrress.getAddress());

            Mining.deleteFiles(Seting.ORIGINAL_POOL_URL_ADDRESS_FILE);
            nodes.stream().forEach(t -> {
                try {

                    UtilsAllAddresses.saveAllAddresses(t, Seting.ORIGINAL_POOL_URL_ADDRESS_FILE);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                } catch (NoSuchAlgorithmException e) {
                    throw new RuntimeException(e);
                } catch (SignatureException e) {
                    throw new RuntimeException(e);
                } catch (InvalidKeySpecException e) {
                    throw new RuntimeException(e);
                } catch (NoSuchProviderException e) {
                    throw new RuntimeException(e);
                } catch (InvalidKeyException e) {
                    throw new RuntimeException(e);
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        } finally {

        }

    }


    @GetMapping("/findAddresses")
    public void findAddresses() {
        for (String s : Seting.ORIGINAL_ADDRESSES) {
            Set<String> addressesSet = new HashSet<>();
            try {
                String addresses = UtilUrl.readJsonFromUrl(s + "/getDiscoveryAddresses");
                addressesSet = UtilsJson.jsonToSetAddresses(addresses);
            } catch (IOException e) {
                System.out.println("BasisController: findAddress: error");
                continue;
            } catch (JSONException e) {
                throw new RuntimeException(e);
            }

            for (String s1 : addressesSet) {

                register_node(new AddressUrl(s1));
            }

        }

    }

    public static void sendAddress() {
        //лист временный для отправки аддресов

        for (String s : Seting.ORIGINAL_ADDRESSES) {

            String original = s;
            String url = s + "/nodes/register";

            if (BasisController.getExcludedAddresses().contains(url)) {
                System.out.println("MainController: its your address or excluded address: " + url);
                continue;
            }
            try {
                for (String s1 : BasisController.getNodes()) {


                    AddressUrl addressUrl = new AddressUrl(s1);
                    String json = UtilsJson.objToStringJson(addressUrl);
                    UtilUrl.sendPost(json, url);
                }
            } catch (Exception e) {
                System.out.println("BasisController: sendAddress: wronge node: " + original);

                continue;
            }


        }
    }

    /**
     * возвращает информацию о сложности текущей в блокчейне и суммарном
     */
    @GetMapping("/difficultyBlockchain")
    public InfoDificultyBlockchain dificultyBlockchain() {
        InfoDificultyBlockchain dificultyBlockchain = new InfoDificultyBlockchain();
        dificultyBlockchain.setDifficultyAllBlockchain(shortDataBlockchain.getHashCount());
        dificultyBlockchain.setDiffultyOneBlock(dificultyOneBlock);
        return dificultyBlockchain;
    }


    @GetMapping("/timentp")
    @ResponseBody
    public Long timentp() {
        return UtilsTime.getUniversalTimestamp();
    }
}
