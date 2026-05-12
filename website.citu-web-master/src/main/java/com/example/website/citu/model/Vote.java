package com.example.website.citu.model;

import lombok.Data;

@Data
public class Vote {
    private String address;
    private double vote;

    public Vote() {
    }

    public Vote(String address, double vote) {
        this.address = address;
        this.vote = vote;
    }
}
