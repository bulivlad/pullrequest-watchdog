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
@FieldDefaults(level = AccessLevel.PRIVATE)
@ConfigurationProperties(prefix = "repository")
public class RepositoryConfig {

    String username;
    String slug;
    String endpoint;
    Integer timeout;
    String pullRequestsUrl;
    String usersUrl;

}
