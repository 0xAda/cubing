package rip.ada.groups.printing;

import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rip.ada.wcif.*;
import rip.ada.wcif.event.OfficialEvent;

import java.io.IOException;
import java.io.OutputStream;
import java.time.Instant;
import java.util.*;

public class AssignedScramblersPrinter {

    private static final Logger LOGGER = LoggerFactory.getLogger(AssignedScramblersPrinter.class);
    private static final PdfFont HELVETICA;

    static {
        try {
            HELVETICA = PdfFontFactory.createFont("Helvetica");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void printAssignedScramblers(final Competition competition, final OutputStream outputStream) {
        final PdfWriter pdfWriter = new PdfWriter(outputStream);
        final PdfDocument pdf = new PdfDocument(pdfWriter);

        final List<String> lines = getText(competition);
        try (Document document = new Document(pdf)) {
            document.add(new Paragraph(new Text(competition.getName() + " - Assigned Round 1 Scramblers").setFont(HELVETICA).simulateBold()));
            for (final String line : lines) {
                document.add(new Paragraph(new Text(line).setFont(HELVETICA)));
            }
        }
    }

    private List<String> getText(final Competition competition) {
        final Map<ActivityCode, List<String>> scramblers = new HashMap<>();
        for (final Person person : competition.getCompetingPersons()) {
            for (final Assignment assignment : person.assignments()) {
                if (assignment.assignmentCode() != StandardAssignmentCode.SCRAMBLER) {
                    continue;
                }
                final Activity activity = competition.getActivityById(assignment.activityId());
                if (activity == null) {
                    LOGGER.error("Failed to get assignment activity by id, this should never happen {} {} {}", competition.getId(), person.name(), assignment);
                    continue;
                }
                final String latinName = person.name().indexOf('(') == -1 ? person.name() : person.name().split(" \\(")[0];
                scramblers.computeIfAbsent(activity.getActivityCode(), _ -> new ArrayList<>())
                        .add(latinName);
            }
        }

        final List<OfficialEvent> eventOrder = getEventOrder(competition);
        final List<String> scramblerRows = new ArrayList<>();

        for (final OfficialEvent officialEvent : eventOrder) {
            for (int i = 1; i < Integer.MAX_VALUE; i++) {
                final ActivityCode r1group = new ActivityCode(officialEvent, 1, i, null);
                final List<String> assignedScramblers = scramblers.get(r1group);
                if (assignedScramblers == null) {
                    break;
                }
                assignedScramblers.sort(Comparator.comparing(x -> x));
                scramblerRows.add(officialEvent.getFriendlyName() + " Group " + i + " - " + String.join(", ", assignedScramblers));
            }
            scramblerRows.add("");
        }
        return scramblerRows;
    }

    private List<OfficialEvent> getEventOrder(final Competition competition) {
        final List<EventStartTime> eventStartTimes = new ArrayList<>();
        for (final Venue venue : competition.getSchedule().getVenues()) {
            for (final Room room : venue.getRooms()) {
                for (final Activity activity : room.activities()) {
                    if (activity.getActivityCode().event() instanceof OfficialEvent oe && activity.getActivityCode().round() == 1) {
                        eventStartTimes.add(new EventStartTime(activity.getStartTime(), oe));
                    }
                }
            }
        }
        eventStartTimes.sort(Comparator.comparing(eventStartTime -> eventStartTime.startTime));

        return eventStartTimes.stream().map(EventStartTime::officialEvent).toList();
    }

    private record EventStartTime(Instant startTime, OfficialEvent officialEvent) {

    }

}
