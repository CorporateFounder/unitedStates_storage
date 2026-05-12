package International_Trade_Union.protocol.v35;

/**
 * Documentation holder for the future V35 chain scoring policy.
 */
public final class ChainTournamentScorer {
    public static final String INTENDED_PRIORITY =
            "1. valid chain; 2. highest cumulativeWork; 3. latest valid height; "
                    + "4. capped stake/tx tie-breaker; 5. deterministic hash tie-breaker";

    private ChainTournamentScorer() {
    }

    public static String intendedPriority() {
        return INTENDED_PRIORITY;
    }
}
