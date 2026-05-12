package com.example.website.citu.controller;

import com.example.website.citu.entity.Account;
import com.example.website.citu.entity.EntityBlock;
import com.example.website.citu.entity.SubBlockchainEntity;
import com.example.website.citu.model.Block;
import com.example.website.citu.model.LiteVersionWiner;
import com.example.website.citu.utils.UtilUrl;
import com.example.website.citu.utils.UtilsJson;
import com.example.website.citu.utils.UtilsTime;
import com.example.website.citu.utils.UtilsUse;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.bouncycastle.math.raw.Mod;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.example.website.citu.controller.WebController.address;

@Controller
public class WinnersController {

    List<String> raitings = new ArrayList<>();
    private long lastUpdateTime = 0L;

    @GetMapping("/winners")
    public String winners(Model model) throws JsonProcessingException {
        String allWiners = WebController.getResult("/allwinners", "error allWiners");
        List<LiteVersionWiner> allWinersList = UtilsJson.jsonToLiteVersionWiners(allWiners);
        model.addAttribute("allWiners", allWinersList);

        int uniq = allWinersList.stream().filter(UtilsUse.distinctByKey(LiteVersionWiner::getAddress)).collect(Collectors.toList()).size();
        model.addAttribute("uniq", uniq);
        model.addAttribute("allWinnerCount", allWinersList.size());

        String bigRandom = WebController.getResult("/bigRandomWiner", "error bigRandom");
        List<LiteVersionWiner> bigRandomList = UtilsJson.jsonToLiteVersionWiners(bigRandom);
        model.addAttribute("bigRandom", bigRandomList);
        model.addAttribute("bigRandomCount", bigRandomList.size());

        return "winners";
    }



    @GetMapping("/daily_rating")
    public String raitings(Model model){
        Integer size = -1;
        try {
            String sizeStr = UtilUrl.readJsonFromUrl(address + "/size");
            size = Integer.valueOf(sizeStr);
        }catch (Exception e){
            e.printStackTrace();
        }

        long currentTime = UtilsTime.getUniversalTimestamp();
        long timeElapsed = UtilsTime.differentMillSecondTime(lastUpdateTime, currentTime);

        // Проверка, нужно ли обновить рейтинг
        if(raitings.isEmpty() || timeElapsed >= 86400000L ){
            try{
                raitings = percent();
                lastUpdateTime = currentTime; // обновляем время последнего обновления
            }catch (Exception e){
                e.printStackTrace();
            }
        }

        // Вычисление времени до следующего обновления
        long timeRemaining = (86400000L - timeElapsed) / 1000; // Оставшееся время до следующего обновления в секундах

        model.addAttribute("time", timeRemaining);
        model.addAttribute("title", "daily_rating");
        model.addAttribute("day", (size/576));
        model.addAttribute("raitings", raitings);
        return "daily_rating";
    }



    public List<String> percent() throws IOException {
        List<String> result = new ArrayList<>();

        try {
            // Получить размер блокчейна
            String sizeStr = UtilUrl.readJsonFromUrl(address + "/size");
            if (sizeStr == null || sizeStr.isBlank()) {
                throw new IOException("Failed to retrieve blockchain size.");
            }

            Integer size = Integer.valueOf(sizeStr);

            // Создать запрос на подцепочку
            SubBlockchainEntity subBlockchainEntity = new SubBlockchainEntity(size - 576, size);
            String subBlockchainJson = UtilsJson.objToStringJson(subBlockchainEntity);

            // Получить подцепочку
            String str = UtilUrl.getObject(subBlockchainJson, address + "/sub-blocks");
            if (str == null || str.isBlank()) {
                System.out.println("Subblocks response is empty.");
                return result;
            }

            // Преобразовать JSON в список блоков
            List<Block> subBlocks = UtilsJson.jsonToListBLock(str);
            if (subBlocks == null || subBlocks.isEmpty()) {
                System.out.println("No blocks found in sub-blockchain.");
                return result;
            }

            // Карты для подсчета
            Map<String, Integer> addressCountMap = new HashMap<>();
            Map<String, Long> addressComplexityMap = new HashMap<>();
            Map<String, Map<Long, Integer>> complexityFrequencyMap = new HashMap<>();

            // Обработка блоков
            for (Block block : subBlocks) {
                String minerAddress = block.getMinerAddress();
                long blockComplexity = block.getHashCompexity();

                addressCountMap.put(minerAddress, addressCountMap.getOrDefault(minerAddress, 0) + 1);
                addressComplexityMap.put(minerAddress, addressComplexityMap.getOrDefault(minerAddress, 0L) + blockComplexity);

                Map<Long, Integer> frequencyMap = complexityFrequencyMap.getOrDefault(minerAddress, new HashMap<>());
                frequencyMap.put(blockComplexity, frequencyMap.getOrDefault(blockComplexity, 0) + 1);
                complexityFrequencyMap.put(minerAddress, frequencyMap);
            }

            int totalBlocks = subBlocks.size();
            List<Map.Entry<String, Integer>> entryList = new ArrayList<>(addressCountMap.entrySet());

            // Сортировка по проценту
            entryList.sort((e1, e2) -> {
                double percentage1 = (e1.getValue() * 100.0) / totalBlocks;
                double percentage2 = (e2.getValue() * 100.0) / totalBlocks;
                return Double.compare(percentage2, percentage1);
            });

            // Формирование результата
            for (Map.Entry<String, Integer> entry : entryList) {
                String pubkey = entry.getKey();
                int count = entry.getValue();
                double percentage = (count * 100.0) / totalBlocks;
                long totalComplexity = addressComplexityMap.get(pubkey);

                Map<Long, Integer> frequencyMap = complexityFrequencyMap.get(pubkey);
                long modeComplexity = -1;
                int maxFrequency = 0;
                for (Map.Entry<Long, Integer> freqEntry : frequencyMap.entrySet()) {
                    if (freqEntry.getValue() > maxFrequency) {
                        maxFrequency = freqEntry.getValue();
                        modeComplexity = freqEntry.getKey();
                    }
                }

                // Получение данных аккаунта
                String json = UtilUrl.readJsonFromUrl(address + "/account?address=" + URLEncoder.encode(pubkey, StandardCharsets.UTF_8));
                if (json == null || json.isBlank()) {
                    System.out.println("Account data not found for address: " + pubkey);
                    continue;
                }

                Account account = UtilsJson.jsonToAccount(json);
                long staking = UtilsUse.calculateScore(account.getDigitalStakingBalance(), BigDecimal.valueOf(1));
                result.add(String.format("pubkey: %s - blocks: %d - percent: %.2f%% - difficult: %d (moda: %d) - points for staking: %d", pubkey, count, percentage, totalComplexity, modeComplexity, staking));
            }

        } catch (Exception e) {
            System.err.println("Error in percent method: " + e.getMessage());
            e.printStackTrace();
        }

        return result;
    }


}
