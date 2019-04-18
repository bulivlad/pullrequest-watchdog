package io.watchdog.pullrequest.bot;

import io.watchdog.pullrequest.config.AuthConfig;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import me.ramswaroop.jbot.core.slack.models.User;
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

    /**
     * Retrieve the slack token from config
     *
     * @return the slack token
     */
    @Override
    public String getSlackToken() {
        return authConfig.getSlack().getToken();
    }

    @Override
    public Bot getSlackBot() {
        return this;
    }

    /**
     * Retrieve the packages where the {@code @Controller} annotation leaves
     *
     * @return the packages to be scanned for @Controller annotation
     */
    protected String getScanPackage(){
        return scanPackage;
    }


    /**
     * Retrieve the slack web socket keep alive connection status
     *
     * @return true if the keep alive to slack web socket was successful
     */
    public boolean isKeepAliveSuccessful(){
        return super.isKeepAliveSuccessful();
    }


    /**
     * Retrieve the bot slack user
     *
     * @return the bot slack user details
     */
    public User getBotUser() {
        return slackService.getCurrentUser();
    }
}
