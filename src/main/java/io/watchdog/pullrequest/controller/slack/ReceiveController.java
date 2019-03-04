package io.watchdog.pullrequest.controller.slack;

import me.ramswaroop.jbot.core.slack.Controller;
import me.ramswaroop.jbot.core.slack.EventType;
import me.ramswaroop.jbot.core.slack.models.Event;
import me.ramswaroop.jbot.core.slack.models.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;
import io.watchdog.pullrequest.bot.SlackBot;

/**
 * @author vladclaudiubulimac on 05/03/2018.
 */

@Component
public class ReceiveController {

    private final SlackBot slackBot;

    @Autowired
    public ReceiveController(SlackBot slackBot) {
        this.slackBot = slackBot;
    }

    @Controller(events = EventType.DIRECT_MESSAGE)
    public void onReceiveDirectMessage(WebSocketSession session, Event event){
        slackBot.reply(session,event,new Message("Direct message received!"));
    }

    @Controller(events = EventType.DIRECT_MENTION)
    public void onReceiveMention(WebSocketSession session, Event event){
        slackBot.reply(session,event,new Message("Mention received!"));
    }

    @Controller(events = EventType.MESSAGE)
    public void onReceiveMessage(WebSocketSession session, Event event){
        slackBot.reply(session,event,new Message("Message received!"));
    }

}
