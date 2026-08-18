package rip.ada.groups.sorting;

import rip.ada.wcif.Person;
import rip.ada.wcif.ResultType;
import rip.ada.wcif.ResultValue;
import rip.ada.wcif.event.OfficialEvent;

import java.util.Comparator;

public class CompetitorComparators {

    public static Comparator<Person> sortByPersonalBest(final OfficialEvent event, final ResultType resultType) {
        final ResultType tieBreakResultType = resultType == ResultType.AVERAGE
                ? ResultType.SINGLE
                : ResultType.AVERAGE;

        return Comparator
                .comparingInt((Person person) -> personalBest(person, event, resultType))
                .thenComparingInt(person -> personalBest(person, event, tieBreakResultType));
    }

    private static int personalBest(final Person person,
                                    final OfficialEvent event,
                                    final ResultType resultType) {
        final ResultValue personalBest = person.getPersonalBest(event, resultType);
        return personalBest.isSuccess() ? personalBest.value() : Integer.MAX_VALUE;
    }

}
