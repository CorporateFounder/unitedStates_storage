package International_Trade_Union.utils;

import International_Trade_Union.logger.MyLogger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

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
    // Новый метод получения временной метки UTC через API

}


