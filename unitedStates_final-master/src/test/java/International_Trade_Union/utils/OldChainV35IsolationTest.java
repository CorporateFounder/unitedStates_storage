package International_Trade_Union.utils;

import International_Trade_Union.entity.DtoTransaction.DtoTransaction;
import International_Trade_Union.entity.blockchain.Blockchain;
import International_Trade_Union.entity.blockchain.block.Block;
import International_Trade_Union.model.Account;
import International_Trade_Union.protocol.v35.BurnAddressPolicy;
import International_Trade_Union.protocol.v35.JackpotCalculator;
import International_Trade_Union.protocol.v35.ProtocolVersion;
import International_Trade_Union.protocol.v35.V35RewardCalculator;
import International_Trade_Union.setings.Seting;
import International_Trade_Union.utils.base.Base;
import International_Trade_Union.utils.base.Base58;
import International_Trade_Union.vote.Laws;
import International_Trade_Union.vote.VoteEnum;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.security.PrivateKey;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class OldChainV35IsolationTest {
    private static final long LEGACY_TEST_INDEX = International_Trade_Union.protocol.v35.V35TestMode.preForkIndex();
    private static final String LEGACY_MINER = "legacy-miner-address";
    private static final String LEGACY_SENDER = "legacy-sender-address";
    private static final String LEGACY_RECIPIENT = "legacy-recipient-address";

    @Test
    public void productionForkConstantsKeepNormalHeightsOnLegacyPath() {
        International_Trade_Union.protocol.v35.V35TestMode.assertConfiguredForkSafety();
        assertEquals("BURN_ADDRESS", Seting.V35_BURN_ADDRESS);

        assertFalse(ProtocolVersion.isV35(0L));
        assertFalse(ProtocolVersion.isV35(LEGACY_TEST_INDEX));
        assertFalse(ProtocolVersion.isV35(International_Trade_Union.protocol.v35.V35TestMode.preForkIndex()));
        assertTrue(ProtocolVersion.isLegacy(LEGACY_TEST_INDEX));

        assertFalse(JackpotCalculator.isV35JackpotPayoutAllowedAt(LEGACY_TEST_INDEX));
        assertFalse(BurnAddressPolicy.isNoSpendActive(LEGACY_TEST_INDEX));
        assertFalse(BurnAddressPolicy.isSpendForbidden(LEGACY_TEST_INDEX, Seting.V35_BURN_ADDRESS));
    }

    @Test
    public void legacyFounderAndMinerRewardsAreStillAccountedBeforeV35() throws Exception {
        List<DtoTransaction> rewards = new ArrayList<>();
        rewards.add(signedBasisReward(LEGACY_MINER, 100.00, 100.00));
        rewards.add(signedBasisReward(Seting.ADDRESS_FOUNDER, 50.00, 50.00));
        Block legacyBlock = block(1L, rewards, LEGACY_MINER);

        Map<String, Account> balances = new HashMap<>();
        balances.put(Seting.BASIS_ADDRESS, account(Seting.BASIS_ADDRESS, "0.00"));

        Map<String, Account> updated = UtilsBalance.calculateBalance(
                balances, legacyBlock, new ArrayList<>(), new ArrayList<>());

        assertEquals(0, new BigDecimal("100.00")
                .compareTo(updated.get(LEGACY_MINER).getDigitalDollarBalance()));
        assertEquals(0, new BigDecimal("50.00")
                .compareTo(updated.get(Seting.ADDRESS_FOUNDER).getDigitalDollarBalance()));
        assertFalse(ProtocolVersion.isV35(legacyBlock.getIndex()));
    }

    @Test
    public void v35RewardShapeIsNotRequiredForLegacyRewardFixtures() {
        assertEquals(6.00, V35RewardCalculator.getInstantMinerGoldReward(), 0.00000001);
        assertEquals(0.00, V35RewardCalculator.getFounderGoldReward(), 0.00000001);
        assertFalse(V35RewardCalculator.isValidInstantMinerReward(100.00, 100.00));
        assertFalse(ProtocolVersion.isV35(LEGACY_TEST_INDEX));
    }

    @Test
    public void burnAddressRecipientIsAllowedByLegacyTransactionPoolBalancePath() throws Exception {
        Map<String, Account> balances = new HashMap<>();
        balances.put(LEGACY_SENDER, account(LEGACY_SENDER, "10.00"));

        DtoTransaction toBurnAddress = unsignedUserTransaction(LEGACY_SENDER, Seting.V35_BURN_ADDRESS, 2.00);
        List<DtoTransaction> accepted = UtilsUse.balanceTransaction(
                Collections.singletonList(toBurnAddress), balances, LEGACY_TEST_INDEX);

        assertEquals(1, accepted.size());
        assertEquals(0, new BigDecimal("2.00")
                .compareTo(balances.get(Seting.V35_BURN_ADDRESS).getDigitalDollarBalance()));
        assertFalse(BurnAddressPolicy.isNoSpendActive(LEGACY_TEST_INDEX));
    }

    @Test
    public void burnAddressSenderIsNotBlockedByV35NoSpendBeforeForkGate() throws Exception {
        Map<String, Account> balances = new HashMap<>();
        balances.put(Seting.V35_BURN_ADDRESS, account(Seting.V35_BURN_ADDRESS, "10.00"));

        DtoTransaction fromBurnAddress = unsignedUserTransaction(Seting.V35_BURN_ADDRESS, LEGACY_RECIPIENT, 1.00);
        List<DtoTransaction> accepted = UtilsUse.balanceTransaction(
                Collections.singletonList(fromBurnAddress), balances, LEGACY_TEST_INDEX);

        assertEquals(1, accepted.size());
        assertEquals(0, new BigDecimal("1.00")
                .compareTo(balances.get(LEGACY_RECIPIENT).getDigitalDollarBalance()));
        assertFalse(BurnAddressPolicy.isSpendForbidden(LEGACY_TEST_INDEX, Seting.V35_BURN_ADDRESS));
    }

    @Test
    public void jackpotLookingBasisAmountDoesNotRequireV35AccountingBeforeFork() {
        DtoTransaction jackpotLookingReward = unsignedBasisReward(
                LEGACY_MINER,
                JackpotCalculator.expectedPayoutAmount(JackpotCalculator.JackpotPeriod.DAILY).doubleValue(),
                0.00);
        Block legacyBlock = block(LEGACY_TEST_INDEX, Collections.singletonList(jackpotLookingReward), LEGACY_MINER);

        assertTrue(UtilsResolving.containsV35JackpotPayoutCandidate(legacyBlock));
        assertFalse(UtilsResolving.isV35JackpotAccountingRequired(legacyBlock));
        assertFalse(UtilsResolving.requiresV35JackpotAccounting(Collections.singletonList(legacyBlock)));
        assertFalse(JackpotCalculator.isV35JackpotPayoutAllowedAt(legacyBlock.getIndex()));
    }

    @Test
    public void cumulativeWorkDoesNotAccumulateForLegacyBlocksBeforeFork() {
        Block legacyBlock = block(LEGACY_TEST_INDEX, Collections.emptyList(), LEGACY_MINER);
        BigInteger startingWork = BigInteger.valueOf(1234L);

        assertEquals(BigInteger.ZERO, Blockchain.v35Work(legacyBlock));
        assertEquals(startingWork, Blockchain.addV35Work(startingWork, legacyBlock));
        assertFalse(ProtocolVersion.isV35(legacyBlock.getIndex()));
    }

    private static DtoTransaction signedBasisReward(String customer,
                                                    double digitalDollar,
                                                    double digitalStock) throws Exception {
        DtoTransaction reward = unsignedBasisReward(customer, digitalDollar, digitalStock);
        reward.setSign(UtilsSecurity.sign(basisPrivateKey(), reward.toSign()));
        return reward;
    }

    private static DtoTransaction unsignedBasisReward(String customer,
                                                      double digitalDollar,
                                                      double digitalStock) {
        DtoTransaction reward = new DtoTransaction(
                Seting.BASIS_ADDRESS,
                customer,
                digitalDollar,
                digitalStock,
                new Laws(),
                0.0,
                VoteEnum.YES);
        reward.setSign(new byte[]{1, 2, 3});
        return reward;
    }

    private static DtoTransaction unsignedUserTransaction(String sender, String customer, double digitalDollar) {
        DtoTransaction transaction = new DtoTransaction(
                sender,
                customer,
                digitalDollar,
                0.0,
                new Laws(),
                0.0,
                VoteEnum.YES);
        transaction.setSign(new byte[]{4, 5, 6});
        return transaction;
    }

    private static PrivateKey basisPrivateKey() throws Exception {
        Base base = new Base58();
        return UtilsSecurity.privateBytToPrivateKey(base.decode(Seting.BASIS_PASSWORD));
    }

    private static Account account(String address, String gold) {
        return new Account(address, new BigDecimal(gold), BigDecimal.ZERO, BigDecimal.ZERO);
    }

    private static Block block(long index, List<DtoTransaction> transactions, String minerAddress) {
        return new Block(
                new ArrayList<>(transactions),
                "previous-" + index,
                minerAddress,
                Seting.ADDRESS_FOUNDER,
                0L,
                0.0,
                1L,
                new Timestamp(index * 1000L),
                index,
                "hash-" + index);
    }
}
