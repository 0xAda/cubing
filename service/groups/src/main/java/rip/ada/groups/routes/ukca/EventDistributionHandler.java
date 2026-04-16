package rip.ada.groups.routes.ukca;

import io.javalin.http.Context;
import io.pebbletemplates.pebble.PebbleEngine;
import rip.ada.groups.report.Report;
import rip.ada.groups.session.AuthenticatedHandler;
import rip.ada.groups.session.Session;
import rip.ada.wca.AuthenticatedWcaApi;
import rip.ada.wca.model.CompetitionInfo;
import rip.ada.wcif.Competition;
import rip.ada.wcif.Event;
import rip.ada.wcif.Venue;
import rip.ada.wcif.event.OfficialEvent;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static rip.ada.groups.templates.Templates.model;
import static rip.ada.groups.templates.Templates.render;

public class EventDistributionHandler extends AuthenticatedHandler {

    private static final List<CompRegion> COMP_REGIONS = List.of(
            new CompRegion("Scotland", -7.7941, 55.2163, -1.0595, 59.3274),
            new CompRegion("Proper North", -5.0186, 53.679, 0.971, 55.4397),
            new CompRegion("Nev Land", -5.0175, 52.886, -1.0167, 53.6799),
            new CompRegion("West Midlands", -5.017, 51.6866, -1.016, 52.8869),
            new CompRegion("Other Midlands", -1.0167, 51.6875, 2.1925, 52.8876),
            new CompRegion("France", -1.5936, 50.5032, 2.1932, 51.6883),
            new CompRegion("Weston", -5.9737, 49.7927, -1.1706, 51.7759)
    );
    private final PebbleEngine engine;
    private final AuthenticatedWcaApi wcaApi;

    public EventDistributionHandler(final PebbleEngine engine, final AuthenticatedWcaApi wcaApi) {
        this.engine = engine;
        this.wcaApi = wcaApi;
    }

    @Override
    public void handle(final Session session, final Context ctx) {
        final Map<String, EventCounter> eventCounts = new HashMap<>();
        for (final CompetitionInfo competition : session.getCompetitions()) {
            final Competition comp = session.getStaleCachedCompetition(competition.id(), wcaApi);
            if (comp.getSchedule().getVenues().isEmpty()) {
                continue;
            }
            final Venue venue = comp.getSchedule().getVenues().get(0);
            final String region = findClosestRegion(venue);
            final EventCounter regionEventCounter = eventCounts.computeIfAbsent(region, k -> new EventCounter());
            final EventCounter ukEventCounter = eventCounts.computeIfAbsent("UK", k -> new EventCounter());
            final int monthsUntil = (int) LocalDate.now().until(comp.getSchedule().getStartDate(), ChronoUnit.MONTHS);
            if (monthsUntil > 24) {
                continue;
            }
            for (final Event event : comp.getEvents()) {
                regionEventCounter.counts[event.eventType().ordinal()][monthsUntil] += event.rounds().size();
                if (!"British Empire".equals(region)) {
                    ukEventCounter.counts[event.eventType().ordinal()][monthsUntil] += event.rounds().size();
                }
            }
        }

        final List<Report> report = new ArrayList<>();
        final int[] periods = new int[]{3, 6, 9, 12, 24};
        for (final String region : eventCounts.keySet()) {
            final List<List<String>> rows = new ArrayList<>();
            for (final OfficialEvent event : OfficialEvent.values()) {
                final List<String> row = new ArrayList<>();
                row.add(event.getFriendlyName());
                for (final int period : periods) {
                    row.add(String.valueOf(eventCounts.get(region).getOccurenceInMonths(period, event)));
                }
                rows.add(row);
            }
            report.add(new Report(region, List.of("Event", "3M", "6M", "9M", "12M", "24M"), rows));
        }

        model(ctx).put("report", report);
        render(engine, "display_report", ctx);
    }

    private String findClosestRegion(final Venue venue) {
        for (final CompRegion compRegion : COMP_REGIONS) {
            final double venueLat = (double) venue.getLatitudeMicroDegrees() / 1000000;
            final double venueLon = (double) venue.getLongitudeMicroDegrees() / 1000000;
            if (venueLat > compRegion.minLat && venueLon > compRegion.minLon && venueLat < compRegion.maxLat && venueLon < compRegion.maxLon) {
                return compRegion.name;
            }
        }
        return "British Empire";
    }

    private static class EventCounter {
        public final int[][] counts = new int[OfficialEvent.values().length][25];

        public int getOccurenceInMonths(final int months, final OfficialEvent event) {
            int total = 0;
            for (int i = 0; i < months; i++) {
                total += counts[event.ordinal()][i];
            }
            return total;
        }
    }

    private record CompRegion(String name, double minLon, double minLat, double maxLon, double maxLat) {}
}
