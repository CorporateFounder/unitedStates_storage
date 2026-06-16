package International_Trade_Union.logger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MyLogger {
    private static final Logger logger = LoggerFactory.getLogger(MyLogger.class);

    public static void saveLog(String log, Throwable throwable) {
        String safeLog = ServerLogSanitizer.safeMessage(log);
        if (throwable == null) {
            logger.error(safeLog);
        } else {
            logger.error(safeLog, throwable);
        }
    }

    public static void saveLog(String log) {
        logger.info(ServerLogSanitizer.safeMessage(log));
    }

    public static void warnLog(String log) {
        logger.warn(ServerLogSanitizer.safeMessage(log));
    }

    public static void warnLog(String log, Throwable throwable) {
        String safeLog = ServerLogSanitizer.safeMessage(log);
        if (throwable == null) {
            logger.warn(safeLog);
        } else {
            logger.warn(safeLog, throwable);
        }
    }
}
