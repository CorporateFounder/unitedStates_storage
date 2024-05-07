package International_Trade_Union.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.json.JSONException;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.Charset;

public class UtilUrl {
    ////модифицированный ими код
    public static String readJsonFromUrl_silent(String url) throws IOException, JSONException {
        InputStream is = new URL(url).openStream();
        try {
            BufferedReader rd = new BufferedReader(new InputStreamReader(is, Charset.forName("UTF-8")));
            String jsonText = readAll(rd);
            ObjectMapper mapper = new ObjectMapper();
            return jsonText;
        } finally {
//             System.out.println("UtilUrl: readJsonFromUrl: " + url );
            is.close();
        }
    }
    public static String readJsonFromUrl(String url) throws IOException, JSONException {
        URL url1 = new URL(url);
        URLConnection conn = url1.openConnection();
        conn.setConnectTimeout(3000); // Устанавливаем таймаут соединения в 5 секунд
        conn.setReadTimeout(3000); // Устанавливаем таймаут чтения в 5 секунд
        InputStream is = conn.getInputStream();

        try {
            BufferedReader rd = new BufferedReader(new InputStreamReader(is, Charset.forName("UTF-8")));
            String jsonText = readAll(rd);
            ObjectMapper mapper = new ObjectMapper();
            return jsonText;
        } finally {
            System.out.println("UtilUrl: readJsonFromUrl: " + url );
            is.close();

        }
    }


    private static String readAll(Reader rd) throws IOException {
        StringBuilder sb = new StringBuilder();
        int cp;
        while ((cp = rd.read()) != -1) {
            sb.append((char) cp);
        }
        return sb.toString();
    }

    public static String getObject(String jsonObject, String requstStr) throws IOException {
        URL url = new URL(requstStr);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
//        conn.connect();
        conn.setReadTimeout(25000);
        conn.setConnectTimeout(25000);
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json; utf-8");
        conn.setRequestProperty("Accept", "application/json");
        conn.setDoOutput(true);



        try(OutputStream outputStream = conn.getOutputStream()) {
            byte[] input = jsonObject.getBytes("utf-8");
            outputStream.write(input, 0, input.length);
            conn.getResponseCode();
        }catch (IOException e) {
            // Обработка исключения при записи данных
            throw e;
        } finally {
            // Закрытие соединения в блоке finally
            if (conn != null) {
                conn.disconnect();
            }
        }



        try(BufferedReader br = new BufferedReader(
                new InputStreamReader(conn.getInputStream(), "utf-8"))) {
            StringBuilder response = new StringBuilder();
            String responseLine = null;
            while ((responseLine = br.readLine()) != null) {
                response.append(responseLine.trim());
            }
            return response.toString();

        }

    }


    public static int sendPost(String jsonObject, String requestStr) throws IOException {
        int response;

        URL url = new URL(requestStr);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
//        conn.connect();
        conn.setReadTimeout(25000);
        conn.setConnectTimeout(25000);
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json; utf-8");
        conn.setRequestProperty("Accept", "application/json");
        conn.setDoOutput(true);

    try (OutputStream outputStream = conn.getOutputStream()) {
        byte[] input = jsonObject.getBytes("utf-8");
        outputStream.write(input, 0, input.length);
        response = conn.getResponseCode();
    } catch (IOException e) {
        // Обработка исключения при записи данных
        e.printStackTrace();
        // Можно добавить логирование или другую обработку
        throw e; // Перебросить исключение выше
    } finally {
        conn.disconnect(); // Закрыть соединение независимо от исключения
    }

    return response;
}


}
