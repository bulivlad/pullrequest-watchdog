package io.watchdog.pullrequest.quartz;

import io.watchdog.pullrequest.model.slack.SlackTeam;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.TriggerKey;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * @author vladbulimac on 16/03/2019.
 */

@RunWith(MockitoJUnitRunner.class)
@FieldDefaults(level = AccessLevel.PRIVATE)
public class SchedulerServiceTest {

    @Mock
    Scheduler scheduler;

    @InjectMocks
    SchedulerService schedulerService;

    @Before
    public void setUp() {
        schedulerService = new SchedulerService(scheduler);
    }

    @Test
    public void scheduleEventForTeamWithCronExpression() throws SchedulerException {
        SlackTeam team = SlackTeam.builder().name("teamName").channel("channel").checkingSchedule("0 20 11 1/1 * ? *").build();
        boolean result = schedulerService.scheduleEventForTeam(team);

        assertThat(result, is(true));
    }

    @Test
    public void scheduleEventForTeamWithoutCronExpression() throws SchedulerException {
        SlackTeam team = SlackTeam.builder().name("teamName").channel("channel").build();
        boolean result = schedulerService.scheduleEventForTeam(team);

        assertThat(result, is(false));
        verify(scheduler, times(0)).scheduleJob(any());
    }

    @Test
    public void rescheduleEventForTeam() throws SchedulerException {
        SlackTeam team = SlackTeam.builder().name("teamName").channel("channel").checkingSchedule("0 20 11 1/1 * ? *").build();

        boolean result = schedulerService.rescheduleEventForTeam(team);

        assertThat(result, is(true));
        verify(scheduler).rescheduleJob(any(), any());
    }

    @Test
    public void rescheduleEventForTeamWithoutCronExpression() throws SchedulerException {
        SlackTeam team = SlackTeam.builder().name("teamName").channel("channel").slug("dummy-slug").build();

        TriggerKey triggerKey = TriggerKey.triggerKey(team.getName() + "-" + team.getChannel() + "-" + team.getSlug() + "-trigger");
        when(scheduler.checkExists(eq(triggerKey))).thenReturn(true);

        boolean result = schedulerService.rescheduleEventForTeam(team);

        assertThat(result, is(true));
        verify(scheduler).unscheduleJob(triggerKey);
        verify(scheduler, times(0)).rescheduleJob(any(), any());
    }

    @Test
    public void rescheduleEventForTeamNoTriggerExistent() throws SchedulerException {
        SlackTeam team = SlackTeam.builder().name("teamName").channel("channel").build();

        TriggerKey triggerKey = TriggerKey.triggerKey(team.getName() + "-" + team.getChannel() + "-trigger");
        when(scheduler.checkExists(eq(triggerKey))).thenReturn(false);

        boolean result = schedulerService.rescheduleEventForTeam(team);

        assertThat(result, is(false));
    }
}