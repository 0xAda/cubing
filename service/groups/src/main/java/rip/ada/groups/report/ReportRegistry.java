package rip.ada.groups.report;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ReportRegistry {

    private final Map<String, ReportGenerator> reports = new HashMap<>();

    public ReportRegistry() {
        reports.put("competitorsPerEvent", new CompetitorsPerEvent());
        reports.put("femalePodiums", new FemalePodiums());
        reports.put("groupSchedule", new GroupSchedule());
        reports.put("mostAssignments", new MostGroupAssignments());
        reports.put("random2024Id", new Random2024Id());
        reports.put("competitorAges", new CompetitorAges());
        reports.put("helpingStats", new HelpingStats());
        reports.put("badScramblers", new UnqualifiedScramblers());
        reports.put("allResults", new AllResults());
        reports.put("finalists", new Finalists());
    }

    public List<ReportName> getReportNames() {
        return List.copyOf(reports.keySet()).stream().map(slug -> new ReportName(reports.get(slug).getDisplayName(), slug)).toList();
    }

    public ReportGenerator getReport(final String name) {
        return reports.get(name);
    }
}
