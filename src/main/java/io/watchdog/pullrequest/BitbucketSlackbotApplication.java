package io.watchdog.pullrequest;

import io.watchdog.pullrequest.config.AuthConfig;
import io.watchdog.pullrequest.config.RepositoryConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.data.mongodb.core.mapping.event.ValidatingMongoEventListener;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;

@Slf4j
@SpringBootApplication(scanBasePackages = {"me.ramswaroop.jbot", "io.watchdog.pullrequest"})
public class BitbucketSlackbotApplication {

	private final AuthConfig authConfig;
	private final RepositoryConfig repositoryConfig;

	@Autowired
	public BitbucketSlackbotApplication(AuthConfig authConfig,
										RepositoryConfig repositoryConfig) {
		this.authConfig = authConfig;
		this.repositoryConfig = repositoryConfig;
	}

	public static void main(String[] args) {
		SpringApplication.run(BitbucketSlackbotApplication.class, args);
	}

	@Bean
	public CommandLineRunner commandLineRunner() {
		return strings -> log.info("App started");
	}

	@Bean
	public RestTemplate restTemplate() {
		return new RestTemplateBuilder()
				.basicAuthentication(authConfig.getBitbucket().getAuthUsername(), String.valueOf(authConfig.getBitbucket().getPassword()))
				.setConnectTimeout(Duration.ofMillis(repositoryConfig.getTimeout()))
				.setReadTimeout(Duration.ofMillis(repositoryConfig.getTimeout()))
				.build();
	}

	@Bean
	public ValidatingMongoEventListener validatingMongoEventListener() {
		return new ValidatingMongoEventListener(validator());
	}

	@Bean
	public LocalValidatorFactoryBean validator() {
		return new LocalValidatorFactoryBean();
	}

}
