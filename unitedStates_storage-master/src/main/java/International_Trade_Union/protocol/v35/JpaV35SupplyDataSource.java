package International_Trade_Union.protocol.v35;

import International_Trade_Union.setings.Seting;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Repository
@Transactional(readOnly = true)
public class JpaV35SupplyDataSource implements V35SupplyDataSource {
    private static final int MAX_PAGE_SIZE = 1000;

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public V35ChainTip currentTip() {
        List<?> rows = entityManager.createQuery(
                        "SELECT b.specialIndex, b.hashBlock FROM EntityBlock b "
                                + "WHERE b.specialIndex = (SELECT MAX(t.specialIndex) FROM EntityBlock t) "
                                + "ORDER BY b.id DESC")
                .setMaxResults(1)
                .getResultList();
        if (rows.isEmpty()) {
            return new V35ChainTip(-1L, "UNKNOWN");
        }
        Object[] row = (Object[]) rows.get(0);
        return new V35ChainTip(number(row[0]).longValue(), string(row[1]));
    }

    @Override
    public String blockHashAt(long height) {
        List<?> hashes = entityManager.createQuery(
                        "SELECT b.hashBlock FROM EntityBlock b "
                                + "WHERE b.specialIndex = :height ORDER BY b.id DESC")
                .setParameter("height", height)
                .setMaxResults(1)
                .getResultList();
        return hashes.isEmpty() ? "UNKNOWN" : string(hashes.get(0));
    }

    @Override
    public V35SupplyTransactionPage basisTransactions(long fromHeight,
                                                      long toHeight,
                                                      int pageNumber,
                                                      int pageSize) {
        if (toHeight < fromHeight || toHeight < 0) {
            return new V35SupplyTransactionPage(Collections.emptyList(), false);
        }
        int boundedSize = Math.max(1, Math.min(MAX_PAGE_SIZE, pageSize));
        int boundedPage = Math.max(0, pageNumber);
        Query query = entityManager.createQuery(
                        "SELECT t.sender, t.digitalDollar, t.entityBlock.specialIndex "
                                + "FROM EntityDtoTransaction t "
                                + "WHERE t.sender = :sender "
                                + "AND t.entityBlock.specialIndex BETWEEN :fromHeight AND :toHeight "
                                + "ORDER BY t.id ASC")
                .setParameter("sender", Seting.BASIS_ADDRESS)
                .setParameter("fromHeight", fromHeight)
                .setParameter("toHeight", toHeight)
                .setFirstResult(boundedPage * boundedSize)
                .setMaxResults(boundedSize + 1);

        List<?> result = query.getResultList();
        boolean hasNext = result.size() > boundedSize;
        int rowCount = Math.min(result.size(), boundedSize);
        List<V35SupplyTransactionRow> rows = new ArrayList<>(rowCount);
        for (int i = 0; i < rowCount; i++) {
            Object[] row = (Object[]) result.get(i);
            rows.add(new V35SupplyTransactionRow(
                    string(row[0]),
                    number(row[1]).doubleValue(),
                    number(row[2]).longValue()));
        }
        entityManager.clear();
        return new V35SupplyTransactionPage(rows, hasNext);
    }

    @Override
    public BigDecimal accountGoldBalance(String address) {
        if (address == null || address.trim().isEmpty()) {
            return BigDecimal.ZERO.setScale(2);
        }
        List<?> balances = entityManager.createQuery(
                        "SELECT a.digitalDollarBalance FROM EntityAccount a "
                                + "WHERE a.account = :address ORDER BY a.id DESC")
                .setParameter("address", address)
                .setMaxResults(1)
                .getResultList();
        if (balances.isEmpty() || balances.get(0) == null) {
            return BigDecimal.ZERO.setScale(2);
        }
        Object balance = balances.get(0);
        if (balance instanceof BigDecimal) {
            return (BigDecimal) balance;
        }
        return new BigDecimal(balance.toString());
    }

    private static Number number(Object value) {
        return value instanceof Number ? (Number) value : 0L;
    }

    private static String string(Object value) {
        return value == null || value.toString().trim().isEmpty() ? "UNKNOWN" : value.toString();
    }
}
