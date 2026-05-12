package International_Trade_Union.protocol.v35;

import International_Trade_Union.setings.Seting;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class BurnAddressPolicyTest {
    @Test
    public void exposesPlaceholderAndPureSpendPolicy() {
        String burnAddress = Seting.V35_BURN_ADDRESS;

        assertEquals(burnAddress, BurnAddressPolicy.getBurnAddress());
        assertTrue(BurnAddressPolicy.isBurnAddress(burnAddress));
        assertFalse(BurnAddressPolicy.isBurnAddress("normal-address"));
        assertTrue(BurnAddressPolicy.canSpendFromAddress(0, "normal-address"));
        assertFalse(BurnAddressPolicy.canSpendFromAddress(Long.MAX_VALUE, burnAddress));
    }
}
