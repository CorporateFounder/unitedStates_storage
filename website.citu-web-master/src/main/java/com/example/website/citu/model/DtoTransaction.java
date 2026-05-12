package com.example.website.citu.model;

import com.example.website.citu.model.utils.base.Base;
import com.example.website.citu.model.utils.base.Base58;
import com.example.website.citu.utils.UtilsUse;
import com.fasterxml.jackson.annotation.JsonAutoDetect;
import lombok.Data;
import org.bouncycastle.jcajce.provider.asymmetric.ec.BCECPublicKey;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SignatureException;
import java.security.spec.InvalidKeySpecException;
import java.util.Arrays;
import java.util.Base64;
import java.util.Objects;


@JsonAutoDetect
@Data
public class DtoTransaction {
    private String sender;
    private String customer;
    private double digitalDollar;
    private double digitalStockBalance;
    private Laws laws;
    private double bonusForMiner;
    private VoteEnum voteEnum;
    private byte[] sign;
    private String signStr;

    public String hex(){
        return UtilsUse.bytesToHex(sign);
    }


    public DtoTransaction(String sender,
                          String customer,
                          double digitalDollar,
                          double digitalStockBalance,
                          Laws laws,
                          double bonusForMiner,
                          VoteEnum voteEnum,
                          byte[] sign) {
        this.sender = sender;
        this.customer = customer;
        this.digitalDollar = digitalDollar;
        this.digitalStockBalance = digitalStockBalance;
        this.laws = laws;
        this.bonusForMiner = bonusForMiner;
        this.voteEnum = voteEnum;
        this.sign = sign;

        this.signStr = Base64.getEncoder().encodeToString(sign);
    }

    public String getSignStr() {
        return Base64.getEncoder().encodeToString(sign);

    }

    public void setSignStr(String signStr) {
        this.signStr = signStr;
    }

    public DtoTransaction() {
    }

    //TODO возможно стоит перевести проверку подписи в отдельный utils, под вопросом!!


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof DtoTransaction)) return false;
        DtoTransaction that = (DtoTransaction) o;
        return Double.compare(that.getDigitalDollar(), getDigitalDollar()) == 0 && Double.compare(that.getDigitalStockBalance(), getDigitalStockBalance()) == 0 && Double.compare(that.getBonusForMiner(), getBonusForMiner()) == 0 && getSender().equals(that.getSender()) && getCustomer().equals(that.getCustomer()) && getLaws().equals(that.getLaws()) && getVoteEnum() == that.getVoteEnum() && Arrays.equals(getSign(), that.getSign());
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(getSender(), getCustomer(), getDigitalDollar(), getDigitalStockBalance(), getLaws(), getBonusForMiner(), getVoteEnum());
        result = 31 * result + Arrays.hashCode(getSign());
        return result;
    }
}
