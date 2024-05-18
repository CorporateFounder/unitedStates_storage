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
    TournamentService tournament;

    private static long prevTime;
    private static long prevUpdateTime;

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

                if(prevTime == 0){
                    if(BasisController.prevBlock() == null ){
                        prevTime = UtilsTime.getUniversalTimestamp() / 1000;
                    }
                    prevTime = BasisController.prevBlock().getTimestamp().getTime();
                    tournament.updatingNodeEndBlocks(true);
                }
                if(prevUpdateTime == 0){
                    if(BasisController.prevBlock() == null ){
                        prevUpdateTime = UtilsTime.getUniversalTimestamp() / 1000;
                    }
                    prevUpdateTime = BasisController.prevBlock().getTimestamp().getTime();
                    tournament.updatingNodeEndBlocks(true);
                }


                tournament.updatingNodeEndBlocks(false);
                tournament.tournament();

                tournament.updatingNodeEndBlocks(false);



                //TODO
                //если после обновления победитель изменился
                //то нужно в all winners добавить нового победителя
                //и заменить победителя, на победителя из базы данных последнего


                long timestamp = UtilsTime.getUniversalTimestamp() / 1000;
                long prevTime = Tournament.getPrevTime() / 1000L;
                long timeDifference = timestamp - prevTime;

                if(timeDifference > Seting.TIME_TOURNAMENT_SECOND + 10){
                    System.out.println("----------------------------------------------------");

                    System.out.println("change time prev before: " + Tournament.getPrevTime());
                    Tournament.setPrevTime(UtilsTime.getUniversalTimestamp());
                    System.out.println("timeDifference: " + timeDifference);
                    System.out.println("change time after before: " + Tournament.getPrevTime());
                    System.out.println("----------------------------------------------------");
                }

                long timestamp2 = UtilsTime.getUniversalTimestamp() / 1000;
                long prevTime2 = Tournament.getPrevUpdateTime() / 1000L;
                long timeDifference2 = timestamp2 - prevTime2;

                if(timeDifference2 > Seting.TIME_UPDATING + 10){
                    System.out.println("----------------------------------------------------");

                    System.out.println("change time prev before: " + Tournament.getPrevTime());
                    Tournament.setPrevUpdateTime(UtilsTime.getUniversalTimestamp());
                    System.out.println("timeDifference: " + timeDifference);
                    System.out.println("change time after before: " + Tournament.getPrevTime());
                    System.out.println("----------------------------------------------------");


                }



            }catch (Exception e){
                e.printStackTrace();
                BasisController.setWinnerList(null);
                BasisController.getWinnerList().clear();
                if(BasisController.getWinnerList() == null){
                    BasisController.setWinnerList(new CopyOnWriteArrayList<>());
                }else {
                    BasisController.getWinnerList().clear();
                }
                BasisController.setIsSaveFile(true);
                System.out.println("exeption");
                continue;
            }finally {
                BasisController.setIsSaveFile(true);
            }


        }


    }


}
