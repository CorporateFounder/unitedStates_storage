package International_Trade_Union.protocol.v35;

import java.math.BigDecimal;

public interface V35SupplyDataSource {
    V35ChainTip currentTip();

    String blockHashAt(long height);

    V35SupplyTransactionPage basisTransactions(long fromHeight,
                                               long toHeight,
                                               int pageNumber,
                                               int pageSize);

    BigDecimal accountGoldBalance(String address);
}
