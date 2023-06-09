package International_Trade_Union.controllers;

import International_Trade_Union.entity.*;
import International_Trade_Union.entity.blockchain.DataShortBlockchainInformation;
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

import java.util.*;
import java.util.stream.Collectors;

@RestController
public class BasisController {
    private static long dificultyOneBlock;
    private static boolean isSaveFile = true;
    private static Block prevBlock = null;
    private static DataShortBlockchainInformation shortDataBlockchain = null;
    private static int blockcheinSize = 0;
    private static boolean blockchainValid = false;
    private static Blockchain blockchain;
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
            blockchain = BLockchainFactory.getBlockchain(BlockchainFactoryEnum.ORIGINAL);
            blockchain = Mining.getBlockchain(
                    Seting.ORIGINAL_BLOCKCHAIN_FILE,
                    BlockchainFactoryEnum.ORIGINAL);
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
        System.out.println("start /size");
        if(blockcheinSize == 0){
            System.out.println("blockchain is 0 blockchainSize " + blockcheinSize);
            blockchain = Mining.getBlockchain(
                    Seting.ORIGINAL_BLOCKCHAIN_FILE,
                    BlockchainFactoryEnum.ORIGINAL);
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
            shortDataBlockchain = Blockchain.checkFromFile(Seting.ORIGINAL_BLOCKCHAIN_FILE);
            blockcheinSize = (int) shortDataBlockchain.getSize();
            blockchainValid = shortDataBlockchain.isValidation();
        }
        System.out.println("finish subBlocks");
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
            blockchain = Mining.getBlockchain(
                    Seting.ORIGINAL_BLOCKCHAIN_FILE,
                    BlockchainFactoryEnum.ORIGINAL);
            shortDataBlockchain = Blockchain.checkFromFile(Seting.ORIGINAL_BLOCKCHAIN_FILE);
            blockcheinSize = (int) shortDataBlockchain.getSize();
            blockchainValid = shortDataBlockchain.isValidation();
        }
//        System.out.println("finish getBlock");
        return Blockchain.indexFromFile(index, Seting.ORIGINAL_BLOCKCHAIN_FILE);
    }
    @GetMapping("/nodes/resolve")
    public synchronized int resolve_conflicts() throws NoSuchAlgorithmException, InvalidKeySpecException, IOException, SignatureException, NoSuchProviderException, InvalidKeyException, JSONException {
        System.out.println("start resolve");
        Blockchain temporaryBlockchain = BLockchainFactory.getBlockchain(BlockchainFactoryEnum.ORIGINAL);
        Blockchain bigBlockchain = BLockchainFactory.getBlockchain(BlockchainFactoryEnum.ORIGINAL);
        if(blockchainValid == false || blockcheinSize == 0){
            blockchain = Mining.getBlockchain(
                    Seting.ORIGINAL_BLOCKCHAIN_FILE,
                    BlockchainFactoryEnum.ORIGINAL);
            shortDataBlockchain = Blockchain.checkFromFile(Seting.ORIGINAL_BLOCKCHAIN_FILE);
            blockcheinSize = (int) shortDataBlockchain.getSize();
            blockchainValid = shortDataBlockchain.isValidation();
        }

        int bigSize = 0;
        int blocks_current_size = blockcheinSize;
        long hashCountZeroTemporary = 0;
        long hashCountZeroBigBlockchain = 0;
        EntityChain entityChain = null;
        System.out.println("resolve_conflicts: blocks_current_size: " + blocks_current_size);
        long hashCountZeroAll = 0;
        //count hash start with zero all

        hashCountZeroAll =  shortDataBlockchain.getHashCount();

        Set<String> nodesAll = getNodes();

        System.out.println("BasisController: resolve_conflicts: size nodes: " + getNodes().size());
        for (String s : nodesAll) {
            System.out.println("while resolve_conflicts: node address: " + s);
            String temporaryjson = null;

            if (BasisController.getExcludedAddresses().contains(s)) {
                System.out.println("its your address or excluded address: " + s);
                continue;
            }
            try {
                if(s.contains("localhost") || s.contains("127.0.0.1"))
                    continue;
                String address = s + "/chain";

                System.out.println("resolve_conflicts: start /size");
                System.out.println("BasisController:resolve conflicts: address: " + s + "/size");
                String sizeStr = UtilUrl.readJsonFromUrl(s + "/size");
                Integer size = Integer.valueOf(sizeStr);

                System.out.println("resolve_conflicts: finish /size: " + size);
                if (size > blocks_current_size) {

                    System.out.println("size from address: " + s + " upper than: " + size + ":blocks_current_size " + blocks_current_size);
                    //Test start algorithm
                    SubBlockchainEntity subBlockchainEntity = new SubBlockchainEntity(blocks_current_size, size);
                    String subBlockchainJson = UtilsJson.objToStringJson(subBlockchainEntity);

                    List<Block> emptyList = new ArrayList<>();

                    System.out.println("download sub block: " + subBlockchainJson);
                    List<Block> subBlocks = UtilsJson.jsonToListBLock(UtilUrl.getObject(subBlockchainJson, s + "/sub-blocks"));
                    emptyList.addAll(subBlocks);
                    if(blocks_current_size > 1)
                        emptyList.addAll(Blockchain.subFromFile(0, blockcheinSize, Seting.ORIGINAL_BLOCKCHAIN_FILE));

                    emptyList = emptyList.stream().sorted(Comparator.comparing(Block::getIndex)).collect(Collectors.toList());
                    temporaryBlockchain.setBlockchainList(emptyList);

                    if (!temporaryBlockchain.validatedBlockchain()) {
                        System.out.println("download blocks");
                        emptyList = new ArrayList<>();

                        for (int i = size - 1; i > 0; i--) {

                            Block block = UtilsJson.jsonToBLock(UtilUrl.getObject(UtilsJson.objToStringJson(i), s + "/block"));

                            if(i > blockcheinSize -1){
                                System.out.println("download blocks: " + block.getIndex()+
                                        " your block : " + (blockcheinSize ));
                                emptyList.add(block);
                            }
                            else if (
                                    !Blockchain.indexFromFile(i, Seting.ORIGINAL_BLOCKCHAIN_FILE).getHashBlock().equals(block.getHashBlock())) {
                                emptyList.add(block);
                                System.out.println("********************************");
                                System.out.println("dowdnload block index: " + i);
                                System.out.println("block original index: " + Blockchain.indexFromFile(i, Seting.ORIGINAL_BLOCKCHAIN_FILE).getIndex());
                                System.out.println("block from index: " + block.getIndex());
                                System.out.println("---------------------------------");
                            } else {
                                emptyList.add(block);
                                System.out.println("sub: " + 0 + " : " + i);
                                if(i != 0){
                                    emptyList.addAll(Blockchain.subFromFile(0, i, Seting.ORIGINAL_BLOCKCHAIN_FILE));
                                }

                                emptyList = emptyList.stream().sorted(Comparator.comparing(Block::getIndex)).collect(Collectors.toList());
                                temporaryBlockchain.setBlockchainList(emptyList);
                                break;
                            }
                        }
                    }
//                    if (!temporaryBlockchain.validatedBlockchain()) {
//                        System.out.println("download all blockchain");
//                        temporaryjson = UtilUrl.readJsonFromUrl(address);
//                        entityChain = UtilsJson.jsonToEntityChain(temporaryjson);
//                        temporaryBlockchain.setBlockchainList(
//                                entityChain.getBlocks().stream().sorted(Comparator.comparing(Block::getIndex)).collect(Collectors.toList()));
//                    }
                } else {
                    System.out.println("BasisController: resove: size less: " + size + " address: " + address);
                    continue;
                }
            } catch (IOException e) {

//                e.printStackTrace();
                System.out.println("BasisController: resolve_conflicts: connect refused Error: " + s);
                continue;
            }


            if (temporaryBlockchain.validatedBlockchain()) {
                if(bigSize < temporaryBlockchain.sizeBlockhain()){
                    bigSize = temporaryBlockchain.sizeBlockhain();
                }
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


        if (bigBlockchain.sizeBlockhain() > blockcheinSize && hashCountZeroBigBlockchain > hashCountZeroAll)
        {

            blockchain = bigBlockchain;
            UtilsBlock.deleteFiles();
            addBlock(bigBlockchain.getBlockchainList());
            System.out.println("BasisController: resolve: bigblockchain size: " + bigBlockchain.sizeBlockhain());

        }
        if(blockcheinSize > bigSize){
            return 1;
        }
        else if(blockcheinSize < bigSize){
            return -1;
        }
        else {
            return 0;
        }
    }

    public static void addBlock(List<Block> orignalBlocks) throws IOException, NoSuchAlgorithmException, SignatureException, InvalidKeySpecException, NoSuchProviderException, InvalidKeyException {
        System.out.println("start addBLock");
        isSave = false;
        System.out.println("start  save in addBlock");

        for (Block block : orignalBlocks) {
            UtilsBlock.saveBLock(block, Seting.ORIGINAL_BLOCKCHAIN_FILE);
        }
        System.out.println("finish save in addBlock");
        System.out.println("BasisController: addBlock: finish");



        isSave =true;
    }
    @GetMapping("/addBlock")
    public boolean getBLock() throws IOException, NoSuchAlgorithmException, InvalidKeySpecException, SignatureException, NoSuchProviderException, InvalidKeyException {
        System.out.println("start /addblock");
        if(blockchain == null || blockcheinSize == 0){
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
        addBlock(blockchain.getBlockchainList());

        System.out.println("finish addblock finish");
        return true;
    }


    @PostMapping("/nodes/resolve_from_to_block")
    public synchronized ResponseEntity<String> resolve_conflict(@RequestBody SendBlocksEndInfo sendBlocksEndInfo) throws JSONException, NoSuchAlgorithmException, InvalidKeySpecException, IOException, SignatureException, NoSuchProviderException, InvalidKeyException, CloneNotSupportedException {
        System.out.println("start resolve_from_to_block");
        while (!isSaveFile){
            System.out.println("saving file: resolve_from_to_block");
        }
        if(sendBlocksEndInfo.getVersion() != Seting.VERSION){
            System.out.println("wrong version version " + Seting.VERSION + " but: " + sendBlocksEndInfo.getVersion());
            return new ResponseEntity<>("FALSE", HttpStatus.FAILED_DEPENDENCY);
        }
        List<Block> blocks = sendBlocksEndInfo.getList();
        isSaveFile = false;
        try {

            List<Block> addlist = Blockchain.clone(0, blocks.size(), blocks);
            System.out.println("resolve_from_to_block");
            if(prevBlock == null){
                prevBlock = Blockchain.indexFromFile(blockcheinSize-1, Seting.ORIGINAL_BLOCKCHAIN_FILE);
            }
            if(shortDataBlockchain.getSize() == 0
                    || !shortDataBlockchain.isValidation()
                    || shortDataBlockchain.getHashCount() == 0){
                shortDataBlockchain= Blockchain.checkFromFile(Seting.ORIGINAL_BLOCKCHAIN_FILE);
            }

            DataShortBlockchainInformation temp =Blockchain.shortCheck(prevBlock, addlist, shortDataBlockchain);// Blockchain.checkEqualsFromToBlockFile(Seting.ORIGINAL_BLOCKCHAIN_FILE, addlist);

            System.out.println("<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<");
            System.out.println("original: " + shortDataBlockchain);
            System.out.println("temp: " + temp);


            System.out.println("address mininer: " + blocks.get(blocks.size()-1).getMinerAddress());
            System.out.println("<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<");
            System.out.println("addList size: " + addlist.size());
            if(blockcheinSize == 0 || blockchainValid == false){
                blockchain = Mining.getBlockchain(
                        Seting.ORIGINAL_BLOCKCHAIN_FILE,
                        BlockchainFactoryEnum.ORIGINAL);
                shortDataBlockchain = Blockchain.checkFromFile(Seting.ORIGINAL_BLOCKCHAIN_FILE);
                blockcheinSize = (int) shortDataBlockchain.getSize();
                blockchainValid = shortDataBlockchain.isValidation();
                prevBlock = Blockchain.indexFromFile(blockcheinSize-1, Seting.ORIGINAL_BLOCKCHAIN_FILE);

            }

            if (!shortDataBlockchain.isValidation()) {
                System.out.println("wrong block chain, delete blocks: from to block:");
                UtilsBlock.deleteFiles();
                blockchain.setBlockchainList(new ArrayList<>());
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
                    addBlock(addlist);
                    shortDataBlockchain = temp;
                    blockcheinSize = (int) shortDataBlockchain.getSize();
                    blockchainValid = shortDataBlockchain.isValidation();
                     System.out.println("after original: " + shortDataBlockchain);
                         System.out.println("after temp: " + temp);
                         prevBlock = Blockchain.indexFromFile(blockcheinSize-1, Seting.ORIGINAL_BLOCKCHAIN_FILE);
                if(blockcheinSize > 600){
                    dificultyOneBlock = UtilsBlock.difficulty(Blockchain.subFromFile(
                                    blockcheinSize-600, blockcheinSize, Seting.ORIGINAL_BLOCKCHAIN_FILE),
                            Seting.BLOCK_GENERATION_INTERVAL,
                            Seting.DIFFICULTY_ADJUSTMENT_INTERVAL);
                }
                else {
                    dificultyOneBlock = UtilsBlock.difficulty(Blockchain.subFromFile(
                                    0, blockcheinSize, Seting.ORIGINAL_BLOCKCHAIN_FILE),
                            Seting.BLOCK_GENERATION_INTERVAL,
                            Seting.DIFFICULTY_ADJUSTMENT_INTERVAL);
                }
                    System.out.println("*************************************");



                return new ResponseEntity<>("OK", HttpStatus.OK);
            }
            else {
                return new ResponseEntity<>("FALSE", HttpStatus.EXPECTATION_FAILED);
            }


        }finally {
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


