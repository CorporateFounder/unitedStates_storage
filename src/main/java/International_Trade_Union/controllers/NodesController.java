package International_Trade_Union.controllers;

import International_Trade_Union.setings.Seting;
import International_Trade_Union.utils.*;
import org.json.JSONException;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SignatureException;
import java.security.spec.InvalidKeySpecException;
import java.util.List;
import java.util.Set;

@RestController
public class NodesController {


    /**добавить адрес узла*/
    @PostMapping("/putNode")

    public void addNode(@RequestBody MyHost host)  {
        System.out.println("add node: host: " + host);
        UtilsAllAddresses.putNode(host);

    }


}
