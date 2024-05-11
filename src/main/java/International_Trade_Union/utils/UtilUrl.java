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
        URLConnection conn = new URL(url).openConnection();
        conn.setConnectTimeout(25000); // Таймаут соединения в миллисекундах
        conn.setReadTimeout(25000); // Таймаут чтения в миллисекундах

        try (InputStream is = conn.getInputStream();
             BufferedReader rd = new BufferedReader(new InputStreamReader(is, Charset.forName("UTF-8")))) {
            String jsonText = readAll(rd);

            return jsonText;
        } finally {
            if (conn instanceof HttpURLConnection) {
                ((HttpURLConnection) conn).disconnect();
            }
        }
    }

    public static String readJsonFromUrl(String url) throws IOException, JSONException {
        URL url1 = new URL(url);
        URLConnection conn = url1.openConnection();
        conn.setConnectTimeout(20000); // Устанавливаем таймаут соединения в 5 секунд
        conn.setReadTimeout(20000); // Устанавливаем таймаут чтения в 5 секунд
        InputStream is = conn.getInputStream();
        BufferedReader rd = null;
        try {
            rd = new BufferedReader(new InputStreamReader(is, Charset.forName("UTF-8")));
            String jsonText = readAll(rd);
            return jsonText;
        } finally {
            if (rd != null) {
                try {
                    rd.close();
                } catch (IOException e) {
                    e.printStackTrace(); // Логируем исключение
                }
            }
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                    e.printStackTrace(); // Логируем исключение
                }
            }
            if (conn instanceof HttpURLConnection) {
                ((HttpURLConnection) conn).disconnect();
            }

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
        try {


            conn.setReadTimeout(35000);
            conn.setConnectTimeout(35000);
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json; utf-8");
            conn.setRequestProperty("Accept", "application/json");
            conn.setDoOutput(true);


            try (OutputStream outputStream = conn.getOutputStream()) {
                byte[] input = jsonObject.getBytes("utf-8");
                outputStream.write(input, 0, input.length);
                conn.getResponseCode();
            }


            conn.connect();
            try (BufferedReader br = new BufferedReader(
                    new InputStreamReader(conn.getInputStream(), "utf-8"))) {
                StringBuilder response = new StringBuilder();
                String responseLine = null;
                while ((responseLine = br.readLine()) != null) {
                    response.append(responseLine.trim());
                }
                return response.toString();

            }
        } finally {
            conn.disconnect();
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
