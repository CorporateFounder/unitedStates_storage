package International_Trade_Union.protocol.v35;

import International_Trade_Union.setings.Seting;

/**
 * Placeholder burn-address policy. Validation enforcement is intentionally not wired yet.
 */
public final class BurnAddressPolicy {
    private BurnAddressPolicy() {
    }

    public static String getBurnAddress() {
        return Seting.V35_BURN_ADDRESS;
    }

    public static boolean isBurnAddress(String address) {
        return address != null && address.equals(getBurnAddress());
    }

    public static boolean canSpendFromAddress(long index, String address) {
        return !(ProtocolVersion.isV35(index) && isBurnAddress(address));
    }
}
