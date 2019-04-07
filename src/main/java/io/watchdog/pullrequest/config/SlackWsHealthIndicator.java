package io.watchdog.pullrequest.config;

import io.watchdog.pullrequest.bot.SlackBot;
import org.springframework.boot.actuate.health.AbstractHealthIndicator;
import org.springframework.boot.actuate.health.Health;
import org.springframework.web.socket.client.WebSocketConnectionManager;

/**
 * The health indicator for Slack Websocket connection.
 * The health check is based on {@link WebSocketConnectionManager#isRunning()} method which will check whether
 * the connection manager is started
 *
 * @author vladclaudiubulimac on 2019-03-14.
 */
public class SlackWsHealthIndicator extends AbstractHealthIndicator {

    private final WebSocketConnectionManager webSocketConnectionManager;

    public SlackWsHealthIndicator(SlackBot slackBot) {
        super((exception) -> "SlackWs health check failed because of " + exception);
        this.webSocketConnectionManager = slackBot.getWebSocketConnectionManager();
    }

    /**
     * Health check performing method. The {@link WebSocketConnectionManager#isRunning()} method which will
     * check whether the connection manager is started
     *
     * @param builder the {@link Health.Builder} containing the health of Slack Websocket connection
     */
    @Override
    protected void doHealthCheck(Health.Builder builder) {
        boolean result = webSocketConnectionManager.isRunning();
        builder.up()
                .withDetail("WebSocketRunning", result)
                .withDetail("autoStartup", webSocketConnectionManager.isAutoStartup())
                .build();
    }

}
