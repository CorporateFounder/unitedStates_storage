package com.example.website.citu.controller;

import com.example.website.citu.entity.Account;
import com.example.website.citu.entity.SubBlockchainEntity;
import com.example.website.citu.model.Block;
import com.example.website.citu.model.LiteVersionWiner;
import com.example.website.citu.utils.UtilUrl;
import com.example.website.citu.utils.UtilsJson;
import com.example.website.citu.utils.UtilsTime;
import com.example.website.citu.utils.UtilsUse;
import com.fasterxml.jackson.core.JsonProcessingException;
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

    @GetMapping({"/winners", "/allwinners", "/allWinners"})
    public String winners(Model model) throws JsonProcessingException {
        List<LiteVersionWiner> allWinersList = safeLiteWinners(WebController.getResult("/allwinners", "error allWiners"));
        model.addAttribute("allWiners", allWinersList);
        model.addAttribute("uniq", allWinersList.stream().filter(UtilsUse.distinctByKey(LiteVersionWiner::getAddress)).collect(Collectors.toList()).size());
        model.addAttribute("allWinnerCount", allWinersList.size());

        List<LiteVersionWiner> bigRandomList = safeLiteWinners(WebController.getResult("/bigRandomWiner", "error bigRandomWiner"));
        if (bigRandomList.isEmpty()) {
            bigRandomList = safeLiteWinners(WebController.getResult("/bigRandomWinner", "error bigRandomWinner"));
        }
        model.addAttribute("bigRandom", bigRandomList);
        model.addAttribute("bigRandomCount", bigRandomList.size());
        model.addAttribute("title", "allwinners");
        model.addAttribute("sourceEndpoint", "/allwinners");
        model.addAttribute("status", allWinersList.isEmpty() && bigRandomList.isEmpty() ? ApiFallback.UNAVAILABLE : "");

        return "winners";
    }

    @GetMapping({"/winnerList", "/winerList"})
    public String winnerList(Model model) throws JsonProcessingException {
        List<LiteVersionWiner> winnerList = safeLiteWinners(WebController.getResult("/winnerList", "error winnerList"));
        if (winnerList.isEmpty()) {
            winnerList = safeLiteWinners(WebController.getResult("/winerList", "error winerList"));
        }
        model.addAttribute("allWiners", winnerList);
        model.addAttribute("uniq", winnerList.stream().filter(UtilsUse.distinctByKey(LiteVersionWiner::getAddress)).collect(Collectors.toList()).size());
        model.addAttribute("allWinnerCount", winnerList.size());
        model.addAttribute("bigRandom", new ArrayList<>());
        model.addAttribute("bigRandomCount", 0);
        model.addAttribute("title", "winnerList / winerList");
        model.addAttribute("sourceEndpoint", "/winnerList or /winerList");
        model.addAttribute("status", winnerList.isEmpty() ? ApiFallback.UNAVAILABLE : "");
        return "winners";
    }

    @GetMapping("/daily_rating")
    public String raitings(Model model) {
        int size = -1;
        try {
            size = ApiFallback.intValue(UtilUrl.readJsonFromUrl(address + "/size"), -1);
        } catch (Exception e) {
            System.out.println("daily rating size unavailable: " + e.getClass().getSimpleName());
        }

        long currentTime = UtilsTime.getUniversalTimestamp();
        long timeElapsed = UtilsTime.differentMillSecondTime(lastUpdateTime, currentTime);

        if (raitings.isEmpty() || timeElapsed >= 86400000L) {
            try {
                raitings = percent();
                lastUpdateTime = currentTime;
            } catch (Exception e) {
                System.out.println("daily rating unavailable: " + e.getClass().getSimpleName());
            }
        }

        long timeRemaining = (86400000L - timeElapsed) / 1000;
        model.addAttribute("time", timeRemaining);
        model.addAttribute("title", "daily_rating");
        model.addAttribute("day", size <= 0 ? 0 : (size / 576));
        model.addAttribute("raitings", raitings);
        return "daily_rating";
    }

    public List<String> percent() throws IOException {
        List<String> result = new ArrayList<>();

        try {
            int size = ApiFallback.intValue(UtilUrl.readJsonFromUrl(address + "/size"), 0);
            if (size <= 0) {
                return result;
            }

            SubBlockchainEntity subBlockchainEntity = new SubBlockchainEntity(Math.max(0, size - 576), size);
            String subBlockchainJson = UtilsJson.objToStringJson(subBlockchainEntity);
            List<Block> subBlocks = ApiFallback.blockList(UtilUrl.getObject(subBlockchainJson, address + "/sub-blocks"));
            if (subBlocks.isEmpty()) {
                return result;
            }

            Map<String, Integer> addressCountMap = new HashMap<>();
            Map<String, Long> addressComplexityMap = new HashMap<>();
            Map<String, Map<Long, Integer>> complexityFrequencyMap = new HashMap<>();

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
            entryList.sort((e1, e2) -> Double.compare(
                    (e2.getValue() * 100.0) / totalBlocks,
                    (e1.getValue() * 100.0) / totalBlocks));

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

                String json = UtilUrl.readJsonFromUrl(address + "/account?address=" + URLEncoder.encode(pubkey, StandardCharsets.UTF_8));
                if (ApiFallback.isBlank(json)) {
                    continue;
                }

                Account account = UtilsJson.jsonToAccount(json);
                long staking = UtilsUse.calculateScore(account.getDigitalStakingBalance(), BigDecimal.valueOf(1));
                result.add(String.format("pubkey: %s - blocks: %d - percent: %.2f%% - difficult: %d (moda: %d) - points for staking: %d", pubkey, count, percentage, totalComplexity, modeComplexity, staking));
            }
        } catch (Exception e) {
            System.err.println("Error in percent method: " + e.getClass().getSimpleName());
        }

        return result;
    }

    private List<LiteVersionWiner> safeLiteWinners(String json) {
        if (ApiFallback.isBlank(json)) {
            return new ArrayList<>();
        }
        try {
            List<LiteVersionWiner> result = UtilsJson.jsonToLiteVersionWiners(json);
            return result == null ? new ArrayList<>() : result;
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }
}
