package io.watchdog.pullrequest.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * @author vladclaudiubulimac on 2019-03-01.
 */

@Data
@Configuration
@ConfigurationProperties(prefix = "auth")
public class AuthConfig {

    private Slack slack;
    private Bitbucket bitbucket;

    @Data
    public static class Slack {
        private String token;
        private String rtmUrl;
    }

    @Data
    public static class Bitbucket {
        private String authUsername;
        private char[] password;
    }

}
