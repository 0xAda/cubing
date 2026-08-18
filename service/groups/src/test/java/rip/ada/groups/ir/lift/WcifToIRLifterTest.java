package rip.ada.groups.ir.lift;

import org.junit.jupiter.api.Test;
import rip.ada.groups.ir.AssignableRound;
import rip.ada.groups.ir.AssignableRoundId;
import rip.ada.groups.ir.Competitor;
import rip.ada.groups.ir.CompetitorId;
import rip.ada.groups.ir.ExistingCommitment;
import rip.ada.groups.ir.IRCompetition;
import rip.ada.groups.ir.RoundSet;
import rip.ada.groups.ir.TimeWindow;
import rip.ada.wcif.Activity;
import rip.ada.wcif.ActivityCode;
import rip.ada.wcif.Competition;
import rip.ada.wcif.CountryCode;
import rip.ada.wcif.Event;
import rip.ada.wcif.Person;
import rip.ada.wcif.ParticipationRuleset;
import rip.ada.wcif.RegistrationInfo;
import rip.ada.wcif.RegistrationsParticipationSource;
import rip.ada.wcif.ResultValue;
import rip.ada.wcif.Room;
import rip.ada.wcif.Round;
import rip.ada.wcif.RoundFormat;
import rip.ada.wcif.Schedule;
import rip.ada.wcif.TimeLimit;
import rip.ada.wcif.Venue;
import rip.ada.wcif.event.OfficialEvent;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static rip.ada.groups.assigner.Fixtures.getCompetition;

class WcifToIRLifterTest {

    @Test
    public void shouldLiftPerson() {
        final Competition competition = getCompetition("BristolAugust2026");
        final WcifToIRLifter lifter = new WcifToIRLifter(competition);

        final Person personById = competition.getPersonById(1);
        final Competitor competitor = lifter.liftCompetitor(personById);

        assertEquals(1, competitor.id().registrantId());
        assertEquals(new ExistingCommitment(new TimeWindow(Instant.parse("2026-08-08T14:00:00Z"),
                Instant.parse("2026-08-08T14:25:00Z"))),
                competitor.commitments().get(0));
    }

    @Test
    public void shouldLiftCompetition() {
        final Competition competition = getCompetition("BristolAugust2026");
        final IRCompetition ir = new WcifToIRLifter(competition).lift();

        assertEquals("BristolAugust2026", ir.competitionId());
        assertEquals(100, ir.competitors().size());
        assertFalse(ir.rounds().isEmpty());
        assertFalse(ir.roundSets().isEmpty());
    }

    @Test
    public void shouldLiftSeededRoundEntries() {
        final Competition competition = getCompetition("BristolAugust2026");
        final WcifToIRLifter lifter = new WcifToIRLifter(competition);
        final Round round = findRound(competition, OfficialEvent.THREE_BY_THREE, 1);

        final AssignableRound assignableRound = lifter.liftRound(round).getFirst();

        assertEquals(new AssignableRoundId(OfficialEvent.THREE_BY_THREE, 1), assignableRound.id());
        assertEquals(97, assignableRound.entries().size());
        assertEquals(1, assignableRound.entries().getFirst().seed());
        assertEquals(97, assignableRound.entries().getLast().seed());
    }

    @Test
    public void shouldLiftGroupsAsAssignmentSlots() {
        final Competition competition = getCompetition("BristolAugust2026");
        final WcifToIRLifter lifter = new WcifToIRLifter(competition);
        final Round round = findRound(competition, OfficialEvent.THREE_BY_THREE, 1);

        final RoundSet roundSet = lifter.liftSimpleRoundSets(round).getFirst();

        assertEquals(Set.of(new AssignableRoundId(OfficialEvent.THREE_BY_THREE, 1)), roundSet.roundIds());
        assertEquals(4, roundSet.slots().size());
        assertEquals(
                new TimeWindow(Instant.parse("2026-08-09T09:00:00Z"),
                        Instant.parse("2026-08-09T09:17:30Z")),
                roundSet.slots().getFirst().timeWindow()
        );
        assertEquals(Set.of(25), Set.copyOf(roundSet.slots().getFirst().activityIds().values()));
    }

    @Test
    public void shouldUseAttemptRoundIdsInBothRoundsAndRoundSets() {
        final Competition competition = getCompetition("KewbzUKChampionshipFMC2026");
        final WcifToIRLifter lifter = new WcifToIRLifter(competition);
        final Round round = findRound(competition, OfficialEvent.FMC, 1);

        final Set<AssignableRoundId> roundIds = lifter.liftRound(round).stream()
                .map(AssignableRound::id)
                .collect(Collectors.toSet());
        final Set<AssignableRoundId> roundSetIds = lifter.liftSimpleRoundSets(round).stream()
                .map(RoundSet::roundIds)
                .flatMap(Set::stream)
                .collect(Collectors.toSet());

        assertEquals(Set.of(
                new AssignableRoundId(OfficialEvent.FMC, 1, 1),
                new AssignableRoundId(OfficialEvent.FMC, 1, 2),
                new AssignableRoundId(OfficialEvent.FMC, 1, 3)
        ), roundIds);
        assertEquals(roundIds, roundSetIds);
    }

    @Test
    public void shouldMakeEveryCompetitorEligibleForStaffing() {
        final Competition competition = getCompetition("BristolAugust2026");
        final IRCompetition ir = new WcifToIRLifter(competition).lift();
        final Set<CompetitorId> competitors = ir.competitors().stream()
                .map(Competitor::id)
                .collect(Collectors.toSet());

        assertFalse(ir.staffingRequirements().isEmpty());
        for (final var requirement : ir.staffingRequirements()) {
            assertEquals(competitors.size(), requirement.eligiblePeople().size());
            assertEquals(competitors, requirement.eligiblePeople());
        }
    }

    @Test
    public void shouldPutGroupsFromMultipleRoomsInOneRoundSet() {
        final ActivityCode roundCode = ActivityCode.round(OfficialEvent.THREE_BY_THREE, 1);
        final Round round = round(roundCode, RoundFormat.AVERAGE_OF_FIVE, null);
        final Competition competition = competition(
                List.of(event(OfficialEvent.THREE_BY_THREE, round)),
                room("Room 1", groupedActivity(roundCode, 100, Instant.parse("2050-01-01T10:00:00Z"), 1)),
                room("Room 2", groupedActivity(roundCode, 200, Instant.parse("2050-01-01T10:00:00Z"), 1))
        );

        final IRCompetition ir = new WcifToIRLifter(competition).lift();

        assertEquals(1, ir.roundSets().size());
        assertEquals(2, ir.roundSets().getFirst().slots().size());
        assertRoundSetInvariants(ir);
    }

    @Test
    public void shouldCombineCumulativeRoundsAcrossRoomsAndIgnoreEmptyRooms() {
        final ActivityCode sixBySix = ActivityCode.round(OfficialEvent.SIX_BY_SIX, 1);
        final ActivityCode sevenBySeven = ActivityCode.round(OfficialEvent.SEVEN_BY_SEVEN, 1);
        final TimeLimit cumulativeLimit = new TimeLimit(new ResultValue(60000), List.of(sixBySix, sevenBySeven));
        final Round sixBySixRound = round(sixBySix, RoundFormat.MEAN_OF_THREE, cumulativeLimit);
        final Round sevenBySevenRound = round(sevenBySeven, RoundFormat.MEAN_OF_THREE, cumulativeLimit);
        final Instant start = Instant.parse("2050-01-01T10:00:00Z");
        final Competition competition = competition(
                List.of(
                        event(OfficialEvent.SIX_BY_SIX, sixBySixRound),
                        event(OfficialEvent.SEVEN_BY_SEVEN, sevenBySevenRound)
                ),
                room("Room 1",
                        groupedActivity(sixBySix, 100, start, 2),
                        groupedActivity(sevenBySeven, 200, start, 2)),
                room("Room 2",
                        groupedActivity(sixBySix, 300, start, 2),
                        groupedActivity(sevenBySeven, 400, start, 2)),
                room("Unused room")
        );

        final IRCompetition ir = new WcifToIRLifter(competition).lift();

        assertEquals(1, ir.roundSets().size());
        assertEquals(Set.of(
                new AssignableRoundId(OfficialEvent.SIX_BY_SIX, 1),
                new AssignableRoundId(OfficialEvent.SEVEN_BY_SEVEN, 1)
        ), ir.roundSets().getFirst().roundIds());
        assertEquals(4, ir.roundSets().getFirst().slots().size());
        assertTrue(ir.roundSets().getFirst().slots().stream()
                .allMatch(slot -> slot.activityIds().size() == 2));
        assertRoundSetInvariants(ir);
    }

    @Test
    public void shouldNotCombinePartialCumulativeRoomLayouts() {
        final ActivityCode sixBySix = ActivityCode.round(OfficialEvent.SIX_BY_SIX, 1);
        final ActivityCode sevenBySeven = ActivityCode.round(OfficialEvent.SEVEN_BY_SEVEN, 1);
        final TimeLimit cumulativeLimit = new TimeLimit(new ResultValue(60000), List.of(sixBySix, sevenBySeven));
        final Instant start = Instant.parse("2050-01-01T10:00:00Z");
        final Competition competition = competition(
                List.of(
                        event(OfficialEvent.SIX_BY_SIX,
                                round(sixBySix, RoundFormat.MEAN_OF_THREE, cumulativeLimit)),
                        event(OfficialEvent.SEVEN_BY_SEVEN,
                                round(sevenBySeven, RoundFormat.MEAN_OF_THREE, cumulativeLimit))
                ),
                room("Complete room",
                        groupedActivity(sixBySix, 100, start, 2),
                        groupedActivity(sevenBySeven, 200, start, 2)),
                room("Partial room", groupedActivity(sixBySix, 300, start, 2))
        );

        final IRCompetition ir = new WcifToIRLifter(competition).lift();

        assertEquals(2, ir.roundSets().size());
        assertTrue(ir.roundSets().stream().allMatch(roundSet -> roundSet.roundIds().size() == 1));
        assertRoundSetInvariants(ir);
    }

    @Test
    public void shouldPreserveAttemptIdsOnGroupedMultiAttemptRounds() {
        final ActivityCode roundCode = ActivityCode.round(OfficialEvent.FMC, 1);
        final Round round = round(roundCode, RoundFormat.MEAN_OF_THREE, null);
        final Competition competition = competition(
                List.of(event(OfficialEvent.FMC, round)),
                room("Room 1",
                        groupedActivity(roundCode.attempt(1), 100, Instant.parse("2050-01-01T10:00:00Z"), 1),
                        groupedActivity(roundCode.attempt(2), 200, Instant.parse("2050-01-02T10:00:00Z"), 1),
                        groupedActivity(roundCode.attempt(3), 300, Instant.parse("2050-01-03T10:00:00Z"), 1))
        );

        final IRCompetition ir = new WcifToIRLifter(competition).lift();

        assertEquals(Set.of(1, 2, 3), ir.roundSets().stream()
                .flatMap(roundSet -> roundSet.roundIds().stream())
                .map(AssignableRoundId::attempt)
                .collect(Collectors.toSet()));
        assertRoundSetInvariants(ir);
    }

    private static Round findRound(final Competition competition,
                                   final OfficialEvent eventType,
                                   final int roundNumber) {
        return competition.getEvents().stream()
                .filter(event -> event.eventType() == eventType)
                .flatMap(event -> event.rounds().stream())
                .filter(round -> round.roundNumber() == roundNumber)
                .findFirst()
                .orElseThrow();
    }

    private static void assertRoundSetInvariants(final IRCompetition competition) {
        final Set<AssignableRoundId> roundIds = competition.rounds().stream()
                .map(AssignableRound::id)
                .collect(Collectors.toSet());
        final Map<AssignableRoundId, Integer> occurrences = new HashMap<>();
        for (final RoundSet roundSet : competition.roundSets()) {
            for (final AssignableRoundId roundId : roundSet.roundIds()) {
                occurrences.merge(roundId, 1, Integer::sum);
            }
            for (final var slot : roundSet.slots()) {
                assertTrue(roundSet.roundIds().containsAll(slot.activityIds().keySet()));
            }
        }

        assertEquals(roundIds, occurrences.keySet());
        assertTrue(occurrences.values().stream().allMatch(count -> count == 1));
    }

    private static Competition competition(final List<Event> events, final Room... rooms) {
        final Venue venue = new Venue(
                1,
                "Venue",
                0,
                0,
                CountryCode.GB,
                "UTC",
                List.of(rooms),
                List.of()
        );
        return new Competition(
                "1.0",
                "TestCompetition2050",
                "Test Competition 2050",
                "Test 2050",
                null,
                List.of(),
                events,
                new Schedule(LocalDate.of(2050, 1, 1), 3, List.of(venue)),
                new RegistrationInfo(Instant.EPOCH, Instant.EPOCH, 0, "GBP", true, true),
                100,
                List.of()
        );
    }

    private static Event event(final OfficialEvent eventType, final Round round) {
        return new Event(eventType, List.of(round), null, null, List.of());
    }

    private static Round round(final ActivityCode activityCode,
                               final RoundFormat format,
                               final TimeLimit timeLimit) {
        return new Round(
                activityCode,
                format,
                timeLimit,
                null,
                null,
                new ParticipationRuleset(new RegistrationsParticipationSource(), null),
                List.of(),
                format.getSolveCount(),
                null,
                List.of()
        );
    }

    private static Room room(final String name, final Activity... activities) {
        return new Room(name.hashCode(), name, "#000000", List.of(activities), List.of());
    }

    private static Activity groupedActivity(final ActivityCode round,
                                            final int parentId,
                                            final Instant start,
                                            final int groupCount) {
        final List<Activity> groups = new ArrayList<>();
        Instant groupStart = start;
        for (int groupNumber = 1; groupNumber <= groupCount; groupNumber++) {
            final Instant groupEnd = groupStart.plus(Duration.ofMinutes(10));
            final ActivityCode groupCode = round.group(groupNumber);
            groups.add(new Activity(
                    parentId + groupNumber,
                    groupCode.getDisplayName(),
                    groupCode,
                    groupStart,
                    groupEnd,
                    List.of(),
                    null,
                    List.of()
            ));
            groupStart = groupEnd;
        }
        return new Activity(
                parentId,
                round.getDisplayName(),
                round,
                start,
                groupStart,
                groups,
                null,
                List.of()
        );
    }

}
