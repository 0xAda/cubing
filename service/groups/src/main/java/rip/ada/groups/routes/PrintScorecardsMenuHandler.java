package rip.ada.groups.routes;

import io.javalin.http.Context;
import io.pebbletemplates.pebble.PebbleEngine;
import rip.ada.groups.session.AuthenticatedCompetitionHandler;
import rip.ada.groups.session.Session;
import rip.ada.groups.wca.WcaApi;
import rip.ada.wcif.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static rip.ada.groups.templates.Templates.model;
import static rip.ada.groups.templates.Templates.render;

public class PrintScorecardsMenuHandler extends AuthenticatedCompetitionHandler {

    private final PebbleEngine engine;

    public PrintScorecardsMenuHandler(final WcaApi wcaApi, final PebbleEngine engine) {
        super(wcaApi);
        this.engine = engine;
    }

    @Override
    public void handle(final Competition competition, final Session session, final Context ctx) {
        model(ctx).put("competition_name", competition.getName());
        model(ctx).put("competition_id", competition.getId());
        model(ctx).put("competition", competition);

        final List<Map<String, String>> rounds = new ArrayList<>();
        final List<Map<String, String>> enteredRounds = new ArrayList<>();
        for (final Event event : competition.getEvents()) {
            for (final Round round : event.rounds()) {
                if (!roundHasAssignments(competition, round)) {
                    continue;
                }

                boolean resultsEntered = false;
                for (final Result result : round.results()) {
                    if (!result.attempts().isEmpty()) {
                        resultsEntered = true;
                        break;
                    }
                }
                final Map<String, String> roundData = new HashMap<>();
                roundData.put("activity_code", round.activityCode().toString());
                roundData.put("round_name", round.activityCode().getDisplayName());
                if (!resultsEntered) {
                    rounds.add(roundData);
                } else {
                    enteredRounds.add(roundData);
                }
            }
        }

        model(ctx).put("rounds", rounds);
        model(ctx).put("enteredRounds", enteredRounds);

        final List<String> rooms = new ArrayList<>();
        competition.getSchedule().getVenues().forEach(venue -> venue.getRooms().forEach(room -> rooms.add(room.name())));
        model(ctx).put("rooms", rooms);

        render(engine, "printing", ctx);
    }

    private boolean roundHasAssignments(final Competition competition, final Round round) {
        for (final Person person : competition.getPersons()) {
            for (final Assignment assignment : person.assignments()) {
                for (final Venue venue : competition.getSchedule().getVenues()) {
                    for (final Room room : venue.getRooms()) {
                        for (final Activity activity : room.activities()) {
                            for (final Activity childActivity : activity.getChildActivities()) {
                                if (childActivity.getId() == assignment.activityId() &&
                                        assignment.assignmentCode().equals(StandardAssignmentCode.COMPETITOR) &&
                                        childActivity.getActivityCode().event() == round.event() &&
                                        childActivity.getActivityCode().round().equals(round.roundNumber())) {
                                    return true;
                                }
                            }
                        }
                    }
                }
            }
        }
        return false;
    }
}
