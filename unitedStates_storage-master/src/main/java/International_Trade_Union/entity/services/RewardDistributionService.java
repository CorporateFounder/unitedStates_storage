package International_Trade_Union.entity.services;

import International_Trade_Union.controllers.BasisController;
import International_Trade_Union.entity.DtoTransaction.DtoTransaction;
import International_Trade_Union.entity.blockchain.block.Block;
import International_Trade_Union.entity.entities.PoolBlock;
import International_Trade_Union.logger.MyLogger;
import International_Trade_Union.network.AllTransactions;
import International_Trade_Union.setings.Seting;
import International_Trade_Union.setings.SetingPool;
import International_Trade_Union.vote.Laws;
import International_Trade_Union.vote.VoteEnum;
import International_Trade_Union.utils.UtilsFileSaveRead;
import International_Trade_Union.utils.UtilsJson;
import International_Trade_Union.utils.base.Base58;
import International_Trade_Union.utils.UtilsSecurity;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.security.PrivateKey;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Data
public class RewardDistributionService {

    @Autowired
    private AllTransactions allTransactions;
    @Autowired
    private BlockService blockService;
    @Autowired
    private PoolBlockService poolBlockService;

    // Confirmed balances for miners
    private final Map<String, BigDecimal> confirmedBalances = new HashMap<>();
    // Pending transactions: key – Base58 signature, value – the transaction object
    private final Map<String, DtoTransaction> pendingTxMap = new HashMap<>();

    // Threshold for reward sending (10 coins)
    private BigDecimal threshold = BigDecimal.valueOf(10);
    private final ObjectMapper mapper = new ObjectMapper();

    // Settings object loaded from file (setingPool.txt)
    private SetingPool setingPool;

    public RewardDistributionService() {
        try {
            setingPool = UtilsFileSaveRead.loadJson(SetingPool.SETING_FILE, SetingPool.class);
            if (setingPool == null) {
                MyLogger.saveLog("setingPool not found, creating new instance.");
                setingPool = new SetingPool();
                String json = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(setingPool);
                UtilsFileSaveRead.save(json, SetingPool.SETING_FILE, false);
            }
        } catch (Exception e) {
            MyLogger.saveLog("Error loading setingPool: " + e.getMessage());
            e.printStackTrace();
            setingPool = new SetingPool();
        }
    }

    public void sendReward() throws Exception {
        MyLogger.saveLog("Starting sendReward process.");
        Base58 base = new Base58();
        String pubKeyPool = setingPool.getPublicKey();
        String privKeyPool = setingPool.getPrivateKey();
        Block prevBlock = BasisController.prevBlock();
        if (prevBlock == null) {
            MyLogger.saveLog("PrevBlock is null. Aborting sendReward.");
            return;
        }
        MyLogger.saveLog("PrevBlock miner: " + prevBlock.getMinerAddress());

        // Check if pool won the block
        if (!prevBlock.getMinerAddress().equals(pubKeyPool)) {
            MyLogger.saveLog("Pool did not win the block. PrevBlock miner: " + prevBlock.getMinerAddress()
                    + ", Pool pubKey: " + pubKeyPool);
            return;
        }
        MyLogger.saveLog("Pool won the block.");

        // Retrieve pool reward transactions
        var poolTxs = prevBlock.getDtoTransactions().stream()
                .filter(t -> t.getSender().equals(Seting.BASIS_ADDRESS) &&
                        t.getCustomer().equals(pubKeyPool))
                .collect(Collectors.toList());
        if (poolTxs.isEmpty()) {
            MyLogger.saveLog("No reward transactions found in block for pool.");
            return;
        }
        MyLogger.saveLog("Found " + poolTxs.size() + " reward transaction(s) for pool.");

        BigDecimal rewardPool = BigDecimal.valueOf(poolTxs.get(0).getDigitalDollar());
        BigDecimal poolCommission = rewardPool.multiply(BigDecimal.valueOf(setingPool.getPoolCommission()));
        BigDecimal distributableReward = rewardPool.subtract(poolCommission);
        MyLogger.saveLog("RewardPool: " + rewardPool + ", Commission: " + poolCommission
                + ", DistributableReward: " + distributableReward);

        // Log poolBlockMap before computing weights
        Map<String, PoolBlock> poolBlockMap = poolBlockService.getPoolBlockMap();
        if (poolBlockMap == null) {
            MyLogger.saveLog("PoolBlockMap is null. Initializing empty map.");
            poolBlockMap = new HashMap<>();
        } else {
            MyLogger.saveLog("PoolBlockMap size before computeMinerWeights: " + poolBlockMap.size());
            MyLogger.saveLog("PoolBlockMap content: " + poolBlockMap);
        }

        // Calculate miner weights
        Map<String, BigDecimal> minerWeights = computeMinerWeights();
        if (minerWeights == null || minerWeights.isEmpty()) {
            MyLogger.saveLog("Miner weights empty; no miners participated.");
            return;
        }
        BigDecimal totalWeight = minerWeights.values().stream().reduce(BigDecimal.ZERO, BigDecimal::add);
        if (totalWeight.compareTo(BigDecimal.ZERO) == 0) {
            MyLogger.saveLog("Total weight is zero; aborting reward distribution.");
            return;
        }

        // Distribute reward proportionally into confirmedBalances
        minerWeights.forEach((miner, weight) -> {
            BigDecimal share = weight.multiply(distributableReward)
                    .divide(totalWeight, 8, RoundingMode.HALF_UP);
            confirmedBalances.merge(miner, share, BigDecimal::add);
            MyLogger.saveLog("Miner " + miner + " receives share: " + share);
        });

        // Process each miner from confirmedBalances: send aggregated transaction if amount exceeds threshold
        for (String miner : new ArrayList<>(confirmedBalances.keySet())) {
            BigDecimal confirmed = confirmedBalances.get(miner);
            BigDecimal pending = getPendingAmount(miner);
            BigDecimal available = confirmed.subtract(pending);
            BigDecimal sendAmount = available.setScale(2, RoundingMode.HALF_UP);
            MyLogger.saveLog("Miner " + miner + " - Confirmed: " + confirmed + ", Pending: " + pending
                    + ", Available: " + available + ", SendAmount: " + sendAmount);
            if (sendAmount.compareTo(BigDecimal.valueOf(0.01)) < 0 || sendAmount.compareTo(threshold) < 0) {
                MyLogger.saveLog("Miner " + miner + " amount below threshold. Skipping.");
                continue;
            }
            try {
                createAndAddTransaction(pubKeyPool, privKeyPool, miner, sendAmount);
                confirmedBalances.put(miner, confirmed.subtract(sendAmount));
                MyLogger.saveLog("Transaction created for miner " + miner + " with amount: " + sendAmount);
            } catch (Exception e) {
                MyLogger.saveLog("Error creating transaction for miner " + miner + ": " + e.getMessage());
                e.printStackTrace();
            }
        }

        // Create staking transaction for pool commission
        try {
            createAndAddStakingTransaction(pubKeyPool, privKeyPool, poolCommission);
            MyLogger.saveLog("Staking transaction created for pool commission: " + poolCommission);
        } catch (Exception e) {
            MyLogger.saveLog("Error creating staking transaction: " + e.getMessage());
            e.printStackTrace();
        }

        persistState();

        // Log poolBlockMap before clearing
        Map<String, PoolBlock> pbMapBeforeClear = poolBlockService.getPoolBlockMap();
        if (pbMapBeforeClear == null) {
            MyLogger.saveLog("PoolBlockMap is null before clear.");
        } else {
            MyLogger.saveLog("PoolBlockMap size before clear: " + pbMapBeforeClear.size());
        }
        poolBlockService.clearPoolBlocks();
        Map<String, PoolBlock> pbMapAfterClear = poolBlockService.getPoolBlockMap();
        if (pbMapAfterClear == null) {
            MyLogger.saveLog("PoolBlockMap is null after clear.");
        } else {
            MyLogger.saveLog("PoolBlockMap cleared. New size: " + pbMapAfterClear.size());
        }

        MyLogger.saveLog("sendReward process completed.");
    }

    // Calculate miner weights based on pool blocks
    private Map<String, BigDecimal> computeMinerWeights() {
        Map<String, PoolBlock> poolBlockMap = poolBlockService.getPoolBlockMap();
        if (poolBlockMap == null) {
            MyLogger.saveLog("poolBlockMap is null in computeMinerWeights().");
            return new HashMap<>();
        }
        Map<String, BigDecimal> minerWeights = new HashMap<>();
        for (PoolBlock pb : poolBlockMap.values()) {
            if (pb == null || pb.getBlock() == null) {
                MyLogger.saveLog("Encountered null PoolBlock or Block while computing weights.");
                continue;
            }
            String miner = pb.getMiner();
            int difficulty = (int) pb.getBlock().getHashCompexity();
            BigDecimal weight = BigDecimal.valueOf(2).pow(difficulty - 17);
            minerWeights.merge(miner, weight, BigDecimal::add);
            MyLogger.saveLog("Calculated weight for miner " + miner + ": " + weight);
        }
        MyLogger.saveLog("Total minerWeights map: " + minerWeights);
        return minerWeights;
    }

    // Get total pending amount for a given miner
    private BigDecimal getPendingAmount(String miner) {
        BigDecimal pending = pendingTxMap.values().stream()
                .filter(tx -> tx != null && tx.getCustomer() != null && tx.getCustomer().equals(miner))
                .map(tx -> BigDecimal.valueOf(tx.getDigitalDollar()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        MyLogger.saveLog("Pending amount for miner " + miner + ": " + pending);
        return pending;
    }

    // Create and add reward transaction for miner
    private void createAndAddTransaction(String pubKeyPool, String privKeyPool, String miner, BigDecimal sendAmount) throws Exception {
        DtoTransaction dtoTransaction = new DtoTransaction(
                pubKeyPool,
                miner,
                sendAmount.doubleValue(),
                sendAmount.doubleValue(),
                new Laws(),
                0,
                VoteEnum.YES
        );
        Base58 base = new Base58();
        PrivateKey privateKey = UtilsSecurity.privateBytToPrivateKey(base.decode(privKeyPool));
        byte[] sign = UtilsSecurity.sign(privateKey, dtoTransaction.toSign());
        if (isTransactionAlreadyProcessed(sign)) {
            MyLogger.saveLog("Transaction already processed for miner " + miner);
            return;
        }
        dtoTransaction.setSign(sign);
        allTransactions.addTransaction(dtoTransaction);
        String signKey = new Base58().encode(sign);
        pendingTxMap.put(signKey, dtoTransaction);
        MyLogger.saveLog("Reward transaction added to pending with key: " + signKey);
    }

    // Create and add staking transaction for pool commission
    private void createAndAddStakingTransaction(String pubKeyPool, String privKeyPool, BigDecimal poolCommission) throws Exception {
        DtoTransaction stakingTx = new DtoTransaction(
                pubKeyPool,
                pubKeyPool,
                poolCommission.setScale(2, RoundingMode.HALF_UP).doubleValue(),
                poolCommission.setScale(2, RoundingMode.HALF_UP).doubleValue(),
                new Laws(),
                0,
                VoteEnum.STAKING
        );
        Base58 base = new Base58();
        PrivateKey privateKey = UtilsSecurity.privateBytToPrivateKey(base.decode(privKeyPool));
        byte[] sign = UtilsSecurity.sign(privateKey, stakingTx.toSign());
        stakingTx.setSign(sign);
        allTransactions.addTransaction(stakingTx);
        MyLogger.saveLog("Staking transaction created with key: " + new Base58().encode(sign));
    }

    // Check if transaction is already processed (exists in blockchain or pending)
    private boolean isTransactionAlreadyProcessed(byte[] sign) {
        String signBase58 = new Base58().encode(sign);
        boolean exists = blockService.existsBySign(sign) || pendingTxMap.containsKey(signBase58);
        MyLogger.saveLog("Checking transaction " + signBase58 + ": already processed = " + exists);
        return exists;
    }

    // Check pending transactions: remove from pending if confirmed in blockchain
    public void checkPending() throws Exception {
        MyLogger.saveLog("Starting checkPending.");
        for (String key : new ArrayList<>(pendingTxMap.keySet())) {
            DtoTransaction tx = pendingTxMap.get(key);
            if (tx == null) continue;
            if (blockService.existsBySign(tx.getSign())) {
                pendingTxMap.remove(key);
                MyLogger.saveLog("Removed confirmed transaction with key: " + key);
            }
        }
        persistState();
        MyLogger.saveLog("checkPending completed.");
    }

    // Persist confirmedBalances and poolBlockMap to file
    private void persistState() throws Exception {
        Map<String, Object> data = new HashMap<>();
        data.put("confirmedBalances", confirmedBalances);
        String jsonData = UtilsJson.objToStringJson(data);
        UtilsFileSaveRead.save(jsonData, SetingPool.ORIGINAL_BALANCE_FILE, false);
        MyLogger.saveLog("Persisted confirmedBalances: " + jsonData);
        Map<String, PoolBlock> poolBlockMap = poolBlockService.getPoolBlockMap();
        String jsonPoolBlockMap = UtilsJson.objToStringJson(poolBlockMap == null ? new HashMap<>() : poolBlockMap);
        UtilsFileSaveRead.save(jsonPoolBlockMap, SetingPool.POOL_BLOCK_MAP_FILE, false);
        MyLogger.saveLog("Persisted poolBlockMap: " + jsonPoolBlockMap);
    }

    // Load state from files and update confirmedBalances and poolBlockMap accordingly
    public void loadState() throws Exception {
        MyLogger.saveLog("Loading state from files.");
        String jsonData = UtilsFileSaveRead.read(SetingPool.ORIGINAL_BALANCE_FILE);
        if (jsonData != null && !jsonData.isEmpty()) {
            Map<String, Object> data = mapper.readValue(jsonData, new TypeReference<Map<String, Object>>() {});
            if (data != null) {
                Map<String, BigDecimal> loadedConfirmed = mapper.convertValue(
                        data.get("confirmedBalances"), new TypeReference<Map<String, BigDecimal>>() {});
                if (loadedConfirmed != null) {
                    confirmedBalances.clear();
                    confirmedBalances.putAll(loadedConfirmed);
                    MyLogger.saveLog("Loaded confirmedBalances: " + loadedConfirmed);
                }
            }
        } else {
            MyLogger.saveLog("No confirmedBalances data found in file.");
        }
        String jsonPoolBlockMap = UtilsFileSaveRead.read(SetingPool.POOL_BLOCK_MAP_FILE);
        if (jsonPoolBlockMap != null && !jsonPoolBlockMap.isEmpty()) {
            Map<String, PoolBlock> loadedPoolBlockMap = mapper.readValue(jsonPoolBlockMap, new TypeReference<Map<String, PoolBlock>>() {});
            if (loadedPoolBlockMap != null) {
                poolBlockService.clearPoolBlocks();
                MyLogger.saveLog("Cleared current poolBlockMap.");
                for (PoolBlock pb : loadedPoolBlockMap.values()) {
                    try {
                        poolBlockService.addPoolBlock(pb);
                        MyLogger.saveLog("Added PoolBlock for miner: " + pb.getMiner());
                    } catch (Exception e) {
                        MyLogger.saveLog("Error adding PoolBlock: " + e.getMessage());
                        e.printStackTrace();
                    }
                }
            }
        } else {
            MyLogger.saveLog("No poolBlockMap data found in file.");
        }
        MyLogger.saveLog("State loading completed.");
    }
}
