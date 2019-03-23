package io.watchdog.pullrequest.service.slack;

import com.mongodb.MongoWriteException;
import io.watchdog.pullrequest.dto.PullRequestDTO;
import io.watchdog.pullrequest.dto.ReviewerDTO;
import io.watchdog.pullrequest.dto.slack.SlackUserDTO;
import io.watchdog.pullrequest.model.BitbucketUser;
import io.watchdog.pullrequest.model.CorrelatedUser;
import io.watchdog.pullrequest.model.slack.SlackTeam;
import io.watchdog.pullrequest.model.slack.SlackUser;
import io.watchdog.pullrequest.service.PullRequestRetrieveService;
import io.watchdog.pullrequest.service.TeamService;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import me.ramswaroop.jbot.core.slack.models.Event;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.quartz.SchedulerException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

/**
 * @author vladbulimac on 16/03/2019.
 */

@RunWith(MockitoJUnitRunner.class)
@FieldDefaults(level = AccessLevel.PRIVATE)
public class SlackTeamServiceTest {

    @Mock
    TeamService teamService;
    @Mock
    PullRequestRetrieveService pullRequestRetrieveService;
    @Mock
    SlackApiRestService slackApiRestService;

    @InjectMocks
    SlackTeamService slackTeamService;

    @Test
    public void saveTeam() throws SchedulerException {
        SlackTeam slackTeam = SlackTeam.builder().name("teamName").channel("channel").build();

        when(teamService.saveTeam(eq(slackTeam))).thenReturn(slackTeam);

        boolean result = slackTeamService.saveTeam(slackTeam);

        assertThat(result, is(true));
    }

    @Test
    public void saveTeamSchedulerException() throws SchedulerException {
        SlackTeam slackTeam = SlackTeam.builder().name("teamName").channel("channel").build();

        when(teamService.saveTeam(eq(slackTeam))).thenThrow(SchedulerException.class);

        boolean result = slackTeamService.saveTeam(slackTeam);

        assertThat(result, is(false));
    }

    @Test
    public void saveTeamMongoWriteException() throws SchedulerException {
        SlackTeam slackTeam = SlackTeam.builder().name("teamName").channel("channel").build();

        when(teamService.saveTeam(eq(slackTeam))).thenThrow(MongoWriteException.class);

        boolean result = slackTeamService.saveTeam(slackTeam);

        assertThat(result, is(false));
    }

    @Test
    public void updateTeam() throws SchedulerException {
        SlackTeam slackTeam = SlackTeam.builder().name("teamName").channel("channel").build();

        when(teamService.saveTeam(eq(slackTeam))).thenReturn(slackTeam);

        boolean result = slackTeamService.saveTeam(slackTeam);

        assertThat(result, is(true));
    }

    @Test
    public void updateTeamSchedulerException() throws SchedulerException {
        SlackTeam slackTeam = SlackTeam.builder().name("teamName").channel("channel").build();

        when(teamService.saveTeam(eq(slackTeam))).thenThrow(SchedulerException.class);

        boolean result = slackTeamService.saveTeam(slackTeam);

        assertThat(result, is(false));
    }

    @Test
    public void getSlackMessages() {
        BitbucketUser bitbucketUser = new BitbucketUser();
        bitbucketUser.setUsername("bbusername");
        SlackUser slackUser = new SlackUser();
        slackUser.setMention("bbusernamemention");
        slackUser.setUsername("bbusername");
        CorrelatedUser correlatedUser = new CorrelatedUser();
        correlatedUser.setBitbucketUser(bitbucketUser);
        correlatedUser.setSlackUser(slackUser);

        BitbucketUser bitbucketUser1 = new BitbucketUser();
        bitbucketUser1.setUsername("bbusername1");
        SlackUser slackUser1 = new SlackUser();
        slackUser1.setMention("bbusernamemention1");
        slackUser1.setUsername("bbusername1");
        CorrelatedUser correlatedUser1 = new CorrelatedUser();
        correlatedUser1.setBitbucketUser(bitbucketUser1);
        correlatedUser1.setSlackUser(slackUser1);

        List<CorrelatedUser> correlatedUsers = Arrays.asList(correlatedUser, correlatedUser1);

        ReviewerDTO reviewerDTO = new ReviewerDTO();
        reviewerDTO.setUsername("bbusername");
        ReviewerDTO reviewerDTO1 = new ReviewerDTO();
        reviewerDTO1.setUsername("bbusername1");

        PullRequestDTO pullRequestDTO = new PullRequestDTO();
        pullRequestDTO.setReviewers(Arrays.asList(reviewerDTO, reviewerDTO1));
        pullRequestDTO.setSourceBranch("sourceBranch1");
        pullRequestDTO.setLink("http://pr1");
        PullRequestDTO pullRequestDTO1 = new PullRequestDTO();
        pullRequestDTO1.setReviewers(Collections.singletonList(reviewerDTO1));
        pullRequestDTO1.setSourceBranch("sourceBranch2");
        pullRequestDTO1.setLink("http://pr2");

        List<PullRequestDTO> pullRequestDTOS = Arrays.asList(pullRequestDTO, pullRequestDTO1);

        List<String> expected = new ArrayList<>();
        expected.add("PRs waiting for reviewers today:\n");
        expected.add("sourceBranch1 - http://pr1 - <@bbusernamemention> <@bbusernamemention1> ");
        expected.add("sourceBranch2 - http://pr2 - <@bbusernamemention1> ");

        when(pullRequestRetrieveService.getUnapprovedPRs(anyList())).thenReturn(pullRequestDTOS);

        List<String> result = slackTeamService.getSlackMessages(correlatedUsers);

        assertThat(result, equalTo(expected));
    }

    @Test
    public void getSlackMessagesNoUnnaprovedPRs() {
        BitbucketUser bitbucketUser = new BitbucketUser();
        bitbucketUser.setUsername("bbusername");
        SlackUser slackUser = new SlackUser();
        slackUser.setMention("bbusernamemention");
        slackUser.setUsername("bbusername");
        CorrelatedUser correlatedUser = new CorrelatedUser();
        correlatedUser.setBitbucketUser(bitbucketUser);
        correlatedUser.setSlackUser(slackUser);

        BitbucketUser bitbucketUser1 = new BitbucketUser();
        bitbucketUser1.setUsername("bbusername1");
        SlackUser slackUser1 = new SlackUser();
        slackUser1.setMention("bbusernamemention1");
        slackUser1.setUsername("bbusername1");
        CorrelatedUser correlatedUser1 = new CorrelatedUser();
        correlatedUser1.setBitbucketUser(bitbucketUser1);
        correlatedUser1.setSlackUser(slackUser1);

        List<CorrelatedUser> correlatedUsers = Arrays.asList(correlatedUser, correlatedUser1);

        List<String> expected = new ArrayList<>();
        expected.add(":woohoo: No PRs to be reviewed today !\n");

        when(pullRequestRetrieveService.getUnapprovedPRs(anyList())).thenReturn(Collections.emptyList());

        List<String> result = slackTeamService.getSlackMessages(correlatedUsers);

        assertThat(result, equalTo(expected));
    }

    @Test
    public void buildSlackTeam() {
        Event event = new Event();
        event.setText("add team rdc2-team with members [<@user1>, <@user2>] and scheduler 0 20 11 1/1 * ? *");
        event.setChannelId("channelId");

        SlackUserDTO slackUserDTO = new SlackUserDTO();
        SlackUserDTO.User user = slackUserDTO.new User();
        user.setRealName("name1");
        user.setDisplayName("name1");
        user.setEmail("name1@example.com");
        slackUserDTO.setUser(user);

        SlackUserDTO slackUserDTO1 = new SlackUserDTO();
        SlackUserDTO.User user1 = slackUserDTO1.new User();
        user1.setRealName("name2");
        user1.setDisplayName("name2");
        user1.setEmail("name2@example.com");
        slackUserDTO1.setUser(user1);

        SlackUser slackUser = new SlackUser();
        slackUser.setEmail("name1@example.com");
        slackUser.setMention("user1");
        slackUser.setUsername("name1");
        slackUser.setName("name1");

        SlackUser slackUser1 = new SlackUser();
        slackUser1.setEmail("name2@example.com");
        slackUser1.setMention("user2");
        slackUser1.setUsername("name2");
        slackUser1.setName("name2");

        SlackTeam expected = SlackTeam.builder()
                .channel("channelId")
                .name("rdc2-team")
                .member(new CorrelatedUser(slackUser))
                .member(new CorrelatedUser(slackUser1))
                .checkingSchedule("0 20 11 1/1 * ? *")
                .build();

        when(slackApiRestService.retrieveSlackUserDetails("user1")).thenReturn(slackUserDTO);
        when(slackApiRestService.retrieveSlackUserDetails("user2")).thenReturn(slackUserDTO1);

        SlackTeam result = slackTeamService.buildSlackTeam(event);

        assertNotNull(result);
        assertEquals(expected, result);
    }

    @Test
    public void buildSlackTeamNoScheduler() {
        Event event = new Event();
        event.setText("add team rdc2-team with members [<@user1>, <@user2>] and scheduler ");
        event.setChannelId("channelId");

        SlackUserDTO slackUserDTO = new SlackUserDTO();
        SlackUserDTO.User user = slackUserDTO.new User();
        user.setRealName("name1");
        user.setDisplayName("name1");
        user.setEmail("name1@example.com");
        slackUserDTO.setUser(user);

        SlackUserDTO slackUserDTO1 = new SlackUserDTO();
        SlackUserDTO.User user1 = slackUserDTO1.new User();
        user1.setRealName("name2");
        user1.setDisplayName("name2");
        user1.setEmail("name2@example.com");
        slackUserDTO1.setUser(user1);

        SlackUser slackUser = new SlackUser();
        slackUser.setEmail("name1@example.com");
        slackUser.setMention("user1");
        slackUser.setUsername("name1");
        slackUser.setName("name1");

        SlackUser slackUser1 = new SlackUser();
        slackUser1.setEmail("name2@example.com");
        slackUser1.setMention("user2");
        slackUser1.setUsername("name2");
        slackUser1.setName("name2");

        SlackTeam expected = SlackTeam.builder()
                .channel("channelId")
                .name("rdc2-team")
                .member(new CorrelatedUser(slackUser))
                .member(new CorrelatedUser(slackUser1))
                .checkingSchedule("")
                .build();

        when(slackApiRestService.retrieveSlackUserDetails("user1")).thenReturn(slackUserDTO);
        when(slackApiRestService.retrieveSlackUserDetails("user2")).thenReturn(slackUserDTO1);

        SlackTeam result = slackTeamService.buildSlackTeam(event);

        assertNotNull(result);
        assertEquals(expected, result);
    }

    @Test
    public void removeTeam() {
        String channel = "channel";
        String teamName = "teamName";

        when(teamService.deleteTeam(eq(channel), eq(teamName))).thenReturn(true);

        boolean result = slackTeamService.removeTeam(channel, teamName);

        assertTrue(result);
    }

    @Test
    public void removeTeamIllegalArgumentException() {
        String channel = "channel";
        String teamName = "teamName";

        when(teamService.deleteTeam(eq(channel), eq(teamName))).thenThrow(IllegalArgumentException.class);

        boolean result = slackTeamService.removeTeam(channel, teamName);

        assertFalse(result);
    }

    @Test
    public void unscheduleTeam() throws SchedulerException {
        String channel = "channelId";
        String teamName = "teamName";
        SlackTeam slackTeam = SlackTeam.builder()
                .channel(channel)
                .name(teamName)
                .checkingSchedule("0 20 11 1/1 * ? *")
                .build();

        when(teamService.getSpecificTeamOrNewTeam(eq(channel), eq(teamName))).thenReturn(slackTeam);
        when(teamService.updateTeam(eq(slackTeam))).thenReturn(slackTeam);

        boolean result = slackTeamService.unscheduleTeam(channel, teamName);

        assertTrue(result);
    }

    @Test
    public void unscheduleTeamMongoWriteException() throws SchedulerException {
        String channel = "channelId";
        String teamName = "teamName";
        SlackTeam slackTeam = SlackTeam.builder()
                .channel(channel)
                .name(teamName)
                .checkingSchedule("0 20 11 1/1 * ? *")
                .build();

        when(teamService.getSpecificTeamOrNewTeam(eq(channel), eq(teamName))).thenReturn(slackTeam);
        when(teamService.updateTeam(eq(slackTeam))).thenThrow(MongoWriteException.class);

        boolean result = slackTeamService.unscheduleTeam(channel, teamName);

        assertFalse(result);
    }

    @Test
    public void unscheduleTeamSchedulerException() throws SchedulerException {
        String channel = "channelId";
        String teamName = "teamName";
        SlackTeam slackTeam = SlackTeam.builder()
                .channel(channel)
                .name(teamName)
                .checkingSchedule("0 20 11 1/1 * ? *")
                .build();

        when(teamService.getSpecificTeamOrNewTeam(eq(channel), eq(teamName))).thenReturn(slackTeam);
        when(teamService.updateTeam(eq(slackTeam))).thenThrow(SchedulerException.class);

        boolean result = slackTeamService.unscheduleTeam(channel, teamName);

        assertFalse(result);
    }
}