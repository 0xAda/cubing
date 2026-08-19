package rip.ada.groups.assigner.model;

import ai.timefold.solver.core.api.domain.common.PlanningId;
import ai.timefold.solver.core.api.domain.entity.PlanningEntity;
import ai.timefold.solver.core.api.domain.valuerange.ValueRangeProvider;
import ai.timefold.solver.core.api.domain.variable.PlanningVariable;
import rip.ada.groups.ir.AssignableRoundId;
import rip.ada.groups.ir.AssignmentSlot;
import rip.ada.groups.ir.CompetitorId;
import rip.ada.groups.ir.RoundSet;

import java.time.Instant;
import java.util.List;
import java.util.Set;

@PlanningEntity
public class CompetitorPlacementEntity {

    private long id;
    private RoundSet roundSet;
    private CompetitorId competitorId;
    private Set<AssignableRoundId> enteredRoundIds;
    private List<AssignmentSlot> eligibleSlots;
    private AssignmentSlot assignedSlot;

    public CompetitorPlacementEntity() {
        enteredRoundIds = Set.of();
        eligibleSlots = List.of();
    }

    public CompetitorPlacementEntity(
            final long id,
            final RoundSet roundSet,
            final CompetitorId competitorId,
            final Set<AssignableRoundId> enteredRoundIds,
            final List<AssignmentSlot> eligibleSlots
    ) {
        this.id = id;
        this.roundSet = roundSet;
        this.competitorId = competitorId;
        this.enteredRoundIds = enteredRoundIds;
        this.eligibleSlots = eligibleSlots;
    }

    public CompetitorPlacementEntity(
            final long id,
            final RoundSet roundSet,
            final CompetitorId competitorId,
            final Set<AssignableRoundId> enteredRoundIds,
            final List<AssignmentSlot> eligibleSlots,
            final AssignmentSlot assignedSlot
    ) {
        this(id, roundSet, competitorId, enteredRoundIds, eligibleSlots);
        this.assignedSlot = assignedSlot;
    }

    public RoundSet getRoundSet() {
        return roundSet;
    }

    public CompetitorId getCompetitorId() {
        return competitorId;
    }

    public Set<AssignableRoundId> getEnteredRoundIds() {
        return enteredRoundIds;
    }

    @PlanningId
    public long getId() {
        return id;
    }

    @ValueRangeProvider(id = "eligibleSlots")
    public List<AssignmentSlot> getEligibleSlots() {
        return eligibleSlots;
    }

    @PlanningVariable(valueRangeProviderRefs = "eligibleSlots")
    public AssignmentSlot getAssignedSlot() {
        return assignedSlot;
    }

    public Instant getStartTime() {
        return assignedSlot.timeWindow().start();
    }

    public Instant getEndTime() {
        return assignedSlot.timeWindow().end();
    }

    public void setAssignedSlot(final AssignmentSlot assignedSlot) {
        this.assignedSlot = assignedSlot;
    }
}
