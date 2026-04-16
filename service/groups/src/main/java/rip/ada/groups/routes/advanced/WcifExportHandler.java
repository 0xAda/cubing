package rip.ada.groups.routes.advanced;

import io.javalin.http.ContentType;
import io.javalin.http.Context;
import io.javalin.http.Header;
import rip.ada.groups.session.AuthenticatedCompetitionHandler;
import rip.ada.groups.session.Session;
import rip.ada.wca.AuthenticatedWcaApi;
import rip.ada.wcif.Competition;

public class WcifExportHandler extends AuthenticatedCompetitionHandler {

    private final AuthenticatedWcaApi wcaApi;

    public WcifExportHandler(final AuthenticatedWcaApi wcaApi) {
        super(wcaApi);
        this.wcaApi = wcaApi;
    }

    @Override
    public void handle(final Competition competition, final Session session, final Context ctx) throws Exception {
        final String output = wcaApi.serialize(competition);
        ctx.contentType(ContentType.APPLICATION_JSON);
        ctx.header(Header.CONTENT_DISPOSITION, "attachment; filename=\"" + competition.getId() + ".json\"");
        ctx.result(output);
    }
}
