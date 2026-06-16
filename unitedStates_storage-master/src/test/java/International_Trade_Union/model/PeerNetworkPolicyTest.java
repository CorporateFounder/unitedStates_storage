package International_Trade_Union.model;

import International_Trade_Union.entity.blockchain.DataShortBlockchainInformation;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.net.SocketTimeoutException;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PeerNetworkPolicyTest {

    @AfterEach
    void cleanup() {
        System.clearProperty("citu.peer.maxPeersPerCycle");
        System.clearProperty("citu.peer.maxHealthEntries");
        System.clearProperty("citu.memoryGuard.skipPeerWorkPercent");
        System.clearProperty("citu.memoryGuard.emergencyPeerWorkPercent");
        PeerHealthRegistry.resetForTests();
    }

    @Test
    void peerSelectionCapsAtThreeByDefaultAndSkipsSelf() {
        List<HostEndDataShortB> hosts = IntStream.range(0, 8)
                .mapToObj(i -> host("http://peer" + i + ":82", 507540))
                .toList();

        List<HostEndDataShortB> selected = PeerHealthRegistry.selectHosts(hosts, PeerNetworkPolicy.maxPeersPerCycle(), "test", 507540);

        assertEquals(3, selected.size());
        assertFalse(selected.stream().anyMatch(peer -> PeerHealthRegistry.isSelfPeer(peer.getHost())));
    }

    @Test
    void configuredMaxFourWorks() {
        System.setProperty("citu.peer.maxPeersPerCycle", "4");
        List<HostEndDataShortB> hosts = IntStream.range(0, 8)
                .mapToObj(i -> host("http://configured-peer" + i + ":82", 507540))
                .toList();

        List<HostEndDataShortB> selected = PeerHealthRegistry.selectHosts(hosts, PeerNetworkPolicy.maxPeersPerCycle(), "test", 507540);

        assertEquals(4, selected.size());
    }

    @Test
    void stalePeerIsQuarantinedAndSkipped() {
        HostEndDataShortB stale = host("http://stale-peer:82", 482360);

        List<HostEndDataShortB> selected = PeerHealthRegistry.selectHosts(List.of(stale), 3, "resolve3", 507540);

        assertTrue(selected.isEmpty());
        assertTrue(PeerHealthRegistry.isQuarantined("http://stale-peer:82"));
        Map<String, Object> snapshot = PeerHealthRegistry.snapshot();
        assertEquals(1L, snapshot.get("quarantinedPeerCount"));
        assertEquals(1L, snapshot.get("stalePeerCount"));
    }

    @Test
    void timeoutPeerIsQuarantinedAfterThreshold() {
        String peer = "http://timeout-peer:82";

        PeerHealthRegistry.recordFailure(peer, "/datashort", new SocketTimeoutException("Read timed out"));
        PeerHealthRegistry.recordFailure(peer, "/datashort", new SocketTimeoutException("Read timed out"));

        assertTrue(PeerHealthRegistry.isQuarantined(peer));
        assertTrue(String.valueOf(PeerHealthRegistry.snapshot().get("lastPeerErrorSummary")).contains("SocketTimeoutException"));
    }

    @Test
    void debugSnapshotExposesCompactLimits() {
        Map<String, Object> snapshot = PeerHealthRegistry.snapshot();

        assertEquals(PeerNetworkPolicy.maxRemoteCallsPerMinute(), snapshot.get("maxRemoteCallsPerMinute"));
        assertEquals(PeerNetworkPolicy.maxPeersPerCycle(), snapshot.get("maxPeersPerCycle"));
        assertEquals(PeerNetworkPolicy.maxPeersPerResolveCycle(), snapshot.get("maxResolvePeersPerCycle"));
        assertEquals(PeerNetworkPolicy.maxPeersPerPropagationCycle(), snapshot.get("maxPropagationPeers"));
        assertEquals(PeerNetworkPolicy.maxPeerHealthEntries(), snapshot.get("maxPeerHealthEntries"));
        assertTrue(snapshot.containsKey("peerWorkMemoryGuardSkipPercent"));
        assertTrue(snapshot.containsKey("peerWorkMemoryGuardEmergencyPercent"));
    }

    @Test
    void peerHealthRegistryIsCappedForBadPeerChurn() {
        System.setProperty("citu.peer.maxHealthEntries", "32");

        for (int i = 0; i < 80; i++) {
            PeerHealthRegistry.recordFailure(
                    "http://bad-peer-" + i + ".invalid:82",
                    "/datashort",
                    new SocketTimeoutException("Read timed out"));
        }

        Number activePeerCount = (Number) PeerHealthRegistry.snapshot().get("activePeerCount");
        assertTrue(activePeerCount.intValue() <= PeerNetworkPolicy.maxPeerHealthEntries());
    }

    @Test
    void selfPeerSkipsDoNotCreateHealthEntries() {
        PeerHealthRegistry.recordSelfSkipped("http://194.87.236.238:82", "test");
        PeerHealthRegistry.recordSelfSkipped("http://127.0.0.1:82", "test");

        Number activePeerCount = (Number) PeerHealthRegistry.snapshot().get("activePeerCount");
        assertEquals(0, activePeerCount.intValue());
    }

    @Test
    void memoryGuardThresholdsArePercentBasedAndBounded() {
        System.setProperty("citu.memoryGuard.skipPeerWorkPercent", "70");
        System.setProperty("citu.memoryGuard.emergencyPeerWorkPercent", "85");

        assertFalse(PeerWorkMemoryGuard.shouldSkipForMemory(699, 1000));
        assertTrue(PeerWorkMemoryGuard.shouldSkipForMemory(700, 1000));
        assertFalse(PeerWorkMemoryGuard.shouldEmergencySkipForMemory(849, 1000));
        assertTrue(PeerWorkMemoryGuard.shouldEmergencySkipForMemory(850, 1000));
    }

    private static HostEndDataShortB host(String url, long size) {
        DataShortBlockchainInformation info = new DataShortBlockchainInformation();
        info.setSize(size);
        info.setValidation(true);
        return new HostEndDataShortB(url, info);
    }
}
