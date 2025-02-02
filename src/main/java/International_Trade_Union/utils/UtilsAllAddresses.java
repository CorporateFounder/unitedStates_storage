package International_Trade_Union.utils;

import International_Trade_Union.controllers.BasisController;
import International_Trade_Union.logger.MyLogger;
import International_Trade_Union.setings.Seting;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.File;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SignatureException;
import java.security.spec.InvalidKeySpecException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

public class UtilsAllAddresses {
    @Autowired
    DomainConfiguration domainConfiguration;

    public static void saveAllAddresses(String adress, String filename) throws IOException, NoSuchAlgorithmException, SignatureException, InvalidKeySpecException, NoSuchProviderException, InvalidKeyException {
        int fileLimit = Seting.SIZE_FILE_LIMIT * 1024 * 1024;

        //папка чтобы проверить есть ли
        File folder = new File(filename);
        List<String> files = new ArrayList<>();
        for (File file : folder.listFiles()) {
            if(!file.isDirectory()){
                files.add(file.getAbsolutePath());
            }
        }


        int count = 0;
        files = files.stream().sorted().collect(Collectors.toList());
        String nextFile = "";

        if (files.size() > 0) {
            nextFile = files.get(files.size()-1);

            count = Integer.parseInt(nextFile.replaceAll("[^\\d]", ""));


        }

        File file = new File(nextFile);

        if(file.length() >= fileLimit){
            count++;

        }

        nextFile = filename + count + ".txt";

        String json = UtilsJson.objToStringJson(adress);
        UtilsFileSaveRead.save(json + "\n", nextFile);

    }

    public static Set<String> readLineObject(String filename ) throws IOException, NoSuchAlgorithmException, InvalidKeySpecException, SignatureException, NoSuchProviderException, InvalidKeyException {
        Set<String> allAddresses = new HashSet<>();
        File folder = new File(filename);
        for (final File fileEntry : folder.listFiles()) {
            if (fileEntry.isDirectory()) {
                System.out.println("is directory " + fileEntry.getAbsolutePath());
            } else {
                Set<String> list = UtilsFileSaveRead.readSet(fileEntry.getAbsolutePath())
                        .stream().collect(Collectors.toSet());
                for (String s : list) {
                    allAddresses.add(s);
                }

            }
        }



        allAddresses = allAddresses.stream()
                .map(address -> address.replace("\"", ""))
                .collect(Collectors.toSet());

        return allAddresses;
    }

    public static void sendAddress(Set<String> nodes, MyHost myHost) throws IOException {

        ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

        nodes.stream().forEach(s -> executor.submit(() -> {
            try {
                String hostStr = s.contains("\"") ? s.replaceAll("\"", "") : s;

                System.out.println("send " + s + " my host: " + myHost + " nodes: " + nodes.size());
                UtilUrl.sendPost(UtilsJson.objToStringJson(myHost), hostStr + "/putNode");

            } catch (Exception e) {
                System.out.println("error send to host: " + s);
            }
        }));

        executor.shutdown();
    }
    public static void putNode(MyHost host) {

        System.out.println("-----------------------------------------------------");
        //TODO test save
        System.out.println("start testing host for put: " + host);


        try {
            Set<String> myhosts = UtilsAllAddresses.readLineObject(Seting.ORIGINAL_POOL_URL_ADDRESS_FILE);
            Set<String> blocked = UtilsAllAddresses.readLineObject(Seting.ORIGINAL_POOL_URL_ADDRESS_BLOCKED_FILE);


            if (host != null
                    && !host.getHost().isBlank()
                    && !host.getHost().isEmpty()
                    && HostValidator.isValidHost(host.getHost())) {
                String hostStr = host.getHost();
                if(host.getHost().contains("\""))
                    hostStr = host.getHost().replaceAll("\"", "");
                String sizeStr = UtilUrl.readJsonFromUrl(hostStr + "/size");
                if (sizeStr.isBlank() || sizeStr.isEmpty()) {
                    System.out.println("not added host: size is blank: " + sizeStr);
                    return;
                }
                if(myhosts.contains(host.getHost()) || blocked.contains(host.getHost())){
                    return;
                }

                UtilsAllAddresses.saveAllAddresses(host.getHost(), Seting.ORIGINAL_POOL_URL_ADDRESS_FILE);
                System.out.println("___________________________________");
                System.out.println("added host: " + host);
                System.out.println("___________________________________");

                return;
            }
        }catch (Exception e){
            System.out.println("---------------------------------------------------");
            System.out.println("error putNode: " + host);
//            e.printStackTrace();
            System.out.println("---------------------------------------------------");


            return;
        }finally {
            System.out.println("finish put");
            System.out.println("-----------------------------------------------------");

        }
        System.out.println("not added host: " + host);
        return ;
    }
    public static void putHost(String host)  {

        System.out.println("put host: " + host);


        try {
            Set<String> myhosts = UtilsAllAddresses.readLineObject(Seting.ORIGINAL_POOL_URL_ADDRESS_FILE);
            Set<String> blocked = UtilsAllAddresses.readLineObject(Seting.ORIGINAL_POOL_URL_ADDRESS_BLOCKED_FILE);

            if (host != null
                    && !host.isBlank()
                    && !host.isEmpty()
                    && HostValidator.isValidHost(host)) {

                String hostStr = host;
                if(host.contains("\""))
                    hostStr = host.replaceAll("\"", "");

                String sizeStr = UtilUrl.readJsonFromUrl(hostStr + "/size");
                if (sizeStr.isBlank() || sizeStr.isEmpty()) {
                    System.out.println("not added host: size is blank");
                    return;
                }
                if(myhosts.contains(host) || blocked.contains(host)){
                    return;
                }

                UtilsAllAddresses.saveAllAddresses(host, Seting.ORIGINAL_POOL_URL_ADDRESS_FILE);
                System.out.println("added host: " + host);
                return;
            }
        }catch (Exception e){
            System.out.println("-----------------------------------");
            System.out.println("error put host: ");
//            e.printStackTrace();
            System.out.println("-----------------------------------");

        }
        System.out.println("not added host");
        return ;
    }
}
