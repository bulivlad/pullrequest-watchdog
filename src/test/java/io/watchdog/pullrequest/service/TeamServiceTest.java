package io.watchdog.pullrequest.service;

import io.watchdog.pullrequest.config.RepositoryConfig;
import io.watchdog.pullrequest.dto.BitbucketUserDTO;
import io.watchdog.pullrequest.model.BitbucketUser;
import io.watchdog.pullrequest.model.CorrelatedUser;
import io.watchdog.pullrequest.model.slack.SlackTeam;
import io.watchdog.pullrequest.model.slack.SlackUser;
import io.watchdog.pullrequest.quartz.SchedulerService;
import io.watchdog.pullrequest.repository.TeamRepository;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.hamcrest.Matchers;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.quartz.SchedulerException;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * @author vladbulimac on 16/03/2019.
 */

@RunWith(MockitoJUnitRunner.class)
@FieldDefaults(level = AccessLevel.PRIVATE)
public class TeamServiceTest {

    @Mock
    TeamRepository teamRepository;
    @Mock
    BitBucketApiRestService bitBucketApiRestService;
    @Mock
    SchedulerService schedulerService;
    @Mock
    RepositoryConfig repositoryConfig;

    @InjectMocks
    TeamService teamService;

    @Test
    public void getAllTeams() {
        SlackTeam team = SlackTeam.builder().channel("channel").name("teamName").build();
        SlackTeam team1 = SlackTeam.builder().channel("channel1").name("teamName1").build();
        List<SlackTeam> slackTeams = Arrays.asList(team, team1);

        when(teamRepository.findAll()).thenReturn(slackTeams);

        List<SlackTeam> result = teamService.getAllTeams();

        assertThat(result, notNullValue());
        assertThat(result.isEmpty(), is(false));
        assertThat(result, equalTo(slackTeams));
    }

    @Test
    public void getAllTeamsInChannel() {
        String channel = "channel";
        SlackTeam team = SlackTeam.builder().channel(channel).name("teamName").build();
        SlackTeam team1 = SlackTeam.builder().channel(channel).name("teamName1").build();
        List<SlackTeam> slackTeams = Arrays.asList(team, team1);

        when(teamRepository.findSlackTeamsByChannel(eq(channel))).thenReturn(slackTeams);

        List<SlackTeam> result = teamService.getAllTeamsInChannel(channel);

        assertThat(result, notNullValue());
        assertThat(result, Matchers.hasSize(2));
        assertThat(result, equalTo(slackTeams));
    }

    @Test
    public void getAllTeamsWithScheduleExpression() {
        String channel = "channel";
        SlackTeam team = SlackTeam.builder().channel(channel).name("teamName").checkingSchedule("0 20 11 1/1 * ? *").build();
        List<SlackTeam> slackTeams = Arrays.asList(team);

        when(teamRepository.findAllByCheckingScheduleIsNotNull()).thenReturn(slackTeams);

        List<SlackTeam> result = teamService.getAllTeamsWithScheduleExpression();

        assertThat(result, notNullValue());
        assertThat(result.isEmpty(), is(false));
        assertThat(result, equalTo(slackTeams));
    }

    @Test
    public void saveTeam() throws SchedulerException {
        BitbucketUserDTO bitbucketUserDTO = new BitbucketUserDTO();
        bitbucketUserDTO.setUsername("bbusername");
        bitbucketUserDTO.setDisplayName("BB Username");

        BitbucketUser bitbucketUser = BitbucketUser.builder().username("bbusername").name("BB Username").build();

        SlackUser slackUser = new SlackUser();
        slackUser.setEmail("bbusername@example.com");

        String channel = "channel";
        SlackTeam slackTeam = SlackTeam.builder().channel(channel).name("teamName").member(new CorrelatedUser(slackUser)).slug("dummy-slug").build();

        when(bitBucketApiRestService.fetchBitbucketUserDetailsByEmail("bbusername@example.com")).thenReturn(bitbucketUserDTO);
        when(schedulerService.scheduleEventForTeam(eq(slackTeam))).thenReturn(true);
        when(teamRepository.save(eq(slackTeam))).thenReturn(slackTeam);

        SlackTeam result = teamService.saveTeam(slackTeam);

        assertThat(result, notNullValue());
        assertThat(result.getMembers().size(), equalTo(1));
        assertThat(result.getMembers().get(0).getSlackUser(), equalTo(slackUser));
        assertThat(result.getMembers().get(0).getBitbucketUser(), equalTo(bitbucketUser));
        assertThat(result.getSlug(), equalTo("dummy-slug"));
    }

    @Test
    public void saveTeamWithoutSlug() throws SchedulerException {
        BitbucketUserDTO bitbucketUserDTO = new BitbucketUserDTO();
        bitbucketUserDTO.setUsername("bbusername");
        bitbucketUserDTO.setDisplayName("BB Username");

        BitbucketUser bitbucketUser = BitbucketUser.builder().username("bbusername").name("BB Username").build();

        SlackUser slackUser = new SlackUser();
        slackUser.setEmail("bbusername@example.com");

        String channel = "channel";
        SlackTeam slackTeam = SlackTeam.builder().channel(channel).name("teamName").member(new CorrelatedUser(slackUser)).build();

        when(repositoryConfig.getSlug()).thenReturn("dummy-slug");
        when(bitBucketApiRestService.fetchBitbucketUserDetailsByEmail("bbusername@example.com")).thenReturn(bitbucketUserDTO);
        when(schedulerService.scheduleEventForTeam(eq(slackTeam))).thenReturn(true);
        when(teamRepository.save(eq(slackTeam))).thenReturn(slackTeam);

        SlackTeam result = teamService.saveTeam(slackTeam);

        assertThat(result, notNullValue());
        assertThat(result.getMembers().size(), equalTo(1));
        assertThat(result.getMembers().get(0).getSlackUser(), equalTo(slackUser));
        assertThat(result.getMembers().get(0).getBitbucketUser(), equalTo(bitbucketUser));
        assertThat(result.getSlug(), equalTo("dummy-slug"));
    }

    @Test(expected = SchedulerException.class)
    public void saveTeamSchedulerException() throws SchedulerException {
        BitbucketUserDTO bitbucketUserDTO = new BitbucketUserDTO();
        bitbucketUserDTO.setUsername("bbusername");
        bitbucketUserDTO.setDisplayName("BB Username");
        bitbucketUserDTO.setFirstName("BB");
        bitbucketUserDTO.setLastName("Username");

        SlackUser slackUser = new SlackUser();
        slackUser.setEmail("bbusername@example.com");

        String channel = "channel";
        SlackTeam slackTeam = SlackTeam.builder().channel(channel).name("teamName").member(new CorrelatedUser(slackUser)).slug("dummy-slug").build();

        when(bitBucketApiRestService.fetchBitbucketUserDetailsByEmail("bbusername@example.com")).thenReturn(bitbucketUserDTO);
        doThrow(SchedulerException.class).when(schedulerService).scheduleEventForTeam(eq(slackTeam));

        teamService.saveTeam(slackTeam);

        verify(teamRepository, times(0)).save(slackTeam);
    }

    @Test
    public void updateTeamNotExistent() throws SchedulerException {
        BitbucketUserDTO bitbucketUserDTO = new BitbucketUserDTO();
        bitbucketUserDTO.setUsername("bbusername");
        bitbucketUserDTO.setDisplayName("BB Username");
        bitbucketUserDTO.setFirstName("BB");
        bitbucketUserDTO.setLastName("Username");

        BitbucketUser bitbucketUser = BitbucketUser.builder().username("bbusername").name("BB Username").build();

        SlackUser slackUser = new SlackUser();
        slackUser.setEmail("bbusername@example.com");

        String channel = "channel";
        String teamName = "teamName";
        SlackTeam slackTeam = SlackTeam.builder().channel(channel).name(teamName).member(new CorrelatedUser(slackUser)).build();

        when(teamRepository.findSlackTeamByChannelAndName(eq(channel), eq(teamName))).thenReturn(Optional.empty());
        when(schedulerService.rescheduleEventForTeam(eq(slackTeam))).thenReturn(true);
        when(bitBucketApiRestService.fetchBitbucketUserDetailsByEmail(eq("bbusername@example.com"))).thenReturn(bitbucketUserDTO);
        when(teamRepository.save(eq(slackTeam))).thenReturn(slackTeam);

        SlackTeam result = teamService.updateTeam(slackTeam);

        assertThat(result, notNullValue());
        assertThat(result.getMembers().size(), equalTo(1));
        assertThat(result.getMembers().get(0).getSlackUser(), equalTo(slackUser));
        assertThat(result.getMembers().get(0).getBitbucketUser(), equalTo(bitbucketUser));
    }

    @Test
    public void updateTeamExistent() throws SchedulerException {
        BitbucketUserDTO bitbucketUserDTO = new BitbucketUserDTO();
        bitbucketUserDTO.setUsername("bbusername");
        bitbucketUserDTO.setDisplayName("BB Username");
        bitbucketUserDTO.setFirstName("BB");
        bitbucketUserDTO.setLastName("Username");

        BitbucketUser bitbucketUser = BitbucketUser.builder().username("bbusername").name("BB Username").build();

        SlackUser slackUser = new SlackUser();
        slackUser.setEmail("bbusername@example.com");

        String channel = "channel";
        String teamName = "teamName";
        SlackTeam slackTeam = SlackTeam.builder().channel(channel).name(teamName).member(new CorrelatedUser(slackUser)).build();
        SlackTeam existentSlackTeam = SlackTeam.builder().id("1").channel(channel).name(teamName).member(new CorrelatedUser(slackUser)).build();

        when(teamRepository.findSlackTeamByChannelAndName(eq(channel), eq(teamName))).thenReturn(Optional.of(existentSlackTeam));
        when(schedulerService.rescheduleEventForTeam(eq(slackTeam))).thenReturn(true);
        when(bitBucketApiRestService.fetchBitbucketUserDetailsByEmail(eq("bbusername@example.com"))).thenReturn(bitbucketUserDTO);
        when(teamRepository.save(eq(slackTeam))).thenReturn(slackTeam);

        SlackTeam result = teamService.updateTeam(slackTeam);

        assertThat(result, notNullValue());
        assertThat(result.getMembers().size(), equalTo(1));
        assertThat(result.getId(), equalTo(existentSlackTeam.getId()));
        assertThat(result.getMembers().get(0).getSlackUser(), equalTo(slackUser));
        assertThat(result.getMembers().get(0).getBitbucketUser(), equalTo(bitbucketUser));
    }

    @Test(expected = SchedulerException.class)
    public void updateTeamExistentSchedulerException() throws SchedulerException {
        SlackUser slackUser = new SlackUser();
        slackUser.setEmail("bbusername@example.com");

        String channel = "channel";
        String teamName = "teamName";
        SlackTeam slackTeam = SlackTeam.builder().channel(channel).name(teamName).member(new CorrelatedUser(slackUser)).build();
        SlackTeam existentSlackTeam = SlackTeam.builder().id("1").channel(channel).name(teamName).member(new CorrelatedUser(slackUser)).build();

        when(teamRepository.findSlackTeamByChannelAndName(eq(channel), eq(teamName))).thenReturn(Optional.of(existentSlackTeam));
        doThrow(SchedulerException.class).when(schedulerService).rescheduleEventForTeam(eq(slackTeam));

        teamService.updateTeam(slackTeam);
    }

    @Test
    public void deleteTeam() throws SchedulerException {
        String channel = "channel";
        String teamName = "teamName";
        String slug = "dummy-slug";

        SlackTeam slackTeam = SlackTeam.builder().channel(channel).name(teamName).build();

        when(teamRepository.findSlackTeamByChannelAndNameAndSlug(eq(channel), eq(teamName), eq(slug))).thenReturn(Optional.empty());
        doNothing().when(teamRepository).delete(eq(slackTeam));

        boolean result = teamService.deleteTeam(channel, teamName, slug);

        assertThat(result, is(true));
    }

    @Test
    public void getSpecificTeamNotExistent() {
        String channel = "channel";
        String teamName = "teamName";
        SlackTeam slackTeam = SlackTeam.builder().channel(channel).name(teamName).build();

        when(teamRepository.findSlackTeamByChannelAndName(eq(channel), eq(teamName))).thenReturn(Optional.empty());

        SlackTeam result = teamService.getSpecificTeamOrNewTeam(channel, teamName);

        assertThat(result, notNullValue());
        assertThat(result, equalTo(slackTeam));
    }

    @Test
    public void getSpecificTeamExistent() {
        SlackUser slackUser = new SlackUser();
        slackUser.setEmail("bbusername@example.com");

        String channel = "channel";
        String teamName = "teamName";
        SlackTeam slackTeam = SlackTeam.builder().channel(channel).name(teamName).member(new CorrelatedUser(slackUser)).build();

        when(teamRepository.findSlackTeamByChannelAndName(eq(channel), eq(teamName))).thenReturn(Optional.of(slackTeam));

        SlackTeam result = teamService.getSpecificTeamOrNewTeam(channel, teamName);

        assertThat(result, notNullValue());
        assertThat(result, equalTo(slackTeam));
    }
}