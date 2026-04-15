package rip.ada.groups.report;

import rip.ada.wcif.*;
import rip.ada.wcif.event.OfficialEvent;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class Finalists implements ReportGenerator {
    @Override
    public String getDisplayName() {
        return "Finalists";
    }

    @Override
    public List<Report> generateReport(final Competition competition) {
        final List<Report> reports = new ArrayList<>();
        for (final Event event : competition.getEvents()) {
            for (final Round round : event.rounds()) {
                if (round.advancementCondition() == null) {
                    if (round.roundNumber() == 1) {
                        continue;
                    }
                    boolean resultsEntered = false;
                    for (final Result result : round.results()) {
                        if (!result.attempts().isEmpty()) {
                            resultsEntered = true;
                            break;
                        }
                    }
                    if (resultsEntered) {
                        continue;
                    }
                    final List<List<String>> rows = new ArrayList<>();
                    for (final Result result : round.results()) {
                        rows.add(List.of(competition.getPersonById(result.personId()).name(), String.valueOf(getRanking(competition.getPersonById(result.personId()), competition, (OfficialEvent) round.event(), round.roundNumber()))));
                    }
                    rows.sort(Comparator.comparingInt(list -> Integer.parseInt(list.get(1))));
                    if (rows.isEmpty()) {
                        continue;
                    }
                    reports.add(new Report(round.activityCode().getDisplayName(), List.of("Name", "Seed"), rows));
                }
            }
        }
        return reports;
    }

    private int getRanking(final Person person, final Competition competition, final OfficialEvent event, final int round) {
        for (final Event competitionEvent : competition.getEvents()) {
            for (final Round round1 : competitionEvent.rounds()) {
                if (round1.event() == event) {
                    if (round1.roundNumber() == round - 1) {
                        for (final Result result : round1.results()) {
                            if (result.personId() == person.registrantId()) {
                                return result.ranking();
                            }
                        }
                    }
                }
            }
        }
        throw new RuntimeException("No ranking for " + person.name() + " (" + person.wcaId() + ")");
    }
}
