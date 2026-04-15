package rip.ada.groups.routes.ukca;

import io.javalin.http.Context;
import io.pebbletemplates.pebble.PebbleEngine;
import rip.ada.groups.session.AuthenticatedCompetitionHandler;
import rip.ada.groups.session.Session;
import rip.ada.groups.wca.WcaApi;
import rip.ada.wcif.Competition;

import static rip.ada.groups.templates.Templates.render;

public class UKCAHandler extends AuthenticatedCompetitionHandler {

    private final PebbleEngine engine;

    public UKCAHandler(final WcaApi wcaApi, final PebbleEngine engine) {
        super(wcaApi);
        this.engine = engine;
    }

    @Override
    public void handle(final Competition competition, final Session session, final Context ctx) {
        render(engine, "ukca", ctx);
    }
}
