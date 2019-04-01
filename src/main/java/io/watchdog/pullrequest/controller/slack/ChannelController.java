package io.watchdog.pullrequest.controller.slack;

import io.watchdog.pullrequest.bot.Controller;
import io.watchdog.pullrequest.bot.SlackBot;
import io.watchdog.pullrequest.model.slack.SlackChannel;
import io.watchdog.pullrequest.service.slack.SlackChannelService;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import me.ramswaroop.jbot.core.slack.EventType;
import me.ramswaroop.jbot.core.slack.models.Event;
import me.ramswaroop.jbot.core.slack.models.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

/**
 * @author vladclaudiubulimac on 2019-03-25.
 */

@Slf4j
@Component
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ChannelController {

    SlackBot slackBot;
    SlackChannelService slackChannelService;

    @Autowired
    public ChannelController(SlackBot slackBot, SlackChannelService slackChannelService) {
        this.slackBot = slackBot;
        this.slackChannelService = slackChannelService;
    }

    @Controller(events = EventType.GROUP_JOINED)
    public void onReceiveChannelJoined(WebSocketSession session, Event event) {
        SlackChannel savedChannel = slackChannelService.saveJoinedChannel(event);
        if (savedChannel.getId() != null) {
            log.info("Channel {} joined and saved to db", savedChannel.getName());
        }
        slackBot.reply(session, event, new Message(":waving: Yo! Hi there!"));
    }

    @Controller(events = EventType.GROUP_LEFT)
    public void onReceiveChannelLeft(WebSocketSession session, Event event) {
        slackBot.reply(session, event, new Message(":waving: Yo! Hi there!"));
    }

}
