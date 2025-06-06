package International_Trade_Union.entity.repository;

import International_Trade_Union.entity.entities.EntityAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface EntityAccountRepository extends JpaRepository<EntityAccount, Long> {
    EntityAccount findByAccount(String account);
    List<EntityAccount> findByAccountIn(List<String> accounts);

    // Добавьте этот метод для пакетной записи списка объектов EntityAccount
    @Modifying
    @Query(value = "insert into EntityAccount (account, digitalDollarBalance, digitalStockBalance, digitalStakingBalance) values (:account, :digitalDollarBalance, :digitalStockBalance, :digitalStakingBalance)", nativeQuery = true)
    void batchInsert(@Param("account") List<String> accounts,
                     @Param("digitalDollarBalance") List<BigDecimal> digitalDollarBalances,
                     @Param("digitalStockBalance") List<BigDecimal> digitalStockBalances,
                     @Param("digitalStakingBalance") List<BigDecimal> digitalStakingBalances);

    @Query("SELECT SUM(e.digitalDollarBalance) FROM EntityAccount e")
    Double getTotalDigitalDollarBalance();

    @Query("SELECT SUM(e.digitalStakingBalance) FROM EntityAccount e")
    Double getTotalDigitalStakingBalance();
    List<EntityAccount> findAllByAccountIn(List<String> accounts);
}
