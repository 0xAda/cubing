package rip.ada.groups.assigner;

import ai.timefold.solver.core.api.score.stream.test.ConstraintVerifier;
import org.junit.jupiter.api.Test;
import rip.ada.groups.assigner.model.AssignmentSolution;
import rip.ada.groups.assigner.model.CompetitorPlacementEntity;
import rip.ada.groups.ir.AssignableRoundId;
import rip.ada.groups.ir.AssignmentSlot;
import rip.ada.groups.ir.CompetitorId;
import rip.ada.groups.ir.RoundSet;
import rip.ada.groups.ir.TimeWindow;
import rip.ada.wcif.event.OfficialEvent;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class AssignmentConstraintsTest {

    @Test
    public void shouldHardPenaliseCompetitorWithOverlappingSlots() {
        final CompetitorId competitorId = new CompetitorId(1);
        final AssignableRoundId sixBySix = new AssignableRoundId(OfficialEvent.SIX_BY_SIX, 1);
        final AssignableRoundId sevenBySeven = new AssignableRoundId(OfficialEvent.SEVEN_BY_SEVEN, 1);

        ConstraintVerifier.build(new AssignmentConstraints(), AssignmentSolution.class, CompetitorPlacementEntity.class)
                .verifyThat(AssignmentConstraints::noOverlappingAssignments)
                .given(
                        placement(0, competitorId, sixBySix, slot(sixBySix, 100, 0)),
                        placement(1, competitorId, sevenBySeven, slot(sevenBySeven, 200, 10))
                )
                .penalizesBy(1);
    }

    @Test
    public void shouldNotPenaliseCompetitorWithNoOverlappingSlots() {
        final CompetitorId competitorId = new CompetitorId(1);
        final AssignableRoundId sixBySix = new AssignableRoundId(OfficialEvent.SIX_BY_SIX, 1);
        final AssignableRoundId sevenBySeven = new AssignableRoundId(OfficialEvent.SEVEN_BY_SEVEN, 1);

        ConstraintVerifier.build(new AssignmentConstraints(), AssignmentSolution.class, CompetitorPlacementEntity.class)
                .verifyThat(AssignmentConstraints::noOverlappingAssignments)
                .given(
                        placement(0, competitorId, sixBySix, slot(sixBySix, 100, 0)),
                        placement(1, competitorId, sevenBySeven, slot(sevenBySeven, 200, 30))
                )
                .hasNoImpact();
    }

    @Test
    public void shouldNotPenaliseCompetitorWithAdjacentSlots() {
        final CompetitorId competitorId = new CompetitorId(1);
        final AssignableRoundId sixBySix = new AssignableRoundId(OfficialEvent.SIX_BY_SIX, 1);
        final AssignableRoundId sevenBySeven = new AssignableRoundId(OfficialEvent.SEVEN_BY_SEVEN, 1);

        ConstraintVerifier.build(new AssignmentConstraints(), AssignmentSolution.class, CompetitorPlacementEntity.class)
                .verifyThat(AssignmentConstraints::noOverlappingAssignments)
                .given(
                        placement(0, competitorId, sixBySix, slot(sixBySix, 100, 0)),
                        placement(1, competitorId, sevenBySeven, slot(sevenBySeven, 200, 20))
                )
                .hasNoImpact();
    }

    @Test
    public void shouldNotPenaliseDifferentCompetitorWithOverlappingSlots() {
        final CompetitorId competitorId = new CompetitorId(1);
        final CompetitorId competitorId2 = new CompetitorId(2);
        final AssignableRoundId sixBySix = new AssignableRoundId(OfficialEvent.SIX_BY_SIX, 1);
        final AssignableRoundId sevenBySeven = new AssignableRoundId(OfficialEvent.SEVEN_BY_SEVEN, 1);

        ConstraintVerifier.build(new AssignmentConstraints(), AssignmentSolution.class, CompetitorPlacementEntity.class)
                .verifyThat(AssignmentConstraints::noOverlappingAssignments)
                .given(
                        placement(0, competitorId, sixBySix, slot(sixBySix, 100, 0)),
                        placement(1, competitorId2, sevenBySeven, slot(sevenBySeven, 200, 10))
                )
                .hasNoImpact();
    }

    private static CompetitorPlacementEntity placement(
            final long id,
            final CompetitorId competitorId,
            final AssignableRoundId roundId,
            final AssignmentSlot assignedSlot
    ) {
        final RoundSet roundSet = new RoundSet(Set.of(roundId), List.of(assignedSlot));
        return new CompetitorPlacementEntity(
                id,
                roundSet,
                competitorId,
                Set.of(roundId),
                roundSet.slots(),
                assignedSlot
        );
    }

    private static AssignmentSlot slot(
            final AssignableRoundId roundId,
            final int activityId,
            final int minutesAfterStart
    ) {
        final Instant start = Instant.parse("2050-01-01T10:00:00Z")
                .plus(Duration.ofMinutes(minutesAfterStart));
        return new AssignmentSlot(
                new TimeWindow(start, start.plus(Duration.ofMinutes(20))),
                Map.of(roundId, activityId)
        );
    }

}
