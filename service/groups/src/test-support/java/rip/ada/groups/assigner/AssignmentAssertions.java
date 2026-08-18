package rip.ada.groups.assigner;

import rip.ada.wcif.*;
import rip.ada.wcif.event.OfficialEvent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

public class AssignmentAssertions {

    public static void assertAllCompetitorsHaveASlotInEventFirstRound(final Competition competition,
                                                                      final OfficialEvent event,
                                                                      final List<ProposedAssignment> proposedAssignments) {
        final List<String> errors = new ArrayList<>();
        for (final Person person : competition.getPersons()) {
            final Registration reg = person.registration();
            final boolean registeredForEvent = reg != null &&
                    reg.registrationStatus() == RegistrationStatus.ACCEPTED &&
                    reg.events().contains(event);

            final long assignmentCount = proposedAssignments
                    .stream()
                    .filter(assignment ->
                            Objects.equals(assignment.registrantId(), person.registrantId()) &&
                                    assignment.isCompetitor() &&
                                    competition.getActivityById(assignment.activityId()).getActivityCode().event() == event &&
                                    competition.getActivityById(assignment.activityId()).getActivityCode().round() == 1
                    )
                    .count();

            if (registeredForEvent && assignmentCount != 1) {
                errors.add(person.name() + " registered for " + event + " but has " + assignmentCount + " competing assignments in r1");
            } else if (!registeredForEvent && assignmentCount != 0) {
                errors.add(person.name() + " not registered for " + event + " but has " + assignmentCount + " competing assignments in r1");
            }
        }

        assertTrue(errors.isEmpty(), String.join("\n", errors));
    }

    public static void assertGroupsForRoundAreEvenlyDistributed(final Competition competition,
                                                                 final ActivityCode round,
                                                                 final List<ProposedAssignment> proposedAssignments) {
        final Map<Integer, AtomicInteger> competitorCountByGroup = new HashMap<>();

        for (final Activity group : competition.getGroups(round)) {
            competitorCountByGroup.put(group.getId(), new AtomicInteger());
        }

        for (final ProposedAssignment assignment : proposedAssignments) {
            final Activity activity = competition.getActivityById(assignment.activityId());

            if (!assignment.isCompetitor() ||
                    !activity.getActivityCode().roundOnly().equals(round)) {
                continue;
            }

            competitorCountByGroup
                    .get(assignment.activityId())
                    .incrementAndGet();
        }

        if (competitorCountByGroup.isEmpty()) {
            assertTrue(false, "No groups found for " + round);
            return;
        }

        final int minGroupSize = competitorCountByGroup.values().stream().mapToInt(AtomicInteger::intValue).min().orElse(0);
        final int maxGroupSize = competitorCountByGroup.values().stream().mapToInt(AtomicInteger::intValue).max().orElse(0);
        final int difference = maxGroupSize - minGroupSize;

        assertTrue(difference <= 1,
                "Groups for " + round + " are not evenly distributed. " +
                        "Min group size: " + minGroupSize + ", Max group size: " + maxGroupSize + ", Difference: " + difference);
    }

    public static void assertPlacement(final Competition competition,
                                       final ActivityCode round,
                                       final List<ProposedAssignment> proposedAssignments,
                                       final int registrantId,
                                       final int group) {
        for (final ProposedAssignment proposedAssignment : proposedAssignments) {
            if (proposedAssignment.registrantId() != registrantId || !proposedAssignment.isCompetitor()) {
                continue;
            }
            final Activity activity = competition.getActivityById(proposedAssignment.activityId());
            if (activity.getActivityCode().equals(round.group(group))) {
                return;
            }
        }
        fail("Did not find placement for " + registrantId + " in " + round.group(group));
    }
}
