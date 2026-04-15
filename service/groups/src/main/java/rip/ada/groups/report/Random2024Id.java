package rip.ada.groups.report;

import rip.ada.wcif.Competition;
import rip.ada.wcif.Person;
import rip.ada.wcif.RegistrationStatus;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class Random2024Id implements ReportGenerator {
    @Override
    public String getDisplayName() {
        return "Random 2024 ID";
    }

    @Override
    public List<Report> generateReport(final Competition competition) {
        final List<Person> persons = new ArrayList<>();
        for (final Person person : competition.getPersons()) {
            if (person.registration() == null || person.registration().registrationStatus() != RegistrationStatus.ACCEPTED) {
                continue;
            }
            if (person.wcaId() == null) {
                persons.add(person);
                continue;
            }
            final int year = Integer.parseInt(person.wcaId().substring(0, 4));
            if (year >= 2024) {
                persons.add(person);
            }
        }
        final int index = ThreadLocalRandom.current().nextInt(0, persons.size());
        final Person person = persons.get(index);
        return List.of(new Report("Random 2024 ID", List.of("ID", "Name"), List.of(List.of(person.wcaId() == null ? "First Competition" : person.wcaId(), person.name()))));
    }
}
