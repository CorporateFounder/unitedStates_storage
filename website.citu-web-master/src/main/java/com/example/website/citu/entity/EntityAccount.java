package com.example.website.citu.entity;


import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;


import java.math.BigDecimal;
import java.util.Objects;

@Data
public class EntityAccount {
    private Long id;

    public void setId(Long id) {
        this.id = id;
    }

    public Long getId() {
        return id;
    }

    private String account;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "0.0000000000")
    private BigDecimal digitalDollarBalance;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "0.0000000000")
    private BigDecimal digitalStockBalance;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "0.0000000000")
    private BigDecimal digitalStakingBalance;

    public EntityAccount() {
    }

    public EntityAccount(String account,
                         BigDecimal digitalDollarBalance,
                         BigDecimal digitalStockBalance,
                         BigDecimal digitalStakingBalance) {
        this.account = account;
        this.digitalDollarBalance = digitalDollarBalance;
        this.digitalStockBalance = digitalStockBalance;
        this.digitalStakingBalance = digitalStakingBalance;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof EntityAccount)) return false;
        EntityAccount that = (EntityAccount) o;
        return getAccount().equals(that.getAccount());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getAccount());
    }
}
