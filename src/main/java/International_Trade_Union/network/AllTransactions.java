package International_Trade_Union.network;

import International_Trade_Union.entity.DtoTransaction.DtoTransaction;
import International_Trade_Union.logger.MyLogger;
import International_Trade_Union.model.Mining;
import International_Trade_Union.setings.Seting;
import International_Trade_Union.utils.*;
import International_Trade_Union.utils.base.Base;
import International_Trade_Union.utils.base.Base58;
import org.json.JSONException;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SignatureException;
import java.security.spec.InvalidKeySpecException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class AllTransactions {
    private static List<DtoTransaction> instance = new ArrayList<>();
    //все транзакции которые уже добавлены в блок, нужно чтобы повторно
    //не добавлялись в блок если они скачены с дисковери.
    private static List<DtoTransaction> sendedTransaction = new ArrayList<>();

    public static List<DtoTransaction> readFrom() throws IOException, NoSuchAlgorithmException, InvalidKeySpecException, SignatureException, NoSuchProviderException, InvalidKeyException {
        return UtilsTransaction.readLineObject(Seting.ORGINAL_ALL_TRANSACTION_FILE);
    }

    public static synchronized List<DtoTransaction> getInstance()  {
        if (instance == null) {
            instance = new ArrayList<>();
        }
        instance = new ArrayList<>();
        try {
            instance.addAll(UtilsTransaction.readLineObject(Seting.ORGINAL_ALL_TRANSACTION_FILE));
        } catch (Exception e){
            MyLogger.saveLog("getInstance: ", e);
            return new ArrayList<>();
        }
        Base base = new Base58();
        instance = instance.stream()
                .filter(UtilsUse.distinctByKeyString(t -> {
                    try {
                        return base.encode(t.getSign());
                    } catch (Exception e) {
                        e.printStackTrace();
                        return null; // или другое значение по умолчанию
                    }
                }))
                .collect(Collectors.toList());
        instance = instance.stream().filter(t->t!= null).collect(Collectors.toList());
        try {
            sendedTransaction = getInsanceSended();
        }catch (Exception e){
            MyLogger.saveLog("getInstance: getInstance: ");
            return instance;
        }


        instance.removeAll(sendedTransaction);

        return instance;
    }

    public static synchronized void clearAllTransaction() {
        instance = new ArrayList<>();
        Mining.deleteFiles(Seting.ORGINAL_ALL_TRANSACTION_FILE);
    }

    public static synchronized void clearAllSendedTransaction(boolean deleted) {
        if(deleted){
            sendedTransaction = new ArrayList<>();
            Mining.deleteFiles(Seting.ORIGINAL_ALL_SENDED_TRANSACTION_FILE);
            System.out.println("clear delete sended transaction");
            AllTransactions.clearAllTransaction();
        }

    }

    public static synchronized void clearUsedTransaction(List<DtoTransaction> transactions) throws IOException, NoSuchAlgorithmException, SignatureException, InvalidKeySpecException, NoSuchProviderException, InvalidKeyException {
        instance = getInstance();
        List<DtoTransaction> temporaryDto = new ArrayList<>();

        instance = temporaryDto;
        instance.removeAll(transactions);
        Mining.deleteFiles(Seting.ORGINAL_ALL_TRANSACTION_FILE);
        for (DtoTransaction dtoTransaction : instance) {

            UtilsTransaction.saveAllTransaction(dtoTransaction, Seting.ORGINAL_ALL_TRANSACTION_FILE);
        }


    }


    public static synchronized void addAllTransactions(List<DtoTransaction> transactions){
        try {
            createPackageTransactions(transactions);
            instance = new ArrayList<>();
            instance.addAll(transactions);
            Mining.deleteFiles(Seting.ORGINAL_ALL_TRANSACTION_FILE);
            Base base = new Base58();
            instance = instance.stream()
                    .filter(UtilsUse.distinctByKeyString(t -> {
                        try {
                            return base.encode(t.getSign());
                        } catch (Exception e) {
                            e.printStackTrace();
                            return null; // или другое значение по умолчанию
                        }
                    }))
                    .collect(Collectors.toList());
            instance = instance.stream().filter(t->t!= null).collect(Collectors.toList());
            for (DtoTransaction dtoTransaction : instance) {
                UtilsTransaction.saveAllTransaction(dtoTransaction, Seting.ORGINAL_ALL_TRANSACTION_FILE);
            }
        }catch (Exception e){
            MyLogger.saveLog("addAllTransactions: ", e);
            return;
        }


    }

    public static void createPackageTransactions(List<DtoTransaction> transactions) {
        try {
            File file = new File(Seting.ORGINAL_ALL_TRANSACTION_FILE);
            if (transactions.isEmpty() || !file.exists()) {
                Mining.deleteFiles(Seting.ORGINAL_ALL_TRANSACTION_FILE);
                initializeFile(file);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    private static void initializeFile(File file) throws IOException {
        String filePath = file.getPath();
        if (!filePath.contains(".txt") && !file.exists()) {
            System.out.println("is directory: " + Files.isDirectory(file.toPath()) + " : " + filePath);
            Files.createDirectories(file.toPath());
        } else if (!file.exists()) {
            Files.createDirectories(Paths.get(filePath).getParent());
            Files.createFile(file.toPath());
        }
    }
    public static synchronized void addTransaction(DtoTransaction transaction) throws IOException, NoSuchAlgorithmException, SignatureException, InvalidKeySpecException, NoSuchProviderException, InvalidKeyException {

        try{
            instance = getInstance();
            instance.add(transaction);
            Mining.deleteFiles(Seting.ORGINAL_ALL_TRANSACTION_FILE);
            Base base = new Base58();
            instance = instance.stream()
                    .filter(UtilsUse.distinctByKeyString(t -> {
                        try {
                            return base.encode(t.getSign());
                        } catch (Exception e) {
                            e.printStackTrace();
                            return null; // или другое значение по умолчанию
                        }
                    }))
                    .collect(Collectors.toList());

            instance = instance.stream().filter(t->t!= null).collect(Collectors.toList());
            for (DtoTransaction dtoTransaction : instance) {
                UtilsTransaction.saveAllTransaction(dtoTransaction, Seting.ORGINAL_ALL_TRANSACTION_FILE);
            }
        }catch (Exception e){
            MyLogger.saveLog("addTransaction: ", e);
        }


    }
    public static synchronized void addTransaction(List<DtoTransaction> transactions) throws IOException, NoSuchAlgorithmException, SignatureException, InvalidKeySpecException, NoSuchProviderException, InvalidKeyException {
        for (DtoTransaction transaction : transactions) {
            addTransaction(transaction);
        }
    }

    public static synchronized void addSendedTransaction(List<DtoTransaction> transactions) throws IOException, NoSuchAlgorithmException, InvalidKeySpecException, SignatureException, NoSuchProviderException, InvalidKeyException {
        sendedTransaction = getInsanceSended();
        sendedTransaction.addAll(transactions);

        Mining.deleteFiles(Seting.ORIGINAL_ALL_SENDED_TRANSACTION_FILE);
        for (DtoTransaction dtoTransaction : sendedTransaction) {
            UtilsTransaction.saveAllTransaction(dtoTransaction, Seting.ORIGINAL_ALL_SENDED_TRANSACTION_FILE);
        }

        System.out.println("AllTransaction: addSendedTransaction: " + sendedTransaction.size());

    }

    public static List<DtoTransaction> getInsanceSended() throws IOException, NoSuchAlgorithmException, InvalidKeySpecException, SignatureException, NoSuchProviderException, InvalidKeyException {
        if (sendedTransaction == null) {
            sendedTransaction = new ArrayList<>();
        }
        sendedTransaction = UtilsTransaction.readLineObject(Seting.ORIGINAL_ALL_SENDED_TRANSACTION_FILE);
        Base base = new Base58();
        sendedTransaction = sendedTransaction.stream()
                .filter(UtilsUse.distinctByKeyString(t -> {
                    try {
                        return base.encode(t.getSign());
                    } catch (Exception e) {
                        e.printStackTrace();
                        return null; // или другое значение по умолчанию
                    }
                }))
                .collect(Collectors.toList());
        instance = instance.stream().filter(t->t!= null).collect(Collectors.toList());
        return sendedTransaction;
    }

}
