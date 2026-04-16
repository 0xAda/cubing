package rip.ada.groups.session;

import rip.ada.wca.OauthApi;
import rip.ada.wca.OauthSession;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class SessionRegistry {

    private final Map<UUID, Session> sessions = new ConcurrentHashMap<>();
    private final OauthApi oauthApi;

    public SessionRegistry(final OauthApi oauthApi) {
        this.oauthApi = oauthApi;
    }

    public Session createSession(final String code) {
        final UUID sessionUUID = UUID.randomUUID();
        final OauthSession oauthSession = OauthSession.create(oauthApi, code);
        final Session session = new Session(sessionUUID, oauthSession);
        sessions.put(sessionUUID, session);
        return session;
    }

    public Session refreshSession(final Session session) {
        session.getWcaSession().refresh(oauthApi);
        return session;
    }

    public Session getSession(final UUID sessionUUID) {
        return sessions.get(sessionUUID);
    }

    public void deleteSession(final UUID sessionUUID) {
        sessions.remove(sessionUUID);
    }

}
