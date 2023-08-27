package International_Trade_Union.controllers;

import International_Trade_Union.entity.*;
import International_Trade_Union.entity.blockchain.DataShortBlockchainInformation;
import International_Trade_Union.model.Account;
import International_Trade_Union.vote.LawEligibleForParliamentaryApproval;
import International_Trade_Union.vote.Laws;
import International_Trade_Union.vote.UtilsLaws;
import org.json.JSONException;

import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import International_Trade_Union.entity.blockchain.Blockchain;
import International_Trade_Union.entity.blockchain.block.Block;
import International_Trade_Union.config.BLockchainFactory;
import International_Trade_Union.config.BlockchainFactoryEnum;
import International_Trade_Union.model.Mining;
import International_Trade_Union.setings.Seting;
import International_Trade_Union.utils.*;
import org.springframework.http.ResponseEntity;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import java.security.*;
import java.security.spec.InvalidKeySpecException;

import java.sql.Timestamp;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

import static International_Trade_Union.utils.UtilsBalance.calculateBalance;

@RestController
public class BasisController {
    private static Map<String, Account> balances = new HashMap<>();
    private static long dificultyOneBlock;
    private static boolean isSaveFile = true;
    private static Block prevBlock = null;
    private static DataShortBlockchainInformation shortDataBlockchain = null;
    private static int blockcheinSize = 0;
    private static boolean blockchainValid = false;
//    private static Blockchain blockchain;
    private static Set<String> excludedAddresses = new HashSet<>();
    private static boolean isSave = true;

    public static long getDificultyOneBlock() {
        return dificultyOneBlock;
    }

    public static DataShortBlockchainInformation getShortDataBlockchain() {
        return shortDataBlockchain;
    }

    public static int getBlockcheinSize() {
        return blockcheinSize;
    }

    public static HttpServletRequest getCurrentRequest() {
        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
        if(requestAttributes == null)
            return null;
        Assert.state(requestAttributes != null, "Could not find current request via RequestContextHolder");
        Assert.isInstanceOf(ServletRequestAttributes.class, requestAttributes);
        HttpServletRequest servletRequest = ((ServletRequestAttributes) requestAttributes).getRequest();
        Assert.state(servletRequest != null, "Could not find current HttpServletRequest");
        return servletRequest;
    }

    public static Set<String> getExcludedAddresses() {
        HttpServletRequest request = getCurrentRequest();
        if(request == null)
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

    /**Возвращает список хостов*/
    public static Set<String> getNodes() throws IOException, NoSuchAlgorithmException, InvalidKeySpecException, SignatureException, NoSuchProviderException, InvalidKeyException {

        nodes = new HashSet<>();

//        Set<String> temporary = UtilsAllAddresses.readLineObject(Seting.ORIGINAL_POOL_URL_ADDRESS_FILE);


//        nodes.addAll(temporary);


//        nodes = nodes.stream()
//                .filter(t -> !t.isBlank())
//                .filter(t -> t.startsWith("\""))
//                .collect(Collectors.toSet());
//        nodes = nodes.stream().map(t -> t.replaceAll("\"", "")).collect(Collectors.toSet());
//        Set<String> bloked = UtilsAllAddresses.readLineObject(Seting.ORIGINAL_POOL_URL_ADDRESS_BLOCKED_FILE);
//        nodes.removeAll(bloked);
        nodes.removeAll(Seting.ORIGINAL_BLOCKED_ADDRESS);
        nodes.addAll(Seting.ORIGINAL_ADDRESSES);
        return nodes;
    }

    @GetMapping("/getNodes")
    public Set<String> getAllNodes() throws IOException, NoSuchAlgorithmException, InvalidKeySpecException, SignatureException, NoSuchProviderException, InvalidKeyException {
        Set<String> temporary = UtilsAllAddresses.readLineObject(Seting.ORIGINAL_POOL_URL_ADDRESS_FILE);
        nodes.addAll(temporary);
        nodes.addAll(Seting.ORIGINAL_ADDRESSES);
        nodes = nodes.stream().filter(t -> t.startsWith("\""))
                .collect(Collectors.toSet());
        Set<String> bloked = UtilsAllAddresses.readLineObject(Seting.ORIGINAL_POOL_URL_ADDRESS_BLOCKED_FILE);
        nodes.removeAll(bloked);
        nodes.removeAll(Seting.ORIGINAL_BLOCKED_ADDRESS);
        return nodes;
    }


    static {
        try {
            UtilsCreatedDirectory.createPackages();
//            blockchain = BLockchainFactory.getBlockchain(BlockchainFactoryEnum.ORIGINAL);
//            blockchain = Mining.getBlockchain(
//                    Seting.ORIGINAL_BLOCKCHAIN_FILE,
//                    BlockchainFactoryEnum.ORIGINAL);
            shortDataBlockchain = Blockchain.checkFromFile(Seting.ORIGINAL_BLOCKCHAIN_FILE);
            blockcheinSize = (int) shortDataBlockchain.getSize();
            blockchainValid = shortDataBlockchain.isValidation();
            prevBlock = Blockchain.indexFromFile(blockcheinSize-1, Seting.ORIGINAL_BLOCKCHAIN_FILE);
            if(blockcheinSize > 600){
                dificultyOneBlock = UtilsBlock.difficulty(Blockchain.subFromFile(
                        blockcheinSize-600, blockcheinSize, Seting.ORIGINAL_BLOCKCHAIN_FILE),
                        Seting.BLOCK_GENERATION_INTERVAL,
                        Seting.DIFFICULTY_ADJUSTMENT_INTERVAL);
            }
            else {
                dificultyOneBlock = UtilsBlock.difficulty(Blockchain.subFromFile(
                                blockcheinSize-600, blockcheinSize, Seting.ORIGINAL_BLOCKCHAIN_FILE),
                        Seting.BLOCK_GENERATION_INTERVAL,
                        Seting.DIFFICULTY_ADJUSTMENT_INTERVAL);
            }

            balances = SaveBalances.readLineObject(Seting.ORIGINAL_BALANCE_FILE);
            if(balances.isEmpty()){
                Blockchain.saveBalanceFromfile(Seting.ORIGINAL_BLOCKCHAIN_FILE);
                balances = SaveBalances.readLineObject(Seting.ORIGINAL_BALANCE_FILE);
            }
            else balances = SaveBalances.readLineObject(Seting.ORIGINAL_BALANCE_FILE);

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


    /**возвращяет размер локального блокчейна*/
    @GetMapping("/size")
    @ResponseBody
    public  Integer sizeBlockchain() throws IOException, NoSuchAlgorithmException, InvalidKeySpecException, SignatureException, NoSuchProviderException, InvalidKeyException, InterruptedException {
//        System.out.println("start /size");
        if(blockcheinSize == 0){
            System.out.println("blockchain is 0 blockchainSize " + blockcheinSize);
//            blockchain = Mining.getBlockchain(
//                    Seting.ORIGINAL_BLOCKCHAIN_FILE,
//                    BlockchainFactoryEnum.ORIGINAL);
            shortDataBlockchain = Blockchain.checkFromFile(Seting.ORIGINAL_BLOCKCHAIN_FILE);
            blockcheinSize = (int) shortDataBlockchain.getSize();
            blockchainValid = shortDataBlockchain.isValidation();
            prevBlock = Blockchain.indexFromFile(blockcheinSize-1, Seting.ORIGINAL_BLOCKCHAIN_FILE);

        }

        if(blockchainValid == false){
            System.out.println("/size blockchain not valid: " + blockchainValid);
            UtilsBlock.deleteFiles();
            return 1;
        }
//        System.out.println("finish /size");
        return blockcheinSize; //blockchain.sizeBlockhain();
    }

    /**Возвращает список блоков ОТ до ДО,*/
    @PostMapping("/sub-blocks")
    @ResponseBody
    public  List<Block> subBlocks(@RequestBody SubBlockchainEntity entity) throws IOException, NoSuchAlgorithmException, InvalidKeySpecException, SignatureException, NoSuchProviderException, InvalidKeyException {

        if(blockchainValid == false || blockcheinSize == 0){
//            blockchain = Mining.getBlockchain(
//                    Seting.ORIGINAL_BLOCKCHAIN_FILE,
//                    BlockchainFactoryEnum.ORIGINAL);
            shortDataBlockchain = Blockchain.checkFromFile(Seting.ORIGINAL_BLOCKCHAIN_FILE);
            blockcheinSize = (int) shortDataBlockchain.getSize();
            blockchainValid = shortDataBlockchain.isValidation();
        }

//        return blockchain.getBlockchainList().subList(entity.getStart(), entity.getFinish());
        return Blockchain.subFromFile(entity.getStart(), entity.getFinish(), Seting.ORIGINAL_BLOCKCHAIN_FILE);
    }
    /**Возвращяет блок по индексу*/

    @GetMapping("/version")
    @ResponseBody
    public double version(){
        return Seting.VERSION;
    }
    @PostMapping("/block")
    @ResponseBody
    public Block getBlock(@RequestBody Integer index) throws IOException, NoSuchAlgorithmException, InvalidKeySpecException, SignatureException, NoSuchProviderException, InvalidKeyException {
//        System.out.println("start getBlock");
        if(blockchainValid == false || blockcheinSize == 0){
//            blockchain = Mining.getBlockchain(
//                    Seting.ORIGINAL_BLOCKCHAIN_FILE,
//                    BlockchainFactoryEnum.ORIGINAL);
            shortDataBlockchain = Blockchain.checkFromFile(Seting.ORIGINAL_BLOCKCHAIN_FILE);
            blockcheinSize = (int) shortDataBlockchain.getSize();
            blockchainValid = shortDataBlockchain.isValidation();
        }
//        System.out.println("finish getBlock");
        return Blockchain.indexFromFile(index, Seting.ORIGINAL_BLOCKCHAIN_FILE);
    }
//    @GetMapping("/nodes/resolve")
//    public synchronized int resolve_conflicts() throws NoSuchAlgorithmException, InvalidKeySpecException, IOException, SignatureException, NoSuchProviderException, InvalidKeyException, JSONException {
//
//        boolean isPortion = false;
//        boolean isBigPortion = false;
//        try {
//            System.out.println(" :start resolve");
//            Blockchain temporaryBlockchain = BLockchainFactory.getBlockchain(BlockchainFactoryEnum.ORIGINAL);
//            Blockchain bigBlockchain = BLockchainFactory.getBlockchain(BlockchainFactoryEnum.ORIGINAL);
//            if (blockchainValid == false || blockcheinSize == 0) {
//                blockchain = Mining.getBlockchain(
//                        Seting.ORIGINAL_BLOCKCHAIN_FILE,
//                        BlockchainFactoryEnum.ORIGINAL);
//                shortDataBlockchain = Blockchain.checkFromFile(Seting.ORIGINAL_BLOCKCHAIN_FILE);
//                blockcheinSize = (int) shortDataBlockchain.getSize();
//                blockchainValid = shortDataBlockchain.isValidation();
//            }
//
//            //size of the most recent long blockchain downloaded from hosts (storage)
//            //размер самого актуального длинного блокчейна, скачанного из хостов (хранилище)
//            int bigSize = 0;
//
//            //local blockchain size
//            //размер локального блокчейна
//            int blocks_current_size = blockcheinSize;
//
//            //the sum of the complexity (all zeros) of the temporary blockchain, needed to select the most complex blockchain
//            //сумма сложности (всех нулей) временного блокчейна, нужна чтобы отобрать самый сложный блокчейн
//            long hashCountZeroTemporary = 0;
//
//            //the sum of the complexity (all zeros) of the longest downloaded blockchain is needed to select the most complex blockchain
//            //сумма сложности (всех нулей) самого длинного блокчейна из скачанных, нужна чтобы отобрать самый сложный блокчейн
//            long hashCountZeroBigBlockchain = 0;
//
//            EntityChain entityChain = null;
//            System.out.println(" :resolve_conflicts: blocks_current_size: " + blocks_current_size);
//
//            //the sum of the complexity (all zeros) of the local blockchain
//            //сумма сложности (всех нулей) локального блокчейна
//            long hashCountZeroAll = 0;
//
//            //get the total complexity of the local blockchain
//            //получить общую сложность локального блокчейна
//            hashCountZeroAll = shortDataBlockchain.getHashCount();
//
//            Set<String> nodesAll = getNodes();
//
//            System.out.println(":BasisController: resolve_conflicts: size nodes: " + getNodes().size());
//
//            //goes through all hosts (repositories) in search of the most up-to-date blockchain
//            //проходит по всем хостам(хранилищам) в поисках самого актуального блокчейна
//            for (String s : nodesAll) {
//                System.out.println(":while resolve_conflicts: node address: " + s);
//                String temporaryjson = null;
//
//                //if the local address matches the host address, it skips
//                //если локальный адрес совпадает с адресом хоста, он пропускает
//                if (BasisController.getExcludedAddresses().contains(s)) {
//                    System.out.println(":its your address or excluded address: " + s);
//                    continue;
//                }
//                try {
//                    //if the address is localhost, it skips
//                    //если адрес локального хоста, он пропускает
//                    if (s.contains("localhost") || s.contains("127.0.0.1"))
//                        continue;
//
//
//
//                    System.out.println("start:BasisController:resolve conflicts: address: " + s + "/size");
//
//                    String sizeStr = UtilUrl.readJsonFromUrl(s + "/size");
//                    Integer size = Integer.valueOf(sizeStr);
////                    MainController.setGlobalSize(size);
//                    System.out.println(" :resolve_conflicts: finish /size: " + size);
//                    //if the size from the storage is larger than on the local server, start checking
//                    //если размер с хранилища больше чем на локальном сервере, начать проверку
//                    if (size > blocks_current_size) {
//
//                        System.out.println(":size from address: " + s + " upper than: " + size + ":blocks_current_size " + blocks_current_size);
//                        //Test start algorithm
//                        List<Block> emptyList = new ArrayList<>();
//                        SubBlockchainEntity subBlockchainEntity = null;
//                        String subBlockchainJson = null;
//
//                        //if the local one lags behind the global one by more than PORTION_DOWNLOAD, then you need to download in portions from the storage
//                        //если локальный отстает от глобального больше чем PORTION_DOWNLOAD, то нужно скачивать порциями из хранилища
//                        if (size - blocks_current_size > Seting.PORTION_DOWNLOAD) {
//                            boolean downloadPortion = true;
//                            int finish = blocks_current_size + Seting.PORTION_DOWNLOAD;
//                            int start = blocks_current_size;
//                            //while the difference in the size of the local blockchain is greater than from the host, it will continue to download in portions to download the entire blockchain
//                            //пока разница размера локального блокчейна больше чем с хоста будет продожаться скачивать порциями, чтобы скачать весь блокчейн
//                            while (downloadPortion) {
//
//                                subBlockchainEntity = new SubBlockchainEntity(start, finish);
//
//                                System.out.println("downloadPortion: " + subBlockchainEntity.getStart() +
//                                        ": " + subBlockchainEntity.getFinish());
//                                subBlockchainJson = UtilsJson.objToStringJson(subBlockchainEntity);
//
//                                List<Block> subBlocks = UtilsJson.jsonToListBLock(UtilUrl.getObject(subBlockchainJson, s + "/sub-blocks"));
//                                finish = (int) subBlocks.get(subBlocks.size() - 1).getIndex() + Seting.PORTION_DOWNLOAD;
//                                start = (int) subBlocks.get(subBlocks.size() - 1).getIndex() + 1;
//
//                                emptyList.addAll(subBlocks);
//                                System.out.println("subblocks: " + subBlocks.get(0).getIndex() + ":"
//                                        + subBlocks.get(subBlocks.size() - 1).getIndex());
//
//                                if (size - emptyList.get(emptyList.size() - 1).getIndex() < Seting.PORTION_DOWNLOAD) {
//                                    downloadPortion = false;
//                                    finish = size;
//                                    subBlockchainEntity = new SubBlockchainEntity(start, finish);
//                                    subBlockchainJson = UtilsJson.objToStringJson(subBlockchainEntity);
//                                    subBlocks = UtilsJson.jsonToListBLock(UtilUrl.getObject(subBlockchainJson, s + "/sub-blocks"));
//                                    System.out.println("subblocks: " + subBlocks.get(0).getIndex() + ":"
//                                            + subBlocks.get(subBlocks.size() - 1).getIndex());
//                                    emptyList.addAll(subBlocks);
//                                }
//                            }
//                        } else {
//                            //If the difference is not greater than PORTION_DOWNLOAD, then downloads once a portion of this difference
//                            //Если разница не больше PORTION_DOWNLOAD, то скачивает один раз порцию эту разницу
//                            subBlockchainEntity = new SubBlockchainEntity(blocks_current_size, size);
//                            subBlockchainJson = UtilsJson.objToStringJson(subBlockchainEntity);
//
//                            System.out.println(":download sub block: " + subBlockchainJson);
//
//                            List<Block> subBlocks = UtilsJson.jsonToListBLock(UtilUrl.getObject(subBlockchainJson, s + "/sub-blocks"));
//                            emptyList.addAll(subBlocks);
//
//                            System.out.println("subblocks: " + subBlocks.get(0).getIndex() + ":"
//                                    + subBlocks.get(subBlocks.size() - 1).getIndex());
//                            System.out.println("blocks_current_size: " + blocks_current_size);
//                            System.out.println("sub: " + subBlocks.get(0).getIndex() + ":" + subBlocks.get(0).getHashBlock() + ":"
//                                    + "prevHash: " + subBlocks.get(0).getPreviousHash());
//                        }
//
//                        //if the local blockchain was originally greater than 0, then add part of the missing list of blocks to the list.
//                        //если локальный блокчейн изначально был больше 0, то добавить в список часть недостающего списка блоков.
//                        if (blocks_current_size > 0) {
//                            System.out.println("sub: from 0 " + ":" + blocks_current_size);
//                            List<Block> temp = blockchain.subBlock(0, blocks_current_size);
//
//                            emptyList.addAll(temp);
//                        }
//
//
//                        emptyList = emptyList.stream().sorted(Comparator.comparing(Block::getIndex)).collect(Collectors.toList());
//                        temporaryBlockchain.setBlockchainList(emptyList);
//
//
//                        System.out.println("size temporaryBlockchain: " + temporaryBlockchain.sizeBlockhain());
//                        System.out.println("resolve: temporaryBlockchain: " + temporaryBlockchain.validatedBlockchain());
//
//                        //if the global blockchain is larger but there is a branching in the blockchain, for example, the global size is 25,
//                        // the local size is 20,
//                        //but from block 15 they differ, then you need to remove all blocks from the local block from block 15
//                        // and add 15-25 blocks from the global blockchain there
//                        //если глобальный блокчейн больше но есть развлетление в блокчейне, к примеру глобальный размер 25,
//                        // локальный 20,
//                        //но с 15 блока они отличаются, то нужно удалить из локального с
//                        // 15 все блоки и добавить туда 15-25 с глобального блокчейна
//
//                        if(temporaryBlockchain.validatedBlockchain() && blockcheinSize > 1){
//                            isPortion = true;
//                        }else {
//                            isPortion = false;
//                        }
//                        if (!temporaryBlockchain.validatedBlockchain()) {
//                            System.out.println(":download blocks");
//                            emptyList = new ArrayList<>();
//
//                            for (int i = size - 1; i > 0; i--) {
//
//                                Block block = UtilsJson.jsonToBLock(UtilUrl.getObject(UtilsJson.objToStringJson(i), s + "/block"));
//
//                                System.out.println("block index: " + block.getIndex());
//                                if (i > blocks_current_size - 1) {
//                                    System.out.println(":download blocks: " + block.getIndex() +
//                                            " your block : " + (blocks_current_size) + ":wating need downoad blocks: " + (block.getIndex() - blocks_current_size));
//                                    emptyList.add(block);
//                                } else if (!blockchain.getBlock(i).getHashBlock().equals(block.getHashBlock())) {
//                                    emptyList.add(block);
//                                    System.out.println("********************************");
//                                    System.out.println(":dowdnload block index: " + i);
//                                    System.out.println(":block original index: " + blockchain.getBlock(i).getIndex());
//                                    System.out.println(":block from index: " + block.getIndex());
//                                    System.out.println("---------------------------------");
//                                } else {
//                                    emptyList.add(block);
//
//                                    if (i != 0) {
//                                        System.out.println("portion:sub: " + 0 + " : " + i + " block index: " + block.getIndex());
//                                        emptyList.addAll(blockchain.subBlock(0, i));
//                                    }
//
//                                    emptyList = emptyList.stream().sorted(Comparator.comparing(Block::getIndex)).collect(Collectors.toList());
//                                    temporaryBlockchain.setBlockchainList(emptyList);
//                                    System.out.println("<><><<><><><>><><><><><><><<>><><><><>");
//                                    System.out.println(":resolve_conflicts: temporaryBlockchain: " + temporaryBlockchain.validatedBlockchain());
//                                    System.out.println(":dowdnload block index: " + i);
//                                    System.out.println(":block original index: " + blockchain.getBlock(i).getIndex());
//                                    System.out.println(":block from index: " + block.getIndex());
//                                    System.out.println("<><><<><><><>><><><><><><><<>><><><><>");
//                                    break;
//                                }
//                            }
//                        }
//                    } else {
//                        System.out.println(":BasisController: resove: size less: " + size + " address: " + s);
//                        continue;
//                    }
//                } catch (IOException e) {
//
//                    System.out.println(":BasisController: resolve_conflicts: connect refused Error: " + s);
//                    continue;
//                } catch (JSONException e) {
//                    throw new RuntimeException(e);
//                } catch (CloneNotSupportedException e) {
//                    throw new RuntimeException(e);
//                }
//
//                //if the global blockchain is correct and it is larger than the longest previous temporary blockchain, then make it a contender as a future local blockchain
//                //если глобальный блокчейн верный и он больше самого длиного предыдущего временного блокчейна, то сделать его претендентом в качестве будущего локального блокчейна
//                if (temporaryBlockchain.validatedBlockchain()) {
//                    if (bigSize < temporaryBlockchain.sizeBlockhain()) {
//                        isBigPortion = isPortion;
//                        bigSize = temporaryBlockchain.sizeBlockhain();
//                    }
//                    for (Block block : temporaryBlockchain.getBlockchainList()) {
//                        hashCountZeroTemporary += UtilsUse.hashCount(block.getHashBlock());
//                    }
//
//                    if (blocks_current_size < temporaryBlockchain.sizeBlockhain() && hashCountZeroAll < hashCountZeroTemporary) {
//                        blocks_current_size = temporaryBlockchain.sizeBlockhain();
//                        bigBlockchain = temporaryBlockchain;
//                        hashCountZeroBigBlockchain = hashCountZeroTemporary;
//                    }
//                    hashCountZeroTemporary = 0;
//                }
//
//            }
//
//            System.out.println("bigBlockchain: " + bigBlockchain.validatedBlockchain() + " : " + bigBlockchain.sizeBlockhain());
//            //Only the blockchain that is not only the longest but also the most complex will be accepted.
//            //Будет принять только тот блокчейн который является не только самым длинным, но и самым сложным.
//            if (bigBlockchain.validatedBlockchain() && bigBlockchain.sizeBlockhain() > blockcheinSize && hashCountZeroBigBlockchain > hashCountZeroAll) {
//                System.out.println("resolve start addBlock start: ");
//                blockchain = bigBlockchain;
//                if(isBigPortion){
//                    List<Block> temp = bigBlockchain.subBlock(blockcheinSize, bigBlockchain.sizeBlockhain());
//                    Map<String, Account> balances = SaveBalances.readLineObject(Seting.ORIGINAL_BALANCE_FILE);
//                    addBlock2(temp,
//                            balances );
//                    System.out.println("temp size: " + temp.size());
//
//                }else {
//
//
//                    UtilsBlock.deleteFiles();
//                    addBlock(bigBlockchain.getBlockchainList());
//                }
//                List<Block> temp = bigBlockchain.subBlock(blockcheinSize, bigBlockchain.sizeBlockhain());
//
//                System.out.println("size: " + blockcheinSize);
//                System.out.println(":BasisController: resolve: bigblockchain size: " + bigBlockchain.sizeBlockhain());
//                System.out.println(":BasisController: resolve: validation bigblochain: " + bigBlockchain.validatedBlockchain());
//
//                System.out.println("isPortion: " + isPortion + ":isBigPortion: " +  isBigPortion + " size: " + temp.size());
//                if (blockcheinSize > bigSize) {
//                    return 1;
//                } else if (blockcheinSize < bigSize) {
//                    return -1;
//                } else {
//                    return 0;
//                }
//            }
//        } catch (CloneNotSupportedException e) {
//            throw new RuntimeException(e);
//        } finally {
//
//        }
//        return -4;
//    }
    public static void addBlock2(List<Block> originalBlocks, Map<String, Account> balances) throws IOException, NoSuchAlgorithmException, InvalidKeySpecException, SignatureException, NoSuchProviderException, InvalidKeyException {

        //delete all files from resources folder
        //удалить все файлы из папки resources

        System.out.println(" addBlock2 start: ");

        //write a new blockchain from scratch to the resources folder
        //записать с нуля новый блокчейн в папку resources
        for (Block block : originalBlocks) {
            System.out.println(" :BasisController: addBlock2: blockchain is being updated: ");
            UtilsBlock.saveBLock(block, Seting.ORIGINAL_BLOCKCHAIN_FILE);
        }

//        blockchain = Mining.getBlockchain(
//                Seting.ORIGINAL_BLOCKCHAIN_FILE,
//                BlockchainFactoryEnum.ORIGINAL);
//        shortDataBlockchain = Blockchain.checkFromFile(Seting.ORIGINAL_BLOCKCHAIN_FILE);
//        blockcheinSize = (int) shortDataBlockchain.getSize();
//        blockchainValid = shortDataBlockchain.isValidation();

        List<String> signs = new ArrayList<>();
        Map<String, Laws> allLaws = new HashMap<>();
        List<LawEligibleForParliamentaryApproval> allLawsWithBalance = new ArrayList<>();
        for (Block block :  originalBlocks) {
            calculateBalance(balances, block, signs);
            balances = UtilsBalance.calculateBalanceFromLaw(balances, block, allLaws, allLawsWithBalance);
        }

        Mining.deleteFiles(Seting.ORIGINAL_BALANCE_FILE);
        SaveBalances.saveBalances(balances, Seting.ORIGINAL_BALANCE_FILE);

        //removal of obsolete laws
        //удаление устаревших законов
//        Mining.deleteFiles(Seting.ORIGINAL_ALL_CORPORATION_LAWS_WITH_BALANCE_FILE);

        //rewriting all existing laws
        //перезапись всех действующих законов
        UtilsLaws.saveCurrentsLaws(allLawsWithBalance, Seting.ORIGINAL_ALL_CORPORATION_LAWS_WITH_BALANCE_FILE);


        System.out.println(":BasisController: addBlock2: finish: " + originalBlocks.size());
    }
    public static void addBlock(List<Block> orignalBlocks) throws IOException, NoSuchAlgorithmException, SignatureException, InvalidKeySpecException, NoSuchProviderException, InvalidKeyException {
        System.out.println("start addBLock");
        isSave = false;
        System.out.println("start  save in addBlock");
        List<String> signs = new ArrayList<>();
        Map<String, Laws> allLaws = new HashMap<>();
        List<LawEligibleForParliamentaryApproval> allLawsWithBalance = new ArrayList<>();
        for (Block block : orignalBlocks) {
            UtilsBlock.saveBLock(block, Seting.ORIGINAL_BLOCKCHAIN_FILE);
            calculateBalance(balances, block, signs);
            balances = UtilsBalance.calculateBalanceFromLaw(balances, block, allLaws, allLawsWithBalance);
        }
        System.out.println("finish save in addBlock");
        System.out.println("BasisController: addBlock: finish");




        Mining.deleteFiles(Seting.ORIGINAL_BALANCE_FILE);
        SaveBalances.saveBalances(balances, Seting.ORIGINAL_BALANCE_FILE);

        //removal of obsolete laws
        //удаление устаревших законов
//        Mining.deleteFiles(Seting.ORIGINAL_ALL_CORPORATION_LAWS_WITH_BALANCE_FILE);

        //rewriting all existing laws
        //перезапись всех действующих законов
        UtilsLaws.saveCurrentsLaws(allLawsWithBalance, Seting.ORIGINAL_ALL_CORPORATION_LAWS_WITH_BALANCE_FILE);


        isSave =true;
    }
//    @GetMapping("/addBlock")
//    public boolean getBLock() throws IOException, NoSuchAlgorithmException, InvalidKeySpecException, SignatureException, NoSuchProviderException, InvalidKeyException {
//        System.out.println("start /addblock");
//        if( blockcheinSize == 0){
//
//            blockcheinSize = blockchain.sizeBlockhain();
//            blockchainValid = blockchain.validatedBlockchain();
//        }
//
////        System.out.println("size /addblock blockchain size before: " + blockcheinSize);
//
//
//        UtilsBlock.deleteFiles();
////        System.out.println("files deleted");
////        System.out.println("size /addblock blockchain size after: " + blockcheinSize);
////        System.out.println("start addBlock save");
//        addBlock(blockchain.getBlockchainList());
//
//        System.out.println("finish addblock finish");
//        return true;
//    }
    @GetMapping("/balance")
    @ResponseBody
    public Account getBalance(@RequestParam String address) throws IOException, NoSuchAlgorithmException, SignatureException, InvalidKeySpecException, NoSuchProviderException, InvalidKeyException {
       if(balances.isEmpty()){
           Blockchain.saveBalanceFromfile(Seting.ORIGINAL_BLOCKCHAIN_FILE);
           balances = SaveBalances.readLineObject(Seting.ORIGINAL_BALANCE_FILE);
       }
        return balances.get(address);
    }

    @PostMapping("/nodes/resolve_from_to_block")
    public synchronized ResponseEntity<String> resolve_conflict(@RequestBody SendBlocksEndInfo sendBlocksEndInfo) throws JSONException, NoSuchAlgorithmException, InvalidKeySpecException, IOException, SignatureException, NoSuchProviderException, InvalidKeyException, CloneNotSupportedException {
        System.out.println("start resolve_from_to_block: " + sendBlocksEndInfo.getList().get(0).getMinerAddress());


        while (!isSaveFile){
//            System.out.println("saving file: resolve_from_to_block");
        }
        isSaveFile = false;



        if(sendBlocksEndInfo.getVersion() != Seting.VERSION){
            System.out.println("wrong version version " + Seting.VERSION + " but: " + sendBlocksEndInfo.getVersion());
            return new ResponseEntity<>("FALSE", HttpStatus.FAILED_DEPENDENCY);
        }
        List<Block> blocks = sendBlocksEndInfo.getList();
        System.out.println("miner address: " + blocks.get(blocks.size()-1).getMinerAddress());

        try {

            List<Block> addlist = Blockchain.clone(0, blocks.size(), blocks);
            System.out.println("account: " + addlist.get(0).getMinerAddress());
            Account account = balances.get(addlist.get(0).getMinerAddress());
            if(account == null){
                account = new Account(addlist.get(0).getMinerAddress(), 0, 0);
            }



            //четное и нечетное
            System.out.println("odd or not: " + addlist.get(0).getIndex() % 2);
            if(addlist.get(0).getIndex() % 2 == 0){
                if(account.getDigitalStockBalance() == 0 || account.getDigitalStockBalance() %2 != 0){
                    System.out.println("wrong balance: !=" );
                    return new ResponseEntity<>("FALSE", HttpStatus.LOCKED);
                }
            }

            Timestamp actualTime = Timestamp.from(Instant.now());
            Timestamp lastIndex = addlist.get(addlist.size()-1).getTimestamp();
            Long result = actualTime.toInstant().until(lastIndex.toInstant(), ChronoUnit.MINUTES);
            System.out.println("different time: " + result);
            if(
                    result > 1440 || result < -1440
               ){
                   System.out.println("_____________________________________________");
                   System.out.println("wrong timestamp");
                   System.out.println("new time 0 index: " + addlist.get(0).getTimestamp());
                   System.out.println("new time last index: " + addlist.get(addlist.size()-1).getTimestamp());
                   System.out.println("actual time: " + actualTime);
                System.out.println("result: " + result);
                System.out.println("miner: " + addlist.get(addlist.size()-1).getMinerAddress());

                   System.out.println("_____________________________________________");
                   return new ResponseEntity<>("FALSE", HttpStatus.EXPECTATION_FAILED);
               }

            if(prevBlock == null){
                prevBlock = Blockchain.indexFromFile(blockcheinSize-1, Seting.ORIGINAL_BLOCKCHAIN_FILE);
            }
            if(shortDataBlockchain.getSize() == 0
                    || !shortDataBlockchain.isValidation()
                    || shortDataBlockchain.getHashCount() == 0){
                shortDataBlockchain= Blockchain.checkFromFile(Seting.ORIGINAL_BLOCKCHAIN_FILE);
            }
            List<Block> lastDiff = Blockchain.subFromFile(
                    (int) (prevBlock.getIndex()-Seting.PORTION_BLOCK_TO_COMPLEXCITY),
                    (int) (prevBlock.getIndex()+1),
                    Seting.ORIGINAL_BLOCKCHAIN_FILE
            );

            //удаление транзакций
            if(prevBlock.getIndex() % 288 == 0)
                Mining.deleteFiles(Seting.ORGINAL_ALL_TRANSACTION_FILE);
            if(prevBlock.getIndex() % 288 == 0)
                Mining.deleteFiles(Seting.ORIGINAL_ALL_SENDED_TRANSACTION_FILE);

            System.out.println("+++++++++++++++++++++++++++++++++");
            int diff = UtilsBlock.difficulty(lastDiff, Seting.BLOCK_GENERATION_INTERVAL, Seting.DIFFICULTY_ADJUSTMENT_INTERVAL);
            System.out.println("actual difficult: " + blocks.get(0).getHashCompexity() + ":expected: "
                    + diff);

            System.out.println("+++++++++++++++++++++++++++++++++");
            DataShortBlockchainInformation temp =Blockchain.shortCheck(prevBlock, addlist, shortDataBlockchain, lastDiff);// Blockchain.checkEqualsFromToBlockFile(Seting.ORIGINAL_BLOCKCHAIN_FILE, addlist);

            System.out.println("<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<");
            System.out.println("original: " + shortDataBlockchain);
            System.out.println("temp: " + temp);


            System.out.println("address mininer: " + blocks.get(blocks.size()-1).getMinerAddress());
            System.out.println("<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<");
            System.out.println("addList size: " + addlist.size());
            if(blockcheinSize == 0 || blockchainValid == false){

                shortDataBlockchain = Blockchain.checkFromFile(Seting.ORIGINAL_BLOCKCHAIN_FILE);
                blockcheinSize = (int) shortDataBlockchain.getSize();
                blockchainValid = shortDataBlockchain.isValidation();
                prevBlock = Blockchain.indexFromFile(blockcheinSize-1, Seting.ORIGINAL_BLOCKCHAIN_FILE);

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
                if(temp.getSize() > shortDataBlockchain.getSize() && temp.getHashCount() > shortDataBlockchain.getHashCount()){
                    return new ResponseEntity<>("FALSE", HttpStatus.CONFLICT);
                }
                return new ResponseEntity<>("FALSE", HttpStatus.EXPECTATION_FAILED);
            }

            if (temp.getSize() > shortDataBlockchain.getSize()
                    && temp.getHashCount() > shortDataBlockchain.getHashCount()) {



                    System.out.println("*************************************");
                    System.out.println("before original: " + shortDataBlockchain);
                    System.out.println("before temp: " + temp);
                    addBlock2(addlist, balances);
                    balances = SaveBalances.readLineObject(Seting.ORIGINAL_BALANCE_FILE);
                    shortDataBlockchain = temp;
                    blockcheinSize = (int) shortDataBlockchain.getSize();
                    blockchainValid = shortDataBlockchain.isValidation();

                    dificultyOneBlock = diff;

                     System.out.println("after original: " + shortDataBlockchain);
                         System.out.println("after temp: " + temp);
                         prevBlock = Blockchain.indexFromFile(blockcheinSize-1, Seting.ORIGINAL_BLOCKCHAIN_FILE);

                    System.out.println("*************************************");

                    //задержка чтобы другие участники смогли скачать более актуальный блокчейн
                    Thread.sleep(20000);

                return new ResponseEntity<>("OK", HttpStatus.OK);
            }
            else {
                return new ResponseEntity<>("FALSE", HttpStatus.EXPECTATION_FAILED);
            }


        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } finally {
            prevBlock = Blockchain.indexFromFile(blockcheinSize-1, Seting.ORIGINAL_BLOCKCHAIN_FILE);
//            resolve_conflicts();
            isSaveFile = true;
            System.out.println("finish resolve_from_to_block");
        }

    }



    @RequestMapping(method = RequestMethod.POST, value = "/nodes/register", consumes = MediaType.APPLICATION_JSON_VALUE)
    public synchronized void register_node(@RequestBody AddressUrl urlAddrress) throws IOException, NoSuchAlgorithmException, InvalidKeySpecException, SignatureException, NoSuchProviderException, InvalidKeyException
    {


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

            if(BasisController.getExcludedAddresses().contains(url)){
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
    public InfoDificultyBlockchain dificultyBlockchain(){
        InfoDificultyBlockchain dificultyBlockchain = new InfoDificultyBlockchain();
       dificultyBlockchain.setDifficultyAllBlockchain(shortDataBlockchain.getHashCount());
       dificultyBlockchain.setDiffultyOneBlock(dificultyOneBlock);
        return dificultyBlockchain;
    }
}


