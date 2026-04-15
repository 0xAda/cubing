package rip.ada.groups.routes.advanced;

import io.javalin.http.Context;
import io.pebbletemplates.pebble.PebbleEngine;
import rip.ada.groups.session.AuthenticatedCompetitionHandler;
import rip.ada.groups.session.Session;
import rip.ada.groups.templates.Message;
import rip.ada.groups.wca.WcaApi;
import rip.ada.wcif.Competition;
import rip.ada.wcif.Person;

import java.util.List;

import static rip.ada.groups.templates.Templates.model;
import static rip.ada.groups.templates.Templates.render;

public class ClearAssignmentsHandler extends AuthenticatedCompetitionHandler {

    private final WcaApi wcaApi;
    private final PebbleEngine engine;

    public ClearAssignmentsHandler(final WcaApi wcaApi, final PebbleEngine engine) {
        super(wcaApi);
        this.wcaApi = wcaApi;
        this.engine = engine;
    }

    @Override
    public void handle(final Competition competition, final Session session, final Context ctx) {
        final String ids = ctx.formParam("ids");
        if (ids != null && !ids.isBlank()) {
            for (final Person person : competition.getPersons()) {
                if (ids.contains(person.wcaId())) {
                    person.assignments().clear();
                }
            }
        } else {
            for (final Person person : competition.getPersons()) {
                person.assignments().clear();
            }
        }
        wcaApi.updateCompetition(session.getWcaSession(), competition);
        model(ctx).put("messages", List.of(new Message("Cleared all assignments", Message.Type.SUCCESS)));
        render(engine, "advanced", ctx);
    }
}
