package rip.ada.groups;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public record Config(String wcaUrl, String wcaClientId, String wcaClientSecret, String externalUrl,
                     String gsuiteClientId, String gsuiteRedirectUrl, String gsuiteClientSecret,
                     String dbHost, int dbPort, String dbUsername, String dbPassword,
                     String dbSchema, int dbPoolSize) {

    public static Config fromProperties() {
        final Properties properties = new Properties();
        try (InputStream inputStream = Config.class.getClassLoader().getResourceAsStream("groups.properties")) {
            properties.load(inputStream);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return new Config(
                properties.getProperty("wca.url"),
                properties.getProperty("wca.client_id"),
                properties.getProperty("wca.client_secret"),
                properties.getProperty("external_url"),
                properties.getProperty("gsuite.client_id"),
                properties.getProperty("gsuite.redirect_url"),
                properties.getProperty("gsuite.client_secret"),
                properties.getProperty("db.host"),
                Integer.parseInt(properties.getProperty("db.port")),
                properties.getProperty("db.username"),
                properties.getProperty("db.password"),
                properties.getProperty("db.schema"),
                Integer.parseInt(properties.getProperty("db.poolSize")));
    }

    public String getOauthRedirectUri() {
        return externalUrl() + "/oauth/wca/callback";
    }
}
