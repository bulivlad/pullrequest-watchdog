package io.watchdog.pullrequest.repository;

import io.watchdog.pullrequest.model.slack.SlackTeam;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

/**
 * @author vladclaudiubulimac on 2019-03-03.
 */

@Component
public interface TeamRepository extends MongoRepository<SlackTeam, String> {

    Optional<SlackTeam> findSlackTeamByChannelAndName(String channel, String name);
    Optional<SlackTeam> findSlackTeamByChannelAndNameAndSlug(String channel, String name, String slug);
    List<SlackTeam> findSlackTeamsByChannel(String channel);
    List<SlackTeam> findAllByCheckingScheduleIsNotNull();

}
