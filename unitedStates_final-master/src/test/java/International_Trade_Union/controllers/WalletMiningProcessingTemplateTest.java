package International_Trade_Union.controllers;

import International_Trade_Union.setings.Seting;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class WalletMiningProcessingTemplateTest {

    @Test
    void processingTemplateUsesModernWalletStatusUiAndFallbackText() throws IOException {
        String template = Files.readString(Path.of("src", "main", "resources", "templates", "processUpdating.html"));

        assertTrue(template.contains("citu-wallet-page"));
        assertTrue(template.contains("Wallet mining status"));
        assertTrue(template.contains("legacy / pre-V35 until activation"));
        assertTrue(template.contains("Current node size"));
        assertTrue(template.contains("Thread count"));
        assertTrue(template.contains("Hash rate"));
        assertTrue(template.contains("Last status"));
        assertTrue(template.contains("Block submission"));
        assertTrue(template.contains("Last found block"));
        assertTrue(template.contains("Last submit URL"));
        assertTrue(template.contains("Last submit error"));
        assertTrue(template.contains("temporarily unavailable"));
        assertFalse(template.contains("alert alert-info mt-2"));
        assertFalse(template.contains(">null<"));
    }

    @Test
    void v35ConstantsRemainActivationPreparedAndBurnPlaceholder() {
        assertEquals(514291L, Seting.V35_RESCUE_FORK_INDEX);
        assertEquals("BURN_ADDRESS", Seting.V35_BURN_ADDRESS);
    }

    @Test
    void walletSubmitUrlTargetsNodePortAndResolveEndpoint() {
        String url = BasisController.blockSubmitUrlFor("http://194.87.236.238:82");

        assertEquals("http://194.87.236.238:82/nodes/resolve_from_to_block", url);
        assertTrue(url.contains(":82/"));
        assertFalse(url.contains(":8082/"));
    }

    @Test
    void submitDebugStartsVisibleBeforeBlockFound() {
        BasisController.resetMiningSubmitDebugForTests();

        assertEquals("not submitted yet", BasisController.getLastSubmitUrlForTests());
        assertEquals("none", BasisController.getLastSubmitErrorForTests());
        assertEquals(0, BasisController.getLastSubmitHttpStatusForTests());
    }
}
