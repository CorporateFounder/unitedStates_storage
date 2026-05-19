package International_Trade_Union.protocol.v35;

import International_Trade_Union.entity.blockchain.Blockchain;
import International_Trade_Union.entity.blockchain.DataShortBlockchainInformation;
import International_Trade_Union.entity.blockchain.block.Block;
import International_Trade_Union.entity.services.BlockService;
import International_Trade_Union.model.Account;
import International_Trade_Union.setings.Seting;
import International_Trade_Union.utils.UtilsBlock;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class ProductionJackpotValidationWiringTest {

    @Test
    public void contextAwareProductionValidationEntrypointsExistWhileV35Disabled() throws Exception {
        assertEquals(Long.MAX_VALUE, Seting.V35_RESCUE_FORK_INDEX);

        assertNotNull(UtilsBlock.class.getMethod(
                "validationOneBlockWithCanonicalContext",
                String.class,
                Block.class,
                Block.class,
                List.class,
                BlockService.class,
                Map.class,
                List.class,
                List.class,
                List.class,
                long.class));

        assertNotNull(Blockchain.class.getMethod(
                "shortCheckWithCanonicalContext",
                Block.class,
                List.class,
                DataShortBlockchainInformation.class,
                List.class,
                Map.class,
                List.class,
                Map.class,
                List.class,
                List.class,
                long.class));
    }
}
