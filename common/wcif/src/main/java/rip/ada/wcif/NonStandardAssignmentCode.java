package rip.ada.wcif;

public record NonStandardAssignmentCode(String assignmentCode) implements AssignmentCode {

    @Override
    public String asString() {
        return assignmentCode;
    }
}
