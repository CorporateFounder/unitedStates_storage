package International_Trade_Union.controllers;


import International_Trade_Union.entity.DtoTransaction.DtoTransaction;
import International_Trade_Union.entity.blockchain.Blockchain;
import International_Trade_Union.entity.blockchain.block.Block;
import International_Trade_Union.entity.services.BlockService;
import International_Trade_Union.model.Account;
import International_Trade_Union.network.AllTransactions;
import International_Trade_Union.setings.Seting;
import International_Trade_Union.utils.UtilsAccountToEntityAccount;
import International_Trade_Union.utils.UtilsBalance;
import International_Trade_Union.utils.UtilsBlock;
import International_Trade_Union.utils.UtilsUse;
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

    @PostConstruct
    public void init() {
        Blockchain.setBlockService(blockService);
        UtilsBalance.setBlockService(blockService);
        UtilsBlock.setBlockService(blockService);

    }
    public TransactionController() {
    }

    /**
     * Добавить транзакцию в список транзакций, ожидающих добавления в блокчейн
     */

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
    public Boolean isWait58(@RequestParam String sign) {
        Base base = new Base58();
        long count = getTransaction().stream().filter(t -> base.encode(t.getSign()).equals(sign)).count();
        return count > 0;
    }

    /**
     * Возвращает список транзакций ожидающих добавления в блокчейн. В список не попадают транзакции,
     * если они были уже добавлены в блокчейн или их баланс не соответствует сумме которую они хотят отправить
     */
    @GetMapping("/getTransactions")
    public List<DtoTransaction> getTransaction() {

        List<DtoTransaction> transactions = new ArrayList<>();
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

            List<DtoTransaction> temp = new ArrayList<>();
            for (DtoTransaction dtoTransaction : transactions) {
                double digitalDollar = dtoTransaction.getDigitalDollar();
                double digitalStock = dtoTransaction.getDigitalStockBalance();
                double digitalBonus = dtoTransaction.getBonusForMiner();
                if(!UtilsUse.isTransactionValid(BigDecimal.valueOf(digitalDollar))){
                    System.out.println("the number dollar of decimal places exceeds ." + Seting.SENDING_DECIMAL_PLACES);
                    continue;
                }
                if(!UtilsUse.isTransactionValid(BigDecimal.valueOf(digitalStock))){
                    System.out.println("the number stock of decimal places exceeds ." + Seting.SENDING_DECIMAL_PLACES);
                    continue;
                }
                if(!UtilsUse.isTransactionValid(BigDecimal.valueOf(digitalBonus))){
                    System.out.println("the number bonus of decimal places exceeds ." + Seting.SENDING_DECIMAL_PLACES);
                    continue;
                }
                temp.add(dtoTransaction);
            }
            transactions = temp;


            transactions = balanceTransaction(transactions);

            AllTransactions.addAllTransactions(transactions);

        } catch (Exception e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
        transactions = transactions.stream().sorted(Comparator.comparing(DtoTransaction::getDigitalDollar).reversed()).collect(Collectors.toList());
        Base base = new Base58();
        transactions = transactions.stream()
                .filter(UtilsUse.distinctByKeyString(t -> {
                    try {
                        return base.encode(t.getSign());
                    } catch (Exception e) {
                        e.printStackTrace();
                        return null; // или другое значение по умолчанию
                    }
                }))
                .collect(Collectors.toList());
        transactions = transactions.stream()
                .filter(t -> t != null)
                .filter(t -> UtilsUse.isTransaction(t)).collect(Collectors.toList());

        return transactions;
    }


    /**
     * Возвращает транзакции, которые имеют достаточно денег на счетах
     */
    public List<DtoTransaction> balanceTransaction(List<DtoTransaction> transactions) throws IOException {

        List<DtoTransaction> dtoTransactions = new ArrayList<>();
        Map<String, Account> balances = UtilsAccountToEntityAccount.entityAccountsToMapAccounts(blockService.findByDtoAccounts(transactions));
        for (DtoTransaction transaction : transactions) {


                if (transaction.getVoteEnum().equals(VoteEnum.YES) || transaction.getVoteEnum().equals(VoteEnum.NO)) {
                    if (transaction.getSender().equals(transaction.getCustomer())) {
                        System.out.println("*************************************");
                        System.out.println("dtoSender: The sender and recipient address cannot be the same if VoteEnum.YES or NO");
                        System.out.println("*************************************");
                       continue;
                    }
                }

            if(!transaction.getCustomer().equals(BASIS_ADDRESS)){
                if(transaction.getDigitalDollar() < MINIMUM
                        && transaction.getDigitalStockBalance() < MINIMUM
                ){
                    System.out.println("*************************************");
                    System.out.println("If a transaction is not a voting transaction, it cannot transfer less than 0.01 of both a dollar and shares at the same time.");

                    System.out.println("transaction: " + transaction);
                    System.out.println("*************************************");
                   continue;
                }
            }
            // Check if both digital dollar and digital stock are below the minimum
            boolean result = false;
            if (balances.containsKey(transaction.getSender())) {
                Account sender = balances.get(transaction.getSender());
                Account customer = balances.get(transaction.getCustomer());
                BigDecimal transactionDigitalDollar = BigDecimal.valueOf(transaction.getDigitalDollar());
                BigDecimal transactionDigitalStock = BigDecimal.valueOf(transaction.getDigitalStockBalance());

                BigDecimal transactionBonusForMiner = BigDecimal.valueOf(transaction.getBonusForMiner());

                if (sender.getDigitalDollarBalance().compareTo(transactionDigitalDollar.add(transactionBonusForMiner)) >= 0) {
                    dtoTransactions.add(transaction);
                    result = true;
                } else if (sender.getDigitalStockBalance().compareTo(transactionDigitalStock.add(transactionBonusForMiner)) >= 0 && transaction.getVoteEnum().equals(VoteEnum.YES)) {
                    dtoTransactions.add(transaction);
                    result = true;
                } else if (sender.getDigitalStockBalance().compareTo(transactionDigitalStock.add(transactionBonusForMiner)) >= 0 && transaction.getVoteEnum().equals(VoteEnum.NO)) {
                    dtoTransactions.add(transaction);
                    result = true;
                } else if (sender.getDigitalDollarBalance().compareTo(transactionDigitalDollar.add(transactionBonusForMiner)) >= 0 && transaction.getVoteEnum().equals(VoteEnum.STAKING)) {
                    dtoTransactions.add(transaction);
                    result = true;
                } else if (sender.getDigitalStakingBalance().compareTo(transactionDigitalDollar.add(transactionBonusForMiner)) >= 0 && transaction.getVoteEnum().equals(VoteEnum.UNSTAKING)) {
                    dtoTransactions.add(transaction);
                    result = true;
                }
                try {
                    if (result == true) {
                        boolean sendtrue = UtilsBalance.sendMoneyNew(
                                sender,
                                customer,
                                BigDecimal.valueOf(transaction.getDigitalDollar()),
                                BigDecimal.valueOf(transaction.getDigitalStockBalance()),
                                BigDecimal.valueOf(transaction.getBonusForMiner()),
                                transaction.getVoteEnum());
                        if (sendtrue) {
                            balances.put(sender.getAccount(), sender);
                            balances.put(customer.getAccount(), customer);
                        }
                    }


                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        }
        return dtoTransactions;
    }

    //вычисляет транзакции которые были добавлены в блок, и их снова не добавляет
    public List<DtoTransaction> getTransactions(List<DtoTransaction> transactions) throws IOException {
        List<DtoTransaction> dtoTransactions = new ArrayList<>();

        for (DtoTransaction dtoTransaction : transactions) {
            if (!blockService.existsBySign(dtoTransaction.getSign())) {
                dtoTransactions.add(dtoTransaction);
            }
        }

        return dtoTransactions;
    }


    public Set<String> getTransactions() throws IOException, NoSuchAlgorithmException, InvalidKeySpecException, SignatureException, NoSuchProviderException, InvalidKeyException {
        Base base = new Base58();
        List<DtoTransaction> transactions = AllTransactions.getInstance().stream()
                .filter(UtilsUse.distinctByKeyString(t -> {
                    try {
                        return base.encode(t.getSign());
                    } catch (Exception e) {
                        e.printStackTrace();
                        return null; // или другое значение по умолчанию
                    }
                })).collect(Collectors.toList());
        transactions = transactions.stream().filter(t -> t != null).collect(Collectors.toList());

        Set<String> strings = new HashSet<>();
        for (DtoTransaction dtoTransaction : transactions) {
            if (!blockService.existsBySign(dtoTransaction.getSign())) {
                strings.add(dtoTransaction.toSign());
            }
        }
        return strings;
    }


}
