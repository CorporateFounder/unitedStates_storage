package International_Trade_Union.controllers;

import International_Trade_Union.model.NodeState;
import org.springframework.boot.SpringApplication;
import org.springframework.web.bind.annotation.*;

@RestController
public class NodeController {

    private static final NodeState nodeState = new NodeState("not_ready");

    @GetMapping("/confirmReadiness")
    @ResponseBody
    public String confirmReadiness() {
        return nodeState.getState();
    }

    // Добавим методы для внутреннего управления состоянием
    public static void setReady() {
        nodeState.setState("ready");
    }

    public static void setNotReady() {
        nodeState.setState("not_ready");
    }

}