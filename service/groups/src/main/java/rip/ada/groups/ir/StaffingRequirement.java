package rip.ada.groups.ir;

import rip.ada.wcif.StandardAssignmentCode;

import java.util.Set;

public record StaffingRequirement(
        AssignmentSlot slot,
        StandardAssignmentCode assignmentCode,
        int requiredCount,
        Set<CompetitorId> eligiblePeople
) {
    public StaffingRequirement {
        eligiblePeople = Set.copyOf(eligiblePeople);
    }
}
