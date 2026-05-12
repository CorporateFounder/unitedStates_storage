import International_Trade_Union.entity.DtoTransaction.DtoTransaction;
import International_Trade_Union.entity.blockchain.Blockchain;
import International_Trade_Union.entity.blockchain.DataShortBlockchainInformation;
import International_Trade_Union.entity.blockchain.block.Block;
import International_Trade_Union.model.Account;
import International_Trade_Union.utils.UtilsBalance;
import International_Trade_Union.utils.UtilsJson;
import International_Trade_Union.utils.UtilsUse;
import International_Trade_Union.vote.VoteEnum;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;
import java.math.BigDecimal;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


//TODO высянить почему не списывается правильно долги
@SpringBootTest
public class UtilsBalanceTest {






    //TODO исправить sendTest установив баланс отправилтеля
    @Test
    public void SendTest() throws NoSuchAlgorithmException, InvalidKeySpecException, IOException, SignatureException, NoSuchProviderException, InvalidKeyException, InvalidAlgorithmParameterException, CloneNotSupportedException {


        Block block = UtilsJson.jsonToBLock("{\"dtoTransactions\":[{\"sender\":\"faErFrDnBhfSfNnj1hYjxydKNH28cRw1PBwDQEXH3QsJ\",\"customer\":\"nNifuwmFZr7fnV1zvmpiyQDV5z7ETWvqR6GSeqeHTY43\",\"digitalDollar\":14.5,\"digitalStockBalance\":14.5,\"laws\":{\"packetLawName\":null,\"laws\":null,\"hashLaw\":null},\"bonusForMiner\":0.0,\"voteEnum\":\"YES\",\"sign\":\"MEUCIQCkBX4IBVOq/2mPsOTWIn2N8TPfS5FItHqrEqhuU/YgHAIgKmdMlLXLM4sfnWh0aO0ezTZeInUdc/dh7SDF1qOKPAI=\"},{\"sender\":\"faErFrDnBhfSfNnj1hYjxydKNH28cRw1PBwDQEXH3QsJ\",\"customer\":\"2A8vxijdyY5ST1WhLQan3N1P6wSdzBDo9VmEFhck9bArG\",\"digitalDollar\":145.0,\"digitalStockBalance\":145.0,\"laws\":{\"packetLawName\":null,\"laws\":null,\"hashLaw\":null},\"bonusForMiner\":3.0,\"voteEnum\":\"YES\",\"sign\":\"MEYCIQD8iQWqgDwsxDV3ew0yZeUDS+xnI2iNjuNHak4MccU1WgIhAOP1cKFcZfrJ6Oek4UH6cX9FcIBYaA3OwAItctWny1k7\"}],\"previousHash\":\"904045a20888b4581d08c9587242b740cb12603802c440104f10063236603202\",\"minerAddress\":\"2A8vxijdyY5ST1WhLQan3N1P6wSdzBDo9VmEFhck9bArG\",\"founderAddress\":\"nNifuwmFZr7fnV1zvmpiyQDV5z7ETWvqR6GSeqeHTY43\",\"randomNumberProof\":947262,\"minerRewards\":0.0,\"hashCompexity\":16,\"timestamp\":1704736749000,\"index\":167479,\"hashBlock\":\"185ac49d8c2320b030d4ac8454000b2a21423a00816a042c5403470032439188\"}");
        DtoTransaction transaction = block.getDtoTransactions().get(1);
        transaction.setBonusForMiner(3);
        transaction.setSender("2A8vxijdyY5ST1WhLQan3N1P6wSdzBDo9VmEFhck9bArG");
        transaction.setCustomer("nNifuwmFZr7fnV1zvmpiyQDV5z7ETWvqR6GSeqeHTY43");

        long index = 284984 + 5000;
        System.out.println("index: " + index);
        Account sender = new Account(transaction.getSender(), BigDecimal.valueOf(1000.00000000000001), BigDecimal.valueOf(1000.00000000001), BigDecimal.ZERO);
        Account miner = new Account(block.getMinerAddress(), BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO);
        Account recipient = new Account(transaction.getCustomer(), BigDecimal.valueOf(1000.00000000000001), BigDecimal.valueOf(1000.00000000001), BigDecimal.valueOf(0.0));
        Account testSender = sender.clone();
        Account testRecipietnt = recipient.clone();
        Account testMiner = miner.clone();
        System.out.println("sender before: " + sender);
        System.out.println("recipient before: " + recipient);
        System.out.println("minier before: " + miner);
        UtilsBalance.sendMoney(sender, recipient,  BigDecimal.valueOf(transaction.getDigitalDollar()),BigDecimal.valueOf(transaction.getDigitalStockBalance()),  BigDecimal.valueOf(transaction.getBonusForMiner()), VoteEnum.YES);
        System.out.println("sender after: " + sender);
        System.out.println("recipient after: " + recipient);
        System.out.println("minier after: " + miner);
        System.out.println("bonus for miner: " + block.getDtoTransactions().get(1).getBonusForMiner());

        System.out.println("roll back");
        UtilsBalance.rollBackSendMoney(sender, recipient,  BigDecimal.valueOf(transaction.getDigitalDollar()),BigDecimal.valueOf(transaction.getDigitalStockBalance()),  BigDecimal.valueOf(transaction.getBonusForMiner()), VoteEnum.YES);
        System.out.println("sender after: " + sender);
        System.out.println("recipient after: " + recipient);
        System.out.println("minier after: " + miner);

        Assert.assertTrue(testMiner.equals(miner) && testSender.equals(sender) && testRecipietnt.equals(recipient));
        System.out.println("equals: " + (testMiner.equals(miner) && testSender.equals(sender) && testRecipietnt.equals(recipient)));
    }
}


