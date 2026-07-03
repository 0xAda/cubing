package rip.ada.groups.routes.advanced;

import io.javalin.http.Context;
import io.pebbletemplates.pebble.PebbleEngine;
import rip.ada.groups.session.AuthenticatedCompetitionHandler;
import rip.ada.groups.session.Session;
import rip.ada.groups.templates.Message;
import rip.ada.wca.AuthenticatedWcaApi;
import rip.ada.wcif.*;

import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;

import static rip.ada.groups.templates.Templates.model;
import static rip.ada.groups.templates.Templates.render;

public class CloneScheduleHandler extends AuthenticatedCompetitionHandler {

    private final PebbleEngine engine;
    private final AuthenticatedWcaApi wcaApi;

    public CloneScheduleHandler(final AuthenticatedWcaApi wcaApi, final PebbleEngine engine) {
        super(wcaApi);
        this.engine = engine;
        this.wcaApi = wcaApi;
    }

    @Override
    public void handle(final Competition competition, final Session session, final Context ctx) throws Exception {
        final String otherComp = ctx.formParam("otherComp");
        if (otherComp == null || otherComp.isBlank()) {
            model(ctx).put("messages", List.of(new Message("Specify a comp", Message.Type.ERROR)));
            render(engine, "advanced", ctx);
            return;
        }

        try {
            final Competition competitionPublic = wcaApi.getCompetitionPublic(otherComp);

            if (competitionPublic.getSchedule().getNumberOfDays() != competition.getSchedule().getNumberOfDays()) {
                model(ctx).put("messages", List.of(new Message("Comps must be the same number of days", Message.Type.ERROR)));
                render(engine, "advanced", ctx);
                return;
            }

            final Schedule oldSchedule = competition.getSchedule();

            final int idOffset = competition.getNextActivityId();

            competition.setEvents(competitionPublic.getEvents());
            competition.setSchedule(competitionPublic.getSchedule());
            for (final Person person : competition.getPersons()) {
                person.assignments().clear();
            }

            for (final Venue venue : competition.getSchedule().getVenues()) {
                for (final Room room : venue.getRooms()) {
                    recursivelyFixTimestamps(competition, oldSchedule, room.activities());
                }
            }

            final List<Event> eventsWithoutQualification = new ArrayList<>();
            for (final Event event : competition.getEvents()) {
                eventsWithoutQualification.add(new Event(
                        event.eventType(),
                        event.rounds(),
                        event.competitorLimit(),
                        null,
                        event.extensions()
                ));
            }

            competitionPublic.getSchedule().setStartDate(oldSchedule.getStartDate());

            competition.setEvents(eventsWithoutQualification);

            for (final Venue venue : competition.getSchedule().getVenues()) {
                for (final Room room : venue.getRooms()) {
                    recursivelyFixIds(idOffset, room.activities());
                }
            }

            wcaApi.updateCompetition(session.getWcaSession(), competition);
            model(ctx).put("messages", List.of(new Message("Cloned schedule", Message.Type.SUCCESS)));
            render(engine, "advanced", ctx);
        } catch (final Exception e) {
            model(ctx).put("messages", List.of(new Message("Failed to import schedule from " + otherComp + " does it exist?", Message.Type.ERROR)));
            render(engine, "advanced", ctx);
        }
    }

    private void recursivelyFixTimestamps(final Competition otherComp, final Schedule oldSchedule, final List<Activity> activities) {

        final Instant otherCompStartTime = otherComp.getSchedule().getStartDate().atStartOfDay().atOffset(ZoneOffset.UTC).toInstant();
        final Instant compStartTime = oldSchedule.getStartDate().atStartOfDay().atOffset(ZoneOffset.UTC).toInstant();
        for (final Activity activity : activities) {
            final Duration startTimeSinceStart = Duration.between(otherCompStartTime, activity.getStartTime());
            final Instant newStartTime = compStartTime.plus(startTimeSinceStart);
            activity.setStartTime(newStartTime);

            final Duration endTimeSinceStart = Duration.between(otherCompStartTime, activity.getEndTime());
            final Instant newEndTime = compStartTime.plus(endTimeSinceStart);
            activity.setEndTime(newEndTime);

            recursivelyFixTimestamps(otherComp, oldSchedule, activity.getChildActivities());
        }
    }

    private void recursivelyFixIds(final int offset, final List<Activity> activities) {
        for (final Activity activity : activities) {
            activity.setId(activity.getId() + offset);

            recursivelyFixIds(offset, activity.getChildActivities());
        }
    }
}
