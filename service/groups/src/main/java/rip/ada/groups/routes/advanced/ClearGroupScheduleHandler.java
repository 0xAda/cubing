package rip.ada.groups.routes.advanced;

import io.javalin.http.Context;
import io.pebbletemplates.pebble.PebbleEngine;
import rip.ada.groups.session.AuthenticatedCompetitionHandler;
import rip.ada.groups.session.Session;
import rip.ada.groups.templates.Message;
import rip.ada.groups.wca.WcaApi;
import rip.ada.wcif.Activity;
import rip.ada.wcif.Competition;
import rip.ada.wcif.Room;
import rip.ada.wcif.Venue;

import java.util.ArrayList;
import java.util.List;

import static rip.ada.groups.templates.Templates.model;
import static rip.ada.groups.templates.Templates.render;

public class ClearGroupScheduleHandler extends AuthenticatedCompetitionHandler {

    private final PebbleEngine engine;
    private final WcaApi wcaApi;

    public ClearGroupScheduleHandler(final WcaApi wcaApi, final PebbleEngine engine) {
        super(wcaApi);
        this.engine = engine;
        this.wcaApi = wcaApi;
    }

    @Override
    public void handle(final Competition competition, final Session session, final Context ctx) {
        for (final Venue venue : competition.getSchedule().getVenues()) {
            for (final Room room : venue.getRooms()) {
                clearGroups(room.activities());
            }
        }

        wcaApi.updateCompetition(session.getWcaSession(), competition);

        model(ctx).put("messages", List.of(new Message("Cleared group schedule", Message.Type.SUCCESS)));
        render(engine, "advanced", ctx);
    }

    private void clearGroups(final Activity activity) {
        for (final Activity childActivity : activity.getChildActivities()) {
            clearGroups(childActivity);
        }
        final List<Activity> childActivities = new ArrayList<>(activity.getChildActivities());
        childActivities.removeIf(a -> a.getActivityCode().group() != null);
        activity.setChildActivities(childActivities);
    }

    private void clearGroups(final List<Activity> activityList) {
        activityList.forEach(this::clearGroups);
    }
}
