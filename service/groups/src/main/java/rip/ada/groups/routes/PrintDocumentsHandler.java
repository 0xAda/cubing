package rip.ada.groups.routes;

import io.javalin.http.ContentType;
import io.javalin.http.Context;
import io.javalin.http.Header;
import rip.ada.groups.printing.AssignedScramblersPrinter;
import rip.ada.groups.session.AuthenticatedCompetitionHandler;
import rip.ada.groups.session.Session;
import rip.ada.groups.wca.WcaApi;
import rip.ada.wcif.Competition;

import java.io.ByteArrayOutputStream;

public class PrintDocumentsHandler extends AuthenticatedCompetitionHandler {

    private final AssignedScramblersPrinter assignedScramblersPrinter = new AssignedScramblersPrinter();

    public PrintDocumentsHandler(final WcaApi wcaApi) {
        super(wcaApi);
    }

    @Override
    public void handle(final Competition competition, final Session session, final Context ctx) throws Exception {
        if (ctx.formParam("assignedScramblers") != null) {
            try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
                assignedScramblersPrinter.printAssignedScramblers(competition, out);
                ctx.contentType(ContentType.APPLICATION_PDF);
                ctx.header(Header.CONTENT_DISPOSITION, "attachment; filename=\"" + competition.getId() + "-scramblers.pdf\"");
                ctx.result(out.toByteArray());
            }
        } else {
            ctx.redirect("/" + competition.getId() + "/scorecardPrinting");
        }
    }
}
