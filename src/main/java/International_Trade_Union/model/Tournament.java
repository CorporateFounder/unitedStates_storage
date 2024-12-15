package International_Trade_Union.model;

import International_Trade_Union.controllers.BasisController;
import International_Trade_Union.controllers.NodeController;
import International_Trade_Union.entity.DtoTransaction.DtoTransaction;
import International_Trade_Union.entity.blockchain.Blockchain;
import International_Trade_Union.entity.blockchain.DataShortBlockchainInformation;
import International_Trade_Union.entity.blockchain.block.Block;
import International_Trade_Union.entity.entities.EntityBlock;
import International_Trade_Union.entity.services.BlockService;
import International_Trade_Union.logger.MyLogger;

import International_Trade_Union.network.AllTransactions;
import International_Trade_Union.setings.Seting;
import International_Trade_Union.utils.*;
import International_Trade_Union.utils.base.Base;
import org.json.JSONException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.transaction.Transactional;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SignatureException;
import java.security.spec.InvalidKeySpecException;
import java.sql.Timestamp;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Component
@Scope("singleton")
public class Tournament implements Runnable {
    public static List<HostEndDataShortB> hostsG = new ArrayList<>();
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    boolean fileBlockedDeleted = false;
    @Autowired
    AllTransactions allTransactions;
    @Autowired
    NodeChecker nodeChecker;

    @Autowired
    private TournamentService tournament;
    @Autowired
    private UtilsResolving utilsResolving;

    @Autowired
    private BlockService blockService;
    private static final long TOURNAMENT_INTERVAL = 100 * 1000; // 100 секунд в миллисекундах
    private static final long MAX_METHOD_EXECUTION_TIME = 18 * 1000; // 14 секунд в миллисекундах
    private static final long GET_ALL_WINNERS_ADVANCE_TIME = MAX_METHOD_EXECUTION_TIME + 40 * 1000; // 34 секунд в миллисекундах

    @PostConstruct
    public void init() {
        Blockchain.setBlockService(blockService);
        UtilsBalance.setBlockService(blockService);
        UtilsBlock.setBlockService(blockService);

        // Первоначальная синхронизация времени при запуске
        try {
            TimeSyncManager.syncTime();
            System.out.println("Время успешно синхронизировано при запуске.");
        } catch (Exception e) {
            System.err.println("Ошибка синхронизации времени при запуске: " + e.getMessage());
        }

        // Запуск потока для основного цикла турнира
        Thread thread = new Thread(this);
        thread.setDaemon(true);
        thread.start();

        // Ежедневная синхронизация времени (каждые 24 часа)
        scheduler.scheduleAtFixedRate(() -> {
            try {
                TimeSyncManager.syncTime();
                System.out.println("Time has been successfully synchronized.");
            } catch (Exception e) {
                System.err.println("Error during daily time synchronization: " + e.getMessage());
            }
        }, 24, 24, TimeUnit.HOURS); // Интервал - каждые 24 часа
    }

    private void deleteBlockedHosts() {
        try {
            Mining.deleteFiles(Seting.ORIGINAL_POOL_URL_ADDRESS_BLOCKED_FILE);
            System.out.println("Blocked hosts have been successfully removed.");
        } catch (Exception e) {
            System.err.println("Error while deleting blocked hosts: " + e.getMessage());
            MyLogger.saveLog("TournamentService: ", e);
        }
    }

    @Override
    public void run() {
        BasisController.getBlockedNewSendBlock().set(false);
        // 1. Получаем исходный список узлов
        Set<String> nodes = BasisController.getNodes();
        List<HostEndDataShortB> hosts = utilsResolving.sortPriorityHost(nodes);
        hostsG = hosts;
        try {
            nodeChecker.checkNodes(utilsResolving);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        }
        Blockchain.setBlockService(blockService);
        UtilsBalance.setBlockService(blockService);
        UtilsBlock.setBlockService(blockService);

        tournament.updatingNodeEndBlocks(hosts);
        BasisController.getBlockedNewSendBlock().set(true);

        try {
            long currentTime = UtilsTime.getUniversalTimestamp();
            long nextTournamentStartTime = getNextTournamentStartTime(currentTime);
            long startTimeWithDelay = nextTournamentStartTime + 10000; // 5 секунд после следующего начала турнира

            // Ждем, пока не наступит время начала турнира с задержкой
            waitUntil(startTimeWithDelay - currentTime);

        } catch (InterruptedException e) {
            handleException(e);
            return;
        }


        scheduler.scheduleAtFixedRate(this::deleteBlockedHosts, 0, Seting.DELETED_FILE_BLOCKED_HOST_TIME_SECOND, TimeUnit.SECONDS);
        fileBlockedDeleted = false;

        while (true) {

            // Запланировать задачу удаления каждые 500 секунд с начальной задержкой 0


            Blockchain.setBlockService(blockService);
            UtilsBalance.setBlockService(blockService);
            UtilsBlock.setBlockService(blockService);

            try {
                long currentTime = UtilsTime.getUniversalTimestamp();
                long nextTournamentStartTime = getNextTournamentStartTime(currentTime);

                // Ждем, пока не наступит время начала турнира
                waitUntil(nextTournamentStartTime);

                // Начинаем турнир

                hosts = utilsResolving.sortPriorityHost(BasisController.getNodes());
                hostsG = hosts;
                BasisController.getBlockedNewSendBlock().set(false);
                tournament.tournament(hosts);
                tournament.updatingNodeEndBlocks(hosts);
                allTransactions.addAllTransactions(tournament.getInstance(hosts));
                BasisController.getBlockedNewSendBlock().set(true);
                tournament.getCheckSyncTime(hosts);

                // Вычисляем время до следующего турнира
                long currentTimeAfterTournament = UtilsTime.getUniversalTimestamp();
                long sleepTime = TOURNAMENT_INTERVAL - (currentTimeAfterTournament - nextTournamentStartTime);

                if (sleepTime > 0) {
                    System.out.println("Sleeping for " + sleepTime + " milliseconds until next tournament.");
                    Thread.sleep(sleepTime);
                } else {
                    System.out.println("Negative or zero sleep time: " + sleepTime + ". Skipping sleep and proceeding to next iteration.");
                }

            } catch (Exception e) {
                tournament.updatingNodeEndBlocks(hosts);
                handleException(e);

            } finally {
                NodeController.setNotReady();
                BasisController.setIsSaveFile(true);
                BasisController.getBlockedNewSendBlock().set(true);
            }
        }
    }

    private void waitUntil(long targetTime) throws InterruptedException {
        long currentTime = UtilsTime.getUniversalTimestamp();
        long sleepTime = targetTime - currentTime;
        if (sleepTime > 0) {
            System.out.println("Waiting for " + sleepTime + " milliseconds.");
            Thread.sleep(sleepTime);
        } else {
            System.out.println("No need to wait. Current time (" + currentTime + ") has already passed target time (" + targetTime + ").");
        }
    }

    private long getNextTournamentStartTime(long currentTime) {
        if (TOURNAMENT_INTERVAL <= 0) {
            throw new IllegalArgumentException("TOURNAMENT_INTERVAL must be positive");
        }

        // Вычисляем количество интервалов, прошедших с начала эпохи
        long intervalsPassed = currentTime / TOURNAMENT_INTERVAL;
        long nextTournamentStartTime = (intervalsPassed + 1) * TOURNAMENT_INTERVAL;

        // Логирование для отладки
        System.out.println("Current Time: " + currentTime);
        System.out.println("TOURNAMENT_INTERVAL: " + TOURNAMENT_INTERVAL);
        System.out.println("Intervals Passed: " + intervalsPassed);
        System.out.println("Next Tournament Start Time: " + nextTournamentStartTime);

        return nextTournamentStartTime;
    }

    private void handleException(Exception e) {
        e.printStackTrace();
        MyLogger.saveLog("Tournament exception: ", e);
        BasisController.setWinnerList(null);
        if (BasisController.getWinnerList() == null) {
            BasisController.setWinnerList(new CopyOnWriteArrayList<>());
            BasisController.setSizeWinnerList(0);
        } else {
            BasisController.getWinnerList().clear();
            BasisController.setSizeWinnerList(0);
        }
    }


}
