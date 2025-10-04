package rip.ada.wca;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import rip.ada.wca.model.CompetitionInfo;
import rip.ada.wca.model.PersonWithCompetitionList;
import rip.ada.wcif.Competition;
import rip.ada.wcif.CountryCode;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class UnauthenticatedWcaApi {
    protected final HttpClient httpClient = HttpClient.newHttpClient();
    protected ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
    protected final WcaApiConfig config;

    public UnauthenticatedWcaApi(final WcaApiConfig config) {
        this.config = config;
    }

    public PersonWithCompetitionList getPersonWithUpcomingComps(final int personId) {
        try {
            final HttpRequest request = get("users/" + personId + "?upcoming_competitions=true");
            final String body = httpClient.send(request, HttpResponse.BodyHandlers.ofString()).body();
            return objectMapper.readValue(body, PersonWithCompetitionList.class);
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public Competition getCompetitionPublic(final String id) {
        try {
            final HttpRequest request = get("competitions/" + id + "/wcif/public");
            final String body = httpClient.send(request, HttpResponse.BodyHandlers.ofString()).body();
            return objectMapper.readValue(body, Competition.class);
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public List<CompetitionInfo> getUpcomingCompetitionsInCountry(final CountryCode countryCode) {
        try {
            final LocalDateTime oneWeekAgo = LocalDateTime.now().minusWeeks(1);
            final String isoDate = oneWeekAgo.format(DateTimeFormatter.ISO_DATE);

            final HttpRequest request = get("competitions?sort=start_date&start=" + isoDate + "&country_iso2=" + countryCode.getCode());
            final String body = httpClient.send(request, HttpResponse.BodyHandlers.ofString()).body();
            return objectMapper.readValue(body, new com.fasterxml.jackson.core.type.TypeReference<>() {
            });
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public <T> String serialize(final T body) throws JsonProcessingException {
        return objectMapper.writeValueAsString(body);
    }

    public <T> ObjectNode serializeNode(final T body) throws JsonProcessingException {
        return objectMapper.valueToTree(body);
    }

    protected HttpRequest get(final String url) {
        return HttpRequest.newBuilder()
                .GET()
                .uri(URI.create(config.wcaUrl() + "/api/v0/" + url))
                .build();
    }
}
