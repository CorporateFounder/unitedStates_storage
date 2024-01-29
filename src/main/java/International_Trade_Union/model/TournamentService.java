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
import java.math.BigInteger;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

@Component
public class TournamentService {

    @Autowired
    UtilsAddBlock utilsAddBlock;

    @Autowired
    BlockService blockService;
    private List<Block> winnerDiff = new ArrayList<>();
    private List<Block> winnerCountTransaction = new ArrayList<>();
    private List<Block> winnerStaking = new ArrayList<>();
    private List<Block> winner = new ArrayList<>();


    public static Block selectWinner(List<Block> candidates, int limit) {
        Block winner = null;
        BigInteger highestValue = BigInteger.ZERO;

        for (Block candidate : candidates) {
            // Суммирование хешей
            String combinedHash = candidate.getPreviousHash() + candidate.getHashBlock(); // предполагается, что у блока есть метод getHash
            BigInteger seed = new BigInteger(combinedHash.getBytes());

            // Генерация числа в диапазоне от 1 до limit
            BigInteger candidateValue = new BigInteger(limit, new SecureRandom(seed.toByteArray()));
            BigInteger result = candidateValue.mod(new BigInteger("131"));

            // Проверка, является ли текущий кандидат победителем
            if (result.compareTo(highestValue) > 0) {
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
                    UtilsUse.bigRandomWinner(block),
                    block.getHashCompexity()
            );
            list1.add(liteVersionWiner);
        }
        return list1;
    }

    @Transactional
    public void tournament() {
        long timestamp = UtilsTime.getUniversalTimestampSecond();
        try {
            ;

            if (timestamp % Seting.TIME_TOURNAMENT_SECOND == 0) {
                System.out.println("tournament: winner: " + winner.size());

                BasisController.setIsSaveFile(false);
                List<Block> list = BasisController.getWinnerList();
                if (list.isEmpty() || list.size() == 0)
                    return;

                //Сначала отбираем 200 блоков с высокой сложностью.
                //С уникальным хэш блока
                winnerDiff = list.stream()
                        .filter(UtilsUse.distinctByKey(Block::getHashBlock))
                        .sorted(Comparator.comparing(Block::getHashCompexity).reversed())
                        .filter(t -> t.getHashCompexity() >= Seting.V34_MIN_DIFF)
                        .limit(Seting.POWER_WINNER)
                        .collect(Collectors.toList());

                winnerCountTransaction = winnerDiff.stream()
                        .sorted((t1, t2) -> t2.getDtoTransactions().size() - t1.getDtoTransactions().size())
                        .limit(Seting.TRANSACTION_WINNER)
                        .collect(Collectors.toList());

                Map<String, Account> balances = null;

                balances = SaveBalances.readLineObject(Seting.ORIGINAL_BALANCE_FILE);


                Map<String, Account> tempBalances = new HashMap<>();
                // Пройдемся по списку пар ключ-значение
                for (Block entry : winnerCountTransaction) {
                    // Если номер счета из списка строк является ключом в карте, добавим аккаунт в список
                    if (balances.containsKey(entry.getMinerAddress())) {
                        tempBalances.put(entry.getMinerAddress(), balances.get(entry.getMinerAddress()));
                    }
                }

                //создает карту из 130 счетов с наибольшим стэйкингом.
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
                winner.add(selectWinner(winnerStaking, Seting.STAKING_WINNER));

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
                utilsAddBlock.addBlock2(winner, balances);


                //Добавляет мета данные в статическую переменную.
                BasisController.setShortDataBlockchain(temp);
                BasisController.setBlockcheinSize((int) BasisController.getShortDataBlockchain().getSize());
                BasisController.setBlockchainValid(BasisController.getShortDataBlockchain().isValidation());

                EntityBlock entityBlock = BlockService.findBySpecialIndex(temp.getSize() - 1);
                System.out.println("entityBlock: " + entityBlock + " temp size: " + (temp.getSize() - 1));
                //берет последний блок, и добавляет его в статистическую переменную.
                prevBlock = UtilsBlockToEntityBlock.entityBlockToBlock(entityBlock);
                BasisController.setPrevBlock(prevBlock);

                //обнуляет победителей, для нового раунда.
                BasisController.setWinnerList(new CopyOnWriteArrayList<>());


                System.out.println("++++++++++++++++++++++++++");
                System.out.println("prev block: " + prevBlock.getIndex());
                System.out.println("winner: " + winner.size());
                System.out.println("getShortData: " + BasisController.getShortDataBlockchain());
                System.out.println("last diff: " + lastDiff.size());
                System.out.println("tempBalances: " + tempBalances.size());
                System.out.println("++++++++++++++++++++++++++");




                BasisController.setAllWiners(blockToLiteVersion(list, balances));
                BasisController.setPowerWiners(blockToLiteVersion(winnerDiff, balances));
                BasisController.setCountTransactionsWiner(blockToLiteVersion(winnerCountTransaction, balances));
                BasisController.setStakingWiners(blockToLiteVersion(winnerStaking, balances));
                BasisController.setBigRandomWiner(blockToLiteVersion(winner, balances));

                winnerDiff = new ArrayList<>();
                winnerCountTransaction = new ArrayList<>();
                winnerStaking = new ArrayList<>();
                winner = new ArrayList<>();

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
