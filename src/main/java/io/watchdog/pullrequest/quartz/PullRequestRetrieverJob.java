package io.watchdog.pullrequest.quartz;

import io.watchdog.pullrequest.model.SlackUser;
import io.watchdog.pullrequest.model.User;
import io.watchdog.pullrequest.model.slack.MessageType;
import io.watchdog.pullrequest.model.slack.SlackMessageRequest;
import io.watchdog.pullrequest.service.slack.SlackRestService;
import io.watchdog.pullrequest.service.slack.SlackTeamService;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author vladclaudiubulimac on 2019-03-05.
 */

@Slf4j
@Component
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class PullRequestRetrieverJob implements Job {

    SlackTeamService slackService;
    SlackRestService slackRestService;

    @Autowired
    public PullRequestRetrieverJob(SlackTeamService slackService, SlackRestService slackRestService) {
        this.slackService = slackService;
        this.slackRestService = slackRestService;
    }

    public void execute(JobExecutionContext context) {
        JobDataMap mergedJobDataMap = context.getJobDetail().getJobDataMap();
        mergedJobDataMap.getWrappedMap().forEach((key, value) -> log.debug("key '{}' and value '{}' ", key, value));
        List<SlackUser> teamSlackUsers = (List<SlackUser>) mergedJobDataMap.getOrDefault("members", Collections.emptyList());
        List<String> reviewers = teamSlackUsers.stream().map(User::getUsername).collect(Collectors.toList());

        List<String> messagesList = slackService.getPullRequestsWithSlackUsers(reviewers);

        SlackMessageRequest slackMessage = buildSlackRequestMessage(mergedJobDataMap, messagesList);
        log.debug("Sending message {} to slack", slackMessage);

        slackRestService.sendMessageToChannel(slackMessage);

        log.info("cronjob for pull requests executed for team {} in channel {}",
                mergedJobDataMap.getString("team"), mergedJobDataMap.getString("channel"));
    }

    private SlackMessageRequest buildSlackRequestMessage(JobDataMap mergedJobDataMap, List<String> messagesList) {
        return SlackMessageRequest.builder()
                    .channel(mergedJobDataMap.getString("channel"))
                    .blocks(buildMessageBlocks(messagesList))
                    .iconEmoji(":white_check_mark:")
                    .build();
    }

    private List<SlackMessageRequest.Block> buildMessageBlocks(List<String> messagesList) {
        List<SlackMessageRequest.Block> blocks = new ArrayList<>();
        messagesList.forEach(message -> {
            blocks.add(new SlackMessageRequest.Block(new SlackMessageRequest.Text(message)));
            blocks.add(new SlackMessageRequest.Block(MessageType.DIVIDER.name().toLowerCase()));
        });
        return blocks;
    }

}