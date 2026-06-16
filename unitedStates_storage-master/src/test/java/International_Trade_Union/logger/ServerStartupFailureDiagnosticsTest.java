package International_Trade_Union.logger;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ServerStartupFailureDiagnosticsTest {
    @Test
    void classifiesH2DatabaseLockAsDuplicateNode() {
        RuntimeException error = new RuntimeException("Database may be already in use: C:/server/resources/h2DB/db.mv.db. Possible solutions: close all other connection(s)");

        assertEquals(
                ServerStartupFailureDiagnostics.H2_LOCK_CODE,
                ServerStartupFailureDiagnostics.classify(error));
    }

    @Test
    void leavesUnrelatedStartupFailuresGeneric() {
        RuntimeException error = new RuntimeException("port already in use");

        assertEquals("UNKNOWN_STARTUP_FAILURE", ServerStartupFailureDiagnostics.classify(error));
    }
}
