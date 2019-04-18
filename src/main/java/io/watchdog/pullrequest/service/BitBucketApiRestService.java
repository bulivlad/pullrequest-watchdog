package io.watchdog.pullrequest.service;

import io.watchdog.pullrequest.config.RepositoryConfig;
import io.watchdog.pullrequest.dto.BitbucketUserDTO;
import io.watchdog.pullrequest.dto.BitbucketUserDTOWrapper;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import static java.util.Objects.nonNull;

/**
 * @author vladclaudiubulimac on 2019-03-07.
 */

@Slf4j
@Component
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class BitBucketApiRestService {

    RestTemplate restTemplate;
    RepositoryConfig repositoryConfig;

    @Autowired
    public BitBucketApiRestService(RestTemplate restTemplate, RepositoryConfig repositoryConfig) {
        this.restTemplate = restTemplate;
        this.repositoryConfig = repositoryConfig;
    }

    @Retryable(value = {HttpClientErrorException.TooManyRequests.class}, backoff = @Backoff(delay = 500))
    public BitbucketUserDTO fetchBitbucketUserDetailsByEmail(String email) {
        log.info("Getting user info for {}", email);
            BitbucketUserDTOWrapper userWrapper = restTemplate.getForObject(
                    repositoryConfig.getUsersUrl(),
                    BitbucketUserDTOWrapper.class,
                    email);
            log.info("Got user info for {}", email);
            return nonNull(userWrapper) ? userWrapper.getUser() : new BitbucketUserDTO();
    }

}
