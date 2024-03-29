package International_Trade_Union.utils;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;

public class UtilsTime {
    public static long getUniversalTimestamp() {
        return ZonedDateTime.now(ZoneOffset.UTC).toEpochSecond() * 1000L;
    }



    // Возвращает временную метку в текстовом формате UTC
    String getUniversalTimestampString() {
        return ZonedDateTime.now(ZoneOffset.UTC).toString();
    }

    public static long differentMillSecondTime(long first, long second){
        return second - first;
    }
}
