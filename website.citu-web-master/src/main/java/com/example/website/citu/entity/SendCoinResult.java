package com.example.website.citu.entity;


import com.example.website.citu.model.DtoTransaction;
import lombok.Data;

import java.util.HashMap;
import java.util.Map;

@Data
public class SendCoinResult {
    DtoTransaction dtoTransaction;
    Map<String, String> statusServer;
    String sign;

    public SendCoinResult() {
        statusServer = new HashMap<>();
    }

    public void put(String str, String status){
        statusServer.put(str, status);
    }
}
