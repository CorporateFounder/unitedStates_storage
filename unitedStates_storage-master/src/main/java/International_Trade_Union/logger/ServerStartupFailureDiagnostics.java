package International_Trade_Union.logger;

import International_Trade_Union.setings.Seting;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationFailedEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import java.util.Locale;

@Component
public class ServerStartupFailureDiagnostics implements ApplicationListener<ApplicationFailedEvent> {
    public static final String H2_LOCK_CODE = "H2_DATABASE_LOCKED_ANOTHER_NODE_RUNNING";
    private static final Logger logger = LoggerFactory.getLogger(ServerStartupFailureDiagnostics.class);

    @Override
    public void onApplicationEvent(ApplicationFailedEvent event) {
        String code = classify(event.getException());
        if (H2_LOCK_CODE.equals(code)) {
            logger.error("{} dbPath={} message=Another CITU node process appears to hold the H2 database. Stop the duplicate node process before restart. No database files were modified.",
                    H2_LOCK_CODE,
                    Seting.H2_DB + "db");
        }
    }

    public static String classify(Throwable throwable) {
        Throwable current = throwable;
        while (current != null) {
            String message = String.valueOf(current.getMessage()).toLowerCase(Locale.ROOT);
            if (message.contains("database may be already in use")
                    || (message.contains("db.mv.db") && (message.contains("locked") || message.contains("file lock")))) {
                return H2_LOCK_CODE;
            }
            current = current.getCause();
        }
        return "UNKNOWN_STARTUP_FAILURE";
    }
}
