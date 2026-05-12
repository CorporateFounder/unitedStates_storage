package International_Trade_Union.utils;

import International_Trade_Union.model.Mining;
import International_Trade_Union.setings.Seting;
import org.springframework.web.multipart.MultipartFile;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class UtilsFileSaveRead {
    private static final ObjectMapper mapper = new ObjectMapper();

    public static void moveFile(String src, String dest ) {
        File folder = new File(src);
        Mining.deleteFiles(dest);
        for (File file : folder.listFiles()) {
            if (!file.isDirectory()){
                Path result = null;
                try {
                    result = Files.move(Paths.get(file.getAbsolutePath()),
                            Paths.get(dest + file.getName()));
                } catch (IOException e) {
                    System.out.println("Exception while moving file: " + e.getMessage());
                }
                if(result != null) {
                    System.out.println("File moved successfully.");
                } else {
                    System.out.println("File movement failed.");
                }
            }
        }
        Mining.deleteFiles(src);
    }

    public static void write(MultipartFile file, Path dir) {
        Path filepath = Paths.get(dir.toString(), file.getOriginalFilename());
        try (OutputStream os = Files.newOutputStream(filepath)) {
            os.write(file.getBytes());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static boolean deleted(String fileName, int index) throws IOException {
        File inputFile = new File(fileName);
        File tempFile = new File(Seting.ORIGINAL_BLOCKCHAIN_FILE + "myTempFile.txt");
        boolean deleted = false;
        try (BufferedReader reader = new BufferedReader(new FileReader(inputFile));
             BufferedWriter writer = new BufferedWriter(new FileWriter(tempFile))) {
            int innerIndex = 0;
            String currentLine;
            while ((currentLine = reader.readLine()) != null) {
                if (innerIndex == index) {
                    System.out.println("deleted: " + index);
                    deleted = true;
                    return deleted;
                }
                writer.write(currentLine + System.getProperty("line.separator"));
                innerIndex++;
            }
            deleteFile(fileName);
            boolean successful = tempFile.renameTo(inputFile);
        }
        return deleted;
    }

    public static void save(String object, String fileName) throws IOException {
        save(object, fileName, true);
    }

    public static void save(String object, String fileName, boolean append) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(fileName, append))) {
            writer.write(object);
            writer.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void saves(List<String> objects, String fileName, boolean append) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(fileName, append))) {
            for (String s : objects) {
                writer.write(s + "\n");
            }
            writer.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static String read(String file) throws FileNotFoundException {
        StringBuilder text = new StringBuilder();
        File file1 = new File(file);
        if (!file1.exists()){
            System.out.println("file doesn't exist");
            return text.toString();
        }
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null){
                text.append(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return text.toString();
    }

    public static Set<String> readSet(String file) {
        Set<String> list = new HashSet<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            while (reader.ready()){
                list.add(reader.readLine());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return list;
    }

    public static List<String> reads(String file) {
        List<String> list = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            while (reader.ready()){
                list.add(reader.readLine());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return list;
    }

    public static void deleteAllFiles(String path) {
        File folder = new File(path);
        for (File file : folder.listFiles()) {
            if (!file.isDirectory()){
                file.delete();
            }
        }
    }

    public static void deleteFile(String path) {
        File file = new File(path);
        file.delete();
    }

    // Универсальный метод загрузки JSON в объект заданного класса
    public static <T> T loadJson(String file, Class<T> clazz) throws IOException {
        File f = new File(file);
        if (!f.exists()) {
            return null;
        }
        return mapper.readValue(f, clazz);
    }
}
