package International_Trade_Union.protocol.v35;

import International_Trade_Union.entity.DtoTransaction.DtoTransaction;
import International_Trade_Union.model.Account;
import International_Trade_Union.setings.Seting;
import International_Trade_Union.vote.Laws;
import International_Trade_Union.vote.VoteEnum;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class V35SupplyCalculatorTest {
    private static final String FINAL_BURN_ADDRESS = "burn-address-for-supply-test";
    private static final String FOUNDER = Seting.ADDRESS_FOUNDER;

    @Test
    public void v35RemainsDisabled() {
        International_Trade_Union.protocol.v35.V35TestMode.assertConfiguredForkSafety();
    }

    @Test
    public void calculatesBasicSupplySnapshot() {
        V35SupplySnapshot snapshot = V35SupplyCalculator.snapshot(
                bd("1000.00"),
                bd("250.00"),
                bd("100.00"),
                bd("50.00"));

        assertTrue(snapshot.isValid());
        assertTrue(snapshot.isBurnAddressConfigured());
        assertEquals(bd("1000.00"), snapshot.getTotalMintedGold());
        assertEquals(bd("250.00"), snapshot.getBurnedGold());
        assertEquals(bd("750.00"), snapshot.getCirculatingGold());
        assertEquals(bd("100.00"), snapshot.getFounderReserveGold());
        assertEquals(bd("50.00"), snapshot.getJackpotPaidGold());
        assertEquals(bd("1261440.00"), snapshot.getAnnualEmissionGold());
    }

    @Test
    public void calculatesBurnedAndFounderReserveFromAccountMap() {
        Map<String, Account> balances = new HashMap<>();
        balances.put(FINAL_BURN_ADDRESS, new Account(FINAL_BURN_ADDRESS, bd("60.82")));
        balances.put(FOUNDER, new Account(FOUNDER, bd("15.21")));

        V35SupplySnapshot snapshot = V35SupplyCalculator.snapshotFromBalances(
                bd("159.55"),
                balances,
                bd("302.40"),
                FINAL_BURN_ADDRESS,
                FOUNDER);

        assertTrue(snapshot.isValid());
        assertTrue(snapshot.isBurnAddressConfigured());
        assertEquals(bd("60.82"), snapshot.getBurnedGold());
        assertEquals(bd("98.73"), snapshot.getCirculatingGold());
        assertEquals(bd("15.21"), snapshot.getFounderReserveGold());
        assertEquals(bd("302.40"), snapshot.getJackpotPaidGold());
    }

    @Test
    public void placeholderBurnAddressBalanceDoesNotBecomeBurnedSupply() {
        Map<String, Account> balances = new HashMap<>();
        balances.put(Seting.V35_BURN_ADDRESS, new Account(Seting.V35_BURN_ADDRESS, bd("60.82")));
        balances.put(FOUNDER, new Account(FOUNDER, bd("76.03")));

        V35SupplySnapshot snapshot = V35SupplyCalculator.snapshotFromBalances(
                bd("159.55"),
                balances,
                bd("0.00"));

        assertTrue(BurnAddressPolicy.isPlaceholderBurnAddress());
        assertFalse(snapshot.isBurnAddressConfigured());
        assertTrue(snapshot.isValid());
        assertEquals(bd("0.00"), snapshot.getBurnedGold());
        assertEquals(bd("159.55"), snapshot.getCirculatingGold());
        assertEquals(bd("76.03"), snapshot.getFounderReserveGold());
    }

    @Test
    public void modelsFounderBurnEconomics() {
        V35SupplySnapshot beforeBurn = V35SupplyCalculator.snapshot(
                bd("159550000.00"),
                bd("0.00"),
                bd("76030000.00"),
                bd("0.00"));
        V35SupplySnapshot afterBurn = V35SupplyCalculator.snapshot(
                bd("159550000.00"),
                bd("60824000.00"),
                bd("15206000.00"),
                bd("0.00"));

        assertEquals(bd("159550000.00"), beforeBurn.getCirculatingGold());
        assertEquals(bd("98726000.00"), afterBurn.getCirculatingGold());
        assertEquals(new BigDecimal("0.7906"), beforeBurn.getInflationBeforeBurnPercent());
        assertEquals(new BigDecimal("1.2777"), afterBurn.getInflationAfterBurnPercent());
    }

    @Test
    public void exposesAnnualEmissionBreakdown() {
        assertEquals(bd("1261440.00"), V35SupplyCalculator.annualEmissionGold());
        assertEquals(bd("946080.00"), V35SupplyCalculator.annualMinerEmissionGold());
        assertEquals(bd("315360.00"), V35SupplyCalculator.annualJackpotEmissionGold());
    }

    @Test
    public void rejectsInvalidSupplyInputsSafely() {
        V35SupplySnapshot burnedTooHigh = V35SupplyCalculator.snapshot(
                bd("100.00"), bd("101.00"), bd("0.00"), bd("0.00"));
        V35SupplySnapshot negative = V35SupplyCalculator.snapshot(
                bd("-1.00"), bd("0.00"), bd("0.00"), bd("0.00"));

        assertFalse(burnedTooHigh.isValid());
        assertEquals(bd("0.00"), burnedTooHigh.getCirculatingGold());
        assertEquals("burned supply exceeds total minted", burnedTooHigh.getInvalidReason());
        assertFalse(negative.isValid());
        assertEquals("negative supply input", negative.getInvalidReason());
    }

    @Test
    public void countsMintedAndJackpotPaidFromBasisTransactionsOnly() {
        DtoTransaction minerReward = tx(Seting.BASIS_ADDRESS, "miner", 6.00);
        DtoTransaction jackpotPayout = tx(Seting.BASIS_ADDRESS, "winner", 302.40);
        DtoTransaction ordinaryTransfer = tx("sender", "recipient", 100.00);

        assertEquals(bd("308.40"), V35SupplyCalculator.totalMintedFromTransactions(
                Arrays.asList(minerReward, jackpotPayout, ordinaryTransfer)));
        assertEquals(bd("302.40"), V35SupplyCalculator.jackpotPaidFromTransactions(
                Arrays.asList(minerReward, jackpotPayout, ordinaryTransfer)));
    }

    @Test
    public void virtualJackpotReserveIsNotCountedUntilPayoutTransactionExists() {
        DtoTransaction minerRewardOnly = tx(Seting.BASIS_ADDRESS, "miner", 6.00);

        assertEquals(bd("6.00"), V35SupplyCalculator.totalMintedFromTransactions(Arrays.asList(minerRewardOnly)));
        assertEquals(bd("0.00"), V35SupplyCalculator.jackpotPaidFromTransactions(Arrays.asList(minerRewardOnly)));
    }

    private static DtoTransaction tx(String sender, String recipient, double gold) {
        return new DtoTransaction(sender, recipient, gold, 0.0, new Laws(), 0.0, VoteEnum.YES);
    }

    private static BigDecimal bd(String value) {
        return new BigDecimal(value).setScale(2);
    }
}
