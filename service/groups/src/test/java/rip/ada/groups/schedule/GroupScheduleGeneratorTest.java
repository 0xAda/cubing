package rip.ada.groups.schedule;

import org.junit.jupiter.api.Test;
import rip.ada.wcif.*;
import rip.ada.wcif.event.OfficialEvent;

import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class GroupScheduleGeneratorTest {

    @Test
    public void shouldGenerateSimpleGroupSchedule() {
        final GroupScheduleGenerator groupScheduleGenerator = new GroupScheduleGenerator();
        final Competition testComp = competition(schedule(venue(
                        "Venue",
                        room(
                                "Room 1",
                                "red",
                                activity("333-r1", "10:00:00", "10:40:00")
                        )
                )),
                event(OfficialEvent.THREE_BY_THREE,
                        round("333-r1", 2)));
        groupScheduleGenerator.generate(testComp, ScheduleType.GROUPS);

        assertGroupSchedule(testComp, "Room 1", OfficialEvent.THREE_BY_THREE, 1,
                group(1, "10:00:00", "10:20:00"),
                group(2, "10:20:00", "10:40:00")
        );
    }

    @Test
    public void shouldGenerateSimultaneousMultiStageGroups() {
        final GroupScheduleGenerator groupScheduleGenerator = new GroupScheduleGenerator();
        final Competition testComp = competition(schedule(venue(
                        "Venue",
                        room(
                                "Room 1",
                                "red",
                                activity("333-r1", "10:00:00", "10:40:00")
                        ),
                        room(
                                "Room 2",
                                "red",
                                activity("333-r1", "10:00:00", "10:40:00")
                        )
                )),
                event(OfficialEvent.THREE_BY_THREE,
                        round("333-r1", 2)));
        groupScheduleGenerator.generate(testComp, ScheduleType.WAVES);

        assertGroupSchedule(testComp, "Room 1", OfficialEvent.THREE_BY_THREE, 1,
                group(1, "10:00:00", "10:20:00"),
                group(2, "10:20:00", "10:40:00")
        );
        assertGroupSchedule(testComp, "Room 2", OfficialEvent.THREE_BY_THREE, 1,
                group(1, "10:00:00", "10:20:00"),
                group(2, "10:20:00", "10:40:00")
        );
    }

    @Test
    public void shouldGenerateSimultaneousMultiStageGroupsWithDifferentEndTimes() {
        final GroupScheduleGenerator groupScheduleGenerator = new GroupScheduleGenerator();
        final Competition testComp = competition(schedule(venue(
                        "Venue",
                        room(
                                "Room 1",
                                "red",
                                activity("333-r1", "10:00:00", "10:40:00")
                        ),
                        room(
                                "Room 2",
                                "red",
                                activity("333-r1", "10:00:00", "11:00:00")
                        )
                )),
                event(OfficialEvent.THREE_BY_THREE,
                        round("333-r1", 3)));
        groupScheduleGenerator.generate(testComp, ScheduleType.WAVES);

        assertGroupSchedule(testComp, "Room 1", OfficialEvent.THREE_BY_THREE, 1,
                group(1, "10:00:00", "10:20:00"),
                group(2, "10:20:00", "10:40:00")
        );
        assertGroupSchedule(testComp, "Room 2", OfficialEvent.THREE_BY_THREE, 1,
                group(1, "10:00:00", "10:20:00"),
                group(2, "10:20:00", "10:40:00"),
                group(3, "10:40:00", "11:00:00")
        );
    }

    @Test
    public void shouldGenerateSimultaneousMultiStageGroupsWithDifferentStartTimes() {
        final GroupScheduleGenerator groupScheduleGenerator = new GroupScheduleGenerator();
        final Competition testComp = competition(schedule(venue(
                        "Venue",
                        room(
                                "Room 1",
                                "red",
                                activity("333-r1", "10:00:00", "10:40:00")
                        ),
                        room(
                                "Room 2",
                                "red",
                                activity("333-r1", "10:20:00", "11:00:00")
                        )
                )),
                event(OfficialEvent.THREE_BY_THREE,
                        round("333-r1", 3)));
        groupScheduleGenerator.generate(testComp, ScheduleType.WAVES);

        assertGroupSchedule(testComp, "Room 1", OfficialEvent.THREE_BY_THREE, 1,
                group(1, "10:00:00", "10:20:00"),
                group(2, "10:20:00", "10:40:00")
        );
        assertGroupSchedule(testComp, "Room 2", OfficialEvent.THREE_BY_THREE, 1,
                group(1, "10:20:00", "10:40:00"),
                group(2, "10:40:00", "11:00:00")
        );
    }

    @Test
    public void shouldGenerateUKC2025Schedule() {
        final GroupScheduleGenerator groupScheduleGenerator = new GroupScheduleGenerator();
        final Competition testComp = competition(schedule(venue(
                        "Venue",
                        room(
                                "Blue Stage",
                                "blue",
                                activity("clock-r1", "09:15:00", "10:22:30"),
                                activity("sq1-r1", "10:22:30", "10:45:00"),
                                activity("555-r1", "10:45:00", "12:10:00"),
                                activity("777-r1", "12:10:00", "12:40:00"),
                                activity("minx-r1", "13:40:00", "14:35:00"),
                                activity("clock-r2", "14:35:00", "15:00:00"),
                                activity("333bf-r1", "15:00:00", "15:40:00"),
                                activity("444-r1", "15:40:00", "16:25:00"),
                                activity("666-r1", "16:25:00", "17:15:00")
                        ),
                        room(
                                "Red Stage",
                                "red",
                                activity("clock-r1", "09:15:00", "10:00:00"),
                                activity("sq1-r1", "10:00:00", "10:45:00"),
                                activity("555-r1", "10:45:00", "11:40:00"),
                                activity("777-r1", "11:40:00", "12:40:00"),
                                activity("minx-r1", "13:40:00", "14:35:00"),
                                activity("clock-r2", "14:35:00", "15:00:00"),
                                activity("333bf-r1", "15:00:00", "15:40:00"),
                                activity("444-r1", "15:40:00", "16:25:00"),
                                activity("666-r1", "16:25:00", "17:15:00"),
                                activity("clock-r3", "17:15:00", "17:40:00"),
                                activity("777-r2", "17:40:00", "18:10:00"),
                                activity("sq1-r2", "18:10:00", "18:35:00"),
                                activity("555-r2", "18:35:00", "19:00:00")
                        ),
                        room(
                                "Yellow Stage",
                                "yellow",
                                activity("clock-r1", "09:15:00", "10:00:00"),
                                activity("sq1-r1", "10:00:00", "10:45:00"),
                                activity("555-r1", "10:45:00", "11:40:00"),
                                activity("777-r1", "11:40:00", "12:40:00"),
                                activity("minx-r1", "13:40:00", "14:35:00"),
                                activity("clock-r2", "14:35:00", "15:00:00"),
                                activity("444-r1", "15:20:00", "16:50:00"),
                                activity("666-r1", "16:50:00", "17:15:00")
                        )
                )),
                event(OfficialEvent.CLOCK, round("clock-r1", 3),
                        round("clock-r2", 1),
                        round("clock-r3", 2)),
                event(OfficialEvent.SQUARE_ONE, round("sq1-r1", 2), round("sq1-r2", 1)),
                event(OfficialEvent.FIVE_BY_FIVE, round("555-r1", 3), round("555-r2", 1)),
                event(OfficialEvent.SEVEN_BY_SEVEN, round("777-r1", 2), round("777-r2", 1)),
                event(OfficialEvent.MEGAMINX, round("minx-r1", 2)),
                event(OfficialEvent.THREE_BLIND, round("333bf-r1", 2)),
                event(OfficialEvent.FOUR_BY_FOUR, round("444-r1", 4)),
                event(OfficialEvent.SIX_BY_SIX, round("666-r1", 2)));
        groupScheduleGenerator.generate(testComp, ScheduleType.WAVES);

        assertGroupSchedule(testComp, "Blue Stage", OfficialEvent.CLOCK, 1,
                group(1, "09:15:00", "09:37:30"),
                group(2, "09:37:30", "10:00:00"),
                group(3, "10:00:00", "10:22:30")
        );
        assertGroupSchedule(testComp, "Red Stage", OfficialEvent.CLOCK, 1,
                group(1, "09:15:00", "09:37:30"),
                group(2, "09:37:30", "10:00:00")
        );
        assertGroupSchedule(testComp, "Yellow Stage", OfficialEvent.CLOCK, 1,
                group(1, "09:15:00", "09:37:30"),
                group(2, "09:37:30", "10:00:00")
        );

        assertGroupSchedule(testComp, "Blue Stage", OfficialEvent.SQUARE_ONE, 1,
                group(1, "10:22:30", "10:45:00")
        );
        assertGroupSchedule(testComp, "Red Stage", OfficialEvent.SQUARE_ONE, 1,
                group(1, "10:00:00", "10:22:30"),
                group(2, "10:22:30", "10:45:00")
        );
        assertGroupSchedule(testComp, "Yellow Stage", OfficialEvent.SQUARE_ONE, 1,
                group(1, "10:00:00", "10:22:30"),
                group(2, "10:22:30", "10:45:00")
        );

        assertGroupSchedule(testComp, "Blue Stage", OfficialEvent.FIVE_BY_FIVE, 1,
                group(1, "10:45:00", "11:13:20"),
                group(2, "11:13:20", "11:40:00"),
                group(3, "11:40:00", "12:10:00")
        );
        assertGroupSchedule(testComp, "Red Stage", OfficialEvent.FIVE_BY_FIVE, 1,
                group(1, "10:45:00", "11:13:20"),
                group(2, "11:13:20", "11:40:00")
        );
        assertGroupSchedule(testComp, "Yellow Stage", OfficialEvent.FIVE_BY_FIVE, 1,
                group(1, "10:45:00", "11:13:20"),
                group(2, "11:13:20", "11:40:00")
        );

        assertGroupSchedule(testComp, "Blue Stage", OfficialEvent.SEVEN_BY_SEVEN, 1,
                group(1, "12:10:00", "12:40:00")
        );
        assertGroupSchedule(testComp, "Red Stage", OfficialEvent.SEVEN_BY_SEVEN, 1,
                group(1, "11:40:00", "12:10:00"),
                group(2, "12:10:00", "12:40:00")
        );
        assertGroupSchedule(testComp, "Yellow Stage", OfficialEvent.SEVEN_BY_SEVEN, 1,
                group(1, "11:40:00", "12:10:00"),
                group(2, "12:10:00", "12:40:00")
        );

        assertGroupSchedule(testComp, "Blue Stage", OfficialEvent.MEGAMINX, 1,
                group(1, "13:40:00", "14:07:30"),
                group(2, "14:07:30", "14:35:00")
        );
        assertGroupSchedule(testComp, "Red Stage", OfficialEvent.MEGAMINX, 1,
                group(1, "13:40:00", "14:07:30"),
                group(2, "14:07:30", "14:35:00")
        );
        assertGroupSchedule(testComp, "Yellow Stage", OfficialEvent.MEGAMINX, 1,
                group(1, "13:40:00", "14:07:30"),
                group(2, "14:07:30", "14:35:00")
        );

        assertGroupSchedule(testComp, "Blue Stage", OfficialEvent.CLOCK, 2,
                group(1, "14:35:00", "15:00:00")
        );
        assertGroupSchedule(testComp, "Red Stage", OfficialEvent.CLOCK, 2,
                group(1, "14:35:00", "15:00:00")
        );

        assertGroupSchedule(testComp, "Blue Stage", OfficialEvent.THREE_BLIND, 1,
                group(1, "15:00:00", "15:20:00"),
                group(2, "15:20:00", "15:40:00")
        );
        assertGroupSchedule(testComp, "Red Stage", OfficialEvent.THREE_BLIND, 1,
                group(1, "15:00:00", "15:20:00"),
                group(2, "15:20:00", "15:40:00")
        );

        assertGroupSchedule(testComp, "Blue Stage", OfficialEvent.FOUR_BY_FOUR, 1,
                group(1, "15:40:00", "16:02:30"),
                group(2, "16:02:30", "16:25:00")
        );
        assertGroupSchedule(testComp, "Red Stage", OfficialEvent.FOUR_BY_FOUR, 1,
                group(1, "15:40:00", "16:02:30"),
                group(2, "16:02:30", "16:25:00")
        );
        assertGroupSchedule(testComp, "Yellow Stage", OfficialEvent.FOUR_BY_FOUR, 1,
                group(1, "15:20:00", "15:40:00"),
                group(2, "15:40:00", "16:02:30"),
                group(3, "16:02:30", "16:25:00"),
                group(4, "16:25:00", "16:50:00")
        );

        assertGroupSchedule(testComp, "Blue Stage", OfficialEvent.SIX_BY_SIX, 1,
                group(1, "16:25:00", "16:50:00"),
                group(2, "16:50:00", "17:15:00")
        );
        assertGroupSchedule(testComp, "Red Stage", OfficialEvent.SIX_BY_SIX, 1,
                group(1, "16:25:00", "16:50:00"),
                group(2, "16:50:00", "17:15:00")
        );
        assertGroupSchedule(testComp, "Yellow Stage", OfficialEvent.SIX_BY_SIX, 1,
                group(1, "16:50:00", "17:15:00")
        );
    }

    private Group group(final int group, final String startTime, final String endTime) {
        return new Group(group, parseTime(startTime), parseTime(endTime));
    }

    private static Instant parseTime(final String startTime) {
        return Instant.parse("2050-01-01T" + startTime + "Z");
    }

    private record Group(int group, Instant startTime, Instant endTime) {
    }

    private void assertGroupSchedule(final Competition competition, final String room, final OfficialEvent event, final int round, final Group... schedule) {
        final List<Activity> childActivities = getChildActivities(competition, room, event, round);
        assertEquals(schedule.length, childActivities.size(), "Expected " + schedule.length + " groups, got " + childActivities.size() + "\n" + childActivities);
        for (int i = 0; i < schedule.length; i++) {
            final Group group = schedule[i];
            final Activity groupActivity = childActivities.get(i);
            assertEquals(group.group(), groupActivity.getActivityCode().group());
            assertEquals(group.startTime(), groupActivity.getStartTime(), "Expected group " + group.group + " of " + event + " to start at " + group.startTime + " not " + groupActivity.getStartTime());
            assertEquals(group.endTime(), groupActivity.getEndTime(), "Expected group " + group.group + " of " + event + " to end at " + group.endTime + " not " + groupActivity.getEndTime());
        }
    }

    private static List<Activity> getChildActivities(final Competition competition, final String room, final OfficialEvent event, final int round) {
        for (final Venue venue : competition.getSchedule().getVenues()) {
            for (final Room venueRoom : venue.getRooms()) {
                if (venueRoom.name().equals(room)) {
                    for (final Activity activity : venueRoom.activities()) {
                        if (activity.getActivityCode().event() == event && activity.getActivityCode().round() == round) {
                            return activity.getChildActivities();
                        }
                    }
                }
            }
        }
        throw new RuntimeException("Could not find activity " + event + " r" + round + " in room " + room);
    }

    public Round round(final String activityCodeString, final int scrambleSets) {
        final ActivityCode activityCode = ActivityCode.fromString(activityCodeString);
        return new Round(
                activityCode,
                RoundFormat.AVERAGE_OF_FIVE,
                null,
                null,
                new PercentAdvancementCondition(75),
                new ArrayList<>(),
                scrambleSets,
                null,
                new ArrayList<>()
        );
    }

    public Event event(final OfficialEvent event, final Round... rounds) {
        return new Event(
                event,
                List.of(rounds),
                null,
                null,
                new ArrayList<>()
        );
    }

    public Activity activity(final String activityCodeString, final String startTime, final String endTime, final Activity... childActivities) {
        final ActivityCode activityCode = ActivityCode.fromString(activityCodeString);
        return new Activity(
                activityCode.getDisplayName().hashCode(),
                activityCode.getDisplayName(),
                activityCode,
                parseTime(startTime),
                parseTime(endTime),
                new ArrayList<>(List.of(childActivities)),
                null,
                new ArrayList<>()
        );
    }

    public Room room(final String name, final String colour, final Activity... activities) {
        return new Room(
                name.hashCode(),
                name,
                colour,
                List.of(activities),
                new ArrayList<>()
        );
    }

    public Venue venue(final String name, final Room... rooms) {
        return new Venue(
                name.hashCode(),
                name,
                0,
                0,
                CountryCode.GB,
                "UTC",
                List.of(rooms),
                new ArrayList<>()
        );
    }

    public Schedule schedule(final Venue... venues) {
        return new Schedule(
                LocalDate.of(2050, 1, 1),
                1,
                List.of(venues)
        );
    }

    public Competition competition(final Schedule schedule, final Event... events) {
        return new Competition(
                "1",
                "TestComp2050",
                "Test Comp 2050",
                "Test Comp 2050",
                null,
                new ArrayList<>(),
                List.of(events),
                schedule,
                new RegistrationInfo(
                        Instant.now(),
                        Instant.now(),
                        0,
                        "GBP",
                        true,
                        true
                ),
                200,
                new ArrayList<>()
        );
    }

}
