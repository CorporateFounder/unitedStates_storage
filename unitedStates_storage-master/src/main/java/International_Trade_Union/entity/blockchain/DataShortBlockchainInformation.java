package International_Trade_Union.entity.blockchain;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class DataShortBlockchainInformation {
    public static final String DEFAULT_CUMULATIVE_WORK = "0";
    public static final String DEFAULT_TIP_HASH = "";

    private long size;
    private boolean isValidation;
    private long hashCount;
    private BigDecimal staking;
    private long transactions;
    private int bigRandomNumber;
    private String cumulativeWork = DEFAULT_CUMULATIVE_WORK;
    private String tipHash = DEFAULT_TIP_HASH;

    public DataShortBlockchainInformation() {
    }

    public DataShortBlockchainInformation(long size, boolean isValidation, long hashCount, BigDecimal staking, long transactions, int bigRandomNumber) {
        this(size, isValidation, hashCount, staking, transactions, bigRandomNumber, DEFAULT_CUMULATIVE_WORK, DEFAULT_TIP_HASH);
    }

    public DataShortBlockchainInformation(long size,
                                          boolean isValidation,
                                          long hashCount,
                                          BigDecimal staking,
                                          long transactions,
                                          int bigRandomNumber,
                                          String cumulativeWork,
                                          String tipHash) {
        this.size = size;
        this.isValidation = isValidation;
        this.hashCount = hashCount;
        this.staking = staking;
        this.transactions = transactions;
        this.bigRandomNumber = bigRandomNumber;
        this.cumulativeWork = cumulativeWork == null ? DEFAULT_CUMULATIVE_WORK : cumulativeWork;
        this.tipHash = tipHash == null ? DEFAULT_TIP_HASH : tipHash;

    }

    @Override
    public DataShortBlockchainInformation clone() throws CloneNotSupportedException {
        return new DataShortBlockchainInformation(size, isValidation, hashCount, staking, transactions, bigRandomNumber, cumulativeWork, tipHash);
    }
}
