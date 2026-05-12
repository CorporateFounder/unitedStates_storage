import International_Trade_Union.controllers.BasisController;
import International_Trade_Union.entity.DtoTransaction.DtoTransaction;
import International_Trade_Union.entity.SubBlockchainEntity;
import International_Trade_Union.entity.blockchain.DataShortBlockchainInformation;
import International_Trade_Union.entity.blockchain.block.Block;
import International_Trade_Union.entity.entities.EntityAccount;
import International_Trade_Union.entity.entities.EntityBlock;
import International_Trade_Union.model.Account;
import International_Trade_Union.model.HostEndDataShortB;
import International_Trade_Union.model.comparator.HostEndDataShortBComparator;
import International_Trade_Union.setings.Seting;
import International_Trade_Union.utils.*;
import International_Trade_Union.utils.base.Base;
import International_Trade_Union.utils.base.Base58;
import International_Trade_Union.vote.Laws;
import International_Trade_Union.vote.VoteEnum;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.json.JSONException;
import org.junit.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.sql.Timestamp;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

import static International_Trade_Union.setings.Seting.DOLLAR;
import static International_Trade_Union.utils.UtilsBalance.rollbackCalculateBalance;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;


@SpringBootTest
public class Testing {

    /**  if (this.index > Seting.NEW_ALGO_MINING) {
     MerkleTree merkleTree = new MerkleTree(this.getDtoTransactions());
     String hash = merkleTree.getRoot() + this.previousHash + this.minerAddress + this.founderAddress
     + this.randomNumberProof + this.minerRewards + this.hashCompexity + this.timestamp +
     this.index;
     return UtilsUse.sha256hash(hash);
     } else {
     return UtilsUse.sha256hash(jsonString());

     }*/

    @Test
    public void time() throws JSONException, IOException {
        String s = "http://194.87.236.238:82";
        String s1 = "http://37.205.15.167:82";
        String s2 = "http://37.27.60.116:7588";
       String time = UtilUrl.readJsonFromUrl_silent(s + "/time");
       String time1 = UtilUrl.readJsonFromUrl_silent(s1+"/time");
       String time2 = UtilUrl.readJsonFromUrl_silent(s2+"/time");
        System.out.println("time: " + time);
        System.out.println("time: " + time1);
        System.out.println("time: " + time2);
    }
    @Test
    public void block() throws IOException, NoSuchAlgorithmException, SignatureException, InvalidKeySpecException, NoSuchProviderException, InvalidKeyException {
        BigDecimal money =BigDecimal.valueOf(200);
        Block block = UtilsJson.jsonToBLock("{\"dtoTransactions\":[{\"sender\":\"faErFrDnBhfSfNnj1hYjxydKNH28cRw1PBwDQEXH3QsJ\",\"customer\":\"nNifuwmFZr7fnV1zvmpiyQDV5z7ETWvqR6GSeqeHTY43\",\"digitalDollar\":35.38,\"digitalStockBalance\":35.38,\"laws\":{\"packetLawName\":null,\"laws\":null,\"hashLaw\":null},\"bonusForMiner\":0.0,\"voteEnum\":\"YES\",\"sign\":\"MEQCIAVpifvnWhOx+7878Ry5adN1dhmOfwkmddRROrs++DjbAiBcwXQVaDa3QY/MkaXJt3EkPTUHNwZRN8z9nSECFEqFkw==\"},{\"sender\":\"faErFrDnBhfSfNnj1hYjxydKNH28cRw1PBwDQEXH3QsJ\",\"customer\":\"243kiLx69wNtpBYh7NTimcJdqUzu6ZRAUf8KJuydhjEsi\",\"digitalDollar\":353.8,\"digitalStockBalance\":353.8,\"laws\":{\"packetLawName\":null,\"laws\":null,\"hashLaw\":null},\"bonusForMiner\":0.0,\"voteEnum\":\"YES\",\"sign\":\"MEQCIHICM/fhkvx+rQEsZ6bt88kvuwbH9F4Q49B4E9LTZ0ksAiAQp2Cq3Qv1aSZDVS0xk3W1Sds3J+F5hvc6DZRN1uFtYA==\"},{\"sender\":\"jcqrb3nW1kYLAEYcuzfKFrCeoX2WCTnXq4sH2Rr3fQrE\",\"customer\":\"bZyM2pkbTHNUDbV9Z53u9rWu8TChhPkppdGWg3up7JSg\",\"digitalDollar\":1.111,\"digitalStockBalance\":0.0,\"laws\":{\"packetLawName\":\"\",\"laws\":[],\"hashLaw\":\"\"},\"bonusForMiner\":0.0,\"voteEnum\":\"YES\",\"sign\":\"MEUCIQDCCt6i5vEk8wxrCVS4OICbfv+fF6X3QImjB4sB4g+3xwIgAhWHz7vOx/VPbefGNnQ5+gLlsrWgKB78yrrdjam1qc4=\"}],\"previousHash\":\"0d05ca4c80440002c0c369ac05448903983c0d88064b48c4080808120d64180c\",\"minerAddress\":\"243kiLx69wNtpBYh7NTimcJdqUzu6ZRAUf8KJuydhjEsi\",\"founderAddress\":\"nNifuwmFZr7fnV1zvmpiyQDV5z7ETWvqR6GSeqeHTY43\",\"randomNumberProof\":2702159802066160,\"minerRewards\":0.0,\"hashCompexity\":21,\"timestamp\":1730195015000,\"index\":321433,\"hashBlock\":\"4e260ae0242b49832155061602083084c4005074955844040230c1192001208b\"}\n");
        Account sender = new Account("jcqrb3nW1kYLAEYcuzfKFrCeoX2WCTnXq4sH2Rr3fQrE", money, money, money );
        Account Customer = new Account("bZyM2pkbTHNUDbV9Z53u9rWu8TChhPkppdGWg3up7JSg", BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO);
        Account miner = new Account("243kiLx69wNtpBYh7NTimcJdqUzu6ZRAUf8KJuydhjEsi", BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO);
        Map<String, Account> balances = new HashMap<>();
        balances.put(sender.getAccount(), sender);
        balances.put(Customer.getAccount(), Customer);
        balances.put(miner.getAccount(), miner);
        UtilsBalance.calculateBalance(balances, block, new ArrayList<>(), new ArrayList<>());
        balances.entrySet().stream().forEach(System.out::println);

    }
    @Test
    public void getTransactionCountTest() throws IOException, JSONException, NoSuchAlgorithmException, SignatureException, InvalidKeySpecException, NoSuchProviderException, InvalidKeyException {
        DtoTransaction transaction = new DtoTransaction();
        transaction.setSender("faErFrDnBhfSfNnj1hYjxydKNH28cRw1PBwDQEXH3QsJ");
        transaction.setCustomer("nNifuwmFZr7fnV1zvmpiyQDV5z7ETWvqR6GSeqeHTY43");
        transaction.setDigitalDollar(27.84);
        transaction.setDigitalStockBalance(27.84);
        Laws laws = new Laws();
        laws.setLaws(null);
        laws.setHashLaw(null);
        laws.setPacketLawName(null);
        transaction.setLaws(laws);
        transaction.setBonusForMiner(0.0);
        transaction.setVoteEnum(VoteEnum.YES);
        byte[] bytes = new byte[]{48, 69, 2, 33, 0, -100, -120, -52, 24, 16, -106, 88, 26, 88, 97, -96, 15, -17, 42, 32, 86, 101, 125, -68, -14, 65, -111, -3, 108, 22, -25, 35, -44, 62, -113, -80, -53, 2, 32, 120, 54, -3, 11, -114, -103, -111, 112, 76, 106, 46, -18, -116, 110, 85, -105, -22, -96, 81, 52, -102, -33, 99, -69, -70, 115, 27, -124, -98, 103, -48, 7};


        transaction.setSign(bytes);
        System.out.println("verify: " + transaction.verify());
        DtoTransaction transaction1 = UtilsJson.jsonToDtoTransaction("{\"sender\":\"faErFrDnBhfSfNnj1hYjxydKNH28cRw1PBwDQEXH3QsJ\",\"customer\":\"nNifuwmFZr7fnV1zvmpiyQDV5z7ETWvqR6GSeqeHTY43\",\"digitalDollar\":27.84,\"digitalStockBalance\":27.84,\"laws\":{\"packetLawName\":null,\"laws\":null,\"hashLaw\":null},\"bonusForMiner\":0.0,\"voteEnum\":\"YES\",\"sign\":\"MEUCIQCciMwYEJZYGlhhoA/vKiBWZX288kGR/WwW5yPUPo+wywIgeDb9C46ZkXBMai7ujG5Vl+qgUTSa32O7unMbhJ5n0Ac=\"}");

        System.out.println("verfiy: 2: " + transaction1.verify());
        System.out.println(transaction);

    }

    @Test
    public void hash() throws IOException, NoSuchAlgorithmException, SignatureException, InvalidKeySpecException, NoSuchProviderException, InvalidKeyException {
        Block block = UtilsJson.jsonToBLock("{\"dtoTransactions\":[{\"sender\":\"h392yDGLyzh4Bk3A8hfGvi8qiGK84X4TWWTSTe55jMM4\",\"customer\":\"m2iC2FBEcqNa4PPghE45AaAuXjeAC7f1SfanM4LMjQkM\",\"digitalDollar\":0.1818181818,\"digitalStockBalance\":0.1818181818,\"laws\":{\"packetLawName\":\"\",\"laws\":[],\"hashLaw\":\"\"},\"bonusForMiner\":0.0,\"voteEnum\":\"YES\",\"sign\":\"MEUCIHWb5voJj8D5PgHAjhTbRR4NzjJ81e2qalfkIQRTSIeyAiEAsGRdj9UOcHP6AA769GxXbJzIAWxWCwRVzFb5hEhvO0M=\"},{\"sender\":\"h392yDGLyzh4Bk3A8hfGvi8qiGK84X4TWWTSTe55jMM4\",\"customer\":\"wCfTQCrCs37ZNEShPBzPyjkZa1LjMUwnWn27dLQ8bj6o\",\"digitalDollar\":0.375,\"digitalStockBalance\":0.375,\"laws\":{\"packetLawName\":\"\",\"laws\":[],\"hashLaw\":\"\"},\"bonusForMiner\":0.0,\"voteEnum\":\"YES\",\"sign\":\"MEQCIB2Uk7VL0WbT3kIYmqpyY8TE/9+ttqt0yijMuxeLMSX7AiAAhdU9Ecd8i5PDDDb8BiBOL5MUaa7SwA1pPHLoMpjiow==\"},{\"sender\":\"h392yDGLyzh4Bk3A8hfGvi8qiGK84X4TWWTSTe55jMM4\",\"customer\":\"eHacKrkFscdJe4tVNwSQznotMv2dDY2iXsgRG1HVLHB7\",\"digitalDollar\":0.3333333333,\"digitalStockBalance\":0.3333333333,\"laws\":{\"packetLawName\":\"\",\"laws\":[],\"hashLaw\":\"\"},\"bonusForMiner\":0.0,\"voteEnum\":\"YES\",\"sign\":\"MEQCIBANj/gvJEXcib8mazHXHoabUPcJL2kYeIrLmvMCcteiAiBMSzYL5m1C5U1swKH8US3GzxSjplYz6c5hgxois5oBqg==\"},{\"sender\":\"h392yDGLyzh4Bk3A8hfGvi8qiGK84X4TWWTSTe55jMM4\",\"customer\":\"gC49xWaTVtQVKamqM7Neq3F7UGyqma3aXHJ5ep6WwcdY\",\"digitalDollar\":0.2222222222,\"digitalStockBalance\":0.2222222222,\"laws\":{\"packetLawName\":\"\",\"laws\":[],\"hashLaw\":\"\"},\"bonusForMiner\":0.0,\"voteEnum\":\"YES\",\"sign\":\"MEYCIQDmByNyAUmbslorqQHXxdaZ4uNIbGpISob9ZVZZWMFNGQIhAJ7x0fzEyY1TDYdk5yG5GfmLoEXchDqUljnzO34AgXx/\"},{\"sender\":\"h392yDGLyzh4Bk3A8hfGvi8qiGK84X4TWWTSTe55jMM4\",\"customer\":\"gC49xWaTVtQVKamqM7Neq3F7UGyqma3aXHJ5ep6WwcdY\",\"digitalDollar\":0.1428571429,\"digitalStockBalance\":0.1428571429,\"laws\":{\"packetLawName\":\"\",\"laws\":[],\"hashLaw\":\"\"},\"bonusForMiner\":0.0,\"voteEnum\":\"YES\",\"sign\":\"MEYCIQD7uAbvePL9AaxuO4CmmfY9uGkYQ5aS2YjHqOzMqjiqeQIhAMX/kaVP1UAOijyJZ59PlVwHH4+0S+ZbYXBhK+WoMh/U\"},{\"sender\":\"h392yDGLyzh4Bk3A8hfGvi8qiGK84X4TWWTSTe55jMM4\",\"customer\":\"wCfTQCrCs37ZNEShPBzPyjkZa1LjMUwnWn27dLQ8bj6o\",\"digitalDollar\":0.125,\"digitalStockBalance\":0.125,\"laws\":{\"packetLawName\":\"\",\"laws\":[],\"hashLaw\":\"\"},\"bonusForMiner\":0.0,\"voteEnum\":\"YES\",\"sign\":\"MEYCIQD6oeNLSlXz5QKTsEamaIB/BAD1k/Iw6+NhsTdIKn0hJAIhALIrVxOfMS75gzYh13+UTR/HEyj16EuWCmjpN+ol7PVS\"},{\"sender\":\"h392yDGLyzh4Bk3A8hfGvi8qiGK84X4TWWTSTe55jMM4\",\"customer\":\"gC49xWaTVtQVKamqM7Neq3F7UGyqma3aXHJ5ep6WwcdY\",\"digitalDollar\":0.2,\"digitalStockBalance\":0.2,\"laws\":{\"packetLawName\":\"\",\"laws\":[],\"hashLaw\":\"\"},\"bonusForMiner\":0.0,\"voteEnum\":\"YES\",\"sign\":\"MEYCIQDlnBFbOHXFGVRVT177ep4GdzmgImn4ZhYEHdlCDL+FqAIhAPbdWPk/V5tdaABDinqtgsgbvwRycfVvOluM0dEWobs9\"},{\"sender\":\"h392yDGLyzh4Bk3A8hfGvi8qiGK84X4TWWTSTe55jMM4\",\"customer\":\"gC49xWaTVtQVKamqM7Neq3F7UGyqma3aXHJ5ep6WwcdY\",\"digitalDollar\":0.3,\"digitalStockBalance\":0.3,\"laws\":{\"packetLawName\":\"\",\"laws\":[],\"hashLaw\":\"\"},\"bonusForMiner\":0.0,\"voteEnum\":\"YES\",\"sign\":\"MEUCIF992QNkfmdQbbwpNtf5deRVrgnvdaU9g1CM36cYbNlyAiEAsmjZMotPwuHNyQE4oM66IXlVsxeUYA6mYXJBAZX8LiE=\"},{\"sender\":\"h392yDGLyzh4Bk3A8hfGvi8qiGK84X4TWWTSTe55jMM4\",\"customer\":\"274NjHBzcVSBYKHN6dmHVADJ3bjnfaDkYyrDT25aLig7m\",\"digitalDollar\":0.2857142857,\"digitalStockBalance\":0.2857142857,\"laws\":{\"packetLawName\":\"\",\"laws\":[],\"hashLaw\":\"\"},\"bonusForMiner\":0.0,\"voteEnum\":\"YES\",\"sign\":\"MEQCICO+dC/uAgKKAzG7yadYT8aVyD5XXGRemL687ESdZRetAiBa8jVuolOGAcIVKBSXxkm1gx1yueUxNqC2tVlbrC7YEQ==\"},{\"sender\":\"h392yDGLyzh4Bk3A8hfGvi8qiGK84X4TWWTSTe55jMM4\",\"customer\":\"m2iC2FBEcqNa4PPghE45AaAuXjeAC7f1SfanM4LMjQkM\",\"digitalDollar\":0.2222222222,\"digitalStockBalance\":0.2222222222,\"laws\":{\"packetLawName\":\"\",\"laws\":[],\"hashLaw\":\"\"},\"bonusForMiner\":0.0,\"voteEnum\":\"YES\",\"sign\":\"MEUCIQD27Am6Pj3oZjWomzgLXMHQatNpG7YJLRJmlJLG2n+Z2AIgGz/WzLT33FrkBDZutlnSCAB1mfXd1yrC8H1k07x46wI=\"},{\"sender\":\"h392yDGLyzh4Bk3A8hfGvi8qiGK84X4TWWTSTe55jMM4\",\"customer\":\"274NjHBzcVSBYKHN6dmHVADJ3bjnfaDkYyrDT25aLig7m\",\"digitalDollar\":0.125,\"digitalStockBalance\":0.125,\"laws\":{\"packetLawName\":\"\",\"laws\":[],\"hashLaw\":\"\"},\"bonusForMiner\":0.0,\"voteEnum\":\"YES\",\"sign\":\"MEUCIQCngg4JsnXwYLvijt0mlQyuDLUuNsA4fhDDV+EIBVAURwIgfvO9LulhXjo4gyggCvWRxLQzAfnAoxrUV0jOBF7+w4o=\"},{\"sender\":\"h392yDGLyzh4Bk3A8hfGvi8qiGK84X4TWWTSTe55jMM4\",\"customer\":\"gC49xWaTVtQVKamqM7Neq3F7UGyqma3aXHJ5ep6WwcdY\",\"digitalDollar\":0.2727272727,\"digitalStockBalance\":0.2727272727,\"laws\":{\"packetLawName\":\"\",\"laws\":[],\"hashLaw\":\"\"},\"bonusForMiner\":0.0,\"voteEnum\":\"YES\",\"sign\":\"MEUCIB2Z6LKCQmV8eQXYsTzS6dDacJI+FP+AjgXRpZjRtgZdAiEAnuKAMdobl4AyKESR838WxmakXMiBNAWgJnXEDhMQxkE=\"},{\"sender\":\"h392yDGLyzh4Bk3A8hfGvi8qiGK84X4TWWTSTe55jMM4\",\"customer\":\"274NjHBzcVSBYKHN6dmHVADJ3bjnfaDkYyrDT25aLig7m\",\"digitalDollar\":0.2857142857,\"digitalStockBalance\":0.2857142857,\"laws\":{\"packetLawName\":\"\",\"laws\":[],\"hashLaw\":\"\"},\"bonusForMiner\":0.0,\"voteEnum\":\"YES\",\"sign\":\"MEUCIAiDpHPK4+oTGbXO3aaeM0ShIRMVisJALU7aIxeCwA2aAiEAz0sBLWb/jLKJmTGnwM9zTuzVGLrfHpWekcZyV+jvx+c=\"},{\"sender\":\"h392yDGLyzh4Bk3A8hfGvi8qiGK84X4TWWTSTe55jMM4\",\"customer\":\"eHacKrkFscdJe4tVNwSQznotMv2dDY2iXsgRG1HVLHB7\",\"digitalDollar\":0.1111111111,\"digitalStockBalance\":0.1111111111,\"laws\":{\"packetLawName\":\"\",\"laws\":[],\"hashLaw\":\"\"},\"bonusForMiner\":0.0,\"voteEnum\":\"YES\",\"sign\":\"MEUCICT0CNS3u86jtyY8PwudsdMXludJrhqPEYVhfZyg1LfiAiEArCF+XBOSi0T/36BtHqS16KEWXNpZBerEkDod2puxwu4=\"},{\"sender\":\"h392yDGLyzh4Bk3A8hfGvi8qiGK84X4TWWTSTe55jMM4\",\"customer\":\"gC49xWaTVtQVKamqM7Neq3F7UGyqma3aXHJ5ep6WwcdY\",\"digitalDollar\":0.3636363636,\"digitalStockBalance\":0.3636363636,\"laws\":{\"packetLawName\":\"\",\"laws\":[],\"hashLaw\":\"\"},\"bonusForMiner\":0.0,\"voteEnum\":\"YES\",\"sign\":\"MEYCIQC855htlhaOie87d3TEreB/pkrPfXkUyJwNik0K4VSRVgIhALpnoC8al0pzcghHYo83M8vPLQ3DjHNOFgvjqDfEJQw/\"},{\"sender\":\"faErFrDnBhfSfNnj1hYjxydKNH28cRw1PBwDQEXH3QsJ\",\"customer\":\"nNifuwmFZr7fnV1zvmpiyQDV5z7ETWvqR6GSeqeHTY43\",\"digitalDollar\":33.06,\"digitalStockBalance\":33.06,\"laws\":{\"packetLawName\":null,\"laws\":null,\"hashLaw\":null},\"bonusForMiner\":0.0,\"voteEnum\":\"YES\",\"sign\":\"MEUCIQCt/yj8SmgpX1oH/wVh65qyuwFZ19qqjrt+GiHhmupQOwIgMscJSLyCiuJazuvVgFeU65KjhK4QQKSR0i0odJVFgaY=\"},{\"sender\":\"faErFrDnBhfSfNnj1hYjxydKNH28cRw1PBwDQEXH3QsJ\",\"customer\":\"rDqx8hhZRzNm6xxvL1GL5aWyYoQRKVdjEHqDo5PY2nbM\",\"digitalDollar\":330.6,\"digitalStockBalance\":330.6,\"laws\":{\"packetLawName\":null,\"laws\":null,\"hashLaw\":null},\"bonusForMiner\":0.0,\"voteEnum\":\"YES\",\"sign\":\"MEQCIGZAp+OlIFhLr6AmS0jwPTSVIjEeWad4LgtH4oCW4KjdAiAF6pA2FYRaz+DD23ARIxlikwfgi0FvVFjQ7xp/9yr6eg==\"}],\"previousHash\":\"a083d84224200809c12c94b8148cf3375008d110188811a9424a051129251302\",\"minerAddress\":\"rDqx8hhZRzNm6xxvL1GL5aWyYoQRKVdjEHqDo5PY2nbM\",\"founderAddress\":\"nNifuwmFZr7fnV1zvmpiyQDV5z7ETWvqR6GSeqeHTY43\",\"randomNumberProof\":6305039483596012,\"minerRewards\":0.0,\"hashCompexity\":17,\"timestamp\":1722271666000,\"index\":286893,\"hashBlock\":\"35082585038c1e6910400918b88118c06954a00c80454192a8a2810689e81894\"}");
        System.out.println(block.getHashBlock().equals(block.hashForTransaction()));
        System.out.println("index: " + block.getIndex());
        System.out.println("hash1: " + block.getHashBlock());
        System.out.println("hash2: " + block.hashForTransaction());



    }

 }
