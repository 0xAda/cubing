package rip.ada.groups.wca;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import rip.ada.groups.Config;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class OauthApi {

    public static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    public static final ObjectReader RESPONSE_READER = OBJECT_MAPPER.readerFor(OauthTokenResponse.class);
    private final Config config;

    public OauthApi(final Config config) {
        this.config = config;
    }

    public OauthTokenResponse authenticateWithCode(final String code) {
        final Map<String, String> formData = new HashMap<>();
        formData.put("grant_type", "authorization_code");
        formData.put("code", code);
        return authenticateInternal(formData);
    }

    public OauthTokenResponse authenticateWithRefreshToken(final String refreshToken) {
        final Map<String, String> formData = new HashMap<>();
        formData.put("grant_type", "refresh_token");
        formData.put("refresh_token", refreshToken);
        return authenticateInternal(formData);
    }

    private OauthTokenResponse authenticateInternal(final Map<String, String> formData) {
        //TODO: Error handling
        formData.put("client_id", config.wcaClientId());
        formData.put("client_secret", config.wcaClientSecret());
        formData.put("redirect_uri", config.getOauthRedirectUri());

        final String encodedForm = formData.entrySet().stream()
                .map(entry -> URLEncoder.encode(entry.getKey(), StandardCharsets.UTF_8) + "=" +
                        URLEncoder.encode(entry.getValue(), StandardCharsets.UTF_8))
                .collect(Collectors.joining("&"));

        final HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(config.wcaUrl() + "/oauth/token"))
                .header("Content-Type", "application/x-www-form-urlencoded")
                .POST(HttpRequest.BodyPublishers.ofString(encodedForm))
                .build();

        try (HttpClient httpClient = HttpClient.newHttpClient()) {
            final String body = httpClient.send(request, HttpResponse.BodyHandlers.ofString()).body();
            return RESPONSE_READER.readValue(body, OauthTokenResponse.class);
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

}
