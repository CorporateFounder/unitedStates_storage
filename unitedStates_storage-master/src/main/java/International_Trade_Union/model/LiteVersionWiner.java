package International_Trade_Union.model;

import lombok.Data;

@Data
public class LiteVersionWiner {
    private long index;
    private String address;
    private String minerAddress;
    private String hash;
    private String hashBlock;
    private String previousHash;
    private String timestamp;
    private int countTransactions;
    private double staking;
    private int bigRandom;
    private long diff;
    private long hashCompexity;

    public LiteVersionWiner() {
    }

    public LiteVersionWiner(long index, String address, String hash, int countTransactions, double staking, int bigRandom, long diff) {
        this.index = index;
        this.address = address;
        this.minerAddress = address;
        this.hash = hash;
        this.hashBlock = hash;
        this.countTransactions = countTransactions;
        this.staking = staking;
        this.bigRandom = bigRandom;
        this.diff = diff;
        this.hashCompexity = diff;
    }
}
