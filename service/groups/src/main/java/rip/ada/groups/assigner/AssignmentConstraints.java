package rip.ada.groups.assigner;

import ai.timefold.solver.core.api.score.HardSoftScore;
import ai.timefold.solver.core.api.score.stream.*;
import rip.ada.groups.assigner.model.CompetitorPlacementEntity;

public class AssignmentConstraints implements ConstraintProvider {

    @Override
    public Constraint[] defineConstraints(final ConstraintFactory constraintFactory) {
        return new Constraint[]{
                balanced(constraintFactory),
                noOverlappingAssignments(constraintFactory)
        };
    }

    Constraint balanced(final ConstraintFactory constraintFactory) {
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

    Constraint noOverlappingAssignments(final ConstraintFactory constraintFactory) {
        return constraintFactory.forEachUniquePair(CompetitorPlacementEntity.class,
                Joiners.equal(CompetitorPlacementEntity::getCompetitorId),
                Joiners.overlapping(CompetitorPlacementEntity::getStartTime, CompetitorPlacementEntity::getEndTime))
                .penalize(HardSoftScore.ONE_HARD)
                .asConstraint("No overlapping assignments");
    }
}
