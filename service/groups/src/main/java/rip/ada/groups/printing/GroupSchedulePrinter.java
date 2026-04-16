package rip.ada.groups.printing;

import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.*;
import com.itextpdf.layout.properties.AreaBreakType;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.VerticalAlignment;
import rip.ada.wcif.*;
import rip.ada.wcif.event.ScheduleEvent;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.*;
import java.util.List;

public class GroupSchedulePrinter {

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("H:mm");

    public void printGroupSchedule(final Competition competition, final Document document) {
        final ZoneId zoneId = ZoneId.of(competition.getSchedule().getVenues().getFirst().getTimezone());

        for (int i = 0; i < competition.getSchedule().getNumberOfDays(); i++) {
            if (i != 0) {
                document.add(new AreaBreak(AreaBreakType.NEXT_PAGE));
            }
            final LocalDate date = competition.getSchedule().getStartDate().plusDays(i);

            final List<Activity> allActivitiesForDay = new ArrayList<>();
            for (final Venue venue : competition.getSchedule().getVenues()) {
                for (final Room room : venue.getRooms()) {
                    for (final Activity activity : room.activities()) {
                        if (activity.getStartTime().atZone(zoneId).toLocalDate().equals(date)) {
                            allActivitiesForDay.add(activity);
                        }
                    }
                }
            }
            allActivitiesForDay.sort(Comparator.comparing(Activity::getStartTime));

            final List<GroupScheduleEvent> events = new ArrayList<>();
            final Set<ActivityCode> processedRounds = new HashSet<>();
            boolean wsapPhotoInserted = false;

            for (final Activity activity : allActivitiesForDay) {
                final ActivityCode code = activity.getActivityCode();
                if (code.event() instanceof ScheduleEvent se) {
                    if (se == ScheduleEvent.AWARDS) {
                        events.add(new TextEvent("Dele Photo"));
                    }
                    events.add(new NonCompetingEvent(activity.getName(), activity.getStartTime().atZone(zoneId)));
                } else if (code.event().isCompetingEvent()) {
                    if (!processedRounds.contains(code)) {
                        processedRounds.add(code);
                        final boolean isFirstEvent = !wsapPhotoInserted;
                        if (isFirstEvent && i == 0) {
                            events.add(new TextEvent("WSAP Photo"));
                        }
                        wsapPhotoInserted = true;
                        events.add(buildCompetingEvent(competition, code, allActivitiesForDay, zoneId));
                        if (isFirstEvent) {
                            events.add(new TextEvent("Shoutout Sponsor"));
                        }
                    }
                }
            }
            events.add(new NonCompetingEvent("End", allActivitiesForDay.getLast().getEndTime().atZone(zoneId)));

            document.add(new Paragraph(new Text(competition.getName() + " " + date.getDayOfWeek().getDisplayName(TextStyle.FULL, Locale.UK) + " Schedule").setFontSize(24).setTextAlignment(TextAlignment.CENTER)).setTextAlignment(TextAlignment.CENTER));
            final Table table = new Table(8);
            table.useAllAvailableWidth();
            table.startNewRow();
            table.addHeaderCell("Event/Round");
            table.addHeaderCell("S");
            table.addHeaderCell("Printed");
            table.addHeaderCell("Progess");
            table.addHeaderCell("C/G");
            table.addHeaderCell("G");
            table.addHeaderCell("Time Start");
            table.addHeaderCell("Actual Time");

            for (final GroupScheduleEvent event : events) {
                event.addRows(table);
            }
            document.add(table);
        }
    }

    private CompetingEvent buildCompetingEvent(final Competition competition, final ActivityCode roundCode, final List<Activity> allActivitiesForDay, final ZoneId zoneId) {
        final List<Activity> roundActivities = allActivitiesForDay.stream()
                .filter(a -> a.getActivityCode().equals(roundCode))
                .toList();

        final int stages = roundActivities.size();

        final TreeMap<Integer, Activity> groupByNumber = new TreeMap<>();
        for (final Activity roomActivity : roundActivities) {
            for (final Activity child : roomActivity.getChildActivities()) {
                final Integer groupNum = child.getActivityCode().group();
                if (groupNum != null) {
                    groupByNumber.put(groupNum, child);
                }
            }
        }
        final int totalGroups = groupByNumber.size();

        final List<ZonedDateTime> startTimes = new ArrayList<>();
        for (int g = 1; g <= totalGroups; g++) {
            final Activity groupActivity = groupByNumber.get(g);
            final ZonedDateTime startTime = groupActivity != null
                    ? groupActivity.getStartTime().atZone(zoneId)
                    : roundActivities.getFirst().getStartTime().atZone(zoneId);
            startTimes.add(startTime);
        }

        if (startTimes.isEmpty()) {
            for (final Activity roundActivity : roundActivities) {
                startTimes.add(roundActivity.getStartTime().atZone(zoneId));
            }
        }

        final Event event = competition.getEvents().stream()
                .filter(e -> e.eventType().equals(roundCode.event()))
                .findFirst().orElse(null);
        final Round round = event != null ? event.rounds().stream()
                .filter(r -> Objects.equals(r.roundNumber(), roundCode.round()))
                .findFirst().orElse(null) : null;

        final String eventName = roundCode.event().getFriendlyName() + " Round " + roundCode.round();
        final String progress = getProgressString(round);
        final int competitorCount = calculateCompetitorCount(competition, roundCode, event);
        final int competitorsPerGroup = totalGroups > 0 ? competitorCount / totalGroups : 0;

        return new CompetingEvent(eventName, stages, progress, competitorsPerGroup, totalGroups, startTimes);
    }

    private String getProgressString(final Round round) {
        if (round == null || round.advancementCondition() == null) {
            return "";
        }
        if (round.advancementCondition() instanceof PercentAdvancementCondition(int percent)) {
            return percent + "%";
        }
        if (round.advancementCondition() instanceof RankingAdvancementCondition(int ranking)) {
            return String.valueOf(ranking);
        }
        return "";
    }

    private int calculateCompetitorCount(final Competition competition, final ActivityCode roundCode, final Event event) {
        final int round1Count = countRound1Competitors(competition, roundCode);
        if (roundCode.round() == 1) {
            return round1Count;
        }
        int count = round1Count;
        for (int r = 1; r < roundCode.round() && event != null; r++) {
            final int roundIndex = r - 1;
            if (roundIndex >= event.rounds().size()) {
                break;
            }
            final AdvancementCondition condition = event.rounds().get(roundIndex).advancementCondition();
            count = applyAdvancement(count, condition);
        }
        return count;
    }

    private int countRound1Competitors(final Competition competition, final ActivityCode roundCode) {
        int count = 0;
        for (final Person person : competition.getCompetingPersons()) {
            for (final Assignment assignment : person.assignments()) {
                if (assignment.assignmentCode() != StandardAssignmentCode.COMPETITOR) {
                    continue;
                }
                final Activity activity = competition.getActivityById(assignment.activityId());
                if (activity == null) {
                    continue;
                }
                final ActivityCode ac = activity.getActivityCode();
                if (ac.event().equals(roundCode.event()) && Objects.equals(ac.round(), 1) && ac.group() != null) {
                    count++;
                    break;
                }
            }
        }
        return count;
    }

    private int applyAdvancement(final int count, final AdvancementCondition condition) {
        if (condition instanceof PercentAdvancementCondition(int percent)) {
            return (int) Math.ceil(count * percent / 100.0);
        }
        if (condition instanceof RankingAdvancementCondition(int ranking)) {
            return Math.min(count, ranking);
        }
        return count;
    }

    private interface GroupScheduleEvent {
        void addRows(Table table);
    }

    private record TextEvent(String text) implements GroupScheduleEvent {
        @Override
        public void addRows(final Table table) {
            table.addCell(new Cell(1, 8).add(new Paragraph(new Text(text)).setTextAlignment(TextAlignment.RIGHT)));
        }
    }

    private record NonCompetingEvent(String event, ZonedDateTime time) implements GroupScheduleEvent {
        @Override
        public void addRows(final Table table) {
            table.addCell(new Cell(1, 6).add(new Paragraph(new Text(event)))).setTextAlignment(TextAlignment.LEFT);
            table.addCell(time.format(DATE_TIME_FORMATTER)).setTextAlignment(TextAlignment.CENTER);
            table.addCell("");
        }
    }

    private record CompetingEvent(String event, int stages, String progress, int competitorsPerGroup, int groups, List<ZonedDateTime> startTimes) implements GroupScheduleEvent {
        @Override
        public void addRows(final Table table) {
            final int height = Math.max(1, stages * groups);
            table.addCell(new Cell(height, 1).add(new Paragraph(new Text(event))).setVerticalAlignment(VerticalAlignment.MIDDLE));
            table.addCell(new Cell(height, 1).add(new Paragraph(new Text(String.valueOf(stages)))).setVerticalAlignment(VerticalAlignment.MIDDLE));
            table.addCell(new Cell(height, 1).add(new Paragraph(new Text(""))));
            table.addCell(new Cell(height, 1).add(new Paragraph(new Text(progress))).setTextAlignment(TextAlignment.CENTER).setVerticalAlignment(VerticalAlignment.MIDDLE));
            table.addCell(new Cell(height, 1).add(new Paragraph(new Text(String.valueOf(competitorsPerGroup)))).setTextAlignment(TextAlignment.CENTER).setVerticalAlignment(VerticalAlignment.MIDDLE));
            if (groups == 0) {
                table.addCell(new Cell().add(new Paragraph(new Text(String.valueOf(1)))).setTextAlignment(TextAlignment.CENTER).setVerticalAlignment(VerticalAlignment.MIDDLE));
                table.addCell(new Cell().add(new Paragraph(new Text(startTimes.getFirst().format(DATE_TIME_FORMATTER)))).setTextAlignment(TextAlignment.CENTER).setVerticalAlignment(VerticalAlignment.MIDDLE));
                table.addCell(new Cell().add(new Paragraph(new Text(""))));
                return;
            }
            for (int i = 0; i < groups; i++) {
                final boolean isFirstGroupInWave = i % stages == 0;
                table.addCell(new Cell().add(new Paragraph(new Text(String.valueOf(i + 1)))).setTextAlignment(TextAlignment.CENTER).setVerticalAlignment(VerticalAlignment.MIDDLE));
                if (isFirstGroupInWave) {
                    table.addCell(new Cell(stages, 1).add(new Paragraph(new Text(startTimes.get(i).format(DATE_TIME_FORMATTER)))).setTextAlignment(TextAlignment.CENTER).setVerticalAlignment(VerticalAlignment.MIDDLE));
                    table.addCell(new Cell(stages, 1).add(new Paragraph(new Text(""))));
                }
                table.startNewRow();
            }

        }
    }

}
