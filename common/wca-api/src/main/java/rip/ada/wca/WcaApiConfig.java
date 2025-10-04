package rip.ada.wca;

public record WcaApiConfig(String wcaUrl, String clientId, String clientSecret, String oauthRedirectUri) {
    public static WcaApiConfig unauthenticatedDefault() {
        return new WcaApiConfig("https://www.worldcubeassociation.org", null, null, null);
    }
}
