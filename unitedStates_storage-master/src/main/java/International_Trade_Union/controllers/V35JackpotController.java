package International_Trade_Union.controllers;

import International_Trade_Union.entity.blockchain.block.Block;
import International_Trade_Union.entity.entities.EntityBlock;
import International_Trade_Union.entity.services.BlockService;
import International_Trade_Union.protocol.v35.V35JackpotApiResponseBuilder;
import International_Trade_Union.utils.UtilsBlockToEntityBlock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@RestController
public class V35JackpotController {
    private final BlockService blockService;

    @Autowired
    public V35JackpotController(BlockService blockService) {
        this.blockService = blockService;
    }

    @GetMapping("/v35/jackpot/status")
    public Map<String, Object> status() throws IOException {
        long height = currentHeight();
        return V35JackpotApiResponseBuilder.status(canonicalChain(height), height);
    }

    @GetMapping("/v35/jackpot/periods")
    public Map<String, Object> periods() {
        return V35JackpotApiResponseBuilder.periods(currentHeight());
    }

    @GetMapping("/v35/jackpot/history")
    public Map<String, Object> history() throws IOException {
        long height = currentHeight();
        return V35JackpotApiResponseBuilder.history(canonicalChain(height), height);
    }

    @GetMapping("/v35/jackpot/tickets/{address}")
    public Map<String, Object> tickets(@PathVariable String address) throws IOException {
        long height = currentHeight();
        return V35JackpotApiResponseBuilder.tickets(canonicalChain(height), height, address);
    }

    @GetMapping("/v35/jackpot/proof/{periodType}/{periodEnd}")
    public Map<String, Object> proof(@PathVariable String periodType,
                                     @PathVariable long periodEnd) throws IOException {
        long height = currentHeight();
        return V35JackpotApiResponseBuilder.proof(canonicalChain(height), height, periodType, periodEnd);
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
        return count <= 0 ? -1L : count - 1L;
    }
}
