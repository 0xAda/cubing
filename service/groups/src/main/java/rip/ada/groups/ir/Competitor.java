package rip.ada.groups.ir;

import java.util.List;

public record Competitor(CompetitorId id,
                         List<ExistingCommitment> commitments) {
    public Competitor {
        commitments = List.copyOf(commitments);
    }
}
