package International_Trade_Union.utils;

import java.io.BufferedReader;
import java.io.InputStreamReader;

public class TimeSyncManager {

    public static void syncTime() throws Exception {
        String osName = System.getProperty("os.name").toLowerCase();
        String[] commands;

        if (osName.contains("win")) {
            // Команды для Windows
            commands = new String[]{
                "net stop w32time",
                "w32tm /config /manualpeerlist:\"0.ch.pool.ntp.org, 1.ch.pool.ntp.org, 2.ch.pool.ntp.org, 3.ch.pool.ntp.org\" /syncfromflags:manual /reliable:yes /update",
                "net start w32time",
                "w32tm /resync"
            };
        } else {
            // Команды для Linux
            commands = new String[]{
                "sudo service ntp stop",
                "sudo ntpdate -u pool.ntp.org",
                "sudo service ntp start"
            };
        }

        // Выполнение команд
        for (String command : commands) {
            runCommand(command);
        }

        System.out.println("Время успешно синхронизировано на " + (osName.contains("win") ? "Windows" : "Linux") + ".");
    }

    private static void runCommand(String command) throws Exception {
        Process process = Runtime.getRuntime().exec(command);
        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        String line;
        while ((line = reader.readLine()) != null) {
            System.out.println(line);
        }
        process.waitFor();
    }

    public static void main(String[] args) {
        try {
            syncTime();
        } catch (Exception e) {
            System.err.println("Ошибка синхронизации времени: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
