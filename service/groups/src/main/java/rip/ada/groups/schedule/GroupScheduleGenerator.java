package rip.ada.groups.schedule;

import rip.ada.wcif.*;
import rip.ada.wcif.event.OfficialEvent;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public class GroupScheduleGenerator {

    private record GroupTimeRange(Instant startTime, Instant endTime) {
    }

    private record PendingGroup(Activity parent, Instant startTime, Instant endTime) {
    }

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
        Duration shortestActivityDuration = null;
        final Set<Instant> endTimes = new HashSet<>();
        final List<Activity> matchingActivities = new ArrayList<>();
        for (final Venue venue : competition.getSchedule().getVenues()) {
            for (final Room room : venue.getRooms()) {
                for (final Activity activity : room.activities()) {
                    if (activity.getActivityCode().equals(activityCode)) {
                        matchingActivities.add(activity);
                        if (earliestTime.isAfter(activity.getStartTime())) {
                            earliestTime = activity.getStartTime();
                        }
                        if (latestTime.isBefore(activity.getEndTime())) {
                            latestTime = activity.getEndTime();
                        }
                        endTimes.add(activity.getEndTime());
                        endTimes.add(activity.getStartTime());
                        final Duration activityDuration = Duration.between(activity.getStartTime(), activity.getEndTime());
                        if (shortestActivityDuration == null || activityDuration.compareTo(shortestActivityDuration) < 0) {
                            shortestActivityDuration = activityDuration;
                        }
                    }
                }
            }
        }

        if (earliestTime == Instant.MAX || latestTime == Instant.MIN) {
            throw new RuntimeException("Failed to determine start times for group " + activityCode);
        }

        if (scheduleType == ScheduleType.GROUPS) {
            generateGroupsForIndividuallyNumberedGroups(competition, activityCode, scrambleSetCount, shortestActivityDuration, matchingActivities, endTimes);
        } else {
            generateGroupsForWaves(competition, activityCode, scrambleSetCount, earliestTime, latestTime, matchingActivities, endTimes);
        }
    }

    private void generateGroupsForWaves(final Competition competition,
                                        final ActivityCode activityCode,
                                        final int scrambleSetCount,
                                        final Instant earliestTime,
                                        final Instant latestTime,
                                        final List<Activity> matchingActivities,
                                        final Set<Instant> endTimes) {
        final Duration totalEventDuration = Duration.between(earliestTime, latestTime);
        final Duration averageTimePerGroup = totalEventDuration.dividedBy(scrambleSetCount);

        final List<List<GroupTimeRange>> rangesPerStage = new ArrayList<>();
        for (final Activity activity : matchingActivities) {
            rangesPerStage.add(computeGroupTimeRanges(activity, averageTimePerGroup, endTimes));
        }

        final List<Instant> waveStartTimes = rangesPerStage.stream()
                .flatMap(List::stream)
                .map(GroupTimeRange::startTime)
                .distinct()
                .sorted()
                .toList();

        for (int stage = 0; stage < matchingActivities.size(); stage++) {
            final Activity activity = matchingActivities.get(stage);
            for (final GroupTimeRange range : rangesPerStage.get(stage)) {
                final int groupNumber = waveStartTimes.indexOf(range.startTime()) + 1;
                addGroupActivity(competition, activity, activityCode, groupNumber, range.startTime(), range.endTime());
            }
        }
    }

    private void generateGroupsForIndividuallyNumberedGroups(final Competition competition,
                                                             final ActivityCode activityCode,
                                                             final int scrambleSetCount,
                                                             final Duration shortestActivityDuration,
                                                             final List<Activity> matchingActivities,
                                                             final Set<Instant> endTimes) {
        final Duration averageTimePerGroup = shortestActivityDuration.dividedBy(scrambleSetCount);
        final List<PendingGroup> pendingGroups = new ArrayList<>();
        for (final Activity activity : matchingActivities) {
            for (final GroupTimeRange range : computeGroupTimeRanges(activity, averageTimePerGroup, endTimes)) {
                pendingGroups.add(new PendingGroup(activity, range.startTime(), range.endTime()));
            }
        }
        pendingGroups.sort(Comparator.comparing(PendingGroup::startTime));
        for (int i = 0; i < pendingGroups.size(); i++) {
            final PendingGroup pendingGroup = pendingGroups.get(i);
            addGroupActivity(competition, pendingGroup.parent(), activityCode, i + 1, pendingGroup.startTime(), pendingGroup.endTime());
        }
    }

    private List<GroupTimeRange> computeGroupTimeRanges(final Activity activity, final Duration averageTimePerGroup, final Set<Instant> endTimes) {
        final Duration activityLength = Duration.between(activity.getStartTime(), activity.getEndTime());
        final double groupsFittingInTime = (double) activityLength.toNanos() / averageTimePerGroup.toNanos();
        final int groupCount = calculateGroupCount(groupsFittingInTime);
        final Duration timePerGroup = activityLength.dividedBy(groupCount);
        final List<GroupTimeRange> ranges = new ArrayList<>();
        Instant currentStartTime = activity.getStartTime();
        for (int i = 1; i <= groupCount; i++) {
            final Instant estimatedGroupEndTime = i != groupCount ? currentStartTime.plus(timePerGroup) : activity.getEndTime();
            final Optional<Instant> maybeNearbyEndTime = endTimes.stream().filter(activityEndTime -> Duration.between(activityEndTime, estimatedGroupEndTime).abs().compareTo(Duration.ofMinutes(6)) <= 0).findAny();
            final Instant groupEndTime = maybeNearbyEndTime.orElse(estimatedGroupEndTime);
            endTimes.add(groupEndTime);
            ranges.add(new GroupTimeRange(currentStartTime, groupEndTime));
            currentStartTime = groupEndTime;
        }
        return ranges;
    }

    private void addGroupActivity(final Competition competition, final Activity parent, final ActivityCode activityCode, final int group, final Instant startTime, final Instant endTime) {
        final ActivityCode groupCode = new ActivityCode(activityCode.event(), activityCode.round(), group, activityCode.attempt());
        final Activity e = new Activity(
                competition.getNextActivityId(),
                groupCode.getDisplayName(),
                groupCode,
                startTime,
                endTime,
                List.of(),
                null,
                List.of()
        );
        parent.getChildActivities().add(e);
    }

}
