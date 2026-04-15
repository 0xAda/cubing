package rip.ada.groups.report;

import rip.ada.wcif.*;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class FemalePodiums implements ReportGenerator {
    @Override
    public String getDisplayName() {
        return "Female Podiums";
    }

    @Override
    public List<Report> generateReport(final Competition competition) {
        final List<Report> reports = new ArrayList<>();

        for (final Event event : competition.getEvents()) {
            final Round finalRound = event.rounds().getLast();

            final List<Result> femaleResults = finalRound.results().stream()
                    .filter(r -> {
                        final Person competitor = competition.getPersons().stream().filter(person -> person.registrantId() == r.personId()).findFirst().orElse(null);
                        return competitor != null && competitor.gender() == Gender.FEMALE;
                    })
                    .sorted(Comparator.comparingInt(Result::ranking))
                    .limit(3)
                    .toList();

            final List<List<String>> podiums = new ArrayList<>();
            for (final Result femaleResult : femaleResults) {
                podiums.add(List.of(String.valueOf(femaleResult.ranking()), competition.getPersons().stream().filter(person -> person.registrantId() == femaleResult.personId()).findFirst().get().name(), femaleResult.average().toString(), femaleResult.best().toString()));
            }

            reports.add(new Report(event.eventType().getFriendlyName(), List.of("Position", "Name", "Average", "Single"), podiums));
        }
        return reports;
    }
}
