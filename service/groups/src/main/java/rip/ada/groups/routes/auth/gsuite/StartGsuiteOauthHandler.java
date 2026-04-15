package rip.ada.groups.routes.auth.gsuite;

import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeRequestUrl;
import com.google.api.services.sheets.v4.SheetsScopes;
import io.javalin.http.Context;
import rip.ada.groups.Config;
import rip.ada.groups.session.AuthenticatedHandler;
import rip.ada.groups.session.Session;

import java.util.List;

public class StartGsuiteOauthHandler extends AuthenticatedHandler {

    private final Config config;

    public StartGsuiteOauthHandler(final Config config) {
        this.config = config;
    }

    @Override
    public void handle(final Session session, final Context ctx) {
        final String authUrl = new GoogleAuthorizationCodeRequestUrl(
                config.gsuiteClientId(), config.gsuiteRedirectUrl(), List.of(SheetsScopes.SPREADSHEETS_READONLY)
        ).setAccessType("offline").build();
        ctx.redirect(authUrl);
    }
}
