package International_Trade_Union.utils;

import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class UtilsFileSaveRead {
    public static void write(MultipartFile file, Path dir) {
        Path filepath = Paths.get(dir.toString(), file.getOriginalFilename());

        try (OutputStream os = Files.newOutputStream(filepath)) {
            os.write(file.getBytes());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    public static boolean deleted(int index, String fileName, String temp) throws IOException {
        File inputFile = new File("myFile.txt");
        File tempFile = new File("myTempFile.txt");

        BufferedReader reader = new BufferedReader(new FileReader(inputFile));
        BufferedWriter writer = new BufferedWriter(new FileWriter(tempFile));

        int number = 0;
        String currentLine;

        while((currentLine = reader.readLine()) != null) {
            number++;
            // trim newline when comparing with lineToRemove
            String trimmedLine = currentLine.trim();
            if(index == number) continue;
            writer.write(currentLine + System.getProperty("line.separator"));
        }
        writer.close();
        reader.close();
        deleteFile(fileName);
        boolean successful = tempFile.renameTo(inputFile);
        return successful;
    }
    public static void save(String object, String fileName) throws IOException {
       save(object, fileName, true);
    }
    public static void save(String object, String fileName, boolean save){
        try(BufferedWriter writer = new BufferedWriter(new FileWriter(fileName, save))) {
            writer.write(object);
            writer.flush();

        }catch (IOException e){
            e.printStackTrace();
        }


    }

    public static void saves(List<String> objects, String fileName, boolean save){

        try(BufferedWriter writer = new BufferedWriter(new FileWriter(fileName, save))) {

            for (String s : objects) {
                writer.write(s + "\n");
            }
            writer.flush();

        }catch (IOException e){
            e.printStackTrace();
        }
    }

    public static String read(String file) throws FileNotFoundException {
        String text = "";
        File file1 = new File(file);
        if(!file1.exists()){
            System.out.println("file dosn't have");
            return text;
        }
        try( BufferedReader reader = new BufferedReader(new FileReader(file))){
            while (reader.ready()){
                text += reader.readLine();
            }
        }catch (IOException e){
            e.printStackTrace();
        }
        return text;
    }
    public static Set<String> readSet(String file){
        Set<String> list = new HashSet<>();

        try( BufferedReader reader = new BufferedReader(new FileReader(file))){
            while (reader.ready()){
                list.add(reader.readLine());
            }
        }catch (IOException e){
            e.printStackTrace();
        }
        return list;
    }
    public static List<String> reads(String file){
        List<String> list = new ArrayList<>();

        try( BufferedReader reader = new BufferedReader(new FileReader(file))){
            while (reader.ready()){
                list.add(reader.readLine());
            }
        }catch (IOException e){
            e.printStackTrace();
        }
        return list;
    }
   
    public static void deleteAllFiles(String path){
        File folder = new File(path);
        for (File file : folder.listFiles()) {
            if(!file.isDirectory()){
                file.delete();
            }
        }
    }

    public static void deleteFile(String path){
        File file = new File(path);
        file.delete();
    }

}
