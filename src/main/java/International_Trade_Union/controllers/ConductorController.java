package International_Trade_Union.controllers;

import International_Trade_Union.controllers.config.BlockchainFactoryEnum;
import International_Trade_Union.entity.DtoTransaction.DtoTransaction;
import International_Trade_Union.entity.blockchain.Blockchain;
import International_Trade_Union.entity.blockchain.block.Block;
import International_Trade_Union.entity.entities.EntityBlock;
import International_Trade_Union.entity.entities.EntityDtoTransaction;
import International_Trade_Union.entity.entities.SignRequest;
import International_Trade_Union.entity.services.BlockService;
import International_Trade_Union.governments.Directors;
import International_Trade_Union.governments.UtilsGovernment;
import International_Trade_Union.logger.MyLogger;
import International_Trade_Union.model.*;

import International_Trade_Union.setings.Seting;
import International_Trade_Union.utils.*;
import International_Trade_Union.utils.base.Base;
import International_Trade_Union.utils.base.Base58;
import International_Trade_Union.vote.Laws;
import International_Trade_Union.vote.VoteEnum;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.util.*;
import java.util.stream.Collectors;

import static International_Trade_Union.controllers.BasisController.getNodes;

@RestController
public class ConductorController {


    @Autowired
    BlockService blockService;

    @PostConstruct
    public void init() {
        Blockchain.setBlockService(blockService);
        UtilsBalance.setBlockService(blockService);
        UtilsBlock.setBlockService(blockService);


    }
    @Autowired
    UtilsResolving utilsResolving;
    /**
     * создает новую пару ключ и пароль.
     * creates a new key and password pair.
     * (from local host)
     */
    @GetMapping("/keys")
    public Map<String, String> keys() throws InvalidAlgorithmParameterException, NoSuchAlgorithmException, InvalidKeySpecException, NoSuchProviderException {
        return CreateAccount.create();
    }





    /**
     *получить полный баланс адреса, по публичному ключу адреса.
     * get the full balance of the address using the public key of the address.
     * (from local host)
     */
    @GetMapping("/account")
    @ResponseBody
    public Account account(@RequestParam String address) throws IOException, NoSuchAlgorithmException, InvalidKeySpecException, SignatureException, NoSuchProviderException, InvalidKeyException {
//        Map<String, Account> balances = SaveBalances.readLineObject(Seting.ORIGINAL_BALANCE_FILE);
        //TODO если что можно это включить
        if (!BasisController.isIsSaveFile()) {
            System.out.println("saving file: resolve_from_to_block: sub block");
            return new Account();
        }
        Map<String, Account> balances = UtilsAccountToEntityAccount.entityAccountsToMapAccounts(blockService.findAllAccounts());

        Account account = UtilsBalance.getBalance(address, balances);

        return account;
    }

    /**
     * get dollar balance
     * (from local host)
     */
    @GetMapping("/dollar")
    @ResponseBody
    public Double dollar(@RequestParam String address) throws IOException, NoSuchAlgorithmException, InvalidKeySpecException, SignatureException, NoSuchProviderException, InvalidKeyException {
//        Map<String, Account> balances = SaveBalances.readLineObject(Seting.ORIGINAL_BALANCE_FILE);

        if (!BasisController.isIsSaveFile()) {
            System.out.println("saving file: resolve_from_to_block: sub block");
            return -1.0;
        }
        Map<String, Account> balances = UtilsAccountToEntityAccount.entityAccountsToMapAccounts(blockService.findAllAccounts());

        Account account = UtilsBalance.getBalance(address, balances);
        return UtilsUse.round(account.getDigitalDollarBalance(), Seting.SENDING_DECIMAL_PLACES).doubleValue();
    }

    /**
     * get stock balance
     */
    @GetMapping("/stock")
    @ResponseBody
    public Double stock(@RequestParam String address) throws IOException, NoSuchAlgorithmException, InvalidKeySpecException, SignatureException, NoSuchProviderException, InvalidKeyException {
//        Map<String, Account> balances = SaveBalances.readLineObject(Seting.ORIGINAL_BALANCE_FILE);

        if (!BasisController.isIsSaveFile()) {
            System.out.println("saving file: resolve_from_to_block: sub block");
            return -1.0;
        }
        Map<String, Account> balances = UtilsAccountToEntityAccount.entityAccountsToMapAccounts(blockService.findAllAccounts());

        Account account = UtilsBalance.getBalance(address, balances);
        return UtilsUse.round(account.getDigitalStockBalance(), Seting.SENDING_DECIMAL_PLACES).doubleValue();
    }

    /**
     * send dollar or stock (if return wrong-its not sending, if return sign its success)
     * (send to global node)
     */
//    @GetMapping("/sendCoin")
//    @ResponseBody
//    public String send(@RequestParam String sender,
//                       @RequestParam String recipient,
//                       @RequestParam(defaultValue = "0.0") Double dollar,
//                       @RequestParam(defaultValue = "0.0") Double stock,
//                       @RequestParam(defaultValue = "0.0") Double reward,
//                       @RequestParam String password) throws NoSuchAlgorithmException, InvalidKeySpecException, NoSuchProviderException, IOException, SignatureException, InvalidKeyException {
//        Base base = new Base58();
//        String result = "false";
//
//        dollar = UtilsUse.round(dollar, Seting.DECIMAL_PLACES);
//        stock = UtilsUse.round(stock, Seting.DECIMAL_PLACES);
//        reward = UtilsUse.round(reward, Seting.DECIMAL_PLACES);
//        if (dollar == null || dollar < 0.0)
//            dollar = 0.0;
//
//        if (stock == null || stock < 0.0)
//            stock = 0.0;
//
//        if (reward == null || reward < 0.0)
//            reward = 0.0;
//
//        Laws laws = new Laws();
//        laws.setLaws(new ArrayList<>());
//        laws.setHashLaw("");
//        laws.setPacketLawName("");
//        DtoTransaction dtoTransaction = new DtoTransaction(
//                sender,
//                recipient,
//                dollar,
//                stock,
//                laws,
//                reward,
//                VoteEnum.YES);
//        PrivateKey privateKey = UtilsSecurity.privateBytToPrivateKey(base.decode(password));
//        byte[] sign = UtilsSecurity.sign(privateKey, dtoTransaction.toSign());
//        System.out.println("Main Controller: new transaction: vote: " + VoteEnum.YES);
//        dtoTransaction.setSign(sign);
//        Directors directors = new Directors();
//        System.out.println("sender: " + sender);
//        System.out.println("recipient: " + recipient);
//        System.out.println("dollar: " + dollar + ": class: " + dollar.getClass());
//        System.out.println("stock: " + stock + ": class: " + stock.getClass());
//        System.out.println("reward: " + reward + ": class: " + reward.getClass());
//        System.out.println("password: " + password);
//        System.out.println("sign: " + dtoTransaction.toSign());
//        System.out.println("verify: " + dtoTransaction.verify());
//
//        if (dtoTransaction.verify()) {
//
//            //если в названия закона совпадает с корпоративными должностями, то закон является действительным только когда
//            //отправитель совпадает с законом
//            List<String> corporateSeniorPositions = directors.getDirectors().stream()
//                    .map(t -> t.getName()).collect(Collectors.toList());
//            System.out.println("LawsController: create_law: " + laws.getPacketLawName() + "contains: " + corporateSeniorPositions.contains(laws.getPacketLawName()));
//            if (corporateSeniorPositions.contains(laws.getPacketLawName()) && !UtilsGovernment.checkPostionSenderEqualsLaw(sender, laws)) {
//                System.out.println("sending" + "wrong transaction: Position to be equals whith send");
//                return result;
//            }
//            result = base.encode(dtoTransaction.getSign());
//
//            String str = base.encode(dtoTransaction.getSign());
//            System.out.println("sign: " + str);
//            AllTransactions.addTransaction(dtoTransaction);
//            String jsonDto = UtilsJson.objToStringJson(dtoTransaction);
//            Set<String> nodesAll = getNodes();
//            List<HostEndDataShortB> sortPriorityHost = utilsResolving.sortPriorityHost(nodesAll);
//
//            for (HostEndDataShortB hostEndDataShortB :sortPriorityHost) {
//
//                String original = hostEndDataShortB.getHost();
//                String url = hostEndDataShortB.getHost() + "/addTransaction";
//                //если адресс совпадает с внутреним хостом, то не отправляет самому себе
//                if (BasisController.getExcludedAddresses().contains(url)) {
//                    System.out.println("MainController: its your address or excluded address: " + url);
//                    continue;
//                }
//                try {
//                    //отправка в сеть
//                    UtilUrl.sendPost(jsonDto, url);
//
//                } catch (Exception e) {
//                    System.out.println("exception discover: " + original);
//
//                }
//            }
//
//
//        } else
//            return result;
//        return result;
//
//    }

    /**
     * whether the transaction was added to the blockchain, find with sign
     * (check from local host)
     */
    @GetMapping("/isTransactionAdd")
    public Boolean isTransactionGet(@RequestParam String sign) throws IOException, NoSuchAlgorithmException, InvalidKeySpecException, SignatureException, NoSuchProviderException, InvalidKeyException {
        if (!BasisController.isIsSaveFile()) {
            System.out.println("saving file: resolve_from_to_block: sub block");
            return false;
        }
        Base base = new Base58();
        return blockService.existsBySign(base.decode(sign));


    }

    @PostMapping("/TransactionAddBase")
    public DtoTransaction TransactionGetBase(@RequestBody SignRequest request) throws IOException, NoSuchAlgorithmException, InvalidKeySpecException, SignatureException, NoSuchProviderException, InvalidKeyException {
        try {
            if (!BasisController.isIsSaveFile()) {
                System.out.println("saving file: resolve_from_to_block: sub block");
                return new DtoTransaction();
            }
            // Удаление всех пробелов из строки Base64
            String sanitizedSign = request.getSign().replaceAll("\\s+", "");
            // Декодирование строки Base64 в байты

            // Преобразование байтов в строку Base58
            String base58Sign =sanitizedSign;
            // Проверка наличия в базе данных по строке Base58
            EntityDtoTransaction entityDtoTransaction = blockService.findBySign(base58Sign);
            if(entityDtoTransaction == null)
                return null;
            return UtilsBlockToEntityBlock.entityToDto(entityDtoTransaction);
        } catch (IllegalArgumentException e) {
            throw new IllegalStateException("Invalid Base64 input", e);
        }
    }

    private final Base58 base58 = new Base58();
    @PostMapping("/isTransactionAddBase64")
    public Boolean isTransactionGetBase64(@RequestBody SignRequest request) throws IOException, NoSuchAlgorithmException, InvalidKeySpecException, SignatureException, NoSuchProviderException, InvalidKeyException {
        try {
            if (!BasisController.isIsSaveFile()) {
                System.out.println("saving file: resolve_from_to_block: sub block");
                return false;
            }
            // Удаление всех пробелов из строки Base64
            String sanitizedSign = request.getSign().replaceAll("\\s+", "");
            // Декодирование строки Base64 в байты
            byte[] decodedBytes = Base64.getDecoder().decode(sanitizedSign);
            // Преобразование байтов в строку Base58
            String base58Sign = base58.encode(decodedBytes);
            // Проверка наличия в базе данных по строке Base58
            if(base58Sign == null)
                return false;
            return blockService.findBySign(base58Sign) != null;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    @PostMapping("/TransactionAddBase64")
    public DtoTransaction TransactionGetBase64(@RequestBody SignRequest request) throws IOException, NoSuchAlgorithmException, InvalidKeySpecException, SignatureException, NoSuchProviderException, InvalidKeyException {
        try {
            if (!BasisController.isIsSaveFile()) {
                System.out.println("saving file: resolve_from_to_block: sub block");
                return new DtoTransaction();
            }
            // Удаление всех пробелов из строки Base64
            String sanitizedSign = request.getSign().replaceAll("\\s+", "");
            // Декодирование строки Base64 в байты
            byte[] decodedBytes = Base64.getDecoder().decode(sanitizedSign);
            // Преобразование байтов в строку Base58
            String base58Sign = base58.encode(decodedBytes);
            // Проверка наличия в базе данных по строке Base58
            EntityDtoTransaction entityDtoTransaction = blockService.findBySign(base58Sign);
            if(entityDtoTransaction == null)
                return null;
            return UtilsBlockToEntityBlock.entityToDto(entityDtoTransaction);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }


    /**find block from hash
     * (from local host)*/
    @GetMapping("/blockHash")
    @ResponseBody
    public Block blockFromHash(@RequestParam String hash) throws IOException {

        if (!BasisController.isIsSaveFile()) {
            System.out.println("saving file: resolve_from_to_block: sub block");
            return null;
        }
//        return Blockchain.hashFromFile(hash, Seting.ORIGINAL_BLOCKCHAIN_FILE);
        Block block = UtilsBlockToEntityBlock.entityBlockToBlock(
                blockService.findByHashBlock(hash)
        );
        return block;
    }

    @GetMapping ("/findBlocksFromSign58")
    public List<Block> findBlocksFromSign58(@RequestBody SignRequest reques){
        try {
            List<EntityBlock> blocks = blockService.findBlocksByTransactionSign(reques.getSign());
            List<Block> blocks1 = UtilsBlockToEntityBlock.entityBlocksToBlocks(blocks);
            return blocks1;
        }catch (Exception e){
            MyLogger.saveLog("findBlocksFromSign58: ", e);
            return new ArrayList<>();
        }

    }

    @GetMapping ("/findBlocksFromSign64")
    public List<Block> findBlocksFromSign64(@RequestBody SignRequest reques){
        try {
            if (!BasisController.isIsSaveFile()) {
                System.out.println("saving file: resolve_from_to_block: sub block");
                return null;
            }
            byte[] bytes = Base64.getDecoder().decode(reques.getSign());
            String sign = base58.encode(bytes);
            List<EntityBlock> blocks = blockService.findBlocksByTransactionSign(sign);
            List<Block> blocks1 = UtilsBlockToEntityBlock.entityBlocksToBlocks(blocks);
            return blocks1;
        }catch (Exception e){
            MyLogger.saveLog("findBlocksFromSign58: ", e);
            return new ArrayList<>();
        }

    }
    /**найти блок по индексу.
     * find a block by index.
     * (from local host)*/
    @GetMapping("/conductorBlock")
    @ResponseBody
    public Block  block(@RequestParam Integer index) throws IOException {

        if (!BasisController.isIsSaveFile()) {
            System.out.println("saving file: resolve_from_to_block: sub block");
            return null;
        }
        if(index < 0 ){
            index = 0;
        }
        if(index > BasisController.getBlockchainSize() -1){
            index = BasisController.getBlockchainSize() - 1;
        }
        return UtilsBlockToEntityBlock.entityBlockToBlock(
                blockService.findBySpecialIndex(index)
        );
    }
    /***
     * находит транзакцию по подписи.
     * finds a transaction by signature.
     * (from local host)
     */

    @GetMapping("/conductorHashTran")
    @ResponseBody
    public DtoTransaction transaction(@RequestParam String hash) throws IOException, NoSuchAlgorithmException, InvalidKeySpecException, SignatureException, NoSuchProviderException, InvalidKeyException {
        if (!BasisController.isIsSaveFile()) {
            System.out.println("saving file: resolve_from_to_block: sub block");
            return null;
        }
        List<EntityDtoTransaction> entityDtoTransactions =
                blockService.findAllDto();
        EntityDtoTransaction entityDtoTransaction = null;
        try {
             entityDtoTransaction = blockService.findBySign(hash);

            if(entityDtoTransaction == null){
                return null;
            }
        }catch (Exception e){
           return null;
        }

        return UtilsBlockToEntityBlock.entityToDto(entityDtoTransaction);
    }
    /**
     * Получить список транзакций для адреса отправителя, от определенного блока до определенного блока
     */
    @GetMapping("/senderTransactions")
    @ResponseBody
    public List<DtoTransaction> senderTransactions(
            @RequestParam String address,
            @RequestParam int from,
            @RequestParam int to
    ) {
        try {

            if (!BasisController.isIsSaveFile()) {
                System.out.println("saving file: resolve_from_to_block: sub block");
                return null;
            }
            if (to - from > 500) {
                to = from + 500;
            }
            return blockService.findBySender(address, from, to);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @GetMapping("/customerTransactions")
    @ResponseBody
    public List<DtoTransaction> customerTransactions(
            @RequestParam String address,
            @RequestParam int from,
            @RequestParam int to
    ) {
        try {
            if (!BasisController.isIsSaveFile()) {
                System.out.println("saving file: resolve_from_to_block: sub block");
                return null;
            }
            if (to - from > 500) {
                to = from + 500;
            }
            return blockService.findByCustomer(address, from, to);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
    /**
     * количество отправленных транзакций от данного адреса
     */
    @GetMapping("/senderCountDto")
    @ResponseBody
    public long countSenderTransaction(
            @RequestParam String address
    ) {
        try {
            if (!BasisController.isIsSaveFile()) {
                System.out.println("saving file: resolve_from_to_block: sub block");
                return -1;
            }
            return blockService.countSenderTransaction(address);

        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }
    }

    /**
     * количество полученных транзакций от данного адреса
     */

    @GetMapping("/customerCountDto")
    @ResponseBody
    public long countCustomerTransaction(
            @RequestParam String address
    ) {
        try {
            if (!BasisController.isIsSaveFile()) {
                System.out.println("saving file: resolve_from_to_block: sub block");
                return -1;
            }
            return blockService.countCustomerTransaction(address);

        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }
    }


    /**
     * Получить все балансы данного адреса
     */
    @GetMapping("/addresses")
    @ResponseBody
    public Map<String, Account> addresses() {
        Map<String, Account> accountMap = new HashMap<>();
        try {
            if (!BasisController.isIsSaveFile()) {
                System.out.println("saving file: resolve_from_to_block: sub block");
                return null;
            }
            accountMap = UtilsAccountToEntityAccount
                    .entityAccountsToMapAccounts(blockService.findAllAccounts());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return accountMap;
    }



}
