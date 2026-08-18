package rip.ada.groups.assigner.model;

import ai.timefold.solver.core.api.domain.solution.PlanningEntityCollectionProperty;
import ai.timefold.solver.core.api.domain.solution.PlanningScore;
import ai.timefold.solver.core.api.domain.solution.PlanningSolution;
import ai.timefold.solver.core.api.score.HardSoftScore;

import java.util.List;

@PlanningSolution
public class AssignmentSolution {

    private List<CompetitorPlacementEntity> competitorPlacements;
    private HardSoftScore score;

    public AssignmentSolution() {
    }

    public AssignmentSolution(final List<CompetitorPlacementEntity> competitorPlacements) {
        this.competitorPlacements = competitorPlacements;
    }

    @PlanningEntityCollectionProperty
    public List<CompetitorPlacementEntity> getCompetitorPlacements() {
        return competitorPlacements;
    }

    @PlanningScore
    public HardSoftScore getScore() {
        return score;
    }

    public void setCompetitorPlacements(final List<CompetitorPlacementEntity> competitorPlacements) {
        this.competitorPlacements = competitorPlacements;
    }

    public void setScore(final HardSoftScore score) {
        this.score = score;
    }

}
