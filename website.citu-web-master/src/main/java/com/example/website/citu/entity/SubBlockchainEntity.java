package com.example.website.citu.entity;

import lombok.Data;

@Data
public class SubBlockchainEntity {
    private int start;
    private int finish;

    public SubBlockchainEntity(int start, int finish) {
        this.start = start;
        this.finish = finish;
    }

    public SubBlockchainEntity() {
    }
}
