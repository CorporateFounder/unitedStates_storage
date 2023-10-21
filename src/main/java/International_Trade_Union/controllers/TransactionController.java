package International_Trade_Union.controllers;


import International_Trade_Union.entity.DtoTransaction.DtoTransaction;
import International_Trade_Union.entity.blockchain.Blockchain;
import International_Trade_Union.entity.blockchain.block.Block;
import International_Trade_Union.network.AllTransactions;
import International_Trade_Union.setings.Seting;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SignatureException;
import java.security.spec.InvalidKeySpecException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
public class TransactionController {
    //транзакции которые попали в блокчейн.
    private static List<Block> transactionsAdded = new ArrayList<>();

    @RequestMapping(method = RequestMethod.POST, value = "/addTransaction", consumes = MediaType.APPLICATION_JSON_VALUE)
    public void add(@RequestBody DtoTransaction data) throws IOException, NoSuchAlgorithmException, SignatureException, InvalidKeySpecException, NoSuchProviderException, InvalidKeyException {
//        System.out.println("add transaction: " + data);

        AllTransactions.getInstance();
        if (!AllTransactions.getInstance().contains(data)) {
            if (!hashTransaction().contains(data.toSign()))
                AllTransactions.addTransaction(data);
        }

        System.out.println("TransactionController: add: " + AllTransactions.getInstance().size());
    }

    public static Set<String> hashTransaction() throws JsonProcessingException {
        Set<String> strings = new HashSet<>();
        transactionsAdded = Blockchain.subFromFile(BasisController.getBlockcheinSize() - Seting.TRANSACTIONS_COUNT_ADDED,
                BasisController.getBlockcheinSize(), Seting.ORIGINAL_BLOCKCHAIN_FILE);

        for (Block block : transactionsAdded) {
            for (DtoTransaction dtoTransaction : block.getDtoTransactions()) {
                strings.add(dtoTransaction.toSign());
            }

        }
        return strings;
    }

    @GetMapping("/getTransactions")
    public List<DtoTransaction> getTransaction() throws IOException, NoSuchAlgorithmException, InvalidKeySpecException, SignatureException, NoSuchProviderException, InvalidKeyException {
       List<DtoTransaction> transactions= AllTransactions.getInstance().stream().distinct().collect(Collectors.toList());
       return getTransactions(transactions);
    }

    //вычисляет транзакции которые были добавлены в блок, и их снова не добавляет
    public static List<DtoTransaction> getTransactions(List<DtoTransaction> transactions) throws JsonProcessingException {
        List<DtoTransaction> dtoTransactions = new ArrayList<>();
        Set<String> strings = hashTransaction();
        for (DtoTransaction dtoTransaction : transactions) {
            if(!strings.contains(dtoTransaction.toSign())){
                dtoTransactions.add(dtoTransaction);
            }
        }

        return dtoTransactions;
    }

    public static boolean check(Block block) throws JsonProcessingException {
        boolean result = true;

        Set<String> strings = hashTransaction();
        for (DtoTransaction transaction : block.getDtoTransactions()) {
            if(!strings.contains(transaction.toSign())){
                result = false;
            }
        }
        return result;
    }
    public Set<String> getTransactions() throws IOException, NoSuchAlgorithmException, InvalidKeySpecException, SignatureException, NoSuchProviderException, InvalidKeyException {
        List<DtoTransaction> transactions = AllTransactions.getInstance().stream().distinct().collect(Collectors.toList());
        Set<String> strings = hashTransaction();
        for (DtoTransaction dtoTransaction : transactions) {
            if(!strings.contains(dtoTransaction.toSign())){
                strings.add(dtoTransaction.toSign());
            }
        }
        return strings;
    }


}
