package io.watchdog.pullrequest.service.slack;

import io.watchdog.pullrequest.config.AuthConfig;
import io.watchdog.pullrequest.config.RepositoryConfig;
import io.watchdog.pullrequest.dto.slack.SlackUserDTO;
import io.watchdog.pullrequest.model.slack.SlackCommand;
import io.watchdog.pullrequest.dto.slack.SlackMessageRequestDTO;
import io.watchdog.pullrequest.dto.slack.SlackMessageResponseDTO;
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
public class SlackApiRestService {

    AuthConfig authConfig;
    RestTemplate restTemplate;
    RepositoryConfig repositoryConfig;

    @Autowired
    public SlackApiRestService(AuthConfig authConfig, RepositoryConfig repositoryConfig) {
        this.authConfig = authConfig;
        this.repositoryConfig = repositoryConfig;
        this.restTemplate = new RestTemplate();
    }

    public void sendMessageToChannel(SlackMessageRequestDTO message) {
        sendSlackCommand(message, SlackCommand.MESSAGE_CHANNEL);
    }

    public SlackUserDTO retrieveSlackUserDetails(String slackUserId){
        ResponseEntity<SlackUserDTO> slackResponse = restTemplate.exchange(
                authConfig.getSlack().getEndpoint() + SlackCommand.USERS_INFO.getValue() + "?user={userId}",
                HttpMethod.GET,
                buildHttpEntityWithAuthorisation(null),
                SlackUserDTO.class,
                slackUserId);
        handleResponse(slackResponse);
        return slackResponse.getBody();
    }

    private SlackMessageResponseDTO sendSlackCommand(SlackMessageRequestDTO message, SlackCommand slackCommand){
        ResponseEntity<SlackMessageResponseDTO> slackResponse = restTemplate.exchange(
                authConfig.getSlack().getEndpoint() + slackCommand.getValue(),
                HttpMethod.POST,
                buildHttpEntityWithAuthorisation(message),
                SlackMessageResponseDTO.class,
                repositoryConfig.getUsername(),
                repositoryConfig.getSlug());
        handleResponse(slackResponse);
        return slackResponse.getBody();
    }

    private void handleResponse(ResponseEntity response) {
        log.info("Slack response code is {}", response.getStatusCode().toString());
        log.debug("Slack response body is {}", response.getBody().toString());
    }

    private HttpEntity buildHttpEntityWithAuthorisation(SlackMessageRequestDTO message){
        HttpHeaders multiValueMap = new HttpHeaders();
        multiValueMap.setContentType(MediaType.APPLICATION_JSON);
        multiValueMap.set(HttpHeaders.AUTHORIZATION, "Bearer " + authConfig.getSlack().getToken());
        return new HttpEntity<>(message, multiValueMap);
    }

}
