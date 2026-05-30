package International_Trade_Union.entity.blockchain;

import International_Trade_Union.entity.blockchain.block.Block;
import International_Trade_Union.protocol.v35.V35TargetDifficulty;
import org.junit.jupiter.api.Test;

import java.math.BigInteger;
import java.sql.Timestamp;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class BlockchainV35MetadataTest {
    @Test
    public void v35WorkIsZeroWhileV35ForkIsDisabled() {
        Block block = block(International_Trade_Union.protocol.v35.V35TestMode.preForkIndex(), V35TargetDifficulty.getInitialCompactTarget(), "0001");

        assertEquals(BigInteger.ZERO, Blockchain.v35Work(block));
        assertEquals(BigInteger.TEN, Blockchain.addV35Work(BigInteger.TEN, block));
    }

    @Test
    public void latestTipHashUsesBlockHashWhenAvailable() {
        Block block = block(1L, 1L, "abcd");

        assertEquals("abcd", Blockchain.latestTipHash("", block));
    }

    @Test
    public void latestTipHashPreservesCurrentValueWhenBlockHashUnavailable() {
        Block block = block(1L, 1L, null);

        assertEquals("previous", Blockchain.latestTipHash("previous", block));
        assertEquals("previous", Blockchain.latestTipHash("previous", null));
    }

    private static Block block(long index, long hashCompexity, String hashBlock) {
        return new Block(
                Collections.emptyList(),
                "previous",
                "miner",
                "founder",
                0L,
                0.0,
                hashCompexity,
                new Timestamp(0L),
                index,
                hashBlock);
    }
}
