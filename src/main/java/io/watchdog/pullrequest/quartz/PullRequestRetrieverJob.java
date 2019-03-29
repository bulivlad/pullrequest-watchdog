package io.watchdog.pullrequest.quartz;

import com.google.common.collect.ImmutableMap;
import io.watchdog.pullrequest.dto.slack.SlackMessageRequestDTO;
import io.watchdog.pullrequest.model.CorrelatedUser;
import io.watchdog.pullrequest.model.slack.MessageType;
import io.watchdog.pullrequest.service.slack.SlackApiRestService;
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

/**
 * @author vladclaudiubulimac on 2019-03-05.
 */

@Slf4j
@Component
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class PullRequestRetrieverJob implements Job {

    SlackTeamService slackService;
    SlackApiRestService slackApiRestService;

    @Autowired
    public PullRequestRetrieverJob(SlackTeamService slackService, SlackApiRestService slackApiRestService) {
        this.slackService = slackService;
        this.slackApiRestService = slackApiRestService;
    }

    public void execute(JobExecutionContext context) {
        JobDataMap mergedJobDataMap = context.getJobDetail().getJobDataMap();
        mergedJobDataMap.getWrappedMap().forEach((key, value) -> log.debug("key '{}' and value '{}' ", key, value));
        List<CorrelatedUser> teamUsers = (List<CorrelatedUser>) mergedJobDataMap.getOrDefault("members", Collections.emptyList());
        String repoSlug = (String) mergedJobDataMap.getOrDefault("slug", "");

        List<String> messagesList = slackService.getSlackMessages(teamUsers, repoSlug);

        SlackMessageRequestDTO slackMessage = buildSlackRequestMessage(mergedJobDataMap, messagesList);
        log.debug("Sending message {} to slack", slackMessage);

        slackApiRestService.sendMessageToChannel(slackMessage, ImmutableMap.of("slug", repoSlug));

        log.info("cronjob for pull requests executed for team {} in channel {}",
                mergedJobDataMap.getString("team"), mergedJobDataMap.getString("channel"));
    }

    private SlackMessageRequestDTO buildSlackRequestMessage(JobDataMap mergedJobDataMap, List<String> messagesList) {
        return SlackMessageRequestDTO.builder()
                    .channel(mergedJobDataMap.getString("channel"))
                    .blocks(buildMessageBlocks(messagesList))
                    .build();
    }

    private List<SlackMessageRequestDTO.Block> buildMessageBlocks(List<String> messagesList) {
        List<SlackMessageRequestDTO.Block> blocks = new ArrayList<>();
        messagesList.forEach(message -> {
            blocks.add(new SlackMessageRequestDTO.Block(new SlackMessageRequestDTO.Text(message)));
            blocks.add(new SlackMessageRequestDTO.Block(MessageType.DIVIDER.name().toLowerCase()));
        });
        return blocks;
    }

}