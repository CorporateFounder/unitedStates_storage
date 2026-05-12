package com.example.website.citu.controller;

import com.example.website.citu.model.CurrentLawVotesEndBalance;
import com.example.website.citu.utils.UtilUrl;
import com.example.website.citu.utils.UtilsJson;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static com.example.website.citu.controller.WebController.address;

@Controller
public class VotingController {
    private static volatile long prevSize = 0;
    List<CurrentLawVotesEndBalance> currentLawVotesEndBalances = new ArrayList<>();
    @GetMapping("/voting")
    public String voting(Model model) throws IOException {
        String url =  "http://localhost:8082/current-laws-body";

        String str = UtilUrl.readJsonFromUrl(address + "/size");
        if(!str.isBlank()){
            long size = Integer.valueOf(str);
            String json = UtilUrl.readJsonFromUrl(url);
            if(size - prevSize > 432){
                prevSize = size;
                currentLawVotesEndBalances =UtilsJson.jsonToCurrentVoting(json);

            }
            if(currentLawVotesEndBalances.isEmpty()){
                json = UtilUrl.readJsonFromUrl(url);
                currentLawVotesEndBalances =  UtilsJson.jsonToCurrentVoting(json);
                prevSize = size;

            }
        }
        model.addAttribute("currentLaw", currentLawVotesEndBalances);
        System.out.println("prevSize in voting: " + prevSize);
        return "voting";
    }
}
