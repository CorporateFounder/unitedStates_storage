package International_Trade_Union.entity.services;

import International_Trade_Union.controllers.BasisController;
import International_Trade_Union.entity.blockchain.block.Block;
import International_Trade_Union.entity.entities.PoolBlock;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Service
public class PoolBlockService {
    // Хранение полученных блоков по хэшу
    private final Map<String, PoolBlock> poolBlockMap = new HashMap<>();

    public void addPoolBlock(PoolBlock poolBlock) throws IOException {
        Block tempBlock = poolBlock.getBlock();
        Block prevBlock = BasisController.prevBlock();
        // Проверка корректности блока
        if (!tempBlock.hashForTransaction().equals(tempBlock.getHashBlock())) {
            System.out.println("addPoolBlock: wrong hash:");
            return;
        }
        if (poolBlockMap.containsKey(tempBlock.getHashBlock())) {
            System.out.println("addPoolBlock: contains hash");
            return;
        }
        if (tempBlock.getIndex() <= prevBlock.getIndex()) {
            System.out.println("addPoolBlock: wrong index");
            return;
        }
        poolBlockMap.put(tempBlock.getHashBlock(), poolBlock);
    }

    public Map<String, PoolBlock> getPoolBlockMap() {
        return poolBlockMap;
    }

    public void clearPoolBlocks() {
        poolBlockMap.clear();
    }
}
