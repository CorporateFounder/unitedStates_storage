package International_Trade_Union.controllers;


import International_Trade_Union.entity.DtoTransaction.DtoTransaction;
import International_Trade_Union.entity.blockchain.Blockchain;
import International_Trade_Union.entity.blockchain.block.Block;
import International_Trade_Union.entity.services.BlockService;
import International_Trade_Union.logger.MyLogger;
import International_Trade_Union.model.Account;

import International_Trade_Union.network.AllTransactions;
import International_Trade_Union.setings.Seting;
import International_Trade_Union.utils.*;
import International_Trade_Union.utils.base.Base;
import International_Trade_Union.utils.base.Base58;
import International_Trade_Union.vote.VoteEnum;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.math.BigDecimal;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SignatureException;
import java.security.spec.InvalidKeySpecException;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static International_Trade_Union.setings.Seting.*;

@RestController
public class TransactionController {
    @Autowired
    BlockService blockService;

    @Autowired
    AllTransactions allTransactions;

    @PostConstruct
    public void init() {
        Blockchain.setBlockService(blockService);
        UtilsBalance.setBlockService(blockService);
        UtilsBlock.setBlockService(blockService);
    }

    public TransactionController() {
    }

    @RequestMapping(method = RequestMethod.POST, value = "/addTransaction", consumes = MediaType.APPLICATION_JSON_VALUE)
    public void add(@RequestBody DtoTransaction data) throws IOException, NoSuchAlgorithmException, InvalidKeySpecException, SignatureException, NoSuchProviderException, InvalidKeyException {
        try {
            allTransactions.addTransaction(data);
        } catch (Exception e) {
            MyLogger.saveLog("add ", e);
        }

        System.out.println("TransactionController: add: " + allTransactions.getTransactions().size());
    }

    @GetMapping("/isWait58")
    @ResponseBody
    public Boolean isWait58(@RequestParam String sign) throws IOException, NoSuchAlgorithmException, InvalidKeySpecException, SignatureException, NoSuchProviderException, InvalidKeyException {
        Base base = new Base58();
        long count = allTransactions.getTransactions().stream()
                .filter(t -> base.encode(t.getSign()).equals(sign))
                .count();
        return count > 0;
    }

    @GetMapping("/getTransactions")
    public List<DtoTransaction> getTransaction() {
        List<DtoTransaction> transactions = new ArrayList<>();
        try {
            transactions = allTransactions.getTransactions();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return transactions;
    }
}
