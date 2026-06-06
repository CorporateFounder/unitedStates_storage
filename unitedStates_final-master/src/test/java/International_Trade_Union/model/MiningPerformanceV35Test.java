package International_Trade_Union.model;

import International_Trade_Union.entity.blockchain.block.Block;
import International_Trade_Union.protocol.v35.V35PowValidator;
import International_Trade_Union.protocol.v35.V35TargetDifficulty;
import International_Trade_Union.setings.Seting;
import org.junit.jupiter.api.Test;

import java.math.BigInteger;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MiningPerformanceV35Test {

    @Test
    void preV35MiningThreadCountUsesSafeMultiThreadDefault() {
        assertTrue(Mining.defaultMiningThreadCount() >= 1);
        assertEquals(Math.max(1, Runtime.getRuntime().availableProcessors() - 1), Mining.defaultMiningThreadCount());
    }

    @Test
    void v35MiningUsesMultiThreadedNonceSearchAndProducesValidBlock() throws Exception {
        long easyCompactTarget = V35TargetDifficulty.encodeCompactTarget(BigInteger.ONE.shiftLeft(256).subtract(BigInteger.ONE));

        Block block = Mining.mineV35Block(
                Collections.emptyList(),
                "previous-hash",
                "miner-address",
                "founder-address",
                0.0,
                easyCompactTarget,
                Seting.V35_RESCUE_FORK_INDEX,
                2
        );

        assertEquals(2, Mining.getLastMiningThreadCount());
        assertTrue(V35PowValidator.isHashUnderTarget(
                block.getHashBlock(),
                V35TargetDifficulty.decodeCompactTarget(easyCompactTarget)
        ));
        assertFalse(Mining.getLastHashRateDisplay().contains("temporarily unavailable"));
    }

    @Test
    void hashRateFormattingUsesAttemptsAndWallClockTime() {
        assertEquals("500.00 H/s", Mining.formatHashRate(500, 1_000_000_000L));
        assertEquals("1.50 KH/s", Mining.formatHashRate(1500, 1_000_000_000L));
        assertEquals("2.00 MH/s", Mining.formatHashRate(2_000_000, 1_000_000_000L));
    }
}
