package International_Trade_Union.controllers;

import International_Trade_Union.controllers.config.BlockchainFactoryEnum;
import International_Trade_Union.entity.blockchain.Blockchain;
import International_Trade_Union.model.Mining;
import International_Trade_Union.setings.Seting;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SignatureException;
import java.security.spec.InvalidKeySpecException;

@RestController
public class BlockchainCheckController {

    //TODO во время майнинга, если майнинг не остановлен, он почему то возвращает false, но после остановки true. Нужно исправить.
    //TODO during mining, if mining is not stopped, for some reason it returns false, but after stopping it returns true. Need to fix.
    //TODO worked after restart server, get true.
    //TODO ошибка возникает если прервать mine,
    //TODO error occurs if mine is interrupted,
    @GetMapping("/checkValidation")
    public synchronized boolean checkValidation() throws IOException, NoSuchAlgorithmException, SignatureException, InvalidKeySpecException, NoSuchProviderException, InvalidKeyException {
        try{
            Blockchain blockchain = Mining.getBlockchain(
                    Seting.ORIGINAL_BLOCKCHAIN_FILE,
                    BlockchainFactoryEnum.ORIGINAL);

            System.out.println("BlockchainCheckController: checkValidation: size: " + blockchain.sizeBlockhain());
            return blockchain.validatedBlockchain();}
        catch (IOException e){
            e.printStackTrace();
            return false;
        }
    }
}
