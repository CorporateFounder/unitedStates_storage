package International_Trade_Union.protocol.v35;

import International_Trade_Union.setings.Seting;

/**
 * Read-only V35 target difficulty settings. Mining and validation integration is future work.
 */
public final class V35TargetDifficulty {
    private V35TargetDifficulty() {
    }

    public static int getTargetWindow() {
        return Seting.V35_TARGET_WINDOW;
    }

    public static int getTargetBlockTimeSeconds() {
        return Seting.V35_TARGET_BLOCK_TIME_SECONDS;
    }

    public static long getEmergencyDelaySeconds() {
        return Seting.V35_EMERGENCY_DELAY_SECONDS;
    }
}
