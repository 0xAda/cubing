package rip.ada.links.handlers;

import com.github.jknack.handlebars.Template;
import io.javalin.http.Context;
import io.javalin.http.Handler;
import rip.ada.links.Competition;
import rip.ada.links.Competitions;

import java.time.LocalDate;
import java.util.ArrayList;
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
        final LocalDate localDate = LocalDate.now();
        final List<Competition> currentCompetitions = new ArrayList<>();
        for (final Competition competition : competitions.getAll()) {
            final LocalDate startDate = competition.startDate();
            if ((startDate.isBefore(localDate) || startDate.isEqual(localDate)) && localDate.plusDays(7).isAfter(startDate)) {
                currentCompetitions.add(competition);
            }
        }
        if (currentCompetitions.size() == 1) {
            context.redirect("/competition/" + currentCompetitions.getFirst().id());
        } else {
            render(template, context, Map.of("competitions", currentCompetitions));
        }
    }
}
