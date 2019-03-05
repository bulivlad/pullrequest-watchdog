package io.watchdog.pullrequest.quartz;

import io.watchdog.pullrequest.model.SlackUser;
import io.watchdog.pullrequest.model.User;
import io.watchdog.pullrequest.service.slack.SlackTeamService;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

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

    @Autowired
    public PullRequestRetrieverJob(SlackTeamService slackService) {
        this.slackService = slackService;
    }

    public void execute(JobExecutionContext context) {
        JobDataMap mergedJobDataMap = context.getJobDetail().getJobDataMap();
        mergedJobDataMap.getWrappedMap().forEach((key, value) -> log.debug("key '{}' and value '{}' ", key, value));
        List<SlackUser> teamSlackUsers = (List) mergedJobDataMap.getOrDefault("members", Collections.emptyList());
        List<String> reviewers = teamSlackUsers.stream().map(User::getUsername).collect(Collectors.toList());

        List<String> messagesList = slackService.getPullRequestsWithSlackUsers(reviewers);
        log.info(messagesList.toString());
        log.info("cronjob for pull requests executed");
    }

}