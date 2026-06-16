package International_Trade_Union.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TournamentSnapshot {
    private long blockIndex;
    private long tournamentStartTime;
    private long tournamentEndTime;
    private int candidateCount;
    private List<LiteVersionWiner> candidates = new ArrayList<>();
    private LiteVersionWiner selectedWinner;
    private String selectedWinnerMinerAddress;
    private String selectedWinnerHash;
    private int selectedWinnerBigRandom;
    private long selectedWinnerDifficulty;
    private int selectedWinnerTxCount;
    private double selectedWinnerStakingScore;
    private String selectionStage;
    private String selectionReason;
    private String createdAt;

    public static TournamentSnapshot empty() {
        return new TournamentSnapshot(
                -1L,
                -1L,
                -1L,
                0,
                new ArrayList<>(),
                null,
                null,
                null,
                0,
                0L,
                0,
                0.0,
                "none",
                "snapshot_not_created",
                new Date().toString());
    }
}
