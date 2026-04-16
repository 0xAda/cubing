package rip.ada.groups.routes;

import io.javalin.http.Context;
import io.pebbletemplates.pebble.PebbleEngine;
import rip.ada.groups.report.Report;
import rip.ada.groups.report.ReportGenerator;
import rip.ada.groups.report.ReportRegistry;
import rip.ada.groups.session.AuthenticatedCompetitionHandler;
import rip.ada.groups.session.Session;
import rip.ada.wca.AuthenticatedWcaApi;
import rip.ada.wcif.Competition;

import java.util.List;

import static rip.ada.groups.templates.Templates.model;
import static rip.ada.groups.templates.Templates.render;

public class ReportHandler extends AuthenticatedCompetitionHandler {

    private final PebbleEngine engine;
    private final ReportRegistry reportRegistry;

    public ReportHandler(final AuthenticatedWcaApi wcaApi, final PebbleEngine engine, final ReportRegistry reportRegistry) {
        super(wcaApi);
        this.engine = engine;
        this.reportRegistry = reportRegistry;
    }

    @Override
    public void handle(final Competition competition, final Session session, final Context ctx) {
        final String reportName = ctx.pathParam("report");
        final ReportGenerator reportGenerator = reportRegistry.getReport(reportName);
        if (reportGenerator == null) {
            ctx.redirect("/");
            return;
        }
        model(ctx).put("reportName", reportGenerator.getDisplayName());
        final List<Report> report = reportGenerator.generateReport(competition);
        model(ctx).put("report", report);
        render(engine, "display_report", ctx);
    }
}
