package rip.ada.groups.assigner;

import org.junit.jupiter.api.Test;
import rip.ada.groups.schedule.GroupScheduleGenerator;
import rip.ada.groups.schedule.ScheduleType;
import rip.ada.wcif.ActivityCode;
import rip.ada.wcif.Competition;
import rip.ada.wcif.event.OfficialEvent;

import java.util.List;

import static rip.ada.groups.assigner.AssignmentAssertions.*;
import static rip.ada.groups.assigner.Fixtures.getCompetition;

public class CompetitorPlacementStrategyTest {

    public static final ActivityCode ROUND_ONE_333 = ActivityCode.round(OfficialEvent.THREE_BY_THREE, 1);

    @Test
    public void randomPlacementShouldEvenlyDistributeGroups() {
        final Competition competition = getCompetition("YorkSummer2026");
        new GroupScheduleGenerator().generate(competition, ScheduleType.GROUPS);
        final CompetitorPlacementStrategy random = CompetitorPlacementStrategy.RANDOM_PLACEMENT;
        final List<ProposedAssignment> proposedAssignments = random.placeCompetitorsForRound(competition, ROUND_ONE_333);

        assertAllCompetitorsHaveASlotInEventFirstRound(competition, OfficialEvent.THREE_BY_THREE, proposedAssignments);

        assertGroupsForRoundAreEvenlyDistributed(competition, ROUND_ONE_333, proposedAssignments);
    }

    @Test
    public void rankedPlacement() {
        final Competition competition = getCompetition("YorkSummer2026");
        new GroupScheduleGenerator().generate(competition, ScheduleType.GROUPS);
        final CompetitorPlacementStrategy random = CompetitorPlacementStrategy.RANKED;
        final List<ProposedAssignment> proposedAssignments = random.placeCompetitorsForRound(competition, ROUND_ONE_333);

        assertAllCompetitorsHaveASlotInEventFirstRound(competition, OfficialEvent.THREE_BY_THREE, proposedAssignments);

        assertGroupsForRoundAreEvenlyDistributed(competition, ROUND_ONE_333, proposedAssignments);

        assertPlacement(competition, ROUND_ONE_333, proposedAssignments, 21, 1); //Newcomer with no result goes in first group
        assertPlacement(competition, ROUND_ONE_333, proposedAssignments, 4, 4); //I am not fast, group before fast group
        assertPlacement(competition, ROUND_ONE_333, proposedAssignments, 9, 5); //Narch is fast, fast group
    }

    @Test
    public void symmetricPlacement() {
        final Competition competition = getCompetition("YorkSummer2026");
        new GroupScheduleGenerator().generate(competition, ScheduleType.GROUPS);
        final CompetitorPlacementStrategy random = CompetitorPlacementStrategy.SYMMETRIC;
        final List<ProposedAssignment> proposedAssignments = random.placeCompetitorsForRound(competition, ROUND_ONE_333);

        assertAllCompetitorsHaveASlotInEventFirstRound(competition, OfficialEvent.THREE_BY_THREE, proposedAssignments);

        assertGroupsForRoundAreEvenlyDistributed(competition, ROUND_ONE_333, proposedAssignments);

        assertPlacement(competition, ROUND_ONE_333, proposedAssignments, 9, 1);
        assertPlacement(competition, ROUND_ONE_333, proposedAssignments, 97, 2);
        assertPlacement(competition, ROUND_ONE_333, proposedAssignments, 25, 3);
        assertPlacement(competition, ROUND_ONE_333, proposedAssignments, 7, 4);
        assertPlacement(competition, ROUND_ONE_333, proposedAssignments, 89, 5);
    }

}
