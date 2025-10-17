package rip.ada.links;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rip.ada.wca.UnauthenticatedWcaApi;
import rip.ada.wca.model.CompetitionInfo;
import rip.ada.wcascraper.ScrapedCompetition;
import rip.ada.wcascraper.WcaScraper;
import rip.ada.wcif.CountryCode;
import rip.ada.wcif.Event;
import rip.ada.wcif.Schedule;

import java.time.LocalDate;
import java.util.*;

public class Competitions {
    private static final Logger LOGGER = LoggerFactory.getLogger(Competitions.class);

    private final WcaScraper wcaScraper;
    private final UnauthenticatedWcaApi wcaApi;

    private volatile Map<String, Competition> competitions = new HashMap<>();
    private volatile Map<Sponsor, List<Competition>> sponsoredCompetitions = new HashMap<>();
    private volatile List<Competition> liveCompetitions = new ArrayList<>();

    public Competitions(final WcaScraper wcaScraper, final UnauthenticatedWcaApi wcaApi) {
        this.wcaScraper = wcaScraper;
        this.wcaApi = wcaApi;
    }

    public Competition get(final String id) {
        return competitions.get(id);
    }

    public List<Competition> getSponsoredBy(final Sponsor sponsor) {
        return sponsoredCompetitions.get(sponsor);
    }

    public List<Competition> getLiveCompetitions() {
        return liveCompetitions;
    }

    public void fetchCompetitions() {
        final LocalDate now = LocalDate.now();

        final Map<String, Competition> updatedCompetitions = new HashMap<>();
        final Map<Sponsor, List<Competition>> updatedSponsoredCompetitions = new HashMap<>();
        final List<Competition> updatedLiveCompetitions = new ArrayList<>();

        try {
            final List<CompetitionInfo> listedCompetitions = wcaApi.getUpcomingCompetitionsInCountry(CountryCode.GB, 5);
            for (final CompetitionInfo competitionInfo : listedCompetitions) {
                final rip.ada.wcif.Competition wcif = wcaApi.getCompetitionPublic(competitionInfo.id());
                final ScrapedCompetition scrapedCompetition = wcaScraper.scrapeCompetition(competitionInfo.id());

                final boolean isIrishComp = scrapedCompetition.tabs().containsKey("Speedcubing Ireland");

                final List<Sponsor> sponsors = getSponsors(scrapedCompetition);

                final Schedule schedule = wcif.getSchedule();
                final Competition competition = new Competition(wcif.getName(),
                        wcif.getId(),
                        isIrishComp,
                        schedule.getStartDate(),
                        schedule.getStartDate().plusDays(schedule.getNumberOfDays() - 1),
                        wcif.getEvents().stream().map(Event::eventType).toList(),
                        sponsors);

                updatedCompetitions.put(wcif.getId(), competition);
                for (final Sponsor sponsor : competition.sponsor()) {
                    updatedSponsoredCompetitions.computeIfAbsent(sponsor, _ -> new ArrayList<>()).add(competition);
                }

                final LocalDate displayAfter = now.minusDays(5);
                final LocalDate displayBefore = now.plusDays(2);
                if (competition.startDate().isAfter(displayAfter) && competition.endDate().isBefore(displayBefore)) {
                    updatedLiveCompetitions.add(competition);
                }
            }
            competitions = updatedCompetitions;
            sponsoredCompetitions = updatedSponsoredCompetitions;
            liveCompetitions = updatedLiveCompetitions;
        } catch (final Exception e) {
            LOGGER.error("Failed to fetch competitions", e);
        }
    }

    private static List<Sponsor> getSponsors(final ScrapedCompetition scrapedCompetition) {
        final List<Sponsor> sponsors = new ArrayList<>();
        for (final Sponsor sponsor : Sponsor.values()) {
            for (final String tab : scrapedCompetition.tabs().keySet()) {
                final String lowerCaseTab = tab.toLowerCase(Locale.UK);
                if (lowerCaseTab.contains(sponsor.getName().toLowerCase(Locale.UK)) && !lowerCaseTab.contains("strathclyde")) {
                    sponsors.add(sponsor);
                }
            }
        }
        return sponsors;
    }
}
