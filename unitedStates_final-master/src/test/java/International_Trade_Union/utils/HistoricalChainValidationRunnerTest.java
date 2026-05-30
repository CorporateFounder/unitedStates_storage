package International_Trade_Union.utils;

import International_Trade_Union.entity.blockchain.DataShortBlockchainInformation;
import International_Trade_Union.entity.blockchain.Blockchain;
import International_Trade_Union.entity.DtoTransaction.DtoTransaction;
import International_Trade_Union.entity.blockchain.block.Block;
import International_Trade_Union.model.Account;
import International_Trade_Union.protocol.v35.BurnAddressPolicy;
import International_Trade_Union.protocol.v35.ProtocolVersion;
import International_Trade_Union.setings.Seting;
import International_Trade_Union.vote.Laws;
import International_Trade_Union.vote.VoteEnum;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class HistoricalChainValidationRunnerTest {
    private static final String CHAIN_PATH_PROPERTY = "citu.chain.path";
    private static final String BLOCK_301393_STAKING_SENDER = "qDWgdtEBzbSGs7AdUVLYEiBP5Bs7nb3m3L8GaBGgbSEP";
    private static final String BLOCK_301409_STAKING_SENDER = "28eNM7pFKzTgYb8WyUHgttJK9v9oV3TH4jYMxB1xXAK4o";
    private static final String BLOCK_331090_DUST_SENDER = "hK6XwB7LxM5wkqbHV3WTpjhZG1TstggsxqEWMw8FnbGf";
    private static final String BLOCK_331090_DUST_RECIPIENT = "yfudXXYdHx1W9QBZuDubr5ZjiqmdTMuKYkMdJyZYmevc";
    private static final String BLOCK_335680_DRIFT_SENDER = "otw9wf3DrtiiT1AwiMryu7DYyE6gPTfzd8v9tDmQdWJw";
    private static final String BLOCK_335680_DRIFT_RECIPIENT = "mLKZnGvEh1kefCZrq2WHwANUrpmP6ejbZsGRQCNWkxvx";
    private static final String BLOCK_341646_UNSTAKING_SENDER = "2A8vxijdyY5ST1WhLQan3N1P6wSdzBDo9VmEFhck9bArG";

    @Test
    public void block301393SelfStakingTransactionsRequirePreBlockBalanceContext() throws Exception {
        International_Trade_Union.protocol.v35.V35TestMode.assertConfiguredForkSafety();
        assertEquals("BURN_ADDRESS", Seting.V35_BURN_ADDRESS);
        assertFalse(ProtocolVersion.isV35(301393L));
        assertFalse(BurnAddressPolicy.isNoSpendActive(301393L));

        List<DtoTransaction> stakingTransactions = List.of(
                stakingTransaction(60.0, new byte[]{1}),
                stakingTransaction(1000.0, new byte[]{2})
        );

        Map<String, Account> preBlockBalances = new HashMap<>();
        preBlockBalances.put(BLOCK_301393_STAKING_SENDER,
                new Account(BLOCK_301393_STAKING_SENDER, new BigDecimal("1060.0"), BigDecimal.ZERO, BigDecimal.ZERO));

        List<DtoTransaction> acceptedFromPreBlock = UtilsUse.balanceTransaction(
                stakingTransactions, preBlockBalances, 301393L);

        assertEquals(2, acceptedFromPreBlock.size(),
                "Both block 301393 self-staking transactions are valid against the pre-block balance context");

        Map<String, Account> postBlockBalances = new HashMap<>();
        postBlockBalances.put(BLOCK_301393_STAKING_SENDER,
                new Account(BLOCK_301393_STAKING_SENDER, BigDecimal.ZERO, BigDecimal.ZERO, new BigDecimal("1060.0")));

        List<DtoTransaction> acceptedFromPostBlock = UtilsUse.balanceTransaction(
                stakingTransactions, postBlockBalances, 301393L);

        assertEquals(0, acceptedFromPostBlock.size(),
                "Revalidating the same staking transactions against post-block balances reproduces the replay failure");
    }

    @Test
    public void insufficientSelfStakingStillFailsLegacyBalanceValidation() throws Exception {
        Map<String, Account> balances = new HashMap<>();
        balances.put(BLOCK_301393_STAKING_SENDER,
                new Account(BLOCK_301393_STAKING_SENDER, new BigDecimal("59.99"), BigDecimal.ZERO, BigDecimal.ZERO));

        List<DtoTransaction> accepted = UtilsUse.balanceTransaction(
                List.of(stakingTransaction(60.0, new byte[]{3})), balances, 301393L);

        assertEquals(0, accepted.size(), "The replay-ordering fix must not weaken insufficient-balance checks");
    }

    @Test
    public void block301409LegacySelfStakingDeclarationIsAcceptedOnlyForHistoricalReplay() throws Exception {
        International_Trade_Union.protocol.v35.V35TestMode.assertConfiguredForkSafety();
        assertEquals("BURN_ADDRESS", Seting.V35_BURN_ADDRESS);
        assertFalse(ProtocolVersion.isV35(301409L));
        assertFalse(BurnAddressPolicy.isNoSpendActive(301409L));

        DtoTransaction stakingDeclaration = stakingTransaction(BLOCK_301409_STAKING_SENDER, 1_400_000.0, new byte[]{4});
        Map<String, Account> balances = new HashMap<>();
        balances.put(BLOCK_301409_STAKING_SENDER,
                new Account(
                        BLOCK_301409_STAKING_SENDER,
                        new BigDecimal("71365.20885803517"),
                        new BigDecimal("1412196.2788580486"),
                        new BigDecimal("1370831.0700000033")));

        List<DtoTransaction> standardAccepted = UtilsUse.balanceTransaction(
                List.of(stakingDeclaration), UtilsUse.balancesClone(balances), 301409L);
        List<DtoTransaction> historicalAccepted = UtilsUse.balanceTransactionForHistoricalReplay(
                List.of(stakingDeclaration), UtilsUse.balancesClone(balances), 301409L);

        assertEquals(0, standardAccepted.size(),
                "Live balance validation still requires enough spendable digital dollars for STAKING");
        assertEquals(1, historicalAccepted.size(),
                "Historical replay accepts legacy self-STAKING declarations backed by current stake plus spendable balance");
    }

    @Test
    public void historicalReplaySelfStakingStillRejectsTrulyInsufficientStakeDeclaration() throws Exception {
        Map<String, Account> balances = new HashMap<>();
        balances.put(BLOCK_301409_STAKING_SENDER,
                new Account(BLOCK_301409_STAKING_SENDER, new BigDecimal("10.00"), BigDecimal.ZERO, new BigDecimal("20.00")));

        DtoTransaction impossibleDeclaration = stakingTransaction(BLOCK_301409_STAKING_SENDER, 100.0, new byte[]{5});
        List<DtoTransaction> accepted = UtilsUse.balanceTransactionForHistoricalReplay(
                List.of(impossibleDeclaration), balances, 301409L);

        assertEquals(0, accepted.size(), "Historical replay must not accept self-STAKING declarations without sufficient declared stake coverage");
    }

    @Test
    public void block331090TinyPrecisionDustTransferIsAcceptedOnlyForHistoricalReplay() throws Exception {
        International_Trade_Union.protocol.v35.V35TestMode.assertConfiguredForkSafety();
        assertEquals("BURN_ADDRESS", Seting.V35_BURN_ADDRESS);
        assertFalse(ProtocolVersion.isV35(331090L));
        assertFalse(BurnAddressPolicy.isNoSpendActive(331090L));

        DtoTransaction dustTransfer = yesTransaction(
                BLOCK_331090_DUST_SENDER,
                BLOCK_331090_DUST_RECIPIENT,
                805_179.0,
                764_179.0,
                new byte[]{6});
        Map<String, Account> balances = new HashMap<>();
        balances.put(BLOCK_331090_DUST_SENDER,
                new Account(
                        BLOCK_331090_DUST_SENDER,
                        new BigDecimal("805178.9999999844"),
                        new BigDecimal("764178.9999999857"),
                        BigDecimal.ZERO));
        balances.put(BLOCK_331090_DUST_RECIPIENT,
                new Account(
                        BLOCK_331090_DUST_RECIPIENT,
                        new BigDecimal("679920.1999999811"),
                        new BigDecimal("645932.1999999819"),
                        BigDecimal.ZERO));

        List<DtoTransaction> standardAccepted = UtilsUse.balanceTransaction(
                List.of(dustTransfer), UtilsUse.balancesClone(balances), 331090L);
        List<DtoTransaction> historicalAccepted = UtilsUse.balanceTransactionForHistoricalReplay(
                List.of(dustTransfer), UtilsUse.balancesClone(balances), 331090L);

        assertEquals(0, standardAccepted.size(),
                "Live balance validation remains strict for the tiny dust shortage");
        assertEquals(1, historicalAccepted.size(),
                "Historical replay accepts bounded legacy precision dust");
    }

    @Test
    public void historicalReplayPrecisionDustStillRejectsMeaningfulInsufficientYesTransfer() throws Exception {
        DtoTransaction transfer = yesTransaction("meaningful-shortage-sender", "meaningful-shortage-recipient", 100.0, 100.0, new byte[]{7});
        Map<String, Account> balances = new HashMap<>();
        balances.put("meaningful-shortage-sender",
                new Account("meaningful-shortage-sender", new BigDecimal("99.999"), new BigDecimal("99.999"), BigDecimal.ZERO));
        balances.put("meaningful-shortage-recipient",
                new Account("meaningful-shortage-recipient", BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO));

        List<DtoTransaction> historicalAccepted = UtilsUse.balanceTransactionForHistoricalReplay(
                List.of(transfer), UtilsUse.balancesClone(balances), 331090L);

        assertEquals(0, historicalAccepted.size(),
                "Historical replay precision handling must not accept meaningful insufficient-balance transfers");
    }

    @Test
    public void block335680LegacyCentDriftTransferIsAcceptedOnlyForHistoricalReplay() throws Exception {
        International_Trade_Union.protocol.v35.V35TestMode.assertConfiguredForkSafety();
        assertEquals("BURN_ADDRESS", Seting.V35_BURN_ADDRESS);
        assertFalse(ProtocolVersion.isV35(335680L));
        assertFalse(BurnAddressPolicy.isNoSpendActive(335680L));

        DtoTransaction driftTransfer = yesTransaction(
                BLOCK_335680_DRIFT_SENDER,
                BLOCK_335680_DRIFT_RECIPIENT,
                68169.77,
                0.01,
                new byte[]{8});
        Map<String, Account> balances = new HashMap<>();
        balances.put(BLOCK_335680_DRIFT_SENDER,
                new Account(
                        BLOCK_335680_DRIFT_SENDER,
                        new BigDecimal("68169.74710190214898"),
                        new BigDecimal("2369169.73710190214898"),
                        new BigDecimal("2300000.0")));
        balances.put(BLOCK_335680_DRIFT_RECIPIENT,
                new Account(BLOCK_335680_DRIFT_RECIPIENT, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO));

        List<DtoTransaction> standardAccepted = UtilsUse.balanceTransaction(
                List.of(driftTransfer), UtilsUse.balancesClone(balances), 335680L);
        List<DtoTransaction> historicalAccepted = UtilsUse.balanceTransactionForHistoricalReplay(
                List.of(driftTransfer), UtilsUse.balancesClone(balances), 335680L);

        assertEquals(0, standardAccepted.size(),
                "Live balance validation remains strict for the legacy replay cent drift");
        assertEquals(1, historicalAccepted.size(),
                "Historical replay accepts bounded legacy cent drift when stock is fully covered");
    }

    @Test
    public void historicalReplayCentDriftStillRejectsStockShortage() throws Exception {
        DtoTransaction transfer = yesTransaction("stock-shortage-sender", "stock-shortage-recipient", 100.02, 0.01, new byte[]{10});
        Map<String, Account> balances = new HashMap<>();
        balances.put("stock-shortage-sender",
                new Account("stock-shortage-sender", new BigDecimal("100.00"), new BigDecimal("0.009"), BigDecimal.ZERO));
        balances.put("stock-shortage-recipient",
                new Account("stock-shortage-recipient", BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO));

        List<DtoTransaction> historicalAccepted = UtilsUse.balanceTransactionForHistoricalReplay(
                List.of(transfer), UtilsUse.balancesClone(balances), 335680L);

        assertEquals(0, historicalAccepted.size(),
                "Historical replay cent-drift handling must not accept insufficient-stock YES/NO transfers");
    }

    @Test
    public void block341646LegacySelfUnstakingIsAcceptedOnlyForHistoricalReplay() throws Exception {
        International_Trade_Union.protocol.v35.V35TestMode.assertConfiguredForkSafety();
        assertEquals("BURN_ADDRESS", Seting.V35_BURN_ADDRESS);
        assertFalse(ProtocolVersion.isV35(341646L));
        assertFalse(BurnAddressPolicy.isNoSpendActive(341646L));

        DtoTransaction unstakingDeclaration = unstakingTransaction(BLOCK_341646_UNSTAKING_SENDER, 1_340_000.0, new byte[]{11});
        Map<String, Account> balances = new HashMap<>();
        balances.put(BLOCK_341646_UNSTAKING_SENDER,
                new Account(
                        BLOCK_341646_UNSTAKING_SENDER,
                        new BigDecimal("125562.28003514555213"),
                        new BigDecimal("1352479.11003514555213"),
                        new BigDecimal("1330000.0")));

        List<DtoTransaction> standardAccepted = UtilsUse.balanceTransaction(
                List.of(unstakingDeclaration), UtilsUse.balancesClone(balances), 341646L);
        List<DtoTransaction> historicalAccepted = UtilsUse.balanceTransactionForHistoricalReplay(
                List.of(unstakingDeclaration), UtilsUse.balancesClone(balances), 341646L);

        assertEquals(0, standardAccepted.size(),
                "Live balance validation remains strict for legacy self-UNSTAKING");
        assertEquals(1, historicalAccepted.size(),
                "Historical replay accepts legacy self-UNSTAKING when spendable plus staking balance covers the declaration");
    }

    @Test
    public void historicalReplaySelfUnstakingStillRejectsTrulyInsufficientDeclaration() throws Exception {
        Map<String, Account> balances = new HashMap<>();
        balances.put("insufficient-unstaking-sender",
                new Account("insufficient-unstaking-sender", new BigDecimal("9999.99"), BigDecimal.ZERO, new BigDecimal("1330000.0")));

        DtoTransaction impossibleDeclaration = unstakingTransaction("insufficient-unstaking-sender", 1_340_000.0, new byte[]{12});
        List<DtoTransaction> historicalAccepted = UtilsUse.balanceTransactionForHistoricalReplay(
                List.of(impossibleDeclaration), UtilsUse.balancesClone(balances), 341646L);

        assertEquals(0, historicalAccepted.size(),
                "Historical replay must not accept self-UNSTAKING declarations without combined balance coverage");
    }

    @Test
    public void replayCompatibilityCannotWeakenNormalLiveBalanceValidation() throws Exception {
        International_Trade_Union.protocol.v35.V35TestMode.assertConfiguredForkSafety();
        assertEquals("BURN_ADDRESS", Seting.V35_BURN_ADDRESS);
        assertFalse(ProtocolVersion.isV35(341646L));
        assertFalse(BurnAddressPolicy.isNoSpendActive(341646L));

        DtoTransaction dollarShortage = yesTransaction("live-dollar-shortage", "live-dollar-recipient", 100.0, 1.0, new byte[]{13});
        Map<String, Account> dollarBalances = new HashMap<>();
        dollarBalances.put("live-dollar-shortage", new Account("live-dollar-shortage", new BigDecimal("99.99999999"), new BigDecimal("10.0"), BigDecimal.ZERO));
        dollarBalances.put("live-dollar-recipient", new Account("live-dollar-recipient", BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO));
        assertEquals(0, UtilsUse.balanceTransaction(List.of(dollarShortage), dollarBalances, 331090L).size(),
                "Normal live validation must reject replay precision dust dollar shortages");

        DtoTransaction stockShortage = yesTransaction("live-stock-shortage", "live-stock-recipient", 1.0, 100.0, new byte[]{14});
        Map<String, Account> stockBalances = new HashMap<>();
        stockBalances.put("live-stock-shortage", new Account("live-stock-shortage", new BigDecimal("10.0"), new BigDecimal("99.99999999"), BigDecimal.ZERO));
        stockBalances.put("live-stock-recipient", new Account("live-stock-recipient", BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO));
        assertEquals(0, UtilsUse.balanceTransaction(List.of(stockShortage), stockBalances, 331090L).size(),
                "Normal live validation must reject replay precision dust stock shortages");

        DtoTransaction centDrift = yesTransaction("live-cent-drift", "live-cent-recipient", 100.02, 0.01, new byte[]{15});
        Map<String, Account> centBalances = new HashMap<>();
        centBalances.put("live-cent-drift", new Account("live-cent-drift", new BigDecimal("100.00"), new BigDecimal("1.0"), BigDecimal.ZERO));
        centBalances.put("live-cent-recipient", new Account("live-cent-recipient", BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO));
        assertEquals(0, UtilsUse.balanceTransaction(List.of(centDrift), centBalances, 335680L).size(),
                "Normal live validation must reject replay-only cent drift shortages");

        DtoTransaction selfStaking = stakingTransaction("live-self-staking", 100.0, new byte[]{16});
        Map<String, Account> stakingCompatibilityBalances = new HashMap<>();
        stakingCompatibilityBalances.put("live-self-staking", new Account("live-self-staking", new BigDecimal("10.0"), BigDecimal.ZERO, new BigDecimal("100.0")));
        assertEquals(0, UtilsUse.balanceTransaction(List.of(selfStaking), UtilsUse.balancesClone(stakingCompatibilityBalances), 301409L).size(),
                "Normal live validation must reject replay-only self-STAKING compatibility");

        Map<String, Account> normallyValidStakingBalances = new HashMap<>();
        normallyValidStakingBalances.put("live-self-staking", new Account("live-self-staking", new BigDecimal("100.0"), BigDecimal.ZERO, BigDecimal.ZERO));
        assertEquals(1, UtilsUse.balanceTransaction(List.of(selfStaking), normallyValidStakingBalances, 301409L).size(),
                "Normal live validation must still accept self-STAKING when spendable balance is sufficient");

        DtoTransaction selfUnstaking = unstakingTransaction("live-self-unstaking", 100.0, new byte[]{17});
        Map<String, Account> unstakingCompatibilityBalances = new HashMap<>();
        unstakingCompatibilityBalances.put("live-self-unstaking", new Account("live-self-unstaking", new BigDecimal("100.0"), BigDecimal.ZERO, new BigDecimal("90.0")));
        assertEquals(0, UtilsUse.balanceTransaction(List.of(selfUnstaking), UtilsUse.balancesClone(unstakingCompatibilityBalances), 341646L).size(),
                "Normal live validation must reject replay-only self-UNSTAKING compatibility");

        Map<String, Account> normallyValidUnstakingBalances = new HashMap<>();
        normallyValidUnstakingBalances.put("live-self-unstaking", new Account("live-self-unstaking", BigDecimal.ZERO, BigDecimal.ZERO, new BigDecimal("100.0")));
        assertEquals(1, UtilsUse.balanceTransaction(List.of(selfUnstaking), normallyValidUnstakingBalances, 341646L).size(),
                "Normal live validation must still accept self-UNSTAKING when staking balance is sufficient");
    }

    @Test
    public void optionalExternalHistoricalChainPathCanBeValidatedWhenProvided() throws Exception {
        International_Trade_Union.protocol.v35.V35TestMode.assertConfiguredForkSafety();
        assertEquals("BURN_ADDRESS", Seting.V35_BURN_ADDRESS);

        String configuredPath = System.getProperty(CHAIN_PATH_PROPERTY);
        if (configuredPath == null || configuredPath.trim().isEmpty()) {
            return;
        }

        Path chainPath = Path.of(configuredPath).toAbsolutePath().normalize();
        assertTrue(Files.isDirectory(chainPath), "Configured historical chain path must be a directory");

        List<Block> blocks = UtilsBlock.readLineObject(chainPath.toString());
        assertFalse(blocks.isEmpty(), "Configured historical chain path must contain block JSON lines");
        assertTrue(blocks.stream().noneMatch(block -> ProtocolVersion.isV35(block.getIndex())),
                "Historical replay path must stay below the disabled V35 fork gate");
        assertTrue(blocks.stream().noneMatch(block -> BurnAddressPolicy.isNoSpendActive(block.getIndex())),
                "Burn-address no-spend must remain inactive for historical replay heights");

        DataShortBlockchainInformation result = Blockchain.checkFromFile(chainPath.toString());

        assertTrue(result.isValidation(), "Historical chain replay must validate");
        assertEquals(blocks.size(), result.getSize(), "Replay summary size must match loaded block count");
        assertEquals(DataShortBlockchainInformation.DEFAULT_CUMULATIVE_WORK, result.getCumulativeWork(),
                "V35 cumulative work must not accrue while the fork gate is disabled");
    }

    private static DtoTransaction stakingTransaction(double amount, byte[] sign) {
        return stakingTransaction(BLOCK_301393_STAKING_SENDER, amount, sign);
    }

    private static DtoTransaction stakingTransaction(String address, double amount, byte[] sign) {
        DtoTransaction transaction = new DtoTransaction(
                address,
                address,
                amount,
                0.0,
                new Laws(),
                0.0,
                VoteEnum.STAKING);
        transaction.setSign(sign);
        return transaction;
    }

    private static DtoTransaction unstakingTransaction(String address, double amount, byte[] sign) {
        DtoTransaction transaction = new DtoTransaction(
                address,
                address,
                amount,
                0.0,
                new Laws(),
                0.0,
                VoteEnum.UNSTAKING);
        transaction.setSign(sign);
        return transaction;
    }

    private static DtoTransaction yesTransaction(String sender, String customer, double dollar, double stock, byte[] sign) {
        DtoTransaction transaction = new DtoTransaction(
                sender,
                customer,
                dollar,
                stock,
                new Laws(),
                0.0,
                VoteEnum.YES);
        transaction.setSign(sign);
        return transaction;
    }
}
