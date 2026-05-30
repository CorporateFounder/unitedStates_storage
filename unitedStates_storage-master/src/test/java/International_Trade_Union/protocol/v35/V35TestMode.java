package International_Trade_Union.protocol.v35;

import International_Trade_Union.setings.Seting;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public final class V35TestMode {
    private V35TestMode() {
    }

    public static void assertConfiguredForkSafety() {
        assertEquals("BURN_ADDRESS", Seting.V35_BURN_ADDRESS);
        assertEquals(200, Seting.V35_TARGET_BLOCK_TIME_SECONDS);
        assertEquals(8.00, Seting.V35_GOLD_REWARD, 0.00000001);
        assertEquals(6.00, Seting.V35_INSTANT_MINER_REWARD, 0.00000001);
        assertEquals(2.00, Seting.V35_JACKPOT_REWARD, 0.00000001);
        assertFalse(Seting.V35_FOUNDER_REWARD_ENABLED);
        assertEquals(0.00, Seting.V35_EXPECTED_FOUNDER_REWARD, 0.00000001);

        if (Seting.V35_RESCUE_FORK_INDEX == Long.MAX_VALUE) {
            assertFalse(ProtocolVersion.isV35(0L));
            return;
        }

        assertTrue(Seting.V35_RESCUE_FORK_INDEX > 0L);
        assertFalse(ProtocolVersion.isV35(Seting.V35_RESCUE_FORK_INDEX - 1L));
        assertTrue(ProtocolVersion.isV35(Seting.V35_RESCUE_FORK_INDEX));
    }

    public static long preForkIndex() {
        return Seting.V35_RESCUE_FORK_INDEX == Long.MAX_VALUE
                ? 1_000_000L
                : Seting.V35_RESCUE_FORK_INDEX - 1L;
    }

    public static String configuredForkIndexLabel() {
        return Seting.V35_RESCUE_FORK_INDEX == Long.MAX_VALUE
                ? "Long.MAX_VALUE"
                : Long.toString(Seting.V35_RESCUE_FORK_INDEX);
    }

    public static Object configuredForkIndexValue() {
        return Seting.V35_RESCUE_FORK_INDEX == Long.MAX_VALUE
                ? "Long.MAX_VALUE"
                : Seting.V35_RESCUE_FORK_INDEX;
    }
}
