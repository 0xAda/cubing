package rip.ada.wca;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.concurrent.TimeUnit;

public record OauthTokenResponse(
        @JsonProperty("access_token") String accessToken,
        @JsonProperty("token_type") String tokenType,
        @JsonProperty("expires_in") int expiresIn,
        @JsonProperty("refresh_token") String refreshToken,
        @JsonProperty("scope") String scope,
        @JsonProperty("created_at") int createdAt
) {

    public long getExpirationNanos() {
        return System.nanoTime() + TimeUnit.SECONDS.toNanos(expiresIn);
    }

}
