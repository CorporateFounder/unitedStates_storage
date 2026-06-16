package International_Trade_Union.model;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletionException;
import java.util.concurrent.TimeoutException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TournamentServiceMemoryGuardTest {

    @Test
    void tournamentHostListIsBounded() {
        List<HostEndDataShortB> hosts = new ArrayList<>();
        for (int i = 0; i < TournamentService.MAX_TOURNAMENT_HOSTS + 20; i++) {
            hosts.add(new HostEndDataShortB("http://example-" + i + ".invalid", null));
        }

        List<HostEndDataShortB> bounded = TournamentService.boundedHostsForTournament(hosts);

        assertEquals(TournamentService.MAX_TOURNAMENT_HOSTS, bounded.size());
    }

    @Test
    void tournamentHostListDropsBlankHosts() {
        List<HostEndDataShortB> hosts = new ArrayList<>();
        hosts.add(new HostEndDataShortB("", null));
        hosts.add(new HostEndDataShortB("   ", null));
        hosts.add(new HostEndDataShortB("http://194.87.236.238:82", null));
        hosts.add(new HostEndDataShortB("http://65.21.246.188:82", null));

        List<HostEndDataShortB> bounded = TournamentService.boundedHostsForTournament(hosts);

        assertEquals(1, bounded.size());
        assertEquals("http://65.21.246.188:82", bounded.get(0).getHost());
    }

    @Test
    void tournamentHttpTimeoutIsShortEnoughForRuntimeStability() {
        assertTrue(TournamentService.TOURNAMENT_HTTP_TIMEOUT_MILLIS <= 3000);
    }

    @Test
    void tournamentMemoryStatusIncludesRuntimeFields() {
        String memoryStatus = TournamentService.memoryStatusForLog();

        assertTrue(memoryStatus.contains("freeMemory="));
        assertTrue(memoryStatus.contains("totalMemory="));
        assertTrue(memoryStatus.contains("maxMemory="));
    }

    @Test
    void tournamentDiagnosticsCanBeResetForFreshRuntime() {
        TournamentService.resetDiagnosticsForTests();

        assertEquals(-1L, TournamentService.getLastTournamentStartMillis());
        assertEquals(-1L, TournamentService.getLastTournamentEndMillis());
        assertEquals(-1L, TournamentService.getLastTournamentDurationMillis());
        assertEquals("none", TournamentService.getLastOomRiskWarning());
        assertEquals(0, TournamentService.getTrackedPeerErrorCount());
    }

    @Test
    void tournamentWinnerResponseCapIsSmallForHeapSafety() {
        assertTrue(TournamentService.MAX_PEER_WINNER_BLOCKS <= 16);
    }

    @Test
    void peerTimeoutLoggingIsRateLimitedByTrackedErrorKey() {
        PeerErrorRateLimiter limiter = new PeerErrorRateLimiter(60_000);

        limiter.logPeerError("http://65.21.246.188:82", "/nodes/resolve_from_to_block", new java.net.SocketTimeoutException("Read timed out"));
        limiter.logPeerError("http://65.21.246.188:82", "/nodes/resolve_from_to_block", new java.net.SocketTimeoutException("Read timed out"));
        limiter.logPeerError("http://65.21.246.188:82", "/nodes/resolve_from_to_block", new java.net.SocketTimeoutException("Read timed out"));

        assertEquals(1, limiter.trackedErrorCount());
        assertTrue(limiter.lastWarning().contains("endpoint=/nodes/resolve_from_to_block"));
        assertTrue(limiter.lastWarning().contains("error=SocketTimeoutException"));
        assertTrue(limiter.lastWarning().contains("count=3"));
    }

    @Test
    void peerErrorLimiterIsCappedForBadPeerChurn() {
        PeerErrorRateLimiter limiter = new PeerErrorRateLimiter(60_000);

        for (int i = 0; i < 80; i++) {
            limiter.logPeerError(
                    "http://bad-peer-" + i + ".invalid:82",
                    "/nodes/resolve_from_to_block",
                    new java.net.SocketTimeoutException("Read timed out"));
        }

        assertTrue(limiter.trackedErrorCount() <= PeerNetworkPolicy.maxPeerErrorEntries());
    }

    @Test
    void completionWrappedTimeoutIsClassifiedAsNetworkTimeout() {
        CompletionException wrapped = new CompletionException(new TimeoutException("remote peer timed out"));

        assertTrue(PeerErrorRateLimiter.isNetworkTimeoutLike(wrapped));
    }

    @Test
    void peerTimeoutDefaultSummaryIntervalIsFiveMinutesOrOneHundredRepeats() {
        assertEquals(300_000L, PeerErrorRateLimiter.DEFAULT_SUMMARY_INTERVAL_MILLIS);
        assertEquals(100L, PeerErrorRateLimiter.SUMMARY_COUNT_INTERVAL);
    }
}
