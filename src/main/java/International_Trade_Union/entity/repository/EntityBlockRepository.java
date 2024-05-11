package International_Trade_Union.entity.repository;

import International_Trade_Union.entity.entities.EntityBlock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;


@Repository
public interface EntityBlockRepository extends JpaRepository<EntityBlock, Long> {

        List<EntityBlock> findAllByIdBetween(long from, long to);
        List<EntityBlock> findBySpecialIndexBetween(long from, long to);
        EntityBlock findBySpecialIndex(long specialIndex);
        EntityBlock findById(long id);

        EntityBlock findByHashBlock(String hashBlock);



        @Modifying
        void deleteAllBySpecialIndexGreaterThanEqual(Long threshold);

        @Modifying
        @Query("DELETE FROM EntityBlock e WHERE e.specialIndex >= :threshold")
        void deleteInBatchBySpecialIndexGreaterThanEqual(@Param("threshold") Long threshold);




}
