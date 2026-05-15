package International_Trade_Union.entity.blockchain;

import International_Trade_Union.utils.UtilsJson;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.math.BigInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class DataShortBlockchainInformationTest {
    @Test
    public void defaultConstructionUsesSafeV35MetadataDefaults() {
        DataShortBlockchainInformation data = new DataShortBlockchainInformation();

        assertEquals(DataShortBlockchainInformation.DEFAULT_CUMULATIVE_WORK, data.getCumulativeWork());
        assertEquals(DataShortBlockchainInformation.DEFAULT_TIP_HASH, data.getTipHash());
    }

    @Test
    public void legacyConstructorPreservesOldFieldsAndDefaultsV35Metadata() {
        DataShortBlockchainInformation data = new DataShortBlockchainInformation(
                10L,
                true,
                20L,
                BigDecimal.valueOf(30),
                40L,
                50);

        assertEquals(10L, data.getSize());
        assertTrue(data.isValidation());
        assertEquals(20L, data.getHashCount());
        assertEquals(BigDecimal.valueOf(30), data.getStaking());
        assertEquals(40L, data.getTransactions());
        assertEquals(50, data.getBigRandomNumber());
        assertEquals("0", data.getCumulativeWork());
        assertEquals("", data.getTipHash());
    }

    @Test
    public void extendedConstructorStoresLargeCumulativeWorkAndTipHash() {
        String largeWork = BigInteger.ONE.shiftLeft(256).toString();
        String tipHash = "00000000000000000000000000000000000000000000000000000000000000ab";

        DataShortBlockchainInformation data = new DataShortBlockchainInformation(
                10L,
                true,
                20L,
                BigDecimal.valueOf(30),
                40L,
                50,
                largeWork,
                tipHash);

        assertEquals(largeWork, data.getCumulativeWork());
        assertEquals(tipHash, data.getTipHash());
    }

    @Test
    public void clonePreservesV35Metadata() throws CloneNotSupportedException {
        DataShortBlockchainInformation data = new DataShortBlockchainInformation(
                10L,
                true,
                20L,
                BigDecimal.valueOf(30),
                40L,
                50,
                "123456789012345678901234567890",
                "abcd");

        DataShortBlockchainInformation clone = data.clone();

        assertEquals(data.getCumulativeWork(), clone.getCumulativeWork());
        assertEquals(data.getTipHash(), clone.getTipHash());
        assertEquals(data.getHashCount(), clone.getHashCount());
    }

    @Test
    public void jsonRoundTripPreservesV35Metadata() throws Exception {
        DataShortBlockchainInformation data = new DataShortBlockchainInformation(
                10L,
                true,
                20L,
                BigDecimal.valueOf(30),
                40L,
                50,
                BigInteger.ONE.shiftLeft(200).toString(),
                "0001");

        String json = UtilsJson.objToStringJson(data);
        DataShortBlockchainInformation parsed = UtilsJson.jsonToDataShortBlockchainInformation(json);

        assertEquals(data.getCumulativeWork(), parsed.getCumulativeWork());
        assertEquals(data.getTipHash(), parsed.getTipHash());
        assertEquals(data.getTransactions(), parsed.getTransactions());
    }

    @Test
    public void oldJsonWithoutV35MetadataUsesDefaults() throws Exception {
        String oldJson = "{\"size\":10,\"validation\":true,\"hashCount\":20,\"staking\":30,\"transactions\":40,\"bigRandomNumber\":50}";

        DataShortBlockchainInformation parsed = UtilsJson.jsonToDataShortBlockchainInformation(oldJson);

        assertEquals("0", parsed.getCumulativeWork());
        assertEquals("", parsed.getTipHash());
        assertEquals(20L, parsed.getHashCount());
    }
}
