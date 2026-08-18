package rip.ada.groups.ir;

import java.util.Map;

public record AssignmentSlot(TimeWindow timeWindow, Map<AssignableRoundId, Integer> activityIds) {
    public AssignmentSlot {
        activityIds = Map.copyOf(activityIds);
    }
}
