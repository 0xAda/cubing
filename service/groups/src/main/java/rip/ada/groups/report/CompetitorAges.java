package rip.ada.groups.report;

import rip.ada.wcif.Competition;
import rip.ada.wcif.Person;
import rip.ada.wcif.RegistrationStatus;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class CompetitorAges implements ReportGenerator {
    @Override
    public String getDisplayName() {
        return "Competitor Ages";
    }

    @Override
    public List<Report> generateReport(final Competition competition) {
        int min = Integer.MAX_VALUE;
        String minName = "";
        int max = Integer.MIN_VALUE;
        String maxName = "";
        int total = 0;
        final List<Integer> all = new ArrayList<>();
        for (final Person person : competition.getPersons()) {
            if (person.registration() == null || person.registration().registrationStatus() != RegistrationStatus.ACCEPTED) {
                continue;
            }
            final int age = person.birthdate().until(LocalDate.now()).getYears();
            all.add(age);
            if (age < min) {
                min = age;
                minName = person.name();
            }
            if (age > max) {
                max = age;
                maxName = person.name();
            }
            total += age;
        }
        all.sort(Integer::compareTo);

        return List.of(new Report("Competitor Ages", List.of(
                "Stat", "Age", "Competitor"
        ),
                List.of(
                        List.of("Youngest", String.valueOf(min), minName),
                        List.of("Oldest", String.valueOf(max), maxName),
                        List.of("Mean", String.valueOf(total / all.size()), ""),
                        List.of("Median", String.valueOf(all.get(all.size() / 2)), "")
                )));
    }
}
