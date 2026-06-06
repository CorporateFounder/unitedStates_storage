package com.example.website.citu.controller;

import com.example.website.citu.entity.InfoDificultyBlockchain;
import com.example.website.citu.model.Block;
import com.example.website.citu.model.DtoTransaction;
import com.example.website.citu.model.StatusTransaction;
import com.example.website.citu.utils.UtilsJson;

import java.util.Collections;
import java.util.List;

final class ApiFallback {
    static final String UNAVAILABLE = "temporarily unavailable";

    private ApiFallback() {
    }

    static boolean isBlank(String value) {
        return value == null || value.isBlank() || UNAVAILABLE.equalsIgnoreCase(value.trim());
    }

    static int intValue(String value, int fallback) {
        try {
            return Integer.parseInt(clean(value));
        } catch (Exception e) {
            return fallback;
        }
    }

    static long longValue(String value, long fallback) {
        try {
            return Long.parseLong(clean(value));
        } catch (Exception e) {
            return fallback;
        }
    }

    static Block block(String json) {
        if (isBlank(json)) {
            return null;
        }
        try {
            return (Block) UtilsJson.jsonToClass(json, Block.class);
        } catch (Exception e) {
            return null;
        }
    }

    static StatusTransaction statusTransaction(String json) {
        if (isBlank(json)) {
            return null;
        }
        try {
            return (StatusTransaction) UtilsJson.jsonToClass(json, StatusTransaction.class);
        } catch (Exception e) {
            return null;
        }
    }

    static InfoDificultyBlockchain difficulty(String json) {
        if (isBlank(json)) {
            return new InfoDificultyBlockchain(-1, -1);
        }
        try {
            return UtilsJson.jsonToInfoDifficulty(json);
        } catch (Exception e) {
            return new InfoDificultyBlockchain(-1, -1);
        }
    }

    static List<Block> blocks(String json) {
        if (isBlank(json)) {
            return Collections.emptyList();
        }
        try {
            List<Block> blocks = UtilsJson.jsonToListBlock(json);
            return blocks == null ? Collections.emptyList() : blocks;
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }

    static List<Block> blockList(String json) {
        if (isBlank(json)) {
            return Collections.emptyList();
        }
        try {
            List<Block> blocks = UtilsJson.jsonToListBLock(json);
            return blocks == null ? Collections.emptyList() : blocks;
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }

    static List<DtoTransaction> transactions(String json) {
        if (isBlank(json)) {
            return Collections.emptyList();
        }
        try {
            List<DtoTransaction> transactions = UtilsJson.jsonToListDto(json);
            return transactions == null ? Collections.emptyList() : transactions;
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }

    private static String clean(String value) {
        return value == null ? "" : value.replace("\"", "").trim();
    }
}
