package International_Trade_Union.protocol.v35;

import java.util.Map;

public interface V35SupplySnapshotProvider {
    V35SupplyContext snapshot();

    long currentHeight();

    Map<String, Object> diagnostics();
}
