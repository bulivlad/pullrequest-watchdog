package io.watchdog.pullrequest.config;

import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * @author vladclaudiubulimac on 2019-03-01.
 */

@Data
@Configuration
@ConfigurationProperties(prefix = "auth")
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AuthConfig {

    Slack slack;
    Bitbucket bitbucket;

    @Data
    @FieldDefaults(level = AccessLevel.PRIVATE)
    public static class Slack {
        String token;
        String rtmUrl;
        String endpoint;
    }

    @Data
    @FieldDefaults(level = AccessLevel.PRIVATE)
    public static class Bitbucket {
        String authUsername;
        char[] password;
    }

}
