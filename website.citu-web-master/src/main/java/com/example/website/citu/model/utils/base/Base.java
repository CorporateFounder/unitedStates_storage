package com.example.website.citu.model.utils.base;

public interface Base {
    String encode(byte[] input);
    byte[] decode(String input);
}
