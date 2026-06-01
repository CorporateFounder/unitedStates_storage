package International_Trade_Union.protocol.v35;

import International_Trade_Union.setings.Seting;

/**
 * V35 burn-address policy. The configured burn address is not final while Seting contains
 * an internal placeholder, and no-spend enforcement remains inactive while the fork index is
 * Long.MAX_VALUE.
 */
public final class BurnAddressPolicy {
    public static final String PLACEHOLDER_BURN_ADDRESS = "TODO_V35_BURN_ADDRESS";
    public static final String INTERNAL_DEFAULT_BURN_ADDRESS = "BURN_ADDRESS";
    private static final String PUBLICATION_WARNING =
            "Official public burn address is not published yet; BURN_ADDRESS is an internal placeholder, not a public transfer destination.";

    private BurnAddressPolicy() {
    }

    public static String getBurnAddress() {
        return Seting.V35_BURN_ADDRESS;
    }

    public static boolean isPlaceholderBurnAddress() {
        return isPlaceholderBurnAddress(getBurnAddress());
    }

    public static boolean isPlaceholderBurnAddress(String burnAddress) {
        if (burnAddress == null || burnAddress.trim().isEmpty()) {
            return true;
        }
        String normalized = burnAddress.trim();
        return PLACEHOLDER_BURN_ADDRESS.equals(normalized)
                || INTERNAL_DEFAULT_BURN_ADDRESS.equals(normalized);
    }

    public static boolean isBurnAddressConfigured() {
        return !isPlaceholderBurnAddress();
    }

    public static boolean isPublicBurnAddressPublished() {
        return isBurnAddressConfigured();
    }

    public static String publicBurnAddressWarning() {
        return isPlaceholderBurnAddress() ? PUBLICATION_WARNING : "";
    }

    public static boolean isBurnAddress(String address) {
        return address != null && address.equals(getBurnAddress());
    }

    public static boolean isNoSpendActive(long index) {
        return isNoSpendActive(index, Seting.V35_RESCUE_FORK_INDEX);
    }

    public static boolean isNoSpendActive(long index, long forkIndex) {
        return forkIndex != Long.MAX_VALUE && index >= forkIndex;
    }

    public static boolean canSpendFromAddress(long index, String address) {
        return canSpendFromAddress(index, address, Seting.V35_RESCUE_FORK_INDEX);
    }

    public static boolean canSpendFromAddress(long index, String address, long forkIndex) {
        return !isSpendForbidden(index, address, forkIndex);
    }

    public static boolean isSpendForbidden(long index, String address) {
        return isSpendForbidden(index, address, Seting.V35_RESCUE_FORK_INDEX);
    }

    public static boolean isSpendForbidden(long index, String address, long forkIndex) {
        return isNoSpendActive(index, forkIndex) && isBurnAddress(address);
    }
}
