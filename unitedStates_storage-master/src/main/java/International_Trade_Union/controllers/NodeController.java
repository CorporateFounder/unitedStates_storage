package International_Trade_Union.controllers;

import International_Trade_Union.entity.blockchain.Blockchain;
import International_Trade_Union.entity.services.BlockService;
import International_Trade_Union.model.NodeState;
import International_Trade_Union.model.SlidingWindowManager;
import International_Trade_Union.utils.UtilsBalance;
import International_Trade_Union.utils.UtilsBlock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.web.bind.annotation.*;

import javax.annotation.PostConstruct;

@RestController
public class NodeController {
    @Autowired
    BlockService blockService;

    private static final NodeState nodeState = new NodeState("not_ready");

    @GetMapping("/confirmReadiness")
    @ResponseBody
    public String confirmReadiness() {
        return nodeState.getState();
    }

    @PostConstruct
    public void init() {
        Blockchain.setBlockService(blockService);
        UtilsBalance.setBlockService(blockService);
        UtilsBlock.setBlockService(blockService);


    }

    // Добавим методы для внутреннего управления состоянием
    public static void setReady() {
        nodeState.setState("ready");
    }

    public static void setNotReady() {
        nodeState.setState("not_ready");
    }

}