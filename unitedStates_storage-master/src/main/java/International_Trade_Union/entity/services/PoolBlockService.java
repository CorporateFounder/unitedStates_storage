package International_Trade_Union.entity.services;

import International_Trade_Union.controllers.BasisController;
import International_Trade_Union.entity.blockchain.block.Block;
import International_Trade_Union.entity.entities.PoolBlock;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

@Service
public class PoolBlockService {
    static final int MAX_POOL_BLOCKS = 256;
    // Хранение полученных блоков по хэшу
    private final Map<String, PoolBlock> poolBlockMap = new LinkedHashMap<String, PoolBlock>(MAX_POOL_BLOCKS, 0.75f, true) {
        @Override
        protected boolean removeEldestEntry(Map.Entry<String, PoolBlock> eldest) {
            return size() > MAX_POOL_BLOCKS;
        }
    };

    public void addPoolBlock(PoolBlock poolBlock) throws IOException {
        if (poolBlock == null || poolBlock.getBlock() == null) {
            return;
        }
        Block tempBlock = poolBlock.getBlock();
        Block prevBlock = BasisController.prevBlock();
        if (prevBlock == null) {
            System.out.println("addPoolBlock: prevBlock is null");
            return;
        }
        // Проверка корректности блока
        if (!tempBlock.hashForTransaction().equals(tempBlock.getHashBlock())) {
            System.out.println("addPoolBlock: wrong hash:");
            return;
        }
        synchronized (poolBlockMap) {
            if (poolBlockMap.containsKey(tempBlock.getHashBlock())) {
                System.out.println("addPoolBlock: contains hash");
                return;
            }
        }
        if (tempBlock.getIndex() <= prevBlock.getIndex()) {
            System.out.println("addPoolBlock: wrong index");
            return;
        }
        synchronized (poolBlockMap) {
            poolBlockMap.put(tempBlock.getHashBlock(), poolBlock);
        }
    }

    public Map<String, PoolBlock> getPoolBlockMap() {
        return poolBlockMap;
    }

    public int size() {
        synchronized (poolBlockMap) {
            return poolBlockMap.size();
        }
    }

    public void clearPoolBlocks() {
        synchronized (poolBlockMap) {
            poolBlockMap.clear();
        }
    }
}
