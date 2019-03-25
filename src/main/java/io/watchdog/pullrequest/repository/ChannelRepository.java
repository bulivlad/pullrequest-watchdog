package io.watchdog.pullrequest.repository;

import io.watchdog.pullrequest.model.slack.SlackChannel;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

/**
 * @author vladclaudiubulimac on 2019-03-25.
 */
public interface ChannelRepository extends MongoRepository<SlackChannel, String> {

    Optional<SlackChannel> findByIdOrName(String id, String name);

}
