package International_Trade_Union.controllers;

import International_Trade_Union.controllers.config.BlockchainFactoryEnum;
import International_Trade_Union.entity.DtoTransaction.DtoTransaction;
import International_Trade_Union.entity.blockchain.Blockchain;
import International_Trade_Union.entity.blockchain.DataShortBlockchainInformation;
import International_Trade_Union.entity.blockchain.block.Block;
import International_Trade_Union.entity.entities.EntityDtoTransaction;
import International_Trade_Union.entity.services.BlockService;
import International_Trade_Union.model.Mining;
import International_Trade_Union.setings.Seting;
import International_Trade_Union.utils.UtilsBlock;
import International_Trade_Union.utils.UtilsBlockToEntityBlock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SignatureException;
import java.security.spec.InvalidKeySpecException;

@Controller
public class ConductorControlller {

    @Autowired
    BlockService blockService;

    @GetMapping("/conductorBlock")
    @ResponseBody
    public Block  block(@RequestParam Integer index) throws IOException {
        if(index < 0 ){
            index = 0;
        }
        if(index > BasisController.getBlockcheinSize() -1){
            index = BasisController.getBlockcheinSize() - 1;
        }
        return UtilsBlockToEntityBlock.entityBlockToBlock(
                BlockService.findBySpecialIndex(index)
        );
    }

    @GetMapping("/conductorHashTran")
    @ResponseBody
    public DtoTransaction transaction(@RequestParam String hash) throws IOException, NoSuchAlgorithmException, InvalidKeySpecException, SignatureException, NoSuchProviderException, InvalidKeyException {
        DataShortBlockchainInformation shortDataBlockchain
                = Blockchain.checkFromFile(Seting.ORIGINAL_BLOCKCHAIN_FILE);
        System.out.println("shortDataBlockchain: " + shortDataBlockchain);

       return BlockService.findBySign(hash);
    }


}
