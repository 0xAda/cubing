package rip.ada.groups.routes.auth;

import io.javalin.http.Context;
import io.javalin.http.Handler;
import rip.ada.groups.session.Session;
import rip.ada.groups.session.SessionRegistry;

import static rip.ada.groups.templates.Templates.model;

public class LogoutHandler implements Handler {

    private final SessionRegistry sessionRegistry;

    public LogoutHandler(final SessionRegistry sessionRegistry) {
        this.sessionRegistry = sessionRegistry;
    }

    @Override
    public void handle(final Context ctx) {
        final Session session = (Session) model(ctx).get("session");
        if (session == null) {
            ctx.redirect("/");
            return;
        }

        sessionRegistry.deleteSession(session.getSessionToken());
        ctx.header("Set-Cookie", "session=; Path=/; Max-Age=0; HttpOnly; SameSite=Lax");
        ctx.redirect("/");
    }
}
