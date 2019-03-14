/*
 * Copyright 2012-2018 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.watchdog.pullrequest.config;

import io.watchdog.pullrequest.bot.SlackBot;
import org.springframework.boot.actuate.autoconfigure.health.CompositeHealthIndicatorConfiguration;
import org.springframework.boot.actuate.autoconfigure.health.ConditionalOnEnabledHealthIndicator;
import org.springframework.boot.actuate.autoconfigure.health.HealthIndicatorAutoConfiguration;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.boot.actuate.mongo.MongoHealthIndicator;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * {@link EnableAutoConfiguration Auto-configuration} for {@link MongoHealthIndicator}.
 *
 * @author Stephane Nicoll
 * @since 2.1.0
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
