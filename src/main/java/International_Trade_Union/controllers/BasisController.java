package International_Trade_Union.controllers;

import International_Trade_Union.controllers.config.BlockchainFactoryEnum;
import International_Trade_Union.entity.*;
import International_Trade_Union.entity.DtoTransaction.DtoTransaction;
import International_Trade_Union.entity.blockchain.DataShortBlockchainInformation;
import International_Trade_Union.entity.entities.EntityAccount;
import International_Trade_Union.entity.entities.EntityBlock;
import International_Trade_Union.entity.services.BlockService;
import International_Trade_Union.logger.MyLogger;
import International_Trade_Union.model.Account;
import International_Trade_Union.model.LiteVersionWiner;
import International_Trade_Union.model.Tournament;
import International_Trade_Union.vote.LawEligibleForParliamentaryApproval;
import International_Trade_Union.vote.Laws;
import International_Trade_Union.vote.UtilsLaws;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.json.JSONException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import International_Trade_Union.entity.blockchain.Blockchain;
import International_Trade_Union.entity.blockchain.block.Block;
import International_Trade_Union.model.Mining;
import International_Trade_Union.setings.Seting;
import International_Trade_Union.utils.*;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

import java.security.*;
import java.security.spec.InvalidKeySpecException;

import java.sql.Timestamp;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

import static International_Trade_Union.utils.UtilsBalance.calculateBalance;

@RestController
public class BasisController {


    //Сервис для работы с базой данных h2
    @Autowired
    BlockService blockService;
    /**
     * Каждые 100 секунд происходить турнир для отбора победителя.
     */
    @Autowired
    Tournament tournament;
    @Autowired
    UtilsResolving utilsResolving;

    private static volatile AtomicBoolean blockedNewSendBlock = new AtomicBoolean(true);

    public static AtomicBoolean getBlockedNewSendBlock() {
        return blockedNewSendBlock;
    }

    public static void setBlockedNewSendBlock(AtomicBoolean blockedNewSendBlock) {
        BasisController.blockedNewSendBlock = blockedNewSendBlock;
    }

    /**
     * список кандидатов с данного сервера в качестве победителей
     */
    private static CopyOnWriteArrayList<Block> winnerList = new CopyOnWriteArrayList<>();

    @GetMapping("/winnerList")
    @ResponseBody
    public String winnerList() {
        String json = "";
        try {
            json = UtilsJson.objToStringJson(winnerList);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return json;
    }


    private static boolean updating;
    /**
     * список всех победителей, данные могут не отражать реального победителя
     * если победитель был получен из другого сервера
     */
    private static List<LiteVersionWiner> allWiners = new ArrayList<>();
    /**
     * на данный момент присутствуетЮ в качестве анализа сложности блока, но особо не используется
     */
    private static List<LiteVersionWiner> powerWiners = new ArrayList<>();
    /**
     * на данный момент присутствуетЮ в качестве анализа сложности блока, но особо не используется
     */

    private static List<LiteVersionWiner> countTransactionsWiner = new ArrayList<>();
    /**
     * на данный момент присутствуетЮ в качестве анализа сложности блока, но особо не используется
     */
    private static List<LiteVersionWiner> stakingWiners = new ArrayList<>();
    /**
     * окончательный победитель по показателю big random,
     * но может не учитывать реального победителя, если он получен из другого сервера.
     */
    private static List<LiteVersionWiner> bigRandomWiner = new ArrayList<>();

    public static void setTotalTransactionsDays(int totalTransactionsDays) {
        BasisController.totalTransactionsDays = totalTransactionsDays;
    }

    /**
     * общее количество транзакций в сутки
     */
    public static long totalTransactionsDays() {
        return BasisController.totalTransactionsDays;
    }

    /**
     * общее сумма долларов переведенных в сутки
     */
    public static double totalTransactionsSumDollar() {
        return BasisController.totalTransactionsSumDllar;
    }

    public static void setTotalTransactionsSumDllar(double totalTransactionsSumDllar) {
        BasisController.totalTransactionsSumDllar = totalTransactionsSumDllar;
    }

    public static void setTotalDollars(double totalDollars) {
        BasisController.totalDollars = totalDollars;
    }

    public static boolean isBlockchainValid() {
        return blockchainValid;
    }

    public static double totalDollars() {
        return totalDollars;
    }

    public static void setUpdating(boolean b) {
        updating = b;
    }

    public static boolean getUpdating() {
        return updating;
    }

    /**
     * Список кандидатов из которых был выбран победитель в данном сервере
     */
    @GetMapping("/allwinners")
    @ResponseBody

    public String allWinners() {
        String json = "";
        try {
            json = UtilsJson.objToStringJson(allWiners);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return json;
    }

    /**
     * Устарел и не используется
     */
    @GetMapping("/powerWiners")
    @ResponseBody
    public String powerWiners() {
        String json = "";
        try {
            json = UtilsJson.objToStringJson(powerWiners);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return json;
    }

    /**
     * Устарел и не используется
     */
    @GetMapping("/countTransactionsWiner")
    @ResponseBody
    public String countTransactionsWiner() {
        String json = "";
        try {
            json = UtilsJson.objToStringJson(countTransactionsWiner);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return json;
    }

    /**
     * Устарел и не используется
     */
    @GetMapping("/stakingWiners")
    @ResponseBody
    public String stakingWiners() {
        String json = "";
        try {
            json = UtilsJson.objToStringJson(stakingWiners);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return json;
    }

    /**
     * Параметр который определяет победителя, складывается из трех параметров.
     * 1. Хэш блока является семенем для генерации случайного числа от нуля до 135.
     * 2. сложность блока умноженая на число на данный момент 35, но может быть изменен
     * 3. Стэйкинг, где спомощью логарифма вычисляется баллы, которые указаны в таблице.
     * Все эти числа суммируется и получается результат, тот который получил наибольшее
     * значение, становиться победителем.
     */
    @GetMapping("/bigRandomWiner")
    @ResponseBody
    public String bigRandomWiner() {
        String json = "";
        try {

            json = UtilsJson.objToStringJson(bigRandomWiner);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return json;
    }

    public static List<LiteVersionWiner> getCountTransactionsWiner() {
        return countTransactionsWiner;
    }

    public static void setCountTransactionsWiner(List<LiteVersionWiner> countTransactionsWiner) {
        BasisController.countTransactionsWiner = countTransactionsWiner;
    }

    public static List<LiteVersionWiner> getBigRandomWiner() {
        return bigRandomWiner;
    }

    public static void setBigRandomWiner(List<LiteVersionWiner> bigRandomWiner) {
        BasisController.bigRandomWiner = bigRandomWiner;
    }

    public static List<LiteVersionWiner> getStakingWiners() {
        return stakingWiners;
    }

    public static void setStakingWiners(List<LiteVersionWiner> stakingWiners) {
        BasisController.stakingWiners = stakingWiners;
    }

    public static List<LiteVersionWiner> getPowerWiners() {
        return powerWiners;
    }

    public static void setPowerWiners(List<LiteVersionWiner> powerWiners) {
        BasisController.powerWiners = powerWiners;
    }

    public static List<LiteVersionWiner> getAllWiners() {
        return allWiners;
    }

    public static void setAllWiners(List<LiteVersionWiner> allWiners) {
        BasisController.allWiners = allWiners;
    }

    private static int totalTransactionsDays = 0;
    private static double totalTransactionsSumDllar = 0.0;




    /**
     * Все акаунты
     */
    private static Map<String, Account> balances = new HashMap<>();

    /**
     * Общее сумма долларов, не находящиеся в стэйкинге
     */
    private static double totalDollars = 0;
    private static long dificultyOneBlock;

    /**
     * Если происходит запись, то не дает доступа к базе данных
     */
    private volatile static boolean isSaveFile = true;

    /**
     * Последний блок в базе данных
     */
    private static Block prevBlock = null;

    /**
     * Определяет в ценость блокчейна за счет мета данных, некоторые части уже не используется
     * на данный момент используется только big random, size и количество транзакций. Оставшиеся
     * параметры могут быть искажены. Если кошелек настроен на сервер и на сервере и на кошельке отличается
     * параметр staking при одинаковой высоте, это может быть что поврежден файл resources либо
     * на кошельке или сервере. Решение скачать с нуля папку ресурсы.
     */
    private static DataShortBlockchainInformation shortDataBlockchain = null;

    /**
     * Высота блокчейна
     */
    private static int blockcheinSize = 0;
    /**
     * целостность блокчейна, если блокчейн поврежден будет false, и папка ресурсы на кошельке
     * удаляется, а сервер автоматически выключается System.exit(0)
     */
    private static boolean blockchainValid = false;
    //    private static Blockchain blockchain;
    private static Set<String> excludedAddresses = new HashSet<>();
    private static boolean isSave = true;

    /**
     * целостность блокчейна, если блокчейн поврежден будет false, и папка ресурсы на кошельке
     * удаляется, а сервер автоматически выключается System.exit(0)
     */
    public static void setBlockchainValid(boolean blockchainValid) {
        BasisController.blockchainValid = blockchainValid;
    }

    public static void setShortDataBlockchain(DataShortBlockchainInformation shortDataBlockchain) {
        BasisController.shortDataBlockchain = shortDataBlockchain;
    }

    /**
     * Последний блок в базе данных
     */
    public static Block prevBlock() {
        return prevBlock;
    }

    public static void changePrevBlock(Block block) {
        prevBlock = block;
    }

    /**
     * Определяет в ценость блокчейна за счет мета данных, некоторые части уже не используется
     * на данный момент используется только big random, size и количество транзакций. Оставшиеся
     * параметры могут быть искажены. Если кошелек настроен на сервер и на сервере и на кошельке отличается
     * параметр staking при одинаковой высоте, это может быть что поврежден файл resources либо
     * на кошельке или сервере. Решение скачать с нуля папку ресурсы.
     */
    @GetMapping("/datashort")
    public DataShortBlockchainInformation dataShortBlockchainInformation() {
//        System.out.println("get /datashort");
        DataShortBlockchainInformation temp = shortDataBlockchain;
//        System.out.println("/datashort: " + temp);
        return temp;
    }

    public static void setPrevBlock(Block prevBlock) {
        BasisController.prevBlock = prevBlock;
    }


    /**
     * Список потенциальных кандидатов на сервере за блок, но еще результат не предрешён
     * и сюда будут добавляться блоки в течение 100 секунд, но блоки, если их время
     * больше по времени от предыдущего блока на 100 секунд или больше
     */
    public static CopyOnWriteArrayList<Block> getWinnerList() {
        return winnerList;
    }

    public static void setWinnerList(CopyOnWriteArrayList<Block> winnerList) {
        BasisController.winnerList = winnerList;
    }

    public static boolean utilsMethod() throws IOException, NoSuchAlgorithmException, InvalidKeySpecException, SignatureException, NoSuchProviderException, InvalidKeyException {
        boolean result = false;
        if (shortDataBlockchain.getSize() == 0
                || !shortDataBlockchain.isValidation()
                || shortDataBlockchain.getHashCount() == 0
                || prevBlock == null) {

            shortDataBlockchain = Blockchain.checkFromFile(Seting.ORIGINAL_BLOCKCHAIN_FILE);


            blockcheinSize = (int) shortDataBlockchain.getSize();
            blockchainValid = shortDataBlockchain.isValidation();

            result = true;
        }
        return result;
    }

    /**
     * Если блок до и после ********* одинаковый, то файл последний на сервере не поврежден.
     * Так как первый блок отбирается из базы данных h2, а второй тот же блок из уже файла.
     */
    @GetMapping("/status")
    @ResponseBody
    public String status() {
        String result = "";
        try {
            String strIsSave = "isSave: " + isIsSave() + "\n";
            String strBlockchainSize = "blockchainSize: " + getBlockchainSize() + "\n";
            String isSaveFile = "isSaveFile: " + isSaveFile() + "\n";
            String blockFromDb =
                    "blockFromDb: " + String.valueOf(blockService.findBySpecialIndex(blockcheinSize - 1))
                            + "\n";
            String blockFromFile = "*********************************\nblockFromFile: " + Blockchain.indexFromFileBing(blockcheinSize - 1, Seting.ORIGINAL_BLOCKCHAIN_FILE)
                    + "\n";


            result = strIsSave + strBlockchainSize + isSaveFile + blockFromDb + blockFromFile;
            result += "prevBlock: " + BasisController.prevBlock() + "\n";
            result += "**********************************************\n";
            result += "getAllWiners: " + BasisController.getAllWiners().size() + "\n";
            result += "getBigRandomWiner: " + BasisController.getBigRandomWiner().size() + "\n";
            result += "getCountTransactionsWiner: " + BasisController.getCountTransactionsWiner().size() + "\n";
            result += "getWinnerList: " + BasisController.getWinnerList().size() + "\n";
            result += "getStakingWiners: " + BasisController.getStakingWiners().size() + "\n";
            result += "**********************************************\n";
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }


        return result;
    }

    public static boolean isIsSaveFile() {
        return isSaveFile;
    }

    public static void setIsSaveFile(boolean isSaveFile) {
        BasisController.isSaveFile = isSaveFile;
    }

    public static boolean isIsSave() {
        return isSave;
    }

    public static void setIsSave(boolean isSave) {
        BasisController.isSave = isSave;
    }

    public static long getDificultyOneBlock() {
        return dificultyOneBlock;
    }

    public static DataShortBlockchainInformation getShortDataBlockchain() {
        return shortDataBlockchain;
    }

    /**
     * Высота локального блокчейна
     */
    public static int getBlockchainSize() {
        return blockcheinSize;
    }

    public static void setBlockcheinSize(int blockcheinSize) {
        BasisController.blockcheinSize = blockcheinSize;
    }

    public static HttpServletRequest getCurrentRequest() {
        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
        if (requestAttributes == null)
            return null;
        Assert.state(requestAttributes != null, "Could not find current request via RequestContextHolder");
        Assert.isInstanceOf(ServletRequestAttributes.class, requestAttributes);
        HttpServletRequest servletRequest = ((ServletRequestAttributes) requestAttributes).getRequest();
        Assert.state(servletRequest != null, "Could not find current HttpServletRequest");
        return servletRequest;
    }

    /**
     * Возвращает список балансов
     */

    @GetMapping("/v28Index")
    public int v28Start() {
        return Seting.V28_CHANGE_ALGORITH_DIFF_INDEX;
    }

    /**
     * Общее количество всех балансов.
     */
    @GetMapping("/allAccounts")
    public long accounts() {
        return blockService.countAccount();
    }

    /**
     * Общее количество долларов в обороте, не находящиеся в стэйкинге.
     */
    @GetMapping("/totalDollars")
    public double getTotalDollars() {
        try {
            Map<String, Account> balances = UtilsAccountToEntityAccount.entityAccountsToMapAccounts(blockService.findAllAccounts());
            if (totalDollars == 0) {
                for (Map.Entry<String, Account> account : balances.entrySet()) {
                    totalDollars += account.getValue().getDigitalDollarBalance();
                }

            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return totalDollars;
    }

    /**
     * Общее количество транзакций совершенных в день
     */
    @GetMapping("/totalTransactionsDay")
    public int getTotalTransactionsDays() {
        return totalTransactionsDays;
    }

    /**
     * Общее количество денег переведенных в день.
     */
    @GetMapping("/totalTransactionsSum")
    public double getTotalTransactionsSumDllar() {
        return totalTransactionsSumDllar;
    }

    /**
     * Мультипликатор, который определяет какой максимальный доход может быть добыт в этом году,
     * каждый год он уменьшается, но не будет ниже единицы. Аналог халвинга для биткоина, но более
     * плавный. В первый год начал работать с 29
     */
    @GetMapping("/multiplier")
    public long multiplier() {
        long money = Seting.MULTIPLIER;
        if (prevBlock.getIndex() > Seting.V28_CHANGE_ALGORITH_DIFF_INDEX) {
            money = (prevBlock.getIndex() - Seting.V28_CHANGE_ALGORITH_DIFF_INDEX)
                    / (576 * Seting.YEAR);
            money = (long) (Seting.MULTIPLIER - money);
            money = money < 1 ? 1 : money;
        }
        return money;
    }


    /**
     * Обратный отсчет для снижения мультипликатора, вычисляет сколько дней осталось до
     * следующего снижения
     */
    @GetMapping("/dayReduce")
    public long daysReduce() {
        long reduceDays = 0;
        if (prevBlock.getIndex() > Seting.V28_CHANGE_ALGORITH_DIFF_INDEX) {
            int blocksPerDay = 576;
            int blocksSinceReduction = (int) ((prevBlock.getIndex() - Seting.V28_CHANGE_ALGORITH_DIFF_INDEX)
                    % (blocksPerDay * Seting.YEAR));

            // Оставшиеся блоки до следующего снижения
            reduceDays = (blocksPerDay * Seting.YEAR) - blocksSinceReduction;

        }
        return reduceDays;
    }

    public static Set<String> getExcludedAddresses() {
        HttpServletRequest request = getCurrentRequest();
        if (request == null)
            return excludedAddresses;

        String scheme = request.getScheme();
        String serverName = request.getServerName();
        int serverPort = request.getServerPort();
        String contextPath = request.getContextPath();  // includes leading forward slash

        String localaddress = scheme + "://" + serverName + ":" + serverPort;

        excludedAddresses.add(localaddress);
        return excludedAddresses;
    }

    public static void setExcludedAddresses(Set<String> excludedAddresses) {
        BasisController.excludedAddresses = excludedAddresses;
    }


    /**
     * Возвращает список хостов
     */
    public static Set<String> getNodes() {


        Set<String> nodes = new HashSet<>();


        try {

            Set<String> temporary = UtilsAllAddresses.readLineObject(Seting.ORIGINAL_POOL_URL_ADDRESS_FILE);

            nodes.addAll(temporary);

            nodes = nodes.stream()
                    .filter(t -> !t.isBlank()).map(t -> t.replaceAll("\"", "")).collect(Collectors.toSet());

            Set<String> bloked = UtilsAllAddresses.readLineObject(Seting.ORIGINAL_POOL_URL_ADDRESS_BLOCKED_FILE);
            nodes.removeAll(bloked);
            nodes.removeAll(Seting.ORIGINAL_BLOCKED_ADDRESS);
            nodes.addAll(Seting.ORIGINAL_ADDRESSES);

            System.out.println("standard nodes: " + nodes);
        } catch (Exception e) {
            e.printStackTrace();
        }


        return nodes;
    }

    @GetMapping("/getNodes")
    @ResponseBody
    public Set<String> getAllNodes() {


        Set<String> nodes = new HashSet<>();

        try {
            Set<String> temporary = UtilsAllAddresses.readLineObject(Seting.ORIGINAL_POOL_URL_ADDRESS_FILE);

            nodes.addAll((new HashSet<>(temporary)));

            nodes = nodes.stream()
                    .filter(t -> !t.isBlank()).map(t -> t.replaceAll("\"", "")).collect(Collectors.toSet());

            Set<String> bloked = UtilsAllAddresses.readLineObject(Seting.ORIGINAL_POOL_URL_ADDRESS_BLOCKED_FILE);
            nodes.removeAll((new HashSet<>(bloked)));
            nodes.removeAll(Seting.ORIGINAL_BLOCKED_ADDRESS);
            nodes.addAll((new HashSet<>(Seting.ORIGINAL_ADDRESSES)));

            System.out.println("nodes: " + nodes);
        } catch (Exception e) {
            e.printStackTrace();
        }


        return nodes;
    }


    static {
        try {

            UtilsCreatedDirectory.createPackages();
//            blockchain = BLockchainFactory.getBlockchain(BlockchainFactoryEnum.ORIGINAL);
//            blockchain = Mining.getBlockchain(
//                    Seting.ORIGINAL_BLOCKCHAIN_FILE,
//                    BlockchainFactoryEnum.ORIGINAL);
            String json = UtilsFileSaveRead.read(Seting.TEMPORARY_BLOCKCHAIN_FILE);
            if (!json.isEmpty() || !json.isBlank()) {
                shortDataBlockchain = UtilsJson.jsonToDataShortBlockchainInformation(json);

            } else {
                shortDataBlockchain = Blockchain.checkFromFile(Seting.ORIGINAL_BLOCKCHAIN_FILE);

//            prevBlock = UtilsBlockToEntityBlock.entityBlockToBlock(BlockService.findById((long) blockchainSize+1));

                json = UtilsJson.objToStringJson(shortDataBlockchain);
                UtilsFileSaveRead.save(json, Seting.TEMPORARY_BLOCKCHAIN_FILE, false);
            }
            System.out.println("static: shortDataBlockchain: " + shortDataBlockchain);
            blockcheinSize = (int) shortDataBlockchain.getSize();
            blockchainValid = shortDataBlockchain.isValidation();
            prevBlock = Blockchain.indexFromFile(blockcheinSize - 1, Seting.ORIGINAL_BLOCKCHAIN_FILE);


            if(blockcheinSize < Seting.V34_NEW_ALGO){
            if (blockcheinSize > 600) {
                dificultyOneBlock = UtilsBlock.difficulty(Blockchain.subFromFile(
                                blockcheinSize - 600, blockcheinSize, Seting.ORIGINAL_BLOCKCHAIN_FILE),
                        Seting.BLOCK_GENERATION_INTERVAL,
                        Seting.DIFFICULTY_ADJUSTMENT_INTERVAL);
            } else {
                dificultyOneBlock = UtilsBlock.difficulty(Blockchain.subFromFile(
                                blockcheinSize - 600, blockcheinSize, Seting.ORIGINAL_BLOCKCHAIN_FILE),
                        Seting.BLOCK_GENERATION_INTERVAL,
                        Seting.DIFFICULTY_ADJUSTMENT_INTERVAL);
            }
            }else {
                dificultyOneBlock = prevBlock().getHashCompexity();
            }


        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        } catch (InvalidKeySpecException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (SignatureException e) {
            throw new RuntimeException(e);
        } catch (NoSuchProviderException e) {
            throw new RuntimeException(e);
        } catch (InvalidKeyException e) {
            throw new RuntimeException(e);
        }
    }

    public BasisController() {
    }

    //TODO если вы прервали mine, то перед следующим вызовом перезапустите сервер и вызовите /addBlock перед mine
    //TODO if you interrupted mine, restart the server before next call and call /addBlock before mine
    //TODO иначе будет расождение в файле балансов
    //TODO otherwise there will be a discrepancy in the balance file


    /**
     * возвращяет размер локального блокчейна
     */
    @GetMapping("/size")
    @ResponseBody
    public Integer sizeBlockchain() throws IOException, NoSuchAlgorithmException, InvalidKeySpecException, SignatureException, NoSuchProviderException, InvalidKeyException, InterruptedException, CloneNotSupportedException {
//        System.out.println("start /size");


        if (blockcheinSize == 0) {
            System.out.println("blockchain is 0 blockchainSize " + blockcheinSize);
//
            shortDataBlockchain = Blockchain.checkFromFile(Seting.ORIGINAL_BLOCKCHAIN_FILE);
            blockcheinSize = (int) shortDataBlockchain.getSize();
            blockchainValid = shortDataBlockchain.isValidation();
            prevBlock = Blockchain.indexFromFile(blockcheinSize - 1, Seting.ORIGINAL_BLOCKCHAIN_FILE);

        }


        if (blockchainValid == false) {
            System.out.println("/size blockchain not valid: " + blockchainValid);

//            UtilsBlock.deleteFiles();
//            return 1;
            System.exit(0);
        }
//        System.out.println("finish /size");
        return blockcheinSize; //blockchain.sizeBlockhain();
    }

    /**
     * Возвращает список блоков ОТ до ДО,
     */
    @PostMapping("/sub-blocks")
    @ResponseBody
    public List<Block> subBlocks(@RequestBody SubBlockchainEntity entity) {
        List<Block> blocksDb = new ArrayList<>();
        try {
            System.out.println("************************************");
            System.out.println("subBlocks");
            System.out.println("valid: " + blockchainValid);
            System.out.println("size:" + blockcheinSize);


            if (blockchainValid == false || blockcheinSize == 0) {

                shortDataBlockchain = Blockchain.checkFromFile(Seting.ORIGINAL_BLOCKCHAIN_FILE);
                blockcheinSize = (int) shortDataBlockchain.getSize();
                blockchainValid = shortDataBlockchain.isValidation();
            }
            //TODO если что можно это включить
            if (!isSaveFile) {
                System.out.println("saving file: resolve_from_to_block: sub block");
                return new ArrayList<>();
            }

            int start = entity.getStart() >= 1 ? entity.getStart() : 0;
            int finish = entity.getFinish() > start ? entity.getFinish() : blockcheinSize - 1;
            if (start >= blockcheinSize || finish > blockcheinSize) {

                System.out.println("start >= blockcheinSize || finish > blockcheinSize");
                return new ArrayList<>();
            }

            //проверить на ограничение порций
            if (finish - start > Seting.PORTION_BLOCK_TO_COMPLEXCITY) {
                if (finish - start < blockcheinSize) {
                    finish = start + Seting.PORTION_BLOCK_TO_COMPLEXCITY;
                } else {
                    finish = blockcheinSize - 1;
                }
            }


            List<EntityBlock> entityBlocks =
                    blockService.findBySpecialIndexBetween(start, finish);
            blocksDb = UtilsBlockToEntityBlock.entityBlocksToBlocks(entityBlocks);

//        return Blockchain.subFromFileBing(start,finish, Seting.ORIGINAL_BLOCKCHAIN_FILE);
            System.out.println("finish sub: " + finish);
            System.out.println("******************************************************");
        } catch (IOException | NoSuchAlgorithmException | InvalidKeySpecException | SignatureException |
                 NoSuchProviderException | InvalidKeyException e) {
            System.out.println("exception sub");
            return new ArrayList<>();

        } finally {
            return blocksDb;
        }

    }


    @GetMapping("/version")
    @ResponseBody
    public double version() {
        return Seting.VERSION;
    }


    /**
     * Возвращяет блок по индексу
     */

    @PostMapping("/block")
    @ResponseBody
    @Async("threadPoolTaskExecutor")
    public Block getBlock(@RequestBody Integer index) {
        try {
//        System.out.println("start getBlock");
            if (blockchainValid == false || blockcheinSize == 0) {
//            blockchain = Mining.getBlockchain(
//                    Seting.ORIGINAL_BLOCKCHAIN_FILE,
//                    BlockchainFactoryEnum.ORIGINAL);
                shortDataBlockchain = Blockchain.checkFromFile(Seting.ORIGINAL_BLOCKCHAIN_FILE);
                blockcheinSize = (int) shortDataBlockchain.getSize();
                blockchainValid = shortDataBlockchain.isValidation();
            }
//        System.out.println("finish getBlock");
//        return Blockchain.indexFromFileBing(index, Seting.ORIGINAL_BLOCKCHAIN_FILE);

            if (index < 0) {
                index = 0;
            }
            if (index > blockcheinSize - 1) {
                index = blockcheinSize - 1;
            }
            return UtilsBlockToEntityBlock.entityBlockToBlock(
                    blockService.findBySpecialIndex(index)
            );
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }


    @GetMapping("/isSaveFile")
    @ResponseBody
    public boolean isSaveFile() {
        return isSaveFile;
    }

    /**
     * возвращает баланс аккаунта
     */
    @GetMapping("/balance")
    @ResponseBody
    public Account getBalance(@RequestParam String address) {
        try {
            return UtilsAccountToEntityAccount.entityAccountToAccount(blockService.findByAccount(address));

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }



    @GetMapping("/prevBlock")
    @ResponseBody
    public Block getPrevBlock() {
        return prevBlock;
    }


    /**
     * метод добавляет блоки в список ожидания, после чего их них уже формируется кандидаты и победитель
     */
    @PostMapping("/nodes/resolve_from_to_block")
    public synchronized ResponseEntity<String> resolve_conflict(@RequestBody SendBlocksEndInfo sendBlocksEndInfo) {
        try {

            if(!blockedNewSendBlock.get()){
                return new ResponseEntity<>("START SYNCHORNIZATION", HttpStatus.OK);
            }
            UtilsBalance.setBlockService(blockService);
            Blockchain.setBlockService(blockService);
            UtilsBlock.setBlockService(blockService);

            System.out.println("start resolve_from_to_block: " + sendBlocksEndInfo.getList().get(0).getMinerAddress());
            System.out.println("isSave: " + isSaveFile);


            if (sendBlocksEndInfo.getVersion() != Seting.VERSION) {
                System.out.println("wrong version version " + Seting.VERSION + " but: " + sendBlocksEndInfo.getVersion());
                return new ResponseEntity<>("FALSE", HttpStatus.FAILED_DEPENDENCY);
            }

            List<Block> blocks = sendBlocksEndInfo.getList();
            Map<String, Account> balances = UtilsAccountToEntityAccount.entityAccountsToMapAccounts(UtilsUse.accounts(blocks, blockService));

            ///последовательность временных меток
            if (prevBlock.getTimestamp().getTime() > blocks.get(blocks.size() - 1).getTimestamp().getTime()) {
                System.out.println("wrong time: prev uper now");
                return new ResponseEntity<>("FALSE", HttpStatus.EXPECTATION_FAILED);
            }
            String addressMiner = null;
            if (blocks.get(blocks.size() - 1).getMinerAddress() != null && !blocks.get(blocks.size() - 1).getMinerAddress().isEmpty()) {
                addressMiner = blocks.get(blocks.size() - 1).getMinerAddress();

            } else {
                System.out.println("wrong: empty address;");
                return new ResponseEntity<>("FALSE", HttpStatus.EXPECTATION_FAILED);
            }

            try {

                List<Block> addlist = Blockchain.clone(0, blocks.size(), blocks);

                if(addlist.isEmpty()
                        || addlist.get(0).getIndex() != BasisController.getBlockchainSize()
                 || winnerList.contains(addlist)){
                    MyLogger.saveLog("this block has in system: ");
                    return new ResponseEntity<>("FALSE", HttpStatus.OK);
                }
                System.out.println("account: " + addressMiner);
                Account account = balances.get(addressMiner);
                if (account == null) {
                    account = new Account(addressMiner, 0, 0, 0);
                }


                Timestamp actualTime = new Timestamp(UtilsTime.getUniversalTimestamp());
                Timestamp lastIndex = addlist.get(addlist.size() - 1).getTimestamp();

                Long result = actualTime.toInstant().until(lastIndex.toInstant(), ChronoUnit.MINUTES);
                System.out.println("different time: " + result);


                if (prevBlock == null) {
                    EntityBlock tempBlock = blockService.findBySpecialIndex(blockcheinSize - 1);
                    prevBlock = UtilsBlockToEntityBlock.entityBlockToBlock(tempBlock);
                }
                if (shortDataBlockchain.getSize() == 0
                        || !shortDataBlockchain.isValidation()
                        || shortDataBlockchain.getHashCount() == 0) {
                    shortDataBlockchain = Blockchain.checkFromFile(Seting.ORIGINAL_BLOCKCHAIN_FILE);
                }
                List<Block> lastDiff = new ArrayList<>();

                if (prevBlock().getIndex() < Seting.V34_NEW_ALGO) {
                    lastDiff = UtilsBlockToEntityBlock.entityBlocksToBlocks(
                            blockService.findBySpecialIndexBetween(
                                    (prevBlock.getIndex() + 1) - Seting.PORTION_BLOCK_TO_COMPLEXCITY,
                                    prevBlock.getIndex() + 1
                            )
                    );
                }


                //удаление транзакций
                if (prevBlock.getIndex() % 288 == 0)
                    Mining.deleteFiles(Seting.ORGINAL_ALL_TRANSACTION_FILE);
                if (prevBlock.getIndex() % 288 == 0)
                    Mining.deleteFiles(Seting.ORIGINAL_ALL_SENDED_TRANSACTION_FILE);

                List<String> sign = new ArrayList<>();
                Map<String, Account> tempBalances = UtilsAccountToEntityAccount.entityAccountsToMapAccounts(UtilsUse.accounts(addlist, blockService));
                tempBalances.putAll(UtilsAccountToEntityAccount.entityAccountsToMapAccounts(UtilsUse.accounts(lastDiff, blockService)));
                DataShortBlockchainInformation temp = Blockchain.shortCheck(prevBlock, addlist, shortDataBlockchain, lastDiff, tempBalances, sign);// Blockchain.checkEqualsFromToBlockFile(Seting.ORIGINAL_BLOCKCHAIN_FILE, addlist);

                System.out.println("<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<");
                System.out.println("original: " + shortDataBlockchain);
                System.out.println("temp: " + temp);


                System.out.println("address mininer: " + blocks.get(blocks.size() - 1).getMinerAddress());
                System.out.println("<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<");
                System.out.println("addList size: " + addlist.size());
                if (blockcheinSize == 0 || blockchainValid == false) {

                    shortDataBlockchain = Blockchain.checkFromFile(Seting.ORIGINAL_BLOCKCHAIN_FILE);
                    blockcheinSize = (int) shortDataBlockchain.getSize();
                    blockchainValid = shortDataBlockchain.isValidation();
//                    prevBlock = Blockchain.indexFromFileBing(blockcheinSize - 1, Seting.ORIGINAL_BLOCKCHAIN_FILE);
                    EntityBlock tempBlock = blockService.findBySpecialIndex(blockcheinSize - 1);
                    prevBlock = UtilsBlockToEntityBlock.entityBlockToBlock(tempBlock);
                    String json = UtilsJson.objToStringJson(shortDataBlockchain);
                    UtilsFileSaveRead.save(json, Seting.TEMPORARY_BLOCKCHAIN_FILE, false);
                }

                if (!shortDataBlockchain.isValidation()) {
                    System.out.println("wrong block chain, delete blocks: from to block:");
                    System.exit(0);
                    return new ResponseEntity<>("please retry  wrong blockchain in storage", HttpStatus.CONFLICT);
                }


                if (temp.isValidation()) {
                    System.out.println("from to block is valid");

                } else {
                    if (temp.getSize() > shortDataBlockchain.getSize() && temp.getHashCount() > shortDataBlockchain.getHashCount()) {
                        System.out.println("code error: " + HttpStatus.CONFLICT);
                        System.out.println("miner: " + account);
                        return new ResponseEntity<>("FALSE", HttpStatus.CONFLICT);
                    }
                    return new ResponseEntity<>("FALSE", HttpStatus.EXPECTATION_FAILED);
                }


                if (temp.getSize() > shortDataBlockchain.getSize()
                        && temp.getHashCount() >= shortDataBlockchain.getHashCount()) {


                    System.out.println("*************************************");
                    System.out.println("before original: " + shortDataBlockchain);
                    System.out.println("before temp: " + temp);


                    winnerList.addAll(addlist);
                    utilsResolving.sendAllBlocksToStorage(addlist);

                    //прибавить к общей сумме денег

                    dificultyOneBlock = prevBlock().getHashCompexity();

                    System.out.println("after original: " + shortDataBlockchain);
                    System.out.println("after temp: " + temp);

                    return new ResponseEntity<>("OK", HttpStatus.OK);
                } else {
                    return new ResponseEntity<>("FALSE", HttpStatus.EXPECTATION_FAILED);
                }


            } catch (Exception e) {
                throw new RuntimeException(e);
            } finally {

                System.out.println("finish resolve_from_to_block");
            }

        } catch (Exception e) {
            e.printStackTrace();
            MyLogger.saveLog("resolve_conflict: ", e);
            isSaveFile = true;
            return new ResponseEntity<>("FALSE", HttpStatus.EXPECTATION_FAILED);
        } finally {

            System.out.println("finish resolve_from_to_block");
        }
    }


    @RequestMapping(method = RequestMethod.POST, value = "/nodes/register", consumes = MediaType.APPLICATION_JSON_VALUE)
    public synchronized void register_node(@RequestBody AddressUrl urlAddrress) {

        try {

            for (String s : BasisController.getNodes()) {
                String original = s;
                String url = s + "/nodes/register";

                try {
                    UtilUrl.sendPost(urlAddrress.getAddress(), url);
                    sendAddress();


                } catch (Exception e) {
                    System.out.println("BasisController: register node: wrong node: " + original);
                    BasisController.getNodes().remove(original);
                    continue;
                }
            }

            Set<String> nodes = BasisController.getNodes();
            nodes = nodes.stream()
                    .map(t -> t.replaceAll("\"", ""))
                    .map(t -> t.replaceAll("\\\\", ""))
                    .collect(Collectors.toSet());
            nodes.add(urlAddrress.getAddress());
//            BasisController.setNodes(nodes);

            Mining.deleteFiles(Seting.ORIGINAL_POOL_URL_ADDRESS_FILE);
            nodes.stream().forEach(t -> {
                try {
                    UtilsAllAddresses.saveAllAddresses(t, Seting.ORIGINAL_POOL_URL_ADDRESS_FILE);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                } catch (NoSuchAlgorithmException e) {
                    throw new RuntimeException(e);
                } catch (SignatureException e) {
                    throw new RuntimeException(e);
                } catch (InvalidKeySpecException e) {
                    throw new RuntimeException(e);
                } catch (NoSuchProviderException e) {
                    throw new RuntimeException(e);
                } catch (InvalidKeyException e) {
                    throw new RuntimeException(e);
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        } finally {

        }

    }


    @GetMapping("/findAddresses")
    public void findAddresses() {
        for (String s : Seting.ORIGINAL_ADDRESSES) {
            Set<String> addressesSet = new HashSet<>();
            try {
                String addresses = UtilUrl.readJsonFromUrl(s + "/getDiscoveryAddresses");
                addressesSet = UtilsJson.jsonToSetAddresses(addresses);
            } catch (IOException e) {
                System.out.println("BasisController: findAddress: error");
                continue;
            } catch (JSONException e) {
                throw new RuntimeException(e);
            }

            for (String s1 : addressesSet) {

                register_node(new AddressUrl(s1));
            }

        }

    }

    public static void sendAddress() {
        //лист временный для отправки аддресов

        for (String s : Seting.ORIGINAL_ADDRESSES) {

            String original = s;
            String url = s + "/nodes/register";

            if (BasisController.getExcludedAddresses().contains(url)) {
                System.out.println("MainController: its your address or excluded address: " + url);
                continue;
            }
            try {
                for (String s1 : BasisController.getNodes()) {


                    AddressUrl addressUrl = new AddressUrl(s1);
                    String json = UtilsJson.objToStringJson(addressUrl);
                    UtilUrl.sendPost(json, url);
                }
            } catch (Exception e) {
                System.out.println("BasisController: sendAddress: wronge node: " + original);

                continue;
            }


        }
    }

    /**
     * возвращает информацию о сложности текущей в блокчейне и суммарном
     */
    @GetMapping("/difficultyBlockchain")
    public InfoDificultyBlockchain dificultyBlockchain() {
        InfoDificultyBlockchain dificultyBlockchain = new InfoDificultyBlockchain();
        dificultyBlockchain.setDifficultyAllBlockchain(shortDataBlockchain.getHashCount());
        dificultyBlockchain.setDiffultyOneBlock(dificultyOneBlock);
        return dificultyBlockchain;
    }


    /**
     * Получить список транзакций для адреса отправителя, от определенного блока до определенного блока
     */
    @GetMapping("/senderTransactions")
    @ResponseBody
    public List<DtoTransaction> senderTransactions(
            @RequestParam String address,
            @RequestParam int from,
            @RequestParam int to
    ) {
        try {
            return blockService.findBySender(address, from, to);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Получить список транзакций для адреса получателя, от определенного блока до определенного блока
     */

    @GetMapping("/customerTransactions")
    @ResponseBody
    public List<DtoTransaction> customerTransactions(
            @RequestParam String address,
            @RequestParam int from,
            @RequestParam int to
    ) {
        try {
            return blockService.findByCustomer(address, from, to);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * количество отправленных транзакций от данного адреса
     */
    @GetMapping("/senderCountDto")
    @ResponseBody
    public long countSenderTransaction(
            @RequestParam String address
    ) {
        try {
            return blockService.countSenderTransaction(address);

        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }
    }

    /**
     * количество полученных транзакций от данного адреса
     */

    @GetMapping("/customerCountDto")
    @ResponseBody
    public long countCustomerTransaction(
            @RequestParam String address
    ) {
        try {
            return blockService.countCustomerTransaction(address);

        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }
    }


    /**
     * Получить все балансы данного адреса
     */
    @GetMapping("/addresses")
    @ResponseBody
    public Map<String, Account> addresses() {
        Map<String, Account> accountMap = new HashMap<>();
        try {
            accountMap = UtilsAccountToEntityAccount
                    .entityAccountsToMapAccounts(blockService.findAllAccounts());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return accountMap;
    }


    @GetMapping("/timentp")
    @ResponseBody
    public Long timentp(){
        return UtilsTime.getUniversalTimestamp();
    }
}


