package rip.ada.groups.ir.lift;

import rip.ada.groups.assigner.CompetitorProvider;
import rip.ada.groups.ir.*;
import rip.ada.groups.sorting.ActivityComparators;
import rip.ada.groups.sorting.CompetitorComparators;
import rip.ada.wcif.*;
import rip.ada.wcif.event.OfficialEvent;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

public class WcifToIRLifter {

    private final Competition competition;

    public WcifToIRLifter(final Competition competition) { //TODO: Some config to say what to lift
        this.competition = competition;
    }

    public IRCompetition lift() {
        final List<Competitor> competitors = liftCompetitors();
        final List<AssignableRound> rounds = liftRounds();
        final List<RoundSet> roundSets = liftRoundSets();
        final List<StaffingRequirement> staffingRequirements = liftStaffingRequirements(roundSets);

        return new IRCompetition(
                competition.getId(),
                "",
                competitors,
                rounds,
                roundSets,
                staffingRequirements
        );
    }

    private List<Competitor> liftCompetitors() {
        final List<Competitor> competitors = new ArrayList<>();
        for (final Person person : competition.getPersons()) {
            if (person.registration() == null ||
                    person.registration().registrationStatus() != RegistrationStatus.ACCEPTED ||
                    !person.registration().isCompeting()) {
                continue;
            }

            competitors.add(liftCompetitor(person));
        }
        return competitors;
    }

    Competitor liftCompetitor(final Person person) {
        final List<ExistingCommitment> commitments = new ArrayList<>();
        for (final Assignment assignment : person.assignments()) {
            final Activity activity = competition.getActivityById(assignment.activityId());
            final TimeWindow timeWindow = new TimeWindow(activity.getStartTime(), activity.getEndTime());
            commitments.add(new ExistingCommitment(timeWindow));
        }

        return new Competitor(
                new CompetitorId(person.registrantId()),
                commitments
        );
    }

    private List<AssignableRound> liftRounds() {
        final List<AssignableRound> rounds = new ArrayList<>();
        for (final Event event : competition.getEvents()) {
            for (final Round round : event.rounds()) {
                rounds.addAll(liftRound(round));
            }
        }
        return rounds;
    }

    List<AssignableRound> liftRound(final Round round) {
        final List<AssignableRound> assignableRounds = new ArrayList<>();

        if (round.event().multiAttempt()) {
            for (int i = 1; i <= round.format().getSolveCount(); i++) {
                assignableRounds.add(new AssignableRound(
                        new AssignableRoundId(round.event(), round.roundNumber(), i),
                        getEntriesForRound(round)
                ));
            }
        } else {
            assignableRounds.add(new AssignableRound(
                    new AssignableRoundId(round.event(), round.roundNumber()),
                    getEntriesForRound(round)
            ));
        }

        return assignableRounds;
    }

    List<RoundEntry> getEntriesForRound(final Round round) {
        final List<Person> personsInRound = CompetitorProvider.getCompetitors(competition, round.activityCode());
        personsInRound.sort(CompetitorComparators.sortByPersonalBest((OfficialEvent) round.event(), round.format().getSortBy()));

        final List<RoundEntry> roundEntries = new ArrayList<>(personsInRound.size());
        for (int i = 0; i < personsInRound.size(); i++) {
            roundEntries.add(new RoundEntry(toCompetitorId(personsInRound.get(i)), i + 1));
        }

        return roundEntries;
    }

    private List<RoundSet> liftRoundSets() {
        final List<RoundSet> roundSets = new ArrayList<>();
        final Set<Set<ActivityCode>> usedCumulativeRoundIds = new HashSet<>();

        for (final Event event : competition.getEvents()) {
            for (final Round round : event.rounds()) {
                if (round.timeLimit() != null) {
                    final List<ActivityCode> cumulRoundIds = round.timeLimit().cumulativeRoundIds();

                    if (cumulRoundIds != null && cumulRoundIds.size() > 1) {
                        if (usedCumulativeRoundIds.contains(new HashSet<>(cumulRoundIds))) {
                            continue;
                        }
                        final RoundSet rs = trySchedulingSimultaneously(cumulRoundIds);
                        if (rs != null) {
                            roundSets.add(rs);
                            usedCumulativeRoundIds.add(new HashSet<>(cumulRoundIds));
                            continue;
                        }
                    }
                }

                roundSets.addAll(liftSimpleRoundSets(round));
            }
        }

        return roundSets;
    }

    private RoundSet trySchedulingSimultaneously(final List<ActivityCode> rounds) {
        final List<AssignmentSlot> assignmentSlots = new ArrayList<>();
        for (final Room room : competition.getAllRooms()) {
            final RoomScheduleResult result = trySchedulingRoomSimultaneously(room, rounds);
            if (result.status() == RoomScheduleStatus.INVALID) {
                return null;
            }
            if (result.status() == RoomScheduleStatus.VALID) {
                assignmentSlots.addAll(result.slots());
            }
        }

        if (assignmentSlots.isEmpty()) {
            return null;
        }

        final Set<AssignableRoundId> roundIds = rounds.stream()
                .map(AssignableRoundId::new)
                .collect(Collectors.toSet());
        return new RoundSet(roundIds, assignmentSlots);
    }

    private RoomScheduleResult trySchedulingRoomSimultaneously(final Room room, final List<ActivityCode> rounds) {
        final List<List<Activity>> roundGroups = rounds
                .stream()
                .map(room::getGroups)
                .toList();

        roundGroups.forEach(acs -> acs.sort(ActivityComparators.START_TIME));

        final int groupCount = roundGroups.getFirst().size();
        final boolean allRoundsAbsent = roundGroups.stream().allMatch(List::isEmpty);
        if (allRoundsAbsent) {
            return new RoomScheduleResult(RoomScheduleStatus.ABSENT, List.of());
        }

        for (final List<Activity> groups : roundGroups) {
            if (groups.size() != groupCount) {
                return new RoomScheduleResult(RoomScheduleStatus.INVALID, List.of());
            }
        }

        for (final List<Activity> groups : roundGroups) {
            for (int i = 1; i < groups.size(); i++) {
                final Activity currentActivity = groups.get(i);
                final Activity previousActivity = groups.get(i - 1);
                if (currentActivity.getStartTime().isBefore(previousActivity.getEndTime())) {
                    // TODO: We could handle multiple concurrent groups of cumulative time limits on the same stage, or we could not.
                    return new RoomScheduleResult(RoomScheduleStatus.INVALID, List.of());
                }
            }
        }

        final List<AssignmentSlot> assignmentSlots = new ArrayList<>();
        for (int i = 0; i < groupCount; i++) {
            final Map<AssignableRoundId, Integer> roundToActivityId = new HashMap<>();
            Instant startTime = null;
            Instant endTime = null;
            for (final List<Activity> roundGroup : roundGroups) {
                final Activity activity = roundGroup.get(i);
                roundToActivityId.put(new AssignableRoundId(activity.getActivityCode()), activity.getId());

                if (startTime == null) {
                    startTime = activity.getStartTime();
                    endTime = activity.getEndTime();
                    continue;
                }

                if (!startTime.equals(activity.getStartTime()) || !endTime.equals(activity.getEndTime())) {
                    return new RoomScheduleResult(RoomScheduleStatus.INVALID, List.of());
                }
            }
            assignmentSlots.add(new AssignmentSlot(new TimeWindow(startTime, endTime), roundToActivityId));
        }

        return new RoomScheduleResult(RoomScheduleStatus.VALID, assignmentSlots);
    }

    List<RoundSet> liftSimpleRoundSets(final Round round) {
        if (round.event().multiAttempt()) {
            return liftMultiAttemptRoundSets(round);
        }

        final AssignableRoundId roundId = new AssignableRoundId(round.event(), round.roundNumber());
        final List<AssignmentSlot> slots = liftGroupSlots(round.activityCode(), roundId);
        if (slots.isEmpty()) {
            return List.of();
        }
        return List.of(new RoundSet(Set.of(roundId), slots));
    }

    private List<RoundSet> liftMultiAttemptRoundSets(final Round round) {
        final List<RoundSet> roundSets = new ArrayList<>();
        for (int i = 1; i <= round.format().getSolveCount(); i++) {
            final ActivityCode attempt = round.activityCode().attempt(i);
            final AssignableRoundId roundId = new AssignableRoundId(attempt);
            final List<AssignmentSlot> slots = liftGroupSlots(attempt, roundId);
            if (slots.isEmpty()) {
                slots.addAll(liftTopLevelAttemptSlots(attempt, roundId));
            }
            if (slots.isEmpty()) {
                throw new IllegalArgumentException("No scheduled activity found for " + attempt);
            }
            roundSets.add(new RoundSet(Set.of(roundId), slots));
        }
        return roundSets;
    }

    private List<AssignmentSlot> liftGroupSlots(final ActivityCode activityCode,
                                                final AssignableRoundId roundId) {
        final List<AssignmentSlot> slots = new ArrayList<>();
        for (final Room room : competition.getAllRooms()) {
            final List<Activity> groups = room.getGroups(activityCode);
            groups.sort(ActivityComparators.START_TIME);
            for (final Activity group : groups) {
                slots.add(toAssignmentSlot(group, roundId));
            }
        }
        return slots;
    }

    private List<AssignmentSlot> liftTopLevelAttemptSlots(final ActivityCode attempt,
                                                          final AssignableRoundId roundId) {
        final List<AssignmentSlot> slots = new ArrayList<>();
        for (final Room room : competition.getAllRooms()) {
            final Activity activity = room.getTopLevelActivityByCode(attempt);
            if (activity != null) {
                slots.add(toAssignmentSlot(activity, roundId));
            }
        }
        return slots;
    }

    private static AssignmentSlot toAssignmentSlot(final Activity activity,
                                                   final AssignableRoundId roundId) {
        final TimeWindow timeWindow = new TimeWindow(activity.getStartTime(), activity.getEndTime());
        return new AssignmentSlot(timeWindow, Map.of(roundId, activity.getId()));
    }

    List<StaffingRequirement> liftStaffingRequirements(final List<RoundSet> roundSets) {
        final Set<CompetitorId> allCompetitors = competition
                .getCompetingPersons()
                .stream()
                .map(this::toCompetitorId)
                .collect(Collectors.toSet());

        final List<StaffingRequirement> staffingRequirements = new ArrayList<>();
        for (final RoundSet roundSet : roundSets) {
            for (final AssignmentSlot slot : roundSet.slots()) {
                staffingRequirements.add(new StaffingRequirement(
                        slot,
                        StandardAssignmentCode.SCRAMBLER,
                        3,
                        allCompetitors
                ));
                staffingRequirements.add(new StaffingRequirement(
                        slot,
                        StandardAssignmentCode.JUDGE,
                        12,
                        allCompetitors
                ));
                staffingRequirements.add(new StaffingRequirement(
                        slot,
                        StandardAssignmentCode.RUNNER,
                        2,
                        allCompetitors
                ));
            }
        }
        return staffingRequirements;
    }

    CompetitorId toCompetitorId(final Person person) {
        return new CompetitorId(person.registrantId());
    }

    private enum RoomScheduleStatus {
        ABSENT,
        VALID,
        INVALID
    }

    private record RoomScheduleResult(RoomScheduleStatus status, List<AssignmentSlot> slots) {
        private RoomScheduleResult {
            slots = List.copyOf(slots);
        }
    }

}
