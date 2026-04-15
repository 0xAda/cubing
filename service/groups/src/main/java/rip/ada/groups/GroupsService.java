package rip.ada.groups;

import io.javalin.Javalin;
import io.pebbletemplates.pebble.PebbleEngine;
import rip.ada.groups.report.ReportRegistry;
import rip.ada.groups.routes.*;
import rip.ada.groups.routes.advanced.*;
import rip.ada.groups.routes.auth.LogoutHandler;
import rip.ada.groups.routes.auth.gsuite.GsuiteOauthCallbackHandler;
import rip.ada.groups.routes.auth.gsuite.StartGsuiteOauthHandler;
import rip.ada.groups.routes.auth.wca.StartWcaOauthHandler;
import rip.ada.groups.routes.auth.wca.WcaOauthCallbackHandler;
import rip.ada.groups.routes.groups.GenerateGroupsHandler;
import rip.ada.groups.routes.groups.ManageGroupScheduleHandler;
import rip.ada.groups.routes.ukca.EventDistributionHandler;
import rip.ada.groups.routes.ukca.ScheduleImportHandler;
import rip.ada.groups.routes.ukca.UKCAHandler;
import rip.ada.groups.session.SessionHandler;
import rip.ada.groups.session.SessionRegistry;
import rip.ada.groups.templates.Templates;
import rip.ada.groups.wca.OauthApi;
import rip.ada.groups.wca.WcaApi;
import rip.ada.groups.wca.patcher.WcifPatchRequest;
import rip.ada.groups.wca.patcher.WcifPatcher;
import rip.ada.groups.wcalive.WcaLiveApi;
import rip.ada.groups.wcalive.WcaLiveData;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static rip.ada.groups.templates.Templates.renderStackTrace;

public class GroupsService {

    private final Config config;

    public GroupsService(final Config config) {
        this.config = config;
    }

    public void start() {
        final ScheduledExecutorService executorService = Executors.newScheduledThreadPool(4);
        final PebbleEngine engine = Templates.create();
        final SessionRegistry sessionRegistry = new SessionRegistry(new OauthApi(config));
        final ArrayBlockingQueue<WcifPatchRequest> wcifPatchRequests = new ArrayBlockingQueue<>(1000);
        final WcaApi wcaApi = new WcaApi(config, wcifPatchRequests);
        final ReportRegistry reportRegistry = new ReportRegistry();
        final WcaLiveApi wcaLiveApi = new WcaLiveApi();
        final WcaLiveData wcaLiveData = new WcaLiveData(wcaLiveApi);
        final WcifPatcher wcifPatcher = new WcifPatcher(wcifPatchRequests, wcaApi);
        executorService.execute(wcifPatcher);
        executorService.scheduleWithFixedDelay(wcaLiveData::refresh, 0, 30, TimeUnit.SECONDS);

        final ManageGroupScheduleHandler manageGroupScheduleHandler = new ManageGroupScheduleHandler(wcaApi, engine);

        final Javalin app = Javalin.create();

        app.before(new SessionHandler(sessionRegistry)::handle);

        app.get("/", new IndexHandler(engine));
        app.get("/logout", new LogoutHandler(sessionRegistry));
        app.get("/oauth/wca/start", new StartWcaOauthHandler(config));
        app.get("/oauth/wca/callback", new WcaOauthCallbackHandler(config, sessionRegistry));
        app.get("/oauth/gsuite/start", new StartGsuiteOauthHandler(config));
        app.get("/oauth/gsuite/callback", new GsuiteOauthCallbackHandler(config));
        app.post("/regions", new EventDistributionHandler(engine, wcaApi));
        app.get("/refreshCompetitions", new RefreshCompetitionsHandler());
        app.get("/{competition}", new CompetitionHandler(wcaApi, engine, wcaLiveData));
        app.get("/{competition}/manageGroupSchedule", manageGroupScheduleHandler);
        app.post("/{competition}/manageGroupSchedule", manageGroupScheduleHandler);
        app.post("/{competition}/print", new PrintScorecardsHandler(wcaApi, engine));
        app.get("/{competition}/scorecardPrinting", new PrintScorecardsMenuHandler(wcaApi, engine));
        app.get("/{competition}/reports", new ReportsHandler(wcaApi, engine, reportRegistry));
        app.get("/{competition}/ukca", new UKCAHandler(wcaApi, engine));
        app.post("/{competition}/ukca/scheduleImport", new ScheduleImportHandler(wcaApi, engine));
        app.get("/{competition}/reports/{report}", new ReportHandler(wcaApi, engine, reportRegistry));
        app.get("/{competition}/advanced", new AdvancedHandler(wcaApi, engine));
        app.post("/{competition}/advanced/wcifImport", new WcifImportHandler(wcaApi, engine));
        app.post("/{competition}/advanced/wcifExport", new WcifExportHandler(wcaApi));
        app.post("/{competition}/advanced/clearGroupSchedule", new ClearGroupScheduleHandler(wcaApi, engine));
        app.post("/{competition}/advanced/clearSchedule", new ClearScheduleHandler(wcaApi, engine));
        app.post("/{competition}/advanced/clearAssignments", new ClearAssignmentsHandler(wcaApi, engine));
        app.post("/{competition}/generateGroups", new GenerateGroupsHandler(wcaApi, engine));

        app.exception(Exception.class, (e, ctx) -> renderStackTrace(engine, ctx, e));

        app.start(8080);
    }
}
