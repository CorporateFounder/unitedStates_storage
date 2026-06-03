package International_Trade_Union.controllers;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class WalletPublicUiTemplateTest {
    @Test
    public void createAccountExplainsPublicAndSecretCredentialBoundaries() throws IOException {
        String content = template("create-account.html");

        assertTrue(content.contains("citu-wallet-page"));
        assertTrue(content.contains("Address / login / public key can be public"));
        assertTrue(content.contains("Password / private key is secret"));
        assertTrue(content.contains("Never share it in screenshots"));
        assertTrue(content.contains("account recovery may be impossible"));
        assertTrue(content.contains("Private key / password - secret"));
        assertFalse(content.contains("<body>"));
    }

    @Test
    public void statisticsAndMenuUseModernCituLayout() throws IOException {
        String statistics = template("statistics.html");
        String header = template("blocks/header.html");

        assertTrue(statistics.contains("citu-wallet-page"));
        assertTrue(statistics.contains("Legacy / pre-V35 statistics"));
        assertTrue(statistics.contains("V35 changes the reward model at block <strong>514291</strong>"));
        assertTrue(statistics.contains("citu-wallet-stat-list"));
        assertTrue(header.contains("Mining / pool status"));
        assertTrue(header.contains("/settings"));
        assertTrue(header.contains("/vote"));
        assertFalse(header.contains("pink"));
    }

    @Test
    public void settingsMiningTransactionsAndGovernanceCarrySafetyLabels() throws IOException {
        String content = template("seting.html")
                + template("mining.html")
                + template("transactions.html")
                + template("board-of-shareholders.html")
                + template("current-laws.html")
                + template("voting.html");

        assertTrue(content.contains("http://194.87.236.238:82"));
        assertTrue(content.contains("http://127.0.0.1:8083"));
        assertTrue(content.contains("http://127.0.0.1:8082"));
        assertTrue(content.contains("legacy / pre-V35"));
        assertTrue(content.contains("Pool configuration is legacy / pre-V35"));
        assertTrue(content.contains("Do not enter passwords or private keys on public transaction query pages"));
        assertTrue(content.contains("Password / private key - secret"));
        assertFalse(content.contains("alert alert-info mt-2"));
    }

    private String template(String file) throws IOException {
        return Files.readString(Path.of("src", "main", "resources", "templates", file));
    }
}
