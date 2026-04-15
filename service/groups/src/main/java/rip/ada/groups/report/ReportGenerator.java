package rip.ada.groups.report;

import rip.ada.wcif.Competition;

import java.util.List;

public interface ReportGenerator {

    String getDisplayName();

    List<Report> generateReport(Competition competition);

}
