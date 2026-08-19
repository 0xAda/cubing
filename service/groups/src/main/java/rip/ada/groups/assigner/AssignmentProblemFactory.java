package rip.ada.groups.assigner;

import rip.ada.groups.assigner.model.AssignmentSolution;
import rip.ada.groups.assigner.model.CompetitorPlacementEntity;
import rip.ada.groups.ir.AssignmentSlot;
import rip.ada.groups.ir.Competitor;
import rip.ada.groups.ir.CompetitorId;
import rip.ada.groups.ir.IRCompetition;
import rip.ada.groups.ir.RoundSet;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public class AssignmentProblemFactory {

    private final CompetitorPlacementStrategy placementStrategy;

    public AssignmentProblemFactory(final CompetitorPlacementStrategy placementStrategy) {
        this.placementStrategy = placementStrategy;
    }

    public AssignmentSolution create(final IRCompetition competition) {
        final Map<CompetitorId, Competitor> competitorsById = competition.competitors().stream()
                .collect(Collectors.toMap(Competitor::id, Function.identity()));
        final List<CompetitorPlacementEntity> entities = new ArrayList<>();

        long id = 0;
        for (final RoundSet roundSet : competition.roundSets()) {
            for (final CompetitorPlacement placement : placementStrategy.placeCompetitors(competition, roundSet)) {
                final Competitor competitor = competitorsById.get(placement.competitorId());
                final List<AssignmentSlot> eligibleSlots = roundSet.slots()
                        .stream()
                        .filter(slot -> !competitor.hasConflictingCommitment(slot.timeWindow()))
                        .toList();
                final AssignmentSlot initialSlot = eligibleSlots.contains(placement.slot()) ? placement.slot() : null;

                entities.add(new CompetitorPlacementEntity(
                        id++,
                        roundSet,
                        placement.competitorId(),
                        placement.roundIds(),
                        eligibleSlots,
                        initialSlot
                ));
            }
        }

        return new AssignmentSolution(entities);
    }
}
