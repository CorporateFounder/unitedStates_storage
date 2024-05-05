package International_Trade_Union.model;

import International_Trade_Union.controllers.BasisController;
import International_Trade_Union.entity.blockchain.Blockchain;
import International_Trade_Union.entity.blockchain.DataShortBlockchainInformation;
import International_Trade_Union.entity.blockchain.block.Block;
import International_Trade_Union.entity.entities.EntityBlock;
import International_Trade_Union.entity.services.BlockService;
import International_Trade_Union.setings.Seting;
import International_Trade_Union.utils.*;
import org.springframework.beans.factory.annotation.Autowired;
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

import static International_Trade_Union.utils.UtilsUse.bigRandomWinner;

@Component
public class TournamentService {

    @Autowired
    UtilsAddBlock utilsAddBlock;

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

    public static List<Block> sortWinner(Map<String, Account> finalBalances, List<Block> list){
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

    @Transactional
    public void tournament() {

        long timestamp = UtilsTime.getUniversalTimestamp() / 1000;
            long prevTime = Tournament.getPrevTime() / 1000L;
            long timeDifference = timestamp - prevTime ;

        //TODO удаляет заблокированные хосты, каждые 500 секунд. Возможно
        //TODO хост уже работает правильно
        if(timestamp % Seting.DELETED_FILE_BLOCKED_HOST_TIME_SECOND == 0){
            Mining.deleteFiles(Seting.ORIGINAL_POOL_URL_ADDRESS_BLOCKED_FILE);
        }
        try {
            List<Block> list = BasisController.getWinnerList();
            list = list.stream()
                    .filter(t->t.getIndex() == BasisController.getBlockchainSize())
                    .filter(UtilsUse.distinctByKey(Block::getHashBlock))
                    .collect(Collectors.toList());


//                Thread.sleep(100);
            if (list.isEmpty() || list.size() == 0){
                BasisController.setIsSaveFile(true);
                return;
            }
            UtilsBalance.setBlockService(blockService);
            Blockchain.setBlockService(blockService);
            UtilsBlock.setBlockService(blockService);

//            System.out.println("different time: " + timeDifference);

            if ( timeDifference > Seting.TIME_TOURNAMENT_SECOND) {
                System.out.println("+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++");
                System.out.println("start tournament:");
                long startTournament = UtilsTime.getUniversalTimestamp();
                System.out.println("prevTime: " + prevTime);
                System.out.println("prevTime /1000L: " + prevTime/1000L);
                System.out.println("timestamp: " + timestamp);
                System.out.println("timeDifferent: " + timeDifference);


                System.out.println("+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++");

                winnerDiff = new ArrayList<>();
                winnerCountTransaction = new ArrayList<>();
                winnerStaking = new ArrayList<>();
                winner = new ArrayList<>();
                System.out.println("tournament: winner: " + winner.size());
                Map<String, Account> balances = null;

//                balances = SaveBalances.readLineObject(Seting.ORIGINAL_BALANCE_FILE);
                balances = UtilsAccountToEntityAccount.entityAccountsToMapAccounts(blockService.findAllAccounts());
                BasisController.setIsSaveFile(false);



                Map<String, Account> finalBalances = balances;
                // Обеспечение наличия всех аккаунтов в finalBalances
                list.forEach(block -> finalBalances.computeIfAbsent(block.getMinerAddress(), address -> new Account(address, 0.0, 0.0, 0.0)));

               List<Block> winnerList = new ArrayList<>();

               if(list.isEmpty()){
                   return;
               }


                winnerList = sortWinner(finalBalances, list);


                Block prevBlock = BasisController.prevBlock();

                winner.add(winnerList.get(0));
                if(winner == null || winner.size() == 0 || winner.get(0) == null){
                    System.out.println("--------------------------------------------");

                    System.out.println("winner: " + winner);
                    System.out.println("--------------------------------------------");
                    return;
                }


                List<Block> lastDiff = new ArrayList<>();
                List<String> sign = new ArrayList<>();

                if(prevBlock.getIndex() < Seting.V34_NEW_ALGO){
                    lastDiff = UtilsBlockToEntityBlock.entityBlocksToBlocks(
                            blockService.findBySpecialIndexBetween(
                                    (prevBlock.getIndex() + 1) - Seting.PORTION_BLOCK_TO_COMPLEXCITY,
                                    prevBlock.getIndex() + 1
                            )
                    );
                }



                Map<String, Account> tempBalances = UtilsAccountToEntityAccount.entityAccountsToMapAccounts(blockService.findAllAccounts());
                sign = new ArrayList<>();
                //Вычисляет мета данные блокчейна, с учетом нового блока, его целостность, длину, а также другие параметры
                DataShortBlockchainInformation temp = Blockchain.shortCheck(BasisController.prevBlock(), winner, BasisController.getShortDataBlockchain(), lastDiff, tempBalances, sign);

                if (temp == null || !temp.isValidation() ) {
                    System.out.println("wrong validation short: " + temp);
                    return;
                }
                String json = UtilsJson.objToStringJson(temp);

                //делает запись мета данных блокчейна.
                UtilsFileSaveRead.save(json, Seting.TEMPORARY_BLOCKCHAIN_FILE, false);

                System.out.println("save winner: " + winner.size() + " balances: " + balances.size());
                //TODO прекратить давать блоки через sub block, если происходит запись
                BasisController.setIsSaveFile(false);
                balances = UtilsAccountToEntityAccount.entityAccountsToMapAccounts(blockService.findAllAccounts());

                //производит запись блока в файл и в базу данных, а также подсчитывает новый баланс.
                utilsResolving.addBlock3(winner, balances, Seting.ORIGINAL_BLOCKCHAIN_FILE);

                //Добавляет мета данные в статическую переменную.
                BasisController.setShortDataBlockchain(temp);
                BasisController.setBlockcheinSize((int) temp.getSize());
                BasisController.setBlockchainValid(temp.isValidation());


                EntityBlock entityBlock = blockService.findBySpecialIndex(temp.getSize() - 1);
                System.out.println("entityBlock: " + entityBlock + " temp size: " + (temp.getSize() - 1));
                //берет последний блок, и добавляет его в статистическую переменную.
                prevBlock = UtilsBlockToEntityBlock.entityBlockToBlock(entityBlock);
                BasisController.setPrevBlock(prevBlock);
                BasisController.setIsSaveFile(true);

                BasisController.setAllWiners(blockToLiteVersion(winnerList, balances));


                BasisController.setCountTransactionsWiner(blockToLiteVersion(new ArrayList<>(), balances));
                BasisController.setStakingWiners(blockToLiteVersion(new ArrayList<>(), balances));
                BasisController.setBigRandomWiner(blockToLiteVersion(winner, balances));

                BasisController.setPowerWiners(blockToLiteVersion(new ArrayList<>(), balances));
                if(winner.get(0).getIndex() % 576 == 0){
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
                                        .mapToDouble(t->t.getDigitalDollar())
                                        .sum()
                );

                if(BasisController.totalDollars() == 0){
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
                                        .filter(t->t.getSender().equals(Seting.BASIS_ADDRESS))
                                        .mapToDouble(t->t.getDigitalDollar())
                                        .sum()
                );

                winnerDiff = new ArrayList<>();
                winnerCountTransaction = new ArrayList<>();
                winnerStaking = new ArrayList<>();
                winner = new ArrayList<>();
                //обнуляет победителей, для нового раунда.
                BasisController.setWinnerList(new CopyOnWriteArrayList<>());



                System.out.println("___________________________________________________");
                long finishTournament = UtilsTime.getUniversalTimestamp();
                System.out.println("finish time: " + UtilsTime.differentMillSecondTime(startTournament, finishTournament));
                System.out.println("___________________________________________________");
                BasisController.setIsSaveFile(true);
            }


        } catch (IOException e) {
            System.out.println("TournamentService: IOException");
            e.printStackTrace();
            throw new RuntimeException(e);
        } catch (NoSuchAlgorithmException e) {
            System.out.println("TournamentService: NoSuchAlgorithmException");
            e.printStackTrace();
            throw new RuntimeException(e);
        } catch (InvalidKeySpecException e) {
            System.out.println("TournamentService: InvalidKeySpecException");

            e.printStackTrace();
            throw new RuntimeException(e);
        } catch (SignatureException e) {
            System.out.println("TournamentService: SignatureException");
            e.printStackTrace();
            throw new RuntimeException(e);
        } catch (NoSuchProviderException e) {
            System.out.println("TournamentService: NoSuchProviderException");
            e.printStackTrace();
            throw new RuntimeException(e);
        } catch (InvalidKeyException e) {
            System.out.println("TournamentService: InvalidKeyException");
            e.printStackTrace();
            throw new RuntimeException(e);
        } catch (CloneNotSupportedException e) {
            System.out.println("TournamentService: CloneNotSupportedException");
            e.printStackTrace();
            throw new RuntimeException(e);
        }  finally {

            BasisController.setIsSaveFile(true);
        }

    }


    public void updatingNodeEndBlocks()  {
        int result = -10;
        try {

            MyHost myHost = new MyHost(domainConfiguration.getPubllc_domain(), Seting.NAME_SERVER, Seting.PUBLIC_KEY);
            long timestamp = UtilsTime.getUniversalTimestamp() / 1000;



            long prevTime = Tournament.getPrevUpdateTime()/1000L;
            long timeDifference = timestamp - prevTime ;
            //timestamp % Seting.TIME_UPDATING == 0
            if(timeDifference > Seting.TIME_UPDATING){
                System.out.println("updating --------------------------------------------");
                System.out.println("updatingNodeEndBlocks: start resolving ");
                System.out.println("prevTime: " + prevTime);
                System.out.println("prevTime /1000L: " + prevTime/1000L);
                System.out.println("timestamp: " + timestamp);
                System.out.println("timeDifferent: " + timeDifference);
                //TODO здесь будет скачиваться обновление
                result = utilsResolving.resolve3();
                System.out.println("finish updating --------------------------------------------");
                System.out.println("time changing in update: " + timeDifference);

                //TODO отправка своего хоста
                System.out.println("sending host --------------------------------------------");
                System.out.println("updatingNodeEndBlocks: send my host");
                Set<String> nodes = BasisController.getNodes();

                System.out.println("nodes: " + nodes);
                System.out.println("my host: " + myHost);
                System.out.println("domain configuration: " + domainConfiguration);
                UtilsAllAddresses.sendAddress(nodes, myHost);
                System.out.println("finish sending host --------------------------------------------");
                //TODO отправка скачивание всех хостов
                System.out.println("download host --------------------------------------------");
                System.out.println("download host");
                Set<String> node = BasisController.getNodes();
                node.remove(myHost.getHost());
                for (String s : node) {
                    try {
                        if(s == null || s.isBlank())
                            continue;

                        System.out.println("updating");
                        Set<String> tempNode = UtilsJson.jsonToSetAddresses( UtilUrl.readJsonFromUrl(s + "/getNodes"));

                        if (BasisController.getExcludedAddresses().contains(s) || s.equals(myHost.getHost())) {
                            System.out.println(":its your address or excluded address: " + s);
                            continue;
                        }
                        for (String s1 : tempNode) {
                            System.out.println("put host: s1:  " + s1);
                            UtilsAllAddresses.putHost(s1);
                        }
                    }
                   catch (Exception e){
                        e.printStackTrace();
                       System.out.println("updatingNodeEndBlocks: host not worked: " + s);
                        continue;
                   }

                }
                System.out.println("finish download host");
                System.out.println("download host --------------------------------------------");
                winnerDiff = new ArrayList<>();
                winnerCountTransaction = new ArrayList<>();
                winnerStaking = new ArrayList<>();
                winner = new ArrayList<>();
                //обнуляет победителей, для нового раунда.
                BasisController.setWinnerList(new CopyOnWriteArrayList<>());

            }
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        } catch (InvalidKeySpecException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        } catch (SignatureException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        } catch (NoSuchProviderException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        } catch (InvalidKeyException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
        finally {
            BasisController.setIsSaveFile(true);
        }


    }
}
