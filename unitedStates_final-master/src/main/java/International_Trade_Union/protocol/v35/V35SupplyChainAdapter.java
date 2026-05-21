package International_Trade_Union.protocol.v35;

import International_Trade_Union.entity.DtoTransaction.DtoTransaction;
import International_Trade_Union.entity.blockchain.block.Block;
import International_Trade_Union.model.Account;
import International_Trade_Union.setings.Seting;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Pure adapter from explicit chain/account inputs to a V35 supply snapshot.
 * This class does not read storage, call controllers, mutate balances, or validate blocks.
 */
public final class V35SupplyChainAdapter {
    private V35SupplyChainAdapter() {
    }

    public static V35SupplySnapshot snapshotFromChainAndAccounts(List<Block> chain,
                                                                 Map<String, Account> balances) {
        return snapshotFromChainAndAccounts(chain, balances, Seting.V35_BURN_ADDRESS, Seting.ADDRESS_FOUNDER);
    }

    public static V35SupplySnapshot snapshotFromChainAndAccounts(List<Block> chain,
                                                                 Map<String, Account> balances,
                                                                 String burnAddress,
                                                                 String founderAddress) {
        List<DtoTransaction> transactions = transactionsFromChain(chain);
        return snapshotFromTransactionsAndAccounts(transactions, balances, burnAddress, founderAddress);
    }

    public static V35SupplySnapshot snapshotFromTransactionsAndAccounts(List<DtoTransaction> transactions,
                                                                        Map<String, Account> balances) {
        return snapshotFromTransactionsAndAccounts(transactions, balances, Seting.V35_BURN_ADDRESS, Seting.ADDRESS_FOUNDER);
    }

    public static V35SupplySnapshot snapshotFromTransactionsAndAccounts(List<DtoTransaction> transactions,
                                                                        Map<String, Account> balances,
                                                                        String burnAddress,
                                                                        String founderAddress) {
        BigDecimal totalMinted = totalMintedGold(transactions);
        BigDecimal jackpotPaid = jackpotPaidGold(transactions);
        return V35SupplyCalculator.snapshotFromBalances(totalMinted, balances, jackpotPaid, burnAddress, founderAddress);
    }

    public static BigDecimal totalMintedGold(List<DtoTransaction> transactions) {
        return V35SupplyCalculator.totalMintedFromTransactions(transactions);
    }

    public static BigDecimal jackpotPaidGold(List<DtoTransaction> transactions) {
        return V35SupplyCalculator.jackpotPaidFromTransactions(transactions);
    }

    public static BigDecimal totalMintedGoldFromChain(List<Block> chain) {
        return totalMintedGold(transactionsFromChain(chain));
    }

    public static BigDecimal jackpotPaidGoldFromChain(List<Block> chain) {
        return jackpotPaidGold(transactionsFromChain(chain));
    }

    public static List<DtoTransaction> transactionsFromChain(List<Block> chain) {
        if (chain == null || chain.isEmpty()) {
            return Collections.emptyList();
        }
        List<DtoTransaction> transactions = new ArrayList<>();
        for (Block block : chain) {
            if (block != null && block.getDtoTransactions() != null) {
                for (DtoTransaction transaction : block.getDtoTransactions()) {
                    if (transaction != null) {
                        transactions.add(transaction);
                    }
                }
            }
        }
        return Collections.unmodifiableList(transactions);
    }
}
