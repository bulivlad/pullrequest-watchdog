package io.watchdog.pullrequest.service;

import io.watchdog.pullrequest.model.SlackTeam;
import io.watchdog.pullrequest.repository.TeamRepository;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @author vladclaudiubulimac on 2019-03-03.
 */

@Component
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class TeamService {

    TeamRepository teamRepository;

    @Autowired
    public TeamService(TeamRepository teamRepository) {
        this.teamRepository = teamRepository;
    }

    public List<SlackTeam> getAllTeams() {
        return teamRepository.findAll();
    }

    public List<SlackTeam> getAllTeamsInChannel(String channel) {
        return teamRepository.findSlackTeamsByChannel(channel);
    }

    public SlackTeam saveTeam(SlackTeam slackTeam) {
        return teamRepository.save(slackTeam);
    }

    public SlackTeam updateTeam(SlackTeam slackTeam) {
        SlackTeam existingTeam = getSpecificTeam(slackTeam.getChannel(), slackTeam.getName());
        slackTeam.setId(existingTeam.getId());
        return teamRepository.save(slackTeam);
    }

    public void deleteTeam(String channel, String teamName) {
        SlackTeam slackTeam = getSpecificTeam(channel, teamName);
        teamRepository.delete(slackTeam);
    }

    public SlackTeam getSpecificTeam(String channel, String teamName) {
        return teamRepository.findSlackTeamByChannelAndAndName(channel, teamName);
    }

}
