package rip.ada.wca;

import java.util.concurrent.TimeUnit;

public class OauthSession {

    private String bearerToken;
    private String refreshToken;
    private long expirationNanoTime;

    private OauthSession(final String bearerToken, final String refreshToken, final long expirationNanoTime) {
        this.bearerToken = bearerToken;
        this.refreshToken = refreshToken;
        this.expirationNanoTime = expirationNanoTime;
    }

    public static OauthSession create(final OauthApi oauthApi, final String code) {
        final OauthTokenResponse oauthTokenResponse = oauthApi.authenticateWithCode(code);
        return new OauthSession(oauthTokenResponse.accessToken(), oauthTokenResponse.refreshToken(), oauthTokenResponse.getExpirationNanos());
    }

    public void refresh(final OauthApi oauthApi) {
        final OauthTokenResponse response = oauthApi.authenticateWithRefreshToken(refreshToken);
        this.bearerToken = response.accessToken();
        this.refreshToken = response.refreshToken();
        this.expirationNanoTime = response.getExpirationNanos();
    }

    public String getBearerToken() {
        return bearerToken;
    }

    public boolean needsRefresh() {
        return System.nanoTime() + TimeUnit.MINUTES.toNanos(10) > expirationNanoTime;
    }

}
