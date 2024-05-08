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



    @Autowired
    private EntityBlockRepository entityBlockRepository;


    @Autowired
    private EntityDtoTransactionRepository dtoTransactionRepository;

    @Autowired
    private EntityAccountRepository entityAccountRepository;


    @Transactional
    public void deletedAll() {
        try(Session session = entityManager.unwrap(Session.class)){
            entityBlockRepository.deleteAll();
            entityAccountRepository.deleteAll();
            entityLawsRepository.deleteAll();
            dtoTransactionRepository.deleteAll();
        }catch (Exception e){
            e.printStackTrace();
        }

    }

    public EntityLawsRepository getLawService() {
        return entityLawsRepository;
    }

    @Transactional
    public EntityBlockRepository getEntityBlockRepository() {
        return entityBlockRepository;
    }

    public EntityLawsRepository getEntityLawsRepository() {
        return entityLawsRepository;
    }

    @Transactional
    public EntityAccountRepository getEntityAccountRepository() {
        return entityAccountRepository;
    }

    public void saveBlock(EntityBlock entityBlock) {
        try(Session session = entityManager.unwrap(Session.class)){
            entityBlockRepository.save(entityBlock);
            entityBlockRepository.flush();
        }catch (Exception e){
            e.printStackTrace();
        }


    }


    @Transactional
    public void deleteEntityBlocksAndRelatedData(Long threshold) {
        try(Session session = entityManager.unwrap(Session.class)){
            session.setJdbcBatchSize(50);
            entityBlockRepository.deleteAllBySpecialIndexGreaterThanEqual(threshold);
            entityBlockRepository.flush();
        }catch (Exception e){
            e.printStackTrace();
        }
    }


    @Transactional
    public List<EntityAccount> findByAccountIn(Map<String, Account> map) {
        List<EntityAccount> entityAccounts = new ArrayList<>();
        try(Session session = entityManager.unwrap(Session.class)){
            List<String> accounts = map.entrySet().stream().map(t -> t.getValue().getAccount()).collect(Collectors.toList());
            entityAccounts = entityAccountRepository.findByAccountIn(accounts);

        }catch (Exception e){
            e.printStackTrace();
        }

        return entityAccounts;

    }

    @Transactional
    public EntityAccount findByAccount(String account) {
        EntityAccount entityAccounts = null;
        try(Session session = entityManager.unwrap(Session.class)){
            entityAccounts = entityAccountRepository.findByAccount(account);
        }catch (Exception e){
            e.printStackTrace();
        }
        return entityAccounts;
    }

    @Transactional
    public List<EntityAccount> findBYAccountString(List<String> accounts) {
        List<EntityAccount> entityAccounts = new ArrayList<>();
        try(Session session = entityManager.unwrap(Session.class)){
            entityAccounts  = entityAccountRepository.findByAccountIn(accounts);

        }catch (Exception e){
            e.printStackTrace();
        }

        return entityAccounts;
    }

    @Transactional
    public List<EntityAccount> findByDtoAccounts(List<DtoTransaction> transactions) {
        List<String> accounts = new ArrayList<>();
        for (DtoTransaction transaction : transactions) {
            accounts.add(transaction.getSender());
            accounts.add(transaction.getCustomer());
        }
        List<EntityAccount> entityAccounts = new ArrayList<>();
        try(Session session = entityManager.unwrap(Session.class)){
            entityAccounts = entityAccountRepository.findByAccountIn(accounts);
        }catch (Exception e){
            e.printStackTrace();
        }

        return entityAccounts;
    }


    @Transactional
    public List<EntityAccount> findAllAccounts() {
        List<EntityAccount> entityAccounts = new ArrayList<>();
        try(Session session = entityManager.unwrap(Session.class)){
           entityAccounts = entityAccountRepository.findAll();
        }catch (Exception e){
            e.printStackTrace();
        }

        return entityAccounts;
    }


    @Transactional
    public long sizeBlock() {
        long size = 0;
        try(Session session = entityManager.unwrap(Session.class)){
             size = entityBlockRepository.count();
        }catch (Exception e){
            e.printStackTrace();
        }

        return size;
    }

    @Transactional
    public EntityBlock lastBlock() {
        EntityBlock entityBlock = null;
        try( Session session = entityManager.unwrap(Session.class)){
            entityBlock = entityBlockRepository.findBySpecialIndex(entityBlockRepository.count() - 1);

        }catch (Exception e){
            e.printStackTrace();
        }
        return entityBlock;
    }

    @Transactional
    public void saveAllBLockF(List<EntityBlock> entityBlocks) {
        try(Session session = entityManager.unwrap(Session.class)){
            session.setJdbcBatchSize(50);
            entityBlockRepository.saveAll(entityBlocks);
            entityBlockRepository.flush();
        }catch (Exception e){
            e.printStackTrace();
        }

    }

    @Transactional
    public void saveAllBlock(List<EntityBlock> entityBlocks) {
        try(Session session = entityManager.unwrap(Session.class)){
            entityBlockRepository.saveAll(entityBlocks);
            entityBlockRepository.flush();
        }catch (Exception e){
            e.printStackTrace();
        }

    }

    public void removeAllBlock(List<EntityBlock> entityBlocks) {
        try(Session session = entityManager.unwrap(Session.class)){
            entityBlockRepository.deleteAll(entityBlocks);
            entityBlockRepository.flush();
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public void saveAccount(EntityAccount entityAccount) {
        try(Session session = entityManager.unwrap(Session.class)){
            entityAccountRepository.save(entityAccount);
            entityAccountRepository.flush();
        }catch (Exception e){
            e.printStackTrace();
        }

    }


    @Transactional

    public void saveAccountAllF(List<EntityAccount> entityAccounts) {
        try(Session session = entityManager.unwrap(Session.class)){
            session.setJdbcBatchSize(50);
            List<EntityAccount> entityResult = new ArrayList<>();
            for (EntityAccount entityAccount : entityAccounts) {
                if (entityAccountRepository.findByAccount(entityAccount.getAccount()) != null) {
                    EntityAccount temp = entityAccountRepository.findByAccount(entityAccount.getAccount());
                    temp.setDigitalDollarBalance(entityAccount.getDigitalDollarBalance());
                    temp.setDigitalStockBalance(entityAccount.getDigitalStockBalance());
                    entityResult.add(temp);
                } else {
                    entityResult.add(entityAccount);
                }
            }

            entityAccountRepository.saveAll(entityResult);
            entityAccountRepository.flush();
        }catch (Exception e){
            e.printStackTrace();
        }


    }

    @Transactional
    public void saveAccountAll(List<EntityAccount> entityAccounts) {
        try(Session session = entityManager.unwrap(Session.class)){
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
        }catch (Exception e){
            e.printStackTrace();
        }

    }

    public EntityBlock findByHashBlock(String hashBlock) {
        return entityBlockRepository.findByHashBlock(hashBlock);
    }

    @Transactional
    public EntityDtoTransaction findBySign(String sign) {
        EntityDtoTransaction entityDtoTransaction = null;
        try( Session session = entityManager.unwrap(Session.class)){
            entityDtoTransaction = dtoTransactionRepository.findBySign(sign);

        }catch (Exception e){
            e.printStackTrace();
        }
        return entityDtoTransaction;
    }

    @Transactional
    public boolean existsBySign(byte[] sign) {
        boolean result = false;
        try(Session session = entityManager.unwrap(Session.class)){
            Base base = new Base58();
            result = dtoTransactionRepository.existsBySign(base.encode(sign));
        }

        return result;
    }

    @Transactional
    public List<EntityDtoTransaction> findAllDto() {
        List<EntityDtoTransaction> dtoTransactions = new ArrayList<>();
        try(Session session = entityManager.unwrap(Session.class)){
           dtoTransactions = dtoTransactionRepository.findAll();
        }

        return dtoTransactions;
    }

    @Transactional
    public EntityDtoTransaction findByIdDto(long id) {
        return dtoTransactionRepository.findById(id);
    }

    @Transactional
    public EntityBlock findById(long id) {
        return entityBlockRepository.findById(id);
    }

    @Transactional
    public EntityBlock findBySpecialIndex(long specialIndex) {
        EntityBlock entityBlock = null;
        try(Session session = entityManager.unwrap(Session.class)){
            entityBlock = entityBlockRepository.findBySpecialIndex(specialIndex);
        }

        return entityBlock;
    }

    public List<EntityBlock> findAllByIdBetween(long from, long to) {
        return entityBlockRepository.findAllByIdBetween(from, to);
    }

    @Transactional
    public List<EntityBlock> findBySpecialIndexBetween(long from, long to) {
        List<EntityBlock> entityBlocks = null;
        try(Session session = entityManager.unwrap(Session.class)){
            entityBlocks = entityBlockRepository.findBySpecialIndexBetween(from, to);
        }
        return entityBlocks;

    }

    public List<EntityBlock> findAll() {
        return entityBlockRepository.findAll();
    }

    public EntityAccount entityAccount(String account) {
        return entityAccountRepository.findByAccount(account);
    }

    public long countBlock() {
        return entityBlockRepository.count();
    }

    @Transactional
    public long countAccount() {
        return entityAccountRepository.count();
    }

    @Transactional
    public boolean isEmpty() {
        boolean exists = false;
        try(Session session = entityManager.unwrap(Session.class)){
            exists = entityBlockRepository.existsById(1L);
        }

        return exists;
    }


    @Transactional
    public List<DtoTransaction> findBySender(String sender, int from, int to) throws IOException {
        List<DtoTransaction> dtoTransactions = null;
        try(Session session = entityManager.unwrap(Session.class)){
            Pageable firstPageWithTenElements = (Pageable) PageRequest.of(from, to);
            List<EntityDtoTransaction> list =
                    dtoTransactionRepository.findBySender(sender, firstPageWithTenElements)
                            .getContent();
            dtoTransactions =
                    UtilsBlockToEntityBlock.entityDtoTransactionToDtoTransaction(list);
        }catch (Exception e){
            e.printStackTrace();
        }
        return dtoTransactions;
    }

    @Transactional
    public List<DtoTransaction> findByCustomer(String customer, int from, int to) throws IOException {
        List<DtoTransaction> dtoTransactions = null;
        try(Session session = entityManager.unwrap(Session.class)){
            Pageable firstPageWithTenElements = (Pageable) PageRequest.of(from, to);
            List<EntityDtoTransaction> list =
                    dtoTransactionRepository.findByCustomer(customer, firstPageWithTenElements)
                            .getContent();
            dtoTransactions =
                    UtilsBlockToEntityBlock.entityDtoTransactionToDtoTransaction(list);
        }catch (Exception e){
            e.printStackTrace();
        }


        return dtoTransactions;
    }

    @Transactional
    public long countSenderTransaction(String sender) {
        long size = 0;
        try(Session session = entityManager.unwrap(Session.class)){
            size = dtoTransactionRepository.countBySender(sender);
        }catch (Exception e){
            e.printStackTrace();
        }

        return size;

    }

    @Transactional
    public long countCustomerTransaction(String customer) {
        long size = 0;
        try(Session session = entityManager.unwrap(Session.class)){
            size = dtoTransactionRepository.countByCustomer(customer);
        }catch (Exception e){
            e.printStackTrace();
        }

        return size;

    }

}
