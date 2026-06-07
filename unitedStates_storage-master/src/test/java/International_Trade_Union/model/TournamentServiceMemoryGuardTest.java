package International_Trade_Union.model;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

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

        List<HostEndDataShortB> bounded = TournamentService.boundedHostsForTournament(hosts);

        assertEquals(1, bounded.size());
        assertEquals("http://194.87.236.238:82", bounded.get(0).getHost());
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
}
