package com.example.website.citu.entity;



import com.example.website.citu.model.VoteEnum;


import lombok.Getter;
import lombok.Setter;





@Getter
@Setter
public class EntityDtoTransaction {


    private Long id;


    public EntityDtoTransaction() {
    }


    public EntityDtoTransaction(String sender, String customer, double digitalDollar, double digitalStockBalance, EntityLaws entityLaws, double bonusForMiner, VoteEnum voteEnum, String sign) {

        this.sender = sender;
        this.customer = customer;
        this.digitalDollar = digitalDollar;
        this.digitalStockBalance = digitalStockBalance;
        this.entityLaws = entityLaws;
        this.bonusForMiner = bonusForMiner;
        this.voteEnum = voteEnum;
        this.sign = sign;
    }

    private String sender;
    private String customer;
    private double digitalDollar;
    private double digitalStockBalance;


    private EntityLaws entityLaws;

    private double bonusForMiner;
    private VoteEnum voteEnum;


    private String sign;



    private EntityBlock entityBlock;
    // Остальные поля класса

    // Геттеры и сеттеры для поля entityBlock
    public EntityBlock getEntityBlock() {
        return entityBlock;
    }

    public void setEntityBlock(EntityBlock entityBlock) {
        this.entityBlock = entityBlock;
    }

    @Override
    public String toString() {
        return "EntityDtoTransaction{" +
                "id=" + id +
                ", sender='" + sender + '\'' +
                ", customer='" + customer + '\'' +
                ", digitalDollar=" + digitalDollar +
                ", digitalStockBalance=" + digitalStockBalance +
                ", entityLaws=" + entityLaws +
                ", bonusForMiner=" + bonusForMiner +
                ", voteEnum=" + voteEnum +
                ", sign=" + sign +
                '}';
    }
}
