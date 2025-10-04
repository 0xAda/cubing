package rip.ada.wcascraper;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class CompetitionScrapingTest {
    @Test
    public void shouldParseBelfastAutumn() {
        final ScrapedCompetition scrapedCompetition = ScrapedCompetition.fromString(readResource("BelfastAutumn2025.html"));
        assertFalse(scrapedCompetition.isChampionship());
        assertTrue(scrapedCompetition.tabs().containsKey("Speedcubing Ireland"));
        assertTrue(scrapedCompetition.tabs().get("UKCA").contains("completely volunteer-run organisation"));
    }

    @Test
    public void shouldParseRUKC() {
        final ScrapedCompetition scrapedCompetition = ScrapedCompetition.fromString(readResource("RubiksUKChampionship2025.html"));
        assertTrue(scrapedCompetition.isChampionship());
        assertTrue(scrapedCompetition.tabs().containsKey("Guests, Spectators, and Media"));
        assertTrue(scrapedCompetition.tabs().get("UKCA").contains("completely volunteer-run organisation"));
    }

    private String readResource(final String resource) {
        try (InputStream is = CompetitionScrapingTest.class.getClassLoader().getResourceAsStream(resource)) {
            return new String(is.readAllBytes(), StandardCharsets.UTF_8);
        } catch (final IOException e) {
            throw new RuntimeException(e);
        }
    }
}
