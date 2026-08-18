package rip.ada.groups.assigner;

import rip.ada.wcif.StandardAssignmentCode;

public record ProposedAssignment(int registrantId,
                                 int activityId,
                                 StandardAssignmentCode assignmentCode) {

    public boolean isCompetitor() {
        return assignmentCode == StandardAssignmentCode.COMPETITOR;
    }
}
