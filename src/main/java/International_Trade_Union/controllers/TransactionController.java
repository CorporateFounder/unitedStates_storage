package International_Trade_Union.controllers;


import International_Trade_Union.entity.DtoTransaction.DtoTransaction;
import International_Trade_Union.entity.blockchain.Blockchain;
import International_Trade_Union.entity.blockchain.block.Block;
import International_Trade_Union.entity.services.BlockService;
import International_Trade_Union.model.Account;
import International_Trade_Union.network.AllTransactions;
import International_Trade_Union.setings.Seting;
import International_Trade_Union.utils.UtilsAccountToEntityAccount;
import International_Trade_Union.utils.UtilsUse;
import International_Trade_Union.utils.base.Base;
import International_Trade_Union.utils.base.Base58;
import International_Trade_Union.vote.VoteEnum;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

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
        Base base = new Base58();
        List<DtoTransaction> transactions = AllTransactions.getInstance();
        transactions = transactions.stream().filter(UtilsUse.distinctByKeyString(t -> {
            try {
                return base.encode(t.getSign());
            } catch (Exception e) {
                e.printStackTrace();
                return null; // или другое значение по умолчанию
            }
        })).collect(Collectors.toList());
        transactions = balanceTransaction(transactions);
        if (!transactions.contains(data)) {
            if (!blockService.existsBySign(data.getSign()))
                AllTransactions.addTransaction(data);
        }

        System.out.println("TransactionController: add: " + AllTransactions.getInstance().size());
    }

    @GetMapping("/isWait58")
    @ResponseBody
    public Boolean isWait58(@RequestParam String sign){
        Base base = new Base58();
        long count = getTransaction().stream().filter(t->base.encode(t.getSign()).equals(sign)).count();
        return count > 0;
    }

    /**Возвращает список транзакций ожидающих добавления в блокчейн. В список не попадают транзакции,
     * если они были уже добавлены в блокчейн или их баланс не соответствует сумме которую они хотят отправить*/
    @GetMapping("/getTransactions")
    public List<DtoTransaction> getTransaction() {

        List<DtoTransaction> transactions  = new ArrayList<>();
        try {

            transactions = AllTransactions.getInstance();
            transactions = getTransactions(transactions);
            transactions = transactions.stream()
                    .filter(t -> {
                        try {
                            return !blockService.existsBySign(t.getSign());
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }
                    })
                    .collect(Collectors.toList());
            transactions = balanceTransaction(transactions);
            AllTransactions.addAllTransactions(transactions);

        }catch (Exception e){
            e.printStackTrace();
            return new ArrayList<>();
        }
        System.out.println("tranactions: " + transactions);
        transactions = transactions.stream().sorted(Comparator.comparing(DtoTransaction::getDigitalDollar).reversed()).collect(Collectors.toList());
        Base base = new Base58();
        transactions = transactions .stream()
                .filter(UtilsUse.distinctByKeyString(t -> {
                    try {
                        return base.encode(t.getSign());
                    } catch (Exception e) {
                        e.printStackTrace();
                        return null; // или другое значение по умолчанию
                    }
                }))
                .collect(Collectors.toList());
        transactions = transactions.stream().filter(t->t!= null).collect(Collectors.toList());
        return transactions;
    }

    /**Возвращает транзакции, которые имеют достаточно денег на счетах*/
    public List<DtoTransaction> balanceTransaction(List<DtoTransaction> transactions) throws IOException {
        List<DtoTransaction> dtoTransactions = new ArrayList<>();
        Map<String, Account> balances = UtilsAccountToEntityAccount.entityAccountsToMapAccounts(blockService.findByDtoAccounts(transactions));

        BigDecimal minimum = BigDecimal.valueOf(Seting.MINIMUM);

        for (DtoTransaction transaction : transactions) {
            BigDecimal transactionDigitalDollar = BigDecimal.valueOf(transaction.getDigitalDollar());
            BigDecimal transactionDigitalStock = BigDecimal.valueOf(transaction.getDigitalStockBalance());

            // Check if both digital dollar and digital stock are below the minimum
            if (transactionDigitalDollar.compareTo(minimum) < 0 && transactionDigitalStock.compareTo(minimum) < 0) {
                continue;
            }

            if (balances.containsKey(transaction.getSender())) {
                Account account = balances.get(transaction.getSender());

                BigDecimal transactionBonusForMiner = BigDecimal.valueOf(transaction.getBonusForMiner());

                if (account.getDigitalDollarBalance().compareTo(transactionDigitalDollar.add(transactionBonusForMiner)) >= 0) {
                    dtoTransactions.add(transaction);
                }
                if (account.getDigitalStockBalance().compareTo(transactionDigitalStock.add(transactionBonusForMiner)) >= 0 && transaction.getVoteEnum().equals(VoteEnum.YES)) {
                    dtoTransactions.add(transaction);
                }
                if (account.getDigitalStockBalance().compareTo(transactionDigitalStock.add(transactionBonusForMiner)) >= 0 && transaction.getVoteEnum().equals(VoteEnum.NO)) {
                    dtoTransactions.add(transaction);
                }
                if (account.getDigitalDollarBalance().compareTo(transactionDigitalDollar.add(transactionBonusForMiner)) >= 0 && transaction.getVoteEnum().equals(VoteEnum.STAKING)) {
                    dtoTransactions.add(transaction);
                }
                if (account.getDigitalStakingBalance().compareTo(transactionDigitalDollar.add(transactionBonusForMiner)) >= 0 && transaction.getVoteEnum().equals(VoteEnum.UNSTAKING)) {
                    dtoTransactions.add(transaction);
                }
            }
        }
        return dtoTransactions;
    }
    //вычисляет транзакции которые были добавлены в блок, и их снова не добавляет
    public  List<DtoTransaction> getTransactions(List<DtoTransaction> transactions) throws IOException {
        List<DtoTransaction> dtoTransactions = new ArrayList<>();

        for (DtoTransaction dtoTransaction : transactions) {
            if(!blockService.existsBySign(dtoTransaction.getSign())){
                dtoTransactions.add(dtoTransaction);
            }
        }

        return dtoTransactions;
    }


    public Set<String> getTransactions() throws IOException, NoSuchAlgorithmException, InvalidKeySpecException, SignatureException, NoSuchProviderException, InvalidKeyException {
        Base base = new Base58();
        List<DtoTransaction> transactions = AllTransactions.getInstance() .stream()
                .filter(UtilsUse.distinctByKeyString(t -> {
                    try {
                        return base.encode(t.getSign());
                    } catch (Exception e) {
                        e.printStackTrace();
                        return null; // или другое значение по умолчанию
                    }
                })).collect(Collectors.toList());
        transactions = transactions.stream().filter(t->t!= null).collect(Collectors.toList());

        Set<String> strings = new HashSet<>();
        for (DtoTransaction dtoTransaction : transactions) {
            if(!blockService.existsBySign(dtoTransaction.getSign())){
                strings.add(dtoTransaction.toSign());
            }
        }
        return strings;
    }


}
