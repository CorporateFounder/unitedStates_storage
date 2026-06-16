package International_Trade_Union.logger;

import International_Trade_Union.setings.Seting;
import International_Trade_Union.model.DiagnosticLogRetention;
import International_Trade_Union.model.NodeMemoryProfiler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
public class ServerStartupLogging implements ApplicationRunner {
    private static final Logger logger = LoggerFactory.getLogger(ServerStartupLogging.class);

    @Value("${server.port:82}")
    private String port;

    @Value("${server.address:localhost}")
    private String address;

    @Value("${logging.file.path:C:/server/logs}")
    private String logPath;

    @Value("${citu.logs.cleanup.enabled:true}")
    private boolean logRetentionEnabled;

    @Value("${citu.logs.cleanupEveryBlocks:100}")
    private long cleanupEveryBlocks;

    @Value("${citu.logs.keepBlocks:5000}")
    private long keepBlocks;

    @Value("${citu.logs.maxFileBytes:52428800}")
    private long maxFileBytes;

    @Value("${citu.logs.maxFiles:20}")
    private int maxFiles;

    @Value("${citu.logs.dir:C:\\server\\logs}")
    private String retentionLogDir;

    @Override
    public void run(ApplicationArguments args) {
        Runtime runtime = Runtime.getRuntime();
        String version = "1.0.14";
        DiagnosticLogRetention.configure(
                logRetentionEnabled,
                cleanupEveryBlocks,
                keepBlocks,
                maxFileBytes,
                maxFiles,
                retentionLogDir);
        logger.info("NODE_VERSION={}", version);
        logger.info("MEMORY_DIAGNOSTIC_ENABLED={}", NodeMemoryProfiler.isEnabled());
        logger.info("{}", DiagnosticLogRetention.settingsSummary());
        logger.info("CITU_NODE_STARTUP version={} processId={} dbPath={} address={} port={} v35ForkIndex={} burnAddressPlaceholder={} maxHeap={} logPath={} nodeLogMaxFile=25MB nodeLogTotalCap=500MB errorLogMaxFile=25MB errorLogTotalCap=200MB",
                version,
                ProcessHandle.current().pid(),
                Seting.H2_DB + "db",
                address,
                port,
                Seting.V35_RESCUE_FORK_INDEX,
                "BURN_ADDRESS".equals(Seting.V35_BURN_ADDRESS),
                runtime.maxMemory(),
                logPath);
    }
}
