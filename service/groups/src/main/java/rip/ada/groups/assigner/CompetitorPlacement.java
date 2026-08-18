package rip.ada.groups.assigner;

import rip.ada.groups.ir.AssignableRoundId;
import rip.ada.groups.ir.AssignmentSlot;
import rip.ada.groups.ir.CompetitorId;
import rip.ada.wcif.StandardAssignmentCode;

import java.util.Comparator;
import java.util.List;
import java.util.Set;

public record CompetitorPlacement(
        CompetitorId competitorId,
        Set<AssignableRoundId> roundIds,
        AssignmentSlot slot
) {
    public CompetitorPlacement {
        roundIds = Set.copyOf(roundIds);
    }

    public List<ProposedAssignment> toProposedAssignments() {

        return roundIds.stream()
                .sorted(Comparator
                        .comparing((AssignableRoundId id) -> id.eventType().getEventId())
                        .thenComparingInt(AssignableRoundId::round)
                        .thenComparing(AssignableRoundId::attempt, Comparator.nullsFirst(Integer::compareTo)))
                .map(roundId -> new ProposedAssignment(
                        competitorId.registrantId(),
                        slot.activityIds().get(roundId),
                        StandardAssignmentCode.COMPETITOR
                ))
                .toList();
    }
}
