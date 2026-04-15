package rip.ada.groups.warnings;

import rip.ada.groups.templates.Message;
import rip.ada.wcif.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static rip.ada.groups.templates.Joiner.join;
import static rip.ada.wcif.AttemptResult.renderWithoutCentiseconds;

public class CompetitionWarnings {

    public List<Message> getWarnings(final Competition competition) {
        return checkForResultsExceedingCumulativeTimeLimit(competition);
    }

    public List<Message> checkForResultsExceedingCumulativeTimeLimit(final Competition competition) {
        final Map<List<ActivityCode>, TimeLimitTracker> timeLimits = new HashMap<>();
        final List<Message> messages = new ArrayList<>();
        for (final Event event : competition.getEvents()) {
            for (final Round round : event.rounds()) {
                for (final Result result : round.results()) {
                    if (!result.attempts().isEmpty() && result.attempts().getFirst().result().value() == result.personId()) {
                        messages.add(new Message("Competitor " + competition.getPersonById(result.personId()).name() + " has a time entered in " + round.activityCode().getDisplayName() + " that is equal to their competitor id",
                                Message.Type.WARNING));
                    }
                }
                if (round.timeLimit() == null || round.timeLimit().cumulativeRoundIds().isEmpty() || round.results().isEmpty()) {
                    continue;
                }
                final TimeLimitTracker timeLimitTracker = timeLimits.computeIfAbsent(round.timeLimit().cumulativeRoundIds(),
                        x -> new TimeLimitTracker(round.timeLimit().cumulativeRoundIds(), round.timeLimit().centiseconds().value()));

                for (final Result result : round.results()) {
                    for (final Attempt attempt : result.attempts()) {
                        if (!result.attempts().isEmpty()) {

                        }
                        timeLimitTracker.addResult(result.personId(), attempt.result());
                    }
                }
            }
        }

        if (timeLimits.isEmpty()) {
            return List.of();
        }

        for (final TimeLimitTracker value : timeLimits.values()) {
            final List<ExceededTimeLimit> personsExceedingCumulativeTimeLimit = value.getPersonsExceedingCumulativeTimeLimit();
            if (personsExceedingCumulativeTimeLimit.isEmpty()) {
                continue;
            }

            for (final ExceededTimeLimit exceededTimeLimit : personsExceedingCumulativeTimeLimit) {
                final Person person = competition.getPersonById(exceededTimeLimit.personId());
                final String[] displayNames = exceededTimeLimit.rounds().stream().map(ActivityCode::getDisplayName).toList().toArray(new String[0]);
                messages.add(new Message("Competitor " + person.name() + "'s total solve time in " + join(displayNames) + " is " +
                        renderWithoutCentiseconds(exceededTimeLimit.totalTime()) + " , this exceeds the cumulative time limit of " +
                        renderWithoutCentiseconds(exceededTimeLimit.timeLimit()),
                        Message.Type.WARNING));
            }
        }

        return messages;
    }

}
