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
import org.springframework.stereotype.Component;

import javax.transaction.Transactional;
import java.io.IOException;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
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
    private List<Block> winnerDiff = new ArrayList<>();
    private List<Block> winnerCountTransaction = new ArrayList<>();
    private List<Block> winnerStaking = new ArrayList<>();
    private List<Block> winner = new ArrayList<>();


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

        long timestamp = UtilsTime.getUniversalTimestamp();
        try {

            if (timestamp % Seting.TIME_TOURNAMENT_SECOND == 0) {
                System.out.println("+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++");
                System.out.println("start tournament:");
                System.out.println("+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++");

                winnerDiff = new ArrayList<>();
                winnerCountTransaction = new ArrayList<>();
                winnerStaking = new ArrayList<>();
                winner = new ArrayList<>();
                System.out.println("tournament: winner: " + winner.size());
                Map<String, Account> balances = null;

//                balances = SaveBalances.readLineObject(Seting.ORIGINAL_BALANCE_FILE);
                balances = UtilsAccountToEntityAccount.entityAccountsToMapAccounts(BlockService.findAllAccounts());
                BasisController.setIsSaveFile(false);
                List<Block> list = BasisController.getWinnerList();
                list = list.stream()
                        .filter(t->t.getIndex() == BasisController.getBlockcheinSize())
                        .collect(Collectors.toList());


                if (list.isEmpty() || list.size() == 0)
                    return;


                Map<String, Account> finalBalances = balances;
                // Обеспечение наличия всех аккаунтов в finalBalances
                list.forEach(block -> finalBalances.computeIfAbsent(block.getMinerAddress(), address -> new Account(address, 0.0, 0.0, 0.0)));

                Comparator<Block> comparator = Comparator
                        .comparing(Block::getHashCompexity, Comparator.reverseOrder())
                        .thenComparing(
                                block -> Optional.ofNullable(finalBalances.get(block.getMinerAddress()))
                                        .map(Account::getDigitalStakingBalance)
                                        .orElse(0.0),
                                Comparator.reverseOrder());
                //Сначала отбираем 200 блоков с высокой сложностью.
                //С уникальным хэш блока
                List<Block> winnerDiff = list.stream()
                        .filter(UtilsUse.distinctByKey(Block::getHashBlock))
                        .sorted(comparator)
                        .filter(t -> t.getHashCompexity() >= Seting.V34_MIN_DIFF)
                        .limit(Seting.POWER_WINNER)
                        .collect(Collectors.toList());


                 comparator = (Block t1, Block t2) -> {
                    int sizeComparison = Integer.compare(t2.getDtoTransactions().size(), t1.getDtoTransactions().size());
                    if (sizeComparison != 0) {
                        return sizeComparison;
                    } else {
                        double stakingBalance1 = Optional.ofNullable(finalBalances.get(t1.getMinerAddress()))
                                .map(Account::getDigitalStakingBalance)
                                .orElse(0.0);
                        double stakingBalance2 = Optional.ofNullable(finalBalances.get(t2.getMinerAddress()))
                                .map(Account::getDigitalStakingBalance)
                                .orElse(0.0);
                        return Double.compare(stakingBalance2, stakingBalance1);
                    }
                };

                winnerCountTransaction = winnerDiff.stream()
                        .sorted(comparator)
                        .limit(Seting.TRANSACTION_WINNER)
                        .collect(Collectors.toList());



                Map<String, Account> tempBalances = new HashMap<>();
                // Пройдемся по списку пар ключ-значение
                for (Block entry : winnerCountTransaction) {
                    // Если номер счета из списка строк является ключом в карте, добавим аккаунт в список
                    if (balances.containsKey(entry.getMinerAddress())) {
                        tempBalances.put(entry.getMinerAddress(), balances.get(entry.getMinerAddress()));
                    }else {
                        Account miner = new Account(entry.getMinerAddress(), 0, 0, 0);
                        tempBalances.put(entry.getMinerAddress(), miner);
                    }
                }

                //создает карту из 55 счетов с наибольшим стэйкингом.
                tempBalances = tempBalances.entrySet().stream()
                        .sorted(Map.Entry.comparingByValue(Comparator.comparing(Account::getDigitalStakingBalance).reversed()))
                        .limit(Seting.STAKING_WINNER)
                        .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

                //создает список 50 с наибольшим стэйкингом
                for (Block block : winnerCountTransaction) {
                    if (tempBalances.containsKey(block.getMinerAddress())) {
                        winnerStaking.add(block);
                    }

                }

                Block prevBlock = BasisController.prevBlock();
                winnerStaking = winnerStaking.stream().sorted(Comparator.comparing(Block::getHashBlock))
                        .collect(Collectors.toList());
                //случайный выбор победителя.
                ;
                winner.add(selectWinner(winnerStaking, finalBalances));
                if(winner.size() == 0 || winner == null){
                    System.out.println("--------------------------------------------");

                    System.out.println("winner: " + winner);
                    System.out.println("--------------------------------------------");
                    return;
                }

                List<Block> lastDiff = new ArrayList<>();
                List<String> sign = new ArrayList<>();

                lastDiff = UtilsBlockToEntityBlock.entityBlocksToBlocks(
                        BlockService.findBySpecialIndexBetween(
                                (prevBlock.getIndex() + 1) - Seting.PORTION_BLOCK_TO_COMPLEXCITY,
                                prevBlock.getIndex() + 1
                        )
                );


                //Вычисляет мета данные блокчейна, с учетом нового блока, его целостность, длину, а также другие параметры
                DataShortBlockchainInformation temp = Blockchain.shortCheck(BasisController.prevBlock(), winner, BasisController.getShortDataBlockchain(), lastDiff, tempBalances, sign);

                if (!temp.isValidation()) {
                    System.out.println("wrong validation short: " + temp);
                    return;
                }
                String json = UtilsJson.objToStringJson(temp);

                //делает запись мета данных блокчейна.
                UtilsFileSaveRead.save(json, Seting.TEMPORARY_BLOCKCHAIN_FILE, false);

                System.out.println("save winner: " + winner.size() + " balances: " + balances.size());
                //производит запись блока в файл и в базу данных, а также подсчитывает новый баланс.
//                utilsAddBlock.addBlock2(winner, balances);
                utilsResolving.addBlock3(winner, balances, Seting.ORIGINAL_BLOCKCHAIN_FILE);



                //Добавляет мета данные в статическую переменную.
                BasisController.setShortDataBlockchain(temp);
                BasisController.setBlockcheinSize((int) BasisController.getShortDataBlockchain().getSize());
                BasisController.setBlockchainValid(BasisController.getShortDataBlockchain().isValidation());

                EntityBlock entityBlock = BlockService.findBySpecialIndex(temp.getSize() - 1);
                System.out.println("entityBlock: " + entityBlock + " temp size: " + (temp.getSize() - 1));
                //берет последний блок, и добавляет его в статистическую переменную.
                prevBlock = UtilsBlockToEntityBlock.entityBlockToBlock(entityBlock);
                BasisController.setPrevBlock(prevBlock);




                BasisController.setAllWiners(blockToLiteVersion(list, balances));
                BasisController.setPowerWiners(blockToLiteVersion(winnerDiff, balances));
                BasisController.setCountTransactionsWiner(blockToLiteVersion(winnerCountTransaction, balances));
                BasisController.setStakingWiners(blockToLiteVersion(winnerStaking, balances));
                BasisController.setBigRandomWiner(blockToLiteVersion(winner, balances));

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


                Thread.sleep(1000);
                BasisController.setIsSaveFile(true);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        } catch (InvalidKeySpecException e) {
            throw new RuntimeException(e);
        } catch (SignatureException e) {
            throw new RuntimeException(e);
        } catch (NoSuchProviderException e) {
            throw new RuntimeException(e);
        } catch (InvalidKeyException e) {
            throw new RuntimeException(e);
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } finally {

            BasisController.setIsSaveFile(true);
        }

    }
}
