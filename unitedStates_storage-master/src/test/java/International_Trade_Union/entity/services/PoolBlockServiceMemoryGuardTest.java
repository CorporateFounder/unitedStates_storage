package International_Trade_Union.entity.services;

import International_Trade_Union.entity.entities.PoolBlock;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

class PoolBlockServiceMemoryGuardTest {

    @Test
    void poolBlockMapIsCappedEvenWhenManyTemporaryEntriesArrive() {
        PoolBlockService service = new PoolBlockService();

        for (int i = 0; i < PoolBlockService.MAX_POOL_BLOCKS + 50; i++) {
            service.getPoolBlockMap().put("hash-" + i, new PoolBlock());
        }

        assertTrue(service.size() <= PoolBlockService.MAX_POOL_BLOCKS);
    }
}
