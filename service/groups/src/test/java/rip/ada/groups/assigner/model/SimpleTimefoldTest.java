package rip.ada.groups.assigner.model;

import ai.timefold.solver.core.api.score.HardSoftScore;
import ai.timefold.solver.core.api.score.stream.Constraint;
import ai.timefold.solver.core.api.score.stream.ConstraintCollectors;
import ai.timefold.solver.core.api.score.stream.ConstraintFactory;
import ai.timefold.solver.core.api.score.stream.ConstraintProvider;
import ai.timefold.solver.core.api.solver.SolverFactory;
import ai.timefold.solver.core.config.solver.SolverConfig;
import org.junit.jupiter.api.Test;
import rip.ada.groups.ir.*;
import rip.ada.wcif.event.OfficialEvent;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class SimpleTimefoldTest {

    @Test
    public void foldTime() {
        final AssignableRoundId roundId = new AssignableRoundId(OfficialEvent.THREE_BY_THREE, 1);
        final AssignmentSlot firstSlot = new AssignmentSlot(new TimeWindow(Instant.parse("2026-08-09T09:00:00Z"), Instant.parse("2026-08-09T10:00:00Z")), Map.of(roundId, 1));
        final AssignmentSlot secondSlot = new AssignmentSlot(new TimeWindow(Instant.parse("2026-08-09T10:00:00Z"), Instant.parse("2026-08-09T12:00:00Z")), Map.of(roundId, 1));
        final RoundSet roundSet = new RoundSet(
                Set.of(roundId),
                List.of(firstSlot, secondSlot)
        );

        final List<CompetitorPlacementEntity> placements =
                IntStream.range(0, 4)
                        .mapToObj(index -> new CompetitorPlacementEntity(
                                roundSet,
                                new CompetitorId(index + 1),
                                Set.of(roundId),
                                roundSet.slots(),
                                firstSlot
                        ))
                        .toList();

        final AssignmentSolution problem =
                new AssignmentSolution(placements);

        final SolverConfig solverConfig = new SolverConfig()
                .withSolutionClass(AssignmentSolution.class)
                .withEntityClasses(CompetitorPlacementEntity.class)
                .withConstraintProviderClass(AssignmentConstraints.class)
                .withRandomSeed(1L)
                .withTerminationSpentLimit(Duration.ofSeconds(1));

        final AssignmentSolution solution = SolverFactory.<AssignmentSolution>create(solverConfig)
                .buildSolver()
                .solve(problem);

        final Map<AssignmentSlot, Long> counts =
                solution.getCompetitorPlacements().stream()
                        .collect(Collectors.groupingBy(
                                CompetitorPlacementEntity::getAssignedSlot,
                                Collectors.counting()
                        ));

        assertEquals(2, counts.get(firstSlot));
        assertEquals(2, counts.get(secondSlot));
        assertEquals(HardSoftScore.ofSoft(-8), solution.getScore());
    }

    public static class AssignmentConstraints implements ConstraintProvider {

        @Override
        public Constraint[] defineConstraints(final ConstraintFactory constraintFactory) {
            return new Constraint[]{
                    balanced(constraintFactory)
            };
        }

        private Constraint balanced(final ConstraintFactory constraintFactory) {
            return constraintFactory.forEach(CompetitorPlacementEntity.class)
                    .groupBy(
                            CompetitorPlacementEntity::getRoundSet,
                            CompetitorPlacementEntity::getAssignedSlot,
                            ConstraintCollectors.count()
                    )
                    .penalize(
                            HardSoftScore.ONE_SOFT,
                            ((roundSet, assignmentSlot, count) -> count * count)
                    )
                    .asConstraint("Balanced competitor placements");
        }
    }

}
