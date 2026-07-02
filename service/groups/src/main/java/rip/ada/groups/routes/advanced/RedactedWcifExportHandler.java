package rip.ada.groups.routes.advanced;

import io.javalin.http.ContentType;
import io.javalin.http.Context;
import io.javalin.http.Header;
import rip.ada.groups.session.AuthenticatedCompetitionHandler;
import rip.ada.groups.session.Session;
import rip.ada.wca.AuthenticatedWcaApi;
import rip.ada.wcif.Competition;
import rip.ada.wcif.Person;
import rip.ada.wcif.Registration;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class RedactedWcifExportHandler extends AuthenticatedCompetitionHandler {

    private final AuthenticatedWcaApi wcaApi;

    public RedactedWcifExportHandler(final AuthenticatedWcaApi wcaApi) {
        super(wcaApi);
        this.wcaApi = wcaApi;
    }

    @Override
    public void handle(final Competition competition, final Session session, final Context ctx) throws Exception {
        final Competition redacted = new Competition(
                competition.getFormatVersion(),
                competition.getId(),
                competition.getName(),
                competition.getShortName(),
                competition.getSeries(),
                redactPersons(competition.getPersons()),
                competition.getEvents(),
                competition.getSchedule(),
                competition.getRegistrationInfo(),
                competition.getCompetitorLimit(),
                competition.getExtensions()
        );

        final String output = wcaApi.serializePretty(redacted);
        ctx.contentType(ContentType.APPLICATION_JSON);
        ctx.header(Header.CONTENT_DISPOSITION, "attachment; filename=\"" + competition.getId() + "-redacted.json\"");
        ctx.result(output);
    }

    private static List<Person> redactPersons(final List<Person> persons) {
        final List<Person> redacted = new ArrayList<>();
        for (final Person person : persons) {
            redacted.add(new Person(
                    person.registrantId(),
                    person.name(),
                    person.wcaUserId(),
                    person.wcaId(),
                    person.country(),
                    person.gender(),
                    LocalDate.of(1970, 1, 1),
                    "example@example.com",
                    person.avatar(),
                    person.roles(),
                    redactRegistration(person.registration()),
                    person.assignments(),
                    person.personalBests(),
                    person.extensions()
            ));
        }
        return redacted;
    }

    private static Registration redactRegistration(final Registration registration) {
        if (registration == null) {
            return null;
        }
        return new Registration(
                registration.wcaRegistrationId(),
                registration.events(),
                registration.registrationStatus(),
                registration.guests(),
                "",
                "",
                registration.isCompeting()
        );
    }
}
