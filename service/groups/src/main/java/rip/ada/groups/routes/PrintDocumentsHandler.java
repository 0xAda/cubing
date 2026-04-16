package rip.ada.groups.routes;

import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.AreaBreak;
import com.itextpdf.layout.properties.AreaBreakType;
import io.javalin.http.ContentType;
import io.javalin.http.Context;
import io.javalin.http.Header;
import rip.ada.groups.printing.AssignedScramblersPrinter;
import rip.ada.groups.printing.GroupSchedulePrinter;
import rip.ada.groups.session.AuthenticatedCompetitionHandler;
import rip.ada.groups.session.Session;
import rip.ada.groups.wca.WcaApi;
import rip.ada.wcif.Competition;

import java.io.ByteArrayOutputStream;

public class PrintDocumentsHandler extends AuthenticatedCompetitionHandler {

    private final AssignedScramblersPrinter assignedScramblersPrinter = new AssignedScramblersPrinter();
    private final GroupSchedulePrinter groupSchedulePrinter = new GroupSchedulePrinter();

    public PrintDocumentsHandler(final WcaApi wcaApi) {
        super(wcaApi);
    }

    @Override
    public void handle(final Competition competition, final Session session, final Context ctx) throws Exception {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            final PdfWriter pdfWriter = new PdfWriter(out);
            final PdfDocument pdf = new PdfDocument(pdfWriter);
            final Document document = new Document(pdf);

            boolean hasPrinted = false;

            if (ctx.formParam("assignedScramblers") != null) {
                hasPrinted = true;
                assignedScramblersPrinter.printAssignedScramblers(competition, document);
            }


            if (ctx.formParam("groupSchedule") != null) {
                if (hasPrinted) {
                    document.add(new AreaBreak(AreaBreakType.NEXT_PAGE));
                }
                hasPrinted = true;
                groupSchedulePrinter.printGroupSchedule(competition, document);
            }

            document.flush();
            document.close();
            ctx.contentType(ContentType.APPLICATION_PDF);
            ctx.header(Header.CONTENT_DISPOSITION, "attachment; filename=\"" + competition.getId() + "-scramblers.pdf\"");
            ctx.result(out.toByteArray());
        }
    }
}
