package io.watchdog.pullrequest.config;

import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Config class to handle authentication details to both Slack and Bitbucket
 *
 * @author vladclaudiubulimac on 2019-03-01.
 */

@Data
@Configuration
@ConfigurationProperties(prefix = "auth")
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AuthConfig {

    Slack slack;
    Bitbucket bitbucket;

    /**
     * Slack related authentication details
     */
    @Data
    @FieldDefaults(level = AccessLevel.PRIVATE)
    public static class Slack {
        String token;
        String rtmUrl;
        String endpoint;
    }

    /**
     * Versioning provider related authentication details
     */
    @Data
    @FieldDefaults(level = AccessLevel.PRIVATE)
    public static class Bitbucket {
        String authUsername;
        char[] password;
    }

}
