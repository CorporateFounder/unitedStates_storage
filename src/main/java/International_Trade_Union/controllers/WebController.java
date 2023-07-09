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
        model.addAttribute("size", BasisController.getBlockcheinSize());
        model.addAttribute("difficulty", BasisController.getDificultyOneBlock());
        model.addAttribute("difficultyAll", BasisController.getShortDataBlockchain().getHashCount());
        model.addAttribute("title", "Main page");
        model.addAttribute("Summary", "Summary and Benefits");
        model.addAttribute("discord", "https://discord.gg/MqkvC3SGHH");
        model.addAttribute("telegram", "https://t.me/citu_coin");
        model.addAttribute("github", "https://github.com/CorporateFounder/unitedStates_final");
        model.addAttribute("storage", "https://github.com/CorporateFounder/unitedStates_storage");



        model.addAttribute("text",
                "1. The unique mining algorithm allows participants to continuously mine 400 digital dollars and digital shares without creating inflation.\n" +
                        "2. Every six months, 0.1% of digital dollars and 0.2% of digital shares are burned, which allows the destruction of the same amount of coins that was created. Thus the total number of coins in a hundred years will not exceed 11 billion and will always be about 10 billion.\n" +
                        "3. Difficulty is adjusted every 12 hours, allowing members to mine 576 blocks daily.\n" +
                        "4. High transaction speed, as all transactions go to the global server and all miners simultaneously add transactions to the block, so the chance of your transaction hitting is higher than in other currencies.\n" +
                        "5. The world's first cryptocurrency to achieve coin cap by burning and there are no more halvings that bring bankruptcies.\n" +
                        "6. A unique voting system that allows participants to coordinate their actions very quickly and with minimal losses.\n" +
                        "7. Due to the fact that coins are burned every half a year, conditions are created for exchange rate stability and become profitable for long-term investments." +
                        "8. The number of mined coins and the percentage of burning was determined on the basis of knowledge of macroeconomics and also took into account such schools as the monetarism of Milton Friedman, the Austrian School of Economics, Silvio Gezel, as well as rich experience in the field of sociology, and trading on the forex exchange. Every detail including voting has been developed on the basis of scientific knowledge and scientific articles in the field of politics and economics." +
                        "9. This system uses the SHA - 256 algorithm where a block is considered valid if its number of leading zeros corresponds to the complexity of the defined algorithm, which is optimized every 12 hours."
                        );

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
                "5. Only one account with the highest number of ratings received from shares and from judges can be the chief judge." +
                "all positions thus elected are legitimate.");
        model.addAttribute("text3", "1. for the election of judges and factions, it is necessary that the rating with the help of shares be more than one,\n" +
                "2. further all participants are sorted and participants with the highest number of ratings are selected.\n" +
                "3. In order to elect a Chief Justice, apart from the fact that his rating received from shares must be high, he must also receive at least a rating from judges of 2 or more.\n" +
                "4. For the CEO, also the rating received from the shares must be the highest and the rating from the factions must be 15% or more.\n" +
                "Judges vote according to the ONE_VOTE type, participants who have just shares vote according to the STOCK_VOTE type and fractions vote according to the FRACTION_VOTE type");

        return "how_to_apply_for_a_job";

    }

    @GetMapping("/how_to_make_laws")
    public String howToMakeLaws(Model model){
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
    public String howToVoteAndWhatVotingTypesAreThere(Model model){
        model.addAttribute("text1", "There are three types of voting that are used here." +
                "1. ONE_VOTE (One Voice)\n" +
                "\n" +
                "When these positions are voted count as one score = one vote\n" +
                "(CORPORATE_COUNCIL_OF_REFEREES-Council of Corporate Judges,\n" +
                "GENERAL_EXECUTIVE_DIRECTOR-General Executive Director,\n" +
                "HIGH_JUDGE - Supreme Judge and Board of Shareholders).\n" +
                "Each score that starts with LIBER counts all votes FOR (VoteEnum.YES) and AGAINST (VoteEnum.NO) for it\n" +
                "further deducted from FOR - AGAINST = if the balances are above the threshold, then it becomes the current law. But if a position is elected,\n" +
                "then after that it is sorted from largest to smallest and the largest number that is described for this position is selected.\n" +
                "Recalculation of votes occurs every block.\n" +
                "\n" +
                "After voting, the vote can only be changed to the opposite one.\n" +
                "There is no limit on the number of times you can change your vote. Only those votes that are given by accounts are taken into account\n" +
                "in his position, for example, if the account ceased to be in CORPORATE_COUNCIL_OF_REFEREES, his vote as\n" +
                "CORPORATE_COUNCIL_OF_REFEREES does not count and will not count in voting. All votes are valid until the bills\n" +
                "voters are in their positions. Only those votes from which no more than\n" +
                "four years, but each participant may at any time renew their vote." +
                "" +
                "2. VOTE_STOCK (How shares are voted.)\n" +
                "How shares are voted.\n" +
                "1. The number of shares is equal to the number of votes.\n" +
                "2. Your votes are recounted every block and if you lose your shares,\n" +
                "   or increase your shares, your cast votes also change\n" +
                "   according to the number of shares.\n" +
                "3. For each law that you voted, for this law, all\n" +
                "   FOR and AGAINST and after that with FOR minus AGAINST and this is the rating of the law.\n" +
                "4. Your votes are divided separately for all the laws that you voted FOR and separately AGAINST\n" +
                "   Example: you have 100 shares and you voted FOR one candidate and for one law,\n" +
                "   you also voted AGAINST two candidates and two laws.\n" +
                "   Now each of your candidates and the law for which you voted FOR will receive 50 votes.\n" +
                "   and for which you voted AGAINST will receive 25 votes AGAINST.\n" +
                "   the formula is simple FOR/number of laws and AGAINST/number of laws you are against." +
                "" +
                "3. FAVORITE_FRACTION\n" +
                "The faction is extracted like the chief judges, 200 scores received by the maximum number of votes\n" +
                "from a unique electoral one, as previously and an observed share equal to one vote of the described\n" +
                "in VOTE_STOCK\n" +
                "\n" +
                "#VOTE_FRACTION\n" +
                "This voting system is used only for factions.\n" +
                "First, 200 factions are selected that have become legitimate.\n" +
                "Then all the votes given to 200 selected factions are summed up.\n" +
                "After that, the share of each fraction from the total amount is determined.\n" +
                "votes cast for this faction.\n" +
                "The number of votes of each faction is equal to its percentage shares.\n" +
                "Thus, if a faction has 23% of the votes of all votes, out of\n" +
                "200 factions, then her vote is equal to 23%.\n" +
                "On behalf of the factions, the leaders always act and because of this it is\n" +
                "First of all, the leader system. Identical factions with ideological\n" +
                "system here can be represented by different leaders, even\n" +
                "if they are from the same community.\n" +
                "\n" +
                "Then every time a faction votes for laws,\n" +
                "that start with LIBER (VoteEnum.YES) or (VoteEnum.NO).\n" +
                "This law counts all the votes given *** for ***\n" +
                "and *** against ***, after which it is subtracted from *** for *** - *** against ***.\n" +
                "This result is displayed as a percentage.");

        model.addAttribute("text2", "to vote you have to do a few things.\n" +
                "1. First you must go to the tab of all created law packages.\n" +
                "2. see the details of this law, if it is a network rule, there will be a list of laws inside the package.\n" +
                "3. if this is a candidate, then the first line inside the packet will be the address of the candidate.\n" +
                "4. copy the address of the law, it always starts with LIBER\n" +
                "5. enter the vote tab.\n" +
                "6. Enter your address in the first line.\n" +
                "7. to the second address of the law,\n" +
                "8. enter the amount of remuneration to the earner in digital dollars. Choose your vote YES or NO and click vote.");

        model.addAttribute("text3", "1. If you are an independent member and your account is not an elected position, then your vote will be counted according to the second type.\n" +
                "2. If you are a judge and you vote for chief judge, then your vote will be counted according to the first type only if you vote for chief judge, as a judge.\n" +
                "3. if you are a faction and you vote for laws or the CEO, your vote will be counted by\n" +
                "the third type, but if you do not vote for candidates of other positions.\n" +
                "If you are a member of the Board of Shareholders and vote for amendments, then you vote by type 1.\n" +
                "\n" +
                "1. how the faction is elected. The 200 candidates who received the highest number of share rankings become factions.\n" +
                "2. how judges are elected. The 55 candidates who receive the most votes in the ranking of the shares become judges.\n" +
                "3. How the Chief Justice is elected. 1 candidate who received the most votes of the share rating and the most (more than 2 votes) the number of ratings from the votes of the judges, becomes the supreme judge.\n" +
                "4. how the CEO is elected.\n" +
                "The 1 candidate who receives the most share ranking votes and the most faction ranking votes (more than 15% of the rating) becomes the Executive CEO.\n" +
                "5. How laws are elected, any package of laws must receive more than 1 rating from the number of votes of shares and a rating from the votes of factions above 15% percent, then it is valid.");

        return "how_to_vote_and_what_voting_types_are_there";
    }

    @GetMapping("/solving_common_problems")
    public String solvingCommonProblems(Model model){
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
}
