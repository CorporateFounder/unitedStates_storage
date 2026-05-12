package International_Trade_Union.utils;

import java.io.IOException;
import java.net.URL;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.format.DateTimeFormatter;

public class GoogleTime {
    public static Timestamp getGoogleTimestamp() throws IOException {

        URL googleDateUrl = new URL("https://www.google.com/date/t");
        String dateString = googleDateUrl.openConnection().getHeaderField("Date");
        DateTimeFormatter formatter = DateTimeFormatter.RFC_1123_DATE_TIME;
        Instant instant = Instant.from(formatter.parse(dateString));

        return Timestamp.from(instant);

    }

}
