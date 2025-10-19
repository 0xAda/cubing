package rip.ada.links.handlers;

import com.github.jknack.handlebars.Template;
import io.javalin.http.Context;
import io.javalin.http.Handler;
import rip.ada.links.Competition;
import rip.ada.links.Competitions;
import rip.ada.links.Sponsor;
import rip.ada.links.model.Link;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static rip.ada.links.handlers.TemplateRenderer.render;

public class CompetitionHandler implements Handler {

    private final Competitions competitions;
    private final Template template;

    public CompetitionHandler(final Competitions competitions, final Template template) {
        this.competitions = competitions;
        this.template = template;
    }

    @Override
    public void handle(final Context context) {
        final Competition competition = competitions.get(context.pathParam("competition"));
        if (competition == null) {
            context.redirect("/");
            return;
        }

        final List<Link> links = new ArrayList<>(List.of(
                new Link("WCA Live", "https://ugc.production.linktr.ee/42084225-aa45-42e9-85e9-42d5c73a76c9_image.png?io=true&size=thumbnail-stack_v1_0", "https://live.worldcubeassociation.org/link/competitions/" + competition.id()),
                new Link("Competing And Judging Groups", "https://static.ukca.org/CompetitionGroups_Logo.png", "https://competitiongroups.com/competitions/" + competition.id()),
                new Link("WCA Competition Website", "https://static.ukca.org/WCA_Logo.svg", "https://www.worldcubeassociation.org/competitions/" + competition.id()),
                new Link("UKCA", "https://static.ukca.org/UKCA_Logo.png", "https://ukca.org"),
                new Link("Upcoming Competitions", "https://static.ukca.org/UKCA_Logo.png", "https://wca.ukca.org")
        ));
        for (final Sponsor sponsor : competition.sponsor()) {
            links.addFirst(new Link(sponsor.getName(), sponsor.getLogo(), sponsor.getWebsite()));
        }
        render(template, context, Map.of("competitionName", competition.name(), "links", links));
    }

}
