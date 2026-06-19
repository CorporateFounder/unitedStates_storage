package International_Trade_Union.controllers;

import International_Trade_Union.protocol.v35.V35SupplyApiResponseBuilder;
import International_Trade_Union.protocol.v35.V35SupplyContext;
import International_Trade_Union.protocol.v35.V35SupplySnapshotProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.Map;

@RestController
public class V35SupplyController {
    private final V35SupplySnapshotProvider snapshotProvider;

    @Autowired
    public V35SupplyController(V35SupplySnapshotProvider snapshotProvider) {
        this.snapshotProvider = snapshotProvider;
    }

    @GetMapping("/v35/supply")
    public Map<String, Object> supply() {
        V35SupplyContext context = snapshotProvider.snapshot();
        return withSnapshotMetadata(
                V35SupplyApiResponseBuilder.supply(context.getSnapshot(), context.getCurrentHeight()),
                context);
    }

    @GetMapping("/v35/protocol")
    public Map<String, Object> protocol() {
        return V35SupplyApiResponseBuilder.protocol(snapshotProvider.currentHeight());
    }

    @GetMapping("/v35/burn")
    public Map<String, Object> burn() {
        V35SupplyContext context = snapshotProvider.snapshot();
        return withSnapshotMetadata(
                V35SupplyApiResponseBuilder.burn(context.getSnapshot(), context.getCurrentHeight()),
                context);
    }

    @GetMapping("/v35/founder")
    public Map<String, Object> founder() {
        V35SupplyContext context = snapshotProvider.snapshot();
        return withSnapshotMetadata(
                V35SupplyApiResponseBuilder.founder(context.getSnapshot(), context.getCurrentHeight()),
                context);
    }

    @GetMapping("/v35/founder/burn-snapshot")
    public Map<String, Object> founderBurnSnapshot() {
        V35SupplyContext context = snapshotProvider.snapshot();
        return withSnapshotMetadata(
                V35SupplyApiResponseBuilder.founderBurnSnapshot(
                        context.getSnapshot(),
                        context.getCurrentHeight(),
                        context.getLatestBlockHash()),
                context);
    }

    @GetMapping("/v35/public-snapshot")
    public Map<String, Object> publicSnapshot() {
        V35SupplyContext context = snapshotProvider.snapshot();
        Map<String, Object> response = V35SupplyApiResponseBuilder.supply(
                context.getSnapshot(), context.getCurrentHeight());
        response.put("latestBlockHash", context.getLatestBlockHash());
        response.put("snapshotHeight", context.getCurrentHeight());
        response.put("snapshotCreatedAt", context.getCreatedAt().toString());
        response.put("snapshotSource", context.getSource());
        response.put("snapshotStale", context.isStale());
        response.put("cacheState", context.isStale() ? "last_good_stale" : "current");
        response.put("burnTransactionCreated", false);
        response.put("founderBurnOccurred", false);
        return response;
    }

    @GetMapping("/debug/v35/supply")
    public Map<String, Object> supplyDiagnostics() {
        return snapshotProvider.diagnostics();
    }

    private static Map<String, Object> withSnapshotMetadata(Map<String, Object> response,
                                                            V35SupplyContext context) {
        Map<String, Object> result = new LinkedHashMap<>(response);
        result.put("snapshotSource", context.getSource());
        result.put("snapshotHeight", context.getCurrentHeight());
        result.put("snapshotBlockHash", context.getLatestBlockHash());
        result.put("snapshotCreatedAt", context.getCreatedAt().toString());
        result.put("snapshotStale", context.isStale());
        return result;
    }
}
