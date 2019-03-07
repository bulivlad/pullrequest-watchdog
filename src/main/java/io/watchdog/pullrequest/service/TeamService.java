package io.watchdog.pullrequest.service;

import io.watchdog.pullrequest.dto.BitbucketUserDTO;
import io.watchdog.pullrequest.model.BitbucketUser;
import io.watchdog.pullrequest.model.slack.SlackTeam;
import io.watchdog.pullrequest.model.slack.SlackUser;
import io.watchdog.pullrequest.repository.TeamRepository;
import io.watchdog.pullrequest.service.slack.SlackApiRestService;
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

    BitBucketApiRestService bitBucketApiRestService;
    SlackApiRestService slackApiRestService;
    TeamRepository teamRepository;

    @Autowired
    public TeamService(BitBucketApiRestService bitBucketApiRestService,
                       SlackApiRestService slackApiRestService,
                       TeamRepository teamRepository) {
        this.bitBucketApiRestService = bitBucketApiRestService;
        this.slackApiRestService = slackApiRestService;
        this.teamRepository = teamRepository;
    }

    public List<SlackTeam> getAllTeams() {
        return teamRepository.findAll();
    }

    public List<SlackTeam> getAllTeamsInChannel(String channel) {
        return teamRepository.findSlackTeamsByChannel(channel);
    }

    public List<SlackTeam> getAllTeamsWithScheduleExpression() {
        return teamRepository.findAllByCheckingScheduleIsNotNull();
    }

    public SlackTeam saveTeam(SlackTeam slackTeam) {
        slackTeam.getMembers().forEach(e -> e.setBitbucketUser(buildBitbucketUsersForTeam(e.getSlackUser())));
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
        return teamRepository.findSlackTeamByChannelAndName(channel, teamName);
    }

    private BitbucketUser buildBitbucketUsersForTeam(SlackUser slackUser) {
        return convertBitbucketDtoToBitbucketUser(bitBucketApiRestService.fetchBitbucketUserDetailsByEmail(slackUser.getEmail()));
    }

    private BitbucketUser convertBitbucketDtoToBitbucketUser(BitbucketUserDTO bitbucketUserDTO){
        return BitbucketUser.builder().name(bitbucketUserDTO.getDisplayName()).username(bitbucketUserDTO.getUsername()).build();
    }

}
