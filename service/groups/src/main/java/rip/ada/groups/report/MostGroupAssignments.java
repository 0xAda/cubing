package rip.ada.groups.report;

import rip.ada.wcif.*;

import java.util.*;

public class MostGroupAssignments implements ReportGenerator {
    @Override
    public String getDisplayName() {
        return "Most Group Assignments";
    }

    @Override
    public List<Report> generateReport(final Competition competition) {
        final List<AssignmentCode> assignmentCodes = new ArrayList<>();

        for (final Person person : competition.getPersons()) {
            for (final Assignment assignment : person.assignments()) {
                if (!assignmentCodes.contains(assignment.assignmentCode())) {
                    assignmentCodes.add(assignment.assignmentCode());
                }
            }
        }

        final List<List<String>> rows = new ArrayList<>();
        for (final Person person : competition.getPersons()) {
            if (person.registration() == null || person.registration().registrationStatus() != RegistrationStatus.ACCEPTED) {
                continue;
            }
            final Map<AssignmentCode, Integer> assignments = new HashMap<>();
            int total = 0;
            int totalStaff = 0;
            for (final Assignment assignment : person.assignments()) {
                assignments.put(assignment.assignmentCode(), assignments.getOrDefault(assignment.assignmentCode(), 0) + 1);
                if (assignment.assignmentCode() != StandardAssignmentCode.COMPETITOR) {
                    totalStaff++;
                }
                total++;
            }
            final List<String> row = new ArrayList<>();
            row.add(person.name());
            assignmentCodes.forEach(assignmentCode -> row.add(String.valueOf(assignments.getOrDefault(assignmentCode, 0))));
            row.add(String.valueOf(total));
            row.add(String.valueOf(totalStaff));
            rows.add(row);
        }

        rows.sort(Comparator.comparingInt((List<String> row) -> Integer.parseInt(row.get(row.size() - 2))).reversed());

        final List<String> headers = new ArrayList<>();
        headers.add("Name");
        for (final AssignmentCode assignmentCode : assignmentCodes) {
            headers.add(assignmentCode.asString());
        }
        headers.add("Total");
        headers.add("Total Staff");
        return List.of(new Report("Most Group Assignments", headers, rows));
    }
}
