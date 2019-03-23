package io.watchdog.pullrequest.util;


import lombok.extern.slf4j.Slf4j;
import org.springframework.web.socket.BinaryMessage;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.AbstractWebSocketHandler;
import io.watchdog.pullrequest.bot.Bot;

/**
 * @author vladclaudiubulimac on 22/08/2018.
 */

@Slf4j
public class BotWebSocketHandler extends AbstractWebSocketHandler {

    private Bot bot;

    public BotWebSocketHandler(Bot bot) {
        this.bot = bot;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        bot.afterConnectionEstablished(session);
    }

    @Override
    public void handleTextMessage(WebSocketSession session, TextMessage message) {
        bot.handleTextMessage(session, message);
    }

    @Override
    protected void handleBinaryMessage(WebSocketSession session, BinaryMessage message) {
        log.error("Binary messages are not supported in Slack RTM API");
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        bot.afterConnectionClosed(session, status);
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) {
        bot.handleTransportError(session, exception);
    }
}
