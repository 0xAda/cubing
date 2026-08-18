package rip.ada.groups.assigner;

import rip.ada.groups.ir.AssignableRound;
import rip.ada.groups.ir.AssignableRoundId;
import rip.ada.groups.ir.AssignmentSlot;
import rip.ada.groups.ir.CompetitorId;
import rip.ada.groups.ir.IRCompetition;
import rip.ada.groups.ir.RoundEntry;
import rip.ada.groups.ir.RoundSet;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public interface CompetitorPlacementStrategy {

    List<CompetitorPlacement> placeCompetitors(IRCompetition competition, RoundSet roundSet);

    default List<CompetitorPlacement> placeCompetitors(final IRCompetition competition) {
        return competition.roundSets().stream()
                .flatMap(roundSet -> placeCompetitors(competition, roundSet).stream())
                .toList();
    }

    CompetitorPlacementStrategy RANDOM_PLACEMENT = (competition, roundSet) -> {
        final List<PlacementCandidate> competitors = getCompetitors(competition, roundSet);
        Collections.shuffle(competitors);

        return roundRobin(roundSet, competitors);
    };

    CompetitorPlacementStrategy SYMMETRIC = (competition, roundSet) -> {
        final List<PlacementCandidate> competitors = getCompetitors(competition, roundSet);
        competitors.sort(Comparator
                .comparingDouble(PlacementCandidate::averageSeed)
                .thenComparingInt(candidate -> candidate.competitorId().registrantId()));

        return roundRobin(roundSet, competitors);
    };

    CompetitorPlacementStrategy RANKED = (competition, roundSet) -> {
        final List<PlacementCandidate> competitors = getCompetitors(competition, roundSet);
        competitors.sort(Comparator
                .comparingDouble(PlacementCandidate::averageSeed)
                .reversed()
                .thenComparingInt(candidate -> candidate.competitorId().registrantId()));

        final List<AssignmentSlot> slots = sortedSlots(roundSet);
        final List<CompetitorPlacement> placements = new ArrayList<>(competitors.size());
        int competitorIndex = 0;

        for (int slotIndex = 0; slotIndex < slots.size(); slotIndex++) {
            final int competitorsInSlot = rankedSlotSize(competitors.size(), slots.size(), slotIndex);
            for (int i = 0; i < competitorsInSlot; i++) {
                final PlacementCandidate competitor = competitors.get(competitorIndex++);
                placements.add(competitor.placeIn(slots.get(slotIndex)));
            }
        }

        return List.copyOf(placements);
    };

    private static List<PlacementCandidate> getCompetitors(
            final IRCompetition competition,
            final RoundSet roundSet
    ) {
        final Map<AssignableRoundId, AssignableRound> roundsById = new HashMap<>();
        for (final AssignableRound round : competition.rounds()) {
            roundsById.put(round.id(), round);
        }

        final Map<CompetitorId, CandidateBuilder> competitors = new LinkedHashMap<>();
        for (final AssignableRoundId roundId : roundSet.roundIds()) {
            final AssignableRound round = roundsById.get(roundId);

            for (final RoundEntry entry : round.entries()) {
                competitors.computeIfAbsent(entry.competitorId(), CandidateBuilder::new)
                        .addEntry(roundId, entry.seed());
            }
        }

        return competitors.values().stream()
                .map(CandidateBuilder::build)
                .collect(Collectors.toCollection(ArrayList::new));
    }

    private static List<CompetitorPlacement> roundRobin(
            final RoundSet roundSet,
            final List<PlacementCandidate> competitors
    ) {
        final List<AssignmentSlot> slots = sortedSlots(roundSet);
        final List<CompetitorPlacement> placements = new ArrayList<>(competitors.size());

        for (int i = 0; i < competitors.size(); i++) {
            placements.add(competitors.get(i).placeIn(slots.get(i % slots.size())));
        }

        return List.copyOf(placements);
    }

    private static List<AssignmentSlot> sortedSlots(final RoundSet roundSet) {
        return roundSet.slots().stream()
                .sorted(Comparator
                        .comparing((AssignmentSlot slot) -> slot.timeWindow().start())
                        .thenComparing(slot -> slot.timeWindow().end())
                        .thenComparingInt(slot -> slot.activityIds().values().stream()
                                .mapToInt(Integer::intValue)
                                .min()
                                .orElseThrow()))
                .toList();
    }

    private static int rankedSlotSize(final int competitorCount, final int slotCount, final int slotIndex) {
        final int minimumSize = competitorCount / slotCount;
        final int largerSlotCount = competitorCount % slotCount;
        return minimumSize + (slotIndex < largerSlotCount ? 1 : 0);
    }

    record PlacementCandidate(
            CompetitorId competitorId,
            Map<AssignableRoundId, Integer> seeds
    ) {
        double averageSeed() {
            return seeds.values().stream()
                    .mapToInt(Integer::intValue)
                    .average()
                    .orElseThrow();
        }

        CompetitorPlacement placeIn(final AssignmentSlot slot) {
            return new CompetitorPlacement(competitorId, seeds.keySet(), slot);
        }
    }

    final class CandidateBuilder {
        private final CompetitorId competitorId;
        private final Map<AssignableRoundId, Integer> seeds = new HashMap<>();

        private CandidateBuilder(final CompetitorId competitorId) {
            this.competitorId = competitorId;
        }

        private void addEntry(final AssignableRoundId roundId, final int seed) {
            seeds.put(roundId, seed);
        }

        private PlacementCandidate build() {
            return new PlacementCandidate(competitorId, Map.copyOf(seeds));
        }
    }
}
