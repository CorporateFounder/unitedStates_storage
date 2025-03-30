package International_Trade_Union.setings;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.math.BigDecimal;

@JsonIgnoreProperties(ignoreUnknown = true)
public class SetingPool {
    public static final String SETING_FILE = "/server/resources/privatePool/seting.txt";
    public static final String ORIGINAL_BALANCE_FILE = "/server/resources/balance/original_balance.txt";
   public static final String POOL_BLOCK_MAP_FILE = "/server/resources/pool/pool_block_map.txt"; // новое имя

    private String publicKey;
    private String privateKey;
    private double poolCommission; // Например, 0.1 означает 10%
    private BigDecimal pendingReward; // Сумма наград в ожидании
    private BigDecimal unsentReward;  // Сумма наград, которые ещё не отправлены

    public SetingPool() {
        this.publicKey = "defaultPublicKey";
        this.privateKey = "defaultPrivateKey";
        this.poolCommission = 0.1;
        this.pendingReward = BigDecimal.ZERO;
        this.unsentReward = BigDecimal.ZERO;
    }

    public String getPublicKey() { return publicKey; }
    public void setPublicKey(String publicKey) { this.publicKey = publicKey; }
    public String getPrivateKey() { return privateKey; }
    public void setPrivateKey(String privateKey) { this.privateKey = privateKey; }
    public double getPoolCommission() { return poolCommission; }
    public void setPoolCommission(double poolCommission) { this.poolCommission = poolCommission; }
    public BigDecimal getPendingReward() { return pendingReward; }
    public void setPendingReward(BigDecimal pendingReward) { this.pendingReward = pendingReward; }
    public BigDecimal getUnsentReward() { return unsentReward; }
    public void setUnsentReward(BigDecimal unsentReward) { this.unsentReward = unsentReward; }
}
