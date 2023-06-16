package International_Trade_Union.controllers;

import International_Trade_Union.entity.AddressUrl;
import International_Trade_Union.entity.SubBlockchainEntity;
import International_Trade_Union.entity.blockchain.DataShortBlockchainInformation;
import International_Trade_Union.network.AllTransactions;
import org.json.JSONException;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import International_Trade_Union.entity.blockchain.Blockchain;
import International_Trade_Union.entity.blockchain.block.Block;
import International_Trade_Union.config.BLockchainFactory;
import International_Trade_Union.config.BlockchainFactoryEnum;
import International_Trade_Union.entity.EntityChain;
import International_Trade_Union.model.Account;
import International_Trade_Union.model.Mining;
import International_Trade_Union.setings.Seting;
import International_Trade_Union.utils.*;
import International_Trade_Union.vote.*;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpServletRequest;
import java.io.FileNotFoundException;
import java.io.IOException;

import java.security.*;
import java.security.spec.InvalidKeySpecException;

import java.util.*;
import java.util.stream.Collectors;

@RestController
public class BasisController {
    private static int blockcheinSize = 0;
    private static boolean blockchainValid = false;
    private static Blockchain blockchain;
    private static Set<String> excludedAddresses = new HashSet<>();
    private static boolean isSave = true;
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

        Set<String> temporary = UtilsAllAddresses.readLineObject(Seting.ORIGINAL_POOL_URL_ADDRESS_FILE);


        nodes.addAll(temporary);


        nodes = nodes.stream()
                .filter(t -> !t.isBlank())
                .filter(t -> t.startsWith("\""))
                .collect(Collectors.toSet());
        nodes = nodes.stream().map(t -> t.replaceAll("\"", "")).collect(Collectors.toSet());
        nodes.addAll(Seting.ORIGINAL_ADDRESSES);
        return nodes;
    }


    /**Возвращяет действующий блокчейн*/
//    public static Blockchain getBlockchain() {
//        return blockchain;
//    }
//
//    public static synchronized void setBlockchain(Blockchain blockchain) {
//        BasisController.blockchain = blockchain;
//    }

    static {
        try {
            UtilsCreatedDirectory.createPackages();
            blockchain = BLockchainFactory.getBlockchain(BlockchainFactoryEnum.ORIGINAL);
            blockchain = Mining.getBlockchain(
                    Seting.ORIGINAL_BLOCKCHAIN_FILE,
                    BlockchainFactoryEnum.ORIGINAL);
            blockcheinSize = blockchain.sizeBlockhain();
            blockchainValid = blockchain.validatedBlockchain();

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

    /**Возвращает EntityChain который хранит в себе размер блокчейна и список блоков*/
    @GetMapping("/chain")
    @ResponseBody
    public  EntityChain full_chain() throws IOException, NoSuchAlgorithmException, InvalidKeySpecException, SignatureException, NoSuchProviderException, InvalidKeyException {
        System.out.println("start /chain");
       if(isSave == false)

        while (isSave == false){
//            System.out.println("cannot save, because updating data");
        }
        if(isSave == true)
            System.out.println("start /chain");
        if(blockchainValid == false || blockcheinSize == 0){
            System.out.println("update blockchain");
            blockchain = Mining.getBlockchain(
                Seting.ORIGINAL_BLOCKCHAIN_FILE,
                BlockchainFactoryEnum.ORIGINAL);
            blockcheinSize = blockchain.sizeBlockhain();
            blockchainValid = blockchain.validatedBlockchain();
        }
        System.out.println("finish /chain");
        return new EntityChain(blockcheinSize, blockchain.getBlockchainList());
    }

    /**возвращяет размер локального блокчейна*/
    @GetMapping("/size")
    @ResponseBody
    public  Integer sizeBlockchain() throws IOException, NoSuchAlgorithmException, InvalidKeySpecException, SignatureException, NoSuchProviderException, InvalidKeyException, InterruptedException {
        System.out.println("start /size");
        while (isSave ==false){
//            System.out.println("now saving new blockchain");
        }
        if(blockcheinSize == 0){
            System.out.println("blockchain is 0 blockchainSize " + blockcheinSize);
            blockchain = Mining.getBlockchain(
                    Seting.ORIGINAL_BLOCKCHAIN_FILE,
                    BlockchainFactoryEnum.ORIGINAL);
            blockcheinSize =blockchain.sizeBlockhain();
            blockchainValid = blockchain.validatedBlockchain();

        }

        if(blockchainValid == false){
            System.out.println("/size blockchain not valid: " + blockchainValid);
            UtilsBlock.deleteFiles();
            return 1;
        }
        System.out.println("finish /size");
        return blockcheinSize; //blockchain.sizeBlockhain();
    }

    /**Возвращает список блоков ОТ до ДО,*/
    @PostMapping("/sub-blocks")
    @ResponseBody
    public  List<Block> subBlocks(@RequestBody SubBlockchainEntity entity) throws IOException, NoSuchAlgorithmException, InvalidKeySpecException, SignatureException, NoSuchProviderException, InvalidKeyException {
        System.out.println("start subBlocks");
        if(blockchainValid == false || blockcheinSize == 0){
            blockchain = Mining.getBlockchain(
                    Seting.ORIGINAL_BLOCKCHAIN_FILE,
                    BlockchainFactoryEnum.ORIGINAL);
            blockcheinSize = blockchain.sizeBlockhain();
            blockchainValid = blockchain.validatedBlockchain();
        }
        System.out.println("finish subBlocks");
        return blockchain.getBlockchainList().subList(entity.getStart(), entity.getFinish());
    }
    /**Возвращяет блок по индексу*/
    @PostMapping("/block")
    @ResponseBody
    public Block getBlock(@RequestBody Integer index) throws IOException, NoSuchAlgorithmException, InvalidKeySpecException, SignatureException, NoSuchProviderException, InvalidKeyException {
        System.out.println("start getBlock");
        if(blockchainValid == false || blockcheinSize == 0){
            blockchain = Mining.getBlockchain(
                    Seting.ORIGINAL_BLOCKCHAIN_FILE,
                    BlockchainFactoryEnum.ORIGINAL);
            blockcheinSize = blockchain.sizeBlockhain();
            blockchainValid = blockchain.validatedBlockchain();
        }
        System.out.println("finish getBlock");
        return blockchain.getBlock(index);
    }
    @GetMapping("/nodes/resolve")
    public synchronized void resolve_conflicts() throws NoSuchAlgorithmException, InvalidKeySpecException, IOException, SignatureException, NoSuchProviderException, InvalidKeyException, JSONException {
        System.out.println("start resolve_conflicts");
        Blockchain temporaryBlockchain = BLockchainFactory.getBlockchain(BlockchainFactoryEnum.ORIGINAL);
        Blockchain bigBlockchain = BLockchainFactory.getBlockchain(BlockchainFactoryEnum.ORIGINAL);
        if(blockchainValid == false || blockcheinSize == 0){
            System.out.println("resolve: blockchain is null");
            blockchain = Mining.getBlockchain(
                    Seting.ORIGINAL_BLOCKCHAIN_FILE,
                    BlockchainFactoryEnum.ORIGINAL);
            blockcheinSize = blockchain.sizeBlockhain();
            blockchainValid = blockchain.validatedBlockchain();
        }

        int blocks_current_size = blockcheinSize;
        long hashCountZeroTemporary = 0;
        long hashCountZeroBigBlockchain = 0;
        EntityChain entityChain = null;

        long hashCountZeroAll = 0;
        //count hash start with zero all
        for (Block block : blockchain.getBlockchainList()) {
            hashCountZeroAll += UtilsUse.hashCount(block.getHashBlock());
        }

        Set<String> nodesAll = getNodes();
//        nodesAll.addAll(Seting.ORIGINAL_ADDRESSES_BLOCKCHAIN_STORAGE);
        System.out.println("BasisController: resolve: size: " + getNodes().size());
        for (String s : nodesAll) {
            System.out.println("BasisController: resove: address: " + s);
            String temporaryjson = null;

            if (BasisController.getExcludedAddresses().contains(s)) {
                System.out.println("its your address or excluded address: " + s);
                continue;
            }
            try {
                if(s.contains("localhost") || s.contains("127.0.0.1"))
                    continue;
                String address = s + "/chain";
                System.out.println("BasisController:resolve conflicts: address: " + s + "/size");
                String sizeStr = UtilUrl.readJsonFromUrl(s + "/size");
                Integer size = Integer.valueOf(sizeStr);
                if (size > blocks_current_size) {
                    System.out.println("size from address: " + s + " upper than: " + size + ":blocks_current_size " + blocks_current_size);
                    //Test start algorithm
                    SubBlockchainEntity subBlockchainEntity = new SubBlockchainEntity(blocks_current_size, size);
                    String subBlockchainJson = UtilsJson.objToStringJson(subBlockchainEntity);

                    List<Block> emptyList = new ArrayList<>();


                    List<Block> subBlocks = UtilsJson.jsonToListBLock(UtilUrl.getObject(subBlockchainJson, s + "/sub-blocks"));
                    emptyList.addAll(subBlocks);
                    emptyList.addAll(blockchain.getBlockchainList());

                    emptyList = emptyList.stream().sorted(Comparator.comparing(Block::getIndex)).collect(Collectors.toList());
                    temporaryBlockchain.setBlockchainList(emptyList);
                    if (!temporaryBlockchain.validatedBlockchain()) {
                        System.out.println("first algorithm not worked");
                        emptyList = new ArrayList<>();
                        emptyList.addAll(subBlocks);
                        for (int i = blockcheinSize - 1; i > 0; i--) {
                            Block block = UtilsJson.jsonToBLock(UtilUrl.getObject(UtilsJson.objToStringJson(i), s + "/block"));
                            if (!blockchain.getBlock(i).getHashBlock().equals(block.getHashBlock())) {
                                emptyList.add(block);
                            } else {
                                emptyList.add(block);
                                emptyList.addAll(blockchain.getBlockchainList().subList(0, i));
                                emptyList = emptyList.stream().sorted(Comparator.comparing(Block::getIndex)).collect(Collectors.toList());
                                temporaryBlockchain.setBlockchainList(emptyList);
                                break;
                            }
                        }
                    }
                    if (!temporaryBlockchain.validatedBlockchain()) {
                        System.out.println("second algorith not worked");
                        temporaryjson = UtilUrl.readJsonFromUrl(address);
                        entityChain = UtilsJson.jsonToEntityChain(temporaryjson);
                        temporaryBlockchain.setBlockchainList(
                                entityChain.getBlocks().stream().sorted(Comparator.comparing(Block::getIndex)).collect(Collectors.toList()));
                    }
                } else {
                    System.out.println("BasisController: resove: size less: " + size + " address: " + address);
                    continue;
                }
            } catch (IOException e) {
//                e.printStackTrace();
                System.out.println("BasisController: resolve_conflicts: Error: " + s);
                continue;
            }


            if (temporaryBlockchain.validatedBlockchain()) {
                for (Block block : temporaryBlockchain.getBlockchainList()) {
                    hashCountZeroTemporary += UtilsUse.hashCount(block.getHashBlock());
                }

                if (blocks_current_size < temporaryBlockchain.sizeBlockhain() && hashCountZeroAll < hashCountZeroTemporary) {
                    blocks_current_size = temporaryBlockchain.sizeBlockhain();
                    bigBlockchain = temporaryBlockchain;
                    hashCountZeroBigBlockchain = hashCountZeroTemporary;
                }
                hashCountZeroTemporary = 0;
            }

        }


        if (bigBlockchain.sizeBlockhain() > blockcheinSize && hashCountZeroBigBlockchain > hashCountZeroAll) {

            blockchain = bigBlockchain;
            UtilsBlock.deleteFiles();
            addBlock(bigBlockchain.getBlockchainList(), false);
            System.out.println("BasisController: resolve: bigblockchain size: " + bigBlockchain.sizeBlockhain());

        }
        System.out.println("finish resolve_conflicts");
    }
    public static void addBlock(List<Block> orignalBlocks, boolean isSave) throws IOException, NoSuchAlgorithmException, SignatureException, InvalidKeySpecException, NoSuchProviderException, InvalidKeyException {
        System.out.println("start addBLock");;
        isSave = false;
        Blockchain temporaryForValidation = BLockchainFactory.getBlockchain(BlockchainFactoryEnum.ORIGINAL);
        temporaryForValidation.setBlockchainList(orignalBlocks);
        UtilsBlock.deleteFiles();
        System.out.println("temporaryForValidation: size: " + temporaryForValidation.sizeBlockhain() +
                " valid: " + temporaryForValidation.validatedBlockchain());
        System.out.println("start  save in addBlock");
        for (Block block : orignalBlocks) {
            UtilsBlock.saveBLock(block, Seting.ORIGINAL_BLOCKCHAIN_FILE, isSave);
        }
        System.out.println("finish save in addBlock");
        System.out.println("saving block: " + orignalBlocks.size());
        System.out.println("blockchain size:  before: " + blockcheinSize);
        blockchain = Mining.getBlockchain(
                Seting.ORIGINAL_BLOCKCHAIN_FILE,
                BlockchainFactoryEnum.ORIGINAL);
        blockcheinSize = blockchain.sizeBlockhain();
        blockchainValid = blockchain.validatedBlockchain();
        System.out.println("blockchain size: after: " + blockcheinSize);

        System.out.println("finish add block");
        System.out.println("BasisController: addBlock: finish");



        isSave =true;
    }
    @GetMapping("/addBlock")
    public boolean getBLock() throws IOException, NoSuchAlgorithmException, InvalidKeySpecException, SignatureException, NoSuchProviderException, InvalidKeyException {
        System.out.println("start /addblock");
        if(blockchainValid == false || blockcheinSize == 0){
            blockchain = Mining.getBlockchain(
                    Seting.ORIGINAL_BLOCKCHAIN_FILE,
                    BlockchainFactoryEnum.ORIGINAL);
            blockcheinSize = blockchain.sizeBlockhain();
            blockchainValid = blockchain.validatedBlockchain();
        }

        System.out.println("size /addblock blockchain size before: " + blockcheinSize);

        UtilsBlock.deleteFiles();
        System.out.println("files deleted");
        System.out.println("size /addblock blockchain size after: " + blockcheinSize);
        System.out.println("start addBlock save");
        UtilsBlock.deleteFiles();
        addBlock(blockchain.getBlockchainList(), false);

        System.out.println("finish addblock finish");
        return true;
    }

//    @PostMapping("/nodes/resolve_portion_block")
//    public synchronized ResponseEntity<String> portionblock(@RequestBody List<Block> blocks) throws NoSuchAlgorithmException, InvalidKeySpecException, IOException, SignatureException, NoSuchProviderException, InvalidKeyException {
//        Blockchain temporary = BLockchainFactory.getBlockchain(BlockchainFactoryEnum.ORIGINAL);
//        if(blockchainValid == false || blockcheinSize == 0){
//            System.out.println("resolve: blockchain is null");
//            blockchain = Mining.getBlockchain(
//                    Seting.ORIGINAL_BLOCKCHAIN_FILE,
//                    BlockchainFactoryEnum.ORIGINAL);
//            blockcheinSize = blockchain.sizeBlockhain();
//            blockchainValid = blockchain.validatedBlockchain();
//        }
//        int index = 0;
//        boolean stopedFind = false;
//
//        blocks = blocks.stream().sorted(Comparator.comparing(Block::getIndex)).collect(Collectors.toList());
//        long hashCountZeroTemporary = 0;
//
//        long hashCountZeroAll = 0;
//
//        List<Block> blockList = new ArrayList<>();
//        blockList.addAll(blocks.subList(blockcheinSize, blocks.size()));
//        System.out.println("start while in resolve_portion_block");
//        for (int i = blockcheinSize - 1; i >= blocks.get(0).getIndex(); i--) {
//          String hashOriginal = blockchain.getBlock(i).getHashBlock();
//          String hashFrom = blocks.get(i).getHashBlock();
//          if(hashOriginal.equals(hashFrom)){
//              index = i;
//              System.out.println("portion good");
//              stopedFind = true;
//              blockList.add(blockList.get(i));
//              break;
//          }
//          blockList.add(blocks.get(i));
//        }
//
//
//        blockList.addAll(blockList.subList(0, index));
//        blockList = blockList.stream().sorted(Comparator.comparing(Block::getIndex))
//                .collect(Collectors.toList());
//
//        //count hash start with zero all
//        for (Block block : blockchain.getBlockchainList()) {
//            hashCountZeroAll += UtilsUse.hashCount(block.getHashBlock());
//        }
//
//        if (temporary.validatedBlockchain()) {
//            for (Block block : temporary.getBlockchainList()) {
//                hashCountZeroTemporary += UtilsUse.hashCount(block.getHashBlock());
//            }
//
//        }else {
//            System.out.println("portion blocks not worked");
//            return new ResponseEntity<>("FALSE", HttpStatus.EXPECTATION_FAILED);
//        }
//
//        temporary.setBlockchainList(blockList);
//
//            System.out.println("its true");
//            if(stopedFind){
//                if (temporary.sizeBlockhain() > blockcheinSize && hashCountZeroTemporary > hashCountZeroAll) {
//
//                    blockchain = temporary;
//                    UtilsBlock.deleteFiles();
//                    System.out.println("starting portions block: ");
//                    if(temporary.validatedBlockchain()){
//                        System.out.println("reslove from to block: ");
//                        UtilsBlock.deleteFiles();
//                        addBlock(temporary.getBlockchainList());
//                    }
//                    System.out.println("BasisController: resolve: bigblockchain size: " + temporary.sizeBlockhain());
//
//                return  new ResponseEntity<>("OK", HttpStatus.OK);
//                }
//            }else {
//                System.out.println("portion not work");
//                return new ResponseEntity<>("FALSE", HttpStatus.EXPECTATION_FAILED);
//            }
//        System.out.println("portion not ok");
//            return new ResponseEntity<>("FALSE", HttpStatus.EXPECTATION_FAILED);
//    }
    @PostMapping("/nodes/resolve_from_to_block")
    public synchronized ResponseEntity<String> resolve_conflict(@RequestBody List<Block> blocks) throws JSONException, NoSuchAlgorithmException, InvalidKeySpecException, IOException, SignatureException, NoSuchProviderException, InvalidKeyException, CloneNotSupportedException {
        resolve_conflicts();
        Blockchain temporaryBlockchain = BLockchainFactory.getBlockchain(BlockchainFactoryEnum.ORIGINAL);
        System.out.println("******************************************");
        DataShortBlockchainInformation originalFromFile =
                Blockchain.checkFromFile(Seting.ORIGINAL_BLOCKCHAIN_FILE);
        DataShortBlockchainInformation fromList =
                Blockchain.checkEqualsFromFile(Seting.ORIGINAL_BLOCKCHAIN_FILE, blocks);
        System.out.println("original originalFromFile: " + originalFromFile);
        System.out.println("original fromList: " + fromList);

        System.out.println("******************************************");
        if(blockchainValid == false || blockcheinSize == 0){
            System.out.println("resolve: blockchain is null");
            blockchain = Mining.getBlockchain(
                    Seting.ORIGINAL_BLOCKCHAIN_FILE,
                    BlockchainFactoryEnum.ORIGINAL);
            blockcheinSize = blockchain.sizeBlockhain();
            blockchainValid = blockchain.validatedBlockchain();

        }

        List<Block> tempBlocks = blockchain.getBlockchainList();
//        blocks.addAll(tempBlocks);
//        blocks = blocks.stream().sorted(Comparator.comparing(Block::getIndex)).collect(Collectors.toList());
//        temporaryBlockchain.setBlockchainList(blocks);
//        System.out.println("resolve_from_to_block: temporaryBlockchain " + temporaryBlockchain.sizeBlockhain() +
//                " valid " + temporaryBlockchain.validatedBlockchain());
//        System.out.println("resolve from to block: " + temporaryBlockchain.sizeBlockhain() + " valid: " +
//                temporaryBlockchain.validatedBlockchain());

        long hashCountZeroTemporary = 0;

        long hashCountZeroAll = 0;


        //count hash start with zero all
//        for (Block block : blockchain.getBlockchainList()) {
//            hashCountZeroAll += UtilsUse.hashCount(block.getHashBlock());
//        }

        if (fromList.isValidation()) {
            System.out.println("from block is true");
//            for (Block block : temporaryBlockchain.getBlockchainList()) {
//                hashCountZeroTemporary += UtilsUse.hashCount(block.getHashBlock());
//            }

        }else {
            System.out.println("from to block not worked");
            return new ResponseEntity<>("FALSE", HttpStatus.EXPECTATION_FAILED);
        }
//        System.out.println("temporaryBlockchain: size: "
//                + temporaryBlockchain.sizeBlockhain()
//                + " valid: " + temporaryBlockchain.validatedBlockchain());
//        System.out.println("blockchain: size: "
//                + blockcheinSize
//                + " valid: " + blockchainValid);
        if (fromList.getSize() > blockcheinSize && fromList.getHashCount() > originalFromFile.getHashCount()) {

            blockchain = temporaryBlockchain;
            UtilsBlock.deleteFiles();
            System.out.println("starting resolve from to block: ");
            if(fromList.isValidation()){
                System.out.println("reslove from to block: ");

//                UtilsBlock.deleteFiles();
                addBlock(blocks, true);
                return  new ResponseEntity<>("OK", HttpStatus.OK);
            }
            System.out.println("BasisController: resolve: bigblockchain size: " + temporaryBlockchain.sizeBlockhain());


        }
        return new ResponseEntity<>("FALSE", HttpStatus.EXPECTATION_FAILED);

    }
//    /**добавляет блоки в блок чейн пересохраняя файлы, предназначен когда у нас есть готовый
//     * блокчейн и нужно все файлы(balance, vote, government и т. д.) заного пересохранить.
//     * adds blocks to the block chain by resaving files, designed when we have it ready
//     *      * Blockchain and you need to save all files (balance, vote, government, etc.) again.*/

    @PostMapping("/nodes/resolve_all_blocks") //resolve_all_blocks
    public synchronized ResponseEntity<String>resolve_blocks_conflict(@RequestBody List<Block> blocks) throws IOException, NoSuchAlgorithmException, InvalidKeySpecException, SignatureException, NoSuchProviderException, InvalidKeyException, JSONException, CloneNotSupportedException {
        resolve_conflicts();
        Blockchain temporaryBlockchain = BLockchainFactory.getBlockchain(BlockchainFactoryEnum.ORIGINAL);
        blocks = blocks.stream().sorted(Comparator.comparing(Block::getIndex)).collect(Collectors.toList());
        temporaryBlockchain.setBlockchainList(blocks);
        System.out.println("size temporary blocks: " + blocks.size());

        if(blockchainValid == false || blockcheinSize == 0){
            System.out.println("resolve: blockchain is null");
            blockchain = Mining.getBlockchain(
                    Seting.ORIGINAL_BLOCKCHAIN_FILE,
                    BlockchainFactoryEnum.ORIGINAL);
            blockchainValid = blockchain.validatedBlockchain();
            blockcheinSize = blockchain.sizeBlockhain();
        }


        long hashCountZeroTemporary = 0;

        long hashCountZeroAll = 0;


        //count hash start with zero all
        for (Block block : blockchain.getBlockchainList()) {
            hashCountZeroAll += UtilsUse.hashCount(block.getHashBlock());
        }

        if (temporaryBlockchain.validatedBlockchain()) {
            System.out.println("******************************************");
            DataShortBlockchainInformation originalFromFile =
                    Blockchain.checkFromFile(Seting.ORIGINAL_BLOCKCHAIN_FILE);
            DataShortBlockchainInformation fromList =
                    Blockchain.checkEqualsFromFile(Seting.ORIGINAL_BLOCKCHAIN_FILE, blocks);
            System.out.println("original originalFromFile: " + originalFromFile);
            System.out.println("original fromList: " + fromList);

            System.out.println("******************************************");
            for (Block block : temporaryBlockchain.getBlockchainList()) {
                hashCountZeroTemporary += UtilsUse.hashCount(block.getHashBlock());
            }

        }else {
            return new ResponseEntity<>("FALSE", HttpStatus.EXPECTATION_FAILED);
        }
        System.out.println(" resolve_all_blocks: " + temporaryBlockchain.getBlock(temporaryBlockchain.sizeBlockhain() - 1));
        if (temporaryBlockchain.sizeBlockhain() > blockcheinSize && hashCountZeroTemporary > hashCountZeroAll) {
            System.out.println("temp");
            blockchain = temporaryBlockchain;
            UtilsBlock.deleteFiles();
            if(temporaryBlockchain.validatedBlockchain()){
                System.out.println("delete resolve all blocks");
                UtilsBlock.deleteFiles();
                addBlock(temporaryBlockchain.getBlockchainList(), false);
            }

            System.out.println("BasisController: resolve: bigblockchain size: " + temporaryBlockchain.sizeBlockhain());


        }

        return  new ResponseEntity<>("OK", HttpStatus.OK);

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


    @GetMapping("/getNodes")
    public Set<String> getAllNodes() throws IOException, NoSuchAlgorithmException, InvalidKeySpecException, SignatureException, NoSuchProviderException, InvalidKeyException {
        Set<String> temporary = UtilsAllAddresses.readLineObject(Seting.ORIGINAL_POOL_URL_ADDRESS_FILE);
        nodes.addAll(temporary);
        nodes.addAll(Seting.ORIGINAL_ADDRESSES);
        nodes = nodes.stream().filter(t -> t.startsWith("\""))
                .collect(Collectors.toSet());
        return nodes;
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
}


