package rip.ada.groups.report;

import rip.ada.wcif.Activity;
import rip.ada.wcif.Competition;
import rip.ada.wcif.Room;
import rip.ada.wcif.Venue;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class GroupSchedule implements ReportGenerator {
    @Override
    public String getDisplayName() {
        return "Group Schedule";
    }

    @Override
    public List<Report> generateReport(final Competition competition) {
        final List<Report> reports = new ArrayList<>();
        for (final Venue venue : competition.getSchedule().getVenues()) {
            for (final Room room : venue.getRooms()) {
                final List<List<String>> rows = new ArrayList<>();
                final Report report = new Report(room.name(), List.of("Start Time", "End Time", "Event"), rows);
                room.activities().stream().sorted(Comparator.comparing(Activity::getStartTime)).forEach(activity -> {
                    if (activity.getChildActivities().isEmpty()) {
                        rows.add(List.of(activity.getStartTime().toString(), activity.getEndTime().toString(), activity.getActivityCode().getDisplayName()));
                    }
                    for (final Activity childActivity : activity.getChildActivities()) {
                        rows.add(List.of(childActivity.getStartTime().toString(), childActivity.getEndTime().toString(), childActivity.getActivityCode().getDisplayName()));
                    }
                });
                reports.add(report);
            }
        }
        return reports;
    }
}
