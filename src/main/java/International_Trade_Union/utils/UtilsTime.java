package International_Trade_Union.utils;

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
    public static long getUniversalTimestamp2() {
        try {
            String url = "http://worldtimeapi.org/api/timezone/Etc/UTC";
            String jsonResponse = readJsonFromUrl(url);
            // Парсинг временной метки из ответа API
            String dateTimeString = jsonResponse.split("\"datetime\":\"")[1].split("\"")[0];
            ZonedDateTime utcTime = ZonedDateTime.parse(dateTimeString, DateTimeFormatter.ISO_ZONED_DATE_TIME);
            return utcTime.toEpochSecond() * 1000L;
        } catch (Exception e) {
            e.printStackTrace();
            // В случае ошибки возвращаем системное время
            return ZonedDateTime.now(ZoneOffset.UTC).toEpochSecond() * 1000L;
        }
    }

    // Новый метод возвращения временной метки в текстовом формате UTC через API
    public static String getUniversalTimestampString2() {
        try {
            String url = "http://worldtimeapi.org/api/timezone/Etc/UTC";
            String jsonResponse = readJsonFromUrl(url);
            // Парсинг временной метки из ответа API
            String dateTimeString = jsonResponse.split("\"datetime\":\"")[1].split("\"")[0];
            ZonedDateTime utcTime = ZonedDateTime.parse(dateTimeString, DateTimeFormatter.ISO_ZONED_DATE_TIME);
            return utcTime.toString();
        } catch (Exception e) {
            e.printStackTrace();
            // В случае ошибки возвращаем системное время
            return ZonedDateTime.now(ZoneOffset.UTC).toString();
        }
    }

    // Метод для чтения JSON из URL
    private static String readJsonFromUrl(String urlString) throws IOException {
        URL url = new URL(urlString);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setConnectTimeout(6000); // Таймаут соединения в миллисекундах
        conn.setReadTimeout(6000); // Таймаут чтения в миллисекундах

        try (BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))) {
            StringBuilder sb = new StringBuilder();
            int cp;
            while ((cp = rd.read()) != -1) {
                sb.append((char) cp);
            }
            return sb.toString();
        } finally {
            conn.disconnect();
        }
    }
}


