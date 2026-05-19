package International_Trade_Union.protocol.v35;

import International_Trade_Union.entity.DtoTransaction.DtoTransaction;
import International_Trade_Union.entity.blockchain.block.Block;
import International_Trade_Union.setings.Seting;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Pure V35 jackpot chain-context proof service. It does not mutate blocks,
 * create payouts, update balances, or call controllers/database/network code.
 */
public final class V35JackpotProofService {
    private static final BigDecimal ZERO = BigDecimal.ZERO.setScale(2);

    private V35JackpotProofService() {
    }

    public static JackpotProofResult verifyPayout(List<Block> canonicalBlocks,
                                                  Block payoutBlock,
                                                  DtoTransaction payoutTransaction,
                                                  String founderAddress) {
        return verifyPayout(canonicalBlocks, payoutBlock, payoutTransaction, founderAddress,
                Collections.emptySet(), false, Seting.V35_RESCUE_FORK_INDEX);
    }

    public static JackpotProofResult verifyPayout(List<Block> canonicalBlocks,
                                                  Block payoutBlock,
                                                  DtoTransaction payoutTransaction,
                                                  String founderAddress,
                                                  Set<String> previousPayoutMarkers,
                                                  boolean alreadyPaid,
                                                  long forkIndex) {
        if (payoutBlock == null || payoutTransaction == null) {
            return invalid("missing payout block or transaction", null, null, ZERO, "");
        }

        JackpotPayoutCandidate candidate = JackpotPayoutCandidate.fromTransaction(
                payoutBlock.getIndex(), payoutTransaction);
        JackpotPeriodInstance instance = periodInstanceForPayoutBlock(
                candidate.getPeriodType(), payoutBlock.getIndex(), forkIndex);
        BigDecimal expectedAmount = JackpotCalculator.expectedPayoutAmount(candidate.getPeriodType());

        if (!isV35ActiveAt(payoutBlock.getIndex(), forkIndex)) {
            return invalid("V35 is not active for payout block", null, instance, expectedAmount, candidate.getRecipient());
        }
        if (!candidate.isBasisPayout()) {
            return invalid("payout sender is not BASIS", null, instance, expectedAmount, candidate.getRecipient());
        }
        if (candidate.getPeriodType() == JackpotCalculator.JackpotPeriod.UNKNOWN) {
            return invalid("payout amount is not canonical", null, instance, expectedAmount, candidate.getRecipient());
        }
        if (Math.abs(candidate.getDigitalStockBalance()) > 0.00000001) {
            return invalid("jackpot payout stock must be zero", null, instance, expectedAmount, candidate.getRecipient());
        }
        if (Math.abs(candidate.getBonusForMiner()) > 0.00000001) {
            return invalid("jackpot payout bonus must be zero", null, instance, expectedAmount, candidate.getRecipient());
        }
        if (candidate.getRecipient().isEmpty() || Seting.BASIS_ADDRESS.equals(candidate.getRecipient())) {
            return invalid("invalid jackpot recipient", null, instance, expectedAmount, candidate.getRecipient());
        }
        if (!normalize(founderAddress).isEmpty() && candidate.getRecipient().equals(normalize(founderAddress))) {
            return invalid("founder cannot receive jackpot payout", null, instance, expectedAmount, candidate.getRecipient());
        }
        if (!instance.isValid() || payoutBlock.getIndex() != instance.getFirstPayoutBlock()) {
            return invalid("payout block is not the deterministic payout slot", null, instance, expectedAmount, candidate.getRecipient());
        }
        if (alreadyPaid || containsMarker(previousPayoutMarkers, instance.getDuplicateKey())) {
            return invalid("jackpot period already paid", null, instance, expectedAmount, candidate.getRecipient());
        }
        if (hasSamePeriodDuplicate(payoutBlock, payoutTransaction, instance, forkIndex)) {
            return invalid("same-period duplicate payout in block", null, instance, expectedAmount, candidate.getRecipient());
        }

        Map<Long, Block> byIndex = blocksByIndex(canonicalBlocks);
        List<JackpotTicket> tickets = ticketsForPeriod(byIndex, instance, forkIndex);
        if (tickets.size() != windowSize(instance.getPeriodType())) {
            return invalid("eligible period tickets are incomplete", null, instance, expectedAmount, candidate.getRecipient());
        }

        List<String> futureHashes = futureHashesForSeed(byIndex, instance);
        if (futureHashes.size() < JackpotCalculator.JACKPOT_RANDOM_DELAY_BLOCKS) {
            return invalid("future seed hashes are insufficient", null, instance, expectedAmount, candidate.getRecipient());
        }

        JackpotWinnerProof proof = JackpotCalculator.selectWinner(
                instance.getPeriodStart(), instance.getPeriodEnd(), tickets, futureHashes);
        if (!proof.hasWinner()) {
            return invalid("winner proof has no winner", proof, instance, expectedAmount, candidate.getRecipient());
        }
        if (!candidate.getRecipient().equals(proof.getWinnerMinerAddress())) {
            return invalid("recipient does not match jackpot winner", proof, instance, expectedAmount, candidate.getRecipient());
        }

        return JackpotProofResult.valid(proof, instance, expectedAmount, candidate.getRecipient());
    }

    public static JackpotPeriodInstance periodInstanceForPayoutBlock(JackpotCalculator.JackpotPeriod periodType,
                                                                     long payoutBlockIndex,
                                                                     long forkIndex) {
        if (periodType == null || periodType == JackpotCalculator.JackpotPeriod.UNKNOWN
                || forkIndex == Long.MAX_VALUE || payoutBlockIndex < forkIndex) {
            return JackpotPeriodInstance.invalid(periodType);
        }
        long window = windowSize(periodType);
        if (window <= 0) {
            return JackpotPeriodInstance.invalid(periodType);
        }
        long seedAnchorBlock = payoutBlockIndex - JackpotCalculator.JACKPOT_RANDOM_DELAY_BLOCKS - 1L;
        if (seedAnchorBlock < forkIndex) {
            return JackpotPeriodInstance.invalid(periodType);
        }
        long periodStart = forkRelativePeriodStart(seedAnchorBlock, window, forkIndex);
        long periodEnd = periodStart + window - 1L;
        long maturityBlock = JackpotCalculator.maturityBlock(periodEnd, JackpotCalculator.JACKPOT_RANDOM_DELAY_BLOCKS);
        long firstPayoutBlock = maturityBlock + 1L;
        if (periodEnd != seedAnchorBlock || firstPayoutBlock != payoutBlockIndex) {
            return JackpotPeriodInstance.invalid(periodType);
        }
        return new JackpotPeriodInstance(periodType, periodStart, periodEnd, maturityBlock, firstPayoutBlock);
    }

    public static boolean isV35ActiveAt(long blockIndex, long forkIndex) {
        return forkIndex != Long.MAX_VALUE && blockIndex >= forkIndex;
    }

    public static boolean containsMarker(Set<String> previousPayoutMarkers, String duplicateKey) {
        return previousPayoutMarkers != null && previousPayoutMarkers.contains(duplicateKey);
    }

    public static List<JackpotTicket> ticketsForPeriod(List<Block> canonicalBlocks,
                                                       JackpotPeriodInstance instance,
                                                       long forkIndex) {
        return ticketsForPeriod(blocksByIndex(canonicalBlocks), instance, forkIndex);
    }

    public static List<String> futureHashesForSeed(List<Block> canonicalBlocks,
                                                   JackpotPeriodInstance instance) {
        return futureHashesForSeed(blocksByIndex(canonicalBlocks), instance);
    }

    public static Set<String> markersForPayoutBlock(Block payoutBlock, long forkIndex) {
        if (payoutBlock == null || payoutBlock.getDtoTransactions() == null) {
            return Collections.emptySet();
        }
        Set<String> markers = new HashSet<>();
        for (DtoTransaction transaction : payoutBlock.getDtoTransactions()) {
            if (transaction == null || !Seting.BASIS_ADDRESS.equals(transaction.getSender())) {
                continue;
            }
            JackpotCalculator.JackpotPeriod periodType = JackpotCalculator.classifyPayoutAmount(transaction.getDigitalDollar());
            JackpotPeriodInstance instance = periodInstanceForPayoutBlock(periodType, payoutBlock.getIndex(), forkIndex);
            if (instance.isValid()) {
                markers.add(instance.getDuplicateKey());
            }
        }
        return Collections.unmodifiableSet(markers);
    }

    private static JackpotProofResult invalid(String reason,
                                              JackpotWinnerProof proof,
                                              JackpotPeriodInstance instance,
                                              BigDecimal expectedAmount,
                                              String recipient) {
        return JackpotProofResult.invalid(reason, proof, instance, expectedAmount, recipient);
    }

    private static boolean hasSamePeriodDuplicate(Block payoutBlock,
                                                  DtoTransaction payoutTransaction,
                                                  JackpotPeriodInstance target,
                                                  long forkIndex) {
        if (payoutBlock.getDtoTransactions() == null || target == null || !target.isValid()) {
            return false;
        }
        int matches = 0;
        for (DtoTransaction transaction : payoutBlock.getDtoTransactions()) {
            if (transaction == null || !Seting.BASIS_ADDRESS.equals(transaction.getSender())) {
                continue;
            }
            JackpotCalculator.JackpotPeriod periodType = JackpotCalculator.classifyPayoutAmount(transaction.getDigitalDollar());
            JackpotPeriodInstance instance = periodInstanceForPayoutBlock(periodType, payoutBlock.getIndex(), forkIndex);
            if (instance.isValid() && target.getDuplicateKey().equals(instance.getDuplicateKey())) {
                matches++;
            }
        }
        if (matches == 0 && payoutTransaction != null) {
            return false;
        }
        return matches > 1;
    }

    private static List<JackpotTicket> ticketsForPeriod(Map<Long, Block> byIndex,
                                                        JackpotPeriodInstance instance,
                                                        long forkIndex) {
        if (byIndex.isEmpty() || instance == null || !instance.isValid()) {
            return Collections.emptyList();
        }
        List<JackpotTicket> tickets = new ArrayList<>();
        for (long index = instance.getPeriodStart(); index <= instance.getPeriodEnd(); index++) {
            Block block = byIndex.get(index);
            if (block == null || !isV35ActiveAt(block.getIndex(), forkIndex)
                    || !JackpotCalculator.isEligibleTicket(block.getIndex(), block.getMinerAddress(), true)) {
                return Collections.emptyList();
            }
            tickets.add(JackpotTicket.of(block.getIndex(), block.getMinerAddress()));
        }
        return JackpotCalculator.eligibleTickets(tickets);
    }

    private static List<String> futureHashesForSeed(Map<Long, Block> byIndex, JackpotPeriodInstance instance) {
        if (byIndex.isEmpty() || instance == null || !instance.isValid()) {
            return Collections.emptyList();
        }
        List<String> hashes = new ArrayList<>();
        long firstSeedBlock = instance.getPeriodEnd() + 1L;
        long lastSeedBlock = instance.getPeriodEnd() + JackpotCalculator.JACKPOT_RANDOM_DELAY_BLOCKS;
        for (long index = firstSeedBlock; index <= lastSeedBlock; index++) {
            Block block = byIndex.get(index);
            if (block == null || block.getHashBlock() == null || block.getHashBlock().trim().isEmpty()) {
                return Collections.emptyList();
            }
            hashes.add(block.getHashBlock().trim());
        }
        return Collections.unmodifiableList(hashes);
    }

    private static Map<Long, Block> blocksByIndex(List<Block> canonicalBlocks) {
        if (canonicalBlocks == null || canonicalBlocks.isEmpty()) {
            return Collections.emptyMap();
        }
        Map<Long, Block> byIndex = new LinkedHashMap<>();
        for (Block block : canonicalBlocks) {
            if (block != null) {
                byIndex.put(block.getIndex(), block);
            }
        }
        return byIndex;
    }

    private static long forkRelativePeriodStart(long blockIndex, long window, long forkIndex) {
        if (blockIndex < forkIndex || window <= 0) {
            return -1L;
        }
        long relative = blockIndex - forkIndex;
        return forkIndex + (relative / window) * window;
    }

    private static long windowSize(JackpotCalculator.JackpotPeriod periodType) {
        if (periodType == null || periodType == JackpotCalculator.JackpotPeriod.UNKNOWN) {
            return -1L;
        }
        return JackpotCalculator.windowBlocksForPeriod(periodType.getPeriodName());
    }

    private static String normalize(String value) {
        return value == null ? "" : value.trim();
    }
}
