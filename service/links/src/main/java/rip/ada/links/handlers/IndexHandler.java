package rip.ada.links.handlers;

import com.github.jknack.handlebars.Template;
import io.javalin.http.Context;
import io.javalin.http.Handler;
import rip.ada.links.Competition;
import rip.ada.links.Competitions;

import java.util.List;
import java.util.Map;

import static rip.ada.links.handlers.TemplateRenderer.render;

public class IndexHandler implements Handler {

    private final Competitions competitions;
    private final Template template;

    public IndexHandler(final Competitions competitions, final Template template) {
        this.competitions = competitions;
        this.template = template;
    }

    @Override
    public void handle(final Context context) {
        final List<Competition> currentCompetitions = competitions.getLiveCompetitions();
        if (currentCompetitions.size() == 1) {
            context.redirect("/competition/" + currentCompetitions.getFirst().id());
        } else {
            render(template, context, Map.of("competitions", currentCompetitions));
        }
    }
}
