package International_Trade_Union.utils;

import International_Trade_Union.controllers.BasisController;
import International_Trade_Union.entity.EntityChain;
import International_Trade_Union.entity.SendBlocksEndInfo;
import International_Trade_Union.entity.SubBlockchainEntity;
import International_Trade_Union.entity.blockchain.Blockchain;
import International_Trade_Union.entity.blockchain.DataShortBlockchainInformation;
import International_Trade_Union.entity.blockchain.block.Block;
import International_Trade_Union.entity.entities.EntityAccount;
import International_Trade_Union.entity.entities.EntityBlock;
import International_Trade_Union.entity.services.BlockService;
import International_Trade_Union.logger.MyLogger;
import International_Trade_Union.model.Account;
import International_Trade_Union.model.HostEndDataShortB;
import International_Trade_Union.model.Mining;
import International_Trade_Union.model.comparator.HostEndDataShortBComparator;
import International_Trade_Union.setings.Seting;
import International_Trade_Union.vote.LawEligibleForParliamentaryApproval;
import International_Trade_Union.vote.Laws;
import International_Trade_Union.vote.UtilsLaws;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.json.JSONException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;


import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SignatureException;
import java.security.spec.InvalidKeySpecException;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static International_Trade_Union.controllers.BasisController.*;
import static International_Trade_Union.setings.Seting.RANDOM_HOSTS;
import static International_Trade_Union.utils.UtilsBalance.calculateBalance;
import static International_Trade_Union.utils.UtilsBalance.rollbackCalculateBalance;

@Component
public class UtilsResolving {
    @Autowired
    BlockService blockService;

    @Autowired
    DomainConfiguration domainConfiguration;

    public int resolve3() {
        BasisController.setUpdating(true);
        UtilsBalance.setBlockService(blockService);
        Blockchain.setBlockService(blockService);
        UtilsBlock.setBlockService(blockService);

        //удаляет файлы которые хранять заблокированные хосты

        int bigSize = 0;
        try {
            System.out.println(" :start resolve");
            utilsMethod();

            //local blockchain size
            //размер локального блокчейна
            int blocks_current_size = BasisController.getBlockchainSize();

            EntityChain entityChain = null;
            System.out.println(" resolve3:local size: " + blocks_current_size);


            System.out.println(":resolve3: size nodes: " + getNodes().size());
            //goes through all hosts (repositories) in search of the most up-to-date blockchain
            //проходит по всем хостам(хранилищам) в поисках самого актуального блокчейна

            //сортирует по приоритетности блокчейны
            Map<HostEndDataShortB, List<Block>> tempBestBlock = new HashMap<>();
            Set<String> nodesAll = getNodes();
            List<HostEndDataShortB> sortPriorityHost = sortPriorityHost(nodesAll);
            Set<String> newAddress = newHostsLoop(sortPriorityHost.stream().map(t -> t.getHost()).collect(Collectors.toSet()));
            newAddress.remove(nodesAll);

            for (String s : newAddress) {
                UtilsAllAddresses.putHost(s);
            }


            hostContinue:
            for (HostEndDataShortB hostEndDataShortB : sortPriorityHost) {
                String s = hostEndDataShortB.getHost();

                //if the local address matches the host address, it skips
                //если локальный адрес совпадает с адресом хоста, он пропускает
                if (BasisController.getExcludedAddresses().contains(s)) {
                    System.out.println(":its your address or excluded address: " + s);
                    continue;
                }
                try {
                    //if the address is localhost, it skips
                    //если адрес локального хоста, он пропускает
                    if (Seting.IS_TEST == false && (s.contains("localhost") || s.contains("127.0.0.1")))
                        continue;
                    String sizeStr = UtilUrl.readJsonFromUrl(s + "/size");
                    Integer size = Integer.valueOf(sizeStr);

                    MyLogger.saveLog("resolve3: " + sizeStr + " s " + s);
                    //здесь устанавливает самый длинный блокчейн.
                    if (size > bigSize) {
                        bigSize = size;

                    }


                    String jsonGlobalData = UtilUrl.readJsonFromUrl(s + "/datashort");
                    System.out.println("jsonGlobalData: " + jsonGlobalData);
                    if (jsonGlobalData == null || jsonGlobalData.isEmpty() || jsonGlobalData.isBlank()) {
                        System.out.println("*********************************************************");
                        System.out.println("jsonGlobalData: error: " + jsonGlobalData);
                        System.out.println("*********************************************************");
                        continue;
                    }
                    DataShortBlockchainInformation global = UtilsJson.jsonToDataShortBlockchainInformation(jsonGlobalData);
//
                    //if the size from the storage is larger than on the local server, start checking
                    //если размер с хранилища больше чем на локальном сервере, начать проверку
                    System.out.println("resolve3 size: " + size + " blocks_current_size: " + blocks_current_size);

                    if (isBig(BasisController.getShortDataBlockchain(), global)) {
                        System.out.println(":size from address: " + s + " upper than: " + size + ":blocks_current_size " + blocks_current_size);
                        //Test start algorithm
                        //600 последних блоков, для подсчета сложности, для последнего блока.
                        List<Block> lastDiff = new ArrayList<>();
                        SubBlockchainEntity subBlockchainEntity = null;
                        String subBlockchainJson = null;
//                        Map<String, Account> balances = SaveBalances.readLineObject(Seting.ORIGINAL_BALANCE_FILE);
//                        Map<String, Account> balances = UtilsAccountToEntityAccount.entityAccountsToMapAccounts(blockService.findAllAccounts());
                        Map<String, Account> balances = new HashMap<>();
//                        Map<String, Account> tempBalances = SaveBalances.readLineObject(Seting.ORIGINAL_BALANCE_FILE);
//                        Map<String, Account> tempBalances = UtilsUse.balancesClone(balances);
//                        Map<String, Account> tempBalances = UtilsAccountToEntityAccount.entityAccountsToMapAccounts(blockService.findAllAccounts());
                        Map<String, Account> tempBalances = new HashMap<>();


                        //if the local one lags behind the global one by more than PORTION_DOWNLOAD (500 blocks), then you need to download in portions from the storage
                        //если локальный отстает от глобального больше чем PORTION_DOWNLOAD (500 блоков), то нужно скачивать порциями из хранилища
                        if (size - blocks_current_size > Seting.PORTION_DOWNLOAD) {
                            boolean downloadPortion = true;
                            int finish = blocks_current_size + Seting.PORTION_DOWNLOAD;
                            int start = blocks_current_size;

                            //while the difference in the size of the local blockchain is greater than from the host, it will continue to download in portions to download the entire blockchain
                            //пока разница размера локального блокчейна больше чем с хоста будет продолжаться скачивать порциями, чтобы скачать весь блокчейн
                            stop:
                            while (downloadPortion) {
                                //здесь говориться, с какого блока по какой блок скачивать.

                                boolean different_value = false;
                                boolean local_size_upper = false;
                                //TODO возможно это решит проблему, если блоки будут равны
                                if (blocks_current_size == size) {
                                    subBlockchainEntity = new SubBlockchainEntity(blocks_current_size - 1, size);
                                    different_value = true;
                                }
                                if (size < blocks_current_size) {
                                    subBlockchainEntity = new SubBlockchainEntity(size - Seting.IS_BIG_DIFFERENT, size);
                                    System.out.println("subBlockchainEntity: size < blocks_current_size: " + subBlockchainEntity);
                                    local_size_upper = true;
                                }


                                subBlockchainEntity = new SubBlockchainEntity(start, finish);

                                System.out.println("1:shortDataBlockchain:  " + BasisController.getShortDataBlockchain());
                                System.out.println("1:sublockchainEntity: " + subBlockchainEntity);
                                subBlockchainJson = UtilsJson.objToStringJson(subBlockchainEntity);
                                System.out.println("1:sublockchainJson: " + subBlockchainJson);
                                String str = UtilUrl.getObject(subBlockchainJson, s + "/sub-blocks");
                                if (str.isEmpty() || str.isBlank()) {
                                    System.out.println("-------------------------------------");
                                    System.out.println("sublocks:  str: empty " + str);
                                    System.out.println("-------------------------------------");
                                    continue;
                                }
                                List<Block> subBlocks = UtilsJson.jsonToObject(str);

                                if (subBlocks.isEmpty() || subBlocks.size() == 0) {
                                    System.out.println("-------------------------------------");
                                    System.out.println("sublocks: " + subBlocks.size());
                                    System.out.println("-------------------------------------");
                                    continue;
                                }
                                System.out.println("1:download sub block: " + subBlocks.size());
                                System.out.println("2: host: " + s);
                                if (Seting.IS_SECURITY == true && subBlocks.size() < Seting.PORTION_DOWNLOAD) {
                                    System.out.println("Blocked host: " + subBlocks.size());
                                    //TODO записывать сюда заблокированные хосты
                                    UtilsAllAddresses.saveAllAddresses(hostEndDataShortB.getHost(), Seting.ORIGINAL_POOL_URL_ADDRESS_BLOCKED_FILE);
                                    continue hostContinue;
                                }

                                System.out.println("1. subBlocks: subBLock size - 1:" + (subBlocks.size() - 1));
                                System.out.println("1. finish: subBlocks: subBLock getIndex() + Seting.PORTION_DOWNLOAD + 1:" + (subBlocks.get(subBlocks.size() - 1).getIndex() + Seting.PORTION_DOWNLOAD + 1));
                                System.out.println("1. start: subBlocks: subBLockstart = (int) subBlocks.get(subBlocks.size() - 1).getIndex() + 1;:" + (subBlocks.get(subBlocks.size() - 1).getIndex() + 1));

                                finish = (int) subBlocks.get(subBlocks.size() - 1).getIndex() + Seting.PORTION_DOWNLOAD + 1;
                                start = (int) subBlocks.get(subBlocks.size() - 1).getIndex() + 1; //вот здесь возможно сделать + 2


//                                balances = SaveBalances.readLineObject(Seting.ORIGINAL_BALANCE_FILE);
//                                balances = UtilsAccountToEntityAccount.entityAccountsToMapAccounts(blockService.findAllAccounts());
                                balances = UtilsAccountToEntityAccount.entityAccountsToMapAccounts(UtilsUse.accounts(subBlocks, blockService));
//                                tempBalances = UtilsAccountToEntityAccount.entityAccountsToMapAccounts(blockService.findAllAccounts());
                                tempBalances = UtilsAccountToEntityAccount.entityAccountsToMapAccounts(UtilsUse.accounts(subBlocks, blockService));

                                //вычисляет сложность блока, для текущего блока, на основе предыдущих блоков.
                                //select a block class for the current block, based on previous blocks.

                                if (BasisController.getBlockchainSize() > Seting.PORTION_BLOCK_TO_COMPLEXCITY && BasisController.getBlockchainSize() < Seting.V34_NEW_ALGO) {
                                    lastDiff = UtilsBlockToEntityBlock.entityBlocksToBlocks(
                                            blockService.findBySpecialIndexBetween(
                                                    (BasisController.prevBlock().getIndex() + 1) - Seting.PORTION_BLOCK_TO_COMPLEXCITY,
                                                    BasisController.prevBlock().getIndex() + 1
                                            )
                                    );
                                }

                                if (Seting.IS_SECURITY == true && subBlocks.size() < Seting.PORTION_DOWNLOAD) {
                                    System.out.println("Blocked host: size block:" + subBlocks.size());
                                    //TODO записывать сюда заблокированные хосты
                                    UtilsAllAddresses.saveAllAddresses(hostEndDataShortB.getHost(), Seting.ORIGINAL_POOL_URL_ADDRESS_BLOCKED_FILE);
                                    continue hostContinue;

                                }
                                //класс мета данных блокчейна.
                                DataShortBlockchainInformation temp = new DataShortBlockchainInformation();

                                //загружает баланс всех счетов для текущего блокчейна.
                                List<String> sign = new ArrayList<>();
                                if (BasisController.getBlockchainSize() > 1) {
                                    //проверяет скаченные блоки на целостность
                                    //checks downloaded blocks for integrity
                                    temp = Blockchain.shortCheck(BasisController.prevBlock(), subBlocks, BasisController.getShortDataBlockchain(), lastDiff, tempBalances, sign);
                                    System.out.println("prevBlock: " + BasisController.prevBlock().getIndex());
                                } else {

                                    Block genesis = UtilsJson.jsonToBLock("{\"dtoTransactions\":[{\"sender\":\"faErFrDnBhfSfNnj1hYjxydKNH28cRw1PBwDQEXH3QsJ\",\"customer\":\"nNifuwmFZr7fnV1zvmpiyQDV5z7ETWvqR6GSeqeHTY43\",\"digitalDollar\":6.5E7,\"digitalStockBalance\":6.5E7,\"laws\":{\"packetLawName\":null,\"laws\":null,\"hashLaw\":null},\"bonusForMiner\":0.0,\"voteEnum\":\"YES\",\"sign\":\"MEUCIDDW9fKvwUY0aXpvamxOU6pypicO3eCqEVM9LDFrIpjIAiEA81Zh7yCBbJOLrAzx4mg5HS0hMdqvB0obO2CZARczmfY=\"}],\"previousHash\":\"0234a350f4d56ae45c5ece57b08c54496f372bc570bd83a465fb6d2d85531479\",\"minerAddress\":\"nNifuwmFZr7fnV1zvmpiyQDV5z7ETWvqR6GSeqeHTY43\",\"founderAddress\":\"nNifuwmFZr7fnV1zvmpiyQDV5z7ETWvqR6GSeqeHTY43\",\"randomNumberProof\":12,\"minerRewards\":0.0,\"hashCompexity\":1,\"timestamp\":1685942742706,\"index\":1,\"hashBlock\":\"08b1e6634457a40d3481e76ebd377e76322706e4ea27013b773686f7df8f8a4c\"}");
                                    Block firstBlock = UtilsJson.jsonToBLock("{\"dtoTransactions\":[{\"sender\":\"faErFrDnBhfSfNnj1hYjxydKNH28cRw1PBwDQEXH3QsJ\",\"customer\":\"nNifuwmFZr7fnV1zvmpiyQDV5z7ETWvqR6GSeqeHTY43\",\"digitalDollar\":400.0,\"digitalStockBalance\":400.0,\"laws\":{\"packetLawName\":null,\"laws\":null,\"hashLaw\":null},\"bonusForMiner\":0.0,\"voteEnum\":\"YES\",\"sign\":\"MEUCIQDfQ3TAOyuWi4NGr0hNuXjqzxDCL0U8DzwAmedSOw9eiwIgdRlZwmudMZJZURMtgmOwpT+wk569jo/Ok/fAGv0x/NE=\"},{\"sender\":\"faErFrDnBhfSfNnj1hYjxydKNH28cRw1PBwDQEXH3QsJ\",\"customer\":\"nNifuwmFZr7fnV1zvmpiyQDV5z7ETWvqR6GSeqeHTY43\",\"digitalDollar\":8.0,\"digitalStockBalance\":8.0,\"laws\":{\"packetLawName\":null,\"laws\":null,\"hashLaw\":null},\"bonusForMiner\":0.0,\"voteEnum\":\"YES\",\"sign\":\"MEYCIQDV9MbTMPl/dWBTfc87rMcRBBKcNZsGtkuRx1pdzGrSKQIhALOFpX81JEyFCC8uQ//bZkSW9CaOODSgOaMkYgTHn5HC\"}],\"previousHash\":\"08b1e6634457a40d3481e76ebd377e76322706e4ea27013b773686f7df8f8a4c\",\"minerAddress\":\"nNifuwmFZr7fnV1zvmpiyQDV5z7ETWvqR6GSeqeHTY43\",\"founderAddress\":\"nNifuwmFZr7fnV1zvmpiyQDV5z7ETWvqR6GSeqeHTY43\",\"randomNumberProof\":4,\"minerRewards\":0.0,\"hashCompexity\":1,\"timestamp\":1685942784960,\"index\":1,\"hashBlock\":\"06b932aadd602056b0fb7294ef693535009cb3ba54581f32fd4aa1d93108703f\"}");

                                    if (!subBlocks.get(0).equals(genesis) || !subBlocks.get(1).equals(firstBlock)) {
                                        System.out.println("error basis block: ");
                                        System.out.println("genesis: " + subBlocks.get(0).getIndex());
                                        System.out.println("first: " + subBlocks.get(1).getIndex());
                                        System.out.println("temp: " + temp);
                                    }
                                    boolean save = addBlock3(subBlocks, balances, Seting.ORIGINAL_BLOCKCHAIN_FILE);
                                    temp = Blockchain.checkFromFile(Seting.ORIGINAL_BLOCKCHAIN_FILE);
                                    if (!temp.isValidation()) {
                                        System.out.println("error validation: " + temp);
                                    }

                                    if (save) {
                                        BasisController.setShortDataBlockchain(temp);
                                        BasisController.setBlockcheinSize((int) temp.getSize());
                                        BasisController.setBlockchainValid(temp.isValidation());

                                        EntityBlock tempBlock = blockService.findBySpecialIndex(BasisController.getBlockchainSize() - 1);
                                        BasisController.setPrevBlock(UtilsBlockToEntityBlock.entityBlockToBlock(tempBlock));
                                        System.out.println("prevBlock: " + BasisController.prevBlock().getIndex() + " shortDataBlockchain: " + BasisController.getShortDataBlockchain());
                                        String json = UtilsJson.objToStringJson(BasisController.getShortDataBlockchain());
                                        UtilsFileSaveRead.save(json, Seting.TEMPORARY_BLOCKCHAIN_FILE, false);
//                                    continue;
                                    }
                                    continue hostContinue;

                                }


                                System.out.println("++++++++++++++++++++++++++++++++++++++++++++++++");
                                System.out.println("1: temp: " + temp);
                                System.out.println("1: blockchainsize: " + BasisController.getBlockchainSize());
                                System.out.println("1: sublocks: size: " + subBlocks.size());
                                System.out.println("1: shortDataBlockchain: " + BasisController.getShortDataBlockchain());
                                System.out.println("1: host: " + s);


                                jsonGlobalData = UtilUrl.readJsonFromUrl(s + "/datashort");
                                if (jsonGlobalData == null || jsonGlobalData.isEmpty() || jsonGlobalData.isBlank()) {
                                    System.out.println("*********************************************************");
                                    System.out.println(" 2 jsonGlobalData: error: " + jsonGlobalData);
                                    System.out.println("*********************************************************");
                                    continue;
                                }
                                System.out.println("2: jsonGlobalData: " + jsonGlobalData);
                                global = UtilsJson.jsonToDataShortBlockchainInformation(jsonGlobalData);
                                temp = new DataShortBlockchainInformation();
                                sign = new ArrayList<>();
                                temp = Blockchain.shortCheck(BasisController.prevBlock(), subBlocks, BasisController.getShortDataBlockchain(), lastDiff, tempBalances, sign);


//                                balances = UtilsAccountToEntityAccount.entityAccountsToMapAccounts(blockService.findAllAccounts());
                                balances = UtilsAccountToEntityAccount.entityAccountsToMapAccounts(UtilsUse.accounts(subBlocks, blockService));
//                                tempBalances = UtilsAccountToEntityAccount.entityAccountsToMapAccounts(blockService.findAllAccounts());
                                tempBalances = UtilsAccountToEntityAccount.entityAccountsToMapAccounts(UtilsUse.accounts(subBlocks, blockService));
                                sign = new ArrayList<>();
                                temp = new DataShortBlockchainInformation();
                                temp = Blockchain.shortCheck(BasisController.prevBlock(), subBlocks, BasisController.getShortDataBlockchain(), lastDiff, tempBalances, sign);
//                                balances = UtilsAccountToEntityAccount.entityAccountsToMapAccounts(blockService.findAllAccounts());
                                balances = UtilsAccountToEntityAccount.entityAccountsToMapAccounts(UtilsUse.accounts(subBlocks, blockService));
//                                tempBalances = UtilsAccountToEntityAccount.entityAccountsToMapAccounts(blockService.findAllAccounts());
                                tempBalances = UtilsAccountToEntityAccount.entityAccountsToMapAccounts(UtilsUse.accounts(subBlocks, blockService));
                                sign = new ArrayList<>();
                                if (!local_size_upper) {
                                    System.out.println("===========================");
                                    System.out.println("!local_size_upper: " + !local_size_upper);
                                    System.out.println("===========================");
                                    temp = helpResolve4(temp, global, s, lastDiff, tempBalances, sign, balances, subBlocks, false);

                                }

                                if (local_size_upper) {
                                    System.out.println("===========================");
                                    System.out.println("local_size_upper: " + local_size_upper);
                                    System.out.println("===========================");
                                    temp = helpResolve5(temp, global, s, lastDiff, tempBalances, sign, balances, subBlocks, false);
                                }

                                System.out.println("++++++++++++++++++++++++++++++++++++++++++++++++");
                                //если скачанный блокчейн не валидный, то не добавляет в блокчейн, возвращает -10
                                if (BasisController.getBlockchainSize() > 1 && !temp.isValidation()) {
                                    System.out.println("error resolve 2 in portion upper > 500");
                                    return -10;
                                }


                                //если количество новых блоков, относительно локального блокчейна меньше 500,
                                //то скачать эти блоки и прекратить попытки скачивания с данного узла.
                                //if the number of new blocks relative to the local blockchain is less than 500,
                                //then download these blocks and stop trying to download from this node.
                                if (size - BasisController.prevBlock().getIndex() < Seting.PORTION_DOWNLOAD) {

                                    different_value = false;
                                    local_size_upper = false;
                                    //TODO возможно это решит проблему, если блоки будут равны
                                    if (blocks_current_size == size) {
                                        subBlockchainEntity = new SubBlockchainEntity(blocks_current_size - 1, size);
                                        different_value = true;
                                    }
                                    if (size < blocks_current_size) {
                                        subBlockchainEntity = new SubBlockchainEntity(size - Seting.IS_BIG_DIFFERENT, size);
                                        System.out.println("subBlockchainEntity: size < blocks_current_size: " + subBlockchainEntity);
                                        local_size_upper = true;
                                    }

                                    downloadPortion = false;
                                    finish = size;
                                    subBlockchainEntity = new SubBlockchainEntity(start, finish);
                                    System.out.println("2:sublockchainEntity: " + subBlockchainEntity);
                                    subBlockchainJson = UtilsJson.objToStringJson(subBlockchainEntity);
                                    System.out.println("2:sublockchainJson: " + subBlockchainJson);
                                    str = UtilUrl.getObject(subBlockchainJson, s + "/sub-blocks");
                                    if (str.isEmpty() || str.isBlank()) {
                                        System.out.println("-------------------------------------");
                                        System.out.println("sublocks:  str: empty " + str);
                                        System.out.println("-------------------------------------");
                                        continue;
                                    }
                                    subBlocks = UtilsJson.jsonToObject(str);

                                    if (subBlocks.isEmpty() || subBlocks.size() == 0) {
                                        System.out.println("-------------------------------------");
                                        System.out.println("sublocks: " + subBlocks.size());
                                        System.out.println("-------------------------------------");
                                        continue;
                                    }
                                    System.out.println("2:download sub block: " + subBlocks.size());
                                    System.out.println("2: host: " + s);

//                                    balances = SaveBalances.readLineObject(Seting.ORIGINAL_BALANCE_FILE);
                                    if (BasisController.getBlockchainSize() > Seting.PORTION_BLOCK_TO_COMPLEXCITY && BasisController.getBlockchainSize() < Seting.V34_NEW_ALGO) {
                                        lastDiff = UtilsBlockToEntityBlock.entityBlocksToBlocks(
                                                blockService.findBySpecialIndexBetween(
                                                        (BasisController.prevBlock().getIndex() + 1) - Seting.PORTION_BLOCK_TO_COMPLEXCITY,
                                                        BasisController.prevBlock().getIndex() + 1
                                                )
                                        );
                                    }

//                                    if (BasisController.getBlockchainSize() > 1) {
//                                        temp = Blockchain.shortCheck(BasisController.getPrevBlock(), subBlocks, BasisController.getShortDataBlockchain(), lastDiff, tempBalances, sign);
//                                    }

                                    jsonGlobalData = UtilUrl.readJsonFromUrl(s + "/datashort");
                                    if (jsonGlobalData == null || jsonGlobalData.isEmpty() || jsonGlobalData.isBlank()) {
                                        System.out.println("*********************************************************");
                                        System.out.println(" 2 jsonGlobalData: error: " + jsonGlobalData);
                                        System.out.println("*********************************************************");
                                        continue;
                                    }
                                    System.out.println("2: jsonGlobalData: " + jsonGlobalData);
                                    global = UtilsJson.jsonToDataShortBlockchainInformation(jsonGlobalData);
//                                    balances = UtilsAccountToEntityAccount.entityAccountsToMapAccounts(blockService.findAllAccounts());
                                    balances = UtilsAccountToEntityAccount.entityAccountsToMapAccounts(UtilsUse.accounts(subBlocks, blockService));
//                                    tempBalances = UtilsAccountToEntityAccount.entityAccountsToMapAccounts(blockService.findAllAccounts());
                                    tempBalances = UtilsAccountToEntityAccount.entityAccountsToMapAccounts(UtilsUse.accounts(subBlocks, blockService));
                                    sign = new ArrayList<>();
                                    temp = new DataShortBlockchainInformation();
                                    temp = Blockchain.shortCheck(BasisController.prevBlock(), subBlocks, BasisController.getShortDataBlockchain(), lastDiff, tempBalances, sign);
//                                    tempBalances = UtilsAccountToEntityAccount.entityAccountsToMapAccounts(blockService.findAllAccounts());
                                    tempBalances = UtilsAccountToEntityAccount.entityAccountsToMapAccounts(UtilsUse.accounts(subBlocks, blockService));
                                    sign = new ArrayList<>();


                                    System.out.println("2: temp: " + temp);
                                    System.out.println("2: blockchainsize: " + BasisController.getBlockchainSize());
                                    System.out.println("2: sublocks: " + subBlocks.size());
                                    System.out.println("2: host: " + s);


//                                    balances = UtilsAccountToEntityAccount.entityAccountsToMapAccounts(blockService.findAllAccounts());
                                    balances = UtilsAccountToEntityAccount.entityAccountsToMapAccounts(UtilsUse.accounts(subBlocks, blockService));
//                                    tempBalances = UtilsAccountToEntityAccount.entityAccountsToMapAccounts(blockService.findAllAccounts());
                                    tempBalances = UtilsAccountToEntityAccount.entityAccountsToMapAccounts(UtilsUse.accounts(subBlocks, blockService));
                                    sign = new ArrayList<>();
                                    temp = new DataShortBlockchainInformation();
                                    temp = Blockchain.shortCheck(BasisController.prevBlock(), subBlocks, BasisController.getShortDataBlockchain(), lastDiff, tempBalances, sign);
//                                    balances = UtilsAccountToEntityAccount.entityAccountsToMapAccounts(blockService.findAllAccounts());
                                    balances = UtilsAccountToEntityAccount.entityAccountsToMapAccounts(UtilsUse.accounts(subBlocks, blockService));
//                                    tempBalances = UtilsAccountToEntityAccount.entityAccountsToMapAccounts(blockService.findAllAccounts());
                                    tempBalances = UtilsAccountToEntityAccount.entityAccountsToMapAccounts(UtilsUse.accounts(subBlocks, blockService));
                                    sign = new ArrayList<>();
                                    if (!local_size_upper) {
                                        System.out.println("===========================");
                                        System.out.println("!local_size_upper: " + !local_size_upper);
                                        System.out.println("===========================");
                                        temp = helpResolve4(temp, global, s, lastDiff, tempBalances, sign, balances, subBlocks, true);

                                    }

                                    if (local_size_upper) {
                                        System.out.println("===========================");
                                        System.out.println("local_size_upper: " + local_size_upper);
                                        System.out.println("===========================");
                                        temp = helpResolve5(temp, global, s, lastDiff, tempBalances, sign, balances, subBlocks, true);
                                    }

                                    if (Seting.IS_SECURITY && BasisController.getBlockchainSize() > 1 && !temp.isValidation()) {
                                        //TODO добавить хост в заблокированный файл

                                        System.out.println("-------------------------------------------------");
                                        System.out.println("Blocked host: ");
                                        System.out.println("expected host: " + hostEndDataShortB.getDataShortBlockchainInformation());
                                        System.out.println("host: " + hostEndDataShortB.getHost());
                                        System.out.println("-------------------------------------------------");
                                        UtilsAllAddresses.saveAllAddresses(hostEndDataShortB.getHost(), Seting.ORIGINAL_POOL_URL_ADDRESS_BLOCKED_FILE);
                                        continue;
                                    }

//                                    BasisController.setShortDataBlockchain(temp);
//                                    BasisController.setBlockcheinSize((int) temp.getSize());
//                                    BasisController.setBlockchainValid(temp.isValidation());
//
//                                    tempBlock = blockService.findBySpecialIndex(BasisController.getBlockchainSize() - 1);
//                                    BasisController.setPrevBlock(UtilsBlockToEntityBlock.entityBlockToBlock(tempBlock));
//
//                                    json = UtilsJson.objToStringJson(BasisController.getShortDataBlockchain());
//                                    UtilsFileSaveRead.save(json, Seting.TEMPORARY_BLOCKCHAIN_FILE, false);
                                }
                            }
                        } else {

                            //здесь нужно проверить
                            //If the difference is not greater than PORTION_DOWNLOAD, then downloads once a portion of this difference
                            //Если разница не больше PORTION_DOWNLOAD, то скачивает один раз порцию эту разницу
                            subBlockchainEntity = new SubBlockchainEntity(blocks_current_size, size);

                            boolean different_value = false;
                            boolean local_size_upper = false;
                            //TODO возможно это решит проблему, если блоки будут равны
                            if (blocks_current_size == size) {
                                subBlockchainEntity = new SubBlockchainEntity(blocks_current_size - 1, size);
                                different_value = true;
                                MyLogger.saveLog("blocks_current_size == size");
                            }
                            if (size < blocks_current_size) {
                                subBlockchainEntity = new SubBlockchainEntity(size - Seting.IS_BIG_DIFFERENT, size);
                                System.out.println("subBlockchainEntity: size < blocks_current_size: " + subBlockchainEntity);
                                local_size_upper = true;
                                MyLogger.saveLog("size < blocks_current_size");
                            }


                            subBlockchainJson = UtilsJson.objToStringJson(subBlockchainEntity);


                            String str = UtilUrl.getObject(subBlockchainJson, s + "/sub-blocks");
                            if (str.isEmpty() || str.isBlank()) {
                                System.out.println("-------------------------------------");
                                System.out.println("sublocks:  str: empty " + str);
                                MyLogger.saveLog("str.isEmpty() || str.isBlank()");
                                System.out.println("-------------------------------------");
                                continue;
                            }
                            List<Block> subBlocks = UtilsJson.jsonToObject(str);

                            if (subBlocks == null || subBlocks.isEmpty() || subBlocks.size() == 0) {
                                System.out.println("-------------------------------------");
                                System.out.println("sublocks: " + subBlocks.size());
                                MyLogger.saveLog("subBlocks.isEmpty() || subBlocks.size() == 0");
                                System.out.println("-------------------------------------");
                                continue;
                            }
                            System.out.println("3:download sub block: " + subBlocks.size());
//                            tempBalances = SaveBalances.readLineObject(Seting.ORIGINAL_BALANCE_FILE);
                            List<EntityAccount> accounts = blockService.findAllAccounts();
                            if (accounts == null || accounts.isEmpty()) {
                                MyLogger.saveLog("accounts == null || accounts.isEmpty()");
                                continue;
                            }
                            tempBalances = UtilsAccountToEntityAccount.entityAccountsToMapAccounts(accounts);
                            List<String> sign = new ArrayList<>();

                            if (BasisController.getBlockchainSize() > Seting.PORTION_BLOCK_TO_COMPLEXCITY && BasisController.getBlockchainSize() < Seting.V34_NEW_ALGO) {
                                lastDiff = UtilsBlockToEntityBlock.entityBlocksToBlocks(
                                        blockService.findBySpecialIndexBetween(
                                                (BasisController.prevBlock().getIndex() + 1) - Seting.PORTION_BLOCK_TO_COMPLEXCITY,
                                                BasisController.prevBlock().getIndex() + 1
                                        )
                                );
                            }

                            DataShortBlockchainInformation temp = new DataShortBlockchainInformation();
                            temp = Blockchain.shortCheck(BasisController.prevBlock(), subBlocks, BasisController.getShortDataBlockchain(), lastDiff, tempBalances, sign);

                            jsonGlobalData = UtilUrl.readJsonFromUrl(s + "/datashort");
                            if (jsonGlobalData == null || jsonGlobalData.isEmpty() || jsonGlobalData.isBlank()) {
                                System.out.println("*********************************************************");
                                System.out.println("jsonGlobalData: error: " + jsonGlobalData);
                                MyLogger.saveLog("jsonGlobalData == null || jsonGlobalData.isEmpty() || jsonGlobalData.isBlank():" + jsonGlobalData);
                                System.out.println("*********************************************************");
                                continue;
                            }
                            System.out.println("3: jsonGlobalData: " + jsonGlobalData);
                            global = UtilsJson.jsonToDataShortBlockchainInformation(jsonGlobalData);


                            System.out.println("3: temp: " + temp);
                            System.out.println("3: blockchainsize: " + BasisController.getBlockchainSize());
                            System.out.println("3: sublocks: " + subBlocks.size());


//                            balances = UtilsAccountToEntityAccount.entityAccountsToMapAccounts(blockService.findAllAccounts());
                            MyLogger.saveLog("before UtilsUse.accounts balances");
                            balances = UtilsAccountToEntityAccount.entityAccountsToMapAccounts(UtilsUse.accounts(subBlocks, blockService));
//                            tempBalances = UtilsAccountToEntityAccount.entityAccountsToMapAccounts(blockService.findAllAccounts());
                            MyLogger.saveLog("before UtilsUse.accounts tempBalances");
                            tempBalances = UtilsAccountToEntityAccount.entityAccountsToMapAccounts(UtilsUse.accounts(subBlocks, blockService));
                            sign = new ArrayList<>();
                            temp = new DataShortBlockchainInformation();
                            MyLogger.saveLog("before shortCheck");
                            temp = Blockchain.shortCheck(BasisController.prevBlock(), subBlocks, BasisController.getShortDataBlockchain(), lastDiff, tempBalances, sign);
//                            balances = UtilsAccountToEntityAccount.entityAccountsToMapAccounts(blockService.findAllAccounts());
                            MyLogger.saveLog("before UtilsUse.accounts balances");
                            balances = UtilsAccountToEntityAccount.entityAccountsToMapAccounts(UtilsUse.accounts(subBlocks, blockService));
//                            tempBalances = UtilsAccountToEntityAccount.entityAccountsToMapAccounts(blockService.findAllAccounts());
                            MyLogger.saveLog("before UtilsUse.accounts tempBalances");
                            tempBalances = UtilsAccountToEntityAccount.entityAccountsToMapAccounts(UtilsUse.accounts(subBlocks, blockService));
                            sign = new ArrayList<>();
                            if (!local_size_upper) {
                                System.out.println("===========================");
                                System.out.println("!local_size_upper: " + !local_size_upper);
                                System.out.println("===========================");
                                MyLogger.saveLog("before helpresolve4: "
                                        + "temp: " + temp
                                        + " global: " + global
                                        + " s: " + s
                                        + " last Diff: " + lastDiff
                                        + " tempBalances: " + tempBalances.size()
                                        + " sign: " + sign.size()
                                        + " balances: " + balances.size()
                                        + " subBlocks: " + subBlocks.size()
                                        + " checking true");

                                temp = helpResolve4(temp, global, s, lastDiff, tempBalances, sign, balances, subBlocks, true);
                                MyLogger.saveLog("after helpresolve4: "
                                        + "temp: " + temp
                                        + " global: " + global
                                        + " s: " + s
                                        + " last Diff: " + lastDiff
                                        + " tempBalances: " + tempBalances.size()
                                        + " sign: " + sign.size()
                                        + " balances: " + balances.size()
                                        + " subBlocks: " + subBlocks.size()
                                        + " checking true");

                            }

                            if (local_size_upper) {
                                System.out.println("===========================");
                                System.out.println("local_size_upper: " + local_size_upper);
                                System.out.println("===========================");
                                MyLogger.saveLog("before helpresolve5: "
                                        + "temp: " + temp
                                        + " global: " + global
                                        + " s: " + s
                                        + " last Diff: " + lastDiff
                                        + " tempBalances: " + tempBalances.size()
                                        + " sign: " + sign.size()
                                        + " balances: " + balances.size()
                                        + " subBlocks: " + subBlocks.size()
                                        + " checking true");
                                temp = helpResolve5(temp, global, s, lastDiff, tempBalances, sign, balances, subBlocks, true);
                                MyLogger.saveLog("after helpresolve5: "
                                        + "temp: " + temp
                                        + " global: " + global
                                        + " s: " + s
                                        + " last Diff: " + lastDiff
                                        + " tempBalances: " + tempBalances.size()
                                        + " sign: " + sign.size()
                                        + " balances: " + balances.size()
                                        + " subBlocks: " + subBlocks.size()
                                        + " checking true");
                            }


                            if (!temp.isValidation()) {
                                continue;
                            }

                        }
                        System.out.println("size temporaryBlockchain: ");
                        System.out.println("resolve: temporaryBlockchain: ");
                    } else {
                        System.out.println(":BasisController: resove: size less: " + size + " address: " + s);
                        continue;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    MyLogger.saveLog("resolve3: error: ", e);
                    MyLogger.saveLog("resolve3: error: ", e);
                    String stack = "";
                    for (StackTraceElement stack1 : e.getStackTrace()) {
                        stack += stack1.toString() + "\n";
                    }
                    MyLogger.saveLog("resolve3: stack error: " + stack);


                    continue;
                }
            }

        } finally {

            BasisController.setUpdating(false);
            if (BasisController.getBlockchainSize() > bigSize) {
                return 1;
            } else if (BasisController.getBlockchainSize() < bigSize) {
                return -1;
            } else {
                return 0;
            }

        }


    }

    //TODO возникает ошибка, потому что высота одинаковая, но при этом big random разный.
    //TODO нужно сделать еще один if внутри него поместить все if этого метода
    //TODO или он должен удалять свой блок и добавлять другой блок
    //TODO тестовая версия
    public boolean isBig(
            DataShortBlockchainInformation actual,
            DataShortBlockchainInformation global) {
        if (global.getSize() >= actual.getSize() - Seting.IS_BIG_DIFFERENT
                && global.getBigRandomNumber() > actual.getBigRandomNumber() + (prevBlock().getHashCompexity() * 25)) {
            return true;
        }
        return false;

    }


    public boolean isSmall(DataShortBlockchainInformation expected, DataShortBlockchainInformation actual) {
        if (
                actual.getSize() < expected.getSize()
                        || actual.getBigRandomNumber() < expected.getBigRandomNumber()
                        || actual.getTransactions() < expected.getTransactions()
                        || actual.getStaking() < expected.getTransactions()

        ) {
            return true;
        }
        return false;
    }

    public DataShortBlockchainInformation check(DataShortBlockchainInformation temp,
                                                DataShortBlockchainInformation global,
                                                String s,
                                                List<Block> lastDiff,
                                                Map<String, Account> tempBalances,
                                                List<String> sign) throws CloneNotSupportedException, IOException, NoSuchAlgorithmException, SignatureException, InvalidKeySpecException, NoSuchProviderException, InvalidKeyException {
        Map<String, Account> tempBalance = UtilsUse.balancesClone(tempBalances);
        if (BasisController.getShortDataBlockchain().getSize() > 1 && !temp.isValidation()) {
            System.out.println("__________________________________________________________");

            List<Block> emptyList = new ArrayList<>();
            List<Block> different = new ArrayList<>();

            int lastBlockIndex = (int) (global.getSize() - 1);
            int currentIndex = lastBlockIndex;


            stop:
            while (currentIndex >= 0) {

                int startIndex = Math.max(currentIndex - 499, 0);
                int endIndex = currentIndex;


                SubBlockchainEntity subBlockchainEntity = new SubBlockchainEntity(startIndex, endIndex);
                String subBlockchainJson = UtilsJson.objToStringJson(subBlockchainEntity);
                List<Block> blockList = UtilsJson.jsonToObject(UtilUrl.getObject(subBlockchainJson, s + "/sub-blocks"));
                System.out.println("check subBlockchainEntity: " + subBlockchainEntity);
                blockList = blockList.stream().sorted(Comparator.comparing(Block::getIndex).reversed()).collect(Collectors.toList());
                for (Block block : blockList) {
                    System.out.println("helpResolve4: block index: " + block.getIndex());

                    if (block.getIndex() > BasisController.getBlockchainSize() - 1) {
                        System.out.println("check :download blocks: " + block.getIndex() +
                                " your block : " + (BasisController.getBlockchainSize()) + ":waiting need download blocks: " + (block.getIndex() - BasisController.getBlockchainSize())
                                + " host: " + s);
                        emptyList.add(block);
                    } else if (!blockService.findBySpecialIndex(block.getIndex()).getHashBlock().equals(block.getHashBlock())) {
                        emptyList.add(block);
                        different.add(UtilsBlockToEntityBlock.entityBlockToBlock(blockService.findBySpecialIndex(block.getIndex())));
                        System.out.println("********************************");
                        System.out.println(":dowdnload block index: " + block.getIndex());
                        System.out.println(":block original index: " + blockService.findBySpecialIndex(block.getIndex()).getIndex());
                        System.out.println(":block from index: " + block.getIndex());
                    } else {
                        // Останавливаем итерацию, т.к. дальнейшие блоки будут идентичными
//                        emptyList.add(block);
//                        different.add(UtilsBlockToEntityBlock.entityBlockToBlock(blockService.findBySpecialIndex(block.getIndex())));

                        break stop;
                    }
                }

                // Обновляем индекс для следующей итерации
                currentIndex = startIndex - 1;
            }

            if (different.isEmpty() && emptyList.isEmpty()) {
                return temp;
            }
            System.out.println("different: " + different.size());
            System.out.println("emptyList: " + emptyList.size());

            System.out.println("check: shortDataBlockchain: " + BasisController.getShortDataBlockchain());
            System.out.println("check temp: " + temp);


            different = different.stream().sorted(Comparator.comparing(Block::getIndex)).collect(Collectors.toList());
            emptyList = emptyList.stream().sorted(Comparator.comparing(Block::getIndex)).collect(Collectors.toList());
            Block tempPrevBlock = UtilsBlockToEntityBlock.entityBlockToBlock(blockService.findBySpecialIndex(different.get(0).getIndex() - 1));
            temp = Blockchain.rollBackShortCheck(different, BasisController.getShortDataBlockchain(), tempBalance, sign);


            for (Block block : emptyList) {
                List<Block> tempList = new ArrayList<>();
                tempList.add(block);
                temp = Blockchain.shortCheck(tempPrevBlock, tempList, temp, lastDiff, tempBalance, sign);
                tempPrevBlock = block;
//                System.out.println("check: " + block.getIndex());
//                System.out.println("check: temp " + temp);
            }

        }
//        System.out.println("rollback temp: " + temp);

        return temp;
    }


    public DataShortBlockchainInformation check2(DataShortBlockchainInformation temp,
                                                 DataShortBlockchainInformation global,
                                                 String s,
                                                 List<Block> lastDiff,
                                                 Map<String, Account> tempBalances,
                                                 List<String> sign) throws CloneNotSupportedException, IOException, NoSuchAlgorithmException, SignatureException, InvalidKeySpecException, NoSuchProviderException, InvalidKeyException {
        Map<String, Account> tempBalance = UtilsUse.balancesClone(tempBalances);
        if (BasisController.getShortDataBlockchain().getSize() > 1 && !temp.isValidation()) {
            System.out.println("__________________________________________________________");

            List<Block> emptyList = new ArrayList<>();
            List<Block> different = new ArrayList<>();

            int lastBlockIndex = (int) (global.getSize() - 1);
            int currentIndex = lastBlockIndex;


            //TODO тестовая версия, мы проверяем если блокчейн ценее, но при этом меньше
            if (global.getSize() < BasisController.getBlockchainSize()) {
                List<EntityBlock> entityBlocks = blockService.findBySpecialIndexBetween(global.getSize(), BasisController.getBlockchainSize() - 1);
                different.addAll(UtilsBlockToEntityBlock.entityBlocksToBlocks(entityBlocks));

            }

            stop:
            while (currentIndex >= 0) {

                int startIndex = Math.max(currentIndex - 499, 0);
                int endIndex = currentIndex;

                SubBlockchainEntity subBlockchainEntity = new SubBlockchainEntity(startIndex, endIndex);
                String subBlockchainJson = UtilsJson.objToStringJson(subBlockchainEntity);
                List<Block> blockList = UtilsJson.jsonToObject(UtilUrl.getObject(subBlockchainJson, s + "/sub-blocks"));
                System.out.println("check2 subBlockchainEntity: " + subBlockchainEntity);
                blockList = blockList.stream().sorted(Comparator.comparing(Block::getIndex).reversed()).collect(Collectors.toList());


                for (Block block : blockList) {
                    System.out.println("check2: block index: " + block.getIndex());

                    if (block.getIndex() > BasisController.getBlockchainSize() - 1) {
                        System.out.println("check2 :download blocks: " + block.getIndex() +
                                " your block : " + (BasisController.getBlockchainSize()) + ":waiting need download blocks: " + (block.getIndex() - BasisController.getBlockchainSize())
                                + " host: " + s);
                        emptyList.add(block);
                    } else if (!blockService.findBySpecialIndex(block.getIndex()).getHashBlock().equals(block.getHashBlock())) {
                        emptyList.add(block);
                        different.add(UtilsBlockToEntityBlock.entityBlockToBlock(blockService.findBySpecialIndex(block.getIndex())));
                        System.out.println("********************************");
                        System.out.println(":dowdnload block index: " + block.getIndex());
                        System.out.println(":block original index: " + blockService.findBySpecialIndex(block.getIndex()).getIndex());
                        System.out.println(":block from index: " + block.getIndex());
                    } else {

                        break stop;
                    }
                }

                // Обновляем индекс для следующей итерации
                currentIndex = startIndex - 1;
            }

            if (different.isEmpty() && emptyList.isEmpty()) {
                return temp;
            }


            System.out.println("shortDataBlockchain: " + BasisController.getShortDataBlockchain());
            System.out.println("check 2: rollback temp: " + temp);

            different = different.stream().sorted(Comparator.comparing(Block::getIndex)).collect(Collectors.toList());
            emptyList = emptyList.stream().sorted(Comparator.comparing(Block::getIndex)).collect(Collectors.toList());

            Block tempPrevBlock = UtilsBlockToEntityBlock.entityBlockToBlock(blockService.findBySpecialIndex(different.get(0).getIndex() - 1));
            temp = Blockchain.rollBackShortCheck(different, BasisController.getShortDataBlockchain(), tempBalance, sign);

            if (!emptyList.isEmpty()) {

                for (Block block : emptyList) {
                    List<Block> tempList = new ArrayList<>();
                    tempList.add(block);
                    temp = Blockchain.shortCheck(tempPrevBlock, tempList, temp, lastDiff, tempBalance, sign);
                    tempPrevBlock = block;
                }
            }


        }
        return temp;
    }

    public DataShortBlockchainInformation helpResolve5(DataShortBlockchainInformation temp,
                                                       DataShortBlockchainInformation global,
                                                       String s,
                                                       List<Block> lastDiff,
                                                       Map<String, Account> tempBalances,
                                                       List<String> sign,
                                                       Map<String, Account> balances,
                                                       List<Block> subBlocks,
                                                       boolean checking)
            throws CloneNotSupportedException, IOException, NoSuchAlgorithmException, SignatureException,
            InvalidKeySpecException, NoSuchProviderException, InvalidKeyException {
        //TODO сначала найти блок откуда начинается ответление и докуда
        MyLogger.saveLog("start helpResolve5");
        Map<String, Account> tempBalance = UtilsUse.balancesClone(tempBalances);

        if (BasisController.getShortDataBlockchain().getSize() > 1 && !temp.isValidation()) {
            MyLogger.saveLog("helpResolve5 !temp.isValidation(): " + !temp.isValidation());
            System.out.println("__________________________________________________________");

            List<Block> emptyList = new ArrayList<>();
            List<Block> different = new ArrayList<>();

            int lastBlockIndex = (int) (global.getSize() - 1);
            int currentIndex = lastBlockIndex;
            //TODO тестовая версия, мы проверяем если блокчейн ценее, но при этом меньше
            if (global.getSize() < BasisController.getBlockchainSize()) {
                List<EntityBlock> entityBlocks = blockService.findBySpecialIndexBetween(global.getSize(), BasisController.getBlockchainSize() - 1);
                different.addAll(UtilsBlockToEntityBlock.entityBlocksToBlocks(entityBlocks));
            }
            try {

                stop:
                while (currentIndex >= 0) {

                    int startIndex = Math.max(currentIndex - 499, 0);
                    int endIndex = currentIndex;

                    SubBlockchainEntity subBlockchainEntity = new SubBlockchainEntity(startIndex, endIndex);
                    String subBlockchainJson = UtilsJson.objToStringJson(subBlockchainEntity);
                    List<Block> blockList = UtilsJson.jsonToObject(UtilUrl.getObject(subBlockchainJson, s + "/sub-blocks"));
                    System.out.println("subBlockchainEntity: " + subBlockchainEntity);
                    blockList = blockList.stream().sorted(Comparator.comparing(Block::getIndex).reversed()).collect(Collectors.toList());
                    for (Block block : blockList) {
                        System.out.println("helpResolve5: block index: " + block.getIndex());

                        if (block.getIndex() > BasisController.getBlockchainSize() - 1) {
                            System.out.println("helpResolve5 :download blocks: " + block.getIndex() +
                                    " your block : " + (BasisController.getBlockchainSize()) + ":waiting need download blocks: " + (block.getIndex() - BasisController.getBlockchainSize())
                                    + " host: " + s);
                            emptyList.add(block);
                        } else if (!blockService.findBySpecialIndex(block.getIndex()).getHashBlock().equals(block.getHashBlock())) {
                            emptyList.add(block);
                            different.add(UtilsBlockToEntityBlock.entityBlockToBlock(blockService.findBySpecialIndex(block.getIndex())));
                            System.out.println("********************************");
                            System.out.println(":dowdnload block index: " + block.getIndex());
                            System.out.println(":block original index: " + blockService.findBySpecialIndex(block.getIndex()).getIndex());
                            System.out.println(":block from index: " + block.getIndex());
                        } else {


                            break stop;
                        }
                    }

                    // Обновляем индекс для следующей итерации
                    currentIndex = startIndex - 1;
                }

            } catch (Exception e) {
                System.out.println("******************************");
                System.out.println("helpresolve5 1: address: " + s);
                e.printStackTrace();
                MyLogger.saveLog("helpResolve5:  926:: error", e);
                System.out.println("******************************");
                return temp;
            }

            System.out.println("different: ");
            if (different.isEmpty() && emptyList.isEmpty()) {
                MyLogger.saveLog("helpResolve5 different: " + different);
                MyLogger.saveLog("helpResolve5 emptyList: " + emptyList);
                return temp;
            }

            balances.putAll(UtilsAccountToEntityAccount.entityAccountsToMapAccounts(UtilsUse.accounts(different, blockService)));
            balances.putAll(UtilsAccountToEntityAccount.entityAccountsToMapAccounts(UtilsUse.accounts(emptyList, blockService)));
            balances.putAll(UtilsAccountToEntityAccount.entityAccountsToMapAccounts(UtilsUse.accounts(lastDiff, blockService)));
            tempBalance.putAll(UtilsAccountToEntityAccount.entityAccountsToMapAccounts(UtilsUse.accounts(different, blockService)));
            tempBalance.putAll(UtilsAccountToEntityAccount.entityAccountsToMapAccounts(UtilsUse.accounts(emptyList, blockService)));
            tempBalance.putAll(UtilsAccountToEntityAccount.entityAccountsToMapAccounts(UtilsUse.accounts(lastDiff, blockService)));


            System.out.println("shortDataBlockchain: " + BasisController.getShortDataBlockchain());
            System.out.println("rollback temp: " + temp);

            different = different.stream().sorted(Comparator.comparing(Block::getIndex)).collect(Collectors.toList());
            emptyList = emptyList.stream().sorted(Comparator.comparing(Block::getIndex)).collect(Collectors.toList());
            Block tempPrevBlock = UtilsBlockToEntityBlock.entityBlockToBlock(blockService.findBySpecialIndex(different.get(0).getIndex() - 1));

            MyLogger.saveLog("helpResolve5 before: rollBackShortCheck");
            temp = Blockchain.rollBackShortCheck(different, BasisController.getShortDataBlockchain(), tempBalance, sign);
            if (!emptyList.isEmpty()) {


                for (Block block : emptyList) {
                    List<Block> tempList = new ArrayList<>();
                    tempList.add(block);
                    temp = Blockchain.shortCheck(tempPrevBlock, tempList, temp, lastDiff, tempBalance, sign);
                    tempPrevBlock = block;
                }
            }
            MyLogger.saveLog("helpResolve5: temp: " + temp);
            //TODO проверка теперь будет происходит уже сразу и при скачивании.
            if (Seting.IS_SECURITY == true && checking && isSmall(global, temp)) {
                System.out.println("host: " + s);
                UtilsAllAddresses.saveAllAddresses(s, Seting.ORIGINAL_POOL_URL_ADDRESS_BLOCKED_FILE);
                temp.setValidation(false);
                return temp;
            }


            System.out.println("after rollback: " + temp);
            if (temp.isValidation()) {
                System.out.println("------------------------------------------");
                System.out.println("rollback 5");
                try {
                    MyLogger.saveLog("rollBackAddBlock4 before");
                    boolean result = rollBackAddBlock4(different, emptyList, balances, Seting.ORIGINAL_BLOCKCHAIN_FILE);
                    MyLogger.saveLog("rollBackAddBlock4 after: " + result);
                    if (result) {
                        BasisController.setShortDataBlockchain(temp);
                        BasisController.setBlockcheinSize((int) temp.getSize());
                        BasisController.setBlockchainValid(temp.isValidation());

                        EntityBlock tempBlock = blockService.findBySpecialIndex(BasisController.getBlockchainSize() - 1);

                        BasisController.setPrevBlock(UtilsBlockToEntityBlock.entityBlockToBlock(tempBlock));
                        String json = UtilsJson.objToStringJson(BasisController.getShortDataBlockchain());
                        UtilsFileSaveRead.save(json, Seting.TEMPORARY_BLOCKCHAIN_FILE, false);
                        return temp;
                    }


                    System.out.println("------------------------------------------");
                    System.out.println("emptyList: " + emptyList.size());

                    System.out.println("==========================================");
                    System.out.println("different start index: " + different.get(0).getIndex());
                    System.out.println("different finish index: " + different.get(different.size() - 1).getIndex());
                    System.out.println("------------------------------------------");
                } catch (Exception e) {
                    System.out.println("******************************");
                    System.out.println("helpresolve5: address: " + s);
                    e.printStackTrace();
                    MyLogger.saveLog("helpResolve5:  1033: ", e);
                    System.out.println("******************************");
                }
                System.out.println("------------------------------------------");
                System.out.println("helpResolve5: temp: " + temp);
                System.out.println("------------------------------------------");
            } else {

                return temp;
            }


        } else if (BasisController.getShortDataBlockchain().getSize() > 1 && temp.isValidation()) {
            //вызывает методы, для сохранения списка блоков в текущий блокчейн,
            //так же записывает в базу h2, делает перерасчет всех балансов,
            //и так же их записывает, а так же записывает другие данные.

            //TODO проверка теперь будет происходит уже сразу и при скачивании.
            if (Seting.IS_SECURITY == true && checking && isSmall(global, temp)) {
                System.out.println("host: " + s);
                UtilsAllAddresses.saveAllAddresses(s, Seting.ORIGINAL_POOL_URL_ADDRESS_BLOCKED_FILE);
                temp.setValidation(false);
                return temp;
            }

            boolean save = addBlock3(subBlocks, balances, Seting.ORIGINAL_BLOCKCHAIN_FILE);
            if (save) {
                BasisController.setShortDataBlockchain(temp);
                BasisController.setBlockcheinSize((int) temp.getSize());
                BasisController.setBlockchainValid(temp.isValidation());

                EntityBlock tempBlock = blockService.findBySpecialIndex(BasisController.getBlockchainSize() - 1);
                BasisController.setPrevBlock(UtilsBlockToEntityBlock.entityBlockToBlock(tempBlock));

                String json = UtilsJson.objToStringJson(BasisController.getShortDataBlockchain());
                UtilsFileSaveRead.save(json, Seting.TEMPORARY_BLOCKCHAIN_FILE, false);
                return temp;
            }

        }


        System.out.println("__________________________________________________________");

        return temp;
    }

    public DataShortBlockchainInformation helpResolve4(DataShortBlockchainInformation temp,
                                                       DataShortBlockchainInformation global,
                                                       String s,
                                                       List<Block> lastDiff,
                                                       Map<String, Account> tempBalances,
                                                       List<String> sign,
                                                       Map<String, Account> balances,
                                                       List<Block> subBlocks,
                                                       boolean checking)
            throws CloneNotSupportedException, IOException, NoSuchAlgorithmException, SignatureException,
            InvalidKeySpecException, NoSuchProviderException, InvalidKeyException {
        //TODO сначала найти блок откуда начинается ответление и докуда
        MyLogger.saveLog("start helpResolve4");
        Map<String, Account> tempBalance = UtilsUse.balancesClone(tempBalances);

        if (BasisController.getShortDataBlockchain().getSize() > 1 && !temp.isValidation()) {
            MyLogger.saveLog("helpResolve4 !temp.isValidation(): " + !temp.isValidation());
            System.out.println("__________________________________________________________");

            List<Block> emptyList = new ArrayList<>();
            List<Block> different = new ArrayList<>();

            int lastBlockIndex = (int) (global.getSize() - 1);
            int currentIndex = lastBlockIndex;
            try {

                stop:
                while (currentIndex >= 0) {

                    int startIndex = Math.max(currentIndex - 499, 0);
                    int endIndex = currentIndex;


                    SubBlockchainEntity subBlockchainEntity = new SubBlockchainEntity(startIndex, endIndex);
                    String subBlockchainJson = UtilsJson.objToStringJson(subBlockchainEntity);

                    List<Block> blockList = UtilsJson.jsonToObject(UtilUrl.getObject(subBlockchainJson, s + "/sub-blocks"));
                    System.out.println("subBlockchainEntity: " + subBlockchainEntity);

                    blockList = blockList.stream().sorted(Comparator.comparing(Block::getIndex).reversed()).collect(Collectors.toList());
                    for (Block block : blockList) {
                        System.out.println("helpResolve4:1136: block index: " + block.getIndex());

                        if (block.getIndex() > BasisController.getBlockchainSize() - 1) {
                            System.out.println("helpResolve4 :download blocks: " + block.getIndex() +
                                    " your block : " + (BasisController.getBlockchainSize()) + ":waiting need download blocks: " + (block.getIndex() - BasisController.getBlockchainSize())
                                    + " host: " + s);
                            emptyList.add(block);
                        } else if (!blockService.findBySpecialIndex(block.getIndex()).getHashBlock().equals(block.getHashBlock())) {
                            emptyList.add(block);
                            different.add(UtilsBlockToEntityBlock.entityBlockToBlock(blockService.findBySpecialIndex(block.getIndex())));
                            System.out.println("********************************");
                            System.out.println(":dowdnload block index: " + block.getIndex());
                            System.out.println(":block original index: " + blockService.findBySpecialIndex(block.getIndex()).getIndex());
                            System.out.println(":block from index: " + block.getIndex());
                        } else {
                            // Останавливаем итерацию, т.к. дальнейшие блоки будут идентичными


                            break stop;
                        }
                    }

                    // Обновляем индекс для следующей итерации
                    currentIndex = startIndex - 1;
                }

            } catch (Exception e) {
                System.out.println("******************************");
                System.out.println("connecting exeption helpresolve4: address: " + s);
                System.out.println("******************************");
                return temp;
            }

            System.out.println("different: ");
            if (different.isEmpty() && emptyList.isEmpty()) {
                MyLogger.saveLog("helpResolve4 different: " + different);
                MyLogger.saveLog("helpResolve4 emptyList: " + emptyList);
                return temp;
            }

            balances.putAll(UtilsAccountToEntityAccount.entityAccountsToMapAccounts(UtilsUse.accounts(different, blockService)));
            balances.putAll(UtilsAccountToEntityAccount.entityAccountsToMapAccounts(UtilsUse.accounts(emptyList, blockService)));
            balances.putAll(UtilsAccountToEntityAccount.entityAccountsToMapAccounts(UtilsUse.accounts(lastDiff, blockService)));
            tempBalance.putAll(UtilsAccountToEntityAccount.entityAccountsToMapAccounts(UtilsUse.accounts(different, blockService)));
            tempBalance.putAll(UtilsAccountToEntityAccount.entityAccountsToMapAccounts(UtilsUse.accounts(emptyList, blockService)));
            tempBalance.putAll(UtilsAccountToEntityAccount.entityAccountsToMapAccounts(UtilsUse.accounts(lastDiff, blockService)));
            System.out.println("shortDataBlockchain: " + BasisController.getShortDataBlockchain());
            System.out.println("rollback temp: " + temp);
            different = different.stream().sorted(Comparator.comparing(Block::getIndex)).collect(Collectors.toList());
            emptyList = emptyList.stream().sorted(Comparator.comparing(Block::getIndex)).collect(Collectors.toList());
            Block tempPrevBlock = UtilsBlockToEntityBlock.entityBlockToBlock(blockService.findBySpecialIndex(different.get(0).getIndex() - 1));

            MyLogger.saveLog("helpResolve4 before: rollBackShortCheck");
            temp = Blockchain.rollBackShortCheck(different, BasisController.getShortDataBlockchain(), tempBalance, sign);

            //TODO проверить если данные совпадают с ожидаемыми global, то произвести запись


            for (Block block : emptyList) {
                List<Block> tempList = new ArrayList<>();
                tempList.add(block);
                temp = Blockchain.shortCheck(tempPrevBlock, tempList, temp, lastDiff, tempBalance, sign);
                tempPrevBlock = block;
            }
            MyLogger.saveLog("helpResolve4: temp: " + temp);
            //TODO проверка теперь будет происходит уже сразу и при скачивании.
            if (Seting.IS_SECURITY == true && checking && isSmall(global, temp)) {

                MyLogger.saveLog("helpResolve4: isSmall(global, temp): " + isSmall(global, temp));
                MyLogger.saveLog("helpResolve4: global: " + global);
                MyLogger.saveLog("helpResolve4: temp: " + temp);
                if (different != null) {
                    MyLogger.saveLog("helpResolve4: different: 0 " + different.get(0).getIndex() + " hash: " + different.get(0).getHashBlock());
                    MyLogger.saveLog("helpResolve4: different: size-1 " + different.get(different.size() - 1).getIndex() +
                            " hash: " + different.get(different.size() - 1).getHashBlock());

                }
                if (emptyList != null) {
                    MyLogger.saveLog("helpResolve4: emptyList: 0 " + emptyList.get(0).getIndex() + " hash: " + emptyList.get(0).getHashBlock());
                    MyLogger.saveLog("helpResolve4: emptyList: size-1 " + emptyList.get(emptyList.size() - 1).getIndex() +
                            " hash: " + emptyList.get(emptyList.size() - 1).getHashBlock());

                }
                System.out.println("host: " + s);
                UtilsAllAddresses.saveAllAddresses(s, Seting.ORIGINAL_POOL_URL_ADDRESS_BLOCKED_FILE);
                temp.setValidation(false);

                return temp;
            }


            System.out.println("after rollback: " + temp);
            if (temp.isValidation()) {
                System.out.println("------------------------------------------");
                System.out.println("rollback");
                try {
                    MyLogger.saveLog("rollBackAddBlock3 before");
                    boolean result = rollBackAddBlock3(different, emptyList, balances, Seting.ORIGINAL_BLOCKCHAIN_FILE);
                    MyLogger.saveLog("rollBackAddBlock3 after: " + result);
                    if (result) {
                        BasisController.setShortDataBlockchain(temp);
                        BasisController.setBlockcheinSize((int) temp.getSize());
                        BasisController.setBlockchainValid(temp.isValidation());

                        EntityBlock tempBlock = blockService.findBySpecialIndex(BasisController.getBlockchainSize() - 1);


                        BasisController.setPrevBlock(UtilsBlockToEntityBlock.entityBlockToBlock(tempBlock));
                        String json = UtilsJson.objToStringJson(BasisController.getShortDataBlockchain());
                        UtilsFileSaveRead.save(json, Seting.TEMPORARY_BLOCKCHAIN_FILE, false);

                    }


                    System.out.println("------------------------------------------");
                    System.out.println("emptyList start index: " + emptyList.get(0).getIndex());
                    System.out.println("emptyList finish index: " + emptyList.get(emptyList.size() - 1).getIndex());
                    System.out.println("==========================================");
                    System.out.println("different start index: " + different.get(0).getIndex());
                    System.out.println("different finish index: " + different.get(different.size() - 1).getIndex());
                    System.out.println("------------------------------------------");
                } catch (Exception e) {
                    System.out.println("******************************");
                    System.out.println("elpresolve4: address: " + s);
                    e.printStackTrace();
                    MyLogger.saveLog("helpResolve4:  1274:error: ", e);

                    System.out.println("******************************");
                }
                System.out.println("------------------------------------------");
                System.out.println("helpResolve4: temp: " + temp);
                System.out.println("------------------------------------------");
            } else {

                MyLogger.saveLog("rollBackAddBlock3 finish: temp: " + temp);
                return temp;
            }


        } else if (BasisController.getShortDataBlockchain().getSize() > 1 && temp.isValidation()) {
            //вызывает методы, для сохранения списка блоков в текущий блокчейн,
            //так же записывает в базу h2, делает перерасчет всех балансов,
            //и так же их записывает, а так же записывает другие данные.

            //TODO проверка теперь будет происходит уже сразу и при скачивании.
            if (Seting.IS_SECURITY == true && checking && isSmall(global, temp)) {
                System.out.println("host: " + s);
                UtilsAllAddresses.saveAllAddresses(s, Seting.ORIGINAL_POOL_URL_ADDRESS_BLOCKED_FILE);
                temp.setValidation(false);
                return temp;
            }


            boolean save = addBlock3(subBlocks, balances, Seting.ORIGINAL_BLOCKCHAIN_FILE);
            if (save) {
                BasisController.setShortDataBlockchain(temp);
                BasisController.setBlockcheinSize((int) temp.getSize());
                BasisController.setBlockchainValid(temp.isValidation());

                EntityBlock tempBlock = blockService.findBySpecialIndex(BasisController.getBlockchainSize() - 1);
                BasisController.setPrevBlock(UtilsBlockToEntityBlock.entityBlockToBlock(tempBlock));

                String json = UtilsJson.objToStringJson(BasisController.getShortDataBlockchain());
                UtilsFileSaveRead.save(json, Seting.TEMPORARY_BLOCKCHAIN_FILE, false);
                return temp;
            }
        }


        System.out.println("__________________________________________________________");

        return temp;
    }


    public DataShortBlockchainInformation helpResolve3(DataShortBlockchainInformation temp,
                                                       DataShortBlockchainInformation global,
                                                       String s,
                                                       List<Block> lastDiff,
                                                       Map<String, Account> tempBalances,
                                                       List<String> sign,
                                                       Map<String, Account> balances,

                                                       List<Block> subBlocks) throws CloneNotSupportedException, IOException, NoSuchAlgorithmException, SignatureException, InvalidKeySpecException, NoSuchProviderException, InvalidKeyException {
        //TODO сначала найти блок откуда начинается ответление и докуда

        Map<String, Account> tempBalance = UtilsUse.balancesClone(tempBalances);
        if (BasisController.getShortDataBlockchain().getSize() > 1 && !temp.isValidation()) {
            System.out.println("__________________________________________________________");

            List<Block> emptyList = new ArrayList<>();
            List<Block> different = new ArrayList<>();


            for (int i = (int) (global.getSize() - 1); i >= 0; i--) {

                Block block = UtilsJson.jsonToBLock(UtilUrl.getObject(UtilsJson.objToStringJson(i), s + "/block"));

                System.out.println("helpResolve3: block index: " + block.getIndex());
                if (i > BasisController.getBlockchainSize() - 1) {
                    System.out.println(":download blocks: " + block.getIndex() +
                            " your block : " + (BasisController.getBlockchainSize()) + ":waiting need download blocks: " + (block.getIndex() - BasisController.getBlockchainSize()));
                    emptyList.add(block);

                } else if (!blockService.findBySpecialIndex(i).getHashBlock().equals(block.getHashBlock())) {
                    emptyList.add(block);
                    different.add(UtilsBlockToEntityBlock.entityBlockToBlock(blockService.findBySpecialIndex(i)));
                    System.out.println("********************************");
                    System.out.println(":dowdnload block index: " + i);
                    System.out.println(":block original index: " + blockService.findBySpecialIndex(i).getIndex());
                    System.out.println(":block from index: " + block.getIndex());

                } else {
//                    emptyList.add(block);
//                    different.add(UtilsBlockToEntityBlock.entityBlockToBlock(blockService.findBySpecialIndex(i)));

                    break;
                }
            }
            System.out.println("different: ");


            System.out.println("shortDataBlockchain: " + BasisController.getShortDataBlockchain());
            System.out.println("rollback temp: " + temp);

            different = different.stream().sorted(Comparator.comparing(Block::getIndex)).collect(Collectors.toList());
            emptyList = emptyList.stream().sorted(Comparator.comparing(Block::getIndex)).collect(Collectors.toList());
            Block tempPrevBlock = UtilsBlockToEntityBlock.entityBlockToBlock(blockService.findBySpecialIndex(different.get(0).getIndex() - 1));
            temp = Blockchain.rollBackShortCheck(different, BasisController.getShortDataBlockchain(), tempBalance, sign);

            for (Block block : emptyList) {
                List<Block> tempList = new ArrayList<>();
                tempList.add(block);
                temp = Blockchain.shortCheck(tempPrevBlock, tempList, temp, lastDiff, tempBalance, sign);
                tempPrevBlock = block;
            }

            System.out.println("after rollback: " + temp);
            if (temp.isValidation()) {
                System.out.println("------------------------------------------");
                System.out.println("rollback");
                try {
                    rollBackAddBlock3(different, emptyList, balances, Seting.ORIGINAL_BLOCKCHAIN_FILE);


                    System.out.println("------------------------------------------");
                    System.out.println("emptyList start index: " + emptyList.get(0).getIndex());
                    System.out.println("emptyList finish index: " + emptyList.get(emptyList.size() - 1).getIndex());
                    System.out.println("==========================================");
                    System.out.println("different start index: " + different.get(0).getIndex());
                    System.out.println("different finish index: " + different.get(different.size() - 1).getIndex());
                    System.out.println("------------------------------------------");
                } catch (Exception e) {
                    e.printStackTrace();
                }
                System.out.println("------------------------------------------");
                System.out.println("helpResolve3: temp: " + temp);
                System.out.println("------------------------------------------");
            } else {

                return temp;
            }


        } else if (BasisController.getShortDataBlockchain().getSize() > 1 && temp.isValidation()) {
            //вызывает методы, для сохранения списка блоков в текущий блокчейн,
            //так же записывает в базу h2, делает перерасчет всех балансов,
            //и так же их записывает, а так же записывает другие данные.
            boolean save = addBlock3(subBlocks, balances, Seting.ORIGINAL_BLOCKCHAIN_FILE);
            if (save) {
                BasisController.setShortDataBlockchain(temp);
                BasisController.setBlockcheinSize((int) temp.getSize());
                BasisController.setBlockchainValid(temp.isValidation());

                EntityBlock tempBlock = blockService.findBySpecialIndex(BasisController.getBlockchainSize() - 1);
                BasisController.setPrevBlock(UtilsBlockToEntityBlock.entityBlockToBlock(tempBlock));

                String json = UtilsJson.objToStringJson(BasisController.getShortDataBlockchain());
                UtilsFileSaveRead.save(json, Seting.TEMPORARY_BLOCKCHAIN_FILE, false);
                return temp;
            }
        }


        System.out.println("__________________________________________________________");
        temp.setValidation(false);
        return temp;
    }

    @Transactional
    public boolean rollBackAddBlock4(List<Block> deleteBlocks, List<Block> saveBlocks, Map<String, Account> balances, String filename) throws IOException, NoSuchAlgorithmException, InvalidKeySpecException, SignatureException, NoSuchProviderException, InvalidKeyException, CloneNotSupportedException {
    java.sql.Timestamp lastIndex = new java.sql.Timestamp(UtilsTime.getUniversalTimestamp());
    boolean existM = true;

    List<String> signs = new ArrayList<>();
    Map<String, Laws> allLaws = new HashMap<>();
    List<LawEligibleForParliamentaryApproval> allLawsWithBalance = new ArrayList<>();
    deleteBlocks = deleteBlocks.stream().sorted(Comparator.comparing(Block::getIndex)).collect(Collectors.toList());
    long threshold = deleteBlocks.get(0).getIndex();
    if(threshold <= 0 )
        return false;

    File file = Blockchain.indexNameFileBlock((int) threshold, filename);
    if (file == null) {
        System.out.println("rollBackAddBlock4 file:" + file.getAbsolutePath());
        MyLogger.saveLog("rollBackAddBlock4 file is null:");
        existM = false;
        return existM;
    }

    System.out.println("rollBackAddBlock4: file: " + file.getAbsolutePath());

    List<Block> tempBlock = new ArrayList<>();
    try (Stream<String> lines = Files.lines(file.toPath()).parallel()) {
        tempBlock = lines.map(line -> {
            try {
                return UtilsJson.jsonToBLock(line);
            } catch (JsonProcessingException e) {
                MyLogger.saveLog("rollBackAddBlock4 JSON processing error: " + e.getMessage());
                return null;
            }
        }).filter(block -> block != null && block.getIndex() < threshold)
          .sorted(Comparator.comparing(Block::getIndex))
          .collect(Collectors.toList());
    }

//    Map<String, Account> tempBalances = UtilsUse.balancesClone(balances);

    for (int i = deleteBlocks.size() - 1; i >= 0; i--) {
        Block block = deleteBlocks.get(i);
        System.out.println("rollBackAddBlock4 :BasisController: addBlock3: blockchain is being updated: index" + block.getIndex());

        balances = rollbackCalculateBalance(balances, block);
    }

//    tempBalances = UtilsUse.differentAccount(tempBalances, balances);
//    tempBalances = UtilsUse.merge(tempBalances, balances);
//    List<EntityAccount> accountList = blockService.findByAccountIn(balances);
//    accountList = UtilsUse.mergeAccounts(tempBalances, accountList);

    try {
        blockService.saveAccountAllF(UtilsAccountToEntityAccount.accountsToEntityAccounts(balances));
    }catch (Exception e){
        MyLogger.saveLog("error: rollBackAddBlock3: ", e);
        return false;
    }

    System.out.println("UtilsResolving: rollBackAddBlock4: total different balance: " + balances.size());
    System.out.println("UtilsResolving: rollBackAddBlock4: total original balance: " + balances.size());


    blockService.deleteEntityBlocksAndRelatedData(threshold);

    allLawsWithBalance = UtilsLaws.getCurrentLaws(allLaws, balances, Seting.ORIGINAL_ALL_CORPORATION_LAWS_WITH_BALANCE_FILE);

    Mining.deleteFiles(Seting.ORIGINAL_ALL_CORPORATION_LAWS_WITH_BALANCE_FILE);
    UtilsLaws.saveCurrentsLaws(allLawsWithBalance, Seting.ORIGINAL_ALL_CORPORATION_LAWS_WITH_BALANCE_FILE);

    java.sql.Timestamp actualTime = new java.sql.Timestamp(UtilsTime.getUniversalTimestamp());
    Long result = actualTime.toInstant().until(lastIndex.toInstant(), ChronoUnit.MILLIS);

    Blockchain.deleteFileBlockchain(Integer.parseInt(file.getName().replace(".txt", "")), Seting.ORIGINAL_BLOCKCHAIN_FILE);
    tempBlock = tempBlock.stream().sorted(Comparator.comparing(Block::getIndex)).collect(Collectors.toList());
    UtilsBlock.saveBlocks(tempBlock, filename);

    System.out.println("addBlock 3: time: result: " + result);
    System.out.println(":BasisController: addBlock3: finish: " + deleteBlocks.size());
    System.out.println("deleteBlocks: index: start: " + deleteBlocks.get(deleteBlocks.size() - 1).getIndex());
    System.out.println("tempBlock: index: start: " + tempBlock.get(0).getIndex());
    System.out.println("tempBlock: index: finish: " + tempBlock.get(tempBlock.size() - 1).getIndex());
    System.out.println("balances size: " + balances.size());

    if (!saveBlocks.isEmpty()) {
        boolean save = addBlock3(saveBlocks, balances, Seting.ORIGINAL_BLOCKCHAIN_FILE);
        if (!save) {
            existM = false;
        }
    }

    return existM;
}



    @Transactional
    public boolean rollBackAddBlock3(List<Block> deleteBlocks, List<Block> saveBlocks, Map<String, Account> balances, String filename) throws IOException, NoSuchAlgorithmException, InvalidKeySpecException, SignatureException, NoSuchProviderException, InvalidKeyException, CloneNotSupportedException {
        java.sql.Timestamp lastIndex = new java.sql.Timestamp(UtilsTime.getUniversalTimestamp());
        MyLogger.saveLog("rollBackAddBlock3 start");
        boolean existM = true;

        Map<String, Laws> allLaws = new HashMap<>();
        List<LawEligibleForParliamentaryApproval> allLawsWithBalance = new ArrayList<>();
        deleteBlocks = deleteBlocks.stream().sorted(Comparator.comparing(Block::getIndex)).collect(Collectors.toList());
        long threshold = deleteBlocks.get(0).getIndex();
        if(threshold <= 0 ){
            MyLogger.saveLog("threshold <= 0: " + threshold);
            return false;
        }

        File file = Blockchain.indexNameFileBlock((int) threshold, filename);

        if (file == null) {
            MyLogger.saveLog("rollBackAddBlock3 file is null: index: " + deleteBlocks.get(0).getIndex());
            existM = false;
            return existM;
        }

        MyLogger.saveLog("rollBackAddBlock3: synchronizedList: " + threshold);
        List<Block> tempBlock = Collections.synchronizedList(new ArrayList<>());
        try (Stream<String> lines = Files.lines(file.toPath())) {
            lines.parallel().forEach(line -> {
                try {
                    Block block = UtilsJson.jsonToBLock(line);
                    if (block.getIndex() < threshold) {
                        tempBlock.add(block);
                    }
                } catch (JsonProcessingException e) {
                    MyLogger.saveLog("rollBackAddBlock3 JSON processing error: " + e.getMessage());
                }
            });
        }

        MyLogger.saveLog("rollBackAddBlock3 before clone");
//        Map<String, Account> tempBalances = UtilsUse.balancesClone(balances);
        MyLogger.saveLog("rollBackAddBlock3 afer clone");
        //TODO именно в даной части кода происхдоит прерывание и полчему то в логер не записывается ошибка.
        //TODO но метод прекращается после этого участка.
        try {
            for (int i = deleteBlocks.size() - 1; i >= 0; i--) {
                MyLogger.saveLog("rollBackAddBlock3 index: " + i);
                Block block = deleteBlocks.get(i);
                MyLogger.saveLog("rollBackAddBlock3 block: index: " + block.getIndex());
                balances = rollbackCalculateBalance(balances, block);
                MyLogger.saveLog("rollBackAddBlock3 after: rollbackCalculateBalance");
            }
        }catch (Throwable  e){
            MyLogger.saveLog("rollBackAddBlock3: rollbackCalculateBalance: ", e);
            return false;
        }
        MyLogger.saveLog("rollBackAddBlock3: after rollbackCalculateBalance: ");
//        tempBalances = UtilsUse.differentAccount(tempBalances, balances);
//        tempBalances = UtilsUse.merge(tempBalances, balances);
        MyLogger.saveLog("rollBackAddBlock3: after: differentAccount:");
        List<EntityAccount> accountList = null;
        try {
//            accountList = blockService.findByAccountIn(balances);
//            accountList = UtilsUse.mergeAccounts(tempBalances, accountList);
            blockService.saveAccountAllF(UtilsAccountToEntityAccount.accountsToEntityAccounts(balances));

        }catch (Exception e){
            MyLogger.saveLog("error: rollBackAddBlock3: ", e);
            return false;
        }
        MyLogger.saveLog("rollBackAddBlock3: before: deleteEntityBlocksAndRelatedData:");
        blockService.deleteEntityBlocksAndRelatedData(threshold);
        allLawsWithBalance = UtilsLaws.getCurrentLaws(allLaws, balances, Seting.ORIGINAL_ALL_CORPORATION_LAWS_WITH_BALANCE_FILE);
        Mining.deleteFiles(Seting.ORIGINAL_ALL_CORPORATION_LAWS_WITH_BALANCE_FILE);
        UtilsLaws.saveCurrentsLaws(allLawsWithBalance, Seting.ORIGINAL_ALL_CORPORATION_LAWS_WITH_BALANCE_FILE);
        MyLogger.saveLog("rollBackAddBlock3: before: deleteFileBlockchain:");
        Blockchain.deleteFileBlockchain(Integer.parseInt(file.getName().replace(".txt", "")), Seting.ORIGINAL_BLOCKCHAIN_FILE);
        UtilsBlock.saveBlocks(tempBlock, filename);
        MyLogger.saveLog("rollBackAddBlock3: after: saveBlocks:");
        boolean save = addBlock3(saveBlocks, balances, Seting.ORIGINAL_BLOCKCHAIN_FILE);
        if (!save) {
            existM = false;
        }
        MyLogger.saveLog("rollBackAddBlock3 finish");
        return existM;
    }





    public int resovle2() throws IOException, NoSuchAlgorithmException, InvalidKeySpecException, SignatureException, NoSuchProviderException, InvalidKeyException {

        BasisController.setUpdating(true);
        int bigSize = 0;
        try {
            System.out.println(" :start resolve");
            utilsMethod();

//            blockchainSize = (int) shortDataBlockchain.getSize();

            //local blockchain size
            //размер локального блокчейна
            int blocks_current_size = BasisController.getBlockchainSize();

            System.out.println(" resolve2:local size: " + blocks_current_size);

            //адреса узлов.
            Set<String> nodesAll = getNodes();
            //сортирует по приоритетности блокчейны
            List<HostEndDataShortB> sortPriorityHost = sortPriorityHost(nodesAll);
            System.out.println(":resolve2: size nodes: " + getNodes().size());
            //goes through all hosts (repositories) in search of the most up-to-date blockchain
            //проходит по всем хостам(хранилищам) в поисках самого актуального блокчейна
            for (HostEndDataShortB hostEndDataShortB : sortPriorityHost) {
                String s = hostEndDataShortB.getHost();
                //if the local address matches the host address, it skips
                //если локальный адрес совпадает с адресом хоста, он пропускает
                if (BasisController.getExcludedAddresses().contains(s)) {
                    System.out.println(":its your address or excluded address: " + s);
                    continue;
                }
                try {
                    //if the address is localhost, it skips
                    //если адрес локального хоста, он пропускает
                    if (Seting.IS_TEST == false && (s.contains("localhost") || s.contains("127.0.0.1")))
                        continue;
                    String sizeStr = UtilUrl.readJsonFromUrl(s + "/size");
                    Integer size = Integer.valueOf(sizeStr);

                    //здесь устанавливает самый длинный блокчейн.
                    if (size > bigSize) {
                        bigSize = size;
                    }
                    //if the size from the storage is larger than on the local server, start checking
                    //если размер с хранилища больше чем на локальном сервере, начать проверку
                    System.out.println("resolve2 size: " + size + " blocks_current_size: " + blocks_current_size);
                    String jsonGlobalData = UtilUrl.readJsonFromUrl(s + "/datashort");
                    System.out.println("jsonGlobalData: " + jsonGlobalData);

                    DataShortBlockchainInformation global = UtilsJson.jsonToDataShortBlockchainInformation(jsonGlobalData);
                    if (isBig(BasisController.getShortDataBlockchain(), global)) {
                        System.out.println(":size from address: " + s + " upper than: " + size + ":blocks_current_size " + blocks_current_size);
                        //Test start algorithm
                        //600 последних блоков, для подсчета сложности, для последнего блока.
                        List<Block> lastDiff = new ArrayList<>();
                        SubBlockchainEntity subBlockchainEntity = null;
                        String subBlockchainJson = null;
//                        Map<String, Account> balances = SaveBalances.readLineObject(Seting.ORIGINAL_BALANCE_FILE);
                        Map<String, Account> balances = UtilsAccountToEntityAccount.entityAccountsToMapAccounts(blockService.findAllAccounts());
//                        Map<String, Account> tempBalances = SaveBalances.readLineObject(Seting.ORIGINAL_BALANCE_FILE);
                        Map<String, Account> tempBalances = UtilsUse.balancesClone(balances);

                        //if the local one lags behind the global one by more than PORTION_DOWNLOAD (500 blocks), then you need to download in portions from the storage
                        //если локальный отстает от глобального больше чем PORTION_DOWNLOAD (500 блоков), то нужно скачивать порциями из хранилища
                        if (size - blocks_current_size > Seting.PORTION_DOWNLOAD) {
                            boolean downloadPortion = true;
                            int finish = blocks_current_size + Seting.PORTION_DOWNLOAD;
                            int start = blocks_current_size;
                            //while the difference in the size of the local blockchain is greater than from the host, it will continue to download in portions to download the entire blockchain
                            //пока разница размера локального блокчейна больше чем с хоста будет продолжаться скачивать порциями, чтобы скачать весь блокчейн
                            while (downloadPortion) {
                                //здесь говориться, с какого блока по какой блок скачивать.
                                subBlockchainEntity = new SubBlockchainEntity(start, finish);

                                System.out.println("1:shortDataBlockchain:  " + BasisController.getShortDataBlockchain());
                                System.out.println("1:sublockchainEntity: " + subBlockchainEntity);
                                subBlockchainJson = UtilsJson.objToStringJson(subBlockchainEntity);
                                System.out.println("1:sublockchainJson: " + subBlockchainJson);
                                List<Block> subBlocks = UtilsJson.jsonToObject(UtilUrl.getObject(subBlockchainJson, s + "/sub-blocks"));
                                System.out.println("1:download sub block: " + subBlocks.size());

                                finish = (int) subBlocks.get(subBlocks.size() - 1).getIndex() + Seting.PORTION_DOWNLOAD + 1;
                                start = (int) subBlocks.get(subBlocks.size() - 1).getIndex() + 1; //вот здесь возможно сделать + 2


//                                balances = SaveBalances.readLineObject(Seting.ORIGINAL_BALANCE_FILE);
                                balances = UtilsAccountToEntityAccount.entityAccountsToMapAccounts(blockService.findAllAccounts());

                                //вычисляет сложность блока, для текущего блока, на основе предыдущих блоков.
                                //select a block class for the current block, based on previous blocks.
                                if (BasisController.getBlockchainSize() > Seting.PORTION_BLOCK_TO_COMPLEXCITY) {
                                    lastDiff = UtilsBlockToEntityBlock.entityBlocksToBlocks(
                                            blockService.findBySpecialIndexBetween(
                                                    (BasisController.prevBlock().getIndex() + 1) - Seting.PORTION_BLOCK_TO_COMPLEXCITY,
                                                    BasisController.prevBlock().getIndex() + 1
                                            )
                                    );
                                }


                                //класс мета данных блокчейна.
                                DataShortBlockchainInformation temp = new DataShortBlockchainInformation();

                                //загружает баланс всех счетов для текущего блокчейна.
                                List<String> sign = new ArrayList<>();
                                if (BasisController.getBlockchainSize() > 1) {
                                    //проверяет скаченные блоки на целостность
                                    //checks downloaded blocks for integrity
                                    temp = Blockchain.shortCheck(
                                            BasisController.prevBlock(),
                                            subBlocks, BasisController.getShortDataBlockchain(), lastDiff, tempBalances, sign);
                                    System.out.println("prevBlock: " + BasisController.prevBlock().getIndex());
                                }


                                System.out.println("++++++++++++++++++++++++++++++++++++++++++++++++");
                                System.out.println("1: temp: " + temp);
                                System.out.println("1: blockchainsize: " + BasisController.getBlockchainSize());
                                System.out.println("1: sublocks: " + subBlocks.size());
                                System.out.println("1: shortDataBlockchain: " + BasisController.getShortDataBlockchain());

                                System.out.println("++++++++++++++++++++++++++++++++++++++++++++++++");
                                //если скачанный блокчейн не валидный, то не добавляет в блокчейн, возвращает -10
                                if (BasisController.getBlockchainSize() > 1 && !temp.isValidation()) {
                                    System.out.println("error resolve 2 in portion upper > 500");
                                    return -10;
                                }

                                //вызывает методы, для сохранения списка блоков в текущий блокчейн,
                                //так же записывает в базу h2, делает перерасчет всех балансов,
                                //и так же их записывает, а так же записывает другие данные.
                                addBlock3(subBlocks, balances, Seting.ORIGINAL_BLOCKCHAIN_FILE);

                                if (!temp.isValidation()) {
                                    System.out.println("check all file");
                                    //проверить целостность блокчейна всего на кошельке
                                    //check the integrity of the blockchain of everything on the wallet
                                    temp = Blockchain.checkFromFile(Seting.ORIGINAL_BLOCKCHAIN_FILE);
                                }

                                //добавляет мета данные блокчейна в static переменную, как так
                                //уже эти мета данные являются актуальными.
                                //adds capacitor metadata to a static variable like so
                                //this metadata is already relevant.
                                BasisController.setShortDataBlockchain(temp);
                                //размер блокчейна в кошельке.
                                //the size of the blockchain in the wallet.

                                BasisController.setBlockcheinSize((int) BasisController.getShortDataBlockchain().getSize());
                                //валидность блокчейна в кошельке.
                                //validity of the blockchain in the wallet.
                                BasisController.setBlockchainValid(BasisController.getShortDataBlockchain().isValidation());
//
                                //получить последний блок из базы данных.
                                //get the last block from the database.
                                EntityBlock tempBlock = blockService.findBySpecialIndex(BasisController.getBlockchainSize() - 1);
                                //последний блок в локальном сервере.
                                //last block in the local server.
                                BasisController.setPrevBlock(UtilsBlockToEntityBlock.entityBlockToBlock(tempBlock));
                                System.out.println("prevBlock: " + BasisController.prevBlock().getIndex() + " shortDataBlockchain: " + BasisController.getShortDataBlockchain());
                                String json = UtilsJson.objToStringJson(BasisController.getShortDataBlockchain());
                                //сохранить мета данные блокчейна.
                                UtilsFileSaveRead.save(json, Seting.TEMPORARY_BLOCKCHAIN_FILE, false);

                                //если количество новых блоков, относительно локального блокчейна меньше 500,
                                //то скачать эти блоки и прекратить попытки скачивания с данного узла.
                                //if the number of new blocks relative to the local blockchain is less than 500,
                                //then download these blocks and stop trying to download from this node.
                                if (size - BasisController.prevBlock().getIndex() < Seting.PORTION_DOWNLOAD) {
                                    downloadPortion = false;
                                    finish = size;
                                    subBlockchainEntity = new SubBlockchainEntity(start, finish);
                                    System.out.println("2:sublockchainEntity: " + subBlockchainEntity);
                                    subBlockchainJson = UtilsJson.objToStringJson(subBlockchainEntity);
                                    System.out.println("2:sublockchainJson: " + subBlockchainJson);
                                    subBlocks = UtilsJson.jsonToObject(UtilUrl.getObject(subBlockchainJson, s + "/sub-blocks"));
                                    System.out.println("2:download sub block: " + subBlocks.size());

//                                    balances = SaveBalances.readLineObject(Seting.ORIGINAL_BALANCE_FILE);
                                    balances = UtilsAccountToEntityAccount.entityAccountsToMapAccounts(blockService.findAllAccounts());
                                    if (BasisController.getBlockchainSize() > Seting.PORTION_BLOCK_TO_COMPLEXCITY) {
                                        lastDiff = UtilsBlockToEntityBlock.entityBlocksToBlocks(
                                                blockService.findBySpecialIndexBetween(
                                                        (BasisController.prevBlock().getIndex() + 1) - Seting.PORTION_BLOCK_TO_COMPLEXCITY,
                                                        BasisController.prevBlock().getIndex() + 1
                                                )
                                        );
                                    }

                                    if (BasisController.getBlockchainSize() > 1) {
                                        temp = Blockchain.shortCheck(BasisController.prevBlock(), subBlocks, BasisController.getShortDataBlockchain(), lastDiff, tempBalances, sign);
                                    }


                                    System.out.println("2: temp: " + temp);
                                    System.out.println("2: blockchainsize: " + BasisController.getBlockchainSize());
                                    System.out.println("2: sublocks: " + subBlocks.size());

                                    if (BasisController.getBlockchainSize() > 1 && !temp.isValidation()) {
                                        return -10;
                                    }

                                    //метод, который сохраняет скачанные порции блока, в блокчейн
                                    addBlock3(subBlocks, balances, Seting.ORIGINAL_BLOCKCHAIN_FILE);
                                    if (!temp.isValidation()) {
                                        temp = Blockchain.checkFromFile(Seting.ORIGINAL_BLOCKCHAIN_FILE);
                                    }
                                    BasisController.setShortDataBlockchain(temp);
                                    BasisController.setBlockcheinSize((int) BasisController.getShortDataBlockchain().getSize());
                                    BasisController.setBlockchainValid(BasisController.getShortDataBlockchain().isValidation());
//
                                    tempBlock = blockService.findBySpecialIndex(BasisController.getBlockchainSize() - 1);
                                    BasisController.setPrevBlock(UtilsBlockToEntityBlock.entityBlockToBlock(tempBlock));

                                    json = UtilsJson.objToStringJson(BasisController.getShortDataBlockchain());
                                    UtilsFileSaveRead.save(json, Seting.TEMPORARY_BLOCKCHAIN_FILE, false);
                                }
                            }
                        } else {

                            //здесь нужно проверить
                            //If the difference is not greater than PORTION_DOWNLOAD, then downloads once a portion of this difference
                            //Если разница не больше PORTION_DOWNLOAD, то скачивает один раз порцию эту разницу
                            subBlockchainEntity = new SubBlockchainEntity(blocks_current_size, size);

                            subBlockchainJson = UtilsJson.objToStringJson(subBlockchainEntity);

                            String str = UtilUrl.getObject(subBlockchainJson, s + "/sub-blocks");
                            if (str.isEmpty() || str.isBlank()) {
                                System.out.println("-------------------------------------");
                                System.out.println("sublocks:  str: empty " + str);
                                System.out.println("-------------------------------------");
                                continue;
                            }
                            List<Block> subBlocks = UtilsJson.jsonToObject(str);

                            if (subBlocks.isEmpty() || subBlocks.size() == 0) {
                                System.out.println("-------------------------------------");
                                System.out.println("sublocks: " + subBlocks.size());
                                System.out.println("-------------------------------------");
                                continue;
                            }
                            System.out.println("3:download sub block: " + subBlocks.size());
//                            tempBalances = SaveBalances.readLineObject(Seting.ORIGINAL_BALANCE_FILE);
                            tempBalances = UtilsAccountToEntityAccount.entityAccountsToMapAccounts(blockService.findAllAccounts());
                            List<String> sign = new ArrayList<>();

                            if (BasisController.getBlockchainSize() > Seting.PORTION_BLOCK_TO_COMPLEXCITY) {
                                lastDiff = UtilsBlockToEntityBlock.entityBlocksToBlocks(
                                        blockService.findBySpecialIndexBetween(
                                                (BasisController.prevBlock().getIndex() + 1) - Seting.PORTION_BLOCK_TO_COMPLEXCITY,
                                                BasisController.prevBlock().getIndex() + 1
                                        )
                                );
                            }

                            DataShortBlockchainInformation temp = new DataShortBlockchainInformation();
                            if (BasisController.getBlockchainSize() > 1) {
                                temp = Blockchain.shortCheck(BasisController.prevBlock(), subBlocks, BasisController.getShortDataBlockchain(), lastDiff, tempBalances, sign);
                            }

                            System.out.println("3: temp: " + temp);
                            System.out.println("3: blockchainsize: " + BasisController.getBlockchainSize());
                            System.out.println("3: sublocks: " + subBlocks.size());

                            if (temp.getSize() > 1 && !temp.isValidation()) {
                                System.out.println("error resolve 2 in portion upper < 500");

                                return -10;
                            }

                            addBlock3(subBlocks, balances, Seting.ORIGINAL_BLOCKCHAIN_FILE);
                            if (!temp.isValidation()) {
                                System.out.println("check all file");
                                temp = Blockchain.checkFromFile(Seting.ORIGINAL_BLOCKCHAIN_FILE);
                            }

                            BasisController.setShortDataBlockchain(temp);
                            BasisController.setBlockcheinSize((int) BasisController.getShortDataBlockchain().getSize());
                            BasisController.setBlockchainValid(BasisController.getShortDataBlockchain().isValidation());

//                            prevBlock = Blockchain.indexFromFile(blockchainSize - 1, Seting.ORIGINAL_BLOCKCHAIN_FILE);
                            EntityBlock tempBlock = blockService.findBySpecialIndex(BasisController.getBlockchainSize() - 1);
                            BasisController.setPrevBlock(UtilsBlockToEntityBlock.entityBlockToBlock(tempBlock));

                            String json = UtilsJson.objToStringJson(BasisController.getShortDataBlockchain());
                            UtilsFileSaveRead.save(json, Seting.TEMPORARY_BLOCKCHAIN_FILE, false);
                        }
                        System.out.println("size temporaryBlockchain: ");
                        System.out.println("resolve: temporaryBlockchain: ");
                    } else {
                        System.out.println(":BasisController: resove: size less: " + size + " address: " + s);
                        continue;
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    System.out.println("BasisController: resove2: " + e.getMessage());
                    continue;
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                } catch (CloneNotSupportedException e) {
                    throw new RuntimeException(e);
                }
            }

        } finally {

            BasisController.setUpdating(false);
            if (BasisController.getBlockchainSize() > bigSize) {
                return 1;
            } else if (BasisController.getBlockchainSize() < bigSize) {
                return -1;
            } else {
                return 0;
            }

        }


    }

    /**
     * rewrites the blockchain into files and into the h2 database. From here they are called
     * * methods that calculate balance and other calculations.
     * производит перезапись блокчейна в файлы и в базу h2. Отсюда вызываются
     * методы которые, вычисляют баланс и другие вычисления.
     */


    @Transactional
    public boolean addBlock3(List<Block> originalBlocks, Map<String, Account> balances, String filename) throws IOException, NoSuchAlgorithmException, InvalidKeySpecException, SignatureException, NoSuchProviderException, InvalidKeyException, CloneNotSupportedException {
        java.sql.Timestamp lastIndex = new java.sql.Timestamp(UtilsTime.getUniversalTimestamp());
        UtilsBalance.setBlockService(blockService);
        Blockchain.setBlockService(blockService);
        UtilsBlock.setBlockService(blockService);
        List<EntityBlock> list = new ArrayList<>();
        List<String> signs = new ArrayList<>();
        Map<String, Laws> allLaws = new HashMap<>();
        List<LawEligibleForParliamentaryApproval> allLawsWithBalance = new ArrayList<>();

        originalBlocks = originalBlocks.stream().sorted(Comparator.comparing(Block::getIndex)).collect(Collectors.toList());

        Map<String, Account> tempBalances = UtilsUse.balancesClone(balances);
        long start = UtilsTime.getUniversalTimestamp();
        for (Block block : originalBlocks) {
            System.out.println(" :BasisController: addBlock3: blockchain is being updated: index" + block.getIndex());

            EntityBlock entityBlock = UtilsBlockToEntityBlock.blockToEntityBlock(block);
            list.add(entityBlock);

            calculateBalance(balances, block, signs);
//            UtilsBlock.saveBLock(block, filename);
        }

        list = list.stream().sorted(Comparator.comparing(EntityBlock::getSpecialIndex)).collect(Collectors.toList());
        // Вызов getLaws один раз для всех блоков

        long finish = UtilsTime.getUniversalTimestamp();
        System.out.println("UtilsResolving: addBlock3: for: time different: " + UtilsTime.differentMillSecondTime(start, finish));
        try {
            blockService.saveAllBLockF(list);

            tempBalances = UtilsUse.differentAccount(tempBalances, balances);
            List<EntityAccount> accountList = blockService.findByAccountIn(tempBalances);
            accountList = UtilsUse.mergeAccounts(tempBalances, accountList);

            start = UtilsTime.getUniversalTimestamp();
            blockService.saveAccountAllF(accountList);
            finish = UtilsTime.getUniversalTimestamp();
        }catch (Exception e){
            MyLogger.saveLog("addBlock3: error: ", e);
            String stackerror = "";
            for (StackTraceElement stackTraceElement : e.getStackTrace()) {
                stackerror += stackTraceElement.toString() + "\n";
            }
            MyLogger.saveLog("addBlock3: error: " + stackerror);
            return false;

        }

        System.out.println("UtilsResolving: addBlock3: time save accounts: " + UtilsTime.differentMillSecondTime(start, finish));
        System.out.println("UtilsResolving: addBlock3: total different balance: " + tempBalances.size());
        System.out.println("UtilsResolving: addBlock3: total original balance: " + balances.size());

        UtilsBlock.saveBlocks(originalBlocks, filename);
        allLaws = UtilsLaws.getLaws(originalBlocks, Seting.ORIGINAL_ALL_CORPORATION_LAWS_FILE, allLaws);
        allLawsWithBalance = UtilsLaws.getCurrentLaws(allLaws, balances, Seting.ORIGINAL_ALL_CORPORATION_LAWS_WITH_BALANCE_FILE);

        Mining.deleteFiles(Seting.ORIGINAL_ALL_CORPORATION_LAWS_WITH_BALANCE_FILE);
        UtilsLaws.saveCurrentsLaws(allLawsWithBalance, Seting.ORIGINAL_ALL_CORPORATION_LAWS_WITH_BALANCE_FILE);

        java.sql.Timestamp actualTime = new java.sql.Timestamp(UtilsTime.getUniversalTimestamp());
        Long result = actualTime.toInstant().until(lastIndex.toInstant(), ChronoUnit.MILLIS);
        System.out.println("addBlock 3: time: result: " + result);
        System.out.println(":BasisController: addBlock3: finish: " + originalBlocks.size());
        return true;
    }


    public List<HostEndDataShortB> sortPriorityHostOriginal(Set<String> hosts) throws IOException, JSONException {
        List<HostEndDataShortB> list = new ArrayList<>();
        for (String s : hosts) {
            try {
                String jsonGlobalData = UtilUrl.readJsonFromUrl(s + "/datashort");
                System.out.println("jsonGlobalData: " + jsonGlobalData);
                DataShortBlockchainInformation global = UtilsJson.jsonToDataShortBlockchainInformation(jsonGlobalData);
                if (global.isValidation()) {
                    HostEndDataShortB dataShortB = new HostEndDataShortB(s, global);
                    list.add(dataShortB);
                }
            } catch (Exception e) {
                e.printStackTrace();
                continue;
            }


        }

        //сортировать здесь.
        // сортировка
        Collections.sort(list, new HostEndDataShortBComparator());
        return list;
    }

    /**
     * Записывает Блоки и баланс во временный файл.
     */

    public boolean isRightDSB(DataShortBlockchainInformation actual, DataShortBlockchainInformation expected) {
        boolean result = true;
        if (actual.isValidation() != expected.isValidation()
                || actual.getSize() < expected.getSize()
                || actual.getTransactions() < expected.getTransactions()
                || actual.getStaking() < expected.getStaking()
                || actual.getBigRandomNumber() < expected.getBigRandomNumber()
                || actual.getHashCount() < expected.getHashCount()) {
            result = false;
        }
        return result;
    }

    public List<HostEndDataShortB> sortPriorityHost(Set<String> hosts) {

        // Добавляем ORIGINAL_ADDRESSES к входящему набору хостов
        Set<String> modifiedHosts = new HashSet<>(hosts);
        modifiedHosts.addAll(Seting.ORIGINAL_ADDRESSES);

        // Отбираем случайные 10 хостов
        Set<String> selectedHosts = modifiedHosts.stream()
                .collect(Collectors.collectingAndThen(
                        Collectors.toList(),
                        listHost -> {
                            Collections.shuffle(listHost);
                            return listHost.stream().limit(RANDOM_HOSTS).collect(Collectors.toSet());
                        }
                ));


        List<CompletableFuture<HostEndDataShortB>> futures = new ArrayList<>(); // Список для хранения CompletableFuture

        // Вывод информации о начале метода
        System.out.println("start: sortPriorityHost: " + selectedHosts);

        // Перебираем все хосты
        for (String host : selectedHosts) {
            // Создаем CompletableFuture для каждого хоста
            CompletableFuture<HostEndDataShortB> future = CompletableFuture.supplyAsync(() -> {
                try {
                    // Вызов метода для получения данных из источника
                    DataShortBlockchainInformation global = fetchDataShortBlockchainInformation(host);
                    // Если данные действительны, создаем объект HostEndDataShortB
                    if (global != null && global.isValidation()) {
                        return new HostEndDataShortB(host, global);
                    }
                } catch (IOException | JSONException e) {
                    // Перехват и логирование ошибки
                    logError("Error while retrieving data for host: " + host, e);

                }
                return null;
            });

            // Добавление CompletableFuture в список
            futures.add(future);
        }

        // Получение CompletableFuture, которые будут завершены
        CompletableFuture<Void> allFutures = CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));

        // Создание CompletableFuture для обработки завершенных результатов
        CompletableFuture<List<HostEndDataShortB>> allComplete = allFutures.thenApplyAsync(result -> {
            // Получение результатов из CompletableFuture, фильтрация недействительных результатов и сборка в список
            return futures.stream()
                    .map(CompletableFuture::join)
                    .filter(result1 -> result1 != null)
                    .collect(Collectors.toList());
        });

        // Получение итогового списка
        List<HostEndDataShortB> resultList = allComplete.join();

        // Сортировка списка по приоритету
        Collections.sort(resultList, new HostEndDataShortBComparator());

        // Вывод информации о завершении метода
        System.out.println("finish: sortPriorityHost: " + resultList);

        // Возвращение итогового списка
        return resultList;
    }

    // Метод для получения данных из источника
    private DataShortBlockchainInformation fetchDataShortBlockchainInformation(String host) throws IOException, JSONException {
        // Загрузка JSON данных с URL
        String jsonGlobalData = UtilUrl.readJsonFromUrl(host + "/datashort");
        // Вывод загруженных данных
        System.out.println("jsonGlobalData: " + jsonGlobalData);
        // Преобразование JSON данных в объект
        return UtilsJson.jsonToDataShortBlockchainInformation(jsonGlobalData);
    }

    //скачивает хосты из других узлов
    public Set<String> newHosts(String host) throws JSONException, IOException {
        String addresses = UtilUrl.readJsonFromUrl(host + "/getNodes");
        // Вывод загруженных данных
        System.out.println("jsonGlobalData: " + addresses);
        // Преобразование JSON данных в объект
        return UtilsJson.jsonToSetAddresses(addresses);
    }

    public Set<String> newHostsLoop(Set<String> hosts) throws JSONException, IOException {
        Set<String> addresses = new HashSet<>();
        for (String s : hosts) {
            addresses.addAll(newHosts(s));
        }
        return addresses;
    }

    // Метод для логирования ошибки
    private void logError(String message, Exception e) {
        // Вывод ошибки и сообщения
        System.out.println("-----------------------------------");
        System.out.println("Ошибка: " + message);
        // Вывод стека вызовов исключения
        if (e != null) {
            e.printStackTrace();
        }
        // Завершение логирования
        System.out.println("-----------------------------------");
    }

    public int sendAllBlocksToStorage(List<Block> blocks) throws IOException, NoSuchAlgorithmException, InvalidKeySpecException, SignatureException, NoSuchProviderException, InvalidKeyException {
        System.out.println(new Date() + ":BasisController: sendAllBlocksToStorage: start: ");
        int blocksCurrentSize = (int) blocks.get(blocks.size() - 1).getIndex() + 1;
        System.out.println(":BasisController: sendAllBlocksToStorage: ");
        Set<String> nodesAll = getNodes();
        List<HostEndDataShortB> sortPriorityHost = sortPriorityHost(nodesAll);

        ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
        List<CompletableFuture<Void>> futures = new ArrayList<>();

        for (HostEndDataShortB hostEndDataShortB : sortPriorityHost) {
            String s = hostEndDataShortB.getHost();
            if (BasisController.getExcludedAddresses().contains(s)) {
                System.out.println(":its your address or excluded address: " + s);
                continue;
            }

            CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                try {
                    System.out.println(":BasisController:resolve conflicts: address: " + s + "/size");
                    String sizeStr = UtilUrl.readJsonFromUrl(s + "/size");
                    Integer size = Integer.valueOf(sizeStr);
                    System.out.println(":BasisController: send: local size: " + blocksCurrentSize + " global size: " + size);

                    List<Block> fromToTempBlock = new ArrayList<>(blocks);
                    SendBlocksEndInfo infoBlocks = new SendBlocksEndInfo(Seting.VERSION, fromToTempBlock);
                    String jsonFromTo = UtilsJson.objToStringJson(infoBlocks);

                    String urlFrom = s + "/nodes/resolve_from_to_block";
                    int response = UtilUrl.sendPost(jsonFromTo, urlFrom);
                    System.out.println(":response: " + response + " address: " + s);
                    MyLogger.saveLog("sendBlock: response: " + response + " address: " + s);
                } catch (Exception e) {
                    e.printStackTrace();
                    System.out.println(":exception resolve_from_to_block: " + s);
                }
            }, executor);
            futures.add(future);
        }

        CompletableFuture<Void> allFutures = CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));
        allFutures.join();
        executor.shutdown();

        int bigsize = 0; // Возможно, вам нужно изменить способ определения значения bigsize
        if (BasisController.getBlockchainSize() > bigsize) {
            return 1;
        } else if (BasisController.getBlockchainSize() < bigsize) {
            return -1;
        } else if (BasisController.getBlockchainSize() == bigsize) {
            return 0;
        } else {
            return -4;
        }
    }


}

