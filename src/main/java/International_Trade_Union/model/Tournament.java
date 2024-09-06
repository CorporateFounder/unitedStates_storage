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
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

@Component
@Scope("singleton")
public class Tournament implements Runnable {

    @Autowired
    AllTransactions allTransactions;
    @Autowired
    NodeChecker nodeChecker;

    @Autowired
    SlidingWindowManager slidingWindowManager;

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
        UtilsBlock.setSlidingWindowManager(slidingWindowManager);

        Thread thread = new Thread(this);
        thread.setDaemon(true);
        thread.start();

    }


    @Override
    public void run() {
        BasisController.getBlockedNewSendBlock().set(false);
        // 1. Получаем исходный список узлов
        Set<String> nodes = BasisController.getNodes();
        List<HostEndDataShortB> hosts = utilsResolving.sortPriorityHost(nodes);
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
        UtilsBlock.setSlidingWindowManager(slidingWindowManager);
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

        while (true) {

            Blockchain.setBlockService(blockService);
            UtilsBalance.setBlockService(blockService);
            UtilsBlock.setBlockService(blockService);
            UtilsBlock.setSlidingWindowManager(slidingWindowManager);
            try {
                long currentTime = UtilsTime.getUniversalTimestamp();
                long nextTournamentStartTime = getNextTournamentStartTime(currentTime);

                // Wait until it's time to start getAllWinner
                waitUntil(nextTournamentStartTime);
                // Start getAllWinner
                BasisController.getBlockedNewSendBlock().set(false);
                hosts = utilsResolving.sortPriorityHost(BasisController.getNodes());
                tournament.tournament(hosts);
                tournament.updatingNodeEndBlocks(hosts);
                allTransactions.addAllTransactions(tournament.getInstance());
                BasisController.getBlockedNewSendBlock().set(true);
                tournament.getCheckSyncTime();


                // Sleep until the next tournament interval
                Thread.sleep(TOURNAMENT_INTERVAL - (UtilsTime.getUniversalTimestamp() - nextTournamentStartTime));

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
        if (currentTime < targetTime) {
            Thread.sleep(targetTime - currentTime);
        }
    }

    private long getNextTournamentStartTime(long currentTime) {
        long referenceTime = 0; // Начало эпохи (1 января 1970 года, 00:00:00 UTC)
        long timeSinceReference = currentTime - referenceTime;
        long nextTournamentStartTime = referenceTime + ((timeSinceReference / TOURNAMENT_INTERVAL) + 1) * TOURNAMENT_INTERVAL;
        return nextTournamentStartTime;
    }


    private void handleException(Exception e) {
        e.printStackTrace();
        MyLogger.saveLog("Tournament exception: ", e);
        BasisController.setWinnerList(null);
        if (BasisController.getWinnerList() == null) {
            BasisController.setWinnerList(new CopyOnWriteArrayList<>());
        } else {
            BasisController.getWinnerList().clear();
        }
    }

}
