package International_Trade_Union;

import International_Trade_Union.controllers.BasisController;
import International_Trade_Union.logger.MyLogger;
import International_Trade_Union.model.NodeMemoryProfiler;
import International_Trade_Union.utils.UtilsCreatedDirectory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

import java.io.IOException;


@SpringBootApplication
@EnableAspectJAutoProxy
public class UnitedStatesApplication {
	public static void main(String[] args) throws IOException {
		installJvmDiagnostics();
		SpringApplication.run(UnitedStatesApplication.class, args);
	}

	private static void installJvmDiagnostics() {
		Thread.setDefaultUncaughtExceptionHandler((thread, throwable) -> {
			Runtime runtime = Runtime.getRuntime();
			MyLogger.saveLog("UNCAUGHT_EXCEPTION class=" + throwable.getClass().getName()
					+ " message=" + throwable.getMessage()
					+ " thread=" + thread.getName()
					+ " usedMemory=" + (runtime.totalMemory() - runtime.freeMemory())
					+ " maxMemory=" + runtime.maxMemory(), throwable);
		});
		Runtime.getRuntime().addShutdownHook(new Thread(() -> {
			Runtime runtime = Runtime.getRuntime();
			MyLogger.warnLog("JVM_SHUTDOWN usedMemory=" + (runtime.totalMemory() - runtime.freeMemory())
					+ " maxMemory=" + runtime.maxMemory()
					+ " currentChainSize=" + safeChainSize()
					+ " latestBlockIndex=" + safeLatestBlockIndex()
					+ " lastMemoryProbeStage=" + NodeMemoryProfiler.lastStageName());
		}, "citu-shutdown-diagnostics"));
	}

	private static long safeChainSize() {
		try {
			return BasisController.getBlockchainSize();
		} catch (Exception e) {
			return -1L;
		}
	}

	private static long safeLatestBlockIndex() {
		try {
			return BasisController.prevBlock() == null ? -1L : BasisController.prevBlock().getIndex();
		} catch (Exception e) {
			return -1L;
		}
	}
}
