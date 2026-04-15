package rip.ada.groups.report;

import rip.ada.wcif.Assignment;
import rip.ada.wcif.Competition;
import rip.ada.wcif.Person;
import rip.ada.wcif.StandardAssignmentCode;

import java.util.List;

public class HelpingStats implements ReportGenerator {
    @Override
    public String getDisplayName() {
        return "Helping Stat";
    }

    @Override
    public List<Report> generateReport(final Competition competition) {
        int noAssignments = 0;
        int moreThan10Assignments = 0;
        double ratio = 0;

        for (final Person person : competition.getCompetingPersons()) {
            int judging = 0;
            int running = 0;
            int scrambling = 0;
            int competing = 0;
            for (final Assignment assignment : person.assignments()) {
                switch (assignment.assignmentCode()) {
                    case StandardAssignmentCode.SCRAMBLER -> {
                        scrambling++;
                    }
                    case StandardAssignmentCode.JUDGE -> {
                        judging++;
                    }
                    case StandardAssignmentCode.RUNNER -> {
                        running++;
                    }
                    case StandardAssignmentCode.COMPETITOR -> {
                        competing++;
                    }
                    default -> {
                    }
                }
            }
            if (judging == 0 && running == 0 && scrambling == 0) {
                noAssignments++;
            } else if (judging + running + scrambling > 10) {
                moreThan10Assignments++;
            }
            ratio += competing / (double) (judging + running + scrambling);
        }
        return List.of(new Report("Helping Stats", List.of("Stat", "Value"), List.of(List.of("No Assignments", String.valueOf(noAssignments)), List.of("More than 10", String.valueOf(moreThan10Assignments)), List.of("Average ratio", String.valueOf(ratio / competition.getCompetingPersons().size())))));
    }
}
