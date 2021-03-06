package io.watchdog.pullrequest.controller.slack;

import io.watchdog.pullrequest.bot.Controller;
import io.watchdog.pullrequest.bot.SlackBot;
import io.watchdog.pullrequest.model.slack.SlackEventMapping;
import io.watchdog.pullrequest.model.slack.SlackTeam;
import io.watchdog.pullrequest.service.slack.SlackTeamService;
import io.watchdog.pullrequest.util.BotUtil;
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
 * @author vladclaudiubulimac on 05/03/2018.
 */

@Slf4j
@Component
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class DirectMentionController {

    SlackBot slackBot;
    SlackTeamService slackTeamService;

    @Autowired
    public DirectMentionController(SlackBot slackBot, SlackTeamService slackTeamService) {
        this.slackBot = slackBot;
        this.slackTeamService = slackTeamService;
    }

    @Controller(events = EventType.DIRECT_MENTION, pattern = SlackEventMapping.ADD_TEAM_EVENT_REGEX)
    public void onReceiveAddTeamMention(WebSocketSession session, Event event) {
        SlackTeam slackTeam = slackTeamService.buildSlackTeam(event);
        boolean saved = slackTeamService.saveTeam(slackTeam);
        if (!saved) {
            slackBot.reply(session, event, new Message(":x: ERROR <@" + event.getUserId()
                    + "> , team is already existing or it was already scheduled!"));
            return;
        }
        slackBot.reply(session, event, new Message(":white_check_mark: OK <@" + event.getUserId() + "> , Scheduled " + saved + "!"));
    }

    @Controller(events = EventType.DIRECT_MENTION, pattern = SlackEventMapping.REMOVE_TEAM_EVENT_REGEX)
    public void onReceiveRemoveTeamMention(WebSocketSession session, Event event) {
        String channel = event.getChannelId();
        String teamName = BotUtil.getGroupMatcherFromEventMessage(event.getText(),
                SlackEventMapping.REMOVE_TEAM_EVENT_REGEX.getValue(),
                "teamName").orElseThrow(IllegalArgumentException::new);
        String repoSlug = BotUtil.getGroupMatcherFromEventMessage(event.getText(),
                SlackEventMapping.REMOVE_TEAM_EVENT_REGEX.getValue(),
                "repository").orElseThrow(IllegalArgumentException::new);

        boolean removed = slackTeamService.removeTeam(channel, teamName, repoSlug);
        if(!removed) {
            slackBot.reply(session, event, new Message(":x: ERROR <@" + event.getUserId()
                    + "> , team could not be removed!"));
            return;
        }
        slackBot.reply(session, event, new Message(":white_check_mark: OK <@" + event.getUserId() + "> , team *" + teamName + "* for repository *" + repoSlug +"* removed!"));
    }

    @Controller(events = EventType.DIRECT_MENTION, pattern = SlackEventMapping.UNSCHEDULE_TEAM_EVENT_REGEX)
    public void onReceiveUnscheduleTeamMention(WebSocketSession session, Event event) {
        String channel = event.getChannelId();
        String teamName = BotUtil.getGroupMatcherFromEventMessage(event.getText(),
                SlackEventMapping.UNSCHEDULE_TEAM_EVENT_REGEX.getValue(),
                "teamName").orElseThrow(IllegalArgumentException::new);
        String repoSlug = BotUtil.getGroupMatcherFromEventMessage(event.getText(),
                SlackEventMapping.UNSCHEDULE_TEAM_EVENT_REGEX.getValue(),
                "repository").orElseThrow(IllegalArgumentException::new);

        boolean unscheduled = slackTeamService.unscheduleTeam(channel, teamName, repoSlug);
        if (!unscheduled) {
            slackBot.reply(session, event, new Message(":x: ERROR <@" + event.getUserId()
                    + "> , team could not be unscheduled!"));
            return;
        }
        slackBot.reply(session, event, new Message(":white_check_mark: OK <@" + event.getUserId() + "> , Unscheduled " + unscheduled + "!"));
    }

    @Controller(events = EventType.DIRECT_MENTION, pattern = SlackEventMapping.HELP_REGEX)
    public void onReceiveHelpMention(WebSocketSession session, Event event) {
        slackBot.reply(session, event, new Message(":muscle: I'm here to help! Try something like: "));
        slackBot.reply(session, event, new Message("add team {team name} for repository {repository} with members [{@member1}, {@member2}, .. {@member n}] and scheduler {cron expression}"));
        slackBot.reply(session, event, new Message("remove team {team name} for repository {repository}"));
        slackBot.reply(session, event, new Message("unschedule team {team name} for repository {repository}"));
    }

    @Controller(events = EventType.DIRECT_MENTION, pattern = SlackEventMapping.DEFAULT)
    public void onReceiveDefaultDirectMention(WebSocketSession session, Event event) {
        if(slackBot.getBotUser().getId().equals(event.getUserId())){
            log.debug("Message from self '{}'", event.getText());
            return;
        }

        slackBot.reply(session, event, new Message(":confused: Sorry folk, I don't know what you are talking about. Try _@username_ help"));
    }
}
