package International_Trade_Union.controllers;


import International_Trade_Union.entity.DtoTransaction.DtoTransaction;
import International_Trade_Union.entity.blockchain.Blockchain;
import International_Trade_Union.entity.blockchain.block.Block;
import International_Trade_Union.model.Account;
import International_Trade_Union.network.AllTransactions;
import International_Trade_Union.setings.Seting;
import International_Trade_Union.vote.VoteEnum;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SignatureException;
import java.security.spec.InvalidKeySpecException;
import java.util.*;
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
       transactions = getTransactions(transactions);
       transactions = balanceTransaction(transactions);
       return transactions;
    }

    public static List<DtoTransaction> balanceTransaction(List<DtoTransaction> transactions){
        List<DtoTransaction> dtoTransactions = new ArrayList<>();
        Map<String, Account> balances = BasisController.getBalances();
        for (DtoTransaction transaction : transactions) {
            if(balances.containsKey(transaction.getSender())){
                Account account = balances.get(transaction.getSender());
                if(account.getDigitalStockBalance() >= transaction.getDigitalDollar()
                        + transaction.getBonusForMiner()
                ){
                    dtoTransactions.add(transaction);
                }
                if(account.getDigitalStockBalance() >= transaction.getDigitalStockBalance() && transaction.getVoteEnum().equals(VoteEnum.YES)){
                    dtoTransactions.add(transaction);
                }
                if(account.getDigitalStockBalance() >= transaction.getDigitalStockBalance() && transaction.getVoteEnum().equals(VoteEnum.NO)){
                    dtoTransactions.add(transaction);
                }
                if(account.getDigitalStockBalance() >= transaction.getDigitalStockBalance() && transaction.getVoteEnum().equals(VoteEnum.STAKING)){
                    dtoTransactions.add(transaction);
                }
            }
        }
        return transactions;
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
