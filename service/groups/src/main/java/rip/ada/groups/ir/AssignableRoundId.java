package rip.ada.groups.ir;

import rip.ada.wcif.ActivityCode;
import rip.ada.wcif.EventType;

public record AssignableRoundId(EventType eventType, int round, Integer attempt) {

    public AssignableRoundId(final ActivityCode activityCode) {
        this(activityCode.event(), activityCode.round(), activityCode.attempt());
    }

    public AssignableRoundId(final EventType eventType, final int round) {
        this(eventType, round, null);
    }
}
