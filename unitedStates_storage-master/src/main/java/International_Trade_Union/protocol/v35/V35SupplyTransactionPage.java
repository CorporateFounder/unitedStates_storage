package International_Trade_Union.protocol.v35;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class V35SupplyTransactionPage {
    private final List<V35SupplyTransactionRow> rows;
    private final boolean hasNext;

    public V35SupplyTransactionPage(List<V35SupplyTransactionRow> rows, boolean hasNext) {
        List<V35SupplyTransactionRow> safeRows = rows == null
                ? Collections.emptyList()
                : new ArrayList<>(rows);
        this.rows = Collections.unmodifiableList(safeRows);
        this.hasNext = hasNext;
    }

    public List<V35SupplyTransactionRow> getRows() {
        return rows;
    }

    public boolean hasNext() {
        return hasNext;
    }
}
