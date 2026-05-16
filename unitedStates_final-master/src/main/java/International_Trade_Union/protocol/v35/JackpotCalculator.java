package International_Trade_Union.protocol.v35;

import International_Trade_Union.setings.Seting;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Pure V35 jackpot helpers. This class does not create transactions, mutate blocks,
 * or wire jackpot payouts into validation.
 */
public final class JackpotCalculator {
    public static final String PERIOD_DAILY = "daily";
    public static final String PERIOD_WEEKLY = "weekly";
    public static final String PERIOD_MONTHLY = "monthly";
    public static final String PERIOD_QUARTERLY = "quarterly";
    public static final String PERIOD_HALF_YEAR = "half-year";
    public static final String PERIOD_YEARLY = "yearly";

    public static final long JACKPOT_RANDOM_DELAY_BLOCKS = 60L;
    private static final long SECONDS_PER_DAY = 86_400L;
    private static final long BLOCKS_PER_DAY = SECONDS_PER_DAY / Seting.V35_TARGET_BLOCK_TIME_SECONDS;
    private static final double DELTA = 0.00000001;

    private JackpotCalculator() {
    }

    public static double getDailyPerBlock() {
        return Seting.V35_DAILY_JACKPOT_PER_BLOCK;
    }

    public static double getWeeklyPerBlock() {
        return Seting.V35_WEEKLY_JACKPOT_PER_BLOCK;
    }

    public static double getMonthlyPerBlock() {
        return Seting.V35_MONTHLY_JACKPOT_PER_BLOCK;
    }

    public static double getQuarterlyPerBlock() {
        return Seting.V35_QUARTERLY_JACKPOT_PER_BLOCK;
    }

    public static double getHalfYearPerBlock() {
        return Seting.V35_HALF_YEAR_JACKPOT_PER_BLOCK;
    }

    public static double getYearlyPerBlock() {
        return Seting.V35_YEARLY_JACKPOT_PER_BLOCK;
    }

    public static double reservePerBlockForPeriod(String period) {
        if (period == null) {
            return 0.0;
        }
        switch (period.trim().toLowerCase()) {
            case PERIOD_DAILY:
                return getDailyPerBlock();
            case PERIOD_WEEKLY:
                return getWeeklyPerBlock();
            case PERIOD_MONTHLY:
                return getMonthlyPerBlock();
            case PERIOD_QUARTERLY:
                return getQuarterlyPerBlock();
            case PERIOD_HALF_YEAR:
                return getHalfYearPerBlock();
            case PERIOD_YEARLY:
                return getYearlyPerBlock();
            default:
                return 0.0;
        }
    }

    public static double getTotalPerBlock() {
        return totalReservePerBlock();
    }

    public static double totalReservePerBlock() {
        return getDailyPerBlock()
                + getWeeklyPerBlock()
                + getMonthlyPerBlock()
                + getQuarterlyPerBlock()
                + getHalfYearPerBlock()
                + getYearlyPerBlock();
    }

    public static boolean validateSplit() {
        return validateReserveSplit();
    }

    public static boolean validateReserveSplit() {
        return Math.abs(totalReservePerBlock() - Seting.V35_JACKPOT_REWARD) < DELTA;
    }

    public static long dailyWindowBlocks() {
        return BLOCKS_PER_DAY;
    }

    public static long weeklyWindowBlocks() {
        return dailyWindowBlocks() * 7L;
    }

    public static long monthlyWindowBlocks() {
        return dailyWindowBlocks() * 30L;
    }

    public static long quarterlyWindowBlocks() {
        return dailyWindowBlocks() * 90L;
    }

    public static long halfYearWindowBlocks() {
        return dailyWindowBlocks() * 182L;
    }

    public static long yearlyWindowBlocks() {
        return dailyWindowBlocks() * 365L;
    }

    public static long periodStart(long blockIndex, long windowSize) {
        if (blockIndex < 0 || windowSize <= 0) {
            return -1L;
        }
        return (blockIndex / windowSize) * windowSize;
    }

    public static long periodEnd(long blockIndex, long windowSize) {
        long start = periodStart(blockIndex, windowSize);
        if (start < 0) {
            return -1L;
        }
        return start + windowSize - 1L;
    }

    public static boolean isPeriodEnd(long blockIndex, long windowSize) {
        return blockIndex >= 0 && windowSize > 0 && blockIndex == periodEnd(blockIndex, windowSize);
    }

    public static long maturityBlock(long periodEnd, long delayBlocks) {
        if (periodEnd < 0 || delayBlocks < 0) {
            return -1L;
        }
        return periodEnd + delayBlocks;
    }

    public static long periodStartForBlock(long blockIndex, long windowSize) {
        return periodStart(blockIndex, windowSize);
    }

    public static long periodEndForBlock(long blockIndex, long windowSize) {
        return periodEnd(blockIndex, windowSize);
    }

    public static long maturityBlockForPeriod(long blockIndex, long windowSize) {
        long end = periodEndForBlock(blockIndex, windowSize);
        return maturityBlock(end, JACKPOT_RANDOM_DELAY_BLOCKS);
    }

    public static int selectWinnerOffset(String seedHex, int eligibleTicketCount) {
        if (eligibleTicketCount <= 0 || seedHex == null || seedHex.trim().isEmpty()) {
            return -1;
        }
        try {
            BigInteger seed = new BigInteger(normalizeHex(seedHex), 16);
            return seed.mod(BigInteger.valueOf(eligibleTicketCount)).intValue();
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    public static String combineHashesForSeed(List<String> futureHashes) {
        if (futureHashes == null || futureHashes.isEmpty()) {
            return "";
        }
        StringBuilder combined = new StringBuilder();
        for (String hash : futureHashes) {
            if (hash != null && !hash.trim().isEmpty()) {
                combined.append(hash.trim());
            }
        }
        if (combined.length() == 0) {
            return "";
        }
        return sha256Hex(combined.toString());
    }

    public static boolean isMature(long currentHeight, long periodEnd) {
        long maturity = maturityBlock(periodEnd, JACKPOT_RANDOM_DELAY_BLOCKS);
        return maturity >= 0 && currentHeight >= maturity;
    }

    public static String seedFromFutureHashes(List<String> futureHashes) {
        if (futureHashes == null || futureHashes.size() < JACKPOT_RANDOM_DELAY_BLOCKS) {
            return "";
        }
        List<String> delayedHashes = new ArrayList<>();
        for (int i = 0; i < JACKPOT_RANDOM_DELAY_BLOCKS; i++) {
            String hash = futureHashes.get(i);
            if (hash == null || hash.trim().isEmpty()) {
                return "";
            }
            delayedHashes.add(hash.trim());
        }
        return combineHashesForSeed(delayedHashes);
    }

    public static int baseTicketCountForBlock() {
        return 1;
    }

    public static boolean isEligibleTicketBlock(boolean isV35Block, String minerAddress) {
        return isV35Block && minerAddress != null && !minerAddress.trim().isEmpty();
    }

    public static boolean isEligibleTicket(long blockIndex, String minerAddress, boolean isV35Block) {
        return blockIndex >= 0 && isEligibleTicketBlock(isV35Block, minerAddress);
    }

    public static List<JackpotTicket> eligibleTickets(List<JackpotTicket> tickets) {
        if (tickets == null || tickets.isEmpty()) {
            return Collections.emptyList();
        }
        List<JackpotTicket> eligible = new ArrayList<>();
        for (JackpotTicket ticket : tickets) {
            if (ticket != null && ticket.isValid()) {
                eligible.add(ticket);
            }
        }
        eligible.sort(Comparator.comparingLong(JackpotTicket::getBlockIndex));
        return Collections.unmodifiableList(eligible);
    }

    public static JackpotWinnerProof selectWinner(long periodStart, long periodEnd,
                                                  List<JackpotTicket> tickets,
                                                  List<String> futureHashes) {
        long maturity = maturityBlock(periodEnd, JACKPOT_RANDOM_DELAY_BLOCKS);
        if (periodStart < 0 || periodEnd < periodStart || maturity < 0) {
            return JackpotWinnerProof.noWinner(periodStart, periodEnd, maturity, (int) JACKPOT_RANDOM_DELAY_BLOCKS, "");
        }

        List<JackpotTicket> eligible = eligibleTicketsForPeriod(tickets, periodStart, periodEnd);
        String seed = seedFromFutureHashes(futureHashes);
        int offset = selectWinnerOffset(seed, eligible.size());
        if (offset < 0) {
            return JackpotWinnerProof.noWinner(periodStart, periodEnd, maturity, (int) JACKPOT_RANDOM_DELAY_BLOCKS, seed);
        }

        JackpotTicket winner = eligible.get(offset);
        return new JackpotWinnerProof(
                periodStart,
                periodEnd,
                maturity,
                (int) JACKPOT_RANDOM_DELAY_BLOCKS,
                seed,
                offset,
                winner.getBlockIndex(),
                winner.getMinerAddress()
        );
    }

    public static JackpotWinnerProof selectWinner(List<JackpotTicket> tickets, List<String> futureHashes) {
        List<JackpotTicket> eligible = eligibleTickets(tickets);
        String seed = seedFromFutureHashes(futureHashes);
        int offset = selectWinnerOffset(seed, eligible.size());
        if (offset < 0) {
            return JackpotWinnerProof.noWinner(-1L, -1L, -1L, (int) JACKPOT_RANDOM_DELAY_BLOCKS, seed);
        }

        JackpotTicket winner = eligible.get(offset);
        return new JackpotWinnerProof(
                -1L,
                -1L,
                -1L,
                (int) JACKPOT_RANDOM_DELAY_BLOCKS,
                seed,
                offset,
                winner.getBlockIndex(),
                winner.getMinerAddress()
        );
    }

    private static List<JackpotTicket> eligibleTicketsForPeriod(List<JackpotTicket> tickets,
                                                                long periodStart,
                                                                long periodEnd) {
        List<JackpotTicket> eligible = eligibleTickets(tickets);
        if (eligible.isEmpty()) {
            return eligible;
        }
        List<JackpotTicket> inPeriod = new ArrayList<>();
        for (JackpotTicket ticket : eligible) {
            if (ticket.getBlockIndex() >= periodStart && ticket.getBlockIndex() <= periodEnd) {
                inPeriod.add(ticket);
            }
        }
        return Collections.unmodifiableList(inPeriod);
    }

    private static String normalizeHex(String seedHex) {
        String normalized = seedHex.trim();
        if (normalized.startsWith("0x") || normalized.startsWith("0X")) {
            return normalized.substring(2);
        }
        return normalized;
    }

    private static String sha256Hex(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashed = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            StringBuilder hex = new StringBuilder(hashed.length * 2);
            for (byte b : hashed) {
                hex.append(String.format("%02x", b));
            }
            return hex.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 is required", e);
        }
    }
}
