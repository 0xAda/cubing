package rip.ada.groups.sorting;

import org.junit.jupiter.api.Test;
import rip.ada.wcif.Competition;
import rip.ada.wcif.Person;
import rip.ada.wcif.ResultType;
import rip.ada.wcif.event.OfficialEvent;

import java.util.ArrayList;
import java.util.Comparator;

import static org.junit.jupiter.api.Assertions.*;
import static rip.ada.groups.assigner.Fixtures.getCompetition;

class CompetitorComparatorsTest {

    @Test
    public void shouldSortByPersonalBest() {
        final Competition competition = getCompetition("YorkSummer2026");
        final Comparator<Person> personComparator = CompetitorComparators.sortByPersonalBest(OfficialEvent.THREE_BY_THREE, ResultType.AVERAGE);
        final ArrayList<Person> people = new ArrayList<>(competition.getPersons());
        people.sort(personComparator);

        assertEquals(people.get(0).wcaId(), "2020ARCH01");
    }

    @Test
    public void shouldSortByPersonalBestSingleWhenAverageTies() {
        final Competition competition = getCompetition("YorkSummer2026");
        final Comparator<Person> personComparator = CompetitorComparators.sortByPersonalBest(OfficialEvent.TWO_BY_TWO, ResultType.AVERAGE);
        final ArrayList<Person> people = new ArrayList<>(competition.getPersons());
        people.sort(personComparator);

        assertEquals(people.get(0).wcaId(), "2023YANG43");
        assertEquals(people.get(1).wcaId(), "2023PRAB09");
    }

    @Test
    public void shouldSortByPersonalBestAverageWhenSingleTies() {
        final Competition competition = getCompetition("YorkSummer2026");
        final Comparator<Person> personComparator = CompetitorComparators.sortByPersonalBest(OfficialEvent.TWO_BY_TWO, ResultType.SINGLE);
        final ArrayList<Person> people = new ArrayList<>(competition.getPersons());
        people.sort(personComparator);

        assertEquals(people.get(21).wcaId(), "2023SCHM07");
        assertEquals(people.get(22).wcaId(), "2024HUNT04");
    }

}
