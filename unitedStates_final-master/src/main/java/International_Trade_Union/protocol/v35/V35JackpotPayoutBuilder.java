package International_Trade_Union.protocol.v35;

import International_Trade_Union.entity.DtoTransaction.DtoTransaction;
import International_Trade_Union.entity.blockchain.block.Block;
import International_Trade_Union.setings.Seting;
import International_Trade_Union.vote.Laws;
import International_Trade_Union.vote.VoteEnum;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Pure V35 jackpot payout builder. It creates candidate BASIS payout
 * transactions from explicit canonical chain context, but it is not wired into
 * mining, validation, balances, controllers, or persistence.
 */
public final class V35JackpotPayoutBuilder {
    private V35JackpotPayoutBuilder() {
    }

    public static List<DtoTransaction> buildPayoutTransactions(List<Block> canonicalChain,
                                                               long payoutBlockIndex,
                                                               String minerAddress,
                                                               String founderAddress) {
        return buildPayoutTransactions(canonicalChain, payoutBlockIndex, minerAddress,
                founderAddress, Seting.V35_RESCUE_FORK_INDEX);
    }

    public static List<DtoTransaction> buildPayoutTransactions(List<Block> canonicalChain,
                                                               long payoutBlockIndex,
                                                               String minerAddress,
                                                               String founderAddress,
                                                               long forkIndex) {
        if (!V35JackpotProofService.isV35ActiveAt(payoutBlockIndex, forkIndex)
                || canonicalChain == null
                || canonicalChain.isEmpty()) {
            return Collections.emptyList();
        }

        List<DtoTransaction> payouts = new ArrayList<>();
        for (JackpotCalculator.JackpotPeriod period : supportedPeriods()) {
            DtoTransaction payout = buildPayoutTransactionForPeriod(
                    canonicalChain, payoutBlockIndex, minerAddress, founderAddress, forkIndex, period, payouts);
            if (payout != null) {
                payouts.add(payout);
            }
        }
        return Collections.unmodifiableList(payouts);
    }

    public static List<JackpotPayoutCandidate> buildPayoutCandidates(List<Block> canonicalChain,
                                                                     long payoutBlockIndex,
                                                                     String minerAddress,
                                                                     String founderAddress) {
        return buildPayoutCandidates(canonicalChain, payoutBlockIndex, minerAddress,
                founderAddress, Seting.V35_RESCUE_FORK_INDEX);
    }

    public static List<JackpotPayoutCandidate> buildPayoutCandidates(List<Block> canonicalChain,
                                                                     long payoutBlockIndex,
                                                                     String minerAddress,
                                                                     String founderAddress,
                                                                     long forkIndex) {
        List<DtoTransaction> transactions = buildPayoutTransactions(
                canonicalChain, payoutBlockIndex, minerAddress, founderAddress, forkIndex);
        if (transactions.isEmpty()) {
            return Collections.emptyList();
        }
        List<JackpotPayoutCandidate> candidates = new ArrayList<>();
        for (DtoTransaction transaction : transactions) {
            candidates.add(JackpotPayoutCandidate.fromTransaction(payoutBlockIndex, transaction));
        }
        return Collections.unmodifiableList(candidates);
    }

    public static boolean isExactPayoutSlot(JackpotCalculator.JackpotPeriod period,
                                            long payoutBlockIndex,
                                            long forkIndex) {
        JackpotPeriodInstance instance = V35JackpotChainContextAdapter.buildPeriodInstance(
                period, payoutBlockIndex, forkIndex);
        return instance.isValid() && instance.getFirstPayoutBlock() == payoutBlockIndex;
    }

    private static DtoTransaction buildPayoutTransactionForPeriod(List<Block> canonicalChain,
                                                                  long payoutBlockIndex,
                                                                  String minerAddress,
                                                                  String founderAddress,
                                                                  long forkIndex,
                                                                  JackpotCalculator.JackpotPeriod period,
                                                                  List<DtoTransaction> alreadyBuilt) {
        JackpotPeriodInstance instance = V35JackpotChainContextAdapter.buildPeriodInstance(
                period, payoutBlockIndex, forkIndex);
        if (!instance.isValid() || instance.getFirstPayoutBlock() != payoutBlockIndex) {
            return null;
        }
        if (V35JackpotChainContextAdapter.hasPriorPayoutForPeriod(
                canonicalChain, instance, payoutBlockIndex, forkIndex)) {
            return null;
        }

        List<JackpotTicket> tickets = V35JackpotChainContextAdapter.collectEligibleTickets(
                canonicalChain, instance, forkIndex);
        List<String> futureHashes = V35JackpotChainContextAdapter.collectFutureHashes(canonicalChain, instance);
        JackpotWinnerProof winnerProof = JackpotCalculator.selectWinner(
                instance.getPeriodStart(), instance.getPeriodEnd(), tickets, futureHashes);
        if (winnerProof == null || !winnerProof.hasWinner()) {
            return null;
        }

        BigDecimal amount = JackpotCalculator.expectedPayoutAmount(period);
        DtoTransaction payout = new DtoTransaction(
                Seting.BASIS_ADDRESS,
                winnerProof.getWinnerMinerAddress(),
                amount.doubleValue(),
                0.0,
                new Laws(),
                0.0,
                VoteEnum.YES);

        List<DtoTransaction> provisionalTransactions = new ArrayList<>(alreadyBuilt);
        provisionalTransactions.add(payout);
        Block provisionalPayoutBlock = provisionalBlock(
                canonicalChain, payoutBlockIndex, minerAddress, founderAddress, provisionalTransactions);

        JackpotProofResult result = V35JackpotChainContextAdapter.verifyPayoutWithChainContext(
                canonicalChain, provisionalPayoutBlock, payout, founderAddress, forkIndex);
        return result.isValid() ? payout : null;
    }

    private static Block provisionalBlock(List<Block> canonicalChain,
                                          long payoutBlockIndex,
                                          String minerAddress,
                                          String founderAddress,
                                          List<DtoTransaction> transactions) {
        return new Block(
                new ArrayList<>(transactions),
                latestHash(canonicalChain),
                normalize(minerAddress),
                normalize(founderAddress),
                0L,
                0.0,
                0L,
                new Timestamp(0L),
                payoutBlockIndex,
                "");
    }

    private static String latestHash(List<Block> canonicalChain) {
        if (canonicalChain == null || canonicalChain.isEmpty()) {
            return "";
        }
        Block latest = canonicalChain.get(canonicalChain.size() - 1);
        return latest == null || latest.getHashBlock() == null ? "" : latest.getHashBlock();
    }

    private static List<JackpotCalculator.JackpotPeriod> supportedPeriods() {
        List<JackpotCalculator.JackpotPeriod> periods = new ArrayList<>();
        periods.add(JackpotCalculator.JackpotPeriod.DAILY);
        periods.add(JackpotCalculator.JackpotPeriod.WEEKLY);
        periods.add(JackpotCalculator.JackpotPeriod.MONTHLY);
        periods.add(JackpotCalculator.JackpotPeriod.QUARTERLY);
        periods.add(JackpotCalculator.JackpotPeriod.HALF_YEAR);
        periods.add(JackpotCalculator.JackpotPeriod.YEARLY);
        return periods;
    }

    private static String normalize(String value) {
        return value == null ? "" : value.trim();
    }
}
