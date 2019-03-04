package io.watchdog.pullrequest.bot;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import io.watchdog.pullrequest.config.AuthConfig;

/**
 * @author vladclaudiubulimac on 05/03/2018.
 */


@Component
public class SlackBot extends Bot {

    private final AuthConfig authConfig;
    private final String scanPackage;

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
}
