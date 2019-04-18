package io.watchdog.pullrequest.controller.slack;

import io.watchdog.pullrequest.bot.Controller;
import io.watchdog.pullrequest.bot.SlackBot;
import io.watchdog.pullrequest.dto.slack.SlackMessageRequestDTO;
import io.watchdog.pullrequest.model.slack.SlackChannel;
import io.watchdog.pullrequest.service.slack.SlackApiRestService;
import io.watchdog.pullrequest.service.slack.SlackChannelService;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import me.ramswaroop.jbot.core.slack.EventType;
import me.ramswaroop.jbot.core.slack.models.Event;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

import java.util.Collections;

/**
 * @author vladclaudiubulimac on 2019-03-25.
 */

@Slf4j
@Component
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ChannelController {

    SlackBot slackBot;
    SlackChannelService slackChannelService;
    SlackApiRestService slackApiRestService;

    @Autowired
    public ChannelController(SlackBot slackBot, SlackChannelService slackChannelService, SlackApiRestService slackApiRestService) {
        this.slackBot = slackBot;
        this.slackChannelService = slackChannelService;
        this.slackApiRestService = slackApiRestService;
    }

    @Controller(events = { EventType.GROUP_JOINED, EventType.CHANNEL_JOINED })
    public void onReceiveChannelJoined(WebSocketSession session, Event event) {
        SlackChannel savedChannel = slackChannelService.saveJoinedChannel(event);
        if (savedChannel.getId() != null) {
            log.info("Channel {} joined and saved to db", savedChannel.getName());
        }
        SlackMessageRequestDTO slackMessageRequestDTO = SlackMessageRequestDTO.builder()
                .channel(event.getChannel().getId())
                .block(new SlackMessageRequestDTO.Block(new SlackMessageRequestDTO.Text(":waving: Yo! Hi there!")))
                .build();
        slackApiRestService.sendMessageToChannel(slackMessageRequestDTO, Collections.emptyMap());
    }

    @Controller(events = { EventType.GROUP_LEFT, EventType.CHANNEL_LEFT })
    public void onReceiveChannelLeft(WebSocketSession session, Event event) {
        String channelId = event.getChannelId();
        log.info("Channel {} with id {} left", slackChannelService.findChannelByIdOrName(channelId, channelId), channelId);
    }

}
