package rip.ada.groups.routes.advanced;

import io.javalin.http.Context;
import io.pebbletemplates.pebble.PebbleEngine;
import rip.ada.groups.session.AuthenticatedCompetitionHandler;
import rip.ada.groups.session.Session;
import rip.ada.groups.templates.Message;
import rip.ada.wca.AuthenticatedWcaApi;
import rip.ada.wcif.Competition;

import java.util.List;

import static rip.ada.groups.templates.Templates.model;
import static rip.ada.groups.templates.Templates.render;

public class ClearScheduleHandler extends AuthenticatedCompetitionHandler {

    private final PebbleEngine engine;
    private final AuthenticatedWcaApi wcaApi;

    public ClearScheduleHandler(final AuthenticatedWcaApi wcaApi, final PebbleEngine engine) {
        super(wcaApi);
        this.engine = engine;
        this.wcaApi = wcaApi;
    }

    @Override
    public void handle(final Competition competition, final Session session, final Context ctx) {
        competition.getSchedule().getVenues().clear();

        wcaApi.updateCompetition(session.getWcaSession(), competition);

        model(ctx).put("messages", List.of(new Message("Cleared schedule", Message.Type.SUCCESS)));
        render(engine, "advanced", ctx);
    }
}
