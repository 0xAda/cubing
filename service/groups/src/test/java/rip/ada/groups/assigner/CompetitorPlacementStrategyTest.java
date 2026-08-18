package rip.ada.groups.assigner;

import org.junit.jupiter.api.Test;
import rip.ada.groups.ir.AssignableRound;
import rip.ada.groups.ir.AssignableRoundId;
import rip.ada.groups.ir.AssignmentSlot;
import rip.ada.groups.ir.CompetitorId;
import rip.ada.groups.ir.IRCompetition;
import rip.ada.groups.ir.RoundEntry;
import rip.ada.groups.ir.RoundSet;
import rip.ada.groups.ir.TimeWindow;
import rip.ada.groups.ir.lift.WcifToIRLifter;
import rip.ada.groups.schedule.GroupScheduleGenerator;
import rip.ada.groups.schedule.ScheduleType;
import rip.ada.wcif.ActivityCode;
import rip.ada.wcif.Competition;
import rip.ada.wcif.event.OfficialEvent;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import static rip.ada.groups.assigner.AssignmentAssertions.*;
import static rip.ada.groups.assigner.Fixtures.getCompetition;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class CompetitorPlacementStrategyTest {

    public static final ActivityCode ROUND_ONE_333 = ActivityCode.round(OfficialEvent.THREE_BY_THREE, 1);

    @Test
    public void randomPlacementShouldEvenlyDistributeGroups() {
        final PlacementFixture fixture = yorkThreeByThreeRoundOne();
        final CompetitorPlacementStrategy random = CompetitorPlacementStrategy.RANDOM_PLACEMENT;
        final List<ProposedAssignment> proposedAssignments = proposedAssignments(
                random.placeCompetitors(fixture.ir(), fixture.roundSet())
        );

        assertAllCompetitorsHaveASlotInEventFirstRound(
                fixture.wcif(),
                OfficialEvent.THREE_BY_THREE,
                proposedAssignments
        );

        assertGroupsForRoundAreEvenlyDistributed(fixture.wcif(), ROUND_ONE_333, proposedAssignments);
    }

    @Test
    public void rankedPlacement() {
        final PlacementFixture fixture = yorkThreeByThreeRoundOne();
        final CompetitorPlacementStrategy ranked = CompetitorPlacementStrategy.RANKED;
        final List<ProposedAssignment> proposedAssignments = proposedAssignments(
                ranked.placeCompetitors(fixture.ir(), fixture.roundSet())
        );

        assertAllCompetitorsHaveASlotInEventFirstRound(
                fixture.wcif(),
                OfficialEvent.THREE_BY_THREE,
                proposedAssignments
        );

        assertGroupsForRoundAreEvenlyDistributed(fixture.wcif(), ROUND_ONE_333, proposedAssignments);

        assertRankedFromSlowestToFastest(fixture.ir(), fixture.roundSet(), ranked);
    }

    @Test
    public void symmetricPlacement() {
        final PlacementFixture fixture = yorkThreeByThreeRoundOne();
        final CompetitorPlacementStrategy symmetric = CompetitorPlacementStrategy.SYMMETRIC;
        final List<ProposedAssignment> proposedAssignments = proposedAssignments(
                symmetric.placeCompetitors(fixture.ir(), fixture.roundSet())
        );

        assertAllCompetitorsHaveASlotInEventFirstRound(
                fixture.wcif(),
                OfficialEvent.THREE_BY_THREE,
                proposedAssignments
        );

        assertGroupsForRoundAreEvenlyDistributed(fixture.wcif(), ROUND_ONE_333, proposedAssignments);

        assertFastestCompetitorsAreInDifferentSlots(fixture.ir(), fixture.roundSet(), symmetric);
    }

    @Test
    public void competitorEnteredInTwoRoundsSharingASlotIsPlacedOnce() {
        final AssignableRoundId sixBySix = new AssignableRoundId(OfficialEvent.SIX_BY_SIX, 1);
        final AssignableRoundId sevenBySeven = new AssignableRoundId(OfficialEvent.SEVEN_BY_SEVEN, 1);
        final CompetitorId enteredInBoth = new CompetitorId(1);
        final CompetitorId enteredInSix = new CompetitorId(2);
        final CompetitorId enteredInSeven = new CompetitorId(3);

        final AssignmentSlot firstSlot = slot(100, 200, sixBySix, sevenBySeven, 0);
        final AssignmentSlot secondSlot = slot(101, 201, sixBySix, sevenBySeven, 30);
        final RoundSet roundSet = new RoundSet(Set.of(sixBySix, sevenBySeven), List.of(firstSlot, secondSlot));
        final IRCompetition competition = new IRCompetition(
                "CombinedRounds",
                "",
                List.of(
                        competitor(enteredInBoth),
                        competitor(enteredInSix),
                        competitor(enteredInSeven)
                ),
                List.of(
                        new AssignableRound(sixBySix, List.of(
                                new RoundEntry(enteredInBoth, 1),
                                new RoundEntry(enteredInSix, 2)
                        )),
                        new AssignableRound(sevenBySeven, List.of(
                                new RoundEntry(enteredInSeven, 1),
                                new RoundEntry(enteredInBoth, 2)
                        ))
                ),
                List.of(roundSet),
                List.of()
        );

        final List<CompetitorPlacement> placements = CompetitorPlacementStrategy.SYMMETRIC
                .placeCompetitors(competition, roundSet);
        final Map<CompetitorId, CompetitorPlacement> placementsByCompetitor = placements.stream()
                .collect(Collectors.toMap(CompetitorPlacement::competitorId, Function.identity()));

        assertEquals(3, placements.size());
        assertEquals(Set.of(sixBySix, sevenBySeven), placementsByCompetitor.get(enteredInBoth).roundIds());
        assertEquals(2, placementsByCompetitor.get(enteredInBoth).toProposedAssignments().size());
        assertEquals(
                Set.copyOf(placementsByCompetitor.get(enteredInBoth).slot().activityIds().values()),
                placementsByCompetitor.get(enteredInBoth).toProposedAssignments().stream()
                        .map(ProposedAssignment::activityId)
                        .collect(Collectors.toSet())
        );
        assertEquals(Set.of(sixBySix), placementsByCompetitor.get(enteredInSix).roundIds());
        assertEquals(Set.of(sevenBySeven), placementsByCompetitor.get(enteredInSeven).roundIds());

        final Map<AssignmentSlot, Long> placementCountBySlot = placements.stream()
                .collect(Collectors.groupingBy(CompetitorPlacement::slot, Collectors.counting()));
        assertTrue(placementCountBySlot.values().stream().allMatch(count -> count == 1 || count == 2));
    }

    private static void assertRankedFromSlowestToFastest(
            final IRCompetition competition,
            final RoundSet roundSet,
            final CompetitorPlacementStrategy strategy
    ) {
        final AssignableRound round = competition.rounds().stream()
                .filter(candidate -> roundSet.roundIds().contains(candidate.id()))
                .findFirst()
                .orElseThrow();
        final Map<CompetitorId, Integer> seedByCompetitor = round.entries().stream()
                .collect(Collectors.toMap(RoundEntry::competitorId, RoundEntry::seed));
        final List<AssignmentSlot> slots = roundSet.slots().stream()
                .sorted(java.util.Comparator.comparing(slot -> slot.timeWindow().start()))
                .toList();
        final Map<AssignmentSlot, Integer> slotIndex = java.util.stream.IntStream.range(0, slots.size())
                .boxed()
                .collect(Collectors.toMap(slots::get, Function.identity()));
        final List<CompetitorPlacement> placements = strategy.placeCompetitors(competition, roundSet);

        for (final CompetitorPlacement earlier : placements) {
            for (final CompetitorPlacement later : placements) {
                if (slotIndex.get(earlier.slot()) < slotIndex.get(later.slot())) {
                    assertTrue(
                            seedByCompetitor.get(earlier.competitorId())
                                    >= seedByCompetitor.get(later.competitorId()),
                            "A faster competitor was placed in an earlier ranked slot"
                    );
                }
            }
        }
    }

    private static void assertFastestCompetitorsAreInDifferentSlots(
            final IRCompetition competition,
            final RoundSet roundSet,
            final CompetitorPlacementStrategy strategy
    ) {
        final AssignableRound round = competition.rounds().stream()
                .filter(candidate -> roundSet.roundIds().contains(candidate.id()))
                .findFirst()
                .orElseThrow();
        final Set<CompetitorId> fastestCompetitors = round.entries().stream()
                .sorted(java.util.Comparator.comparingInt(RoundEntry::seed))
                .limit(roundSet.slots().size())
                .map(RoundEntry::competitorId)
                .collect(Collectors.toSet());
        final Set<AssignmentSlot> theirSlots = strategy.placeCompetitors(competition, roundSet).stream()
                .filter(placement -> fastestCompetitors.contains(placement.competitorId()))
                .map(CompetitorPlacement::slot)
                .collect(Collectors.toSet());

        assertEquals(roundSet.slots().size(), theirSlots.size());
    }

    private static PlacementFixture yorkThreeByThreeRoundOne() {
        final Competition competition = getCompetition("YorkSummer2026");
        new GroupScheduleGenerator().generate(competition, ScheduleType.GROUPS);
        final IRCompetition ir = new WcifToIRLifter(competition).lift();
        final AssignableRoundId roundId = new AssignableRoundId(ROUND_ONE_333);
        final RoundSet roundSet = ir.roundSets().stream()
                .filter(candidate -> candidate.roundIds().equals(Set.of(roundId)))
                .findFirst()
                .orElseThrow();
        return new PlacementFixture(competition, ir, roundSet);
    }

    private static List<ProposedAssignment> proposedAssignments(final List<CompetitorPlacement> placements) {
        return placements.stream()
                .flatMap(placement -> placement.toProposedAssignments().stream())
                .toList();
    }

    private static rip.ada.groups.ir.Competitor competitor(final CompetitorId competitorId) {
        return new rip.ada.groups.ir.Competitor(competitorId, List.of());
    }

    private static AssignmentSlot slot(
            final int sixBySixActivityId,
            final int sevenBySevenActivityId,
            final AssignableRoundId sixBySix,
            final AssignableRoundId sevenBySeven,
            final int minutesAfterStart
    ) {
        final Instant start = Instant.parse("2050-01-01T10:00:00Z")
                .plus(Duration.ofMinutes(minutesAfterStart));
        return new AssignmentSlot(
                new TimeWindow(start, start.plus(Duration.ofMinutes(20))),
                Map.of(sixBySix, sixBySixActivityId, sevenBySeven, sevenBySevenActivityId)
        );
    }

    private record PlacementFixture(Competition wcif, IRCompetition ir, RoundSet roundSet) {
    }

}
