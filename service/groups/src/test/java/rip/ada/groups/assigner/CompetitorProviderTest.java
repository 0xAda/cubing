package rip.ada.groups.assigner;

import org.junit.jupiter.api.Test;
import rip.ada.wcif.ActivityCode;
import rip.ada.wcif.Competition;
import rip.ada.wcif.Person;
import rip.ada.wcif.event.OfficialEvent;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static rip.ada.groups.assigner.Fixtures.getCompetition;

public class CompetitorProviderTest {

    @Test
    public void shouldGetRoundOneCompetitors() {
        final Competition competition = getCompetition("YorkSummer2026");
        final CompetitorProvider competitorProvider = new CompetitorProvider(competition);

        final List<Person> competitors = competitorProvider.getCompetitors(ActivityCode.round(OfficialEvent.THREE_BY_THREE, 1));
        assertEquals(84, competitors.size());
    }

    @Test
    public void shouldGetSecondRoundCompetitors() {
        final Competition competition = getCompetition("BristolAugust2026");
        final CompetitorProvider competitorProvider = new CompetitorProvider(competition);

        final List<Person> competitors = competitorProvider.getCompetitors(ActivityCode.round(OfficialEvent.THREE_BY_THREE, 2));
        assertEquals(62, competitors.size());
    }

    @Test
    public void shouldGetDualRoundCompetitorsFromRegistration() {
        final Competition competition = getCompetition("ManchesterCLFinal2026");
        final CompetitorProvider competitorProvider = new CompetitorProvider(competition);

        final List<Person> competitors = competitorProvider.getCompetitors(ActivityCode.round(OfficialEvent.THREE_BY_THREE, 2));
        assertEquals(44, competitors.size());
    }

}
