package International_Trade_Union.utils;

import International_Trade_Union.entity.blockchain.DataShortBlockchainInformation;
import International_Trade_Union.entity.blockchain.Blockchain;
import International_Trade_Union.entity.blockchain.block.Block;
import International_Trade_Union.protocol.v35.BurnAddressPolicy;
import International_Trade_Union.protocol.v35.ProtocolVersion;
import International_Trade_Union.setings.Seting;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class HistoricalChainValidationRunnerTest {
    private static final String CHAIN_PATH_PROPERTY = "citu.chain.path";

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
}
