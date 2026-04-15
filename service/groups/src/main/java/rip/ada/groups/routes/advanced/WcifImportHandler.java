package rip.ada.groups.routes.advanced;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.javalin.http.Context;
import io.pebbletemplates.pebble.PebbleEngine;
import io.javalin.http.UploadedFile;
import rip.ada.groups.session.AuthenticatedCompetitionHandler;
import rip.ada.groups.session.Session;
import rip.ada.groups.templates.Message;
import rip.ada.groups.wca.WcaApi;
import rip.ada.wcif.Competition;

import java.util.List;

import static rip.ada.groups.templates.Templates.model;
import static rip.ada.groups.templates.Templates.render;

public class WcifImportHandler extends AuthenticatedCompetitionHandler {

    private final WcaApi wcaApi;
    private final PebbleEngine engine;

    public WcifImportHandler(final WcaApi wcaApi, final PebbleEngine engine) {
        super(wcaApi);
        this.wcaApi = wcaApi;
        this.engine = engine;
    }

    @Override
    public void handle(final Competition competition, final Session session, final Context ctx) throws Exception {
        final UploadedFile uploadedFile = ctx.uploadedFile("wcif");
        if (uploadedFile == null) {
            model(ctx).put("messages", List.of(new Message("No file uploaded", Message.Type.ERROR)));
            render(engine, "advanced", ctx);
            return;
        }
        final String json = new String(uploadedFile.content().readAllBytes());
        final Competition newCompetition = new ObjectMapper()
                .registerModule(new JavaTimeModule())
                .readValue(json, Competition.class);
        wcaApi.updateCompetition(session.getWcaSession(), newCompetition);
        model(ctx).put("messages", List.of(new Message("Successfully imported WCIF", Message.Type.SUCCESS)));
        render(engine, "advanced", ctx);
    }
}
