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
    public void getTransactionCountTest() throws IOException, JSONException, NoSuchAlgorithmException, SignatureException, InvalidKeySpecException, NoSuchProviderException, InvalidKeyException {
        Base base = new Base58();
        Block server = UtilsJson.jsonToBLock("{\"dtoTransactions\":[{\"sender\":\"h392yDGLyzh4Bk3A8hfGvi8qiGK84X4TWWTSTe55jMM4\",\"customer\":\"274NjHBzcVSBYKHN6dmHVADJ3bjnfaDkYyrDT25aLig7m\",\"digitalDollar\":0.09,\"digitalStockBalance\":0.09,\"laws\":{\"packetLawName\":\"\",\"laws\":[],\"hashLaw\":\"\"},\"bonusForMiner\":0.0,\"voteEnum\":\"YES\",\"sign\":\"MEQCICTpBDpvFdSD6t9bT23QzbD5XgQ9oPow2CDhJ3AlCSPIAiANu0QXJHghfQOIEEI6bgf9q+7X8LvpBcoewHx4H0d2Lw==\"},{\"sender\":\"h392yDGLyzh4Bk3A8hfGvi8qiGK84X4TWWTSTe55jMM4\",\"customer\":\"eHacKrkFscdJe4tVNwSQznotMv2dDY2iXsgRG1HVLHB7\",\"digitalDollar\":0.2,\"digitalStockBalance\":0.2,\"laws\":{\"packetLawName\":\"\",\"laws\":[],\"hashLaw\":\"\"},\"bonusForMiner\":0.0,\"voteEnum\":\"YES\",\"sign\":\"MEQCICw+jziUiyjO75TFIMg25pjGkv6dXVZdSqfSeLUzhQ34AiBWY+/HP3EhUDIrHDsqPi5sftkws+eLL5cB1mo+HO1qGg==\"},{\"sender\":\"h392yDGLyzh4Bk3A8hfGvi8qiGK84X4TWWTSTe55jMM4\",\"customer\":\"eHacKrkFscdJe4tVNwSQznotMv2dDY2iXsgRG1HVLHB7\",\"digitalDollar\":0.14,\"digitalStockBalance\":0.14,\"laws\":{\"packetLawName\":\"\",\"laws\":[],\"hashLaw\":\"\"},\"bonusForMiner\":0.0,\"voteEnum\":\"YES\",\"sign\":\"MEQCIHLEyJfdaBkKURA9VzDwNZaJiQwNPr/O1hdYd7Ny4VhEAiBTEmPI+qcBg/U9yBxO8ikEr8IReKnm5vvRFlk6xmx/Ew==\"},{\"sender\":\"h392yDGLyzh4Bk3A8hfGvi8qiGK84X4TWWTSTe55jMM4\",\"customer\":\"m2iC2FBEcqNa4PPghE45AaAuXjeAC7f1SfanM4LMjQkM\",\"digitalDollar\":0.3,\"digitalStockBalance\":0.3,\"laws\":{\"packetLawName\":\"\",\"laws\":[],\"hashLaw\":\"\"},\"bonusForMiner\":0.0,\"voteEnum\":\"YES\",\"sign\":\"MEUCIA73+tDloVLErRepnYbeuHA76BGqukunKUeFyce4UsxwAiEA9bZ7NYRhHDQkdVZIZddJ80GO1jrJemI0DJe/JvOfb7g=\"},{\"sender\":\"h392yDGLyzh4Bk3A8hfGvi8qiGK84X4TWWTSTe55jMM4\",\"customer\":\"274NjHBzcVSBYKHN6dmHVADJ3bjnfaDkYyrDT25aLig7m\",\"digitalDollar\":0.3,\"digitalStockBalance\":0.3,\"laws\":{\"packetLawName\":\"\",\"laws\":[],\"hashLaw\":\"\"},\"bonusForMiner\":0.0,\"voteEnum\":\"YES\",\"sign\":\"MEUCIERL9cAnRE86RfHcEEx9w9Tc7mFcGnmdj9852MdkYC1RAiEAy83yeCxwFTcQG81G+6oOMBkJdg3G6LSUppNHBsQdE3o=\"},{\"sender\":\"h392yDGLyzh4Bk3A8hfGvi8qiGK84X4TWWTSTe55jMM4\",\"customer\":\"eHacKrkFscdJe4tVNwSQznotMv2dDY2iXsgRG1HVLHB7\",\"digitalDollar\":0.44,\"digitalStockBalance\":0.44,\"laws\":{\"packetLawName\":\"\",\"laws\":[],\"hashLaw\":\"\"},\"bonusForMiner\":0.0,\"voteEnum\":\"YES\",\"sign\":\"MEUCIE52gq9VInIUb8IK2NICKd/62aPHIQQWtNb6ugE24MnZAiEAoiYKHOH2wveITIHQy9nWSi1CWfgVJMwPynadcX2TAf8=\"},{\"sender\":\"h392yDGLyzh4Bk3A8hfGvi8qiGK84X4TWWTSTe55jMM4\",\"customer\":\"gC49xWaTVtQVKamqM7Neq3F7UGyqma3aXHJ5ep6WwcdY\",\"digitalDollar\":0.28,\"digitalStockBalance\":0.28,\"laws\":{\"packetLawName\":\"\",\"laws\":[],\"hashLaw\":\"\"},\"bonusForMiner\":0.0,\"voteEnum\":\"YES\",\"sign\":\"MEUCIFXf9F6yfM11caRCv4Z4QmfPmkX1sS0fboJKVawbOtcxAiEA4XyKnOtvkebinno5A/J1xy38LCqd/kD1ImGac6UgNhM=\"},{\"sender\":\"h392yDGLyzh4Bk3A8hfGvi8qiGK84X4TWWTSTe55jMM4\",\"customer\":\"eHacKrkFscdJe4tVNwSQznotMv2dDY2iXsgRG1HVLHB7\",\"digitalDollar\":0.42,\"digitalStockBalance\":0.42,\"laws\":{\"packetLawName\":\"\",\"laws\":[],\"hashLaw\":\"\"},\"bonusForMiner\":0.0,\"voteEnum\":\"YES\",\"sign\":\"MEUCIGNgZWgMnHTwx4hqOMWkjJrHcH93AGd9Nxn8pd7+Pq/zAiEAykQ5Ma1jZqOdP1N+HzityHtG8UAlA0jxRx/35lwemus=\"},{\"sender\":\"h392yDGLyzh4Bk3A8hfGvi8qiGK84X4TWWTSTe55jMM4\",\"customer\":\"gC49xWaTVtQVKamqM7Neq3F7UGyqma3aXHJ5ep6WwcdY\",\"digitalDollar\":0.09,\"digitalStockBalance\":0.09,\"laws\":{\"packetLawName\":\"\",\"laws\":[],\"hashLaw\":\"\"},\"bonusForMiner\":0.0,\"voteEnum\":\"YES\",\"sign\":\"MEUCIQCLZ7KfMuZOCQEXkZZym++EqAjQ0PocAj4omNZ7yDEZFgIgcyPgOsBpFwSU+X15B8jm735GYvFvecvRYLcwBrDM4ew=\"},{\"sender\":\"faErFrDnBhfSfNnj1hYjxydKNH28cRw1PBwDQEXH3QsJ\",\"customer\":\"nNifuwmFZr7fnV1zvmpiyQDV5z7ETWvqR6GSeqeHTY43\",\"digitalDollar\":24.36,\"digitalStockBalance\":24.36,\"laws\":{\"packetLawName\":null,\"laws\":null,\"hashLaw\":null},\"bonusForMiner\":0.0,\"voteEnum\":\"YES\",\"sign\":\"MEUCIQDsytYjCaj2D+bQYXomv57D232ZTRLZ241WOoB1cIZX1gIgFb/DkLQJ6sFJXrEl0/i0T6CaiDLNOzVhfBf/napP9p4=\"},{\"sender\":\"faErFrDnBhfSfNnj1hYjxydKNH28cRw1PBwDQEXH3QsJ\",\"customer\":\"21Qsp2EjJhYhqP1fnWm6UufnEtavocHXJbS8MhAV9UKwJ\",\"digitalDollar\":243.6,\"digitalStockBalance\":243.6,\"laws\":{\"packetLawName\":null,\"laws\":null,\"hashLaw\":null},\"bonusForMiner\":0.0,\"voteEnum\":\"YES\",\"sign\":\"MEYCIQCNfUnvlZeW3Uf7nummgYXTDk7YEztdYt0mgiOSAgWS7gIhAP9awY1Fkm2LNLgo7ZhRq8MNRFrct+wVOODn5plGsvHa\"},{\"sender\":\"h392yDGLyzh4Bk3A8hfGvi8qiGK84X4TWWTSTe55jMM4\",\"customer\":\"274NjHBzcVSBYKHN6dmHVADJ3bjnfaDkYyrDT25aLig7m\",\"digitalDollar\":0.11,\"digitalStockBalance\":0.11,\"laws\":{\"packetLawName\":\"\",\"laws\":[],\"hashLaw\":\"\"},\"bonusForMiner\":0.0,\"voteEnum\":\"YES\",\"sign\":\"MEYCIQCbs1f246EobdZcrP5Y9gfFDnFtMEMoDjJ4unNrv7I4qQIhAP046DxjerwMeOKC7+q1u2EFaJYIKUynXDxZ1suMlHZR\"}],\"previousHash\":\"18004e9c2c010021011c8943902320ca80456023e29088a4e240ac2208036d10\",\"minerAddress\":\"21Qsp2EjJhYhqP1fnWm6UufnEtavocHXJbS8MhAV9UKwJ\",\"founderAddress\":\"nNifuwmFZr7fnV1zvmpiyQDV5z7ETWvqR6GSeqeHTY43\",\"randomNumberProof\":16264312,\"minerRewards\":0.0,\"hashCompexity\":17,\"timestamp\":1726515524000,\"index\":303805,\"hashBlock\":\"b2a31635941008008802623070027203a217c521c980c3e82325900a0211a112\"}");
        Block wallet = UtilsJson.jsonToBLock("{\"dtoTransactions\":[{\"sender\":\"h392yDGLyzh4Bk3A8hfGvi8qiGK84X4TWWTSTe55jMM4\",\"customer\":\"274NjHBzcVSBYKHN6dmHVADJ3bjnfaDkYyrDT25aLig7m\",\"digitalDollar\":0.09,\"digitalStockBalance\":0.09,\"laws\":{\"packetLawName\":\"\",\"laws\":[],\"hashLaw\":\"\"},\"bonusForMiner\":0.0,\"voteEnum\":\"YES\",\"sign\":\"MEQCICTpBDpvFdSD6t9bT23QzbD5XgQ9oPow2CDhJ3AlCSPIAiANu0QXJHghfQOIEEI6bgf9q+7X8LvpBcoewHx4H0d2Lw==\"},{\"sender\":\"h392yDGLyzh4Bk3A8hfGvi8qiGK84X4TWWTSTe55jMM4\",\"customer\":\"eHacKrkFscdJe4tVNwSQznotMv2dDY2iXsgRG1HVLHB7\",\"digitalDollar\":0.2,\"digitalStockBalance\":0.2,\"laws\":{\"packetLawName\":\"\",\"laws\":[],\"hashLaw\":\"\"},\"bonusForMiner\":0.0,\"voteEnum\":\"YES\",\"sign\":\"MEQCICw+jziUiyjO75TFIMg25pjGkv6dXVZdSqfSeLUzhQ34AiBWY+/HP3EhUDIrHDsqPi5sftkws+eLL5cB1mo+HO1qGg==\"},{\"sender\":\"h392yDGLyzh4Bk3A8hfGvi8qiGK84X4TWWTSTe55jMM4\",\"customer\":\"eHacKrkFscdJe4tVNwSQznotMv2dDY2iXsgRG1HVLHB7\",\"digitalDollar\":0.14,\"digitalStockBalance\":0.14,\"laws\":{\"packetLawName\":\"\",\"laws\":[],\"hashLaw\":\"\"},\"bonusForMiner\":0.0,\"voteEnum\":\"YES\",\"sign\":\"MEQCIHLEyJfdaBkKURA9VzDwNZaJiQwNPr/O1hdYd7Ny4VhEAiBTEmPI+qcBg/U9yBxO8ikEr8IReKnm5vvRFlk6xmx/Ew==\"},{\"sender\":\"h392yDGLyzh4Bk3A8hfGvi8qiGK84X4TWWTSTe55jMM4\",\"customer\":\"m2iC2FBEcqNa4PPghE45AaAuXjeAC7f1SfanM4LMjQkM\",\"digitalDollar\":0.3,\"digitalStockBalance\":0.3,\"laws\":{\"packetLawName\":\"\",\"laws\":[],\"hashLaw\":\"\"},\"bonusForMiner\":0.0,\"voteEnum\":\"YES\",\"sign\":\"MEUCIA73+tDloVLErRepnYbeuHA76BGqukunKUeFyce4UsxwAiEA9bZ7NYRhHDQkdVZIZddJ80GO1jrJemI0DJe/JvOfb7g=\"},{\"sender\":\"h392yDGLyzh4Bk3A8hfGvi8qiGK84X4TWWTSTe55jMM4\",\"customer\":\"274NjHBzcVSBYKHN6dmHVADJ3bjnfaDkYyrDT25aLig7m\",\"digitalDollar\":0.3,\"digitalStockBalance\":0.3,\"laws\":{\"packetLawName\":\"\",\"laws\":[],\"hashLaw\":\"\"},\"bonusForMiner\":0.0,\"voteEnum\":\"YES\",\"sign\":\"MEUCIERL9cAnRE86RfHcEEx9w9Tc7mFcGnmdj9852MdkYC1RAiEAy83yeCxwFTcQG81G+6oOMBkJdg3G6LSUppNHBsQdE3o=\"},{\"sender\":\"h392yDGLyzh4Bk3A8hfGvi8qiGK84X4TWWTSTe55jMM4\",\"customer\":\"eHacKrkFscdJe4tVNwSQznotMv2dDY2iXsgRG1HVLHB7\",\"digitalDollar\":0.44,\"digitalStockBalance\":0.44,\"laws\":{\"packetLawName\":\"\",\"laws\":[],\"hashLaw\":\"\"},\"bonusForMiner\":0.0,\"voteEnum\":\"YES\",\"sign\":\"MEUCIE52gq9VInIUb8IK2NICKd/62aPHIQQWtNb6ugE24MnZAiEAoiYKHOH2wveITIHQy9nWSi1CWfgVJMwPynadcX2TAf8=\"},{\"sender\":\"h392yDGLyzh4Bk3A8hfGvi8qiGK84X4TWWTSTe55jMM4\",\"customer\":\"gC49xWaTVtQVKamqM7Neq3F7UGyqma3aXHJ5ep6WwcdY\",\"digitalDollar\":0.28,\"digitalStockBalance\":0.28,\"laws\":{\"packetLawName\":\"\",\"laws\":[],\"hashLaw\":\"\"},\"bonusForMiner\":0.0,\"voteEnum\":\"YES\",\"sign\":\"MEUCIFXf9F6yfM11caRCv4Z4QmfPmkX1sS0fboJKVawbOtcxAiEA4XyKnOtvkebinno5A/J1xy38LCqd/kD1ImGac6UgNhM=\"},{\"sender\":\"h392yDGLyzh4Bk3A8hfGvi8qiGK84X4TWWTSTe55jMM4\",\"customer\":\"eHacKrkFscdJe4tVNwSQznotMv2dDY2iXsgRG1HVLHB7\",\"digitalDollar\":0.42,\"digitalStockBalance\":0.42,\"laws\":{\"packetLawName\":\"\",\"laws\":[],\"hashLaw\":\"\"},\"bonusForMiner\":0.0,\"voteEnum\":\"YES\",\"sign\":\"MEUCIGNgZWgMnHTwx4hqOMWkjJrHcH93AGd9Nxn8pd7+Pq/zAiEAykQ5Ma1jZqOdP1N+HzityHtG8UAlA0jxRx/35lwemus=\"},{\"sender\":\"h392yDGLyzh4Bk3A8hfGvi8qiGK84X4TWWTSTe55jMM4\",\"customer\":\"gC49xWaTVtQVKamqM7Neq3F7UGyqma3aXHJ5ep6WwcdY\",\"digitalDollar\":0.09,\"digitalStockBalance\":0.09,\"laws\":{\"packetLawName\":\"\",\"laws\":[],\"hashLaw\":\"\"},\"bonusForMiner\":0.0,\"voteEnum\":\"YES\",\"sign\":\"MEUCIQCLZ7KfMuZOCQEXkZZym++EqAjQ0PocAj4omNZ7yDEZFgIgcyPgOsBpFwSU+X15B8jm735GYvFvecvRYLcwBrDM4ew=\"},{\"sender\":\"faErFrDnBhfSfNnj1hYjxydKNH28cRw1PBwDQEXH3QsJ\",\"customer\":\"nNifuwmFZr7fnV1zvmpiyQDV5z7ETWvqR6GSeqeHTY43\",\"digitalDollar\":24.36,\"digitalStockBalance\":24.36,\"laws\":{\"packetLawName\":null,\"laws\":null,\"hashLaw\":null},\"bonusForMiner\":0.0,\"voteEnum\":\"YES\",\"sign\":\"MEUCIQDsytYjCaj2D+bQYXomv57D232ZTRLZ241WOoB1cIZX1gIgFb/DkLQJ6sFJXrEl0/i0T6CaiDLNOzVhfBf/napP9p4=\"},{\"sender\":\"faErFrDnBhfSfNnj1hYjxydKNH28cRw1PBwDQEXH3QsJ\",\"customer\":\"21Qsp2EjJhYhqP1fnWm6UufnEtavocHXJbS8MhAV9UKwJ\",\"digitalDollar\":243.6,\"digitalStockBalance\":243.6,\"laws\":{\"packetLawName\":null,\"laws\":null,\"hashLaw\":null},\"bonusForMiner\":0.0,\"voteEnum\":\"YES\",\"sign\":\"MEYCIQCNfUnvlZeW3Uf7nummgYXTDk7YEztdYt0mgiOSAgWS7gIhAP9awY1Fkm2LNLgo7ZhRq8MNRFrct+wVOODn5plGsvHa\"},{\"sender\":\"h392yDGLyzh4Bk3A8hfGvi8qiGK84X4TWWTSTe55jMM4\",\"customer\":\"274NjHBzcVSBYKHN6dmHVADJ3bjnfaDkYyrDT25aLig7m\",\"digitalDollar\":0.11,\"digitalStockBalance\":0.11,\"laws\":{\"packetLawName\":\"\",\"laws\":[],\"hashLaw\":\"\"},\"bonusForMiner\":0.0,\"voteEnum\":\"YES\",\"sign\":\"MEYCIQCbs1f246EobdZcrP5Y9gfFDnFtMEMoDjJ4unNrv7I4qQIhAP046DxjerwMeOKC7+q1u2EFaJYIKUynXDxZ1suMlHZR\"}],\"previousHash\":\"18004e9c2c010021011c8943902320ca80456023e29088a4e240ac2208036d10\",\"minerAddress\":\"21Qsp2EjJhYhqP1fnWm6UufnEtavocHXJbS8MhAV9UKwJ\",\"founderAddress\":\"nNifuwmFZr7fnV1zvmpiyQDV5z7ETWvqR6GSeqeHTY43\",\"randomNumberProof\":16264312,\"minerRewards\":0.0,\"hashCompexity\":17,\"timestamp\":1726515524000,\"index\":303805,\"hashBlock\":\"b2a31635941008008802623070027203a217c521c980c3e82325900a0211a112\"}");
        System.out.println("block equals: "+server.equals(wallet));
        System.out.println(server.getMinerAddress());
        System.out.println("block index: " + server.getIndex());
        for (int i = 0; i < server.getDtoTransactions().size(); i++) {
            DtoTransaction serverDto = server.getDtoTransactions().get(i);
            DtoTransaction walletDto = wallet.getDtoTransactions().get(i);
            System.out.println("-----------------------------");

            System.out.println("server: digital: " + serverDto.getDigitalDollar() + " sender " + serverDto.getSender() + " customer " + serverDto.getCustomer() + " vote: " + serverDto.getVoteEnum() + " sign: " + base.encode(serverDto.getSign()));
            System.out.println("wallet: digital: " + walletDto.getDigitalDollar() + " sender " + walletDto.getSender() + " customer " + walletDto.getCustomer() + " vote: " + walletDto.getVoteEnum()+ " sign: " + base.encode(walletDto.getSign()));

            System.out.println("-----------------------------");

        }
        // Создаем экземпляры Account с заданными балансами
        Account account1 = new Account(
                "zraD2LuUah83KXTtYP33pwbUuS62dmp21CxPYXDNGWqW",
                BigDecimal.valueOf(111626.40),
                BigDecimal.valueOf(145626.40),
                BigDecimal.valueOf(34000.00)
        );

        Account account2 = new Account(
                "h392yDGLyzh4Bk3A8hfGvi8qiGK84X4TWWTSTe55jMM4",
                BigDecimal.valueOf(64222.99),
                BigDecimal.valueOf(64222.99),
                BigDecimal.valueOf(0.00)
        );

        Account account3 = new Account(
                "gC49xWaTVtQVKamqM7Neq3F7UGyqma3aXHJ5ep6WwcdY",
                BigDecimal.valueOf(3577.10),
                BigDecimal.valueOf(3577.10),
                BigDecimal.valueOf(0.00)
        );

        Account account4 = new Account(
                "jKvrxZX8Wx8H4toXBW3axAWHPUEbDBoWBCcCkrNELZQz",
                BigDecimal.valueOf(64400.19),
                BigDecimal.valueOf(644327.19),
                BigDecimal.valueOf(1000000.00)
        );

        Account account5 = new Account(
                "2B2JAxRi6Uf12PGG6bYkkQKKNGC46zFr2dRhLXxFM4VwL",
                BigDecimal.valueOf(5667213.10),
                BigDecimal.valueOf(10067213.10),
                BigDecimal.valueOf(4400000.00)
        );

        Account account6 = new Account(
                "21Qsp2EjJhYhqP1fnWm6UufnEtavocHXJbS8MhAV9UKwJ",
                BigDecimal.valueOf(18744.80),
                BigDecimal.valueOf(418744.80),
                BigDecimal.valueOf(400000.00)
        );

        Account account7 = new Account(
                "m2iC2FBEcqNa4PPghE45AaAuXjeAC7f1SfanM4LMjQkM",
                BigDecimal.valueOf(313859.81),
                BigDecimal.valueOf(313859.81),
                BigDecimal.valueOf(0.00)
        );

        Account account8 = new Account(
                "qDWgdtEBzbSGs7AdUVLYEiBP5Bs7nb3m3L8GaBGgbSEP",
                BigDecimal.valueOf(557.25),
                BigDecimal.valueOf(270386.25),
                BigDecimal.valueOf(269229.00)
        );

        Account account9 = new Account(
                "faErFrDnBhfSfNnj1hYjxydKNH28cRw1PBwDQEXH3QsJ",
                BigDecimal.valueOf(0.00),
                BigDecimal.valueOf(0.00),
                BigDecimal.valueOf(0.00)
        );

        Account account10 = new Account(
                "274NjHBzcVSBYKHN6dmHVADJ3bjnfaDkYyrDT25aLig7m",
                BigDecimal.valueOf(3618.34),
                BigDecimal.valueOf(3618.34),
                BigDecimal.valueOf(0.00)
        );

        Account account11 = new Account(
                "nNifuwmFZr7fnV1zvmpiyQDV5z7ETWvqR6GSeqeHTY43",
                BigDecimal.valueOf(28325166.84),
                BigDecimal.valueOf(28325189.86),
                BigDecimal.valueOf(0.00)
        );

        Account account12 = new Account(
                "wCfTQCrCs37ZNEShPBzPyjkZa1LjMUwnWn27dLQ8bj6o",
                BigDecimal.valueOf(3679.33),
                BigDecimal.valueOf(3679.33),
                BigDecimal.valueOf(0.00)
        );

        Account account13 = new Account(
                "2A8vxijdyY5ST1WhLQan3N1P6wSdzBDo9VmEFhck9bArG",
                BigDecimal.valueOf(7995.41),
                BigDecimal.valueOf(1297995.41),
                BigDecimal.valueOf(1290000.00)
        );

        Account account14 = new Account(
                "28eNM7pFKzTgYb8WyUHgttJK9v9oV3TH4jYMxB1xXAK4o",
                BigDecimal.valueOf(22629.19),
                BigDecimal.valueOf(1418629.25),
                BigDecimal.valueOf(1426000.06)
        );

        Account account15 = new Account(
                "eHacKrkFscdJe4tVNwSQznotMv2dDY2iXsgRG1HVLHB7",
                BigDecimal.valueOf(309989.76),
                BigDecimal.valueOf(309989.76),
                BigDecimal.valueOf(0.00)
        );

        // Инициализируем Map<String, Account> balances и добавляем аккаунты
        Map<String, Account> balances = new HashMap<>();
        balances.put(account1.getAccount(), account1);
        balances.put(account2.getAccount(), account2);
        balances.put(account3.getAccount(), account3);
        balances.put(account4.getAccount(), account4);
        balances.put(account5.getAccount(), account5);
        balances.put(account6.getAccount(), account6);
        balances.put(account7.getAccount(), account7);
        balances.put(account8.getAccount(), account8);
        balances.put(account9.getAccount(), account9);
        balances.put(account10.getAccount(), account10);
        balances.put(account11.getAccount(), account11);
        balances.put(account12.getAccount(), account12);
        balances.put(account13.getAccount(), account13);
        balances.put(account14.getAccount(), account14);
        balances.put(account15.getAccount(), account15);

        Map<String, Account> different = UtilsBalance.calculateBalance(balances, wallet, new ArrayList<>(), new ArrayList<>());


        System.out.println("different");
        different.entrySet().stream().map(t->t.getValue()).forEach(System.out::println);


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
