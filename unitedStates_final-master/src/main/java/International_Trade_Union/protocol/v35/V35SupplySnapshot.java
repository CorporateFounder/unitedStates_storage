package International_Trade_Union.protocol.v35;

import java.math.BigDecimal;

/**
 * Immutable V35 supply view. This is a reporting value object only; it does not
 * mutate balances or validate blocks.
 */
public final class V35SupplySnapshot {
    private final BigDecimal totalMintedGold;
    private final BigDecimal burnedGold;
    private final BigDecimal circulatingGold;
    private final BigDecimal founderReserveGold;
    private final BigDecimal jackpotPaidGold;
    private final BigDecimal annualEmissionGold;
    private final BigDecimal inflationBeforeBurnPercent;
    private final BigDecimal inflationAfterBurnPercent;
    private final boolean burnAddressConfigured;
    private final boolean valid;
    private final String invalidReason;

    public V35SupplySnapshot(BigDecimal totalMintedGold,
                             BigDecimal burnedGold,
                             BigDecimal circulatingGold,
                             BigDecimal founderReserveGold,
                             BigDecimal jackpotPaidGold,
                             BigDecimal annualEmissionGold,
                             BigDecimal inflationBeforeBurnPercent,
                             BigDecimal inflationAfterBurnPercent,
                             boolean burnAddressConfigured,
                             boolean valid,
                             String invalidReason) {
        this.totalMintedGold = totalMintedGold;
        this.burnedGold = burnedGold;
        this.circulatingGold = circulatingGold;
        this.founderReserveGold = founderReserveGold;
        this.jackpotPaidGold = jackpotPaidGold;
        this.annualEmissionGold = annualEmissionGold;
        this.inflationBeforeBurnPercent = inflationBeforeBurnPercent;
        this.inflationAfterBurnPercent = inflationAfterBurnPercent;
        this.burnAddressConfigured = burnAddressConfigured;
        this.valid = valid;
        this.invalidReason = invalidReason;
    }

    public BigDecimal getTotalMintedGold() {
        return totalMintedGold;
    }

    public BigDecimal getBurnedGold() {
        return burnedGold;
    }

    public BigDecimal getCirculatingGold() {
        return circulatingGold;
    }

    public BigDecimal getFounderReserveGold() {
        return founderReserveGold;
    }

    public BigDecimal getJackpotPaidGold() {
        return jackpotPaidGold;
    }

    public BigDecimal getAnnualEmissionGold() {
        return annualEmissionGold;
    }

    public BigDecimal getInflationBeforeBurnPercent() {
        return inflationBeforeBurnPercent;
    }

    public BigDecimal getInflationAfterBurnPercent() {
        return inflationAfterBurnPercent;
    }

    public boolean isBurnAddressConfigured() {
        return burnAddressConfigured;
    }

    public boolean isValid() {
        return valid;
    }

    public String getInvalidReason() {
        return invalidReason;
    }
}
