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
        long timestamp = UtilsTime.getUniversalTimestamp() / 1000;


        while (true) {
            try {
                List<Block> list = BasisController.getWinnerList();


                if(timestamp % Seting.TIME_UPDATING == 0){
                    try {
                        System.out.println("updating");
                        tournament.updatingNodeEndBlocks();
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }

                if(list.isEmpty() || list.size() == 0){
                    BasisController.setIsSaveFile(true);
//                    try {
//                        Thread.sleep(1000);
//                    } catch (InterruptedException e) {
//                        e.printStackTrace();
//                        continue;
//                    }
                    continue;

                }

                tournament.tournament();
                tournament.updatingNodeEndBlocks();
            }catch (Exception e){
                e.printStackTrace();
                BasisController.setWinnerList(new CopyOnWriteArrayList<>());
                BasisController.setIsSaveFile(true);
                continue;
            }


        }


    }


}
