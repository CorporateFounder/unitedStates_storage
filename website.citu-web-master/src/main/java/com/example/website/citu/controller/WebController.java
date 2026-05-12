package com.example.website.citu.controller;

import com.example.website.citu.entity.InfoDificultyBlockchain;
import com.example.website.citu.entity.SubBlockchainEntity;
import com.example.website.citu.model.Block;
import com.example.website.citu.model.DtoTransaction;
import com.example.website.citu.utils.UtilUrl;
import com.example.website.citu.utils.UtilsJson;
import com.example.website.citu.utils.UtilsUse;
import com.fasterxml.jackson.core.JsonProcessingException;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.NoRouteToHostException;
import java.net.SocketException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SignatureException;
import java.security.spec.InvalidKeySpecException;
import java.util.*;
import java.util.stream.Collectors;

@Controller
public class WebController {
    String BASIS_ADDRESS = "faErFrDnBhfSfNnj1hYjxydKNH28cRw1PBwDQEXH3QsJ";

    public static String getResult(String url, String errorText) {
        String str = "-1";
        try {
            str = UtilUrl.readJsonFromUrl(address + url);
        } catch (NoRouteToHostException e) {
            System.out.println(errorText);
            str = "-1";
        } catch (SocketException e) {
            System.out.println(errorText);
            str = "-1";
        } catch (IOException e) {
            throw new RuntimeException(e);

        } finally {
            return str;
        }

    }

    private String uriBase = "http://localhost:8080"; // change this to your server address
    static String address = ExplorerController.getAddress();

    @GetMapping("/")
    public String mainPage(Model model) throws IOException {
        //first api size blockchain
        String sizeStr = "-1";
        sizeStr = getResult("/size", "home page you cannot connect to global server,\" +\n" +
                "                    \"you can't give size global server");

        model.addAttribute("size", sizeStr);

        //second api
        InfoDificultyBlockchain infoDificultyBlockchain = new InfoDificultyBlockchain(-1, -1);
        String difficultOneBlock = ":";
        String difficultAllBlockchain = ":";
        try {
            String json = UtilUrl.readJsonFromUrl(address + "/difficultyBlockchain");
            infoDificultyBlockchain = UtilsJson.jsonToInfoDifficulty(json);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        String startReduce = getResult("/v28Index", "you can't give start index global server");

        String cleanedValue = "";
        String totalDollars = getResult("/totalDollars", "you can't give total dollars global server");
        cleanedValue = totalDollars.replaceAll("\"", "");
        totalDollars = cleanedValue;
        String totalAccounts = getResult("/allAccounts", "you can't give total accounts global server");
        String multiplier = getResult("/multiplier", "you can't give multiplier global server");
        String daysReduce = getResult("/dayReduce", "you can't give day reduce global server");

        long size = Long.valueOf(sizeStr);

        String urlPrev = address + "/conductorBlock?index=" + (size - 1);

        String prevJson = UtilUrl.getObject(urlPrev);

        Block prev = (Block) UtilsJson.jsonToClass(prevJson, Block.class);

        int prevBlockTotalTransaction = prev.getDtoTransactions().size();
        double prevBlockTotalSum = prev.getDtoTransactions()
                .stream().mapToDouble(t->t.getDigitalDollar())
                .sum();


        String totalTransactionsJson = getResult("/totalTransactionsDay", "you can't give total transactions day global server");
        int totalTransactionsDay = Integer.valueOf(totalTransactionsJson);
        String totalTransactionsSumJson = getResult("/totalTransactionsSum", "you can't give total transactions sum global server");

        cleanedValue = totalTransactionsSumJson.replaceAll("\"", "");
        System.out.println("cleaneValue: " + cleanedValue);
        double totalTransactionsDaySum =  Double.parseDouble(cleanedValue);
        System.out.println("totalTransactionsDaySum: " + totalTransactionsDaySum);

        double targetTotalSum = prev.getDtoTransactions().stream()
                .filter(t->!t.getSender().equals(BASIS_ADDRESS))
                .mapToDouble(t->t.getDigitalDollar())
                .sum();
        long targetTotalUniqAddress = prev.getDtoTransactions().stream()
                .filter(t->!t.getSender().equals(BASIS_ADDRESS))
                .map(t->t.getSender())
                .distinct()
                .count();



        difficultOneBlock = Long.toString(infoDificultyBlockchain.getDiffultyOneBlock());
        difficultAllBlockchain = Long.toString(infoDificultyBlockchain.getDifficultyAllBlockchain());
        model.addAttribute("difficulty", difficultOneBlock);
        model.addAttribute("difficultyAll", difficultAllBlockchain);
        model.addAttribute("targetTotalUniqAddress", targetTotalUniqAddress);
        model.addAttribute("targetTotalSum", targetTotalSum);

        model.addAttribute("startReduce", startReduce);
        model.addAttribute("totalDollars", totalDollars);
        model.addAttribute("totalAccounts", totalAccounts);
        model.addAttribute("multiplier", multiplier);
        model.addAttribute("daysReduce", daysReduce);
        model.addAttribute("prevBlockTotalTransaction", prevBlockTotalTransaction);
        model.addAttribute("prevBlockTotalSum", prevBlockTotalSum);
        model.addAttribute("totalTransactionsDay", totalTransactionsDay);
        model.addAttribute("totalTransactionsDaySum", totalTransactionsDaySum);





        model.addAttribute("title", "Main page");
        model.addAttribute("Summary", "Summary and Benefits");
        model.addAttribute("discord", "https://discord.gg/Szbs8G4Q8c");
        model.addAttribute("telegram", "https://t.me/citu_coin");
        model.addAttribute("telegramUsername", "Negmat_Tuychiev"); // замените пробелы на подчеркивания
        model.addAttribute("github", "https://github.com/CorporateFounder/unitedStates_final");
        model.addAttribute("storage", "https://github.com/CorporateFounder/unitedStates_storage");
        model.addAttribute("twitter", "citu4030");
        model.addAttribute("gmail", "citu4030@gmail.com");




        model.addAttribute("text",
                "Preamble:\n" +
                        "Each coin has only 2 decimal places.\n" +
                        "\n" +
                        "The lower boundary of the cryptocurrency price is determined by the electricity costs for its mining, reflecting the labor theory of value. The upper boundary is governed by the subjective theory of value, representing the value users attribute to the cryptocurrency. The average price will grow in accordance with the marginal utility theory since mining is inelastic, gradually decreasing, but never reaching zero. The annual coin supply increase is significantly less than 0.005% relative to the total issuance, offsetting lost coins and supporting miner expenses.\n" +
                        "\n" +
                        "The system dynamically adapts: when demand decreases, mining complexity decreases, reducing emissions, and during a sharp price increase, complexity grows, stimulating coin issuance. This allows the system to regulate the exchange rate using market tools. Excess emissions are directed to staking, promoting long-term stability.\n" +
                        "\n" +
                        "This cryptocurrency is especially suitable as a reserve currency. Its limited supply makes it attractive due to its scarcity, enhancing its market value.");
        return "main";
    }

    @GetMapping("/summary_and_benefits")
    public String summaryAndBenefits(Model model) {
        List<String> strings = new ArrayList<>();
        strings.add("Brief description of cryptocurrency\n" +
                "A unique cryptocurrency with a number of features, such as:\n" +
                "Two unique coins, a digital dollar and digital shares.\n" +
                "On average, 576 blocks are mined per day.\n" +
                "A unique mining algorithm in which a block is considered valid if it meets these conditions\n" +

                "The mining algorithm is that we convert the hash into bits and count all the units in bits." +
                " The sum of units must be less than or equal to the target. The goal is 100 - difficulty. that is, the " +
                "difficulty can only double each time. Example: if previously the complexity was 1 and became 2," +
                " then the probability of finding the correct hash has become half as much. This allows us " +
                "to better control the difficulty of the difficulty levels, up to 100 in fact." +
                "public static int getBitSum2(String hash) {\n" +
                "    int bitSum = 0;\n" +
                "    String hashUpper = hash.toUpperCase();\n" +
                "    for (int i = 0; i < hashUpper.length(); i += 2) {\n" +
                "      String hex = hashUpper.substring(i, i + 2);\n" +
                "      int hexValue = Integer.parseInt(hex, 16);\n" +
                "      while (hexValue > 0) {\n" +
                "        bitSum += hexValue & 1;\n" +
                "        hexValue >>= 1;\n" +
                "      }\n" +
                "    }\n" +
                "    return bitSum;\n" +
                "  }\n" +
                "\n" +
                "\n" +
                "\n" +
                "  public static boolean isValidHashV29(String hash, int difficulty){\n" +
                "    // Вычислить сумму битов в хэше\n" +
                "    int bitSum = getBitSum2(hash);\n" +
                "    // Проверить, меньше ли или равна сумма битов заданному уровню сложности\n" +
                "    return bitSum <= difficulty;\n" +
                "  }" +
                "A unique electoral system that allows you to choose your representatives.\n" +
                "Three branches of government: legislative, judicial and executive.\n" +
                "Parliamentary form of government.\n" +
                "\n" +
                "A unique mining system, the number of mined coins per block is equal to the formula (5+coefficient) * multiplier.\n" +
                "where the coefficient can be either zero or three if these conditions are met \"target uniq sender in prev block\" where the number of unique senders must be greater than\n" +
                "the number of unique senders in the previous block, but not counting the base address, also the sum of all transactions, not counting the founder and miner reward\n" +
                "must be higher than in the previous block, then the coefficient is 3, otherwise 0. \"target sum sender transaction in prev block\"\n" +
                "\n" +
                "\n" +
                "\n" +
                "All laws and elected positions are valid for exactly 4 years and must be re-voted every four years.\n" +
                "A unique sanctions system where participants can donate their coins to another participant who has lost the same number of coins, but this mechanism only works for shares.\n" +
                "Detailed description of each part\n" +
                "1. Two unique coins\n" +
                "For each block two types of coins are given, one coin is a dollar, the second is a share. Shares are used to vote on the election of officials and to vote on legislation.\n" +
                "2. Approximately 576 are mined per day\n" +
                "Approximately 576 blocks are mined per day, allowing for a large number of participants to engage in mining. But the quantity is not strictly fixed, and each block takes about 150 seconds to mine, and the difficulty can rise or fall depending on the mining. increased by 2.3 times or fell by 1.6 times\n" +

                "10. Laws and regulations\n" +
                "All participants can vote for both multiple participants and individuals. Each vote is counted only for the last four years. For example, you have one hundred shares, which means you have one hundred votes FOR and one hundred votes against. Example: You have 100 shares. There are 6 positions of candidates on the board A) B) C) D) E) E) F) you want to support A) and B) then by voting for them, they receive 50 votes each, the votes are divided by the number of candidates FOR Similarly, you want to vote against the remaining 4 at the same time, and each of them will receive minus 25 votes, divide the votes by the number of candidates AGAINST, which means you voted for 6 candidates, 2 of them received +50, four of them -25 .Each law, like each position, has its own hash, for which participants can vote and thus take part in voting.\n" +
                "\n" +
                "11. Sanctions\n" +
                "This system implements a sanctions mechanism; imagine that there are participants who violate the rules of the network and they are trying to use laws of a fairly radical type. Let's imagine that there are six participants with such views,\n" +
                "one centrist.\n" +
                "two left\n" +
                "two on the right\n" +
                "  one radical.\n" +
                "  Each participant has one hundred shares, so a radical may try to make a decision that all other participants do not support. And then they decide that each of them is ready to lose twenty coins, but the radical loses this number of coins. .Now imagine that each of them imposes sanctions against the radical, and all participants lose their coins, but the radical loses all his hundred shares. But other participants also lose their shares, 20 each.");


        model.addAttribute("title", "Summary and benefits");
        model.addAttribute("texts", strings);
        return "summary_and_benefits";
    }


    @GetMapping("/how_to_install")
    public String installPage(Model model) {
        model.addAttribute("title", "INSTALLATION:  how to install");
        model.addAttribute("text", "If you have windows, then you need to download from the folder target unitedStates-0.0.1-SHAPSHOT.jar\n" +
                "in the search for windows, enter cmd open the command line and enter java -jar there (where the file is located) / unitedStates-0.0.1-SNAPSHOT.jar\n" +
                "example: java -jar C://unitedStates-0.0.1-SNAPSHOT.jar.\n" +
                "\n" +
                "To work properly you need to download and install jre https://www.java.com/en/download/manual.jsp\n" +
                "https://www.oracle.com/cis/java/technologies/downloads/,\n" +
                "and jdk 19 or higher\n" +
                "\n" +
                "after launch jar, the resources folder will be automatically created where windows, then \n" +
                "go to localhost:8082 go down push button update blockchain\n" +
                "\n" +
                "the resources folder is in src/main/java/resources\n" +
                "there are stored\n" +
                "1. blockchain files in the /blockchain folder\n" +
                "2. balance files in the folder /balance\n" +
                "3. rules files voted for with their votes /allLawsWithBalance\n" +
                "4.files all rules without votes in /federalLaws\n" +
                "5. account files that have been elected as guide /federalLaws\n" +
                "6. file storing miner account /minerAccount\n" +
                "7. host address files /poolAddress\n" +
                "8. files sent by transaction /sentTransaction\n" +
                "9. transaction list files to send /transactions");

        return "how_to_install";
    }

    @GetMapping("/mining-app-guide-win")
    public String install_supr_trupr(Model model){
        model.addAttribute("application", "https://github.com/SuprTrupr/CITU_app_miner/tree/main");

        return "mining-app-guide-win";
    }

    @GetMapping("how_to_install_server")
    public String installServer(){
        return "how_to_install_server";
    }
    @GetMapping("/how_to_open_an_account")
    public String howToOpenAnAccount(Model model) {
        model.addAttribute("title", "How to open an account");
        model.addAttribute("text1", "Once the server has been properly started, go to http://localhost:8082/create-account\n" +
                "There you need to copy NEW ADDRESS this is your LOGIN and PUBLIC KEY.\n" +
                "You also need to copy PASSWORD this is your PRIVATE KEY.\n" +
                "Copy your username and password and keep it in a safe place.");
        model.addAttribute("tex2", "\n" +
                "After you need to change the address of the miner, enter the settings http://localhost:8082/seting\n" +
                "\n" +
                "enter your pub-key login there, and click the button CHANGE MINER ADDRESS");

        return "how_to_open_an_account";
    }

    @GetMapping("/how_to_change_miner_account")
    public String howToChangeMinerAccount(Model model) {
        model.addAttribute("title", "How to change miner account");
        model.addAttribute("text1", "Start local server and login http://localhost:8082/seting\n" +
                "or click the settings button, enter your ADDRESS (PUBLIC KEY)\n" +
                "ENTER PUBKEY TO CHANGE MINER ADDRESS and press the button\n" +
                "CHANGE MINER ADDRESS. UtilsFileSaveRead.save() saves the new public account to a file\n" +
                "in folder: resources/minerAccount/minerAccount.txt");
        return "how_to_change_miner_account";
    }

    @GetMapping("/how_to_mining")
    public String howToMining(Model model, @RequestParam(defaultValue = "0.0") Double staking) {
        model.addAttribute("title", "How to mining");
        model.addAttribute("noteText", "Block mining NOTE!!! THE BEING BLOCK HAS INDEX = 1, THE SAME AS THE ONE FOLLOWING IT. SO IN THIS BLOCKCHAIN \u200B\u200BTHERE ARE TWO BLOCKS WITH IDENTICAL INDICES, BUT WITH DIFFERENT CONTENT. THIS IS ABSOLUTELY NORMAL AND THIS IS THE FEATURE OF THIS BLOCKCHAIN. A unique mining system is implemented here, which stimulates a smooth increase in the value of the coin, preventing it from falling much with the onset of winter and is resistant to strong volatility, but at the same time the value increases. For mine, you need to go to localhost:8082/, multithreading is automatically turned on there and the number of threads is automatically turned on. Then go back to the menu in the top right corner and click on “Get Locked”, click on the “Start” button and then click on “International Union Corporation”, in the information window you should see “Is Mining Proper”. the update is also correct, which means that the system is loading the blockchain. Nothing will be displayed in the console, because if we display information there, mining will slow down six times for single-threaded and multi-threaded mining. If you want to turn off mining, click the “Stop” button twice, then click on the Corporation Intertation Union inscription, if everything stopped correctly, the information window will disappear. Under no circumstances should you interrupt the mining process by disabling the command line during mining or updating, as at this point the Blockchain and balance will be overwritten. When the information window goes out, mining will stop. A block is valid if the hash matches the target value according to this formula");
        model.addAttribute("instructionsText", "In the get a block tab (http://localhost:8082/mining) you can make staking or withdraw money from staking. By filling in all fields.\n" +
                "NOTE: IMPORTANT!!!!!!\n" +
                "IF YOU CALL http://localhost:8082/testCalculate in the console you will see a list of accounts, and the balance there is different. Be sure to report this\n" +
                "to the server owner, since the balance either on your wallet or on the server is not displayed correctly and he will not be able to send money.\n" +
                "look at the picture. If the server owner has discovered this problem, then he should update the resources folder and you don’t have to worry,\n" +
                "if the problem is with the server, all the owners are exceptional professionals and know their stuff.\n" +
                "P.S. (At the moment, in addition to the founder, you can also contact @suprtrupr and @caspar2114 on discord, since they are one of the best in\n" +
                "these matters and can help you when I don’t have time.)");
        model.addAttribute("importantInfoText", "(important information, the text below will be out of date after reaching index 342201)" +
                " In this system, the block with the most big random wins. Big random is a number that is formed from three components.\n" +
                "1. Difficulty\n" +
                "2. Random number\n" +
                "3. staking\n" +
                "\n" +
                "up to index 247867, each difficulty level gave 9 points. difficult * 9.\n" +
                "after 247867 each difficulty level gives 55 points. Difficult * 55.\n" +
                "\n" +
                "up to index 247867 hash as a seed could give a number from 0 to 55.\n" +
                "after 247867 hash as a seed, it gives a number from 0 to 135.\n" +
                "\n" +
                "up to index 247867, initial staking cost 1 coin, first point.\n" +
                "After the index 247867, initial staking costs 10 coins, the first point.\n" +
                "\n" +
                "Now in more detail: this is the formula by which a single score is calculated.\n" +
                "int result = deterministicRandom.nextInt(limit);\n" +
                "result = (int) ((int) (result + (actual.getHashCompexity() * waight)) + calculateScore(miner.getDigitalStakingBalance(), number)\n" +
                "\n" +
                "1. Difficulty.\n" +
                "Each participant can choose the difficulty from 17 to 100.\n" +
                "Each block has a hash, where the sum of the ones in bits must be equal to or lower than the target.\n" +
                "The goal is calculated as 100 - difficulty (100 minus difficulty.).\n" +
                "Each difficulty level gives 55 points. That is, if you set the difficulty to 20, then the difficulty points will be equal to 20 * 55 = 1100.\n" +
                "\n" +
                "2. Random number.\n" +
                "The hash of the block becomes the seed to generate a random number from 0 to 135.\n" +
                "\n" +
                "3. Staking.\n" +
                "Each subsequent point costs twice as much and is calculated using this formula:\n" +
                " public static long calculateScore(double x, double x0) {\n" +
                " if (x <= 0) {\n" +
                " return 0;\n" +
                " }\n" +
                " double score = Math.ceil(Math.log(x / x0) / Math.log(2));\n" +
                " return Math.min(400, (long) score);\n" +
                " }\n" +
                "\n" +
                "but this number cannot be more than 400.\n All points from the three parameters are summed up.\n" +
                "Each node (server) selects one block with the highest number of points every 100 seconds.\n" +
                "Afterwards, each node contacts random 7 nodes and compares the total\n" +
                "big random (of all blocks of a given blockchain) of your blockchain and the blockchain of another node.\n" +
                "If on another node the total big random is greater than on yours + the complexity of the last block, then your server will delete blocks up to the intersection point,\n" +
                "and will add blocks of this node. The difference between blocks must be at least 100 seconds.\nThe mining reward is calculated using the formula (5+coefficient + (difficulty * 0.2)) * multiplier. Where the multiplier is 29, but decreases by one each year until it reaches 1. The multiplier can be 0 or 3; For it to become equal to 3, two conditions must be met. 1. The sum of all transactions in the current block must be greater than in the previous block, excluding the founder's reward and the miner's reward. 2. The number of different senders in this block must be greater than in the previous block, not counting the base address from which the reward is sent to the founder and miner. http://194.87.236.238:82/allwinners this URL shows the last winner on this node, but does not show the winner if it was taken from another server and to see the last final winner you need /prevBlock\n" +
                "IMPORTANT!!! The founder's reward from each block is equal to 10% of the miner's reward. This is necessary so that we can finance development. But these coins are created additionally, that is, they are not taken from the miner, but additional coins are created." +
                " account founder nNifuwmFZr7fnV1zvmpiyQDV5z7ETWvqR6GSeqeHTY43 P.S. (before this index 187200, the reward was also different and the details are Mining and UtilsUse)." +
                "be sure, if you use your own server, you can configure your wallet on your server by going to the settings and writing your server there, without the slash at the end.\n");
        model.addAttribute("randomAndStakingText", "Story:\n" +
                "a special block was created on index 24281, with the help of which we created 12,000,000 coins and these coins were given to the participants, since the blockchain was broken by an attack in 40 days\n" +
                "and with the help of this address jPjuyLStHTCzwYt9J7R5M7pGUKshfcmEbtE3zVvCBE52 this money was returned to all participants who lost their coins.\n" +
                "The bug in the code that allowed this attack was also finally fixed, and now the system is completely safe.\nStory:\n" +
                "Starting from the index int V34_NEW_ALGO = 187200, the difficulty is selected by participants from 17 to 100.\n" +
                "Before this index, the complexity algorithm changed and its details are in the UtilsBlock class method difficulty.\n" +
                "Before this index, the mining algorithm also changed. Before index 151940, the mining algorithm was also different and the details are in the Block class as well as in related classes,\n" +
                "UtilsUse.\n");
        model.addAttribute("newReward", "friends, starting from index 295316 and from index 296583 the mining reward has been increased, as the minimum sending amount is now 0.01 coin. Here is the new formula\n" +
                "if (index > Seting.ALGORITM_MINING ) {\n" +
                "moneyFromDif = (difficulty - DIFFICULT_MONEY) / 2;\n" +
                "moneyFromDif = moneyFromDif > 0 ? moneyFromDif : 0;\n" +
                "}\n" +
                "\n" +
                "minerRewards = (Seting.V28_REWARD + G + (difficulty * Seting.V34_MINING_REWARD) + moneyFromDif) * money;\n" +
                "if(index > ALGORITM_MINING_2){\n" +
                "minerRewards += moneyFromDif * (MULT + G);\n" +
                "digitalReputationForMiner += moneyFromDif * (MULT + G);\n" +
                "} V28_REWARD = 5. G = 3 if the number and amount of transactions of the current block relative to the previous block is higher, then it is equal to 3, otherwise zero. G is the coefficient. difficulty - the difficulty of the block, DIFFICULT_MONEY - 22. V34_MINING_REWARD - 0.2. money - the multiplier is 29, but decreases by 1 every year, but not lower than 1. MULT - is equal to 6. These changes are made to compensate for the high complexity, as well as to stabilize the rate, which will give our coin a rate of 10 dollars or higher.");

        model.addAttribute("newBigRandom", "Friends, the latest versions work stably and display the balance stably, but we noticed that some abuse points for the number of transactions, and create transaction spam using a script to get more points. Since the number of transactions does not play on mining in any way, and hashing has the same speed in any case, now big random points from transactions will be accrued from the total amount, similarly if this money was in your staking. Thus, it is not the number that plays more, but the total amount, but the calculation of the coefficient remains the same, where both the number and the amount are taken into account. The maximum number of points from transactions you can get int diffLimit = (int) (actual.getHashCompexity() - 19);\n" +
                "diffLimit = diffLimit >= 0 ? diffLimit : 0; // New formula for the maximum number of points for transactions\n" +
                "double maxTransactionPoints = diffLimit * 3 + mineScore; where mineScore is the minescore you have in staking, and you can't get more points than here. Download wallet 0.32.127, and server 0.0.298. new rules will come into effect from block index 299845");

        model.addAttribute("udatingBigRandom", "starting with index 286892 each difficulty level gives 25 points, staking, the first point costs 1 coin, each subsequent point in staking requires twice as much, that is, for three points you need to have 8 coins in staking. Also randomness goes from 0 to 150, where seed is your hash block. The first point requires 1.01, in staking, the second point, 2.01, the third point 4.01, the fourth point, 8.01.");

        // add more entries as needed
        long score = UtilsUse.calculateScore(BigDecimal.valueOf(staking), BigDecimal.valueOf(1.0));
        model.addAttribute("score", score);
        return "how_to_mining";
    }

    @PostMapping("/how_to_mining")
    public String updateStaking(Model model, @RequestParam Double staking) {
        model.addAttribute("title", "How to mining");
        model.addAttribute("noteText", "Block mining NOTE!!! THE BEING BLOCK HAS INDEX = 1, THE SAME AS THE ONE FOLLOWING IT. SO IN THIS BLOCKCHAIN \u200B\u200BTHERE ARE TWO BLOCKS WITH IDENTICAL INDICES, BUT WITH DIFFERENT CONTENT. THIS IS ABSOLUTELY NORMAL AND THIS IS THE FEATURE OF THIS BLOCKCHAIN. A unique mining system is implemented here, which stimulates a smooth increase in the value of the coin, preventing it from falling much with the onset of winter and is resistant to strong volatility, but at the same time the value increases. For mine, you need to go to localhost:8082/, multithreading is automatically turned on there and the number of threads is automatically turned on. Then go back to the menu in the top right corner and click on “Get Locked”, click on the “Start” button and then click on “International Union Corporation”, in the information window you should see “Is Mining Proper”. the update is also correct, which means that the system is loading the blockchain. Nothing will be displayed in the console, because if we display information there, mining will slow down six times for single-threaded and multi-threaded mining. If you want to turn off mining, click the “Stop” button twice, then click on the Corporation Intertation Union inscription, if everything stopped correctly, the information window will disappear. Under no circumstances should you interrupt the mining process by disabling the command line during mining or updating, as at this point the Blockchain and balance will be overwritten. When the information window goes out, mining will stop. A block is valid if the hash matches the target value according to this formula");
        model.addAttribute("instructionsText", "In the get a block tab (http://localhost:8082/mining) you can make staking or withdraw money from staking. By filling in all fields.\n" +
                "NOTE: IMPORTANT!!!!!!\n" +
                "IF YOU CALL http://localhost:8082/testCalculate in the console you will see a list of accounts, and the balance there is different. Be sure to report this\n" +
                "to the server owner, since the balance either on your wallet or on the server is not displayed correctly and he will not be able to send money.\n" +
                "look at the picture. If the server owner has discovered this problem, then he should update the resources folder and you don’t have to worry,\n" +
                "if the problem is with the server, all the owners are exceptional professionals and know their stuff.\n" +
                "P.S. (At the moment, in addition to the founder, you can also contact @suprtrupr and @caspar2114 on discord, since they are one of the best in\n" +
                "these matters and can help you when I don’t have time.)");
        model.addAttribute("importantInfoText", "In this system, the block with the most big random wins. Big random is a number that is formed from three components.\n" +
                "1. Difficulty\n" +
                "2. Random number\n" +
                "3. staking\n" +
                "\n" +
                "up to index 247867, each difficulty level gave 9 points. difficult * 9.\n" +
                "after 247867 each difficulty level gives 55 points. Difficult * 55.\n" +
                "\n" +
                "up to index 247867 hash as a seed could give a number from 0 to 55.\n" +
                "after 247867 hash as a seed, it gives a number from 0 to 135.\n" +
                "\n" +
                "up to index 247867, initial staking cost 1 coin, first point.\n" +
                "After the index 247867, initial staking costs 10 coins, the first point.\n" +
                "\n" +
                "Now in more detail: this is the formula by which a single score is calculated.\n" +
                "int result = deterministicRandom.nextInt(limit);\n" +
                "result = (int) ((int) (result + (actual.getHashCompexity() * waight)) + calculateScore(miner.getDigitalStakingBalance(), number)\n" +
                "\n" +
                "1. Difficulty.\n" +
                "Each participant can choose the difficulty from 17 to 100.\n" +
                "Each block has a hash, where the sum of the ones in bits must be equal to or lower than the target.\n" +
                "The goal is calculated as 100 - difficulty (100 minus difficulty.).\n" +
                "Each difficulty level gives 55 points. That is, if you set the difficulty to 20, then the difficulty points will be equal to 20 * 55 = 1100.\n" +
                "\n" +
                "2. Random number.\n" +
                "The hash of the block becomes the seed to generate a random number from 0 to 135.\n" +
                "\n" +
                "3. Staking.\n" +
                "Each subsequent point costs twice as much and is calculated using this formula:\n" +
                " public static long calculateScore(double x, double x0) {\n" +
                " if (x <= 0) {\n" +
                " return 0;\n" +
                " }\n" +
                " double score = Math.ceil(Math.log(x / x0) / Math.log(2));\n" +
                " return Math.min(400, (long) score);\n" +
                " }\n" +
                "\n" +
                "but this number cannot be more than 400.\n All points from the three parameters are summed up.\n" +
                "Each node (server) selects one block with the highest number of points every 100 seconds.\n" +
                "Afterwards, each node contacts random 7 nodes and compares the total\n" +
                "big random (of all blocks of a given blockchain) of your blockchain and the blockchain of another node.\n" +
                "If on another node the total big random is greater than on yours + the complexity of the last block, then your server will delete blocks up to the intersection point,\n" +
                "and will add blocks of this node. The difference between blocks must be at least 100 seconds.\nThe mining reward is calculated using the formula (5+coefficient + (difficulty * 0.2)) * multiplier. Where the multiplier is 29, but decreases by one each year until it reaches 1. The multiplier can be 0 or 3; For it to become equal to 3, two conditions must be met. 1. The sum of all transactions in the current block must be greater than in the previous block, excluding the founder's reward and the miner's reward. 2. The number of different senders in this block must be greater than in the previous block, not counting the base address from which the reward is sent to the founder and miner. http://194.87.236.238:82/allwinners this URL shows the last winner on this node, but does not show the winner if it was taken from another server and to see the last final winner you need /prevBlock\n" +
                "IMPORTANT!!! The founder's reward from each block is equal to 10% of the miner's reward. This is necessary so that we can finance development. But these coins are created additionally, that is, they are not taken from the miner, but additional coins are created." +
                " account founder nNifuwmFZr7fnV1zvmpiyQDV5z7ETWvqR6GSeqeHTY43 P.S. (before this index 187200, the reward was also different and the details are Mining and UtilsUse)." +
                "be sure, if you use your own server, you can configure your wallet on your server by going to the settings and writing your server there, without the slash at the end.\n");
        model.addAttribute("randomAndStakingText", "Story:\n" +
                "a special block was created on index 24281, with the help of which we created 12,000,000 coins and these coins were given to the participants, since the blockchain was broken by an attack in 40 days\n" +
                "and with the help of this address jPjuyLStHTCzwYt9J7R5M7pGUKshfcmEbtE3zVvCBE52 this money was returned to all participants who lost their coins.\n" +
                "The bug in the code that allowed this attack was also finally fixed, and now the system is completely safe.\nStory:\n" +
                "Starting from the index int V34_NEW_ALGO = 187200, the difficulty is selected by participants from 17 to 100.\n" +
                "Before this index, the complexity algorithm changed and its details are in the UtilsBlock class method difficulty.\n" +
                "Before this index, the mining algorithm also changed. Before index 151940, the mining algorithm was also different and the details are in the Block class as well as in related classes,\n" +
                "UtilsUse.\n");
        model.addAttribute("newReward", "friends, starting from index 295316 and from index 296583 the mining reward has been increased, as the minimum sending amount is now 0.01 coin. Here is the new formula\n" +
                "if (index > Seting.ALGORITM_MINING ) {\n" +
                "moneyFromDif = (difficulty - DIFFICULT_MONEY) / 2;\n" +
                "moneyFromDif = moneyFromDif > 0 ? moneyFromDif : 0;\n" +
                "}\n" +
                "\n" +
                "minerRewards = (Seting.V28_REWARD + G + (difficulty * Seting.V34_MINING_REWARD) + moneyFromDif) * money;\n" +
                "if(index > ALGORITM_MINING_2){\n" +
                "minerRewards += moneyFromDif * (MULT + G);\n" +
                "digitalReputationForMiner += moneyFromDif * (MULT + G);\n" +
                "} V28_REWARD = 5. G = 3 if the number and amount of transactions of the current block relative to the previous block is higher, then it is equal to 3, otherwise zero. G is the coefficient. difficulty - the difficulty of the block, DIFFICULT_MONEY - 22. V34_MINING_REWARD - 0.2. money - the multiplier is 29, but decreases by 1 every year, but not lower than 1. MULT - is equal to 6. These changes are made to compensate for the high complexity, as well as to stabilize the rate, which will give our coin a rate of 10 dollars or higher.");

        model.addAttribute("newBigRandom", "Update:\n" +
                "Friends, the latest versions work stably and display the balance stably, but we noticed that some people abuse the points for the number of transactions and create spam transactions using a script to get more points. Since the number of transactions does not play a role in mining, and hashing has the same speed anyway, now large random points from transactions will be awarded from the total amount, similarly if you had this money in staking. Thus, it is not the number that plays more, but the total amount, but the calculation of the coefficient remains the same, where both the number and the amount are taken into account. The maximum number of points from transactions that you can get int diffLimit = (int) (actual.getHashCompexity() - 19); diffLimit = diffLimit >= 0 ? diffLimit : 0; // New formula for the maximum number of points for transactions double maxTransactionPoints = diffLimit * 3 + mineScore; where mineScore is your minescore in staking, and you can't get more points than this. Download wallet 0.32.129 and server 0.0.300. new rules will come into effect from block index 299845 example: if you have 0.81 coins in your block, it will give you an additional 3 points, as if you had these coins additionally in staking. the first point is worth 0.1, that is, the sum in all transactions of your block must be greater than 0.1, that is 0.11. For the second point 0.21 and for the third 0.41 and so on.");

        model.addAttribute("udatingBigRandom", "starting with index 286892 each difficulty level gives 25 points, staking, the first point costs 1 coin, each subsequent point in staking requires twice as much, that is, for three points you need to have 8 coins in staking. Also randomness goes from 0 to 150, where seed is your hash block. The first point requires 1.01, in staking, the second point, 2.01, the third point 4.01, the fourth point, 8.01.");


        // add more entries as needed
        long score = UtilsUse.calculateScore(BigDecimal.valueOf(staking), BigDecimal.valueOf(1.0));
        model.addAttribute("score", score);
        model.addAttribute("staking", staking);
        // Add other model attributes if needed.
        return "how_to_mining"; // This should match the name of your HTML template file.
    }

    @GetMapping("/how_to_transaction")
    public String howToTransaction(Model model) {
        model.addAttribute("title", "How to transaction");
        model.addAttribute("text1", "# Transaction\n" +
                "\n" +
                "## How to send a transaction\n" +
                "How to send money\n" +
                "\n" +
                "login to http://localhost:8082/\n" +
                "Enter the sender's address, recipient's address, how many digital\n" +
                "dollars you want to send, how many digital shares you want to send,\n" +
                "miner reward\n" +
                "\n" +
                "Before sending, update the local blockchain, but up-to-date.\n" +
                "Before voting, and other actions, you can update the blockchain,\n" +
                "but before the vote is not necessary, since no amount is sent.\n" +
                "Also, in order to see current positions, it is worth updating the blockchain.\n" +
                "Before mining happens automatically.\n" +
                "To do this, you need to press the button ***update blockchain*** on the main menu and at the very bottom\n");
        model.addAttribute("text2", "And enter the password, then click the send money button");
        model.addAttribute("text3", "at localhost:8082/\n" +
                "need to keep data in\n" +
                "- input address sender public key of the sender\n" +
                "- input address recipient public key of the recipient\n" +
                "- input digital dollar to send amount of digital dollars to send\n" +
                "- input digital stock to send amount of digital stock to send\n" +
                "- send reward for miner\n" +
                "\n" +
                "- input password keep private key\n" +
                "- and click send money\n" +
                "\n" +
                "## What the transaction class consists of\n" +
                "\n" +

                " src/main/java/entity/blockchain/DtoTransaction/DtoTransaction.java\n" +

                "\n" +
                "Transaction constructor.\n" +
                "- sender (sender)\n" +
                "- customer (recipient)\n" +
                "- digitalDollar (digital dollar)\n" +
                "- digitalStock (digital stocks)\n" +
                "- laws (Package of laws)\n" +
                "- bonusMiner (miner reward)\n" +
                "- VoteEnum (sender's vote, which can be YES or NO)\n" +
                "- sign (sender's signature)\n" +
                "\n" +
                "\n" +
                "checks the integrity of the transaction that the transaction was signed correctly\n" +
                "The method is in the DtoTransaction.java class");
        return "how_to_transaction";
    }

    @GetMapping("/how_to_apply_for_a_job")
    public String howToApplyForAJob(Model model) {
        model.addAttribute("title", "how to apply for a job");
        model.addAttribute("text1", " If you want to become a member of the board of directors or a member of the Council of Corporate Judges or a General Director, then you need to go to apply for a position, select a position, fill out all fields, then click submit. When this transaction is accepted online, find in the all laws tab the identification number of your positions, there the name of the package will correspond to your position, go to each position, and see that the first line inside is your pubkey, as soon as you found, copy the identification number of your position, it always starts with LIBER, then send it to all network participants so that they go to the tab, vote and vote for you. You can also vote for other network members, both FOR and AGAINST. You can also remove your voice. Elections: Each network member can vote for a candidate. Each participant has three ways to vote FOR, AGAINST or WITHDRAW VOTE. The vote of a network participant is equal to the amount of staking on his account. That is, if you have 100 coins reserved, you can vote for several candidates, and if you vote AGAINST (NO), then the candidate receives minus 100, if FOR (YES) plus 100. As a result, for each candidate his YES-NO rating is calculated, Thus, seven candidates for the Board of Directors, and the 7 highest ranked candidates for the Council of Corporate Judges are in office. Each network participant can change their vote at any time. Only votes cast in the last two years are counted. Board of Directors: Can approve network decisions, for example, can create a project by approving a decision where participants will voluntarily send money to an approved address. Example: Let's imagine that the board of directors has created a package, where the first line is the address and the amount separated by a space. The second line is a description, and the third is contact information. Moreover, the amount and address do not necessarily have to be in this coin, the board of directors can approve the decision in other currencies, then after the amount, separated by a space you need to register the type of usdt coin or other currency. 1. rDqx8hhZRzNm6xxvL1GL5aWyYoQRKVdjEHqDo5PY2nbM 10000 2. For the development of the project and the acquisition of new servers, you need to collect ten thousand coins 3. citucorp.com According to the decision of the Board of Directors: Each director has a vote equal to his rating share relative to the other directors. Let's imagine that we have 7 directors, 1. First, we will sum up the ratings of all these 7 directors. For example, it turned out to be 200. But director A has a rating of 40, that is, his share is 20% Therefore, for a resolution to be adopted, the resolution must receive a vote of 57% or more from the Directors. If the director has a very high rating, then he can make decisions alone; if he has a low rating, then he will have to get the support of other directors. Similarly, the Board of Directors elects the General Director. Council of Corporate Judges: The Board of Corporate Judges is elected in the same way as the Board of Directors, but it performs several functions 1. Resolves disputes that arise between network participants, creating a precedent. 2. May veto a decision of the Board of Directors if this decision contradicts the Charter or other current decisions. Each Judge has one vote, each Judge can vote YES, NO or abstain, as well as remove their previous vote. For each decision, the votes of the judges are calculated using the following formula: YES - NO. If the number falls below 0, then the decision is VETOed. Powers of the founder; only the founder can approve the new charter. also the property of a corporation belongs to the corporation, but to manage this property Board of Directors (Member accounts are not the property of the corporation). The Board of Directors can finance new projects by collecting money from voluntary contributions, or from sales of goods and services of the corporation, as well as from voluntary membership fees. The merger of this corporation with other corporations can only be carried out after approval by the Founder. Board of Directors A board of directors is appointed to govern a given corporation within the scope of its bylaws. General Director: General Director, implements decisions made by the Board of Directors. 1. Board of Directors (7 participants): Can approve decisions for the network, such as creating projects and raising funds for their implementation. Approves decisions by a majority vote, where the weight of the director's vote depends on his rating relative to other directors. Elects the General Director. May not act contrary to the Charter or decisions imposed by the Council of Corporate Judges. 2. Council of Corporate Judges (7 participants): Resolves disputes between network participants by creating precedents. Can veto decisions of the Board of Directors if they contradict the Charter or other current decisions. Each judge has one vote, decisions are made by majority vote (YES - NO > 0). Cannot make decisions that contradict the Charter approved by the Founder. 3. General Director: Implements decisions made by the Board of Directors. Has no authority have independent decisions, except for the execution of decisions of the Board of Directors. It should be noted that one person can hold several elected positions at the same time, for example, be a member of the Board of Directors and the Council of Corporate Judges. However, in this case, when voting on decisions, his votes will be counted separately for each position in accordance with the established rules.");

        return "how_to_apply_for_a_job";

    }

    @GetMapping("/how_to_make_laws")
    public String howToMakeLaws(Model model) {
        model.addAttribute("text1", "1. Go to the CREATE LAWS PACKAGE tab.\n" +
                "2. fill in the fields, the name of the package must be filled in\n" +
                "in capital letters and if the package name consists of several words, " +
                "then they must be separated by an underscore: Example: LAW_ON_FREEDOM.\n" +
                "after you click the send button, this transaction will go to all participants " +
                "and when the miners include this transaction in the blockchain, it will go " +
                "to the ALL CREATED LAW PACKAGES tab.");
        return "how_to_make_laws";
    }

    @GetMapping("/how_to_vote_and_what_voting_types_are_there")
    public String howToVoteAndWhatVotingTypesAreThere(Model model) {
      return "how_to_vote_and_what_voting_types_are_there";
    }

    @GetMapping("/solving_common_problems")
    public String solvingCommonProblems(Model model) {
        model.addAttribute("titile", "solving common problems");
        List<String> list = new ArrayList<>();
        list.add("1. Problem with the port.\n" +
                "If you see this error on the command line, then take this port.\n" +
                "This problem occurs if you are on the same computer twice without closing the previous\n" +
                "command line trying to run the program. You need to restart your computer.\n" +
                "***************************\n" +
                "APPLICATION FAILED TO START\n" +
                "***************************\n" +
                "Description:\n" +
                "Web server failed to start. Port 8082 was already in use.\n" +
                "action:\n" +
                "Identify and stop the process that's listening on port 8082 or configure this application to listen on another port.\n");
        list.add("2. Sometimes the balance display disappears.\n" +
                "This occurs when, for some reason, the blockchain is incorrectly recorded.\n" +
                "The actual blockchain is always stored in the global server.\n" +
                "And to restore your balance, it is enough to update the blockchain on the main page.\n");
        list.add("3. Your local blockchain outperforms the global one and then gets deleted and part of the balance is lost.\n" +
                "The system after each finding of the block tries to connect to the global network for one minute,\n" +
                "transfer the actual blockchain there. If, for example, there are 20 blocks on the global server,\n" +
                "and you are trying to add N blocks, but their index continues the global blockchain. 21, 22, .... etc.\n" +
                "Then when your wallet can connect, your block will be added.\n" +
                "If your branch is different from the global one and perhaps someone has already added 21 blocks, then your\n" +
                "blocks are removed and your balance is lost because of this. How so your balance contained not up-to-date\n" +
                "blockchain.");

        model.addAttribute("list", list);

        return "solving_common_problems";
    }

    @GetMapping("/wallet_and_node_url")
    public String wallet_and_node_url(Model model){
        model.addAttribute("title", "url for wallet and node");

        return "wallet_and_node_url";
    }

    @GetMapping("/charter")
    public String charter(Model model){
        model.addAttribute("title", "charter");
        return "charter";
    }
    @GetMapping("charter_rus")
    public String charter_rus(){
        return "charter_rus";
    }

    @GetMapping("charter_eng")
    public String charter_eng(){
        return "charter_eng";
    }
    @GetMapping("/vision")
    public String vision_of_the_future(Model model){

        return "vision";
    }

    @GetMapping("white_papper")
    public String white_papper(Model model){
        model.addAttribute("title", "white papper");
        return "white_papper";
    }

    @PostMapping("/calculate_reward")
    public String calculateReward(
            @RequestParam("difficulty") int difficulty,
            @RequestParam("MULTIPLIER") int MULTIPLIER,
            @RequestParam("index") int index,
            Model model
    ) {
        double rewardG0 = UtilsUse.rewardCalculatorreward(0, difficulty, MULTIPLIER, index);
        double rewardG3 = UtilsUse.rewardCalculatorreward(3, difficulty, MULTIPLIER, index);

        model.addAttribute("rewardG0", rewardG0);
        model.addAttribute("rewardG3", rewardG3);

        return "how_to_mining";
    }
}
