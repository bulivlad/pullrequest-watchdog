package io.watchdog.pullrequest.config;

import io.watchdog.pullrequest.bot.SlackBot;
import org.springframework.boot.actuate.autoconfigure.health.CompositeHealthIndicatorConfiguration;
import org.springframework.boot.actuate.autoconfigure.health.ConditionalOnEnabledHealthIndicator;
import org.springframework.boot.actuate.autoconfigure.health.HealthIndicatorAutoConfiguration;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * {@link EnableAutoConfiguration Auto-configuration} for {@link SlackWsHealthIndicator}.
 *
 * @author vladclaudiubulimac on 2019-03-05.
 */
@Configuration
@ConditionalOnClass(SlackBot.class)
@ConditionalOnBean(SlackBot.class)
@ConditionalOnEnabledHealthIndicator("slackWs")
@AutoConfigureBefore(HealthIndicatorAutoConfiguration.class)
@AutoConfigureAfter(SlackBot.class)
public class SlackWsHealthIndicatorAutoConfiguration extends
		CompositeHealthIndicatorConfiguration<SlackWsHealthIndicator, SlackBot> {

	private final SlackBot slackBot;

	SlackWsHealthIndicatorAutoConfiguration(SlackBot slackBot) {
		this.slackBot = slackBot;
	}

	@Bean
	@ConditionalOnMissingBean(name = "slackWsHealthIndicator")
	public HealthIndicator slackWsHealthIndicator() {
		return createHealthIndicator(this.slackBot);
	}

}
