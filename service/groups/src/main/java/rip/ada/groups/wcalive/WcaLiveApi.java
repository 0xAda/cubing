package rip.ada.groups.wcalive;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;

public class WcaLiveApi {

    private static final String WCA_LIVE_API = "https://live.worldcubeassociation.org/api";
    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
    private final ObjectReader synchronizedListReader = objectMapper.readerFor(new TypeReference<GraphQLResponse<LiveCompetitions<LastSynchronizedAt>>>() {
    });
    private final GraphQLQuery query = new GraphQLQuery("query SynchronizedQuery {\n" +
            "  competitions {\n" +
            "    synchronizedAt\n" +
            "    wcaId\n" +
            "  }\n" +
            "}", "SynchronizedQuery");

    public List<LastSynchronizedAt> getSynchronizedCompetitions() {
        try {
            final HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(WCA_LIVE_API))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(query)))
                    .build();

            final HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            final GraphQLResponse<LiveCompetitions<LastSynchronizedAt>> graphQLResponse = synchronizedListReader.readValue(response.body());
            return graphQLResponse.data().competitions();
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    //curl 'https://live.worldcubeassociation.org/api' -X POST -H 'User-Agent: Mozilla/5.0 (X11; Linux x86_64; rv:138.0) Gecko/20100101 Firefox/138.0' -H 'Accept: application/json, multipart/mixed' -H 'Accept-Language: en-GB,en;q=0.5' -H 'Accept-Encoding: gzip, deflate, br, zstd' -H 'Referer: https://cloud.hasura.io/' -H 'content-type: application/json' -H 'Origin: https://cloud.hasura.io' -H 'Connection: keep-alive' -H 'Sec-Fetch-Dest: empty' -H 'Sec-Fetch-Mode: cors' -H 'Sec-Fetch-Site: cross-site' -H 'Priority: u=0' -H 'TE: trailers'
    // --data-raw '{"query":"query MyQuery {\n  competitions {\n    synchronizedAt\n    wcaId\n  }\n}\n","operationName":"MyQuery"}'
}
