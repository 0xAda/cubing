package rip.ada.groups.report;

import rip.ada.wcif.Competition;
import rip.ada.wcif.EventType;
import rip.ada.wcif.Person;
import rip.ada.wcif.RegistrationStatus;

import java.util.*;

public class CompetitorsPerEvent implements ReportGenerator {
    @Override
    public String getDisplayName() {
        return "Competitors Per Event";
    }

    @Override
    public List<Report> generateReport(final Competition competition) {
        final Report report = new Report("Competitors Per Event", List.of("Event", "Competitors"), new ArrayList<>());

        final Map<EventType, Integer> counts = new HashMap<>();
        for (final Person person : competition.getPersons()) {
            if (person.registration() == null || person.registration().registrationStatus() != RegistrationStatus.ACCEPTED) {
                continue;
            }

            for (final EventType event : person.registration().events()) {
                counts.put(event, counts.getOrDefault(event, 0) + 1);
            }
        }
        for (final Map.Entry<EventType, Integer> eventCount : counts.entrySet()) {
            report.rows().add(List.of(eventCount.getKey().getFriendlyName(), eventCount.getValue().toString()));
        }

        report.rows().sort(Comparator.comparing(List::getFirst));

        return List.of(report);
    }
}
