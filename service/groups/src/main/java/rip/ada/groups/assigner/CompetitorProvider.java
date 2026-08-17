package rip.ada.groups.assigner;

import rip.ada.wcif.*;

import java.util.ArrayList;
import java.util.List;

public class CompetitorProvider {

    private final Competition competition;

    public CompetitorProvider(final Competition competition) {
        this.competition = competition;
    }

    public List<Person> getCompetitors(final ActivityCode round) {
        for (final Event event : competition.getEvents()) {
            for (final Round eventRound : event.rounds()) {
                if (!eventRound.activityCode().equals(round)) {
                    continue;
                }

                if (eventRound.participationRuleset().participationSource() instanceof RegistrationsParticipationSource) {
                    return competition.getPersonsRegisteredForEvent(event.eventType());
                }

                final List<Person> persons = new ArrayList<>();

                for (final Result result : eventRound.results()) {
                    final Person person = competition.getPersonById(result.personId());
                    persons.add(person);
                }

                return persons;
            }
        }
        return List.of();
    }
}
