package rip.ada.wcascraper;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;

public class WcaScraper {

    private final HttpClient httpClient = HttpClient.newBuilder().build();

    public ScrapedCompetition scrapeCompetition(final String competitionId) {
        final HttpRequest req = HttpRequest
                .newBuilder()
                .GET()
                .uri(URI.create("https://www.worldcubeassociation.org/competitions/" + competitionId))
                .build();

        try {
            final HttpResponse<String> send = httpClient.send(req, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
            if (send.statusCode() != 200) {
                throw new RuntimeException("Got non 200 status code from WCA");
            }
            return ScrapedCompetition.fromString(send.body());
        } catch (final IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

}
