package rip.ada.groups.assigner;

import org.junit.jupiter.api.Test;
import rip.ada.groups.assigner.model.AssignmentSolution;
import rip.ada.groups.ir.IRCompetition;
import rip.ada.groups.ir.lift.WcifToIRLifter;
import rip.ada.groups.schedule.GroupScheduleGenerator;
import rip.ada.groups.schedule.ScheduleType;
import rip.ada.wcif.Competition;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static rip.ada.groups.assigner.Fixtures.getCompetition;

class AssignmentProblemFactoryTest {

    @Test
    public void shouldHandleARealComp() {
        final Competition competition = getCompetition("YorkSummer2026");
        new GroupScheduleGenerator().generate(competition, ScheduleType.GROUPS);
        final WcifToIRLifter lifter = new WcifToIRLifter(competition);
        final IRCompetition comp = lifter.lift();

        final AssignmentSolution problem = new AssignmentProblemFactory(CompetitorPlacementStrategy.SYMMETRIC)
                .create(comp);

        // dumb assertion but whatever it works i think
        assertFalse(problem.getCompetitorPlacements().isEmpty());
    }

}
