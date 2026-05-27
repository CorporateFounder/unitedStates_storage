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

    @Test
    public void bundledServerSampleChainLoadsAndValidatesReadOnly() throws Exception {
        assertEquals(Long.MAX_VALUE, Seting.V35_RESCUE_FORK_INDEX);
        assertEquals("BURN_ADDRESS", Seting.V35_BURN_ADDRESS);

        Path samplePath = Path.of("src", "main", "resources", "blockchain").toAbsolutePath().normalize();
        assertTrue(Files.isDirectory(samplePath), "Server sample chain resource directory must exist");

        List<Block> blocks = UtilsBlock.readLineObject(samplePath.toString());
        assertEquals(4, blocks.size(), "Bundled server sample chain currently contains four block lines");
        assertEquals(1L, blocks.get(0).getIndex());
        assertEquals(3L, blocks.get(blocks.size() - 1).getIndex());
        assertTrue(blocks.stream().noneMatch(block -> ProtocolVersion.isV35(block.getIndex())),
                "Bundled sample chain must stay below the disabled V35 fork gate");
        assertTrue(blocks.stream().noneMatch(block -> BurnAddressPolicy.isNoSpendActive(block.getIndex())),
                "Burn-address no-spend must remain inactive for bundled sample heights");

        DataShortBlockchainInformation result = Blockchain.checkFromFile(samplePath.toString());

        assertTrue(result.isValidation(), "Bundled server sample chain must validate");
        assertEquals(blocks.size(), result.getSize(), "Replay summary size must match loaded block count");
        assertEquals(DataShortBlockchainInformation.DEFAULT_CUMULATIVE_WORK, result.getCumulativeWork(),
                "V35 cumulative work must not accrue while the fork gate is disabled");
    }

    @Test
    public void block301393SelfStakingTransactionsRequirePreBlockBalanceContext() throws Exception {
        assertEquals(Long.MAX_VALUE, Seting.V35_RESCUE_FORK_INDEX);
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
        assertEquals(Long.MAX_VALUE, Seting.V35_RESCUE_FORK_INDEX);
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
    public void optionalExternalHistoricalChainPathCanBeValidatedWhenProvided() throws Exception {
        assertEquals(Long.MAX_VALUE, Seting.V35_RESCUE_FORK_INDEX);
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
}
