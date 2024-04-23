package International_Trade_Union.model;

import International_Trade_Union.controllers.BasisController;
import International_Trade_Union.entity.blockchain.Blockchain;
import International_Trade_Union.entity.blockchain.DataShortBlockchainInformation;
import International_Trade_Union.entity.blockchain.block.Block;
import International_Trade_Union.entity.entities.EntityBlock;
import International_Trade_Union.entity.services.BlockService;
import International_Trade_Union.setings.Seting;
import International_Trade_Union.utils.*;
import org.springframework.beans.factory.annotation.Autowired;
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
public class Tournament implements Runnable {

    @Autowired
    TournamentService tournament;

    private static long prevTime;

    public static long getPrevTime() {
        return prevTime;
    }

    public static void setPrevTime(long prevTime) {
        Tournament.prevTime = prevTime;
    }

    @PostConstruct
    public void init() {

        Thread thread = new Thread(this);
        thread.setDaemon(true);
        thread.start();
    }
    @Override
    public void run() {
        System.out.println("+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++");
        System.out.println("tournament:");
        System.out.println("+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++");



        while (true) {
            try {

                if(prevTime == 0){
                    prevTime = BasisController.prevBlock().getTimestamp().getTime();
                }

                tournament.updatingNodeEndBlocks();
                tournament.tournament();
                tournament.updatingNodeEndBlocks();

                List<Block> winner = new ArrayList<>();
                List<Block> winnerList = new ArrayList<>();

                //TODO
                //если после обновления победитель изменился
                //то нужно в all winners добавить нового победителя
                //и заменить победителя, на победителя из базы данных последнего


                long timestamp = UtilsTime.getUniversalTimestamp() / 1000;
                long prevTime = Tournament.getPrevTime() / 1000L;
                long timeDifference = timestamp - prevTime;

                if(timeDifference > Seting.TIME_UPDATING ){
                    System.out.println("----------------------------------------------------");

                    System.out.println("change time prev before: " + Tournament.getPrevTime());
                    Tournament.setPrevTime(UtilsTime.getUniversalTimestamp());
                    System.out.println("timeDifference: " + timeDifference);
                    System.out.println("change time after before: " + Tournament.getPrevTime());
                    System.out.println("----------------------------------------------------");
                }

            }catch (Exception e){
                e.printStackTrace();
                BasisController.setWinnerList(new CopyOnWriteArrayList<>());
                BasisController.setIsSaveFile(true);
                System.out.println("exeption");
                continue;
            }finally {
                BasisController.setIsSaveFile(true);
            }


        }


    }


}
