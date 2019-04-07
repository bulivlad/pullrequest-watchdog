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
 * Cron job class used to build and send the slack messages for each scheduled slack team
 *
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

    /**
     * Execution point of the cron job. This method will build and send the slack messages regarding the
     * team's open pull requests for the slack team scheduled
     *
     * @param context the cron job context
     */
    @Override
    public void execute(JobExecutionContext context) {
        JobDataMap jobDataContext = context.getJobDetail().getJobDataMap();
        jobDataContext.getWrappedMap().forEach((key, value) -> log.debug("key '{}' and value '{}' ", key, value));
        List<CorrelatedUser> teamUsers = (List<CorrelatedUser>) jobDataContext.getOrDefault("members", Collections.emptyList());
        String repoSlug = (String) jobDataContext.getOrDefault("slug", "");

        List<String> messagesList = slackService.getSlackMessages(teamUsers, repoSlug);

        SlackMessageRequestDTO slackMessage = buildSlackRequestMessage(jobDataContext, messagesList);
        log.debug("Sending message {} to slack", slackMessage);

        slackApiRestService.sendMessageToChannel(slackMessage, ImmutableMap.of("slug", repoSlug));

        log.info("cronjob for pull requests executed for team {} in channel {}",
                jobDataContext.getString("team"), jobDataContext.getString("channel"));
    }

    /**
     * Building the {@link SlackMessageRequestDTO} object containing the slack message info like channel where the
     * message will be sent, the message body ..
     *
     * @param jobDataContext cron job context data containing the info about the slack team for pull requests
     * @param messagesList the list with slack messages containing the header and open pull requests messages
     * @return {@link SlackMessageRequestDTO} object containing the slack messages info
     */
    private SlackMessageRequestDTO buildSlackRequestMessage(JobDataMap jobDataContext, List<String> messagesList) {
        return SlackMessageRequestDTO.builder()
                    .channel(jobDataContext.getString("channel"))
                    .blocks(buildMessageBlocks(messagesList))
                    .build();
    }

    /**
     * Builds the list of block messages to be sent to the team corresponding slack channel. Each message block will
     * contain one of the header messages {@link SlackTeamService#NO_PR_SLACK_MESSAGE_TEMPLATE},
     * {@link SlackTeamService#START_SLACK_MESSAGE_TEMPLATE} and one message for each open pull request.
     *
     * @param messagesList the list with slack messages containing the header and open pull requests messages
     * @return a list of {@link SlackMessageRequestDTO.Block} blocks to be sent to a slack channel
     */
    private List<SlackMessageRequestDTO.Block> buildMessageBlocks(List<String> messagesList) {
        List<SlackMessageRequestDTO.Block> blocks = new ArrayList<>();
        messagesList.forEach(message -> {
            blocks.add(new SlackMessageRequestDTO.Block(new SlackMessageRequestDTO.Text(message)));
            blocks.add(new SlackMessageRequestDTO.Block(MessageType.DIVIDER.name().toLowerCase()));
        });
        return blocks;
    }

}