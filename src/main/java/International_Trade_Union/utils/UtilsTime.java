package International_Trade_Union.utils;

import International_Trade_Union.logger.MyLogger;
import org.apache.commons.net.ntp.NTPUDPClient;
import org.apache.commons.net.ntp.TimeInfo;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;

public class UtilsTime {
    public static long getUniversalTimestamp() {
        return ZonedDateTime.now(ZoneOffset.UTC).toEpochSecond() * 1000L;
    }
    public static boolean isTimeSynchronized(long localTime, long serverTime) {
        long tolerance = 5000L; // Допустимая погрешность в миллисекундах
        return Math.abs(localTime - serverTime) <= tolerance;
    }
    public static String getUniversalTimestampString() {
        return ZonedDateTime.now(ZoneOffset.UTC).toString();
    }

    public static long differentMillSecondTime(long first, long second){
        return second - first;
    }

    public static long getUniversalTimestampFromHttp() {
        try {
            URL url = new URL("http://worldtimeapi.org/api/timezone/Etc/UTC");
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(5000);

            BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8));
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = in.readLine()) != null) {
                response.append(line);
            }
            in.close();

            String jsonResponse = response.toString();
            long unixTime = Long.parseLong(jsonResponse.replaceAll(".*\"unixtime\":(\\d+).*", "$1"));
            return unixTime * 1000;
        } catch (IOException e) {
            e.printStackTrace();
            MyLogger.saveLog("UtilsTime HTTP error: ", e);
            return Instant.now().getEpochSecond() * 1000;
        }
    }

    public static long getUniversalTimestampFromNTP() {
        String[] ntpServers = {
                "pool.ntp.org"
        };

        for (String server : ntpServers) {
            try {
                NTPUDPClient client = new NTPUDPClient();
                client.setDefaultTimeout(5000);
                InetAddress address = InetAddress.getByName(server);
                TimeInfo timeInfo = client.getTime(address);
                long returnTime = timeInfo.getMessage().getTransmitTimeStamp().getTime() * 1000;
                client.close();
                return returnTime;
            } catch (Exception e) {
                e.printStackTrace();
                MyLogger.saveLog("UtilsTime NTP error with server " + server + ": ", e);
            }
        }

        // If all NTP servers fail, fall back to HTTP
        return getUniversalTimestampFromHttp();
    }
}
