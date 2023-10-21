package International_Trade_Union.entity.services;

import International_Trade_Union.entity.DtoTransaction.DtoTransaction;
import International_Trade_Union.entity.entities.EntityAccount;
import International_Trade_Union.entity.entities.EntityBlock;
import International_Trade_Union.entity.entities.EntityDtoTransaction;
import International_Trade_Union.entity.repository.EntityAccountRepository;
import International_Trade_Union.entity.repository.EntityBlockRepository;
import International_Trade_Union.entity.repository.EntityDtoTransactionRepository;
import International_Trade_Union.entity.repository.EntityLawsRepository;
import International_Trade_Union.utils.UtilsBlockToEntityBlock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Component
public class BlockService {
    private static EntityBlockRepository blockService;
    private static EntityLawsRepository lawService;
    private static EntityDtoTransactionRepository dtoService;

    private static EntityAccountRepository accountService;


    @Autowired
    private EntityBlockRepository entityBlockRepository;

    @Autowired
    private EntityLawsRepository entityLawsRepository;
    @Autowired
    private EntityDtoTransactionRepository dtoTransactionRepository;

    @Autowired
    private EntityAccountRepository entityAccountRepository;


    @PostConstruct
    public void init() {
        blockService = entityBlockRepository;
        lawService = entityLawsRepository;
        dtoService = dtoTransactionRepository;
        accountService = entityAccountRepository;

    }

    public static void deletedAll(){
        blockService.deleteAll();
        accountService.deleteAll();
        lawService.deleteAll();
        dtoService.deleteAll();

        blockService.flush();
        accountService.flush();
        lawService.flush();
        dtoService.findAll();


    }

    public static EntityLawsRepository getLawService() {
        return lawService;
    }

    public EntityBlockRepository getEntityBlockRepository() {
        return entityBlockRepository;
    }

    public EntityLawsRepository getEntityLawsRepository() {
        return entityLawsRepository;
    }

    public EntityAccountRepository getEntityAccountRepository() {
        return entityAccountRepository;
    }

    public static void saveBlock(EntityBlock entityBlock) {

        blockService.save(entityBlock);
        blockService.flush();

    }

    public static void saveAllBlock(List<EntityBlock> entityBlocks) {
        blockService.saveAll(entityBlocks);
        blockService.flush();
    }

    public static void saveAccount(EntityAccount entityAccount){

        accountService.save(entityAccount);
        accountService.flush();
    }
    public static void saveAccountAll(List<EntityAccount> entityAccounts){

        List<EntityAccount> entityResult = new ArrayList<>();
        for (EntityAccount entityAccount : entityAccounts) {
            if(accountService.findByAccount(entityAccount.getAccount()) != null){
                EntityAccount temp = accountService.findByAccount(entityAccount.getAccount());
                temp.setDigitalDollarBalance(entityAccount.getDigitalDollarBalance());
                temp.setDigitalStockBalance(entityAccount.getDigitalStockBalance());
                entityResult.add(temp);
            }else {
                entityResult.add(entityAccount);
            }
        }
        accountService.saveAll(entityResult);
        accountService.flush();

    }
    public static EntityBlock findById(long id){
        return blockService.findById(id);
    }
    public static EntityBlock findBySpecialIndex(long specialIndex){
        return blockService.findBySpecialIndex(specialIndex);
    }

    public static List<EntityBlock> findAllByIdBetween(long from, long to){
        return blockService.findAllByIdBetween(from, to);
    }
    public static List<EntityBlock> findBySpecialIndexBetween(long from, long to){
        return blockService.findBySpecialIndexBetween(from, to);
    }

    public static List<EntityBlock> findAll() {
        return blockService.findAll();
    }

    public static EntityAccount entityAccount(String account){
        return accountService.findByAccount(account);
    }

    public static long countBlock() {
        return blockService.count();
    }

    public static long countAccount() {
        return accountService.count();
    }

    public static boolean isEmpty() {
        boolean exists = blockService.existsById(1L);
        return exists;
    }

    public static DtoTransaction findByIdDto(long id) throws IOException {

        Optional<EntityDtoTransaction> dtoTransaction = dtoService.findById(id);
        if(!dtoTransaction.isEmpty()){
            DtoTransaction dtoTransaction1 =
                    UtilsBlockToEntityBlock.entityDtoToDto(dtoTransaction.get());
            return dtoTransaction1;
        }else {
            return null;
        }

    }

    public static DtoTransaction findBySign(String hash) throws IOException {

        EntityDtoTransaction entityDtoTransaction
                = dtoService.findByToSign(hash);


        if(entityDtoTransaction == null){
            return null;
        }
        DtoTransaction dtoTransaction
                = UtilsBlockToEntityBlock.entityDtoToDto(entityDtoTransaction);

        return dtoTransaction;
    }


}
