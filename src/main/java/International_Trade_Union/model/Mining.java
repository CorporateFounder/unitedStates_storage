package International_Trade_Union.model;



import International_Trade_Union.config.BLockchainFactory;
import International_Trade_Union.config.BlockchainFactoryEnum;
import International_Trade_Union.entity.blockchain.Blockchain;
import International_Trade_Union.entity.blockchain.block.Block;
import International_Trade_Union.utils.*;
import java.io.File;
import java.io.IOException;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.util.*;


public class Mining {

    public static Blockchain getBlockchain(String filename, BlockchainFactoryEnum factoryEnum) throws IOException, NoSuchAlgorithmException, InvalidKeySpecException, SignatureException, NoSuchProviderException, InvalidKeyException {

        List<Block> blocks = UtilsBlock.readLineObject(filename);
        Blockchain blockchain = null;
        blockchain = BLockchainFactory.getBlockchain(factoryEnum);

        if (blocks.size() != 0) {
           blockchain.setBlockchainList(blocks);
        }
        return blockchain;
    }

    public static Map<String, Account> getBalances(String filename, Blockchain blockchain, Map<String, Account> balances) throws IOException, NoSuchAlgorithmException, SignatureException, InvalidKeySpecException, NoSuchProviderException, InvalidKeyException {
        //start test


        //папка чтобы проверить есть ли
        File folder = new File(filename);
        List<String> files = new ArrayList<>();
        for (File file : folder.listFiles()) {
            if(!file.isDirectory()){
                files.add(file.getAbsolutePath());
            }
        }

        if (files.size() > 0 ){
            File file = new File(files.get(files.size()-1));
            if(file.exists() && file.length() > 0){
                balances = SaveBalances.readLineObject(filename);
            }

        }

        if (balances == null) {
            balances = new HashMap<>();
        }

        Block block;
        if(blockchain != null && blockchain.sizeBlockhain() > 0){
            block = blockchain.getBlock(blockchain.sizeBlockhain() - 1);
            balances = UtilsBalance.calculateBalance(balances, block);


        }


        return balances;
    }

    public static void deleteFiles(String fileDelit) {
        UtilsFileSaveRead.deleteAllFiles(fileDelit);
    }


}
