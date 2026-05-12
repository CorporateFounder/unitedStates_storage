package International_Trade_Union.protocol.v35;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class AssetDisplayMapperTest {
    private static final double DELTA = 0.00000001;

    @Test
    public void mapsGoldAndSilverDisplayOnly() {
        assertEquals(10.00, AssetDisplayMapper.rawDigitalDollarToGoldDisplay(10.00), DELTA);
        assertEquals(1000.00, AssetDisplayMapper.rawDigitalStockToSilverDisplay(10.00), DELTA);
        assertEquals(10.00, AssetDisplayMapper.silverDisplayToRawDigitalStock(1000.00), DELTA);
        assertEquals(100.00, AssetDisplayMapper.rawDigitalStockToSilverDisplay(1.00), DELTA);
    }
}
