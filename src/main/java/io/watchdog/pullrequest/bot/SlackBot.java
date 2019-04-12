package io.watchdog.pullrequest.bot;

import io.watchdog.pullrequest.config.AuthConfig;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Specific class for SlackBot to provide config attributes
 *
 * @author vladclaudiubulimac on 05/03/2018.
 */

@Slf4j
@Component
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class SlackBot extends Bot {

    AuthConfig authConfig;
    String scanPackage;

    @Autowired
    public SlackBot(AuthConfig authConfig, @Value("${scanPackageForControllerAnnotations}") String scanPackage) {
        this.authConfig = authConfig;
        this.scanPackage = scanPackage;
    }

    @Override
    public String getSlackToken() {
        return authConfig.getSlack().getToken();
    }

    @Override
    public Bot getSlackBot() {
        return this;
    }

    protected String getScanPackage(){
        return scanPackage;
    }

    public boolean isKeepAliveSuccessful(){
        return super.isKeepAliveSuccessful();
    }
}
