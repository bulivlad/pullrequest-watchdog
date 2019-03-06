package io.watchdog.pullrequest.controller.slack;

import io.watchdog.pullrequest.bot.SlackBot;
import io.watchdog.pullrequest.model.SlackTeam;
import io.watchdog.pullrequest.quartz.SchedulerService;
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
public class ReceiveController {

    SlackBot slackBot;
    SlackTeamService slackTeamService;
    SchedulerService schedulerService;

    @Autowired
    public ReceiveController(SlackBot slackBot, SlackTeamService slackTeamService, SchedulerService schedulerService) {
        this.slackBot = slackBot;
        this.slackTeamService = slackTeamService;
        this.schedulerService = schedulerService;
    }

    @Controller(events = EventType.DIRECT_MENTION)
    public void onReceiveMention(WebSocketSession session, Event event) {
        SlackTeam slackTeam = slackTeamService.buildSlackTeam(event);
        boolean saved = slackTeamService.saveTeam(slackTeam);
        if(!saved) {
            slackBot.reply(session,event,new Message(":negative_squared_cross_mark: ERROR <@" + event.getUserId() + "> , team is already exisiting!"));
            return ;
        }
        boolean scheduled = schedulerService.scheduleEventForTeam(slackTeam);
        slackBot.reply(session,event,new Message(":white_check_mark: OK <@" + event.getUserId() + "> , Scheduled " + scheduled + "!"));
    }

}
