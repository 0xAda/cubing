package rip.ada.groups.ir;

import java.util.List;

public record AssignableRound(AssignableRoundId id, List<RoundEntry> entries) {
    public AssignableRound {
        entries = List.copyOf(entries);
    }
}
