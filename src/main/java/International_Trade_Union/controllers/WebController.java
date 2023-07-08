package International_Trade_Union.controllers;

import International_Trade_Union.setings.Seting;
import International_Trade_Union.vote.LawEligibleForParliamentaryApproval;
import International_Trade_Union.vote.Laws;
import International_Trade_Union.vote.UtilsLaws;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SignatureException;
import java.security.spec.InvalidKeySpecException;
import java.util.ArrayList;
import java.util.List;

@Controller
public class WebController {
    @GetMapping("/")
    public String mainPage(Model model){
        model.addAttribute("title", "Main page test");
        model.addAttribute("Summary", "Summary and Benefits");
        model.addAttribute("discord", "https://discord.gg/MqkvC3SGHH");
        model.addAttribute("telegram", "https://t.me/citu_coin");
        model.addAttribute("github", "https://github.com/CorporateFounder/unitedStates_final");
        model.addAttribute("storage", "https://github.com/CorporateFounder/unitedStates_storage");


        model.addAttribute("text",
                "1. There are no such problems as halving or inflation, as in other coins. All coins are " +
                "divided into two groups, they either reduce production like bitcoin every four years," +
                " which leads to the bankruptcy of small miners, since the cost of electricity is constant," +
                " and the cost does not always double after halving, which reduces profits. " +
                "There are coins that do not limit production, but then inflation occurs." +
                " In this coin, we burn from all accounts every six months 0.1% of digital dollars and 0.2% " +
                "of digital shares, which allows miners to always mine the same number of coins, but the " +
                "total amount of money never increases.\n" +
                "2. A unique electoral system that allows you to elect your representatives and directly " +
                "vote on the rules of the network, which will prevent the community from splitting as" +
                " it happened in other coins, when the community was forced to create hard forks simply" +
                " because of the block size. This system has a contract mechanism.\n" +
                "3. You can mine blocks immediately from your computer on a local server." +
                " Your blocks will be transferred to the global server, which only stores," +
                " distributes and records the blockchain.\n" +
                "4. You can mine up to 576 blocks per day, and for" +
                " each block you will receive 400 digital dollars and 400 digital shares. " +
                "5. The unique architecture allows you to maintain a higher reliability of your money, " +
                "while maintaining high competition among miners, allowing participants to mine more blocks.");

        return "main";
    }

    @GetMapping("/summary_and_benefits")
    public String summaryAndBenefits(Model model){
        List<String> strings = new ArrayList<>();
        strings.add("Why is the voting system implemented in this system.\n" +
                "\n" +
                "We have seen a lot of cryptocurrencies that split due to small problems.\n" +
                "and instead of one coin, we get hundreds, which reduced the value of the coin, as well as\n" +
                "to capital losses.\n" +
                "Each of you knows what is in one of the best coins, and the founder of all cryptocurrency,\n" +
                "such as bitcoin, the reason for the split was simply the size of the block and from this was created\n" +
                "many coins. In this system, shareholders, and everyone who has shares are shareholders,\n" +
                "can solve such problems through voting and each decision will be valid only\n" +
                "4 years, and if this decision is still relevant in four years, then the participants\n" +
                "can easily support this decision again.\n" +
                "1. Direct Democracy allows you to vote for Laws and members directly. This\n" +
                "   a measure is needed when people have their own opinion on some specific issues.\n" +
                "2. Factions are your delegates and represent the share that shareholders support.\n" +
                "   Factions vote on the rules of the network");

        strings.add("Brief description of the cryptocurrency\n" +
                "This cryptocurrency is unique, since the total money supply does not grow, but at the same time, the production of miners does not decrease.\n" +
                "How did we achieve this?! All cryptocurrencies in the world now use only two strategies and\n" +
                "I will give on the most successful currencies Bitcoin and Dogecoin.\n" +
                "1. Bitcoin, in order to limit the number of coins, reduces production by half every four years.\n" +
                "   But if the value does not double after each reduction, then many small players will go bankrupt,\n" +
                "   as the cost of production is maintained, and profits are reduced. transaction costs can't either\n" +
                "   grow, because if the cost is excessively high, then it makes no sense to acquire these coins.\n" +
                "2. Dogecoin removed production cuts, but this creates inflation as the money supply continues to grow,\n" +
                "   which causes problems.\n" +
                "3. My coin burns 0.1% of digital dollars and 0.2% of digital shares from all accounts every half a year,\n" +
                "   which allows miners to always mine 400 digital dollars and 400 digital shares for each block,\n" +
                "   nor does the money supply grow as extraction and destruction come into equilibrium.\n" +
                "4. You do not need to create a server with a white ip for mining. Since all local servers send\n" +
                "   their blocks to a global server that stores, updates and transmits the actual blockchain.\n" +
                "5. All your transactions automatically go to the global server and all miners automatically\n" +
                "   they take transactions and add them to the block, so the chances that your transfer will be added to the block,\n" +
                "   significantly higher.\n" +
                "6. Uses unique SHA-256 algorithm. In this system, the complexity is determined by the number\n" +
                "   zeros in the hash string when finding a block, but the actual blockchain is not only the longest blockchain,\n" +
                "   but also one where the sum of all zeros is greater than that of an alternative blockchain.\n" +
                "7. About 576 blocks are mined per day, which allows more to be mined.\n" +
                "8. Difficulty is adjusted every half a year.");
        model.addAttribute("title", "Summary and benefits");
        model.addAttribute("texts", strings);
        return "summary_and_benefits";
    }
    @GetMapping("/how_to_install")
    public String installPage(Model model){
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

    @GetMapping("/how_to_open_an_account")
    public String howToOpenAnAccount(Model model){
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
    public String howToChangeMinerAccount(Model model){
        model.addAttribute("title", "How to change miner account");
        model.addAttribute("text1", "Start local server and login http://localhost:8082/seting\n" +
                "or click the settings button, enter your ADDRESS (PUBLIC KEY)\n" +
                "ENTER PUBKEY TO CHANGE MINER ADDRESS and press the button\n" +
                "CHANGE MINER ADDRESS. UtilsFileSaveRead.save() saves the new public account to a file\n" +
                "in folder: resources/minerAccount/minerAccount.txt");
        return "how_to_change_miner_account";
    }

    @GetMapping("/how_to_mining")
    public String howToMining(Model model){
        model.addAttribute("title", "How to mining");
        model.addAttribute("text1", "Block mining\n" +
                "\n" +
                "HOW TO START MINING\n" +
                "Before you start mining blocks, you\n" +
                "you need to set the address of the miner to which the block will be mined.\n" +
                "Once you have set your address as a miner, there are two options.\n" +
                "\n" +
                "OPTION 1.\n" +
                "To start mining, after launch, go to\n" +
                "there will be a button on http://localhost:8082/miningblock. START MINING\n" +
                "clicking on it will automatically produce a block.");

        model.addAttribute("text2", "### OPTION 2\n" +
                "push button ***Constant mining 576 block in while***\n" +
                "there will be a cycle of 576 attempts to find blocks\n" +
                "\n" +
                "\n" +
                "OPTION 3.\n" +
                "calling http://localhost:8082/mine automatically starts mining.\n" +
                "\n" +
                "\n" +
                "\n" +
                "\n" +
                "Blockchain complexity adapts similarly to bitcoin, but adaptation happens\n" +
                "once every half day.\n" +
                "Each block gives 400 digital dollars and 400 digital shares\n" +
                "\n" +
                "The current blockchain is not only the longest blockchain, but it should also have more zeros.\n" +
                "\n" +
                "this method counts the number of zeros in the blockchain and the current blockchain, not only the longest, but also the one with the most zeros\n");

        return "how_to_mining";
    }

    @GetMapping("/how_to_transaction")
    public String howToTransaction(Model model){
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
    public String howToApplyForAJob(Model model){
        model.addAttribute("title", "how to apply for a job");
        model.addAttribute("text1", "In this system, you can be elected as an independent judge, chief executive officer, chief justice, or a faction delegate representing a certain percentage of the shareholders. All positions are elected.\n" +
                "1. All positions are elected.\n" +
                "2. ordinary judges and factions are elected by any member of the network,\n" +
                "who has shares.\n" +
                "3. The chief judge is elected by the judges.\n" +
                "4. the general executive judge is elected by the faction\n" +
                "5. but it is important for the chief justice and the CEO that the shareholders also vote positively.\n" +
                "6. at any time, network members can change their vote and remove from office.\n" +
                "7. It is the rating that is taken into account everywhere, the rating is calculated as all votes YES-NO and this result is the rating.\n" +
                "8. Only votes cast in the last four years count.");
        model.addAttribute("text2", "1. all places are limited.\n" +
                "2. Judges can only be 55 accounts with the highest number of ratings received from rating shares.\n" +
                "3. fractions can be only two hundred with the highest number of ratings received from the rating of shares.\n" +
                "4. Only one account with the highest number of ratings received from shares and from factions can be the CEO.\n" +
                "5. Only one account with the highest number of ratings received from shares and from judges can be the chief judge.");

        return "how_to_apply_for_a_job";

    }
}
