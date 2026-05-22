package International_Trade_Union.protocol.v35;

import International_Trade_Union.entity.DtoTransaction.DtoTransaction;
import International_Trade_Union.entity.blockchain.block.Block;
import International_Trade_Union.model.Account;
import International_Trade_Union.setings.Seting;
import International_Trade_Union.vote.Laws;
import International_Trade_Union.vote.VoteEnum;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class V35SupplyChainAdapterTest {
    private static final String BURN = "final-burn-address";
    private static final String FOUNDER = Seting.ADDRESS_FOUNDER;

    @Test
    public void v35RemainsDisabled() {
        assertEquals(Long.MAX_VALUE, Seting.V35_RESCUE_FORK_INDEX);
    }

    @Test
    public void buildsSnapshotFromTransactionsAndAccounts() {
        List<DtoTransaction> transactions = Arrays.asList(
                tx(Seting.BASIS_ADDRESS, "miner", 6.00),
                tx(Seting.BASIS_ADDRESS, "winner", 302.40),
                tx("sender", "recipient", 99.00),
                tx(Seting.BASIS_ADDRESS, "odd-recipient", 7.77)
        );
        Map<String, Account> balances = balances("60.00", "15.00");

        V35SupplySnapshot snapshot = V35SupplyChainAdapter.snapshotFromTransactionsAndAccounts(
                transactions, balances, BURN, FOUNDER);

        assertTrue(snapshot.isValid());
        assertTrue(snapshot.isBurnAddressConfigured());
        assertEquals(bd("316.17"), snapshot.getTotalMintedGold());
        assertEquals(bd("302.40"), snapshot.getJackpotPaidGold());
        assertEquals(bd("60.00"), snapshot.getBurnedGold());
        assertEquals(bd("256.17"), snapshot.getCirculatingGold());
        assertEquals(bd("15.00"), snapshot.getFounderReserveGold());
        assertEquals(bd("1261440.00"), snapshot.getAnnualEmissionGold());
    }

    @Test
    public void normalTransfersDoNotCreateMintedSupply() {
        List<DtoTransaction> transactions = Arrays.asList(
                tx("alice", "bob", 100.00),
                tx("bob", "carol", 25.00)
        );

        assertEquals(bd("0.00"), V35SupplyChainAdapter.totalMintedGold(transactions));
        assertEquals(bd("0.00"), V35SupplyChainAdapter.jackpotPaidGold(transactions));
    }

    @Test
    public void minerRewardIsMintedButNotJackpotPaid() {
        List<DtoTransaction> transactions = Collections.singletonList(
                tx(Seting.BASIS_ADDRESS, "miner", 6.00)
        );

        assertEquals(bd("6.00"), V35SupplyChainAdapter.totalMintedGold(transactions));
        assertEquals(bd("0.00"), V35SupplyChainAdapter.jackpotPaidGold(transactions));
    }

    @Test
    public void nonCanonicalBasisAmountIsMintedButNotJackpotPaid() {
        List<DtoTransaction> transactions = Collections.singletonList(
                tx(Seting.BASIS_ADDRESS, "recipient", 8.01)
        );

        assertEquals(bd("8.01"), V35SupplyChainAdapter.totalMintedGold(transactions));
        assertEquals(bd("0.00"), V35SupplyChainAdapter.jackpotPaidGold(transactions));
    }

    @Test
    public void configuredBurnAddressReturnsActualBurnedBalance() {
        Map<String, Account> balances = new HashMap<>();
        balances.put(Seting.V35_BURN_ADDRESS, new Account(Seting.V35_BURN_ADDRESS, bd("2.00")));
        balances.put(FOUNDER, new Account(FOUNDER, bd("15.00")));

        V35SupplySnapshot snapshot = V35SupplyChainAdapter.snapshotFromTransactionsAndAccounts(
                Collections.singletonList(tx(Seting.BASIS_ADDRESS, "miner", 6.00)),
                balances);

        assertTrue(snapshot.isBurnAddressConfigured());
        assertEquals(bd("2.00"), snapshot.getBurnedGold());
        assertEquals(bd("4.00"), snapshot.getCirculatingGold());
    }

    @Test
    public void buildsSnapshotFromChainAndAccounts() {
        Block first = block(1L, Arrays.asList(
                tx(Seting.BASIS_ADDRESS, "miner", 6.00),
                tx("alice", "bob", 1.00)
        ));
        Block second = block(2L, Collections.singletonList(
                tx(Seting.BASIS_ADDRESS, "winner", 302.40)
        ));

        V35SupplySnapshot snapshot = V35SupplyChainAdapter.snapshotFromChainAndAccounts(
                Arrays.asList(first, null, second),
                balances("10.00", "5.00"),
                BURN,
                FOUNDER);

        assertEquals(3, V35SupplyChainAdapter.transactionsFromChain(Arrays.asList(first, null, second)).size());
        assertEquals(bd("308.40"), V35SupplyChainAdapter.totalMintedGoldFromChain(Arrays.asList(first, second)));
        assertEquals(bd("302.40"), V35SupplyChainAdapter.jackpotPaidGoldFromChain(Arrays.asList(first, second)));
        assertEquals(bd("308.40"), snapshot.getTotalMintedGold());
        assertEquals(bd("10.00"), snapshot.getBurnedGold());
        assertEquals(bd("298.40"), snapshot.getCirculatingGold());
    }

    private static Map<String, Account> balances(String burn, String founder) {
        Map<String, Account> balances = new HashMap<>();
        balances.put(BURN, new Account(BURN, bd(burn)));
        balances.put(FOUNDER, new Account(FOUNDER, bd(founder)));
        return balances;
    }

    private static DtoTransaction tx(String sender, String recipient, double amount) {
        return new DtoTransaction(sender, recipient, amount, 0.0, new Laws(), 0.0, VoteEnum.YES);
    }

    private static Block block(long index, List<DtoTransaction> transactions) {
        return new Block(transactions, "prev", "miner", FOUNDER, 0L, 0.0, 1L,
                new Timestamp(index * 1000L), index, "hash-" + index);
    }

    private static BigDecimal bd(String value) {
        return new BigDecimal(value).setScale(2);
    }
}
