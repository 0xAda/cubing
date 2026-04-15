package rip.ada.groups.wcalive;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class WcaLiveData {

    private static final Logger LOGGER = LoggerFactory.getLogger(WcaLiveData.class);
    public static WcaLiveData INSTANCE;
    private final WcaLiveApi wcaLiveApi;
    private final Map<String, Instant> lastSynchronizedAt = new ConcurrentHashMap<>();

    public WcaLiveData(final WcaLiveApi wcaLiveApi) {
        this.wcaLiveApi = wcaLiveApi;
        INSTANCE = this;
    }

    public void refresh() {
        try {
            wcaLiveApi.getSynchronizedCompetitions().forEach(competition -> lastSynchronizedAt.put(competition.wcaId(), competition.synchronizedAt()));
        } catch (final Exception e) {
            LOGGER.warn("Failed to refresh WCA live data", e);
        }
    }

    public Instant getLastSynchronizedAt(final String wcaId) {
        return lastSynchronizedAt.get(wcaId);
    }

}
