package International_Trade_Union.protocol.v35;

import International_Trade_Union.entity.DtoTransaction.DtoTransaction;
import International_Trade_Union.entity.blockchain.block.Block;
import International_Trade_Union.setings.Seting;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Read-only response builder for V35 jackpot status endpoints.
 */
public final class V35JackpotApiResponseBuilder {
    private static final int GOLD_SCALE = 2;
    private static final long FIRST_PAYOUT_BLOCK_OFFSET = JackpotCalculator.JACKPOT_RANDOM_DELAY_BLOCKS + 1L;
    private static final String DISABLED_WARNING = "V35 jackpot policy is configured but not active yet.";

    private V35JackpotApiResponseBuilder() {
    }

    public static Map<String, Object> status(List<Block> canonicalChain, long currentHeight) {
        Map<String, Object> response = baseFields(currentHeight);
        response.put("jackpotReservePerBlockGold", gold(Seting.V35_JACKPOT_REWARD));
        response.put("warning", isV35Active(currentHeight) ? "" : DISABLED_WARNING);
        response.put("periods", periodSummaries(canonicalChain, currentHeight));
        return response;
    }

    public static Map<String, Object> periods(long currentHeight) {
        Map<String, Object> response = baseFields(currentHeight);
        response.put("exactSlotOnly", true);
        response.put("missedPayoutsPaidLater", false);
        response.put("explanation", "Each V35 mined block is one jackpot ticket. Winners use minerAddress only and delayed future block hashes.");
        response.put("periods", periodDefinitions());
        return response;
    }

    public static Map<String, Object> history(List<Block> canonicalChain, long currentHeight) {
        Map<String, Object> response = baseFields(currentHeight);
        response.put("status", isV35Active(currentHeight) ? "NO_HISTORY" : "DISABLED");
        response.put("warning", isV35Active(currentHeight) ? "" : DISABLED_WARNING);
        response.put("items", isV35Active(currentHeight) ? paidHistory(canonicalChain) : Collections.emptyList());
        return response;
    }

    public static Map<String, Object> tickets(List<Block> canonicalChain, long currentHeight, String address) {
        Map<String, Object> response = baseFields(currentHeight);
        response.put("address", safe(address));
        response.put("note", "Only minerAddress tickets count. Sender addresses are never jackpot participants.");
        response.put("periods", isV35Active(currentHeight)
                ? ticketSummaries(canonicalChain, currentHeight, address)
                : disabledTicketSummaries());
        response.put("warning", isV35Active(currentHeight) ? "" : DISABLED_WARNING);
        return response;
    }

    public static Map<String, Object> proof(List<Block> canonicalChain,
                                            long currentHeight,
                                            String periodTypeValue,
                                            long periodEnd) {
        Map<String, Object> response = baseFields(currentHeight);
        JackpotCalculator.JackpotPeriod periodType = parsePeriod(periodTypeValue);
        response.put("periodType", periodType.name());
        response.put("periodEnd", periodEnd);
        response.put("futureHashDelay", JackpotCalculator.JACKPOT_RANDOM_DELAY_BLOCKS);
        response.put("payoutExpectedGold", gold(JackpotCalculator.expectedPayoutAmount(periodType)));

        if (!isV35Active(currentHeight)) {
            response.put("proofStatus", "DISABLED");
            response.put("warning", DISABLED_WARNING);
            response.put("periodStart", null);
            response.put("maturityBlock", null);
            response.put("seedHash", "");
            response.put("selectedOffset", -1);
            response.put("selectedBlockIndex", -1L);
            response.put("winnerMinerAddress", "");
            response.put("payoutPaid", false);
            return response;
        }

        JackpotPeriodInstance instance = instanceFromPeriodEnd(periodType, periodEnd, Seting.V35_RESCUE_FORK_INDEX);
        response.put("periodStart", instance.getPeriodStart());
        response.put("maturityBlock", instance.getMaturityBlock());
        response.put("firstPayoutBlock", instance.getFirstPayoutBlock());

        if (!instance.isValid()) {
            response.put("proofStatus", "INVALID_CONTEXT");
            response.put("warning", "Requested period is not aligned to the V35 fork-relative jackpot window.");
            return response;
        }
        if (currentHeight < instance.getMaturityBlock()) {
            response.put("proofStatus", "NOT_MATURE");
            response.put("warning", "Jackpot proof is not mature yet.");
            return response;
        }

        List<JackpotTicket> tickets = V35JackpotChainContextAdapter.collectEligibleTickets(
                canonicalChain, instance, Seting.V35_RESCUE_FORK_INDEX);
        List<String> futureHashes = V35JackpotChainContextAdapter.collectFutureHashes(canonicalChain, instance);
        JackpotWinnerProof proof = JackpotCalculator.selectWinner(
                instance.getPeriodStart(), instance.getPeriodEnd(), tickets, futureHashes);
        JackpotProofResult paid = findPaidPayout(canonicalChain, instance);

        response.put("seedHash", proof.getSeedHex());
        response.put("selectedOffset", proof.getSelectedOffset());
        response.put("selectedBlockIndex", proof.getSelectedBlockIndex());
        response.put("winnerMinerAddress", proof.getWinnerMinerAddress());
        response.put("eligibleTicketCount", tickets.size());
        response.put("payoutPaid", paid.isValid());
        response.put("payoutBlockIndex", paid.isValid() ? instance.getFirstPayoutBlock() : null);
        response.put("payoutBlockHash", paid.isValid() ? blockHash(blockByIndex(canonicalChain, instance.getFirstPayoutBlock())) : "");
        response.put("payoutTxHash", "");
        response.put("proofStatus", proof.hasWinner() ? (paid.isValid() ? "PAID" : "MATURE") : "UNAVAILABLE");
        response.put("warning", proof.hasWinner() ? "" : "Jackpot proof could not be built from available canonical context.");
        return response;
    }

    private static Map<String, Object> baseFields(long currentHeight) {
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("v35Active", isV35Active(currentHeight));
        response.put("v35ForkIndex", forkIndexValue());
        response.put("blockTimeSeconds", Seting.V35_TARGET_BLOCK_TIME_SECONDS);
        response.put("blocksPerDay", JackpotCalculator.dailyWindowBlocks());
        response.put("randomDelayBlocks", JackpotCalculator.JACKPOT_RANDOM_DELAY_BLOCKS);
        response.put("firstPayoutBlockOffset", FIRST_PAYOUT_BLOCK_OFFSET);
        response.put("burnAddress", Seting.V35_BURN_ADDRESS);
        response.put("isBurnAddressPlaceholder", BurnAddressPolicy.isPlaceholderBurnAddress());
        response.put("burnAddressConfigured", BurnAddressPolicy.isBurnAddressConfigured());
        response.put("publicBurnAddressPublished", BurnAddressPolicy.isPublicBurnAddressPublished());
        response.put("burnAddressWarning", BurnAddressPolicy.publicBurnAddressWarning());
        response.put("winnerSource", "minerAddress");
        response.put("senderEligibilityAllowed", false);
        response.put("currentHeight", currentHeight);
        return response;
    }

    private static List<Map<String, Object>> periodDefinitions() {
        List<Map<String, Object>> definitions = new ArrayList<>();
        for (JackpotCalculator.JackpotPeriod period : periodsInOrder()) {
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("periodType", period.name());
            row.put("periodName", period.getPeriodName());
            row.put("windowBlocks", JackpotCalculator.windowBlocksForPeriod(period.getPeriodName()));
            row.put("canonicalPayoutGold", gold(JackpotCalculator.expectedPayoutAmount(period)));
            row.put("reservePerBlockGold", gold(JackpotCalculator.reservePerBlockForPeriod(period.getPeriodName())));
            row.put("randomDelayBlocks", JackpotCalculator.JACKPOT_RANDOM_DELAY_BLOCKS);
            row.put("explanation", "One eligible V35 mined block is one ticket; winner is selected from minerAddress values only.");
            definitions.add(row);
        }
        return Collections.unmodifiableList(definitions);
    }

    private static List<Map<String, Object>> periodSummaries(List<Block> canonicalChain, long currentHeight) {
        List<Map<String, Object>> summaries = new ArrayList<>();
        boolean active = isV35Active(currentHeight);
        for (JackpotCalculator.JackpotPeriod period : periodsInOrder()) {
            Map<String, Object> row = periodDefinition(period);
            if (!active) {
                row.put("currentPeriodStart", null);
                row.put("currentPeriodEnd", null);
                row.put("maturityBlock", null);
                row.put("mature", false);
                row.put("estimatedTickets", 0);
                row.put("status", "DISABLED");
                summaries.add(row);
                continue;
            }

            JackpotPeriodInstance instance = currentPeriodInstance(period, currentHeight);
            row.put("currentPeriodStart", instance.getPeriodStart());
            row.put("currentPeriodEnd", instance.getPeriodEnd());
            row.put("maturityBlock", instance.getMaturityBlock());
            row.put("firstPayoutBlock", instance.getFirstPayoutBlock());
            row.put("mature", currentHeight >= instance.getMaturityBlock());
            List<JackpotTicket> tickets = V35JackpotChainContextAdapter.collectEligibleTickets(
                    canonicalChain, instance, Seting.V35_RESCUE_FORK_INDEX);
            row.put("estimatedTickets", tickets.size());
            JackpotProofResult paid = findPaidPayout(canonicalChain, instance);
            row.put("status", paid.isValid() ? "PAID" : (currentHeight >= instance.getMaturityBlock() ? "MATURE" : "NOT_MATURE"));
            summaries.add(row);
        }
        return Collections.unmodifiableList(summaries);
    }

    private static Map<String, Object> periodDefinition(JackpotCalculator.JackpotPeriod period) {
        Map<String, Object> row = new LinkedHashMap<>();
        row.put("periodType", period.name());
        row.put("windowBlocks", JackpotCalculator.windowBlocksForPeriod(period.getPeriodName()));
        row.put("canonicalPayoutGold", gold(JackpotCalculator.expectedPayoutAmount(period)));
        return row;
    }

    private static List<Map<String, Object>> paidHistory(List<Block> canonicalChain) {
        if (canonicalChain == null || canonicalChain.isEmpty()) {
            return Collections.emptyList();
        }
        List<Map<String, Object>> history = new ArrayList<>();
        for (Block block : canonicalChain) {
            if (block == null || block.getIndex() < Seting.V35_RESCUE_FORK_INDEX || block.getDtoTransactions() == null) {
                continue;
            }
            for (DtoTransaction transaction : block.getDtoTransactions()) {
                JackpotPayoutCandidate candidate = JackpotPayoutCandidate.fromTransaction(block.getIndex(), transaction);
                if (!candidate.isBasisPayout() || candidate.getPeriodType() == JackpotCalculator.JackpotPeriod.UNKNOWN) {
                    continue;
                }
                JackpotPeriodInstance instance = V35JackpotProofService.periodInstanceForPayoutBlock(
                        candidate.getPeriodType(), block.getIndex(), Seting.V35_RESCUE_FORK_INDEX);
                JackpotProofResult result = verifyCandidate(canonicalChain, block, transaction, instance);
                if (result.isValid()) {
                    history.add(historyRow(block, transaction, result));
                }
            }
        }
        return Collections.unmodifiableList(history);
    }

    private static Map<String, Object> historyRow(Block block, DtoTransaction transaction, JackpotProofResult result) {
        JackpotPeriodInstance instance = result.getPeriodInstance();
        JackpotWinnerProof proof = result.getWinnerProof();
        Map<String, Object> row = new LinkedHashMap<>();
        row.put("periodType", instance.getPeriodType().name());
        row.put("periodStart", instance.getPeriodStart());
        row.put("periodEnd", instance.getPeriodEnd());
        row.put("maturityBlock", instance.getMaturityBlock());
        row.put("payoutBlockIndex", block.getIndex());
        row.put("payoutBlockHash", blockHash(block));
        row.put("payoutTxHash", safeTransactionHash(transaction));
        row.put("winnerMinerAddress", result.getActualRecipient());
        row.put("payoutGold", gold(result.getExpectedAmount()));
        row.put("proofStatus", "VALID");
        row.put("selectedOffset", proof == null ? -1 : proof.getSelectedOffset());
        row.put("selectedBlockIndex", proof == null ? -1L : proof.getSelectedBlockIndex());
        return row;
    }

    private static List<Map<String, Object>> disabledTicketSummaries() {
        List<Map<String, Object>> summaries = new ArrayList<>();
        for (JackpotCalculator.JackpotPeriod period : periodsInOrder()) {
            Map<String, Object> row = periodDefinition(period);
            row.put("tickets", 0);
            row.put("eligibleBlockIndexes", Collections.emptyList());
            row.put("status", "DISABLED");
            summaries.add(row);
        }
        return Collections.unmodifiableList(summaries);
    }

    private static List<Map<String, Object>> ticketSummaries(List<Block> canonicalChain, long currentHeight, String address) {
        List<Map<String, Object>> summaries = new ArrayList<>();
        String normalized = safe(address);
        for (JackpotCalculator.JackpotPeriod period : periodsInOrder()) {
            JackpotPeriodInstance instance = currentPeriodInstance(period, currentHeight);
            List<JackpotTicket> tickets = V35JackpotChainContextAdapter.collectEligibleTickets(
                    canonicalChain, instance, Seting.V35_RESCUE_FORK_INDEX);
            List<Long> indexes = new ArrayList<>();
            for (JackpotTicket ticket : tickets) {
                if (ticket.getMinerAddress().equals(normalized)) {
                    indexes.add(ticket.getBlockIndex());
                }
            }
            Map<String, Object> row = periodDefinition(period);
            row.put("periodStart", instance.getPeriodStart());
            row.put("periodEnd", instance.getPeriodEnd());
            row.put("tickets", indexes.size());
            row.put("eligibleTicketCount", tickets.size());
            row.put("eligibleBlockIndexes", Collections.unmodifiableList(indexes));
            row.put("status", "ACTIVE");
            summaries.add(row);
        }
        return Collections.unmodifiableList(summaries);
    }

    private static JackpotProofResult findPaidPayout(List<Block> canonicalChain, JackpotPeriodInstance instance) {
        Block payoutBlock = blockByIndex(canonicalChain, instance == null ? -1L : instance.getFirstPayoutBlock());
        if (payoutBlock == null || payoutBlock.getDtoTransactions() == null || instance == null || !instance.isValid()) {
            return JackpotProofResult.invalid("payout not found", null, instance,
                    JackpotCalculator.expectedPayoutAmount(instance == null ? JackpotCalculator.JackpotPeriod.UNKNOWN : instance.getPeriodType()), "");
        }
        for (DtoTransaction transaction : payoutBlock.getDtoTransactions()) {
            JackpotPayoutCandidate candidate = JackpotPayoutCandidate.fromTransaction(payoutBlock.getIndex(), transaction);
            if (candidate.isBasisPayout() && candidate.getPeriodType() == instance.getPeriodType()) {
                JackpotProofResult result = verifyCandidate(canonicalChain, payoutBlock, transaction, instance);
                if (result.isValid()) {
                    return result;
                }
            }
        }
        return JackpotProofResult.invalid("payout not found", null, instance,
                JackpotCalculator.expectedPayoutAmount(instance.getPeriodType()), "");
    }

    private static JackpotProofResult verifyCandidate(List<Block> canonicalChain,
                                                      Block payoutBlock,
                                                      DtoTransaction transaction,
                                                      JackpotPeriodInstance instance) {
        Set<String> previousMarkers = previousMarkers(canonicalChain, payoutBlock == null ? -1L : payoutBlock.getIndex());
        boolean alreadyPaid = instance != null && instance.isValid() && previousMarkers.contains(instance.getDuplicateKey());
        return V35JackpotProofService.verifyPayout(canonicalChain, payoutBlock, transaction,
                Seting.ADDRESS_FOUNDER, previousMarkers, alreadyPaid, Seting.V35_RESCUE_FORK_INDEX);
    }

    private static Set<String> previousMarkers(List<Block> canonicalChain, long payoutBlockIndex) {
        if (canonicalChain == null || canonicalChain.isEmpty()) {
            return Collections.emptySet();
        }
        Set<String> markers = new HashSet<>();
        for (Block block : canonicalChain) {
            if (block != null && block.getIndex() < payoutBlockIndex) {
                markers.addAll(V35JackpotProofService.markersForPayoutBlock(block, Seting.V35_RESCUE_FORK_INDEX));
            }
        }
        return Collections.unmodifiableSet(markers);
    }

    private static JackpotPeriodInstance currentPeriodInstance(JackpotCalculator.JackpotPeriod period, long currentHeight) {
        long window = JackpotCalculator.windowBlocksForPeriod(period.getPeriodName());
        long periodStart = Seting.V35_RESCUE_FORK_INDEX
                + ((currentHeight - Seting.V35_RESCUE_FORK_INDEX) / window) * window;
        long periodEnd = periodStart + window - 1L;
        long maturity = JackpotCalculator.maturityBlock(periodEnd, JackpotCalculator.JACKPOT_RANDOM_DELAY_BLOCKS);
        return new JackpotPeriodInstance(period, periodStart, periodEnd, maturity, maturity + 1L);
    }

    private static JackpotPeriodInstance instanceFromPeriodEnd(JackpotCalculator.JackpotPeriod period,
                                                               long periodEnd,
                                                               long forkIndex) {
        if (period == null || period == JackpotCalculator.JackpotPeriod.UNKNOWN || forkIndex == Long.MAX_VALUE) {
            return JackpotPeriodInstance.invalid(period);
        }
        long window = JackpotCalculator.windowBlocksForPeriod(period.getPeriodName());
        long periodStart = periodEnd - window + 1L;
        if (periodStart < forkIndex || ((periodStart - forkIndex) % window) != 0) {
            return JackpotPeriodInstance.invalid(period);
        }
        long maturity = JackpotCalculator.maturityBlock(periodEnd, JackpotCalculator.JACKPOT_RANDOM_DELAY_BLOCKS);
        return new JackpotPeriodInstance(period, periodStart, periodEnd, maturity, maturity + 1L);
    }

    private static Block blockByIndex(List<Block> canonicalChain, long index) {
        if (canonicalChain == null) {
            return null;
        }
        for (Block block : canonicalChain) {
            if (block != null && block.getIndex() == index) {
                return block;
            }
        }
        return null;
    }

    private static JackpotCalculator.JackpotPeriod parsePeriod(String value) {
        if (value == null || value.trim().isEmpty()) {
            return JackpotCalculator.JackpotPeriod.UNKNOWN;
        }
        String normalized = value.trim().replace('-', '_').toUpperCase();
        for (JackpotCalculator.JackpotPeriod period : JackpotCalculator.JackpotPeriod.values()) {
            if (period.name().equals(normalized) || period.getPeriodName().replace('-', '_').toUpperCase().equals(normalized)) {
                return period;
            }
        }
        return JackpotCalculator.JackpotPeriod.UNKNOWN;
    }

    private static List<JackpotCalculator.JackpotPeriod> periodsInOrder() {
        List<JackpotCalculator.JackpotPeriod> periods = new ArrayList<>();
        periods.add(JackpotCalculator.JackpotPeriod.DAILY);
        periods.add(JackpotCalculator.JackpotPeriod.WEEKLY);
        periods.add(JackpotCalculator.JackpotPeriod.MONTHLY);
        periods.add(JackpotCalculator.JackpotPeriod.QUARTERLY);
        periods.add(JackpotCalculator.JackpotPeriod.HALF_YEAR);
        periods.add(JackpotCalculator.JackpotPeriod.YEARLY);
        return periods;
    }

    private static boolean isV35Active(long currentHeight) {
        return Seting.V35_RESCUE_FORK_INDEX != Long.MAX_VALUE
                && currentHeight >= Seting.V35_RESCUE_FORK_INDEX;
    }

    private static Object forkIndexValue() {
        if (Seting.V35_RESCUE_FORK_INDEX == Long.MAX_VALUE) {
            return "Long.MAX_VALUE";
        }
        return Seting.V35_RESCUE_FORK_INDEX;
    }

    private static String blockHash(Block block) {
        if (block == null || block.getHashBlock() == null || block.getHashBlock().trim().isEmpty()) {
            return "";
        }
        return block.getHashBlock().trim();
    }

    private static String safeTransactionHash(DtoTransaction transaction) {
        if (transaction == null) {
            return "";
        }
        try {
            return transaction.toSign();
        } catch (RuntimeException e) {
            return "";
        }
    }

    private static String gold(double value) {
        return gold(BigDecimal.valueOf(value));
    }

    private static String gold(BigDecimal value) {
        if (value == null || value.compareTo(BigDecimal.ZERO) < 0) {
            return BigDecimal.ZERO.setScale(GOLD_SCALE).toPlainString();
        }
        return value.setScale(GOLD_SCALE, RoundingMode.HALF_UP).toPlainString();
    }

    private static String safe(String value) {
        return value == null ? "" : value.trim();
    }
}
