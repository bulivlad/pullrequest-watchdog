package io.watchdog.pullrequest.quartz;

import io.watchdog.pullrequest.model.slack.SlackTeam;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.quartz.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Class used to schedule jobs after a team was added/updated
 *
 * @author vladclaudiubulimac on 2019-03-05.
 */

@Slf4j
@Component
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class SchedulerService {

    Scheduler scheduler;

    @Autowired
    public SchedulerService(Scheduler scheduler) {
        this.scheduler = scheduler;
    }

    public boolean scheduleEventForTeam(SlackTeam slackTeam) throws SchedulerException {
        if(StringUtils.isEmpty(slackTeam.getCheckingSchedule())){
            return false;
        }

        JobDetail jobDetail = buildJobDetail(slackTeam);
        Trigger trigger = buildJobTrigger(jobDetail, slackTeam);
        scheduler.scheduleJob(jobDetail, trigger);

        return true;
    }

    public boolean rescheduleEventForTeam(SlackTeam slackTeam) throws SchedulerException {
        TriggerKey triggerKey = TriggerKey.triggerKey(slackTeam.getName() + "-" + slackTeam.getChannel() + "-" + slackTeam.getSlug() + "-trigger");

        if(StringUtils.isEmpty(slackTeam.getCheckingSchedule()) && !scheduler.checkExists(triggerKey)){
            return false;
        }

        if(StringUtils.isEmpty(slackTeam.getCheckingSchedule())){
            scheduler.unscheduleJob(triggerKey);
            log.debug("Unscheduled the job {}", triggerKey.getName());
            return true;
        }

        JobDetail jobDetail = buildJobDetail(slackTeam);
        Trigger trigger = buildJobTrigger(jobDetail, slackTeam);

        scheduler.rescheduleJob(triggerKey ,trigger);
        log.debug("Rescheduled the job {}", triggerKey.getName());
        return true;
    }

    private JobDetail buildJobDetail(SlackTeam slackTeam){
        JobDataMap jobDataMap = new JobDataMap();
        jobDataMap.put("team", slackTeam.getName());
        jobDataMap.put("channel", slackTeam.getChannel());
        jobDataMap.put("members", slackTeam.getMembers());
        jobDataMap.put("slug", slackTeam.getSlug());

        return JobBuilder.newJob(PullRequestRetrieverJob.class)
                .withIdentity(slackTeam.getName() + "-" + slackTeam.getChannel() + "-" + slackTeam.getSlug() + "-" + UUID.randomUUID().toString())
                .withDescription("Retrieve Pull Requests job")
                .usingJobData(jobDataMap)
                .storeDurably()
                .build();
    }

    private Trigger buildJobTrigger(JobDetail jobDetail, SlackTeam slackTeam){
        return TriggerBuilder.newTrigger()
                .forJob(jobDetail)
                .withIdentity(slackTeam.getName() + "-" + slackTeam.getChannel() + "-" + slackTeam.getSlug() + "-trigger")
                .withDescription("Retrieve Pull Requests trigger")
                .withSchedule(CronScheduleBuilder.cronSchedule(slackTeam.getCheckingSchedule())
                        .withMisfireHandlingInstructionFireAndProceed()
                        .build()
                        .getScheduleBuilder())
                .build();
    }

}
