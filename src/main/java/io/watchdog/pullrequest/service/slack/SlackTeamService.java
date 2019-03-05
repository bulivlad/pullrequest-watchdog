package io.watchdog.pullrequest.service.slack;

import com.mongodb.MongoWriteException;
import io.watchdog.pullrequest.dto.PullRequestDTO;
import io.watchdog.pullrequest.dto.slack.SlackTeamDTO;
import io.watchdog.pullrequest.model.SlackTeam;
import io.watchdog.pullrequest.model.SlackUser;
import io.watchdog.pullrequest.service.PullRequestRetrieveService;
import io.watchdog.pullrequest.service.TeamService;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import me.ramswaroop.jbot.core.slack.models.Event;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
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
    private final static String START_SLACK_MESSAGE_TEMPLATE = "PRs waiting for reviewers today:\n";
    // \t\t{name} - {link} - {users}
    private final static String BODY_SLACK_MESSAGE_TEMPLATE = "\t\t %s - %s - %s";

    TeamService teamService;
    PullRequestRetrieveService pullRequestRetrieveService;

    @Autowired
    public SlackTeamService(TeamService teamService, PullRequestRetrieveService pullRequestRetrieveService) {
        this.teamService = teamService;
        this.pullRequestRetrieveService = pullRequestRetrieveService;
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

    public List<String> getPullRequestsWithSlackUsers(List<String> reviewers) {
        List<PullRequestDTO> unapprovedPRs = pullRequestRetrieveService.getUnapprovedPRs(reviewers);
        log.debug(unapprovedPRs.toString());

        List<String> messages = buildSlackMessagesString(unapprovedPRs.stream());
        log.debug(messages.toString());

        return messages;
    }

    public SlackTeam buildSlackTeam(Event event){
        SlackTeamDTO slackTeamDTO = buildSlackTeamDTO(event);
        List<SlackUser> users = Stream.of(slackTeamDTO.getMembers())
                .map(member -> member.substring(member.indexOf("@") + 1))
                .map(String::trim)
                .map(member -> SlackUser.builder().name(member).username(member).build())
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
        SlackTeamDTO slackTeamDTO = new SlackTeamDTO();

        String text = event.getText();
        log.info("Got message {}", text);

        slackTeamDTO.setChannel(event.getChannelId());
        slackTeamDTO.setTeamName(StringUtils.substringBetween(text, "team ", " ").trim());
        slackTeamDTO.setMembers(StringUtils.substringBetween(text, "members [", "]").trim().split(","));
        slackTeamDTO.setScheduler(StringUtils.substringAfter(text, "scheduler "));
        log.info("Got messages team name '{}' members '{}' scheduler '{}'", slackTeamDTO.getTeamName(),
                slackTeamDTO.getMembers(), slackTeamDTO.getScheduler());

        return slackTeamDTO;
    }

    private List<String> buildSlackMessagesString(Stream<PullRequestDTO> unapprovedPRs){
        List<String> messages = new ArrayList<>();
        messages.add(START_SLACK_MESSAGE_TEMPLATE);

        unapprovedPRs.forEach(pullRequestDTO -> {
            StringBuilder stringBuilder = new StringBuilder();
            pullRequestDTO.getReviewers().forEach((value) -> {
                stringBuilder.append("<@").append(value.getUsername()).append("> ");
                messages.add(String.format(BODY_SLACK_MESSAGE_TEMPLATE, pullRequestDTO.getSourceBranch(), pullRequestDTO.getLink(), stringBuilder));
            });
        });
        return messages;
    }

}
