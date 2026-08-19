package rip.ada.groups.ir;

import java.util.List;

public record Competitor(CompetitorId id,
                         List<ExistingCommitment> commitments) {
    public Competitor {
        commitments = List.copyOf(commitments);
    }

    public boolean hasConflictingCommitment(final TimeWindow timeWindow) {
        for (final ExistingCommitment commitment : commitments) {
            if (commitment.timeWindow().overlaps(timeWindow)) {
                return true;
            }
        }
        return false;
    }
}
