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

    public  static List<Block> sortWinner(Map<String, Account> finalBalances, List<Block> list, long M) {
        //TODO start test ---------------------------------------------------------
        // Получение big random значения для блока

        Function<Block, Integer> bigRandomValue = block -> bigRandomWinner(block, finalBalances.get(block.getMinerAddress()), (int) M);

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




    public List<LiteVersionWiner> blockToLiteVersion(List<Block> list, Map<String, Account> balances, long M) {
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
                    bigRandomWinner(block, account, (int) M),
                    block.getHashCompexity()
            );
            list1.add(liteVersionWiner);
        }
        return list1;
    }

    public void getCheckSyncTime( List<HostEndDataShortB> hosts){
        List<HostEndDataShortB> sortPriorityHost = null;


        try {

            sortPriorityHost = hosts;
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


    }

    public void getAllWinner(List<HostEndDataShortB> hostEndDataShortBS) {
        // Временное потокобезопасное множество для хранения уникальных блоков
        Set<Block> tempWinnerSet = ConcurrentHashMap.newKeySet();

        // Создаём список задач
        List<CompletableFuture<Void>> futures = hostEndDataShortBS.stream()
                .map(hostEndDataShortB -> CompletableFuture.runAsync(() -> {
                    String s = hostEndDataShortB.getHost();
                    try {
                        if (BasisController.getExcludedAddresses().contains(s)) {
                            MyLogger.saveLog(":its your address or excluded address: " + s);
                            return;
                        }

                        CompletableFuture<String> winnerListFuture = CompletableFuture.supplyAsync(() -> {
                            try {
                                return UtilUrl.readJsonFromUrl(s + "/winnerList");
                            } catch (IOException | JSONException e) {
                                MyLogger.saveLog("Error reading winnerList from " + s + ": " + e.getMessage());
                                return "";
                            }
                        });

                        CompletableFuture<String> prevBlockFuture = CompletableFuture.supplyAsync(() -> {
                            try {
                                return UtilUrl.readJsonFromUrl(s + "/prevBlock");
                            } catch (IOException | JSONException e) {
                                MyLogger.saveLog("Error reading prevBlock from " + s + ": " + e.getMessage());
                                return "";
                            }
                        });

                        CompletableFuture<Void> combinedFuture = CompletableFuture.allOf(winnerListFuture, prevBlockFuture);
                        combinedFuture.join();

                        String winnerListJson = winnerListFuture.getNow("");
                        String prevBlockJson = prevBlockFuture.getNow("");

                        if (winnerListJson.isEmpty() || prevBlockJson.isEmpty()) {
                            return;
                        }

                        List<Block> blocks = UtilsJson.jsonToObject(winnerListJson);
                        Block prevBlock = UtilsJson.jsonToBLock(prevBlockJson);

                        if (BasisController.getBlockchainSize() == prevBlock.getIndex()) {
                            blocks.add(prevBlock);
                        }

                        for (Block block : blocks) {
                            MyLogger.saveLog("Processing block with index: " + block.getIndex());
                            validateAndAddBlock(block, tempWinnerSet, s);
                        }
                    } catch (Exception e) {
                        MyLogger.saveLog("Unexpected error for host: " + s + ": " + e.getMessage());
                    }
                }))
                .collect(Collectors.toList());

        // Ограничиваем общее время выполнения всех задач
        CompletableFuture<Void> allOf = CompletableFuture
                .allOf(futures.toArray(new CompletableFuture[0]))
                .orTimeout(36, TimeUnit.SECONDS); // Ограничение времени в 32 секунды

        try {
            allOf.join(); // Ждём завершения всех задач
        } catch (Exception e) {
            MyLogger.saveLog("Unexpected error in getAllWinner: " + e.getMessage());
        }

        // Перед выходом обновляем winnerList атомарно

            for (Block block : tempWinnerSet) {
                if (!BasisController.getWinnerList().contains(block)) {
                    BasisController.getWinnerList().add(block);
                }
            }

    }

    private void validateAndAddBlock(Block block, Set<Block> tempWinnerSet, String host) {
        try {
            List<String> sign = new ArrayList<>();
            List<Block> tempBlock = new ArrayList<>();
            tempBlock.add(block);

            Map<String, Account> tempBalances = UtilsAccountToEntityAccount.entityAccountsToMapAccounts(
                    UtilsUse.accounts(tempBlock, blockService));

            DataShortBlockchainInformation temp = Blockchain.shortCheck(
                    BasisController.prevBlock(), tempBlock, BasisController.getShortDataBlockchain(),
                    new ArrayList<>(), tempBalances, sign, UtilsUse.balancesClone(tempBalances), new ArrayList<>());

            if (temp.isValidation()) {
                MyLogger.saveLog("Block is valid: " + block.getIndex() + " from host: " + host);
                tempWinnerSet.add(block);
            }
        } catch (Exception e) {
            MyLogger.saveLog("Error validating block with index: " + block.getIndex() + " from host: " + host + ": " + e.getMessage());
        }
    }


    public void tournament(List<HostEndDataShortB> hostEndDataShortBS)  {

        long timeBefore = UtilsTime.getUniversalTimestamp();
        // Сначала вызываем getAllWinner
        getAllWinner(hostEndDataShortBS);
        long timeAfter = UtilsTime.getUniversalTimestamp();
        MyLogger.saveLog("getAllWinner: millisecond: " + (timeAfter - timeBefore) + " second: " +((timeAfter-timeBefore)/1000));

        // Меняем состояние на "готов"
        NodeController.setReady();

        timeBefore = UtilsTime.getUniversalTimestamp();
        // Затем вызываем initiateProcess
        nodeChecker.initiateProcess(hostEndDataShortBS);
        timeAfter = UtilsTime.getUniversalTimestamp();
        MyLogger.saveLog("initiateProcess: millisecond: " + (timeAfter - timeBefore) + " second: " +((timeAfter-timeBefore)/1000));

        try {
            timeBefore = UtilsTime.getUniversalTimestamp();
            List<Block> list = BasisController.getWinnerList();
            timeAfter = UtilsTime.getUniversalTimestamp();
            MyLogger.saveLog("getWinnerList: millisecond: " + (timeAfter - timeBefore) + " second: " +((timeAfter-timeBefore)/1000));

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


            int M = 0;
            if (list.get(0).getIndex() > Seting.OPTIMAL_SCORE_INDEX)
                M = Math.toIntExact(blockService.findModeHashComplexityInRange(list.get(0).getIndex()));


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


            winnerList = sortWinner(finalBalances, list, M);


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

            BasisController.setAllWiners(blockToLiteVersion(winnerList, balances, M));
            BasisController.getCountTransactionsWiner().clear();
            BasisController.getStakingWiners().clear();
            BasisController.getBigRandomWiner().clear();
            BasisController.getPowerWiners().clear();
            BasisController.setCountTransactionsWiner(null);
            BasisController.setStakingWiners(null);
            BasisController.setBigRandomWiner(null);
            BasisController.setPowerWiners(null);

            BasisController.setCountTransactionsWiner(blockToLiteVersion(new ArrayList<>(), balances, M));
            BasisController.setStakingWiners(blockToLiteVersion(new ArrayList<>(), balances, M));
            BasisController.setBigRandomWiner(blockToLiteVersion(winner, balances, M));

            BasisController.setPowerWiners(blockToLiteVersion(new ArrayList<>(), balances, M));
            if (winner.get(0).getIndex() % 432 == 0) {
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
                BasisController.setSizeWinnerList(0);
            } else {
                BasisController.getWinnerList().clear();
                BasisController.setSizeWinnerList(0);
            }


            System.out.println("___________________________________________________");
            long finishTournament = UtilsTime.getUniversalTimestamp();
            System.out.println("finish time: " + UtilsTime.differentMillSecondTime(startTournament, finishTournament));
            System.out.println("___________________________________________________");
            BasisController.setIsSaveFile(true);


        } catch (IOException e) {
            System.out.println("TournamentService: IOException");
            e.printStackTrace();
            MyLogger.saveLog("TournamentService: " + " message: " + e.getMessage() + " ", e );

        } catch (NoSuchAlgorithmException e) {
            System.out.println("TournamentService: NoSuchAlgorithmException");
            e.printStackTrace();
            MyLogger.saveLog("TournamentService: " + " message: " + e.getMessage() + " ", e);

        } catch (InvalidKeySpecException e) {
            System.out.println("TournamentService: InvalidKeySpecException");

            e.printStackTrace();
            MyLogger.saveLog("TournamentService: " + " message: " + e.getMessage() + " ", e);

        } catch (SignatureException e) {
            System.out.println("TournamentService: SignatureException");
            e.printStackTrace();
            MyLogger.saveLog("TournamentService: "+ " message: " + e.getMessage() + " ", e);

        } catch (NoSuchProviderException e) {
            System.out.println("TournamentService: NoSuchProviderException");
            e.printStackTrace();
            MyLogger.saveLog("TournamentService: "+ " message: " + e.getMessage() + " ", e);

        } catch (InvalidKeyException e) {
            System.out.println("TournamentService: InvalidKeyException");
            e.printStackTrace();
            MyLogger.saveLog("TournamentService: "+ " message: " + e.getMessage() + " ", e);

        } catch (CloneNotSupportedException e) {
            System.out.println("TournamentService: CloneNotSupportedException");
            e.printStackTrace();
            MyLogger.saveLog("TournamentService: "+ " message: " + e.getMessage() + " " , e);

        } finally {
            NodeController.setNotReady();
            BasisController.setIsSaveFile(true);
        }

    }

    public void sendAndPutHost(Set<String> nodes){
        // Отправка собственного хоста
        System.out.println("sending host --------------------------------------------");
        MyHost myHost = new MyHost(domainConfiguration.getPubllc_domain(), Seting.NAME_SERVER, Seting.PUBLIC_KEY);
        System.out.println("tournament nodes: " + nodes);
        System.out.println("my host: " + myHost);
        try {
            UtilsAllAddresses.sendAddress(nodes, myHost);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        System.out.println("finish sending host --------------------------------------------");

        // Скачивание всех хостов
        System.out.println("download host --------------------------------------------");

        nodes.remove(myHost.getHost());

        // Параллельная обработка узлов
        List<CompletableFuture<Void>> futures = nodes.stream()
                .filter(s -> s != null && !s.isBlank()) // Удаляем пустые строки и null
                .map(s -> CompletableFuture.runAsync(() -> {
                    try {
                        System.out.println("updating: " + s);
                        Set<String> tempNode = UtilsJson.jsonToSetAddresses(UtilUrl.readJsonFromUrl(s + "/getNodes"));

                        if (BasisController.getExcludedAddresses().contains(s) || s.equals(myHost.getHost())) {
                            System.out.println(":its your address or excluded address: " + s);
                            return;
                        }

                        tempNode.forEach(s1 -> {
                            System.out.println("put host: s1: " + s1);
                            UtilsAllAddresses.putHost(s1);
                        });
                    } catch (Exception e) {
                        System.err.println("updatingNodeEndBlocks: host not worked: " + s);
                        e.printStackTrace();
                    }
                }))
                .toList();

        // Ожидание завершения всех задач
        CompletableFuture<Void> allFutures = CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));
        allFutures.join(); // Дождаться завершения всех задач
        System.out.println("All hosts updated in parallel.");
    }

    public void updatingNodeEndBlocks(List<HostEndDataShortB> hostEndDataShortBS) {
        int result = -10;
        try {


            // Обновление через utilsResolving
            result = utilsResolving.resolve3(hostEndDataShortBS);
            System.out.println("finish updating --------------------------------------------");


            // Очистка списков победителей
            clearWinners();


        }finally {
            BasisController.setIsSaveFile(true);
        }
    }

    // Очистка списков победителей
    private void clearWinners() {
        if (winner == null) winner = new ArrayList<>();
        else winner.clear();

        if (winnerDiff == null) winnerDiff = new ArrayList<>();
        else winnerDiff.clear();

        if (winnerCountTransaction == null) winnerCountTransaction = new ArrayList<>();
        else winnerCountTransaction.clear();

        if (winnerStaking == null) winnerStaking = new ArrayList<>();
        else winnerStaking.clear();

        if (BasisController.getWinnerList() == null) {
            BasisController.setWinnerList(new CopyOnWriteArrayList<>());
            BasisController.setSizeWinnerList(0);
        } else {
            BasisController.getWinnerList().clear();
            BasisController.setSizeWinnerList(0);
        }
    }    public List<DtoTransaction> getInstance(List<HostEndDataShortB> hosts) throws IOException, NoSuchAlgorithmException, InvalidKeySpecException, SignatureException, NoSuchProviderException, InvalidKeyException {
        // Асинхронная обработка для получения транзакций с каждого хоста
        List<CompletableFuture<List<DtoTransaction>>> futures = hosts.stream()
                .map(hostEndDataShortB -> CompletableFuture.supplyAsync(() -> {
                    String host = hostEndDataShortB.getHost();
                    try {
                        System.out.println("Получение транзакций с сервера: " + host + ". Время ожидания 45 секунд.");
                        String json = UtilUrl.readJsonFromUrl(host + "/getTransactions");
                        if (!json.isEmpty()) {
                            List<DtoTransaction> list = UtilsJson.jsonToDtoTransactionList(json);
                            return utilsTransactions.balanceTransaction(list, blockService);
                        }
                    } catch (IOException | JSONException e) {
                        System.out.println("Ошибка при получении транзакций с сервера " + host + ": " + e.getMessage());
                    }
                    return new ArrayList<DtoTransaction>();
                }))
                .toList();

        // Сбор всех результатов
        List<DtoTransaction> instance = futures.stream()
                .map(CompletableFuture::join)
                .flatMap(List::stream)
                .collect(Collectors.toList());

        // Добавление транзакций из локального хранилища и удаление дубликатов
        instance.addAll(UtilsTransaction.readLineObject(Seting.ORGINAL_ALL_TRANSACTION_FILE));
        return instance.stream().distinct().collect(Collectors.toList());
    }




}
