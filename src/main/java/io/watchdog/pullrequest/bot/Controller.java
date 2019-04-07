package io.watchdog.pullrequest.bot;

import io.watchdog.pullrequest.model.slack.SlackEventMapping;
import me.ramswaroop.jbot.core.slack.EventType;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation used to map different Event types in Slack RTM API to methods to handle such events.
 *
 * @author vladclaudiubulimac on 2019-03-15.
 */

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Controller {

    /**
     * The event to be handled by the annotated method
     *
     * @return an array with all types of the events that will be handled
     */
    EventType[] events() default EventType.MESSAGE;

    /**
     * The regex pattern to match against the message body that will be handled by the annotated method
     * This fields is used to add more granularity on methods mapping.
     * The message needs to match both {@link Controller#events()} and {@link Controller#pattern()} in order to be
     * handled by the annotated method
     *
     * @return the pattern used to match the handling method
     */
    SlackEventMapping pattern() default SlackEventMapping.DEFAULT;

    /**
     * Used to specify the next method in conversation. Could be used to build chained responses to users messages
     *
     * @return the name of the next method in conversation chain
     */
    String next() default "";

}
