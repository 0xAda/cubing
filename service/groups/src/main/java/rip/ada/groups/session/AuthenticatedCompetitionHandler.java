package rip.ada.groups.session;

import io.javalin.http.Context;
import io.javalin.http.HandlerType;
import rip.ada.groups.wca.WcaApi;
import rip.ada.groups.wcalive.WcaLiveData;
import rip.ada.wcif.Competition;

import java.time.Instant;
import java.util.Set;

import static rip.ada.groups.templates.Templates.model;

public abstract class AuthenticatedCompetitionHandler extends AuthenticatedHandler {

    private static final Set<HandlerType> UNCACHED_METHODS = Set.of(HandlerType.POST, HandlerType.PATCH, HandlerType.PUT);
    private final WcaApi wcaApi;

    protected AuthenticatedCompetitionHandler(final WcaApi wcaApi) {
        this.wcaApi = wcaApi;
    }

    @Override
    public void handle(final Session session, final Context ctx) throws Exception {
        final String competitionId = ctx.pathParam("competition");
        final Competition cachedCompetition = session.getCachedCompetition(competitionId);
        final Instant lastSynchronizedAt = WcaLiveData.INSTANCE.getLastSynchronizedAt(competitionId);
        if (cachedCompetition != null && lastSynchronizedAt != null && !UNCACHED_METHODS.contains(ctx.method()) && session.isCompetitionCachedAfter(competitionId, lastSynchronizedAt)) {
            model(ctx).put("competition", cachedCompetition);
            model(ctx).put("competition_id", cachedCompetition.getId());
            handle(cachedCompetition, session, ctx);
            return;
        }

        final Competition competition = wcaApi.getCompetition(session.getWcaSession(), competitionId);
        session.cacheCompetition(competitionId, competition);
        model(ctx).put("competition", competition);
        model(ctx).put("competition_id", competition.getId());
        handle(competition, session, ctx);
    }

    public abstract void handle(Competition competition, Session session, Context ctx) throws Exception;
}
