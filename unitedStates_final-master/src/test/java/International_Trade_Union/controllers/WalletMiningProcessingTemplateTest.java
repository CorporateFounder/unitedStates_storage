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
        assertTrue(template.contains("temporarily unavailable"));
        assertFalse(template.contains("alert alert-info mt-2"));
        assertFalse(template.contains(">null<"));
    }

    @Test
    void v35ConstantsRemainActivationPreparedAndBurnPlaceholder() {
        assertEquals(514291L, Seting.V35_RESCUE_FORK_INDEX);
        assertEquals("BURN_ADDRESS", Seting.V35_BURN_ADDRESS);
    }
}
