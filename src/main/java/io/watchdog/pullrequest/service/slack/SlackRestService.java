package io.watchdog.pullrequest.service.slack;

import io.watchdog.pullrequest.config.AuthConfig;
import io.watchdog.pullrequest.config.RepositoryConfig;
import io.watchdog.pullrequest.model.slack.SlackCommand;
import io.watchdog.pullrequest.model.slack.SlackMessageRequest;
import io.watchdog.pullrequest.model.slack.SlackMessageResponse;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

/**
 * @author vladclaudiubulimac on 2019-03-06.
 */

@Slf4j
@Component
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class SlackRestService {

    AuthConfig authConfig;
    RestTemplate restTemplate;
    RepositoryConfig repositoryConfig;

    @Autowired
    public SlackRestService(AuthConfig authConfig, RepositoryConfig repositoryConfig) {
        this.authConfig = authConfig;
        this.repositoryConfig = repositoryConfig;
        this.restTemplate = new RestTemplate();
    }

    public void sendMessageToChannel(SlackMessageRequest message){
        sendSlackCommand(message, SlackCommand.MESSAGE_CHANNEL);
    }

    private void sendSlackCommand(SlackMessageRequest message, SlackCommand slackCommand){

        ResponseEntity<SlackMessageResponse> slackResponse = restTemplate.exchange(
                authConfig.getSlack().getEndpoint() + slackCommand.getValue(),
                HttpMethod.POST,
                buildHttpEntityWithAuthorisation(message),
                SlackMessageResponse.class,
                repositoryConfig.getUsername(),
                repositoryConfig.getSlug());
        handleResponse(slackResponse);
    }

    private void handleResponse(ResponseEntity<SlackMessageResponse> response) {
        log.info("Slack response code is {}", response.getStatusCode().toString());
        log.info("Slack response body is {}", response.getBody().toString());
    }

    private HttpEntity buildHttpEntityWithAuthorisation(SlackMessageRequest message){
        HttpHeaders multiValueMap = new HttpHeaders();
        multiValueMap.setContentType(MediaType.APPLICATION_JSON);
        multiValueMap.set(HttpHeaders.AUTHORIZATION, "Bearer " + authConfig.getSlack().getToken());
        return new HttpEntity<>(message, multiValueMap);
    }

}
