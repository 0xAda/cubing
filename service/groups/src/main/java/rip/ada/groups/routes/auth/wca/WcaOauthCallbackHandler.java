package rip.ada.groups.routes.auth.wca;

import io.javalin.http.Context;
import io.javalin.http.Handler;
import rip.ada.groups.Config;
import rip.ada.groups.session.Session;
import rip.ada.groups.session.SessionRegistry;
import rip.ada.wca.AuthenticatedWcaApi;
import rip.ada.wca.model.CompetitionInfo;
import rip.ada.wca.model.Person;

import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;

public class WcaOauthCallbackHandler implements Handler {

    private final Config config;
    private final SessionRegistry sessionRegistry;

    public WcaOauthCallbackHandler(final Config config, final SessionRegistry sessionRegistry) {
        this.config = config;
        this.sessionRegistry = sessionRegistry;
    }

    @Override
    public void handle(final Context ctx) {
        final String oauthCode = ctx.queryParam("code");
        if (oauthCode == null) {
            ctx.redirect("/");
            return;
        }

        final Session session = sessionRegistry.createSession(oauthCode);
        final AuthenticatedWcaApi wcaApi = new AuthenticatedWcaApi(config.wcaApiConfig(), new ArrayBlockingQueue<>(1));
        final Person me = wcaApi.getMe(session.getWcaSession());
        session.setPerson(me);

        final List<CompetitionInfo> competitionList = wcaApi.getCompetitionList(session.getWcaSession());
        session.setCompetitions(competitionList);

        ctx.header("Set-Cookie", String.format("session=%s; Path=/; Max-Age=%d; HttpOnly; SameSite=Lax",
                session.getSessionToken().toString(), 60 * 60 * 24 * 7));
        ctx.redirect("/");
    }
}
