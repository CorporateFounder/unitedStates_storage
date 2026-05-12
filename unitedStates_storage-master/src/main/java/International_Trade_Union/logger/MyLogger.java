package International_Trade_Union.logger;

import International_Trade_Union.setings.Seting;
import International_Trade_Union.utils.UtilsFileSaveRead;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.FileNotFoundException;
import java.security.Provider;

public class MyLogger {
    private static final Logger logger = LogManager.getLogger(MyLogger.class);
    // Переменная для настройки максимального размера файла в килобайтах
    public static int MAX_FILE_SIZE_KB = 100;

    public static void saveLog(String log, Throwable throwable) {
        String filePath = Seting.ERROR_FILE;
        // Переменная для настройки максимального размера файла в килобайтах


        try {
            File file = new File(filePath);

            // Проверяем размер файла
            if (file.exists() && file.length() > MAX_FILE_SIZE_KB * 1024) {
                UtilsFileSaveRead.deleteFile(filePath);
            }

            UtilsFileSaveRead.save(log + "\n" + throwable.toString() + "\n", filePath, true);
        } catch (Exception e) {
            System.err.println("Ошибка при сохранении лога с исключением: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static void saveLog(String log) {
        String filePath = Seting.ERROR_FILE;

        try {
            logger.error(log);

            File file = new File(filePath);

            // Проверяем размер файла
            if (file.exists() && file.length() > MAX_FILE_SIZE_KB * 1024) {
                UtilsFileSaveRead.deleteFile(filePath);
            }

            UtilsFileSaveRead.save(log + "\n", filePath, true);
        } catch (Exception e) {
            System.err.println("Ошибка при сохранении лога: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
