package rip.ada.groups.assigner;

import rip.ada.groups.sorting.ActivityComparators;
import rip.ada.groups.sorting.CompetitorComparators;
import rip.ada.wcif.*;
import rip.ada.wcif.event.OfficialEvent;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public interface CompetitorPlacementStrategy {

    List<ProposedAssignment> placeCompetitorsForRound(Competition competition, CompetitorProvider competitorProvider, ActivityCode round);

    CompetitorPlacementStrategy RANDOM_PLACEMENT = (competition, competitorProvider, round) -> {
        final List<ProposedAssignment> proposedAssignments = new ArrayList<>();
        final List<Person> competitors = competitorProvider.getCompetitors(round);
        Collections.shuffle(competitors);

        roundRobin(competition, round, competitors, proposedAssignments);

        return proposedAssignments;
    };

    CompetitorPlacementStrategy SYMMETRIC = (competition, competitorProvider, round) -> {
        final List<ProposedAssignment> proposedAssignments = new ArrayList<>();
        final List<Person> competitors = competitorProvider.getCompetitors(round);
        competitors.sort(CompetitorComparators.sortByPersonalBest((OfficialEvent) round.event(), ResultType.AVERAGE));

        roundRobin(competition, round, competitors, proposedAssignments);

        return proposedAssignments;
    };

    CompetitorPlacementStrategy RANKED = (competition, competitorProvider, round) -> {
        final List<ProposedAssignment> proposedAssignments = new ArrayList<>();
        final List<Person> competitors = competitorProvider.getCompetitors(round);
        competitors.sort(CompetitorComparators.sortByPersonalBest((OfficialEvent) round.event(), ResultType.AVERAGE).reversed());

        final List<Activity> groups = competition.getGroups(round);
        groups.sort(ActivityComparators.START_TIME);

        final int groupSize = competitors.size() / groups.size();
        int added = 0;
        int groupIndex = 0;

        for (final Person competitor : competitors) {
            if (++added > groupSize) {
                groupIndex++;
                added = 0;
            }

            final Activity group = groups.get(groupIndex);
            proposedAssignments.add(new ProposedAssignment(
                    competitor.registrantId(),
                    group.getId(),
                    StandardAssignmentCode.COMPETITOR
            ));
        }

        return proposedAssignments;
    };

    private static void roundRobin(final Competition competition, final ActivityCode round, final List<Person> competitors, final List<ProposedAssignment> proposedAssignments) {
        final List<Activity> groups = competition.getGroups(round);

        for (int i = 0; i < competitors.size(); i++) {
            final Person person = competitors.get(i);
            final Activity group = groups.get(i % groups.size());
            proposedAssignments.add(new ProposedAssignment(
                    person.registrantId(),
                    group.getId(),
                    StandardAssignmentCode.COMPETITOR
            ));
        }
    }

}
