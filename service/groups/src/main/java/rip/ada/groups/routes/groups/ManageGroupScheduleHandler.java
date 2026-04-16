package rip.ada.groups.routes.groups;

import io.javalin.http.Context;
import io.pebbletemplates.pebble.PebbleEngine;
import io.javalin.http.HandlerType;
import rip.ada.groups.schedule.GroupScheduleGenerator;
import rip.ada.groups.schedule.ScheduleType;
import rip.ada.groups.session.AuthenticatedCompetitionHandler;
import rip.ada.groups.session.Session;
import rip.ada.wca.AuthenticatedWcaApi;
import rip.ada.wcif.Competition;
import rip.ada.wcif.Venue;

import java.util.ArrayList;
import java.util.List;

import static rip.ada.groups.templates.Templates.model;
import static rip.ada.groups.templates.Templates.render;

public class ManageGroupScheduleHandler extends AuthenticatedCompetitionHandler {

    private final PebbleEngine engine;
    private final AuthenticatedWcaApi wcaApi;

    public ManageGroupScheduleHandler(final AuthenticatedWcaApi wcaApi, final PebbleEngine engine) {
        super(wcaApi);
        this.engine = engine;
        this.wcaApi = wcaApi;
    }

    @Override
    public void handle(final Competition competition, final Session session, final Context ctx) {
        if (ctx.method() == HandlerType.GET) {
            final List<Room> rooms = new ArrayList<>();
            for (final Venue venue : competition.getSchedule().getVenues()) {
                for (final rip.ada.wcif.Room room : venue.getRooms()) {
                    rooms.add(new Room(String.valueOf(room.id()), room.name()));
                }
            }
            model(ctx).put("rooms", rooms);
            render(engine, "manage_group_schedule", ctx);
        } else {
            new GroupScheduleGenerator().generate(competition, ScheduleType.WAVES);
            wcaApi.updateCompetition(session.getWcaSession(), competition);
            ctx.redirect("/");
        }
    }

    private record Room(String id, String name) {}
}
