package io.watchdog.pullrequest.controller.slack;

import io.watchdog.pullrequest.bot.SlackBot;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import me.ramswaroop.jbot.core.slack.Controller;
import me.ramswaroop.jbot.core.slack.EventType;
import me.ramswaroop.jbot.core.slack.models.Event;
import me.ramswaroop.jbot.core.slack.models.Message;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

/**
 * @author vladclaudiubulimac on 2019-03-09.
 */

@Slf4j
@Component
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class DirectMessageController {

    private static final String HEALTH_CHECK_MESSAGE = "healthcheck";

    SlackBot slackBot;

    public DirectMessageController(SlackBot slackBot) {
        this.slackBot = slackBot;
    }

    @Controller(events = EventType.DIRECT_MESSAGE)
    public void onReceiveDirectMessage(WebSocketSession session, Event event) {
        String message = event.getText();
        if (HEALTH_CHECK_MESSAGE.equalsIgnoreCase(message)) {
            slackBot.reply(session,event,new Message(":white_check_mark: I'm healthy. Thanks!"));
        }
    }
}
