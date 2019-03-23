package io.watchdog.pullrequest.bot;

import io.watchdog.pullrequest.model.slack.SlackEventMapping;
import me.ramswaroop.jbot.core.slack.EventType;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation for different Event types in Slack RTM API.
 *
 * @author vladclaudiubulimac on 2019-03-15.
 */

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Controller {

    EventType[] events() default EventType.MESSAGE;

    SlackEventMapping pattern() default SlackEventMapping.DEFAULT;

    String next() default "";

}
