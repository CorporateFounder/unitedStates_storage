package International_Trade_Union.model;

import International_Trade_Union.controllers.BasisController;
import International_Trade_Union.entity.blockchain.Blockchain;
import International_Trade_Union.entity.blockchain.DataShortBlockchainInformation;
import International_Trade_Union.entity.blockchain.block.Block;
import International_Trade_Union.entity.entities.EntityBlock;
import International_Trade_Union.entity.services.BlockService;
import International_Trade_Union.logger.MyLogger;
import International_Trade_Union.setings.Seting;
import International_Trade_Union.utils.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.http.converter.json.GsonBuilderUtils;
import org.springframework.stereotype.Component;
import org.w3c.dom.ls.LSOutput;

import javax.transaction.Transactional;
import java.io.IOException;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Function;
import java.util.stream.Collectors;

import static International_Trade_Union.controllers.BasisController.getNodes;
import static International_Trade_Union.utils.UtilsUse.bigRandomWinner;

@Component
@Scope("singleton")
public class TournamentService {


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
                        .orElse(0.0), Comparator.reverseOrder()) // Затем по staking
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


    public List<LiteVersionWiner> blockToLiteVersion(List<Block> list, Map<String, Account> balances) {
        List<LiteVersionWiner> list1 = new ArrayList<>();
        for (Block block : list) {
            Account account = balances.get(block.getMinerAddress());
            if (account == null)
                account = new Account(block.getMinerAddress(), 0, 0, 0);
            LiteVersionWiner liteVersionWiner = new LiteVersionWiner(
                    block.getIndex(),
                    block.getMinerAddress(),
                    block.getHashBlock(),
                    block.getDtoTransactions().size(),
                    account.getDigitalStakingBalance(),
                    bigRandomWinner(block, account),
                    block.getHashCompexity()
            );
            list1.add(liteVersionWiner);
        }
        return list1;
    }

    public void getAllWinner() {

        long timestamp = UtilsTime.getUniversalTimestamp() / 1000;
        long prevTime = Tournament.getPrevTime() / 1000L;
        long timeDifference = timestamp - prevTime;
        if (timeDifference > Seting.GET_WINNER_SECOND) {
            Set<String> nodesAll = getNodes();
            List<HostEndDataShortB> sortPriorityHost = utilsResolving.sortPriorityHost(nodesAll);
            for (HostEndDataShortB hostEndDataShortB : sortPriorityHost) {
                String s = hostEndDataShortB.getHost();

                try {
                    String json = UtilUrl.readJsonFromUrl(s + "/winnerList");
                    List<Block> blocks = UtilsJson.jsonToListBLock(json);
                    for (Block block : blocks) {
                        List<String> sign = new ArrayList<>();
                        List<Block> tempBlock = new ArrayList<>();
                        tempBlock.add(block);
                        Map<String, Account> tempBalances = UtilsAccountToEntityAccount.entityAccountsToMapAccounts(UtilsUse.accounts(tempBlock, blockService));

                        DataShortBlockchainInformation temp = Blockchain.shortCheck(BasisController.prevBlock(), tempBlock, BasisController.getShortDataBlockchain(), new ArrayList<>(), tempBalances, sign);// Blockchain.checkEqualsFromToBlockFile(Seting.ORIGINAL_BLOCKCHAIN_FILE, addlist);
                        if (temp.isValidation()) {
                            if (!BasisController.getWinnerList().contains(block))
                                BasisController.getWinnerList().add(block);
                        }
                    }
                } catch (Exception e) {
                    System.out.println("cannot connect");
                    continue;
                }
            }

        }
    }

    @Transactional
    public void tournament() {

        long timestamp = UtilsTime.getUniversalTimestamp() / 1000;
        long prevTime = Tournament.getPrevTime() / 1000L;
        long timeDifference = timestamp - prevTime;

        //TODO удаляет заблокированные хосты, каждые 500 секунд. Возможно
        //TODO хост уже работает правильно
        if (timestamp % Seting.DELETED_FILE_BLOCKED_HOST_TIME_SECOND == 0) {
            Mining.deleteFiles(Seting.ORIGINAL_POOL_URL_ADDRESS_BLOCKED_FILE);
        }
        try {
            List<Block> list = BasisController.getWinnerList();
            list = list.stream()
                    .filter(t -> t.getIndex() == BasisController.getBlockchainSize())
                    .filter(UtilsUse.distinctByKey(Block::getHashBlock))
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

            if (timeDifference > Seting.TIME_TOURNAMENT_SECOND) {
                System.out.println("+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++");
                System.out.println("start tournament:");
                long startTournament = UtilsTime.getUniversalTimestamp();
                System.out.println("prevTime: " + prevTime);
                System.out.println("prevTime /1000L: " + prevTime / 1000L);
                System.out.println("timestamp: " + timestamp);
                System.out.println("timeDifferent: " + timeDifference);


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

//                balances = SaveBalances.readLineObject(Seting.ORIGINAL_BALANCE_FILE);
//                balances = UtilsAccountToEntityAccount.entityAccountsToMapAccounts(blockService.findAllAccounts());
                balances = UtilsAccountToEntityAccount.entityAccountsToMapAccounts(UtilsUse.accounts(list, blockService));
                BasisController.setIsSaveFile(false);


                Map<String, Account> finalBalances = UtilsUse.balancesClone(balances);
                // Обеспечение наличия всех аккаунтов в finalBalances
                list.forEach(block -> finalBalances.computeIfAbsent(block.getMinerAddress(), address -> new Account(address, 0.0, 0.0, 0.0)));

                List<Block> winnerList = new ArrayList<>();

                if (list.isEmpty()) {
                    return;
                }


                winnerList = sortWinner(finalBalances, list);


                Block prevBlock = BasisController.prevBlock();

                winner.add(winnerList.get(0));
                if (winner == null || winner.size() == 0 || winner.get(0) == null || winner.get(0).getTimestamp() == null) {
                    System.out.println("--------------------------------------------");

                    System.out.println("error null: winner: " + winner);
                    System.out.println("--------------------------------------------");
                    return;
                }


                List<Block> lastDiff = new ArrayList<>();
                List<String> sign = new ArrayList<>();

                if (prevBlock.getIndex() < Seting.V34_NEW_ALGO) {
                    lastDiff = UtilsBlockToEntityBlock.entityBlocksToBlocks(
                            blockService.findBySpecialIndexBetween(
                                    (prevBlock.getIndex() + 1) - Seting.PORTION_BLOCK_TO_COMPLEXCITY,
                                    prevBlock.getIndex() + 1
                            )
                    );
                }


                Map<String, Account> tempBalances = UtilsAccountToEntityAccount.entityAccountsToMapAccounts(UtilsUse.accounts(list, blockService));
                sign = new ArrayList<>();
                //Вычисляет мета данные блокчейна, с учетом нового блока, его целостность, длину, а также другие параметры
                DataShortBlockchainInformation temp = Blockchain.shortCheck(BasisController.prevBlock(), winner, BasisController.getShortDataBlockchain(), lastDiff, tempBalances, sign);

                if (temp == null || !temp.isValidation()) {
                    System.out.println("wrong validation short: " + temp);
                    return;
                }

                System.out.println("save winner: " + winner.size() + " balances: " + balances.size());
                //TODO прекратить давать блоки через sub block, если происходит запись
                BasisController.setIsSaveFile(false);
//                balances = UtilsAccountToEntityAccount.entityAccountsToMapAccounts(blockService.findAllAccounts());
                balances = UtilsAccountToEntityAccount.entityAccountsToMapAccounts(UtilsUse.accounts(list, blockService));


                boolean save = false;
                //производит запись блока в файл и в базу данных, а также подсчитывает новый баланс.
                if (winner != null && balances != null) {
                    save = utilsResolving.addBlock3(winner, balances, Seting.ORIGINAL_BLOCKCHAIN_FILE);
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
                    BasisController.setIsSaveFile(true);

                    BasisController.setAllWiners(blockToLiteVersion(winnerList, balances));
                }


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

                if (BasisController.totalDollars() == 0) {
                    for (Map.Entry<String, Account> ba : balances.entrySet()) {
                        BasisController.setTotalDollars(
                                BasisController.totalDollars() +
                                        ba.getValue().getDigitalDollarBalance()
                        );
                    }

                }
                BasisController.setTotalDollars(
                        BasisController.totalDollars() +
                                winner.get(0).getDtoTransactions()
                                        .stream()
                                        .filter(t -> t.getSender().equals(Seting.BASIS_ADDRESS))
                                        .mapToDouble(t -> t.getDigitalDollar())
                                        .sum()
                );


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
            } else {
                System.out.println("you can safely shut down the server. tournament method");

            }


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

            BasisController.setIsSaveFile(true);
        }

    }


    public void updatingNodeEndBlocks(boolean fastUpdating) {
        int result = -10;
        try {

            MyHost myHost = new MyHost(domainConfiguration.getPubllc_domain(), Seting.NAME_SERVER, Seting.PUBLIC_KEY);
            long timestamp = UtilsTime.getUniversalTimestamp() / 1000;


            long prevTime = Tournament.getPrevUpdateTime() / 1000L;
            long timeDifference = timestamp - prevTime;
            //timestamp % Seting.TIME_UPDATING == 0
            if (timeDifference > Seting.TIME_UPDATING || fastUpdating) {

                System.out.println("updating --------------------------------------------");
                System.out.println("updatingNodeEndBlocks: start resolving ");
                System.out.println("prevTime: " + prevTime);
                System.out.println("prevTime /1000L: " + prevTime / 1000L);
                System.out.println("timestamp: " + timestamp);
                System.out.println("timeDifferent: " + timeDifference);
                //TODO здесь будет скачиваться обновление
//                long beforeMemory = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
//                MyLogger.saveLog("memory before: resolve3" + beforeMemory);
                result = utilsResolving.resolve3();
                MyLogger.saveLog("start: updatingNodeEndBlocks: result: " + result);
//                long afterMemory = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
//                MyLogger.saveLog("memory after: resolve3" + afterMemory);
//                MyLogger.saveLog("memory result resolve3: " + (afterMemory - beforeMemory));
                System.out.println("finish updating --------------------------------------------");
                System.out.println("time changing in update: " + timeDifference);

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

                MyLogger.saveLog("*******************[tournamentService-updating-start]*****************");
                MyLogger.saveLog("winner.size: " + winner.size());
                MyLogger.saveLog("winnerDiff.size: " + winnerDiff.size());
                MyLogger.saveLog("winnerCountTransaction.size: " + winnerCountTransaction.size());
                MyLogger.saveLog("winnerStaking.size: " + winnerStaking.size());
                MyLogger.saveLog("BasisController.getAllWiners.size: " + BasisController.getAllWiners().size());
                MyLogger.saveLog("BasisController.getStakingWiners.size: " + BasisController.getStakingWiners().size());
                MyLogger.saveLog("BasisController.getPowerWiners.size: " + BasisController.getPowerWiners().size());
                MyLogger.saveLog("BasisController.getWinnerList.size: " + BasisController.getWinnerList().size());
                MyLogger.saveLog("BasisController.getCountTransactionsWiner.size: " + BasisController.getCountTransactionsWiner().size());
                MyLogger.saveLog("BasisController.getBigRandomWiner.size: " + BasisController.getBigRandomWiner().size());
                MyLogger.saveLog("BasisController.getNodes.size: " + BasisController.getNodes().size());
                MyLogger.saveLog("*******************[tournamentService-updating-finish]****************");

            } else {
                System.out.println("you can safely shut down the server. Update method");
            }
        } catch (IOException e) {
            e.printStackTrace();
            MyLogger.saveLog("TournamentService updating: ", e);
        } finally {
            BasisController.setIsSaveFile(true);
        }


    }
}
