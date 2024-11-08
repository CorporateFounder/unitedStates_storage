package International_Trade_Union.model;

import International_Trade_Union.controllers.BasisController;
import International_Trade_Union.controllers.NodeController;
import International_Trade_Union.entity.DtoTransaction.DtoTransaction;
import International_Trade_Union.entity.blockchain.Blockchain;
import International_Trade_Union.entity.blockchain.DataShortBlockchainInformation;
import International_Trade_Union.entity.blockchain.block.Block;
import International_Trade_Union.entity.entities.EntityBlock;
import International_Trade_Union.entity.services.BlockService;
import International_Trade_Union.logger.MyLogger;
import International_Trade_Union.setings.Seting;
import International_Trade_Union.utils.*;
import org.json.JSONException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.transaction.Transactional;
import java.io.IOException;
import java.math.BigDecimal;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.util.*;
import java.util.concurrent.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static International_Trade_Union.controllers.BasisController.getNodes;
import static International_Trade_Union.utils.UtilsUse.bigRandomWinner;

@Component
@Scope("singleton")
public class TournamentService {
    @PostConstruct
    public void init() {
        Blockchain.setBlockService(blockService);
        UtilsBalance.setBlockService(blockService);
        UtilsBlock.setBlockService(blockService);
    }

    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    @Autowired
    NodeChecker nodeChecker;
    @Autowired
    UtilsTransactions utilsTransactions;

    @Autowired
    UtilsResolving utilsResolving;

    @Autowired
    BlockService blockService;

    @Autowired
    DomainConfiguration domainConfiguration;

    private List<Block> winnerDiff = new ArrayList<>();
    private List<Block> winnerCountTransaction = new ArrayList<>();
    private List<Block> winnerStaking = new ArrayList<>();
    private List<Block> winner = new ArrayList<>();

    public List<Block> getWinnerDiff() {
        return winnerDiff;
    }

    public void setWinnerDiff(List<Block> winnerDiff) {
        this.winnerDiff = winnerDiff;
    }

    public static List<Block> sortWinner(Map<String, Account> finalBalances, List<Block> list) {
        //TODO start test ---------------------------------------------------------
        // Получение big random значения для блока
        Function<Block, Integer> bigRandomValue = block -> bigRandomWinner(block, finalBalances.get(block.getMinerAddress()));

// Создание компаратора с учетом big random, hashComplexity, staking и transactionCount
        Comparator<Block> blockComparator = Comparator
                .comparing(bigRandomValue, Comparator.reverseOrder()) // Сначала по bigRandom
                .thenComparing(Block::getHashCompexity, Comparator.reverseOrder()) // Затем по hashComplexity
                .thenComparing(block -> Optional.ofNullable(finalBalances.get(block.getMinerAddress()))
                        .map(Account::getDigitalStakingBalance)
                        .orElse(BigDecimal.ZERO), Comparator.reverseOrder()) // Затем по staking
                .thenComparing(block -> block.getDtoTransactions().size(), Comparator.reverseOrder()); // И наконец, по количеству транзакций

// Применение компаратора для сортировки списка
        List<Block> sortedList = list.stream()
                .sorted(blockComparator)
                .collect(Collectors.toList());

        return sortedList;
        //TODO finish test ---------------------------------------------------------
    }

    public static Block selectWinner(List<Block> candidates, Map<String, Account> list) {
        Block winner = null;
        int highestValue = 0;

        for (Block candidate : candidates) {
            // Использование bigRandomWinner для генерации случайного числа для кандидата
            int candidateValue = bigRandomWinner(candidate, list.get(candidate.getMinerAddress()));

            // Проверка, является ли текущий кандидат победителем
            if (candidateValue > highestValue) {
                highestValue = candidateValue;
                winner = candidate;
            }
        }

        return winner;
    }
    private void deleteBlockedHosts() {
        try {
            Mining.deleteFiles(Seting.ORIGINAL_POOL_URL_ADDRESS_BLOCKED_FILE);
            System.out.println("Заблокированные хосты успешно удалены.");
        } catch (Exception e) {
            System.err.println("Ошибка при удалении заблокированных хостов: " + e.getMessage());
            MyLogger.saveLog("TournamentService: ", e);
        }
    }

    public List<LiteVersionWiner> blockToLiteVersion(List<Block> list, Map<String, Account> balances) {
        List<LiteVersionWiner> list1 = new ArrayList<>();
        for (Block block : list) {
            Account account = balances.get(block.getMinerAddress());
            if (account == null)
                account = new Account(block.getMinerAddress(), BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO);
            LiteVersionWiner liteVersionWiner = new LiteVersionWiner(
                    block.getIndex(),
                    block.getMinerAddress(),
                    block.getHashBlock(),
                    block.getDtoTransactions().size(),
                    account.getDigitalStakingBalance().doubleValue(),
                    bigRandomWinner(block, account),
                    block.getHashCompexity()
            );
            list1.add(liteVersionWiner);
        }
        return list1;
    }

    public void getCheckSyncTime(){
        List<HostEndDataShortB> sortPriorityHost = null;
        MyLogger.saveLog("start: getCheckSyncTime");

        try {
            Set<String> nodesAll = getNodes();
            sortPriorityHost = utilsResolving.sortPriorityHost(nodesAll);
        } catch (Exception e) {
            MyLogger.saveLog("getCheckSyncTime: ", e);
            return;
        }

        List<CompletableFuture<Void>> futures = sortPriorityHost.stream().map(hostEndDataShortB -> CompletableFuture.runAsync(() -> {
            String s = hostEndDataShortB.getHost();
            try {
                if (BasisController.getExcludedAddresses().contains(s)) {
                    System.out.println(":its your address or excluded address: " + s);
                    return;
                }

                //TODO здесь должен он получить время и сравнить его с pool.ntp.org
                //TODO так как каждый сервер ntp настраивает на уровне виндовса
                //TODO как мы можем убедиться здесь чтобы этот сервер настроил свой ntp
                //TODO или нет, чтобы если он не настроил, у него не брать блоки
                //TODO если нет, то он должен так заблокировать
                // UtilsAllAddresses.saveAllAddresses(hostEndDataShortB.getHost(), Seting.ORIGINAL_POOL_URL_ADDRESS_BLOCKED_FILE);
                String timestr = UtilUrl.readJsonFromUrl(s +"/timentp");
                if(timestr == null || timestr.isEmpty() || timestr.isBlank()){
                    return;
                }
                long localTime = UtilsTime.getUniversalTimestamp();
                long serverTime = (long) UtilsJson.jsonToObject(timestr, Long.class);
                if (!UtilsTime.isTimeSynchronized(localTime, serverTime)){
                    MyLogger.saveLog("pool.ntp.org different time in server");
                    UtilsAllAddresses.saveAllAddresses(hostEndDataShortB.getHost(), Seting.ORIGINAL_POOL_URL_ADDRESS_BLOCKED_FILE);
                }


            } catch (IOException | JSONException e) {
                MyLogger.saveLog("cannot connect to " + s);
                MyLogger.saveLog(e.toString());
            } catch (Exception e) {
                MyLogger.saveLog("Unexpected error: " + s);
                MyLogger.saveLog(e.toString());
            }
        })).collect(Collectors.toList());

        CompletableFuture<Void> allOf = CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));
        try {
            allOf.get();
        } catch (InterruptedException | ExecutionException e) {
            MyLogger.saveLog("getCheckSyncTime: ", e);
        }

        MyLogger.saveLog("finish: getCheckSyncTime");
    }

    public void getAllWinner(List<HostEndDataShortB> hostEndDataShortBS) {
        List<CompletableFuture<Void>> futures = hostEndDataShortBS.stream().map(hostEndDataShortB -> CompletableFuture.runAsync(() -> {
            String s = hostEndDataShortB.getHost();
            try {
                if (BasisController.getExcludedAddresses().contains(s)) {
                    System.out.println(":its your address or excluded address: " + s);
                    return;
                }

                CompletableFuture<String> winnerListFuture = CompletableFuture.supplyAsync(() -> {
                    try {
                        return UtilUrl.readJsonFromUrl(s + "/winnerList");
                    } catch (IOException | JSONException e) {
                        MyLogger.saveLog("Error reading winnerList from " + s);
                        MyLogger.saveLog(e.toString());
                        return "";
                    }
                });

                CompletableFuture<String> prevBlockFuture = CompletableFuture.supplyAsync(() -> {
                    try {
                        return UtilUrl.readJsonFromUrl(s + "/prevBlock");
                    } catch (IOException | JSONException e) {
                        MyLogger.saveLog("Error reading prevBlock from " + s);
                        MyLogger.saveLog(e.toString());
                        return "";
                    }
                });

                CompletableFuture<Void> combinedFuture = CompletableFuture.allOf(winnerListFuture, prevBlockFuture);
                combinedFuture.join();

                String winnerListJson = winnerListFuture.get();
                String prevBlockJson = prevBlockFuture.get();

                if (winnerListJson.isEmpty() || winnerListJson.isBlank() || prevBlockJson.isEmpty() || prevBlockJson.isBlank()) {
                    return;
                }

                List<Block> blocks = UtilsJson.jsonToObject(winnerListJson);
                Block prevBlock = UtilsJson.jsonToBLock(prevBlockJson);

                if (BasisController.getBlockchainSize() == prevBlock.getIndex()) {
                    blocks.add(prevBlock);
                }

                for (Block block : blocks) {
                    MyLogger.saveLog("Processing block with index: " + block.getIndex());
                    List<String> sign = new ArrayList<>();
                    List<Block> tempBlock = new ArrayList<>();
                    tempBlock.add(block);

                    Map<String, Account> tempBalances = UtilsAccountToEntityAccount.entityAccountsToMapAccounts(UtilsUse.accounts(tempBlock, blockService));

                    DataShortBlockchainInformation temp = Blockchain.shortCheck(
                            BasisController.prevBlock(), tempBlock, BasisController.getShortDataBlockchain(),
                            new ArrayList<>(), tempBalances, sign, UtilsUse.balancesClone(tempBalances), new ArrayList<>());

                    if (temp.isValidation()) {
                        MyLogger.saveLog("Block is valid: " + block.getIndex() + " s: " + s);
                        synchronized (BasisController.getWinnerList()) {
                            if (!BasisController.getWinnerList().contains(block)) {
                                BasisController.getWinnerList().add(block);
                            }
                        }
                    }
                }
            } catch (Exception e) {
                MyLogger.saveLog("Unexpected error: " + s);
                MyLogger.saveLog(e.toString());
            }
        })).collect(Collectors.toList());

        CompletableFuture<Void> allOf = CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));
        try {
            allOf.get();
        } catch (InterruptedException | ExecutionException e) {
            MyLogger.saveLog("getAllWinner: ", e);
        }
    }

    public void tournament(List<HostEndDataShortB> hostEndDataShortBS)  {

        // Сначала вызываем getAllWinner
        getAllWinner(hostEndDataShortBS);

        // Меняем состояние на "готов"
        NodeController.setReady();

        // Затем вызываем initiateProcess
        nodeChecker.initiateProcess(hostEndDataShortBS);

        // Запланировать задачу удаления каждые 500 секунд с начальной задержкой 0
        scheduler.scheduleAtFixedRate(this::deleteBlockedHosts, 0, Seting.DELETED_FILE_BLOCKED_HOST_TIME_SECOND, TimeUnit.SECONDS);

        try {
            List<Block> list = BasisController.getWinnerList();
            list = list.stream()
                    .filter(t -> t.getIndex() == BasisController.getBlockchainSize())
                    .filter(UtilsUse.distinctByKey(Block::getHashBlock))
                    .filter(t-> UtilsUse.getDuplicateTransactions(t).size() == 0)
                    .collect(Collectors.toList());




            if (list == null || list.isEmpty() || list.size() == 0) {
                BasisController.setIsSaveFile(true);
                System.out.println("-----------------------");
                System.out.println("you can safely shut down the server.: " + list.size());
                System.out.println("-----------------------");
                return;
            }
            UtilsBalance.setBlockService(blockService);
            Blockchain.setBlockService(blockService);
            UtilsBlock.setBlockService(blockService);

//            System.out.println("different time: " + timeDifference);


            System.out.println("+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++");
            System.out.println("start tournament:");
            long startTournament = UtilsTime.getUniversalTimestamp();


            System.out.println("+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++");

            if (winner == null) {
                winner = new ArrayList<>();
            } else {
                winner.clear();
            }
            if (winnerDiff == null) {
                winnerDiff = new ArrayList<>();
            } else {
                winnerDiff.clear();
            }
            if (winnerCountTransaction == null) {
                winnerCountTransaction = new ArrayList<>();
            } else {
                winnerCountTransaction.clear();
            }
            if (winnerStaking == null) {
                winnerStaking = new ArrayList<>();
            } else {
                winnerStaking.clear();
            }


            System.out.println("tournament: winner: " + winner.size());
            Map<String, Account> balances = new HashMap<>();

            balances = UtilsAccountToEntityAccount.entityAccountsToMapAccounts(UtilsUse.accounts(list, blockService));
            BasisController.setIsSaveFile(false);


            Map<String, Account> finalBalances = UtilsUse.balancesClone(balances);
            // Обеспечение наличия всех аккаунтов в finalBalances
            list.forEach(block -> finalBalances.computeIfAbsent(block.getMinerAddress(), address -> new Account(address, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO)));

            List<Block> winnerList = new ArrayList<>();

            if (list.isEmpty()) {
                return;
            }


            winnerList = sortWinner(finalBalances, list);


            Block prevBlock = BasisController.prevBlock();
            if (winner == null) {
                winner = new ArrayList<>();
            } else {
                winner.clear();
            }
            winner.add(winnerList.get(0));
            if (winner == null || winner.size() == 0 || winner.get(0) == null || winner.get(0).getTimestamp() == null) {
                System.out.println("--------------------------------------------");

                System.out.println("error null: winner: " + winner);
                System.out.println("--------------------------------------------");
                return;
            }


            List<Block> lastDiff = new ArrayList<>();


            if (prevBlock.getIndex() < Seting.V34_NEW_ALGO) {
                lastDiff = UtilsBlockToEntityBlock.entityBlocksToBlocks(
                        blockService.findBySpecialIndexBetween(
                                (prevBlock.getIndex() + 1) - Seting.PORTION_BLOCK_TO_COMPLEXCITY,
                                prevBlock.getIndex() + 1
                        )
                );
            }


            Map<String, Account> tempBalances = UtilsAccountToEntityAccount.entityAccountsToMapAccounts(UtilsUse.accounts(winner, blockService));
            List<String> sign = new ArrayList<>();
            //Вычисляет мета данные блокчейна, с учетом нового блока, его целостность, длину, а также другие параметры
            DataShortBlockchainInformation temp = Blockchain.shortCheck(BasisController.prevBlock(), winner, BasisController.getShortDataBlockchain(), lastDiff, tempBalances, sign, UtilsUse.balancesClone(tempBalances), new ArrayList<>());

            if (temp == null || !temp.isValidation()) {
                System.out.println("wrong validation short: " + temp);
                return;
            }

            System.out.println("save winner: " + winner.size() + " balances: " + balances.size());
            //TODO прекратить давать блоки через sub block, если происходит запись
            BasisController.setIsSaveFile(false);
//                balances = UtilsAccountToEntityAccount.entityAccountsToMapAccounts(blockService.findAllAccounts());



            boolean save = false;
            //производит запись блока в файл и в базу данных, а также подсчитывает новый баланс.
            if (winner != null && balances != null ) {
                balances = UtilsAccountToEntityAccount.entityAccountsToMapAccounts(UtilsUse.accounts(winner, blockService));
                winner = winner.stream().filter(UtilsUse.distinctByKey(Block::getIndex)).collect(Collectors.toList());
                save = utilsResolving.addBlock3(winner, balances, Seting.ORIGINAL_BLOCKCHAIN_FILE, new ArrayList<>());
            }


            if (save) {
                //делает запись мета данных блокчейна.

                //Добавляет мета данные в статическую переменную.
                BasisController.setShortDataBlockchain(temp);
                BasisController.setBlockcheinSize((int) temp.getSize());
                BasisController.setBlockchainValid(temp.isValidation());
                String json = UtilsJson.objToStringJson(BasisController.getShortDataBlockchain());
                UtilsFileSaveRead.save(json, Seting.TEMPORARY_BLOCKCHAIN_FILE, false);


                EntityBlock entityBlock = blockService.findBySpecialIndex(temp.getSize() - 1);
                System.out.println("entityBlock: " + entityBlock + " temp size: " + (temp.getSize() - 1));
                //берет последний блок, и добавляет его в статистическую переменную.

                prevBlock = UtilsBlockToEntityBlock.entityBlockToBlock(entityBlock);
                BasisController.setPrevBlock(prevBlock);
                if (prevBlock == null) {
                    System.out.println("----------");
                    System.out.println("prevBlock: " + prevBlock);
                    System.out.println("----------");
                    System.exit(1);
                }

            }else {
                MyLogger.saveLog("Tournament addBlock3 has error: " + winner.get(0).getIndex());
            }
            balances = UtilsAccountToEntityAccount.entityAccountsToMapAccounts(UtilsUse.accounts(list, blockService));

            BasisController.setIsSaveFile(true);

            BasisController.setAllWiners(blockToLiteVersion(winnerList, balances));
            BasisController.getCountTransactionsWiner().clear();
            BasisController.getStakingWiners().clear();
            BasisController.getBigRandomWiner().clear();
            BasisController.getPowerWiners().clear();
            BasisController.setCountTransactionsWiner(null);
            BasisController.setStakingWiners(null);
            BasisController.setBigRandomWiner(null);
            BasisController.setPowerWiners(null);

            BasisController.setCountTransactionsWiner(blockToLiteVersion(new ArrayList<>(), balances));
            BasisController.setStakingWiners(blockToLiteVersion(new ArrayList<>(), balances));
            BasisController.setBigRandomWiner(blockToLiteVersion(winner, balances));

            BasisController.setPowerWiners(blockToLiteVersion(new ArrayList<>(), balances));
            if (winner.get(0).getIndex() % 576 == 0) {
                BasisController.setTotalTransactionsDays(0);
                BasisController.setTotalTransactionsSumDllar(0);
            }
            BasisController.setTotalTransactionsDays(
                    (int) (BasisController.totalTransactionsDays()
                            + winner.get(0).getDtoTransactions().size())
            );

            BasisController.setTotalTransactionsSumDllar(
                    BasisController.totalTransactionsSumDollar() +
                            winner.get(0).getDtoTransactions().stream()
                                    .mapToDouble(t -> t.getDigitalDollar())
                                    .sum()
            );

            if (BasisController.getBlockchainSize() % 576 == 0) {
              BasisController.setTotalDollars(blockService.getTotalDigitalDollarBalance());

            }


            if (winner == null) {
                winner = new ArrayList<>();
            } else {
                winner.clear();
            }
            if (winnerDiff == null) {
                winnerDiff = new ArrayList<>();
            } else {
                winnerDiff.clear();
            }
            if (winnerCountTransaction == null) {
                winnerCountTransaction = new ArrayList<>();
            } else {
                winnerCountTransaction.clear();
            }
            if (winnerStaking == null) {
                winnerStaking = new ArrayList<>();
            } else {
                winnerStaking.clear();
            }
            if (BasisController.getWinnerList() == null) {
                BasisController.setWinnerList(new CopyOnWriteArrayList<>());
            } else {
                BasisController.getWinnerList().clear();
            }


            System.out.println("___________________________________________________");
            long finishTournament = UtilsTime.getUniversalTimestamp();
            System.out.println("finish time: " + UtilsTime.differentMillSecondTime(startTournament, finishTournament));
            System.out.println("___________________________________________________");
            BasisController.setIsSaveFile(true);


        } catch (IOException e) {
            System.out.println("TournamentService: IOException");
            e.printStackTrace();
            MyLogger.saveLog("TournamentService: ", e);

        } catch (NoSuchAlgorithmException e) {
            System.out.println("TournamentService: NoSuchAlgorithmException");
            e.printStackTrace();
            MyLogger.saveLog("TournamentService: ", e);

        } catch (InvalidKeySpecException e) {
            System.out.println("TournamentService: InvalidKeySpecException");

            e.printStackTrace();
            MyLogger.saveLog("TournamentService: ", e);

        } catch (SignatureException e) {
            System.out.println("TournamentService: SignatureException");
            e.printStackTrace();
            MyLogger.saveLog("TournamentService: ", e);

        } catch (NoSuchProviderException e) {
            System.out.println("TournamentService: NoSuchProviderException");
            e.printStackTrace();
            MyLogger.saveLog("TournamentService: ", e);

        } catch (InvalidKeyException e) {
            System.out.println("TournamentService: InvalidKeyException");
            e.printStackTrace();
            MyLogger.saveLog("TournamentService: ", e);

        } catch (CloneNotSupportedException e) {
            System.out.println("TournamentService: CloneNotSupportedException");
            e.printStackTrace();
            MyLogger.saveLog("TournamentService: ", e);

        } finally {
            NodeController.setNotReady();
            BasisController.setIsSaveFile(true);
        }

    }


    public void updatingNodeEndBlocks(List<HostEndDataShortB> hostEndDataShortBS) {
        int result = -10;
        try {

            MyHost myHost = new MyHost(domainConfiguration.getPubllc_domain(), Seting.NAME_SERVER, Seting.PUBLIC_KEY);

            //TODO здесь будет скачиваться обновление
//                long beforeMemory = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
//                MyLogger.saveLog("memory before: resolve3" + beforeMemory);
            result = utilsResolving.resolve3(hostEndDataShortBS);
            MyLogger.saveLog("start: updatingNodeEndBlocks: result: " + result);
//                long afterMemory = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
//                MyLogger.saveLog("memory after: resolve3" + afterMemory);
//                MyLogger.saveLog("memory result resolve3: " + (afterMemory - beforeMemory));
            System.out.println("finish updating --------------------------------------------");


            //TODO отправка своего хоста
            System.out.println("sending host --------------------------------------------");
            System.out.println("updatingNodeEndBlocks: send my host");
//                beforeMemory = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
//                MyLogger.saveLog("memory before: getNodes: " + beforeMemory);
            Set<String> nodes = BasisController.getNodes();
//                afterMemory = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
//                MyLogger.saveLog("memory after: getNodes: " + afterMemory);
//                MyLogger.saveLog("memory result getNodes: " + (afterMemory - beforeMemory));

            System.out.println("tournament nodes: " + nodes);
            System.out.println("my host: " + myHost);
            System.out.println("domain configuration: " + domainConfiguration);
//
//                beforeMemory = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
//                MyLogger.saveLog("memory before: sendAddress: " + beforeMemory);
            UtilsAllAddresses.sendAddress(nodes, myHost);
//                afterMemory = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
//                MyLogger.saveLog("memory after: sendAddress: " + afterMemory);
//                MyLogger.saveLog("memory result sendAddress: " + (afterMemory - beforeMemory));

            System.out.println("finish sending host --------------------------------------------");
            //TODO отправка скачивание всех хостов
            System.out.println("download host --------------------------------------------");
            System.out.println("download host");
            Set<String> node = BasisController.getNodes();
            node.remove(myHost.getHost());
//
//                beforeMemory = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
//                MyLogger.saveLog("memory before: for (String s : node): " + beforeMemory);
            for (String s : node) {
                try {
                    if (s == null || s.isBlank())
                        continue;

                    System.out.println("updating");
                    Set<String> tempNode = UtilsJson.jsonToSetAddresses(UtilUrl.readJsonFromUrl(s + "/getNodes"));

                    if (BasisController.getExcludedAddresses().contains(s) || s.equals(myHost.getHost())) {
                        System.out.println(":its your address or excluded address: " + s);
                        continue;
                    }
                    for (String s1 : tempNode) {
                        System.out.println("put host: s1:  " + s1);
                        UtilsAllAddresses.putHost(s1);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    System.out.println("updatingNodeEndBlocks: host not worked: " + s);
                    continue;
                }

            }
//                afterMemory = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
//                MyLogger.saveLog("memory after: for (String s : node) " + afterMemory);
//                MyLogger.saveLog("memory result for (String s : node) " + (afterMemory - beforeMemory));


//                beforeMemory = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
//                MyLogger.saveLog("memory before: clear: " + beforeMemory);
            if (winner == null) {
                winner = new ArrayList<>();
            } else {
                winner.clear();
            }
            if (winnerDiff == null) {
                winnerDiff = new ArrayList<>();
            } else {
                winnerDiff.clear();
            }
            if (winnerCountTransaction == null) {
                winnerCountTransaction = new ArrayList<>();
            } else {
                winnerCountTransaction.clear();
            }
            if (winnerStaking == null) {
                winnerStaking = new ArrayList<>();
            } else {
                winnerStaking.clear();
            }
            if (BasisController.getWinnerList() == null) {
                BasisController.setWinnerList(new CopyOnWriteArrayList<>());
            } else {
                BasisController.getWinnerList().clear();
            }

//                afterMemory = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
//                MyLogger.saveLog("memory after: clear " + afterMemory);
//                MyLogger.saveLog("memory result clear " + (afterMemory - beforeMemory));


        } catch (IOException e) {
            e.printStackTrace();
            MyLogger.saveLog("TournamentService updating: ", e);
        } finally {
            BasisController.setIsSaveFile(true);
        }


    }
    public List<DtoTransaction> getInstance() throws IOException, NoSuchAlgorithmException, InvalidKeySpecException, SignatureException, NoSuchProviderException, InvalidKeyException {
        List<DtoTransaction> instance = new ArrayList<>();
        List<CompletableFuture<Void>> futures = new ArrayList<>();

        for (HostEndDataShortB hostEndDataShortB : utilsResolving.sortPriorityHost(BasisController.getNodes())) {
            String s = hostEndDataShortB.getHost();
            final List<DtoTransaction> finalInstance = instance; // Создаем final-копию instance
            CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                try {
                    System.out.println("Получение транзакций с сервера: " + s + ". Время ожидания 45 секунд.");
                    String json = UtilUrl.readJsonFromUrl(s + "/getTransactions");
                    List<DtoTransaction> list;
                    if(!json.isEmpty() ){
                         list = UtilsJson.jsonToDtoTransactionList(json);
                        list = utilsTransactions.balanceTransaction(list, blockService);

                    }else {
                        list = new ArrayList<>();
                    }
                     synchronized(finalInstance) {
                        finalInstance.addAll(list);
                    }
                } catch (IOException | JSONException e) {
                    System.out.println("Ошибка при получении транзакций с сервера " + s + ": " + e.getMessage());
                }
            });
            futures.add(future);
        }

        // Дожидаемся завершения всех CompletableFuture
        CompletableFuture<Void> allFutures = CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));
        allFutures.join();

        // Добавляем транзакции из локального хранилища и удаляем дубликаты
        instance.addAll(UtilsTransaction.readLineObject(Seting.ORGINAL_ALL_TRANSACTION_FILE));
        instance = instance.stream().distinct().collect(Collectors.toList());

        return instance;
    }






    private boolean confirmReadiness(String host) {
        try {
            String response = UtilUrl.readJsonFromUrl(host + "/confirmReadiness", 2000); // Таймаут 2 секунды
            return "ready".equals(response);
        } catch (IOException | JSONException e) {
            MyLogger.saveLog("cannot connect to " + host);
            MyLogger.saveLog(e.toString());
            return false;
        }
    }



}
