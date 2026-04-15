package rip.ada.groups.session;

import io.javalin.http.Context;
import io.javalin.http.Handler;

import static rip.ada.groups.templates.Templates.model;

public abstract class AuthenticatedHandler implements Handler {

    @Override
    public void handle(final Context ctx) throws Exception {
        final Session session = (Session) model(ctx).get("session");
        if (session == null) {
            ctx.redirect("/");
            return;
        }
        handle(session, ctx);
    }

    public abstract void handle(Session session, Context ctx) throws Exception;
}
