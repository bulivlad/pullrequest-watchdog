package io.watchdog.pullrequest.service;

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import io.watchdog.pullrequest.dto.PaginatedPullRequestDTO;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.function.Consumer;

/**
 * @author vladclaudiubulimac on 2019-03-03.
 */

@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PullRequestDTOIterator implements Iterator<PaginatedPullRequestDTO> {

    final RestTemplate restTemplate;
    String next;

    public PullRequestDTOIterator(PaginatedPullRequestDTO paginatedPullRequestDTO, RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
        this.next = paginatedPullRequestDTO.getNext();
    }

    @Override
    public boolean hasNext() {
        return next != null;
    }

    @Override
    public PaginatedPullRequestDTO next() {
        try {
            PaginatedPullRequestDTO forObject = restTemplate.getForObject(
                    URLDecoder.decode(this.next, StandardCharsets.UTF_8.toString()),
                    PaginatedPullRequestDTO.class);
            this.next = forObject.getNext();
            return forObject;
        } catch (RestClientException | UnsupportedEncodingException ex) {
            log.error("Exception on retrieving PR's for " + next, ex);
            return new PaginatedPullRequestDTO();
        }
    }

    @Override
    public void forEachRemaining(Consumer<? super PaginatedPullRequestDTO> action) {
        while(hasNext()){
            action.accept(next());
        }
    }
}
