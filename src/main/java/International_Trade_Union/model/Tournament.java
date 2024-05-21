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

    private static long prevTime;
    private static long prevUpdateTime;
    private static long prevAllwinnersUpdateTime;

    public static long getPrevTime() {
        return prevTime;
    }

    public static void setPrevTime(long prevTime) {
        Tournament.prevTime = prevTime;
    }

    public static long getPrevUpdateTime() {
        return prevUpdateTime;
    }

    public static void setPrevUpdateTime(long prevUpdateTime) {
        Tournament.prevUpdateTime = prevUpdateTime;
    }

    public static long getPrevAllwinnersUpdateTime() {
        return prevAllwinnersUpdateTime;
    }

    public static void setPrevAllwinnersUpdateTime(long prevAllwinnersUpdateTime) {
        Tournament.prevAllwinnersUpdateTime = prevAllwinnersUpdateTime;
    }

    @PostConstruct
    public void init() {
        Thread thread = new Thread(this);
        thread.setDaemon(true);
        thread.start();
    }

    private long nextTournamentTime;
    public void handleTournament(long currentTime) {
        if (isTimeForTournament(currentTime)) {
            long startTime = UtilsTime.getUniversalTimestamp();
            tournament.getAllWinner();
            long endTime = UtilsTime.getUniversalTimestamp();
            long getAllWinnerDuration = (endTime - startTime) / 1000L;

            nextTournamentTime = calculateNextTournamentTime(currentTime, getAllWinnerDuration);

            while (true) {
                currentTime = UtilsTime.getUniversalTimestamp() / 1000L;
                if (currentTime >= nextTournamentTime) {
                    tournament.tournament();
                    break;
                }
            }

            logTimeUpdate("Tournament", prevTime, currentTime);
            prevTime = UtilsTime.getUniversalTimestamp();
        }
    }



    private long calculateNextTournamentTime(long currentTime, long getAllWinnerDuration) {
        long nextTournamentTimeSeconds = (currentTime / Seting.TIME_TOURNAMENT_SECOND + 1) * Seting.TIME_TOURNAMENT_SECOND;
        return nextTournamentTimeSeconds * 1000L + Seting.TIME_OFFSET_SECOND * 1000L - getAllWinnerDuration * 1000L;
    }
    @Override
    public void run() {
        while (true) {
            try {
                initializePrevTimesIfNeeded();

                long currentTime = UtilsTime.getUniversalTimestamp() / 1000;

                handleTournament(currentTime);

                if (isTimeForUpdate(currentTime)) {
                    tournament.updatingNodeEndBlocks();
                    logTimeUpdate("Node Update", prevUpdateTime, currentTime);
                    prevUpdateTime = UtilsTime.getUniversalTimestamp();

                }
                System.out.println("you can safely shut down the server");

//                checkAndUpdatePrevTime();
//                checkAndUpdatePrevUpdateTime();
//                checkAndUpdatePrevAllwinnersUpdateTime();

            } catch (Exception e) {
//                handleException(e);
            } finally {
                BasisController.setIsSaveFile(true);
            }
        }
    }

    private void initializePrevTimesIfNeeded() {
        if (prevTime == 0 || prevUpdateTime == 0 || prevAllwinnersUpdateTime == 0) {
            tournament.updatingNodeEndBlocks();
            long currentTime = UtilsTime.getUniversalTimestamp() / 1000;

            if (BasisController.prevBlock() == null) {
                prevTime = currentTime;
                prevUpdateTime = currentTime;
                prevAllwinnersUpdateTime = currentTime;
            } else {
                long blockTime = BasisController.prevBlock().getTimestamp().getTime() / 1000;
                prevTime = (prevTime == 0) ? blockTime : prevTime;
                prevUpdateTime = (prevUpdateTime == 0) ? blockTime : prevUpdateTime;
                prevAllwinnersUpdateTime = (prevAllwinnersUpdateTime == 0) ? blockTime : prevAllwinnersUpdateTime;
            }
        }
    }



    private void logTimeUpdate(String process, long previousTime, long currentTime) {
        System.out.println("----------------------------------------------------");
        System.out.println("Process: " + process);
        System.out.println("Previous time: " + previousTime);
        System.out.println("Current time: " + currentTime);
        System.out.println("----------------------------------------------------");
    }

    private boolean isTimeForTournament(long currentTime) {
        long timeDifference = currentTime - (prevTime / 1000L);
        return timeDifference > Seting.TIME_TOURNAMENT_SECOND ;
    }

    private boolean isTimeForUpdate(long currentTime) {
        long timeDifference = currentTime - (prevUpdateTime / 1000L);
        return timeDifference > Seting.TIME_UPDATING;
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

        BasisController.setIsSaveFile(true);
    }
}
