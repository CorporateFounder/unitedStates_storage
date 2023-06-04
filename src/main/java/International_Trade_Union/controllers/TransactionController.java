package International_Trade_Union.controllers;


import International_Trade_Union.entity.DtoTransaction.DtoTransaction;
import International_Trade_Union.network.AllTransactions;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SignatureException;
import java.security.spec.InvalidKeySpecException;
import java.util.List;
import java.util.stream.Collectors;

@RestController
public class TransactionController {
    @RequestMapping(method = RequestMethod.POST, value = "/addTransaction", consumes = MediaType.APPLICATION_JSON_VALUE)
    public  void add(@RequestBody DtoTransaction data) throws IOException, NoSuchAlgorithmException, SignatureException, InvalidKeySpecException, NoSuchProviderException, InvalidKeyException {
        System.out.println("add transaction: " + data);
        AllTransactions.getInstance();
        if(!AllTransactions.getInstance().contains(data))
            AllTransactions.addTransaction(data);
        System.out.println("TransactionController: add: " + AllTransactions.getInstance().size());
    }

    @GetMapping("/getTransactions")
    public List<DtoTransaction> getTransaction() throws IOException, NoSuchAlgorithmException, InvalidKeySpecException, SignatureException, NoSuchProviderException, InvalidKeyException {
        return AllTransactions.getInstance().stream().distinct().collect(Collectors.toList());
    }

}
