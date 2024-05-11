package International_Trade_Union.entity.services;

import International_Trade_Union.entity.DtoTransaction.DtoTransaction;
import International_Trade_Union.entity.entities.EntityAccount;
import International_Trade_Union.entity.entities.EntityBlock;
import International_Trade_Union.entity.entities.EntityDtoTransaction;
import International_Trade_Union.entity.repository.EntityAccountRepository;
import International_Trade_Union.entity.repository.EntityBlockRepository;
import International_Trade_Union.entity.repository.EntityDtoTransactionRepository;
import International_Trade_Union.entity.repository.EntityLawsRepository;
import International_Trade_Union.logger.MyLogger;
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
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
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
    public void deletedAll() throws IOException {
        try(Session session = entityManager.unwrap(Session.class)){
            entityBlockRepository.deleteAll();
            entityAccountRepository.deleteAll();
            entityLawsRepository.deleteAll();
            dtoTransactionRepository.deleteAll();
        }catch (Exception e){
            e.printStackTrace();
            throw new IOException("deletedAll: error: save: ");

        }


    }

    public EntityLawsRepository getLawService() {
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

    public void saveBlock(EntityBlock entityBlock) throws IOException {
        try(Session session = entityManager.unwrap(Session.class)){
            entityBlockRepository.save(entityBlock);
            entityBlockRepository.flush();
        }catch (Exception e){
            e.printStackTrace();
            throw new IOException("saveBlock: error: save: ");

        }


    }


    @Transactional
    public void deleteEntityBlocksAndRelatedData(Long threshold) throws IOException {
        try(Session session = entityManager.unwrap(Session.class)){
            session.setJdbcBatchSize(50);
            entityBlockRepository.deleteAllBySpecialIndexGreaterThanEqual(threshold);
            entityBlockRepository.flush();
        }catch (Exception e){
            e.printStackTrace();
            throw new IOException("deleteEntityBlocksAndRelatedData: error: save: ");

        }
    }



    public List<EntityAccount> findByAccountIn(Map<String, Account> map) throws IOException {
        List<EntityAccount> entityAccounts = new ArrayList<>();
        try(Session session = entityManager.unwrap(Session.class)){
            List<String> accounts = map.entrySet().stream().map(t -> t.getValue().getAccount()).collect(Collectors.toList());
            entityAccounts = entityAccountRepository.findByAccountIn(accounts);

        }catch (Exception e){
            e.printStackTrace();
            throw new IOException("findByAccountIn: error: save: ");

        }

        return entityAccounts;

    }


    public EntityAccount findByAccount(String account) throws IOException {
        EntityAccount entityAccounts = null;
        try(Session session = entityManager.unwrap(Session.class)){
            entityAccounts = entityAccountRepository.findByAccount(account);
        }catch (Exception e){
            e.printStackTrace();
            throw new IOException("findByAccount: error: save: ");

        }
        return entityAccounts;
    }

    public List<EntityAccount> findBYAccountString(List<String> accounts) throws IOException {
        List<EntityAccount> entityAccounts = new ArrayList<>();
        try(Session session = entityManager.unwrap(Session.class)){
            entityAccounts  = entityAccountRepository.findByAccountIn(accounts);

        }catch (Exception e){
            e.printStackTrace();
            throw new IOException("findBYAccountString: error: save: ");

        }

        return entityAccounts;
    }


    public List<EntityAccount> findByDtoAccounts(List<DtoTransaction> transactions) throws IOException {
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
            throw new IOException("findByDtoAccounts: error: save: ");

        }

        return entityAccounts;
    }



    public List<EntityAccount> findAllAccounts() throws IOException {
        List<EntityAccount> entityAccounts = new ArrayList<>();
        try(Session session = entityManager.unwrap(Session.class)){
           entityAccounts = entityAccountRepository.findAll();
        }catch (Exception e){
            e.printStackTrace();
            throw new IOException("findAllAccounts: error: save: ");

        }

        return entityAccounts;
    }



    public long sizeBlock() throws IOException {
        long size = 0;
        try(Session session = entityManager.unwrap(Session.class)){
             size = entityBlockRepository.count();
        }catch (Exception e){
            e.printStackTrace();
            throw new IOException("sizeBlock: error: save: ");
        }

        return size;
    }


    public EntityBlock lastBlock() throws IOException {
        EntityBlock entityBlock = null;
        try( Session session = entityManager.unwrap(Session.class)){
            entityBlock = entityBlockRepository.findBySpecialIndex(entityBlockRepository.count() - 1);

        }catch (Exception e){
            e.printStackTrace();
            throw new IOException("lastBlock: error: save: ");
        }
        return entityBlock;
    }

    @Transactional
    public void saveAllBLockF(List<EntityBlock> entityBlocks) throws IOException {
        try (Session session = entityManager.unwrap(Session.class)) {
            session.setJdbcBatchSize(50);
            for (int i = 0; i < entityBlocks.size(); i++) {
                session.save(entityBlocks.get(i));
                if (i % 50 == 0) { // Flush a batch of inserts and release memory.
                    session.flush();
                    session.clear();
                }
            }
            session.flush(); // Ensure final batch is flushed.
            session.clear();
        } catch (Exception e) {
            MyLogger.saveLog("saveAllBlockF: error: save: ", e);
            throw new RuntimeException("saveAllBlockF: error: save: ", e);
        }

    }

    @Transactional
    public void saveAllBlock(List<EntityBlock> entityBlocks) {
        try (Session session = entityManager.unwrap(Session.class)) {
            for (int i = 0; i < entityBlocks.size(); i++) {
                session.save(entityBlocks.get(i));
                if ((i + 1) % 50 == 0) { // Flush and clear every 50 entities to manage memory.
                    session.flush();
                    session.clear();
                }
            }
            session.flush(); // Ensure any remaining entities are flushed.
            session.clear();
        } catch (Exception e) {
            MyLogger.saveLog("saveAllBlock: error: save: ", e);
            throw new RuntimeException("saveAllBlock: error: save: ", e);
        }
    }

    public void removeAllBlock(List<EntityBlock> entityBlocks) throws IOException {
        try(Session session = entityManager.unwrap(Session.class)){
            entityBlockRepository.deleteAll(entityBlocks);
            entityBlockRepository.flush();
        }catch (Exception e){
            e.printStackTrace();
            throw new IOException("removeAllBlock: error: save: ");

        }
    }

    public void saveAccount(EntityAccount entityAccount) throws IOException {
        try(Session session = entityManager.unwrap(Session.class)){
            entityAccountRepository.save(entityAccount);
            entityAccountRepository.flush();
        }catch (Exception e){
            e.printStackTrace();
            throw new IOException("saveAccount: error: save: ");

        }

    }


    @Transactional
    public void saveAccountAllF(List<EntityAccount> entityAccounts) {
        Map<String, EntityAccount> accountMap = entityAccounts.stream()
                .collect(Collectors.toMap(EntityAccount::getAccount, Function.identity()));

        try (Session session = entityManager.unwrap(Session.class)) {
            session.setJdbcBatchSize(50);
            List<EntityAccount> entityResult = new ArrayList<>();

            for (EntityAccount entityAccount : entityAccounts) {
                EntityAccount existingAccount = accountMap.get(entityAccount.getAccount());
                if (existingAccount != null) {
                    existingAccount.setDigitalDollarBalance(entityAccount.getDigitalDollarBalance());
                    existingAccount.setDigitalStockBalance(entityAccount.getDigitalStockBalance());
                    entityResult.add(existingAccount);
                } else {
                    entityResult.add(entityAccount);
                }
            }

            for (int i = 0; i < entityResult.size(); i++) {
                session.saveOrUpdate(entityResult.get(i));
                if ((i + 1) % 50 == 0) { // Flush and clear every 50 entities to manage memory.
                    session.flush();
                    session.clear();
                }
            }
            session.flush(); // Ensure any remaining entities are flushed.
            session.clear();
        } catch (Exception e) {
            MyLogger.saveLog("saveAccountAllF: error: save: ", e);
            throw new RuntimeException("saveAccountAllF: error: save: ", e);
        }
    }
    @Transactional
    public void saveAccountAll(List<EntityAccount> entityAccounts) {
        // Получение существующих аккаунтов одним запросом
        List<String> accountNumbers = entityAccounts.stream()
                .map(EntityAccount::getAccount)
                .collect(Collectors.toList());
        List<EntityAccount> existingAccounts = entityAccountRepository.findByAccountIn(accountNumbers);

        // Кэш для существующих аккаунтов
        Map<String, EntityAccount> cache = existingAccounts.stream()
                .collect(Collectors.toMap(EntityAccount::getAccount, Function.identity()));

        try (Session session = entityManager.unwrap(Session.class)) {
            for (EntityAccount entityAccount : entityAccounts) {
                EntityAccount cachedAccount = cache.get(entityAccount.getAccount());

                if (cachedAccount != null) {
                    // Обновить существующую запись
                    cachedAccount.setDigitalDollarBalance(entityAccount.getDigitalDollarBalance());
                    cachedAccount.setDigitalStockBalance(entityAccount.getDigitalStockBalance());
                    cachedAccount.setDigitalStakingBalance(entityAccount.getDigitalStakingBalance());
                    session.update(cachedAccount);
                } else {
                    // Сохранить новую запись
                    session.save(entityAccount);
                }

                // Пакетное обновление и очистка каждые 50 записей
                if ((cache.size() + 1) % 50 == 0) {
                    session.flush();
                    session.clear();
                }
            }

            // Завершающее пакетное обновление и очистка
            session.flush();
            session.clear();
        } catch (Exception e) {
            MyLogger.saveLog("saveAccountAll: error: save: ", e);
            throw new RuntimeException("saveAccountAll: error: save: ", e);
        }
    }


    public EntityBlock findByHashBlock(String hashBlock) {
        return entityBlockRepository.findByHashBlock(hashBlock);
    }


    public EntityDtoTransaction findBySign(String sign) throws IOException {
        EntityDtoTransaction entityDtoTransaction = null;
        try( Session session = entityManager.unwrap(Session.class)){
            entityDtoTransaction = dtoTransactionRepository.findBySign(sign);

        }catch (Exception e){
            e.printStackTrace();
            throw new IOException("findBySign: error: save: ");

        }
        return entityDtoTransaction;
    }


    public boolean existsBySign(byte[] sign) throws IOException {
        boolean result = false;
        try(Session session = entityManager.unwrap(Session.class)){
            Base base = new Base58();
            result = dtoTransactionRepository.existsBySign(base.encode(sign));
        }catch (Exception e){
            e.printStackTrace();
            throw new IOException("existsBySign: error: save: ");

        }

        return result;
    }


    public List<EntityDtoTransaction> findAllDto() throws IOException {
        List<EntityDtoTransaction> dtoTransactions = new ArrayList<>();
        try(Session session = entityManager.unwrap(Session.class)){
           dtoTransactions = dtoTransactionRepository.findAll();
        }catch (Exception e){
            e.printStackTrace();
            throw new IOException("findAllDto: error: save: ");

        }
        return dtoTransactions;
    }


    public EntityDtoTransaction findByIdDto(long id) {
        return dtoTransactionRepository.findById(id);
    }


    public EntityBlock findById(long id) {
        return entityBlockRepository.findById(id);
    }


    public EntityBlock findBySpecialIndex(long specialIndex) throws IOException {
        EntityBlock entityBlock = null;
        try(Session session = entityManager.unwrap(Session.class)){
            entityBlock = entityBlockRepository.findBySpecialIndex(specialIndex);
        }catch (Exception e){
            e.printStackTrace();
            throw new IOException("findBySpecialIndex: error: save: ");

        }

        return entityBlock;
    }

    public List<EntityBlock> findAllByIdBetween(long from, long to) {
        return entityBlockRepository.findAllByIdBetween(from, to);
    }


    public List<EntityBlock> findBySpecialIndexBetween(long from, long to) throws IOException {
        List<EntityBlock> entityBlocks = null;
        try(Session session = entityManager.unwrap(Session.class)){
            entityBlocks = entityBlockRepository.findBySpecialIndexBetween(from, to);
        }catch (Exception e){
            e.printStackTrace();
            throw new IOException("findBySpecialIndexBetween: error: save: ");

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


    public long countAccount() {
        return entityAccountRepository.count();
    }


    public boolean isEmpty() throws IOException {
        boolean exists = false;
        try(Session session = entityManager.unwrap(Session.class)){
            exists = entityBlockRepository.existsById(1L);
        }catch (Exception e){
            e.printStackTrace();
            throw new IOException("isEmpty: error: save: ");

        }

        return exists;
    }



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
            throw new IOException("findBySender: error: save: ");

        }
        return dtoTransactions;
    }


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
            throw new IOException("findByCustomer: error: save: ");

        }


        return dtoTransactions;
    }


    public long countSenderTransaction(String sender) throws IOException {
        long size = 0;
        try(Session session = entityManager.unwrap(Session.class)){
            size = dtoTransactionRepository.countBySender(sender);
        }catch (Exception e){
            e.printStackTrace();
            throw new IOException("countSenderTransaction: error: save: ");

        }

        return size;

    }


    public long countCustomerTransaction(String customer) throws IOException {
        long size = 0;
        try(Session session = entityManager.unwrap(Session.class)){
            size = dtoTransactionRepository.countByCustomer(customer);
        }catch (Exception e){
            e.printStackTrace();
            throw new IOException("countCustomerTransaction: error: save: ");

        }

        return size;

    }

}
