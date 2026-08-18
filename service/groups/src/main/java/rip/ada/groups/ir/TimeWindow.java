package rip.ada.groups.ir;

import java.time.Instant;

public record TimeWindow(Instant start, Instant end) {
    public boolean overlaps(final TimeWindow other) {
        return start.isBefore(other.end)
                && other.start.isBefore(end);
    }
}
