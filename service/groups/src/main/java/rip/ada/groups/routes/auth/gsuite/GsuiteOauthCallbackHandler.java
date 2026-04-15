package rip.ada.groups.routes.auth.gsuite;

import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeTokenRequest;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.auth.oauth2.GoogleTokenResponse;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import io.javalin.http.Context;
import rip.ada.groups.Config;
import rip.ada.groups.session.AuthenticatedHandler;
import rip.ada.groups.session.Session;

import java.io.IOException;

public class GsuiteOauthCallbackHandler extends AuthenticatedHandler {

    private final Config config;

    public GsuiteOauthCallbackHandler(final Config config) {
        this.config = config;
    }

    @Override
    public void handle(final Session session, final Context ctx) {
        final String code = ctx.queryParam("code");

        if (code == null) {
            ctx.status(400).result("Missing code");
            return;
        }

        final HttpTransport httpTransport = new NetHttpTransport();
        final JsonFactory jsonFactory = GsonFactory.getDefaultInstance();

        final GoogleTokenResponse tokenResponse;
        try {
            tokenResponse = new GoogleAuthorizationCodeTokenRequest(
                    httpTransport, jsonFactory, config.gsuiteClientId(), config.gsuiteClientSecret(), code, config.gsuiteRedirectUrl()
            ).execute();
        } catch (final IOException e) {
            ctx.status(500).result("Token exchange failed");
            return;
        }

        final GoogleCredential credential = new GoogleCredential.Builder()
                .setTransport(httpTransport)
                .setJsonFactory(jsonFactory)
                .setClientSecrets(config.gsuiteClientId(), config.gsuiteClientSecret())
                .build()
                .setFromTokenResponse(tokenResponse);

        session.setGoogleCredential(credential);
        ctx.redirect("/");
    }
}
