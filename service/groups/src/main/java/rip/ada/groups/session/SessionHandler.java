package rip.ada.groups.session;

import io.javalin.http.Context;
import io.javalin.http.Handler;

import java.util.UUID;

import static rip.ada.groups.templates.Templates.model;

public class SessionHandler implements Handler {

    private final SessionRegistry sessionRegistry;

    public SessionHandler(final SessionRegistry sessionRegistry) {
        this.sessionRegistry = sessionRegistry;
    }

    @Override
    public void handle(final Context ctx) {
        final String sessionCookieValue = ctx.cookie("session");
        if (sessionCookieValue == null) {
            return;
        }

        final UUID sessionUUID;
        try {
            sessionUUID = UUID.fromString(sessionCookieValue);
        } catch (final IllegalArgumentException e) {
            return;
        }

        final Session session = sessionRegistry.getSession(sessionUUID);
        if (session == null) {
            return;
        }

        final Session activeSession;
        if (session.getWcaSession().needsRefresh()) {
            activeSession = sessionRegistry.refreshSession(session);
        } else {
            activeSession = session;
        }
        model(ctx).put("session", activeSession);
    }
}
