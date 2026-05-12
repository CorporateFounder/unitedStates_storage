package International_Trade_Union.protocol.v35;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class V35TargetDifficultyTest {
    @Test
    public void exposesTargetDifficultyConstants() {
        assertEquals(60, V35TargetDifficulty.getTargetWindow());
        assertEquals(150, V35TargetDifficulty.getTargetBlockTimeSeconds());
        assertEquals(1800L, V35TargetDifficulty.getEmergencyDelaySeconds());
    }
}
