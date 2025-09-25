package rip.ada.wca;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import rip.ada.wca.model.CompetitionInfo;
import rip.ada.wca.model.Me;
import rip.ada.wca.model.Person;
import rip.ada.wca.patcher.WcifPatchRequest;
import rip.ada.wcif.Competition;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;

public class AuthenticatedWcaApi extends UnauthenticatedWcaApi {

    private final ArrayBlockingQueue<WcifPatchRequest> requests;

    public AuthenticatedWcaApi(final WcaApiConfig config, final ArrayBlockingQueue<WcifPatchRequest> requests) {
        super(config);
        this.requests = requests;
    }

    public Person getMe(final OauthSession session) {
        try {
            final HttpRequest request = authenticatedGet(session, "me");
            final String body = httpClient.send(request, HttpResponse.BodyHandlers.ofString()).body();
            final Me me = objectMapper.readValue(body, Me.class);
            return me.me();
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public List<CompetitionInfo> getCompetitionList(final OauthSession session) {
        try {
            final LocalDateTime oneWeekAgo = LocalDateTime.now().minusWeeks(1);
            final String isoDate = oneWeekAgo.format(DateTimeFormatter.ISO_DATE);

            final HttpRequest request = authenticatedGet(session, "competitions?managed_by_me=true&sort=start_date&start=" + isoDate);
            final String body = httpClient.send(request, HttpResponse.BodyHandlers.ofString()).body();
            return objectMapper.readValue(body, new com.fasterxml.jackson.core.type.TypeReference<>() {
            });
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public Competition getCompetition(final OauthSession session, final String id) {
        try {
            final HttpRequest request = authenticatedGet(session, "competitions/" + id + "/wcif");
            final String body = httpClient.send(request, HttpResponse.BodyHandlers.ofString()).body();
            return objectMapper.registerModule(new JavaTimeModule()).readValue(body, Competition.class);
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public void updateCompetition(final OauthSession session, final Competition competition) {
        requests.add(new WcifPatchRequest(competition, session));
    }

    public void updateWcifDirectly(final OauthSession session, final String competitionId, final ObjectNode body) {
        try {
            final String url = "competitions/" + competitionId + "/wcif";
            final HttpRequest request = authenticatedPost(session, url, body);
            final HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != 200) {
                throw new RuntimeException(String.format("Got error response from WCA, status %d (headers %s). Error: %s", response.statusCode(), response.headers(), response.body()));
            }
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
            final String body1 = serialize(body);
            Files.writeString(Path.of("/tmp/comp.json"), body1);
            return HttpRequest.newBuilder()
                    .method("PATCH", HttpRequest.BodyPublishers.ofString(body1))
                    .header("Authorization", "Bearer " + session.getBearerToken())
                    .header("Content-Type", "application/json")
                    .uri(URI.create(config.wcaUrl() + "/api/v0/" + url))
                    .build();
        } catch (final IOException e) {
            throw new RuntimeException(e);
        }
    }

}
