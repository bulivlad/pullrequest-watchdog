package io.watchdog.pullrequest.util;

import io.watchdog.pullrequest.bot.SlackBot;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.powermock.api.mockito.PowerMockito.doNothing;

/**
 * @author vladbulimac on 15/03/2019.
 */

@RunWith(PowerMockRunner.class)
@PrepareForTest(SlackBot.class)
@FieldDefaults(level = AccessLevel.PRIVATE)
public class BotWebSocketHandlerTest {

    @Mock
    SlackBot slackBot;
    @Mock
    WebSocketSession webSocketSession;

    @InjectMocks
    BotWebSocketHandler botWebSocketHandler;

    @Before
    public void setUp() {
    }

    @Test
    public void afterConnectionEstablished() throws Exception {
        botWebSocketHandler.afterConnectionEstablished(webSocketSession);

        verify(slackBot).afterConnectionEstablished(eq(webSocketSession));
    }

    @Test
    public void handleTextMessage() throws Exception {
        TextMessage textMessage = new TextMessage("{\"type\":\"ACK\"}");

        doNothing().when(slackBot).handleTextMessage(any(), any());

        botWebSocketHandler.handleTextMessage(webSocketSession, textMessage);

        verify(slackBot).handleTextMessage(eq(webSocketSession), eq(textMessage));
    }

    @Test
    public void afterConnectionClosed() throws Exception {
        CloseStatus closeStatus = new CloseStatus(1000);

        botWebSocketHandler.afterConnectionClosed(webSocketSession, closeStatus);

        verify(slackBot).afterConnectionClosed(eq(webSocketSession), eq(closeStatus));
    }

    @Test
    public void handleTransportError() throws Exception {
        Throwable throwable = new Exception();

        botWebSocketHandler.handleTransportError(webSocketSession, throwable);

        verify(slackBot).handleTransportError(eq(webSocketSession), eq(throwable));
    }
}