package rip.ada.groups.routes;

import io.javalin.http.Context;
import io.pebbletemplates.pebble.PebbleEngine;
import rip.ada.groups.report.ReportRegistry;
import rip.ada.groups.session.AuthenticatedCompetitionHandler;
import rip.ada.groups.session.Session;
import rip.ada.groups.wca.WcaApi;
import rip.ada.wcif.Competition;

import static rip.ada.groups.templates.Templates.model;
import static rip.ada.groups.templates.Templates.render;

public class ReportsHandler extends AuthenticatedCompetitionHandler {

    private final ReportRegistry reportRegistry;
    private final PebbleEngine engine;

    public ReportsHandler(final WcaApi wcaApi, final PebbleEngine engine, final ReportRegistry reportRegistry) {
        super(wcaApi);
        this.reportRegistry = reportRegistry;
        this.engine = engine;
    }

    @Override
    public void handle(final Competition competition, final Session session, final Context ctx) {
        model(ctx).put("reports", reportRegistry.getReportNames());
        render(engine, "reports", ctx);
    }
}
