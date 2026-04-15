package rip.ada.groups.routes.auth.wca;

import io.javalin.http.Context;
import io.javalin.http.Handler;
import rip.ada.groups.Config;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

public class StartWcaOauthHandler implements Handler {

    private final Config config;

    public StartWcaOauthHandler(final Config config) {
        this.config = config;
    }

    @Override
    public void handle(final Context ctx) {
        final String oauthRedirectUrl = String.format("%s/oauth/authorize?client_id=%s&redirect_uri=%s&response_type=code&scope=public+manage_competitions",
                config.wcaUrl(),
                URLEncoder.encode(config.wcaClientId(), StandardCharsets.UTF_8),
                URLEncoder.encode(config.getOauthRedirectUri(), StandardCharsets.UTF_8));
        ctx.redirect(oauthRedirectUrl);
    }
}
