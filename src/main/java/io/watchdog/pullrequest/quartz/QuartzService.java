package io.watchdog.pullrequest.quartz;

import io.watchdog.pullrequest.model.slack.SlackTeam;
import io.watchdog.pullrequest.service.TeamService;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.quartz.SchedulerException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.List;

/**
 * Class used to re-schedule add jobs saved in the database after application started
 *
 * @author vladclaudiubulimac on 2019-03-05.
 */

@Slf4j
@Component
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class QuartzService {

    TeamService teamService;
    SchedulerService schedulerService;

    @Autowired
    public QuartzService(TeamService teamService, SchedulerService schedulerService) {
        this.teamService = teamService;
        this.schedulerService = schedulerService;
    }

    @PostConstruct
    public void rescheduleAllJobs(){
        log.info("Start rescheduling the cronjobs for all teams");
        List<SlackTeam> allTeamsWithScheduleExpression = teamService.getAllTeamsWithScheduleExpression();
        allTeamsWithScheduleExpression.forEach(this::scheduleJobAndLog);
    }

    private void scheduleJobAndLog(SlackTeam slackTeam)  {
        log.debug("Scheduling the job for team '{}' for slug '{}' in channel '{}' with cron expression '{}'",
                slackTeam.getName(),
                slackTeam.getSlug(),
                slackTeam.getChannel(),
                slackTeam.getCheckingSchedule());
        boolean scheduled = false;
        try {
            scheduled = schedulerService.scheduleEventForTeam(slackTeam);
        } catch (SchedulerException ex){
            log.error("Could not schedule cronjob for team " + slackTeam.getName() + " in channel " + slackTeam.getChannel() + "and slug " + slackTeam.getSlug(), ex);
        }
        log.debug("Scheduled {}", scheduled);
    }

}
