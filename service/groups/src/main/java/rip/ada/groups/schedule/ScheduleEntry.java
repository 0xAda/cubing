package rip.ada.groups.schedule;

import rip.ada.wcif.EventType;

import java.time.Instant;

public record ScheduleEntry(EventType event, Integer round, Instant startTime, Instant endTime) {
}
