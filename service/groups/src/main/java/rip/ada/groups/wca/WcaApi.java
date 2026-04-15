package rip.ada.groups.wca;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rip.ada.groups.Config;
import rip.ada.groups.wca.model.CompetitionInfo;
import rip.ada.groups.wca.model.Me;
import rip.ada.groups.wca.model.Person;
import rip.ada.groups.wca.model.PersonWithCompetitionList;
import rip.ada.groups.wca.patcher.WcifPatchRequest;
import rip.ada.wcif.Competition;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;

public class WcaApi {

    public static final HttpClient HTTP_CLIENT = HttpClient.newHttpClient();
    private static final Logger LOGGER = LoggerFactory.getLogger(WcaApi.class);
    private final Config config;
    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
    private final ArrayBlockingQueue<WcifPatchRequest> requests;

    public WcaApi(final Config config, final ArrayBlockingQueue<WcifPatchRequest> requests) {
        this.config = config;
        this.requests = requests;
    }

    public Person getMe(final OauthSession session) {
        try {
            final HttpRequest request = authenticatedGet(session, "me");
            final String body = HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString()).body();
            final Me me = new ObjectMapper().readValue(body, Me.class);
            return me.me();
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public PersonWithCompetitionList getPersonWithUpcomingComps(final int personId) {
        try {
            final HttpRequest request = get("users/" + personId + "?upcoming_competitions=true");
            final String body = HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString()).body();
            final PersonWithCompetitionList person = new ObjectMapper().readValue(body, PersonWithCompetitionList.class);
            return person;
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public List<CompetitionInfo> getCompetitionList(final OauthSession session) {
        try {
            final LocalDateTime oneWeekAgo = LocalDateTime.now().minusWeeks(1);
            final String isoDate = oneWeekAgo.format(DateTimeFormatter.ISO_DATE);

            final HttpRequest request = authenticatedGet(session, "competitions?managed_by_me=true&sort=start_date&start=" + isoDate);
            final String body = HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString()).body();
            return new ObjectMapper().readValue(body, new com.fasterxml.jackson.core.type.TypeReference<>() {
            });
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public Competition getCompetition(final OauthSession session, final String id) {
        try {
            final HttpRequest request = authenticatedGet(session, "competitions/" + id + "/wcif");
            final String body = HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString()).body();
            return new ObjectMapper().registerModule(new JavaTimeModule()).readValue(body, Competition.class);
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public void updateCompetition(final OauthSession session, final Competition competition) {
        requests.add(new WcifPatchRequest(competition, session));
    }

    public void internalSendWcifPatch(final OauthSession session, final String competitionId, final ObjectNode body) {
        try {
            final String url = "competitions/" + competitionId + "/wcif";
            final HttpRequest request = authenticatedPost(session, url, body);
            final HttpResponse<String> response = HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != 200) {
                LOGGER.error("Got error response from WCA, status {} (headers {}). Error: {}", response.statusCode(), response.headers(), response.body());
            } else {
                LOGGER.error("Successfully sent patch for competition {}", competitionId);
            }
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public Competition getCompetitionPublic(final String id) {
        try {
            final HttpRequest request = get("competitions/" + id + "/wcif");
            final String body = HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString()).body();
            return objectMapper.readValue(body, Competition.class);
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private HttpRequest authenticatedGet(final OauthSession session, final String url) {
        return HttpRequest.newBuilder()
                .GET()
                .header("Authorization", "Bearer " + session.getBearerToken())
                .uri(URI.create(config.wcaUrl() + "/api/v0/" + url))
                .build();
    }

    private <T> HttpRequest authenticatedPost(final OauthSession session, final String url, final T body) {
        try {
            final String serializedBody = serialize(body);
            return HttpRequest.newBuilder()
                    .method("PATCH", HttpRequest.BodyPublishers.ofString(serializedBody))
                    .header("Authorization", "Bearer " + session.getBearerToken())
                    .header("Content-Type", "application/json")
                    .uri(URI.create(config.wcaUrl() + "/api/v0/" + url))
                    .build();
        } catch (final IOException e) {
            throw new RuntimeException(e);
        }
    }

    public <T> String serialize(final T body) throws JsonProcessingException {
        return objectMapper.writeValueAsString(body);
    }

    public <T> ObjectNode serializeNode(final T body) {
        return objectMapper.valueToTree(body);
    }

    private HttpRequest get(final String url) {
        return HttpRequest.newBuilder()
                .GET()
                .uri(URI.create(config.wcaUrl() + "/api/v0/" + url))
                .build();
    }

}
