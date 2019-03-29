package io.watchdog.pullrequest.service.slack;

import com.mongodb.MongoWriteException;
import io.watchdog.pullrequest.dto.PullRequestDTO;
import io.watchdog.pullrequest.dto.slack.SlackTeamDTO;
import io.watchdog.pullrequest.dto.slack.SlackUserDTO;
import io.watchdog.pullrequest.exception.SlackTeamNotFoundException;
import io.watchdog.pullrequest.model.CorrelatedUser;
import io.watchdog.pullrequest.model.User;
import io.watchdog.pullrequest.model.slack.SlackTeam;
import io.watchdog.pullrequest.model.slack.SlackUser;
import io.watchdog.pullrequest.service.PullRequestRetrieveService;
import io.watchdog.pullrequest.service.TeamService;
import io.watchdog.pullrequest.util.BotUtil;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import me.ramswaroop.jbot.core.slack.models.Event;
import org.apache.commons.lang.StringUtils;
import org.quartz.SchedulerException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static io.watchdog.pullrequest.model.slack.SlackEventMapping.ADD_TEAM_EVENT_REGEX;

/**
 * @author vladclaudiubulimac on 2019-03-05.
 */

@Slf4j
@Component
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class SlackTeamService {
    private final static String START_SLACK_MESSAGE_TEMPLATE = "PRs waiting for reviewers today for repo *%s*:\n";
    private final static String NO_PR_SLACK_MESSAGE_TEMPLATE = ":woohoo: No PRs to be reviewed today for repo *%s* !\n";
    // {name} - {link} - {users}
    private final static String BODY_SLACK_MESSAGE_TEMPLATE = "%s - %s - %s";

    TeamService teamService;
    PullRequestRetrieveService pullRequestRetrieveService;
    SlackApiRestService slackApiRestService;

    @Autowired
    public SlackTeamService(TeamService teamService,
                            PullRequestRetrieveService pullRequestRetrieveService,
                            SlackApiRestService slackApiRestService) {
        this.teamService = teamService;
        this.pullRequestRetrieveService = pullRequestRetrieveService;
        this.slackApiRestService = slackApiRestService;
    }

    public boolean saveTeam(SlackTeam slackTeam) {
        try{
            teamService.saveTeam(slackTeam);
        } catch (MongoWriteException | DuplicateKeyException ex){
            log.warn("Team '{}' in channel {} is already existing in the database!", slackTeam.getName(), slackTeam.getChannel());
            return false;
        } catch (SchedulerException ex) {
            log.error("Could not schedule cronjob for team " + slackTeam.getName() + " in channel " + slackTeam.getChannel(), ex);
            return false;
        }

        return true;
    }

    public boolean updateTeam(SlackTeam slackTeam) {
        try {
            teamService.updateTeam(slackTeam);
        } catch (SchedulerException ex) {
            log.error("Could not schedule cronjob for team " + slackTeam.getName() + " in channel " + slackTeam.getChannel(), ex);
            return false;
        }

        return true;
    }

    public List<String> getSlackMessages(List<CorrelatedUser> reviewers, String repoSlug) {
        List<String> reviewersUsername = reviewers.stream()
                .map(CorrelatedUser::getBitbucketUser)
                .map(User::getUsername)
                .collect(Collectors.toList());
        List<PullRequestDTO> unapprovedPRs = pullRequestRetrieveService.getUnapprovedPRs(reviewersUsername, repoSlug);

        log.debug(unapprovedPRs.toString());

        List<String> messages = buildSlackMessagesString(unapprovedPRs.stream(), reviewers, repoSlug);
        log.debug(messages.toString());

        return messages;
    }

    public SlackTeam buildSlackTeam(Event event){
        SlackTeamDTO slackTeamDTO = buildSlackTeamDTO(event);
        List<CorrelatedUser> users = Stream.of(slackTeamDTO.getMembers())
                .map(member -> StringUtils.substringBetween(member, "<@", ">"))
                .map(String::trim)
                .map(this::buildCorrelatedUser)
                .collect(Collectors.toList());
        return SlackTeam.builder()
                .channel(slackTeamDTO.getChannel())
                .members(users)
                .name(slackTeamDTO.getTeamName())
                .checkingSchedule(slackTeamDTO.getScheduler())
                .slug(slackTeamDTO.getSlug())
                .build();
    }

    //add team rdc2-team for repository development with members [@vbulimac, @nbuhosu] and scheduler 0 20 11 1/1 * ? *
    //(add team).*(repository\s)(?<repository>[a-zA-Z0-9]).*(members\s\[).*(\]).*(and\sscheduler ).*

    private SlackTeamDTO buildSlackTeamDTO(Event event) {
        String text = event.getText();
        log.info("Got message {}", text);

        SlackTeamDTO slackTeamDTO = new SlackTeamDTO();
        slackTeamDTO.setChannel(event.getChannelId());
        slackTeamDTO.setTeamName(BotUtil.getGroupMatcherFromEventMessage(event.getText(), ADD_TEAM_EVENT_REGEX.getValue(), "teamName").orElse("").trim());
        slackTeamDTO.setMembers(BotUtil.getGroupMatcherFromEventMessage(event.getText(), ADD_TEAM_EVENT_REGEX.getValue(), "members").orElse("").trim().split(","));
        slackTeamDTO.setScheduler(BotUtil.getGroupMatcherFromEventMessage(event.getText(), ADD_TEAM_EVENT_REGEX.getValue(), "schedulerExpression").orElse(""));
        slackTeamDTO.setSlug(BotUtil.getGroupMatcherFromEventMessage(event.getText(), ADD_TEAM_EVENT_REGEX.getValue(), "repository").orElse(""));
        log.debug("Got messages team name '{}' members '{}' scheduler '{}'", slackTeamDTO.getTeamName(),
                slackTeamDTO.getMembers(),
                slackTeamDTO.getScheduler());

        return slackTeamDTO;
    }

    private List<String> buildSlackMessagesString(Stream<PullRequestDTO> unapprovedPRs, List<CorrelatedUser> bitbucketUserDTOs, String repoSlug){
        List<String> messages = new ArrayList<>();

        unapprovedPRs.filter(pullRequestDTO -> !CollectionUtils.isEmpty(pullRequestDTO.getReviewers()))
                .forEach(pullRequestDTO -> {
                    StringBuilder stringBuilder = new StringBuilder();
                    pullRequestDTO.getReviewers()
                            .forEach((value) -> stringBuilder.append("<@").append(getSlackUserMention(bitbucketUserDTOs.stream(), value.getUsername())).append("> "));
                    messages.add(String.format(BODY_SLACK_MESSAGE_TEMPLATE, pullRequestDTO.getSourceBranch(), pullRequestDTO.getLink(), stringBuilder));
                });

        messages.add(0, getMessageHeader(repoSlug, messages));
        return messages;
    }

    private String getMessageHeader(String repoSlug, List<String> messages) {
        return String.format(messages.isEmpty() ? NO_PR_SLACK_MESSAGE_TEMPLATE : START_SLACK_MESSAGE_TEMPLATE, repoSlug);
    }

    private String getSlackUserMention(Stream<CorrelatedUser> bitbucketUserDTOs, String reviewerUsername){
        return bitbucketUserDTOs
                .filter(user -> Objects.nonNull(user.getBitbucketUser()))
                .filter(user -> Objects.nonNull(user.getBitbucketUser().getUsername()))
                .filter(users -> users.getBitbucketUser().getUsername().equals(reviewerUsername))
                .findFirst()
                .orElse(new CorrelatedUser())
                .getSlackUser()
                .getMention();
    }

    private CorrelatedUser buildCorrelatedUser(String slackUserId){
        SlackUserDTO slackUserDTO = slackApiRestService.retrieveSlackUserDetails(slackUserId);
        SlackUser slackUser = SlackUser.builder()
                .name(slackUserDTO.getUserRealName())
                .mention(slackUserId)
                .username(slackUserDTO.getUserDisplayName())
                .email(slackUserDTO.getUserEmail())
                .build();

        return new CorrelatedUser(slackUser);
    }

    public boolean removeTeam(String channel, String teamName, String repoSlug) {
        try {
            return teamService.deleteTeam(channel, teamName, repoSlug);
        } catch (IllegalArgumentException ex) {
            log.warn("Team '" + teamName + "' in channel '" + channel + "' could not be found!", ex);
        } catch (SchedulerException ex) {
            log.error("Could not unschedule cronjob for team " + teamName + " in channel " + channel, ex);
        }
        return false;
    }

    public boolean unscheduleTeam(String channel, String teamName, String slug) {
        try{
            SlackTeam slackTeam = teamService.getSpecificTeamInRepo(channel, teamName, slug)
                    .orElseThrow(() -> new SlackTeamNotFoundException("Team " + teamName + " not found in channel " + channel));
            slackTeam.setCheckingSchedule(null);
            teamService.updateTeam(slackTeam);
        } catch (MongoWriteException ex) {
            log.warn("Team '" + teamName + "' in channel '" + channel + "' could not be updated!", ex);
            return false;
        } catch (SchedulerException ex) {
            log.error("Could not unschedule cronjob for team " + teamName + " in channel " + channel, ex);
            return false;
        } catch (SlackTeamNotFoundException ex) {
            log.warn("Team '" + teamName + "' in channel '" + channel + "' could not be found!", ex);
            return false;
        } catch (Exception ex) {
            log.error("Unexpected error when tried to handle team " + teamName + " in channel " + channel, ex);
            return false;
        }
        return true;
    }
}
