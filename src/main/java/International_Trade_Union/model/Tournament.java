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

    @Override
    public void run() {
        while (true) {
            try {
                initializePrevTimesIfNeeded();


                tournament.getAllWinner();
                tournament.tournament();
                tournament.updatingNodeEndBlocks(false);

                checkAndUpdatePrevTime();
                checkAndUpdatePrevUpdateTime();
                checkAndUpdatePrevAllwinnersUpdateTime();

            } catch (Exception e) {
                handleException(e);
            } finally {
                BasisController.setIsSaveFile(true);
            }
        }
    }

    private void initializePrevTimesIfNeeded() {
        if (prevTime == 0 || prevUpdateTime == 0 || prevAllwinnersUpdateTime == 0) {
            tournament.updatingNodeEndBlocks(true);
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

    private void checkAndUpdatePrevTime() {
        long currentTime = UtilsTime.getUniversalTimestamp() / 1000;
        long timeDifference = currentTime - prevTime;

        if (timeDifference > Seting.TIME_TOURNAMENT_SECOND) {
            logTimeUpdate("Tournament", prevTime, currentTime);
            prevTime = currentTime;
        }
    }

    private void checkAndUpdatePrevUpdateTime() {
        long currentTime = UtilsTime.getUniversalTimestamp() / 1000;
        long timeDifference = currentTime - prevUpdateTime;

        if (timeDifference > Seting.TIME_UPDATING) {
            logTimeUpdate("Node Update", prevUpdateTime, currentTime);
            prevUpdateTime = currentTime;
        }
    }

    private void checkAndUpdatePrevAllwinnersUpdateTime(){
        long currentTime = UtilsTime.getUniversalTimestamp() / 1000;
        long timeDifference = currentTime - prevAllwinnersUpdateTime;

        if (timeDifference > Seting.GET_WINNER_SECOND) {
            logTimeUpdate("Node Update", prevAllwinnersUpdateTime, currentTime);
            prevAllwinnersUpdateTime = currentTime;
        }
    }

    private void logTimeUpdate(String process, long previousTime, long currentTime) {
        System.out.println("----------------------------------------------------");
        System.out.println("Process: " + process);
        System.out.println("Previous time: " + previousTime);
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

        BasisController.setIsSaveFile(true);
    }
}
