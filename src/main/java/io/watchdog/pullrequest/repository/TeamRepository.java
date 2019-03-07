package io.watchdog.pullrequest.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Component;
import io.watchdog.pullrequest.model.slack.SlackTeam;

import java.util.List;

/**
 * @author vladclaudiubulimac on 2019-03-03.
 */

@Component
public interface TeamRepository extends MongoRepository<SlackTeam, String> {

    SlackTeam findSlackTeamByChannelAndName(String channel, String name);
    List<SlackTeam> findSlackTeamsByChannel(String channel);
    List<SlackTeam> findAllByCheckingScheduleIsNotNull();

}
