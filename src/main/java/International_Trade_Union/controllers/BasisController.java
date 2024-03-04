package International_Trade_Union.controllers;

import International_Trade_Union.controllers.config.BlockchainFactoryEnum;
import International_Trade_Union.entity.*;
import International_Trade_Union.entity.DtoTransaction.DtoTransaction;
import International_Trade_Union.entity.blockchain.DataShortBlockchainInformation;
import International_Trade_Union.entity.entities.EntityAccount;
import International_Trade_Union.entity.entities.EntityBlock;
import International_Trade_Union.entity.services.BlockService;
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
import java.util.stream.Collectors;

import static International_Trade_Union.utils.UtilsBalance.calculateBalance;

@RestController
public class BasisController {
    @Autowired
    BlockService blockService;


    private static CopyOnWriteArrayList<Block> winnerList = new CopyOnWriteArrayList<>();

    private static   boolean updating;
    //список всех победителей
    private static List<LiteVersionWiner> allWiners = new ArrayList<>();
    private static List<LiteVersionWiner> powerWiners = new ArrayList<>();
    private static List<LiteVersionWiner> countTransactionsWiner = new ArrayList<>();
    private static List<LiteVersionWiner> stakingWiners = new ArrayList<>();
    private static List<LiteVersionWiner> bigRandomWiner = new ArrayList<>();

    public static void setTotalTransactionsDays(int totalTransactionsDays) {
        BasisController.totalTransactionsDays = totalTransactionsDays;
    }
    public static long totalTransactionsDays(){
        return BasisController.totalTransactionsDays;
    }
    public static double totalTransactionsSumDollar(){
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
    public static double totalDollars(){
        return totalDollars;
    }

    public static void setUpdating(boolean b) {
        updating = b;
    }
    public static boolean getUpdating(){
        return updating;
    }

    @GetMapping("/allwinners")
    @ResponseBody
    public String allWinners() throws IOException {
        String json = UtilsJson.objToStringJson(allWiners);
        return json;
    }

    @GetMapping("/powerWiners")
    @ResponseBody
    public String powerWiners() throws IOException {
        String json = UtilsJson.objToStringJson(powerWiners);
        return json;
    }

    @GetMapping("/countTransactionsWiner")
    @ResponseBody
    public String countTransactionsWiner() throws IOException {
        String json = UtilsJson.objToStringJson(countTransactionsWiner);
        return json;
    }

    @GetMapping("/stakingWiners")
    @ResponseBody
    public String stakingWiners() throws IOException {
        String json = UtilsJson.objToStringJson(stakingWiners);
        return json;
    }

    @GetMapping("/bigRandomWiner")
    @ResponseBody
    public String bigRandomWiner() throws IOException {
        String json = UtilsJson.objToStringJson(bigRandomWiner);
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
    private static double totalTransactionsSumDllar =0.0;

    @Autowired
    Tournament tournament;



    @Autowired
    UtilsAddBlock utilsAddBlock;
    private static Map<String, Account> balances = new HashMap<>();
    private static double totalDollars = 0;
    private static long dificultyOneBlock;
    private volatile static boolean isSaveFile = true;
    private static Block prevBlock = null;
    private static DataShortBlockchainInformation shortDataBlockchain = null;
    private static int blockcheinSize = 0;
    private static boolean blockchainValid = false;
    //    private static Blockchain blockchain;
    private static Set<String> excludedAddresses = new HashSet<>();
    private static boolean isSave = true;

    public static void setBlockchainValid(boolean blockchainValid) {
        BasisController.blockchainValid = blockchainValid;
    }

    public static void setShortDataBlockchain(DataShortBlockchainInformation shortDataBlockchain) {
        BasisController.shortDataBlockchain = shortDataBlockchain;
    }
    public static Block prevBlock(){
        return prevBlock;
    }
    public static void changePrevBlock(Block block){
        prevBlock = block;
    }

    @GetMapping("/datashort")
    public DataShortBlockchainInformation dataShortBlockchainInformation(){
        System.out.println("get /datashort");
        DataShortBlockchainInformation temp = shortDataBlockchain;
        System.out.println("/datashort: " + temp);
        return temp;
    }

    public static void setPrevBlock(Block prevBlock) {
        BasisController.prevBlock = prevBlock;
    }

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
    @GetMapping("/status")
    @ResponseBody
    public String status() throws JsonProcessingException {

        String strIsSave ="isSave: "+ isIsSave() + "\n";
        String strBlockchainSize = "blockchainSize: " + getBlockcheinSize() + "\n";
        String isSaveFile = "isSaveFile: "+ isSaveFile() + "\n";
        String blockFromDb =
               "blockFromDb: " +String.valueOf(blockService.findBySpecialIndex(blockcheinSize-1))
                + "\n";
        String blockFromFile = "*********************************\nblockFromFile: " + Blockchain.indexFromFileBing(blockcheinSize-1, Seting.ORIGINAL_BLOCKCHAIN_FILE)
                + "\n";

        String result = strIsSave + strBlockchainSize + isSaveFile + blockFromDb + blockFromFile;

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

    public static int getBlockcheinSize() {
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

    public static Map<String, Account> getBalances() {
        return balances;
    }

    public static void setBalances(Map<String, Account> balances) {
        BasisController.balances = balances;
    }

    @GetMapping("/v28Index")
    public int v28Start(){
        return Seting.V28_CHANGE_ALGORITH_DIFF_INDEX;
    }
    @GetMapping("/allAccounts")
    public long accounts(){
        return balances.size();
    }
    @GetMapping("/totalDollars")
    public double getTotalDollars(){
        if(totalDollars == 0){
            for (Map.Entry<String, Account> account : balances.entrySet()) {
                totalDollars += account.getValue().getDigitalDollarBalance();
            }

        }
        return totalDollars;
    }

    @GetMapping("/totalTransactionsDay")
    public int getTotalTransactionsDays(){
        return totalTransactionsDays;
    }

    @GetMapping("/totalTransactionsSum")
    public double getTotalTransactionsSumDllar(){
        return totalTransactionsSumDllar;
    }
    
    @GetMapping("/multiplier")
    public long multiplier(){
        long money = Seting.MULTIPLIER;
        if(prevBlock.getIndex() > Seting.V28_CHANGE_ALGORITH_DIFF_INDEX){
             money = (prevBlock.getIndex() - Seting.V28_CHANGE_ALGORITH_DIFF_INDEX)
                    / (576 * Seting.YEAR);
            money = (long) (Seting.MULTIPLIER - money);
            money = money < 1 ? 1: money;
        }
        return money;
    }



    @GetMapping("/dayReduce")
    public long daysReduce(){
        long reduceDays = 0;
        if(prevBlock.getIndex() > Seting.V28_CHANGE_ALGORITH_DIFF_INDEX){
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

    private static Set<String> nodes = new HashSet<>();
//    private static Nodes nodes = new Nodes();

    public static void setNodes(Set<String> nodes) {
        BasisController.nodes = nodes;
    }

    /**
     * Возвращает список хостов
     */
    public static Set<String> getNodes() throws IOException, NoSuchAlgorithmException, InvalidKeySpecException, SignatureException, NoSuchProviderException, InvalidKeyException {

        nodes = new HashSet<>();

        Set<String> temporary = UtilsAllAddresses.readLineObject(Seting.ORIGINAL_POOL_URL_ADDRESS_FILE);
        System.out.println("//getNodes temporary nodes: " + temporary);


        nodes.addAll(temporary);

        System.out.println("//getNodes  nodes: " + nodes);
        nodes = nodes.stream()
                .filter(t -> !t.isBlank())
                .filter(t -> t.startsWith("\""))
                .collect(Collectors.toSet());
        System.out.println("//getNodes after filter: " + nodes);
        nodes = nodes.stream()
                .filter(t->!t.isBlank()).map(t -> t.replaceAll("\"", "")).collect(Collectors.toSet());

        System.out.println("//getNodes after filter: " + nodes);
        Set<String> bloked = UtilsAllAddresses.readLineObject(Seting.ORIGINAL_POOL_URL_ADDRESS_BLOCKED_FILE);
        nodes.removeAll(bloked);
        nodes.removeAll(Seting.ORIGINAL_BLOCKED_ADDRESS);
        nodes.addAll(Seting.ORIGINAL_ADDRESSES);
        return nodes;
    }

    @GetMapping("/getNodes")
    @ResponseBody
    public Set<String> getAllNodes() throws IOException, NoSuchAlgorithmException, InvalidKeySpecException, SignatureException, NoSuchProviderException, InvalidKeyException {
        nodes = new HashSet<>();

        Set<String> temporary = UtilsAllAddresses.readLineObject(Seting.ORIGINAL_POOL_URL_ADDRESS_FILE);
        System.out.println("//getNodes temporary nodes: " + temporary);


        nodes.addAll(temporary);

        System.out.println("//getNodes  nodes: " + nodes);
        nodes = nodes.stream()
                .filter(t -> !t.isBlank())
                .filter(t -> t.startsWith("\""))
                .collect(Collectors.toSet());

        System.out.println("//getNodes after filter: " + nodes);
        nodes = nodes.stream()
                .filter(t->!t.isBlank()).map(t -> t.replaceAll("\"", "")).collect(Collectors.toSet());

        System.out.println("//getNodes after filter: " + nodes);
        Set<String> bloked = UtilsAllAddresses.readLineObject(Seting.ORIGINAL_POOL_URL_ADDRESS_BLOCKED_FILE);
        nodes.removeAll(bloked);
        nodes.removeAll(Seting.ORIGINAL_BLOCKED_ADDRESS);
        nodes.addAll(Seting.ORIGINAL_ADDRESSES);
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
            if(!json.isEmpty() || !json.isBlank()){
                shortDataBlockchain = UtilsJson.jsonToDataShortBlockchainInformation(json);

            }else {
                shortDataBlockchain = Blockchain.checkFromFile(Seting.ORIGINAL_BLOCKCHAIN_FILE);

//            prevBlock = UtilsBlockToEntityBlock.entityBlockToBlock(BlockService.findById((long) blockchainSize+1));

                json = UtilsJson.objToStringJson(shortDataBlockchain);
                UtilsFileSaveRead.save(json, Seting.TEMPORARY_BLOCKCHAIN_FILE, false);
            }
            System.out.println("static: shortDataBlockchain: " + shortDataBlockchain);
            blockcheinSize = (int) shortDataBlockchain.getSize();
            blockchainValid = shortDataBlockchain.isValidation();
            prevBlock = Blockchain.indexFromFile(blockcheinSize - 1, Seting.ORIGINAL_BLOCKCHAIN_FILE);



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

//            balances = SaveBalances.readLineObject(Seting.ORIGINAL_BALANCE_FILE);
//            balances = UtilsAccountToEntityAccount.entityAccountsToMapAccounts(BlockService.findAllAccounts());
//            if (balances.isEmpty()) {
//                Blockchain.saveBalanceFromfile(Seting.ORIGINAL_BLOCKCHAIN_FILE);
////                balances = SaveBalances.readLineObject(Seting.ORIGINAL_BALANCE_FILE);
//                balances = UtilsAccountToEntityAccount.entityAccountsToMapAccounts(BlockService.findAllAccounts());
//            } else {
////                balances = SaveBalances.readLineObject(Seting.ORIGINAL_BALANCE_FILE);
//                balances = UtilsAccountToEntityAccount.entityAccountsToMapAccounts(BlockService.findAllAccounts());
//
//            }


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
            UtilsBlock.deleteFiles();
            return 1;
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
            while (!isSaveFile) {
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
        }catch (IOException | NoSuchAlgorithmException | InvalidKeySpecException | SignatureException |
                NoSuchProviderException | InvalidKeyException e){
            System.out.println("exception sub");
            return new ArrayList<>();

        }finally {
            return blocksDb;
        }

    }

    /**
     * Возвращяет блок по индексу
     */

    @GetMapping("/version")
    @ResponseBody
    public double version() {
        return Seting.VERSION;
    }

    @PostMapping("/block")
    @ResponseBody
    public Block getBlock(@RequestBody Integer index) throws IOException, NoSuchAlgorithmException, InvalidKeySpecException, SignatureException, NoSuchProviderException, InvalidKeyException {
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

        if(index < 0 ){
            index = 0;
        }
        if(index > blockcheinSize -1){
            index = blockcheinSize - 1;
        }
        return UtilsBlockToEntityBlock.entityBlockToBlock(
                blockService.findBySpecialIndex(index)
        );
    }


    public  void addBlock(List<Block> orignalBlocks) throws IOException, NoSuchAlgorithmException, SignatureException, InvalidKeySpecException, NoSuchProviderException, InvalidKeyException {
        System.out.println("start addBLock");
        isSave = false;
        System.out.println("start  save in addBlock");
        List<String> signs = new ArrayList<>();

        if(balances == null || balances.isEmpty()){
            balances = UtilsAccountToEntityAccount.entityAccountsToMapAccounts(blockService.findAllAccounts());

        }

        List<LawEligibleForParliamentaryApproval> allLawsWithBalance = new ArrayList<>();
        List<EntityBlock> entityBlocks = new ArrayList<>();
        for (Block block : orignalBlocks) {
            UtilsBlock.saveBLock(block, Seting.ORIGINAL_BLOCKCHAIN_FILE);
            calculateBalance(balances, block, signs);

            EntityBlock entityBlock = UtilsBlockToEntityBlock.blockToEntityBlock(block);
            entityBlocks.add(entityBlock);


        }

        blockService.saveAllBlock(entityBlocks);
        List<EntityAccount> entityBalances = UtilsAccountToEntityAccount
                .accountsToEntityAccounts(balances);
        blockService.saveAccountAll(entityBalances);
        System.out.println("finish save in addBlock");
        System.out.println("BasisController: addBlock: finish");


        Mining.deleteFiles(Seting.ORIGINAL_BALANCE_FILE);
        SaveBalances.saveBalances(balances, Seting.ORIGINAL_BALANCE_FILE);

        //возвращает все законы с балансом
        //rewriting all existing laws
        //перезапись всех действующих законов
        UtilsLaws.saveCurrentsLaws(allLawsWithBalance, Seting.ORIGINAL_ALL_CORPORATION_LAWS_WITH_BALANCE_FILE);


        isSave = true;
    }


    @GetMapping("/isSaveFile")
    @ResponseBody
    public boolean isSaveFile() {
        return isSaveFile;
    }

    @GetMapping("/balance")
    @ResponseBody
    public Account getBalance(@RequestParam String address) throws IOException, NoSuchAlgorithmException, SignatureException, InvalidKeySpecException, NoSuchProviderException, InvalidKeyException {
        if (balances == null ||balances.isEmpty()) {
//            Blockchain.saveBalanceFromfile(Seting.ORIGINAL_BLOCKCHAIN_FILE);
//            balances = SaveBalances.readLineObject(Seting.ORIGINAL_BALANCE_FILE);
            balances  = UtilsAccountToEntityAccount.entityAccountsToMapAccounts(blockService.findAllAccounts());
        }

        return balances.get(address);
    }

    public  void addBlock3(List<Block> originalBlocks, Map<String, Account> balances, String filename) throws IOException, NoSuchAlgorithmException, InvalidKeySpecException, SignatureException, NoSuchProviderException, InvalidKeyException, CloneNotSupportedException {
        java.sql.Timestamp lastIndex = new java.sql.Timestamp(UtilsTime.getUniversalTimestamp());
        if(balances == null || balances.isEmpty()){
            balances = UtilsAccountToEntityAccount.entityAccountsToMapAccounts(blockService.findAllAccounts());

        }
        List<EntityBlock> list = new ArrayList<>();
        List<String> signs = new ArrayList<>();
        Map<String, Laws> allLaws = new HashMap<>();
        List<LawEligibleForParliamentaryApproval> allLawsWithBalance = new ArrayList<>();

        for (Block block : originalBlocks) {
            System.out.println(" :BasisController: addBlock3: blockchain is being updated: ");
            UtilsBlock.saveBLock(block, filename);
            EntityBlock entityBlock = UtilsBlockToEntityBlock.blockToEntityBlock(block);
            list.add(entityBlock);
            calculateBalance(balances, block, signs);


            //получение и отображение законов, а также сохранение новых законов
            //и изменение действующих законов
            allLaws = UtilsLaws.getLaws(block, Seting.ORIGINAL_ALL_CORPORATION_LAWS_FILE, allLaws);

        }
        blockService.saveAllBlock(list);
        List<EntityAccount> entityBalances = UtilsAccountToEntityAccount
                .accountsToEntityAccounts(balances);
        blockService.saveAccountAll(entityBalances);

        Mining.deleteFiles(Seting.ORIGINAL_BALANCE_FILE);
        SaveBalances.saveBalances(balances, Seting.ORIGINAL_BALANCE_FILE);


        //возвращает все законы с балансом
        allLawsWithBalance = UtilsLaws.getCurrentLaws(allLaws, balances,
                Seting.ORIGINAL_ALL_CORPORATION_LAWS_WITH_BALANCE_FILE);

        //removal of obsolete laws
        //удаление устаревших законов
        Mining.deleteFiles(Seting.ORIGINAL_ALL_CORPORATION_LAWS_WITH_BALANCE_FILE);
        //rewriting all existing laws
        //перезапись всех действующих законов
        UtilsLaws.saveCurrentsLaws(allLawsWithBalance, Seting.ORIGINAL_ALL_CORPORATION_LAWS_WITH_BALANCE_FILE);


        java.sql.Timestamp actualTime = new java.sql.Timestamp(UtilsTime.getUniversalTimestamp());


        Long result = actualTime.toInstant().until(lastIndex.toInstant(), ChronoUnit.MILLIS);
        System.out.println("addBlock 3: time: result: " + result);
        System.out.println(":BasisController: addBlock3: finish: " + originalBlocks.size());

    }
    public  void getBlock() throws IOException, NoSuchAlgorithmException, InvalidKeySpecException, SignatureException, NoSuchProviderException, InvalidKeyException, CloneNotSupportedException {
        int size = 0;


        Blockchain blockchain = Mining.getBlockchain(
                Seting.ORIGINAL_BLOCKCHAIN_FILE,
                BlockchainFactoryEnum.ORIGINAL);


        UtilsFileSaveRead.deleteAllFiles(Seting.ORIGINAL_BLOCKCHAIN_FILE);
        UtilsFileSaveRead.deleteAllFiles(Seting.ORIGINAL_BALANCE_FILE);
        UtilsFileSaveRead.deleteAllFiles(Seting.ORIGINAL_ALL_CORPORATION_LAWS_FILE);
        UtilsFileSaveRead.deleteAllFiles(Seting.ORIGINAL_ALL_CORPORATION_LAWS_WITH_BALANCE_FILE);
        UtilsFileSaveRead.deleteAllFiles(Seting.BALANCE_REPORT_ON_DESTROYED_COINS);
        UtilsFileSaveRead.deleteAllFiles(Seting.ORIGINAL_POOL_URL_ADDRESS_FILE);
        UtilsFileSaveRead.deleteAllFiles(Seting.CURRENT_BUDGET_END_EMISSION);
//        UtilsFileSaveRead.deleteAllFiles(Seting.H2_DB);
//        UtilsCreatedDirectory.createPackage(Seting.H2_DB);
//        UtilsCreatedDirectory.createPackage(Seting.H2_DB+"db.mv.db");

        UtilsFileSaveRead.deleteFile(Seting.TEMPORARY_BLOCKCHAIN_FILE);
        blockService.deletedAll();
        List<Block> list = new ArrayList<>();

        Map<String, Account> balances = new HashMap<>();


        while (true){
            if(size > Seting.PORTION_DOWNLOAD){
                list = blockchain.subBlock(size, Seting.PORTION_DOWNLOAD);
                list = list.stream().sorted(Comparator.comparing(Block::getIndex)).collect(Collectors.toList());
                addBlock3(list, balances, Seting.ORIGINAL_BLOCKCHAIN_FILE);

            }else {
                list = blockchain.subBlock(size, blockchain.sizeBlockhain());
                list = list.stream().sorted(Comparator.comparing(Block::getIndex)).collect(Collectors.toList());
                addBlock3(list, balances, Seting.ORIGINAL_BLOCKCHAIN_FILE);
                break;
            }
            Block block = list.get(list.size()-1);
            size = (int) (block.getIndex());

        }


        shortDataBlockchain = Blockchain.checkFromFile(Seting.ORIGINAL_BLOCKCHAIN_FILE);
        blockcheinSize = (int) shortDataBlockchain.getSize();
        blockchainValid = shortDataBlockchain.isValidation();
    }

    @GetMapping("/prevBlock")
    @ResponseBody
    public Block getPrevBlock(){
        return prevBlock;
    }

    @PostMapping("/nodes/resolve_from_to_block")
    public synchronized ResponseEntity<String> resolve_conflict(@RequestBody SendBlocksEndInfo sendBlocksEndInfo) throws JSONException, NoSuchAlgorithmException, InvalidKeySpecException, IOException, SignatureException, NoSuchProviderException, InvalidKeyException, CloneNotSupportedException {
        try {
            if(balances == null || balances.isEmpty()){
                balances = UtilsAccountToEntityAccount.entityAccountsToMapAccounts(blockService.findAllAccounts());

            }

            System.out.println("start resolve_from_to_block: " + sendBlocksEndInfo.getList().get(0).getMinerAddress());
            System.out.println("isSave: " + isSaveFile);


            if (sendBlocksEndInfo.getVersion() != Seting.VERSION) {
                System.out.println("wrong version version " + Seting.VERSION + " but: " + sendBlocksEndInfo.getVersion());
                return new ResponseEntity<>("FALSE", HttpStatus.FAILED_DEPENDENCY);
            }
            List<Block> blocks = sendBlocksEndInfo.getList();

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
                System.out.println("account: " + addressMiner);
                Account account = balances.get(addressMiner);
                if (account == null) {
                    account = new Account(addressMiner, 0, 0, 0);
                }


                Timestamp actualTime = new Timestamp(UtilsTime.getUniversalTimestamp());
                Timestamp lastIndex = addlist.get(addlist.size() - 1).getTimestamp();

                Long result = actualTime.toInstant().until(lastIndex.toInstant(), ChronoUnit.MINUTES);
                System.out.println("different time: " + result);

                if(!Seting.IS_TEST){
                    if (
                            result > 400 || result < -400
                    ) {
                        System.out.println("_____________________________________________");
                        System.out.println("wrong timestamp");
                        System.out.println("new time 0 index: " + addlist.get(0).getTimestamp());
                        System.out.println("new time last index: " + addlist.get(addlist.size() - 1).getTimestamp());
                        System.out.println("actual time: " + actualTime);
                        System.out.println("result: " + result);
                        System.out.println("miner: " + addlist.get(addlist.size() - 1).getMinerAddress());

                        System.out.println("_____________________________________________");
                        return new ResponseEntity<>("FALSE", HttpStatus.EXPECTATION_FAILED);
                    }
                }


                if (prevBlock == null) {
//                    prevBlock = Blockchain.indexFromFileBing(blockcheinSize - 1, Seting.ORIGINAL_BLOCKCHAIN_FILE);
                    EntityBlock tempBlock = blockService.findBySpecialIndex(blockcheinSize - 1);
                    prevBlock = UtilsBlockToEntityBlock.entityBlockToBlock(tempBlock);
                }
                if (shortDataBlockchain.getSize() == 0
                        || !shortDataBlockchain.isValidation()
                        || shortDataBlockchain.getHashCount() == 0) {
                    shortDataBlockchain = Blockchain.checkFromFile(Seting.ORIGINAL_BLOCKCHAIN_FILE);
                }
                List<Block> lastDiff = new ArrayList<>();

//                    lastDiff = Blockchain.subFromFile(
//                        (int) (prevBlock.getIndex() - Seting.PORTION_BLOCK_TO_COMPLEXCITY),
//                        (int) (prevBlock.getIndex() + 1),
//                        Seting.ORIGINAL_BLOCKCHAIN_FILE
//                );

                lastDiff = UtilsBlockToEntityBlock.entityBlocksToBlocks(
                        blockService.findBySpecialIndexBetween(
                                (prevBlock.getIndex() + 1) - Seting.PORTION_BLOCK_TO_COMPLEXCITY,
                                prevBlock.getIndex() + 1
                        )
                );


                //удаление транзакций
                if (prevBlock.getIndex() % 288 == 0)
                    Mining.deleteFiles(Seting.ORGINAL_ALL_TRANSACTION_FILE);
                if (prevBlock.getIndex() % 288 == 0)
                    Mining.deleteFiles(Seting.ORIGINAL_ALL_SENDED_TRANSACTION_FILE);

                List<String> sign = new ArrayList<>();
                Map<String, Account> tempBalances = UtilsAccountToEntityAccount.entityAccountsToMapAccounts(blockService.findAllAccounts());
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
                    EntityBlock tempBlock = blockService.findBySpecialIndex(blockcheinSize-1);
                    prevBlock = UtilsBlockToEntityBlock.entityBlockToBlock(tempBlock);
                    String json = UtilsJson.objToStringJson(shortDataBlockchain);
                    UtilsFileSaveRead.save(json, Seting.TEMPORARY_BLOCKCHAIN_FILE, false);
                }

                if (!shortDataBlockchain.isValidation()) {
                    System.out.println("wrong block chain, delete blocks: from to block:");
                    UtilsBlock.deleteFiles();
//                blockchain.setBlockchainList(new ArrayList<>());
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

                    while (!isSaveFile) {
//            System.out.println("saving file: resolve_from_to_block");
                    }
                    isSaveFile = false;
                    System.out.println("*************************************");
                    System.out.println("before original: " + shortDataBlockchain);
                    System.out.println("before temp: " + temp);
//                    addBlock2(addlist, balances);

//                    utilsAddBlock.addBlock2(addlist, balances);
//                    String json = UtilsJson.objToStringJson(shortDataBlockchain);
//                    UtilsFileSaveRead.save(json, Seting.TEMPORARY_BLOCKCHAIN_FILE, false);

                    winnerList.addAll(addlist);
                    //прибавить к общей сумме денег


//                    balances = SaveBalances.readLineObject(Seting.ORIGINAL_BALANCE_FILE);
//                    shortDataBlockchain = temp;
//                    blockcheinSize = (int) shortDataBlockchain.getSize();
//                    blockchainValid = shortDataBlockchain.isValidation();

                    dificultyOneBlock = prevBlock().getHashCompexity();

                    System.out.println("after original: " + shortDataBlockchain);
                    System.out.println("after temp: " + temp);
//                    prevBlock = Blockchain.indexFromFileBing(blockcheinSize - 1, Seting.ORIGINAL_BLOCKCHAIN_FILE);

                    EntityBlock tempBlock = blockService.findBySpecialIndex(blockcheinSize-1);
                    prevBlock = UtilsBlockToEntityBlock.entityBlockToBlock(tempBlock);
                    System.out.println("*************************************");

                    System.out.println("*************************************");
////                    //задержка чтобы другие участники смогли скачать более актуальный блокчейн
//                    if(diff >= 9)
//                        Thread.sleep(30000);

                    return new ResponseEntity<>("OK", HttpStatus.OK);
                } else {
                    return new ResponseEntity<>("FALSE", HttpStatus.EXPECTATION_FAILED);
                }


            } catch (Exception e) {

//                prevBlock = Blockchain.indexFromFileBing(blockcheinSize - 1, Seting.ORIGINAL_BLOCKCHAIN_FILE);
//            resolve_conflicts();
                EntityBlock tempBlock = blockService.findBySpecialIndex(blockcheinSize-1);
                prevBlock = UtilsBlockToEntityBlock.entityBlockToBlock(tempBlock);
                isSaveFile = true;
                throw new RuntimeException(e);
            } finally {
//                prevBlock = Blockchain.indexFromFileBing(blockcheinSize - 1, Seting.ORIGINAL_BLOCKCHAIN_FILE);
                EntityBlock tempBlock = blockService.findBySpecialIndex(blockcheinSize-1);
                prevBlock = UtilsBlockToEntityBlock.entityBlockToBlock(tempBlock);
//            resolve_conflicts();
                isSaveFile = true;
                System.out.println("finish resolve_from_to_block");
            }

        } catch (Exception e) {
            e.printStackTrace();
//            prevBlock = Blockchain.indexFromFileBing(blockcheinSize - 1, Seting.ORIGINAL_BLOCKCHAIN_FILE);
//            resolve_conflicts();
            EntityBlock tempBlock = blockService.findBySpecialIndex(blockcheinSize-1);
            prevBlock = UtilsBlockToEntityBlock.entityBlockToBlock(tempBlock);
            isSaveFile = true;
            return new ResponseEntity<>("FALSE", HttpStatus.EXPECTATION_FAILED);
        } finally {
            prevBlock = Blockchain.indexFromFileBing(blockcheinSize - 1, Seting.ORIGINAL_BLOCKCHAIN_FILE);
//            resolve_conflicts();
//            EntityBlock tempBlock = BlockService.findBySpecialIndex(blockcheinSize-1);
//                    prevBlock = UtilsBlockToEntityBlock.entityBlockToBlock(tempBlock);
            isSaveFile = true;
            System.out.println("finish resolve_from_to_block");
        }
    }


    @RequestMapping(method = RequestMethod.POST, value = "/nodes/register", consumes = MediaType.APPLICATION_JSON_VALUE)
    public synchronized void register_node(@RequestBody AddressUrl urlAddrress) throws IOException, NoSuchAlgorithmException, InvalidKeySpecException, SignatureException, NoSuchProviderException, InvalidKeyException {


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
        BasisController.setNodes(nodes);

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

    }


    @GetMapping("/findAddresses")
    public void findAddresses() throws IOException, NoSuchAlgorithmException, InvalidKeySpecException, SignatureException, NoSuchProviderException, InvalidKeyException {
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

    public static void sendAddress() throws IOException, NoSuchAlgorithmException, InvalidKeySpecException, SignatureException, NoSuchProviderException, InvalidKeyException {
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

    @GetMapping("/difficultyBlockchain")
    public InfoDificultyBlockchain dificultyBlockchain() {
        InfoDificultyBlockchain dificultyBlockchain = new InfoDificultyBlockchain();
        dificultyBlockchain.setDifficultyAllBlockchain(shortDataBlockchain.getHashCount());
        dificultyBlockchain.setDiffultyOneBlock(dificultyOneBlock);
        return dificultyBlockchain;
    }


    @GetMapping("/senderTransactions")
    @ResponseBody
    public List<DtoTransaction> senderTransactions(
            @RequestParam String address,
            @RequestParam int from,
            @RequestParam int to
    ) throws IOException {
        return blockService.findBySender(address, from, to);
    }


    @GetMapping("/customerTransactions")
    @ResponseBody
    public List<DtoTransaction> customerTransactions(
            @RequestParam String address,
            @RequestParam int from,
            @RequestParam int to
    ) throws IOException {
        return blockService.findByCustomer(address, from, to);
    }

    @GetMapping("/senderCountDto")
    @ResponseBody
    public long countSenderTransaction(
            @RequestParam String address
    ){
        return blockService.countSenderTransaction(address);
    }

    @GetMapping("/customerCountDto")
    @ResponseBody
    public long countCustomerTransaction(
            @RequestParam String address
    ){
        return blockService.countCustomerTransaction(address);
    }
}


