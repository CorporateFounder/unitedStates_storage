package International_Trade_Union.utils;

import International_Trade_Union.setings.Seting;
import International_Trade_Union.setings.SetingPool;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class UtilsCreatedDirectory {

    public static void createPackages() throws IOException {
        List<String> files = new ArrayList<>();

        files.add(Seting.ORIGINAL_BLOCKCHAIN_FILE);
        files.add(Seting.ORIGINAL_BALANCE_FILE);
        files.add(Seting.ORIGINAL_ALL_CORPORATION_LAWS_FILE);
        files.add(Seting.ORIGINAL_CORPORATE_VOTE_FILE);
        files.add(Seting.ORIGINAL_ALL_CORPORATION_LAWS_WITH_BALANCE_FILE);
        files.add(Seting.ORGINAL_ALL_TRANSACTION_FILE);
        files.add(Seting.ORIGINAL_POOL_URL_ADDRESS_FILE);
        files.add(Seting.ORIGINAL_CORPORATE_VOTE_FILE);
        files.add(Seting.ORIGINAL_POOL_URL_ADDRESS_FILE);
        files.add(Seting.ORIGINAL_ALL_SENDED_TRANSACTION_FILE);
        files.add(Seting.ORIGINAL_POOL_URL_ADDRESS_BLOCKED_FILE);
        files.add(Seting.ORIGINAL_ACCOUNT);
        files.add(Seting.ORIGINAL_BOARD_0F_SHAREHOLDERS_FILE);
        files.add(Seting.TEMPORARY_BLOCKCHAIN_FILE);
        files.add(Seting.BALANCE_REPORT_ON_DESTROYED_COINS);
        files.add(Seting.CURRENT_BUDGET_END_EMISSION);
        files.add(Seting.H2_DB);
        files.add(Seting.ERROR_FILE);

        files.add(Seting.SLIDING_WINDOWS_BALANCE);


        files.add(SetingPool.SETING_FILE);
        files.add(SetingPool.POOL_BLOCK_MAP_FILE);
        files.add(SetingPool.ORIGINAL_BALANCE_FILE);

        ObjectMapper mapper = new ObjectMapper();

        for (String s : files) {
            File f = new File(s);
            if (!s.contains(".txt") && !f.exists()) {
                System.out.println("Создаём директорию: " + s);
                Files.createDirectories(f.toPath());
            } else if (!f.exists()) {
                Files.createDirectories(Paths.get(s).getParent());
                Files.createFile(Paths.get(s));
                System.out.println("Создан файл: " + s);
                if (s.equals(SetingPool.SETING_FILE)) {
                    SetingPool defaultSetingPool = new SetingPool();
                    String json = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(defaultSetingPool);
                    Files.write(Paths.get(s), json.getBytes());
                    System.out.println("Записаны дефолтные настройки в " + s);
                }
            }
        }
    }

    public static String getJarDirectory() {
        String jarPath = UtilsCreatedDirectory.class.getProtectionDomain().getCodeSource().getLocation().getPath();
        return new File(jarPath).getParent();
    }
}
