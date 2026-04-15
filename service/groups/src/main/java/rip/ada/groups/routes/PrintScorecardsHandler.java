package rip.ada.groups.routes;

import io.javalin.http.ContentType;
import io.pebbletemplates.pebble.PebbleEngine;
import io.javalin.http.Context;
import io.javalin.http.Header;
import org.apache.pdfbox.pdmodel.PDDocument;
import rip.ada.groups.scorecards.Generator;
import rip.ada.groups.scorecards.ScorecardPrinter;
import rip.ada.groups.scorecards.ScorecardSet;
import rip.ada.groups.session.AuthenticatedCompetitionHandler;
import rip.ada.groups.session.Session;
import rip.ada.groups.wca.WcaApi;
import rip.ada.wcif.ActivityCode;
import rip.ada.wcif.Competition;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;

import static rip.ada.groups.templates.Templates.render;

public class PrintScorecardsHandler extends AuthenticatedCompetitionHandler {

    private final PebbleEngine engine;
    private final Generator scorecardGenerator = new Generator();

    public PrintScorecardsHandler(final WcaApi wcaApi, final PebbleEngine engine) {
        super(wcaApi);
        this.engine = engine;
    }

    @Override
    public void handle(final Competition competition, final Session session, final Context ctx) throws Exception {
        final List<String> rounds = ctx.formParams("rounds[]");
        final List<String> rooms = ctx.formParams("rooms[]");
        final ScorecardSet scorecardSet = new ScorecardSet(new ArrayList<>());
        for (final String round : rounds) {
            final ActivityCode activity = ActivityCode.fromString(round);
            final ScorecardSet roundScorecardSet = scorecardGenerator.generateScorecards(competition, activity.event(), activity.round(), rooms);
            scorecardSet.scorecards().addAll(roundScorecardSet.scorecards());
        }

        if (scorecardSet.scorecards().isEmpty()) {
            render(engine, "notopen", ctx);
            return;
        }

        try (PDDocument pdDocument = new ScorecardPrinter().printScorecards(scorecardSet);
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            pdDocument.save(out);
            ctx.contentType(ContentType.APPLICATION_PDF);
            ctx.header(Header.CONTENT_DISPOSITION, "attachment; filename=\"" + competition.getId() + "-scorecards.pdf\"");
            ctx.result(out.toByteArray());
        }
    }
}
