package rip.ada.links;

import rip.ada.wca.UnauthenticatedWcaApi;
import rip.ada.wca.model.CompetitionInfo;
import rip.ada.wcascraper.ScrapedCompetition;
import rip.ada.wcascraper.WcaScraper;
import rip.ada.wcif.CountryCode;
import rip.ada.wcif.Event;
import rip.ada.wcif.Schedule;

import java.util.*;

public class Competitions {
    private final WcaScraper wcaScraper;
    private final UnauthenticatedWcaApi wcaApi;

    private volatile Map<String, Competition> competitions = new HashMap<>();
    private volatile Map<Sponsor, List<Competition>> sponsoredCompetitions = new HashMap<>();

    public Competitions(final WcaScraper wcaScraper, final UnauthenticatedWcaApi wcaApi) {
        this.wcaScraper = wcaScraper;
        this.wcaApi = wcaApi;
    }

    public Collection<Competition> getAll() {
        return competitions.values();
    }

    public Competition get(final String id) {
        return competitions.get(id);
    }

    public List<Competition> getSponsoredBy(final Sponsor sponsor) {
        return sponsoredCompetitions.get(sponsor);
    }

    public void fetchCompetitions() {
        final Map<String, Competition> updatedCompetitions = new HashMap<>();
        final Map<Sponsor, List<Competition>> updatedSponsoredCompetitions = new HashMap<>();
        for (final CompetitionInfo competitionInfo : wcaApi.getUpcomingCompetitionsInCountry(CountryCode.GB)) {
            final rip.ada.wcif.Competition wcif = wcaApi.getCompetitionPublic(competitionInfo.id());
            final ScrapedCompetition scrapedCompetition = wcaScraper.scrapeCompetition(competitionInfo.id());

            final List<Sponsor> sponsors = new ArrayList<>();
            for (final Sponsor sponsor : Sponsor.values()) {
                for (final String tab : scrapedCompetition.tabs().keySet()) {
                    final String lowerCaseTab = tab.toLowerCase(Locale.UK);
                    if (lowerCaseTab.contains(sponsor.getName().toLowerCase(Locale.UK)) && !lowerCaseTab.contains("strathclyde")) {
                        sponsors.add(sponsor);
                    }
                }
            }

            final Schedule schedule = wcif.getSchedule();
            final Competition competition = new Competition(wcif.getName(),
                    wcif.getId(),
                    scrapedCompetition.tabs().containsKey("Speedcubing Ireland"),
                    schedule.getStartDate(),
                    schedule.getStartDate().plusDays(schedule.getNumberOfDays()),
                    wcif.getEvents().stream().map(Event::eventType).toList(),
                    sponsors);

            updatedCompetitions.put(wcif.getId(), competition);
            for (final Sponsor sponsor : competition.sponsor()) {
                updatedSponsoredCompetitions.computeIfAbsent(sponsor, _ -> new ArrayList<>()).add(competition);
            }
        }
        competitions = updatedCompetitions;
        sponsoredCompetitions = updatedSponsoredCompetitions;
        System.out.println("Updated competitions");
    }
}
