package International_Trade_Union.protocol.v35;

import International_Trade_Union.setings.Seting;

/**
 * Display-only asset mapper. It must not be used to change transaction or balance formats.
 */
public final class AssetDisplayMapper {
    private AssetDisplayMapper() {
    }

    public static double rawDigitalDollarToGoldDisplay(double raw) {
        return raw;
    }

    public static double rawDigitalStockToSilverDisplay(double raw) {
        return raw * Seting.V35_SILVER_DISPLAY_MULTIPLIER;
    }

    public static double silverDisplayToRawDigitalStock(double silver) {
        return silver / Seting.V35_SILVER_DISPLAY_MULTIPLIER;
    }
}
