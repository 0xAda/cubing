package rip.ada.groups.routes.ukca;

import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.model.ValueRange;
import io.javalin.http.Context;
import io.pebbletemplates.pebble.PebbleEngine;
import rip.ada.groups.schedule.CompSchedule;
import rip.ada.groups.schedule.ScheduleEntry;
import rip.ada.groups.session.AuthenticatedCompetitionHandler;
import rip.ada.groups.session.Session;
import rip.ada.groups.templates.Message;
import rip.ada.wca.AuthenticatedWcaApi;
import rip.ada.wcif.*;
import rip.ada.wcif.event.OfficialEvent;
import rip.ada.wcif.event.ScheduleEvent;
import rip.ada.wcif.event.UnofficialEvent;

import java.time.Instant;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.*;

import static rip.ada.groups.templates.Templates.model;
import static rip.ada.groups.templates.Templates.render;

public class ScheduleImportHandler extends AuthenticatedCompetitionHandler {

    private static final Map<String, EventType> EVENT_TYPE_MAP = Map.ofEntries(
            Map.entry("Competing and Judging Tutorial", ScheduleEvent.TUTORIAL),
            Map.entry("Lunch", ScheduleEvent.LUNCH),
            Map.entry("Lunch Saturday", ScheduleEvent.LUNCH),
            Map.entry("Lunch Sunday", ScheduleEvent.LUNCH),
            Map.entry("Awards", ScheduleEvent.AWARDS),
            Map.entry("Registration Opens", ScheduleEvent.REGISTRATION),
            Map.entry("Set Up", ScheduleEvent.SETUP),
            Map.entry("Pack Down", ScheduleEvent.TEARDOWN),
            Map.entry("Mystery Event", UnofficialEvent.MYSTERY),
            Map.entry("3x3 Head to Head", OfficialEvent.THREE_BY_THREE),
            Map.entry("3x3", OfficialEvent.THREE_BY_THREE),
            Map.entry("2x2", OfficialEvent.TWO_BY_TWO),
            Map.entry("4x4", OfficialEvent.FOUR_BY_FOUR),
            Map.entry("5x5", OfficialEvent.FIVE_BY_FIVE),
            Map.entry("6x6", OfficialEvent.SIX_BY_SIX),
            Map.entry("7x7", OfficialEvent.SEVEN_BY_SEVEN),
            Map.entry("3BLD", OfficialEvent.THREE_BLIND),
            Map.entry("4BLD", OfficialEvent.FOUR_BLIND),
            Map.entry("5BLD", OfficialEvent.FIVE_BLIND),
            Map.entry("OH", OfficialEvent.ONE_HANDED),
            Map.entry("Clock", OfficialEvent.CLOCK),
            Map.entry("Megaminx", OfficialEvent.MEGAMINX),
            Map.entry("Pyraminx", OfficialEvent.PYRAMINX),
            Map.entry("Skewb", OfficialEvent.SKEWB),
            Map.entry("Square-1", OfficialEvent.SQUARE_ONE),
            Map.entry("MBLD", OfficialEvent.MULTI_BLIND),
            Map.entry("FMC", OfficialEvent.FMC)
    );
    static int index = 1;
    private final AuthenticatedWcaApi wcaApi;
    private final PebbleEngine engine;

    public ScheduleImportHandler(final AuthenticatedWcaApi wcaApi, final PebbleEngine engine) {
        super(wcaApi);
        this.wcaApi = wcaApi;
        this.engine = engine;
    }

    private static ActivityCode createActivityCode(final EventType cumulativeEventType, final Integer roundNumber) {
        if (cumulativeEventType == OfficialEvent.MULTI_BLIND || cumulativeEventType == OfficialEvent.FMC) {
            return new ActivityCode(cumulativeEventType, 1, null, roundNumber);
        }
        return new ActivityCode(cumulativeEventType, roundNumber, null, null);
    }

    @Override
    public void handle(final Competition competition, final Session session, final Context ctx) throws Exception {
        if (session.getGoogleCredential() == null) {
            model(ctx).put("messages", List.of(new Message("Not signed in with Google.", Message.Type.ERROR)));
            render(engine, "ukca", ctx);
            return;
        }

        final String spreadsheetUrl = ctx.formParam("spreadsheet_url");
        final String spreadsheetId = spreadsheetUrl.split("/spreadsheets/d/")[1].split("/")[0];

        final HttpTransport httpTransport = new NetHttpTransport();
        final JsonFactory jsonFactory = GsonFactory.getDefaultInstance();

        final Sheets sheetsService = new Sheets.Builder(httpTransport, jsonFactory, session.getGoogleCredential())
                .setApplicationName("Ada Groups")
                .build();

        final ValueRange response;
        try {
            final String range = "G!A1:U100";
            response = sheetsService.spreadsheets().values()
                    .get(spreadsheetId, range)
                    .execute();
        } catch (final Exception e) {
            throw new RuntimeException(e);
        }

        final List<List<Object>> rows = response.getValues();
        if (rows == null || rows.isEmpty()) {
            model(ctx).put("messages", List.of(new Message("Sheet returned no data.", Message.Type.ERROR)));
            render(engine, "ukca", ctx);
            return;
        }

        final List<Object> firstRow = rows.getFirst();
        final int dayIndex = firstRow.indexOf("Day");
        final int timeIndex = firstRow.indexOf("Time");
        final int lengthIndex = firstRow.indexOf("Length");
        final int eventIndex = firstRow.indexOf("Event");
        final int roundIndex = firstRow.indexOf("Round");
        final int groupIndex = firstRow.indexOf("Groups");
        final int timeLimitIndex = firstRow.indexOf("Time Limit");
        final int cutoffIndex = firstRow.indexOf("Cutoff");
        final int progressionIndex = firstRow.indexOf("Progression");

        final Map<OfficialEvent, List<Round>> rounds = new HashMap<>();
        final List<ScheduleEntry> scheduleEntries = new ArrayList<>();

        for (int i = 1; i < rows.size(); i++) {
            final List<Object> row = rows.get(i);
            final String day = (String) row.get(dayIndex);
            final String time = (String) row.get(timeIndex);
            final String length = (String) row.get(lengthIndex);
            final String event = (String) row.get(eventIndex);
            final String round = (String) row.get(roundIndex);
            final String groups = (String) row.get(groupIndex);
            if (event.isBlank()) {
                continue;
            }
            final Integer roundNumber = round.isBlank() ? null : Integer.valueOf(round);

            final String[] timeParts = time.split(":");
            int minutesOffset = Integer.parseInt(timeParts[0]) * 60 + Integer.parseInt(timeParts[1]);
            if (day.equals("Sunday") && rows.get(1).get(dayIndex).equals("Saturday")) {
                minutesOffset += 24 * 60;
            }

            final Instant startTime = competition.getSchedule().getStartDate().atStartOfDay().atZone(ZoneId.of("Europe/London")).toInstant().plus(minutesOffset, ChronoUnit.MINUTES);

            final String[] lengthParts = length.split(":");
            final int lengthMinutes = Integer.parseInt(lengthParts[0]) * 60 + Integer.parseInt(lengthParts[1]);
            final Instant endTime = startTime.plus(lengthMinutes, ChronoUnit.MINUTES);

            final String[] events;
            if (event.endsWith("Cumulative")) {
                if (event.toLowerCase(Locale.ROOT).startsWith("4/5bld")) {
                    events = new String[]{"4BLD", "5BLD"};
                } else {
                    events = event.split(" ")[0].split("/");
                }
            } else {
                events = new String[]{event};
            }

            for (final String eventName : events) {
                final EventType eventType = EVENT_TYPE_MAP.get(eventName.trim());

                if (eventType == null) {
                    throw new RuntimeException("Failed to get event " + eventName);
                }

                if (eventType instanceof OfficialEvent oe && !((eventType == OfficialEvent.FMC || eventType == OfficialEvent.MULTI_BLIND) && (roundNumber != null && roundNumber != 1))) {
                    final ActivityCode activityCode = new ActivityCode(oe, roundNumber, null, null);
                    final ActivityCode[] cumulativeRounds;
                    if (events.length == 1) {
                        cumulativeRounds = new ActivityCode[0];
                    } else {
                        cumulativeRounds = new ActivityCode[events.length - 1];
                        int idx = 0;
                        for (final String cumulativeEvent : events) {
                            if (cumulativeEvent.equals(eventName)) {
                                continue;
                            }
                            final EventType cumulativeEventType = EVENT_TYPE_MAP.get(cumulativeEvent.trim());
                            if (cumulativeEventType == null) {
                                throw new RuntimeException("Failed to get event " + eventName);
                            }
                            if (!(cumulativeEventType instanceof OfficialEvent)) {
                                throw new RuntimeException("Cannot import cumulative event " + eventName + " for official event " + oe.name());
                            }
                            cumulativeRounds[idx++] = createActivityCode(cumulativeEventType, roundNumber);
                        }
                    }

                    rounds.computeIfAbsent(oe, x -> new ArrayList<>()).add(new Round(
                            activityCode,
                            oe.getPreferredRoundFormat(),
                            parseTimeLimit(timeLimitIndex, row, activityCode, cumulativeRounds),
                            parseCutoff(cutoffIndex, row, oe.getPreferredRoundFormat()),
                            parseAdvancementCondition(progressionIndex, row),
                            List.of(),
                            Integer.parseInt(groups),
                            List.of(),
                            List.of()
                    ));
                }

                scheduleEntries.add(new ScheduleEntry(eventType, roundNumber, startTime, endTime));
            }
        }

        final CompSchedule schedule = new CompSchedule(scheduleEntries);

        for (final Venue venue : competition.getSchedule().getVenues()) {
            for (final Room room : venue.getRooms()) {
                room.activities().clear();
                for (final ScheduleEntry scheduleEntry : schedule.schedule()) {
                    final ActivityCode activityCode = createActivityCode(scheduleEntry.event(), scheduleEntry.round());
                    final Activity activity = new Activity(index++,
                            activityCode.getDisplayName(),
                            activityCode,
                            scheduleEntry.startTime(),
                            scheduleEntry.endTime(),
                            new ArrayList<>(),
                            null,
                            List.of()
                    );
                    room.activities().add(activity);
                }
            }
        }

        competition.getEvents().clear();

        for (final Map.Entry<OfficialEvent, List<Round>> eventRounds : rounds.entrySet()) {
            competition.getEvents().add(new Event(
                    eventRounds.getKey(),
                    eventRounds.getValue(),
                    null,
                    null,
                    List.of()
            ));
        }

        wcaApi.updateCompetition(session.getWcaSession(), competition);

        model(ctx).put("messages", List.of(new Message("Imported schedule from sheet.", Message.Type.SUCCESS)));
        render(engine, "ukca", ctx);
    }

    private AdvancementCondition parseAdvancementCondition(final int rowIndex, final List<Object> row) {
        if (rowIndex == -1 || row.get(rowIndex).toString().isBlank()) {
            return null;
        }
        final String advancementCondition = row.get(rowIndex).toString();
        if (advancementCondition.endsWith("%")) {
            return new PercentAdvancementCondition(Integer.parseInt(advancementCondition.split("%")[0]));
        }
        return new RankingAdvancementCondition(Integer.parseInt(advancementCondition));
    }

    private Cutoff parseCutoff(final int rowIndex, final List<Object> row, final RoundFormat roundFormat) {
        if (rowIndex == -1 || row.get(rowIndex).toString().isBlank() || roundFormat.getAllowedFirstPhaseFormats().length == 0) {
            return null;
        }
        final int attemptCount = roundFormat.getAllowedFirstPhaseFormats()[0].getSolveCount();
        final int targetTime = timeToCentiseconds(row.get(rowIndex).toString());
        return new Cutoff(attemptCount, new AttemptResult(targetTime));
    }

    private TimeLimit parseTimeLimit(final int rowIndex, final List<Object> row, final ActivityCode currentRound, final ActivityCode... cumulativeRounds) {
        if (currentRound.event() == OfficialEvent.FMC || currentRound.event() == OfficialEvent.MULTI_BLIND) {
            return null;
        }
        if (rowIndex == -1) {
            return new TimeLimit(new AttemptResult(100), List.of());
        }
        final String timeLimit = (String) row.get(rowIndex);
        if (timeLimit.endsWith(" Cumulative")) {
            final int time = timeToCentiseconds(timeLimit.split(" ")[0]);
            final List<ActivityCode> cumulativeRoundsList = new ArrayList<>();
            cumulativeRoundsList.add(currentRound);
            cumulativeRoundsList.addAll(List.of(cumulativeRounds));
            return new TimeLimit(new AttemptResult(time), cumulativeRoundsList);
        }
        return new TimeLimit(new AttemptResult(timeToCentiseconds(timeLimit)), List.of());
    }

    private int timeToCentiseconds(final String time) {
        if (time.endsWith(" Hour")) {
            return 360000 * Integer.parseInt(time.split(" ")[0]);
        }
        final String[] parts = time.split("\\.")[0].split(":");
        final int seconds = Integer.parseInt(parts[parts.length - 1]);
        final int minutes = parts.length >= 2 ? Integer.parseInt(parts[parts.length - 2]) : 0;
        final int hours = parts.length >= 3 ? Integer.parseInt(parts[parts.length - 3]) : 0;
        return (seconds + minutes * 60 + hours * 3600) * 100;
    }
}
