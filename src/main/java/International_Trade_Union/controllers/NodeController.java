package International_Trade_Union.controllers;

import International_Trade_Union.model.NodeState;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class NodeController {

    private NodeState nodeState = NodeState.NOT_READY;

    @GetMapping("/isReady")
    public String isReady() {
        return nodeState == NodeState.READY || nodeState == NodeState.DONE ? "true" : "false";
    }

    @PostMapping("/setAllWinnerReady")
    public void setAllWinnerReady(@RequestParam boolean ready) {
        if (ready) {
            this.nodeState = NodeState.READY;
        }
    }

    @PostMapping("/setAllWinnerDone")
    public void setAllWinnerDone() {
        this.nodeState = NodeState.DONE;
    }

    @PostMapping("/setAllWinnerNotReady")
    public void setAllWinnerNotReady() {
        this.nodeState = NodeState.NOT_READY;
    }
}
