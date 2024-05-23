package International_Trade_Union.model;

import International_Trade_Union.controllers.BasisController;
import International_Trade_Union.entity.blockchain.Blockchain;
import International_Trade_Union.entity.blockchain.DataShortBlockchainInformation;
import International_Trade_Union.entity.blockchain.block.Block;
import International_Trade_Union.entity.entities.EntityBlock;
import International_Trade_Union.entity.services.BlockService;
import International_Trade_Union.logger.MyLogger;
import International_Trade_Union.setings.Seting;
import International_Trade_Union.utils.*;
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
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

@Component
@Scope("singleton")
public class Tournament implements Runnable {

    @Autowired
    private TournamentService tournament;

    private static final long TOURNAMENT_INTERVAL = 100 * 1000; // 100 секунд в миллисекундах
    private static final long GET_ALL_WINNERS_ADVANCE_TIME = 20 * 1000; // 20 секунд в миллисекундах

    @PostConstruct
    public void init() {
        Thread thread = new Thread(this);
        thread.setDaemon(true);
        thread.start();
        tournament.updatingNodeEndBlocks();
    }

    @Override
    public void run() {
        while (true) {
            try {
                long currentTime = UtilsTime.getUniversalTimestamp();
                long nextTournamentStartTime = getNextTournamentStartTime(currentTime);
                long nextGetAllWinnersStartTime = nextTournamentStartTime - GET_ALL_WINNERS_ADVANCE_TIME;

                if (currentTime >= nextGetAllWinnersStartTime && currentTime < nextTournamentStartTime) {
                    tournament.getAllWinner();
                    logTimeUpdate("getAllWinner", nextGetAllWinnersStartTime, currentTime);
                    Thread.sleep(nextTournamentStartTime - currentTime); // Подождать до начала турнира
                }

                if (currentTime >= nextTournamentStartTime) {
                    tournament.tournament();
                    tournament.updatingNodeEndBlocks();
                    logTimeUpdate("Tournament", nextTournamentStartTime, currentTime);
                    Thread.sleep(TOURNAMENT_INTERVAL); // Подождать до следующего запуска цикла
                }

                // Sleep until the next check
                Thread.sleep(1000); // Проверять каждую секунду

            } catch (Exception e) {
                handleException(e);
            } finally {
                BasisController.setIsSaveFile(true);
            }
        }
    }

    private long getNextTournamentStartTime(long currentTime) {
        long referenceTime = 0; // Начало эпохи (1 января 1970 года, 00:00:00 UTC)
        long timeSinceReference = currentTime - referenceTime;
        long nextTournamentStartTime = referenceTime + ((timeSinceReference / TOURNAMENT_INTERVAL) + 1) * TOURNAMENT_INTERVAL;
        return nextTournamentStartTime;
    }

    private void logTimeUpdate(String process, long scheduledTime, long currentTime) {
        System.out.println("----------------------------------------------------");
        System.out.println("Process: " + process);
        System.out.println("Scheduled time: " + scheduledTime);
        System.out.println("Current time: " + currentTime);
        System.out.println("----------------------------------------------------");
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
