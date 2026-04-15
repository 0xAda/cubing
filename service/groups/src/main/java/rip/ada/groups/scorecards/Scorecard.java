package rip.ada.groups.scorecards;

import rip.ada.wcif.Cutoff;
import rip.ada.wcif.EventType;
import rip.ada.wcif.RoundFormat;
import rip.ada.wcif.TimeLimit;

import java.util.Comparator;

public record Scorecard(
        String competitionName,
        int personId,
        String wcaId,
        String personName,
        int seed,
        EventType event,
        int round,
        int group,
        Cutoff cutoff,
        TimeLimit timeLimit,
        RoundFormat roundFormat,
        boolean doubleChecked,
        boolean fixedSeating,
        Integer stationNumber
) implements Comparable<Scorecard> {
    @Override
    public int compareTo(final Scorecard o) {
        return Comparator.comparing((Scorecard s) -> s.event().getEventId()).thenComparingInt(s -> s.group).thenComparing(s -> s.seed).compare(this, o);
    }
}
