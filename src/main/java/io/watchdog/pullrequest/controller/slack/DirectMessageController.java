package io.watchdog.pullrequest.controller.slack;

import io.watchdog.pullrequest.bot.Controller;
import io.watchdog.pullrequest.bot.SlackBot;
import io.watchdog.pullrequest.model.slack.SlackEventMapping;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
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

    SlackBot slackBot;

    public DirectMessageController(SlackBot slackBot) {
        this.slackBot = slackBot;
    }

    @Controller(events = EventType.DIRECT_MESSAGE, pattern = SlackEventMapping.HEALTH_CHECK_MESSAGE_REGEX)
    public void onReceiveHealthCheckDirectMessage(WebSocketSession session, Event event) {
        slackBot.reply(session,event,new Message(":white_check_mark: I'm healthy. Thanks!"));
    }

    @Controller(events = EventType.DIRECT_MESSAGE, pattern = SlackEventMapping.DEFAULT)
    public void onReceiveDefaultDirectMention(WebSocketSession session, Event event) {
        slackBot.reply(session, event, new Message(":confused: Sorry folk, I don't know what you are talking about"));
    }
}
