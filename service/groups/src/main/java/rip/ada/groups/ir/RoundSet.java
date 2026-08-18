package rip.ada.groups.ir;

import java.util.List;
import java.util.Set;

public record RoundSet(Set<AssignableRoundId> roundIds, List<AssignmentSlot> slots) {
    public RoundSet {
        roundIds = Set.copyOf(roundIds);
        slots = List.copyOf(slots);
    }
}
