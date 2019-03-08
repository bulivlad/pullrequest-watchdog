package io.watchdog.pullrequest.service.slack;

import com.mongodb.MongoWriteException;
import io.watchdog.pullrequest.dto.PullRequestDTO;
import io.watchdog.pullrequest.dto.slack.SlackTeamDTO;
import io.watchdog.pullrequest.dto.slack.SlackUserDTO;
import io.watchdog.pullrequest.model.CorrelatedUser;
import io.watchdog.pullrequest.model.User;
import io.watchdog.pullrequest.model.slack.SlackTeam;
import io.watchdog.pullrequest.model.slack.SlackUser;
import io.watchdog.pullrequest.service.PullRequestRetrieveService;
import io.watchdog.pullrequest.service.TeamService;
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

/**
 * @author vladclaudiubulimac on 2019-03-05.
 */

@Slf4j
@Component
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class SlackTeamService {
    private final static String START_SLACK_MESSAGE_TEMPLATE = "PRs waiting for reviewers today:\n";
    // {name} - {link} - {users}
    private final static String BODY_SLACK_MESSAGE_TEMPLATE = "%s - %s - %s";

    TeamService teamService;
    PullRequestRetrieveService pullRequestRetrieveService;
    SlackApiRestService slackApiRestService;

    @Autowired
    public SlackTeamService(TeamService teamService, PullRequestRetrieveService pullRequestRetrieveService, SlackApiRestService slackApiRestService) {
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

    public List<String> getSlackMessages(List<CorrelatedUser> reviewers) {
        List<String> reviewersUsername = reviewers.stream()
                .map(CorrelatedUser::getBitbucketUser)
                .map(User::getUsername)
                .collect(Collectors.toList());
        List<PullRequestDTO> unapprovedPRs = pullRequestRetrieveService.getUnapprovedPRs(reviewersUsername);

        log.debug(unapprovedPRs.toString());

        List<String> messages = buildSlackMessagesString(unapprovedPRs.stream(), reviewers);
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
                .build();
    }

    //add team rdc2-team with members [@vbulimac, @nbuhosu] and scheduler 0 20 11 1/1 * ? *

    private SlackTeamDTO buildSlackTeamDTO(Event event) {
        String text = event.getText();
        log.info("Got message {}", text);

        SlackTeamDTO slackTeamDTO = new SlackTeamDTO();
        slackTeamDTO.setChannel(event.getChannelId());
        slackTeamDTO.setTeamName(StringUtils.substringBetween(text, "team ", " ").trim());
        slackTeamDTO.setMembers(StringUtils.substringBetween(text, "members [", "]").trim().split(","));
        slackTeamDTO.setScheduler(StringUtils.substringAfter(text, "scheduler "));
        log.debug("Got messages team name '{}' members '{}' scheduler '{}'", slackTeamDTO.getTeamName(),
                slackTeamDTO.getMembers(),
                slackTeamDTO.getScheduler());

        return slackTeamDTO;
    }

    private List<String> buildSlackMessagesString(Stream<PullRequestDTO> unapprovedPRs, List<CorrelatedUser> bitbucketUserDTOs){
        List<String> messages = new ArrayList<>();
        messages.add(START_SLACK_MESSAGE_TEMPLATE);

        unapprovedPRs.filter(pullRequestDTO -> !CollectionUtils.isEmpty(pullRequestDTO.getReviewers()))
                .forEach(pullRequestDTO -> {
                    StringBuilder stringBuilder = new StringBuilder();
                    pullRequestDTO.getReviewers()
                            .forEach((value) -> stringBuilder.append("<@").append(getSlackUserMention(bitbucketUserDTOs.stream(), value.getUsername())).append("> "));
                    messages.add(String.format(BODY_SLACK_MESSAGE_TEMPLATE, pullRequestDTO.getSourceBranch(), pullRequestDTO.getLink(), stringBuilder));
                });
        return messages;
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

}
