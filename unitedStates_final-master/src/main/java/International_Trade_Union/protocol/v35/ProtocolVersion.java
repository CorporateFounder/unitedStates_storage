package International_Trade_Union.protocol.v35;

import International_Trade_Union.setings.Seting;

/**
 * V35 fork switch helper. V35 remains disabled while the fork index is Long.MAX_VALUE.
 */
public final class ProtocolVersion {
    private ProtocolVersion() {
    }

    public static boolean isV35(long index) {
        return index >= Seting.V35_RESCUE_FORK_INDEX;
    }

    public static boolean isLegacy(long index) {
        return !isV35(index);
    }
}
