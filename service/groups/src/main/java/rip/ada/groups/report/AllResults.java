package rip.ada.groups.report;

import rip.ada.wcif.Competition;
import rip.ada.wcif.Event;
import rip.ada.wcif.Result;
import rip.ada.wcif.Round;

import java.util.ArrayList;
import java.util.List;

public class AllResults implements ReportGenerator {
    @Override
    public String getDisplayName() {
        return "All Results";
    }

    @Override
    public List<Report> generateReport(final Competition competition) {
        final List<Report> reports = new ArrayList<>();
        for (final Event event : competition.getEvents()) {
            for (final Round round : event.rounds()) {
                final List<List<String>> results = new ArrayList<>();
                for (final Result result : round.results()) {
                    results.add(List.of(competition.getPersonById(result.personId()).wcaId(), String.valueOf(result.ranking()), String.valueOf(result.average()), String.valueOf(result.best())));
                }
                reports.add(new Report(round.activityCode().getDisplayName(), List.of("WCA id", "Ranking", "Average", "Best"), results));
            }
        }
        return reports;
    }
}
