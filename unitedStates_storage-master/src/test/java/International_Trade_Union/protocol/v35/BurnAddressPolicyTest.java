package International_Trade_Union.protocol.v35;

import International_Trade_Union.setings.Seting;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class BurnAddressPolicyTest {
    @Test
    public void exposesPlaceholderAndKeepsNoSpendInactiveWhileV35Disabled() {
        String burnAddress = Seting.V35_BURN_ADDRESS;

        assertEquals(burnAddress, BurnAddressPolicy.getBurnAddress());
        assertTrue(BurnAddressPolicy.isPlaceholderBurnAddress());
        assertTrue(BurnAddressPolicy.isBurnAddress(burnAddress));
        assertFalse(BurnAddressPolicy.isBurnAddress("normal-address"));
        assertTrue(BurnAddressPolicy.canSpendFromAddress(0, "normal-address"));
        assertTrue(BurnAddressPolicy.canSpendFromAddress(Long.MAX_VALUE, burnAddress));
        assertFalse(BurnAddressPolicy.isNoSpendActive(Long.MAX_VALUE));
        assertFalse(BurnAddressPolicy.isSpendForbidden(Long.MAX_VALUE, burnAddress));
    }

    @Test
    public void rejectsBurnAddressSpendOnlyWhenExplicitForkIsActive() {
        String burnAddress = Seting.V35_BURN_ADDRESS;
        long simulatedForkIndex = 10L;

        assertTrue(BurnAddressPolicy.canSpendFromAddress(9L, burnAddress, simulatedForkIndex));
        assertFalse(BurnAddressPolicy.canSpendFromAddress(10L, burnAddress, simulatedForkIndex));
        assertTrue(BurnAddressPolicy.canSpendFromAddress(10L, "normal-address", simulatedForkIndex));
        assertTrue(BurnAddressPolicy.isSpendForbidden(10L, burnAddress, simulatedForkIndex));
        assertFalse(BurnAddressPolicy.isSpendForbidden(9L, burnAddress, simulatedForkIndex));
        assertFalse(BurnAddressPolicy.isSpendForbidden(10L, "normal-address", simulatedForkIndex));
    }
}
