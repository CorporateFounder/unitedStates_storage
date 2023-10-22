package International_Trade_Union.utils;

import International_Trade_Union.entity.DtoTransaction.DtoTransaction;
import International_Trade_Union.entity.blockchain.block.Block;
import International_Trade_Union.entity.entities.EntityBlock;
import International_Trade_Union.entity.entities.EntityDtoTransaction;
import International_Trade_Union.entity.entities.EntityLaws;
import International_Trade_Union.vote.Laws;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class UtilsBlockToEntityBlock {


    public static EntityBlock blockToEntityBlock(Block block) {

        long specialIndex = 0;
        if(block.getHashBlock().equals("08b1e6634457a40d3481e76ebd377e76322706e4ea27013b773686f7df8f8a4c")){
            specialIndex = 0;
        }else {
            specialIndex = block.getIndex();
        }
        List<EntityDtoTransaction> entityDtoTransactions =
                dtoTransactionToEntity(block.getDtoTransactions());
        EntityBlock entityBlock = new EntityBlock(
                entityDtoTransactions,
                block.getPreviousHash(),
                block.getMinerAddress(),
                block.getFounderAddress(),
                block.getRandomNumberProof(),
                block.getMinerRewards(),
                block.getHashCompexity(),
                block.getTimestamp(),
                block.getIndex(),
                block.getHashBlock(),
                specialIndex
        );

        for (EntityDtoTransaction dtoTransaction : entityDtoTransactions) {
            dtoTransaction.setEntityBlock(entityBlock);
        }

        return entityBlock;
    }

    public static DtoTransaction EntityDto(EntityDtoTransaction entityDtoTransaction) throws IOException {
        Laws laws = entityLawsToLaws(entityDtoTransaction.getEntityLaws());
        DtoTransaction dtoTransaction = new DtoTransaction(
                entityDtoTransaction.getSender(),
                entityDtoTransaction.getCustomer(),
                entityDtoTransaction.getDigitalDollar(),
                entityDtoTransaction.getDigitalStockBalance(),
                laws,
                entityDtoTransaction.getBonusForMiner(),
                entityDtoTransaction.getVoteEnum());
        dtoTransaction.setSign(entityDtoTransaction.getSign());
        return dtoTransaction;
    }

    public static DtoTransaction entityToDto(EntityDtoTransaction entityDtoTransaction) throws IOException {

        Laws laws = UtilsBlockToEntityBlock.entityLawsToLaws(entityDtoTransaction.getEntityLaws());
        DtoTransaction transaction = new DtoTransaction(
                entityDtoTransaction.getSender(),
                entityDtoTransaction.getCustomer(),
                entityDtoTransaction.getDigitalDollar(),
                entityDtoTransaction.getDigitalStockBalance(),
                laws,
                entityDtoTransaction.getBonusForMiner(),
                entityDtoTransaction.getVoteEnum()
        );
        transaction.setSign(entityDtoTransaction.getSign());
        return transaction;
    }
    public static List<EntityDtoTransaction> dtoTransactionToEntity(List<DtoTransaction> list) {
        List<EntityDtoTransaction> entityDtoTransactions = new ArrayList<>();
        for (DtoTransaction transaction : list) {
            EntityLaws entityLaws = lawsToEntity(transaction.getLaws());
            EntityDtoTransaction entityDtoTransaction = new EntityDtoTransaction(
                    transaction.getSender(),
                    transaction.getCustomer(),
                    transaction.getDigitalDollar(),
                    transaction.getDigitalStockBalance(),
                    entityLaws,
                    transaction.getBonusForMiner(),
                    transaction.getVoteEnum(),
                    transaction.getSign()
            );
            entityLaws.setEntityDtoTransaction(entityDtoTransaction);
            entityDtoTransaction.setEntityBlock(null); // Поле будет заполнено при преобразовании блока
            entityDtoTransactions.add(entityDtoTransaction);
//
        }

        return entityDtoTransactions;
    }
    public static String listToString(List<String> list) {
        // Проверить, что список не равен null
        if (list == null) {
            return null;
        }
        // Использовать метод String.join() для объединения элементов списка в одну строку с разделителем ","
        return String.join("#", list);
    }

    public static List<String> stringToList(String str) {
        // Проверить, что строка не равна null
        if (str == null) {
            return null;
        }
        // Использовать метод String.split() для разбиения строки на массив подстрок по разделителю ","
        String[] array = str.split("#");
        // Преобразовать массив в список с помощью метода Arrays.asList()
        return Arrays.asList(array);
    }
    public static EntityLaws lawsToEntity(Laws laws) {

        boolean isNull = laws.getLaws() == null? true :false;
        EntityLaws entityLaws = new EntityLaws(

                isNull,
                laws.getPacketLawName(),
                laws.getLaws(),
                laws.getHashLaw()
        );
        return entityLaws;
    }



    public static List<Block> entityBlocksToBlocks(List<EntityBlock> entityBlocks) throws IOException {
        List<Block> blocks = new ArrayList<>();
        for (EntityBlock entityBlock : entityBlocks) {
            Block block = entityBlockToBlock(entityBlock);
            blocks.add(block);
        }
        return blocks;

    }
    public static Block entityBlockToBlock(EntityBlock entityBlock) throws IOException {
        List<DtoTransaction> dtoTransactions = entityDtoTransactionToDtoTransaction(
                entityBlock.getDtoTransactions()
        );
        Block block = new Block(
                dtoTransactions,
                entityBlock.getPreviousHash(),
                entityBlock.getMinerAddress(),
                entityBlock.getFounderAddress(),
                entityBlock.getRandomNumberProof(),
                entityBlock.getMinerRewards(),
                entityBlock.getHashCompexity(),
                entityBlock.getTimestamp(),
                entityBlock.getIndex(),
                entityBlock.getHashBlock()
        );
        return block;
    }
    public static List<DtoTransaction> entityDtoTransactionToDtoTransaction(
            List<EntityDtoTransaction> entityDtoTransactions
    ) throws IOException {
        List<DtoTransaction> dtoTransactions = new ArrayList<>();


        for (EntityDtoTransaction entityDtoTransaction : entityDtoTransactions) {
            Laws laws = entityLawsToLaws(entityDtoTransaction.getEntityLaws());
            DtoTransaction dtoTransaction = new DtoTransaction(
                    entityDtoTransaction.getSender(),
                    entityDtoTransaction.getCustomer(),
                    entityDtoTransaction.getDigitalDollar(),
                    entityDtoTransaction.getDigitalStockBalance(),
                    laws,
                    entityDtoTransaction.getBonusForMiner(),
                    entityDtoTransaction.getVoteEnum()
            );
            dtoTransaction.setSign(entityDtoTransaction.getSign());

            dtoTransactions.add(dtoTransaction);

        }

        return dtoTransactions;
    }
    public static Laws entityLawsToLaws(EntityLaws entityLaws) throws IOException {
        String name = entityLaws.getPacketLawName() == null? null: entityLaws.getPacketLawName();
        List<String> strings = entityLaws.getLaws() == null? null: entityLaws.getLaws();
        String hash = entityLaws.getHashLaw() == null? null: entityLaws.getHashLaw();


        Laws laws;

        laws = new Laws(name, strings);


        if(entityLaws.isLawsIsNull()){
            laws.setLaws(null);
        }
        laws.setHashLaw(hash);
        return laws;

    }

}