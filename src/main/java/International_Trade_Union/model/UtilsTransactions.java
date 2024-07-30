package International_Trade_Union.model;

import International_Trade_Union.entity.DtoTransaction.DtoTransaction;
import International_Trade_Union.entity.services.BlockService;
import International_Trade_Union.utils.UtilsAccountToEntityAccount;
import International_Trade_Union.vote.VoteEnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
public class UtilsTransactions {

    public List<DtoTransaction> balanceTransaction(List<DtoTransaction> transactions, BlockService blockService) throws IOException {
        List<DtoTransaction> dtoTransactions = new ArrayList<>();
        Map<String, Account> balances = UtilsAccountToEntityAccount.entityAccountsToMapAccounts( blockService.findByDtoAccounts(transactions));
        for (DtoTransaction transaction : transactions) {
            if(balances.containsKey(transaction.getSender())){
                Account account = balances.get(transaction.getSender());
                if(account.getDigitalDollarBalance() >= transaction.getDigitalDollar()
                        + transaction.getBonusForMiner()
                ){
                    dtoTransactions.add(transaction);
                }
                if(account.getDigitalStockBalance() >= transaction.getDigitalStockBalance() + transaction.getBonusForMiner() && transaction.getVoteEnum().equals(VoteEnum.YES)){

                    dtoTransactions.add(transaction);
                }
                if(account.getDigitalStockBalance() >= transaction.getDigitalStockBalance()  + transaction.getBonusForMiner() && transaction.getVoteEnum().equals(VoteEnum.NO)){
                    dtoTransactions.add(transaction);
                }
                if(account.getDigitalDollarBalance() >= transaction.getDigitalDollar() + transaction.getBonusForMiner() && transaction.getVoteEnum().equals(VoteEnum.STAKING)){

                    dtoTransactions.add(transaction);
                }
                if(account.getDigitalStakingBalance() >= transaction.getDigitalDollar()+ transaction.getBonusForMiner() && transaction.getVoteEnum().equals(VoteEnum.UNSTAKING)){
                    dtoTransactions.add(transaction);
                }


            }

        }
        return dtoTransactions;
    }

}
