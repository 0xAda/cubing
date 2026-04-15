package rip.ada.groups.schedule;

import rip.ada.wcif.*;
import rip.ada.wcif.event.OfficialEvent;

import java.time.Duration;
import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public class GroupScheduleGenerator {

    private static int calculateGroupCount(final double groupsFittingInTime) {
        if (groupsFittingInTime % 1 == 0 || groupsFittingInTime < 1) {
            return (int) Math.max(1, groupsFittingInTime);
        } else if (groupsFittingInTime % 1 <= 0.5) {
            return (int) Math.floor(groupsFittingInTime);
        } else if (groupsFittingInTime % 1 > 0.5) {
            return (int) Math.ceil(groupsFittingInTime);
        }
        throw new IllegalStateException("Could not figure out amount of groups");
    }

    public void generate(final Competition competition, final ScheduleType scheduleType) {
        for (final Event event : competition.getEvents()) {
            for (final Round round : event.rounds()) {
                if (round.event() == OfficialEvent.MULTI_BLIND) {
                    for (int i = 0; i < round.format().getSolveCount(); i++) {
                        createGroupsForRound(competition, scheduleType, new ActivityCode(round.activityCode().event(), round.roundNumber(), null, i + 1), round.scrambleSetCount());
                    }
                    continue;
                }
                createGroupsForRound(competition, scheduleType, round.activityCode(), round.scrambleSetCount());
            }
        }
    }

    private void createGroupsForRound(final Competition competition, final ScheduleType scheduleType, final ActivityCode activityCode, final int scrambleSetCount) {
        Instant earliestTime = Instant.MAX;
        Instant latestTime = Instant.MIN;
        final Set<Instant> endTimes = new HashSet<>();
        for (final Venue venue : competition.getSchedule().getVenues()) {
            for (final Room room : venue.getRooms()) {
                for (final Activity activity : room.activities()) {
                    if (activity.getActivityCode().equals(activityCode)) {
                        if (earliestTime.isAfter(activity.getStartTime())) {
                            earliestTime = activity.getStartTime();
                        }
                        if (latestTime.isBefore(activity.getEndTime())) {
                            latestTime = activity.getEndTime();
                        }
                        endTimes.add(activity.getEndTime());
                        endTimes.add(activity.getStartTime());
                    }
                }
            }
        }

        if (earliestTime == Instant.MAX || latestTime == Instant.MIN) {
            throw new RuntimeException("Failed to determine start times for group " + activityCode);
        }

        final Duration totalEventDuration = Duration.between(earliestTime, latestTime);
        final Duration averageTimePerGroup = totalEventDuration.dividedBy(scrambleSetCount);

        for (final Venue venue : competition.getSchedule().getVenues()) {
            for (final Room room : venue.getRooms()) {
                for (final Activity activity : room.activities()) {
                    if (activity.getActivityCode().equals(activityCode)) {
                        final Duration activityLength = Duration.between(activity.getStartTime(), activity.getEndTime());
                        final double groupsFittingInTime = (double) activityLength.toNanos() / averageTimePerGroup.toNanos();
                        final int groupCount = calculateGroupCount(groupsFittingInTime);
                        final Duration timePerGroup = activityLength.dividedBy(groupCount);
                        Instant currentStartTime = activity.getStartTime();
                        for (int i = 1; i <= groupCount; i++) {
                            final Instant estimatedGroupEndTime = i != groupCount ? currentStartTime.plus(timePerGroup) : activity.getEndTime();
                            final Optional<Instant> maybeNearbyEndTime = endTimes.stream().filter(activityEndTime -> Duration.between(activityEndTime, estimatedGroupEndTime).abs().compareTo(Duration.ofMinutes(6)) <= 0).findAny();
                            final Instant groupEndTime;
                            groupEndTime = maybeNearbyEndTime.orElse(estimatedGroupEndTime);
                            endTimes.add(groupEndTime);
                            final ActivityCode groupCode = new ActivityCode(activity.getActivityCode().event(), activity.getActivityCode().round(), i, activity.getActivityCode().attempt());
                            final Activity e = new Activity(
                                    competition.getNextActivityId(),
                                    groupCode.getDisplayName(),
                                    groupCode,
                                    currentStartTime,
                                    groupEndTime,
                                    List.of(),
                                    null,
                                    List.of()
                            );
                            activity.getChildActivities().add(e);
                            currentStartTime = groupEndTime;
                        }
                    }
                }
            }
        }
    }

}
