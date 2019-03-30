package io.watchdog.pullrequest.service;

import io.watchdog.pullrequest.config.RepositoryConfig;
import io.watchdog.pullrequest.dto.BitbucketUserDTO;
import io.watchdog.pullrequest.model.BitbucketUser;
import io.watchdog.pullrequest.model.CorrelatedUser;
import io.watchdog.pullrequest.model.slack.SlackTeam;
import io.watchdog.pullrequest.model.slack.SlackUser;
import io.watchdog.pullrequest.quartz.SchedulerService;
import io.watchdog.pullrequest.repository.TeamRepository;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.quartz.SchedulerException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * @author vladclaudiubulimac on 2019-03-03.
 */

@Component
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class TeamService {

    BitBucketApiRestService bitBucketApiRestService;
    SchedulerService schedulerService;
    TeamRepository teamRepository;
    RepositoryConfig repositoryConfig;

    @Autowired
    public TeamService(BitBucketApiRestService bitBucketApiRestService,
                       SchedulerService schedulerService,
                       TeamRepository teamRepository,
                       RepositoryConfig repositoryConfig) {
        this.bitBucketApiRestService = bitBucketApiRestService;
        this.schedulerService = schedulerService;
        this.teamRepository = teamRepository;
        this.repositoryConfig = repositoryConfig;
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

    public SlackTeam saveTeam(SlackTeam slackTeam) throws SchedulerException {
        if(StringUtils.isEmpty(slackTeam.getSlug())) {
            slackTeam.setSlug(repositoryConfig.getSlug());
        }
        slackTeam.getMembers().forEach(member -> member.setBitbucketUser(buildBitbucketUsersForTeam(member.getSlackUser())));
        schedulerService.scheduleEventForTeam(slackTeam);
        return teamRepository.save(slackTeam);
    }

    public SlackTeam updateTeam(SlackTeam slackTeam) throws SchedulerException {
        SlackTeam existingTeam = getSpecificTeamOrNewTeamInRepo(slackTeam.getChannel(), slackTeam.getName(), slackTeam.getSlug());
        schedulerService.rescheduleEventForTeam(slackTeam);

        if(!CollectionUtils.isEmpty(slackTeam.getMembers())){
            slackTeam.getMembers().stream()
                    .filter(this::isBitbucketUserMissing)
                    .forEach(member -> member.setBitbucketUser(buildBitbucketUsersForTeam(member.getSlackUser())));
        }

        slackTeam.setId(existingTeam.getId());
        return teamRepository.save(slackTeam);
    }

    private boolean isBitbucketUserMissing(CorrelatedUser member) {
        return Objects.isNull(member.getBitbucketUser()) ||
                Objects.equals(member.getBitbucketUser(), new BitbucketUser());
    }

    public boolean deleteTeam(String channel, String teamName, String repoSlug) throws SchedulerException {
        SlackTeam slackTeam = getSpecificTeamOrNewTeamInRepo(channel, teamName, repoSlug);
        slackTeam.setCheckingSchedule(null);
        schedulerService.rescheduleEventForTeam(slackTeam);
        teamRepository.delete(slackTeam);
        return true;
    }

    public SlackTeam getSpecificTeamOrNewTeamInRepo(String channel, String teamName, String repoSlug) {
        return teamRepository.findSlackTeamByChannelAndNameAndSlug(channel, teamName, repoSlug)
                .orElse(SlackTeam.builder().channel(channel).name(teamName).build());
    }

    public SlackTeam getSpecificTeamOrNewTeam(String channel, String teamName) {
        return teamRepository.findSlackTeamByChannelAndName(channel, teamName)
                .orElse(SlackTeam.builder().channel(channel).name(teamName).build());
    }

    public Optional<SlackTeam> getSpecificTeam(String channel, String teamName) {
        return teamRepository.findSlackTeamByChannelAndName(channel, teamName);
    }

    public Optional<SlackTeam> getSpecificTeamInRepo(String channel, String teamName, String slug) {
        return teamRepository.findSlackTeamByChannelAndNameAndSlug(channel, teamName, slug);
    }

    private BitbucketUser buildBitbucketUsersForTeam(SlackUser slackUser) {
        return convertBitbucketDtoToBitbucketUser(bitBucketApiRestService.fetchBitbucketUserDetailsByEmail(slackUser.getEmail()));
    }

    private BitbucketUser convertBitbucketDtoToBitbucketUser(BitbucketUserDTO bitbucketUserDTO){
        return BitbucketUser.builder().name(bitbucketUserDTO.getDisplayName()).username(bitbucketUserDTO.getUsername()).build();
    }

}
