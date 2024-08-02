package International_Trade_Union.utils;

import International_Trade_Union.controllers.BasisController;
import International_Trade_Union.entity.DtoTransaction.DtoTransaction;
import International_Trade_Union.entity.blockchain.Blockchain;
import International_Trade_Union.entity.blockchain.block.Block;
import International_Trade_Union.entity.services.BlockService;
import International_Trade_Union.model.Account;
import International_Trade_Union.setings.Seting;
import International_Trade_Union.utils.base.Base;
import International_Trade_Union.utils.base.Base58;
import International_Trade_Union.vote.LawEligibleForParliamentaryApproval;
import International_Trade_Union.vote.Laws;
import International_Trade_Union.vote.VoteEnum;

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

import static International_Trade_Union.setings.Seting.SPECIAL_FORK_BALANCE;

public class UtilsBalance {
    private static BlockService blockService;

    public static BlockService getBlockService() {
        return blockService;
    }

    public static void setBlockService(BlockService blockService) {
        UtilsBalance.blockService = blockService;
    }

    public static Map<String, Account> rollbackCalculateBalance(
            Map<String, Account> balances,
            Block block
    ) throws IOException, NoSuchAlgorithmException, SignatureException, InvalidKeySpecException, NoSuchProviderException, InvalidKeyException {
        Base base = new Base58();
        System.out.println("start rollbackCalculateBalance: index: " + block.getIndex());
        int i = (int) block.getIndex();

        int BasisSendCount = 0;
        for (int j = 0; j < block.getDtoTransactions().size(); j++) {
            DtoTransaction transaction = block.getDtoTransactions().get(j);
            if (transaction.getSender().startsWith(Seting.NAME_LAW_ADDRESS_START)) {
                System.out.println("law balance cannot be sender");
                continue;
            }
            if (transaction.verify()) {
                if (transaction.getSender().equals(Seting.BASIS_ADDRESS))
                    BasisSendCount++;

                Account sender = getBalance(transaction.getSender(), balances);
                Account customer = getBalance(transaction.getCustomer(), balances);
                Account miner = getBalance(block.getMinerAddress(), balances);

                boolean sendTrue = true;
                if (sender.getAccount().equals(Seting.BASIS_ADDRESS) && BasisSendCount > 2) {
                    System.out.println("Basis address can send only two the base address can send no more than two times per block:" + Seting.BASIS_ADDRESS);
                    continue;
                }

                double minerRewards = Seting.DIGITAL_DOLLAR_REWARDS_BEFORE;
                double digitalReputationForMiner = Seting.DIGITAL_STOCK_REWARDS_BEFORE;

                if (block.getIndex() > Seting.CHECK_UPDATING_VERSION) {
                    minerRewards = block.getHashCompexity() * Seting.MONEY;
                    digitalReputationForMiner = block.getHashCompexity() * Seting.MONEY;
                    minerRewards += block.getIndex() % 2 == 0 ? 0 : 1;
                    digitalReputationForMiner += block.getIndex() % 2 == 0 ? 0 : 1;
                }

                if (block.getIndex() > Seting.V28_CHANGE_ALGORITH_DIFF_INDEX && block.getIndex() < Seting.V34_NEW_ALGO) {
                    minerRewards = 261;
                    digitalReputationForMiner = 261;
                } else if (block.getIndex() >= Seting.V34_NEW_ALGO) {
                    minerRewards = 1500;
                    digitalReputationForMiner = 1500;
                }

                if (block.getIndex() == Seting.SPECIAL_BLOCK_FORK && block.getMinerAddress().equals(Seting.FORK_ADDRESS_SPECIAL)) {
                    minerRewards = SPECIAL_FORK_BALANCE;
                    digitalReputationForMiner = SPECIAL_FORK_BALANCE;
                }

                if (sender.getAccount().equals(Seting.BASIS_ADDRESS)) {
                    if (i > 1 && (transaction.getDigitalDollar() > minerRewards || transaction.getDigitalStockBalance() > digitalReputationForMiner)) {
                        System.out.println("rewards cannot be upper than " + minerRewards);
                        System.out.println("rewards cannot be upper than " + digitalReputationForMiner);
                        System.out.println("rewards dollar: " + transaction.getDigitalDollar());
                        System.out.println("rewards stock: " + transaction.getDigitalStockBalance());
                        continue;
                    }
                    if (!customer.getAccount().equals(block.getFounderAddress()) && !customer.getAccount().equals(block.getMinerAddress())) {
                        System.out.println("Basis address can send only to founder or miner");
                        continue;
                    }
                }
                sendTrue = UtilsBalance.rollBackSendMoney(
                        sender,
                        customer,
                        miner,
                        transaction.getDigitalDollar(),
                        transaction.getDigitalStockBalance(),
                        transaction.getBonusForMiner(),
                        transaction.getVoteEnum(),
                        block.getIndex());

                if (sendTrue) {
                    balances.put(sender.getAccount(), sender);
                    balances.put(customer.getAccount(), customer);
                    if (block.getIndex() > Seting.NEW_ALGO_MINING)
                        balances.put(miner.getAccount(), miner);
                }
            }
        }

        System.out.println("finish calculateBalance");
        return balances;
    }

    public static Map<String, Account> calculateBalance(
            Map<String, Account> balances,
            Block block,
            List<String> sign) throws IOException, NoSuchAlgorithmException, SignatureException, InvalidKeySpecException, NoSuchProviderException, InvalidKeyException {

        Base base = new Base58();
        System.out.println("calculateBalance: index: " + block.getIndex());
        int i = (int) block.getIndex();

        int BasisSendCount = 0;
        for (int j = 0; j < block.getDtoTransactions().size(); j++) {
            DtoTransaction transaction = block.getDtoTransactions().get(j);
            if (blockService != null) {
                if (blockService.existsBySign(transaction.getSign())) {
                    System.out.println("this transaction signature has already been used and is not valid from db");
                    continue;
                }
            }
            if (sign.contains(base.encode(transaction.getSign()))) {
                System.out.println("this transaction signature has already been used and is not valid");
                continue;
            } else {
                sign.add(base.encode(transaction.getSign()));
            }

            if (transaction.getSender().startsWith(Seting.NAME_LAW_ADDRESS_START)) {
                System.out.println("law balance cannot be sender");
                continue;
            }
            Account sender = getBalance(transaction.getSender(), balances);
            Account customer = getBalance(transaction.getCustomer(), balances);
            Account miner = getBalance(block.getMinerAddress(), balances);
            if (transaction.verify()) {
                if (transaction.getSender().equals(Seting.BASIS_ADDRESS)) {
                    BasisSendCount++;
                    if (sender.getAccount().equals(Seting.BASIS_ADDRESS) && BasisSendCount > 2) {
                        System.out.println("Basis address can send only two the base address can send no more than two times per block:" + Seting.BASIS_ADDRESS);
                        continue;
                    }
                }

                boolean sendTrue = true;

                double minerRewards = Seting.DIGITAL_DOLLAR_REWARDS_BEFORE;
                double digitalReputationForMiner = Seting.DIGITAL_STOCK_REWARDS_BEFORE;

                if (block.getIndex() > Seting.CHECK_UPDATING_VERSION) {
                    minerRewards = block.getHashCompexity() * Seting.MONEY;
                    digitalReputationForMiner = block.getHashCompexity() * Seting.MONEY;
                    minerRewards += block.getIndex() % 2 == 0 ? 0 : 1;
                    digitalReputationForMiner += block.getIndex() % 2 == 0 ? 0 : 1;
                }

                if (block.getIndex() > Seting.V28_CHANGE_ALGORITH_DIFF_INDEX && block.getIndex() < Seting.V34_NEW_ALGO) {
                    minerRewards = 261;
                    digitalReputationForMiner = 261;
                } else if (block.getIndex() >= Seting.V34_NEW_ALGO) {
                    minerRewards = 1500;
                    digitalReputationForMiner = 1500;
                }

                if (block.getIndex() == Seting.SPECIAL_BLOCK_FORK && block.getMinerAddress().equals(Seting.FORK_ADDRESS_SPECIAL)) {
                    minerRewards = SPECIAL_FORK_BALANCE;
                    digitalReputationForMiner = SPECIAL_FORK_BALANCE;
                }

                if (sender.getAccount().equals(Seting.BASIS_ADDRESS)) {
                    if (i > 1 && (transaction.getDigitalDollar() > minerRewards || transaction.getDigitalStockBalance() > digitalReputationForMiner)) {
                        System.out.println("rewards cannot be upper than " + minerRewards);
                        System.out.println("rewards cannot be upper than " + digitalReputationForMiner);
                        System.out.println("rewards dollar: " + transaction.getDigitalDollar());
                        System.out.println("rewards stock: " + transaction.getDigitalStockBalance());
                        continue;
                    }
                    if (!customer.getAccount().equals(block.getFounderAddress()) && !customer.getAccount().equals(block.getMinerAddress())) {
                        System.out.println("Basis address can send only to founder or miner");
                        continue;
                    }
                }
                sendTrue = UtilsBalance.sendMoney(
                        sender,
                        customer,
                        miner,
                        transaction.getDigitalDollar(),
                        transaction.getDigitalStockBalance(),
                        transaction.getBonusForMiner(),
                        transaction.getVoteEnum(),
                        block.getIndex());

                if (sendTrue) {
                    balances.put(sender.getAccount(), sender);
                    balances.put(customer.getAccount(), customer);
                    if (block.getIndex() > Seting.NEW_ALGO_MINING)
                        balances.put(miner.getAccount(), miner);
                }
            }
        }
        return balances;
    }

    public static Map<String, Account> calculateBalances(List<Block> blocks) throws IOException, NoSuchAlgorithmException, SignatureException, InvalidKeySpecException, NoSuchProviderException, InvalidKeyException {
        Map<String, Account> balances = new HashMap<>();
        List<String> signs = new ArrayList<>();
        Map<String, Laws> allLaws = new HashMap<>();
        List<LawEligibleForParliamentaryApproval> allLawsWithBalance = new ArrayList<>();
        for (Block block : blocks) {
            calculateBalance(balances, block, signs);
        }
        return balances;
    }

    public static Account getBalance(String address, Map<String, Account> balances) {
        if (balances.containsKey(address)) {
            return balances.get(address);
        } else {
            Account account = new Account(address, 0.0, 0.0, 0.0);
            return account;
        }
    }

    public static Account findAccount(Blockchain blockList, String address) throws IOException, NoSuchAlgorithmException, SignatureException, InvalidKeySpecException, NoSuchProviderException, InvalidKeyException {
        Map<String, Account> accountMap = calculateBalances(blockList.getBlockchainList());
        Account account = accountMap.get(address);
        return account != null ? account : new Account(address, 0.0, 0.0, 0.0);
    }

    public static boolean sendMoney(Account senderAddress, Account recipientAddress, Account minerAddress, double digitalDollar, double digitalStock, double minerRewards,  long indexBlock ){
        try{
            return sendMoney(senderAddress, recipientAddress, minerAddress, digitalDollar, digitalStock, minerRewards, VoteEnum.YES, indexBlock);
        }catch (Exception e){

            return false;
        }

    }
    public static boolean sendMoney(Account senderAddress, Account recipientAddress, Account minerAddress, double digitalDollar, double digitalStock, double minerRewards, VoteEnum voteEnum, long indexBlock) throws NoSuchAlgorithmException, InvalidKeySpecException, NoSuchProviderException, IOException, SignatureException, InvalidKeyException {
        double senderDigitalDollar = senderAddress.getDigitalDollarBalance();
        double senderDigitalStock = senderAddress.getDigitalStockBalance();
        double senderDigitalStaking = senderAddress.getDigitalStakingBalance();
        double recipientDigitalDollar = recipientAddress.getDigitalDollarBalance();
        double recipientDigitalStock = recipientAddress.getDigitalStockBalance();

        boolean sendTrue = true;

        if (!senderAddress.getAccount().equals(Seting.BASIS_ADDRESS)) {
            if (senderDigitalStock < digitalStock) {
                System.out.println("less stock");
                sendTrue = false;

            } else if (recipientAddress.getAccount().equals(Seting.BASIS_ADDRESS)) {
                System.out.println("Basis cannot be recipient;");
                sendTrue = false;
            } else if ((voteEnum.equals(VoteEnum.YES) || voteEnum.equals(VoteEnum.NO))) {
                if (senderAddress.getAccount().equals(recipientAddress.getAccount())) {
                    System.out.println("sender %s, recipient %s cannot be equals! Error!".format(senderAddress.getAccount(), recipientAddress.getAccount()));
                    sendTrue = false;
                    return sendTrue;
                }
                if (senderDigitalDollar < digitalDollar + minerRewards) {
                    System.out.println("less dollar");
                    sendTrue = false;
                    return sendTrue;
                }

                if (BasisController.getBlockchainSize() > Seting.START_BLOCK_DECIMAL_PLACES) {
                    senderAddress.setDigitalDollarBalance(UtilsUse.round(senderDigitalDollar - digitalDollar, Seting.DECIMAL_PLACES));
                    senderAddress.setDigitalStockBalance(UtilsUse.round(senderDigitalStock - digitalStock, Seting.DECIMAL_PLACES));
                    recipientAddress.setDigitalDollarBalance(UtilsUse.round(recipientDigitalDollar + digitalDollar, Seting.DECIMAL_PLACES));
                } else {
                    senderAddress.setDigitalDollarBalance(senderDigitalDollar - digitalDollar);
                    senderAddress.setDigitalStockBalance(senderDigitalStock - digitalStock);
                    recipientAddress.setDigitalDollarBalance(recipientDigitalDollar + digitalDollar);
                }
                // сделано чтобы можно было увеличить или отнять власть
                if (voteEnum.equals(VoteEnum.YES)) {
                    if (BasisController.getBlockchainSize() > Seting.START_BLOCK_DECIMAL_PLACES) {
                        recipientAddress.setDigitalStockBalance(UtilsUse.round(recipientDigitalStock + digitalStock, Seting.DECIMAL_PLACES));
                    } else {
                        recipientAddress.setDigitalStockBalance(recipientDigitalStock + digitalStock);
                    }
                } else if (voteEnum.equals(VoteEnum.NO)) {
                    // политика сдерживания.
                    if (BasisController.getBlockchainSize() > Seting.START_BLOCK_DECIMAL_PLACES) {
                        recipientAddress.setDigitalStockBalance(UtilsUse.round(recipientDigitalStock - digitalStock, Seting.DECIMAL_PLACES));
                    } else {
                        recipientAddress.setDigitalStockBalance(recipientDigitalStock - digitalStock);
                    }
                }

                if (indexBlock > Seting.NEW_ALGO_MINING) {
                    updateMinerBalance(minerAddress, senderAddress, recipientAddress, minerRewards);
                }

            } else if (voteEnum.equals(VoteEnum.STAKING)) {
                System.out.println("STAKING: ");
                if (senderDigitalDollar < digitalDollar + minerRewards) {
                    System.out.println("less dollar");
                    sendTrue = false;
                    return sendTrue;
                }
                if (BasisController.getBlockchainSize() > Seting.START_BLOCK_DECIMAL_PLACES) {
                    senderAddress.setDigitalDollarBalance(UtilsUse.round(senderDigitalDollar - digitalDollar, Seting.DECIMAL_PLACES));
                    senderAddress.setDigitalStakingBalance(UtilsUse.round(senderDigitalStaking + digitalDollar, Seting.DECIMAL_PLACES));
                } else {
                    senderAddress.setDigitalDollarBalance(senderDigitalDollar - digitalDollar);
                    senderAddress.setDigitalStakingBalance(senderDigitalStaking + digitalDollar);
                }

                if (indexBlock > Seting.NEW_ALGO_MINING) {
                    updateMinerBalance(minerAddress, senderAddress, recipientAddress, minerRewards);
                }

            } else if (voteEnum.equals(VoteEnum.UNSTAKING)) {
                System.out.println("UNSTAKING");
                if (senderDigitalStaking < digitalDollar) {
                    System.out.println("less staking");
                    sendTrue = false;
                    return sendTrue;
                }

                if (BasisController.getBlockchainSize() > Seting.START_BLOCK_DECIMAL_PLACES) {
                    senderAddress.setDigitalDollarBalance(UtilsUse.round(senderDigitalDollar + digitalDollar, Seting.DECIMAL_PLACES));
                    senderAddress.setDigitalStakingBalance(UtilsUse.round(senderDigitalStaking - digitalDollar, Seting.DECIMAL_PLACES));
                } else {
                    senderAddress.setDigitalDollarBalance(senderDigitalDollar + digitalDollar);
                    senderAddress.setDigitalStakingBalance(senderDigitalStaking - digitalDollar);
                }
                if (indexBlock > Seting.NEW_ALGO_MINING) {
                    updateMinerBalance(minerAddress, senderAddress, recipientAddress, minerRewards);
                }

            }

        } else if (senderAddress.getAccount().equals(Seting.BASIS_ADDRESS)) {
            if (BasisController.getBlockchainSize() > Seting.START_BLOCK_DECIMAL_PLACES) {
                recipientAddress.setDigitalDollarBalance(UtilsUse.round(recipientDigitalDollar + digitalDollar, Seting.DECIMAL_PLACES));
                recipientAddress.setDigitalStockBalance(UtilsUse.round(recipientDigitalStock + digitalStock, Seting.DECIMAL_PLACES));
            } else {
                recipientAddress.setDigitalDollarBalance(recipientDigitalDollar + digitalDollar);
                recipientAddress.setDigitalStockBalance(recipientDigitalStock + digitalStock);
            }
        }
        return sendTrue;
    }

    public static boolean rollBackSendMoney(
            Account senderAddress,
            Account recipientAddress,
            Account minerAddress,
            double digitalDollar, double digitalStock, double minerRewards, VoteEnum voteEnum, long indexBlock) {
        double senderDigitalDollar = senderAddress.getDigitalDollarBalance();
        double senderDigitalStock = senderAddress.getDigitalStockBalance();
        double senderDigitalStaking = senderAddress.getDigitalStakingBalance();
        double recipientDigitalDollar = recipientAddress.getDigitalDollarBalance();
        double recipientDigitalStock = recipientAddress.getDigitalStockBalance();

        boolean sendTrue = true;
        if (!senderAddress.getAccount().equals(Seting.BASIS_ADDRESS)) {
            if ((voteEnum.equals(VoteEnum.YES) || voteEnum.equals(VoteEnum.NO))) {
                if (BasisController.getBlockchainSize() > Seting.START_BLOCK_DECIMAL_PLACES) {
                    senderAddress.setDigitalDollarBalance(UtilsUse.round(senderDigitalDollar + digitalDollar, Seting.DECIMAL_PLACES));
                    senderAddress.setDigitalStockBalance(UtilsUse.round(senderDigitalStock + digitalStock, Seting.DECIMAL_PLACES));
                    recipientAddress.setDigitalDollarBalance(UtilsUse.round(recipientDigitalDollar - digitalDollar, Seting.DECIMAL_PLACES));
                } else {
                    senderAddress.setDigitalDollarBalance(senderDigitalDollar + digitalDollar);
                    senderAddress.setDigitalStockBalance(senderDigitalStock + digitalStock);
                    recipientAddress.setDigitalDollarBalance(recipientDigitalDollar - digitalDollar);
                }
                if (voteEnum.equals(VoteEnum.YES)) {
                    if (BasisController.getBlockchainSize() > Seting.START_BLOCK_DECIMAL_PLACES) {
                        recipientAddress.setDigitalStockBalance(UtilsUse.round(recipientDigitalStock - digitalStock, Seting.DECIMAL_PLACES));
                    } else {
                        recipientAddress.setDigitalStockBalance(recipientDigitalStock - digitalStock);
                    }
                } else if (voteEnum.equals(VoteEnum.NO)) {
                    if (BasisController.getBlockchainSize() > Seting.START_BLOCK_DECIMAL_PLACES) {
                        recipientAddress.setDigitalStockBalance(UtilsUse.round(recipientDigitalStock + digitalStock, Seting.DECIMAL_PLACES));
                    } else {
                        recipientAddress.setDigitalStockBalance(recipientDigitalStock + digitalStock);
                    }
                }
                if (indexBlock > Seting.NEW_ALGO_MINING) {
                    rolBackupdateMinerBalance(minerAddress, senderAddress, recipientAddress, minerRewards);
                }

            } else if (voteEnum.equals(VoteEnum.STAKING)) {
                if (BasisController.getBlockchainSize() > Seting.START_BLOCK_DECIMAL_PLACES) {
                    senderAddress.setDigitalDollarBalance(UtilsUse.round(senderDigitalDollar + digitalDollar, Seting.DECIMAL_PLACES));
                    senderAddress.setDigitalStakingBalance(UtilsUse.round(senderDigitalStaking - digitalDollar, Seting.DECIMAL_PLACES));
                } else {
                    senderAddress.setDigitalDollarBalance(senderDigitalDollar + digitalDollar);
                    senderAddress.setDigitalStakingBalance(senderDigitalStaking - digitalDollar);
                }
                if (indexBlock > Seting.NEW_ALGO_MINING) {
                    rolBackupdateMinerBalance(minerAddress, senderAddress, recipientAddress, minerRewards);
                }

            } else if (voteEnum.equals(VoteEnum.UNSTAKING)) {
                if (BasisController.getBlockchainSize() > Seting.START_BLOCK_DECIMAL_PLACES) {
                    senderAddress.setDigitalDollarBalance(UtilsUse.round(senderDigitalDollar - digitalDollar, Seting.DECIMAL_PLACES));
                    senderAddress.setDigitalStakingBalance(UtilsUse.round(senderDigitalStaking + digitalDollar, Seting.DECIMAL_PLACES));
                } else {
                    senderAddress.setDigitalDollarBalance(senderDigitalDollar - digitalDollar);
                    senderAddress.setDigitalStakingBalance(senderDigitalStaking + digitalDollar);
                }
                if (indexBlock > Seting.NEW_ALGO_MINING) {
                    rolBackupdateMinerBalance(minerAddress, senderAddress, recipientAddress, minerRewards);
                }
            }

        } else if (senderAddress.getAccount().equals(Seting.BASIS_ADDRESS)) {
            if (BasisController.getBlockchainSize() > Seting.START_BLOCK_DECIMAL_PLACES) {
                recipientAddress.setDigitalDollarBalance(UtilsUse.round(recipientDigitalDollar - digitalDollar, Seting.DECIMAL_PLACES));
                recipientAddress.setDigitalStockBalance(UtilsUse.round(recipientDigitalStock - digitalStock, Seting.DECIMAL_PLACES));
            } else {
                recipientAddress.setDigitalDollarBalance(recipientDigitalDollar - digitalDollar);
                recipientAddress.setDigitalStockBalance(recipientDigitalStock - digitalStock);
            }
        }
        return sendTrue;
    }

    private static void updateMinerBalance(Account minerAddress, Account senderAddress, Account recipientAddress, double minerRewards) {
        double minerDigitalDollar = minerAddress.getDigitalDollarBalance();
        if (BasisController.getBlockchainSize() > Seting.START_BLOCK_DECIMAL_PLACES) {
            minerDigitalDollar = UtilsUse.round(minerDigitalDollar, Seting.DECIMAL_PLACES);
            minerRewards = UtilsUse.round(minerRewards, Seting.DECIMAL_PLACES);
        }

        if (senderAddress.getAccount().equals(minerAddress.getAccount())) {
            double updatedBalance = UtilsUse.round(senderAddress.getDigitalDollarBalance() + minerRewards, Seting.DECIMAL_PLACES);
            senderAddress.setDigitalDollarBalance(updatedBalance);

            minerAddress.setDigitalDollarBalance(senderAddress.getDigitalDollarBalance());
            minerAddress.setDigitalStockBalance(senderAddress.getDigitalStockBalance());
            minerAddress.setDigitalStakingBalance(senderAddress.getDigitalStakingBalance());
        } else if (recipientAddress.getAccount().equals(minerAddress.getAccount())) {
            double updatedBalance = UtilsUse.round(recipientAddress.getDigitalDollarBalance() + minerRewards, Seting.DECIMAL_PLACES);
            recipientAddress.setDigitalDollarBalance(updatedBalance);

            minerAddress.setDigitalDollarBalance(recipientAddress.getDigitalDollarBalance());
            minerAddress.setDigitalStockBalance(recipientAddress.getDigitalStockBalance());
            minerAddress.setDigitalStakingBalance(recipientAddress.getDigitalStakingBalance());
        } else {
            double updatedBalance = UtilsUse.round(minerDigitalDollar + minerRewards, Seting.DECIMAL_PLACES);
            minerAddress.setDigitalDollarBalance(updatedBalance);
        }
    }
    private static void rolBackupdateMinerBalance(Account minerAddress, Account senderAddress, Account recipientAddress, double minerRewards) {
        double minerDigitalDollar = minerAddress.getDigitalDollarBalance();
        if (BasisController.getBlockchainSize() > Seting.START_BLOCK_DECIMAL_PLACES) {
            minerDigitalDollar = UtilsUse.round(minerDigitalDollar, Seting.DECIMAL_PLACES);
            minerRewards = UtilsUse.round(minerRewards, Seting.DECIMAL_PLACES);
        }

        if (senderAddress.getAccount().equals(minerAddress.getAccount())) {
            double updatedBalance = UtilsUse.round(senderAddress.getDigitalDollarBalance() - minerRewards, Seting.DECIMAL_PLACES);
            senderAddress.setDigitalDollarBalance(updatedBalance);

            minerAddress.setDigitalDollarBalance(senderAddress.getDigitalDollarBalance());
            minerAddress.setDigitalStockBalance(senderAddress.getDigitalStockBalance());
            minerAddress.setDigitalStakingBalance(senderAddress.getDigitalStakingBalance());
        } else if (recipientAddress.getAccount().equals(minerAddress.getAccount())) {
            double updatedBalance = UtilsUse.round(recipientAddress.getDigitalDollarBalance() - minerRewards, Seting.DECIMAL_PLACES);
            recipientAddress.setDigitalDollarBalance(updatedBalance);

            minerAddress.setDigitalDollarBalance(recipientAddress.getDigitalDollarBalance());
            minerAddress.setDigitalStockBalance(recipientAddress.getDigitalStockBalance());
            minerAddress.setDigitalStakingBalance(recipientAddress.getDigitalStakingBalance());
        } else {
            double updatedBalance = UtilsUse.round(minerDigitalDollar - minerRewards, Seting.DECIMAL_PLACES);
            minerAddress.setDigitalDollarBalance(updatedBalance);
        }
    }
}
