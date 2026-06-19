package International_Trade_Union.protocol.v35;

import International_Trade_Union.entity.entities.EntityAccount;
import International_Trade_Union.entity.entities.EntityBlock;
import International_Trade_Union.entity.entities.EntityDtoTransaction;
import International_Trade_Union.setings.Seting;
import International_Trade_Union.vote.VoteEnum;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;

import java.math.BigDecimal;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

@DataJpaTest
@Import(JpaV35SupplyDataSource.class)
class JpaV35SupplyDataSourceTest {
    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private JpaV35SupplyDataSource dataSource;

    @Test
    void readsOnlyScalarTipTransactionAndAccountValues() {
        EntityBlock block = new EntityBlock(
                Collections.emptyList(),
                "previous",
                "miner",
                Seting.ADDRESS_FOUNDER,
                1L,
                0.0,
                1L,
                1L,
                7L,
                "hash7",
                7L);
        entityManager.persist(block);

        EntityDtoTransaction transaction = new EntityDtoTransaction(
                Seting.BASIS_ADDRESS,
                "customer",
                8.0,
                0.0,
                null,
                0.0,
                VoteEnum.YES,
                "signature");
        transaction.setEntityBlock(block);
        entityManager.persist(transaction);
        entityManager.persist(new EntityAccount(
                Seting.ADDRESS_FOUNDER,
                new BigDecimal("123.45"),
                BigDecimal.ZERO,
                BigDecimal.ZERO));
        entityManager.flush();
        entityManager.clear();

        V35ChainTip tip = dataSource.currentTip();
        V35SupplyTransactionPage page = dataSource.basisTransactions(0L, 7L, 0, 1000);

        assertEquals(7L, tip.getHeight());
        assertEquals("hash7", tip.getBlockHash());
        assertEquals("hash7", dataSource.blockHashAt(7L));
        assertEquals(1, page.getRows().size());
        assertFalse(page.hasNext());
        assertEquals(Seting.BASIS_ADDRESS, page.getRows().get(0).getSender());
        assertEquals(8.0, page.getRows().get(0).getDigitalDollar());
        assertEquals(new BigDecimal("123.45"), dataSource.accountGoldBalance(Seting.ADDRESS_FOUNDER));
    }
}
