package International_Trade_Union.controllers;

import International_Trade_Union.entity.blockchain.block.Block;
import International_Trade_Union.entity.entities.EntityAccount;
import International_Trade_Union.entity.entities.EntityBlock;
import International_Trade_Union.entity.services.BlockService;
import International_Trade_Union.model.Account;
import International_Trade_Union.protocol.v35.V35SupplyApiResponseBuilder;
import International_Trade_Union.protocol.v35.V35SupplyChainAdapter;
import International_Trade_Union.protocol.v35.V35SupplySnapshot;
import International_Trade_Union.utils.UtilsAccountToEntityAccount;
import International_Trade_Union.utils.UtilsBlockToEntityBlock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@RestController
public class V35SupplyController {
    private final BlockService blockService;

    @Autowired
    public V35SupplyController(BlockService blockService) {
        this.blockService = blockService;
    }

    @GetMapping("/v35/supply")
    public Map<String, Object> supply() throws IOException {
        SupplyContext context = supplyContext();
        return V35SupplyApiResponseBuilder.supply(context.snapshot, context.currentHeight);
    }

    @GetMapping("/v35/protocol")
    public Map<String, Object> protocol() {
        return V35SupplyApiResponseBuilder.protocol(currentHeight());
    }

    @GetMapping("/v35/burn")
    public Map<String, Object> burn() throws IOException {
        SupplyContext context = supplyContext();
        return V35SupplyApiResponseBuilder.burn(context.snapshot, context.currentHeight);
    }

    @GetMapping("/v35/founder")
    public Map<String, Object> founder() throws IOException {
        SupplyContext context = supplyContext();
        return V35SupplyApiResponseBuilder.founder(context.snapshot, context.currentHeight);
    }

    @GetMapping("/v35/founder/burn-snapshot")
    public Map<String, Object> founderBurnSnapshot() throws IOException {
        SupplyContext context = supplyContext();
        return V35SupplyApiResponseBuilder.founderBurnSnapshot(
                context.snapshot,
                context.currentHeight,
                context.snapshotBlockHash);
    }

    private SupplyContext supplyContext() throws IOException {
        long height = currentHeight();
        List<Block> chain = canonicalChain(height);
        List<EntityAccount> entityAccounts = blockService.findAllAccounts();
        Map<String, Account> accounts = UtilsAccountToEntityAccount.entityAccountsToMapAccounts(entityAccounts);
        V35SupplySnapshot snapshot = V35SupplyChainAdapter.snapshotFromChainAndAccounts(chain, accounts);
        return new SupplyContext(snapshot, height, latestBlockHash(chain));
    }

    private List<Block> canonicalChain(long currentHeight) throws IOException {
        if (currentHeight < 0) {
            return Collections.emptyList();
        }
        List<EntityBlock> entityBlocks = blockService.findBySpecialIndexBetween(0, currentHeight);
        if (entityBlocks == null || entityBlocks.isEmpty()) {
            return Collections.emptyList();
        }
        return UtilsBlockToEntityBlock.entityBlocksToBlocks(entityBlocks);
    }

    private long currentHeight() {
        long count = blockService.countBlock();
        return count <= 0 ? -1 : count - 1;
    }

    private String latestBlockHash(List<Block> chain) {
        if (chain == null || chain.isEmpty()) {
            return "UNKNOWN";
        }
        Block latest = null;
        for (Block block : chain) {
            if (block != null && (latest == null || block.getIndex() > latest.getIndex())) {
                latest = block;
            }
        }
        if (latest == null || latest.getHashBlock() == null || latest.getHashBlock().trim().isEmpty()) {
            return "UNKNOWN";
        }
        return latest.getHashBlock();
    }

    private static final class SupplyContext {
        private final V35SupplySnapshot snapshot;
        private final long currentHeight;
        private final String snapshotBlockHash;

        private SupplyContext(V35SupplySnapshot snapshot, long currentHeight, String snapshotBlockHash) {
            this.snapshot = snapshot;
            this.currentHeight = currentHeight;
            this.snapshotBlockHash = snapshotBlockHash;
        }
    }
}
