package International_Trade_Union.network;

import International_Trade_Union.controllers.BasisController;
import International_Trade_Union.entity.DtoTransaction.DtoTransaction;
import International_Trade_Union.entity.services.BlockService;
import International_Trade_Union.logger.MyLogger;
import International_Trade_Union.model.Account;
import International_Trade_Union.model.Mining;
import International_Trade_Union.setings.Seting;
import International_Trade_Union.utils.*;
import International_Trade_Union.utils.base.Base;
import International_Trade_Union.utils.base.Base58;
import International_Trade_Union.vote.VoteEnum;
import org.json.JSONException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SignatureException;
import java.security.spec.InvalidKeySpecException;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

@Component
public class AllTransactions {
    private final Lock lock = new ReentrantLock();
    private final List<DtoTransaction> instance = new CopyOnWriteArrayList<>();

    private final BlockService blockService;

    @Autowired
    public AllTransactions(BlockService blockService) {
        this.blockService = blockService;
    }

    public List<DtoTransaction> getTransactions() {
        lock.lock();
        try {
            return validateAndFilterTransactions(new ArrayList<>(instance));
        }catch (Exception e){
            MyLogger.saveLog("allTransactions error: ", e);
            return new CopyOnWriteArrayList<>();
        }
            finally {
            lock.unlock();
        }
    }

    public void addTransaction(DtoTransaction transaction) throws Exception {
        lock.lock();
        try {
            List<DtoTransaction> transactionsToValidate = new ArrayList<>();
            transactionsToValidate.add(transaction);

            List<DtoTransaction> validatedTransactions = validateAndFilterTransactions(transactionsToValidate);
            if (!validatedTransactions.isEmpty()) {
                instance.addAll(validatedTransactions);
            }
        } finally {
            lock.unlock();
        }
    }

    public void addAllTransactions(List<DtoTransaction> transactions) throws Exception {
        lock.lock();
        try {
            transactions = validateAndFilterTransactions(transactions);
            instance.addAll(transactions);
        } finally {
            lock.unlock();
        }
    }

    private List<DtoTransaction> validateAndFilterTransactions(List<DtoTransaction> transactions) throws IOException, NoSuchAlgorithmException, InvalidKeySpecException, SignatureException, NoSuchProviderException, InvalidKeyException {
        // Удаление дубликатов и транзакций, уже добавленных в блокчейн
        Base base = new Base58();
        List<DtoTransaction> filteredTransactions = transactions.stream()
                .filter(UtilsUse.distinctByKeyString(t -> {
                    try {
                        return base.encode(t.getSign());
                    } catch (Exception e) {
                        e.printStackTrace();
                        return null;
                    }
                }))
                .filter(t -> t != null)
                .filter(t -> {
                    try {
                        return !blockService.existsBySign(t.getSign());
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                })
                .collect(Collectors.toList());

        // Проверка на валидность количества знаков после запятой


        List<DtoTransaction> validTransactions = new ArrayList<>();
        for (DtoTransaction dtoTransaction : filteredTransactions) {
            double digitalDollar = dtoTransaction.getDigitalDollar();
            double digitalStock = dtoTransaction.getDigitalStockBalance();
            double digitalBonus = dtoTransaction.getBonusForMiner();

            if (!UtilsUse.isTransactionValid(BigDecimal.valueOf(digitalDollar))) {
                System.out.println("The number of decimal places for digitalDollar exceeds " + Seting.SENDING_DECIMAL_PLACES);
                continue;
            }
            if (!UtilsUse.isTransactionValid(BigDecimal.valueOf(digitalStock))) {
                System.out.println("The number of decimal places for digitalStock exceeds " + Seting.SENDING_DECIMAL_PLACES);
                continue;
            }
            if (!UtilsUse.isTransactionValid(BigDecimal.valueOf(digitalBonus))) {
                System.out.println("The number of decimal places for digitalBonus exceeds " + Seting.SENDING_DECIMAL_PLACES);
                continue;
            }
            validTransactions.add(dtoTransaction);
        }

        // Проверка баланса и других логических проверок
        return balanceTransaction(validTransactions);
    }

    private List<DtoTransaction> balanceTransaction(List<DtoTransaction> transactions) throws IOException, NoSuchAlgorithmException, InvalidKeySpecException, SignatureException, NoSuchProviderException, InvalidKeyException {
        Map<String, Account> balances = UtilsAccountToEntityAccount.entityAccountsToMapAccounts(blockService.findByDtoAccounts(transactions));
        return UtilsUse.balanceTransaction(transactions, balances, BasisController.getBlockchainSize()-1);
    }
}