package rip.ada.groups.routes;

import io.javalin.http.Context;
import io.pebbletemplates.pebble.PebbleEngine;
import rip.ada.groups.session.AuthenticatedCompetitionHandler;
import rip.ada.groups.session.Session;
import rip.ada.groups.warnings.CompetitionWarnings;
import rip.ada.groups.wca.WcaApi;
import rip.ada.groups.wcalive.WcaLiveData;
import rip.ada.wcif.Competition;

import java.time.Duration;
import java.time.Instant;

import static rip.ada.groups.templates.Templates.model;
import static rip.ada.groups.templates.Templates.render;

public class CompetitionHandler extends AuthenticatedCompetitionHandler {

    private final PebbleEngine engine;
    private final WcaLiveData wcaLiveData;
    private final CompetitionWarnings competitionWarnings;

    public CompetitionHandler(final WcaApi wcaApi, final PebbleEngine engine, final WcaLiveData wcaLiveData) {
        super(wcaApi);
        this.engine = engine;
        this.wcaLiveData = wcaLiveData;
        this.competitionWarnings = new CompetitionWarnings();
    }

    @Override
    public void handle(final Competition competition, final Session session, final Context ctx) {
        model(ctx).put("competition_name", competition.getName());
        model(ctx).put("competition_id", competition.getId());
        model(ctx).put("competition", competition);
        final Instant lastSynchronizedAt = wcaLiveData.getLastSynchronizedAt(competition.getId());
        if (lastSynchronizedAt != null) {
            model(ctx).put("synchronized_ago_seconds", Duration.between(lastSynchronizedAt, Instant.now()).getSeconds());
        }
        model(ctx).put("messages", competitionWarnings.getWarnings(competition));
        render(engine, "competition_info", ctx);
    }
}
