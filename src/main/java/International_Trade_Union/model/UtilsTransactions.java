package International_Trade_Union.model;

import International_Trade_Union.entity.DtoTransaction.DtoTransaction;
import International_Trade_Union.entity.services.BlockService;
import International_Trade_Union.setings.Seting;
import International_Trade_Union.utils.UtilsAccountToEntityAccount;
import International_Trade_Union.vote.VoteEnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
public class UtilsTransactions {

    public List<DtoTransaction> balanceTransaction(List<DtoTransaction> transactions, BlockService blockService) throws IOException {
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
}
