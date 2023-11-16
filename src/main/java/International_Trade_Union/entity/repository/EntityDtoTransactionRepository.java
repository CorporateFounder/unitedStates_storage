package International_Trade_Union.entity.repository;

import International_Trade_Union.entity.entities.EntityDtoTransaction;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

import org.springframework.data.domain.Pageable;
import java.util.List;

@Repository
public interface EntityDtoTransactionRepository extends PagingAndSortingRepository<EntityDtoTransaction, Long> {
    EntityDtoTransaction findById(long id);
    List<EntityDtoTransaction> findAll();
    EntityDtoTransaction findBySign(byte[] sign);
    Page<EntityDtoTransaction> findBySender(String sender, Pageable pageable);
    Page<EntityDtoTransaction> findByCustomer(String customer, Pageable pageable);
    long countBySender(String sender);
    long countByCustomer(String customer);

}
