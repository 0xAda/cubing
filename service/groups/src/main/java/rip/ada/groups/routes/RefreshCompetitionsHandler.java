package rip.ada.groups.routes;

import io.javalin.http.Context;
import rip.ada.groups.session.AuthenticatedHandler;
import rip.ada.groups.session.Session;

public class RefreshCompetitionsHandler extends AuthenticatedHandler {

    @Override
    public void handle(final Session session, final Context ctx) {
        session.clearCompetitionCache();
        ctx.redirect("/");
    }
}
