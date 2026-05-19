package International_Trade_Union.protocol.v35;

import International_Trade_Union.entity.DtoTransaction.DtoTransaction;
import International_Trade_Union.entity.blockchain.block.Block;
import International_Trade_Union.setings.Seting;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Pure adapter that turns an explicit canonical chain list into inputs for
 * {@link V35JackpotProofService}. It does not wire jackpot logic into block
 * validation, balance accounting, mining, controllers, or persistence.
 */
public final class V35JackpotChainContextAdapter {
    private V35JackpotChainContextAdapter() {
    }

    public static JackpotPayoutCandidate buildPayoutCandidate(Block payoutBlock, DtoTransaction payoutTransaction) {
        long payoutBlockIndex = payoutBlock == null ? -1L : payoutBlock.getIndex();
        return JackpotPayoutCandidate.fromTransaction(payoutBlockIndex, payoutTransaction);
    }

    public static JackpotPeriodInstance buildPeriodInstance(JackpotPayoutCandidate candidate, long forkIndex) {
        if (candidate == null) {
            return JackpotPeriodInstance.invalid(JackpotCalculator.JackpotPeriod.UNKNOWN);
        }
        return buildPeriodInstance(candidate.getPeriodType(), candidate.getPayoutBlockIndex(), forkIndex);
    }

    public static JackpotPeriodInstance buildPeriodInstance(JackpotCalculator.JackpotPeriod periodType,
                                                            long payoutBlockIndex,
                                                            long forkIndex) {
        return V35JackpotProofService.periodInstanceForPayoutBlock(periodType, payoutBlockIndex, forkIndex);
    }

    public static List<JackpotTicket> collectEligibleTickets(List<Block> canonicalChain,
                                                            JackpotPeriodInstance periodInstance,
                                                            long forkIndex) {
        return V35JackpotProofService.ticketsForPeriod(canonicalChain, periodInstance, forkIndex);
    }

    public static List<String> collectFutureHashes(List<Block> canonicalChain,
                                                   JackpotPeriodInstance periodInstance) {
        return V35JackpotProofService.futureHashesForSeed(canonicalChain, periodInstance);
    }

    public static Set<String> collectPriorPayoutMarkers(List<Block> canonicalChain,
                                                        long payoutBlockIndex,
                                                        long forkIndex) {
        if (canonicalChain == null || canonicalChain.isEmpty()) {
            return Collections.emptySet();
        }
        Set<String> markers = new HashSet<>();
        for (Block block : canonicalChain) {
            if (block == null || block.getIndex() > payoutBlockIndex) {
                continue;
            }
            markers.addAll(V35JackpotProofService.markersForPayoutBlock(block, forkIndex));
        }
        return Collections.unmodifiableSet(markers);
    }

    public static boolean hasPriorPayoutForPeriod(List<Block> canonicalChain,
                                                  JackpotPeriodInstance periodInstance,
                                                  long payoutBlockIndex,
                                                  long forkIndex) {
        if (periodInstance == null || !periodInstance.isValid()) {
            return false;
        }
        return collectPriorPayoutMarkers(canonicalChain, payoutBlockIndex, forkIndex)
                .contains(periodInstance.getDuplicateKey());
    }

    public static JackpotProofResult verifyPayoutWithChainContext(List<Block> canonicalChain,
                                                                  Block payoutBlock,
                                                                  DtoTransaction payoutTransaction,
                                                                  String founderAddress) {
        return verifyPayoutWithChainContext(canonicalChain, payoutBlock, payoutTransaction,
                founderAddress, Seting.V35_RESCUE_FORK_INDEX);
    }

    public static JackpotProofResult verifyPayoutWithChainContext(List<Block> canonicalChain,
                                                                  Block payoutBlock,
                                                                  DtoTransaction payoutTransaction,
                                                                  String founderAddress,
                                                                  long forkIndex) {
        JackpotPayoutCandidate candidate = buildPayoutCandidate(payoutBlock, payoutTransaction);
        JackpotPeriodInstance instance = buildPeriodInstance(candidate, forkIndex);
        Set<String> previousMarkers = collectPriorPayoutMarkers(canonicalChain, candidate.getPayoutBlockIndex(), forkIndex);
        boolean alreadyPaid = instance.isValid() && previousMarkers.contains(instance.getDuplicateKey());
        return V35JackpotProofService.verifyPayout(canonicalChain, payoutBlock, payoutTransaction,
                founderAddress, previousMarkers, alreadyPaid, forkIndex);
    }
}
