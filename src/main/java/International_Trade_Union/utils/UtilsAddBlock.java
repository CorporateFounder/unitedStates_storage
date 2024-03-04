package International_Trade_Union.utils;

import International_Trade_Union.controllers.BasisController;
import International_Trade_Union.entity.blockchain.block.Block;
import International_Trade_Union.entity.entities.EntityAccount;
import International_Trade_Union.entity.entities.EntityBlock;
import International_Trade_Union.entity.services.BlockService;
import International_Trade_Union.model.Account;
import International_Trade_Union.model.Mining;
import International_Trade_Union.setings.Seting;
import International_Trade_Union.vote.LawEligibleForParliamentaryApproval;
import International_Trade_Union.vote.Laws;
import International_Trade_Union.vote.UtilsLaws;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SignatureException;
import java.security.spec.InvalidKeySpecException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static International_Trade_Union.utils.UtilsBalance.calculateBalance;

@Component
public class UtilsAddBlock {
    @Autowired
    BlockService blockService;


    public BlockService getBlockService() {
        return blockService;
    }

    public  void addBlock2(List<Block> originalBlocks, Map<String, Account> balances) throws IOException, NoSuchAlgorithmException, InvalidKeySpecException, SignatureException, NoSuchProviderException, InvalidKeyException, CloneNotSupportedException {

        //TODO найти различия после calculate Balance, после произсвести
        //TODO сделать запись только те которые изменились.
        Map<String, Account> prevBalances = UtilsUse.balancesClone(balances);

        System.out.println(" addBlock2 start: ");

        List<String> signs = new ArrayList<>();

        List<LawEligibleForParliamentaryApproval> allLawsWithBalance = new ArrayList<>();

        List<EntityBlock> entityBlocks = new ArrayList<>();
        //write a new blockchain from scratch to the resources folder
        //записать с нуля новый блокчейн в папку resources
        for (Block block : originalBlocks) {
            System.out.println(" :BasisController: addBlock2: blockchain is being updated: ");
            UtilsBlock.saveBLock(block, Seting.ORIGINAL_BLOCKCHAIN_FILE);
            EntityBlock entityBlock = UtilsBlockToEntityBlock.blockToEntityBlock(block);
            entityBlocks.add(entityBlock);
            calculateBalance(balances, block, signs);


        }


        blockService.getEntityBlockRepository().saveAll(entityBlocks);

//        List<EntityAccount> entityBalances = UtilsAccountToEntityAccount
//                .accountsToEntityAccounts(balances);
//        blockService.getEntityAccountRepository().saveAll(entityBalances);
//        List<EntityAccount> entityAccounts = new ArrayList<>();

        //TODO найти решение для оптимизации
//        for (Map.Entry<String, Account> accountEntry : balances.entrySet()) {
//            if(accountEntry.getValue().getAccount() != null || !accountEntry.getValue().getAccount().isBlank()){
//                EntityAccount temp = BlockService.entityAccount(accountEntry.getValue().getAccount());
//                if(temp != null){
//                    temp.setDigitalDollarBalance(accountEntry.getValue().getDigitalDollarBalance());
//                    temp.setDigitalStockBalance(accountEntry.getValue().getDigitalStockBalance());
//                    temp.setDigitalStakingBalance(accountEntry.getValue().getDigitalStakingBalance());
//                    entityAccounts.add(temp);
//                }else {
//                    temp = UtilsAccountToEntityAccount.account(accountEntry.getValue());
//                    entityAccounts.add(temp);
//                }
//
//            }
//
//        }
//        BlockService.saveAccountAll(entityAccounts);

        Mining.deleteFiles(Seting.ORIGINAL_BALANCE_FILE);
        SaveBalances.saveBalances(balances, Seting.ORIGINAL_BALANCE_FILE);

        System.out.println(":BasisController: addBlock2: finish: " + originalBlocks.size());
    }


    public  void addBlock(List<Block> orignalBlocks, Map<String, Account> balances) throws IOException, NoSuchAlgorithmException, SignatureException, InvalidKeySpecException, NoSuchProviderException, InvalidKeyException {
        System.out.println("start addBLock");
        BasisController.setIsSaveFile(false);
        System.out.println("start  save in addBlock");
        List<String> signs = new ArrayList<>();

        List<LawEligibleForParliamentaryApproval> allLawsWithBalance = new ArrayList<>();
        List<EntityBlock> entityBlocks = new ArrayList<>();
        for (Block block : orignalBlocks) {
            UtilsBlock.saveBLock(block, Seting.ORIGINAL_BLOCKCHAIN_FILE);
            calculateBalance(balances, block, signs);

            EntityBlock entityBlock = UtilsBlockToEntityBlock.blockToEntityBlock(block);
            entityBlocks.add(entityBlock);
        }

        blockService.getEntityBlockRepository().saveAll(entityBlocks);

        List<EntityAccount> entityBalances = UtilsAccountToEntityAccount
                .accountsToEntityAccounts(balances);
        blockService.getEntityAccountRepository().saveAll(entityBalances);
        blockService.saveAccountAll(entityBalances);
        System.out.println("finish save in addBlock");
        System.out.println("BasisController: addBlock: finish");


        Mining.deleteFiles(Seting.ORIGINAL_BALANCE_FILE);
        SaveBalances.saveBalances(balances, Seting.ORIGINAL_BALANCE_FILE);




        BasisController.setIsSave(true);

    }
}
