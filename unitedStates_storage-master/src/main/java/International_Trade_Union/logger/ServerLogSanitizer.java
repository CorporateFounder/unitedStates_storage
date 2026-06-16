package International_Trade_Union.logger;

public final class ServerLogSanitizer {
    public static final int MAX_LOG_MESSAGE_CHARS = 300;

    private ServerLogSanitizer() {
    }

    public static String safeMessage(String message) {
        if (message == null) {
            return "null";
        }
        String normalized = message.replace('\r', ' ').replace('\n', ' ').trim();
        if (normalized.length() <= MAX_LOG_MESSAGE_CHARS) {
            return normalized;
        }
        return normalized.substring(0, MAX_LOG_MESSAGE_CHARS)
                + "... [truncated, originalChars=" + normalized.length() + "]";
    }
}
