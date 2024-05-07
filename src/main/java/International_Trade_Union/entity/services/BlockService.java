package International_Trade_Union.entity.services;

import International_Trade_Union.entity.DtoTransaction.DtoTransaction;
import International_Trade_Union.entity.entities.EntityAccount;
import International_Trade_Union.entity.entities.EntityBlock;
import International_Trade_Union.entity.entities.EntityDtoTransaction;
import International_Trade_Union.entity.repository.EntityAccountRepository;
import International_Trade_Union.entity.repository.EntityBlockRepository;
import International_Trade_Union.entity.repository.EntityDtoTransactionRepository;
import International_Trade_Union.entity.repository.EntityLawsRepository;
import International_Trade_Union.model.Account;
import International_Trade_Union.utils.UtilsBlockToEntityBlock;
import International_Trade_Union.utils.base.Base;
import International_Trade_Union.utils.base.Base58;
import org.hibernate.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
@Scope("singleton")
public class BlockService {
    @PersistenceContext
    EntityManager entityManager;
    @Autowired
    private EntityLawsRepository entityLawsRepository;
//    private static EntityBlockRepository blockService;
//    private static EntityLawsRepository lawService;
//    private static EntityDtoTransactionRepository dtoService;
//
//    private static EntityAccountRepository accountService;


    @Autowired
    private EntityBlockRepository entityBlockRepository;


    @Autowired
    private EntityDtoTransactionRepository dtoTransactionRepository;

    @Autowired
    private EntityAccountRepository entityAccountRepository;




    public  void deletedAll(){
        Session session = entityManager.unwrap(Session.class);
        entityBlockRepository.deleteAll();
        entityAccountRepository.deleteAll();
        entityLawsRepository.deleteAll();
        dtoTransactionRepository.deleteAll();

        session.clear();
    }

    public  EntityLawsRepository getLawService() {
        return entityLawsRepository;
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

    public  void saveBlock(EntityBlock entityBlock) {
        Session session = entityManager.unwrap(Session.class);
        entityBlockRepository.save(entityBlock);
        entityBlockRepository.flush();
        session.clear();
    }


    @Transactional
    public  void deleteEntityBlocksAndRelatedData(Long threshold) {
        Session session = entityManager.unwrap(Session.class);
        session.setJdbcBatchSize(50);
        entityBlockRepository.deleteAllBySpecialIndexGreaterThanEqual(threshold);
        entityBlockRepository.flush();
        session.clear();
    }




    public List<EntityAccount> findByAccountIn(Map<String, Account> map){
        Session session = entityManager.unwrap(Session.class);
        List<String> accounts = map.entrySet().stream().map(t->t.getValue().getAccount()).collect(Collectors.toList());
        List<EntityAccount> entityAccounts = entityAccountRepository.findByAccountIn(accounts);
        session.clear();
        return entityAccounts;

    }
    public List<EntityAccount> findBYAccountString(List<String> accounts){
        Session session = entityManager.unwrap(Session.class);
        List<EntityAccount> entityAccounts = entityAccountRepository.findByAccountIn(accounts);
        session.clear();
        return entityAccounts;
    }



    public  List<EntityAccount> findAllAccounts(){
        Session session = entityManager.unwrap(Session.class);
        List<EntityAccount> entityAccounts = entityAccountRepository.findAll();
        session.clear();
        return entityAccounts;
    }




    public  long sizeBlock(){
        Session session = entityManager.unwrap(Session.class);
        long size = entityBlockRepository.count();
        session.clear();
        return size;
    }
    public  EntityBlock lastBlock(){
        Session session = entityManager.unwrap(Session.class);
        EntityBlock entityBlock = entityBlockRepository.findBySpecialIndex(entityBlockRepository.count()-1);
        session.clear();
        return entityBlock;
    }

    @Transactional
    public void saveAllBLockF(List<EntityBlock> entityBlocks){
        Session session = entityManager.unwrap(Session.class);
        session.setJdbcBatchSize(50);
        entityBlockRepository.saveAll(entityBlocks);
        entityBlockRepository.flush();
        session.clear();
    }
    public  void saveAllBlock(List<EntityBlock> entityBlocks) {
        Session session = entityManager.unwrap(Session.class);
        entityBlockRepository.saveAll(entityBlocks);
        entityBlockRepository.flush();
        session.clear();
    }
    public  void removeAllBlock(List<EntityBlock> entityBlocks){
        Session session = entityManager.unwrap(Session.class);
        entityBlockRepository.deleteAll(entityBlocks);
        entityBlockRepository.flush();
        session.clear();
    }
    public  void saveAccount(EntityAccount entityAccount){
        Session session = entityManager.unwrap(Session.class);
        entityAccountRepository.save(entityAccount);
        entityAccountRepository.flush();
        session.clear();
    }



    @Transactional

    public void saveAccountAllF(List<EntityAccount> entityAccounts){
        Session session = entityManager.unwrap(Session.class);
        session.setJdbcBatchSize(50);
        List<EntityAccount> entityResult = new ArrayList<>();
        for (EntityAccount entityAccount : entityAccounts) {
            if(entityAccountRepository.findByAccount(entityAccount.getAccount()) != null){
                EntityAccount temp = entityAccountRepository.findByAccount(entityAccount.getAccount());
                temp.setDigitalDollarBalance(entityAccount.getDigitalDollarBalance());
                temp.setDigitalStockBalance(entityAccount.getDigitalStockBalance());
                entityResult.add(temp);
            }else {
                entityResult.add(entityAccount);
            }
        }

        entityAccountRepository.saveAll(entityResult);
        entityAccountRepository.flush();
        session.clear();
    }
    public  void saveAccountAll(List<EntityAccount> entityAccounts){
        Session session = entityManager.unwrap(Session.class);

        // Кэш для результатов findByAccount
        Map<String, EntityAccount> cache = new HashMap<>();

        // Списки для пакетного обновления
        List<String> accounts = new ArrayList<>();
        List<Double> digitalDollarBalances = new ArrayList<>();
        List<Double> digitalStockBalances = new ArrayList<>();
        List<Double> digitalStakingBalances = new ArrayList<>();

        for (EntityAccount entityAccount : entityAccounts) {
            EntityAccount cachedAccount = cache.get(entityAccount.getAccount());

            if (cachedAccount != null) {
                // Обновить существующую запись в кэше
                cachedAccount.setDigitalDollarBalance(entityAccount.getDigitalDollarBalance());
                cachedAccount.setDigitalStockBalance(entityAccount.getDigitalStockBalance());
            } else {
                // Добавить новую запись для пакетного обновления
                accounts.add(entityAccount.getAccount());
                digitalDollarBalances.add(entityAccount.getDigitalDollarBalance());
                digitalStockBalances.add(entityAccount.getDigitalStockBalance());
                digitalStakingBalances.add(entityAccount.getDigitalStakingBalance());
            }

            // Сохранить в кэш для потенциального обновления
            cache.put(entityAccount.getAccount(), entityAccount);

        }

        // Пакетное обновление
        entityAccountRepository.batchInsert(accounts, digitalDollarBalances, digitalStockBalances, digitalStakingBalances);

        // Обновить кэш с новыми данными (необязательно, зависит от логики)
        for (EntityAccount entityAccount : entityAccounts) {
            cache.put(entityAccount.getAccount(), entityAccount);
        }
        session.clear();

    }

    public  EntityBlock findByHashBlock(String hashBlock){
        return entityBlockRepository.findByHashBlock(hashBlock);
    }

    public  EntityDtoTransaction findBySign(String sign){
        Session session = entityManager.unwrap(Session.class);
        EntityDtoTransaction entityDtoTransaction = dtoTransactionRepository.findBySign(sign);
        session.clear();
        return entityDtoTransaction;
    }

    public boolean existsBySign(byte[] sign){
        Session session = entityManager.unwrap(Session.class);
        Base base = new Base58();
        boolean result = dtoTransactionRepository.existsBySign(base.encode(sign));
        session.clear();
        return result;
    }
    @javax.transaction.Transactional
    public  List<EntityDtoTransaction> findAllDto(){
        Session session = entityManager.unwrap(Session.class);
        List<EntityDtoTransaction> dtoTransactions = dtoTransactionRepository.findAll();
        session.clear();
        return dtoTransactions;
    }

    @Transactional
    public  EntityDtoTransaction findByIdDto(long id){
        return dtoTransactionRepository.findById(id);
    }

    @Transactional
    public  EntityBlock findById(long id){
        return entityBlockRepository.findById(id);
    }

    @Transactional
    public  EntityBlock findBySpecialIndex(long specialIndex){
        Session session = entityManager.unwrap(Session.class);
        EntityBlock entityBlock = entityBlockRepository.findBySpecialIndex(specialIndex);
        session.clear();
        return entityBlock;
    }

    public  List<EntityBlock> findAllByIdBetween(long from, long to){
        return entityBlockRepository.findAllByIdBetween(from, to);
    }

    @Transactional
    public  List<EntityBlock> findBySpecialIndexBetween(long from, long to){
        Session session = entityManager.unwrap(Session.class);
        List<EntityBlock> entityBlocks = entityBlockRepository.findBySpecialIndexBetween(from, to);
        session.clear();
        return entityBlocks;

    }

    public  List<EntityBlock> findAll() {
        return entityBlockRepository.findAll();
    }

    public  EntityAccount entityAccount(String account){
        return entityAccountRepository.findByAccount(account);
    }

    public  long countBlock() {
        return entityBlockRepository.count();
    }

    public  long countAccount() {
        return entityAccountRepository.count();
    }

    public  boolean isEmpty() {
        Session session = entityManager.unwrap(Session.class);
        boolean exists = entityBlockRepository.existsById(1L);
        session.clear();
        return exists;
    }


    public  List<DtoTransaction> findBySender(String sender, int from, int to) throws IOException {
        Session session = entityManager.unwrap(Session.class);

        Pageable firstPageWithTenElements = (Pageable) PageRequest.of(from, to);
        List<EntityDtoTransaction> list =
                 dtoTransactionRepository.findBySender(sender, firstPageWithTenElements)
                        .getContent();
        List<DtoTransaction> dtoTransactions =
                UtilsBlockToEntityBlock.entityDtoTransactionToDtoTransaction(list);
        session.clear();
        return dtoTransactions;
    }

    public  List<DtoTransaction> findByCustomer(String customer, int from, int to) throws IOException {

        Session session = entityManager.unwrap(Session.class);
        Pageable firstPageWithTenElements = (Pageable) PageRequest.of(from, to);
        List<EntityDtoTransaction> list =
                 dtoTransactionRepository.findByCustomer(customer,firstPageWithTenElements)
                        .getContent();
        List<DtoTransaction> dtoTransactions =
                UtilsBlockToEntityBlock.entityDtoTransactionToDtoTransaction(list);
        session.clear();
        return dtoTransactions;
    }

    public  long countSenderTransaction(String sender){
        Session session = entityManager.unwrap(Session.class);
        long size = dtoTransactionRepository.countBySender(sender);
        session.clear();
        return size;

    }

    public  long countCustomerTransaction(String customer){
        Session session = entityManager.unwrap(Session.class);
        long size = dtoTransactionRepository.countByCustomer(customer);
        session.clear();
        return size;

    }

}
