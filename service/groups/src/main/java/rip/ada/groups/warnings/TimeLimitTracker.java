package rip.ada.groups.warnings;

import rip.ada.wcif.ActivityCode;
import rip.ada.wcif.AttemptResult;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TimeLimitTracker {

    private final List<ActivityCode> rounds;
    private final int cumulativeTimeLimit;
    private final Map<Integer, Integer> timeUsed = new HashMap<>();

    public TimeLimitTracker(final List<ActivityCode> rounds, final int cumulativeTimeLimit) {
        this.rounds = rounds;
        this.cumulativeTimeLimit = cumulativeTimeLimit;
    }

    public void addResult(final int personId, final AttemptResult result) {
        if (!result.isSuccess()) {
            return;
        }

        timeUsed.put(personId, timeUsed.getOrDefault(personId, 0) + result.value());
    }

    public List<ExceededTimeLimit> getPersonsExceedingCumulativeTimeLimit() {
        final List<ExceededTimeLimit> persons = new ArrayList<>();
        for (final Map.Entry<Integer, Integer> entry : timeUsed.entrySet()) {
            if (entry.getValue() >= cumulativeTimeLimit) {
                persons.add(new ExceededTimeLimit(entry.getKey(), entry.getValue(), cumulativeTimeLimit, rounds));
            }
        }
        return persons;
    }
}
