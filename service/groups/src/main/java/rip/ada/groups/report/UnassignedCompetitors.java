package rip.ada.groups.report;

import rip.ada.wcif.Competition;

import java.util.List;

public class UnassignedCompetitors implements ReportGenerator {
    @Override
    public String getDisplayName() {
        return "Unassigned Competitors";
    }

    @Override
    public List<Report> generateReport(final Competition competition) {

        return List.of();
    }
}
