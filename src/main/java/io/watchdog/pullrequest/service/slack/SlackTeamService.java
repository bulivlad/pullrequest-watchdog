package io.watchdog.pullrequest.service.slack;

import com.mongodb.MongoWriteException;
import io.watchdog.pullrequest.dto.slack.SlackTeamDTO;
import io.watchdog.pullrequest.model.SlackTeam;
import io.watchdog.pullrequest.model.SlackUser;
import io.watchdog.pullrequest.service.TeamService;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import me.ramswaroop.jbot.core.slack.models.Event;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author vladclaudiubulimac on 2019-03-05.
 */

@Slf4j
@Component
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class SlackTeamService {

    TeamService teamService;

    @Autowired
    public SlackTeamService(TeamService teamService) {
        this.teamService = teamService;
    }

    public boolean saveTeam(SlackTeam slackTeam) {
        try{
            teamService.saveTeam(slackTeam);
        } catch (MongoWriteException | DuplicateKeyException ex){
            log.warn("Team '{}' in channel {} is already existing in the database!", slackTeam.getName(), slackTeam.getChannel());
            return false;
        }

        return true;
    }

    public SlackTeam buildSlackTeam(Event event){
        SlackTeamDTO slackTeamDTO = buildSlackTeamDTO(event);
        List<SlackUser> users = Stream.of(slackTeamDTO.getMembers())
                .map(e -> e.substring(e.indexOf("@") + 1))
                .map(e -> SlackUser.builder().name(e).username(e).build())
                .collect(Collectors.toList());
        return SlackTeam.builder()
                .channel(slackTeamDTO.getChannel())
                .members(users)
                .name(slackTeamDTO.getTeamName())
                .checkingSchedule(slackTeamDTO.getScheduler())
                .build();
    }

    private SlackTeamDTO buildSlackTeamDTO(Event event) {
        SlackTeamDTO slackTeamDTO = new SlackTeamDTO();

        String text = event.getText();
        log.info("Got message {}", text);

        slackTeamDTO.setChannel(event.getChannelId());
        slackTeamDTO.setTeamName(StringUtils.substringBetween(text, "team ", " ").trim());
        slackTeamDTO.setMembers(StringUtils.substringBetween(text, "members ", " ").trim().split(","));
        slackTeamDTO.setScheduler(StringUtils.substringAfter(text, "scheduler "));
        log.info("Got messages team name '{}' members '{}' scheduler '{}'", slackTeamDTO.getTeamName(),
                slackTeamDTO.getMembers(), slackTeamDTO.getScheduler());

        return slackTeamDTO;
    }

}
