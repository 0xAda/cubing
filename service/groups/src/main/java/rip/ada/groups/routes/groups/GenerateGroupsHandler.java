package rip.ada.groups.routes.groups;

import io.javalin.http.Context;
import io.pebbletemplates.pebble.PebbleEngine;
import rip.ada.groups.session.AuthenticatedCompetitionHandler;
import rip.ada.groups.session.Session;
import rip.ada.groups.templates.Message;
import rip.ada.groups.wca.WcaApi;
import rip.ada.wcif.Competition;

import java.util.List;

import static rip.ada.groups.templates.Templates.model;
import static rip.ada.groups.templates.Templates.render;

public class GenerateGroupsHandler extends AuthenticatedCompetitionHandler {

    private final WcaApi wcaApi;
    private final PebbleEngine engine;

    public GenerateGroupsHandler(final WcaApi wcaApi, final PebbleEngine engine) {
        super(wcaApi);
        this.wcaApi = wcaApi;
        this.engine = engine;
    }

    @Override
    public void handle(final Competition competition, final Session session, final Context ctx) {
        wcaApi.updateCompetition(session.getWcaSession(), competition);
        model(ctx).put("messages", List.of(new Message("Assigned groups", Message.Type.SUCCESS)));
        render(engine, "competition_info", ctx);
    }
}
