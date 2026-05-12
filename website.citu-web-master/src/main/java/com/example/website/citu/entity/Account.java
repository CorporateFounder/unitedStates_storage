package com.example.website.citu.entity;

import com.example.website.citu.model.DtoTransaction;
import com.example.website.citu.model.utils.base.Base;
import com.example.website.citu.model.utils.base.Base58;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.IOException;
import java.math.BigDecimal;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.util.Objects;

/**
 * Класс Аккаунт, хранит данные такие данные.
 * account - public key ECDSA
 * digitalDollarBalance - цифровой доллар (деньги).
 * digitalStockBalance - акции, используется для голосования.
 * digitalStakingBalance - сумма долларов которые зарезервированы для staking (pos).
 **/
@Data
public class Account implements Cloneable {
    private String account;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "0.0000000000")
    private BigDecimal digitalDollarBalance;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "0.0000000000")
    private BigDecimal digitalStockBalance;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "0.0000000000")
    private BigDecimal digitalStakingBalance;

    public Account(String account, BigDecimal digitalDollarBalance) {
        this(account, digitalDollarBalance, BigDecimal.ZERO, BigDecimal.ZERO);
    }

    public Account(String account, BigDecimal digitalDollarBalance, BigDecimal digitalStockBalance, BigDecimal digitalStakingBalance) {
        this.account = account;
        this.digitalDollarBalance = digitalDollarBalance;
        this.digitalStockBalance = digitalStockBalance;
        this.digitalStakingBalance = digitalStakingBalance;
    }

    public Account() {
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Account)) return false;
        Account account1 = (Account) o;
        return getAccount().equals(account1.getAccount());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getAccount());
    }




    @Override
    public Account clone() throws CloneNotSupportedException {
        return new Account(account, digitalDollarBalance, digitalStockBalance, digitalStakingBalance);
    }
}
