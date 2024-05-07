package International_Trade_Union.controllers;


import International_Trade_Union.entity.DtoTransaction.DtoTransaction;
import International_Trade_Union.entity.blockchain.Blockchain;
import International_Trade_Union.entity.blockchain.block.Block;
import International_Trade_Union.entity.services.BlockService;
import International_Trade_Union.model.Account;
import International_Trade_Union.network.AllTransactions;
import International_Trade_Union.setings.Seting;
import International_Trade_Union.utils.UtilsAccountToEntityAccount;
import International_Trade_Union.vote.VoteEnum;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.beans.factory.annotation.Autowired;
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
    @Autowired
    BlockService blockService;

    public TransactionController() {
    }

    /**Добавить транзакцию в список транзакций, ожидающих добавления в блокчейн*/

    @RequestMapping(method = RequestMethod.POST, value = "/addTransaction", consumes = MediaType.APPLICATION_JSON_VALUE)
    public void add(@RequestBody DtoTransaction data) throws IOException, NoSuchAlgorithmException, SignatureException, InvalidKeySpecException, NoSuchProviderException, InvalidKeyException {
//        System.out.println("add transaction: " + data);

        AllTransactions.getInstance();
        if (!AllTransactions.getInstance().contains(data)) {
            if (!blockService.existsBySign(data.getSign()))
                AllTransactions.addTransaction(data);
        }

        System.out.println("TransactionController: add: " + AllTransactions.getInstance().size());
    }

    /**Возвращает хэш транзакций*/


    /**Возвращает список транзакций ожидающих добавления в блокчейн. В список не попадают транзакции,
     * если они были уже добавлены в блокчейн или их баланс не соответствует сумме которую они хотят отправить*/
    @GetMapping("/getTransactions")
    public List<DtoTransaction> getTransaction() throws IOException, NoSuchAlgorithmException, InvalidKeySpecException, SignatureException, NoSuchProviderException, InvalidKeyException {
       List<DtoTransaction> transactions= AllTransactions.getInstance().stream().distinct().collect(Collectors.toList());
       transactions = getTransactions(transactions);
       transactions = transactions.stream()
               .filter(t->!blockService.existsBySign(t.getSign()))
               .collect(Collectors.toList());
       transactions = balanceTransaction(transactions);
       return transactions;
    }

    /**Возвращает транзакции, которые имеют достаточно денег на счетах*/
    public  List<DtoTransaction> balanceTransaction(List<DtoTransaction> transactions){
        List<DtoTransaction> dtoTransactions = new ArrayList<>();
        Map<String, Account> balances = UtilsAccountToEntityAccount.entityAccountsToMapAccounts( blockService.findByDtoAccounts(transactions));
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
    public  List<DtoTransaction> getTransactions(List<DtoTransaction> transactions) throws JsonProcessingException {
        List<DtoTransaction> dtoTransactions = new ArrayList<>();

        for (DtoTransaction dtoTransaction : transactions) {
            if(!blockService.existsBySign(dtoTransaction.getSign())){
                dtoTransactions.add(dtoTransaction);
            }
        }

        return dtoTransactions;
    }

    public  boolean check(Block block) throws JsonProcessingException {
        boolean result = true;


        for (DtoTransaction transaction : block.getDtoTransactions()) {
            if(!blockService.existsBySign(transaction.getSign())){
                result = false;
            }
        }
        return result;
    }
    public Set<String> getTransactions() throws IOException, NoSuchAlgorithmException, InvalidKeySpecException, SignatureException, NoSuchProviderException, InvalidKeyException {
        List<DtoTransaction> transactions = AllTransactions.getInstance().stream().distinct().collect(Collectors.toList());
        Set<String> strings = new HashSet<>();
        for (DtoTransaction dtoTransaction : transactions) {
            if(!blockService.existsBySign(dtoTransaction.getSign())){
                strings.add(dtoTransaction.toSign());
            }
        }
        return strings;
    }


}
