package International_Trade_Union.protocol.v35;

import International_Trade_Union.entity.DtoTransaction.DtoTransaction;
import International_Trade_Union.model.Account;
import International_Trade_Union.setings.Seting;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Map;

/**
 * Pure V35 supply accounting helpers. These helpers do not read databases,
 * call controllers, mutate balances, or change validation behavior.
 */
public final class V35SupplyCalculator {
    private static final int GOLD_SCALE = 2;
    private static final int PERCENT_SCALE = 4;
    private static final BigDecimal ZERO_GOLD = BigDecimal.ZERO.setScale(GOLD_SCALE);
    private static final BigDecimal ONE_HUNDRED = new BigDecimal("100");

    private V35SupplyCalculator() {
    }

    public static BigDecimal annualEmissionGold() {
        return blocksPerYear()
                .multiply(gold(Seting.V35_GOLD_REWARD))
                .setScale(GOLD_SCALE, RoundingMode.UNNECESSARY);
    }

    public static BigDecimal annualMinerEmissionGold() {
        return blocksPerYear()
                .multiply(gold(Seting.V35_INSTANT_MINER_REWARD))
                .setScale(GOLD_SCALE, RoundingMode.UNNECESSARY);
    }

    public static BigDecimal annualJackpotEmissionGold() {
        return blocksPerYear()
                .multiply(gold(Seting.V35_JACKPOT_REWARD))
                .setScale(GOLD_SCALE, RoundingMode.UNNECESSARY);
    }

    public static BigDecimal circulatingGold(BigDecimal totalMintedGold, BigDecimal burnedGold) {
        BigDecimal total = nonNegative(totalMintedGold);
        BigDecimal burned = nonNegative(burnedGold);
        if (burned.compareTo(total) > 0) {
            return ZERO_GOLD;
        }
        return total.subtract(burned).setScale(GOLD_SCALE, RoundingMode.UNNECESSARY);
    }

    public static BigDecimal inflationPercent(BigDecimal annualEmissionGold, BigDecimal supplyGold) {
        BigDecimal annual = nonNegative(annualEmissionGold);
        BigDecimal supply = nonNegative(supplyGold);
        if (supply.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO.setScale(PERCENT_SCALE);
        }
        return annual.multiply(ONE_HUNDRED).divide(supply, PERCENT_SCALE, RoundingMode.HALF_UP);
    }

    public static V35SupplySnapshot snapshot(BigDecimal totalMintedGold,
                                             BigDecimal burnedGold,
                                             BigDecimal founderReserveGold,
                                             BigDecimal jackpotPaidGold) {
        return snapshot(totalMintedGold, burnedGold, founderReserveGold, jackpotPaidGold, true);
    }

    public static V35SupplySnapshot snapshot(BigDecimal totalMintedGold,
                                             BigDecimal burnedGold,
                                             BigDecimal founderReserveGold,
                                             BigDecimal jackpotPaidGold,
                                             boolean burnAddressConfigured) {
        boolean inputsValid = isNonNegative(totalMintedGold)
                && isNonNegative(burnedGold)
                && isNonNegative(founderReserveGold)
                && isNonNegative(jackpotPaidGold);

        BigDecimal total = nonNegative(totalMintedGold);
        BigDecimal burned = nonNegative(burnedGold);
        BigDecimal founder = nonNegative(founderReserveGold);
        BigDecimal jackpot = nonNegative(jackpotPaidGold);
        boolean burnedExceedsMinted = burned.compareTo(total) > 0;
        boolean valid = inputsValid && !burnedExceedsMinted;
        String reason = valid ? "" : invalidReason(inputsValid, burnedExceedsMinted);
        BigDecimal circulating = valid ? total.subtract(burned).setScale(GOLD_SCALE, RoundingMode.UNNECESSARY) : ZERO_GOLD;
        BigDecimal annual = annualEmissionGold();

        return new V35SupplySnapshot(
                total,
                burned,
                circulating,
                founder,
                jackpot,
                annual,
                inflationPercent(annual, total),
                inflationPercent(annual, circulating),
                burnAddressConfigured,
                valid,
                reason
        );
    }

    public static V35SupplySnapshot snapshotFromBalances(BigDecimal totalMintedGold,
                                                         Map<String, Account> balances,
                                                         BigDecimal jackpotPaidGold) {
        return snapshotFromBalances(totalMintedGold, balances, jackpotPaidGold,
                Seting.V35_BURN_ADDRESS, Seting.ADDRESS_FOUNDER);
    }

    public static V35SupplySnapshot snapshotFromBalances(BigDecimal totalMintedGold,
                                                         Map<String, Account> balances,
                                                         BigDecimal jackpotPaidGold,
                                                         String burnAddress,
                                                         String founderAddress) {
        boolean burnConfigured = !BurnAddressPolicy.isPlaceholderBurnAddress(burnAddress);
        BigDecimal burned = burnConfigured ? accountGoldBalance(balances, burnAddress) : ZERO_GOLD;
        BigDecimal founder = accountGoldBalance(balances, founderAddress);
        return snapshot(totalMintedGold, burned, founder, jackpotPaidGold, burnConfigured);
    }

    public static BigDecimal totalMintedFromTransactions(List<DtoTransaction> transactions) {
        BigDecimal total = ZERO_GOLD;
        if (transactions == null) {
            return total;
        }
        for (DtoTransaction transaction : transactions) {
            if (transaction != null && Seting.BASIS_ADDRESS.equals(transaction.getSender())) {
                total = total.add(gold(transaction.getDigitalDollar()));
            }
        }
        return total.setScale(GOLD_SCALE, RoundingMode.UNNECESSARY);
    }

    public static BigDecimal jackpotPaidFromTransactions(List<DtoTransaction> transactions) {
        BigDecimal total = ZERO_GOLD;
        if (transactions == null) {
            return total;
        }
        for (DtoTransaction transaction : transactions) {
            if (transaction != null
                    && Seting.BASIS_ADDRESS.equals(transaction.getSender())
                    && JackpotCalculator.isCanonicalPayoutAmount(transaction.getDigitalDollar())) {
                total = total.add(gold(transaction.getDigitalDollar()));
            }
        }
        return total.setScale(GOLD_SCALE, RoundingMode.UNNECESSARY);
    }

    public static BigDecimal accountGoldBalance(Map<String, Account> balances, String address) {
        if (balances == null || address == null) {
            return ZERO_GOLD;
        }
        Account account = balances.get(address);
        if (account == null) {
            return ZERO_GOLD;
        }
        return nonNegative(account.getDigitalDollarBalance());
    }

    public static BigDecimal gold(double value) {
        if (!Double.isFinite(value) || value < 0.0) {
            return ZERO_GOLD;
        }
        return BigDecimal.valueOf(value).setScale(GOLD_SCALE, RoundingMode.HALF_UP);
    }

    public static BigDecimal gold(String value) {
        if (value == null) {
            return ZERO_GOLD;
        }
        try {
            return nonNegative(new BigDecimal(value));
        } catch (NumberFormatException e) {
            return ZERO_GOLD;
        }
    }

    private static BigDecimal blocksPerYear() {
        return BigDecimal.valueOf(JackpotCalculator.dailyWindowBlocks() * 365L);
    }

    private static boolean isNonNegative(BigDecimal value) {
        return value != null && value.compareTo(BigDecimal.ZERO) >= 0;
    }

    private static BigDecimal nonNegative(BigDecimal value) {
        if (!isNonNegative(value)) {
            return ZERO_GOLD;
        }
        return value.setScale(GOLD_SCALE, RoundingMode.HALF_UP);
    }

    private static String invalidReason(boolean inputsValid, boolean burnedExceedsMinted) {
        if (!inputsValid) {
            return "negative supply input";
        }
        if (burnedExceedsMinted) {
            return "burned supply exceeds total minted";
        }
        return "invalid supply input";
    }
}
