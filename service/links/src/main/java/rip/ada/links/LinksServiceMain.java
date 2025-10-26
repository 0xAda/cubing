package rip.ada.links;

import com.github.jknack.handlebars.Handlebars;
import com.github.jknack.handlebars.io.ClassPathTemplateLoader;
import com.github.jknack.handlebars.io.TemplateLoader;
import io.javalin.Javalin;
import rip.ada.links.handlers.APIListHandler;
import rip.ada.links.handlers.CompetitionHandler;
import rip.ada.links.handlers.IndexHandler;
import rip.ada.links.handlers.SponsoredListHandler;
import rip.ada.wca.UnauthenticatedWcaApi;
import rip.ada.wca.WcaApiConfig;
import rip.ada.wcascraper.WcaScraper;

import java.io.IOException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class LinksServiceMain {

    static void main() throws IOException {
        final WcaScraper wcaScraper = new WcaScraper();
        final WcaApiConfig wcaApiConfig = WcaApiConfig.unauthenticatedDefault();
        final UnauthenticatedWcaApi wcaApi = new UnauthenticatedWcaApi(wcaApiConfig);

        final TemplateLoader templateLoader = new ClassPathTemplateLoader();
        templateLoader.setPrefix("/templates");
        templateLoader.setSuffix(".html");
        final Handlebars handlebars = new Handlebars(templateLoader);

        final ScheduledExecutorService executor = Executors.newScheduledThreadPool(2);

        final Competitions competitions = new Competitions(wcaScraper, wcaApi);

        executor.scheduleAtFixedRate(competitions::fetchCompetitions, 0, 30, TimeUnit.MINUTES);

        final IndexHandler indexHandler = new IndexHandler(competitions, handlebars.compile("list_comps"));
        final SponsoredListHandler sponsoredListHandler = new SponsoredListHandler(competitions);
        final APIListHandler apiListHandler = new APIListHandler(competitions);
        final CompetitionHandler competitionHandler = new CompetitionHandler(competitions, handlebars.compile("competition_links"));
        Javalin.create()
                .get("/", indexHandler)
                .get("/competition/{competition}", competitionHandler)
                .get("/api/sponsoredby/{sponsor}", sponsoredListHandler)
                .get("/api/all", apiListHandler)
                .start("0.0.0.0", 6002);
    }

}
