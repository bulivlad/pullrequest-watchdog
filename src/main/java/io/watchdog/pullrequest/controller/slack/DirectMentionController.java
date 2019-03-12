package io.watchdog.pullrequest.controller.slack;

import io.watchdog.pullrequest.bot.SlackBot;
import io.watchdog.pullrequest.model.slack.SlackTeam;
import io.watchdog.pullrequest.service.slack.SlackTeamService;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import me.ramswaroop.jbot.core.slack.Controller;
import me.ramswaroop.jbot.core.slack.EventType;
import me.ramswaroop.jbot.core.slack.models.Event;
import me.ramswaroop.jbot.core.slack.models.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

/**
 * @author vladclaudiubulimac on 05/03/2018.
 */

@Slf4j
@Component
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class DirectMentionController {

    private static final String ADD_TEAM_EVENT_REGEX = "(add team).*(members\\s\\[).*(\\]).*(and\\sscheduler\\s).*";

    SlackBot slackBot;
    SlackTeamService slackTeamService;

    @Autowired
    public DirectMentionController(SlackBot slackBot, SlackTeamService slackTeamService) {
        this.slackBot = slackBot;
        this.slackTeamService = slackTeamService;
    }

    @Controller(events = EventType.DIRECT_MENTION, pattern = ADD_TEAM_EVENT_REGEX)
    public void onReceiveAddTeamMention(WebSocketSession session, Event event) {
        SlackTeam slackTeam = slackTeamService.buildSlackTeam(event);
        boolean saved = slackTeamService.saveTeam(slackTeam);
        if (!saved) {
            slackBot.reply(session, event, new Message(":negative_squared_cross_mark: ERROR <@" + event.getUserId() + "> , team is already existing or it was already scheduled!"));
            return;
        }
        slackBot.reply(session, event, new Message(":white_check_mark: OK <@" + event.getUserId() + "> , Scheduled " + saved + "!"));
    }

    @Controller(events = EventType.DIRECT_MENTION)
    public void onReceiveDefaultDirectMention(WebSocketSession session, Event event) {
        slackBot.reply(session, event, new Message(":confused: Sorry folk, I don't know what you are talking about"));
    }

}