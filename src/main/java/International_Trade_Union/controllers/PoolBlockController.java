package International_Trade_Union.controllers;

import International_Trade_Union.entity.entities.PoolBlock;
import International_Trade_Union.entity.services.PoolBlockService;

import International_Trade_Union.entity.services.RewardDistributionService;
import International_Trade_Union.setings.Seting;
import International_Trade_Union.setings.SetingPool;
import International_Trade_Union.utils.UtilsFileSaveRead;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Map;

@RestController
@RequestMapping("/pool")
public class PoolBlockController {
    @Autowired
    private PoolBlockService poolBlockService;

    @Autowired
    private RewardDistributionService rewardDistributionService;

    @PostMapping("/add")
    public ResponseEntity<String> addPoolBlock(@RequestBody PoolBlock poolBlock) {
        try {
            System.out.println("**********************");
            System.out.println("add block to pool: " + poolBlock);
            System.out.println("**********************");
            poolBlockService.addPoolBlock(poolBlock);
            return ResponseEntity.ok("Block added");
        } catch (IOException e) {
            return ResponseEntity.badRequest().body("Ошибка: " + e.getMessage());
        }
    }

    @GetMapping("/getPoolBlockMap")
    @ResponseBody
    public Map get(){

        return poolBlockService.getPoolBlockMap();
    }


    @GetMapping("/ConfirmedBalances")
    @ResponseBody
    public Map ConfirmedBalances(){
        return rewardDistributionService.getConfirmedBalances();
    }
    @GetMapping("/getPendingTxMap")
    @ResponseBody
    public Map getPendingTxMap(){
        return rewardDistributionService.getPendingTxMap();
    }
    @GetMapping("/getPoolAddress")
    @ResponseBody
    public String getPool() throws IOException {
        return UtilsFileSaveRead.loadJson(SetingPool.SETING_FILE, SetingPool.class).getPublicKey();
    }
}
