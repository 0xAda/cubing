package rip.ada.groups.ir;

import java.util.List;

public record IRCompetition(
        String competitionId,
        String wcifHash,
        List<Competitor> competitors,
        List<AssignableRound> rounds,
        List<RoundSet> roundSets,
        List<StaffingRequirement> staffingRequirements
) {
    public IRCompetition {
        competitors = List.copyOf(competitors);
        rounds = List.copyOf(rounds);
        roundSets = List.copyOf(roundSets);
        staffingRequirements = List.copyOf(staffingRequirements);
    }
}
